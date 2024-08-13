/*
 * Project: dp-api-common
 * File:	IngestionChannelEvaluatorTest.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IngestionChannelEvaluatorTest
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
 * @since Aug 11, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.grpc;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnectionFactory;
import com.ospreydcs.dp.api.model.DpGrpcStreamType;

/**
 * <p>
 * JUnit driver for <code>IngestionChannelEvaluator</code> class instances.
 * </p>
 * <p>
 * Contains configurations and runs evaluations.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 11, 2024
 *
 */
public class IngestionChannelEvaluatorTest {

    
    //
    // Class Constants
    //
    
    /** Location of performance results output */
    public static final String          STR_PATH_OUTPUT = "test/output/ingest/channel/";
    
    
    //
    // Class Resources
    //
    
    /** The connection to the Ingestion Service used by all tests */
    private static DpIngestionConnection    connIngest;
    
    /** Print output stream for storing evaluation results to single file */
    private static PrintStream              psOutput;
    
    
    //
    // Test Configurations
    //
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_1_STRM_BIDI = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.BIDIRECTIONAL, 1);
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_2_STRM_BIDI = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.BIDIRECTIONAL, 2);
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_3_STRM_BIDI = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.BIDIRECTIONAL, 3);
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_4_STRM_BIDI = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.BIDIRECTIONAL, 4);
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_5_STRM_BIDI = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.BIDIRECTIONAL, 5);
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_6_STRM_BIDI = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.BIDIRECTIONAL, 6);
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_7_STRM_BIDI = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.BIDIRECTIONAL, 7);
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_8_STRM_BIDI = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.BIDIRECTIONAL, 8);
    
    
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_1_STRM_FWD = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.FORWARD, 1);
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_2_STRM_FWD = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.FORWARD, 2);
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_3_STRM_FWD = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.FORWARD, 3);
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_4_STRM_FWD = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.FORWARD, 4);
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_5_STRM_FWD = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.FORWARD, 5);
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_6_STRM_FWD = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.FORWARD, 6);
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_7_STRM_FWD = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.FORWARD, 7);
    
    /** <code>IngestionChannelEvaluator</code> test configuration */ 
    public static final IngestionChannelEvaluator.IngestionChannelEvaluatorConfig   CFG_8_STRM_FWD = 
            new IngestionChannelEvaluator.IngestionChannelEvaluatorConfig(DpGrpcStreamType.FORWARD, 8);
    
    
    //
    // Test Suites
    //
    
    /** Suite of bidirectional data stream configurations */
    public static final List<IngestionChannelEvaluator.IngestionChannelEvaluatorConfig> LST_CFG_BIDI = List.of(
            CFG_1_STRM_BIDI,
            CFG_2_STRM_BIDI,
            CFG_3_STRM_BIDI,
            CFG_4_STRM_BIDI,
            CFG_5_STRM_BIDI,
            CFG_6_STRM_BIDI,
            CFG_7_STRM_BIDI,
            CFG_8_STRM_BIDI
            );
    
    /** Suite of unidirectional (forward) data stream configurations */
    public static final List<IngestionChannelEvaluator.IngestionChannelEvaluatorConfig> LST_CFG_FWD = List.of(
            CFG_1_STRM_FWD,
            CFG_2_STRM_FWD,
            CFG_3_STRM_FWD,
            CFG_4_STRM_FWD,
            CFG_5_STRM_FWD,
            CFG_6_STRM_FWD,
            CFG_7_STRM_FWD,
            CFG_8_STRM_FWD
            );
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        // Open the Ingestion Service connection
        connIngest = DpIngestionConnectionFactory.FACTORY.connect();
        
        // Open the common output file
        String  strFileName = IngestionChannelEvaluatorTest.class.getSimpleName() + "-" + Instant.now().toString() + ".txt";
        Path    pathOutput = Paths.get(STR_PATH_OUTPUT, strFileName);
        File    fileOutput = pathOutput.toFile();
        
        psOutput = new PrintStream(fileOutput);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        
        // Close the Ingestion Service connection
        connIngest.shutdownSoft();
        
        // Close the common output file
        psOutput.close();
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
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannelEvaluator#evaluate(com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection)}.
     */
    @Test
    public final void testEvaluateBidi() {
        
        psOutput.println("Bidirectional Streaming Tests");
        
        try {
            this.runSuite(LST_CFG_BIDI);
            
        } catch (CompletionException e) {
            Assert.fail("Evaluation failed for configuration - " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.grpc.IngestionChannelEvaluator#evaluate(com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection)}.
     * 
     * Unidirectional streaming is not implemented until version 1.5
     */
//    @Test
    public final void testEvaluateForward() {

        psOutput.println("Unidirectional Streaming Tests");
        
        try {
            this.runSuite(LST_CFG_FWD);
            
        } catch (CompletionException e) {
            Assert.fail("Evaluation failed for configuration - " + e.getMessage());
            
        }
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Performs evaluation test on all given configurations and stores the results to the common output file.
     * </p>
     *  
     * @param lstCfgs   suite of performance evaluation configurations
     * 
     * @throws CompletionException  a given configuration failed the evaluation (see detail message) 
     */
    private void runSuite(List<IngestionChannelEvaluator.IngestionChannelEvaluatorConfig> lstCfgs) throws CompletionException {
        
        for (IngestionChannelEvaluator.IngestionChannelEvaluatorConfig cfg : lstCfgs) {
            
            String  strCfgName = cfg.toString();
            
            // Create the evaluation test from configuration
            IngestionChannelEvaluator   test = new IngestionChannelEvaluator(cfg);
            
            // Perform evaluation test and check for success
            boolean bolResult = test.evaluate(connIngest);
            
            if (!bolResult) {
                ResultStatus    recStatus = test.getStatus();
                
                throw new CompletionException(strCfgName + " failed to complete: " + recStatus.message(), recStatus.cause());
            }
            
            // Store results
            psOutput.println();
            test.printResults(psOutput, strCfgName);
        }
    }
}
