/*
 * Project: dp-api-common
 * File:	CfgStructureTest.java
 * Package: com.ospreydcs.dp.api.config.model
 * Type: 	CfgStructureTest
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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * <p>
 * JUnit test cases for <code>CfgStructure</code> class.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 2, 2024
 *
 */
public class CfgStructureTest {

    //
    // Class Types
    //
    
    
    
    
    //
    // Class Resources
    //
    
    /** Structure class under test */
    public static final TestCfgStructure STRUCT1 = TestCfgStructure.createStructure();
    
    
    
    //
    // Fixture Setup
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
     * Test method for {@link com.ospreydcs.dp.api.config.model.CfgStructure#equals(java.lang.Object)}.
     */
    @Test
    public final void testEqualsObject() {
        TestCfgStructure   test = TestCfgStructure.createStructure();
        
        Assert.assertEquals(STRUCT1, test);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.config.model.CfgStructure#equals(java.lang.Object)}.
     */
    @Test
    public final void testEqualsNotObject() {
        TestCfgStructure   test = TestCfgStructure.createStructure();
        test.data.sub2.bol = !test.data.sub2.bol;
        
        Assert.assertNotEquals(test, STRUCT1);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.config.model.CfgStructure#toString()}.
     */
    @Test
    public final void testToString() {
        System.out.println( STRUCT1.toString() );
    }

    
}
