/*
 * Project: dp-api-common
 * File:	CorrelatedQueryDataTest.java
 * Package: com.ospreydcs.dp.api.query.model.grpc
 * Type: 	CorrelatedQueryDataTest
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
 * @since Feb 10, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.grpc;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.grpc.util.ProtoTime;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.query.test.TestQueryResponses.SingleQueryType;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.SamplingClock;
import com.ospreydcs.dp.grpc.v1.common.Timestamp;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData.DataBucket;

/**
 * JUnit test cases for <code>{@link CorrelatedQueryData}</code>.
 *
 * @author Christopher K. Allen
 * @since Feb 10, 2024
 *
 */
public class CorrelatedQueryDataTest {

    
    //
    // Test Resources
    //
    
    /** Test data - single bucket query */
    public static final DataBucket          BUCKET = TestQueryResponses.queryBuckets(SingleQueryType.BUCKET).get(0);
    
    /** Test data - 1 data source - 10 seconds */
    public static final List<DataBucket>    LST_BUCKETS_ONE = TestQueryResponses.queryBuckets(SingleQueryType.ONE_SOURCE);
    
    /** Test data - 2 data sources - 2 seconds */
    public static final List<DataBucket>    LST_BUCKETS_TWO = TestQueryResponses.queryBuckets(SingleQueryType.TWO_SOURCE);

    /** Test data - 5 data source bucket list - 60 seconds */
    public static final List<DataBucket>    LST_BUCKETS_LONG = TestQueryResponses.queryBuckets(SingleQueryType.LONG);
    
    /** Test data - 100 data source bucket list - 5 seconds */
    public static final List<DataBucket>    LST_BUCKETS_WIDE = TestQueryResponses.queryBuckets(SingleQueryType.WIDE);
    
    
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData#from(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData.DataBucket)}.
     */
    @Test
    public final void testFrom() {
        DataBucket  msgBucket = BUCKET;
        
        CorrelatedQueryData cqdTest = CorrelatedQueryData.from(msgBucket);
        
        Assert.assertTrue("Number of data sources NOT equal to 1.", cqdTest.getSourceCount() == 1);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData#CorrelatedQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData.DataBucket)}.
     */
    @Test
    public final void testCorrelatedQueryData() {
        DataBucket  msgBucket = BUCKET;
        
        CorrelatedQueryData cqdTest = new CorrelatedQueryData(msgBucket);
        
        Assert.assertTrue("Number of data sources NOT equal to 1.", cqdTest.getSourceCount() == 1);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData#getSampleCount()}.
     */
    @Test
    public final void testGetSampleCount() {
        DataBucket          msgBucket = BUCKET;
        
        // Check bucket for sampling clock
        if (!msgBucket.getDataTimestamps().hasSamplingClock())
            Assert.fail("Data bucket does NOT contain a sampling clock.");
        
        // Get the sample count for the target sampling clock
        final int   cntSamples = msgBucket.getDataTimestamps().getSamplingClock().getCount();
        
        // Create initialized correlated data object 
        CorrelatedQueryData cqdTest = CorrelatedQueryData.from(msgBucket);
        
        // Compare the sample counts
        Assert.assertEquals(cntSamples, cqdTest.getSampleCount());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData#getStartInstant()}.
     */
    @Test
    public final void testGetStartInstant() {
        DataBucket          msgBucket = BUCKET;

        // Check bucket for sampling clock
        if (!msgBucket.getDataTimestamps().hasSamplingClock())
            Assert.fail("Data bucket does NOT contain a sampling clock.");
        
        // Get the start time for the sampling clock
        Timestamp   tmsStart = msgBucket.getDataTimestamps().getSamplingClock().getStartTime();
        Instant     insStart = ProtoMsg.toInstant(tmsStart);
        
        // Create initialized correlated data object 
        CorrelatedQueryData cqdTest = CorrelatedQueryData.from(msgBucket);
        
        // Compare start times
        Assert.assertEquals(insStart, cqdTest.getStartInstant());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData#getTimeDomain()}.
     * <p> 
     * Within a <code>CorrelatedQueryData</code> instance the time domain is the smallest, connected
     * time interval that contains all sample points. 
     */
    @Test
    public final void testGetTimeDomain() {
        DataBucket          msgBucket = BUCKET;

        // Check bucket for sampling clock
        if (!msgBucket.getDataTimestamps().hasSamplingClock())
            Assert.fail("Data bucket does NOT contain a sampling clock.");
        
        // Get the start time, period, and sample count for the sampling clock
        SamplingClock   msgClock = msgBucket.getDataTimestamps().getSamplingClock();
        
        Timestamp   tmsStart = msgClock.getStartTime();
        int         cntSamples = msgClock.getCount();
        long        lngPeriodNs = msgClock.getPeriodNanos();
        
        // Create the time domain interval for the bucket samples
        //  NOTE - the last sample period is NOT included => duration = (cntSamples-1)*lngPeriodNs
        Instant         insStart = ProtoMsg.toInstant(tmsStart);
        Instant         insStop = insStart.plusNanos((cntSamples-1) * lngPeriodNs);
        TimeInterval    domSamples = TimeInterval.from(insStart, insStop);
        
        // Create initialized correlated data object 
        CorrelatedQueryData cqdTest = CorrelatedQueryData.from(msgBucket);
        
        // Compare time domains
        Assert.assertEquals(domSamples, cqdTest.getTimeDomain());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData#getSourceNames()}.
     */
    @Test
    public final void testGetSourceNames() {
        List<DataBucket>    lstBuckets = LST_BUCKETS_WIDE;
        DataBucket          msgBucket = lstBuckets.get(0);
        
        // Create initialized correlated data object 
        CorrelatedQueryData cqdTest = CorrelatedQueryData.from(msgBucket);
        
        // Attempt to insert all the data - should be multiple accepted/rejected
        List<DataBucket>    lstInserted = lstBuckets
                .stream()
                .filter(bucket -> cqdTest.insertBucketData(bucket))
                .toList();
        
        Assert.assertTrue("Too many bucket insertion acceptanes", lstInserted.size() < lstBuckets.size());
        
        // Extract the unique source names from the source data buckets
        List<String>    lstSrcNmsBuckets = lstBuckets
                .stream()
                .<DataColumn>map(DataBucket::getDataColumn)
                .<String>map(DataColumn::getName)
                .toList();
        
        Set<String> setSrcNmsBuckets = new TreeSet<>(lstSrcNmsBuckets);
        
        // Get the unique source names within the correlated query data and compare
        Set<String> setSrcNms = cqdTest.getSourceNames();
        
        Assert.assertEquals(setSrcNmsBuckets, setSrcNms);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData#getSamplingMessage()}.
     */
    @Test
    public final void testGetSamplingMessage() {
        List<DataBucket>    lstBuckets = LST_BUCKETS_WIDE;
        DataBucket          msgBucket = lstBuckets.get(0);
        
        // Create initialized correlated data object 
        CorrelatedQueryData cqdTest = CorrelatedQueryData.from(msgBucket);
        
        // Attempt to insert all the data - should be multiple accepted/rejected
        List<DataBucket>    lstInserted = lstBuckets
                .stream()
                .filter(bucket -> cqdTest.insertBucketData(bucket))
                .toList();
        
        Assert.assertTrue("Too many bucket insertion acceptanes", lstInserted.size() < lstBuckets.size());

        // Check bucket for sampling clock
        if (!msgBucket.getDataTimestamps().hasSamplingClock())
            Assert.fail("Data bucket does NOT contain a sampling clock.");
        
        // Compare the sample clocks of the initializing bucket and the correlated data
        SamplingClock  msgClockOrig = msgBucket.getDataTimestamps().getSamplingClock();
        SamplingClock  msgClockData = cqdTest.getSamplingMessage();
        
        Assert.assertEquals(msgClockOrig, msgClockData);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData#getAllDataMessages()}.
     */
    @Test
    public final void testGetAllDataMessages() {
        List<DataBucket>    lstBuckets = LST_BUCKETS_WIDE;
        DataBucket          msgBucket = lstBuckets.get(0);
        
        // Create initialized correlated data object 
        CorrelatedQueryData cqdTest = CorrelatedQueryData.from(msgBucket);
        
        // Attempt to insert all the data - should be multiple accepted/rejected
        List<DataBucket>    lstInserted = lstBuckets
                .stream()
                .filter(bucket -> cqdTest.insertBucketData(bucket))
                .toList();
        
        Assert.assertTrue("Too many bucket insertion acceptanes", lstInserted.size() < lstBuckets.size());
        
        // Check buckets for sampling clock
        if ( !lstBuckets.stream().allMatch(msgBck -> msgBck.getDataTimestamps().hasSamplingClock()) )
            Assert.fail("Data buckets do NOT all contain a sampling clock.");
        
        // Extract all data columns with the same sampling clock as the initiating data bucket
        SamplingClock msgClock = msgBucket.getDataTimestamps().getSamplingClock();
        
        List<DataColumn>    lstColsBuckets = lstBuckets
                .stream()
                .filter(msgBck -> ProtoTime.equals(msgClock, msgBck.getDataTimestamps().getSamplingClock()))
                .<DataColumn>map(DataBucket::getDataColumn)
                .toList();
        
        // Get all the data columns within the correlated data and compare
        List<DataColumn>    lstCols = cqdTest.getAllDataMessages();
        
        Assert.assertTrue("The correlated data does not contain all required DataColumn messages.", lstColsBuckets.containsAll(lstCols));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData#verifySourceUniqueness()}.
     */
    @Test
    public final void testVerifySourceUniqueness() {
        List<DataBucket>    lstBuckets = LST_BUCKETS_WIDE;
        DataBucket          msgBucket = lstBuckets.get(0);
        
        // Create initialized correlated data object 
        CorrelatedQueryData cqdTest = CorrelatedQueryData.from(msgBucket);
        
        // Attempt to insert all the data - should be multiple accepted/rejected
        List<DataBucket>    lstInserted = lstBuckets
                .stream()
                .filter(bucket -> cqdTest.insertBucketData(bucket))
                .toList();
        
        Assert.assertTrue("Too many bucket insertion acceptanes", lstInserted.size() < lstBuckets.size());

        // Apply the verifySourceUniqueness() operation
        ResultStatus recUnique = cqdTest.verifySourceUniqueness();
        
        if (recUnique.isFailure())
            Assert.fail(recUnique.message());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData#verifySourceSizes()}.
     */
    @Test
    public final void testVerifySourceSizes() {
        List<DataBucket>    lstBuckets = LST_BUCKETS_WIDE;
        DataBucket          msgBucket = lstBuckets.get(0);
        
        // Create initialized correlated data object 
        CorrelatedQueryData cqdTest = CorrelatedQueryData.from(msgBucket);
        
        // Attempt to insert all the data - should be multiple accepted/rejected
        List<DataBucket>    lstInserted = lstBuckets
                .stream()
                .filter(bucket -> cqdTest.insertBucketData(bucket))
                .toList();
        
        Assert.assertTrue("Too many bucket insertion acceptanes", lstInserted.size() < lstBuckets.size());

        // Apply the verifySourceSizes() operation
        ResultStatus recSizes = cqdTest.verifySourceSizes();
        
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData#insertBucketData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData.DataBucket)}.
     */
    @Test
    public final void testInsertBucketData() {
        List<DataBucket>    lstBuckets = LST_BUCKETS_TWO;
        DataBucket          msgBucket = lstBuckets.get(0);
        
        // Create initialized correlated data object 
        CorrelatedQueryData cqdTest = CorrelatedQueryData.from(msgBucket);
        
        // Attempt to insert all the data - should be multiple accepted/rejected
        List<DataBucket>    lstInserted = lstBuckets
                .stream()
                .filter(bucket -> cqdTest.insertBucketData(bucket))
                .toList();
        
        Assert.assertTrue("Too many bucket insertion acceptanes", lstInserted.size() < lstBuckets.size());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData#insertBucketData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData.DataBucket)}.
     */
    @Test
    public final void testInsertBucketDataRepeated() {
        DataBucket          msgBucket = BUCKET;
        
        // Create initialized correlated data object 
        CorrelatedQueryData cqdTest = CorrelatedQueryData.from(msgBucket);
        
        // Attempt to insert the same bucket again
        boolean bolInsert = cqdTest.insertBucketData(msgBucket);
        
        Assert.assertFalse("Repeated bucket insertion accepted.", bolInsert);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData#compareTo(com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData)}.
     */
    @Test
    public final void testCompareTo() {
        List<DataBucket>    lstBuckets = LST_BUCKETS_ONE;
        
        // Create a sorted set of correlated data 
        SortedSet<CorrelatedQueryData>  setData = new TreeSet<>(CorrelatedQueryData.StartTimeComparator.newInstance());
        
        lstBuckets.forEach(msgBucket -> setData.add(CorrelatedQueryData.from(msgBucket)));
        
        // Compare each element in the set for ordering
        int                     indPrev = 0;
        CorrelatedQueryData     cqdPrev = null;
        for (CorrelatedQueryData cqdCurr : setData) {
            if (cqdPrev == null) {
                cqdPrev = cqdCurr;
                continue;
            }
                
            if (cqdPrev.compareTo(cqdCurr) >= 0) {
                Assert.fail("Found bad CorrelatedQueryData ordering at index " + Integer.toString(indPrev));
                return;
            }
            
            cqdPrev = cqdCurr;
            indPrev++;
        }
    }

}
