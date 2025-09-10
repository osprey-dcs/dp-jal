/*
 * Project: dp-api-common
 * File:	DpQueryConnectionFactoryTest.java
 * Package: com.ospreydcs.dp.api.grpc.query
 * Type: 	DpQueryConnectionFactoryTest
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
 * @since Jan 14, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.query;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.JalConfig;
import com.ospreydcs.dp.api.config.JalUnitTestConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;

/**
 * <p>
 * JUnit test cases for class <code>{@link DpQueryConnectionFactory}</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 14, 2024
 *
 */
public class DpQueryConnectionFactoryTest {

    
    //
    // Application Resources
    //
    
    /** The DP API Library Query Service default configuration parameters */
    private static final DpGrpcConnectionConfig.Channel   CFG_DEFAULT = JalConfig.getInstance().connections.query.channel;

    /** The DP API Library Query Service testing configuration parameters */
    private static final DpGrpcConnectionConfig           CFG_TESTING = JalUnitTestConfig.getInstance().testQuery.connection;

    
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
     * Test method for {@link com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory#getFactory()}.
     */
    @Test
    public final void testGetFactory() {
        DpQueryConnectionFactory    factory = DpQueryConnectionFactory.getFactory();
        
        Assert.assertEquals(DpQueryConnectionFactory.FACTORY, factory);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory#newFactory(com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig)}.
     */
    @Test
    public final void testNewFactory() {
        DpQueryConnectionFactory    factory = DpQueryConnectionFactory.newFactory(CFG_TESTING);

        Assert.assertNotEquals(DpQueryConnectionFactory.FACTORY, factory);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactoryBase#connect()}.
     */
    @Test
    public final void testConnect() {
        try {
            DpQueryConnection   conn = DpQueryConnectionFactory.FACTORY.connect();
            
            conn.shutdownSoft();
            conn.awaitTermination();
            
            Assert.assertTrue("Connection failed to terminated in alloted time", conn.isTerminated() );
            
        } catch (DpGrpcException e) {
            fail("Threw DpGrpcExcecption: " + e.getMessage()); 
            
        } catch (InterruptedException e) {
            fail("Process exception while waiting for termination: " + e.getMessage()); 
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactoryBase#connect(java.lang.String, int)}.
     */
    @Test
    public final void testConnectStringInt() {
        String  strUrl = CFG_DEFAULT.host.url;
        int     intPort = CFG_DEFAULT.host.port;
        
        try {
            DpQueryConnection   conn = DpQueryConnectionFactory.FACTORY.connect(strUrl, intPort);

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
     * Test method for {@link com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactoryBase#connect(java.lang.String, int, boolean)}.
     */
    @Test
    public final void testConnectStringIntBoolean() {
        String  strUrl = CFG_DEFAULT.host.url;
        int     intPort = CFG_DEFAULT.host.port;
        
        boolean bolPlainText = CFG_DEFAULT.grpc.usePlainText;
        
        try {
            DpQueryConnection   conn = DpQueryConnectionFactory.FACTORY.connect(strUrl, intPort, bolPlainText);

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
     * Test method for {@link com.ospreydcs.dp.api.grpc.model.DpGrpcConnectionFactoryBase#connect(java.lang.String, int, boolean, long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testConnectStringIntBooleanLongTimeUnit() {
        String  strUrl = CFG_DEFAULT.host.url;
        int     intPort = CFG_DEFAULT.host.port;
        
        boolean bolPlainText = CFG_DEFAULT.grpc.usePlainText;
        
        long    lngTmout = CFG_DEFAULT.grpc.timeoutLimit;
        TimeUnit tuTmout = CFG_DEFAULT.grpc.timeoutUnit;
        
        try {
            DpQueryConnection   conn = DpQueryConnectionFactory.FACTORY.connect(strUrl, intPort,
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

}
