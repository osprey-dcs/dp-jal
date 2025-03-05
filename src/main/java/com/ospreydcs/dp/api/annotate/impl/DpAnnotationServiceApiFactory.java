/*
 * Project: dp-api-common
 * File:	DpAnnotationServiceApiFactory.java
 * Package: com.ospreydcs.dp.api.annotate.impl
 * Type: 	DpAnnotationServiceApiFactory
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
 * @since Mar 2, 2025
 *
 */
package com.ospreydcs.dp.api.annotate.impl;

import com.ospreydcs.dp.api.annotate.IAnnotationService;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnection;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnection;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiFactoryBase;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc.DpAnnotationServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc.DpAnnotationServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc.DpAnnotationServiceStub;

/**
 * <p>
 * Connection factory for the Annotation Service API <code>IAnnotationService</code> interface.
 * </p>
 * <p>
 * This is the instance-based connection factory for the Annotation Service.  The class maintains a
 * singleton connection factory instance for all connection operations.  Connection operations are
 * performed in the generic base class which is bound to the <code>IAnnotationService</code> interface
 * and the Annotation Service gRPC interface classes.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 2, 2025
 *
 */
public class DpAnnotationServiceApiFactory extends DpServiceApiFactoryBase<
                                                    IAnnotationService, 
                                                    DpAnnotationConnection, 
                                                    DpAnnotationServiceGrpc, 
                                                    DpAnnotationServiceBlockingStub, 
                                                    DpAnnotationServiceFutureStub, 
                                                    DpAnnotationServiceStub> 
{

    
    //
    // Application Resources
    //
    
    /** Default configuration parameters for all DP Annotation Service connections as taken from the application configuration */
    private static final DpGrpcConnectionConfig   CFG_CONN_DEFAULT = DpApiConfig.getInstance().connections.annotation;

    
    //
    // Class Resources
    //
    
    /** The singleton instance of the connection factory */
    public static final DpAnnotationServiceApiFactory   FACTORY = newFactory(CFG_CONN_DEFAULT);
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new, initialized instance of the <code>DpAnnotationServiceApiFactory</code> 
     * Annotation Service API connection factory.
     * </p>
     * <p>
     * The caller must supply the new factory's default connection parameters used for the 
     * Data Platform Annotation Service.
     * </p>
     * 
     * @param   cfgDefault  the default connection parameters for the Annotation Service used by new factory
     * 
     * @return  a new Query Service API factory ready for <code>DpAnnotationServiceApiFactory</code> creation and connection
     */
    public static final DpAnnotationServiceApiFactory   newFactory(DpGrpcConnectionConfig cfgDefault) {
        return new DpAnnotationServiceApiFactory(cfgDefault);
    }
    
    
    //
    // Class Methods
    //
    
    /**
     * Returns the singleton instance of this connection factory.
     * 
     * @return  the static instance <code>{@link #FACTORY}</code>
     */
    public static DpAnnotationServiceApiFactory  getInstance() {
        return FACTORY;
    }
    
    
    //
    // Base Class Requirement
    //
    
    /**
     * <p>
     * Constructs a new <code>DpAnnotationServiceApiFactory</code> instance.
     * </p>
     * <p>
     * Super class requirement for instance construction.   Supplies the Protobuf-generated service interface
     * class type and the new factory's default connection parameters for the Data Platform Annotation Service.
     * </p>
     * 
     * @param   cfg the default connection parameters for the Annotation Service used by new factory
     */
    protected DpAnnotationServiceApiFactory(DpGrpcConnectionConfig cfg) {
        super(DpAnnotationServiceGrpc.class, cfg);
    }

    
    //
    // Base Class Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiFactoryBase#connectionFrom(com.ospreydcs.dp.api.grpc.model.DpGrpcConnection)
     */
    @Override
    protected DpAnnotationConnection connectionFrom(
            DpGrpcConnection<DpAnnotationServiceGrpc, DpAnnotationServiceBlockingStub, DpAnnotationServiceFutureStub, DpAnnotationServiceStub> conn)
            throws DpGrpcException {
        
        return DpAnnotationConnection.from(conn);
    }

    /**
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiFactoryBase#apiFrom(com.ospreydcs.dp.api.grpc.model.DpGrpcConnection)
     */
    @Override
    protected IAnnotationService apiFrom(DpAnnotationConnection conn) throws DpGrpcException {
        return DpAnnotationServiceApiImpl.from(conn);
    }

}
