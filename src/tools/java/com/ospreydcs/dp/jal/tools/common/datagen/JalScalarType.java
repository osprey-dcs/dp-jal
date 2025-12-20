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
package com.ospreydcs.dp.jal.tools.common.datagen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * Enumeration of Java scalar values supported for simulated data value generation.
 *
 * @author Christopher K. Allen
 * @since May 9, 2024
 *
 */
public enum JalScalarType {

    /** Boolean value type */
    BOOLEAN(Boolean.class, DpSupportedType.BOOLEAN, false),
    
    /** Integer valued type */
    INTEGER(Integer.class, DpSupportedType.INTEGER, true),
    
    /** Long integer valued type */
    LONG(Long.class, DpSupportedType.LONG, true),
    
    /** Floating point value type */
    FLOAT(Float.class, DpSupportedType.FLOAT, true),
    
    /** Double-valued floating point type */
    DOUBLE(Double.class, DpSupportedType.DOUBLE, true),
    
    /** String valued type */
    STRING(String.class, DpSupportedType.STRING, false),
    
    
    /** Error condition */
    UNSUPPORTED(null, DpSupportedType.UNSUPPORTED_TYPE, false),
    
    ;
    
    
    //
    // Constant Attributes
    //
    
    /** Java class type for enumeration constant */
    private final Class<?>          clsJavaType;
    
    /** The Data Platform supported type for enumeration constant */
    private final DpSupportedType   enmDpType;
    
    /** Is numeric type flag */
    private final boolean           bolNumeric;
    
    //
    // Constant Constructor
    //
    
    /** Constructor */
    private JalScalarType(Class<?> clsType, DpSupportedType enmDpType, boolean bolNumeric) { 
        this.clsJavaType = clsType;
        this.enmDpType = enmDpType;
        this.bolNumeric = bolNumeric;
    };
    

    //
    // Constant Operations
    //
    
    /** 
     * Get the Java class type corresponding to this enumeration constant.
     * 
     * return the Java <code>Class</code> for the scalar type represented by this constant, or <code>null</code> if <code>UNSUPPORTED</code>
     */
    public Class<?>         getJavaType() { return this.clsJavaType; };  
    
    /** 
     * Get the Data Platform supported type corresponding to this enumeration constant.
     * 
     * @return the Data Platform supported type represented by this scalar 
     */
    public DpSupportedType  getDpType() { return this.enmDpType; };
    
    /**
     * Determines whether or not this scalar type represents a numeric type (e.g., integer, float, double, etc.).
     * 
     * @return  <code>true</code> if the type is numeric, <code>false</code> otherwise
     */
    public boolean  isNumeric() { return this.bolNumeric; };
    
    /** 
     * Check if the given Java class is consistent with this enumeration constant (i.e., is assignable from) 
     * 
     * @return <code>true</code> if the given class type can be cast to the scalar type represented by this constant,
     *         <code>false</code> otherwise
     */
    public boolean  isAssignableFrom(Class<?> cls)  { 
        // Check for UNSUPPORTED
        if (this == UNSUPPORTED)
            return false;
        
        return this.clsJavaType.isAssignableFrom(cls); 
    };
    
    /** 
     * Check if the given object is assignable to the scalar type represented by this enumeration constant.
     *
     * @return <code>true</code> if the given object can be cast to the type represented by this constant,
     *         <code>false</code> otherwise
     */
    public boolean  isAssignable(Object objVal)     {
        // Check for UNSUPPORTED
        if (this == UNSUPPORTED)
            return false;
        
        return this.clsJavaType.isAssignableFrom(objVal.getClass()); 
    };
    
    /**
     * <p>
     * Parse the string argument and convert it to an object of the proper type for the constant.
     * </p>
     * <p>
     * If the constant represents a numeric type (i.e., any constant exception <code>{@link #STRING}</code>)
     * it is converted to the appropriate Java numeric type using the <code>valueOf(String)</code> method 
     * using reflection.  If the constant is of type <code>{@link #STRING}</code> the argument simply passes
     * through.
     * </p>
     * 
     * @param strValue  typically a string representation of a numeric type, or any string if <code>this</code> is <code>{@link #STRING}</code>
     * 
     * @return  the Java numeric type after parsing and conversion, or the argument itself if <code>this</code> is <code>{@link #STRING}</code>
     * 
     * @throws UnsupportedOperationException    the type constant is <code>{@link #UNSUPPORTED}</code> and cannot be parsed
     * @throws NoSuchMethodException    the Java class <code>{@link #getJavaType()}</code> does not contain method <code>valueOf(String)</code>
     * @throws SecurityException        the class loader denied access to method <code>valueOf(String)</code> (e.g., typically package access)
     * @throws IllegalAccessException   the method <code>valueOf(String)</code> is not accessible
     * @throws InvocationTargetException    the <code>valueOf(String)</code> method threw an exception (e.g., NumberFormatException)
     */
    public Object   parseValue(String strValue) 
            throws UnsupportedOperationException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException 
    {
        // Check if UNSUPPORTED
        if (this == UNSUPPORTED)
            throw new UnsupportedOperationException(JavaRuntime.getQualifiedMethodNameSimple() + " - Type " + UNSUPPORTED + " cannot be parsed.");
        
        // Check if type is already a string
        if (this == STRING)
            return strValue;
        
        // Parse the string using the 'valueOf(String)' method for each numeric class
        Method mthValue = this.clsJavaType.getMethod("valueOf", String.class);  // throws NoSuchMethodException, SecurityException
        Object objValue = mthValue.invoke(null, strValue);                      // throws IllegalAccessException, InvocationTargetException

        return objValue;
    }

    /**
     * <p>
     * Returns the <code>JalScalarType</code> enumeration constant with the given name.
     * </p>
     * <p>
     * This a a convenience method that simply calls the method <code>{@link Enum#valueOf(Class, String)}</code>
     * with first argument given by <code>JalScalarType.class</code> and the second argument given by
     * the argument of this method.  Any exception thrown is caught an returned as a 
     * <code>{@link TypeNotPresentException}</code>.
     * </p>
     * 
     * @param strName   name of the <code>JalScalarType</code> enumeration constant
     * 
     * @return  the <code>JalScalarType</code> constant with the given name
     * 
     * @throws TypeNotPresentException  the name was invalid
     */
    public static JalScalarType    valueFrom(String strName) throws TypeNotPresentException {
        
        try {
            JalScalarType   enmConstant = JalScalarType.valueOf(JalScalarType.class, strName);
            return enmConstant;
            
        } catch (Exception e) {
            throw new TypeNotPresentException(strName, e);
        }
    }
    
}
