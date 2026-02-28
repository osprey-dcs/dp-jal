/*
 * Project: dp-jal
 * File:	FrameTimestampsSpecTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.specs
 * Type: 	FrameTimestampsSpecTest
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
 * @since Jan 10, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.common.DpTimestampCase;
import com.ospreydcs.dp.jal.common.UniformSamplingClock;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameTimestampsFactory;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.frames.JalToolsFramesTmsConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for record <code>FrameTimestampsSpec</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 10, 2026
 *
 */
public class FrameTimestampsSpecTest {

    
    //
    // JAL Tools Library Resources
    //
    
    /** The default ingestion frame timestamps properties */
    public static final JalToolsFramesTmsConfig     CFG_DEF = JalToolsConfig.getInstance().datagen.frame.timestamps;
    
    
    //
    // Class Constants
    //
    
    /** The default ingestion frame start instant */
    public static final DpTimestampCase ENM_CASE_DEF = CFG_DEF.type;
    
    /** The default ingestion frame sample count */
    public static final int             INT_COUNT_DEF = CFG_DEF.count;
    
    /** The default ingestion frame sample period */
    public static final Duration        DUR_PERIOD_DEF = CFG_DEF.periodDuration();
    
    /** The default ingestion frame start instant */
    public static final Instant         INS_START_DEF = CFG_DEF.startInstant();
    
    /** The default ingestion frame start instant delay */
    public static final Duration        DUR_DELAY_DEF = CFG_DEF.delayDuration();

    
    //
    // Test Resources
    //
    
    /** The sample period for parsing tests 3, 4, and 5 */
    public static final Duration        DUR_PERIOD_345 = Duration.parse("PT0.001S");

    
    /** The start time instant for parsing test 3 */
    public static final Instant         INS_START_3 = Instant.parse("2026-01-10T10:15:00.000Z");
    
    /** The start time instant for parsing test 4 */
    public static final Instant         INS_START_4 = Instant.parse("2026-01-10T10:15:02.000+00:00");
    
    /** The start time instant for parsing test 3 */
    public static final Instant         INS_START_5 = Instant.parse("2026-01-10T10:15:02.000Z");

    
    /** The start time delay for parsing test 5 */
    public static final Duration        DUR_DELAY_5 = Duration.parse("PT0.003S");
    
    
    /** Arguments collection for parsing creator test */
    public static final String[]        ARR_ARGS_PARSE_1 = { };
    
    /** Arguments collection for parsing creator test */
    public static final String[]        ARR_ARGS_PARSE_2 = { "23" };
    
    /** Arguments collection for parsing creator test */
    public static final String[]        ARR_ARGS_PARSE_3 = { "23", "PT0.001S", "SAMPLING_CLOCK" };
    
    /** Arguments collection for parsing creator test */
    public static final String[]        ARR_ARGS_PARSE_4 = { "23", "PT0.001S", "TIMESTAMP_LIST", "2026-01-10T10:15:02.000+00:00" };
    
    /** Arguments collection for parsing creator test */
    public static final String[]        ARR_ARGS_PARSE_5 = { "23", "PT0.001S", "TIMESTAMP_LIST", "2026-01-10T10:15:02.000Z", "PT0.003S" };
    
    
    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec#from()}.
     */
    @Test
    public final void testFrom() {
        
        // Test Parameters
        final int                 cntSamples = INT_COUNT_DEF;
        final Duration            durPeriod = DUR_PERIOD_DEF;
        final Instant             insStart = INS_START_DEF;
        final DpTimestampCase     enmType = ENM_CASE_DEF;
        final Duration            durDelay = DUR_DELAY_DEF;
        
        // Create frame timestamps specification and check configuration
        FrameTimestampsSpec     specTest = FrameTimestampsSpec.from();
        
        Assert.assertEquals(cntSamples, specTest.cntSamples());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(enmType, specTest.enmCase());
        Assert.assertEquals(durDelay, specTest.durDelay());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec#from(int)}.
     */
    @Test
    public final void testFromInt() {
        
        // Test Parameters
        final int                 cntSamples = 42;
        final Duration            durPeriod = DUR_PERIOD_DEF;
        final Instant             insStart = INS_START_DEF;
        final DpTimestampCase     enmType = ENM_CASE_DEF;
        final Duration            durDelay = DUR_DELAY_DEF;
        
        // Create frame timestamps specification and check configuration
        FrameTimestampsSpec     specTest = FrameTimestampsSpec.from(cntSamples);
        
        Assert.assertEquals(cntSamples, specTest.cntSamples());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(enmType, specTest.enmCase());
        Assert.assertEquals(durDelay, specTest.durDelay());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec#from(int, java.time.Duration)}.
     */
    @Test
    public final void testFromIntDuration() {
        
        // Test Parameters
        final int                 cntSamples = 42;
        final Duration            durPeriod = Duration.ofMillis(1);
        final Instant             insStart = INS_START_DEF;
        final DpTimestampCase     enmType = ENM_CASE_DEF;
        final Duration            durDelay = DUR_DELAY_DEF;
        
        // Create frame timestamps specification and check configuration
        FrameTimestampsSpec     specTest = FrameTimestampsSpec.from(cntSamples, durPeriod);
        
        Assert.assertEquals(cntSamples, specTest.cntSamples());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(enmType, specTest.enmCase());
        Assert.assertEquals(durDelay, specTest.durDelay());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec#from(int, java.time.Duration, java.time.Instant)}.
     */
    @Test
    public final void testFromIntDurationInstant() {
        
        // Test Parameters
        final int                 cntSamples = 42;
        final Duration            durPeriod = Duration.ofMillis(1);
        final Instant             insStart = Instant.now();
        final DpTimestampCase     enmType = ENM_CASE_DEF;
        final Duration            durDelay = DUR_DELAY_DEF;
        
        // Create frame timestamps specification and check configuration
        FrameTimestampsSpec     specTest = FrameTimestampsSpec.from(cntSamples, durPeriod, insStart);
        
        Assert.assertEquals(cntSamples, specTest.cntSamples());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(enmType, specTest.enmCase());
        Assert.assertEquals(durDelay, specTest.durDelay());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec#from(int, java.time.Duration, java.time.Instant, com.ospreydcs.dp.jal.common.DpTimestampCase)}.
     */
    @Test
    public final void testFromIntDurationInstantDpTimestampCase() {
        
        // Test Parameters
        final int                 cntSamples = 42;
        final Duration            durPeriod = Duration.ofMillis(1);
        final Instant             insStart = Instant.now();
        final DpTimestampCase     enmType = DpTimestampCase.TIMESTAMP_LIST;
        final Duration            durDelay = DUR_DELAY_DEF;
        
        // Create frame timestamps specification and check configuration
        FrameTimestampsSpec     specTest = FrameTimestampsSpec.from(cntSamples, durPeriod, enmType, insStart);
        
        Assert.assertEquals(cntSamples, specTest.cntSamples());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(enmType, specTest.enmCase());
        Assert.assertEquals(durDelay, specTest.durDelay());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec#from(int, java.time.Duration, java.time.Instant, com.ospreydcs.dp.jal.common.DpTimestampCase, java.time.Duration)}.
     */
    @Test
    public final void testFromIntDurationInstantDpTimestampCaseDuration() {
        
        // Test Parameters
        final int                 cntSamples = 42;
        final Duration            durPeriod = Duration.ofMillis(1);
        final Instant             insStart = Instant.now();
        final DpTimestampCase     enmType = DpTimestampCase.TIMESTAMP_LIST;
        final Duration            durDelay = Duration.ofMillis(3);
        
        // Create frame timestamps specification and check configuration
        FrameTimestampsSpec     specTest = FrameTimestampsSpec.from(cntSamples, durPeriod, enmType, insStart, durDelay);
        
        Assert.assertEquals(cntSamples, specTest.cntSamples());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(enmType, specTest.enmCase());
        Assert.assertEquals(durDelay, specTest.durDelay());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse1() {
        
        // Test Parameters
        final String[]            arrArgs = ARR_ARGS_PARSE_1;
        
        final int                 cntSamples = INT_COUNT_DEF;
        final Duration            durPeriod = DUR_PERIOD_DEF;
        final Instant             insStart = INS_START_DEF;
        final DpTimestampCase     enmType = ENM_CASE_DEF;
        final Duration            durDelay = DUR_DELAY_DEF;
        
        // Create frame timestamps specification via parsing and check configuration
        try {
            FrameTimestampsSpec     specTest = FrameTimestampsSpec.parse(arrArgs);

            Assert.assertEquals(cntSamples, specTest.cntSamples());
            Assert.assertEquals(durPeriod, specTest.durPeriod());
            Assert.assertEquals(insStart, specTest.insStart());
            Assert.assertEquals(enmType, specTest.enmCase());
            Assert.assertEquals(durDelay, specTest.durDelay());

        } catch (Exception e) {
            Assert.fail("Parsing creator failed with exception " + e.getClass().getName() + " for arguments " + Arrays.asList(arrArgs) +  " : " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse2() {
        
        // Test Parameters
        final String[]            arrArgs = ARR_ARGS_PARSE_2;
        
        final int                 cntSamples = 23;
        final Duration            durPeriod = DUR_PERIOD_DEF;
        final Instant             insStart = INS_START_DEF;
        final DpTimestampCase     enmType = ENM_CASE_DEF;
        final Duration            durDelay = DUR_DELAY_DEF;
        
        // Create frame timestamps specification via parsing and check configuration
        try {
            FrameTimestampsSpec     specTest = FrameTimestampsSpec.parse(arrArgs);

            Assert.assertEquals(cntSamples, specTest.cntSamples());
            Assert.assertEquals(durPeriod, specTest.durPeriod());
            Assert.assertEquals(insStart, specTest.insStart());
            Assert.assertEquals(enmType, specTest.enmCase());
            Assert.assertEquals(durDelay, specTest.durDelay());

        } catch (Exception e) {
            Assert.fail("Parsing creator failed with exception " + e.getClass().getName() + " for arguments " + Arrays.asList(arrArgs) +  " : " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse3() {
        
        // Test Parameters
        final String[]            arrArgs = ARR_ARGS_PARSE_3;
        
        final int                 cntSamples = 23;
        final Duration            durPeriod = DUR_PERIOD_345;
        final DpTimestampCase     enmType = DpTimestampCase.SAMPLING_CLOCK;
        final Instant             insStart = INS_START_DEF;
        final Duration            durDelay = DUR_DELAY_DEF;
        
        // Create frame timestamps specification via parsing and check configuration
        try {
            FrameTimestampsSpec     specTest = FrameTimestampsSpec.parse(arrArgs);

            Assert.assertEquals(cntSamples, specTest.cntSamples());
            Assert.assertEquals(durPeriod, specTest.durPeriod());
            Assert.assertEquals(insStart, specTest.insStart());
            Assert.assertEquals(enmType, specTest.enmCase());
            Assert.assertEquals(durDelay, specTest.durDelay());

        } catch (Exception e) {
            Assert.fail("Parsing creator failed with exception " + e.getClass().getName() + " for arguments " + Arrays.asList(arrArgs) +  " : " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse4() {
        
        // Test Parameters
        final String[]            arrArgs = ARR_ARGS_PARSE_4;
        
        final int                 cntSamples = 23;
        final Duration            durPeriod = DUR_PERIOD_345;
        final Instant             insStart = INS_START_4;
        final DpTimestampCase     enmType = DpTimestampCase.TIMESTAMP_LIST;
        final Duration            durDelay = DUR_DELAY_DEF;
        
        // Create frame timestamps specification via parsing and check configuration
        try {
            FrameTimestampsSpec     specTest = FrameTimestampsSpec.parse(arrArgs);

            Assert.assertEquals(cntSamples, specTest.cntSamples());
            Assert.assertEquals(durPeriod, specTest.durPeriod());
            Assert.assertEquals(insStart, specTest.insStart());
            Assert.assertEquals(enmType, specTest.enmCase());
            Assert.assertEquals(durDelay, specTest.durDelay());

        } catch (Exception e) {
            Assert.fail("Parsing creator failed with exception " + e.getClass().getName() + " for arguments " + Arrays.asList(arrArgs) +  " : " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse5() {
        
        // Test Parameters
        final String[]            arrArgs = ARR_ARGS_PARSE_5;
        
        final int                 cntSamples = 23;
        final Duration            durPeriod = DUR_PERIOD_345;
        final Instant             insStart = INS_START_5;
        final DpTimestampCase     enmType = DpTimestampCase.TIMESTAMP_LIST;
        final Duration            durDelay = DUR_DELAY_5;
        
        // Create frame timestamps specification via parsing and check configuration
        try {
            FrameTimestampsSpec     specTest = FrameTimestampsSpec.parse(arrArgs);

            Assert.assertEquals(cntSamples, specTest.cntSamples());
            Assert.assertEquals(durPeriod, specTest.durPeriod());
            Assert.assertEquals(insStart, specTest.insStart());
            Assert.assertEquals(enmType, specTest.enmCase());
            Assert.assertEquals(durDelay, specTest.durDelay());

        } catch (Exception e) {
            Assert.fail("Parsing creator failed with exception " + e.getClass().getName() + " for arguments " + Arrays.asList(arrArgs) +  " : " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec#defaultFrame()}.
     */
    @Test
    public final void testDefaultFrame() {
        
        // Test Parameters
        final int                 cntSamples = INT_COUNT_DEF;
        final Duration            durPeriod = DUR_PERIOD_DEF;
        final Instant             insStart = INS_START_DEF;
        final DpTimestampCase     enmType = ENM_CASE_DEF;
        final Duration            durDelay = DUR_DELAY_DEF;
        
        // Create frame timestamps specification and check configuration
        FrameTimestampsSpec     specTest = FrameTimestampsSpec.defaultFrame();
        
        Assert.assertEquals(cntSamples, specTest.cntSamples());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(enmType, specTest.enmCase());
        Assert.assertEquals(durDelay, specTest.durDelay());
        
        // Print out default frame configuration (tests FrameTimestampsSpec#toString())
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("Default Frame Configuration");
        System.out.println(specTest);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec#newFactory()}.
     */
    @Test
    public final void testNewFactory() {
        
        // Test Parameters
        final int                 cntSamples = INT_COUNT_DEF;
        final Duration            durPeriod = DUR_PERIOD_DEF;
        final Instant             insStart = INS_START_DEF;
        final DpTimestampCase     enmType = ENM_CASE_DEF;
        final Duration            durDelay = DUR_DELAY_DEF;
        
        // Create frame timestamps specification and check configuration
        FrameTimestampsSpec     specTest = FrameTimestampsSpec.defaultFrame();
        
        Assert.assertEquals(cntSamples, specTest.cntSamples());
        Assert.assertEquals(durPeriod, specTest.durPeriod());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(enmType, specTest.enmCase());
        Assert.assertEquals(durDelay, specTest.durDelay());

        // Create frame timestamp factory and check configuration
        IFrameTimestampsFactory  facTms = specTest.newFactory();
        
        Assert.assertEquals(cntSamples, facTms.getSampleCount());
        Assert.assertEquals(enmType, facTms.getTimestampCase());
        
        // Create a uniform sampling clock and check parameters
        UniformSamplingClock    clkTest = facTms.nextUniformClock();
        
        Assert.assertEquals(cntSamples, clkTest.getSampleCount());
        Assert.assertEquals(durPeriod, clkTest.getSamplePeriodDuration());
        Assert.assertEquals(insStart.plus(durDelay), clkTest.getStartInstant());
        
        // Create a timestamp list and check properties
        List<Instant>   lstTms = facTms.nextTimestampVector();

        Instant     insFirst = insStart.plus(durDelay).plus(durPeriod.multipliedBy(cntSamples));
        Instant     insExpect = insFirst;
        for (Instant insCurr : lstTms) {
            Assert.assertEquals(insExpect, insCurr);
            
            insExpect = insExpect.plus(durPeriod);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec#equals(java.lang.Object)}.
     */
    @Test
    public final void testEquals() {
        
        // Create equivalent frame timestamp specifications and check
        FrameTimestampsSpec     specFrom = FrameTimestampsSpec.from();
        FrameTimestampsSpec     specDef = FrameTimestampsSpec.defaultFrame();
        
        Assert.assertEquals(specFrom, specDef);
    }


}
