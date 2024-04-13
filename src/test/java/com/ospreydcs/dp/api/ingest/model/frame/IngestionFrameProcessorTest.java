/*
 * Project: dp-api-common
 * File:	IngestionFrameProcessorTest.java
 * Package: com.ospreydcs.dp.api.ingest.model.frame
 * Type: 	IngestionFrameProcessorTest
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
 * @since Apr 13, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.frame;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.ingest.model.IngestionFrame;
import com.ospreydcs.dp.api.ingest.test.TestIngestionFrameGenerator;

/**
 * <p>
 * JUnit test cases for class <code>IngestionFrameProcessor</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 13, 2024
 *
 */
public class IngestionFrameProcessorTest {

    
    //
    // Test Constants
    //
    
    /** The data provider UID used for all <code>IngestDataRequest</code> messages. */
    public static final int         INT_PROVIDER_ID = 1;
    
    
    //
    // Test Resources
    //
    
    /** A list of test data frames that are small */
    private static final List<IngestionFrame>   LST_FRAMES_SMALL = createDoubleFrames(10, 10, 10);
    
    /** A list of test data frames that have moderate allocation */
    private static final List<IngestionFrame>   LST_FRAMES_MOD = createDoubleFrames(10, 100, 100);
    
    
    /** A processor used for general testing -  activated for each test case in default configuration */
    private IngestionFrameProcessor processorTest;
    
    
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
        this.processorTest = IngestionFrameProcessor.from(INT_PROVIDER_ID);
        this.processorTest.activate();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        this.processorTest.shutdown();
    }

    
    // 
    // Test Cases
    //
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#from(int)}.
     */
    @Test
    public final void testFrom() {
        IngestionFrameProcessor     processor = IngestionFrameProcessor.from(INT_PROVIDER_ID);
        
        // Start it up 
        boolean bolActivated = processor.activate();
        
        Assert.assertTrue(bolActivated);
        
        // Shut it down and wait
        try {
            boolean bolShutdown = processor.shutdown();
            
            Assert.assertTrue(bolShutdown);
            
        } catch (InterruptedException e) {
            Assert.fail("Processor throw InterruptedException while waiting for shutdown: " + e.getMessage());
        }
        
        Assert.assertFalse(processor.isActive());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#IngestionFrameProcessor(int)}.
     */
    @Test
    public final void testIngestionFrameProcessor() {
        IngestionFrameProcessor     processor = new IngestionFrameProcessor(INT_PROVIDER_ID);
        
        // Start it up 
        boolean bolActivated = processor.activate();
        
        Assert.assertTrue(bolActivated);
        
        // Shut it down hard 
        processor.shutdownNow();
        
        Assert.assertFalse(processor.isActive());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#enableConcurrency(int)}.
     */
    @Test
    public final void testEnableConcurrency() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#disableConcurrency()}.
     */
    @Test
    public final void testDisableConcurrency() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#enableFrameDecomposition(long)}.
     */
    @Test
    public final void testEnableFrameDecomposition() {
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#disableFrameDecomposition()}.
     */
    @Test
    public final void testDisableFrameDecomposition() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#enableBackPressure(int)}.
     */
    @Test
    public final void testEnableBackPressure() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#disableBackPressure()}.
     */
    @Test
    public final void testDisableBackPressure() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#activate()}.
     */
    @Test
    public final void testActivate() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#shutdown()}.
     */
    @Test
    public final void testShutdown() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#shutdownNow()}.
     */
    @Test
    public final void testShutdownNow() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#addFrame(com.ospreydcs.dp.api.ingest.model.IngestionFrame)}.
     */
    @Test
    public final void testAddFrame() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#addFrames(java.util.List)}.
     */
    @Test
    public final void testAddFrames() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#awaitBackPressure()}.
     */
    @Test
    public final void testAwaitBackPressure() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#isActive()}.
     */
    @Test
    public final void testIsActive() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#take()}.
     */
    @Test
    public final void testTake() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#poll()}.
     */
    @Test
    public final void testPoll() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor#poll(long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testPollLongTimeUnit() {
        fail("Not yet implemented"); // TODO
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
