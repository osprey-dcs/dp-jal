/*
 * Project: dp-jal
 * File:	JalToolsFramesConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config.datagen
 * Type: 	JalToolsFramesConfig
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
 * @since Nov 28, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.config.datagen.frames;

import java.util.List;
import java.util.Map;

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing default configuration parameters for simulated ingestion frame generation.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Nov 28, 2025
 *
 */
public class JalToolsFramesConfig extends CfgStructure<JalToolsFramesConfig> {

    /** Default constructor required of base class */
    public JalToolsFramesConfig() { super(JalToolsFramesConfig.class); }

    
    //
    // Attributes
    //
    
    /** Default tag values for ingestion frames */
    @ACfgOverride.Field(name="TAGS")
    public List<String>                 tags;
    
    /** Default attribute pairs for ingestion frames */
    @ACfgOverride.Field(name="ATTRIBUTES")
    public Map<String, String>          attributes;
    
    /** Default timestamp generation parameters for simulated ingestion frames */
    @ACfgOverride.Struct(pathelem="TIMESTAMPS")
    public JalToolsFramesTmsConfig      timestamps;
    
    /** Default column configurations for simulated data column generation */
    @ACfgOverride.Field(name="COLUMNS")
    public List<JalToolsColumnsConfig>  columns;
    
}
