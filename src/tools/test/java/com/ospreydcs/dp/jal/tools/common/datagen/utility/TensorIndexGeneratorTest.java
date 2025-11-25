/*
 * Project: dp-jal
 * File:	TensorIndexGeneratorTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.utility
 * Type: 	TensorIndexGeneratorTest
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
package com.ospreydcs.dp.jal.tools.common.datagen.utility;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator.IndexDirection;


/**
 * <p>
 * JUnit test cases for class <code>TensorIndexGenerator</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 13, 2025
 *
 */
public class TensorIndexGeneratorTest {

    
    //
    // Test Resources
    //
    
    /** Test tensor shape */
    public static final int[]   ARR_SHAPE_SCALAR = {0};
    
    /** Test tensor shape */
    public static final int[]   ARR_SHAPE_ONE = {1};
    
    /** Test tensor shape */
    public static final int[]   ARR_SHAPE_VECTOR = {10};
    
    /** Test tensor shape */
    public static final int[]   ARR_SHAPE_ARRAY_SMALL = { 3, 3 }; 
    
    /** Test tensor shape */
    public static final int[]   ARR_SHAPE_ARRAY_MED = { 10, 10 }; 
    
    /** Test tensor shape */
    public static final int[]   ARR_SHAPE_ARRAY_LARGE = { 100, 100 }; 
    
    /** Test tensor shape */
    public static final int[]   ARR_SHAPE_3TENSOR_SMALL = { 3, 3, 3 }; 
    
    /** Test tensor shape */
    public static final int[]   ARR_SHAPE_3TENSOR_MED = { 10, 10, 10 }; 
    
    /** Test tensor shape */
    public static final int[]   ARR_SHAPE_4TENSOR_SMALL = { 2, 2, 2, 2 }; 
    

    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    
    // 
    // Test Cases
    //
    
    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#from(int[])}.
     */
    @Test
    public final void testFromIntArray() {
        
        // Parameters
        final int[]     arrShape = ARR_SHAPE_3TENSOR_SMALL;
        
        // Create generator
        TensorIndexGenerator    genIndexes = TensorIndexGenerator.from(arrShape);
        
        Assert.assertEquals(genIndexes.getTensorRank(), arrShape.length);
        Assert.assertEquals(genIndexes.getTensorShape(), arrShape);
        Assert.assertEquals(genIndexes.getTensorSize(), this.computeTensorSize(arrShape));
        Assert.assertEquals(genIndexes.getIncrementDirection(), TensorIndexGenerator.ENM_DIRECTION_DEFAULT);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#from(int[], com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator.IndexDirection)}.
     */
    @Test
    public final void testFromIntArrayIndexDirection() {
        
        // Parameters
        final int[]             arrShape = ARR_SHAPE_3TENSOR_SMALL;
        final IndexDirection    enmDirection = IndexDirection.LEFT_TO_RIGHT;
        
        // Create generator
        TensorIndexGenerator    genIndexes = TensorIndexGenerator.from(arrShape, enmDirection);
        
        Assert.assertEquals(genIndexes.getTensorRank(), arrShape.length);
        Assert.assertEquals(genIndexes.getTensorShape(), arrShape);
        Assert.assertEquals(genIndexes.getTensorSize(), this.computeTensorSize(arrShape));
        Assert.assertEquals(genIndexes.getIncrementDirection(), enmDirection);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#TensorIndexGenerator(int[])}.
     */
    @Test
    public final void testTensorIndexGeneratorIntArray() {
        
        // Parameters
        final int[]     arrShape = ARR_SHAPE_3TENSOR_SMALL;
        
        // Create generator
        TensorIndexGenerator    genIndexes = new TensorIndexGenerator(arrShape);
        
        Assert.assertEquals(genIndexes.getTensorRank(), arrShape.length);
        Assert.assertEquals(genIndexes.getTensorShape(), arrShape);
        Assert.assertEquals(genIndexes.getTensorSize(), this.computeTensorSize(arrShape));
        Assert.assertEquals(genIndexes.getIncrementDirection(), TensorIndexGenerator.ENM_DIRECTION_DEFAULT);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#TensorIndexGenerator(int[], com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator.IndexDirection)}.
     */
    @Test
    public final void testTensorIndexGeneratorIntArrayIndexDirection() {
        
        // Parameters
        final int[]             arrShape = ARR_SHAPE_3TENSOR_SMALL;
        final IndexDirection    enmDirection = IndexDirection.LEFT_TO_RIGHT;
        
        // Create generator
        TensorIndexGenerator    genIndexes = new TensorIndexGenerator(arrShape, enmDirection);
        
        Assert.assertEquals(genIndexes.getTensorRank(), arrShape.length);
        Assert.assertEquals(genIndexes.getTensorShape(), arrShape);
        Assert.assertEquals(genIndexes.getTensorSize(), this.computeTensorSize(arrShape));
        Assert.assertEquals(genIndexes.getIncrementDirection(), enmDirection);
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#getTensorRank()}.
//     */
//    @Test
//    public final void testGetTensorRank() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#getTensorShape()}.
//     */
//    @Test
//    public final void testGetTensorShape() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#getTensorSize()}.
//     */
//    @Test
//    public final void testGetTensorSize() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#getIncrementDirection()}.
//     */
//    @Test
//    public final void testGetIncrementDirection() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#hasNext()}.
     */
    @Test
    public final void testHasNext() {

        // Parameters
        final int[]             arrShape = ARR_SHAPE_SCALAR;
        final IndexDirection    enmDirection = IndexDirection.RIGHT_TO_LEFT;
        
        // Create generator
        TensorIndexGenerator    genIndexes = TensorIndexGenerator.from(arrShape, enmDirection);

        // A scalar has no indexes
        Assert.assertFalse(genIndexes.hasNext());
        
        // Check other properties of a scalar
        Assert.assertEquals(0, genIndexes.getTensorSize());
        Assert.assertEquals(1, genIndexes.getTensorRank());
        Assert.assertEquals(arrShape, genIndexes.getTensorShape());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#nextIndex()}.
     */
    @Test
    public final void testNextIndexArraySmall() {
        
        // Parameters
        final int[]             arrShape = ARR_SHAPE_ARRAY_SMALL;
        final IndexDirection    enmDirection = IndexDirection.LEFT_TO_RIGHT;
        
        // Create generator
        TensorIndexGenerator    genIndexes = TensorIndexGenerator.from(arrShape, enmDirection);

        // Generate tensor indexes and collect
        List<Integer[]>     lstIndexes = new ArrayList<>(genIndexes.getTensorSize());
        
        while (genIndexes.hasNext()) {
            Integer[]   arrIndex = genIndexes.nextIndex();
            
            lstIndexes.add(arrIndex);
        }
        
        System.out.println("Tensor indexes for shape " + this.toString(arrShape) + ": " + this.toString(lstIndexes));
        
        Assert.assertEquals(genIndexes.getTensorSize(), lstIndexes.size());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#nextIndex()}.
     */
    @Test
    public final void testNextIndexArraySmallBackwards() {
        
        // Parameters
        final int[]             arrShape = ARR_SHAPE_ARRAY_SMALL;
        final IndexDirection    enmDirection = IndexDirection.RIGHT_TO_LEFT;
        
        // Create generator
        TensorIndexGenerator    genIndexes = TensorIndexGenerator.from(arrShape, enmDirection);

        // Generate tensor indexes and collect
        List<Integer[]>     lstIndexes = new ArrayList<>(genIndexes.getTensorSize());
        
        while (genIndexes.hasNext()) {
            Integer[]   arrIndex = genIndexes.nextIndex();
            
            lstIndexes.add(arrIndex);
        }
        
        System.out.println("Tensor indexes for shape " + this.toString(arrShape) + ": " + this.toString(lstIndexes));
        
        Assert.assertEquals(genIndexes.getTensorSize(), lstIndexes.size());
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#nextIndex()}.
     */
    @Test
    public final void testNextIndex3TensorSmall() {
        
        // Parameters
        final int[]             arrShape = ARR_SHAPE_3TENSOR_SMALL;
        final IndexDirection    enmDirection = IndexDirection.LEFT_TO_RIGHT;
        
        // Create generator
        TensorIndexGenerator    genIndexes = TensorIndexGenerator.from(arrShape, enmDirection);

        // Generate tensor indexes and collect
        List<Integer[]>     lstIndexes = new ArrayList<>(genIndexes.getTensorSize());
        
        while (genIndexes.hasNext()) {
            Integer[]   arrIndex = genIndexes.nextIndex();
            
            lstIndexes.add(arrIndex);
        }
        
        System.out.println("Tensor indexes for shape " + this.toString(arrShape) + ": " + this.toString(lstIndexes));
        
        Assert.assertEquals(genIndexes.getTensorSize(), lstIndexes.size());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#allIndexes()}.
     */
    @Test
    public final void testAllIndexes() {

        // Parameters
        final int[]             arrShape = ARR_SHAPE_4TENSOR_SMALL;
        final IndexDirection    enmDirection = IndexDirection.RIGHT_TO_LEFT;
        
        // Create generator
        TensorIndexGenerator    genIndexes = TensorIndexGenerator.from(arrShape, enmDirection);

        // Generate all tensor indexes at once
        List<Integer[]>     lstIndexes = genIndexes.allIndexes();
        
        System.out.println("All indexes for shape " + this.toString(arrShape) + ": " + this.toString(lstIndexes));
        
        Assert.assertEquals(genIndexes.getTensorSize(), lstIndexes.size());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#resetIndexCounter()}.
     */
    @Test
    public final void testResetIndexCounter() {

        // Parameters
        final int[]             arrShape = ARR_SHAPE_4TENSOR_SMALL;
        final IndexDirection    enmDirection = IndexDirection.RIGHT_TO_LEFT;
        
        // Create generator
        TensorIndexGenerator    genIndexes = TensorIndexGenerator.from(arrShape, enmDirection);

        // Generate all tensor indexes at once
        List<Integer[]>     lstIndexes = genIndexes.allIndexes();
        Assert.assertEquals(genIndexes.getTensorSize(), lstIndexes.size());
        
        // There should be no indexes left
        Assert.assertFalse(genIndexes.hasNext());
        
        // Reset index counter
        genIndexes.resetIndexCounter();
        Assert.assertTrue(genIndexes.hasNext());
        
        lstIndexes = genIndexes.allIndexes();
        Assert.assertFalse(genIndexes.hasNext());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#resetIndexCounter(com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator.IndexDirection)}.
     */
    @Test
    public final void testResetIndexCounterIndexDirection() {

        // Parameters
        final int[]             arrShape = ARR_SHAPE_ARRAY_SMALL;
        final IndexDirection    enmDirection1 = IndexDirection.RIGHT_TO_LEFT;
        final IndexDirection    enmDirection2 = IndexDirection.LEFT_TO_RIGHT;
        
        // Create generator
        TensorIndexGenerator    genIndexes = TensorIndexGenerator.from(arrShape, enmDirection1);

        // Generate all tensor indexes at once
        List<Integer[]>     lstIndexes1 = genIndexes.allIndexes();
        Assert.assertEquals(genIndexes.getTensorSize(), lstIndexes1.size());
        
        // There should be no indexes left
        Assert.assertFalse(genIndexes.hasNext());
        
        // Reset index counter
        genIndexes.resetIndexCounter(enmDirection2);
        Assert.assertTrue(genIndexes.hasNext());
        
        List<Integer[]>     lstIndexes2 = genIndexes.allIndexes();
        Assert.assertFalse(genIndexes.hasNext());
        
        System.out.println("All indexfor shape " + this.toString(arrShape) + ": "); 
        System.out.println("  Direction 1: " + this.toString(lstIndexes1));
        System.out.println("  Direction 2: " + this.toString(lstIndexes2));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator#iterator()}.
     */
    @Test
    public final void testIteratorArraySmall() {
        
        // Parameters
        final int[]             arrShape = ARR_SHAPE_ARRAY_SMALL;
        final IndexDirection    enmDirection = IndexDirection.LEFT_TO_RIGHT;
        
        // Create generator
        TensorIndexGenerator    genIndexes = TensorIndexGenerator.from(arrShape, enmDirection);

        // Generate tensor indexes and collect
        List<Integer[]>     lstIndexes = new ArrayList<>(genIndexes.getTensorSize());
        
        for (Integer[] arrIndex : genIndexes) {
            
            lstIndexes.add(arrIndex);
        }
        
        System.out.println("Iterator indexes for shape " + this.toString(arrShape) + ": " + this.toString(lstIndexes));
        
        Assert.assertEquals(genIndexes.getTensorSize(), lstIndexes.size());
    }

    /**
     * Test method for {@link TensorIndexGenerator#iterator()}.
     */
    @Test
    public final void testIterator3TensorSmall() {
        
        // Parameters
        final int[]             arrShape = ARR_SHAPE_3TENSOR_SMALL;
        final IndexDirection    enmDirection = IndexDirection.LEFT_TO_RIGHT;
        
        // Create generator
        TensorIndexGenerator    genIndexes = TensorIndexGenerator.from(arrShape, enmDirection);

        // Generate tensor indexes and collect
        List<Integer[]>     lstIndexes = new ArrayList<>(genIndexes.getTensorSize());
        
        for (Integer[] arrIndex : genIndexes) {
            
            lstIndexes.add(arrIndex);
        }
        
        System.out.println("Iterator indexes for shape " + this.toString(arrShape) + ": " + this.toString(lstIndexes));
        
        Assert.assertEquals(genIndexes.getTensorSize(), lstIndexes.size());
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Computes the size of a tensor with the given shape.
     * </p>
     * 
     * @param arrTensorShape    integer array containing the shape of target tensor
     * 
     * @return  number of elements within target tensor (i.e., the size)
     */
    private int computeTensorSize(int[] arrTensorShape) {
        int     cntAxes = arrTensorShape.length;
        int     szTensor = 1;
        
        for (int iAxis=0; iAxis<cntAxes; iAxis++) {
            int     szDim = arrTensorShape[iAxis];
            
            szTensor *= szDim;
        }
        
        return szTensor;
    }
    
    /**
     * <p>
     * Creates and returns a string description for the given tensor shape.
     * </p>
     * 
     * @param arrTensorShape    integer array containing tensor shape
     * 
     * @return  tensor shape string representation
     */
    private String  toString(int[] arrTensorShape) {
        int     cntAxes = arrTensorShape.length;
        
        String      strBuf = "(";
        for (int iAxis=0; iAxis<cntAxes; iAxis++) {
            int     szDim = arrTensorShape[iAxis];
            
            strBuf += szDim;
            
            if (iAxis < cntAxes-1)
                strBuf += ", ";
        }
        strBuf += ")";
        
        return strBuf;
    }
    
    /**
     * <p>
     * Creates and returns a string representation for the given tensor index.
     * </p>
     * 
     * @param arrIndex  integer array containing a tensor index
     * 
     * @return  tensor index string representation
     */
    private String  toString(Integer[] arrIndex) {
        int     cntIndices = arrIndex.length;
        
        String      strBuf = "(";
        for (int iAxis=0; iAxis<cntIndices; iAxis++) {
            Integer intIndex = arrIndex[iAxis];
            
            strBuf += intIndex;
            
            if (iAxis < cntIndices-1)
                strBuf += ", ";
        }
        strBuf += ")";
        
        return strBuf;
    }
    
    /**
     * <p>
     * Creates and returns a string representation for the ordered list of tensor indexes.
     * </p>
     * 
     * @param lstIndexes    ordered list of integer arrays containing string indexes
     * 
     * @return  a string representation of all indexes within the argument
     */
    private String  toString(List<Integer[]> lstIndexes) {
        String  strBuf = "{";
        String  strComma = ", ";
        
        for (Integer[] arrIndex : lstIndexes) {
            strBuf += this.toString(arrIndex);
            strBuf += strComma;
        }
        
        int indLast = strBuf.lastIndexOf(strComma);
        strBuf = strBuf.substring(0, indLast);
        strBuf += "}";
        
        return strBuf;
    }
}
