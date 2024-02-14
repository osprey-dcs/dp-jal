/*
 * Project: dp-api-common
 * File:	QueryDataCorrelatorTest.java
 * Package: com.ospreydcs.dp.api.query.model.proto
 * Type: 	QueryDataCorrelatorTest
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
 * @since Jan 13, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.proto;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.naming.CannotProceedException;
import javax.naming.OperationNotSupportedException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.model.ResultRecord;
import com.ospreydcs.dp.api.model.TimeInterval;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.query.test.TestQueryResponses.SingleQueryType;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData;

/**
 * <p>
 * JUnit test cases for the <code>{@link QueryDataCorrelator}</code> class.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Jan 13, 2024
 *
 * @see QueryDataCorrelator
 */
public class QueryDataCorrelatorTest {

    
    //
    // Class Constants
    //
    
    
    //
    // Class Resources
    //
    
    /** Sample query response for test cases */
    public static final List<QueryResponse>   LST_QUERY_RSP = TestQueryResponses.queryResults(SingleQueryType.WIDE);
    
    
    /** Sample query data for test cases - 1 source, 10 seconds */
    public static final List<BucketData>   LST_QUERY_DATA_ONE = TestQueryResponses.queryData(SingleQueryType.ONE_SOURCE);
    
    /** Sample query data for test cases - 2 sources, 2 seconds */
    public static final List<BucketData>   LST_QUERY_DATA_TWO = TestQueryResponses.queryData(SingleQueryType.TWO_SOURCE);
    
    /** Sample query data for test cases - 100 sources, 5 seconds */
    public static final List<BucketData>   LST_QUERY_DATA_WIDE = TestQueryResponses.queryData(SingleQueryType.WIDE);
    
    /** Sample query data for test cases - 5 sources, 60 seconds */
    public static final List<BucketData>   LST_QUERY_DATA_LONG = TestQueryResponses.queryData(SingleQueryType.LONG);
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        
//        List<QueryResponse> lstRspsWide = TestQueryResponses.queryResults(SingleQueryType.WIDE);
//        LST_QUERY_DATA_WIDE = lstRspsWide.stream().map(m -> m.getQueryReport().getBucketData()).toList();
//        
//        List<QueryResponse> lstRspsLong = TestQueryResponses.queryResults(SingleQueryType.LONG);
//        LST_QUERY_DATA_LONG = lstRspsLong.stream().map(m -> m.getQueryReport().getBucketData()).toList();
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryDataCorrelator#getProcessedSet()}.
     */
    @Test
    public final void testGetTargetSet() {

        // Create new processor and add data stream
        QueryDataCorrelator prcrTest = QueryDataCorrelator.newInstance();
        
        // Get the empty processed data set and try to verify
        SortedSet<CorrelatedQueryData>  setPrcdData = prcrTest.getProcessedSet();
        
        ResultRecord    recOrder = QueryDataCorrelator.verifyOrdering(setPrcdData);
        Assert.assertTrue("QueryDataCorrelator verified order of empty data set.", recOrder.isFailure());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryDataCorrelator#insertBucketData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData.DataBucket)}.
     */
    @Test
    public final void testInsertQueryResponse() {
        List<QueryResponse>  lstRsps = LST_QUERY_RSP;
        
        // Create new processor and add data stream
        QueryDataCorrelator prcrTest = QueryDataCorrelator.newInstance();
        
        try {
            for (QueryResponse msgRsp : lstRsps) 
                prcrTest.insertQueryResponse(msgRsp);
            
        } catch (OperationNotSupportedException | CannotProceedException e) {
            Assert.fail("Exception thrown will processing data: type=" + e.getClass().getSimpleName() + ", message=" + e.getMessage());

        }
        
        // Perform available verification checks
        SortedSet<CorrelatedQueryData>   setPrcdData = prcrTest.getProcessedSet();
        
        ResultRecord    recOrder = QueryDataCorrelator.verifyOrdering(setPrcdData);
        if (recOrder.isFailure())
            Assert.fail(recOrder.message());
        
        ResultRecord    recSizes = QueryDataCorrelator.verifyColumnSizes(setPrcdData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        ResultRecord    recDomains = QueryDataCorrelator.verifyTimeDomains(setPrcdData);
        if (recDomains.isFailure())
            Assert.fail(recDomains.message());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryDataCorrelator#insertQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)}.
     */
    @Test
    public final void testInsertQueryDataOne() {
        List<BucketData>        lstRawData = LST_QUERY_DATA_ONE;
        
        // Create new processor and add raw data
        QueryDataCorrelator  prcrTest = new QueryDataCorrelator();
        
        for (QueryResponse.QueryReport.BucketData msgData : lstRawData) {
            prcrTest.insertQueryData(msgData);
        }
        
        Assert.assertTrue("QueryDataCorrelator has no data.", prcrTest.sizeProcessedSet() > 0);
        
        // Perform available verification checks
        SortedSet<CorrelatedQueryData>   setPrcdData = prcrTest.getProcessedSet();
        
        ResultRecord    recOrder = QueryDataCorrelator.verifyOrdering(setPrcdData);
        if (recOrder.isFailure())
            Assert.fail(recOrder.message());
        
        ResultRecord    recSizes = QueryDataCorrelator.verifyColumnSizes(setPrcdData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        ResultRecord    recDomains = QueryDataCorrelator.verifyTimeDomains(setPrcdData);
        if (recDomains.isFailure())
            Assert.fail(recDomains.message());

        // Printout processed set
        this.printProperties(JavaRuntime.getQualifiedCallerNameSimple(), prcrTest.getProcessedSet());
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryDataCorrelator#insertQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)}.
     */
    @Test
    public final void testInsertQueryDataTwo() {
        List<BucketData>        lstRawData = LST_QUERY_DATA_TWO;
        
        // Create new processor and add raw data
        QueryDataCorrelator  prcrTest = new QueryDataCorrelator();
        
        for (QueryResponse.QueryReport.BucketData msgData : lstRawData) {
            prcrTest.insertQueryData(msgData);
        }
        
        Assert.assertTrue("QueryDataCorrelator has no data.", prcrTest.sizeProcessedSet() > 0);
        
        // Perform available verification checks
        SortedSet<CorrelatedQueryData>   setPrcdData = prcrTest.getProcessedSet();
        
        ResultRecord    recOrder = QueryDataCorrelator.verifyOrdering(setPrcdData);
        if (recOrder.isFailure())
            Assert.fail(recOrder.message());
        
        ResultRecord    recSizes = QueryDataCorrelator.verifyColumnSizes(setPrcdData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        ResultRecord    recDomains = QueryDataCorrelator.verifyTimeDomains(setPrcdData);
        if (recDomains.isFailure())
            Assert.fail(recDomains.message());

        // Printout processed set
        this.printProperties(JavaRuntime.getQualifiedCallerNameSimple(), prcrTest.getProcessedSet());
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryDataCorrelator#insertQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)}.
     */
    @Test
    public final void testInsertQueryDataLong() {
        List<BucketData>        lstRawData = LST_QUERY_DATA_LONG;
        
        // Create new processor and add raw data
        QueryDataCorrelator  prcrTest = new QueryDataCorrelator();
        
        for (QueryResponse.QueryReport.BucketData msgData : lstRawData) {
            prcrTest.insertQueryData(msgData);
        }
        
        Assert.assertTrue("QueryDataCorrelator has no data.", prcrTest.sizeProcessedSet() > 0);
        
        // Perform available verification checks
        SortedSet<CorrelatedQueryData>   setPrcdData = prcrTest.getProcessedSet();
        
        ResultRecord    recOrder = QueryDataCorrelator.verifyOrdering(setPrcdData);
        if (recOrder.isFailure())
            Assert.fail(recOrder.message());
        
        ResultRecord    recSizes = QueryDataCorrelator.verifyColumnSizes(setPrcdData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        ResultRecord    recDomains = QueryDataCorrelator.verifyTimeDomains(setPrcdData);
        if (recDomains.isFailure())
            Assert.fail(recDomains.message());
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryDataCorrelator#insertQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)}.
     */
    @Test
    public final void testInsertQueryDataWide() {
        List<BucketData>        lstRawData = LST_QUERY_DATA_WIDE;
        
        QueryDataCorrelator  prcrTest = new QueryDataCorrelator();
        
        for (QueryResponse.QueryReport.BucketData msgData : lstRawData) {
            prcrTest.insertQueryData(msgData);
        }
        
        Assert.assertTrue("QueryDataCorrelator has no data.", prcrTest.sizeProcessedSet() > 0);

        // Extract data source names and print them out
        Set<String> setDataSrcs = prcrTest.extractDataSourceNames();
        
        System.out.println("Data Source Names: " + setDataSrcs);
        
        
        // Perform available verification checks
        SortedSet<CorrelatedQueryData>   setPrcdData = prcrTest.getProcessedSet();
        
        ResultRecord    recOrder = QueryDataCorrelator.verifyOrdering(setPrcdData);
        if (recOrder.isFailure())
            Assert.fail(recOrder.message());
        
        ResultRecord    recSizes = QueryDataCorrelator.verifyColumnSizes(setPrcdData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        ResultRecord    recDomains = QueryDataCorrelator.verifyTimeDomains(setPrcdData);
        if (recDomains.isFailure())
            Assert.fail(recDomains.message());
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * Prints out the properties of each <code>CorrelatedQueryData</code> instance within the argument,
     * in order.
     * 
     * @param strHdr    header message for print out
     * @param setData   processed data set to be displayed
     */
    private void printProperties(String strHdr, SortedSet<CorrelatedQueryData> setData) {
        
        // Print out header
        System.out.println(strHdr + " - CorrelatedQueryData Ordered Set");
        
        // Print out properties of each processed data set
        int     indCurr = 0;
        for (CorrelatedQueryData data : setData) {
            System.out.println("  CorrelatedQueryData instance #" + Integer.toString(indCurr));
            System.out.println("  source count: " + data.getSourceCount());
            System.out.println("  data sources: " + data.getSourceNames());
            System.out.println("  start instant: " + data.getStartInstant());
            System.out.println("  time range: " + data.getTimeDomain());
            System.out.println("  sample count: " + data.getSampleCount());
            
            indCurr++;
        }
    }

}
