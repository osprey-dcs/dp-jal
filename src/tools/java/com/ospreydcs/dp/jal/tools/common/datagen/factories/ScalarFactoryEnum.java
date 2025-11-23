/*
 * Project: dp-jal
 * File:	ScalarFactoryEnum.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.utility
 * Type: 	ScalarFactoryEnum
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
 * @since Nov 20, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactoryConfig;

/**
 * <p>
 * An enumeration of pre-defined scalar factories available for testing and evaluations.
 * </p>
 * <p>
 * This is a convenience enumeration that provides constants for common <code>ScalarFactoryConfig</code> configuration
 * records.  The constants can create new <code>ScalarFactory</code> instances for the given configuration
 * which are then available to applications for simulated data creation.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 20, 2025
 *
 */
public enum ScalarFactoryEnum {
    
    /**
     * The default <code>ScalarFactory</code> configuration as defined in the JAL default configuration.
     */
    DEFAULT( ScalarFactoryConfig.from() ),

    /**
     * A string factory whose string suffix starts at 0 then increments by 1.
     */
    STRING_INCR_1( ScalarFactoryConfig.from( JalScalarType.STRING, false, 0, Integer.valueOf(1)) ),
    
    /**
     * A string factory whose string suffix starts at 0 then increments by 2.
     */
    STRING_INCR_2( ScalarFactoryConfig.from( JalScalarType.STRING, false, 0, Integer.valueOf(2)) ),

    /**
     * A string factory whose string suffixes are randomly generated integers.
     */
    STRING_INCR_RND( ScalarFactoryConfig.from( JalScalarType.STRING, true, 0) ),

    /**
     * A boolean value factory which always produces <code>false</code>.
     */
    BOOLEAN_FALSE( ScalarFactoryConfig.from(JalScalarType.BOOLEAN, false, 0, Integer.valueOf(0)) ),
    
    /**
     * A boolean value factory which always produces <code>true</code>.
     */
    BOOLEAN_TRUE( ScalarFactoryConfig.from(JalScalarType.BOOLEAN, false, 1, Integer.valueOf(0)) ),
    
    /**
     * A boolean value factory which starts at <code>false</code> then alternates.
     */
    BOOLEAN_ALT( ScalarFactoryConfig.from(JalScalarType.BOOLEAN, false, 0, Integer.valueOf(1)) ),

    /**
     * A boolean value factory which produces random boolean values.
     */
    BOOLEAN_RND( ScalarFactoryConfig.from(JalScalarType.BOOLEAN, true, 0) ),

    /**
     * An integer value factory whose initial value is 0 then increments by 1.
     */
    INTEGER_INCR_1( ScalarFactoryConfig.from(JalScalarType.INTEGER, false, 0, Integer.valueOf(1)) ),
    
    /**
     * An integer value factory whose initial value is 0 then increments by 2.
     */
    INTEGER_INCR_2( ScalarFactoryConfig.from(JalScalarType.INTEGER, false, 0, Integer.valueOf(2)) ),
    
    /**
     * An integer value factory whose initial value is 0 then increments by 3.
     */
    INTEGER_INCR_3( ScalarFactoryConfig.from(JalScalarType.INTEGER, false, 0, Integer.valueOf(3)) ),
    
    /**
     * An integer value factory that produces random integer values.
     */
    INTEGER_RND( ScalarFactoryConfig.from(JalScalarType.INTEGER, true, 0) ),
    
    /**
     * A long value factory whose initial value is 0 then increments by 1.
     */
    LONG_INCR_1( ScalarFactoryConfig.from(JalScalarType.LONG, false, 0, Long.valueOf(1)) ),
    
    /**
     * A long value factory whose initial value is 0 then increments by 2.
     */
    LONG_INCR_2( ScalarFactoryConfig.from(JalScalarType.LONG, false, 0, Long.valueOf(2)) ),
    
    /**
     * A long value factory whose initial value is 0 then increments by 3.
     */
    LONG_INCR_3( ScalarFactoryConfig.from(JalScalarType.LONG, false, 0, Long.valueOf(3)) ),
    
    /**
     * A long value factory that produces random long values.
     */
    LONG_RND( ScalarFactoryConfig.from(JalScalarType.LONG, true, 0) ),
    
    /**
     * A float value factory whose initial value is 0 then increments by 0.1.
     */
    FLOAT_INCR_P1( ScalarFactoryConfig.from(JalScalarType.FLOAT, false, 0, Float.valueOf(0.1f)) ),
    
    /**
     * A float value factory whose initial value is 0 then increments by 0.2.
     */
    FLOAT_INCR_P2( ScalarFactoryConfig.from(JalScalarType.FLOAT, false, 0, Float.valueOf(0.2f)) ),
    
    /**
     * A float value factory whose initial value is 0 then increments by 0.01.
     */
    FLOAT_INCR_P01( ScalarFactoryConfig.from(JalScalarType.FLOAT, false, 0, Float.valueOf(0.01f)) ),
    
    /**
     * A float value factory that produces random floating point values in [0, 1].
     */
    FLOAT_RND( ScalarFactoryConfig.from(JalScalarType.FLOAT, true, 0) ),
    
    /**
     * A double value factory whose initial value is 0 then increments by 0.1.
     */
    DOUBLE_INCR_P1( ScalarFactoryConfig.from(JalScalarType.DOUBLE, false, 0, Double.valueOf(0.1)) ),
    
    /**
     * A double value factory whose initial value is 0 then increments by 0.01. 
     */
    DOUBLE_INCR_P01( ScalarFactoryConfig.from(JalScalarType.DOUBLE, false, 0, Double.valueOf(0.01)) ),
    
    /**
     * A double value factory whose initial value is 0 then increments by 0.001.
     */
    DOUBLE_INCR_P001( ScalarFactoryConfig.from(JalScalarType.DOUBLE, false, 0, Double.valueOf(0.001)) ),
    
    /**
     * A double value factory whose initial value is 0 then increments by 1.0e-16.
     */
    DOUBLE_INCR_EM16( ScalarFactoryConfig.from(JalScalarType.DOUBLE, false, 0, Double.valueOf(1.0e-16)) ),
    
    /**
     * A double value factory that produces random double values in [0, 1].
     */
    DOUBLE_RND( ScalarFactoryConfig.from(JalScalarType.DOUBLE, true, 0) ),
    
    ;
    
    //
    // Enumeration Resources
    //
    
    
    //
    // Constant Attributes
    //
    
    /** The scalar factory configuration record associated with this constant */
    private final ScalarFactoryConfig   recCfg;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>ScalarFactoryEnum</code> instance for the given <code>ScalarFactoryConfig</code> record.
     * </p>
     *
     * @param recCfg    the <code>{@link ScalarFactoryConfig}</code> record associated with this constant
     */
    private ScalarFactoryEnum(ScalarFactoryConfig recCfg) {
        this.recCfg = recCfg;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Returns the <code>ScalarFactoryConfig</code> configuration record associated with this constant
     * </p>
     * 
     * @return  the <code>{@link ScalarFactoryConfig}</code> record provided at constant construction
     * 
     * @see ScalarFactoryConfig
     */
    public ScalarFactoryConfig  getConfiguration() {
        return this.recCfg;
    }
    
    /**
     * <p>
     * Returns the JAL Tools <code>JalScalarType</code> enumeration constant for simulated data values.
     * </p>
     * <p>
     * Returns the <code>{@link JalScalarType}</code> enumeration constant identifying the data types
     * for the simulated data generated by the scalar factory returned by <code>{@link #newFactory()}</code>.
     * Note that <code>{@link JalScalarType}</code> constants only identify scalar-valued types.
     * </p>
     * 
     * @return  the data type for the scalar factory configuration associated with this constant
     */
    public JalScalarType    getJalType() {
        return this.getConfiguration().enmValueType();
    }
    
    /**
     * <p>
     * Returns the Data Platform supported type <code>DpSupportedType</code> enumeration constant for simulated data values.
     * </p>
     * <p>
     * Returns the <code>{@link DpSupportedType}</code> enumeration constant identifying the data types
     * for the simulated data generated by the scalar factory returned by <code>{@link #newFactory()}</code>.
     * Note that <code>{@link DpSupportedType}</code> constants identify both scalar-valued and complex data types.
     * </p>
     * 
     * @return  the data type for the scalar factory configuration associated with this constant
     */
    public DpSupportedType  getDpType() {
        return this.getJalType().getDpType();
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactory</code> instance configured by this constant.
     * </p>
     * <p>
     * The returned <code>{@link ScalarFactory}</code> is configured by the configuration record associated
     * with this <code>ScalarFactoryEnum</code> enumeration constant.  The scalar factory is ready for
     * scalar value generation, that is, simulated data generation.  See the <code>{@link ScalarFactory}</code>
     * class documentation for further information on scalar factory use.
     * </p>  
     * 
     * @return  a new instance of <code>ScalarFactory</code> configured and ready to use
     * 
     * @see ScalarFactoryConfig
     * @see ScalarFactory
     */
    public ScalarFactory    newFactory() {
        return ScalarFactory.from(this.recCfg);
    }
}
