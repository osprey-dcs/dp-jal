/*
 * Project: dp-api-common
 * File:	RawDataType.java
 * Package: com.ospreydcs.dp.api.query.model.correl
 * Type: 	RawDataType
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
 * @since Mar 12, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.correl;

/**
 * <p>
 * Enumeration of the <code>RawCorrelatedData</code> subclass types.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 12, 2025
 *
 */
public enum RawDataType {
    
    /**
     * Sampling data correlated with a uniforming sampling clock.
     */
    CLOCKED(RawClockedData.class),
    
    /**
     * Sampling data correlated to an explicit list of timestamps (supports spurious process data).
     */
    TIMESTAMPLIST(RawTmsListData.class);
    
    
    //
    // Enumeration Constant Attributes
    //
    
    /** The class type for the supported data */
    private final Class<? extends RawCorrelatedData>    clsType;
    
    /** Constructs and initializes the enumeration constant. */
    RawDataType(Class<? extends RawCorrelatedData> clsType) {
        this.clsType = clsType;
    }
    
    /** Return the class type for the correlated data indicated by the constant. */ 
    Class<? extends RawCorrelatedData>  getClassType() { return this.clsType; };

}
