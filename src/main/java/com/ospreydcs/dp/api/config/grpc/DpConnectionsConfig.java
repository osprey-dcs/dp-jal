/*
 * Project: dp-api-common
 * File:    DpConnectionsConfig.java
 * Package: com.ospreydcs.dp.api.config.grpc
 * Type:    DpConnectionsConfig
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
 * @since Dec 31, 2023
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config.grpc;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure containing connection parameters for Data Platform services.
 * </p>
 *
 * @see GrpcConnectionConfig
 */
@ACfgOverride.Root(root="DP_API_CONNECTIION")
public final class DpConnectionsConfig extends CfgStructure<DpConnectionsConfig> {
    
    /** Default constructor require for base structure class */
    public DpConnectionsConfig() {
        super(DpConnectionsConfig.class);
    }


    /** Data Platform Ingestion Service connection parameters */
    @ACfgOverride.Struct(pathelem="INGESTION")
    public GrpcConnectionConfig     ingestion;
    
    /** Data Platform Query Service connection parameters */
    @ACfgOverride.Struct(pathelem="QUERY")
    public GrpcConnectionConfig     query;

}