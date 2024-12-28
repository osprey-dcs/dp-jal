/*
 * Project: dp-api-common
 * File:    DpQueryStreamType.java
 * Package: com.ospreydcs.dp.api.query
 * Type:    DpQueryStreamType
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
 * @since Sep 23, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model;

/**
 * <p>
 * Enumeration of gRPC data streaming services currently supported by the Query Service.
 * </p>
 * <p>
 * Currently there are two type of gRPC data streams available for Query Service data requests:
 * <ul>
 * <li>
 * <code>{@link #UNIDIRECTIONAL}</code> - Unidirectional gRPC data streaming.  A single data stream is 
 * established between the client and the Query Service.  This is a "backward stream" transmitting request data
 * from the Query Service to the client.  Transmission rates are determined by the Query Service.
 * </li>
 * <br/>
 * <code>{@link #BIDIRECTIONAL}</code> - Bidirectional gRPC data streaming.  Both a "forward stream" and a 
 * "backward stream" are established between client and Query Service.  The backward stream sends data messages
 * containing requested data back to the client.  The forward stream sends data receipt "acknowledgments" forward
 * to the Query Service.  The client is able to control data transmission rates via the acknowledgment process.
 * </li>
 * </ul>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * The gRPC data stream type can affect transmission performance.  Generally, the unidirectional stream will have
 * better performance as the Query Service typically transmits requested data as fast as it is available.
 * </li>
 * <br/>
 * <li>
 * Bidirectional streams provide synchronization between client and Query Service.  This can be beneficial when
 * network traffic is high, or if the client has expensive processing requirements for each "data page" recovered.
 * However, the additional synchronization between client and Query Service can affect the recovery time of 
 * a data request.
 * </li>
 * </ul>
 * </p>
 * 
 * @deprecated  Replaced by DpGrpcStreamType
 */
@Deprecated(since="Dec 28, 2024", forRemoval=true)
public enum DpQueryStreamType {
    
    /**
     * Unidirectional gRPC Stream.
     * <p>
     * Indicates a unidirectional (backward) stream from the Query Service to this client.
     */
    UNIDIRECTIONAL,
    
    
    /**
     * Bidirectional gRPC Stream.
     * <p>
     * Indicates a fully bidirectional stream between the Client and the Query Service.
     * Client explicitly requests each data page (within QueryResponse message) from the
     * Query Service. 
     */
    BIDIRECTIONAL;
}