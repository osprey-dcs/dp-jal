/*
 * Project: dp-jal
 * File:	ImageFactoryTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.values
 * Type: 	ImageFactoryTest
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
 * @since Nov 15, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.values;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.common.BufferedImage;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.JalToolsImageValuesConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>ImageFactory</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 15, 2025
 *
 */
public class ImageFactoryTest {

    
    //
    // JAL Tools Resources
    //
    
    /** The default configuration parameters for simulated image value generation */
    public static final JalToolsImageValuesConfig   CFG_DEF = JalToolsConfig.getInstance().datagen.values.image;
    
    
    // 
    // Class Constants
    //
    
    /** The delimiter placed between image name prefixes and the image count */
    public static final String  STR_SEP = CFG_DEF.separator;
    
    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ImageFactory#from(int)}.
     */
    @Test
    public final void testFromInt() {
        
        // Test Parameters
        final int                   szAlloc = 10512;
        final BufferedImage.Format  enmFmt = CFG_DEF.format;
        final String                strPref = CFG_DEF.namePrefix;
        
        // Create factory and check configuration
        ImageFactory    facTest = ImageFactory.from(szAlloc);
        
        Assert.assertEquals(szAlloc, facTest.getSize());
        Assert.assertEquals(enmFmt, facTest.getFormat());
        Assert.assertEquals(strPref, facTest.getNamePrefix());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ImageFactory#from(int, com.ospreydcs.dp.jal.common.BufferedImage.Format)}.
     */
    @Test
    public final void testFromIntFormat() {
        
        // Test Parameters
        final int                   szAlloc = 10512;
        final BufferedImage.Format  enmFmt = BufferedImage.Format.BMP;
        final String                strPref = CFG_DEF.namePrefix;
        
        // Create factory and check configuration
        ImageFactory    facTest = ImageFactory.from(szAlloc, enmFmt);
        
        Assert.assertEquals(szAlloc, facTest.getSize());
        Assert.assertEquals(enmFmt, facTest.getFormat());
        Assert.assertEquals(strPref, facTest.getNamePrefix());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ImageFactory#from(int, com.ospreydcs.dp.jal.common.BufferedImage.Format, java.lang.String)}.
     */
    @Test
    public final void testFromIntFormatString() {
        
        // Test Parameters
        final int                   szAlloc = 10512;
        final BufferedImage.Format  enmFmt = BufferedImage.Format.BMP;
        final String                strPref = "UnitTest";
        
        // Create factory and check configuration
        ImageFactory    facTest = ImageFactory.from(szAlloc, enmFmt, strPref);
        
        Assert.assertEquals(szAlloc, facTest.getSize());
        Assert.assertEquals(enmFmt, facTest.getFormat());
        Assert.assertEquals(strPref, facTest.getNamePrefix());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ImageFactory#ImageFactory(int, com.ospreydcs.dp.jal.common.BufferedImage.Format, java.lang.String)}.
     */
    @Test
    public final void testImageFactory() {
        
        // Test Parameters
        final int                   szAlloc = 10512;
        final BufferedImage.Format  enmFmt = BufferedImage.Format.CUSTOM;
        final String                strPref = "Constructor";
        
        // Construct factory and check configuration
        ImageFactory    facTest = new ImageFactory(szAlloc, enmFmt, strPref);
        
        Assert.assertEquals(szAlloc, facTest.getSize());
        Assert.assertEquals(enmFmt, facTest.getFormat());
        Assert.assertEquals(strPref, facTest.getNamePrefix());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ImageFactory#nextValue()}.
     */
    @Test
    public final void testNextValueBmp() {
        
        // Test Parameters
        final int                   szAlloc = 10512;
        final BufferedImage.Format  enmFmt = BufferedImage.Format.BMP;
        final String                strPref = "UnitTest";
        final int                   cntImgs = 10;
        
        // Create factory and check configuration
        ImageFactory    facTest = ImageFactory.from(szAlloc, enmFmt, strPref);
        
        Assert.assertEquals(szAlloc, facTest.getSize());
        Assert.assertEquals(enmFmt, facTest.getFormat());
        Assert.assertEquals(strPref, facTest.getNamePrefix());
        
        // Create images and check properties
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        
        for (int iImg=0; iImg<cntImgs; iImg++) {
            Object  objVal = facTest.nextValue();
            
            if (objVal instanceof BufferedImage img) {
                String  strName = strPref + STR_SEP + Integer.toString(iImg);
                
                Assert.assertEquals(img.getName(), strName);
                Assert.assertEquals(enmFmt, img.getFormat());
                Assert.assertEquals(szAlloc, img.getSize());
                
                System.out.println("Image #" + iImg);
                img.printOutProperties(System.out, "  ");
                
            } else {
                Assert.fail("Object was not of type BufferedImage: " + objVal.getClass().getName());
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.values.ImageFactory#nextValue()}.
     */
    @Test
    public final void testNextValueAllFormats() {
        
        // Test Parameters
        final int       cntImgs = 10;
        final int       szBase = 512;
        
        
        // Create test factory for each format
        int iFmt = 0;
        for (BufferedImage.Format enmFmt : BufferedImage.Format.values()) {
            final String                strPref = "Test-" + enmFmt;
            final int                   szAlloc = szBase * iFmt++;
            
            // Create factory and check configuration
            ImageFactory    facTest = ImageFactory.from(szAlloc, enmFmt, strPref);

            Assert.assertEquals(szAlloc, facTest.getSize());
            Assert.assertEquals(enmFmt, facTest.getFormat());
            Assert.assertEquals(strPref, facTest.getNamePrefix());

            // Create images and check properties
            for (int iImg=0; iImg<cntImgs; iImg++) {
                Object  objVal = facTest.nextValue();

                if (objVal instanceof BufferedImage img) {
                    String  strName = strPref + STR_SEP + Integer.toString(iImg);

                    Assert.assertEquals(img.getName(), strName);
                    Assert.assertEquals(enmFmt, img.getFormat());
                    Assert.assertEquals(szAlloc, img.getSize());

                } else {
                    Assert.fail("Object was not of type BufferedImage: " + objVal.getClass().getName());
                }
            }
        }
    }

}
