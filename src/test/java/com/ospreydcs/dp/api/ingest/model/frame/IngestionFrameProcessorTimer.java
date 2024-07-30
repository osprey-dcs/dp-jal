/*
 * Project: dp-api-common
 * File:	IngestionFrameProcessorTimer.java
 * Package: com.ospreydcs.dp.api.ingest.model.frame
 * Type: 	IngestionFrameProcessorTimer
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

import static org.junit.Assert.*;

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

import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.test.TestIngestionFrameGenerator;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * JUnit test cases for timer the <code>IngestionFrameProcessor</code> operations.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 29, 2024
 *
 */
public class IngestionFrameProcessorTimer {

    
    //
    // Class Constants
    //
    
    /** Default Data Provider UID used for testing */
    public static final ProviderUID     REC_PRV_UID = ProviderUID.from(2);

    
    /** Timeout limit for polling operations */
    public static final long            LNG_TIMEOUT = 20;
    
    /** Timeout unit for polling operations */
    public static final TimeUnit        TU_TIMEOUT = TimeUnit.MILLISECONDS;
    
    
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#submit(com.ospreydcs.dp.api.ingest.IngestionFrame)}.
     */
    @Test
    public final void testSubmitIngestionFrame() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#submit(java.util.List)}.
     */
    @Test
    public final void testSubmitListOfIngestionFrame() {
        
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
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedCallerNameSimple(), "take()", e));
            
        }
        Instant     insFinish = Instant.now();
        
        // Shutdown the processor
        try {
            processor.shutdown();
            
        } catch (InterruptedException e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedCallerNameSimple(), "shutdown()", e));
        }
        Instant insShutdown = Instant.now();

        
        // Compute and report results
        Duration    durPayload = Duration.between(insMark, insStart);
        Duration    durProcessing = Duration.between(insStart, insFinish);
        Duration    durShutdown = Duration.between(insFinish, insShutdown);
        Duration    durTotalMsg = Duration.between(insStart, insShutdown);
        
        System.out.println(JavaRuntime.getQualifiedCallerNameSimple() + " - Configuration");
        System.out.println("  Concurrency         : " + bolConcurrency);
        System.out.println("    Thread count      : " + cntThreads);
        System.out.println("  Frame decomposition : " + bolFrameDecomp);
        System.out.println("    Max frame size    : " + szMaxFrame);
        
        System.out.println(JavaRuntime.getQualifiedCallerNameSimple() + " - Payload (table frames)");
        System.out.println("  Creation time       : " + durPayload);
        System.out.println("  Number of frames    : " + cntFrames);
        System.out.println("    Columns per frame : " + cntCols);
        System.out.println("    Rows per frame    : " + cntRows);
        System.out.println("    Bytes per frame   : " + szFrameAlloc);
        System.out.println("  Payload allocation  : " + szPayloadAlloc);
        
        System.out.println(JavaRuntime.getQualifiedCallerNameSimple() + " - Execution Results");
        System.out.println("  Polling attempts    : " + cntPolls);
        System.out.println("  Messages consumed   : " + cntMsgs);
        System.out.println("  Bytes consumed      : " + cntMsgBytes);
        System.out.println("  Processing time     : " + durProcessing);
        System.out.println("  Shutdown time       : " + durShutdown);
        System.out.println("  Total message time  : " + durTotalMsg);
    }

    
    //
    // Support Methods
    //
    
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
