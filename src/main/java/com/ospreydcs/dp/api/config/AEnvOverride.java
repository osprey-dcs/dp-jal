/*
 * Project: datastore-admin
 * File:	AOverrideCapable.java
 * Package: com.ospreydcs.datastore.admin.db.mongodb.model
 * Type: 	AOverrideCapable
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
     * Annotation indicating that a type contains attributes that can be overridden.
     * 
     *
     * @author Christopher K. Allen
     * @since Sep 18, 2022
     *
     * @deprecated Not currently supported
     */
    @Deprecated
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Type {
        
    }
    
    
    /**
     * Annotation for a field of a class that represents an underlying
     * data structure containing a field that is annotated with 
     * <code>AOverrideCapable.Field</code>.
     * Or, the data structure may contain fields that are themselves data
     * structures that are annotated with <code>AOverrideCapable.Struct</code>. 
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
     * Annotation for fields of a properties class that can be overridden 
     * (e.g., by environment variables).
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
