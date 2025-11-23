/*
 * Project: dp-api-common
 * File:	package-info.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen
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
 * @since Jun 14, 2025
 *
 */
/**
 * <p>
 * Package containing resources for JAL tools supporting Ingestion Service evaluations.
 * </p>
 * <p>
 * Sub-package contain common resources for JAL Tools and applications that require simulated data generation.
 * The classes and resources within the sub-packages are used in the generation of simulated data.  Simulated
 * data is generally used to test the Ingestion Service of the Data Platform, or components of the JAL library
 * that perform ingestion operations.
 * </p>
 * <p>
 * <h2>Time-Series Data</h2>
 * The resources contained within this package and its sub-packages are primarily for generating
 * simulated time-series data for use in evaluation the Data Platform Ingestion Service.
 * </p> 
 * <p>
 * <h2>Sub-Packages</h2>
 * <ul>
 * <li><code>values</code> - contains "value factories" and support, classes that generate simulated data values.
 * <li><code>frames</code> - contains "frame factories" and support, classes that generate simulated ingestion frames.
 * <li><code>utility</code> - utility classes that perform operations on complex data types
 * </ul>
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Jun 14, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen;