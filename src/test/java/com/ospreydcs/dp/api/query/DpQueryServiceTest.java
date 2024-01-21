/*
 * Project: dp-api-common
 * File:	DpQueryServiceTest.java
 * Package: com.ospreydcs.dp.api.query
 * Type: 	DpQueryServiceTest
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
 * @since Jan 10, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.query.model.DpQueryException;
import com.ospreydcs.dp.api.query.model.DpQueryStreamQueueBuffer;
import com.ospreydcs.dp.api.query.test.TestDpDataRequestGenerator;

/**
 * <p>
 * JUnit test cases for <code>{@link DpQueryService}</code> Query Service API interface.
 * </p>
 * <p>
 * <h2>WARNING:</h2>
 * For test cases to pass the following conditions must be current:
 * <ul>
 * <li>
 * A Data Platform Query Service must be currently active and running at the default parameter set.
 * </li>
 * <li>
 * The Data Platform data archive must be populated with test data from the Ingestion Service benchmark 
 * application.
 * </li>
 * </ul>
 * <p>
 *
 * @author Christopher K. Allen
 * @since Jan 10, 2024
 *
 */
public class DpQueryServiceTest {

    
    //
    // Application Resources
    //
    
    /** The default Query Service configuration parameters */
    private static final DpQueryConfig  CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** Output file for <code>DpQueryStreamQueueBuffer</code> data buffer results with lots of columns */
    public static final String  STR_PATH_QUERY_DATA_RESULTS_WIDE = "src/test/resources/data/querydata-results-wide.dat";
    
    /** Output file for <code>DpQueryStreamQueueBuffer</code> data buffer results with largest time interval */
    public static final String  STR_PATH_QUERY_DATA_RESULTS_LONG = "src/test/resources/data/querydata-results-long.dat";
    
    
//    /** The inception time instant of the test Data Platform data archive test data set*/
//    public static final Instant INS_INCEPT = Instant.ofEpochSecond(1698767462L);
//    
//    /** The final time instant of all Data Platform data archive test data set */
//    public static final Instant INS_FINAL = INS_INCEPT.plusSeconds(60L);
//    
//    /** The total number of unique data source names within the Data Platform data archive test data set */
//    public static final int     CNT_PV_NAMES = 4000;
//    
//    /** List of all data source names within the Data Platform data archive test data set */
//    public static final List<String> LST_PV_NAMES = IntStream.rangeClosed(1, CNT_PV_NAMES).mapToObj( i -> "pv_" + Integer.toString(i)).toList();   
//    
    
    /** Timeout limit */
    public static final Long        LNG_TMOUT = 5L;
    
    /** Timeout units */
    public static final TimeUnit    TU_TMOUT = TimeUnit.SECONDS;
    
    
    //
    // Class Resources
    //
    
    /** The Data Platform Query Service API interface under test - created in fixture */
    private static DpQueryService   apiQuery;
    
    
//    //
//    // Class Methods
//    //
//    
//    /**
//     * <p>
//     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
//     * </p>
//     * 
//     * @param cntSources    number of data sources in the query
//     * 
//     * @return  new <code>DpDataRequest</code> for the first cntSources in LST_PV_NAMES and time range [INS_INCEPT, INS_FINAL]
//     */
//    public static DpDataRequest createRequest(int cntSources) {
//        DpDataRequest   rqst = DpDataRequest.newRequest();
//        
//        List<String>    lstNames = LST_PV_NAMES.subList(0, cntSources);
//        
//        rqst.rangeBetween(INS_INCEPT, INS_FINAL);
//        rqst.selectSources(lstNames);
//        
//        return rqst;
//    }
//    
//    /**
//     * <p>
//     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
//     * </p>
//     * 
//     * @param cntSources    number of data sources in the query
//     * @param lngDuration   time duration of query in seconds, range = [INS_INCEPT, INS_INCEPT + lngDuration]
//     * 
//     * @return  new <code>DpDataRequest</code> for the first cntSources in LST_PV_NAMES and the specified time range
//     */
//    public static DpDataRequest createRequest(int cntSources, long lngDuration) {
//        DpDataRequest   rqst = DpDataRequest.newRequest();
//        
//        List<String>    lstNames = LST_PV_NAMES.subList(0, cntSources);
//        Instant         insFinal = INS_INCEPT.plusSeconds(lngDuration);
//        
//        rqst.rangeBetween(INS_INCEPT, insFinal);
//        rqst.selectSources(lstNames);
//        
//        return rqst;
//    }
//    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        apiQuery = DpQueryServiceFactory.INSTANCE.connect();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        apiQuery.shutdownSoft();
        apiQuery.awaitTermination();
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
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryService#querySingle(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testQuerySingle() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryService#queryUniStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testQueryUniStreamDpDataRequest() {
        final int   CNT_SOURCES = 100;
        final long  LNG_DURATION = 10;
        
        final DpDataRequest rsqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        
        try {
            Instant insStart = Instant.now();
            DpQueryStreamQueueBuffer bufResult = apiQuery.queryUniStream(rsqst);
            
            bufResult.awaitStreamCompleted();
            Instant insStop = Instant.now();
            
            Duration    durQuery = Duration.between(insStart, insStop);
            Long        szQuery = bufResult.getPageSize() * bufResult.getBufferSize();
            Long        cntVals = szQuery/Double.BYTES;
            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
            
            System.out.println("Query completed in " + durQuery.toMillis() + " milliseconds.");
            System.out.println("  Total query size = " + szQuery);
            System.out.println("  Double value count = " + cntVals);
            System.out.println("  Transmission rate = " + dblRate);
            System.out.println(bufResult.toString());
            
            if (bufResult.isStreamError())
                Assert.fail("Stream buffer reported an error - " + bufResult.getStreamError());
            
        } catch (DpQueryException e) {
            Assert.fail("Exception thrown during query: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Process interrupted while waiting for stream completion: " + e.getMessage());
            
        } catch (TimeoutException e) {
            Assert.fail("Timeout while waiting for stream completion: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryService#queryUniStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
//    @Test
    public final void testQueryUniStreamDpDataRequestBig() {
        final int   CNT_SOURCES = 1000;
        final long  LNG_DURATION = 60;
        
        final DpDataRequest rsqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        
        try {
            Instant insStart = Instant.now();
            DpQueryStreamQueueBuffer bufResult = apiQuery.queryUniStream(rsqst);
            
            bufResult.awaitStreamCompleted();
            Instant insStop = Instant.now();
            
            Duration    durQuery = Duration.between(insStart, insStop);
            Long        szQuery = bufResult.getPageSize() * bufResult.getBufferSize();
            Long        cntVals = szQuery/Double.BYTES;
            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
            
            System.out.println("Query completed in " + durQuery.toMillis() + " milliseconds.");
            System.out.println("  Total query size = " + szQuery);
            System.out.println("  Double value count = " + cntVals);
            System.out.println("  Transmission rate = " + dblRate);
            System.out.println(bufResult.toString());
            
            if (bufResult.isStreamError())
                Assert.fail("Stream buffer reported an error - " + bufResult.getStreamError());
            
        } catch (DpQueryException e) {
            Assert.fail("Exception thrown during query: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Process interrupted while waiting for stream completion: " + e.getMessage());
            
        } catch (TimeoutException e) {
            Assert.fail("Timeout while waiting for stream completion: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryService#queryUniStream(com.ospreydcs.dp.api.query.DpDataRequest, long, java.util.concurrent.TimeUnit)}.
     */
//    @Test
    public final void testQueryUniStreamDpDataRequestLongTimeUnit() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryService#queryBidiStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testQueryBidiStreamDpDataRequest() {
        final int   CNT_SOURCES = 100;
        final long  LNG_DURATION = 10;
        
        final DpDataRequest rsqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        
        try {
            Instant insStart = Instant.now();
            DpQueryStreamQueueBuffer bufResult = apiQuery.queryBidiStream(rsqst);
            
            bufResult.awaitStreamCompleted();
            Instant insStop = Instant.now();
            
            Duration    durQuery = Duration.between(insStart, insStop);
            Long        szQuery = bufResult.getPageSize() * bufResult.getBufferSize();
            Long        cntVals = szQuery/Double.BYTES;
            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
            
            System.out.println("Query completed in " + durQuery.toMillis() + " milliseconds.");
            System.out.println("  Total query size = " + szQuery);
            System.out.println("  Double value count = " + cntVals);
            System.out.println("  Transmission rate = " + dblRate);
            System.out.println(bufResult.toString());
            
            if (bufResult.isStreamError())
                Assert.fail("Stream buffer reported an error - " + bufResult.getStreamError());
            
        } catch (DpQueryException e) {
            Assert.fail("Exception thrown during query: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Process interrupted while waiting for stream completion: " + e.getMessage());
            
        } catch (TimeoutException e) {
            Assert.fail("Timeout while waiting for stream completion: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryService#queryBidiStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
//    @Test
    public final void testQueryBidiStreamDpDataRequestBig() {
        final int   CNT_SOURCES = 1000;
        final long  LNG_DURATION = 60;
        
        final DpDataRequest rsqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        
        try {
            Instant insStart = Instant.now();
            DpQueryStreamQueueBuffer bufResult = apiQuery.queryBidiStream(rsqst);
            
            bufResult.awaitStreamCompleted();
            Instant insStop = Instant.now();
            
            Duration    durQuery = Duration.between(insStart, insStop);
            Long        szQuery = bufResult.getPageSize() * bufResult.getBufferSize();
            Long        cntVals = szQuery/Double.BYTES;
            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
            
            System.out.println("Query completed in " + durQuery.toMillis() + " milliseconds.");
            System.out.println("  Total query size = " + szQuery);
            System.out.println("  Double value count = " + cntVals);
            System.out.println("  Transmission rate = " + dblRate);
            System.out.println(bufResult.toString());
            
            if (bufResult.isStreamError())
                Assert.fail("Stream buffer reported an error - " + bufResult.getStreamError());
            
        } catch (DpQueryException e) {
            Assert.fail("Exception thrown during query: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Process interrupted while waiting for stream completion: " + e.getMessage());
            
        } catch (TimeoutException e) {
            Assert.fail("Timeout while waiting for stream completion: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryService#queryBidiStream(com.ospreydcs.dp.api.query.DpDataRequest, long, java.util.concurrent.TimeUnit)}.
     */
//    @Test
    public final void testQueryBidiStreamDpDataRequestLongTimeUnit() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Writes the results data from a BIDI stream query to the output file.
     */
//    @Test
    public final void testSaveQueryDataWide() {
        final int   CNT_SOURCES = 100;
        final long  LNG_DURATION = 10;
        
        final DpDataRequest rsqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        final DpQueryStreamQueueBuffer   bufResult;
        
        try {
            Instant insStart = Instant.now();
            bufResult = apiQuery.queryBidiStream(rsqst);
            
            bufResult.awaitStreamCompleted();
            Instant insStop = Instant.now();
            
            Duration    durQuery = Duration.between(insStart, insStop);
            Long        szQuery = bufResult.getPageSize() * bufResult.getBufferSize();
            Long        cntVals = szQuery/Double.BYTES;
            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
            
            System.out.println("Query completed in " + durQuery.toMillis() + " milliseconds.");
            System.out.println("  Total query size = " + szQuery);
            System.out.println("  Double value count = " + cntVals);
            System.out.println("  Transmission rate = " + dblRate);
            System.out.println(bufResult.toString());
            
            if (bufResult.isStreamError())
                Assert.fail("Stream buffer reported an error - " + bufResult.getStreamError());
            
            
        } catch (DpQueryException e) {
            Assert.fail("Exception thrown during query: " + e.getMessage());
            return;
            
        } catch (InterruptedException e) {
            Assert.fail("Process interrupted while waiting for stream completion: " + e.getMessage());
            return;
            
        } catch (TimeoutException e) {
            Assert.fail("Timeout while waiting for stream completion: " + e.getMessage());
            return;
            
        }
        
        // Save the result to an output file
        String  strFilePath = STR_PATH_QUERY_DATA_RESULTS_WIDE;
        
        try {
            FileOutputStream    fos = new FileOutputStream(strFilePath);
            ObjectOutputStream  oos = new ObjectOutputStream(fos);
            
            oos.writeObject(bufResult.getBuffer());
            
            oos.close();
            fos.close();

        } catch (FileNotFoundException e) {
            Assert.fail("Unable to create/open output file " + strFilePath);
            
        } catch (IOException e) {
            Assert.fail("Unable to write data to output file " + strFilePath);
            
        }
    }

    /**
     * Writes the results data from a BIDI stream query to the output file.
     */
//    @Test
    public final void testSaveQueryDataLong() {
        final int   CNT_SOURCES = 10;
        final long  LNG_DURATION = 60;
        
        final DpDataRequest rsqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        final DpQueryStreamQueueBuffer   bufResult;
        
        try {
            Instant insStart = Instant.now();
            bufResult = apiQuery.queryBidiStream(rsqst);
            
            bufResult.awaitStreamCompleted();
            Instant insStop = Instant.now();
            
            Duration    durQuery = Duration.between(insStart, insStop);
            Long        szQuery = bufResult.getPageSize() * bufResult.getBufferSize();
            Long        cntVals = szQuery/Double.BYTES;
            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
            
            System.out.println("Query completed in " + durQuery.toMillis() + " milliseconds.");
            System.out.println("  Total query size = " + szQuery);
            System.out.println("  Double value count = " + cntVals);
            System.out.println("  Transmission rate = " + dblRate);
            System.out.println(bufResult.toString());
            
            if (bufResult.isStreamError())
                Assert.fail("Stream buffer reported an error - " + bufResult.getStreamError());
            
            
        } catch (DpQueryException e) {
            Assert.fail("Exception thrown during query: " + e.getMessage());
            return;
            
        } catch (InterruptedException e) {
            Assert.fail("Process interrupted while waiting for stream completion: " + e.getMessage());
            return;
            
        } catch (TimeoutException e) {
            Assert.fail("Timeout while waiting for stream completion: " + e.getMessage());
            return;
            
        }
        
        // Save the result to an output file
        String  strFilePath = STR_PATH_QUERY_DATA_RESULTS_LONG;
        
        try {
            FileOutputStream    fos = new FileOutputStream(strFilePath);
            ObjectOutputStream  oos = new ObjectOutputStream(fos);
            
            oos.writeObject(bufResult.getBuffer());
            
            oos.close();
            fos.close();

        } catch (FileNotFoundException e) {
            Assert.fail("Unable to create/open output file " + strFilePath);
            
        } catch (IOException e) {
            Assert.fail("Unable to write data to output file " + strFilePath);
            
        }
    }
}
