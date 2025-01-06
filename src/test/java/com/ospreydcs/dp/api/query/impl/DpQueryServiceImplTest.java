/*
 * Project: dp-api-common
 * File:	DpQueryServiceImplTest.java
 * Package: com.ospreydcs.dp.api.query
 * Type: 	DpQueryServiceImplTest
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
package com.ospreydcs.dp.api.query.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.DpApiTestingConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.model.DpGrpcStreamType;
import com.ospreydcs.dp.api.model.IDataTable;
import com.ospreydcs.dp.api.model.PvMetaRecord;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpMetadataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.DpQueryStreamBuffer;
import com.ospreydcs.dp.api.query.impl.DpQueryServiceFactory;
import com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl;
import com.ospreydcs.dp.api.query.model.DpQueryStreamTypeDeprecated;
import com.ospreydcs.dp.api.query.model.request.DataRequestDecompType;
import com.ospreydcs.dp.api.query.model.request.DataRequestDecomposer;
import com.ospreydcs.dp.api.query.test.TestDpDataRequestGenerator;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

/**
 * <p>
 * JUnit test cases for <code>{@link DpQueryServiceImpl}</code> Query Service API interface.
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
public class DpQueryServiceImplTest {

    
    //
    // Application Resources
    //
    
//    /** The default Query Service configuration parameters */
//    private static final DpQueryConfig          CFG_QUERY = DpApiConfig.getInstance().query;
//    
//    /** The default API Test configuration */
//    private static final DpApiTestingConfig     CFG_TEST = DpApiTestingConfig.getInstance();
    
    
    //
    // Class Constants
    //
    
    /** Process Variable prefixes used in the test archive */
    public static final String  STR_PV_PREFIX = "dpTest_"; // CFG_TEST.testArchive.pvPrefix;
    
    
    /** Output file for <code>DpQueryStreamQueueBufferDeprecated</code> data buffer results with lots of columns */
    public static final String  STR_PATH_QUERY_DATA_RESULTS_WIDE = "src/test/resources/data/querydata-results-wide.dat";
    
    /** Output file for <code>DpQueryStreamQueueBufferDeprecated</code> data buffer results with largest time interval */
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
    private static DpQueryServiceImpl   apiQuery;
    
    
    //
    // Class Methods
    //
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        apiQuery = DpQueryServiceFactory.FACTORY.connect();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        apiQuery.shutdown();
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
    // ----------- Test Cases ------------
    //
    
    //
    // Metadata Queries
    //
    
    /** 
     * Test method for {@link DpQueryServiceImpl#queryPvs(DpMetadataReuest)}.
     */
    @Test
    public final void testQueryPvsSingle() {
        String      strPvName1 = STR_PV_PREFIX + Integer.toString(1);
        
        final DpMetadataRequest rqst = DpMetadataRequest.newRequest();
        
        rqst.selectPv(strPvName1);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());
            
            List<PvMetaRecord>  lstRecs = apiQuery.queryPvs(rqst);
            
            Assert.assertTrue("Record list should contain 1 record", lstRecs.size() == 1);
            
            PvMetaRecord        recTest = lstRecs.get(0);
            
            Assert.assertEquals(strPvName1, recTest.name());
            
            System.out.println("Single PV Metadata Record:");
            System.out.println(recTest);
            
        } catch (DpQueryException e) {
            Assert.fail("Metadata query threw exception: + " + e.getMessage());
            
        }
    }
    
    /** 
     * Test method for {@link DpQueryServiceImpl#queryPvs(DpMetadataReuest)}.
     */
    @Test
    public final void testQueryPvsMultiple() {
        final int   SZ_LIST = 10;
        
        List<String>    lstPvNames = IntStream
                .rangeClosed(1, SZ_LIST)
                .mapToObj(i -> STR_PV_PREFIX + Integer.toString(i))
                .toList();
        
        
        final DpMetadataRequest rqst = DpMetadataRequest.newRequest();
        
        for (String strName : lstPvNames) 
            rqst.selectPv(strName);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

            List<PvMetaRecord>  lstRecs = apiQuery.queryPvs(rqst);
            
            Assert.assertTrue("Record list should contain " + SZ_LIST + " records", lstRecs.size() == SZ_LIST);

            for (PvMetaRecord rec : lstRecs) {
                String      strPvName = rec.name();
                
                Assert.assertTrue(lstPvNames.contains(strPvName));
            }
            
        } catch (DpQueryException e) {
            Assert.fail("Metadata query threw exception: + " + e.getMessage());
            
        }
    }
    
    /** 
     * Test method for {@link DpQueryServiceImpl#queryPvs(DpMetadataReuest)}.
     */
    @Test
    public final void testQueryPvsList() {
        final int   SZ_LIST = 10;
        
        List<String>    lstPvNames = IntStream
                .rangeClosed(1, SZ_LIST)
                .mapToObj(i -> STR_PV_PREFIX + Integer.toString(i))
                .toList();
        
        
        final DpMetadataRequest rqst = DpMetadataRequest.newRequest();
        
        rqst.selectPvs(lstPvNames);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

            List<PvMetaRecord>  lstRecs = apiQuery.queryPvs(rqst);
            
            Assert.assertTrue("Record list should contain " + SZ_LIST + " records", lstRecs.size() == SZ_LIST);

            for (PvMetaRecord rec : lstRecs) {
                String      strPvName = rec.name();
                
                Assert.assertTrue(lstPvNames.contains(strPvName));
            }
            
        } catch (DpQueryException e) {
            Assert.fail("Metadata query threw exception: + " + e.getMessage());
            
        }
    }
    
    /** 
     * Test method for {@link DpQueryServiceImpl#queryPvs(DpMetadataReuest)}.
     */
    @Test
    public final void testQueryPvsRegex() {
        String      strPvRegex = STR_PV_PREFIX + "111";
        
        final DpMetadataRequest rqst = DpMetadataRequest.newRequest();
        
        rqst.setPvRegex(strPvRegex);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

            List<PvMetaRecord>  lstRecs = apiQuery.queryPvs(rqst);
            
            Assert.assertTrue("Record list should contain at least 1 record", lstRecs.size() >= 1);
            
            System.out.println(" Regular Expression PV Metadata Records: " + strPvRegex);
            for (PvMetaRecord rec : lstRecs) {
                System.out.println("  name: " + rec.name());
                System.out.println("  type: " + rec.type());
                System.out.println("  first tms: " + rec.firstTimestamp());
                System.out.println("  last tms: " + rec.lastTimestamp());
                System.out.println();
            }
            
        } catch (DpQueryException e) {
            Assert.fail("Metadata query threw exception: + " + e.getMessage());
            
        }
    }

    
    // 
    // Data Queries
    //
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryDataUnary(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testQueryUnary() {
        final int   CNT_SOURCES = 10;
        final long  LNG_DURATION = 2L;
        
        final DpDataRequest rsqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

            Instant insStart = Instant.now();
            IDataTable  table = apiQuery.queryDataUnary(rsqst);
            Instant insStop = Instant.now();
            
            Duration    durQuery = Duration.between(insStart, insStop);
            Long        szQuery = table.allocationSize();
            Long        cntVals = szQuery/Double.BYTES;
            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
            
            System.out.println("  Unary Query completed in " + durQuery.toMillis() + " milliseconds.");
            System.out.println("  Total query size = " + szQuery);
            System.out.println("  Double value count = " + cntVals);
            System.out.println("  Transmission rate = " + dblRate);
            
        } catch (DpQueryException e) {
            Assert.fail("Process exception during query operation: type=" + e.getClass().getSimpleName() + ", message=" + e.getMessage());
            
        }
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryData(DpDataRequest)
     */
    @Test
    public final void testQueryDataUni() {
        final Integer   CNT_SOURCES = 500;
        final Long      LNG_DURATION = 10L;
        
        final Integer   LNG_ROWS = 1000 * LNG_DURATION.intValue(); // sample rate (1 kHz) times duration
        
        final DpDataRequest rqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        rqst.setStreamType(DpGrpcStreamType.BACKWARD);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

            IDataTable  table = apiQuery.queryData(rqst);
            
            Assert.assertEquals(CNT_SOURCES, table.getColumnCount());
//            Assert.assertEquals(LNG_ROWS, table.getRowCount());  // Buckets in test archive are 1000 values
            
        } catch (DpQueryException e) {
            Assert.fail("queryData() threw exception: "  + e);
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryData(DpDataRequest)
     */
    @Test
    public final void testQueryDataBidi() {
        final Integer   CNT_SOURCES = 500;
        final Long      LNG_DURATION = 10L;
        
        final Integer   LNG_ROWS = 1000 * LNG_DURATION.intValue(); // sample rate (1 kHz) times duration
        
        final DpDataRequest rqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        rqst.setStreamType(DpGrpcStreamType.BIDIRECTIONAL);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

            IDataTable  table = apiQuery.queryData(rqst);
            
            Assert.assertEquals(CNT_SOURCES, table.getColumnCount());
//            Assert.assertEquals(LNG_ROWS, table.getRowCount());   // Buckets in test archive are 1000 values
            
        } catch (DpQueryException e) {
            Assert.fail("queryData() threw exception: "  + e);
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryData(List)
     */
    @Test
    public final void testQueryDataListUni() {
        final Integer           CNT_SOURCES = 500;
        final Long              LNG_DURATION = 10L;
        final int               CNT_REQUESTS = 3;
        final DpGrpcStreamType  ENM_STREAM_TYPE = DpGrpcStreamType.BACKWARD;
        DataRequestDecomposer   rqstDecomp = DataRequestDecomposer.create();
        
        final Integer   LNG_ROWS = 1000 * LNG_DURATION.intValue(); // sample rate (1 kHz) times duration
        
        final DpDataRequest rqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        final List<DpDataRequest>   lstRqsts = rqstDecomp.buildCompositeRequest(rqst, DataRequestDecompType.HORIZONTAL, CNT_REQUESTS);
        
        rqst.setStreamType(ENM_STREAM_TYPE);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

            IDataTable  table = apiQuery.queryData(lstRqsts);
            
            Assert.assertEquals(CNT_SOURCES, table.getColumnCount());
//            Assert.assertEquals(LNG_ROWS, table.getRowCount());       // Buckets in test archive are 1000 values
            
        } catch (DpQueryException e) {
            Assert.fail("queryData() threw exception: "  + e);
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryData(List)
     */
    @Test
    public final void testQueryDataListBidi() {
        final int               CNT_SOURCES = 500;
        final long              LNG_DURATION = 10L;
        final int               CNT_REQUESTS = 3;
        final DpGrpcStreamType  ENM_STREAM_TYPE = DpGrpcStreamType.BIDIRECTIONAL;
        DataRequestDecomposer   rqstDecomp = DataRequestDecomposer.create();
        
        final int               CNT_ROWS = 1000 * Long.valueOf(LNG_DURATION).intValue(); // sample rate (1 kHz) times duration
        
        final DpDataRequest rqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        final List<DpDataRequest>   lstRqsts = rqstDecomp.buildCompositeRequest(rqst, DataRequestDecompType.HORIZONTAL, CNT_REQUESTS);
        
        rqst.setStreamType(ENM_STREAM_TYPE);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

             IDataTable  table = apiQuery.queryData(lstRqsts);
             int cntCols = table.getColumnCount();
             int cntRows = table.getRowCount();
             
            Assert.assertEquals(CNT_SOURCES, cntCols);
//            Assert.assertEquals(CNT_ROWS, cntRows);
            
        } catch (DpQueryException e) {
            Assert.fail("queryData() threw exception: "  + e);
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryDataStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testQueryDataStreamDpDataRequestUni() {
        final int   CNT_SOURCES = 100;
        final long  LNG_DURATION = 10L;
        
        final DpDataRequest rqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        rqst.setStreamType(DpGrpcStreamType.BACKWARD);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

            DpQueryStreamBuffer bufResult = apiQuery.queryDataStream(rqst);
            
            Instant insStart = Instant.now();
            bufResult.start();
            bufResult.awaitStreamCompleted();
            Instant insStop = Instant.now();
            
            Duration    durQuery = Duration.between(insStart, insStop);
            Long        szQuery = bufResult.getPageSize() * bufResult.getBufferSize();
            Long        cntVals = szQuery/Double.BYTES;
            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
            
            System.out.println("  Query completed in " + durQuery.toMillis() + " milliseconds.");
            System.out.println("  Total query size = " + szQuery);
            System.out.println("  Double value count = " + cntVals);
            System.out.println("  Transmission rate = " + dblRate);
            System.out.println(bufResult.toString());
            
            if (bufResult.isStreamError())
                Assert.fail("Stream buffer reported an error - " + bufResult.getStreamError());
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            Assert.fail("Exception thrown during stream start: " + e.getMessage());

        } catch (InterruptedException e) {
            Assert.fail("Process interrupted while waiting for stream completion: " + e.getMessage());
            
        } catch (TimeoutException e) {
            Assert.fail("Timeout while waiting for stream completion: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryDataStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testQueryDataStreamDpDataRequestBidi() {
        final int   CNT_SOURCES = 100;
        final long  LNG_DURATION = 10L;
        
        final DpDataRequest rqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        rqst.setStreamType(DpGrpcStreamType.BIDIRECTIONAL);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

            DpQueryStreamBuffer bufResult = apiQuery.queryDataStream(rqst);
            
            Instant insStart = Instant.now();
            bufResult.start();
            bufResult.awaitStreamCompleted();
            Instant insStop = Instant.now();
            
            Duration    durQuery = Duration.between(insStart, insStop);
            Long        szQuery = bufResult.getPageSize() * bufResult.getBufferSize();
            Long        cntVals = szQuery/Double.BYTES;
            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
            
            System.out.println("  Query completed in " + durQuery.toMillis() + " milliseconds.");
            System.out.println("  Total query size = " + szQuery);
            System.out.println("  Double value count = " + cntVals);
            System.out.println("  Transmission rate = " + dblRate);
            System.out.println(bufResult.toString());
            
            if (bufResult.isStreamError())
                Assert.fail("Stream buffer reported an error - " + bufResult.getStreamError());
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            Assert.fail("Exception thrown during stream start: " + e.getMessage());

        } catch (InterruptedException e) {
            Assert.fail("Process interrupted while waiting for stream completion: " + e.getMessage());
            
        } catch (TimeoutException e) {
            Assert.fail("Timeout while waiting for stream completion: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryStreamUni(com.ospreydcs.dp.api.query.DpDataRequest)}.
     * 
     * @deprecated
     */
    @Test
    public final void testQueryStreamUniDpDataRequestOne() {
        
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
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

            DpQueryStreamBuffer bufResult = apiQuery.queryStreamUni(rqst);
        
            bufResult.startAndAwaitCompletion();
            
            List<QueryDataResponse> lstResultSet = bufResult.getBuffer();
            List<QueryDataResponse.QueryData.DataBucket>    lstBuckets = lstResultSet
                    .stream()
                    .map(msgRsp -> msgRsp.getQueryData())
                    .flatMap(msgData -> msgData.getDataBucketsList().stream())
                    .toList();
            
            System.out.println("  Query for " + strSrcNm + " in " + TimeInterval.from(insBegin, insEnd));
            System.out.println("  results set size: " + lstResultSet.size());
            System.out.println("  bucket count: " + lstBuckets.size());
            
        } catch (Exception e) {
            Assert.fail("Process exception while waiting for stream completion: type=" + e.getClass().getSimpleName() + ", message=" + e.getMessage());
            
        }
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryStreamUni(com.ospreydcs.dp.api.query.DpDataRequest)}.
     * 
     * @deprecated
     */
    @Test
    public final void testQueryStreamUniDpDataRequest() {
        final int   CNT_SOURCES = 100;
        final long  LNG_DURATION = 10L;
        
        final DpDataRequest rsqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

            DpQueryStreamBuffer bufResult = apiQuery.queryStreamUni(rsqst);
            
            Instant insStart = Instant.now();
            bufResult.start();
            bufResult.awaitStreamCompleted();
            Instant insStop = Instant.now();
            
            Duration    durQuery = Duration.between(insStart, insStop);
            Long        szQuery = bufResult.getPageSize() * bufResult.getBufferSize();
            Long        cntVals = szQuery/Double.BYTES;
            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
            
            System.out.println("  Query completed in " + durQuery.toMillis() + " milliseconds.");
            System.out.println("  Total query size = " + szQuery);
            System.out.println("  Double value count = " + cntVals);
            System.out.println("  Transmission rate = " + dblRate);
            System.out.println(bufResult.toString());
            
            if (bufResult.isStreamError())
                Assert.fail("Stream buffer reported an error - " + bufResult.getStreamError());
            
//        } catch (DpQueryException e) {
//            Assert.fail("Exception thrown during query: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Process interrupted while waiting for stream completion: " + e.getMessage());
            
        } catch (TimeoutException e) {
            Assert.fail("Timeout while waiting for stream completion: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryStreamUni(com.ospreydcs.dp.api.query.DpDataRequest)}.
     * 
     * @deprecated
     */
//    @Test
    public final void testQueryStreamUniDpDataRequestBig() {
        final int   CNT_SOURCES = 1000;
        final long  LNG_DURATION = 60;
        
        final DpDataRequest rsqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

            DpQueryStreamBuffer bufResult = apiQuery.queryStreamUni(rsqst);
            
            Instant insStart = Instant.now();
            bufResult.start();
            bufResult.awaitStreamCompleted();
            Instant insStop = Instant.now();
            
            Duration    durQuery = Duration.between(insStart, insStop);
            Long        szQuery = bufResult.getPageSize() * bufResult.getBufferSize();
            Long        cntVals = szQuery/Double.BYTES;
            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
            
            System.out.println("  Query completed in " + durQuery.toMillis() + " milliseconds.");
            System.out.println("  Total query size = " + szQuery);
            System.out.println("  Double value count = " + cntVals);
            System.out.println("  Transmission rate = " + dblRate);
            System.out.println(bufResult.toString());
            
            if (bufResult.isStreamError())
                Assert.fail("Stream buffer reported an error - " + bufResult.getStreamError());
            
//        } catch (DpQueryException e) {
//            Assert.fail("Exception thrown during query: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Process interrupted while waiting for stream completion: " + e.getMessage());
            
        } catch (TimeoutException e) {
            Assert.fail("Timeout while waiting for stream completion: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryStreamBidi(com.ospreydcs.dp.api.query.DpDataRequest)}.
     * 
     * @deprecated
     */
    @Test
    public final void testQueryStreamBidiDpDataRequest() {
        final int   CNT_SOURCES = 100;
        final long  LNG_DURATION = 10;
        
        final DpDataRequest rsqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

            DpQueryStreamBuffer bufResult = apiQuery.queryStreamBidi(rsqst);
            
            Instant insStart = Instant.now();
            bufResult.start();
            bufResult.awaitStreamCompleted();
            Instant insStop = Instant.now();
            
            Duration    durQuery = Duration.between(insStart, insStop);
            Long        szQuery = bufResult.getPageSize() * bufResult.getBufferSize();
            Long        cntVals = szQuery/Double.BYTES;
            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
            
            System.out.println("  Query completed in " + durQuery.toMillis() + " milliseconds.");
            System.out.println("  Total query size = " + szQuery);
            System.out.println("  Double value count = " + cntVals);
            System.out.println("  Transmission rate = " + dblRate);
            System.out.println(bufResult.toString());
            
            if (bufResult.isStreamError())
                Assert.fail("Stream buffer reported an error - " + bufResult.getStreamError());
            
//        } catch (DpQueryException e) {
//            Assert.fail("Exception thrown during query: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Process interrupted while waiting for stream completion: " + e.getMessage());
            
        } catch (TimeoutException e) {
            Assert.fail("Timeout while waiting for stream completion: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryStreamBidi(com.ospreydcs.dp.api.query.DpDataRequest)}.
     * 
     * @deprecated
     */
//    @Test
    public final void testQueryStreamBidiDpDataRequestBig() {
        final int   CNT_SOURCES = 1000;
        final long  LNG_DURATION = 60;
        
        final DpDataRequest rsqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
        
        try {
            System.out.println(JavaRuntime.getQualifiedMethodName());

            DpQueryStreamBuffer bufResult = apiQuery.queryStreamBidi(rsqst);
            
            Instant insStart = Instant.now();
            bufResult.start();
            bufResult.awaitStreamCompleted();
            Instant insStop = Instant.now();
            
            Duration    durQuery = Duration.between(insStart, insStop);
            Long        szQuery = bufResult.getPageSize() * bufResult.getBufferSize();
            Long        cntVals = szQuery/Double.BYTES;
            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
            
            System.out.println("  Query completed in " + durQuery.toMillis() + " milliseconds.");
            System.out.println("  Total query size = " + szQuery);
            System.out.println("  Double value count = " + cntVals);
            System.out.println("  Transmission rate = " + dblRate);
            System.out.println(bufResult.toString());
            
            if (bufResult.isStreamError())
                Assert.fail("Stream buffer reported an error - " + bufResult.getStreamError());
            
//        } catch (DpQueryException e) {
//            Assert.fail("Exception thrown during query: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Process interrupted while waiting for stream completion: " + e.getMessage());
            
        } catch (TimeoutException e) {
            Assert.fail("Timeout while waiting for stream completion: " + e.getMessage());
            
        }
    }

//    /**
//     * Writes the results data from a BIDI stream query to the output file.
//     */
////    @Test
//    public final void testSaveQueryDataWide() {
//        final int   CNT_SOURCES = 100;
//        final long  LNG_DURATION = 10;
//        
//        final DpDataRequest rsqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
//        final DpQueryStreamBuffer   bufResult;
//        
//        try {
//            bufResult = apiQuery.queryStreamBidi(rsqst);
//            
//            Instant insStart = Instant.now();
//            bufResult.start();
//            bufResult.awaitStreamCompleted();
//            Instant insStop = Instant.now();
//            
//            Duration    durQuery = Duration.between(insStart, insStop);
//            Long        szQuery = bufResult.getPageSize() * bufResult.getBufferSize();
//            Long        cntVals = szQuery/Double.BYTES;
//            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
//            
//            System.out.println("Query completed in " + durQuery.toMillis() + " milliseconds.");
//            System.out.println("  Total query size = " + szQuery);
//            System.out.println("  Double value count = " + cntVals);
//            System.out.println("  Transmission rate = " + dblRate);
//            System.out.println(bufResult.toString());
//            
//            if (bufResult.isStreamError())
//                Assert.fail("Stream buffer reported an error - " + bufResult.getStreamError());
//            
//            
////        } catch (DpQueryException e) {
////            Assert.fail("Exception thrown during query: " + e.getMessage());
////            return;
//            
//        } catch (InterruptedException e) {
//            Assert.fail("Process interrupted while waiting for stream completion: " + e.getMessage());
//            return;
//            
//        } catch (TimeoutException e) {
//            Assert.fail("Timeout while waiting for stream completion: " + e.getMessage());
//            return;
//            
//        }
//        
//        // Save the result to an output file
//        String  strFilePath = STR_PATH_QUERY_DATA_RESULTS_WIDE;
//        
//        try {
//            FileOutputStream    fos = new FileOutputStream(strFilePath);
//            ObjectOutputStream  oos = new ObjectOutputStream(fos);
//            
//            oos.writeObject(bufResult.getBuffer());
//            
//            oos.close();
//            fos.close();
//
//        } catch (FileNotFoundException e) {
//            Assert.fail("Unable to create/open output file " + strFilePath);
//            
//        } catch (IOException e) {
//            Assert.fail("Unable to write data to output file " + strFilePath);
//            
//        }
//    }
//
//    /**
//     * Writes the results data from a BIDI stream query to the output file.
//     */
////    @Test
//    public final void testSaveQueryDataLong() {
//        final int   CNT_SOURCES = 10;
//        final long  LNG_DURATION = 60;
//        
//        final DpDataRequest rsqst = TestDpDataRequestGenerator.createRequest(CNT_SOURCES, LNG_DURATION);
//        final DpQueryStreamBuffer   bufResult;
//        
//        try {
//            bufResult = apiQuery.queryStreamBidi(rsqst);
//            
//            Instant insStart = Instant.now();
//            bufResult.start();
//            bufResult.awaitStreamCompleted();
//            Instant insStop = Instant.now();
//            
//            Duration    durQuery = Duration.between(insStart, insStop);
//            Long        szQuery = bufResult.getPageSize() * bufResult.getBufferSize();
//            Long        cntVals = szQuery/Double.BYTES;
//            Double      dblRate = 1000.0 * Math.floorDiv(szQuery, durQuery.toMillis());
//            
//            System.out.println("Query completed in " + durQuery.toMillis() + " milliseconds.");
//            System.out.println("  Total query size = " + szQuery);
//            System.out.println("  Double value count = " + cntVals);
//            System.out.println("  Transmission rate = " + dblRate);
//            System.out.println(bufResult.toString());
//            
//            if (bufResult.isStreamError())
//                Assert.fail("Stream buffer reported an error - " + bufResult.getStreamError());
//            
//            
////        } catch (DpQueryException e) {
////            Assert.fail("Exception thrown during query: " + e.getMessage());
////            return;
//            
//        } catch (InterruptedException e) {
//            Assert.fail("Process interrupted while waiting for stream completion: " + e.getMessage());
//            return;
//            
//        } catch (TimeoutException e) {
//            Assert.fail("Timeout while waiting for stream completion: " + e.getMessage());
//            return;
//            
//        }
//        
//        // Save the result to an output file
//        String  strFilePath = STR_PATH_QUERY_DATA_RESULTS_LONG;
//        
//        try {
//            FileOutputStream    fos = new FileOutputStream(strFilePath);
//            ObjectOutputStream  oos = new ObjectOutputStream(fos);
//            
//            oos.writeObject(bufResult.getBuffer());
//            
//            oos.close();
//            fos.close();
//
//        } catch (FileNotFoundException e) {
//            Assert.fail("Unable to create/open output file " + strFilePath);
//            
//        } catch (IOException e) {
//            Assert.fail("Unable to write data to output file " + strFilePath);
//            
//        }
//    }
}
