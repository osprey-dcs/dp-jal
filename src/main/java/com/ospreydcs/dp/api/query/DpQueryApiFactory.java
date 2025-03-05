/*
 * Project: dp-api-common
 * File:	DpQueryApiFactory.java
 * Package: com.ospreydcs.dp.api.query
 * Type: 	DpQueryApiFactory
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
 * @since Dec 27, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.impl.DpQueryServiceApiFactory;

/**
 * <h2>Connection factory for Data Platform Query Service APIs</h2>
 * <p>
 * Connection factory supplying the Java applications programming interfaces to the Data Platform Query Service.
 * Currently the following interface is available:
 * <ol>
 * <li>
 * <code>{@link IQueryService}</code> - Supports all Query Service requests, time-series and metadata. 
 * </li>
 * </ol>
 * Both RPC unary and gRPC streaming time-series data requests are available within the above interface.
 * Also the interface uses the following classes for Query Service requests:
 * <ol>
 * <li> 
 * <code>{@link DpDataRequest}</code> - Supports time-series data requests (both unary and streaming).
 * </li>
 * <li>
 * <code>{@link DpMetadataRequest}</code> - Supports archive metadata requests (which are always unary).
 * </li>
 * </ol>
 * </p>
 * <p>
 * <h2>Interface Configuration</h2>
 * There are multiple interface creation methods for each API type.  These method allow clients to supply their
 * own connection target or API connection parameters.  
 * </p>
 * <p>
 * All other configuration parameters for the created Query Service API instances are taken from the default 
 * library configuration <code>{@link DpApiConfig}</code>.  Site installations can tune these parameters for
 * specific platforms within the configuration file <code>{@value DpApiConfig#STR_CFG_FILE}</code>.
 * </p>
 * <p>
 * <h2>API Shut Downs</h2>
 * All API interface implementations are returned in the connected state.  That is, the gRPC connection to the
 * host service is established.  This connection <em>must be</em> terminated before discarding a returned interface.
 * All APIs offer shut down operations for this task.  A interface that is never shut down will unnecessarily 
 * consume gRPC and other resources potentially creating significant performance degradation.
 * </p> 
 *
 *
 * @author Christopher K. Allen
 * @since Dec 27, 2024
 *
 */
public class DpQueryApiFactory {

    
    //
    // Class Resources
    //
    
    /** The API connection factory for connection to the Query Service */
    private static final DpQueryServiceApiFactory   FAC_SERVICE = DpQueryServiceApiFactory.FACTORY;
    
    
    //
    // IQueryService Connections
    //
    
    /**
     * <p>
     * Create and return a connected Query Service interface with all default parameters.
     * </p>
     * <p>
     * The returned instance is connected to the default Query Service server, has all default connection
     * parameters, and has all default interface configuration parameters.
     * </p>
     * 
     * @return  new, connected <code>IQueryService</code> implementation ready for data requests
     * 
     * @throws DpGrpcException  general gRPC resource or connection exception (see detail and cause)
     */
    public static IQueryService    connect() throws DpGrpcException {
        return FAC_SERVICE.connect();
    }
    
    /**
     * <p>
     * Create and return a connected Query Service interface connected to given service address.
     * </p>
     * <p>
     * This method allows clients to connect to an Query Service server other than the default.
     * If the server address is invalid an exception will be thrown.
     * </p>
     * 
     * @param strHostUrl    network URL of the desired service
     * @param intHostPort   server port used by the at the above service
     * 
     * @return  new, connected <code>IQueryService</code> implementation ready for data requests
     * 
     * @throws DpGrpcException  general gRPC resource or connection exception (see detail and cause)
     */
    public static IQueryService    connect(String strHostUrl, int intHostPort) throws DpGrpcException {
        return FAC_SERVICE.connect(strHostUrl, intHostPort);
    }
    
    /**
     * <p>
     * Create and return a connected Query Service interface connected to given service address with plain text 
     * transmission option.
     * </p>
     * <p>
     * The use of "plain text" data transmission mode is available for network debugging.  In general this option
     * should be avoided due to lack of security and its poor performance. 
     * </p>
     * 
     * @param strHostUrl    network URL of the desired service
     * @param intHostPort   server port used by the at the above service
     * @param bolPlainText  transmit data using plain ASCII (negates any TLS security)
     * 
     * @return  new, connected <code>IQueryService</code> implementation ready for data requests
     * 
     * @throws DpGrpcException  general gRPC resource or connection exception (see detail and cause)
     */
    public static IQueryService    connect(String strHostUrl, int intHostPort, boolean bolPlainText) throws DpGrpcException {
        return FAC_SERVICE.connect(strHostUrl, intHostPort, bolPlainText);
    }
    
    /**
     * <p>
     * Create and return a connected Query Service interface connected to given service address
     * with plain text transmission option and service connection timeout parameters.
     * </p>
     * <p>
     * The use of "plain text" data transmission mode is available for network debugging.  In general this option
     * should be avoided due to lack of security and its poor performance.
     * </p>
     * <p>
     * The timeout parameters allow clients to specify desired Query Service connection timeout limits.
     * This action may be useful in situations with long pauses between request operations (i.e., hours) and
     * network interruptions.
     * Normally, gRPC pings connections to determine status.  If a ping does not response gRPC shut down operations
     * may proceed.  
     * </p>
     * 
     * @param strHostUrl    network URL of the desired service
     * @param intHostPort   server port used by the at the above service
     * @param bolPlainText  transmit data using plain ASCII (negates any TLS security)
     * @param lngTimeout    timeout limit used for connection operations (keepalive ping timeout) 
     * @param tuTimeout     timeout units used for connection operations (keepalive ping timeout)
     * 
     * @return  new, connected <code>IQueryService</code> implementation ready for data request
     * 
     * @throws DpGrpcException  general gRPC resource or connection exception (see detail and cause)
     */
    public static IQueryService    connect(String strHostUrl, int intHostPort, boolean bolPlainText, long lngTimeout, TimeUnit tuTimeout) throws DpGrpcException {
        return FAC_SERVICE.connect(strHostUrl, intHostPort, bolPlainText, lngTimeout, tuTimeout);
    }
    
    /**
     * <p>
     * Create and return a connected Query Service interface with all available connection parameters specified.
     * </p>
     * <p>
     * This method allows clients to specify all gRPC connection parameters.  It also provides the ability to
     * enable/disable the default Transport Layer Security (TLS) established by the current platform. 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * No default connection parameters connection are used.
     * </li>
     * <li>
     * Note that Transport Layer Security (TLS) is inherently disabled with the <code>bolPlainText</code> 
     * argument set to <code>true</code>.  Otherwise TLS security is enforced using the argument 
     * <code>bolTlsActive</code>.
     * </li>
     * <li>
     * Use of "plain text" data transmission mode is available for network debugging.  In general this option
     * should be avoided due to lack of security and its poor performance.
     * </li>
     * <li>
     * The maximum message size parameter allows clients to specify their own maximum limit for gRPC messages
     * sent over the wire.  The default gRPC value is 4 Mbytes (i.e., 2<sup>22</sup>).  If a custom limit is
     * used this value <b>must</b> match that of the server, otherwise transmission faces rejection.  All
     * maximum message size values must be a power of 2.
     * </li>
     * <li>
     * The "keep alive" parameter allows clients to maintain the gRPC connection in "active mode".  Normally,
     * gRPC will switch to "idle mode" after it detects no client activity.  Note that forcing active mode allows
     * faster response to client activity but may consume unneeded gRPC resources.
     * </li>
     * <li>
     * Use of GZIP compression has been found to significantly reduce transmission performance due
     * to large CPU loading of the compression algorithm.
     * </li>
     * <li>
     * The timeout parameters allow clients to specify desired Ingestion Service connection timeout limits.
     * This action may be useful in situations with long pauses between ingestion operations (i.e., hours) and
     * network interruptions.
     * Normally, gRPC pings connections to determine status.  If a ping does not response gRPC shut down operations
     * may proceed.  
     * </li>
     * <li>
     * All interface configuration parameters are default values taken from <code>{@link DpApiConfig}</code>.
     * </li>
     * </ul>
     * </p>
     * 
     * @param strHostUrl    network URL of the desired service
     * @param intHostPort   server port used by the at the above service
     * @param bolTlsActive  use default TLS security (false = insecure channel)
     * @param bolPlainText  transmit data using plain ASCII (negates any TLS security)
     * @param intMsgSizeMax maximum message size for gRPC transmission (bytes)
     * @param bolKeepAlive  keep connection without call activity (no idle mode)
     * @param bolGzipCompr  enable GZIP compress for data transmission
     * @param lngTimeout    timeout limit used for connection operations (keepalive ping timeout) 
     * @param tuTimeout     timeout units used for connection operations (keepalive ping timeout)
     * 
     * @return  new, connected <code>IIngestionService</code> implementation ready for data requests
     * 
     * @throws DpGrpcException  general gRPC resource or connection exception (see detail and cause)
     */
    public static IQueryService    connect(String strHostUrl, 
                                                      int intHostPort, 
                                                      boolean bolTisActive, 
                                                      boolean bolPlainText, 
                                                      int intMsgSizeMax, 
                                                      boolean bolKeepAlive, 
                                                      boolean bolGzipCompr, 
                                                      long lngTimeout, 
                                                      TimeUnit tuTimeout) throws DpGrpcException {
        return FAC_SERVICE.connect(strHostUrl, intHostPort, bolTisActive, bolPlainText, intMsgSizeMax, bolKeepAlive, bolGzipCompr, lngTimeout, tuTimeout);
    }
    
    
    //
    // IQueryService Connections with Explicit Security
    //
    
    /**
     * <p>
     * Create and return a connected Query Service interface connected to the default Query Service host
     * using the given explicit security parameters.
     * </p>
     * <p>
     * The returned instance is connected to the default Query Service server, has all default connection
     * parameters, has all default interface configuration parameters, and uses the given files defining 
     * TLS security for the communications channel.
     * </p>
     * 
     * @param fileTrustedCerts  root file of all trusted server certificates
     * @param fileClientCerts   file of client certificates
     * @param fileClientKey     file containing client private key
     * 
     * @return  new, connected <code>IIngestionService</code> implementation ready for data requests
     * 
     * @throws DpGrpcException  general gRPC resource or connection exception (see detail and cause)
     */
    public static IQueryService    connect(File fileTrustedCerts, File fileClientCerts, File fileClientKey) throws DpGrpcException {
        return FAC_SERVICE.connect(fileTrustedCerts, fileClientCerts, fileClientKey);
    }
    
    /**
     * <p>
     * Create and return a connected Query Service interface connected to the given Query Service 
     * host using the given explicit security parameters.
     * </p>
     * <p>
     * The returned instance is connected to the Query Service server at the given URL and and port address. 
     * It has all default connection parameters, has all default interface configuration parameters, and uses the 
     * given files defining TLS security for the communications channel.
     * </p>
     * 
     * @param strHostUrl        network URL of the desired service
     * @param intHostPort       server port used by the at the above service
     * @param fileTrustedCerts  root file of all trusted server certificates
     * @param fileClientCerts   file of client certificates
     * @param fileClientKey     file containing client private key
     * 
     * @return  new, connected <code>IIngestionService</code> implementation ready for data requests
     * 
     * @throws DpGrpcException  general gRPC resource or connection exception (see detail and cause)
     */
    public static IQueryService connect(String strHostUrl, int intHostPort, File fileTrustedCerts, File fileClientCerts, File fileClientKey) throws DpGrpcException {

        return FAC_SERVICE.connect(strHostUrl, intHostPort, fileTrustedCerts, fileClientCerts, fileClientKey);
    }
    
    /**
     * <p>
     * Create and return a connected Query Service interface with all connection parameters specified
     * and the given explicit TLS security.
     * </p>
     * <p>
     * This method allows clients to specify all gRPC connection parameters.  It also provides the ability to
     * explicitly specify the Transport Layer Security (TLS) established by the connection channel. 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * No default connection parameters connection are used.
     * </li>
     * <li>
     * Note that Transport Layer Security (TLS) is inherently disabled with the <code>bolPlainText</code> 
     * argument set to <code>true</code>.  Otherwise TLS security is enforced using the argument 
     * <code>bolTlsActive</code>.
     * </li>
     * <li>
     * Use of "plain text" data transmission mode is available for network debugging.  In general this option
     * should be avoided due to lack of security and its poor performance.
     * </li>
     * <li>
     * The maximum message size parameter allows clients to specify their own maximum limit for gRPC messages
     * sent over the wire.  The default gRPC value is 4 Mbytes (i.e., 2<sup>22</sup>).  If a custom limit is
     * used this value <b>must</b> match that of the server, otherwise transmission faces rejection.  All
     * maximum message size values must be a power of 2.
     * </li>
     * <li>
     * The "keep alive" parameter allows clients to maintain the gRPC connection in "active mode".  Normally,
     * gRPC will switch to "idle mode" after it detects no client activity.  Note that forcing active mode allows
     * faster response to client activity but may consume unneeded gRPC resources.
     * </li>
     * <li>
     * Use of GZIP compression has been found to significantly reduce transmission performance due
     * to large CPU loading of the compression algorithm.
     * </li>
     * <li>
     * The timeout parameters allow clients to specify desired Query Service connection timeout limits.
     * This action may be useful in situations with long pauses between query operations (i.e., hours) and
     * network interruptions.
     * Normally, gRPC pings connections to determine status.  If a ping does not response gRPC shut down operations
     * may proceed.  
     * </li>
     * <li>
     * The arguments of type <code>File</code> specify the location of the the TLS security resources used by
     * the gRPC channel connection, that is, the file of all trusted server certificates, the file of client
     * certificates, and the file containing the client's private key, respectively.  This option is valuable
     * if clients wish to use explicit TLS certification other than the default values of the current platform.  
     * If default values are all that is required consider method 
     * <code>{@link #connect(String, int, boolean, boolean, int, boolean, boolean, long, TimeUnit)}</code>.
     * </li> 
     * <li>
     * All interface configuration parameters are default values taken from <code>{@link DpApiConfig}</code>.
     * </li>
     * </ul>
     * </p>
     * 
     * @param strHostUrl        network URL of the desired service
     * @param intHostPort       server port used by the at the above service
     * @param fileTrustedCerts  root file of all trusted server certificates
     * @param fileClientCerts   file of client certificates
     * @param fileClientKey     file containing client private key
     * @param bolPlainText      transmit data using plain ASCII (negates any TLS security)
     * @param intMsgSizeMax     maximum message size for gRPC transmission (bytes)
     * @param bolKeepAlive      keep connection without call activity (no idle mode)
     * @param bolGzipCompr      enable GZIP compress for data transmission
     * @param lngTimeout        timeout limit used for connection operations (keepalive ping timeout) 
     * @param tuTimeout         timeout units used for connection operations (keepalive ping timeout)
     * 
     * @return  new, connected <code>IIngestionService</code> implementation ready for data requests
     * 
     * @throws DpGrpcException  general gRPC resource or connection exception (see detail and cause)
     */
    public static IQueryService connect(
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
        
        return FAC_SERVICE.connect(strHost, intPort, fileTrustedCerts, fileClientCertsChain, fileClientKey, bolPlainText, intMsgSizeMax, bolKeepAlive, bolGzipCompr, lngTimeout, tuTimeout);
    }
    
    
    //
    // Support Methods
    // 
    
    /**
     * <p>
     * Prevent construction of <code>DpQueryApiFactory</code> instances.
     * </p>
     *
     */
    private DpQueryApiFactory() {
    }

}
