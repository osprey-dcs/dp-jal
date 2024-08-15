/*
 * Project: dp-api-common
 * File:	IngestionFrameProcessorDepTest.java
 * Package: com.ospreydcs.dp.api.ingest.model.frame
 * Type: 	IngestionFrameProcessorDepTest
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
 * @since Apr 13, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.frame;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.test.TestIngestionFrameGenerator;
import com.ospreydcs.dp.api.model.IDataColumn;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.model.UniformSamplingClock;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.DataValue;
import com.ospreydcs.dp.grpc.v1.common.SamplingClock;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest.IngestionDataFrame;

/**
 * <p>
 * JUnit test cases for class <code>IngestionFrameProcessorDep</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 13, 2024
 *
 */
public class IngestionFrameProcessorDepTest {

    
    //
    // Test Constants
    //
    
    /** The data provider UID used for all <code>IngestDataRequest</code> messages. */
    public static final int         INT_PROVIDER_UID = 1;
    
    /** The data provider UID record used for all <code>IngestDataRequest</code> messages */
    public static final ProviderUID REC_PROVIDER_UID = ProviderUID.from(INT_PROVIDER_UID);
    
    
    /** Concurrency - Number of concurrent threads - for concurrency testing */
    public static final int         CNT_CONCURRENT_THREADS = 5;
    
    /** Frame Decomposition - Maximum ingestion frame memory allocation limit */
    public static final long        LNG_BINNING_SIZE = 2000000;
    
    /** Back Pressure - Message queue buffer capacity */
    public static final int         INT_BUFFER_CAPACITY = 10;
    
    
    //
    // Test Resources
    //
    
    /** A test ingestion frame */
    private static final IngestionFrame         MSG_FRAME_SMALL = createDoubleFrames(1, 10, 10).get(0);
    
    /** A list of test data frames that are small */
    private static final List<IngestionFrame>   LST_FRAMES_SMALL = createDoubleFrames(10, 10, 10);
    
    /** A list of test data frames that have moderate allocation */
    private static final List<IngestionFrame>   LST_FRAMES_MOD = createDoubleFrames(10, 100, 100);
    
    /** A list of test data frames that have moderate allocation */
    private static final List<IngestionFrame>   LST_FRAMES_LARGE = createDoubleFrames(10, 1000, 1000);
    
    
    /** A processor available for general testing -  activated for each test case in default configuration */
    private IngestionFrameProcessorDep processor;
    
    
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
        this.processor = IngestionFrameProcessorDep.from(REC_PROVIDER_UID);
        this.processor.activate();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        this.processor.shutdown();
    }

    
    // 
    // Test Cases
    //
    
    
    /**
     * Test method for {@link IngestionFrameProcessorDep#createRequest(IngestionFrame)}. 
     */
//    @Test
//    public final void testCreateRequest() {
//        
//        IngestionFrame  frame = LST_FRAMES_SMALL.get(0);
//        
//        try {
//            IngestDataRequest msgRqst = this.processor.createRequest(frame);
//
//            Assert.assertEquals(INT_PROVIDER_UID, msgRqst.getProviderId());
//
//        } catch (Exception e) {
//            Assert.fail("createRequest(IngestionFrame) threw exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
//        }
//    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#from(int)}.
     */
    @Test
    public final void testFrom() {
        IngestionFrameProcessorDep     prcrFrom = IngestionFrameProcessorDep.from(REC_PROVIDER_UID);
        
        // Start it up 
        boolean bolActivated = prcrFrom.activate();
        
        Assert.assertTrue(bolActivated);
        
        // Shut it down and wait
        try {
            boolean bolShutdown = prcrFrom.shutdown();
            
            Assert.assertTrue(bolShutdown);
            
        } catch (InterruptedException e) {
            Assert.fail("Processor throw InterruptedException while waiting for shutdown: " + e.getMessage());
        }
        
        Assert.assertFalse(prcrFrom.isSupplying());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#IngestionFrameProcessor(int)}.
     */
    @Test
    public final void testIngestionFrameProcessor() {
        IngestionFrameProcessorDep     prcrCtor = new IngestionFrameProcessorDep(REC_PROVIDER_UID);
        
        // Start it up 
        boolean bolActivated = prcrCtor.activate();
        
        Assert.assertTrue(bolActivated);
        
        // Shut it down hard 
        prcrCtor.shutdownNow();
        
        Assert.assertFalse(prcrCtor.isSupplying());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#enableConcurrency(int)}.
     */
    @Test
    public final void testEnableConcurrency() {
        
        boolean bolConcurrency = this.processor.hasConcurrency();
        
        try {
            this.processor.enableConcurrency(CNT_CONCURRENT_THREADS);
            
            Assert.fail("Attempting to enable concurrency while active should throw exception.");
            
        } catch (IllegalStateException e) {
            Assert.assertTrue(this.processor.isSupplying());
            Assert.assertEquals(bolConcurrency, this.processor.hasConcurrency());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#disableConcurrency()}.
     */
    @Test
    public final void testDisableConcurrency() {
        
        boolean bolConcurrency = this.processor.hasConcurrency();

        try {
            this.processor.disableConcurrency();
            
            Assert.fail("Attempting to disable concurrency while active should throw exception.");
            
        } catch (IllegalStateException e) {
            Assert.assertTrue(this.processor.isSupplying());
            Assert.assertEquals(bolConcurrency, this.processor.hasConcurrency());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#enableFrameDecomposition(long)}.
     */
    @Test
    public final void testEnableFrameDecomposition() {
        
        boolean bolDecomp = this.processor.hasFrameDecomposition();
        
        if (bolDecomp) {
            this.processor.disableFrameDecomposition();
            Assert.assertFalse(this.processor.hasFrameDecomposition());
            
            this.processor.enableFrameDecomposition(LNG_BINNING_SIZE);
            Assert.assertTrue(this.processor.hasFrameDecomposition());
            
        } else {
            this.processor.enableFrameDecomposition(LNG_BINNING_SIZE);
            Assert.assertTrue(this.processor.hasFrameDecomposition());
            
            this.processor.disableFrameDecomposition();
            Assert.assertFalse(this.processor.hasFrameDecomposition());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#disableFrameDecomposition()}.
     */
    @Test
    public final void testDisableFrameDecomposition() {
        
        boolean bolDecomp = this.processor.hasFrameDecomposition();
        
        if (bolDecomp) {
            this.processor.disableFrameDecomposition();
            Assert.assertFalse(this.processor.hasFrameDecomposition());
            
            this.processor.enableFrameDecomposition(LNG_BINNING_SIZE);
            Assert.assertTrue(this.processor.hasFrameDecomposition());
            
        } else {
            this.processor.enableFrameDecomposition(LNG_BINNING_SIZE);
            Assert.assertTrue(this.processor.hasFrameDecomposition());
            
            this.processor.disableFrameDecomposition();
            Assert.assertFalse(this.processor.hasFrameDecomposition());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#enableBackPressure(int)}.
     */
    @Test
    public final void testEnableBackPressure() {
        
        boolean bolBackPressure = this.processor.hasBackPressure();
        
        if (bolBackPressure) {
            this.processor.disableBackPressure();
            Assert.assertFalse(this.processor.hasBackPressure());
            
            this.processor.enableBackPressure(INT_BUFFER_CAPACITY);
            Assert.assertTrue(this.processor.hasBackPressure());
            
        } else {
            this.processor.enableBackPressure(INT_BUFFER_CAPACITY);
            Assert.assertTrue(this.processor.hasBackPressure());
            
            this.processor.disableBackPressure();
            Assert.assertFalse(this.processor.hasBackPressure());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#disableBackPressure()}.
     */
    @Test
    public final void testDisableBackPressure() {
        
        boolean bolBackPressure = this.processor.hasBackPressure();
        
        if (bolBackPressure) {
            this.processor.disableBackPressure();
            Assert.assertFalse(this.processor.hasBackPressure());
            
            this.processor.enableBackPressure(INT_BUFFER_CAPACITY);
            Assert.assertTrue(this.processor.hasBackPressure());
            
        } else {
            this.processor.enableBackPressure(INT_BUFFER_CAPACITY);
            Assert.assertTrue(this.processor.hasBackPressure());
            
            this.processor.disableBackPressure();
            Assert.assertFalse(this.processor.hasBackPressure());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#activate()}.
     */
    @Test
    public final void testActivate() {
        IngestionFrameProcessorDep prcrTest = IngestionFrameProcessorDep.from(REC_PROVIDER_UID);
        
        Assert.assertFalse(prcrTest.isSupplying());
        prcrTest.activate();
        Assert.assertTrue(prcrTest.isSupplying());
        
        try {
            prcrTest.shutdown();
            
            Assert.assertFalse(prcrTest.isSupplying());
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException while waiting for shutdown: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#shutdown()}.
     */
    @Test
    public final void testShutdown() {
        IngestionFrameProcessorDep prcrTest = IngestionFrameProcessorDep.from(REC_PROVIDER_UID);
        
        Assert.assertFalse(prcrTest.isSupplying());
        prcrTest.activate();
        Assert.assertTrue(prcrTest.isSupplying());
        
        try {
            prcrTest.shutdown();
            
            Assert.assertFalse(prcrTest.isSupplying());
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException while waiting for shutdown: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#shutdownNow()}.
     */
    @Test
    public final void testShutdownNow() {
        IngestionFrameProcessorDep prcrTest = IngestionFrameProcessorDep.from(REC_PROVIDER_UID);
        
        Assert.assertFalse(prcrTest.isSupplying());
        prcrTest.activate();
        Assert.assertTrue(prcrTest.isSupplying());
        
        prcrTest.shutdownNow();
        Assert.assertFalse(prcrTest.isSupplying());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#addFrame(com.ospreydcs.dp.api.ingest.IngestionFrame)}.
     */
    @Test
    public final void testAddFrame() {
        
        // Parameters and resources
        List<IngestionFrame>    lstFrames = LST_FRAMES_MOD;
        final int               cntFrames = lstFrames.size();
        
        // Add frames one at a time checking for exceptions
        int     indFrames = 0;
        for (IngestionFrame frame : LST_FRAMES_MOD) {
            
            try {
                this.processor.addFrame(frame);
                
            } catch (IllegalStateException | InterruptedException e) {
                Assert.fail("addFrame() threw " + e.getClass().getSimpleName() + " exception for frame #" + indFrames + " of " + cntFrames + ": " + e.getMessage());
                
            } finally {
                indFrames++;
            }
        }
        
        try {
            // Shutdown the processor - does not return until all frame are processed
            this.processor.shutdown();
            
        } catch (InterruptedException e) {
            Assert.fail("Processor shutdown threw interrupted exception: " + e.getMessage());
        }
        
        int szMsgQue = this.processor.getRequestQueueSize();
        Assert.assertEquals(cntFrames, this.processor.getRequestQueueSize());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#addFrames(java.util.List)}.
     */
    @Test
    public final void testAddFrames() {
        
        // Parameters and resources
        List<IngestionFrame>    lstFrames = LST_FRAMES_MOD;
        final int               cntFrames = lstFrames.size();

        // Add frames all at once
        try {
            this.processor.addFrames(lstFrames);
            
            Thread.sleep(20L);
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("addFrames() threw " + e.getClass().getSimpleName() + " exception adding "  + cntFrames + " frames: " + e.getMessage());
        }
        
        try {
            // Shutdown the processor - does not return until all frame are processed
            this.processor.shutdown();
            
        } catch (InterruptedException e) {
            Assert.fail("Processor shutdown threw interrupted exception: " + e.getMessage());
        }
        
        Assert.assertEquals(cntFrames, this.processor.getRequestQueueSize());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#awaitQueueReady()}.
     */
    @Test
    public final void testAwaitBackPressure() {
        
        // Resources and Parameters
        List<IngestionFrame>    lstFrames = LST_FRAMES_SMALL;
        final int               cntFrames = lstFrames.size();
        final int               intQueCapacity = cntFrames;// - 1;
        final long              lngDelay = 100; // thread take delay - milliseconds
        Runnable                tskTake = () -> {
            try {
                Thread.sleep(lngDelay);
                this.processor.take();
//                this.processor.take();
                return;
                
            } catch (IllegalStateException | InterruptedException e) {
                String  strMsg = "awaitBackPressure() thread take() exception " + e.getClass().getSimpleName() + ": " + e.getMessage();
                
                System.err.println(strMsg);
                Assert.fail(strMsg);
            }
        };
        Thread                  thdTake = new Thread(tskTake);
        
        try {
            this.processor.enableBackPressure(intQueCapacity);
            this.processor.addFrames(lstFrames);
            
            // Give processor time to fill up message queue
            Thread.sleep(lngDelay);
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("addFrames() threw " + e.getClass().getSimpleName() + " exception adding "  + cntFrames + " frames: " + e.getMessage());
        }
        
        try {
            Instant     insStart = Instant.now();
            thdTake.start();
            this.processor.awaitQueueReady();
            Instant     insStop = Instant.now();
            Duration    durWait = Duration.between(insStart, insStop);
            
            System.out.println("The IngestionFrameProcessorDep#awaitBackPressure() blocked for " + durWait.toString());
            
            thdTake.join();
            Instant     insThreadStop = Instant.now();;
            Duration    durThread = Duration.between(insStop, insThreadStop);
            
            System.out.println("The take() thread then completed after " + durThread.toString());
        
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("awaitBackPressure() threw " + e.getClass().getSimpleName() + " exception : " + e.getMessage());
        }
    }

    /**
     * Test method for {@link IngestionFrameProcessorDep#awaitRequestQueueEmpty()} 
     */
    @Test
    public final void testAwaitRequestQueueEmpty() {
        
        // Resources and Parameters
        List<IngestionFrame>    lstFrames = LST_FRAMES_MOD;
        final int               cntFrames = lstFrames.size();
        final long              lngDelay = 10; // milliseconds
        
        Thread thdConsumer  = new Thread( () -> {
            int cntTakes = 0;
            while (this.processor.isSupplying()) {
//                System.out.println("  awaitRequestQueueEmpty() isActive() = " + this.processor.isActive());
                try {
                    IngestDataRequest msgRqst = this.processor.take();
                    Thread.sleep(lngDelay);
//                    System.out.println("  awaitRequestQueueEmpty() take() #" + cntTakes);
//                    System.out.println("  msg==null = " + (msgRqst==null));
                    cntTakes++;

                } catch (IllegalStateException e) {
                    String  strMsg = "awaitRequestQueueEmpty() thread take() exception " + e.getClass().getSimpleName() + ": " + e.getMessage();

                    System.err.println(strMsg);
                    System.err.println("  isActive() = " + this.processor.isSupplying());
                    Assert.fail(strMsg);
                    
                } catch (InterruptedException e) {
                    System.out.println("  INTERRUPTED - awaitRequestQueueEmpty() thread after iteration " + cntTakes);
                }
            }
//            System.out.println("  TERMINATED - awaitRequestQueueEmpty() thread after iteration " + cntTakes);
        });
        
        try {
            this.processor.addFrames(lstFrames);
            
            // Give processor time to fill up message queue
            Thread.sleep(100);
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("addFrames() threw " + e.getClass().getSimpleName() + " exception adding "  + cntFrames + " frames: " + e.getMessage());
        }
        
        try {
            Instant     insStart = Instant.now();
            thdConsumer.start();
            this.processor.awaitRequestQueueEmpty();
            Instant     insStop = Instant.now();
            Duration    durWait = Duration.between(insStart, insStop);
            
            System.out.println("The IngestionFrameProcessorDep#awaitRequestQueueEmpty() blocked for " + durWait.toString());
            
            this.processor.shutdown();
            thdConsumer.interrupt();
            
            Instant     insThreadStop = Instant.now();;
            Duration    durThread = Duration.between(insStop, insThreadStop);
            
            System.out.println("The consumer() thread then completed after " + durThread.toString());
        
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("awaitBackPressure() threw " + e.getClass().getSimpleName() + " exception : " + e.getMessage());
        }
        
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#isSupplying()}.
     */
    @Test
    public final void testIsActive() {

        // Parameters and resources
        List<IngestionFrame>    lstFrames = LST_FRAMES_MOD;
        final int               cntFrames = lstFrames.size();

        // Add frames all at once
        try {
            this.processor.addFrames(lstFrames);
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("addFrames() threw " + e.getClass().getSimpleName() + " exception adding "  + cntFrames + " frames: " + e.getMessage());
        }
        
        try {
            this.processor.shutdown();
            
            // Still message in buffer - should be active after soft shutdown
            Assert.assertTrue(this.processor.isSupplying());
            
            // Clear the buffer then true again
            while (this.processor.isSupplying())
                this.processor.take();
            
            Assert.assertFalse(this.processor.isSupplying());
            
        } catch (InterruptedException e) {
            Assert.fail("shutdown() threw Interrupted exception: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#take()}.
     */
    @Test
    public final void testTake() {

        // Parameters and resources
        List<IngestionFrame>    lstFrames = LST_FRAMES_MOD;
        final int               cntFrames = lstFrames.size();

        // Add frames all at once
        try {
            this.processor.addFrames(lstFrames);
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("addFrames() threw " + e.getClass().getSimpleName() + " exception adding "  + cntFrames + " frames: " + e.getMessage());
        }
        
        try {
            this.processor.shutdown();
            
            // Still message in buffer - should be active after soft shutdown
            Assert.assertTrue(this.processor.isSupplying());
            
            // Clear the buffer then try again
            while (this.processor.isSupplying())
                this.processor.take();
            
            Assert.assertFalse(this.processor.isSupplying());
            
        } catch (InterruptedException e) {
            Assert.fail("shutdown() threw Interrupted exception: " + e.getMessage());

        } catch (IllegalStateException e) {
            Assert.fail("shutdown() threw Interrupted exception: " + e.getMessage());
        }
        
        try {
            
            // This should throw an IllegalStateException
            this.processor.take();
            
            Assert.fail("Inactive processor take() should have thrown exception.");
            
        } catch (InterruptedException e) {
            Assert.fail("Inactive processed take() threw unexpected Interrupted exception: " + e.getMessage());

        } catch (IllegalStateException e) {
            // We should end here
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#poll()}.
     */
    @Test
    public final void testPoll() {

        // Parameters and resources
        List<IngestionFrame>    lstFrames = LST_FRAMES_MOD;
        final int               cntFrames = lstFrames.size();

        // Add frames all at once
        try {
            this.processor.addFrames(lstFrames);
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("addFrames() threw " + e.getClass().getSimpleName() + " exception adding "  + cntFrames + " frames: " + e.getMessage());
        }
        
        int                 cntMsgs = 0;
        try {
            this.processor.shutdown();
            
            // Still message in buffer - should be active after soft shutdown
            Assert.assertTrue(this.processor.isSupplying());
            
            // Clear the buffer with polling until exception is thrown
            IngestDataRequest   msgRqst = null;
            do { 
                msgRqst = this.processor.poll();
                
                cntMsgs++;
            } while (msgRqst != null);
            
            Assert.fail("The last poll() should have thrown an exception.");
            
        } catch (InterruptedException e) {
            Assert.fail("shutdown() threw Interrupted exception: " + e.getMessage());

        } catch (IllegalStateException e) {
            
            Assert.assertFalse(this.processor.isSupplying());
            Assert.assertEquals(cntFrames, cntMsgs);
        }
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessorDep#poll(long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testPollLongTimeUnit() {

        // Parameters and resources
        List<IngestionFrame>    lstFrames = LST_FRAMES_MOD;
        final int               cntFrames = lstFrames.size();
        final long              lngWait = 10;
        final TimeUnit          tuWait = TimeUnit.MILLISECONDS;
        final Exchanger<Integer> rsltPolls = new Exchanger<>();
        final Exchanger<Integer> rsltMsgs = new Exchanger<>();
        
        Thread thdPoller = new Thread(() -> {
            Integer cntPolls = 0;
            Integer cntMsgs = 0;

//            System.out.println("  Poller Thread: entering loop, isActive() = " + this.processor.isActive());
            
            while (this.processor.isSupplying()) {
                try {
                    IngestDataRequest msgRqst = this.processor.poll(lngWait, tuWait);
                    cntPolls++;
                    
//                    System.out.println("  Poller Thread: cntPolls=" + cntPolls);
                    
                    if (msgRqst == null)
                        continue;
                    
                    cntMsgs++;
                    
                } catch (IllegalStateException | InterruptedException e) {
                    String  strMsg = "testPollLongTimeUnit polling thread - exception while polling " 
                                    + e.getClass().getSimpleName() 
                                    + ": " + e.getMessage();
                    System.err.println(strMsg);
                    Assert.fail(strMsg);
                }
            }
            try {
                rsltPolls.exchange(cntPolls);
                rsltMsgs.exchange(cntMsgs);
                
            } catch (InterruptedException e) {
                String  strMsg = "testPollLongTimeUnit polling thread - interrupted while exchanging results " 
                                + ": " + e.getMessage();
                System.err.println(strMsg);
                Assert.fail(strMsg);
            }
        });
        
        // Start polling thread
        thdPoller.start();
        
        // Sleep for while - make some failed polls
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Assert.fail("Interrupted while waiting to begin adding frames");
        }

        // Add frames all at once
        try {
            this.processor.addFrames(lstFrames);
            
            // Do a soft shutdown
            this.processor.shutdown();
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("addFrames() threw " + e.getClass().getSimpleName() + " exception adding "  + cntFrames + " frames: " + e.getMessage());
        }
        
        // Get the results
        try {
            int cntPolls = rsltPolls.exchange(0);
            int cntMsgs = rsltMsgs.exchange(0);
            
            System.out.println("testPollLongTimeUnit Results:");
            System.out.println("  number of polls = " + cntPolls);
            System.out.println("  number of messages acquired = " + cntMsgs);
            System.out.println("  number of frames processed = " + cntFrames);
            Assert.assertEquals(cntFrames, cntMsgs);
            
        } catch (InterruptedException e) {
            Assert.fail("Could not exchange results with poller thread?");
        }
    }
    
    /**
     * Times ingestion frame processing
     */
    @Test
    public final void testTimeProcessor() {
        
        //  Parameters and resources
        final int                   cntRows = 1000;
        final int                   cntCols = 2000;
        final int                   cntFrames = 10;
        List<IngestionFrame>        lstFrames = createDoubleFrames(cntFrames, cntCols, cntRows);
        
        final long                  lngFrmAlloc = lstFrames.get(0).allocationSizeFrame();
        
        // Note that the ingestion frames will be destroyed due to decomposition
        try {
            // Start timer, add frames, shutdown, stop timer
            Instant     insStart = Instant.now();
            this.processor.addFrames(lstFrames);
            this.processor.shutdown();
            Instant     insStop = Instant.now();
            
            // Compute results
            Duration    durActive = Duration.between(insStart, insStop);
            int         cntMsgs = this.processor.getRequestQueueSize();
            int         intMsgAlloc = this.processor.take().getSerializedSize();
            
            // Report Results
            System.out.println("testTimeProcessor:");
            System.out.println("  frame allocation = " + lngFrmAlloc);
            System.out.println("  frames processed = " + cntFrames);
            System.out.println("  messages created = " + cntMsgs);
            System.out.println("  message allocation = " + intMsgAlloc);
            System.out.println("  time elapsed = " + durActive.toMillis() + "ms");
            
            // Check that all source frame were destroyed
            boolean bolData = lstFrames.stream().allMatch(frm -> !frm.hasData());
            boolean bolNoCols = lstFrames.stream().allMatch(frm -> frm.getColumnCount() == 0);
//            boolean bolNoRows = lstFrames.stream().allMatch(frm -> frm.getRowCount() == 0);
//            int indFrame = 0;
//            for (IngestionFrame frm :  lstFrames) {
//                System.out.println("Frame #" + indFrame);
//                System.out.println("  columns = " + frm.getColumnCount());
//                System.out.println("  rows = " + frm.getRowCount());
//                indFrame++;
//            }
            Assert.assertTrue(bolData);;
            Assert.assertTrue(bolNoCols);
            
        } catch (IllegalStateException e) {
            Assert.fail("IllegalStateException: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("InterruptedException: " + e.getMessage());
            
        }
    }
    
    /**
     * Test the processing accuracy 
     */
    @Test
    public final void testProcessorAccuracy() {
        
        // Parameters and resources
        IngestionFrame      frame = MSG_FRAME_SMALL;
        
        try {
            this.processor.addFrame(frame);
            this.processor.shutdown();
            
            IngestDataRequest   msgRqst = this.processor.take();
            
            IngestionDataFrame  msgFrm = msgRqst.getIngestionDataFrame();
            
            // Compare clocks
            SamplingClock  msgClk = msgFrm.getDataTimestamps().getSamplingClock();
            UniformSamplingClock    clkMsg = ProtoMsg.toUniformSamplingClock(msgClk);
            UniformSamplingClock    clkFrm = frame.getSamplingClock();
            
            Assert.assertEquals(clkFrm, clkMsg);
            
            // Compare data
            List<DataColumn> lstMsgCols = msgFrm.getDataColumnsList();
            for (DataColumn msgCol : lstMsgCols) {
                String                  strName = msgCol.getName();
                IDataColumn<Object>     frmCol = frame.getDataColumn(strName);
                
                Assert.assertNotNull(frmCol);
                
                List<DataValue> lstMsgColVals = msgCol.getDataValuesList();
                List<Object>    lstMsgColObjs = ProtoMsg.extractValues(msgCol);
                List<Object>    lstFrmColObjs = frmCol.getValues();
                
                Assert.assertEquals(lstFrmColObjs, lstMsgColObjs);
            }
            
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("addFrame() threw exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates a collection of <code>IngestionFrame</code> instances used for testing.
     * </p>
     * <p>
     * All returned ingestion frames are populated with double values and establish a 
     * uniform sampling clock to identify timestamps.
     * </p>
     * 
     * @param cntFrames the number of ingestion frames to create
     * @param cntCols   the number of column in each frame
     * @param cntRows   the number of rows in each frame
     * 
     * @return  a new collection of ingestion frames 
     */
    private static List<IngestionFrame> createDoubleFrames(int cntFrames, int cntCols, int cntRows) {
        
        // Returned object
        ArrayList<IngestionFrame>   lstFrames = new ArrayList<>(cntFrames);
        
        // Create the frames
        for (int iFrame=0; iFrame<cntFrames; iFrame++) {
            IngestionFrame  frame = TestIngestionFrameGenerator.createDoublesFrameWithClock(cntCols, cntRows);
            
            lstFrames.add(frame);
        }
        
        return lstFrames;
    }
}
