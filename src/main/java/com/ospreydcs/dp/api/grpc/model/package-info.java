/*
 * Project: dp-api-common
 * File:	package-info.java
 * Package: com.ospreydcs.dp.api.grpc.model
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
 * @since Dec 28, 2023
 *
 * TODO:
 * - None
 */
/**
 * <p>
 * Package defines the model for creating client gRPC connections with the Data Platform services.
 * </p>
 * <p>
 * Contains the common resources to build client connections with the Data Platform services.  The model
 * is based upon creating a <code>DpGrpcConnection</code> instance which maintains a gRPC <code>Channel</code>
 * object performing the actual HTTP2 network transport.  The <code>DpGrpcConnection</code> class also provides
 * clients access to all Protocol Buffers communication stubs available for a gRPC service defined in the 
 * {@link com.ospreydcs.dp.grpc} package (i.e., within the <code>protoc</code> source files). 
 * </p>
 * <p>
 * The <em>connection factory</em> class <code>DpGrpcConnectionFactory</code> is provided as utility for
 * creating <code>DpGrpcConnection</code> instances.  The class offers multiple <code>connect</code> methods
 * for creating and configuring connections to desired Data Platform services.  The multiple method signatures
 * allow clients to specify common parameters such as the host address, timeout limits, TLS security options,
 * etc.  The <code>connect()</code> method (without arguments) is also available which creates a connection
 * using all default parameters for that service (which are specified in project configuration files, environment
 * variables, or command-line start options.
 * </p>
 * <p>
 * <h2>Java Generics</h2>
 * Note that both <code>DpGrpcConnection</code> and <code>DpGrpcConnectionFactory</code> are both generic
 * classes with multiple template parameters.  The template parameters are needed to specify the targeted Data 
 * Platform service and the corresponding types of the communication stubs for that service.  Thus, it is 
 * convenient to create concrete classes specific for each Data Platform service.  Otherwise, due to the
 * abundance of generic parameters, typed code can be cumbersome and confusing when using these
 * classes explicitly. The intent here is to centralize gRPC resource creation in order to facilitate
 * code maintenance and upgradeability.
 * </p>
 * <p>
 * There are three main classes forming the gRPC connection model:
 * <ul>
 * <li>
 * {@link DpGrpcConnection} - generic class defining the connection to a Data Platform service.
 * </li>
 * <li>
 * {@link DpGrpcConnectionFactoryBase} - generic utility class for creating connections in variable configurations.
 * </li>
 * <li>
 * {@link DpGrpcException} - Exception class thrown for general gRPC connection errors (see message and cause).
 * </li>
 * </ul>
 * </p>
 * <p>
 * Connection implementations for specific Data Platform services are found in sub-packages.  These are 
 * composed of fly-weight classes for enforcing type and using the above resources for the underlying mechanism.    
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Dec 28, 2023
 *
 */
package com.ospreydcs.dp.api.grpc.model;