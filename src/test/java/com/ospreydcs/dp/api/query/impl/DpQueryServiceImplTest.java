/*
 * Project: dp-api-common
 * File:	DpQueryServiceImplTest.java
 * Package: com.ospreydcs.dp.api.query.impl
 * Type: 	DpQueryServiceImplTest
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
 * @since Feb 19, 2025
 *
 */
package com.ospreydcs.dp.api.query.impl;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.IDataTable;
import com.ospreydcs.dp.api.common.MetadataRecord;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.api.model.table.StaticDataTable;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpMetadataRequest;
import com.ospreydcs.dp.api.query.DpQueryApiFactory;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.DpQueryStreamBuffer;
import com.ospreydcs.dp.api.query.IQueryService;
import com.ospreydcs.dp.api.query.model.request.DataRequestDecomposer;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.JavaSize;

/**
 * <p>
 * JUnit test cases for class <code>DpQueryServiceImpl</code>.
 * </p>
 * <p>
 * <h2>WARNINGS:</h2>
 * For test cases to pass the following conditions must be current:
 * <ul>
 * <li>
 * A Query Service must be running at the default port location and with default parameter set.
 * </li>
 * <li>
 * The Data Platform archive must be populated with test data from the <em>app-run-test-data-generator</em>
 * application within the Data Platform installer.
 * </li>
 * </ul>
 * Due to the large size of some test requests (RQST_HALF_SRC, RQST_HALF_RNG) heap space issues may arise,
 * especially with static data tables.  The following are Java command line options to manage heap space
 * and garbage collection:
 * <ul>
 * <li>-XX:GCTimeRatio=19 - sets the garbage collection runtime to 1/20 of the application runtime.</li> 
 * <li>-XX:MaxHeapFreeRatio=70 - sets the maximum ratio (%) of heap space to live object before garbage collection.</li>
 * <li>-XX:MinHeapFreeRatio=40 - sets the minimum ratio (%) of heap space to live object before garbage collection.</li>
 * <li>-Xmx4096m - sets the maximum heap space size to 4 GBytes.</li>
 * <li>-Xms4096m - sets the minimum heap space size to 4 GBytes.</li>
 * </ul>
 * <p>
 *
 * @author Christopher K. Allen
 * @since Feb 19, 2025
 *
 */
public class DpQueryServiceImplTest {

    //
    // Class Types
    //
    
    /**
     * <p>
     * Record containing configuration parameters for a <code>DpQueryServiceImpl</code> object.
     * </p>
     *
     * @param name              text name for configuration
     * @param bolStaticDef      use static data tables as the default
     * @param bolStaticHasMax   static data tables have a maximum size
     * @param szStaticMax       maximum size (in bytes) of static data tables
     * @param bolDynEnable      enable dynamic data tables
     */
    public static record Config(String name, boolean bolStaticDef, boolean bolStaticHasMax, int szStaticMax, boolean bolDynEnable) {
        
        /**
         * <p>
         * Creates a new <code>Config</code> record initialized with the given arguments.
         * </p>
         * 
         * @param name              text name for configuration
         * @param bolStaticDef      use static data tables as the default
         * @param bolStaticHasMax   static data tables have a maximum size
         * @param szStaticMax       maximum size (in bytes) of static data tables
         * @param bolDynEnable      enable dynamic data tables
         * 
         * @return  a new <code>Config</code> record with the given parameters
         */
        public static Config from(String name, boolean bolStaticDef, boolean bolStaticHasMax, int szStaticMax, boolean bolDynEnable) {
            return new Config(name, bolStaticDef, bolStaticHasMax, szStaticMax, bolDynEnable);
        }
        
        /**
         * <p>
         * Configures the given <code>DpQueryServiceImpl</code> object according to the record configuration parameters.
         * </p>
         * 
         * @param api   API implementation to be configured
         */
        public void configure(DpQueryServiceImpl api) {
            api.setStaticTableDefault(this.bolStaticDef);
            if (this.bolStaticHasMax)
                api.enableStaticTableMaxSize(this.szStaticMax);
            else
                api.disableStaticTableMaxSize();
            api.enableDynamicTables(this.bolDynEnable);
        }
    }
    
    //
    // Application Resources
    //
    
    /** The default Query Service configuration parameters */
    public static final DpQueryConfig          CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** General timeout limit */
    public static final long        LNG_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** General timeout units */
    public static final TimeUnit    TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
    /** Location of performance results output */
    public static final String          STR_PATH_OUTPUT = "test/output/query/impl/";
    
    
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

    
    /** Common List of all PV names in the test archive */
    public static final List<String>                LST_PV_NAMES = RQST_HALF_SRC.getSourceNames();

    
    /** Common List of data requests used for testing */
    public static final List<DpDataRequest>         LST_RQSTS = List.of(RQST_SMALL, 
                                                                        RQST_WIDE, 
                                                                        RQST_BIG, 
                                                                        RQST_HALF_SRC, 
                                                                        RQST_HALF_RNG);
    
    /** A map of the common test requests to their (arbitrary) names */
    public static final Map<DpDataRequest, String>  MAP_RQST_NMS = Map.of(RQST_SMALL, "RQST_SMALL", 
                                                                          RQST_WIDE, "RQST_WIDE",
                                                                          RQST_BIG, "RQST_BIG",
                                                                          RQST_HALF_SRC, "RQST_HALF_SRC", 
                                                                          RQST_HALF_RNG, "RQST_HALF_RNG");
    
    public static final Config  CFG_ALL_STATIC = Config.from("All Static Tables", true, false, 0, false);  // use all static tables
    public static final Config  CFG_STATIC_MAX = Config.from("Static Tables with 4 Mbyte Max", true, true, 4000000, true);
    public static final Config  CFG_ALL_DYN = Config.from("All Dynamic Tables", false, false, 0, true);
    
    /** Common List of <code>DpQueryServiceImpl</code> configuration parameters settings - populated in static block */
    public static final List<Config>                LST_API_CFGS = List.of(CFG_ALL_STATIC, CFG_STATIC_MAX, CFG_ALL_DYN);
    
    
//    static {
//        
//        // Create the list of Query Service API configurations
//        Config  cfg1 = Config.from("All Static Tables", true, false, 0, false);  // use all static tables
//        Config  cfg2 = Config.from("Static Tables with 4 Mbyte Max", true, true, 4000000, true);
//        Config  cfg3 = Config.from("All Dynamic Tables", false, false, 0, true);
//
//        LST_API_CFGS = List.of(cfg1, cfg2, cfg3);
//    }
    
    //
    // Test Fixture Resources
    //
    
    /** The common Query Service API used for query test cases */
    private static IQueryService            apiTest;
    
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
        
        // Create the Query Service connection
        apiTest = DpQueryApiFactory.connect();
        
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
    public static void tearDownAfterClass() {
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
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#shutdown()}.
     */
    @Test
    public final void testShutdown() {
        try {
            IQueryService   apiQuery = DpQueryApiFactory.connect();
            
            Assert.assertFalse(apiQuery.isShutdown());
            
            boolean bolShutdown = apiQuery.shutdown();
            boolean bolIsShutdown = apiQuery.isShutdown();
            
            Assert.assertTrue(bolShutdown);
            Assert.assertTrue(bolIsShutdown);
            
        } catch (DpGrpcException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Unable to create interface: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during shut down: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#shutdownNow()}.
     */
    @Test
    public final void testShutdownNow() {
        try {
            IQueryService   apiQuery = DpQueryApiFactory.connect();
            
            Assert.assertFalse(apiQuery.isShutdown());
            
            boolean bolShutdown = apiQuery.shutdownNow();
            boolean bolIsShutdown = apiQuery.isShutdown();
            boolean bolIsTerminated = apiQuery.isTerminated();
            
            Assert.assertTrue(bolShutdown);
            Assert.assertTrue(bolIsShutdown);
            Assert.assertTrue(bolIsTerminated);
            
            apiQuery.awaitTermination(LNG_TIMEOUT, TU_TIMEOUT);
            
            Assert.assertTrue(apiQuery.isShutdown());
            Assert.assertTrue(apiQuery.isTerminated());
            
        } catch (DpGrpcException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Unable to create interface: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during hard shut down: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#awaitTermination(long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testAwaitTerminationLongTimeUnit() {
        try {
            IQueryService   apiQuery = DpQueryApiFactory.connect();
            
            Assert.assertFalse(apiQuery.isShutdown());
            
            boolean bolShutdown = apiQuery.shutdown();
            boolean bolIsTerminated = apiQuery.isTerminated();
            boolean bolIsShutdown = apiQuery.isShutdown();
            
            Assert.assertTrue(bolShutdown);
            Assert.assertTrue(bolIsShutdown);
//            Assert.assertFalse(bolIsTerminated);
            
            apiQuery.awaitTermination(LNG_TIMEOUT, TU_TIMEOUT);
            
            Assert.assertTrue(apiQuery.isTerminated());
            
            psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
            psOutput.println("  isTerminated() before wait = " + bolIsTerminated);
            psOutput.println("  isTerminated() after wait  = " + apiQuery.isTerminated());
            
        } catch (DpGrpcException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Unable to create interface: " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during shut down: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#from(com.ospreydcs.dp.api.grpc.query.DpQueryConnection)}.
     */
    @Test
    public final void testFrom() {
        DpQueryConnection connQuery;
        try {
            connQuery = DpQueryConnectionFactory.FACTORY.connect();
            
        } catch (DpGrpcException e) {
            Assert.fail("Unable to obtain Query Service connection, cannot continue: " + e.getMessage());
            return;
        }
        
        DpQueryServiceImpl apiQuery = DpQueryServiceImpl.from(connQuery);
        try {
            apiQuery.shutdown();
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during shut down: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#DpQueryServiceImpl(com.ospreydcs.dp.api.grpc.query.DpQueryConnection)}.
     */
    @Test
    public final void testDpQueryServiceImpl() {
        
        try {
            DpQueryConnection   connQuery = DpQueryConnectionFactory.FACTORY.connect();
            
            DpQueryServiceImpl  apiQuery = new DpQueryServiceImpl(connQuery);
            
            apiQuery.shutdown();
            
        } catch (DpGrpcException e) {
            Assert.fail("Unable to obtain Query Service connection, cannot continue: " + e.getMessage());
            return;
            
        } catch (InterruptedException e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " - Interrupted during shut down: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryMeta(com.ospreydcs.dp.api.query.DpMetadataRequest)}.
     */
    @Test
    public final void testQueryMeta() {
        
        // Test Parameters
        final int           cntPvs = 10;
        final List<String>  strPvNames = LST_PV_NAMES.subList(0, cntPvs);
        
        DpMetadataRequest rqst = DpMetadataRequest.newRequest();
        rqst.selectPvs(strPvNames);
        
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        try {
            List<MetadataRecord>    lstRcrds = apiTest.queryMeta(rqst);
            
            Assert.assertEquals(cntPvs, lstRcrds.size());
            
            for (MetadataRecord rcrd : lstRcrds) 
                psOutput.println("  " + rcrd.toString());
            psOutput.println();
            
        } catch (DpQueryException e) {
            String  strMsg = "Metadata request exception " + e.getClass().getSimpleName() + ": " + e.getMessage();
            
            psOutput.println(strMsg);
            psOutput.println();
            Assert.fail(strMsg);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryDataUnary(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testQueryDataUnary() {
        
        // Test Parameters
        final DpDataRequest rqst = RQST_SMALL;
        
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        try {
            Instant     insStart = Instant.now();
            IDataTable  table = apiTest.queryDataUnary(rqst);
            Instant     insFinish = Instant.now();
            Duration    durRqst = Duration.between(insStart, insFinish);

            this.printResults(psOutput, rqst, table, durRqst);
            
            Assert.assertEquals(rqst.getSourceCount(), (int)table.getColumnCount());
            Assert.assertEquals(rqst.rangeDuration(), table.getDuration());
            
        } catch (DpQueryException e) {
            String  strMsg = "Unary time-series request exception " + e.getClass().getSimpleName() + ": " + e.getMessage();
            
            psOutput.println(strMsg);
            psOutput.println();
            Assert.fail(strMsg);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryData(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testQueryDataDpDataRequest() {
        
        // Test Parameters
        final DpDataRequest rqst = RQST_HALF_SRC;
        
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        try {
            Instant     insStart = Instant.now();
            IDataTable  table = apiTest.queryData(rqst);
            Instant     insFinish = Instant.now();
            Duration    durRqst = Duration.between(insStart, insFinish);

            this.printResults(psOutput, rqst, table, durRqst);
            
            Assert.assertEquals(rqst.getSourceCount(), (int)table.getColumnCount());
            Assert.assertEquals(rqst.rangeDuration(), table.getDuration());
            
        } catch (DpQueryException e) {
            String  strMsg = "Streaming time-series request exception " + e.getClass().getSimpleName() + ": " + e.getMessage();
            
            psOutput.println(strMsg);
            psOutput.println();
            Assert.fail(strMsg);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryData(java.util.List)}.
     */
    @Test
    public final void testQueryDataListOfDpDataRequest() {

        // Test Resources
        DataRequestDecomposer   prcrDcmp = DataRequestDecomposer.create();
        
        // Test Parameters
        final int                   cntRqsts = 4;
        final RequestDecompType     enmDcmpType = RequestDecompType.HORIZONTAL;
        final DpDataRequest         rqst = RQST_HALF_SRC;
        final List<DpDataRequest>   lstRqsts = prcrDcmp.buildCompositeRequest(rqst, enmDcmpType, cntRqsts);
        
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        try {
            Instant     insStart = Instant.now();
            IDataTable  table = apiTest.queryData(lstRqsts);
            Instant     insFinish = Instant.now();
            
            Duration    durRqst = Duration.between(insStart, insFinish);
            
            this.printResults(psOutput, rqst, table, durRqst);
            
            Assert.assertEquals(rqst.getSourceCount(), (int)table.getColumnCount());
            Assert.assertEquals(rqst.rangeDuration(), table.getDuration());
            
        } catch (DpQueryException e) {
            String  strMsg = "Streaming time-series request exception " + e.getClass().getSimpleName() + ": " + e.getMessage();
            
            psOutput.println(strMsg);
            psOutput.println();
            Assert.fail(strMsg);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryDataStream(com.ospreydcs.dp.api.query.DpDataRequest)}.
     */
    @Test
    public final void testQueryDataStream() {
        // Test Parameters
        final DpDataRequest rqst = RQST_HALF_SRC;
        
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        
        DpQueryStreamBuffer bufData = apiTest.queryDataStream(rqst);
        
        try {
            Instant insStart = Instant.now();
            bufData.startAndAwaitCompletion();
            Instant insFinish = Instant.now();
            
            Duration    durRqst = Duration.between(insStart, insFinish);
            int         szData = bufData.getBuffer().stream().mapToInt(msg -> msg.getSerializedSize()).sum();
            long        szRqst = rqst.approxDomainSize() * 1_000L * JavaSize.SZ_Double;
            double      dblRate = ((double)szData*1_000L)/((double)durRqst.toNanos());
            
            psOutput.println("  Request:");
            psOutput.println("    Data sources          : " + rqst.getSourceCount());
            psOutput.println("    Time duration         : " + rqst.rangeDuration());
            psOutput.println("    Approx. size (Mbytes) : " + szRqst/1_000_000L);
            psOutput.println("  Result Buffer:");
            psOutput.println("    Buffer type           : " + bufData.getClass().getSimpleName());
            psOutput.println("    Allocation (Mbytes)   : " + szData/1_000_000L);
            psOutput.println("  Request Operation:");
            psOutput.println("    Duration              : " + durRqst);
            psOutput.println("    Data rate (Mbps)      : " + dblRate);
            psOutput.println();
            
        } catch (IllegalStateException | IllegalArgumentException | InterruptedException e) {
            String  strMsg = "Streaming time-series request exception " + e.getClass().getSimpleName() + ": " + e.getMessage();
            
            psOutput.println(strMsg);
            psOutput.println();
            Assert.fail(strMsg);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.impl.DpQueryServiceImpl#queryData(com.ospreydcs.dp.api.query.DpDataRequest)}.
     * <p>
     * Uses multiple configuration of <code>DpQueryServiceImpl</code>
     */
    @Test
    public final void testQueryDataConfigurations() {
        
        // Test Parameters
        final DpQueryServiceImpl            apiImpl = DpQueryServiceImpl.class.cast(apiTest);       
        final List<Config>                  lstCfgs = LST_API_CFGS;
        final List<DpDataRequest>           lstRqsts = LST_RQSTS;
        
        psOutput.println(JavaRuntime.getQualifiedMethodNameSimple());
        psOutput.println();
        for (Config cfg : lstCfgs) {
            cfg.configure(apiImpl);
            System.out.println("Running Configuration=" + cfg.name());
            
            psOutput.println("-- Configuration: " + cfg.name() + " ----");
            psOutput.println();
            for (DpDataRequest rqst : lstRqsts) 
                try {
                    System.out.println("  Running Request=" + MAP_RQST_NMS.get(rqst));
                    if (cfg.equals(CFG_ALL_STATIC) && (rqst.equals(RQST_HALF_RNG) || rqst.equals(RQST_HALF_SRC))) {
                        String  strMsg = "  Skipping query for request " + MAP_RQST_NMS.get(rqst) + " in configuration " + cfg.name() + ".";
                        
                        System.out.println(strMsg);
                        psOutput.println(strMsg);
                        psOutput.println();
                        continue;
                    }
                    
                    Instant insStart = Instant.now();
                    IDataTable  table = apiImpl.queryData(rqst);
                    Instant insFinish = Instant.now();
                    
                    Duration    durRqst = Duration.between(insStart, insFinish);

                    this.printResults(psOutput, rqst, table, durRqst);
                    
                    // Garbage collect the table to prevent heap errors
                    table.clear();
                    System.gc();
                    Runtime.getRuntime().gc();
                    
                } catch (DpQueryException e) {
                    String  strMsg = "  ERROR Request: " + MAP_RQST_NMS.get(rqst) + "\n"
                            + "    Configured streaming time-series request exception " + e.getClass().getSimpleName() + "\n" 
//                            + "    Configuration = " + cfg.name() + "\n"  
                            + "    Request       = " + MAP_RQST_NMS.get(rqst) + "\n"
                            + "    Message       = " + e.getMessage() + "\n"
                            + "    Cause type    = " + e.getCause().getClass().getSimpleName() + "\n"
                            + "    Cause message = " + e.getCause().getMessage() + "\n";
                    
                    psOutput.println(strMsg);
//                    psOutput.println();
                    
                    apiImpl.setDefaultConfiguration();
//                    Assert.fail(strMsg);
                }
            
        }
        
        apiImpl.setDefaultConfiguration();
    }
    
    //
    // Support Functions
    //
    
    /**
     * <p>
     * Prints out text description of the given time-series data results properties to the given print stream.
     * </p>
     * 
     * @param ps        print stream to receive text output
     * @param rqst      the original time-series data request
     * @param table     the request results
     * @param durRqst   duration of the request
     */
    private void printResults(PrintStream ps, DpDataRequest rqst, IDataTable table, Duration durRqst) {
        
        long        szRqst = rqst.approxDomainSize() * 1_000L * JavaSize.SZ_Double;
        long        szTbl = table.allocationSize();
        double      dblRate = ((double)szTbl*1_000L)/((double)durRqst.toNanos());
        
        String  strTblType;
        if (table instanceof StaticDataTable)
            strTblType = "Static";
        else
            strTblType = "Dynamic";
        
        ps.println("  Request: " + MAP_RQST_NMS.get(rqst));
        ps.println("    Data source count       : " + rqst.getSourceCount());
        ps.println("    Time range duration     : " + rqst.rangeDuration());
        ps.println("    Estimated size (Mbytes) : " + szRqst/1_000_000L);
        ps.println("  Result Table:");
        ps.println("    Table type          : " + strTblType);
        ps.println("    Column count        : " + table.getColumnCount());
        ps.println("    Time range duration : " + table.getDuration());
        ps.println("    Allocation (Mbytes) : " + szTbl/1_000_000L);
        ps.println("  Request Operation:");
        ps.println("    Duration         : " + durRqst);
        ps.println("    Data rate (Mbps) : " + dblRate);
        ps.println();
    }
}
