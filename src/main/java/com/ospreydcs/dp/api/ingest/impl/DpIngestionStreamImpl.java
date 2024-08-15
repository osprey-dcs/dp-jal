/*
 * Project: dp-api-common
 * File:	DpIngestionStreamImpl.java
 * Package: com.ospreydcs.dp.api.ingest.impl
 * Type: 	DpIngestionStreamImpl
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
 * @since Aug 14, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.impl;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnectionFactory;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiBase;
import com.ospreydcs.dp.api.ingest.DpIngestionException;
import com.ospreydcs.dp.api.ingest.IIngestionStream;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.model.IMessageSupplier;
import com.ospreydcs.dp.api.ingest.model.IResourceConsumer;
import com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor;
import com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel;
import com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer;
import com.ospreydcs.dp.api.ingest.model.grpc.ProviderRegistrationService;
import com.ospreydcs.dp.api.model.ClientRequestId;
import com.ospreydcs.dp.api.model.DpGrpcStreamType;
import com.ospreydcs.dp.api.model.IngestionResponse;
import com.ospreydcs.dp.api.model.ProviderRegistrar;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceStub;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * Implementation of the <code>IIngestionStream</code> interface.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 14, 2024
 *
 */
public class DpIngestionStreamImpl extends
DpServiceApiBase<DpIngestionStreamImpl, 
DpIngestionConnection, 
DpIngestionServiceGrpc, 
DpIngestionServiceBlockingStub, 
DpIngestionServiceFutureStub, 
DpIngestionServiceStub> implements IIngestionStream {


    //
    // Application Resources
    //

    /** Default Query Service configuration parameters */
    private static final DpIngestionConfig  CFG_DEFAULT = DpApiConfig.getInstance().ingest;


    //
    // Class Types
    //
    
    private static class DataTransferThread implements Runnable {

        //
        // Class Constants
        //
        
        private static final long       LNG_TIMEOUT = 15;
        
        private static final TimeUnit   TU_TIMEOUT = TimeUnit.MILLISECONDS;
        
        //
        // Transfer Targets
        //
        
        private final IMessageSupplier<IngestDataRequest>   dataSource;
        
        private final IResourceConsumer<IngestDataRequest>  dataSink;

        
        //
        // State Variables
        //
        
        private boolean bolComplete = false;
        
        
        private DataTransferThread(IMessageSupplier<IngestDataRequest> dataSource, IResourceConsumer<IngestDataRequest> dataSink) {
            this.dataSource = dataSource;
            this.dataSink = dataSink;
        }
        
        @Override
        public void run() {
            
            while (this.dataSource.isSupplying()) {
                try {
                    IngestDataRequest   msgRqst = this.dataSource.poll(LNG_TIMEOUT, TU_TIMEOUT);
                    
                    this.dataSink.offer(msgRqst);
                    
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
        }
        
    }
    
    
    //
    // Class Constants 
    //

    /** Logging active flag */
    private static final boolean    BOL_LOGGING = CFG_DEFAULT.logging.active;
    
    
    /** General timeout parameters (e.ge., used for awaitTermination(long, TimeUnit) */
    private static final boolean    BOL_TIMEOUT_WAIT = CFG_DEFAULT.timeout.active;

    /** General timeout parameters (e.ge., used for awaitTermination(long, TimeUnit) */
    private static final long       LNG_TIMEOUT_WAIT = CFG_DEFAULT.timeout.limit;

    /** General timeout parameters (e.ge., used for awaitTermination(long, TimeUnit) */
    private static final TimeUnit   TU_TIMEOUT_WAIT = CFG_DEFAULT.timeout.unit;
    
    
    /** Staging buffer polling timeout limit (must poll to avoid thread lock) */
    private static final long       LNG_TIMEOUT_POLL = 15;
    
    /** Staging buffer polling timeout limit (must poll to avoid thread lock) */
    private static final TimeUnit   TU_TIMEOUT_POLL = TimeUnit.MILLISECONDS;


    //
    // Class Constants - Frame Decomposition Default Values
    //

    /** Are general concurrency active - used for ingestion frame decomposition */
    private static final Boolean    BOL_CONCURRENCY_ACTIVE = CFG_DEFAULT.concurrency.active;
    
    /** Maximum number of concurrent processing threads */
    private static final Integer    INT_CONCURRENCY_CNT_THREADS = CFG_DEFAULT.concurrency.threadCount;
    
    
    /** Perform ingestion frame decomposition (i.e., "binning") */
    private static final Boolean    BOL_DECOMP_ACTIVE = CFG_DEFAULT.decompose.active;
    
    /** Maximum size limit (in bytes) of decomposed ingestion frame */
    private static final Integer    LNG_DECOMP_MAX_ALLOC = CFG_DEFAULT.decompose.maxSize;
    
    
    //
    // Class Constants - Ingestion Throttling Default Values
    //

    /** Allow ingestion throttling from staging buffer to client */
    private static final Boolean    BOL_STAGING_BACKPRESSURE = CFG_DEFAULT.stream.buffer.backPressure;

    /** Default staging capacity - maximum number of <code>IngestDataRequest</code> messages */
    private static final int        INT_STAGING_MAX_MSGS = CFG_DEFAULT.stream.buffer.size;

    /** Default staging capacity - maximum memory allocation */
    private static final Long       LNG_STAGING_MAX_ALLOC = CFG_DEFAULT.stream.buffer.allocation;
    
    
    //
    // Class Resources
    //

    /** Class event logger */
    private static final Logger LOGGER = LogManager.getLogger();


    //
    // Instance Resources
    //
    
    /** Autonomous processor of <code>IngestionFrame</code> instances into <code>IngestDataRequest</code> messages */
    private final IngestionFrameProcessor   prcrFrames; // = new IngestionFrameProcessor();
    
    /** Intermediate staging buffer for processed messages awaiting transmission (accommodates ingestion throttling) */
    private final IngestionDataBuffer       buffStaging; // = new IngestionDataBuffer();
    
    /** Autonomous data channel to DP Ingestion Service */
    private final IngestionChannel          chanIngest; // = IngestionChannel.from(this.buffStaging, super.grpcConn);
    
    
    /** The processor to staging buffer task (independent thread) - created when stream opened */
    private Thread      thdXferTask = null;
    
    
    //
    // Configuration
    //
    
//    /** Employ automatic decomposition of incoming ingestion frames */
//    private boolean     bolDecompAuto = BOL_DECOMP_ACTIVE;
//    
//    /** Maximum memory allocation of incoming ingestion frames */
//    private long        szDecompFrameMax = LNG_DECOMP_MAX_ALLOC;
//    
//    /** Use concurrency in frame decomposition */
//    private boolean     bolDecompConcurrent = BOL_CONCURRENCY_ACTIVE;
//    
//    /** Number of processing threads for automatic frame decomposition */ 
//    private int         cntDecompThrds = INT_CONCURRENCY_CNT_THREADS;
    
    
    /** Ingestion throttling switch */
    private boolean     bolBackPressure = BOL_STAGING_BACKPRESSURE;
    
//    /** The maximum capacity (in bytes) of the staging queue buffer - used in ingestion throttling */
//    private long        szStagingCapacity = LNG_STAGING_MAX_ALLOC;
    
    
    //
    // State Variables
    //

    /** UID for the data provider using the current stream */
    private ProviderUID     recProviderUid = null;


    /** Stream opened flag */
    private boolean         bolStreamOpen = false;
    
    /** Stream error flag */
    private boolean         bolStreamError = false;
    
    
    /** The number of ingest data messages transmitted */
    private int             cntMsgsXmit = 0;
    
    /** The total data transmitted (in bytes) */
    private long            szDataXmit = 0;
    
    /** Status result of staging buffer transfer operations */
    private ResultStatus    recXferStatus = null;
    
    

    //
    // Constructors
    //

    /**
     * <p>
     * Constructs a new instance of <code>DpIngestionStreamImpl</code> attached to the given
     * Ingestion Service connection.
     * </p>
     * <p>
     * The argument should be obtained from the appropriate connection factory,
     * specifically, <code>{@link DpIngestionConnectionFactory}</code>.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * The returned object should be shut down when no longer needed using 
     * <code>{@link #shutdown()}</code> or <code>{@link #shutdownNow()}</code>.  
     * This action is necessary to release unused gRPC resources and maintain 
     * overall performance.  
     * </p>
     * 
     * @param connIngest  the gRPC channel connection to the desired DP Ingestion Service
     * 
     * @see DpIngestionConnectionFactory
     */
    protected DpIngestionStreamImpl(DpIngestionConnection connIngest) {
        super(connIngest);
        
        this.prcrFrames = IngestionFrameProcessor.create();
        this.buffStaging = IngestionDataBuffer.create();
        this.chanIngest = IngestionChannel.from(this.buffStaging, super.grpcConn);
        
        // We handle ingestion throttling directly
        this.buffStaging.disableBackPressure();
    }

    
    //
    // Configuration
    //
    
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
    public void setFrameDecompConcurrency(int cntThreads) throws IllegalStateException {
        
        if (this.bolStreamOpen)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change concurency once stream opened.");
        
//        this.bolDecompConcurrent = true;
//        this.cntDecompThrds = cntThreads;
        
        this.prcrFrames.setConcurrency(cntThreads);;
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
     * @param szMaxFrmAlloc maximum allowable size (in bytes) of decomposed ingestion frames 
     */
    public void setFrameDecomposition(long szMaxFrmAlloc) {

        //        if (this.bolActive)
        //            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change concurency once activated.");

//        this.bolDecompAuto = true;
//        this.szDecompFrameMax = szMaxFrmAlloc;
        
        this.prcrFrames.setFrameDecomposition(szMaxFrmAlloc);
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
     */
    public void disableFrameDecomposition() {
//        this.bolDecompAuto = false;
        this.prcrFrames.disableFrameDecomposition();
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
    public void disableDecompConcurrency() throws IllegalStateException {

        if (this.bolStreamOpen)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change concurency once stream opened.");
        
//        this.bolDecompConcurrent = false;
        this.prcrFrames.disableConcurrency();
    }
    
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
    public void setStagingCapcity(long szQueueCapacity) {
//        this.szStagingCapacity = szQueueCapacity;
        this.buffStaging.setQueueCapcity(szQueueCapacity);
    }
    
    /**
     * <p>
     * Enables client back pressure (implicit throttling) from finite capacity ingestion staging buffer.
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
     */
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
    public void disableBackPressure() {
        this.bolBackPressure = false;
    }
    
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
     * activated with <code>{@link #activate()}</code>.  Invoking this method afterwards throws an 
     * exception.
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
        if (this.bolStreamOpen)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change gRPC stream type once stream opened.");
        
        // Check the argument
        if (enmStreamType == DpGrpcStreamType.BACKWARD)
            throw new UnsupportedOperationException(JavaRuntime.getQualifiedCallerNameSimple() + " - " + enmStreamType + " not supported");
        
        this.chanIngest.setStreamType(enmStreamType);
    }
    
    /**
     * <p>
     * Explicitly enables the given number of multiple gRPC data stream for transmitting ingestion data.
     * </p>
     * <p>
     * <h2>Multiple gRPC Streams</h2>
     * Using multiple gRPC data streams can significantly improve transmission performance. 
     * Although a single gRPC channel is used between this client and the Ingestion Service,
     * the channel allows the use of multiple, concurrent data streams between client and
     * service.
     * </p>
     * <p>
     * When enabling this feature class instances create a thread pool of independent
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
     * 
     * @see #CNT_MULTISTREAM_MAX
     */
    public void setMultipleStreams(int cntStreamsMax) throws IllegalStateException, IllegalArgumentException {

        // Check current state
        if (this.bolStreamOpen)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change stream count once stream opened.");
        
        // Check the argument
        if (cntStreamsMax <= 0)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedCallerNameSimple() + " - argument must be greater that zero. ");

        this.chanIngest.setMultipleStreams(cntStreamsMax);
    }
    
    /**
     * <p>
     * Disable the use of multiple gRPC stream for transmitting ingestion data.
     * </p>
     * <p>
     * <h2>Multiple gRPC Streams</h2>
     * Using multiple gRPC data streams can significantly improve transmission performance. 
     * Although a single gRPC channel is used between this client and the Ingestion Service,
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
     * This configuration parameter can only be modified <em>before</em> the stream pool is 
     * activated with <code>{@link #activate()}</code> , otherwise an exception is throw.
     * </p>
     * 
     * @throws IllegalStateException    method called while processor is active
     */
    public void disableMultipleStreams() throws IllegalStateException {

        // Check current state
        if (this.bolStreamOpen)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change concurrency once stream opened.");
        
        this.chanIngest.disableMultipleStreams();
    }
    

    //
    // Operations
    //
    
    /**
     * <p>
     * Returns the Data Provider UID for the Data Provider registration that was used to open this data stream.
     * </p>
     * <p>
     * If the data stream to the Ingestion Service was opened then subsequently closed, the Provider UID last used
     * to open the stream is returned.
     * </p>
     * 
     * @return  the UID of the data provider which opened the data stream to the Ingestion Service
     * 
     * @throws IllegalStateException    the data stream was never opened
     */
    public ProviderUID getProviderUid() throws IllegalStateException {
        
        return this.recProviderUid;
    }
    
    /**
     * <p>
     * Returns the current size of the staging buffer containing outgoing data ingestion 
     * messages waiting for transmission. 
     * </p>
     * <p>
     * Returns the current size of the ingest data request message staging buffer used internally
     * for buffering the frame processor.  This value can be used to estimate transmission
     * performance by the client by timing the consumption of queued data ingestion messages.
     * </p>
     * <p>
     * Technically, the value returned is the number of 
     * <code>IngestDataRequest</code> messages that are currently queued up and waiting 
     * for on an available stream transmission thread.
     * </p> 
     * 
     * @return  number of <code>IngestDataRequest<code> messages in the staging buffer
     */
    public int getQueueSize() {
        return this.buffStaging.getQueueSize();
    }
    
    /**
     * <p>
     * Returns an immutable list of "client request IDs" within all the data ingestion messages
     * sent to the Ingestion Service during the current open stream session.
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
     * Client request IDs are assigned by the internal processor before transmission to the
     * Ingestion Service. Since large <code>IngestionFrame</code> instances may be decomposed into
     * smaller frames (to meet gRPC message size limits), it is not practical to set request
     * IDs within a frame itself.
     * </li>
     * <li>
     * The returned collection is a list of all client request IDs assigned to outgoing data
     * ingestion messages and can be used by clients to locate ingested data within the archive,
     * or queery the Ingestion Service about request status post ingestion.
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
     * @throws IllegalStateException    the stream was not opened and processor was never activated
     */
    public List<ClientRequestId> getClientRequestIds() throws IllegalStateException {
        
        // Get the client request IDs from the internal processor
        List<ClientRequestId>   lstIds = this.chanIngest.getRequestIds();
        
        return lstIds;
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
     * The internal processor monitors incoming responses from the Ingestion Service for 
     * exceptions.  The returned list is the collection of all responses indicating an exception 
     * with its corresponding request, which may be identified by the client request ID.
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
     * If a client request ID does not appear in the returned list there is <em>no guarantee</em> 
     * that an error <em>did not occur later</em> in the Ingestion Service processing and archiving.
     * One may query the Ingestion Service, via client request ID, for all client ingestion
     * requests that failed archiving. 
     * </li>
     * </ul>
     * </p>
     *   
     * @return  the collection of Ingestion Service responses reporting an exception
     * 
     * @throws IllegalStateException    stream was never opened and processor was never activated
     */
    public List<IngestionResponse> getIngestionExceptions() throws IllegalStateException {
        
        // Get the list of exceptional responses from the internal stream processor
        List<IngestionResponse> lstRsps = this.chanIngest.getIngestionExceptions();
        
        return lstRsps;
    }

    
    //
    // IIngestionStream Interface
    //

    /**
     *
     * @see @see com.ospreydcs.dp.api.ingest.IIngestionStream#openStream(com.ospreydcs.dp.api.model.ProviderRegistrar)
     */
    @Override
    synchronized
    public ProviderUID openStream(ProviderRegistrar recRegistration) throws DpIngestionException {

        // Check if stream is already open
        if (this.bolStreamOpen)
            return this.recProviderUid;

        // Register the data provider with the Ingestion Service (throws exception upon failure)
        this.recProviderUid = ProviderRegistrationService.registerProvider(super.grpcConn, recRegistration);

        // Start the ingestion stream processor
        this.prcrFrames.setProviderUid(this.recProviderUid);
        this.prcrFrames.activate();
        
        // Activate the staging buffer
        this.buffStaging.activate();
        
        // Activate the Ingestion Service channel
        this.chanIngest.activate();

        // Create the processor to staging buffer task and start
        Runnable    tskTranfer = this.createTransferTask();
        this.thdXferTask = new Thread(tskTranfer);
        this.thdXferTask.start();
        
        // Set the open stream flag - Stream is ready
        this.bolStreamOpen = true;
        
        return this.recProviderUid;
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.ingest.IIngestionStream#ingest(com.ospreydcs.dp.api.ingest.IngestionFrame)
     */
    @Override
    public void ingest(IngestionFrame frame) throws IllegalStateException, InterruptedException, DpIngestionException {

        // Check state
        if (!this.bolStreamOpen)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Stream is not open.");
        
        // Check if no back pressure
        if (!this.bolBackPressure)
            this.prcrFrames.submit(frame);      // throws exception if inactive
        
        // Enforce back pressure from staging buffer
        this.buffStaging.awaitQueueReady();     // throws interrupted exception
        this.prcrFrames.submit(frame);          // throws  exception if inactive
    }

    /**
     *
     * @throws InterruptedException 
     * @see @see com.ospreydcs.dp.api.ingest.IIngestionStream#ingest(java.util.List)
     */
    @Override
    public void ingest(List<IngestionFrame> lstFrames) throws IllegalStateException, InterruptedException, DpIngestionException {

        // Check state
        if (!this.bolStreamOpen)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Stream is not open.");
        
        // If no back pressure submit immediately
        if (!this.bolBackPressure)
            this.prcrFrames.submit(lstFrames);  // throws exception if inactive
        
        // Enforce back pressure from staging buffer
        this.buffStaging.awaitQueueReady();     // throws interrupted exception
        this.prcrFrames.submit(lstFrames);      // throws exception if inactive
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.ingest.IIngestionStream#awaitQueueReady()
     */
    @Override
    public void awaitQueueReady() throws IllegalStateException, InterruptedException {
        this.buffStaging.awaitQueueReady();
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.ingest.IIngestionStream#awaitQueueEmpty()
     */
    @Override
    public void awaitQueueEmpty() throws IllegalStateException, InterruptedException {
        this.buffStaging.awaitQueueEmpty();
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.ingest.IIngestionStream#closeStream()
     */
    @Override
    synchronized
    public List<IngestionResponse> closeStream() throws IllegalStateException, InterruptedException, CompletionException {
        
        // Check state
        if (!this.bolStreamOpen) 
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Stream is not open.");
        
        // Shutdown the ingestion frame processor - will supply until output buffer empty
        this.prcrFrames.shutdown();     // prevents any further ingestion, blocks until complete
        
        // Wait for all data to be transferred to staging buffer
        this.thdXferTask.join();        // throws interrupted exception
        
        // Check for error in transfers
        if (this.recXferStatus.isFailure()) {
            String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() + 
                    " - error in message transfer and staging: " +
                    this.recXferStatus.message(); 
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            this.buffStaging.shutdownNow();
            this.chanIngest.shutdownNow();
            
            throw new CompletionException(strMsg, this.recXferStatus.cause());
        }
        
        // Shutdown staging buffer - prevents any new transfers of messages
        this.buffStaging.shutdown();    // allows channel to consume messages until exhausted
        
        // Shutdown the ingestion channel
        this.chanIngest.shutdown();     // blocks until complete
        
        // All internal resources are shutdown - close stream is complete
        this.bolStreamOpen = false;
        
        // Retrieve ingestion responses and return
        List<IngestionResponse> lstRsps = this.chanIngest.getIngestionResponses();
        
        return lstRsps;
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.ingest.IIngestionStream#closeStreamNow()
     */
    @Override
    public boolean closeStreamNow() {

        // Check state
        if (!this.bolStreamOpen)
            return false;
        
        // Shut down all internal processes immediately
        this.prcrFrames.shutdownNow();
        this.thdXferTask.interrupt();
        this.buffStaging.shutdownNow();
        this.chanIngest.shutdownNow();
        
        this.bolStreamOpen = false;
        
        return true;
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.ingest.IIngestionStream#isStreamOpen()
     */
    @Override
    public boolean isStreamOpen() {
        return this.bolStreamOpen;
    }

//    /**
//     *
//     * @see @see com.ospreydcs.dp.api.ingest.IIngestionStream#getClientRequestIds()
//     */
//    @Override
//    public List<ClientRequestId> getClientRequestIds() throws IllegalStateException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    /**
//     *
//     * @see @see com.ospreydcs.dp.api.ingest.IIngestionStream#getIngestionExceptions()
//     */
//    @Override
//    public List<IngestionResponse> getIngestionExceptions() throws IllegalStateException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    /**
//     *
//     * @see @see com.ospreydcs.dp.api.ingest.IIngestionStream#getProviderUid()
//     */
//    @Override
//    public ProviderUID getProviderUid() throws IllegalStateException {
//        // TODO Auto-generated method stub
//        return null;
//    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.ingest.IIngestionStream#shutdown()
     */
    @Override
    public boolean shutdown() throws InterruptedException {

        // Close stream if it is open
        if (this.bolStreamOpen) {
            this.closeStream();
        }
        
        return super.shutdown();
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.ingest.IIngestionStream#shutdownNow()
     */
    @Override
    public boolean shutdownNow() {
        
        // Close stream if it is open
        if (this.bolStreamOpen) {
            this.closeStreamNow();
        }
        return super.shutdownNow();
    }


    //
    // IConnection Interface
    //

    /**
     *
     * @see @see com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#awaitTermination(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public boolean awaitTermination(long lngTimeout, TimeUnit tuTimeout) throws InterruptedException {
        return super.awaitTermination(lngTimeout, tuTimeout);
    }
    
    /**
     *
     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination()
     */
    @Override
    public boolean awaitTermination() throws InterruptedException {
        return super.awaitTermination(LNG_TIMEOUT_WAIT, TU_TIMEOUT_WAIT);
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#isShutdown()
     */
    @Override
    public boolean isShutdown() {
        return super.isShutdown();
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#isTerminated()
     */
    @Override
    public boolean isTerminated() {
        return super.isTerminated();
    }
    

    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates a new frame processor to staging buffer ingest request message transfer task.
     * </p>
     * <p>
     * The returned task instance continuously transfers <code>IngestDataRequest</code> messages from the
     * ingestion frame processor to the staging buffer while the frame processor is active.  Once
     * <code>{@link #prcrFrames}</code> method <code>{@link IngestionFrameProcessor#isSupplying()}</code>
     * return <code>false</code> the task terminates normally and the attribute <code>{@link #recXferStatus}</code>
     * is set to <code>{@link ResultStatus#SUCCESS}</code>
     * </p>
     * <p>
     * If an error occurs during the transfer process the task terminates abnormally setting the 
     * <code>{@link #recXferStatus}</code> attribute with the detailed cause and the exception creating
     * the error.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The task uses the <code>{@link IngestionFrameProcessor#poll(long, TimeUnit)}</code> operation to 
     * obtain <code>IngestDataRequest</code> messages.  This is done to avoid thread lock when the processor
     * stops supplying (i.e., a <code>take()</code> operation would wait indefinitely).
     * </p>
     *  
     * @return  new <code>Runnable</code> implementation of the transfer task
     * 
     * @see #LNG_TIMEOUT_POLL
     * @see #TU_TIMEOUT_POLL
     */
    @SuppressWarnings("unused")
    private Runnable    createTransferTask() {
        
        Runnable    task = () -> {
            
            // Continuously transfer ingest data messages while processor is actively supplying
            while (this.prcrFrames.isSupplying()) {
                
                // Poll the processor to avoid thread lock (take() wait indefinitely if processor stops supplying)
                IngestDataRequest msgRqst;
                try {
                    msgRqst = this.prcrFrames.poll(LNG_TIMEOUT_POLL, TU_TIMEOUT_POLL);
                    
                    // If timeout occurred poll again
                    if (msgRqst == null)
                        continue;
                    
                } catch (IllegalStateException e) {
                    String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() +
                            " - IngestionFrameProcessor illegal state during polling: " +
                            e.getMessage();
                    this.signalTransferError(strMsg, e);
                    return;
                    
                } catch (InterruptedException e) {
                    String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() +
                            " - IngestionFrameProcessor polling operation externally interrupted while waiting.";

                    this.signalTransferError(strMsg, e);
                    return;
                }

                // Transfer the message to the staging buffer for transport
                try {
                    
                    this.buffStaging.offer(msgRqst);
                    this.cntMsgsXmit++;
                    this.szDataXmit += msgRqst.getSerializedSize();
                    
                } catch (IllegalStateException e) {
                    String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() +
                            " - Staging buffer illegal state while transferring message: " +
                            e.getMessage();
                    
                    this.signalTransferError(strMsg, e);
                    return;
                    
                } catch (InterruptedException e) {
                    String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() +
                            " - Staging buffer transfer operation externally interrupted while waiting.";

                    this.signalTransferError(strMsg, e);
                    return;
                }
            }
            
            this.recXferStatus = ResultStatus.SUCCESS;
        };
        
        return task;
    }
    
    /**
     * <p>
     * Signals a message transfer error between the ingestion frame processor and the staging buffer.
     * </p>
     * <p>
     * Creates an error log entry if logging is enabled and sets the status record to the failure.
     * </p>
     * 
     * @param strMsg    detail message describing the error
     * @param e         exception causing the error
     */
    private void signalTransferError(String strMsg, Throwable e) {
        
        if (BOL_LOGGING)
            LOGGER.error(strMsg);
        
        this.recXferStatus = ResultStatus.newFailure(strMsg, e);
    }
    
}
