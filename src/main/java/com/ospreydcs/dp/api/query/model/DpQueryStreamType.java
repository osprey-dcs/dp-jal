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
 * Enumeration of gRPC data streaming services currently supported by the Query Service.
 */
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