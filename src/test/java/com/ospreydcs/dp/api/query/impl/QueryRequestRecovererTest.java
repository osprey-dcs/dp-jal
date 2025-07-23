/*
 * Project: dp-api-common
 * File:	QueryRequestRecovererTest.java
 * Package: com.ospreydcs.dp.api.query.impl
 * Type: 	QueryRequestRecovererTest
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
 * @since Apr 24, 2025
 *
 */
package com.ospreydcs.dp.api.query.impl;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>QueryRequeestProcessorNew</code>.
 * </p>
 * <p>
 * <h2>WARNINGS:</h2>
 * For test cases to pass the following conditions must be current:
 * <ul>
 * <li>
 * A Query Service must be running at the default port location and with default parameter set.
 * </li>
 * <li>
 * The Data Platform archive must be populated with test data from the <em>app-run-test-data-generator</em>
 * application within the Data Platform installer.
 * </li>
 * </ul>
 * Due to the large size of some test requests (RQST_HALF_SRC, RQST_HALF_RNG) heap space issues may arise,
 * especially with static data tables.  The following are Java command line options to manage heap space
 * and garbage collection:
 * <ul>
 * <li>-XX:GCTimeRatio=19 - sets the garbage collection runtime to 1/20 of the application runtime.</li> 
 * <li>-XX:MaxHeapFreeRatio=70 - sets the maximum ratio (%) of heap space to live object before garbage collection.</li>
 * <li>-XX:MinHeapFreeRatio=40 - sets the minimum ratio (%) of heap space to live object before garbage collection.</li>
 * <li>-Xmx4096m - sets the maximum heap space size to 4 GBytes.</li>
 * <li>-Xms4096m - sets the minimum heap space size to 4 GBytes.</li>
 * </ul>
 * <p>
 *
 * @author Christopher K. Allen
 * @since Apr 24, 2025
 *
 */
public class QueryRequestRecovererTest {


    //
    // Application Resources
    //
    
    /** The Data Platform API default configuration parameter set */
    public static final DpQueryConfig  CFG_QUERY = DpApiConfig.getInstance().query;

    
    //
    // Class Constants
    //
    
    /** Location of performance results output */
    public static final String          STR_PATH_OUTPUT = "test/output/query/impl/";
    
    
    /** Large test request */
    public static final DpDataRequest   RQST_BIG = TestQueryResponses.QREC_BIG.createRequest();
    
    /** Huge test request */
    public static final DpDataRequest   RQST_HUGE = TestQueryResponses.QREC_HUGE.createRequest();
    
    /** The data request for the half the test archive - used for timing responses */
    public static final DpDataRequest   RQST_HALF_SRC = TestQueryResponses.QREC_HALF_SRC.createRequest();

    /** The data request for the half the test archive - used for timing responses */
    public static final DpDataRequest   RQST_HALF_RNG = TestQueryResponses.QREC_HALF_RNG.createRequest();

    /** The data request for the entire test archive - used for timing responses */
    public static final DpDataRequest   RQST_ALL = TestQueryResponses.QREC_ALL.createRequest();

    
    /** Common List of data requests used for testing */
    public static final List<DpDataRequest>         LST_RQSTS = List.of(RQST_HALF_SRC, RQST_HALF_RNG);
    
    
    /** Configuration - List of all maximum data stream counts for request recovery */
//    public static final List<Integer>           LST_CFG_MAX_STRMS = List.of(1, 2, 3, 4, 5);
    public static final List<Integer>           LST_CFG_MAX_STRMS = List.of(2, 3);
    
    /** Configuration - List of all maximum data source counts for composite requests */
//    public static final List<Integer>           LST_CFG_MAX_SRCS = List.of(500, 1000, 2000, 4000);
    public static final List<Integer>           LST_CFG_MAX_SRCS = List.of(2000);
    
    /** Configuration - List of maximum time range duration for composite requests */
//    public static final List<Duration>          LST_CFG_MAX_DUR = List.of(Duration.ofSeconds(1L), Duration.ofSeconds(2L), Duration.ofSeconds(3L), Duration.ofSeconds(5L));
    public static final List<Duration>          LST_CFG_MAX_DUR = List.of(Duration.ofSeconds(5L));
    
    /** Configuration - List of all common configuration records - built in static section */
    public static final List<TestConfig>        LST_CFG_TEST_CASES = new LinkedList<>();
    
    
    /** The "target data rate" for the request processor (in MBps) */
    public static final double                  DBL_DATA_RATE_TARGET = 200.0;
    
    
    /** Test Case - List of gRPC data stream types used for request recovery */
    public static final List<DpGrpcStreamType>  LST_STRM_TYP = List.of(DpGrpcStreamType.BACKWARD, DpGrpcStreamType.BIDIRECTIONAL);

    
    /** Test Case - List of multi-streaming gRPC data streams */
//    public static final List<Integer>           LST_MSTRM_CNT_STRMS = List.of(1, 2, 3, 4, 5);
    public static final List<Integer>           LST_MSTRM_CNT_STRMS = List.of(2, 3);
    
    /** Test Case - List of request domain decomposition strategies */
    public static final List<RequestDecompType> LST__MSTRM_DCMP_TYP = List.of(RequestDecompType.HORIZONTAL, RequestDecompType.VERTICAL);
    
    /** Test Case - List of all common test case records - built in static section */
    public static final List<TestCase>          LST_MSTRM_TST_CASES = new LinkedList<>();
    
    
    /** Universal enable/disable recovery request with multiple gRPC data streams flag */
    public static final boolean                 BOL_UNIV_RCVR_MULT = true;
    
    /** Universal enable/disable correlation concurrency flag */
    public static final boolean                 BOL_UNIVR_CORR_CONC = true;
    
    /** Universal enable/disable correlation while streaming flag */
    public static final boolean                 BOL_UNIVR_CORR_STRM = true;
    
    /** Universal multi-stream test configuration */
    public static final TestConfig              REC_UNIVR_MSTRM_CFG = TestConfig.from(BOL_UNIV_RCVR_MULT, BOL_UNIVR_CORR_CONC, BOL_UNIVR_CORR_STRM, 5, 4000, Duration.ofSeconds(5L));
    
    
    /*
     * Static block - Initialize configuration and multi-stream test cases
     */
    static {
        
        // Assign names to the common requests
        RQST_BIG.setRequestId("RQST_BIG");
        RQST_HUGE.setRequestId("RQST_HUGE");
        RQST_HALF_SRC.setRequestId("RQST_HALF_SRC");
        RQST_HALF_RNG.setRequestId("RQST_HALF_RNG");
        RQST_ALL.setRequestId("RQST_ALL");
        
        // Populate the test configurations
        List<Boolean>   lstBool = List.of(false, true);
        
        for (Integer cntMaxStrms : LST_CFG_MAX_STRMS)
            for (Integer cntMaxSrcs : LST_CFG_MAX_SRCS)
                for (Duration durMaxRng : LST_CFG_MAX_DUR)
                    for (Boolean bolCorrCon : lstBool) 
                        for (Boolean bolCorrStrm : lstBool) {

                            TestConfig  recCfg = TestConfig.from(BOL_UNIV_RCVR_MULT, 
                                    bolCorrCon, 
                                    bolCorrStrm, 
                                    cntMaxStrms, 
                                    cntMaxSrcs, 
                                    durMaxRng);

                            LST_CFG_TEST_CASES.add(recCfg);
                        }
        
        // Populate the explicit multi-streaming test cases, building the composite request
        for (DpDataRequest rqst : LST_RQSTS)
            for (DpGrpcStreamType enmStrType : LST_STRM_TYP)
                for (RequestDecompType enmDcmpType : LST__MSTRM_DCMP_TYP)
                    for (Integer cntStrms : LST_MSTRM_CNT_STRMS) {
                        
                        TestCase    recCase = TestCase.from(rqst, enmDcmpType, enmStrType, cntStrms);
                        
                        LST_MSTRM_TST_CASES.add(recCase);
                    }
    }
    
    
    //
    // Test Fixture Resources
    //
    
    /** The single connection to the Query Service used by all test cases */
    private static DpQueryConnection            connQuery;
    
    /** The QueryRequestProcessorOld under test */
    private static QueryRequestRecoverer     prcrQuery;
    
    /** Print output stream for storing evaluation results to single file */
    private static PrintStream                  psOutput;
    
    
    //
    // Test Fixture
    //
        
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        // Create the Query Service connection
        connQuery = DpQueryConnectionFactory.FACTORY.connect();
        
        // Create the test query response processor
        prcrQuery = QueryRequestRecoverer.from(connQuery);
        
        // Open the common output file
        String  strFileName = QueryRequestRecovererTest.class.getSimpleName() + "-" + Instant.now().toString() + ".txt";
        Path    pathOutput = Paths.get(STR_PATH_OUTPUT, strFileName);
        File    fileOutput = pathOutput.toFile();
        
        psOutput = new PrintStream(fileOutput);
        
        // Write header
        DateFormat  date = DateFormat.getDateTimeInstance();
        String      strDateTime = date.format(new Date());
        psOutput.println(QueryRequestRecovererTest.class.getSimpleName() + ": " + strDateTime);
        psOutput.println("  Multi-streaming request recovery : " + BOL_UNIV_RCVR_MULT);
        psOutput.println("  Correlate using multiple threads : " + BOL_UNIVR_CORR_CONC);
        psOutput.println("  Correlate while streaming        : " + BOL_UNIVR_CORR_STRM);
        psOutput.println();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        connQuery.shutdownSoft();
        psOutput.close();
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
     * Confirms immutability of <code>Integer</code> objects within map container.
     */
    @Test
    public final void testIntegerMap() {
        
        // Parameters
        String  str1 = "One";
        String  str2 = "Two";
        String  str3 = "Three";
        List<String>            lstStrs = List.of(str1, str2, str3);
        Map<String, Integer>    mapStrToInt = new HashMap<>();
        
        // Populate map
        int     intVal = 1;
        for (String str : lstStrs)
            mapStrToInt.put(str, intVal++);
//        mapStrToInt.put(str1, 1);
//        mapStrToInt.put(str2, 2);
//        mapStrToInt.put(str3, 3);
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Map before increment: " + mapStrToInt);
       
        // Attempt direct map value increment
        mapStrToInt.values().forEach(i -> i++);
        System.out.println("  Map after 1st increment attempt: " + mapStrToInt);
        
        // Attempt map value retrieval then increment
        for (String str : lstStrs) {
            Integer i = mapStrToInt.get(str);
            
            if (i == null)
                Assert.fail("Map value for key " + str + " missing.");
            i++;
        }
        System.out.println("  Map after 2nd increment attempt: " + mapStrToInt);
        
        // Attempt map value retrieval and replacement
        for (String str : lstStrs) {
            Integer i = mapStrToInt.get(str);
            
            if (i == null)
                Assert.fail("Map value for key " + str + " missing.");
            
            Integer iplus = ++i;
            mapStrToInt.put(str, iplus);
        }
        System.out.println("  Map after increment and replacement: " + mapStrToInt);
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer#from(com.ospreydcs.dp.api.grpc.query.DpQueryConnection)}.
     */
    @Test
    public final void testFrom() {
        QueryRequestRecoverer    prcrTest = QueryRequestRecoverer.from(connQuery);
        
        Assert.assertNotNull(prcrTest);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer#QueryRequestProcessorNew(com.ospreydcs.dp.api.grpc.query.DpQueryConnection)}.
     */
    @Test
    public final void testQueryRequestProcessorNew() {
        QueryRequestRecoverer    prcrTest = new QueryRequestRecoverer(connQuery);
        
        Assert.assertNotNull(prcrTest);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer#resetDefaultConfiguration()}.
     */
    @Test
    public final void testResetDefaultConfiguration() {

        // Independent processor instance
        QueryRequestRecoverer    prcrTest = QueryRequestRecoverer.from(connQuery);
        
        Assert.assertNotNull(prcrTest);
        
        // Parameters
        final boolean bolMultStr = prcrTest.isMultiStreaming();
        final long    lngDomSize = prcrTest.getMultiStreamingDomainSize();
        final long    lngNewSize = lngDomSize/2;
        final int     cntMaxStrs = prcrTest.getMultiStreamingMaxStreamCount();
        final int     cntNewStrs = cntMaxStrs * 2;
        final int     cntMaxSrcs = prcrTest.getRequestDecompMaxPvCount();
        final int     cntNewSrcs = cntMaxSrcs * 2;
        final boolean bolCorrCon = prcrTest.isCorrelateConcurrencyEnabled();
        final boolean bolCorrStr = prcrTest.isCorrelatingWhileStreaming();
        final int     cntMaxThds = prcrTest.getCorrelateConcurrencyMaxThreads();
        final int     cntNexThds = cntMaxThds/2;
        final Duration    durMaxRng = prcrTest.getRequestDecompMaxTimeRange();
        final Duration    durNewRng = durMaxRng.dividedBy(2L);
        
        // Change configuration from default and confirm
        prcrTest.enableMultiStreaming(!bolMultStr);
        prcrTest.setMultiStreamingDomainSize(lngNewSize);
        prcrTest.setMultiStreamingMaxStreamCount(cntNewStrs);
        prcrTest.setRequestDecompMaxPvCount(cntNewSrcs);
        prcrTest.enableCorrelateConcurrency(!bolCorrCon);
        prcrTest.enableCorrelateWhileStreaming(!bolCorrStr);
        prcrTest.setCorrelateConcurrencyMaxThreads(cntNexThds);
        prcrTest.setRequestDecompMaxTimeRange(durNewRng);
        
        Assert.assertTrue(prcrTest.isMultiStreaming() != bolMultStr);
        Assert.assertEquals(lngNewSize, prcrTest.getMultiStreamingDomainSize());
        Assert.assertEquals(cntNewStrs, prcrTest.getMultiStreamingMaxStreamCount());
        Assert.assertEquals(cntNewSrcs, prcrTest.getRequestDecompMaxPvCount());
        Assert.assertTrue(prcrTest.isCorrelateConcurrencyEnabled() != bolCorrCon);
        Assert.assertTrue(prcrTest.isCorrelatingWhileStreaming() != bolCorrStr);
        Assert.assertEquals(cntNexThds, prcrTest.getCorrelateConcurrencyMaxThreads());
        Assert.assertEquals(durNewRng, prcrTest.getRequestDecompMaxTimeRange());
        
        // Reset to default configuration and confirm
        prcrTest.resetDefaultConfiguration();
        
        Assert.assertTrue(prcrTest.isMultiStreaming() == CFG_QUERY.data.recovery.multistream.enabled);
        Assert.assertEquals(CFG_QUERY.data.recovery.multistream.sizeDomain.longValue(), prcrTest.getMultiStreamingDomainSize());
        Assert.assertEquals(CFG_QUERY.data.recovery.multistream.maxStreams.intValue(), prcrTest.getMultiStreamingMaxStreamCount());
        Assert.assertEquals(CFG_QUERY.data.request.decompose.maxSources.intValue(), prcrTest.getRequestDecompMaxPvCount());
        Assert.assertTrue(CFG_QUERY.data.recovery.correlate.concurrency.enabled == prcrTest.isCorrelateConcurrencyEnabled());
        Assert.assertTrue(CFG_QUERY.data.recovery.correlate.whileStreaming == prcrTest.isCorrelatingWhileStreaming());
        Assert.assertEquals(CFG_QUERY.data.recovery.multistream.maxStreams.intValue(), prcrTest.getMultiStreamingMaxStreamCount());
        Assert.assertEquals(CFG_QUERY.data.request.decompose.maxDuration, prcrTest.getRequestDecompMaxTimeRange().getSeconds());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer#enableMultiStreaming(boolean)}.
     */
    @Test
    public final void testEnableMultiStreaming() {
        
        // Parameters
        final boolean bolEnabled = prcrQuery.isMultiStreaming();
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default multi-streaming enabled: " + bolEnabled);
        
        prcrQuery.enableMultiStreaming(!bolEnabled);
        
        Assert.assertTrue(prcrQuery.isMultiStreaming() != bolEnabled);
        
        prcrQuery.enableMultiStreaming(bolEnabled);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer#setMultiStreamingDomainSize(long)}.
     */
    @Test
    public final void testSetMultiStreamingDomainSize() {
        
        // Parameters
        final long    lngDomSize = prcrQuery.getMultiStreamingDomainSize();
        final long    lngNewSize = lngDomSize/2;
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default multi-streaming domain size: " + lngDomSize);
        
        prcrQuery.setMultiStreamingDomainSize(lngNewSize);
        
        Assert.assertEquals(lngNewSize, prcrQuery.getMultiStreamingDomainSize());
     
        prcrQuery.setMultiStreamingDomainSize(lngDomSize);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer#setMultiStreamingMaxStreamCount(int)}.
     */
    @Test
    public final void testSetMaxStreamCount() {
        
        // Parameters
        final int     cntMaxStrs = prcrQuery.getMultiStreamingMaxStreamCount();
        final int     cntNewStrs = cntMaxStrs * 2;
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default multi-streaming maximum stream count: " + cntMaxStrs);
        
        prcrQuery.setMultiStreamingMaxStreamCount(cntNewStrs);
        
        Assert.assertEquals(cntNewStrs, prcrQuery.getMultiStreamingMaxStreamCount());
        
        prcrQuery.setMultiStreamingMaxStreamCount(cntMaxStrs);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer#setRequestDecompMaxPvCount(int)}.
     */
    @Test
    public final void testSetMaxDataSourceCount() {
        
        // Parameters
        final int     cntMaxSrcs = prcrQuery.getRequestDecompMaxPvCount();
        final int     cntNewSrcs = cntMaxSrcs * 2;
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default maximum data source count: " + cntMaxSrcs);
        
        prcrQuery.setRequestDecompMaxPvCount(cntNewSrcs);
        
        Assert.assertEquals(cntNewSrcs, prcrQuery.getRequestDecompMaxPvCount());
        
        prcrQuery.setRequestDecompMaxPvCount(cntMaxSrcs);
        Assert.assertEquals(cntMaxSrcs, prcrQuery.getRequestDecompMaxPvCount());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer#setRequestDecompMaxTimeRange(java.time.Duration)}.
     */
    @Test
    public final void testSetMaxTimeRange() {
        
        // Parameters
        final Duration    durMaxRng = prcrQuery.getRequestDecompMaxTimeRange();
        final Duration    durNewRng = durMaxRng.dividedBy(2L);
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default maximum time range duration: " + durMaxRng);
        
        prcrQuery.setRequestDecompMaxTimeRange(durNewRng);
        
        Assert.assertEquals(durNewRng, prcrQuery.getRequestDecompMaxTimeRange());
        
        prcrQuery.setRequestDecompMaxTimeRange(durMaxRng);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer#enableCorrelateConcurrency(boolean)}.
     */
    @Test
    public final void testEnableCorrelateConcurrently() {
        
        // Parameters
        final boolean bolEnabled = prcrQuery.isCorrelateConcurrencyEnabled();
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default correlate concurrently: " + bolEnabled);
        
        prcrQuery.enableCorrelateConcurrency(!bolEnabled);
        
        Assert.assertTrue(prcrQuery.isCorrelateConcurrencyEnabled() != bolEnabled);
        
        prcrQuery.enableCorrelateConcurrency(bolEnabled);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer#enableCorrelateWhileStreaming(boolean)}.
     */
    @Test
    public final void testEnableCorrelateWhileStreaming() {
        
        // Parameters
        final boolean bolEnabled = prcrQuery.isCorrelatingWhileStreaming();
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default correlate while streaming: " + bolEnabled);
        
        prcrQuery.enableCorrelateWhileStreaming(!bolEnabled);
        
        Assert.assertTrue(prcrQuery.isCorrelatingWhileStreaming() != bolEnabled);
        
        prcrQuery.enableCorrelateWhileStreaming(bolEnabled);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer#setCorrelateConcurrencyMaxThreads(int)}.
     */
    @Test
    public final void testSetCorrelateMaxThreads() {
        
        // Parameters
        final int   cntMaxThds = prcrQuery.getCorrelateConcurrencyMaxThreads();
        final int   cntNexThds = cntMaxThds/2;
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Default correlate max thread count: " + cntMaxThds);
        
        prcrQuery.setCorrelateConcurrencyMaxThreads(cntNexThds);
        
        Assert.assertEquals(cntNexThds, prcrQuery.getCorrelateConcurrencyMaxThreads());
        
        prcrQuery.setCorrelateConcurrencyMaxThreads(cntMaxThds);
    }


    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer#processRequest(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequest() {
        
        // Test Parameters
        final PrintStream   ps = psOutput;
//        final boolean       bolMultStrm = BOL_UNIV_RCVR_MULT;
        final int           cntCases = LST_RQSTS.size() * LST_CFG_TEST_CASES.size();  
        
        // Test resource - we are changing configuration so we use an independent processor
        final QueryRequestRecoverer  prcrTest = QueryRequestRecoverer.from(connQuery);
        final SortedSet<ConfigResult>   setResults = new TreeSet<>(ConfigResult.ResultOrder.create());
        
//        prcrTest.enableMultiStreaming(bolMultStrm);
        
        // Print Header
        ps.println(JavaRuntime.getQualifiedMethodNameSimple());
        ps.println();
        
        int     indCase = 1;
        for (TestConfig recCfg : LST_CFG_TEST_CASES) { 
            
//            // Configure the processor
//            recCfg.configure(prcrTest);
            
            for (DpDataRequest rqst : LST_RQSTS) {
                String strRqstNm = rqst.getRequestId();

                System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Running Test Case #" + indCase + " of " + cntCases + " for request " + strRqstNm);
                try {
                    
                    ConfigResult    recResult = ConfigResult.evaluate(prcrTest, rqst, recCfg);
                    setResults.add(recResult);

                } catch (DpQueryException e) {
                    Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown during test case #" + indCase + " : " + e.getMessage());
                }

                indCase++;
            }
        }
        
        // Deal with the set of configuration results
        this.dealWithProcessRequestResults(ps, setResults);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer#processRequests(java.util.List)}.
     */
    @Test
    public final void testProcessRequests() {
        
        // Test Parameters
        final int           cntCases = LST_MSTRM_TST_CASES.size();
        final PrintStream   ps = psOutput;
//        final boolean       bolMultStrm = BOL_UNIV_RCVR_MULT;
//        final boolean       bolCorrCon = BOL_UNIVR_CORR_CONC;    
//        final boolean       bolCorrStrm = BOL_UNIVR_CORR_STRM;        
        
        ps.println(JavaRuntime.getQualifiedMethodNameSimple());
        ps.println();
        
        // Configure Correlator
//        QueryRequestRecovererTest.prcrQuery.enableMultiStreaming(bolMultStrm);
//        QueryRequestRecovererTest.prcrQuery.enableCorrelateConcurrently(bolCorrCon);
//        QueryRequestRecovererTest.prcrQuery.enableCorrelateWhileStreaming(bolCorrStrm);
        
        int     indCase = 1;
        for (TestCase recCase : LST_MSTRM_TST_CASES) {
            System.out.println("Test Case #" + indCase + " of " + cntCases);
            ps.println("Test Case #" + indCase + " of " + cntCases);

            try {

                ConfigResult    recResult = recCase.evaluate(prcrQuery, REC_UNIVR_MSTRM_CFG);
//                Instant insStart = Instant.now();
//                SortedSet<RawCorrelatedData>  setData = QueryRequestRecovererTest.prcrQuery.processRequests(recCase.lstCmpRqsts);
//                Instant insFinish = Instant.now();
//                
//                // Compute results
//                Duration    durRqst = Duration.between(insStart, insFinish);
//                int         cntMsgs = QueryRequestRecovererTest.prcrQuery.getProcessedMessageCount();
//                int         cntBlks = setData.size();
//                long        szAlloc = QueryRequestRecovererTest.prcrQuery.getProcessedByteCount();
//                
//                double      dblRateXmit  = ( ((double)szAlloc) * 1000 )/durRqst.toNanos();
//                
//                // Print out results
//                ps.println("  Recover w/ multi-stream : " + bolMultStrm);
//                ps.println("  Correlate w/ streaming  : " + bolCorrStrm);
//                ps.println("  Correlation concurrency : " + bolCorrCon);
//                ps.println("  Messages recovered      : " + cntMsgs);
//                ps.println("  Processed data blocks   : " + cntBlks);
//                ps.println("  Processed bytes         : " + szAlloc);
//                ps.println("  Request duration        : " + durRqst);
//                ps.println("  Processing rate (Mbps)  : " + dblRateXmit);
                recResult.printOut(ps, "");
                recCase.printConfiguration(ps);
//                this.printConfiguration(ps, recCase);
                ps.println();
                
            } catch (DpQueryException e) {
                Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown during request recovery #" + indCase + " : " + e.getMessage());
            }
            
            indCase++;
        }
    }
    

    // 
    // Support Methods
    //
    
    /**
     * <p>
     * Computes the <code>ConfigResult</code> scoring and prints out the scoring and results.
     * </p>
     * 
     * @param os            print stream to receive results output
     * @param setResults    the set of results from <code>{@link QueryRequestProcessorOld#processRequest(DpDataRequest)}</code>
     */
    private void    dealWithProcessRequestResults(PrintStream os, final SortedSet<ConfigResult> setResults) {
//        
//        // Compute general results and print them
//        int         cntResults = setResults.size();
//        double      dblRateAvg = setResults.stream().mapToDouble(rec -> rec.dblRate()).sum()/cntResults;
//        int         cntRatesAboveAvg = setResults.stream().filter(rec -> rec.dblRate() >= dblRateAvg).mapToInt(rec -> 1).sum();
//        int         cntRatesAbove200 = setResults.stream().filter(rec -> rec.dblRate() >= 200.0).mapToInt(rec -> 1).sum();
//        Duration    durRqstAvg = setResults.stream().<Duration>map(rec -> rec.durRequest()).reduce(Duration.ZERO, (r1,r2) -> r1.plus(r2)).dividedBy(cntResults);
//        
//        int         cntHalfSrc = 0;
//        int         cntHalfRng = 0;
//        for (ConfigResult recResult : setResults) {
//            if (recResult.request().equals(RQST_HALF_SRC))
//                cntHalfSrc++;
//            if (recResult.request().equals(RQST_HALF_RNG))
//                cntHalfRng++;
//        }
//        
//        Double  dblRateMax = null;
//        Double  dblRateMin = null;
//        Double  dblRateStd = 0.0;
//        for (ConfigResult recResult : setResults) {
//            double  dblRate = recResult.dblRate();
//            
//            if (dblRateMax == null)
//                dblRateMax = dblRate;
//            if (dblRateMin == null)
//                dblRateMin = dblRate;
//            if (dblRate > dblRateMax)
//                dblRateMax = dblRate;
//            if (dblRate < dblRateMin)
//                dblRateMin = dblRate;
//            
//            dblRateStd += (dblRate - dblRateAvg)*(dblRate - dblRateAvg);
//        }
//        dblRateStd = Math.sqrt(dblRateStd/cntResults);
//        
//        os.println();
//        os.println(JavaRuntime.getQualifiedMethodNameSimple() + " Configuration Results Average Scoring:");
//        os.println("    Total number of result cases : " + cntResults);
//        os.println("    Cases with rates >= Avg Rate : " + cntRatesAboveAvg);
//        os.println("    Cases with rates >= 200 Mbps : " + cntRatesAbove200);
//        os.println("    Average data rate (Mbps)     : " + dblRateAvg);
//        os.println("    Minimum data rate (Mbps)     : " + dblRateMin);
//        os.println("    Maximum data rate (Mbps)     : " + dblRateMax);
//        os.println("    Standard deviation (Mbps)    : " + dblRateStd);
//        os.println("    Average request duration     : " + durRqstAvg);
//        os.println("    Number of HALF_SRC requests  : " + cntHalfSrc);
//        os.println("    Number of HALF_RNG requests  : " + cntHalfRng);
//        
        // Compute summary of the results and print them out
        ResultSummary.assignTargetDataRate(DBL_DATA_RATE_TARGET);
        ResultSummary   recSummary  = ResultSummary.summarize(setResults);
        recSummary.printOut(os);
        
        // Get the data rates of interest 
        final double  dblRateAvg = recSummary.dblRateAvg();
        final double  dblRateTgt = DBL_DATA_RATE_TARGET;
        final double  dblRateBoth = Math.max(dblRateAvg, dblRateTgt);

        // Count results and print them
        int     cntAboveAvg = ConfigResult.countRatesGreaterEqual(setResults, dblRateAvg);
        int     cntAboveTgt = ConfigResult.countRatesGreaterEqual(setResults, dblRateTgt);
        int     cntBoth = ConfigResult.countRatesGreaterEqual(setResults, dblRateBoth);

        os.println();
        os.println("    Number of cases over average : " + cntAboveAvg);
        os.println("    Number of cases over target  : " + cntAboveTgt);
        os.println("    Number of cases over both    : " + cntBoth);
        
        
        // Score results and print them
        ConfigResultScore   scoreAll = ConfigResultScore.from(LST_RQSTS, setResults);
        ConfigResultScore   scoreAboveAvg = ConfigResultScore.from(LST_RQSTS, setResults, dblRateAvg);
        ConfigResultScore   scoreAboveTgt = ConfigResultScore.from(LST_RQSTS, setResults, dblRateTgt);
        
        ConfigResultScore.Score scoreBest = scoreAll.getScoreWithBestAvgRate();
        double                  dblRateMinBestConfig = scoreBest.getDataRateMin();
        ConfigResultScore       scoreBestAboveMin = ConfigResultScore.from(LST_RQSTS, setResults, dblRateMinBestConfig);
        os.println("    Minimum rate for best config : " + dblRateMinBestConfig);
        
//        int     cntAboveAvg = 0;
//        int     cntAbove200 = 0;
//        int     cntBoth = 0;
//        boolean bolTag = false;
//        
//        for (ConfigResult recResult : setResults) {
//            bolTag = false;
//            
//            // Cases with rates >= the average rate
//            if (recResult.dblRate() >= dblRateAvg) {
//                scoreAboveAvg.score(recResult);
//                cntAboveAvg++;
//                bolTag = true;
//            }
//            
//            // Cases with rates >= 200 Mbps
//            if (recResult.dblRate() >= 200.0) {
//                scoreAbove200.score(recResult);
//                cntAbove200++;
//                if (bolTag == true)
//                    cntBoth++;
//            }
//            
//            // All cases
//            scoreAll.score(recResult);
//        }
        
        
//        for (ConfigResult recResult : setResults) {
//            if (recResult.dblRate() >= dblRateMinBestConfig)
//                scoreBestAboveMin.score(recResult);
//        }
        
        os.println();
        os.println("Configuration Results >= " + dblRateMinBestConfig + " MBps Scoring:");
        scoreBestAboveMin.printOutByHits(os);
        
        os.println();
        os.println("Configuration Results >= " + dblRateTgt + " MBps Scoring:");
        scoreAboveTgt.printOutByHits(os);
        
        os.println();
        os.println("Configuration Results >= " + dblRateAvg + " MBps Scoring:");
        scoreAboveAvg.printOutByHits(os);
        
        os.println();
        os.println("Configuration Results All Scoring:");
        scoreAll.printOutByRates(os);
        
        // Printout results
        int     indResult = 1;
        
        os.println();
        os.println(JavaRuntime.getQualifiedMethodNameSimple() + " RESULTS:");
        os.println("  Total number of test cases run: " + setResults.size());
        os.println();
        for (ConfigResult recResult : setResults) {
            os.println("  Result #" + indResult);
            recResult.printOut(os, "  ");
            os.println();
            
            indResult++;
        }
    }
    
//    /**
//     * <p>
//     * Prints out the configuration of the given test case record to the given print stream.
//     * </p>
//     * 
//     * @param os        print stream receiving output data (i.e., the configuration)
//     * @param recCase   the test case whose configuration is to be output 
//     */
//    private void    printConfiguration(PrintStream os, TestCase recCase) {
//        
//        DpDataRequest   rqst = recCase.rqstOrg;
//        
//        os.println("  Original Time-series Data Request");
//        this.printConfiguration(os, "", rqst);
//        
//        os.println("  Query Recovery Parameters");
//        os.println("    Request decomposition type : " + recCase.enmDcmpType);
//        os.println("    gRPC stream type           : " + recCase.enmStrmType);
//        os.println("    Number of gRPC streams     : " + recCase.cntStrms);
//        
//        int     indRqst = 1;
//        for (DpDataRequest rqstCmp : recCase.lstCmpRqsts) {
//            os.println("    Composite Request #" + indRqst);
//            this.printConfiguration(os, "  ", rqstCmp);
//            indRqst++;
//        }
//    }
//    
//    /**
//     * <p>
//     * Prints out the configuration of the given data request to the given print stream.
//     * </p>
//     * 
//     * @param os        print stream receiving output data (i.e., the configuration)
//     * @param strPad    padding string for parameters
//     * @param rqst      the data request whose configuration is to be output 
//     */
//    private void    printConfiguration(PrintStream os, String strPad, DpDataRequest rqst) {
//        
//        os.println(strPad + "    gRPC stream type   : " + rqst.getStreamType());
//        os.println(strPad + "    data source count  : " + rqst.getSourceCount());
//        os.println(strPad + "    duration (seconds) : " + rqst.rangeDuration().toSeconds());
//        os.println(strPad + "    time interval      : " + rqst.range());
//        os.println(strPad + "    domain size        : " + rqst.approxDomainSize());
//    }

}
