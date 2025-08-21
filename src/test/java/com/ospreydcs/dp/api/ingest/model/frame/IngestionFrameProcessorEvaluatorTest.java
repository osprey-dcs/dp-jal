/*
 * Project: dp-api-common
 * File:	IngestionFrameProcessorEvaluatorTest.java
 * Package: com.ospreydcs.dp.api.ingest.model.frame
 * Type: 	IngestionFrameProcessorEvaluatorTest
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
 * @since Aug 2, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.frame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for evaluating the <code>IngestionFrameProcessor</code> performance and operations
 * using <code>IngestionFrameProcessorEvaluator</code> instances.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 2, 2024
 *
 */
public class IngestionFrameProcessorEvaluatorTest {

    
    //
    // Class Constants
    //
    
    /** Location of performance results output */
    public static final String          STR_PATH_OUTPUT = "test/output/ingest/frame/";
    
    
    /** Timeout limit for polling operations */
    public static final long            LNG_TIMEOUT = 20;
    
    /** Timeout unit for polling operations */
    public static final TimeUnit        TU_TIMEOUT = TimeUnit.MILLISECONDS;
    
    
    //
    // Test Configurations
    //
    
    /** Standard Payload: frame count */
    public static final int     CNT_FRAMES = 15;
    
    /** Standard Payload: number of table columns */
    public static final int     CNT_COLS = 1000;
    
    /** Standard Payload: number of table rows */
    public static final int     CNT_ROWS = 1000;
    
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_FALSE_DECMP_100K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, false, 1, true, 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_FALSE_DECMP_200K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, false, 1, true, 2 * 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_FALSE_DECMP_400K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, false, 1, true, 4 * 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);

    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_FALSE_DECMP_1M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, false, 1, true, 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_FALSE_DECMP_2M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, false, 1, true, 2 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_FALSE_DECMP_4M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, false, 1, true, 4 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);

    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_1_DECMP_1M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 1, true, 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_2_DECMP_1M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 2, true, 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_3_DECMP_1M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 3, true, 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_4_DECMP_1M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 4, true, 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_5_DECMP_1M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 5, true, 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_6_DECMP_1M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 6, true, 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_1_DECMP_2M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 1, true, 2 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_2_DECMP_2M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 2, true, 2 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_3_DECMP_2M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 3, true, 2 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_1_DECMP_4M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 1, true, 4 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_2_DECMP_4M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 2, true, 4 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_3_DECMP_4M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 3, true, 4 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_4_DECMP_4M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 4, true, 4 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_5_DECMP_4M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 5, true, 4 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_6_DECMP_4M = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 6, true, 4 * 1_000_000L, LNG_TIMEOUT, TU_TIMEOUT);
    
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_1_DECMP_400K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 1, true, 4 * 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);

    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_2_DECMP_400K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 2, true, 4 * 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_3_DECMP_400K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 3, true, 4 * 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_4_DECMP_400K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 4, true, 4 * 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_5_DECMP_400K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 5, true, 4 * 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);
    
    /** IngestionFrameProcessorEvaluation Test configuration */
    public static final IngestionFrameProcessorEvalConfig   CFG_MTHRD_6_DECMP_400K = 
            IngestionFrameProcessorEvalConfig.from(CNT_FRAMES, CNT_COLS, CNT_ROWS, true, 6, true, 4 * 1_000_00L, LNG_TIMEOUT, TU_TIMEOUT);
    
    
    
    //
    // Test Suites
    //
    
    /** Suite of IngestionFrameProcessorEvaluation test configurations */
    public static final List<IngestionFrameProcessorEvalConfig> LST_CFG_400K = List.of(
            CFG_MTHRD_FALSE_DECMP_400K,
            CFG_MTHRD_1_DECMP_400K,
            CFG_MTHRD_2_DECMP_400K,
            CFG_MTHRD_3_DECMP_400K,
            CFG_MTHRD_4_DECMP_400K,
            CFG_MTHRD_5_DECMP_400K,
            CFG_MTHRD_6_DECMP_400K
            );
    
    /** Suite of IngestionFrameProcessorEvaluation test configurations */
    public static final List<IngestionFrameProcessorEvalConfig> LST_CFG_1M = List.of(
            CFG_MTHRD_FALSE_DECMP_1M,
            CFG_MTHRD_1_DECMP_1M,
            CFG_MTHRD_2_DECMP_1M,
            CFG_MTHRD_3_DECMP_1M,
            CFG_MTHRD_4_DECMP_1M,
            CFG_MTHRD_5_DECMP_1M,
            CFG_MTHRD_6_DECMP_1M
            );
    
    /** Suite of IngestionFrameProcessorEvaluation test configurations */
    public static final List<IngestionFrameProcessorEvalConfig> LST_CFG_4M = List.of(
            CFG_MTHRD_FALSE_DECMP_4M,
            CFG_MTHRD_1_DECMP_4M,
            CFG_MTHRD_2_DECMP_4M,
            CFG_MTHRD_3_DECMP_4M,
            CFG_MTHRD_4_DECMP_4M,
            CFG_MTHRD_5_DECMP_4M,
            CFG_MTHRD_6_DECMP_4M
            );
    
    
    //
    // Class Resources
    //
    
    /** Print output stream for storing evaluation results to single file */
    private static PrintStream  psOutput;
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        // Open the common output file
        String  strFileName = IngestionFrameProcessorEvaluatorTest.class.getSimpleName() + "-" + Instant.now().toString() + ".txt";
        Path    pathOutput = Paths.get(STR_PATH_OUTPUT, strFileName);
        File    fileOutput = pathOutput.toFile();
        
        psOutput = new PrintStream(fileOutput);
        
        // Print Common Parameters
        psOutput.println("Common Payload - Table of Double Values");
        psOutput.println("  Frames count     :  " + CNT_FRAMES);
        psOutput.println("  Colums per frame : " + CNT_COLS);
        psOutput.println("  Rows per frame   : " + CNT_ROWS);
        psOutput.println();
        
        psOutput.println("Common Polling Parameters");
        psOutput.println("  Timeout limit : " + LNG_TIMEOUT + " " + TU_TIMEOUT);
        psOutput.println();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        
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
     * Single configuration performance evaluation for <code>IngestionFrameProcessor</code>.
     */
//    @Test
    public final void testPollMultiThread3Decomp4M() {
        
        IngestionFrameProcessorEvalConfig   cfgTest = CFG_MTHRD_3_DECMP_4M;
        
        try {
            // Run test
            IngestionFrameProcessorEvaluator  test = new IngestionFrameProcessorEvaluator();
            test.evaluatePolling(cfgTest);

            // Print out test results to standard output
            test.printAll(System.out, "TEST: " + JavaRuntime.getQualifiedMethodNameSimple());
            System.out.println("Output file name = " + cfgTest.createOutputFileName());
            
        } catch (Exception e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "evaluatePolling()", e));
        }
    }

    /**
     * Single configuration performance evaluation for <code>IngestionFrameProcessor</code>.
     */
//    @Test
    public final void testTakeMultiThread3Decomp4M() {
        
        IngestionFrameProcessorEvalConfig   cfgTest = CFG_MTHRD_3_DECMP_4M;
        
        // Run test
        try {
            IngestionFrameProcessorEvaluator  test = new IngestionFrameProcessorEvaluator();
            test.evaluateTake(cfgTest);

            // Save the test results
            this.storeResults(test, JavaRuntime.getMethodName());
            
        } catch (Exception e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "evaluateTake()", e));
            return;
        }
    }
    
    /**
     * Configuration suite performance evaluation for <code>IngestionFrameProcessor</code>.
     */
    @Test
    public final void testSuite400KDecmp() {

        try {
            this.runSuite(LST_CFG_400K);
            
        } catch (Exception e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), e.getCause().getMessage(), e));
        }
    }
    
    /**
     * Configuration suite performance evaluation for <code>IngestionFrameProcessor</code>.
     */
    @Test
    public final void testSuite1MDecmp() {

        try {
            this.runSuite(LST_CFG_1M);
            
        } catch (Exception e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), e.getCause().getMessage(), e));
        }
    }
    
    /**
     * Configuration suite performance evaluation for <code>IngestionFrameProcessor</code>.
     */
    @Test
    public final void testSuite4MDecmp() {

        try {
            this.runSuite(LST_CFG_4M);
            
        } catch (Exception e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), e.getCause().getMessage(), e));
        }
    }
    
    
    //
    // Support Methods
    //

    /**
     * <p>
     * Runs all performance evaluation(s) and stores results to common file for the given collection.
     * </p>
     *  
     * @param lstCfgs       suite of performance evaluation configurations
     * 
     * @throws Exception    take() or poll(lng, TimeUnit) threw an exception (see cause)
     */
    private void runSuite(List<IngestionFrameProcessorEvalConfig> lstCfgs) throws Exception {
        
        // Run test for each configuration in the suit
        for (IngestionFrameProcessorEvalConfig cfgTest : lstCfgs) {
            
            String  strCfgName = cfgTest.createConfigName();
            
            // Run test and store results
            try {
                // Create performance evaluator
                IngestionFrameProcessorEvaluator test = new IngestionFrameProcessorEvaluator();
                
                test.evaluateTake(cfgTest);
                test.printAll(psOutput, strCfgName + "take()");
                psOutput.println();
                
            }   catch (Exception e) {
                throw new Exception("Configuration " + strCfgName + ": take() exception", e);
            }
            
            try {
                // Create performance evaluator
                IngestionFrameProcessorEvaluator test = new IngestionFrameProcessorEvaluator();
                
                test.evaluatePolling(cfgTest);
                test.printAll(psOutput, strCfgName + "poll()");
                psOutput.println();
                
            }   catch (Exception e) {
                throw new Exception("Configuration " + strCfgName + ": poll() exception", e);
            }
        }
    }
    /**
     * <p>
     * Saves the individual test results to a a new file with the given name.
     * </p>
     * <p>
     * The test results are stored to a new file with the given name within the
     * path location identified by class constant <code>{@link #STR_OUTPUT_PATH_DEF}</code>.
     * A header is provided within the file using the given name of the testing method.
     * </p>
     * <p>
     * The file name is that created by the configuration (i.e., 
     * <code>{@link IngestionFrameProcessorEvalConfig#createOutputFileName()</code>.
     * </p>
     * 
     * @param test          the test that was performed
     * @param strTest       the method name that performed the test
     * 
     */
    private void storeResults(IngestionFrameProcessorEvaluator test, String strTest) {
        
        String strFileName = test.config.createOutputFileName();
        Path    pathOutput = Paths.get(STR_PATH_OUTPUT, strFileName);
        File    fileOutput = pathOutput.toFile();
        
        try {
            PrintStream osResults = new PrintStream(fileOutput);

            test.printAll(osResults, "TEST: " + strTest);
            osResults.close();

        } catch (FileNotFoundException e) {
            Assert.fail(this.createFailExceptionMessage(JavaRuntime.getQualifiedMethodNameSimple(), "storeResults()", e));
        }
    }

    /**
     * <p>
     * Creates a failure message from the given arguments.
     * </p>
     * <p>
     * The strings are concatenated with a failure notification along with the type of the exception and its
     * detail message.
     * </p>
     * 
     * @param strTest       name of the test case
     * @param strMethod     name of the method under test
     * @param e             exception thrown
     * 
     * @return              failure message
     */
    private String  createFailExceptionMessage(String strTest, String strMethod, Exception e) {
        
        String buf = strTest + " - " + strMethod + " failed with exception " 
                            + e.getClass().getName() 
                            + ": " + e.getMessage();
        
        return buf;
    }
    
}
