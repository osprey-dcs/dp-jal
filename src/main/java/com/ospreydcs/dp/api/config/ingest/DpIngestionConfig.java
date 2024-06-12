/*
 * Project: dp-api-common
 * File:	DpIngestionConfig.java
 * Package: com.ospreydcs.dp.api.config.ingest
 * Type: 	DpIngestionConfig
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
package com.ospreydcs.dp.api.config.ingest;

import com.ospreydcs.dp.api.config.common.DpConcurrencyConfig;
import com.ospreydcs.dp.api.config.common.DpLoggingConfig;
import com.ospreydcs.dp.api.config.common.DpTimeoutConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcStreamConfig;
import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing configuration parameters for the Ingestion Service client API.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 28, 2024
 *
 */
@ACfgOverride.Root(root="DP_API_INGEST")
public class DpIngestionConfig extends CfgStructure<DpIngestionConfig> {

    /** Default constructor for super class structure */
    public DpIngestionConfig() { super(DpIngestionConfig.class); }
    
    
    //
    // Configuration Fields
    //
    
    /** Default parameters for general Query Service gRPC streaming operations */
    @ACfgOverride.Struct(pathelem="STREAM")
    public DpGrpcStreamConfig   stream;
    
    /** Default concurrency parameters for Query Service operations */
    @ACfgOverride.Struct(pathelem="CONCURRENCY")
    public DpConcurrencyConfig  concurrency;
    
    /** Default timeout parameters for Query Service operations */
    @ACfgOverride.Struct(pathelem="TIMEOUT")
    public DpTimeoutConfig      timeout;

    /** Default logging configuration for Query Service operations */
    @ACfgOverride.Struct(pathelem="LOGGING")
    public DpLoggingConfig      logging;
    
}
