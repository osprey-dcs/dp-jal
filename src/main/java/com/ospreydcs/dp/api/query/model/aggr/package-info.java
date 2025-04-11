/*
 * Project: dp-api-common
 * File:	package-info.java
 * Package: com.ospreydcs.dp.api.query.model.aggr
 * Type: 	package-info
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
 * @since Apr 9, 2025
 *
 */
/**
 * <p>
 * Package containing resources for Query Service raw, time-series data processing.
 * </p>
 * <p>
 * The resources in this package are intended for situations that require an additional degree
 * of processing, when time domain collisions occur within correlated raw data.  Here we require
 * additional architecture to handle the situation to create the final data tables presented to clients.
 * </p>
 * <p>
 * In the future these package resources could be streamlined for all situations, thus bypassing the
 * much of the processing within the <code>com.ospreydcs.dp.api.query.series</code> package
 * (i.e., the <code>SampledBock</code> class hierarchy).
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Apr 9, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.aggr;