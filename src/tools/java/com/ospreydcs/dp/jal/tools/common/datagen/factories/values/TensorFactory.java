/*
 * Project: dp-jal
 * File:	TensorFactory.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.values
 * Type: 	TensorFactory
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
 * @since Nov 13, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.values;

import java.util.ArrayList;
import java.util.List;

import org.epics.pvdata.pv.ScalarType;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.values.JalToolsTensorFactoryConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Creates Data Platform compatible arrays of simulated data suitable for populating an <code>Array</code> Protocol Buffers message.
 * </p>
 * <p>
 * The Data Platform <code>Array</code> Protocol Buffers messages are linear vectors of heterogeneous data values.  
 * Thus, each element of the vector can contain other <code>Array</code> messages, and so on.  Thus, effectively, arrays
 * of arbitrary shape are supported by the Data Platform heterogeneous data mechanism.
 * The arrays generated here are intended to verify that mechanism. 
 * </p>
 * <p>
 * <h2>Array Format</h2>
 * Instances of this class essentially create N-dimensional tensors where the shape of the tensor is given by
 * an integer array upon construction.  The values of the tensor are scalar quantities of type 
 * <code>{@link ScalarType}</code> also set at construction.  These values are always contained in the last
 * axis of the tensor.
 * </p>  
 * <p>
 * The axes of each generated tensor are always represented as a Java <code>{@link List}</code> container, specifically,
 * an <code>{@link ArrayList}</code> as it serves as a vector container.
 * For example, for a NxM matrix tensor (where rank = 2), the returned values are column-packed
 * arrays where the 1st axis is an N-length <code>List<code> of M-length <code>List</code> containers, the latter
 * containing the scalar values of the tensor. 
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Nov 13, 2025
 *
 */
public class TensorFactory implements IDatumFactory {
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactory</code> instance configured with all default parameters.
     * </p>
     * <p>
     * This creator uses all default parameters.  All are taken from the JAL Tools default configuration.
     * Some are used for the internal <code>{@link ScalarFactory}</code> and some for the 
     * <code>{@link TensorFactory}</code> configuration.  
     * <ul>
     * <li><code>arrShape = {@link #ARR_SHAPE_DEF}</code>.</i>
     * <li><code>enmType = {@link #ENM_TYPE_DEF}</code>.</li>
     * <li><code>bolRandEnbl = {@link #BOL_RAND_ENBL_DEF}</code>.</li>
     * <li><code>{@link #BOL_RAND_ENBL_DEF} == true</code>
     *   <ul>
     *   <li><code>lngSeed = {@link #LNG_RAND_SEED_DEF}</code>.</li>
     *   <li><code>facValues = {@link ScalarFactory#from(JalScalarType, boolean, long)}</code>.</li>
     *   </ul>
     * </li>
     * <li><code>{@link #BOL_RAND_ENBL_DEF} == false</code>
     *   <ul>
     *   <li><code>facValues = {@link ScalarFactory#from(JalScalarType, boolean)}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     * <p>
     * Note that when the parameter 'bolRandEnbl' is set to <code>true</code> the 
     * scalar factory configuration parameter 'numIncr' is ignored.
     * When 'bolRandEnbl' is set to <code>false</code> the  but populated according to the above. 
     * </p> 
     * 
     * @return  a new <code>TensorFactory</code> instance ready for array value generation
     * 
     * @throws IllegalArgumentException tensor rank must be > 0, each axis size must be >= 1, scalar factory must not be <code>null</code>
     */
    public static TensorFactory  from() throws IllegalArgumentException {

        return TensorFactory.from(ARR_SHAPE_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactory</code> instance configured according to the given argument(s).
     * </p>
     * <p>
     * This creator uses some default parameters from the JAL Tools default configuration for the 
     * internal <code>{@link ScalarFactory}</code> and for some of the <code>{@link TensorFactory}</code>
     * configuration.  
     * <ul>
     * <li><code>enmType = {@link #ENM_TYPE_DEF}</code>.</li>
     * <li><code>bolRandEnbl = {@link #BOL_RAND_ENBL_DEF}</code>.</li>
     * <li><code>{@link #BOL_RAND_ENBL_DEF} == true</code>
     *   <ul>
     *   <li><code>lngSeed = {@link #LNG_RAND_SEED_DEF}</code>.</li>
     *   <li><code>facValues = {@link ScalarFactory#from(JalScalarType, boolean, long)}</code>.</li>
     *   </ul>
     * </li>
     * <li><code>{@link #BOL_RAND_ENBL_DEF} == false</code>
     *   <ul>
     *   <li><code>facValues = {@link ScalarFactory#from(JalScalarType, boolean)}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     * <p>
     * Note that when the parameter 'bolRandEnbl' is set to <code>true</code> the 
     * scalar factory configuration parameter 'numIncr' is ignored.
     * When 'bolRandEnbl' is set to <code>false</code> the  but populated according to the above. 
     * </p> 
     * 
     * @param arrShape      array containing size of each tensor axis
     * 
     * @return  a new <code>TensorFactory</code> instance ready for array value generation
     * 
     * @throws IllegalArgumentException tensor rank must be > 0, each axis size must be >= 1, scalar factory must not be <code>null</code>
     */
    public static TensorFactory  from(int[] arrShape) throws IllegalArgumentException {

        return TensorFactory.from(arrShape, ENM_TYPE_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactory</code> instance configured according to the given argument(s).
     * </p>
     * <p>
     * This creator uses some default parameters from the JAL Tools default configuration for the 
     * internal <code>{@link ScalarFactory}</code> and for some of the <code>{@link TensorFactory}</code>
     * configuration.  
     * <ul>
     * <li><code>bolRandEnbl = {@link #BOL_RAND_ENBL_DEF}</code>.</li>
     * <li><code>{@link #BOL_RAND_ENBL_DEF} == true</code>
     *   <ul>
     *   <li><code>lngSeed = {@link #LNG_RAND_SEED_DEF}</code>.</li>
     *   <li><code>facValues = {@link ScalarFactory#from(JalScalarType, boolean, long)}</code>.</li>
     *   </ul>
     * </li>
     * <li><code>{@link #BOL_RAND_ENBL_DEF} == false</code>
     *   <ul>
     *   <li><code>facValues = {@link ScalarFactory#from(JalScalarType, boolean)}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     * <p>
     * Note that when the parameter 'bolRandEnbl' is set to <code>true</code> the 
     * scalar factory configuration parameter 'numIncr' is ignored.
     * When 'bolRandEnbl' is set to <code>false</code> the  but populated according to the above. 
     * </p> 
     * 
     * @param arrShape      array containing size of each tensor axis
     * @param enmType       the data type of the generated tensor elements
     * 
     * @return  a new <code>TensorFactory</code> instance ready for array value generation
     * 
     * @throws IllegalArgumentException tensor rank must be > 0, each axis size must be >= 1, scalar factory must not be <code>null</code>
     */
    public static TensorFactory  from(int[] arrShape, JalScalarType enmType) throws IllegalArgumentException {

        return TensorFactory.from(arrShape, enmType, BOL_RAND_ENBL_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactory</code> instance configured according to the given argument(s).
     * </p>
     * <p>
     * This creator uses some default parameters from the JAL Tools default configuration for the 
     * internal <code>{@link ScalarFactory}</code> and for some of the <code>{@link TensorFactory}</code>
     * configuration.  
     * <ul>
     * <li><code>bolRandEnbl == true</code>
     *   <ul>
     *   <li><code>lngSeed = {@link #LNG_RAND_SEED_DEF}</code>.</li>
     *   <li><code>facValues = {@link ScalarFactory#from(JalScalarType, boolean, long)}</code>.</li>
     *   </ul>
     * </li>
     * <li><code>bolRandEnbl == false</code>
     *   <ul>
     *   <li><code>facValues = {@link ScalarFactory#from(JalScalarType, boolean)}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     * <p>
     * Note that when the parameter 'bolRandEnbl' is set to <code>true</code> the 
     * scalar factory configuration parameter 'numIncr' is ignored.
     * When 'bolRandEnbl' is set to <code>false</code> the  but populated according to the above. 
     * </p> 
     * 
     * @param arrShape      array containing size of each tensor axis
     * @param enmType       the data type of the generated tensor elements
     * @param bolRandEnbl   enable/disable the use of random sequence generation, <code>false</code> indicates an incremental factory
     * 
     * @return  a new <code>TensorFactory</code> instance ready for array value generation
     * 
     * @throws IllegalArgumentException tensor rank must be > 0, each axis size must be >= 1, scalar factory must not be <code>null</code>
     */
    public static TensorFactory  from(int[] arrShape, JalScalarType enmType, boolean bolRandEnbl) throws IllegalArgumentException {

        return TensorFactory.from(arrShape, enmType, bolRandEnbl, LNG_RAND_SEED_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactory</code> instance configured according to the given argument(s).
     * </p>
     * <p>
     * This creator uses some default parameters from the JAL Tools default configuration for the 
     * internal <code>{@link ScalarFactory}</code> and for some of the <code>{@link TensorFactory}</code>
     * configuration.  
     * <ul>
     * <li><code>bolRandEnbl == true</code>
     *   <ul>
     *   <li><code>lngSeed = {@link #LNG_RAND_SEED_DEF}</code>.</li>
     *   <li><code>facValues = {@link ScalarFactory#from(JalScalarType, boolean, long)}</code>.</li>
     *   </ul>
     * </li>
     * <li><code>bolRandEnbl == false</code>
     *   <ul>
     *   <li><code>facValues = {@link ScalarFactory#from(JalScalarType, boolean)}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     * <p>
     * Note that when the parameter 'bolRandEnbl' is set to <code>true</code> the 
     * scalar factory configuration parameter 'numIncr' is ignored.
     * When 'bolRandEnbl' is set to <code>false</code> the  but populated according to the above. 
     * </p> 
     * 
     * @param arrShape      array containing size of each tensor axis
     * @param enmType       the data type of the generated tensor elements
     * @param bolRandEnbl   enable/disable the use of random sequence generation, <code>false</code> indicates an incremental factory
     * @param lngSeed       seed value for random number generator (0 indicates 'random' seed) or 1st incremental value
     * 
     * @return  a new <code>TensorFactory</code> instance ready for array value generation
     * 
     * @throws IllegalArgumentException tensor rank must be > 0, each axis size must be >= 1, scalar factory must not be <code>null</code>
     */
    public static TensorFactory  from(int[] arrShape, JalScalarType enmType, boolean bolRandEnbl, long lngSeed) throws IllegalArgumentException {
        
        // Create scalar factory according to random flag
        ScalarFactory   facValues; 
        if (bolRandEnbl)
            facValues = ScalarFactory.from(enmType, bolRandEnbl, lngSeed);
        else 
            facValues = ScalarFactory.from(enmType, bolRandEnbl);
        
        return TensorFactory.from(arrShape, facValues);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactory</code> instance configured according to the given argument(s).
     * </p>
     * <p>
     * This creator uses some default parameters from the JAL Tools default configuration for the 
     * internal <code>{@link ScalarFactory}</code>.  
     * <ul>
     * <li><code>facValues = {@link ScalarFactory#from(JalScalarType, boolean, long, Number)}</code>.</li>
     * </ul>
     * </p>
     * <p>
     * Note that when the parameter 'bolRandEnbl' is set to <code>true</code> the 
     * parameter 'numIncr' is ignored but populated according to the above. 
     * </p> 
     * 
     * @param arrShape      array containing size of each tensor axis
     * @param enmType       the data type of the generated tensor elements
     * @param bolRandEnbl   enable/disable the use of random sequence generation, <code>false</code> indicates an incremental factory
     * @param lngSeed       seed value for random number generator (0 indicates 'random' seed) or 1st incremental value
     * @param numIncr       increment value for incremental factories, ignored for random factories
     * 
     * @return  a new <code>TensorFactory</code> instance ready for array value generation
     * 
     * @throws IllegalArgumentException tensor rank must be > 0, each axis size must be >= 1, scalar factory must not be <code>null</code>
     */
    public static TensorFactory  from(int[] arrShape, JalScalarType enmType, boolean bolRandEnbl, long lngSeed, Number numIncr) throws IllegalArgumentException {
        ScalarFactory   facValues = ScalarFactory.from(enmType, bolRandEnbl, lngSeed, numIncr);
        
        return TensorFactory.from(arrShape, facValues);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactory</code> instance configured according to the given arguments.
     * </p>
     * <p>
     * This creator contains all the required parameters for the <code>{@link ScalarFactory}</code>.  It is
     * essentially equivalent to creator <code>{@link #from(int[], ScalarFactory)}</code> where the scalar
     * factory is created here as a convenience.
     * <ul>
     * <li><code>facValues = {@link ScalarFactory#from(JalScalarType, boolean, long, Number, String)}</code>.</li>
     * </ul>
     * </p>
     * <p>
     * Note that when the parameter 'bolRandEnbl' is set to <code>true</code> the 
     * parameter 'numIncr' is ignored but populated according to the above. 
     * </p> 
     * 
     * @param arrShape      array containing size of each tensor axis
     * @param enmType       the data type of the generated tensor elements
     * @param bolRandEnbl   enable/disable the use of random sequence generation, <code>false</code> indicates an incremental factory
     * @param lngSeed       seed value for random number generator (0 indicates 'random' seed) or 1st incremental value
     * @param numIncr       increment value for incremental factories, ignored for random factories
     * @param strPrefix     prefix given to all string values when <code>enmType = {@link JalScalarType#STRING}</code>
     * 
     * @return  a new <code>TensorFactory</code> instance ready for array value generation
     * 
     * @throws IllegalArgumentException tensor rank must be > 0, each axis size must be >= 1, scalar factory must not be <code>null</code>
     */
    public static TensorFactory  from(int[] arrShape, JalScalarType enmType, boolean bolRandEnbl, long lngSeed, Number numIncr, String strPrefx) throws IllegalArgumentException {
        ScalarFactory   facValues = ScalarFactory.from(enmType, bolRandEnbl, lngSeed, numIncr, strPrefx);
        
        return TensorFactory.from(arrShape, facValues);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactory</code> instance configured according to the given arguments.
     * </p>
     * <p>
     * This creator is equivalent to the default constructor.
     * </p>
     * 
     * @param arrShape  array containing size of each tensor axis
     * @param facValues scalar value factory used to generate array element values (i.e., last axis)
     * 
     * @return  a new <code>TensorFactory</code> instance ready for array value generation
     * 
     * @throws IllegalArgumentException tensor rank must be > 0, each axis size must be >= 1, scalar factory must not be <code>null</code>
     */
    public static TensorFactory  from(int[] arrShape, ScalarFactory facValues) throws IllegalArgumentException {
        return new TensorFactory(arrShape, facValues);
    }
    

    //
    // Library Resources
    //
    
    /** The default parameters for scalar-valued simulated data generation */
    private static final JalToolsTensorFactoryConfig     CFG_DEF = JalToolsConfig.getInstance().datagen.values.tensor;
    
    
    //
    // Class Constants - Default Arguments
    //
    
    /** The default scalar value type when none is given */
    public final static JalScalarType   ENM_TYPE_DEF = CFG_DEF.elements.type;
    
    /** The default string value prefix */
    public final static int[]           ARR_SHAPE_DEF = CFG_DEF.shapeArray();
    
    /** The default enable/disable random number generator */
    public static final boolean         BOL_RAND_ENBL_DEF = CFG_DEF.elements.random.enabled;

    /** The default random number generator seed value */
    public static final long            LNG_RAND_SEED_DEF = CFG_DEF.elements.random.seed;


    //
    // Class Constants
    //
    
    /** The value type of all simulated data returned by this value factory */
    public static final DpSupportedType     ENM_DATUM_TYPE = DpSupportedType.ARRAY;
    
    /** Complex value type of all simulated data produced by this data value factory */
    public static final JalComplexType       ENM_CMPLX_TYPE = JalComplexType.ARRAY;
    

    //
    // Resources
    //
    
    /** Generator of scalar field values */
    private final ScalarFactory facValues;
    
    
    //
    // Configuration
    //
    
    /** Rank of arrays produced */
    private final int       intRank;
    
    /** Shape of arrays produced */
    private final int[]     arrShape;
    
    /** Size of the arrays produces (i.e., number of elements) */
    private final int       szArray;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>TensorFactory</code> instance.
     * </p>
     *
     * @param arrShape  array containing size of each array axis
     * @param facValues scalar value factory used to generate array element values (i.e., last axis)
     * 
     * @throws IllegalArgumentException tensor rank must be > 0, each axis size must be >= 1, scalar factory must not be <code>null</code>
     */
    public TensorFactory(int[] arrShape, ScalarFactory facValues) {
        
        // Check arguments
        if (arrShape.length < 1)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Tensor rank must be > 0");
        if (facValues == null)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Scalar factory cannot be null.");
        
        this.arrShape = arrShape.clone();
        this.intRank = arrShape.length;
        this.szArray = this.computeSize(arrShape);
        
        this.facValues = facValues;
    }

    //
    // Configuration
    //
    
    /**
     * <p>
     * Determines whether or not terminal-level array values within are generated 
     * randomly (i.e., using a random number generator).
     * </p>
     * <p>
     * Note that property of the generated tensor values is determined at creation/construction.
     * </p> 
     * 
     * @return  <code>true</code> the terminal field values are generated randomly,
     *          <code>false</code> the terminal field values are generated incrementally
     */
    public boolean      isRandomValued() {
        return this.facValues.isRandom();
    }
    
//    /**
//     * <p>
//     * Returns the scalar type of the terminal-level array values.
//     * </p>
//     * 
//     * @return  scalar type of terminal-level structure field values. 
//     */
//    public JalScalarType   getType() {
//        return this.facValues.getScalarType();
//    }
    
    /**
     * <p>
     * Returns the seed value used for generating scalar values within the array.
     * </p>
     * <p>
     * Note that property of the generated tensor values is determined at creation/construction.
     * </p> 
     * 
     * @return  value generation seed value provided at construction
     */
    public long  getSeed() {
        return this.facValues.getSeed();
    }
    
    /**
     * <p>
     * Returns the rank of each generated tensor value.
     * </p>
     * <p>
     * Note that property of the generated tensor values is determined at creation/construction.
     * </p> 
     * 
     * @return rank of tensor, i.e., number of axes
     */
    public int      getTensorRank() {
        return this.intRank;
    }
    
    /**
     * <p>
     * Returns the shape of each generated tensor value.
     * </p>
     * <p>
     * Note that property of the generated tensor values is determined at creation/construction.
     * </p> 
     * 
     * @return  tensor shape as an <code>int</code> array, axis size for each array dimension
     */
    public int[]    getTensorShape() {
        return this.arrShape;
    }
    
    /**
     * <p>
     * Returns the total number of elements within each generated tensor value, that is, the tensor size.
     * </p>
     * <p>
     * Note that property of the generated tensor values is determined at creation/construction.
     * </p> 
     * 
     * @return  total element count for generated tensor values, i.e., the multiply reduction of the shape
     */
    public int      getTensorSize() {
        return this.szArray;
    }
    
    
    //
    // IDatumFactory Interface
    //

    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory#getDatumType()
     */
    @Override
    public DpSupportedType  getDatumType() {
        return ENM_DATUM_TYPE;
    }
    
    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory#getScalarType()
     */
    @Override
    public JalScalarType getScalarType() {
        return this.facValues.getScalarType();
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory#getComplexType()
     */
    @Override
    public JalComplexType getComplexType() {
        return ENM_CMPLX_TYPE;
    }
    
    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory#nextDatum()
     */
    @Override
    public Object nextDatum() {
        
        // Check for exception case - zero rank tensor, or scalar
        if (this.intRank == 0) {
            Object  objVal = this.facValues.nextDatum();
            
            return List.of(objVal);
        }
        
        List<Object>    lstAxisOne = this.createVector(0);
        
        return lstAxisOne;
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Computes the size of a multi-dimensional array (tensor) with the given shape.
     * </p>
     * <p>
     * The size is computed with a multiply reduction of all the elements within the given integer array.
     * </p>
     * 
     * @param arrShape  shape of the ND-Array
     * 
     * @return  size of the given multi-dimensional array (i.e., number of elements)
     */
    private int computeSize(int[] arrShape) {
        int     rank = arrShape.length;
        int     size = 1;
        
        for (int iAxis=0; iAxis<rank; iAxis++) {
            int     szDim = arrShape[iAxis];
            
            size *= szDim; 
        }
        
        return size;
    }
    /**
     * <p>
     * Creates a vector (sub-)array within the overall array.
     * </p>
     * <p>
     * This is a recursive function and should be called with argument 0 to generate the full
     * array described by the configuration.
     * </p>
     *  
     * @param cntDepth  current depth within recursion - use 0 for full array
     * 
     * @return  vector with overall array
     */
    private List<Object>    createVector(int cntDepth) {
        
        // Check if we are mid-level
        // - return a vector of (recursive) vectors
        if (cntDepth < (this.intRank-1) ) {
            int             szAxis = this.arrShape[cntDepth];
            List<Object>    vecAxis = new ArrayList<>(szAxis);
            
            for (int i=0; i<szAxis; i++) {
                List<Object>    vecVals = this.createVector(cntDepth + 1);
                
                vecAxis.add(i, vecVals);
            }
            
            return vecAxis;
        }
        
        // Otherwise we are at maximum depth
        // - return a vector of scalar values
        int             szAxis = this.arrShape[cntDepth];
        List<Object>    vecVals = new ArrayList<>(szAxis);
        
        for (int i=0; i<szAxis; i++) {
            Object      objVal = this.facValues.nextDatum();
            
            vecVals.add(i, objVal);
        }
        
        return vecVals;
    }
}
