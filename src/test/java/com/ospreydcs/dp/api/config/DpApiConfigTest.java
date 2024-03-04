/*
 * Project: dp-api-common
 * File:	DpApiConfigTest.java
 * Package: com.ospreydcs.dp.api.config
 * Type: 	DpApiConfigTest
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
 * @since Dec 21, 2023
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.model.CfgOverrideRec;
import com.ospreydcs.dp.api.config.model.CfgOverrideUtility;


/**
 * <p>
 * JUnit test cases for class <code>DpApiConfig</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 21, 2023
 *
 */
public class DpApiConfigTest {
    
    
    //
    // Class Constants
    //
    
    public static final String  DP_API_ARCHIVE_INCEPTION = "2024-01-01T00:00:00.0Z";
    public static final String  DP_API_QUERY_PAGE_SIZE = "200";
    public static final int     DP_API_QUERY_CONCURRENCY_PIVOT_SIZE = 10;

    
    //
    // Class Resources
    //
    
    /** Map of system variable (name, value) pairs used for configuration overrides */
    public static final Map<String, String> MAP_VAR_PAIRS = Map.of(
            "DP_API_ARCHIVE_INCEPTION", DP_API_ARCHIVE_INCEPTION,
            "DP_API_QUERY_PAGE_SIZE", DP_API_QUERY_PAGE_SIZE
            );

    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        for (Map.Entry<String, String> pair : MAP_VAR_PAIRS.entrySet()) 
            System.setProperty(pair.getKey(), pair.getValue());
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
     * Test method for {@link com.ospreydcs.dp.api.config.DpApiConfig#getInstance()}.
     */
    @Test
    public final void testGetInstance() {
        
        String  strIncpt = DpApiConfig.getInstance().archive.inception;
        System.out.println("Data Platform TestArchive Inception: " + strIncpt);
        Instant insIncpt = Instant.parse(strIncpt);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.config.DpApiConfig#toString()}.
     */
    @Test
    public final void testToString() {
        DpApiConfig cfg = DpApiConfig.getInstance();
        
        System.out.println("DP API Default Configuration:");
        System.out.println(cfg);
    }
    
    @Test
    public final void testOverrides() {
        DpApiConfig cfg = DpApiConfig.getInstance();
        
        // Need to force system properties override in case this is part of a test suite
        try {
            CfgOverrideUtility.overrideRoot(cfg, CfgOverrideUtility.SOURCE.PROPERTIES);
            
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Assert.fail("Forced override of DpApiConfig instance threw exception: " + e.getMessage());
            
        }
        
//        Integer intPageSize = Integer.parseInt(DP_API_QUERY_PAGE_SIZE);
//        
//        Assert.assertEquals(intPageSize, cfg.query.pageSize); 
        Assert.assertEquals(DP_API_ARCHIVE_INCEPTION, cfg.archive.inception);
    }
    
    /**
     * Test method for {@link CfgOverrideUtility#extractOverrideables(Object, com.ospreydcs.dp.api.config.model.CfgOverrideUtility.SOURCE)}
     * as applied to {@link DpApiConfig}.
     */
    @Test
    public final void testExtractOverrideables() {
        DpApiConfig cfg = DpApiConfig.getInstance();
        
        try {
            List<CfgOverrideRec> lstRecs = CfgOverrideUtility.extractOverrideables(cfg, CfgOverrideUtility.SOURCE.ENVIRONMENT);
            
            for (CfgOverrideRec rec : lstRecs) 
                System.out.println(rec.printLine());
            
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Assert.fail("Override extract threw exception: " + e.getMessage());
        }
        
    }

}
