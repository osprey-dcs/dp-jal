/*
 * Project: dp-jal
 * File:	ByteArrayFactorySpecTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.specs
 * Type: 	ByteArrayFactorySpecTest
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
 * @since Dec 11, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ByteArrayFactory;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for record <code>ByteArrayFactorySpec</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 11, 2025
 *
 */
public class ByteArrayFactorySpecTest {

    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ByteArrayFactorySpec#from()}.
     */
    @Test
    public final void testFrom() {
        
        // Test Parameters
        final int   cntBytes = ByteArrayFactorySpec.INT_SIZE_DEF;
        
        // Create ByteArrayFactorySpec record and check field values
        ByteArrayFactorySpec    recSpec = ByteArrayFactorySpec.from();
        
        Assert.assertEquals(cntBytes, recSpec.szArrays());
        
        // Printout default configuration
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("Default Configuration:");
        System.out.println(recSpec);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ByteArrayFactorySpec#from(int)}.
     */
    @Test
    public final void testFromInt() {
        
        // Test Parameters
        final int   cntBytes = 1234;
        
        // Create ByteArrayFactorySpec record and check field values
        ByteArrayFactorySpec    recSpec = ByteArrayFactorySpec.from(cntBytes);
        
        Assert.assertEquals(cntBytes, recSpec.szArrays());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ByteArrayFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse() {
        
        // Test Parameters
        final String[]      arrArgs = { };
        
        final int           cntBytes = ByteArrayFactorySpec.INT_SIZE_DEF;

        // Create ByteArrayFactorySpec record and check field values
        ByteArrayFactorySpec    recSpec = ByteArrayFactorySpec.parse(arrArgs);
        
        Assert.assertEquals(cntBytes, recSpec.szArrays());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ByteArrayFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParseInt() {
        
        // Test Parameters
        final String        strSize = "42";
        final String[]      arrArgs = { strSize };
        
        final int           cntBytes = Integer.valueOf(strSize);

        // Create ByteArrayFactorySpec record and check field values
        ByteArrayFactorySpec    recSpec = ByteArrayFactorySpec.parse(arrArgs);
        
        Assert.assertEquals(cntBytes, recSpec.szArrays());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ByteArrayFactorySpec#newFactory()}.
     */
    @Test
    public final void testNewFactory() {
        
        // Test Parameters
        final int   cntBytes = ByteArrayFactorySpec.INT_SIZE_DEF;
        
        final JalComplexType    enmJalType = JalComplexType.BYTES;
        final DpSupportedType   enmDpType = DpSupportedType.BYTE_ARRAY;
        
        // Create ByteArrayFactorySpec record and check field values
        ByteArrayFactorySpec    recSpec = ByteArrayFactorySpec.from();
        
        Assert.assertEquals(cntBytes, recSpec.szArrays());
        
        // Create ByteArrayFactory and check configuration
        ByteArrayFactory    facTest = recSpec.newFactory();
        
        Assert.assertEquals(cntBytes, facTest.getArraySize());
        Assert.assertEquals(enmJalType, facTest.getComplexType());
        Assert.assertEquals(enmDpType, facTest.getDatumType());
    }

}
