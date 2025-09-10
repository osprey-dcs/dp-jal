/*
 * Project: dp-api-common
 * File:	JalQueryConfig.java
 * Package: com.ospreydcs.dp.api.config.query
 * Type: 	JalQueryConfig
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
package com.ospreydcs.dp.api.config.query;

import com.ospreydcs.dp.api.config.common.JalLoggingConfig;
import com.ospreydcs.dp.api.config.common.JalTimeoutConfig;
import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing configuration parameters for Data Platform Query Service API operations.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 31, 2023
 *
 */
@ACfgOverride.Root(root="DP_API_QUERY")
public final class JalQueryConfig extends CfgStructure<JalQueryConfig> {

    /** Default constructor required for base structure class */
    public JalQueryConfig() { super(JalQueryConfig.class); }


    //
    // Configuration Fields
    //
    
    /** Default parameters for Query Service time-series data requests and responses */
    @ACfgOverride.Struct(pathelem="DATA")
    public JalQueryRecoveryConfig    data;
    
//    /** Default concurrency parameters for Query Service operations */
//    @ACfgOverride.Struct(pathelem="CONCURRENCY")
//    public JalConcurrencyConfig      concurrency;
    
    /** Default timeout parameters for Query Service operations */
    @ACfgOverride.Struct(pathelem="TIMEOUT")
    public JalTimeoutConfig          timeout;

    /** Default logging configuration for Query Service operations */
    @ACfgOverride.Struct(pathelem="LOGGING")
    public JalLoggingConfig          logging;
}
