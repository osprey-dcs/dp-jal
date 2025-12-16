/*
 * Project: dp-jal
 * File:	JalToolsTensorFactoryConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config.datagen.values
 * Type: 	JalToolsTensorFactoryConfig
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
 * @since Dec 12, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.config.datagen.values;

import java.util.List;

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;

/**
 * <p>
 * Structure class containing default configuration parameters for <code>TensorFactory</code> instances.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 12, 2025
 *
 */
public class JalToolsTensorFactoryConfig extends CfgStructure<JalToolsTensorFactoryConfig> {

    /** Default constructor required of base class */
    public JalToolsTensorFactoryConfig() { super(JalToolsTensorFactoryConfig.class); };
    
    
    //
    // Attributes
    //

    /** The default shape for generated tensor objects */
    @ACfgOverride.Field(name="SHAPE")
    public List<Integer>        shape;
    
    /** The default parameters for tensor (scalar) element creation */
    @ACfgOverride.Struct(pathelem="ELEMENTS")
    public Elements             elements;
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Converts the <code>{@link #shape}</code> attribute to a new <code>int[]</code> array and returns it.
     * </p>
     *  
     * @return  a new <code>int[]</code> containing the equivalent elements of <code>{@link #shape}</code>
     */
    public int[]    shapeArray() {
        int[]   arrShape = shape.stream().mapToInt(i -> i).toArray();
        
        return arrShape;
    }
    
    
    //
    // Internal Types
    //
    
    /**
     * Structure class containing default parameters for tensor element (scalar) value generation.
     */
    public static class Elements extends CfgStructure<Elements> {
        
        /** Default constructor required of base class */
        public Elements()   { super(Elements.class); };
        
        
        //
        // Attributes
        //
        
        /** The tensor (scalar) element data type */
        @ACfgOverride.Field(name="TYPE")
        public JalScalarType            type;
        
        /** Random number generation parameters for tensor element creation */
        @ACfgOverride.Struct(pathelem="RANDOM")
        public JalToolsRandomConfig     random;
    }
}
