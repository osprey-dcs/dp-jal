/*
 * Project: dp-api-common
 * File:	DpIngestionStreamFactory.java
 * Package: com.ospreydcs.dp.api.ingest
 * Type: 	DpIngestionStreamFactory
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
 * @since Apr 27, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.impl;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnection;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiFactoryBase;
import com.ospreydcs.dp.api.ingest.IIngestionStream;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceStub;

/**
 * <p>
 * Connection factory for <code>DpIngestionStreamDeprecated</code> instances supporting the <code>IIngestionStream</code> interface.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * The <code>DpIngestionStreamDeprecated</code> implementation is currently being deprecated.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 27, 2024
 */
//@Deprecated(since="Aug 22, 2024", forRemoval=true)
public class DpIngestionStreamFactory extends
        DpServiceApiFactoryBase<IIngestionStream, 
                                DpIngestionConnection, 
                                DpIngestionServiceGrpc, 
                                DpIngestionServiceBlockingStub, 
                                DpIngestionServiceFutureStub, 
                                DpIngestionServiceStub> {

    //
    // Application Resources
    //
    
    /** Default configuration parameters for all DP Query Service connections as taken from the application configuration */
    private static final DpGrpcConnectionConfig   CFG_CONN_DEFAULT = DpApiConfig.getInstance().connections.ingestion;

    
    //
    // Class Resources
    //
    
    /** The singleton instance of the connection factory */
    public static final DpIngestionStreamFactory   FACTORY = newFactory(CFG_CONN_DEFAULT);
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new, initialized instance of the <code>DpIngestionServiceFactory</code> 
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
    public static final DpIngestionStreamFactory   newFactory(DpGrpcConnectionConfig cfgDefault) {
        return new DpIngestionStreamFactory(cfgDefault);
    }
    
    
    //
    // Class Methods
    //
    
    /**
     * Returns the singleton instance of this connection factory.
     * 
     * @return  the static instance <code>{@link #FACTORY}</code>
     */
    public static DpIngestionStreamFactory getInstance() {
        return FACTORY;
    }
    

    //
    // Support Methods
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpIngestionStreamFactory</code>.
     * </p>
     * <p>
     * Super class requirement for instance construction.   Supplies the Protobuf-generated service interface
     * class type and the new factory's default connection parameters for the Data Platform Ingestion Service.
     * </p>
     * 
     * @param   cfgDefault  the default connection parameters for the Ingestion Service used by new factory
     */
    private DpIngestionStreamFactory(DpGrpcConnectionConfig cfgDefault) {
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
    protected IIngestionStream apiFrom(DpIngestionConnection conn) throws DpGrpcException {
        IIngestionStream    apiIngest = DpIngestionStreamImpl.from(conn);
        
        return apiIngest;
    }

}
