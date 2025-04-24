/*
 * Project: dp-api-common
 * File:	package-info.java
 * Package: com.ospreydcs.dp.api.query
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
 * @since Dec 31, 2023
 *
 * TODO:
 * - None
 */
/**
 * <p>
 * Package supporting Data Platform Query Service operations.
 * </p>
 * <h2>Query Service APIs</h2>
 * Currently there is only one interface for the Data Platform Query Service, <code>IQueryService</code>.
 * It contains operations for both time-series data requests and metadata requests.
 * <p>
 * <b>Time-Series Data Requests</b>
 * <br/>  
 * Results of time-series
 * data requests are returned as <code>IDataTable</code> instances.  Additionally, clients can request
 * a data stream object represented by a <code>DpQueryStreamBuffer</code> instance.  In the latter case
 * client do their own data processing.
 * </p>
 * <p>
 * <b>Metadata Requests</b>
 * <br/>
 * Metadata results are returned as lists of metadata records, for example <code>MetadataRecord</code>.  There will
 * be one record for every matching criteria within the request.
 * </p>
 * <p>
 * <h2>Connection Factory</h2>
 * Implementations of <code>IQueryService</code> are obtained from the Query Service API <em>connection factory</em>.
 * Specifically, the class <code>DpQueryApiFactoryOld</code> contains numerous static methods for connecting to the
 * Data Platform Query Service and returning an appropriate interface.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 31, 2023
 *
 */
package com.ospreydcs.dp.api.query;