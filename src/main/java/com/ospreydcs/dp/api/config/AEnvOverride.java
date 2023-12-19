/*
 * Project: dp-api-common
 * File:	AEnvOverride.java
 * Package: com.ospreydcs.dp.api.config
 * Type: 	AEnvOverride
 *
 * @author Christopher K. Allen
 * @since Sep 18, 2022
 *
 * TODO:
 * - TODO
 */
package com.ospreydcs.dp.api.config;

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
 * The Annotation is used to mark specific class fields for override by environment variables.  There
 * are the following enclosed annotations
 * <ul>
 * <li><code>Field</code> - explicitly marks a class field for environment variable override.</li>
 * <li><code>Struct</code> - Indicates that the field is a sub-structure containing a overrideable field.</li>
 * </ul>
 * The enclosing annotation <code>AEnvOverride</code> can be used to mark the top-level structure class 
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
public @interface AEnvOverride {


    /**
     * Annotation for a field of a class that represents an underlying
     * data structure containing a field that is annotated with 
     * <code>AEnvOverride.Field</code>.
     * Or, the data structure may contain fields that are themselves data
     * structures that are annotated with <code>AEnvOverride.Struct</code>. 
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
     * Annotation for fields of a parameters class that can be overridden 
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
         * Name of the environment variable used to override this property field value.
         * 
         * @return  environment variable name containing new value for field
         */
        public String env() default "";
    }

}
