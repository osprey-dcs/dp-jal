/*
 * Project: dp-api-common
 * File:	DpAnnotationServiceApiImplTest.java
 * Package: com.ospreydcs.dp.api.annotate.impl
 * Type: 	DpAnnotationServiceApiImplTest
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
package com.ospreydcs.dp.api.annotate.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.annotate.DpAnnotationApiFactory;
import com.ospreydcs.dp.api.annotate.IAnnotationService;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.annotate.DpAnnotationConfig;
import com.ospreydcs.dp.api.query.DpQueryApiFactory;
import com.ospreydcs.dp.api.query.IQueryService;
import com.ospreydcs.dp.api.query.impl.DpQueryServiceImplTest;

/**
 * <p>
 * JUnit test cases for class <code>DpAnnotationServiceApiImpl</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 2, 2025
 *
 */
public class DpAnnotationServiceApiImplTest {

    
    //
    // Application Resources
    //
    
    /** The default Query Service configuration parameters */
    public static final DpAnnotationConfig      CFG_DEF = DpApiConfig.getInstance().annotation;
    
    
    //
    // Class Constants
    //
    
    /** General timeout limit */
    public static final long        LNG_TIMEOUT = CFG_DEF.timeout.limit;
    
    /** General timeout units */
    public static final TimeUnit    TU_TIMEOUT = CFG_DEF.timeout.unit;
    
    
    /** Location of performance results output */
    public static final String          STR_PATH_OUTPUT = "test/output/annotate/impl/";

    
    //
    // Test Fixture Resources
    //
    
    /** The common Annotation Service API used for query test cases */
    private static IAnnotationService       apiTest;
    
    /** Print output stream for storing evaluation results to single file */
    private static PrintStream              psOutput;
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        // Create the Annotation Service connection
        apiTest = DpAnnotationApiFactory.connect();
        
        // Open the common output file
        String  strFileName = DpQueryServiceImplTest.class.getSimpleName() + "-" + Instant.now().toString() + ".txt";
        Path    pathOutput = Paths.get(STR_PATH_OUTPUT, strFileName);
        File    fileOutput = pathOutput.toFile();
        
        psOutput = new PrintStream(fileOutput);
        
        // Write header
        DateFormat  date = DateFormat.getDateTimeInstance();
        String      strDateTime = date.format(new Date());
        psOutput.println(DpQueryServiceImplTest.class.getSimpleName() + ": " + strDateTime);
        psOutput.println();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        try {
            apiTest.shutdown();
            
        } catch (InterruptedException e) {
            String strMsg = "WARNING: Query Service API interrupted during shut down: " + e.getMessage();
            
            System.err.println(strMsg);
            psOutput.println();
            psOutput.println(strMsg);
        }
        
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
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#shutdown()}.
     */
    @Test
    public final void testShutdown() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#shutdownNow()}.
     */
    @Test
    public final void testShutdownNow() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#awaitTermination(long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testAwaitTerminationLongTimeUnit() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#isShutdown()}.
     */
    @Test
    public final void testIsShutdown() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#isTerminated()}.
     */
    @Test
    public final void testIsTerminated() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#from(com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnection)}.
     */
    @Test
    public final void testFrom() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#DpAnnotationServiceApiImpl(com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnection)}.
     */
    @Test
    public final void testDpAnnotationServiceApiImpl() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#awaitTermination()}.
     */
    @Test
    public final void testAwaitTermination() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#createDataset(com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest)}.
     */
    @Test
    public final void testCreateDataset() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#queryDatasets(com.ospreydcs.dp.api.annotate.DpDatasetsRequest)}.
     */
    @Test
    public final void testQueryDatasets() {
        fail("Not yet implemented"); // TODO
    }

}
