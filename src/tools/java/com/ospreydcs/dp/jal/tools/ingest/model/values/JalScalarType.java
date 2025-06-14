/*
 * Project: dp-data-simulator
 * File:	JalScalarType.java
 * Package: com.ospreydcs.dp.datasim.frame.model
 * Type: 	JalScalarType
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
 * @since May 9, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.jal.tools.ingest.model.values;

import com.ospreydcs.dp.api.common.DpSupportedType;

/**
 * Enumeration of Java scalar values available for simulated data values.
 *
 * @author Christopher K. Allen
 * @since May 9, 2024
 *
 */
public enum JalScalarType {

    /** Boolean value type */
    BOOLEAN(Boolean.class, DpSupportedType.BOOLEAN),
    
    /** Integer valued type */
    INTEGER(Integer.class, DpSupportedType.INTEGER),
    
    /** Long integer valued type */
    LONG(Long.class, DpSupportedType.LONG),
    
    /** Floating point value type */
    FLOAT(Float.class, DpSupportedType.FLOAT),
    
    /** Double-valued floating point type */
    DOUBLE(Double.class, DpSupportedType.DOUBLE),
    
    /** String valued type */
    STRING(String.class, DpSupportedType.STRING);
    
    
    //
    // Constant Attributes
    /** Java class type for enumeration constant */
    private final Class<?>          clsJavaType;
    
    /** The Data Platform supported type for enumeration constant */
    private final DpSupportedType   enmDpType;
    
    //
    // Constant Constructor
    //
    
    /** Constructor */
    private JalScalarType(Class<?> clsType, DpSupportedType enmDpType) { 
        this.clsJavaType = clsType;
        this.enmDpType = enmDpType;
    };
    

    //
    // Constant Operations
    //
    
    /** 
     * Get the Java class type corresponding to this enumeration constant.
     * 
     * return the Java <code>Class</code> for the scalar type represented by this constant
     */
    public Class<?>         getJavaType() { return this.clsJavaType; };  
    
    /** 
     * Get the Data Platform supported type corresponding to this enumeration constant.
     * 
     * @return the Data Platform supported type represented by this scalar 
     */
    public DpSupportedType  getDpType() { return this.enmDpType; };
    
    /** 
     * Check if the given Java class is consistent with this enumeration constant (i.e., is assignable from) 
     * 
     * @return <code>true</code> if the given class type can be cast to the scalar type represented by this constant,
     *         <code>false</code> otherwise
     */
    public boolean  isAssignableFrom(Class<?> cls)  { return this.clsJavaType.isAssignableFrom(cls); };
    
    /** 
     * Check if the given object is assignable to the scalar type represented by this enumeration constant.
     *
     * @return <code>true</code> if the given object can be cast to the type represented by this constant,
     *         <code>false</code> otherwise
     */
    public boolean  isAssignable(Object objVal)     { return this.clsJavaType.isAssignableFrom(objVal.getClass()); };
}
