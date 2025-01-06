/*
 * Project: dp-api-common
 * File:	QueryResponseCorrelatorTest2.java
 * Package: com.ospreydcs.dp.api.query.model.grpc
 * Type: 	QueryResponseCorrelatorTest2
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
 * @since Jan 4, 2025
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.grpc;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpDataResponseConfig;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannelEvaluatorTest;
import com.ospreydcs.dp.api.model.DpGrpcStreamType;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * JUnit Test Cases for performance testing of <code>QueryResponseCorrelator</code> configurations.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 4, 2025
 *
 */
public class QueryResponseCorrelatorTest2 {
    
    //
    // Application Resources
    //
    
    /** Default configuration for recovering time-series query data */
    public static final DpDataResponseConfig    CFG_RSP = DpApiConfig.getInstance().query.data.response;
    
//    /** Default concurrency configuration for recovering time-series query data */
//    public static final Dp
    
    
    //
    // Class Constants
    //
    
    /** Location of performance results output */
    public static final String          STR_PATH_OUTPUT = "test/output/query/correl/";
    
    
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

    
    /** List of multi-streaming gRPC data streams */
    public static final List<Integer>           LST_CNT_STRS = List.of(1, 2, 3, 4, 5);
    
    /** List of gRPC data stream types used for request recovery */
    public static final List<DpGrpcStreamType>  LST_STR_TYP = List.of(DpGrpcStreamType.BACKWARD, DpGrpcStreamType.BIDIRECTIONAL);
    
    
    //
    // Test Fixture Resources
    //
    
    /** The single connection to the Query Service used by all test cases */
    private static DpQueryConnection    connQuery;
    
    /** Print output stream for storing evaluation results to single file */
    private static PrintStream          psOutput;
    

    //
    // Test Case Resources
    //
    
    /** The QueryResponseCorrelator under test */
    private QueryResponseCorrelator     tstRspCorrelator;
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        // Create the Query Service connection
        connQuery = DpQueryConnectionFactory.FACTORY.connect();
        
        // Open the common output file
        String  strFileName = QueryResponseCorrelatorTest2.class.getSimpleName() + "-" + Instant.now().toString() + ".txt";
        Path    pathOutput = Paths.get(STR_PATH_OUTPUT, strFileName);
        File    fileOutput = pathOutput.toFile();
        
        psOutput = new PrintStream(fileOutput);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        connQuery.shutdownSoft();
        psOutput.close();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.tstRspCorrelator = QueryResponseCorrelator.from(connQuery);
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#from(com.ospreydcs.dp.api.grpc.query.DpQueryConnection)}.
     */
    @Test
    public final void testFrom() {
        QueryResponseCorrelator corTest = QueryResponseCorrelator.from(connQuery);
        
        Assert.assertNotEquals(null, corTest);
        Assert.assertTrue( corTest.geMultiStreamingDomainSize() > 0 );
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#setMultiStreamingResponse(boolean)}.
     */
    @Test
    public final void testSetMultiStreamingResponse() {
        Boolean bolMultiStream = this.tstRspCorrelator.isMultiStreamingResponse();
        Assert.assertEquals(CFG_RSP.multistream.active, bolMultiStream);
        
        this.tstRspCorrelator.setMultiStreamingResponse(!bolMultiStream);
        Assert.assertEquals(!bolMultiStream, this.tstRspCorrelator.isMultiStreamingResponse());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#setMultiStreamingDomainSize(long)}.
     */
    @Test
    public final void testSetMultiStreamingDomainSize() {
        Long    szDomain = this.tstRspCorrelator.geMultiStreamingDomainSize();
        Assert.assertEquals(CFG_RSP.multistream.sizeDomain, szDomain);
        
        szDomain += szDomain;
        this.tstRspCorrelator.setMultiStreamingDomainSize(szDomain);
        Assert.assertEquals(szDomain, Long.valueOf( this.tstRspCorrelator.geMultiStreamingDomainSize() ));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#setMultiStreamCount(int)}.
     */
    @Test
    public final void testSetMultiStreamCount() {
        Integer cntStreams = this.tstRspCorrelator.getMultiStreamCount();
        Assert.assertEquals(CFG_RSP.multistream.maxStreams, cntStreams);
        
        cntStreams = 1;
        this.tstRspCorrelator.setMultiStreamCount(cntStreams);
        Assert.assertEquals(1, this.tstRspCorrelator.getMultiStreamCount());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#setCorrelationConcurrency(boolean)}.
     */
    @Test
    public final void testSetCorrelationConcurrency() {
        Boolean     bolCorrelConc = this.tstRspCorrelator.isCorrelatingConcurrently();
        Assert.assertEquals(CFG_RSP.correlate.useConcurrency, bolCorrelConc);
        
        this.tstRspCorrelator.setCorrelationConcurrency(!bolCorrelConc);
        Assert.assertEquals(!bolCorrelConc, this.tstRspCorrelator.isCorrelatingConcurrently());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator#setCorrelateWhileStreaming(boolean)}.
     */
    @Test
    public final void testSetCorrelateMidstream() {
        Boolean     bolCorrelMidstrm = this.tstRspCorrelator.isCorrelatingWhileStreaming();
        Assert.assertEquals(CFG_RSP.correlate.whileStreaming, bolCorrelMidstrm);
        
        this.tstRspCorrelator.setCorrelateWhileStreaming(!bolCorrelMidstrm);
        Assert.assertEquals(!bolCorrelMidstrm, this.tstRspCorrelator.isCorrelatingWhileStreaming());
    }
    
    
    //
    // Performance Evaluations
    //
    
    /**
     * Test method for {@link QueryResponseCorrelator#processRequestStream(DpDataRequest)}
     */
    @Test
    public final void testProcessRequestStreamDefault() {
        
        // Test Parameters
        PrintStream     os = System.out;
        DpDataRequest   rqst = RQST_HALF_RNG;
        Duration        durQuery;
        
        try {
            durQuery = this.performQuery(rqst, this.tstRspCorrelator);
            
        } catch (DpQueryException e) {
            Assert.fail("Query operation failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return;
        }
        
        os.println(JavaRuntime.getQualifiedMethodName());
        os.println("  Query Duration  : " + durQuery);
        this.printConfiguration(os, rqst);
        this.printConfiguration(os, tstRspCorrelator);
    }
    
    /**
     * Performance tests method for {@link QueryResponseCorrelator#processRequestStream(DpDataRequest)}
     */
    @Test 
    public final void testProcessRequestStreamCases() {
        
        // Test Parameters
        PrintStream             os = psOutput;
        DpDataRequest           rqst = RQST_HALF_SRC;
        List<Integer>           lstStrCnt = LST_CNT_STRS;
        List<DpGrpcStreamType>  lstStrType = LST_STR_TYP;
        QueryResponseCorrelator correl = this.tstRspCorrelator;
        
        try {
            os.println(JavaRuntime.getQualifiedMethodName());
            os.println("CONCURRENCY = " + correl.isCorrelatingConcurrently());
            os.println("WHILE STREAMING = " + correl.isCorrelatingWhileStreaming());
            this.performQueries(os, rqst, correl, lstStrCnt, lstStrType);

            correl.setCorrelateWhileStreaming(false);
            os.println("CONCURRENCY = " + correl.isCorrelatingConcurrently());
            os.println("WHILE STREAMING = " + correl.isCorrelatingWhileStreaming());
            this.performQueries(os, rqst, correl, lstStrCnt, lstStrType);
            
            correl.setCorrelationConcurrency(false);
            os.println("CONCURRENCY = " + correl.isCorrelatingConcurrently());
            os.println("WHILE STREAMING = " + correl.isCorrelatingWhileStreaming());
            this.performQueries(os, rqst, correl, lstStrCnt, lstStrType);
            
            correl.setCorrelateWhileStreaming(true);
            os.println("CONCURRENCY = " + correl.isCorrelatingConcurrently());
            os.println("WHILE STREAMING = " + correl.isCorrelatingWhileStreaming());
            this.performQueries(os, rqst, correl, lstStrCnt, lstStrType);
            
        } catch (DpQueryException e) {
            Assert.fail("Query operation failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return;
        }
    }


    //
    // Support Methods
    //
    
    private void    performQueries(PrintStream os, DpDataRequest rqst, QueryResponseCorrelator corr, List<Integer> lstStrCnt, List<DpGrpcStreamType> lstStrType) throws DpQueryException {
        
        for (DpGrpcStreamType enmType : lstStrType)
            for (Integer cntStrs : lstStrCnt) {
                
                // Configure request
                rqst.setStreamType(enmType);
                
                // Configure correlator
                corr.setMultiStreamCount(cntStrs);
                
                // Perform query
                Duration    durQuery = this.performQuery(rqst, corr);
                
                // Print out results
                os.println("  Query Duration  : " + durQuery);
                this.printConfiguration(os, rqst);
                this.printConfiguration(os, tstRspCorrelator);
                os.println();
            }
    }
    
    private Duration    performQuery(DpDataRequest rqst, QueryResponseCorrelator rspCorrelator) throws DpQueryException {
        
        Instant insStart = Instant.now();
        rspCorrelator.processRequestStream(rqst);
        Instant insFinish = Instant.now();
        
        Duration    durQuery = Duration.between(insStart, insFinish);
        
        return durQuery;
    }
    
    private void    printConfiguration(PrintStream os, DpDataRequest rqst) {
        os.println("  Time-series Data Request");
        os.println("    gRPC stream type   : " + rqst.getStreamType());
        os.println("    data source count  : " + rqst.getSourceCount());
        os.println("    time interval      : " + rqst.range());
        os.println("    domain size        : " + rqst.approxDomainSize());
    }
    
    private void    printConfiguration(PrintStream os, QueryResponseCorrelator corr) {
        
        os.println("  Mutli-streaming Properties");
        os.println("    multi-streaming active : " + corr.isMultiStreamingResponse());
        os.println("    maximum stream count   : " + corr.getMultiStreamCount());
        os.println("    minimum domain size    : " + corr.geMultiStreamingDomainSize());
        os.println("  Correlation Concurrency Properties");
        os.println("    concurrency active   : " + corr.isCorrelatingConcurrently());
        os.println("    while streaming      : " + corr.isCorrelatingWhileStreaming());
    }
}
