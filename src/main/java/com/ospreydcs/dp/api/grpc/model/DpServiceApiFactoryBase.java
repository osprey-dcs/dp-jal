/*
 * Project: dp-api-common
 * File:	DpServiceApiFactoryBase.java
 * Package: com.ospreydcs.dp.api.grpc.model
 * Type: 	DpServiceApiFactoryBase
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
package com.ospreydcs.dp.api.grpc.model;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;

/**
 * <p>
 * Abstract base class for creating <code>DpServiceApiBase</code>-based Data Platform service API interfaces.
 * </p>
 * <p>
 * Although this class is prolific with generic types it is easy to use if explicit service API factories are 
 * desirable.  It also centralizes factory operations and reduces a lot of repeated code.  
 * This class defers gRPC connection creation to the an internal <code>{@link DpGrpcConnectionFactory}</code> 
 * instance.  The creation of the corresponding service API <code>{@link ServiceApi}</code> must be 
 * implemented in the subclass with abstract method <code>{@link #apiFrom(DpGrpcConnection)}</code>.  
 * Implemented this method is the major effort of the derived class, along with getting all the generic
 * type parameters correct.
 * </p>
 * <p>
 * Service APIs should implement a <code>from(Connection)</code> static creator that creates instances
 * from a <code>DpGrpcConnection</code> derived class.  Such connection typically have their own factory
 * to create configured connections to Data Platform services.  Thus, the implementation of connection 
 * factories for the service APIs themselves is not really necessary.  But sometimes it is helpful to 
 * users.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * All generic type parameter must be consistent.  Specifically, 
 * <ul>
 * <li>The <code>ServiceApi</code> must support the <code>ServiceGrpc</code> interface.</li>
 * <li>All stub types must belong to the <code>ServiceGrpc</code> type (i.e., they are enclosed classes).</li>
 * <li>The <code>Connection</code> must support to the <code>ServiceApi</code> type.</li>
 * </ul>
 * </p>
 * <p>
 * <h2>WARNING:</h2>
 * All derived Data Platform service API interfaces should be shut down when no longer needed using
 * <code>{@link #shutdownSoft()} or <code>{@link #shutdownNow()}</code>.  Otherwise gRPC resources are not
 * released and performance degrades.
 * </p> 
 *
 * @param <ServiceApi>  DP service API being created (sub-type of <code>DpServiceApiBase</code>)
 * @param <Connection>  gRPC Connection type to Data Platform service (sub-type of <code>DpGrpcConnection</code>)
 * @param <ServiceGrpc> the specific Data Platform service, that is, the Protobuf-generated gRPC service class  
 * @param <BlockStub>   synchronous (blocking) stub supporting unary gPRC communications 
 * @param <FutureStub>  non-blocking stub supporting unary gPRC communications 
 * @param <AsyncStub>   asynchronous (non-blocking) stub for both unary and streaming gRPC communications 
 *
 * @author Christopher K. Allen
 * @since Jan 7, 2024
 *
 * @see DpGrpcConnection
 * @see DpGrpcConnectionFactory
 * @see DpServiceApiBase
 */
public abstract class DpServiceApiFactoryBase<
    ServiceApi extends DpServiceApiBase<ServiceApi, Connection, ServiceGrpc, BlockStub, FutureStub, AsyncStub>,
    Connection extends DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub>,
    ServiceGrpc,
    BlockStub extends io.grpc.stub.AbstractBlockingStub<BlockStub>, 
    FutureStub extends io.grpc.stub.AbstractFutureStub<FutureStub>,
    AsyncStub extends io.grpc.stub.AbstractAsyncStub<AsyncStub> 
    > 
{

    
    //
    // Class Types
    //
    
    /**
     * </p>
     * Shorthand for class factory type of the internal class factory instance.
     * </p> 
     */
//    private class ConnectionFactory extends DpGrpcConnectionFactory<ServiceGrpc, BlockStub, FutureStub, AsyncStub> {
//
//        protected ConnectionFactory(Class<ServiceGrpc> clsService, DpGrpcConnectionConfig cfgConn) {
//            super(clsService, cfgConn);
//        }
//        
//    }
    private class ConnectionFactory extends DpGrpcConnectionFactoryBase<Connection, ServiceGrpc, BlockStub, FutureStub, AsyncStub> {

        protected ConnectionFactory(Class<ServiceGrpc> clsService, DpGrpcConnectionConfig cfgConn) {
            super(clsService, cfgConn);
        }

        @Override
        protected Connection createFrom(DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> conn)
                throws DpGrpcException {
            Connection connBound = DpServiceApiFactoryBase.this.connectionFrom(conn);
            
            return connBound;
        }
        
    }

    //
    // Attributes
    //
    
    /** The <code>DpGrpcConnectionFactory</code> type bound for the <code>API</code> type */
    private final ConnectionFactory     fac;
    
    
    // 
    // Abstract Methods
    //
    
    protected abstract Connection connectionFrom(DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> conn) throws DpGrpcException;
    
    /**
     * <p>
     * Create and return a <code>{@link ServiceApi}</code> instance from the given <code>{@link DpGrpcConnection}</code> object.
     * </p>
     * <p>
     * Derived classes must implement this method to create their factory instances from the given connection
     * type.  It is required that the <code>Connection</code> type is derived from the given argument type and
     * that the <code>ServiceApi</code> is supported by <code>Connection</code>.  Thus, the typing is consistent
     * and a process for creating <code>Connection</code> instances from their base class should be available.
     * </p>
     *  
     * @param conn  gRPC connection support the returned service API
     * @return      new service api instance supported by the argument
     * 
     * @throws DpGrpcException  general gRPC resource creation error (see message and cause) 
     */
//    protected abstract ServiceApi apiFrom(DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> conn) throws DpGrpcException;
    protected abstract ServiceApi apiFrom(Connection conn) throws DpGrpcException;

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpServiceApiFactoryBase</code>.
     * </p>
     * <p>
     * Derived classes must implement a (default) constructor supply the correct arguments for this constructor.
     * The arguments are required to instantiate a gRPC connection factory instance used by this class.
     * </p>
     *   
     * @param clsService    class type of the Protobuf-generated gRPC interface supporting the service APIs
     * @param cfg           default configuration parameters for the gRPC connections required by service APIs
     */
    protected DpServiceApiFactoryBase(Class<ServiceGrpc> clsService, DpGrpcConnectionConfig cfg) {
        this.fac = new ConnectionFactory(clsService, cfg);
    }
    

    //
    // Factory Methods - Default TLS Security Parameters
    //
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect()} for details.
     * </p>
     * 
     * @return new <code>{@link ServiceApi}</code> instance with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect()
     */
    public ServiceApi connect() throws DpGrpcException {
//        DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> conn = this.fac.connect();
        Connection conn = this.fac.connect();
        ServiceApi api = this.apiFrom(conn);
        
        return api;
    }
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int)} for details.
     * </p>
     * 
     * @param strUrl    server URL for desired service 
     * @param intPort   service port hosting desired service
     *  
     * @return new <code>{@link ServiceApi}</code> instance with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(String, int)
     */
    public ServiceApi connect(String strUrl, int intPort) throws DpGrpcException {
//        DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> conn = this.fac.connect(strUrl, intPort);
        Connection conn = this.fac.connect(strUrl, intPort);
        ServiceApi api = this.apiFrom(conn);
        
        return api;
    }
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, boolean)} for details.
     * </p>
     * 
     * @return new <code>{@link ServiceApi}</code> instance with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(String, int, boolean)
     */
    public ServiceApi connect(String strUrl, int intPort, boolean bolPlainText) throws DpGrpcException {
//        DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> conn = this.fac.connect(
//                strUrl, intPort,
//                bolPlainText);
        Connection conn = this.fac.connect(strUrl, intPort, bolPlainText);
        ServiceApi api = this.apiFrom(conn);
        
        return api;
    }
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, boolean, long, TimeUnit)} for details.
     * </p>
     * 
     * @return new <code>{@link ServiceApi}</code> instance with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(String, int, boolean, long, TimeUnit)
     */
    public ServiceApi connect(String strHost, int intPort, boolean bolPlainText, long lngTimeout, TimeUnit tuTimeout) throws DpGrpcException {
//        DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> conn = this.fac.connect(
//                strHost, intPort,
//                bolPlainText,
//                lngTimeout, 
//                tuTimeout);
        Connection conn = this.fac.connect(
                strHost, intPort,
                bolPlainText,
                lngTimeout, 
                tuTimeout);
        ServiceApi api = this.apiFrom(conn);
        
        return api;
    }
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, boolean, boolean, int, boolean, boolean, long, TimeUnit)} for details.
     * </p>
     * 
     * @return new <code>{@link ServiceApi}</code> instance with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(String, int, boolean, int, boolean, boolean, boolean, long, TimeUnit)
     */
    public ServiceApi connect(
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
//        DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> conn = this.fac.connect(
//                strHost, intPort,
//                bolTlsActive,
//                bolPlainText, 
//                intMsgSizeMax, 
//                bolKeepAlive, 
//                bolGzipCompr, 
//                lngTimeout, 
//                tuTimeout);
        Connection conn = this.fac.connect(
                strHost, intPort,
                bolTlsActive,
                bolPlainText, 
                intMsgSizeMax, 
                bolKeepAlive, 
                bolGzipCompr, 
                lngTimeout, 
                tuTimeout);
        
        ServiceApi api = this.apiFrom(conn);
        
        return api;
    }


    //
    // Factory Methods - Explicit TLS Security Parameters
    //
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(File, File, File)} for details.
     * </p>
     * 
     * @return new <code>{@link ServiceApi}</code> instance with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(File, File, File)
     */
    public ServiceApi connect(File fileTrustedCerts, File fileClientCerts, File fileClientKey) throws DpGrpcException {
//        DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> conn = this.fac.connect(fileTrustedCerts, fileClientCerts, fileClientKey);
        Connection conn = this.fac.connect(fileTrustedCerts, fileClientCerts, fileClientKey);
        ServiceApi api = this.apiFrom(conn);
        
        return api;
    }
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, File, File, File)} for details.
     * </p>
     * 
     * @return new <code>{@link ServiceApi}</code> instance with given parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(String, int, File, File, File)
     */
    public ServiceApi connect(String strHost, int intPort, File fileTrustedCerts, File fileClientCerts, File fileClientKey) throws DpGrpcException {
//        DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> conn = fac.connect(
//                strHost, intPort, 
//                fileTrustedCerts, 
//                fileClientCerts, 
//                fileClientKey);
        Connection conn = fac.connect(
                strHost, intPort, 
                fileTrustedCerts, 
                fileClientCerts, 
                fileClientKey);
        
        ServiceApi api = this.apiFrom(conn);
        
        return api;
    }
    
    /**
     * <p>
     * See {@link DpGrpcConnectionFactory#connect(String, int, File, File, File, boolean, int, boolean, boolean, long, TimeUnit)} for details.
     * </p>
     * 
     * @return new <code>{@link ServiceApi}</code> instance with given connection parameters
     * 
     * @throws DpGrpcException general gRPC resource creation error (see message and cause)
     * 
     * @see DpGrpcConnectionFactory#connect(String, int, File, File, File, boolean, int, boolean, boolean, long, TimeUnit)
     */
    public ServiceApi connect(
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
//        DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub> conn = this.fac.connect(
//                strHost, intPort, 
//                fileTrustedCerts, 
//                fileClientCertsChain, 
//                fileClientKey,
//                bolPlainText,
//                intMsgSizeMax, 
//                bolKeepAlive, 
//                bolGzipCompr, 
//                lngTimeout, 
//                tuTimeout);
        Connection conn = this.fac.connect(
                strHost, intPort, 
                fileTrustedCerts, 
                fileClientCertsChain, 
                fileClientKey,
                bolPlainText,
                intMsgSizeMax, 
                bolKeepAlive, 
                bolGzipCompr, 
                lngTimeout, 
                tuTimeout);
        
        ServiceApi api = this.apiFrom(conn);
        
        return api;
    }

}
