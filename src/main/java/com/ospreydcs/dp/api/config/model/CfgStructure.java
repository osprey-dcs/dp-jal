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
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.util.JavaRuntime;


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
 * The base class is part of the Data Platform API configuration mechanism used to define
 * structure classes holding configuration parameters.  This class is typically used in 
 * conjunction with the sub-annotations of <code>{@link ACfgOverride}</code> for defining 
 * system variables capable of overriding configuration parameters.
 * The resulting structure class is capable of be created and populated by the
 * <code>{@link CfgLoaderYaml}</code> utility class, and overridden by the 
 * <code>{@link CfgOverrideUility}</code> utility class.
 * <p>
 * <h2>USE:</h2>
 * <ul>
 * <li>
 * Declare the derived structure class with its class type as the generic parameter <code>T</code>.
 * </li>
 * <br/> 
 * <li>
 * Derived types should implement a default (zero-argument) constructor which calls constructor
 * {@link #CfgStructure(Class)} supplying it with the derived class type.  This is required for
 * Java reflection operations.
 * </li>
 * <br/>
 * <li>
 * Structure class fields to be used as configuration parameters should be declared <code>public</code>.
 * (And optionally annotated with <code>{@link ACfgOverride}</code> sub-annotation.)
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
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * We do NOT instantiate static structure class fields.  They are assumed to
     * be handled by the class itself.
     * </li>
     * <br/>
     * <li>
     * Private fields are not used as configuration parameters and, consequently, we
     * do not create them here.
     * </li>
     * </ul>
     * </p>
     */
    public CfgStructure(Class<T> clsType) {
        
        // Get the type and name of the derived structure class, and recast
        this.clsChldType = clsType;
        this.strChldName = clsType.getCanonicalName();
        this.This = clsType.cast(this);

        // Set each field to a newly instantiated empty object
        for (Field fld : this.clsChldType.getFields()) {
            
            // Skip fields of this class - already handled above
            Class<?>    clsOwner = fld.getDeclaringClass();
            if (clsOwner.equals(CfgStructure.class))
                continue;
            
            // We do not touch private attributes
            if (Modifier.isPrivate(fld.getModifiers()))
                continue;
            
            // We do not instantiate static attribute - assumed to be handled by subclass
            if (Modifier.isStatic(fld.getModifiers()))
                continue;
            
            // Skip field if it is a primitive
            Class<?>    clsFldType = fld.getType();
            if (clsFldType.isPrimitive())
                continue;
            
            // If there is no zero-argument (default) constructor there is nothing we can do
            if (JavaRuntime.hasDefaultConstructor(clsFldType))
                continue;
//            if (Stream.of( clsFldType.getConstructors() ).noneMatch(c -> c.getParameterCount() == 0) )
//                continue;
            
            // Extract names - used for logging messages
            String  strFldName = fld.getName();
            String  strFldType = fld.getType().getName();
            
            try {

                // Create empty field instance
                Constructor<?> ctor = fld.getType().getConstructor();

                Object obj = ctor.newInstance();
                
                // Set structure class field value to empty object
                fld.set(This, obj);

            } catch (NoSuchMethodException e) {
                LOGGER.info("No default constructor for field '{}' with type {} in structure class {}: Exception = ", strFldName, strFldType, this.strChldName, e);

            } catch (SecurityException e) {
                LOGGER.error("Default constructor not accessible for field '{}' with type {} in structure class {}: Exception = {}", strFldName, strFldType, this.strChldName, e);

            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                LOGGER.error("Default constructor invocation error for field '{}' with type {} in structure class {}: Exception = {}", strFldName, strFldType, this.strChldName, e);
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
    public boolean equals(Object objCmp) {
        
        // Check argument type then cast comparison object
        if (!this.clsChldType.equals(objCmp.getClass())) {
            LOGGER.warn(this.strChldName + ".equals(Object): argument is not of type {}", this.strChldName);
            return false;
        }
        T CMP = this.clsChldType.cast(objCmp);
        
        // Check equivalence of argument field values
        boolean bolResult = true;
        
        for (Field fld : this.clsChldType.getFields()) {
            fld.setAccessible(true);
            
            // Base class fields are not part of the configuration - skip
            Class<?>    clsOwner = fld.getDeclaringClass();
            if (clsOwner.equals(CfgStructure.class))
                continue;
            
            // Private fields are not part of the configuration - skip
            if (Modifier.isPrivate(fld.getModifiers()))
                continue;
            
            String  strFldName = fld.getName();

            try {
                // Compare field objects of this structure and comparison object
                Object  objFldThis = fld.get(This);
                Object  objFldCmp = fld.get(CMP);
                
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
        
        // First time through marker - used to append comma
        boolean         bolFirst = true;
        
        // Iterate through all structure class fields
        for (Field fld : this.clsChldType.getFields()) {
            
            // if this is not first iterate append comma
            if (!bolFirst)
                buf.append(", ");
            else
                bolFirst = false;
        
            // 
            try {
                Object  objVal = fld.get(This);
                
                buf.append(fld.getName());
                buf.append("=");
                if (objVal != null)
                    buf.append(objVal.toString());
                
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOGGER.warn("{}.toString() field {} access exception {}", this.strChldName, fld.getName(), e.getMessage());
            }
        }
        
        buf.append("}");

        return buf.toString();
    }
    
    
    //
    // Debugging
    //
    
    /**
     * <p>
     * Returns a list of all structure fields that have <code>null</code> values.
     * </p>
     * <p>
     * This function is recursive.  Thus, if a field of this structure is a substructure
     * (is has superclass <code>CfgStructure</code>), the method calls itself on that field.
     * </p>
     * 
     * @return  a list of all fields (and subfields) having a <code>null</code> value
     * 
     * @throws IllegalArgumentException Java reflection class/object inconsistency (should not happen)
     * @throws IllegalAccessException   a field or subfield was inaccessible (private)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Field>  getNullValuedFields() throws IllegalArgumentException, IllegalAccessException {

        // Create returned list object
        List<Field> lstFlds = new LinkedList<Field>();
        
        // Iterate through all structure fields
        for (Field fld : this.clsChldType.getFields()) {
            Class<?>    clsFldType = fld.getType();
            Object      objFld = fld.get(This); 
            
            // Check if field is also a configuration structure
            if (CfgStructure.class.isAssignableFrom(clsFldType)) {
                CfgStructure    cfgFld = CfgStructure.class.cast(objFld);
                List<Field>     lstSubFlds = cfgFld.getNullValuedFields(); 
                
                lstFlds.addAll(lstSubFlds);
                continue;
            }
            
            if (objFld == null)
                lstFlds.add(fld);
        }
        
        return lstFlds;
    }
}
