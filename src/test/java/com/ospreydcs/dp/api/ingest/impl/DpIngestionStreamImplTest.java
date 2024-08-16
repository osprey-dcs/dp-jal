/*
 * Project: dp-api-common
 * File:	DpIngestionStreamImplTest.java
 * Package: com.ospreydcs.dp.api.ingest.impl
 * Type: 	DpIngestionStreamImplTest
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
 * @since Aug 16, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.impl;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnectionFactory;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.ingest.DpIngestionException;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.test.TestIngestionFrameGenerator;
import com.ospreydcs.dp.api.model.DpGrpcStreamType;
import com.ospreydcs.dp.api.model.IngestionResponse;
import com.ospreydcs.dp.api.model.ProviderRegistrar;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>DpIngestionStreamImpl</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 16, 2024
 *
 */
public class DpIngestionStreamImplTest {

    
    //
    // Class Constants
    //
    
    /** General waiting timeout limit */
    public static final long            LNG_TIMEOUT_WAIT = 10;
    
    /** General waiting timeout units */
    public static final TimeUnit        TU_TIMEOUT_WAIT = TimeUnit.SECONDS;
    
    
    //
    // Test Resources
    //
    
    /** Data Provider unique name */
    public static final String                  STR_PRVR_NAME = DpIngestionStreamImplTest.class.getSimpleName();
    
    /** The Data Provider registration record used for testing */
    private static final ProviderRegistrar      REC_PRVR_REG = ProviderRegistrar.from(STR_PRVR_NAME);
    
    
    /** The Data Provider UID obtained from the Ingestion Service */
    private static ProviderUID                  REC_PRVR_UID = null;
    
    
    /** The common connection used when possible */
    private static DpIngestionConnection        CONN_INGEST;
    
    /** The common DpIngestionStreamImpl under test (used when possible) */
    private static DpIngestionStreamImpl        INGEST_TEST;
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        CONN_INGEST = DpIngestionConnectionFactory.FACTORY.connect();
        INGEST_TEST = new DpIngestionStreamImpl(CONN_INGEST);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        
        // Shut down if not already
        if (!INGEST_TEST.isShutdown())
            INGEST_TEST.shutdownNow();
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#DpIngestionStreamImpl(com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection)}.
     */
    @Test
    public final void testDpIngestionStreamImpl() {

        // Open the connection to the Ingestion Service
        DpIngestionConnection connIngest;
        try {
            connIngest = DpIngestionConnectionFactory.FACTORY.connect();
            
        } catch (DpGrpcException e) {
            Assert.fail("Ingestion Service connection failed: " + e.getMessage());
            return;
        }
        
        // Create the interface implementation from the connection
        DpIngestionStreamImpl   istream = new DpIngestionStreamImpl(connIngest);
        Assert.assertFalse(istream.isStreamOpen());
        Assert.assertFalse(istream.isShutdown());
        Assert.assertFalse(istream.isTerminated());
        
        Assert.assertEquals(0, istream.getQueueSize());
        
        // Open the interface stream
        try {
            REC_PRVR_UID = istream.openStream(REC_PRVR_REG);
            Assert.assertTrue(istream.isStreamOpen());
            
        } catch (DpIngestionException e) {
            istream.shutdownNow();
            
            Assert.fail("The interface failed openStream(): " + e.getMessage());
            return;
        }
        
        // Close the interface stream
        try {
            List<IngestionResponse> lstRsps = istream.closeStream();
            Assert.assertFalse(istream.isStreamOpen());
            Assert.assertEquals(0, lstRsps.size());
            
        } catch (IllegalStateException | CompletionException | InterruptedException e) {
            istream.shutdownNow();
            Assert.fail("The interface failed closeStream(): type=" + e.getClass().getSimpleName() + ", detail=" + e.getMessage());
            return;
        }
        
        // Shut down the interface
        try {
            boolean bolResult = istream.shutdown();
            Assert.assertTrue(bolResult);
            Assert.assertTrue(istream.isShutdown());
            
        } catch (InterruptedException e) {
            Assert.fail("Interface failed shutdown(): " + e.getMessage());
            return;
        }
        
        // Wait for everything to terminate
        try {
            boolean bolResult = istream.awaitTermination();
            Assert.assertTrue(bolResult);
            Assert.assertTrue(istream.isTerminated());
            
        } catch (InterruptedException e) {
            Assert.fail("Interrupted while waiting for interface termination: " + e.getMessage());
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#shutdown()}.
//     */
//    @Test
//    public final void testShutdown() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#shutdownNow()}.
     */
    @Test
    public final void testShutdownNow() {

        // Open the connection to the Ingestion Service
        DpIngestionConnection connIngest;
        try {
            connIngest = DpIngestionConnectionFactory.FACTORY.connect();
            
        } catch (DpGrpcException e) {
            Assert.fail("Ingestion Service connection failed: " + e.getMessage());
            return;
        }
        
        // Create the interface implementation from the connection
        DpIngestionStreamImpl   istream = new DpIngestionStreamImpl(connIngest);
        Assert.assertFalse(istream.isStreamOpen());
        Assert.assertFalse(istream.isShutdown());
        Assert.assertFalse(istream.isTerminated());
        
        Assert.assertEquals(0, istream.getQueueSize());

        // Perform a hard shutdown
        boolean bolResult = istream.shutdownNow();
        Assert.assertTrue(bolResult);
        Assert.assertTrue(istream.isShutdown());
        Assert.assertTrue(istream.isTerminated());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#awaitTermination(long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testAwaitTerminationLongTimeUnit() {
        fail("Not yet implemented"); // TODO
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#isShutdown()}.
//     */
//    @Test
//    public final void testIsShutdown() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#isTerminated()}.
//     */
//    @Test
//    public final void testIsTerminated() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#setFrameDecompConcurrency(int)}.
     */
    @Test
    public final void testSetFrameDecompConcurrency() {

        // Parameters
        final int   cntThreads = 3; 
        
        try {
            INGEST_TEST.disableDecompConcurrency();
            INGEST_TEST.setFrameDecompConcurrency(cntThreads);

        } catch (IllegalStateException e) {
            Assert.fail("Test interface was in illegal state: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#setFrameDecomposition(long)}.
     */
    @Test
    public final void testSetFrameDecomposition() {
        
        // Parameters
        final   int     szFrameAlloc = 4_000_000;
        
        INGEST_TEST.disableFrameDecomposition();
        INGEST_TEST.setFrameDecomposition(szFrameAlloc);
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#disableFrameDecomposition()}.
//     */
//    @Test
//    public final void testDisableFrameDecomposition() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#disableDecompConcurrency()}.
//     */
//    @Test
//    public final void testDisableDecompConcurrency() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#setStagingCapcity(long)}.
     */
    @Test
    public final void testSetStagingCapcity() {
        
        // Parameters
        final long      szCapacity = 400_000_000L;
        
        INGEST_TEST.setStagingCapcity(szCapacity);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#enableBackPressure()}.
     */
    @Test
    public final void testEnableBackPressure() {
        INGEST_TEST.disableBackPressure();
        INGEST_TEST.enableBackPressure();
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#disableBackPressure()}.
//     */
//    @Test
//    public final void testDisableBackPressure() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#setStreamType(com.ospreydcs.dp.api.model.DpGrpcStreamType)}.
     */
    @Test
    public final void testSetStreamType() {
        
        // Parameters
        final DpGrpcStreamType  enmTypeGood = DpGrpcStreamType.BIDIRECTIONAL;
        final DpGrpcStreamType  enmTypeBad = DpGrpcStreamType.BACKWARD;

        try {
            INGEST_TEST.setStreamType(enmTypeBad);
            Assert.fail("Interface accepted a bad stream type: " + enmTypeBad);
            
        } catch (IllegalStateException e) {
            Assert.fail("Test interface in bad state: " + e.getMessage());
            
        } catch (UnsupportedOperationException e) {
            // Should end up here
        }
        
        try {
            INGEST_TEST.setStreamType(enmTypeGood);
            
        } catch (IllegalStateException e) {
            Assert.fail("Test interface in bad state: " + e.getMessage());
            
        } catch (UnsupportedOperationException e) {
            Assert.fail("Interface failed to use a good stream type: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#setMultipleStreams(int)}.
     */
    @Test
    public final void testSetMultipleStreams() {
        
        // Parameters
        final int   cntStreams = 2;
        
        // Disable multi-streaming
        try {
            INGEST_TEST.disableMultipleStreams();
            
        } catch (IllegalStateException e) {
            Assert.fail("Test interface failed disable multiple data streams: " + e.getMessage());
        }
        
        // Enable multi-streaming
        try {
            INGEST_TEST.setMultipleStreams(cntStreams);
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            Assert.fail("Test interface failed to set data stream count " + cntStreams);
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#disableMultipleStreams()}.
//     */
//    @Test
//    public final void testDisableMultipleStreams() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#getProviderUid()}.
     */
    @Test
    public final void testGetProviderUid() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#getQueueSize()}.
     */
    @Test
    public final void testGetQueueSize() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#getClientRequestIds()}.
     */
    @Test
    public final void testGetClientRequestIds() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#getIngestionExceptions()}.
     */
    @Test
    public final void testGetIngestionExceptions() {
        fail("Not yet implemented"); // TODO
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#openStream(com.ospreydcs.dp.api.model.ProviderRegistrar)}.
//     */
//    @Test
//    public final void testOpenStream() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#ingest(com.ospreydcs.dp.api.ingest.IngestionFrame)}.
     */
    @Test
    public final void testIngestIngestionFrame() {
        
        // Parameters
        final int   cntRows = 1000;
        final int   cntCols = 100;
        
        
        // Open the connection to the Ingestion Service
        DpIngestionConnection connIngest;
        try {
            connIngest = DpIngestionConnectionFactory.FACTORY.connect();
            
        } catch (DpGrpcException e) {
            Assert.fail("Ingestion Service connection failed: " + e.getMessage());
            return;
        }
        
        // Create the interface implementation from the connection
        DpIngestionStreamImpl   istream = new DpIngestionStreamImpl(connIngest);
        
        // Open the interface stream
        try {
            REC_PRVR_UID = istream.openStream(REC_PRVR_REG);
            Assert.assertTrue(istream.isStreamOpen());
            
        } catch (DpIngestionException e) {
            istream.shutdownNow();
            
            Assert.fail("The interface failed openStream(): " + e.getMessage());
            return;
        }
        
        // Create an ingestion frame and transmit it
        IngestionFrame  frame = TestIngestionFrameGenerator.createDoublesFrameWithClock(cntCols, cntRows);
        
        try {
            istream.ingest(frame);
            
        } catch (IllegalStateException | InterruptedException | DpIngestionException e) {
            Assert.fail("Data ingest() exception: type=" + e.getClass().getSimpleName() + ", detail=" + e.getMessage());
            
        }
        
        // Close the interface stream
        try {
            List<IngestionResponse> lstRsps = istream.closeStream();
            Assert.assertFalse(istream.isStreamOpen());
            Assert.assertEquals(1, lstRsps.size());
            
        } catch (IllegalStateException | CompletionException | InterruptedException e) {
            istream.shutdownNow();
            Assert.fail("The interface failed closeStream(): type=" + e.getClass().getSimpleName() + ", detail=" + e.getMessage());
            return;
        }
        
        // Shut down the interface
        try {
            Instant insStart = Instant.now();
            boolean bolResult = istream.shutdown();
            Instant insFinish = Instant.now();
            
            Assert.assertTrue(bolResult);
            Assert.assertTrue(istream.isShutdown());
            
            System.out.println(JavaRuntime.getQualifiedCallerNameSimple() + "- Shutdown took " + Duration.between(insStart, insFinish));
            
        } catch (InterruptedException e) {
            Assert.fail("Interface failed shutdown(): " + e.getMessage());
            return;
        }
        
        // Wait for everything to terminate
        try {
            boolean bolResult = istream.awaitTermination(LNG_TIMEOUT_WAIT, TU_TIMEOUT_WAIT);
            Assert.assertTrue(bolResult);
            Assert.assertTrue(istream.isTerminated());
            
        } catch (InterruptedException e) {
            Assert.fail("Interrupted while waiting for interface termination: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#ingest(java.util.List)}.
     */
    @Test
    public final void testIngestListOfIngestionFrame() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#awaitQueueReady()}.
     */
    @Test
    public final void testAwaitQueueReady() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#awaitQueueEmpty()}.
     */
    @Test
    public final void testAwaitQueueEmpty() {
        fail("Not yet implemented"); // TODO
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#closeStream()}.
//     */
//    @Test
//    public final void testCloseStream() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#closeStreamNow()}.
     */
    @Test
    public final void testCloseStreamNow() {
        fail("Not yet implemented"); // TODO
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#isStreamOpen()}.
//     */
//    @Test
//    public final void testIsStreamOpen() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#awaitTermination()}.
//     */
//    @Test
//    public final void testAwaitTermination() {
//        fail("Not yet implemented"); // TODO
//    }

}
