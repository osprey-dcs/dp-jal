/*
 * Project: dp-jal
 * File:	ListTestRequestOptions.java
 * Package: com.ospreydcs.dp.jal.tools.apps.query.listrqst
 * Type: 	ListTestRequestOptions
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
 * @since Mar 3, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.query.listrqst;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.MalformedParametersException;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.tools.appfwk.ITestParameter;
import com.ospreydcs.dp.jal.tools.common.requests.TestArchiveRequest;

/**
 * <p>
 * Enumeration of the <code>ListTestArchiveRequests</code> application command-line options.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 3, 2026
 *
 */
public enum ListTestRequestOptions implements ITestParameter<ListTestRequestOptions> {
    
    /** 
     * Switch for displaying all request names in the <code>TestArchiveRequest</code> enumeration. 
     */
    ALL("List all TestArchiveRequest enumeration constants", "-all", Boolean.class, false),
    
    /** 
     * Display all request name(s) having name matching the given regular expression(s) 
     */
    REGEX("List all TestArchiveRequest constant with name(s) matching regular expression(s)", "--regex", String.class, null),
    
    /** 
     * Display the properties of specific <code>TestArchiveRequest</code> with given constant name(s) 
     */
    REQUEST("List properties of named request", "--rqst", TestArchiveRequest.class, TestArchiveRequest.EMPTY_REQUEST),
    ;

    
    //
    //  Constant Attributes
    //
    
    /** The parameter description */
    private final String    strOptionDesc;
    
    /** The command-line delimited option name containing */
    private final String    strDelOption;
    
    /** The parameter type */
    private final Class<?>  clsOptionType;
    
    /** The parameter default value */
    private final Object    objValueDef;
    
    
    //
    // Constant Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>FrameProcTestParams</code> instance.
     * </p>
     *
     * @param strOptionDesc  the parameter string description
     * @param strDelOption   the delimited variable name used to identify parameter values on the command-line
     * @param clsOptionType  the parameter class type
     * @param objValueDef   the parameter default value
     */
    private ListTestRequestOptions(String strOptionDesc, String strDelOption, Class<?> clsOptionType, Object objValueDef) {
        this.strOptionDesc = strOptionDesc;
        this.strDelOption = strDelOption;
        this.clsOptionType = clsOptionType;
        this.objValueDef = objValueDef;
    }

    
    //
    // Enumeration Operations
    //
    
    /**
     * <p>
     * Returns the <code>ListTestRequestOptions</code> enumeration constant with the given name.
     * </p>
     * <p>
     * Defers to <code>{@link ITestParameter#valueFrom(Class, String)}</code> with the <code>ListTestRequestOptions</code>
     * class object and given name.
     * </p>
     * 
     * @param strName   name of the enumeration constant
     * 
     * @return  the enumeration constant with the given name 
     * 
     * @throws NoSuchElementException   invalid constant name
     * 
     * @see ITestParameter#valueFrom(Class, String)
     */
    public static ListTestRequestOptions valueFrom(String strName) throws NoSuchElementException {
        return ITestParameter.valueFrom(ListTestRequestOptions.class, strName);
    }
    
    /**
     * <p>
     * Prints out a line-by-line description of this enumeration.
     * </p>
     * <p>
     * Defers to <code>{@link ITestParameter#printOut(Class, PrintStream, String)}</code> with the 
     * <code>ListTestRequestOptions</code> class object and given arguments.
     * </p>
     * 
     * @param ps        output stream to receive text description of record fields
     * @param strPad    white space padding for left-hand side line headings (or <code>null</code>.
     * 
     * @throws ClassCastException       enumeration type does not implement <code>ITestParameter</code> interface
     * @throws NoSuchElementException   a maximum value could not be found for the constant name, delimited variable name, or class type name
     * 
     * @see ITestParameter#printOut(Class, PrintStream, String)
     */
    public static void  printOut(PrintStream ps, String strPad) throws ClassCastException, NoSuchElementException {
        ITestParameter.printOut(ListTestRequestOptions.class, ps, strPad);
    }
    
    /**
     * <p>
     * Returns an ordered list of all valid, delimited command-line options according to the enumeration.
     * </p>
     * <p>
     * Defers to <code>{@link ITestParameter#validDelimOptions(Class)}</code> with the 
     * <code>ListTestRequestOptions</code> class object.
     * 
     * @return  the ordered list of all valid delimited command-line options for the enumeration 
     * 
     * @throws ClassCastException       enumeration type does not implement <code>ITestParameter</code> interface
     * 
     * @see ITestParameter#validDelimOptions(Class)
     */
    public static List<String>  validDelimOptions() throws ClassCastException {
        return ITestParameter.validDelimOptions(ListTestRequestOptions.class);
    }
    
    
    //
    // ITestParameter Interface
    //
    
    /**
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getParameterConstant()
     */
    @Override
    public ListTestRequestOptions getParameterConstant() {
        return this;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getParameterDescription()
     */
    @Override
    public String getParameterDescription() {
        return this.strOptionDesc;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getParameterType()
     */
    @Override
    public Class<?> getParameterType() {
        return this.clsOptionType;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getParameterDelimOption()
     */
    @Override
    public String getParameterDelimOption() {
        return this.strDelOption;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getDefaultValue()
     */
    @Override
    public Object getDefaultValue() {
        return this.objValueDef;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#parseValue(java.lang.String)
     */
    @Override
    public Object parseValue(String strValue) throws NoSuchElementException, DateTimeParseException, NumberFormatException, UnsupportedOperationException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, IllegalArgumentException, TypeNotPresentException, ConfigurationException, MalformedParametersException {
        
        // Check for special case
        if (this == REQUEST) { 
            TestArchiveRequest  enmRqst = TestArchiveRequest.valueFrom(strValue);   // throws NoSuchElementException

            return enmRqst;
        }
        
        // Else defer to super class (interface) implementation
        return ITestParameter.super.parseValue(strValue);
    }
}
