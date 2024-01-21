/*
 * Project: dp-api-common
 * File:	DpGrpcConnection.java
 * Package: com.ospreydcs.dp.api.grpc
 * Type: 	DpGrpcConnection
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.grpc.ManagedChannel;

/**
 * <p>
 * Convenience class for maintaining a gRPC channel connection 
 * and internally managing (and creating) all Protocol Buffers communications stubs
 * for a gRPC service interface.
 * </p>
 * <p>
 * The blocking gRPC communication stub (<code>SyncStub</code>)
 * for synchronous communication, the future stub (<code>FutureStub</code>) for
 * non-block unary operations, and the non-blocking, asynchronous stub (<code>AsyncStub</code>)
 * containing all RPC operations are created by this class during construction.
 * The gRPC managed channel then backs all RPC interface communications stubs.
 * </p>
 * <p>
 * The constructor arguments are assumed to be created in a connection factory
 * offering various connection configurations for the communications services.
 * </p>
 * 
 * @param <ServiceGrpc> Protocol Buffers generated gRPC service class
 * @param <BlockStub>   Protocol Buffer communication stub containing blocking, synchronous RPC operations
 * @param <FutureStub>  Protocol Buffer communication stub containing non-blocking (future) RPC operations
 * @param <AsyncStub>   Protocol Buffer communication stub containing asynchronous streaming RPC operations
 *
 * @author Christopher K. Allen
 * @since Nov 16, 2022
 *
 */
public class DpGrpcConnection<
    ServiceGrpc,
    BlockStub extends io.grpc.stub.AbstractBlockingStub<BlockStub>, 
    FutureStub extends io.grpc.stub.AbstractFutureStub<FutureStub>,
    AsyncStub extends io.grpc.stub.AbstractAsyncStub<AsyncStub> 
    > 
//    implements IConnection
{
    
    //
    // Class Constants
    //

    /** Method name within <code>Service</code> class for creating new synchronous, blocking communications stubs */
    public static final String STR_MTHD_NEW_BLOCKING_STUB = "newBlockingStub";

    /** Method name within <code>Service</code> class for creating new synchronous, non-blocking communications stubs */
    public static final String STR_MTHD_NEW_FUTURE_STUB = "newFutureStub";

    /** Method name within <code>Service</code> class for creating new asynchronous, non-blocking communications stubs */
    public static final String STR_MTHD_NEW_STUB = "newStub";
    
    
    //
    // Class Resources
    //
    
    /** The general event logger */
    private static final Logger LOGGER = LogManager.getLogger();

    
    //
    // Connection Resources
    //
    
    /** The class type of the gRPC service service being supported */
    private final Class<ServiceGrpc>    clsService;
    
    /** The single gRPC data channel supporting all communications stubs */
    private final ManagedChannel    grpcChan;
    
    /** Blocking, synchronous communications stub (no streaming operations)*/
    private final BlockStub          stubBlock;
    
    /** Non-blocking communications stub (no streaming operations) */
    private final FutureStub        stubFuture;

    /** Full Stub - Nonblocking, asynchronous communications stub with all streaming operations */
    private final AsyncStub         stubAsync;
    
    
//    /** The timeout limit for channel shutdown and termination operations */
//    private final Integer           cntTimeout;
//    
//    /** The timeout units for channel shutdown and termination operations */
//    private final TimeUnit          tuTimeout;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpGrpcConnection</code> containing all communication
     * stubs for the given gRPC <code>Service</code> class.
     * </p>
     * <p>
     * The <code>ManagedChannel</code> argument will be the gRPC channel
     * backing all internally created connection stubs.  
     * </p>
     *
     * @param <Service>     Protocol Buffers generated gRPC service class
     * 
     * @param grpcChan       gRPC managed channel backing the synchronous blocking stub
     * @param clsService    class object of the <code>Service</code> class
     * 
     * @throws DpGrpcException a Java reflection error occurred during communication stubs creation (see message and cause)
     */
    public DpGrpcConnection(Class<ServiceGrpc> clsService, ManagedChannel grpcChan) throws DpGrpcException {
        this.clsService = clsService;
        this.grpcChan = grpcChan;
        this.stubBlock = this.newBlockStub(grpcChan);
        this.stubFuture = this.newFutureStub(grpcChan);
        this.stubAsync = this.newAsyncStub(grpcChan);
        
        LOGGER.debug("Created new connection {} for gRPC service {}", this.getClass().getSimpleName(), clsService.getSimpleName());
    }

    /**
     * <p>
     * Clones an instance of <code>DpGrpcConnection</code>.
     * </p>
     * <p>
     * This method is provided for subclasses to create instances of themselves from
     * instances of this class (as a base class).  This is essentially an aliasing
     * operation or might be called "a move constructor" in C++.
     * </p>
     *
     * @param chnGrpc   the single gRPC communications channel used by the service stubs
     * @param stubBlock   synchronous communication stub for desired RPC interface
     * @param stubAsync  asynchronous communication stub for desired RPC interface
     */
    protected DpGrpcConnection(DpGrpcConnection<ServiceGrpc, BlockStub, FutureStub, AsyncStub>  conn) {
        this.clsService = conn.clsService;
        this.grpcChan = conn.grpcChan;
        this.stubBlock = conn.stubBlock;
        this.stubFuture = conn.stubFuture;
        this.stubAsync = conn.stubAsync;
        
        LOGGER.debug("Cloned new connection {} for gRPC service {}", this.getClass().getSimpleName(), this.clsService.getSimpleName());
    }
    
//    /**
//     * <p>
//     * Creates a new instance of <code>DpGrpcConnection</code> connected to 
//     * the given gRPC channel where all communications stubs are explicitly provided.
//     * </p>
//     * <p>
//     * This method is provided for subclasses to create instances of themselves from
//     * instances of this class (as a base class).  The would be called "a move constructor"
//     * in C++.
//     * The <code>ManagedChannel</code> argument should be the gRPC channel
//     * backing all connection stubs.  
//     * </p>
//     *
//     * @param chnGrpc   the single gRPC communications channel used by the service stubs
//     * @param stubBlock   synchronous communication stub for desired RPC interface
//     * @param stubAsync  asynchronous communication stub for desired RPC interface
//     */
//    protected DpGrpcConnection(
//            ManagedChannel chnGrpc, 
//            BlockStub stubBlock, 
//            FutureStub stubFuture,
//            AsyncStub stubAsync)
//    {
//        this.grpcChan = chnGrpc;
//        this.stubBlock = stubBlock;
//        this.stubFuture = stubFuture;
//        this.stubAsync = stubAsync;
//        
//        LOGGER.debug("Created new connection {} for gRPC unknown service", this.getClass().getName());
//    }
//    
    
    //
    // IConnection Interface
    //
    
    /**
     * <p>
     * Does a soft shutdown of the gRPC connection.
     * </p>
     * <p>
     * Shuts down the service connection such that any new requests are 
     * cancelled but any currently executing processes are allowed to finish.  
     * Once all processes are finished the connection releases 
     * all gRPC resources.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>The ultimate result of a shutdown operation is gRPC channel termination.
     * </li>
     * <li>There may be ongoing gRPC operations and the <code>{@link #isTerminated()}
     *     method will return <code>false</code> until all operations are completed and
     *     the channel is terminated.
     *     </li>
     * <li>The {@link #isShutdown()} method will return <code>true</code> after this call
     *     even though operations may be ongoing.
     *     </li>
     *  <li>The {@link #awaitTermination(long, TimeUnit)} method can be used to block until
     *      all gRPC operations are completed and the channel is terminated.
     *  </ul>
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * Do not use this service after calling this method.  Any further method
     * calls will result in an exception.
     * </p>
     * 
     * @return <code>true</code> if connection shutdown operation was successful,
     *         <code>false</code> if connection is already shut down
     * 
     * @throws InterruptedException process was interrupted while waiting for channel to shut down
     * 
     * @see #isShutdown()
     * @see #isTerminated()
     * @see #awaitTermination(long, TimeUnit)
     */
//    @Override
    public boolean shutdownSoft() {

        if (this.grpcChan.isShutdown())
            return false;
        
        this.grpcChan.shutdown();
        
//        boolean bolShutdown = this.chnGprc.awaitTermination(this.cntTimeout, this.tuTimeout);
//        
//        if (!bolShutdown) 
//            return this.shutdownNow();
//     
        LOGGER.info("Soft shutdown initiated for connection {}", this.getClass().getName());
        return true;
    }
    
    /**
     * <p>
     * Performs a hard shutdown of the gRPC connection.
     * </p>
     * <p>
     * All requests, pre-existing and new, are immediately terminated.   
     * The connection then releases all its gRPC resources.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * Do not use this service after calling this method.  Any further method
     * calls will result in an exception.
     * </p>
     * 
     * @return <code>true</code> if hard shutdown operation was successfully initiated,
     *         <code>false</code> if connection is already shut down
     * 
     * @see #isShutdown()
     * @see #isTerminated()
     * @see #awaitTermination(long, TimeUnit)
     */
//    @Override
    public boolean shutdownNow() {
        
        if (this.grpcChan.isShutdown())
            return false;
        
        this.grpcChan.shutdownNow();
        
        LOGGER.info("Hard shutdown initiated for connection {}", this.getClass().getName());
        return true;
    }
    
    /**
     * <p>
     * Blocks until the connection finishes a shutdown operation and the underlying gRPC
     * channel fully terminates.
     * </p>
     * <p>
     * It is assumed that either a {@link #shutdownSoft()} or {@link #shutdownNow()} operation
     * was previously invoked, otherwise the method immediately returns a <code>false</code> value.
     * After returning a <code>true</code> value all gRPC operations have completed, the underlying 
     * gRPC channel has terminated, and all gRPC resource have been released.
     * </p>
     * 
     * @param cntTimeout    The timeout limit for channel shutdown and termination operations
     * @param tuTimeout     The timeout units for channel shutdown and termination operations
     * 
     * @return  <code>true</code> if connection shut down and terminated within the alloted limit,
     *          <code>false</code> either the shutdown operation was never invoked, or the operation failed 
     * 
     * @throws InterruptedException process was interrupted while waiting for channel to fully terminate
     */
    public boolean awaitTermination(long cntTimeout, TimeUnit tuTimeout) throws InterruptedException {
        if (!this.isShutdown())
            return false;
        
        boolean bolResult = this.grpcChan.awaitTermination(cntTimeout, tuTimeout);
        
        return bolResult;
    }
    
    //
    // Connection State Query
    //
    
    /**
     * <p>
     * Indicates whether or not the underlying channel has been shutdown.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned value may depend upon the type of shutdown that was issued.
     * <ul>
     * <li>If a soft shutdown was initiated ({@link #shutdownSoft()} the returned value
     *     will be <code>false</code> until all request processes have terminated.
     *     </li>
     * 
     * <li>If a hard shutdown was initiated ({@link #shutdownNow()} the returned value
     *     will return <code>true</code>, although the service may not have 
     *     finished releasing all its resources.
     *     </li>
     * </ul>
     * </p>
     * 
     * @return <code>true</code> if a shutdown was initiated, 
     *         <code>false</code> if the service is still connected
     */
//    @Override
    public boolean isShutdown() {
        return this.grpcChan.isShutdown();
    }

    /**
     * <p>
     * Indicates whether or not the connection has been fully terminated.
     * </p>
     * <p>
     * The "fully terminated" condition indicates that there are no active
     * processes and all channel resources have been released.  This condition
     * differs from that of <code>{@link #isShutdown()}</code> where a 
     * value <code>true</code> may be returned although the connection may still
     * have active processes and allocated resources. 
     * </p>
     * <p>
     * This method defers to the <code>isTerminated()</code> method of the
     * <code>ManagedChannel</code> resource.
     * </p>
     * 
     * @return <code>true</code> if there are not active processes and all resources have been releases,
     *         <code>false</code> otherwise
     */
//    @Override
    public boolean isTerminated() {
        return this.grpcChan.isTerminated();
    }
    
    
    //
    // Connection Query
    //
    
    /**
     * <p>
     * Returns the gRPC managed channel backing all Protocol Buffers communications stubs.
     * </p>
     * <p>
     * The returned channel is the Protocol Buffer managed channel object
     * that supports all communications stubs within this connection.
     * Thus, <b>be careful</b> with any operations on the returned object
     * as it will effect all communications utilizing this connection.
     * </p>
     * <p>
     * Due to the situation described above access to this method is set to 
     * package private.
     * </p>
     * <p>
     * <b>Addendum:</b> This method has been changed to <code>public</code> as this class is
     * used by multiple query objects outside the package.
     * </p>
     * 
     * @return the gRPC managed channel supporting this connection
     */
    public ManagedChannel getChannel() {
        return this.grpcChan;
    }
    
    /**
     * <p>
     * Get the synchronous blocking stub for this connection.
     * </p>
     * <p>
     * The returned object is the synchronous gRPC service handle
     * that was passed upon creation.  The object is backed by the gRPC managed
     * channel returned by the <code>{@link #getChannel()}</code> method.
     * It shares the managed channel with the asynchronous non-blocking stub in 
     * this connection.  
     * Thus, care should be used when using this object.
     * </p>
     * <p>
     * The returned stub is used to communicate with blocking, unary RPC calls.
     * </p>
     * <p>
     * Due to the situation described above access to this method is set to 
     * package private.
     * </p>
     * <p>
     * <b>Addendum:</b> This method has been changed to <code>public</code> as this class is
     * used by multiple query objects outside the package.
     * </p>
     * 
     * @return <code>QueryServiceGrpc</code> blocking stub used by this service
     * 
     * @see #getChannel()
     */
    public BlockStub getStubBlock() {
        return this.stubBlock;
    }
    
    /**
     * <p>
     * Get the non-blocking Protobuf communications stub supporting unary RPC operations.
     * </p>
     * <p>
     * The returned communications stub contains all non-blocking RPC operations but only
     * for non-streaming (unary) operations defined within the RPC interface.
     * </p>
     * 
     * @return non-blocking Protobuf communications stub containing only unary operations
     */
    public FutureStub getStubFuture() {
        return this.stubFuture;
    }
    
    /**
     * <p>
     * Get the asynchronous, non-blocking stub associated for this connection.
     * </p>
     * <p>
     * The returned object is the asynchronous gRPC query service handle
     * that was passed upon creation. The object is backed by the gRPC managed
     * channel returned by the <code>{@link #getChannel()}</code> method.
     * It shares the managed channel with the synchronous blocking stub in 
     * this connection.  
     * Thus, care should be used when using this object.
     * </p>
     * <p>
     * The returned stub is contains all RPC operations defined in the RPC interface including
     * non-blocking, streaming calls.
     * <p>
     * Due to the situation described above access to this method is set to 
     * package private.
     * </p>
     * <p>
     * <b>Addendum:</b> This method has been changed to <code>public</code> as this class is
     * used by multiple query objects outside the package.
     * </p>
     * 
     * @return non-blocking, asynchronous stub containing all streaming operations
     * 
     * @see #getChannel()
     */
    public AsyncStub getStubAsync() {
        return this.stubAsync;
    }

    
    // 
    // Support Methods
    //
    
    /**
     * <p>
     * Create a new synchronous (blocking) communications stub for the given gRPC managed
     * channel instance.
     * </p>
     * <p>
     * Uses Java reflection to locate the static method <code>newBlockingStub</code>
     * of the <code>Service</code> class (which was passed as an argument to
     * the constructor).  If found, the method is invoked with the given argument
     * to create a new synchronous blocking stub for the gRPC communication
     * service.
     * </p>
     * <p>
     * If any of the Java reflection operations fail, a exception is thrown.
     * </p>
     * 
     * @param chnGrpc       gRPC managed channel backing the synchronous blocking stub
     * 
     * @return  new synchronous blocking stub attached to the given gRPC channel,
     *          or <code>null</code> if the operation fails
     *          
     * @throws DpGrpcException  a Java reflection operation failed (see message and cause)
     */
    protected BlockStub  newBlockStub(ManagedChannel chnGrpc) throws DpGrpcException {
        try {
            Method mthNewStub = this.clsService.getMethod(STR_MTHD_NEW_BLOCKING_STUB, io.grpc.Channel.class);
            
            @SuppressWarnings("unchecked")
            BlockStub stub = (BlockStub) mthNewStub.invoke(null, chnGrpc);
            
            return stub;
            
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            
            throw new DpGrpcException(e.getMessage(), e);
        }
    };
    
    /**
     * <p>
     * Create a new synchronous (blocking) communication stub for the given gRPC managed
     * channel instance.
     * </p>
     * <p>
     * Uses Java reflection to locate the static method <code>newFutureStub</code>
     * of the <code>Service</code> class (which was passed as an argument to
     * the constructor).  If found, the method is invoked with the given argument
     * to create a new future non-blocking stub for the gRPC communication
     * service.
     * </p>
     * <p>
     * If any of the Java reflection operations fail, a exception is thrown.
     * </p>
     * 
     * @param chnGrpc       gRPC managed channel backing the synchronous blocking stub
     * 
     * @return  new synchronous blocking stub attached to the given gRPC channel,
     *          or <code>null</code> if the operation fails
     *          
     * @throws DpGrpcException  a Java reflection operation failed (see message and cause)
     */
    protected FutureStub  newFutureStub(ManagedChannel chnGrpc) throws DpGrpcException {
        try {
            Method mthNewStub = this.clsService.getMethod(STR_MTHD_NEW_FUTURE_STUB, io.grpc.Channel.class);
            
            @SuppressWarnings("unchecked")
            FutureStub stub = (FutureStub) mthNewStub.invoke(null, chnGrpc);
            
            return stub;
            
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            
            throw new DpGrpcException(e.getMessage(), e);
        }
    };
    
    /**
     * <p>
     * Create a new asynchronous (non-blocking) communications for the given gRPC managed
     * channel instance.
     * </p>
     * <p>
     * Uses Java reflection to locate the static method <code>newStub</code>
     * of the <code>Service</code> class (which was passed as an argument to
     * the constructor).  If found, the method is invoked with the given argument
     * to create a new asynchronous non-blocking stub for the gRPC communication
     * service.
     * </p>
     * <p>
     * If any of the Java reflection operations fail, a exception is thrown.
     * </p>
     * 
     * @param chnGrpc       gRPC managed channel backing the asynchronous non-blocking stub to create
     * 
     * @return  new asynchronous stub attached to the given gRPC channel,
     *          or <code>null</code> if the operation fails
     *          
     * @throws DpGrpcException  a Java reflection operation failed (see message and cause)
     */
    protected AsyncStub newAsyncStub(ManagedChannel chnGrpc) throws DpGrpcException {
        try {
            Method mthNewStub = this.clsService.getMethod(STR_MTHD_NEW_STUB, io.grpc.Channel.class);
            
            @SuppressWarnings("unchecked")
            AsyncStub stub = (AsyncStub) mthNewStub.invoke(null, chnGrpc);
            
            return stub;
            
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            
            throw new DpGrpcException(e.getMessage(), e);
        }
    };
    
}
