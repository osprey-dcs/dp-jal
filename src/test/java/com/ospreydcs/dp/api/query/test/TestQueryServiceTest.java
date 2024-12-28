/*
 * Project: dp-api-common
 * File:	TestQueryServiceTest.java
 * Package: com.ospreydcs.dp.api.query.test
 * Type: 	TestQueryServiceTest
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
package com.ospreydcs.dp.api.query.test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.grpc.util.ProtoTime;
import com.ospreydcs.dp.api.model.DpGrpcStreamType;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.model.DpQueryStreamType;
import com.ospreydcs.dp.api.query.test.TestQueryResponses.SingleQueryType;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.DataTimestamps;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult;
import com.ospreydcs.dp.grpc.v1.common.SamplingClock;
import com.ospreydcs.dp.grpc.v1.common.Timestamp;
import com.ospreydcs.dp.grpc.v1.common.TimestampList;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData.DataBucket;

/**
 * JUnit test cases for class <code>{@link TestQueryService}</code>.
 * 
 * @author Christopher K. Allen
 * @since Feb 10, 2024
 *
 */
public class TestQueryServiceTest {

    //
    // Test Types
    //
    
    /** 
     * Record containing a data source and the range of time which it is active.
     * 
     * @param   pvName  name of the data source
     * @param   range   time interval over which data source was active
     *
     */
    public static record PvActivityRange(String pvName, TimeInterval range) {
        
        /**
         * @param strName       name of the data source
         * @param domActive     time interval over which it is active
         * 
         * @return  new <code>PvActivityRange</code> instance initialized with arguments
         */
        public static PvActivityRange   from(String strName, TimeInterval domActive) {
            return new PvActivityRange(strName, domActive);
        }
    };
    
    //
    // Test Constants
    //

    /** Query Service request whose result should be a single data bucket */
    public static QueryDataRequest  MSG_REQUEST_BUCKET = TestQueryResponses.requestMessage(SingleQueryType.BUCKET);
    
    /** Query Service request whose result should be a single data source time series */
    public static QueryDataRequest  MSG_REQUEST_ONE = TestQueryResponses.requestMessage(SingleQueryType.ONE_SOURCE);

    /** Query Service request whose result should be two data source time series */
    public static QueryDataRequest  MSG_REQUEST_TWO = TestQueryResponses.requestMessage(SingleQueryType.TWO_SOURCE);
    
    /** Query Service request whose result should be two data source time series */
    public static QueryDataRequest  MSG_REQUEST_WIDE = TestQueryResponses.requestMessage(SingleQueryType.WIDE);
    
    /** Query Service request whose result should be two data source time series */
    public static QueryDataRequest  MSG_REQUEST_LONG = TestQueryResponses.requestMessage(SingleQueryType.LONG);
    

    
    //
    // Test Resources
    //
    
    /** The common <code>TestQueryService</code> under test */
    private static  TestQueryService    apiQueryService;
    
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestQueryServiceTest.apiQueryService = TestQueryService.newService();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestQueryServiceTest.apiQueryService.shutdown();
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
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryService#TestQueryService()}.
     */
    @Test
    public final void testTestQueryService() {
        try {
            TestQueryService apiTest = TestQueryService.newService();
            
            apiTest.shutdown();
            
        } catch (DpGrpcException e) {
            Assert.fail("TestQueryService creation threw DpGrpcException: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Shutdown threw InterruptedException: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryService#queryResponseSingle(com.ospreydcs.dp.grpc.v1.query.QueryRequest)}.
     */
    @Test
    public final void testQueryResponseSingleBucket() {
        QueryDataRequest    msgRequest = MSG_REQUEST_BUCKET;
        
        // Inspect the request
        List<String>    lstSrcs = msgRequest.getQuerySpec().getPvNamesList();
        Timestamp       msgTms1 = msgRequest.getQuerySpec().getBeginTime();
        Timestamp       msgTms2 = msgRequest.getQuerySpec().getEndTime();
        
        if (lstSrcs.size() != 1) 
            Assert.fail("Query request did not have 1 data source: " + lstSrcs);

        if (ProtoTime.compare(msgTms1, msgTms2) >= 0)
            Assert.fail("Query request had bad time range.");
        
        // Perform the query
        QueryDataResponse   msgResponse = apiQueryService.queryResponseSingle(msgRequest);
        
        // Check the response for errors
        ResultStatus    recValid = this.checkErrors(msgResponse);
        if (recValid.isFailure()) {
            Assert.fail(recValid.message());
        }

        // Check the response against the query
        ResultStatus    recSources = this.checkSourceNames(msgRequest, List.of(msgResponse));
        if (recSources.isFailure())
            Assert.fail(recSources.message());
        
        ResultStatus    recActivity = this.checkActivityDomains(msgRequest, List.of(msgResponse));;
        if (recActivity.isFailure())
            Assert.fail(recActivity.message());
        
        // Check the query data
        QueryData   msgData = msgResponse.getQueryData();
        
        ResultStatus    recUnique = this.verifySourceUniqueness(msgData);
        if (recUnique.isFailure())
            Assert.fail(recUnique.message());
        
        ResultStatus    recSizes = this.verifySourceSizes(msgData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        // Inspect the query data
        List<DataBucket>    lstBuckets = msgData.getDataBucketsList();
        
        Assert.assertTrue("Results set should contain ONE data bucket", lstBuckets.size() == 1);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryService#queryResponseSingle(com.ospreydcs.dp.grpc.v1.query.QueryRequest)}.
     */
    @Test
    public final void testQueryResponseSingleOne() {
        QueryDataRequest    msgRequest = MSG_REQUEST_ONE;
        
        // Inspect the request
        List<String>    lstSrcs = msgRequest.getQuerySpec().getPvNamesList();
        Timestamp       msgTms1 = msgRequest.getQuerySpec().getBeginTime();
        Timestamp       msgTms2 = msgRequest.getQuerySpec().getEndTime();
        
        if (lstSrcs.size() != 1) 
            Assert.fail("Query request did not have 1 data source: " + lstSrcs);

        if (ProtoTime.compare(msgTms1, msgTms2) >= 0)
            Assert.fail("Query request had bad time range.");
        
        // Perform the query
        QueryDataResponse   msgResponse = apiQueryService.queryResponseSingle(msgRequest);
        
        // Check the response for errors
        ResultStatus    recValid = this.checkErrors(msgResponse);
        if (recValid.isFailure()) {
            Assert.fail(recValid.message());
        }

        // Check the response against the query
        ResultStatus    recSources = this.checkSourceNames(msgRequest, List.of(msgResponse));
        if (recSources.isFailure())
            Assert.fail(recSources.message());
        
        ResultStatus    recActivity = this.checkActivityDomains(msgRequest, List.of(msgResponse));;
        if (recActivity.isFailure())
            Assert.fail(recActivity.message());
        
        // Verify the query data
        QueryData   msgData = msgResponse.getQueryData();
        
//        ResultStatus    recUnique = this.verifySourceUniqueness(msgData);
//        if (recUnique.isFailure())
//            Assert.fail(recUnique.message());
        
        ResultStatus    recSizes = this.verifySourceSizes(msgData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        // Inspect the query data
        List<DataBucket>    lstBuckets = msgData.getDataBucketsList();
        
        Assert.assertTrue("Results set should contain more than ONE data bucket", lstBuckets.size() > 1);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryService#queryResponseSingle(com.ospreydcs.dp.grpc.v1.query.QueryRequest)}.
     */
    @Test
    public final void testQueryResponseSingleOneExplicit() {
        // Request parameters
        final String  strSrcNm = "dpTest_1";
        final Instant insBegin = Instant.ofEpochSecond(1698767462);
        final Instant insEnd = Instant.ofEpochSecond(1698767472);
        final DpGrpcStreamType enmType = DpGrpcStreamType.BACKWARD;
        
        // Create request and configure
        DpDataRequest   rqst = DpDataRequest.newRequest();
        
        rqst.selectSource(strSrcNm);
        rqst.rangeAfter(insBegin);
        rqst.rangeBefore(insEnd);
        rqst.setStreamType(enmType);

        QueryDataRequest    msgCompare = MSG_REQUEST_ONE;
        QueryDataRequest    msgRequest = rqst.buildQueryRequest();
        
        // Inspect the request
        List<String>    lstSrcs = msgRequest.getQuerySpec().getPvNamesList();
        Timestamp       msgTms1 = msgRequest.getQuerySpec().getBeginTime();
        Timestamp       msgTms2 = msgRequest.getQuerySpec().getEndTime();
        
        if (lstSrcs.size() != 1) 
            Assert.fail("Query request did not have 1 data source: " + lstSrcs);

        if (ProtoTime.compare(msgTms1, msgTms2) >= 0)
            Assert.fail("Query request had bad time range.");
        
        // Perform the query
        QueryDataResponse   msgResponse = apiQueryService.queryResponseSingle(msgRequest);
        
        // Check the response for errors
        ResultStatus    recValid = this.checkErrors(msgResponse);
        if (recValid.isFailure()) {
            Assert.fail(recValid.message());
        }

        // Check the response against the request
        ResultStatus    recSources = this.checkSourceNames(msgRequest, List.of(msgResponse));
        if (recSources.isFailure())
            Assert.fail(recSources.message());
        
        ResultStatus    recActivity = this.checkActivityDomains(msgRequest, List.of(msgResponse));
        if (recActivity.isFailure())
            Assert.fail(recActivity.message());
        
        // Check the query data
        QueryData   msgData = msgResponse.getQueryData();
        
//        ResultStatus    recUnique = this.verifySourceUniqueness(msgData);
//        if (recUnique.isFailure())
//            Assert.fail(recUnique.message());
        
        ResultStatus    recSizes = this.verifySourceSizes(msgData);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        // Inspect the query data
        List<DataBucket>    lstBuckets = msgData.getDataBucketsList();
        
        Assert.assertTrue("Results set should contain more than ONE data bucket", lstBuckets.size() > 1);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryService#queryResponseSingle(com.ospreydcs.dp.grpc.v1.query.QueryRequest)}.
     */
    @Test
    public final void testQueryResponseSingleTwo() {
        QueryDataRequest    msgRequest = MSG_REQUEST_TWO;
        
        // Perform the query
        QueryDataResponse   msgResponse = apiQueryService.queryResponseSingle(msgRequest);
        
        // Check the response
        this.checkResults(JavaRuntime.getQualifiedMethodNameSimple(), msgRequest, List.of(msgResponse));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryService#queryResponseSingle(com.ospreydcs.dp.grpc.v1.query.QueryRequest)}.
     */
    @Test
    public final void testQueryResponseSingleLong() {
        QueryDataRequest    msgRequest = MSG_REQUEST_LONG;
        
        // Perform the query
        QueryDataResponse   msgResponse = apiQueryService.queryResponseSingle(msgRequest);
        
        // Check the response
        this.checkResults(JavaRuntime.getQualifiedMethodNameSimple(), msgRequest, List.of(msgResponse));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryService#queryResponseStream(com.ospreydcs.dp.grpc.v1.query.QueryRequest)}.
     */
    @Test
    public final void testQueryResponseStreamOne() {
        QueryDataRequest    msgRequest = MSG_REQUEST_ONE;
        
        // Inspect the request
        List<String>    lstSrcs = msgRequest.getQuerySpec().getPvNamesList();
        Timestamp       msgTms1 = msgRequest.getQuerySpec().getBeginTime();
        Timestamp       msgTms2 = msgRequest.getQuerySpec().getEndTime();
        
        if (lstSrcs.size() != 1) 
            Assert.fail("Query request did not have 1 data source: " + lstSrcs);

        if (ProtoTime.compare(msgTms1, msgTms2) >= 0)
            Assert.fail("Query request had bad time range.");
        
        // Perform the query
        List<QueryDataResponse>   lstResultsSet = apiQueryService.queryResponseStream(msgRequest);
        
        // Check the response for errors
        ResultStatus    recValid = this.checkErrors(lstResultsSet);
        if (recValid.isFailure()) {
            Assert.fail(recValid.message());
        }

        // Check the response for all data source representation
        ResultStatus    recSources = this.checkSourceNames(msgRequest, lstResultsSet);
        if (recSources.isFailure())
            Assert.fail(recSources.message());
        
        ResultStatus    recActivity = this.checkActivityDomains(msgRequest, lstResultsSet);;
        if (recActivity.isFailure())
            Assert.fail(recActivity.message());
        
        // Check the query data
//        ResultStatus    recUnique = this.verifySourceUniqueness(lstResultsSet);
//        if (recUnique.isFailure())
//            Assert.fail(recUnique.message());
        
        ResultStatus    recSizes = this.verifySourceSizes(lstResultsSet);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        // Inspect the query data
        this.printActivityRanges(JavaRuntime.getQualifiedMethodNameSimple(), lstResultsSet);
        
        List<DataBucket>    lstBuckets = lstResultsSet
                .stream()
                .<QueryData>map(msgRsp -> msgRsp.getQueryData())
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .toList();
        
        Assert.assertTrue("Results set should contain more than ONE data bucket", lstBuckets.size() > 1);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryService#queryResponseStream(com.ospreydcs.dp.grpc.v1.query.QueryRequest)}.
     */
    @Test
    public final void testQueryResponseStreamTwo() {
        QueryDataRequest    msgRequest = MSG_REQUEST_TWO;
        
        // Perform the query
        List<QueryDataResponse>   lstResultsSet = apiQueryService.queryResponseStream(msgRequest);
        
        // Check the response
        this.checkResults(JavaRuntime.getQualifiedMethodNameSimple(), msgRequest, lstResultsSet);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryService#queryResponseStream(com.ospreydcs.dp.grpc.v1.query.QueryRequest)}.
     */
    @Test
    public final void testQueryResponseStreamLong() {
        QueryDataRequest    msgRequest = MSG_REQUEST_LONG;
        
        // Perform the query
        List<QueryDataResponse>   lstResultsSet = apiQueryService.queryResponseStream(msgRequest);
        
        // Check the response
        this.checkResults(JavaRuntime.getQualifiedMethodNameSimple(), msgRequest, lstResultsSet);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryService#queryResponseCursor(com.ospreydcs.dp.grpc.v1.query.QueryRequest)}.
     */
    @Test
    public final void testQueryResponseCursorOne() {
        QueryDataRequest    msgRequest = MSG_REQUEST_ONE;
        
        // Inspect the request
        List<String>    lstSrcs = msgRequest.getQuerySpec().getPvNamesList();
        Timestamp       msgTms1 = msgRequest.getQuerySpec().getBeginTime();
        Timestamp       msgTms2 = msgRequest.getQuerySpec().getEndTime();
        
        if (lstSrcs.size() != 1) 
            Assert.fail("Query request did not have 1 data source: " + lstSrcs);

        if (ProtoTime.compare(msgTms1, msgTms2) >= 0)
            Assert.fail("Query request had bad time range.");
        
        // Perform the query
        List<QueryDataResponse> lstResultsSet;
        try {
            lstResultsSet = apiQueryService.queryResponseCursor(msgRequest);
            
        } catch (CompletionException | InterruptedException e) {
            Assert.fail("queryResponseCursor threw exception: type=" + e.getClass().getSimpleName() + ", message=" + e.getMessage());
            return;
        }
        
        // Check the response for errors
        ResultStatus    recValid = this.checkErrors(lstResultsSet);
        if (recValid.isFailure()) {
            Assert.fail(recValid.message());
        }

        // Check the response against the query
        ResultStatus    recSources = this.checkSourceNames(msgRequest, lstResultsSet);
        if (recSources.isFailure())
            Assert.fail(recSources.message());
        
        ResultStatus    recActivity = this.checkActivityDomains(msgRequest, lstResultsSet);
        if (recActivity.isFailure())
            Assert.fail(recActivity.message());
        
        // Check the query data
//        ResultStatus    recUnique = this.verifySourceUniqueness(lstResultsSet);
//        if (recUnique.isFailure())
//            Assert.fail(recUnique.message());
        
        ResultStatus    recSizes = this.verifySourceSizes(lstResultsSet);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        // Inspect the query data
        this.printActivityRanges(JavaRuntime.getQualifiedMethodNameSimple(), lstResultsSet);
        
        List<DataBucket>    lstBuckets = lstResultsSet
                .stream()
                .<QueryData>map(msgRsp -> msgRsp.getQueryData())
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .toList();
        
        Assert.assertTrue("Results set should contain more than ONE data bucket", lstBuckets.size() > 1);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryService#queryResponseCursor(com.ospreydcs.dp.grpc.v1.query.QueryRequest)}.
     */
    @Test
    public final void testQueryResponseCursorTwo() {
        QueryDataRequest    msgRequest = MSG_REQUEST_TWO;
        
        // Perform the query
        List<QueryDataResponse> lstResultsSet;
        try {
            lstResultsSet = apiQueryService.queryResponseCursor(msgRequest);
            
        } catch (CompletionException | InterruptedException e) {
            Assert.fail("queryResponseCursor threw exception: type=" + e.getClass().getSimpleName() + ", message=" + e.getMessage());
            return;
        }
        
        // Check the response
        this.checkResults(JavaRuntime.getQualifiedMethodNameSimple(), msgRequest, lstResultsSet);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryService#queryResponseCursor(com.ospreydcs.dp.grpc.v1.query.QueryRequest)}.
     */
    @Test
    public final void testQueryResponseCursorLong() {
        QueryDataRequest    msgRequest = MSG_REQUEST_LONG;
        
        // Perform the query
        List<QueryDataResponse> lstResultsSet;
        try {
            lstResultsSet = apiQueryService.queryResponseCursor(msgRequest);
            
        } catch (CompletionException | InterruptedException e) {
            Assert.fail("queryResponseCursor threw exception: type=" + e.getClass().getSimpleName() + ", message=" + e.getMessage());
            return;
        }
        
        // Check the response
        this.checkResults(JavaRuntime.getQualifiedMethodNameSimple(), msgRequest, lstResultsSet);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryService#queryResponseCursor(com.ospreydcs.dp.grpc.v1.query.QueryRequest)}.
     */
    @Test
    public final void testQueryResponseCursorWide() {
        QueryDataRequest    msgRequest = MSG_REQUEST_WIDE;
        
        // Perform the query
        List<QueryDataResponse> lstResultsSet;
        try {
            lstResultsSet = apiQueryService.queryResponseCursor(msgRequest);
            
        } catch (CompletionException | InterruptedException e) {
            Assert.fail("queryResponseCursor threw exception: type=" + e.getClass().getSimpleName() + ", message=" + e.getMessage());
            return;
        }
        
        // Check the response
        this.checkResults(JavaRuntime.getQualifiedMethodNameSimple(), msgRequest, lstResultsSet);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.test.TestQueryService#shutdown()}.
     */
    @Test
    public final void testShutdown() {
        try {
            TestQueryService    apiTest = TestQueryService.newService();
            
            apiTest.shutdown();
            
        } catch (DpGrpcException e) {
            Assert.fail("Threw DpGrpcException: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Shutdown threw InterruptedException: " + e.getMessage());
            
        }
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Performs all the checks and verification possible on the arguments.
     * </p>
     * 
     * @param strMsg        header or message passed to any standard output
     * @param msgRequest    the data request message
     * @param lstResultsSet the Query Service results set from the given data request 
     */
    private void    checkResults(String strMsg, QueryDataRequest msgRequest, List<QueryDataResponse> lstResultsSet) {
        
        // Inspect the request
        List<String>    lstSrcs = msgRequest.getQuerySpec().getPvNamesList();
        Timestamp       msgTms1 = msgRequest.getQuerySpec().getBeginTime();
        Timestamp       msgTms2 = msgRequest.getQuerySpec().getEndTime();
        
        if (ProtoTime.compare(msgTms1, msgTms2) >= 0)
            Assert.fail("Query request had bad time range.");
        
        // Check the response for errors
        ResultStatus    recValid = this.checkErrors(lstResultsSet);
        if (recValid.isFailure()) {
            Assert.fail(recValid.message());
        }

        // Check the response against the query
        ResultStatus    recSources = this.checkSourceNames(msgRequest, lstResultsSet);
        if (recSources.isFailure())
            Assert.fail(recSources.message());
        
        ResultStatus    recActivity = this.checkActivityDomains(msgRequest, lstResultsSet);
        if (recActivity.isFailure())
            Assert.fail(recActivity.message());
        
        ResultStatus    recSizes = this.verifySourceSizes(lstResultsSet);
        if (recSizes.isFailure())
            Assert.fail(recSizes.message());
        
        // Inspect the query data
        this.printActivityRanges(strMsg, lstResultsSet);
        
        List<DataBucket>    lstBuckets = lstResultsSet
                .stream()
                .<QueryData>map(msgRsp -> msgRsp.getQueryData())
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .toList();
        
        Assert.assertTrue("Results set should contain more than ONE data bucket", lstBuckets.size() > 1);
    }
    
    /**
     * <p>
     * Checks the argument for response errors and reports them if present.
     * </p>
     * <p>
     * Specifically, the given <code>QueryResponse</code> is checked for a request rejection
     * and then a response error as reported by the Query Service.
     * </p>
     *  
     * @param msgResponse   <code>QueryResponse</code> message under inspection
     * 
     * @return  <code>{@link ResultStatus#SUCCESS</code> if no errors present,
     *          a failure record with message description if errors detected
     */
    private ResultStatus    checkErrors(QueryDataResponse msgResponse) {

        // Check for query rejection or response error
        if (msgResponse.hasExceptionalResult()) {
            ExceptionalResult        msgReject = msgResponse.getExceptionalResult();
            ExceptionalResult.ExceptionalResultStatus enmCause = msgReject.getExceptionalResultStatus();
            String                  strMsg = msgReject.getMessage();
            
            return ResultStatus.newFailure("Query was rejected or response error: cause=" + enmCause + ", message=" + strMsg);
        }
        
//        // Check for response error
//        if (msgResponse.getQueryResult().hasQueryStatus()) {
//            QueryStatus     msgStatus = msgResponse.getQueryResult().getQueryStatus();
//            QueryStatusType enmType = msgStatus.getQueryStatusType();
//            String          strMsg = msgStatus.getStatusMessage();
//
//            return ResultStatus.newFailure("Query contains response error: type="+ enmType + ", message=" + strMsg);
//        }
        
        return ResultStatus.SUCCESS;
    }
    
    /**
     * <p>
     * Checks the argument for response errors and reports them if present.
     * </p>
     * <p>
     * The argument is treated as the results set from a data request. 
     * Each <code>QueryResponse</code> is checked for a request rejection
     * and then a response error as reported by the Query Service.
     * Error checking of messages is deferred to <code>{@link #checkErrors(QueryResponse)}</code>.
     * </p>
     * 
     * @param lstResultsSet list of <code>QueryRespoonse</code> messages representing a data query results set
     * 
     * @return  <code>{@link ResultStatus#SUCCESS</code> if no errors present,
     *          a failure record with message description if errors detected
     *          
     * @see #checkErrors(QueryResponse)
     */
    private ResultStatus checkErrors(List<QueryDataResponse> lstResultsSet) {
    
        // Check each QueryResponse message and record results
        List<ResultStatus> lstFails  = lstResultsSet
                .stream()
                .<ResultStatus>map(msgRsp -> this.checkErrors(msgRsp))
                .filter(ResultStatus::isFailure)
                .toList();
        
        if (!lstFails.isEmpty())
            return ResultStatus.newFailure("Results set contains errors: " + lstFails);
        
        return ResultStatus.SUCCESS;
    }
    
    /**
     * <p>
     * Checks (verifies) that data source names within the given request are represented in the given results set.
     * </p>
     * <p>
     * First extracts all the data column names from the <code>QueryRequest</code>.
     * Extracts all the data columns from the given list of <code>QueryResponse</code> messages
     * representing the results set of the performed data query.
     * The results set is checked that all source names are present.
     * </p>
     * 
     * @param msgRequest       request message containing data source names 
     * @param lstResultsSet    results set of a Query service data request
     * 
     * @return  <code>{@link ResultStatus#SUCCESS}</code> if all sources are present,
     *          a failure record with description if any are missing
     */
    private ResultStatus    checkSourceNames(QueryDataRequest msgRequest, List<QueryDataResponse> lstResultsSet) {
     
        // Extract all the data source names from the query request message
        List<String>        lstSrcNms = msgRequest.getQuerySpec().getPvNamesList();
        
        // Extract all the data columns from the QueryResponse list
        List<DataColumn>    lstMsgCols = lstResultsSet
                .stream()
                .<QueryData>map(msgRsp -> msgRsp.getQueryData())
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .<DataColumn>map(msgBck -> msgBck.getDataColumn())
                .toList();
        
        // Get a list of column names
        List<String>        lstColNms = lstMsgCols.stream().map(DataColumn::getName).toList();
        
        // Check for data source name occurrence one by one
        for (String strSrcNm : lstSrcNms) {
            boolean bolExists = lstColNms.contains(strSrcNm);
            if (!bolExists)
                return ResultStatus.newFailure("Data source name NOT represented: " + strSrcNm);
        }
               
        return ResultStatus.SUCCESS;
    }
    
    /**
     * <p>
     * Checks all PV time ranges of the given results set against the request time range.
     * </p>
     * <p>
     * Extracts the time range specified in the data request message argument.   
     * Then extracts the time ranges where each data source within the results set is active 
     * using method <code>{@link #extractActivityRanges(List)}</code>.
     * Compares the time domain of each active data source against that specified within
     * the original data request.  If any time range is different, the method returns
     * a FAILURE result with a message describing all offending data source names.
     * </p>
     *    
     * @param msgRequest    Query Service data request producing the results set
     * @param lstResultsSet Query Service results set of the given data query
     * 
     * @return  <code>{@link ResultStatus#SUCCESS}</code> if all data source have activity intervals equal to query time domain,
     *          otherwise a FAILURE record with a list of all offending data sources
     * 
     * @see #extractActivityRanges(List)
     */
    private ResultStatus    checkActivityDomains(QueryDataRequest msgRequest, List<QueryDataResponse> lstResultsSet) {
        
        // Extract query time range and create domain interval
        Timestamp   tmsBegin = msgRequest.getQuerySpec().getBeginTime();
        Timestamp   tmsEnd = msgRequest.getQuerySpec().getEndTime();
        Instant     insBegin = ProtoMsg.toInstant(tmsBegin);
        Instant     insEnd = ProtoMsg.toInstant(tmsEnd);
        
        TimeInterval    domRequest = TimeInterval.from(insBegin, insEnd);
        
        // Extract the results set PV activity time ranges
        List<PvActivityRange>  lstPvDoms = this.extractActivityRanges(lstResultsSet);
        
        // Check all PV activity ranges against the request query range
        List<String>    lstBadPvs = lstPvDoms
                .stream()
                .filter(rec -> !rec.range.equals(domRequest))
                .map(rec -> rec.pvName)
                .toList();
        
        // Check for bad PV activity ranges
        if (!lstBadPvs.isEmpty())
            return ResultStatus.newFailure("Results set contains incomplete time series for the following: " + lstBadPvs);
        
        return ResultStatus.SUCCESS;
                    
    }
    
    /**
     * <p>
     * Verifies that the given data has unique data source names.
     * </p>
     * <p>
     * This method verifies that the current set of data in the argument
     * is consistent and ready for further processing.
     * </p>
     * <p>
     * The following conditions are checked:
     * <ul>
     * <li>Each data source has only one contribution to the data set.</li>
     * <li>Each data source has at least one contribution to the data set.</li>
     * </ul>
     * If the given data set fails the verification check, the cause of the failure
     * is included in the result.  Otherwise no cause message is provided.
     * </p> 
     * 
     * @param   msgData the <code>BucketData</code> under inspection
     * 
     * @return  result of the verification check, containing the cause if failed
     */
    private ResultStatus verifySourceUniqueness(QueryData msgData) {
        
        // Extract the data columns and create a mutable list of data source names
        List<DataColumn>    lstCols = msgData.getDataBucketsList().stream().map(DataBucket::getDataColumn).toList();
        List<String>        lstSrcNms = lstCols.stream().map(DataColumn::getName).toList();
        
        List<String>        lstSrcNmsMutable = new ArrayList<>(lstSrcNms);
        
        // Inspect the list, element by element
        for (String strSrcNm : lstSrcNms) {
            
            // Remove the first occurrence of target source name from the mutable list
            boolean bolRemoved = lstSrcNmsMutable.remove(strSrcNm);
            if (!bolRemoved)
                return ResultStatus. newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " Algorithm remove FAILURE for source " + strSrcNm);

            // Check if there are any other source name list entries
            boolean bolMatch = lstSrcNmsMutable.stream().anyMatch(s -> s.equals(strSrcNm));
            if (bolMatch)
                return ResultStatus.newFailure("Data source name NOT UNIQUE: " + strSrcNm);
            
            // Return the target name to mutable list and continue
            boolean bolReplaced = lstSrcNmsMutable.add(strSrcNm);
            if (!bolReplaced)
                return ResultStatus. newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " Algorithm replace FAILURE for source " + strSrcNm);
                
        }
        
        return ResultStatus.SUCCESS;
    }
    
    /**
     * <p>
     * Verifies that the given results set has unique data source names within pages.
     * </p>
     * <p>
     * This method verifies that the current set of data in the argument
     * is consistent and ready for further processing.
     * Extracts all query data from results set then checks each page individually
     * using <code>{@link #verifySourceUniqueness(BucketData)}</code>
     * </p>
     * <p>
     * The following conditions are checked:
     * <ul>
     * <li>Extracts a data set.</li>
     * <li>Each data source has only one contribution to the data set.</li>
     * <li>Each data source has at least one contribution to the data set.</li>
     * </ul>
     * If the given data set fails the verification check, the cause of the failure
     * is included in the result.  Otherwise no cause message is provided.
     * </p> 
     * 
     * @param lstResultsSet list of <code>QueryRespoonse</code> messages representing a data query results set
     * 
     * @return  <code>{@link ResultStatus#SUCCESS</code> if each data set has unique PV names,
     *          a failure record with message description if errors detected
     * 
     * @see #verifySourceUniqueness(BucketData)
     */
    private ResultStatus verifySourceUniqueness(List<QueryDataResponse> lstResultsSet) {
        
        // Extract results set data, check each data set, record results
        List<ResultStatus>    lstFails = lstResultsSet
                .stream()
                .<QueryData>map(msgRsp -> msgRsp.getQueryData())
                .<ResultStatus>map(msgData -> this.verifySourceUniqueness(msgData))
                .filter(ResultStatus::isFailure)
                .toList();

        if (!lstFails.isEmpty())
            return ResultStatus.newFailure("Results set contained non-unique PVs: " + lstFails);
        
        return ResultStatus.SUCCESS;
    }
    
    /**
     * <p>
     * Verifies that the given set of data has source data sets all of same size.
     * </p>
     * <p>
     * Note that the data sources should all have data sets of the same size.  Further, the
     * size should equal the number of samples specified in the sampling interval message.  
     * This method verifies that the given set of data is consistent with the above and ready 
     * for further processing.
     * </p>
     * <p>
     * The following conditions are checked:
     * <ul>
     * <li>Each data source has size equal to the number data samples in sampling message.</li>
     * </ul>
     * If the given data set fails the verification check, the cause of the failure
     * is included in the result.  Otherwise no cause message is provided.
     * </p> 
     * 
     * @param   msgData     data set under inspection
     * 
     * @return  result of the verification check, containing the cause if failed
     */
    private ResultStatus verifySourceSizes(QueryData msgData) {
        
        // This only works if there is at least one DataBucket
        if (msgData.getDataBucketsList().isEmpty())
            return ResultStatus.newFailure("The BucketData message contained no data.");
        
        // Get the number of samples for the first bucket
        int             cntSamples;
        DataTimestamps  msgDataTms = msgData.getDataBuckets(0).getDataTimestamps();
        
        if (msgDataTms.hasSamplingClock())
            cntSamples = msgDataTms.getSamplingClock().getCount();
        else
            cntSamples = msgDataTms.getTimestampList().getTimestampsCount();
        
        // Check all timestamps counts 
        int     indBucket = 0;
        for (DataBucket msgBucket : msgData.getDataBucketsList()) {
            DataTimestamps  msgTms = msgBucket.getDataTimestamps();
            
            if (msgTms.hasSamplingClock()) {
                if (msgTms.getSamplingClock().getCount() != cntSamples)
                    return ResultStatus.newFailure("Sampling clock count for bucket " + indBucket + " not equal to " + cntSamples);
            
            } else {
                if (msgTms.getTimestampList().getTimestampsCount() != cntSamples) 
                    return ResultStatus.newFailure("TimestampList count for bucket " + indBucket + " not equal to " + cntSamples);
            }
                    
            indBucket++;
        }
        
        
        // Each source should provide the same number of data samples
//        int cntSamples = msgData.getDataBuckets(0).getSamplingInterval().getNumSamples();
        
        // Extract the data columns 
        List<DataColumn>    lstMsgCols = msgData.getDataBucketsList().stream().map(DataBucket::getDataColumn).toList();
        
        // Get list of all data columns with different size
        List<DataColumn> lstBadCols = lstMsgCols
                .stream()
                .filter(msg -> msg.getDataValuesCount() != cntSamples)
                .toList();
        
        // If the list is empty we passed the test
        if (lstBadCols.isEmpty())
            return ResultStatus.SUCCESS;
        
        // Test failed - return failure with list of source names and count
        List<String> lstFailedSrcs = lstBadCols
                .stream()
                .map(msg -> msg.getName() + ": " + Integer.toString(msg.getDataValuesCount()))
                .toList();
        
        return ResultStatus.newFailure("Data column(s) had value count != " + Integer.toString(cntSamples) + ": " + lstFailedSrcs);
    }
    
    /**
     * <p>
     * Verifies that the results set has source data sets all of same size (within each page).
     * </p>
     * <p>
     * Note that the data sources should all have data sets of the same size.  Further, the
     * size should equal the number of samples specified in the sampling interval message.  
     * This method verifies that the given set of data is consistent with the above and ready 
     * for further processing.
     * </p>
     * <p>
     * The following conditions are checked:
     * <ul>
     * <li>Each data source has size equal to the number data samples in sampling message.</li>
     * </ul>
     * If the given data set fails the verification check, the cause of the failure
     * is included in the result.  Otherwise no cause message is provided.
     * </p> 
     * 
     * @param lstResultsSet list of <code>QueryRespoonse</code> messages representing a data query results set
     * 
     * @return  <code>{@link ResultStatus#SUCCESS</code> if each data set PV data with same size,
     *          a failure record with message description if errors detected
     * 
     * @see #verifySourceSizes(BucketData)
     */
    private ResultStatus    verifySourceSizes(List<QueryDataResponse> lstResultsSet) {
        
        // Extract results set data, check each data set, record results
        List<ResultStatus>    lstFails = lstResultsSet
                .stream()
                .<QueryData>map(msgRsp -> msgRsp.getQueryData())
                .<ResultStatus>map(msgData -> this.verifySourceSizes(msgData))
                .filter(ResultStatus::isFailure)
                .toList();

        if (!lstFails.isEmpty())
            return ResultStatus.newFailure("Results set inconsistent source sizes: " + lstFails);
        
        return ResultStatus.SUCCESS;
    }

    /**
     * <p>
     * Extracts and returns the time ranges over which each PV in the results set is active.
     * </p>
     * 
     * @param lstResultsSet list of <code>QueryRespoonse</code> messages representing a data query results set
     * 
     * @return  a list of pairs (PV name, TimeInterval) containing the time ranges of PV activity
     */
    private List<PvActivityRange>    extractActivityRanges(/* QueryRequest msgRequest, */ List<QueryDataResponse> lstResultsSet) {
            
    //        // Get the start and end times of the request
    //        Timestamp   tmsBegin = msgRequest.getQuerySpec().getStartTime();
    //        Timestamp   tmsEnd = msgRequest.getQuerySpec().getEndTime();
            
            // Extract all data columns
            List<DataBucket>    lstMsgBuckets = lstResultsSet
                    .stream()
                    .<QueryData>map(msgRsp -> msgRsp.getQueryData())
                    .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                    .toList();
    
            // Get set of unique source names
            List<String>    lstSrcNms = lstMsgBuckets
                    .stream()
                    .<DataColumn>map(DataBucket::getDataColumn)
                    .<String>map(DataColumn::getName)
                    .toList();
            
            Set<String>     setSrcNms = new TreeSet<>(lstSrcNms);
            
            // Process each data source
            List<PvActivityRange>   lstRanges = new ArrayList<>(setSrcNms.size());
            
            for (String strSrcNm : setSrcNms) {
                
                // Get all sampling clocks for the data source
                List<SamplingClock>    lstClocks = lstMsgBuckets
                        .stream()
                        .filter(msgBck -> msgBck.getDataColumn().getName().equals(strSrcNm))
                        .<DataTimestamps>map(DataBucket::getDataTimestamps)
                        .filter(DataTimestamps::hasSamplingClock)
                        .<SamplingClock>map(DataTimestamps::getSamplingClock)
                        .toList();

                // Get all TimestampList's for the data source
                List<TimestampList>     lstTmsList = lstMsgBuckets
                        .stream()
                        .filter(msgBck -> msgBck.getDataColumn().getName().equals(strSrcNm))
                        .<DataTimestamps>map(DataBucket::getDataTimestamps)
                        .filter(DataTimestamps::hasTimestampList)
                        .<TimestampList>map(DataTimestamps::getTimestampList)
                        .toList();

                // Collect all start timestamps - then find (i.e. reduce) the smallest one
                List<Timestamp>     lstTmsFirst = new LinkedList<>();

                lstTmsFirst.addAll( lstClocks.stream().<Timestamp>map(SamplingClock::getStartTime).toList() );
                lstTmsFirst.addAll( lstTmsList.stream().<Timestamp>map(tsl -> tsl.getTimestamps(0)).toList() );
                
                Timestamp   tmsFirst = lstTmsFirst
                        .stream()
                        .reduce( 
                                (t1, t2) -> { return (ProtoTime.compare(t1, t2) < 0 ? t1 : t2); } 
                               )
                        .get();
                
                // Collect all end timestamps - then find (i.e. reduce) the largest one
                List<Timestamp>     lstTmsLast = new LinkedList<>();
                
                lstTmsLast.addAll( 
                        lstClocks
                        .stream()
                        .<Timestamp>map( clock -> ProtoTime.addNanos(clock.getStartTime(), clock.getCount() * clock.getPeriodNanos()) )
                        .toList()
                        );
                lstTmsLast.addAll( 
                        lstTmsList
                        .stream()
                        .<Timestamp>map( tsl -> tsl.getTimestamps(tsl.getTimestampsCount() - 1))
                        .toList()
                        );
                
                Timestamp   tmsLast = lstTmsLast
                        .stream()
                        .reduce(
                                (t1, t2) -> { return (ProtoTime.compare(t1, t2) < 0) ? t2 : t1; }
                                )
                        .get();
                
//                // Get all SamplingClock's for the data source
//                Timestamp   tmsFirst = lstClocks
//                        .stream()
//                        .<Timestamp>map(clock -> clock.getStartTime())
//                        .reduce(
//                            (tms1, tms2) -> { return (ProtoTime.compare(tms1, tms2) < 0) ? tms1 : tms2; }
//                                )
//                        .get();
    
//                Timestamp   tmsLast = lstClocks
//                        .stream()
//                        .<Timestamp>map(clock -> ProtoTime.addNanos(clock.getStartTime(), clock.getCount() * clock.getPeriodNanos()) )
//                        .reduce(
//                            (tms1, tms2) -> { return (ProtoTime.compare(tms1, tms2) < 0) ? tms2 : tms1; }
//                                )
//                        .get();
    
                TimeInterval    domActive = TimeInterval.from(ProtoMsg.toInstant(tmsFirst), ProtoMsg.toInstant(tmsLast));
                PvActivityRange recActive = PvActivityRange.from(strSrcNm, domActive);
                
                lstRanges.add(recActive);
            }
            
            return lstRanges;
        }
    
    /**
     * <p>
     * Prints out the activity time ranges for each data source in the results set to standard output.
     * </p>
     * 
     * @param strHdr        string header for output message.
     * @param lstResultsSet list of <code>QueryRespoonse</code> messages representing a data query results set
     */
    private void printActivityRanges(String strHdr, List<QueryDataResponse> lstResultsSet) {

        List<PvActivityRange>   lstRanges = this.extractActivityRanges(lstResultsSet);
        
        System.out.println(strHdr + " - PV Activity Time Domains");
        for (PvActivityRange rec : lstRanges) {
            System.out.println("  " + rec.pvName + ": " + rec.range);
        }
        
    }
}
