/*
 * Project: dp-api-common
 * File:	DpQueryConnection.java
 * Package: com.ospreydcs.dp.api.grpc.query
 * Type: 	DpQueryConnection
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
 * @since Dec 28, 2023
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.query;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnection;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;

import io.grpc.ManagedChannel;

/**
 * <p>
 * Fly weight binding generic <code>DpGrpcConnection</code> to the Data Platform Query Service.  
 * </p>
 * <p>
 * Concrete class supplies type bindings for <code>DpGrpcConnection</code> to provide connections to
 * the <code>DpQueryServiceGrpc</code> Protobuf interface and communications stubs.
 * Convenience constructors and creators are supplied to make instances from the <code>DpGrpcConnection</code>
 * base class and also gRPC managed channels.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 28, 2023
 *
 */
public class DpQueryConnection extends DpGrpcConnection<DpQueryServiceGrpc, DpQueryServiceBlockingStub, DpQueryServiceFutureStub, DpQueryServiceStub> {

    
    //
    // Class Resources
    //
    
    /** The Query Service default connection parameters */
    public static final DpGrpcConnectionConfig        CFG_DEFAULT = DpApiConfig.getInstance().connections.query;
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new <code>DpQueryConnection</code> instance supported by the given gRPC channel.
     * </p>
     * 
     * @param grpcChan  gRPC channel instance supporting all communications with the Query Service
     * 
     * @return new <code>DpQueryConnection</code> using gRPC channel for Query Service connection
     *
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     */
    public static DpQueryConnection from(ManagedChannel grpcChan) throws DpGrpcException {
        return new DpQueryConnection(grpcChan);
    }

    /**
     * <p>
     * Creates a new code>DpQueryConnection</code> instance as an alias of the given bound <code>DpGrpcConnection</code>
     * object.
     * </p>
     * 
     * @param conn  bound <code>DpGrpcConnection</code> object to be aliased
     * 
     * @return  the argument cast as a <code>DpQueryConnection</code> 
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     */
    public static DpQueryConnection from(DpGrpcConnection<DpQueryServiceGrpc, DpQueryServiceBlockingStub, DpQueryServiceFutureStub, DpQueryServiceStub> conn) throws DpGrpcException {
        return new DpQueryConnection(conn);
    }
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpQueryConnection</code> supported from the gRPC managed 
     * channel instance.
     * </p>
     *
     * @param grpcChan  gRPC channel instance supporting all communications with the Query Service
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     */
    public DpQueryConnection(ManagedChannel grpcChan) throws DpGrpcException {
        super(DpQueryServiceGrpc.class, grpcChan);
    }
    

    /**
     * <p>
     * Constructs a new instance of <code>DpQueryConnection</code> from a bound <code>DpGrpcConnection</code> instance.
     * </p>
     * <p>
     * Aliases the given <code>DpGrpcConnection</code> instance as the newly created <code>DpQueryConnection</code> 
     * object.  That is, we are essentially casting the argument as a <code>DpQueryConnection</code> class. 
     * </p>
     *
     * @param conn  <code>DpGrpcConnection</code> object to be cast as a <code>DpQueryConnection</code> 
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     */
    public DpQueryConnection(DpGrpcConnection<DpQueryServiceGrpc, DpQueryServiceBlockingStub, DpQueryServiceFutureStub, DpQueryServiceStub> conn) throws DpGrpcException {
        super(conn);
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Blocks until the connection finishes a shutdown operation and the underlying gRPC channel fully terminates.
     * </p>
     * <p>
     * Defers to the base class {@link #awaitTermination(long, java.util.concurrent.TimeUnit)} supply the default
     * timeout parameters for the Query Service connection.
     * </p> 
     * 
     * @return  <code>true</code> if connection shut down and terminated within the alloted limit, 
     *          <code>false</code> either the shutdown operation was never invoked, or the operation failed
     *          
     * @throws InterruptedException process was interrupted while waiting for channel to fully terminate
     * 
     * @see #awaitTermination(long, java.util.concurrent.TimeUnit)
     */
    public boolean awaitTermination() throws InterruptedException {
        return super.awaitTermination(CFG_DEFAULT.timeout.limit, CFG_DEFAULT.timeout.unit);
    }

}
