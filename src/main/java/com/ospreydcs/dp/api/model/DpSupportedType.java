/*
 * Project: dp-api-common
 * File:	DpSupportedType.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	DpSupportedType
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
 * @since Jan 8, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model;

import java.util.Map;
import java.util.Vector;

import com.google.protobuf.ByteString;

/**
 * <p>
 * Enumeration of data types supported as heterogeneous <em>data values</em> within the Data Platform.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Jan 8, 2024
 *
 */
public enum DpSupportedType {

    BOOLEAN(Boolean.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    STRING(String.class),
    RAW(ByteString.class),
    IMAGE(BufferedImage.class),
    ARRAY(Vector.class),
    STRUCTURE(Map.class)
    ;

    //
    // Attributes
    //
    
    /** Java class type used to represent data value instances */
    private final Class<? extends Object>       clsType;
    
    /**
     * <p>
     * Constructs each enumeration instance of <code>DpSupportedType</code>.
     * </p>
     *
     * @param clsType   Java class type used to represent data value instances 
     */
    private DpSupportedType(Class<? extends Object> clsType) {
        this.clsType = clsType;
    }
    
    
    //
    // Public Methods
    //
    
    /**
     * Returns the Java class type used within Data Platform model to represent data of this type.
     * 
     * @return  Java class type representing this data value type
     */
    public Class<? extends Object>  getType() {
        return this.clsType;
    }
}
