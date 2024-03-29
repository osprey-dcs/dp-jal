/*
 * Project: dp-api-common
 * File:	DpIngestionServiceFactory.java
 * Package: com.ospreydcs.dp.api.ingest
 * Type: 	DpIngestionServiceFactory
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
 * @since Mar 28, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;

import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnectionFactory;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnection;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiFactoryBase;

import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceStub;

/**
 * <p>
 * Connection factory for the <code>DqIngestionService</code> Data Platform Ingestion Service API interface.
 * </p>
 * <p>
 * The connection factory is capable of creating <code>{@link DpIngestionService}</code> interfaces with 
 * various gRPC connection configurations to the Data Platform Ingestion Service.  These configurations range
 * from fully default to fully user specified, with various combinations as determined by the 
 * <code>connect(...)</code> method arguments.
 * </p>
 * <p>
 * <h2>Factory Singleton</h2>
 * This class uses the static instance <code>{@link #FACTORY}</code> as the singleton connection factory 
 * for <code>DpIngestionService</code> creation using the DP API library default configuration.  
 * Client can use the <code>{@link #FACTORY}</code> instance directory or obtain it indirectly through the 
 * <code>{@link #getInstance()}</code> static method.
 * </p>
 * <p>
 * <h2>Direct API Creation</h2>
 * Note that <code>{@link DpIngestionService}</code> instances can be created directly with 
 * <code>{@link DpIngestionConnection}</code>
 * objects using the <code>{@link DpIngestionService#from(DpGrpcConnection)}</code> method.  
 * <code>DpQueryConnection</code> instances are obtained from the <code>{@link DpIngestionConnectionFactory}</code> 
 * connection factory utility which has <code>connect(...)</code> methods analogous to those here.
 * </p>
 * <p>
 * <h2>Independent Factories</h2>
 * Clients can create specialized connection factory instances if desired.  Specifically, factory instances can 
 * be created independently using the <code>{@link #newFactory(DpGrpcConnectionConfig)}</code> method.  The
 * argument should be a connection configuration designating a valid Data Platform service.
 * </p>   
 *
 * @author Christopher K. Allen
 * @since Mar 28, 2024
 *
 * @see DpIngestionService
 * @see DpIngestionConnection
 * @see DpIngestionConnectionFactory
 * @see DpServiceApiFactoryBase
 * @see DpGprcConnectionConfig
 */
public final class DpIngestionServiceFactory extends
        DpServiceApiFactoryBase<DpIngestionService, DpIngestionConnection, DpIngestionServiceGrpc, DpIngestionServiceBlockingStub, DpIngestionServiceFutureStub, DpIngestionServiceStub> {


    //
    // Application Resources
    //
    
    /** Default configuration parameters for all DP Query Service connections as taken from the application configuration */
    private static final DpGrpcConnectionConfig   CFG_CONN_DEFAULT = DpApiConfig.getInstance().connections.ingestion;

    
    //
    // Class Resources
    //
    
    /** The singleton instance of the connection factory */
    public static final DpIngestionServiceFactory   FACTORY = newFactory(CFG_CONN_DEFAULT);
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new, initialized instance of the <code>DpQueryServiceFactory</code> 
     * Query Service API connection factory.
     * </p>
     * <p>
     * The caller must supply the new factory's default connection parameters used for the 
     * Data Platform Query Service.
     * </p>
     * 
     * @param   cfgDefault  the default connection parameters for the query service used by new factory
     * 
     * @return  a new Query Service API factory ready for <code>DpQueryService</code> creation and connection
     */
    public static final DpIngestionServiceFactory   newFactory(DpGrpcConnectionConfig cfgDefault) {
        return new DpIngestionServiceFactory(cfgDefault);
    }
    
    
    //
    // Class Methods
    //
    
    /**
     * Returns the singleton instance of this connection factory.
     * 
     * @return  the static instance <code>{@link #FACTORY}</code>
     */
    public static DpIngestionServiceFactory getInstance() {
        return FACTORY;
    }
    

    //
    // Support Methods
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpIngestionServiceFactory</code>.
     * </p>
     * <p>
     * Super class requirement for instance construction.   Supplies the Protobuf-generated service interface
     * class type and the new factory's default connection parameters for the Data Platform Ingestion Service.
     * </p>
     * 
     * @param   cfgDefault  the default connection parameters for the Ingestion Service used by new factory
     */
    private DpIngestionServiceFactory(DpGrpcConnectionConfig cfgDefault) {
        super(DpIngestionServiceGrpc.class, cfgDefault);
    }


    //
    // DpServiceApiFactoryBase Abstract Methods
    //
    
    /**
     *
     * @see @see com.ospreydcs.dp.api.grpc.model.DpServiceApiFactoryBase#connectionFrom(com.ospreydcs.dp.api.grpc.model.DpGrpcConnection)
     */
    @Override
    protected DpIngestionConnection connectionFrom(
            DpGrpcConnection<DpIngestionServiceGrpc, DpIngestionServiceBlockingStub, DpIngestionServiceFutureStub, DpIngestionServiceStub> conn)
            throws DpGrpcException {
        
        return DpIngestionConnection.from(conn);
    }


    /**
     *
     * @see @see com.ospreydcs.dp.api.grpc.model.DpServiceApiFactoryBase#apiFrom(com.ospreydcs.dp.api.grpc.model.DpGrpcConnection)
     */
    @Override
    protected DpIngestionService apiFrom(DpIngestionConnection conn) throws DpGrpcException {
        DpIngestionService      isApi = DpIngestionService.from(conn);
        
        return isApi;
    }

}
