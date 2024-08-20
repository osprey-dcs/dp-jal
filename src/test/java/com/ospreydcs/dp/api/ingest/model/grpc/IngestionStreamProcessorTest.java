/*
 * Project: dp-api-common
 * File:	IngestionStreamProcessorTest.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IngestionStreamProcessorTest
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
 * @since Apr 18, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.grpc;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnectionFactory;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.test.TestIngestionFrameGenerator;
import com.ospreydcs.dp.api.model.ClientRequestUID;
import com.ospreydcs.dp.api.model.DpGrpcStreamType;
import com.ospreydcs.dp.api.model.IngestionResponse;
import com.ospreydcs.dp.api.model.ProviderRegistrar;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.RegisterProviderRequest;
import com.ospreydcs.dp.grpc.v1.ingestion.RegisterProviderResponse;

/**
 * <p>
 * JUnit test cases for class <code>IngestionStreamProcessor</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 18, 2024
 *
 */
public class IngestionStreamProcessorTest {

    
    //
    // Class Constants
    //
    
    
    /** Ingestion Service registerProvider() implemented */
    private static final boolean            BOL_PROVIDER_REG_IMPL = false;
    
    /** Default data provider UID (i.e., if registerProvider() not implemented) */
    private static final int                INT_PROVIDER_UID = 42;
    
    
    /** Data Provider unique name */
    private static final String             STR_PROVIDER_NAME = IngestionStreamProcessorTest.class.getSimpleName();
    
    /** Data provider registration record */
    private static final ProviderRegistrar  REC_PROVIDER_REGISTRAR = ProviderRegistrar.from(STR_PROVIDER_NAME);;
    
    
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
    

    // 
    // Test Fixture Resources
    //
    
    /** The single connection to the Ingestion Service used by all test cases */
    private static DpIngestionConnection    CONN_INGEST;
    
    /** Data provider UID used for testing */
    private static ProviderUID              REC_PROVIDER_UID;

    
    /** A <code>IngestionStreamProcessor</code> instance available for testing */ 
    private IngestionStreamProcessor        processor;
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // Obtain the Ingestion Service connection - terminating unit test if failure
        try {

            CONN_INGEST = DpIngestionConnectionFactory.FACTORY.connect();
            
        } catch (DpGrpcException e) {
            String      strMsg = "Unable to obtain a DpIngestionConnection from connection factory.";
         
            Assert.fail(strMsg);
            System.err.println(strMsg);
            System.exit(1);
        }

        // Obtain a data provider UID by registering with the Ingestion Service 
        RegisterProviderRequest msgRegRqst = RegisterProviderRequest
                .newBuilder()
                .setProviderName(STR_PROVIDER_NAME)
                .build();

        if (BOL_PROVIDER_REG_IMPL) {
            RegisterProviderResponse msgRegRsp = CONN_INGEST.getStubBlock().registerProvider(msgRegRqst);

            if (msgRegRsp.hasExceptionalResult()) {
                String      strMsg = "Provider registration failed: " + msgRegRsp.getExceptionalResult().getMessage();

                System.err.println(strMsg);
                Assert.fail(strMsg);
            }

            REC_PROVIDER_UID = ProviderUID.from(msgRegRsp.getRegistrationResult().getProviderId());

            System.out.println(JavaRuntime.getQualifiedCallerNameSimple() + " obtained provider UID " + REC_PROVIDER_UID + " from provider name " + STR_PROVIDER_NAME);
        
        } else {
            
            REC_PROVIDER_UID = ProviderUID.from(INT_PROVIDER_UID);
        }
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        CONN_INGEST.shutdownSoft();
        CONN_INGEST.awaitTermination();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        processor = IngestionStreamProcessor.from(CONN_INGEST);
        processor.activate(REC_PROVIDER_UID);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        processor.shutdown();
    }
    
    
    //
    // Test Cases
    //

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#IngestionStreamProcessor(com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection)}.
     */
    @Test
    public final void testIngestionStreamProcessor() {
        IngestionStreamProcessor processor   = IngestionStreamProcessor.from(CONN_INGEST);
        
        Assert.assertFalse(processor.isActive());
        
        processor.activate(REC_PROVIDER_UID);
        Assert.assertTrue(processor.isActive());
        
        try {
            boolean bolShutdown = processor.shutdown();
        
            Assert.assertTrue(bolShutdown);
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during shutdown() operation: " + e.getMessage());

        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#setStreamType(com.ospreydcs.dp.api.model.DpGrpcStreamType)}.
     */
    @Test
    public final void testSetStreamType() {
        
        DpGrpcStreamType    enmTypeInit = this.processor.getStreamType();
        
        try {
            
            // Already activated - should throw an exception
            this.processor.setStreamType(DpGrpcStreamType.FORWARD);
            Assert.fail("Activated processor should throw exception attempting to set stream type.");
            
        } catch (IllegalStateException e) {
            Assert.assertEquals(enmTypeInit, this.processor.getStreamType());
        }
        
        // Shutdown then set stream type
        
        try {
            this.processor.shutdown();
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during shutdown() operation: " + e.getMessage());
        }

        try {
            this.processor.setStreamType(DpGrpcStreamType.FORWARD);
            Assert.assertEquals(DpGrpcStreamType.FORWARD, this.processor.getStreamType());
            
            this.processor.setStreamType(DpGrpcStreamType.BIDIRECTIONAL);
            Assert.assertEquals(DpGrpcStreamType.BIDIRECTIONAL, this.processor.getStreamType());
            
            // This should throw an exception
            this.processor.setStreamType(DpGrpcStreamType.BACKWARD);
            Assert.fail("Setting the processor stream type to BACKWARD did not throw exception.");
        
        } catch (IllegalStateException e) {
            Assert.fail("Inactive processor threw IllegalStateException attempting to set stream type.");
            
        } catch (UnsupportedOperationException e) {
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#enableMultipleStreams(int)}.
     */
    @Test
    public final void testEnableMultipleStreams() {
        
        // Activated processor - this should throw exception
        try {
            this.processor.enableMultipleStreams(3);
            Assert.fail("Enabling multiple stream on active processor did not throw exception.");
            
        } catch (IllegalStateException e) {
            
        }
        
        try {
            this.processor.shutdown();
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during shutdown() operation: " + e.getMessage());
        }
        
        try {
            this.processor.enableMultipleStreams(3);
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            Assert.fail("Inactive processor threw exception while enabling multiple streams: " + e.getMessage() );
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#disableMultipleStreams()}.
     */
    @Test
    public final void testDisableMultipleStreams() {
        // Activated processor - this should throw exception
        try {
            this.processor.disableMultipleStreams();
            Assert.fail("Disabling multiple stream on active processor did not throw exception.");
            
        } catch (IllegalStateException e) {
            
        }
        
        try {
            this.processor.shutdown();
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during shutdown() operation: " + e.getMessage());
        }
        
        try {
            this.processor.disableMultipleStreams();
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            Assert.fail("Inactive processor threw exception while disabling multiple streams: " + e.getMessage() );
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#isActive()}.
     */
    @Test
    public final void testIsActive() {
        
        Assert.assertTrue(this.processor.isActive());
        
        try {
            this.processor.shutdown();
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during shutdown() operation: " + e.getMessage());
        }

        Assert.assertFalse(this.processor.isActive());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#getRequestCount()}.
     */
    @Test
    public final void testGetRequestsCount() {
        Assert.assertEquals(0, this.processor.getRequestCount());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#getResponseCount()}.
     */
    @Test
    public final void testGetResponseCount() {
        Assert.assertEquals(0,  this.processor.getResponseCount());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#getRequestIds()}.
     */
    @Test
    public final void testGetRequestIds() {

        // Parameters and resources
        List<IngestionFrame>    lstFrames = LST_FRAMES_MOD;
        final int               cntFrames = lstFrames.size();
        
        try {
            this.processor.transmit(lstFrames);
            
        } catch (IllegalStateException e) {
            Assert.fail("Processor threw IllegalStateException during transmit() operation: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during transmit() operation: " + e.getMessage());
        }
        
        // Shutdown processor and wait for all message transmissions
        try {
            boolean bolShutdown = this.processor.shutdown();
            Assert.assertTrue(bolShutdown);
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during shutdown() operation: " + e.getMessage());
        }

        try {
            List<ClientRequestUID>   lstIds = this.processor.getRequestIds();
            Assert.assertEquals(cntFrames, lstIds.size());
            
            List<String>    lstStrIds = lstIds.stream().<String>map(ClientRequestUID::requestId).toList();
            System.out.println("Client request IDs: " + lstStrIds);
            
        } catch (IllegalStateException e) {
            Assert.fail("Processor threw IllegalStateException during while requesting client IDs: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#getIngestionResponses()}.
     */
    @Test
    public final void testGetIngestionResponses() {

        // Parameters and resources
        List<IngestionFrame>    lstFrames = LST_FRAMES_MOD;
        final int               cntFrames = lstFrames.size();
        
        try {
            this.processor.transmit(lstFrames);
            
        } catch (IllegalStateException e) {
            Assert.fail("Processor threw IllegalStateException during transmit() operation: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during transmit() operation: " + e.getMessage());
        }
        
        // Shutdown processor and wait for all message transmissions
        try {
            boolean bolShutdown = this.processor.shutdown();
            Assert.assertTrue(bolShutdown);
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during shutdown() operation: " + e.getMessage());
        }

        try {
            List<IngestionResponse>   lstRsps = this.processor.getIngestionResponses();
            
            List<Integer>   lstPrvIds = lstRsps.stream().<Integer>map(IngestionResponse::providerId).toList(); 
            List<String>    lstCltIds = lstRsps.stream().<String>map(IngestionResponse::clientRequestId).toList();
            System.out.println("Provider IDs: " + lstPrvIds);
            System.out.println("Client request IDs: " + lstCltIds);
            
            Assert.assertEquals(cntFrames, lstRsps.size());
            Assert.assertTrue(lstRsps.stream().allMatch(rsp -> rsp.providerId() == REC_PROVIDER_UID.uid()) );
            
        } catch (IllegalStateException e) {
            Assert.fail("Processor threw IllegalStateException during while requesting responses: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#getIngestionExceptions()}.
     */
    @Test
    public final void testGetIngestionExceptions() {

        // Parameters and resources
        List<IngestionFrame>    lstFrames = LST_FRAMES_MOD;
        
        try {
            this.processor.transmit(lstFrames);
            
        } catch (IllegalStateException e) {
            Assert.fail("Processor threw IllegalStateException during transmit() operation: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during transmit() operation: " + e.getMessage());
        }
        
        // Shutdown processor and wait for all message transmissions
        try {
            boolean bolShutdown = this.processor.shutdown();
            Assert.assertTrue(bolShutdown);
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during shutdown() operation: " + e.getMessage());
        }

        try {
            List<IngestionResponse>   lstRsps = this.processor.getIngestionExceptions();
            
            List<Integer>   lstPrvIds = lstRsps.stream().<Integer>map(IngestionResponse::providerId).toList(); 
            List<String>    lstCltIds = lstRsps.stream().<String>map(IngestionResponse::clientRequestId).toList();
            System.out.println("Provider IDs: " + lstPrvIds);
            System.out.println("Client request IDs: " + lstCltIds);
            
            Assert.assertTrue(lstRsps.stream().allMatch(rsp -> rsp.providerId() == REC_PROVIDER_UID.uid()) );
            
        } catch (IllegalStateException e) {
            Assert.fail("Processor threw IllegalStateException during while requesting exceptions: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#activate(int)}.
     */
    @Test
    public final void testActivate() {
        IngestionStreamProcessor processor   = IngestionStreamProcessor.from(CONN_INGEST);
        
        Assert.assertFalse(processor.isActive());
        
        processor.activate(REC_PROVIDER_UID);
        Assert.assertTrue(processor.isActive());
        
        try {
            boolean bolShutdown = processor.shutdown();
        
            Assert.assertTrue(bolShutdown);
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during shutdown() operation: " + e.getMessage());

        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#shutdown()}.
     */
    @Test
    public final void testShutdown() {
        IngestionStreamProcessor processor   = IngestionStreamProcessor.from(CONN_INGEST);
        
        Assert.assertFalse(processor.isActive());
        
        processor.activate(REC_PROVIDER_UID);
        Assert.assertTrue(processor.isActive());
        
        try {
            boolean bolShutdown = processor.shutdown();
        
            Assert.assertTrue(bolShutdown);
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during shutdown() operation: " + e.getMessage());

        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#shutdownNow()}.
     */
    @Test
    public final void testShutdownNow() {
        IngestionStreamProcessor processor   = IngestionStreamProcessor.from(CONN_INGEST);
        
        Assert.assertFalse(processor.isActive());
        
        processor.activate(REC_PROVIDER_UID);
        Assert.assertTrue(processor.isActive());

        boolean bolShutdown = processor.shutdownNow();

        Assert.assertTrue(bolShutdown);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#awaitRequestQueueEmpty()}.
     */
    @Test
    public final void testAwaitRequestQueueEmpty() {

        // Parameters and resources
        List<IngestionFrame>    lstFrames = createDoubleFrames(10, 1000, 1000);
        final int               cntFrames = lstFrames.size();
        
        try {
            this.processor.transmit(lstFrames);
            
        } catch (IllegalStateException e) {
            Assert.fail("Processor threw IllegalStateException during transmit() operation: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during transmit() operation: " + e.getMessage());
        }
        
        try {
            Instant     insStart = Instant.now();
            this.processor.awaitRequestQueueEmpty();
            Instant     insStop = Instant.now();
            
            // Compute results
            Duration    durWait = Duration.between(insStart, insStop);
            System.out.println("awaitRequestQueueEmpty() waited " + durWait + " while processing/transmitting " + cntFrames + " ingestion frames.");
            
            Assert.assertEquals(0, this.processor.getRequestQueueSize());
            
        } catch (IllegalStateException e) {
            Assert.fail("Processor threw IllegalStateException during awaitRequestQueueEmpty() operation: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during awaitRequestQueueEmpty() operation: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#transmit(com.ospreydcs.dp.api.ingest.IngestionFrame)}.
     */
    @Test
    public final void testTransmitIngestionFrame() {
        
        try {
            this.processor.transmit(MSG_FRAME_SMALL);
            
        } catch (IllegalStateException e) {
            Assert.fail("Processor threw IllegalStateException during transmit() operation: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during transmit() operation: " + e.getMessage());
        }
        
        try {
            boolean bolShutdown = this.processor.shutdown();
            
            Assert.assertTrue(bolShutdown);
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("Processor threw exception " + e.getClass().getSimpleName() + " exception while waiting for queue emtpy: " + e.getMessage());
        }
        Assert.assertEquals(1, this.processor.getRequestCount());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionStreamProcessor#transmit(java.util.List)}.
     */
    @Test
    public final void testTransmitListOfIngestionFrame() {

        // Parameters and resources
        List<IngestionFrame>    lstFrames = LST_FRAMES_MOD;
        final int               cntFrames = lstFrames.size();
        
        try {
            this.processor.transmit(lstFrames);
            
        } catch (IllegalStateException e) {
            Assert.fail("Processor threw IllegalStateException during transmit() operation: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Processor threw InterruptedException during transmit() operation: " + e.getMessage());
        }
        
        try {
            boolean bolShutdown = this.processor.shutdown();
            
            Assert.assertTrue(bolShutdown);
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("Processor threw exception " + e.getClass().getSimpleName() + " exception while waiting for queue emtpy: " + e.getMessage());
        }
        Assert.assertEquals(cntFrames, this.processor.getRequestCount());
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
