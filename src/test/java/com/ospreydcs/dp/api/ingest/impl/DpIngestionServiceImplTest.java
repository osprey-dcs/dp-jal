/*
 * Project: dp-api-common
 * File:	DpIngestionServiceImplTest.java
 * Package: com.ospreydcs.dp.api.ingest
 * Type: 	DpIngestionServiceImplTest
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
 * @since Mar 29, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnectionFactory;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.ingest.impl.DpIngestionServiceImpl;
import com.ospreydcs.dp.api.ingest.DpIngestionException;
import com.ospreydcs.dp.api.ingest.IIngestionService;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.impl.DpIngestionServiceFactory;
import com.ospreydcs.dp.api.ingest.test.TestIngestionFrameGenerator;
import com.ospreydcs.dp.api.model.IngestionResponse;
import com.ospreydcs.dp.api.model.ProviderRegistrar;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>DpIngestionServiceImpl</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 29, 2024
 *
 */
public class DpIngestionServiceImplTest {

    
    //
    // Application Resources
    //
    
    /** The default Ingestion Service configuration parameters */
    private static final DpIngestionConfig          CFG_INGEST = DpApiConfig.getInstance().ingest;
    
    
    //
    // Class Constants
    //
    
    /** Test Data Provider registration unique name */
    private static final String                 STR_PROVIDER_NAME_1 = DpIngestionServiceImplTest.class.getName() + "_TEST_PROVIDER_1";
    
    /** Test Data Provider attributes */
    private static final Map<String, String>    MAP_PROVIDER_ATTRS_1 = Map.of(
                                                    "Experiment", "JUnit unit test",
                                                    "Location", "Office",
                                                    "Platform", "Developer"
                                                );
    
    /** Test Data Provider registration information record */
    private static final ProviderRegistrar      REC_PROVIDER_REG_1 = ProviderRegistrar.from(STR_PROVIDER_NAME_1, MAP_PROVIDER_ATTRS_1);

    
    //
    // Test Resources
    //
    
    /** A test ingestion frame */
    private static final IngestionFrame         MSG_FRAME_SMALL = createDoubleFrames(1, 10, 10).get(0);
    
    /** A test ingestion frame */
    private static final IngestionFrame         MSG_FRAME_MOD = createDoubleFrames(1, 100, 100).get(0);
    
    /** A test ingestion frame */
    private static final IngestionFrame         MSG_FRAME_LARGE = createDoubleFrames(1, 1000, 1000).get(0);
    
    
    //
    // Test Subject
    //
    
    /** The Ingestion Service API under test - only instantiate one */
//    private static DpIngestionServiceImpl       apiIngest;
    private static IIngestionService            apiIngest;
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
//        DpIngestionConnection   connIngest = DpIngestionConnectionFactory.FACTORY.connect();
//        apiIngest = new DpIngestionServiceImpl(connIngest);
        
        apiIngest = DpIngestionServiceFactory.FACTORY.connect();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionServiceImpl#from(com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection)}.
     */
    @Test
    public final void testFrom() {
        
        try {
            DpIngestionConnection   connIngest = DpIngestionConnectionFactory.FACTORY.connect();

            DpIngestionServiceImpl      apiTest = DpIngestionServiceImpl.from(connIngest);
            
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionServiceImpl#awaitTermination()}.
     */
    @Test
    public final void testAwaitTermination() {
        
        try {

            // Create new Ingestion Service API, then shut it down
            IIngestionService      apiTest = DpIngestionServiceFactory.FACTORY.connect();
            
            apiTest.shutdown();
            Assert.assertTrue("Test Ingestion Service API reported NOT shutdown after shutdownSoft() operation.", apiTest.isShutdown());
            
            apiTest.awaitTermination();
            Assert.assertTrue("Ingestion API failed to report termination after waiting.", apiTest.isTerminated());
            
        } catch (DpGrpcException e) {
            Assert.fail("Attempting to create Ingestion Service API failed with exception: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Process was interrupted while waiting for Ingestion Service API to terminate.");
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#shutdownNow()}.
     */
    @Test
    public final void testShutdownNow() {

        try {
            // Create new Ingestion Service API, then shut it down hard
            IIngestionService      apiTest = DpIngestionServiceFactory.FACTORY.connect();
            
            apiTest.shutdownNow();
            Assert.assertTrue("Test Ingestion Service API reported NOT shutdown after shutdownNow() operation.", apiTest.isShutdown());
            
            apiTest.awaitTermination();
            Assert.assertTrue("Ingestion API failed to report termination after waiting.", apiTest.isTerminated());
            
        } catch (DpGrpcException e) {
            Assert.fail("Attempting to create Ingestion Service API failed with exception: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Process was interrupted while waiting for Ingestion Service API to terminate.");
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#shutdownSoft()}.
//     */
//    @Test
//    public final void testShutdownSoft() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#awaitTermination(long, java.util.concurrent.TimeUnit)}.
//     */
//    @Test
//    public final void testAwaitTerminationLongTimeUnit() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#isShutdown()}.
//     */
//    @Test
//    public final void testIsShutdown() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#isTerminated()}.
//     */
//    @Test
//    public final void testIsTerminated() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.impl.DpIngestionServiceImpl#registerProvider(com.ospreydcs.dp.api.model.ProviderRegistrar)}.
     */
    @Test
    public final void testRegisterProvider() {
        
        // Create registration record
        ProviderRegistrar   recReg = new ProviderRegistrar(STR_PROVIDER_NAME_1, MAP_PROVIDER_ATTRS_1);

        
        try {
            // Attempt provider registration
            ProviderUID     recUID = apiIngest.registerProvider(recReg);
            
            System.out.println("Test Data Provider UID = " + recUID.uid());
            
        } catch (DpIngestionException e) {
            Assert.fail("Data Provider registration failed with exception: " + e.getMessage());
            
        }
    }
    
    /**
     * Test method for {@link DpIngestionServiceImpl#ingest(com.ospreydcs.dp.api.ingest.IngestionFrame)
     */
    @Test
    public final void testIngestIngestionFrameSmall() {
        
        // Attempt provider registration
        ProviderUID     recUID;
        try {
            recUID = apiIngest.registerProvider(REC_PROVIDER_REG_1);
            
            System.out.println("UID = " + recUID.uid() + " for provider registration " + REC_PROVIDER_REG_1);
            
        } catch (DpIngestionException e) {
            Assert.fail("Data Provider registration failed with exception: " + e.getMessage());
            return;
        }
        
        // Attempt data ingestion
        try {
//            List<IngestionResponse> lstRsps = apiIngest.ingest(recUID, MSG_FRAME_SMALL);
            List<IngestionResponse> lstRsps = apiIngest.ingest(MSG_FRAME_SMALL);
            
            Assert.assertEquals(1, lstRsps.size());
            
        } catch (DpIngestionException e) {
            
            // Check if ingestData() is still unimplemented
            if (e.getCause() instanceof io.grpc.StatusRuntimeException gRpcExcp) {
                if (e.getMessage().contains("UNIMPLEMENTED")) {
                    System.out.println("WARNING: The Ingestion Service ingestData() operation is still unimplemented");
                    System.out.println("  Details: " + gRpcExcp.getMessage());
                    
                    return;
                }
            }
            
            Assert.fail("Data ingestion failed with exception: " + e.getMessage());
        }
    }


    /**
     * Test method for {@link DpIngestionServiceImpl#ingest(ProviderUID, com.ospreydcs.dp.api.ingest.IngestionFrame)
     */
    @Test
    public final void testIngestIngestionFrameLarge() {
        
        // Enable frame decomposition
        
        
        // Attempt provider registration
        ProviderUID     recUID;
        try {
            recUID = apiIngest.registerProvider(REC_PROVIDER_REG_1);
            
            System.out.println("UID = " + recUID.uid() + " for provider registration " + REC_PROVIDER_REG_1);
            
        } catch (DpIngestionException e) {
            Assert.fail("Data Provider registration failed with exception: " + e.getMessage());
            return;
        }
        
        // Attempt data ingestion
        try {
//            List<IngestionResponse> lstRsps = apiIngest.ingest(recUID, MSG_FRAME_LARGE);
            List<IngestionResponse> lstRsps = apiIngest.ingest(MSG_FRAME_LARGE);
            
            Assert.assertEquals(1, lstRsps.size());
            
        } catch (DpIngestionException e) {
            
            // Check if ingestData() is still unimplemented
            if (e.getCause() instanceof io.grpc.StatusRuntimeException gRpcExcp) {
                if (e.getMessage().contains("UNIMPLEMENTED")) {
                    System.out.println("WARNING: The Ingestion Service ingestData() operation is still unimplemented");
                    System.out.println("  Details: " + gRpcExcp.getMessage());
                    
                    return;
                }
            }
            
            Assert.fail("Data ingestion failed with exception: " + e.getMessage());
            
        } finally {
            
            // Check that the ingestion frame was destroyed
            Assert.assertEquals(0, MSG_FRAME_LARGE.getColumnCount());
        }
    }
    
    /**
     * Test method for {@link DpIngestionServiceImpl#ingest(ProviderUID, com.ospreydcs.dp.api.ingest.IngestionFrame)
     */
    @Test
    public final void testIngestListOfIngestionFramesLarge() {
        
        // Parameters
        final int       cntFrames = 15;
        final int       cntCols = 1000; // ensure decomposition
        final int       cntRows = 1000; //
        final long      szFrmMax = 4_000_000L;
        
        final List<IngestionFrame>  lstFrames = TestIngestionFrameGenerator.createDoublesPayloadWithClock(cntFrames, cntCols, cntRows, true);
        final long      szFrame = lstFrames.get(0).allocationSizeFrame();
        final long      szPayload = lstFrames.stream().mapToLong(msg -> msg.allocationSizeFrame()).sum();
        
        
        // Create API for this test
        DpIngestionServiceImpl  apiIngest;
        try {
            DpIngestionConnection   connIngest = DpIngestionConnectionFactory.FACTORY.connect();
            apiIngest = new DpIngestionServiceImpl(connIngest);

            // Enable frame decomposition
            apiIngest.enableFrameDecomposition(szFrmMax);
            
        } catch (DpGrpcException e) {
            Assert.fail("Could not create Ingestion Service connection: " + e.getMessage());
            return;
        }
        
        
        // Attempt provider registration
        
        try {
            apiIngest.registerProvider(REC_PROVIDER_REG_1);
            
        } catch (DpIngestionException e) {
            Assert.fail("Data Provider registration failed with exception: " + e.getMessage());
            return;
        }
        
        
        // Test results
        List<IngestionResponse> lstRspsAll = new LinkedList<>();    // responses from full payload
        Instant insStart, insFinish, insShutdown, insTerminate;     // Time everything
        
        try {
            // Submit payload frame-by-frame
            insStart = Instant.now();
            for (IngestionFrame frame : lstFrames) {
                List<IngestionResponse> lstRsps = apiIngest.ingest(frame);
                
                lstRspsAll.addAll(lstRsps);
            }
            insFinish = Instant.now();
            
        } catch (Exception e) {
            
            // Check if ingestData() is still unimplemented
            if (e.getCause() instanceof io.grpc.StatusRuntimeException gRpcExcp) {
                if (e.getMessage().contains("UNIMPLEMENTED")) {
                    System.out.println("WARNING: The Ingestion Service ingestData() operation is still unimplemented");
                    System.out.println("  Details: " + gRpcExcp.getMessage());
                }
            }
            
            Assert.fail("Data ingestion failed with exception: " + e.getMessage());
            return;
        }
        
        // Get state variables
        ProviderUID     recUID = apiIngest.getProviderUid();
        int             cntXmissions = apiIngest.getTransmissionCount();
        
        // Shutdown API
        try {
            apiIngest.shutdown();
            insShutdown = Instant.now();
            Assert.assertTrue(apiIngest.isShutdown());
            
            apiIngest.awaitTermination();
            insTerminate = Instant.now();
            Assert.assertTrue(apiIngest.isTerminated());
            
        } catch (InterruptedException e) {
            Assert.fail("API shutdown() interrupted: " + e.getMessage());
            return;
        }
            
        // Check that all ingestion frames were destroyed during decomposition
        for (IngestionFrame frame : lstFrames)
            Assert.assertEquals(0, frame.getColumnCount());
        
        // Print out results
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Payload frames   : " + cntFrames);
        System.out.println("  Frame allocation : " + szFrame);
        System.out.println("  Payload allocation : " + szPayload);
        System.out.println("  Maximum frame size : " + szFrmMax);
        System.out.println("  Provider UID       : " + recUID);
        System.out.println("  Messages transmitted : " + cntXmissions);
        System.out.println("  Time for transmission : " + Duration.between(insStart, insFinish));
        System.out.println("  Time for shutdown     : " + Duration.between(insFinish, insShutdown));
        System.out.println("  Time for termination  : " + Duration.between(insShutdown, insTerminate));
        System.out.println("  Total time active     : " + Duration.between(insStart, insTerminate));
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
