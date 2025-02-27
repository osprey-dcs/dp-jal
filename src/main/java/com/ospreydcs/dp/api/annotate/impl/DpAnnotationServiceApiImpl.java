/*
 * Project: dp-api-common
 * File:	DpAnnotationServiceApiImpl.java
 * Package: com.ospreydcs.dp.api.annotate.impl
 * Type: 	DpAnnotationServiceApiImpl
 *
 * Copyright 2010-2025 the original author or authors.
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
 * @since Feb 27, 2025
 *
 */
package com.ospreydcs.dp.api.annotate.impl;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.annotate.DpAnnotationException;
import com.ospreydcs.dp.api.annotate.DpDataset;
import com.ospreydcs.dp.api.annotate.IAnnotationService;
import com.ospreydcs.dp.api.common.DatasetUID;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.annotate.DpAnnotationConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnection;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiBase;
import com.ospreydcs.dp.api.grpc.model.IConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl;
import com.ospreydcs.dp.api.query.impl.QueryRequestProcessor;
import com.ospreydcs.dp.api.query.model.correl.QueryDataCorrelator;
import com.ospreydcs.dp.grpc.v1.annotation.CreateDataSetRequest;
import com.ospreydcs.dp.grpc.v1.annotation.CreateDataSetResponse;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc.DpAnnotationServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc.DpAnnotationServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc.DpAnnotationServiceStub;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult;

/**
 *
 * @author Christopher K. Allen
 * @since Feb 27, 2025
 *
 */
public class DpAnnotationServiceApiImpl extends
              DpServiceApiBase<DpAnnotationServiceApiImpl, 
                               DpAnnotationConnection, 
                               DpAnnotationServiceGrpc, 
                               DpAnnotationServiceBlockingStub, 
                               DpAnnotationServiceFutureStub, 
                               DpAnnotationServiceStub> 
    implements IAnnotationService, IConnection { 


    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>DpAnnotationServiceApiImpl</code> attached to the given connection.
     * </p>
     * <p>
     * This method is available primarily for unit testing.  Java API Library clients should generally obtain
     * implementations of <code>IAnnotationService</code> from the Annotation Service connection factory.
     * </p> 
     * <p>
     * The argument should be obtained from the appropriate connection factory,
     * specifically, <code>{@link DpAnnotationConnectionFactory}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This object takes ownership of the given Annotation Service connection.  Do not attempt to shut down 
     * the connection externally while using this instance.  Use the methods described below.
     * </li>
     * <li>
     * The returned object should be shut down when no longer needed using 
     * <code>{@link #shutdown()}</code> or <code>{@link #shutdownNow()}</code>.  
     * This action is necessary to release unused gRPC resources and maintain 
     * overall performance.
     * </li>  
     * </ul>
     * </p>
     * 
     * @param connAnnot  the gRPC channel connection to the desired DP Annotation Service
     *  
     * @return new <code>DpAnnotationServiceApiImpl</code> interfaces attached to the argument
     */
    public static DpAnnotationServiceApiImpl from(DpAnnotationConnection connAnnot) {
        return new DpAnnotationServiceApiImpl(connAnnot);
    }
    
    
    //
    // Application Resources
    //
    
    /** Default Annotation Service API configuration parameters */
    private static final DpAnnotationConfig  CFG_DEF = DpApiConfig.getInstance().annotation;
    
    
    //
    // Class Constants
    //
    
    /** Logging active flag */
    private static final boolean        BOL_LOGGING = CFG_DEF.logging.active;
    
    
    /** General query timeout limit */
    private static final long           LNG_TIMEOUT = CFG_DEF.timeout.limit;
    
    /** General query timeout units */
    private static final TimeUnit       TU_TIMEOUT = CFG_DEF.timeout.unit;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger         LOGGER = LogManager.getLogger();
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpAnnotationServiceApiImpl</code>.
     * </p>
     * <p>
     * The argument should be obtained from the appropriate connection factory,
     * specifically, <code>{@link DpAnnotationConnectionFactory}</code>.
     * </p>
     * <ul>
     * <li>
     * This object takes ownership of the given Annotation Service connection.  
     * Do not attempt to shut down the connection externally while using this instance.
     * </li>
     * <li>
     * The returned object should be shut down when no longer needed using 
     * <code>{@link #shutdown()}</code> or <code>{@link #shutdownNow()}</code>.  
     * This action is necessary to release unused gRPC resources and maintain 
     * overall performance.
     * </li>  
     * </p>
     * 
     * @param connAnnot  the gRPC channel connection to the desired DP Query Service 
     * 
     * @see DpAnnotationConnectionFactory
     */
    protected DpAnnotationServiceApiImpl(DpAnnotationConnection connAnnot) {
        super(connAnnot);
    }
    

    //
    // IConnection Interface
    //
    
    /**
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#shutdown()
     */
    @Override
    public boolean shutdown() throws InterruptedException {
        return super.shutdown();
    }

    /**
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#shutdownNow()
     */
    @Override
    public boolean shutdownNow() {
        return super.shutdownNow();
    }

    /**
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#awaitTermination(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public boolean awaitTermination(long cntTimeout, TimeUnit tuTimeout) throws InterruptedException {
        return super.awaitTermination(cntTimeout, tuTimeout);
    }

    /**
     * @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination()
     */
    @Override
    public boolean awaitTermination() throws InterruptedException {
        return super.awaitTermination(LNG_TIMEOUT, TU_TIMEOUT);
    }

    /**
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#isShutdown()
     */
    @Override
    public boolean isShutdown() {
        return super.isShutdown();
    }

    /**
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#isTerminated()
     */
    @Override
    public boolean isTerminated() {
        return super.isTerminated();
    }

    
    //
    // IAnnotationService Interface
    //
    
    /**
     * @see com.ospreydcs.dp.api.annotate.IAnnotationService#createDataset(com.ospreydcs.dp.api.annotate.DpDataset)
     */
    @Override
    public DatasetUID createDataset(DpDataset rqst) throws DpAnnotationException {
        
        // Perform new data set request directly with the Annotation Service API blocking stub
        CreateDataSetRequest    msgRqst = rqst.buildDataSetRequest();
        CreateDataSetResponse   msgRsp = super.grpcConn.getStubBlock().createDataSet(msgRqst);
        
        // Check for any exception reported by the Annotation Service
        if (msgRsp.hasExceptionalResult()) {
            ExceptionalResult   msgErr = msgRsp.getExceptionalResult();
            String              strMsg = ProtoMsg.exceptionMessage(msgErr, "Annotation Service");
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpAnnotationException(strMsg);
        }
        
        // Extract data set UID and return it
        CreateDataSetResponse.CreateDataSetResult   msgResult = msgRsp.getCreateDataSetResult();
        
        String      strUid = msgResult.getDataSetId();
        DatasetUID  recUid = DatasetUID.from(strUid);
        
        return recUid;
    }

}
