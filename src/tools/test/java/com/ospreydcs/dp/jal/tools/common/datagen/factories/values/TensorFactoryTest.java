/*
 * Project: dp-jal
 * File:	TensorFactoryTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.values
 * Type: 	TensorFactoryTest
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactorySpec;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorUtility;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>TensorFactory</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 13, 2025
 *
 */
public class TensorFactoryTest {


    //
    // Internal Types
    //
    
    /** Record containing array index as string and array value as Integer */
    private record ValueRecord(String index, Object value) {

        @Override
        public String toString() { return index + "=" + value.toString(); };
    };
    
    

    //
    // Test Resources
    //
    
    /** Test tensor shape */
    public static final int[]   ARR_SHAPE_EMPTY = {0};
    
    /** Test tensor shape */
    public static final int[]   ARR_SHAPE_SCALAR = {1};
    
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
    
    
    /** String prefix used for string-value generation */
    public static final String                  STR_PREFIX = "str:";
    

    /** Configuration for a unit increment string-value scalar factory */
    public static final ScalarFactorySpec     REC_CFG_STR_1 = ScalarFactorySpec.from(JalScalarType.STRING, false, 0, Integer.valueOf(1), STR_PREFIX);
    
    /** Configuration for an incremental boolean-value scalar factory */
    public static final ScalarFactorySpec     REC_CFG_BOL_1 = ScalarFactorySpec.from(JalScalarType.BOOLEAN, false, 0, Integer.valueOf(1));
    
    /** Configuration for a unit increment integer-value scalar factory */
    public static final ScalarFactorySpec     REC_CFG_INT_1 = ScalarFactorySpec.from(JalScalarType.INTEGER, false, 0, Integer.valueOf(1));
    
    /** Configuration for a 2 increment integer-value scalar factory */
    public static final ScalarFactorySpec     REC_CFG_INT_2 = ScalarFactorySpec.from(JalScalarType.INTEGER, false, 0, Integer.valueOf(2));
    
    /** Configuration for an incremental double-value scalar factory */
    public static final ScalarFactorySpec     REC_CFG_DBL_1 = ScalarFactorySpec.from(JalScalarType.DOUBLE, false, 0, Double.valueOf(1.602e-19));
    
    /** Configuration for a random double-value scalar factory */
    public static final ScalarFactorySpec     REC_CFG_DBL_RND = ScalarFactorySpec.from(JalScalarType.DOUBLE, true, 0);
    

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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#from(int[], com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactorySpec)}.
     */
    @Test
    public final void testFromIntArrayScalarFactoryConfig() {

        // Parameters
        final int[]                 arrShape = ARR_SHAPE_3TENSOR_MED;
        final ScalarFactorySpec   recCfg = REC_CFG_INT_1;
        
        final int           intRank = TensorUtility.computeTensorRank(arrShape);
        final int           intSize = TensorUtility.computeTensorSize(arrShape);
        final JalScalarType enmType = recCfg.enmType();
        
        TensorFactory   facTest = TensorFactory.from(arrShape, recCfg);
        Assert.assertEquals(recCfg.bolRandEnbl(), facTest.isRandom());
        Assert.assertEquals(intRank, facTest.getRank());
        Assert.assertEquals(intSize, facTest.getSize());
        Assert.assertTrue(TensorUtility.equivalent(arrShape, facTest.getShape()));
        
        Object objTensor = facTest.nextDatum();
        
        Assert.assertTrue(TensorUtility.isElementValueOfType(enmType, objTensor));
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#from(int[], com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactory)}.
     */
    @Test
    public final void testFromIntArrayScalarFactory() {

        // Parameters
        final int[]                 arrShape = ARR_SHAPE_VECTOR;
        final ScalarFactorySpec   recCfg = REC_CFG_DBL_1;
        
        final int           intRank = TensorUtility.computeTensorRank(arrShape);
        final int           intSize = TensorUtility.computeTensorSize(arrShape);
        final JalScalarType enmType = recCfg.enmType();
        
        TensorFactory   facTest = TensorFactory.from(arrShape, recCfg);
        Assert.assertEquals(recCfg.bolRandEnbl(), facTest.isRandom());
        Assert.assertEquals(intRank, facTest.getRank());
        Assert.assertEquals(intSize, facTest.getSize());
        Assert.assertTrue(TensorUtility.equivalent(arrShape, facTest.getShape()));
        
        Object objTensor = facTest.nextDatum();
        
        Assert.assertTrue(TensorUtility.isElementValueOfType(enmType, objTensor));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#TensorFactory(int[], com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactory)}.
     */
    @Test
    public final void testTensorFactory() {

        // Parameters
        final int[]               arrShape = ARR_SHAPE_ARRAY_LARGE;
        final ScalarFactorySpec   recCfg = REC_CFG_DBL_RND;
        final ScalarFactory       facVals = recCfg.newFactory();
        
        final int           intRank = TensorUtility.computeTensorRank(arrShape);
        final int           intSize = TensorUtility.computeTensorSize(arrShape);
        final JalScalarType enmType = recCfg.enmType();
        
        TensorFactory   facTest = new TensorFactory(arrShape, facVals);
        Assert.assertEquals(recCfg.bolRandEnbl(), facTest.isRandom());
        Assert.assertEquals(intRank, facTest.getRank());
        Assert.assertEquals(intSize, facTest.getSize());
        Assert.assertTrue(TensorUtility.equivalent(arrShape, facTest.getShape()));
        
        Object objTensor = facTest.nextDatum();
        
        Assert.assertTrue(TensorUtility.isElementValueOfType(enmType, objTensor));
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#isRandom()}.
//     */
//    @Test
//    public final void testIsRandom() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#getType()}.
//     */
//    @Test
//    public final void testGetType() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#getSeed()}.
//     */
//    @Test
//    public final void testGetSeed() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#getRank()}.
//     */
//    @Test
//    public final void testGetRank() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#getShape()}.
//     */
//    @Test
//    public final void testGetShape() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#getSize()}.
//     */
//    @Test
//    public final void testGetSize() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#nextDatum()}.
     */
    @Test
    public final void testNextValueEmpty() {

        // Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_INT_1;
        final int[]                 arrShape = ARR_SHAPE_EMPTY;
        
        final int           intRank = TensorUtility.computeTensorRank(arrShape);
        final int           intSize = TensorUtility.computeTensorSize(arrShape);
        final int           intSeed = Math.toIntExact( recCfg.lngSeed() );
        final boolean       bolRand = recCfg.bolRandEnbl();
        
        // Create the tensor factory and check configuration
        TensorFactory  facTest = TensorFactory.from(arrShape, recCfg);
        Assert.assertEquals(intSeed, facTest.getSeed());
        Assert.assertEquals(bolRand, facTest.isRandom() );
        Assert.assertEquals(intRank, facTest.getRank());
        Assert.assertEquals(intSize, facTest.getSize());
        Assert.assertTrue(TensorUtility.equivalent(arrShape, facTest.getShape()));
        
        Object objValue = facTest.nextDatum();
        if (objValue instanceof ArrayList vec) 
            Assert.assertTrue(vec.size() == 0);
        else
            Assert.fail("Data value not ArrayList: " + objValue.getClass().getName());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#nextDatum()}.
     */
    @Test
    public final void testNextValueDoubleScalar() {

        // Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_DBL_1;
        final int[]                 arrShape = ARR_SHAPE_SCALAR;
        
        final int           intRank = TensorUtility.computeTensorRank(arrShape);
        final int           intSize = TensorUtility.computeTensorSize(arrShape);
        final JalScalarType enmType = recCfg.enmType();
        final int           intSeed = Math.toIntExact( recCfg.lngSeed() );
        final double        dblIncr = recCfg.numIncr().doubleValue();
        final boolean       bolRand = recCfg.bolRandEnbl();
        final int           cntVals = 10;
        
        // Create the tensor factory and check configuration
        TensorFactory  facTest = TensorFactory.from(arrShape, recCfg);
        Assert.assertEquals(intSeed, facTest.getSeed());
        Assert.assertEquals(bolRand, facTest.isRandom() );
        Assert.assertEquals(intRank, facTest.getRank());
        Assert.assertEquals(intSize, facTest.getSize());
        Assert.assertTrue(TensorUtility.equivalent(arrShape, facTest.getShape()));
        
        Object objTensor = facTest.nextDatum();
        Assert.assertTrue(TensorUtility.isElementValueOfType(enmType, objTensor));
        
        if (objTensor instanceof ArrayList vec) 
            Assert.assertEquals(1, vec.size());
        else
            Assert.fail("First data value not ArrayList: " + objTensor.getClass().getName());

        // Check the next 'cntVals' values of the tensor factory
        List<Double>    lstVals = new ArrayList<>(cntVals);
        
        Double  dblCurr = dblIncr;
        for (int iVal=0; iVal<cntVals; iVal++) {
            objTensor = facTest.nextDatum();
            
            if (objTensor instanceof ArrayList vec) {
                Assert.assertEquals(1, vec.size());
                Object objVal = vec.getFirst();
                
                if (objVal instanceof Double dblVal) { 
                    Assert.assertEquals(dblCurr, dblVal);
                    
                    lstVals.add(dblVal);
                    
                } else
                    Assert.fail("Tensor #" + iVal + " was not of value type Double.");
            } else
                Assert.fail("Tensor #" + iVal + " data value not ArrayList: " + objTensor.getClass().getName());
            
            dblCurr += dblIncr;
        }
        
        // Print out values to standard output
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Scalar double values: " + lstVals);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#nextDatum()}.
     */
    @Test
    public final void testNextValueStringScalar() {

        // Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_STR_1;
        final int[]                 arrShape = ARR_SHAPE_SCALAR;
        
        final int           intRank = TensorUtility.computeTensorRank(arrShape);
        final int           intSize = TensorUtility.computeTensorSize(arrShape);
        final JalScalarType enmType = recCfg.enmType();
        final int           intSeed = Math.toIntExact( recCfg.lngSeed() );
        final int           intIncr = recCfg.numIncr().intValue();
        final boolean       bolRand = recCfg.bolRandEnbl();
        final int           cntVals = 10;
        
        // Create the tensor factory and check configuration
        TensorFactory  facTest = TensorFactory.from(arrShape, recCfg);
        Assert.assertEquals(intSeed, facTest.getSeed());
        Assert.assertEquals(bolRand, facTest.isRandom() );
        Assert.assertEquals(intRank, facTest.getRank());
        Assert.assertEquals(intSize, facTest.getSize());
        Assert.assertTrue(TensorUtility.equivalent(arrShape, facTest.getShape()));
        
        Object objTensor = facTest.nextDatum();
        Assert.assertTrue(TensorUtility.isElementValueOfType(enmType, objTensor));
        
        if (objTensor instanceof ArrayList vec) 
            Assert.assertEquals(1, vec.size());
        else
            Assert.fail("First data value not ArrayList: " + objTensor.getClass().getName());

        // Check the next 'cntVals' values of the tensor factory
        List<String>    lstVals = new ArrayList<>(cntVals);
        
        int     intCurr = intIncr;
        String  strCurr = STR_PREFIX + intCurr;
        for (int iVal=0; iVal<cntVals; iVal++) {
            objTensor = facTest.nextDatum();
            
            if (objTensor instanceof ArrayList vec) {
                Assert.assertEquals(1, vec.size());
                Object objVal = vec.getFirst();
                
                if (objVal instanceof String strVal) {
                    Assert.assertEquals(strCurr, strVal);
                    
                    lstVals.add(strVal);
                    
                } else
                    Assert.fail("Tensor #" + iVal + " was not of value type Double.");
            } else
                Assert.fail("Tensor #" + iVal + " data value not ArrayList: " + objTensor.getClass().getName());
            
            intCurr++;
            strCurr = STR_PREFIX + intCurr;
        }
        
        // Print out values to standard output
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Scalar string values: " + lstVals);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#nextDatum()}.
     */
    @Test
    public final void testNextValueBooleanArraySmall() {

        // Parameters
        final int[]                 arrShape = ARR_SHAPE_ARRAY_SMALL;
        final ScalarFactorySpec   recCfg = REC_CFG_BOL_1;
        
        final int           intRank = TensorUtility.computeTensorRank(arrShape);
        final int           intSize = TensorUtility.computeTensorSize(arrShape);
        final JalScalarType enmType = recCfg.enmType();
        final int           intSeed = Math.toIntExact( recCfg.lngSeed() );
        final boolean       bolRand = recCfg.bolRandEnbl();
        
        // Create the tensor factory and check configuration
        TensorFactory facTest = TensorFactory.from(arrShape, recCfg);
        Assert.assertEquals(intSeed, facTest.getSeed());
        Assert.assertEquals(bolRand, facTest.isRandom() );
        Assert.assertEquals(intRank, facTest.getRank());
        Assert.assertEquals(intSize, facTest.getSize());
        Assert.assertTrue(TensorUtility.equivalent(arrShape, facTest.getShape()));
        
        Object objArray = facTest.nextDatum();
        Assert.assertTrue(TensorUtility.isElementValueOfType(enmType, objArray));

        // Check the values of the first tensor
        List<ValueRecord>    lstValues = new ArrayList<>(facTest.getSize());
        TensorIndexGenerator facIndexes = TensorIndexGenerator.from(arrShape);
        
        for (Integer[] arrIndex : facIndexes) {
            String  strIndex = TensorUtility.toString(arrIndex);
            Object  objElem = TensorUtility.extractElementAt(arrIndex, objArray);

            lstValues.add(new ValueRecord(strIndex, objElem));
            
            Assert.assertTrue(enmType.isAssignable(objElem));
        }
        
        // Print out values to standard output
        String  strShape = TensorUtility.toString(arrShape);
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Array of type " + enmType + ", shape = " + strShape + ", random = " + bolRand + ":");
        System.out.println("  " + lstValues);
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#nextDatum()}.
     */
    @Test
    public final void testNextValueIntegerArraySmall() {

        // Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_INT_1;
        final int[]                 arrShape = ARR_SHAPE_ARRAY_SMALL;
        
        final int           intRank = TensorUtility.computeTensorRank(arrShape);
        final int           intSize = TensorUtility.computeTensorSize(arrShape);
        final JalScalarType enmType = recCfg.enmType();
        final int           intSeed = Math.toIntExact( recCfg.lngSeed() );
        final boolean       bolRand = recCfg.bolRandEnbl();
        
        // Create the tensor factory and check configuration
        TensorFactory  facTest = TensorFactory.from(arrShape, recCfg);
        Assert.assertEquals(intSeed, facTest.getSeed());
        Assert.assertEquals(bolRand, facTest.isRandom() );
        Assert.assertEquals(intRank, facTest.getRank());
        Assert.assertEquals(intSize, facTest.getSize());
        Assert.assertTrue(TensorUtility.equivalent(arrShape, facTest.getShape()));
        
        Object objTensor = facTest.nextDatum();
        Assert.assertTrue(TensorUtility.isElementValueOfType(enmType, objTensor));

        // Check the values of the first tensor
        List<ValueRecord>       lstValues = new ArrayList<>(facTest.getSize());
        Map<String, Integer>    mapValues = new HashMap<>();
        TensorIndexGenerator    facIndexes = TensorIndexGenerator.from(arrShape);
        
        for (Integer[] arrIndex : facIndexes) {
            Object  objElem = TensorUtility.extractElementAt(arrIndex, objTensor);
            String  strIndex = TensorUtility.toString(arrIndex);

            if (objElem instanceof Integer intElem) {
                
                lstValues.add(new ValueRecord(strIndex, intElem));
                mapValues.put(strIndex, intElem);
                
            } else {
                Assert.fail("Element at " + strIndex + " was not an Integer.");
            }
        }
        
        // Print values to standard output
        String  strShape = TensorUtility.toString(arrShape);
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Array of type " + enmType + ", shape = " + strShape + ", random = " + bolRand + ":");
        System.out.println("  " + mapValues);
        System.out.println("  " + lstValues);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#nextDatum()}.
     */
    @Test
    public final void testNextValueDouble3TensorMed() {

        // Parameters
        final int[]                 arrShape = ARR_SHAPE_3TENSOR_MED;
        final ScalarFactorySpec   recCfg = REC_CFG_INT_1;
        
        final int           intRank = TensorUtility.computeTensorRank(arrShape);
        final int           intSize = TensorUtility.computeTensorSize(arrShape);
        final JalScalarType enmType = recCfg.enmType();
        final long          lngSeed = recCfg.lngSeed();
        final boolean       bolRand = recCfg.bolRandEnbl();
        
        // Create the tensor factory and check configuration
        TensorFactory   facTest = TensorFactory.from(arrShape, recCfg);
        Assert.assertEquals(lngSeed, facTest.getSeed());
        Assert.assertEquals(bolRand, facTest.isRandom() );
        Assert.assertEquals(intRank, facTest.getRank());
        Assert.assertEquals(intSize, facTest.getSize());
        Assert.assertTrue(TensorUtility.equivalent(arrShape, facTest.getShape()));
        
        Object objArray = facTest.nextDatum();
        Assert.assertTrue(TensorUtility.isElementValueOfType(enmType, objArray));

        // Check the values of the first tensor
        List<ValueRecord>    lstValues = new ArrayList<>(facTest.getSize());
        TensorIndexGenerator genIndexes = TensorIndexGenerator.from(arrShape);
        
        for (Integer[] arrIndex : genIndexes) {
            String  strIndex = TensorUtility.toString(arrIndex);
            Object  objElem = TensorUtility.extractElementAt(arrIndex, objArray);

            lstValues.add(new ValueRecord(strIndex, objElem));
            
            Assert.assertTrue(enmType.isAssignable(objElem));
        }
        
        Assert.assertEquals(facTest.getSize(), lstValues.size());
        Assert.assertEquals(genIndexes.getTensorSize(), lstValues.size());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory#nextDatum()}.
     */
    @Test
    public final void testNextValueInteger4TensorSmal() {

        // Parameters
        final int[]                 arrShape = ARR_SHAPE_4TENSOR_SMALL;
        final ScalarFactorySpec   recCfg = REC_CFG_INT_2;
        
        final int           intRank = TensorUtility.computeTensorRank(arrShape);
        final int           intSize = TensorUtility.computeTensorSize(arrShape);
        final JalScalarType enmType = recCfg.enmType();
        final long          lngSeed = recCfg.lngSeed();
        final int           intIncr = recCfg.numIncr().intValue();
        final boolean       bolRand = recCfg.bolRandEnbl();
        final int           cntVals = 10;
        
        // Create the tensor factory and check configuration
        TensorFactory   facTest = TensorFactory.from(arrShape, recCfg);
        Assert.assertEquals(lngSeed, facTest.getSeed());
        Assert.assertEquals(bolRand, facTest.isRandom() );
        Assert.assertEquals(intRank, facTest.getRank());
        Assert.assertEquals(intSize, facTest.getSize());
        Assert.assertTrue(TensorUtility.equivalent(arrShape, facTest.getShape()));
        
        Object objValue = facTest.nextDatum();
        Assert.assertTrue(TensorUtility.isElementValueOfType(enmType, objValue));

        // Check the values of the first tensor
        List<ValueRecord>    lstElems = new ArrayList<>(facTest.getSize());
        TensorIndexGenerator facIndexes = TensorIndexGenerator.from(arrShape);
        
        Integer     intCurr = Math.toIntExact(lngSeed);
        for (int iVal=0; iVal<cntVals; iVal++) {
            for (Integer[] arrIndex : facIndexes) {
                String  strIndex = TensorUtility.toString(arrIndex);
                Object  objElem = TensorUtility.extractElementAt(arrIndex, objValue);

                lstElems.add(new ValueRecord(strIndex, objElem));

                Assert.assertTrue(enmType.isAssignable(objElem));

                if (objElem instanceof Integer intVal) 
                    Assert.assertEquals(intCurr, intVal);
                else
                    Assert.fail("Tensor element not of type Integer: " + objElem.getClass().getName());
                
                intCurr += intIncr;
            }
            objValue = facTest.nextDatum();
            facIndexes.resetIndexCounter();
        }
        
        Assert.assertEquals(facTest.getSize() * cntVals, lstElems.size());
        Assert.assertEquals(facIndexes.getTensorSize() * cntVals, lstElems.size());
    }
}
