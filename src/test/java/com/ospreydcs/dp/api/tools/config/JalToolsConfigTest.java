/*
 * Project: dp-api-common
 * File:	JalToolsConfigTest.java
 * Package: com.ospreydcs.dp.jal.tools.config
 * Type: 	JalToolsConfigTest
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
import java.time.Instant;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.DpSupportedType;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.archive.JalTestArchiveConfig;
import com.ospreydcs.dp.jal.tools.config.archive.JalTestArchivePvsConfig;
import com.ospreydcs.dp.jal.tools.query.request.TestArchiveRequest;

/**
 * <p>
 * JUnit test cases for class <code>JalToolsConfig</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 7, 2025
 *
 */
public class JalToolsConfigTest {

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
     * Test method for {@link com.ospreydcs.dp.jal.tools.config.JalToolsConfig#getInstance()}.
     */
    @Test
    public final void testGetInstance() {
        JalToolsConfig    cfgTest = JalToolsConfig.getInstance();
        
        String  strOutputPath = cfgTest.output.path;
        
        // Announce test method
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());

        System.out.println(JalToolsConfig.class.getSimpleName() + " Parameters:");
        System.out.println("  output.path = " + strOutputPath);
        System.out.println();
    }
    
    /**
     * Test method for inspection of <code>{@link JalTestArchiveConfig}</code> class recovered by <code>JalToolsConfig</code>.
     */
    @Test
    public final void testTestArchive() {
        final JalTestArchiveConfig    cfgArch = JalToolsConfig.getInstance().testArchive;
        
        // Parameters
        final Instant   insStart = Instant.parse(cfgArch.range.start);
        final Instant   insEnd = Instant.parse(cfgArch.range.end);
        
        
        // Announce test method
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());

        System.out.println("Archive inception : " + insStart);
        System.out.println("Last timestamp    : " + insEnd);

        this.printOut(System.out, "", cfgArch.pvs);
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
    private void printOut(PrintStream ps, String strPad, JalTestArchivePvsConfig cfg) {
        if (strPad == null)
            strPad = "";
        
        ps.println(strPad + "PV Prefix    :" + cfg.prefix);
        
        ps.println(strPad + "PV Data Types:");
        for (DpSupportedType enmType : cfg.types)
            ps.println(strPad + "  - " + enmType.name());
        
        ps.println(strPad + "PV Sampling Clock:");
        ps.println(strPad + "  period : " + cfg.clock.period);
        ps.println(strPad + "  units  : " + cfg.clock.units);
        
        ps.println(strPad + "PV Counts:");
        ps.println(strPad + "  total          : " + cfg.count.total);
        ps.println(strPad + "  clocked        : " + cfg.count.clocked);
        ps.println(strPad + "  timestamp list : " + cfg.count.tmsList);
        
        ps.println(strPad + "PV Indexes:");
        ps.println(strPad + "  clocked        : " + cfg.indexes.clocked);
        ps.println(strPad + "  timestamp list : " + cfg.indexes.tmsList);
    }

}
