/*
 * Project: dp-api-common
 * File:	IngestionStreamPool.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IngestionStreamPool
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

import java.util.ArrayList;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.ingest.model.IMessageSupplier;
import com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor;
import com.ospreydcs.dp.api.model.ClientRequestId;
import com.ospreydcs.dp.api.model.DpGrpcStreamType;
import com.ospreydcs.dp.api.model.IngestionResponse;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataResponse;

/**
 * <p>
 * Implements a cooperating collection of <code>IngestionStream</code> instances used for concurrent data
 * transmission to the Ingestion Service.
 * </p>
 * <p>
 * Class instances transmit processed client ingestion data using a collection of 
 * <code>{@link IngestionStream}</code> instances - one for each active gRPC data stream.  Processed ingestion
 * data is represented as a stream of <code>IngestDataRequest</code> Protobuf messages.
 * The stream classes are executed on separate threads that all compete for the 
 * <code>IngestDataRequest</code> messages produced by the <code>IMessageSupplier&lt;IngestDataRequest&gt;</code>.
 * (If the multiple data streams feature is disabled only one stream will be active.)  
 * </p>
 * <h2>Operation</h2>
 * After creation instances of <code>IngestionStreamPool</code> must be activated using
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
 * <code>IngestionStreamPool</code> instance.  For example, an instance can be activated
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
 * continuously active throughout the lifetime of this instance, or explicitly shut down.  
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
 * <h2>NOTES:</h2>
 * At the time of this documentation only the bidirectional streaming operation is supported
 * by the Ingestion Service (i.e., <code>{@link DpGrpcStreamType#BIDIRECTIONAL}</code>).  
 * Attempting to use a unidirectional stream could result in a RPC failure at the 
 * Ingestion Service.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Jul 22, 2024
 *
 */
public class IngestionStreamPool {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Constructs a new <code>IngestionStreamPool</code> instance ready for activation.
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
     * @return
     */
    public static IngestionStreamPool from(IMessageSupplier<IngestDataRequest> srcRqstMsgs, DpIngestionConnection connIngest) {
        return new IngestionStreamPool(srcRqstMsgs, connIngest);
    }
    
    //
    // Application Resources
    //
    
    /** The Ingestion Service client API default configuration */
    private static final DpIngestionConfig  CFG_DEFAULT = DpApiConfig.getInstance().ingest; 
    
    
    //
    // Class Constants
    //
    
    /** General operation timeout limit */
    private static final long       LNG_TIMEOUT_GENERAL = CFG_DEFAULT.timeout.limit;
    
    /** General operation timeout units */
    private static final TimeUnit   TU_TIMEOUT_GENERAL = CFG_DEFAULT.timeout.unit;
    
    
    /** Event logging active flag */
    private static final boolean    BOL_LOGGING = CFG_DEFAULT.logging.active;
    
    
    /** The default gRPC stream type */
    private static final DpGrpcStreamType   ENM_STREAM_TYPE = CFG_DEFAULT.stream.type;
    
    /** Use multiple gRPC data stream to transmit ingestion frames */
    private static final Boolean            BOL_MULTISTREAM_ACTIVE = CFG_DEFAULT.stream.concurrency.active;
    
    /** The maximum number of gRPC data stream used to transmit ingestion data */
    private static final Integer            CNT_MULTISTREAM_MAX = CFG_DEFAULT.stream.concurrency.maxStreams;
    
//  /** When the number of frames available exceeds this value multiple gRPC data streams are used */ 
//  private static final Long       LNG_MULTISTREAM_PIVOT = CFG_DEFAULT.stream.concurrency.pivotSize;
  
    
    //
    // Class Resources
    //
    
    /** Class event logger instance */
    protected static final Logger     LOGGER = LogManager.getLogger();
    
    
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
    private ExecutorService             xtorStreamTasks = null;
    
    /** Collection of executing stream processing tasks  */
    private Collection<IngestionStream> setStreamTasks = null;
    
    /** Collection of future results from streaming tasks - used for shutdowns */
    private Collection<Future<Boolean>> setStreamFutures = null;

    
    /** Collection of all incoming ingestion responses - from bidirectional streaming tasks */
    private Collection<IngestDataResponse>      setResponses = null; // = new LinkedList<>();
    
    /** Collection of all ingestion responses with exceptions */
    private Collection<IngestDataResponse>      setBadResponses = null;
    
    
    // 
    // State Variables
    //
    
    /** Are streaming tasks active */
    private boolean bolActive = false;
    
    
    
    //
    // Constructors
    // 
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionStreamPool</code> using the given message source and connection to the
     * Ingestion Service.
     * </p>
     *
     * @param srcRqstMsgs   the supplier of incoming, processed <code>IngestDataRequest</code> messages 
     * @param connIngest    Java API encapsulated gRPC connection to the Ingestion Service
     */
    public IngestionStreamPool(IMessageSupplier<IngestDataRequest> srcRqstMsgs, DpIngestionConnection connIngest) {
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
     * @throws IllegalStateException            method called while processor is active
     * @throws UnsupportedOperationException    an unsupported stream type was provided
     */
    public void setStreamType(DpGrpcStreamType enmStreamType) throws IllegalStateException, UnsupportedOperationException {

        // Check current state
        if (this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change gRPC stream type once activated.");
        
        // Check the argument
        if (enmStreamType == DpGrpcStreamType.BACKWARD)
            throw new UnsupportedOperationException(JavaRuntime.getQualifiedCallerNameSimple() + " - " + enmStreamType + " not supported");
        
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
     * @throws IllegalStateException    method called while processor is active
     * @throws IllegalArgumentException the argument was zero or negative
     * 
     * @see #CNT_MULTISTREAM_MAX
     */
    public void setMultipleStreams(int cntStreamsMax) throws IllegalStateException, IllegalArgumentException {

        // Check current state
        if (this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change stream count once activated.");
        
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
        if (this.bolActive)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Cannot change concurrency once activated.");
        
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
     * @return  <code>true</code> if multi-streaming is active, <code>false</code> otherwise (only a single stream is used)
     */
    public boolean  hasMultipleStreams() {
        return this.bolMultistream;
    }
    
    /**
     * <p>
     * Returns the maximum number of concurrent gRPC data streams used to transmit data to the Ingestion Service.
     * </p>
     * <p>
     * If multiple data streaming is active this is the number of concurrent data stream used by the pool to 
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
     * is the total number of messages transmitted while active.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The value returned by this method is not necessary equal to the number of 
     * <code>IngestionFrame</code> instances offered to upstream processing.  
     * If ingestion frame decomposition
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
     * <br/>
     * <li>
     * If the <code>active()</code> method is called after a shutdown the returned value 
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
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - instance was never activated.");

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
    public List<ClientRequestId>    getRequestIds() throws IllegalStateException {
        
        // Check if activated
        if (this.setStreamTasks == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processor was never activated.");

        // Collect all the client IDs recorded within each ingestion stream
        List<ClientRequestId>   lstIds = this.setStreamTasks
                .stream()
                .<ClientRequestId>flatMap(stream -> stream.getRequestIds().stream())
                .toList();
        
        return lstIds;
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
     * <li>
     * This method returns a collection of <code>{@link IngestionResponse}</code> records native to the API
     * library, rather than <code>{@link IngestDataResponse}</code> Protocol Buffers messages.  This action
     * is simply for convenience. 
     * </ul>
     * </p>
     * 
     * @return  a list of all Ingestion Service responses to all transmitted ingestion requests
     * 
     * @throws IllegalStateException    processor was never activated
     * @throws MissingResourceException a IngestDataResponse message could not be converted to API record
     */
    public List<IngestionResponse>   getIngestionResponses() throws IllegalStateException, MissingResourceException {

        // Check if activated
        if (this.setStreamTasks == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - processor was never activated.");

        // Iterate through the collection of IngestDataResponse messages converting them to API record instance
        List<IngestionResponse> lstRsps = new ArrayList<>(this.setResponses.size());
        for (IngestDataResponse msgRsp : this.setResponses) {
            IngestionResponse   recRsp = ProtoMsg.toIngestionResponse(msgRsp);
            
            lstRsps.add(recRsp);
        }
        
        // Iterate through the collection of IngestDataResponse messages converting them to API record instance
        //  Not sure why this is unstable
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
     * The stream pool monitors incoming responses from the Ingestion Service for exceptions.
     * The returned list is the collection of all responses indicating an exception with
     * its corresponding request, which may be identified by the client request ID.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Any ingestion request message identified in the list (i.e., by client request ID) had
     * some type of exception encountered immediately by the Ingestion Service.  The exception
     * details are included in the response.
     * </li>
     * <li>
     * If a client request ID does not appear in the returned list there is no guarantee that
     * an error did not occur later in the Ingestion Service processing and archiving.
     * One may query the Ingestion Service, via client request ID, for all client ingestion
     * requests that failed processing post transmission. 
     * </li>
     * </ul>
     * </p>
     *   
     * @return  the collection of Ingestion Service responses reporting an exception
     * 
     * @throws IllegalStateException    stream pool instance was never activated
     */
    public List<IngestionResponse>    getIngestionExceptions() throws IllegalStateException {
        
        // Check if activated
        if (this.setStreamTasks == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - instance was never activated.");

        // Iterate through the list of known exceptional responses and convert them to IngestionResponse records
        return this.setBadResponses
                .stream()
                .<IngestionResponse>map(ProtoMsg::toIngestionResponse)
                .toList();
    }
    
    
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
     * <em>must be active</em> for this method to be successful.  Due to the design of the 
     * <code>{@link IngestionStream}</code> class, launching the gRPC stream thread task will
     * immediately complete if the supplier is inactive (has no pending messages).
     * Thus, make sure to active the message supplier before invoking this method.
     * </p> 
     * <p>
     * Once this method returns all gRPC streaming tasks are created and active.  
     * Processed messages available from the message supplier are transmitted to the Ingestion Service 
     * via the gRPC streaming tasks.
     * </p>
     * <h2>Operation</h2>
     * This method starts all ingestion data transmission tasks which are then 
     * continuously active throughout the lifetime of this instance, or until explicitly shut down.  
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
     *          <code>false</code> if the processor was already active
     * 
     * @throws IllegalStateException            the message supplier is not active
     * @throws UnsupportedOperationException    an unsupported ingestion stream type was specified
     * @throws RejectedExecutionException       (internal) unable to start a streaming task
     */
    synchronized 
    public boolean activate() throws IllegalStateException, UnsupportedOperationException, RejectedExecutionException {
        
        // Check our state - already active?
        if (this.bolActive)
            return false;

        // Check message supplier state - not active?
        if (!this.srcRqstMsgs.isActive())
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - IMessageSupplier instance is not active.");
        
        // Set activation flag before launching threads
        this.bolActive = true;
        
        // Determine the number of streams
        int cntStreams = 1;
        if (this.bolMultistream) 
            cntStreams = this.cntStreamsMax;
        
        // Create the thread pool executor and container for thread futures (used in shutdown)
        this.xtorStreamTasks = Executors.newFixedThreadPool(cntStreams);
        this.setStreamFutures = new LinkedList<>();
        
        // Create new containers for streaming tasks and streaming responses
        this.setStreamTasks = new LinkedList<>();
        this.setResponses = new LinkedList<>();
        this.setBadResponses = new LinkedList<>();
        
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
     * Performs an orderly shutdown of all ingestion data gRPC streaming operations.
     * </p>
     * <p>
     * All ingestion data transmission will continue until the <code>IMessageSupplier&lt;IngestDataRequest&gt;</code>
     * (provided at construction) becomes inactive (i.e., <code>{@link IMessageSupplier#isActive()}</code> returns 
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
        
        // Check state
        if (!this.bolActive)
            return false;

        // Shutdown all streaming tasks 
        this.xtorStreamTasks.shutdown();
//        this.setStreamTasks.forEach(task -> task.terminate());
        this.setStreamFutures.forEach(future -> future.cancel(false));
        
        // Wait for all pending streaming tasks to complete - all should be complete at this point
        boolean bolResult = this.xtorStreamTasks.awaitTermination(LNG_TIMEOUT_GENERAL, TU_TIMEOUT_GENERAL);
        
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
        if (this.enmStreamType == DpGrpcStreamType.FORWARD)
            stream = IngestionStream.newUniProcessor(this.connIngest.getStubAsync(), this.srcRqstMsgs);

        // Create the stream task based upon bidirectional stream type
        else if (this.enmStreamType == DpGrpcStreamType.BIDIRECTIONAL) {
            
            // Create consumer of response messages as lambda function calling processResponse()
            Consumer<IngestDataResponse>    fncRspSink = (msgRsp) -> { this.processResponse(msgRsp); };
            
            stream = IngestionStream.newBidiProcessor(this.connIngest.getStubAsync(), this.srcRqstMsgs, fncRspSink);
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
    private void processResponse(IngestDataResponse msgRsp) {

        // Check for exception then pass to message consumer
        if (msgRsp.hasExceptionalResult()) {

            this.setBadResponses.add(msgRsp);
        }
        
        this.setResponses.add(msgRsp);
    }


}
