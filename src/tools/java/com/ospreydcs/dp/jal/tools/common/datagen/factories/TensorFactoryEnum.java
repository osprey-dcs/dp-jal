/*
 * Project: dp-jal
 * File:	TensorFactoryEnum.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories
 * Type: 	TensorFactoryEnum
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
 * @since Nov 22, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactoryConfig;
import com.ospreydcs.dp.jal.tools.common.datagen.values.TensorFactory;

/**
 * <p>
 * An enumeration of pre-defined tensor factories available for testing and evaluations.
 * </p>
 * <p>
 * The collection of pre-defined tensor factories mirrors the enumeration <code>{@link ScalarFactoryEnum}</code>.
 * The underlying scalar factory used for the tensor factory is given by the configuration in the scalar
 * factory enumeration.
 * </p>
 * <p>
 * <h2>Tensor Factories</h2>
 * The tensor factories available here are of type <code>{@link TensorFactory}</code>.  The 
 * <code>TensorFactory</code> class instances produce tensors of arbitrary shape whose values are packed in the last axis. 
 * For more information on tensor factories see the class documentation <code>{@link TensorFactory}</code>.
 * </p>
 * <p>
 * <h2>Factory Configuration</h2>
 * Each enumeration constant represents a particular configuration of tensor factory, specifically identifying the 
 * type of tensor element values the factory produces.  Other configurations are also required for 
 * <code>TensorFactory</code> creation, specifically, the tensor 'shape'.
 * </p>
 * <p>
 * <h2>Factory Creation</h2>
 * Tensor factories for an enumeration constant are created with method <code>{@link #newFactory(int[])}</code>.
 * Note that a new <code>{@link ScalarFactory}</code> is created and assigned to every new <code>TensorFactory</code>
 * created.  Thus, for incremental scalar generation the tensor element values will be repeated in new instances.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 22, 2025
 *
 * @see TensorFactory
 * @see ScalarFactory
 * @see ScalarFactoryEnum
 */
public enum TensorFactoryEnum {
    
    
    /**
     * A tensor factory producing elements using the default <code>ScalarFactory</code> configuration as defined 
     * in the JAL default configuration.
     * 
     * @see ScalarFactoryEnum#DEFAULT
     */
    DEFAULT( ScalarFactoryEnum.DEFAULT ),
    
    /**
     * A tensor factory producing string-valued elements whose string suffix starts at 0 then increments by 1.
     * 
     * @see ScalarFactoryEnum#STRING_INCR_1
     */
    STRING_INCR_1( ScalarFactoryEnum.STRING_INCR_1 ),
    
    /**
     * A tensor factory producing string-valued elements whose string suffix starts at 0 then increments by 2.
     * 
     * @see ScalarFactoryEnum#STRING_INCR_2
     */
    STRING_INCR_2( ScalarFactoryEnum.STRING_INCR_2 ),

    /**
     * A tensor factory producing string-valued elements whose string suffixes are randomly generated integers.
     * 
     * @see ScalarFactoryEnum#STRING_INCR_RND
     */
    STRING_INCR_RND( ScalarFactoryEnum.STRING_INCR_RND ),

    /**
     * A tensor factory producing boolean-valued elements that are all <code>false</code>.
     * 
     * @see ScalarFactoryEnum#BOOLEAN_FALSE
     */
    BOOLEAN_FALSE( ScalarFactoryEnum.BOOLEAN_FALSE ),
    
    /**
     * A tensor factory producing boolean-valued elements factory that are all <code>true</code>.
     * 
     * @see ScalarFactoryEnum#BOOLEAN_TRUE
     */
    BOOLEAN_TRUE( ScalarFactoryEnum.BOOLEAN_TRUE ),
    
    /**
     * A tensor factory producing boolean-valued elements that alternate between <code>false</code> and <code>true</code>.
     * 
     * @see ScalarFactoryEnum#BOOLEAN_ALT
     */
    BOOLEAN_ALT( ScalarFactoryEnum.BOOLEAN_ALT ),

    /**
     * A tensor factory producing boolean-valued elements that are randomly generated.
     * 
     * @see ScalarFactoryEnum#BOOLEAN_RND
     */
    BOOLEAN_RND( ScalarFactoryEnum.BOOLEAN_RND ),

    /**
     * A tensor factory producing integer-valued elements whose initial value is 0 then increments by 1.
     *
     * @see ScalarFactoryEnum#INTEGER_INCR_1
     */
    INTEGER_INCR_1( ScalarFactoryEnum.INTEGER_INCR_1 ),
    
    /**
     * A tensor factory producing integer-valued elements whose initial value is 0 then increments by 2. 
     *
     * @see ScalarFactoryEnum#INTEGER_INCR_2
     */
    INTEGER_INCR_2( ScalarFactoryEnum.INTEGER_INCR_2 ),
    
    /**
     * A tensor factory producing integer-valued elements whose initial value is 0 then increments by 3.
     *
     * @see ScalarFactoryEnum#INTEGER_INCR_3
     */
    INTEGER_INCR_3( ScalarFactoryEnum.INTEGER_INCR_3 ),
    
    /**
     * A tensor factory producing integer-value elements that are randomly generated. 
     *
     * @see ScalarFactoryEnum#INTEGER_RND
     */
    INTEGER_RND( ScalarFactoryEnum.INTEGER_RND ),
    
    /**
     * A tensor factory producing long-valued elements whose initial value is 0 then increments by 1.
     *
     * @see ScalarFactoryEnum#LONG_INCR_1
     */
    LONG_INCR_1( ScalarFactoryEnum.LONG_INCR_1 ),
    
    /**
     * A tensor factory producing long-valued elements whose initial value is 0 then increments by 2.
     *
     * @see ScalarFactoryEnum#LONG_INCR_2
     */
    LONG_INCR_2( ScalarFactoryEnum.LONG_INCR_2 ),
    
    /**
     * A tensor factory producing long-valued elements whose initial value is 0 then increments by 3.
     *
     * @see ScalarFactoryEnum#LONG_INCR_3
     */
    LONG_INCR_3( ScalarFactoryEnum.LONG_INCR_3 ),
    
    /**
     * A tensor factory producing long-value elements that are randomly generated. 
     *
     * @see ScalarFactoryEnum#LONG_RND
     */
    LONG_RND( ScalarFactoryEnum.LONG_RND ),
    
    /**
     * A tensor factor producing float-valued elements whose initial value is 0 then increments by 0.1.
     * 
     * @see ScalarFactoryEnum#FLOAT_INCR_P1
     */
    FLOAT_INCR_P1( ScalarFactoryEnum.FLOAT_INCR_P1 ),
    
    /**
     * A tensor factor producing float-valued elements whose initial value is 0 then increments by 0.2.
     * 
     * @see ScalarFactoryEnum#FLOAT_INCR_P2
     */
    FLOAT_INCR_P2( ScalarFactoryEnum.FLOAT_INCR_P2 ),
    
    /**
     * A tensor factor producing float-valued elements whose initial value is 0 then increments by 0.01.
     * 
     * @see ScalarFactoryEnum#FLOAT_INCR_P01
     */
    FLOAT_INCR_P01( ScalarFactoryEnum.FLOAT_INCR_P01 ),
    
    /**
     * A tensor factory producing float-valued element that are randomly generated in [0, 1].
     * 
     * @see ScalarFactoryEnum#FLOAT_RND
     */
    FLOAT_RND( ScalarFactoryEnum.FLOAT_RND ),
    
    /**
     * A tensor factor producing double-valued elements whose initial value is 0 then increments by 0.1.
     * 
     * @see ScalarFactoryEnum#DOUBLE_INCR_P1
     */
    DOUBLE_INCR_P1( ScalarFactoryEnum.DOUBLE_INCR_P1 ),
    
    /**
     * A tensor factor producing double-valued elements whose initial value is 0 then increments by 0.01.
     * 
     * @see ScalarFactoryEnum#DOUBLE_INCR_P01
     */
    DOUBLE_INCR_P01( ScalarFactoryEnum.DOUBLE_INCR_P01 ),
    
    /**
     * A tensor factor producing double-valued elements whose initial value is 0 then increments by 0.001.
     * 
     * @see ScalarFactoryEnum#DOUBLE_INCR_P001
     */
    DOUBLE_INCR_P001( ScalarFactoryEnum.DOUBLE_INCR_P001 ),
    
    /**
     * A tensor factor producing double-valued elements whose initial value is 0 then increments by 1.0e-16.
     * 
     * @see ScalarFactoryEnum#DOUBLE_INCR_EM16
     */
    DOUBLE_INCR_EM16( ScalarFactoryEnum.DOUBLE_INCR_EM16 ),
    
    /**
     * A tensor factory that produces double-valued elements that are randomly generated in [0, 1].
     * 
     * @see ScalarFactoryEnum#DOUBLE_RND
     */
    DOUBLE_RND( ScalarFactoryEnum.DOUBLE_RND ),
    
    ;

    
    //
    // Constant Attributes
    //
    
    /** The underlying scalar factory enumeration constant used for element value creation */
    private final ScalarFactoryEnum     enmFacElemVals;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>TensorFactoryEnum</code> constant with the given scalar factory.
     * </p>
     *
     * @param enmFacElemVals    the <code>ScalarFactoryEnum</code> used to create tensor element values
     */
    private TensorFactoryEnum(ScalarFactoryEnum enmElemFac) {
        this.enmFacElemVals = enmElemFac;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Returns the <code>ScalarFactoryEnum</code> constant associated with this tensor factory.
     * </p>
     * <p>
     * The returned enumeration constant is used to create <code>{@link ScalarFactory}</code> instances
     * required for creation/construction of <code>{@link TensorFactory}</code> objects.
     * </p>
     * <p>
     * Note that a new code>ScalarFactory</code> is always created for <code>TensorFactory</code> objects
     * created from the <code>{@link #newFactory(int[])}</code> method.  The returned enumeration constant
     * is used to create all <code>ScalarFactory</code> instances.  
     * </p>
     *   
     * @return  the associated <code>ScalarFactoryEnum</code> constant used to create <code>ScalarFactory</code> instances
     */
    public ScalarFactoryEnum    getScalarFactoryEnum() {
        return this.enmFacElemVals;
    }
    
    /**
     * <p>
     * Returns the configuration record for <code>ScalarFactory</code> instances used in <code>TensorFactory</code> creation.
     * </p>
     * <p>
     * This is a convenience method which is the equivalent of 
     * <code>{@link #getScalarFactoryEnum()}.{@link ScalarFactoryEnum#getConfiguration()}</code>.
     * </p>
     * 
     * @return  configuration of the <code>ScalarFactory</code> used for all <code>TensorFactory</code> created by this constant
     */
    public ScalarFactoryConfig  getScalarFactoryConfig() {
        return this.getScalarFactoryEnum().getConfiguration();
    }
    
    /**
     * <p>
     * Returns the scalar type of the tensor elements for all tensors generated by the factory.
     * </p>
     * <p>
     * This is a convenience method which is the equivalent of
     * <code>{@link #getScalarFactoryEnum()}.{@link ScalarFactoryEnum#getJalType()}</code>.
     * </p>
     * 
     * @return  the data type of all tensor elements produced by all associated factories as a <code>JalScalarType</code>
     */
    public JalScalarType    getJalScalarType() {
        return this.getScalarFactoryEnum().getJalType();
    }
    
    /**
     * <p>
     * Returns the scalar type of the tensor elements for all tensors generated by the factory.
     * </p>
     * <p>
     * This is a convenience method which is the equivalent of
     * <code>{@link #getScalarFactoryEnum()}.{@link ScalarFactoryEnum#getDpType()}</code>.
     * </p>
     * 
     * @return  the data type of all tensor elements produced by all associated factories as a <code>DpSupportedType</code>
     */
    public DpSupportedType  getDpScalarType() {
        return this.getScalarFactoryEnum().getDpType();
    }
    
    /**
     * <p>
     * Creates a new <code>TensorFactory</code> instances with the given shape configured according to this constant.
     * </p>
     * <p>
     * The returned <code>{@link TensorFactory}</code> produces tensors with the given shape while the element types
     * and value generation strategy is determined by the this enumeration constant.  For specific details on
     * the <code>ScalarFactory</code> used to generate tensor elements see <code>{@link #getScalarFactoryConfig()}</code>.
     * </p>
     * 
     * @param shape the shape of all tensors created by the returned tensor factory
     * 
     * @return  new <code>TensorFactory</code> ready for simulated-valued tensor creation
     * 
     * @throws IllegalArgumentException tensor shape must be > 0
     */
    public TensorFactory newFactory(int[] shape) throws IllegalArgumentException {
        ScalarFactory   facVals = this.getScalarFactoryEnum().newFactory();
        TensorFactory   facTnsr = TensorFactory.from(shape, facVals);
        
        return facTnsr;
    }
}
