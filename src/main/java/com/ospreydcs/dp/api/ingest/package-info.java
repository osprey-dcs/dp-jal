/*
 * Project: dp-api-common
 * File:	package-info.java
 * Package: com.ospreydcs.dp.api.ingest
 * Type: 	package-info
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
 * @since Mar 28, 2024
 *
 * TODO:
 * - None
 */
/**
 * <p>
 * Package containing classes and resources for interaction with the Data Platform Ingestion Service.
 * </p>
 * <p>
 * This is the top-level package supporting client interactions with the Ingestion Service.  Contains
 * most resources necessary for data ingestion by the Ingestion Service.
 * </p>
 * <p>
 * <h2>Ingestion Service Interfaces</h2>   
 * There are currently 2 available interfaces with the Ingestion Service:
 * <ol>
 * <li><code>{@link DpIngestionService}</code>: 
 *   Basic interaction with the Ingestion Service where the underlying 
 *   data transmission mechanism is unary RPC.  This interface is intended for clients with 
 *   minimal data ingestion requirements.
 *   <ul>
 *   <li>Ingestion frames may be size limited due to message size limitations of gRPC.</li>
 *   <li>All data ingestion is performed synchronously using blocking operations.</li>
 *   </ul> 
 * </li>
 * <li><code>{@link DpIngestionStream}</code>: 
 *   Advanced interaction with the Ingestion Service where the underlying
 *   data transmission mechanism is supported by gRPC data streams.  (In fact, multiple, concurrent
 *   data stream may be used depending upon the client API library configuration.)  This interface
 *   is intended for clients with large ingestion requirements.
 *   <ul>
 *   <li>Ingestion frames are not size limited - they are decomposed internally if too large.</li>
 *   <li>All data ingestion is performed asynchronously using non-blocking operations.</li>
 *   <li>Back pressure can be enabled forcing clients to feel bock-logged Ingestion Service processing.</li>
 *   </ul>
 * </ol>
 * </p> 
 * <p>
 * <h2>Unit of Ingestion</h2>
 * All Ingestion Service interfaces support data ingestion using the instances of the 
 * <code>{@link IngestionFrame}</code> class.  It is the responsibility of clients to properly
 * populate instances so they are accepted by Ingestion Service interfaces.
 * </p>
 * <p>
 * <code>IngestionFrame</code> objects are essentially table data where columns represent 
 * correlated, time-series data produced by data sources (i.e., process variables) and the
 * rows are the correlated, time-series data values.  The <code>IngestionFrame</code> also
 * supports additional metadata to be provided by clients, such as attribute pairs, snapshot
 * support, labels, and frame timestamps.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 28, 2024
 *
 */
package com.ospreydcs.dp.api.ingest;