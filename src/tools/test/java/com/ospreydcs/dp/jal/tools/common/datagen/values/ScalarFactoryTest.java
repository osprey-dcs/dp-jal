/*
 * Project: dp-jal
 * File:	ScalarFactoryTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.values
 * Type: 	ScalarFactoryTest
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
 * @since Nov 12, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.values;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>ScalarFactory</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 12, 2025
 *
 */
public class ScalarFactoryTest {

    
    //
    // Test Resources
    //
    
    /** The string prefix used for string value generation */
    public static final String                  STR_PREFIX = "str:";
    
    
    /** A <code>ScalarFactory</code> configuration for string values */
    public static final ScalarFactoryConfig     REC_CFG_STR_1 = ScalarFactoryConfig.from(JalScalarType.STRING, false, 0, Integer.valueOf(1), STR_PREFIX);
    
    /** A <code>ScalarFactory</code> configuration for boolean values */
    public static final ScalarFactoryConfig     REC_CFG_BOL_1 = ScalarFactoryConfig.from(JalScalarType.BOOLEAN, false, 0, Integer.valueOf(1), "NotUsed");

    /** A <code>ScalarFactory</code> configuration for boolean values */
    public static final ScalarFactoryConfig     REC_CFG_BOL_2 = ScalarFactoryConfig.from(JalScalarType.BOOLEAN, false, 1, Integer.valueOf(0), "NotUsed");

    /** A <code>ScalarFactory</code> configuration for integer values */
    public static final ScalarFactoryConfig     REC_CFG_INT_1 = ScalarFactoryConfig.from(JalScalarType.INTEGER, false, 0, Integer.valueOf(2), "NotUsed");

    /** A <code>ScalarFactory</code> configuration for integer values */
    public static final ScalarFactoryConfig     REC_CFG_INT_2 = ScalarFactoryConfig.from(JalScalarType.INTEGER, false, 100, Integer.valueOf(23));

    /** A <code>ScalarFactory</code> configuration for long values */
    public static final ScalarFactoryConfig     REC_CFG_LNG_1 = ScalarFactoryConfig.from(JalScalarType.LONG, false, 1_000_000_000, Long.valueOf(5_000_000));

    /** A <code>ScalarFactory</code> configuration for float values */
    public static final ScalarFactoryConfig     REC_CFG_FLT_1 = ScalarFactoryConfig.from(JalScalarType.FLOAT, false, 0, Float.valueOf(0.025f));

    /** A <code>ScalarFactory</code> configuration for double values */
    public static final ScalarFactoryConfig     REC_CFG_DBL_1 = ScalarFactoryConfig.from(JalScalarType.DOUBLE, false, 0, Double.valueOf(1.602e-19));

    /** A <code>ScalarFactory</code> configuration for random double values */
    public static final ScalarFactoryConfig     REC_CFG_DBL_RND = ScalarFactoryConfig.from(JalScalarType.DOUBLE, true, 0);

    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory#from(com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactoryConfig)}.
     */
    @Test
    public final void testFrom() {
        
        ScalarFactory   facTest = ScalarFactory.from(REC_CFG_INT_1);
        
        Assert.assertEquals(REC_CFG_INT_1, facTest.getConfiguration());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory#ScalarFactory(com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactoryConfig)}.
     */
    @Test
    public final void testScalarFactory() {
        
        ScalarFactory   facTest = new ScalarFactory(REC_CFG_STR_1);

        Assert.assertEquals(REC_CFG_STR_1, facTest.getConfiguration());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory#nextValue()}.
     */
    @Test
    public final void testNextValueBoolean1() {
        
        // Test Parameters
        final ScalarFactoryConfig     recCfg = REC_CFG_BOL_1;
        
        final long    lngSeed = recCfg.seed();
        final boolean bolSeed = (lngSeed % 2) == 0 ? false : true;
        final boolean bolIncr = (recCfg.increment().intValue() % 2) == 0 ? false : true;
        final int     cntVals = 10;
        
        ScalarFactory   facTest = ScalarFactory.from(recCfg);
        
        Boolean bolVal = bolSeed;
        for (int iVal=0; iVal<cntVals; iVal++) {
            Object  objVal = facTest.nextValue();
            
            if (objVal instanceof Boolean bolNext) {
                Assert.assertEquals(bolVal, bolNext);
                
            } else
                Assert.fail("Next value bad type " + objVal.getClass().getName());
            
            bolVal = (bolIncr) ? !bolVal : bolVal;
        }
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory#nextValue()}.
     */
    @Test
    public final void testNextValueBoolean2() {
        
        // Test Parameters
        final ScalarFactoryConfig     recCfg = REC_CFG_BOL_2;
        
        final long    lngSeed = recCfg.seed();
        final boolean bolSeed = (lngSeed % 2) == 0 ? false : true;
        final boolean bolIncr = (recCfg.increment().intValue() % 2) == 0 ? false : true;
        final int     cntVals = 10;
        
        ScalarFactory   facTest = ScalarFactory.from(recCfg);
        
        Boolean bolVal = bolSeed;
        for (int iVal=0; iVal<cntVals; iVal++) {
            Object  objVal = facTest.nextValue();
            
            if (objVal instanceof Boolean bolNext) {
                Assert.assertEquals(bolVal, bolNext);
                
            } else
                Assert.fail("Next value bad type " + objVal.getClass().getName());
            
            bolVal = (bolIncr) ? !bolVal : bolVal;
        }
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory#nextValue()}.
     */
    @Test
    public final void testNextValueString1() {
        
        // Test Parameters
        final ScalarFactoryConfig     recCfg = REC_CFG_STR_1;
        
        final long    lngSeed = recCfg.seed();
        final int     intIncr = recCfg.increment().intValue();
        final int     cntVals = 10;
        
        ScalarFactory   facTest = ScalarFactory.from(recCfg);
        
        Integer     intSuff = Math.toIntExact(lngSeed);
        for (int iVal=0; iVal<cntVals; iVal++) {
            Object  objVal = facTest.nextValue();
            
            if (objVal instanceof String strNext) {
                String  strVal = STR_PREFIX + intSuff;
                
                Assert.assertEquals(strVal, strNext);
                
            } else
                Assert.fail("Next value bad type " + objVal.getClass().getName());
            
            intSuff += intIncr;
        }
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory#nextValue()}.
     */
    @Test
    public final void testNextValueInteger1() {
        
        // Test Parameters
        final ScalarFactoryConfig     recCfg = REC_CFG_INT_1;
        
        final long    lngSeed = recCfg.seed();
        final int     intIncr = recCfg.increment().intValue();
        final int     cntVals = 10;
        
        ScalarFactory   facTest = ScalarFactory.from(recCfg);
        
        Integer     intVal = Math.toIntExact(lngSeed);
        for (int iVal=0; iVal<cntVals; iVal++) {
            Object  objVal = facTest.nextValue();
            
            if (objVal instanceof Integer intNext) 
                Assert.assertEquals(intVal, intNext);
            else
                Assert.fail("Next value bad type " + objVal.getClass().getName());
            
            intVal += intIncr;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory#nextValue()}.
     */
    @Test
    public final void testNextValueInteger2() {
        
        // Test Parameters
        final ScalarFactoryConfig     recCfg = REC_CFG_INT_2;
        
        final long    lngSeed = recCfg.seed();
        final int     intIncr = recCfg.increment().intValue();
        final int     cntVals = 10;
        
        ScalarFactory   facTest = ScalarFactory.from(recCfg);
        
        Integer     intVal = Math.toIntExact(lngSeed);
        for (int iVal=0; iVal<cntVals; iVal++) {
            Object  objVal = facTest.nextValue();
            
            if (objVal instanceof Integer intNext) 
                Assert.assertEquals(intVal, intNext);
            else
                Assert.fail("Next value bad type " + objVal.getClass().getName());
            
            intVal += intIncr;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory#nextValue()}.
     */
    @Test
    public final void testNextValueLong1() {
        
        // Test Parameters
        final ScalarFactoryConfig     recCfg = REC_CFG_LNG_1;
        
        final long    lngSeed = recCfg.seed();
        final long    lngIncr = recCfg.increment().longValue();
        final int     cntVals = 10;
        
        ScalarFactory   facTest = ScalarFactory.from(recCfg);
        
        Long    lngVal = lngSeed;
        for (int iVal=0; iVal<cntVals; iVal++) {
            Object  objVal = facTest.nextValue();
            
            if (objVal instanceof Long lngNext) 
                Assert.assertEquals(lngVal, lngNext);
            else
                Assert.fail("Next value bad type " + objVal.getClass().getName());
            
            lngVal += lngIncr;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory#nextValue()}.
     */
    @Test
    public final void testNextValueFloat1() {
        
        // Test Parameters
        final ScalarFactoryConfig     recCfg = REC_CFG_FLT_1;
        
        final long    lngSeed = recCfg.seed();
        final float   fltIncr = recCfg.increment().floatValue();
        final int     cntVals = 10;
        
        ScalarFactory   facTest = ScalarFactory.from(recCfg);
        
        Float   fltVal = (float) lngSeed;
        for (int iVal=0; iVal<cntVals; iVal++) {
            Object  objVal = facTest.nextValue();
            
            if (objVal instanceof Float fltNext) 
                Assert.assertEquals(fltVal, fltNext);
            else
                Assert.fail("Next value bad type " + objVal.getClass().getName());
            
            fltVal += fltIncr;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory#nextValue()}.
     */
    @Test
    public final void testNextValueDouble1() {
        
        // Test Parameters
        final ScalarFactoryConfig     recCfg = REC_CFG_DBL_1;
        
        final long    lngSeed = recCfg.seed();
        final double  dblIncr = recCfg.increment().doubleValue();
        final int     cntVals = 10;
        
        ScalarFactory   facTest = ScalarFactory.from(recCfg);
        
        Double  dblVal = (double) lngSeed;
        for (int iVal=0; iVal<cntVals; iVal++) {
            Object  objVal = facTest.nextValue();
            
            if (objVal instanceof Double dblNext) 
                Assert.assertEquals(dblVal, dblNext);
            else
                Assert.fail("Next value bad type " + objVal.getClass().getName());
            
            dblVal += dblIncr;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactory#nextValue()}.
     */
    @Test
    public final void testNextValueDoubleRand() {
        
        // Test Parameters
        final ScalarFactoryConfig     recCfg = REC_CFG_DBL_RND;
        
        final int     cntVals = 10;
        
        ScalarFactory   facTest = ScalarFactory.from(recCfg);
        
        // Test start value
        Double  dblStart = null;
        Object  objStart = facTest.nextValue();
        if (objStart instanceof Double dblVal)
            dblStart = dblVal;
        else
            Assert.fail("Start value bad type " + objStart.getClass().getName());

        // Generate sequence of random values
        List<Object>    lstVals = IntStream.range(0, cntVals).mapToObj(i -> facTest.nextValue()).toList();
        
        // Test types of all generated double values
        boolean         bolTypes = lstVals.stream().allMatch(obj -> (obj instanceof Double));
        Assert.assertTrue("Not all randomly generated values were of type 'Double'", bolTypes);
        
        // Test range of all generated double values
        boolean         bolRange = lstVals.stream().<Double>map(obj -> (Double)obj).allMatch(d -> (0.0 <= d) && (d<= 1.0));
        Assert.assertTrue("Not all randomly generated values were in [0,1]", bolRange);
        
        // Print out the random values just for fun
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + ": " + cntVals + " random double values after start value " + dblStart);
        System.out.println("  " + lstVals);
    }

}
