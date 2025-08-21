/*
 * Project: dp-api-common
 * File:	JalTestRequestConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config.request
 * Type: 	JalTestRequestConfig
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
 * @since May 10, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.config.request;

import java.util.List;

import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing configuration parameters for Java API Library Query tools test requests.
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 10, 2025
 *
 */
public class JalTestRequestConfig extends CfgStructure<JalTestRequestConfig> {

    /** Default constructor required for super class */
    public JalTestRequestConfig() { super(JalTestRequestConfig.class); }

    
    //
    // Configuration Parameters
    //
    
    /** List of test request test suites */
    public List<JalRequestSuiteConfig>       testSuites;
}
