/*
 * Project: dp-data-simulator
 * File:	JalComplexType.java
 * Package: com.ospreydcs.dp.datasim.model
 * Type: 	JalComplexType
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
package com.ospreydcs.dp.jal.tools.common.datagen;

import com.ospreydcs.dp.jal.common.DpSupportedType;

/**
 * <p>
 * Enumeration of JAL recognized complex heterogeneous data types.
 * </p>
 * <p>
 * The supported data value types enumerated here are for identification of complex 
 * data types.  The composite fields or elements of the complex data type may then allow further
 * type identification with the <code>{@link JalScalarType}</code> enumeration.  
 * </p>
 * <p>
 * The constant <code>{@link #SCALAR}</code> is included to indicate a scalar-value data type, that is,
 * rather than a complex type.
 * Again, the enumeration <code>{@link JalScalarType}</code> can then be used to further clarify
 * the exact data type of the scalar.
 * </p>
 * <p>
 * Note that the <code>{@link DpSupportType}</code> constant associated with each <code>JalComplexType</code>
 * constant is available with method <code>{@link #getDpType()}</code>.
 * </p> 
 * 
 * @author Christopher K. Allen
 * @since Jun 13, 2024
 *
 */
public enum JalComplexType {
    
    /** 
     * Scalar data type
     * <p>
     * A value representable by a primitive type.  
     */
    SCALAR(DpSupportedType.UNSUPPORTED_TYPE),
    
    /** 
     * Byte array type
     * <p>
     * Format is a bytes array <code>byte[]</code>
     */
    BYTES(DpSupportedType.BYTE_ARRAY),
    
    /** 
     * Timestamp data type
     * <p>
     * Format is <code>Instant</code> = (seconds, nanosecond offset).
     */
    TIMESTAMP(DpSupportedType.TIMESTAMP),
    
    /** 
     * Image data type
     * <p>
     * Format is <code>BufferedImage</code> 
     */
    IMAGE(DpSupportedType.IMAGE),
    
    /** 
     * Multi-dimensional array (tensor) type
     * <p>
     * Format is <code>ArrayList&lt;ArrayList&lt; ...ArrayList&lt;Scalar&gt; ...&gt;&gt; 
     */
    TENSOR(DpSupportedType.ARRAY),
    
    /** 
     * Complex data structure type
     * <p>
     * Format is <code>Map&ltString, Object&gt;</code> representing (name, value) where 
     * 'name' is field name and
     * 'value' is field value, potentially other structures or complex data types.
     */
    STRUCTURE(DpSupportedType.STRUCTURE),
    
    ;

    
    //
    // Constant Attributes
    //
    
    /** The Data Platform supported type for this type */
    private final DpSupportedType   enmDpType;
    
    
    //
    // Constant Constructors
    //
    
    /** Enumeration constant constructor */
    private JalComplexType(DpSupportedType enmDpType) { this.enmDpType = enmDpType; };
    
    
    //
    // Operations
    //
    
    /** Returns the Data Platform supported data type for this constant */
    public DpSupportedType  getDpType() { return this.enmDpType; };
    
    /**
     * <p>
     * Returns the <code>JalComplexType</code> enumeration constant with the given name.
     * </p>
     * <p>
     * This a a convenience method that simply calls the method <code>{@link Enum#valueOf(Class, String)}</code>
     * with first argument given by <code>JalComplexType.class</code> and the second argument given by
     * the argument of this method.  Any exception thrown is caught an returned as a 
     * <code>{@link TypeNotPresentException}</code>.
     * </p>
     * 
     * @param strName   name of the <code>JalComplexType</code> enumeration constant
     * 
     * @return  the <code>JalComplexType</code> constant with the given name
     * 
     * @throws TypeNotPresentException  the name was invalid
     */
    public static JalComplexType    valueFrom(String strName) throws TypeNotPresentException {
        
        try {
            JalComplexType   enmConstant = JalComplexType.valueOf(JalComplexType.class, strName);
            return enmConstant;
            
        } catch (Exception e) {
            throw new TypeNotPresentException(strName, e);
        }
    }
    
}
