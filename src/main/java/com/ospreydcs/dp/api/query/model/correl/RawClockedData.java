/*
 * Project: dp-api-common
 * File:	RawClockedData.java
 * Package: com.ospreydcs.dp.api.query.model.correl
 * Type: 	RawClockedData
 *
 * Copyright 2010-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 * @author Christopher K. Allen
 * @org    OspreyDCS
 * @since Mar 11, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.correl;

import java.time.Instant;
import java.util.ArrayList;

import com.ospreydcs.dp.api.common.DpTimestampCase;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.grpc.util.ProtoTime;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.SamplingClock;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

/**
 * <p>
 * Subclass of <code>RawCorrelatedData</code> supporting process data that is sampled with a uniform clock.
 * </p>   
 *
 * @author Christopher K. Allen
 * @since Mar 11, 2025
 *
 */
public class RawClockedData extends RawCorrelatedData {

    
    //
    // Defining Attributes
    //
    
    /** The sampling clock message to correlate against */
    private final SamplingClock     msgClock;
    
    
    //
    // Instance Attributes
    //
    
    /** The timestamp vector for this correlated data block */
    private final ArrayList<Instant>    vecTms;

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>RawClockedData</code> instance.
     * </p>
     *
     * @param msgBucket Protocol Buffers message containing initializing data
     * 
     * @throws IllegalArgumentException argument does not contains a sampling clock
     */
    protected RawClockedData(QueryDataResponse.QueryData.DataBucket msgBucket) throws IllegalArgumentException {
        super(DpTimestampCase.SAMPLING_CLOCK, ProtoTime.range(msgBucket.getDataTimestamps().getSamplingClock()), msgBucket);

        // Check argument
        if (!msgBucket.getDataTimestamps().hasSamplingClock()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Argument does not contain a sampling clock.";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalArgumentException();
        }
        
        // Extract sampling clock and create timestamp vector
        this.msgClock = msgBucket.getDataTimestamps().getSamplingClock();
        this.vecTms = ProtoMsg.toUniformSamplingClock(this.msgClock).createTimestamps();
    }
    
    
    //
    // Base Class Abstract Methods
    //

    /**
     * @see com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData#getSampleCount()
     */
    @Override
    public int getSampleCount() {
        return this.msgClock.getCount();
    }

    /**
     * @see com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData#getTimestampVector()
     */
    @Override
    public ArrayList<Instant> getTimestampVector() {
        return this.vecTms;
    }

    /**
     * @see com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData#insertBucketData(com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData.DataBucket)
     */
    @Override
    synchronized
    public boolean insertBucketData(QueryDataResponse.QueryData.DataBucket msgBucket) {
        
        // Check argument for sampling clock
        if (!msgBucket.getDataTimestamps().hasSamplingClock())
            return false;
            
        SamplingClock   msgBckClk = msgBucket.getDataTimestamps().getSamplingClock();
        DataColumn      msgBckCol = msgBucket.getDataColumn();
        String          strSrcNm  = msgBckCol.getName();
        
        // Check if list addition is possible 
        // - must have same sampling clock
        if (!ProtoTime.equals(this.msgClock, msgBckClk)) 
            return false;

        // - data source must not already be present
        if (super.setSrcNms.contains(strSrcNm))
            return false;
        
        // Add the data column and record its data source 
        super.lstMsgCols.add( msgBckCol );
        super.setSrcNms.add( strSrcNm );
        
        return true;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Returns the Protocol Buffers message describing the sampling clock for all correlated data.
     * </p>
     * <p>
     * The returned message identifies the sampling clock to which all data message are
     * correlated.  That is, the returned message is applicable to all data returned
     * by <code>{@link #getRawDataMessages()}</code>.
     * </p>
     * 
     * @return Protocol Buffers message describing the time-series sampling clock
     * 
     * @see #getRawDataMessages()
     */
    public final SamplingClock getSamplingClockMessage() {
        return this.msgClock;
    }

}
