/*
 * Project: dp-data-simulator
 * File:	TensorIndexGenerator.java
 * Package: com.ospreydcs.dp.datasim.utility
 * Type: 	TensorIndexGenerator
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
 * @since May 15, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.jal.tools.common.datagen.utility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Utility class for creating indexes for Data Platform Array messages.
 * </p>
 * <p>
 * An "Array" here is actually a tensor of rank <i>r</i>.  The shape <i>S</i> of the tensor is given by 
 * the tuple
 * <pre>
 *   <i>S</i> = (N<sub>1</sub>, N<sub>2</sub>, ..., N<sub><i>r</i></sub>) 
 * </pre>
 * where the N<sub><i>n</i></sub> is the size (dimension) of tensor axis <i>n</i>.  Thus, the 
 * total number of indices within the tensor, or the "size" N of the tensor, is
 * N = N<sub>1</sub> &times; N<sub>2</sub> &times; ... &times; N<sub><i>r</i></sub>.
 * </p>
 * <h2>Tensor Indexes and Positions</h2>
 * <p>
 * A tensor with the above shape <i>S</i> has indexes given by tuples <i>i</i> of the form
 * <pre>
 *   <i>i</i> = (<i>i</i><sub>1</sub>, <i>i</i><sub>1</sub>, ..., <i>i</i><sub><i>r</i></sub>)
 * </pre> 
 * where <i>i</i><sub><i>n</i></sub> &in; {0, 1, ..., N<sub><i>n</i></sub> - 1} is the index for
 * tensor axis <i>n</i>. 
 * Here we refer to <i>i</i><sub>1</sub> as the <em>most significant axis</em> and to 
 * <i>i</i><sub><i>r</i></sub> as the <em>least significant axis</em>.
 * </p>
 * <p>
 * Tensor indices can be incremented in various ways.  Currently we have the following:
 * <ol>
 * <li>From most significant axis forward, or left-to-right.</li>
 * <li>From least significant axis backward, or right-to-left.</li> 
 * </ol>
 * </p>
 * 
 * @author Christopher K. Allen
 * @since May 15, 2024
 *
 */
public class TensorIndexGenerator implements Iterable<Integer[]> {
    
    
    //
    // Creators 
    //
    
    /**
     * <p>
     * Creates a new <code>TensorIndexGenerator</code> for the given tensor shape.
     * </p>
     * <p>
     * The returned instance is ready for index generation.  For repeated iteration
     * of tensor indexes use <code>{@link #resetIndexCounter()}</code> or
     * <code>{@link #resetIndexCounter(IndexDirection)}</code>.
     * </p>
     * 
     * @param arrTensorShape    shape of target tensor (integer array containing dimension sizes)
     * 
     * @return  new tensor index generator ready for tensor index sequence iteration
     */
    public static TensorIndexGenerator from(int[] arrTensorShape) {
        return new TensorIndexGenerator(arrTensorShape);
    }
    
    /**
     * <p>
     * Creates a new <code>TensorIndexGenerator</code> for the given tensor shape.
     * </p>
     * <p>
     * The returned instance is ready for index generation.  For repeated iteration
     * of tensor indexes use <code>{@link #resetIndexCounter()}</code> or
     * <code>{@link #resetIndexCounter(IndexDirection)}</code>.
     * 
     * @param arrTensorShape    shape of target tensor (integer array containing dimension sizes)
     * @param enmDirection      sequence direction for iterating tensor indexes
     * 
     * @return  new tensor index generator ready for tensor index sequence iteration
     */
    public static TensorIndexGenerator  from(int[] arrTensorShape, IndexDirection enmDirection) {
        return new TensorIndexGenerator(arrTensorShape, enmDirection);
    }
    
    //
    // Internal Types
    //
    
    /**
     * Enumeration of tensor index increment directions.
     */
    public enum IndexDirection {
        
        /** Tensor indices are incremented forward from the most significant index. */
        LEFT_TO_RIGHT,
        
        /** Tensor indices are incremented backward from the least significant index. */
        RIGHT_TO_LEFT;
    }

    
    //
    // Class Constants
    //
    
    /** Default tensor index increment direction - used in construction */
    public static final IndexDirection      ENM_DIRECTION_DEFAULT = IndexDirection.RIGHT_TO_LEFT;

    
    //
    // Configuration
    //
    
    /** shape of the target tensor */
    private final int[]     arrShape;
    
    /** rank of the target tensor */
    private final int       intRank;
    
    /** total number of tensor indices, i.e., tensor size */
    private final int       intSize;

    
    //
    // Variables
    //
    
    /** Increment direction of tensor indices */
    private IndexDirection  enmDirection = ENM_DIRECTION_DEFAULT;
    
    /** integer array containing current tensor index (i1, i2, ..., ir) */
    private int[]           arrIndexCntr;
    
    /** Current value of total index counter */
    private int             indCurr = 0;

    
    //
    // Constructors
    //

    /**
     * <p>
     * Constructs a new instance of <code>TensorIndexGenerator</code> ready for index generation
     * in default direction.
     * </p>
     *
     * @param arrTensorShape    shape of the target tensor
     */
    public TensorIndexGenerator(int[] arrTensorShape) {
        this(arrTensorShape, ENM_DIRECTION_DEFAULT);
    }
    /**
     * <p>
     * Constructs a new instance of <code>TensorIndexGenerator</code> ready for index generation in 
     * the given direction.
     * </p>
     *
     * @param arrTensorShape    shape of the target tensor
     * @param enmDirection      sequence direction of iterated tensor indexes
     */
    public TensorIndexGenerator(int[] arrTensorShape, IndexDirection enmDirection) {
        
        // Set configuration
        this.arrShape = arrTensorShape;
        this.intRank = arrTensorShape.length;
        this.intSize = this.computeTensorSize(arrTensorShape);
        this.enmDirection = enmDirection;
        
        // Allocate/initialize variables
        this.arrIndexCntr = new int[this.getTensorRank()];
        this.resetIndexCounter(enmDirection);
    }
    
    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Returns the rank of the target tensor identified at construction.
     * </p>
     * 
     * @return rank of tensor, i.e., number of axes
     */
    public int      getTensorRank() {
        return this.intRank;
    }
    
    /**
     * <p>
     * Returns the shape of the target tensor identified at construction.
     * </p>
     * 
     * @return  tensor shape as an <code>int</code> array, axis size for each array dimension
     */
    public int[]    getTensorShape() {
        return this.arrShape;
    }
    
    /**
     * <p>
     * Returns the total number of elements within the target tensor.
     * </p>
     * 
     * @return  total element count for tensor, i.e., the multiply reduction of the shape
     */
    public int      getTensorSize() {
        return this.intSize;
    }
    
    /**
     * <p>
     * Returns the current tensor index increment director for iterating tensor index sequences.
     * </p>
     *  
     * @return  current sequence direction when iterating tensor indexes
     */
    public IndexDirection getIncrementDirection() {
        return this.enmDirection;
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Determines whether or not there are more tensor indexes left in the current iteration.
     * </p>
     * 
     * @return  <code>true</code> there are more tensor indexes available,
     *          <code>false</code> otherwise
     */
    public boolean      hasNext() {
        return this.indCurr < this.intSize;
    }
    
    /**
     * <p>
     * Returns the next tensor index in the full index set according to prescribed direction.
     * </p>
     * <p>
     * After construction this method may be called exactly <code>{@link #getTensorSize()}</code> times.
     * Calling the method beyond that will thrown an exception.
     * Optionally, one may use the <code>{@link #hasNext()}</code> method to determine if more
     * tensor indexes are available.
     * </p>   
     * <p>
     * <h2>Restarting</h2>
     * The methods <code>{@link #resetIndexCounter()}</code> or 
     * <code>{@link #resetIndexCounter(IndexDirection)}</code> can be invoked to restart this index
     * creation process.  After one of the above methods is called this method may be called exactly 
     * <code>{@link #getTensorSize()}</code> times.  
     * <p>
     * 
     * @return  next tensor index as a Java <code>Integer[]</code> object
     * 
     * @throws NoSuchElementException    no more tensor indexes are available
     */
    public Integer[]    nextIndex() throws NoSuchElementException {
        
        // Check current state
        if (this.indCurr >= this.intSize) 
            throw new NoSuchElementException(JavaRuntime.getQualifiedMethodNameSimple() + " - No more indexes available.");
        
        // Create the index object and increment total index counter
        Integer[]   arrIndex = this.createCurrentTensorIndex();
        this.indCurr++;
        
        // If the total index counter is less than maximal increment for next call
        if (this.indCurr < this.intSize) {

            try {
                // Increment the tensor index counter in appropriate direction for next call
                switch (this.enmDirection) {
                case LEFT_TO_RIGHT: 
                    this.incrementMostSigIndex(0);  // call with most significant axis position 
                    break;
                case RIGHT_TO_LEFT:
                    this.incrementLeastSigIndex(this.intRank - 1);  // call with least significant axis position
                    break;
                }

            } catch (ArithmeticException e) {
                throw new NoSuchElementException(JavaRuntime.getQualifiedMethodNameSimple() + " - Internal error: Unable to increment index.", e);
            }
        }
        
        return arrIndex;
    }
    
    /**
     * <p>
     * Creates and returns all tensor index tuples as an ordered list respecting the configured
     * index increment direction.
     * </p>
     * <p>
     * This method first invokes <code>{@link #resetIndexCounter()}</code> to initialize the
     * internal tensor index counting operation.  The method <code>{@link #nextIndex()}</code>
     * is then called <code>{@link #getTensorSize()}</code> times to populate the returned
     * list of index objects.
     * </p>
     * 
     * @return  an ordered list of all tensor index objects
     */
    public List<Integer[]>  allIndexes() {
        this.resetIndexCounter();
        
        // Create returned object and populate
        List<Integer[]>     lstIndexes = new ArrayList<>(this.getTensorSize());
        
        for (int iIndex=0; iIndex<this.intSize; iIndex++) {
            Integer[]       arrIndex = this.nextIndex();
            
            lstIndexes.add(arrIndex);
        }
        
        return lstIndexes;
    }
    
    /**
     * <p>
     * Resets the tensor index generator to create a new set of tensor indexes.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * After creation the current instance is ready to generate tensor indexes.  To generate
     * additional sets of indexes call the method to restart this instance.
     * </p>
     */
    public void resetIndexCounter() {
        this.resetIndexCounter(this.enmDirection);
    }
    
    /**
     * <p>
     * Resets the tensor index generator to create a new set of tensor indexes in the prescribed direction.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * After creation the current instance is ready to generate tensor indexes.  To generate
     * additional sets of indexes call the method to restart this instance.
     * </p>
     * 
     * @param enmDirection  the direction of the generated sequence of tensor indexes
     */
    public void resetIndexCounter(IndexDirection enmDirection) {
        this.enmDirection = enmDirection;
        this.indCurr = 0;
        
        // Reset the internal tensor index counter
        for (int iAxis=0; iAxis<this.getTensorRank(); iAxis++) { 
            this.arrIndexCntr[iAxis] = 0; 
        };
    }
    
    
    //
    // Iterable<Integer[]> Interface
    //
    
    /**
     * <p>
     * Creates and returns an <code>{@link Iterator}</code> interface that iterates over all
     * tensor indexes in this instance.
     * </p>
     * <p>
     * An anonymous <code>Iterator&lt;Integer[];gt;</code> interface is created which defers all 
     * operations to this instance.  This method may be called <em>only once</em> per index
     * iteration; that is, unless <code>{@link #resetIndexCounter()}</code> is invoked.
     * </p>
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Integer[]> iterator() {
        
        // Create an anonymous Iterator interface and return it
        Iterator<Integer[]>     iter = new Iterator<Integer[]>() {

            @Override
            public boolean hasNext() {
                return TensorIndexGenerator.this.hasNext();
            }

            @Override
            public Integer[]   next() {
                return TensorIndexGenerator.this.nextIndex();
            }
        };
        
        return iter;
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Computes and returns the total number of index sets for an array (tensor) of the given shape.
     * </p>
     * 
     * @param arrShape  the tensor shape as an integer array representing (N<sub>1</sub>,...,N<sub><i>r</i></sub>)
     * 
     * @return  total index count = N<sub>1</sub>&times;N<sub>2</sub>&times;...&times;N<sub><i>r</i></sub>
     */
    private int computeTensorSize(int[] arrShape) {
        int     szTensor = 1;
        
        for (int szDim : arrShape) {
            szTensor *= szDim;
        }
        
        return szTensor;
    }
    
    /**
     * <p>
     * Creates a Java <code>Integer</code> array containing the current index values within
     * the internal tensor index counter.
     * </p>
     * <p>
     * This method simple copies the values of <code>{@link #arrIndexCntr}</code> into a new
     * Java <code>Integer[]</code> object of the same dimensions.
     * </p>
     * 
     * @return  a Java <code>Integer[]</code> object containing the current contents of <code>{@link #arrIndexCntr}</code>
     */
    private Integer[]   createCurrentTensorIndex() {
        Integer[]       arrIndices = new Integer[this.getTensorRank()];
        
        for (int iAxis=0; iAxis<this.getTensorRank(); iAxis++) {
            arrIndices[iAxis] = this.arrIndexCntr[iAxis];
        }
        
        return arrIndices;
    }
    
    /**
     * <p>
     * Increments the current tensor index counter from the right side of the index set.
     * </p>
     * <p>
     * This is a recursive function and should be always be called with the argument equal
     * to the value (rank - 1), which is the Java index of the least significant tensor
     * index set (<i>i</i><sub>0</sub>, ..., <i>i</i><sub><i>r</i>-1</sub>).
     * </p>
     * 
     * @param iAxis     least signification axis position (rank - 1)
     * 
     * @throws ArithmeticException  overflow exception, the increment exceeded the tensor size
     */
    private void incrementLeastSigIndex(int iAxis) throws ArithmeticException {
        int szDim = this.arrShape[iAxis];
        int indVal = this.arrIndexCntr[iAxis];
        int indMax = szDim - 1;
        
        if (indVal < indMax) {
            this.arrIndexCntr[iAxis]++;
            
            return;
        
        } else {
            
            // Check if we are at final array axis - cannot increment
            if (iAxis == 0)
                throw new ArithmeticException(JavaRuntime.getQualifiedMethodNameSimple() + ": Array index overflow.");
            
            this.arrIndexCntr[iAxis] = 0;
            this.incrementLeastSigIndex(iAxis-1);
        }
    }
    
    /**
     * <p>
     * Increments the current tensor index counter from the left side of the index set.
     * </p>
     * <p>
     * This is a recursive function and should be always be called with the argument equal
     * to the value 0, which is the Java index of the most significant tensor
     * index set (<i>i</i><sub>0</sub>, ..., <i>i</i><sub><i>r</i>-1</sub>).
     * </p>
     * 
     * @param iAxis     most signification axis position (0)
     * 
     * @throws ArithmeticException  array index overflow (past the right-most index)
     */
    private void incrementMostSigIndex(int iAxis) throws ArithmeticException {
        int szDim = this.arrShape[iAxis];
        int indVal = this.arrIndexCntr[iAxis];
        int indMax = szDim - 1;
        
        if (indVal < indMax) {
            this.arrIndexCntr[iAxis]++;
            
            return;
        
        } else {
            
            // Check if we are at final array axis - cannot increment
            if (iAxis == this.getTensorRank()-1)
                throw new ArithmeticException(JavaRuntime.getQualifiedMethodNameSimple() + ": Array index overflow.");
            
            this.arrIndexCntr[iAxis] = 0;
            this.incrementMostSigIndex(iAxis+1);
        }
    }
    
}
