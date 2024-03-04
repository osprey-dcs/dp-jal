/*
 * Project: dp-api-common
 * File:	DpGrpcConnectionFactory.java
 * Package: com.ospreydcs.dp.api.grpc
 * Type: 	DpGrpcConnectionFactory
 *
 * Copyright 2010-2022 the original author or authors.
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
 * @since Nov 16, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.model;

import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;

/**
 * <p>
 * Factory class for creating <code>DpGrpcConnection</code> instances.
 * </p>
 * <p>
 * Creates <code>DsGrpcConnection</code> instances supporting the Protocol Buffers connection
 * stubs specified by the generic parameter types.  Various configuration 
 * combinations are available which mix user-supplied gRPC connection parameters
 * and default connection parameters supplied by the <code>DpGrpcConnectionConfig<code> instance
 * at creation.
 * </p>
 * <p>
 * This implementation is provided to allow for the creation of factory class objects that 
 * create raw <code>DpGrpcConnection</code> instances (i.e., the generic types are left unbound).
 * The base class does all the work, see <code>{@link DpGrpcConnectionFactoryBase}</code>.
 * This class basically absorbs the <code>Connection</code> type of the base class.
 * </p>
 * <p>
 * The types of the Java generics parameters determine the Data Platform service for connection.
 * For example the Ingestion Service or the Query Service may be supported with the following
 * bindings:
 * <ul>
 * Ingestion Service Connections
 * <li><code>ServiceGrpc</code> = <code>IngestionServiceGrpc</code>
 *     </li>
 * <li><code>SyncStub</code> = <code>IngestionServiceGrpc.IngestionServiceBlockingStub</code>
 *     </li>
 * <li><code>FutureStub</code> = <code>IngestionServiceGrpc.IngestionServiceFutureStub</code>
 *     </li>
 * <li><code>AsyncStub</code> = <code>IngestionServiceGrpc.IngestionServiceStub</code>
 *     </li> 
 *     <br/>
 * Query Service Connections
 * <li><code>ServiceGrpc</code> = <code>QueryServiceGrpc</code>
 *     </li>
 * <li><code>SyncStub</code> = <code>QueryServiceGrpc.QueryServiceBlockingStub</code>
 *     </li>
 * <li><code>FutureStub</code> = <code>QueryServiceGrpc.QueryServiceFutureStub</code>
 *     </li>
 * <li><code>AsyncStub</code> = <code>QueryServiceGrpc.QueryServiceStub</code>
 *     </li> 
 * </ul>
 * Thus, for example, the following statement creates a connection factory <code>facQueryConn</code> 
 * instance for the Data Platform Query Service:
 * <pre>
 * private static final DpGrpcConnectionFactory&lt;
 *                       DpQueryServiceGrpc, 
 *                       DpQueryServiceBlockingStub,
 *                       DpQueryServiceFutureStub, 
 *                       DpQueryServiceStub&gt; facQueryConnec = 
 *                           DpGrpcConnectionFactory.newFactory(
 *                                DpQueryServiceGrpc.class, 
 *                                DpApiConfig.getInstance()
 *                                             .services
 *                                             .query
 *                                );
 * </pre>
 * Since only one factory class instance is required to create all
 * Data Platform query service connections, the instance may be 
 * declared <code>static final</code>.
 * </p>
 * <p>
 * <h2>Java Generics</h2>
 * Due to the use of Java generic parameters, the connection factories 
 * cannot be static classes, they must be instantiated.  However, the advantage
 * is that only one class factory type needs to be implemented which will
 * create connections to a Data Platform service.  Thus, the use of generic templates
 * consolidates the code for creating Data Platform gRPC connections. 
 * </p>
 * <p>
 * <h2>Fly Weight Connection Factories</h2>
 * Connection factory instances can be used within fly weight connection factory classes
 * that bind the generic parameters for a specific Data Platform service.
 * An instance is maintained within the fly weight for creating the connection objects required by the 
 * targeted services.
 * </p>
 * 
 * @param <ServiceGrpc> the specific Data Platform service, that is, the Protobuf-generated gRPC service class  
 * @param <BlockStub>   synchronous (blocking) stub supporting unary gPRC communications 
 * @param <FutureStub>  non-blocking stub supporting unary gPRC communications 
 * @param <AsyncStub>   asynchronous (non-blocking) stub for both unary and streaming gRPC communications 
 *
 * @author Christopher K. Allen
 * @since Nov 16, 2022
 * @version Jan 14, 2024
 *
 * @see DpGrpcConnection
 * @see DpGrpcConnectionFactoryBase
 */
public class DpGrpcConnectionFactory<
    ServiceGrpc,
    BlockStub extends io.grpc.stub.AbstractBlockingStub<BlockStub>, 
    FutureStub extends io.grpc.stub.AbstractFutureStub<FutureStub>,
    AsyncStub extends io.grpc.stub.AbstractAsyncStub<AsyncStub> 
    > 
    extends DpGrpcConnectionFactoryBase<
        DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub>,
        ServiceGrpc,
        BlockStub,
        FutureStub,
        AsyncStub
        >
{

    //
    // DpGrpcConnectionFactoryBase Requirements
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpGrpcConnectionFactory</code>.
     * </p>
     *
     * @param clsService    the class type of the Protobuf-generated gRPC service 
     * @param cfgConn       default parameter set to use in connect(...) methods
     */
    protected DpGrpcConnectionFactory(Class<ServiceGrpc> clsService, DpGrpcConnectionConfig cfgConn) {
        super(clsService, cfgConn);
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactoryBase#createFrom(com.ospreydcs.dp.api.grpc.model.DpGrpcConnection)
     */
    @Override
    protected DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> createFrom(
            DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> conn) throws DpGrpcException {
        return conn;
    }


    //
    // Creator
    //

    /**
     * <p>
     * Creates a new <code>DpGrpcConnectionFactory</code> instance.
     * </p>
     * <p>
     * The returned factory is capable of creating <code>DsGrpcConnection</code>
     * instances for connection with the Data Platform service targeted by the generic parameters.  
     * The type of communications object returned is also dictated by the Java template parameters.  
     * </p>
     * <p>
     * The returned connection factory is fully configured for Data Platform service connection.
     * The default gRPC connection parameters are provided in the argument.
     * </p>
     * 
     * @param <ServiceGrpc> type of the Protobuf-generated gRPC service interface 
     * @param <BlockStub>   type for synchronous blocking communications stub 
     * @param <FutureStub>  type for non-blocking unary communications stub
     * @param <AsyncStub>   type for asynchronous communications stub (contains streaming RPC operations)
     *
     * @param clsService    the class type of the supported Data Platform service
     * @param cfgConn       connections properties record containing the default parameters
     * 
     * @return a new <code>DpGrpcConnectionFactory</code> instance
     */
    public static <ServiceGrpc, 
        BlockStub extends io.grpc.stub.AbstractBlockingStub<BlockStub>, 
        FutureStub extends io.grpc.stub.AbstractFutureStub<FutureStub>, 
        AsyncStub extends io.grpc.stub.AbstractAsyncStub<AsyncStub>
        > 
    DpGrpcConnectionFactory<ServiceGrpc, BlockStub, FutureStub, AsyncStub>  newFactory(Class<ServiceGrpc> clsService, 
            DpGrpcConnectionConfig cfgConn
            ) 
    {
        return new DpGrpcConnectionFactory<ServiceGrpc, BlockStub, FutureStub, AsyncStub>(clsService, cfgConn);        
    }
}
