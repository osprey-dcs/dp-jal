/*
 * Project: dp-api-common
 * File:	QueryDataCorrelatorTest.java
 * Package: com.ospreydcs.dp.api.query.model.grpc
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
package com.ospreydcs.dp.api.query.model.grpc;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import javax.naming.CannotProceedException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.model.series.UniformSamplingBlockTest;
import com.ospreydcs.dp.api.query.test.TestDpDataRequestGenerator;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.query.test.TestQueryResponses.SingleQueryType;
import com.ospreydcs.dp.api.query.test.TestQueryService;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

/**
 * <p>
 * JUnit test cases for the <code>{@link QueryDataCorrelator}</code> class.
 * </p>
 * <p>
 * Note that the integrity of the processed data is checked in 
 * <code>{@link UniformSamplingBlockTest}</code>.
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
    public static final List<QueryDataResponse>   LST_QUERY_RSP = TestQueryResponses.queryResults(SingleQueryType.WIDE);
    
    
    /** Sample query data for test cases - 1 source, 10 seconds */
    public static final List<QueryData>   LST_QUERY_DATA_ONE = TestQueryResponses.queryData(SingleQueryType.ONE_SOURCE);
    
    /** Sample query data for test cases - 2 sources, 2 seconds */
    public static final List<QueryData>   LST_QUERY_DATA_TWO = TestQueryResponses.queryData(SingleQueryType.TWO_SOURCE);
    
    /** Sample query data for test cases - 100 sources, 5 seconds */
    public static final List<QueryData>   LST_QUERY_DATA_WIDE = TestQueryResponses.queryData(SingleQueryType.WIDE);
    
    /** Sample query data for test cases - 5 sources, 60 seconds */
    public static final List<QueryData>   LST_QUERY_DATA_LONG = TestQueryResponses.queryData(SingleQueryType.LONG);
    
    
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryDataCorrelator#getCorrelatedSet()}.
     */
    @Test
    public final void testGetTargetSet() {

        // Create new processor and add data stream
        QueryDataCorrelator prcrTest = QueryDataCorrelator.newInstance();
        
        // Get the empty processed data set and try to verify
        SortedSet<CorrelatedQueryData>  setPrcdData = prcrTest.getCorrelatedSet();
        
        ResultStatus    recOrder = QueryDataCorrelator.verifyOrdering(setPrcdData);
        Assert.assertTrue("QueryDataCorrelator verified order of empty data set.", recOrder.isFailure());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryDataCorrelator#addBucketData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData.DataBucket)}.
     */
    @Test
    public final void testInsertQueryResponse() {
        List<QueryDataResponse>  lstRsps = LST_QUERY_RSP;
        
        // Create new processor and add data stream
        QueryDataCorrelator prcrTest = QueryDataCorrelator.newInstance();
        
        try {
            for (QueryDataResponse msgRsp : lstRsps) 
                prcrTest.addQueryResponse(msgRsp);
            
        } catch (IllegalArgumentException | CompletionException | ExecutionException | CannotProceedException e) {
            Assert.fail("Exception thrown will processing data: type=" + e.getClass().getSimpleName() + ", message=" + e.getMessage());

        }
        
        // Perform available verification checks
        SortedSet<CorrelatedQueryData>   setPrcdData = prcrTest.getCorrelatedSet();
        
        ResultStatus    recOrder = QueryDataCorrelator.verifyOrdering(setPrcdData);
        if (recOrder.isFailure())
            Assert.fail(recOrder.message());
        
        ResultStatus    recSizes = QueryDataCorrelator.verifyColumnSizes(setPrcdData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        ResultStatus    recDomains = QueryDataCorrelator.verifyTimeDomains(setPrcdData);
        if (recDomains.isFailure())
            Assert.fail(recDomains.message());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryDataCorrelator#addQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)}.
     */
    @Test
    public final void testInsertQueryDataOne() {
        List<QueryData>        lstRawData = LST_QUERY_DATA_ONE;
        
        // Create new processor and add raw data
        QueryDataCorrelator  prcrTest = new QueryDataCorrelator();
        
        for (QueryDataResponse.QueryData msgData : lstRawData) {
            prcrTest.addQueryData(msgData);
        }
        
        Assert.assertTrue("QueryDataCorrelator has no data.", prcrTest.sizeCorrelatedSet() > 0);
        
        // Perform available verification checks
        SortedSet<CorrelatedQueryData>   setPrcdData = prcrTest.getCorrelatedSet();
        
        ResultStatus    recOrder = QueryDataCorrelator.verifyOrdering(setPrcdData);
        if (recOrder.isFailure())
            Assert.fail(recOrder.message());
        
        ResultStatus    recSizes = QueryDataCorrelator.verifyColumnSizes(setPrcdData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        ResultStatus    recDomains = QueryDataCorrelator.verifyTimeDomains(setPrcdData);
        if (recDomains.isFailure())
            Assert.fail(recDomains.message());

        // Printout processed set
        this.printProperties(JavaRuntime.getQualifiedCallerNameSimple(), prcrTest.getCorrelatedSet());
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryDataCorrelator#addQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)}.
     */
    @Test
    public final void testInsertQueryDataTwo() {
        List<QueryData>        lstRawData = LST_QUERY_DATA_TWO;
        
        // Create new processor and add raw data
        QueryDataCorrelator  prcrTest = new QueryDataCorrelator();
        
        for (QueryDataResponse.QueryData msgData : lstRawData) {
            prcrTest.addQueryData(msgData);
        }
        
        Assert.assertTrue("QueryDataCorrelator has no data.", prcrTest.sizeCorrelatedSet() > 0);
        
        // Perform available verification checks
        SortedSet<CorrelatedQueryData>   setPrcdData = prcrTest.getCorrelatedSet();
        
        ResultStatus    recOrder = QueryDataCorrelator.verifyOrdering(setPrcdData);
        if (recOrder.isFailure())
            Assert.fail(recOrder.message());
        
        ResultStatus    recSizes = QueryDataCorrelator.verifyColumnSizes(setPrcdData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        ResultStatus    recDomains = QueryDataCorrelator.verifyTimeDomains(setPrcdData);
        if (recDomains.isFailure())
            Assert.fail(recDomains.message());

        // Printout processed set
        this.printProperties(JavaRuntime.getQualifiedCallerNameSimple(), prcrTest.getCorrelatedSet());
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryDataCorrelator#addQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)}.
     */
    @Test
    public final void testInsertQueryDataLong() {
        List<QueryData>        lstRawData = LST_QUERY_DATA_LONG;
        
        // Create new processor and add raw data
        QueryDataCorrelator  prcrTest = new QueryDataCorrelator();
        
        for (QueryDataResponse.QueryData msgData : lstRawData) {
            prcrTest.addQueryData(msgData);
        }
        
        Assert.assertTrue("QueryDataCorrelator has no data.", prcrTest.sizeCorrelatedSet() > 0);
        
        // Perform available verification checks
        SortedSet<CorrelatedQueryData>   setPrcdData = prcrTest.getCorrelatedSet();
        
        ResultStatus    recOrder = QueryDataCorrelator.verifyOrdering(setPrcdData);
        if (recOrder.isFailure())
            Assert.fail(recOrder.message());
        
        ResultStatus    recSizes = QueryDataCorrelator.verifyColumnSizes(setPrcdData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        ResultStatus    recDomains = QueryDataCorrelator.verifyTimeDomains(setPrcdData);
        if (recDomains.isFailure())
            Assert.fail(recDomains.message());
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryDataCorrelator#addQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)}.
     */
    @Test
    public final void testInsertQueryDataWide() {
        List<QueryData>        lstRawData = LST_QUERY_DATA_WIDE;
        
        QueryDataCorrelator  prcrTest = new QueryDataCorrelator();
        
        for (QueryDataResponse.QueryData msgData : lstRawData) {
            prcrTest.addQueryData(msgData);
        }
        
        Assert.assertTrue("QueryDataCorrelator has no data.", prcrTest.sizeCorrelatedSet() > 0);

        // Extract data source names and print them out
        Set<String> setDataSrcs = prcrTest.extractDataSourceNames();
        
        System.out.println("Data Source Names: " + setDataSrcs);
        
        
        // Perform available verification checks
        SortedSet<CorrelatedQueryData>   setPrcdData = prcrTest.getCorrelatedSet();
        
        ResultStatus    recOrder = QueryDataCorrelator.verifyOrdering(setPrcdData);
        if (recOrder.isFailure())
            Assert.fail(recOrder.message());
        
        ResultStatus    recSizes = QueryDataCorrelator.verifyColumnSizes(setPrcdData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        ResultStatus    recDomains = QueryDataCorrelator.verifyTimeDomains(setPrcdData);
        if (recDomains.isFailure())
            Assert.fail(recDomains.message());
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryDataCorrelator#addQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)}.
     */
    @Test
    public final void testInsertQueryDataBig() {
        
        // First, get the test data from a test Query API
        List<QueryDataResponse>     lstMsgRsps;
        
        try {
            DpDataRequest       dpRqstBig = TestQueryResponses.QREC_BIG.createRequest();
            QueryDataRequest    msgRqstBig = dpRqstBig.buildQueryRequest();
            
            TestQueryService    apiQueryTest = TestQueryService.newService();
            
            lstMsgRsps = apiQueryTest.queryResponseStream(msgRqstBig);
        
            apiQueryTest.shutdown();
            
        } catch (DpGrpcException e) {
            Assert.fail("Unable to create test Query Service API: DpGrpcException thrown - " + e.getMessage());
            return;
            
        } catch (InterruptedException e) {
            Assert.fail("Shutdown operation interrupted: " + e.getMessage());
            return;
            
        }
        
        
        // Process the raw query responses with a new data correlator
        QueryDataCorrelator  prcrTest = new QueryDataCorrelator();
//        prcrTest.setConcurrency(false);
        
        int cntr = 0;
        for (QueryDataResponse msgRsp: lstMsgRsps) {
            try {
                prcrTest.addQueryResponse(msgRsp);
                
                cntr++;
                System.out.println("Interted message " + Integer.valueOf(cntr).toString());
                
            } catch (ExecutionException e) {
                Assert.fail("Processor reports query request was rejected: " + e.getMessage());
                return;
                
            } catch (CannotProceedException e) {
                Assert.fail("Processor reports query response error: " + e.getMessage());
                return;
                
            } catch (IllegalArgumentException e) {
                Assert.fail("Processor reports query data bucket did NOT contain sampling clock: " + e.getMessage());
                return;

            }
        }
        
        Assert.assertTrue("QueryDataCorrelator has no data.", prcrTest.sizeCorrelatedSet() > 0);

        // Extract data source names and print them out
        Set<String> setDataSrcs = prcrTest.extractDataSourceNames();
        
        System.out.println("Data Source Names: " + setDataSrcs);
        
        
        // Perform available verification checks
        SortedSet<CorrelatedQueryData>   setPrcdData = prcrTest.getCorrelatedSet();
        
        ResultStatus    recOrder = QueryDataCorrelator.verifyOrdering(setPrcdData);
        if (recOrder.isFailure())
            Assert.fail(recOrder.message());
        
        ResultStatus    recSizes = QueryDataCorrelator.verifyColumnSizes(setPrcdData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        ResultStatus    recDomains = QueryDataCorrelator.verifyTimeDomains(setPrcdData);
        if (recDomains.isFailure())
            Assert.fail(recDomains.message());
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryDataCorrelator#addQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData)}.
     */
//    @Test
    public final void testInsertQueryDataHuge() {
        
        // First, get the test data from a test Query API
        List<QueryDataResponse>     lstMsgRsps;
        
        try {
            DpDataRequest       dpRqstHuge = TestDpDataRequestGenerator.createRequest(1000, 50L);
            QueryDataRequest    msgRqstHuge = dpRqstHuge.buildQueryRequest();
            
            TestQueryService    apiQueryTest = TestQueryService.newService();
            
            lstMsgRsps = apiQueryTest.queryResponseStream(msgRqstHuge);
        
            apiQueryTest.shutdown();
            
        } catch (DpGrpcException e) {
            Assert.fail("Unable to create test Query Service API: DpGrpcException thrown - " + e.getMessage());
            return;
            
        } catch (InterruptedException e) {
            Assert.fail("Shutdown operation interrupted: " + e.getMessage());
            return;
            
        }
        
        
        // Process the raw query responses with a new data correlator
        QueryDataCorrelator  prcrTest = new QueryDataCorrelator();
//        prcrTest.setConcurrency(false);
        
        int cntr = 0;
        for (QueryDataResponse msgRsp: lstMsgRsps) {
            try {
                prcrTest.addQueryResponse(msgRsp);
                
                cntr++;
                System.out.println("Interted message " + Integer.valueOf(cntr).toString());
                
            } catch (ExecutionException e) {
                Assert.fail("Processor reports query request was rejected: " + e.getMessage());
                return;
                
            } catch (CannotProceedException e) {
                Assert.fail("Processor reports query response error: " + e.getMessage());
                return;
                
            } catch (IllegalArgumentException e) {
                Assert.fail("Processor reports query data bucket did NOT contain sampling clock: " + e.getMessage());
                return;

            }
        }
        
        Assert.assertTrue("QueryDataCorrelator has no data.", prcrTest.sizeCorrelatedSet() > 0);

        // Extract data source names and print them out
        Set<String> setDataSrcs = prcrTest.extractDataSourceNames();
        
        System.out.println("Data Source Names: " + setDataSrcs);
        
        
        // Perform available verification checks
        SortedSet<CorrelatedQueryData>   setPrcdData = prcrTest.getCorrelatedSet();
        
        ResultStatus    recOrder = QueryDataCorrelator.verifyOrdering(setPrcdData);
        if (recOrder.isFailure())
            Assert.fail(recOrder.message());
        
        ResultStatus    recSizes = QueryDataCorrelator.verifyColumnSizes(setPrcdData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        ResultStatus    recDomains = QueryDataCorrelator.verifyTimeDomains(setPrcdData);
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
