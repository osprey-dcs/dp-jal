/*
 * Project: dp-jal
 * File:	JalToolsColumnsConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config.datagen.frames
 * Type: 	JalToolsColumnsConfig
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
 * @since Dec 1, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.config.datagen.cols;

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;

/**
 * <p>
 * Structure class containing default configuration parameters for simulated ingestion frame column factories.
 * </p>
 *
 * @implNote
 * The <code>{@link ACfgOverride}</code> annotation is attached to attributes within this structure,
 * Note that its use is impractical where there are multiple <code>JalToolsColumnsConfig</code>
 * structures within the JAL Tools <code>JalToolsFramesConfig</code> default configuration.  
 * In that case the annotations indicate the intent and status of the field.
 * 
 * @author Christopher K. Allen
 * @since Dec 1, 2025
 *
 */
public class JalToolsColumnsConfig extends CfgStructure<JalToolsColumnsConfig> {

    /** Default constructor required of base class */
    public JalToolsColumnsConfig() { super(JalToolsColumnsConfig.class); }

    
    //
    // Attributes
    //

    /** Name of the column bank - also the prefix given to all columns within bank */
    @ACfgOverride.Field(name="NAME")
    public String           name;

    /** The number of columns in the column bank */
    @ACfgOverride.Field(name="COUNT")
    public Integer          count;

    /** The heterogeneous data type of all columns in this bank */
    @ACfgOverride.Field(name="TYPE")
    public JalComplexType   type;

    /** The datum factory parameters for data column value generation */
    @ACfgOverride.Field(name="FACTORY")
    public String[]         factory;

}
