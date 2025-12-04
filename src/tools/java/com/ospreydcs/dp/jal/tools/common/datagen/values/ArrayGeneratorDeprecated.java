/*
 * Project: dp-data-simulator
 * File:	ArrayGeneratorDeprecated.java
 * Package: com.ospreydcs.dp.datasim.frame.model
 * Type: 	ArrayGeneratorDeprecated
 *
 * Copyright 2010-2023 the original author or authors.
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
 * @since May 13, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.jal.tools.common.datagen.values;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.epics.pvdata.pv.ScalarType;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;


/**
 * <p>
 * Creates an Data Platform compatible array suitable for populating an <code>Array</code> Protobuf message.
 * </p>
 * <p>
 * The Data Platform <code>Array</code> messages are linear vectors of heterogeneous data values.  Thus, each
 * element of the vector can contain other <code>Array</code> messages, and so on.  Thus, effectively, arrays
 * of arbitrary shape are supported by the Data Platform heterogeneous data mechanism.
 * The arrays generated here are intended to verify that mechanism. 
 * </p>
 * <p>
 * Instances of this class essentially create N-dimensional tensors where the shape of the tensor is given by
 * an integer array upon construction.  The values of the tensor are scalar quantities of type 
 * <code>{@link ScalarType}</code> also set at construction.  These values are always contained in the last
 * axis of the tensor.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since May 13, 2024
 * 
 * @deprecated  Replaced by TensorFactory
 */
@Deprecated(since="Nov 13, 2025", forRemoval=true)
public class ArrayGeneratorDeprecated implements IDatumFactory {

    
    //
    // Resources
    //
    
    /** Generator of scalar field values */
    private final ScalarGeneratorDeprecated  valGenerator;
    
    
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
     * Constructs a new instance of <code>ArrayGeneratorDeprecated</code>.
     * </p>
     *
     * @param shape     array containing size of each array axis
     * @param type      the scalar type of each array element
     */
    public ArrayGeneratorDeprecated(int[] shape, JalScalarType type) {
        this(shape, type, 0);
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>ArrayGeneratorDeprecated</code>.
     * </p>
     *
     * @param shape     array containing size of each array axis
     * @param type      the scalar type of each array element
     * @param seed      seed used to generate scalar types
     */
    public ArrayGeneratorDeprecated(int[] shape, JalScalarType type, long seed) {
        this(shape, type, seed, false);
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>ArrayGeneratorDeprecated</code>.
     * </p>
     *
     * @param shape     array containing size of each array axis
     * @param type      the scalar type of each array element
     * @param seed      seed used to generate scalar types
     * @param useRandom use random number generator for scalar values (otherwise incremental values)
     */
    public ArrayGeneratorDeprecated(int[] shape, JalScalarType type, long seed, boolean useRandom) {
        this.arrShape = shape.clone();
        this.intRank = shape.length;
        this.szArray = this.computeSize(shape);
        
        this.valGenerator = new ScalarGeneratorDeprecated(type, seed, useRandom);
    }
    
    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Toggles the use of a random value generator for terminal-level array value creation.
     * </p>
     * <h2>NOTES:</h2>
     * <p>
     * <ul>
     * <li>
     * Using random generation can creating a significant resource demand for large number of values.
     * Internally random values are generated using a <code>{@link Random}</code> Java object.
     * </li>
     * <li>
     * Scalar values are generated incrementally by default.  Random number generation is turned on
     * using this function.
     * </li>
     * </ul>
     * </p>  
     * 
     * @param useRandomValues   <code>true</code> generate scalar values randomly,
     *                          <code>false</code> generate scalar values incrementally
     */
    public void setRandom(boolean useRandomValues) {
        this.valGenerator.setRandom(useRandomValues);
    }
    
    /**
     * <p>
     * Determines whether or not terminal-level array values within are generated 
     * randomly (i.e., using a random number generator).
     * </p>
     * 
     * @return  <code>true</code> the terminal field values are generated randomly,
     *          <code>false</code> the terminal field values are generated incrementally
     */
    public boolean      isRandom() {
        return this.valGenerator.isRandom();
    }
    
    /**
     * <p>
     * Returns the scalar type of the terminal-level array values.
     * </p>
     * 
     * @return  scalar type of terminal-level structure field values. 
     */
    public JalScalarType   getType() {
        return this.valGenerator.getType();
    }
    
    /**
     * <p>
     * Returns the seed value used for generating scalar values within the array.
     * </p>
     * 
     * @return  value generation seed value provided at construction
     */
    public long  getSeed() {
        return this.valGenerator.getSeed();
    }
    
    /**
     * <p>
     * Returns the rank of the multi-dimensional array identified at construction.
     * </p>
     * 
     * @return rank of tensor, i.e., number of axes
     */
    public int      getRank() {
        return this.intRank;
    }
    
    /**
     * <p>
     * Returns the shape of the target multi-dimensional array identified at construction.
     * </p>
     * 
     * @return  tensor shape as an <code>int</code> array, axis size for each array dimension
     */
    public int[]    getShape() {
        return this.arrShape;
    }
    
    /**
     * <p>
     * Returns the total number of elements within the target multi-dimensional array, that is, its size.
     * </p>
     * 
     * @return  total element count for tensor, i.e., the multiply reduction of the shape
     */
    public int      getSize() {
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
        return DpSupportedType.ARRAY;
    }
    

    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory#getScalarType()
     */
    @Override
    public JalScalarType getScalarType() {
        return this.valGenerator.getScalarType();
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory#getComplexType()
     */
    @Override
    public JalComplexType getComplexType() {
        return JalComplexType.ARRAY;
    }
    /**
     *
     * @see com.ospreydcs.dp.datasim.frame.model.IDatumFactory#nextDatum()
     */
    @Override
    public Object nextDatum() {
        
        // Check for exception case - zero rank tensor, or scalar
        if (this.intRank == 0) {
            Object  objVal = this.valGenerator.nextDatum();
            
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
            List<Object>    vecTensor = new ArrayList<>(szAxis);
            
            for (int i=0; i<szAxis; i++) {
                List<Object>    vecVals = this.createVector(cntDepth + 1);
                
                vecTensor.add(i, vecVals);
            }
            
            return vecTensor;
        }
        
        // Otherwise we are at maximum depth
        // - return a vector of scalar values
        int             szAxis = this.arrShape[cntDepth];
        List<Object>    vecVals = new ArrayList<>(szAxis);
        
        for (int i=0; i<szAxis; i++) {
            Object      objVal = this.valGenerator.nextDatum();
            
            vecVals.add(i, objVal);
        }
        
        return vecVals;
    }
}
