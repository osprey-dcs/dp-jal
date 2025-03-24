/*
 * Project: dp-api-common
 * File:	package-info.java
 * Package: com.ospreydcs.dp.api.query.model.correl
 * Type: 	package-info
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
 * @since Jan 12, 2025
 *
 * TODO:
 * - None
 */
/**
 * <p>
 * Package containing resources for correlating raw time-series data from Query Service request responses.
 * </p>
 * <p>
 * There are 2 implementations for raw time-series correlation within this package.  The implementations differ
 * in their support of raw data timestamps.
 * <ol> 
 * <li>The original implementation which supports only Query Service <code>SamplingClock</code> messages.</li>
 * <li>The new implementation supports both <code>SamplingClock</code> and <code>TimestampList</code> messages.</li>
 * </ol>
 * The classes and resource for each implementation are as follows:
 * <ol>
 * <li>Original Implementation
 *   <ul>
 *   <li><code>CorrelatedQueryData</code> - a block of raw data correlated to a single <code>SamplingClock</code> message.</li>
 *   <li><code>DataBucketInsertTask</code> - thread task for inserting "data bucket" into a set of <code>CorrelatedQueryData</code>.</li>
 *   <li><code>QueryDataCorrelator</code> - main processor of <code>QueryData</code> messages into <code>CorrelatedQueryData</code> sets.</li>
 *   </ul>
 * </li>
 * <li>New Implementation
 *   <ul>
 *   <li><code>CorrelatedRawData</code> - base class replacing <code>CorrelatedQueryData</code>.</li>
 *   <li><code>UniformRawData</code> - child class supporting raw data correlated to a <code>SamplingClock</code> message.</li>
 *   <li><code>SpuriousRawData</code> - child class supporting raw data correlated to a <code>TimestampList</code> message.</li>
 *   <li><code>RawDataInsertTask</code> - thread task replacing <code>DataBucketInsertTask</code>.</li>
 *   <li><code>RawDataCorrelator</code> - main processor of <code>QueryData</code> messages replacing <code>QueryDataCorrelator</code>.</li>
 *   </ul>
 * </li>
 * </ol>
 * The following resources are used in both implementations:
 * <ul>
 *   <li><code>MessageTransferTask</code> - thread task transfer <code>QueryData</code> message from a source to a sink.</li>
 * </ul> 
 * The first implementation will likely be deprecated and eventually removed.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 12, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.correl;