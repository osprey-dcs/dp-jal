/*
 * Project: dp-jal
 * File:	JalToolsDataGenConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config.datagen
 * Type: 	JalToolsDataGenConfig
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
 * @since Nov 6, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.config.datagen;

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing the default configuration parameters for the JAL Tools simulated data generation.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 6, 2025
 *
 */
@ACfgOverride.Root(root="JAL_TOOLS_DATAGEN")
public class JalToolsDataGenConfig extends CfgStructure<JalToolsDataGenConfig> {

    /** Default constructor required for base class. */
    public JalToolsDataGenConfig() { super(JalToolsDataGenConfig.class);  }

    
    //
    // Fields
    //
    
    /** Default configuration parameters for data value generation */
    @ACfgOverride.Struct(pathelem="VALUES")
    public Values           values;
    
    
    //
    // Internal Types
    //
    
    /**
     * Structure class for default simulated data value generation - all types. 
     */
    @ACfgOverride.Root(root="JAL_TOOLS_DATAGEN_VALUES")
    public static class Values extends CfgStructure<Values> {
        
        /** Default constructor required for base class */
        public Values() { super(Values.class); };
        
        
        //
        // Fields
        //
        
        /** Default configuration parameters for scalar value generation */
        @ACfgOverride.Struct(pathelem="VALUES")
        public JalToolsScalarValuesConfig       scalar;
        
        /** Default configuration parameters for structure value generation */
        @ACfgOverride.Struct(pathelem="STRUCTURE")
        public JalToolsStructValuesConfig       structure;
        
        /** Default configuration parameters for image value generation */
        @ACfgOverride.Struct(pathelem="IMAGE")
        public JalToolsImageValuesConfig        image;
        
        /** Default configuration parameters for timestamp value generation */
        @ACfgOverride.Struct(pathelem="TIMESTAMP")
        public JalToolsTmsValuesConfig          timestamp;
        
    }
}
