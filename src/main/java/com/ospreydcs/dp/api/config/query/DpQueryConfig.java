/*
 * Project: dp-api-common
 * File:	DpQueryConfig.java
 * Package: com.ospreydcs.dp.api.config.query
 * Type: 	DpQueryConfig
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

import com.ospreydcs.dp.api.config.common.DpConcurrencyConfig;
import com.ospreydcs.dp.api.config.common.DpLoggingConfig;
import com.ospreydcs.dp.api.config.common.DpTimeoutConfig;
import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;
import com.ospreydcs.dp.api.model.AUnavailable;
import com.ospreydcs.dp.api.model.AUnavailable.STATUS;

/**
 * <p>
 * Structure class containing default configuration parameters for Data Platform Query Service operations.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 31, 2023
 *
 */
@ACfgOverride.Root(root="DP_API_QUERY")
public final class DpQueryConfig extends CfgStructure<DpQueryConfig> {

    /** Default constructor required for base structure class */
    public DpQueryConfig() {
        super(DpQueryConfig.class);
    }


    //
    // Configuration Fields
    //
    
    /** Default bucket count per page when using cursor requests */
    @AUnavailable(status=STATUS.UNDER_REVIEW)
    @ACfgOverride.Field(name="PAGE_SIZE")
    public Integer  pageSize;
    
    /** Default timeout parameters for Query Service operations */
    @ACfgOverride.Struct(pathelem="TIMEOUT")
    public DpTimeoutConfig      timeout;

    /** Default logging configuration for Query Service operations */
    @ACfgOverride.Struct(pathelem="LOGGING")
    public DpLoggingConfig      logging;
    
    /** Default concurrency parameters for Query Service operations */
    @ACfgOverride.Struct(pathelem="CONCURRENCY")
    public DpConcurrencyConfig  concurrency;
    
    /** Default parameters for Query Service data request queries */
    @ACfgOverride.Struct(pathelem="DATA_REQUEST")
    public DpDataRequestConfig  dataRequest;
}
