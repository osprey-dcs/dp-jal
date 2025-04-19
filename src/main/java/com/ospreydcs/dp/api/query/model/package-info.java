/*
 * Project: dp-api-common
 * File:	package-info.java
 * Package: com.ospreydcs.dp.api.query.model
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
 * @since Jan 7, 2024
 *
 * TODO:
 * - None
 */

/**
 * <p>
 * Package containing classes and resources that define the Java API Library model for Data Platform Query Service interaction.
 * </p>
 * <p>
 * <h2>Requests</h2>
 * Query requests are encapsulated in the classes <code>{@link DpMetadataRequest}</code> and <code>{@link DpDataRequest}</code> 
 * which are located in the package above this one.  However, the model does contain resource for decomposing time-series
 * request, represented by <code>DpDataRequest</code>, into composite requests for simultaneous gRPC data streaming.  These
 * resources are located in package <code>com.ospreydcs.dp.query.model.request</code>.
 * </p>
 * <p>
 * <h2>Request Processing</h2>
 * The processing of metadata requests is relatively straightforward.  The Query Service provides all metadata queries as
 * unary requests which are typically handled directly by Java API Library interface implementations.  The results are 
 * generally returned as collections of Java <code>record</code> instances matching the request.
 * </p>
 * <p>
 * Time-series data request are significantly more involved due to the nature of the data and the ability to use gRPC data
 * streams in request recovery.  The final result of a time-series data request is intended to be a data table containing
 * the request data, implementations for such a data table are encouraged to expose the <code>IDataTable</code> interface
 * to maintain consistency.  Listed below are the basic steps and model sub-packages used by the Java API Library model to 
 * request and build the resulting data table:
 * <ol>
 * <li>Data recovery (<code>model.grpc</code>) - data is recovered in the raw, "data bucket" format of MongoDB using gRPC streaming.</li>
 * <li>Data correlation (<code>model.correl</code>) - time-series data is correlated against common timestamps within buckets.</li>
 * <li>Data aggregation (<code>model.coalesce</code>) - data is coalesced into sampled "blocks" with disjoint time ranges.</li>
 * <li>Data assembly (<code>model.assem</code>) - sampled block are ordered and assembled into the full request.</li>
 * <li>Table creation (<code>model.table</code>) - the aggregated sampled blocks are used to create data tables (static or dynamic).</li>
 * </ol>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 7, 2024
 *
 */
package com.ospreydcs.dp.api.query.model;