/*
 * Project: dp-api-common
 * File:	CorrelatedRawData.java
 * Package: com.ospreydcs.dp.api.query.model.correl
 * Type: 	CorrelatedRawData
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
 * @since Mar 10, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.correl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.query.model.correl.CorrelatedQueryData.StartTimeComparator;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.SamplingClock;
import com.ospreydcs.dp.grpc.v1.common.TimestampList;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

/**
 * <p>
 * Base class used for time correlation of streaming Query Service time-series data requests using 
 * both uniform sampling (i.e., sampling clocks) and spurious sampling (i.e., timestamp lists).
 * </p>
 * <p>
 * Class instances are used to collect and correlate incoming raw time-series data from a streaming data response 
 * (i.e., data contained in Protocol Buffers <code>{@link QueryData}</code> messages).  
 * Each instance contains the time-series data (i.e., sampling process data) for all data sources active during
 * a specific time interval (i.e., the time range of the data block).  
 * Through inheritance each <code>CorrelatedRawData</code> instance maintains a reference to 
 * <em>either</em> a sampling clock <em>or</em> a timestamp list for all time-series within the block.  
 * In this manner all the raw time-series data is managed by this base class and the timestamps are managed by the 
 * derived classes "correlated" by a sampling clock or a timestamp list.
 * </p>
 * <p>
 * <h2>Protocol Buffers Messages</h2>
 * Note that sampling clocks are represented
 * by the Protocol Buffers message <code>{@link SamplingClock}</code>, which has finite
 * duration.  Timestamp lists are represented by the Protocol Buffers message <code>{@link TimestampList}</code>,
 * also of finite duration.
 * All sampled-data processes (i.e., time-series data) are represented by Protocol Buffers 
 * <code>{@link DataColumn}</code> messages; they consist of a name along with a vector of data values.
 * </p>
 * <p>
 * The basic unit used for this class is the "data bucket", which is an artifact of the MongoDB system and
 * represented by the <code>QueryData.DataBucket</code> Protocol Buffers message. There are multiple data buckets
 * included within each <code>QueryData</code> message returned from the Query Service.
 * The <code>DataBucket</code> message contains a single <code>DataColumn</code> message representing the 
 * sampled process within the bucket, and either a <code>SamplingClock</code> message or a <code>TimestampList</code> 
 * message specifying the timestamps for the process.
 * </p>
 * <p>
 * <h2>Operation</h2>
 * An outside entity correlates the Query Service raw time-series data stream by attempting to insert data buckets
 * into a <code>CorrelatedRawData</code> instance using the method 
 * <code>{@link #insertBucketData(QueryDataResponse.QueryData.DataBucket)}</code>.  If the operation is successful
 * the method returns <code>true</code> and all sampled data within the bucket is assigned to the instance.  If the
 * operation fails the <code>DataBucket</code> message is still unprocessed (free) and a new 
 * <code>CorrelatedRawData</code> instance is typically created for the bucket using the 
 * <code>{@link #from(QueryDataResponse.QueryData.DataBucket)}</code> method.  The method chooses the appropriate
 * subclass and creates the new instance populated with the data from the message argument.
 * </p>
 * <p>
 * During normal operations, once the data stream containing the time-series data response is complete, 
 * time-series data within the results set for the specified duration identified by the sampling clock or timestamp list 
 * are then referenced within a single <code>CorrelatedRawData</code> instance.  That is, a <code>CorrelatedRawData</code>
 * will contain a correlated block of the raw time-series data for a finite time interval.  
 * </p>
 * <p>
 * Instances of this class are generated and maintained by the class 
 * <code>{@link RawDataCorrelator}</code>.  The class performs the correlation
 * operation on the stream of <code>{@link QueryData}</code> Protocol Buffers messages produced
 * by a Query Service data request. 
 * Each instance of <code>CorrelatedRawData</code>, in its final form, should contain a finite duration 
 * sampling interval and all time-series data from data sources that were sampled during that interval.
 * </p>
 * <p>
 * <h2>Ordering</h2>
 * The class implements the <code>{@link Comparable}</code> interface for ordering by sample 
 * clock initial start time instant.
 * That is, within collections of <code>CorrelatedRawData</code> objects, instances are ordered 
 * according to the sampling start time.  This is not an equivalence operation; it is possible 
 * that sampling intervals can overlap and this condition should be checked.  That is, the operation
 * <code>{@link Comparable#compareTo(Object)}</code> should never returns 0, as this would clobber a 
 * <code>CorrelatedRawData</code> object within a sorted collection (e.g., a <code>SortedSet</code>).
 * </p>
 * <p>
 * The enclosed class <code>{@link StartTimeComparator}</code>, implementing the 
 * <code>{@link Comparator}</code> interface, is also provided for ordering by sampling interval
 * start times. It is equivalent in function the <code>{@link Comparable}</code> interface.  
 * It is provided for explicit comparison in Java container construction.
 * </p> 
 * <p>
 * <h2>WARNINGS:</h2>
 * <ul>
 * <li>
 * It is assumed that the Data Platform archive is consistent!  If correlated 
 * data was archived simultaneously with different sample clocks or timestamp lists unpredictable 
 * behavior in reconstruction is possible.
 * </li>
 * <br/>
 * <li>
 * If the ultimate product of the <code>RawDataCorrelator</code> applied to a time-series request data stream
 * is a <code>SortedSet</code> of <code>CorrelatedRawData</code> objects, one must be careful when assembling
 * a data table.  There can be many exceptional conditions, consider the following:
 *   <ul>
 *   <li>Multiple data blocks with the same start times but different time ranges.</li>
 *   <li>Multiple data blocks with the same start times but different periods (when clocked).</li>
 *   <li>Multiple data blocks with the same start times, some uniform, some spurious.</li>
 *   <li>Combinations of the above.</li>
 *   </ul>
 * Thus, table creation is now more complex with the addition of timestamp list consideration.   
 * </li>
 * <br/>
 * <li>
 * <s>This class is only applicable to Query Service data responses whose timestamps are defined by a 
 * sampling clock.  Irregular sampling described with timestamp lists is NOT processed with 
 * <code>CorrelatedQueryData</code> instances.</s> 
 * </li>
 * </ul>
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Mar 10, 2025
 *
 * @see Comparable
 * @see RawDataCorrelator
 */
public abstract class CorrelatedRawData implements Comparable<CorrelatedRawData>{

    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new <code>CorrelatedRawData</code> subclass initialized with the argument data.
     * </p>
     * <p>
     * This is the only method available for creating new <code>CorrelatedRawData</code> instances, specifically,
     * subclasses of this type.  New <code>CorrelatedRawData</code> instances are only created when a 
     * new set of timestamps is encountered during the time-series data correlation process.  Constructors
     * are not available since instances must be created according to type of timestamps within a 
     * sampled time-series process.
     * </p>
     * <p>
     * <h2>Timestamps</h2>
     * The <code>DataTimestamp</code> message within the "data bucket" argument contains the timestamps
     * for the time-series data within the data bucket.  The timestamps are defined 
     * either by a uniform sampling clock or an explicit timestamp list, where the timestamp for each
     * data sample is explicitly presented.  The timestamp list case can represent spurious or otherwise
     * non-uniform sampling whereas a sampling clock always assumed uniform sampling.
     * </p>
     * <h2>Returned Value</h2>
     * The actual subclass returned depends upon the timestamps contained in the argument, specifically, the
     * Protocol Buffers message defining the timestamps.  The <code>SamplingClock</code> message identifies
     * a uniform sampling clock where a <code>TimestampList</code> message contains explicit timestamps.
     * The returned object type is as follows:
     * <ul>
     * <li><code>UniformRawData</code> - argument contains a <code>SamplingClock</code> message.</li>
     * <li><code>SpuriousRawData</code> - argument contains a <code>TimestampList</code> message.</li>
     * </ul>
     * The time-series data is managed in the base class and the timestamps are managed in the returned
     * child class.
     * </p>
     * 
     * @param msgBucket Protocol Buffers "data bucket" message containing single time-series sampling process
     * 
     * @return  a new <code>CorrelatedRawData</code> subclass initialized with argument data
     * 
     * @throws IllegalArgumentException the argument does not contain valid timestamps
     */
    public static CorrelatedRawData from(QueryDataResponse.QueryData.DataBucket msgBucket) throws IllegalArgumentException {
        
        // Check argument for sampling clock
        if (msgBucket.getDataTimestamps().hasSamplingClock()) {
            return new UniformRawData(msgBucket);
        }
        
        // Check argument for timestamp list
        if (msgBucket.getDataTimestamps().hasTimestampList()) {
            return new SpuriousRawData(msgBucket); 
        }
        
        // If we are here something is wrong
        String strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                + " - Argument contained neither sampling clock nor timestamp list";
        
        if (BOL_LOGGING)
            LOGGER.error(strMsg);
        
        throw new IllegalArgumentException(strMsg);
    }

    
    //
    //  Class Types
    //
    
    /**
     * <p>
     * <code>{@link Comparator}</code> implementation for ordered comparison of two 
     * <code>CorrelatedRawData</code> instances.
     * </p>
     * <p>
     * The comparison here is performed with the start times of the sampling intervals 
     * represented by <code>{@link CorrelatedRawData}</code> time <code>{@link Instant}</code>
     * returned from the <code>{@link CorrelatedRawData#getStartTime()}</code> method.
     * Instances are intended for use in Java container construction.
     * </p>
     *
     * @author Christopher K. Allen
     * @since Jan 12, 2024
     *
     */
    public static class StartTimeComparator implements Comparator<CorrelatedRawData> {

        //
        // Creators
        //
        
        /**
         * Creates a new <code>StartTimeComparator</code> instance.
         * 
         * @return  new  <code>StartTimeComparator</code> instance ready for use
         */
        public static StartTimeComparator   create() {
            return new StartTimeComparator();
        }
        
        //
        // Comparator<CorrelatedQueryData> Interface
        //
        
        /**
         * <p>
         * Used for ordering within sets and maps of <code>CorrelatedRawData</code> objects.  Returns
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
        public int compare(CorrelatedRawData o1, CorrelatedRawData o2) {
            Instant t1 = o1.getStartTime();
            Instant t2 = o2.getStartTime();
            
            return t1.compareTo(t2);
        }
    }

    
    //
    // Application Resources
    //
    
    /** The Data Platform API default configuration parameter set */
    protected static final DpQueryConfig  CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Resources
    //
    
    /** Event logger */
    protected static final Logger    LOGGER = LogManager.getLogger();
    
    
    //
    // Class Constants
    //
    
    /** Is logging active */
    protected static final boolean    BOL_LOGGING = CFG_QUERY.logging.active;
    
    
    /** General timeout limit  - for parallel thread pool tasks */
    protected static final long       LNG_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** General timeout units - for parallel thread pool tasks */
    protected static final TimeUnit   TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
    // 
    // Defining Attributes 
    //
    
    /** The time range of contained raw time series data */
    protected final TimeInterval      tvlRange;
    
    /** List of all data column Protocol Buffers messages correlated within sampling interval - correlated objects */
    protected final List<DataColumn>  lstMsgCols = new LinkedList<>();
    
    
    //
    // Instance Resources
    //
    
    /** Set of all unique data source names active within sampling interval - taken from incoming data messages */
    protected final Set<String>     setSrcNms = new TreeSet<>();
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>CorrelatedRawData</code> instance initialized from the given arguments
     * (sans the timestamps).
     * </p>
     *
     * @param tvlRange  the time range for the new correlated block 
     * @param msgBucket the raw data bucket containing process data (ignore timestamps)
     */
    protected CorrelatedRawData(TimeInterval tvlRange, QueryData.DataBucket msgBucket) {
        
        // Set the time interval for the block (including start time)
        this.tvlRange = tvlRange;
        
        // Extract the process data and store
        DataColumn  msgDataCol = msgBucket.getDataColumn();
        
        this.setSrcNms.add(msgDataCol.getName());
        this.lstMsgCols.add(msgDataCol);
    }

    
    //
    // Abstract Methods
    //
    
    /**
     * <p>
     * Returns the number of samples for each data column as specified in the sampling interval message.
     * </p>
     * <p>
     * Obtains the returned value from the Protocol Buffers message identifying the timestamps.
     * All correlated data columns <em>should</em> have the same number of data values (i.e.,
     * the returned value) if the Data Platform archive is consistent. 
     * </p>
     * 
     * @return  the size of each data column within the correlated time-series collection
     */
    abstract public int getSampleCount();
    
    /**
     * <p>
     * Adds the <code>DataColumn</code> message within the argument to the collection of 
     * correlated data ONLY IF sampling intervals and timestamp specifications are equivalent 
     * AND the data column is not already referenced.
     * </p>
     * <p>
     * <h2>Operation</h2>
     * This is the primary processing operation for the <code>CorrelatedRawData</code>
     * class.  It performs the following operations:
     * <ol>
     * <li>
     * Checks for an equivalent timestamps within the argument and, if so, continues
     * to the next step.  
     * If the timestamps are NOT equivalent then nothing is done and a value 
     * <code>false</code> is returned.
     * </li>
     * <br/>
     * <li>
     * If the argument has equivalent timestamps, the argument data column is then
     * checked to see if already exists within the referenced collection (checked by name).  
     * If it is already present in the collection nothing is done and the method returns <code>false</code>.
     * </li>
     * </ol>
     * </p>
     * <p>
     * <h2>Concurrency (Thread Safe)</h2>
     * This operation <em>must be</em> atomic and is therefore <em>must be</em> synchronized for thread safety.
     * Thus, a single instance of <code>CorrelatedQueryData</code> can be processed concurrently for 
     * multiple <code>DataBucket</code> messages on separate execution threads with this method.  
     * Doing so will provide consistent results as NO duplicate data sources within the same 
     * sampling clock are accepted.  
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The correlated data set can only have ONE data source entry.  Attempting to insert
     * a multiple time-series columns (by name) for the SAME timestamps indicates a likely error with  
     * the Query Service data request response, or the data archive itself.  Especially egregious 
     * are multiple data source with different heterogeneous types.  
     * This error is not caught here; the insertion is simply rejected by returning the value <code>false</code>. 
     * </li>
     * <br/>
     * <li>
     * If the above duplicates are exist, the data source entry encountered FIRST will be entered into 
     * the correlated data set.
     * </li>
     * </ul>
     * </p>
     * 
     * @param msgBucket Protocol Buffers message containing timestamps and column data
     * 
     * @return      <code>true</code> only if argument data was successfully added to this reference,
     *              <code>false</code> otherwise (nothing done)
     */
    public abstract boolean insertBucketData(QueryDataResponse.QueryData.DataBucket msgBucket);
    

    //
    // Comparable<CorrelatedRawData> Interface
    //
    
    /**
     * <p>
     * Compares the start time of the given <code>CorrelatedRawData</code> with this one.
     * </p>
     * <p>
     * Used for ordering within sets and maps of <code>CorrelatedQueryData</code> objects.  Returns
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
    public int compareTo(CorrelatedRawData o) {
        if (this.tvlRange.begin().isBefore(o.tvlRange.begin()))
            return -1;
        else
            return 1;
    }

    
    //
    // Operations - State Inquiry
    //
    
    /**
     * <p>
     * Returns the starting time instant of the sampling interval for this block.
     * </p>
     * <p>
     * All time-series data within this correlated data set should have the same start
     * time (i.e., the returned value).  This value is determined by the appropriate Protocol 
     * Buffers message and stored within the time range interval 
     * </p>
     * 
     * @return sampling interval start instant
     */
    public Instant getStartTime() {
        return this.tvlRange.begin();
    };
    
    /**
     * <p>
     * Returns the time domain of the sampling interval.
     * </p>
     * <p>
     * The time domain is the <em>smallest</em>, connected time interval that contains
     * all sample points.  Thus, the returned interval DOES NOT include the last sample
     * period.
     * </p>
     * <p>
     * The returned interval left end point is the start time of the sampling interval 
     * returned by <code>{@link #getStartInstant()}</code>.  The right end point is the
     * last timestamp of the sampled data within the query data.  Specifically, it 
     * DOES NOT include the sample period duration following the last timestamp.
     * </p>
     * <p>
     * When a sampling clock is specified the returned time interval <i>I</i> is given by
     * <pre>
     *   <i>I</i> = [<i>t</i><sub>0</sub>, <i>t</i><sub>0</sub> + (<i>N</i> - 1)<i>T</i>] 
     * </pre>
     * where <i>t</i><sub>0</sub> is the clock start time 
     * (returned by <code>{@link #getStartInstant()}</code>), <i>N</i> is the number of 
     * samples (returned by <code>{@link #getSampleCount()}</code>), and <i>T</i> is the
     * sampling period (returned by <code>{@link #getSamplingPeriod()}</code>).
     * </p>
     * <p>
     * When a timestamp list is specified the returned time interval <i>I</i> is given by
     * <pre>
     *   <i>I</i> = [<i>t</i><sub>0</sub>, <i>t</i><sub><i>N</i>-1</sub>]
     * </pre>
     * where <i>t</i><sub>0</sub> is the first time stamp
     * (returned by <code>{@link #getStartInstant()}</code>), <i>N</i> is the number of 
     * samples (returned by <code>{@link #getSampleCount()}</code>), and <i>t</i><sub><i>N</i>-1</sub> 
     * is the last time stamp.
     * </p>
     * 
     * @return  the time range of correlated query data [first timestamp, last timestamp]
     */
    public TimeInterval getTimeRange() {
        return this.tvlRange;
    }
    
    /**
     * <p>
     * Returns the number of unique data source names within the current collection of data messages.
     * </p>
     * <p>
     * <code>CorrelatedQueryData</code> objects maintain a set of unique data source names
     * for all Query Service response data successfully inserted into the correlated collection.
     * All data sources within the collection should be unique for a consistent data archive. 
     * </p>
     * 
     * @return  number of unique data source names encountered so far
     * 
     * @see #getSourceNames()
     */
    public final int    getSourceCount() {
        return this.setSrcNms.size();
    }
    
    /**
     * <p>
     * Returns the set of unique names for all the data sources that are active within the
     * associated sampling interval.
     * </p>
     * <p>
     * <code>CorrelatedQueryData</code> objects maintain a set of unique data source names
     * for all Query Service response data successfully inserted into the correlated collection.
     * All data sources within the collection should be unique for a consistent data archive. 
     * </p>
     *  
     * @return  set of unique data source names for current collection of correlated data 
     */
    public final Set<String>    getSourceNames() {
        return this.setSrcNms;
    }
    
    /**
     * <p>
     * Returns the entire collection of correlated Protobuf data column message associated 
     * with this sampling interval.
     * </p>
     * <p>
     * The returned collection of data messages are all sampled according to the sampling
     * clock described by <code>{@link #getSamplingMessage()}</code>.  It represents the
     * correlated data set of this <code>CorrelatedQueryData</code> instance.
     * </p>
     * 
     * @return all correlated data currently associated with the sampling interval 
     */
    public final List<DataColumn> getRawDataMessages() {
        return this.lstMsgCols;
    }
    

    //
    // Operations - Data Verifications
    //
    
    /**
     * <p>
     * Verifies that the given set of correlated data has unique data source names.
     * </p>
     * <p>
     * Note that the data source name for each inserted <code>BucketData</code> message is 
     * recorded during insertion.  This method verifies that the current set of data inserted
     * into this instance is consistent and ready for further processing.
     * </p>
     * <p>
     * The following conditions are checked:
     * <ul>
     * <li>Each registered data source has only one contribution to the correlated data set.</li>
     * <li>Each registered data source has at least one contribution to the correlated data set.</li>
     * </ul>
     * If the current correlated data set fails the verification check, the cause of the failure
     * is included in the result.  Otherwise no cause message is provided.
     * </p> 
     * 
     * @return  result of the verification check, containing the cause if failure
     */
    public ResultStatus verifySourceUniqueness() {
        
        // Create list of all (potentially repeating) data source names within column collection
        List<String>    lstColNms = this.lstMsgCols.stream().map(DataColumn::getName).toList();
        List<String>    vecColNms = new ArrayList<>(lstColNms);
        
        // Remove from the above vector - each unique data source name recorded so far
        boolean bolAllRemoved = this.setSrcNms
                            .stream()
                            .allMatch(strNm -> vecColNms.remove(strNm));

        // Check for registered data sources that are missing from the data columns list
        if (!bolAllRemoved) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Serious ERROR: time-series data column list was missing at least one data source."; 
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            return ResultStatus.newFailure(strMsg);
        }
        
        // Check for repeated data source entries within the data columns list
        if (!vecColNms.isEmpty()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + "- Serious ERROR: Data column list contains multiple entries for following data sources: " 
                    + lstColNms;
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            return ResultStatus.newFailure(strMsg);
        }
        
        return ResultStatus.SUCCESS;
    }
    
    /**
     * <p>
     * Verifies that the given set of correlated data has source data sets all of same size.
     * </p>
     * <p>
     * Note that the data sources should all have data sets of the same size.  Further, the
     * size should equal the number of samples specified in the sampling interval message.  
     * This method verifies that the current set of data inserted
     * into this instance is consistent with the above and ready for further processing.
     * </p>
     * <p>
     * The following conditions are checked:
     * <ul>
     * <li>Each data source has size equal to the number data samples in sampling message.</li>
     * </ul>
     * If the current correlated data set fails the verification check, the cause of the failure
     * is included in the result.  Otherwise no cause message is provided.
     * </p> 
     * 
     * @return  result of the verification check, containing the cause if failed
     */
    public ResultStatus verifySourceSizes() {
        
        // Each source should provide the same number of data samples
        int cntSamples = this.getSampleCount();
        
        // Get list of all data columns with different size
        List<DataColumn> lstBadCols = this.lstMsgCols
                .stream()
                .filter(msg -> msg.getDataValuesCount() != cntSamples)
                .toList();
        
        // If the list is empty we passed the test
        if (lstBadCols.isEmpty())
            return ResultStatus.SUCCESS;
        
        // Test failed - return failure with list of source names and count
        List<String> lstFailedSrcs = lstBadCols
                .stream()
                .map(msg -> msg.getName() + ": " + Integer.toString(msg.getDataValuesCount()))
                .toList();
        
        String  strMsg = "Data column(s) had value count != " + Integer.toString(cntSamples) + ": " + lstFailedSrcs;
        
        if (BOL_LOGGING)
            LOGGER.error(strMsg);
        
        return ResultStatus.newFailure(strMsg);
    }
    
    
}
