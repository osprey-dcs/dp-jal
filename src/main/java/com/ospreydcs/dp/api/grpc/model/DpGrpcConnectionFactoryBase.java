/*
 * Project: dp-api-common
 * File:	DpGrpcConnectionFactoryBase.java
 * Package: com.ospreydcs.dp.api.grpc.model
 * Type: 	DpGrpcConnectionFactoryBase
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
package com.ospreydcs.dp.api.grpc.model;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.TlsChannelCredentials;

/**
 * <p>
 * Abstract base class for factories creating <code>DsGrpcConnection</code> derived instances.
 * </p>
 * <p>
 * This base class is set up to create active gRPC connections represented by generic parameter 
 * type <code>Connection</code>.  The <code>Connection</code> type must be derived from 
 * <code>{@link DpGrpcConnection}</code>.  Thus, the requirement for all additional generic type
 * parameters. 
 * </p>
 * <p>
 * Connection factories provide multiple <code>connect(...)</code> methods with various argument
 * signatures that determine combinations of specified connection parameters and default
 * connection parameters.  For example, the method {@link #connect()} returns an active 
 * <code>Connection</code> instance with all default connection parameters.  The default
 * connection parameters are provided to the class constructor and are of type
 * <code>{@link DpGrpcConnectionConfig}</code>.
 * </p>
 * <p>
 * Derived classes must implement the following methods:
 * <ul>
 * <li>
 * <code>{@link #DpGrpcConnectionFactoryBase(Class, DpGrpcConnectionConfig)}</code> - The base
 * class must implement a constructor that supplies the arguments for the <code>ServiceGrpc</code>
 * type and the default parameters used for gRPC connections.
 * </li>
 * <br/>
 * <li>
 * <code>{@link #createFrom(DpGrpcConnection)}</code> - Creates a new <code>Connection</code>
 * instance of the proper type from a <code>DpGrpcConnection</code> instance with correct
 * type bindings.
 * </li>
 * </ul>
 * </p>
 * <p>
 * <h2>Java Generics</h2>
 * Due to the use of Java generic parameters, the connection factories 
 * cannot be static classes, they must be instantiated.  However, the advantage
 * is that only one class factory type needs to be implemented which will
 * create connections to a Data Platform service.  Thus, the use of generic templates
 * consolidates the code for creating Data Platform gRPC connections. 
 * </p>
 * <p>
 * The types of the Java generics parameters determine the Data Platform service for connection.
 * For example the Ingestion Service or the Query Service may be supported with the following
 * bindings:
 * <ul>
 * Ingestion Service Connection Factory
 * <li><code>Connection</code> = <code>DpIngestionConnection</code>
 *     </li>
 * <li><code>ServiceGrpc</code> = <code>IngestionServiceGrpc</code>
 *     </li>
 * <li><code>SyncStub</code> = <code>IngestionServiceGrpc.IngestionServiceBlockingStub</code>
 *     </li>
 * <li><code>FutureStub</code> = <code>IngestionServiceGrpc.IngestionServiceFutureStub</code>
 *     </li>
 * <li><code>AsyncStub</code> = <code>IngestionServiceGrpc.IngestionServiceStub</code>
 *     </li> 
 *     <br/>
 * Query Service Connections
 * <li><code>Connection</code> = <code>DpQueryConnection</code>
 * <li><code>ServiceGrpc</code> = <code>QueryServiceGrpc</code>
 *     </li>
 * <li><code>SyncStub</code> = <code>QueryServiceGrpc.QueryServiceBlockingStub</code>
 *     </li>
 * <li><code>FutureStub</code> = <code>QueryServiceGrpc.QueryServiceFutureStub</code>
 *     </li>
 * <li><code>AsyncStub</code> = <code>QueryServiceGrpc.QueryServiceStub</code>
 *     </li> 
 * </ul>
 * Thus, for example, the following class declaration yields a connection factory 
 * <code>ExampleQueryConnectionFactory</code> 
 * class for the Data Platform Query Service:
 * <pre>
 * <code>
 * public final class ExampleQueryConnectionFactory extends DpGrpcConnectionFactoryBase&lt;
 *                       DpQueryConnection,
 *                       DpQueryServiceGrpc, 
 *                       DpQueryServiceBlockingStub,
 *                       DpQueryServiceFutureStub, 
 *                       DpQueryServiceStub&gt;
 * </code> 
 * </pre>
 * Since only one factory class instance is required to create all
 * Data Platform query service connections, the instance may be 
 * declared
 * <br/> <br/>
 * &nbsp; &nbsp;  <code>public static final ExampleQueryConnectionFactory FACTORY;</code>
 * <br/> <br/>
 * within the class.
 * </p>
 * <h2>WARNING:</h2>
 * All created <code>DpGrpcConnection</code> derived objects should be shut down when no longer 
 * needed using
 * <code>{@link #shutdownSoft()} or 
 * <code>{@link #shutdownNow()}</code>.  
 * Otherwise gRPC resources are not released and performance degrades.
 * </p>
 *  
 * @param <Connection>  gRPC Connection type to Data Platform service (sub-type of <code>DpGrpcConnection</code>)
 * @param <ServiceGrpc> the specific Data Platform service, that is, the Protobuf-generated gRPC service class  
 * @param <BlockStub>   synchronous (blocking) stub supporting unary gPRC communications 
 * @param <FutureStub>  non-blocking stub supporting unary gPRC communications 
 * @param <AsyncStub>   asynchronous (non-blocking) stub for both unary and streaming gRPC communications 
 *
 * @author Christopher K. Allen
 * @since Jan 14, 2024
 *
 * @see DpGrpcConnection
 */
public abstract class DpGrpcConnectionFactoryBase<
//    Factory extends DpGrpcConnectionFactoryBase<Factory, Connection, ServiceGrpc, BlockStub, FutureStub, AsyncStub>,
    Connection extends DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub>,
    ServiceGrpc,
    BlockStub extends io.grpc.stub.AbstractBlockingStub<BlockStub>, 
    FutureStub extends io.grpc.stub.AbstractFutureStub<FutureStub>,
    AsyncStub extends io.grpc.stub.AbstractAsyncStub<AsyncStub> 
    > 
{

    //
    // Class Constants
    //

    /** Default maximum gRPC message size - this is a gRPC parameter */
    public static final int    INT_MSG_SIZE_MAX_DEFAULT = 4194304;
    

    //
    // Class Resources
    //
    
    /** The static logging utility */
    private static final Logger LOGGER = LogManager.getLogger();

    
    //
    // Instance Attributes
    //
    
    /** The class type of the gRPC service */
    private final Class<ServiceGrpc>    clsService;
    
    /** Record containing default connection parameters */
    private final DpGrpcConnectionConfig  cfgConn;
    
    
    
    // 
    // Abstract Methods
    //
    
    /**
     * <p>
     * Create and return a <code>{@link Connection}</code> instance from the given <code>{@link DpGrpcConnection}</code> object.
     * </p>
     * <p>
     * Derived classes must implement this method to bind factory-produced instances of unbound
     * <code>DpGrpcConnection</code> to the <code>Connection</code> type. 
     * It is required that the <code>Connection</code> type is derived from the given argument type.
     * The derived factory class then produces <code>Connection</code> instances without modification.  
     * Thus, this method provides for consistent typing of factory instances from the derived factory class.
     * </p>
     *  
     * @param conn  gRPC connection support the derived connection factory class
     * 
     * @return      new <code>Connection</code> instance aliasing the argument
     * 
     * @throws DpGrpcException  general gRPC resource creation error (see message and cause) 
     */
    protected abstract Connection createFrom(DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> conn) throws DpGrpcException;

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Construct a new instance of <code>DpGrpcConnectionFactory</code> 
     * for the given service class and using the given default configuration 
     * parameters.
     * </p>
     *
     * @param clsService class type of the gRPC communications service
     * @param cfgConn    record of the default configuration parameters
     */
    protected DpGrpcConnectionFactoryBase(Class<ServiceGrpc> clsService, DpGrpcConnectionConfig cfgConn) {
        this.clsService = clsService;
        this.cfgConn    = cfgConn;
    }

    
    //
    // Factory Operations - Use Security Specified in Default Configuration 
    //
    
    /**
     * <p>
     * Creates and returns a new <code>Connection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>ServiceGrpc</code>.
     * </p>
     * <p>
     * All other configuration parameters are taken from the default connection
     * properties, including the service host location.
     * Defers to method <code>{@link #connect(String, int)}</code>
     * </p>
     * 
     * @return new <code>Connection</code> instance with all default configuration
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see #connect(String, int)
     * @see DpGrpcConnectionConfig
     */
    public Connection connect() throws DpGrpcException {
        
        return this.connect(this.cfgConn.channel.host.url, this.cfgConn.channel.host.port);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>Connection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>ServiceGrpc</code>.
     * </p>
     * <p>
     * The URL and port address of the <em>Data Platform</em> service are explicitly specified.
     * All other configuration parameters are taken from the default configuration parameters.
     * Defers to method <code>{@link #connect(String, int, boolean)}</code>.
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
     * @see #connect(String, int, boolean)
     * @see DpGrpcConnectionConfig
     */
    public Connection connect(
            String strHost, 
            int intPort 
            ) throws DpGrpcException 
    {
        return this.connect(strHost, intPort, this.cfgConn.channel.grpc.usePlainText);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>Connection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>ServiceGrpc</code>.
     * </p>
     * <p>
     * The URL and port address of the <em>Data Platform</em> service are explicitly specified,
     * along with the Plain Text security override flag.
     * All other configuration parameters are taken from the default configuration parameters.
     * Defers to method <code>{@link #connect(String, int, boolean, long, TimeUnit)}</code>.
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
     * @see #connect(String, int, boolean, long, TimeUnit)
     * @see DpGrpcConnectionConfig
     */
    public Connection connect(String strHost, int intPort, boolean bolPlainText) throws DpGrpcException 
    {
        return this.connect(strHost, intPort,
                bolPlainText,
                this.cfgConn.channel.grpc.timeoutLimit, 
                this.cfgConn.channel.grpc.timeoutUnit);        
    }
    
    /**
     * <p>
     * Creates and returns a new <code>Connection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>ServiceGrpc</code>.
     * </p>
     * <p>
     * The URL and port address of the <em>Data Platform</em> service are explicitly specified,
     * along with the timeout parameters.
     * All other configuration parameters are taken from the default connection
     * properties.
     * </p>
     * <p>
     * <h2>IMPORTANT:</h2>
     * This method decides upon <em>default</em> TLS security or <em>explicit</em> TLS security.
     * <br/><br/>
     * The decision depends upon the default configuration parameter 
     * <code>DpGrpcConnectionConfig#channel.tls.defaultTls</code>.
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
     * @return new <code>Connection</code> instance connected to the given address
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see DpGrpcConnectionConfig
     * @see #connect(String, int, boolean, boolean, int, boolean, boolean, long, TimeUnit)
     * @see #connect(String, int, File, File, File, boolean, int, boolean, boolean, long, TimeUnit)
     */
    public Connection connect(
            String strHost, 
            int intPort, 
            boolean bolPlainText, 
            long lngTimeout, 
            TimeUnit tuTimeout
            ) throws DpGrpcException 
    {

        // If we are default configured for NO TLS security 
        //  Always call default TLS connect method - then specifies an insecure channel
        if (!this.cfgConn.channel.tls.active)
            return this.connect(strHost, intPort, 
                    this.cfgConn.channel.tls.active,    // Insecure channel
                    bolPlainText, 
                    this.cfgConn.channel.grpc.messageSizeMax, 
                    this.cfgConn.channel.grpc.keepAliveWithoutCalls, 
                    this.cfgConn.channel.grpc.gzip, 
                    lngTimeout, tuTimeout);
        
        // If we are default configured for default TLS security
        //  Call default TLS connect method - security ON/OFF is determined by configuration parameters
        if (this.cfgConn.channel.tls.defaultTls)
            return this.connect(strHost, intPort, 
                    this.cfgConn.channel.tls.active,    // TLS security ON/OFF
                    bolPlainText, 
                    this.cfgConn.channel.grpc.messageSizeMax, 
                    this.cfgConn.channel.grpc.keepAliveWithoutCalls, 
                    this.cfgConn.channel.grpc.gzip, 
                    lngTimeout, tuTimeout);

        // Explicit TLS security
        //  Extract and create the File objects for the TLS resources
        File    fileTrustedCerts = new File(this.cfgConn.channel.tls.filepaths.trustedCerts);
        File    fileClientCerts = new File(this.cfgConn.channel.tls.filepaths.clientCerts);
        File    fileClientKey = new File(this.cfgConn.channel.tls.filepaths.clientKey);
        
        //  Call the explicit TLS connect method with file objects
        return this.connect(strHost, intPort, 
                fileTrustedCerts, 
                fileClientCerts,
                fileClientKey,
                bolPlainText,
                this.cfgConn.channel.grpc.messageSizeMax,
                this.cfgConn.channel.grpc.keepAliveWithoutCalls,
                this.cfgConn.channel.grpc.gzip,
                lngTimeout, tuTimeout);        
    }

    
    //
    // Factory Operations - Default TLS Security
    //
    
    /**
     * <p>
     * Creates and returns a new <code>Connection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>ServiceGrpc</code>.
     * </p>
     * <p>
     * Transport security can be disabled with the <code>bolPlainText</code> argument set to 
     * <code>false</code>.  Otherwise TLS security is enforced using the default system configuration.
     * </p>
     * No default parameters are used.
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
     */
    public Connection connect(
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
        
        // Configure the gRPC channel 
//        ManagedChannelBuilder<?> bldrChan = ManagedChannelBuilder.forAddress(strHost, intPort);
        ManagedChannelBuilder<?> bldrChan; // = ManagedChannelBuilder.forAddress(strHost, intPort);

        // Configure - TLS security (default operation)
        if (bolTlsActive) {
            bldrChan = ManagedChannelBuilder.forAddress(strHost, intPort);
            bldrChan.useTransportSecurity();
            LOGGER.info("Enforcing default TLS transport security.");
            
        } else {
            ChannelCredentials  credsInsecure = InsecureChannelCredentials.create();
            
            bldrChan = Grpc.newChannelBuilderForAddress(strHost, intPort, credsInsecure);
            LOGGER.info("Not enforcing security - insecure channel credentials.");
        }
        
        // NOTE: Plain text negates TLS security
        if (bolPlainText) {
            bldrChan.usePlaintext();
            LOGGER.warn("Transmitting plain text - any default TLS security is disabled.");
            
//        } else {
//            bldrChan.useTransportSecurity();
//            LOGGER.info("Enforcing default TLS transport security.");
        }
        
        // Configure - Maximum message size 
        if (INT_MSG_SIZE_MAX_DEFAULT != intMsgSizeMax) {
            bldrChan.maxInboundMessageSize(intMsgSizeMax);
            LOGGER.info("Maximum gRPC message set to nondefault value {}", intMsgSizeMax);
        }
        
        // Configure - Idle timeouts
        if (bolKeepAlive) {
            bldrChan.keepAliveWithoutCalls(true);
            LOGGER.warn("Keeping connection alive indefinitely - excessive gRPC resources");
            
        } else {
            bldrChan.keepAliveTimeout(lngTimeout, tuTimeout);
            LOGGER.info("Timeout after Keepalive ping set to {} {}", lngTimeout, tuTimeout);
        }
        
        // Configure - Set the data compression flag enable
        if (bolGzipCompr) {
            bldrChan.enableFullStreamDecompression();
            LOGGER.warn("Enabling GZIP compression - known performance issues with gRPC compression.");
        }

        
        // Build a gRPC channel 
        ManagedChannel grpcChan = bldrChan.build();
        LOGGER.info("Created gRPC channel for host connection {}:{}", strHost, intPort);
        
        // Create gRPC service connection
        DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub>   connRaw = 
                new DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub>(this.clsService, grpcChan);
        
        Connection  connBound = this.createFrom(connRaw);
        
        LOGGER.info("Service {} established for channel {}", this.clsService.getSimpleName(), grpcChan);

        // Return the connection
        return connBound;
    }

    
    //
    // Factory Operations - Explicit TLS Security Options
    //
    
    /**
     * <p>
     * Creates and returns a new <code>Connection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>ServiceGrpc</code>.
     * </p>
     * <p>
     * TLS security is explicitly specified with full authorization given by TLS certificates and
     * key files.
     * The URL and port address of the <em>Data Platform</em> service are taken from the default parameters.
     * All other configuration parameters are taken from the default connection properties.
     * Defers to method <code>{@link #connect(String, int, File, File, File)}</code>
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
     * @return new <code>Connection</code> instance connected to the default address
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see DpGrpcConnectionConfig
     * @see #connect(String, int, File, File, File)
     */
    public Connection connect(File fileTrustedCerts, File fileClientCerts, File fileClientKey) throws DpGrpcException {
    
        return this.connect(this.cfgConn.channel.host.url, 
                            this.cfgConn.channel.host.port,
                            fileTrustedCerts,
                            fileClientCerts,
                            fileClientKey
                            );
    }

    /**
     * <p>
     * Creates and returns a new <code>Connection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>ServiceGrpc</code>.
     * </p>
     * <p>
     * TLS security is explicitly specified with full authorization given by TLS certificates and
     * key files.
     * The URL and port address of the <em>Data Platform</em> service are explicitly specified.
     * All other configuration parameters are taken from the default connection
     * properties.
     * Defers to method <code>{@link #connect(String, int, File, File, File, boolean, int, boolean, boolean, long, TimeUnit)}</code>.
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
     * @return new <code>Connection</code> instance connected to the given address
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see DpGrpcConnectionConfig
     * @see #connect(String, int, File, File, File, boolean, int, boolean, boolean, long, TimeUnit)
     */
    public Connection connect(
            String strHost, 
            int intPort, 
            File fileTrustedCerts, 
            File fileClientCerts, 
            File fileClientKey
            ) throws DpGrpcException {
    
        return this.connect(strHost, intPort,
                fileTrustedCerts,
                fileClientCerts,
                fileClientKey,
                this.cfgConn.channel.grpc.usePlainText,
                this.cfgConn.channel.grpc.messageSizeMax,
                this.cfgConn.channel.grpc.keepAliveWithoutCalls,
                this.cfgConn.channel.grpc.gzip,
                this.cfgConn.channel.grpc.timeoutLimit, 
                this.cfgConn.channel.grpc.timeoutUnit
                );        
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
    public Connection connect(
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
        
        // Build the TLS credentials object 
        TlsChannelCredentials.Builder bldrCreds = TlsChannelCredentials.newBuilder();
        try {
            bldrCreds.keyManager(fileClientCertsChain, fileClientKey);
            bldrCreds.trustManager(fileTrustedCerts);
            
        } catch (IOException e) {
            throw new DpGrpcException(e.getMessage(), e);
            
        }
        
        ChannelCredentials  grpcCreds = bldrCreds.build();
        LOGGER.info("Using TLS security from files {}, {}, {}.", fileTrustedCerts, fileClientCertsChain, fileClientKey);
        
        // Configure the gRPC channel 
        ManagedChannelBuilder<?> bldrChan = Grpc.newChannelBuilderForAddress(strHost, intPort, grpcCreds);
        LOGGER.info("Building gRPC channel for host {} and port {}", strHost, intPort);

        // NOTE: this negates TLS security
        if (bolPlainText) {
            bldrChan.usePlaintext();
            LOGGER.warn("Transmitting plain text - security disabled.");
        }

        // Configure - Maximum message size 
        if (INT_MSG_SIZE_MAX_DEFAULT != intMsgSizeMax) {
            bldrChan.maxInboundMessageSize(intMsgSizeMax);
            LOGGER.info("Maximum gRPC message set to nondefault value {}", intMsgSizeMax);
        }
        
        // Configure - Idle timeouts
        if (bolKeepAlive) {
            bldrChan.keepAliveWithoutCalls(true);
            LOGGER.warn("Keeping connection alive indefinitely - excessive gRPC resources");
            
        } else {
            bldrChan.keepAliveTimeout(lngTimeout, tuTimeout);
            LOGGER.info("Timeout after Keepalive ping set to {} {}", lngTimeout, tuTimeout);
        }
        
        // Configure - Set the data compression flag enable
        if (bolGzipCompr) {
            bldrChan.enableFullStreamDecompression();
            LOGGER.warn("Enabling GZIP compression - known performance issues with gRPC compression.");
        }

        // Build a gRPC channel 
        ManagedChannel grpcChan = bldrChan.build();
        LOGGER.info("gRPC channel created for host connection {}:{}", strHost, intPort);
        
        // Create gRPC service connection
        DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub>   connRaw = new DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub>(this.clsService, grpcChan);
        Connection  connBound = this.createFrom(connRaw);
        
        LOGGER.info("gRPC connection established for service {}", this.clsService);

        // Return the connection
        return connBound;
    }

    
    
}
