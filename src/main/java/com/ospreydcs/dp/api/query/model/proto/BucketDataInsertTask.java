/*
 * Project: dp-api-common
 * File:	BucketDataInsertTask.java
 * Package: com.ospreydcs.dp.api.query.model.proto
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
package com.ospreydcs.dp.api.query.model.proto;

import java.util.Collection;
import java.util.concurrent.Callable;

import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

/**
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
    private final QueryResponse.QueryReport.QueryData.DataBucket    msgSubject;
    
    /** The target collection of existing sampling intervals */
    private final Collection<SamplingIntervalRef>                   setTarget;
    
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
    
    public static BucketDataInsertTask newTask(QueryResponse.QueryReport.QueryData.DataBucket msgSubject, final Collection<SamplingIntervalRef> setTarget) {
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
     * @param setTarget     the data insertion target - an collection of <code>SamplingIntervalRef</code> instances
     */
    public BucketDataInsertTask(QueryResponse.QueryReport.QueryData.DataBucket msgSubject, final Collection<SamplingIntervalRef> setTarget) {
        this.msgSubject = msgSubject;
        this.setTarget = setTarget;
    }
    
    //
    // State Query
    //
    
    public boolean  isExecuted() {
        return this.bolExecuted;
    }
    
    public boolean  isSuccess() {
        return this.bolSuccess;
    }
    
    public final QueryResponse.QueryReport.QueryData.DataBucket    getSubject() {
        return this.msgSubject;
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
     * processed.  If not successful a new <code>SamplingIntervalRef</code> instance must be constructed
     * for the sampling interval referenced in the bucket.
     * </p>
     * 
     * @return  the task instance <code>this</code> which can be used to recover the result and the subject message if necessary
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public BucketDataInsertTask call() throws Exception {
        
        // Run the task and get result
        this.bolSuccess = this.setTarget
                .stream()
                .map(tar -> tar.addBucketData(this.msgSubject))
                .anyMatch( r -> r );
        
        this.bolExecuted = true;
        
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
     * processed.  If not successful a new <code>SamplingIntervalRef</code> instance must be constructed
     * for the sampling interval referenced in the bucket.
     * </p>
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        
        this.bolSuccess = this.setTarget.stream().map(tar -> tar.addBucketData(this.msgSubject)).anyMatch( r -> r );
        this.bolExecuted = true;
    }

}
