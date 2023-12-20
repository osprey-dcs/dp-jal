/*
 * Project: dp-api-common
 * File:	GrpcChannelConfigTest.java
 * Package: com.ospreydcs.dp.api.config.grpc
 * Type: 	GrpcChannelConfigTest
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

import com.ospreydcs.dp.api.config.EnvOverrideUtility;
import com.ospreydcs.dp.api.config.EnvVariable;
import com.ospreydcs.dp.api.config.YamlLoader;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * <p>
 * Test Cases for <code>GrpcChannelConfi</code> structure class.
 * </p>
 * <p>
 * Tests correct loading of YAML files using the YamlLoader utility class.
 * Also test the environment variable override feature.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Dec 18, 2023
 *
 */
public class GrpcChannelConfigTest {
    
    //
    // Class Constants
    //
    
    /** YAML configuration file location */
//    public static final String      STR_CONFIG_FILENAME_1 = "src/test/resources/test-grpc-channel-1.yml";
    public static final String      STR_CONFIG_FILENAME_1 = "test-grpc-channel-1.yml";
    
    /** Comparison configuration records */
    public static GrpcChannelConfig   CFG_1 = createConfig1();
    public static GrpcChannelConfig   CFG_ENV = createConfigEnv();

    static {
    }
    
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
     * Test method for {@link com.ospreydcs.dp.api.config.YamlLoader#load(String, Class)}.
     */
    @Test
    public void testLoadString1() {
        String      strFile = STR_CONFIG_FILENAME_1;
        
        try {
            GrpcChannelConfig   cfgChan = YamlLoader.load(strFile, GrpcChannelConfig.class);
            
//            System.out.println("name = " + cfgChan.name);
//            System.out.println("version = " + cfgChan.version);
//            System.out.println("description = " + cfgChan.description);
//            System.out.println("supplement = " + cfgChan.supplement);
//            
//            
//            System.out.println(cfgChan.toString());
            Assert.assertEquals("Configuration has at least one incorrect field", CFG_1, cfgChan);
            
            
        } catch (FileNotFoundException | SecurityException e) {
            fail("Exception thrown while loading " + strFile + ": " + e.getMessage());
            e.printStackTrace();
        }
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.config.YamlLoader#load(File, Class)}.
     */
    @Test
    public void testLoadFile1() {
        String      strFile = STR_CONFIG_FILENAME_1;
        URL         urlFile = this.getClass().getClassLoader().getResource(strFile);
        
        try {
            File        fileCfg = new File(urlFile.toURI());
            GrpcChannelConfig   cfgChan = YamlLoader.load(fileCfg, GrpcChannelConfig.class);
            
//            System.out.println("name = " + cfgChan.name);
//            System.out.println("description = " + cfgChan.description);
//            System.out.println("supplement = " + cfgChan.supplement);
//            
//            
//            System.out.println(cfgChan.toString());
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
     * Test method for {@link com.ospreydcs.dp.api.config.EnvOverrideUtility#override(Object)}.
     */
    @Test
    public void testOverrideConfig1() {
        String      strFile = STR_CONFIG_FILENAME_1;
        
        try {
            GrpcChannelConfig   cfgChan = YamlLoader.load(strFile, GrpcChannelConfig.class);

            boolean bolResult = EnvOverrideUtility.override(cfgChan);
            
            if (!bolResult)
                fail("No parameters were overridden.");
            
//            System.out.println(cfgChan.toString());
            Assert.assertEquals("Configuration has at least one incorrect field", CFG_ENV, cfgChan);
            
            
        } catch (FileNotFoundException | SecurityException e) {
            fail("Exception thrown while loading " + strFile + ": " + e.getMessage());
            e.printStackTrace();
            
        } catch (IllegalArgumentException e) {
            fail("There was and attempt to override with invalid argument in GrpcChannelConfig.");
            e.printStackTrace();
            
        } catch (IllegalAccessException e) {
            fail("The GrpcChannelConfig instance refused access to its fields.");
            e.printStackTrace();
        }
        
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.config.EnvOverrideUtility#parse(Object)}.
     */
    @Test
    public void testParseConfig1() {
        try {
            List<EnvVariable>   lstVars = EnvOverrideUtility.parse(CFG_1);
            
            Assert.assertTrue("There were no environment variables", lstVars.size() > 0);

            System.out.println();
            System.out.println("Environment Variables:");
            for (EnvVariable var : lstVars) 
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
     * Test method for {@link com.ospreydcs.dp.api.config.grpc.GrpcChannelConfig#toString()}.
     */
    @Test
    public void testToString1() {
        String      strFile = STR_CONFIG_FILENAME_1;
        
        try {
            GrpcChannelConfig   cfgChan = YamlLoader.load(strFile, GrpcChannelConfig.class);
            
            System.out.println();
            System.out.println("GrpcChannelConfig Parameters:");
            System.out.println("name = " + cfgChan.name);
            System.out.println("version = " + cfgChan.version);
            System.out.println("description = " + cfgChan.description);
            System.out.println("supplement = " + cfgChan.supplement);
            

            System.out.println("GrpcChannelConfig#toString() for " + strFile);
            System.out.println(cfgChan.toString());
            
            
        } catch (FileNotFoundException | SecurityException e) {
            fail("Exception thrown while loading " + strFile + ": " + e.getMessage());
            e.printStackTrace();
        }
        
    }

    
    
    //
    // Support Methods
    //
    
    /**
     * Creates and populates a <code>GrpcChannelConfig</code> structure equivalent to the
     * first test configuration.
     * 
     * @return new, populated test configuration
     */
    private static GrpcChannelConfig    createConfig1() {
        
        GrpcChannelConfig.Channel.Host host = new GrpcChannelConfig.Channel.Host();
        host.url = "localhost";
        host.port = 50051;
        
        GrpcChannelConfig.Channel.Grpc grpc = new GrpcChannelConfig.Channel.Grpc();
        grpc.messageSize = 4194304L;
        grpc.timeout = 60;
        grpc.usePlainText = false;
        grpc.keepAliveWithoutCalls = true;
        grpc.gzip = false;
        
        GrpcChannelConfig.Channel chan = new GrpcChannelConfig.Channel();
        chan.host = host;
        chan.grpc = grpc;

        GrpcChannelConfig   cfg = new GrpcChannelConfig();
        cfg.channel = chan;
        cfg.name = "test-grpc-channel-1";
        cfg.version = "0.0.0";
        cfg.description = "Testing parameter set for default channel configuration";
        cfg.supplement = "No supplemental parameters";
        
        return cfg;
    }
    
    /**
     * Creates and populates a <code>GrpcChannelConfig</code> structure that is equivalent
     * to the first test case after being overridden by environment variables.
     * @return
     */
    private static GrpcChannelConfig    createConfigEnv() {
        
        GrpcChannelConfig   cfg = createConfig1();
        
        cfg.channel.grpc.messageSize = 16777216L;
        cfg.channel.grpc.timeout = 5;
        cfg.channel.grpc.usePlainText = true;
        cfg.channel.grpc.keepAliveWithoutCalls = false;
        cfg.channel.grpc.gzip = true;
        
        return cfg;
    }
}
