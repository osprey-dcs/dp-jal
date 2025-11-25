/*
 * Project: dp-jal
 * File:    JalToolsRandomConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config.datagen
 * Type:    JalToolsRandomConfig
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
 * @since Nov 24, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.config.datagen;

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;

/**
 * <p>
 * Structure class for random number generator default parameters.
 * </p>
 */
public class JalToolsRandomConfig extends CfgStructure<JalToolsRandomConfig> {
    
    /** Default constructor required for base class */
    public JalToolsRandomConfig() { super(JalToolsRandomConfig.class); };
    
    
    /** Enable/disable the use of a random number generator for value creation (random generation can be expensive) */
    @ACfgOverride.Field(name="ENABLED")
    public Boolean      enabled;
    
    /** The seed value for random number generator - use 0 for default field generation */
    @ACfgOverride.Field(name="SEED")
    public Long         seed;
}