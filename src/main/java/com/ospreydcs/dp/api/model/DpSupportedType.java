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

//    BOOLEAN(boolean.class),
//    INTEGER(int.class),
//    LONG(long.class),
//    FLOAT(float.class),
//    DOUBLE(double.class),
//    STRING(String.class),
//    BYTE_ARRAY(byte[].class),
//    IMAGE(BufferedImage.class),
//    ARRAY(Vector.class),
//    STRUCTURE(Map.class),
//    UNSUPPORTED_TYPE(null)
    BOOLEAN(Boolean.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    STRING(String.class),
    BYTE_ARRAY(ByteString.class),
    IMAGE(BufferedImage.class),
    ARRAY(Vector.class),
    STRUCTURE(Map.class),
    UNSUPPORTED_TYPE(null)
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
    // Enumeration Class Methods
    //
    
    /**
     * Returns the enumeration constant representing a type compatible with the argument.
     * <p>
     * Returns the constant whose supported type can be assigned value from the given type.
     * 
     * @param <T>       the type under compatibility test
     * 
     * @param clsTarget the <code>Class</code> object representing type <code>T</code>
     * 
     * @return  enumeration constant whose type is compatible with the argument,
     *          or <code>{@link #UNSUPPORTED_TYPE}</code> if <code>T</code> is unsupported
     */
    public static <T extends Object> DpSupportedType   getAssignable(Class<T> clsTarget) {
        for (DpSupportedType enmType : DpSupportedType.values()) {
            if (enmType.isAssignableFrom(clsTarget))
                return enmType;
        }
        
        return DpSupportedType.UNSUPPORTED_TYPE;
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
    
    /**
     * Returns whether or not this type is consider a (numeric) scalar value.
     * 
     * @return <code>true</code> if <code>INTEGER || LONG || FLOAT || DOUBLE</code>
     */
    public boolean  isScalar() {
        if (this == INTEGER || this == LONG || this == FLOAT || this == DOUBLE)
            return true;
        
        return false;
    }
    
    /**
     * Returns whether or not this type is compatible with the given type.
     * <p>
     * Specifically, returns <code>true</code> only if objects of this type can be assigned
     * values with objects of type <code>T</code>.
     *  
     * @param <T>       The type under compatibility test
     * 
     * @param clsCmp    the <code>Class</code> object representing type <code>T</code>
     * 
     * @return  <code>true</code> if objects of this supported type can be assigned values from type <code>T</code>,
     *          <code>false</code> otherwise
     */
    public <T extends Object>   boolean isAssignableFrom(Class<T> clsCmp) {
        return this.clsType.isAssignableFrom(clsCmp);
    }
    
    /**
     * Returns whether or not objects of this supported type can be assigned by the given argument.
     * <p>
     * Returns <code>true</code> if the argument can be used to assign a value to this supported
     * type.  More exactly, whether type <code>T</code> is compatible with this supported type.
     *  
     * @param <T>       The type under compatibility test
     * 
     * @param val       sample value of type <code>T</code>
     * 
     * @return  <code>true</code> if this supported type can be assigned by the given argument
     */
    public <T extends Object>   boolean isAssignableFrom(T val) {
        return this.isAssignableFrom( val.getClass() );
    }
}
