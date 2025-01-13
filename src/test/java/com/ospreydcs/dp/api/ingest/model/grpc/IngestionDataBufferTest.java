/*
 * Project: dp-api-common
 * File:	IngestionDataBufferTest.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IngestionDataBufferTest
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
 * @since Aug 6, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.grpc;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.ingest.test.TestIngestDataRequestGenerator;
import com.ospreydcs.dp.api.model.IMessageSupplier;
import com.ospreydcs.dp.api.model.IMessageConsumer;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * JUnit test cases for class <code>IngestionDataBuffer</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 6, 2024
 *
 */
public class IngestionDataBufferTest {

    //
    // Class Constants
    //
    
    /** Fixed thread pool (periodic task executor) maximum size */
    public static final int         CNT_MAX_PERD_TASKS = 5;
    
    
    /** Message supplier task execution period */
    public static final long        LNG_PERD_PROD_TASK = 25;
    
    /** Message supplier task execution period time units */
    public static final TimeUnit    TU_PERD_PROD_TASK = TimeUnit.MILLISECONDS;
    
    
    /** Message consumer task execution period */
    public static final long        LNG_PERD_CONS_TASK = 50;
    
    /** Message consumer task execution period units */
    public static final TimeUnit    TU_PERD_CONS_TASK = TimeUnit.MILLISECONDS; 
            
    
    /** General Polling Timeout limit */
    public static final long        LNG_POLL_TIMEOUT = 25;
    
    /** General polling timeout unit */
    public static final TimeUnit    TU_POLL_TIMEOUT = TimeUnit.MILLISECONDS;
    
    
    //
    // Test Resources
    //
    
    /** Collection of data request messages available as payload for all tests */
    private static final List<IngestDataRequest>    LST_MSGS_RQST = TestIngestDataRequestGenerator.createDoublesMessagesWithClock();
    
    /** The number of messages within the test message payload */
    private static final int                        CNT_PAYLOAD = LST_MSGS_RQST.size();
    
    /** The total memory allocation of the test message payload */
    private static final long                       SZ_PAYLOAD_ALLOC = LST_MSGS_RQST.stream().mapToLong(msg -> msg.getSerializedSize()).sum();
    
    
    /** Periodic task executor service */
    private static final ScheduledExecutorService   XTOR_PERD_TASKS = Executors.newScheduledThreadPool(CNT_MAX_PERD_TASKS);
    
    
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
        XTOR_PERD_TASKS.shutdownNow();
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#IngestionDataBuffer()}.
     */
    @Test
    public final void testIngestionDataBuffer() {
        
        IngestionDataBuffer  buffer = new IngestionDataBuffer();
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#IngestionDataBuffer(long)}.
     */
    @Test
    public final void testIngestionDataBufferLong() {
        
        // Parameters
        final   int     szCapacity = 101;     
        
        IngestionDataBuffer  buffer = new IngestionDataBuffer(szCapacity);
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertEquals(szCapacity, buffer.getQueueCapacity());
        Assert.assertFalse(buffer.isSupplying());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#IngestionDataBuffer(long, boolean)}.
     */
    @Test
    public final void testIngestionDataBufferLongBoolean() {
        
        // Parameters
        final int       szCapacity = 101;
        final boolean   bolBackPressure = false;
        
        IngestionDataBuffer  buffer = new IngestionDataBuffer(szCapacity, bolBackPressure);
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertEquals(szCapacity, buffer.getQueueCapacity());
        Assert.assertEquals(bolBackPressure, buffer.hasBackPressure());
        Assert.assertFalse(buffer.isSupplying());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#setQueueCapcity(long)}.
     */
    @Test
    public final void testSetQueueCapcity() {
        
        // Parameters
        final int       szCapacity = 42;
        
        IngestionDataBuffer  buffer = IngestionDataBuffer.create();
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        Assert.assertNotEquals(szCapacity, buffer.getQueueCapacity());
        buffer.setQueueCapcity(szCapacity);
        Assert.assertEquals(szCapacity, buffer.getQueueCapacity());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#enableBackPressure()}.
     */
    @Test
    public final void testEnableBackPressure() {
        
        IngestionDataBuffer  buffer = IngestionDataBuffer.create();
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());

        buffer.enableBackPressure();
        Assert.assertTrue(buffer.hasBackPressure());
        
        buffer.disableBackPressure();
        Assert.assertFalse(buffer.hasBackPressure());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#disableBackPressure()}.
     */
    @Test
    public final void testDisableBackPressure() {
        
        IngestionDataBuffer  buffer = IngestionDataBuffer.create();
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());

        buffer.enableBackPressure();
        Assert.assertTrue(buffer.hasBackPressure());
        
        buffer.disableBackPressure();
        Assert.assertFalse(buffer.hasBackPressure());
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#hasBackPressure()}.
//     */
//    @Test
//    public final void testHasBackPressure() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#getQueueCapacity()}.
//     */
//    @Test
//    public final void testGetQueueCapacity() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#getQueueSize()}.
//     */
//    @Test
//    public final void testGetQueueSize() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#activate()}.
     */
    @Test
    public final void testActivate() {
        
        // Parameters
        final int       szCapacity = 101;
        final boolean   bolBackPressure = false;
        
        IngestionDataBuffer  buffer = new IngestionDataBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        Assert.assertEquals(0, buffer.getQueueSize());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#shutdown()}.
     */
    @Test
    public final void testShutdown() {
        
        // Parameters
        final int       szCapacity = 101;
        final boolean   bolBackPressure = false;
        
        IngestionDataBuffer  buffer = new IngestionDataBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        
        try {
            boolean bolResult = buffer.shutdown();
            Assert.assertTrue(bolResult);
            Assert.assertFalse(buffer.isSupplying());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception thrown during shutdown: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#shutdownNow()}.
     */
    @Test
    public final void testShutdownNow() {
        
        // Parameters
        final int       szCapacity = 101;
        final boolean   bolBackPressure = false;
        
        IngestionDataBuffer  buffer = new IngestionDataBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        
        try {
            Assert.assertEquals(0, buffer.getQueueSize());
            buffer.offer(LST_MSGS_RQST);
            Assert.assertEquals(CNT_PAYLOAD, buffer.getQueueSize());
            
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("buffer enqueue() threw exception: " + e.getMessage());
        }

        buffer.shutdownNow();
        Assert.assertFalse(buffer.isSupplying());
        Assert.assertEquals(0, buffer.getQueueSize());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#offer(IngestDataRequest)}.
     */
    @Test
    public final void testEnqueueIngestDataRequest() {
        
        // Parameters
        final int       szCapacity = 101;
        final boolean   bolBackPressure = false;
        
        final IngestDataRequest msgRqst = LST_MSGS_RQST.get(0);
        final long              lngMsgAlloc = msgRqst.getSerializedSize();
                
        IngestionDataBuffer  buffer = new IngestionDataBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        Assert.assertEquals(0, buffer.getQueueSize());
        
        try {
            buffer.offer(msgRqst);
            Assert.assertEquals(1, buffer.getQueueSize());
            Assert.assertEquals(lngMsgAlloc, buffer.getQueueAllocation());
            
            buffer.shutdownNow();
            Assert.assertEquals(0, buffer.getQueueSize());
            Assert.assertFalse(buffer.isSupplying());
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("Exception thrown during enqueue(): " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#offer(List)}.
     */
    @Test
    public final void testEnqueueListOfIngestDataRequest() {
        
        // Parameters
        final List<IngestDataRequest>   lstMsgs = LST_MSGS_RQST;
        final int       cntMsgs =       lstMsgs.size();
        final long      szPayload =     SZ_PAYLOAD_ALLOC;
        
        final long      szCapacity = szPayload/2;
        final boolean   bolBackPressure = true;
        
        IngestionDataBuffer  buffer = new IngestionDataBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        Assert.assertEquals(0, buffer.getQueueSize());
        
        try {
            buffer.offer(lstMsgs);
            Assert.assertEquals(cntMsgs, buffer.getQueueSize());
            Assert.assertEquals(szPayload, buffer.getQueueAllocation());
            
            buffer.shutdownNow();
            Assert.assertEquals(0, buffer.getQueueSize());
            Assert.assertEquals(0, buffer.getQueueAllocation());
            Assert.assertFalse(buffer.isSupplying());
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("Exception thrown during enqueue(): " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#awaitQueueReady()}.
     */
    @Test
    public final void testAwaitQueueReady() {
        
        // Parameters
        final List<IngestDataRequest>   lstMsgs = LST_MSGS_RQST;
        final int       cntMsgs =       lstMsgs.size();
        final long      szPayload =     SZ_PAYLOAD_ALLOC;
        
        final long      szCapacity = szPayload/2;
        final boolean   bolBackPressure = false;
        
        IngestionDataBuffer  buffer = new IngestionDataBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        Assert.assertEquals(0, buffer.getQueueSize());
        Assert.assertEquals(0, buffer.getQueueAllocation());

        try {
            buffer.offer(lstMsgs);
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("Failed to enqueue message list.");
        }
        
        MessageConsumerTask        tskCons = new MessageConsumerTask(buffer, MessageConsumerTask.BufferOperation.TAKE);

        Instant     insStart = Instant.now();
        Future<?>   futCons = XTOR_PERD_TASKS.scheduleAtFixedRate(tskCons, LNG_PERD_CONS_TASK, LNG_PERD_CONS_TASK, TU_PERD_CONS_TASK);
        
        try {
            buffer.awaitQueueReady();
            
        } catch (InterruptedException e) {
            Assert.fail("Interrupted while waiting for queue ready.");
        }
        
        Instant     insReady = Instant.now();
        
        try {
            buffer.awaitQueueEmpty();
            
        } catch (InterruptedException e) {
            Assert.fail("Interrupted while waiting for queue empty.");
        }
        
        Instant     insEmpty = Instant.now();
        
        futCons.cancel(false);
        
        try {
            buffer.shutdown();
            
        } catch (InterruptedException e) {
            Assert.fail("Interrupted while waiting for queue shutdown.");
        }
        
        // Compute results
        Duration        durReady = Duration.between(insStart, insReady);
        Duration        durEmpty = Duration.between(insStart, insEmpty);
        
        // Report findings
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Message count    : " + cntMsgs);
        System.out.println("  Buffer capacity  : " + szCapacity);
        System.out.println("  Queue ready wait : " + durReady);
        System.out.println("  Queue empty wait : " + durEmpty);
        System.out.println("  Consumer ");
        System.out.println("    completed : " + tskCons.hasCompleted());
        System.out.println("    error     : " + tskCons.hasError());
        System.out.println("    messages  : " + tskCons.getConsumedMessageCount());
        System.out.println("    allocation: " + tskCons.getConsumedMessageAlloc());
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#awaitQueueEmpty()}.
//     */
//    @Test
//    public final void testAwaitQueueEmpty() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#isSupplying()}.
     */
    @Test
    public final void testIsSupplying() {
        
        // Parameters
        final List<IngestDataRequest>   lstMsgs = LST_MSGS_RQST;
        final int       cntMsgs =       lstMsgs.size();
        final long      szPayload =     SZ_PAYLOAD_ALLOC;
        
        final long      szCapacity = szPayload/2;
        final boolean   bolBackPressure = false;
        
        IngestionDataBuffer  buffer = new IngestionDataBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        Assert.assertEquals(0, buffer.getQueueSize());

        try {
            buffer.offer(lstMsgs);
            
            Thread  thdShutdown = new Thread(() -> { 
                    try {
                        buffer.shutdown();
                    } catch (InterruptedException e) {
                    } 
                }
            );
            thdShutdown.start();
            
            Assert.assertTrue(buffer.isSupplying());
            
            FutureTask<Integer> tskCons = new FutureTask<>(this.createGreedyConsumerTake(buffer));
            Thread              thdCons = new Thread(tskCons);
            thdCons.start();
            int cntMsgsConsumed = tskCons.get();
            
            Assert.assertFalse(buffer.isSupplying());
            Assert.assertEquals(cntMsgs, cntMsgsConsumed);
        
        } catch (Exception e) {
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#take()}.
     */
    @Test
    public final void testTake() {
        
        // Parameters
        final List<IngestDataRequest>   lstMsgs = LST_MSGS_RQST;
        final int       cntMsgs =       lstMsgs.size();
        final long      szPayload =     SZ_PAYLOAD_ALLOC;
        
        final long      szCapacity = szPayload/2;
        final boolean   bolBackPressure = true;
        
        final int       intWait = 10;
        final long      lngUpdate = 1;
        final TimeUnit  tuUpdate = TimeUnit.SECONDS;
        
        IngestionDataBuffer  buffer = new IngestionDataBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        Assert.assertEquals(0, buffer.getQueueSize());

        MessageSupplierTask    tskProd = new MessageSupplierTask(lstMsgs, buffer);
        MessageConsumerTask    tskCons = new MessageConsumerTask(buffer, MessageConsumerTask.BufferOperation.TAKE);
        Runnable        tskBuffUpdt = this.createBufferUpdateTask(buffer, System.out);
        Runnable        tskConsUpdt = this.createConsumerUpdateTask(tskCons, System.out);

        Thread          thdBuffMntr = new Thread(this.createBufferMonitorTask(buffer, intWait));
        System.out.println("--------------- " + JavaRuntime.getQualifiedMethodNameSimple() + " ----------------------");
        
        Instant     insStart = Instant.now();
        Future<?>   futProd = XTOR_PERD_TASKS.scheduleAtFixedRate(tskProd, LNG_PERD_PROD_TASK, LNG_PERD_PROD_TASK, TU_PERD_PROD_TASK);
        Future<?>   futCons = XTOR_PERD_TASKS.scheduleAtFixedRate(tskCons, LNG_PERD_CONS_TASK, LNG_PERD_CONS_TASK, TU_PERD_CONS_TASK);
        Future<?>   futBuffUpdt = XTOR_PERD_TASKS.scheduleAtFixedRate(tskBuffUpdt, lngUpdate, lngUpdate, tuUpdate);
        Future<?>   futConsUpdt = XTOR_PERD_TASKS.scheduleAtFixedRate(tskConsUpdt, lngUpdate, lngUpdate, tuUpdate);
        
        thdBuffMntr.start();
        try {
            thdBuffMntr.join();
            
        } catch (InterruptedException e) {
            Assert.fail("Interrupted while waiting for buffer monitor task to complete: " + e.getMessage());
        }
        
        Instant     insStop = Instant.now();
        futProd.cancel(false);
        futCons.cancel(false);
        futBuffUpdt.cancel(false);
        futConsUpdt.cancel(false);
        
        Duration    durActive = Duration.between(insStart, insStop);
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Message count    : " + cntMsgs);
        System.out.println("  Buffer capacity  : " + szCapacity);
        System.out.println("  Duration active  : " + durActive);
        System.out.println("  Consumer ");
        System.out.println("    completed : " + tskCons.hasCompleted());
        System.out.println("    error     : " + tskCons.hasError());
        System.out.println("    messages  : " + tskCons.getConsumedMessageCount());
        System.out.println("--------------- " + JavaRuntime.getQualifiedMethodNameSimple() + " ----------------------");
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#poll()}.
     */
    @Test
    public final void testPoll() {
        
        // Parameters
        final List<IngestDataRequest>   lstMsgs = LST_MSGS_RQST;
        final int       cntMsgs =       lstMsgs.size();
        final long      szPayload =     SZ_PAYLOAD_ALLOC;
//        final long      lngPoll = 10;
//        final TimeUnit  tuPoll = TimeUnit.MILLISECONDS;
        
        final long      szCapacity = szPayload/2;
        final boolean   bolBackPressure = true;
        
        IngestionDataBuffer  buffer = new IngestionDataBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        Assert.assertEquals(0, buffer.getQueueSize());

        MessageSupplierTask  tskProd = new MessageSupplierTask(lstMsgs, buffer);
        Callable<Integer>   callCons = this.createGreedyConsumerPoll(buffer);
        
        FutureTask<Integer> tskCons = new FutureTask<>(callCons);
        Thread              thdCons = new Thread(tskCons);
        thdCons.start();
        
        Instant     insStart = Instant.now();
        Future<?>   futProd = XTOR_PERD_TASKS.scheduleAtFixedRate(tskProd, LNG_PERD_PROD_TASK, LNG_PERD_PROD_TASK, TU_PERD_PROD_TASK);
        
        try {
            int     cntPolls = tskCons.get();
            Instant insStop = Instant.now();
            futProd.cancel(false);
            
            Duration durPolling = Duration.between(insStart, insStop);
            
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
            System.out.println("  Message count    : " + cntMsgs);
            System.out.println("  Buffer capacity  : " + szCapacity);
            System.out.println("  Polling interval : 0 " );
            System.out.println("  Duration polling : " + durPolling);
            System.out.println("  Number of polls  : " + cntPolls);
        
        } catch (Exception e) {
            Assert.fail("Exception attempting to recover future result: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionDataBuffer#poll(long, TimeUnit)}.
     */
    @Test
    public final void testPollLongTimeUnit() {
        
        // Parameters
        final List<IngestDataRequest>   lstMsgs = LST_MSGS_RQST;
        final int       cntMsgs =       lstMsgs.size();
        final long      szPayload =     SZ_PAYLOAD_ALLOC;
        final long      lngPoll = 10;
        final TimeUnit  tuPoll = TimeUnit.MILLISECONDS;
        
        final long      szCapacity = szPayload/2;
        final boolean   bolBackPressure = true;
        
        IngestionDataBuffer  buffer = new IngestionDataBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        Assert.assertEquals(0, buffer.getQueueSize());

        MessageSupplierTask        tskProd = new MessageSupplierTask(lstMsgs, buffer);
        Callable<Integer>   callCons = this.createGreedyConsumerPoll(buffer, lngPoll, tuPoll);
        
        FutureTask<Integer> tskCons = new FutureTask<>(callCons);
        Thread              thdCons = new Thread(tskCons);
        thdCons.start();
        
        Instant     insStart = Instant.now();
        Future<?>   futProd = XTOR_PERD_TASKS.scheduleAtFixedRate(tskProd, LNG_PERD_PROD_TASK, LNG_PERD_PROD_TASK, TU_PERD_PROD_TASK);
        
        try {
            int     cntPolls = tskCons.get();
            Instant insStop = Instant.now();
            futProd.cancel(false);
            
            Duration durPolling = Duration.between(insStart, insStop);
            
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
            System.out.println("  Message count    : " + cntMsgs);
            System.out.println("  Buffer capacity  : " + szCapacity);
            System.out.println("  Polling interval : " + lngPoll + " " + tuPoll);
            System.out.println("  Duration polling : " + durPolling);
            System.out.println("  Number of polls  : " + cntPolls);
        
        } catch (Exception e) {
            Assert.fail("Exception attempting to recover future result: " + e.getMessage());
        }
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates a <code>IngestionDataBuffer</code> monitoring task that.
     * </p>
     * <p>
     * The returned task does not complete until the given buffer stops supplying ingest data request 
     * messages.  The task continues (indefinitely) polling the buffer using 
     * <code>{@link IngestionDataBuffer#isSupplying()</code> method until it returns <code>false</code>.
     * </p>
     * 
     * @param buffer        buffer being monitored
     * @param lngWaitTime   waiting time between IsSupplying() polls (in milliseconds)
     * 
     * @return  task that executes until buffer stops supplying
     */
    private Runnable    createBufferMonitorTask(IMessageSupplier<IngestDataRequest> buffer, long lngWaitTime) {
        
        Runnable task = () -> {
            
            while (buffer.isSupplying()) {
                
                try {
                    Thread.sleep(lngWaitTime);
                    
                } catch (InterruptedException e) {
                    return;
                }
            }
        };
        
        return task;
    }
    
    /**
     * <p>
     * Creates a task that updates the given print stream as to current buffer conditions.
     * </p>
     * 
     * @param buffer    the target message buffer being monitored
     * @param ps        the output print stream to receive updates
     * 
     * @return  a new buffer update task
     */
    private Runnable    createBufferUpdateTask(IngestionDataBuffer buffer, PrintStream ps) {
        
        Runnable    task = () -> {
            ps.println("Buffer: isSupplying = " + buffer.isSupplying());
            ps.println("Buffer: size = " + buffer.getQueueSize());
        };
        
        return task;
    }
    
    /**
     * <p>
     * Creates a task the updates the given print stream as to the current message consumer conditions.
     * </p>
     * 
     * @param consumer  consumer task being monitored
     * @param ps        the output print stream to receive updates
     * 
     * @return  a new consumer update task
     */
    private Runnable    createConsumerUpdateTask(MessageConsumerTask consumer, PrintStream ps) {
        
        Runnable task = () -> {
            
            ps.println("Consumer: messages consumed = " + consumer.getConsumedMessageCount());
            ps.println("Consumer: has completed = " + consumer.hasCompleted());
            ps.println("Consumer: has error = " + consumer.hasError());
        };
        
        return task;
    }
    
    /**
     * <p>
     * Creates a supplier of <code>IngestDataReqst</code> messages offering individual messages as fast as the
     * buffer accepts them.
     * </p>
     * <p>
     * The supplier task simply iterates through all messages within the given iterator offering them to the
     * given buffer as fast as accepted. 
     * </p>
     * 
     * @param itrMsgs   iterator of the request message payload
     * @param buffer    target message buffer receiving payload
     * 
     * @return  supplier task for the given payload iterator and target message buffer
     */
    @SuppressWarnings("unused")
    private Callable<Integer>   createSupplier(Iterator<IngestDataRequest> itrMsgs, IMessageConsumer<IngestDataRequest> buffer) {
        
        Callable<Integer>   task = () -> {
            
            // The number of ingest data request message supplied
            int cntMsgs = 0;
            
            while (itrMsgs.hasNext()) {
                IngestDataRequest   msgRqst = itrMsgs.next();
                
                buffer.offer(msgRqst);
            }
            
            return cntMsgs;
        };
        
        return task;
    }
    
    /**
     * <p>
     * Creates a "greedy" <code>IngestDataRequest</code> consumer task for the given message buffer.
     * </p>
     * <p>
     * Consumer retrieves ingest data request messages as soon as they are available.  It does not 
     * terminate until the the buffer stops supplying.
     * </p>
     * <p>
     * The consumer task uses the <code>{@link IngestionDataBuffer#take()}</code> blocking method for
     * message retrieval.
     * </p> 
     * <p>
     * The returned <code>Callable&lt;Integer&gt;</code> task returns the number of messages consumed. 
     * </p>
     * 
     * @param buffer    the message buffer supplying request messages
     * 
     * @return  the consumer task for the given message buffer
     */
    private Callable<Integer>   createGreedyConsumerTake(IMessageSupplier<IngestDataRequest> buffer) {
        
        Callable<Integer>   task = () -> {
            
            // The number of ingest data messages consumed
            int cntMsgs = 0;
            
            while (buffer.isSupplying()) {
                IngestDataRequest   msgRqst = buffer.take();
                
                if (msgRqst != null)
                    cntMsgs++;
            }
            
            return cntMsgs;
        };
        
        return task;
    }

    /**
     * <p>
     * Creates a "greedy" <code>IngestDataRequest</code> consumer task for the given message buffer.
     * </p>
     * <p>
     * Consumer retrieves ingest data request messages as soon as they are available.  It does not 
     * terminate until the the buffer stops supplying.
     * </p>
     * <p>
     * The consumer task uses the <code>{@link IngestionDataBuffer#poll()}</code> non-blocking method for
     * message retrieval.
     * </p> 
     * <p>
     * The returned <code>Callable&lt;Integer&gt;</code> task returns the number of polling attempts. 
     * </p>
     * 
     * @param buffer        the message buffer supplying request messages
     * @param lngTimeout    timeout limit for polling operation
     * @param tuTimeout     timeout units for polling operation
     * 
     * @return  the consumer task for the given message buffer
     */
    private Callable<Integer>   createGreedyConsumerPoll(IMessageSupplier<IngestDataRequest> buffer) {
        
        Callable<Integer>   task = () -> {
            
            // The number of ingest data messages consumed
            int cntMsgs = 0;
            int cntPolls = 0;
            
            while (buffer.isSupplying()) {
                IngestDataRequest   msgRqst = buffer.poll();
                
                cntPolls++;
                
                if (msgRqst != null)
                    cntMsgs++;
            }
            
            return cntPolls;
        };
        
        return task;
    }

    /**
     * <p>
     * Creates a "greedy" <code>IngestDataRequest</code> consumer task for the given message buffer.
     * </p>
     * <p>
     * Consumer retrieves ingest data request messages as soon as they are available.  It does not 
     * terminate until the the buffer stops supplying.
     * </p>
     * <p>
     * The consumer task uses the <code>{@link IngestionDataBuffer#poll(long, TimeUnit)}</code> blocking method for
     * message retrieval.
     * </p> 
     * <p>
     * The returned <code>Callable&lt;Integer&gt;</code> task returns the number of polling attempts. 
     * </p>
     * 
     * @param buffer        the message buffer supplying request messages
     * @param lngTimeout    timeout limit for polling operation
     * @param tuTimeout     timeout units for polling operation
     * 
     * @return  the consumer task for the given message buffer
     */
    private Callable<Integer>   createGreedyConsumerPoll(IMessageSupplier<IngestDataRequest> buffer, long lngTimeout, TimeUnit tuTimeout) {
        
        Callable<Integer>   task = () -> {
            
            // The number of ingest data messages consumed
            int cntMsgs = 0;
            int cntPolls = 0;
            
            while (buffer.isSupplying()) {
                IngestDataRequest   msgRqst = buffer.poll(lngTimeout, tuTimeout);
                
                cntPolls++;
                
                if (msgRqst != null)
                    cntMsgs++;
            }
            
            return cntPolls;
        };
        
        return task;
    }

    
}
