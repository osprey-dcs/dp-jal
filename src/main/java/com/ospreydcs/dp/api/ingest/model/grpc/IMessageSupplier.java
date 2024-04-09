/*
 * Project: dp-api-common
 * File:	IMessageSupplier.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IMessageSupplier
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
 * @since Apr 9, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.grpc;

import java.util.concurrent.TimeUnit;

import com.google.protobuf.GeneratedMessageV3;

/**
 * <p>
 * Interface defining the operations of a Protocol Buffers message supplier.
 * </p>
 * <p>
 * <h2>Message Suppliers</h2>
 * Protobuf message suppliers (i.e., classes implementing this interface) typically have a 
 * collection of available messages that should be provided with the 
 * <code>{@link #getNext()}</code> operation.  The operation can be called
 * repeatedly until the supply of Protobuf messages is exhausted, after which an exception is
 * thrown.
 * </p>
 * <p>
 * <h2>Message Consumers</h2>
 * Consumers of Protobuf messages (i.e., clients of this interface) can determine if there exists 
 * available messages by invoking the <code>{@link #hasNext()}</code> operation.  
 * Optionally, the consumer can call
 * the <code>{@link #getNext()}</code> operation until an exception is thrown.
 * </p>  
 * 
 * @param   <T>     the Protobuf message type
 * 
 * @author Christopher K. Allen
 * @since Apr 9, 2024
 *
 */
public interface IMessageSupplier<T extends GeneratedMessageV3> {

    /**
     * <p>
     * Determines whether or not the message supplier has more messages available.
     * </p>
     * 
     * @return  <code>true</code> if there are more messages to consume, <code>false, otherwise
     */
    public boolean  hasNext();
    
    /**
     * <p>
     * Returns the next available Protobuf message from the message supplier potentially blocking 
     * indefinitely until one becomes available.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This can be a blocking operation.  Although <code>{@link #hasNext()}</code> may have returned
     * <code>true</code>, the message may not yet be available.  In that case the operation may
     * block until the next message becomes available for return.
     * </li>
     * <br/>
     * <li>
     * Calling this method when <code>{@link #hashNext()} returns <code>false</code> should throw
     * an exception.
     * </li>
     * </ul>
     * </p>
     *  
     * @return  the next message available from the message supplier
     * 
     * @throws IllegalStateException called when the message supplier has no more available messages
     */
    public T    getNext() throws IllegalStateException;
    
    /**
     * <p>
     * Returns the next available Protobuf message from the message supplier blocking until the
     * given timeout limit until one becomes available.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This is a blocking operation if a message is not immediately available (although 
     * <code>{@link #hasNext()}</code> may have returned <code>true</code>, the message may not yet 
     * be available).  
     * </li>
     * <br/>
     * <li>
     * If no message becomes available within the timeout limit the method returns a <code>null>/code>
     * value (which should be checked).
     * </li>
     * <br/>
     * <li>
     * Calling this method when <code>{@link #hashNext()} returns <code>false</code> should throw
     * an exception.
     * </li>
     * </ul>
     * </p>
     * 
     * @param cntTimeout    timeout limit
     * @param tuTimeout     time units for timeout limit
     * 
     * @return  the next message available from the message supplier, 
     *          or <code>null</code> if timeout limit exceeded
     *          
     * @throws IllegalStateException called when the message supplier has no more available messages
     */
    public T    poll(int cntTimeout, TimeUnit tuTimeout) throws IllegalStateException;
}
