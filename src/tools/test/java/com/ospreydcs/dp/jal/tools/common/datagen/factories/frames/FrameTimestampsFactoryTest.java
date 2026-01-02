/*
 * Project: dp-jal
 * File:	FrameTimestampsFactoryTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.frames
 * Type: 	FrameTimestampsFactoryTest
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
 * @since Jan 2, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.frames;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.common.DpTimestampCase;
import com.ospreydcs.dp.jal.common.UniformSamplingClock;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.frames.JalToolsFramesTmsConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>FrameTimestampFactory</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 2, 2026
 *
 */
public class FrameTimestampsFactoryTest {

    
    //
    // Library Resources
    //
    
    /** The default ingestion frame timestamp configuration */
    public static final JalToolsFramesTmsConfig      CFG_DEF = JalToolsConfig.getInstance().datagen.frames.timestamps;
    
    
    //
    // Class Constants
    //
    
    /** The default ingestion frame timestamp case */
    public static final DpTimestampCase     ENM_TMS_CASE_DEF = CFG_DEF.type;
    
    /** The default ingestion frame timestamp count */
    public static final int                 INT_COUNT_DEF = CFG_DEF.count;
    
    /** The default ingestion frame sampling period */
    public static final Duration            DUR_PERIOD_DEF = CFG_DEF.periodDuration();
    
    /** The default ingestion frame sampling start time instant */
    public static final Instant             INS_START_DEF = CFG_DEF.startInstant();
    
    /** The default ingestion frame sampling start time delay */
    public static final Duration            DUR_DELAY_DEF = CFG_DEF.delayDuration();
    
    
    
    /** Sample count string value used for parsing constructor tests */
    public static final String              STR_COUNT_PARSE = "42";
    
    /** Sampling period ISO-8605 format string used for parsing constructor tests */
    public static final String              STR_PERIOD_PARSE = "PT0.023S";
    
    /** Start time instant ISO8605 format string used for parsing constructor tests */
    public static final String              STR_START_PARSE = "2026-01-02T11:11:00.00+00:00";
    
    /** <code>DpTimestampCase</code> constant name used for parsing constructor tests */
    public static final String              STR_TMS_CASE_PARSE = "TIMESTAMP_LIST";
    
    /** Start time delay ISO-8605 format string used for parsing constructor tests */
    public static final String              STR_DELAY_PARSE = "PT0.001S";
    
    
    /** Sample count used for parsing constructor tests */
    public static final int                 INT_COUNT_PARSE = Integer.valueOf(STR_COUNT_PARSE);
    
    /** Sampling period used for parsing constructor tests */
    public static final Duration            DUR_PERIOD_PARSE = Duration.parse(STR_PERIOD_PARSE);
    
    /** Start time instant used for parsing constructor tests */
    public static final Instant             INS_START_PARSE = Instant.parse(STR_START_PARSE);
    
    /** Timestamp case type used for parsing constructor tests */
    public static final DpTimestampCase     ENM_TMS_CASE_PARSE = DpTimestampCase.valueFrom(STR_TMS_CASE_PARSE);
    
    /** Start time delay used for parsing constructor tests */
    public static final Duration            DUR_DELAY_PARSE = Duration.parse(STR_DELAY_PARSE);
    
    
    /** Arguments for parsing constructor tests */
    public static final String[]            ARR_ARGS_PARSE_1 = { STR_COUNT_PARSE, STR_PERIOD_PARSE, STR_START_PARSE, STR_TMS_CASE_PARSE, STR_DELAY_PARSE };
    
    /** Arguments for parsing constructor tests */
    public static final String[]            ARR_ARGS_PARSE_2 = { STR_COUNT_PARSE, STR_PERIOD_PARSE, STR_START_PARSE, STR_TMS_CASE_PARSE };
    
    /** Arguments for parsing constructor tests */
    public static final String[]            ARR_ARGS_PARSE_3 = { STR_COUNT_PARSE, STR_PERIOD_PARSE, STR_START_PARSE };
    
    /** Arguments for parsing constructor tests */
    public static final String[]            ARR_ARGS_PARSE_4 = { STR_COUNT_PARSE, STR_PERIOD_PARSE };
    
    /** Arguments for parsing constructor tests */
    public static final String[]            ARR_ARGS_PARSE_5 = { STR_COUNT_PARSE };
    
    
    //
    // Test Resources
    //
    
    /** Frame timestamps factory corresponding to parse arguments */
    public static final FrameTimestampsFactory  FAC_PARSE_1 = FrameTimestampsFactory.from(INT_COUNT_PARSE, DUR_PERIOD_PARSE, INS_START_PARSE, ENM_TMS_CASE_PARSE, DUR_DELAY_PARSE);
    
    /** Frame timestamps factory corresponding to parse arguments */
    public static final FrameTimestampsFactory  FAC_PARSE_2 = FrameTimestampsFactory.from(INT_COUNT_PARSE, DUR_PERIOD_PARSE, INS_START_PARSE, ENM_TMS_CASE_PARSE);
    
    /** Frame timestamps factory corresponding to parse arguments */
    public static final FrameTimestampsFactory  FAC_PARSE_3 = FrameTimestampsFactory.from(INT_COUNT_PARSE, DUR_PERIOD_PARSE, INS_START_PARSE);
    
    /** Frame timestamps factory corresponding to parse arguments */
    public static final FrameTimestampsFactory  FAC_PARSE_4 = FrameTimestampsFactory.from(INT_COUNT_PARSE, DUR_PERIOD_PARSE);
    
    /** Frame timestamps factory corresponding to parse arguments */
    public static final FrameTimestampsFactory  FAC_PARSE_5 = FrameTimestampsFactory.from(INT_COUNT_PARSE);
    
    
    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#from()}.
     */
    @Test
    public final void testFrom() {
        
        // Test Parameter
        final DpTimestampCase       enmCase = ENM_TMS_CASE_DEF;
        final int                   cntTms = INT_COUNT_DEF;
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = FrameTimestampsFactory.from();
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
        
        // Print out default configuration - tests Object#toString() override
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("Default Configuration");
        System.out.println(facTest);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#from(int)}.
     */
    @Test
    public final void testFromInt() {
        
        // Test Parameter
        final DpTimestampCase       enmCase = ENM_TMS_CASE_DEF;
        final int                   cntTms = 42;
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = FrameTimestampsFactory.from(cntTms);
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#from(int, java.time.Duration)}.
     */
    @Test
    public final void testFromIntDuration() {
        
        // Test Parameter
        final DpTimestampCase       enmCase = ENM_TMS_CASE_DEF;
        final int                   cntTms = 42;
        final Duration              durPeriod = Duration.ofMillis(23);
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = FrameTimestampsFactory.from(cntTms, durPeriod);
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
        
        // Check the period from a generated sample clock
        UniformSamplingClock    clk = facTest.nextUniformClock();
        
        Assert.assertEquals(durPeriod, clk.getSamplePeriodDuration());
        Assert.assertEquals(cntTms, clk.getSampleCount());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#from(int, java.time.Duration, java.time.Instant)}.
     */
    @Test
    public final void testFromIntDurationInstant() {
        
        // Test Parameter
        final DpTimestampCase       enmCase = ENM_TMS_CASE_DEF;
        final int                   cntTms = 42;
        final Duration              durPeriod = Duration.ofMillis(23);
        final Instant               insStart = Instant.now();
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = FrameTimestampsFactory.from(cntTms, durPeriod, insStart);
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
        
        // Check the period from a generated sample clock
        UniformSamplingClock    clk = facTest.nextUniformClock();
        
        Assert.assertEquals(durPeriod, clk.getSamplePeriodDuration());
        Assert.assertEquals(cntTms, clk.getSampleCount());
        Assert.assertEquals(insStart, clk.getStartInstant());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#from(int, java.time.Duration, java.time.Instant, com.ospreydcs.dp.jal.common.DpTimestampCase)}.
     */
    @Test
    public final void testFromIntDurationInstantDpTimestampCase() {
        
        // Test Parameter
        final DpTimestampCase       enmCase = DpTimestampCase.TIMESTAMP_LIST;
        final int                   cntTms = 42;
        final Duration              durPeriod = Duration.ofMillis(23);
        final Instant               insStart = Instant.now();
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = FrameTimestampsFactory.from(cntTms, durPeriod, insStart, enmCase);
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
        
        // Check the period from a generated sample clock
        UniformSamplingClock    clk = facTest.nextUniformClock();
        
        Assert.assertEquals(durPeriod, clk.getSamplePeriodDuration());
        Assert.assertEquals(cntTms, clk.getSampleCount());
        Assert.assertEquals(insStart, clk.getStartInstant());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#from(int, java.time.Duration, java.time.Instant, com.ospreydcs.dp.jal.common.DpTimestampCase, java.time.Duration)}.
     */
    @Test
    public final void testFromIntDurationInstantDpTimestampCaseDuration() {
        
        // Test Parameter
        final DpTimestampCase       enmCase = DpTimestampCase.TIMESTAMP_LIST;
        final int                   cntTms = 42;
        final Duration              durPeriod = Duration.ofMillis(23);
        final Instant               insStart = Instant.now();
        final Duration              durDelay = Duration.ofMillis(1);
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = FrameTimestampsFactory.from(cntTms, durPeriod, insStart, enmCase, durDelay);
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
        
        // Check the period from a generated sample clock
        UniformSamplingClock    clk = facTest.nextUniformClock();
        
        Assert.assertEquals(durPeriod, clk.getSamplePeriodDuration());
        Assert.assertEquals(cntTms, clk.getSampleCount());
        Assert.assertEquals(insStart.plus(durDelay), clk.getStartInstant());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse1() {
        
        // Test Parameter
        final String[]              arrArgs = ARR_ARGS_PARSE_1;
        
        final int                   cntTms = INT_COUNT_PARSE;
        final Duration              durPeriod = DUR_PERIOD_PARSE;
        final Instant               insStart = INS_START_PARSE;
        final DpTimestampCase       enmCase = ENM_TMS_CASE_PARSE;
        final Duration              durDelay = DUR_DELAY_PARSE;
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = FrameTimestampsFactory.parse(arrArgs);
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
        
        // Check the period from a generated sample clock
        UniformSamplingClock    clk = facTest.nextUniformClock();
        
        Assert.assertEquals(durPeriod, clk.getSamplePeriodDuration());
        Assert.assertEquals(cntTms, clk.getSampleCount());
        Assert.assertEquals(insStart.plus(durDelay), clk.getStartInstant());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse2() {
        
        // Test Parameter
        final String[]              arrArgs = ARR_ARGS_PARSE_2;
        
        final int                   cntTms = INT_COUNT_PARSE;
        final Duration              durPeriod = DUR_PERIOD_PARSE;
        final Instant               insStart = INS_START_PARSE;
        final DpTimestampCase       enmCase = ENM_TMS_CASE_PARSE;
        
        final Duration              durDelay = DUR_DELAY_DEF;
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = FrameTimestampsFactory.parse(arrArgs);
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
        
        // Check the period from a generated sample clock
        UniformSamplingClock    clk = facTest.nextUniformClock();
        
        Assert.assertEquals(durPeriod, clk.getSamplePeriodDuration());
        Assert.assertEquals(cntTms, clk.getSampleCount());
        Assert.assertEquals(insStart.plus(durDelay), clk.getStartInstant());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse3() {
        
        // Test Parameter
        final String[]              arrArgs = ARR_ARGS_PARSE_3;
        
        final int                   cntTms = INT_COUNT_PARSE;
        final Duration              durPeriod = DUR_PERIOD_PARSE;
        final Instant               insStart = INS_START_PARSE;
        
        final DpTimestampCase       enmCase = ENM_TMS_CASE_DEF;
        final Duration              durDelay = DUR_DELAY_DEF;
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = FrameTimestampsFactory.parse(arrArgs);
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
        
        // Check the period from a generated sample clock
        UniformSamplingClock    clk = facTest.nextUniformClock();
        
        Assert.assertEquals(durPeriod, clk.getSamplePeriodDuration());
        Assert.assertEquals(cntTms, clk.getSampleCount());
        Assert.assertEquals(insStart.plus(durDelay), clk.getStartInstant());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse4() {
        
        // Test Parameter
        final String[]              arrArgs = ARR_ARGS_PARSE_4;
        
        final int                   cntTms = INT_COUNT_PARSE;
        final Duration              durPeriod = DUR_PERIOD_PARSE;
        
        final Instant               insStart = INS_START_DEF;
        final DpTimestampCase       enmCase = ENM_TMS_CASE_DEF;
        final Duration              durDelay = DUR_DELAY_DEF;
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = FrameTimestampsFactory.parse(arrArgs);
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
        
        // Check the period from a generated sample clock
        UniformSamplingClock    clk = facTest.nextUniformClock();
        
        Assert.assertEquals(durPeriod, clk.getSamplePeriodDuration());
        Assert.assertEquals(cntTms, clk.getSampleCount());
        Assert.assertEquals(insStart.plus(durDelay), clk.getStartInstant());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse5() {
        
        // Test Parameter
        final String[]              arrArgs = ARR_ARGS_PARSE_5;
        
        final int                   cntTms = INT_COUNT_PARSE;
        
        final Duration              durPeriod = DUR_PERIOD_DEF;
        final Instant               insStart = INS_START_DEF;
        final DpTimestampCase       enmCase = ENM_TMS_CASE_DEF;
        final Duration              durDelay = DUR_DELAY_DEF;
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = FrameTimestampsFactory.parse(arrArgs);
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
        
        // Check the period from a generated sample clock
        UniformSamplingClock    clk = facTest.nextUniformClock();
        
        Assert.assertEquals(durPeriod, clk.getSamplePeriodDuration());
        Assert.assertEquals(cntTms, clk.getSampleCount());
        Assert.assertEquals(insStart.plus(durDelay), clk.getStartInstant());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#parse(java.lang.String[])}.
     */
    @Test
    public final void testParseDefault() {
        
        // Test Parameter
        final String[]              arrArgs = { };
        
        final int                   cntTms = INT_COUNT_DEF;
        final Duration              durPeriod = DUR_PERIOD_DEF;
        final Instant               insStart = INS_START_DEF;
        final DpTimestampCase       enmCase = ENM_TMS_CASE_DEF;
        final Duration              durDelay = DUR_DELAY_DEF;
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = FrameTimestampsFactory.parse(arrArgs);
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
        
        // Check the period from a generated sample clock
        UniformSamplingClock    clk = facTest.nextUniformClock();
        
        Assert.assertEquals(durPeriod, clk.getSamplePeriodDuration());
        Assert.assertEquals(cntTms, clk.getSampleCount());
        Assert.assertEquals(insStart.plus(durDelay), clk.getStartInstant());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#FrameTimestampsFactory(int, java.time.Duration, java.time.Instant, com.ospreydcs.dp.jal.common.DpTimestampCase, java.time.Duration)}.
     */
    @Test
    public final void testFrameTimestampsFactory() {
        
        // Test Parameter
        final DpTimestampCase       enmCase = DpTimestampCase.TIMESTAMP_LIST;
        final int                   cntTms = 42;
        final Duration              durPeriod = Duration.ofMillis(23);
        final Instant               insStart = Instant.now();
        final Duration              durDelay = Duration.ofMillis(1);
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = new FrameTimestampsFactory(cntTms, durPeriod, insStart, enmCase, durDelay);
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
        
        // Check the period from a generated sample clock
        UniformSamplingClock    clk = facTest.nextUniformClock();
        
        Assert.assertEquals(durPeriod, clk.getSamplePeriodDuration());
        Assert.assertEquals(cntTms, clk.getSampleCount());
        Assert.assertEquals(insStart.plus(durDelay), clk.getStartInstant());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#nextUniformClock()}.
     */
    @Test
    public final void testNextUniformClock() {
        
        // Test Parameter
        final int                   cntTms = 42;
        final Duration              durPeriod = Duration.ofMillis(23);
        final Instant               insStart = Instant.now();
        final DpTimestampCase       enmCase = DpTimestampCase.SAMPLING_CLOCK;
        final Duration              durDelay = Duration.ofMillis(1);
        
        final int                   cntClks = 10;
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = new FrameTimestampsFactory(cntTms, durPeriod, insStart, enmCase, durDelay);
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
        
        // Create a sequence of sampling clocks and check parameters
        Instant     insClkStart = insStart.plus(durDelay);
        Duration    durRange = durPeriod.multipliedBy(cntTms);
        for (int iClk=0; iClk<cntClks; iClk++) {
            UniformSamplingClock    clk = facTest.nextUniformClock();

            Assert.assertEquals(durPeriod, clk.getSamplePeriodDuration());
            Assert.assertEquals(cntTms, clk.getSampleCount());
            Assert.assertEquals(insClkStart, clk.getStartInstant());
            
            insClkStart = insClkStart.plus(durRange);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#nextTimestampVector()}.
     */
    @Test
    public final void testNextTimestampVector() {
        
        // Test Parameter
        final int                   cntTms = 42;
        final Duration              durPeriod = Duration.ofMillis(23);
        final Instant               insStart = Instant.now();
        final DpTimestampCase       enmCase = DpTimestampCase.TIMESTAMP_LIST;
        final Duration              durDelay = Duration.ofMillis(1);
        
        final int                   cntClks = 10;
        
        // Create default frame timestamps factory and check configuration
        FrameTimestampsFactory   facTest = new FrameTimestampsFactory(cntTms, durPeriod, insStart, enmCase, durDelay);
        
        Assert.assertEquals(enmCase, facTest.getTimestampCase());
        Assert.assertEquals(cntTms, facTest.getSampleCount());
        
        // Create a sequence of timestamp lists and check values
        Instant     insFrmStart = insStart.plus(durDelay);
        Duration    durRange = durPeriod.multipliedBy(cntTms);
        for (int iClk=0; iClk<cntClks; iClk++) {
            ArrayList<Instant>      lstTms = facTest.nextTimestampVector();

            // Check list properties
            Assert.assertEquals(cntTms, lstTms.size());
            Assert.assertEquals(insFrmStart, lstTms.getFirst());
            
            // Check all the intervals between timestamps
            Instant     insPrev = null;
            for (Instant insCurr : lstTms) {
                if (insPrev == null) {
                    insPrev = insCurr;
                    continue;
                }
                
                Duration    durIval = Duration.between(insPrev, insCurr);
                Assert.assertEquals(durPeriod, durIval);
                
                insPrev = insCurr;
            }
            
            insFrmStart = insFrmStart.plus(durRange);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory#equals(java.lang.Object)}.
     */
    @Test
    public final void testEqualsObject() {
        
        // Test Parameters
        FrameTimestampsFactory  facExpect1 = FAC_PARSE_1;
        FrameTimestampsFactory  facExpect2 = FAC_PARSE_2;
        FrameTimestampsFactory  facExpect3 = FAC_PARSE_3;
        FrameTimestampsFactory  facExpect4 = FAC_PARSE_4;
        FrameTimestampsFactory  facExpect5 = FAC_PARSE_5;
        
        // Create new factories and test equivalence
        FrameTimestampsFactory  facTest1 = FrameTimestampsFactory.parse(ARR_ARGS_PARSE_1);
        FrameTimestampsFactory  facTest2 = FrameTimestampsFactory.parse(ARR_ARGS_PARSE_2);
        FrameTimestampsFactory  facTest3 = FrameTimestampsFactory.parse(ARR_ARGS_PARSE_3);
        FrameTimestampsFactory  facTest4 = FrameTimestampsFactory.parse(ARR_ARGS_PARSE_4);
        FrameTimestampsFactory  facTest5 = FrameTimestampsFactory.parse(ARR_ARGS_PARSE_5);
        
        Assert.assertEquals(facExpect1, facTest1);
        Assert.assertEquals(facExpect2, facTest2);
        Assert.assertEquals(facExpect3, facTest3);
        Assert.assertEquals(facExpect4, facTest4);
        Assert.assertEquals(facExpect5, facTest5);
    }

}
