/*
 * Project: dp-api-common
 * File:	QueryResponseCorrelatorTest.java
 * Package: com.ospreydcs.dp.api.query.model.proto
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
package com.ospreydcs.dp.api.query.model.proto;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.DpApiTestingConfig;
import com.ospreydcs.dp.api.config.grpc.GrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;

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
    private static final GrpcConnectionConfig  CFG_CONN_TEST  = DpApiTestingConfig.getInstance().testQuery.connection;
    
    
    //
    // Test Resources
    //
    
    /** The single connection to the Query Service used by all test cases */
    private static DpQueryConnection        connQuery;
    
    
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
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        connQuery.shutdownSoft();
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryResponseCorrelator#QueryResponseCorrelator(com.ospreydcs.dp.api.grpc.query.DpQueryConnection)}.
     */
    @Test
    public final void testQueryResponseCorrelator() {
        QueryResponseCorrelator correlator = new QueryResponseCorrelator(connQuery);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryResponseCorrelator#setMultiStreaming(boolean)}.
     */
    @Test
    public final void testSetMultiStreaming() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryResponseCorrelator#setCorrelateConcurrency(boolean)}.
     */
    @Test
    public final void testSetCorrelateConcurrency() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryResponseCorrelator#setCorrelateMidstream(boolean)}.
     */
    @Test
    public final void testSetCorrelateMidstream() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryResponseCorrelator#processRequestUnary(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequestUnary() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryResponseCorrelator#processRequestStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testProcessRequestStream() {
        fail("Not yet implemented"); // TODO
    }

}
