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

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.util.JavaRuntime;


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
 * <p>
 * See the <code>{@link ACfgOverride} annotation documentation for proper annotation of configuration
 * structure classes.
 * </p>
 * <p>
 * Note that override capable configuration parameters can also be overridden by the Java command line using
 * the "-D<name=value>" convention.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 18, 2023
 * 
 * @see ACfgOverride
 */
public final class CfgOverrideUtility {
    
    
    //
    // Class Types
    //
    
    /**
     * <p>
     * Enumeration of possible sources for override-capable configuration parameters.
     * </p>
     * <p>
     * Note that all sources are taken from the specified cache of system variables enumerated here.
     * Currently the following sources are available:
     * <ul>
     * <li><code>{@link #ENVIRONMENT}</code></li>
     * <li><code>{@link #PROPERTIES}</code></li>
     * </ul>
     * </p>
     * 
     * @author Christopher K. Allen
     * @since Jan 3, 2024
     *
     */
    public static enum SOURCE {
    
        /** From system environment variables (i.e., from<code>{@link System.getenv(String)}</code>) */
        ENVIRONMENT,
        
        /** From system properties variables (i.e., from <code>{@link System.getProperty(String)}</code>) */
        PROPERTIES;
        
        /**
         * Get the value for the given variable name.
         * 
         * @param strName   variable name
         * 
         * @return variable value
         */
        public String   getValue(String strName) {
            if (this.equals(ENVIRONMENT))
                return System.getenv(strName);
            if (this.equals(PROPERTIES))
                return System.getProperty(strName);
            
            return null;
        }
    }
    
    
    //
    // Class Constants
    //
    
    /** The environment variable path element separator */
    public static final String  STR_PATHEL_SEP = "_";
    
    
    //
    // Class Resources
    //
    
    /** The static logging utility */
    private static final Logger LOGGER = LogManager.getLogger();


    //
    // Utility Methods
    //
    
    /**
     * <p>
     * Overrides the parameter values in the given structure class with system variables.
     * </p>
     * <p>
     * The argument must be a structure class annotated with the <code>{@link ACfgOverride.Root}</code> 
     * annotation otherwise a runtime exception is thrown.  The generic type <code>R</code> is included
     * simply to reinforce this requirement at compile time. Java provides only runtime checking for
     * class annotations.
     * </p>
     * <p>
     * The argument is is assumed to be a structure class containing configuration parameters, for example,
     * the <code>{@link DpApiConfig}</code> instance. Although any appropriately annotated
     * structure class is a viable argument.
     * </p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The argument must be a structure class type annotated with <code>@ACfgOverride.Root</code>.
     * </li>
     * <li>
     * The source for the override values is given by the <code>{@link SOURCE}</code> enumeration.
     * It specifies which cache of system variable is used.
     * </li>
     * <li>
     * This method calls the recursive function <code>{@link #overrideField(Field, Object, String, SOURCE)} to
     * process each field of the given structure class.  The method calls itself whenever it encounters
     * a field that is a substructure.
     * </li>
     * </ul> 
     * </p>
     * 
     * @param <R>    structure class type containing the annotation <code>{@link ACfgOverride.Root}</code>
     * 
     * @param root   structure class object whose annotated field values are to be overridden with system variables
     * @param enmSrc enumeration indicating the system cache for the override values
     * 
     * @return      <code>true</code> if at least one parameter was overridden, <code>false</code> if none
     * 
     * @throws IllegalArgumentException the argument is <code>null</code> or is missing the required annotation
     * @throws IllegalAccessException   a field or subfield of the argument is inaccessible (e.g., <code>{@link java.lang.reflect.Field#get(Object)}</code>).
     * 
     * @see #overrideField(Field, Object, String, SOURCE)
     */
    public static <R extends Object> boolean overrideRoot(R root, SOURCE enmSrc) throws IllegalArgumentException, IllegalAccessException {
        
        Class<? extends Object>    clsRoot = root.getClass();
        
        // Runtime check for null argument
        if ( Objects.isNull(root) ) {
            String  strMsg = JavaRuntime.getQualifiedCallerName() + ": Argument is null";
            LOGGER.error(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
        
        // Runtime check if root annotation is present
        if (!clsRoot.isAnnotationPresent(ACfgOverride.Root.class)) {
            String  strMthdNm = JavaRuntime.getQualifiedCallerName();
            String  strErrMsg = strMthdNm +": Argument type + " + root.getClass().getCanonicalName() + " is not annotated with " + ACfgOverride.Root.class.getCanonicalName();
            LOGGER.error(strErrMsg);
            
            throw new IllegalArgumentException(strErrMsg);
        }
        
        // Get the root annotation and its environment variable prefix string
        ACfgOverride.Root   annRoot = clsRoot.getAnnotation(ACfgOverride.Root.class);
        String              strRootName = annRoot.root();
        String              strPathExt = strRootName.concat(STR_PATHEL_SEP);
        
        // Call recursive function to override fields of root structure class
        boolean bolResult = false;
        
        for (Field fld : clsRoot.getDeclaredFields()) {
            fld.setAccessible(true);
            
            if (CfgOverrideUtility.overrideField(fld, root, strPathExt, enmSrc))
                bolResult = true; // result is true if at least one field was overridden
        }

        return bolResult;
    }
    
    /**
         * <p>
         * Convenience method for overriding substructure fields within an appropriately annotated structure class.
         * </p>
         * <p>
         * <h2>WARNING:</h2>
         * Use this method with caution.  It is provided to override the fields of substructures within
         * configuration structure classes without overriding the entire configuration.  
         * Such a need would likely be rare and discretion is advised.
         * <p>
         * If the above application is encounter one could directly call the
         * <code>{@link #overrideField(Field, Object, String, SOURCE)}</code> method with
         * the same arguments and essentially the same effect, although the intent is less clear. 
         * </p>
         * </p>
         * <p>
         * <h2>USE:</h2>
         * <ul>
         * <br/>
         * <li>
         * The argument <code>struct</code> is assumed (but not required) to be a structure class annotated with 
         * <code>{@link ACfgOverride.Struct}</code>.  
         * The generic parameter <code>S</code> is included simply to reinforce this assumption as no 
         * compile-time requirements are possible.
         * </li>
         * <br/>
         * <li>
         * Regarding the above: Further, it is not practical to require that argument <code>struct</code> come from 
         * a <code>{@link ACfgOverride.Struct}</code> annotated field as the annotation is only recoverable from 
         * the containing structure class.
         * Again, <em>use this method with caution</em>.  
         * </li>
         * <br/>
         * <li>
         * The argument <code>struct</code> is assumed to be a substructure field within a configuration class 
         * (i.e., annotated with <code>{@link ACfgOverride.Root}</code>).  The only situation for this method 
         * would be the need to override <em>only the fields of this substructure</em> without disturbing the 
         * entire configuration. 
         * </li>
         * <br/>
         * <li>
         * The fields within the <code>struct</code> argument are assumed to be annotated with either
         * <code>{@link ACfgOverride.Struct}</code> or <code>{@link ACfgOverride.Field}</code>.  If not,
         * nothing is done and the method returns <code>false</code> 
         * </li>
         * <br/>
         * <li>
         * The argument <code>strVarPrefix</code> must accurately identify the prefix of a system
         * variable in relation to the structure class.  If the system variable name is constructed incorrectly
         * it will not be recognized and any override-capable fields within the structure argument will NOT be
         * overridden.
         * </li>
         * <br/>
         * <li>
         * Regarding the above: System variable names are constructed recursively using the argument 
         * <code>strVarPrefix</code>.  This argument specifies the current path, or prefix, of the system variable 
         * name within the overall configuration.  This prefix is constructed analogously to path elements for file
         * names where the separator is given by <code>{@link #STR_PATHEL_SEP}</code>.
         * </li>
         * </ul>
         * <p>
         * <h2>NOTES:</h2>
         * <ul>
         * <li>
         * This method calls the recursive function <code>{@link #overrideField(Field, Object, String, SOURCE)} to
         * process each field of the given structure class.  The method calls itself whenever it encounters
         * a field that is a substructure.
         * </li>
         * </p> 
         *  
         * @param <S>           structure class type is annotated with <code>{@link ACfgOverride.Struct}</code> 
         * 
         * @param struct        structure class assumed to be substructure field of a configuration
         * @param strVarPrefix  prefix, or "path element", for the system variable name w.r.t. overall configuration
         * @param enmSrc        enumeration indicating the system cache for the override values
         * 
         * @return      <code>true</code> if at least one parameter was overridden, <code>false</code> if none
         * 
         * @throws IllegalArgumentException the argument is <code>null</code> or is missing the required annotation
         * @throws IllegalAccessException   a field or subfield of the argument is inaccessible (e.g., <code>{@link java.lang.reflect.Field#get(Object)}</code>).
         * 
         * @see #overrideField(Field, Object, String, SOURCE)
         */
        public static <S extends Object> boolean overrideStruct(S struct, String strVarPrefix, SOURCE enmSrc) throws IllegalArgumentException, IllegalAccessException {
            
            // Get the class type of the structure
            Class<? extends Object> clsStruct = struct.getClass();
            
            // Runtime check for null argument
            if ( Objects.isNull(struct) ) {
                String  strMsg = JavaRuntime.getQualifiedCallerName() + ": Argument is null";
                LOGGER.error(strMsg);
                
                throw new IllegalArgumentException(strMsg);
            }
    
            // NOTE: The following is not practical
    //        // Runtime check if Struct annotation is present
    //        if (!clsStruct.isAnnotationPresent(ACfgOverride.Struct.class)) {
    //            String  strMthdNm = JavaRuntime.getQualifiedCallerName();
    //            String  strErrMsg = strMthdNm +": Argument type + " + struct.getClass().getCanonicalName() + " is not annotated with " + ACfgOverride.Struct.class.getCanonicalName();
    //            LOGGER.error(strErrMsg);
    //            
    //            throw new IllegalArgumentException(strErrMsg);
    //        }
            
            // Add the path element separator
            String  strPathExt = strVarPrefix.concat(STR_PATHEL_SEP);
            
            // Call recursive function to override fields of structure class 
            boolean bolResult = false;
            
            for (Field fldSub : clsStruct.getDeclaredFields()) {
                fldSub.setAccessible(true);
                
                if (CfgOverrideUtility.overrideField(fldSub, struct, strPathExt, enmSrc))
                    bolResult = true; // result is true if at least one field was overridden
            }
    
            return bolResult;
        }

    /**
     * <p>
     * Attempts to override a field within a structure class with a system variable value.
     * </p>
     * <p>
     * This method is intended for use by <code>{@link #overrideRoot(Object, SOURCE)}</code>
     * and <code>{@link #overrideStruct(Object, String, SOURCE)}</code> but its access is
     * left public.  There may be rare circumstances where its application could be warranted.
     * Thus, <em>user discretion</em> is advised with this method.
     * </p>
     * <p>
     * The primary application for public use is to override a field value within
     * a configuration structure class without overriding the entire configuration.
     * If the field is a known substructure, use of the method 
     * <code>{@link #overrideStruct(Object, String, SOURCE)}</code> is recommended since
     * it clarifies the intent.
     * </p>
     * 
     * <h2>USE:</h2>
     * <h3>Arguments <code>Field</code>, <code>Object</code></h3>
     * The argument with type <code>Field</code> must describe a field of argument with type
     * <code>Object</code>, which is assumed to be a structure class object.  If these 
     * arguments are not consistent an exception is thrown.
     * 
     * <h3>Argument <code>String</code></h3>
     * <p>
     * The argument with type <code>String</code> (i.e., <code>strPath</code>) must accurately 
     * identify the prefix of a system variable in relation to the structure class.  If the 
     * system variable name is constructed incorrectly it will not be recognized and any override-capable fields within the structure argument will NOT be
     * overridden.
     * </p>
     * <p>
     * Regarding system variable name construction, names are constructed recursively using the argument 
     * <code>strVarPrefix</code>.  This argument specifies the current path, or prefix, of the system variable 
     * name within the overall configuration.  This prefix is constructed analogously to path elements for file
     * names where the separator is given by <code>{@link #STR_PATHEL_SEP}</code>.
     * </p>
     * <p>
     * <b>WARNING:</b> When using this method independently (called directly) a path separator
     * <b>must be</b> appended.  This is the value current in the <code>{@link #STR_PATHEL_SEP}</code>
     * class variable.
     * </p> 
     * 
     * <h3>Argument <code>SOURCE</code></h3>
     * This argument identifies the source of system variables used for overriding the
     * given field value.  Specifically, it identifies the cache of system variables whose
     * values are used for the override process.
     * 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This is a recursive function. It calls itself in the case where the structure field 
     * identified by the arguments is a substructure containing subfields.  The method then
     * calls itself on every subfield of the substructure.
     * </li>
     * <br/>
     * <li>
     * Since this method is recursive it works correctly for an argument(s) specifying a
     * multi-level structure of arbitrary depth.  It continues recursive execution until
     * a scalar field is encountered in which case it attempts to override the value and
     * returns.
     * </li>
     * </ul>
     * </p>
     * 
     * @param fld       Java reflection <code>Field</code> identifying assignable field of object argument
     * @param obj       Java <code>Object</code> with the given field
     * @param strPath   prefix, or "path element", for the system variable name w.r.t. overall configuration
     * @param enmSrc    enumeration indicating the system cache for the override values
     * 
     * @return <code>true</code> if field value was set (at least one for structure), <code>false</code> otherwise
     * 
     * @throws IllegalArgumentException arguments <code>Field</code> and <code>Object</code> are inconsistent  
     * @throws IllegalAccessException unable to get a structure field object (java.lang.reflect.Field#get)
     */
    public static boolean overrideField(Field fld, Object obj, String strPath, SOURCE enmSrc) throws IllegalArgumentException, IllegalAccessException {
        
        boolean bolResult = false;
        
        // Is the target a terminal field? Specifically, is it marked as an override-capable Field?
        if (fld.isAnnotationPresent(ACfgOverride.Field.class)) {
            fld.setAccessible(true);
            
            // Get the Field annotation and, from it, the corresponding environment variable name 
            //  Then construct the full environment variable path element name
            ACfgOverride.Field  annField = fld.getAnnotation(ACfgOverride.Field.class);
            String              strName = annField.name();
            String              strVarName = strPath.concat(strName);
    
            // Get the environment variable value from name and SOURCE enumeration
            String  strNewVal = enmSrc.getValue(strVarName);
            
            // Override the configuration parameter value with the environment variable value 
            bolResult = CfgOverrideUtility.setFieldValue(fld, obj, strNewVal);
            
            return bolResult;
        }
        
        // Is target field a structure containing fields to be overridden
        if (fld.isAnnotationPresent(ACfgOverride.Struct.class)) {
            
            // Get the Struct annotation and, from it, the environment variable path element extension
            ACfgOverride.Struct annStruct = fld.getAnnotation(ACfgOverride.Struct.class);
            String              strPathelem = annStruct.pathelem();
            String              strPathelExt = strPath.concat(strPathelem).concat(STR_PATHEL_SEP);
    
            // Get the field type (a structure class) and the underlying field object
            Object      objStruct = fld.get(obj);
            Class<?>    clsStruct = fld.getType();
    
            // Then we recursively call this method for each field
            for (Field fldStruct : clsStruct.getDeclaredFields()) {
                fldStruct.setAccessible(true);
    
                // Recursively override the structures field and any subfields within substructures
                if (CfgOverrideUtility.overrideField(fldStruct, objStruct, strPathelExt, enmSrc))
                    bolResult = true; // result is true if at least one field was overridden
            }
            
            return bolResult;
        }
        
        // If we are here, nothing was done.
        return false;
    }

    /**
     * <p>
     * Extracts and returns a list of override-capable fields for the given configuration structure class.
     * </p>
     * <p>
     * An override-capable field is identified by a <code>{@link CfgOverrideRec}</code> record which specifies
     * the environment variable assigned to it, along with other field properties such as its structure type, 
     * structure field, and values.  A record is returned for every such field identified within the argument.
     * </p>
     * <p>
     * The method parses the fields of the structure class for the 
     * <code>ACfgOverride</code> annotations to determine the supported environment
     * variables for the class.  The method then returns a record list identifying these
     * variables which it found (along with the current values, the properties object,
     * etc. as an <code>CfgOverrideRec</code> records).
     * </p>
     * <p>
     * It is necessary that the argument is a properly annotated structure class instance.  The generic type
     * parameter <code>R</code> is included simply to stress this requirement, as Java provides no compile-time
     * method for enforcing this condition.  If the argument type does not contain the <code>ACfgOverride.Root</code>
     * annotation an exception is thrown. 
     * </p>
     * <p>
     * It is assumed, but not required, that the argument has been loaded with values 
     * (e.g., using the <code>CfgLoaderYaml</code> utility), as current values of the configuration 
     * parameters are included in the returned records.
     * </p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The type of the argument is required to be an <code>@CfgOverride.Root</code> annotated structure class.
     * </li>
     * <li>
     * This method calls the recursive function <code>{@link #extractOverrideableFields(Field, Object, String, SOURCE)</code>
     * to process the structure fields.
     * </li>   
     * </ul>
     * </p>
     * 
     * @param <R>       a type that is annotated with <code>{@link ACfgOverride.Root}</code>
     * 
     * @param root      structure class of configuration parameters  
     * @param enmSrc    enumeration indicating the system cache for the override values
     * 
     * @return unordered list of environment variables records corresponding to override-capable configuration parameters 
     * 
     * @throws IllegalArgumentException the argument is <code>null</code> or is missing the required annotation
     * @throws IllegalAccessException   a field or subfield of the argument is inaccessible (e.g., <code>{@link java.lang.reflect.Field#get(Object)}</code>).
     * 
     * @see #extractOverrideableFields(Field, Object, String, SOURCE)
     */
    public static <R extends Object> List<CfgOverrideRec> extractOverrideables(R root, SOURCE enmSrc) throws IllegalArgumentException, IllegalAccessException {
        
        // Check for null argument
        if ( Objects.isNull(root) ) {
            String  strMsg = JavaRuntime.getQualifiedCallerName() + ": Argument is null";
            LOGGER.error(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
        
        // Check for Root annotation
        Class<?> clsRoot = root.getClass();
        if (!clsRoot.isAnnotationPresent(ACfgOverride.Root.class)) {
            String  strMsg = JavaRuntime.getQualifiedCallerName() + ": Argument (type=" + clsRoot.getSimpleName() + ") missing annotation " + ACfgOverride.Root.class.getSimpleName();
            LOGGER.error(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
    
        // Get the root annotation and its environment variable prefix string
        ACfgOverride.Root   annRoot = clsRoot.getAnnotation(ACfgOverride.Root.class);
        String              strRootName = annRoot.root();
        String              strPathExt = strRootName.concat(STR_PATHEL_SEP);
    
        // Extract override capable parameter from each field using recursive method
        List<CfgOverrideRec> lstParams = new LinkedList<CfgOverrideRec>();
    
        for (Field fld : clsRoot.getDeclaredFields()) {
            
            List<CfgOverrideRec> lstFldParams = CfgOverrideUtility.extractOverrideableFields(fld, root, strPathExt, enmSrc);
            
            lstParams.addAll(lstFldParams);
        }        
    
        return lstParams;
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Recursive function for extracting the system variable name and value corresponding to an
     * override-capable structure class.
     * </p>
     * <p>
     * This method is called by <code>{@link #extractOverrideables(Object, SOURCE)}</code>.
     * </p>
     * <p>
     * The <code>Field</code> type argument is assumed to be the field of a structure class containing 
     * configuration parameters. If the structure field described by the arguments is override-capable 
     * then it is annotated with one of the <code>{@link ACfgOverride}</code> enclosed annotations.  
     * If the argument is not annotated the field is not override-capable and an empty list is returned.
     * </p>
     * <p>
     * The <code>Object</code> argument is the structure class instance containing the given
     * field.  To get the field value, the invocation 
     * <br/><br/>
     * &nbsp; &nbsp; <code>{@link Field#get(Object)}</code>
     * <br/><br/>
     * is required on this argument.  (The above returns a Java <code>Object</code> containing
     * the value.)  If the <code>Field</code> type argument is annotated with 
     * <code>{@link ACfgOverride.Field}</code> a new record is created and the above invocation 
     * is used to acquire the <code>{@link CfgOverrideRec#cfgValue()}</code> attribute.  
     * The above reflection method call is the source of any thrown exceptions.
     * </p>
     * <p>
     * The name of the overriding system variable is constructed recursively using the argument 
     * <code>strPath</code>.  This argument specifies the current path, or prefix, of the system variable 
     * name.  For substructure fields this prefix is constructed analogously to path elements for file
     * names where the separator is given by <code>{@link #STR_PATHEL_SEP}</code>.
     * </p>
     * <p>
     * This method is recursive.  It calls itself whenever the given field represents a substructure within the
     * overall structure class (as indicated by annotation <code>{@link ACfgOverride.Struct</code>). Specifically,
     * it extracts the path element of the environment variable name, then calls itself on every subfield of
     * the substructure (while extracting the corresponding <code>Object</code> for the subfield).
     * </p> 
     * 
     * @param fldStruct Java <code>Field</code> descriptor to be checked for annotation
     * @param objStruct structure class instance which contains the given field 
     * @param strPath   current environment variable name prefix ("path element")
     * @param enmSrc    enumeration for source of the configuration parameter new value
     * 
     * @return  an unordered list of field records if given field is a override-capable substructure, 
     *          a list containing a single record if the given field is an override-capable field,
     *          otherwise an empty list
     *          
     * @throws IllegalArgumentException the given <code>fld</code> and <code>obj</code> arguments are inconsistent (this should not occur)
     * @throws IllegalAccessException   a field or subfield of the argument is inaccessible (e.g., <code>{@link java.lang.reflect.Field#get(Object)}</code>).
     */
    private static List<CfgOverrideRec>   extractOverrideableFields(Field fldStruct, Object objStruct, String strPath, SOURCE enmSrc) throws IllegalArgumentException, IllegalAccessException {
        
        List<CfgOverrideRec> lstRecs = new LinkedList<CfgOverrideRec>();
    
        // Check if object is a terminal field - if so process and return
        if (fldStruct.isAnnotationPresent(ACfgOverride.Field.class)) {
            ACfgOverride.Field annFld = fldStruct.getAnnotation(ACfgOverride.Field.class);
            
            if ( !annFld.name().isBlank() ) {
                
                // Construct the system variable name and get value
                String strName   = annFld.name();
                String strVarNm  = strPath.concat(strName);
                String strVarVal = enmSrc.getValue(strVarNm);
                
                // Extract structure type and field value
                Class<?> clsStruct = fldStruct.getDeclaringClass();
                Object   objFld = fldStruct.get(objStruct);   // this should be safe since its annotated
                
                // Build record and add to list - single element list gets returned
                CfgOverrideRec rec = new CfgOverrideRec(strVarNm, strVarVal, clsStruct, fldStruct, objFld);
               
                lstRecs.add(rec);
            }
            
            return lstRecs;
        }
        
        // Check if object is a structure - if so call this method for all structure fields
        if (fldStruct.isAnnotationPresent(ACfgOverride.Struct.class)) {
    
            // Get the Struct annotation and, from it, the environment variable path element extension
            ACfgOverride.Struct annStruct = fldStruct.getAnnotation(ACfgOverride.Struct.class);
            String              strPathelem = annStruct.pathelem();
            String              strPathelExt = strPath.concat(strPathelem).concat(STR_PATHEL_SEP);

            // Get the substructure type and object
            Class<?>    clsSubStruct = fldStruct.getType();
            Object      objSubStruct = fldStruct.get(objStruct);
            
            // Then we recursively call this method for each field
            for (Field fldSubStruct : clsSubStruct.getDeclaredFields()) {
    
                List<CfgOverrideRec> lstFldRecs = CfgOverrideUtility.extractOverrideableFields(fldSubStruct, objSubStruct, strPathelExt, enmSrc);
                
                lstRecs.addAll(lstFldRecs);
            }
            
            return lstRecs;
        }
        
        // If we are here then field is not override-capable
        //  Returns an empty list
        return lstRecs;
    }

    /**
     * <p>
     * Sets the field value for the given object to new value from string representation.
     * </p>
     * <p>
     * The given object must contain the given Java reflection field or an exception is thrown.  
     * The string is parsed according to the field Java class type.  If the field type is not supported
     * the method returns <code>false</code>.  Otherwise the string argument is parsed and its 
     * converted value is assigned to the object's field through reflection.
     * </p>
     * <p>
     * The list of currently supported types are as follows:
     * <ul>
     * <li><code>Enum.class</code> - string is assumed to be enumeration constant name.</li>
     * <li><code>String.class</code>  - direct assignment.</li>
     * <li><code>Boolean.class</code> - <code>Boolean.parseBoolean(String)</code>.</li>
     * <li><code>Integer.class</code> - <code>Integer.parseInt(String)</code>.</li>
     * <li><code>Float.class</code> - <code>Float.parse(StringFloat)</code>.</li>
     * <li><code>Double.class</code> - <code>Double.parseDouble(String)</code>.</li>
     * </ul>
     * </p>
     * 
     * @param fld       Java reflection <code>Field</code> identifying assignable field of object argument
     * @param obj       Java <code>Object</code> with the given field
     * @param strNewVal string representation of the new value (to be parsed by type)
     * 
     * @return <code>true</code> if field value was set, <code>false</code> otherwise
     * 
     * @throws IllegalArgumentException arguments <code>Field</code> and <code>Object</code> are inconsistent  
     * @throws IllegalAccessException unable to get a structure field object (java.lang.reflect.Field#get)
     */
    private static boolean setFieldValue(Field fld, Object obj, String strNewVal) throws IllegalArgumentException, IllegalAccessException {
        
        // Skip unassigned system variables
        if (strNewVal == null || strNewVal.isBlank() || strNewVal.isEmpty())
            return false;
    
        // Parse by type
        if (fld.getType().isEnum()) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Enum<?> enmNewVal = Enum.valueOf((Class<Enum>)fld.getType(), strNewVal);
            fld.set(obj, enmNewVal);
            
            return true;
        }
        if (fld.getType().equals(java.lang.String.class)) {
            fld.set(obj, strNewVal);
        
            return true;
        }
        if (fld.getType().equals(Boolean.class)) {
            Boolean bolNewVal = Boolean.parseBoolean(strNewVal);
            fld.set(obj, bolNewVal);
            
            return true;
        }
        if (fld.getType().equals(Integer.class)) {
            Integer intNewVal = Integer.parseInt(strNewVal);
            fld.set(obj, intNewVal);
            
            return true;
        }
        if (fld.getType().equals(Long.class)) {
            Long    lngNewVal = Long.parseLong(strNewVal);
            fld.set(obj, lngNewVal);
            
            return true;
        }
        if (fld.getType().equals(Float.class)) {
            Float   fltNewVal = Float.parseFloat(strNewVal);
            fld.set(obj, fltNewVal);
            
            return true;
        }
        if (fld.getType().equals(Double.class)) {
            Double  dblNewVal = Double.parseDouble(strNewVal);
            fld.set(obj, dblNewVal);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * <p>
     * Sets the object value directly to new value from string representation.
     * </p>
     * <p>
     * Maybe be used in rather than <code>{@link #setFieldValue(Field, Object, String)}</code>
     * if only the super object for a class field is not available, or the Java <code>Field</code>
     * descriptor for the structure field is not available.
     * </p>
     * <p>
     * The given object must contain the Java reflection field or an exception is thrown.  The
     * string is parsed according to the field Java class type.  If the field type is not supported
     * the method returns <code>false</code>.  Otherwise the string argument is parsed and its 
     * converted value is assigned to the object's field through reflection.
     * </p>
     * <p>
     * The list of currently supported types are as follows:
     * <ul>
     * <li><code>Enum.class</code> - string is assumed to be enumeration constant name.</li>
     * <li><code>String.class</code>  - direct assignment.</li>
     * <li><code>Boolean.class</code> - <code>Boolean.parseBoolean(String)</code>.</li>
     * <li><code>Integer.class</code> - <code>Integer.parseInt(String)</code>.</li>
     * <li><code>Float.class</code> - <code>Float.parse(StringFloat)</code>.</li>
     * <li><code>Double.class</code> - <code>Double.parseDouble(String)</code>.</li>
     * </ul>
     * </p>
     * 
     * @param clsType   Java class type for assignable object argument
     * @param obj       Java <code>Object</code> with the given field
     * @param strNewVal string representation of the new value (to be parsed by type)
     * 
     * @return <code>true</code> if field value was set, <code>false</code> otherwise
     * 
     * @throws IllegalArgumentException encountered a bad format (i.e., in system variable) during parsing
     * @throws IllegalAccessException this exception should not occur
     * @throws ClassCastException the <code>Class</code> argument and <code>Object</code> argument are inconsistent
     */
    @Deprecated(since="Jan 3, 2024", forRemoval=false)
    @SuppressWarnings("unused")
    private static boolean setObjectValue(Class<?> clsType, Object obj, String strNewVal) throws IllegalArgumentException, IllegalAccessException, ClassCastException {
        
        boolean bolResult = false;
        
        if (strNewVal == null || strNewVal.isBlank() || strNewVal.isEmpty())
            return false;
    
        // Parse by type
        if (clsType.isEnum()) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Enum<?> enmNewVal = Enum.valueOf((Class<Enum>)clsType, strNewVal);
            obj = enmNewVal;
            
            bolResult = true;
        }
        if (clsType.equals(java.lang.String.class)) {
            obj = strNewVal;
        
            bolResult = true;
        }
        if (clsType.equals(Boolean.class)) {
            Boolean bolNewVal = Boolean.parseBoolean(strNewVal);
            obj = bolNewVal;
            
            bolResult = true;
        }
        if (clsType.equals(Integer.class)) {
            Integer intNewVal = Integer.parseInt(strNewVal);
            obj = intNewVal;
            
            bolResult = true;
        }
        if (clsType.equals(Long.class)) {
            Long    lngNewVal = Long.parseLong(strNewVal);
            obj = lngNewVal;
            
            bolResult = true;
        }
        if (clsType.equals(Float.class)) {
            Float   fltNewVal = Float.parseFloat(strNewVal);
            obj = fltNewVal;
            
            bolResult = true;
        }
        if (clsType.equals(Double.class)) {
            Double  dblNewVal = Double.parseDouble(strNewVal);
            obj = dblNewVal;
            
            bolResult = true;
        }
        
        return bolResult;
    }

    
    //
    // Deprecated Methods
    //

    /**
     * <p>
     * Parses the fields in a structure class for the 
     * <code>ACfgOverride</code> annotations to determine the supported environment
     * variables for the class.  The method then returns a list of these
     * variables which it found (along with the current values, the properties object,
     * etc. as an <code>CfgOverrideRec</code> records).
     * </p>
     * <p>
     * It is assumed the argument is
     * a properly annotated structure class instance which has already been loaded (e.g., using
     * the <code>CfgLoaderYaml</code> utility).  from the
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
     * 
     * @deprecated Replaced by 
     */
    @Deprecated(since="Jan 3, 2024", forRemoval=true)
    public static List<CfgOverrideRec> extractOverrides(Object objStruct) throws IllegalArgumentException, IllegalAccessException {
        
        if ( Objects.isNull(objStruct) )
            throw new IllegalArgumentException("Properties object is null");
        
        List<CfgOverrideRec> lstVars = new LinkedList<CfgOverrideRec>();

        Class<?> clsProps = objStruct.getClass();

        for (Field fld : clsProps.getDeclaredFields()) {
            if (fld.isAnnotationPresent(ACfgOverride.Struct.class)) {
                fld.setAccessible(true);
                Object objFld = fld.get(objStruct);
                
                lstVars.addAll( CfgOverrideUtility.extractOverrides(objFld) );
            }
            
            if (fld.isAnnotationPresent(ACfgOverride.Field.class)) {
                ACfgOverride.Field annFld = fld.getAnnotation(ACfgOverride.Field.class);
                if ( !annFld.name().isBlank() ) {
                    String strVarNm  = annFld.name();
                    String strVarVal = System.getenv(annFld.name());
                    
                    CfgOverrideRec var = new CfgOverrideRec(strVarNm, strVarVal, fld.getType(), fld, objStruct);
                   
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
     * 
     * @deprecated Replaced by {@link #overrideRoot(Object, SOURCE)} and {@link #overrideStruct(Object, String, SOURCE)}
     */
    @Deprecated(since="Jan 3, 2024", forRemoval=true)
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
                    
                    bolResult = CfgOverrideUtility.setFieldValue(fld, objProps, strNewVal);
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
     * 
     * @deprecated Replaced by {@link #overrideRoot(Object, SOURCE)} and {@link #overrideStruct(Object, String, SOURCE)}
     */
    @Deprecated(since="Jan 3, 2024", forRemoval=true)
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
                   
                    if (strNewVal == null || strNewVal.isBlank() || strNewVal.isEmpty())
                        continue;

                    fld.setAccessible(true);
                    
                    bolResult = CfgOverrideUtility.setFieldValue(fld, objProps, strNewVal);
                }
            }
            
        }        
        
        return bolResult;
    }
    
    
    /**
     * <p>
     * Prevent construction of new <code>CfgOverrideUtility</code> instances.
     * </p>
     *
     */
    private CfgOverrideUtility() {
        
    }
}
