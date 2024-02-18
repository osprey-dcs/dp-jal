/*
 * Project: dp-api-common
 * File:	QueryDataCorrelator.java
 * Package: com.ospreydcs.dp.api.query.model.proto
 * Type: 	QueryDataCorrelator
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
 * @since Jan 11, 2024
 *
 * TODO:
 * - See documentation
 */
package com.ospreydcs.dp.api.query.model.proto;

import java.util.ArrayList;
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

import javax.naming.CannotProceedException;
import javax.naming.OperationNotSupportedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.model.ResultRecord;
import com.ospreydcs.dp.api.model.TimeInterval;
import com.ospreydcs.dp.api.query.model.DpQueryException;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.RejectDetails;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

/**
 * <p>
 * Collects, organizes, and reduces the result set from a stream of <code>QueryResponse</code> Protobuf messages.
 * </p>
 * <p>
 * The intent is to organize the results of a Data Platform Query Service query according to the sampling
 * intervals.  That is, the collector parses the result sets for equivalent sampling intervals then associates
 * each data column of the result set to a <code>{@link CorrelatedQueryData}</code> instance.  Once the query
 * result is fully processed there should be one <code>CorrelatedQueryData</code> instance for every unique
 * sampling interval within the result set.  Every data column within the result set will be associated with
 * one <code>CorrelatedQueryData</code> instance.
 * </p>
 * <p>
 * <h2>Data Processing Verification</code>
 * There are multiple static utility methods for verifying correct processing of a results set.  These methods
 * are prefixed with <code>verify</code>.  They perform various verification checks on the processed results
 * set obtained from <code>{@link #getProcessedSet()}</code>, such as ordering, time domain collisions, and
 * time series sizes.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * The correlation operations performed within <code>QueryDataCorrelator</code>
 * allow a high-level of multi-threaded, concurrent, data processing.  Using concurrency
 * can significantly reduce processing time for a results set.
 * </li>
 * <br/>
 * <li>
 * The <code>QueryDataCorrelator</code> is currently designed to exploit all available 
 * CPU cores.  Thus, for some situations it may be desirable to stop concurrent processing
 * so it does not interfere with other real-time operations (e.g., such as gRPC streaming).
 * </li>
 * <br/>
 * <li>
 * Due to thread safety synchronization concurrency cannot be toggled until a correlation
 * operation (i.e., <code>insert...</code> method) has completed.
 * </li>
 * </ul>
 * </p>
 * <p>
 * <h2>TODO</h2>
 * <ul>
 * <li>
 * It may be beneficial to introduce a limit on the number of concurrent processing threads
 * so that concurrent processing does not interfere with real-time operations requiring
 * dedicated processor cores.
 * </li>
 * </ul>
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Jan 11, 2024
 *
 * @see CorrelatedQueryData
 * @see BucketDataInsertTask
 */
public class QueryDataCorrelator {

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
    public static final int        CNT_CONCURRENCY_THDS = CFG_QUERY.concurrency.threadCount;
    
    
    //
    // Class Resources
    //
    
    /** Event logger */
    private final Logger    LOGGER = LogManager.getLogger();
    
    
    //
    // Instance Resources
    //
    
    /** Synchronization lock for thread safety */
    private final Object            objLock = new Object();
    
    /** Manages thread pools of many, short-lived execution tasks */
//    private final ExecutorService   exeThreadPool = Executors.newCachedThreadPool();
    private final ExecutorService   exeThreadPool = Executors.newFixedThreadPool(CNT_CONCURRENCY_THDS);
    
    /** Target Set - Ordered set of sampling interval references */
    private final SortedSet<CorrelatedQueryData> setTargetRefs = new TreeSet<>(CorrelatedQueryData.StartTimeComparator.newInstance());

    
    //
    // State Variables
    //
    
    /** Toggle the use of concurrency in data processing */
    private boolean     bolConcurrency = BOL_CONCURRENCY;
    

    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new <code>QueryDataCorrelator</code> instance ready for processing.
     * </p>
     * <p>
     * Instances are created in their initial state - ready for processing.  The method 
     * <code>{@link #insertQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)}</code>
     * is recommended for query response data processing; that is, it is recommended to process all query and 
     * streaming errors externally, supplying only <code>BucketData</code> messages to 
     * <code>QueryDataCorrelator</code> objects.
     * </p>
     * <p>
     * One the entire query results set has be passed to a <code>QueryDataCorrelator</code> object
     * (e.g., using the above method repeated for each data message), the correlated data is recoverable
     * with <code>{@link #getProcessedSet()}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <code>QueryDataCorrelator</code> objects can be reused for multiple query results sets.
     * Use the method <code>{@link #reset()}</code> before processing a new results set.
     * </p>
     * 
     * @return
     * 
     * @see #insertQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)
     * @see #getProcessedSet()
     */
    public static QueryDataCorrelator newInstance() {
        return new QueryDataCorrelator();
    }
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryDataCorrelator</code>.
     * </p>
     * <p>
     * Instances are created in their initialized state and are ready for the processing of
     * Query Service results sets.
     * </p>
     */
    public QueryDataCorrelator() {
    }
    
    
    //
    // State Control
    //
    
    /**
     * <p>
     * Reset this correlator instance to its original (default) state.
     * </p>
     * <p>
     * This is a thread-safe operation.  Performs the following operations:
     * <ul>
     * <li>Clears out the target set of all <code>CorrelatedQueryData</code> references.</li>
     * <li>Returns the concurrency flag to its default state <code>{@link #BOL_CONCURRENCY}</code>.</li>
     * </ul>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <code>QueryDataCorrelator</code> objects can be reused.  After calling this method the 
     * collector is returned to its initial state and is ready to process another Query Service
     * response results set.
     * </p>
     * 
     */
    public void reset() {
        
        synchronized (this.objLock) {
            this.setTargetRefs.clear();
            this.bolConcurrency = BOL_CONCURRENCY;
        }
    }
    
    /**
     * <p>
     * Toggles ON/OFF the use of concurrency in data processing. 
     * </p>
     * <p>
     * This is a thread-safe operation and will not interrupt any ongoing processing.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The correlation operations performed within <code>QueryDataCorrelator</code>
     * allow a high-level of multi-threaded, concurrent, data processing.  Using concurrency
     * can significantly reduce processing time for a results set.
     * </li>
     * <br/>
     * <li>
     * The <code>QueryDataCorrelator</code> is currently designed to exploit all available 
     * CPU cores.  Thus, for some situations it may be desirable to stop concurrent processing
     * so it does not interfere with other real-time operations (e.g., such as gRPC streaming).
     * </li>
     * <br/>
     * <li>
     * Due to thread safety synchronization concurrency cannot be toggled until a correlation
     * operation (i.e., <code>insert...</code> method) has completed.
     * </li>
     * </ul>
     * </p>
     * 
     * @param bolUseConcurrency     set <code>true</code> to utilize concurrent processing, 
     *                              set <code>false</code> to turn off
     */
    public void setConcurrency(boolean bolUseConcurrency) {
        
        synchronized (this.objLock) {
            this.bolConcurrency = bolUseConcurrency;
        }
    }

    
    //
    // Attribute Query
    //
    
    /**
     * <p>
     * Returns the target set of <code>CorrelatedQueryData</code> instances in its current state.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This <code>QueryDataCorrelator</code> instance retains ownership of the returned set.
     * If the <code>{@link #reset()}</code> method is invoked this set is destroyed.
     * All subsequent processing of the returned data set must be completed before invoking
     * <code>{@link #reset()}</code>.
     * <p>
     * 
     * @return  the sorted set (by start-time instant) of currently processed data 
     */
    public final SortedSet<CorrelatedQueryData>   getProcessedSet() {
        return this.setTargetRefs;
    }
    
    /**
     * Returns the current size of the target set of <code>CorrelatedQueryData</code> instances.
     * 
     * @return  current size of the target set
     */
    public int sizeProcessedSet() {
        return this.setTargetRefs.size();
    }
    
    /**
     * <p>
     * Extracts and returns a set of unique data source names for all data within target set.
     * </p>
     * <p>
     * Collects all data source names within the target set of sampling interval references and
     * adds them to the returned set of unique names.
     * </p>
     * 
     * @return  set of all data source names within the Query Service response collection 
     */
    public Set<String>   extractDataSourceNames() {
        Set<String> setNames = this.setTargetRefs
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
     * Insert the column data from a single <code>DataBucket</code> message into the target set.
     * </p>
     * <p>
     * The sampling interval within the argument is compared against the current target set.  
     * <ul>
     * <li>If a match is found the data column within the argument is added to the sampling reference.</li>
     * <li>If there is no match a new sampling reference is created and added to the target set.</li>
     * </ul>
     * </p>
     * <p>
     * This is an atomic operation potentially modifying the target set. 
     * It synchronizing on the <code>{@link #objLock}</code> lock.
     * </p>
     * <p>
     * <h2>Concurrency</h2>
     * If concurrency is active (i.e., <code>{@link #BOL_CONCONCURRENCY}</code> = <code>true</code>),
     * this method pivots from serial processing to parallel processing when the target set size
     * is greater than {@link #SZ_CONCURRENCY_PIVOT} = {@value #SZ_CONCURRENCY_PIVOT}.
     * </p>
     * 
     * @param msgBucket Query Service Protobuf message containing a query result data unit
     */
    public void    insertBucketData(QueryResponse.QueryReport.BucketData.DataBucket msgBucket) {

        // This operation must be atomic - potentially modifies the target set 
        synchronized (this.objLock) {

            boolean bolSuccess; // result of the data insertion attemp
            
            // Attempt to add the message data into the current set of sampling interval references
            
            if (this.bolConcurrency && (this.setTargetRefs.size() > SZ_CONCURRENCY_PIVOT)) {
                bolSuccess = this.setTargetRefs
                        .parallelStream()
                        .anyMatch( i -> i.insertBucketData(msgBucket) );
                
            } else {
                bolSuccess = this.setTargetRefs
                        .stream()
                        .anyMatch( i -> i.insertBucketData(msgBucket) );

            }

            // If the message data was not added we must create a new reference and add it to the current target set
            if (!bolSuccess) {
                CorrelatedQueryData refNew = CorrelatedQueryData.from(msgBucket);

                this.setTargetRefs.add(refNew);
            }
        }
    }
    
    /**
     * <p>
     * Inserts a Query Service <code>BucketData</code> message obtained from a 
     * <code>QueryResponse</code> message, without error checking.
     * </p>
     * <p>
     * Inserts all data columns within the <code>BucketData</code> message into the current target set of 
     * correlated data references, creating new references if needed.  No error checking is
     * enforced.
     * </p>
     * <p>
     * Once this method returns all <code>DataBucket</code> messages within the argument are processed
     * and its data column is associated with some sampling interval reference within the target set.
     * </p>
     * <p>
     * <h2>Concurrency</h2>
     * The data processing technique pivots upon the size of the current target set.
     * For target set size less than <code>{@link #SZ_CONCURRENCY_PIVOT}</code> data buckets are processed
     * serially, as frequent additions to the target reference set are expected.  
     * For larger target sets the processing technique pivots to a parallel method.  An attempt is made to 
     * insert each data bucket within the argument concurrently into the target set.  
     * (The probability of insertion is higher since the target set is large.) 
     * Then a new reference set is created for the collection of data buckets that failed insertion.
     * The new reference set is inserted into the target set.
     * </p>
     * <p>
     * This is an atomic operation potentially modifying the target set. 
     * It synchronizes on the <code>{@link #objLock}</code> lock to maintain thread
     * safety.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This is the preferred method of data processing.  It processes all data within a 
     * <code>QueryResponse.QueryReport.BucketData</code> message without error checking.
     * </p>
     *  
     * @param msgData   Query Service response data message
     * 
     * @throws CompletionException  error in <code>DataBucket</code> insertion task execution (see cause)
     */
    public void insertQueryData(QueryResponse.QueryReport.BucketData msgData) throws CompletionException {
        
        // Check for empty data message
        if (msgData.getDataBucketsList().isEmpty()) {
            if (isLogging())
                LOGGER.warn("{}: attempt to insert data from empty data message.", JavaRuntime.getCallerName());
            
            return;
        }
        
        // This operation must be atomic - synchronized for thread safety
        synchronized (this.objLock) {
            
            // If the target set is small or concurrency is inactive - insert all data at once
            if (!this.bolConcurrency || (this.setTargetRefs.size() < SZ_CONCURRENCY_PIVOT)) {
                this.insertDataSerial(msgData);

                return;
            }

            // If the target set is large - pivot to concurrent processing of message data
//            Collection<QueryResponse.QueryReport.BucketData.DataBucket>  setFreeBuckets = this.attemptDataInsertConcurrent(msgData);
            Collection<QueryResponse.QueryReport.BucketData.DataBucket>  setFreeBuckets = this.attemptDataInsertThreadPool(msgData);
            SortedSet<CorrelatedQueryData>  setNewTargets = this.buildDisjointTargets(setFreeBuckets);
            this.setTargetRefs.addAll(setNewTargets);

        }
    }
    
    /**
     * <p>
     * Inserts the data from the given <code>QueryReport</code> message into the correlated data set,
     * performing response error checking.
     * </p>
     * <p>
     * The method extracts the <code>BucketData</code> message from the argument then defers 
     * processing to 
     * <code>{@link #insertQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)}</code>.
     * The argument is first checked for a rejected request by the Query Service and throws an
     * exception if so.  The argument data is then extracted and passed to the 
     * <code>insertQueryData()</code> method for data processing.
     * </p>
     * 
     * @param msgReport the <code>QueryReport</code> message within a <code>QueryResponse</code>
     * 
     * @throws CannotProceedException   the argument contained a query response error
     * 
     * @see #insertQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)
     */
    public void insertQueryReport(QueryResponse.QueryReport msgReport) throws CannotProceedException {
        
        // Check for response errors
        if (msgReport.hasQueryStatus()) {
            QueryResponse.QueryReport.QueryStatus msgStatus = msgReport.getQueryStatus();
            String strStatusMsg = msgStatus.getStatusMessage();
            QueryResponse.QueryReport.QueryStatus.QueryStatusType enmStatus = msgStatus.getQueryStatusType();
            
            if (isLogging()) 
                LOGGER.error("{}: Query Service response reported error with status={}, message={}", JavaRuntime.getCallerName(), enmStatus, strStatusMsg);
            
            throw new CannotProceedException("Query Service response reported error with status=" + enmStatus + " and message=" + strStatusMsg);
        }
        
        // Insert the query response data
        QueryResponse.QueryReport.BucketData msgData = msgReport.getBucketData();
        
        this.insertQueryData(msgData);
    }
    /**
     * <p>
     * Inserts all data from the given <code>QueryResponse</code> into the correlated data set,
     * performing all error checking.
     * </p>
     * <p>
     * The method extracts the <code>BucketData</code> message from the argument then defers 
     * further processing to 
     * <code>{@link #insertQueryReport(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport)}</code>.
     * The argument is first checked for a rejected request by the Query Service and throws an
     * exception if so.  It then extracts the <code>QueryResport</code> message and passes it
     * to the <code>insertQueryReport</code> method. 
     * </p>
     * 
     * @param msgRsp    the Query Service query response raw response message
     * 
     * @throws OperationNotSupportedException   the query request was rejected by the Query Service
     * @throws CannotProceedException           the argument contained a query response error
     * 
     * @see #insertQueryReport(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport)
     */
    public void insertQueryResponse(QueryResponse msgRsp) throws OperationNotSupportedException, CannotProceedException {
     
        // Check for rejected request
        if (msgRsp.hasQueryReject()) {
            RejectDetails msgReject = msgRsp.getQueryReject();
            String strRejectMsg = msgReject.getMessage();
            
            if (isLogging()) 
                LOGGER.error("{}: Query Service reported request rejection with message {}", JavaRuntime.getCallerName(), strRejectMsg);

            throw new OperationNotSupportedException("Query Service reported request rejection: " + strRejectMsg);
        }
        
        // Get the query report pass it for further processing
        QueryResponse.QueryReport   msgReport = msgRsp.getQueryReport();

        this.insertQueryReport(msgReport);
    }

    
    // 
    // Processing Verification
    //
    
    /**
     * <p>
     * Verifies the correct ordering (i.e., by sampling start time) of the given processed data set.
     * </p>
     * <p>
     * Loops through the sorted set of <code>CorrelatedQueryData</code> instances checking all adjacent
     * instances for proper ordering.  If a mis-ordered instance is found all further checking stops
     * and a FAILURE result is returned with a message describing the index.
     * </p>
     * 
     * @param setProcData   the ordered set of processed data produced by a <code>QueryDataCorrelator</code>
     * 
     * @return  <code>{@link ResultRecord#SUCCESS}</code> if the set is properly ordered,
     *          otherwise a FAILURE result with description message
     */
    public static ResultRecord    verifyOrdering(SortedSet<CorrelatedQueryData> setProcData) {
        
        // We need at least one data set
        if (setProcData.isEmpty())
            return ResultRecord.newFailure("Empty argument - cannot verify empty data set.");
        
        // Loop through all processed data in order
        int                 indPrev = 0;
        CorrelatedQueryData cqdPrev = null;
        
        for (CorrelatedQueryData cqdCurr : setProcData) {

            // Initiate loop
            if (cqdPrev == null) {
                cqdPrev = cqdCurr;
                continue;
            }
            
            if (cqdPrev.compareTo(cqdCurr) >= 0) {
                return ResultRecord.newFailure("Bad ordering found at index " + Integer.toString(indPrev));
            }
            
            cqdPrev = cqdCurr;
            indPrev++;
        }
        
        return ResultRecord.SUCCESS;
    }
    
    /**
     * <p>
     * Verifies that each data set within the argument has the correct column size.
     * </p>
     * <p>
     * Loops through all processed data.  The sample count for each data set is taken from
     * the sampling clock message.  All <code>DataColumn</code> messages within the correlated
     * set are then checked for the proper size (i.e., the sample count).  If a correlated data
     * object is found to have data columns with incorrect size all further processing stops
     * and a FAILURE result is returned with a message describing the bad columns.
     * </p>
     * 
     * @param setProcData   the ordered set of processed data produced by a <code>QueryDataCorrelator</code>
     * 
     * @return  <code>{@link ResultRecord#SUCCESS}</code> if all column sizes are correct,
     *          otherwise a FAILURE result with description message
     */
    public static ResultRecord    verifyColumnSizes(SortedSet<CorrelatedQueryData> setProcData) {

        // We need at least one data set
        if (setProcData.isEmpty())
            return ResultRecord.newFailure("Empty argument - cannot verify empty data set.");
        
        // Loop through all processed data in order
        int     indCurr = 0;
        for (CorrelatedQueryData cqdCurr : setProcData) {

            // Get the sample count for each set
            final int cntSamples = cqdCurr.getSamplingMessage().getNumSamples();

            // Filter all processed data columns with size unequal to sample count
            List<DataColumn>    lstBadCols = cqdCurr
                    .getAllDataMessages()
                    .stream()
                    .filter(msgCol -> (msgCol.getDataValuesCount() != cntSamples) )
                    .toList();
            
            if (!lstBadCols.isEmpty()) {
                List<String>    lstBadSrcNms = lstBadCols.stream().map(DataColumn::getName).toList();
                
                return ResultRecord.newFailure("Bad column size(s) for data set index " 
                        + Integer.toString(indCurr)
                        + ": " + lstBadSrcNms);
            }
            
            indCurr++;
        }
        
        return ResultRecord.SUCCESS;
    }
    
    /**
     * <p>
     * Verifies that all time domains within the given processed data set are disjiont.
     * </p>
     * <p>
     * Checks all adjacent time domains within the sorted set of processed data for closed intersections.
     * If an intersection is detected all further checking stops and a FAILURE results is returned along
     * with a message containing the offending element index.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This verification must be run after <code>{@link #verifyOrdering(SortedSet)}</code>.  The algorithm
     * is accurate ONLY IF the set is ordered correctly, specifically, by sampling start times.
     * </p>
     * 
     * @param setProcData   the ordered set of processed data produced by a <code>QueryDataCorrelator</code>
     * 
     * @return  <code>{@link ResultRecord#SUCCESS}</code> if no time domain collisions are detected,
     *          otherwise a FAILURE result with description message
     */
    public static ResultRecord    verifyTimeDomains(SortedSet<CorrelatedQueryData> setProcData) {
        
        // We need at least one data set
        if (setProcData.isEmpty())
            return ResultRecord.newFailure("Empty argument - cannot verify empty data set.");
        
        // Loop through all processed data in order
        int                 indPrev = 0;
        CorrelatedQueryData cqdPrev = null;
        
        for (CorrelatedQueryData cqdCurr : setProcData) {

            // Initiate loop
            if (cqdPrev == null) {
                cqdPrev = cqdCurr;
                continue;
            }

            TimeInterval    domPrev = cqdPrev.getTimeDomain();
            TimeInterval    domCurr = cqdCurr.getTimeDomain();
            
            if (domPrev.hasIntersectionClosed(domCurr)) {
                return ResultRecord.newFailure("Time domain intersect found at index " + Integer.toString(indPrev));
            }
            
            cqdPrev = cqdCurr;
            indPrev++;
        }
        
        return ResultRecord.SUCCESS;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Inserts all data columns within the <code>BucketData</code> message into the target set of 
     * interval references, creating new target references if necessary.
     * </p>
     * <p>
     * This method processes each <code>DataBucket</code> message within the argument serially.
     * An attempt is made to insert its data column into the existing target set of references.
     * If the attempt fails a new reference is created, the message is associated, then the reference
     * is added to the target set.
     * </p>
     * <p>
     * This method leaves the overall collection consistent.  All the <code>DataBucket</code> messages within
     * the argument are processed upon completion. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method should be efficient for small target sets where the probability for new reference creation
     * is high.  All operations are serial.
     * </li>
     * <br/>
     * <li>
     * This method is not thread safe.  The method modifies the target set of references.  The target set 
     * must not be modified during method call and should be synchronized externally.
     * </li>
     * </ul>
     * </p>
     * 
     * @param msgData   Query Service data message to be processed into this collection
     */
    private void insertDataSerial(QueryResponse.QueryReport.BucketData msgData) {

        for (QueryResponse.QueryReport.BucketData.DataBucket msgBucket : msgData.getDataBucketsList()) {
            
            // Attempt to add the message data into the current set of sampling interval references
            boolean bolSuccess = this.setTargetRefs
                    .stream()
                    .anyMatch( i -> i.insertBucketData(msgBucket) );

            // If the message data was not added we must create a new reference and add it to the current target set
            if (!bolSuccess) 
                this.setTargetRefs.add( CorrelatedQueryData.from(msgBucket) );
            
        }
    }
    
    /**
     * <p>
     * Attempts to insert all the data columns within the <code>BucketData</code> message into 
     * the target set of interval references.
     * </p>
     * <p>
     * The method creates a collection of data insertion tasks <code>BucketDataInsertTask</code>
     * (one task for each <code>DataBucket</code> message within the argument)
     * then executes them concurrently.  If a task fails its <code>DataBucket</code> subject is 
     * identified and returned in the set of <code>BucketData</code> messages that were not 
     * successfully inserted into the target reference set. 
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * <ul>
     * <li>
     * This method does NOT modify the target set.  However, it requires a consistent target set
     * during its operation.  Thus, external synchronization is advised.
     * </li>
     * <br/>
     * <li>
     * This method should be efficient for large target sets.  For an example in the contrary, 
     * if the target set is empty then all <code>DataBucket</code> messages with the argument are returned 
     * (after considerable computation).  
     * </li>
     * </ul>
     * </p>
     * 
     * @param msgData   Query Service response data to be inserted into the target set of references
     * 
     * @return  the collection of <code>DataBucket</code> messages that failed insertion
     * 
     * @Deprecated  Unstable for large target sets - seems to be Java internal TreeMap problem
     */
    @Deprecated(since="Feb 17, 2024")
    private Collection<QueryResponse.QueryReport.BucketData.DataBucket>  attemptDataInsertConcurrent(QueryResponse.QueryReport.BucketData msgData) {
        
        // Create the data insertion tasks
        Collection<BucketDataInsertTask> lstTasks = this.createInsertionTasks(msgData);

        // Execute all tasks simultaneously then wait for join
        lstTasks
            .parallelStream()
            .forEach(t -> t.run());
        
        // Collect all data buckets messages that were not processed
        Collection<QueryResponse.QueryReport.BucketData.DataBucket>  lstBuckets = this.extractFailedTaskBuckets(lstTasks);
        
        return lstBuckets;
    }
    
    /**
     * <p>
     * Attempts to insert all the data columns within the <code>BucketData</code> message into 
     * the target set of interval references.
     * </p>
     * <p>
     * The method creates a collection of data insertion tasks <code>BucketDataInsertTask</code>
     * (one task for each <code>DataBucket</code> message within the argument)
     * then executes them concurrently using the instance thread pool executor.  
     * If a task fails its <code>DataBucket</code> subject is 
     * identified and returned in the set of <code>BucketData</code> messages that were not 
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
     * during its operation.  Thus, external synchronization is advised.
     * </li>
     * <br/>
     * <li>
     * This method should be efficient for large target sets.  For an example in the contrary, 
     * if the target set is empty then all <code>DataBucket</code> messages with the argument are returned 
     * (after considerable computation).  
     * </li>
     * </ul>
     * </p>
     * 
     * @param msgData   Query Service response data to be inserted into the target set of references
     * 
     * @return  the collection of <code>DataBucket</code> messages that failed insertion
     * 
     * @throws CompletionException  error in <code>DataBucket</code> insertion task execution (see cause)
     */
    private Collection<QueryResponse.QueryReport.BucketData.DataBucket>  attemptDataInsertThreadPool(QueryResponse.QueryReport.BucketData msgData) 
//            throws DpQueryException
            throws CompletionException
    {
        
        // Create the data insertion tasks
        Collection<BucketDataInsertTask> lstTasks = this.createInsertionTasks(msgData);

        // Execute all tasks simultaneously then wait for completion or timeout
        try {
            this.exeThreadPool.invokeAll(lstTasks, LNG_TIMEOUT, TU_TIMEOUT);

        } catch (InterruptedException e) {
            String strMsg = JavaRuntime.getQualifiedCallerName() + ": thread pool execution interrupted - " + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);

            throw new CompletionException(strMsg, e);
            
        } catch (RejectedExecutionException e) {
            String strMsg = JavaRuntime.getQualifiedCallerName() + ": thread pool executor rejected execution - " + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new CompletionException(strMsg, e);
        }

        // Collect all data buckets messages that were not processed
        Collection<QueryResponse.QueryReport.BucketData.DataBucket>  lstBuckets = this.extractFailedTaskBuckets(lstTasks);

        return lstBuckets;
    }

    /**
     * <p>
     * Builds a set of target references for the collection of <code>DataBucket</code> messages.
     * </p>
     * <p>
     * The assumption is that the argument collection is not associated with the current managed set of 
     * target references returned by <code>{@link #getProcessedSet()}</code>.  More specifically, the arguments
     * have already been checked against the current target set and we know that new sampling interval references 
     * must be created.
     * </p>
     * <p>  
     * If the above assumption holds, then the references returned by this method are consistent, disjoint
     * from the current managed target set, and associated with the argument data.  As such, the returned
     * collection can then be safely added to the existing managed target set.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * The operations here are self-contained and orthogonal to the consistency of the managed target set.
     * Thus, everything here should be thread safe.
     * </p>  
     * 
     * @param setBuckets    collection of <code>DataBucket</code> messages disjoint with the current managed target set
     * 
     * @return  set of new target references associated with the given argument data
     */
    private SortedSet<CorrelatedQueryData>  buildDisjointTargets(Collection<QueryResponse.QueryReport.BucketData.DataBucket> setBuckets) {
        
        // The returned sampling interval reference - that is, the targets
        SortedSet<CorrelatedQueryData>  setRefs = new TreeSet<>(CorrelatedQueryData.StartTimeComparator.newInstance());
        
        // Treat each data bucket individually - high probability of modifying target set 
        for (QueryResponse.QueryReport.BucketData.DataBucket msgBucket : setBuckets) {

            // Attempt to insert bucket data into existing targets
            boolean bolSuccess = setRefs.stream().anyMatch(r -> r.insertBucketData(msgBucket));
            
            // If insertion failed then create a new sampling interval reference for targets
            if (!bolSuccess) 
                setRefs.add(CorrelatedQueryData.from(msgBucket));
        }
        
        return setRefs;
    }
    
    /**
     * <p>
     * Creates a collection of data bucket insertion tasks for the given argument and current target set of 
     * references.
     * </p>
     * <p>
     * This operation is currently serial.  If the bucket count of the argument is large parallelization may
     * be appropriate.
     * </p>
     * 
     * @param msgData   Query Service response data message
     * 
     * @return  collection of data insertion tasks for current target set, one for each data bucket of the argument
     */
    private Collection<BucketDataInsertTask> createInsertionTasks(QueryResponse.QueryReport.BucketData msgData) {
        
        List<BucketDataInsertTask> lstTasks = msgData
                .getDataBucketsList()
                .stream()
                .map(buc -> BucketDataInsertTask.newTask(buc, this.setTargetRefs))
                .toList();
                
        return lstTasks;
    }
    
    /**
     * <p>
     * Parses the collection of executed bucket-data insertion tasks for failed tasks collecting the
     * <code>DataBucket</code> subjects.
     * </p>
     * <p>
     * This operation is currently serial.  For large task collections parallelization may be appropriate.
     * </p>
     * 
     * @param setTasks  collection of executed data-bucket insertion tasks
     * 
     * @return  collection of task subjects where task execution failed
     */
    private Collection<QueryResponse.QueryReport.BucketData.DataBucket>  extractFailedTaskBuckets(Collection<BucketDataInsertTask> setTasks) {
        
        List<QueryResponse.QueryReport.BucketData.DataBucket>  lstBuckets = setTasks
                .stream()
                .filter(t -> !t.isSuccess())
                .map(t -> t.getSubject())
                .toList();
//                .stream()
//                .collect(
//                        ArrayList::new, 
//                        (c, t) -> { if (!t.isSuccess()) c.add(t.getSubject()); }, 
//                        ArrayList::addAll
//                        );

        return lstBuckets;
    }
    

    //
    // Class Support
    //
    
    /**
     * Returns whether or not class logging is active.
     * 
     * @return  <code>true</code> if logging is active, <code>false</code> otherwise
     */
    private static boolean isLogging() {
        return BOL_LOGGING;
    }
}
