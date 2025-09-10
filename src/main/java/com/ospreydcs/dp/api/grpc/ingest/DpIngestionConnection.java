/*
 * Project: dp-api-common
 * File:    DpIngestionConnection.java
 * Package: com.ospreydcs.dp.api.grpc.ingest
 * Type:    DpIngestionConnection
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
package com.ospreydcs.dp.api.grpc.ingest;

import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.JalConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnection;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceStub;

import io.grpc.ManagedChannel;

/**
 * <p>
 * Fly weight class specifying correct generic types to function as a connection to the the Data Platform
 * Ingestion Service.
 * </p>
 * <p>
 * Concrete class supplies type bindings for generic <code>DpGrpcConnection</code> class to support connections
 * and operation with the Data Platform Ingestion Service.  This class adds no additional functionality.
 * The binding is done through inheritance and a creator and constructor is supplied to create instances
 * from the bound generic class. 
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 28, 2023
 *
 */
public final class DpIngestionConnection extends DpGrpcConnection<DpIngestionServiceGrpc, DpIngestionServiceBlockingStub, DpIngestionServiceFutureStub, DpIngestionServiceStub> {

    
    //
    // Application Resources
    //
    
    /** Default connection parameters for Ingestion Service */
    public static final DpGrpcConnectionConfig CFG_DEFAULT = JalConfig.getInstance().connections.ingestion;
    
    
    //
    // Class Constants
    //
    
    /** Default timeout limit */
    public static final long        LNG_TIMEOUT = CFG_DEFAULT.timeout.limit;
    
    /** Default timeout limit units */
    public static final TimeUnit    TU_TIMEOUT = CFG_DEFAULT.timeout.unit;
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new <code>DpIngestionConnection</code> instance from a gRPC managed channel.
     * </p>
     * 
     * @param grpcChan  gRPC channel supporting all connection operations
     * 
     * @return  new connection to the Data Platform Ingestion Service
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     */
    public static DpIngestionConnection from(ManagedChannel grpcChan) throws DpGrpcException {
        return new DpIngestionConnection(grpcChan);
    }
    
    /**
     * <p>
     * Creates a new <code>DpIngestionConnection</code> instance aliasing the given <code>DpGrpcConnection</code>.
     * </p>
     * 
     * @param grpcConn  bound <code>DpGrpcConnection</code> object to be aliased
     * 
     * @return  new connection to the Data Platform Ingestion Service
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     */
    public static DpIngestionConnection from(DpGrpcConnection<DpIngestionServiceGrpc, DpIngestionServiceBlockingStub, DpIngestionServiceFutureStub, DpIngestionServiceStub> conn) throws DpGrpcException {
        return new DpIngestionConnection(conn);
    }
    

    //
    // Constructors
    //

    /**
     * <p>
     * Constructs a new instance of <code>DpIngestionConnection</code> from a gRPC channel.
     * </p>
     *
     * @param grpcChan   gRPC channel supporting all connection operations
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     */
    public DpIngestionConnection(ManagedChannel grpcChan) throws DpGrpcException {
        super(DpIngestionServiceGrpc.class, grpcChan);
    }

    /**
     * <p>
     * Constructs a new instance of <code>DpIngestionConnection</code> from a bound <code>DpGrpcConnection</code>.
     * </p>
     *
     * @param conn the (bound) <code>DpGrpcConnection</code> object to be aliased 
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     */
    public DpIngestionConnection(DpGrpcConnection<DpIngestionServiceGrpc, DpIngestionServiceBlockingStub, DpIngestionServiceFutureStub, DpIngestionServiceStub> conn) throws DpGrpcException {
        super(conn);
    }
    
    
    //
    // Operations
    //

    /**
     * <p>
     * Blocks until the connection finishes a shutdown operation and the underlying gRPC
     * channel fully terminates.
     * </p>
     * <p>
     * Calls {@link DpGrpcConnection#awaitTermination(long, TimeUnit)} using the default timeout limits.
     * </p>
     * 
     * @return  <code>true</code> if connection shut down and terminated within the alloted limit,
     *          <code>false</code> either the shutdown operation was never invoked, or the operation failed 
     * 
     * @throws InterruptedException process was interrupted while waiting for channel to fully terminate
     * 
     * @see DpGrpcConnection#awaitTermination(long, TimeUnit)
     */
    public boolean awaitTermination() throws InterruptedException {
        return super.awaitTermination(LNG_TIMEOUT, TU_TIMEOUT);
    }

}