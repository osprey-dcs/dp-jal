/*
 * Project: dp-api-common
 * File:	IngestionFrameProcessorTest.java
 * Package: com.ospreydcs.dp.api.ingest.model.frame
 * Type: 	IngestionFrameProcessorTest
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

import org.junit.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
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
 * JUnit test cases for class <code>{@link IngestionFrameProcessor}</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 29, 2024
 *
 */
public class IngestionFrameProcessorTest {
    
    
    //
    // Class Constants
    //
    
    /** Default Data Provider UID used for testing */
    public static final ProviderUID     REC_PRV_UID = ProviderUID.from("42", IngestionFrameProcessorTest.class.getSimpleName(), false);

    
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#IngestionFrameProcessor()}.
     */
    @Test
    public final void testIngestionFrameProcessor() {
        
        IngestionFrameProcessor processor = new IngestionFrameProcessor();
        
        Assert.assertNotEquals(null, processor);
        Assert.assertEquals(null, processor.getProviderUid());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#IngestionFrameProcessor(com.ospreydcs.dp.api.model.ProviderUID)}.
     */
    @Test
    public final void testIngestionFrameProcessorProviderUID() {
        
        IngestionFrameProcessor processor = new IngestionFrameProcessor(REC_PRV_UID);
        
        Assert.assertNotEquals(null, processor);
        Assert.assertEquals(REC_PRV_UID, processor.getProviderUid());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#setProviderUid(com.ospreydcs.dp.api.model.ProviderUID)}.
     */
    @Test
    public final void testSetProviderUid() {
        
        IngestionFrameProcessor processor = new IngestionFrameProcessor();
        
        Assert.assertNotEquals(null, processor);
        Assert.assertEquals(null, processor.getProviderUid());
        
        processor.setProviderUid(REC_PRV_UID);
        Assert.assertEquals(REC_PRV_UID, processor.getProviderUid());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#setConcurrency(int)}.
     */
    @Test
    public final void testSetConcurrency() {
        
        // Parameters
        final int       cntThreads = 4;
        
        IngestionFrameProcessor processor = new IngestionFrameProcessor(REC_PRV_UID);
        
        Assert.assertNotEquals(null, processor);
        Assert.assertEquals(REC_PRV_UID, processor.getProviderUid());
        
        int cntThrdsDef = processor.getConcurrencyCount();
        
        processor.setConcurrency(cntThreads);
        Assert.assertEquals(cntThreads, processor.getConcurrencyCount());
        
        int cntThrdsNew = processor.getConcurrencyCount();
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + ": Default thread count=" + cntThrdsDef + ", new thread count=" + cntThrdsNew);
        
        processor.disableConcurrency();
        Assert.assertFalse(processor.hasConcurrency());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#setFrameDecomposition(long)}.
     */
    @Test
    public final void testSetFrameDecomposition() {
        
        // Parameters
        final long      szFrameMax = 5 * 1_000_000;
        
        IngestionFrameProcessor processor = new IngestionFrameProcessor(REC_PRV_UID);
        
        Assert.assertNotEquals(null, processor);
        Assert.assertEquals(REC_PRV_UID, processor.getProviderUid());
        
        Assert.assertNotEquals(szFrameMax, processor.getMaxFrameSize());
        
        processor.setFrameDecomposition(szFrameMax);
        Assert.assertTrue(processor.hasFrameDecomposition());
        Assert.assertEquals(szFrameMax, processor.getMaxFrameSize());

        processor.disableFrameDecomposition();
        Assert.assertFalse(processor.hasFrameDecomposition());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#disableFrameDecomposition()}.
     */
    @Test
    public final void testDisableFrameDecomposition() {

        IngestionFrameProcessor processor = new IngestionFrameProcessor(REC_PRV_UID);
        
        Assert.assertNotEquals(null, processor);
        Assert.assertEquals(REC_PRV_UID, processor.getProviderUid());
        
        Assert.assertNotEquals(0, processor.getMaxFrameSize());
        
        processor.disableFrameDecomposition();
        Assert.assertFalse(processor.hasFrameDecomposition());
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#disableConcurrency()}.
//     */
//    @Test
//    public final void testDisableConcurrency() {
//        Assert.fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#hasConcurrency()}.
//     */
//    @Test
//    public final void testHasConcurrency() {
//        Assert.fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#hasFrameDecomposition()}.
//     */
//    @Test
//    public final void testHasFrameDecomposition() {
//        Assert.fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#getConcurrencyCount()}.
//     */
//    @Test
//    public final void testGetConcurrencyCount() {
//        Assert.fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#getFailedDecompositions()}.
//     */
//    @Test
//    public final void testGetFailedDecompositions() {
//        Assert.fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#getFailedConversions()}.
//     */
//    @Test
//    public final void testGetFailedConversions() {
//        Assert.fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#hasProcessingFailure()}.
//     */
//    @Test
//    public final void testHasProcessingFailure() {
//        Assert.fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#hasPendingMessages()}.
//     */
//    @Test
//    public final void testHasPendingMessages() {
//        Assert.fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#hasShutdown()}.
//     */
//    @Test
//    public final void testHasShutdown() {
//        Assert.fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#activate()}.
     */
    @Test
    public final void testActivate() {

        IngestionFrameProcessor processor = new IngestionFrameProcessor(REC_PRV_UID);
        
        Assert.assertNotEquals(null, processor);
        Assert.assertEquals(REC_PRV_UID, processor.getProviderUid());

        Assert.assertFalse(processor.isSupplying());
        processor.activate();
        Assert.assertTrue(processor.isSupplying());
        
        try {
            boolean bolResult = processor.shutdown();
            
            Assert.assertTrue(bolResult);
            Assert.assertFalse(processor.isSupplying());
            
        } catch (InterruptedException e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "shutdown()", e));
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#shutdown()}.
//     */
//    @Test
//    public final void testShutdown() {
//        Assert.fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#shutdownNow()}.
     */
    @Test
    public final void testShutdownNow() {

        IngestionFrameProcessor processor = new IngestionFrameProcessor(REC_PRV_UID);
        
        Assert.assertNotEquals(null, processor);
        Assert.assertEquals(REC_PRV_UID, processor.getProviderUid());

        Assert.assertFalse(processor.isSupplying());
        processor.activate();
        Assert.assertTrue(processor.isSupplying());
        
        Assert.assertTrue(processor.isSupplying());
        processor.shutdownNow();
        Assert.assertFalse(processor.isSupplying());
        Assert.assertTrue(processor.hasShutdown());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#submit(com.ospreydcs.dp.api.ingest.IngestionFrame)}.
     */
    @Test
    public final void testSubmitIngestionFrameMultiThread() {
        
        // Parameters
        final int       cntCols = 100;
        final int       cntRows = 100;
        
        final IngestionFrame    frame = TestIngestionFrameGenerator.createDoublesFrameWithClock(cntCols, cntRows);
        
        IngestionFrameProcessor processor = new IngestionFrameProcessor(REC_PRV_UID);
        
        Assert.assertNotEquals(null, processor);
        Assert.assertEquals(REC_PRV_UID, processor.getProviderUid());

        processor.activate();
        Assert.assertTrue(processor.isSupplying());
        Assert.assertEquals(0, processor.getRequestQueueSize());
        
        Instant     insSubmit = Instant.now();
        processor.submit(frame);
        
        IngestDataRequest msgRqst = processor.poll();
        Assert.assertEquals(null, msgRqst);
        
        try {
            msgRqst = processor.take();
            Instant insTake = Instant.now();
            
            Duration durProcess = Duration.between(insSubmit, insTake);
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Single frame processing duration: " + durProcess);
            
            Assert.assertNotEquals(null, msgRqst);
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "take()", e));
            
        } finally {
            
            try {
                Assert.assertTrue(processor.isSupplying());
                processor.shutdown();
                Assert.assertFalse(processor.isSupplying());
                Assert.assertTrue(processor.hasShutdown());
                
            } catch (InterruptedException e) {
                Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "shutdown()", e));
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#submit(com.ospreydcs.dp.api.ingest.IngestionFrame)}.
     */
    @Test
    public final void testSubmitIngestionFrameSingleThread() {
        
        // Parameters
        final int       cntCols = 100;
        final int       cntRows = 100;
        
        final IngestionFrame    frame = TestIngestionFrameGenerator.createDoublesFrameWithClock(cntCols, cntRows);
        
        final long      szFrame = frame.allocationSizeFrame();
        
        IngestionFrameProcessor processor = new IngestionFrameProcessor(REC_PRV_UID);
        
        Assert.assertNotEquals(null, processor);
        Assert.assertEquals(REC_PRV_UID, processor.getProviderUid());
        
        processor.disableConcurrency();
        processor.activate();
        Assert.assertFalse(processor.hasConcurrency());
        Assert.assertTrue(processor.isSupplying());
        Assert.assertEquals(0, processor.getRequestQueueSize());
        
        Instant     insSubmit = Instant.now();
        processor.submit(frame);
        
        IngestDataRequest msgRqst = processor.poll();
        Instant insTake = Instant.now();
        
        Duration durProcess = Duration.between(insSubmit, insTake);
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Single frame processing duration: " + durProcess);
        
        Assert.assertNotEquals(null, msgRqst);
        try {
            Assert.assertTrue(processor.isSupplying());
            processor.shutdown();
            Assert.assertFalse(processor.isSupplying());
            Assert.assertTrue(processor.hasShutdown());

        } catch (InterruptedException e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "shutdown()", e));
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#submit(java.util.List)}.
     */
    @Test
    public final void testSubmitListOfIngestionFrameMultiThread() {
        
        // Parameters
        final int       cntFrames = 10;
        final int       cntCols = 100;
        final int       cntRows = 100;
        
        final List<IngestionFrame>    lstFrames = TestIngestionFrameGenerator.createDoublesPayloadWithClock(cntFrames, cntCols, cntRows);
        
        IngestionFrameProcessor processor = new IngestionFrameProcessor(REC_PRV_UID);
        
        Assert.assertNotEquals(null, processor);
        Assert.assertEquals(REC_PRV_UID, processor.getProviderUid());

        processor.activate();
        Assert.assertTrue(processor.isSupplying());
        Assert.assertEquals(0, processor.getRequestQueueSize());
        
        Instant     insSubmit = Instant.now();
        processor.submit(lstFrames);
        
        IngestDataRequest msgRqst = processor.poll();
        Assert.assertEquals(null, msgRqst);
        
        try {
            for (int iMsg=0; iMsg<cntFrames; iMsg++) {
                msgRqst = processor.take();
                Assert.assertNotEquals(null, msgRqst);
            }
            Instant insTake = Instant.now();
            
            Duration durProcess = Duration.between(insSubmit, insTake);
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - multi-frame processing duration: " + durProcess);

            // Frames are smaller than decomposition - queue should be exhausted
            msgRqst = processor.poll();
            Assert.assertEquals(null, msgRqst);
            
            // Check for processing failures
            Assert.assertFalse(processor.hasProcessingFailure());
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "take()", e));
            
        } finally {
            
            try {
                Assert.assertTrue(processor.isSupplying());
                processor.shutdown();
                Assert.assertFalse(processor.isSupplying());
                Assert.assertTrue(processor.hasShutdown());
                
            } catch (InterruptedException e) {
                Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "shutdown()", e));
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#submit(java.util.List)}.
     */
    @Test
    public final void testSubmitListOfIngestionFrameSingleThread() {
        
        // Parameters
        final int       cntFrames = 10;
        final int       cntCols = 100;
        final int       cntRows = 100;
        
        final List<IngestionFrame>    lstFrames = TestIngestionFrameGenerator.createDoublesPayloadWithClock(cntFrames, cntCols, cntRows);
        
        IngestionFrameProcessor processor = new IngestionFrameProcessor(REC_PRV_UID);
        
        Assert.assertNotEquals(null, processor);
        Assert.assertEquals(REC_PRV_UID, processor.getProviderUid());

        processor.disableConcurrency();
        processor.activate();
        Assert.assertFalse(processor.hasConcurrency());
        Assert.assertTrue(processor.isSupplying());
        Assert.assertEquals(0, processor.getRequestQueueSize());
        
        Instant     insSubmit = Instant.now();
        processor.submit(lstFrames);
        
        for (int iMsg=0; iMsg<cntFrames; iMsg++) {
            IngestDataRequest msgRqst = processor.poll();
            Assert.assertNotEquals(null, msgRqst);
        }
        Instant insTake = Instant.now();
        
        Duration durProcess = Duration.between(insSubmit, insTake);
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - multi-frame processing duration: " + durProcess);

        // Frames are smaller than decomposition - queue should be exhausted
        IngestDataRequest msgRqst = processor.poll();
        Assert.assertEquals(null, msgRqst);
        
        try {
            Assert.assertTrue(processor.isSupplying());
            processor.shutdown();
            Assert.assertFalse(processor.isSupplying());
            Assert.assertTrue(processor.hasShutdown());

        } catch (InterruptedException e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "shutdown()", e));
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#isSupplying()}.
//     */
//    @Test
//    public final void testIsSupplying() {
//        Assert.fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#take()}.
//     */
//    @Test
//    public final void testTake() {
//        Assert.fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#poll()}.
//     */
//    @Test
//    public final void testPoll() {
//        Assert.fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#poll(long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testPollLongTimeUnit() {
        
        // Parameters
        final int       cntFrames = 15;
        final int       cntCols = 100;
        final int       cntRows = 100;
        
        final List<IngestionFrame>    lstFrames = TestIngestionFrameGenerator.createDoublesPayloadWithClock(cntFrames, cntCols, cntRows);
        
        IngestionFrameProcessor processor = new IngestionFrameProcessor(REC_PRV_UID);
        
        Assert.assertNotEquals(null, processor);
        Assert.assertEquals(REC_PRV_UID, processor.getProviderUid());

        processor.activate();
        Assert.assertTrue(processor.isSupplying());
        Assert.assertEquals(0, processor.getRequestQueueSize());
        
        Instant     insSubmit = Instant.now();
        processor.submit(lstFrames);
        
        IngestDataRequest msgRqst = processor.poll();
        Assert.assertEquals(null, msgRqst);
        
        try {
            int     cntMsgs = 0;
            do {
                msgRqst = processor.poll(LNG_TIMEOUT, TU_TIMEOUT);
                
                if (msgRqst != null)
                    cntMsgs++;
                
                // Check the request queue size
                if (processor.getRequestQueueSize() == 0 && (cntMsgs < cntFrames))
                    Assert.assertTrue(processor.hasPendingTasks());
                
            } while (cntMsgs < cntFrames);
            Instant insComplete = Instant.now();
            
            Duration durProcess = Duration.between(insSubmit, insComplete);
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - multi-frame polled processing duration: " + durProcess);

            // Frames are smaller than decomposition - queue should be exhausted
            msgRqst = processor.poll();
            Assert.assertEquals(null, msgRqst);
            
            // Check for processing failures
            Assert.assertFalse(processor.hasProcessingFailure());
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "poll(long, TimeUnit)", e));
            
        } finally {
            
            try {
                Assert.assertTrue(processor.isSupplying());
                processor.shutdown();
                Assert.assertFalse(processor.isSupplying());
                Assert.assertTrue(processor.hasShutdown());
                
            } catch (InterruptedException e) {
                Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "shutdown()", e));
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#getRequestQueueSize()}.
     */
    @Test
    public final void testGetRequestQueueSize() {
        
        // Parameters
        final int       cntFrames = 15;
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        
        final List<IngestionFrame>    lstFrames = TestIngestionFrameGenerator.createDoublesPayloadWithClock(cntFrames, cntCols, cntRows);
        
        final long      szFrame = lstFrames.get(0).allocationSizeFrame();
        
        IngestionFrameProcessor processor = new IngestionFrameProcessor(REC_PRV_UID);
        
        Assert.assertNotEquals(null, processor);
        Assert.assertEquals(REC_PRV_UID, processor.getProviderUid());

        processor.activate();
        Assert.assertTrue(processor.isSupplying());
        Assert.assertEquals(0, processor.getRequestQueueSize());
        
        Instant     insSubmit = Instant.now();
        processor.submit(lstFrames);
        
        IngestDataRequest msgRqst = processor.poll();
        Assert.assertEquals(null, msgRqst);
        
        try {
            int     cntMsgs = 0;
            do {
                msgRqst = processor.poll(LNG_TIMEOUT, TU_TIMEOUT);
                
                if (msgRqst != null)
                    cntMsgs++;
                
//                // Check the request queue size
//                if (processor.getRequestQueueSize() == 0)
//                    Assert.assertTrue(processor.hasPendingMessages());
                
            } while (processor.getRequestQueueSize() > 0 || processor.hasPendingTasks()); // method under test
            Instant insComplete = Instant.now();
            
            Duration durProcess = Duration.between(insSubmit, insComplete);
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " - large multi-frame polled processing duration: " + durProcess);
            System.out.println("  Processed " + cntFrames + " large frames (" + szFrame + " bytes) into " + cntMsgs + " IngestDataRequest messages.");

            // Frames are smaller than decomposition - queue should be exhausted
            msgRqst = processor.poll();
            Assert.assertEquals(null, msgRqst);
            
            // Check for processing failures
            Assert.assertFalse(processor.hasProcessingFailure());
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "poll(long, TimeUnit)", e));
            
        } finally {
            
            try {
                Assert.assertTrue(processor.isSupplying());
                processor.shutdown();
                Assert.assertFalse(processor.isSupplying());
                Assert.assertTrue(processor.hasShutdown());
                
            } catch (InterruptedException e) {
                Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "shutdown()", e));
            }
        }
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
