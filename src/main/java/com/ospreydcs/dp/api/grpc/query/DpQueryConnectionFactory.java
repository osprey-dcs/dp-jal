/*
 * Project: dp-api-common
 * File:	DpQueryConnectionFactory.java
 * Package: com.ospreydcs.dp.api.grpc.query
 * Type: 	DpQueryConnectionFactory
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
 * @since Jan 14, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.query;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnection;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactoryBase;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;


/**
 * <p>
 * Singleton class for creating client connection <code>DpQueryConnection</code> instances
 * to the Data Platform Query Service.
 * </p>
 * <p>
 * This class maintains a static singleton instance of <code>DpQueryConnectionFactory</code> bound
 * to the Data Platform Query Service with DP API default connection parameters.
 * The static connection factory instance <code>{@link #FACTORY}</code> may be accessed directly
 * or through the static method <code>{@link #getFactory()}</code>.
 * </p>
 * <p>  
 * All connection operations are performed with the <code>connect(...)</code> methods implemented
 * in the base class.
 * See the {@link DpGrpcConnectionFactoryBase} documentation for further details.
 * </p> 
 * <p>
 * <h2>NOTES:</h2>
 * It is possible to create Query Service connection factory instances that use alternate default
 * parameters for connection configurations.  The static creator method 
 * <code>{@link #newFactory(DpGrpcConnectionConfig)}</code> is available for this purpose.
 * (It also creates the static instance <code>{@link #FACTORY}</code> using the DP API library
 * configuration.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 14, 2024
 * 
 * @see DpGrpcConnectionFactoryBase
 */
public final class DpQueryConnectionFactory extends 
    DpGrpcConnectionFactoryBase<DpQueryConnection, DpQueryServiceGrpc, DpQueryServiceBlockingStub, DpQueryServiceFutureStub, DpQueryServiceStub>

{

    //
    // Application Resources
    //
    
    /** The API Library default Query Service configuration parameters */
    private static final DpGrpcConnectionConfig   CFG_DEFAULT = DpApiConfig.getInstance().connections.query;

    
    //
    // Class Resources
    //

    /** The singleton factory instance using the DP API default parameters for the Query Service */
    public static final DpQueryConnectionFactory   FACTORY = newFactory(CFG_DEFAULT);

    
    //
    // Class Methods
    //
    
    /**
     * <p>
     * Returns the singleton Query Service connection factory using the Data Platform default
     * configuration parameters.
     * </p>
     * 
     * @return  the singleton Query Service connection factory
     */
    public static DpQueryConnectionFactory  getFactory() {
        return FACTORY;
    }
   
    
    //
    // Creator - For Alternate Default Configurations
    //
    
    /**
     * <p>
     * Creates a new <code>DpQueryConnectionFactory</code> instance with the given default parameters.
     * </p>
     * <p>
     * This creator can be used to create additional connection factors with different default 
     * parameters for the factory connection instances.  For example, if a separate Data Platform
     * Query Service is used for testing the default parameters can point to that deployment.
     * </p>
     * 
     * @param cfgDefault    the default gRPC connection parameters used for factory connections
     * 
     * @return  a new Query Service connection factory use the given default parameters
     */
    public static DpQueryConnectionFactory  newFactory(DpGrpcConnectionConfig cfgDefault) {
        return new DpQueryConnectionFactory(cfgDefault);
    }
    
    
    // 
    // DpGrpcConnectionFactoryBase Requirements
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpQueryConnectionFactory</code>.
     * </p>
     */
    protected DpQueryConnectionFactory(DpGrpcConnectionConfig cfgDefault) {
        super(DpQueryServiceGrpc.class, cfgDefault);
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactoryBase#createFrom(com.ospreydcs.dp.api.grpc.model.DpGrpcConnection)
     */
    @Override
    protected DpQueryConnection createFrom(
            DpGrpcConnection<DpQueryServiceGrpc, DpQueryServiceBlockingStub, DpQueryServiceFutureStub, DpQueryServiceStub> conn)
            throws DpGrpcException {
        
        DpQueryConnection   connBnd = DpQueryConnection.from(conn);
        
        return connBnd;
    }

}
