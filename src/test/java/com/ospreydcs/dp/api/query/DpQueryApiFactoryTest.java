/*
 * Project: dp-api-common
 * File:	DpQueryApiFactoryTest.java
 * Package: com.ospreydcs.dp.api.query
 * Type: 	DpQueryApiFactoryTest
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
 * @since Dec 28, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;

/**
 * <p>
 * JUnit test cases for class <code>DpQueryApiFactory</code>.
 *
 * @author Christopher K. Allen
 * @since Dec 28, 2024
 *
 */
public class DpQueryApiFactoryTest {

    
    //
    // Class Resources
    //
    
    /** Application default parameters for the Data Platform Query Service gRPC connection */
    public static final DpGrpcConnectionConfig    CFG_DEFAULT = DpApiConfig.getInstance().connections.query;
    
    
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

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactory#connect()}.
     */
    @Test
    public final void testConnectService() {
        boolean bolResult = false;
        
        try {
            IQueryService qs = DpQueryApiFactory.connect();
            
            bolResult = qs.shutdown();
            
            Assert.assertTrue("Service API shutdown reported failure", bolResult);
            
            bolResult = qs.awaitTermination();
            
            Assert.assertTrue("Termination wait failure - service API apparently failed to shut down.", bolResult);
            
        } catch (DpGrpcException e) {
            Assert.fail("Connection factory creation exception: " + e.getMessage());

        } catch (InterruptedException e) {
            Assert.fail("Exception thrown while waiting for service api shutdown termination: " + e.getMessage());

        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactory#connect(java.lang.String, int)}.
     */
    @Test
    public final void testConnectServiceStringInt() {
        boolean bolResult = false;
        
        try {
            IQueryService   qs = DpQueryApiFactory.connect(CFG_DEFAULT.channel.host.url, CFG_DEFAULT.channel.host.port);
            
            bolResult = qs.shutdown();
            
            Assert.assertTrue("Service API shutdown reported failure", bolResult);
            
            bolResult = qs.awaitTermination();
            
            Assert.assertTrue("Termination wait failure - service API apparently failed to shut down.", bolResult);
            
        } catch (DpGrpcException e) {
            Assert.fail("Connection factory creation exception: " + e.getMessage());

        } catch (InterruptedException e) {
            Assert.fail("Exception thrown while waiting for service api shutdown termination: " + e.getMessage());

        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactory#connect(java.lang.String, int, boolean)}.
     */
    @Test
    public final void testConnectServiceStringIntBoolean() {
        boolean bolResult = false;
        
        try {
            IQueryService qs = DpQueryApiFactory.connect(
                    CFG_DEFAULT.channel.host.url, 
                    CFG_DEFAULT.channel.host.port,
                    CFG_DEFAULT.channel.grpc.usePlainText
                    );
            
            bolResult = qs.shutdown();
            
            Assert.assertTrue("Service API shutdown reported failure", bolResult);
            
            bolResult = qs.awaitTermination();
            
            Assert.assertTrue("Termination wait failure - service API apparently failed to shut down.", bolResult);
            
        } catch (DpGrpcException e) {
            Assert.fail("Connection factory creation exception: " + e.getMessage());

        } catch (InterruptedException e) {
            Assert.fail("Exception thrown while waiting for service api shutdown termination: " + e.getMessage());

        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactory#connect(java.lang.String, int, boolean, long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testConnectServiceStringIntBooleanLongTimeUnit() {
        boolean bolResult = false;
        
        try {
            IQueryService qs = DpQueryApiFactory.connect(
                    CFG_DEFAULT.channel.host.url, 
                    CFG_DEFAULT.channel.host.port,
                    CFG_DEFAULT.channel.grpc.usePlainText,
                    CFG_DEFAULT.channel.grpc.timeoutLimit, 
                    CFG_DEFAULT.channel.grpc.timeoutUnit
                    );
            
            bolResult = qs.shutdown();
            
            Assert.assertTrue("Service API shutdown reported failure", bolResult);
            
            bolResult = qs.awaitTermination();
            
            Assert.assertTrue("Termination wait failure - service API apparently failed to shut down.", bolResult);
            
        } catch (DpGrpcException e) {
            Assert.fail("Connection factory creation exception: " + e.getMessage());

        } catch (InterruptedException e) {
            Assert.fail("Exception thrown while waiting for service api shutdown termination: " + e.getMessage());

        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactory#connect(java.lang.String, int, boolean, boolean, int, boolean, boolean, long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testConnectServiceStringIntBooleanBooleanIntBooleanBooleanLongTimeUnit() {
        boolean bolResult = false;
        
        try {
            IQueryService   qs = DpQueryApiFactory.connect(
                    CFG_DEFAULT.channel.host.url, 
                    CFG_DEFAULT.channel.host.port,
                    CFG_DEFAULT.channel.tls.active,
                    CFG_DEFAULT.channel.grpc.usePlainText,
                    CFG_DEFAULT.channel.grpc.messageSizeMax,
                    CFG_DEFAULT.channel.grpc.keepAliveWithoutCalls,
                    CFG_DEFAULT.channel.grpc.gzip,
                    CFG_DEFAULT.channel.grpc.timeoutLimit, 
                    CFG_DEFAULT.channel.grpc.timeoutUnit
                    );
            
            bolResult = qs.shutdown();
            
            Assert.assertTrue("Service API shutdown reported failure", bolResult);
            
            bolResult = qs.awaitTermination();
            
            Assert.assertTrue("Termination wait failure - service API apparently failed to shut down.", bolResult);
            
        } catch (DpGrpcException e) {
            Assert.fail("Connection factory creation exception: " + e.getMessage());

        } catch (InterruptedException e) {
            Assert.fail("Exception thrown while waiting for service api shutdown termination: " + e.getMessage());

        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactory#connectService(java.io.File, java.io.File, java.io.File)}.
//     */
//    @Test
//    public final void testConnectServiceFileFileFile() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactory#connectService(java.lang.String, int, java.io.File, java.io.File, java.io.File)}.
//     */
//    @Test
//    public final void testConnectServiceStringIntFileFileFile() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactory#connectService(java.lang.String, int, java.io.File, java.io.File, java.io.File, boolean, int, boolean, boolean, long, java.util.concurrent.TimeUnit)}.
//     */
//    @Test
//    public final void testConnectServiceStringIntFileFileFileBooleanIntBooleanBooleanLongTimeUnit() {
//        fail("Not yet implemented"); // TODO
//    }

}
