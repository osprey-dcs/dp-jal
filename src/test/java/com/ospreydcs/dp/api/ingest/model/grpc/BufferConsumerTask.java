/*
 * Project: dp-api-common
 * File:    BufferConsumerTask.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type:    BufferConsumerTask
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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.ingest.model.IMessageSupplier;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * Consumer task that retrieves <code>IngestDataRequest</code> messages from an 
 * <code>IMessageSupplier&lt;IngestDataRequst&gt;</code> interface implementation.
 * </p>
 * <p>
 * The task is meant to be run periodically within an executor service.    
 * At each time of invocation the task retrieves a single message from the buffer offered to it at construction.
 * </p>
 * <p>  
 * Consumer task maintains an internal state which is modified at each invocation by the executor service.  
 * By maintaining a reference to the task instance, state variable quantities are available with getter methods.
 * </p>
 * <p>
 * <h2>NOTE</h2>
 * The consumer simply sets the <code>{@link #bolCompleted}</code> to <code>true</code> when the buffer stops
 * supplying (no buffer state change is performed).
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 7, 2024
 *
 */
public final class BufferConsumerTask implements Runnable, Callable<Integer> {

    //
    // Internal Types
    //
    
    /**
     * Enumeration of buffer operation for consuming ingest data request messages.
     */
    public enum BufferOperation {
        
        /** Use blocking <code>{@link IMessageSupplier#take()}</code> */
        TAKE,
        
        /** Use non-blocking <code>{@link IMessageSupplier#poll()}</code> */
        POLL_NONBLCK,
        
        /** Use blocking <code>{@link IMessageSupplier#poll(long, TimeUnit)}</code> */
        POLL_TMOUT;
    }
    
    
    //
    // Defining Attributes
    //
    
    /** Message message buffer to supplying ingest data request messages */
    private final IMessageSupplier<IngestDataRequest>   bufSource;
    
    /** Consumption operation used to retrieve ingest data request message */
    private final BufferConsumerTask.BufferOperation    enmOperation;
    
    
    //
    // Resources
    //
    
    /** Collection of ingest data request messages to supply to buffer */
    private final List<IngestDataRequest>       lstMsgsConsumed = new LinkedList<>();
    
    
    //
    // State Variables
    //
    
    /** Current number of message consumed from the buffer */
    private int         cntMsgsConsumed = 0;
    
    /** Current memory allocation of consumed messages */
    private long        szAllocConsumed = 0;
    
    
    /** Had the task fully completed - all messages have been consumed */
    private boolean     bolCompleted = false;
    
    /** Error occurred during message transfer */
    private boolean     bolError = false;
    
    /** Exception thrown by message transfer if any */ 
    private Exception   excError = null;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>BufferConsumerTask</code>.
     * </p>
     *
     * @param bufSource     the source of the ingest data message
     * @param enmOperation  the buffer operation used to retrieve messages
     */
    public BufferConsumerTask(IMessageSupplier<IngestDataRequest> bufSource, BufferConsumerTask.BufferOperation enmOperation) {
        this.bufSource = bufSource;
        this.enmOperation = enmOperation;
    }

    
    //
    // State Inquiry
    //
    
    /** @return <code>true</code> if task completed normally, <code>false</code> otherwise */
    public boolean  hasCompleted()      { return this.bolCompleted; };
    
    /** @return <code>true</code> if task encountered error, <code>false</code> if error free*/
    public boolean  hasError()          { return this.bolError; };
    
    
    /** @return number of request data message consumed so far */
    public int          getConsumedMessageCount()   { return this.cntMsgsConsumed; };
    
    /** @return the memory allocation of consumed messages */
    public long         getConsumedMessageAlloc()   { return this.szAllocConsumed; };
    
    /** @return any exceptions thrown during execution (see {@link #hasError()}) */
    public Exception    getRetrieveException()      { return this.excError; };
    
    
    //
    // Runnable Interface
    //
    
    /**
     * @see @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        
        if (this.bolCompleted || this.bolError)
            return;
        
        if (this.bufSource.isSupplying()) {
            try {
                IngestDataRequest   msgRqst =
                        switch (this.enmOperation) {
                        case TAKE -> this.bufSource.take();
                        case POLL_NONBLCK -> this.bufSource.poll();
                        case POLL_TMOUT -> this.bufSource.poll(IngestionMessageBufferTest.LNG_PERD_CONS_TASK, IngestionMessageBufferTest.TU_PERD_CONS_TASK);
                        };

                if (msgRqst != null) {
                    this.lstMsgsConsumed.add(msgRqst);
                    this.cntMsgsConsumed++;
                    this.szAllocConsumed += msgRqst.getSerializedSize();
                }

            } catch (Exception e) {
                this.bolError = true;
                this.excError = e;
            }
        }
//        } else {
//            this.bolCompleted = true;
//        }
        
        // Check if this is the last message
        if (!this.bufSource.isSupplying())
            this.bolCompleted = true;
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
        
        return this.cntMsgsConsumed;
    }
}