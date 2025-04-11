/*
 * Project: dp-api-common
 * File:	RawDataCorrelator.java
 * Package: com.ospreydcs.dp.api.query.model.correl
 * Type: 	RawDataCorrelator
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
 * @since Mar 13, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.correl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData.DataBucket;

/**
 * <p>
 * Time correlates the result set from a stream of <code>QueryResponse</code> Protocol Buffers messages.
 * </p>
 * <p>
 * Correlates all time-series data within the results set of a Query Service data request into
 * a sorted collection of <code>{@link RawCorrelatedData}</code> data instances.  
 * Each instance within the sorted collect contains a correlated collection of <code>{@link DataColumn}</code> 
 * Protocol Buffers time-series data messages.
 * Each instance within the sorted collection also contains either a <code>{@link SamplingClock}</code> sampling 
 * clock Protobuf message or a <code>{@link TimestampList}</code> defining the sampling process timestamps.
 * All data within the <code>RawCorrelatedData</code> instances are correlated to the above message.
 * </p>  
 * <p>
 * Once processing of a results set is complete, the results are available from the 
 * <code>{@link #getCorrelatedSet()}</code> method.
 * Note, however, <code>RawDataCorrelator</code> objects maintain ownership of the returned
 * sorted set and all data within that set should be further processed (or copied) before 
 * attempting to correlated another results set; specifically, before invoking the 
 * <code>{@link #reset()}</code> method. 
 * </p>
 * <p>
 * <h2>Time Correlation</h2>
 * The intent is to correlate the results set of a Data Platform Query Service data request 
 * according to the sampling clocks.  That is, the correlator parses the results set for 
 * equivalent sampling intervals as specified by the sampling clock data message 
 * <code>{@link SamplingClock}</code> or timestamp list message <code>{@link TimestampList}</code>. 
 * The time-series for each data source corresponding to equivalent timestamps are collected
 * into a single <code>{@link RawCorrelatedData}</code> instance.  
 * Once the query results set is fully processed there should be one <code>RawCorrelatedData</code> 
 * instance for every unique sampling interval within the result set.  Every data column within the 
 * results set will be associated with one <code>RawCorrelatedData</code> instance.
 * </p>
 * <p>
 * <h2>Operation</h2>
 * Data correlation is performed using the methods prefixed with <code>add</code>.  The preferred
 * method is 
 * <code>{@link #processQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)}</code>,
 * which processes data messages within the Query Service response stream.  That is, the data
 * messages are extracted from the <code>{@link QueryResponse}</code> messages externally.
 * However, for convenience there are additional <code>add</code> methods that extract the data
 * messages which are then passed to the above method for actual processing.  Exceptions are
 * thrown if an error is encountered within the response message.
 * Processing follows the general flow:
 * <ol>
 * <li>Results sets are "added" to the correlator, message by message, until stream completion.</li>
 * <li>Correlated data is then available from the method <code>{@link #getCorrelatedSet()}</code>.</li>
 * <li>The correlator is then reset for further processing with method <code>{@link #reset()}</code>.</li>
 * </ol>
 * </p>
 * <p>
 * Once the entire results set data is "added" to a <code>RawDataCorrelator</code> object, 
 * it is fully correlated.  The correlated data is then available from the method
 * <code>{@link #getCorrelatedSet()}</code>.  Each <code>{@link CorrelatedQueryData}</code> 
 * instance within the returned set contains all the data messages for a single sampling clock.
 * Further, the set is ordered according to the start time instant for each clock.
 * </p>
 * <p>
 * <code>RawDataCorrelator</code> objects can be reused.  That is, a single 
 * <code>RawDataCorrelator</code> instance can be used to process multiple data request
 * results sets (serially, of course).  To reuse a instances invoke the <code>{@link #reset()}</code>
 * method before processing another results set.  WARNING: <code>RawDataCorrelator</code> 
 * objects maintain ownership of the correlated data set returned by 
 * <code>{@link #getCorrelatedSet()}</code>.  Thus, all data returned by that method must be
 * either copied or fully processed before reusing an instance.
 * </p>
 * <p>
 * All correlation operations are done ATOMICALLY.  Specifically, the 
 * <code>{@link #processQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)}</code>
 * method is synchronized with the internal lock <code>{@link #objLock}</code>.  If concurrent
 * threads attempt to add Query Service messages they will block until the previous correlation
 * operation is complete.  Thus condition is necessary to maintain consistency within the
 * processed set.     
 * </p>
 * <p>
 * <h2>Data Processing Verification</h2>
 * There are multiple static utility methods for verifying correct processing of a results set.  
 * These methods are prefixed with <code>verify</code> in their method name.  They perform 
 * various verification checks on the processed results set obtained from 
 * <code>{@link #getCorrelatedSet()}</code>, such as ordering, time domain collisions, and
 * time series sizes.
 * </p>
 * <p>
 * <h2>Concurrency</h2>
 * The correlation operations performed within <code>RawDataCorrelator</code>
 * allow a high-level of multi-threaded, concurrent, data processing.  Using concurrency
 * can significantly reduce processing time for a results set.  However, use of concurrency
 * can also steal CPU resources for any other concurrent operations.
 * <ul>
 * <li>
 * The concurrency mechanism utilizes <code>{@link DataBucketInsertTask}</code> instances to
 * perform thread operations.  Each task attempts to insert a single data bucket message
 * into the current target set of correlated data and can be executed on a separate thread.
 * </li>
 * <br/> 
 * <li>
 * Concurrency will not be activated until the size of the correlated data set
 * <code>{@link #setPrcdData}</code> becomes larger than the 
 * <code>{@link #SZ_CONCURRENCY_PIVOT}</code> value.  This is done to avoid the overhead
 * of parallel processing for small data sets.  Thus, the <code>{@link #SZ_CONCURRENCY_PIVOT}</code>
 * can be used as a tuning parameter.  The default value is taken from the
 * Query Service API configuration parameters.  
 * </li>
 * <br/>
 * <li>
 * The number of independent threads used for concurrent processing is determined by
 * <code>{@link #CNT_CONCURRENCY_THDS}</code>.  The default value is taken from the
 * Query Service API configuration parameters.  
 * </li>
 * <br/>  
 * <li>
 * For some situations it may be desirable to stop concurrent processing
 * so it does not interfere with other real-time operations (e.g., such as gRPC streaming).
 * The method <code>{@link #setConcurrency(boolean)}</code> is available to toggle processing
 * concurrency ON/OFF.
 * </li>
 * <br/>
 * <li>
 * Due to thread safety, concurrency toggling is synchronized.  That is, a toggle operation
 * <code>{@link #setConcurrency(boolean)}</code> will not apply until a correlation
 * operation (i.e., <code>add...</code> method) has completed.  The
 * <code>{@link #setConcurrency(boolean)}</code> will block on the internal synchronization
 * lock <code>{@link #objLock}</code> until a processing method releases it.
 * </li>
 * <br/>
 * <li>
 * The <code>RawDataCorrelator</code> class uses the default concurrency configuration in the 
 * Query Service API default parameters (@see {@link DpApiConfig}</code>). The concurrency 
 * configuration parameters there can be used to tune performance (or hard-coded into this
 * class).
 * </li>
 * </ul>
 * </p>
 * <p>
 * <h2>WARNING - Data Validation:</h2>
 * This class contains several methods for various aspects of data validation.  These are static methods
 * prefixed with <code>verify...()</code> and are available to check the current correlated set.  These
 * methods can be expensive to perform but are valuable in testing.  It is recommended that once data
 * validation is confirmed, for performance reasons, use of these methods be limited.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * Correlator objects maintain ownership of processed data sets available with
 * <code>{@link #getCorrelatedSet()}</code>.  Invoking the <code>{@link #reset()}</code>
 * method will destroy the current processed data set.
 * </li>
 * <br/>
 * <li>
 * It may be beneficial to limit the number of concurrent processing threads
 * (i.e., within the default API configuration parameters)
 * so that concurrent processing does not interfere with real-time operations requiring
 * dedicated processor cores.
 * </li>
 * <br/>
 * <li>
 * It may be beneficial to eliminate concurrency altogether using method 
 * <code>{@link #setConcurrency(boolean)}</code>.
 * </li>
 * <br/>
 * <li>
 * The processing methods <code>add...</code> are internally synchronized and competing threads
 * will block until the method completes the correlation operation.
 * </li>
 * </ul>
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Mar 13, 2025
 *
 */
public class RawDataCorrelator {

    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new <code>RawDataCorrelator</code> instance ready for processing.
     * </p>
     * <p>
     * Instances are created in their initial state - ready for processing.  The method 
     * <code>{@link #processQueryData(QueryDataResponse.QueryData)}</code>
     * is recommended for query response data processing; that is, it is recommended to process all query and 
     * streaming errors externally, supplying only <code>QueryData</code> messages to 
     * <code>RawDataCorrelator</code> objects.
     * </p>
     * <p>
     * One the entire query results set has be passed to a <code>RawDataCorrelator</code> object
     * (e.g., using the above method repeated for each data message), the correlated data is recoverable
     * with <code>{@link #getCorrelatedSet()}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * <code>RawDataCorrelator</code> objects can be reused for multiple query results sets.
     * Use the method <code>{@link #reset()}</code> before processing a new results set.
     * </li>
     * <li>
     * <code>RawDataCorrelator</code> objects maintain ownership of their correlated sets available from
     * method <code>{@link #getCorrelatedSet()}</code> .  Be sure to
     * process all correlated data before calling <code>{@link #reset()}</code>.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  a new <code>RawDataCorrelator</code> instance ready for processing
     * 
     * @see #processQueryData(com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData)
     * @see #getCorrelatedSet()
     */
    public static RawDataCorrelator create() {
        return new RawDataCorrelator();
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
    
    
    /** Parallelism timeout limit  - for parallel thread pool tasks */
    public static final long       LNG_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** Parallelism timeout units - for parallel thread pool tasks */
    public static final TimeUnit   TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
    /** Concurrency active flag */
    public static final boolean    BOL_CONCURRENCY = CFG_QUERY.concurrency.active;
    
    /** Parallelism tuning parameter - pivot to parallel processing when target set size hits this limit */
    public static final int        SZ_CONCURRENCY_PIVOT = CFG_QUERY.concurrency.pivotSize;
    
    /** Parallelism tuning parameter - default number of independent processing threads */
    public static final int        CNT_CONCURRENCY_THDS = CFG_QUERY.concurrency.maxThreads;
    
    
    //
    // Class Resources
    //
    
    /** Event logger */
    private static final Logger    LOGGER = LogManager.getLogger();
    
    
    //
    // Instance Resources
    //
    
    /** Synchronization lock for thread safety */
    private final Object            objLock = new Object();
    
    /** Target set of correlated results set data - Ordered according to sampling interval start time */
    private final SortedSet<RawCorrelatedData> setPrcdData = new TreeSet<>(RawCorrelatedData.StartTimeComparator.create());

    /** Manages thread pools of many, short-lived execution tasks */
//  private final ExecutorService   exeThreadPool = Executors.newCachedThreadPool();
  private ExecutorService   exeInsertTasks = Executors.newFixedThreadPool(CNT_CONCURRENCY_THDS);
  
    
    //
    // State Variables
    //
    
    /** Toggle the use of concurrency in data processing */
    private boolean     bolConcurrency = BOL_CONCURRENCY;
    
    /** The number of threads to use for concurrent data processing */
    private int         cntMaxThreads = CNT_CONCURRENCY_THDS;
    
    /** The size of the correlated set triggering concurrent data processing */
    private int         szSetConcPivot = SZ_CONCURRENCY_PIVOT;
    
    /** Byte counter for processed data - measured results set size */
    private long        lngBytesProcessed = 0;
    

    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>RawDataCorrelator</code> instance in default configuration (empty).
     * </p>
     */
    public RawDataCorrelator() {
    }

    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Enables the use of concurrent data processing and sets the maximum number of processing threads. 
     * </p>
     * <p>
     * This is a thread-safe operation and will not interrupt any current processing.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The correlation operations performed within <code>RawDataCorrelator</code>
     * allow a high-level of multi-threaded, concurrent, data processing.  Using concurrency
     * can significantly reduce processing time for a results set.
     * </li>
     * <br/>
     * <li>
     * The <code>RawDataCorrelator</code> is currently designed to exploit multiple
     * CPU cores for correlation operations (i.e., method prefixed with <code>add</code>).  
     * Thus, for some situations it may be desirable to stop concurrent processing
     * so it does not interfere with other real-time operations (e.g., such as gRPC streaming).
     * </li>
     * <br/>
     * <li>
     * For to thread safety this method is synchronized internally. Concurrency processing will not be 
     * toggled until a correlation operation 
     * (i.e., <code>{@link #processQueryData(com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData)}</code>) 
     * has completed.
     * </li>
     * </ul>
     * </p>
     * <p>
     * The default value for concurrency is given by the constant <code>{@link #BOL_CONCURRENCY}</code> which
     * is taken from the Java API Library configuration parameters.  The default value for the maximum
     * number of processing threads is <code>{@link #CNT_CONCURRENCY_THDS}</code> which is also taken
     * from the library configuration parameters. 
     * </p>   
     * 
     * @param cntMaxThreads    the maximum number of processing threads to use
     * 
     * @see #disableConcurrency()
     */
    public void enableConcurrency(int cntMaxThreads) {
        synchronized (this.objLock) {
            this.bolConcurrency = true;
            this.cntMaxThreads = cntMaxThreads;
            
            this.exeInsertTasks = Executors.newFixedThreadPool(this.cntMaxThreads);
        }
    }
    
    /**
     * <p>
     * Disables the use of concurrent data processing.
     * </p> 
     * <p>
     * This is a thread-safe operation and will not interrupt any current processing.
     * </p>
     * <p>
     * See class documentation for <code>{@link #enableConcurrency(int)}</code> for details
     * on concurrent data processing.
     * </p>
     * <p>
     * The default value for this parameter is given by the constant <code>{@link #BOL_CONCURRENCY}</code> which
     * is taken from the Java API Library configuration parameters.
     * </p>   
     * 
     * @see #enableConcurrency(int)
     */
    public void disableConcurrency() {
        synchronized (this.objLock) {
            this.bolConcurrency = false;
        }
    }
    
    /**
     * <p>
     * Sets the size of the correlated raw data set (i.e., number of blocks) triggering concurrent processing.
     * </p>
     * <p>
     * This is a thread-safe operation and will not interrupt any current processing.
     * </p>
     * <p>
     * This parameter is active only if concurrent processing is enabled (see <code>{@link #enableConcurrency(int)}</code>).
     * This is a tuning parameter which prevent concurrent processing for correlated data set sizes below the
     * given value.  The intent is to prevent the allocation of independent thread resources if none are required.
     * A value of 1 will ensure concurrent processing for all situations.
     * </p>
     * <p>
     * The default value for this parameter is given by the constant <code>{@link #SZ_CONCURRENCY_PIVOT}</code> which
     * is taken from the Java API Library configuration parameters.
     * </p>   
     * 
     * @param szPivot   set of the correlated data set triggering concurrent processing
     */
    public void setConcurrencyPivotSize(int szPivot) {
        synchronized (this.objLock) {
            this.szSetConcPivot = szPivot;
        }
    }

    /**
     * <p>
     * Reset this correlator instance to its original (default) state.
     * </p>
     * <p>
     * <code>QueryDataCorrelator</code> objects can be reused.  After calling this method the 
     * correlator is returned to its initial state and is ready to process another Query Service
     * response results set.
     * </p>
     * <p>
     * This is a thread-safe operation.  Performs the following operations:
     * <ul>
     * <li>Clears out the target set of all <code>CorrelatedQueryData</code> references.</li>
     * <li>Returns the concurrency flag to its default state <code>{@link #BOL_CONCURRENCY}</code>.</li>
     * </ul>
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * Any processed data previous returned by <code>{@link #getCorrelatedSet()}</code> is destroyed
     * by this method.  All previously correlated data should be either copied or fully processed
     * before calling this method.
     * </p>
     * 
     */
    public void reset() {
        synchronized (this.objLock) {
            this.lngBytesProcessed = 0L;
            this.setPrcdData.clear();
        }
    }
    
    
    //
    // Configuration Inquiry
    //
    
    /**
     * <p>
     * Determines whether or not concurrent processing of raw time-series data is enabled.
     * </p>
     * 
     * @return  <code>true</code> if concurrent processing of raw data is enabled, <code>false</code> otherwise
     * 
     * @see #enableConcurrency(int)
     * @see #disableConcurrency()
     */
    public boolean  isConcurrencyEnabled() {
        return this.bolConcurrency;
    }
    
    /**
     * <p>
     * Returns the maximum number of allowable concurrent threads for processing of raw time-series data.
     * </p>
     * <p>
     * Note that this value only has context if concurrent processing is enabled.  
     * See method <code>{@link #isConcurrencyEnabled()}</code>.
     * </p>
     *   
     * @return  the maximum number of concurrent processing threads
     */
    public int  getConcurrencytMaxThreads() {
        return this.cntMaxThreads;
    }
    
    /**
     * <p>
     * Returns the size of the correlated data set which triggers concurrent processing.
     * </p>
     * <p>
     * Note that this value only has context if concurrent processing is enabled.  
     * See method <code>{@link #isConcurrencyEnabled()}</code>.
     * </p>
     * 
     * @return  the size of the correlated raw data set required for concurrent processing 
     * 
     * @see #setConcurrencyPivotSize(int)
     */
    public int  getConcurrencyPivotSize() {
        return this.szSetConcPivot;
    }
    
 
    //
    // Attribute Query
    //
    
    /**
     * <p>
     * Returns the target set of <code>CorrelatedQueryData</code> instances in its current 
     * processing state.
     * </p>
     * <p>
     * If the entire results set of a Query Service data request has been added to the
     * correlator instance then the returned collection is the fully correlated data set
     * of the data request, ordered according to sampling clock start times.
     * <p>
     * <h2>WARNING:</h2>
     * This <code>QueryDataCorrelator</code> instance retains ownership of the returned set.
     * If the <code>{@link #reset()}</code> method is invoked this set is destroyed.
     * All subsequent processing of the returned data set must be completed before invoking
     * <code>{@link #reset()}</code>, or the data set must be copied.
     * <p>
     * 
     * @return  the sorted set (by start-time instant) of currently processed data 
     */
    public final SortedSet<RawCorrelatedData>   getCorrelatedSet() {
        return this.setPrcdData;
    }
    
    /**
     * <p>
     * Returned the number of bytes the correlator has processed so far.
     * </p>
     * <p>
     * The returned value is the serialized size of all Protobuf messages added to the correlator for
     * processing so far.  The value provides an estimate of size of any sampling process or results set
     * table created from the processed data.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Actual processed data sizes (in bytes) are typically larger than serialized size (> ~110%).</li>
     * <li>This value is reset to 0 after invoking <code>{@link #reset()}</code>.</li>
     * </ul>
     * </p>
     * 
     * @return      the number of serialized bytes processed by the correlator so far
     */
    public long getBytesProcessed() {
        return this.lngBytesProcessed;
    }
    
    /**
     * <p>
     * Returns the size of the correlated set of <code>CorrelatedQueryData</code> 
     * instances processed so far.
     * </p>
     * 
     * @return  current size of the correlated data set 
     */
    public int sizeCorrelatedSet() {
        return this.setPrcdData.size();
    }
    
    /**
     * <p>
     * Extracts and returns a set of unique data source names for all data within target set.
     * </p>
     * <p>
     * Extracts all data source names within the target set of <code>CorrelatedQueryData</code> 
     * objects and collects them to the returned set of unique names.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This operation can be expansive for large data sets.  Use should be limited.
     * </p>
     * 
     * @return  set of all data source names within the processed results set so far
     */
    public Set<String>   extractDataSourceNames() {
        Set<String> setNames = this.setPrcdData
                .stream()
                .collect(
                        TreeSet::new, 
                        (set, r) -> set.addAll(r.getSourceNames()), 
                        TreeSet::addAll
                        );
        return setNames;
    }
    
    
    //
    // Processing Operations
    //
    
    /**
     * <p>
     * Adds all data within a Query Service <code>BucketData</code> message <code>QueryResponse</code> message.
     * </p>
     * <p>
     * The preferred method for correlating results sets from Query Service data requests.
     * Correlates all data columns within the <code>BucketData</code> message, adding the 
     * data into the current target set of correlated data instances, creating new instances 
     * if needed.  (No data stream error checking is enforced.)
     * </p>
     * <p>
     * Once this method returns all <code>DataBucket</code> messages within the argument are 
     * processed and its data column is associated with some <code>{@link CorrelatedQueryData}</code>
     * instance within the target set.
     * </p>
     * <p>
     * <h2>Concurrency</h2>
     * The data processing operation pivots upon the size of the current target set.
     * For target set size less than <code>{@link #SZ_CONCURRENCY_PIVOT}</code> data buckets are 
     * processed serially, as frequent additions to the target reference set are expected.  
     * For larger target sets the processing technique pivots to a parallel method.  An attempt 
     * is made to insert each data bucket within the argument concurrently into the target set.  
     * (The probability of insertion is higher since the target set is large.) 
     * Then a <code>CorrelatedQueryData</code> set is created for the collection of data buckets 
     * that failed insertion. The new correlated set is then added to the target set.  Note
     * that the new correlated set is necessary disjoint (i.e., references different sampling
     * clocks) to the current target set.
     * </p>
     * <p>
     * This must be an atomic operation, since it typically modifies the target set. 
     * Thus, it synchronizes on the <code>{@link #objLock}</code> lock to maintain thread
     * safety.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This is the preferred method of data processing.  It processes all data within a 
     * <code>QueryResponse.QueryReport.BucketData</code> message without error checking.
     * </p>
     *  
     * @param msgData   data message extracted from response data message
     * 
     * @throws IllegalArgumentException a <code>DataBucket</code> message did not contain a sampling clock
     * @throws CompletionException      error in <code>DataBucket</code> insertion task execution (see cause)
     */
    public void processQueryData(QueryDataResponse.QueryData msgData) throws IllegalArgumentException, CompletionException {
        
        // Check for empty data message
        if (msgData.getDataBucketsList().isEmpty()) {
            if (BOL_LOGGING)
                LOGGER.warn("{}: attempt to insert data from empty data message.", JavaRuntime.getMethodName());
            
            return;
        }
        
        // This operation must be atomic - synchronized for thread safety
        synchronized (this.objLock) {
            
            // If the target set is small or concurrency is inactive - insert all data at once
            if (!this.bolConcurrency || (this.setPrcdData.size() < this.szSetConcPivot)) {
                this.processDataSerial(msgData);

                // Increment byte counter
                this.lngBytesProcessed += msgData.getSerializedSize();
                
                return;
            }

            // If the target set is large - pivot to concurrent processing of message data
            Collection<QueryData.DataBucket>    setFreeBuckets = this.attemptDataInsertConcurrent(msgData);
            SortedSet<RawCorrelatedData>        setDisjointData = this.processDisjointRawData(setFreeBuckets); // done serially
            this.setPrcdData.addAll(setDisjointData);

            // Increment byte counter
            this.lngBytesProcessed += msgData.getSerializedSize();
        }
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Processes all data columns within the <code>QueryData</code> message into the target 
     * set of correlated data, creating new target data instances if necessary.
     * </p>
     * <p>
     * This method processes each <code>DataBucket</code> message within the argument serially.
     * An attempt is made to insert its data column into the existing target set of correlated data.
     * If the attempt fails a new <code>RawCorrelatedData</code> instance is created, the message is associated, 
     * then the instance is added to the target set.
     * </p>
     * <p>
     * This method leaves the overall collection consistent.  All the <code>DataBucket</code> 
     * messages within the argument are processed upon completion. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method should be efficient for small target sets where the probability for new 
     * reference creation is high.  All operations are serial.
     * </li>
     * <br/>
     * <li>
     * This method is not thread safe.  The method modifies the target set of references.  
     * The target set must not be modified during method call and should be synchronized 
     * externally.
     * </li>
     * </ul>
     * </p>
     * 
     * @param msgData   Query Service data message to be processed into this collection
     */
    private void processDataSerial(QueryDataResponse.QueryData msgData) {

        for (QueryDataResponse.QueryData.DataBucket msgBucket : msgData.getDataBucketsList()) {
            
            // Attempt to add the message data into the current set of sampling interval references
            boolean bolSuccess = this.setPrcdData
                    .stream()
                    .anyMatch( i -> i.insertBucketData(msgBucket) );

            // If the message data was not added we must create a new reference and add it to the current target set
            if (!bolSuccess) {
                RawCorrelatedData   blkRaw = RawCorrelatedData.from(msgBucket);
                
                this.setPrcdData.add( blkRaw );
            }
            
        }
    }
    
    /**
     * <p>
     * Attempts to insert all the data columns within the <code>QueryData</code> message into 
     * the target set of correlated data.
     * </p>
     * <p>
     * The method creates a collection of data insertion tasks <code>RawDataInsertTask</code>
     * (one task for each <code>DataBucket</code> message within the argument)
     * then executes them concurrently using the instance thread pool executor.  
     * If a task fails its <code>DataBucket</code> subject is  identified and returned in the set of <code>BucketData</code> messages that were not 
     * successfully inserted into the target reference set. 
     * </p>
     * <p>
     * <h2>Concurrency</h2>
     * This method always invokes concurrency.  It manages a thread pool of independent tasks
     * as described above.
     * <p>
     * <h2>WARNINGS:</h2>
     * <ul>
     * <li>
     * This method does NOT modify the target set.  However, it requires a consistent target set
     * during its operation.  Thus, external synchronization is required.
     * </li>
     * <br/>
     * <li>
     * This method should be efficient for large target sets.  For an example in the contrary, 
     * if the target set is empty then all <code>DataBucket</code> messages with the argument 
     * are returned (after considerable computation).  
     * </li>
     * </ul>
     * </p>
     * 
     * @param msgData   Query Service response data to be inserted into the target set of references
     * 
     * @return  the collection of <code>DataBucket</code> messages that failed insertion
     * 
     * @throws CompletionException       error in <code>DataBucket</code> insertion task execution (see cause)
     * @throws RejectedExecutedException a data bucket insertion task was rejected
     */
    private Collection<QueryDataResponse.QueryData.DataBucket>  attemptDataInsertConcurrent(QueryDataResponse.QueryData msgData) 
            throws CompletionException, RejectedExecutionException
    {
        
        // Create the data insertion tasks
        Collection<RawDataInsertTask>   lstTasks = this.createInsertTasks(msgData);
        
        // Execute all tasks simultaneously then wait for completion or timeout
        try {
            this.exeInsertTasks.invokeAll(lstTasks, LNG_TIMEOUT, TU_TIMEOUT);

        } catch (InterruptedException e) {
            String strMsg = JavaRuntime.getQualifiedMethodName() + " - thread pool execution interrupted: " + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);

            throw new CompletionException(strMsg, e);
            
        } catch (RejectedExecutionException e) {
            String strMsg = JavaRuntime.getQualifiedMethodName() + " - thread pool executor rejected execution: " + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new CompletionException(strMsg, e);
        }

        // Collect all data buckets messages that were not processed and returned the data
//        Collection<QueryDataResponse.QueryData.DataBucket>  lstBucsFree = this.extractFailedTaskBuckets(lstTasks);
        List<QueryData.DataBucket> lstBucsFree = lstTasks
                .stream()
                .filter(task -> task.isSuccess()==false)
                .<QueryData.DataBucket>map(task -> task.getSubject())
                .toList();
        
        return lstBucsFree;
    }

    /**
     * <p>
     * Creates and returns a collection of data bucket insertion tasks for the given argument
     * and the current target set of correlated data.
     * </p>
     * <p>
     * A separate <code>{@link RawDataInsertTask}</code> object is create for each 
     * <code>{@link DataBucket}</code> message contained within the argument message.  
     * The target of each task is the current correlated data set <code>{@link #setPrcdData}</code>.  
     * Specifically, each task attempts, when activated, to insert its data bucket message into 
     * the current correlated data set.
     * If the task fails, its data bucket should be recovered and further processed in the
     * <code>{@link #processDisjointTargets(Collection)}</code> operation. 
     * <p>
     * <h2>NOTES:</h2>
     * This operation is currently serial.  If the bucket count of the argument is large parallelization may
     * be appropriate.
     * </p>
     * 
     * @param msgData   Query Service response data message
     * 
     * @return  collection of data insertion tasks for current target set, one for each data bucket of the argument
     */
    private Collection<RawDataInsertTask> createInsertTasks(QueryDataResponse.QueryData msgData) {
        
        List<RawDataInsertTask> lstTasks = msgData
                .getDataBucketsList()
                .stream()
                .<RawDataInsertTask>map(msgBucket -> RawDataInsertTask.from(msgBucket, this.setPrcdData))
                .toList();
                
        return lstTasks;
    }
    
//    /**
//     * <p>
//     * Parses the collection of executed raw-data insertion tasks for failed tasks collecting the
//     * <code>DataBucket</code> for further processing.
//     * </p>
//     * <p>
//     * <h2>NOTES:</h2>
//     * This operation is currently serial.  For large task collections parallelization may be appropriate.
//     * </p>
//     * 
//     * @param setTasks  collection of executed data-bucket insertion tasks
//     * 
//     * @return  collection of failed task subjects where raw data was not inserted 
//     */
//    private Collection<QueryDataResponse.QueryData.DataBucket>  extractFailedInsertions(Collection<RawDataInsertTask> setTasks) {
//        
//        List<QueryDataResponse.QueryData.DataBucket>  lstBuckets = setTasks
//                .stream()
//                .filter(task -> task.isSuccess()==false)
//                .<QueryData.DataBucket>map(t -> t.getSubject())
//                .toList();
//
//        return lstBuckets;
//    }
    
    /**
     * <p>
     * Correlates the given argument producing a separate <code>RawCorrelatedData</code>
     * sorted data, disjoint from the current managed target set.
     * </p>  
     * <p>
     * Builds and returns a disjoint set of <code>RawCorrelatedData</code> instances 
     * corresponding to the given argument collection of <code>DataBucket</code> messages.  
     * That is, the returned collection is a consistent set of correlated data processed from the 
     * given argument and sorted according to timestamp start times.  
     * </p>
     * <p>
     * The assumption is that the argument collection is not associated with the current managed 
     * target set returned by <code>{@link #getCorrelatedSet()}</code>.  More specifically, the 
     * arguments have already been processed against the current target set and we KNOW that new 
     * <code>RawCorrelatedData</code> instances must be created.
     * </p>
     * <p>
     * A new <code>{@link SortedSet}</code> of <code>CorrelatedQueryData</code> object is created.
     * An attempt is made to insert each <code>{@link DataBucket}</code> message within the argument 
     * into this collection of correlated data (clearly the first attempt will always fail).
     * If the insertion fails, a new <code>RawCorrelatedData</code> instance is created for the
     * message and the new instance is added to the sorted set.
     * </p> 
     * <p>  
     * If the previous assumption holds, then the set returned by this method are 
     * a consistent, processed data set and disjoint from the current managed target set.  
     * As such, the returned collection can then be safely added to the existing managed target set.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * <ul>
     * <li>
     * The operations here are self-contained and orthogonal to the consistency of the managed 
     * target set. Thus, everything here should be thread safe.
     * </li>
     * <br/>
     * <li>
     * All operations are performed serially, there is no internal multi-threading.
     * </li>
     * </ul>
     * </p>  
     * 
     * @param setBuckets    collection of <code>DataBucket</code> messages disjoint with the current managed target set
     * 
     * @return  set of new target references associated with the given argument data
     */
    private SortedSet<RawCorrelatedData>  processDisjointRawData(Collection<QueryDataResponse.QueryData.DataBucket> setBuckets) {
        
        // The returned sampling interval reference - that is, the targets
        SortedSet<RawCorrelatedData>  setRefs = new TreeSet<>(RawCorrelatedData.StartTimeComparator.create());
        
        // Treat each data bucket individually - high probability of modifying target set 
        for (QueryDataResponse.QueryData.DataBucket msgBucket : setBuckets) {

            // Attempt to insert bucket data into existing targets
            boolean bolSuccess = setRefs.stream().anyMatch(r -> r.insertBucketData(msgBucket));
            
            // If insertion failed then create a new sampling interval reference for targets
            if (!bolSuccess) {
                RawCorrelatedData   blkNew = RawCorrelatedData.from(msgBucket);
                
                setRefs.add(blkNew);
            }
        }
        
        return setRefs;
    }
    
    
}
