/*
 * Project: dp-api-common
 * File:	IngestionChannel.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IngestionChannel
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
 * @since Jul 22, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.grpc;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.common.IngestRequestUID;
import com.ospreydcs.dp.api.common.IngestionResult;
import com.ospreydcs.dp.api.common.ProviderUID;
import com.ospreydcs.dp.api.config.JalConfig;
import com.ospreydcs.dp.api.config.ingest.JalIngestionConfig;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.model.IMessageSupplier;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataResponse;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataStreamResponse;

/**
 * <h1>Representation of an ingestion channel to the Ingestion Service</h1>
 * <p>
 * Class instances function as "data channels" to the Ingestion Service using gRPC data streams
 * for data transmission.  This condition is not to be interpreted as a gRPC <code>Channel</code>
 * instance, although they are related (a single gRPC <code>Channel</code> can support multiple 
 * data streams).
 * Class instances implement a cooperating collection of <code>IngestionStream</code> objects used 
 * for concurrent data transmission to the Ingestion Service via a gRPC channel.
 * </p>
 * <p>
 * Class instances transmit processed client ingestion data using a collection of 
 * <code>{@link IngestionStream}</code> instances - one for each enabled gRPC data stream.  Processed ingestion
 * data is represented as a stream of <code>IngestDataRequest</code> Protobuf messages.
 * The stream classes are executed on separate threads that all compete for the 
 * <code>IngestDataRequest</code> messages produced by the <code>IMessageSupplier&lt;IngestDataRequest&gt;</code>.
 * (If the multiple data streams feature is disabled only one stream will be enabled.)  
 * </p>
 * <h2>Operation</h2>
 * After creation instances of <code>IngestionChannel</code> must be activated using
 * either the <code>{@link #activate(ProviderUID)}</code> or <code>{@link #activate(int)}</code> 
 * method, which requires the data provider UID (either as a record or directly as an integer value)
 * to be given to all outgoing <code>IngestDataRequest</code> messages.  
 * When no longer required the processor should be shutdown using either 
 * <code>{@link #shutdown()}</code>, which allows all transmission to complete, 
 * or <code>{@link #shutdownNow()}</code>, which performs a hard shutdown immediately terminating all 
 * transmission tasks.  
 * </p>
 * <p>
 * It is possible to cycle through repeated activations and shutdowns with a single 
 * <code>IngestionChannel</code> instance.  For example, an instance can be activated
 * for a specific data provider (with its UID) then shutdown and re-activated for a different
 * data provider.
 * </p>
 * <p>
 * <h3>Activation</h3>
 * After activating the instance it is ready for ingestion request transmission.  
 * Ingestion request messages are retrieved from the supplied <code>IMessageSupplier</code>
 * source and transmitted through the gRPC data streams.
 * </p>
 * <p>
 * Activation starts all transmission tasks which are then 
 * continuously enabled throughout the lifetime of this instance, or explicitly shut down.  
 * Streaming tasks execute independently in
 * thread pools where they block until ingestion requests become available through the source.
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
 * <h2>Exception Checking</h2>
 * This class no longer monitors the ingestion stream for rejected ingestion requests.  It simply
 * collects all Ingestion Service responses (either unidirectional or bidirectional) and makes
 * them available to clients of this class.  It is the responsibility of the client to determine
 * if exceptions occurred. Such exceptions and be parsed and reported in an 
 * <code>{@link IngestionResult}</code> record.
 * </p>
 * <p>
 * Note that the underlying <code>IngestionStream</code> objects managed by this class do respond
 * to errors in the ingestion stream itself.  
 * <p>
 * <h2>NOTES:</h2>
 * <s>At the time of this documentation only the bidirectional streaming operation is supported
 * by the Ingestion Service (i.e., <code>{@link DpGrpcStreamType#BIDIRECTIONAL}</code>).  
 * Attempting to use a unidirectional stream could result in a RPC failure at the 
 * Ingestion Service.</s>
 * <br/>
 * <b>UPDATE:</b>
 * Currently both unidirectional and bidirectional streaming are now supported by the Ingestion
 * Service; this class will support either streaming alternative. 
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Jul 22, 2024
 *
 */
public class IngestionChannel {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Constructs a new <code>IngestionChannel</code> instance ready for activation.
     * </p>
     * <p>
     * Instance clients must supply the source of the processed ingestion data, specifically, the source
     * of <code>IngestDataRequest</code> messages.  The connection to the Ingestion Service must also be 
     * given at creation.  This connection is used to establish all gRPC data streams with the Ingestion
     * Service.  
     * </p>  
     * <p>
     * Use method <code>{@link #activate(int)}</code> to begin ingesting client data, providing
     * the method with the UID of the data provider UID supplying ingestion data.
     * When data ingestion is over, or to switch to a different data provider, shut down the
     * processor with either <code>{@link #shutdown()}</code> or 
     * <code>{@link #shutdownNow()}</code>. 
     * </p>
     * 
     * @param srcRqstMsgs   the supplier of incoming, processed <code>IngestDataRequest</code> messages 
     * @param connIngest    Java API encapsulated gRPC connection to the Ingestion Service
     * 
     * @return  a new <code>IngestionChannel</code> instance ready for activation (and additional configuration)
     */
    public static IngestionChannel from(IMessageSupplier<IngestDataRequest> srcRqstMsgs, DpIngestionConnection connIngest) {
        return new IngestionChannel(srcRqstMsgs, connIngest);
    }
    
    //
    // Application Resources
    //
    
    /** The Ingestion Service client API default configuration */
    private static final JalIngestionConfig  CFG_DEFAULT = JalConfig.getInstance().ingest; 
    
    
    //
    // Class Constants
    //
    
    /** General operation timeout limit */
    private static final long       LNG_TIMEOUT_GENERAL = CFG_DEFAULT.timeout.limit;
    
    /** General operation timeout units */
    private static final TimeUnit   TU_TIMEOUT_GENERAL = CFG_DEFAULT.timeout.unit;
    
    
    /** Event logging enabled flag */
    @SuppressWarnings("unused")
    private static final boolean    BOL_LOGGING = CFG_DEFAULT.logging.enabled;
    
    /** Event logging level */
    private static final String     STR_LOGGING_LEVEL = CFG_DEFAULT.logging.level;
    
    
    /** The default gRPC stream type */
    private static final DpGrpcStreamType   ENM_STREAM_TYPE = CFG_DEFAULT.stream.type;
    
    /** Use multiple gRPC data stream to transmit ingestion frames */
    private static final Boolean            BOL_MULTISTREAM_ACTIVE = CFG_DEFAULT.stream.concurrency.enabled;
    
    /** The maximum number of gRPC data stream used to transmit ingestion data */
    private static final Integer            CNT_MULTISTREAM_MAX = CFG_DEFAULT.stream.concurrency.maxStreams;
    
//  /** When the number of frames available exceeds this value multiple gRPC data streams are used */ 
//  private static final Long       LNG_MULTISTREAM_PIVOT = CFG_DEFAULT.stream.concurrency.pivotSize;
  
    
    //
    // Class Resources
    //
    
    /** Class event logger instance */
    @SuppressWarnings("unused")
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    /**
     * <p>
     * Class Initialization - Initializes the event logger, sets logging level.
     * </p>
     */
    static {
        Configurator.setLevel(LOGGER, Level.toLevel(STR_LOGGING_LEVEL, LOGGER.getLevel()));
    }
    
    
    //
    // Defining Attributes
    //
    
    /** The source of the incoming <code>IngestDataRequest</code> messages */
    private final IMessageSupplier<IngestDataRequest>   srcRqstMsgs;
    
    /** The gRPC connection to the Ingestion Service - channel for the outgoing messages */
    private final DpIngestionConnection                 connIngest;

    
    //
    // Configuration Parameters
    //
    
    /** gRPC stream type */
    private DpGrpcStreamType    enmStreamType = ENM_STREAM_TYPE;
    
    /** Multiple gRPC data streams flag */
    private boolean             bolMultistream = BOL_MULTISTREAM_ACTIVE;
    
    /** The maximum number of gRPC data streams to use */
    private int                 cntStreamsMax = CNT_MULTISTREAM_MAX;
    
    
    //
    // Instance Resources
    //
    
    /** The pool of (multiple) gRPC streaming thread(s) for recovering requests */
    private ExecutorService                     xtorStreamTasks = null;
    
    /** Collection of executing stream processing tasks  */
    private final Collection<IngestionStream>   setStreamTasks = new LinkedList<>();
    
    /** Collection of future results from streaming tasks - used for shutdowns */
    private final Collection<Future<Boolean>>   setStreamFutures = new LinkedList<>();


    /** Object used as synchronization lock for incoming unidirectional responses (there may be concurrent streaming tasks) */
    private final Object        objUniRspLock = new Object();
    
    /** Object used as synchronization lock for incoming bidirectional responses (there may be concurrent streaming tasks) */
    private final Object        objBidiRspLock = new Object();

    
    /** The collection of ingestion responses - one from each unidirectional streaming tasks */
    private final List<IngestDataStreamResponse>    lstUniRsps = new LinkedList<>();
    
    /** Collection of all incoming ingestion responses - multiple from each bidirectional streaming tasks */
    private final List<IngestDataResponse>          lstBidiRsps = new LinkedList<>();
    
    /** Collection of all ingestion responses with exceptions */    // No longer monitored
//    private final Collection<IngestionResponse> setRspRecsBad = new LinkedList<>();
    
    
    // 
    // State Variables
    //
    
    /** The number of Ingestion Service responses so far */
    private int     cntResponses = 0;
    
    /** Are streaming tasks enabled */
    private boolean bolActive = false;
    
    
    
    //
    // Constructors
    // 
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionChannel</code> using the given message source and connection to the
     * Ingestion Service.
     * </p>
     *
     * @param srcRqstMsgs   the supplier of incoming, processed <code>IngestDataRequest</code> messages 
     * @param connIngest    Java API encapsulated gRPC connection to the Ingestion Service
     */
    public IngestionChannel(IMessageSupplier<IngestDataRequest> srcRqstMsgs, DpIngestionConnection connIngest) {
        this.srcRqstMsgs = srcRqstMsgs;
        this.connIngest = connIngest;
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
     * activated with <code>{@link #activate()}</code>.  Invoking this method afterwards throws an 
     * exception.
     * </li>  
     * </p>
     * 
     * @param enmStreamType gRPC stream type for data transmission
     * 
     * @throws IllegalStateException            method called while processor is enabled
     * @throws UnsupportedOperationException    an unsupported stream type was provided
     */
    public void setStreamType(DpGrpcStreamType enmStreamType) throws IllegalStateException, UnsupportedOperationException {

        // Check current state
        if (this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Cannot change gRPC stream type once activated.");
        
        // Check the argument
        if (enmStreamType == DpGrpcStreamType.BACKWARD)
            throw new UnsupportedOperationException(JavaRuntime.getQualifiedMethodNameSimple() + " - " + enmStreamType + " not supported for data ingestion.");
        
        this.enmStreamType = enmStreamType;
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
     * @throws IllegalStateException    method called while processor is enabled
     * @throws IllegalArgumentException the argument was zero or negative
     * 
     * @see #CNT_MULTISTREAM_MAX
     */
    public void setMultipleStreams(int cntStreamsMax) throws IllegalStateException, IllegalArgumentException {

        // Check current state
        if (this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Cannot change stream count once activated.");
        
        // Check the argument
        if (cntStreamsMax <= 0)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - argument must be greater that zero. ");
        
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
     * @throws IllegalStateException    method called while processor is enabled
     */
    public void disableMultipleStreams() throws IllegalStateException {

        // Check current state
        if (this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Cannot change concurrency once activated.");
        
        this.bolMultistream = false;
    }
    
    
    // 
    // Configuration Query
    //
    
    /**
     * <p>
     * Determines whether or not multiple gRPC data streams are used to transport data to the Ingestion Service.
     * </p>
     *  
     * @return  <code>true</code> if multi-streaming is enabled, <code>false</code> otherwise (only a single stream is used)
     */
    public boolean  hasMultipleStreams() {
        return this.bolMultistream;
    }
    
    /**
     * <p>
     * Returns the maximum number of concurrent gRPC data streams used to transmit data to the Ingestion Service.
     * </p>
     * <p>
     * If multiple data streaming is enabled this is the number of concurrent data stream used by the pool to 
     * transport data to the Ingestion Service over gRPC.  If multi-streaming is disabled this value should be
     * ignored (i.e., only a single gRPC data stream is used).
     * </p>
     *  
     * @return  the maximum number of independent, concurrent gRPC data streams used for data transmission 
     * 
     * @see #hasMultiStream()
     */
    public int      getMaxDatastreams() {
        return this.cntStreamsMax;
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
    

    //
    // State and Attribute Query
    //
    
    /**
     * <p>
     * Returns the number of <code>IngestDataRequest</code> messages transmitted to the 
     * Ingestion Service so far.
     * </p>
     * <p>
     * The returned value is the number of Protocol Buffers messages carrying ingestion
     * data that have been transmitted to the Ingestion Service at the time of invocation.
     * If called after invoking <code>{@link #shutdown()}</code> then the returned value
     * is the total number of messages transmitted while enabled.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The value returned by this method is not necessary equal to the number of 
     * <code>IngestionFrame</code> instances offered to upstream processing.  
     * If ingestion frame decomposition
     * is enabled large ingestion frame exceeding the size limit which be decomposed into
     * smaller ingestion frames before being converted into <code>IngestDataRequest</code>
     * messages.
     * </li>
     * <br/>
     * <li>
     * This value is available after a shutdown operation has been called.  At that time
     * the returned value is the total number of <code>IngestDataRequest</code> messages
     * transmitted to the Ingestion Service during that activation cycle.
     * </li> 
     * <br/>
     * <li>
     * If the <code>activate()</code> method is called after a shutdown the returned value 
     * resets.
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
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - instance was never activated.");

        // Return the sum of all stream responses
        return this.setStreamTasks
                .stream()
                .mapToInt(IngestionStream::getRequestCount)
                .sum();
    }
    
    /**
     * <p>
     * Returns an immutable list of client request IDs within all the <code>IngestDataRequest</code>
     * messages transmitted during the current session.
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
     * The ordering of this list does not necessarily reflect the order that ingestion request messages
     * were offered to the stream pool.  Processed ingestion request messages do not necessarily
     * get transmitted in order.
     * </li>
     * <li>
     * This is potentially a non-trivial operation and is best called post shutdown.
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
    public List<IngestRequestUID>    getRequestIds() throws IllegalStateException {
        
        // Check if activated
        if (this.setStreamTasks == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - processor was never activated.");

        // Collect all the client IDs recorded within each ingestion stream
        List<IngestRequestUID>   lstIds = this.setStreamTasks
                .stream()
                .<IngestRequestUID>flatMap(stream -> stream.getRequestIds().stream())
                .toList();
        
        return lstIds;
    }
    
    /**
     * <h1>
     * Returns the number of response messages received from the Ingestion Service so far.
     * </h1>
     * <p>
     * For a bidirectional gRPC data stream the Ingestion Service will send a <code>IngestDataResponse</code> 
     * response for every ingestion request it receives.  In that case, this method returns the number of 
     * responses received so far, which (ideally) should be equal to the value returned by
     * <code>{@link #getRequestCount()}</code>.  For multiple data streams the returned value will
     * be the combined number of responses from each stream. 
     * </p>
     * <p>  
     * For unidirectional gRPC streaming the Ingestion Service will return at most one response for each data
     * stream.  This response is send only after all data is transmitted and a "half-closed" event is signaled
     * to the Ingestion Service.  Thus, under normal operations the returned value should be 0 while all gRPC
     * data streams are still enabled, and the number of data streams used to transmit data after the shutdown 
     * operation is called.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * At the time of this documentation the Ingestion Service now supports both unidirectional and bidirectional
     * gRPC data streams.  
     * </li>
     * <br/>
     * <li>
     * This value is available after a shutdown operation has been called.  At that time
     * the returned value is the total number of <code>IngestDataRequest</code> messages
     * transmitted to the Ingestion Service during that activation cycle for a bidirectional stream, and
     * the total number of gRPC data streams for a unidirectional stream.
     * </li> 
     * </ul>
     * </p>
     * 
     * @return  the number of response messages received from the Ingestion Service so far
     * 
     * @throws IllegalStateException    processor was never activated
     */
    public int getResponseCount() throws IllegalStateException {
        
        // Check if activated
        if (this.setStreamTasks == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - processor was never activated.");

        return this.cntResponses;
//        return this.setStreamTasks
//                .stream()
//                .mapToInt(IngestionStream::getResponseCount)
//                .sum();
    }
    
    /**
     * <h1>
     * Return a list of all the Ingestion Service response messages for an unidirectional ingestion operation.
     * </h1>
     * <p>
     * This method has context only when using unidirectional gRPC data streams; it can be invoked at any
     * time after the channel is activated (i.e., after <code>{@link #activate()}</code> is invoked).  The
     * current number of response messages will be returns (i.e., the number at the time of invocation).
     * </p>
     * <p>
     * For unidirectional gRPC data streams the Ingestion Service sends at most one response to the streaming
     * operation, at the time of the stream closure.  Thus, invoking this method before any 
     * <code>IngestionStream</code> task within the channel has terminated will return an empty list.
     * Invoking this method after a successful shutdown operation will return the list of single responses
     * for each ingestion stream.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The size of this list should, in theory, be the value returned by 
     * <code>{@link #getResponseCount()}</code>.  However, due to the asynchronous nature of the streaming
     * operation and the possibility of multiple, concurrent data streams, the values may differ at the
     * same time instant.
     * </li>
     * <li>
     * This method will return an empty list if a bidirectional streaming operation was specified. 
     * </li>
     * <li>
     * The ordering of this list does not necessarily reflect the order that ingestion frames
     * were offered to the processor.  Processed ingestion request messages do not necessarily
     * get transmitted in order.
     * </li>
     * <li>
     * This method returns a collection of <code>{@link IngestionResponse}</code> records native to the API
     * library, rather than <code>{@link IngestDataResponse}</code> Protocol Buffers messages.  This action
     * is simply for convenience. 
     * </ul>
     * </p>
     * 
     * @return  a list of all Ingestion Service responses to all transmitted ingestion requests
     * 
     * @throws IllegalStateException    channel was never activated
     */
    public List<IngestDataStreamResponse>    getIngestionUniResponses() throws IllegalStateException {
        
        // Check if activated
        if (this.setStreamTasks == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - channel was never activated.");

        return this.lstUniRsps;
    }
    
    /**
     * <h1>
     * Return a list of all the Ingestion Service response messages for a bidirectional ingestion operation.
     * </h1>
     * <p>
     * This method has context only when using bidirectional gRPC data streams; it can be invoked at any
     * time after the channel is activated (i.e., after <code>{@link #activate()}</code> is invoked).  The
     * current number of response messages will be returns (i.e., the number at the time of invocation).
     * </p>
     * <p>
     * For bidirectional streaming the Ingestion Service sends a response for <em>every</em> ingestion request.
     * The returned list will contain all responses from all currently enabled <code>IngestionStream</code> tasks.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The size of this list should, in theory, be the value returned by 
     * <code>{@link #getResponseCount()}</code>.  However, due to the asynchronous nature of the streaming
     * operation and the possibility of multiple, concurrent data streams, the values may differ at the
     * same time instant.
     * </li>
     * <li>
     * This method will return an empty list if an unidirectional streaming operation was specified. 
     * </li>
     * <li>
     * The ordering of this list does not necessarily reflect the order that ingestion frames
     * were offered to the processor.  Processed ingestion request messages do not necessarily
     * get transmitted in order.
     * </li>
     * <li>
     * This method returns a collection of <code>{@link IngestionResponse}</code> records native to the API
     * library, rather than <code>{@link IngestDataResponse}</code> Protocol Buffers messages.  This action
     * is simply for convenience. 
     * </ul>
     * </p>
     * 
     * @return  a list of all Ingestion Service responses to all transmitted ingestion requests
     * 
     * @throws IllegalStateException    channel was never activated
     */
    public List<IngestDataResponse> getIngestionBidiResponses() throws IllegalStateException {
        
        // Check if activated
        if (this.setStreamTasks == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - channel was never activated.");

        return this.lstBidiRsps;
    }
    
    /**
     * <h1>
     * Returns the result of the ingestion operation so far.
     * </h1>
     * <p>
     * Returns the result of the ingestion operation as determined by the responses obtained from the
     * Ingestion Service.  All Ingestion Services responses are parsed and collected to created the
     * returned result, which summarizes the ingestion operation, whether unidirectional, bidirectional,
     * single stream, or multiple stream.
     * </p>   
     * <p>
     * If the ingestion operation is still in progress the returned record will either contain
     * a partial result if the operations used a bidirectional streaming operation, or the operation used
     * a unidirectional streaming operation <b>and</b> at least one of <code>IngestionStream</code> tasks has
     * finished (i.e., the stream has closed).  Otherwise the returned result will be the 
     * <code>{@link IngestionResult#NULL}</code> record (rather than throwing an exception).
     * </p>
     * <p>
     * <h2>Usage</h2>
     * The intended use of this method is invocation after the ingestion channel has been activated, all data
     * has been transmitted, and the channel shut down.  In that situation the returned value will be the true result
     * of the full ingestion operation.  The success or failure of the operation can be determined via the
     * record <code>{@link IngestionResult#hasException()}</code> method.  The record then contains a list of
     * all exceptions and rejected request UIDs provided by the Ingestion Service. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * To obtain the Ingestion Service responses in raw Protobuf messsage format use 
     * <code>{@link #getIngestionUniResponses()}</code> for the unidirectional streaming case and
     * <code>{@link #getIngestionBidiResponses()}</code> for the bidirectional streaming case.  See method
     * <code>{@link #getStreamType()}</code> to determine the current gRPC stream type setting.
     * </p>
     * 
     * @return  the current result of the ingestion operation or <code>NULL</code> record if none is available
     * 
     * @throws IllegalStateException            channel was never activated
     * @throws UnsupportedOperationException    an unsupported gRPC stream type was specified
     * @throws MissingResourceException         a response message could not be converted to API record due to missing resource
     */
    public IngestionResult  getIngestionResult() throws IllegalStateException, UnsupportedOperationException, MissingResourceException {
     
        // Check if activated
        if (this.setStreamTasks == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - channel was never activated.");
        
//        // TODO - Consider removing
//        if (BOL_LOGGING)
//            LOGGER.debug("{} - Stream type = {}, lstUniRsps size = {}, lstBidiRsps size = {}", JavaRuntime.getCallerClassSimple(), this.enmStreamType, this.lstUniRsps.size(), this.lstBidiRsps.size());
        
        // Create result depending upon stream type
        if (this.enmStreamType == DpGrpcStreamType.FORWARD) {
            if (this.lstUniRsps.isEmpty())
                return IngestionResult.NULL;
            
            List<IngestRequestUID> lstXmitIds = this.getRequestIds();
            IngestionResult recResult = ProtoMsg.toIngestionResultUni(lstXmitIds, this.lstUniRsps); // throws MissingResourceException
            
            return recResult;
            
        } else if (this.enmStreamType == DpGrpcStreamType.BIDIRECTIONAL) {
            if (this.lstBidiRsps.isEmpty())
                return IngestionResult.NULL;
            
            List<IngestRequestUID> lstXmitIds = this.getRequestIds();
            IngestionResult recResult = ProtoMsg.toIngestionResult(lstXmitIds, this.lstBidiRsps);    // throws MissingResourceException

            return recResult;
            
        } else
            throw new UnsupportedOperationException(JavaRuntime.getQualifiedMethodNameSimple() + " - encountered an unsupported stream type " + this.enmStreamType);
    }
    
//    /**
//     * <h1>
//     * Return a list of all the responses to the ingest data request messages.
//     * </h1>
//     * <p>
//     * This method is only practical when using bidirectional gRPC data streams where the
//     * Ingestion Service sents a response corresponding to every ingestion request.
//     * For unidirectional gRPC data streams the Ingestion Service sends at most one
//     * reply.  
//     * </p>
//     * <p>
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li>
//     * The size of this list should be the value returned by <code>{@link #getResponseCount()}</code>.
//     * </li>
//     * <li>
//     * The ordering of this list does not necessarily reflect the order that ingestion frames
//     * were offered to the processor.  Processed ingestion request messages do not necessarily
//     * get transmitted in order.
//     * </li>
//     * <li>
//     * This method returns a collection of <code>{@link IngestionResponse}</code> records native to the API
//     * library, rather than <code>{@link IngestDataResponse}</code> Protocol Buffers messages.  This action
//     * is simply for convenience. 
//     * </ul>
//     * </p>
//     * 
//     * @return  a list of all Ingestion Service responses to all transmitted ingestion requests
//     * 
//     * @throws IllegalStateException    channel was never activated
//     * @throws MissingResourceException a IngestDataResponse message could not be converted to API record
//     */
////    @AAdvancedApi(status=STATUS.TESTED_ALPHA, note="This implementation is unstable! - NullPointerException: Cannot read field 'next' because 'this.next' is null.")
//    public List<IngestionResponse>   getIngestionResponses() throws IllegalStateException, MissingResourceException {
//
//        // Check if activated
//        if (this.setStreamTasks == null)
//            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - channel was never activated.");
//
////        return this.lstBidiRsps;
//        // Iterate through the collection of IngestDataResponse messages converting them to API record instance
//        List<IngestionResponse> lstRsps = new ArrayList<>(this.setRspRecs);
//        
//        return lstRsps;
//    }
//    
//    /**
//     * <p>
//     * Returns a list of all ingestion responses reporting an exception with the corresponding
//     * ingestion request.
//     * </p>
//     * <p>
//     * This method is only practical when using bidirectional gRPC data streams where the
//     * Ingestion Service sents a response corresponding to every ingestion request.
//     * For unidirectional gRPC data streams the Ingestion Service sends at most one
//     * reply.
//     * </p>
//     * <p>
//     * The stream pool monitors incoming responses from the Ingestion Service for exceptions.
//     * The returned list is the collection of all responses indicating an exception with
//     * its corresponding request, which may be identified by the client request ID.
//     * </p>
//     * <p>
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li>
//     * Any ingestion request message identified in the list (i.e., by client request ID) had
//     * some type of exception encountered immediately by the Ingestion Service.  The exception
//     * details are included in the response.
//     * </li>
//     * <li>
//     * If a client request ID does not appear in the returned list there is no guarantee that
//     * an error did not occur later in the Ingestion Service processing and archiving.
//     * One may query the Ingestion Service, via client request ID, for all client ingestion
//     * requests that failed processing post transmission. 
//     * </li>
//     * </ul>
//     * </p>
//     *   
//     * @return  the collection of Ingestion Service responses reporting an exception
//     * 
//     * @throws IllegalStateException    stream pool instance was never activated
//     */
//    public List<IngestionResponse>    getIngestionExceptions() throws IllegalStateException {
//        
//        // Check if activated
//        if (this.setStreamTasks == null)
//            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - instance was never activated.");
//
//        // Returned value
//        ArrayList<IngestionResponse>    lstRsps = new ArrayList<>(this.setRspRecsBad);
//        
//        return lstRsps;
//    }
    
    
    /**
     * <p>
     * Returns whether or not the data stream(s) have been activated.
     * </p>
     * <p>
     * The returned value indicates the current <em>state</em> of the this instance.
     * <ul>
     * <li>
     * <code>true</code> - activated state: 
     * A returned value of <code>true</code> indicates that the 
     * <code>{@link #activate(int)}}</code> method has been called and the stream pool has
     * not been shut down.  In this state the stream pool is able to accept and transmit ingestion data.
     * </li>
     * <br/>
     * <li>
     * <code>false</code> - inactive state:   
     * A value <code>false</code> indicates that the stream pool was
     * never activated, or was activated then shut down.  In either case the processor instance
     * will not transmit ingestion data from the message supplier, even if available.
     * </li>
     * </ul>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * It is possible to activate and shut down a stream pool instance multiple times.
     * In this respect, the returned value indicates the phase of this activation cycle.
     * </p>
     * 
     * @return  <code>true</code> if the stream pool is ready for data transmission,
     *          <code>false</code> the stream pool is shut down or has never been activated
     */
    public boolean isActive() {
        return this.bolActive;
    }
    

    //
    // Operations
    //
    
    /**
     * <p>
     * Activates the ingestion stream pool.
     * </p>
     * <p>
     * The <code>IMessageSupplier&lt;IngestDataRequest&gt;</code> instance provided at construction
     * <em>must be enabled</em> for this method to be successful.  Due to the design of the 
     * <code>{@link IngestionStream}</code> class, launching the gRPC stream thread task will
     * immediately complete if the supplier is inactive (has no pending messages).
     * Thus, make sure to enabled the message supplier before invoking this method.
     * </p> 
     * <p>
     * Once this method returns all gRPC streaming tasks are created and enabled.  
     * Processed messages available from the message supplier are transmitted to the Ingestion Service 
     * via the gRPC streaming tasks.
     * </p>
     * <h2>Operation</h2>
     * This method starts all ingestion data transmission tasks which are then 
     * continuously enabled throughout the lifetime of this instance, or until explicitly shut down.  
     * Processing and streaming tasks execute independently in
     * thread pools where they block until ingestion messages become available.
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
     * This method can be called repeatedly after proper shut down.
     * </li>
     * <br/>
     * <li>
     * A shutdown operation should always be invoked when the stream pool is no longer needed.
     * </li>
     * <br/>
     * <li>
     * This method creates the thread pool executor service, the streaming tasks 
     * (<code>{@link IngestionStream}</code> objects), all containers for the thread <code>Future</code> objects,
     * all containers for streaming tasks, and all containers for the responses retrieved from the streaming tasks.
     * Thus, activation is a non-trivial operation and cycling through activation-shutdown-reactivation should be
     * recognized as such.
     * </ul>
     * </p>
     * 
     * @return  <code>true</code> if the processor was successfully activated,
     *          <code>false</code> if the processor was already enabled
     * 
     * @throws IllegalStateException            the message supplier is not enabled
     * @throws UnsupportedOperationException    an unsupported ingestion stream type was specified
     * @throws RejectedExecutionException       (internal) unable to start a streaming task
     */
    synchronized 
    public boolean activate() throws IllegalStateException, UnsupportedOperationException, RejectedExecutionException {
        
        // Check our state - already enabled?
        if (this.bolActive)
            return false;

        // Check message supplier state - not enabled?
        if (!this.srcRqstMsgs.isSupplying())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - IMessageSupplier instance is not enabled.");
        
        // Set activation flag before launching threads
        this.bolActive = true;
        
        // Determine the number of streams
        int cntStreams = 1;
        if (this.bolMultistream) 
            cntStreams = this.cntStreamsMax;
        
        // Create the thread pool executor and container for thread futures (used in shutdown)
        this.xtorStreamTasks = Executors.newFixedThreadPool(cntStreams);
//      this.setStreamFutures = new LinkedList<>();
        this.setStreamFutures.clear();
        
        // Create new containers for streaming tasks and streaming responses
        this.cntResponses = 0;
        this.setStreamTasks.clear();
//        this.setResponses = new LinkedList<>();
//        this.setResponses.clear();
//        this.setRspRecs.clear();
//        this.setRspRecsBad.clear();
//        this.setBadResponses = new LinkedList<>();
        this.lstBidiRsps.clear();
        this.lstUniRsps.clear();
        
        // Create the streaming tasks and submit them
        for (int iStream=0; iStream<cntStreams; iStream++) {
            IngestionStream     objStream = this.createIngestionStream();   // throws exception
            Callable<Boolean>   tskStream = (Callable<Boolean>)objStream;
            
            Future<Boolean> futStream = this.xtorStreamTasks.submit(tskStream); // throws exception
            this.setStreamFutures.add(futStream);
            this.setStreamTasks.add(objStream);
        }
        
        return true;
    }
    
    /**
     * <p>
     * Performs an orderly shutdown of all ingestion data gRPC streaming operations using default timeout limit.
     * </p>
     * <p>
     * All ingestion data transmission will continue until the <code>IMessageSupplier&lt;IngestDataRequest&gt;</code>
     * (provided at construction) becomes inactive (i.e., <code>{@link IMessageSupplier#isSupplying()}</code> returns 
     * <code>false</code>).
     * That is, transmission continues until all ingestion request messages within that supplier are exhausted.  
     * To shutdown transmission immediately without regard to any pending ingestion 
     * messages use <code>{@link #shutdownNow()}</code>.
     * </p>
     * <p>
     * This method <b>blocks</b> until all pending messages within the message supplier are exhausted.
     * Once the message supplier becomes inactive (i.e., is exhausted) the method will return. All streaming
     * tasks will be terminated at that time.
     * </p>  
     * 
     * @return  <code>true</code> if everything was shutdown, 
     *          <code>false</code> if stream pool was already shutdown or an error occurred during operation
     *          
     * @throws InterruptedException process interrupted while waiting for pending operations to complete
     */
    synchronized
    public boolean shutdown() throws InterruptedException {

        return this.shutdown(LNG_TIMEOUT_GENERAL, TU_TIMEOUT_GENERAL);
    }
    
    /**
     * <p>
     * Performs an orderly shutdown of all ingestion data gRPC streaming operations using the given timeout limits.
     * </p>
     * <p>
     * All ingestion data transmission will continue until the <code>IMessageSupplier&lt;IngestDataRequest&gt;</code>
     * (provided at construction) becomes inactive (i.e., <code>{@link IMessageSupplier#isSupplying()}</code> returns 
     * <code>false</code>).
     * That is, transmission continues until all ingestion request messages within that supplier are exhausted.  
     * To shutdown transmission immediately without regard to any pending ingestion 
     * messages use <code>{@link #shutdownNow()}</code>.
     * </p>
     * <p>
     * This method <b>blocks</b> until all pending messages within the message supplier are exhausted.
     * Once the message supplier becomes inactive (i.e., is exhausted) the method will return. All streaming
     * tasks will be terminated at that time.
     * </p>  
     * 
     * @param   lngTimeout  timeout limit to wait for streaming tasks to complete
     * @param   tuTimeout   timeout units for timeout limit
     * 
     * @return  <code>true</code> if everything was shutdown, 
     *          <code>false</code> if stream pool was already shutdown or an error occurred during operation
     *          
     * @throws InterruptedException process interrupted while waiting for pending operations to complete
     */
    synchronized
    public boolean shutdown(long lngTimeout, TimeUnit tuTimeout) throws InterruptedException {
        
        // Check state
        if (!this.bolActive)
            return false;

        // Shutdown all streaming tasks 
        this.xtorStreamTasks.shutdown();

        // Wait for all pending streaming tasks to complete - all should be complete at this point
        boolean bolResult = this.xtorStreamTasks.awaitTermination(lngTimeout, tuTimeout);
        
        // Just in case - terminate any threads still enabled (timeout occurred)
        this.setStreamFutures.forEach(future -> future.cancel(false));
        
        this.bolActive = false;

        return bolResult;
    }
    
    /**
     * <p>
     * Performs a hard shutdown of all ingestion frame processing and gRPC streaming operations.
     * </p>
     * <p>
     * All current ingestion data transmission is terminated immediately.  All ingestion streaming tasks 
     * within the thread pool are terminated, the thread pool is shut down, and the thread futures all are
     * hard cancelled.
     * </p>
     * <p>
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

        // Hard shutdown of all streaming tasks
        this.xtorStreamTasks.shutdownNow();
        
        this.setStreamTasks.forEach(task -> task.terminate());
        this.setStreamFutures.forEach(future -> future.cancel(true));
        
        this.bolActive = false;
        
        return true;
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
        if (this.enmStreamType == DpGrpcStreamType.FORWARD) {
            
            // Create the response consumer as a lambda function
            Consumer<IngestDataStreamResponse>  fncRspSink = (msgRsp) -> { 
                synchronized (this.objUniRspLock) {     // there can be multiple, concurrent streams
                    this.lstUniRsps.add(msgRsp);
                    this.cntResponses++;
                }
            };
            
            stream = IngestionStream.newUniStream(this.connIngest.getStubAsync(), this.srcRqstMsgs, fncRspSink);
        }

        // Create the stream task based upon bidirectional stream type
        else if (this.enmStreamType == DpGrpcStreamType.BIDIRECTIONAL) {
            
            // Create consumer of response messages as lambda function 
            Consumer<IngestDataResponse>    fncRspSink = (msgRsp) -> {
                synchronized (this.objBidiRspLock) {    // there can be multiple, concurrent streams
                    this.lstBidiRsps.add(msgRsp);
                    this.cntResponses++;
                }
            };
            
            stream = IngestionStream.newBidiStream(this.connIngest.getStubAsync(), this.srcRqstMsgs, fncRspSink);
        }

        else
            throw new UnsupportedOperationException(JavaRuntime.getQualifiedMethodNameSimple() + " - attempted to use an unsupported stream type " + this.enmStreamType);

        return stream;
    }
    
    /**
     * <p>
     * Processes the ingestion response message.
     * </p>
     * <p>
     * For normal operation, the argument contains no exceptional result message. 
     * If so confirmed the argument is then added to the response message container <code>{@link #setResponses}</code>
     * and the method returns.
     * If the message contains an exceptional result (i.e. <code>{@link IngestDataResponse#hasExceptionalResult()}</code>
     * returns <code>true</code>), the message is added to both containers <code>{@link #setBadResponses}</code>
     * and <code>{@link #setResponses}</code> before returning.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This method is invoked within <code>{@link IngestionStream}</code> instances through a lambda function created
     * within <code>{@link #createIngestionStream()}</code>.
     * </p>
     * 
     * @param msgRsp    an Ingestion Service response message containing acknowledgment or status error
     */
    @Deprecated(since="Oct 13, 2024", forRemoval=true)
    private void processResponse(IngestDataResponse msgRsp) {
//        IngestionResponse   recRsp = ProtoMsg.toIngestionResponse(msgRsp);
//        
//        this.setRspRecs.add(recRsp);
//        
//        // Check for exception then pass to message consumer
//        if (msgRsp.hasExceptionalResult()) {
//
//            this.setRspRecsBad.add(recRsp);
//        }
    }


}
