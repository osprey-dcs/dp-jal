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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;

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
    
    /** A <code>ScalarFactory</code> configuration for integer values */
    public static final ScalarFactoryConfig     REC_CFG_INT_1 = ScalarFactoryConfig.from(JalScalarType.INTEGER, false, 0, Integer.valueOf(2), "NotUsed");

    
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
    public final void testNextValueString() {
        
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
    public final void testNextValueInt() {
        
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

}
