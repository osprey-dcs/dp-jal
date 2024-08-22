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


import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnectionFactory;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.ingest.DpIngestionException;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.test.TestIngestionFrameGenerator;
import com.ospreydcs.dp.api.model.ClientRequestUID;
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
    public static final ProviderRegistrar      REC_PRVR_REG = ProviderRegistrar.from(STR_PRVR_NAME);
    
    
    /** The Data Provider UID obtained from the Ingestion Service */
    private static ProviderUID                  REC_PRVR_UID = null;
    
    
    /** Event Logger */
    private static final Logger                 LOGGER = LogManager.getLogger(); 

    
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
     * <p>
     * Includes
     * <br/>
     * {@link DpIngestionStreamImpl#openStream(ProviderRegistrar)}
     * <br/>
     * {@link DpIngestionStreamImpl#closeStream()}
     * <br/>
     * {@link DpIngestionStreamImpl#shutdown()}
     * <br/>
     * {@link DpIngestionStreamImpl#awaitTermination()}
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
     * <p>
     * Includes
     * <br/>
     * {@link DpIngestionStreamImpl#isShutdown()}
     * <br/>
     * {@link DpIngestionStreamImpl#isTerminated()}
     */
    @Test
    public final void testAwaitTerminationLongTimeUnit() {
    

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
        
        // We time everything
        Instant insStart, insOpened, insClosed, insShutdown, insTerminated;
        
        insStart = Instant.now();
        
        // Open the interface stream
        try {
            REC_PRVR_UID = istream.openStream(REC_PRVR_REG);
            insOpened = Instant.now();
            Assert.assertTrue(istream.isStreamOpen());
            
        } catch (DpIngestionException e) {
            istream.shutdownNow();
            
            Assert.fail("The interface failed openStream(): " + e.getMessage());
            return;
        }
        
        // Close the interface stream
        try {
            List<IngestionResponse> lstRsps = istream.closeStream();
            insClosed = Instant.now();
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
            insShutdown = Instant.now();
            Assert.assertTrue(bolResult);
            Assert.assertTrue(istream.isShutdown());
            
        } catch (InterruptedException e) {
            Assert.fail("Interface failed shutdown(): " + e.getMessage());
            return;
        }
        
        // Wait for everything to terminate
        try {
            boolean bolResult = istream.awaitTermination(LNG_TIMEOUT_WAIT, TU_TIMEOUT_WAIT);
            insTerminated = Instant.now();
            Assert.assertTrue(bolResult);
            Assert.assertTrue(istream.isTerminated());
            
        } catch (InterruptedException e) {
            Assert.fail("Interrupted while waiting for interface termination: " + e.getMessage());
            return;
        }
        
        // Print out results
        System.out.println(JavaRuntime.getQualifiedCallerNameSimple());
        System.out.println("  Time to open stream  : " + Duration.between(insStart, insOpened));
        System.out.println("  Time to close stream : " + Duration.between(insOpened, insClosed));
        System.out.println("  Time to shutdown     : " + Duration.between(insClosed, insShutdown));
        System.out.println("  Time for termination : " + Duration.between(insShutdown, insTerminated));
        System.out.println("  Total test time      : " + Duration.between(insStart, insTerminated));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#setFrameProcessingConcurrency(int)}.
     * <p>
     * Includes
     * <br/>
     * {@link DpIngestionStreamImpl#disableFrameProcessingConcurrency()}
     */
    @Test
    public final void testSetFrameProcessingConcurrency() {

        // Parameters
        final int   cntThreads = 3; 
        
        try {
            INGEST_TEST.disableFrameProcessingConcurrency();
            INGEST_TEST.setFrameProcessingConcurrency(cntThreads);

        } catch (IllegalStateException e) {
            Assert.fail("Test interface was in illegal state: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#setFrameDecomposition(long)}.
     * <p>
     * Includes
     * <br/>
     * {@link DpIngestionStreamImpl#disableFrameDecomposition()}
     */
    @Test
    public final void testSetFrameDecomposition() {
        
        // Parameters
        final   int     szFrameAlloc = 4_000_000;
        
        INGEST_TEST.disableFrameDecomposition();
        INGEST_TEST.setFrameDecomposition(szFrameAlloc);
    }

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
     * <p>
     * Includes
     * {@link DpIngestionStreamImpl#disableBackPressure()}
     */
    @Test
    public final void testEnableBackPressure() {
        INGEST_TEST.disableBackPressure();
        INGEST_TEST.enableBackPressure();
    }

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
     * <p>
     * Includes
     * {@link DpIngestionStreamImpl#disableMultipleStreams()}
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

//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#getProviderUid()}.
//     */
////    @Test
//    public final void testGetProviderUid() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#getQueueSize()}.
//     */
////    @Test
//    public final void testGetQueueSize() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#getClientRequestIds()}.
//     */
//    @Test
//    public final void testGetClientRequestIds() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#getIngestionExceptions()}.
//     */
//    @Test
//    public final void testGetIngestionExceptions() {
//        fail("Not yet implemented"); // TODO
//    }
//
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
        final int               cntRows = 1000;
        final int               cntCols = 100;
        final IngestionFrame    frame = TestIngestionFrameGenerator.createDoublesFrameWithClock(cntCols, cntRows);
        
        
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
        
        // Time the data transmission
        Instant insStart, insClosed, insShutdown;
        
        // Transmit single ingestion frame 
        try {
            insStart = Instant.now();
            istream.ingest(frame);
            
        } catch (IllegalStateException | InterruptedException | DpIngestionException e) {
            istream.shutdownNow();
            Assert.fail("Data ingest() exception: type=" + e.getClass().getSimpleName() + ", detail=" + e.getMessage());
            return;
        }
        
        // Close the interface stream
        try {
            List<IngestionResponse> lstRsps = istream.closeStream();
            insClosed = Instant.now();
            Assert.assertFalse(istream.isStreamOpen());
            Assert.assertEquals(1, lstRsps.size());
            
        } catch (IllegalStateException | CompletionException | InterruptedException e) {
            istream.shutdownNow();
            Assert.fail("The interface failed closeStream(): type=" + e.getClass().getSimpleName() + ", detail=" + e.getMessage());
            return;
        }
        
        // Shut down the interface
        try {
            boolean bolResult = istream.shutdown();
            insShutdown = Instant.now();
            
            Assert.assertTrue(bolResult);
            Assert.assertTrue(istream.isShutdown());
            
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
        
        System.out.println(JavaRuntime.getQualifiedCallerNameSimple());
        System.out.println("  Single frame - allocaton : " + frame.allocationSizeFrame());
        System.out.println("  Transmission time        : " + Duration.between(insStart, insClosed));
        System.out.println("  Shutdown time            : " + Duration.between(insClosed, insShutdown));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#ingest(java.util.List)}.
     */
    @Test
    public final void testIngestListOfIngestionFrame() {
        
        // Parameters
        //  Use payload that requires decomposition
        final int   cntFrms = 3;
        final int   cntRows = 1000;
        final int   cntCols = 1000;
        final List<IngestionFrame>  lstFrms1 = TestIngestionFrameGenerator.createDoublesPayloadWithClock(cntFrms, cntCols, cntRows, true);
//        final List<IngestionFrame>  lstFrms2 = TestIngestionFrameGenerator.createDoublesPayloadWithClock(cntFrms, cntCols, cntRows, true);
        
        final long  szPayload = lstFrms1.stream().mapToLong(IngestionFrame::allocationSizeFrame).sum();
        final long  szCapacity = szPayload/3;
        final int   cntThreads = 4;
        final int   cntStreams = 2;
//        final int   intIngestPause = 15; 
//        final int   intTestWait = 400;
        
        
        // Open the connection to the Ingestion Service
        DpIngestionConnection connIngest;
        try {
            connIngest = DpIngestionConnectionFactory.FACTORY.connect();
            
        } catch (DpGrpcException e) {
            Assert.fail("Ingestion Service connection failed: " + e.getMessage());
            return;
        }
        
        // Create the interface implementation from the connection and configure it
        DpIngestionStreamImpl   istream = new DpIngestionStreamImpl(connIngest);
        istream.setFrameProcessingConcurrency(cntThreads);
        istream.setStagingCapcity(szCapacity);
        istream.disableBackPressure();
        istream.setMultipleStreams(cntStreams);
        
        // Open the interface stream
        try {
            REC_PRVR_UID = istream.openStream(REC_PRVR_REG);
            Assert.assertTrue(istream.isStreamOpen());
            
        } catch (DpIngestionException e) {
            istream.shutdownNow();
            
            Assert.fail("The interface failed openStream(): " + e.getMessage());
            return;
        }
        
        // Time everything
        Instant insStart, insClosed, insShutdown, insTerminated;
        
        // ------------ Data transmission and waiting testing ------------------ 
        try {
            
            // Transmit payload 
            insStart = Instant.now();
            istream.ingest(lstFrms1);
//            istream.ingest(lstFrms2);
            
        } catch (IllegalStateException | InterruptedException | DpIngestionException e) {
            istream.shutdownNow();
            Assert.fail("Data ingest() exception: type=" + e.getClass().getSimpleName() + ", detail=" + e.getMessage());
            return;
        }
        
        // Close the interface stream
        try {
            istream.closeStream();
            insClosed = Instant.now();
            
            Assert.assertFalse(istream.isStreamOpen());
            
        } catch (IllegalStateException | CompletionException | InterruptedException e) {
            istream.shutdownNow();
            Assert.fail("The interface failed closeStream(): type=" + e.getClass().getSimpleName() + ", detail=" + e.getMessage());
            return;
        }
        
        // Get the transmission results
        ProviderUID             recPrvrUid = istream.getProviderUid();
        List<ClientRequestUID>  lstRqstUids = istream.getClientRequestIds();
        int                     cntXmissions = istream.getTransmissionCount();
        List<IngestionResponse> lstRsps = istream.getIngestionResponses();
        List<IngestionResponse> lstXmitExcps = istream.getIngestionExceptions();
        Collection<Exception>   setFrmDecompExcp = istream.getFailedFrameDecompositions();
        Collection<Exception>   setFrmCnvrtExcp = istream.getFailedFrameConversions();
        
        // Shut down the interface
        try {
            boolean bolResult = istream.shutdown();
            insShutdown = Instant.now();
            Assert.assertTrue(bolResult);
            Assert.assertTrue(istream.isShutdown());
            
        } catch (InterruptedException e) {
            Assert.fail("Interface failed shutdown(): " + e.getMessage());
            return;
        }
        
        // Wait for everything to terminate
        try {
            boolean bolResult = istream.awaitTermination(LNG_TIMEOUT_WAIT, TU_TIMEOUT_WAIT);
            insTerminated = Instant.now();
            Assert.assertTrue(bolResult);
            Assert.assertTrue(istream.isTerminated());
            
        } catch (InterruptedException e) {
            Assert.fail("Interrupted while waiting for interface termination: " + e.getMessage());
            return;
        }
        
        System.out.println(JavaRuntime.getQualifiedCallerNameSimple() + "Results");
        System.out.println("  Payload allocation (bytes)      : " + szPayload);
        System.out.println("  Staging capacity (bytes)        : " + szCapacity);
        System.out.println("  Transmission time (open stream) : " + Duration.between(insStart, insClosed));
        System.out.println("  Shutdown time                   : " + Duration.between(insClosed, insShutdown));
        System.out.println("  Termination time                : " + Duration.between(insShutdown, insTerminated));
        System.out.println("  Total API active time           : " + Duration.between(insStart, insTerminated));
        System.out.println("  Messages transmitted            : " + cntXmissions);
        System.out.println("  Responses received              : " + lstRsps.size());
        System.out.println("  Data Provider UID               : " + recPrvrUid);
        System.out.println("  Client request UIDs             : " + lstRqstUids);
        System.out.println("  Exceptional responses           : " + lstXmitExcps);
        System.out.println("  Failed frame decompsitions      : " + setFrmDecompExcp);
        System.out.println("  Failed frame conversions        : " + setFrmCnvrtExcp);
        System.out.println("  All Ingestion Service responses : " + lstRsps);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#awaitQueueReady()}.
     */
    @Test
    public final void testAwaitQueueReady() {
        
        // Parameters
        //  Use payload that does not require decomposition
        final int   cntFrms = 150;
        final int   cntRows = 1000;
        final int   cntCols = 100;
        final List<IngestionFrame>  lstFrms1 = TestIngestionFrameGenerator.createDoublesPayloadWithClock(cntFrms, cntCols, cntRows, true);
        final List<IngestionFrame>  lstFrms2 = TestIngestionFrameGenerator.createDoublesPayloadWithClock(cntFrms, cntCols, cntRows, true);
        final IngestionFrame        frame = TestIngestionFrameGenerator.createDoublesFrameWithClock(cntCols, cntRows);
        
        final long  szPayload = lstFrms1.stream().mapToLong(IngestionFrame::allocationSizeFrame).sum();
        final long  szCapacity = szPayload/20;
        final int   cntThreads = 4;
        final int   cntStreams = 2;
        final int   intIngestPause = 15; 
        final int   intTestWait = 400;
        
        
        // Open the connection to the Ingestion Service
        DpIngestionConnection connIngest;
        try {
            connIngest = DpIngestionConnectionFactory.FACTORY.connect();
            
        } catch (DpGrpcException e) {
            Assert.fail("Ingestion Service connection failed: " + e.getMessage());
            return;
        }
        
        // Create the interface implementation from the connection and configure it
        DpIngestionStreamImpl   istream = new DpIngestionStreamImpl(connIngest);
        istream.setFrameProcessingConcurrency(cntThreads);
        istream.disableFrameDecomposition();
        istream.setStagingCapcity(szCapacity);
        istream.disableBackPressure();
        istream.setMultipleStreams(cntStreams);
        
        // Open the interface stream
        try {
            REC_PRVR_UID = istream.openStream(REC_PRVR_REG);
            Assert.assertTrue(istream.isStreamOpen());
            
        } catch (DpIngestionException e) {
            istream.shutdownNow();
            
            Assert.fail("The interface failed openStream(): " + e.getMessage());
            return;
        }
        
        // ------------ Data transmission and waiting testing ------------------ 
        Instant insStart = Instant.now();
        try {
            
            // Transmit first payload then wait for queue ready 
            istream.ingest(lstFrms1);
            Thread.sleep(intIngestPause);
            istream.ingest(lstFrms2);
            istream.ingest(frame);
            
            LOGGER.debug("Ingestion submitted; waiting for {} milliseconds, queue size = {}.", intTestWait,  istream.getQueueSize());
            Thread.sleep(intTestWait);
            
            Instant insReadyStart = Instant.now();
            LOGGER.debug("Starting awaitQueueReady(): queue size = {}, queue allocation = {}, queue capacity = {}.", istream.getQueueSize(), istream.getQueueAllocation(), szCapacity);
            istream.awaitQueueReady();
            Instant insReadyUnblock = Instant.now();

            Instant insEmptyStart = Instant.now();
            LOGGER.debug("Starting awaitQueueEmpty(): queue size = {}, queue allocation = {}.", istream.getQueueSize(), istream.getQueueAllocation());
            istream.awaitQueueEmpty();
            Instant insEmptyUnblock = Instant.now();
            LOGGER.debug("awaitQueueEmpty() returned: queue size = {}.", istream.getQueueSize());
            
            Duration    durReady = Duration.between(insReadyStart, insReadyUnblock);
            Duration    durEmpty = Duration.between(insEmptyStart, insEmptyUnblock);
            
            System.out.println(JavaRuntime.getQualifiedCallerNameSimple() + "Results");
            System.out.println("  Payload allocation (bytes) : " + szPayload);
            System.out.println("  Staging capacity (bytes)   : " + szCapacity);
            System.out.println("  Queue ready wait time      : " + durReady);
            System.out.println("  Queue empty wait time      : " + durEmpty);
            
        } catch (IllegalStateException | InterruptedException | DpIngestionException e) {
            Assert.fail("Data ingest() exception: type=" + e.getClass().getSimpleName() + ", detail=" + e.getMessage());
            
        }
        
        // Close the interface stream
        try {
            List<IngestionResponse> lstRsps = istream.closeStream();
            Instant     insClosed = Instant.now();
            
            System.out.println("  Messages transmitted       : " + istream.getTransmissionCount());
            System.out.println("  Responses received         : " + lstRsps.size());
            System.out.println("  Total time stream open     : " + Duration.between(insStart, insClosed));

            Assert.assertFalse(istream.isStreamOpen());
//            Assert.assertEquals(1, lstRsps.size());
            
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
            boolean bolResult = istream.awaitTermination(LNG_TIMEOUT_WAIT, TU_TIMEOUT_WAIT);
            Assert.assertTrue(bolResult);
            Assert.assertTrue(istream.isTerminated());
            
        } catch (InterruptedException e) {
            Assert.fail("Interrupted while waiting for interface termination: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#awaitQueueEmpty()}.
     */
    @Test
    public final void testAwaitQueueEmpty() {
        
        // Parameters
        //  Use payload that requires decomposition
        final int   cntFrms = 15;
        final int   cntRows = 1000;
        final int   cntCols = 1000;
        final List<IngestionFrame>  lstFrms1 = TestIngestionFrameGenerator.createDoublesPayloadWithClock(cntFrms, cntCols, cntRows, true);
        final List<IngestionFrame>  lstFrms2 = TestIngestionFrameGenerator.createDoublesPayloadWithClock(cntFrms, cntCols, cntRows, true);
        final IngestionFrame        frame = TestIngestionFrameGenerator.createDoublesFrameWithClock(cntCols, cntRows);
        
        final long  szPayload = lstFrms1.stream().mapToLong(IngestionFrame::allocationSizeFrame).sum();
        final long  szCapacity = szPayload/5;
        final int   cntThreads = 4;
        final int   cntStreams = 2;
        final int   intIngestPause = 15; 
        final int   intTestWait = 400;
        
        
        // Open the connection to the Ingestion Service
        DpIngestionConnection connIngest;
        try {
            connIngest = DpIngestionConnectionFactory.FACTORY.connect();
            
        } catch (DpGrpcException e) {
            Assert.fail("Ingestion Service connection failed: " + e.getMessage());
            return;
        }
        
        // Create the interface implementation from the connection and configure it
        DpIngestionStreamImpl   istream = new DpIngestionStreamImpl(connIngest);
        istream.setFrameProcessingConcurrency(cntThreads);
        istream.setStagingCapcity(szCapacity);
        istream.disableBackPressure();
        istream.setMultipleStreams(cntStreams);
        
        // Open the interface stream
        try {
            REC_PRVR_UID = istream.openStream(REC_PRVR_REG);
            Assert.assertTrue(istream.isStreamOpen());
            
        } catch (DpIngestionException e) {
            istream.shutdownNow();
            
            Assert.fail("The interface failed openStream(): " + e.getMessage());
            return;
        }
        
        // ------------ Data transmission and waiting testing ------------------ 
        Instant insStart = Instant.now();
        try {
            
            // Transmit first payload then wait for queue ready 
            istream.ingest(lstFrms1);
            Thread.sleep(intIngestPause);
            istream.ingest(lstFrms2);
            istream.ingest(frame);
            
            LOGGER.debug("Ingestion submitted; waiting for {} milliseconds, queue size = {}.", intTestWait,  istream.getQueueSize());
            Thread.sleep(intTestWait);

            Instant insReadyStart = Instant.now();
            LOGGER.debug("Starting awaitQueueReady(): queue size = {}, queue allocation = {}.", istream.getQueueSize(), istream.getQueueAllocation());;
            istream.awaitQueueReady();
            Instant insReadyUnblock = Instant.now();

            Instant insEmptyStart = Instant.now();
            LOGGER.debug("Starting awaitQueueEmpty(): queue size = {}.", istream.getQueueSize());
            istream.awaitQueueEmpty();
            Instant insEmptyUnblock = Instant.now();
            LOGGER.debug("awaitQueueEmpty() returned: queue size = {}.", istream.getQueueSize());
            
            Duration    durReady = Duration.between(insReadyStart, insReadyUnblock);
            Duration    durEmpty = Duration.between(insEmptyStart, insEmptyUnblock);
            
            System.out.println(JavaRuntime.getQualifiedCallerNameSimple() + "Results");
            System.out.println("  Payload allocation (bytes) : " + szPayload);
            System.out.println("  Staging capacity (bytes)   : " + szCapacity);
            System.out.println("  Queue ready wait time      : " + durReady);
            System.out.println("  Queue empty wait time      : " + durEmpty);
            
        } catch (IllegalStateException | InterruptedException | DpIngestionException e) {
            Assert.fail("Data ingest() exception: type=" + e.getClass().getSimpleName() + ", detail=" + e.getMessage());
            
        }
        
        // Close the interface stream
        try {
            List<IngestionResponse> lstRsps = istream.closeStream();
            Instant     insClosed = Instant.now();
            
            System.out.println("  Messages transmitted       : " + istream.getTransmissionCount());
            System.out.println("  Responses received         : " + lstRsps.size());
            System.out.println("  Total time stream open     : " + Duration.between(insStart, insClosed));

            Assert.assertFalse(istream.isStreamOpen());
            
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
            boolean bolResult = istream.awaitTermination(LNG_TIMEOUT_WAIT, TU_TIMEOUT_WAIT);
            Assert.assertTrue(bolResult);
            Assert.assertTrue(istream.isTerminated());
            
        } catch (InterruptedException e) {
            Assert.fail("Interrupted while waiting for interface termination: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#closeStream()}.
     */
    @Test
    public final void testCloseStream() {
        
        // Parameters
        final int   cntFrms = 15;
        final int   cntRows = 1000;
        final int   cntCols = 1000;
        final List<IngestionFrame>  lstFrms1 = TestIngestionFrameGenerator.createDoublesPayloadWithClock(cntFrms, cntCols, cntRows, true);
        
        final long  szPayload = lstFrms1.stream().mapToLong(IngestionFrame::allocationSizeFrame).sum();
        final long  szCapacity = szPayload/3;
        final int   cntThreads = 3;
        final int   intWait = 500;
        
        
        // Open the connection to the Ingestion Service
        DpIngestionConnection connIngest;
        try {
            connIngest = DpIngestionConnectionFactory.FACTORY.connect();
            
        } catch (DpGrpcException e) {
            Assert.fail("Ingestion Service connection failed: " + e.getMessage());
            return;
        }
        
        // Create the interface implementation from the connection and configure it
        DpIngestionStreamImpl   istream = new DpIngestionStreamImpl(connIngest);
        istream.setFrameProcessingConcurrency(cntThreads);
        istream.setStagingCapcity(szCapacity);
        istream.disableBackPressure();
        
        // Open the interface stream
        try {
            REC_PRVR_UID = istream.openStream(REC_PRVR_REG);
            Assert.assertTrue(istream.isStreamOpen());
            
        } catch (DpIngestionException e) {
            istream.shutdownNow();
            
            Assert.fail("The interface failed openStream(): " + e.getMessage());
            return;
        }
        
        // ------------ Data transmission and waiting testing ------------------ 
        try {
            
            // Transmit first payload then wait for queue ready 
            istream.ingest(lstFrms1);
            
        } catch (IllegalStateException | InterruptedException | DpIngestionException e) {
            Assert.fail("Data ingest() exception: type=" + e.getClass().getSimpleName() + ", detail=" + e.getMessage());
            
        }
        
        // Close the interface stream
        try {
            Instant insStart = Instant.now();
            List<IngestionResponse> lstRsps = istream.closeStream();
            Instant insClosed = Instant.now();
            
            Duration    durClose = Duration.between(insStart, insClosed);
            
            System.out.println(JavaRuntime.getQualifiedCallerNameSimple() + "Results");
            System.out.println("  Payload allocation (bytes) : " + szPayload);
            System.out.println("  Staging capacity (bytes)   : " + szCapacity);
            System.out.println("  Close stream wait time     : " + durClose);
            System.out.println("  Messages transmitted       : " + istream.getTransmissionCount());
            System.out.println("  Responses received         : " + lstRsps.size());
            
            Assert.assertFalse(istream.isStreamOpen());
//            Assert.assertEquals(1, lstRsps.size());
            
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamImpl#closeStreamNow()}.
     */
    @Test
    public final void testCloseStreamNow() {
        
        // Parameters
        final int   cntFrms = 15;
        final int   cntRows = 1000;
        final int   cntCols = 1000;
        final List<IngestionFrame>  lstFrms1 = TestIngestionFrameGenerator.createDoublesPayloadWithClock(cntFrms, cntCols, cntRows, true);
        
        final long  szPayload = lstFrms1.stream().mapToLong(IngestionFrame::allocationSizeFrame).sum();
        final long  szCapacity = szPayload/3;
        final int   cntThreads = 3;
        final int   intWait = 500;
        
        
        // Open the connection to the Ingestion Service
        DpIngestionConnection connIngest;
        try {
            connIngest = DpIngestionConnectionFactory.FACTORY.connect();
            
        } catch (DpGrpcException e) {
            Assert.fail("Ingestion Service connection failed: " + e.getMessage());
            return;
        }
        
        // Create the interface implementation from the connection and configure it
        DpIngestionStreamImpl   istream = new DpIngestionStreamImpl(connIngest);
        istream.setFrameProcessingConcurrency(cntThreads);
        istream.setStagingCapcity(szCapacity);
        istream.disableBackPressure();
        
        // Open the interface stream
        try {
            istream.openStream(REC_PRVR_REG);
            Assert.assertTrue(istream.isStreamOpen());
            
        } catch (DpIngestionException e) {
            istream.shutdownNow();
            
            Assert.fail("The interface failed openStream(): " + e.getMessage());
            return;
        }
        
        // ------------ Data transmission and waiting testing ------------------ 
        try {
            
            // Transmit first payload then wait for queue ready 
            istream.ingest(lstFrms1);
            
        } catch (IllegalStateException | InterruptedException | DpIngestionException e) {
            Assert.fail("Data ingest() exception: type=" + e.getClass().getSimpleName() + ", detail=" + e.getMessage());
            
        }
        
        // Pause - hoping for a partial transmission
        try {
            Thread.sleep(intWait);
            
        } catch (InterruptedException e) {
            istream.shutdownNow();
            Assert.fail("Interrupted waiting pausing before closeStreamNow().");
            return;
        }

        // Close the interface stream
        LOGGER.debug("Issuing a hard close operation after pausing {} milliseconds: queue size = {}, messages transmitted = {}", intWait, istream.getQueueSize(), istream.getTransmissionCount());
        
        Instant insStart = Instant.now();
        boolean bolClosed = istream.closeStreamNow();
        Instant insClosed = Instant.now();

        // Get results
        Duration    durClose = Duration.between(insStart, insClosed);
        List<IngestionResponse> lstRsps = istream.getIngestionResponses();
        List<IngestionResponse> lstExcps = istream.getIngestionExceptions();
        List<ClientRequestUID>  lstRqstUids = istream.getClientRequestIds();
        Collection<Exception>   setFrmDecmpExcp = istream.getFailedFrameDecompositions();
        Collection<Exception>   setFrmCnvrtExcp = istream.getFailedFrameConversions();

        System.out.println(JavaRuntime.getQualifiedCallerNameSimple() + "Results");
        System.out.println("  Payload allocation (bytes) : " + szPayload);
        System.out.println("  Staging capacity (bytes)   : " + szCapacity);
        System.out.println("  Hard Close stream time     : " + durClose);
        System.out.println("  Messages transmitted       : " + istream.getTransmissionCount());
        System.out.println("  Responses received         : " + lstRsps.size());
        System.out.println("  Transmission exceptions    : " + lstExcps.size());
        System.out.println("  Client Request UIDs        : " + lstRqstUids.size());
        System.out.println("  Frame decompsition errors  : " + setFrmDecmpExcp);
        System.out.println("  Frame conversion errors    : " + setFrmCnvrtExcp);

        Assert.assertFalse(istream.isStreamOpen());
        Assert.assertTrue(bolClosed);

        
        // Shut down the interface
        try {
            boolean bolResult = istream.shutdown();
            Instant insShutdown = Instant.now();
            
            System.out.println("  Shutdown time              :" + Duration.between(insStart, insShutdown));
            
            Assert.assertTrue(bolResult);
            Assert.assertTrue(istream.isShutdown());
            
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
