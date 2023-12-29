/*
 * Project: dp-api-common
 * File:	DpGrpcConnectionFactory.java
 * Package: com.ospreydcs.dp.api.grpc
 * Type: 	DpGrpcConnectionFactory
 *
 * Copyright 2010-2022 the original author or authors.
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
 * @since Nov 16, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.model;

import com.ospreydcs.dp.api.config.grpc.GrpcConnectionConfig;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.TlsChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * Factory class for creating <code>DsGrpcConnection</code> instances.
 * </p>
 * <p>
 * Creates <code>DsGrpcConnection</code> instances supporting the Protocol Buffers connection
 * stubs specified by the generic parameter types.  Various configuration 
 * combinations are available which mix user-supplied gRPC connection parameters
 * and default connection parameters supplied by the <code>GrpcConnectionConfig<code> instance
 * at creation.
 * </p>
 * <p>
 * The types of the Java generics parameters determine the Data Platform service for connection.
 * For example the Ingestion Service or the Query Service may be supported with the following
 * bindings:
 * <ul>
 * Ingestion Service Connections
 * <li><code>Service</code> = <code>IngestionServiceGrpc</code>
 *     </li>
 * <li><code>SyncStub</code> = <code>IngestionServiceGrpc.IngestionServiceBlockingStub</code>
 *     </li>
 * <li><code>FutureStub</code> = <code>IngestionServiceGrpc.IngestionServiceFutureStub</code>
 *     </li>
 * <li><code>AsyncStub</code> = <code>IngestionServiceGrpc.IngestionServiceStub</code>
 *     </li> 
 *     <br/>
 * Query Service Connections
 * <li><code>Service</code> = <code>QueryServiceGrpc</code>
 *     </li>
 * <li><code>SyncStub</code> = <code>QueryServiceGrpc.QueryServiceBlockingStub</code>
 *     </li>
 * <li><code>FutureStub</code> = <code>QueryServiceGrpc.QueryServiceFutureStub</code>
 *     </li>
 * <li><code>AsyncStub</code> = <code>QueryServiceGrpc.QueryServiceStub</code>
 *     </li> 
 * </ul>
 * Thus, for example, the following statement creates a connection factory <code>facQueryConn</code> 
 * instance for the Data Platform Query Service:
 * <pre>
 * private static final DpGrpcConnectionFactory&lt;
 *                       DpQueryServiceGrpc, 
 *                       DpQueryServiceBlockingStub,
 *                       DpQueryServiceFutureStub, 
 *                       DpQueryServiceStub&gt; facQueryConnec = 
 *                           DpGrpcConnectionFactory.newFactory(
 *                                DpQueryServiceGrpc.class, 
 *                                DpApiConfig.getInstance()
 *                                             .services
 *                                             .query
 *                                );
 * </pre>
 * Since only one factory class instance is required to create all
 * Data Platform query service connections, the instance may be 
 * declared <code>static final</code>.
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
 * <h2>Fly Weight Connection Factories</h2>
 * Connection factory instances can be used within fly weight connection factory classes
 * that bind the generic parameters for a specific Data Platform service.
 * An instance is maintained within the fly weight for creating the connection objects required by the 
 * targeted services.
 * </p>
 * 
 * @param <Service>     the specific Data Platform service, that is, the Protobuf-generated gRPC service  
 * @param <SyncStub>    synchronous (blocking) stub supporting unary gPRC communications 
 * @param <FutureStub>  non-blocking stub supporting unary gPRC communications 
 * @param <AsyncStub>   asynchronous (non-blocking) stub for both unary and streaming gRPC communications 
 *
 * @author Christopher K. Allen
 * @since Nov 16, 2022
 *
 * @see DpGrpcConnection
 */
public class DpGrpcConnectionFactory<
    Service,
    SyncStub extends io.grpc.stub.AbstractBlockingStub<SyncStub>, 
    FutureStub extends io.grpc.stub.AbstractFutureStub<FutureStub>,
    AsyncStub extends io.grpc.stub.AbstractAsyncStub<AsyncStub> 
    > 
{
    
    //
    // Application Resources
    //
    
//    /**
//     * <p>
//     * Public singleton factory for <em>Datastore</em> Ingestion Service connections
//     * </p>
//     * <p>
//     * Produces <em>Datastore</em> gRPC connections of type
//     * <code>DsGrpcConnection&lt;IngestionServiceBlockingStub, IngestionServiceStub&gt;</code>
//     * </p>
//     * 
//     * @see DsGrpcConnection
//     */
//    public static final DpGrpcConnectionFactory<IngestionServiceGrpc, IngestionServiceBlockingStub, IngestionServiceStub> FAC_INGEST;
//    
//    /**
//     * <p>
//     * Public singleton factory for <em>Datstore</em> Query Service connections
//     * </p> 
//     * <p>
//     * Produces <em>Datastore</em> gRPC connections of type
//     * <code>DsGrpcConnection&lt;QueryServiceBlockingStub, QueryServiceStub&gt;</code>
//     * </p>
//     * 
//     * @see DsGrpcConnection
//     */
//    public static final DpGrpcConnectionFactory<QueryServiceGrpc, QueryServiceBlockingStub, QueryServiceStub> FAC_QUERY;
//    
//    /**
//     * Create the factory singletons
//     */
//    static {
//        FAC_INGEST =  DpGrpcConnectionFactory.newFactory(IngestionServiceGrpc.class, AppProperties.getInstance().getDatastore().getProvider().getConnection());
//        FAC_QUERY  = DpGrpcConnectionFactory.newFactory(QueryServiceGrpc.class, AppProperties.getInstance().getDatastore().getClient().getConnection());
//    }
    
    
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
    private final Class<Service>    clsService;
    
    /** Record containing default connection parameters */
    private final GrpcConnectionConfig    cfgConn;
    
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new <code>DpGrpcConnectionFactory</code> instance.
     * </p>
     * <p>
     * The returned factory is capable of creating <code>DsGrpcConnection</code>
     * instances for connection with the Data Platform service targeted by the generic parameters.  
     * The type of communications object returned is also dictated by the Java template parameters.  
     * </p>
     * <p>
     * The returned connection factory is fully configured for Data Platform service connection.
     * The default gRPC connection parameters are provided in the argument.
     * </p>
     * 
     * @param <Service>     type of the Protobuf-generated service class
     * @param <SyncStub>    type for synchronous blocking communications stub 
     * @param <FutureStub>  type for non-blocking unary communications stub
     * @param <AsyncStub>   type for asynchronous communications stub (contains streaming RPC operations)
     *
     * @param clsService    the class type of the supported Data Platform service
     * @param cfgConn       connections properties record containing the default parameters
     * 
     * @return a new <code>DpGrpcConnectionFactory</code> instance
     */
    public static <Service, SyncStub extends io.grpc.stub.AbstractBlockingStub<SyncStub>, FutureStub extends io.grpc.stub.AbstractFutureStub<FutureStub>, AsyncStub extends io.grpc.stub.AbstractAsyncStub<AsyncStub>> 
        DpGrpcConnectionFactory<Service, SyncStub, FutureStub, AsyncStub> 
        newFactory(Class<Service> clsService, GrpcConnectionConfig cfgConn) 
    {
        return new DpGrpcConnectionFactory<Service, SyncStub, FutureStub, AsyncStub>(clsService, cfgConn);        
    }

    
    //
    // Factory Operations - Default TLS Security
    //
    
    /**
     * <p>
     * Creates and returns a new <code>DsGrpcConnection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>Service</code>.
     * </p>
     * <p>
     * All other configuration parameters are taken from the default connection
     * properties, including the service host location.
     * </p>
     * 
     * @return new <code>DpGrpcConnection</code> instance with all default configuration
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see DpGrpcConnectionConfig
     */
    public DpGrpcConnection<Service, SyncStub, FutureStub, AsyncStub> connect() throws DpGrpcException {
        
        return this.connect(this.cfgConn.channel.host.url, this.cfgConn.channel.host.port);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DsGrpcConnection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>Service</code>.
     * </p>
     * <p>
     * The URL and port address of the <em>Data Platform</em> service are explicitly specified.
     * All other configuration parameters are taken from the default connection
     * properties.
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       server port used by the at the above service
     * 
     * @return new <code>DpGrpcConnection</code> instance connected to the given address
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see DpGrpcConnectionConfig
     */
    public DpGrpcConnection<Service, SyncStub, FutureStub, AsyncStub> connect(String strHost, int intPort) throws DpGrpcException {

        return this.connect(strHost, intPort, this.cfgConn.channel.grpc.timeoutLimit, this.cfgConn.channel.grpc.timeoutUnit);        
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DsGrpcConnection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>Service</code>.
     * </p>
     * <p>
     * The URL and port address of the <em>Data Platform</em> service are explicitly specified,
     * along with the timeout parameters.
     * All other configuration parameters are taken from the default connection
     * properties.
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       server port used by the at the above service
     * @param lngTimeout    timeout limit used for connection operations (keepalive ping timeout) 
     * @param tuTimeout     timeout units used for connection operations (keepalive ping timeout)
     * 
     * @return new <code>DpGrpcConnection</code> instance connected to the given address
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see DpGrpcConnectionConfig
     */
    public DpGrpcConnection<Service, SyncStub, FutureStub, AsyncStub> connect(String strHost, int intPort, long lngTimeout, TimeUnit tuTimeout) throws DpGrpcException {

        if (!this.cfgConn.channel.tls.active)
            return this.connect(strHost, intPort, true, this.cfgConn.channel.grpc.messageSizeMax, this.cfgConn.channel.grpc.keepAliveWithoutCalls, this.cfgConn.channel.grpc.gzip, lngTimeout, tuTimeout);
        
        if (this.cfgConn.channel.tls.defaultTls)
            return this.connect(strHost, intPort, false, this.cfgConn.channel.grpc.messageSizeMax, this.cfgConn.channel.grpc.keepAliveWithoutCalls, this.cfgConn.channel.grpc.gzip, lngTimeout, tuTimeout);

        // Create the File objects for the TLS resources
        File    fileTrustedCerts = new File(this.cfgConn.channel.tls.filepaths.trustedCerts);
        File    fileClientCerts = new File(this.cfgConn.channel.tls.filepaths.clientCerts);
        File    fileClientKey = new File(this.cfgConn.channel.tls.filepaths.clientKey);
        
        return this.connect(strHost, intPort, 
                fileTrustedCerts, 
                fileClientCerts,
                fileClientKey,
                this.cfgConn.channel.grpc.messageSizeMax,
                this.cfgConn.channel.grpc.keepAliveWithoutCalls,
                this.cfgConn.channel.grpc.gzip,
                lngTimeout, tuTimeout);        
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DsGrpcConnection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>Service</code>.
     * </p>
     * <p>
     * Transport security can be disabled with the <code>bolPlainText</code> argument set to 
     * <code>false</code>.  Otherwise TLS security is enforced using the default system configuration.
     * </p>
     * No default parameters are used.
     * </p>
     * <p>
     * Creates and returns a new <code>DsQueryConnection</code> instance connected
     * to the <em>Datastore</em> with the given host URL and using the given
     * port.  The query service is configured with the given <code>boolean</code>
     * argument parameters.
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       server port used by the at the above service
     * @param bolPlainText  transmit data using plain ASCII (w/out TLS security)
     * @param intMsgSizeMax maximum message size for gRPC transmission (bytes)
     * @param bolKeepAlive  keep connection without call activity (no idle mode)
     * @param bolGzipCompr  enable GZIP compress for data transmission
     * @param lngTimeout    timeout limit used for connection operations (keepalive ping timeout) 
     * @param tuTimeout     timeout units used for connection operations (keepalive ping timeout)
     * 
     * @return new <code>DsGrpcConnection</code> instance connected to the given address
     *         and using the given configuration parameters
     *         
     * @throws DpGrpcException error while creating connection object (see message and cause) 
     */
    public DpGrpcConnection<Service, SyncStub, FutureStub, AsyncStub> connect(
            String strHost, 
            int intPort, 
            boolean bolPlainText,
            int     intMsgSizeMax,
            boolean bolKeepAlive,
            boolean bolGzipCompr,
            long    lngTimeout,
            TimeUnit tuTimeout
            ) throws DpGrpcException 
    {
        
        // Configure the gRPC channel 
        ManagedChannelBuilder<?> bldrChan = ManagedChannelBuilder.forAddress(strHost, intPort);

        // Configure - TLS security (default operation)
        // NOTE: Plain text negates TLS security
        if (bolPlainText) {
            bldrChan.usePlaintext();
            LOGGER.warn("Transmitting plain text - security disabled.");
            
        } else {
            bldrChan.useTransportSecurity();
            LOGGER.info("Enforcing transport security.");
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
        DpGrpcConnection<Service, SyncStub, FutureStub, AsyncStub>   connService = new DpGrpcConnection<Service, SyncStub, FutureStub, AsyncStub>(this.clsService, grpcChan);
        LOGGER.info("gRPC connection established for service {}", this.clsService);

        // Return the connection
        return connService;
    }

    
    //
    // Factory Operations - Explicit TLS Security Options
    //
    
    /**
     * <p>
     * Creates and returns a new <code>DsGrpcConnection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>Service</code>.
     * </p>
     * <p>
     * TLS security is explicitly specified with full authorization given by TLS certificates and
     * key files.
     * The URL and port address of the <em>Data Platform</em> service are taken from the default parameters.
     * All other configuration parameters are taken from the default connection properties.
     * </p>
     * 
     * @param fileTrustedCerts root file of all trusted server certificates
     * @param fileClientCerts  file of client certificates
     * @param fileClientKey file containing client private key
     * 
     * @return new <code>DpGrpcConnection</code> instance connected to the default address
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see DpGrpcConnectionConfig
     */
    public DpGrpcConnection<Service, SyncStub, FutureStub, AsyncStub> connect(File fileTrustedCerts, File fileClientCerts, File fileClientKey) throws DpGrpcException {
    
        return this.connect(this.cfgConn.channel.host.url, 
                            this.cfgConn.channel.host.port,
                            fileTrustedCerts,
                            fileClientCerts,
                            fileClientKey
                            );
    }

    /**
     * <p>
     * Creates and returns a new <code>DsGrpcConnection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>Service</code>.
     * </p>
     * <p>
     * TLS security is explicitly specified with full authorization given by TLS certificates and
     * key files.
     * The URL and port address of the <em>Data Platform</em> service are explicitly specified.
     * All other configuration parameters are taken from the default connection
     * properties.
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       server port used by the at the above service
     * @param fileTrustedCerts root file of all trusted server certificates
     * @param fileClientCerts  file of client certificates
     * @param fileClientKey file containing client private key
     * 
     * @return new <code>DpGrpcConnection</code> instance connected to the given address
     * 
     * @throws DpGrpcException general gRPC resource creation exception (see message and cause)  
     * 
     * @see DpGrpcConnectionConfig
     */
    public DpGrpcConnection<Service, SyncStub, FutureStub, AsyncStub> connect(String strHost, int intPort, File fileTrustedCerts, File fileClientCerts, File fileClientKey) throws DpGrpcException {
    
        return this.connect(strHost, intPort,
                fileTrustedCerts,
                fileClientCerts,
                fileClientKey,
                this.cfgConn.channel.grpc.messageSizeMax,
                this.cfgConn.channel.grpc.keepAliveWithoutCalls,
                this.cfgConn.channel.grpc.gzip,
                this.cfgConn.channel.grpc.timeoutLimit, 
                this.cfgConn.channel.grpc.timeoutUnit
                );        
    }


    /**
     * <p>
     * Creates and returns a new <code>DsGrpcConnection</code> instance connected
     * to the supported <em>Data Platform</em> service identified by <code>Service</code>.
     * </p>
     * <p>
     * Transport Layer Security (TLS) is assumed here and parameters required for full authorization
     * are required.
     * </p>
     * No default parameters are used.
     * </p>
     * 
     * @param strHost       network URL of the desired service
     * @param intPort       network port used by the at the above service
     * @param fileTrustedCerts root file of all trusted server certificates
     * @param fileClientCerts  file of client certificates
     * @param fileClientKey file containing client private key
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
     */
    public DpGrpcConnection<Service, SyncStub, FutureStub, AsyncStub> connect(
            String  strHost, 
            int     intPort, 
            File    fileTrustedCerts,
            File    fileClientCertsChain,
            File    fileClientKey,
            int     intMsgSizeMax,
//            boolean bolPlainText,
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
        
//        // NOTE: Remove since this negates TLS security
//        if (bolPlainText) {
//            bldrChan.usePlaintext();
//            LOGGER.warn("Transmitting plain text - security disabled.");
//        }
        
        // Configure - Set the data compression flag enable
        if (bolGzipCompr) {
            bldrChan.enableFullStreamDecompression();
            LOGGER.warn("Enabling GZIP compression - known performance issues with gRPC compression.");
        }

        // Build a gRPC channel 
        ManagedChannel grpcChan = bldrChan.build();
        LOGGER.info("gRPC channel created for host connection {}:{}", strHost, intPort);
        
        // Create gRPC service connection
        DpGrpcConnection<Service, SyncStub, FutureStub, AsyncStub>   connService = new DpGrpcConnection<Service, SyncStub, FutureStub, AsyncStub>(this.clsService, grpcChan);
        LOGGER.info("gRPC connection established for service {}", this.clsService);

        // Return the connection
        return connService;
    }

    
    //
    // Protected Methods
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
    protected DpGrpcConnectionFactory(Class<Service> clsService, GrpcConnectionConfig cfgConn) {
        this.clsService = clsService;
        this.cfgConn    = cfgConn;
        
//        this.strHostUrl =  cfgConn.getHost().getName();
//        this.intHostPort = cfgConn.getHost().getPort();
//        this.bolSendPlainText = cfgConn.getUsePlainText();
//        this.bolKeepConnAlive = cfgConn.getKeepAliveWithoutCalls();
//        this.bolGzipCompress = cfgConn.getGzip();
//        
//        this.cntTimeout = cfgConn.getTimeout();
    }

//    /**
//     * <p>
//     * Create a new synchronous (blocking) handle for the given gRPC managed
//     * channel instance.
//     * </p>
//     * <p>
//     * Uses Java reflection to locate the static method <code>newBlockingStub</code>
//     * of the <code>Service</code> class (which was passed as an argument to
//     * the constructor).  If found, the method is invoked with the given argument
//     * to create a new synchronous blocking stub for the gRPC communication
//     * service.
//     * </p>
//     * <p>
//     * If any of the Java reflection operations fail, a <code>null</code> value
//     * is returned.
//     * </p>
//     * 
//     * @param chnGrpc gRPC managed channel backing the synchronous blocking stub
//     * 
//     * @return  new synchronous blocking stub attached to the given gRPC channel,
//     *          or <code>null</code> if the operation fails
//     */
//    protected SyncStub  newSyncStub(ManagedChannel chnGrpc) {
//        try {
//            Method mthNewStub = this.clsService.getMethod("newBlockingStub", io.grpc.Channel.class);
//            
//            @SuppressWarnings("unchecked")
//            SyncStub stub = (SyncStub) mthNewStub.invoke(null, chnGrpc);
//            
//            return stub;
//            
//        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//            
//            return null;
//        }
//    };
//    
//    /**
//     * <p>
//     * Create a new asynchronous (non-blocking) handle for the given gRPC managed
//     * channel instance.
//     * </p>
//     * <p>
//     * Uses Java reflection to locate the static method <code>newStub</code>
//     * of the <code>Service</code> class (which was passed as an argument to
//     * the constructor).  If found, the method is invoked with the given argument
//     * to create a new asynchronous non-blocking stub for the gRPC communication
//     * service.
//     * </p>
//     * <p>
//     * If any of the Java reflection operations fail, a <code>null</code> value
//     * is returned.
//     * </p>
//     * 
//     * @param chnGrpc gRPC managed channel backing the asynchronous blocking stub
//     * 
//     * @return  new asynchronous stub attached to the given gRPC channel,
//     *          or <code>null</code> if the operation fails
//     */
//    protected AsyncStub newAsyncStub(ManagedChannel chnGrpc) {
//        try {
//            Method mthNewStub = this.clsService.getMethod("newStub", io.grpc.Channel.class);
//            
//            @SuppressWarnings("unchecked")
//            AsyncStub stub = (AsyncStub) mthNewStub.invoke(null, chnGrpc);
//            
//            return stub;
//            
//        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//            
//            return null;
//        }
//    };
    
}
