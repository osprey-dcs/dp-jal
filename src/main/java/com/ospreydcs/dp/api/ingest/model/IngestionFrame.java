/*
 * Project: dp-api-common
 * File:	IngestionFrame.java
 * Package: com.ospreydcs.dp.api.ingest.model
 * Type: 	IngestionFrame
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
 * @since Mar 29, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model;

/**
 * <p>
 * <h1>The unit of data ingestion for the Ingestion Service client API.</h1>
 * </p>
 * </p>
 * Defines a unit of correlated, heterogeneous, time-series data for data ingestion by the Data Platform
 * Ingestion Service.  The basic structure is a table where a table column represent each independent time-series 
 * plus an additional column of timestamps for each of the table columns.  Being correlated, each timestamp applies
 * to all table columns according to row position.
 * </p>
 * <p>
 * <h2>Sampling Clocks</h2>
 * Rather than explicitly provide the table timestamps, a <em>sampling clock</em> can be assigned.  In this case
 * the sampling is assumed to be uniform with the sampling clock specifying the sampling period and start time 
 * instant.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Mar 29, 2024
 *
 */
public class IngestionFrame {

    /**
     * <p>
     * Constructs a new instance of <code>IngestionFrame</code>.
     * </p>
     *
     */
    public IngestionFrame() {
        // TODO Auto-generated constructor stub
    }

}
