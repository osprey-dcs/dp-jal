/*
 * Project: dp-api-common
 * File:	IngestionChannelTest.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IngestionChannelTest
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
 * @since Aug 8, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.grpc;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnectionFactory;
import com.ospreydcs.dp.api.ingest.model.IMessageSupplier;
import com.ospreydcs.dp.api.ingest.test.TestIngestDataRequestGenerator;
import com.ospreydcs.dp.api.model.ClientRequestUID;
import com.ospreydcs.dp.api.model.DpGrpcStreamType;
import com.ospreydcs.dp.api.model.IngestionResponse;
import com.ospreydcs.dp.api.model.IngestionResult;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * JUnit test cases for class <code>IngestionChannel</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 8, 2024
 *
 */
public class IngestionChannelTest {

    
    //
    // Class Constants
    //
    
//    /** Fixed thread pool (periodic task executor) maximum size */
//    public static final int         CNT_MAX_PERD_TASKS = 5;
//    
//    
//    /** Message supplier task execution period */
//    public static final long        LNG_PERD_PROD_TASK = 25;
//    
//    /** Message supplier task execution period time units */
//    public static final TimeUnit    TU_PERD_PROD_TASK = TimeUnit.MILLISECONDS;
//    
//    
//    /** Message consumer task execution period */
//    public static final long        LNG_PERD_CONS_TASK = 50;
//    
//    /** Message consumer task execution period units */
//    public static final TimeUnit    TU_PERD_CONS_TASK = TimeUnit.MILLISECONDS; 
//            
//    
//    /** General Polling Timeout limit */
//    public static final long        LNG_POLL_TIMEOUT = 25;
//    
//    /** General polling timeout unit */
//    public static final TimeUnit    TU_POLL_TIMEOUT = TimeUnit.MILLISECONDS;
    
    
    //
    // Test Resources
    //
    
    /** The connection to the Ingestion Service */
    private static DpIngestionConnection            CONN_INGEST;
    
    
    /** Collection of data request messages available as payload for all tests */
    private static final List<IngestDataRequest>    LST_MSGS_RQST = TestIngestDataRequestGenerator.createDoublesMessagesWithClock();
    
    /** The number of messages within the test message payload */
    private static final int                        CNT_PAYLOAD = LST_MSGS_RQST.size();
    
    /** The total memory allocation of the test message payload */
    private static final long                       SZ_PAYLOAD_ALLOC = LST_MSGS_RQST.stream().mapToLong(msg -> msg.getSerializedSize()).sum();
    
    
    /** A message buffer available for testing */
    private static final IngestionMessageBuffer     BUF_MSGS = new IngestionMessageBuffer();
    
    
//    /** Periodic task executor service */
//    private static final ScheduledExecutorService   XTOR_PERD_TASKS = Executors.newScheduledThreadPool(CNT_MAX_PERD_TASKS);
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        CONN_INGEST = DpIngestionConnectionFactory.FACTORY.connect();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        CONN_INGEST.shutdownSoft();
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#from(com.ospreydcs.dp.api.ingest.model.IMessageSupplier, com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection)}.
     */
    @Test
    public final void testFrom() {
        IngestionChannel    chan = IngestionChannel.from(BUF_MSGS, CONN_INGEST);
        
        Assert.assertNotEquals(null, chan);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#IngestionChannel(com.ospreydcs.dp.api.ingest.model.IMessageSupplier, com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection)}.
     */
    @Test
    public final void testIngestionChannel() {
        IngestionChannel    chan = new IngestionChannel(BUF_MSGS, CONN_INGEST);
        
        Assert.assertNotEquals(null, chan);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#setStreamType(com.ospreydcs.dp.api.model.DpGrpcStreamType)}.
     */
    @Test
    public final void testSetStreamType() {

        // Parameters
        final DpGrpcStreamType    enmStrmType = DpGrpcStreamType.FORWARD;
        final DpGrpcStreamType    enmStrmBad = DpGrpcStreamType.BACKWARD;
        
        IngestionChannel    chan = new IngestionChannel(BUF_MSGS, CONN_INGEST);
        Assert.assertNotEquals(null, chan);
        
        chan.setStreamType(enmStrmType);
        Assert.assertEquals(enmStrmType, chan.getStreamType());
        
        try {
            chan.setStreamType(enmStrmBad);
            Assert.fail("setStreamType accepted bad stream " + enmStrmBad);
            
        } catch (Exception e) {
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#setMultipleStreams(int)}.
     */
    @Test
    public final void testSetMultipleStreams() {
        
        // Parameters 
        final int   cntStrms1 = 1;
        final int   cntStrms2 = 7;
        
        IngestionChannel    chan = new IngestionChannel(BUF_MSGS, CONN_INGEST);
        Assert.assertNotEquals(null, chan);
        
        chan.setMultipleStreams(cntStrms1);
        Assert.assertTrue(chan.hasMultipleStreams());
        Assert.assertEquals(cntStrms1, chan.getMaxDatastreams());
        
        chan.setMultipleStreams(cntStrms2);
        Assert.assertTrue(chan.hasMultipleStreams());
        Assert.assertNotEquals(cntStrms1, chan.getMaxDatastreams());
        Assert.assertEquals(cntStrms2, chan.getMaxDatastreams());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#disableMultipleStreams()}.
     */
    @Test
    public final void testDisableMultipleStreams() {
        
        // Parameters 
        final int   cntStrms = 3;
        
        IngestionChannel    chan = new IngestionChannel(BUF_MSGS, CONN_INGEST);
        Assert.assertNotEquals(null, chan);
        
        chan.setMultipleStreams(cntStrms);
        Assert.assertTrue(chan.hasMultipleStreams());
        Assert.assertEquals(cntStrms, chan.getMaxDatastreams());
        
        chan.disableMultipleStreams();
        Assert.assertFalse(chan.hasMultipleStreams());
        Assert.assertEquals(cntStrms, chan.getMaxDatastreams());
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#hasMultipleStreams()}.
//     */
//    @Test
//    public final void testHasMultipleStreams() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#getMaxDatastreams()}.
//     */
//    @Test
//    public final void testGetMaxDatastreams() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#getStreamType()}.
//     */
//    @Test
//    public final void testGetStreamType() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#getRequestCount()}.
     */
    @Test
    public final void testGetRequestCount() {
        
        // Parameters
        final List<IngestDataRequest>   lstMsgs = LST_MSGS_RQST;
        final int       cntMsgs =       lstMsgs.size();
        final int       szCapacity =    cntMsgs/2;
        final boolean   bolBackPressure = false;
        
        // Create a message buffer, load it with the payload, then activate
        IngestionMessageBuffer  buffer = new IngestionMessageBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        try {
            buffer.offer(lstMsgs);
            Assert.assertEquals(cntMsgs, buffer.getQueueSize());
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("buffer.offer() threw exception: " + e.getMessage());
        }
        
        // Shutdown buffer on separate thread since shutdown() operation is blocking - does not return until exhausted
        Thread  thdShutdnBuff = new Thread(() -> { try { buffer.shutdown(); } catch (Exception e) {} });
        thdShutdnBuff.start();
        
        // Create the channel then send everything all at once - the buffer will unload everything
        IngestionChannel    chan = new IngestionChannel(buffer, CONN_INGEST);
        Assert.assertNotEquals(null, chan);
        
        Instant insStart, insFinish, insShutdn;
        try {
            insStart = Instant.now();
            boolean bolActive = chan.activate();
            Assert.assertTrue(bolActive);
        
        } catch (Exception e) {
            Assert.fail("active() exception: " + e.getMessage());
            return;
        }
        
        // Shut down channel (waits for all messages to transmit)
        try {
            boolean bolShutdown = chan.shutdown();
            insFinish = Instant.now();
            Assert.assertTrue(bolShutdown);
            
        } catch (Exception e) {
            Assert.fail("shutdown() exception: " + e.getMessage());
            return;
        }
        
        try {
            thdShutdnBuff.join();
            insShutdn = Instant.now();
            
        } catch (InterruptedException e) {
            Assert.fail("Buffer shutdown thread was interrupted during join(): " + e.getMessage());
            return;
        }
        
        // Collect results
        Duration    durXmit = Duration.between(insStart, insFinish);
        Duration    durShdn = Duration.between(insStart, insShutdn);
        int         cntXmts = chan.getRequestCount();
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Transmission duration  : " + durXmit);
        System.out.println("  Active duration        : " + durShdn);
        System.out.println("  Message payload count  : " + cntMsgs);
        System.out.println("  Message transmit count : " + cntXmts);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#getRequestIds()}.
     */
    @Test
    public final void testGetRequestIds() {

        // Parameters
        final List<IngestDataRequest>   lstMsgs = LST_MSGS_RQST;
        final int       cntMsgs =       lstMsgs.size();
        
        // Create the channel and transmit all data
        Instant             insStart = Instant.now();
        IngestionChannel    chan = this.createChannelAndTransmitAll(lstMsgs);
        Instant             insFinish = Instant.now();
        
        // Collect results
        Duration    durXmit = Duration.between(insStart, insFinish);
        int         cntXmits = chan.getRequestCount();
        int         cntRsps = chan.getResponseCount();
        List<ClientRequestUID>  lstRqstIds = chan.getRequestIds();
        IngestionResult         recResult = chan.getIngestionResult();
//        List<IngestionResponse> lstRsps = chan.getIngestionResponses();
//        List<IngestionResponse> lstExcps = chan.getIngestionExceptions();
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Transmission duration   : " + durXmit);
        System.out.println("  Message payload count   : " + cntMsgs);
        System.out.println("  Message transmit count  : " + cntXmits);
        System.out.println("  Message response count  : " + cntRsps);
        System.out.println("  Exception count         : " + recResult.exceptions().size());
        System.out.println("  Request UIDs sent       : " + lstRqstIds);
        System.out.println("  Request UIDs received   : " + recResult.receivedRequestIds());
        System.out.println("  Request UIDs rejected   : " + recResult.rejectedRequestIds());
        System.out.println("  Transmit exceptions     : " + recResult.exceptions());
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#getResponseCount()}.
//     */
//    @Test
//    public final void testGetResponseCount() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#getIngestionResponses()}.
//     */
//    @Test
//    public final void testGetIngestionResponses() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#getIngestionExceptions()}.
//     */
//    @Test
//    public final void testGetIngestionExceptions() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#isActive()}.
     */
    @Test
    public final void testIsActive() {
        
        IngestionChannel    chan = new IngestionChannel(BUF_MSGS, CONN_INGEST);
        Assert.assertNotEquals(null, chan);
        Assert.assertFalse(chan.isActive());

        try {
            // This should fail because the IMessageSupplier interface is inactive
            chan.activate();

            Assert.fail("Cannot activate an IngestionChannel with inactive supplier.");
            
        } catch (Exception e) {
            // Should end up here
        }
        
        Assert.assertFalse(chan.isActive());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#activate()}.
     */
    @Test
    public final void testActivate() {
        
        // Parameters
        final List<IngestDataRequest>   lstMsgs = LST_MSGS_RQST;
        final int       cntMsgs =       lstMsgs.size();
        final int       szCapacity =    cntMsgs/2;
        final boolean   bolBackPressure = false;
        
        // Create a message buffer, load it with the payload, then activate
        IngestionMessageBuffer  buffer = new IngestionMessageBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        
        IngestionChannel    chan = new IngestionChannel(buffer, CONN_INGEST);
        Assert.assertNotEquals(null, chan);
        Assert.assertFalse(chan.isActive());
        
        // Activate channel
        Instant     insActiveStart, insActiveFinish;
        try {
            insActiveStart = Instant.now();
            boolean bolActive = chan.activate();
            insActiveFinish = Instant.now();
            
            Assert.assertTrue(bolActive);
            Assert.assertTrue(chan.isActive());
            
        } catch (Exception e) {
            Assert.fail("Activation failed : " + e.getMessage());
            return;
        }
        
        // Shut down the channel
        Instant     insShdnStart, insShdnFinish;
        try {
            System.out.println("Shutting down buffer...");
            boolean bolBuffer = buffer.shutdown();
            Assert.assertTrue(bolBuffer);
            
            insShdnStart = Instant.now();
            boolean bolShutdown = chan.shutdown();
            insShdnFinish = Instant.now();
            
            Assert.assertTrue(bolShutdown);
            Assert.assertFalse(chan.isActive());
            
        } catch (InterruptedException e) {
            Assert.fail("Soft shutdown() failed: " + e.getMessage());
            return;
        }
        
        // Collection results
        Duration    durActive = Duration.between(insActiveStart, insActiveFinish);
        Duration    durShdn = Duration.between(insShdnStart, insShdnFinish);
                
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Time to activate  : " + durActive);
        System.out.println("  Time to shut down : " + durShdn);
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#shutdown()}.
//     */
//    @Test
//    public final void testShutdown() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannel#shutdownNow()}.
     */
    @Test
    public final void testShutdownNow() {
        
        // Parameters
        final List<IngestDataRequest>   lstMsgs = LST_MSGS_RQST;
        final int       cntMsgs =       lstMsgs.size();
        final int       szCapacity =    cntMsgs/2;
        final boolean   bolBackPressure = false;
        
        // Create a message buffer, load it with the payload, then activate
        IngestionMessageBuffer  buffer = new IngestionMessageBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        
        IngestionChannel    chan = new IngestionChannel(buffer, CONN_INGEST);
        Assert.assertNotEquals(null, chan);
        Assert.assertFalse(chan.isActive());
        
        // Activate channel
        Instant     insActiveStart, insActiveFinish;
        try {
            insActiveStart = Instant.now();
            boolean bolActive = chan.activate();
            insActiveFinish = Instant.now();
            
            Assert.assertTrue(bolActive);
            Assert.assertTrue(chan.isActive());
            
        } catch (Exception e) {
            Assert.fail("Activation failed : " + e.getMessage());
            return;
        }
        
        // Shut down the channel
        Instant     insShdnStart, insShdnFinish;
        
        insShdnStart = Instant.now();
        boolean bolShutdown = chan.shutdownNow();
        insShdnFinish = Instant.now();

        Assert.assertTrue(bolShutdown);
        Assert.assertFalse(chan.isActive());
            
        
        // Collection results
        Duration    durActive = Duration.between(insActiveStart, insActiveFinish);
        Duration    durShdn = Duration.between(insShdnStart, insShdnFinish);
                
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Time to activate  : " + durActive);
        System.out.println("  Time to shut down : " + durShdn);
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates a new <code>IngestionChannel</code> instance with all default parameters then transmit
     * all the given messages.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned instance has been shutdown and is inactive.  Howevever, all transmission parameters
     * should be available.
     * </p>
     * 
     * @param lstMsgs   the payload of ingest data request messages to transmit on new channel
     * 
     * @return  the channel instance after creation, transmission, and shut down
     */
    private IngestionChannel    createChannelAndTransmitAll(List<IngestDataRequest> lstMsgs) {
        
        // Parameters
        final int       cntMsgs =       lstMsgs.size();
        final int       szCapacity =    cntMsgs/2;
        final boolean   bolBackPressure = false;
        
        // Create a message buffer, load it with the payload, then activate
        IngestionMessageBuffer  buffer = new IngestionMessageBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        try {
            buffer.offer(lstMsgs);
            Assert.assertEquals(cntMsgs, buffer.getQueueSize());
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("buffer.offer() threw exception: " + e.getMessage());
        }
        
        // Shutdown buffer on separate thread since shutdown() operation is blocking - does not return until exhausted
        Thread  thdShutdnBuff = new Thread(() -> { try { buffer.shutdown(); } catch (Exception e) {} });
        thdShutdnBuff.start();
        
        // Create the channel then send everything all at once - the buffer will unload everything
        IngestionChannel    chan = new IngestionChannel(buffer, CONN_INGEST);
        Assert.assertNotEquals(null, chan);
        
        try {
            boolean bolActive = chan.activate();
            Assert.assertTrue(bolActive);
        
        } catch (Exception e) {
            Assert.fail("active() exception: " + e.getMessage());
            return null;
        }
        
        // Shut down channel (waits for all messages to transmit)
        try {
            boolean bolShutdown = chan.shutdown();
            Assert.assertTrue(bolShutdown);
            
        } catch (Exception e) {
            Assert.fail("shutdown() exception: " + e.getMessage());
            return null;
        }
        
        try {
            thdShutdnBuff.join();
            
        } catch (InterruptedException e) {
            Assert.fail("Buffer shutdown thread was interrupted during join(): " + e.getMessage());
            return null;
        }
        
        return chan;
    }
}
