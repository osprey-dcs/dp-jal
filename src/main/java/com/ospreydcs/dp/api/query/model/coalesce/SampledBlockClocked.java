/*
 * Project: dp-api-common
 * File:	SampledBlockClocked.java
 * Package: com.ospreydcs.dp.api.query.model.coalesce
 * Type: 	SampledBlockClocked
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
package com.ospreydcs.dp.api.query.model.coalesce;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import com.ospreydcs.dp.api.common.IDataTable;
import com.ospreydcs.dp.api.common.UniformSamplingClock;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.query.model.correl.CorrelatedQueryDataOld;
import com.ospreydcs.dp.api.query.model.correl.RawClockedData;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.SamplingClock;

/**
 * <p>
 * Subclass of <code>SampledBlock</code> supporting time-series data sampled with a uniform clock.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 24, 2025
 *
 */
public class SampledBlockClocked extends SampledBlock {
    
    
    //
    // Initializing Attributes
    //
    
    /** The raw data correlated to a uniform sampling clock */
    private final RawClockedData        datRawClk;
    

    /**
     * <p>
     * Constructs a new, initialized <code>SampledBlockClocked</code> instance from the given argument.
     * </p>
     *
     * @param datRawClk raw query data correlated to a uniform sampling clock
     * 
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws TypeNotPresentException  an unsupported data type was detected within the argument
     */
    public SampledBlockClocked(RawClockedData datRawClk) {
        super();

        if (BOL_ERROR_CHK)
            super.verifySourceData(datRawClk);   // throws IllegalArgument exception
        
        this.datRawClk = datRawClk;
        super.initialize();
    }

    /**
     * @see com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#createTimestampsVector()
     */
    @Override
    protected ArrayList<Instant> createTimestampsVector() {
        
        SamplingClock           msgClock = this.datRawClk.getSamplingClockMessage();
        UniformSamplingClock    clkTms = ProtoMsg.toUniformSamplingClock(msgClock);
        ArrayList<Instant>      vecTms = clkTms.createTimestamps();
        
        return vecTms;
    }

    /**
     * <p>
     * Creates all <code>{@link SampledTimeSeries}</code> objects and returns them as a vector (i.e. array list).
     * </p>
     * <p>
     * Creates all the <code>{@link SampledTimeSeries}</code> instances for this sampling
     * block using the given argument as source data.  The returned object is a vector of
     * of such objects respecting the the argument order.  It is assumed that the data
     * sources within the argument are all unique. Thus, this condition should be checked.
     * </p>
     * <p>
     * This method is intended for the creation of attribute <code>{@link #vecTimeSeries}</code>, also needed for the
     * <code>{@link IDataTable}</code> implementation (i.e., for table column indexing).
     * The returned vector is ordered according to the ordering of the argument entries.
     * </p>
     * <h2>Concurrency</h2>
     * If concurrency is enabled (i.e., <code>{@link #BOL_CONCURRENCY}</code> = <code>true</code>),
     * this method utilizes streaming parallelism if the argument size is greater than the
     * pivot number <code>{@link #SZ_CONCURRENCY_PIVOT}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The returned Java container is mutable, additional entries can be added (e.g., null series).
     * </li>
     * <li>
     * The argument should have already been checked for duplicate data source names using
     * <code>{@link CorrelatedQueryDataOld#verifySourceUniqueness()}</code>.
     * </li>
     * </ul>
     * </p>
     * 
     * @param lstMsgDataCols   source data for sampled time series creation
     * 
     * @return  vector containing a time series for each data column in the argument, ordered by argument
     * 
     * @throws MissingResourceException      a data column message contained no data
     * @throws IllegalStateException         the argument contained or non-uniform data types
     * @throws TypeNotPresentException an unsupported data type was detected within the argument
     */
    @Override
    protected ArrayList<SampledTimeSeries<Object>> createTimeSeriesVector() 
            throws MissingResourceException, IllegalStateException, TypeNotPresentException {

        // List of time series are created, one for each unique data source name
        List<SampledTimeSeries<Object>>  lstCols; // = new ArrayList<>();

        // Create processing stream based upon number of data columns
        List<DataColumn>    lstMsgDataCols = this.datRawClk.getRawDataMessages();
        
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

}
