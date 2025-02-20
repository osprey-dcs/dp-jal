/*
 * Project: dp-api-common
 * File:	QueryChannelTest.java
 * Package: com.ospreydcs.dp.api.query.model.grpc
 * Type: 	QueryChannelTest
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
 * @since Jan 17, 2025
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
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.api.model.IMessageSupplier;
import com.ospreydcs.dp.api.model.ProtoMessageBuffer;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.impl.QueryResponseCorrelatorDeprecated;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.query.model.correl.QueryDataCorrelator;
import com.ospreydcs.dp.api.query.model.request.DataRequestDecomposer;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

/**
 * <p>
 * JUnit tests for class <code>QueryChannel</code>.
 * </p>
 * 
 * <ul>
 * <li>Tests query request data recovery operation.</li>
 * <li>Times recovery operations for many different configurations (see <code>TestCase</code> record).</li>
 * <li>Also times independent correlation of recovered query data (i.e., post correlation).</li>
 * </ul> 
 * 
 * NOTE:
 * <p>
 * This test may take several minutes to complete.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Jan 17, 2025
 *
 */
public class QueryChannelTest {

    
    //
    // Class Types
    //
    
    /**
     * <p>
     * Record containing all the parameters of a time-series data request streaming data recovery.
     * </p>
     * 
     * @param   rqstOrg     the original time-series data request
     * @param   enmDcmpType the request domain decomposition type
     * @param   enmStrType  the gRPC data stream type used to recover the request data
     * @param   cntStrms    the number of gRPC streams used for request data recovery
     * @param   lstCmpRqsts the resulting composite time-series data request (from creator)
     *
     * @author Christopher K. Allen
     * @since Jan 18, 2025
     *
     */
    public static record    TestCase(DpDataRequest rqstOrg, RequestDecompType enmDcmpType, DpGrpcStreamType enmStrmType, int cntStrms, List<DpDataRequest> lstCmpRqsts) {
        
        /** Request decomposer used in non-canonical construction/creation */
        private static final    DataRequestDecomposer   PRCR_DECOMP = DataRequestDecomposer.create();
        
        
        //
        // Creator
        //
        
        /**
         * <p>
         * Creates a new instances of <code>TestCase</code> from the given parameters.
         * </p>
         * 
         * @param   rqstOrg     the original time-series data request
         * @param   enmDcmpType the request domain decomposition type
         * @param   enmStrType  the gRPC data stream type used to recover the request data
         * @param   cntStrms    the number of gRPC streams used for request data recovery
         * @param   lstCmpRqsts    the resulting composite time-series data request
         *
         * @return  a new, initialized instance of <code>TestCase</code>
         */
        public static TestCase  from(DpDataRequest rqstOrg, RequestDecompType enmRqstDcmp, DpGrpcStreamType enmStrmType, int cntStrms) {
        
            List<DpDataRequest> lstRqsts = PRCR_DECOMP.buildCompositeRequest(rqstOrg, enmRqstDcmp, cntStrms);
            
            lstRqsts.forEach(r -> r.setStreamType(enmStrmType));
            
            return new TestCase(rqstOrg, enmRqstDcmp, enmStrmType, cntStrms, lstRqsts);
        }
    }
    
    //
    // Class Constants
    //
    
    /** Location of performance results output */
    public static final String          STR_PATH_OUTPUT = "test/output/query/channel/";
    
    
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

    
    /** List of data requests used for testing */
    public static final List<DpDataRequest>     LST_RQSTS = List.of(RQST_HALF_SRC, RQST_HALF_RNG);
    
    /** List of multi-streaming gRPC data streams */
    public static final List<Integer>           LST_CNT_STRMS = List.of(1, 2, 3, 4, 5);
    
    /** List of gRPC data stream types used for request recovery */
    public static final List<DpGrpcStreamType>  LST_STRM_TYP = List.of(DpGrpcStreamType.BACKWARD, DpGrpcStreamType.BIDIRECTIONAL);

    /** List of request domain decomposition strategies */
    public static final List<RequestDecompType> LST_DCMP_TYP = List.of(RequestDecompType.HORIZONTAL, RequestDecompType.VERTICAL);
    
    
    /** List of all common test case records - built in static section */
    public static final List<TestCase>          LST_TST_CASES = new LinkedList<>();
    
    
    /** Universal enable/disable correlation concurrency flag */
    public static final boolean                 BOL_CORR_CONC = true;
    
    
    /*
     * Static block - Initialize multi-stream test cases
     */
    static {
        
        // Populate the test cases, building the composite request
        for (DpDataRequest rqst : LST_RQSTS)
            for (DpGrpcStreamType enmStrType : LST_STRM_TYP)
                for (RequestDecompType enmDcmpType : LST_DCMP_TYP)
                    for (Integer cntStrms : LST_CNT_STRMS) {
                        
                        TestCase    recCase = TestCase.from(rqst, enmDcmpType, enmStrType, cntStrms);
                        
                        LST_TST_CASES.add(recCase);
                    }
    }
    
    
    //
    // Test Fixture Resources
    //
    
    /** The single connection to the Query Service used by all test cases */
    private static DpQueryConnection    connQuery;
    
    /** The Message Buffer used by the Query Channel */
    private static QueryMessageBuffer   queMsgBuffer;
    
    /** The Query Channel under test */
    private static QueryChannel         chanQuery;
    
    /** The query data correlator */
    private static QueryDataCorrelator  prcrCorrelator;
    
    /** Print output stream for storing evaluation results to single file */
    private static PrintStream          psOutput;
    
    
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
        
        // Create the message buffer
        queMsgBuffer = QueryMessageBuffer.create();
        
        // Create the common Query Service Channel
        chanQuery = QueryChannel.from(connQuery, queMsgBuffer);
        
        // Create the query request data correlator
        prcrCorrelator = QueryDataCorrelator.create();
        
        // Open the common output file
        String  strFileName = QueryChannelTest.class.getSimpleName() + "-" + Instant.now().toString() + ".txt";
        Path    pathOutput = Paths.get(STR_PATH_OUTPUT, strFileName);
        File    fileOutput = pathOutput.toFile();
        
        psOutput = new PrintStream(fileOutput);
        
        // Write header
        DateFormat  date = DateFormat.getDateTimeInstance();
        String      strDateTime = date.format(new Date());
        psOutput.println(QueryChannelTest.class.getSimpleName() + ": " + strDateTime);
        psOutput.println("  Correlate using multiple threads : " + BOL_CORR_CONC);
        psOutput.println();
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryChannel#from(com.ospreydcs.dp.api.grpc.query.DpQueryConnection, com.ospreydcs.dp.api.model.IMessageConsumer)}.
     */
    @Test
    public final void testFrom() {
        QueryMessageBuffer      queMsgBuffer = QueryMessageBuffer.create();
        
        @SuppressWarnings("unused")
        QueryChannel    chan = QueryChannel.from(connQuery, queMsgBuffer);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryChannel#QueryChannel(com.ospreydcs.dp.api.grpc.query.DpQueryConnection, com.ospreydcs.dp.api.model.IMessageConsumer)}.
     */
    @Test
    public final void testQueryChannel() {
        QueryMessageBuffer      queMsgBuffer = QueryMessageBuffer.create();
        
        @SuppressWarnings("unused")
        QueryChannel    chan = new QueryChannel(connQuery, queMsgBuffer);
    }
    
    /**
     * Test the performance of <code>QueryMessageBuffer</code>.
     */
    @Test
    public final void   testQueryMessageBuffer() {
        
        // Test Parameters
        final List<DpDataRequest> lstRqsts = List.of(RQST_HALF_SRC, RQST_HALF_RNG);
        final PrintStream         os = QueryChannelTest.psOutput;
        final boolean             bolCorrCon = BOL_CORR_CONC;
        
        final QueryMessageBuffer  bufMsgs1 = QueryMessageBuffer.create();
        final QueryChannel        chan1 = QueryChannel.from(connQuery, bufMsgs1);
        
        final ProtoMessageBuffer<QueryDataResponse.QueryData>   bufMsgs2 = ProtoMessageBuffer.create();
        final QueryChannel                                      chan2 = QueryChannel.from(connQuery, bufMsgs2);
        
        os.println(JavaRuntime.getQualifiedMethodNameSimple());
        int         indCase = 0;
        int         cntMsgs;
        int         szBuff;
        long        szAlloc;
        double      dblRateXmit; 
        Instant     insStart;
        Instant     insFinish;
        Duration    durQuery;
        try {
            os.println("  QueryMessageBuffer Test");
            for (DpDataRequest rqst : lstRqsts) { 

                bufMsgs1.activate();
                
                insStart = Instant.now();
                cntMsgs = chan1.recoverRequest(rqst);
                insFinish = Instant.now();
                
                durQuery = Duration.between(insStart, insFinish);
                szBuff = bufMsgs1.getQueueSize();
                szAlloc = bufMsgs1.computeQueueAllocation();
                dblRateXmit = ( ((double)szAlloc) * 1000 )/durQuery.toNanos();
                
                bufMsgs1.shutdownNow();
                
                os.println("  Test Case #" + indCase);
                os.println("    Messages streamed        : " + cntMsgs);
                os.println("    Message buffer size      : " + szBuff);
                os.println("    Bytes recovered          : " + szAlloc);
                os.println("    Query duration           : " + durQuery);
                os.println("    Transmission rate (Mbps) : " + dblRateXmit);
                os.println("    Query Request");
                this.printConfiguration(os, "  ", rqst);
                os.println();
                
                indCase++;
            }

            os.println("  ProtoMessageBuffer<QueryData> Test");
            indCase = 0;
            for (DpDataRequest rqst : lstRqsts) { 

                bufMsgs1.activate();
                
                insStart = Instant.now();
                cntMsgs = chan1.recoverRequest(rqst);
                insFinish = Instant.now();
                
                durQuery = Duration.between(insStart, insFinish);
                szBuff = bufMsgs1.getQueueSize();
                szAlloc = bufMsgs1.computeQueueAllocation();
                dblRateXmit = ( ((double)szAlloc) * 1000 )/durQuery.toNanos();
                
                bufMsgs1.shutdownNow();
                
                os.println("  Test Case #" + indCase);
                os.println("    Messages streamed        : " + cntMsgs);
                os.println("    Message buffer size      : " + szBuff);
                os.println("    Bytes recovered          : " + szAlloc);
                os.println("    Query duration           : " + durQuery);
                os.println("    Transmission rate (Mbps) : " + dblRateXmit);
                os.println("    Query Request");
                this.printConfiguration(os, "  ", rqst);
                os.println();
             
                indCase++;
            }
            
        } catch (DpQueryException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown during request #" + indCase + " recovery: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryChannel#recoveryRequestUnary(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testRecoveryRequestUnary() {
        
        //  Test Parameters
        final DpDataRequest       rqstOrg = RQST_HALF_RNG;
        final int                 cntRqsts = 200;
        final TestCase            recCase = TestCase.from(rqstOrg, RequestDecompType.HORIZONTAL, DpGrpcStreamType.BACKWARD, cntRqsts);
        final List<DpDataRequest> lstRqsts = recCase.lstCmpRqsts;
        final PrintStream         os = QueryChannelTest.psOutput;
        final QueryMessageBuffer  bufMsgs = QueryChannelTest.queMsgBuffer;
        final boolean             bolCorrCon = BOL_CORR_CONC;
        
        
        bufMsgs.activate();
        
        // Query original request by composites so they fit in the gRPC message limit
        int         indRqst = 0;
        Instant     insStart = Instant.now();
        for (DpDataRequest rqst : lstRqsts) {
            try {
                QueryChannelTest.chanQuery.recoveryRequestUnary(rqst);

            } catch (DpQueryException e) {
                bufMsgs.shutdownNow();
                Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown during request recovery #" + indRqst + " : " + e.getMessage());
                
            }

            indRqst++;
        }
        Instant     insFinish = Instant.now();

        // Recover and compute results
        Duration    durQueryFull = Duration.between(insStart, insFinish);
        int         cntMsgs = QueryChannelTest.queMsgBuffer.getQueueSize();
        long        szAlloc = QueryChannelTest.queMsgBuffer.computeQueueAllocation();
        double      dblRateXmit = ( ((double)szAlloc) * 1000 )/durQueryFull.toNanos();
        
        // Correlate data
        Duration    durCorrel = this.correlate(bolCorrCon, bufMsgs);
        long        lngCorrel = prcrCorrelator.getBytesProcessed();
        prcrCorrelator.reset();
        
        // Compute results
        Duration    durTotal = durQueryFull.plus(durCorrel);
        double      dblRateCorrel = ( ((double)szAlloc) * 1000 )/durCorrel.toNanos();
        double      dblRateTotal = ( ((double)szAlloc) * 1000 )/durTotal.toNanos();
        
        // Release buffer
        bufMsgs.shutdownNow();
        
        os.println(JavaRuntime.getQualifiedMethodNameSimple());
        os.println("  Composite requests performed : " + indRqst);
        os.println("  Message buffer size      : " + cntMsgs);
        os.println("  Bytes recovered          : " + szAlloc);
        os.println("  Bytes correlated         : " + lngCorrel);
        os.println("  Query duration           : " + durQueryFull);
        os.println("  Correlation duration     : " + durCorrel);
        os.println("  Correlation concurrency  : " + bolCorrCon);
        os.println("  Transmission rate (Mbps) : " + dblRateXmit);
        os.println("  Correlation rate (Mbps)  : " + dblRateCorrel);
        os.println("  Total data rate (Mbps)   : " + dblRateTotal);
        os.println("  Original Request");
        this.printConfiguration(os, "", rqstOrg);
        
        os.println();
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryChannel#recoverRequest(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testRecoverRequestDpDataRequest() {
        
        // Test Parameters
        final List<DpDataRequest> lstRqsts = List.of(RQST_HALF_SRC, RQST_HALF_RNG);
        final QueryMessageBuffer  bufMsgs = QueryChannelTest.queMsgBuffer; // QueryMessageBuffer.create();
        final QueryChannel        chan = QueryChannelTest.chanQuery;     // QueryChannel.from(connQuery, bufMsgs);
        final PrintStream         os = QueryChannelTest.psOutput;
        final boolean             bolCorrCon = BOL_CORR_CONC;
        
        os.println(JavaRuntime.getQualifiedMethodNameSimple());
        os.println();
        
        int     indCase = 0;
        try {
            for (DpDataRequest rqstOrg : lstRqsts) { 
                
                DpDataRequest   rqst = rqstOrg.clone();

                os.println("Test Case #" + indCase);
                rqst.setStreamType(DpGrpcStreamType.BACKWARD);
                this.singleStreamRecovery(os, chan, bufMsgs, rqst, bolCorrCon);
                indCase++;
                
                os.println("Test Case #" + indCase);
                rqst.setStreamType(DpGrpcStreamType.BIDIRECTIONAL);
                this.singleStreamRecovery(os, chan, bufMsgs, rqst, bolCorrCon);
                indCase++;
            }

        } catch (DpQueryException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown during request #" + indCase + " recovery: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.grpc.QueryChannel#recoverRequest(java.util.List)}.
     */
    @Test
    public final void testRecoverRequestListOfDpDataRequest() {
        
        // Test Parameters
        final PrintStream         os = psOutput;
        final QueryMessageBuffer  bufMsgs = queMsgBuffer; // QueryMessageBuffer.create();
        final QueryChannel        chan = chanQuery;     // QueryChannel.from(connQuery, bufMsgs);
        final boolean             bolCorCon = BOL_CORR_CONC;     
        
        os.println(JavaRuntime.getQualifiedMethodNameSimple());
        os.println();
        
        int     indCase = 0;
        for (TestCase recCase : LST_TST_CASES) {
        
            os.println("Test Case #" + indCase);
            try {
                this.multiStreamRecovery(os, chan, bufMsgs, recCase, bolCorCon);
                
            } catch (DpQueryException e) {
                bufMsgs.shutdownNow();
                Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown during request recovery #" + indCase + " : " + e.getMessage());
            }
            
            indCase++;
        }
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Performs the given request on a single gRPC stream using <cod>{@link QueryChannel#recoverRequest(DpDataRequest)</code>
     * and prints results to the given print stream.
     * </p>
     * 
     * @param os        output location of the results
     * @param chan      QueryChannel for the request recovery
     * @param bufMsgs   QueryData message consumer for query channel
     * @param rqst      time-series data request to perform
     * @param bolCorrCon    correlate concurrently enable/disable
     * 
     * @throws DpQueryException general exception during data recovery or processing
     */
    private void    singleStreamRecovery(PrintStream os, QueryChannel chan, QueryMessageBuffer bufMsgs, DpDataRequest rqst, boolean bolCorrCon) throws DpQueryException {
        
//        QueryMessageBuffer    queMsgBuffer = QueryMessageBuffer.create();
//        QueryChannel          chan = QueryChannel.from(connQuery, queMsgBuffer);
        
        queMsgBuffer.activate();
        Instant insStart = Instant.now();
        int cntMsgs = chan.recoverRequest(rqst);
        Instant insFinish = Instant.now();

        // Compute results
        Duration    durQuery = Duration.between(insStart, insFinish);
        int         szBuffer = queMsgBuffer.getQueueSize();
        long        szAlloc = bufMsgs.computeQueueAllocation();
        double      dblRateXmit  = ( ((double)szAlloc) * 1000 )/durQuery.toNanos();

        // Correlate data
        Duration    durCorrel = this.correlate(bolCorrCon, bufMsgs);
        long        szCorrel = prcrCorrelator.getBytesProcessed();
        prcrCorrelator.reset();
        
        // Compute results
        Duration    durTotal = durQuery.plus(durCorrel);
        double      dblRateCorrel = ( ((double)szAlloc) * 1000 )/durCorrel.toNanos();
        double      dblRateTotal = ( ((double)szAlloc) * 1000 )/durTotal.toNanos();
        
        queMsgBuffer.shutdownNow();

        // Print results
        os.println("  Messages streamed        : " + cntMsgs);
        os.println("  Message buffer size      : " + szBuffer);
        os.println("  Bytes recovered          : " + szAlloc);
        os.println("  Bytes correlated         : " + szCorrel);
        os.println("  Query duration           : " + durQuery);
        os.println("  Correlation duration     : " + durCorrel);
        os.println("  Correlation concurrency  : " + bolCorrCon);
        os.println("  Transmission rate (Mbps) : " + dblRateXmit);
        os.println("  Correlation rate (Mbps)  : " + dblRateCorrel);
        os.println("  Total data rate (Mbps)   : " + dblRateTotal);
        this.printConfiguration(os, "", rqst);
        os.println();
    }
    
    /**
     * <p>
     * Performs all the requests in the given test case using the argument resources.
     * </p>
     * 
     * @param os        print stream to store output data
     * @param chan      QueryChannel for the request recovery
     * @param bufMsgs   QueryData message consumer for query channel
     * @param recCase   the test case to perform
     * @param bolCorrCon    correlate concurrently enable/disable
     * 
     * @throws DpQueryException general exception during data recovery or processing
     */
    private void    multiStreamRecovery(PrintStream os, QueryChannel chan, QueryMessageBuffer bufMsgs, TestCase recCase, boolean bolCorrCon) throws DpQueryException {

        // Perform query
        bufMsgs.activate();
        
        Instant     insStart = Instant.now();
        int cntMsgs = chan.recoverRequest(recCase.lstCmpRqsts);   // blocks until all data is recovered
        Instant     insFinish = Instant.now();

        // Compute results
        Duration    durQuery = Duration.between(insStart, insFinish);
        int         szBuffer = bufMsgs.getQueueSize();
        long        szAlloc = bufMsgs.computeQueueAllocation();
        double      dblRateXmit  = ( ((double)szAlloc) * 1000 )/durQuery.toNanos();
        
        // Correlate data
        Duration    durCorrel = this.correlate(bolCorrCon, bufMsgs);
        long        szCorrel = prcrCorrelator.getBytesProcessed();
        prcrCorrelator.reset();
        
        // Compute results
        Duration    durTotal = durQuery.plus(durCorrel);
        double      dblRateCorrel = ( ((double)szAlloc) * 1000 )/durCorrel.toNanos();
        double      dblRateTotal = ( ((double)szAlloc) * 1000 )/durTotal.toNanos();
        
        // Release buffer
        bufMsgs.shutdownNow();

        // Print out results
        os.println("  Messages streamed        : " + cntMsgs);
        os.println("  Message buffer size      : " + szBuffer);
        os.println("  Bytes recovered          : " + szAlloc);
        os.println("  Bytes correlated         : " + szCorrel);
        os.println("  Query duration           : " + durQuery);
        os.println("  Correlation duration     : " + durCorrel);
        os.println("  Correlation concurrency  : " + bolCorrCon);
        os.println("  Transmission rate (Mbps) : " + dblRateXmit);
        os.println("  Correlation rate (Mbps)  : " + dblRateCorrel);
        os.println("  Total data rate (Mbps)   : " + dblRateTotal);
        this.printConfiguration(os, recCase);
        os.println();
    }
    
    /**
     * <p>
     * Correlates all data within the given message buffer and returns the time required.
     * </p>
     * <p>
     * Upon returning all data within the given buffer is correlated and the buffer is exhausted.
     * Nothing is done further with the correlated data but it is available within the class
     * resource <code>{@link #prcrCorrelator}</code>
     * </p>
     * 
     * @param bolConcurrency    use concurrency when correlating
     * @param bufMsgs           queue buffer containing query response
     * 
     * @return  the time taken to perform correlation of the given response data
     */
    private Duration    correlate(boolean bolConcurrency, QueryMessageBuffer bufMsgs) {
        
        QueryChannelTest.prcrCorrelator.reset();
        QueryChannelTest.prcrCorrelator.setConcurrency(bolConcurrency);
        
        Instant insStart = Instant.now();
        try {
            while (bufMsgs.getQueueSize() > 0) {

                QueryDataResponse.QueryData msgData = bufMsgs.take();
                
                QueryChannelTest.prcrCorrelator.addQueryData(msgData);
            }

        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown during request correlation: " + e.getMessage());
        }
        Instant insFinish = Instant.now();
        
        Duration    durCorrel = Duration.between(insStart, insFinish);
        
        return durCorrel;
    }
    
    /**
     * <p>
     * Prints out the configuration of the given test case record to the given print stream.
     * </p>
     * 
     * @param os        print stream receiving output data (i.e., the configuration)
     * @param recCase   the test case whose configuration is to be output 
     */
    private void    printConfiguration(PrintStream os, TestCase recCase) {
        
        DpDataRequest   rqst = recCase.rqstOrg;
        
        os.println("  Original Time-series Data Request");
        this.printConfiguration(os, "", rqst);
        
        os.println("  Query Recovery Parameters");
        os.println("    Request decomposition type : " + recCase.enmDcmpType);
        os.println("    gRPC stream type           : " + recCase.enmStrmType);
        os.println("    Number of gRPC streams     : " + recCase.cntStrms);
        
        int     indRqst = 0;
        for (DpDataRequest rqstCmp : recCase.lstCmpRqsts) {
            os.println("    Composite Request #" + indRqst);
            this.printConfiguration(os, "  ", rqstCmp);
            indRqst++;
        }
    }
    
    /**
     * <p>
     * Prints out the configuration of the given data request to the given print stream.
     * </p>
     * 
     * @param os        print stream receiving output data (i.e., the configuration)
     * @param strPad    padding string for parameters
     * @param rqst      the data request whose configuration is to be output 
     */
    private void    printConfiguration(PrintStream os, String strPad, DpDataRequest rqst) {
        
        os.println(strPad + "    gRPC stream type   : " + rqst.getStreamType());
        os.println(strPad + "    data source count  : " + rqst.getSourceCount());
        os.println(strPad + "    duration (seconds) : " + rqst.rangeDuration().toSeconds());
        os.println(strPad + "    time interval      : " + rqst.range());
        os.println(strPad + "    domain size        : " + rqst.approxDomainSize());
    }

}
