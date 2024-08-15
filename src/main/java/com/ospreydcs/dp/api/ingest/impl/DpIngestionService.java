/*
 * Project: dp-api-common
 * File:	DpIngestionService.java
 * Package: com.ospreydcs.dp.api.ingest
 * Type: 	DpIngestionService
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
 * @since Mar 28, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.CompletionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.AUnavailable;
import com.ospreydcs.dp.api.common.AUnavailable.STATUS;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnectionFactory;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiBase;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.ingest.DpIngestionException;
import com.ospreydcs.dp.api.ingest.IIngestionService;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameBinner;
import com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameConverter;
import com.ospreydcs.dp.api.ingest.model.grpc.ProviderRegistrationService;
import com.ospreydcs.dp.api.model.IngestionResponse;
import com.ospreydcs.dp.api.model.ProviderRegistrar;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceStub;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataResponse;

/**
 * <p>
 * <h1>Data Platform Ingestion Service Application Programming Interface (API).</h1>
 * </p>
 * <p>
 * This class is an interface for Ingestion Service clients only requiring blocking, synchronous
 * ingestion (i.e., unary RPC operations).  The class exposes all the unary operations of the 
 * Ingestion Service without details of the underlying gRPC mechanism.
 * </p>
 * <p>
 * This Ingestion Service interface is available for data providers with minimal requirements.
 * Data providers intending to supply large amounts of data over extended durations should
 * consider using <code>{@link DpIngestionStream}</code>.
 * </p>
 * <p>
 * <h2>Data Ingestion</h2>
 * Instances of <code>{@link IngestionFrame}</code> are the primary unit of ingestion.  Objects
 * of <code>IngestionFrame</code> are ingested one at a time using single, unary, RPC operations.
 * Here, ingestion frame may be subject to gRPC message size limitations.  If the memory allocation
 * of an ingestion frame is too large the ingestion operation can be rejected.  For large
 * ingestion frame use <code>{@link DpIngestionStream}</code>
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
 * Within this interface provider registration must be done explicitly.
 * The <code>ProviderRegistrar</code> record for the registration operation.  The data provider
 * must identify itself with the returns <code>ProviderUID</code> record for each ingestion
 * operation.
 * </p>
 * <p>
 * <h2>Instance Creation</h2>
 * In generation objects of <code>DpIngestionService</code> should be obtained from the connection
 * factory <code>{@link DpIngestionServiceFactory}</code>.  However, there are creator and 
 * constructor method available which require a <code>{@link DpIngestionConnection}</code>
 * instance (which may be obtained from connection factory <code>DpIngestionConnectionFactory</code>.
 * </p>
 * <p>
 * <h2>Instance Shutdown</h2>
 * All instances of <code>DpIngestionService</code> should be shutdown when no longer needed.
 * This will release all resources and increase overall performance.  See methods
 * <code>{@link #shutdown()}</code> and <code>{@link #shutdownNow()}</code>.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Mar 28, 2024
 *
 */
public final class DpIngestionService extends
        DpServiceApiBase<DpIngestionService, DpIngestionConnection, DpIngestionServiceGrpc, DpIngestionServiceBlockingStub, DpIngestionServiceFutureStub, DpIngestionServiceStub> implements IIngestionService {

    //
    // Application Resources
    //
    
    /** Default Query Service configuration parameters */
    private static final DpIngestionConfig  CFG_DEFAULT = DpApiConfig.getInstance().ingest;
    
    
    //
    // Class Constants
    //
    
    /** Logging active flag */
    private static final boolean    BOL_LOGGING = CFG_DEFAULT.logging.active;

    
//    /** General operation timeout limit */
//    private static final long       LNG_TIMEOUT_GENERAL = CFG_DEFAULT.timeout.limit;
//    
//    /** General operation timeout units */
//    private static final TimeUnit   TU_TIMEOUT_GENERAL = CFG_DEFAULT.timeout.unit;
//    
    
    //
    // Class Constants - Default Values
    //
    
//    /** Are general concurrency active - used for ingestion frame decomposition */
//    private static final Boolean    BOL_CONCURRENCY_ACTIVE = CFG_DEFAULT.concurrency.active;
//    
//    /** Thresold in which to pivot to concurrent processing */
//    private static final Integer    INT_CONCURRENCY_PIVOT_SZ = CFG_DEFAULT.concurrency.pivotSize;
//    
//    /** Maximum number of concurrent processing threads */
//    private static final Integer    INT_CONCURRENCY_CNT_THREADS = CFG_DEFAULT.concurrency.threadCount;
    
    
//  /** Use ingestion frame buffering from client to gRPC stream */
//  private static final Boolean    BOL_BUFFER_ACTIVE = CFG_DEFAULT.stream.buffer.active;
  
    /** Perform ingestion frame decomposition (i.e., "binning") */
    private static final Boolean    BOL_BINNING_ACTIVE = CFG_DEFAULT.decompose.active;
    
    /** Maximum size limit (in bytes) of decomposed ingestion frame */
    private static final Integer    LNG_BINNING_MAX_SIZE = CFG_DEFAULT.decompose.maxSize;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger LOGGER = LogManager.getLogger();
    
    
    // 
    // Instance  Resources
    //
    
    /** The ingestion frame to Protobuf message converter tool */
    private final IngestionFrameConverter   toolFrmToMsg = IngestionFrameConverter.create();
    
    /** The ingestion frame decomposition tool (created if used) */
    private IngestionFrameBinner            toolFrmDecomposer = null;
    
    
    //
    // Configuration Parameters
    //
    
    /** Ingestion frame decomposition (binning) enabled flag  */
    private boolean bolDecompAuto = BOL_BINNING_ACTIVE;
    
    /** Ingestion frame decomposition maximum size */
    private long    lngBinSizeMax = LNG_BINNING_MAX_SIZE;

    /** Data Provider UID record obtained from registration */
    private ProviderUID     recProviderUid = null;
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>DpIngestionService</code> attached to the 
     * given connection.
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
     * @return new <code>DpIngestionService</code> interfaces attached to the argument
     * 
     * @see DpIngestionConnectionFactory
     */
    public static DpIngestionService from(DpIngestionConnection connIngest) {
        return new DpIngestionService(connIngest);
    }
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpIngestionService</code>.
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
    public DpIngestionService(DpIngestionConnection connIngest) {
        super(connIngest);
        
        if (this.bolDecompAuto) 
            this.toolFrmDecomposer = IngestionFrameBinner.from(this.lngBinSizeMax);
    }


    //
    // Configuration
    //
    
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
     * <h2>WARNINGS:</h2>
     * <ul>
     * <li>
     * If automatic ingestion frame decomposition is disabled it is imperative that all
     * offered ingestion frames have memory allocations less than the current gRPC message
     * size limit or gRPC will throw a runtime exception.
     * </li>
     * <br/>
     * <li>
     * When an ingestion frame is decomposed the original frame is destroyed.  If automatic
     * decomposition is enabled, recognize that the original ingestion frame 
     * may no longer be viable after being offered for ingestion (it will be empty).
     * </li>
     * </ul>
     * </p>
     * 
     * @param lngMaxBinSize maximum allowable size (in bytes) decomposed ingestion frames 
     */
    synchronized 
    public void enableFrameDecomposition(long lngMaxBinSize) {

        this.bolDecompAuto = true;
        this.lngBinSizeMax = lngMaxBinSize;
        this.toolFrmDecomposer = IngestionFrameBinner.from(lngMaxBinSize);
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
     * When an ingestion frame is decomposed the original frame is destroyed.  If automatic
     * decomposition is enabled, recognize that the original ingestion frame 
     * may no longer be viable after being offered for ingestion (it will be empty).
     * </li>
     * </ul>
     * </p>
     * 
     */
    synchronized
    public void disableFrameDecomposition() {
        this.bolDecompAuto = false;
    }
    
    
    //
    // Ingestion Service API
    //
    
    /**
     * <p>
     * Register the Data Provider with the Ingestion Service for later data ingestion operations.
     * </p>
     * <p>
     * Data providers must first register with the Ingestion Service using their unique name and optional
     * attributes.  Upon registration the Ingestion Service returns a UID for the provider which it must use
     * for all subsequent ingestion operations.  If a data provider has previously registered with the Ingestion 
     * Service its original UID is returned.
     * </p>
     * <p>
     * <h2>Provider Attributes</h2>
     * Data provider attributes can be added in the form of (name, value) pairs (of strings) during registration.
     * Use the <code>{@link ProviderRegistrar#addAttribute(String, String)}</code> method to assignment attribute
     * pairs to a data provider (this method may be called as many times as desired, once for each attribute pair).
     * Data Provider attributes are used as metadata to garnish the Data Platform data archive.
     * </p>
     * 
     * @param recRegistration   record containing data provider unique name and any optional attributes
     * 
     * @return                  record containing the data provider UID
     *         
     * @throws DpIngestionException     registration failure or general communications exception (see details)
     */
    @Override
    @AUnavailable(status=STATUS.ACCEPTED, note="Operation is mocked - Not implemented within the Ingestion Service.")
    public ProviderUID  registerProvider(ProviderRegistrar recRegistration) throws DpIngestionException {
        
        this.recProviderUid = ProviderRegistrationService.registerProvider(super.grpcConn, recRegistration);

        return this.recProviderUid;
        
//        // Create the Protobuf request message from the argument 
//        RegisterProviderRequest     msgRqst = RegisterProviderRequest.newBuilder()
//                .setProviderName(recRegistration.name())
//                .addAllAttributes(ProtoMsg.createAttributes(recRegistration.attributes()))
//                .setRequestTime(ProtoMsg.from(Instant.now()))
//                .build();
//        
//        // Perform the registration request
//        RegisterProviderResponse    msgRsp;
//        
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
//        // Exception checking
//        if (msgRsp.hasExceptionalResult()) {
//            ExceptionalResult       msgExcept = msgRsp.getExceptionalResult();
//            ExceptionalResultStatus enmStatus = msgExcept.getExceptionalResultStatus();
//            String                  strDetails = msgExcept.getMessage();
//            
//            String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() 
//                            + " - Provider registration failed: " 
//                            + " status=" + enmStatus
//                            + ", details=" + strDetails;
//            
//            // Log exception if logging
//            if (BOL_LOGGING)
//                LOGGER.error(strMsg);
//            
//            throw new DpIngestionException(strMsg);
//        }
//        
//        // Extract the provider UID and create return value
//        RegistrationResult  msgResult = msgRsp.getRegistrationResult();
//        int                 intUid = msgResult.getProviderId();
//        
//        return new ProviderUID(intUid);
    }
    
    /**
     * <p>
     * Blocking, synchronous, unary ingestion of the given ingestion frame.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Clients must first register with the Ingestion Service before invoking this method otherwise
     * an exception is thrown.
     * </li>
     * <li>
     * A list of Ingestion Service response is returned in order to accommodate all cases.
     *   <ul>
     *   <li>Frame Decomposition: If the frame has allocation larger than the maximum it is decomposed into composite 
     *       frames each receiving a response upon transmission.</li>
     *   <li>No Decomposition: If the frame has allocation less than the maximum limit a single response is
     *       contained in the list.</li>
     *   </ul>
     * </li>
     * </p>
     * 
     * @param frame ingestion frame containing data for transmission to the Ingestion Service
     * 
     * @return  collection of Ingestion Service responses to data within ingestion frame
     * 
     * @throws IllegalStateException    unregistered Data Provider
     * @throws DpIngestionException general ingestion exception (see detail and cause)
     */
    @Override
    public List<IngestionResponse> ingest(IngestionFrame frame) throws IllegalStateException, DpIngestionException {
        
        // Check registration
        if (this.recProviderUid == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Data Provider was not registered.");
        
        // Create list of frames to be ingested - depends on auto-decomposition
        List<IngestionFrame>    lstFrames = this.decomposeFrame(frame); // throws DpIngestionException
        
        // Convert frames to messages and transmit, recovering responses
        List<IngestionResponse> lstRsps = this.transmitFrames(this.recProviderUid, lstFrames); // throws DpIngestionException

        // Return responses
        return lstRsps;
    }
    
    
    //
    // IConnection Interface
    //
    
    /**
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
     * Decomposes large ingestion frames if automatic decomposition is enabled.
     * </p>
     * <p>
     * Attempts to decompose the given ingestion frame horizontally (by columns).  If
     * column size is too large to fit memory allocation limit an exception is throw.
     * </p>
     * <p>
     * If automatic frame decomposition is disabled then the method simple returns a 
     * list of one element, the given ingestion frame.
     * </p>
     * <p>
     * <h2>WARNING</h2>
     * If the frame is decomposed then the original ingestion frame is destroyed.
     * </p>
     * 
     * @param frame     ingestion frame to be decomposed
     * 
     * @return          list of composite ingestion frames if decomposed, 
     *                  otherwise original frame
     *                   
     * @throws DpIngestionException
     */
    private List<IngestionFrame>    decomposeFrame(IngestionFrame frame) throws DpIngestionException {

        // Check if auto-decomposition is disabled
        if (!this.bolDecompAuto) {
            
            return  List.of(frame);
        }
        
        // Decompose the original ingestion frame if too large
        try {
            List<IngestionFrame>    lstFrames = this.toolFrmDecomposer.decomposeHorizontally(frame);
            
            return lstFrames;
        
            // The ingestion frame could not be decomposed - columns too large
        } catch (IllegalArgumentException e) {
            String      strMsg = JavaRuntime.getQualifiedCallerNameSimple() 
                    + " - ingestion frame decomposition (by column) FAILED, columns are too large: "
                    + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpIngestionException(strMsg, e);
            
            // Ingestion frame decomposition error
        } catch (CompletionException e) {
            String      strMsg = JavaRuntime.getQualifiedCallerNameSimple() 
                    + " - ingestion frame decomposition (by column) ERROR: "
                    + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpIngestionException(strMsg, e);
        }
    }
    
    /**
     * <p>
     * Transmits all given data to the Ingestion Service using unary RPC calls.
     * </p>
     * <p>
     * Each ingestion frame within the argument is converted to a <code>IngestDataRequest</code>
     * message using the given provider UID and an internally generated client request ID.
     * The messages are transmitted serially, in order, using synchronous, unary gRPC calls
     * on the blocking communications stub obtained from the super class.
     * </p>
     * <p>
     * <h2>Exception Conditions</h2>
     * There are 2 exceptions conditions.  
     * <ol>
     * <li><code>io.grpc.StatusRuntimeException</code> - thrown during the gRPC ingestion operation. 
     *   Indicates a general, and unexpected, gRPC exception encountered during unary ingestion.
     *   </li>
     * <li><code>MissingResourceException</code> - thrown converting the <code>IngestDataResponse</code>
     *   message to a client APi record.  Indicates that the Ingestion Service rejected the ingestion
     *   request.
     *   </li>
     * </ol> 
     * A <code>DpIngestionException</code> type exception is thrown in either case.  The original 
     * exception is included as the cause parameter.
     * </p>
     * 
     * @param recUid    UID of the data provider supplying the ingestion frames
     * @param lstFrames collection of ingestion frames to be converted and transmitted 
     * 
     * @return  ordered list of Ingestion Service ingest data responses
     * 
     * @throws DpIngestionException unexpected gRPC runtime exception or request was rejected (see details)
     */
    private List<IngestionResponse> transmitFrames(ProviderUID recUid, List<IngestionFrame> lstFrames) throws DpIngestionException {
        
        // Returned collection
        List<IngestionResponse> lstRsps = new LinkedList<>();
        
        // For each ingestion frame transmit synchronously, in serial, using unary RPC
        for (IngestionFrame frm : lstFrames) {

            // Convert ingestion frame to ingest data message
            IngestDataRequest   msgRqst = this.toolFrmToMsg.createRequest(frm, recUid);
            
            try {
                // Transmit data message and recover response - throws StatusRuntimeException
                IngestDataResponse msgRsp = super.grpcConn.getStubBlock().ingestData(msgRqst);
                
                // Convert response to client API record - throws MissingResourceException if rejected
                IngestionResponse  recRsp = ProtoMsg.toIngestionResponse(msgRsp);

                // Add to returned list
                lstRsps.add(recRsp);
                
                // Unexpected gRPC runtime exception during ingestion 
            } catch (io.grpc.StatusRuntimeException e) {
                String      strMsg = JavaRuntime.getQualifiedCallerNameSimple()
                        + " - gRPC runtime exception during unary ingestData() operation: "
                        + e.getMessage();
                
                if (BOL_LOGGING)
                    LOGGER.error(strMsg);
                
                throw new DpIngestionException(strMsg, e);
                
                // The Ingestion Service rejected the ingestion request
            } catch (MissingResourceException e) {
                String      strMsg = JavaRuntime.getQualifiedCallerNameSimple()
                        + " - Ingestion Service rejected unary ingestData() operation: "
                        + e.getMessage();
                
                if (BOL_LOGGING)
                    LOGGER.error(strMsg);
                
                throw new DpIngestionException(strMsg, e);
            }
        }
        
        return lstRsps;
    }
    
//    /**
//     * Returns the <code>DpGrpcConnection</code> in super class cast to a
//     * <code>DpIngestionConnection</code> instance.
//     * 
//     * @return connection instances as a <code>DpIngestionConnection</code> object
//     */
//    private DpIngestionConnection getConnection() {
//        return super.grpcConn;
//    }
    
}
