/*
 * Project: dp-api-common
 * File:	DpAnnotationConnectionFactoryStaticTest.java
 * Package: com.ospreydcs.dp.api.grpc.annotate
 * Type: 	DpAnnotationConnectionFactoryStaticTest
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
 * @since Feb 6, 2025
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.annotate;

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
 * JUnit test cases for class <code>DpAnnotationConnectionFactoryStatic</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 6, 2025
 *
 */
public class DpAnnotationConnectionFactoryStaticTest {

    
    //
    // Application Resources
    //
    
    /** Default connection parameters for the Annotation Service */
    public static final DpGrpcConnectionConfig  CFG_DEFAULT = DpApiConfig.getInstance().connections.annotation;
    
    
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
     * Test method for {@link com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnectionFactoryStatic#connect()}.
     */
    @Test
    public final void testConnect() {
        
        try {
            DpAnnotationConnection  connTest = DpAnnotationConnectionFactoryStatic.connect();
            
            connTest.shutdownSoft();
            connTest.awaitTermination();
            
        } catch (DpGrpcException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown connecting to Annotation Service: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown connecting to Annotation Service: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnectionFactoryStatic#connect(java.lang.String, int)}.
     */
    @Test
    public final void testConnectStringInt() {
        
        // Test Parameters
        final String    strUrl = CFG_DEFAULT.channel.host.url;
        final int       intPort = CFG_DEFAULT.channel.host.port;
        
        try {
            DpAnnotationConnection  connTest = DpAnnotationConnectionFactoryStatic.connect(strUrl, intPort);
            
            connTest.shutdownSoft();
            connTest.awaitTermination();

        } catch (DpGrpcException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown connecting to Annotation Service: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown connecting to Annotation Service: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnectionFactoryStatic#connect(java.lang.String, int, boolean)}.
     */
    @Test
    public final void testConnectStringIntBoolean() {

        // Test Parameters
        final String    strUrl = CFG_DEFAULT.channel.host.url;
        final int       intPort = CFG_DEFAULT.channel.host.port;
        final boolean   bolPlain = CFG_DEFAULT.channel.grpc.usePlainText;
        
        try {
            DpAnnotationConnection  connTest = DpAnnotationConnectionFactoryStatic.connect(strUrl, intPort, bolPlain);
            
            connTest.shutdownSoft();
            connTest.awaitTermination();

        } catch (DpGrpcException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown connecting to Annotation Service: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown connecting to Annotation Service: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnectionFactoryStatic#connect(java.lang.String, int, boolean, long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testConnectStringIntBooleanLongTimeUnit() {

        // Test Parameters
        final String    strUrl = CFG_DEFAULT.channel.host.url;
        final int       intPort = CFG_DEFAULT.channel.host.port;
        final boolean   bolTlsAct = CFG_DEFAULT.channel.tls.active;
        final boolean   bolPlain = CFG_DEFAULT.channel.grpc.usePlainText;
        final int       intMaxMsg = CFG_DEFAULT.channel.grpc.messageSizeMax;
        final boolean   bolKeepAlive = CFG_DEFAULT.channel.grpc.keepAliveWithoutCalls;
        final boolean   bolGzipCmp = CFG_DEFAULT.channel.grpc.gzip;
        final long      lngTimeout = CFG_DEFAULT.channel.grpc.timeoutLimit;
        final TimeUnit  tuTimeout = CFG_DEFAULT.channel.grpc.timeoutUnit;
        
        try {
            DpAnnotationConnection  connTest = DpAnnotationConnectionFactoryStatic.connect(strUrl, 
                    intPort, 
                    bolTlsAct, 
                    bolPlain,
                    intMaxMsg,
                    bolKeepAlive,
                    bolGzipCmp,
                    lngTimeout, 
                    tuTimeout);
            
            connTest.shutdownSoft();
            connTest.awaitTermination();

        } catch (DpGrpcException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown connecting to Annotation Service: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown connecting to Annotation Service: " + e.getMessage());
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnectionFactoryStatic#connect(java.lang.String, int, boolean, boolean, int, boolean, boolean, long, java.util.concurrent.TimeUnit)}.
//     */
//    @Test
//    public final void testConnectStringIntBooleanBooleanIntBooleanBooleanLongTimeUnit() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnectionFactoryStatic#connect(java.io.File, java.io.File, java.io.File)}.
//     */
//    @Test
//    public final void testConnectFileFileFile() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnectionFactoryStatic#connect(java.lang.String, int, java.io.File, java.io.File, java.io.File)}.
//     */
//    @Test
//    public final void testConnectStringIntFileFileFile() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnectionFactoryStatic#connect(java.lang.String, int, java.io.File, java.io.File, java.io.File, boolean, int, boolean, boolean, long, java.util.concurrent.TimeUnit)}.
//     */
//    @Test
//    public final void testConnectStringIntFileFileFileBooleanIntBooleanBooleanLongTimeUnit() {
//        fail("Not yet implemented"); // TODO
//    }

}
