/*
 * Project: dp-api-common
 * File:	DpQueryServiceApiFactoryNew.java
 * Package: com.ospreydcs.dp.api.query.impl
 * Type: 	DpQueryServiceApiFactoryNew
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
 * @since Apr 24, 2025
 *
 */
package com.ospreydcs.dp.api.query.impl;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnection;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiFactoryBase;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.query.IQueryService;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;

/**
 * <p>
 * Connection factory for the Query Service API <code>IQueryService</code> interface.
 * </p>
 * <p>
 * This is the instance-based connection factory derived directly from <code>DpServiceApiFactoryBase</code>.
 * It maintains a singleton instance as a class resource for Query Service connections.
 * </p> 
 * <p>
 * <h2>NOTES:</h2>
 * This connection factory implementation supplies the <code>{@link DpQueryServiceImplNew}</code> implementation
 * for the <code>IQueryService</code> interface.  The <code>DpQueryServiceImplNew</code> implementation 
 * supports BOTH timestamp lists and time-domain collisions within recovered data once correlated.  This is the
 * preferred connection factory once <code>DpQueryServiceImplNew</code> is stable.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Apr 24, 2025
 *
 */
public class DpQueryServiceApiFactoryNew extends DpServiceApiFactoryBase<
                                                    IQueryService, 
                                                    DpQueryConnection, 
                                                    DpQueryServiceGrpc, 
                                                    DpQueryServiceBlockingStub, 
                                                    DpQueryServiceFutureStub, 
                                                    DpQueryServiceStub> 
{
    
    //
    // Application Resources
    //
    
    /** Default configuration parameters for all DP Query Service connections as taken from the application configuration */
    private static final DpGrpcConnectionConfig   CFG_CONN_DEFAULT = DpApiConfig.getInstance().connections.query;

    
    //
    // Class Resources
    //
    
    /** The singleton instance of the connection factory */
    public static final DpQueryServiceApiFactoryNew   FACTORY = newFactory(CFG_CONN_DEFAULT);
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new, initialized instance of the <code>DpQueryServiceApiFactoryNew</code> 
     * Query Service API connection factory.
     * </p>
     * <p>
     * The caller must supply the new factory's default connection parameters used for the 
     * Data Platform Query Service.
     * </p>
     * 
     * @param   cfgDefault  the default connection parameters for the query service used by new factory
     * 
     * @return  a new Query Service API factory ready for <code>DpQueryServiceImplNew</code> creation and connection
     */
    public static final DpQueryServiceApiFactoryNew   newFactory(DpGrpcConnectionConfig cfgDefault) {
        return new DpQueryServiceApiFactoryNew(cfgDefault);
    }
    
    
    //
    // Class Methods
    //
    
    /**
     * Returns the singleton instance of this connection factory.
     * 
     * @return  the static instance <code>{@link #FACTORY}</code>
     */
    public static DpQueryServiceApiFactoryNew  getInstance() {
        return FACTORY;
    }
    
    
    //
    // Base Class Requirement
    //
    
    /**
     * <p>
     * Constructs a new <code>DpQueryServiceApiFactoryNew</code> instance.
     * </p>
     * <p>
     * Super class requirement for instance construction.   Supplies the Protocol Buffers generated service 
     * Query Service interface class type <code>DpQueryServiceGrpc</code> and the new factory's default connection 
     * parameters to the super class.
     * </p>
     * 
     * @param   cfg the default connection parameters for the Query Service used by new factory
     */
    protected DpQueryServiceApiFactoryNew(DpGrpcConnectionConfig cfg) {
        super(DpQueryServiceGrpc.class, cfg);
    }

    
    //
    // DpServiceApiFactoryBase Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiFactoryBase#connectionFrom(com.ospreydcs.dp.api.grpc.model.DpGrpcConnection)
     */
    @Override
    protected DpQueryConnection connectionFrom(
            DpGrpcConnection<DpQueryServiceGrpc, 
                             DpQueryServiceBlockingStub, 
                             DpQueryServiceFutureStub, 
                             DpQueryServiceStub> conn) throws DpGrpcException {
        return DpQueryConnection.from(conn);
    }

    /**
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiFactoryBase#apiFrom(com.ospreydcs.dp.api.grpc.model.DpGrpcConnection)
     */
    @Override
    protected IQueryService apiFrom(DpQueryConnection conn) throws DpGrpcException {
        return DpQueryServiceImplNew.from(conn);
    }



}
