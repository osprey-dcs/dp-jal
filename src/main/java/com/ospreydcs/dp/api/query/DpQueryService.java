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

import com.ospreydcs.dp.api.grpc.model.DpServiceApiBase;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;

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
    // Attributes
    //
    
//    /** the gRPC channel connection to the desired DP Query Service */ 
//    private final DpQueryConnection     conQuery;

    
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
     * @param conQuery  the gRPC channel connection to the desired DP Query Service
     *  
     * @return new <code>DpQueryService</code> interfaces attached to the argument
     */
    public static DpQueryService from(DpQueryConnection conQuery) {
        return new DpQueryService(conQuery);
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
     * @param conQuery  the gRPC channel connection to the desired DP Query Service 
     * 
     * @see DpQueryConnectionFactory
     */
    public DpQueryService(DpQueryConnection conQuery) {
        super(conQuery);
    }


    //
    // IConnection Interface
    //
    
//    /**
//     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#shutdownSoft()
//     */
//    @Override
//    public boolean shutdownSoft() {
//        return this.conQuery.shutdownSoft();
//    }
//
//    /**
//     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#shutdownNow()
//     */
//    @Override
//    public boolean shutdownNow() {
//        return this.conQuery.shutdownNow();
//    }
//
//    /**
//     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination(long, java.util.concurrent.TimeUnit)
//     */
//    @Override
//    public boolean awaitTermination(long cntTimeout, TimeUnit tuTimeout) throws InterruptedException {
//        return this.conQuery.awaitTermination(cntTimeout, tuTimeout);
//    }

    /**
     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination()
     */
    @Override
    public boolean awaitTermination() throws InterruptedException {
        return this.getConnection().awaitTermination();
    }

//    /**
//     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#isShutdown()
//     */
//    @Override
//    public boolean isShutdown() {
//        return this.conQuery.isShutdown();
//    }
//
//    /**
//     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#isTerminated()
//     */
//    @Override
//    public boolean isTerminated() {
//        return this.conQuery.isTerminated();
//    }
//
    
    //
    // Query Operations
    //
    
    
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
        return (DpQueryConnection)super.grpcConn;
    }
    
}
