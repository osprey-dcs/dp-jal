/*
 * Project: dp-api-common
 * File:	DpQueryConnectionFactoryDeprecatedTest.java
 * Package: com.ospreydcs.dp.api.grpc.query
 * Type: 	DpQueryConnectionFactoryDeprecatedTest
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
 * @since Dec 29, 2023
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.query;

import static org.junit.Assert.fail;

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
 * JUnit test cases for <code>DpQueryConnectionFactoryStatic</code> class.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 29, 2023
 *
 */
public class DpQueryConnectionFactoryDeprecatedTest {

    
    //
    // Application Resources
    //
    
    /** The API Library default Query Service configuration parameters */
    private static final DpGrpcConnectionConfig.Channel   CFG_DEFAULT = DpApiConfig.getInstance().connections.query.channel;


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
     * Test method for {@link com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactoryStatic#connect()}.
     */
    @Test
    public final void testConnect() {
        try {
            DpQueryConnection   conn = DpQueryConnectionFactoryStatic.connect();
            
            conn.shutdownSoft();
            
        } catch (DpGrpcException e) {
            fail("Threw execption: " + e.getMessage());
            
        } catch (InterruptedException e) {
            fail("Shutdown threw InterrtupedException: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactoryStatic#connect(java.lang.String, int)}.
     */
    @Test
    public final void testConnectStringInt() {
        String  strUrl = CFG_DEFAULT.host.url;
        int     intPort = CFG_DEFAULT.host.port;
        
        try {
            DpQueryConnection   conn = DpQueryConnectionFactoryStatic.connect(strUrl, intPort);

            conn.shutdownSoft();
            conn.awaitTermination();

            Assert.assertTrue("Connection failed to terminated in alloted time", conn.isTerminated() );
            
        } catch (DpGrpcException e) {
            fail("Threw connection execption: " + e.getMessage()); 
            e.printStackTrace();
            
        } catch (InterruptedException e) {
            fail("Threw interrupted exception: " + e.getMessage());
            e.printStackTrace();
        }
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactoryStatic#connect(java.lang.String, int, boolean, long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testConnectStringIntBooleanLongTimeUnit() {
        String  strUrl = CFG_DEFAULT.host.url;
        int     intPort = CFG_DEFAULT.host.port;
        
        boolean bolPlainText = CFG_DEFAULT.grpc.usePlainText;
        
        long    lngTmout = CFG_DEFAULT.grpc.timeoutLimit;
        TimeUnit tuTmout = CFG_DEFAULT.grpc.timeoutUnit;
        
        try {
            DpQueryConnection   conn = DpQueryConnectionFactoryStatic.connect(strUrl, intPort,
                    bolPlainText,
                    lngTmout, 
                    tuTmout);

            conn.shutdownSoft();
            conn.awaitTermination();

            Assert.assertTrue("Connection failed to terminated in alloted time", conn.isTerminated() );
            
        } catch (DpGrpcException e) {
            fail("Threw connection execption: " + e.getMessage()); 
            e.printStackTrace();
            
        } catch (InterruptedException e) {
            fail("Threw interrupted exception: " + e.getMessage());
            e.printStackTrace();
        }
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactoryStatic#connect(java.lang.String, int, boolean, boolean, int, boolean, boolean, long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testConnectStringIntBooleanBooleanIntBooleanBooleanLongTimeUnit() {
        String  strUrl = CFG_DEFAULT.host.url;
        int     intPort = CFG_DEFAULT.host.port;
        
        boolean bolTlsActive = CFG_DEFAULT.tls.enabled;
        boolean bolPlainText = CFG_DEFAULT.grpc.usePlainText;
        int     intMaxSz = CFG_DEFAULT.grpc.messageSizeMax;
        boolean bolKeepAlive = CFG_DEFAULT.grpc.keepAliveWithoutCalls;
        boolean bolGzipCmp = CFG_DEFAULT.grpc.gzip;
        
        long    lngTmout = CFG_DEFAULT.grpc.timeoutLimit;
        TimeUnit tuTmout = CFG_DEFAULT.grpc.timeoutUnit;
        
        try {
            DpQueryConnection   conn = DpQueryConnectionFactoryStatic.connect(strUrl, intPort,
                    bolTlsActive,
                    bolPlainText, 
                    intMaxSz, 
                    bolKeepAlive, 
                    bolGzipCmp, 
                    lngTmout, 
                    tuTmout);

            conn.shutdownSoft();
            conn.awaitTermination();

            Assert.assertTrue("Connection failed to terminated in alloted time", conn.isTerminated() );
            
        } catch (DpGrpcException e) {
            fail("Threw connection execption: " + e.getMessage()); 
            e.printStackTrace();
            
        } catch (InterruptedException e) {
            fail("Threw interrupted exception: " + e.getMessage());
            e.printStackTrace();
        }
        
    }

}
