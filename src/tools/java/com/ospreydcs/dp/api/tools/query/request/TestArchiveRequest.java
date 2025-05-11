/*
 * Project: dp-api-common
 * File:	TestArchiveRequest.java
 * Package: com.ospreydcs.dp.api.tools.query.request
 * Type: 	TestArchiveRequest
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
package com.ospreydcs.dp.api.tools.query.request;

import java.io.PrintStream;
import java.time.Duration;

import com.ospreydcs.dp.api.query.DpDataRequest;

/**
 * <p>
 * Enumeration of exampled Data Platform test archive time-series data requests.
 * </p>
 * <p>
 * The enumeration constants define a standard battery of example requests available for Query Service testing.
 * Each constant can create <code>DpDataRequest</code> instances for its defined request.  The enumerated test
 * requests cover a range of request type, data source counts, and requested time durations.
 * </p> 
 * <p>
 * <h2>Request Types</h2>
 * There are 4 type of time-series data request represented within the collection of enumeration constants.
 * They are prefixed according to the following:
 * <ol>
 * <li><code>CLOCKED</code> - requested data sources are sampled with uniform sampling clocks.</li>
 * <li><code>TMS_LIST</code> - requested data sources are sampled with explicit timestamp lists.</li>
 * <li><code>BOTH</code> - requested data source are sampled with <em>both</em> sampling clocks <em>and</em>
 *     explicit timestamp lists.</li>
 * <li><code>GENERAL</code> - the represented request is indifferent to source of timestamps.  These constants
 *     are for large data requests covering the breadth of the test archive.</li>
 * </ol>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * Example request instances created by enumeration constants with method <code>{@link #create()}</code> defer 
 * to the <code>DpTestArchiveRquestCreator</code> utility.
 * </li>
 * <li>
 * Enumeration constants that create requests containing both types of data sources (i.e., those using sample
 * clocks and those using explicit timestamp lists) rely on the assumed format of the Data Platform test archive.
 * </li>
 * <li>
 * Estimated sizes of the recovered data through <code>{@link #estimatedRequestSize()}</code> rely on the
 * assumed format of the Data Platform test archive.
 * </li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 7, 2025
 *
 */
public enum TestArchiveRequest {
    
    EMPTY_REQUEST(TestRequestType.GENERAL, 0, 0, Duration.ZERO, Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 1</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_1PVx1SEC(TestRequestType.CLOCKED, 1, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 1</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 5 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_1PVx5SEC(TestRequestType.CLOCKED, 1, 0, Duration.ofSeconds(5L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 1</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_1PVx10SEC(TestRequestType.CLOCKED, 1, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 1</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_1PVx20SEC(TestRequestType.CLOCKED, 1, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 5</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_5PVSx1SEC(TestRequestType.CLOCKED, 5, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 5</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 5 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_5PVSx5SEC(TestRequestType.CLOCKED, 5, 0, Duration.ofSeconds(5L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 5</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_5PVSx10SEC(TestRequestType.CLOCKED, 5, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 5</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_5PVSx20SEC(TestRequestType.CLOCKED, 5, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 10</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_10PVSx1SEC(TestRequestType.CLOCKED, 10, 0, Duration.ofSeconds(1L), Duration.ZERO),

    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 10</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_10PVSx10SEC(TestRequestType.CLOCKED, 10, 0, Duration.ofSeconds(10L), Duration.ZERO),

    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 10</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_10PVSx20SEC(TestRequestType.CLOCKED, 10, 0, Duration.ofSeconds(20L), Duration.ZERO),

    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 50</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_50PVSx1SEC(TestRequestType.CLOCKED, 50, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 100</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_100PVSx1SEC(TestRequestType.CLOCKED, 100, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 100</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_100PVSx10SEC(TestRequestType.CLOCKED, 100, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 100</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_100PVSx20SEC(TestRequestType.CLOCKED, 100, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 500</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_500PVSx1SEC(TestRequestType.CLOCKED, 500, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 500</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_500PVSx10SEC(TestRequestType.CLOCKED, 500, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 500</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_500PVSx20SEC(TestRequestType.CLOCKED, 500, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 1000</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_1000PVSx1SEC(TestRequestType.CLOCKED, 1000, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 1000</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 5 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_1000PVSx5SEC(TestRequestType.CLOCKED, 1000, 0, Duration.ofSeconds(5L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 1000</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_1000PVSx10SEC(TestRequestType.CLOCKED, 1000, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 1000</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_1000PVSx20SEC(TestRequestType.CLOCKED, 1000, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 2000</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_2000PVSx1SEC(TestRequestType.CLOCKED, 2000, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 2000</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 5 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_2000PVSx5SEC(TestRequestType.CLOCKED, 2000, 0, Duration.ofSeconds(5L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 2000</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_2000PVSx10SEC(TestRequestType.CLOCKED, 2000, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a clocked-source time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#CLOCKED}</code></li>
     * <li>Data Source (PV) Count = 2000</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    CLOCKED_2000PVSx20SEC(TestRequestType.CLOCKED, 2000, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 1</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_1PVx1SEC(TestRequestType.TMS_LIST, 1, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 1</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_1PVx10SEC(TestRequestType.TMS_LIST, 1, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 1</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_1PVx20SEC(TestRequestType.TMS_LIST, 1, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 5</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_5PVSx1SEC(TestRequestType.TMS_LIST, 5, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 5</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 5 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_5PVSx5SEC(TestRequestType.TMS_LIST, 5, 0, Duration.ofSeconds(5L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 5</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_5PVSx10SEC(TestRequestType.TMS_LIST, 5, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 5</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_5PVSx20SEC(TestRequestType.TMS_LIST, 5, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 10</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_10PVSx1SEC(TestRequestType.TMS_LIST, 10, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 50</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_50PVSx1SEC(TestRequestType.TMS_LIST, 50, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 100</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_100PVSx1SEC(TestRequestType.TMS_LIST, 100, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 100</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_100PVSx10SEC(TestRequestType.TMS_LIST, 100, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 100</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_100PVSx20SEC(TestRequestType.TMS_LIST, 100, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 200</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_200PVSx1SEC(TestRequestType.TMS_LIST, 200, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 200</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 5 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_200PVSx5SEC(TestRequestType.TMS_LIST, 200, 0, Duration.ofSeconds(5L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 200</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_200PVSx10SEC(TestRequestType.TMS_LIST, 200, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using an explicit timestamp list 
     * with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#TMS_LIST}</code></li>
     * <li>Data Source (PV) Count = 200</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    TMS_LIST_200PVSx20SEC(TestRequestType.TMS_LIST, 200, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 2</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 1 </li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 1 clocked data source(s) and 1 data source(s) with explicit timestamp list(s)
     */
    BOTH_2PVSx1SEC(TestRequestType.BOTH, 2, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 2</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 1 </li>
     * <li>Request Duration = 5 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 1 clocked data source(s) and 1 data source(s) with explicit timestamp list(s)
     */
    BOTH_2PVSx5SEC(TestRequestType.BOTH, 2, 0, Duration.ofSeconds(5L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 2</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 1 </li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 1 clocked data source(s) and 1 data source(s) with explicit timestamp list(s)
     */
    BOTH_2PVSx10SEC(TestRequestType.BOTH, 2, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 2</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 1 </li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 1 clocked data source(s) and 1 data source(s) with explicit timestamp list(s)
     */
    BOTH_2PVSx20SEC(TestRequestType.BOTH, 2, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 5</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 2 </li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 2 clocked data sources and 3 data sources with explicit timestamp list(s)
     */
    BOTH_5PVSx1SEC(TestRequestType.BOTH, 5, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 5</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 2 </li>
     * <li>Request Duration = 5 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 2 clocked data sources and 3 data sources with explicit timestamp list(s)
     */
    BOTH_5PVSx5SEC(TestRequestType.BOTH, 5, 0, Duration.ofSeconds(5L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 5</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 2 </li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 2 clocked data sources and 3 data sources with explicit timestamp list(s)
     */
    BOTH_5PVSx10SEC(TestRequestType.BOTH, 5, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 5</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 2 </li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 2 clocked data sources and 3 data sources with explicit timestamp list(s)
     */
    BOTH_5PVSx20SEC(TestRequestType.BOTH, 5, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 10</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 5 </li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 5 clocked data sources and 5 data sources with explicit timestamp list(s)
     */
    BOTH_10PVSx1SEC(TestRequestType.BOTH, 10, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 10</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 5 </li>
     * <li>Request Duration = 5 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 5 clocked data sources and 5 data sources with explicit timestamp list(s)
     */
    BOTH_10PVSx5SEC(TestRequestType.BOTH, 10, 0, Duration.ofSeconds(5L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 10</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 5 </li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 5 clocked data sources and 5 data sources with explicit timestamp list(s)
     */
    BOTH_10PVSx10SEC(TestRequestType.BOTH, 10, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 10</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 5 </li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 5 clocked data sources and 5 data sources with explicit timestamp list(s)
     */
    BOTH_10PVSx20SEC(TestRequestType.BOTH, 10, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 50</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 25 </li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 25 clocked data sources and 25 data sources with explicit timestamp list(s)
     */
    BOTH_50PVSx1SEC(TestRequestType.BOTH, 50, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 50</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 25 </li>
     * <li>Request Duration = 5 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 25 clocked data sources and 25 data sources with explicit timestamp list(s)
     */
    BOTH_50PVSx5SEC(TestRequestType.BOTH, 50, 0, Duration.ofSeconds(5L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 50</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 25 </li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 25 clocked data sources and 25 data sources with explicit timestamp list(s)
     */
    BOTH_50PVSx10SEC(TestRequestType.BOTH, 50, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 50</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 25 </li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 25 clocked data sources and 25 data sources with explicit timestamp list(s)
     */
    BOTH_50PVSx20SEC(TestRequestType.BOTH, 50, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 50</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 50 </li>
     * <li>Request Duration = 1 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 50 clocked data sources and 50 data sources with explicit timestamp list(s)
     */
    BOTH_100PVSx1SEC(TestRequestType.BOTH, 100, 0, Duration.ofSeconds(1L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 50</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 50 </li>
     * <li>Request Duration = 5 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 50 clocked data sources and 50 data sources with explicit timestamp list(s)
     */
    BOTH_100PVSx5SEC(TestRequestType.BOTH, 100, 0, Duration.ofSeconds(5L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 100</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 50 </li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 50 clocked data sources and 50 data sources with explicit timestamp list(s)
     */
    BOTH_100PVSx10SEC(TestRequestType.BOTH, 100, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request for data sources using both a uniform sampling clock and 
     * an explicit timestamp list with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 100</li>
     * <li>Data Source 1st Index = <code>{@link DpTestArchiveRequestCreator#IND_FIRST_PV_TMS_LIST}</code> - 50 </li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     * The result is 50 clocked data sources and 50 data sources with explicit timestamp list(s)
     */
    BOTH_100PVSx20SEC(TestRequestType.BOTH, 100, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 2000</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 5 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    GENERAL_2000PVSx5SEC(TestRequestType.GENERAL, 20000, 0, Duration.ofSeconds(5L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 2000</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    GENERAL_2000PVSx10SEC(TestRequestType.GENERAL, 20000, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 2000</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    GENERAL_2000PVSx20SEC(TestRequestType.GENERAL, 20000, 0, Duration.ofSeconds(20L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 2000</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    GENERAL_4000PVSx5SEC(TestRequestType.GENERAL, 40000, 0, Duration.ofSeconds(5L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 2000</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 10 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    GENERAL_4000PVSx10SEC(TestRequestType.GENERAL, 40000, 0, Duration.ofSeconds(10L), Duration.ZERO),
    
    /**
     * Specifies a time-series data request with the following properties:
     * <ul>
     * <li>Request Type = <code>{@link TestRequestType#GENERAL}</code></li>
     * <li>Data Source (PV) Count = 2000</li>
     * <li>Data Source 1st Index = 0</li>
     * <li>Request Duration = 20 second(s)</li>
     * <li>Request Start Time = 0 second(s)</li>
     * </ul>
     */
    GENERAL_4000PVSx20SEC(TestRequestType.GENERAL, 40000, 0, Duration.ofSeconds(20L), Duration.ZERO)
    
    ;
    
    
    
    //
    // Enumeration Constant Attributes
    //
    
    /** The request type - identifies the timestamp sources within the requested data */
    private final TestRequestType   enmType;

    /** The number of data sources (PVs) within the requested data */
    private final int               cntPvs;
    
    /** The index offset of the first data source within the requested data */
    private final int               indPvOffset;
    
    /** The time duration of the request */
    private final Duration          durRequest;
    
    /** The start time of the request, i.e., the offset from archive inception */
    private final Duration          durStartTime;
    
    
    /**
     * <p>
     * Constructs a new <code>TestArchiveRequest</code> instance.
     * </p>
     *
     * @param enmType       the generated request type - identifies the timestamp sources within the requested data
     * @param cntPvs        number of data sources (PVs) within the requested data 
     * @param indPvOffset   index offset of the first data source within the test archive 
     * @param durRequest    time duration for the requested data
     * @param durStartTime  start time for the requested data, i.e., the offset from archive inception
     */
    private TestArchiveRequest(TestRequestType enmType, int cntPvs, int indPvOffset, Duration durRequest, Duration durStartTime) {
        this.enmType = enmType;
        this.cntPvs = cntPvs;
        this.indPvOffset = indPvOffset;
        this.durRequest = durRequest;
        this.durStartTime = durStartTime;
    }
    
    
    //
    // Field Getters
    //
    
    /**
     * <p>
     * Returns the request type represented by this enumeration constant.
     * </p>
     * 
     * @return  the generated request type - identifies the timestamp sources within the requested data
     */
    public TestRequestType  getRequestType() {
        return this.enmType;
    }
    
    /**
     * <p>
     * Returns the number of data sources (PVs) queried by the time-series data request represented 
     * by this enumeration constant.
     * </p>
     *  
     * @return  number of data sources (PVs) within the requested data
     */
    public int  getDataSourceCount() {
        return this.cntPvs;
    }
    
    /**
     * <p>
     * Returns the index offset of the first data source queried by the time-series data request represented 
     * by this enumeration constant.
     * </p>
     * 
     * @return  index offset of the first data source within the test archive
     */
    public int  getDataSourceStartIndex() {
        return this.indPvOffset;
    }
    
    /**
     * <p>
     * Returns the time duration of the time-series data request represented by this enumeration constant.
     * </p>
     * 
     * @return
     */
    public Duration getRequestDuration() {
        return this.durRequest;
    }
    
    /**
     * <p>
     * Returns the start time offset of the time-series data request represented by this enumeration constant.
     * </p>
     * 
     * @return  start time for the requested data, i.e., the offset from archive inception
     */
    public Duration getStartTimeOffset() {
        return this.durStartTime;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance defined by the enumeration constant parameters.
     * </p>
     * <p>
     * The returned request instance is not owned by this enumeration constant and can be subsequently modified.
     * The request parameters are determined by the field of this enumeration constant.  See the field documents
     * for more information.
     * </p>
     * 
     * @return  a new <code>DpDataRequest</code> instance represented by this enumeration constant parameters
     * 
     * @see #getRequestType()
     * @see #getDataSourceCount()
     * @see #getDataSourceStartIndex()
     * @see #getRequestDuration()
     * @see #getStartTimeOffset()
     */
    public DpDataRequest    create() {
        DpDataRequest rqst = DpTestArchiveRequestCreator.createRequest(
                this.enmType, 
                this.cntPvs, 
                this.indPvOffset, 
                this.durRequest, 
                this.durStartTime);
        
        rqst.setRequestId(this.name());
        
        return rqst;
    }
    
    /**
     * <p>
     * Returns an estimate of the recovered data size for the request in bytes.
     * </p>
     * <p>
     * The returned estimate assumes that the Data Platform test archive is populated with double-valued
     * process variables sampled at a 1kHz rate.  Thus, the returned value <i>N</i> is given as
     * <pre>
     *     <i>N</i> = <code>{@link #getDataSourceCount()} * {@link #getRequestDuration()} * 1,000 * {@link Double#BYTES}</code>
     * </pre>
     * 
     * @return  the estimated size of the recovered data for the request represented by this enumeration constant
     */
    public long estimatedRequestSize() {
        long    lngSmplsPerPv = this.durRequest.toMillis();
        long    szRqst = lngSmplsPerPv * this.cntPvs * Double.BYTES;
        
        return szRqst;
    }
    
    /**
     * <p>
     * Prints out a text description of the enumeration constant parameters to the given output stream.
     * </p>
     * <p>
     * The enumeration constant parameters assigned during construction are printed out to the given output
     * stream along with a description heading.  The padding argument can be used to assign additional left-hand
     * white space before parameter value descriptions when the output is part of a larger output block.
     * </p>
     * 
     * @param ps        output stream to receive enumeration constant description.
     * @param strPad    optional left-hand white space padding for output lines
     */
    public void printOut(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        
        ps.println(strPad + this.getClass().getSimpleName() + "." + this.name() + " Properties");
        ps.println(strPad + "  Request ID           : " + this.name());
        ps.println(strPad + "  Request type         : " + this.getRequestType());
        ps.println(strPad + "  Data source count    : " + this.getDataSourceCount());
        ps.println(strPad + "  Source start index   : " + this.getDataSourceStartIndex());
        ps.println(strPad + "  Request duration     : " + this.getRequestDuration());
        ps.println(strPad + "  Start time offset    : " + this.getStartTimeOffset());
        ps.println(strPad + "  approx. size (Bytes) : " + this.estimatedRequestSize());
    }

}
