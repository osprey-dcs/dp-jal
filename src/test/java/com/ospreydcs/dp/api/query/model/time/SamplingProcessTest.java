/*
 * Project: dp-api-common
 * File:	SamplingProcessTest.java
 * Package: com.ospreydcs.dp.api.query.model.time
 * Type: 	SamplingProcessTest
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
 * @since Feb 9, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.time;

import static org.junit.Assert.*;
import org.junit.Assert;

import java.util.List;
import java.util.MissingResourceException;
import java.util.SortedSet;
import java.util.concurrent.CompletionException;

import javax.naming.CannotProceedException;
import javax.naming.OperationNotSupportedException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.ranges.RangeException;

import com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData;
import com.ospreydcs.dp.api.query.model.proto.QueryDataCorrelator;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.query.test.TestQueryResponses.SingleQueryType;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData;

/**
 * JUnit test cases for <code>{@link SamplingProcess}</code>.
 * 
 * @author Christopher K. Allen
 * @since Feb 9, 2024
 *
 */
public class SamplingProcessTest {

    
    //
    // Class Constants
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.SamplingProcess#from(java.util.SortedSet)}.
     */
    @Test
    public final void testFrom() {
        List<QueryResponse> lstRspMsgs = LST_QUERY_RSP;
        
        try {
            for (QueryResponse msgRsp : lstRspMsgs)
                CORRELATOR.insertQueryResponse(msgRsp);

        } catch (OperationNotSupportedException | CannotProceedException e) {
            Assert.fail(failMessage("QueryDataCorrelator#insertQueryResponse()", e));
            
        }
        
        // Get the processed data and try to create SamplingProcess
        SortedSet<CorrelatedQueryData>  setData = CORRELATOR.getProcessedSet();
        
        try {
            SamplingProcess result = SamplingProcess.from(setData);
            
            assertTrue("Results set contained no data sources.", result.getDataSourceCount() > 0);
            
        } catch (MissingResourceException | IllegalArgumentException | IllegalStateException | 
                 RangeException | UnsupportedOperationException | CompletionException e) {
            
            Assert.fail(failMessage("SamplingProcess#from()", e));
        }
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.SamplingProcess#SamplingProcess(java.util.SortedSet)}.
     */
    @Test
    public final void testSamplingProcess() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.SamplingProcess#getSampleCount()}.
     */
    @Test
    public final void testGetSampleCount() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.SamplingProcess#getTimeDomain()}.
     */
    @Test
    public final void testGetTimeDomain() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.SamplingProcess#getDataSourceCount()}.
     */
    @Test
    public final void testGetDataSourceCount() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.SamplingProcess#getDataSourceNames()}.
     */
    @Test
    public final void testGetDataSourceNames() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.time.SamplingProcess#getSourceType(java.lang.String)}.
     */
    @Test
    public final void testGetSourceType() {
        fail("Not yet implemented"); // TODO
    }

    
    //
    // Support Methods
    //
    
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
