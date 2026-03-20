/*
 * Project: dp-jal
 * File:	DataRequestSpecTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.requests
 * Type: 	DataRequestSpecTest
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
 * @since Mar 17, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.requests;

import java.lang.reflect.MalformedParametersException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.naming.ConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.query.DpDataRequest;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.archive.JalToolsTestArchiveConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for record <code>DataRequestSpec</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 17, 2026
 *
 */
public class DataRequestSpecTest {
    
    
    //
    // JAL Library Resources
    //
    
    /** The Data Platform Test Archive default configuration */
    private static final JalToolsTestArchiveConfig      CFG_ARC = JalToolsConfig.getInstance().testArchive;

    
    //
    // Test Constants
    //
    
    /** The default PV prefix used for data sources within the Test Archive */
    public static final String      STR_ARC_PV_PRFX = CFG_ARC.pvs.prefix;
    
    /** The total number of PV names in the Test Archive */
    public static final int         CNT_ARC_PV_NMS = CFG_ARC.pvs.count.total;
    
    
    /** The default start time instant of the Test Archive */
    public static final Instant     INS_ARC_START = CFG_ARC.range.startInstant();
    
    /** The default time range duration of the Test Archive */
    public static final Duration    DUR_ARC_RANGE = CFG_ARC.range.rangeDuration();

    
    //
    // Test Resources
    //
    
    /** The Data Request ID used in parsing tests */
    public static final String      STR_PARSE_RQST_ID = "JAL-Unit:0x11";
    
    /** The PV name prefix used in parsing tests */
    public static final String      STR_PARSE_PRFX = "PvParse:";
    
    
    /** The start instant string and resulting parsed <code>Instant</code> used in parsing tests */
    public static final String      STR_PARSE_INS_START = "2026-03-20T15:15:02.000+00:00";
    public static final Instant     INS_PARSE_START = Instant.parse(STR_PARSE_INS_START);
    
    /** The time range duration string and resulting parsed <code>Duration</code> used in parsing tests */ 
    public static final String      STR_PARSE_DUR_RANGE = "P1DT2H3M4.56S";
    public static final Duration    DUR_PARSE_RANGE = Duration.parse(STR_PARSE_DUR_RANGE);
    
    
    /** Parsing string used for parsing test */
    public static final String[]    ARR_PARSE_1 = {STR_PARSE_RQST_ID, "43" }; 
    
    /** Parsing string used for parsing test */
    public static final String[]    ARR_PARSE_2 = {STR_PARSE_RQST_ID, "43", STR_PARSE_PRFX }; 
    
    /** Parsing string used for parsing test */
    public static final String[]    ARR_PARSE_3 = {STR_PARSE_RQST_ID, "43", STR_PARSE_PRFX, STR_PARSE_DUR_RANGE }; 
    
    /** Parsing string used for parsing test */
    public static final String[]    ARR_PARSE_4 = {STR_PARSE_RQST_ID, "43", STR_PARSE_PRFX, STR_PARSE_DUR_RANGE, STR_PARSE_INS_START }; 
    
    /** Parsing string used for parsing test */
    public static final String[]    ARR_PARSE_5 = {STR_PARSE_RQST_ID, "PV:1", "PV:2", "PV:3", "PV:4" }; 
    
    /** Parsing string used for parsing test */
    public static final String[]    ARR_PARSE_6 = {STR_PARSE_RQST_ID, "PV:1", "PV:2", "PV:3", "PV:1" }; 
    
    /** Parsing string used for parsing test */
    public static final String[]    ARR_PARSE_7 = {STR_PARSE_RQST_ID, "PV:1", "PV:2", "PV:3", STR_PARSE_DUR_RANGE }; 
    
    /** Parsing string used for parsing test */
    public static final String[]    ARR_PARSE_8 = {STR_PARSE_RQST_ID, "PV:1", "PV:2", "PV:3", STR_PARSE_DUR_RANGE, STR_PARSE_INS_START }; 
    
    
    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#from(java.lang.String, int)}.
     */
    @Test
    public final void testFromStringInt() {
        
        // Test Parameters
        final String    strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final int       cntPvs = 100;
        
        final Set<String>   setPvNms = this.createPvNames(cntPvs);
        final Instant       insStart = INS_ARC_START;
        final Duration      durRange = DUR_ARC_RANGE;
        
        // Create data request specification and check
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, cntPvs);
        
        Assert.assertEquals(strRqstId, specTest.strRqstId());
        Assert.assertEquals(setPvNms, specTest.setPvNms());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(durRange, specTest.durRange());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#from(java.lang.String, int, java.lang.String)}.
     */
    @Test
    public final void testFromStringIntString() {
        
        // Test Parameters
        final String    strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final int       cntPvs = 100;
        final String    strPrfx = "PvUnitTest:";
        
        final Set<String>   setPvNms = this.createPvNames(cntPvs, strPrfx);
        final Instant       insStart = INS_ARC_START;
        final Duration      durRange = DUR_ARC_RANGE;
        
        // Create data request specification and check
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, cntPvs, strPrfx);
        
        Assert.assertEquals(strRqstId, specTest.strRqstId());
        Assert.assertEquals(setPvNms, specTest.setPvNms());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(durRange, specTest.durRange());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#from(java.lang.String, int, java.lang.String, java.time.Instant)}.
     */
    @Test
    public final void testFromStringIntStringInstant() {
        
        // Test Parameters
        final String    strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final int       cntPvs = 100;
        final String    strPrfx = "PvUnitTest:";
        final Instant   insStart = Instant.now();
        
        final Set<String>   setPvNms = this.createPvNames(cntPvs, strPrfx);
        final Duration      durRange = DUR_ARC_RANGE;
        
        // Create data request specification and check
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, cntPvs, strPrfx, insStart);
        
        Assert.assertEquals(strRqstId, specTest.strRqstId());
        Assert.assertEquals(setPvNms, specTest.setPvNms());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(durRange, specTest.durRange());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#from(java.lang.String, int, java.lang.String, java.time.Duration)}.
     */
    @Test
    public final void testFromStringIntStringDuration() {
        
        // Test Parameters
        final String    strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final int       cntPvs = 100;
        final String    strPrfx = "PvUnitTest:";
        final Duration  durRange = Duration.ofMillis(3);
        
        final Set<String>   setPvNms = this.createPvNames(cntPvs, strPrfx);
        final Instant       insStart = INS_ARC_START;
        
        // Create data request specification and check
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, cntPvs, strPrfx, durRange);
        
        Assert.assertEquals(strRqstId, specTest.strRqstId());
        Assert.assertEquals(setPvNms, specTest.setPvNms());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(durRange, specTest.durRange());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#from(java.lang.String, int, java.lang.String, java.time.Instant, java.time.Instant)}.
     */
    @Test
    public final void testFromStringIntStringInstantInstant() {
        
        // Test Parameters
        final String    strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final int       cntPvs = 100;
        final String    strPrfx = "PvUnitTest:";
        final Instant   insStart = Instant.parse("2026-03-20T15:15:02.000+00:00");
        final Instant   insEnd = Instant.now();
        
        final Set<String>   setPvNms = this.createPvNames(cntPvs, strPrfx);
        final Duration      durRange = Duration.between(insStart, insEnd);
        
        // Create data request specification and check
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, cntPvs, strPrfx, insStart, insEnd);
        
        Assert.assertEquals(strRqstId, specTest.strRqstId());
        Assert.assertEquals(setPvNms, specTest.setPvNms());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(durRange, specTest.durRange());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#from(java.lang.String, int, java.lang.String, java.time.Instant, java.time.Duration)}.
     */
    @Test
    public final void testFromStringIntStringInstantDuration() {
        
        // Test Parameters
        final String    strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final int       cntPvs = 100;
        final String    strPrfx = "PvUnitTest:";
        final Instant   insStart = Instant.parse("2026-03-20T15:15:02.000+00:00");
        final Duration  durRange = Duration.ofHours(5);
        
        final Set<String>   setPvNms = this.createPvNames(cntPvs, strPrfx);

        // Create data request specification and check
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, cntPvs, strPrfx, insStart, durRange);
        
        Assert.assertEquals(strRqstId, specTest.strRqstId());
        Assert.assertEquals(setPvNms, specTest.setPvNms());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(durRange, specTest.durRange());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#from(java.lang.String, java.util.Set)}.
     */
    @Test
    public final void testFromStringSetOfString() {
        
        // Test Parameters
        final int           cntPvs = 100;
        final String        strPrfx = "HappyPv:";

        final String        strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final Set<String>   setPvNms = this.createPvNames(cntPvs, strPrfx);
        
        final Instant       insStart = INS_ARC_START;
        final Duration      durRange = DUR_ARC_RANGE;
        
        // Create data request specification and check
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, setPvNms);
        
        Assert.assertEquals(strRqstId, specTest.strRqstId());
        Assert.assertEquals(setPvNms, specTest.setPvNms());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(durRange, specTest.durRange());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#from(java.lang.String, java.util.Set, java.time.Instant)}.
     */
    @Test
    public final void testFromStringSetOfStringInstant() {
        
        // Test Parameters
        final int           cntPvs = 100;
        final String        strPrfx = "HappyPv:";

        final String        strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final Set<String>   setPvNms = this.createPvNames(cntPvs, strPrfx);
        final Instant       insStart = Instant.now();
        
        final Duration      durRange = DUR_ARC_RANGE;
        
        // Create data request specification and check
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, setPvNms, insStart);
        
        Assert.assertEquals(strRqstId, specTest.strRqstId());
        Assert.assertEquals(setPvNms, specTest.setPvNms());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(durRange, specTest.durRange());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#from(java.lang.String, java.util.Set, java.time.Duration)}.
     */
    @Test
    public final void testFromStringSetOfStringDuration() {
        
        // Test Parameters
        final int           cntPvs = 100;
        final String        strPrfx = "HappyPv:";

        final String        strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final Set<String>   setPvNms = this.createPvNames(cntPvs, strPrfx);
        final Duration      durRange = Duration.ofMinutes(55);
        
        final Instant       insStart = INS_ARC_START;
        
        // Create data request specification and check
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, setPvNms, durRange);
        
        Assert.assertEquals(strRqstId, specTest.strRqstId());
        Assert.assertEquals(setPvNms, specTest.setPvNms());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(durRange, specTest.durRange());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#from(java.lang.String, java.util.Set, java.time.Instant, java.time.Instant)}.
     */
    @Test
    public final void testFromStringSetOfStringInstantInstant() {
        
        // Test Parameters
        final int           cntPvs = 100;
        final String        strPrfx = "HappyPv:";

        final String        strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final Set<String>   setPvNms = this.createPvNames(cntPvs, strPrfx);
        final Instant       insStart = Instant.parse("2026-03-20T15:15:02.000+00:00");
        final Instant       insEnd = Instant.now();
        final Duration      durRange = Duration.between(insStart, insEnd);
        
        // Create data request specification and check
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, setPvNms, insStart, insEnd);
        
        Assert.assertEquals(strRqstId, specTest.strRqstId());
        Assert.assertEquals(setPvNms, specTest.setPvNms());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(durRange, specTest.durRange());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#from(java.lang.String, java.util.Set, java.time.Instant, java.time.Duration)}.
     */
    @Test
    public final void testFromStringSetOfStringInstantDuration() {
        
        // Test Parameters
        final int           cntPvs = 100;
        final String        strPrfx = "HappyPv:";

        final String        strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final Set<String>   setPvNms = this.createPvNames(cntPvs, strPrfx);
        final Instant       insStart = Instant.now();
        final Duration      durRange = Duration.ofMinutes(23);
        
        // Create data request specification and check
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, setPvNms, insStart, durRange);
        
        Assert.assertEquals(strRqstId, specTest.strRqstId());
        Assert.assertEquals(setPvNms, specTest.setPvNms());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(durRange, specTest.durRange());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse1() {
        
        // Test Parameters
        final String[]      arrArgs = ARR_PARSE_1;
        
        final String        strRqstId = STR_PARSE_RQST_ID;
        final int           cntPvs = 43;
        final String        strPrfx = STR_ARC_PV_PRFX;
        final Set<String>   setPvNms = this.createPvNames(cntPvs, strPrfx);
        final Duration      durRange = DUR_ARC_RANGE;
        final Instant       insStart = INS_ARC_START;
        
        // Create data request specification and check fields
        try {
            DataRequestSpec     specTest = DataRequestSpec.parse(arrArgs);
            
            Assert.assertEquals(cntPvs, specTest.setPvNms().size());
            Assert.assertEquals(strRqstId, specTest.strRqstId());
            Assert.assertEquals(setPvNms, specTest.setPvNms());
            Assert.assertEquals(durRange, specTest.durRange());
            Assert.assertEquals(insStart, specTest.insStart());
            
        } catch (IllegalArgumentException | ConfigurationException | MalformedParametersException e) {
            Assert.fail("Parsing creation failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse2() {
        
        // Test Parameters
        final String[]      arrArgs = ARR_PARSE_2;
        
        final String        strRqstId = STR_PARSE_RQST_ID;
        final int           cntPvs = 43;
        final String        strPrfx = STR_PARSE_PRFX;
        final Set<String>   setPvNms = this.createPvNames(cntPvs, strPrfx);
        final Duration      durRange = DUR_ARC_RANGE;
        final Instant       insStart = INS_ARC_START;
        
        // Create data request specification and check fields
        try {
            DataRequestSpec     specTest = DataRequestSpec.parse(arrArgs);
            
            Assert.assertEquals(cntPvs, specTest.setPvNms().size());
            Assert.assertEquals(strRqstId, specTest.strRqstId());
            Assert.assertEquals(setPvNms, specTest.setPvNms());
            Assert.assertEquals(durRange, specTest.durRange());
            Assert.assertEquals(insStart, specTest.insStart());
            
        } catch (IllegalArgumentException | ConfigurationException | MalformedParametersException e) {
            Assert.fail("Parsing creation failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse3() {
        
        // Test Parameters
        final String[]      arrArgs = ARR_PARSE_3;
        
        final String        strRqstId = STR_PARSE_RQST_ID;
        final int           cntPvs = 43;
        final String        strPrfx = STR_PARSE_PRFX;
        final Set<String>   setPvNms = this.createPvNames(cntPvs, strPrfx);
        final Duration      durRange = DUR_PARSE_RANGE;
        final Instant       insStart = INS_ARC_START;
        
        // Create data request specification and check fields
        try {
            DataRequestSpec     specTest = DataRequestSpec.parse(arrArgs);
            
            Assert.assertEquals(cntPvs, specTest.setPvNms().size());
            Assert.assertEquals(strRqstId, specTest.strRqstId());
            Assert.assertEquals(setPvNms, specTest.setPvNms());
            Assert.assertEquals(durRange, specTest.durRange());
            Assert.assertEquals(insStart, specTest.insStart());
            
        } catch (IllegalArgumentException | ConfigurationException | MalformedParametersException e) {
            Assert.fail("Parsing creation failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse4() {
        
        // Test Parameters
        final String[]      arrArgs = ARR_PARSE_4;
        
        final String        strRqstId = STR_PARSE_RQST_ID;
        final int           cntPvs = 43;
        final String        strPrfx = STR_PARSE_PRFX;
        final Set<String>   setPvNms = this.createPvNames(cntPvs, strPrfx);
        final Duration      durRange = DUR_PARSE_RANGE;
        final Instant       insStart = INS_PARSE_START;
        
        // Create data request specification and check fields
        try {
            DataRequestSpec     specTest = DataRequestSpec.parse(arrArgs);
            
            Assert.assertEquals(cntPvs, specTest.setPvNms().size());
            Assert.assertEquals(strRqstId, specTest.strRqstId());
            Assert.assertEquals(setPvNms, specTest.setPvNms());
            Assert.assertEquals(durRange, specTest.durRange());
            Assert.assertEquals(insStart, specTest.insStart());
            
        } catch (IllegalArgumentException | ConfigurationException | MalformedParametersException e) {
            Assert.fail("Parsing creation failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse5() {
        
        // Test Parameters
        final String[]      arrArgs = ARR_PARSE_5;
        
        final String        strRqstId = STR_PARSE_RQST_ID;
        final int           cntPvs = 4;
        final Set<String>   setPvNms = Set.of("PV:1", "PV:2", "PV:3", "PV:4");
        final Duration      durRange = DUR_ARC_RANGE;
        final Instant       insStart = INS_ARC_START;
        
        // Create data request specification and check fields
        try {
            DataRequestSpec     specTest = DataRequestSpec.parse(arrArgs);
            
            Assert.assertEquals(cntPvs, specTest.setPvNms().size());
            Assert.assertEquals(strRqstId, specTest.strRqstId());
            Assert.assertEquals(setPvNms, specTest.setPvNms());
            Assert.assertEquals(durRange, specTest.durRange());
            Assert.assertEquals(insStart, specTest.insStart());
            
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | ConfigurationException | MalformedParametersException e) {
            Assert.fail("Parsing creation failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse6() {
        
        // Test Parameters
        final String[]      arrArgs = ARR_PARSE_6;
        
        final String        strRqstId = STR_PARSE_RQST_ID;
        final int           cntPvs = 3;
        final Set<String>   setPvNms = Set.of("PV:1", "PV:2", "PV:3");
        final Duration      durRange = DUR_ARC_RANGE;
        final Instant       insStart = INS_ARC_START;
        
        // Create data request specification and check fields
        try {
            DataRequestSpec     specTest = DataRequestSpec.parse(arrArgs);
            
            Assert.assertEquals(cntPvs, specTest.setPvNms().size());
            Assert.assertEquals(strRqstId, specTest.strRqstId());
            Assert.assertEquals(setPvNms, specTest.setPvNms());
            Assert.assertEquals(durRange, specTest.durRange());
            Assert.assertEquals(insStart, specTest.insStart());
            
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | ConfigurationException | MalformedParametersException e) {
            Assert.fail("Parsing creation failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse7() {
        
        // Test Parameters
        final String[]      arrArgs = ARR_PARSE_7;
        
        final String        strRqstId = STR_PARSE_RQST_ID;
        final int           cntPvs = 3;
        final Set<String>   setPvNms = Set.of("PV:1", "PV:2", "PV:3");
        final Duration      durRange = DUR_PARSE_RANGE;
        final Instant       insStart = INS_ARC_START;
        
        // Create data request specification and check fields
        try {
            DataRequestSpec     specTest = DataRequestSpec.parse(arrArgs);
            
            Assert.assertEquals(cntPvs, specTest.setPvNms().size());
            Assert.assertEquals(strRqstId, specTest.strRqstId());
            Assert.assertEquals(setPvNms, specTest.setPvNms());
            Assert.assertEquals(durRange, specTest.durRange());
            Assert.assertEquals(insStart, specTest.insStart());
            
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | ConfigurationException | MalformedParametersException e) {
            Assert.fail("Parsing creation failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse8() {
        
        // Test Parameters
        final String[]      arrArgs = ARR_PARSE_8;
        
        final String        strRqstId = STR_PARSE_RQST_ID;
        final int           cntPvs = 3;
        final Set<String>   setPvNms = Set.of("PV:1", "PV:2", "PV:3");
        final Duration      durRange = DUR_PARSE_RANGE;
        final Instant       insStart = INS_PARSE_START;
        
        // Create data request specification and check fields
        try {
            DataRequestSpec     specTest = DataRequestSpec.parse(arrArgs);
            
            Assert.assertEquals(cntPvs, specTest.setPvNms().size());
            Assert.assertEquals(strRqstId, specTest.strRqstId());
            Assert.assertEquals(setPvNms, specTest.setPvNms());
            Assert.assertEquals(durRange, specTest.durRange());
            Assert.assertEquals(insStart, specTest.insStart());
            
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | ConfigurationException | MalformedParametersException e) {
            Assert.fail("Parsing creation failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#equals(java.lang.Object)}.
     */
    @Test
    public final void testEquals() {
        
        // Test Parameters
        final String    strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final int       cntPvs = 100;
        
        final Set<String>   setPvNms = this.createPvNames(cntPvs);
        final Instant       insStart = INS_ARC_START;
        final Duration      durRange = DUR_ARC_RANGE;
        
        // Create data request specification with default parameters and one with explicit parameters and check
        DataRequestSpec     specTest1 = DataRequestSpec.from(strRqstId, cntPvs);
        DataRequestSpec     specTest2 = DataRequestSpec.from(strRqstId, setPvNms, insStart, durRange);
        
        Assert.assertEquals(specTest1, specTest2);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#endTime()}.
     */
    @Test
    public final void testEndTime() {
        
        // Test Parameters
        final String    strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final int       cntPvs = 100;
        
        final Instant       insStart = INS_ARC_START;
        final Duration      durRange = DUR_ARC_RANGE;
        
        final Instant       insEnd = insStart.plus(durRange);
        
        // Create data request specification with default parameters and one with explicit parameters and check
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, cntPvs);
        
        Assert.assertEquals(insEnd, specTest.endTime());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#pvNamesList()}.
     */
    @Test
    public final void testPvNamesList() {
        
        // Test Parameters
        final String    strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final int       cntPvs = 100;
        final String    strPrfx = "MyUnitTestPv:";
        
        final List<String>  lstPvNms = IntStream.range(0, cntPvs).mapToObj(i -> strPrfx + Integer.toString(i)).toList();
        
        // Create data request specification and check PV name list contents
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, cntPvs, strPrfx);
        
        Assert.assertTrue(specTest.setPvNms().containsAll(lstPvNms));
        Assert.assertTrue(lstPvNms.containsAll(specTest.setPvNms()));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#createRequest()}.
     */
    @Test
    public final void testCreateRequest() {
        
        // Test Parameters
        final String    strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final int       cntPvs = 100;
        
        final Set<String>   setPvNms = this.createPvNames(cntPvs);
        final Instant       insStart = INS_ARC_START;
        final Duration      durRange = DUR_ARC_RANGE;
        final Instant       insEnd = insStart.plus(durRange);
        
        // Create data request specification with most default parameters and check
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, cntPvs);
        
        Assert.assertEquals(strRqstId, specTest.strRqstId());
        Assert.assertEquals(setPvNms, specTest.setPvNms());
        Assert.assertEquals(insStart, specTest.insStart());
        Assert.assertEquals(durRange, specTest.durRange());
        
        // Create time-series data request and check
        DpDataRequest   rqstTest = specTest.createRequest();
        
        Assert.assertEquals(insStart, rqstTest.getInitialTime());
        Assert.assertEquals(insEnd, rqstTest.getFinalTime());
        Assert.assertEquals(durRange, rqstTest.rangeDuration());
        Assert.assertTrue(rqstTest.getSourceNames().containsAll(setPvNms));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.requests.DataRequestSpec#printOut(java.io.PrintStream, java.lang.String)}.
     */
    @Test
    public final void testPrintOut() {
        
        // Test Parameters
        final String    strRqstId = JavaRuntime.getQualifiedMethodNameSimple();
        final int       cntPvs = 100;
        
        // Create data request specification with most default parameters and check
        DataRequestSpec     specTest = DataRequestSpec.from(strRqstId, cntPvs);

        // Print out data request specification to stdout
        specTest.printOut(System.out, "  ");
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates and returns a set of <code>cntPvs</code> PV names using the default prefix <code>{@link #STR_ARC_PV_PRFX}</code>.
     * </p>
     * <p>
     * Defers to <code>{@link #createPvNames(int, String)}</code> with <code>{@link #STR_ARC_PV_PRFX}</code> as second argument.
     * 
     * @param cntPvs    number of PV names to create
     * @param strPvPrfx suffix given to all PV names
     * 
     * @return  a set of PV names with index 0 to <code>cntPvs</code> - 1
     */
    private Set<String> createPvNames(int cntPvs) {
        return this.createPvNames(cntPvs, STR_ARC_PV_PRFX);
    }
    
    /**
     * <p>
     * Creates and returns a set of <code>cntPvs</code> PV names with the prefix <code>strPvPrfx</code> and suffix given by index.
     * </p>
     *  
     * @param cntPvs    number of PV names to create
     * @param strPvPrfx suffix given to all PV names
     * 
     * @return  a set of PV names with index 0 to <code>cntPvs</code> - 1
     */
    private Set<String> createPvNames(int cntPvs, String strPvPrfx) {
        Set<String>     setPvNms = IntStream.range(0, cntPvs).mapToObj(i -> strPvPrfx + Integer.toString(i)).collect(Collectors.toSet());
        
        return setPvNms;
    }

}
