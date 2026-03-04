/*
 * Project: dp-api-common
 * File:    TestRequestType.java
 * Package: com.ospreydcs.dp.jal.tools.common.requests
 * Type:    TestRequestType
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
 * @since May 6, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.requests;

import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Enumeration of all time-series data request types available from the utility class 
 * <code>TestArchiveRequestCreator</code>.
 * </p>
 */
public enum TestRequestType {
    
    /**
     * Empty time-series data request (requests no data).
     */
    EMPTY("Emtpy time-series data request."),
    
    /**
     * General time-series data request for data sources using sampling clock and/or explicit timestamp list.
     */
    GENERAL("General time-series data request; sample clock or timestamp list not explicitely specified."),
    
    /**
     * Time-series data request for data sources using a uniform sampling clock only. 
     */
    CLOCKED("Time-series data request with timestamps specified by a sampling clock."),
    
    /**
     * Time-series data request for data sources using an explicit timestamp list only. 
     */
    TMS_LIST("Time-series data request with timestamps specified by an eplicit list."),
    
    /**
     * The request contains data sources with both uniform sampling clocks <em>and</em> explicit timestamp lists.
     */
    BOTH("Time-series data request containing timestamps specified by both sampling clock(s) and timestamp lists(s)."),
    ;
    
    
    //
    // Constant Attributes
    //
    
    /** String description of the constant */
    private final String        strDesc;
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>TestRequestType</code> constant.
     * </p>
     *
     * @param strDesc   text description of the constant
     */
    private TestRequestType(String strDesc) {
        this.strDesc = strDesc;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Returns the <code>TestRequestType</code> enumeration constant with the given name.
     * </p>
     * <p>
     * This is a convenience method which defers to <code>{@link Enum#valueOf(Class, String)}</code> by
     * supplying the first argument with the class type of this enumeration.  Any exception is caught
     * and returned as a <code>{@link TypeNotPresentException}</code> which includes the originating 
     * exception as the cause.
     * </p>
     * 
     * @param strName   name of the enumeration constant
     * 
     * @return  the <code>FrameProcTestParams</code> constant with the given name
     * 
     * @throws TypeNotPresentException  the argument was <code>null</code> or an invalid enumeration constant name  
     */
    public TestRequestType  valueFrom(String strName) throws TypeNotPresentException {
     
        try {
            TestRequestType enmType = TestRequestType.valueOf(TestRequestType.class, strName);
            
            return enmType;
            
        } catch (Exception e) {
            throw new TypeNotPresentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Invalid constant name: " + strName, e);
        }
    }
    
    /**
     * <p>
     * Returns the string describing the context for this enumeration constant.
     * </p>
     * 
     * @return  the text description for this enumeration constant
     */
    public String   getDescription() {
        return this.strDesc;
    }
}