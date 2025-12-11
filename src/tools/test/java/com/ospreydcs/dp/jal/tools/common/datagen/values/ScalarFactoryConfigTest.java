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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.naming.ConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.yaml.snakeyaml.error.YAMLException;

import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.values.JalToolsScalarValuesConfig;

/**
 * <p>
 * JUnit test cases for record <code>ScalarFactorySpec</code>.
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
    
    
    //
    // Constants - Default Argument Values
    //
    
    /** The default scalar value type */
    private final static JalScalarType  ENM_TYPE_DEF = CFG_DEF.type;
    
    /** The default string value prefix */
    private final static String         STR_PREFIX_DEF = CFG_DEF.stringPrefix;

    
    /** The default enable/disable random number generator */
    public static final boolean BOL_RAND_ENBL_DEF = CFG_DEF.random.enabled;

    /** The default random number generator seed value */
    public static final long    LNG_RAND_SEED_DEF = CFG_DEF.random.seed;

    /** The default incremental seed value */
    public static final long    LNG_INCR_SEED_DEF = CFG_DEF.increment.seed;
    

    //
    // Constants - Scalar increment values
    //
    
    /** Boolean value default increment value */
    public final static Integer     INT_BOL_INCR_DEF = CFG_DEF.increment.booleanv;
    
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
    // Constants - Test Resources
    //
    
    /** Command-line argument set used for application command-line argument parsing */
    public static final String[]    STR_CMD_ARGS_1 = {"LONG"};
    
    /** Command-line argument set used for application command-line argument parsing */
    public static final String[]    STR_CMD_ARGS_2 = {"DOUBLE", "false"};
    
    /** Command-line argument set used for application command-line argument parsing */
    public static final String[]    STR_CMD_ARGS_3 = {"FLOAT", "false", "0"};
    
    /** Command-line argument set used for application command-line argument parsing */
    public static final String[]    STR_CMD_ARGS_4 = {"INTEGER", "true", "0", "2"};
    
    /** Command-line argument set used for application command-line argument parsing */
    public static final String[]    STR_CMD_ARGS_5 = {"STRING", "true", "0", "2", "str_"};
    
    /** The <code>ScalarFactorySpec</code> record equivalent to <code>{@link #STR_CMD_ARGS_1}</code> */
    public static final ScalarFactorySpec REC_ARGS_1 = (BOL_RAND_ENBL_DEF) ? ScalarFactorySpec.from(JalScalarType.LONG, BOL_RAND_ENBL_DEF, LNG_RAND_SEED_DEF, LNG_INCR_DEF, STR_PREFIX_DEF)
                                                                             : ScalarFactorySpec.from(JalScalarType.LONG, BOL_RAND_ENBL_DEF, LNG_INCR_SEED_DEF, LNG_INCR_DEF, STR_PREFIX_DEF);
    
    /** The <code>ScalarFactorySpec</code> record equivalent to <code>{@link #STR_CMD_ARGS_2}</code> */
    public static final ScalarFactorySpec REC_ARGS_2 = ScalarFactorySpec.from(JalScalarType.DOUBLE, false, LNG_INCR_SEED_DEF, DBL_INCR_DEF, STR_PREFIX_DEF);
    
    /** The <code>ScalarFactorySpec</code> record equivalent to <code>{@link #STR_CMD_ARGS_3}</code> */
    public static final ScalarFactorySpec REC_ARGS_3 = ScalarFactorySpec.from(JalScalarType.FLOAT, false, 0, FLT_INCR_DEF, STR_PREFIX_DEF);
    
    /** The <code>ScalarFactorySpec</code> record equivalent to <code>{@link #STR_CMD_ARGS_4}</code> */
    public static final ScalarFactorySpec REC_ARGS_4 = ScalarFactorySpec.from(JalScalarType.INTEGER, true, 0, 2, STR_PREFIX_DEF);
    
    /** The <code>ScalarFactorySpec</code> record equivalent to <code>{@link #STR_CMD_ARGS_5}</code> */
    public static final ScalarFactorySpec REC_ARGS_5 = ScalarFactorySpec.from(JalScalarType.STRING, true, 0, 2, "str_");
    
    
    /** String equivalent to a YAML document - used for testing YAML configuration parsing */ 
    public static final String  STR_YAML_DOC =
             "# This is a comment line \n"
          +  "   # This is an indented comment line \n"
          +  "stringPrefix: \"str:\" # the string prefix \n"
          + "type: INTEGER          # the scalar type \n"
          + "random: \n"
          + "   enabled: true       # enable/disable random number generation\n"
          + "   seed: 0             # seed value for random number generator (0 is random)\n"
          + "increment: \n"
          + "   start: 0            # seed (start) value for incremental number generation \n"
          + "   value: 2            # the increment value for incremental number generation\n";
    
    /** The <code>ScalarFactorySpec</code> record equivalent to <code>{@link #STR_YAML_DOC}</code> */
    public static final ScalarFactorySpec REC_YML_CFG = ScalarFactorySpec.from(JalScalarType.INTEGER, true, 0, Integer.valueOf(2), "str:");
    
    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactorySpec#parseArgs(java.lang.String[])}.
     */
    @Test
    public final void testParseArgs1() {
        
        try {
            ScalarFactorySpec recCfg = ScalarFactorySpec.parseArgs(STR_CMD_ARGS_1);
        
            Assert.assertEquals(REC_ARGS_1, recCfg);
            
        } catch (Exception e) {
            Assert.fail("Record creation failed to parse with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactorySpec#parseArgs(java.lang.String[])}.
     */
    @Test
    public final void testParseArgs2() {
        
        try {
            ScalarFactorySpec recCfg = ScalarFactorySpec.parseArgs(STR_CMD_ARGS_2);
        
            Assert.assertEquals(REC_ARGS_2, recCfg);
            
        } catch (Exception e) {
            Assert.fail("Record creation failed to parse with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactorySpec#parseArgs(java.lang.String[])}.
     */
    @Test
    public final void testParseArgs3() {
        
        try {
            ScalarFactorySpec recCfg = ScalarFactorySpec.parseArgs(STR_CMD_ARGS_3);
        
            Assert.assertEquals(REC_ARGS_3, recCfg);
            
        } catch (Exception e) {
            Assert.fail("Record creation failed to parse with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactorySpec#parseArgs(java.lang.String[])}.
     */
    @Test
    public final void testParseArgs4() {
        
        try {
            ScalarFactorySpec recCfg = ScalarFactorySpec.parseArgs(STR_CMD_ARGS_4);
        
            Assert.assertEquals(REC_ARGS_4, recCfg);
            
        } catch (Exception e) {
            Assert.fail("Record creation failed to parse with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactorySpec#parseArgs(java.lang.String[])}.
     */
    @Test
    public final void testParseArgs5() {
        
        try {
            ScalarFactorySpec recCfg = ScalarFactorySpec.parseArgs(STR_CMD_ARGS_5);
        
            Assert.assertEquals(REC_ARGS_5, recCfg);
            
        } catch (Exception e) {
            Assert.fail("Record creation failed to parse with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactorySpec#parseYamlDoc(java.io.InputStream)}.
     */
    @Test
    public final void testParseYamlDoc() {

        ByteArrayInputStream    is = new ByteArrayInputStream( STR_YAML_DOC.getBytes() );
        
        try {
            ScalarFactorySpec recCfg = ScalarFactorySpec.parseYamlDoc(is);
            
            Assert.assertEquals(REC_YML_CFG, recCfg);
            
        } catch (YAMLException e) {
            Assert.fail("Record creation failed to parse with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactorySpec#parseYamlNode(java.io.InputStream)}.
     */
    @Test
    public final void testParseYamlNode() {

        ByteArrayInputStream    is = new ByteArrayInputStream( STR_YAML_DOC.getBytes() );
        
        try {
            ScalarFactorySpec recCfg = ScalarFactorySpec.parseYamlNode(is);
            
            Assert.assertEquals(REC_YML_CFG, recCfg);
            
        } catch (IndexOutOfBoundsException | NumberFormatException | TypeNotPresentException
                | ConfigurationException | IOException e) {
            Assert.fail("Record creation failed to parse with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactorySpec#from(com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType)}.
     */
    @Test
    public final void testFrom() {
        
        ScalarFactorySpec recCfg = ScalarFactorySpec.from();
        
        // Check record fields
        Assert.assertEquals(recCfg.enmType(), ENM_TYPE_DEF);
        Assert.assertEquals(recCfg.strPrefix(), STR_PREFIX_DEF);
        Assert.assertEquals(recCfg.bolRandEnbl(), BOL_RAND_ENBL_DEF);

        if (BOL_RAND_ENBL_DEF)
            Assert.assertEquals(recCfg.lngSeed(), LNG_RAND_SEED_DEF);
        else
            Assert.assertEquals(recCfg.lngSeed(), LNG_INCR_SEED_DEF);
        
        switch (ENM_TYPE_DEF) {
        case STRING:
            Assert.assertEquals(recCfg.numIncr(), INT_STR_INCR_DEF);
            break;
        case BOOLEAN:
            Assert.assertEquals(recCfg.numIncr(), INT_BOL_INCR_DEF);
            break;
        case DOUBLE:
            Assert.assertEquals(recCfg.numIncr(), DBL_INCR_DEF);
            break;
        case FLOAT:
            Assert.assertEquals(recCfg.numIncr(), FLT_INCR_DEF);
            break;
        case INTEGER:
            Assert.assertEquals(recCfg.numIncr(), INT_INCR_DEF);
            break;
        case LONG:
            Assert.assertEquals(recCfg.numIncr(), LNG_INCR_DEF);
            break;
        case UNSUPPORTED:
            Assert.fail("The default scalar value type is unsupported: " + ENM_TYPE_DEF);
            break;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactorySpec#from(com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType)}.
     */
    @Test
    public final void testFromJalScalarType() {
        
        // Test values
        final JalScalarType     enmType = JalScalarType.FLOAT;
        
        ScalarFactorySpec recCfg = ScalarFactorySpec.from(enmType);
        
        // Check record fields
        Assert.assertEquals(recCfg.enmType(), enmType);
        Assert.assertEquals(recCfg.bolRandEnbl(), BOL_RAND_ENBL_DEF);
        if (BOL_RAND_ENBL_DEF)
            Assert.assertEquals(recCfg.lngSeed(), LNG_RAND_SEED_DEF);
        else
            Assert.assertEquals(recCfg.lngSeed(), LNG_INCR_SEED_DEF);
        Assert.assertEquals(recCfg.numIncr(), FLT_INCR_DEF);
        Assert.assertEquals(recCfg.strPrefix(), STR_PREFIX_DEF);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactorySpec#from(com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, long)}.
     */
    @Test
    public final void testFromJalScalarTypeLong() {
        
        // Test values
        final JalScalarType     enmType = JalScalarType.INTEGER;
        final long              lngSeed = 0;
        
        ScalarFactorySpec recCfg = ScalarFactorySpec.from(enmType, lngSeed);
        
        // Check record fields
        Assert.assertEquals(recCfg.enmType(), enmType);
        Assert.assertEquals(recCfg.bolRandEnbl(), BOL_RAND_ENBL_DEF);
        Assert.assertEquals(recCfg.lngSeed(), lngSeed);
        Assert.assertEquals(recCfg.numIncr(), INT_INCR_DEF);
        Assert.assertEquals(recCfg.strPrefix(), STR_PREFIX_DEF);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactorySpec#from(com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean)}.
     */
    @Test
    public final void testFromJalScalarTypeBoolean() {
        
        // Test values
        final JalScalarType     enmType = JalScalarType.STRING;
        final boolean           bolRand = false;
        
        ScalarFactorySpec recCfg = ScalarFactorySpec.from(enmType, bolRand);
        
        // Check record fields
        Assert.assertEquals(recCfg.enmType(), enmType);
        Assert.assertEquals(recCfg.bolRandEnbl(), bolRand);
        Assert.assertEquals(recCfg.lngSeed(), LNG_INCR_SEED_DEF);
        Assert.assertEquals(recCfg.numIncr(), INT_STR_INCR_DEF);
        Assert.assertEquals(recCfg.strPrefix(), STR_PREFIX_DEF);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactorySpec#from(com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean, long)}.
     */
    @Test
    public final void testFromJalScalarTypeBooleanLong() {
        
        // Test values
        final JalScalarType     enmType = JalScalarType.DOUBLE;
        final boolean           bolRand = true;
        final long              lngSeed = 0;
        
        ScalarFactorySpec recCfg = ScalarFactorySpec.from(enmType, bolRand, lngSeed);
        
        // Check record fields
        Assert.assertEquals(recCfg.enmType(), enmType);
        Assert.assertEquals(recCfg.bolRandEnbl(), bolRand);
        Assert.assertEquals(recCfg.lngSeed(), lngSeed);
        Assert.assertEquals(recCfg.numIncr(), DBL_INCR_DEF);
        Assert.assertEquals(recCfg.strPrefix(), STR_PREFIX_DEF);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactorySpec#from(com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean, long, java.lang.Number)}.
     */
    @Test
    public final void testFromJalScalarTypeBooleanLongNumber() {
        
        // Test values
        final JalScalarType     enmType = JalScalarType.BOOLEAN;
        final boolean           bolRand = true;
        final long              lngSeed = 0;
        final Number            numIncr = Integer.valueOf(1);
        
        ScalarFactorySpec recCfg = ScalarFactorySpec.from(enmType, bolRand, lngSeed, numIncr);
        
        // Check record fields
        Assert.assertEquals(recCfg.enmType(), enmType);
        Assert.assertEquals(recCfg.bolRandEnbl(), bolRand);
        Assert.assertEquals(recCfg.lngSeed(), lngSeed);
        Assert.assertEquals(recCfg.numIncr(), numIncr);
        Assert.assertEquals(recCfg.strPrefix(), STR_PREFIX_DEF);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarFactorySpec#from(com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean, long, java.lang.Number, java.lang.String)}.
     */
    @Test
    public final void testFromJalScalarTypeBooleanLongNumberString() {
        
        // Test values
        final JalScalarType     enmType = JalScalarType.BOOLEAN;
        final boolean           bolRand = true;
        final long              lngSeed = 0;
        final Number            numIncr = Integer.valueOf(1);
        final String            strPref = "str:";
        
        ScalarFactorySpec recCfg = ScalarFactorySpec.from(enmType, bolRand, lngSeed, numIncr, strPref);
        
        // Check record fields
        Assert.assertEquals(recCfg.enmType(), enmType);
        Assert.assertEquals(recCfg.bolRandEnbl(), bolRand);
        Assert.assertEquals(recCfg.lngSeed(), lngSeed);
        Assert.assertEquals(recCfg.numIncr(), numIncr);
        Assert.assertEquals(recCfg.strPrefix(), strPref);
    }

}
