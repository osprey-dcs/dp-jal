/*
 * Project: dp-api-common
 * File:	DpGrpcStreamType.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	DpGrpcStreamType
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
 * @since Feb 21, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.common;

/**
 * <p>
 * Enumeration of general gRPC data stream types supported by the Data Platform.
 * </p>
 * <p>
 * There are 3 types of gRPC data streams available:
 * <ul>
 * <li>
 * <code>{@link #FORWARD}</code> - Unidirectional <em>forward</em> gRPC data stream from client to service.  
 * A single data stream is established between the client and the service transmitting messages from client to
 * service ONLY.  Transmission rates are determined by the client (although service may force back pressure).
 * </li>
 * <br/>
 * <li>
 * <code>{@link #BACKWARD}</code> - Unidirectional <em>backward</em> gRPC data stream from service to client.  
 * A single data stream is established between the client and the service transmitting messages from service to
 * client ONLY.  Transmission rates are determined by the service (although client may force back pressure).
 * </li>
 * <br/>
 * <code>{@link #BIDIRECTIONAL}</code> - Fully bidirectional gRPC data stream between client and service.  
 * Both a "forward stream" and a "backward stream" are established between client and service.  Messages are
 * transmitted by both client and service according to an agreed protocol.  Transmission rates are determined
 * by either the client, or the service, or both and usually involved some type of synchronization within the
 * agreed protocol. 
 * </li>
 * </ul>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * The gRPC data stream type can affect transmission performance.  Generally, the unidirectional stream will have
 * better performance as messages are transmitted as fast as available.
 * </li>
 * <br/>
 * <li>
 * Bidirectional streams allow synchronization between client and service.  This can be beneficial when
 * network traffic is high, or either the client or service has expensive message processing requirements.
 * However, the additional synchronization can affect overall message transmission rates.
 * </li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 21, 2024
 *
 */
public enum DpGrpcStreamType {

    /**
     * Unidirectional forward gRPC stream.
     * <p>
     * Indicates a unidirectional stream transmitting messages <em>forward</em> from client to service.
     */
    FORWARD,
    
    /**
     * Unidirectional backward gRPC stream.
     * <p>
     * Indicates a unidirectional stream transmitting messages <em>backward</em> from service to client.
     */
    BACKWARD,
    
    /**
     * Bidirectional gRPC stream.
     * <p>
     * Indicates a fully <em>bidirectional</em> stream between client and service.
     * Both client and service transmit message messages through stream. 
     */
    BIDIRECTIONAL;
}
