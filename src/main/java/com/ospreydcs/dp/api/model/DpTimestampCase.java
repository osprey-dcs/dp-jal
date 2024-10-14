/*
 * Project: dp-api-common
 * File:	DpTimestampCase.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	DpTimestampCase
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
 * @since Oct 7, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model;

import java.awt.List;

import com.ospreydcs.dp.api.grpc.util.ProtoMsg;

/**
 * <h1>Enumeration of the Data Platform supported data timestamp mechanisms</h1>
 * <p>
 * Enumeration constants refer to the method of storing timestamps for a given data source (i.e., PV).  The
 * Data Platform currently supports two mechanisms, timestamp lists and sampling clocks.  Timestamp lists contain
 * and explicit, ordered list of timestamps for all data samples.  Sampling clocks assumed uniform sampling at
 * a constant period over a finite duration starting at a given time instant. 
 * </p>
 * <p>
 * <h2>Type Bindings</h2>
 * All Data Platform Protobuf types are hidden and not referenced here, only Java types supported within
 * the client library appear.  All bindings between Java Data Platform native types (i.e., Protobuf messages
 * and enumerations) are localized and performed in utility class <code>{@link ProtoMsg}</code>.
 * </p>
 * <p>
 * <h2>Type Mappings</h2>
 * The current Protobuf type to Java object mappings are listed below:
 * <br/>
 * <code>
 * <pre>
 *    <code>TimestampList</code> -> <code>java.util.List&lt;Instant&gt;</code> 
 *    <code>SamplingClock</code> -> <code>com.ospreydcs.dp.api.model.UniformSamplingClock</code>
 * </pre>
 * </code>
 * </p>
 * <p>
 * <h2>TODO:</h2>
 * Consider changing the <code>List</code> representation to a Java <code>ArrayList</code>, essentially a vector.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Oct 7, 2024
 *
 */
public enum DpTimestampCase {
    
    /**
     * Data timestamps are explicitly given as an ordered list of Java <code>{@link Instant}</code> objects.
     * <p>
     * <code>TimestampList</code> -> <code>java.util.List&lt;Instant&gt;</code>
     */
    TIMESTAMP_LIST(List.class),
    
    /**
     * Data timestamps are described by a uniform sampling clock <code>{@link UniformSamplingClock}</code> object with
     * start time, sampling period, and sample count.
     * <p>
     * <code>SamplingClock</code> -> <code>com.ospreydcs.dp.api.model.UniformSamplingClock</code>
     */
    SAMPLING_CLOCK(UniformSamplingClock.class),
    
    /**
     * Enumeration for unsupported or unrecognized timestamp case.
     * <p>
     * Indicates an exception condition.
     */
    UNSUPPORTED_CASE(null);
    
    
    //
    // Attributes
    //
    
    /** Java class type used to represent data value instances */
    private final Class<? extends Object>       clsType;
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs each enumeration instance of <code>DpSupportedType</code>.
     * </p>
     *
     * @param clsType   Java class type used to represent data value instances 
     */
    private DpTimestampCase(Class<? extends Object> clsType) {
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
     *          or <code>{@link #UNSUPPORTED_CASE}</code> if <code>T</code> is unsupported
     */
    public static <T extends Object> DpTimestampCase   getAssignable(Class<T> clsTarget) {
        for (DpTimestampCase enmType : DpTimestampCase.values()) {
            if (enmType.isAssignableFrom(clsTarget))
                return enmType;
        }
        
        return DpTimestampCase.UNSUPPORTED_CASE;
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
