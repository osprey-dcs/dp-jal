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

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;
import com.ospreydcs.dp.jal.tools.config.datagen.cols.JalToolsColumnsConfig;

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
    
    /** Default ingestion frame label prefix given to all generated ingestion frames */
    @ACfgOverride.Field(name="LABEL")
    public String                       label;
    
    /** Default ingestion frame payload size (i.e., number of frames) */
    @ACfgOverride.Field(name="COUNT")
    public Integer                      count;
    
    /** Default tag values for ingestion frames */
    @ACfgOverride.Struct(pathelem="TAGS")
    public JalToolsFramesTagsConfig     tags;
    
    /** Default attribute pairs for ingestion frames */
    @ACfgOverride.Struct(pathelem="ATTRIBUTES")
    public JalToolsFramesAttrsConfig    attributes;
    
    /** Default timestamp generation parameters for simulated ingestion frames */
    @ACfgOverride.Struct(pathelem="TIMESTAMPS")
    public JalToolsFramesTmsConfig      timestamps;
    
    /** Default column configurations for simulated data column generation */
    @ACfgOverride.Field(name="COLUMNS")
    public List<JalToolsColumnsConfig>  columns;
    
}
