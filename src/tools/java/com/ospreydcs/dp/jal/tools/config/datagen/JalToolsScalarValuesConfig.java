/*
 * Project: dp-jal
 * File:	JalToolsScalarValuesConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config.datagen
 * Type: 	JalToolsScalarValuesConfig
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
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;

import com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory;


/**
 * <p>
 * Structure class containing default parameter values for scalar-valued simulated data generation.
 * </p>
 * <p>
 * The parameters within this structure class are used for default configurations of the
 * JAL Tools <code>{@link ScalarFactory}</code> instances. Note that the scalar-value generator
 * class is used to generated field values within more complex data structures such as arrays and
 * structures.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 6, 2025
 *
 */
@ACfgOverride.Root(root="JAL_TOOLS_DATAGEN_VALUES_SCALAR")
public class JalToolsScalarValuesConfig extends CfgStructure<JalToolsScalarValuesConfig> {

    /** Default constructor required from base class */
    public JalToolsScalarValuesConfig()    { super(JalToolsScalarValuesConfig.class); };
    
    
    //
    // Fields
    //
    
    /** The default scalar value type when none is given */
    @ACfgOverride.Field(name="TYPE")
    public JalScalarType    type;
    
    /** The prefix used for all string value generation - numeric value used as suffix */
    @ACfgOverride.Field(name="STRING_PREFIX")
    public String           stringPrefix;
    
    /** Random number generator value creation parameters if used, otherwise use incremental value creation */
    @ACfgOverride.Struct(pathelem="RANDOM")
    public Random           random;
    
    /** Incremental number generation default parameters (used when random number generation disabled) */
    @ACfgOverride.Struct(pathelem="INCREMENT")
    public Increment        increment;
    
    
    //
    // Internal Structure Classes
    //
    
    /**
     *  Structure class for scalar value random number generator default parameters
     */
    @ACfgOverride.Root(root="JAL_TOOLS_DATAGEN_VALUES_SCALAR_RANDOM")
    public static class Random extends CfgStructure<Random> {
        
        /** Default constructor required for base class */
        public Random() { super(Random.class); };
        
        
        /** Enable/disable the use of a random number generator for value creation (random generation can be expensive) */
        @ACfgOverride.Field(name="ENABLED")
        public Boolean      enabled;
        
        /** The seed value for random number generator - use 0 for default field generation */
        @ACfgOverride.Field(name="SEED")
        public Long         seed;
    }
    
    /**
     * Structure class for scalar value incremental number generation default parameters
     */
    @ACfgOverride.Root(root="JAL_TOOLS_DATAGEN_VALUES_SCALAR_INCREMENT")
    public static class Increment extends CfgStructure<Increment> {
        
        /** Default constructor required for base class */
        public Increment() { super(Increment.class); };
        
        /** Seed value (i.e., start value) for incremental scalar value creation */
        @ACfgOverride.Field(name="SEED")
        public Long         seed;
        
        /** Increment value for boolean value generation */
        @ACfgOverride.Field(name="BOOLEANV")
        public Integer      booleanv;
        
        /** Increment value for integer value generation */
        @ACfgOverride.Field(name="INTEGERV")
        public Integer      integerv;
        
        /** Increment value for long value generation */
        @ACfgOverride.Field(name="LONGV")
        public Long         longv;
        
        /** Increment value for float value generation */
        @ACfgOverride.Field(name="FLOATV")
        public Float        floatv;
        
        /** Increment value for double value generation */
        @ACfgOverride.Field(name="DOUBLEV")
        public Double       doublev;
        
        /** Increment value for string value generation - used as string suffix */
        @ACfgOverride.Field(name="STRING")
        public Integer      stringv;
    }
}
