/*
 * Project: dp-api-common
 * File:	DpConcurrencyConfig.java
 * Package: com.ospreydcs.dp.api.config.common
 * Type: 	DpConcurrencyConfig
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
 * @since Jan 30, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config.common;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 *
 * @author Christopher K. Allen
 * @since Jan 30, 2024
 *
 */
@ACfgOverride.Root(root="DP_API_CONCURRENCY")
public class DpConcurrencyConfig extends CfgStructure<DpConcurrencyConfig> {

    /** Default constructor required for base structure class */
    public DpConcurrencyConfig() {
        super(DpConcurrencyConfig.class);
    }

    //
    // Configuration Parameters
    //
    
    /** Is multi-threading active or not */
    @ACfgOverride.Field(name="ACTIVE")
    public Boolean  active;
    
    /** general size parameter to induce pivoting to concurrency (context dependent) */
    @ACfgOverride.Field(name="PIVOT_SIZE")
    public Integer  pivotSize;
    
    /** general thread count for concurrent operations */
    @ACfgOverride.Field(name="THREAD_COUNT")
    public Integer  threadCount;
}
