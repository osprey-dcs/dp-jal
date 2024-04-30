/*
 * Project: dp-api-common
 * File:	DpIngestionStream.java
 * Package: com.ospreydcs.dp.api.ingest
 * Type: 	DpIngestionStream
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
 * @since Apr 27, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest;

import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.CompletionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.AAdvancedApi;
import com.ospreydcs.dp.api.common.AAdvancedApi.STATUS;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnectionFactory;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiBase;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.ingest.model.IngestionFrame;
import com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor;
import com.ospreydcs.dp.api.model.ClientRequestId;
import com.ospreydcs.dp.api.model.IngestionResponse;
import com.ospreydcs.dp.api.model.ProviderRegistrar;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceStub;
import com.ospreydcs.dp.grpc.v1.ingestion.RegisterProviderRequest;
import com.ospreydcs.dp.grpc.v1.ingestion.RegisterProviderResponse;

/**
  * <p>
 * <h1>Data Platform Ingestion Service Application Programming Interface (API).</h1>
 * </p>
 * <p>
 * This class is an interface for Ingestion Service clients requiring extended data streaming.  
 * The class exposes all the data streaming operations of the Ingestion Service without details 
 * of the underlying gRPC mechanism.
 * </p>
 * <p>
 * <h2>Data Ingestion</h2>
 * Instances of <code>{@link IngestionFrame}</code> are the primary unit of ingestion.  A open data stream
 * with the Ingestion service can be established where <code>IngestionFrame</code> objects are
 * continuously supplied.
 * </p>
 * <p>
 * <h2>Provider Registration</h2>
 * All data providers must first register with the Ingestion Service before supplying 
 * <code>IngestionFrame</code> objects.  The Ingestion Service will then attribute all incoming
 * data to the provider as part of maintaining full data provenance.  Data providers may also
 * be identified when query the archived data.
 * </p>  
 * <p>
 * A populated <code>{@link ProviderRegistrar}</code> containing the data provider's unique name
 * must be supplied to the interface before any data ingestion.  The provider will be returned
 * a <code>{@link ProviderUID}</code> record containing the data provider's unique identifier as
 * assigned by the Ingestion Service.  If a data provider has previously registered with the 
 * Ingestion Service it will be returned its original UID.
 * </p>  
 * <p>
 * Within this interface provider registration is synonymous with the opening of a data stream.
 * The <code>ProviderRegistrar</code> record is required to open the stream.  As long as the
 * data stream is open no further identification is required.
 * </p>
 * <p>
 * <h2>Data Streams</h2>
 * Data streams are not size limited.  Incoming <code>IngestionFrame</code> object are decomposed
 * to smaller frames if their memory allocation is too large.  Additionally, stream maintain 
 * buffers of processed frames and gRPC messages so the clients can supply frame without 
 * waiting for Ingestion Service processing. However, ingestion back-pressure can  be turned
 * on to avoid back logging or overwhelming the Ingestion Service.
 * </p>
 * <p>
 * Only one data stream can be open at any instance.  However, a single interface data stream
 * may use multiple gRPC data stream to transmit ingestion data (gRPC channels can support multiple
 * gRPC data stream).  If clients require multiple, concurrent data stream they should create a 
 * <code>DpIngestionStream</code> instance for each stream.  Note that each instance will 
 * manage its own gRPC channel object.
 * </p>
 * <p>
 * <h2>Instance Creation</h2>
 * In generation object of <code>DpIngestionStream</code> should be obtained from the connection
 * factory <code>{@link DpIngestionStreamFactory}</code>.  However, there are creator and 
 * constructor method available which require a <code>{@link DpIngestionConnection}</code>
 * instance (which may be obtained from connection factory <code>DpIngestionConnectionFactory</code>.
 * </p>
 * <p>
 * <h2>Instance Shutdown</h2>
 * All instances of <code>DpIngestionStream</code> should be shutdown when no longer needed.
 * This will release all resources and increase overall performance.  See methods
 * <code>{@link #shutdownSoft()}</code> and <code>{@link #shutdownNow()}</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 27, 2024
 *
 */
public class DpIngestionStream extends
        DpServiceApiBase<DpIngestionStream, DpIngestionConnection, DpIngestionServiceGrpc, DpIngestionServiceBlockingStub, DpIngestionServiceFutureStub, DpIngestionServiceStub> {


    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>DpIngestionStream</code> attached to the 
     * given Ingestion Service connection.
     * </p>
     * <p>
     * The argument should be obtained from the appropriate connection factory,
     * specifically, <code>{@link DpIngestionConnectionFactory}</code>.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * The returned object should be shut down when no longer needed using 
     * <code>{@link #shutdownSoft()}</code> or <code>{@link #shutdownNow()}</code>.  
     * This action is necessary to release unused gRPC resources and maintain 
     * overall performance.  
     * </p>
     * 
     * @param connIngest  the gRPC channel connection to the desired DP Ingestion Service
     *  
     * @return new <code>DpIngestionStream</code> interfaces attached to the argument
     */
    public static DpIngestionStream from(DpIngestionConnection connIngest) {
        return new DpIngestionStream(connIngest);
    }

    
    //
    // Application Resources
    //
    
    /** Default Query Service configuration parameters */
    private static final DpIngestionConfig  CFG_DEFAULT = DpApiConfig.getInstance().ingest;
    
    
    //
    // Class Constants
    //
    
    /** Logging active flag */
    private static final boolean            BOL_LOGGING = CFG_DEFAULT.logging.active;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /** Provider UID counter - temporary.  Used to fake the provider registration process. */
    private static int  intProviderUidCounter = 1;
    
    
    // 
    // Interface Resources
    //
    
    /** The processor of all incoming ingestion data (only need one) */
    private final IngestionStreamProcessor  processor;
    
    
    //
    // State Variables
    //
    
    /** Stream opened flag */
    private boolean         bolOpenStream = false;
    
    /** UID for the data provider using the current stream */
    private ProviderUID     recProviderUid = null;
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpIngestionStream</code> attached to the given
     * Ingestion Service connection.
     * </p>
     * <p>
     * The argument should be obtained from the appropriate connection factory,
     * specifically, <code>{@link DpIngestionConnectionFactory}</code>.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * The returned object should be shut down when no longer needed using 
     * <code>{@link #shutdownSoft()}</code> or <code>{@link #shutdownNow()}</code>.  
     * This action is necessary to release unused gRPC resources and maintain 
     * overall performance.  
     * </p>
     * 
     * @param connIngest  the gRPC channel connection to the desired DP Ingestion Service
     * 
     * @see DpIngestionConnectionFactory
     */
    public DpIngestionStream(DpIngestionConnection connIngest) {
        super(connIngest);
        
        this.processor = IngestionStreamProcessor.from(connIngest);
    }
    
    
    //
    // Ingestion Service API
    //

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
     * This configuration parameter can only be modified <em>before</em> the stream is 
     * opened with <code>{@link #openStream(ProviderRegistrar)}</code> , otherwise an 
     * exception is throw.
     * </p>
     * 
     * @param intQueueCapacity  capacity of queue buffer before back-pressure blocking
     * 
     * @throws IllegalStateException    invoked while stream is open
     */
    synchronized
    public void enableBackPressure(int intQueueCapacity) throws IllegalStateException {
        if (this.bolOpenStream)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - cannot change back pressure when open.");
        this.processor.enableBackPressure(intQueueCapacity);
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
     * This configuration parameter can only be modified <em>before</em> the stream is 
     * opened with <code>{@link #openStream(ProviderRegistrar)}</code> , otherwise an 
     * exception is throw.
     * </p>
     * 
     * @throws IllegalStateException    invoked while stream is open
     */
    synchronized
    public void disableBackPressure() {
        this.processor.disableBackPressure();
    }
    
    /**
     * <p>
     * Opens the data stream to the Ingestion Service.
     * </p>
     * <p>
     * The data provider is first registered with the Ingestion Service to obtained its UID which
     * is attached to all incoming ingestion frames while this stream is opened.
     * The data stream is then activated so that henceforth the <code>ingest</code> operations
     * are valid.
     * </p> 
     * <p>
     * <h2>Close Stream and Shutdown Operations</h2>
     * The data stream can be closed and reopened for different data providers.
     * Use <code>{@link #closeStream()}</code> or <code>{@link #closeStreamNow()}</code>.  
     * The open/close operations may be cycled repeatedly.
     * </p>
     * <p>
     * When no longer needed the stream should be closed then shutdown.  A final shutdown operation
     * is needed to release all resources held by this stream to maintain performance.
     * </p> 
     * 
     * @param recRegistration   data provider registration information (unique name)
     * 
     * @return  the unique identifier of the data provider with the Ingestion Service
     * 
     * @throws DpIngestionException provider registration failed, or general gRPC runtime error
     * 
     * @see #closeStream()
     * @see #closeStreamNow()
     * @see #shutdownSoft()
     * @see #shutdownNow()
     */
    synchronized
    public ProviderUID  openStream(ProviderRegistrar recRegistration) throws DpIngestionException {

        // Check if stream is already open
        if (this.bolOpenStream)
            return this.recProviderUid;

        // Register the data provider with the Ingestion Service (throws exception upon failure)
        this.recProviderUid = this.registerProvider(recRegistration);

        // Start the ingestion stream processor
        this.processor.activate(this.recProviderUid);

        // Set the open stream flag and return
        this.bolOpenStream = true;
        
        return this.recProviderUid;
    }

    /**
     * <p>
     * Closes the data stream to the Ingestion Service.
     * </p>
     * <p>
     * Performs a "soft close" of the current Ingestion Service data stream.  All previously
     * submitted ingestion frames are allowed to continue processing and transmission to the
     * Ingestion Service.  However, no new ingestion frames will be accepted (unless the stream
     * is re-opened later).  
     * </p>
     * This activity may be useful
     * when clients wish to due their own performance tuning.
     * <p>
     * <h2>Ingestion Service Responses</h2>
     * The method returns a list of all responses received by the Ingestion Service during data
     * transmission, encapsulated as a client API record rather than a raw gRPC message.
     * The number of records in the list depends on several factors:
     * <ul>
     * <li>Size of the ingestion frame (i.e., memory allocation): 
     *   Large ingestion frames may be
     *   decomposed into smaller frames meeting any gRPC message size limitations.  Thus, the
     *   number of records may be larger than the number of ingestion frames in this case.
     * </li>
     * <li>gRPC message size limitation:
     *   As described above, the gRPC message size limitation will determine the number of
     *   composite ingestion frames required to conform to the limit.
     * </li>
     * <li>gRPC data stream type (either unidirectional or bidirectional):
     *   There are two possible gRPC data stream types available for data transmission.  In the
     *   bidirectional case a response is received by the Ingestion Service for every data message
     *   transmitted.  In the unidirectional cases there is at most one response from the 
     *   Ingestion Service.
     * </li>
     * </ul>
     * </p>
     * The conditions described above are technical but all can be configured within the 
     * client API configuration parameter set in <em>dp-api-config.yml</em> and available
     * in the <code>{@link DpApiConfig}</code> configuration class.
     * </p>
     * 
     * @return  list of responses from the Ingestion Service to data ingestion messages
     * 
     * @throws IllegalStateException    attempted to close an opened stream
     * @throws InterruptedException     internal processor interrupted while waiting for pending tasks
     */
    synchronized
    public List<IngestionResponse> closeStream() throws IllegalStateException, InterruptedException {
        
        // Check if stream is already close
        if (!this.bolOpenStream) {
            String      strMsg = JavaRuntime.getQualifiedCallerNameSimple() +
                    " - Attempted to close stream that was not open.";
            
            if (BOL_LOGGING) 
                LOGGER.warn(strMsg);
            
            throw new IllegalStateException(strMsg);
        }
        
        // Shutdown the internal ingestion stream processor 
        // - allows all processes to finish
        // - blocks until all processes are finished
        this.processor.shutdown();  // throw InterruptedException
        
        // Retrieve the Ingestion Service responses 
        List<IngestionResponse> lstRsps = this.processor.getIngestionResponses();
        
        // Clear the open stream flag and return responses
        this.bolOpenStream = false;
        
        return lstRsps;
    }
    
    /**
     * <p>
     * Immediately closes the data stream to the Ingestion Service.
     * </p>
     * <p>
     * Performs a "hard close" of the current Ingestion Service data stream.
     * Specifically,
     * <ul>
     * <li>All pending operations are cancelled.</li>
     * <li>All ingestion frames queued for processing are discarded.</li>
     * <li>All processed frames awaiting transmission are discarded.</li>
     * </ul>   
     * </p>
     * <p>
     * The method returns immediately after canceling all active processes.  Upon return
     * the data stream is inactive.
     * </p>
     * 
     * @return  <code>true</code> if stream was closed everything was shut down,
     *          <code>false</code> if the stream was already closed or internal process failed
     */
    synchronized
    public boolean closeStreamNow() {
        
        // Check if stream is already close
        if (!this.bolOpenStream)
            return false;
        
        // Deactivate the ingestion stream processor
        boolean bolShutdown = this.processor.shutdownNow();

        // Clear the open stream flag and return
        this.bolOpenStream = false;
        
        return bolShutdown;
    }
    
    /**
     * <p>
     * Submits the given ingestion frame for processing and transmission to the 
     * Ingestion Service.
     * </p>
     * <p>
     * The given ingestion frame is passed to the internal processor where it is
     * queued for processing and transmission.  The method returns once the
     * ingestion frame is successfully queued.  Thus, there is no guarantee that the offered
     * data has been, or will be, successfully ingested.
     * </p>
     * <p>
     * <h2>Back Pressure</h2>
     * If client back pressure is enabled, either through client API configuration or explicitly
     * with <code>{@link #enableBackPressure(int)}</code>, the method will block if the
     * outgoing message queue is at capacity and not return until the queue drops below capacity.
     * If back pressure is disabled the method simply queues up the ingestion frame for 
     * processing and transmission then returns (i.e., the queue capacity is infinite).
     * </p> 
     * 
     * @param frame ingestion frame to be processed and submitted to Ingestion Service
     * 
     * @throws IllegalStateException    attempted to submit frame to unopened data stream
     * @throws DpIngestionException     data ingestion failure - typically interruption during back pressure
     */
    public void ingest(IngestionFrame frame) throws IllegalStateException, DpIngestionException {
        this.ingest(List.of(frame));
    }
    
    /**
     * <p>
     * Submits the given collection of ingestion frames for processing and transmission to the
     * Ingestion Service.
     * </p>
     * <p>
     * The given collection of ingestion frames is passed to the internal processor where they
     * are queued for processing and transmission.  The method returns once the
     * ingestion frames are successfully queued.  Thus, there is no guarantee that all offered
     * data has been, or will be, successfully ingested.
     * </p>
     * <p>
     * <h2>Back Pressure</h2>
     * If client back pressure is enabled, either through client API configuration or explicitly
     * with <code>{@link #enableBackPressure(int)}</code>, the method will block if the
     * outgoing message queue is at capacity and not return until the queue drops below capacity.
     * If back pressure is disabled the method simply queues up the ingestion frames for 
     * processing and transmission then returns (i.e., the queue capacity is infinite).
     * </p> 
     * 
     * @param lstFrames ingestion frames to be processed and submitted to Ingestion Service
     * 
     * @throws IllegalStateException    attempted to submit frames to unopened data stream
     * @throws DpIngestionException     data ingestion failure - typically interruption during back pressure
     */
    public void ingest(List<IngestionFrame> lstFrames) throws IllegalStateException, DpIngestionException {
        
        // Check the stream state
        if (!this.bolOpenStream) {
            String      strMsg = JavaRuntime.getQualifiedCallerNameSimple()
                    + " - data ingestion attempted on unopened stream.";
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new IllegalStateException(strMsg);
        }
            
        // Submit frames to internal processor for processing and transmission
        try {
            this.processor.transmit(lstFrames);
           
            // Internal processor not ready - this should not happen (stream was opened if here)
        } catch (IllegalStateException e) {
            String      strMsg = JavaRuntime.getQualifiedCallerNameSimple()
                    + " - INTERNL ERROR - processor would not accept ingestion data: "
                    + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new DpIngestionException(strMsg, e);
            
            // Back pressure enable and we are interrupted while waiting for message queue
        } catch (InterruptedException e) {
            String      strMsg = JavaRuntime.getQualifiedCallerNameSimple()
                    + " - INTERNL ERROR - processor interrupted while waiting for ingestion: "
                    + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new DpIngestionException(strMsg, e);
        }
    }
    
    /**
     * <p>
     * Returns the current size of the queue buffer containing outgoing data ingestion 
     * messages waiting for transmission. 
     * </p>
     * <p>
     * Returns the current size of the ingest data request message queue buffer used by the
     * internal gRPC stream processor.  This value can be used to estimate transmission
     * performance by the client by timing the consumption of queued data ingestion messages.
     * </p>
     * <p>
     * Technically, the value returned is the number of 
     * <code>IngestDataRequest</code> messages that are currently queued up and waiting 
     * on an available stream processor thread for transmission.
     * </p> 
     * 
     * @return  number of <code>IngestDataRequest<code> messages in the request queue
     * 
     * @throws IllegalStateException    stream was never opened and processor never activated
     */
    public int  getOutgoingQueueSize() throws IllegalStateException {
        return this.processor.getRequestQueueSize();
    }
    
    /**
     * <p>
     * Allows clients to block until the queue buffer containing the outgoing data ingestion
     * messages empties.
     * </p>
     * <p>
     * This activity can be useful when clients wish to due their own performance tuning.
     * Clients can wait for the outgoing queue buffer used by the internal gRPC
     * stream processor to fully empty.  Specifically, clients can add a fixed number of 
     * ingestion frames then measure the time for frames to be fully processed and and 
     * transmitted to the Ingestion Service.  
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method will return immediately if the outgoing data message queue is empty.
     * </li>
     * <li>
     * This method is thread safe and multiple clients can block on this method.
     * </li>
     * <li>
     * All clients blocking on this method will unblock when the outgoing message buffer empties.
     * </li>
     * </ul>
     * </p> 
     * 
     * @throws IllegalStateException    operation invoked while stream closed and processor inactive
     * @throws InterruptedException     operation interrupted while waiting for queue ready
     */
    public void awaitOutgoingQueueEmpty() throws IllegalStateException, InterruptedException {
        this.processor.awaitRequestQueueEmpty();
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
    public List<ClientRequestId>    getClientRequestIds() throws IllegalStateException {
    
        // Get the client request IDs from the internal processor
        List<ClientRequestId>   lstIds = this.processor.getRequestIds();
        
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
    public List<IngestionResponse>  getIngestionExceptions() throws IllegalStateException {
        
        // Get the list of exceptional responses from the internal stream processor
        List<IngestionResponse> lstRsps = this.processor.getIngestionExceptions();
        
        return lstRsps;
    }

    /**
     * <p>
     * Determines whether or not the stream between the Ingestion Service is currently open.
     * </p>
     * 
     * @return  <code>true</code> if open, <code>false</code> if closed.
     */
    public boolean isStreamOpen() {
        return this.bolOpenStream;
    }
    
    
    //
    // Base Class Overrides
    //
    
    /**
     * <p>
     * Performs soft shutdown of this Ingestion Service interface.
     * </p>
     * <p>
     * This method is overridden to check for a currently open data stream.
     * Normally the data stream to the Ingestion Service should be explicitly closed before
     * calling this operation, either with <code>{@link #closeStream()}</code> or
     * <code>{@link #closeStreamNow()}</code>.  
     * If the data stream has not been closed this method will
     * perform a soft close <code>{@link #closeStream()}</code> operation, blocking until
     * complete.  
     * </p>
     * <p>
     * Once the data stream is closed, either explicitly or implicitly as described above,
     * the method defers to the base class for the actual interface shut down (e.i., involving
     * the gRPC channel under management).  
     * See <code>{@link DpServiceApiBase#shutdownSoft()}</code> documentation for details.
     * </p>
     * <p>
     * <h2>WARNING</h2>
     * Do not use this interface after calling this method, this is a finalizing operation
     * before discarding.
     * This interface is no longer valid after collection and any further operations will
     * result in exceptions.  All connections with the Ingestion Service have been terminated.
     * </p>
     *
     * @return  <code>true</code> if the Ingestion Service interface was successfully shut down,
     *          <code>false</code> if an error occurred
     *          
     * @throws  IterruptedException interrupted on a close operation or gRPC connection shut down         
     *          
     * @see #closeStream()
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#shutdownSoft()
     */
    @Override
    public boolean shutdownSoft() throws InterruptedException {
        if (this.bolOpenStream)
            try {
                this.closeStream();
                
            } catch (InterruptedException e) {
                String strMsg = JavaRuntime.getQualifiedCallerNameSimple() 
                        + " - interrupted while waiting for closeStream() operation: "
                        + e.getMessage();
                
                if (BOL_LOGGING)
                    LOGGER.error(strMsg);
                
                throw e;
            }
        
        return super.shutdownSoft();
    }

    /**
     * <p>
     * Performs hard shutdown of this Ingestion Service interface.
     * </p>
     * <p>
     * This method is overridden to check for a currently open data stream.
     * Normally the data stream to the Ingestion Service should be explicitly closed before
     * calling this operation, either with <code>{@link #closeStream()}</code> or
     * <code>{@link #closeStreamNow()}</code>.  
     * If the data stream has not been closed this method will
     * perform a soft close <code>{@link #closeStreamNow()}</code> operation, immediately
     * terminating all active processing and data transmission. 
     * </p>
     * <p>
     * Once the data stream is closed, either explicitly or implicitly as described above,
     * the method defers to the base class for the actual interface shut down (e.i., involving
     * the gRPC channel under management).  
     * See <code>{@link DpServiceApiBase#shutdownNow()}</code> documentation for details.
     * </p>
     * <p>
     * <h2>WARNING</h2>
     * Do not use this interface after calling this method, this is a finalizing operation
     * before discarding.
     * This interface is no longer valid after collection and any further operations will
     * result in exceptions.  All connections with the Ingestion Service have been terminated.
     * </p>
     * 
     * @return  <code>true</code> if Ingestion Service interface was successfully shut down,
     *          <code>false</code> if an error occurred
     *
     * @see #closeStreamNow()
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#shutdownNow()
     */
    @Override
    public boolean shutdownNow() {
        if (this.bolOpenStream)
            this.closeStreamNow();
        
        return super.shutdownNow();
    }


    //
    // IConnetion Interface
    //

    /**
     *
     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination()
     */
    @Override
    public boolean awaitTermination() throws InterruptedException {
        return super.grpcConn.awaitTermination();
    }


    //
    // Support Methods
    //
    
    /**
     * <p>
     * Performs the data provider registration task with the Ingestion Service.
     * </p>
     * <p>
     * The argument is converted to a <code>RegisterProviderRequest</code> message and
     * the <code>registerProvider()</code> operation is called directly on the blocking stub
     * of the gRPC ingestion connection (this is a unary operation).  Any gRPC runtime
     * exceptions are caught.  The response is checked for exceptions then returned as
     * a <code>ProviderUID</code> record if successful.
     * </p>  
     * <p>
     * <h2>Status</h2>
     * The Ingestion Service has not yet implemented data provider registration.
     * Thus, this method always returns a
     * 
     * @param recRegistration   data provider registration information (unique name)
     * 
     * @return  record containing unique identifier of the data provider with the Ingestion Service
     * 
     * @throws DpIngestionException either a gRPC runtime exception occurred or registration failed
     */
    @AAdvancedApi(status=STATUS.DEVELOPMENT, note="Provider registration is currently not implemented by Ingestion Service")
    private ProviderUID registerProvider(ProviderRegistrar recRegistration) throws DpIngestionException {

        // Provider registration fake
        ProviderUID     recUid = ProviderUID.from(intProviderUidCounter);
        intProviderUidCounter++;
        
        return recUid;
        
        // Future Implementation of Provider Registration
//        // Create the request message and response buffer
//        RegisterProviderRequest     msgRqst = ProtoMsg.from(recRegistration);
//        RegisterProviderResponse    msgRsp = null;
//        
//        // Perform the registration request
//        try {
//            // Attempt blocking unary RPC call 
//            msgRsp = super.grpcConn.getStubBlock().registerProvider(msgRqst);
//            
//        } catch (io.grpc.StatusRuntimeException e) {
//            String  strMsg = JavaRuntime.getQualifiedCallerNameSimple()
//                           + " - gRPC threw runtime exception attempting to register provider: "
//                           + "type=" + e.getClass().getName()
//                           + ", details=" + e.getMessage();
//            
//            if (BOL_LOGGING)
//                LOGGER.error(strMsg);
//            
//            throw new DpIngestionException(strMsg, e);
//        }
//     
//        // Unpack the results - checking for failed registration
//        ProviderUID recUid;
//        
//        try {
//            recUid = ProtoMsg.toProviderUID(msgRsp);
//            
//        } catch (MissingResourceException e) {
//            String  strMsg = JavaRuntime.getQualifiedCallerNameSimple()
//                    + " - data provider registration failed: "
//                    + e.getMessage();
//
//            if (BOL_LOGGING)
//                LOGGER.error(strMsg);
//
//            throw new DpIngestionException(strMsg, e);
//        }
//        
//        // Everything worked - return the provider UID
//        return recUid;
    }
}
