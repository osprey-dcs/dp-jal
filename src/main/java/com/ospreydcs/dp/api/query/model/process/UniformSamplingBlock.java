/*
 * Project: dp-api-common
 * File:	UniformSamplingBlock.java
 * Package: com.ospreydcs.dp.api.query.model.process
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
package com.ospreydcs.dp.api.query.model.process;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.ResultRecord;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.model.DpSupportedType;
import com.ospreydcs.dp.api.model.IDataColumn;
import com.ospreydcs.dp.api.model.IDataTable;
import com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.SamplingClock;

/**
 * <p>
 * Represents a block of finite-duration, correlated, sampled time-series data.
 * </p>
 * <p>
 * Class instances maintain an collection of <code>{@link SampledTimeSeries}</code> objects 
 * containing the time-series data for the sampling block.
 * The class also contains a uniform sampling clock <code>{@link UniformSamplingClock}</code>
 * of finite duration producing the timestamps for all time-series data sets within the block. 
 * Instances are intended to be created from <code>{@link CorrelatedQueryData}</code> objects
 * during Data Platform Query Service results set reconstruction.
 * </p>
 * <p>
 * Instances of <code>UniformSamplingBlock</code> can be ordered according to the start time
 * of the sampling interval (i.e., the first timestamp) through implementation of the
 * <code>{@link Comparable}</code> interface.  The class also contains an implementation of
 * <code>{@link Comparator}</code> interface for explicit comparison of <code>UniformSamplingBlock</code>
 * instances.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 30, 2024
 *
 */
public class UniformSamplingBlock implements Comparable<UniformSamplingBlock>, IDataTable {

    
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
    
    /** Logging active flag */
    public static final boolean     BOL_LOGGING = CFG_QUERY.logging.active;
    
    
    /** Concurrency active flag */
    public static final boolean     BOL_CONCURRENCY = CFG_QUERY.concurrency.active;
    
    /** Concurrency tuning parameter - pivot to parallel processing when lstMsgDataCols size hits this limit */
    public static final int         SZ_CONCURRENCY_PIVOT = CFG_QUERY.concurrency.pivotSize;
    

    //
    // Class Resources
    //
    
    /** Event logger for class */
    private static final Logger LOGGER = LogManager.getLogger();
    

    //
    // Instance Attributes
    //
    
    /** The uniform clock duration for this sample block */
    private final   UniformSamplingClock                    clkParams;
    
    /** The vector of ordered timestamps correspond to this sample block */
    private final   ArrayList<Instant>                      vecTimestamps;
    
    /** The vector of time-series data in this sampling block */
    private final   ArrayList<SampledTimeSeries<Object>>    vecSeries;
    
    
    /** Set of data source names for sample block - used for IDataTable implementation */
    private final   List<String>                            lstSourceNames;
    
    /** Map of data source name to table column index - used for IDataTable implementation */
    private final   Map<String, Integer>                    mapSrcToIndex;
    
    /** Map of data source name to time series - used for IDataTable implementation */
    private final   Map<String, SampledTimeSeries<Object>>  mapSrcToSeries;
    
    
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
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws TypeNotPresentException  an unsupported data type was detected within the argument
     */
    public static UniformSamplingBlock  from(CorrelatedQueryData cqdSampleBlock) 
            throws MissingResourceException, IllegalArgumentException, IllegalStateException, TypeNotPresentException 
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
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws TypeNotPresentException  an unsupported data type was detected within the argument
     */
    public UniformSamplingBlock(CorrelatedQueryData cqdSampleBlock) 
            throws MissingResourceException, IllegalArgumentException, IllegalStateException, TypeNotPresentException {
        
        // Check the argument for data source name uniqueness
        ResultRecord    rsltUnique = cqdSampleBlock.verifySourceUniqueness();
        if (rsltUnique.isFailure()) {
            if (BOL_LOGGING)
                LOGGER.error("{} correlated data block has non-unique sources: {}", JavaRuntime.getCallerName(), rsltUnique.message());

            throw new IllegalArgumentException("Correlated data block has non-unique sources: " + rsltUnique.message());
        }
        
        // Check the argument for data source size consistency
        ResultRecord    rsltSizes = cqdSampleBlock.verifySourceSizes();
        if (rsltSizes.isFailure()) {
            if (BOL_LOGGING)
                LOGGER.error("{} correlated data block has columns with bad sizes: {}", JavaRuntime.getCallerName(), rsltSizes.message());
            
            throw new IllegalArgumentException("Correlated data block has columns with bad sizes: " + rsltSizes.message());
        }
        
        // Extract the relevant Protobuf message from the argument
        SamplingClock       msgClockParams = cqdSampleBlock.getSamplingMessage();
        List<DataColumn>    lstMsgDataCols = cqdSampleBlock.getAllDataMessages();
        
        // Create the sampling clock and the timestamps for this block
        this.clkParams = UniformSamplingClock.from(msgClockParams);
        this.vecTimestamps = this.clkParams.createTimestamps();
        
        // Get the set of data source names and create all the time-series data for this block
        this.vecSeries = this.createTimeSeriesVector(lstMsgDataCols);
        
        this.lstSourceNames = this.createSourceNameList(this.vecSeries);
        this.mapSrcToIndex = this.createSrcToIndexMap(this.vecSeries);
        this.mapSrcToSeries = this.createSrcToSeriesMap(this.vecSeries);
    }

    
    //
    // Attribute and Property Query
    //
    
    /**
     * <p>
     * Returns the number samples within each time series of the sampling block.
     * </p>
     * <p>
     * Note that each time series within the sampling block must have the same size.  
     * This condition was checked during construction.
     * </p>
     * 
     * @return  the size of each time series within the sampling block
     */
    public final int getSampleCount() {
        return this.vecTimestamps.size();
    }
    
    /**
     * <p>
     * Returns the number of unique data sources within the sampling block.
     * </p>
     * <p>
     * The sampling block must containing one and only one time series from each data source
     * represented.  This condition was checked during construction.
     * To obtain the collection of unique data source names use
     * <code>{@link #getSourceNames()}</code>.
     * </p>
     * 
     * @return  the number of data sources contributing to this sampling block
     */
    public final int getDataSourceCount() {
        return this.lstSourceNames.size();
    }
    
    /**
     * <p>
     * Returns the starting time instant of the sampling interval for this sampling block
     * </p>
     * <p>
     * This is a convenience method where the returned value is taken from the 
     * uniform clock parameters.
     * To obtain the full time domain of the sampling block use method
     * <code>{@link #getTimeDomain()}</code>.
     * <p>
     * 
     * @return sampling interval start instant 
     */
    public final Instant getStartInstant() {
        return this.clkParams.getStartInstant();
    }
    
    /**
     * <p>
     * Returns the time domain over which samples were taken.
     * </p>
     * <p>
     * Returns the smallest connected interval [<i>t</i><sub>0</sub>, <i>t</i><sub><i>N</i>-1</sub>]
     * that contains all sample timestamps.
     * </p>
     * <p>
     * This is a convenience method where the returned value is taken from the 
     * uniform clock parameters.
     * <p>
     * <p>
     * The returned interval is given by 
     * <pre>
     *      [<i>t</i><sub>0</sub>, <i>t</i><sub><i>N</i>-1</sub>]
     * </pre>
     * where <i>t</i><sub>0</sub> is the first timestamp of the clock duration 
     * (given by <code>{@link #getStartInstant()}</code>) and <i>t</i><sub><i>N</i>-1</sub>
     * is the last timestamp of the clock duration.  Note that <i>N</i> is the number
     * of timestamps returned by <code>{@link #getSampleCount()}</code>.
     * <p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned interval DOES NOT include the sampling period duration
     * after the final timestamp.
     * </p>
     *  
     * @return  the time domain which the clock is active, minus the final period duration
     * 
     * @see UniformSamplingClock#getTimeDomain()
     */
    public final TimeInterval   getTimeDomain() {
        return this.clkParams.getTimeDomain();
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
     * @see UniformSamplingClock
     */
    public final UniformSamplingClock getSamplingClock() {
        return this.clkParams;
    }
    
    /**
     * <p>
     * Returns list of all data source names for data sources contributing to this block.
     * </p>
     * <p>
     * Each data source name should be unique.  After construction there should be only one
     * time series data set for each data source.  Time series data may be recovered by name.
     * </p>
     * <p>
     * <h2>NOTES:<h2>
     * Do not modify the returned collection, it is owned by this sampling block.
     * </p>
     *  
     * @return  set of all data sources (by name) contributing time-series data to this block
     */
    public final List<String>  getSourceNames() {
        return this.lstSourceNames;
    }
    
    /**
     * <p>
     * Returns the data type of the time series with the given name.
     * </p>
     * <p>
     * This is a convenience method that first invokes <code>{@link #getTimeSeries(String)}</code>
     * to obtain the time series then invokes <code>{@link SampledTimeSeries#getType()}</code> to
     * obtain the data type of the time series.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>If the given data source is not represented within the current collection of time series
     *    an exception is thrown.</li>
     * <li>Use <code>{@link #getSourceNames()}</code> to obtain all data sources names in block.</li>
     * </ul>
     * </p>
     * 
     * @param strSourceName data source unique name
     * 
     * @return  the data type of the given data source time series 
     * 
     * @throws NullPointerException the given data source was not contained in the current collection
     * 
     * @see #getTimeSeries(String)
     * @see SampledTimeSeries#getType()
     * @see DpSupportedType
     */
    public final DpSupportedType    getSourceType(String strSourceName) throws NullPointerException {
        return this.mapSrcToSeries.get(strSourceName).getType();
    }


    /**
     * <p>
     * Returns the time-series data for the given data source (by name).
     * </p>
     * <p>
     * Returns all the time-series data for the given data source.  If the data source 
     * is not represented within this sampled data block a <code>null</code> value is returned.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Do not modify the returned object, it is owned by this sampling block.</li>
     * <li>Use <code>{@link #getSourceNames()}</code> to obtain all data sources names in block.</li>
     * </ul>
     * </p>
     * 
     * @param strSourceName data source unique name
     * 
     * @return  all time-series data for the given source , or <code>null</code> if data source is not present 
     */
    public final SampledTimeSeries<Object>    getTimeSeries(String strSourceName) {
        return this.mapSrcToSeries.get(strSourceName);
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
     * <p>
     * <h2>NOTES:<h2>
     * Do not modify the returned collection, it is owned by this sampling block.
     * </p>
     * 
     * @return  collection of all time-series data for all data sources within this block
     */
    public final Map<String, SampledTimeSeries<Object>> getTimeSeriesAll() {
        return this.mapSrcToSeries;
    }
    
    
    //
    // State Queries
    //
    
    /**
     * <p>
     * Determine whether or not the sampling block contains time-series data for the given 
     * data source.
     * </p>
     * <p>
     * Simply checks the given data source name against the internal set of represented
     * data sources.
     * </p>
     *  
     * @param strSourceName unique name of data source
     * 
     * @return  <code>true</code> if the data source is represented within this sampling block,
     *          <code>false</code> otherwise
     */
    public boolean  hasSourceData(String strSourceName) {
        return this.lstSourceNames.contains(strSourceName);
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
     * @param usbCmp uniform sampling set under time domain comparison 
     * 
     * @return <code>true</code> if the given sampling time domain collides with this one,
     *         <code>false</code> otherwise
     *         
     * @see UniformSamplingClock#hasDomainIntersection(UniformSamplingClock)
     */
    public boolean  hasDomainIntersection(UniformSamplingBlock usbCmp) {
        return this.clkParams.hasDomainIntersection(usbCmp.clkParams);
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates and returns a new vector (<code>ArrayList</code>) of timestamp instants for the sampling block.
     * </p>
     * <p>
     * An ordered vector of time instants is created and returned.  The timestamp vector
     * applies to all sampled time-series data sets contained within this block.
     * </p>
     * <p>
     * This is a convenience method which defers to the sampling clock within the block
     * to create the return timestamp vector.
     * </p>
     * <p>
     * <h2>NOTES:<h2>
     * The returned vector has no ownership and can be modified.
     * </p>
     * 
     * @return  ordered vector of timestamp instants corresponding to all data within this block
     * 
     * @see UniformSamplingClock#createTimestamps()
     */
    public ArrayList<Instant>  createTimestamps() {
        return this.clkParams.createTimestamps();
    }

    /**
     * <p>
     * Inserts an empty time series of given name and type into the sampling block.
     * </p>
     * <p> 
     * An time series consisting of all <code>null</code> values is inserted into the current
     * collection of time series.  This action may be required for "filling in" missing data
     * sources when this <code>UniformSamplingBlock</code> is a component within a larger
     * aggregation containing data sources not represented during construction.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * If the current collection of time series already has a entry for the given data source
     * name, nothing is done and the method returns <code>false</code>.
     * </p>
     * 
     * @param strSourceName name of the "phantom" data source to be added to time series collection
     * @param enmType       data type of the "phantom" data source to be added
     * 
     * @return  <code>true</code> if the time series collection was modified,
     *          <code>false</code> if the time series collection already has a data source with given name
     */
    public boolean  insertEmptyTimeSeries(String strSourceName, DpSupportedType enmType) {
        
        // Check if data source name is already present
        if (this.hasSourceData(strSourceName))
            return false;
        
        // Create the null series
        SampledTimeSeries<Object>   stsEmpty = SampledTimeSeries.nullSeries(strSourceName, enmType, this.getSampleCount());
        
        // Add null series to collection and lookup maps
        Integer     indLast = this.vecSeries.size();
        
        this.vecSeries.add(stsEmpty);

        this.lstSourceNames.add(strSourceName);
        this.mapSrcToSeries.put(strSourceName, stsEmpty);
        this.mapSrcToIndex.put(strSourceName, indLast);
        
        return true;
    }
    
    
    //
    // Comparable<UniformSamplingBlock> Interface
    //
    
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
    // IDataTable Interface
    //
    
    /**
     * @return always returns <code>true</code> since table is populated at construction
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#isTableComplete()
     */
    @Override
    public boolean isTableComplete() {
        return true;
    }

    /**
     * @return always returns <code>false</code> since table was populated at construction (throws exception if error)
     * 
     * @see com.ospreydcs.dp.api.model.IDataTable#hasError()
     */
    @Override
    public boolean hasError() {
        return false;
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getRowCount()
     */
    @Override
    public Integer getRowCount() {
        return this.getSampleCount();
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getColumnCount()
     */
    @Override
    public Integer getColumnCount() {
        return this.getDataSourceCount();
    }

//    /**
//     *
//     * @see @see com.ospreydcs.dp.api.model.IDataTable#getColumnName(int)
//     */
//    @Override
//    public String getColumnName(int indCol) throws IndexOutOfBoundsException {
//        return this.vecColumns.get(indCol).getName();
//    }

    /**
     * 
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumnIndex(java.lang.String)
     */
    @Override
    public int getColumnIndex(String strName) throws NoSuchElementException {
        
        // Check argument
        if (!this.hasSourceData(strName))
            throw new NoSuchElementException(JavaRuntime.getCallerClassSimple() + " does NOT contain source " + strName);
            
        return this.mapSrcToIndex.get(strName);
//        OptionalInt index = IntStream.range(0, this.getDataSourceCount()).filter(i -> this.vecSeriess.get(i).getName().equals(strName)).findAny();
//        
//        if (index.isEmpty())
//            throw new NoSuchElementException("Time series not contained in UniformSamplingBlock: " + strName);
//        
//        return index.getAsInt();
    }


    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getColumnNames()
     */
    @Override
    public final List<String> getColumnNames() {
        return this.lstSourceNames;
    }

//    /**
//     *
//     * @see @see com.ospreydcs.dp.api.model.IDataTable#getTimestamp(int)
//     */
//    @Override
//    public Instant getTimestamp(int indRow) throws IndexOutOfBoundsException {
//        return this.vecTimestamps.get(indRow);
//    }

    /**
     * <p>
     * Returns the order vector (<code>ArrayList</code>) of timestamp instants corresponding to this sampling block.
     * </p>
     * <p>
     * The returned vector is ordered from earliest timestamp to latest timestamp.  All
     * timestamps are separated by the clock period.  
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Do not modify the returned vector, it is owned by this sampling block.
     * </p>
     * 
     * @return  ordered vector of timestamps for this sampling block, earliest to latest
     */
    @Override
    public final List<Instant>    getTimestamps() {
        return this.vecTimestamps;
    }
    
//    /**
//     *
//     * @see @see com.ospreydcs.dp.api.model.IDataTable#getValue(int, int)
//     */
//    @Override
//    public final Object getValue(int indRow, int indCol) throws IndexOutOfBoundsException, ArithmeticException {
//        return this.vecColumns.get(indCol).getValue(indRow);
//    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getColumn(int)
     */
    @Override
    public final /* <T extends Object> IDataColumn<T> */ IDataColumn<Object> getColumn(int indCol) throws IndexOutOfBoundsException {
        return this.vecSeries.get(indCol);
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getColumn(java.lang.String)
     */
    @Override
    public final /* <T> IDataColumn<T> */ IDataColumn<Object> getColumn(String strName) throws NoSuchElementException {

        // Check argument
        if (!this.hasSourceData(strName))
            throw new NoSuchElementException(JavaRuntime.getCallerClassSimple() + " does NOT contain source " + strName);
            
        return this.mapSrcToSeries.get(strName);
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
     * <h2>Concurrency</h2>
     * If concurrency is enabled (i.e., <code>{@link #BOL_CONCURRENCY}</code> = <code>true</code>),
     * this method utilizes streaming parallelism if the argument size is greater than the
     * pivot number <code>{@link #SZ_CONCURRENCY_PIVOT}</code>.
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
     * @throws MissingResourceException      a data column message contained no data
     * @throws IllegalStateException         the list contained duplicate data source names, or non-uniform data types
     * @throws UnsupportedOperationException an unsupported data type was detected within the argument
     * 
     * @deprecated now uses {@link #createTimeSeriesVector(List)} to create time-series, after implementing IDataTable
     */
    @SuppressWarnings("unused")
    @Deprecated(since="Feb 23, 2024", forRemoval=true)
    private Map<String, SampledTimeSeries<Object>> createTimeSeriesMap(List<DataColumn> lstMsgDataCols) 
            throws MissingResourceException, IllegalStateException, UnsupportedOperationException {

        // Time series are created, one for each unique data source name
        Map<String, SampledTimeSeries<Object>>  mapSrcToCols;

        // Create processing stream based upon number of data columns
        if (BOL_CONCURRENCY && (lstMsgDataCols.size() > SZ_CONCURRENCY_PIVOT)) {
            mapSrcToCols = lstMsgDataCols
                    .parallelStream()
                    .collect(
                            Collectors.toConcurrentMap(     // throws IllegalStateException for duplicate keys
                                    DataColumn::getName, 
                                    SampledTimeSeries::from // throws MissingResourceException, IllegalStateExcepiont, UnsupportedOperationException
                                    )
                            );
            
        } else {
            mapSrcToCols = lstMsgDataCols
                    .stream()
                    .collect(
                            Collectors.toMap(               // throws IllegalStateException for duplicate keys
                                    DataColumn::getName, 
                                    SampledTimeSeries::from // throws MissingResourceExcepiont, UnsupportedOperationException
                                    )
                            );
        }

        // Return the new map of time series data
        return mapSrcToCols;
    }
    
    /**
     * <p>
     * Creates all <code>{@link SampledTimeSeries}</code> objects and returns as a vector (i.e. array list).
     * </p>
     * <p>
     * Creates all the <code>{@link SampledTimeSeries}</code> instances for this sampling
     * block using the given argument as source data.  The returned object is a vector of
     * of such objects ordered according to the argument.  It is assumed that the data
     * sources within the argument are all unique. This condition should, however, be checked.
     * </p>
     * <p>
     * <p>
     * This method is intended for the creation of attribute <code>{@link #vecSeries}</code>, also needed for the
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
     * <code>{@link CorrelatedQueryData#verifySourceUniqueness()}</code>.
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
    private ArrayList<SampledTimeSeries<Object>> createTimeSeriesVector(List<DataColumn> lstMsgDataCols) 
            throws MissingResourceException, IllegalStateException, TypeNotPresentException {

        // List of time series are created, one for each unique data source name
        List<SampledTimeSeries<Object>>  lstCols; // = new ArrayList<>();

        // Create processing stream based upon number of data columns
        
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
//        // Create the vector of table column in the order that they appear in the map
//        ArrayList<SampledTimeSeries<Object>> vecCols = mapSrcToCols
//                .entrySet()
//                .stream()
//                .collect(
//                        ArrayList::new, 
//                        (list, entry) -> list.add(entry.getValue()), 
//                        (collector, list) -> collector.addAll(list)
//                        );
//        
//        return vecCols;
    }
    
    /**
     * <p>
     * Creates and returns a map of (source name, time series) pairs from the argument.
     * </p>
     * <p>
     * This method is used to create a lookup map for data column (i.e., time series) by column name
     * required by the <code>{@link IDataTable}</code> interface.  It is assumed that the argument
     * contains all time series within the sampling block.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The returned collection is mutable.
     * </li>
     * </ul>
     * </p>
     * 
     * @param lstSeries vector containing a time series for each data column in the sampling block
     * 
     * @return  map of (source name, time series) pairs used for time series lookup by name
     * 
     * @throws IllegalStateException    the argument contained duplicate data source names 
     */
    private Map<String, SampledTimeSeries<Object>>  createSrcToSeriesMap(List<SampledTimeSeries<Object>> lstSeries) 
            throws IllegalStateException {
        
        // Map of unique data source name to time series
        Map<String, SampledTimeSeries<Object>>  mapSrcToCols = lstSeries
                .stream()
                .collect(
                        Collectors.toMap(               // throws IllegalStateException for duplicate keys
                                s -> s.getName(), 
                                s -> s
                                )
                        );
        
        return mapSrcToCols;
    }
    
    /**
     * <p>
     * Creates and returns a map of (source name, column index) pairs from the argument.
     * </p>
     * <p>
     * This method is used to create a lookup map for table column index by column name
     * required by the <code>{@link IDataTable}</code> interface.  It is assumed that the argument
     * contains all time series within the sampling block.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The returned collection is mutable.
     * </li>
     * </ul>
     * </p>
     * 
     * @param lstSeries vector containing a time series for each data column in the sampling block
     * 
     * @return  map of (source name, table column index) pairs used for time series lookup by table column index
     */
    private Map<String, Integer> createSrcToIndexMap(List<SampledTimeSeries<Object>> lstSeries) {
        
        // Returned map
        Map<String, Integer>    mapSrcToIndex = new HashMap<>();
        
        // Populate map and return
        Integer     indCurr = 0;
        for (SampledTimeSeries<Object> stms : lstSeries) {
            String  strName = stms.getName();
            
            mapSrcToIndex.put(strName, indCurr);
            
            indCurr++;
        }
        
        return mapSrcToIndex;
    }
    
    /**
     * <p>
     * Creates and returns a map of (source name, column index) pairs from the argument.
     * </p>
     * <p>
     * This method is used to create a data column name list required by the <code>{@link IDataTable}</code> 
     * interface.  It is assumed that the argument contains all time series within the sampling block.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The returned collection is mutable.
     * </li>
     * </ul>
     * </p>
     * 
     * @param lstSeries vector containing a time series for each data column in the sampling block
     * 
     * @return ordered list of data source names within the argument
     */
    private List<String>    createSourceNameList(List<SampledTimeSeries<Object>> lstSeries) {
        
        // Extract the source names
        List<String>    lstNames = lstSeries
                .stream()
                .<String>map(SampledTimeSeries::getName)
                .toList();
        
        // Create a mutable vector and return
        ArrayList<String>   vecNames = new ArrayList<>(lstNames);
        
        return vecNames;
    }
}
