/*
 * Project: dp-api-common
 * File:	BucketDataInsertTask.java
 * Package: com.ospreydcs.dp.api.query.model.grpc
 * Type: 	BucketDataInsertTask
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
 * @since Jan 12, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.grpc;

import java.util.SortedSet;
import java.util.concurrent.Callable;

import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

/**
 * <p>
 * Performs the task of <code>DataBucket</code> insertion into a collection of 
 * <code>CorrelatedQueryData</code> instances.
 * </p> 
 * <p>
 * Attempts to insert all <code>DataColumn</code> messages of the subject 
 * <code>DataBucket</code> Protobuf message into a target collection of 
 * <code>CorrelatedQueryData</code> instances.
 * The task subject and target are provided at construction.
 * </p>
 * <p>
 * The class implements both the <code>{@link Callable}</code> and <code>{@link Runnable}</code>
 * interfaces for thread execution.  The former simply returns the class instance itself for
 * inspection and does not throw exceptions.  Both implementation perform the same function.
 * Insertion attempts of the subject are all done serially on the target collection.
 * That is, this task DOES NOT spawn any sub-threads. 
 * <p>
 * <h2>Usage:</h2>
 * <ul>
 * <li>
 * This task is assumed to be executed on a separate thread.
 * </li>
 * <br/>  
 * <li>
 * It is further assumed that multiple threads are executed for different subjects and
 * the same target.
 * </li>
 * <br/>
 * <li>
 * Use of this class is most appropriate for large target collections where multiple 
 * subjects can be processed concurrently.
 * </li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 12, 2024
 *
 */
public class BucketDataInsertTask implements Callable<BucketDataInsertTask>, Runnable {

    
    // 
    // Resources
    //
    
    /** The subject of this data insertion task */
    private final QueryDataResponse.QueryData.DataBucket    msgSubject;
    
    /** The object of this task - target collection of correlated data */
    private final SortedSet<CorrelatedQueryData>            setTarget;
    
    
    //
    // State Variables
    //
    
    /** Has task been executed */
    private boolean bolExecuted = false;
    
    /** Was task successful */
    private boolean bolSuccess = false;
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new, initialized instances of <code>BucketDataInsertTask</code> 
     * ready for execution.
     * </p>
     * 
     * @param msgSubject    the task subject - a QueryService data bucket message
     * @param setTarget     the data insertion target - collection of <code>CorrelatedQueryData</code> instances
     * 
     * @return  new, initialized thread task ready for execution
     */
    public static BucketDataInsertTask newTask(QueryDataResponse.QueryData.DataBucket msgSubject, SortedSet<CorrelatedQueryData> setTarget) {
        return new BucketDataInsertTask(msgSubject, setTarget);
    }

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>BucketDataInsertTask</code>.
     * </p>
     *
     * @param msgSubject    the task subject - a QueryService data bucket message
     * @param setTarget     the data insertion target - collection of <code>CorrelatedQueryData</code> instances
     */
    public BucketDataInsertTask(QueryDataResponse.QueryData.DataBucket msgSubject, SortedSet<CorrelatedQueryData> setTarget) {
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
     *     <code>CorrelatedQueryData</code> instances.</li>
     * </ul>
     * 
     * @return  <code>true</code> if the task was successfully executed,
     *          <code>false</code> otherwise
     */
    public boolean  isSuccess() {
        return this.bolSuccess;
    }
    
    /**
     * Returns the subject of this execution task.
     * 
     * @return  the <code>DataBucket</code> Protobuf message subject of task
     */
    public final QueryDataResponse.QueryData.DataBucket    getSubject() {
        return this.msgSubject;
    }
    
    /**
     * Returns the target of this execution task.
     * 
     * @return  the collection of <code>CorrelatedQueryData</code> instances 
     */
    public final SortedSet<CorrelatedQueryData>    getTarget() {
        return this.setTarget;
    }

    
    //
    // Callable Interface
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
    public BucketDataInsertTask call() throws Exception {
        
        // Run the task and get result
        this.run();
        
        return this;
    }


    //
    // Runnable Interface
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
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        
        // Attempt bucket insertion for each <code>CorrelatedQueryData</code> instance and get result
        this.bolSuccess = this.setTarget
                .stream()
                .anyMatch(cqd -> cqd.insertBucketData(this.msgSubject));
//                .<Boolean>map(tar -> tar.insertBucketData(this.msgSubject))
//                .anyMatch( r -> r );
        
        this.bolExecuted = true;
        
        // TODO - Remove
        if (!this.bolSuccess) {
            System.out.println("----------- " + JavaRuntime.getQualifiedMethodNameSimple() + "----------");
            System.out.println("  A Bucket Insertion FAILED.");
        }
    }

}
