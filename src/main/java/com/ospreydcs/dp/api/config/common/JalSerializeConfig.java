/*
 * Project: dp-jal
 * File:	JalSerializeConfig.java
 * Package: com.ospreydcs.dp.api.config.common
 * Type: 	JalSerializeConfig
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
 * @since Sep 10, 2025
 *
 */
package com.ospreydcs.dp.api.config.common;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing parameters for <code>IngestionFrame</code> and data recovery serialization.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Sep 10, 2025
 *
 */
public class JalSerializeConfig extends CfgStructure<JalSerializeConfig> {

    /** Default constructor required for base class */
    public JalSerializeConfig() { super(JalSerializeConfig.class);  }
    
    
    // 
    // Configuration Parameters
    //
    
    /** Is serialization enabled */
    @ACfgOverride.Field(name="ENABLED")
    public Boolean      enabled;

}
