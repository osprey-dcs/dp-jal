/*
 * Project: dp-api-common
 * File:	DpQueryConnectionFactoryStatic.java
 * Package: com.ospreydcs.dp.api.grpc.query
 * Type: 	DpQueryConnectionFactoryStatic
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
 * @since Dec 29, 2023
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.query;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactory;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;

/**
 * <p>
 * Utility class for creating client connections to the Data Platform Query Service.
 * </p>
 * <p>
 * This is a fly weight class that maintains a static instance of <code>DpGrpcConnectionFactory</code> bound
 * to the Data Platform Query Service.  All connection operations here are provided by static methods which
 * delegate to the static factory instance <code>{@link FAC}</code>.
 * See the {@link DpGrpcConnectionFactory} documentation for more details.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Dec 29, 2023
 *
 *
 * @see DpGrpcConnectionFactory
 */
//@Deprecated(since="Jan 15, 2024", forRemoval=true)
public final class DpQueryConnectionFactoryStatic {
    
    
    //
    // Application Resources
    //
    
    /** The API Library default Query Service configuration parameters */
    private static final DpGrpcConnectionConfig   CFG_DEFAULT = DpApiConfig.getInstance().connections.query;

    
    //
    // Class Resources
    //

    /** The <code>DpGrpcConnectionFactory</code> bound to the Query Service */
    private static final DpGrpcConnectionFactory<DpQueryServiceGrpc, DpQueryServiceBlockingStub, DpQueryServiceFutureStub, DpQueryServiceStub> FAC;
    
    
    static {
        /** Create the static connection factory instance */
        FAC = DpGrpcConnectionFactory.newFactory(DpQueryServiceGrpc.class, CFG_DEFAULT);
    }
          
    
    //
    // Default TLS Security
    //
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect()} for details.
     * </p>
     * 
     * @return new <code>DpQueryConnection</code> instance with all default parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect()
     */
    public static DpQueryConnection connect() throws DpGrpcException {
        return DpQueryConnection.from(FAC.connect());
    }
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int)} for details.
     * </p>
     * 
     * @return new <code>DpQueryConnection</code> instance with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(String, int)
     */
    public static DpQueryConnection connect(String strUrl, int intPort) throws DpGrpcException {
        return DpQueryConnection.from(FAC.connect(strUrl, intPort));
    }
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, boolean)} for details.
     * </p>
     * 
     * @return new <code>DpQueryConnection</code> instance with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(String, int, boolean)
     */
    public static DpQueryConnection connect(String strUrl, int intPort, boolean bolPlainText) throws DpGrpcException {
        return DpQueryConnection.from(FAC.connect(strUrl, intPort, bolPlainText));
    }
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, boolean, long, TimeUnit)} for details.
     * </p>
     * 
     * @return new <code>DpQueryConnection</code> instance with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(String, int, boolean, long, TimeUnit)
     */
    public static DpQueryConnection connect(String strHost, int intPort, boolean bolPlainText, long lngTimeout, TimeUnit tuTimeout) throws DpGrpcException {
        return DpQueryConnection.from(FAC.connect(strHost, intPort, bolPlainText, lngTimeout, tuTimeout));
    }
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, boolean, boolean, int, boolean, boolean, long, TimeUnit)} for details.
     * </p>
     * 
     * @return new <code>DpQueryConnection</code> instance with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(String, int, boolean, boolean, int, boolean, boolean, long, TimeUnit)
     */
    public static DpQueryConnection connect(
            String strHost, 
            int intPort, 
            boolean bolTlsActive,
            boolean bolPlainText,
            int     intMsgSizeMax,
            boolean bolKeepAlive,
            boolean bolGzipCompr,
            long    lngTimeout,
            TimeUnit tuTimeout
            ) throws DpGrpcException {
        return DpQueryConnection.from(FAC.connect(strHost, intPort, bolTlsActive, bolPlainText, intMsgSizeMax, bolKeepAlive, bolGzipCompr, lngTimeout, tuTimeout));
    }

    
    //
    // Explicit TLS Security Parameters
    //
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(File, File, File)} for details.
     * </p>
     * 
     * @return new <code>DpQueryConnection</code> instance with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(File, File, File)
     */
    public static DpQueryConnection connect(File fileTrustedCerts, File fileClientCerts, File fileClientKey) throws DpGrpcException {
        return DpQueryConnection.from(FAC.connect(fileTrustedCerts, fileClientCerts, fileClientKey));
    }
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, File, File, File)} for details.
     * </p>
     * 
     * @return new <code>DpQueryConnection</code> instance with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(String, int, File, File, File)
     */
    public static DpQueryConnection connect(String strHost, int intPort, File fileTrustedCerts, File fileClientCerts, File fileClientKey) throws DpGrpcException {
        return DpQueryConnection.from(FAC.connect(strHost, intPort, fileTrustedCerts, fileClientCerts, fileClientKey));
    }
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, File, File, File, boolean, int, boolean, boolean, long, TimeUnit)} for details.
     * </p>
     * 
     * @return new <code>DpQueryConnection</code> instance with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(String, int, File, File, File, boolean, int, boolean, boolean, long, TimeUnit)
     */
    public static DpQueryConnection connect(
            String  strHost, 
            int     intPort, 
            File    fileTrustedCerts,
            File    fileClientCertsChain,
            File    fileClientKey,
            boolean bolPlainText,
            int     intMsgSizeMax,
            boolean bolKeepAlive,
            boolean bolGzipCompr,
            long    lngTimeout,
            TimeUnit tuTimeout
            ) throws DpGrpcException {
        
        return DpQueryConnection.from(FAC.connect(strHost, intPort, 
                fileTrustedCerts, 
                fileClientCertsChain, 
                fileClientKey, 
                bolPlainText, 
                intMsgSizeMax, 
                bolKeepAlive, 
                bolGzipCompr, 
                lngTimeout, 
                tuTimeout));
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Prevents creation of <code>DpQueryConnectionFactoryStatic</code> instances.
     * </p>
     */
    private DpQueryConnectionFactoryStatic(DpGrpcConnectionConfig cfgDefault) {
    }

}
