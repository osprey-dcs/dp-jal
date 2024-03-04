/*
 * Project: dp-api-common
 * File:	CfgOverrideUtilityTest.java
 * Package: com.ospreydcs.dp.api.config.model
 * Type: 	CfgOverrideUtilityTest
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
 * @since Jan 2, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config.model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <p>
 * JUnit test cases for the <code>CfgOverrideUtility</code> class.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Jan 2, 2024
 *
 */
public class CfgOverrideUtilityTest {
    
    
    //
    // Class Constants
    //
    
    public static final String  DP_API_TESTSTRUCT_NAME = "Unit Test New Name";
    public static final String  DP_API_TESTSTRUCT_VERSION = "2";
    public static final String  DP_API_TESTSTRUCT_DATA_BOOLEAN = "false";
    public static final String  DP_API_TESTSTRUCT_DATA_INTEGER = "23";
    public static final String  DP_API_TESTSTRUCT_DATA_DOUBLE = "1.23";
    public static final String  DP_API_TESTSTRUCT_DATA_STRING = "Unit Test New data.str Value";
    public static final String  DP_API_TESTSTRUCT_DATA_STRUCT1_BOOLEAN = "false";
    public static final String  DP_API_TESTSTRUCT_DATA_STRUCT1_STRING = "Unit Test New data.sub1.str Value";
    public static final String  DP_API_TESTSTRUCT_DATA_STRUCT2_BOOLEAN = "false";
    public static final String  DP_API_TESTSTRUCT_DATA_STRUCT2_STRING = "Unit Test New data.sub2.str Value";
    
    
    // 
    // Class Resources
    //
    
    /** Structure class used for test cases */
    public static final TestCfgStructure STRUCT1 = TestCfgStructure.createStructure();
    
    /** Map of system variable (name, value) pairs used for configuration overrides */
    public static final Map<String, String> MAP_VAR_PAIRS = Map.of(
            "DP_API_TESTSTRUCT_NAME", DP_API_TESTSTRUCT_NAME,
            "DP_API_TESTSTRUCT_VERSION", DP_API_TESTSTRUCT_VERSION,
            "DP_API_TESTSTRUCT_DATA_BOOLEAN", DP_API_TESTSTRUCT_DATA_BOOLEAN,
            "DP_API_TESTSTRUCT_DATA_INTEGER", DP_API_TESTSTRUCT_DATA_INTEGER,
            "DP_API_TESTSTRUCT_DATA_DOUBLE", DP_API_TESTSTRUCT_DATA_DOUBLE,
            "DP_API_TESTSTRUCT_DATA_STRING", DP_API_TESTSTRUCT_DATA_STRING,
            "DP_API_TESTSTRUCT_DATA_STRUCT1_BOOLEAN", DP_API_TESTSTRUCT_DATA_STRUCT1_BOOLEAN,
            "DP_API_TESTSTRUCT_DATA_STRUCT1_STRING", DP_API_TESTSTRUCT_DATA_STRUCT1_STRING,
            "DP_API_TESTSTRUCT_DATA_STRUCT2_BOOLEAN", DP_API_TESTSTRUCT_DATA_STRUCT2_BOOLEAN,
            "DP_API_TESTSTRUCT_DATA_STRUCT2_STRING", DP_API_TESTSTRUCT_DATA_STRUCT2_STRING
            );

    
    //
    // Class Methods
    //
    
    /**
     * <p>
     * Set some system properties (name, values) pairs as override values.
     * </p>
     */
    public static final void setSystemProperties() {
        for (Map.Entry<String, String> pair : MAP_VAR_PAIRS.entrySet()) 
            System.setProperty(pair.getKey(), pair.getValue());
    }
    
    /**
     * Creates a new test configuration with parameters equal to those overridden
     * with appropriate system property values assigned from class constants.
     * 
     * @return new test configuration populated with system property values
     */
    public static final TestCfgStructure    createStructureWithSysPropOverrides() {
        TestCfgStructure    cfg = new TestCfgStructure();
        
        cfg.name = DP_API_TESTSTRUCT_NAME;
        cfg.version = Integer.parseInt(DP_API_TESTSTRUCT_VERSION);
        cfg.data.bol = Boolean.parseBoolean(DP_API_TESTSTRUCT_DATA_BOOLEAN);
        cfg.data.integer = Integer.parseInt(DP_API_TESTSTRUCT_DATA_INTEGER);
        cfg.data.dbl = Double.parseDouble(DP_API_TESTSTRUCT_DATA_DOUBLE);
        cfg.data.str = DP_API_TESTSTRUCT_DATA_STRING; 
        cfg.data.sub1.bol = Boolean.parseBoolean(DP_API_TESTSTRUCT_DATA_STRUCT1_BOOLEAN);
        cfg.data.sub1.str = DP_API_TESTSTRUCT_DATA_STRUCT1_STRING;
        cfg.data.sub2.bol = Boolean.parseBoolean(DP_API_TESTSTRUCT_DATA_STRUCT2_BOOLEAN);
        cfg.data.sub2.str = DP_API_TESTSTRUCT_DATA_STRUCT2_STRING;
        
        return cfg;
    }


    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        setSystemProperties();
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
     * Test method for {@link com.ospreydcs.dp.api.config.model.CfgOverrideUtility#overrideRoot(Object, com.ospreydcs.dp.api.config.model.CfgOverrideUtility.SOURCE)}.
     */
    @Test
    public final void testOverrideRootEnv() {
        try {
            boolean bolResult = CfgOverrideUtility.overrideRoot(STRUCT1, CfgOverrideUtility.SOURCE.ENVIRONMENT);
            
            Assert.assertFalse("At least one configuration parameter was overriden", bolResult);
            
        } catch (IllegalArgumentException e) {
            Assert.fail("IllegalArgumentException: " + e.getMessage());
            
        } catch (IllegalAccessException e) {
            Assert.fail("IllegalAccessException: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link CfgOverrideUtility#overrideRoot(Object, com.ospreydcs.dp.api.config.model.CfgOverrideUtility.SOURCE)}.
     */
    @Test
    public final void testOverrideRootProps() {
        TestCfgStructure    cfgTest = TestCfgStructure.createStructure();
        
        try {
            boolean bolResult = CfgOverrideUtility.overrideRoot(cfgTest, CfgOverrideUtility.SOURCE.PROPERTIES);
            
            Assert.assertTrue("No configuration parameters were overriden", bolResult);
            
        } catch (IllegalArgumentException e) {
            Assert.fail("IllegalArgumentException: " + e.getMessage());
            
        } catch (IllegalAccessException e) {
            Assert.fail("IllegalAccessException: " + e.getMessage());
            
        }
        
        TestCfgStructure    cfgCmp = CfgOverrideUtilityTest.createStructureWithSysPropOverrides();
        
        Assert.assertEquals(cfgCmp, cfgTest);
    }
    
    /**
     * Test method for {@link CfgOverrideUtility#overrideStruct(Object, String, CfgOverrideUtility.SOURCE)}.
     */
    @Test
    public final void testOverrideStructEnv() {
        try {
            boolean bolResult = CfgOverrideUtility.overrideStruct(STRUCT1.data, "DP_API_TESTSTRUCT_DATA", CfgOverrideUtility.SOURCE.ENVIRONMENT);
            
            Assert.assertFalse("At least one configuration parameter was overriden", bolResult);
            
        } catch (IllegalArgumentException e) {
            Assert.fail("IllegalArgumentException: " + e.getMessage());
            
        } catch (IllegalAccessException e) {
            Assert.fail("IllegalAccessException: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link CfgOverrideUtility#overrideStruct(Object, String, CfgOverrideUtility.SOURCE)}.
     */
    @Test
    public final void testOverrideStructProps() {
        TestCfgStructure    cfgTest = TestCfgStructure.createStructure();
        
        try {
            boolean bolResult = CfgOverrideUtility.overrideStruct(cfgTest.data.sub1, "DP_API_TESTSTRUCT_DATA_STRUCT1", CfgOverrideUtility.SOURCE.PROPERTIES);
            
            Assert.assertTrue("No configuration parameters were overriden", bolResult);
            
        } catch (IllegalArgumentException e) {
            Assert.fail("IllegalArgumentException: " + e.getMessage());
            
        } catch (IllegalAccessException e) {
            Assert.fail("IllegalAccessException: " + e.getMessage());
        }
        
        TestCfgStructure    cfgCmp = TestCfgStructure.createStructure();
        cfgCmp.data.sub1.bol = Boolean.parseBoolean(DP_API_TESTSTRUCT_DATA_STRUCT1_BOOLEAN);
        cfgCmp.data.sub1.str = DP_API_TESTSTRUCT_DATA_STRUCT1_STRING;
        
        Assert.assertEquals(cfgCmp, cfgTest);
    }
    
    /**
     * Test method for {@link  com.ospreydcs.dp.api.config.model.CfgOverrideUtility.overrideField(Field, Object, String, CfgOverrideUtility.SOURCE)}
     */
    @Test
    public final void testOverrideFieldProps() {
        TestCfgStructure    cfgTest = TestCfgStructure.createStructure();
        Field               fldTest;
        
        // Get the substructure field
        try {
            fldTest = cfgTest.data.getClass().getField("sub1");
            
        } catch (NoSuchFieldException | SecurityException e) {
            Assert.fail("Unable to recover Java Field for sub1: " + e.getMessage());
            return;
        } 
        
        try {
            boolean bolResult = CfgOverrideUtility.overrideField(fldTest, cfgTest.data, "DP_API_TESTSTRUCT_DATA_", CfgOverrideUtility.SOURCE.PROPERTIES);
            
            Assert.assertTrue("No configuration parameters were overriden", bolResult);
            
        } catch (IllegalArgumentException e) {
            Assert.fail("IllegalArgumentException: " + e.getMessage());
            
        } catch (IllegalAccessException e) {
            Assert.fail("IllegalAccessException: " + e.getMessage());
        }
        
        TestCfgStructure    cfgCmp = TestCfgStructure.createStructure();
        cfgCmp.data.sub1.bol = Boolean.parseBoolean(DP_API_TESTSTRUCT_DATA_STRUCT1_BOOLEAN);
        cfgCmp.data.sub1.str = DP_API_TESTSTRUCT_DATA_STRUCT1_STRING;
        
        Assert.assertEquals(cfgCmp, cfgTest);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.config.model.CfgOverrideUtility#extractOverrideables(java.lang.Object, CfgOverrideUtility.SOURCE)}.
     */
    @Test
    public final void testExtractOverridesEnv() {
        try {
            List<CfgOverrideRec> lstOverrides = CfgOverrideUtility.extractOverrideables(STRUCT1, CfgOverrideUtility.SOURCE.ENVIRONMENT);
            
            for (CfgOverrideRec rec : lstOverrides) {
                System.out.println(rec.printLine());
            }
            
        } catch (IllegalArgumentException e) {
            Assert.fail("IllegalArgumentException thrown: " + e.getMessage());
            
        } catch (IllegalAccessException e) {
            Assert.fail("IllegalAccessException thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.config.model.CfgOverrideUtility#extractOverrideables(java.lang.Object, CfgOverrideUtility.SOURCE)}.
     */
    @Test
    public final void testExtractOverridesProps() {
        try {
            List<CfgOverrideRec> lstOverrides = CfgOverrideUtility.extractOverrideables(STRUCT1, CfgOverrideUtility.SOURCE.PROPERTIES);
            
            for (CfgOverrideRec rec : lstOverrides) {
                System.out.println(rec.printLine());
            }
            
        } catch (IllegalArgumentException e) {
            Assert.fail("IllegalArgumentException thrown: " + e.getMessage());
            
        } catch (IllegalAccessException e) {
            Assert.fail("IllegalAccessException thrown: " + e.getMessage());
            
        }
    }

    
}
