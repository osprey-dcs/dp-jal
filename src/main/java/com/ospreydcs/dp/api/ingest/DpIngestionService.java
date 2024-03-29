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
package com.ospreydcs.dp.api.ingest;

import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiBase;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.model.AUnavailable;
import com.ospreydcs.dp.api.model.AUnavailable.STATUS;
import com.ospreydcs.dp.api.model.ProviderRegistrar;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult.ExceptionalResultStatus;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceStub;
import com.ospreydcs.dp.grpc.v1.ingestion.RegisterProviderRequest;
import com.ospreydcs.dp.grpc.v1.ingestion.RegisterProviderResponse;
import com.ospreydcs.dp.grpc.v1.ingestion.RegisterProviderResponse.RegistrationResult;

/**
 * <p>
 * <h1>Data Platform Ingestion Service Application Programming Interface (API).</h1>
 * </p>
 * <p>
 * This class is the primary access point for Ingestion Service clients.  The class exposes all
 * the fundamental operations of the Ingestion Service without details of the underlying gRPC
 * interface.
 * </p>
 * <p>
 *
 * @author Christopher K. Allen
 * @since Mar 28, 2024
 *
 */
public final class DpIngestionService extends
        DpServiceApiBase<DpIngestionService, DpIngestionConnection, DpIngestionServiceGrpc, DpIngestionServiceBlockingStub, DpIngestionServiceFutureStub, DpIngestionServiceStub> {

    //
    // Application Resources
    //
    
    /** Default Query Service configuration parameters */
    private static final DpIngestionConfig  CFG_DEFAULT = DpApiConfig.getInstance().ingest;
    
    
    //
    // Class Constants
    //
    
    /** Logging active flag */
    private static final boolean        BOL_LOGGING = CFG_DEFAULT.logging.active;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger LOGGER = LogManager.getLogger();
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>DpIngestionService</code> attached to the given connection.
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
     * @return new <code>DpIngestionService</code> interfaces attached to the argument
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
     * 
     * @param connQuery  the gRPC channel connection to the desired DP Query Service 
     * 
     * @see DpIngestionConnectionFactory
     *
     * @param conn
     */
    public DpIngestionService(DpIngestionConnection conn) {
        super(conn);
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
    @AUnavailable(status=STATUS.ACCEPTED, note="Not fully implemented within the Ingestion Service")
    public ProviderUID  registerProvider(ProviderRegistrar recRegistration) throws DpIngestionException {
        
        // Create the Protobuf request message from the argument 
        RegisterProviderRequest     msgRqst = RegisterProviderRequest.newBuilder()
                .setProviderName(recRegistration.name())
                .addAllAttributes(ProtoMsg.createAttributes(recRegistration.attributes()))
                .setRequestTime(ProtoMsg.from(Instant.now()))
                .build();
        
        // Perform the registration request
        RegisterProviderResponse    msgRsp;
        
        try {
            // Attempt blocking unary RPC call 
            msgRsp = super.grpcConn.getStubBlock().registerProvider(msgRqst);
            
        } catch (io.grpc.StatusRuntimeException e) {
            String  strMsg = JavaRuntime.getQualifiedCallerNameSimple()
                           + " - gRPC threw runtime exception attempting to register provider: "
                           + "type=" + e.getClass().getName()
                           + ", details=" + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpIngestionException(strMsg, e);
        }
        
        // Exception checking
        if (msgRsp.hasExceptionalResult()) {
            ExceptionalResult       msgExcept = msgRsp.getExceptionalResult();
            ExceptionalResultStatus enmStatus = msgExcept.getExceptionalResultStatus();
            String                  strDetails = msgExcept.getMessage();
            
            String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() 
                            + " - Provider registration failed: " 
                            + " status=" + enmStatus
                            + ", details=" + strDetails;
            
            // Log exception if logging
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpIngestionException(strMsg);
        }
        
        // Extract the provider UID and create return value
        RegistrationResult  msgResult = msgRsp.getRegistrationResult();
        int                 intUid = msgResult.getProviderId();
        
        return new ProviderUID(intUid);
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * Returns the <code>DpGrpcConnection</code> in super class cast to a
     * <code>DpIngestionConnection</code> instance.
     * 
     * @return connection instances as a <code>DpIngestionConnection</code> object
     */
    private DpIngestionConnection getConnection() {
        return super.grpcConn;
    }
    
}
