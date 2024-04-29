/*
 * Project: dp-api-common
 * File:	DpServiceApiBase.java
 * Package: com.ospreydcs.dp.api.grpc.model
 * Type: 	DpServiceApiBase
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
 * @since Jan 6, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.model;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Abstract base class for Data Platform client API interfaces to Data Platform services
 * </p> 
 * <p>
 * Although this class is abundant with generic types it is relatively easy to use, so long as all the
 * types are correct.  It then takes care of all the boiler plate requirements of a gRPC connection to
 * the desired Data Platform service.
 * </p>
 * <p>
 * Supports the following basic requirements for a gRPC interface (identified by <code>Service</code>) 
 * to a Data Platform service:
 * <ul>
 * <li>
 * Maintains the <code>{@link DpGrpcConnection}</code> object for access to RPC operations in the 
 * communications stubs of Protobuf generated gRPC interface <code>Service</code>.
 * </li>
 * <br/>
 * <li>
 * Provides the required gRPC connection operations required from the <code>{@link IConnection}</code>
 * interface.  Note that derived class must implement <code>{@link IConnection#awaitTermination()}</code>.
 * </li>
 * </ul>
 * </p> 
 * <p>
 * <h2>NOTES:</h2>
 * All generic type parameter must be consistent.  Specifically, 
 * <ul>
 * <li>The <code>ServiceApi</code> must support the <code>ServiceGrpc</code> interface.</li>
 * <li>All stub types must belong to the <code>ServiceGrpc</code> type (i.e., they are enclosed classes).</li>
 * <li>The <code>Connection</code> must support to the <code>ServiceApi</code> type.</li>
 * </ul>
 * </p>
 * <p>
 * <h2>WARNING:</h2>
 * All derived Data Platform service API interfaces should be shut down when no longer needed using
 * <code>{@link #shutdownSoft()} or <code>{@link #shutdownNow()}</code>.  Otherwise gRPC resources are not
 * released and performance degrades.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Jan 6, 2024
 *
 * @param <API>         Data Platform service API derived class (sub-type of <code>DpServiceApiBase</code>)
 * @param <Connection>  gRPC Connection type to Data Platform service (sub-type of <code>DpGrpcConnection</code>)
 * @param <ServiceGrpc> Protocol Buffers generated gRPC service class
 * @param <BlockStub>   Protocol Buffer communication stub containing blocking, synchronous RPC operations
 * @param <FutureStub>  Protocol Buffer communication stub containing non-blocking (future) RPC operations
 * @param <AsyncStub>   Protocol Buffer communication stub containing asynchronous streaming RPC operations
 *
 */
public abstract class DpServiceApiBase<
    API extends DpServiceApiBase<API, Connection, ServiceGrpc, BlockStub, FutureStub, AsyncStub>,
    Connection extends DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub>,
    ServiceGrpc,
    BlockStub extends io.grpc.stub.AbstractBlockingStub<BlockStub>, 
    FutureStub extends io.grpc.stub.AbstractFutureStub<FutureStub>,
    AsyncStub extends io.grpc.stub.AbstractAsyncStub<AsyncStub> 
    > implements IConnection
{

    //
    // Attributes
    //
    
//    /** the gRPC channel connection to the desired DP Query Service */ 
//    protected final DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub>     grpcConn;

    /** the gRPC channel connection to the desired DP Query Service */ 
    protected final Connection     grpcConn;
    
//    abstract protected API createFrom(Connection conn);
    
    /**
     * <p>
     * Constructs a new instance of <code>DpServiceApiBase</code>.
     * </p>
     * <p>
     * The argument should be obtained from an appropriately typed connection factory
     * <code>{@link DpGrpcConnectionFactory}</code>.
     * </p>
     * 
     * @param conQuery  pre-configured gRPC channel connection to the desired DP service 
     * 
     * @see DpGrpcConnectionFactory
     */
//    protected DpServiceApiBase(DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> conn) {
    protected DpServiceApiBase(Connection conn) {
        this.grpcConn = conn;
    }

    
    //
    // IConnection Interface
    //
    
    /**
     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#shutdownSoft()
     */
    @Override
    public boolean shutdownSoft() throws InterruptedException {
        return this.grpcConn.shutdownSoft();
    }

    /**
     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#shutdownNow()
     */
    @Override
    public boolean shutdownNow() {
        return this.grpcConn.shutdownNow();
    }

    /**
     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public boolean awaitTermination(long cntTimeout, TimeUnit tuTimeout) throws InterruptedException {
        return this.grpcConn.awaitTermination(cntTimeout, tuTimeout);
    }

//    /**
//     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination()
//     */
//    @Override
//    public boolean awaitTermination() throws InterruptedException {
//        return this.conQuery.awaitTermination();
//    }

    /**
     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#isShutdown()
     */
    @Override
    public boolean isShutdown() {
        return this.grpcConn.isShutdown();
    }

    /**
     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#isTerminated()
     */
    @Override
    public boolean isTerminated() {
        return this.grpcConn.isTerminated();
    }


}
