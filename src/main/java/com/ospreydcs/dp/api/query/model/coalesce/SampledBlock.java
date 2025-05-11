/*
 * Project: dp-api-common
 * File:	SampledBlock.java
 * Package: com.ospreydcs.dp.api.query.model.coalesce
 * Type: 	SampledBlock
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
 * @since Mar 19, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.coalesce;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.ospreydcs.dp.api.common.DpSupportedType;
import com.ospreydcs.dp.api.common.IDataColumn;
import com.ospreydcs.dp.api.common.IDataTable;
import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.common.UniformSamplingClock;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.query.model.correl.CorrelatedQueryDataOld;
import com.ospreydcs.dp.api.query.model.correl.RawClockedData;
import com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData;
import com.ospreydcs.dp.api.query.model.correl.RawTmsListData;
import com.ospreydcs.dp.api.query.model.superdom.RawSuperDomData;
import com.ospreydcs.dp.api.query.model.superdom.SampledBlockSuperDom;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Base class representation of a finite-duration block of correlated, sampled, time-series data.
 * </p>
 * <p>
 * Class instances formalize <code>RawCorrelatedData</code> objects to the Java API Library format.  The most
 * important property of a collection of <code>SampledBlock</code> instances is that they are intended to be
 * disjoint in time, whereas <code>RawCorrelatedData</code> instances recovered from a single query can have collisions.
 * Specifically, <code>RawCorrelatedData</code> instances contain Query Service time-series raw data within 
 * Protocol Buffers messages correlated to specific timestamps, which may overlap.  The objective of this class is to 
 * extract the raw data and present it in the format of the Java API Library and with disjoint time ranges.  
 * After construction, the data is available through the <code>{@link IDataTable}</code> interface and other
 * public methods specific to <code>SampledBlock</code> (typically used for further processing).
 * </p>
 * <p>
 * Class instances maintain an collection of <code>{@link SampledTimeSeries}</code> objects 
 * containing the time-series data for the sampled block.
 * The class also maintains all the timestamps (of finite duration) for all time-series data sets within the block. 
 * Instances are intended to be created from <code>{@link CorrelatedQueryDataOld}</code> objects
 * during Data Platform Query Service results set reconstruction.
 * </p>
 * <p>
 * Instances of <code>SampledBlock</code> can be ordered according to the start time
 * of the sampling interval (i.e., the first timestamp) through implementation of the
 * <code>{@link Comparable}</code> interface.  The class also contains an implementation of
 * <code>{@link Comparator}</code> interface for explicit comparison of start time
 * instances.
 * </p>
 * <p>
 * <h2>Subclasses</h2>
 * Subclasses implement the specific requirements for subclasses of <code>RawCorrelatedData</code>.  In addition,
 * raw, correlated super domains (i.e., raw, correlated data with time-domain collisions) is now also implemented with
 * a new subclass.  The following are subclasses currently recognized and available for creation with the creator
 * methods <code>from()</code>:
 * <ul>
 * <li><code>SampledBlockClocked</code> - supports raw data correlated against a uniform sampling clock.</li>
 * <li><code>SampledBlockTmsList</code> - supports raw data correlated against explicit timestamp list.</li>
 * <li><code>SampledBlockSuperDom</code> - supports raw correlated data containing time-domain collisions.</li>
 * </ul>
 * <p> 
 * These subclass instances should be fully initialized once constructed.  The <code>SampledBlock</code> base class provides
 * the method <code>{@link #initialize()}</code> which must be called before the public methods are accessed.  Thus,
 * calling this method within the subclass constructor is recommended.  Although, considered not "good programming
 * practice", the action is warranted here.
 * </p>
 * <p>
 * All subclasses must implement the abstract methods <code>{@link #createTimestampsVector()}</code> and
 * <code>{@link #createTimeSeriesVector()}</code>, which are called within the <code>{@link #initialize()}</code>
 * method to assign attributes <code>{@link #vecTimestamps}</code> and <code>{@link #vecTimeSeries}</code>,
 * respectively.  Thus, all subclass resources necessary for proper invocation of the two abstract methods
 * must be available before invoking <code>{@link #initialize()}</code>.  See the method documentation for
 * <code>{@link #createTimestampsVector()}</code> and <code>{@link #createTimeSeriesVector()}</code> for
 * their requirements.
 * </p> 
 * <p>
 * <h2>NOTES:</h2>
 * Currently comparisons of <code>SampledBlock</code> instances with either the exposed <code>Comparable</code>
 * interface or the contained class <code>StartTimeComparator</code> implementing the <code>Comparator</code>
 * interface are <b>not strict</b>.  Both methods provide comparisons against the start time of the
 * <code>SampledBlock</code> instance but equality is never explicitly indicated (i.e., a zero-value indicating
 * equality).  This action prevents the clobbering of <code>SampledBlock</code> instances within sorted
 * collections.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 19, 2025
 */
public abstract class SampledBlock implements IDataTable, Comparable<SampledBlock> {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new <code>SampledBlock</code> instance initialized from the generalized argument data.
     * </p>
     * <p>
     * The returned object is complete and contains all data within the argument.  Specifically,
     * all argument data is extracted from its raw, Protocol Buffers messages and used to populate
     * the <code>SampledBlock</code> instance.  
     * The data is then immediately available from the <code>SampledBlock</code> interface.
     * </p> 
     * <p>
     * The type of the returned value is determined by the subtype of the argument.  Specifically,
     * <ul>
     * <li><code>SampledBlockClocked</code> when argument is instance of <code>RawClockedData</code>.</li>
     * <li><code>SampledBlockTmsList</code> when argument is instance of <code>RawTmsListData</code>.</li>
     * </ul>
     * </p>
     * 
     * @param datRaw    the correlated raw, time-series data
     * 
     * @return  a new <code>SampledBlock</code> instance ready for data access
     * 
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws UnsupportedOperationException   the raw data contained neither a sampling clock or a timestamp list
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws TypeNotPresentException  an unsupported time-series data type was detected within the argument
     */
    public static SampledBlock  from(RawCorrelatedData datRaw) 
        throws UnsupportedOperationException, MissingResourceException, IllegalArgumentException, IllegalStateException, TypeNotPresentException 
    {

        // Check for correlation against an uniform sampling clock
        if (datRaw instanceof RawClockedData dat) {
            return new SampledBlockClocked(dat);
        }
        
        // Check for correlation against a timestamp list
        if (datRaw instanceof RawTmsListData dat) {
            return new SampledBlockTmsList(dat);
        }
        
        // If we are here then we have an unsupported situation
        String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                + "- Unsupported correlated raw data type: "
                + datRaw.getClass().getName();
        
        if (BOL_LOGGING) 
            LOGGER.error(strMsg);
            
        throw new UnsupportedOperationException(strMsg);
    }
    
    /**
     * <p>
     * Creates a new <code>SampledBlockClocked</code> instance initialized from the generalized argument data.
     * </p>
     * <p>
     * The returned object is complete and contains all data within the argument.  Specifically,
     * all argument data is extracted from its raw, Protocol Buffers messages and used to populate
     * the <code>SampledBlock</code> instance.  
     * The data is then immediately available from the <code>SampledBlock</code> interface.
     * </p>
     *  
     * @param datRaw    the correlated raw, time-series data
     * 
     * @return  a new <code>SampledBlockClocked</code> instance ready for data access
     * 
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws UnsupportedOperationException   the raw data contained neither a sampling clock or a timestamp list
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws TypeNotPresentException  an unsupported time-series data type was detected within the argument
     */
    public static SampledBlockClocked  from(RawClockedData datRaw)
        throws IllegalArgumentException, MissingResourceException, IllegalStateException, TypeNotPresentException 
    {
        return new SampledBlockClocked(datRaw);
    }
    
    /**
     * <p>
     * Creates a new <code>SampledBlockTmsList</code> instance initialized from the generalized argument data.
     * </p>
     * <p>
     * The returned object is complete and contains all data within the argument.  Specifically,
     * all argument data is extracted from its raw, Protocol Buffers messages and used to populate
     * the <code>SampledBlock</code> instance.  
     * The data is then immediately available from the <code>SampledBlock</code> interface.
     * </p>
     *  
     * @param datRaw    the correlated raw, time-series data
     * 
     * @return  a new <code>SampledBlockTmsList</code> instance ready for data access
     * 
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws UnsupportedOperationException   the raw data contained neither a sampling clock or a timestamp list
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws TypeNotPresentException  an unsupported time-series data type was detected within the argument
     */
    public static SampledBlockTmsList   from(RawTmsListData datRaw) 
            throws IllegalArgumentException, MissingResourceException, IllegalStateException, TypeNotPresentException 
    {
        return new SampledBlockTmsList(datRaw);
    }
    
    /**
     * <p>
     * Creates a new <code>SampledBlockSuperDom</code> instance initialized from the argument data.
     * </p>
     * <p>
     * The returned object is complete and fully initiallized, containing all data within the argument.  
     * Specifically, all argument data is extracted from its raw, Protocol Buffers messages and used to populate
     * the <code>SampledBlock</code> instance.  
     * The data is then immediately available from the <code>SampledBlock</code> interface.
     * </p> 
     * 
     * @param datRaw    the correlated raw, time-series data containing time-domain collisions
     * 
     * @return  a new <code>SampledBlockSuperDom</code> instance ready for data access
     * 
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws TypeNotPresentException  an unsupported data type was detected within the argument
     * @throws ExecutionException       general exception for serial processing (see cause)
     * @throws RejectedExecutionException a fill task failed execution for concurrent processing
     * @throws InterruptedException     timeout occurred while waiting for concurrent fill tasks to complete
     */
    public static SampledBlockSuperDom  from(RawSuperDomData datRaw) 
            throws IllegalArgumentException, MissingResourceException, 
                    IllegalStateException, TypeNotPresentException, 
                    RejectedExecutionException, ExecutionException, InterruptedException 
    {
        return new SampledBlockSuperDom(datRaw);
    }
    
    
    //
    // Internal Types
    //
    
    /**
     * <p>
     * <code>{@link Comparator}</code> class for ordered comparison of two <code>SampledBlock</code> instances.
     * </p>
     * <p>
     * The comparison here is performed with the start times of the sampling intervals represented by 
     * start time instant returned from the 
     * <code>{@link SampledBlock#getStartTime()</code> method.
     * Instances are intended for use in Java container construction.
     * </p>
     *
     * @author Christopher K. Allen
     * @since Mar 19, 2025
     */
    public static class StartTimeComparator implements Comparator<SampledBlock> {

        //
        // Creator
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
         * <p>
         * Used for ordering within collections of <code>SampledBlock</code> objects.  Returns
         * -1 if the start time of the first argument is before the start time of the second argument object
         * as dictated by </code>{@link Instant#isBefore(Instant)}</code>.  Otherwise the returned value is 1. 
         * </p>
         * <p>
         * Let <i>t</i><sub>1</sub> be the start time instant of the first argument <b>D</b><sub>1</sub> and 
         * <i>t</i><sub>2</sub> be the start time instant of the second argument argument <b>D</b><sub>2</sub>.
         * Let <b>R</b> be the returned value of this method.  
         * Then, ordering is as follows:
         * <pre>
         *   <i>t</i><sub>1</sub> < <i>t</i><sub>2</sub> &rArr; <b>R</b> = -1 &rArr; { ..., <b>D</b><sub>1</sub>, <b>D</b><sub>2</sub>, ... }  
         *   <i>t</i><sub>1</sub> &ge; <i>t</i><sub>2</sub> &rArr; <b>R</b> = +1 &rArr; { ..., <b>D</b><sub>2</sub>, <b>D</b><sub>1</sub>, ... }  
         * </pre>
         * </p>
         * <p>
         * <h2>NOTES:</h2>
         * <ul>
         * <li>This does identify equality - a returned value of 0 would result in a clobbered object.</li>
         * <li>The compared instances may have overlapping sampling ranges, which should be checked (eventually).</li>
         * </ul>
         * </p>
         * 
         * @see @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(SampledBlock o1, SampledBlock o2) {
            Instant t1 = o1.getStartTime();
            Instant t2 = o2.getStartTime();

            if (t1.isBefore(t2))
                return -1;
            else
                return +1;
        }
    }
    

    //
    // Application Resources
    //
    
    /** The Data Platform API default configuration parameter set */
    protected static final DpQueryConfig  CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** Logging enabled flag */
    public static final boolean     BOL_LOGGING = CFG_QUERY.logging.enabled;
    
    /** Logging event level */
    public static final String      STR_LOGGING_LEVEL = CFG_QUERY.logging.level;
    
    
    /** Concurrency enabled flag */
    public static final boolean     BOL_CONCURRENCY = CFG_QUERY.concurrency.enabled;
    
    /** Concurrency tuning parameter - pivot to parallel processing when lstMsgDataCols size hits this limit */
    public static final int         SZ_CONCURRENCY_PIVOT = CFG_QUERY.concurrency.pivotSize;
    
    
    /** Use extended error checking during initialization */
    public static final boolean     BOL_ERROR_CHK = CFG_QUERY.data.table.construction.errorChecking;
    
    
    //
    // Class Resources
    //
    
    /** Event logger for class */
    protected static final Logger     LOGGER = LogManager.getLogger();
    

    /**
     * <p>
     * Class Initialization - Initializes the event logger, sets logging level.
     * </p>
     */
    static {
        Configurator.setLevel(LOGGER, Level.toLevel(STR_LOGGING_LEVEL, LOGGER.getLevel()));
    }
    
    
    //
    // Instance Attributes
    //
    
    /** The (optional) original time-series data request identifier */
    protected String            strRqstId = null;
    
    /** The time range of the sampled block */
    protected TimeInterval      tvlRange;
    
    
    /** The vector of ordered timestamps correspond to this sample block */
    protected ArrayList<Instant>                      vecTimestamps;
    
    /** The vector of time-series data in this sampling block */
    protected ArrayList<SampledTimeSeries<Object>>    vecTimeSeries;
    
    
    /** Set of data source names for sample block - used for IDataTable implementation */
    protected List<String>                            lstSourceNames;
    
    /** Map of data source name to table column index - used for IDataTable implementation */
    protected Map<String, Integer>                    mapSrcToIndex;
    
    /** Map of data source name to time series - used for IDataTable implementation */
    protected Map<String, SampledTimeSeries<Object>>  mapSrcToSeries;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new uninitialized, instance of <code>SampledBlock</code>.
     * </p>
     * <p>
     * Child classes should initialize themselves then call the <code>{@link #initialize()}</code>
     * method to initialize this base class.
     * </p>
     */
    public SampledBlock() { 
        
//        // Check the argument for data source name uniqueness
//        ResultStatus    recUnique = datRaw.verifySourceUniqueness();
//        if (recUnique.isFailure()) {
//            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
//                    + " - Correlated data block has non-unique sources: " 
//                    + recUnique.message();
//            
//            if (BOL_LOGGING)
//                LOGGER.error(strMsg);
//
//            throw new IllegalArgumentException(strMsg);
//        }
//        
//        // Check the argument for data source size consistency
//        ResultStatus    recSizes = datRaw.verifySourceSizes();
//        if (recSizes.isFailure()) {
//            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
//                    + " - Correlated data block has columns with bad sizes: " 
//                    + recSizes.message();
//            
//            if (BOL_LOGGING)
//                LOGGER.error(strMsg);
//            
//            throw new IllegalArgumentException(strMsg);
//        }
//
//        // Extract the time domain data
//        this.tvlRange = datRaw.getTimeRange();
//        this.vecTimestamps = this.createTimestampsVector();
//        
//        // Extract the relevant Protobuf message from the argument
//        List<DataColumn>    lstMsgDataCols = datRaw.getRawDataMessages();
//        
//        // Create the time-series data all supporting resources for this block
//        this.vecTimeSeries = this.createTimeSeriesVector(lstMsgDataCols);
//        
//        this.lstSourceNames = this.createSourceNameList(this.vecTimeSeries);
//        this.mapSrcToIndex = this.createSrcToIndexMap(this.vecTimeSeries);
//        this.mapSrcToSeries = this.createSrcToSeriesMap(this.vecTimeSeries);
    }
    
    /**
     * <p>
     * Initializes the base class primary and auxiliary data structures.
     * </p>
     * <p>
     * This method must be called from the child class to initialize the base class data structures.  
     * Once initialized, these data structures support all public method invocations, including those
     * of the <code>IDataTable</code> interface.
     * </p>
     * <p>
     * It is logical that this method be called from the child class constructor after it has initialized
     * all its resources according to its supported data.  In general this is not best programming practice,
     * however, here it makes sense to have the entire sampled block fully populated and ready for client
     * access after construction.
     * </p>
     *  
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws TypeNotPresentException  an unsupported data type was detected within the argument
     */
    protected void initialize() throws MissingResourceException, IllegalStateException, TypeNotPresentException {
        
        // Create the defining data for the sampled block
        this.vecTimestamps = this.createTimestampsVector();
        this.vecTimeSeries = this.createTimeSeriesVector(); // throws exceptions
        
        // Create the time range from the timestamp list
        Instant insStart = this.vecTimestamps.getFirst();
        Instant insStop = this.vecTimestamps.getLast();
        this.tvlRange = TimeInterval.from(insStart, insStop);
        
        // Create the auxiliary data from the times series
        this.lstSourceNames = this.createSourceNameList(this.vecTimeSeries);
        this.mapSrcToIndex = this.createSrcToIndexMap(this.vecTimeSeries);
        this.mapSrcToSeries = this.createSrcToSeriesMap(this.vecTimeSeries);
    }

    
    //
    // Abstract Support Methods
    //
    
    /**
     * <p>
     * Subclass construction of the timestamp vector for all data within this sampled block.
     * </p>
     * <p>
     * The timestamps are created and saved in the base class attribute <code>{@link #vecTimestamps}</code>.
     * This method should be called by the base class only once.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * It is unclear if this implementation works as this method is invoked from the base class constructor.
     * If Java completes the base class constructor before the subclass constructor is invoked then the
     * local attribute containing the actual (typed) <code>RawCorrelatedData</code> object may not be 
     * available.  This is certainly the case for C++ which creates a compilation error.
     * </p>
     *  
     * @return  the ordered list (vector) of timestamps for all data within this sampled block
     */
    protected abstract ArrayList<Instant> createTimestampsVector();

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
     * @return  vector containing a time series for each data in the constructor argument, ordered by argument
     * 
     * @throws MissingResourceException a data column message contained no data
     * @throws IllegalStateException    the argument contained non-uniform data types
     * @throws TypeNotPresentException  an unsupported data type was detected within the argument
     */
    protected abstract ArrayList<SampledTimeSeries<Object>> createTimeSeriesVector() 
            throws MissingResourceException, IllegalStateException, TypeNotPresentException;
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Sets the optional request identifier of the original time-series data request.
     * </p>
     * 
     * @param strRqstId
     */
    public void setRequestId(String strRqstId) {
        this.strRqstId = strRqstId;
    }
    
    /**
     * <p>
     * Inserts an empty time series of given name and type into the sampled block.
     * </p>
     * <p> 
     * A time series consisting of all <code>null</code> values is inserted into the current
     * collection of time series.  This action may be required for "filling in" missing data
     * sources when this <code>SampledBlock</code> is a component within a larger
     * aggregation containing data sources not represented during construction.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * If the current collection of time series already has a entry for the given data source
     * name, nothing is done and the method returns <code>false</code>.
     * </li>
     * <li>
     * The new (phantom) time series will have the last index within the <code>IDataTable</code>.
     * </li>
     * </p>
     * 
     * @param strSourceName name of the "phantom" data source to be added to time series collection
     * @param enmType       data type of the "phantom" data source to be added
     * 
     * @return  <code>true</code> if the time series collection was modified,
     *          <code>false</code> if the time series collection already has a data source with given name
     */
    public boolean  insertNullTimeSeries(String strSourceName, DpSupportedType enmType) {
        
        // Check if data source name is already present
        if (this.hasSourceData(strSourceName))
            return false;
        
        // Create the null series
        SampledTimeSeries<Object>   stsEmpty = SampledTimeSeries.nullSeries(strSourceName, enmType, this.getSampleCount());
        
        // Add null series to collection and lookup maps
        Integer     indLast = this.vecTimeSeries.size();
        
        this.vecTimeSeries.add(stsEmpty);

        this.lstSourceNames.add(strSourceName);
        this.mapSrcToSeries.put(strSourceName, stsEmpty);
        this.mapSrcToIndex.put(strSourceName, indLast);
        
        return true;
    }
    
    
    //
    // Attribute Query
    //
    
    /**
     * <p>
     * Returns the starting time instant of the sampling interval for this sampled block.
     * </p>
     * <p>
     * This is a convenience method where the returned value is taken from the 
     * sampled block timestamps (i.e., the first one) which is subsequently held in 
     * attribute <code>{@link #tvlRange}</code> available from <code>{@link #getTimeRange()}</code>.
     * Thus, to obtain the full time domain of the sampling block use method
     * <code>{@link #getTimeRange()}</code>.
     * </p>
     * 
     * @return sampling interval start time instant (equivalent to <code>{@link #getTimeRange()}.begin()</code>)
     */
    public final Instant getStartTime() {
        return this.tvlRange.begin();
    }
    
    /**
     * <p>
     * Returns the final time instant of the sampling interval for this sampled block.
     * </p>
     * <p>
     * This is a convenience method where the returned value is taken from the 
     * sampled block timestamps (i.e., the last one) which is subsequently held in 
     * attribute <code>{@link #tvlRange}</code> available from <code>{@link #getTimeRange()}</code>.
     * Thus, to obtain the full time domain of the sampling block use method
     * <code>{@link #getTimeRange()}</code>.
     * </p>
     * 
     * @return  sampling interval final time instant (equivalent to <code>{@link #getTimeRange()}.end()</code>)
     */
    public final Instant getFinalTime() {
        return this.tvlRange.end();
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
     * (given by <code>{@link #getStartTime()}</code>) and <i>t</i><sub><i>N</i>-1</sub>
     * is the last timestamp of the clock duration.  Note that <i>N</i> is the number
     * of timestamps returned by <code>{@link #getSampleCount()}</code>.
     * <p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned interval DOES NOT include the sampling period duration
     * after the final timestamp.
     * </p>
     *  
     * @return  the time domain which the clock is enabled, minus the final period duration
     * 
     * @see UniformSamplingClock#getTimeDomain()
     */
    public final TimeInterval   getTimeRange() {
        return this.tvlRange;
    }
    
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
     * Determine whether or not the sampled block contains time-series data for the given 
     * data source.
     * </p>
     * <p>
     * Simply checks the given data source name for containment within the internal set of represented
     * data sources.
     * </p>
     *  
     * @param strSourceName unique name of data source
     * 
     * @return  <code>true</code> if the data source is represented within this sampling block,
     *          <code>false</code> otherwise
     */
    public final boolean  hasSourceData(String strSourceName) {
        return this.lstSourceNames.contains(strSourceName);
    }
    
    /**
     * <p>
     * Determines whether of not the sampled block contains process samples at the given timestamp.
     * </p>
     * <p>
     * Simply checks the given argument for containment within internal collection of sampled process timestamps.
     * Equality is determined but the <code>{@link Instant#equals(Object)}</code> method.
     * The timestamp index can be recovered with method <code>{@link #timestampIndex(Instant)}</code>.
     * </p>
     *  
     * @param insTms    timestamp under scrutiny
     * 
     * @return  <code>true</code> if the sampled block contains samples for the given timestamp,
     *          <code>false</code> otherwise
     *          
     * @see #timestampIndex(Instant)
     */
    public final boolean    hasTimestamp(Instant insTms) {
        return this.vecTimestamps.contains(insTms);
    }
    
    /**
     * <p>
     * Returns the row index of the given timestamp within the sampled block if it exists.
     * </p>
     * <p>
     * The returned value is the index <i>i</i> that would yield the argument value for an invocation
     * of method <code>{@link #getTimestamp(int)}</code>.
     * Note that if the sampled block does not contain samples for the given timestamp an exception is thrown.
     * Use <code>{@link #hasTimestamp(Instant)}</code> to determine timestamp validity.
     * </p>
     *  
     * @param insTms    timestamp for which index is to be retrieved
     * 
     * @return  the sampled process timestamp index for the given timestamp
     * 
     * @throws IllegalArgumentException there is no process samples for the given timestamp
     * 
     * @see {@link #hasTimestamp(Instant)}
     */
    public final int    timestampIndex(Instant insTms) throws IllegalArgumentException {
        int indTms = this.vecTimestamps.indexOf(insTms);
        
        if (indTms == -1) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - No process samples for timestamp " + insTms;
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
        
        return indTms;
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
     * sampling range time interval of this block and the argument.
     * <p>
     * 
     * @param blkCmp sampled block under time range comparison 
     * 
     * @return <code>true</code> if the given sampling time domain collides with this one,
     *         <code>false</code> otherwise
     *         
     * @see UniformSamplingClock#hasDomainIntersection(UniformSamplingClock)
     */
    public final boolean  hasDomainIntersection(SampledBlock blkCmp) {
        return this.tvlRange.hasIntersectionClosed(blkCmp.tvlRange);
    }
    
    
    //
    // IDataTable Interface
    //
    
    /**
     * @see com.ospreydcs.dp.api.common.IDataTable#getRequestId()
     */
    @Override
    public String getRequestId() {
        return this.strRqstId;
    }

    /**
     * @return always returns <code>true</code> since table is populated at construction
     *
     * @see com.ospreydcs.dp.api.common.IDataTable#isTableComplete()
     */
    @Override
    public boolean isTableComplete() {
        return true;
    }

    /**
     * @return always returns <code>false</code> since table was populated at construction (throws exception if error)
     * 
     * @see com.ospreydcs.dp.api.common.IDataTable#hasError()
     */
    @Override
    public boolean hasError() {
        return false;
    }
    
    /**
     * @see com.ospreydcs.dp.api.common.IDataTable#clear()
     */
    @Override
    public void clear() {
        this.vecTimeSeries.forEach(SampledTimeSeries::clear);
        
        this.vecTimestamps.clear();
        this.vecTimeSeries.clear();
        this.lstSourceNames.clear();
        this.mapSrcToIndex.clear();
        this.mapSrcToSeries.clear();
    }

    /**
     * @see com.ospreydcs.dp.api.model.IDataTable#getRowCount()
     */
    @Override
    public Integer getRowCount() {
        return this.getSampleCount();
    }

    /**
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumnCount()
     */
    @Override
    public Integer getColumnCount() {
        return this.getDataSourceCount();
    }

    /**
     * @see com.ospreydcs.dp.api.common.IDataTable#getColumnIndex(java.lang.String)
     */
    @Override
    public int getColumnIndex(String strName) throws NoSuchElementException {
        
        // Check argument
        if (!this.hasSourceData(strName)) {
            String strMsg = JavaRuntime.getMethodClassSimple() + " does NOT contain source " + strName;
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new NoSuchElementException(strMsg);
        }
            
        return this.mapSrcToIndex.get(strName);
    }


    /**
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumnNames()
     */
    @Override
    public final List<String> getColumnNames() {
        return this.lstSourceNames;
    }

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
     * 
     * @see com.ospreydcs.dp.api.common.IDataTable#getTimestamps()
     */
    @Override
    public final List<Instant>    getTimestamps() {
        return this.vecTimestamps;
    }
    
    /**
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumn(int)
     */
    @Override
    public final IDataColumn<Object> getColumn(int indCol) throws IndexOutOfBoundsException {
        return this.vecTimeSeries.get(indCol);
    }

    /**
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumn(java.lang.String)
     */
    @Override
    public final IDataColumn<Object> getColumn(String strName) throws NoSuchElementException {

        // Check argument
        if (!this.hasSourceData(strName)) {
            String strMsg = JavaRuntime.getMethodClassSimple() + " does NOT contain source " + strName;
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new NoSuchElementException(strMsg);
        }
            
        return this.mapSrcToSeries.get(strName);
    }
    

    
    //
    // Comparable<SampledBlock> Interface
    //
    
    /**
     * <p>
     * Compares the start time of the given <code>SampledBlock</code> with this one.
     * </p>
     * <p>
     * Used for ordering within sets and maps of <code>SampledBlock</code> objects.  Returns
     * -1 if the start time of this object is before the start time of the argument object
     * as dictated by </code>{@link Instant#isBefore(Instant)}</code>.  Otherwise the returned value is 1. 
     * </p>
     * <p>
     * Let <i>t</i><sub>1</sub> be the start time instant of this object <b>D</b><sub>1</sub> and 
     * <i>t</i><sub>2</sub> be the start time instant of the argument object <b>D</b><sub>2</sub>.
     * Let <b>R</b> be the returned value of this method.  
     * Then, ordering is as follows:
     * <pre>
     *   <i>t</i><sub>1</sub> < <i>t</i><sub>2</sub> &rArr; <b>R</b> = -1 &rArr; { ..., <b>D</b><sub>1</sub>, <b>D</b><sub>2</sub>, ... }  
     *   <i>t</i><sub>1</sub> &ge; <i>t</i><sub>2</sub> &rArr; <b>R</b> = +1 &rArr; { ..., <b>D</b><sub>2</sub>, <b>D</b><sub>1</sub>, ... }  
     * </pre>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This does identify equality - a returned value of 0 would result in a clobbered object.</li>
     * <li>The compared instance may have overlapping sampling ranges, which should be checked (eventually).</li>
     * </ul>
     * </p>
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(SampledBlock o) {
        if (this.getStartTime().isBefore(o.getStartTime()))
            return -1;
        else
            return +1;
    }
    
    
//    /** 
//     * <p>
//     * Creates all <code>{@link SampledTimeSeries}</code> objects and returns them as a vector (i.e. array list).
//     * </p>
//     * <p>
//     * Creates all the <code>{@link SampledTimeSeries}</code> instances for this sampling
//     * block using the given argument as source data.  The returned object is a vector of
//     * of such objects respecting the the argument order.  It is assumed that the data
//     * sources within the argument are all unique. Thus, this condition should be checked.
//     * </p>
//     * <p>
//     * This method is intended for the creation of attribute <code>{@link #vecTimeSeries}</code>, also needed for the
//     * <code>{@link IDataTable}</code> implementation (i.e., for table column indexing).
//     * The returned vector is ordered according to the ordering of the argument entries.
//     * </p>
//     * <h2>Concurrency</h2>
//     * If concurrency is enabled (i.e., <code>{@link #BOL_CONCURRENCY}</code> = <code>true</code>),
//     * this method utilizes streaming parallelism if the argument size is greater than the
//     * pivot number <code>{@link #SZ_CONCURRENCY_PIVOT}</code>.
//     * </p>
//     * <p>
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li>
//     * The returned Java container is mutable, additional entries can be added (e.g., null series).
//     * </li>
//     * <li>
//     * The argument should have already been checked for duplicate data source names using
//     * <code>{@link CorrelatedQueryDataOld#verifySourceUniqueness()}</code>.
//     * </li>
//     * </ul>
//     * </p>
//     * 
//     * @param lstMsgDataCols   source data for sampled time series creation
//     * 
//     * @return  vector containing a time series for each data column in the argument, ordered by argument
//     * 
//     * @throws MissingResourceException      a data column message contained no data
//     * @throws IllegalStateException         the argument contained or non-uniform data types
//     * @throws TypeNotPresentException an unsupported data type was detected within the argument
//     */
//    protected ArrayList<SampledTimeSeries<Object>> createTimeSeriesVector(List<DataColumn> lstMsgDataCols) 
//            throws MissingResourceException, IllegalStateException, TypeNotPresentException {
//
//        // List of time series are created, one for each unique data source name
//        List<SampledTimeSeries<Object>>  lstCols; // = new ArrayList<>();
//
//        // Create processing stream based upon number of data columns
//        
//        // TODO 
//        // - I think there is an IllegalStateException thrown intermittently here
//        // "End size 99 is less than fixed size 100"
//        if (BOL_CONCURRENCY && (lstMsgDataCols.size() > SZ_CONCURRENCY_PIVOT)) {
//            lstCols = lstMsgDataCols
//                    .parallelStream()
//                    .<SampledTimeSeries<Object>>map(SampledTimeSeries::from)           // throws MissingResourceException, IllegalStateExcepiont, TypeNotPresentException
//                    .toList();
//            
//        } else {
//            lstCols = lstMsgDataCols
//                    .stream()
//                    .<SampledTimeSeries<Object>>map(SampledTimeSeries::from)           // throws MissingResourceException, IllegalStateExcepiont, TypeNotPresentException
//                    .toList();
//        }
//        
//        // Create the final ArrayList (vector) for time-series and return
//        ArrayList<SampledTimeSeries<Object>>  vecCols = new ArrayList<>(lstCols);
//        
//        return vecCols;
//    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Verifies the consistency of the <code>RawCorrelatedData</code> argument.
     * </p>
     * <p>
     * This method is available to child classes for source data verification (i.e., where applicable).  
     * It checks that all data source names are unique and that all data column messages have the same size.
     * </p>
     * 
     * @param datRaw    raw correlated data under inspection
     * 
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     */
    protected void    verifySourceData(RawCorrelatedData datRaw) throws IllegalArgumentException {
        
        // Check the argument for data source name uniqueness
        ResultStatus    recUnique = datRaw.verifySourceUniqueness();
        if (recUnique.isFailure()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Correlated data block has non-unique sources: " 
                    + recUnique.message();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);

            throw new IllegalArgumentException(strMsg);
        }
        
        // Check the argument for data source size consistency
        ResultStatus    recSizes = datRaw.verifySourceSizes();
        if (recSizes.isFailure()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Correlated data block has columns with bad sizes: " 
                    + recSizes.message();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
    }
    
    /**
     * <p>
     * Creates and returns a map of (source name, time series) pairs from the argument.
     * </p>
     * <p>
     * This method is used to create a lookup map for data columns (i.e., time series) by column name
     * required by the <code>{@link IDataTable}</code> interface.  It is assumed that the argument
     * contains all time series within the sampling block.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * There are no guarantees as to mutability of the returned collection.
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
     * The returned collection is immutable.
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
