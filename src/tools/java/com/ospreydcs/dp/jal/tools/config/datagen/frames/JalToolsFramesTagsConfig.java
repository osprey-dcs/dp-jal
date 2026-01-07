/*
 * Project: dp-jal
 * File:	JalToolsFramesTagsConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config.datagen.frames
 * Type: 	JalToolsFramesTagsConfig
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

import java.util.List;

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing default tag value parameters for the default ingestion frame.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 4, 2026
 *
 */
public class JalToolsFramesTagsConfig extends CfgStructure<JalToolsFramesTagsConfig> {

    /** Default constructor required of base class */
    public JalToolsFramesTagsConfig() { super(JalToolsFramesTagsConfig.class); }

    
    //
    // Attributes
    //
    
    /** Enable/disable the use of frame factory class tag values for the default ingestion frame */
    @ACfgOverride.Field(name="USE_CLASS")
    public Boolean          useClass;
    
    /** Enable/disable the use of default tag values for the default ingestion frame */
    @ACfgOverride.Field(name="USE_DEFAULT")
    public Boolean          useDefault;
    
    /** The list of default tag values for the default ingestion frame */
    @ACfgOverride.Field(name="VALUES")
    public List<String>     values;
}
