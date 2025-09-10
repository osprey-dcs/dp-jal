/*
 * Project: dp-api-common
 * File:	RawDataCorrelatorTest.java
 * Package: com.ospreydcs.dp.api.query.model.correl
 * Type: 	RawDataCorrelatorTest
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
 * @since Mar 14, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.correl;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.JalConfig;
import com.ospreydcs.dp.api.config.query.JalQueryConfig;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryApiFactoryNew;
import com.ospreydcs.dp.api.query.DpQueryStreamBuffer;
import com.ospreydcs.dp.api.query.IQueryService;
import com.ospreydcs.dp.api.query.test.TestDpDataRequestGenerator;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

/**
 * <p>
 * JUnit test cases for class <code>RawDataCorrelator</code>.
 * </p>
 * <p>
 * <h2>WARNING:</h2>
 * The Data Platform test archive must be available at the default Query Service location.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 14, 2025
 *
 */
public class RawDataCorrelatorTest {

    
    //
    // Application Resources
    //
    
    /** The default Query Service configuration parameters */
    public static final JalQueryConfig          CFG_QUERY = JalConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** General timeout limit */
    public static final long        LNG_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** General timeout units */
    public static final TimeUnit    TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
//    /** Location of performance results output */
//    public static final String          STR_PATH_OUTPUT = "test/output/query/impl/";
    
    
    /** "Small" test request  */
    public static final DpDataRequest   RQST_SMALL = TestQueryResponses.QREC_LONG.createRequest();
    
    /** Wide test request (100 PVs, 5 secs) */
    public static final DpDataRequest   RQST_WIDE = TestQueryResponses.QREC_WIDE.createRequest();
    
    /** Large test request (500 PVs, 20 secs) */
    public static final DpDataRequest   RQST_BIG = TestQueryResponses.QREC_BIG.createRequest();
    
    /** Huge test request (1,000 PVs, 20 secs) */
    public static final DpDataRequest   RQST_HUGE = TestQueryResponses.QREC_HUGE.createRequest();
    
    
    /** The list of all (ordered) PV names - taken from the test request generator */
    public static final List<String>    LST_PV_NMS = TestDpDataRequestGenerator.LST_PV_NAMES;
    
    
    /** "Small" test raw data */
    public static List<QueryDataResponse.QueryData>     LST_DATA_SMALL;
    
    /** Wide test raw data */
    public static List<QueryDataResponse.QueryData>     LST_DATA_WIDE;
    
    /** Large test raw data */
    public static List<QueryDataResponse.QueryData>     LST_DATA_BIG;

    
    //
    // Test Fixture Resources
    //
    
    /** The common Query Service API used for query test cases */
    private static IQueryService            apiTest;
    
    /** The common RawDataCorrelator used for testing cases */
    private static RawDataCorrelator        corrTest;
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        // Create the Query Service connection
        apiTest = DpQueryApiFactoryNew.connect();
        
        // Recover the raw data from the test archive
        LST_DATA_SMALL = RawDataCorrelatorTest.recoverQueryData(RQST_SMALL);
        LST_DATA_WIDE = RawDataCorrelatorTest.recoverQueryData(RQST_WIDE);
        LST_DATA_BIG = RawDataCorrelatorTest.recoverQueryData(RQST_BIG);
        
        // Create the test correlator
        corrTest = RawDataCorrelator.create();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        try {
            apiTest.shutdown();
            
        } catch (InterruptedException e) {
            String strMsg = "WARNING: Query Service API interrupted during shut down: " + e.getMessage();
            
            System.err.println(strMsg);
        }
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator#create()}.
     */
    @Test
    public final void testCreate() {
        @SuppressWarnings("unused")
        RawDataCorrelator   correl = RawDataCorrelator.create();
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator#RawDataCorrelator()}.
     */
    @Test
    public final void testRawDataCorrelator() {
        @SuppressWarnings("unused")
        RawDataCorrelator   correl = new RawDataCorrelator();
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator#enableConcurrency(int)}.
     */
    @Test
    public final void testEnableConcurrency() {
        
        // Parameters
        final Boolean   bolConcDef = CFG_QUERY.data.recovery.correlate.concurrency.enabled;
        final int       cntThreadsDef = CFG_QUERY.data.recovery.correlate.concurrency.maxThreads;
        final int       cntThreads = 11;
        final boolean   bolConcNot = !bolConcDef;
        
        // Verify correlator default configuration
        Assert.assertEquals(bolConcDef, corrTest.isConcurrencyEnabled());
        Assert.assertEquals(cntThreadsDef, corrTest.getConcurrencytMaxThreads());
        
        // Set the correlator concurrency and verify
        corrTest.enableConcurrency(bolConcNot);
        corrTest.setMaxThreadCount(cntThreads);
        Assert.assertTrue(corrTest.isConcurrencyEnabled() == bolConcNot);
        Assert.assertEquals(cntThreads, corrTest.getConcurrencytMaxThreads());
        
        // Return the correlator configuration
        corrTest.enableConcurrency(bolConcDef);
        corrTest.setMaxThreadCount(cntThreadsDef);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator#disableConcurrency()}.
     */
    @Test
    public final void testDisableConcurrency() {
        
        // Get correlator configuration
        boolean bolConc = corrTest.isConcurrencyEnabled();

        // Disable concurrency and verify
        corrTest.disableConcurrency();
        Assert.assertFalse(corrTest.isConcurrencyEnabled());
        
        // Restore configuration
        corrTest.enableConcurrency(bolConc);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator#setConcurrencyPivotSize(int)}.
     */
    @Test
    public final void testSetConcurrencyPivotSize() {
        
        // Parameters
        final int   szPivotDef = CFG_QUERY.data.recovery.correlate.concurrency.pivotSize;
        final int   szPivotTest = 100;

        // Verify correlator default configuration
        Assert.assertEquals(szPivotDef, corrTest.getConcurrencyPivotSize());
        
        // Change configuration and verify
        corrTest.setConcurrencyPivotSize(szPivotTest);
        Assert.assertEquals(szPivotTest, corrTest.getConcurrencyPivotSize());
        
        // Restore default configuration
        corrTest.setConcurrencyPivotSize(szPivotDef);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator#reset()}.
     */
    @Test
    public final void testReset() {
        
        // Parameters
        final DpDataRequest     rqst1 = RQST_SMALL;
        final int               cntPvs1 = rqst1.getSourceCount();
        final List<QueryData>   lstMsgData1 = LST_DATA_SMALL;
        final int               cntMsgs1 = lstMsgData1.size();
        
        final DpDataRequest     rqst2 = RQST_BIG;
        final int               cntPvs2 = rqst2.getSourceCount();
        final List<QueryData>   lstMsgData2 = LST_DATA_BIG;
        final int               cntMsgs2 = lstMsgData2.size();
        
        // Announce
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        
        // Process (correlate) the first test data
        try {
            int     cntPrcd = 0;
            for (QueryData msgData : lstMsgData1) {
                corrTest.processQueryData(msgData);

                cntPrcd++;
            }
            
            Assert.assertEquals("Data Set#1: Not all raw data messages were processed", cntMsgs1, cntPrcd);
            
        } catch (IllegalArgumentException | CompletionException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - raw data correlation threw "
                    + e.getClass().getSimpleName()
                    + " exception: "
                    + e.getMessage();
                    
            System.err.println(strMsg);
            corrTest.reset();
            Assert.fail(strMsg);
            return;
        }
        
        // Recover the correlated set and inspect
        SortedSet<RawCorrelatedData>    setRawData = corrTest.getCorrelatedSet();
        
        System.out.println("  Request #1 PV count       : " + cntPvs1);
        System.out.println("  Request #1 PV names       : " + rqst1.getSourceNames());
        System.out.println("  Response #1 message count : " + cntMsgs1);
        System.out.println("  #1 Correlated block count : " + setRawData.size());
        
        // Reset the correlator and verify
        corrTest.reset();
        
        setRawData = corrTest.getCorrelatedSet();
        System.out.println("  Correlator reset block count : " + setRawData.size());
        
        Assert.assertEquals(0, setRawData.size());

        // Process (correlate) the second test data
        try {
            int     cntPrcd = 0;
            for (QueryData msgData : lstMsgData2) {
                corrTest.processQueryData(msgData);

                cntPrcd++;
            }
            
            Assert.assertEquals("Data Set #2: Not all raw data messages were processed", cntMsgs2, cntPrcd);
            
        } catch (IllegalArgumentException | CompletionException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - raw data correlation threw "
                    + e.getClass().getSimpleName()
                    + " exception: "
                    + e.getMessage();
                    
            System.err.println(strMsg);
            corrTest.reset();
            Assert.fail(strMsg);
            return;
        }
        
        // Recover the correlated set and inspect
        setRawData = corrTest.getCorrelatedSet();
        
        System.out.println("  Request #2 PV count       : " + cntPvs2);
        System.out.println("  Response #2 message count : " + cntMsgs2);
        System.out.println("  #2 Correlated block count : " + setRawData.size());
        System.out.println("  #2 Correlator PV count    : " + corrTest.extractDataSourceNames().size());
        
        // Reset the correlator 
        corrTest.reset();
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator#isConcurrencyEnabled()}.
//     */
//    @Test
//    public final void testIsConcurrencyEnabled() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator#getConcurrencytMaxThreads()}.
//     */
//    @Test
//    public final void testGetConcurrencyMaxThreads() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator#getConcurrencyPivotSize()}.
//     */
//    @Test
//    public final void testGetConcurrencyPivotSize() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator#getCorrelatedSet()}.
//     */
//    @Test
//    public final void testGetCorrelatedSet() {
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator#getBytesProcessed()}.
     */
    @Test
    public final void testGetBytesProcessed() {
        
        // Parameters
        final DpDataRequest     rqstTest = RQST_BIG;
        final int               cntPvs = rqstTest.getSourceCount();
        final List<QueryData>   lstMsgData = LST_DATA_BIG;
        final int               cntMsgs = lstMsgData.size();
        
        // Announce
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        
        // Process (correlate) the test data
        try {
            int     cntPrcd = 0;
            long    szMsg = 0;
            long    szPrcd = 0;
            for (QueryData msgData : lstMsgData) {
                
                corrTest.processQueryData(msgData);
                szPrcd = corrTest.getBytesProcessed();
                szMsg += msgData.getSerializedSize();
                
                Assert.assertEquals(szMsg, szPrcd);
                
                cntPrcd++;
            }
            
            Assert.assertEquals("Not all raw data messages were processed", cntMsgs, cntPrcd);
            
        } catch (IllegalArgumentException | CompletionException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - raw data correlation threw "
                    + e.getClass().getSimpleName()
                    + " exception: "
                    + e.getMessage();
                    
            System.err.println(strMsg);
            corrTest.reset();
            Assert.fail(strMsg);
        }
        
        // This works for BIG request
        Assert.assertEquals(20, corrTest.sizeCorrelatedSet());
        
        // Reset the correlator
        corrTest.reset();
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator#sizeCorrelatedSet()}.
     */
    @Test
    public final void testSizeCorrelatedSet() {
        
        // Parameters
        final DpDataRequest     rqstTest = RQST_WIDE;
        final int               cntPvs = rqstTest.getSourceCount();
        final List<QueryData>   lstMsgData = LST_DATA_WIDE;
        final int               cntMsgs = lstMsgData.size();
        
        // Announce
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        
        // Process (correlate) the test data
        try {
            int     cntPrcd = 0;
            for (QueryData msgData : lstMsgData) {
                
                if (cntPrcd == 0)
                    Assert.assertEquals(0, corrTest.sizeCorrelatedSet());
                
                corrTest.processQueryData(msgData);

//                if (cntPrcd == 0)
//                    Assert.assertEquals(1, corrTest.sizeCorrelatedSet());
                
                cntPrcd++;
            }
            
            Assert.assertEquals("Not all raw data messages were processed", cntMsgs, cntPrcd);
            
        } catch (IllegalArgumentException | CompletionException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - raw data correlation threw "
                    + e.getClass().getSimpleName()
                    + " exception: "
                    + e.getMessage();
                    
            System.err.println(strMsg);
            corrTest.reset();
            Assert.fail(strMsg);
        }
        
        // This works for WIDE request
        Assert.assertEquals(5, corrTest.sizeCorrelatedSet());
        
        // Reset the correlator
        corrTest.reset();
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator#extractDataSourceNames()}.
     */
    @Test
    public final void testExtractDataSourceNames() {
        
        // Parameters
        final DpDataRequest     rqstTest = RQST_WIDE;
        final int               cntPvs = rqstTest.getSourceCount();
        final List<QueryData>   lstMsgData = LST_DATA_WIDE;
        final int               cntMsgs = lstMsgData.size();
//        final List<String>      lstPvNms = LST_PV_NMS.subList(0, cntPvs);
        final List<String>      lstPvNms = rqstTest.getSourceNames().subList(0, cntPvs);
        
        // Announce
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        
        // Process (correlate) the test data
        try {
            int     cntPrcd = 0;
            for (QueryData msgData : lstMsgData) {
                corrTest.processQueryData(msgData);

                cntPrcd++;
            }
            
            Assert.assertEquals("Not all raw data messages were processed", cntMsgs, cntPrcd);
            
        } catch (IllegalArgumentException | CompletionException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - raw data correlation threw "
                    + e.getClass().getSimpleName()
                    + " exception: "
                    + e.getMessage();
                    
            System.err.println(strMsg);
            corrTest.reset();
            Assert.fail(strMsg);
        }
        
        // Recover the correlated set and inspect
        SortedSet<RawCorrelatedData>    setRawData = corrTest.getCorrelatedSet();
        Set<String>                     setPrcdPvNms = corrTest.extractDataSourceNames();
        
        System.out.println("  Request PV count        : " + cntPvs);
        System.out.println("  Response message count  : " + cntMsgs);
        System.out.println("  Correlated block count  : " + setRawData.size());
        System.out.println("  Extracted PV name count : " + setPrcdPvNms.size());
        
        boolean bolAllPvNms = setPrcdPvNms.containsAll(lstPvNms);
        
        // Reset the correlator
        corrTest.reset();
        
        Assert.assertTrue(bolAllPvNms);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator#processQueryData(com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData)}.
     */
    @Test
    public final void testProcessQueryData() {
        
        // Parameters
        final DpDataRequest     rqstTest = RQST_SMALL;
        final int               cntPvs = rqstTest.getSourceCount();
        final List<QueryData>   lstMsgData = LST_DATA_SMALL;
        final int               cntMsgs = lstMsgData.size();
        
        // Announce
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        
        // Process (correlate) the test data
        try {
            int     cntPrcd = 0;
            for (QueryData msgData : lstMsgData) {
                corrTest.processQueryData(msgData);

                cntPrcd++;
            }
            
            Assert.assertEquals("Not all raw data messages were processed", cntMsgs, cntPrcd);
            
        } catch (IllegalArgumentException | CompletionException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - raw data correlation threw "
                    + e.getClass().getSimpleName()
                    + " exception: "
                    + e.getMessage();
                    
            System.err.println(strMsg);
            corrTest.reset();
            Assert.fail(strMsg);
        }
        
        // Recover the correlated set and inspect
        SortedSet<RawCorrelatedData>    setRawData = corrTest.getCorrelatedSet();
        
        System.out.println("  Request PV count       : " + cntPvs);
        System.out.println("  Request PV names       : " + rqstTest.getSourceNames());
        System.out.println("  Response message count : " + cntMsgs);
        System.out.println("  Correlated block count : " + setRawData.size());
        int indBlk = 1;
        for (RawCorrelatedData data : setRawData) {
            System.out.println("  Block #" + indBlk);
            System.out.println("    Start time   : " + data.getStartTime());
            System.out.println("    Time range   : " + data.getTimeRange());
            System.out.println("    Source count : " + data.getSourceCount());
            System.out.println("    Source names : " + data.getSourceNames());
            System.out.println("    Sample count : " + data.getSampleCount());
            indBlk++;
        }
        
        // Reset the correlator
        corrTest.reset();
    }


    //
    // Support Methods
    // 

    /**
     * <p>
     * Performs the given time-series data request and recovers the raw Query Service data.
     * </p>
     * <p>
     * Uses the class resource <code>{@link #apiTest}</code> to perform the query and a 
     * <code>DpQueryStreamBuffer</code> instance to recover the raw data.  The 
     * <code>QueryDataResponse.QueryData</code> Protocol Buffers data messages are extracted from
     * the recovered data and returned.
     * </p>
     * 
     * @param rqst  time-series data request
     * 
     * @return  the raw time-series data obtained from the Query Service 
     */
    private static List<QueryDataResponse.QueryData>   recoverQueryData(DpDataRequest rqst) {
        
        DpQueryStreamBuffer bufData = RawDataCorrelatorTest.apiTest.queryDataStream(rqst);
        
        try {
            bufData.startAndAwaitCompletion();
            
            List<QueryDataResponse> lstRsps = bufData.getBuffer();
            
            boolean bolValid = lstRsps.stream().allMatch(msg -> msg.hasQueryData());
            
            if (!bolValid) {
                String strMsg = JavaRuntime.getQualifiedMethodNameSimple() +
                        " - Query Service reported an exceptional result within the data stream.";
                
                System.err.println(strMsg);
                Assert.fail(strMsg);
            }
            
            List<QueryDataResponse.QueryData>   lstData = lstRsps
                    .stream()
                    .<QueryDataResponse.QueryData>map(msg -> msg.getQueryData())
                    .toList();
            
            return lstData;
            
        } catch (IllegalStateException | IllegalArgumentException | InterruptedException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - exception "
                    + e.getClass().getSimpleName()
                    + " during data recovery: "
                    + e.getMessage();
            
            System.err.println(strMsg);
            Assert.fail(strMsg);
            return null;
        }
    }
}
