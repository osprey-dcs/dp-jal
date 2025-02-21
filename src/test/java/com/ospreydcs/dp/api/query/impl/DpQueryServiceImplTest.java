/*
 * Project: dp-api-common
 * File:	DpQueryServiceImplTest.java
 * Package: com.ospreydcs.dp.api.query.impl
 * Type: 	DpQueryServiceImplTest
 *
 * Copyright 2010-2025 the original author or authors.
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
 * @since Feb 19, 2025
 *
 */
package com.ospreydcs.dp.api.query.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.IDataTable;
import com.ospreydcs.dp.api.common.MetadataRecord;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpMetadataRequest;
import com.ospreydcs.dp.api.query.DpQueryApiFactory;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.IQueryService;
import com.ospreydcs.dp.api.query.model.request.DataRequestDecomposer;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.JavaSize;

/**
 * <p>
 * JUnit test cases for class <code>DpQueryServiceImpl</code>.
 * </p>
 * <p>
 * <h2>WARNING:</h2>
 * For test cases to pass the following conditions must be current:
 * <ul>
 * <li>
 * A Query Service must be running at the default port location and with default parameter set.
 * </li>
 * <li>
 * The Data Platform archive must be populated with test data from the <em>app-run-test-data-generator</em>
 * application within the Data Platform installer.
 * </li>
 * </ul>
 * <p>
 *
 * @author Christopher K. Allen
 * @since Feb 19, 2025
 *
 */
public class DpQueryServiceImplTest {

    
    //
    // Application Resources
    //
    
    /** The default Query Service configuration parameters */
    public static final DpQueryConfig          CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** General timeout limit */
    public static final long        LNG_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** General timeout units */
    public static final TimeUnit    TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
    /** Location of performance results output */
    public static final String          STR_PATH_OUTPUT = "test/output/query/impl/";
    
    
    /** "Small" test request  */
    public static final DpDataRequest   RQST_SMALL = TestQueryResponses.QREC_LONG.createRequest();
    
    /** Wide test request (500 PVs, 5 secs) */
    public static final DpDataRequest   RQST_WIDE = TestQueryResponses.QREC_WIDE.createRequest();
    
    /** Large test request */
    public static final DpDataRequest   RQST_BIG = TestQueryResponses.QREC_BIG.createRequest();
    
    /** Huge test request */
    public static final DpDataRequest   RQST_HUGE = TestQueryResponses.QREC_HUGE.createRequest();
    
    /** The data request for the half the test archive - used for timing responses */
    public static final DpDataRequest   RQST_HALF_SRC = TestQueryResponses.QREC_HALF_SRC.createRequest();

    /** The data request for the half the test archive - used for timing responses */
    public static final DpDataRequest   RQST_HALF_RNG = TestQueryResponses.QREC_HALF_RNG.createRequest();

    /** The data request for the entire test archive - used for timing responses */
    public static final DpDataRequest   RQST_ALL = TestQueryResponses.QREC_ALL.createRequest();

    
    /** Common List of all PV names in the test archive */
    public static final List<String>                LST_PV_NAMES = RQST_HALF_SRC.getSourceNames();
    
    /** Common List of data requests used for testing */
    public static final List<DpDataRequest>         LST_RQSTS = List.of(RQST_HALF_SRC, RQST_HALF_RNG);
    
    /** A map of the common test requests to their (arbitrary) names */
    public static final Map<DpDataRequest, String>  MAP_RQST_NMS = Map.of(RQST_HALF_SRC, "RQST_HALF_SRC", RQST_HALF_RNG, "RQST_HALF_RNG");
    
    
    //
    // Test Fixture Resources
    //
    
    /** The common Query Service API used for query test cases */
    private static IQueryService            apiTest;
    
    /** Print output stream for storing evaluation results to single file */
    private static PrintStream              psOutput;
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // Create the Query Service connection
        apiTest = DpQueryApiFactory.connectService();
        
        // Open the common output file
        String  strFileName = DpQueryServiceImplTest.class.getSimpleName() + "-" + Instant.now().toString() + ".txt";
        Path    pathOutput = Paths.get(STR_PATH_OUTPUT, strFileName);
        File    fileOutput = pathOutput.toFile();
        
        psOutput = new PrintStream(fileOutput);
        
        // Write header
        DateFormat  date = DateFormat.getDateTimeInstance();
        String      strDateTime = date.format(new Date());
        psOutput.println(DpQueryServiceImplTest.class.getSimpleName() + ": " + strDateTime);
        psOutput.println();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() {
        try {
            apiTest.shutdown();
            
        } catch (InterruptedException e) {
            String strMsg = "WARNING: Query Service API interrupted during shut down: " + e.getMessage();
            
            System.err.println(strMsg);
            psOutput.println();
            psOutput.println(strMsg);
        }
        psOutput.close();
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
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#shutdown()}.
     */
    @Test
    public final void testShutdown() {
        try {
            IQueryService   apiQuery = DpQueryApiFactory.connectService();
            
            Assert.assertFalse(apiQuery.isShutdown());
            
            boolean bolShutdown = apiQuery.shutdown();
            boolean bolIsShutdown = apiQuery.isShutdown();
            
            Assert.assertTrue(bolShutdown);
            Assert.assertTrue(bolIsShutdown);
            
        } catch (DpGrpcException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Unable to create interface: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during shut down: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#shutdownNow()}.
     */
    @Test
    public final void testShutdownNow() {
        try {
            IQueryService   apiQuery = DpQueryApiFactory.connectService();
            
            Assert.assertFalse(apiQuery.isShutdown());
            
            boolean bolShutdown = apiQuery.shutdownNow();
            boolean bolIsShutdown = apiQuery.isShutdown();
            boolean bolIsTerminated = apiQuery.isTerminated();
            
            Assert.assertTrue(bolShutdown);
            Assert.assertTrue(bolIsShutdown);
            Assert.assertTrue(bolIsTerminated);
            
            apiQuery.awaitTermination(LNG_TIMEOUT, TU_TIMEOUT);
            
            Assert.assertTrue(apiQuery.isShutdown());
            Assert.assertTrue(apiQuery.isTerminated());
            
        } catch (DpGrpcException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Unable to create interface: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during hard shut down: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#awaitTermination(long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testAwaitTerminationLongTimeUnit() {
        try {
            IQueryService   apiQuery = DpQueryApiFactory.connectService();
            
            Assert.assertFalse(apiQuery.isShutdown());
            
            boolean bolShutdown = apiQuery.shutdown();
            boolean bolIsTerminated = apiQuery.isTerminated();
            boolean bolIsShutdown = apiQuery.isShutdown();
            
            Assert.assertTrue(bolShutdown);
            Assert.assertTrue(bolIsShutdown);
//            Assert.assertFalse(bolIsTerminated);
            
            apiQuery.awaitTermination(LNG_TIMEOUT, TU_TIMEOUT);
            
            Assert.assertTrue(apiQuery.isTerminated());
            
            psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
            psOutput.println("  isTerminated() before wait = " + bolIsTerminated);
            psOutput.println("  isTerminated() after wait  = " + apiQuery.isTerminated());
            
        } catch (DpGrpcException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Unable to create interface: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during shut down: " + e.getMessage());
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#isShutdown()}.
//     */
//    @Test
//    public final void testIsShutdown() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#isTerminated()}.
//     */
//    @Test
//    public final void testIsTerminated() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#from(com.ospreydcs.dp.api.grpc.query.DpQueryConnection)}.
     */
    @Test
    public final void testFrom() {
        DpQueryConnection connQuery;
        try {
            connQuery = DpQueryConnectionFactory.FACTORY.connect();
            
        } catch (DpGrpcException e) {
            Assert.fail("Unable to obtain Query Service connection, cannot continue: " + e.getMessage());
            return;
        }
        
        DpQueryServiceImpl apiQuery = DpQueryServiceImpl.from(connQuery);
        try {
            apiQuery.shutdown();
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during shut down: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#DpQueryServiceImpl(com.ospreydcs.dp.api.grpc.query.DpQueryConnection)}.
     */
    @Test
    public final void testDpQueryServiceImpl() {
        
        try {
            DpQueryConnection   connQuery = DpQueryConnectionFactory.FACTORY.connect();
            
            DpQueryServiceImpl  apiQuery = new DpQueryServiceImpl(connQuery);
            
            apiQuery.shutdown();
            
        } catch (DpGrpcException e) {
            Assert.fail("Unable to obtain Query Service connection, cannot continue: " + e.getMessage());
            return;
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during shut down: " + e.getMessage());
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#awaitTermination()}.
//     */
//    @Test
//    public final void testAwaitTermination() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryMeta(com.ospreydcs.dp.api.query.DpMetadataRequest)}.
     */
    @Test
    public final void testQueryMeta() {
        
        // Test Parameters
        final int           cntPvs = 10;
        final List<String>  strPvNames = LST_PV_NAMES.subList(0, cntPvs);
        
        DpMetadataRequest rqst = DpMetadataRequest.newRequest();
        rqst.selectPvs(strPvNames);
        
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        try {
            List<MetadataRecord>    lstRcrds = apiTest.queryMeta(rqst);
            
            Assert.assertEquals(cntPvs, lstRcrds.size());
            
            for (MetadataRecord rcrd : lstRcrds) 
                psOutput.println("  " + rcrd.toString());
            psOutput.println();
            
        } catch (DpQueryException e) {
            String  strMsg = "Metadata request exception " + e.getClass().getSimpleName() + ": " + e.getMessage();
            
            psOutput.println(strMsg);
            psOutput.println();
            Assert.fail(strMsg);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryDataUnary(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testQueryDataUnary() {
        
        // Test Parameters
        final DpDataRequest rqst = RQST_SMALL;
        
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        try {
            Instant     insStart = Instant.now();
            IDataTable  table = apiTest.queryDataUnary(rqst);
            Instant     insFinish = Instant.now();
            
            long        szRqst = rqst.approxDomainSize() * 1_000L * JavaSize.SZ_Double;
            long        szTbl = table.allocationSize();
            Duration    durRqst = Duration.between(insStart, insFinish);
            double      dblRate = ((double)szTbl*1_000L)/((double)durRqst.toNanos());
            
            psOutput.println("  Request PV count      : " + rqst.getSourceCount());
            psOutput.println("  Request time duration : " + rqst.rangeDuration());
            psOutput.println("  Approx. request size  : " + szRqst);
            psOutput.println("  Table column count    : " + table.getColumnCount());
            psOutput.println("  Table time duration   : " + table.getDuration());
            psOutput.println("  Table byte allocation : " + szTbl);
            psOutput.println("  Request duration      : " + durRqst);
            psOutput.println("  Data rate (Mbps)      : " + dblRate);
            psOutput.println();
            
            Assert.assertEquals(rqst.getSourceCount(), (int)table.getColumnCount());
            Assert.assertEquals(rqst.rangeDuration(), table.getDuration());
            
        } catch (DpQueryException e) {
            String  strMsg = "Unary time-series request exception " + e.getClass().getSimpleName() + ": " + e.getMessage();
            
            psOutput.println(strMsg);
            psOutput.println();
            Assert.fail(strMsg);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryData(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testQueryDataDpDataRequest() {
        
        // Test Parameters
        final DpDataRequest rqst = RQST_HALF_SRC;
        
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        try {
            Instant     insStart = Instant.now();
            IDataTable  table = apiTest.queryData(rqst);
            Instant     insFinish = Instant.now();
            
            long        szRqst = rqst.approxDomainSize() * 1_000L * JavaSize.SZ_Double;
            long        szTbl = table.allocationSize();
            Duration    durRqst = Duration.between(insStart, insFinish);
            double      dblRate = ((double)szTbl*1_000L)/((double)durRqst.toNanos());
            
            psOutput.println("  Request PV count      : " + rqst.getSourceCount());
            psOutput.println("  Request time duration : " + rqst.rangeDuration());
            psOutput.println("  Approx. request size  : " + szRqst);
            psOutput.println("  Table column count    : " + table.getColumnCount());
            psOutput.println("  Table time duration   : " + table.getDuration());
            psOutput.println("  Table byte allocation : " + szTbl);
            psOutput.println("  Request duration      : " + durRqst);
            psOutput.println("  Data rate (Mbps)      : " + dblRate);
            psOutput.println();
            
            Assert.assertEquals(rqst.getSourceCount(), (int)table.getColumnCount());
            Assert.assertEquals(rqst.rangeDuration(), table.getDuration());
            
        } catch (DpQueryException e) {
            String  strMsg = "Streaming time-series request exception " + e.getClass().getSimpleName() + ": " + e.getMessage();
            
            psOutput.println(strMsg);
            psOutput.println();
            Assert.fail(strMsg);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryData(java.util.List)}.
     */
    @Test
    public final void testQueryDataListOfDpDataRequest() {

        // Test Resources
        DataRequestDecomposer   prcrDcmp = DataRequestDecomposer.create();
        
        // Test Parameters
        final int                   cntRqsts = 4;
        final RequestDecompType     enmDcmpType = RequestDecompType.HORIZONTAL;
        final DpDataRequest         rqst = RQST_HALF_SRC;
        final List<DpDataRequest>   lstRqsts = prcrDcmp.buildCompositeRequest(rqst, enmDcmpType, cntRqsts);
        
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        try {
            Instant     insStart = Instant.now();
            IDataTable  table = apiTest.queryData(lstRqsts);
            Instant     insFinish = Instant.now();
            
            long        szRqst = rqst.approxDomainSize() * 1_000L * JavaSize.SZ_Double;
            long        szTbl = table.allocationSize();
            Duration    durRqst = Duration.between(insStart, insFinish);
            double      dblRate = ((double)szTbl*1_000L)/((double)durRqst.toNanos());
            
            psOutput.println("  Request PV count      : " + rqst.getSourceCount());
            psOutput.println("  Request time duration : " + rqst.rangeDuration());
            psOutput.println("  Approx. request size  : " + szRqst);
            psOutput.println("  Table column count    : " + table.getColumnCount());
            psOutput.println("  Table time duration   : " + table.getDuration());
            psOutput.println("  Table byte allocation : " + szTbl);
            psOutput.println("  Request duration      : " + durRqst);
            psOutput.println("  Data rate (Mbps)      : " + dblRate);
            psOutput.println();
            
            Assert.assertEquals(rqst.getSourceCount(), (int)table.getColumnCount());
            Assert.assertEquals(rqst.rangeDuration(), table.getDuration());
            
        } catch (DpQueryException e) {
            String  strMsg = "Streaming time-series request exception " + e.getClass().getSimpleName() + ": " + e.getMessage();
            
            psOutput.println(strMsg);
            psOutput.println();
            Assert.fail(strMsg);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryDataStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testQueryDataStream() {
        fail("Not yet implemented"); // TODO
    }

}
