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
package com.ospreydcs.dp.jal.tools.config.datagen.frames;

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;

/**
 * <p>
 * Structure class containing default configuration parameters for simulated ingestion frame column factories.
 * </p>
 *
 * @implNote
 * The <code>{@link ACfgOverride}</code> annotation is attached to attributes within this structure,
 * however, its use it impractical in that there are potential multiple <code>ColumnBankConfig</code>
 * structures within the JAL Tools default configuration.  The annotations are included to indicate
 * the intent and status of the field.
 * 
 * @author Christopher K. Allen
 * @since Dec 1, 2025
 *
 */
public class JalToolsColumnsConfig extends CfgStructure<JalToolsColumnsConfig> {

    /** Default constructor required of base class */
    public JalToolsColumnsConfig() { super(JalToolsColumnsConfig.class); }

    
//    //
//    // Attributes
//    //
//    
//    public List<ColumnBankConfig>       columns;
//    
//    //
//    // Internal Types
//    //
//    
//    /**
//     * <p>
//     * Structure class containing the default parameters for a bank of ingestion frame data columns.
//     * </p>
//     * 
//     * @implNote
//     * The <code>{@link ACfgOverride}</code> annotation is attached to attributes within this structure,
//     * however, its use it impractical in that there are potential multiple <code>ColumnBankConfig</code>
//     * structures within the JAL Tools default configuration.  The annotations are included to indicate
//     * the intent and status of the field.
//     */
//    public static class ColumnBankConfig extends CfgStructure<ColumnBankConfig> {
//        
//        /** Default constructor required of base class */
//        public ColumnBankConfig()   { super(ColumnBankConfig.class); };
//        
        
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
        @ACfgOverride.Field(name="COL_TYPE")
        public JalComplexType    colType;
        
        /** The data type of all heterogenerous values in the columns */
        @ACfgOverride.Field(name="DATA_TYPE")
        public JalScalarType    dataType;
        
//    }
}
