/*
 * Project: dp-api-common
 * File:	JalRequestSuiteConfigDep.java
 * Package: com.ospreydcs.dp.jal.tools.config.request
 * Type: 	JalRequestSuiteConfigDep
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
 * @since May 9, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.config.request;

import java.util.List;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.config.model.CfgStructure;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.jal.tools.query.testrequests.TestArchiveRequest;

/**
 * <p>
 * Structure class containing test request suite configurations for the Data Platform query tools.
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 9, 2025
 *
 * @deprecated No longer used
 */
@Deprecated(since="Aug 22, 2025")
public class JalRequestSuiteConfigDep extends CfgStructure<JalRequestSuiteConfigDep> {

    /** Default constructor required for super class */
    public JalRequestSuiteConfigDep() { super(JalRequestSuiteConfigDep.class); }

    
    //
    // Configuration Parameters
    //
    
    /** The test suite YAML header */
    public TestSuiteConfig            testSuite;
    
    
    /**
     * Structure class containing test request test suite parameters 
     */
    public static class TestSuiteConfig extends CfgStructure<TestSuiteConfig> {
        
        /** Default constructor required by super class */
        public TestSuiteConfig() { super(TestSuiteConfig.class); };

        
        //
        // Configuration Fields
        //
        
        /** Test suite name */
        public String                       name;

        /** Test request enumeration constant identifier */
        public List<TestArchiveRequest>     requestIds;

        /** Test request decomposition strategy */
        public List<RequestDecompType>      requestComposites;

        /** Number of gRPC streams used for recovery (assuming decomposition) */
        public List<Integer>                streamCounts;

        /** Test request gRPC stream type used for recovery */
        public List<DpGrpcStreamType>       streamTypes;
    }
}
