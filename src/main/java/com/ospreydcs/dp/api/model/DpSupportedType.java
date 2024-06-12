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

import java.time.Instant;
import java.util.Map;
import java.util.Vector;

import com.google.protobuf.ByteString;
import com.ospreydcs.dp.api.common.AUnavailable;
import com.ospreydcs.dp.api.common.AUnavailable.STATUS;

/**
 * <p>
 * Enumeration of data types supported as heterogeneous <em>data values</em> within the Data Platform.
 * </p>
 * <p>
 * The mapping of Protocol Buffers ("Protobuf") data types to Java data types is NOT one-to-one.  Protobuf
 * has more <em>primitive</em> types than Java, particularly the unsigned integral values.  Moreover,
 * structured data defined with Protobuf message (e.g., <code>Array</code>, <code>Structure</code>, 
 * <code>Image</code>) require special handling within Java.  These types are handled with either specially 
 * formatted Java containers (e.g., <code>Array</code> and <code>Structure</code>) or with classes defined 
 * within this library (e.g., <code>Image</code>).
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>All <em>unsigned</em> Protobuf data types are represented as their signed equivalents.</li>
 * <li>The <code>bytes</code> Protobuf primitive is represented by the Google class <code>com.google.Protobuf.ByteString</code> class.</li>
 * <li>The <code>Array</code> Protobuf message is currently represented as a Java <code>Vector</code>. 
 * <li>The <code>Structure</code> Protobuf message is represented as a Java <code>Map</code>.</li>
 * <li>The <code>Image</code> Protobuf message is represented with the DP API class <code>BufferedImage</code>.</li>
 * </ul>
 * </p>
 * <p>
 * <h2>Mappings</h2>
 * The current Protobuf type to Java object mappings are listed below:
 * <br/>
 * <code>
 * <pre>
 *    <code>bool</code> -> <code>java.lang.Boolean</code> 
 *    <code>uint32</code> -> <code>java.lang.Integer</code>
 *    <code>sint32</code> -> <code>java.lang.Integer</code>
 *    <s><code>int32</code> -> <code>java.lang.Integer</code></s> (not used)
 *    <code>uint64</code> -> <code>java.lang.Long</code>
 *    <code>sint64</code> -> <code>java.lang.Long</code>
 *    <s><code>int64</code> -> <code>java.lang.Long</code></s> (not used) 
 *    <code>float</code> -> <code>java.lang.Float</code>
 *    <code>double</code> -> <code>java.lang.Double</code>
 *    <code>string</code> -> <code>Java.lang.String</code> 
 *    <code>bytes</code> -> <code>com.google.Protobuf.ByteString</code>
 *    <code>Array</code> -> <code>java.util.Vector&lt;Object&gt;</code>
 *    <code>Structure</code> -> <code>java.util.Map&lt;Object&gt;</code>
 *    <code>Image</code> -> <code>com.ospreydcs.dp.api.common.BufferedImage</code>
 *    <code>Timestamp</code> -> <code>java.time.Instant</code>
 * </pre>
 * </code>
 * </p>
 * <p>
 * <h2>TODO:</h2>
 * Consider changing the <code>Array</code> representation to a Java <code>ArrayList</code>.
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
    
    /**
     * The Boolean type {<code>false</code>, <code>true</code>} represented as <code>{@link Boolean}</code>.
     * <p>
     * <code>bool</code> -> <code>java.lang.Boolean</code> 
     */
    BOOLEAN(Boolean.class),
    
    /**
     * The 32-bit integer type.
     * <p>
     * <code>uint32</code> -> <code>java.lang.Integer</code><br/>
     * <code>sint32</code> -> <code>java.lang.Integer</code><br/>
     * <s><code>int32</code> -> <code>java.lang.Integer</code></s> (not used)
     */
    INTEGER(Integer.class),
    
    /**
     * The 64-bit integer type. 
     * <p>
     * <code>uint64</code> -> <code>java.lang.Long</code><br/>
     * <code>sint64</code> -> <code>java.lang.Long</code><br/>
     * <s><code>int64</code> -> <code>java.lang.Long</code></s> (not used) 
     */
    LONG(Long.class),
    
    /**
     * The 32-bit floating point type.
     * <p>
     * <code>float</code> -> <code>java.lang.Float</code>
     */
    FLOAT(Float.class),
    
    /**
     * The 64-bit floating point type (double value).
     * <p>
     * <code>double</code> -> <code>java.lang.Double</code>
     */
    DOUBLE(Double.class),
    
    /**
     * Character string type.
     * <p>
     * <code>string</code> -> <code>Java.lang.String</code> 
     */
    STRING(String.class),
    
    /**
     * Raw data type, that is, a string of <code>byte</code> values.
     * <p>
     * <code>bytes</code> -> <code>com.google.Protobuf.ByteString</code>
     */
    BYTE_ARRAY(ByteString.class),
    
    /**
     * Image data type.
     * <p>
     * <code>Image</code> -> <code>com.ospreydcs.dp.api.common.BufferedImage</code>
     */
    @AUnavailable(status=STATUS.ACCEPTED, note="Currently unimplemented within dp-services")
    IMAGE(BufferedImage.class),
    
    /**
     * Array data type (of <code>DataValue</code> messages).
     * <p>
     * <code>Array</code> -> <code>java.util.Vector&lt;Object&gt;</code>
     */
    @AUnavailable(status=STATUS.ACCEPTED, note="Currently unimplemented withiin dp-services")
    ARRAY(Vector.class),
    
    /**
     * Data structure type (contains fields of <code>Structure.Field</code> messages).
     * <p>
     * <code>Structure</code> -> <code>java.util.Map&lt;Object&gt;</code>
     */
    @AUnavailable(status=STATUS.ACCEPTED, note="Currently unimplemented within dp-services")
    STRUCTURE(Map.class),
    
    /**
     * Timestamp of the form (Epoch seconds, nanoseconds offset).
     * <p>
     * <code>Timestamp</code> -> <code>java.time.Instant</code>
     */
    TIMESTAMP(Instant.class),
    
    /**
     * Enumeration for unsupported or unrecognized data types.
     * <p>
     * Indicates an exception condition.
     */
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
    public Class<? extends Object>  getJavaType() {
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
     * Returns whether or not this type is compatible (assignable) with the given type.
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
