/*
 * Project: dp-api-common
 * File:	CfgStructure.java
 * Package: com.ospreydcs.dp.api.config.model
 * Type: 	CfgStructure
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
 * @since Jan 2, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <p>
 * Base class for structure classes containing configuration parameters.
 * </p>
 * <p>
 * This base class is essentially just a convenience for providing the Java <code>Object</code>
 * method overrides appropriate for a structure class.  In particular, the {@link #equals(Object)}
 * method used for comparisons.
 * </p>
 * <p>
 * <h2>USE:</h2>
 * <ul>
 * <li>
 * Declare the derived structure class with its class type as the generic parameter <code>T</code>.
 * </li> 
 * <li>
 * Derived types should implement a default (zero-argument) constructor which calls constructor
 * {@link #CfgStructure(Class)} supplying it with the derived class type.  This is required for
 * Java reflection operations.
 * </li>
 * </ul>
 * </p>
 * 
 * @param   <T> type of the derived structure class 
 *
 * @author Christopher K. Allen
 * @since Jan 2, 2024
 *
 */
public abstract class CfgStructure <T extends CfgStructure<T>> {
    
    //
    // Class Resources
    //
    
    /** The static logging utility */
    private static final Logger LOGGER = LogManager.getLogger();

    
    //
    // Instance Attributes
    //
    
    /** The derived class type given at construction */
    private final Class<T>   clsChldType; 
    
    /** Class name (type name) of the derived type */ 
    private final String     strChldName;
    
    /** this object recast as derived type T */
    private final T          This;
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>CfgStructure</code>.
     * </p>
     * <p>
     * Creates empty objects for each structure class field value.
     * Thus, default constructed instances of the derived class will never have 
     * <code>null</code> valued fields and can be assigned by hand.
     * </p>
     */
    public CfgStructure(Class<T> clsType) {
        
        // Get the type and name of the derived structure class, and recast
        this.clsChldType = clsType;
        this.strChldName = clsType.getCanonicalName();
        this.This = clsType.cast(this);

        // Set each field to an empty object
        for (Field fld : this.clsChldType.getFields()) {
            
            // Skip fields of this class
            Class<?>    clsOwner = fld.getDeclaringClass();
            if (clsOwner.equals(CfgStructure.class))
                continue;
            
            // Skip field if it is a primitive
            Class<?>    clsFldType = fld.getType();
            if (clsFldType.isPrimitive())
                continue;
            
            String  strFldName = fld.getName();
            String  strFldType = fld.getType().getName();
            
            try {

                // Create empty field instance
                Constructor<?> ctor = fld.getType().getConstructor();

                Object obj = ctor.newInstance();
                
                // Set structure class field value to empty object
                fld.set(This, obj);

            } catch (NoSuchMethodException e) {
                LOGGER.info("No default constructor for field '{}' with type {} in structure class {}", strFldName, strFldType, this.strChldName);

            } catch (SecurityException e) {
                LOGGER.error("Default constructor not accessible for field '{}' with type {} in structure class {}", strFldName, strFldType, this.strChldName);

            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                LOGGER.error("Default constructor invocation error for field '{}' with type {} in structure class {}", strFldName, strFldType, this.strChldName);
            }

        }
    }

    
    //
    // Object Overrides
    //
    
    /**
     * @see @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        // Check argument type then cast comparision object
        if (!this.clsChldType.equals(obj.getClass())) {
            LOGGER.warn(this.strChldName + ".equals(Object): argument is not of type {}", this.strChldName);
            return false;
        }
        T cmp = this.clsChldType.cast(obj);
        
        // Check equivalence of argument field values
        boolean bolResult = true;
        
        for (Field fld : this.clsChldType.getFields()) {

            // Can skip fields of the base class
            Class<?>    clsOwner = fld.getDeclaringClass();
            if (clsOwner.equals(CfgStructure.class))
                continue;
            
            String  strFldName = fld.getName();

            try {
                Object  objFldThis = fld.get(this);
                Object  objFldCmp = fld.get(cmp);
                
                if ( !objFldThis.equals(objFldCmp) ) {
                    LOGGER.warn("{}.equals(Object) FALSE for field {}", this.strChldName, strFldName);
                    bolResult = false;
                }
                
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                LOGGER.warn("{}.equals(Object) field {} access exception {}", this.strChldName, strFldName, e.getMessage());
                bolResult = false;
            }
        }
            
        return bolResult;
    }


    /**
     * @see @see java.lang.Object#toString()
     */
    @Override
    public String toString() { 
        StringBuffer    buf = new StringBuffer(this.clsChldType.getSimpleName() + ":{");
        
        boolean         bolFirst = true;
        for (Field fld : this.clsChldType.getFields()) {
            if (!bolFirst)
                buf.append(", ");
            else
                bolFirst = false;
        
            try {
                buf.append(fld.getName());
                buf.append("=");
                buf.append(fld.get(This).toString());
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOGGER.warn("{}.toString() field {} access exception {}", this.strChldName, fld.getName(), e.getMessage());
            }
        }
        
        buf.append("}");

        return buf.toString();
    }
}
