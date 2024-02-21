/*
 * Project: dp-api-common
 * File:	DpIngestionConnectionFactoryDeprecated.java
 * Package: com.ospreydcs.dp.api.grpc.ingest
 * Type: 	DpIngestionConnectionFactoryDeprecated
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
package com.ospreydcs.dp.api.grpc.ingest;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactory;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceStub;


/**
 * <p>
 * Utility class for creating client connections to the Data Platform Ingestion Service.
 * </p>
 * <p>
 * This is a fly weight class that maintains a static instance of <code>DpGrpcConnectionFactory</code> bound
 * to the Data Platform Ingestion Service.  All connection operations are delegated to the static instance.
 * See the {@link DpGrpcConnectionFactory} documentation for more details.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Dec 28, 2023
 *
 * @see DpGrpcConnectionFactory
 * 
 * @deprecated  Please use DpIngestionConnectionFactory instead.
 */
@Deprecated(since="Jan 15, 2024", forRemoval=true)
public final class DpIngestionConnectionFactoryDeprecated{

    
    //    
    // Application Resources
    //
    
    /** The API Library default Ingestion Service configuration parameters */
    private static final DpGrpcConnectionConfig   CFG_DEFAULT = DpApiConfig.getInstance().connections.ingestion;

    
    //
    // Class Resources
    //

    /** The <code>DpGrpcConnectionFactory</code> bound for the Ingestion Service */
    private static final DpGrpcConnectionFactory<DpIngestionServiceGrpc, DpIngestionServiceBlockingStub, DpIngestionServiceFutureStub, DpIngestionServiceStub> FAC;
    
    static {
        FAC = DpGrpcConnectionFactory.newFactory(DpIngestionServiceGrpc.class, CFG_DEFAULT);
    }
             
    
    //
    // Default TLS Connections
    //
    
    /**
     * <p>
     * Creates a new <code>DpIngestionConnection</code> instance using default parameters.
     * </p>
     * <p>
     * See {@link DpGrpcConnectionFactory#connect()} for details.
     * </p>
     *
     * @see com.ospreydcs.dp.api.grpc.DpGrpcConnectionFactory#connect()
     */
    public static DpIngestionConnection connect() throws DpGrpcException {
        
        DpIngestionConnection conn = DpIngestionConnection.from(FAC.connect());
        
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
     * @see com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactory#connect(java.lang.String, int)
     */
    public static DpIngestionConnection connect(String strHost, int intPort) throws DpGrpcException {
        return DpIngestionConnection.from(FAC.connect(strHost, intPort));
    }

    /**
     * <p>
     * Creates a new <code>DpIngestionConnection</code> instance for the given parameters.
     * </p>
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, boolean)} for details.
     * </p>
     *
     * @see com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactory#connect(java.lang.String, int, boolean)
     */
    public static DpIngestionConnection connect(String strHost, int intPort, boolean bolPlainText) throws DpGrpcException {
        return DpIngestionConnection.from(FAC.connect(strHost, intPort, bolPlainText));
    }

    /**
     * <p>
     * Creates a new <code>DpIngestionConnection</code> instance for the given parameters.
     * </p>
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, boolean, long, TimeUnit)} for details.
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       server port used by the at the above service
     * @param bolPlainText  transmit data using plain ASCII (negates all TLS security)
     * @param lngTimeout    timeout limit used for connection operations (keepalive ping timeout) 
     * @param tuTimeout     timeout units used for connection operations (keepalive ping timeout)
     * 
     * @return new <code>DpIngestionConnection</code> instance connected to the given host
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see {@link com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactory#connect(String, int, boolean, long, TimeUnit)}
     */
    public static DpIngestionConnection connect(String strHost, int intPort, boolean bolPlainText, long lngTimeout, TimeUnit tuTimeout) throws DpGrpcException {
        return DpIngestionConnection.from(FAC.connect(strHost, intPort, bolPlainText, lngTimeout, tuTimeout));
    }

    /**
     * <p>
     * Creates a new <code>DpIngestionConnection</code> instance for the given parameters.
     * </p>
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, boolean, boolean, int, boolean, boolean, long, TimeUnit)} for details.
     * </p>
     * <p>
     * No default parameters are used.
     * </p>
     * 
     * @see DpGrpcConnectionFactory#connect(String, int, boolean, boolean, int, boolean, boolean, long, TimeUnit)
     */
    public static DpIngestionConnection connect(
            String strHost, 
            int intPort, 
            boolean bolTlsActive,
            boolean bolPlainText,
            int     intMsgSizeMax,
            boolean bolKeepAlive,
            boolean bolGzipCompr,
            long    lngTimeout,
            TimeUnit tuTimeout
            ) throws DpGrpcException 
    {
        return DpIngestionConnection.from(FAC.connect(strHost, intPort, bolTlsActive, bolPlainText, intMsgSizeMax, bolKeepAlive, bolGzipCompr, lngTimeout, tuTimeout));
    }
    
    
    //
    // Explicit TLS Security Connections
    //

    /**
     * <p>
     * Creates a new <code>DpIngestionConnection</code> instance for the given parameters.
     * </p>
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(File, File, File)} for details.
     * </p>
     *
     * @return new <code>DpIngestionConnection</code> instance connected to the default address
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(File, File, File)
     */
    public static DpIngestionConnection connect(File fileTrustedCerts, File fileClientCerts, File fileClientKey) throws DpGrpcException {
        return DpIngestionConnection.from(FAC.connect(fileTrustedCerts, fileClientCerts, fileClientKey));
    }

    /**
     * <p>
     * Creates a new <code>DpIngestionConnection</code> instance for the given parameters.
     * </p>
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, File, File, File)} for details.
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       server port used by the at the above service
     * @param fileTrustedCerts root file of all trusted server certificates
     * @param fileClientCerts  file of client certificates
     * @param fileClientKey file containing client private key
     * 
     * @return new <code>DpIngestionConnection</code> instance connected to the given address
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see DpGrpcConnectionFactory#connect(String, int, File, File, File)
     */
    public static DpIngestionConnection connect(String strHost, int intPort, File fileTrustedCerts, File fileClientCerts, File fileClientKey) throws DpGrpcException {
        return DpIngestionConnection.from(FAC.connect(strHost, intPort, fileTrustedCerts, fileClientCerts, fileClientKey));
    }

    /**
     * <p>
     * Creates a new <code>DpIngestionConnection</code> instance for the given parameters.
     * </p>
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, File, File, File, boolean, int, boolean, boolean, long, TimeUnit)} for details.
     * </p>
     * </p>
     * No default parameters are used.
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       network port used by the at the above service
     * @param fileTrustedCerts root file of all trusted server certificates
     * @param fileClientCerts  file of client certificates
     * @param fileClientKey file containing client private key
     * @param bolPlainText transmit data using plain ASCII (negates all TLS security)
     * @param intMsgSizeMax maximum message size for gRPC transmission (bytes)
     * @param bolKeepAlive force connection to remain active (otherwise idle after timeout)
     * @param bolGzipCompr enable GZIP compression for data transmission
     * @param lngTimeout   timeout limit used for channel operations (keepalive ping)
     * @param tuTimeout    timeout units used for channel operations (Keepalive ping)
     * 
     * @return new <code>DsGrpcConnection</code> instance connected to the given address
     *         and using the given configuration parameters
     *         
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(String, int, File, File, File, boolean, int, boolean, boolean, long, TimeUnit)
     */
   public static DpIngestionConnection connect(
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
           ) throws DpGrpcException 
   {
       return DpIngestionConnection.from(FAC.connect(strHost, intPort, 
               fileTrustedCerts, 
               fileClientCertsChain, 
               fileClientKey, 
               bolPlainText,
               intMsgSizeMax, 
               bolKeepAlive, 
               bolGzipCompr, 
               lngTimeout, 
               tuTimeout)
               );
   }
   
   
   //
   // Private Support
   //
   
   /**
    * <p>
    * Prevents creation of <code>DpIngestionConnectionFactoryDeprecated</code> instances.
    * </p>
    */
   private DpIngestionConnectionFactoryDeprecated() {
   }

}
