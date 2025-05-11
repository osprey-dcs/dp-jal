/*
 * Project: dp-api-common
 * File:	TestQueryResponsesTest.java
 * Package: com.ospreydcs.dp.api.query.test
 * Type: 	TestQueryResponsesTest
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
 * @since Jan 23, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.DpApiUnitTestConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.test.TestQueryResponses.CompositeQueryType;
import com.ospreydcs.dp.api.query.test.TestQueryResponses.SingleQueryType;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

/**
 * <p>
 * JUnit test cases for the testing utility <code>{@link TestQueryResponses}</code>.
 * </p>
 * <p>
 * <h2>IMPORTANT:</h2>
 * This unit test is also used to create the persistent data for <code>TestQueryResponses</code>
 * with the test case <code>{@link #testStorePersistentData()}</code>.  This method should be
 * run only once, after establishing a Query Service connected to the Data Platform test archive
 * and reachable through the <code>{@link TestQueryService}</code> test API using the test
 * configuration parameters of <code>{@link DpApiUnitTestConfig}</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 23, 2024
 *
 */
public class TestQueryResponsesTest {

    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        // Make sure there is data
        TestQueryResponses.storePersistentData();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        
        // Release any gRPC resources that may have been used.
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
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryResponses#queryResults(com.ospreydcs.dp.api.query.test.TestQueryResponses.SingleQueryType)}.
     */
    @Test
    public final void testQueryResultsSingleQueryTypeOne() {
        List<QueryDataResponse> lstRsps = TestQueryResponses.queryResults(SingleQueryType.ONE_SOURCE);
        
        Assert.assertFalse("The QueryResponse list was empty." , lstRsps.isEmpty());
        Assert.assertFalse("The query has a rejection.", this.hasRejection(lstRsps));
        
        System.out.println("Size of the ONE_SOURCE single query response list = " + lstRsps.size());
        
        List<QueryDataResponse.QueryData.DataBucket>    lstBuckets = this.extractBuckets(lstRsps);
        
        System.out.println("Number of data buckets in the ONE_SOURCE query results set = " + lstBuckets.size());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryResponses#queryResults(com.ospreydcs.dp.api.query.test.TestQueryResponses.SingleQueryType)}.
     */
    @Test
    public final void testQueryResultsSingleQueryTypeTwo() {
        List<QueryDataResponse> lstRsps = TestQueryResponses.queryResults(SingleQueryType.TWO_SOURCE);
        
        Assert.assertFalse("The QueryResponse list was empty." , lstRsps.isEmpty());
        Assert.assertFalse("The query has a rejection.", this.hasRejection(lstRsps));
        
        System.out.println("Size of the ONE_SOURCE single query response list = " + lstRsps.size());
        
        List<QueryDataResponse.QueryData.DataBucket>    lstBuckets = this.extractBuckets(lstRsps);
        
        System.out.println("Number of data buckets in the ONE_SOURCE query results set = " + lstBuckets.size());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryResponses#queryResults(com.ospreydcs.dp.api.query.test.TestQueryResponses.SingleQueryType)}.
     */
    @Test
    public final void testQueryResultsSingleQueryTypeWide() {
        List<QueryDataResponse> lstRsps = TestQueryResponses.queryResults(SingleQueryType.WIDE);
        
        Assert.assertFalse("The QueryResponse list was empty." , lstRsps.isEmpty());
        Assert.assertFalse("The query has a rejection.", this.hasRejection(lstRsps));
        
        System.out.println("Size of the WIDE single query response list = " + lstRsps.size());
        
        List<QueryDataResponse.QueryData.DataBucket>    lstBuckets = this.extractBuckets(lstRsps);
        
        System.out.println("Number of data buckets in the WIDE query results set = " + lstBuckets.size());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryResponses#queryResults(com.ospreydcs.dp.api.query.test.TestQueryResponses.SingleQueryType)}.
     */
    @Test
    public final void testQueryResultsSingleQueryTypeLong() {
        List<QueryDataResponse> lstRsps = TestQueryResponses.queryResults(SingleQueryType.LONG);
        
        Assert.assertFalse("The QueryResponse list was empty." , lstRsps.isEmpty());
        Assert.assertFalse("The query has a rejection.", this.hasRejection(lstRsps));
        
        System.out.println("Size of the LONG single query response list = " + lstRsps.size());
        
        List<QueryDataResponse.QueryData.DataBucket>    lstBuckets = this.extractBuckets(lstRsps);
        
        System.out.println("Number of data buckets in the LONG query results set = " + lstBuckets.size());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryResponses#queryResults(com.ospreydcs.dp.api.query.test.TestQueryResponses.CompositeQueryType, int)}.
     */
    @Test
    public final void testQueryResultsCompositeQueryTypeIntHorizontal() {
        List<QueryDataResponse> lstRspsAll = new LinkedList<>();
        
        int cntQueries = TestQueryResponses.getSubQueryCount(CompositeQueryType.HORIZONTAL);
        
        for (int i = 0; i <cntQueries; i++) {
            List<QueryDataResponse> lstRsps = TestQueryResponses.queryResults(CompositeQueryType.HORIZONTAL, i);
            
            Assert.assertFalse("The QueryResponse list for sub-query " + i + " was empty." , lstRsps.isEmpty());
            Assert.assertFalse("Sub-query " + i + " has a rejection.", this.hasRejection(lstRsps));
            
            lstRspsAll.addAll(lstRsps);
        }
        
        System.out.println("Total size of the HORIZONTAL decompose query response list = " + lstRspsAll.size());
        
        List<QueryDataResponse.QueryData.DataBucket>    lstBuckets = this.extractBuckets(lstRspsAll);
        
        System.out.println("Number of data buckets in the HORIZONTAL decompose query results set = " + lstBuckets.size());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryResponses#queryResults(com.ospreydcs.dp.api.query.test.TestQueryResponses.CompositeQueryType, int)}.
     */
    @Test
    public final void testQueryResultsCompositeQueryTypeIntVertical() {
        List<QueryDataResponse> lstRspsAll = new LinkedList<>();
        
        int cntQueries = TestQueryResponses.getSubQueryCount(CompositeQueryType.VERTICAL);
        
        for (int i = 0; i <cntQueries; i++) {
            List<QueryDataResponse> lstRsps = TestQueryResponses.queryResults(CompositeQueryType.VERTICAL, i);
            
            Assert.assertFalse("The QueryResponse list for sub-query " + i + " was empty." , lstRsps.isEmpty());
            Assert.assertFalse("Sub-query " + i + " has a rejection.", this.hasRejection(lstRsps));
            
            lstRspsAll.addAll(lstRsps);
        }
        
        System.out.println("Total size of the VERTICAL decompose query response list = " + lstRspsAll.size());
        
        List<QueryDataResponse.QueryData.DataBucket>    lstBuckets = this.extractBuckets(lstRspsAll);
        
        System.out.println("Number of data buckets in the VERTICAL decompose query results set = " + lstBuckets.size());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryResponses#queryResults(com.ospreydcs.dp.api.query.test.TestQueryResponses.CompositeQueryType, int)}.
     */
    @Test
    public final void testQueryResultsCompositeQueryTypeIntGrid() {
        List<QueryDataResponse> lstRspsAll = new LinkedList<>();
        
        int cntQueries = TestQueryResponses.getSubQueryCount(CompositeQueryType.GRID);
        
        for (int i = 0; i <cntQueries; i++) {
            List<QueryDataResponse> lstRsps = TestQueryResponses.queryResults(CompositeQueryType.GRID, i);
            
            Assert.assertFalse("The QueryResponse list for sub-query " + i + " was empty." , lstRsps.isEmpty());
            Assert.assertFalse("Sub-query " + i + " has a rejection.", this.hasRejection(lstRsps));
            
            lstRspsAll.addAll(lstRsps);
        }
        
        System.out.println("Total size of the GRID decompose query response list = " + lstRspsAll.size());
        
        List<QueryDataResponse.QueryData.DataBucket>    lstBuckets = this.extractBuckets(lstRspsAll);
        
        System.out.println("Number of data buckets in the GRID decompose query results set = " + lstBuckets.size());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryResponses#shutdown()}.
     */
    @Test
    public final void testShutdown() {
        try {
            TestQueryResponses.QREC_1SRC.performQuery();
        
        } catch (DpGrpcException e) {
            Assert.fail("Attempt to query QREC_1SRC failed with DpGrpcException = " + e.getMessage());
            return;
        }
        
        boolean bolResult = TestQueryResponses.shutdown();
        
        Assert.assertTrue("TestQueryResponse class failed the shutdown operation", bolResult);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryResponses#storePersistentData()}.
     */
    @Test
    public final void testStorePersistentData() {
        
        try {
            boolean bolResult = TestQueryResponses.storePersistentData();
        
            Assert.assertTrue("Persistent storage attemp reported failure.", bolResult);
            
        } catch (FileNotFoundException e) {
            Assert.fail("Persistent storage attempt throw a FileNotFoundException = " + e.getMessage());
            
        } catch (DpGrpcException e) {
            Assert.fail("Persistent storage attempt throw a DpGrpcException = " + e.getMessage());
            
        } catch (IOException e) {
            Assert.fail("Persistent storage attempt throw an IOException = " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryResponsesEdeletePersistentData()}.
     */
    @Test
    public final void testDeletePersistentData() {
        
        // First populate the persistent storage
        testStorePersistentData();
        
        // Now try to delete it
        try {
            boolean bolResult = TestQueryResponses.deletePersistentData();
            
            Assert.assertTrue("Persistent storage deletion reported failure.", bolResult);
            
        } catch (SecurityException e) {
            Assert.fail("Persistent storage deletion attempt throw a SecurityException = " + e.getMessage());

        } catch (URISyntaxException e) {
            Assert.fail("Persistent storage deletion attempt throw a URISyntaxException = " + e.getMessage());
        
        } catch (IOException e) {
            Assert.fail("Persistent storage deletion attempt throw an IOException = " + e.getMessage());
        
        }
    }

    
    //
    // Support Methods
    //
    
    /**
     * Checks the given results set for a query rejection by the Query Service
     * 
     * @param lstRsps   the QueryResponse list to check for RejectDetails message
     * 
     * @return  <code>true</code> if the list contains a <code>RejectDetails</code> Protobuf message,
     *          <code>false</code> otherwise
     */
    private boolean hasRejection(List<QueryDataResponse> lstRsps) {
        boolean bolRejection = lstRsps.stream().map( r -> r.hasExceptionalResult()).anyMatch( b -> b == true);
        
        return bolRejection;
    }
    
    /**
     * Extracts the <code>BucketData</code> messages from the query results set.
     * 
     * @param lstRsps   the target query results set
     *  
     * @return  the list of BucketData messages in the given results set (one for each QueryResponse message)
     */
    private List<QueryDataResponse.QueryData>   extractData(List<QueryDataResponse> lstRsps) {
        List<QueryDataResponse.QueryData>   lstData = lstRsps
                .stream()
                .map( r -> r.getQueryData())
                .toList();
        
        return lstData;
    }
    
    /**
     * <p>
     * Extracts all the <code>DataBucket</code> messages from all the <code>BucketData</code> messages.
     * </p>
     * <p>
     * First extracts all <code>BucketData</code> messages from the given results set then collects all
     * <code>DataBucket</code> messages from each <code>BucketData</code> message into a final collection.
     * </p>
     * 
     * @param lstRsps   the target query results set
     * 
     * @return  a list of all DataBucket message in the given results set
     * 
     * @see #extractData(List)
     */
    private List<QueryDataResponse.QueryData.DataBucket> extractBuckets(List<QueryDataResponse> lstRsps) {
        List<QueryDataResponse.QueryData>   lstData = this.extractData(lstRsps);
        
        List<QueryDataResponse.QueryData.DataBucket> lstBuckets = lstData
                .stream()
                .collect(LinkedList::new, 
                        (lst, data) -> lst.addAll(data.getDataBucketsList()), 
                        (lst1, lst2) -> lst1.addAll(lst2)
                        );
        
        return lstBuckets;
    }
}
