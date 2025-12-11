/*
 * Project: dp-jal
 * File:	TensorFactory.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.values
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
package com.ospreydcs.dp.jal.tools.common.datagen.values;

import java.util.ArrayList;
import java.util.List;

import org.epics.pvdata.pv.ScalarType;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
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
     * Creates and returns a new <code>TensorFactory</code> instance configured according to the given arguments.
     * </p>
     * 
     * @param shape      array containing size of each array axis
     * @param recFacSpec configuration record for the scalar value generator used internally for value generation 
     * 
     * @return  a new <code>TensorFactory</code> instance ready for array value generation
     * 
     * @throws IllegalArgumentException tensor shape equals 0 or scalar factory is <code>null</code>
     */
    public static TensorFactory from(int[] shape, ScalarFactorySpec recFacSpec) throws IllegalArgumentException {
        ScalarFactory   facValues = recFacSpec.newFactory();
        
        return TensorFactory.from(shape, facValues);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactory</code> instance configured according to the given arguments.
     * </p>
     * 
     * @param shape     array containing size of each array axis
     * @param facValues scalar value factory used to generate array element values (i.e., last axis)
     * 
     * @return  a new <code>TensorFactory</code> instance ready for array value generation
     * 
     * @throws IllegalArgumentException tensor shape equals 0 or scalar factory is <code>null</code>
     */
    public static TensorFactory  from(int[] shape, ScalarFactory facValues) throws IllegalArgumentException {
        return new TensorFactory(shape, facValues);
    }
    
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
     * @param shape     array containing size of each array axis
     * @param facValues scalar value factory used to generate array element values (i.e., last axis)
     * 
     * @throws IllegalArgumentException tensor shape equals 0 or scalar factory is <code>null</code>
     */
    public TensorFactory(int[] shape, ScalarFactory facValues) {
        
        // Check arguments
        if (shape.length < 1)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Tensor rank must be > 0");
        if (facValues == null)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Scalar factory cannot be null.");
        
        this.arrShape = shape.clone();
        this.intRank = shape.length;
        this.szArray = this.computeSize(shape);
        
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
    public boolean      isRandom() {
        return this.facValues.isRandom();
    }
    
    /**
     * <p>
     * Returns the scalar type of the terminal-level array values.
     * </p>
     * 
     * @return  scalar type of terminal-level structure field values. 
     */
    public JalScalarType   getType() {
        return this.facValues.getScalarType();
    }
    
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
    public int      getRank() {
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
    public int[]    getShape() {
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
