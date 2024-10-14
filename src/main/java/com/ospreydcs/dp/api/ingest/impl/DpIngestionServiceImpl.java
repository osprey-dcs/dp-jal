/*
 * Project: dp-api-common
 * File:	DpIngestionServiceImpl.java
 * Package: com.ospreydcs.dp.api.ingest
 * Type: 	DpIngestionServiceImpl
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
import com.ospreydcs.dp.api.model.ClientRequestUID;
import com.ospreydcs.dp.api.model.IngestionResponse;
import com.ospreydcs.dp.api.model.IngestionResult;
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
 * <h1>Implementation of the <code>IIngestionService</code> Interface API to the DP Ingestion Service.</h1>
 * </p>
 * <p>
 * This class supports the <code>IIngestionService</code> interface for Ingestion Service clients only requiring 
 * blocking, synchronous ingestion (i.e., unary RPC operations).  The class exposes all the unary operations of the 
 * Ingestion Service without details of the underlying gRPC mechanism.
 * </p>
 * <p>
 * This Ingestion Service interface is available for data providers with minimal requirements.
 * Data providers intending to supply large amounts of data over extended durations should
 * consider using <code>{@link IIngestionStream}</code>.
 * </p>
 * <p>
 * <h2>Data Ingestion</h2>
 * Instances of <code>{@link IngestionFrame}</code> are the primary unit of ingestion.  Objects
 * of <code>IngestionFrame</code> are ingested one at a time using single, unary, RPC operations.
 * Here, ingestion frame may be subject to gRPC message size limitations.  If the memory allocation
 * of an ingestion frame is too large the ingestion operation can be rejected.  For large
 * ingestion frame use <code>{@link DpIngestionStreamDeprecated}</code>
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
 * In generation objects of <code>DpIngestionServiceImpl</code> should be obtained from the connection
 * factory <code>{@link DpIngestionServiceFactory}</code>.  However, there are creator and 
 * constructor method available which require a <code>{@link DpIngestionConnection}</code>
 * instance (which may be obtained from connection factory <code>DpIngestionConnectionFactory</code>.
 * </p>
 * <p>
 * <h2>Instance Shutdown</h2>
 * All instances of <code>DpIngestionServiceImpl</code> should be shutdown when no longer needed.
 * This will release all resources and increase overall performance.  See methods
 * <code>{@link #shutdown()}</code> and <code>{@link #shutdownNow()}</code>.
 * </p>
 * <p>
 * <h2>Configuration</h2>
 * The <code>DpIngestionServiceImpl</code> class provides methods for interface configuration, which are
 * not available through the <code>IIgestionService</code> interface.  (<code>IIngestionService</code>
 * interfaces are obtained from connection factories pre-configured.)  These methods are available
 * for unit testing and performance tuning.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 28, 2024
 *
 */
public final class DpIngestionServiceImpl extends
        DpServiceApiBase<DpIngestionServiceImpl, 
                         DpIngestionConnection, 
                         DpIngestionServiceGrpc, 
                         DpIngestionServiceBlockingStub, 
                         DpIngestionServiceFutureStub, 
                         DpIngestionServiceStub> implements IIngestionService {

    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>DpIngestionServiceImpl</code> attached to the 
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
     * @return new <code>DpIngestionServiceImpl</code> interfaces attached to the argument
     * 
     * @see DpIngestionConnectionFactory
     */
    public static DpIngestionServiceImpl from(DpIngestionConnection connIngest) {
        return new DpIngestionServiceImpl(connIngest);
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
    
    /** Perform ingestion frame decomposition (i.e., "binning") */
    private static final Boolean    BOL_DECOMP_ACTIVE = CFG_DEFAULT.decompose.active;
    
    /** Maximum size limit (in bytes) of decomposed ingestion frame */
    private static final Integer    LNG_DECOMP_MAX_SIZE = CFG_DEFAULT.decompose.maxSize;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    //
    // Configuration Parameters
    //
    
    /** Ingestion frame decomposition (binning) enabled flag  */
    private boolean         bolDecompAuto = BOL_DECOMP_ACTIVE;
    
    /** Ingestion frame decomposition maximum size */
    private long            lngFrmSizeMax = LNG_DECOMP_MAX_SIZE;

    
    // 
    // Instance  Resources
    //
    
    /** The ingestion frame decomposition tool - only created if used */
    private IngestionFrameBinner            prcrFrmDecomp = null;
    
    /** The ingestion frame to Protobuf message converter tool */
    private final IngestionFrameConverter   prcrFrmCnvrtr = IngestionFrameConverter.create();
    
    
    //
    // State Variables
    //
    
    /** Data Provider UID record obtained from registration */
    private ProviderUID             recProviderUid = null;
    
    /** The number of data messages transmitted to the Ingestion Service so far */
    private int                     cntMsgXmissions = 0;
    
    /** The collection of client request UIDs of transmitted data */
    private List<ClientRequestUID>  lstRequestIds = new LinkedList<>();

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpIngestionServiceImpl</code>.
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
    public DpIngestionServiceImpl(DpIngestionConnection connIngest) {
        super(connIngest);
        
        if (this.bolDecompAuto) 
            this.prcrFrmDecomp = IngestionFrameBinner.from(this.lngFrmSizeMax);
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
        this.lngFrmSizeMax = lngMaxBinSize;
        this.prcrFrmDecomp = IngestionFrameBinner.from(lngMaxBinSize);
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
    // State Inquiry
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
    public int getTransmissionCount() throws IllegalStateException {

        return this.cntMsgXmissions;
    }
    
    
    //
    // IIngestionService Interface
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
    @AUnavailable(status=STATUS.ACCEPTED, note="Operation is mocked - Not implemented within the Ingestion Service until version 1.5.")
    public ProviderUID  registerProvider(ProviderRegistrar recRegistration) throws DpIngestionException {
        
        this.recProviderUid = ProviderRegistrationService.registerProvider(super.grpcConn, recRegistration);

        return this.recProviderUid;
    }
    
    /**
     * <p>
     * Blocking, synchronous, unary ingestion of the given ingestion frame.
     * </p>
     * This is the primary, and only, data ingestion operation of the <code>IIngestionService</code> interface.
     * The argument is converted to <code>IngestDataRequest</code> message(s) then transmitted to the Ingestion
     * Service one message at a time using a single,blocking RPC operation.  This is inherently a safe and robust
     * operation but at the cost of performance.  It is appropriate for small to moderate data transmission.
     * Clients requiring large data transmission or continuous and sustained transmission should consider the
     * <code>IIngestionStream</code> interface.  
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Clients must first register with the Ingestion Service before invoking this method otherwise
     * an exception is thrown.
     * </li>
     * <li>
     * A list of Ingestion Service response is returned in order to accommodate all the following cases.
     *   <ul>
     *   <li>Frame Decomposition: If the frame has allocation larger than the maximum it is decomposed into composite 
     *       frames.  Each composite frame is converted into a message that receives a response upon transmission.
     *       The returned list will contain an Ingestion Service response for each composite message.
     *   </li>
     *   <li>No Decomposition: If the frame has allocation less than the maximum limit a single response is
     *       contained in the list.
     *   </li>
     *   <li>
     *   If automatic frame-decomposition is disabled and the offered frame results in a gRPC message larger than
     *   the current gRPC message size limit an exception will be thrown (i.e., nothing is returned).
     *   </li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     * 
     * @param frame ingestion frame containing data for transmission to the Ingestion Service
     * 
     * @return  collection of Ingestion Service responses to data within ingestion frame
     * 
     * @throws IllegalStateException    unregistered Data Provider
     * @throws DpIngestionException     general ingestion exception (see detail and cause)
     */
    @Override
    public IngestionResult ingest(IngestionFrame frame) throws IllegalStateException, DpIngestionException {
        
        // Check registration
        if (this.recProviderUid == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Data Provider was not registered.");
        
        // Create list of frames to be ingested - depends on auto-decomposition
        List<IngestionFrame>    lstFrames = this.decomposeFrame(frame); // throws DpIngestionException
        
        // Save the request UIDs
        this.lstRequestIds.addAll(lstFrames.stream().map(frm -> frm.getClientRequestUid()).toList());
        
        // Convert frames to messages and transmit, recovering responses
        List<IngestDataResponse> lstRsps = this.transmitFrames(this.recProviderUid, lstFrames); // throws DpIngestionException

        // Convert responses to result and return
        IngestionResult     recResult;
        
        if (lstRsps.size() == 1)
            recResult = ProtoMsg.toIngestionResultUnary(lstRsps.get(0));
        else
            recResult = ProtoMsg.toIngestionResult(lstRsps);
        
        return recResult;
    }
    
    /**
     *
     * @see @see com.ospreydcs.dp.api.ingest.IIngestionService#getRequestIds()
     */
    @Override
    public List<ClientRequestUID>   getRequestIds() throws IllegalStateException {
        
        // Check registration
        if (this.recProviderUid == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Data Provider was not registered, no data transmitted.");
        
        return this.lstRequestIds;
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
     * Attempts to decompose the given ingestion frame horizontally (by columns).  
     * If horizontal decomposition fails a vertical decomposition is attempted (by rows).
     * If both attempts fail an exception is throw.
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
     * @throws DpIngestionException both horizontal and vertical decomposition failed
     */
    private List<IngestionFrame>    decomposeFrame(IngestionFrame frame) throws DpIngestionException {

        // Check if auto-decomposition is disabled
        if (!this.bolDecompAuto) {
            
            return  List.of(frame);
        }
        
        // Decompose the original ingestion frame (if too large - does nothing if allocation is less than limit)
        try {
            List<IngestionFrame>    lstFrames = this.prcrFrmDecomp.decomposeHorizontally(frame);
            
            return lstFrames;
        
            // The ingestion frame could not be decomposed horizontally - likely columns too large
        } catch (Exception e1) {
            String      strMsg1 = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - horizontal ingestion frame decomposition (by column) attempt FAILED: "
                    + e1.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg1);
            
            // Try decomposing vertically
            try {
                
                List<IngestionFrame>    lstFrames = this.prcrFrmDecomp.decomposeVertically(frame);
                
                return lstFrames;
                
                // Vertical decomposition failed - exceptional case
            } catch (Exception e2) {
                String      strMsg2 = JavaRuntime.getQualifiedMethodNameSimple() 
                        + " - both horizontal and vertical ingestion frame decomposition attempts FAILED: "
                        + e2.getMessage();

                if (BOL_LOGGING)
                    LOGGER.error(strMsg2);

                throw new DpIngestionException(strMsg2, e2);
            }
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
    private List<IngestDataResponse> transmitFrames(ProviderUID recUid, List<IngestionFrame> lstFrames) throws DpIngestionException {
        
        // Returned collection
        List<IngestDataResponse> lstRsps = new LinkedList<>();
        
        // For each ingestion frame transmit synchronously, in serial, using unary RPC
        for (IngestionFrame frm : lstFrames) {

            // Convert ingestion frame to ingest data message
            IngestDataRequest   msgRqst = this.prcrFrmCnvrtr.createRequest(frm, recUid);
            
            try {
                // Transmit data message and recover response - throws StatusRuntimeException
                IngestDataResponse msgRsp = super.grpcConn.getStubBlock().ingestData(msgRqst);
                
//                // Convert response to client API record - throws MissingResourceException if rejected
//                IngestionResponse  recRsp = ProtoMsg.toIngestionResponse(msgRsp);

                // Add to returned list
                lstRsps.add(msgRsp);
                this.cntMsgXmissions++;
                
                // Unexpected gRPC runtime exception during ingestion 
            } catch (io.grpc.StatusRuntimeException e) {
                String      strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                        + " - gRPC runtime exception during unary ingestData() operation: "
                        + e.getMessage();
                
                if (BOL_LOGGING)
                    LOGGER.error(strMsg);
                
                throw new DpIngestionException(strMsg, e);
                
                // The Ingestion Service rejected the ingestion request
            } catch (MissingResourceException e) {
                String      strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                        + " - Ingestion Service rejected unary ingestData() operation: "
                        + e.getMessage();
                
                if (BOL_LOGGING)
                    LOGGER.error(strMsg);
                
                throw new DpIngestionException(strMsg, e);
            }
        }
        
        return lstRsps;
    }
    
}
