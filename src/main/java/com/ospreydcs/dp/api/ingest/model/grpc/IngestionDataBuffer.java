/*
 * Project: dp-api-common
 * File:	IngestionDataBuffer.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IngestionDataBuffer
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
 * @since Jul 28, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.grpc;

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
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.ingest.model.IMessageSupplier;
import com.ospreydcs.dp.api.ingest.model.IResourceConsumer;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * Ingestion message transmission staging buffer and data stream throttling tool.
 * </p>
 * <p>
 * Class instances act as buffers for producers of <code>IngestDataRequest</code> messages where such produces
 * can stage their output before transmission to the Ingestion Service.  Thus, instances act as a buffering queue
 * to mitigate any spikes in ingestion frame processing and maintain a smooth data flow to the Ingestion Service.
 * Class also implements "back pressure" throttling for the suppliers of processed <code>IngestDataRequest</code> 
 * messages and the consumer utilizing the <code>IMessageSupplier&lt;IngestDataRequest&gt;</code> interface 
 * (presumably a channel to the Ingestion Service).  
 * </p>
 * <p>
 * <h2>Message Throttling</h2>
 * Back pressure, or "throttling", is enforced in a data-allocation based fashion.  Specifically, a class instance 
 * can emulate a finite-capacity data buffer.  That is, rather than being message-based the buffer is capacity limited
 * in the amount of memory allocation it can hold.  
 * If the buffer fills to memory-allocation capacity with <code>IngestDataRequest</code> messages, it will not accept 
 * additional messages until the buffer drops below capacity due to the activity of message consumers using the back-end
 * <code>IMessageSupplier&lt;IngestDataRequest&gt;</code> interface.
 * </p>
 * <p>
 * For an message-based back-pressure implementation see <code>{@link IngestionMessageBuffer}</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 28, 2024
 *
 */
public class IngestionDataBuffer implements IResourceConsumer<IngestDataRequest>, IMessageSupplier<IngestDataRequest> {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new instance of <code>IngestionDataBuffer</code> with default parameters.
     * </p>
     * <p>
     * Ingestion message queue buffer memory allocation capacity and back-pressure enforcement is taken 
     * from the default values of the API library configuration.
     * </p>
     *
     * @return  a new ingestion data buffer ready for operation
     * 
     * @see #LNG_MAX_ALLOC
     * @see #BOL_BUFFER_BACKPRESSURE
     */
    public static IngestionDataBuffer  create() { 
        return new IngestionDataBuffer();
    }
    
    /**
     * <p>
     * Creates a new instance of <code>IngestionDataBuffer</code> with the given queue buffer max allocation.
     * </p>
     * <p>
     * Back-pressure enforcement is taken from the default values of the API library configuration
     * </p>
     *
     * @param szQueueCapacity     maximum memory allocation capacity of queue buffer
     *
     * @return  a new ingestion data buffer ready for operation
     */
    public static IngestionDataBuffer   create(long szQueueCapacity) {
        return new IngestionDataBuffer(szQueueCapacity);
    }
    
    /**
     * <p>
     * Creates a new instance of <code>IngestionDataBuffer</code> initialized with the given parameters.
     * </p>
     *
     * @param szQueueCapacity   maximum memory allocation capacity of queue buffer
     * @param bolBackPressure   enforce back pressure (implicit throttling) at <code>{@link #offer(List)}</code>
     * 
     * @return  a new ingestion data buffer ready for operation
     */
    public static IngestionDataBuffer   create(long szQueueCapacity, boolean bolBackPressure) {
        return new IngestionDataBuffer(szQueueCapacity, bolBackPressure);
    }
    
    
    
    //
    // Application Resources
    //
    
    /** The Ingestion Service client API default configuration */
    private static final DpIngestionConfig  CFG_DEFAULT = DpApiConfig.getInstance().ingest; 
    
    
    //
    // Class Constants - Default Values
    //
    
    /** Is logging active */
    private static final Boolean    BOL_LOGGING = CFG_DEFAULT.logging.active;

    
    /** Size of the ingestion frame queue buffer - used to create default maximum capacity allocation */
    private static final Integer    INT_BUFFER_SIZE = CFG_DEFAULT.stream.buffer.size;
    
    /** Size of the maximum allowable ingestion frame - used to create default maximum capacity allocation */
    private static final Integer    INT_MAX_FRAME_SIZE = CFG_DEFAULT.stream.binning.maxSize;
    
    /** Default maximum memory allocation */
    private static final Long       LNG_MAX_ALLOC = (long) (INT_BUFFER_SIZE * INT_MAX_FRAME_SIZE);
    
    
    /** Allow back pressure to client from queue buffer */
    private static final Boolean    BOL_BUFFER_BACKPRESSURE = CFG_DEFAULT.stream.buffer.backPressure;
    
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    
    //
    // Configuration Parameters
    //
    
    /** Exert back pressure on clients (from frame buffer) enabled flag */
    private boolean bolBackPressure; // = BOL_BUFFER_BACKPRESSURE;
    
    /** The capacity of the outgoing message queue (i.e., when back pressure is active) */ 
    private long    szQueueCapacity; // = INT_MAX_ALLOC;
    

    
    //
    // Instance Resources
    //
    
    
    /** The buffer of ingestion data used for staging before transmission */
    private final BlockingQueue<IngestDataRequest>  queMsgRequests = new LinkedBlockingQueue<>();
    
    
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
    
    /** Is supplier active (not been shutdown) */
    private boolean bolActive = false;
    
    /** Current queue memory allocation */
    private long    szQueueAlloc = 0;
    
    /** Has the supplier been shutdown */
    private boolean bolShutdown = false;
  
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionDataBuffer</code> with default parameters.
     * </p>
     * <p>
     * Ingestion message queue buffer memory allocation capacity and back-pressure enforcement is taken 
     * from the default values of the API library configuration.
     * </p>
     *
     * @see #LNG_MAX_ALLOC
     * @see #BOL_BUFFER_BACKPRESSURE
     */
    public IngestionDataBuffer() {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionDataBuffer</code> with the given queue buffer max allocation.
     * </p>
     * <p>
     * Back-pressure enforcement is taken from the default values of the API library configuration
     * </p>
     *
     * @param szQueueCapacity     maximum memory allocation capacity of queue buffer
     */
    public IngestionDataBuffer(long szQueueCapacity) {
        this(szQueueCapacity, BOL_BUFFER_BACKPRESSURE);
    }

    /**
     * <p>
     * Constructs a new instance of <code>IngestionDataBuffer</code> initialized with the given parameters.
     * </p>
     *
     * @param szQueueCapacity   maximum memory allocation capacity of queue buffer
     * @param bolBackPressure   enforce back pressure (implicit throttling) at <code>{@link #offer(List)}</code>
     */
    public IngestionDataBuffer(long szQueueCapacity, boolean bolBackPressure) {
        this.szQueueCapacity = szQueueCapacity;
        this.bolBackPressure = bolBackPressure;
    }
    
    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Sets the memory allocation capacity of the ingest data message queue buffer.
     * </p>
     * <p>
     * The queue capacity is the critical parameter for ingestion throttling, either implicit through
     * back-pressure blocking at <code>{@link #offer(List)}</code> or explicit throttling with
     * <code>{@link #awaitQueueReady()}</code>.  If the memory allocation within the queue
     * exceed the given value the throttling is activated.  In that case this <code>IngestionDataBuffer</code> 
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
     * 
     * @param szQueueCapacity  maximum memory allocation capacity of message buffer before back-pressure blocking
     */
    synchronized
    public void setQueueCapcity(long szQueCapacity) {
        this.szQueueCapacity = szQueCapacity;
    }
    
    /**
     * <p>
     * Enables client back pressure (implicit throttling) from finite capacity ingestion data buffer.
     * </p>
     * <p>
     * This feature is available to tune the streaming of large data amounts from ingest data request messages;
     * it allows back pressure from the Ingestion Service to be felt at the supplier side.
     * This buffer class maintains a buffer of ingestion request data message to be transmitted 
     * in order to cope with transmission spikes from the client.  Enabling this option
     * prevents clients from adding additional ingestion message when this buffer is full (at capacity).
     * (A full buffer indicates a backlog of processing within the Ingestion Service.)
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
     * @param szQueueCapacity  memory allocation capacity of frame buffer before back-pressure blocking
     */
    synchronized 
    public void enableBackPressure() {
        this.bolBackPressure = true;
    }
    
    /**
     * <p>
     * Disables client back pressure (implicit message throttling).
     * </p>
     * <p>
     * The back-pressure feature is available to tune the streaming of large data amounts within ingestion messages;
     * it allows back pressure from the Ingestion Service to be felt at the client side.
     * The message supplier class maintains a buffer of ingestion messages to be transmitted 
     * in order to cope with transmission spikes from the client.  Enabling this option
     * prevents clients from adding additional ingestion messages when this buffer is full.
     * (A full buffer indicates a backlog of processing within the Ingestion Service.)
     * </p>
     * <p>
     * <h2>Effect</h2>
     * Disabling client back pressure allows the incoming message buffer to expand indefinitely.
     * Clients can always add more ingestion messages regardless of any backlog in transmission and
     * processing at the Ingestion Service.
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
     * Returns the current maximum memory allocation capacity of the request message queue buffer.
     * </p>
     * 
     * @return  the maximum memory allocation capacity of the queue buffer before throttling is invoking
     */
    public long getQueueCapacity() {
        return this.szQueueCapacity;
    }

    /**
     * <p>
     * Returns the current size of the queue buffer.
     * </p>
     * <p>
     * Returns the number of request messages in the queue buffer at the time of invocation.  Note that this
     * is inherently a dynamic quantity.
     * </p>
     *  
     * @return  number of request messages currently in the queue
     */
    public int  getQueueSize() {
        return this.queMsgRequests.size();
    }
    
    /**
     * <p>
     * Returns the current memory allocation within the queue buffer.
     * </p>
     * <p>
     * Returns the memory allocation (in bytes) of all the request messages within the data buffer at the time
     * of allocation.  Note that this quantity is inherently a dynamic quantity.
     * </p>
     * 
     * @return  memory allocation of all request data messages within the queue buffer (in bytes)
     */
    public long getQueueAllocation() {
        return this.szQueueAlloc;
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Allows clients to explicitly block until the request queue buffer is ready (below capacity) 
     * and back pressure is relieved.
     * </p>
     * <p>
     * This method allows clients to explicitly wait for back-pressure relief.  This method blocks
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
     * <s>@throws IllegalStateException    operation invoked while supplier inactive</s>
     * @throws InterruptedException     operation interrupted while waiting for queue ready
     */
    public void awaitQueueReady() throws /* IllegalStateException,*/ InterruptedException {
        
//        // Check if active - if deactivated will wait forever.
//        if (!this.bolActive)
//            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - supplier is no longer active.");

        // Do this regardless of whether back pressure is active or not
        //  The client wants to wait, we wait
        this.lckMsgQueReady.lock();
        try {
            // Return immediately if the queue is not at capacity
            if (this.szQueueAlloc < this.szQueueCapacity)
                return;
            
            else {
                
                // Log event
                if (BOL_LOGGING)
                    LOGGER.info("{}: Blocking on queue allocation {} > capacity {} (back pressure event).", JavaRuntime.getCallerName(), this.szQueueAlloc, this.szQueueCapacity);
            
                // Wait for queue ready signal
                this.cndMsgQueReady.await();
            }
            
        } finally {
            this.lckMsgQueReady.unlock();
        }
    }
    
    /**
     * <p>
     * Allows clients to block until the request message queue queue buffer completely empties.
     * </p>
     * <p>
     * This method allows clients to wait for the <code>IngestDataRequest</code> queue buffer
     * to fully empty.  Clients can add a fixed number of ingestion request messages then measure 
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
     * #@throws IllegalStateException    operation invoked while processor inactive
     * @throws InterruptedException     operation interrupted while waiting for queue ready
     */
    public void awaitQueueEmpty() throws /* IllegalStateException,*/ InterruptedException {

        // Check if active - if deactivated will wait forever.
//        if (!this.bolActive)
//            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - supplier is no longer active.");

        // Get the request message queue empty lock 
        this.lckMsgQueEmpty.lock();
        try {
            // Return immediately if all queues are empty and nothing is pending 
            if (this.queMsgRequests.isEmpty())
                return;
            
            else {
                
                // Log event
                if (BOL_LOGGING)
                    LOGGER.info("{}: Waiting for queue empty, queue size = {}.", JavaRuntime.getCallerName(), this.queMsgRequests.size());
            
                // Wait for queue ready signal
                this.cndMsgQueEmpty.await();
            }
            
        } finally {
            this.lckMsgQueEmpty.unlock();
        }
    }
    

    //
    // IResourceConsumer<IngestDataRequest> Interface
    //
    
    /**
     *
     * @see @see com.ospreydcs.dp.api.ingest.model.IResourceConsumer#isAccepting()
     */
    @Override
    public boolean isAccepting() {
        return this.bolActive;
    }

    /**
     * <p>
     * Activates the <code>IngestionMessageBuffer</code> message queue buffer.
     * </p>
     * <p>
     * After invoking this method the message supplier instance is ready for ingestion data request
     * acceptance via <code>{@link #offer(List)}</code>.  
     * Ingestion request messages are added to the buffer where they
     * are staged for transmission via the <code>IMessageSupplier&lt;IngestDataRequest&gt;</code> interface.
     * (Request messages are available to consumers through the
     * <code>{@link IMessageSupplier}</code> interface blocking methods <code>{@link IMessageSupplier#take()}</code>,
     * <code>{@link IMessageSupplier#poll()}</code>, and <code>{@link IMessageSupplier#poll(long, TimeUnit)}</code>.)
     * Implicit or explicit throttling operations are available via the queue capacity parameter.
     * </p>
     * <h2>Operation</h2>
     * This method enables all queue buffer tasks which are then continuously active
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
     * @return  <code>true</code> if the ingestion message queue buffer was successfully activated,
     *          <code>false</code> if the message supplier was already active
     */
    @Override
    synchronized
    public boolean activate() {
        
        // Check if already active
        if (this.bolActive)
            return false;

        // Set activation flag 
        this.bolActive = true;

        return true;
    }
    
    /**
     * <p>
     * Performs an orderly shut down of the queue buffer and message supplier interface.
     * </p>
     * <p>
     * The <code>{@link #bolActive}</code> flag is set <code>false</code> preventing any new
     * <code>IngestDataRequest</code> to be added to the queue.  However, consumers of the
     * <code>IMessageSupplier&lt;IngestDataRequest&gt;</code> interface can continue to 
     * request messages so long as messages are available.
     * That is, consumers of messages can continue to poll for available messages until the supply
     * is exhausted.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This method will block until all the message queue is completely exhausted.
     * Thus, message consumers <em>must remain active after invoking this method</em> as any remaining messages
     * left in the queue will cause this method to block indefinitely. 
     * </p>
     * <p>
     * To force a hard shutdown of the queue buffer where any remaining messages are discarded use method
     * <code>#shutdownNow()</code>.  This is appropriate when the message consumer is known to be inactive
     * and the queue buffer still contains messages.
     * </p>
     * 
     * @return <code>true</code> if the message supplier was successfully shutdown,
     *         <code>false</code> if the message supplier was not active or shutdown operation failed
     * 
     * @throws InterruptedException interrupted while waiting for processing threads to complete
     */
    @Override
    synchronized
    public boolean shutdown() throws InterruptedException {
        
        // Check state
        if (!this.bolActive)
            return false;
        
        // This will allow ingestion data messages to be consumed until the queue is exhausted
        this.bolActive = false;
        this.awaitQueueEmpty();
        
        this.bolShutdown = true;
        
        return true;
    }
    
    /**
     * <p>
     * Performs a hard shutdown of the queue buffer and message supplier interface.
     * </p>
     * <p>
     * The <code>{@link #bolActive}</code> flag is set to <code>false</code> and all
     * <code>IngestDataRequest</code> messages are cleared from the queue buffer thus
     * terminating the <code>IMessageSupplier&lt;IngestDataRequest&gt;</code> interface.
     * That is, the outgoing message queue is cleared.  
     * The method returns immediately upon terminating all activity.
     * </p>
     */
    @Override
    synchronized
    public void shutdownNow() {
        
        this.bolActive = false;
        this.queMsgRequests.clear();
        this.szQueueAlloc = 0;
        this.bolShutdown = true;
    }
    
    /**
     * <p>
     * Adds the given ingestion data request message to the head of the queue buffer.
     * </p>
     * <p>
     * This is a convenience method for single message enqueueing.  All operations are 
     * deferred to <code>{@link #offer(List)}</code> after converting the argument to a single-element
     * list.
     * </p>
     *  
     * @param lstMsgRqsts  ordered list of ingest data requests for staging
     * 
     * @throws IllegalStateException    the queue buffer is currently inactive
     * @throws InterruptedException     interrupted while waiting for message buffer ready (back-pressure enabled)
     * 
     * @see #offer(List)
     */
    @Override
    synchronized
    public void offer(IngestDataRequest msgRqst) throws IllegalStateException, InterruptedException {
        this.offer(List.of(msgRqst));
    }
    
    /**
     * <p>
     * Add the given list of ingestion request messages to the queue buffer for transmission staging.
     * </p>
     * <p>
     * The given list is added to the queue buffer of ingestion data.  All ingestion data request messages
     * entering the queue buffer are then are then available for transmission through the 
     * <code>{@link IMessageSupplier}</code> interface. Note that an exception is thrown if the queue buffer
     * has not been activated.
     * </p>
     * <p>
     * <h2>Back Pressure - Implicit Throttling</h2>
     * If the back-pressure feature is enable this method blocks whenever the outgoing message
     * queue buffer is at capacity.  The method will not return until the consumer of 
     * <code>IngestDataRequest</code> messages has taken the queue below capacity.
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
     * <li>Invoke the <code>{@link #offer(List)}</code> method to stage ingestion messages.</li>  
     * </ol>
     * Clearly the first step need only be executed once then steps 2 and 3 are executed repeatedly as required 
     * for all additional data.
     * </p>
     * 
     * @param lstMsgRqsts  ordered list of ingest data requests for staging
     * 
     * @throws IllegalStateException    the queue buffer is currently inactive
     * @throws InterruptedException     interrupted while waiting for message buffer ready (back-pressure enabled)
     */
    @Override
    synchronized
    public void offer(List<IngestDataRequest> lstMsgRqsts) throws IllegalStateException, InterruptedException {

        // Check if active
        if (!this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - queue buffer is not active.");
        
        // Compute the the memory allocation of the request messages
        long    szAlloc = lstMsgRqsts.stream().mapToLong(msg -> msg.getSerializedSize()).sum();
        
        // If no back-presssure enforcement just add all frames to raw frame buffer and return
        if (!this.bolBackPressure) {
            this.queMsgRequests.addAll(lstMsgRqsts);
            this.szQueueAlloc += szAlloc;
            
            return;
        }
        
        // Enforce back-pressure to the client if processed frame queue is full 
        this.lckMsgQueReady.lock();
        try {
            if (this.queMsgRequests.size() >= this.szQueueCapacity) {
             
                // Log event
                if (BOL_LOGGING)
                    LOGGER.info("{}: Blocking on queue size {} > capacity {} (back pressure event).", JavaRuntime.getCallerName(), this.queMsgRequests.size(), this.szQueueCapacity);
            
                // Wait for queue ready signal
                this.cndMsgQueReady.await();  // throws InterruptedException
            }

            this.queMsgRequests.addAll(lstMsgRqsts);
            this.szQueueAlloc += szAlloc;

        } finally {
            this.lckMsgQueReady.unlock();
        }
    }
    
    /**
     * <p>
     * Add the given list of ingestion request messages to the queue buffer for transmission staging.
     * </p>
     * <p>
     * The given list is added to the queue buffer of ingestion data.  All ingestion data request messages
     * entering the queue buffer are then are then available for transmission through the 
     * <code>{@link IMessageSupplier}</code> interface. Note that an exception is thrown if the queue buffer
     * has not been activated.
     * </p>
     * <p>
     * <h2>Back Pressure - Implicit Throttling</h2>
     * If the back-pressure feature is enable this method blocks whenever the outgoing message
     * queue buffer is at capacity.  The method will not return until the consumer of 
     * <code>IngestDataRequest</code> messages has taken the queue below capacity, or the given
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
     * <li>Invoke the <code>{@link #offer(List)}</code> method to stage ingestion messages.</li>  
     * </ol>
     * Clearly the first step need only be executed once then steps 2 and 3 are executed repeatedly as required 
     * for all additional data.
     * </p>
     * 
     * @param lstMsgRqsts  ordered list of ingest data requests for staging
     * @param lngTimeout    timeout limit to wait for message acceptance if back-pressure is enabled
     * @param tuTimeout     timeout units for message acceptance under back pressure.
     * 
     * @return  <code>true</code> if the ingest data requests were successfully accepted,
     *          <code>false</code> if the timeout limit was exceeded (messages were not accepted)
     * 
     * @throws IllegalStateException    the queue buffer is currently inactive
     * @throws InterruptedException     interrupted while waiting for message buffer ready (back-pressure enabled)
     * @param lstMsgRqsts
     *
     * @see com.ospreydcs.dp.api.ingest.model.IResourceConsumer#offer(java.util.List, long, java.util.concurrent.TimeUnit)
     */
    @Override
    public boolean offer(List<IngestDataRequest> lstMsgRqsts, long lngTimeout, TimeUnit tuTimeout)
            throws IllegalStateException, InterruptedException {

        // Check if active
        if (!this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - queue buffer is not active.");
        
        // Compute the the memory allocation of the request messages
        long    szAlloc = lstMsgRqsts.stream().mapToLong(msg -> msg.getSerializedSize()).sum();
        
        // If no back-presssure enforcement just add all frames to raw frame buffer and return
        if (!this.bolBackPressure) {
            this.queMsgRequests.addAll(lstMsgRqsts);
            this.szQueueAlloc += szAlloc;
            
            return true;
        }
        
        // Enforce back-pressure to the client if processed frame queue is full 
        this.lckMsgQueReady.lock();
        try {
            boolean bolResult = false;
            
            if (this.queMsgRequests.size() >= this.szQueueCapacity) {
             
                // Log event
                if (BOL_LOGGING)
                    LOGGER.info("{}: Blocking on queue size {} > capacity {} (back pressure event).", JavaRuntime.getCallerName(), this.queMsgRequests.size(), this.szQueueCapacity);
            
                // Wait for queue ready signal
                bolResult = this.cndMsgQueReady.await(lngTimeout, tuTimeout);  // throws InterruptedException
            }

            this.queMsgRequests.addAll(lstMsgRqsts);
            this.szQueueAlloc += szAlloc;
            
            return bolResult;

        } finally {
            this.lckMsgQueReady.unlock();
        }
    }
    
    
    //
    // IMessageSupplier<IngestDataRequest> Interface
    //
    
    /**
     * <p>
     * Determines whether or not <code>IngestDataRequest</code> messages are currently being supplied. 
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
     * @return <code>true</code> if there are currently request messages available or pending
     *         <code>false</code> otherwise
     *
     * @see com.ospreydcs.dp.api.ingest.model.IMessageSupplier#isSupplying()
     */
    @Override
    public boolean isSupplying() {
        return this.bolActive || !this.queMsgRequests.isEmpty();
    }

    /**
     *
     * @see com.ospreydcs.dp.api.ingest.model.IMessageSupplier#take()
     */
    @Override
    public IngestDataRequest take() throws IllegalStateException, InterruptedException {
        
        // Check states
        if (!this.bolActive && this.queMsgRequests.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - supplier is inactive and queue is empty.");
        
        try {
            IngestDataRequest   msgRqst = this.queMsgRequests.take();
            
            if (msgRqst == null)
                return null;
            
            // Compute allocation and adjust current capacity
            synchronized (this.objLock) {
                this.szQueueAlloc -= msgRqst.getSerializedSize();
            }
            
            return msgRqst;
            
        } finally {
            
            // We need to signal any threads blocking on the queue capacity
            this.signalRequestQueueConditions();
        }
    }

    /**
     *
     * @see com.ospreydcs.dp.api.ingest.model.IMessageSupplier#poll()
     */
    @Override
    public IngestDataRequest poll() throws IllegalStateException {

        // Check state
        if (!this.bolActive && this.queMsgRequests.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - supplier is inactive and queue is empty.");
        
        try {
            IngestDataRequest   msgRqst = this.queMsgRequests.poll();
            
            if (msgRqst == null)
                return null;
            
            // Compute allocation and adjust current capacity
            synchronized (this.objLock) {
                this.szQueueAlloc -= msgRqst.getSerializedSize();
            }
            
            return msgRqst;
            
        } finally {
            
            // We need to signal any threads blocking on the queue capacity
            this.signalRequestQueueConditions();
        }
    }

    /**
     *
     * @see com.ospreydcs.dp.api.ingest.model.IMessageSupplier#poll(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public IngestDataRequest poll(long cntTimeout, TimeUnit tuTimeout)
            throws IllegalStateException, InterruptedException {
        
        // Check state
        if (!this.bolActive && this.queMsgRequests.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - supplier is inactive and queue is empty.");
        
        try {
            IngestDataRequest   msgRqst = this.queMsgRequests.poll(cntTimeout, tuTimeout);
            
            if (msgRqst == null)
                return null;
            
            // Compute allocation and adjust current capacity
            synchronized (this.objLock) {
                this.szQueueAlloc -= msgRqst.getSerializedSize();
            }
            
            return msgRqst;
            
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
     * To be called when a a request queue message removal event occurs. 
     * Checks the current size of the message queue <code>{@link #queMsgRequests}</code>
     * and signals all threads with lock <code>{@link #lckMsgQueReady}</code> for the
     * following conditions:
     * <ul>
     * <li>If queue memory allocation (<code>{@link #szQueueAlloc}</code>) < capacity then signal <code>{@link #cndMsgQueReady}</code>.</li>
     * <li>If queue size = 0 then signal <code>{@link #cndMsgQueEmpty}</code>.</li>
     * </ul>
     * </p>
     */
    private void signalRequestQueueConditions() {
        
        // Signal any threads blocking on the queue ready condition
        this.lckMsgQueReady.lock();
        try {
            if (this.szQueueAlloc < this.szQueueCapacity)
                this.cndMsgQueReady.signalAll();
            
        } finally {
            this.lckMsgQueReady.unlock();
        }
        
        // Signal any threads blocking on the queue empty condition
        this.lckMsgQueEmpty.lock();
        try {
            if (this.queMsgRequests.isEmpty())
                this.cndMsgQueEmpty.signalAll();

        } finally {
            this.lckMsgQueEmpty.unlock();
        }
    }

}
