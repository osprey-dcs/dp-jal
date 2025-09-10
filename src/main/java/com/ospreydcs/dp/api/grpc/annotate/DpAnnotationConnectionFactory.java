/*
 * Project: dp-api-common
 * File:	DpAnnotationConnectionFactory.java
 * Package: com.ospreydcs.dp.api.grpc.annotate
 * Type: 	DpAnnotationConnectionFactory
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

import com.ospreydcs.dp.api.config.JalConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnection;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactoryBase;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc.DpAnnotationServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc.DpAnnotationServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.annotation.DpAnnotationServiceGrpc.DpAnnotationServiceStub;

/**
 * <p>
 * Singleton class for creating client connection <code>DpAnnotationConnection</code> instances
 * to the Data Platform Annotation Service.
 * </p>
 * <p>
 * This class maintains a static singleton instance of <code>DpAnnotationConnectionFactory</code> bound
 * to the Data Platform Query Service with DP API default connection parameters.
 * The static connection factory instance <code>{@link #FACTORY}</code> may be accessed directly
 * or through the static method <code>{@link #getInstance()}</code>.
 * </p>
 * <p>  
 * All connection operations are performed with the <code>connect(...)</code> methods implemented
 * in the base class.
 * See the {@link DpGrpcConnectionFactoryBase} documentation for further details.
 * </p> 
 * <p>
 * <h2>NOTES:</h2>
 * It is possible to create Annotation Service connection factory instances that use alternate default
 * parameters for connection configurations.  The static creator method 
 * <code>{@link #newFactory(DpGrpcConnectionConfig)}</code> is available for this purpose.
 * (It also creates the static instance <code>{@link #FACTORY}</code> using the DP API library
 * configuration.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 5, 2025
 *
 */
public class DpAnnotationConnectionFactory extends 
                DpGrpcConnectionFactoryBase<DpAnnotationConnection, 
                                            DpAnnotationServiceGrpc, 
                                            DpAnnotationServiceBlockingStub, 
                                            DpAnnotationServiceFutureStub, 
                                            DpAnnotationServiceStub> 
{


    //
    // Application Resources
    //
    
    /** The API Library default Annotation Service configuration parameters */
    private static final DpGrpcConnectionConfig   CFG_DEFAULT = JalConfig.getInstance().connections.annotation;

    
    //
    // Class Resources
    //

    /** The singleton factory instance using the DP API default parameters for the Annotation Service */
    public static final DpAnnotationConnectionFactory   FACTORY = DpAnnotationConnectionFactory.from(CFG_DEFAULT);

    
    //
    // Class Methods
    //
    
    /**
     * <p>
     * Returns the singleton Query Service connection factory using the Data Platform default
     * configuration parameters.
     * </p>
     * 
     * @return  the singleton Annotation Service connection factory
     */
    public static DpAnnotationConnectionFactory  getInstance() {
        return FACTORY;
    }
   
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new <code>DpAnnotationConnectionFactory</code> instance with the given default parameters.
     * </p>
     * <p>
     * This creator can be used to create additional connection factors with different default 
     * parameters for the connection factory instances.  For example, if a separate Data Platform
     * Annotation Service is used for testing, the default parameters can point to that deployment.
     * </p>
     * 
     * @param cfgDefault    the default gRPC connection parameters used for factory connections
     * 
     * @return  a new Annotation Service connection factory use the given default parameters
     * 
     */
    public static DpAnnotationConnectionFactory from(DpGrpcConnectionConfig cfgDefault) {
        return new DpAnnotationConnectionFactory(cfgDefault);
    }
    
    
    // 
    // DpGrpcConnectionFactoryBase Requirements
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpAnnotationConnectionFactory</code> with the given default parameters.
     * </p>
     *
     * @param cfgDefault   default connection parameters to the Annotation Service
     */
    protected DpAnnotationConnectionFactory(DpGrpcConnectionConfig cfgDefault) {
        super(DpAnnotationServiceGrpc.class, cfgDefault);
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactoryBase#createFrom(com.ospreydcs.dp.api.grpc.model.DpGrpcConnection)
     */
    @Override
    protected DpAnnotationConnection createFrom(
            DpGrpcConnection<DpAnnotationServiceGrpc, DpAnnotationServiceBlockingStub, DpAnnotationServiceFutureStub, DpAnnotationServiceStub> conn)
            throws DpGrpcException {
        
        DpAnnotationConnection  connBnd = DpAnnotationConnection.from(conn);
        
        return connBnd;
    }

}
