/*
 * Project: dp-jal
 * File:	ByteArrayFactoryTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.values
 * Type: 	ByteArrayFactoryTest
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
 * @since Nov 23, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.values;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.grpc.v1.common.DataValue;
import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.grpc.util.ProtoMsg;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ByteArrayFactory;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>ByteArrayFactory</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 23, 2025
 *
 */
public class ByteArrayFactoryTest {

    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ByteArrayFactory#from(int)}.
     */
    @Test
    public final void testFromFail() {
        
        // Test Parameters
        final int               szArrays = -1;
        
        try {
            // Create factory and check configuration
            @SuppressWarnings("unused")
            ByteArrayFactory    facTest = ByteArrayFactory.from(szArrays);
            
            Assert.fail("ByteArrayFactory creation succeeded with illegal argument.");
            
        } catch (Exception e) {
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
            System.out.println("  Expected exception: " + e);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ByteArrayFactory#from(int)}.
     */
    @Test
    public final void testFrom() {
        
        // Test Parameters
        final int               szArrays = 1024;
        final DpSupportedType   enmType = DpSupportedType.BYTE_ARRAY;
        
        try {
            // Create factory and check configuration
            ByteArrayFactory    facTest = ByteArrayFactory.from(szArrays);
            
            Assert.assertEquals(szArrays, facTest.getArraySize());
            Assert.assertEquals(enmType, facTest.getDatumType());
            
        } catch (Exception e) {
            Assert.fail("ByteArrayFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ByteArrayFactory#ByteArrayFactory(int)}.
     */
    @Test
    public final void testByteArrayFactory() {
        
        // Test Parameters
        final int               szArrays = 2048;
        final DpSupportedType   enmType = DpSupportedType.BYTE_ARRAY;
        
        try {
            // Construction factory and check configuration
            ByteArrayFactory    facTest = ByteArrayFactory.from(szArrays);
            
            Assert.assertEquals(szArrays, facTest.getArraySize());
            Assert.assertEquals(enmType, facTest.getDatumType());
            
        } catch (Exception e) {
            Assert.fail("ByteArrayFactory construction failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ByteArrayFactory#getArraySize()}.
//     */
//    @Test
//    public final void testGetArraySize() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ByteArrayFactory#getValueType()}.
//     */
//    @Test
//    public final void testGetValueType() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ByteArrayFactory#nextDatum()}.
     */
    @Test
    public final void testNextValueMultiple() {
        
        // Test Parameters
        final int               szArrays = 1024;
        final DpSupportedType   enmType = DpSupportedType.BYTE_ARRAY;
        
        final int               cntArrays = 10;
        
        // Create factory and check configuration
        ByteArrayFactory    facTest;
        try {
            facTest = ByteArrayFactory.from(szArrays);
            
            Assert.assertEquals(szArrays, facTest.getArraySize());
            Assert.assertEquals(enmType, facTest.getDatumType());
            
        } catch (Exception e) {
            Assert.fail("ByteArrayFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
            return;
        }
        
        // Creates some bytes arrays and check size
        List<byte[]>    lstArrays = new ArrayList<>(cntArrays);
        
        for (int iArray=0; iArray<cntArrays; iArray++) {
            Object objVal = facTest.nextDatum();
            
            if (objVal instanceof byte[] arrVal) {
                Assert.assertEquals(szArrays, arrVal.length);
                
                lstArrays.add(arrVal);
            } else
                Assert.fail("Data value was not byte[].");
        }
        
        // Just for fun print out byte arrays
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        for (int iArray=0; iArray<cntArrays; iArray++) {
            System.out.println("  Array #" + iArray + " : " + lstArrays.get(iArray));
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ByteArrayFactory#nextDatum()}.
     */
    @Test
    public final void testNextValueToDataValue() {
        
        // Test Parameters
        final int               szArrays = 1024;
        final DpSupportedType   enmType = DpSupportedType.BYTE_ARRAY;
        
        final int               cntArrays = 10;
        
        // Create factory and check configuration
        ByteArrayFactory    facTest;
        try {
            facTest = ByteArrayFactory.from(szArrays);
            
            Assert.assertEquals(szArrays, facTest.getArraySize());
            Assert.assertEquals(enmType, facTest.getDatumType());
            
        } catch (Exception e) {
            Assert.fail("ByteArrayFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
            return;
        }
        
        // Creates some bytes arrays and check size and DataValue conversion
        for (int iArray=0; iArray<cntArrays; iArray++) {
            Object objVal = facTest.nextDatum();
            
            if (objVal instanceof byte[] arrVal) {
                Assert.assertEquals(szArrays, arrVal.length);
                
            } else
                Assert.fail("Data value was not byte[].");
            
            DataValue msgValue = ProtoMsg.createDataValue(objVal);
            Assert.assertTrue( msgValue.hasByteArrayValue() );
        }
    }        
}
