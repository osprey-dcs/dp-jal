/*
 * Project: dp-api-common
 * File:	DpAnnotationConnectionFactoryStatic.java
 * Package: com.ospreydcs.dp.api.grpc.annotate
 * Type: 	DpAnnotationConnectionFactoryStatic
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
 * @since Feb 6, 2025
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.annotate;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactory;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;

/**
 * <p>
 * Utility class for creating client connections to the Data Platform Query Service.
 * </p>
 * <p>
 * This is a fly weight class that maintains a static instance of <code>DpGrpcConnectionFactory</code> bound
 * to the Data Platform Annotation Service.  All connection operations here are provided by static methods which
 * delegate to the static factory instance <code>{@link FAC}</code>.
 * See the {@link DpGrpcConnectionFactory} documentation for more details.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Feb 6, 2025
 *
 */
public class DpAnnotationConnectionFactoryStatic {

    /** Convenience reference to the Annotation Service connection factory singleton */
    private static final DpAnnotationConnectionFactory  FAC = DpAnnotationConnectionFactory.FACTORY;
    
    
    //
    // Factory Operations - Use Security Specified in Default Configuration 
    //
    
    /**
     * <p>
     * Creates and returns a new <code>DpAnnotationConnection</code> instance connected
     * to the <em>Data Platform</em> Annotation Service.
     * </p>
     * <p>
     * All configuration parameters are taken from the default connection
     * properties, including the service host location.
     * Defers to method <code>{@link DpAnnotationConnectionFactory#connect()}</code>.
     * </p>
     * 
     * @return new <code>Connection</code> instance with all default configuration parameters
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)
     *   
     * @see DpAnnotationConnectionFactory#connect()
     * @see DpGrpcConnectionConfig
     */
    public static DpAnnotationConnection    connect() throws DpGrpcException {
        return FAC.connect();
    }
    
    /**
     * <p>
     * Creates and returns a new <code>Connection</code> instance connected
     * to the <em>Data Platform</em> Annotation Service.
     * </p>
     * <p>
     * The URL and port address of the <em>Data Platform</em> Annotation Service are explicitly specified.
     * All other configuration parameters are taken from the default configuration parameters.
     * Defers to method <code>{@link DpAnnotationConnectionFactory#connect(String, int)}</code>.
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       server port used by the at the above service
     * 
     * @return new <code>DpAnnotationConnection</code> instance connected to the given address
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see DpAnnotationConnectionFactory#connect(String, int)
     * @see DpGrpcConnectionConfig
     */
    public static DpAnnotationConnection connect(String strHost, int intPort) throws DpGrpcException {
        return FAC.connect(strHost, intPort);
    }

    /**
     * <p>
     * Creates and returns a new <code>DpAnnotationConnection</code> instance connected
     * to the <em>Data Platform</em> Annotation Service.
     * </p>
     * <p>
     * The URL and port address of the <em>Data Platform</em> service are explicitly specified,
     * along with the Plain Text security override flag.
     * All other configuration parameters are taken from the default configuration parameters.
     * Defers to method <code>{@link DpAnnotationConnectionFactory#connect(String, int, boolean, long)}</code>.
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       server port used by the at the above service
     * @param bolPlainText  transmit data using plain ASCII (negates any TLS security)
     * 
     * @return new <code>Connection</code> instance connected to the given address
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see DpAnnotationConnectionFactory#connect(String, int, boolean, long)
     * @see DpGrpcConnectionConfig
     */
    public static DpAnnotationConnection connect(String strHost, int intPort, boolean bolPlainText) throws DpGrpcException {
        return FAC.connect(strHost, intPort, bolPlainText);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DpAnnotationConnection</code> instance connected
     * to the <em>Data Platform</em> Annotation Service.
     * </p>
     * <p>
     * The URL and port address of the <em>Data Platform</em> Annotation Service are explicitly specified,
     * along with the timeout parameters.
     * All other configuration parameters are taken from the default connection
     * properties.
     * </p>
     * <p>
     * <h2>IMPORTANT:</h2>
     * This method decides upon <em>default</em> TLS security or <em>explicit</em> TLS security.
     * <br/><br/>
     * The decision depends upon the default configuration parameter 
     * <code>DpGrpcConnectionConfig#channel.tls.defaultTls</code> in the API library configuration file.
     * <br/><br/>
     * It defers to the following methods depending on the value of this parameter:
     * <ul>
     * <li> 
     * <code>true</code> - method <code>{@link #connect(String, int, boolean, boolean, int, boolean, boolean, long, TimeUnit)}</code>
     * </li>
     * <li>
     * <code>false</code> - method <code>{@link #connect(String, int, File, File, File, int, boolean, boolean, long, TimeUnit)}</code>
     * </li>
     * </ul>
     * In the latter case when default configured for <em>explicit</em> TLS security (using certificate and key files).
     * the file names are extracted from the default configuration.
     * </p> 
     * <h2>NOTES:</h2>
     * <ul>
     * <li>If the default configuration parameter <code>DpGrpcConnectionConfig#channel.tls.active</code> 
     * is set to <code>false</code> then the method 
     * <code>{@link #connect(String, int, boolean, boolean, int, boolean, boolean, long, TimeUnit)}</code>
     * is always called with parameter <code>bolTlsActive</code> set to <code>false</code>.
     * </li>
     * <br/> 
     * <li>
     * If the <code>bolPlainText</code> argument is set to <code>true</code> then any TLS security 
     * is automatically <em>negated</em>.
     * </li>
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       server port used by the at the above service
     * @param bolPlainText  transmit data using plain ASCII (negates any TLS security)
     * @param lngTimeout    timeout limit used for connection operations (keepalive ping timeout) 
     * @param tuTimeout     timeout units used for connection operations (keepalive ping timeout)
     * 
     * @return new <code>DpAnnotationConnection</code> instance connected to the given address with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see DpGrpcConnectionConfig
     * @see DpAnnotationConnectionFactory#connect(String, int, boolean, boolean, int, boolean, boolean, long, TimeUnit)
     * @see DpAnnotationConnectionFactory#connect(String, int, File, File, File, boolean, int, boolean, boolean, long, TimeUnit)
     */
    public static DpAnnotationConnection connect(
            String strHost, 
            int intPort, 
            boolean bolPlainText, 
            long lngTimeout, 
            TimeUnit tuTimeout
            ) throws DpGrpcException 
    {
        return FAC.connect(strHost, intPort, bolPlainText, lngTimeout, tuTimeout);
    }
    
    
    //
    // Factory Operations - Default TLS Security
    //
    
    /**
     * <p>
     * Creates and returns a new <code>DpAnnotationConnection</code> instance connected
     * to the <em>Data Platform</em> Annotation Service. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Note that Transport Layer Security (TLS) is inherently disabled with the <code>bolPlainText</code> 
     * argument set to <code>true</code>.  Otherwise TLS security is enforced when setting the argument 
     * <code>bolTlsActive</code> to <code>true</code>.
     * </li>
     * <li>
     * No default parameters connection are used.
     * </li>
     * </ul>
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       server port used by the at the above service
     * @param bolTlsActive  use default TLS security (false = insecure channel)
     * @param bolPlainText  transmit data using plain ASCII (negates any TLS security)
     * @param intMsgSizeMax maximum message size for gRPC transmission (bytes)
     * @param bolKeepAlive  keep connection without call activity (no idle mode)
     * @param bolGzipCompr  enable GZIP compress for data transmission
     * @param lngTimeout    timeout limit used for connection operations (keepalive ping timeout) 
     * @param tuTimeout     timeout units used for connection operations (keepalive ping timeout)
     * 
     * @return new <code>Connection</code> instance connected to the given address
     *         and using the given configuration parameters
     *         
     * @throws DpGrpcException error while creating connection object (see message and cause)
     * 
     * @see DpAnnotationConnectionFactory#connect(String, int, boolean, boolean, int, boolean, boolean, long, TimeUnit)
     */
    public static DpAnnotationConnection connect(
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
        return FAC.connect(strHost, intPort, bolTlsActive, bolPlainText, intMsgSizeMax, bolKeepAlive, bolGzipCompr, lngTimeout, tuTimeout);
    }
    
    //
    // Factory Operations - Explicit TLS Security Options
    //
    
    /**
     * <p>
     * Creates and returns a new <code>DpAnnotationConnection</code> instance connected
     * to the <em>Data Platform</em> Annotation Service.
     * </p>
     * <p>
     * TLS security is explicitly specified with full authorization given by TLS certificates and
     * key files.
     * The URL and port address of the <em>Data Platform</em> Annotation Service are taken from the default parameters.
     * All other configuration parameters are taken from the default connection properties.
     * Defers to method <code>{@link DpAnnotationConnectionFactory#connect(File, File, File)}</code>
     * </p>
     * <h2>NOTES:</h2>
     * If the <code>DpGrpcConnectionConfig.channel.grpc.usePlainText</code> default parameter is
     * set to <code>true</code> all TLS security is <b>negated</b>.
     * </p>
     * 
     * @param fileTrustedCerts root file of all trusted server certificates
     * @param fileClientCerts  file of client certificates
     * @param fileClientKey file containing client private key
     * 
     * @return new <code>DpAnnotationConnection</code> instance connected to the default address with given TLS parameters
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see DpGrpcConnectionConfig
     * @see DpAnnotationConnectionFactory#connect(File, File, File)
     */
    public static DpAnnotationConnection connect(File fileTrustedCerts, File fileClientCerts, File fileClientKey) throws DpGrpcException {
    
        return FAC.connect(fileTrustedCerts, fileClientCerts, fileClientKey);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DpAnnotationConnection</code> instance connected
     * to the <em>Data Platform</em> Annotation Service.
     * </p>
     * <p>
     * The URL and port address of the <em>Data Platform</em> service are explicitly specified.
     * TLS security is explicitly specified with full authorization given by TLS certificates and
     * key files.
     * All other configuration parameters are taken from the default connection
     * properties.
     * Defers to method <code>{@link DpAnnotationConnectionFactory#connect(String, int, File, File, File, boolean, int, boolean, boolean, long, TimeUnit)}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * If the <code>DpGrpcConnectionConfig.channel.grpc.usePlainText</code> default parameter is
     * set to <code>true</code> all TLS security is <b>negated</b>.
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       server port used by the at the above service
     * @param fileTrustedCerts root file of all trusted server certificates
     * @param fileClientCerts  file of client certificates
     * @param fileClientKey file containing client private key
     * 
     * @return new <code>DoAnnotationConnection</code> instance connected to the given address with given TLS security
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see DpGrpcConnectionConfig
     * @see DpAnnotationConnectionFactory#connect(String, int, File, File, File)
     */
    public static DpAnnotationConnection connect(
            String strHost, 
            int intPort, 
            File fileTrustedCerts, 
            File fileClientCerts, 
            File fileClientKey
            ) throws DpGrpcException {
    
        return FAC.connect(strHost, intPort, fileTrustedCerts, fileClientCerts, fileClientKey);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>Connection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>ServiceGrpc</code>.
     * </p>
     * <p>
     * Transport Layer Security (TLS) is assumed here and parameters required for full authorization
     * are required.
     * </p>
     * No default parameters are used.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * If the <code>bolPlainText</code> argument is
     * set to <code>true</code> all TLS security is <b>negated</b>.
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       network port used by the at the above service
     * @param fileTrustedCerts root file of all trusted server certificates
     * @param fileClientCerts  file of client certificates
     * @param fileClientKey file containing client private key
     * @param bolPlainText  transmit data using plain ASCII (negates all TLS security)
     * @param intMsgSizeMax maximum message size for gRPC transmission (bytes)
     * @param bolKeepAlive force connection to remain active (otherwise idle after timeout)
     * @param bolGzipCompr enable GZIP compression for data transmission
     * @param lngTimeout   timeout limit used for channel operations (keepalive ping)
     * @param tuTimeout    timeout units used for channel operations (Keepalive ping)
     * 
     * @return new <code>Connection</code> instance connected to the given address
     *         and using the given configuration parameters
     *         
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     */
    public static DpAnnotationConnection connect(
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
        return FAC.connect(strHost, intPort, fileTrustedCerts, fileClientCertsChain, fileClientKey, bolPlainText, intMsgSizeMax, bolKeepAlive, bolGzipCompr, lngTimeout, tuTimeout);
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Prevents construction of a <code>DpAnnotationConnectionFactoryStatic</code> instance.
     * </p>
     *
     */
    private DpAnnotationConnectionFactoryStatic() {
    }

}
