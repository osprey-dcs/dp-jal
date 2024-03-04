/*
 * Project: dp-api-common
 * File:	package-info.java
 * Package: com.ospreydcs.dp.api.grpc.query
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
 * Package containing gRPC connection and factory for the Data Platform Query Service. 
 * </p>
 * <p>
 * The resources bind the generic parameters of connection model classes in 
 * <code>{@link com.ospreydcs.dp.grpc.model}</code> to the Query Service.  
 * These are provided as a convenience and to ensure type safety.
 * </p>
 * <p>
 * Connection factory implementations are also available to create <code>DpQueryConnection</code>
 * instance with various configurations.  These are generally not needed by clients but
 * may be useful within the API Library implementation.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 28, 2023
 *
 */
package com.ospreydcs.dp.api.grpc.query;