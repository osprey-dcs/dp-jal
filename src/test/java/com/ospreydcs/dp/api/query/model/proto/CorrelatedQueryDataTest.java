/*
 * Project: dp-api-common
 * File:	CorrelatedQueryDataTest.java
 * Package: com.ospreydcs.dp.api.query.model.proto
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
package com.ospreydcs.dp.api.query.model.proto;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.query.test.TestQueryResponses.SingleQueryType;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData.DataBucket;

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
    public static final List<DataBucket>    LST_BUCKET = TestQueryResponses.queryBuckets(SingleQueryType.BUCKET);
    
    /** Test data - single data source bucket list */
    public static final List<DataBucket>    Lst_BUCKETS_ONE = TestQueryResponses.queryBuckets(SingleQueryType.ONE_SOURCE);
    
    /** Test data - single data source bucket list */
    public static final List<DataBucket>    Lst_BUCKETS_TWO = TestQueryResponses.queryBuckets(SingleQueryType.TWO_SOURCE);

    
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData#from(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData.DataBucket)}.
     */
    @Test
    public final void testFrom() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData#CorrelatedQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData.DataBucket)}.
     */
    @Test
    public final void testCorrelatedQueryData() {
        DataBucket  msgBucket = LST_BUCKET.get(0);
        
        CorrelatedQueryData cqdTest = new CorrelatedQueryData(msgBucket);
        
        Assert.assertTrue("Number of data sources NOT equal to 1.", cqdTest.getSourceCount() == 1);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData#getSampleCount()}.
     */
    @Test
    public final void testGetSampleCount() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData#getStartInstant()}.
     */
    @Test
    public final void testGetStartInstant() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData#getTimeDomain()}.
     */
    @Test
    public final void testGetTimeDomain() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData#getSourceNames()}.
     */
    @Test
    public final void testGetSourceNames() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData#getSamplingMessage()}.
     */
    @Test
    public final void testGetSamplingMessage() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData#getAllDataMessages()}.
     */
    @Test
    public final void testGetAllDataMessages() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData#verifySourceUniqueness()}.
     */
    @Test
    public final void testVerifySourceUniqueness() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData#verifySourceSizes()}.
     */
    @Test
    public final void testVerifySourceSizes() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData#insertBucketData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData.DataBucket)}.
     */
    @Test
    public final void testInsertBucketData() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData#compareTo(com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData)}.
     */
    @Test
    public final void testCompareTo() {
        fail("Not yet implemented"); // TODO
    }

}
