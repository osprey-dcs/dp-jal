/*
 * Project: dp-jal
 * File:	StructureFactoryLib.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.lib
 * Type: 	StructureFactoryLib
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
 * @since Nov 25, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.lib;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactorySpec;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory;

/**
 * <p>
 * An enumeration of pre-defined structure factories available for testing and evaluation.
 * </p>
 * <p>
 * The collection of pre-defined structure factories mirrors the enumeration <code>{@link ScalarFactoryLib}</code>.
 * The underlying scalar factory used for the structure factory is given by the configuration in the scalar
 * factory enumeration.
 * </p>
 * <p>
 * <h2>Structure Factories</h2>
 * The structure factories available here are of type <code>{@link StructureFactory}</code>.  The 
 * <code>StructureFactory</code> class instances produce symmetric tree structures where each structure node
 * has the same number of sub-nodes (the fan-out) down to a depth 'depth'.  For more information on structure
 * factories see the class documentation <code>{@link StructureFactory}</code>.
 * </p>
 * <p>
 * <h2>Factory Configuration</h2>
 * Each enumeration constant represents a particular configuration of structure factory, specifically identifying the 
 * type of structure field values the factory produces.  Other configurations are also required for 
 * <code>StructureFactory</code> creation, specifically, the structure 'depth' and the node 'fan-out'.
 * </p>
 * <p>
 * <h2>Factory Creation</h2>
 * Structure factories for an enumeration constant are created with methods <code>{@link #newFactory(int, int)}</code>
 * and <code>{@link #newFactory(int, int, boolean)}</code>.
 * Note that a new <code>{@link ScalarFactory}</code> is created and assigned to every new <code>StructureFactory</code>
 * created.  Thus, for incremental scalar generation the structure field values will be repeated in new instances.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 25, 2025
 *
 * @see ScalarFactory
 * @see StructureFactory
 * @see ScalarFactoryLib
 */
public enum StructureFactoryLib {

    
    /**
     * A structure factory producing elements using the default <code>ScalarFactory</code> configuration as defined 
     * in the JAL default configuration.
     * 
     * @see ScalarFactoryLib#DEFAULT
     */
    DEFAULT( ScalarFactoryLib.DEFAULT ),
    
    /**
     * A tensor factory producing string field values whose string suffix starts at 0 then increments by 1.
     * 
     * @see ScalarFactoryLib#STRING_INCR_1
     */
    STRING_INCR_1( ScalarFactoryLib.STRING_INCR_1 ),
    
    /**
     * A structure factory producing string field values whose string suffix starts at 0 then increments by 2.
     * 
     * @see ScalarFactoryLib#STRING_INCR_2
     */
    STRING_INCR_2( ScalarFactoryLib.STRING_INCR_2 ),

    /**
     * A tensor factory producing string field values whose string suffixes are randomly generated integers.
     * 
     * @see ScalarFactoryLib#STRING_INCR_RND
     */
    STRING_INCR_RND( ScalarFactoryLib.STRING_INCR_RND ),

    /**
     * A structure factory producing boolean field values that are all <code>false</code>.
     * 
     * @see ScalarFactoryLib#BOOLEAN_FALSE
     */
    BOOLEAN_FALSE( ScalarFactoryLib.BOOLEAN_FALSE ),
    
    /**
     * A structure factory producing boolean field values that are all <code>true</code>.
     * 
     * @see ScalarFactoryLib#BOOLEAN_TRUE
     */
    BOOLEAN_TRUE( ScalarFactoryLib.BOOLEAN_TRUE ),
    
    /**
     * A structure factory producing boolean field values that alternate between <code>false</code> and <code>true</code>.
     * 
     * @see ScalarFactoryLib#BOOLEAN_ALT
     */
    BOOLEAN_ALT( ScalarFactoryLib.BOOLEAN_ALT ),

    /**
     * A structure factory producing boolean field values that are randomly generated.
     * 
     * @see ScalarFactoryLib#BOOLEAN_RND
     */
    BOOLEAN_RND( ScalarFactoryLib.BOOLEAN_RND ),

    /**
     * A structure factory producing integer field values whose initial value is 0 then increments by 1.
     *
     * @see ScalarFactoryLib#INTEGER_INCR_1
     */
    INTEGER_INCR_1( ScalarFactoryLib.INTEGER_INCR_1 ),
    
    /**
     * A structure factory producing integer field values whose initial value is 0 then increments by 2.
     *
     * @see ScalarFactoryLib#INTEGER_INCR_2
     */
    INTEGER_INCR_2( ScalarFactoryLib.INTEGER_INCR_2 ),
    
    /**
     * A structure factory producing integer field values whose initial value is 0 then increments by 3.
     *
     * @see ScalarFactoryLib#INTEGER_INCR_3
     */
    INTEGER_INCR_3( ScalarFactoryLib.INTEGER_INCR_3 ),
    
    /**
     * A structure factory producing integer field values that are randomly generated. 
     *
     * @see ScalarFactoryLib#INTEGER_RND
     */
    INTEGER_RND( ScalarFactoryLib.INTEGER_RND ),
    
    /**
     * A structure factory producing long field values whose initial value is 0 then increments by 1.
     *
     * @see ScalarFactoryLib#LONG_INCR_1
     */
    LONG_INCR_1( ScalarFactoryLib.LONG_INCR_1 ),
    
    /**
     * A structure factory producing long field values whose initial value is 0 then increments by 2.
     *
     * @see ScalarFactoryLib#LONG_INCR_2
     */
    LONG_INCR_2( ScalarFactoryLib.LONG_INCR_2 ),
    
    /**
     * A structure factory producing long field values whose initial value is 0 then increments by 3.
     *
     * @see ScalarFactoryLib#LONG_INCR_3
     */
    LONG_INCR_3( ScalarFactoryLib.LONG_INCR_3 ),
    
    /**
     * A structure factory producing long field values that are randomly generated. 
     *
     * @see ScalarFactoryLib#LONG_RND
     */
    LONG_RND( ScalarFactoryLib.LONG_RND ),
    
    /**
     * A structure factor producing float field values whose initial value is 0 then increments by 0.1.
     * 
     * @see ScalarFactoryLib#FLOAT_INCR_P1
     */
    FLOAT_INCR_P1( ScalarFactoryLib.FLOAT_INCR_P1 ),
    
    /**
     * A structure factor producing float field values whose initial value is 0 then increments by 0.2.
     * 
     * @see ScalarFactoryLib#FLOAT_INCR_P2
     */
    FLOAT_INCR_P2( ScalarFactoryLib.FLOAT_INCR_P2 ),
    
    /**
     * A structure factor producing float field values whose initial value is 0 then increments by 0.01.
     * 
     * @see ScalarFactoryLib#FLOAT_INCR_P01
     */
    FLOAT_INCR_P01( ScalarFactoryLib.FLOAT_INCR_P01 ),
    
    /**
     * A structure factory producing float field values that are randomly generated in [0, 1].
     * 
     * @see ScalarFactoryLib#FLOAT_RND
     */
    FLOAT_RND( ScalarFactoryLib.FLOAT_RND ),
    
    /**
     * A structure factor producing double field values whose initial value is 0 then increments by 0.1.
     * 
     * @see ScalarFactoryLib#DOUBLE_INCR_P1
     */
    DOUBLE_INCR_P1( ScalarFactoryLib.DOUBLE_INCR_P1 ),
    
    /**
     * A structure factor producing double field values whose initial value is 0 then increments by 0.01.
     * 
     * @see ScalarFactoryLib#DOUBLE_INCR_P01
     */
    DOUBLE_INCR_P01( ScalarFactoryLib.DOUBLE_INCR_P01 ),
    
    /**
     * A structure factor producing double field values whose initial value is 0 then increments by 0.001.
     * 
     * @see ScalarFactoryLib#DOUBLE_INCR_P001
     */
    DOUBLE_INCR_P001( ScalarFactoryLib.DOUBLE_INCR_P001 ),
    
    /**
     * A structure factor producing double field values whose initial value is 0 then increments by 1.0e-16.
     * 
     * @see ScalarFactoryLib#DOUBLE_INCR_EM16
     */
    DOUBLE_INCR_EM16( ScalarFactoryLib.DOUBLE_INCR_EM16 ),
    
    /**
     * A structure factory that produces double field values that are randomly generated in [0, 1].
     * 
     * @see ScalarFactoryLib#DOUBLE_RND
     */
    DOUBLE_RND( ScalarFactoryLib.DOUBLE_RND ),
    
    ;
    
    
    //
    // Constant Attributes
    //
    
    /** The scalar factory constant representing the scalar factor used to create structure field values */
    private final ScalarFactoryLib     enmFacFldVals;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>StructureFactoryLib</code> constant with the given scalar factory representation.
     * </p>
     *
     * @param enmFacFldVals scalar factory constant representing the scalar factory used to create structure field values
     */
    private StructureFactoryLib(ScalarFactoryLib enmFacFldVals) {
        this.enmFacFldVals = enmFacFldVals;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Returns the <code>ScalarFactoryLib</code> constant associated with this structure factory.
     * </p>
     * <p>
     * The returned enumeration constant is used to create <code>{@link ScalarFactory}</code> instances
     * required for creation/construction of <code>{@link StructureFactory}</code> objects.
     * </p>
     * <p>
     * Note that a new code>ScalarFactory</code> is always created for <code>StructureFactory</code> objects
     * created from the <code>{@link #newFactory(int, int)}</code> method.  The returned enumeration constant
     * is used to create all <code>ScalarFactory</code> instances.  
     * </p>
     *   
     * @return  the associated <code>ScalarFactoryLib</code> constant used to create <code>ScalarFactory</code> instances
     */
    public ScalarFactoryLib    getScalarFactoryEnum() {
        return this.enmFacFldVals;
    }
    
    /**
     * <p>
     * Returns the configuration record for <code>ScalarFactory</code> instances used in <code>StructureFactory</code> creation.
     * </p>
     * <p>
     * This is a convenience method which is the equivalent of 
     * <code>{@link #getScalarFactoryEnum()}.{@link ScalarFactoryLib#getConfiguration()}</code>.
     * </p>
     * 
     * @return  configuration of the <code>ScalarFactory</code> used for all <code>StructureFactory</code> created by this constant
     */
    public ScalarFactorySpec  getScalarFactoryConfig() {
        return this.getScalarFactoryEnum().getConfiguration();
    }
    
    /**
     * <p>
     * Returns the scalar type of the structure field values for all structures generated by the factory.
     * </p>
     * <p>
     * This is a convenience method which is the equivalent of
     * <code>{@link #getScalarFactoryEnum()}.{@link ScalarFactoryLib#getJalType()}</code>.
     * </p>
     * 
     * @return  the data type of all structure field values produced by all associated factories as a <code>JalScalarType</code>
     */
    public JalScalarType    getJalScalarType() {
        return this.getScalarFactoryEnum().getJalType();
    }
    
    /**
     * <p>
     * Returns the scalar type of the structure field values for all structures generated by the factory.
     * </p>
     * <p>
     * This is a convenience method which is the equivalent of
     * <code>{@link #getScalarFactoryEnum()}.{@link ScalarFactoryLib#getDpType()}</code>.
     * </p>
     * 
     * @return  the data type of all tensor elements produced by all associated factories as a <code>DpSupportedType</code>
     */
    public DpSupportedType  getDpScalarType() {
        return this.getScalarFactoryEnum().getDpType();
    }
    
    /**
     * <p>
     * Creates and returns a new <code>StructureFactory</code> instance configured according to this constant and the arguments.
     * </p>
     * <p>
     * <p>
     * The returned <code>{@link StructureFactory}</code> produces tree structures with the given depth and node fan-out
     * while the field value types and value generation strategy is determined by the this enumeration constant.  For specific 
     * details on the <code>ScalarFactory</code> used to generate tensor elements see 
     * <code>{@link #getScalarFactoryConfig()}</code>.
     * </p>
     * 
     * @param depth         node depth of tree structures produced
     * @param fanout        number of sub-nodes for each tree structure node (above terminal nodes) 
     * 
     * @return  a new <code>StructureFactory</code> instance ready for simulated-value tree structure creation
     * 
     * @throws IllegalArgumentException depth and fan-out must both be greater than 0
     */
    public StructureFactory newFactory(int depth, int fanout) throws IllegalArgumentException {
        ScalarFactory       facFldVals = this.enmFacFldVals.newFactory();
        StructureFactory    facStruct = StructureFactory.from(depth, fanout, facFldVals); // throws IllegalArgumentException
        
        return facStruct;
    }
    
    /**
     * <p>
     * Creates and returns a new <code>StructureFactory</code> instance configured according to this constant and the arguments.
     * </p>
     * <p>
     * <p>
     * The returned <code>{@link StructureFactory}</code> produces tree structures with the given depth and node fan-out
     * while the field value types and value generation strategy is determined by the this enumeration constant.  For specific 
     * details on the <code>ScalarFactory</code> used to generate tensor elements see 
     * <code>{@link #getScalarFactoryConfig()}</code>.
     * </p>
     * <p>
     * This method provides the option of creating structure factories that generate unique structure field names for each
     * structure created by the factory. Note that field names are not necessary unique across multiple 
     * <code>StructureFactory</code> instances, however.
     * </p>
     * 
     * @param depth         node depth of tree structures produced
     * @param fanout        number of sub-nodes for each tree structure node (above terminal nodes) 
     * @param bolUniqFldNms enable/disable unique field names for each produced tree structure
     * 
     * @return  a new <code>StructureFactory</code> instance ready for simulated-value tree structure creation
     * 
     * @throws IllegalArgumentException depth and fan-out must both be greater than 0
     */
    public StructureFactory newFactory(int depth, int fanout, boolean bolUniqFldNms) throws IllegalArgumentException {
        ScalarFactory       facFldVals = this.enmFacFldVals.newFactory();
        StructureFactory    facStruct = StructureFactory.from(depth, fanout, bolUniqFldNms, facFldVals); // throws IllegalArgumentException
        
        return facStruct;
    }
}
