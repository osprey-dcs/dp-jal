/*
 * Project: dp-api-common
 * File:	RawTmsListData.java
 * Package: com.ospreydcs.dp.api.query.model.correl
 * Type: 	RawTmsListData
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
 * @since Mar 12, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.correl;

import java.time.Instant;
import java.util.ArrayList;

import com.ospreydcs.dp.api.common.DpTimestampCase;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.grpc.util.ProtoTime;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.DataBucket;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.TimestampList;

/**
 * <p>
 * Subclass of <code>RawCorrelatedData</code> supporting sampled data with explicit timestamps.
 * </p>   
 *
 * @author Christopher K. Allen
 * @since Mar 12, 2025
 *
 */
public class RawTmsListData extends RawCorrelatedData {

    
    //
    // Defining Attributes
    //
    
    /** The sampling clock message to correlate against */
    private final TimestampList     msgTmsLst;
    
    
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
     * Constructs a new <code>RawTmsListData</code> instance.
     * </p>
     *
     * @param msgBucket Protocol Buffers message containing initializing data
     * 
     * @throws IllegalArgumentException argument does not contains a timestamp list
     */
    protected RawTmsListData(DataBucket msgBucket) {
        super(DpTimestampCase.TIMESTAMP_LIST, ProtoTime.range( msgBucket.getDataTimestamps().getTimestampList() ), msgBucket);
        
        // Check argument
        if (!msgBucket.getDataTimestamps().hasTimestampList()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Argument does not contain a timestamp list.";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalArgumentException();
        }
        
        // Extract the timestamp message and create timestamp vector
        this.msgTmsLst = msgBucket.getDataTimestamps().getTimestampList();
        this.vecTms = new ArrayList<>( ProtoMsg.toInstantList(this.msgTmsLst) );
    }

    
    //
    // Base Class Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData#getSampleCount()
     */
    @Override
    public int getSampleCount() {
        return this.msgTmsLst.getTimestampsCount();
    }

    /**
     * @see com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData#computeRawTmsAllocation()
     */
    @Override
    public long computeRawTmsAllocation() {
        return this.msgTmsLst.getSerializedSize();
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
    public boolean insertBucketData(DataBucket msgBucket) {

        // Check argument for timestamp list
        if (!msgBucket.getDataTimestamps().hasTimestampList())
            return false;
            
        TimestampList   msgBckTms = msgBucket.getDataTimestamps().getTimestampList();
        DataColumn      msgBckCol = msgBucket.getDataColumn();
        String          strSrcNm  = msgBckCol.getName();
        
        // Check if list addition is possible 
        // - data source must not already be present
        if (super.setSrcNms.contains(strSrcNm))
            return false;
        
        // - must have same timestamp lists
        if (!ProtoTime.equals(this.msgTmsLst, msgBckTms)) 
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
     * Returns the Protocol Buffers message describing the timestamp list for all correlated data.
     * </p>
     * <p>
     * The returned message identifies the timestamp list to which all data message are
     * correlated.  That is, the returned message is applicable to all data returned
     * by <code>{@link #getRawDataMessages()}</code>.
     * </p>
     * 
     * @return Protocol Buffers message describing the time-series timestamp list
     * 
     * @see #getRawDataMessages()
     */
    public final TimestampList getTimestampListMessage() {
        return this.msgTmsLst;
    }

}
