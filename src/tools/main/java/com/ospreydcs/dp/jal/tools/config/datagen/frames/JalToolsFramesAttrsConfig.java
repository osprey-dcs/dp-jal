/*
 * Project: dp-jal
 * File:	JalToolsFramesAttrsConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config.datagen.frames
 * Type: 	JalToolsFramesAttrsConfig
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
 * @since Jan 4, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.config.datagen.frames;

import java.util.Map;

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing default attribute parameters for the default ingestion frame.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 4, 2026
 *
 */
public class JalToolsFramesAttrsConfig extends CfgStructure<JalToolsFramesAttrsConfig> {

    /** Default constructor required of base class */
    public JalToolsFramesAttrsConfig() { super(JalToolsFramesAttrsConfig.class); }
    
    
    //
    // Attributes
    //
    
    /** Enable/disable the use of the ingestion factory class attribute pairs */
    @ACfgOverride.Field(name="USE_CLASS")
    public Boolean              useClass;
    
    /** Enable/disable the use of the default attribute pairs */
    @ACfgOverride.Field(name="USE_DEFAULT")
    public Boolean              useDefault;
    
    /** Default (name, value) attribute pairs for default ingestion frame */
    @ACfgOverride.Field(name="pairs")
    public Map<String, String>  pairs;

}
