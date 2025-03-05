/*
 * Project: dp-api-common
 * File:	DpAnnotationConfig.java
 * Package: com.ospreydcs.dp.api.config.annotate
 * Type: 	DpAnnotationConfig
 *
 * Copyright 2010-2025 the original author or authors.
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
 * @since Feb 26, 2025
 *
 */
package com.ospreydcs.dp.api.config.annotate;

import com.ospreydcs.dp.api.config.common.DpLoggingConfig;
import com.ospreydcs.dp.api.config.common.DpTimeoutConfig;
import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing default configuration parameters for the Data Platform Annotation API operations.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 26, 2025
 *
 */
@ACfgOverride.Root(root="DP_API_ANNOTATION")
public class DpAnnotationConfig extends CfgStructure<DpAnnotationConfig> {

    /** Default constructor required for base structure class */
    public DpAnnotationConfig() { super(DpAnnotationConfig.class); }

    
    //
    // Configuration Fields
    //
    
    /** Default timeout parameters for Query Service operations */
    @ACfgOverride.Struct(pathelem="TIMEOUT")
    public DpTimeoutConfig      timeout;

    /** Default logging configuration for Query Service operations */
    @ACfgOverride.Struct(pathelem="LOGGING")
    public DpLoggingConfig      logging;
    
    
}
