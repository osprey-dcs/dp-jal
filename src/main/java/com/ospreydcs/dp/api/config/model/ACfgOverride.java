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
 * The annotations here are recognized by the <code>{@link CfgOverrideUtility}</code> utility
 * class for overriding configuration parameters by system environment variables or properties.
 * The utility class uses the annotations to identify configuration parameters whose values can
 * be overridden by environment variables, and for constructing the names of the corresponding
 * environment variables.
 * </p> 
 * <p>
 * <h2>Value Overrides</h2>
 * Application configuration parameters are contained in structure classes defined within the 
 * API library configuration mechanism.  These annotations are used to target certain structure fields
 * for potential value override by the current environment.
 * </p>
 * <p>
 * The Annotation is used to mark specific class fields for override by environment variables or Java
 * command line arguments.  These parameters fields are marked by the following annotations:
 * <ul>
 * <li><code>Root</code> - Indicates the root class type of the structure class (a top-level class) 
 * <li><code>Struct</code> - Indicates that the field is a sub-structure containing a overrideable field.</li>
 * <li><code>Field</code> - explicitly marks a class field for environment variable override.</li>
 * </ul>
 * The enclosing annotation <code>ACfgOverride</code> can be used to mark the top-level structure class 
 * as containing overrideable fields.  This action is not currently enforced but might be in the future.  
 * </p>
 * <p>
 * <h2>NAMING:</h2>
 * The name of an environment variable (or system property) used to override a marked structure class field
 * is constructed as follows:
 * <br/><br/>
 * &nbsp;&nbsp; [root]_[pathelem]_...[pathelem]_[name]
 * <br/><br/>
 * where
 * <ul>
 * <li>[root] - the string returned by the <code>ACfgOverride.Root.root()</code> methods.</li>
 * <li>[pathelem] - the string returned by the <code>ACfgOverride.Struct.pathelem()</code> methods.</li>
 * <li>[name] - the string returned by the </code>ACfgOverride.Field.name()</code> method.</li>
 * </ul>
 * Note that environment variable names for configuration parameters within multi-level nested structures will 
 * include all path elements for all substructures.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Sep 18, 2022
 *
 * @see CfgOverrideUtility
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ACfgOverride {


    /**
     * <p>
     * Annotation indicating that the root of a structure class containing configuration parameters.
     * </p>
     * <p>
     * The marked class is a root class of a configuration parameter structure, that is, it is the top
     * level of the configuration.  Note that there can be multiple root structure classes within a 
     * configuration but typically only the top-level class is recognized in processing.
     * <p>
     *
     * @author Christopher K. Allen
     * @since Jan 2, 2024
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Root {
        
        
        /**
         * The environment variable or system property root name.
         * All environment variable names will be prefixed with the returned root string.
         * 
         * @return name prefix for all environment variables within this structure class.
         */
        public String   root() default "";
    }
    
    /**
     * <p>
     * Annotation indicating that that the current field of a structure class is a nested
     * substructure class.  
     * </p>
     * <p>
     * The marked nested structure contains a field (or additional substructure)
     * that can be overridden.  Specifically, this annotation is used for a structure class field 
     * representing a nested data structure containing a field that is annotated with 
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
        
        /**
         * Extension or path element of the environment variable or system property that identifies
         * the sub structure within the configuration structure class.
         *   
         * @return path element for this substructure within the structure class
         */
        public String pathelem() default "";
    }
    
    
    /**
     * Annotation for final fields of a structure class that can be overridden 
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
         * Name identifier of the environment variable or system property used to override this configuration 
         * field value.
         * 
         * @return  environment variable name containing new value for field
         */
        public String name() default "";
    }

}
