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

import static org.junit.Assert.*;

import java.time.Instant;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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

    /**
     * Test method for {@link com.ospreydcs.dp.api.config.DpApiConfig#getInstance()}.
     */
    @Test
    public final void testGetInstance() {
        
        String  strIncpt = DpApiConfig.getInstance().archive.inception;
        System.out.println("Data Platform Archive Inception: " + strIncpt);
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

}
