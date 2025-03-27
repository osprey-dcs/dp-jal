/*
 * Project: dp-api-common
 * File:	package-info.java
 * Package: com.ospreydcs.dp.api.query.model.series
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
 * @since Jan 11, 2024
 *
 * TODO:
 * - None
 */
/**
 * <p>
 * Contains classes and resources for defining formal correlated, time-series processes.
 * </p>
 * <p>
 * There are 2 implementations for time-series processes within this package.  The implementations differ
 * in their support of process data timestamps.
 * <ol> 
 * <li>The original implementation which supports only uniformly sampled processes (i.e., those obtained from 
 * Query Service <code>SamplingClock</code> messages.)
 * </li>
 * <li>The new implementation supports both uniformly sampled processes and non-uniformly sampled processes 
 * requiring explicit timestamps (i.e., those obtained from <code>SamplingClock</code> and <code>TimestampList</code> 
 * messages, respectively).
 * </li>
 * </ol>
 * The classes and resource for each implementation are as follows:
 * <ol>
 * <li>Original Implementation
 *   <ul>
 *   <li><code>UniformSamplingBlock</code> - A block (collection) of uniformly sampled processes over a time range.</li>
 *   <li><code>SamplingProcess</code> - An aggregation of <code>UniformSamplingBlock</code> objects.</li>
 *   </ul>
 * </li>
 * <li>New Implementation
 *   <ul>
 *   <li><code>UniformSampledBlock</code> - Child class supporting uniformly sampled processes.</li>
 *   <li><code>SpuriousRawData</code> - Child class supporting arbitrarily sampled processes.</li>
 *   <li><code>SampledBlock</code> - Base class of the above.</li>
 *   </ul>
 * </li>
 * </ol>
 * The following resources are used in both implementations:
 * <ul>
 *   <li><code>SampledTimeSeries</code> - A single sampling process with explicit timestamps.</li>
 * </ul> 
 * The first implementation will likely be deprecated and eventually removed.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Jan 11, 2024
 *
 */
package com.ospreydcs.dp.api.query.model.series;