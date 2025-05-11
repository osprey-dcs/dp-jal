/*
 * Project: dp-api-common
 * File:	IConnection.java
 * Package: com.ospreydcs.dp.api.grpc
 * Type: 	IConnection
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
 * @since Nov 17, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.model;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Interface specifying the requirements of a gRPC channel connection.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 17, 2022
 */
public interface IConnection {

    /**
     * <p>
     * Does a soft shutdown of the gRPC Channel connection.
     * </p>
     * <p>
     * Shuts down the service connection such that any new requests are 
     * cancelled but any currently executing processes are allowed to finish.  
     * Once all processes are finished the connection releases 
     * all gRPC resources.
     * </p>
     * <p>
     * Note that if the soft shutdown process fails within the allotted timeout 
     * limit a hard shutdown is invoked.  The result of this hard shutdown is
     * then returned.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * Do not use this service after calling this method.  Any further method
     * calls will result in an exception.
     * </p>
     * 
     * @return <code>true</code> if connection was successfully shutdown,
     *         <code>false</code> if an error occurred
     * 
     * @throws InterruptedException process was interrupted while waiting for channel to shut down
     */
    public boolean shutdown() throws InterruptedException;
    
    /**
     * <p>
     * Performs a hard shutdown of the gRPC Channel connection.
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
     * @return <code>true</code> if connection was successfully shutdown,
     *         <code>false</code> if an error occurred
     * 
     * throws InterruptedException process was interrupted while waiting for channel to shut down
     */
    public boolean shutdownNow() /* throws InterruptedException */;
    
    
    /**
     * <p>
     * Blocks until the connection finishes a shutdown operation and the underlying gRPC
     * channel fully terminates.
     * </p>
     * <p>
     * It is assumed that either a {@link #shutdown()} or {@link #shutdownNow()} operation
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
    public boolean awaitTermination(long cntTimeout, TimeUnit tuTimeout) throws InterruptedException;
    
    /**
     * <p>
     * Blocks until the connection finishes a shutdown operation and the underlying gRPC channel fully terminates.
     * </p>
     * <p>
     * Typically defers to the method {@link #awaitTermination(long, java.util.concurrent.TimeUnit)} 
     * by supplying parameters from the default configuration for the Query Service connection.
     * </p> 
     * 
     * @return  <code>true</code> if connection shut down and terminated within the alloted limit, 
     *          <code>false</code> either the shutdown operation was never invoked, or the operation failed
     *          
     * @throws InterruptedException process was interrupted while waiting for channel to fully terminate
     * 
     * @see #awaitTermination(long, java.util.concurrent.TimeUnit)
     */
    public boolean awaitTermination() throws InterruptedException;

    
    //
    // Connection State Query
    //
    
    /**
     * <p>
     * Indicates whether or not the gRPC Channel has been shutdown.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned value may depend upon the type of shutdown that was issued.
     * <ul>
     * <li>If a soft shutdown was initiated ({@link #shutdown()} the returned value
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
    public boolean isShutdown();

    /**
     * <p>
     * Indicates whether or not the connection has been fully terminated.
     * </p>
     * <p>
     * The "fully terminated" condition indicates that there are no enabled
     * processes and all channel resources have been released.  This condition
     * differs from that of <code>{@link #isShutdown()}</code> where a 
     * value <code>true</code> may be returned although the connection may still
     * have enabled processes and allocated resources. 
     * </p>
     * <p>
     * This method defers to the <code>isTerminated()</code> method of the
     * <code>ManagedChannel</code> resource.
     * </p>
     * 
     * @return <code>true</code> if there are not enabled processes and all resources have been releases,
     *         <code>false</code> otherwise
     */
    public boolean isTerminated();
    
}
