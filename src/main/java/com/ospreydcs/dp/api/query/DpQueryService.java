/*
 * Project: dp-api-common
 * File:	DpQueryService.java
 * Package: com.ospreydcs.dp.api.query
 * Type: 	DpQueryService
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
 * @since Jan 5, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiBase;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.api.model.AUnavailable;
import com.ospreydcs.dp.api.model.AUnavailable.STATUS;
import com.ospreydcs.dp.api.query.model.DpQueryException;
import com.ospreydcs.dp.api.query.model.DpQueryStreamBuffer;
import com.ospreydcs.dp.api.query.model.DpQueryStreamQueueBufferDeprecated;
import com.ospreydcs.dp.api.query.model.DpQueryStreamBuffer.StreamType;
import com.ospreydcs.dp.api.query.model.data.DataTable;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

/**
 * <p>
 * Basic Data Platform Query Service interface.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 5, 2024
 *
 */
public final class DpQueryService extends DpServiceApiBase<DpQueryService, DpQueryConnection, DpQueryServiceGrpc, DpQueryServiceBlockingStub, DpQueryServiceFutureStub, DpQueryServiceStub> {

    
    //
    // Application Resources
    //
    
    /** Default Query Service configuration parameters */
    private static final DpQueryConfig  CFG_DEFAULT = DpApiConfig.getInstance().query;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger LOGGER = LogManager.getLogger();
    
    
    //
    // Attributes
    //
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>DpQueryService</code> attached to the given connection.
     * </p>
     * <p>
     * The argument should be obtained from the appropriate connection factory,
     * specifically, <code>{@link DpQueryConnectionFactory}</code>.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * The returned object should be shut down when no longer needed using 
     * <code>{@link #shutdownSoft()} or <code>{@link #shutdownNow()}</code>.  
     * This action is necessary to release unused gRPC resources and maintain 
     * overall performance.  
     * </p>
     * 
     * @param connQuery  the gRPC channel connection to the desired DP Query Service
     *  
     * @return new <code>DpQueryService</code> interfaces attached to the argument
     */
    public static DpQueryService from(DpQueryConnection connQuery) {
        return new DpQueryService(connQuery);
    }
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpQueryService</code>.
     * </p>
     * <p>
     * The argument should be obtained from the appropriate connection factory,
     * specifically, <code>{@link DpQueryConnectionFactory}</code>.
     * </p>
     * 
     * @param connQuery  the gRPC channel connection to the desired DP Query Service 
     * 
     * @see DpQueryConnectionFactory
     */
    public DpQueryService(DpQueryConnection connQuery) {
        super(connQuery);
    }


    //
    // IConnection Interface
    //
    
    /**
     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination()
     */
    @Override
    public boolean awaitTermination() throws InterruptedException {
        return this.getConnection().awaitTermination();
    }

    
    //
    // Query Operations
    //
    
    /**
     * <p>
     * Performs a unary data request to the Query Service.
     * </p>
     * <p>
     * The method performs a blocking unary request to the Query Service and does not return until
     * the result set is recovered and used to fully populate the returned data table.
     * </p>
     * <p>
     * <h2>WARNING</h2>
     * The unary request is such that the entire result set must be contain in a single gRPC message,
     * which is size limited.  If the result set of the request is larger than the current gRPC message
     * size limit, the result is truncated.
     * </p> 
     * 
     * @param rqst  an initialized <code>{@link DpDataRequest]</code> request builder instance
     * 
     * @return      fully populated (static) data table.
     */
    @AUnavailable(status=STATUS.ACCEPTED, note="The request is performed but the returned table is empty")
    public DataTable querySingle(DpDataRequest rqst) {
        QueryRequest qry = rqst.buildQueryRequest();
        
        QueryResponse msgRsp = super.grpcConn.getStubBlock().queryResponseSingle(qry);
        
        return new DataTable();
    }
    
    /**
     * <p>
     * Perform a Query Service data query that returns a dynamic stream buffer accumulating the result set.
     * </p>
     * <p>
     * This operation creates a unidirectional (backward) stream from the Query Service to the returned stream
     * buffer instance.  Unidirectional streams are potentially faster but less stable.  
     * Results of the query are accessible via the stream buffer as they are available.
     * </p>
     * <p>
     * The data stream is initiated with the <code>{@link DpQueryStreamBuffer#start()</code> method.
     * Query stream observers implementing the <code>{@link IDpQueryStreamObserver}</code> interface
     * can register with the returned object to receive callback notifications for data and stream events
     * using the <code>{@link DpQueryStreamBuffer#addStreamObserver(com.ospreydcs.dp.api.query.model.IDpQueryStreamObserver)</code>
     * method.
     * </p>
     * 
     * @param rqst  an initialized <code>{@link DpDataRequest]</code> request builder instance
     * 
     * @return      an active query stream buffer ready to accumulate the results set
     * 
     * @see DpQueryStreamBuffer
     * @see DpQueryStreamBuffer#start()
     */
    public DpQueryStreamBuffer   queryStreamUni(DpDataRequest rqst) {
        
        // Extract the Protobuf request message
        QueryRequest    msgRequest = rqst.buildQueryRequest();
        
        // Create the query stream buffer and return it
        DpQueryStreamBuffer buf = DpQueryStreamBuffer.newBuffer(
                StreamType.UNIDIRECTIONAL, 
                this.getConnection().getStubAsync(),
                msgRequest,
                CFG_DEFAULT.logging.active);
        
        return buf;
    }
    
//    /**
//     * <p>
//     * Perform a Query Service data query that returns a dynamic stream buffer accumulating the result set.
//     * </p>
//     * <p>
//     * This operation creates a unidirectional (backward) stream from the Query Service to the returned stream
//     * buffer instance.  Unidirectional streams are typically faster but potentially less stable.   
//     * Results of the query are accessible via the stream buffer as they are available.
//     * </p>
//     * <p>
//     * Explicit timeout limits are given for Query Service responses and operations.
//     * </p>
//     * 
//     * @param rqst          an initialized <code>{@link DpDataRequest]</code> request builder instance
//     * @param cntTimeout    time limit for Query Service timeout limit
//     * @param tuTimeout     time units for Query Service timeout limit
//     * 
//     * @return  an active query stream buffer currently accumulating the result set
//     * 
//     * @throws DpQueryException Query Service exception - typically results from a malformed request (see message and cause)
//     * 
//     * @see DpQueryStreamQueueBufferDeprecated
//     */
//    public DpQueryStreamQueueBufferDeprecated  queryUniStream(DpDataRequest rqst, long cntTimeout, TimeUnit tuTimeout) throws DpQueryException {
//        
//        // Create the query stream buffer
//        DpQueryStreamQueueBufferDeprecated bufStr = DpQueryStreamQueueBufferDeprecated.from(super.grpcConn.getStubAsync(), cntTimeout, tuTimeout);
//                
//        // Get the query request message
//        QueryRequest    msgRqst = rqst.buildQueryRequest();
//                
//        // Initiate stream and return it
//        try {
//            bufStr.startUniStream(msgRqst);
//            
//        } catch (IllegalStateException e) {
//            String strMsg = JavaRuntime.getQualifiedCallerName() + ": IllegalStateException thrown (this should not occur) - " + e.getMessage();
//            
//            LOGGER.error(strMsg);
//            
//            throw new DpQueryException(strMsg, e);
//            
//        } catch (IllegalArgumentException e) {
//            String strMsg = JavaRuntime.getQualifiedCallerName() + ": IllegalArgumentException (malformed QueryRequest) - " + e.getMessage();
//            
//            LOGGER.error(strMsg);
//            
//            throw new DpQueryException(strMsg, e);
//        }
//        
//        return bufStr;
//    }
    
    /**
     * <p>
     * Perform a Query Service data query that returns a dynamic stream buffer accumulating the result set.
     * </p>
     * <p>
     * This operation creates a bidirectional stream from the Query Service to the returned stream
     * buffer instance.  Bidirectional streams are typically more stable but potentially slower.  
     * Results of the query are accessible via the stream buffer as they are available.
     * </p>
     * <p>
     * The data stream is initiated with the <code>{@link DpQueryStreamBuffer#start()</code> method.
     * Query stream observers implementing the <code>{@link IDpQueryStreamObserver}</code> interface
     * can register with the returned object to receive callback notifications for data and stream events
     * using the <code>{@link DpQueryStreamBuffer#addStreamObserver(com.ospreydcs.dp.api.query.model.IDpQueryStreamObserver)</code>
     * method.
     * </p>
     * 
     * @param rqst          an initialized <code>{@link DpDataRequest]</code> request builder instance
     * 
     * @return  an active query stream buffer ready to accumulate the result set
     * 
     * @throws DpQueryException Query Service exception - typically results from a malformed request (see message and cause)
     * 
     * @see DpQueryStreamBuffer
     * @see DpQueryStreamBuffer#start()
     */
    public DpQueryStreamBuffer queryStreamBidi(DpDataRequest rqst) {
        
        // Extract the Protobuf request message
        QueryRequest    msgRequest = rqst.buildQueryRequest();
        
        // Create the query stream buffer and return it
        DpQueryStreamBuffer buf = DpQueryStreamBuffer.newBuffer(
                StreamType.BIDIRECTIONAL, 
                this.getConnection().getStubAsync(),
                msgRequest,
                CFG_DEFAULT.logging.active);
        
        return buf;
    }
    
//    /**
//     * <p>
//     * Perform a Query Service data query that returns a dynamic stream buffer accumulating the result set.
//     * </p>
//     * <p>
//     * This operation creates a bidirectional stream from the Query Service to the returned stream
//     * buffer instance.  Bidirectional streams are more stable but potentially slower.  
//     * Results of the query are accessible via the stream buffer as they are available.
//     * </p>
//     * <p>
//     * Explicit timeout limits are given for Query Service responses and operations.
//     * </p>
//     * 
//     * @param rqst          an initialized <code>{@link DpDataRequest]</code> request builder instance
//     * @param cntTimeout    time limit for Query Service timeout limit
//     * @param tuTimeout     time units for Query Service timeout limit
//     * 
//     * @return  an active query stream buffer currently accumulating the result set
//     * 
//     * @throws DpQueryException Query Service exception - typically results from a malformed request (see message and cause)
//     * 
//     * @see DpQueryStreamQueueBufferDeprecated
//     */
//    public DpQueryStreamQueueBufferDeprecated  queryBidiStream(DpDataRequest rqst, long cntTimeout, TimeUnit tuTimeout) throws DpQueryException {
//        
//        // Create the query stream buffer
//        DpQueryStreamQueueBufferDeprecated bufStr = DpQueryStreamQueueBufferDeprecated.from(super.grpcConn.getStubAsync(), cntTimeout, tuTimeout);
//                
//        // Get the query request message
//        QueryRequest    msgRqst = rqst.buildQueryRequest();
//                
//        // Initiate stream and return it
//        try {
//            bufStr.startBidiStream(msgRqst);
//            
//        } catch (IllegalStateException e) {
//            String strMsg = JavaRuntime.getQualifiedCallerName() + ": IllegalStateException thrown (this should not occur) - " + e.getMessage();
//            
//            LOGGER.error(strMsg);
//            
//            throw new DpQueryException(strMsg, e);
//            
//        } catch (IllegalArgumentException e) {
//            String strMsg = JavaRuntime.getQualifiedCallerName() + ": IllegalArgumentException (malformed QueryRequest) - " + e.getMessage();
//            
//            LOGGER.error(strMsg);
//            
//            throw new DpQueryException(strMsg, e);
//        }
//        
//        return bufStr;
//    }
    
    
    //
    // Support Methods
    //
    
    /**
     * Returns the <code>DpGrpcConnection</code> in super class cast to a
     * <code>DpQueryConnection</code> instance.
     * 
     * @return connection instances as a <code>DpQueryConnection</code> object
     */
    private DpQueryConnection getConnection() {
        return super.grpcConn;
    }
    
}
