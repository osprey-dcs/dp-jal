/*
 * Project: dp-api-common
 * File:    TestRequestType.java
 * Package: com.ospreydcs.dp.api.tools.query.request
 * Type:    TestRequestType
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
 * @since May 6, 2025
 *
 */
package com.ospreydcs.dp.api.tools.query.request;

/**
 * <p>
 * Enumeration of all time-series data request types available from the utility class 
 * <code>TestArchiveRequestCreator</code>.
 * </p>
 */
public enum TestRequestType {
    
    /**
     * General time-series data request for data sources using sampling clock and/or explicit timestamp list.
     */
    GENERAL,
    
    /**
     * Time-series data request for data sources using a uniform sampling clock only. 
     */
    CLOCKED,
    
    /**
     * Time-series data request for data sources using an explicit timestamp list only. 
     */
    TMS_LIST,
    
    /**
     * The request contains data sources with both uniform sampling clocks <em>and</em> explicit timestamp lists.
     */
    BOTH;
}