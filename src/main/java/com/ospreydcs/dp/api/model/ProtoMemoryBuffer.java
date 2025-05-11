/*
 * Project: dp-api-common
 * File:	ProtoMemoryBuffer.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	ProtoMemoryBuffer
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
 * @since Jul 23, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.GeneratedMessage;
import com.ospreydcs.dp.api.ingest.model.grpc.IngestionMemoryBuffer;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * Protocol Buffers message allocation staging buffer and data stream throttling tool based upon memory allocation.
 * </p>
 * <p>
 * Class instances act as buffers for producers of <code>T</code> messages which are presented the
 * <code>IMessageConsumer</code> interface.  Produces can stage their output messages for consumption
 * by <code>T</code> message consumers which are offered the <code>IMessageSupplier</code> interface.
 * (Note that <code>T</code> produces utilize the <code>IMessageConsumer</code> interface operations 
 * while <code>T</code> consumers use the <code>IMessageSupplier</code> interface operations.)
 * </p>
 * <p>  
 * There are essentially 4 functions for class instances:
 * <ol>
 * <li>
 * Universal collection point - Class instances can act as a common collection point for <code>T</code> 
 * messages through the <code>IMessageConsumer&lt;T&gt;</code> interface.  Multiple clients can supply
 * <code>T</code> messages to the message buffer.
 * </li>
 * <li>
 * Universal supply point - Class instances can act as a common supply for <code>T</code> messages
 * through the <code>IMessageSupplier&lt;T&gt;</code> interface.  Multiple clients can consume
 * <code>T</code> messages from the message buffer.
 * </li>
 * <li>
 * Flow control - The class acts as a buffering queue to mitigate any spikes in the data flow of generic 
 * Protocol Buffers <code>T</code> type messages, maintaining a smooth data flow from producer to consumer.
 * </li>
 * <li>
 * Message throttling - The class provides "back pressure," or "message throttling" for the suppliers of <code>T</code> 
 * messages whenever the consumer utilizing the <code>IMessageSupplier&lt;T&gt;</code> interface
 * falls behind.  Throttling is based upon the total memory allocation within the message buffer. 
 * </li>
 * </ol>
 * </p>
 * <p>
 * <h2>Thread Safety</h2>
 * All operations should be thread safe.  Many are explicitly synchronized.  Thus, the generic implementation
 * may not be as efficient as a concrete implementation as performance is chosen over safety.
 * </p>
 * <p>
 * <h2>Flow Control</h2>
 * One significant use for class instances is message flow control between a producer and consumer.  
 * With spikes at the producer end can be absorbed by the message buffer allowing the consumer of 
 * <code>T</code> message to continue at its natural rate.  Additionally, the <code>ProtoMemoryBuffer</code> 
 * class allows clients to set a finite capacity which will block over-enabled producers if consumers
 * fall behind, we call this condition "message throttling", or "back pressure." 
 * </p>
 * <p>
 * <h2>Message Throttling</h2>
 * Back pressure, or "message throttling", is enforced based upon the memory allocated within the queue buffer.  
 * Specifically, a class 
 * instance can emulate a finite-capacity data buffer (although it is not implemented as such).  Thus, if the 
 * buffer fills to capacity <em>data</em> within the <code>T</code> messages, it will not accept additional
 * messages until the memory allocation drops below capacity (i.e., due to the activity of message consumers).
 * There are various configuration methods for enabling/disabling back pressure and setting the queue capacity.
 * Additionally, there are methods for clients to block on the "queue ready" condition (i.e., the queue drops
 * below capacity) and the queue empty condition. 
 * </p>
 * <p>
 * Note that the back pressure is enforced in a memory allocation fashion; specifically, message throttling occurs
 * whenever the memory allocation within the buffer increases beyond capacity, regardless of the total number of 
 * messages within the buffer.  For a message-based back-pressure implementation see 
 * <code>{@link ProtoMessageBuffer}</code>.
 * </p>
 * <p>
 * <h2>WARNING:</h2>
 * There is significant additional processing to determine the memory allocation within the queue buffer.  Specifically,
 * the memory allocation of each message entering the queue must be computed and monitored, along with each message 
 * leaving the queue (i.e., using <code>{@link GeneratedMessage#getSerializedSize()}</code>). If the situation
 * allows a message-based queue capacity throttling, or no throttling at all, consider using the
 * <code>ProtoMessageBuffer</code> class.
 * </p>
 * 
 * @param   <T>     Protocol Buffers message type
 *
 * @author Christopher K. Allen
 * @since Jul 23, 2024
 *
 */
public class ProtoMemoryBuffer<T extends GeneratedMessage> implements IMessageConsumer<T>, IMessageSupplier<T> {

    
    //
    // Creators
    //

    /**
     * <p>
     * Creates a new instance of <code>ProtoMemoryBuffer</code> with all default parameters.
     * </p>
     * <p>
     * The message buffer has the following configuration:
     * <ul>
     * <li>Logging - enabled.</li>
     * <li>Back pressure - disabled.</li>
     * <li>Queue capacity - unlimited.</li>
     * </ul>
     * </p>
     * 
     * @return  new <code>ProtoMemoryBuffer</code> instance ready for activation
     */
    public static <T extends GeneratedMessage> ProtoMemoryBuffer<T> create() {
        return new ProtoMemoryBuffer<T>();
    }
    
    /**
     * <p>
     * Creates a new instance of <code>ProtoMemoryBuffer</code> with the given queue buffer capacity and 
     * back pressure enforcement enabled.
     * </p>
     * <p>
     * Event logging is enabled.
     * </p>
     * 
     * @param szMaxQueAlloc     maximum memory allocation of the message queue buffer
     * 
     * @return  new <code>ProtoMemoryBuffer</code> instance ready for activation
     */
    public static <T extends GeneratedMessage> ProtoMemoryBuffer<T> create(long szMaxQueAlloc) {
        return new ProtoMemoryBuffer<T>(szMaxQueAlloc);
    }
    
    /**
     * <p>
     * Creates a new instance of <code>ProtoMemoryBuffer</code> with the given parameters.
     * </p>
     * <p>
     * Event logging is enabled.
     * </p>
     * 
     * @param szMaxQueAlloc     maximum memory allocation of the message queue buffer
     * @param bolBackPressure   enable/disable back pressure (implicit throttling) at <code>{@link #offer(List)}</code>
     * 
     * @return  new <code>ProtoMemoryBuffer</code> instance ready for activation
     */
    public static <T extends GeneratedMessage> ProtoMemoryBuffer<T> create(long szMaxQueAlloc, boolean bolBackPressure) {
        return new ProtoMemoryBuffer<T>(szMaxQueAlloc, bolBackPressure);
    }
    
    /**
     * <p>
     * Creates a new instance of <code>ProtoMemoryBuffer</code> with the given parameters.
     * </p>
     * <p>
     * Event logging is enabled.
     * </p>
     * 
     * @param szMaxQueAlloc     maximum memory allocation of the message queue buffer
     * @param bolBackPressure   enable/disable back pressure (implicit throttling) at <code>{@link #offer(List)}</code>
     * @param bolLogging        enable/disable event logging
     * 
     * @return  new <code>ProtoMemoryBuffer</code> instance ready for activation
     */
    public static <T extends GeneratedMessage> ProtoMemoryBuffer<T> create(int szMaxQueAlloc, boolean bolBackPressure, boolean bolLogging) {
        return new ProtoMemoryBuffer<T>(szMaxQueAlloc, bolBackPressure, bolLogging);
    }
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    //
    // Configuration Parameters
    //
    
    /** Is logging enabled */
    private boolean bolLogging;

    /** Exert back pressure on clients (from frame buffer) enabled flag */
    private boolean bolBackPressure; 
    
    /** The memory allocation within the outgoing message queue (i.e., when back pressure is enabled) */ 
    private long    szMaxQueueAlloc; 
    

    //
    // Instance Resources
    //
    
    /** The buffer of messages used for staging before consumption */
    private final BlockingQueue<T>  queMsgBuffer = new LinkedBlockingQueue<>();
    
    
    /** Synchronization lock for memory allocation updates */
    private final Object            objLock = new Object();
    
    
    /** The message queue buffer back pressure lock */
    private final Lock              lckMsgQueReady = new ReentrantLock();
    
    /** The message queue buffer ready (or "not full") lock condition */
    private final Condition         cndMsgQueReady = lckMsgQueReady.newCondition();
    
    /** The message queue buffer empty lock */
    private final Lock              lckMsgQueEmpty = new ReentrantLock();
    
    /** The message queue buffer empty lock condition */
    private final Condition         cndMsgQueEmpty = lckMsgQueEmpty.newCondition();
    
    
    //
    // State Variables
    //
    
    /** Is supplier enabled (not been shutdown) */
    private boolean bolActive = false;
    
    /** Current queue memory allocation */
    private long    szQueueAlloc = 0;
    
    
    
    /**
     * <p>
     * Constructs a new instance of <code>ProtoMemoryBuffer</code>.
     * </p>
     * <p>
     * The message buffer has the following configuration:
     * <ul>
     * <li>Logging - enabled.</li>
     * <li>Back pressure - disabled.</li>
     * <li>Queue capacity - unlimited.</li>
     * </ul>
     * </p>
     */
    public ProtoMemoryBuffer() {
        this(Long.MAX_VALUE, false, true);
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>ProtoMemoryBuffer</code> with the given queue buffer capacity.
     * </p>
     * <p>
     * Back-pressure enforcement is enabled and event logging is enabled.
     * </p>
     *
     * @param szQueueAlloc      maximum memory allocation of the message queue buffer
     */
    public ProtoMemoryBuffer(long szQueAlloc) {
        this(szQueAlloc, true, true);
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>ProtoMemoryBuffer</code> initialized with the given configuration.
     * </p>
     * <p>
     * Event logging is enabled.
     * </p>
     *
     * @param szQueueAlloc      maximum memory allocation of the message queue buffer
     * @param bolBackPressure   enable/disable back pressure (implicit throttling) at <code>{@link #offer(List)}</code>
     */
    public ProtoMemoryBuffer(long szQueAlloc, boolean bolBackPressure) {
        this(szQueAlloc, bolBackPressure, true);
    }

    /**
     * <p>
     * Constructs a new instance of <code>ProtoMemoryBuffer</code> initialized with the given configuration.
     * </p>
     *
     * @param szQueueAlloc      maximum memory allocation of the message queue buffer
     * @param bolBackPressure   enable/disable back pressure (implicit throttling) at <code>{@link #offer(List)}</code>
     * @param bolLogging        enable/disable event logging
     */
    public ProtoMemoryBuffer(long szQueAlloc, boolean bolBackPressure, boolean bolLogging) {
        this.szMaxQueueAlloc = szQueAlloc;
        this.bolBackPressure = bolBackPressure;
        this.bolLogging = bolLogging;
    }

    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Enables event logging within message buffer operations.
     * </p>
     */
    synchronized 
    public void enableLogging() {
        this.bolLogging = true;
    }
    
    /**
     * <p>
     * Disables event logging within the message buffer operations.
     * </p>
     */
    synchronized
    public void disableLogging() {
        this.bolLogging = false;
    }
    
    /**
     * <p>
     * Enables client back pressure (implicit throttling) from finite capacity ingestion message buffer.
     * </p>
     * <p>
     * This feature is available to tune the streaming of large numbers of messages;
     * it allows back pressure from the supplier side.
     * This buffer class maintains a buffer of message to be staged for the supplier
     * in order to cope with spikes from the client.  Enabling this option
     * prevents clients from adding additional message when this buffer is full (at capacity).
     * (A full buffer indicates a backlog of processing from the consumer of supplied messages.)
     * Thus, if the buffer is at capacity the method <code>{@link #offer(List)}</code> will block
     * until space is available in the queue buffer.
     * </p>
     * <p>
     * <h2>Thread Safety</h2>
     * This method is synchronized for thread safety.  Changing configuration parameters must
     * be done atomically.  Thus, this configuration parameter 
     * will not be changed until this method acquires the <code>this</code> lock from any other
     * competing threads.
     * </p>
     * 
     * @param szMaxQueAlloc     maximum memory allocation of the message queue buffer
     */
    synchronized 
    public void enableBackPressure(long szMaxQueAlloc) {
        this.bolBackPressure = true;
        this.szMaxQueueAlloc = szMaxQueAlloc;
    }
    
    /**
     * <p>
     * Disables client back pressure (implicit message throttling).
     * </p>
     * <p>
     * The back-pressure feature is available to tune the processing of large numbers of messages;
     * it allows back pressure from the consumers of the supplier to be felt at the client side.
     * The message supplier class maintains a buffer of messages to be staged
     * in order to cope with spikes from the client.  Enabling this option
     * prevents clients from adding additional messages when this buffer is full.
     * (A full buffer indicates a backlog of processing within the Ingestion Service.)
     * </p>
     * <p>
     * <h2>Effect</h2>
     * Disabling client back pressure allows the incoming message buffer to expand indefinitely.
     * Clients can always add more messages regardless of any backlog in consumption at the 
     * supplier processing.
     * </p>
     * <p>
     * <h2>Thread Safety</h2>
     * This method is synchronized for thread safety.  Changing configuration parameters must
     * be done atomically.  Thus, this configuration parameter 
     * will not be changed until this method acquires the <code>this</code> lock from any other
     * competing threads.
     * </p>
     */
    synchronized
    public void disableBackPressure() {
        this.bolBackPressure = false;
    }
    
    /**
     * <p>
     * Sets the maximum message memory allocation of the queue buffer.
     * </p>
     * <p>
     * The queue capacity is the critical parameter for supply throttling, either implicit through
     * back-pressure blocking at <code>{@link #offer(List)}</code> or explicit throttling with
     * <code>{@link #awaitQueueReady()}</code>.  If the request message count within the queue
     * exceed the given value the throttling is activated.  In that case this <code>ProtoMemoryBuffer</code> 
     * instance blocks at <code>{@link #offer(List)}</code> if back-pressure is enabled, and 
     * <code>{@link #awaitQueueReady()}</code> blocks regardless of back-pressure settings.
     * </p>
     * <p>
     * <h2>Thread Safety</h2>
     * This method is synchronized for thread safety.  Changing configuration parameters must
     * be done atomically.  Thus, this configuration parameter 
     * will not be changed until this method acquires the <code>this</code> lock from any other
     * competing threads.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This parameter only has context when back pressure is enabled.  See method
     * <code>{@link #enableBackPressure(int)}</code>.
     * </p>
     * 
     * @param szQueueAlloc      memory allocation of the message queue buffer
     */
    synchronized
    public void setQueueAllocation(int szQueueAlloc) {
        this.szMaxQueueAlloc = szQueueAlloc;
    }
    
    
    //
    // State Inquiry
    //
    
    /**
     * <p>
     * Returns whether or not implicit throttling (i.e., "back pressure") is enabled.
     * </p>
     * <p>
     * Implicit throttling, or "back pressure", is felt at the <code>{@link #offer(IngestDataRequest)}</code>
     * and <code>{@link #offer(List)}</code> methods.  If the queue buffer is at capacity these methods
     * blocking until a sufficient number of ingest data request messages have been consumed.
     * </p>
     * 
     * @return  <code>true</code> if back pressure is enabled at <code>{@link #offer(List)}</code>
     */
    public boolean hasBackPressure() {
        return this.bolBackPressure;
    }
    
    /**
     * <p>
     * Returns the current maximum message memory allocation of the message queue buffer.
     * </p>
     * 
     * @return  the capacity of the queue buffer before throttling is invoking
     */
    public long  getMaxQueueCapacity() {
        return this.szMaxQueueAlloc;
    }
    
    /**
     * <p>
     * Returns the current size of the queue buffer.
     * </p>
     * <p>
     * Returns the number of messages in the queue buffer at the time of invocation.  Note that this
     * is inherently a dynamic quantity.
     * </p>
     *  
     * @return  number of request messages currently in the queue
     */
    public int  getQueueSize() {
        return this.queMsgBuffer.size();
    }
    
    /**
     * <p>
     * Returns the total memory allocation of all messages within the queue buffer.
     * </p>
     * <p>
     * Returns the total allocation currently carried within the queue buffer in bytes.  The value is
     * computed dynamically using the <code>{@link GeneratedMessage#getSerializedSize()</code> method
     * of all messages currently within the buffer.
     * </p>
     * 
     * @return  the total memory allocation of all messages currently within the buffer
     */
    public long getQueueAllocation() {
        return this.szQueueAlloc;
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Allows clients to explicitly block until the message queue buffer is ready (below capacity) 
     * and back pressure is relieved.
     * </p>
     * <p>
     * This method allows supplier clients to explicitly wait for back-pressure relief.  This method blocks
     * if the queue buffer is at maximum capacity, or returns immediately if not.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method will return immediately if the message queue is not at capacity.
     * </li>
     * <li>
     * This method is thread safe and multiple clients can block on this method.
     * </li>
     * <li>
     * All clients blocking on this method will unblock when the message buffer becomes ready.
     * </li>
     * <li>
     * This method can be called before the queue buffer is activated.
     * </li>
     * <li>
     * This method is valid even if the back-pressure features is disabled (i.e., with method
     * <code>{@link #disableBackPressure()}</code> or in API configuration file).
     * </li>
     * </ul>
     * </p> 
     * 
     * @throws InterruptedException     operation interrupted while waiting for queue ready
     */
    public void awaitQueueReady() throws InterruptedException {
        
        // Do this regardless of whether back pressure is enabled or not
        //  The client wants to wait, we wait
        this.lckMsgQueReady.lock();
        try {
            // Return immediately if the queue is not at capacity
            if (this.szQueueAlloc < this.szMaxQueueAlloc)
                return;
            
            else {
                
                // Log event
                if (bolLogging)
                    LOGGER.info("{}: Blocking on queue memory allocation {} > maximum {} (back pressure event).", 
                            JavaRuntime.getMethodName(), 
                            this.szQueueAlloc, 
                            this.szMaxQueueAlloc);
            
                // Wait for queue ready signal
                this.cndMsgQueReady.await();
            }
            
        } finally {
            this.lckMsgQueReady.unlock();
        }
    }
    
    /**
     * <p>
     * Allows clients to block until the message queue queue buffer completely empties.
     * </p>
     * <p>
     * This method allows supplier clients to wait for the message queue buffer
     * to fully empty.  Clients can add a fixed number of messages then measure 
     * the time for messages to be consumed.  This activity may be useful
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
                if (bolLogging)
                    LOGGER.info("{}: Waiting for queue empty, queue size = {}.", 
                            JavaRuntime.getMethodName(), 
                            this.queMsgBuffer.size());
            
                // Wait for queue ready signal
                this.cndMsgQueEmpty.await();
            }
            
        } finally {
            this.lckMsgQueEmpty.unlock();
        }
    }
    
    
    //
    // IMessageConsumer<IngestDataRequest> Interface
    //
    
    /**
     * 
     * @see com.ospreydcs.dp.api.model.IMessageConsumer#isAccepting()
     */
    @Override
    public boolean isAccepting() {
        return this.bolActive;
    }
    
    /**
     * <p>
     * Activates the <code>T</code> message queue buffer.
     * </p>
     * <p>
     * After invoking this method the message supplier instance is ready for message consumption
     * acceptance via <code>{@link #offer(List)}</code>.  
     * <code>T</code> messages are added to the buffer where they
     * are staged for supply via the <code>IMessageSupplier&lt;T&gt;</code> interface.
     * Processed messages are available to consumers through the
     * <code>{@link IMessageSupplier}</code> interface blocking methods.
     * Implicit or explicit throttling operations are available via the queue capacity parameter.
     * </p>
     * <h2>Operation</h2>
     * This method enables all queue buffer tasks which are then continuously enabled
     * throughout the lifetime of this instance, or until explicitly shut down.  The method
     * <code>{@link #isSupplying()}</code> will return <code>true</code> after invocation, regardless
     * of whether or not messages are available.  This condition remains in effect until a shutdown operation
     * is invoked.   
     * </p>
     * <p>
     * <h2>Shutdowns</h2>
     * Proper operation requires that the queue buffer be shutdown where no longer needed.
     * This signals consumers that request messages are no longer available when the queue buffer is
     * exhausted.  That is, after invocation the <code>{@link #isSupplying()}</code> method will return
     * <code>false</code> when there are no more messages available.  
     * Use either <code>{@link #shutdown()}</code> or <code>{@link #shutdownNow()}</code> to shutdown 
     * the queue buffer.
     * </p>
     * <p>
     * <h2>Thread Safety</h2>
     * This method is synchronized for thread safety.  The activation operation must be done
     * atomically and by only one thread. (Additional invocations do nothing but return 
     * <code>false</code>).
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method should be called only <em>once</em>.
     * </li>
     * <li>
     * A shutdown operation should always be invoked when the buffer is no longer supplying messages.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  <code>true</code> if the message queue buffer was successfully activated,
     *          <code>false</code> if the message consumer was already activated
     */
    @Override
    synchronized
    public boolean activate() {
        
        // Check if already enabled
        if (this.bolActive)
            return false;

        // Set activation flag 
        this.bolActive = true;

        return true;
    }
    
    /**
     * <p>
     * Performs an orderly shut down of the queue buffer and message consumer interface.
     * </p>
     * <p>
     * The <code>{@link #bolActive}</code> flag is set <code>false</code> preventing any new
     * <code>T</code> to be added to the queue.  However, consumers of the
     * <code>IMessageSupplier&lt;T&gt;</code> interface can continue to 
     * request messages so long as messages are available.
     * That is, consumers of messages can continue to poll for available messages until the supply
     * is exhausted.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This method will block until all the message queue is completely exhausted.
     * Thus, message consumers <em>must remain enabled after invoking this method</em> as any remaining messages
     * left in the queue will cause this method to block indefinitely. 
     * </p>
     * <p>
     * To force a hard shutdown of the queue buffer where any remaining messages are discarded use method
     * <code>#shutdownNow()</code>.  This is appropriate when the message consumer is known to be inactive
     * and the queue buffer still contains messages.
     * </p>
     * 
     * @return <code>true</code> if the message consumer was successfully shutdown,
     *         <code>false</code> if the message consumer was not enabled or shutdown operation failed
     * 
     * @throws InterruptedException interrupted while waiting for processing threads to complete
     */
    @Override
    synchronized
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
     * <p>
     * Performs a hard shutdown of the queue buffer and message consumer interface.
     * </p>
     * <p>
     * The <code>{@link #bolActive}</code> flag is set to <code>false</code> and all
     * <code>T</code> messages are cleared from the queue buffer thus
     * terminating the <code>IMessageSupplier&lt;T&gt;</code> interface.
     * That is, the outgoing message queue is cleared.  
     * The method returns immediately upon terminating all activity.
     * </p>
     */
    @Override
    synchronized
    public void shutdownNow() {
        
        this.bolActive = false;
        this.queMsgBuffer.clear();
        this.szQueueAlloc = 0;
    }
    
    /**
     * <p>
     * Adds the given message to the head of the queue buffer.
     * </p>
     * <p>
     * This is a convenience method for single message enqueueing.  All operations are 
     * deferred to <code>{@link #offer(List)}</code> after converting the argument to a single-element
     * list.
     * </p>
     *  
     * @param msg   message for consumption staging
     * 
     * @throws IllegalStateException    the queue buffer is currently inactive
     * @throws InterruptedException     interrupted while waiting for message buffer ready (back-pressure enabled)
     * 
     * @see #offer(List)
     */
    @Override
    synchronized
    public void offer(T msg) throws IllegalStateException, InterruptedException {
        this.offer(List.of(msg));
    }
    
    /**
     * <p>
     * Add the given list of messages to the queue buffer for consumption staging (in order).
     * </p>
     * <p>
     * The given list is added to the message queue buffer .  All messages
     * entering the queue buffer are then are then available through the 
     * <code>{@link IMessageSupplier}</code> interface. Note that an exception is 
     * thrown if the queue buffer has not been activated.
     * </p>
     * <p>
     * <h2>Back Pressure - Implicit Throttling</h2>
     * If the back-pressure feature is enable this method blocks whenever the outgoing message
     * queue buffer is at capacity.  The method will not return until the consumer of 
     * <code>T</code> messages has taken the queue below capacity.
     * </p>
     * <p>
     * <h2>Explicit Throttling</h2>
     * The <code>{@link #awaitQueueReady()}</code> method is available for clients to explicitly
     * block until the queue buffer drops below capacity. Clients can disable the back-pressure feature 
     * so that all messages are immediately enqueued into the buffer.  Thus, here explicit throttling
     * consists of the following three steps:
     * <ol>
     * <li>Disable the back-pressure feature with <code>{@link #disableBackPressure()}</code>.</li>
     * <li>Invoke the <code>{@link #awaitQueueReady()}</code> method which blocks if necessary.</li>
     * <li>Invoke the <code>{@link #offer(List)}</code> method to stage messages for consumption.</li>  
     * </ol>
     * Clearly the first step need only be executed once then steps 2 and 3 are executed repeatedly as required 
     * for all additional data.
     * </p>
     * 
     * @param lstMsgs    ordered list of messages for consumption staging
     * 
     * @throws IllegalStateException    the queue buffer is currently inactive
     * @throws InterruptedException     interrupted while waiting for message buffer ready (back-pressure enabled)
     */
    @Override
    synchronized
    public void offer(List<T> lstMsgs) throws IllegalStateException, InterruptedException {

        // Check if enabled
        if (!this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - queue buffer is not enabled.");
        
        // Compute the the memory allocation of the request messages
        long    szAlloc = lstMsgs.stream().mapToLong(msg -> msg.getSerializedSize()).sum();
        
        // If no back-presssure enforcement just add all frames to raw frame buffer and return
        if (!this.bolBackPressure) {
            this.queMsgBuffer.addAll(lstMsgs);
            this.szQueueAlloc += szAlloc;
            
            return;
        }
        
        // Enforce back-pressure to the client if message queue is full 
        this.lckMsgQueReady.lock();
        try {
            if (this.szQueueAlloc >= this.szMaxQueueAlloc) {
             
                // Log event
                if (bolLogging)
                    LOGGER.info("{}: Blocking on queue allocation {} > maximum {} (back pressure event).", 
                            JavaRuntime.getMethodName(), 
                            this.szQueueAlloc, this.szMaxQueueAlloc);
            
                // Wait for queue ready signal
                this.cndMsgQueReady.await();  // throws InterruptedException
            }

            this.queMsgBuffer.addAll(lstMsgs);
            this.szQueueAlloc += szAlloc;

        } finally {
            this.lckMsgQueReady.unlock();
        }
    }
    
    /**
     * <p>
     * Add the given list of messages to the queue buffer for consumption staging.
     * </p>
     * <p>
     * The given list is added to the queue buffer of messages.  All messages
     * entering the queue buffer are then are then available for consumption through the 
     * <code>{@link IMessageSupplier}</code> interface. Note that an exception is thrown 
     * if the queue buffer has not been activated.
     * </p>
     * <p>
     * <h2>Back Pressure - Implicit Throttling</h2>
     * If the back-pressure feature is enable this method blocks whenever the outgoing message
     * queue buffer is at capacity.  The method will not return until the consumer of 
     * <code>T</code> messages has taken the queue below capacity, or the given
     * timeout limit has elapsed.
     * </p>
     * <p>
     * <h2>Explicit Throttling</h2>
     * The <code>{@link #awaitQueueReady()}</code> method is available for clients to explicitly
     * block until the queue buffer drops below capacity. Clients can disable the back-pressure feature 
     * so that all messages are immediately enqueued into the buffer.  Thus, here explicit throttling
     * consists of the following three steps:
     * <ol>
     * <li>Disable the back-pressure feature with <code>{@link #disableBackPressure()}</code>.</li>
     * <li>Invoke the <code>{@link #awaitQueueReady()}</code> method which blocks if necessary.</li>
     * <li>Invoke the <code>{@link #offer(List)}</code> method to stage messages for consumption.</li>  
     * </ol>
     * Clearly the first step need only be executed once then steps 2 and 3 are executed repeatedly as required 
     * for all additional data.
     * </p>
     * 
     * @param lstMsgs       ordered list of messages for consumption staging
     * @param lngTimeout    timeout limit to wait for message acceptance if back-pressure is enabled
     * @param tuTimeout     timeout units for message acceptance under back pressure.
     * 
     * @return  <code>true</code> if the messages were successfully accepted,
     *          <code>false</code> if the timeout limit was exceeded (messages were not accepted)
     * 
     * @throws IllegalStateException    the queue buffer is currently inactive
     * @throws InterruptedException     interrupted while waiting for message buffer ready (back-pressure enabled)
     */
    @Override
    synchronized 
    public boolean offer(List<T> lstMsgs, long lngTimeout, TimeUnit tuTimeout) throws IllegalStateException, InterruptedException {

        // Check if enabled
        if (!this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - queue buffer is not enabled.");
        
        // Compute the the memory allocation of the request messages
        long    szAlloc = lstMsgs.stream().mapToLong(msg -> msg.getSerializedSize()).sum();
        
        // If no back-presssure enforcement just add all frames to raw frame buffer and return
        if (!this.bolBackPressure) {
            this.queMsgBuffer.addAll(lstMsgs);
            this.szQueueAlloc += szAlloc;
            
            return true;
        }
        
        // Enforce back-pressure to the client if processed frame queue is full 
        this.lckMsgQueReady.lock();
        try {
            boolean bolResult = false;
            
            if (this.szQueueAlloc >= this.szMaxQueueAlloc) {
             
                // Log event
                if (bolLogging)
                    LOGGER.info("{}: Blocking on queue allocation {} > maximum {} (back pressure event).", 
                            JavaRuntime.getMethodName(), 
                            this.szQueueAlloc, 
                            this.szMaxQueueAlloc);
            
                // Wait for queue ready signal
                this.cndMsgQueReady.await(lngTimeout, tuTimeout);  // throws InterruptedException
            }

            bolResult = this.queMsgBuffer.addAll(lstMsgs);
            this.szQueueAlloc += szAlloc;

            return  bolResult;
            
        } finally {
            this.lckMsgQueReady.unlock();
        }
    }

    
    //
    // IMessageSupplier<IngestDataRequest> Interface
    //
    
    /**
     * <p>
     * Determines whether or not <code>T</code> messages are currently being supplied. 
     * </p>
     * <p>
     * This condition indications that messages are available, or not currently available but still being supplied 
     * (i.e., the queue buffer is still accepting messages).
     * The returned value is <code>true</code> if any (or all) of the following conditions apply:
     * <ul>
     * <li>The queue buffer has been activated (and not shut down).</li>
     * <li>The buffer has been shut down but there are still messages in the queue.</li>
     * </ul>
     * </p>
     *  
     * @return <code>true</code> if there are currently messages available or pending
     *         <code>false</code> otherwise
     *
     * @see com.ospreydcs.dp.api.model.IMessageSupplier#isSupplying()
     */
    @Override
    synchronized
    public boolean isSupplying() {
        return this.bolActive || !this.queMsgBuffer.isEmpty();
    }

    /**
     *
     * @see com.ospreydcs.dp.api.model.IMessageSupplier#take()
     */
    @Override
    public T    take() throws IllegalStateException, InterruptedException {
        
        // Check states
        if (!this.bolActive && this.queMsgBuffer.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - supplier is inactive and queue is empty.");
        
        try {
            T msg = this.queMsgBuffer.take();
        
            if (msg == null)
                return null;
            
            // Compute allocation and adjust current capacity
            synchronized (this.objLock) {
                this.szQueueAlloc -= msg.getSerializedSize();
            }
            
            return msg;
            
        } finally {
            
            // We need to signal any threads blocking on the queue capacity
            this.signalRequestQueueConditions();
        }
    }

    /**
     *
     * @see com.ospreydcs.dp.api.model.IMessageSupplier#poll()
     */
    @Override
    public T    poll() throws IllegalStateException {

        // Check state
        if (!this.bolActive && this.queMsgBuffer.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - supplier is inactive and queue is empty.");
        
        try {
            T msg = this.queMsgBuffer.poll();
            
            if (msg == null)
                return null;
            
            // Compute allocation and adjust current capacity
            synchronized (this.objLock) {
                this.szQueueAlloc -= msg.getSerializedSize();
            }
            
            return msg;
            
        } finally {
            
            // We need to signal any threads blocking on the queue capacity
            this.signalRequestQueueConditions();
        }
    }

    /**
     *
     * @see com.ospreydcs.dp.api.model.IMessageSupplier#poll(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public T    poll(long cntTimeout, TimeUnit tuTimeout)
            throws IllegalStateException, InterruptedException {
        
        // Check state
        if (!this.bolActive && this.queMsgBuffer.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - supplier is inactive and queue is empty.");
        
        try {
            T   msg = this.queMsgBuffer.poll(cntTimeout, tuTimeout);
            
            if (msg == null)
                return null;
            
            // Compute allocation and adjust current capacity
            synchronized (this.objLock) {
                this.szQueueAlloc -= msg.getSerializedSize();
            }
            
            return msg;
            
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
     * Signals all threads waiting on request message queue conditions.
     * </p>
     * <p>
     * To be called when a a queue message removal event occurs. 
     * Checks the current size of the message queue <code>{@link #queMsgBuffer}</code>
     * and signals all threads with lock <code>{@link #lckMsgQueReady}</code> for the
     * following conditions:
     * <ul>
     * <li>If queue size < capacity then signal <code>{@link #cndMsgQueReady}</code>.</li>
     * <li>If queue size = 0 then signal <code>{@link #cndMsgQueEmpty}</code>.</li>
     * </ul>
     * </p>
     */
    private void signalRequestQueueConditions() {
        
        // Signal any threads blocking on the queue ready condition
        this.lckMsgQueReady.lock();
        try {
            if (this.szQueueAlloc < this.szMaxQueueAlloc)
                this.cndMsgQueReady.signalAll();
            
        } finally {
            this.lckMsgQueReady.unlock();
        }
        
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
