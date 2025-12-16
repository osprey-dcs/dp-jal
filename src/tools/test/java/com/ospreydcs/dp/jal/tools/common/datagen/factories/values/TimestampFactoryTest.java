/*
 * Project: dp-jal
 * File:	TimestampFactoryTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.values
 * Type: 	TimestampFactoryTest
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
 * @since Nov 24, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.values;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.values.JalToolsTmsFactoryConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>TimestampFactory</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 24, 2025
 *
 */
public class TimestampFactoryTest {

    
    //
    // JAL Tools Resources
    //
    
    /** The default configuration parameters for simulated timestamp value generation */
    private static final JalToolsTmsFactoryConfig    CFG_DEF = JalToolsConfig.getInstance().datagen.values.timestamp;
    
    
    //
    // Class Constant
    //
    
    /** The units for randomly generated long values added to <code>{@link Instant#EPOCH}</code> for random timestamp values*/
    public static final ChronoUnit  CU_RND_EPOCH_ADD = ChronoUnit.NANOS;

    
    /** Default random enable/disable timestamp generation flag */
    public static final boolean     BOL_RND_ENBL_DEF = CFG_DEF.random.enabled;
    
    /** Default random generator seed value - use '0' for a random seed value */
    public static final long        LNG_RND_SEED_DEF = CFG_DEF.random.seed;
    
    
    /** The default start time instant for incremental timestamp generation */
    public static final Instant     INS_INCR_START_DEF = CFG_DEF.increment.startInstant();
    
    /** The default timestamp period for incremental timestamp generation */
    public static final Duration    DUR_INCR_PERIOD_DEF = CFG_DEF.increment.periodDuration();

    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#from()}.
     */
    @Test
    public final void testFrom() {
        
        // Test Parameters
        final boolean   bolRandom = BOL_RND_ENBL_DEF;
        final long      lngSeed = LNG_RND_SEED_DEF;
        final Duration  durPeriod = DUR_INCR_PERIOD_DEF;
        final Instant   insStart = INS_INCR_START_DEF;
        
        // Create test factory with all default parameters
        TimestampFactory    facTest = TimestampFactory.from();
        
        // Check configuration depending upon the default random value
        if (bolRandom) {
            Assert.assertEquals(bolRandom, facTest.isRandom());
            Assert.assertEquals(lngSeed, facTest.getRandomSeedValue());
            
        } else {
            
            Assert.assertEquals(durPeriod, facTest.getPeriod());
            Assert.assertEquals(insStart, facTest.getStartInstant());
        }
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#from(long)}.
     */
    @Test
    public final void testFromLong() {
        
        // Test Parameters
        final boolean   bolRandom = true;
        final long      lngSeed = 4;
        
        // Create test factory and check configuration
        TimestampFactory    facTest = TimestampFactory.from(lngSeed);
        
        Assert.assertEquals(bolRandom, facTest.isRandom());
        Assert.assertEquals(lngSeed, facTest.getRandomSeedValue());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#from(boolean)}.
     */
    @Test
    public final void testFromBoolean() {
        
        // Test Parameters
        final boolean   bolRandom = false;
        final long      lngSeed = LNG_RND_SEED_DEF;
        
        // Create test factory and check configuration
        TimestampFactory    facTest = TimestampFactory.from(bolRandom);
        
        Assert.assertEquals(bolRandom, facTest.isRandom());
        Assert.assertEquals(lngSeed, facTest.getRandomSeedValue());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#from(boolean, long)}.
     */
    @Test
    public final void testFromBooleanLong() {
        
        // Test Parameters
        final boolean   bolRandom = true;
        final long      lngSeed = 0;
        
        // Create test factory and check configuration
        TimestampFactory    facTest = TimestampFactory.from(bolRandom, lngSeed);
        
        Assert.assertEquals(bolRandom, facTest.isRandom());
        Assert.assertEquals(lngSeed, facTest.getRandomSeedValue());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#from(java.time.Instant)}.
     */
    @Test
    public final void testFromInstant() {
        
        // Test Parameters
        final Duration          durPeriod = DUR_INCR_PERIOD_DEF;
        final Instant           insStart = Instant.now();
        final DpSupportedType   enmType = DpSupportedType.TIMESTAMP;
        
        // Create test factory and check configuration
        TimestampFactory    facTest = TimestampFactory.from(insStart);
        
        Assert.assertEquals(durPeriod, facTest.getPeriod());
        Assert.assertEquals(insStart, facTest.getStartInstant());
        Assert.assertEquals(enmType, facTest.getDatumType());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#from(java.time.Duration)}.
     */
    @Test
    public final void testFromDuration() {
        
        // Test Parameters
        final Duration          durPeriod = Duration.ofMinutes(2);
        final Instant           insStart = INS_INCR_START_DEF;
        final DpSupportedType   enmType = DpSupportedType.TIMESTAMP;
        
        // Create test factory and check configuration
        TimestampFactory    facTest = TimestampFactory.from(durPeriod);
        
        Assert.assertEquals(durPeriod, facTest.getPeriod());
        Assert.assertEquals(insStart, facTest.getStartInstant());
        Assert.assertEquals(enmType, facTest.getDatumType());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#from(java.time.Duration, java.time.Instant)}.
     */
    @Test
    public final void testFromDurationInstant() {
        
        // Test Parameters
        final Duration          durPeriod = Duration.ofMinutes(2);
        final Instant           insStart = Instant.now();
        final DpSupportedType   enmType = DpSupportedType.TIMESTAMP;
        
        // Create test factory and check configuration
        TimestampFactory    facTest = TimestampFactory.from(durPeriod, insStart);
        
        Assert.assertEquals(durPeriod, facTest.getPeriod());
        Assert.assertEquals(insStart, facTest.getStartInstant());
        Assert.assertEquals(enmType, facTest.getDatumType());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#TimestampFactory(boolean, long)}.
     */
    @Test
    public final void testTimestampFactoryBooleanLong() {
        
        // Test Parameters
        final boolean   bolRandom = true;
        final long      lngSeed = 0;
        
        // Create test factory and check configuration
        TimestampFactory    facTest = new TimestampFactory(bolRandom, lngSeed);
        
        Assert.assertEquals(bolRandom, facTest.isRandom());
        Assert.assertEquals(lngSeed, facTest.getRandomSeedValue());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#TimestampFactory(java.time.Duration, java.time.Instant)}.
     */
    @Test
    public final void testTimestampFactoryDurationInstant() {
        
        // Test Parameters
        final Duration          durPeriod = Duration.ofMinutes(2);
        final Instant           insStart = Instant.now();
        final DpSupportedType   enmType = DpSupportedType.TIMESTAMP;
        
        // Create test factory and check configuration
        TimestampFactory    facTest = new TimestampFactory(durPeriod, insStart);
        
        Assert.assertEquals(durPeriod, facTest.getPeriod());
        Assert.assertEquals(insStart, facTest.getStartInstant());
        Assert.assertEquals(enmType, facTest.getDatumType());
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#isRandom()}.
//     */
//    @Test
//    public final void testIsRandom() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#getRandomSeedValue()}.
//     */
//    @Test
//    public final void testGetRandomSeedValue() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#getStartInstant()}.
//     */
//    @Test
//    public final void testGetStartInstant() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#getPeriod()}.
//     */
//    @Test
//    public final void testGetPeriod() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#getDatumType()}.
     */
    @Test
    public final void testGetValueType() {
        
        // Test Parameters
        final DpSupportedType   enmType = DpSupportedType.TIMESTAMP;
        
        // Create test factory and check value type
        TimestampFactory    facTest = TimestampFactory.from();
        
        Assert.assertEquals(enmType, facTest.getDatumType());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#nextDatum()}.
     */
    @Test
    public final void testNextValueRandom() {
        
        // Test Parameters
        final boolean           bolRandom = true;
        final long              lngSeed = 0;
        
        final DpSupportedType   enmType = DpSupportedType.TIMESTAMP;
        final int               cntTms = 10;
        
        // Create test factory and check configuration
        TimestampFactory    facTest = TimestampFactory.from(bolRandom, lngSeed);
        
        Assert.assertEquals(bolRandom, facTest.isRandom());
        Assert.assertEquals(lngSeed, facTest.getRandomSeedValue());
        Assert.assertEquals(enmType, facTest.getDatumType());
        
        // Create timestamp sequence and inspect
        List<Instant>       lstTms = new ArrayList<>(cntTms);
        
        for (int iTms=0; iTms<cntTms; iTms++) {
            Object      objVal = facTest.nextDatum();
            
            Assert.assertTrue(enmType.isAssignableFrom(objVal));
            
            if (objVal instanceof Instant insVal) 
                lstTms.add(insVal);
            else
                Assert.fail("Timestamp #" + iTms + " is not Instant.");
        }
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Timestamps: " + lstTms);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#nextDatum()}.
     */
    @Test
    public final void testNextValuePseudoRandom() {
        
        // Test Parameters
        final boolean           bolRandom = true;
        final long              lngSeed = 1;
        
        final DpSupportedType   enmType = DpSupportedType.TIMESTAMP;
        final int               cntTms = 10;
        
        // Create 2 test factories with same random seed and check configuration
        TimestampFactory    facTest1 = TimestampFactory.from(bolRandom, lngSeed);
        TimestampFactory    facTest2 = TimestampFactory.from(bolRandom, lngSeed);
        
        Assert.assertEquals(bolRandom, facTest1.isRandom());
        Assert.assertEquals(lngSeed, facTest1.getRandomSeedValue());
        Assert.assertEquals(enmType, facTest1.getDatumType());
        
        Assert.assertEquals(bolRandom, facTest2.isRandom());
        Assert.assertEquals(lngSeed, facTest2.getRandomSeedValue());
        Assert.assertEquals(enmType, facTest2.getDatumType());
        
        // Create timestamp sequence and inspect
        List<Instant>       lstTms1 = new ArrayList<>(cntTms);
        List<Instant>       lstTms2 = new ArrayList<>(cntTms);
        
        for (int iTms=0; iTms<cntTms; iTms++) {
            Object      objVal1 = facTest1.nextDatum();
            Object      objVal2 = facTest2.nextDatum();
            
            Assert.assertTrue(enmType.isAssignableFrom(objVal1));
            Assert.assertTrue(enmType.isAssignableFrom(objVal2));
            
            if (objVal1 instanceof Instant insVal) 
                lstTms1.add(insVal);
            else
                Assert.fail("Factory #1 timestamp #" + iTms + " is not Instant.");

            if (objVal2 instanceof Instant insVal) 
                lstTms2.add(insVal);
            else
                Assert.fail("Factory #2 timestamp #" + iTms + " is not Instant.");
        }

        // Check equivalent series
        Assert.assertEquals(lstTms1, lstTms2);
        
        // Print out sequence
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        for (int iTms=0; iTms<cntTms; iTms++) {
            Instant tms1 = lstTms1.get(iTms);
            Instant tms2 = lstTms2.get(iTms);
            
            System.out.println("  Timestamp # " + iTms + ": " + tms1 + ", " + tms2);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory#nextDatum()}.
     */
    @Test
    public final void testNextValueIncremental() {
        
        // Test Parameters
        final String            strDate = "1964-10-30T11:00:00Z";
        final int               cntTms = 10;
        
        final Duration          durPeriod = Duration.ofMinutes(1);
        final Instant           insStart = Instant.parse(strDate);
        final DpSupportedType   enmType = DpSupportedType.TIMESTAMP;
        
        
        // Create test factory and check configuration
        TimestampFactory    facTest = TimestampFactory.from(durPeriod, insStart);
        
        Assert.assertEquals(durPeriod, facTest.getPeriod());
        Assert.assertEquals(insStart, facTest.getStartInstant());
        Assert.assertEquals(enmType, facTest.getDatumType());

        // Create timestamp sequence and inspect
        List<Instant>       lstTms = new ArrayList<>(cntTms);
        for (int iTms=0; iTms<cntTms; iTms++) {
            Object      objVal = facTest.nextDatum();
            
            Assert.assertTrue(enmType.isAssignableFrom(objVal));
            
            if (objVal instanceof Instant insVal) 
                lstTms.add(insVal);
            else
                Assert.fail("Timestamp #" + iTms + " is not Instant.");
        }

        // Create equivalent instant sequence and compare
        List<Instant>       lstChk = new ArrayList<>(cntTms);
        
        Instant     insCurr = insStart;
        for (int iIns=0; iIns<cntTms; iIns++) {
            lstChk.add(insCurr);
            
            insCurr = insCurr.plus(durPeriod);
        }
        
        Assert.assertEquals(lstChk, lstTms);
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Timestamps: " + lstTms);
    }
}
