/*
 * Project: dp-api-common
 * File:	DpApiToolsConfigTest.java
 * Package: com.ospreydcs.dp.api.tools.config
 * Type: 	DpApiToolsConfigTest
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
 * @since May 7, 2025
 *
 */
package com.ospreydcs.dp.api.tools.config;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.tools.config.query.DpToolsQueryConfig;
import com.ospreydcs.dp.api.tools.config.request.DpRequestSuiteConfig;
import com.ospreydcs.dp.api.tools.config.request.DpTestRequestConfig;
import com.ospreydcs.dp.api.tools.query.request.TestArchiveRequest;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>DpApiToolsConfig</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 7, 2025
 *
 */
public class DpApiToolsConfigTest {

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
     * Test method for the <code>{@link TestArchiveRequest}</code> enumeration used in the query/requests
     * section of the configuration file.
     */
    @Test
    public final void testTestArchiveRequestEnumeration() {
        
//        // Parameters
//        final TestArchiveRequest      enmRqst1 = TestArchiveRequest.CLOCKED_1000PVSx1SEC;
//        final TestArchiveRequest      enmRqst2 = TestArchiveRequest.CLOCKED_1000PVSx5SEC;
//        final TestArchiveRequest      enmRqst3 = TestArchiveRequest.BOTH_100PVSx10SEC;
//        
//        final Collection<TestArchiveRequest>    setRqsts = new TreeSet<>();
//        
//        // Load collection
//        setRqsts.add(enmRqst1);
//        setRqsts.add(enmRqst2);
//        setRqsts.add(enmRqst3);
//
//        // Announce test method
//        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
//
//        // Create data requests from each enumeration constant
//        for (TestArchiveRequest enmRqst : setRqsts) {
//            DpDataRequest rqst = enmRqst.create();
//            
//            System.out.println("  Specific TestArchiveRequest " + enmRqst.name() + ":");
//            rqst.printOutProperties(System.out, "    ");
//            System.out.println();
//        }
        
        System.out.println("All TestArchiveRequest constants:");
        for (TestArchiveRequest enmRqst : TestArchiveRequest.values()) {
            System.out.println("  " + enmRqst);
        }
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.tools.config.DpApiToolsConfig#getInstance()}.
     */
    @Test
    public final void testGetInstance() {
        DpApiToolsConfig    cfgTest = DpApiToolsConfig.getInstance();
        
        boolean bolLogEnabled = cfgTest.query.logging.enabled;
        String  strLogLevel = cfgTest.query.logging.level;
        
        // Announce test method
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());

        System.out.println(DpApiToolsConfig.class.getSimpleName() + " Parameters:");
        System.out.println("  query.logging.enabled = " + bolLogEnabled);
        System.out.println("  query.logging.level = " + strLogLevel);
        System.out.println();
    }
    
    /**
     * Test method for inspection of <code>{@link DpTestRequestConfig}</code> class recovered by <code>DpApiToolsConfig</code>.
     */
    @Test
    public final void testGetQuery() {
        final DpTestRequestConfig    cfgRequests = DpApiToolsConfig.getInstance().query.testRequests;
        
        // Parameters
        final List<DpRequestSuiteConfig>    lstRqstSuites = cfgRequests.testSuites;
        
        // Announce test method
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());

        System.out.println("Request Test Suites:");
        int indSuite = 1;
        for (DpRequestSuiteConfig cfg : lstRqstSuites) {
            System.out.println("  Test Suite #" + indSuite);
            
            this.printOut(System.out, "    ", cfg);
            indSuite++;
        }
    }
    
    
    // 
    // Support Methods
    //
    
    /**
     * <p>
     * Prints out text description of the contents of the given structure class to the given output stream.
     * </p>
     * <p>
     * The <code>strPad</code> is assumed to be optional white space characters providing left-hand
     * side padding to the field headers.
     * </p>
     * 
     * @param ps        output stream to receive text description
     * @param strPad    optional left-hand side white space padding (or <code>null</code>)
     * @param cfg       the structure class containing configuration parameters
     */
    private void printOut(PrintStream ps, String strPad, DpRequestSuiteConfig cfg) {
        if (strPad == null)
            strPad = "";
        
        ps.println(strPad + "Name    :" + cfg.testSuite.name);
        
        ps.println(strPad + "Request IDs:");
        for (TestArchiveRequest enmRqst : cfg.testSuite.requestIds)
            ps.println(strPad + "- " + enmRqst.name());
        
        ps.println(strPad + "Request Decomposition Strategies:");
        for (RequestDecompType enmType : cfg.testSuite.requestComposites)
            ps.println(strPad + "- " + enmType.name());
        
        ps.println(strPad + "gRPC Stream Counts:");
        for (Integer intCnt : cfg.testSuite.streamCounts)
            ps.println(strPad + "- " + intCnt);
        
        ps.println(strPad + "gRPC Stream Types:");
        for (DpGrpcStreamType enmType : cfg.testSuite.streamTypes)
            ps.println(strPad + "- " + enmType.name());
    }

}
