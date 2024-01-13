/*
 * Project: dp-api-common
 * File:	QueryResponseCollectorTest.java
 * Package: com.ospreydcs.dp.api.query.model.proto
 * Type: 	QueryResponseCollectorTest
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

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

/**
 * <p>
 * JUnit test cases for the <code>QueryResponseCollector</code> class.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Jan 13, 2024
 *
 * @see QueryResponseCollector
 */
public class QueryResponseCollectorTest {

    
    //
    // Class Constants
    //
    
    /** Existing file (in resources) containing serialized results from example data query */
    public static final String  STR_FILENAME_QUERY_RESULTS_WIDE = "querydata-results-wide.dat";
    
    /** Existing file (in resources) containing serialized results from example data query */
    public static final String  STR_FILENAME_QUERY_RESULTS_LONG = "querydata-results-wide.dat";
    
    
    //
    // Class Resources
    //
    
    /** Sample query data for test cases - Loaded by test fixture */
    public static List<QueryResponse.QueryReport.QueryData>   LST_QUERY_DATA_WIDE;
    
    /** Sample query data for test cases - Loaded by test fixture */
    public static List<QueryResponse.QueryReport.QueryData>   LST_QUERY_DATA_LONG;
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void setUpBeforeClass() throws Exception {
        
        // Get the wide data set
        InputStream         isFile = QueryResponseCollectorTest.class.getClassLoader().getResourceAsStream(STR_FILENAME_QUERY_RESULTS_WIDE);
        ObjectInputStream   isQueryData = new ObjectInputStream(isFile);
        
        LST_QUERY_DATA_WIDE = (List<QueryResponse.QueryReport.QueryData>)isQueryData.readObject();
        
        isQueryData.close();
        isFile.close();
        
        // Get the long data set
        isFile = QueryResponseCollectorTest.class.getClassLoader().getResourceAsStream(STR_FILENAME_QUERY_RESULTS_LONG);
        isQueryData = new ObjectInputStream(isFile);
        
        LST_QUERY_DATA_LONG = (List<QueryResponse.QueryReport.QueryData>)isQueryData.readObject();
        
        isQueryData.close();
        isFile.close();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryResponseCollector#getTargetRefs()}.
     */
    @Test
    public final void testGetTargetRefs() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryResponseCollector#insertBucketData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.QueryData.DataBucket)}.
     */
    @Test
    public final void testInsertBucketData() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.proto.QueryResponseCollector#insertQueryData(com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.QueryData)}.
     */
    @Test
    public final void testInsertQueryData() {
        QueryResponseCollector  col = new QueryResponseCollector();
        
        for (QueryResponse.QueryReport.QueryData msgData : LST_QUERY_DATA_WIDE) {
            col.insertQueryData(msgData);
        }
        
        Assert.assertTrue("QueryResponseCollector has no data.", col.sizeTargetSet() > 0);
        
        Set<String> setDataSrcs = col.extractDataSourceNames();
        
        System.out.println("Data Source Names: " + setDataSrcs);
    }

}
