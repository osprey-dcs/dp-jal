/*
 * Project: dp-data-simulator
 * File:	HeteroType.java
 * Package: com.ospreydcs.dp.datasim.model
 * Type: 	HeteroType
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
 * @since Jun 13, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.jal.tools.common.data;

import com.ospreydcs.dp.jal.common.DpSupportedType;

/**
 * <p>
 * Enumeration of supported data value types for ingestion frame data columnConfigs.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Jun 13, 2024
 *
 */
public enum HeteroType {
    
    /** Scalar data type */
    SCALAR(DpSupportedType.UNSUPPORTED_TYPE),
    
    /** Multi-dimensional array (tensor) type */
    ARRAY(DpSupportedType.ARRAY),
    
    /** Complex data structure type */
    STRUCTURE(DpSupportedType.STRUCTURE),
    
    /** Image data type */
    IMAGE(DpSupportedType.IMAGE);

    
    /** The Data Platform supported type for this type */
    private final DpSupportedType   enmDpType;
    
    /** Enumeration constant constructor */
    private HeteroType(DpSupportedType enmDpType) { this.enmDpType = enmDpType; };
    
    
    //
    // Operations
    //
    
    /** Returns the Data Platform suported data type for this constant */
    public DpSupportedType  getDpType() { return this.enmDpType; };
}
