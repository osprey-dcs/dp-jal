/*
 * Project: dp-api-common
 * File:	GrpcConnectionConfigTest.java
 * Package: com.ospreydcs.dp.api.config.grpc
 * Type: 	GrpcConnectionConfigTest
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
 * @since Dec 18, 2023
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config.grpc;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.model.CfgLoaderYaml;
import com.ospreydcs.dp.api.config.model.CfgOverrideRec;
import com.ospreydcs.dp.api.config.model.CfgOverrideUtility;

/**
 * <p>
 * Test Cases for <code>GrpcConnectionConfig</code> structure class.
 * </p>
 * <p>
 * Tests correct loading of YAML files using the CfgLoaderYaml utility class.
 * Also test the environment variable override feature.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Dec 18, 2023
 *
 */
public class GrpcConnectionConfigTest {
    
    //
    // Class Constants
    //
    
    /** YAML configuration file location */
//    public static final String      STR_CONFIG_FILENAME_1 = "src/test/resources/test-grpc-channel-1.yml";
    public static final String      STR_CONFIG_FILENAME_1 = "test-grpc-connection-1.yml";
    
    /** Comparison configuration records */
    public static GrpcConnectionConfig   CFG_1 = createConfig1();
    public static GrpcConnectionConfig   CFG_ENV = createConfigEnv();

    static {
    }
    
    
    //
    // Class Methods
    //
    
    /**
     * Creates and populates a <code>GrpcConnectionConfig</code> structure equivalent to the
     * first test configuration {@value #STR_CONFIG_FILENAME_1}.
     * 
     * @return new, populated test configuration
     */
    public static GrpcConnectionConfig    createConfig1() {
        
        GrpcConnectionConfig.Timeout timeout = new GrpcConnectionConfig.Timeout();
        timeout.active = true;
        timeout.limit = 5L;
        timeout.unit = TimeUnit.SECONDS;
        
        GrpcConnectionConfig.Channel.Host host = new GrpcConnectionConfig.Channel.Host();
        host.url = "localhost";
        host.port = 50051;
        
        GrpcConnectionConfig.Channel.TLS tls = new GrpcConnectionConfig.Channel.TLS();
        tls.active = true;
        tls.defaultTls = true;
        tls.filepaths = new GrpcConnectionConfig.Channel.TLS.FilePaths();
        tls.filepaths.trustedCerts = new String();
        tls.filepaths.clientCerts = new String();
        tls.filepaths.clientKey = new String();
        
        GrpcConnectionConfig.Channel.Grpc grpc = new GrpcConnectionConfig.Channel.Grpc();
        grpc.messageSizeMax = 4194304;
        grpc.timeoutLimit = 5L;
        grpc.timeoutUnit = TimeUnit.SECONDS;
        grpc.usePlainText = false;
        grpc.keepAliveWithoutCalls = false;
        grpc.gzip = false;
        
        GrpcConnectionConfig.Channel chan = new GrpcConnectionConfig.Channel();
        chan.host = host;
        chan.tls = tls;
        chan.grpc = grpc;

        GrpcConnectionConfig   cfg = new GrpcConnectionConfig();
        cfg.timeout = timeout;
        cfg.channel = chan;
        cfg.name = "test-grpc-channel-1";
        cfg.version = "0.0.0";
        cfg.description = "Testing parameter set for default channel configuration";
        cfg.supplement = "No supplemental parameters";
        
        return cfg;
    }
    
    /**
     * <p>
     * Creates and populates a <code>GrpcConnectionConfig</code> structure that is equivalent
     * to the first test case (i.e., {@value #STR_CONFIG_FILENAME_1} after being overridden by 
     * pre-assigned system variables.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * To make sense the system variables must be set in the JUnit environment, or java command line, or 
     * system properties, and they must be set to the values assigned in this method.
     * </p>
     * 
     * @return new test configuration which overrides the first configuration with different values
     */
    public static GrpcConnectionConfig    createConfigEnv() {
        
        GrpcConnectionConfig   cfg = createConfig1();
        
        cfg.timeout.limit = 60L;
        cfg.timeout.unit = TimeUnit.SECONDS;
        
        cfg.channel.tls.defaultTls = true;
        cfg.channel.tls.filepaths.trustedCerts = "trusted sources file";
        cfg.channel.tls.filepaths.clientCerts = "client certificates file";
        cfg.channel.tls.filepaths.clientKey = "private key file";
        
        cfg.channel.grpc.messageSizeMax = 16777216;
        cfg.channel.grpc.timeoutLimit = 100L;
        cfg.channel.grpc.timeoutUnit = TimeUnit.MILLISECONDS;
        cfg.channel.grpc.usePlainText = true;
        cfg.channel.grpc.keepAliveWithoutCalls = false;
        cfg.channel.grpc.gzip = true;
        
        return cfg;
    }
    
    
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
     * Test the test configurations of the test fixture.
     */
    @Test
    public final void testFixtureConfigurations() {
        GrpcConnectionConfig cfg1 = GrpcConnectionConfigTest.createConfig1();
        GrpcConnectionConfig cfg2 = GrpcConnectionConfigTest.createConfigEnv();
        
        
        try {
            for (Field fld : cfg1.getNullValuedFields()) 
                System.out.println("Cfg1 null valued field: name=" + fld.getName() + ", type=" + fld.getType().getSimpleName());
            
            for (Field fld : cfg2.getNullValuedFields()) 
                System.out.println("Cfg2 null valued field: name=" + fld.getName() + ", type=" + fld.getType().getSimpleName());
            
            Assert.assertTrue("There were null values in test fixuture configuration 1", cfg1.getNullValuedFields().isEmpty());
            Assert.assertTrue("There were null values in test fixuture configuration 2", cfg2.getNullValuedFields().isEmpty());
            
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Assert.fail("The CfgStructure.getNullValueFields() threw exception: " + e.getMessage());
            
        }
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.config.model.CfgLoaderYaml#load(String, Class)}.
     */
    @Test
    public void testLoadString1() {
        String      strFile = STR_CONFIG_FILENAME_1;
        
        try {
            GrpcConnectionConfig   cfgChan = CfgLoaderYaml.load(strFile, GrpcConnectionConfig.class);
            
            Assert.assertEquals("Configuration has at least one incorrect field", CFG_1, cfgChan);
            
            
        } catch (FileNotFoundException | SecurityException e) {
            fail("Exception thrown while loading " + strFile + ": " + e.getMessage());
            e.printStackTrace();
        }
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.config.model.CfgLoaderYaml#load(File, Class)}.
     */
    @Test
    public void testLoadFile1() {
        String      strFile = STR_CONFIG_FILENAME_1;
        URL         urlFile = this.getClass().getClassLoader().getResource(strFile);
        
        try {
            File        fileCfg = new File(urlFile.toURI());
            GrpcConnectionConfig   cfgChan = CfgLoaderYaml.load(fileCfg, GrpcConnectionConfig.class);
            
            Assert.assertEquals("Configuration has at least one incorrect field", CFG_1, cfgChan);
            
            
        } catch (FileNotFoundException | SecurityException e) {
            fail("Exception thrown while loading " + strFile + ": " + e.getMessage());
            e.printStackTrace();
            
        } catch (URISyntaxException e) {
            fail("URL failed to convert to URI - Testing error that should not occur.");
            e.printStackTrace();
        }
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.config.model.CfgOverrideUtility#overrideRoot(Object, com.ospreydcs.dp.api.config.model.CfgOverrideUtility.SOURCE)}.
     */
    @Test
    public void testOverrideConfig1() {
        String      strFile = STR_CONFIG_FILENAME_1;
        
        try {
            GrpcConnectionConfig   cfgChan = CfgLoaderYaml.load(strFile, GrpcConnectionConfig.class);

            boolean bolResult = CfgOverrideUtility.overrideRoot(cfgChan, CfgOverrideUtility.SOURCE.ENVIRONMENT);
            
            if (!bolResult)
                fail("No parameters were overridden.");
            
//            System.out.println(cfgChan.toString());
            Assert.assertEquals("Configuration has at least one incorrect field", CFG_ENV, cfgChan);
            
            
        } catch (FileNotFoundException | SecurityException e) {
            fail("Exception thrown while loading " + strFile + ": " + e.getMessage());
            e.printStackTrace();
            
        } catch (IllegalArgumentException e) {
            fail("There was and attempt to override with invalid argument in GrpcConnectionConfig.");
            e.printStackTrace();
            
        } catch (IllegalAccessException e) {
            fail("The GrpcConnectionConfig instance refused access to its fields.");
            e.printStackTrace();
        }
        
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.config.model.CfgOverrideUtility#extractOverrideables(Object, com.ospreydcs.dp.api.config.model.CfgOverrideUtility.SOURCE)}.
     */
    @Test
    public void testExtractOverrideablesEnv() {
        try {
            List<CfgOverrideRec>   lstVars = CfgOverrideUtility.extractOverrideables(CFG_1, CfgOverrideUtility.SOURCE.ENVIRONMENT);
            
            Assert.assertTrue("There were no environment variables", lstVars.size() > 0);

            System.out.println();
            System.out.println("Environment Variables:");
            for (CfgOverrideRec var : lstVars) 
                System.out.println(var);
            
        } catch (IllegalArgumentException e) {
            fail("IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            
        } catch (IllegalAccessException e) {
            fail("IllegalAccessException: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.config.grpc.GrpcConnectionConfig#toString()}.
     */
    @Test
    public void testGrpcConnnectionConfigToString() {
        String      strFile = STR_CONFIG_FILENAME_1;
        
        try {
            GrpcConnectionConfig   cfgChan = CfgLoaderYaml.load(strFile, GrpcConnectionConfig.class);
            
            System.out.println();
            System.out.println("GrpcConnectionConfig Parameters:");
            System.out.println("name = " + cfgChan.name);
            System.out.println("version = " + cfgChan.version);
            System.out.println("description = " + cfgChan.description);
            System.out.println("supplement = " + cfgChan.supplement);
            

            System.out.println("GrpcConnectionConfig#toString() for " + strFile);
            System.out.println(cfgChan.toString());
            System.out.println();
            
            CfgOverrideUtility.overrideRoot(cfgChan, CfgOverrideUtility.SOURCE.ENVIRONMENT);
            System.out.println("GrpcConnectionConfig#toString() after envionment override:");
            System.out.println(cfgChan);
            
            
        } catch (FileNotFoundException | SecurityException e) {
            fail("Exception thrown while loading " + strFile + ": " + e.getMessage());
            e.printStackTrace();
            
        } catch (IllegalArgumentException e) {
            fail("Unable to override configuration: " + e.getMessage());
            e.printStackTrace();
            
        } catch (IllegalAccessException e) {
            fail("Unable to override configuration: " + e.getMessage());
            e.printStackTrace();
        }
        
    }

    
    
}
