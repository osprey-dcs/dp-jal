/*
 * Project: dp-api-common
 * File:    JalToolsConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config
 * Type:    JalTestArchiveConfig
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
 * @since May 4, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.config.archive;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure class defining Data Platform Test Archive parameters.
 * </p>
 */
@ACfgOverride.Root(root="JAL_TEST_ARCHIVE")
public class JalTestArchiveConfig extends CfgStructure<JalTestArchiveConfig>{

    /** Default constructor required for base class */
    public JalTestArchiveConfig() { super(JalTestArchiveConfig.class); }
    
    
    //
    // Structure Fields
    //
    
    /** The Data Platform Test Archive sampling range for all test PVs */
    @ACfgOverride.Struct(pathelem="RANGE")
    public SampleRange                  range;
    
    /** The Data Platform Test Archive Process Variable (PV) configuration parameters */
    @ACfgOverride.Struct(pathelem="PVS")
    public JalTestArchivePvsConfig      pvs;
    
    
    
    //
    // Internal Structure Classes
    //
    
    /**
     * Structure class containing Data Platform Test Archive sampling range
     */
    public static class SampleRange extends CfgStructure<SampleRange> {
        
        /** Required default constructor for base class */
        public SampleRange() { super(SampleRange.class); };
        
        
        //
        // Structure Fields
        //
        
        /** The first timestamp of all data sources within the Data Platform test archive, i.e., the archive inception instant */
        @ACfgOverride.Field(name="START")
        public String       start;
        
        /** The last timestamp of all data sources within the Data Platform test archive */
        @ACfgOverride.Field(name="END")
        public String       end; 
        
    }
}