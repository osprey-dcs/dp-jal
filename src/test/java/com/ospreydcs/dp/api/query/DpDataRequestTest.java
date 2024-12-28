/*
 * Project: dp-api-common
 * File:	DpDataRequestTest.java
 * Package: com.ospreydcs.dp.api.query
 * Type: 	DpDataRequestTest
 *
 * Copyright 2010-2023 the original author or authors.
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
 * @since Jan 28, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.grpc.util.ProtoTime;
import com.ospreydcs.dp.api.query.DpDataRequest.CompositeType;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.Timestamp;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest.QuerySpec;


/**
 * JUnit test cases for class <code>{@link DpDataRequest}</code>.
 *
 * @author Christopher K. Allen
 * @since Jan 28, 2024
 *
 */
public class DpDataRequestTest {

    
    //
    // Application Resources
    // 
    
    /** The Data Platform API configuration parameters */
    private static final DpApiConfig    CFG_DEFAULT = DpApiConfig.getInstance();
    
    
    //
    // Class Constants
    //
    
    /** Number of test data sources */
    public static final int             CNT_SOURCE_NAMES = 100;
    
    /** Prefix for each data source name - full name contains index value */
    public static final String          STR_SOURCE_PREFIX = "TEST_SRC-";
    
    /** Data archive time range duration */
    public static final Long            LNG_ARC_DURATION = 100L;
    
    /** Data archive time range duration unit */
    public static final TimeUnit        TM_ARC_DURATION = TimeUnit.SECONDS;
    
    /** Data archive inception instance */
    public static final String          STR_ARC_INCEPTION = CFG_DEFAULT.archive.inception;
    
    
    /** Sample time range offset value */
    public static final int             INT_TIME_OFFSET = -5;
    
    /** Sample time range offset units */
    public static final TimeUnit        TU_TIME_OFFSET = TimeUnit.MINUTES;
    
    
    //
    // Class Resources
    //
    
    /** List of test data source names */
//    public static final List<String>    LST_SOURCE_NAMES = new ArrayList<>(CNT_SOURCE_NAMES);
    public static final List<String>    LST_SOURCE_NAMES = IntStream.range(0, CNT_SOURCE_NAMES).mapToObj(i -> STR_SOURCE_PREFIX + Integer.toString(i)).toList();
    
    /** The data archive duration */
    public static final Duration        DUR_ARC_DURATION = Duration.ofSeconds(LNG_ARC_DURATION);
    
    /** The data archive initial timestamp instance */
    public static final Instant         INS_ARC_START = Instant.parse(STR_ARC_INCEPTION);
    
    /** The data archive final timestamp instance */
    public static final Instant         INS_ARC_STOP = INS_ARC_START.plus(DUR_ARC_DURATION);
    
    
    /** A common request object that can be used for comparison */ 
    private static final DpDataRequest   DPRQST_EMPTY = DpDataRequest.newRequest();
    

    //
    // Test Objects
    //
    
    /** Available object under test */
    private DpDataRequest   rqstTest;
    
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
        this.rqstTest = DpDataRequest.newRequest();
    
        this.rqstTest.selectSources(LST_SOURCE_NAMES);
        this.rqstTest.rangeBetween(INS_ARC_START, INS_ARC_STOP);
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
     * Test method for {@link com.ospreydcs.dp.api.query.DpDataRequest#newRequest()}.
     */
    @Test
    public final void testNewRequest() {
        DpDataRequest dpRqst = DpDataRequest.newRequest();
        
        QueryDataRequest msgRqst = dpRqst.buildQueryRequest();
        QuerySpec        msgQry = msgRqst.getQuerySpec();
        
        List<String>    lstSrcNames = msgQry.getPvNamesList();
        Instant         insStart = ProtoMsg.toInstant( msgQry.getBeginTime() );
        Instant         insStop = ProtoMsg.toInstant( msgQry.getEndTime() );
        
        Assert.assertEquals(INS_ARC_START, insStart);
        Assert.assertTrue("Empty initial query contains data sources.", lstSrcNames.isEmpty());
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " empty query contents:");
        System.out.println("  Data source names = " + lstSrcNames);
        System.out.println("  Archive Inception = " + STR_ARC_INCEPTION);
        System.out.println("  Start time instant = " + insStart);
        System.out.println("  Stop time instant = " + insStop);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpDataRequest#buildCompositeRequest(com.ospreydcs.dp.api.query.DpDataRequest, com.ospreydcs.dp.api.query.DpDataRequest.CompositeType, int)}.
     */
    @Test
    public final void testBuildCompositeRequestDpDataRequestCompositeTypeInt() {
        CompositeType   ENM_TYPE = CompositeType.GRID;
        final int       CNT_QUERIES = 5;
        
        List<DpDataRequest> lstSubRqsts1 = DpDataRequest.buildCompositeRequest(this.rqstTest, ENM_TYPE, CNT_QUERIES);
        List<DpDataRequest> lstSubRqsts2 = this.rqstTest.buildCompositeRequest(ENM_TYPE, CNT_QUERIES);
        
        Assert.assertEquals(lstSubRqsts2, lstSubRqsts1);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpDataRequest#buildCompositeRequest(com.ospreydcs.dp.api.query.DpDataRequest.CompositeType, int)}.
     */
    @Test
    public final void testBuildCompositeRequestCompositeTypeIntHorizontal() {
        CompositeType   ENM_TYPE = CompositeType.HORIZONTAL;
        final int       CNT_QUERIES = 6;
        
        List<DpDataRequest> lstSubRqsts = this.rqstTest.buildCompositeRequest(ENM_TYPE, CNT_QUERIES);
        
        System.out.println("Composite Requests - HORIZONTAL:");
        System.out.println("  " + lstSubRqsts);
        
        // Check composite query
        QueryDataRequest    msgRqstOrg = this.rqstTest.buildQueryRequest();
        List<String>        lstSrcNmsOrg = msgRqstOrg.getQuerySpec().getPvNamesList();
        Timestamp           tmsStartOrg = msgRqstOrg.getQuerySpec().getBeginTime();
        Timestamp           tmsStopOrg = msgRqstOrg.getQuerySpec().getEndTime();
        
        List<String>    lstSrcNmsCmp = new LinkedList<>();
        
        for (DpDataRequest rqst : lstSubRqsts) {
            QueryDataRequest    msgSubRqst = rqst.buildQueryRequest();
            
            List<String>    lstSrcNmsSub = msgSubRqst.getQuerySpec().getPvNamesList();
            Timestamp       tmsStartSub = msgSubRqst.getQuerySpec().getBeginTime();
            Timestamp       tmsStopSub = msgSubRqst.getQuerySpec().getEndTime();
            
            boolean bolStart = ProtoTime.equals(tmsStartOrg, tmsStartSub);
            boolean bolStop = ProtoTime.equals(tmsStopOrg, tmsStopSub);
            Assert.assertTrue("Sub-query has incorrect start timestamp.", bolStart);
            Assert.assertTrue("Sub-query has incorrect end timestamp.", bolStop);
            
            Assert.assertTrue("Sub-query data source name list too large.", lstSrcNmsOrg.size() > lstSrcNmsSub.size());
            Assert.assertTrue("Sub-query data source name list outside original.", lstSrcNmsOrg.containsAll(lstSrcNmsSub));
            
            lstSrcNmsCmp.addAll(lstSrcNmsSub);
        }
        
        Assert.assertEquals(lstSrcNmsOrg, lstSrcNmsCmp);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpDataRequest#buildCompositeRequest(com.ospreydcs.dp.api.query.DpDataRequest.CompositeType, int)}.
     */
    @Test
    public final void testBuildCompositeRequestCompositeTypeIntVertical() {
        CompositeType   ENM_TYPE = CompositeType.VERTICAL;
        final int       CNT_QUERIES = 4;
        
        List<DpDataRequest> lstSubRqsts = this.rqstTest.buildCompositeRequest(ENM_TYPE, CNT_QUERIES);
        
        System.out.println("Composite Requests - VERTICAL:");
        System.out.println("  " + lstSubRqsts);
        
        // Check composite query
        QueryDataRequest    msgRqstOrg = this.rqstTest.buildQueryRequest();
        List<String>        lstSrcNmsOrg = msgRqstOrg.getQuerySpec().getPvNamesList();
        Timestamp           tmsStartOrg = msgRqstOrg.getQuerySpec().getBeginTime();
        Timestamp           tmsStopOrg = msgRqstOrg.getQuerySpec().getEndTime();
        
        Duration        durComposite = Duration.ZERO;
        Instant         insStartCmp = null;
        Instant         insStopCmp = null;
        
        for (DpDataRequest rqst : lstSubRqsts) {
            QueryDataRequest    msgSubRqst = rqst.buildQueryRequest();
            
            List<String>    lstSrcNmsSub = msgSubRqst.getQuerySpec().getPvNamesList();
            Timestamp       tmsStartSub = msgSubRqst.getQuerySpec().getBeginTime();
            Timestamp       tmsStopSub = msgSubRqst.getQuerySpec().getEndTime();
         
            // Check data sources
            Assert.assertEquals(lstSrcNmsOrg, lstSrcNmsSub);

            // Advance the composite time interval endpoints and duration
            Instant         insStartSub = ProtoMsg.toInstant(tmsStartSub);
            Instant         insStopSub = ProtoMsg.toInstant(tmsStopSub);
            Duration        durSubRqst = Duration.between(insStartSub, insStopSub);
            
            if (insStartCmp == null) {
                insStartCmp = insStartSub;
                insStopCmp = insStartSub;
            }
            
            insStopCmp = insStopCmp.plus(durSubRqst); 
            durComposite = durComposite.plus(durSubRqst);
        }        
        
        // Check the composite time interval
        Instant     insStartOrg = ProtoMsg.toInstant(tmsStartOrg);
        Instant     insStopOrg = ProtoMsg.toInstant(tmsStopOrg);
        Duration    durRqstOrg = Duration.between(insStartOrg, insStopOrg);
        
        Assert.assertEquals(durRqstOrg, durComposite);
        Assert.assertEquals(insStartOrg, insStartCmp);
        Assert.assertEquals(insStopOrg, insStopCmp);
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpDataRequest#buildCompositeRequest(com.ospreydcs.dp.api.query.DpDataRequest.CompositeType, int)}.
     */
    @Test
    public final void testBuildCompositeRequestCompositeTypeIntGrid() {
        CompositeType   ENM_TYPE = CompositeType.GRID;
        final int       CNT_QUERIES = 5;
        
        List<DpDataRequest> lstSubRqsts = this.rqstTest.buildCompositeRequest(ENM_TYPE, CNT_QUERIES);
        
        System.out.println("Composite Requests - GRID:");
        System.out.println("  " + lstSubRqsts);
        
        // Too convoluted for a precise check - just check some necessary conditions
        QueryDataRequest    msgRqstOrg = this.rqstTest.buildQueryRequest();
        List<String>        lstSrcNmsOrg = msgRqstOrg.getQuerySpec().getPvNamesList();
        Timestamp           tmsStartOrg = msgRqstOrg.getQuerySpec().getBeginTime();
        Timestamp           tmsStopOrg = msgRqstOrg.getQuerySpec().getEndTime();
        
        Instant         insStartOrg = ProtoMsg.toInstant(tmsStartOrg);
        Instant         insStopOrg = ProtoMsg.toInstant(tmsStopOrg);
        Duration        durRqstOrg = Duration.between(insStartOrg, insStopOrg);

        List<String>    lstSrcNmsCmp = new LinkedList<>();
        
        Instant         insStartCmp = null;
        Instant         insStopCmp = null;

        for (DpDataRequest rqst : lstSubRqsts) {
            QueryDataRequest    msgSubRqst = rqst.buildQueryRequest();
            
            List<String>    lstSrcNmsSub = msgSubRqst.getQuerySpec().getPvNamesList();
            Timestamp       tmsStartSub = msgSubRqst.getQuerySpec().getBeginTime();
            Timestamp       tmsStopSub = msgSubRqst.getQuerySpec().getEndTime();
         
            // Check sub-query data sources
            Assert.assertTrue("Sub-query data sources not in original set", lstSrcNmsOrg.containsAll(lstSrcNmsCmp));

            if (!lstSrcNmsCmp.containsAll(lstSrcNmsSub))
                lstSrcNmsCmp.addAll(lstSrcNmsSub);

            // Check the sub-query time domain
            Instant         insStartSub = ProtoMsg.toInstant(tmsStartSub);
            Instant         insStopSub = ProtoMsg.toInstant(tmsStopSub);
            Duration        durSubRqst = Duration.between(insStartSub, insStopSub);
            
            Assert.assertTrue("Sub-query duration greater than original query.", durSubRqst.minus(durRqstOrg).isNegative());

            if (insStartCmp == null) {
                insStartCmp = insStartSub;
                insStopCmp = insStartSub;
            }
            if (insStartSub.isBefore(insStartCmp))
                insStartCmp = insStartSub;
            if (insStopSub.isAfter(insStopCmp))
                insStopCmp = insStopSub;
        }
        
        Assert.assertEquals(insStartOrg, insStartCmp);
        Assert.assertEquals(insStopOrg, insStopCmp);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpDataRequest#buildQueryRequest()}.
     */
    @Test
    public final void testBuildQueryRequest() {
        DpDataRequest   rqst = DpDataRequest.newRequest();
        
        Assert.assertEquals(DPRQST_EMPTY, rqst);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpDataRequest#reset()}.
     */
    @Test
    public final void testReset() {
        this.rqstTest.reset();
        
        Assert.assertEquals(DPRQST_EMPTY, this.rqstTest);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpDataRequest#rangeBefore(java.time.Instant)}.
     */
    @Test
    public final void testRangeBefore() {
        
        // Pick random timestamp
        Instant     insNow = Instant.now();
        Timestamp   tmsNow = ProtoMsg.from(insNow); 
        
        // Set the request range
        this.rqstTest.rangeBefore(insNow);
        
        // Extract the target timestamp(s) and compare
        Timestamp   tmsStop = this.rqstTest.buildQueryRequest().getQuerySpec().getEndTime();
        
        boolean bolEquiv = ProtoTime.equivalence(tmsNow, tmsStop);
        
        Assert.assertTrue("rangeBefore() failed to set end timestamp correctly.", bolEquiv);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpDataRequest#rangeAfter(java.time.Instant)}.
     */
    @Test
    public final void testRangeAfter() {

        // Pick random timestamp
        Instant     insNow = Instant.now();
        Timestamp   tmsNow = ProtoMsg.from(insNow); 
        
        // Set the request range
        this.rqstTest.rangeAfter(insNow);
        
        // Extract the target timestamp(s) and compare
        Timestamp   tmsStart = this.rqstTest.buildQueryRequest().getQuerySpec().getBeginTime();
        
        boolean bolEquiv = ProtoTime.equivalence(tmsNow, tmsStart);
        
        Assert.assertTrue("rangeAfter() failed to set start timestamp correctly.", bolEquiv);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpDataRequest#rangeBetween(java.time.Instant, java.time.Instant)}.
     */
    @Test
    public final void testRangeBetween() {

        // Set the request range
        DpDataRequest rqst = DpDataRequest.newRequest();
        rqst.rangeBetween(INS_ARC_START, INS_ARC_STOP);

        // Extract the target timestamp(s) and compare
        QueryDataRequest    msgRqst = this.rqstTest.buildQueryRequest();
        
        Timestamp   tmsStartMsg = msgRqst.getQuerySpec().getBeginTime();
        Timestamp   tmsStopMsg = msgRqst.getQuerySpec().getEndTime();

        Timestamp   tmsStartArc = ProtoMsg.from(INS_ARC_START);
        Timestamp   tmsStopArc = ProtoMsg.from(INS_ARC_STOP);
        
        boolean bolStart = ProtoTime.equivalence(tmsStartMsg, tmsStartArc);
        boolean bolStop = ProtoTime.equivalence(tmsStopMsg, tmsStopArc);
 
        
        Assert.assertTrue("rangeBetween() failed to set start timestamp correctly.", bolStart);
        Assert.assertTrue("rangeBetween() failed to set end timestamp correctly.", bolStop);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpDataRequest#rangeDuration(java.time.Instant, java.lang.Long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testRangeDuration() {

        // Set the request range
        DpDataRequest rqst = DpDataRequest.newRequest();
        rqst.rangeDuration(INS_ARC_START, LNG_ARC_DURATION, TM_ARC_DURATION);

        // Extract the target timestamp(s) and compare
        QueryDataRequest    msgRqst = this.rqstTest.buildQueryRequest();
        
        Timestamp   tmsStartMsg = msgRqst.getQuerySpec().getBeginTime();
        Timestamp   tmsStopMsg = msgRqst.getQuerySpec().getEndTime();

        Timestamp   tmsStartArc = ProtoMsg.from(INS_ARC_START);
        Timestamp   tmsStopArc = ProtoMsg.from(INS_ARC_STOP);
        
        boolean bolStart = ProtoTime.equivalence(tmsStartMsg, tmsStartArc);
        boolean bolStop = ProtoTime.equivalence(tmsStopMsg, tmsStopArc);
 
        
        Assert.assertTrue("rangeDuration() failed to set start timestamp correctly.", bolStart);
        Assert.assertTrue("rangeDuration() failed to set end timestamp correctly.", bolStop);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpDataRequest#rangeOffset(int, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testRangeOffset() {

        // Set the request range
        this.rqstTest.rangeOffset(INT_TIME_OFFSET, TU_TIME_OFFSET);
        
        // Create a (nearly) equivalent time range
        Instant     insNow = Instant.now();
        Timestamp   tmsNow = ProtoMsg.from(insNow);
        long        lngDurSecs = - TU_TIME_OFFSET.toSeconds(INT_TIME_OFFSET);
        long        lngStopSecs = tmsNow.getEpochSeconds();
        long        lngStartSecs = lngStopSecs - lngDurSecs;
        
        // Extract the target timestamp(s) and compare
        QueryDataRequest    msgRqst = this.rqstTest.buildQueryRequest();
        
        Timestamp   tmsStart = msgRqst.getQuerySpec().getBeginTime();
        Timestamp   tmsStop = msgRqst.getQuerySpec().getEndTime();
        
        Assert.assertEquals("rangeOffset() failed to set start timestamp correctly (seconds).", tmsStart.getEpochSeconds(), lngStartSecs);
        Assert.assertEquals("rangeOffset() failed to set end timestamp correctly (seconds).", tmsStop.getEpochSeconds(), lngStopSecs);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpDataRequest#selectSource(java.lang.String)}.
     */
    @Test
    public final void testSelectSource() {
        
        // Pick a source
        String  strSourceName = LST_SOURCE_NAMES.get(0);
        
        // Set the request source
        this.rqstTest.reset();
        this.rqstTest.selectSource(strSourceName);
        
        // Check equality
        List<String>    lstSrcNms = this.rqstTest.buildQueryRequest().getQuerySpec().getPvNamesList();
        
        Assert.assertTrue("The request source name list does not have size 1.", lstSrcNms.size() == 1);
        Assert.assertEquals(strSourceName, lstSrcNms.get(0));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpDataRequest#selectSources(java.util.List)}.
     */
    @Test
    public final void testSelectSources() {
        
        // Set the request sources
        this.rqstTest.reset();
        this.rqstTest.selectSources(LST_SOURCE_NAMES);

        // Check equality
        List<String>    lstSrcNms = this.rqstTest.buildQueryRequest().getQuerySpec().getPvNamesList();
        
        Assert.assertEquals(LST_SOURCE_NAMES, lstSrcNms);
    }

    
    //
    // Support Methods
    //
    
}
