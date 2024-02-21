/*
 * Project: dp-api-common
 * File:	package-info.java
 * Package: com.ospreydcs.dp.api.config
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
 * @since Dec 19, 2023
 *
 * TODO:
 * - None
 */

/**
 * <p>
 * Package containing configuration resources and utilities common to all Data Platform
 * API libraries.
 * </p>
 * <p>
 * The package provides utilities for common resource configuration management.  In particular,
 * default configuration parameters are assumed to be supplied as YAML formatted files.  The mechanism 
 * assumes that system <em>environment variables</em> can overwrite some parameters specified in the 
 * YAML configuration files.  Thus, the configuration mechanism contains the following resources:
 * <ul>
 * <li>
 * <code>CfgLoaderYaml</code> - A utility class capable of loading YAML configuration files into appropriate
 * structure classes.
 * </li>
 * <li>
 * <code>CfgOverrideUtility</code> - A utility class that overwrites the parameters within a configuration
 * structure class with values obtained from the system environment.
 * </li>
 * <li>
 * <code>ACfgOverride</code> - Annotation class used for marking fields within a configuration structure class
 * as targets of value overwrite with environment variable values.
 * </li>
 * </ul> 
 * The above utilities and annotations are used together to create the complete configuration mechanism for
 * API libraries.
 * </p>
 * <p>
 * Sub-packages within this package contain actual configuration structure classes for common operations.
 * For example, consider the following:
 * <ul>
 * <li>{@link com.ospreydcs.dp.api.config.db.MongoDbConfig} - (Default) configuration parameters for MongoDD
 * access.
 * </li>
 * <li>{@link com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig} - Configuration parameters for a gRPC channel
 * connection.
 * </li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 19, 2023
 *
 */
package com.ospreydcs.dp.api.config;