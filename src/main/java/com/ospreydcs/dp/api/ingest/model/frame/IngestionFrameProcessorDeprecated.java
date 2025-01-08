/*
 * Project: dp-api-common
 * File:	IngestionFrameProcessorDeprecated.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IngestionFrameProcessorDeprecated
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
 * @since Apr 10, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.frame;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.model.IMessageSupplier;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * <h1>Processor of <code>IngestionFrame</code> instances and supplier of 
 * <code>IngestDataRequest</code> messages from processed ingestion frames.</h1>
 * </p>
 * <p>
 * This class performs several functions, including ingetion frame buffering, ingestion frame
 * decomposition, and conversion of ingestion frames to <code>IngestDataRequest</code> messages.
 * The ultimate use for class instances is as a blocking queue supplier of 
 * <code>IngestDataRequest</code> messages.
 * </p>
 * <p>
 * <h2>Activation</h2>
 * A <code>IngestionFrameProcessorDeprecated</code> instance must be activated before attempting
 * to add ingestion frames; use the method <code>{@link #activate()}</code>.  Likewise,
 * an active processor should be shutdown when no longer needed; use methods
 * <code>{@link #shutdown()}</code> or <code>{@link #shutdownNow()}</code>.
 * </p>
 * <p>
 * <h2>Processing Options</h2>
 * The <code>IngestionFrameProcessorDeprecated</code> class has several processing options which
 * should be set before activation.
 * <ul>
 * <li>
 * <code>{@link #enableConcurrency(int)}</code> - Processes ingestion frames using
 * multiple concurrent processing threads, otherwise all frame processing is done on 
 * single execution threads.
 * </li>
 * <br/>
 * <li>
 * <code>{@link #enableFrameDecomposition(long)}</code> - All incoming ingestion frames
 * are check for memory allocation size.  If a frame has allocation larger than the given
 * size it is decomposed in a collection of equivalent, decompose frame each meeting the
 * size requirement. 
 * </li>
 * <br/>
 * <li>
 * <code>{@link #enableBackPressure(int)}</code> - The ability to add ingestion frames
 * to an <code>IngestionFrameProcessorDeprecated</code> can be blocked when using this option.
 * Specifically, when the outgoing (processed) message queue reaches the given capacity
 * the <code>IngestionFrameProcessorDeprecated</code> will block when adding additional frames.
 * The blocking continues until the outgoing message queue drops below capacity.
 * In this fashion, clients will experience "back pressure" from consumers of 
 * <code>IngestDataRequest</code> messages when they become backlogged.
 * </li>
 * </ul>
 * The default settings for all the above options are taken from the client API configuration
 * parameters. 
 * </p> 
 * <p>
 * <h2><code>IngestDataRequest<code> Message Blocking Supplier</h2>
 * Class instances function as a supplier of <code>{@link IngestDataRequest}</code> messages
 * which are created from <code>{@link IngestionFrame}</code> objects offered to the instance.
 * Consumers of <code>IngestDataRequest</code> messages can poll a message supplier for messages,
 * or wait for messages to become available.
 * </p> 
 * <p>
 * <h2>Ingestion Frame Buffering and Back Pressure</h2>
 * The message supplier class maintains a buffer of ingestion frames to be transmitted 
 * in order to cope with transmission spikes from the client.  This buffer is unbounded
 * when back pressure is turned off and is bounded by the client API configuration parameter
 * when back pressure is turned on.
 * </p>
 * <p>  
 * Enabling the back pressure option
 * prevents clients from adding additional ingestion frames when this buffer is full.
 * This feature is available to tune the streaming of large numbers of ingestion frames;
 * it allows back pressure from the Ingestion Service to be felt at the client side.
 * (A full buffer indicates a backlog of processing within the Ingestion Service.)
 * </p>
 * <p>
 * <h2>Automatic Ingestion Frame Decomposition</h2>
 * <p>
 * When frame decomposition is active any ingestion frame added to this supplier
 * is decomposed so that the total memory allocation is less than the gRPC message
 * size limitation identified in the client API configuration parameters.  Thus, a single, large
 * ingestion frame added to this supplier will be decomposed into multiple smaller ingestion
 * frame, each meeting the gRPC message size limitation specified in the client API configuration
 * parameters.
 * </p>
 * <p>
 * <h2>Concurrency</h2>
 * The frame decomposition operations can be performed concurrently.  Specifically, if multiple
 * ingestion frames are offered at the same time each decomposition will be assigned a separate
 * execution thread (up to the concurrency limit).
 * </p>
 * <p>
 * <h2>WARNINGS:</h2>
 * If automatic ingestion frame decomposition is turned of it is imperative that all
 * offered ingestion frames have memory allocations less than the current gRPC message
 * size limit or gRPC will throw a runtime exception when attempting to transmit the 
 * generated <code>IngestDataRequest</code> message.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * This class uses instances of <code>{@link IngestionFrameDecomposer}</code> for frame decomposition
 * and instances of class <code>{@link IngestionFrameConverter}</code> for frame conversion
 * to ingest data request messages.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Apr 10, 2024
 *
 * @see IngestionFrameDecomposer
 * @see IngestionFrameConverter
 * 
 * @deprecated  Ingestion frame processing was refactored and this class was decomposed into multiple (less complicated) classes
 */
@Deprecated(since="July 25, 2024", forRemoval=true)
public final class IngestionFrameProcessorDeprecated implements IMessageSupplier<IngestDataRequest> {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new instance of <code>IngestionFrameProcessorDeprecated</code> ready for processing
     * of ingestion frames.
     * </p>
     * <p>
     * The data provider UID given as the argument is assigned to all 
     * <code>IngestDataReuest</code> messages supplied by the returned instance.
     * </p>
     * <p>
     * <h2>Activation</h2>
     * A <code>IngestionFrameProcessorDeprecated</code> instance must be activated before attempting
     * to add ingestion frames; use the method <code>{@link #activate()}</code>.  Likewise,
     * an active processor should be shutdown when no longer needed; use methods
     * <code>{@link #shutdown()}</code> or <code>{@link #shutdownNow()}</code>.
     * </p>
     * <p>
     * <h2>Processing Options</h2>
     * The <code>IngestionFrameProcessorDeprecated</code> class has several processing options which
     * should be set before activation.
     * <h2>Processing Options</h2>
     * The <code>IngestionFrameProcessorDeprecated</code> class has several processing options which
     * should be set before activation.
     * <ul>
     * <li>
     * <code>{@link #enableConcurrency(int)}</code> - Processes ingestion frames using
     * multiple concurrent processing threads, otherwise all frame processing is done on 
     * single execution threads.
     * </li>
     * <br/>
     * <li>
     * <code>{@link #enableFrameDecomposition(long)}</code> - All incoming ingestion frames
     * are check for memory allocation size.  If a frame has allocation larger than the given
     * size it is decomposed in a collection of equivalent, decompose frame each meeting the
     * size requirement. 
     * </li>
     * <br/>
     * <li>
     * <code>{@link #enableBackPressure(int)}</code> - The ability to add ingestion frames
     * to an <code>IngestionFrameProcessorDeprecated</code> can be blocked when using this option.
     * Specifically, when the outgoing (processed) message queue reaches the given capacity
     * the <code>IngestionFrameProcessorDeprecated</code> will block when adding additional frames.
     * The blocking continues until the outgoing message queue drops below capacity.
     * In this fashion, clients will experience "back pressure" from consumers of 
     * <code>IngestDataRequest</code> messages when they become backlogged.
     * </li>
     * </ul>
     * The default settings for all the above options are taken from the client API configuration
     * parameters. 
     * </p> 
     * 
     * @param recProviderUid data provider unique identifier assigned to all <code>IngestDataRequest</code> messages
     * 
     * @return new <code>IngestionFrameProcessorDeprecated</code> instance ready for processing
     */
    public static IngestionFrameProcessorDeprecated from(ProviderUID recProviderUid) {
        return new IngestionFrameProcessorDeprecated(recProviderUid);
    }
    
    
    //
    // Application Resources
    //
    
    /** The Ingestion Service client API default configuration */
    private static final DpIngestionConfig  CFG_DEFAULT = DpApiConfig.getInstance().ingest; 
    
    
    //
    // Class Constants
    //
    
    /** Timeout limit to wait for thread worker polling operations */
    private static final int       INT_TIMEOUT_TASK_POLL = 15;
    
    /** Timeout units to wait for thread worker polling operations */
    private static final TimeUnit  TU_TIMEOUT_TASK_POLL = TimeUnit.MILLISECONDS;
    
    
    /** General operation timeout limit */
    private static final long       LNG_TIMEOUT_GENERAL = CFG_DEFAULT.timeout.limit;
    
    /** General operation timeout units */
    private static final TimeUnit   TU_TIMEOUT_GENERAL = CFG_DEFAULT.timeout.unit;
    
    
    /** Is logging active */
    private static final Boolean    BOL_LOGGING = CFG_DEFAULT.logging.active;

    
    //
    // Class Constants - Default Values
    //
    
    /** Are general concurrency active - used for ingestion frame decomposition */
    private static final Boolean    BOL_CONCURRENCY_ACTIVE = CFG_DEFAULT.concurrency.active;
    
//    /** Thresold in which to pivot to concurrent processing */
//    private static final Integer    INT_CONCURRENCY_PIVOT_SZ = CFG_DEFAULT.concurrency.pivotSize;
    
    /** Maximum number of concurrent processing threads */
    private static final Integer    INT_CONCURRENCY_CNT_THREADS = CFG_DEFAULT.concurrency.threadCount;
    
    
    /** Perform ingestion frame decomposition (i.e., "binning") */
    private static final Boolean    BOL_BINNING_ACTIVE = CFG_DEFAULT.decompose.active;
    
    /** Maximum size limit (in bytes) of decomposed ingestion frame */
    private static final Integer    LNG_BINNING_MAX_SIZE = CFG_DEFAULT.decompose.maxSize;
    
    
//    /** Use ingestion frame buffering from client to gRPC stream */
//    private static final Boolean    BOL_BUFFER_ACTIVE = CFG_DEFAULT.stream.buffer.active;
    
    /** Size of the ingestion frame queue buffer */
    private static final Integer    INT_BUFFER_SIZE = CFG_DEFAULT.stream.buffer.size;
    
    /** Allow back pressure to client from queue buffer */
    private static final Boolean    BOL_BUFFER_BACKPRESSURE = CFG_DEFAULT.stream.buffer.backPressure;
    
    
    //
    // Class Resources
    //
    
    /** The class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
//    /** The locking object for synchronizing access to class resources */ 
//    private static final Object   objClassLock = new Object();
//    
//    /** The number of binned frames produced - used for request ID creation */
//    private static long cntFrames = 0L;

    
    //
    // Defining Attributes
    //
    
    /** The data provider unique identifier for all <code>IngestDataRequest</code> messages supplied */
    private final ProviderUID   recProviderUid;
    
    
    //
    // Configuration Parameters
    //
    
    /** Processing concurrency enabled flag */
    private boolean bolConcurrency = BOL_CONCURRENCY_ACTIVE;
    
    /** The number of worker threads used for concurrent processing */
    private int     cntConcurrencyThrds = INT_CONCURRENCY_CNT_THREADS;
    
    
    /** Ingestion frame decomposition (binning) enabled flag  */
    private boolean bolDecompAuto = BOL_BINNING_ACTIVE;
    
    /** Ingestion frame decomposition maximum size */
    private long    lngBinSizeMax = LNG_BINNING_MAX_SIZE;
    
    
    /** Exert back pressure on clients (from frame buffer) enabled flag */
    private boolean bolBackPressure = BOL_BUFFER_BACKPRESSURE;
    
    /** The capacity of the outgoing message queue (i.e., when back pressure is active) */ 
    private int     intQueueCapacity = INT_BUFFER_SIZE;
    
    
    //
    // State Variables
    //
    
    /** Is supplier active (not been shutdown) */
    private boolean bolActive = false;
    
    /** Has the supplier been shutdown */
    private boolean bolShutdown = false;
  
    
    /** DEBUG - The number (and ID generator) of frame decomposition threads */
    private int     cntDecompThrds = 0;
    
    /** DEBUG - The number (and ID generator) of frame-to-message conversion tasks */
    private int     cntConvertThrds = 0;
    
    
    //
    // Instance Resources
    //
    
    /** Incoming ingestion frame buffer */
    private final BlockingQueue<IngestionFrame>     queFramesRaw = new LinkedBlockingQueue<>();
    
    /** Processed (e.g., decomposed) frame queue ready for frame-to-message conversion */ 
    private final BlockingQueue<IngestionFrame>     queFramesPrcd = new LinkedBlockingQueue<>();
    
    /** Outgoing ingestion request message queue */
    private final BlockingQueue<IngestDataRequest>  queMsgRequests = new LinkedBlockingQueue<>();

    
    /** The pool of frame decomposition threads */
    private final ExecutorService               xtorDecompTasks = Executors.newCachedThreadPool();
    
    /** The pool of frame-to-message conversion threads */
    private final ExecutorService               xtorConvertTasks = Executors.newCachedThreadPool();
    
    /** Collection of future results from decomposition tasks - used in shutdown */
    private final Collection<Future<Boolean>>   setDecompFutures = new LinkedList<>();

    /** Collection of future results from frame-to-message conversion tasks - used in shutdown */
    private final Collection<Future<Boolean>>   setConvertFutures = new LinkedList<>();

    
    /** Pending processing operation counter - managed by processing threads */
    private final AtomicInteger     cntPending = new AtomicInteger(0);
    
    /** The message queue buffer back pressure lock */
    private final Lock              lckMsgQueReady = new ReentrantLock();
    
    /** The message queue buffer ready (or "not full") lock condition */
    private final Condition         cndMsgQueReady = lckMsgQueReady.newCondition();
    
    /** The message queue buffer empty lock */
    private final Lock              lckMsgQueEmpty = new ReentrantLock();
    
    /** The message queue buffer empty lock condition */
    private final Condition         cndMsgQueEmpty = lckMsgQueEmpty.newCondition();
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionFrameProcessorDeprecated</code>.
     * </p>
     * <p>
     * Note that the data provider UID given here is used within all <code>IngestDataReuest</code>
     * messages supplied by this instance.
     * </p>
     *
     * @param recProviderUid    the data provider unique identifier
     */
    public IngestionFrameProcessorDeprecated(ProviderUID recProviderUid) {
        this.recProviderUid = recProviderUid;
    }

    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Enables the use of concurrent thread for internal processing.
     * </p>
     * <p>
     * Enables the use of multiple, concurrent execution threads for ingestion frame processing 
     * (e.g., frame decomposition, frame-to-message conversion, etc.).
     * If concurrency is disabled all processing is done on a single execution thread 
     * potentially blocking until complete.
     * </p>
     * <p>
     * <h2>Thread Safety</h2>
     * This method is synchronized for thread safety.  Changing configuration parameters must
     * be done atomically.  Thus, this configuration parameter 
     * will not be changed until this method acquires the <code>this</code> lock from any other
     * competing threads.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This configuration parameter can only be modified <em>before</em> the processor is 
     * activated with <code>{@link #activate()}</code> , otherwise an exception is throw.
     * </p>
     * 
     * @param cntThreads    the maximum number of independent processing threads for concurrent operations
     * 
     * @throws IllegalStateException    method called while processor is active
     */
    synchronized 
    public void enableConcurrency(int cntThreads) throws IllegalStateException {
        
        if (this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Cannot change concurency once activated.");
        
        this.bolConcurrency = true;
        this.cntConcurrencyThrds = cntThreads;
    }
    
    /**
     * <p>
     * Disables the use of concurrent thread for internal processing.
     * </p>
     * <p>
     * Diaables the use of multiple, concurrent execution threads for ingestion frame processing 
     * (e.g., frame decomposition, frame-to-message conversion, etc.).
     * If concurrency is disabled all processing is done on a single execution thread 
     * potentially blocking until complete.
     * </p>
     * <p>
     * <h2>Thread Safety</h2>
     * This method is synchronized for thread safety.  Changing configuration parameters must
     * be done atomically.  Thus, this configuration parameter 
     * will not be changed until this method acquires the <code>this</code> lock from any other
     * competing threads.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This configuration parameter can only be modified <em>before</em> the processor is 
     * activated with <code>{@link #activate()}</code> , otherwise an exception is throw.
     * </p>
     * 
     * @throws IllegalStateException    method called while processor is active
     */
    synchronized
    public void disableConcurrency() throws IllegalStateException {

        if (this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Cannot change concurency once activated.");
        
        this.bolConcurrency = false;
    }
    
    /**
     * <p>
     * Enables automatic ingestion frame decomposition to the given maximum allocation size.
     * </p>
     * <p>
     * Enables the automatic decomposition of ingestion frames (i.e., "frame binning").
     * When frame decomposition is active any ingestion frame added to this supplier
     * is decomposed so that the total memory allocation is less than the given size.
     * </p>
     * <p> 
     * <h2>gRPC Message Sizes</h2>
     * The gRPC framework limits the maximum size of any transmitted message.  The default
     * size limitation is 2<sup>22</sup> = 4,194,304 bytes.  However, it is possible to 
     * change this value which must be done both at the client and server ends.
     * (The client API library has a configuration parameter for this value to change it
     * at the client side.)   
     * If the given value is larger than the current gRPC message size limitation (identified 
     * in the client API configuration parameters) any gRPC transmission of supplied messages
     * will likely yield a runtime exception.
     * </p>
     * <p>
     * <h2>Thread Safety</h2>
     * This method is synchronized for thread safety.  Changing configuration parameters must
     * be done atomically.  Thus, this configuration parameter 
     * will not be changed until this method acquires the <code>this</code> lock from any other
     * competing threads.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This maximum ingestion frame size can only be modified <em>before</em> the processor is 
     * activated with <code>{@link #activate()}</code>. Calling this method post activation
     * will return ingestion frame composition to the bin size set before activation.
     * </p>
     * 
     * @param lngMaxBinSize maximum allowable size (in bytes) decomposed ingestion frames 
     */
    synchronized 
    public void enableFrameDecomposition(long lngMaxBinSize) {

//        if (this.bolActive)
//            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change concurency once activated.");
        
        this.bolDecompAuto = true;
        this.lngBinSizeMax = lngMaxBinSize;
    }
    
    /**
     * <p>
     * Disables automatic ingestion frame decomposition.
     * </p>
     * <p>
     * Disables the automatic decomposition of ingestion frames (i.e., "frame binning").
     * When frame decomposition is active any ingestion frame added to this supplier
     * is decomposed so that the total memory allocation is less than the given size.
     * </p>
     * <p> 
     * <h2>gRPC Message Sizes</h2>
     * The gRPC framework limits the maximum size of any transmitted message.  The default
     * size limitation is 2<sup>22</sup> = 4,194,304 bytes.  However, it is possible to 
     * change this value which must be done both at the client and server ends.
     * (The client API library has a configuration parameter for this value to change it
     * at the client side.)   
     * If the given value is larger than the current gRPC message size limitation (identified 
     * in the client API configuration parameters) any gRPC transmission of supplied messages
     * will likely yield a runtime exception.
     * </p>
     * <p>
     * <h2>Thread Safety</h2>
     * This method is synchronized for thread safety.  Changing configuration parameters must
     * be done atomically.  Thus, this configuration parameter 
     * will not be changed until this method acquires the <code>this</code> lock from any other
     * competing threads.
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * <ul>
     * <li>
     * If automatic ingestion frame decomposition is disabled it is imperative that all
     * offered ingestion frames have memory allocations less than the current gRPC message
     * size limit or gRPC will throw a runtime exception.
     * </li>
     * <br/>
     * <li>
     * The maximum ingestion frame size can only be modified <em>before</em> the processor is 
     * activated with <code>{@link #activate()}</code>.  This method will disable frame
     * decomposition if already active, but the maximum frame size cannot be changed post
     * activation.
     * </li>
     * </p>
     * 
     */
    synchronized
    public void disableFrameDecomposition() {
        this.bolDecompAuto = false;
    }
    
    /**
     * <p>
     * Enables client back pressure from finite capacity ingestion frame buffer.
     * </p>
     * <p>
     * This feature is available to tune the streaming of large numbers of ingestion frames;
     * it allows back pressure from the Ingestion Service to be felt at the client side.
     * The message supplier class maintains a buffer of ingestion frames to be transmitted 
     * in order to cope with transmission spikes from the client.  Enabling this option
     * prevents clients from adding additional ingestion frames when this buffer is full.
     * (A full buffer indicates a backlog of processing within the Ingestion Service.)
     * </p>
     * <p>
     * <h2>Thread Safety</h2>
     * This method is synchronized for thread safety.  Changing configuration parameters must
     * be done atomically.  Thus, this configuration parameter 
     * will not be changed until this method acquires the <code>this</code> lock from any other
     * competing threads.
     * </p>
     * 
     * @param intQueueCapacity  capacity of frame buffer before back-pressure blocking
     */
    synchronized 
    public void enableBackPressure(int intMaxBufferSize) {

        this.bolBackPressure = true;
        this.intQueueCapacity = intMaxBufferSize;
    }
    
    /**
     * <p>
     * Disables client back pressure.
     * </p>
     * <p>
     * The back-pressure feature is available to tune the streaming of large numbers of ingestion frames;
     * it allows back pressure from the Ingestion Service to be felt at the client side.
     * The message supplier class maintains a buffer of ingestion frames to be transmitted 
     * in order to cope with transmission spikes from the client.  Enabling this option
     * prevents clients from adding additional ingestion frames when this buffer is full.
     * (A full buffer indicates a backlog of processing within the Ingestion Service.)
     * </p>
     * <p>
     * <h2>Effect</h2>
     * Disabling client back pressure allows the incoming frame buffer to expand indefinitely.
     * Clients can always add more ingestion frames regardless of any backlog in processing.
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
    // State Query
    //
    
    /**
     * <p>
     * Determine whether or not processing concurrency option is enabled.
     * </p>
     * 
     * @return  <code>true</code> if concurrent processing is enabled, <code>false</code> otherwise
     */
    public boolean hasConcurrency() {
        return this.bolConcurrency;
    }
    
    /**
     * <p>
     * Returns the number of concurrent processing threads.
     * </p>
     * <p>
     * The concurrency count value returned is for both the ingestion frame decomposition tasks and the 
     * tasks that convert ingestion frames to gRPC messages for transport to the Ingestion Service.
     * </p>
     * 
     * @return  the number of thread tasks of each type (decomposition and message conversion)
     */
    public int  getConcurrencyCount() {
        return this.cntConcurrencyThrds;
    }
    
    /**
     * <p>
     * Determine whether or not ingestion frame decomposition option is enabled.
     * </p>
     * 
     * @return <code>true</code> if frame decomposition is enabled, <code>false</code> otherwise
     */
    public boolean hasFrameDecomposition() {
        return this.bolDecompAuto;
    }
    
    /**
     * <p>
     * Determine whether or not back pressure option is enabled.
     * </p>
     * 
     * @return <code>true</code> if back-pressure is being enforced, <code>false</code> otherwise
     */
    public boolean hasBackPressure() {
        return this.bolBackPressure;
    }
    
    /**
     * <p>
     * Returns the number of <code>IngestDataRequest</code> messages in the request message 
     * queue at the time of invocation.
     * </p>
     * 
     * @return  current size of the request message queue buffer
     */
    public int      getRequestQueueSize() {
        return this.queMsgRequests.size();
    }
    
    /**
     * <p>
     * Determine whether or not there are pending processor tasks at time of invocation.
     * </p>
     * <p>
     * Allows clients to check if there are pending processing tasks that have not offered
     * their results to one of the queue buffers.  For example, if the method 
     * </code>{@link #getRequestQueueSize()}</code> reports zero but there are tasks pending
     * the queue will eventually increase.
     * </p>
     * <p>
     * <h2>Thread Tasks</h2>
     * <code>IngestionFrameProcessorDeprecated</code> instances maintain pools of processing threads.
     * Thread tasks take ownership of resources offered by clients and, thus, these resource 
     * may not appear in any of their respective queue buffers until the thread task has 
     * completed is processing.  This method allows clients to determine if there are
     * currently client resources undergoing processing. 
     * </p>
     *   
     * @return  <code>true</code> if there are currently pending processing tasks, 
     *          <code>false</code> otherwise
     */
    public boolean hasPendingMessages() {
        return this.cntPending.getAcquire() > 0;
    }
    
    /**
     * <p>
     * Returns whether or not a shutdown operation has been called.
     * </p>
     * <p>
     * Returns <code>true</code> if either <code>{@link #shutdown()}</code> or 
     * <code>{@link #shutdownNow()}</code> has been called.  
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This method is NOT the complement of <code>{@link IMessageSupplier#isSupplying()}</code>.
     * Note that if  <code>{@link #shutdown()}</code> has been called there may still be messages
     * in the request queue.
     * </p>
     * 
     * @return  <code>true</code> if a shutdown operation has been invoked, <code>false</code> otherwise
     */
    public boolean hasShutdown() {
        return this.bolShutdown;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Activates the <code>IngestDataRequest</code> message supplier.
     * </p>
     * <p>
     * After invoking this method the message supplier instance is ready for ingestion frame
     * processing and conversion.  Ingestion frames can be added to the processor where they
     * are (optionally) decomposed and converted to <code>IngestDataRequest</code> messages.
     * Processed messages are then available to consumers of theses messages throught the
     * <code>{@link IMessageSupplier}</code> interface.
     * </p>
     * <h2>Operation</h2>
     * This method starts all ingestion frame processing tasks which are then continuously active
     * throughout the lifetime of this instance, or until explicitly shut down.  
     * Processing tasks execute independently in
     * thread pools where they block until ingestion frames become available.
     * </p>
     * <p>
     * <h2>Shutdowns</h2>
     * Proper operation requires that the processor be shutdown where no longer needed (otherwise
     * thread tasks run indefinitely).  Use either <code>{@link #shutdown()}</code> or 
     * <code>{@link #shutdownNow()}</code> to shutdown the processor.
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
     * A shutdown operation should always be invoked when the message supplier is no longer needed.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  <code>true</code> if the ingestion frame processor was successfully activated,
     *          <code>false</code> if the message supplier was already active
     */
    synchronized
    public boolean activate() {
        
        // Check if already active
        if (this.bolActive)
            return false;

        // Set activation flag before launching threads
        this.bolActive = true;
        
        // Determine the number of concurrent tasks per processing operation
        int     cntTasks = 1;
        if (this.bolConcurrency)
            cntTasks = this.cntConcurrencyThrds;
        
        // Create all the thread tasks and submit them to their corresponding thread executor pool
        for (int iTask=0; iTask<cntTasks; iTask++) {
            Callable<Boolean>   tskDecomp = this.createFrameDecompositionTask();
            Callable<Boolean>   tskConvert = this.createFrameConvertToMessageTask();

            Future<Boolean>     futDecomp = this.xtorDecompTasks.submit(tskDecomp);
            Future<Boolean>     futConvert = this.xtorConvertTasks.submit(tskConvert);
            this.setDecompFutures.add(futDecomp);
            this.setConvertFutures.add(futConvert);
        }

        return true;
    }
    
    /**
     * <p>
     * Shuts down the <code>IngestDataRequest</code> message supplier preventing any new
     * ingestion frames to be added.
     * </p>
     * <p>
     * After calling this method all independent processing tasks are allowed to finish 
     * but no new ingestion frames will be accepted.  
     * Consumers of messages can continue to poll for available messages until the supply
     * is exhausted.
     * </p>
     * <p>
     * This method will block until all currently executing processing tasks have finished.
     * The queue of ingestion request messages may still contain unconsumed messages
     * however. 
     * 
     * @return <code>true</code> if the message supplier was shutdown,
     *         <code>false</code> if the message supplier was not active or shutdown operation failed
     * 
     * @throws InterruptedException interrupted while waiting for processing threads to complete
     */
    synchronized
    public boolean shutdown() throws InterruptedException {
        
        // Check state
        if (!this.bolActive)
            return false;
        
        // This will terminate thread task loops when queues are empty
        this.bolActive = false;

        // Shutdown all processor tasks 
        this.xtorDecompTasks.shutdown();
        this.xtorConvertTasks.shutdown();

        this.setDecompFutures.forEach(future -> future.cancel(false));
        this.setConvertFutures.forEach(future -> future.cancel(false));
        
        // Wait for all pending tasks to complete
        boolean bolShutdownDecomp = this.xtorDecompTasks.awaitTermination(LNG_TIMEOUT_GENERAL, TU_TIMEOUT_GENERAL);
        boolean bolShutdownConvert = this.xtorConvertTasks.awaitTermination(LNG_TIMEOUT_GENERAL, TU_TIMEOUT_GENERAL);
        
        boolean bolResult = bolShutdownDecomp && bolShutdownConvert;
        
        this.bolShutdown = true;
        
        return bolResult;
    }
    
    /**
     * <p>
     * Performs a hard shutdown of the <code>IngestDataRequest</code> message supplier.
     * </p>
     * <p>
     * All currently executing ingestion frame processing tasks are terminated and all
     * queue buffers are cleared.  The outgoing message queue is also cleared.  
     * The method returns immediately upon terminating all activity.
     * </p>
     */
    synchronized
    public void shutdownNow() {
        
        this.bolActive = false;
        
        this.xtorDecompTasks.shutdownNow();
        this.xtorConvertTasks.shutdownNow();

        this.setDecompFutures.forEach(future -> future.cancel(true));
        this.setConvertFutures.forEach(future -> future.cancel(true));
        
        this.queFramesRaw.clear();
        this.queFramesPrcd.clear();
        this.queMsgRequests.clear();
        
        this.bolShutdown = true;
    }
    
    /**
     * <p>
     * Add the given ingestion frame for processing and conversion to ingest data 
     * request message.
     * </p>
     * <p>
     * The ingesiton frame is added to the queue buffer of raw data frames.  If automatic ingestion
     * frame decomposition is enabled it is then decomposed by the decomposition tasks
     * and transferred to the processed frame queue buffer, otherwise it is transferred
     * directly.  All ingestion frames entering the processed frame buffer are then converted
     * to <code>IngestDataRequest</code> messages where they are available through the 
     * <code>{@link IMessageSupplier}</code> interface. 
     * </p>
     * <p>
     * <h2>Back Pressure</h2>
     * If the back-pressure feature is enable this method blocks whenever the outgoing message
     * queue buffer is at capacity.  The method will not return until the consumer of 
     * <code>IngestDataRequest</code> messages has taken the queue below capacity.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This method is synchronized through <code>{@link #addFrames(List)}</code> to which it
     * defers.
     * </p>
     * <p>
     * <h2>WARNING:</code>
     * If ingestion frame decomposition is enabled the argument frame can be destroyed
     * if it has allocation larger than the given limit.  The offending frame is decomposed 
     * into smaller frames until empty.
     * </p>
     * 
     * @param frame ingestion frame added for processing
     * 
     * @throws IllegalStateException    the processor is currently inactive
     * @throws InterruptedException     interrupted while waiting for message buffer ready
     */
    synchronized
    public void addFrame(IngestionFrame frame) throws IllegalStateException, InterruptedException {
        this.addFrames(List.of(frame));
    }
    
    /**
     * <p>
     * Add the given list of ingestion frames for processing and conversion to ingest data 
     * request messages.
     * </p>
     * <p>
     * The given list is added to the queue buffer of raw data frames.  If automatic ingestion
     * frame decomposition is enabled they are then decomposed by the decomposition tasks
     * and transferred to the processed frame queue buffer, otherwise they are transferred
     * directly.  All ingestion frames entering the processed frame buffer are then converted
     * to <code>IngestDataRequest</code> messages where they are available through the 
     * <code>{@link IMessageSupplier}</code> interface. 
     * </p>
     * <p>
     * <h2>Back Pressure</h2>
     * If the back-pressure feature is enable this method blocks whenever the outgoing message
     * queue buffer is at capacity.  The method will not return until the consumer of 
     * <code>IngestDataRequest</code> messages has taken the queue below capacity.
     * </p>
     * <p>
     * <h2>WARNING:</code>
     * If ingestion frame decomposition is enabled the argument frames can potentially be destroyed
     * if any frame has memory allocation larger than the given limit.  An offending frame is 
     * decomposed into smaller frames until empty.
     * </p>
     * 
     * @param lstFrames ordered list of ingestion frames for processing
     * 
     * @throws IllegalStateException    the processor is currently inactive
     * @throws InterruptedException     interrupted while waiting for message buffer ready
     */
    synchronized
    public void addFrames(List<IngestionFrame> lstFrames) throws IllegalStateException, InterruptedException {

        // Check if active
        if (!this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - supplier is no longer active.");
        
        // If no back-presssure enforcement just add all frames to raw frame buffer and return
        if (!this.bolBackPressure) {
            this.queFramesRaw.addAll(lstFrames);
            
            return;
        }
        
        // Enforce back-pressure to the client if processed frame queue is full 
        this.lckMsgQueReady.lock();
        try {
            if (this.queFramesPrcd.size() >= this.intQueueCapacity)
                this.cndMsgQueReady.await();  // throws InterruptedException

            this.queFramesRaw.addAll(lstFrames);

        } finally {
            this.lckMsgQueReady.unlock();
        }
    }
    
    /**
     * <p>
     * Allows clients to block until the request queue buffer is ready and back pressure is 
     * relieved.
     * </p>
     * <p>
     * This method allows clients to explicitly wait for back-pressure relief.  This method blocks
     * if the frame buffer is at capacity, or returns immediately if not.
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
     * This method is valid even if the back-pressure features is disabled (i.e., with method
     * <code>{@link #disableBackPressure()}</code> or in API configuration file).
     * </li>
     * </ul>
     * </p> 
     * 
     * @throws IllegalStateException    operation invoked while supplier inactive
     * @throws InterruptedException     operation interrupted while waiting for queue ready
     */
    public void awaitQueueReady() throws IllegalStateException, InterruptedException {
        
        // Check if active - if deactivated will wait forever.
        if (!this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - supplier is no longer active.");

        // Do this regardless of whether back pressure is active or not
        //  The client wants to wait, we wait
        this.lckMsgQueReady.lock();
        try {
            // Return immediately if the queue is not at capacity
            if (this.queMsgRequests.size() < this.intQueueCapacity)
                return;
            
            else
                this.cndMsgQueReady.await();
            
        } finally {
            this.lckMsgQueReady.unlock();
        }
    }
    
    /**
     * <p>
     * Allows clients to block until the request message queue empties.
     * </p>
     * <p>
     * This method allows clients to wait for the <code>IngestDataRequest</code> buffer
     * to fully empty.  Clients can add a fixed number of ingestion frames then measure 
     * the time for frames to be processed and and consumed.  This activity may be useful
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
     * </ul>
     * </p> 
     * 
     * @throws IllegalStateException    operation invoked while processor inactive
     * @throws InterruptedException     operation interrupted while waiting for queue ready
     */
    public void awaitRequestQueueEmpty() throws IllegalStateException, InterruptedException {

        // Check if active - if deactivated will wait forever.
//        if (!this.bolActive)
//            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - supplier is no longer active.");

        // Get the request message queue empty lock 
        this.lckMsgQueEmpty.lock();
        try {
            // Return immediately if all queues are empty and nothing is pending 
            if (this.queMsgRequests.isEmpty() && this.queFramesPrcd.isEmpty() && this.queFramesRaw.isEmpty() && !this.hasPendingMessages())
                return;
            
            else
                this.cndMsgQueEmpty.await();
            
        } finally {
            this.lckMsgQueEmpty.unlock();
        }
    }
    
    // 
    // IMessageSupplier<IngestDataRequest> Interface
    //
    
    /**
     * <p>
     * Determines whether or not <code>IngestDataRequest</code> messages are currently 
     * available.
     * </p>
     * <p>
     * The returned value is true if any (or all) of the following conditions apply:
     * <ul>
     * <li>The processor has been activated (and not shut down).</li>
     * <li>The processor has been shut down but there are still messages in the queue.</li>
     * <li>The processor has been shut down, the queue is empty, but there are still pending tasks.</li>
     * </ul>
     * </p>
     *  
     * @return <code>true</code> if there are currently request messages available or pending
     *         <code>false</code> otherwise
     *
     * @see com.ospreydcs.dp.api.ingest.model.IMessageSupplier#isSupplying()
     */
    @Override
    synchronized
    public boolean isSupplying() {
        return this.bolActive || this.hasPendingMessages() || !this.queMsgRequests.isEmpty();
    }

    /**
     *
     * @see com.ospreydcs.dp.api.ingest.model.IMessageSupplier#take()
     */
    @Override
//    synchronized
    public IngestDataRequest take() throws IllegalStateException, InterruptedException {
        
        // Check states
        if (!this.bolActive && !this.hasPendingMessages() && this.queMsgRequests.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - supplier is inactive and queue is empty.");
        
        try {
            IngestDataRequest   msgRqst = this.queMsgRequests.take();
        
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
        if (!this.bolActive && !this.hasPendingMessages() && this.queMsgRequests.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - supplier is inactive and queue is empty.");
        
        try {
            IngestDataRequest   msgRqst = this.queMsgRequests.poll();
            
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
    public IngestDataRequest poll(long cntTimeout, TimeUnit tuTimeout) throws IllegalStateException, InterruptedException {
        
        // Check state
        if (!this.bolActive && !this.hasPendingMessages() && this.queMsgRequests.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - supplier is inactive and queue is empty.");
        
        try {
            IngestDataRequest   msgRqst = this.queMsgRequests.poll(cntTimeout, tuTimeout);
            
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
     * <li>If queue size < capacity then signal <code>{@link #cndMsgQueReady}</code>.</li>
     * <li>If queue size = 0 then signal <code>{@link #cndMsgQueEmpty}</code>.</li>
     * </ul>
     * </p>
     */
    private void signalRequestQueueConditions() {
        
        // Signal any threads blocking on the queue ready condition
        this.lckMsgQueReady.lock();
        try {
            if (this.queMsgRequests.size() < this.intQueueCapacity)
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
    
    /**
     * <p>
     * Creates a new ingestion frame decomposition task for thread pool use.
     * </p>
     * <p>
     * This method creates a thread task that continuously polls the raw ingestion frame buffer
     * for frames which it decomposes then offers to the processed frame buffer.
     * </p>
     * <p>
     * <h2>Decomposition</h2>
     * Currently the returned tasks decompose the ingestion frames horizontally (by column)
     * which is usually more efficient.
     * </p> 
     * 
     * @return  a new <code>Callable</code> instance performing frame decompositions
     * 
     * throws InterruptedException interrupted while waiting for an available IngestionFrameDecomposer instance 
     */
    private Callable<Boolean> createFrameDecompositionTask() /* throws InterruptedException */ {
    
        // Define the task operations as a lambda function
        Callable<Boolean>    task = () -> {
            
            // TODO - Remove
            // Debugging - Create ID
            final int   intThrdId = this.cntDecompThrds++;
            
            
            // Create a new frame binner processor for this thread
            IngestionFrameDecomposer binner = IngestionFrameDecomposer.from(this.lngBinSizeMax);

            // While active - Continuously process frames from raw frame buffer
            // - second OR conditional allows for soft shutdowns
            while (this.bolActive || this.hasPendingMessages() || !this.queFramesRaw.isEmpty()) {
//            while (this.bolActive || !this.queFramesRaw.isEmpty()) {
                
                // Retrieve an unprocessed frame from the queue - blocking until available
                //  - this is thread safe according to Java BlockingQueue documentation
                IngestionFrame frmRaw = this.queFramesRaw.poll(INT_TIMEOUT_TASK_POLL, TU_TIMEOUT_TASK_POLL);
                
                if (frmRaw == null)
                    continue;
                
                // TODO - Remove
                LOGGER.debug("Frame binner thread #" +intThrdId + " activated for raw ingestion frame processing.");
                
                this.cntPending.incrementAndGet();
                
                // If there is no automatic decomposition simply all the frame to the processed queue
                if (!this.bolDecompAuto) {
                    this.queFramesPrcd.offer(frmRaw);
                
                    this.cntPending.decrementAndGet();
                    continue;
                }
                
                // TODO - Remove
                LOGGER.debug("Frame binner thread #" +intThrdId + " activated for decomposition.");
                
                // Process: First attempt to decompose the frame horizontally - this is the cheapest
                List<IngestionFrame>    lstFrmsPrcd;
                try {
                    lstFrmsPrcd = binner.decomposeHorizontally(frmRaw);
                    
                } catch (IllegalArgumentException | CompletionException e1) {
                    
                    // If failed: try to decompose the frame vertically - this is expensive
                    try {
                        lstFrmsPrcd = binner.decomposeVertically(frmRaw);
                        
                    } catch (IllegalArgumentException | CompletionException e2) {
                        
                        // If failed again: There is nothing more to do  
                        if (BOL_LOGGING)
                            LOGGER.error("Frame decomposition failed for frame {} with exception {} = {}.", frmRaw.getFrameLabel(), e2.getClass().getName(), e2.getMessage());
                        throw e2;
                    }
                }

                // Enqueue all the decomposed frames in the processed frames buffer
                lstFrmsPrcd.forEach(f -> this.queFramesPrcd.offer(f));

//                // TODO - Remove
                LOGGER.debug("Frame binner thread {} offered {} frames to processed queue now with size {}.", intThrdId, lstFrmsPrcd.size(), this.queFramesPrcd.size());
                        
//                System.out.println("  binner thread #" +intThrdId + " decomposed raw frame into " + lstFrmsPrcd.size() + " binned frames.");
//                System.out.println("    processed frame queue size = " + this.queFramesPrcd.size());
                
                this.cntPending.decrementAndGet();
            }
            
//            // TODO - Remove
//            System.out.println("Terminating: binner thread #" +intThrdId);
            
            // Everything was successful if the raw frame buffer is empty
            return !this.bolActive && this.queFramesRaw.isEmpty();
        };
        
        return task;
    }
    
    /**
     * <p>
     * Creates a new ingestion frame to ingestion request message task for thread pool use.
     * </p>
     * <p>
     * This method creates a thread task that continuously polls the processed ingestion frame buffer
     * for decomposed ingestion frames which is converts to ingest data request messages that are
     * subsequently offered  to the outgoing message queue.
     * </p>
     * 
     * @return  a new <code>Callable</code> instance converting ingestion frames to request messages
     */
    private Callable<Boolean>   createFrameConvertToMessageTask() {
        
        // Define the task operations as a lambda function
        Callable<Boolean>   task = () -> {
            
            // Ingestion frame converter used for all message creation
            IngestionFrameConverter converter = IngestionFrameConverter.create(this.recProviderUid);
            
            // TODO - Remove
            // Debugging - Create ID
            final int   intThrdId = this.cntConvertThrds++;
            
            
            // While active - Continuously convert frames in processed frame buffer to gRPC messages 
            // - second OR conditional allows for soft shutdowns
            while (this.bolActive || this.hasPendingMessages() || !this.queFramesPrcd.isEmpty()) {
//            while (this.bolActive || !this.queFramesPrcd.isEmpty()) {
//            while (true) {
                
//                this.cntPending.incrementAndGet();
                
                // Poll for the next processed frame in the frame buffer
                //  - this is thread safe according to Java BlockingQueue documentation
                IngestionFrame  frmPrcd = this.queFramesPrcd.poll(INT_TIMEOUT_TASK_POLL, TU_TIMEOUT_TASK_POLL);
//                IngestionFrame  frmPrcd = this.queFramesPrcd.take();
                
//                // TODO - Remove
//                System.err.println("Conversion thread #" + intThrdId + " unblocked, frame==null is " 
//                        + (frmPrcd==null) + ", bolActive=" + this.bolActive 
//                        + ", this.hasPendingMessages() = " + this.hasPendingMessages()
//                        + ", this.queFramesPrcd.size() = " + this.queFramesPrcd.size()
//                        + ", this.queFramesPrcd.isEmpty() = " + this.queFramesPrcd.isEmpty());
                
                if (frmPrcd == null)
                    continue;
                
                this.cntPending.incrementAndGet();
                
                // TODO - Remove
                LOGGER.debug("Conversion thread #" +intThrdId + " activated for 1 message conversion.");
                
                // Convert the ingestion frame to an Ingestion Service data request message 
                IngestDataRequest   msgRqst = converter.createRequest(frmPrcd);
                
                // TODO - Remove
                LOGGER.debug("  conversion thread #" +intThrdId + " created message - (msg==null)=" + (msgRqst==null));
                
                // Add ingestion request message to outgoing queue
                this.queMsgRequests.offer(msgRqst);

                // TODO - Remove
                LOGGER.debug("  conversion thread #" +intThrdId + " queued 1 message - queue size = " + this.getRequestQueueSize());
                
                this.cntPending.decrementAndGet();
            }
            
            // TODO - Remove
            System.err.println("Terminating: conversion thread #" +intThrdId);
            LOGGER.debug("Terminating: conversion thread #" +intThrdId);
            
            return !this.bolActive && this.queFramesRaw.isEmpty() && this.queFramesPrcd.isEmpty();
        };
        
        return task;
    }
    
//    /**
//     * <p>
//     * Converts the given ingestion frame to a new, equivalent <code>IngestDataRequest</code> message.
//     * </p>
//     * 
//     * @param frame source of all data used to populated returned message
//     * 
//     * @return  new <code>IngestDataRequest</code> message populated with argument data
//     */
//    private IngestDataRequest createRequest(IngestionFrame frame) {
//        
//        IngestDataRequest   msgRqst = IngestDataRequest.newBuilder()
//                .setProviderId(this.intProviderUid)
//                .setClientRequestId(this.createNewRequestId())
//                .setRequestTime(ProtoTime.now())
//                .addAllAttributes(ProtoMsg.createAttributes(frame.getAttributes()))
//                .setEventMetadata(this.createEventMetadata(frame))
//                .setIngestionDataFrame(ProtoMsg.from(frame))
//                .build();
//
//        return msgRqst;
//    }
//    
//    /**
//     * <p>
//     * Generates a new, unique client request identifier and returns it.
//     * </p>
//     * <p>
//     * The returns request identifier is "unique" within the lifetime of the current
//     * Java Virtual Machine (JVM).  Request identifiers can, potentially, be repeated for
//     * independent JVMs and should not be used to uniquely identify request beyond the
//     * current JVM execution.
//     * </p>
//     * <p>
//     * <h2>Computation</h2>
//     * The return value is computed by taking the hash code for the 
//     * <code>IngestionFrameProcessorDeprecated</code> class and incrementing it by the current value
//     * of the class frame counter <code>{@link #cntFrames}</code> (which is then incremented).
//     * The <code>long</code> value is then converted to a string value and returned.
//     * </p>
//     * <p>
//     * <h2>Thread Safety</h2>
//     * This method is thread safe.  Computation of the UID is synchronized with the class
//     * lock instance <code>{@link #objClassLock}</code>.  This is necessary since the
//     * <code>{@link #cntFrames}</code> class instance must be modified atomically.
//     * </p>
//     *  
//     * @return  a new client request ID unique within the execution of the current JVM
//     */
//    private String  createNewRequestId() {
//        long lngHash;
//        synchronized (objClassLock) {
//            lngHash = IngestionFrameProcessorDeprecated.class.hashCode() + IngestionFrameProcessorDeprecated.cntFrames;
//            
//            IngestionFrameProcessorDeprecated.cntFrames++;
//        }
//        
//        String strClientId = Long.toString(lngHash);
//        
//        return strClientId;
//    }
//    
//    /**
//     * <p>
//     * Creates and returns a new <code>EventMetadata</code> message using the snapshot parameters
//     * within the given ingestion frame.
//     * </p>
//     * <p>
//     * The optional "snapshot" parameters from the given ingestion frame are used to populate the
//     * return message.  The returned message will <code>null</code> values for any snapshot parameters
//     * that have not been set.
//     * </p>
//     * 
//     * @param frame     source of event metadata parameters
//     * 
//     * @return          new <code>EventMetadata</code> message populated from the argument
//     */
//    private EventMetadata   createEventMetadata(IngestionFrame frame) {
//        
////      EventMetadata   msgMetadata = EventMetadata.newBuilder()
////      .setDescription(frame.getSnapshotId())
////      .setStartTimestamp( ProtoMsg.from(frame.getSnapshotDomain().begin()) )
////      .setStopTimestamp( ProtoMsg.from(frame.getSnapshotDomain().end()) )
////      .build();
//
//        EventMetadata.Builder   bldrMsg = EventMetadata.newBuilder();
//        
//        if (frame.getSnapshotId() != null)
//            bldrMsg
//            .setDescription(frame.getSnapshotId());
//        
//        if (frame.getSnapshotDomain() != null) 
//            bldrMsg
//            .setStartTimestamp( ProtoMsg.from(frame.getSnapshotDomain().begin()) )
//            .setStopTimestamp( ProtoMsg.from(frame.getSnapshotDomain().end()) );
//        
//        EventMetadata   msgMetadata = bldrMsg.build();
//        
//        return msgMetadata;
//    }
}
