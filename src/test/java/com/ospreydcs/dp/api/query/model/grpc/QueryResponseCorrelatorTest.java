/*
 * Project: dp-api-common
 * File:	QueryResponseCorrelatorTest.java
 * Package: com.ospreydcs.dp.api.query.model.grpc
 * Type: 	QueryResponseCorrelatorTest
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
 * @since Feb 16, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.grpc;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.DpApiTestingConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.model.DpQueryException;
import com.ospreydcs.dp.api.query.test.TestQueryCompositeRecord;
import com.ospreydcs.dp.api.query.test.TestQueryRecord;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryResult.QueryData;

/**
 * JUnit test cases for class <code>{@link QueryResponseCorrelator}</code>.
 * 
 * @author Christopher K. Allen
 * @since Feb 16, 2024
 *
 */
public class QueryResponseCorrelatorTest {

    
    //
    // Class Constants
    //
    
    /** The configuration for the Query Service connection to the test archive */
    private static final DpGrpcConnectionConfig  CFG_CONN_TEST  = DpApiTestingConfig.getInstance().testQuery.connection;
    
    
    /** Data Platform API Query Service test request/response - (single bucket) 1 data source, 1 seconds */
    public static final TestQueryRecord REC_BUCKET = TestQueryResponses.QREC_BCKT;
    
    /** Data Platform API Query Service test request/response - 1 data source, 10 seconds */
    public static final TestQueryRecord REC_ONE = TestQueryResponses.QREC_1SRC;
    
    /** Data Platform API Query Service test request/response - 2 data sources, 2 seconds */
    public static final TestQueryRecord REC_TWO = TestQueryResponses.QREC_2SRC;
    
    /** Data Platform API Query Service test request/response - 5 data source, 60 seconds */
    public static final TestQueryRecord REC_LONG = TestQueryResponses.QREC_LONG;
    
    /** Data Platform API Query Service test request/response - 5 data source, 60 seconds */
    public static final TestQueryRecord REC_WIDE = TestQueryResponses.QREC_WIDE;
    
    /** Data Platform API Query Service test request/response - 100 data source, 60 seconds */
    public static final TestQueryRecord REC_BIG = TestQueryResponses.QREC_BIG;
    
    /** Data Platform API Query Service test request/response - 1,000 data source, 60 seconds */
    public static final TestQueryRecord REC_HUGE = new TestQueryRecord("DELETE_THIS.dat", 1000, 0, 50L, 0L);
    
    
    /** Data Platform API Query Service test composite request/response - 5 Components: 50 sources, 5 seconds (10 srcs/query) */
    public static final TestQueryCompositeRecord REC_CMP_HOR = TestQueryResponses.CQRECS_HOR;
    
    /** Data Platform API Query Service test composite request - 5 Components: 10 sources, 5 seconds (1 sec/query) */
    public static final TestQueryCompositeRecord REC_CMP_VER = TestQueryResponses.CQRECS_VER;
    
    /** Data Platform API Query Service test composite request - 10 Components: 50 sources, 5 seconds (10 srcs/query, 1 sec/query) */
    public static final TestQueryCompositeRecord REC_CMP_GRID = TestQueryResponses.CQRECS_GRID;
    
    
    
    //
    // Test Fixture Resources
    //
    
    /** The single connection to the Query Service used by all test cases */
    private static DpQueryConnection        connQuery;
    
    /** Query data corrTest used for comparisons - this class has been unit tested */
    private static QueryDataCorrelator      corrData;
    
    
    //
    // Test Case Resources
    //
    
    /** The QueryResponseCorrelator under test */
    private QueryResponseCorrelator     corrTest;
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        DpQueryConnectionFactory    facTest = DpQueryConnectionFactory.newFactory(CFG_CONN_TEST);
        
        // We connect to the test archive
        connQuery = facTest.connect();
        
        // Create the comparison correlater
        corrData = new QueryDataCorrelator();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        connQuery.shutdownSoft();
        
        TestQueryResponses.shutdown();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        corrData.reset();
        
        corrTest = new QueryResponseCorrelator(connQuery);
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#QueryResponseCorrelator(com.ospreydcs.dp.api.grpc.query.DpQueryConnection)}.
     */
    @Test
    public final void testQueryResponseCorrelator() {
        QueryResponseCorrelator correlator = new QueryResponseCorrelator(connQuery);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#setMultiStreaming(boolean)}.
     * <p>
     * Turns off multi-stream option for default gRPC streaming requests.
     */
    @Test
    public final void testSetMultiStreamingFalseBig() {
        TestQueryRecord     recTest = REC_BIG;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            corrTest.setMultiStreaming(false);  // method under test
            
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestStream(dpRequest);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#setCorrelationConcurrency(boolean)}.
     * <p>
     * Turns off concurrent processing.
     */
    @Test
    public final void testSetCorrelationConcurrencyFalseBig() {
        TestQueryRecord     recTest = REC_BIG;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            corrTest.setCorrelationConcurrency(false);  // method under test
            
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestStream(dpRequest);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#setCorrelateMidstream(boolean)}.
     * <p>
     * Turns off the stream/correlate concurrently option
     */
    @Test
    public final void testSetCorrelateMidstreamFalseBig() {
        TestQueryRecord     recTest = REC_BIG;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            corrTest.setCorrelateMidstream(false);  // method under test
            
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestStream(dpRequest);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#setCorrelateMidstream(boolean)}.
     * <p>
     * Turns off the stream/correlate concurrently option
     */
//    @Test
    public final void testSetCorrelateMidstreamFalseHuge() {
        TestQueryRecord     recTest = REC_HUGE;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        // Perform request and process response data, then compare
        try {
            corrTest.setCorrelateMidstream(false);  // method under test
            
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestStream(dpRequest);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#setCorrelationConcurrency(boolean)}.
     * <p>
     * Turns off multi-stream option for default gRPC streaming requests.
     * <br/>
     * Turns off correlation concurrent processing.
     * <br/>
     * Turns off the stream/correlate concurrently option
     */
    @Test
    public final void testSetEverthingFalseBig() {
        TestQueryRecord     recTest = REC_BIG;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            corrTest.setMultiStreaming(false);  // method under test
            corrTest.setCorrelationConcurrency(false);  // method under test
            corrTest.setCorrelateMidstream(false);  // method under test
            
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestStream(dpRequest);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestUnary(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequestUnaryBucket() {
        TestQueryRecord     recTest = REC_BUCKET;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestUnary(dpRequest);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: " + e.getMessage());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestUnary(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequestUnaryOne() {
        TestQueryRecord     recTest = REC_ONE;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestUnary(dpRequest);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: " + e.getMessage());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestUnary(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequestUnaryTwo() {
        TestQueryRecord     recTest = REC_TWO;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestUnary(dpRequest);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: " + e.getMessage());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestUnary(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequestUnaryLong() {
        TestQueryRecord     recTest = REC_LONG;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestUnary(dpRequest);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: " + e.getMessage());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestUnary(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequestUnaryWide() {
        TestQueryRecord     recTest = REC_WIDE;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        // Perform request and process response data - this should throw exception
        try {
            
            // This should fail because result set cannot be returned in one message
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestUnary(dpRequest);
            
            Assert.fail("Result set should be too large to fit in one message.");
            
        } catch (DpQueryException e) {
            System.out.println(e.getMessage());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequestStreamBucket() {
        TestQueryRecord     recTest = REC_BUCKET;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestStream(dpRequest);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequestStreamOne() {
        TestQueryRecord     recTest = REC_ONE;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestStream(dpRequest);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequestStreamTwo() {
        TestQueryRecord     recTest = REC_TWO;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestStream(dpRequest);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequestStreamLong() {
        TestQueryRecord     recTest = REC_LONG;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestStream(dpRequest);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequestStreamWide() {
        TestQueryRecord     recTest = REC_WIDE;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestStream(dpRequest);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequestStreamBig() {
        TestQueryRecord     recTest = REC_BIG;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestStream(dpRequest);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
//    @Test
    public final void testProcessRequestStreamHuge() {
        TestQueryRecord     recTest = REC_HUGE;
        
        DpDataRequest       dpRequest = recTest.createRequest();
        
        // Perform request and process response data, then compare
        try {
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestStream(dpRequest);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestMultiStream(java.util.List)}.
     */
    @Test
    public final void testProcessRequestMultiStreamHor() {
        TestQueryCompositeRecord     recTest = REC_CMP_HOR;
        
        List<DpDataRequest>       lstRequests = recTest.createCompositeRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestMultiStream(lstRequests);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestMultiStream(java.util.List)}.
     */
    @Test
    public final void testProcessRequestMultiStreamVer() {
        TestQueryCompositeRecord     recTest = REC_CMP_VER;
        
        List<DpDataRequest>       lstRequests = recTest.createCompositeRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestMultiStream(lstRequests);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#processRequestMultiStream(java.util.List)}.
     */
    @Test
    public final void testProcessRequestMultiStreamGrid() {
        TestQueryCompositeRecord     recTest = REC_CMP_GRID;
        
        List<DpDataRequest>       lstRequests = recTest.createCompositeRequest();
        
        SortedSet<CorrelatedQueryData>  setExpected = this.processRawData(recTest);
        
        // Perform request and process response data, then compare
        try {
            SortedSet<CorrelatedQueryData>  setActual = corrTest.processRequestMultiStream(lstRequests);
            
            Assert.assertEquals(setExpected, setActual);
            
        } catch (DpQueryException e) {
            Assert.fail("Request processing threw exception: message=" + e.getMessage() + ", cause=" + e.getCause());
        } 
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Processes (correlates) raw query of test data record into correlated data and return it.
     * </p>
     * <p>
     * If an exception is thrown during the query data recovery an assert FAIL is
     * invoked terminating the test case.
     * </p>
     * 
     * @param recTest   <code>TestQueryRecord</code> describing the test data
     * 
     * @return  ordered, correlated data set from given argument
     */
    private SortedSet<CorrelatedQueryData>  processRawData(TestQueryRecord recTest) {
        corrData.reset();
        
        try {
            List<QueryData>    lstRawData = recTest.recoverQueryData();
            
            lstRawData.forEach(msgData -> corrData.addQueryData(msgData));
            
            SortedSet<CorrelatedQueryData>   setPrcdData = corrData.getCorrelatedSet();
            
            return setPrcdData;
            
        } catch (ClassNotFoundException | IOException | DpGrpcException e) {
            Assert.fail("Exception thrown while recovering comparison data: type=" + e.getClass().getSimpleName() + ", message=" + e.getMessage());
            return null;
            
        }
    }
    
    /**
     * <p>
     * Processes (correlates) raw query of test data record into correlated data and return it.
     * </p>
     * <p>
     * If an exception is thrown during the query data recovery an assert FAIL is
     * invoked terminating the test case.
     * </p>
     * 
     * @param recTest   <code>TestQueryRecord</code> describing the test data
     * 
     * @return  ordered, correlated data set from given argument
     */
    private SortedSet<CorrelatedQueryData>  processRawData(TestQueryCompositeRecord recTest) {
        corrData.reset();
        
        try {
            List<QueryData>    lstRawData = recTest.recoverQueryData();
            
            lstRawData.forEach(msgData -> corrData.addQueryData(msgData));
            
            SortedSet<CorrelatedQueryData>   lstPrcdData = corrData.getCorrelatedSet();
            
            return lstPrcdData;
            
        } catch (ClassNotFoundException | IOException | DpGrpcException e) {
            Assert.fail("Exception thrown while recovering comparison data: type=" + e.getClass().getSimpleName() + ", message=" + e.getMessage());
            return null;
            
        }
    }
}
