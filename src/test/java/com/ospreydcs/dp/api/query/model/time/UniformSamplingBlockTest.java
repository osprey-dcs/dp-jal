/*
 * Project: dp-api-common
 * File:	UniformSamplingBlockTest.java
 * Package: com.ospreydcs.dp.api.query.model.time
 * Type: 	UniformSamplingBlockTest
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
 * @since Feb 14, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.time;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.grpc.util.ProtoTime;
import com.ospreydcs.dp.api.model.DpSupportedType;
import com.ospreydcs.dp.api.model.TimeInterval;
import com.ospreydcs.dp.api.query.model.data.SampledTimeSeries;
import com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData;
import com.ospreydcs.dp.api.query.model.proto.QueryDataCorrelator;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.query.test.TestQueryResponses.SingleQueryType;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.FixedIntervalTimestampSpec;
import com.ospreydcs.dp.grpc.v1.common.Timestamp;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData.DataBucket;

/**
 * JUnit test cases for <code>{@link UniformSamplingBlock}</code> class.
 * 
 * @author Christopher K. Allen
 * @since Feb 14, 2024
 *
 */
public class UniformSamplingBlockTest {

    
    //
    // Test Constants
    //
    
    /** Test data - single bucket query */
    public static final DataBucket          BUCKET = TestQueryResponses.queryBuckets(SingleQueryType.BUCKET).get(0);
    
    /** Sample query data for test cases - 1 source, 10 seconds */
    public static final List<BucketData>   LST_QUERY_DATA_ONE = TestQueryResponses.queryData(SingleQueryType.ONE_SOURCE);
    
    /** Sample query data for test cases - 2 sources, 2 seconds */
    public static final List<BucketData>   LST_QUERY_DATA_TWO = TestQueryResponses.queryData(SingleQueryType.TWO_SOURCE);
    
    /** Sample query data for test cases - 100 sources, 5 seconds */
    public static final List<BucketData>   LST_QUERY_DATA_WIDE = TestQueryResponses.queryData(SingleQueryType.WIDE);
    
    /** Sample query data for test cases - 5 sources, 60 seconds */
    public static final List<BucketData>   LST_QUERY_DATA_LONG = TestQueryResponses.queryData(SingleQueryType.LONG);
    
    
    //
    // Test Case Resources
    //
    
    /** The query results set correlator - we only need one */
    private final static QueryDataCorrelator  CORRELATOR = new QueryDataCorrelator();
    
    
    
    
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
        TestQueryResponses.shutdown();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        CORRELATOR.reset();
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#from(com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData)}.
     */
    @Test
    public final void testFrom() {
        List<BucketData>    lstRawMsgData = LST_QUERY_DATA_WIDE;

        CorrelatedQueryData     cqdFirst = this.correlate(lstRawMsgData).first();
        
        try {
            UniformSamplingBlock    blkTest = UniformSamplingBlock.from(cqdFirst);  // method under test
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
        }
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#UniformSamplingBlock(com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData)}.
     */
    @Test
    public final void testUniformSamplingBlock() {
        DataBucket  msgBucket = BUCKET;

        CorrelatedQueryData cqdTest = CorrelatedQueryData.from(msgBucket);
        
        try {
            UniformSamplingBlock    blkTest = new UniformSamplingBlock(cqdTest); // method under test
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#getSampleCount()}.
     */
    @Test
    public final void testGetSampleCount() {
        DataBucket  msgBucket = BUCKET;

        // Create the single-series sampling block
        UniformSamplingBlock    blkTest;
        try {
            blkTest = this.singleSeries(msgBucket);
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            return;
            
        }
        
        // Extract sample count from data bucket clock and compare
        int     cntSamples = msgBucket.getSamplingInterval().getNumSamples();
        int     szSeries = blkTest.getSampleCount();    // method under test
        
        Assert.assertEquals(cntSamples, szSeries);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#getDataSourceCount()}.
     */
    @Test
    public final void testGetDataSourceCount() {
        List<BucketData>    lstRawMsgData = LST_QUERY_DATA_WIDE;
        
        // Create processed sampling blocks
        List<UniformSamplingBlock>     lstBlocksTest;
        
        try {
            lstBlocksTest = this.process(lstRawMsgData);
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            return;
        }
        
        // Extract data source names from raw data and get size 
        Set<String>     setSrcNmsRaw = lstRawMsgData
                .stream()
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .<String>map(msgBucket -> msgBucket.getDataColumn().getName())
                .collect(Collectors.toSet());
        
        int     cntSources = setSrcNmsRaw.size();
        
        // Check that all sampling blocks have the correct source count
        // - Note that each data series in results set should be represented within 
        //   each sampling block
        boolean bolResult = lstBlocksTest
                .stream()
                .allMatch(blk -> blk.getDataSourceCount() == cntSources); // method under test
        
        Assert.assertTrue("Not all data source within raw data represented in each samplineg block.", bolResult);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#getStartInstant()}.
     */
    @Test
    public final void testGetStartInstant() {
        List<BucketData>    lstRawMsgData = LST_QUERY_DATA_LONG;
        
        // Create processed sampling blocks
        List<UniformSamplingBlock>     lstBlocksTest;
        
        try {
            lstBlocksTest = this.process(lstRawMsgData);
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            return;
        }
        
        // Extract and order all the unique sampling clock start times within raw data
        SortedSet<Instant>   setStartTimes = lstRawMsgData
                .stream()
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .<Timestamp>map(msgBucket -> msgBucket.getSamplingInterval().getStartTime())
                .<Instant>map(ProtoMsg::toInstant)
                .collect(TreeSet<Instant>::new, TreeSet<Instant>::add, TreeSet<Instant>::addAll);
           
        Assert.assertEquals("Incorrect number of unique start times from raw data.", lstBlocksTest.size(), setStartTimes.size());

        // Ordered comparison of all start times within sampling blocks
        int     indBlk = 0;
        for (Instant insStartTime : setStartTimes) {
            UniformSamplingBlock    blkCurr = lstBlocksTest.get(indBlk);
            Instant                 insStartTimeBlk = blkCurr.getStartInstant(); // method under test
            
            Assert.assertEquals("Start time incorrect for index " + Integer.toString(indBlk), insStartTime, insStartTimeBlk);
            
            indBlk++;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#getTimeDomain()}.
     */
    @Test
    public final void testGetTimeDomain() {
        List<BucketData>    lstRawMsgData = LST_QUERY_DATA_LONG;
        
        // Create processed sampling blocks
        List<UniformSamplingBlock>     lstBlocksTest;
        
        try {
            lstBlocksTest = this.process(lstRawMsgData);
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            return;
        }
        
        // Extract all the sampling clock messages within raw data
        List<FixedIntervalTimestampSpec>   lstMsgClocks = lstRawMsgData
                .stream()
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .<FixedIntervalTimestampSpec>map(DataBucket::getSamplingInterval)
                .toList();
           
        // Order all the unique sampling clocks within raw data into a sorted set
        Comparator<FixedIntervalTimestampSpec>  cmpMsgClock = (clk1, clk2) -> {
            return ProtoTime.compare(clk1.getStartTime(), clk2.getStartTime());
        };
        SortedSet<FixedIntervalTimestampSpec>   setMsgClocks = new TreeSet<>(cmpMsgClock);
        setMsgClocks.addAll(lstMsgClocks);
        
        Assert.assertEquals("Incorrect number of unique sampling clocks from raw data.", lstBlocksTest.size(), setMsgClocks.size());

        // Build the ordered list of time domains for each clock
        List<TimeInterval>      lstTimeDomains = setMsgClocks
                .stream()
                .<TimeInterval>map(msgClock -> {
                                    int     cntSamples = msgClock.getNumSamples();
                                    long    lngPeriodNs = msgClock.getSampleIntervalNanos();
                                    long    lngDurationNs = (cntSamples - 1) * lngPeriodNs;
                                    Instant insBegin = ProtoMsg.toInstant(msgClock.getStartTime());
                                    Instant insEnd = insBegin.plusNanos(lngDurationNs);
                                    TimeInterval domClock = TimeInterval.from(insBegin, insEnd);
                                    
                                    return domClock;
                                    }
                                )
                .toList();
        
        // Compare ordered clock domains to sampling block time domains
        int     indCurr = 0;
        for (TimeInterval domClock : lstTimeDomains) {
            UniformSamplingBlock    blkCurr = lstBlocksTest.get(indCurr);
            TimeInterval            domCurr = blkCurr.getTimeDomain();  // method under test
            
            Assert.assertEquals("Bad time domain for sampling block " + Integer.toString(indCurr), domClock, domCurr);
            indCurr++;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#getTimestamps()}.
     */
    @Test
    public final void testGetTimestamps() {
        List<BucketData>    lstRawMsgData = LST_QUERY_DATA_LONG;
        
        // Create processed sampling blocks
        List<UniformSamplingBlock>     lstBlocksTest;
        
        try {
            lstBlocksTest = this.process(lstRawMsgData);
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            return;
        }
        
        // Extract all the sampling clock messages within raw data
        List<FixedIntervalTimestampSpec>   lstMsgClocks = lstRawMsgData
                .stream()
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .<FixedIntervalTimestampSpec>map(DataBucket::getSamplingInterval)
                .toList();
           
        // Order all the unique sampling clocks within raw data into a sorted set
        Comparator<FixedIntervalTimestampSpec>  cmpMsgClock = (clk1, clk2) -> {
            return ProtoTime.compare(clk1.getStartTime(), clk2.getStartTime());
        };
        SortedSet<FixedIntervalTimestampSpec>   setMsgClocks = new TreeSet<>(cmpMsgClock);
        setMsgClocks.addAll(lstMsgClocks);
        
        Assert.assertEquals("Incorrect number of unique sampling clocks from raw data.", lstBlocksTest.size(), setMsgClocks.size());

        // Build the ordered list of sample times for each clock
        List<List<Instant>>      lstClockTms = setMsgClocks
                .stream()
                .<List<Instant>>map(msgClock -> {
                                    int     cntSamples = msgClock.getNumSamples();
                                    long    lngPeriodNs = msgClock.getSampleIntervalNanos();
                                    Instant insBegin = ProtoMsg.toInstant(msgClock.getStartTime());
                                    List<Instant>   lstTms = new ArrayList<>(cntSamples);
                                    
                                    Instant insCurr = insBegin;
                                    for (int i=0; i<cntSamples; i++) {
                                        lstTms.add(insCurr);
                                        
                                        insCurr = insCurr.plusNanos(lngPeriodNs);
                                    }
                                    return lstTms;
                                    }
                                )
                .toList();
        
        // Compare ordered clock domains to sampling block time domains
        int     indCurr = 0;
        for (List<Instant> lstTms : lstClockTms) {
            UniformSamplingBlock    blkCurr = lstBlocksTest.get(indCurr);
            List<Instant>           lstTmsCurr = blkCurr.getTimestamps();   // method under test
            
            Assert.assertEquals("Bad timestamp list for sampling block " + Integer.toString(indCurr), lstTms, lstTmsCurr);
            indCurr++;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#getSamplingClock()}.
     */
    @Test
    public final void testGetSamplingClock() {
        List<BucketData>    lstRawMsgData = LST_QUERY_DATA_LONG;
        
        // Create processed sampling blocks
        List<UniformSamplingBlock>     lstBlocksTest;
        
        try {
            lstBlocksTest = this.process(lstRawMsgData);
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            return;
        }
        
        // Extract all the sampling clock messages within raw data
        List<FixedIntervalTimestampSpec>   lstMsgClocks = lstRawMsgData
                .stream()
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .<FixedIntervalTimestampSpec>map(DataBucket::getSamplingInterval)
                .toList();
           
        // Order all the unique sampling clocks within raw data into a sorted set
        Comparator<FixedIntervalTimestampSpec>  cmpMsgClock = (clk1, clk2) -> {
            return ProtoTime.compare(clk1.getStartTime(), clk2.getStartTime());
        };
        SortedSet<FixedIntervalTimestampSpec>   setMsgClocks = new TreeSet<>(cmpMsgClock);
        setMsgClocks.addAll(lstMsgClocks);
        
        Assert.assertEquals("Incorrect number of unique sampling clocks from raw data.", lstBlocksTest.size(), setMsgClocks.size());

        // Compare sampling block clocks to sampling clock messages 
        int     indCurr = 0;
        for (FixedIntervalTimestampSpec msgClock : setMsgClocks) {
            UniformSamplingClock    clkCurr = lstBlocksTest.get(indCurr).getSamplingClock(); // method under test
            
            int     cntSamples = msgClock.getNumSamples();
            long    lngPeriodNs = msgClock.getSampleIntervalNanos();
            Instant insStart = ProtoMsg.toInstant(msgClock.getStartTime());

            Assert.assertEquals("Sample clock sample count incorrect for index " + Integer.toString(indCurr), cntSamples, clkCurr.getSampleCount());
            Assert.assertEquals("Sample clock start time incorrect for index " + Integer.toString(indCurr), insStart, clkCurr.getStartInstant());
            Assert.assertTrue("Sample clock period units NOT in nanoseconds for index " + Integer.toString(indCurr), clkCurr.getSamplePeriodUnits() == ChronoUnit.NANOS);
            Assert.assertEquals("Sample clock period incorrect for index " + Integer.toString(indCurr), lngPeriodNs, clkCurr.getSamplePeriod());
            
            indCurr++;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#getSourceNames()}.
     */
    @Test
    public final void testGetSourceNames() {
        List<BucketData>    lstRawData = LST_QUERY_DATA_WIDE;
        
        // Create processed sampling blocks
        List<UniformSamplingBlock>     lstBlocksTest;
        
        try {
            lstBlocksTest = this.process(lstRawData);
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            return;
        }
        
        // Extract data source names from raw data 
        Set<String>     setSrcNmsRaw = lstRawData
                .stream()
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .<String>map(msgBucket -> msgBucket.getDataColumn().getName())
                .collect(Collectors.toSet());
        
        // Check that all data source names are represented in each block
        // - Note that each data series in results set should be represented within 
        //   each sampling block
        boolean bolResult = lstBlocksTest
                .stream()
                .allMatch(blk -> blk.getSourceNames().containsAll(setSrcNmsRaw));
        
        Assert.assertTrue("Not all data source within raw data represented in each samplineg block.", bolResult);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#getSourceType(java.lang.String)}.
     */
    @Test
    public final void testGetSourceType() {
        List<BucketData>    lstRawData = LST_QUERY_DATA_WIDE;
        
        // Create processed sampling blocks
        List<UniformSamplingBlock>     lstBlocksTest;
        
        try {
            lstBlocksTest = this.process(lstRawData);
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            return;
        }

        // Create map {source name, data type) from raw data
        Map<String, DpSupportedType>    mapTypesRaw = new HashMap<>();
        
        try {
            for (BucketData msgData : lstRawData) {
                for (DataBucket msgBucket : msgData.getDataBucketsList())  {
                    DataColumn      msgCol = msgBucket.getDataColumn();
                    String          strName = msgCol.getName();
                    DpSupportedType enmSupType = ProtoMsg.extractType(msgCol);
                    
                    mapTypesRaw.put(strName, enmSupType);
                }
            }

        } catch (Exception e) {
            Assert.fail(failMessage("Unable to extract data type from raw data", e));
            return;
        }
        
        // Check data source types for all time series in all sampling blocks
        int     indCurr = 0;
        for (UniformSamplingBlock blkCurr : lstBlocksTest) {
            for (Map.Entry<String, DpSupportedType> entry : mapTypesRaw.entrySet()) {
                String              strSrcNm = entry.getKey();
                DpSupportedType     enmTypeRaw = entry.getValue();
                
                try {
                DpSupportedType     enmTypeBlkSrc = blkCurr.getSourceType(strSrcNm); // method under test
                
                Assert.assertEquals("Bad source type for " + strSrcNm + ". block index " + Integer.toString(indCurr), enmTypeRaw, enmTypeBlkSrc);
                
                } catch (Exception e) {
                    Assert.fail(failMessage("Data source not present: " + strSrcNm + ", block index " + Integer.toString(indCurr), e));
                    
                }
            }
            
            indCurr++;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#getTimeSeries(java.lang.String)}.
     */
    @Test
    public final void testGetTimeSeries() {
        DataBucket  msgBucket = BUCKET;

        // Create the single-series sampling block
        UniformSamplingBlock    blkTest;
        try {
            blkTest = this.singleSeries(msgBucket);
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            return;
            
        }
        
        // Extract data values from raw data
        List<Object>    lstValsRaw;
        try {
        lstValsRaw = ProtoMsg.extractValues(msgBucket.getDataColumn());
        
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            return;
            
        }
        
        // Compare sampling block time series to raw data
        String              strSrcNm = msgBucket.getDataColumn().getName();
        SampledTimeSeries   serPrcd = blkTest.getTimeSeries(strSrcNm);
        Assert.assertNotEquals(null, serPrcd);
        
        List<Object>        lstValsPrcd = serPrcd.getValues();
        Assert.assertEquals(lstValsRaw, lstValsPrcd);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#getTimeSeriesAll()}.
     */
    @Test
    public final void testGetTimeSeriesAll() {
        List<BucketData>    lstRawData = LST_QUERY_DATA_WIDE;
        
        // Create processed sampling blocks
        List<UniformSamplingBlock>     lstBlocksTest;
        
        try {
            lstBlocksTest = this.process(lstRawData);
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            return;
        }

        // Extract data source names from raw data 
        Set<String>     setSrcNmsRaw = lstRawData
                .stream()
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .<String>map(msgBucket -> msgBucket.getDataColumn().getName())
                .collect(Collectors.toSet());
        
        // Check that all data source names are represented as time series within each block
        // - Note that each data series in results set should be represented within 
        //   each sampling block
        boolean bolResult = lstBlocksTest
                .stream()
                .allMatch(blk -> blk.getTimeSeriesAll()     // method under test
                                    .keySet()
                                    .containsAll(setSrcNmsRaw)
                        );
        
        Assert.assertTrue("Not all data source within raw data have time-series within each samplineg block.", bolResult);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#hasDomainIntersection(com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock)}.
     */
    @Test
    public final void testHasDomainIntersection() {
        List<BucketData>    lstRawData = LST_QUERY_DATA_LONG;
        
        // Create processed sampling blocks
        List<UniformSamplingBlock>     lstBlocksTest;
        
        try {
            lstBlocksTest = this.process(lstRawData);
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            return;
        }

        // Loop through sampling blocks looking for domain collisions 
        int                     indPrev = 0;
        UniformSamplingBlock    blkPrev = null;
        for (UniformSamplingBlock blkCurr : lstBlocksTest) {
            
            // Loop initialization - first time through
            if (blkPrev == null) {
                blkPrev = blkCurr;
                continue;
            }
            
            // Compare adjacent time domains
            boolean bolIntersect = blkPrev.hasDomainIntersection(blkCurr);  // method under test
            
            Assert.assertFalse("Time domain collision at sampling block index " + Integer.toString(indPrev), bolIntersect);
            
            indPrev++;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#createTimestamps()}.
     */
    @Test
    public final void testCreateTimestamps() {
        // This is already verified by getTimestamps() (timestamps are created by the sampling clock)
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#insertEmptyTimeSeries(java.lang.String, com.ospreydcs.dp.api.model.DpSupportedType)}.
     */
    @Test
    public final void testInsertEmptyTimeSeries() {
        DataBucket  msgBucket = BUCKET;

        // Create the single-series sampling block
        UniformSamplingBlock    blkTest;
        try {
            blkTest = this.singleSeries(msgBucket);
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            return;
            
        }

        // Add empty time series 
        final String            strDummy = "PV_EMPTY";
        final DpSupportedType   enmDummy = DpSupportedType.IMAGE;
        
        boolean bolInserted = blkTest.insertEmptyTimeSeries(strDummy, enmDummy);    // method under test
        
        Assert.assertTrue(strDummy + " empty time series FAILED insertion.", bolInserted);
        
        // Check the empty time series
        SampledTimeSeries   serDummy = blkTest.getTimeSeries(strDummy);
        
        Assert.assertNotEquals(strDummy + " empty times series could NOT be recovered.", null, serDummy);
        
        List<Object>    lstDumVals = serDummy.getValues();
        
        Assert.assertEquals(strDummy + "empty time series has wrong size.", blkTest.getSampleCount(), lstDumVals.size());
        
        boolean bolAllNull = lstDumVals.stream().allMatch(o -> o == null);
        Assert.assertTrue(strDummy + "empty time series was NOT all null.", bolAllNull);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock#compareTo(com.ospreydcs.dp.api.query.model.time.UniformSamplingBlock)}.
     */
    @Test
    public final void testCompareTo() {
        List<BucketData>    lstRawData = LST_QUERY_DATA_LONG;
        
        // Create processed sampling blocks
        List<UniformSamplingBlock>     lstBlocksTest;
        
        try {
            lstBlocksTest = this.process(lstRawData);
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedCallerNameSimple(), e));
            return;
        }

        // Loop through sampling blocks looking making comparison for ordering 
        int                     indPrev = 0;
        UniformSamplingBlock    blkPrev = null;
        for (UniformSamplingBlock blkCurr : lstBlocksTest) {
            
            // Loop initialization - first time through
            if (blkPrev == null) {
                blkPrev = blkCurr;
                continue;
            }
            
            // Compare adjacent time domains
            int intCmp = blkPrev.compareTo(blkCurr);  // method under test
            
            Assert.assertFalse("CompareTo() shows bad ordering at sampling block index " + Integer.toString(indPrev), intCmp >= 0);
            
            indPrev++;
        }
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates a new <code>{@link UniformSamplingBlock}</code> instance with a single time series
     * from the argument.
     * </p>
     * <p>
     * All exceptions originate from the internal invocation of creator
     * <code>{@link UniformSamplingBlock#from(CorrelatedQueryData)}</code>.
     * </p>
     * 
     * @param msgBucket <code>DataBucket</code> message containing data for single time series
     * 
     * @return  a new <code>UniformSamplingBlock</code> created from argument
     * 
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws UnsupportedOperationException an unsupported data type was detected within the argument
     */
    private UniformSamplingBlock    singleSeries(DataBucket msgBucket) 
            throws MissingResourceException, IllegalArgumentException, IllegalStateException, UnsupportedOperationException
            {
        
        CorrelatedQueryData cqdSingle = CorrelatedQueryData.from(msgBucket);
        
        UniformSamplingBlock    blkSingle = UniformSamplingBlock.from(cqdSingle);

        return blkSingle;
    }
    
    /**
     * <p>
     * Processes the results set from a data query request into ordered sampling blocks.
     * </p>
     * <p>
     * Uses the <code>{@link #CORRELATOR}</code> singleton to process the given data into
     * a sorted set of correlated data.  
     * (Resets the <code>CORRELATOR</code> singleton before
     * processing.)  
     * Creates an ordered list of sampling blocks from the sorted correlated set.
     * </p>
     * <p>
     * All exceptions originate from the internal invocation of creator
     * <code>{@link UniformSamplingBlock#from(CorrelatedQueryData)}</code>.
     * </p>
     *  
     * @param lstRawData    raw query results set data 
     * 
     * @return  ordered list of sampling blocks processed from given raw data
     * 
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws UnsupportedOperationException an unsupported data type was detected within the argument
     */
    private List<UniformSamplingBlock>    process(List<BucketData> lstRawData) 
            throws MissingResourceException, IllegalArgumentException, IllegalStateException, UnsupportedOperationException
            {
        
        // Correlate raw data
        CORRELATOR.reset();
        lstRawData.forEach(msgData -> CORRELATOR.addQueryData(msgData));
        
        SortedSet<CorrelatedQueryData>   setPrcdData = CORRELATOR.getCorrelatedSet();
        
        // Convert to ordered list of sampling blocks and return
        List<UniformSamplingBlock>  lstBlocks = setPrcdData
                .stream()
                .map(UniformSamplingBlock::from)
                .toList();
        
        return lstBlocks;
    }
    
    /**
     * <p>
     * Correlates the given Query Service data into a processed data set and returns it.
     * </p>
     * <p>
     * Uses the <code>{@link #CORRELATOR}</code> singleton to process the given data into
     * a sorted set of correlated data.  Resets the <code>CORRELATOR</code> singleton before
     * processing.
     * </p>
     * 
     * @param lstRawData   raw query results set data to be correlated
     * 
     * @return  a sorted set of <code>CorrelatedQueryData</code> objects 
     */
    private SortedSet<CorrelatedQueryData>  correlate(List<BucketData> lstRawData) {
        
        CORRELATOR.reset();
        lstRawData.forEach(msgData -> CORRELATOR.addQueryData(msgData));
        
        SortedSet<CorrelatedQueryData>   setPrcdData = CORRELATOR.getCorrelatedSet();
        
        return setPrcdData;
    }
    
    /**
     * Creates an exception failure message from the given arguments
     * 
     * @param strHdr    message header
     * @param e         exception causing the failure
     * 
     * @return          message describing the exception failure
     */
    private static String  failMessage(String strHdr, Exception e) {
        String strMsg = strHdr + " - threw exception " + e.getClass().getSimpleName() + ": " + e.getMessage();
        
        return strMsg;
    }
}
