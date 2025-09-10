/*
 * Project: dp-api-common
 * File:	DpAnnotationApiFactoryTest.java
 * Package: com.ospreydcs.dp.api.annotate
 * Type: 	DpAnnotationApiFactoryTest
 *
 * Copyright 2010-2025 the original author or authors.
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
 * @since Mar 2, 2025
 *
 */
package com.ospreydcs.dp.api.annotate;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.JalConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;

/**
 * <p>
 * JUnit test cases for class <code>DpAnnotationApiFactory</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 2, 2025
 *
 */
public class DpAnnotationApiFactoryTest {

    
    //
    // Class Resources
    //
    
    /** Application default parameters for the Data Platform Query Service gRPC connection */
    public static final DpGrpcConnectionConfig    CFG_DEFAULT = JalConfig.getInstance().connections.annotation;
    
    
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
     * Test method for {@link com.ospreydcs.dp.api.annotate.DpAnnotationApiFactory#connect()}.
     */
    @Test
    public final void testConnect() {
        boolean bolResult = false;
        
        try {
            IAnnotationService apiTest = DpAnnotationApiFactory.connect();
            
            bolResult = apiTest.shutdown();
            
            Assert.assertTrue("Service API shutdown reported failure", bolResult);
            
            bolResult = apiTest.awaitTermination();
            
            Assert.assertTrue("Termination wait failure - service API apparently failed to shut down.", bolResult);
            
        } catch (DpGrpcException e) {
            Assert.fail("Connection factory creation exception: " + e.getMessage());

        } catch (InterruptedException e) {
            Assert.fail("Exception thrown while waiting for service api shutdown termination: " + e.getMessage());

        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.DpAnnotationApiFactory#connect(java.lang.String, int)}.
     */
    @Test
    public final void testConnectStringInt() {
        boolean bolResult = false;
        
        try {
            IAnnotationService   apiTest = DpAnnotationApiFactory.connect(CFG_DEFAULT.channel.host.url, CFG_DEFAULT.channel.host.port);
            
            bolResult = apiTest.shutdown();
            
            Assert.assertTrue("Service API shutdown reported failure", bolResult);
            
            bolResult = apiTest.awaitTermination();
            
            Assert.assertTrue("Termination wait failure - service API apparently failed to shut down.", bolResult);
            
        } catch (DpGrpcException e) {
            Assert.fail("Connection factory creation exception: " + e.getMessage());

        } catch (InterruptedException e) {
            Assert.fail("Exception thrown while waiting for service api shutdown termination: " + e.getMessage());

        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.DpAnnotationApiFactory#connect(java.lang.String, int, boolean)}.
     */
    @Test
    public final void testConnectStringIntBoolean() {
        boolean bolResult = false;
        
        try {
            IAnnotationService apiTest = DpAnnotationApiFactory.connect(
                    CFG_DEFAULT.channel.host.url, 
                    CFG_DEFAULT.channel.host.port,
                    CFG_DEFAULT.channel.grpc.usePlainText
                    );
            
            bolResult = apiTest.shutdown();
            
            Assert.assertTrue("Service API shutdown reported failure", bolResult);
            
            bolResult = apiTest.awaitTermination();
            
            Assert.assertTrue("Termination wait failure - service API apparently failed to shut down.", bolResult);
            
        } catch (DpGrpcException e) {
            Assert.fail("Connection factory creation exception: " + e.getMessage());

        } catch (InterruptedException e) {
            Assert.fail("Exception thrown while waiting for service api shutdown termination: " + e.getMessage());

        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.DpAnnotationApiFactory#connect(java.lang.String, int, boolean, long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testConnectStringIntBooleanLongTimeUnit() {
        boolean bolResult = false;
        
        try {
            IAnnotationService apiTest = DpAnnotationApiFactory.connect(
                    CFG_DEFAULT.channel.host.url, 
                    CFG_DEFAULT.channel.host.port,
                    CFG_DEFAULT.channel.grpc.usePlainText,
                    CFG_DEFAULT.channel.grpc.timeoutLimit, 
                    CFG_DEFAULT.channel.grpc.timeoutUnit
                    );
            
            bolResult = apiTest.shutdown();
            
            Assert.assertTrue("Service API shutdown reported failure", bolResult);
            
            bolResult = apiTest.awaitTermination();
            
            Assert.assertTrue("Termination wait failure - service API apparently failed to shut down.", bolResult);
            
        } catch (DpGrpcException e) {
            Assert.fail("Connection factory creation exception: " + e.getMessage());

        } catch (InterruptedException e) {
            Assert.fail("Exception thrown while waiting for service api shutdown termination: " + e.getMessage());

        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.DpAnnotationApiFactory#connect(java.lang.String, int, boolean, boolean, int, boolean, boolean, long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testConnectStringIntBooleanBooleanIntBooleanBooleanLongTimeUnit() {
        boolean bolResult = false;
        
        try {
            IAnnotationService   apiTest = DpAnnotationApiFactory.connect(
                    CFG_DEFAULT.channel.host.url, 
                    CFG_DEFAULT.channel.host.port,
                    CFG_DEFAULT.channel.tls.enabled,
                    CFG_DEFAULT.channel.grpc.usePlainText,
                    CFG_DEFAULT.channel.grpc.messageSizeMax,
                    CFG_DEFAULT.channel.grpc.keepAliveWithoutCalls,
                    CFG_DEFAULT.channel.grpc.gzip,
                    CFG_DEFAULT.channel.grpc.timeoutLimit, 
                    CFG_DEFAULT.channel.grpc.timeoutUnit
                    );
            
            bolResult = apiTest.shutdown();
            
            Assert.assertTrue("Service API shutdown reported failure", bolResult);
            
            bolResult = apiTest.awaitTermination();
            
            Assert.assertTrue("Termination wait failure - service API apparently failed to shut down.", bolResult);
            
        } catch (DpGrpcException e) {
            Assert.fail("Connection factory creation exception: " + e.getMessage());

        } catch (InterruptedException e) {
            Assert.fail("Exception thrown while waiting for service api shutdown termination: " + e.getMessage());

        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.annotate.DpAnnotationApiFactory#connect(java.io.File, java.io.File, java.io.File)}.
//     */
//    @Test
//    public final void testConnectFileFileFile() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.annotate.DpAnnotationApiFactory#connect(java.lang.String, int, java.io.File, java.io.File, java.io.File)}.
//     */
//    @Test
//    public final void testConnectStringIntFileFileFile() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.annotate.DpAnnotationApiFactory#connect(java.lang.String, int, java.io.File, java.io.File, java.io.File, boolean, int, boolean, boolean, long, java.util.concurrent.TimeUnit)}.
//     */
//    @Test
//    public final void testConnectStringIntFileFileFileBooleanIntBooleanBooleanLongTimeUnit() {
//        fail("Not yet implemented"); // TODO
//    }

}
