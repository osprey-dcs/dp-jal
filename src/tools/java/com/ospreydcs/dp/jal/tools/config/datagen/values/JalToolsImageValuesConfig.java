/*
 * Project: dp-jal
 * File:	JalToolsImageValuesConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config.datagen
 * Type: 	JalToolsImageValuesConfig
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
 * @since Nov 15, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.config.datagen.values;

import com.ospreydcs.dp.jal.common.BufferedImage;
import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing default configuration parameters for simulated image generation.
 * </p>
 * <p>
 * The attributes within this structure class are typically used with for <code>ImageFactory</code>
 * instance creation when not all configuration parameters are provided.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 15, 2025
 *
 */
public class JalToolsImageValuesConfig extends CfgStructure<JalToolsImageValuesConfig> {

    /** Default constructor required of base class */
    public JalToolsImageValuesConfig() { super(JalToolsImageValuesConfig.class); }

    
    //
    // Fields
    //
    
    /** The default separator placed between name prefix and image count */
    @ACfgOverride.Field(name="SEPARATOR")
    public String                   separator;
    
    /** The default name prefix given to all generated images (suffix is image count) */
    @ACfgOverride.Field(name="NAME_PREFIX")
    public String                   namePrefix;
    
    /** The default image format used for generated images */
    @ACfgOverride.Field(name="FORMAT")
    public BufferedImage.Format     format;
    
    /** The default image size (in bytes) used for generated images */
    @ACfgOverride.Field(name="SIZE")
    public Integer                  size;
    
}
