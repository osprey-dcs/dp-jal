/*
 * Project: dp-api-common
 * File:	RawDataInsertTask.java
 * Package: com.ospreydcs.dp.api.query.model.correl
 * Type: 	RawDataInsertTask
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

import java.util.SortedSet;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

/**
 * <p>
 * Performs the task of <code>DataBucket</code> insertion into a collection of <code>CorrelatedRawData</code> instances.
 * </p> 
 * <p>
 * Attempts to insert the <code>DataColumn</code> message of the initializing task subject 
 * <code>DataBucket</code> Protocol Buffers message into the initializing target collection of 
 * <code>CorrelatedRawData</code> instances.
 * Insertion attempts of the subject are all done serially on the target collection.
 * That is, this task DOES NOT spawn any sub-threads. 
 * </p>
 * <p>
 * The class implements both the <code>{@link Callable}</code> and <code>{@link Runnable}</code>
 * interfaces for thread execution.  The former simply defers to the <code>{@link #run()}</code>
 * method of the latter interface then returns the value of the <code>{@link #bolSuccess}</code>
 * attribute.
 * <p>
 * <h2>Usage:</h2>
 * <ul>
 * <li>
 * This task is assumed to be executed on a separate thread.
 * </li>
 * <li>
 * It is further assumed that multiple threads are executed for different subject messages and
 * the same target correlated set.
 * </li>
 * <li>
 * Use of this class is most appropriate for large target collections where multiple 
 * subject messages can be processed concurrently.
 * </li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 13, 2025
 *
 */
public class RawDataInsertTask implements Runnable, Callable<Boolean> {

    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new, initialized instances of <code>RawDataInsertTask</code> 
     * ready for execution.
     * </p>
     * 
     * @param msgSubject    the subject of the task - a Query Service data bucket message
     * @param setTarget     the target of the task - collection of <code>CorrelatedRawData</code> instances
     * 
     * @return  new, initialized thread task ready for execution
     */
    public static RawDataInsertTask from(QueryDataResponse.QueryData.DataBucket msgSubject, SortedSet<CorrelatedRawData> setTarget) {
        return new RawDataInsertTask(msgSubject, setTarget);
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
    private static final boolean    BOL_LOGGING = CFG_QUERY.logging.active;
    
    
    //
    // Class Resources
    //
    
    /** Event logger */
    private static final Logger    LOGGER = LogManager.getLogger();

    
    // 
    // Initialization Targets
    //
    
    /** The subject of this data insertion task */
    private final QueryDataResponse.QueryData.DataBucket    msgSubject;
    
    /** The object of this task - target collection of correlated data */
    private final SortedSet<CorrelatedRawData>              setTarget;
    
    
    //
    // State Variables
    //
    
    /** Has task been executed */
    private boolean bolExecuted = false;
    
    /** Was task successful */
    private boolean bolSuccess = false;
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>RawDataInsertTask</code>.
     * </p>
     *
     * @param msgSubject    the task subject - a Query Service data bucket message
     * @param setTarget     the task target - collection of <code>CorrelatedRawData</code> instances
     */
    public RawDataInsertTask(QueryDataResponse.QueryData.DataBucket msgSubject, SortedSet<CorrelatedRawData> setTarget) {
        this.msgSubject = msgSubject;
        this.setTarget = setTarget;
    }
    
    
    //
    // State Query
    //
    
    /**
     * Checks whether or not the task has been executed.
     * 
     * @return <code>true</code> if the thread task has been executed,
     *         <code>false</code> otherwise
     */
    public boolean  isExecuted() {
        return this.bolExecuted;
    }
    
    /**
     * Checks whether or not the task execution was successful.
     * <p>
     * Returns <code>true</code> if and only if
     * <ul>
     * <li>The task has been executed.</li>
     * <li>Data column of data bucket was successfully inserted into the collection of
     *     <code>CorrelatedRawData</code> instances.</li>
     * </ul>
     * 
     * @return  <code>true</code> if the task was successfully executed,
     *          <code>false</code> otherwise
     */
    public boolean  isSuccess() {
        return this.bolExecuted && this.bolSuccess;
    }
    
    /**
     * Returns the subject of this raw data insertion task.
     * 
     * @return  the <code>DataBucket</code> Protobuf message subject of task
     */
    public final QueryDataResponse.QueryData.DataBucket    getSubject() {
        return this.msgSubject;
    }
    
    /**
     * Returns the target of this raw data insertion task.
     * 
     * @return  the collection of <code>CorrelatedQueryData</code> instances 
     */
    public final SortedSet<CorrelatedRawData>    getTarget() {
        return this.setTarget;
    }

    
    //
    // Callable<Boolean> Interface
    //
    
    /**
     * <p>
     * Performs the data insertion task, presumably on a separate execution thread.
     * </p>
     * <p>
     * Attempts to add the data column of the subject data bucket message to the target set of 
     * sampling interval references.  If the task is successful the subject data bucket is considered
     * processed.  If not successful a new <code>CorrelatedQueryData</code> instance must be 
     * constructed for the sampling clock referenced in the bucket.
     * </p>
     * 
     * @return  the task instance <code>this</code> which can be used to recover the result and the subject message if necessary
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Boolean call() throws Exception {
        
        // Run the task and get result
        this.run();
        
        return this.isSuccess();
    }

    
    //
    // Runnable Interface
    //
    
    /**
     * <p>
     * Performs the data insertion task, presumably on an independent execution thread.
     * </p>
     * <p>
     * Attempts to add the data column of the subject data bucket message to the target set of 
     * sampling interval references.  If the task is successful the subject data bucket is considered
     * processed.  If not successful a new <code>CorrelatedQueryData</code> instance must be 
     * constructed for the sampling clock referenced in the bucket.
     * </p>
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        
        // Attempt bucket insertion for each <code>CorrelatedQueryData</code> instance and get result
        this.bolSuccess = this.setTarget
                .stream()
                .anyMatch(blk -> blk.insertBucketData(this.msgSubject));
        
        this.bolExecuted = true;
        
        // TODO - Remove
        if (!this.bolSuccess) {
            
            String strColName = this.msgSubject.getDataColumn().getName(); 
            
            if (BOL_LOGGING)
                LOGGER.debug("{} - A data bucket insertion task FAILED for source {}.", JavaRuntime.getQualifiedMethodNameSimple(), strColName);
        }
    }

}
