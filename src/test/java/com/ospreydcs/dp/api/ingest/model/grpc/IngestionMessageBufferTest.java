/*
 * Project: dp-api-common
 * File:	IngestionMessageBufferTest.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IngestionMessageBufferTest
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
 * @since Aug 4, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.grpc;

import org.junit.Assert;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.ingest.test.TestIngestDataRequestGenerator;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * JUnit test cases for class <code>IngestionMessageBuffer</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 4, 2024
 *
 */
public class IngestionMessageBufferTest {

    
    //
    // Test Resources
    //
    
    /** Collection of data request messages available for all tests */
    private static final List<IngestDataRequest>    LST_RQSTS = TestIngestDataRequestGenerator.createDoublesMessagesWithClock();
    
    
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#create()}.
     */
    @Test
    public final void testCreate() {
        
        IngestionMessageBuffer  buffer = IngestionMessageBuffer.create();
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#create(int)}.
     */
    @Test
    public final void testCreateInt() {
        
        // Parameters
        final   int     szCapacity = 101;     
        
        IngestionMessageBuffer  buffer = IngestionMessageBuffer.create(szCapacity);
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertEquals(szCapacity, buffer.getQueueCapacity());
        Assert.assertFalse(buffer.isSupplying());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#create(int, boolean)}.
     */
    @Test
    public final void testCreateIntBoolean() {
        
        // Parameters
        final int       szCapacity = 101;
        final boolean   bolBackPressure = false;
        
        IngestionMessageBuffer  buffer = IngestionMessageBuffer.create(szCapacity, bolBackPressure);
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertEquals(szCapacity, buffer.getQueueCapacity());
        Assert.assertEquals(bolBackPressure, buffer.hasBackPressure());
        Assert.assertFalse(buffer.isSupplying());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#IngestionMessageBuffer()}.
     */
    @Test
    public final void testIngestionMessageBuffer() {
        
        IngestionMessageBuffer  buffer = new IngestionMessageBuffer();
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#IngestionMessageBuffer(int)}.
     */
    @Test
    public final void testIngestionMessageBufferInt() {
        
        // Parameters
        final   int     szCapacity = 101;     
        
        IngestionMessageBuffer  buffer = new IngestionMessageBuffer(szCapacity);
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertEquals(szCapacity, buffer.getQueueCapacity());
        Assert.assertFalse(buffer.isSupplying());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#IngestionMessageBuffer(int, boolean)}.
     */
    @Test
    public final void testIngestionMessageBufferIntBoolean() {
        
        // Parameters
        final int       szCapacity = 101;
        final boolean   bolBackPressure = false;
        
        IngestionMessageBuffer  buffer = new IngestionMessageBuffer(szCapacity, bolBackPressure);
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertEquals(szCapacity, buffer.getQueueCapacity());
        Assert.assertEquals(bolBackPressure, buffer.hasBackPressure());
        Assert.assertFalse(buffer.isSupplying());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#setQueueCapcity(int)}.
     */
    @Test
    public final void testSetQueueCapcity() {
        
        // Parameters
        final int       szCapacity = 42;
        
        IngestionMessageBuffer  buffer = IngestionMessageBuffer.create();
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        Assert.assertNotEquals(szCapacity, buffer.getQueueCapacity());
        buffer.setQueueCapcity(szCapacity);
        Assert.assertEquals(szCapacity, buffer.getQueueCapacity());
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#enableBackPressure()}.
     */
    @Test
    public final void testEnableBackPressure() {
        
        IngestionMessageBuffer  buffer = IngestionMessageBuffer.create();
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());

        buffer.enableBackPressure();
        Assert.assertTrue(buffer.hasBackPressure());
        
        buffer.disableBackPressure();
        Assert.assertFalse(buffer.hasBackPressure());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#disableBackPressure()}.
     */
    @Test
    public final void testDisableBackPressure() {
        
        IngestionMessageBuffer  buffer = IngestionMessageBuffer.create();
        
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());

        buffer.enableBackPressure();
        Assert.assertTrue(buffer.hasBackPressure());
        
        buffer.disableBackPressure();
        Assert.assertFalse(buffer.hasBackPressure());
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#hasBackPressure()}.
//     */
//    @Test
//    public final void testHasBackPressure() {
//        Assert.fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#getQueueCapacity()}.
//     */
//    @Test
//    public final void testGetQueueCapacity() {
//        Assert.fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#activate()}.
     */
    @Test
    public final void testActivate() {
        
        // Parameters
        final int       szCapacity = 101;
        final boolean   bolBackPressure = false;
        
        IngestionMessageBuffer  buffer = new IngestionMessageBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#shutdown()}.
     */
    @Test
    public final void testShutdown() {
        
        // Parameters
        final int       szCapacity = 101;
        final boolean   bolBackPressure = false;
        
        IngestionMessageBuffer  buffer = new IngestionMessageBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        
        try {
            boolean bolResult = buffer.shutdown();
            Assert.assertTrue(bolResult);
            Assert.assertFalse(buffer.isSupplying());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception thrown during shutdown: " + e.getMessage());
        }
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#shutdownNow()}.
     */
    @Test
    public final void testShutdownNow() {
        
        // Parameters
        final int       szCapacity = 101;
        final boolean   bolBackPressure = false;
        
        IngestionMessageBuffer  buffer = new IngestionMessageBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());

        buffer.shutdownNow();
        Assert.assertFalse(buffer.isSupplying());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#enqueue(com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest)}.
     */
    @Test
    public final void testEnqueueIngestDataRequest() {
        
        // Parameters
        final int       szCapacity = 101;
        final boolean   bolBackPressure = false;
        
        IngestionMessageBuffer  buffer = new IngestionMessageBuffer(szCapacity, bolBackPressure);
        Assert.assertNotEquals(null, buffer);
        Assert.assertFalse(buffer.isSupplying());
        
        buffer.activate();
        Assert.assertTrue(buffer.isSupplying());
        Assert.assertEquals(0, buffer.getQueueSize());
        
        IngestDataRequest   msgRqst = LST_RQSTS.get(0);
        
        try {
            buffer.enqueue(msgRqst);
            Assert.assertEquals(1, buffer.getQueueSize());
            
            buffer.shutdownNow();
            Assert.assertEquals(0, buffer.getQueueSize());
            Assert.assertFalse(buffer.isSupplying());
            
        } catch (IllegalStateException | InterruptedException e) {
            Assert.fail("Exception thrown during enqueue(): " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#enqueue(java.util.List)}.
     */
    @Test
    public final void testEnqueueListOfIngestDataRequest() {
        Assert.fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#awaitQueueReady()}.
     */
    @Test
    public final void testAwaitQueueReady() {
        Assert.fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#awaitQueueEmpty()}.
     */
    @Test
    public final void testAwaitQueueEmpty() {
        Assert.fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#isSupplying()}.
     */
    @Test
    public final void testIsSupplying() {
        Assert.fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#take()}.
     */
    @Test
    public final void testTake() {
        Assert.fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#poll()}.
     */
    @Test
    public final void testPoll() {
        Assert.fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionMessageBuffer#poll(long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testPollLongTimeUnit() {
        Assert.fail("Not yet implemented"); // TODO
    }

}
