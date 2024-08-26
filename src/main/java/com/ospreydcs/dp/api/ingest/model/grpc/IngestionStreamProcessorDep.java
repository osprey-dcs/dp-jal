/*
 * Project: dp-api-common
 * File:	IngestionStreamProcessorDep.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IngestionStreamProcessorDep
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
package com.ospreydcs.dp.api.ingest.model.grpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDeprecated;
import com.ospreydcs.dp.api.model.ClientRequestUID;
import com.ospreydcs.dp.api.model.DpGrpcStreamType;
import com.ospreydcs.dp.api.model.IngestionResponse;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataResponse;

/**
 * <p>
 * Provides all processing and management capabilities to transmit <code>IngestionFrame</code>
 * data to the Ingestion Service via gRPC data streams.
 * </p>
 * <p>
 * Class instances perform all processing of client <code>IngestionFrame</code> ingestion data
 * using a <code>{@link IngestionFrameProcessorDeprecated}</code> resource.  There ingestion frames are
 * (optionally) decomposed for gRPC message size requirements, then converted into 
 * <code>{@link IngestDataRequest}</code> messages suitable for transmission to the Ingestion 
 * Service.  
 * </p>
 * <p>
 * Class instances transmit all processed client ingestion data using a collection of 
 * <code>{@link IngestionStream}</code> instances - one for each active gRPC data stream.
 * (If the multiple data streams feature is disabled only one stream will be active.)  
 * The stream classes are executed on separate threads which compete for the 
 * <code>IngestDataRequest</code> messages produced by the <code>IngestionFrameProcessorDeprecated</code>.
 * </p>
 * <h2>Operation</h2>
 * After creation instances of <code>IngestionStreamProcessorDep</code> must be activated using
 * either the <code>{@link #activate(ProviderUID)}</code> or <code>{@link #activate(int)}</code> 
 * method, which requires the data provider UID (either as a record or directly as an integer value)
 * to be given to all outgoing <code>IngestDataRequest</code> messages.  
 * When no longer required the processor should be shutdown using either 
 * <code>{@link #shutdown()}</code>, which allows all processing and transmission to complete, 
 * or <code>{@link #shutdownNow()}</code>, which performs a hard shutdown of the processor
 * immediately terminating all processing and transmission tasks.  
 * </p>
 * <p>
 * It is possible to cycle through repeated activations and shutdowns with a single 
 * <code>IngestionStreamProcessos</code> instance.  For example, a processor can be activated
 * for a specific data provider (with its UID) then shutdown and re-activated for a different
 * data provider.
 * </p>
 * <p>
 * <h3>Activation</h3>
 * After activating the processor instance it is ready for ingestion frame
 * processing, message conversion, and transmission.  
 * Ingestion frames can be added to the processor where they
 * are (optionally) decomposed and converted to <code>IngestDataRequest</code> messages.
 * Processed messages are then transmitted to the Ingestion Service via the gRPC streaming
 * tasks.
 * </p>
 * <p>
 * Activation starts all ingestion frame processing and transmission tasks which are then 
 * continuously active throughout the lifetime of this instance, or explicitly shut down.  
 * Processing and streaming tasks execute independently in
 * thread pools where they block until ingestion frames become available.
 * </p>
 * <p>
 * <h2>Back Pressure</h2>
 * If the client back-pressure feature is enabled the processor may block, until a sufficient
 * number of ingestion frames have been processed and transmitted.  An extended blocking
 * indicates a back log with the Ingestion Service while is apparently being overwhelmed
 * with ingestion requests.
 * </p>
 * <p>
 * The processor maintains a queue of ingestion frames ready for processing and a queue 
 * of processed messages ready for transmission.  If the message queue fills to capacity
 * it will block the addition of more ingestion frames.
 * </p>
 * <p>
 * <h2>Multiple gRPC Streams</h2>
 * Using multiple gRPC data streams can significantly improve transmission performance. 
 * Although a single gRPC channel is used between this client and the Ingest Service,
 * the channel allows the use of multiple, concurrent data streams between client and
 * service.
 * </p>
 * <p>
 * When enabling this feature class instances will create a thread pool of independent
 * tasks each transmitting ingestion data to the Ingestion Service.  Each task will have
 * an independent gRPC data stream for transmission.  Disabling the feature allows only
 * one thread for data transmission using only a single gRPC data stream.
 * </p>
 * <p>
 * The optimal number of gRPC data streams depends upon the client platform, the Ingestion
 * Service, and the host network.  The number of streams should be considered a tuning parameter
 * for each installation.  Using too many streams will burden the data transmission 
 * with unnecessary gRPC overhead while using two few will backlog transmission on the client
 * side.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * At the time of this documentation only the bidirectional streaming operation is supported
 * by the Ingestion Service (i.e., <code>{@link DpGrpcStreamType#BIDIRECTIONAL}</code>).  
 * Attempting to use a unidirectional stream could result in a RPC failure at the 
 * Ingestion Service.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Apr 10, 2024
 * 
 * @deprecated This class was replaced by several classes offering the same collective operation (i.e., a better design) 
 */
@Deprecated(since="Aug, 2024", forRemoval=true)
public final class IngestionStreamProcessorDep {

    
    // 
    // Creators
    //
    
    /**
     * <p>
     * Constructs a new <code>IngestionStreamProcessorDep</code> ready for accepting
     * client <code>IngestionFrame</code> instances to be processed and whose data is
     * transmitted to the Ingestion Service.
     * </p>  
     * <p>
     * Use method <code>{@link #activate(int)}</code> to begin ingesting client data, providing
     * the method with the UID of the data provider UID supplying ingestion data.
     * When data ingestion is over, or to switch to a different data provider, shut down the
     * processor with either <code>{@link #shutdown()}</code> or 
     * <code>{@link #shutdownNow()}</code>. 
     * </p>
     *
     * @param connIngest    a gRPC connection to the Ingestion Service
     * 
     * @return a new <code>IngestionStreamProcessorDep</code> ready for activation and data ingestion
     */
    public static IngestionStreamProcessorDep from(DpIngestionConnection connIngest) {
        return new IngestionStreamProcessorDep(connIngest);
    }
    
    
    //
    // Internal Types
    //
    
//    /**
//     * <p>
//     * Facilitates ingestion frame decomposition on independent execution thread.
//     * </p>
//     * 
//     * @author Christopher K. Allen
//     * @since Apr 10, 2024
//     *
//     */
//    private class BinnerThread implements Runnable {
//
//        // 
//        // Creator
//        //
//        
////        public static BinnerThread from(final IngestionFrameBinner binner, IngestionFrame frame) {
////            return IngestionStreamProcessorDep.this.new BinnerThread(binner, frame);
////        }
//        
//        //
//        // Initialization Targets
//        //
//        
//        /** The active frame decomposition processing */
//        private final IngestionFrameBinner       binner;
//        
//        /** The target ingestion frame to be decomposed */
//        private final IngestionFrame    frame;
//        
//        BinnerThread(final IngestionFrameBinner binner, IngestionFrame frame) {
//            this.binner = binner;
//            this.frame = frame;
//        }
//        
//        @Override
//        public void run() {
//            // TODO Auto-generated method stub
//            
//        }
//        
//    }
    
    //
    // Application Resources
    //
    
    /** The Ingestion Service client API default configuration */
    private static final DpIngestionConfig  CFG_DEFAULT = DpApiConfig.getInstance().ingest; 
    
    
    //
    // Class Constants
    //
    
//    /** Is logging active */
//    private static final Boolean    BOL_LOGGING = CFG_DEFAULT.logging.active;
//
//    
//    /** Are general concurrency active - used for ingestion frame decomposition */
//    private static final Boolean    BOL_CONCURRENCY_ACTIVE = CFG_DEFAULT.concurrency.active;
//    
//    /** Thresold in which to pivot to concurrent processing */
//    private static final Integer    INT_CONCURRENCY_PIVOT_SZ = CFG_DEFAULT.concurrency.pivotSize;
//    
//    /** Maximum number of concurrent processing threads */
//    private static final Integer    INT_CONCURRENCY_CNT_THREADS = CFG_DEFAULT.concurrency.threadCount;
//    
//    
//    /** Are general timeout active */
//    private static final Boolean    BOL_TIMEOUT_ACTIVE = CFG_DEFAULT.timeout.active;
//    
//    /** General timeout limit for operations */
//    private static final Long      LNG_TIMEOUT = CFG_DEFAULT.timeout.limit;
//    
//    /** General timeout time units */
//    private static final TimeUnit  TU_TIMEOUT = CFG_DEFAULT.timeout.unit;
//    
//    
//    /** Allow gRPC data streaming - this is ignored for all streaming operations */
//    private static final Boolean            BOL_STREAM_ACTIVE = CFG_DEFAULT.stream.active;
//    
//    /** The preferred gRPC stream type to Ingestion Service */
//    private static final DpGrpcStreamType   ENM_STREAM_PREF = CFG_DEFAULT.stream.type;
//    
//    
//    /** Use ingestion frame buffering from client to gRPC stream */
//    private static final Boolean    BOL_BUFFER_ACTIVE = CFG_DEFAULT.stream.buffer.active;
//    
    /** Size of the ingestion frame queue buffer */
    private static final Integer    INT_BUFFER_SIZE = CFG_DEFAULT.stream.buffer.size;
    
    /** Allow back pressure to client from queue buffer */
    private static final Boolean    BOL_BUFFER_BACKPRESSURE = CFG_DEFAULT.stream.buffer.backPressure;
    
    
//    /** Perform ingestion frame decomposition (i.e., "binning") */
//    private static final Boolean    BOL_BINNING_ACTIVE = CFG_DEFAULT.stream.binning.active;
//    
//    /** Maximum size limit (in bytes) of decomposed ingestion frame */
//    private static final Integer    LNG_BINNING_MAX_SIZE = CFG_DEFAULT.stream.binning.maxSize;

    
    
    /** General operation timeout limit */
    private static final long       LNG_TIMEOUT_GENERAL = CFG_DEFAULT.timeout.limit;
    
    /** General operation timeout units */
    private static final TimeUnit   TU_TIMEOUT_GENERAL = CFG_DEFAULT.timeout.unit;
    
    
    
    /** The default gRPC stream type */
    private static final DpGrpcStreamType   ENM_STREAM_TYPE = CFG_DEFAULT.stream.type;
    
    
    /** Use multiple gRPC data stream to transmit ingestion frames */
    private static final Boolean    BOL_MULTISTREAM_ACTIVE = CFG_DEFAULT.stream.concurrency.active;
    
//    /** When the number of frames available exceeds this value multiple gRPC data streams are used */ 
//    private static final Long       LNG_MULTISTREAM_PIVOT = CFG_DEFAULT.stream.concurrency.pivotSize;
    
    /** The maximum number of gRPC data stream used to transmit ingestion data */
    private static final Integer    INT_MULTISTREAM_MAX = CFG_DEFAULT.stream.concurrency.maxStreams;
    
    
    //
    // Class Resources
    //
    
    /** Class logger */
    protected static final Logger     LOGGER = LogManager.getLogger();
    
    
    //
    // Defining Attributes
    //
    
    /** The gRPC connection to the Ingestion Service */
    private final DpIngestionConnection     connIngest;
    
    
    //
    // Configuration Parameters
    //
    
    /** gRPC stream type */
    private DpGrpcStreamType    enmStreamType = ENM_STREAM_TYPE;
    
    
    /** Back pressure enabled flag */
    private boolean bolBackPressure = BOL_BUFFER_BACKPRESSURE;
    
    /** Back pressure queue capacity */
    private int     szQueueCapacity = INT_BUFFER_SIZE;
            
    /** Multiple gRPC data streams flag */
    private boolean bolMultistream = BOL_MULTISTREAM_ACTIVE;
    
    /** The maximum number of gRPC data streams to use */
    private int     cntStreamsMax = INT_MULTISTREAM_MAX;
    
    
    // 
    // State Variables
    //
    
    /** Is processor active */
    private boolean bolActive = false;
    
    
    //
    // Instance Resources
    //
    
//    /** Incoming ingestion frame buffer */
//    private final Queue<IngestionFrame>             bufFrames = new LinkedList<>();
//    
//    /** Pool of ingestion frame decomposers (frame binners) */
//    private final BlockingQueue<IngestionFrameBinner>        polBinners = new LinkedBlockingQueue<>(INT_CONCURRENCY_CNT_THREADS);
//    
//    /** Outgoing ingestion request queue */
//    private final BlockingQueue<IngestDataRequest>  queRequests = new LinkedBlockingQueue(INT_BUFFER_SIZE);
//    
//    /** Collection of all outgoing message client request IDs */
//    private final Collection<ClientRequestUID>       setClientIds = new LinkedList<>();
//    
//    /** Source for incoming response messages */
//    private final Consumer<IngestDataResponse>      fncDataSink = (rsp) -> { this.setResponses.add(rsp); };
    
    
//    /** The data provider UID */
//    private Integer                   intProviderId = null;
    
    /** Ingestion frame processor and source of outgoing ingestion requests */
    private IngestionFrameProcessorDeprecated     fncFrameProcessor = null;
    
    /** Collection of executing stream processing tasks  */
    private Collection<IngestionStream> setStreamTasks = null;
    
    /** The pool of (multiple) gRPC streaming thread(s) for recovering requests */
    private ExecutorService             xtorStreamTasks = null;
    
    /** Collection of future results from streaming tasks - used for shutdowns */
    private Collection<Future<Boolean>> setStreamFutures = null;

    
    /** Collection of all incoming ingestion responses - from bidirectional streaming tasks */
    private Collection<IngestDataResponse>      setResponses = null; // = new LinkedList<>();
    
    /** Collection of all ingestion responses with exceptions */
    private Collection<IngestDataResponse>      setBadResponses = null;
    
    
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionStreamProcessorDep</code>.
     * </p>
     * <p>
     * Constructs a new <code>IngestionStreamProcessorDep</code> ready for accepting
     * client <code>IngestionFrame</code> instances to be processed and whose data is
     * transmitted to the Ingestion Service.
     * </p>  
     * <p>
     * Use method <code>{@link #activate(int)}</code> to begin ingesting client data, providing
     * the method with the UID of the data provider UID supplying ingestion data.
     * When data ingestion is over, or to switch to a different data provider, shut down the
     * processor with either <code>{@link #shutdown()}</code> or 
     * <code>{@link #shutdownNow()}</code>. 
     * </p>
     *
     * @param connIngest    a gRPC connection to the Ingestion Service
     */
    public IngestionStreamProcessorDep(DpIngestionConnection connIngest /*, int intProviderId */) {
        this.connIngest = connIngest;
//        this.intProviderId = intProviderId;
        
//        this.fncDataSource = new IngestionFrameProcessorDeprecated(intProviderId);
    }
    
    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Sets the gRPC stream type used to transmit ingestion data to the Ingestion Service.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * At the time of this documentation only the bidirectional streaming operation is supported
     * by the Ingestion Service (i.e., <code>{@link DpGrpcStreamType#BIDIRECTIONAL}</code>).  
     * Attempting to use a unidirectional stream could result in a RPC failure at the 
     * Ingestion Service.
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * <ul>
     * <li>
     * A unidirectional backward stream from the Ingestion Service has no context here.
     * Attempting to set a backward gRPC stream (i.e., <code>{@link DpGrpcStreamType#BACKWARD}</code>) 
     * will throw an exception.
     * </li>
     * <br/>
     * <li>
     * This configuration parameter can only be modified <em>before</em> the processor is 
     * activated with <code>{@link #activate()}</code> , otherwise an exception is throw.
     * </li>  
     * </p>
     * 
     * @param enmStreamType gRPC stream type for data transmission
     * 
     * @throws IllegalStateException            method called while processor is active
     * @throws UnsupportedOperationException    an unsupported stream type was provided
     */
    public void setStreamType(DpGrpcStreamType enmStreamType) throws IllegalStateException, UnsupportedOperationException {

        // Check current state
        if (this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change concurency once activated.");
        
        // Check the argument
        if (enmStreamType == DpGrpcStreamType.BACKWARD)
            throw new UnsupportedOperationException(JavaRuntime.getQualifiedCallerNameSimple() + " - " + enmStreamType + " not supported");
        
        this.enmStreamType = enmStreamType;
    }
    
    /**
     * <p>
     * Enables the use of multiple gRPC stream for transmitting ingestion data.
     * </p>
     * <p>
     * <h2>Multiple gRPC Streams</h2>
     * Using multiple gRPC data streams can significantly improve transmission performance. 
     * Although a single gRPC channel is used between this client and the Ingest Service,
     * the channel allows the use of multiple, concurrent data streams between client and
     * service.
     * </p>
     * <p>
     * When enabling this feature class instances will create a thread pool of independent
     * tasks each transmitting ingestion data to the Ingestion Service.  Each task will have
     * an independent gRPC data stream for transmission.  Disabling the feature allows only
     * one thread for data transmission using only a single gRPC data stream.
     * </p>
     * <p>
     * The optimal number of gRPC data streams depends upon the client platform, the Ingestion
     * Service, and the host network.  The number of streams should be considered a tuning parameter
     * for each installation.  Using too many streams will burden the data transmission 
     * with unnecessary gRPC overhead while using two few will backlog transmission on the client
     * side.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This configuration parameter can only be modified <em>before</em> the processor is 
     * activated with <code>{@link #activate()}</code> , otherwise an exception is throw.
     * </p>
     * 
     * @param cntStreamsMax maximum number of allowable gRPC data streams (>0)
     * 
     * @throws IllegalStateException    method called while processor is active
     * @throws IllegalArgumentException the argument was zero or negative
     */
    synchronized
    public void enableMultipleStreams(int cntStreamsMax) throws IllegalStateException, IllegalArgumentException {

        // Check current state
        if (this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change concurrency once activated.");
        
        // Check the argument
        if (cntStreamsMax <= 0)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedCallerNameSimple() + " - argument must be greater that zero. ");
        
        this.bolMultistream = true;
        this.cntStreamsMax = cntStreamsMax;
    }
    
    /**
     * <p>
     * Disable the use of multiple gRPC stream for transmitting ingestion data.
     * </p>
     * <p>
     * <h2>Multiple gRPC Streams</h2>
     * Using multiple gRPC data streams can significantly improve transmission performance. 
     * Although a single gRPC channel is used between this client and the Ingest Service,
     * the channel allows the use of multiple, concurrent data streams between client and
     * service.
     * </p>
     * <p>
     * When enabling this feature class instances will create a thread pool of independent
     * tasks each transmitting ingestion data to the Ingestion Service.  Each task will have
     * an independent gRPC data stream for transmission.  Disabling the feature allows only
     * one thread for data transmission using only a single gRPC data stream.
     * </p>
     * <p>
     * The optimal number of gRPC data streams depends upon the client platform, the Ingestion
     * Service, and the host network.  The number of streams should be considered a tuning parameter
     * for each installation.  Using too many streams will burden the data transmission 
     * with unnecessary gRPC overhead while using two few will backlog transmission on the client
     * side.
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
    public void disableMultipleStreams() throws IllegalStateException {

        // Check current state
        if (this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change concurrency once activated.");
        
        this.bolMultistream = false;
    }
    
    /**
     * <p>
     * Enables client back pressure from finite capacity ingestion queue buffer.
     * </p>
     * <p>
     * This feature is available to tune the streaming of large numbers of ingestion frames;
     * it allows back pressure from the Ingestion Service to be felt at the client side.
     * The message processor class maintains a queue buffer of ingestion frames to be transmitted 
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
     * <p>
     * <h2>WARNING:</h2>
     * This configuration parameter can only be modified <em>before</em> the processor is 
     * activated with <code>{@link #activate()}</code> , otherwise an exception is throw.
     * </p>
     * 
     * @param intQueueCapacity  capacity of frame buffer before back-pressure blocking
     * 
     * @throws IllegalStateException    method called while processor is active
     */
    synchronized 
    public void enableBackPressure(int intQueueCapacity) throws IllegalStateException, IllegalArgumentException {

        // CHeck state
        if (this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change back pressure once activated.");
        
        // Check argument
        if (intQueueCapacity <= 0)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedCallerNameSimple() + " - argument must be greater that zero. ");
            
        this.bolBackPressure = true;
        this.szQueueCapacity = intQueueCapacity;
    }
    
    /**
     * <p>
     * Disables client back pressure.
     * </p>
     * <p>
     * The back-pressure feature is available to tune the streaming of large numbers of ingestion frames;
     * it allows back pressure from the Ingestion Service to be felt at the client side.
     * The message processor class maintains a queue buffer of ingestion frames to be transmitted 
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
     * <p>
     * <h2>WARNING:</h2>
     * This configuration parameter can only be modified <em>before</em> the processor is 
     * activated with <code>{@link #activate()}</code> , otherwise an exception is throw.
     * </p>
     * 
     * @throws IllegalStateException    method called while processor is active
     */
    synchronized 
    public void disableBackPressure() throws IllegalStateException {

        // CHeck state
        if (this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change back pressure once activated.");

        this.bolBackPressure = false;
    }
    
    
    // 
    // State and Attribute Query
    //
    
    /**
     * <p>
     * Determines whether or not multiple gRPC data streams are used to transport data to the Ingestion Service.
     * </p>
     *  
     * @return  <code>true</code> if multi-streaming is active, <code>false</code> otherwise (only a single stream is used)
     */
    public boolean  hasMultiStream() {
        return this.bolMultistream;
    }
    
    /**
     * <p>
     * Returns the maximum number of concurrent gRPC data streams used to transmit data to the Ingestion Service.
     * </p>
     * <p>
     * If multi-streaming is active this is the number of concurrent data stream used by the processor to 
     * transport data to the Ingestion Service over gRPC.  If multi-streaming is disabled this value should be
     * ignored.
     * </p>
     *  
     * @return
     * 
     * @see #hasMultiStream()
     */
    public int      getMaxDatastreams() {
        return this.cntStreamsMax;
    }
    
    /**
     * <p>
     * Returns the number of independent threads used for ingestion frame processing.
     * </p>
     * <p>
     * Returns the number of threads for both ingestion frame decomposition (if used) and the conversion
     * of ingestion frames to gRPC messages.  Both these tasks are performed on independent threads.
     * </p>
     * 
     * @return  number of concurrent ingestion frame processing tasks
     * 
     * @throws IllegalStateException    the processor was never activated
     */
    public int      getProcessorThreadCount() throws IllegalStateException {

        if (this.fncFrameProcessor == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processor was never activated.");
        
        // There are equal number of threads for both ingestion frame decomposition and frame to gRPC message conversion
        int     cntThds = this.fncFrameProcessor.getConcurrencyCount();
        
        if ( this.fncFrameProcessor.hasFrameDecomposition() )
            cntThds *= 2;
        
        return cntThds;
    }
    
    /**
     * <p>
     * Returns whether or not the processor is currently active.
     * </p>
     * <p>
     * The returned value indicates the current <em>state</em> of the this processor instance.
     * <ul>
     * <li>
     * <code>true</code> - activated state: 
     * A returned value of <code>true</code> indicates that the 
     * <code>{@link #activate(int)}}</code> method has been called and the processor has
     * not been shut down.  In this state the processor instance is able to accept, process
     * and transmit ingestion data.
     * </li>
     * <br/>
     * <li>
     * <code>false</code> - inactive state:   
     * A value <code>false</code> indicates that the processor was
     * never activated, or was activated then shut down.  In either case the processor instance
     * will not accept ingestion data.
     * </li>
     * </ul>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * It is possible to activate and shut down a processor instance multiple times.
     * In this respect, the returned value indicates the phase of this activation cycle.
     * </p>
     * 
     * @return  <code>true</code> if the processor is ready for data transmission,
     *          <code>false</code> the processor is shut down or has never been activated
     */
    public boolean isActive() {
        return this.bolActive;
    }
    
    /**
     * <p>
     * Returns the gRPC data stream type used for data transmission.
     * </p>
     * 
     * @return  <code>{@link DpGrpcStreamType}</code> enumeration constant identifying gRPC stream
     */
    public DpGrpcStreamType getStreamType() {
        return this.enmStreamType;
    }
    
    /**
     * <p>
     * Returns the current size of the queue buffer containing <code>IngestDataRequest</code>
     * messages waiting for transmission.
     * </p>
     * <p>
     * Returns the current size of the ingest data request message queue buffer.  This is the
     * number of <code>IngestDataRequest</code> messages that are currently queued up and waiting 
     * on an available stream processor thread for transmission.
     * </p> 
     * 
     * @return  number of <code>IngestDataRequest<code> messages in the request queue
     * 
     * @throws IllegalStateException    processor was never activated
     */
    public int getRequestQueueSize() throws IllegalStateException {
        
        // Check state
        if (this.fncFrameProcessor == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processor was never activated.");
        
        return this.fncFrameProcessor.getRequestQueueSize();
    }

    /**
     * <p>
     * Returns the number of <code>IngestDataRequest</code> messages transmitted to the 
     * Ingestion Service so far.
     * </p>
     * <p>
     * The returned value is the number of Protocol Buffers messages carrying ingestion
     * data that have been transmitted to the Ingestion Service at the time of invocation.
     * If called after invoking <code>{@link #shutdown()}</code> then the returned value
     * is the total number of messages transmitted while active.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The value returned by this method is not necessary equal to the number of 
     * <code>IngestionFrame</code> instances ingested.  If ingestion frame decomposition
     * is active large ingestion frame exceeding the size limit which be decomposed into
     * smaller ingestion frames before being converted into <code>IngestDataRequest</code>
     * messages.
     * </li>
     * <br/>
     * <li>
     * This value is available after a shutdown operation has been called.  At that time
     * the returned value is the total number of <code>IngestDataRequest</code> messages
     * transmitted to the Ingestion Service during that activation cycle.
     * </li> 
     * </ul>
     * </p>
     *   
     * @return  the number of <code>IngestDataRequest</code> messages transmitted so far
     * 
     * @throws IllegalStateException    processor was never activated
     */
    public int getRequestCount() throws IllegalStateException {
        
        // Check if activated
        if (this.setStreamTasks == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processor was never activated.");

//        int cntRequests = 0;
//        for (IngestionStream stream : this.setStreamTasks) {
//            cntRequests += stream.getRequestCount();
//        }
//        return cntRequests;
//        
//        List<Integer>   lstRqstCnts = this.setStreamTasks
//                .stream()
//                .<Integer>map(IngestionStream::getRequestCount)
//                .toList();
//        
//        return this.setStreamTasks
//                .stream()
//                .<Integer>map(stream -> stream.getRequestCount())
//                .reduce(0, Integer::sum);
        
        return this.setStreamTasks
                .stream()
                .mapToInt(IngestionStream::getRequestCount)
                .sum();
    }
    
    /**
     * <p>
     * Returns the number of <code>IngestDataResponse</code> messages received from the
     * Ingestion Service so far.
     * </p>
     * <p>
     * For a bidirectional gRPC data stream the Ingestion Service will send a response for
     * every ingestion request it receives.  In that case, this method returns the number of 
     * responses received so far, which (ideally) should be equal to the value returned by
     * <code>{@link #getRequestCount()}</code>.  For unidirectional gRPC streaming the
     * Ingestion Service will return at most one response.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * At the time of this documentation the Ingestion Service only supports bidirectional
     * gRPC data streams.
     * </li>
     * <br/>
     * <li>
     * This value is available after a shutdown operation has been called.  At that time
     * the returned value is the total number of <code>IngestDataRequest</code> messages
     * transmitted to the Ingestion Service during that activation cycle.
     * </li> 
     * </ul>
     * </p>
     * 
     * @return  the number of <code>IngestDataResponse</code> messages received so far
     * 
     * @throws IllegalStateException    processor was never activated
     */
    public int getResponseCount() throws IllegalStateException {
        
        // Check if activated
        if (this.setStreamTasks == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processor was never activated.");

        return this.setStreamTasks
                .stream()
                .mapToInt(IngestionStream::getResponseCount)
                .sum();
    }
    
    /**
     * <p>
     * Returns an immutable list of client request IDs within all the <code>IngestDataRequest</code>
     * messages set during the current session.
     * </p>
     * <p>
     * Every ingest data request message contains a "client request ID" that should provide the
     * unique identifier for that request.  The Ingestion Service records the identifier for later
     * query.  Additionally, when using bidirectional data streams the Ingestion Service will 
     * provide either an acknowledgment of data received for that ingestion message, or an 
     * exception.  See <code>{@link #getIngestionResponses()}</code> and 
     * <code>{@link #getIngestionExceptions()}</code>.
     * </p>  
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The size of this list should be the value returned by <code>{@link #getRequestCount()}</code>.
     * </li>
     * <li>
     * The ordering of this list does not necessarily reflect the order that ingestion frames
     * were offered to the processor.  Processed ingestion request messages do not necessarily
     * get transmitted in order.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  all client request IDs for messages transmitted to Ingestion Service.
     * 
     * @throws IllegalStateException    processor was never activated
     * 
     * @see #getIngestionResponses()
     * @see #getIngestionExceptions()
     */
    public List<ClientRequestUID>    getRequestIds() throws IllegalStateException {
        
        // Check if activated
        if (this.setStreamTasks == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processor was never activated.");

        List<ClientRequestUID>   lstIds = this.setStreamTasks
                .stream()
                .<ClientRequestUID>flatMap(stream -> stream.getRequestIds().stream())
                .toList();
        
        return lstIds;
    }
    
    /**
     * <p>
     * Return a list of all the responses to the ingest data request messages.
     * </p>
     * <p>
     * This method is only practical when using bidirectional gRPC data streams where the
     * Ingestion Service sents a response corresponding to every ingestion request.
     * For unidirectional gRPC data streams the Ingestion Service sends at most one
     * reply.  
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The size of this list should be the value returned by <code>{@link #getResponseCount()}</code>.
     * </li>
     * <li>
     * The ordering of this list does not necessarily reflect the order that ingestion frames
     * were offered to the processor.  Processed ingestion request messages do not necessarily
     * get transmitted in order.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  a list of all Ingestion Service responses to ingestion requests
     * 
     * @throws IllegalStateException    processor was never activated
     */
    public List<IngestionResponse>   getIngestionResponses() throws IllegalStateException {

        // Check if activated
        if (this.setStreamTasks == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processor was never activated.");

        List<IngestionResponse> lstRsps = new ArrayList<>(this.setResponses.size());
        for (IngestDataResponse msgRsp : this.setResponses) {
            IngestionResponse   recRsp = ProtoMsg.toIngestionResponse(msgRsp);
            
            lstRsps.add(recRsp);
        }
//        List<IngestionResponse> lstRsps = this.setResponses
//                .stream()
//                .<IngestionResponse>map(ProtoMsg::toIngestionResponse)
//                .toList();
        
        return lstRsps;
    }
    
    /**
     * <p>
     * Returns a list of all ingestion responses reporting an exception with the corresponding
     * ingestion request.
     * </p>
     * <p>
     * This method is only practical when using bidirectional gRPC data streams where the
     * Ingestion Service sents a response corresponding to every ingestion request.
     * For unidirectional gRPC data streams the Ingestion Service sends at most one
     * reply.
     * </p>
     * <p>
     * The processor monitors incoming responses from the Ingestion Service for exceptions.
     * The returned list is the collection of all responses indicating an exception with
     * its corresponding request, which may be identified by the client request ID.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Any ingestion request message identified in list (i.e., by client request ID) had
     * some type of exception encountered immediately by the Ingestion Service.  The exception
     * details are included in the response.
     * </li>
     * <li>
     * If a client request ID does not appear in the returned list there is no guarantee that
     * an error did not occur later in the Ingestion Service processing and archiving.
     * One may query the Ingestion Service, via client request ID, for all client ingestion
     * requests that failed archiving. 
     * </li>
     * </ul>
     * </p>
     *   
     * @return  the collection of Ingestion Service responses reporting an exception
     * 
     * @throws IllegalStateException    processor was never activated
     */
    public List<IngestionResponse>    getIngestionExceptions() throws IllegalStateException {
        
        // Check if activated
        if (this.setStreamTasks == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processor was never activated.");

        return this.setBadResponses
                .stream()
                .<IngestionResponse>map(ProtoMsg::toIngestionResponse)
                .toList();
    }
    
    
    //
    // Operations
    //
    
//    /**
//     * <p>
//     * Activates the ingestion stream processor for the given data provider UID.
//     * </p>
//     * <p>
//     * This method extracts the integer-valued data provider UID from the argument
//     * and defers to <code>{@link #activate(int)}</code>.  See documentation for
//     * the <code>{@link #activate(int)}</code> method for full details.
//     * </p>
//     * 
//     * @param recProviderId the data provider UID used in all ingest data request messages 
//     * 
//     * @return  <code>true</code> if the processor was successfully activated,
//     *          <code>false</code> if the processor was already active
//     * 
//     * @throws RejectedExecutionException (internal) unable to start a streaming task
//     * 
//     * @see #activate(int)
//     */
//    synchronized 
//    public boolean activate(ProviderUID recProviderId) {
//        return this.activate(recProviderId.uid());
//    }
    
    /**
     * <p>
     * Activates the ingestion stream processor for the given data provider UID.
     * </p>
     * <p>
     * Once this method returns all ingestion frame processing tasks and gRPC streaming
     * tasks are active.  All <code>IngestionFrame</code> instances offered to the
     * active stream processor will be assigned the given data provider UID.
     * </p> 
     * <p>
     * After invoking this method the processor instance is ready for ingestion frame
     * processing, message conversion, and transmission.  
     * Ingestion frames can be added to the processor where they
     * are (optionally) decomposed and converted to <code>IngestDataRequest</code> messages.
     * Processed messages are then transmitted to the Ingestion Service via the gRPC streaming
     * tasks.
     * </p>
     * <h2>Operation</h2>
     * This method starts all ingestion frame processing and transmission tasks which are then 
     * continuously active throughout the lifetime of this instance, or explicitly shut down.  
     * Processing and streaming tasks execute independently in
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
     * This method can be called repeatedly for different data providers (after proper shut down).
     * </li>
     * <li>
     * A shutdown operation should always be invoked when the processor is no longer needed.
     * </li>
     * </ul>
     * </p>
     * 
     * @param recProviderId the data provider UID used in all ingest data request messages 
     * 
     * @return  <code>true</code> if the processor was successfully activated,
     *          <code>false</code> if the processor was already active
     * 
     * @throws RejectedExecutionException (internal) unable to start a streaming task
     */
    synchronized 
    public boolean activate(ProviderUID recProviderId) throws RejectedExecutionException {
        
        // Check our state
        if (this.bolActive)
            return false;
        
        // Set activation flag before launching threads
        this.bolActive = true;
        
//        // Store the data provider UID
//        this.intProviderId = intProviderId;
        
        // Create the ingestion frame processor and activate it (all other default parameters)
        this.fncFrameProcessor = IngestionFrameProcessorDeprecated.from(recProviderId);
        if (this.bolBackPressure)
            this.fncFrameProcessor.enableBackPressure(this.szQueueCapacity);
        this.fncFrameProcessor.activate();
        
        // Determine the number of streams
        int cntStreams = 1;
        if (this.bolMultistream) 
            cntStreams = this.cntStreamsMax;
        
        // Create the thread pool executor
        this.xtorStreamTasks = Executors.newFixedThreadPool(cntStreams);
        this.setStreamFutures = new LinkedList<>();
        this.setStreamTasks = new LinkedList<>();
        this.setResponses = new LinkedList<>();
        this.setBadResponses = new LinkedList<>();
        
        // Create the streaming tasks and submit them
        for (int iStream=0; iStream<cntStreams; iStream++) {
            IngestionStream tskStream = this.createIngestionStream();
            
            Future<Boolean> futStream = this.xtorStreamTasks.submit((Callable<Boolean>)tskStream);
            this.setStreamFutures.add(futStream);
            this.setStreamTasks.add(tskStream);
        }
        
        return true;
    }
    
    /**
     * <p>
     * Performs an orderly shutdown of all ingestion frame processing and gRPC streaming operations.
     * </p>
     * <p>
     * All ingestion frames that have already been submitting are allowed to process and transmit
     * to the Ingestion Service.   However, no new ingestion frames will be accepted.
     * This method will block until all pending data has been processed and transmitted. 
     * </p>  
     * 
     * @return  <code>true</code> if everything was shutdown, 
     *          <code>false</code> if processors was already shutdown or an error occurred during operation
     *          
     * @throws InterruptedException process interrupted while waiting for pending operations to complete
     */
    synchronized
    public boolean shutdown() throws InterruptedException {
        
        // Check state
        if (!this.bolActive)
            return false;

        // Shutdown the ingestion frame processor then wait for request queue empty
        this.fncFrameProcessor.shutdown();                  // waits for all pending tasks  
        this.fncFrameProcessor.awaitRequestQueueEmpty();    // waits for stream task to consume requests
        
        // Shutdown all streaming tasks 
        this.xtorStreamTasks.shutdown();
        this.setStreamTasks.forEach(task -> task.terminate());
        this.setStreamFutures.forEach(future -> future.cancel(false));
        
        // Wait for all pending streaming tasks to complete - all should be complete at this point
        boolean bolResult = this.xtorStreamTasks.awaitTermination(LNG_TIMEOUT_GENERAL, TU_TIMEOUT_GENERAL);
//        this.setStreamFutures.clear();
        
        this.bolActive = false;

        return bolResult;
    }
    
    /**
     * <p>
     * Performs a hard shutdown of all ingestion frame processing and gRPC streaming operations.
     * </p>
     * <p>
     * All current data processing and transmission is terminated immediately.  Any ingestion
     * frame pending processing are cleared.  All pending ingest data requests are cleared.
     * This method returns immediately.
     * </p> 
     * 
     * @return  <code>true</code> if everything was shutdown, 
     *          <code>false</code> if processors was already shutdown
     */
    synchronized
    public boolean shutdownNow() {
        
        // Check state
        if (!this.bolActive)
            return false;

        // Hard shutdown of ingestion frame processor
        this.fncFrameProcessor.shutdownNow();
        
        // Hard shutdown of all streaming tasks
        this.xtorStreamTasks.shutdownNow();
        
        this.setStreamTasks.forEach(task -> task.terminate());
        this.setStreamFutures.forEach(future -> future.cancel(true));
//        this.setStreamFutures.clear();
        
        this.bolActive = false;
        
        return true;
    }
    
    /**
     * <p>
     * Allows clients to block until the request message queue is ready (below capacity).
     * </p>
     * <p>
     * This method allows clients to wait for the <code>IngestDataRequest</code> buffer
     * to drop below its capacity limitation.  Allows clients do manage their own blocking
     * at the ingestion side rather than used the build in back-pressure mechanism. 
     * This activity may be useful when clients wish to due their own performance tuning.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method will return immediately if the message queue is below capacity.
     * </li>
     * <li>
     * This method is thread safe and multiple clients can block on this method.
     * </li>
     * <li>
     * All clients blocking on this method will unblock when the message buffer falls below capacity.
     * </li>
     * </ul>
     * </p> 
     * 
     * @throws IllegalStateException    operation invoked while processor inactive
     * @throws InterruptedException     operation interrupted while waiting for queue ready
     */
    public void awaitRequestQueueReady() throws IllegalStateException, InterruptedException {
        
        // Check current state
        if (!this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processor is not active.");
        
        this.fncFrameProcessor.awaitQueueReady();
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

        // Check current state
        if (!this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processor is not active.");
        
        this.fncFrameProcessor.awaitRequestQueueEmpty();
    }
    
    /**
     * <p>
     * Processes and transmits the ingestion frame data to the Ingestion Service.
     * </p>
     * <p>
     * The ingestion frame is added to the processing queue where it will
     * be (optionally) decomposed, converted to an <code>IngestDataRequest</code> message,
     * then transmitted to the Ingestion Service on a gRPC data stream.
     * This method blocks until the ingestion frame is enqueued in the processor queue.
     * </p>
     * <p>
     * <h2>Back Pressure</h2>
     * If the client back-pressure feature is enabled this method may block until a sufficient
     * number of ingestion frames have been processed and transmitted.  An extended blocking
     * indicates a back log with the Ingestion Service while is apparently being overwhelmed
     * with ingestion requests.
     * </p>
     * <p>
     * The processor maintains a queue of ingestion frames ready for processing and a queue 
     * of processed messages ready for transmission.  If the message queue fills to capacity
     * it will block the addition of more ingestion frames.
     * </p>
     * <p>
     * Clients can wait for a request queue empty event using the method
     * <code>{@link #awaitRequestQueueEmpty()}</code>.
     * </p>
     * 
     * @param frame ingestion frame to be processed and transmitted to Ingestion Service
     * 
     * @throws IllegalStateException    operation invoked while processor inactive
     * @throws InterruptedException     interrupted while waiting for message buffer ready
     */
    public void transmit(IngestionFrame frame) throws IllegalStateException, InterruptedException {

        this.transmit(List.of(frame));
    }
    
    /**
     * <p>
     * Processes and transmits all data of all ingestion frames within the argument
     * to the Ingestion Service.
     * </p>
     * <p>
     * The list of ingestion frames is added to the processing queue where they will
     * be (optionally) decomposed, converted to <code>IngestDataRequest</code> messages,
     * then transmitted to the Ingestion Service on a gRPC data stream.
     * This method blocks until all ingestion frames are enqueued in the processor queue.
     * </p>
     * <p>
     * <h2>Back Pressure</h2>
     * If the client back-pressure feature is enabled this method may block until a sufficient
     * number of ingestion frames have been processed and transmitted.  An extended blocking
     * indicates a back log with the Ingestion Service while is apparently being overwhelmed
     * with ingestion requests.
     * </p>
     * <p>
     * The processor maintains a queue of ingestion frames ready for processing and a queue 
     * of processed messages ready for transmission.  If the message queue fills to capacity
     * it will block the addition of more ingestion frames.
     * </p>
     * <p>
     * Clients can wait for a request queue empty event using the method
     * <code>{@link #awaitRequestQueueEmpty()}</code>.
     * </p>
     * 
     * @param lstFrames collection of ingestion frames to be processed and transmitted to Ingestion Service
     * 
     * @throws IllegalStateException    operation invoked while processor inactive
     * @throws InterruptedException     interrupted while waiting for message buffer ready
     */
    public void transmit(List<IngestionFrame> lstFrames) throws IllegalStateException, InterruptedException {
        
        // Check current state
        if (!this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change concurency once activated.");
        
        // Add ingestion frames to the ingestion frame processor - the rest is automatic
        this.fncFrameProcessor.addFrames(lstFrames);
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates a new <code>IngestionStream</code> according to the current configuration.
     * </p>
     * <p>
     * The returned instance is ready for thread execution or execution within an executor
     * service, as it exposes both <code>Runnable</code> and <code>Callable</code>.
     * </p>
     * 
     * @return a new <code>IngestionStream</code> instance ready for thread execution
     * 
     * @throws UnsupportedOperationException    an unsupported ingestion stream type was specified
     */
    private IngestionStream   createIngestionStream() throws UnsupportedOperationException {
        
        // The returned object
        IngestionStream     stream;
        
        // Create the stream task based upon forward unidirectional stream type
        if (this.enmStreamType == DpGrpcStreamType.FORWARD)
            stream = IngestionStream.newUniProcessor(this.connIngest.getStubAsync(), this.fncFrameProcessor);

        // Create the stream task based upon bidirectional stream type
        else if (this.enmStreamType == DpGrpcStreamType.BIDIRECTIONAL) {
            
            // Need a consumer of response messages
            Consumer<IngestDataResponse>    fncDataSink = (msgRsp) -> { this.processResponse(msgRsp); };
            
            stream = IngestionStream.newBidiProcessor(this.connIngest.getStubAsync(), this.fncFrameProcessor, fncDataSink);
        }

        else
            throw new UnsupportedOperationException(JavaRuntime.getQualifiedCallerNameSimple() + " - attempted to use an unsupported stream type " + this.enmStreamType);

        return stream;
    }
    
    /**
     * <p>
     * Processes the ingestion response message.
     * </p>
     * <p>
     * For normal operation, a <code>AckResult</code> message is confirmed within the argument.
     * If confirmed the argument is then passed to the message consumer identified at construction.
     * A SUCCESS result record is then returned indicating successful consumption of response data.
     * </p>
     * <p>
     * The argument is passed to the message consumer identified at construction.
     * If the argument contains an exception(i.e., rather than data) then the message
     * is added to the list of bad responses.
     * </p>
     * 
     * @param msgRsp    an Ingestion Service response message containing acknowledgment or status error
     * 
     */
    private void processResponse(IngestDataResponse msgRsp) {

        // Check for exception then pass to message consumer
        if (msgRsp.hasExceptionalResult()) {

            this.setBadResponses.add(msgRsp);
        }
        
        this.setResponses.add(msgRsp);
    }

    /**
     * <p>
     * Determines whether or not the original data request was accepted by the Query Service.
     * </p>
     * <p>
     * This method should be called only once, with the argument being the first response
     * from the Query Service after stream initiation.  If the original data query
     * request was invalid or corrupt the Query Service streams back a single 
     * <code>QueryResponse</code> message containing a <code>ExceptionalResult</code> message
     * describing the rejection.
     * </p> 
     * <p>
     * The method Checks for a <code>ExceptionalResult</code> message within the query response 
     * argument.  If present then the original data request was rejected by the Query Service.
     * The details of the rejection are then extracted from the argument and returned in
     * a FAILED result record.  Otherwise (i.e., the request was accepted) the method 
     * returns the SUCCESS result record.
     * </p> 
     * 
     * @param msgRsp    the first response from the Query Service data stream
     * 
     * @return  the SUCCESS record if query was accepted, 
     *          otherwise a failure message containing a description of the rejection
     *          
     * @deprecated This method requires the RequestStatus API for the Ingestion Service
     */
    @Deprecated
    private ResultStatus isRequestAccepted(IngestDataResponse msgRsp) {

        // Check for RequestRejected message
        if (msgRsp.hasExceptionalResult()) {
            ExceptionalResult           msgException = msgRsp.getExceptionalResult();
            String                      strCause = msgException.getMessage();
            ExceptionalResult.ExceptionalResultStatus     enmCause = msgException.getExceptionalResultStatus();

            String       strMsg = "The data request was rejected by Query Service: cause=" + enmCause + ", message=" + strCause;
            ResultStatus result = ResultStatus.newFailure(strMsg);

            return result;
        }

        // No rejection, return success
        return ResultStatus.SUCCESS;
    }

//    /**
//     * <p>
//     * Processes the data within the given Ingestion Service response message.
//     * </p>
//     * <p>
//     * For normal operation, a <code>AckResult</code> message is confirmed within the argument.
//     * If confirmed the argument is then passed to the message consumer identified at construction.
//     * A SUCCESS result record is then returned indicating successful consumption of response data.
//     * </p>
//     * <p>
//     * If the argument contains an exception(i.e., rather than data) then the method
//     * extracts the status and accompanying message.  An error message is constructed
//     * and returned via a FAILED result record.
//     * </p>
//     * 
//     * @param msgRsp    an Ingestion Service response message containing acknowledgment or status error
//     * 
//     * @return  the SUCCESS record if acknowledgment was confirmed,
//     *          otherwise a failure message containing the status error description 
//     */
//    protected ResultStatus processResponse(IngestDataResponse msgRsp) {
//
//        // Confirm acknowledgment and pass to message consumer
//        if (msgRsp.hasAckResult()) {
//            this.fncDataSink.accept(msgRsp);
//
//            return ResultStatus.SUCCESS;
//        }
//
//        // Response Error - extract the details and return them
//        ExceptionalResult   msgException = msgRsp.getExceptionalResult();
//        String              strStatus = msgException.getMessage();
//        ExceptionalResult.ExceptionalResultStatus enmStatus = msgException.getExceptionalResultStatus();
//
//        String          strMsg = "Ingestion Service reported response error: status=" + enmStatus + ", message= " + strStatus;
//        ResultStatus    recErr = ResultStatus.newFailure(strMsg);
//
//        return recErr;
//    }

}
