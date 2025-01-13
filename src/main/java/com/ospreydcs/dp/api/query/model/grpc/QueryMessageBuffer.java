/*
 * Project: dp-api-common
 * File:	QueryMessageBuffer.java
 * Package: com.ospreydcs.dp.api.query.model.grpc
 * Type: 	QueryMessageBuffer
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
 * @since Jan 12, 2025
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.grpc;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.model.IMessageConsumer;
import com.ospreydcs.dp.api.model.IMessageSupplier;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

/**
 * <p>
 * Class implementing a queue buffer for Query Service <code>QueryDataResponse.QueryData</code> messages.
 * </p>
 * <p>
 * Class instances are a consumer of <code>QueryDataResponse.QueryData</code> messages, presumably from a gRPC
 * data stream recovering a time-series data request from the Query Service.  Instances collect these
 * messages then manage their retrieval as a supplier of <code>QueryDataResponse.QueryData</code> messages.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Jan 12, 2025
 *
 */
public class QueryMessageBuffer implements IMessageConsumer<QueryData>, IMessageSupplier<QueryData> {

    
    //
    // Application Resources
    //
    
    /** The Data Platform Query Service default parameters */
    private static final DpQueryConfig CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants - Initialized from API configuration
    //
    
    /** Is logging active? */
    public static final boolean     BOL_LOGGING = CFG_QUERY.logging.active;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    //
    // Instance Resources
    //
    
    /** The queue buffering all response messages for data correlation processing */
    private final BlockingQueue<QueryData>  queMsgBuffer = new LinkedBlockingQueue<>();

    
    /** Synchronization lock for memory allocation updates */
    private final Object            objLock = new Object();
    
    
    /** The message queue buffer empty lock */
    private final Lock              lckMsgQueEmpty = new ReentrantLock();
    
    /** The message queue buffer empty lock condition */
    private final Condition         cndMsgQueEmpty = lckMsgQueEmpty.newCondition();
    

    //
    // State Variables
    //
    
    /** Activated flag */
    private boolean     bolActive = false;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryMessageBuffer</code>.
     * </p>
     *
     */
    public QueryMessageBuffer() {
    }


    //
    // Operations
    //
    
    /**
     * <p>
     * Allows clients to block until the message queue buffer completely empties.
     * </p>
     * <p>
     * This method allows clients to wait for the <code>QueryData</code> queue buffer
     * to fully empty.  Clients can add a fixed number of messages then measure 
     * the time for messages to be consumed (with known memory allocation).  This activity may be useful
     * when clients wish to due their own performance tuning.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method will return immediately if the message queue is empty.
     * </li>
     * <li>
     * This method is thread safe and multiple clients can block on this method.
     * </li>
     * <li>
     * All clients blocking on this method will unblock when the message buffer empties.
     * </li>
     * <li>
     * This method can be called before the queue buffer is activated.
     * </li>
     * </ul>
     * </p> 
     * 
     * @throws InterruptedException     operation interrupted while waiting for queue ready
     */
    public void awaitQueueEmpty() throws InterruptedException {

        // Get the request message queue empty lock 
        this.lckMsgQueEmpty.lock();
        try {
            // Return immediately if all queues are empty and nothing is pending 
            if (this.queMsgBuffer.isEmpty())
                return;
            
            else {
                
                // Log event
                if (BOL_LOGGING)
                    LOGGER.info("{}: Waiting for queue empty, queue size = {}.", JavaRuntime.getMethodName(), this.queMsgBuffer.size());
            
                // Wait for queue ready signal
                this.cndMsgQueEmpty.await();
            }
            
        } finally {
            this.lckMsgQueEmpty.unlock();
        }
    }
    

    //
    // IMessageConsumer<QueryDataResponse> Interface
    //
    
    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IMessageConsumer#isAccepting()
     */
    @Override
    public boolean isAccepting() {
        return this.bolActive;
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IMessageConsumer#activate()
     */
    @Override
    public boolean activate() {
        
        // Check if already active
        if (this.bolActive)
            return false;

        // Set activation flag 
        this.bolActive = true;

        return true;
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IMessageConsumer#shutdown()
     */
    @Override
    public boolean shutdown() throws InterruptedException {
        
        // Check state
        if (!this.bolActive)
            return false;
        
        // This will allow messages to be consumed until the queue is exhausted
        this.bolActive = false;
        this.awaitQueueEmpty();
        
        return true;
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IMessageConsumer#shutdownNow()
     */
    @Override
    public void shutdownNow() {
        
        this.bolActive = false;
        this.queMsgBuffer.clear();
    }
    
    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IMessageConsumer#offer(java.util.List)
     */
    @Override
    public void offer(List<QueryData> lstMsgs) throws IllegalStateException, InterruptedException {

        // Check if active
        if (!this.bolActive) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - queue buffer is not active.";
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new IllegalStateException(strMsg);
        }

        this.queMsgBuffer.addAll(lstMsgs);
    }

    /**
     * <p>
     * This method is equivalent to <code>{@link #offer(List)}</code>, the timeout arguments are ignored.
     * It is a requirement of the <code>IMessageConsumer</code> interface.
     * </p>
     * 
     * @return  always returns <code>true</code>
     *
     * @see com.ospreydcs.dp.api.model.IMessageConsumer#offer(java.util.List, long, java.util.concurrent.TimeUnit)
     * @see #offer(List)
     */
    @Override
    public boolean offer(List<QueryData> lstMsgs, long lngTimeout, TimeUnit tuTimeout)
            throws IllegalStateException, InterruptedException {

        // Check if active
        if (!this.bolActive) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - queue buffer is not active.";
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new IllegalStateException(strMsg);
        }

        this.queMsgBuffer.addAll(lstMsgs);
        
        return true;
    }


    //
    // IMessageSupplier<QueryDataResponse> Interface
    //
    
    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IMessageSupplier#isSupplying()
     */
    @Override
    public boolean isSupplying() {
        return this.bolActive || !this.queMsgBuffer.isEmpty();
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IMessageSupplier#take()
     */
    @Override
    synchronized
    public QueryData    take() throws IllegalStateException, InterruptedException {
        
        // Check states
        if (!this.bolActive && this.queMsgBuffer.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - supplier is inactive and queue is empty.");
        
        try {
            QueryData   msgData = this.queMsgBuffer.take();
            
            if (msgData == null)
                return null;
            
            return msgData;
            
        } finally {
            
            // We need to signal any threads blocking on the queue capacity
            this.signalRequestQueueConditions();
        }
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IMessageSupplier#poll()
     */
    @Override
    synchronized
    public QueryData    poll() throws IllegalStateException {

        // Check state
        if (!this.bolActive && this.queMsgBuffer.isEmpty()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - supplier is inactive and queue is empty.";
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new IllegalStateException(strMsg);
        }
        
        try {
            QueryData   msgData = this.queMsgBuffer.poll();
            
            if (msgData == null)
                return null;
            
            return msgData;
            
        } finally {
            
            // We need to signal any threads blocking on the queue capacity
            this.signalRequestQueueConditions();
        }
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IMessageSupplier#poll(long, java.util.concurrent.TimeUnit)
     */
    @Override
    synchronized
    public QueryData    poll(long cntTimeout, TimeUnit tuTimeout)
            throws IllegalStateException, InterruptedException {
        
        // Check state
        if (!this.bolActive && this.queMsgBuffer.isEmpty()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - supplier is inactive and queue is empty.";
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new IllegalStateException(strMsg);
        }
        
        try {
            QueryData msgData = this.queMsgBuffer.poll(cntTimeout, tuTimeout);
            
            if (msgData == null)
                return null;
            
            return msgData;
            
        } finally {
            
            // We need to signal any threads blocking on the queue capacity
            this.signalRequestQueueConditions();
        }
    }


    //
    // Support Methods
    //
    
    /**
     * <p>
     * Signals all threads waiting on message queue conditions.
     * </p>
     * <p>
     * To be called when a a queue message removal event occurs. 
     * Checks the current size of the message queue <code>{@link #queMsgBuffer}</code>
     * and signals all threads with lock <code>{@link #lckMsgQueReady}</code> for the
     * following conditions:
     * <ul>
     * <li>If queue size = 0 then signal <code>{@link #cndMsgQueEmpty}</code>.</li>
     * </ul>
     * </p>
     */
    private void signalRequestQueueConditions() {
        
        // Signal any threads blocking on the queue empty condition
        this.lckMsgQueEmpty.lock();
        try {
            if (this.queMsgBuffer.isEmpty())
                this.cndMsgQueEmpty.signalAll();

        } finally {
            this.lckMsgQueEmpty.unlock();
        }
    }

}
