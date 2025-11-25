/*
 * Project: dp-jal
 * File:	JalToolsStructValuesConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config.datagen
 * Type: 	JalToolsStructValuesConfig
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
 * @since Nov 14, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.config.datagen;

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing default configuration parameters for <code>StructureFactory</code> instance creation. 
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 14, 2025
 *
 */
public final class JalToolsStructValuesConfig extends CfgStructure<JalToolsStructValuesConfig> {

    /** Default constructor required of base class */
    public JalToolsStructValuesConfig() { super(JalToolsStructValuesConfig.class); }
    
    
    // 
    // Field Values
    //
    
    /** The parameters used to build structure field names */
    @ACfgOverride.Struct(pathelem="FIELD_NAMES")
    public FieldNames       fieldNames;
    
    
    //
    // Internal Structure Classes
    //
    
    /**
     * Structure class containing structure field name creation properties. 
     */
    public static final class FieldNames extends CfgStructure<FieldNames> {
        
        /** Default constructor required of base class */
        public FieldNames() { super(FieldNames.class); };
        
        
        //
        // Fields
        //
        
        /** The prefix for structure field names, full name contains field index */
        @ACfgOverride.Field(name="PREFIX")
        public String   prefix;
        
        /** The separator placed between field name prefix and field index, or between field indices */
        @ACfgOverride.Field(name="SEPARATOR")
        public String   separator;
        
        /** The name extension properties used to produce unique field names within a structure factory */
        @ACfgOverride.Struct(pathelem="UNIQUE")
        public Unique   unique;
        
        
        //
        // Internal Structure Classes
        //
        
        /**
         *  Structure class containing default parameters for enabling/disabling and the creation of 
         *  unique structure field names
         */
        public static final class Unique extends CfgStructure<Unique> {
            
            /** Default constructor required of base class */
            public Unique() { super(Unique.class); };
            
            
            //
            // Fields
            //
            
            /** Enable/disable unique field name generation for structure instances */
            @ACfgOverride.Field(name="ENABLED")
            public Boolean  enabled;
            
            /** The prefix for the unique token prepended to unique field names (indicates structure count) */
            @ACfgOverride.Field(name="PREFIX")
            public String   prefix;
            
            /** The separator placed between unique (structure) prefix and structure count */
            @ACfgOverride.Field(name="SEPARATOR1")
            public String   separator1;
            
            /** The separator placed between the structure count token and the standard structure field name */
            @ACfgOverride.Field(name="SEPARATOR2")
            public String   separator2;
        }
        
    }

}
