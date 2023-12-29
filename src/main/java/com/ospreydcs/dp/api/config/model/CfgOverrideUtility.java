/*
 * Project: dp-api-common
 * File:	CfgOverrideUtility.java
 * Package: com.ospreydcs.dp.api.config
 * Type: 	CfgOverrideUtility
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
 * @since Dec 18, 2023
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config.model;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


/**
 * <p>
 * Utility class supporting environment variable annotations <code>ACfgOverride</code> on structure
 * classes containing configuration parameters.
 * </p>
 * <p>
 * The class provides the ability to override the structure class field values with environment variables
 * that are marked via annotation.  The utility class can also create lists of environment variables and
 * their values from the annotations upon structure classes.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 18, 2023
 *
 */
public final class CfgOverrideUtility {

    /**
     * <p>
     * Parses the fields in a structure class for the 
     * <code>ACfgOverride</code> annotations to determine the supported environment
     * variables for the class.  The application then returns a list of these
     * variables which it found (along with the current values, the properties object,
     * etc. as an <code>CfgParameter</code> records).
     * </p>
     * <p>
     * It is assumed the argument is
     * a properly annotated structure class instance which has already been loaded (e.g., using
     * the <code>YamlLoader</code> utility).  from the
     * </p>
     * <h2>NOTE:</h2>
     * This is a recursive function so the method signature must be of type
     * <code>Object</code> to function correctly.  However, the argument at the time of initial invocation
     * is expected to be an <code>@EnvOverride</code> annotated structure class. 
     * </p>
     * 
     * @param objStruct  the structure class containing <code>@EnvOverride</code> annotations
     * 
     * @return a list of environment variables (records) found when parsing the argument 
     * 
     * @throws IllegalArgumentException encountered a null argument during recursion
     * @throws IllegAccessException unable to get a structure field object (java.lang.reflect.Field#get)
     */
    public static List<CfgParameter> parse(Object objStruct) throws IllegalArgumentException, IllegalAccessException {
        
        if ( Objects.isNull(objStruct) )
            throw new IllegalArgumentException("Properties object is null");
        
        List<CfgParameter> lstVars = new LinkedList<CfgParameter>();

        Class<?> clsProps = objStruct.getClass();

        for (Field fld : clsProps.getDeclaredFields()) {
            if (fld.isAnnotationPresent(ACfgOverride.Struct.class)) {
                fld.setAccessible(true);
                Object objFld = fld.get(objStruct);
                
                lstVars.addAll( CfgOverrideUtility.parse(objFld) );
            }
            
            if (fld.isAnnotationPresent(ACfgOverride.Field.class)) {
                ACfgOverride.Field annFld = fld.getAnnotation(ACfgOverride.Field.class);
                if ( !annFld.name().isBlank() ) {
                    String strVarNm  = annFld.name();
                    String strVarVal = System.getenv(annFld.name());
                    
                    CfgParameter var = new CfgParameter(strVarNm, strVarVal, fld, objStruct);
                   
                    lstVars.add(var);
                }
            }
            
        }        
     
        return lstVars;
    }

    /**
     * <p>
     * Overrides the current parameter values in the given structure class with
     * those specified as environment variables using the <code>ACfgOverride</code> annotations.
     * </p>
     * <p>
     * It is assumed the argument is the <code>DpApiConfig</code> instance which has already been loaded 
     * from the <em>dp-api-config.yml</em> configuration file.  Although any appropriately annotated
     * structure class is a viable argument.
     * </p>
     * 
     * <h2>NOTE:</h2>
     * This is a recursive function so the method signature must be of type
     * <code>Object</code> to function correctly.  However, the argument of initial invocation
     * is expected to be of a structure class type annotated with the <code>@ACfgOverride</code>
     * annotations. 
     * </p>
     * 
     * @param   <Struct>    The type of the structure class containing annotated configuration parameters
     * 
     * @param objProps  structure class whose annotated field values are to be overridden with environment variables  
     * 
     * @return <code>true</code> if at least one parameter was overridden, <code>false</code> otherwise
     * 
     * @throws IllegalArgumentException encountered a null argument during recursion
     * @throws IllegalAccessException unable to get a structure field object (java.lang.reflect.Field#get)
     */
    public static <Struct extends Object> boolean envOverride(Struct objProps) throws IllegalArgumentException, IllegalAccessException {
        boolean bolResult = false;
        
        if ( Objects.isNull(objProps) )
            throw new IllegalArgumentException("Properties object is null");
        
        Class<?> clsProps = objProps.getClass();
        
        // Is the class marked for override (or recursive included class)?
//        if (! (  clsProps.isAnnotationPresent(ACfgOverride.class) 
//              || clsProps.isAnnotationPresent(ACfgOverride.Struct.class)
//              || clsProps.isAnnotationPresent(ACfgOverride.Field.class) ) 
//            ) 
//            return false;

        // For every field in the class
        for (Field fld : clsProps.getDeclaredFields()) {
            
            // Is field a structure containing a field to be overridden
            if (fld.isAnnotationPresent(ACfgOverride.Struct.class)) {
                fld.setAccessible(true);
                Object objFld = fld.get(objProps);
                
                // Recursively override the underlying field 
                if (CfgOverrideUtility.envOverride(objFld))
                    bolResult = true;
                
            }
            
            // Can the field be overridden?
            if (fld.isAnnotationPresent(ACfgOverride.Field.class)) {
                ACfgOverride.Field annFld = fld.getAnnotation(ACfgOverride.Field.class);
                if ( !annFld.name().isBlank() ) {
                    String strNewVal = System.getenv(annFld.name());
                   
                    if (strNewVal == null || strNewVal.isBlank() || strNewVal.isEmpty())
                        continue;

                    fld.setAccessible(true);
                    
                    // Parse by type
                    if (fld.getType().isEnum()) {
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        Enum<?> enmNewVal = Enum.valueOf((Class<Enum>)fld.getType(), strNewVal);
                        fld.set(objProps, enmNewVal);
                        
                        bolResult = true;
                    }
                    if (fld.getType().equals(java.lang.String.class)) {
                        fld.set(objProps, strNewVal);
                    
                        bolResult = true;
                    }
                    if (fld.getType().equals(Boolean.class)) {
                        Boolean bolNewVal = Boolean.parseBoolean(strNewVal);
                        fld.set(objProps, bolNewVal);
                        
                        bolResult = true;
                    }
                    if (fld.getType().equals(Integer.class)) {
                        Integer intNewVal = Integer.parseInt(strNewVal);
                        fld.set(objProps, intNewVal);
                        
                        bolResult = true;
                    }
                    if (fld.getType().equals(Long.class)) {
                        Long    lngNewVal = Long.parseLong(strNewVal);
                        fld.set(objProps, lngNewVal);
                        
                        bolResult = true;
                    }
                    if (fld.getType().equals(Float.class)) {
                        Float   fltNewVal = Float.parseFloat(strNewVal);
                        fld.set(objProps, fltNewVal);
                        
                        bolResult = true;
                    }
                    if (fld.getType().equals(Double.class)) {
                        Double  dblNewVal = Double.parseDouble(strNewVal);
                        fld.set(objProps, dblNewVal);
                        
                        bolResult = true;
                    }
                }
            }
            
        }        
        
        return bolResult;
    }
    
    /**
     * <p>
     * Overrides the current parameter values in the given structure class with
     * those specified as java command-line variables using the <code>ACfgOverride</code> annotations.
     * </p>
     * <p>
     * Java command-line arguments are specified using the "-D<name=value>" convention.
     * </p>
     * <p>
     * It is assumed the argument is the <code>DpApiConfig</code> instance which has already been loaded 
     * from the <em>dp-api-config.yml</em> configuration file.  Although any appropriately annotated
     * structure class is a viable argument.
     * </p>
     * 
     * <h2>NOTE:</h2>
     * This is a recursive function so the method signature must be of type
     * <code>Object</code> to function correctly.  However, the argument of initial invocation
     * is expected to be of a structure class type annotated with the <code>@ACfgOverride</code>
     * annotations. 
     * </p>
     * 
     * @param   <Struct>    The type of the structure class containing annotated configuration parameters
     * 
     * @param objProps  structure class whose annotated field values are to be overridden with environment variables  
     * 
     * @return <code>true</code> if at least one parameter was overridden, <code>false</code> otherwise
     * 
     * @throws IllegalArgumentException encountered a null argument during recursion
     * @throws IllegalAccessException unable to get a structure field object (java.lang.reflect.Field#get)
     */
    public static <Struct extends Object> boolean argOverride(Struct objProps) throws IllegalArgumentException, IllegalAccessException {
        boolean bolResult = false;
        
        if ( Objects.isNull(objProps) )
            throw new IllegalArgumentException("Properties object is null");
        
        Class<?> clsProps = objProps.getClass();
        
        // Is the class marked for override (or recursive included class)?
//        if (! (  clsProps.isAnnotationPresent(ACfgOverride.class) 
//              || clsProps.isAnnotationPresent(ACfgOverride.Struct.class)
//              || clsProps.isAnnotationPresent(ACfgOverride.Field.class) ) 
//            ) 
//            return false;

        // For every field in the class
        for (Field fld : clsProps.getDeclaredFields()) {
            
            // Is field a structure containing a field to be overridden
            if (fld.isAnnotationPresent(ACfgOverride.Struct.class)) {
                fld.setAccessible(true);
                Object objFld = fld.get(objProps);
                
                // Recursively override the underlying field 
                if (CfgOverrideUtility.envOverride(objFld))
                    bolResult = true;
                
            }
            
            // Can the field be overridden?
            if (fld.isAnnotationPresent(ACfgOverride.Field.class)) {
                ACfgOverride.Field annFld = fld.getAnnotation(ACfgOverride.Field.class);
                if ( !annFld.name().isBlank() ) {
                    String strNewVal = System.getProperty(annFld.name());
//                    String strNewVal = System.getenv(annFld.env());
                   
                    if (strNewVal == null || strNewVal.isBlank() || strNewVal.isEmpty())
                        continue;

                    fld.setAccessible(true);
                    
                    // Parse by type
                    if (fld.getType().isEnum()) {
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        Enum<?> enmNewVal = Enum.valueOf((Class<Enum>)fld.getType(), strNewVal);
                        fld.set(objProps, enmNewVal);
                        
                        bolResult = true;
                    }
                    if (fld.getType().equals(java.lang.String.class)) {
                        fld.set(objProps, strNewVal);
                    
                        bolResult = true;
                    }
                    if (fld.getType().equals(Boolean.class)) {
                        Boolean bolNewVal = Boolean.parseBoolean(strNewVal);
                        fld.set(objProps, bolNewVal);
                        
                        bolResult = true;
                    }
                    if (fld.getType().equals(Integer.class)) {
                        Integer intNewVal = Integer.parseInt(strNewVal);
                        fld.set(objProps, intNewVal);
                        
                        bolResult = true;
                    }
                    if (fld.getType().equals(Long.class)) {
                        Long    lngNewVal = Long.parseLong(strNewVal);
                        fld.set(objProps, lngNewVal);
                        
                        bolResult = true;
                    }
                    if (fld.getType().equals(Float.class)) {
                        Float   fltNewVal = Float.parseFloat(strNewVal);
                        fld.set(objProps, fltNewVal);
                        
                        bolResult = true;
                    }
                    if (fld.getType().equals(Double.class)) {
                        Double  dblNewVal = Double.parseDouble(strNewVal);
                        fld.set(objProps, dblNewVal);
                        
                        bolResult = true;
                    }
                }
            }
            
        }        
        
        return bolResult;
    }
    
    
    //
    // Support Methods
    //
    
}
