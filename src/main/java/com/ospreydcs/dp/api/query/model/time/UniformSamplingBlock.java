/*
 * Project: dp-api-common
 * File:	UniformSamplingBlock.java
 * Package: com.ospreydcs.dp.api.query.model.time
 * Type: 	UniformSamplingBlock
 *
 * Copyright 2010-2023 the original author or authors.
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
 * @since Jan 30, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.time;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.model.ResultRecord;
import com.ospreydcs.dp.api.query.model.data.SampledTimeSeries;
import com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.FixedIntervalTimestampSpec;

/**
 *
 * @author Christopher K. Allen
 * @since Jan 30, 2024
 *
 */
public class UniformSamplingBlock implements Comparable<UniformSamplingBlock> {

    
    //
    //  Class Types
    //
    
    /**
     * <p>
     * <code>{@link Comparator}</code> class for ordered comparison of two <code>UniformSamplingBlock</code> instances.
     * </p>
     * <p>
     * The comparison here is performed with the start times of the sampling intervals represented by 
     * <code>{@link UniformSamplingBlock}</code> instances returned from the 
     * <code>{@link UniformSamplingBlock#getStartInstant()</code> method.
     * Instances are intended for use in Java container construction.
     * </p>
     *
     * @author Christopher K. Allen
     * @since Jan 31, 2024
     *
     */
    public static class StartTimeComparator implements Comparator<UniformSamplingBlock> {

        //
        // Creators
        //
        
        /**
         * <p>
         * Creates a new <code>StartTimeComparator</code> instance.
         * </p>
         * <p>
         * The comparator compares the start time instances of the internal
         * uniform sampling clock.
         * </p>
         * 
         * @return  new  <code>StartTimeComparator</code> instance ready for use
         */
        public static StartTimeComparator   newInstance() {
            return new StartTimeComparator();
        }
        
        //
        // Comparator Interface
        //
        
        /**
         *
         * @see @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(UniformSamplingBlock o1, UniformSamplingBlock o2) {
            Instant t1 = o1.getStartInstant();
            Instant t2 = o2.getStartInstant();
            
            return t1.compareTo(t2);
        }
    }
    

    //
    // Application Resources
    //
    
    /** The Data Platform API default configuration parameter set */
    private static final DpQueryConfig  CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    
    //
    // Class Constants
    //
    
    /** Is logging active */
    public static final boolean    BOL_LOGGING = CFG_QUERY.logging.active;
    
    
//    /** Parallelism timeout limit  - for parallel thread pool tasks */
//    public static final long       LNG_TIMEOUT = CFG_QUERY.timeout.limit;
//    
//    /** Parallelism timeout units - for parallel thread pool tasks */
//    public static final TimeUnit   TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
    /** Parallelism tuning parameter - pivot to parallel processing when lstMsgDataCols size hits this limit */
    public static final int        SZ_MSGCOLS_PIVOT = CFG_QUERY.concurrency.pivotSize;
    
    
    //
    // Defining Attributes
    //
    
//    /** Protobuf message describing uniform sampling duration */
//    private final   FixedIntervalTimestampSpec  msgClockParams;
//    
//    /** Protobuf sampled (correlated) data sets for sampling duration */
//    private final   List<DataColumn>            lstMsgDataCols;

    //
    // Instance Attributes
    //
    
    /** The uniform clock duration for this sample block */
    private final   UniformClockDuration    clkParams;
    
    /** Set of data source names for sample block */
    private final   Set<String>             setSourceNames;
    
    /** Map of data source name to data source sample values */
    private final   Map<String, SampledTimeSeries>  mapSrcToSmpls;
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new <code>UniformSamplingBlock</code> fully-populated from argument data.
     * </p>
     * <p>
     * After creation the new instance contains the sampling clock and all sampled time series
     * data as represented by the argument.  If the data within the argument is corrupt or 
     * inconsistent an exception is thrown.
     * </p>
     * 
     * @param cqdSampleBlock    source data for the new instance
     * 
     * @return  new <code>UniformSamplingBlock</code> instance populated with data from the argument
     * 
     * @throws IllegalArgumentException the argument is corrupt: missing data column, empty data column, or duplicate data source names (see message)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws UnsupportedOperationException an unsupported data type was detected within the argument
     */
    public static UniformSamplingBlock  from(CorrelatedQueryData cqdSampleBlock) 
            throws IllegalArgumentException, IllegalStateException, UnsupportedOperationException 
    {
        return new UniformSamplingBlock(cqdSampleBlock);
    }
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>UniformSamplingBlock</code>.
     * </p>
     * <p>
     * After construction the new instance contains the sampling clock and all sampled time series
     * data as represented by the argument.  If the data within the argument is corrupt or 
     * inconsistent an exception is thrown.
     * </p>
     *
     * @param cqdSampleBlock    source data for the new instance
     * 
     * @throws IllegalArgumentException the argument is corrupt: missing data column, empty data column, or duplicate data source names (see message)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws UnsupportedOperationException an unsupported data type was detected within the argument
     */
    public UniformSamplingBlock(CorrelatedQueryData cqdSampleBlock) 
            throws IllegalArgumentException, IllegalStateException, UnsupportedOperationException {
        
        // Check the argument for corruption - i.e., data source name uniqueness
        ResultRecord    result = cqdSampleBlock.verifySourceUniqueness();
        if (result.failed())
            throw new IllegalArgumentException("Correlated data block is corrupt: " + result.getCause());
        
        // Extract the relevant Protobuf message from the argument
        FixedIntervalTimestampSpec  msgClockParams = cqdSampleBlock.getSamplingMessage();
        List<DataColumn>            lstMsgDataCols = cqdSampleBlock.getAllDataMessages();
        
        // Compute and set the sampling block attributes
        this.clkParams = UniformClockDuration.from(msgClockParams);
        this.setSourceNames = cqdSampleBlock.getDataSourceNames();
        this.mapSrcToSmpls = this.createTimeSeries(lstMsgDataCols);
    }

    
    //
    // Attribute and Property Query
    //
    
    /**
     * <p>
     * Returns the starting time instant of the sampling interval for this sampling block
     * </p>
     * <p>
     * This is a convenience method where the returned value is taken from the 
     * uniform clock parameters.
     * <p>
     * 
     * @return sampling interval start instant 
     */
    public final Instant getStartInstant() {
        return this.clkParams.getStartInstant();
    }
    
    /**
     * <p>
     * Compares the sampling domain of the given sampling set with this one for collision.
     * </p>
     * <p>
     * Returns <code>true</code> if the given sampling time domain of the argument has a 
     * non-empty set intersection with this one.  Time domains are treated as closed intervals, 
     * so even if domain intervals share only a common end point a value <code>true</code> 
     * is returned.
     * </p>
     * <p>
     * This is a convenience method where the returned value is determined from the 
     * uniform clock parameters of this block and the argument.
     * <p>
     * 
     * @param usbCmp uniform sampling set under domain comparison 
     * 
     * @return <code>true</code> if the given sampling time domain collides with this one,
     *         <code>false</code> otherwise
     *         
     * @see UniformClockDuration#hasIntersection(UniformClockDuration)
     */
    public boolean  hasIntersection(UniformSamplingBlock usbCmp) {
        return this.clkParams.hasIntersection(usbCmp.clkParams);
    }

    /**
     * <p>
     * Returns the uniform sampling clock parameters for this sampling block.
     * </p>
     * <p>
     * Returns the sampling clock which applies to all time series data within this block.
     * </p>
     * 
     * @return  the sampling clock parameters
     * 
     * @see UniformClockDuration
     */
    public final UniformClockDuration getSamplingClock() {
        return this.clkParams;
    }
    
    /**
     * <p>
     * Returns the set of all data source names for data sources contributing to this block.
     * </p>
     * <p>
     * Each data source name should be unique.  After construction there should be only one
     * time series data set for each data source.  Time series data may be recovered by
     * name.
     * </p>
     *  
     * @return  set of all data sources (by name) contributing time-series data to this block
     */
    public final Set<String>  getDataSourceNames() {
        return this.setSourceNames;
    }
    
    /**
     * <p>
     * Returns the time-series data for the given data source (by name).
     * </p>
     * <p>
     * Returns all the time-series data for the given data source.  If the data source 
     * is not represented within this sampled data block a <code>null</code> value is returned.
     * </p>
     * 
     * @param strSourceName data source unique name
     * 
     * @return  all time-series data for the given source , or <code>null</code> if data source is not present 
     */
    public final SampledTimeSeries    getTimeSeries(String strSourceName) {
        return this.mapSrcToSmpls.get(strSourceName);
    }
    
    /**
     * <p>
     * Returns all sampled time-series data within this block as a map.
     * </p>
     * <p>
     * The returned collection represents all sampled time-series data within this 
     * sampling block.  The timestamps are determined by the sampling clock available
     * from method <code>{@link #getSamplingClock()</code>.
     * </p>
     * <p>
     * The returned map is keyed by data source name.  Specifically, the map is composed
     * of a collection of {(data source name, time-series data)} pairs where each data
     * source name key is unique.
     * </p>
     * 
     * @return  collection of all time-series data for all data sources within this block
     */
    public final Map<String, SampledTimeSeries> getTimeSeriesAll() {
        return this.mapSrcToSmpls;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates and returns a vector of timestamp instants for the sampling block.
     * </p>
     * <p>
     * An ordered vector of time instants is created and returned.  The timestamp vector
     * applies to all sampled time-series data sets contained within this block.
     * </p>
     * <p>
     * This is a convenience method which defers to the sampling clock within the block
     * to create the return timestamp vector.
     * </p>
     * 
     * @return  ordered vector of timestamp instants corresponding to all data within this block
     * 
     * @see UniformClockDuration#createTimestamps()
     */
    public Vector<Instant>  createTimestamps() {
        return this.clkParams.createTimestamps();
    }
    
    
    //
    // Comparable<UniformSamplingBlock> Interface
    
    /**
     * <p>
     * Compares the starting time instant of this sampling block to that of the argument.
     * </p>
     * <p>
     * Used for ordering sets and maps of <code>{@link UniformSamplingBlock}</code> objects.
     * </p>
     * <p>
     * The returned value is determined as follows:
     * <ul>
     * <li>Integer < 0 - If the start time of this block occurs before that of the argument.</li>
     * <li>Zero - If the start time instants are equal.</li>
     * <li>Integer > 0 - If the start time of this block occurs after that of the argument.</li>
     * </ul>
     * <p>
     * <p>
     * <h2>NOTE:</h2>
     * This does not compare for equivalence.  The compared instance may have overlapping
     * sampling domains, which should be checked (eventually).
     * </p>
     * 
     * @param o     the subject of comparison
     * 
     * @return  negative value if this block starts before, 0 if same, positive value if this block starts after
     * 
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(UniformSamplingBlock o) {
        Instant t1 = this.getStartInstant();
        Instant t2 = o.getStartInstant();
        
        return t1.compareTo(t2);
    }
    

    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates the map of data source name to sampled time series for the data source.
     * </p>
     * <p>
     * Creates all the <code>{@link SampledTimeSeries}</code> instances for this sampling
     * block using the given argument as source data.  The returned object is a map of
     * {(data source name, data source time series)} pairs.  It is assumed that the data
     * sources within the argument are all unique, otherwise an exception is thrown.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The argument should have already been checked for duplicate data source names using
     * <code>{@link CorrelatedQueryData#verifySourceUniqueness()}</code>.
     * </p>
     * 
     * @param lstMsgDataCols   source data for sampled time series creation
     * 
     * @return  map containing a time series for each data column in the argument, keyed by source name
     * 
     * @throws IllegalArgumentException      a data column message contained no data
     * @throws IllegalStateException         the list contained duplicate data source names
     * @throws UnsupportedOperationException an unsupported data type was detected within the argument
     */
    private Map<String, SampledTimeSeries> createTimeSeries(List<DataColumn> lstMsgDataCols) 
            throws IllegalArgumentException, IllegalStateException, UnsupportedOperationException {

        // Time series are created, one for each unique data source name
        Map<String, SampledTimeSeries>  mapSrcToCols;

        // Create processing stream based upon number of data columns
        if (lstMsgDataCols.size() > SZ_MSGCOLS_PIVOT) {
            mapSrcToCols = lstMsgDataCols.parallelStream()
                    .collect(
                            Collectors.toConcurrentMap(     // throws IllegalStateException for duplicate keys
                                    DataColumn::getName, 
                                    SampledTimeSeries::from // throws IllegalArgumentExcepiont, UnsupportedOperationException
                                    )
                            );
            
        } else {
            mapSrcToCols = lstMsgDataCols.stream()
                    .collect(
                            Collectors.toMap(               // throws IllegalStateException for duplicate keys
                                    DataColumn::getName, 
                                    SampledTimeSeries::from // throws IllegalArgumentExcepiont, UnsupportedOperationException
                                    )
                            );
        }

        // Return the new map of time series data
        return mapSrcToCols;
    }
    
}
