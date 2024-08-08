/*
 * Project: dp-api-common
 * File:    MessageSupplierTask.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type:    MessageSupplierTask
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
 * @since Aug 7, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.grpc;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import com.ospreydcs.dp.api.ingest.model.IResourceConsumer;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * Producer task that supplies <code>IngestDataRequest</code> messages to a 
 * <code>IResourceConsumer&lt;IngestDataRequest&gt;</code> implementation.
 * </p>
 * <p>
 * The task is meant to be run periodically within an executor service.
 * At each time of invocation the task submits a single message from the payload offered to it at construction.
 * </p>
 * <p>  
 * Supplier task maintains an internal state which is modified at each invocation by the executor service.  
 * By maintaining a reference to the task instance, state variable quantities are available with getter methods.
 * </p>
 * <p>
 * <h2>NOTE</h2>
 * The supplier task shuts down the message buffer when all messages have been supplied.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 7, 2024
 *
 */
public final class MessageSupplierTask implements Runnable, Callable<Integer> {

    
    //
    // Defining Attributes
    //
    
    /** Collection of ingest data request messages to supply to buffer */
    private final List<IngestDataRequest>               lstMsgsPayload;
    
    /** Message message buffer to receive payload of ingest data request messages */
    private final IResourceConsumer<IngestDataRequest>  bufTarget;
    
    
    //
    // Resources
    //
    
    /** Iterator for the payload of request data messages */
    private final Iterator<IngestDataRequest>   itrMsgs;

    
    //
    // State Variables
    //
    
    /** Current number of messages supplied to the buffer */
    private int         cntMsgsSupplied = 0;
    
    /** Current memory allocation of messages supplied to the buffer */
    private long        szAllocSupplied = 0;
    
    
    /** Had the task fully completed - all messages have been supplied */
    private boolean     bolCompleted = false;
    
    /** Error occurred during message transfer */
    private boolean     bolError = false;
    
    /** Exception thrown by enqueue() if any */ 
    private Exception   excError = null;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>MessageSupplierTask</code> initialized with the given parameters.
     * </p>
     *
     * @param lstMsgs   payload of message to be supplied to the buffer
     * @param bufTarget target buffer to receive message payload
     */
    public MessageSupplierTask(List<IngestDataRequest> lstMsgs, IResourceConsumer<IngestDataRequest> bufTarget) {
        this.lstMsgsPayload = lstMsgs;
        this.bufTarget = bufTarget;
        
        this.itrMsgs = lstMsgsPayload.iterator();
    }
    
    
    //
    // State Inquiry
    //
    
    /** @return <code>true</code> if task completed normally, <code>false</code> otherwise */
    public boolean hasCompleted()   { return this.bolCompleted; };

    /** @return <code>true</code> if task encountered error, <code>false</code> if error free*/
    public boolean hasError()       { return this.bolError; };
    
    
    /** @return the number of messages supplied to the buffer so far */
    public int          getSuppliedMessageCount()   { return this.cntMsgsSupplied; };
    
    /** @return total memory allocation of all supplied messages so far */
    public long         getSuppliedMessageAlloc()    { return this.szAllocSupplied; };
    
    /** @return any exception encountered during execution (see {@link #hasError()}) */
    public Exception    getEnqueueException()       { return this.excError; };
    
    
    //
    // Runnable Interface
    //
    
    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        
        // If already completed or error skip execution
        if (this.bolCompleted || this.bolError)
            return;
        
        // If there are available messages supply one at a time
        if (this.itrMsgs.hasNext()) {
            try {
                IngestDataRequest msgRqst = this.itrMsgs.next();

                this.bufTarget.offer(msgRqst);
                this.cntMsgsSupplied++;

            } catch (Exception e) {
                this.bolError = true;
                this.excError = e;
            }
            
        // Else the supply is exhaused - shut down the buffer
        } else {
            try {
                this.bolCompleted = true;
                this.bufTarget.shutdown();  // blocking, throws exception
                
            } catch (Exception e) {
                this.bolError = true;
                this.excError = e;
            }
        }
    }

    //
    // Callable<Integer> Interface
    //
    
    /**
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Integer call() throws Exception {
        
        this.run();
        
        return this.cntMsgsSupplied;
    }
}