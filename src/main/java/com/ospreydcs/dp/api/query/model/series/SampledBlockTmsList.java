/*
 * Project: dp-api-common
 * File:	SampledBlockTmsList.java
 * Package: com.ospreydcs.dp.api.query.model.series
 * Type: 	SampledBlockTmsList
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
 * @since Mar 24, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.series;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.query.model.correl.RawTmsListData;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.TimestampList;

/**
 * <p>
 * Subclass of <code>SampledBlock</code> supporting generalized sampling with timestamp lists.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Mar 24, 2025
 *
 */
public class SampledBlockTmsList extends SampledBlock {
    
    
    //
    // Initializing Attributes
    //
    
    /** The raw data correlated to a timestamp list from which this sampled block is built */
    private final RawTmsListData       datRawTmsLst;

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new, initialized <code>SampledBlockTmsList</code> instance from the given argument.
     * </p>
     *
     * @param datRawTmsLst    raw, time-series data correlated to a timestamp list
     * 
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws TypeNotPresentException  an unsupported data type was detected within the argument
     */
    public SampledBlockTmsList(RawTmsListData datRawTmsLst)             
            throws IllegalArgumentException, MissingResourceException, IllegalStateException, TypeNotPresentException {
        super();
        
        if (BOL_ERROR_CHK)
            super.verifySourceData(datRawTmsLst);   // throws IllegalArgument exception
        
        this.datRawTmsLst = datRawTmsLst;
        super.initialize();    // throws exceptions
    }
    
    
    //
    // Base Class Abstract Methods
    //

    /**
     * @see com.ospreydcs.dp.api.query.model.series.SampledBlock#createTimestampsVector()
     */
    @Override
    protected ArrayList<Instant> createTimestampsVector() {
        TimestampList       msgTms = datRawTmsLst.getTimestampListMessage();
        List<Instant>       lstTms = ProtoMsg.toInstantList(msgTms);
        ArrayList<Instant>  vecTms = new ArrayList<>(lstTms);
        
        return vecTms;
    }

    /**
     * @see com.ospreydcs.dp.api.query.model.series.SampledBlock#createTimeSeriesVector()
     */
    @Override
    protected ArrayList<SampledTimeSeries<Object>> createTimeSeriesVector() 
            throws MissingResourceException, IllegalStateException, TypeNotPresentException {

        // List of time series are created, one for each unique data source name
        List<SampledTimeSeries<Object>>  lstCols; // = new ArrayList<>();

        // Create processing stream based upon number of data columns
        List<DataColumn>    lstMsgDataCols = this.datRawTmsLst.getRawDataMessages();
        
        // TODO 
        // - I think there is an IllegalStateException thrown intermittently here
        // "End size 99 is less than fixed size 100"
        if (BOL_CONCURRENCY && (lstMsgDataCols.size() > SZ_CONCURRENCY_PIVOT)) {
            lstCols = lstMsgDataCols
                    .parallelStream()
                    .<SampledTimeSeries<Object>>map(SampledTimeSeries::from)           // throws MissingResourceException, IllegalStateExcepiont, TypeNotPresentException
                    .toList();
            
        } else {
            lstCols = lstMsgDataCols
                    .stream()
                    .<SampledTimeSeries<Object>>map(SampledTimeSeries::from)           // throws MissingResourceException, IllegalStateExcepiont, TypeNotPresentException
                    .toList();
        }
        
        // Create the final ArrayList (vector) for time-series and return
        ArrayList<SampledTimeSeries<Object>>  vecCols = new ArrayList<>(lstCols);
        
        return vecCols;
    }

    //
    //  Support Methods
    //
    
}
