/*
 * Project: dp-api-common
 * File:	IngestionFrameProcessor.java
 * Package: com.ospreydcs.dp.api.ingest.model.frame
 * Type: 	IngestionFrameProcessor
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
 * @since Jul 25, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.frame;

import java.util.ArrayList;
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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
 *
 * <p>
 * <h1>Processor of <code>IngestionFrame</code> instances and supplier of 
 * <code>IngestDataRequest</code> messages from processed ingestion frames.</h1>
 * </p>
 * <p>
 * This class performs several functions, including basic ingestion frame buffering, ingestion frame
 * decomposition, and conversion of ingestion frames to <code>IngestDataRequest</code> messages.
 * The ultimate use for class instances is as a supplier of <code>IngestDataRequest</code> messages.
 * </p>
 * <p>
 * <h2>Activation</h2>
 * A <code>IngestionFrameProcessor</code> instance must be activated before attempting
 * to add ingestion frames; use the method <code>{@link #activate()}</code>.  Likewise,
 * an active processor should be shutdown when no longer needed; use methods
 * <code>{@link #shutdown()}</code> or <code>{@link #shutdownNow()}</code>.
 * </p>
 * <p>
 * <h2>Processing Options</h2>
 * The <code>IngestionFrameProcessor</code> class has several processing options which
 * should be set before activation.
 * <ul>
 * <li>
 * <code>{@link #setConcurrency(int)}</code> - Processes ingestion frames using
 * multiple concurrent processing threads, otherwise all frame processing is done on 
 * single execution threads.
 * </li>
 * <br/>
 * <li>
 * <code>{@link #setFrameDecomposition(long)}</code> - All incoming ingestion frames
 * are checked for memory allocation size.  If a frame has allocation larger than the maximum
 * gRPC message size it is decomposed in a collection of equivalent, composite frame each meeting the
 * size requirement.  A horizontal decomposition (i.e., by columns) is attempted first since 
 * this technique is the most efficient.  If horizontal decomposition fails a vertical 
 * decomposition (i.e., by row) is attempted, which is significantly more resource intensive.
 * If all decompositions fail for an ingestion frame with allocation larger than maximum gRPC
 * message size an exception is thrown. 
 * </li>
 * <br/>
 * </ul>
 * The default settings for all the above options are taken from the client API configuration
 * parameters. 
 * </p> 
 * <p>
 * <h2><code>IngestDataRequest</code> Message Blocking Supplier</h2>
 * Class instances function as a supplier of <code>{@link IngestDataRequest}</code> messages
 * which are created from <code>{@link IngestionFrame}</code> objects offered to the instance.
 * Consumers of <code>IngestDataRequest</code> messages can poll a message supplier for messages,
 * or wait for messages to become available.
 * </p> 
 * <p>
 * <h2>Ingestion Frame Buffering and Back Pressure</h2>
 * The message supplier class maintains a buffer of ingestion frames to be processed
 * in order to cope with supply spikes from the client (i.e., the frame supplier).  
 * This buffer is unbounded and can potentially create a significant back log if 
 * the back-end consumer of messages is unresponsive.  Thus, some external method of
 * exerting back pressure or throttling is advised. 
 * </p>
 * <p>
 * It is strongly recommended that <em>some type of back pressure or input frame throttling is
 * provided externally</em>.  This is needed to prevent the internal 
 * <code>IngestDataRequest</code> message buffer from growing without bound.
 * The ability to add ingestion frames
 * to an <code>IngestionFrameProcessor</code> can be throttled at the supplier side using
 * an implementation of the <code>{@link IResourceConsumer&lt;IngestionFrame&gt;}</code> interface,
 * in particular a blocking ingestion frame buffer.   
 * In this fashion, clients will experience "back pressure" from consumers of 
 * <code>IngestDataRequest</code> messages when they become backlogged.
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
 * is decomposed whenever its total memory allocation is larger than the current gRPC message
 * size limitation (default is identified in the client API configuration parameters).  
 * Thus, a single, large ingestion frame added to this supplier will be decomposed into multiple smaller ingestion
 * frame, each meeting the gRPC message size limitation specified in the client API configuration
 * parameters.
 * <p>
 * For an ingestion frame with allocation greater than the The decomposition proceeds as follows:
 * <ol>
 * <li>The memory allocation of the frame is computed and compared to the maximum size limit.</li>
 * <li>A horizontal decomposition (i.e., by column) is first attempted as this is generally the fastest method.</li>
 * <li>A vertical decomposition (i.e., by row) is attempted if the horizontal fails.  This method is more expensive.</li>
 * <li>If both attempts fail the frame is added to the collection of failed decompositions (the error is recorded).</li>
 * </ol>
 * Note that the decomposition process can be non-trivial, thus using concurrency can greatly improve
 * performance.  Even if an ingestion frame does not require decomposition, its memory allocation
 * must be computed, which can be significant depending upon the frame (e.g., large tables).
 * </p>
 * <p>
 * <h2>Concurrency</h2>
 * The frame decomposition operations can be performed concurrently.  Specifically, if multiple
 * ingestion frames are offered at the same time each decomposition will be assigned a separate
 * execution thread (up to the concurrency limit).
 * </p>
 * <p>
 * <h2>WARNINGS:</h2>
 * <ul>
 * <li>
 * If automatic ingestion frame decomposition is turned of it is imperative that all
 * offered ingestion frames have memory allocations less than the current gRPC message
 * size limit or gRPC will throw a runtime exception when attempting to transmit the 
 * generated <code>IngestDataRequest</code> message.
 * </li>
 * <br/>
 * <li>
 * If automatic ingestion frame decomposition is active then any frame with memory allocation larger than
 * the gRPC message size limit are decomposed into smaller ingestion frames.  This action destroys the original
 * ingestion frame.
 * </li>
 * </ul>
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * This class uses instances of <code>{@link IngestionFrameBinner}</code> for frame decomposition
 * and instances of class <code>{@link IngestionFrameConverter}</code> for frame conversion
 * to ingest data request messages.
 * </p>
 * <p>
 * <h2>Revisions</h2>
 * Ingestion frame buffering now occurs only for concurrent processing.  If concurrency (mult-threading) is
 * disabled then all processing is done on the main thread.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Jul 25, 2024
 *
 * @see IngestionFrameBinner
 * @see IngestionFrameConverter
 */
public class IngestionFrameProcessor implements IMessageSupplier<IngestDataRequest> {

    
    //
    // Application Resources
    //
    
    /** The Ingestion Service client API default configuration */
    private static final DpIngestionConfig  CFG_DEFAULT = DpApiConfig.getInstance().ingest; 
    
    
    //
    // Class Constants
    //
    
    // TODO - Investigate polling and timeouts - CKA 8/4/2024 15 milliseconds seems reasonable 
    /** Timeout limit to wait for thread task polling operations */
    private static final int       INT_TIMEOUT_TASK_POLL = 15;
    
    /** Timeout units to wait for thread task polling operations */
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
    
    /** Maximum number of concurrent processing threads */
    private static final Integer    INT_CONCURRENCY_CNT_THREADS = CFG_DEFAULT.concurrency.threadCount;
    
    
    /** Perform ingestion frame decomposition (i.e., "binning") */
    private static final Boolean    BOL_BINNING_ACTIVE = CFG_DEFAULT.stream.binning.active;
    
    /** Maximum size limit (in bytes) of decomposed ingestion frame */
    private static final Integer    LNG_BINNING_MAX_SIZE = CFG_DEFAULT.stream.binning.maxSize;
    
    
//    /** Size of the ingestion frame queue buffer */
//    private static final Integer    INT_BUFFER_SIZE = CFG_DEFAULT.stream.buffer.size;
//    
//    /** Allow back pressure to client from queue buffer */
//    private static final Boolean    BOL_BUFFER_BACKPRESSURE = CFG_DEFAULT.stream.buffer.backPressure;
    
    
    //
    // Class Resources
    //
    
    /** The class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
//    /** The locking object for synchronizing access to class resources */ 
//    private static final Object   objClassLock = new Object();

    
    //
    // Configuration Parameters
    //
    
    /** The data provider unique identifier used for <code>IngestDataRequest</code> messages */
    private ProviderUID       recProviderUid = null;

    
    /** Processing concurrency enabled flag */
    private boolean bolConcurrency = BOL_CONCURRENCY_ACTIVE;
    
    /** The number of worker threads used for concurrent processing */
    private int     cntConcurrentThrds = INT_CONCURRENCY_CNT_THREADS;
    
    
    /** Ingestion frame decomposition (binning) enabled flag  */
    private boolean bolDecompAuto = BOL_BINNING_ACTIVE;
    
    /** Ingestion frame decomposition maximum size */
    private long    lngBinSizeMax = LNG_BINNING_MAX_SIZE;
    
    
    //
    // State Variables
    //
    
    /** Is supplier active (not been shutdown) */
    private boolean bolActive = false;
    
    /** Has the supplier been shutdown */
    private boolean bolShutdown = false;
  

//    // TODO - Remove
//    /** DEBUG - The number (and ID generator) of frame decomposition threads */
//    private int     cntDecompThrds = 0;
//    
//    // TODO - Remove
//    /** DEBUG - The number (and ID generator) of frame-to-message conversion tasks */
//    private int     cntConvertThrds = 0;
    
    
    //
    // Instance Resources
    //
    
    /** Incoming ingestion frame buffer for multi-threaded processing */
    private final BlockingQueue<IngestionFrame>     queFramesRaw = new LinkedBlockingQueue<>();
    
    /** Decomposed frame queue ready for frame-to-message conversion in multi-threaded processing */ 
    private final BlockingQueue<IngestionFrame>     queFramesPrcd = new LinkedBlockingQueue<>();
    
    /** Outgoing ingestion request message queue */
    private final BlockingQueue<IngestDataRequest>  queMsgRequests = new LinkedBlockingQueue<>();

    
    /** Collection of ingestion frames that failed decomposition */
    private Collection<Exception>         setFramesFailedDecomp;
    
    /** Collection of ingestion frames that failed message conversion */
    private Collection<Exception>         setFramesFailedConvert;

    
    //
    // Single-Thread Processing Resources
    //
    
    /** Frame decomposition sub-processor for main thread */
    private IngestionFrameBinner      prcrMsgBinner;
    
    /** Frame-to-message sub-processor for main thread */
    private IngestionFrameConverter   prcMsgConverter;

    
    //
    // Multi-Thread Processing Resources
    //
    
    /** The pool of frame decomposition threads */
    private ExecutorService               xtorDecompTasks; // = Executors.newCachedThreadPool();
    
    /** The pool of frame-to-message conversion threads */
    private ExecutorService               xtorConvertTasks; // = Executors.newCachedThreadPool();
    
    /** Collection of future results from decomposition tasks - used in shutdown */
    private Collection<Future<Boolean>>   setDecompFutures; // = new LinkedList<>();

    /** Collection of future results from frame-to-message conversion tasks - used in shutdown */
    private Collection<Future<Boolean>>   setConvertFutures; // = new LinkedList<>();
    
    
    /** Pending processing operation counter - managed by processing threads */
    private final AtomicInteger     cntPendingTasks = new AtomicInteger(0);
    
    
//    /** The message queue buffer back pressure lock */
//    private final Lock              lckMsgQueReady = new ReentrantLock();
//    
//    /** The message queue buffer ready (or "not full") lock condition */
//    private final Condition         cndMsgQueReady = lckMsgQueReady.newCondition();
//    
//    /** The message queue buffer empty lock */
//    private final Lock              lckMsgQueEmpty = new ReentrantLock();
//    
//    /** The message queue buffer empty lock condition */
//    private final Condition         cndMsgQueEmpty = lckMsgQueEmpty.newCondition();
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionFrameProcessor</code>.
     * </p>
     *
     */
    public IngestionFrameProcessor() {
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionFrameProcessor</code>.
     * </p>
     *
     * @param recProviderUid    the default data provider UID used in <code>IngestDataRequest</code> messages
     */
    public IngestionFrameProcessor(ProviderUID recProviderUid) {
        this.recProviderUid = recProviderUid;
    }


    //
    // Configuration
    //
    
    /**
     * <p>
     * Sets the default Data Provider UID used to create <code>IngestDataRequest</code> messages.
     * </p>
     * <p>
     * The given argument is used to populate the Data Provider UID field of the <code>IngestDataRequest</code>
     * messages whenever the source <code>IngestionFrame</code> does not contain one (i.e., 
     * <code>{@link IngestionFrame#getProviderUid()}</code> returns <code>null</code>).
     * </p>
     * 
     * @param recProviderUid    default Data Provider UID used for ingestion request messages 
     */
    public void setProviderUid(ProviderUID recProviderUid) {
        this.recProviderUid = recProviderUid;
    }
    
    /**
     * <p>
     * Enables the use of concurrent multi-threading and sets the number of threads.
     * </p>
     * <p>
     * Enables the use of multiple, concurrent execution threads for ingestion frame processing 
     * (i.e., frame decomposition and frame-to-message conversion).
     * <s>If concurrency is disabled there are still two independent processing threads, one for frame
     * decomposition and one for frame-to-message conversion.</s>
     * If concurrency is disabled all processing occurs on the main execution thread.  
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
     * The argument specifies both the number of decomposition threads <b>and</b> the number of
     * frame-to-message conversion threads.  Thus, the <em>total</em> number of execution threads
     * is actually <em>twice</em> the argument value.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This configuration parameter can only be modified <em>while the processor is inactive</em>, 
     * otherwise an exception is thrown.
     * Specifically, invoke this method either before activation with the <code>{@link #activate()}</code> 
     * or after shutdown with the <code>{@link #shutdown()}</code> method. 
     * </p>
     * 
     * @param cntThreads    the number of independent processing threads each concurrent operation
     * 
     * @throws IllegalStateException    method called while processor is active
     */
    synchronized 
    public void setConcurrency(int cntThreads) throws IllegalStateException {
        
        if (this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change concurency once activated.");
        
        this.bolConcurrency = true;
        this.cntConcurrentThrds = cntThreads;
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
     * <s>
     * This maximum ingestion frame size can only be modified <em>before</em> the processor is 
     * activated with <code>{@link #activate()}</code>. Calling this method post activation
     * will return ingestion frame composition to the bin size set before activation.
     * </s>
     * </p>
     * 
     * @param lngMaxFrmSize maximum allowable size (in bytes) of decomposed ingestion frames 
     */
    synchronized 
    public void setFrameDecomposition(long lngMaxFrmSize) {

        //        if (this.bolActive)
        //            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change concurency once activated.");

        this.bolDecompAuto = true;
        this.lngBinSizeMax = lngMaxFrmSize;
    }

    /**
     * <p>
     * Disables automatic ingestion frame decomposition.
     * </p>
     * <p>
     * Disables the automatic decomposition of ingestion frames (i.e., "frame binning").
     * When frame decomposition is active any ingestion frame added to this processor
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
     * <s>The maximum ingestion frame size can only be modified <em>before</em> the processor is 
     * activated with <code>{@link #activate()}</code>.  This method will disable frame
     * decomposition if already active, but the maximum frame size cannot be changed post
     * activation.</s>
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
     * Disables the use of concurrent thread for internal processing.
     * </p>
     * <p>
     * Disables the use of multiple, concurrent execution threads for ingestion frame processing 
     * (i.e., frame decomposition and frame-to-message conversion).
     * <s>If concurrency is disabled there are still two independent processing threads, one for frame
     * decomposition and one for frame-to-message conversion.
     * Thus, this method has the equivalent effect of invoking <code>{@link #enableConcurrency(1)}</code>.  
     * </s>
     * If concurrency is disabled all processing occurs on the main execution thread.  
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
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change concurency once activated.");
        
        this.bolConcurrency = false;
    }
    
    
    //
    // Configuration Query
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
     * Determine whether or not automatic ingestion frame decomposition option is enabled.
     * </p>
     * 
     * @return <code>true</code> if frame decomposition is enabled, <code>false</code> otherwise
     */
    public boolean hasFrameDecomposition() {
        return this.bolDecompAuto;
    }

    /**
     * <p>
     * Returns the current default Data Provider UID used for frame-to-message conversion.
     * </p>
     * <p>
     * The returned UID is used for the Provider UID field of the <code>IngestDataRequest</code> messages
     * if none is provided in the processed ingestion frame.  Note that if this value is <code>null</code>
     * and the ingestion frame has no provider UID then the frame is not processed.
     * </p>
     * 
     * @return  default Data Provider UID used for frame-to-message conversion, 
     *          or <code>null</code> if none was provided 
     */
    public ProviderUID  getProviderUid() {
        return this.recProviderUid;
    }
    
    /**
     * <p>
     * Returns the current value of the maximum ingestion frame size before frame decomposition is attempted.
     * </p>
     * <p>
     * The returned value is used for automatic decomposition of ingestion frames.  The memory allocation for
     * the ingestion frame is first computed then compared against this value.  If the frame allocation is
     * greater then it is decomposed into the greated number of ingestion frames all with allocation less than 
     * this value.
     * </p> 
     * 
     * @return  maximum allowable ingestion frame size when automatic decomposition is enabled
     */
    public long getMaxFrameSize() {
        return this.lngBinSizeMax;
    }
    
    /**
     * <p>
     * Returns the number of concurrent processing threads when concurrency is enabled.
     * </p>
     * <p>
     * The concurrency count value returned is for both the ingestion frame decomposition tasks and the 
     * tasks that convert ingestion frames to gRPC messages for transport to the Ingestion Service.
     * </p>
     * 
     * @return  the number of thread tasks of each type (decomposition and message conversion)
     */
    public int  getConcurrencyCount() {
        return this.cntConcurrentThrds;
    }

    
    //
    // State Query
    //
    
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
     * Determine whether or not there are pending tasks that may have resources not yet available
     * at time of invocation.
     * </p>
     * <p>
     * Allows clients to check if there are pending processing tasks that have not offered
     * their results to one of the intermediate queue buffers, or the final, outgoing queue buffer.  
     * For example, if the method </code>{@link #getRequestQueueSize()}</code> reports zero but there 
     * are tasks with pending results, the request queue will eventually increase.
     * </p>
     * <p>
     * <h2>Thread Tasks</h2>
     * <code>IngestionFrameProcessor</code> instances maintain pools of processing threads.
     * Thread tasks take ownership of resources offered by clients and, thus, these resource 
     * may not appear in any of their respective intermediate queue buffers until the thread task has 
     * completed is processing.  This method allows clients to determine if there are
     * currently client resources undergoing processing. 
     * </p>
     *   
     * @return  <code>true</code> if there are currently pending processing tasks, 
     *          <code>false</code> otherwise
     */
    public boolean hasPendingTasks() {
        return this.cntPendingTasks.getAcquire() > 0;
    }
    
    /**
     * <p>
     * Determines whether or not there are ingestion request messages available in the outgoing queue or 
     * the queue is empty but messages are currently being assembled.
     * </p>
     * <p>
     * This is essentially an abuse of notational convention and available for testing purposes.
     * It returns the value <code>true</code> whenever there are ingest data request messages in the
     * processing pipeline.
     * The intent is to submit a collection of ingestion frames for concurrent processing then
     * request ingestion request messages for as long as they are available, as one would with a 
     * standard Java iterator.
     * </p>
     * <p>
     * Note that this method can return <code>false</code> while <code>{@link #isSupplying()}</code>
     * continues to return <code>true</code>.
     * </p>
     *  
     * @return  <code>true</code> if there are currently ingest data messages within the processing pipeline,
     *          <code>false</code> there are no ingest data request messages enqueued or pending
     */
    public boolean hasNext() {
        
        // If we are not active there is nothing
        if (!this.bolActive)
            return false;
        
        // For single-thread processing only need to check the outgoing queue buffer
        if (!this.bolConcurrency)
            return !this.queMsgRequests.isEmpty();
        
        // For multi-threaded processed we need to check everything
        return this.hasPendingTasks() 
                || !this.queFramesRaw.isEmpty() 
                || !this.queFramesPrcd.isEmpty() 
                || !this.queMsgRequests.isEmpty();
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
    // Error Conditions
    //
    
    /**
     * <p>
     * Returns the collection of decomposition exceptions for frames that failed decomposition so far.
     * </p>
     * <p>
     * A non-empty collection represents a failure in the processing stream.  Note that
     * the processing continues but the given collection was not processed and the data
     * was never available at the <code>IMessageSupplier</code> interface.
     * </p>
     * <p>
     * Clients can invoke this method to check for errors post shutdown.  Alternatively,
     * the method make be invoked during operation to check for recorded errors so far
     * in the ingestion frame processing stream.
     * </p>
     * 
     * @return  the collection of exceptions for frames which could not be decomposed
     */
    public final Collection<Exception> getFailedDecompositions() {
        return this.setFramesFailedDecomp;
    }
    
    /**
     * <p>
     * Returns the collection of frame-to-message exceptions for frames that failed frame-to-message conversion so far.
     * </p>
     * <p>
     * A non-empty collection represents a failure in the processing stream.  Note that
     * the processing continues but the given collection was not processed and the data
     * was never available at the <code>IMessageSupplier</code> interface.
     * </p>
     * <p>
     * Clients can invoke this method to check for errors post shutdown.  Alternatively,
     * the method make be invoked during operation to check for recorded errors so far
     * in the ingestion frame processing stream.
     * </p>
     * 
     * @return  the collection of exceptions for frames which could not be converted to gRPC messages
     */
    public final Collection<Exception> getFailedConversions() {
        return this.setFramesFailedConvert;
    }
    
    /**
     * <p>
     * Returns whether or not a processing failure has occurred in the processing stream so far.
     * </p>
     * <p>
     * This method simply checks the containers of failed decomposition frames and failed frame-to-message
     * conversion frames.  If either one is non-empty it returns <code>true</code>.
     * </p>
     * <p>
     * Clients can invoke this method to check for errors post shutdown.  Alternatively,
     * the method make be invoked during operation to check for recorded errors so far
     * in the ingestion frame processing stream.
     * </p>
     * 
     * @return  <code>true</code> if an error has occurred in the processing streaming,
     *          <code>false</code> all submitted ingestion frames have been successfully processed so far.
     * 
     * @see #getFailedDecompositions()
     * @see #getFailedConversions()
     */
    public boolean hasProcessingFailure() {
        return !this.getFailedDecompositions().isEmpty() || !this.getFailedConversions().isEmpty();
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Activates the processor and supplier of <code>IngestDataRequest</code> messages.
     * </p>
     * <p>
     * After invoking this method the message supplier instance is ready for ingestion frame
     * processing and conversion.  Ingestion frames can be submitted to the processor 
     * via the <code>{@link #submit(IngestionFrame)}</code> methods where they
     * are (optionally) decomposed and converted to <code>IngestDataRequest</code> messages.
     * Processed messages are then available to consumers of theses messages through the
     * <code>{@link IMessageSupplier}</code> interface.
     * </p>
     * <h2>Operation</h2>
     * This method starts all ingestion frame processing thread tasks which are continuously active
     * until explicitly shut down.  
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
     * Processors can be cycled through activation and shutdown. 
     * </li>
     * <li>
     * A shutdown operation should always be invoked when the message supplier is no longer needed.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  <code>true</code> if the ingestion frame processor was successfully activated,
     *          <code>false</code> if the message supplier was already active
     *          
     * @throws RejectedExecutionException   processing tasks could not be scheduled for execution
     */
    synchronized
    public boolean activate() throws RejectedExecutionException {
        
        // Check if already active
        if (this.bolActive)
            return false;

        // Set activation flag before launching threads
        this.bolActive = true;
        
        // Create containers for failed frame decomposition and convertion - erases any previous activations
        this.setFramesFailedDecomp = new LinkedList<>();
        this.setFramesFailedConvert = new LinkedList<>();
        
        // If no multi-threaded concurrency
        if (!this.bolConcurrency) {
            this.prcrMsgBinner = IngestionFrameBinner.from(this.lngBinSizeMax);
            this.prcMsgConverter = IngestionFrameConverter.create(this.recProviderUid);
            
            return true;
        }
        
        // Multi-threaded Case:
        // Determine the number of concurrent tasks per processing operation
        int     cntTasks = 1;
        if (this.bolConcurrency)
            cntTasks = this.cntConcurrentThrds;
        
        // Create the fixed size thread pool executors
        this.xtorDecompTasks = Executors.newFixedThreadPool(cntTasks);
        this.xtorConvertTasks = Executors.newFixedThreadPool(cntTasks);
        
        // Create the containers for the thread task futures - this erases any previous activations
        this.setDecompFutures = new ArrayList<>(cntTasks);
        this.setConvertFutures = new ArrayList<>(cntTasks);
        
        // Create all the thread tasks and submit them to their corresponding thread executor 
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
     * Performs an orderly shut down of the ingestion frame processor.
     * </p>
     * <p>
     * After invoking this method all processing tasks are allowed to finish 
     * but no new ingestion frames will be accepted.  
     * Consumers of messages can continue request available messages until the supply
     * is exhausted.
     * </p>
     * <p>
     * This method will block until all currently executing processing tasks have finished.
     * The queue of ingestion request messages may still contain available messages
     * however. 
     * 
     * @return <code>true</code> if the message supplier was shutdown,
     *         <code>false</code> if the message supplier was not active or shutdown operation failed to complete
     * 
     * @throws InterruptedException interrupted while waiting for processing threads to complete
     */
    synchronized
    public boolean shutdown() throws InterruptedException {
        
        // Check state
        if (!this.bolActive)
            return false;
        
        // If single-threaded there is nothing to do but set state flags
        if (!this.bolConcurrency) {
            this.bolActive = false;
            this.bolShutdown = true;
            
            return true;
        }
        
        // Multi-Threaded Case:
        // This will terminate thread task loops when queues are empty
        //  It also prevents acceptance of any new ingestion frames
        this.bolActive = false;

        // Shutdown all thread executors
        this.xtorDecompTasks.shutdown();
        this.xtorConvertTasks.shutdown();

        // Request a soft cancel of each currently executing threads
        this.setDecompFutures.forEach(future -> future.cancel(false));
        this.setConvertFutures.forEach(future -> future.cancel(false));
        
        // Wait for all pending thread tasks to complete
        boolean bolShutdownDecomp = this.xtorDecompTasks.awaitTermination(LNG_TIMEOUT_GENERAL, TU_TIMEOUT_GENERAL);
        boolean bolShutdownConvert = this.xtorConvertTasks.awaitTermination(LNG_TIMEOUT_GENERAL, TU_TIMEOUT_GENERAL);
        
        boolean bolResult = bolShutdownDecomp && bolShutdownConvert;
        
        this.bolShutdown = true;
        
        return bolResult;
    }
    
    /**
     * <p>
     * Performs a hard shutdown of the ingestion frame processor.
     * </p>
     * <p>
     * All currently executing ingestion frame processing tasks are terminated and all
     * queue buffers are cleared.  The outgoing message queue is also cleared.  
     * The method returns immediately upon terminating all activity.
     * </p>
     */
    synchronized
    public void shutdownNow() {
        
        // If we are not active just return
        if (!this.bolActive)
            return;
        
        this.bolActive = false;
        
        // If multi-threaded hard terminate all tasks
        if (this.bolConcurrency) {

            this.xtorDecompTasks.shutdownNow();
            this.xtorConvertTasks.shutdownNow();

            this.cntPendingTasks.set(0);

            this.setDecompFutures.forEach(future -> future.cancel(true));
            this.setConvertFutures.forEach(future -> future.cancel(true));
        }
        
        // Clear out all buffers
        this.queFramesRaw.clear();
        this.queFramesPrcd.clear();
        this.queMsgRequests.clear();
        
        this.bolShutdown = true;
    }
    
    /**
     * <p>
     * Submit the given ingestion frame for processing and conversion to ingest data 
     * request message.
     * </p>
     * <p>
     * If automatic ingestion frame decomposition is enabled the given frame is immediately added 
     * to the raw frame queue buffer where it is available to the processing thread tasks.  
     * It is first checked for allocation size then decomposed if required
     * and transferred to the processed frame queue buffer. If no decomposition is required it is 
     * transferred directly to the processed frame buffer.  All ingestion frames entering the processed frame 
     * buffer are converted to <code>IngestDataRequest</code> messages where they are available through the 
     * <code>{@link IMessageSupplier}</code> interface. 
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * <ul>
     * <li>
     * If ingestion frame decomposition is enabled, the argument will be destroyed
     * if it has memory allocation larger than the given limit.  An offending frame is 
     * decomposed into smaller frames until empty.
     * </li>
     * <br/>
     * <li>
     * If concurrency is enable there is no guarantee that the ordering of the arguments (for repeated invocations) 
     * will be maintained within the resulting <code>IngestDataRequest</code> messages after processing.
     * </li>
     * </p>
     * 
     * @param frame     ingestion frame to be processed
     * 
     * @throws IllegalStateException    processor is inactive
     */
    synchronized
    public void submit(IngestionFrame frame) throws IllegalStateException {

        // Check if active
        if (!this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processor is not active.");
        
        // No concurrency - Process on main thread 
        if (!this.bolConcurrency) {
            this.processFrame(frame);
            
            return;
        }
        
        // No auto-decomposition - Add directly to the processed frame buffer and return
        if (!this.bolDecompAuto) {
            this.queFramesPrcd.add(frame);
            
            return;
        }
        
        // Full processing - Add to the raw frames buffer for decomposition
        this.queFramesRaw.add(frame);
    }
    
    /**
     * <p>
     * Add the given list of ingestion frames for processing and conversion to ingest data 
     * request messages.
     * </p>
     * <p>
     * If automatic ingestion frame decomposition is enabled the given list is immediately added 
     * to the raw frame queue buffer where they are available to the processing thread tasks.  
     * If required they are then decomposed by the decomposition tasks
     * and transferred to the processed frame queue buffer.  If no decomposition is required they 
     * are transferred directly.  All ingestion frames entering the processed frame buffer are then 
     * converted to <code>IngestDataRequest</code> messages where they are available through the 
     * <code>{@link IMessageSupplier}</code> interface. 
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * <ul>
     * <li>
     * If ingestion frame decomposition is enabled, any argument frames will be destroyed
     * if it has memory allocation larger than the given limit.  An offending frame is 
     * decomposed into smaller frames until empty.
     * </li>
     * <br/>
     * <li>
     * If concurrency is enable there is no guarantee that the ordering of the arguments will be
     * maintained within the resulting <code>IngestDataRequest</code> messages after processing.
     * </li>
     * </p>
     * 
     * @param lstFrames     list of ingestion frames for processing
     * 
     * @throws IllegalStateException    the processor is currently inactive
     */
    synchronized
    public void submit(List<IngestionFrame> lstFrames) throws IllegalStateException {

        // Check if active
        if (!this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processor is not active.");
        
        // No concurrency - Process on main thread
        if (!this.bolConcurrency) {
            lstFrames.forEach(frm -> this.processFrame(frm));
            
            return;
        }
        
        // No auto-decomposition - Add directly to the processed frame buffer and return
        if (!this.bolDecompAuto) {
            this.queFramesPrcd.addAll(lstFrames);
            
            return;
        }
        
        // Full Processing - Add to the raw frames buffer for decomposition
        this.queFramesRaw.addAll(lstFrames);
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
    public boolean isSupplying() {
        return this.bolActive || this.hasPendingTasks() || !this.queMsgRequests.isEmpty();
    }

    /**
     *
     * @see com.ospreydcs.dp.api.ingest.model.IMessageSupplier#take()
     */
    @Override
    public IngestDataRequest take() throws IllegalStateException, InterruptedException {
        
        // Check state
        if (!this.bolActive && !this.hasPendingTasks() && this.queMsgRequests.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processing is inactive and queue is empty.");
        
        return this.queMsgRequests.take();
    }

    /**
     *
     * @see com.ospreydcs.dp.api.ingest.model.IMessageSupplier#poll()
     */
    @Override
    public IngestDataRequest poll() throws IllegalStateException {
        
        // Check state
        if (!this.bolActive && !this.hasPendingTasks() && this.queMsgRequests.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processing is inactive and queue is empty.");
        
        return this.queMsgRequests.poll();
    }

    /**
     *
     * @see com.ospreydcs.dp.api.ingest.model.IMessageSupplier#poll(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public IngestDataRequest poll(long cntTimeout, TimeUnit tuTimeout)
            throws IllegalStateException, InterruptedException {
        
        // Check state
        if (!this.bolActive && !this.hasPendingTasks() && this.queMsgRequests.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processing is inactive and queue is empty.");
        
        return this.queMsgRequests.poll(cntTimeout, tuTimeout);
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Processes the given frame into <code>IngestDataRequest</code> message(s) then enqueues in outgoing buffer.
     * </p>
     * <p>
     * This is a single-thread operation where the given frame is (possibly) decomposed then converted to
     * <code>IngestDataRequest</code> message(s).  The resulting message(s) is then placed into the outgoing
     * message buffer <code>{@link #queMsgRequests}</code> for access by the 
     * <code>IMessageSupplier&lt;IngestDataRequest&gt;</code> interface.
     * </p>
     * Any decomposition or conversion failures are recorded in the collections <code>{@link #setFramesFailedDecomp}</code>
     * and <code>{@link #setFramesFailedConvert}</code> respectively.  As a convenience the method also returns
     * a Boolean value indicating success or failure.
     * </p>
     * 
     * @param frame ingestion frame to be converted into <code>IngestDataRequest</code> message(s) and queued
     * 
     * @return  <code>true</code> if processing was successful, <code>false</code> otherwise
     */
    private boolean processFrame(IngestionFrame frame) {
       
        // If auto-decomposition is disabled convert frame directly
        if (!this.bolDecompAuto) {

            return this.convertAndQueueFrame(frame);
        }
        
        // Attempt frame decomposition
        List<IngestionFrame>    lstFrames;
        try { 
            // Try horizontal decomposition - least expensive
            lstFrames = this.prcrMsgBinner.decomposeHorizontally(frame);
            
        } catch (Exception eh) {
            
            // Try vertical decomposition
            try {
                lstFrames = this.prcrMsgBinner.decomposeVertically(frame);
                
            } catch (Exception ev) {
                
                this.registerDecompException(frame, ev);
//                this.setFramesFailedDecomp.add(ev);
                return false;
            }
        }
        
        // Frame decomposition successful - convert each composite to gRPC messages
        boolean bolResult = true;
        for (IngestionFrame frmDecomp : lstFrames) {
            bolResult = bolResult && this.convertAndQueueFrame(frmDecomp);
        }
        
        return bolResult;
    }
    
    /**
     * <p>
     * Converts the given ingestion frame into an ingestion request message then enqueues it.
     * </p>
     * <p>
     * This is a supported method called by <code>{@link #processFrame(IngestionFrame)}</code>. 
     * Attempts to convert the given message to an <code>IngestDataRequest</code> gRPC message
     * using the single-thread converter <code>{@link #prcMsgConverter}</code>.  If the process
     * succeeds the resulting message is enqueued in the outgoing message buffer
     * <code>{@link #queMsgRequests}</code>.  If the process fails the exception is recorded
     * in the collection <code>{@link #setFramesFailedConvert}</code>.
     * </p>
     * 
     * @param frame     ingestion frame to be converted to an ingest data request message and enqueued
     * 
     * @return  <code>true</code> if successful, <code>false</code> if a failure occurred
     */
    private boolean convertAndQueueFrame(IngestionFrame frame) {
        
        try {
            IngestDataRequest   msgRqst = this.prcMsgConverter.createRequest(frame, this.recProviderUid);
            
            return this.queMsgRequests.offer(msgRqst);
        
        } catch (Exception e) {
            
            this.registerConvertException(frame, e);
//            this.setFramesFailedConvert.add(e);
            return false;
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
     * <h2>Ingestion Frame Decomposition</h2>
     * <p>
     * When frame decomposition is active any ingestion frame added to this supplier
     * is decomposed whenever its total memory allocation is larger than the current gRPC message
     * size limitation (default is identified in the client API configuration parameters).  
     * Thus, a single, large ingestion frame added to this supplier will be decomposed into multiple smaller ingestion
     * frame, each meeting the gRPC message size limitation specified in the client API configuration
     * parameters.
     * <p>
     * For an ingestion frame with allocation greater than the The decomposition proceeds as follows:
     * <ol>
     * <li>The memory allocation of the frame is computed and compared to the maximum size limit.</li>
     * <li>A horizontal decomposition (i.e., by column) is first attempted as this is generally the fastest method.</li>
     * <li>A vertical decomposition (i.e., by row) is attempted if the horizontal fails.  This method is more expensive.</li>
     * <li>If both attempts fail the frame is added to the collection of failed decompositions (the error is recorded).</li>
     * </ol>
     * Note that the decomposition process can be non-trivial, thus using concurrency can greatly improve
     * performance.  Even if an ingestion frame does not require decomposition, its memory allocation
     * must be computed, which can be significant depending upon the frame (e.g., large tables).
     * </p>
     * 
     * @return  a new <code>Callable</code> instance performing frame decompositions
     * 
     * throws InterruptedException interrupted while waiting for an available IngestionFrameBinner instance 
     */
    private Callable<Boolean> createFrameDecompositionTask() /* throws InterruptedException */ {
    
        // Define the task operations as a lambda function
        Callable<Boolean>    task = () -> {
            
//            // TODO - Remove
//            // Debugging - Create ID
//            final int   intThrdId = this.cntDecompThrds++;
            
            // Create a new frame binner processor for this thread
            IngestionFrameBinner binner = IngestionFrameBinner.from(this.lngBinSizeMax);

            // While active - Continuously process frames from raw frame buffer
            // - remaining OR conditional(s) allow for soft shutdowns
            while (this.bolActive || this.hasPendingTasks() || !this.queFramesRaw.isEmpty()) {
//            while (this.bolActive || !this.queFramesRaw.isEmpty()) {
                
                // TODO - Change this to take() and implement accordingly - thread interruption
                // Retrieve an unprocessed frame from the queue - blocking until timeout
                //  - This is thread safe according to Java BlockingQueue documentation
                IngestionFrame frmRaw = this.queFramesRaw.poll(INT_TIMEOUT_TASK_POLL, TU_TIMEOUT_TASK_POLL);
                
                // Check for timeout
                if (frmRaw == null)
                    continue;
                
                this.cntPendingTasks.incrementAndGet();
                
//                // TODO - Remove
//                LOGGER.debug("Frame binner thread #" +intThrdId + " activated for raw ingestion frame processing.");
                
                // If there is no automatic decomposition simply all the frame to the processed queue
                if (!this.bolDecompAuto) {
                    this.queFramesPrcd.offer(frmRaw);
                
                    this.cntPendingTasks.decrementAndGet();
                    continue;
                }
                
//                // TODO - Remove
//                LOGGER.debug("Frame binner thread #" +intThrdId + " activated for decomposition.");
                
                // Process: First attempt to decompose the frame horizontally - this is the cheapest
                List<IngestionFrame>    lstFrmsPrcd;
                try {
                    lstFrmsPrcd = binner.decomposeHorizontally(frmRaw);
                    
                } catch (Exception e1) {
                    
                    // If failed: try to decompose the frame vertically - this is expensive
                    try {
                        lstFrmsPrcd = binner.decomposeVertically(frmRaw);
                        
                    } catch (Exception e2) {
                        
                        // If failed again: Record exception, decrement pending counter, and continue
//                        if (BOL_LOGGING)
//                            LOGGER.error("Frame decomposition failed for frame {} with exception {} = {}.", frmRaw.getFrameLabel(), e2.getClass().getName(), e2.getMessage());
//
//                        this.setFramesFailedDecomp.add(e2);
                        this.registerDecompException(frmRaw, e2);
                        this.cntPendingTasks.decrementAndGet();
                        continue;
                    }
                }

                // Enqueue all the decomposed frames in the processed frames buffer
                lstFrmsPrcd.forEach(f -> this.queFramesPrcd.offer(f));

//                // TODO - Remove
//                LOGGER.debug("Frame binner thread {} offered {} frames to processed queue now with size {}.", intThrdId, lstFrmsPrcd.size(), this.queFramesPrcd.size());
                        
//                System.out.println("  binner thread #" +intThrdId + " decomposed raw frame into " + lstFrmsPrcd.size() + " binned frames.");
//                System.out.println("    processed frame queue size = " + this.queFramesPrcd.size());
                
                this.cntPendingTasks.decrementAndGet();
            }
            
//            // TODO - Remove
//            LOGGER.debug("Terminating: binner thread #" +intThrdId);
            
            // Everything was successful if the raw frame buffer is empty
            return !this.bolActive && this.setFramesFailedDecomp.isEmpty() && this.queFramesRaw.isEmpty();
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
            
//            // TODO - Remove
//            // Debugging - Create ID
//            final int   intThrdId = this.cntConvertThrds++;
            
            
            // Ingestion frame converter used for all message creation
            IngestionFrameConverter converter = IngestionFrameConverter.create(this.recProviderUid);
            
            // While active - Continuously convert frames in processed frame buffer to gRPC messages 
            // - second OR conditionals allows for soft shutdowns
            while (this.bolActive || this.hasPendingTasks() || !this.queFramesPrcd.isEmpty()) {
//            while (this.bolActive || !this.queFramesPrcd.isEmpty()) {
                
//                this.cntPending.incrementAndGet();
                
                // TODO - Change this to take() and implement accordingly
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
                
                this.cntPendingTasks.incrementAndGet();
                
//                // TODO - Remove
//                LOGGER.debug("Conversion thread #" +intThrdId + " activated for 1 message conversion.");
                
                // Convert the ingestion frame to an Ingestion Service data request message
                try {
                    IngestDataRequest   msgRqst = converter.createRequest(frmPrcd);

//                    // TODO - Remove
//                    LOGGER.debug("  conversion thread #" +intThrdId + " created message - (msg==null)=" + (msgRqst==null));

                    // Add ingestion request message to outgoing queue
                    this.queMsgRequests.offer(msgRqst);

                } catch (Exception e) {
                    
                    // Acknowledge the conversion failure and continue to the next frame
                    this.registerConvertException(frmPrcd, e);
                    this.cntPendingTasks.decrementAndGet();
                    continue;
                }
                
//                // TODO - Remove
//                LOGGER.debug("  conversion thread #" +intThrdId + " queued 1 message - queue size = " + this.getRequestQueueSize());
                
                this.cntPendingTasks.decrementAndGet();
            }
            
//            // TODO - Remove
//            LOGGER.debug("Terminating: conversion thread #" +intThrdId);
            
            return !this.bolActive && this.queFramesRaw.isEmpty() && this.queFramesPrcd.isEmpty();
        };
        
        return task;
    }
    
    /**
     * <p>
     * Acknowledges an exception in the decomposition processing of the given frame.
     * </p>
     * <p>
     * This method is essentially provided for future upgrades.  It is a central location
     * to acknowledge a the given processing exception for the given ingestion frame.
     * At current nothing is done with the frame itself, however, future version may wish
     * to implement a callback function for clients to immediately receive notifications.
     * The event is logged if logging is active.
     * </p>
     * 
     * @param frame     the offending ingestion frame
     * @param e         the exception thrown by the <code>IngestionFrameBinner</code>
     */
    private void registerDecompException(IngestionFrame frame, Exception e) {
        
        // Add the exception to the collection of frame decomposition failures
        this.setFramesFailedDecomp.add(e);
        
        // Log event
        if (BOL_LOGGING)
            LOGGER.error("Frame decomposition failed for frame {} with exception {} = {}.", frame.getFrameLabel(), e.getClass().getName(), e.getMessage());
    }
    
    /**
     * <p>
     * Acknowledges an exception in the frame-to-message conversion of the given frame.
     * </p>
     * <p>
     * This method is essentially provided for future upgrades.  It is a central location
     * to acknowledge a the given processing exception for the given ingestion frame.
     * At current nothing is done with the frame itself, however, future version may wish
     * to implement a callback function for clients to immediately receive notifications.
     * The event is logged if logging is active.
     * </p>
     * 
     * @param frame     the offending ingestion frame
     * @param e         the exception thrown by the <code>IngestionFrameConverter</code>
     */
    private void registerConvertException(IngestionFrame frame, Exception e) {
        
        // Add the exception to the collection of frame-to-message conversion failures
        this.setFramesFailedConvert.add(e);

        // Log event
        if (BOL_LOGGING)
            LOGGER.error("Frame-to-message conversion failed for frame {} with exception {} = {}.", frame.getFrameLabel(), e.getClass().getName(), e.getMessage());
    }
}
