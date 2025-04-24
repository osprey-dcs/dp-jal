/*
 * Project: dp-api-common
 * File:	DpQueryApiFactoryNewTest.java
 * Package: com.ospreydcs.dp.api.query
 * Type: 	DpQueryApiFactoryNewTest
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
 * @since Apr 24, 2025
 *
 */
package com.ospreydcs.dp.api.query;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

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
 * JUnit test cases for class <code>DpQueryApiFactoryNew</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 24, 2025
 *
 */
public class DpQueryApiFactoryNewTest {

    
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

    
    // 
    // Test Cases
    //
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactoryNew#connect()}.
     */
    @Test
    public final void testConnect() {
        boolean bolResult = false;
        
        try {
            IQueryService qs = DpQueryApiFactoryNew.connect();
            
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
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactoryNew#connect(java.lang.String, int)}.
     */
    @Test
    public final void testConnectStringInt() {

        // Test Parameters
        final String    strUrl = CFG_DEFAULT.channel.host.url;
        final int       intPort = CFG_DEFAULT.channel.host.port;
        
        // Connect interface, shutdown and inspect
        try {
            boolean bolResult = false;
            
            IQueryService   qs = DpQueryApiFactoryNew.connect(strUrl, intPort);
            
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
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactoryNew#connect(java.lang.String, int, boolean)}.
     */
    @Test
    public final void testConnectStringIntBoolean() {
        
        // Test Parameters
        final String    strUrl = CFG_DEFAULT.channel.host.url;
        final int       intPort = CFG_DEFAULT.channel.host.port;
        final boolean   bolPlainText = CFG_DEFAULT.channel.grpc.usePlainText;

        // Connect interface, shutdown and inspect
        try {
            boolean bolResult = false;
            
            IQueryService qs = DpQueryApiFactoryNew.connect(strUrl, intPort, bolPlainText);
            
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
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactoryNew#connect(java.lang.String, int, boolean, long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testConnectStringIntBooleanLongTimeUnit() {
        
        // Test Parameters
        final String    strUrl = CFG_DEFAULT.channel.host.url;
        final int       intPort = CFG_DEFAULT.channel.host.port;
        final boolean   bolPlainText = CFG_DEFAULT.channel.grpc.usePlainText;
        final long      lngTimeout = CFG_DEFAULT.channel.grpc.timeoutLimit;
        final TimeUnit  tuTimeout = CFG_DEFAULT.channel.grpc.timeoutUnit;

        // Connect interface, shutdown and inspect
        try {
            boolean bolResult = false;
            
            IQueryService qs = DpQueryApiFactoryNew.connect(
                    strUrl, 
                    intPort,
                    bolPlainText,
                    lngTimeout, 
                    tuTimeout
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
     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactoryNew#connect(java.lang.String, int, boolean, boolean, int, boolean, boolean, long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testConnectStringIntBooleanBooleanIntBooleanBooleanLongTimeUnit() {
        
        // Test Parameters
        final String    strUrl = CFG_DEFAULT.channel.host.url;
        final int       intPort = CFG_DEFAULT.channel.host.port;
        final boolean   bolTlsActive = CFG_DEFAULT.channel.tls.active;
        final boolean   bolPlainText = CFG_DEFAULT.channel.grpc.usePlainText;
        final int       intMsgSizeMax = CFG_DEFAULT.channel.grpc.messageSizeMax;
        final boolean   bolKeepAlive = CFG_DEFAULT.channel.grpc.keepAliveWithoutCalls;
        final boolean   bolGzip = CFG_DEFAULT.channel.grpc.gzip;
        final long      lngTimeout = CFG_DEFAULT.channel.grpc.timeoutLimit;
        final TimeUnit  tuTimeout = CFG_DEFAULT.channel.grpc.timeoutUnit;

        try {
            boolean bolResult = false;
            
            IQueryService   qs = DpQueryApiFactoryNew.connect(
                    strUrl, 
                    intPort,
                    bolTlsActive,
                    bolPlainText,
                    intMsgSizeMax,
                    bolKeepAlive,
                    bolGzip,
                    lngTimeout, 
                    tuTimeout
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
//     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactoryNew#connect(java.io.File, java.io.File, java.io.File)}.
//     */
//    @Test
//    public final void testConnectFileFileFile() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactoryNew#connect(java.lang.String, int, java.io.File, java.io.File, java.io.File)}.
//     */
//    @Test
//    public final void testConnectStringIntFileFileFile() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.query.DpQueryApiFactoryNew#connect(java.lang.String, int, java.io.File, java.io.File, java.io.File, boolean, int, boolean, boolean, long, java.util.concurrent.TimeUnit)}.
//     */
//    @Test
//    public final void testConnectStringIntFileFileFileBooleanIntBooleanBooleanLongTimeUnit() {
//        fail("Not yet implemented"); // TODO
//    }

}
