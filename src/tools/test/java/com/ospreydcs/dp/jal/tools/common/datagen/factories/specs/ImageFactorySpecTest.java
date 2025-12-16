/*
 * Project: dp-jal
 * File:	ImageFactorySpecTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.specs
 * Type: 	ImageFactorySpecTest
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

import com.ospreydcs.dp.jal.common.BufferedImage;
import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ImageFactory;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for record <code>ImageFactorySpec</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 11, 2025
 *
 */
public class ImageFactorySpecTest {
    
    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ImageFactorySpec#from()}.
     */
    @Test
    public final void testFrom() {
        
        // Test Parameters
        final int                   intSize = ImageFactorySpec.INT_SIZE_DEF;
        final BufferedImage.Format  enmFmt = ImageFactorySpec.ENM_FMT_DEF;
        final String                strPref = ImageFactorySpec.STR_PREF_DEF;
        
        // Create specification record and check field values
        ImageFactorySpec    recSpec = ImageFactorySpec.from();
        
        Assert.assertEquals(intSize, recSpec.intSize());
        Assert.assertEquals(enmFmt, recSpec.enmFormat());
        Assert.assertEquals(strPref, recSpec.strPrefix());
        
        // Print out default configuration
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("Default Configuration");
        System.out.println(recSpec);    // tests ImageFactorySpec.toString()
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ImageFactorySpec#from(int)}.
     */
    @Test
    public final void testFromInt() {
        
        // Test Parameters
        final int                   intSize = 42;
        final BufferedImage.Format  enmFmt = ImageFactorySpec.ENM_FMT_DEF;
        final String                strPref = ImageFactorySpec.STR_PREF_DEF;
        
        // Create specification record and check field values
        ImageFactorySpec    recSpec = ImageFactorySpec.from(intSize);
        
        Assert.assertEquals(intSize, recSpec.intSize());
        Assert.assertEquals(enmFmt, recSpec.enmFormat());
        Assert.assertEquals(strPref, recSpec.strPrefix());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ImageFactorySpec#from(int, com.ospreydcs.dp.jal.common.BufferedImage.Format)}.
     */
    @Test
    public final void testFromIntFormat() {
        
        // Test Parameters
        final int                   intSize = 42;
        final BufferedImage.Format  enmFmt = BufferedImage.Format.BMP;
        final String                strPref = ImageFactorySpec.STR_PREF_DEF;
        
        // Create specification record and check field values
        ImageFactorySpec    recSpec = ImageFactorySpec.from(intSize, enmFmt);
        
        Assert.assertEquals(intSize, recSpec.intSize());
        Assert.assertEquals(enmFmt, recSpec.enmFormat());
        Assert.assertEquals(strPref, recSpec.strPrefix());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ImageFactorySpec#from(int, com.ospreydcs.dp.jal.common.BufferedImage.Format, java.lang.String)}.
     */
    @Test
    public final void testFromIntFormatString() {
        
        // Test Parameters
        final int                   intSize = 42;
        final BufferedImage.Format  enmFmt = BufferedImage.Format.BMP;
        final String                strPref = "The Big Beautiful Image";
        
        // Create specification record and check field values
        ImageFactorySpec    recSpec = ImageFactorySpec.from(intSize, enmFmt, strPref);
        
        Assert.assertEquals(intSize, recSpec.intSize());
        Assert.assertEquals(enmFmt, recSpec.enmFormat());
        Assert.assertEquals(strPref, recSpec.strPrefix());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ImageFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse() {

        // Test Parameters
        final String[]  arrArgs = { };
        
        final int                   intSize = ImageFactorySpec.INT_SIZE_DEF;
        final BufferedImage.Format  enmFmt = ImageFactorySpec.ENM_FMT_DEF;
        final String                strPref = ImageFactorySpec.STR_PREF_DEF;
        
        // Create specification record and check field values
        ImageFactorySpec    recSpec = ImageFactorySpec.parse(arrArgs);
        
        Assert.assertEquals(intSize, recSpec.intSize());
        Assert.assertEquals(enmFmt, recSpec.enmFormat());
        Assert.assertEquals(strPref, recSpec.strPrefix());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ImageFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParseInt() {

        // Test Parameters
        final String    strSize = "23";
        
        final String[]  arrArgs = { strSize };
        
        final int                   intSize = Integer.valueOf(strSize);
        final BufferedImage.Format  enmFmt = ImageFactorySpec.ENM_FMT_DEF;
        final String                strPref = ImageFactorySpec.STR_PREF_DEF;
        
        // Create specification record and check field values
        ImageFactorySpec    recSpec = ImageFactorySpec.parse(arrArgs);
        
        Assert.assertEquals(intSize, recSpec.intSize());
        Assert.assertEquals(enmFmt, recSpec.enmFormat());
        Assert.assertEquals(strPref, recSpec.strPrefix());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ImageFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParseIntFormat() {

        // Test Parameters
        final String    strSize = "23";
        final String    strFmt = BufferedImage.Format.BMP.name();
        
        final String[]  arrArgs = { strSize, strFmt };
        
        final int                   intSize = Integer.valueOf(strSize);
        final BufferedImage.Format  enmFmt = BufferedImage.Format.getConstant(strFmt);
        final String                strPref = ImageFactorySpec.STR_PREF_DEF;
        
        // Create specification record and check field values
        ImageFactorySpec    recSpec = ImageFactorySpec.parse(arrArgs);
        
        Assert.assertEquals(intSize, recSpec.intSize());
        Assert.assertEquals(enmFmt, recSpec.enmFormat());
        Assert.assertEquals(strPref, recSpec.strPrefix());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ImageFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParseIntFormatPrefix() {

        // Test Parameters
        final String    strSize = "23";
        final String    strFmt = BufferedImage.Format.BMP.name();
        final String    strPref = "Big Beautiful Image";
        
        final String[]  arrArgs = { strSize, strFmt, strPref };
        
        final int                   intSize = Integer.valueOf(strSize);
        final BufferedImage.Format  enmFmt = BufferedImage.Format.getConstant(strFmt);
        
        // Create specification record and check field values
        ImageFactorySpec    recSpec = ImageFactorySpec.parse(arrArgs);
        
        Assert.assertEquals(intSize, recSpec.intSize());
        Assert.assertEquals(enmFmt, recSpec.enmFormat());
        Assert.assertEquals(strPref, recSpec.strPrefix());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ImageFactorySpec#newFactory()}.
     */
    @Test
    public final void testNewFactory() {
        
        // Test Parameters
        final int                   intSize = ImageFactorySpec.INT_SIZE_DEF;
        final BufferedImage.Format  enmFmt = ImageFactorySpec.ENM_FMT_DEF;
        final String                strPref = ImageFactorySpec.STR_PREF_DEF;
        
        final JalComplexType        enmJalType = JalComplexType.IMAGE;
        final DpSupportedType       enmDpType = DpSupportedType.IMAGE;
        
        // Create specification record and check field values
        ImageFactorySpec    recSpec = ImageFactorySpec.from();
        
        Assert.assertEquals(intSize, recSpec.intSize());
        Assert.assertEquals(enmFmt, recSpec.enmFormat());
        Assert.assertEquals(strPref, recSpec.strPrefix());

        // Create ImageFactory and check configuration
        ImageFactory    facTest = recSpec.newFactory();
        
        Assert.assertEquals(intSize, facTest.getSize());
        Assert.assertEquals(enmFmt, facTest.getFormat());
        Assert.assertEquals(strPref, facTest.getNamePrefix());
        Assert.assertEquals(enmJalType, facTest.getComplexType());
        Assert.assertEquals(enmDpType, facTest.getDatumType());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ImageFactorySpec#equals(java.lang.Object)}.
     */
    @Test
    public final void testEquals() {
        // Test Parameters
        final int                   intSize = ImageFactorySpec.INT_SIZE_DEF;
        final BufferedImage.Format  enmFmt = ImageFactorySpec.ENM_FMT_DEF;
        final String                strPref = ImageFactorySpec.STR_PREF_DEF;
        
        // Create target record and test record 
        ImageFactorySpec    recTarg = ImageFactorySpec.from();
        ImageFactorySpec    recTest = ImageFactorySpec.from(intSize, enmFmt, strPref);
        
        // Test equivalence
        Assert.assertTrue(recTarg.equals(recTest));
    }

}
