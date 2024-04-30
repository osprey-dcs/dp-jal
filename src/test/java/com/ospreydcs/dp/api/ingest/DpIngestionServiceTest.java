/*
 * Project: dp-api-common
 * File:	DpIngestionServiceTest.java
 * Package: com.ospreydcs.dp.api.ingest
 * Type: 	DpIngestionServiceTest
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
package com.ospreydcs.dp.api.ingest;

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
import com.ospreydcs.dp.api.model.ProviderRegistrar;
import com.ospreydcs.dp.api.model.ProviderUID;

/**
 * <p>
 * JUnit test cases for class <code>DpIngestionService</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 29, 2024
 *
 */
public class DpIngestionServiceTest {

    
    //
    // Application Resources
    //
    
    /** The default Ingestion Service configuration parameters */
    private static final DpIngestionConfig          CFG_INGEST = DpApiConfig.getInstance().ingest;
    
    
    //
    // Class Constants
    //
    
    /** Test Data Provider registration unique name */
    private static final String                 STR_PROVIDER_NAME_1 = DpIngestionServiceTest.class.getName() + "TEST_PROVIDER_1";
    
    /** Test Data Provider attributes */
    private static final Map<String, String>    MAP_PROVIDER_ATTRS_1 = Map.of(
                                                    "Experiment", "UnitTest1",
                                                    "Location", "Office",
                                                    "Platform", "Developer"
                                                );
    
    //
    // Test Resources
    //
    
    /** The Ingestion Service API under test - only instantiate one */
    private static DpIngestionService       apiIngest;
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        apiIngest = DpIngestionServiceFactory.FACTORY.connect();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        apiIngest.shutdownSoft();
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.DpIngestionService#from(com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection)}.
     */
    @Test
    public final void testFrom() {
        
        try {
            DpIngestionConnection   connIngest = DpIngestionConnectionFactory.FACTORY.connect();

            DpIngestionService      apiTest = DpIngestionService.from(connIngest);
            
            // Shutdown API connection and test
            apiTest.shutdownSoft();
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.DpIngestionService#awaitTermination()}.
     */
    @Test
    public final void testAwaitTermination() {
        
        try {

            // Create new Ingestion Service API, then shut it down
            DpIngestionService      apiTest = DpIngestionServiceFactory.FACTORY.connect();
            
            apiTest.shutdownSoft();
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
            DpIngestionService      apiTest = DpIngestionServiceFactory.FACTORY.connect();
            
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.DpIngestionService#registerProvider(com.ospreydcs.dp.api.model.ProviderRegistrar)}.
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

}
