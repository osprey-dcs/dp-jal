/*
 * Project: dp-jal
 * File:	TensorFactorySpecTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.specs
 * Type: 	TensorFactorySpecTest
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
 * @since Dec 16, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorIndexGenerator;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.TensorUtility;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.values.JalToolsTensorFactoryConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for record <code>TensorFactorySpec</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 16, 2025
 *
 */
public class TensorFactorySpecTest {

    
    //
    // JAL Tools Resources
    //
    
    /** The tensor factory default configuration */
    private static final JalToolsTensorFactoryConfig    CFG_DEF = JalToolsConfig.getInstance().datagen.values.tensor;
    
    
    //
    // Class Constants
    //
    
    /** The default tensor shape */
    public static final int[]           ARR_TENS_SHAPE_DEF = CFG_DEF.shapeArray();
    
    /** The default tensor element type */
    public static final JalScalarType   ENM_ELEM_TYPE_DEF = CFG_DEF.elements.type;
    
    
    /** The default enable/disable random element value generation */
    public static final boolean         BOL_ELEM_RAND_ENBL_DEF = CFG_DEF.elements.random.enabled;
    
    /** The default seed value for random element generation */
    public static final long            LNG_ELEM_RAND_SEED_DEF = CFG_DEF.elements.random.seed;
    
    
    /** Tensor shape for parsing test 1 */
    public static final int[]           ARR_TENS_SHAPE_PARSE_1 = { 1, 2, 3, 4 };
    
    /** Tensor shape for parsing test 2 */
    public static final int[]           ARR_TENS_SHAPE_PARSE_2 = { 1, 4, 9 };
    
    /** Simulated command-line argument collection for specification parsing test 1 */
    public static final String[]        ARR_STR_ARGS_PARSE_1 = { 
                                            "1", "2", "3", "4", 
                                            "STRING", "true", "23", "42", "str:" }; 

    /** Simulated command-line argument collection for specification parsing test 2 */
    public static final String[]        ARR_STR_ARGS_PARSE_2 = { 
                                            "1", "4", "9", 
                                            "BOOLEAN" }; 
    
    //
    // Test Resources
    //
    
    /** The default scalar factory specification used by <code>TensorFactorySpec</code> */
    public static final ScalarFactorySpec       REC_SCAL_SPEC_TENSOR_DEF = (BOL_ELEM_RAND_ENBL_DEF) ?
                                                                     ScalarFactorySpec.from(ENM_ELEM_TYPE_DEF, BOL_ELEM_RAND_ENBL_DEF, LNG_ELEM_RAND_SEED_DEF) :
                                                                     ScalarFactorySpec.from(ENM_ELEM_TYPE_DEF, BOL_ELEM_RAND_ENBL_DEF);
    
    /** The default scalar factory specification with random number generation enabled */
    public static final ScalarFactorySpec       REC_SCAL_SPEC_RAND_ENBL = ScalarFactorySpec.from(ENM_ELEM_TYPE_DEF, true, LNG_ELEM_RAND_SEED_DEF);
    
    /** The scalar factory specification for parsing test 1 */
    public static final ScalarFactorySpec       REC_SCAL_SPEC_PARSE_1 = ScalarFactorySpec.from(JalScalarType.STRING, true, 23, Integer.valueOf(42), "str:");
    
    /** The Scalar factory specification for parsing test 2 */
    public static final ScalarFactorySpec       REC_SCAL_SPEC_PARSE_2 = (BOL_ELEM_RAND_ENBL_DEF) ?
            ScalarFactorySpec.from(JalScalarType.BOOLEAN, BOL_ELEM_RAND_ENBL_DEF, LNG_ELEM_RAND_SEED_DEF) :
            ScalarFactorySpec.from(JalScalarType.BOOLEAN, BOL_ELEM_RAND_ENBL_DEF);

    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TensorFactorySpec#from()}.
     */
    @Test
    public final void testFrom() {
        
        // Test Parameters
        final int[]                 arrShape = ARR_TENS_SHAPE_DEF;
        final ScalarFactorySpec     specScalar = REC_SCAL_SPEC_TENSOR_DEF;
        
        // Create tensor factory specification and check field values 
        TensorFactorySpec   specTest = TensorFactorySpec.from();
        
        Assert.assertArrayEquals(arrShape, specTest.arrShape());
        Assert.assertEquals(specScalar, specTest.recScalarSpec());
        
        // Print out default configuration (tests TensorFactorySpec.toString())
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("Default Configuration:");
        System.out.println(specTest);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TensorFactorySpec#from(int[])}.
     */
    @Test
    public final void testFromIntArray() {
        
        // Test Parameters
        final int[]                 arrShape = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final ScalarFactorySpec     specScalar = REC_SCAL_SPEC_TENSOR_DEF;
        
        // Create tensor factory specification and check field values 
        TensorFactorySpec   specTest = TensorFactorySpec.from(arrShape);
        
        Assert.assertArrayEquals(arrShape, specTest.arrShape());
        Assert.assertEquals(specScalar, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TensorFactorySpec#from(int[], com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType)}.
     */
    @Test
    public final void testFromIntArrayJalScalarType() {
        
        // Test Parameters
        final int[]                 arrShape = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final JalScalarType         enmType = JalScalarType.BOOLEAN;
        final ScalarFactorySpec     specScalar = ScalarFactorySpec.from(enmType, BOL_ELEM_RAND_ENBL_DEF, LNG_ELEM_RAND_SEED_DEF);
        
        // Create tensor factory specification and check field values 
        TensorFactorySpec   specTest = TensorFactorySpec.from(arrShape, enmType);
        
        Assert.assertArrayEquals(arrShape, specTest.arrShape());
        Assert.assertEquals(specScalar, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TensorFactorySpec#from(int[], com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean)}.
     */
    @Test
    public final void testFromIntArrayJalScalarTypeBoolean() {
        
        // Test Parameters
        final int[]                 arrShape = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final JalScalarType         enmType = JalScalarType.BOOLEAN;
        final boolean               bolRandEnbl = true;
        final ScalarFactorySpec     specScalar = ScalarFactorySpec.from(enmType, bolRandEnbl, LNG_ELEM_RAND_SEED_DEF);
        
        // Create tensor factory specification and check field values 
        TensorFactorySpec   specTest = TensorFactorySpec.from(arrShape, enmType, bolRandEnbl);
        
        Assert.assertArrayEquals(arrShape, specTest.arrShape());
        Assert.assertEquals(specScalar, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TensorFactorySpec#from(int[], com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean, long)}.
     */
    @Test
    public final void testFromIntArrayJalScalarTypeBooleanLong() {
        
        // Test Parameters
        final int[]                 arrShape = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final JalScalarType         enmType = JalScalarType.BOOLEAN;
        final boolean               bolRandEnbl = true;
        final long                  lngSeed = 42;
        final ScalarFactorySpec     specScalar = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed);
        
        // Create tensor factory specification and check field values 
        TensorFactorySpec   specTest = TensorFactorySpec.from(arrShape, enmType, bolRandEnbl, lngSeed);
        
        Assert.assertArrayEquals(arrShape, specTest.arrShape());
        Assert.assertEquals(specScalar, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TensorFactorySpec#from(int[], com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean, long, java.lang.Number)}.
     */
    @Test
    public final void testFromIntArrayJalScalarTypeBooleanLongNumber() {
        
        // Test Parameters
        final int[]                 arrShape = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final JalScalarType         enmType = JalScalarType.INTEGER;
        final boolean               bolRandEnbl = false;
        final long                  lngSeed = 42;
        final Number                numIncr = Integer.valueOf(23);
        final ScalarFactorySpec     specScalar = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed, numIncr);
        
        // Create tensor factory specification and check field values 
        TensorFactorySpec   specTest = TensorFactorySpec.from(arrShape, enmType, bolRandEnbl, lngSeed, numIncr);
        
        Assert.assertArrayEquals(arrShape, specTest.arrShape());
        Assert.assertEquals(specScalar, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TensorFactorySpec#from(int[], com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean, long, java.lang.Number, java.lang.String)}.
     */
    @Test
    public final void testFromIntArrayJalScalarTypeBooleanLongNumberString() {
        
        // Test Parameters
        final int[]                 arrShape = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final JalScalarType         enmType = JalScalarType.INTEGER;
        final boolean               bolRandEnbl = false;
        final long                  lngSeed = 42;
        final Number                numIncr = Integer.valueOf(23);
        final String                strPref = "Happy String";
        final ScalarFactorySpec     specScalar = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed, numIncr, strPref);
        
        // Create tensor factory specification and check field values 
        TensorFactorySpec   specTest = TensorFactorySpec.from(arrShape, enmType, bolRandEnbl, lngSeed, numIncr, strPref);
        
        Assert.assertArrayEquals(arrShape, specTest.arrShape());
        Assert.assertEquals(specScalar, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TensorFactorySpec#from(int[], com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ScalarFactorySpec)}.
     */
    @Test
    public final void testFromIntArrayScalarFactorySpec() {
        
        // Test Parameters
        final int[]                 arrShape = { };
        final JalScalarType         enmType = JalScalarType.INTEGER;
        final boolean               bolRandEnbl = false;
        final long                  lngSeed = 42;
        final Number                numIncr = Integer.valueOf(23);
        final String                strPref = "Happy String";
        final ScalarFactorySpec     specScalar = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed, numIncr, strPref);
        
        // Create tensor factory specification and check field values 
        TensorFactorySpec   specTest = TensorFactorySpec.from(arrShape, specScalar);
        
        Assert.assertArrayEquals(arrShape, specTest.arrShape());
        Assert.assertEquals(specScalar, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TensorFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse0() {
        
        // Test Parameters
        final int[]                 arrShape = ARR_TENS_SHAPE_DEF;
        final String[]              arrArgs = { };
        final ScalarFactorySpec     specScalar = REC_SCAL_SPEC_TENSOR_DEF;
        
        try { 
            // Create tensor factory specification and check field values 
            TensorFactorySpec   specTest = TensorFactorySpec.parse(arrArgs);
            
            Assert.assertArrayEquals(arrShape, specTest.arrShape());
            Assert.assertEquals(specScalar, specTest.recScalarSpec());
            
        } catch (Exception e) {
            Assert.fail("Parsing failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TensorFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse1() {
        
        // Test Parameters
        final int[]                 arrShape = ARR_TENS_SHAPE_PARSE_1;
        final String[]              arrArgs = ARR_STR_ARGS_PARSE_1;
        final ScalarFactorySpec     specScalar = REC_SCAL_SPEC_PARSE_1;
        
        try { 
            // Create tensor factory specification and check field values 
            TensorFactorySpec   specTest = TensorFactorySpec.parse(arrArgs);
            
            Assert.assertArrayEquals(arrShape, specTest.arrShape());
            Assert.assertEquals(specScalar, specTest.recScalarSpec());
            
        } catch (Exception e) {
            Assert.fail("Parsing failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TensorFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse2() {
        
        // Test Parameters
        final int[]                 arrShape = ARR_TENS_SHAPE_PARSE_2;
        final String[]              arrArgs = ARR_STR_ARGS_PARSE_2;
        final ScalarFactorySpec     specScalar = REC_SCAL_SPEC_PARSE_2;
        
        try { 
            // Create tensor factory specification and check field values 
            TensorFactorySpec   specTest = TensorFactorySpec.parse(arrArgs);
            
            Assert.assertArrayEquals(arrShape, specTest.arrShape());
            Assert.assertEquals(specScalar, specTest.recScalarSpec());
            
        } catch (Exception e) {
            Assert.fail("Parsing failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TensorFactorySpec#newFactory()}.
     */
    @Test
    public final void testNewFactory() {
        
        // Test Parameters
        final int[]                 arrShape = { 1, 3, 4 };
        final JalScalarType         enmType = JalScalarType.INTEGER;
        final boolean               bolRandEnbl = false;
        final long                  lngSeed = 42;
        final Number                numIncr = Integer.valueOf(23);
        final String                strPref = "Happy String";
        final ScalarFactorySpec     specScalar = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed, numIncr, strPref);
        
        final JalComplexType        enmCmplxType = JalComplexType.ARRAY;
        final DpSupportedType       enmDpType = DpSupportedType.ARRAY;
        
        final int                   cntVals = 10;
        
        // Create tensor factory specification and check field values 
        TensorFactorySpec   specTest = TensorFactorySpec.from(arrShape, enmType, bolRandEnbl, lngSeed, numIncr, strPref);
        
        Assert.assertArrayEquals(arrShape, specTest.arrShape());
        Assert.assertEquals(specScalar, specTest.recScalarSpec());
        
        // Create tensor factory and check configuration
        TensorFactory   facTest = specTest.newFactory();
        
        Assert.assertArrayEquals(arrShape, facTest.getTensorShape());
        Assert.assertEquals(bolRandEnbl, facTest.isRandomValued());
        Assert.assertEquals(lngSeed, facTest.getSeed());
        
        Assert.assertEquals(enmType, facTest.getScalarType());
        Assert.assertEquals(enmCmplxType, facTest.getComplexType());
        Assert.assertEquals(enmDpType, facTest.getDatumType());
        
        // Creates some tensor values and inspect
        TensorIndexGenerator    genIndices = TensorIndexGenerator.from(arrShape);
        Integer                 intExpected = (int) lngSeed;
        for (int iVal=0; iVal<cntVals; iVal++) {
            Object  objVal = facTest.nextDatum();
            
            if (objVal instanceof List lstTensor) {
                for (Integer[] arrIndices : genIndices) {
                    Object  objElem = TensorUtility.extractElementAt(arrIndices, lstTensor);
                    
                    if (objElem instanceof Integer intElem) {
                        Assert.assertEquals(intExpected, intElem);
                        
                        intExpected += numIncr.intValue();
                        
                    } else
                        Assert.fail("Tensor element at index " + arrIndices + " was not an Integer: " + objElem);
                }
                
            } else
                Assert.fail("Tensor element " + objVal + " was not List.");
            
            genIndices.resetIndexCounter();
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TensorFactorySpec#equals(java.lang.Object)}.
     */
    @Test
    public final void testEquals() {
        
        // Test Parameters
        final ScalarFactorySpec     specScalFac = REC_SCAL_SPEC_TENSOR_DEF;
        final int[]                 arrShape = { 4, 3, 2, 1 };
        
        // Create structure factory specification and test configuration
        TensorFactorySpec   specTest = TensorFactorySpec.from(arrShape, specScalFac);
        
        Assert.assertArrayEquals(arrShape, specTest.arrShape());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
        
        // Create expected tensor factory specification and check equivalence
        TensorFactorySpec    specExpect = TensorFactorySpec.from(arrShape, specScalFac);
        
        Assert.assertTrue(specExpect.equals(specTest));
    }

}
