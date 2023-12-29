/*
 * Project: dp-api-common
 * File:	MongoDbConfigTest.java
 * Package: com.ospreydcs.dp.api.config.db
 * Type: 	MongoDbConfigTest
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
 * @since Dec 19, 2023
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config.db;

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

import com.ospreydcs.dp.api.config.model.CfgOverrideUtility;
import com.ospreydcs.dp.api.config.model.CfgParameter;
import com.ospreydcs.dp.api.config.model.CfgLoaderYaml;

/**
 * <p>
 * JUnit test cases for class <code>MongoDbConfig</code>
 * </p>
 * <p>
 * Tests correct loading of YAML files using the CfgLoaderYaml utility class.
 * Also test the environment variable override feature.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Dec 19, 2023
 *
 */
public class MongoDbConfigTest {

    
    //
    // Class Constants
    //
    
    /** YAML configuration file location */
    public static final String      STR_CONFIG_FILENAME_1 = "test-mongodb-1.yml";
    
    /** Comparison configuration records */
    public static MongoDbConfig     CFG_1 = createConfig1();
    public static MongoDbConfig     CFG_ENV = createConfigEnv();
    
    
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
     * Test method for {@link com.ospreydcs.dp.api.config.model.CfgLoaderYaml#load(String, Class)}.
     */
    @Test
    public void testLoadString1() {
        String      strFile = STR_CONFIG_FILENAME_1;
        
        try {
            MongoDbConfig   cfgMongo = CfgLoaderYaml.load(strFile, MongoDbConfig.class);
            
            Assert.assertEquals("Configuration has at least one incorrect field", CFG_1, cfgMongo);
            
            
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
            File        fileCfg = new File( urlFile.toURI() );
            
            MongoDbConfig   cfgMongo = CfgLoaderYaml.load(fileCfg, MongoDbConfig.class);
            
            Assert.assertEquals("Configuration has at least one incorrect field", CFG_1, cfgMongo);
            
            
        } catch (FileNotFoundException | SecurityException e) {
            fail("Exception thrown while loading " + strFile + ": " + e.getMessage());
            e.printStackTrace();
            
        } catch (URISyntaxException e) {
            fail("The URL failed to convert to an URI - Testing error that should not occur.");
            e.printStackTrace();
        }
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.config.model.CfgOverrideUtility#envOverride(Object)}.
     */
    @Test
    public void testOverrideConfig1() {
        String      strFile = STR_CONFIG_FILENAME_1;
        
        try {
            MongoDbConfig   cfgMongo = CfgLoaderYaml.load(strFile, MongoDbConfig.class);

            boolean bolResult = CfgOverrideUtility.envOverride(cfgMongo);
            
            if (!bolResult)
                fail("No parameters were overridden.");
            
//            System.out.println(cfgChan.toString());
            Assert.assertEquals("Configuration has at least one incorrect field", CFG_ENV, cfgMongo);
            
            
        } catch (FileNotFoundException | SecurityException e) {
            fail("Exception thrown while loading " + strFile + ": " + e.getMessage());
            e.printStackTrace();
            
        } catch (IllegalArgumentException e) {
            fail("There was and attempt to override with invalid argument in MongoDbConfig.");
            e.printStackTrace();
            
        } catch (IllegalAccessException e) {
            fail("The GrpcConnectionConfig instance refused access to its fields.");
            e.printStackTrace();
        }
        
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.config.model.CfgOverrideUtility#parse(Object)}.
     */
    @Test
    public void testParseConfig1() {
        try {
            List<CfgParameter>   lstVars = CfgOverrideUtility.parse(CFG_1);
            
            Assert.assertTrue("There were no environment variables", lstVars.size() > 0);

            System.out.println();
            System.out.println("Environment Variables:");
            for (CfgParameter var : lstVars) 
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
     * Test method for {@link com.ospreydcs.dp.api.config.db.MongoDbConfig#toString()}.
     */
    @Test
    public void testToString() {
        String      strFile = STR_CONFIG_FILENAME_1;
        
        try {
            MongoDbConfig   cfgMongo = CfgLoaderYaml.load(strFile, MongoDbConfig.class);
            
            System.out.println("name = " + cfgMongo.name);
            System.out.println("version = " + cfgMongo.version);
            System.out.println("description = " + cfgMongo.description);
            System.out.println("supplement = " + cfgMongo.supplement);
            

            System.out.println("MongoDbConfig#toString() for " + strFile);
            System.out.println(cfgMongo.toString());
            
            
        } catch (FileNotFoundException | SecurityException e) {
            fail("Exception thrown while loading " + strFile + ": " + e.getMessage());
            e.printStackTrace();
        }
        
    }



    //
    // Support Methods
    //
    
    /**
     * Creates and populates a <code>MongoDbConfig</code> structure equivalent to the
     * first test configuration.
     * 
     * @return new, populated test configuration
     */
    private static MongoDbConfig    createConfig1() {
        
        MongoDbConfig.Host host = new MongoDbConfig.Host();
        host.url = "localhost";
        host.port = 27017;
        
        MongoDbConfig.Client client = new MongoDbConfig.Client();
        client.user = "admin";
        client.password = "admin";
        
        MongoDbConfig   cfg = new MongoDbConfig();
        cfg.name = "test-mongodb-1";
        cfg.version = "0.0.0";
        cfg.description = "Testing parameter set for default MongoDB configuration";
        cfg.supplement = "No supplemental parameters";
        
        cfg.host = host;
        cfg.client = client;
        
        return cfg;
    }
    
    /**
     * Creates and populates a <code>MongoDbConfig</code> structure that is equivalent
     * to the first test case after being overridden by environment variables.
     * @return
     */
    private static MongoDbConfig    createConfigEnv() {
        
        MongoDbConfig   cfg = createConfig1();
        
        cfg.client.user = "captain";
        cfg.client.password = "steve";
        
        return cfg;
    }
}
