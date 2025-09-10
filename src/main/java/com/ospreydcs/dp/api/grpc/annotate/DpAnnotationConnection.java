/*
 * Project: dp-api-common
 * File:	DpAnnotationConnection.java
 * Package: com.ospreydcs.dp.api.grpc.annotate
 * Type: 	DpAnnotationConnection
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
 * @since Feb 5, 2025
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.annotate;

import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.JalConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnection;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc.DpAnnotationServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc.DpAnnotationServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc.DpAnnotationServiceStub;

import io.grpc.ManagedChannel;

/**
 *
 * <p>
 * Fly weight binding generic <code>DpGrpcConnection</code> to the Data Platform Annotation Service.  
 * </p>
 * <p>
 * Concrete class supplies type bindings for <code>DpGrpcConnection</code> to provide connections to
 * the <code>DpAnnotationServiceGrpc</code> Protocol Buffers interface and communications stubs.
 * Convenience constructors and creators are supplied to make instances from the <code>DpGrpcConnection</code>
 * base class and also gRPC managed channels.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 5, 2025
 *
 */
public class DpAnnotationConnection extends DpGrpcConnection<DpAnnotationServiceGrpc, DpAnnotationServiceBlockingStub, DpAnnotationServiceFutureStub, DpAnnotationServiceStub> {
    
    
    //
    // Application Resources
    //
    
    /** The Annotation Service default connection parameters */
    private static final DpGrpcConnectionConfig     CFG_DEFAULT = JalConfig.getInstance().connections.annotation;
    
    
    //
    // Class Constants
    //
    
    /** Default timeout limit for Annotation Service operations */
    public static final long        LNG_DEF_TIMEOUT_LIMIT = CFG_DEFAULT.timeout.limit;
    
    /** Default timeout units for Annotation Service operations */
    public static final TimeUnit    TU_DEF_TIMEOUT_UNIT = CFG_DEFAULT.timeout.unit;
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new <code>DpAnnotatinoConnection</code> instance supported by the given gRPC channel.
     * </p>
     * 
     * @param chanGrpc  gRPC channel instance supporting all communications with the Annotation Service
     * 
     * @return new <code>DpAnnotaionConnection</code> using gRPC channel for Annotation Service connection
     *
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     */
    public static DpAnnotationConnection    from(ManagedChannel chanGrpc) throws DpGrpcException {
        return new DpAnnotationConnection(chanGrpc);
    }
    
    /**
     * <p>
     * Creates a new code>DpAnnotationConnection</code> instance as an alias of the given bound 
     * <code>DpGrpcConnection</code> object.
     * </p>
     * 
     * @param conn  <code>DpGrpcConnection</code> object bound to <code>DpAnnotationServiceGrpc</code> to be aliased
     * 
     * @return  the argument cast as a <code>DpAnnotationConnection</code> 
     */
    public static DpAnnotationConnection    from(DpGrpcConnection<DpAnnotationServiceGrpc, DpAnnotationServiceBlockingStub, DpAnnotationServiceFutureStub, DpAnnotationServiceStub> conn) {
        return new DpAnnotationConnection(conn);
    }
    
    
    //
    // Constructors
    //

    /**
     * <p>
     * Constructs a new instance of <code>DpAnnotationConnection</code> supported by the given gRPC managed channel
     * instance.
     * </p>
     *
     * @param chanGrpc  gRPC channel instance supporting all communications with the Annotation Service
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     */
    public DpAnnotationConnection(ManagedChannel grpcChan) throws DpGrpcException {
        super(DpAnnotationServiceGrpc.class, grpcChan);
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>DpAnnotationConnection</code> from the given bound
     * <code>DpGrpcConnection</code> instance.
     * </p>
     *
     * @param conn  <code>DpGrpcConnection</code> object to be cast as a <code>DpAnnotationConnection</code> 
     */
    public DpAnnotationConnection(DpGrpcConnection<DpAnnotationServiceGrpc, DpAnnotationServiceBlockingStub, DpAnnotationServiceFutureStub, DpAnnotationServiceStub> conn) {
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
     * Defers to the base class {@link #awaitTermination(long, java.util.concurrent.TimeUnit)} method supplying 
     * the default timeout parameters for the Annotation Service connection.
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
        return super.awaitTermination(LNG_DEF_TIMEOUT_LIMIT, TU_DEF_TIMEOUT_UNIT);
    }

}
