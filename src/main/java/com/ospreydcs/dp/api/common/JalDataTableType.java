/*
 * Project: dp-api-common
 * File:	JalDataTableType.java
 * Package: com.ospreydcs.dp.api.common
 * Type: 	JalDataTableType
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
 * @since Aug 8, 2025
 *
 */
package com.ospreydcs.dp.api.common;

/**
 * <p>
 * Enumeration of all available JAL data table types for time-series data requests.
 * </p>
 * <p>
 * The constants represents the types data tables available from the result of a time-series data request
 * using the Java API Library.  There are essentially two available types:
 * <ul>
 * <li><code>STATIC</code> - all table data is instantiated and accessible at creation.</li>
 * <li><code>DYNAMIC</code> - table data is instantiated at time of access.</li>
 * </ul>
 * Clearly a <code>STATIC</code> data table has faster access time but stresses Java heap resources.  These
 * tables should be used for smaller result sets.  A <code>DYNAMIC</code> data table has slower access times
 * but does not place as large a demand on heap resources.  The <code>DYNAMIC</code> data table is analogous
 * to sparse arrays in numerical processing.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>The JAL Query API returns all data tables as <code>{@link IDataTable}</code> interfaces so that the implementation
 *     (i.e., static or dynamic) is hidden to the client.
 * </li> 
 * <li>The JAL Query API generally decides which table type is returned to the client based upon the returned 
 *     result set size.
 * </li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 8, 2025
 *
 */
public enum JalDataTableType {
    
    /**
     * A data table where all table data is available and accessible after creation.
     */
    STATIC,
    
    /**
     * A data table where table data is instantiated at time of access.
     */
    DYNAMIC,
    
    /**
     * The data table type (e.g., <code>STATIC</code> or <code>DYNAMIC</code>) is determined by the JAL.
     */
    AUTO,
    
    /**
     * Indicates an unsupported table type; that is, an error condition
     */
    UNSUPPORTED,

}
