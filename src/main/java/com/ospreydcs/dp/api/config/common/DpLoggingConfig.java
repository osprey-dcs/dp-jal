/*
 * Project: dp-api-common
 * File:	DpLoggingConfig.java
 * Package: com.ospreydcs.dp.api.config.common
 * Type: 	DpLoggingConfig
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
 * @since Jan 8, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config.common;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;
import com.ospreydcs.dp.api.model.AUnavailable;
import com.ospreydcs.dp.api.model.AUnavailable.STATUS;

/**
 * <p>
 * Structure class containing default configuration parameters for Data Platform logging operations.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 8, 2024
 *
 */
@ACfgOverride.Root(root="DP_API_LOGGING")
public final class DpLoggingConfig extends CfgStructure<DpLoggingConfig>{

    /** Default constructor required for base structure class */
    public DpLoggingConfig() {
        super(DpLoggingConfig.class);
    }

    //
    // Configuration Fields
    //
    
    /** Is logging active or not */
    @ACfgOverride.Field(name="ACTIVE")
    public Boolean  active;
    
    /** Logging level */
    @AUnavailable(status=STATUS.UNDER_REVIEW)
    @ACfgOverride.Field(name="LEVEL")
    public String   level;
}
