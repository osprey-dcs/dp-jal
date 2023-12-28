/*
 * Project: dp-api-common
 * File:	DpIngestionConnectionFactory.java
 * Package: com.ospreydcs.dp.api.grpc
 * Type: 	DpIngestionConnectionFactory
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
package com.ospreydcs.dp.api.grpc;

import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceStub;

import io.grpc.ManagedChannel;


/**
 *
 * @author Christopher K. Allen
 * @since Dec 28, 2023
 *
 */
public final class DpIngestionConnectionFactory /* extends DpGrpcConnectionFactory<DpIngestionServiceGrpc, DpIngestionServiceBlockingStub, DpIngestionServiceFutureStub, DpIngestionServiceStub> */{

    
    //
    // Class Types
    //
    
    public static final class DpIngestionConnection extends DpGrpcConnection<DpIngestionServiceBlockingStub, DpIngestionServiceFutureStub, DpIngestionServiceStub> {

        public DpIngestionConnection(ManagedChannel chnGrpc, Class<DpIngestionServiceGrpc> clsService) throws DpGrpcException {
            super(chnGrpc, clsService);
            // TODO Auto-generated constructor stub
        }
        public DpIngestionConnection(DpGrpcConnection<DpIngestionServiceBlockingStub, DpIngestionServiceFutureStub, DpIngestionServiceStub> conn) throws DpGrpcException {
            super(conn.getGrpcChannel(), DpIngestionServiceGrpc.class);
        }

//        /**
//         *
//         * @see @see com.ospreydcs.dp.api.grpc.DpGrpcConnection#getStubAsync()
//         */
//        @Override
//        public DpIngestionServiceStub getStubAsync() {
//            // TODO Auto-generated method stub
//            return super.getStubAsync();
//        }
        
    }
    
    //
    // Class Resources
    //
    
//    public static final DpIngestionConnectionFactory   FAC;
    private static final DpGrpcConnectionFactory<DpIngestionServiceGrpc, DpIngestionServiceBlockingStub, DpIngestionServiceFutureStub, DpIngestionServiceStub> FAC;
    
    static {
//        FAC = new DpIngestionConnectionFactory(DpIngestionServiceGrpc.class, DpApiConfig.getInstance().services.ingestion);
        FAC = DpGrpcConnectionFactory.newFactory(DpIngestionServiceGrpc.class, DpApiConfig.getInstance().services.ingestion);
    }
             
//    protected DpIngestionConnectionFactory(Class<DpIngestionServiceGrpc> clsService, GrpcConnectionConfig cfgConn) {
//        super(clsService, cfgConn);
//    }

    /**
     * <p>
     * Creates a new <code>DpIngestionConnection</code> instance using default parameters.
     * </p>
     * <p>
     * See {@link DpGrpcConnectionFactory#connect()} for details.
     * </p>
     *
     * @see @see com.ospreydcs.dp.api.grpc.DpGrpcConnectionFactory#connect()
     */
//    @Override
    public static DpIngestionConnection connect() throws DpGrpcException {
        
        DpIngestionConnection conn = new DpIngestionConnection(FAC.connect());
        
        return conn;
    }

    /**
     * <p>
     * Creates a new <code>DpIngestionConnection</code> instance for the given parameters.
     * </p>
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int)} for details.
     * </p>
     *
     * @see com.ospreydcs.dp.api.grpc.DpGrpcConnectionFactory#connect(java.lang.String, int)
     */
//    @Override
    public static DpIngestionConnection connect(String strHost, int intPort) throws DpGrpcException {
        return new DpIngestionConnection(FAC.connect(strHost, intPort));
    }

    /**
     * <p>
     * Creates a new <code>DpIngestionConnection</code> instance for the given parameters.
     * </p>
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, long, TimeUnit)} for details.
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       server port used by the at the above service
     * @param lngTimeout    timeout limit used for connection operations (keepalive ping timeout) 
     * @param tuTimeout     timeout units used for connection operations (keepalive ping timeout)
     * 
     * @return new <code>DpIngestionConnection</code> instance connected to the given host
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see {@link com.ospreydcs.dp.api.grpc.DpGrpcConnectionFactory#connect(String, int, long, TimeUnit)}
     */
    public static DpIngestionConnection connect(String strHost, int intPort, long lngTimeout, TimeUnit tuTimeout) throws DpGrpcException {
        return new DpIngestionConnection(FAC.connect(strHost, intPort, lngTimeout, tuTimeout));
    }
}
