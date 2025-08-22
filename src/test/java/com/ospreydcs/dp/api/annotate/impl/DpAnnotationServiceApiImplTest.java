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

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.annotate.DpAnnotationApiFactory;
import com.ospreydcs.dp.api.annotate.DpAnnotationException;
import com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest;
import com.ospreydcs.dp.api.annotate.DpCreateDatasetRequestTest;
import com.ospreydcs.dp.api.annotate.DpDatasetsRequest;
import com.ospreydcs.dp.api.annotate.IAnnotationService;
import com.ospreydcs.dp.api.common.DatasetUID;
import com.ospreydcs.dp.api.common.DpDataset;
import com.ospreydcs.dp.api.common.OwnerUID;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.annotate.DpAnnotationConfig;
import com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnection;
import com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnectionFactory;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.test.TestDpDataRequestGenerator;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.util.JavaRuntime;

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

    
    /** The start instant of the Data Platform test archive */
    public static final Instant         INS_INCEPT = TestDpDataRequestGenerator.INS_INCEPT;
    
    /** The final time instant of the Data Platform test archive */
    public static final Instant         INS_FINAL = TestDpDataRequestGenerator.INS_FINAL;
    
    /** The list of all PV names within the Data Platform test archive */
    public static final List<String>    LST_PV_NMS = TestDpDataRequestGenerator.LST_PV_NAMES;
    
    
    /** "Small" test request  */
    public static final DpDataRequest   RQST_SMALL = TestQueryResponses.QREC_LONG.createRequest();
    
    /** Wide test request (500 PVs, 5 secs) */
    public static final DpDataRequest   RQST_WIDE = TestQueryResponses.QREC_WIDE.createRequest();
    
    /** Large test request */
    public static final DpDataRequest   RQST_BIG = TestQueryResponses.QREC_BIG.createRequest();
    
    /** Huge test request */
    public static final DpDataRequest   RQST_HUGE = TestQueryResponses.QREC_HUGE.createRequest();
    
    /** The data request for the half the test archive - used for timing responses */
    public static final DpDataRequest   RQST_HALF_SRC = TestQueryResponses.QREC_HALF_SRC.createRequest();

    /** The data request for the half the test archive - used for timing responses */
    public static final DpDataRequest   RQST_HALF_RNG = TestQueryResponses.QREC_HALF_RNG.createRequest();

    /** The data request for the entire test archive - used for timing responses */
    public static final DpDataRequest   RQST_ALL = TestQueryResponses.QREC_ALL.createRequest();

    
    //
    // Class Resources
    //
    
    /** Class-based owner UID used for data set creation */
    public static final OwnerUID        REC_OWNER1 = OwnerUID.from(DpCreateDatasetRequestTest.class.getSimpleName() + "_1");
    
    /** Class-based owner UID used for data set creation */
    public static final OwnerUID        REC_OWNER2 = OwnerUID.from(DpCreateDatasetRequestTest.class.getSimpleName() + "_2");
    
    
    /** Random number generator (used for data set names) */
    public static final Random          RND_NUMBERS = Random.from(RandomGenerator.getDefault());

    
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
        String  strFileName = DpAnnotationServiceApiImplTest.class.getSimpleName() + "-" + Instant.now().toString() + ".txt";
        Path    pathOutput = Paths.get(STR_PATH_OUTPUT, strFileName);
        File    fileOutput = pathOutput.toFile();
        
        psOutput = new PrintStream(fileOutput);
        
        // Write header
        DateFormat  date = DateFormat.getDateTimeInstance();
        String      strDateTime = date.format(new Date());
        psOutput.println(DpAnnotationServiceApiImplTest.class.getSimpleName() + ": " + strDateTime);
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
        try {
            IAnnotationService   apiTest = DpAnnotationApiFactory.connect();
            
            Assert.assertFalse(apiTest.isShutdown());
            
            boolean bolShutdown = apiTest.shutdown();
            boolean bolIsShutdown = apiTest.isShutdown();
            
            Assert.assertTrue(bolShutdown);
            Assert.assertTrue(bolIsShutdown);
            
        } catch (DpGrpcException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Unable to create interface: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during shut down: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#shutdownNow()}.
     */
    @Test
    public final void testShutdownNow() {
        try {
            IAnnotationService   apiTest = DpAnnotationApiFactory.connect();
            
            Assert.assertFalse(apiTest.isShutdown());
            
            boolean bolShutdown = apiTest.shutdownNow();
            boolean bolIsShutdown = apiTest.isShutdown();
            boolean bolIsTerminated = apiTest.isTerminated();
            
            Assert.assertTrue(bolShutdown);
            Assert.assertTrue(bolIsShutdown);
            Assert.assertTrue(bolIsTerminated);
            
            apiTest.awaitTermination(LNG_TIMEOUT, TU_TIMEOUT);
            
            Assert.assertTrue(apiTest.isShutdown());
            Assert.assertTrue(apiTest.isTerminated());
            
        } catch (DpGrpcException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Unable to create interface: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during hard shut down: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#awaitTermination(long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testAwaitTerminationLongTimeUnit() {
        try {
            IAnnotationService   apiTest = DpAnnotationApiFactory.connect();
            
            Assert.assertFalse(apiTest.isShutdown());
            
            boolean bolShutdown = apiTest.shutdown();
            boolean bolIsTerminated = apiTest.isTerminated();
            boolean bolIsShutdown = apiTest.isShutdown();
            
            Assert.assertTrue(bolShutdown);
            Assert.assertTrue(bolIsShutdown);
//            Assert.assertFalse(bolIsTerminated);
            
            apiTest.awaitTermination(LNG_TIMEOUT, TU_TIMEOUT);
            
            Assert.assertTrue(apiTest.isTerminated());
            
            psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
            psOutput.println("  isTerminated() before wait = " + bolIsTerminated);
            psOutput.println("  isTerminated() after wait  = " + apiTest.isTerminated());
            
        } catch (DpGrpcException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Unable to create interface: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during shut down: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#from(com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnection)}.
     */
    @Test
    public final void testFrom() {
        DpAnnotationConnection connAnnot;
        try {
            connAnnot = DpAnnotationConnectionFactory.FACTORY.connect();
            
        } catch (DpGrpcException e) {
            Assert.fail("Unable to obtain Query Service connection, cannot continue: " + e.getMessage());
            return;
        }
        
        DpAnnotationServiceApiImpl apiQuery = DpAnnotationServiceApiImpl.from(connAnnot);
        try {
            apiQuery.shutdown();
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during shut down: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#DpAnnotationServiceApiImpl(com.ospreydcs.dp.api.grpc.annotate.DpAnnotationConnection)}.
     */
    @Test
    public final void testDpAnnotationServiceApiImpl() {
        
        try {
            DpAnnotationConnection   connAnnot = DpAnnotationConnectionFactory.FACTORY.connect();
            
            DpAnnotationServiceApiImpl  apiQuery = new DpAnnotationServiceApiImpl(connAnnot);
            
            apiQuery.shutdown();
            
        } catch (DpGrpcException e) {
            Assert.fail("Unable to obtain Query Service connection, cannot continue: " + e.getMessage());
            return;
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during shut down: " + e.getMessage());
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#awaitTermination()}.
//     */
//    @Test
//    public final void testAwaitTermination() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#createDataset(com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest)}.
     */
    @Test
    public final void testCreateDataset1() {
        
        // Parameters
        final OwnerUID  recOwner = REC_OWNER1;
        final String    strName = DpAnnotationServiceApiImplTest.class.getSimpleName() + "-" + RND_NUMBERS.nextInt();
        final String    strDescr = DpAnnotationServiceApiImplTest.class.getName();
        
        // Create the data set creation request
        DpCreateDatasetRequest rqstDset = DpCreateDatasetRequest.from(recOwner, strName, strDescr);

        // New data set parameters
        final DpDataRequest     rqstData = RQST_SMALL;
        final TimeInterval      tvlRng = rqstData.range();
        final List<String>      lstPvNms = rqstData.getSourceNames();

        rqstDset.addDomain(lstPvNms, tvlRng);
        
        // Perform data set creation request and verify
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        try {
            DatasetUID recUid = apiTest.createDataset(rqstDset);

            psOutput.println("  Data set UID = " + recUid);
            psOutput.println();
            
        } catch (DpAnnotationException e) {
            String strMsg = "Data set creation exception " + e.getClass().getSimpleName() + ": " + e.getMessage();

            psOutput.println(strMsg);
            psOutput.println();
            Assert.fail(strMsg);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#createDataset(com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest)}.
     */
    @Test
    public final void testCreateDataset2() {
        
        // Parameters
        final OwnerUID  recOwner = REC_OWNER2;
        final String    strName = DpAnnotationServiceApiImplTest.class.getSimpleName() + "-" + RND_NUMBERS.nextInt();;
        final String    strDescr = DpAnnotationServiceApiImplTest.class.getName();
        
        // Create the data set creation request
        DpCreateDatasetRequest rqstDset = DpCreateDatasetRequest.from(recOwner, strName, strDescr);

        // New data set parameters
        final DpDataRequest     rqstData = RQST_WIDE;
        final TimeInterval      tvlRng = rqstData.range();
        final List<String>      lstPvNms = rqstData.getSourceNames();

        rqstDset.addDomain(lstPvNms, tvlRng);
        
        // Perform data set creation request and verify
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        try {
            DatasetUID recUid = apiTest.createDataset(rqstDset);

            psOutput.println("  Data set UID = " + recUid);
            psOutput.println();
            
        } catch (DpAnnotationException e) {
            String strMsg = "Data set creation exception " + e.getClass().getSimpleName() + ": " + e.getMessage();

            psOutput.println(strMsg);
            psOutput.println();
            Assert.fail(strMsg);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#queryDatasets(com.ospreydcs.dp.api.annotate.DpDatasetsRequest)}.
     */
    @Test
    public final void testQueryDatasetsOwner1() {
        
        // Parameters
        final OwnerUID      recOwner = REC_OWNER1;
        
        // Create data sets request and add criteria
        DpDatasetsRequest   rqst = apiTest.newDatasetsRequest();
        rqst.addCriteriaOwner(recOwner);
        
        // Perform request and verify
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        try {
            List<DpDataset> lstDsets = apiTest.queryDatasets(rqst);
            
            Assert.assertTrue("No datasets recovered.", lstDsets.size()>0);
            
            psOutput.println("  Datasets for owner: " + recOwner);
            for (DpDataset dset : lstDsets)
                psOutput.println("    " + dset);
            psOutput.println();
            
        } catch (DpAnnotationException e) {
            String strMsg = "Data sets query exception " + e.getClass().getSimpleName() + ": " + e.getMessage();

            psOutput.println(strMsg);
            psOutput.println();
            Assert.fail(strMsg);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#queryDatasets(com.ospreydcs.dp.api.annotate.DpDatasetsRequest)}.
     */
    @Test
    public final void testQueryDatasetsOwner2() {
        
        // Parameters
        final OwnerUID      recOwner = REC_OWNER2;
        
        // Create data sets request and add criteria
        DpDatasetsRequest   rqst = apiTest.newDatasetsRequest();
        rqst.addCriteriaOwner(recOwner);
        
        // Perform request and verify
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        try {
            List<DpDataset> lstDsets = apiTest.queryDatasets(rqst);
            
            Assert.assertTrue("No datasets recovered.", lstDsets.size()>0);
            
            psOutput.println("  Datasets for owner: " + recOwner);
            for (DpDataset dset : lstDsets)
                psOutput.println("    " + dset);
            psOutput.println();
            
        } catch (DpAnnotationException e) {
            String strMsg = "Data sets query exception " + e.getClass().getSimpleName() + ": " + e.getMessage();

            psOutput.println(strMsg);
            psOutput.println();
            Assert.fail(strMsg);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.impl.DpAnnotationServiceApiImpl#queryDatasets(com.ospreydcs.dp.api.annotate.DpDatasetsRequest)}.
     */
    @Test
    public final void testQueryDatasetsName1() {
        
        // Parameters
        final String        strName = DpAnnotationServiceApiImplTest.class.getSimpleName();
        
        // Create data sets request and add criteria
        DpDatasetsRequest   rqst = apiTest.newDatasetsRequest();
        rqst.addCriterionName(strName);
        
        // Perform request and verify
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        try {
            List<DpDataset> lstDsets = apiTest.queryDatasets(rqst);
            
            Assert.assertTrue("No datasets recovered.", lstDsets.size()>0);
            
            psOutput.println("  Datasets for name: " + strName);
            for (DpDataset dset : lstDsets)
                psOutput.println("    " + dset);
            psOutput.println();
            
        } catch (DpAnnotationException e) {
            String strMsg = "Data sets query exception " + e.getClass().getSimpleName() + ": " + e.getMessage();

            psOutput.println(strMsg);
            psOutput.println();
            Assert.fail(strMsg);
        }
    }

}
