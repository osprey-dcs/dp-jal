/*
 * Project: dp-api-common
 * File:	QueryResponseCollector.java
 * Package: com.ospreydcs.dp.api.query.model.proto
 * Type: 	QueryResponseCollector
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
 * - None
 */
package com.ospreydcs.dp.api.query.model.proto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.query.model.DpQueryException;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

/**
 * <p>
 * Collects, organizes, and reduces the result set from a stream of <code>QueryResponse</code> Protobuf messages.
 * </p>
 * <p>
 * The intent is to organize the results of a Data Platform Query Service query according to the sampling
 * intervals.  That is, the collector parses the result sets for equivalent sampling intervals then associates
 * each data column of the result set to a <code>{@link SamplingIntervalRef</code> instance.  Once the query
 * result is fully processed there should be one <code>SamplingIntervalRef</code> instance for every unique
 * sampling interval within the result set.  Every data column within the result set will be associated with
 * one <code>SamplingIntervalRef</code> instance.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 11, 2024
 *
 * @see SamplingIntervalRef
 * @see BucketDataInsertTask
 */
public class QueryResponseCollector {

    //
    // Application Resources
    //
    
    
    
    //
    // Class Constants
    //
    
    /** Is logging active */
    public static final boolean    BOL_LOGGING = true;
    
    
    /** Parallelism timeout limit  - for parallel thread pool tasks */
    public static final long       LNG_TIMEOUT = 100;
    
    /** Parallelism timeout units - for parallel thread pool tasks */
    public static final TimeUnit   TU_TIMEOUT = TimeUnit.MILLISECONDS;
    
    
    /** Parallelism tuning parameter - pivot to parallel processing when target set size hits this limit */
    public static final int        SZ_TARGET_PIVOT = 10;
    
    
    //
    // Class Resources
    //
    
    /** Event logger */
    private final Logger    LOGGER = LogManager.getLogger();
    
    
    //
    // Instance Attributes
    //
    
    /** Target Set - Ordered set of sampling interval references */
    private final SortedSet<SamplingIntervalRef> setTargetRefs = new TreeSet<>(SamplingIntervalRef.StartTimeComparator.newInstance());

    
    //
    // Instance Resources
    //
    
    /** Synchronization lock for thread safety */
    private final Object            objLock = new Object();
    
    /** Manages thread pools of many, short-lived execution tasks */
    private final ExecutorService   exeThreadPool = Executors.newCachedThreadPool();
    
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryResponseCollector</code>.
     * </p>
     *
     */
    public QueryResponseCollector() {
    }
    
    
    //
    // Access and Control
    //
    
    /**
     * Returns the target set of <code>SamplingIntervalRef</code> instances in its current state.
     * 
     * @return  the target set of the query data collection and organization operations
     */
    public final SortedSet<SamplingIntervalRef>   getTargetSet() {
        return this.setTargetRefs;
    }
    
    /**
     * Returns the current size of the target set of <code>SamplingIntervalRef</code> instances.
     * 
     * @return  current size of the target set
     */
    public int sizeTargetSet() {
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
                        (set, r) -> set.addAll(r.extractDataSourceNames()), 
                        TreeSet::addAll
                        );
        return setNames;
    }
    
    /**
     * <p>
     * Clears out the target set of all sampling interval references.
     * </p>
     * <p>
     * <code>QueryResponseCollector</code> objects can be reused.  After calling this method the 
     * collector is returned to its initial state and is ready to process another Query Service
     * response stream.
     * </p>
     * 
     */
    public void clear() {
        this.setTargetRefs.clear();
    }
    
    
    // 
    // Operations
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
     * <h2>NOTES:</h2>
     * This method pivots from serial processing to parallel processing when the target set size
     * is greater than {@link #SZ_TARGET_PIVOT} = {@value #SZ_TARGET_PIVOT}.
     * </p>
     * 
     * @param msgBucket Query Service Protobuf message containing a query result data unit
     */
    public void    insertBucketData(QueryResponse.QueryReport.QueryData.DataBucket msgBucket) {

        // This operation must be atomic - potentially modifies the target set 
        synchronized (this.objLock) {

            boolean bolSuccess; // result of the data insertion attemp
            
            // Attempt to add the message data into the current set of sampling interval references
            
            if (this.setTargetRefs.size() < SZ_TARGET_PIVOT)
                bolSuccess = this.setTargetRefs
                    .stream()
                    .anyMatch( i -> i.insertBucketData(msgBucket) );
            else
                bolSuccess = this.setTargetRefs
                .parallelStream()
                .anyMatch( i -> i.insertBucketData(msgBucket) );

            // If the message data was not added we must create a new reference and add it to the current target set
            if (!bolSuccess) {
                SamplingIntervalRef refNew = SamplingIntervalRef.from(msgBucket);

                this.setTargetRefs.add(refNew);
            }
        }
    }
    
    /**
     * <p>
     * Inserts all data columns within the <code>QueryData</code> message into the current target set of 
     * sampling interval references, creating new references if needed.
     * </p>
     * <p>
     * Once this method returns all <code>DataBucket</code> messages within the argument are processed
     * and its data column is associated with some sampling interval reference within the target set.
     * </p>
     * <p>
     * The data processing technique pivots upon the size of the current target set.
     * For target set size less than <code>{@link #SZ_TARGET_PIVOT}</code> data buckets are processed
     * serially, as frequent additions to the target reference set are expected.  
     * For larger target sets the processing technique pivots to a parallel method.  An attempt is made to 
     * insert each data bucket within the argument concurrently into the target set.  
     * (The probability of insertion is higher since the target set is large.) 
     * Then a new reference set is created for the collection of data buckets that failed insertion.
     * The new reference set is inserted into the target set.
     * <p>
     * <p>
     * This is an atomic operation potentially modifying the target set. 
     * It synchronizes on the <code>{@link #objLock}</code> lock.
     * </p>
     *  
     * @param msgData   Query Service response data message
     */
    public void insertQueryData(QueryResponse.QueryReport.QueryData msgData) {
        
        // Check for empty data message
        if (msgData.getDataBucketsList().isEmpty()) {
            if (isLogging())
                LOGGER.warn("{}: attempt to insert data from empty data message.", JavaRuntime.getCallerName());
            
            return;
        }
        
        // This operation must be atomic - synchronized for thread safety
        synchronized (this.objLock) {
            
            // If the target set is small - insert all data at once
            if (this.setTargetRefs.size() < SZ_TARGET_PIVOT) {
                this.insertDataSerial(msgData);

                return;
            }

            // If the target set is large - pivot to concurrent processing of message data
            Collection<QueryResponse.QueryReport.QueryData.DataBucket>  setFreeBuckets = this.attemptDataInsertConcurrent(msgData);
            SortedSet<SamplingIntervalRef>  setNewTargets = this.buildTargetRefs(setFreeBuckets);
            this.setTargetRefs.addAll(setNewTargets);

        }
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Inserts all data columns within the <code>QueryData</code> message into the target set of 
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
    private void insertDataSerial(QueryResponse.QueryReport.QueryData msgData) {

        for (QueryResponse.QueryReport.QueryData.DataBucket msgBucket : msgData.getDataBucketsList()) {
            
            // Attempt to add the message data into the current set of sampling interval references
            boolean bolSuccess = this.setTargetRefs
                    .stream()
                    .anyMatch( i -> i.insertBucketData(msgBucket) );

            // If the message data was not added we must create a new reference and add it to the current target set
            if (!bolSuccess) 
                this.setTargetRefs.add( SamplingIntervalRef.from(msgBucket) );
            
        }
    }
    
    /**
     * <p>
     * Attempts to insert all the data columns within the <code>QueryData</code> message into 
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
     */
    private Collection<QueryResponse.QueryReport.QueryData.DataBucket>  attemptDataInsertConcurrent(QueryResponse.QueryReport.QueryData msgData) {
        
        // Create the data insertion tasks
        Collection<BucketDataInsertTask> lstTasks = this.createInsertionTasks(msgData);
//        List<BucketDataInsertTask> lstTasks = msgData
//                .getDataBucketsList()
//                .stream()
//                .map( buc -> BucketDataInsertTask.newTask(buc, this.getTargetRefs()) )
//                .toList();
                
        // Execute all tasks simultaneously then wait for join
        lstTasks
            .parallelStream()
            .forEach(t -> t.run());
        
        // Collect all data buckets messages that were not processed
        Collection<QueryResponse.QueryReport.QueryData.DataBucket>  lstBuckets = this.extractFailedTaskBuckets(lstTasks);
//        ArrayList<QueryResponse.QueryReport.QueryData.DataBucket>  lstBuckets = lstTasks
//                .stream()
//                .collect(
//                        ArrayList::new, 
//                        (c, t) -> { if (!t.isSuccess()) c.add(t.getSubject()); }, 
//                        ArrayList::addAll
//                        );
        
        return lstBuckets;
    }
    
    /**
     * <p>
     * Attempts to insert all the data columns within the <code>QueryData</code> message into 
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
     */
    @SuppressWarnings("unused")
    private Collection<QueryResponse.QueryReport.QueryData.DataBucket>  attemptDataInsertThreadPool(QueryResponse.QueryReport.QueryData msgData) 
            throws DpQueryException 
    {
        
//      // Create the thread pool executor for the data insertion tasks
//      ExecutorService exeThreadPool = Executors.newCachedThreadPool();
      
        // Create the data insertion tasks
        Collection<BucketDataInsertTask> lstTasks = this.createInsertionTasks(msgData);
//        List<BucketDataInsertTask> lstTasks = msgData
//                .getDataBucketsList()
//                .stream()
//                .map( buc -> BucketDataInsertTask.newTask(buc, this.getTargetRefs()) )
//                .toList();

        // Execute all tasks simultaneously then wait for completion or timeout
        try {
            this.exeThreadPool.invokeAll(lstTasks, LNG_TIMEOUT, TU_TIMEOUT);

        } catch (InterruptedException e) {
            String strMsg = JavaRuntime.getQualifiedCallerName() + ": thread pool execution interrupted with exception - " + e.getMessage();
            
            LOGGER.error(strMsg);
            
            throw new DpQueryException(strMsg);
            
        } catch (RejectedExecutionException e) {
            String strMsg = JavaRuntime.getQualifiedCallerName() + ": thread pool execution interrupted with exception - " + e.getMessage();
            
            LOGGER.error(strMsg);
            
            throw new DpQueryException(strMsg);
        }

        // Collect all data buckets messages that were not processed
        Collection<QueryResponse.QueryReport.QueryData.DataBucket>  lstBuckets = this.extractFailedTaskBuckets(lstTasks);
//        ArrayList<QueryResponse.QueryReport.QueryData.DataBucket>  lstBuckets = lstTasks
//                .stream()
//                .collect(
//                        ArrayList::new, 
//                        (c, t) -> { if (!t.isSuccess()) c.add(t.getSubject()); }, 
//                        ArrayList::addAll
//                        );

        return lstBuckets;
    }

    /**
     * <p>
     * Builds a set of target references for the collection of <code>DataBucket</code> messages.
     * </p>
     * <p>
     * The assumption is that the argument collection is not associated with the current managed set of 
     * target references returned by <code>{@link #getTargetSet()}</code>.  More specifically, the arguments
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
    private SortedSet<SamplingIntervalRef>  buildTargetRefs(Collection<QueryResponse.QueryReport.QueryData.DataBucket> setBuckets) {
        
        // The returned sampling interval reference - that is, the targets
        SortedSet<SamplingIntervalRef>  setRefs = new TreeSet<>(SamplingIntervalRef.StartTimeComparator.newInstance());
        
        // Treat each data bucket individually - high probability of modifying target set 
        for (QueryResponse.QueryReport.QueryData.DataBucket msgBucket : setBuckets) {

            // Attempt to insert bucket data into existing targets
            boolean bolSuccess = setRefs.stream().anyMatch(r -> r.insertBucketData(msgBucket));
            
            // If insertion failed then create a new sampling interval reference for targets
            if (!bolSuccess) 
                setRefs.add(SamplingIntervalRef.from(msgBucket));
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
    private Collection<BucketDataInsertTask> createInsertionTasks(QueryResponse.QueryReport.QueryData msgData) {
        
        List<BucketDataInsertTask> lstTasks = msgData
                .getDataBucketsList()
                .stream()
                .map(buc -> BucketDataInsertTask.newTask(buc, this.getTargetSet()))
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
    private Collection<QueryResponse.QueryReport.QueryData.DataBucket>  extractFailedTaskBuckets(Collection<BucketDataInsertTask> setTasks) {
        
        ArrayList<QueryResponse.QueryReport.QueryData.DataBucket>  lstBuckets = setTasks
                .stream()
                .collect(
                        ArrayList::new, 
                        (c, t) -> { if (!t.isSuccess()) c.add(t.getSubject()); }, 
                        ArrayList::addAll
                        );

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
