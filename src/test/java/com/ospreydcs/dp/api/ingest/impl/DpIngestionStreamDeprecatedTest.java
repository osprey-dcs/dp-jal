/*
 * Project: dp-api-common
 * File:	DpIngestionStreamDeprecatedTest.java
 * Package: com.ospreydcs.dp.api.ingest
 * Type: 	DpIngestionStreamDeprecatedTest
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
 * @since Apr 29, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.IngestRequestUID;
import com.ospreydcs.dp.api.common.IngestionResponse;
import com.ospreydcs.dp.api.common.ProviderRegistrar;
import com.ospreydcs.dp.api.common.ProviderUID;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnectionFactory;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.ingest.DpIngestionException;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.test.TestIngestionFrameGenerator;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>{@link DpIngestionStreamDeprecated}</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 29, 2024
 *
 */
@Deprecated(since="Aug 26, 2024", forRemoval=true)
public class DpIngestionStreamDeprecatedTest {


    
    //
    // Application Resources
    //
    
    /** The default Ingestion Service configuration parameters */
    private static final DpIngestionConfig          CFG_INGEST = DpApiConfig.getInstance().ingest;
    
    
    //
    // Class Constants
    //
    
    /** Test Data Provider registration unique name */
    private static final String                 STR_PROVIDER_NAME_1 = DpIngestionStreamDeprecatedTest.class.getName() + "TEST_PROVIDER_1";
    
    /** Test Data Provider attributes */
    private static final Map<String, String>    MAP_PROVIDER_ATTRS_1 = Map.of(
                                                    "Experiment", "UnitTest1",
                                                    "Location", "Office",
                                                    "Platform", "Developer"
                                                );
    /** Data Provider registration record */
    private static final ProviderRegistrar      REC_REGISTRAR = ProviderRegistrar.from(STR_PROVIDER_NAME_1, MAP_PROVIDER_ATTRS_1);
    
    
    /** Queue capacity for back-pressure testing */
    private static final int                    INT_QUEUE_CAPACITY = 5;
    
    /** Ingestion frame list size */
    private static final int                    INT_FRAMES_COUNT = 10;
    
    
    //
    // Test Resources
    //
    
    /** A test ingestion frame */
    private static final IngestionFrame         MSG_FRAME_SMALL = createDoubleFrames(1, 10, 10).get(0);
    
    /** A list of test data frames that are small */
    private static final List<IngestionFrame>   LST_FRAMES_SMALL = createDoubleFrames(INT_FRAMES_COUNT, 10, 10);
    
    /** A list of test data frames that have moderate allocation */
    private static final List<IngestionFrame>   LST_FRAMES_MOD = createDoubleFrames(INT_FRAMES_COUNT, 100, 100);
    
    /** A list of test data frames that have moderate allocation - this can only be used once if decomposition enabled */
    private static final List<IngestionFrame>   LST_FRAMES_LARGE = createDoubleFrames(INT_FRAMES_COUNT, 1000, 1000);
    

    //
    // Test Subject
    //
    
    /** The Ingestion Service API under test - only use one if test allows it */
    private static DpIngestionStreamDeprecated        apiIngest;
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        DpIngestionConnection connIngest = DpIngestionConnectionFactory.FACTORY.connect();
//        apiIngest = DpIngestionStreamFactory.FACTORY.connect();
        apiIngest = DpIngestionStreamDeprecated.from(connIngest);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (apiIngest.isStreamOpen())
            apiIngest.closeStream();
        apiIngest.shutdown();
        apiIngest.awaitTermination();
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#shutdown()}.
     */
    @Test
    public final void testShutdownSoft() {
        
        try {
            // Connect the interface
            DpIngestionConnection       connIngest = DpIngestionConnectionFactory.FACTORY.connect();
            DpIngestionStreamDeprecated apiTest = DpIngestionStreamDeprecated.from(connIngest);
            
            // Now shut it down
            apiTest.shutdown();
            
            // Check for shutdown
            Assert.assertTrue(apiTest.isShutdown());
            
        } catch (DpGrpcException e) {
            Assert.fail("Unable to connect to Ingestion Service - Connection factory failure.");
            
        } catch (InterruptedException e) {
            Assert.fail("Interrupted while waiting for soft shutdown: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#shutdownNow()}.
     */
    @Test
    public final void testShutdownNow() {
        
        try {
            // Connect the interface
//            DpIngestionStreamDeprecated   apiTest = DpIngestionStreamFactory.FACTORY.connect();
            DpIngestionConnection       connIngest = DpIngestionConnectionFactory.FACTORY.connect();
            DpIngestionStreamDeprecated apiTest = DpIngestionStreamDeprecated.from(connIngest);
            
            // Now shut it down hard
            apiTest.shutdownNow();
            
            // Check for shutdown
            Assert.assertTrue(apiTest.isShutdown());
            
        } catch (DpGrpcException e) {
            Assert.fail("Unable to connect to Ingestion Service - Connection factory failure.");
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#awaitTermination()}.
     */
    @Test
    public final void testAwaitTermination() {

        try {
            // Connect the interface
//            DpIngestionStreamDeprecated   apiTest = DpIngestionStreamFactory.FACTORY.connect();
            DpIngestionConnection       connIngest = DpIngestionConnectionFactory.FACTORY.connect();
            DpIngestionStreamDeprecated apiTest = DpIngestionStreamDeprecated.from(connIngest);
            
            // Now shut it down
            Instant     insShutdown = Instant.now();
            apiTest.shutdown();
            
            // Check for shutdown
            Assert.assertTrue(apiTest.isShutdown());
            
            // Wait for interface termination
            boolean     bolTerminate = apiTest.awaitTermination();
            Instant     insTerminate = Instant.now();
            Duration    durTerminate = Duration.between(insShutdown, insTerminate);
            
            System.out.println("Termination from soft shutdown took " + durTerminate);
            
            Assert.assertTrue(bolTerminate);
            
            
        } catch (DpGrpcException e) {
            Assert.fail("Unable to connect to Ingestion Service - Connection factory failure.");
            
        } catch (InterruptedException e) {
            Assert.fail("Interrupted while waiting for soft shutdown or termination: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#from(com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection)}.
     */
    @Test
    public final void testFrom() {
        
        try {
            DpIngestionConnection   connIngest = DpIngestionConnectionFactory.FACTORY.connect();

            DpIngestionStreamDeprecated       apiTest = DpIngestionStreamDeprecated.from(connIngest);
            
            // Shutdown API connection and test
            apiTest.shutdown();
            Assert.assertTrue("Ingestion API reported NOT shutdown after shutdown operation", apiTest.isShutdown());
            
            apiTest.awaitTermination();
            Assert.assertTrue("Ingestion API failed to report termination after waiting.", apiTest.isTerminated());

        } catch (DpGrpcException e) {
            Assert.fail("Creating connection to Ingestion Service failed with exception: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Process was interrupted while waiting for Ingestion Service API to terminate.");
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#DpIngestionStream(com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection)}.
     */
    @Test
    public final void testDpIngestionStream() {
        
        try {
            DpIngestionConnection   connIngest = DpIngestionConnectionFactory.FACTORY.connect();

            DpIngestionStreamDeprecated       apiTest = new DpIngestionStreamDeprecated(connIngest);
            
            // Shutdown API connection and test
            apiTest.shutdown();
            Assert.assertTrue("Ingestion API reported NOT shutdown after shutdown operation", apiTest.isShutdown());
            
            apiTest.awaitTermination();
            Assert.assertTrue("Ingestion API failed to report termination after waiting.", apiTest.isTerminated());

        } catch (DpGrpcException e) {
            Assert.fail("Creating connection to Ingestion Service failed with exception: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Process was interrupted while waiting for Ingestion Service API to terminate.");
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#enableBackPressure(int)}.
     */
    @Test
    public final void testEnableBackPressure() {
        
        try {
            // stream is not open - should work 
            apiIngest.enableBackPressure(INT_QUEUE_CAPACITY);
            
        } catch (IllegalStateException e) {
            Assert.fail("Unopen stream threw IllegalStateException with enableBackPressure: " + e.getMessage());
        }
        
        try {
            apiIngest.openStream(REC_REGISTRAR);
            
            // Now try to enable back pressure - should fail
            apiIngest.enableBackPressure(INT_QUEUE_CAPACITY);
            Assert.fail("enableBackPressure() did not throw exception while stream open.");
            
        } catch (DpIngestionException e) {
            Assert.fail("Interface threw DpIngestionException opening stream: " + e.getMessage());
            
        } catch (IllegalStateException e) {
            // should end up here
        }
        
        try {
            apiIngest.closeStream();
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("closeStream() operation threw " + e.getClass().getSimpleName() + " exception: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#disableBackPressure()}.
     */
    @Test
    public final void testDisableBackPressure() {

        try {
            // stream is not open - should work 
            apiIngest.disableBackPressure();
            
        } catch (IllegalStateException e) {
            Assert.fail("Unopen stream threw IllegalStateException with disableBackPressure: " + e.getMessage());
        }
        
        try {
            apiIngest.openStream(REC_REGISTRAR);
            
            // Now try to enable back pressure - should fail
            apiIngest.disableBackPressure();
            Assert.fail("disableBackPressure() did not throw exception while stream open.");
            
        } catch (DpIngestionException e) {
            Assert.fail("Interface threw DpIngestionException opening stream: " + e.getMessage());
            
        } catch (IllegalStateException e) {
            // should end up here
        }
        
        try {
            apiIngest.closeStream();
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("closeStream() operation threw " + e.getClass().getSimpleName() + " exception: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#openStream(com.ospreydcs.dp.api.common.ProviderRegistrar)}.
     */
    @Test
    public final void testOpenStream() {

        try {
            ProviderUID recUID = apiIngest.openStream(REC_REGISTRAR);
            
            System.out.println("openStream() recUID = " + recUID);
            
            Assert.assertTrue(apiIngest.isStreamOpen());
            
        } catch (DpIngestionException e) {
            Assert.fail("Interface threw DpIngestionException opening stream: " + e.getMessage());
            
        }
        
        try {
            apiIngest.closeStream();
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("closeStream() operation threw " + e.getClass().getSimpleName() + " exception: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#closeStream()}.
     */
    @Test
    public final void testCloseStream() {

        try {
            apiIngest.openStream(REC_REGISTRAR);
            
        } catch (DpIngestionException e) {
            Assert.fail("Interface threw DpIngestionException opening stream: " + e.getMessage());
            
        }
        
        try {
            apiIngest.closeStream();
            
            Assert.assertFalse(apiIngest.isStreamOpen());
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("closeStream() operation threw " + e.getClass().getSimpleName() + " exception: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#closeStreamNow()}.
     */
    @Test
    public final void testCloseStreamNow() {

        try {
            apiIngest.openStream(REC_REGISTRAR);
            
        } catch (DpIngestionException e) {
            Assert.fail("Interface threw DpIngestionException opening stream: " + e.getMessage());
            
        }
        
        apiIngest.closeStreamNow();
        Assert.assertFalse(apiIngest.isStreamOpen());    
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#ingest(com.ospreydcs.dp.api.ingest.IngestionFrame)}.
     */
    @Test
    public final void testIngestIngestionFrame() {

        // Open the stream
        try {
            ProviderUID recUID = apiIngest.openStream(REC_REGISTRAR);
            
            System.out.println("openStream() recUID = " + recUID);
            
            Assert.assertTrue(apiIngest.isStreamOpen());
            
        } catch (DpIngestionException e) {
            Assert.fail("Interface threw DpIngestionException opening stream: " + e.getMessage());
            
        }
        
        // Send 1 frame to the Ingestion Service
        try {
            apiIngest.ingest(MSG_FRAME_SMALL);
            
        } catch (DpIngestionException e) {
            Assert.fail("ingest() threw DpIngestionException: " + e.getMessage());
            
        } catch (IllegalStateException e) {
            Assert.fail("ingest() threw IllegalStateException - test fixture error: " + e.getMessage());
            
        }
        
        // Close the stream
        try {
            apiIngest.closeStream();
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("closeStream() operation threw " + e.getClass().getSimpleName() + " exception: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#ingest(java.util.List)}.
     */
    @Test
    public final void testIngestListOfIngestionFrame() {

        // Open the stream
        try {
            ProviderUID recUID = apiIngest.openStream(REC_REGISTRAR);
            
            System.out.println("openStream() recUID = " + recUID);
            
            Assert.assertTrue(apiIngest.isStreamOpen());
            
        } catch (DpIngestionException e) {
            Assert.fail("Interface threw DpIngestionException opening stream: " + e.getMessage());
            
        }
        
        // Send 10 small frames to the Ingestion Service
        try {
            apiIngest.ingest(LST_FRAMES_SMALL);
            
        } catch (DpIngestionException e) {
            Assert.fail("ingest() threw DpIngestionException: " + e.getMessage());
            
        } catch (IllegalStateException e) {
            Assert.fail("ingest() threw IllegalStateException - test fixture error: " + e.getMessage());
            
        }
        
        // Close the stream
        try {
            apiIngest.closeStream();
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("closeStream() operation threw " + e.getClass().getSimpleName() + " exception: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#getQueueSize()}.
     */
    @Test
    public final void testGetOutgoingQueueSize() {

        // Parameters and resources
        final int               cntFrames = 20;
        final int               cntCols = 100;
        final int               cntRows = 100;
        List<IngestionFrame>    lstFrames = createDoubleFrames(cntFrames, cntCols, cntRows);

        final int               cntPolls = 10;
        final int[]             arrQueSize = new int[cntPolls];
        final long              lngPollWaitMs = 1;
        
        // Open the stream
        try {
            ProviderUID recUID = apiIngest.openStream(REC_REGISTRAR);
            
            Assert.assertTrue(apiIngest.isStreamOpen());
            
        } catch (DpIngestionException e) {
            Assert.fail("Interface threw DpIngestionException opening stream: " + e.getMessage());
            
        }
        
        // Send 10 frames to the Ingestion Service - then poll queue size, look for dynamics
        try {
            apiIngest.ingest(lstFrames);
            
            for (int iPoll=0; iPoll<cntPolls; iPoll++) {
                arrQueSize[iPoll] = apiIngest.getQueueSize();
                
                Thread.sleep(lngPollWaitMs);
            }
            
            // Report Results
            System.out.println("Outgoing queue sizes after submitting " + cntFrames + " frames:");
            System.out.println("  time (ms)\t queue size");
            for (int iPoll=0; iPoll<cntPolls; iPoll++) {
                String  strLine = String.format("  %9d \t %3d", iPoll*lngPollWaitMs, arrQueSize[iPoll]);
                
                System.out.println(strLine);
            }
            
        } catch (DpIngestionException e) {
            Assert.fail("ingest() threw DpIngestionException: " + e.getMessage());
            
        } catch (IllegalStateException e) {
            Assert.fail("ingest() threw IllegalStateException - test fixture error: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Queue size polling operation interrupted while waiting for next poll: " + e.getMessage());
            
        }
        
        // Close the stream
        try {
            apiIngest.closeStream();
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("closeStream() operation threw " + e.getClass().getSimpleName() + " exception: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#awaitQueueEmpty()}.
     */
    @Test
    public final void testAwaitOutgoingQueueEmpty() {

        // Parameters and resources
        final int               cntFrames = 10;
        final int               cntCols = 1000;
        final int               cntRows = 1000;
        List<IngestionFrame>    lstFrames = createDoubleFrames(cntFrames, cntCols, cntRows);
        
        final long              lngWaitMs = 5;
        
        // Open the stream
        try {
            apiIngest.openStream(REC_REGISTRAR);
            
            Assert.assertTrue(apiIngest.isStreamOpen());
            
        } catch (DpIngestionException e) {
            Assert.fail("Interface threw DpIngestionException opening stream: " + e.getMessage());
            
        }
        
        // Send 10 frames to the Ingestion Service - then wait
        try {
            apiIngest.ingest(lstFrames);

            Thread.sleep(lngWaitMs);
            
            Instant     insStart = Instant.now();
            apiIngest.awaitQueueEmpty();
            Instant     insStop = Instant.now();
            Duration    durWait = Duration.between(insStart, insStop);
            
            // Report Results
            System.out.println("Outgoing queue emtpy wait after submitting 10 moderate frames:");
            System.out.println("  wait time = " + durWait);

            Assert.assertEquals(0, apiIngest.getQueueSize());
            
        } catch (DpIngestionException e) {
            Assert.fail("ingest() threw DpIngestionException: " + e.getMessage());
            
        } catch (IllegalStateException e) {
            Assert.fail("ingest() threw IllegalStateException - test fixture error: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Queue size polling operation interrupted while waiting for next poll: " + e.getMessage());
            
        }
        
        // Close the stream
        try {
            apiIngest.closeStream();
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("closeStream() operation threw " + e.getClass().getSimpleName() + " exception: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#getClientRequestIds()}.
     */
    @Test
    public final void testGetClientRequestIds() {

        // Parameters and resources
        final int               cntFrames = 10;
        final int               cntCols = 10;
        final int               cntRows = 10;
        List<IngestionFrame>    lstFrames = createDoubleFrames(cntFrames, cntCols, cntRows);
        
        // Open the stream
        try {
            apiIngest.openStream(REC_REGISTRAR);
            
            Assert.assertTrue(apiIngest.isStreamOpen());
            
        } catch (DpIngestionException e) {
            Assert.fail("Interface threw DpIngestionException opening stream: " + e.getMessage());
            
        }
        
        // Send 10 frames to the Ingestion Service - then wait
        try {
            apiIngest.ingest(lstFrames);
            Assert.assertTrue(apiIngest.isStreamOpen());
            
        } catch (DpIngestionException e) {
            Assert.fail("ingest() threw DpIngestionException: " + e.getMessage());
            
        } catch (IllegalStateException e) {
            Assert.fail("ingest() threw IllegalStateException - test fixture error: " + e.getMessage());
            
        }
        
        // Close the stream
        try {
            apiIngest.closeStream();
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("closeStream() operation threw " + e.getClass().getSimpleName() + " exception: " + e.getMessage());
        }
        
        // Get the client request IDs and compare
        List<IngestRequestUID>   lstRqstIds = apiIngest.getClientRequestIds();
        Assert.assertEquals(cntFrames, lstRqstIds.size());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionStreamDeprecated#getIngestionExceptions()}.
     */
    @Test
    public final void testGetIngestionExceptions() {

        // Parameters and resources
        final int               cntFrames = 10;
        final int               cntCols = 10;
        final int               cntRows = 10;
        List<IngestionFrame>    lstFrames = createDoubleFrames(cntFrames, cntCols, cntRows);
        
        // Open the stream
        try {
            apiIngest.openStream(REC_REGISTRAR);
            
            Assert.assertTrue(apiIngest.isStreamOpen());
            
        } catch (DpIngestionException e) {
            Assert.fail("Interface threw DpIngestionException opening stream: " + e.getMessage());
            
        }
        
        // Send 10 frames to the Ingestion Service - then wait
        try {
            apiIngest.ingest(lstFrames);
            Assert.assertTrue(apiIngest.isStreamOpen());
            
        } catch (DpIngestionException e) {
            Assert.fail("ingest() threw DpIngestionException: " + e.getMessage());
            
        } catch (IllegalStateException e) {
            Assert.fail("ingest() threw IllegalStateException - test fixture error: " + e.getMessage());
            
        }
        
        // Close the stream
        try {
            apiIngest.closeStream();
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("closeStream() operation threw " + e.getClass().getSimpleName() + " exception: " + e.getMessage());
        }
        
        // Get the the request exception list - not much we can do
        List<IngestionResponse>   lstRspExcp = apiIngest.getIngestionExceptions();
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " reports " + lstRspExcp.size() + " ingestion exceptions.");
        for (IngestionResponse recRsp : lstRspExcp) {
            Assert.assertTrue(recRsp.hasException());
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
