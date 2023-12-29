/*
 * Project: dp-api-common
 * File:	ACfgOverride.java
 * Package: com.ospreydcs.dp.api.config
 * Type: 	ACfgOverride
 *
 * @author Christopher K. Allen
 * @since Sep 18, 2022
 *
 * TODO:
 * - TODO
 */
package com.ospreydcs.dp.api.config.model;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>
 * Annotation for Structure Classes Containing Parameters that can be overridden with 
 * Environment Variables.
 * </p>
 * <p>
 * Application configuration parameters are contained in structure classes defined within the 
 * API library configuration mechanism.  These annotations are used to target certain structure fields
 * for potential value override by the current environment.
 * </p>
 * <p>
 * The Annotation is used to mark specific class fields for override by environment variables or Java
 * command line arguments.  These parameters fields are marked by the following annotations:
 * <ul>
 * <li><code>Field</code> - explicitly marks a class field for environment variable override.</li>
 * <li><code>Struct</code> - Indicates that the field is a sub-structure containing a overrideable field.</li>
 * </ul>
 * The enclosing annotation <code>ACfgOverride</code> can be used to mark the top-level structure class 
 * as containing overrideable fields.  This action is not currently enforced but might be in the future.  
 * </p>
 *
 * @author Christopher K. Allen
 * @since Sep 18, 2022
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ACfgOverride {


    /**
     * <p>
     * Annotation indicating that that the current field of a structure class is a nested
     * structure class.  The marked nested structure contains a field that can be overridden.
     * </p>
     * <p>
     * Specifically, this annotation is used for a structure class field representing a nested
     * data structure containing a field that is annotated with 
     * <code>ACfgOverride.Field</code>.
     * Or, potentially, the nested data structure may contain fields that are themselves nested data
     * structures that are annotated with <code>ACfgOverride.Struct</code>. 
     * </p>
     *
     * @author Christopher K. Allen
     * @since Sep 22, 2022
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Struct {
        
    }
    
    
    /**
     * Annotation for fields of a structure class that can be overridden 
     * by environment variables.
     * 
     * @author Christopher K. Allen
     * @since Sep 18, 2022
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Field {
        
        /**
         * Name of the environment variable or system property used to override this property field value.
         * 
         * @return  environment variable name containing new value for field
         */
        public String name() default "";
    }

}
