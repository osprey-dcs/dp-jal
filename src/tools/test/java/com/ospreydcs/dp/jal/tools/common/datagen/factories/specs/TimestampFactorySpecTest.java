/*
 * Project: dp-jal
 * File:	TimestampFactorySpecTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.specs
 * Type: 	TimestampFactorySpecTest
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

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for record <code>TimestampFactorySpec</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 11, 2025
 *
 */
public class TimestampFactorySpecTest {

    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec#from()}.
     */
    @Test
    public final void testFrom() {
        
        // Test Parameters
        final boolean   bolRand = TimestampFactorySpec.BOL_RND_ENBL_DEF;
        final long      lngSeed = TimestampFactorySpec.LNG_RND_SEED_DEF;
        final Duration  durPeriod = TimestampFactorySpec.DUR_INCR_PERIOD_DEF;
        final Instant   insStart = TimestampFactorySpec.INS_INCR_START_DEF;
        
        // Create TimestampFactorySpec and check field values
        TimestampFactorySpec    specTest = TimestampFactorySpec.from();
        
        Assert.assertEquals(bolRand, specTest.bolRand());
        Assert.assertEquals(lngSeed, specTest.lngSeed());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
        
        // Printout default configuration
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("Default Configuration:");
        System.out.println(specTest);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec#from(java.time.Duration)}.
     */
    @Test
    public final void testFromDuration() {
        
        // Test Parameters
        final boolean   bolRand = TimestampFactorySpec.BOL_RND_ENBL_DEF;
        final long      lngSeed = TimestampFactorySpec.LNG_RND_SEED_DEF;
        final Duration  durPeriod = Duration.of(100, ChronoUnit.NANOS);
        final Instant   insStart = TimestampFactorySpec.INS_INCR_START_DEF;
        
        // Create TimestampFactorySpec and check field values
        TimestampFactorySpec    specTest = TimestampFactorySpec.from(durPeriod);
        
        Assert.assertEquals(bolRand, specTest.bolRand());
        Assert.assertEquals(lngSeed, specTest.lngSeed());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec#from(java.time.Duration, java.time.Instant)}.
     */
    @Test
    public final void testFromDurationInstant() {
        
        // Test Parameters
        final boolean   bolRand = TimestampFactorySpec.BOL_RND_ENBL_DEF;
        final long      lngSeed = TimestampFactorySpec.LNG_RND_SEED_DEF;
        final Duration  durPeriod = Duration.ZERO;
        final Instant   insStart = Instant.MAX;
        
        // Create TimestampFactorySpec and check field values
        TimestampFactorySpec    specTest = TimestampFactorySpec.from(durPeriod, insStart);
        
        Assert.assertEquals(bolRand, specTest.bolRand());
        Assert.assertEquals(lngSeed, specTest.lngSeed());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec#from(boolean)}.
     */
    @Test
    public final void testFromBooleanFalse() {
        
        // Test Parameters
        final boolean   bolRand = false;
        final long      lngSeed = TimestampFactorySpec.LNG_RND_SEED_DEF;
        final Duration  durPeriod = Duration.ZERO;
        final Instant   insStart = Instant.EPOCH;
        
        // Create TimestampFactorySpec and check field values
        TimestampFactorySpec    specTest = TimestampFactorySpec.from(bolRand);
        
        Assert.assertEquals(bolRand, specTest.bolRand());
        Assert.assertEquals(lngSeed, specTest.lngSeed());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec#from(boolean)}.
     */
    @Test
    public final void testFromBooleanTrue() {
        
        // Test Parameters
        final boolean   bolRand = true;
        final long      lngSeed = TimestampFactorySpec.LNG_RND_SEED_DEF;
        final Duration  durPeriod = Duration.ZERO;
        final Instant   insStart = Instant.EPOCH;
        
        // Create TimestampFactorySpec and check field values
        TimestampFactorySpec    specTest = TimestampFactorySpec.from(bolRand);
        
        Assert.assertEquals(bolRand, specTest.bolRand());
        Assert.assertEquals(lngSeed, specTest.lngSeed());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec#from(boolean, long)}.
     */
    @Test
    public final void testFromBooleanLongFalse() {
        
        // Test Parameters
        final boolean   bolRand = false;
        final long      lngSeed = 2;
        final Duration  durPeriod = Duration.ZERO;
        final Instant   insStart = Instant.EPOCH;
        
        // Create TimestampFactorySpec and check field values
        TimestampFactorySpec    specTest = TimestampFactorySpec.from(bolRand, lngSeed);
        
        Assert.assertEquals(bolRand, specTest.bolRand());
        Assert.assertEquals(lngSeed, specTest.lngSeed());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec#from(boolean, long)}.
     */
    @Test
    public final void testFromBooleanLongTrue() {
        
        // Test Parameters
        final boolean   bolRand = true;
        final long      lngSeed = 2;
        final Duration  durPeriod = Duration.ZERO;
        final Instant   insStart = Instant.EPOCH;
        
        // Create TimestampFactorySpec and check field values
        TimestampFactorySpec    specTest = TimestampFactorySpec.from(bolRand, lngSeed);
        
        Assert.assertEquals(bolRand, specTest.bolRand());
        Assert.assertEquals(lngSeed, specTest.lngSeed());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParseFail() {
        
        // Test parameters
        final String[]      arrArgs = { };
        
        try {
            // Attempt TimestampFactorySpec creation 
            @SuppressWarnings("unused")
            TimestampFactorySpec    specTest = TimestampFactorySpec.parse(arrArgs);
            
            Assert.fail("TimestampFactorySpec#parse() did not throw an exception - requires at least one element.");
        
        } catch (Exception e) {
            
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
            System.out.println("  Zero element parsing correctly failed with exception " + e.getClass().getSimpleName() + " : " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParseRandom() {
        
        // Test parameters
        final String[]      arrArgs = { "true" };
        
        final boolean       bolRand = true;
        final long          lngSeed = TimestampFactorySpec.LNG_RND_SEED_DEF;
        final Duration      durPeriod = Duration.ZERO;
        final Instant       insStart = Instant.EPOCH;
        
        // Create TimestampFactorySpec and check field values
        TimestampFactorySpec    specTest = TimestampFactorySpec.parse(arrArgs);
        
        Assert.assertEquals(bolRand, specTest.bolRand());
        Assert.assertEquals(lngSeed, specTest.lngSeed());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParseRandomSeed() {
        
        // Test parameters
        final String        strSeed = "2";
        final String[]      arrArgs = { "true", strSeed };
        
        final boolean       bolRand = true;
        final long          lngSeed = Long.valueOf(strSeed);
        final Duration      durPeriod = Duration.ZERO;
        final Instant       insStart = Instant.EPOCH;
        
        // Create TimestampFactorySpec and check field values
        TimestampFactorySpec    specTest = TimestampFactorySpec.parse(arrArgs);
        
        Assert.assertEquals(bolRand, specTest.bolRand());
        Assert.assertEquals(lngSeed, specTest.lngSeed());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParseIncr() {
        
        // Test parameters
        final String[]      arrArgs = { "false" };
        
        final boolean       bolRand = false;
        final long          lngSeed = TimestampFactorySpec.LNG_RND_SEED_DEF;
        final Duration      durPeriod = TimestampFactorySpec.DUR_INCR_PERIOD_DEF;
        final Instant       insStart = TimestampFactorySpec.INS_INCR_START_DEF;
        
        // Create TimestampFactorySpec and check field values
        TimestampFactorySpec    specTest = TimestampFactorySpec.parse(arrArgs);
        
        Assert.assertEquals(bolRand, specTest.bolRand());
        Assert.assertEquals(lngSeed, specTest.lngSeed());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParseIncrPeriod() {
        
        // Test parameters
        final String        strPeriod = "PT0.001S";
        final String[]      arrArgs = { "false", strPeriod };
        
        final boolean       bolRand = false;
        final long          lngSeed = TimestampFactorySpec.LNG_RND_SEED_DEF;
        final Duration      durPeriod = Duration.parse(strPeriod);
        final Instant       insStart = TimestampFactorySpec.INS_INCR_START_DEF;
        
        // Create TimestampFactorySpec and check field values
        TimestampFactorySpec    specTest = TimestampFactorySpec.parse(arrArgs);
        
        Assert.assertEquals(bolRand, specTest.bolRand());
        Assert.assertEquals(lngSeed, specTest.lngSeed());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParseIncrPeriodStart() {
        
        // Test parameters
        final String        strPeriod = "PT0.001S";
        final String        strInstant = "2025-12-11T12:43:00Z";
        final String[]      arrArgs = { "false", strPeriod, strInstant };
        
        final boolean       bolRand = false;
        final long          lngSeed = TimestampFactorySpec.LNG_RND_SEED_DEF;
        final Duration      durPeriod = Duration.parse(strPeriod);
        final Instant       insStart = Instant.parse(strInstant);
        
        // Create TimestampFactorySpec and check field values
        TimestampFactorySpec    specTest = TimestampFactorySpec.parse(arrArgs);
        
        Assert.assertEquals(bolRand, specTest.bolRand());
        Assert.assertEquals(lngSeed, specTest.lngSeed());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec#newFactory()}.
     */
    @Test
    public final void testNewFactory() {
        
        // Test Parameters
        final boolean   bolRand = TimestampFactorySpec.BOL_RND_ENBL_DEF;
        final long      lngSeed = TimestampFactorySpec.LNG_RND_SEED_DEF;
        final Duration  durPeriod = TimestampFactorySpec.DUR_INCR_PERIOD_DEF;
        final Instant   insStart = TimestampFactorySpec.INS_INCR_START_DEF;
        
        final JalComplexType    enmJalType = JalComplexType.TIMESTAMP;
        final DpSupportedType   enmDpType = DpSupportedType.TIMESTAMP;
        
        // Create TimestampFactorySpec and check field values
        TimestampFactorySpec    specTest = TimestampFactorySpec.from();
        
        Assert.assertEquals(bolRand, specTest.bolRand());
        Assert.assertEquals(lngSeed, specTest.lngSeed());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
        
        // Create TimestampFactory and check configuration
        TimestampFactory    facTest = specTest.newFactory();
        
        Assert.assertEquals(bolRand, facTest.isRandom());
        Assert.assertEquals(lngSeed, facTest.getRandomSeedValue());
        Assert.assertEquals(durPeriod, facTest.getPeriod());
        Assert.assertEquals(insStart, facTest.getStartInstant());
        
        Assert.assertEquals(enmJalType, facTest.getComplexType());
        Assert.assertEquals(enmDpType, facTest.getDatumType());
    }

}
