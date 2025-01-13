/*
 * Project: dp-api-common
 * File:	IngestionFrameProcessorEvaluatorDep.java
 * Package: com.ospreydcs.dp.api.ingest.model.frame
 * Type: 	IngestionFrameProcessorEvaluatorDep
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
 * @since Jul 29, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.frame;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.ProviderUID;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.test.TestIngestionFrameGenerator;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * JUnit test cases for evaluating the <code>IngestionFrameProcessor</code> performance and operations.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 29, 2024
 * 
 * @deprecated Replaced by IngestionFrameProcessorEvaluatorDep
 */
@Deprecated(since="Aug 1, 2024", forRemoval=true)
public class IngestionFrameProcessorEvaluatorDep {

    
    //
    // Class Constants
    //
    
    /** Default Data Provider UID used for testing */
    public static final ProviderUID     REC_PRV_UID = ProviderUID.from("42", IngestionFrameProcessorEvaluatorDep.class.getSimpleName(), false);

    
    /** Timeout limit for polling operations */
    public static final long            LNG_TIMEOUT = 20;
    
    /** Timeout unit for polling operations */
    public static final TimeUnit        TU_TIMEOUT = TimeUnit.MILLISECONDS;
    
    
    //
    // Test Configurations
    //
    
    /** Standard Payload: frame count */
    public static final int     CNT_FRAMES = 15;
    
    /** Standard Payload: number of table columns */
    public static final int     CNT_COLS = 1000;
    
    /** Standard Payload: number of table rows */
    public static final int     CNT_ROWS = 1000;
    
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_FALSE_DECMP_100K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, false, 1, true, 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_FALSE_DECMP_200K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, false, 1, true, 2 * 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_FALSE_DECMP_400K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, false, 1, true, 4 * 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);

    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_FALSE_DECMP_1M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, false, 1, true, 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_FALSE_DECMP_2M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, false, 1, true, 2 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_FALSE_DECMP_4M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, false, 1, true, 4 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);

    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_1_DECMP_1M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 1, true, 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_2_DECMP_1M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 2, true, 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_3_DECMP_1M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 3, true, 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_1_DECMP_2M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 1, true, 2 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_2_DECMP_2M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 2, true, 2 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_3_DECMP_2M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 3, true, 2 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_1_DECMP_4M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 1, true, 4 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_2_DECMP_4M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 2, true, 4 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_3_DECMP_4M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 3, true, 4 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_1_DECMP_400K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 1, true, 4 * 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_2_DECMP_400K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 2, true, 4 * 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_3_DECMP_400K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 3, true, 4 * 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);
    
    
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#submit(java.util.List)}.
     */
    @Test 
    public final void testSubmitListOfIngestionFramePolling() {
        
        // Payload Parameters
        final int       cntFrames = 15;
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        
        // Multi-Threading Parameters
        final boolean   bolConcurrency = true;
//        final boolean   bolConcurrency = false;
        final int       cntThreads = 3;
        
        // Frame Decomposition Parameters
        final boolean   bolFrameDecomp = true;
        final long      szMaxFrame = 4 * 1_000_000;
        
        // Create processor and configure
        IngestionFrameProcessor processor = new IngestionFrameProcessor(REC_PRV_UID);
        
        if (bolConcurrency) {
            processor.setConcurrency(cntThreads);
            
        } else {
            processor.disableConcurrency();
        }
        
        if (bolFrameDecomp) {
            processor.setFrameDecomposition(szMaxFrame);
            
        } else {
            processor.disableFrameDecomposition();
        }
        
        // Create payload and compute payload parameters
        Instant                 insMark = Instant.now();
        
        List<IngestionFrame>    lstFrames = TestIngestionFrameGenerator.createDoublesPayloadWithClock(cntFrames, cntCols, cntRows);
        long                    szFrameAlloc = lstFrames.get(0).allocationSizeFrame();
        long                    szPayloadAlloc = lstFrames.stream().mapToLong(f -> f.allocationSizeFrame()).sum();
        
        
        // Begin processing
        processor.activate();
        
        int         cntMsgs = 0;
        int         cntPolls = 0;
        long        cntMsgBytes = 0;
        Instant     insStart = Instant.now();

        processor.submit(lstFrames);
        
        try {
            // Consume all resulting ingest data requests 
            while (processor.hasNext()) {
                
                IngestDataRequest   msgRqst = processor.poll(LNG_TIMEOUT, TU_TIMEOUT);
                
                cntPolls++;
                
                if (msgRqst != null) {
                    cntMsgBytes += msgRqst.getSerializedSize();
                    cntMsgs++;
                }
            }
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "take()", e));
            
        }
        Instant     insFinish = Instant.now();
        
        // Shutdown the processor
        try {
            processor.shutdown();
            
        } catch (InterruptedException e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "shutdown()", e));
        }
        Instant insShutdown = Instant.now();

        
        // Compute and report results
        Duration    durPayload = Duration.between(insMark, insStart);
        Duration    durProcessing = Duration.between(insStart, insFinish);
        Duration    durShutdown = Duration.between(insFinish, insShutdown);
        Duration    durActive = Duration.between(insStart, insShutdown);
        
        double      dblDurProcessing  = ((double)durProcessing.toNanos()) / 1.0e9;
        double      dblDurActive      = ((double)durActive.toNanos()) / 1.0e9;
        double      dblRatePrcPayload = ((double)szPayloadAlloc) / dblDurProcessing;
        double      dblRatePrcMessage = ((double)cntMsgBytes) / dblDurProcessing;
        double      dblRateActPayload = ((double)szPayloadAlloc) / dblDurActive;
        double      dblRateActMessage = ((double)cntMsgBytes) / dblDurActive;
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Configuration");
        System.out.println("  Concurrency         : " + bolConcurrency);
        System.out.println("    Thread count      : " + cntThreads);
        System.out.println("  Frame decomposition : " + bolFrameDecomp);
        System.out.println("    Max frame size    : " + szMaxFrame);
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Payload (table frames)");
        System.out.println("  Creation time       : " + durPayload);
        System.out.println("  Number of frames    : " + cntFrames);
        System.out.println("    Columns per frame : " + cntCols);
        System.out.println("    Rows per frame    : " + cntRows);
        System.out.println("    Bytes per frame   : " + szFrameAlloc);
        System.out.println("  Payload allocation  : " + szPayloadAlloc);
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Activity Results");
        System.out.println("  Polling attempts    : " + cntPolls);
        System.out.println("  Messages consumed   : " + cntMsgs);
        System.out.println("  Bytes consumed      : " + cntMsgBytes);
        System.out.println("  Processing time     : " + durProcessing);
        System.out.println("  Shutdown time       : " + durShutdown);
        System.out.println("  Total message time  : " + durActive);
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Processing Results");
        System.out.println("  Processing Rate with shutdown (Mbps)");
        System.out.println("    Payload (frontend): " + dblRateActPayload/1_000_000);
        System.out.println("    Message  (backend): " + dblRateActMessage/1_000_000);
        System.out.println("  Processing Rate without shutdown (Mbps)");
        System.out.println("    Payload (frontend): " + dblRatePrcPayload/1_000_000);
        System.out.println("    Message  (backend): " + dblRatePrcMessage/1_000_000);
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#submit(java.util.List)}.
     */
//    @Test 
    public final void testSubmitListOfIngestionFrameTake() {
        
        // Create Test Configuration and populate
//        IngestionFrameProcessorEvalConfig      config = new IngestionFrameProcessorEvalConfig();
        IngestionFrameProcessorEvalConfig      config = CFG_MTHRD_3_DECMP_4M;
        
//        // Payload Configuration
//        config.cntFrames = 15;
//        config.cntCols = 1000;
//        config.cntRows = 1000;
//        
//        // Processor Configuration
//        config.bolConcurrency = true;
//        config.cntThreads = 3;
//        config.bolFrameDecomp = true;
//        config.szMaxFrame = 4 * 1_000_000L;
        
        // Create test results record and mark 
        IngestionFrameProcessorEvaluator     results = new IngestionFrameProcessorEvaluator();
        results.start();
        
        // Create payload and record payload parameters
        List<IngestionFrame>    lstFrames = config.createPayload();
        results.setPayloadParameters(lstFrames);
        
        // Create processor 
        IngestionFrameProcessor processor = results.createProcessor(config);
        
        
        // Activate processor
        results.activate();
        processor.activate();
        
//        // Processing results
//        int                         cntPolls = 0;
//        List<IngestDataRequest>     lstMsgs = new LinkedList<>();

        // Begin processing
        results.submit();
        processor.submit(lstFrames);
        
        try {
            // Consume all resulting ingest data requests 
            while (processor.hasNext()) {
                
                IngestDataRequest   msgRqst = processor.poll(LNG_TIMEOUT, TU_TIMEOUT);

                results.registerMessage(msgRqst);
//                cntPolls++;
//                
//                if (msgRqst != null) {
//                    lstMsgs.add(msgRqst);
//                }
            }
            results.finish();
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "take()", e));
        }
        
        // Shutdown the processor
        try {
            processor.shutdown();
            results.shutdown();
            
        } catch (InterruptedException e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "shutdown()", e));
        }
        
        // Record processing results
//        results.setMessagePollingCount(cntPolls);
//        results.setMessageParameters(lstMsgs);
        results.computeResults();

        // Print out test results to standard output
        results.printAll(System.out, JavaRuntime.getQualifiedMethodNameSimple());
    }    

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Prints out the configuration of the given <code>IngestionFrameProcessor</code> to the given <code>PrintStream</code>.
     * </p>
     * <p>
     * Prints out a textual description of the ingestion frame processor instance in its current configuration.
     * </p>
     * 
     * @param os        print stream (output stream) to receive text output description
     * @param processor target <code>IngestionFrameProcessor</code> 
     * @param strHdr    optional header line to be included in description (if non-<code>null</code>)
     */
    private void printProcessorConfig(PrintStream os, IngestionFrameProcessor processor, String strHdr) {
        
        if (strHdr != null)
            os.println(strHdr);
        os.println("  Concurrency         : " + processor.hasConcurrency());
        os.println("    Thread count      : " + processor.getConcurrencyCount());
        os.println("  Frame decomposition : " + processor.hasFrameDecomposition());
        os.println("    Max frame size    : " + processor.getMaxFrameSize());
        
    }
    
    /**
     * <p>
     * Prints out the payload configuration (i.e., the ingestion frame list) to the given <code>PrintStream</code>.
     * </p>
     * <p>
     * Prints out a textual description of the given <code>List&lt;IngestionFrame&gt;</code> instance.
     * </p> 
     *   
     * @param os        print stream (output stream) to receive text output description
     * @param payload   target payload to be described
     * @param strHdr    optional header line to be included in description (if non-<code>null</code>)
     */
    private void printPayloadConfig(PrintStream os, List<IngestionFrame> payload, String strHdr) {

        // Get example frame
        IngestionFrame  frame = payload.get(0);
        
        // Compute parameters
        int     cntFrames = payload.size();
        int     cntCols = frame.getColumnCount();
        int     cntRows = frame.getRowCount();
        long    szFrameAlloc = frame.allocationSizeFrame();
        long    szPayloadAlloc = payload.stream().mapToLong(f -> f.allocationSizeFrame()).sum();

        
        if (strHdr != null)
            os.println(strHdr);
//        os.println("  Creation time       : " + durPayload);
        os.println("  Payload - IngestionFrame instances ");
        os.println("    Number of frames      : " + cntFrames);
        os.println("    Columns per frame     : " + cntCols);
        os.println("    Rows per frame        : " + cntRows);
        os.println("    Allocation (bytes)    : " + szFrameAlloc);
        os.println("  Total allocation (bytes): " + szPayloadAlloc);
    }
    
    /**
     * <p>
     * Prints out a textual description of the activity for the given test results.
     * </p>
     * <p>
     * Prints out the test parameters and the time durations for each activity.
     * </p>
     * 
     * @param os        print stream (output stream) to receive text output description
     * @param results   the test results record
     * @param strHdr    optional header line to be included in description if (non-<code>null</code>)
     */
    private void    printTestActivity(PrintStream os, IngestionFrameProcessorEvaluator results, String strHdr) {
        
        if (strHdr != null)
            os.println(strHdr);
        os.println("  Polling attempts      : " + results.cntMessagePolls);
        os.println("  Messages produced     : " + results.cntProducedMessages);
        os.println("  Total bytes produced  : " + results.szProducedTotalBytes);
        os.println("  Time processing       : " + results.getDurationProcessing());
        os.println("  Time for shutdown     : " + results.getDurationShutdown());
        os.println("  Time processor active : " + results.getDurationActive());
        os.println("  Tie for total test    : " + results.getDurationTotal());
    }
    
    /**
     * <p>
     * Prints out a textual description of the results within the given test result record.
     * </p>
     * 
     * @param os        print stream (output stream) to receive text output description
     * @param results   the test results record
     * @param strHdr    optional header line to be included in description if (non-<code>null</code>)
     */
    private void    printTestResults(PrintStream os, IngestionFrameProcessorEvaluator results, String strHdr) {
        
        if (strHdr != null)
            os.println(strHdr);
        System.out.println("  Full Processing Rate (with activation and shutdown)");
        System.out.println("    Payload (frontend): " + results.getPayloadProcessingRateFullMbps() + " Mbps");
        System.out.println("    Message  (backend): " + results.getMessageProcessingRateFullMbps() + " Mbps");
        System.out.println("  Raw Processing Rate (without activation and shutdown (Mbps)");
        System.out.println("    Payload (frontend): " + results.getPayloadProcessingRateRawMbps() + " Mbps");
        System.out.println("    Message  (backend): " + results.getMessageProcessingRateRawMbps() + " Mbps");
    }
    
    /**
     * <p>
     * Creates a failure message from the given arguments.
     * </p>
     * <p>
     * The strings are concatenated with a failure notification along with the type of the exception and its
     * detail message.
     * </p>
     * 
     * @param strTest       name of the test case
     * @param strMethod     name of the method under test
     * @param e             exception thrown
     * 
     * @return              failure message
     */
    private String  createFailExceptionMessage(String strTest, String strMethod, Exception e) {
        
        String buf = strTest + " - " + strMethod + " failed with exception " 
                            + e.getClass().getName() 
                            + ": " + e.getMessage();
        
        return buf;
    }
}
