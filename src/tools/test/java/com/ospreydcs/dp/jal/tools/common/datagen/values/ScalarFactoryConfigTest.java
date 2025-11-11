/*
 * Project: dp-jal
 * File:	ScalarFactoryConfigTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.values
 * Type: 	ScalarFactoryConfigTest
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
 * @since Nov 11, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.values;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.naming.ConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.JalToolsScalarValuesConfig;

/**
 * <p>
 * JUnit test cases for record <code>ScalarFactoryConfig</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 11, 2025
 *
 */
public class ScalarFactoryConfigTest {

    
    //
    // Library Resources
    //
    
    /** The default parameters for scalar-valued simulated data generation */
    private static final JalToolsScalarValuesConfig     CFG_DEF = JalToolsConfig.getInstance().datagen.values.scalar;
    
    public static final String  STR_YAML_DOC =
            "stringPref: str:       # the string prefix \n"
          + "type: INTEGER          # the scalar type \n"
          + "random: \n"
          + "   enabled: true       # enable/disable random number generation\n"
          + "   seed: 0             # seed value for random number generator (0 is random)\n"
          + "increment: \n"
          + "   start: 0            # seed (start) value for incremental number generation \n"
          + "   value: 0            # the increment value for incremental number generation\n";
    
    
    //
    // Constants - Default Argument Values
    //
    
    /** String value prefix */
    private final static String     STR_PREFIX_DEF = CFG_DEF.stringPrefix;

    
    /** The default enable/disable random number generator */
    public static final boolean BOL_RAND_ENBL_DEF = CFG_DEF.random.enabled;

    /** The default random number generator seed value */
    public static final long    LNG_RAND_SEED_DEF = CFG_DEF.random.seed;

    /** The default incremental seed value */
    public static final long    LNG_INCR_SEED_DEF = CFG_DEF.increment.seed;
    

    //
    // Constants - Scalar increment values
    //
    
    /** Integer value default increment */
    public final static Integer     INT_INCR_DEF = CFG_DEF.increment.integerv;
    
    /** Long value default increment */
    public final static Long        LNG_INCR_DEF = CFG_DEF.increment.longv;
    
    /** Float value default increment */
    public final static Float       FLT_INCR_DEF = CFG_DEF.increment.floatv;
    
    /** Double value default increment */
    public final static Double      DBL_INCR_DEF = CFG_DEF.increment.doublev;
    
    /** String value increment */
    public final static Integer     INT_STR_INCR_DEF = CFG_DEF.increment.stringv;

    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactoryConfig#parseArgs(java.lang.String[])}.
     */
    @Test
    public final void testParseArgs() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactoryConfig#parseYamlDoc(java.io.InputStream)}.
     */
    @Test
    public final void testParseYamlDoc() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactoryConfig#parseYamlNode(java.io.InputStream)}.
     */
    @Test
    public final void testParseYamlNode() {

        ByteArrayInputStream    is = new ByteArrayInputStream( STR_YAML_DOC.getBytes() );
        
        try {
            ScalarFactoryConfig recCfg = ScalarFactoryConfig.parseYamlNode(is);
            
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException | TypeNotPresentException
                | ConfigurationException | IOException e) {
            Assert.fail("Record creation failed to parse with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactoryConfig#from(com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType)}.
     */
    @Test
    public final void testFromJalScalarType() {
        
        // Test values
        final JalScalarType     enmType = JalScalarType.FLOAT;
        
        ScalarFactoryConfig recCfg = ScalarFactoryConfig.from(enmType);
        
        // Check record fields
        Assert.assertEquals(recCfg.enmValueType(), enmType);
        Assert.assertEquals(recCfg.bolRandEnable(), BOL_RAND_ENBL_DEF);
        if (BOL_RAND_ENBL_DEF)
            Assert.assertEquals(recCfg.seed(), LNG_RAND_SEED_DEF);
        else
            Assert.assertEquals(recCfg.seed(), LNG_INCR_SEED_DEF);
        Assert.assertEquals(recCfg.increment(), FLT_INCR_DEF);
        Assert.assertEquals(recCfg.strPrefix(), STR_PREFIX_DEF);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactoryConfig#from(com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, long)}.
     */
    @Test
    public final void testFromJalScalarTypeLong() {
        
        // Test values
        final JalScalarType     enmType = JalScalarType.INTEGER;
        final long              lngSeed = 0;
        
        ScalarFactoryConfig recCfg = ScalarFactoryConfig.from(enmType, lngSeed);
        
        // Check record fields
        Assert.assertEquals(recCfg.enmValueType(), enmType);
        Assert.assertEquals(recCfg.bolRandEnable(), BOL_RAND_ENBL_DEF);
        Assert.assertEquals(recCfg.seed(), lngSeed);
        Assert.assertEquals(recCfg.increment(), INT_INCR_DEF);
        Assert.assertEquals(recCfg.strPrefix(), STR_PREFIX_DEF);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactoryConfig#from(com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean)}.
     */
    @Test
    public final void testFromJalScalarTypeBoolean() {
        
        // Test values
        final JalScalarType     enmType = JalScalarType.STRING;
        final boolean           bolRand = false;
        
        ScalarFactoryConfig recCfg = ScalarFactoryConfig.from(enmType, bolRand);
        
        // Check record fields
        Assert.assertEquals(recCfg.enmValueType(), enmType);
        Assert.assertEquals(recCfg.bolRandEnable(), bolRand);
        Assert.assertEquals(recCfg.seed(), LNG_INCR_SEED_DEF);
        Assert.assertEquals(recCfg.increment(), INT_STR_INCR_DEF);
        Assert.assertEquals(recCfg.strPrefix(), STR_PREFIX_DEF);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactoryConfig#from(com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean, long)}.
     */
    @Test
    public final void testFromJalScalarTypeBooleanLong() {
        
        // Test values
        final JalScalarType     enmType = JalScalarType.DOUBLE;
        final boolean           bolRand = true;
        final long              lngSeed = 0;
        
        ScalarFactoryConfig recCfg = ScalarFactoryConfig.from(enmType, bolRand, lngSeed);
        
        // Check record fields
        Assert.assertEquals(recCfg.enmValueType(), enmType);
        Assert.assertEquals(recCfg.bolRandEnable(), bolRand);
        Assert.assertEquals(recCfg.seed(), lngSeed);
        Assert.assertEquals(recCfg.increment(), DBL_INCR_DEF);
        Assert.assertEquals(recCfg.strPrefix(), STR_PREFIX_DEF);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactoryConfig#from(com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean, long, java.lang.Number)}.
     */
    @Test
    public final void testFromJalScalarTypeBooleanLongNumber() {
        
        // Test values
        final JalScalarType     enmType = JalScalarType.BOOLEAN;
        final boolean           bolRand = true;
        final long              lngSeed = 0;
        final Number            numIncr = Integer.valueOf(1);
        
        ScalarFactoryConfig recCfg = ScalarFactoryConfig.from(enmType, bolRand, lngSeed, numIncr);
        
        // Check record fields
        Assert.assertEquals(recCfg.enmValueType(), enmType);
        Assert.assertEquals(recCfg.bolRandEnable(), bolRand);
        Assert.assertEquals(recCfg.seed(), lngSeed);
        Assert.assertEquals(recCfg.increment(), numIncr);
        Assert.assertEquals(recCfg.strPrefix(), STR_PREFIX_DEF);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactoryConfig#from(com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean, long, java.lang.Number, java.lang.String)}.
     */
    @Test
    public final void testFromJalScalarTypeBooleanLongNumberString() {
        
        // Test values
        final JalScalarType     enmType = JalScalarType.BOOLEAN;
        final boolean           bolRand = true;
        final long              lngSeed = 0;
        final Number            numIncr = Integer.valueOf(1);
        final String            strPref = "str:";
        
        ScalarFactoryConfig recCfg = ScalarFactoryConfig.from(enmType, bolRand, lngSeed, numIncr, strPref);
        
        // Check record fields
        Assert.assertEquals(recCfg.enmValueType(), enmType);
        Assert.assertEquals(recCfg.bolRandEnable(), bolRand);
        Assert.assertEquals(recCfg.seed(), lngSeed);
        Assert.assertEquals(recCfg.increment(), numIncr);
        Assert.assertEquals(recCfg.strPrefix(), strPref);
    }

}
