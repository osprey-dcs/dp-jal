/*
 * Project: dp-api-common
 * File:	DpQueryServiceFactory.java
 * Package: com.ospreydcs.dp.api.query
 * Type: 	DpQueryServiceFactory
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
 * @since Jan 7, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.grpc.GrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnection;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiFactoryBase;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;

/**
 * <p>
 * Connection factory for the <code>DqQueryService</code> Data Platform Query Service API interface.
 * </p>
 * <p>
 * The connection factory is capable of creating <code>{@link DpQueryService}</code> interfaces with 
 * various gRPC connection configurations to the Data Platform Query Service.  These configurations range
 * from fully default to fully user specified, with various combinations as determined by the 
 * <code>connect(...)</code> method arguments.
 * </p>
 * <p>
 * Note that <code>{@link DpQueryService}</code> instances can be created directly with a <code>{@link DpQueryConnection}</code>
 * objects using the <code>{@link DpQueryService#from(DpGrpcConnection)}</code> method.  
 * <code>DpQueryConnection</code> instances are obtained from the <code>{@link DpQueryConnectionFactory}</code> connection 
 * factory utility which has <code>connect(...)</code> methods analogous to those here.
 * </p>
 * <p>
 * This class uses the static instance <code>{@link #INSTANCE}</code> as the singleton connection factory 
 * for <code>DpQueryService</code> creation using the DP API library default configuration.  
 * Users can use the <code>{@link #INSTANCE}</code> instance directory or obtain it indirectly through the 
 * <code>{@link #getInstance()}</code> static method.
 * </p>
 * <p>
 * Factory instances can also be created independently using the .
 *
 * 
 * @author Christopher K. Allen
 * @since Jan 7, 2024
 *
 * @see DpQueryService
 * @see DpQueryConnection
 * @see DpQueryConnectionFactory
 * @see DpServiceApiFactoryBase
 */
public final class DpQueryServiceFactory
        extends DpServiceApiFactoryBase<DpQueryService, DpQueryConnection, DpQueryServiceGrpc, DpQueryServiceBlockingStub, DpQueryServiceFutureStub, DpQueryServiceStub> {
    

    //
    // Application Resources
    //
    
    /** Default configuration parameters for all DP Query Service connections as taken from the application configuration */
    private static final GrpcConnectionConfig   CFG_CONN_DEFAULT = DpApiConfig.getInstance().connections.query;

    
    //
    // Class Resources
    //
    
    /** The singleton instance of the connection factory */
    public static final DpQueryServiceFactory   INSTANCE = newFactory(CFG_CONN_DEFAULT);
    
    
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
    public static final DpQueryServiceFactory   newFactory(GrpcConnectionConfig cfgDefault) {
        return new DpQueryServiceFactory(cfgDefault);
    }
    
    
    //
    // Class Methods
    //
    
    /**
     * Returns the singleton instance of this connection factory.
     * 
     * @return  the static instance <code>{@link #INSTANCE}</code>
     */
    public static DpQueryServiceFactory getInstance() {
        return INSTANCE;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpQueryServiceFactory</code>.
     * </p>
     * <p>
     * Super class requirement for instance construction.   Supplies the Protobuf-generated service interface
     * class type and the new factory's default connection parameters for the Data Platform Query Service.
     * </p>
     * 
     * @param   cfgDefault  the default connection parameters for the query service used by new factory
     */
    private DpQueryServiceFactory(GrpcConnectionConfig cfgDefault) {
        super(DpQueryServiceGrpc.class, cfgDefault);
    }


    /**
     *
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiFactoryBase#apiFrom(com.ospreydcs.dp.api.grpc.model.DpGrpcConnection)
     */
    @Override
//    protected DpQueryService apiFrom(DpGrpcConnection<DpQueryServiceGrpc, DpQueryServiceBlockingStub, DpQueryServiceFutureStub, DpQueryServiceStub> conn) throws DpGrpcException {
    protected DpQueryService apiFrom(DpQueryConnection conn) throws DpGrpcException {
//        DpQueryConnection   connSub = DpQueryConnection.from(conn);
//        DpQueryService      qsApi = DpQueryService.from(connSub);
        DpQueryService      qsApi = DpQueryService.from(conn);
        
        return qsApi;
    }

    /**
     *
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiFactoryBase#connectionFrom(com.ospreydcs.dp.api.grpc.model.DpGrpcConnection)
     */
    @Override
    protected DpQueryConnection connectionFrom(
            DpGrpcConnection<DpQueryServiceGrpc, DpQueryServiceBlockingStub, DpQueryServiceFutureStub, DpQueryServiceStub> conn)
            throws DpGrpcException {
        return DpQueryConnection.from(conn);
    }

}
