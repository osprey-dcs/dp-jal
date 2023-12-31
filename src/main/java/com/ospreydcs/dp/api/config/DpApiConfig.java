/*
 * Project: dp-api-common
 * File:	DpApiConfig.java
 * Package: com.ospreydcs.dp.api.config
 * Type: 	DpApiConfig
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
 * @since Dec 21, 2023
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import com.ospreydcs.dp.api.config.grpc.DpConnections;
import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgOverrideUtility;
import com.ospreydcs.dp.api.config.query.DpQuery;
import com.ospreydcs.dp.api.config.model.CfgLoaderYaml;

/**
 * <p>
 * Active structure class containing default configuration parameters for Java-based
 * Data Platform API libraries.
 * </p>
 * <p>
 * The class maintains a singleton instance containing all the default configuration parameters
 * as attributes.  The instance is created and populated upon first access using method 
 * {@link #getInstance()}.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li><b>WARNING</b> - all attributes have public accessibility.  Modififying attribute values
 *     affects all systems using these parameters.
 * </li>
 * <li>Default configuration values are contained in the YAML file indicated by {@link #STR_CFG_FILE}.
 * </li>
 * <li>Values within the YAML file can be overridden by supported environment variables as marked by
 *     the <code>ACfgOverride</code> annotation.
 * </li>
 * </ul> 
 * 
 * @author Christopher K. Allen
 * @since Dec 21, 2023
 *
 */
@ACfgOverride
public final class DpApiConfig {
    
    //
    // Application Resources
    //
    
    /** Location of the application properties file (relative path and name) */
    public static final String      STR_CFG_FILE = "dp-api-config.yml";
    
    
    // 
    // Class Resources
    //
    
    /** Singular instance of this class */
    private static DpApiConfig      cfgInstance = null;
    
    /** The class logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    /**
     * <p>
     * Returns the singleton instance of the <code>AppProperties</code> class.  
     * </p>
     * <p>
     * If this is the first call to this method the instance is created using the
     * <code>PropertiesLoader</code> utility class.  The properties loader reads
     * the properties file, creates the instance, and populates it.
     * Any properties that are annotated as override capable (using the 
     * <code>AOverrideCapable</code> annotation class) are then overridden
     * with any environment variables that have been set.
     * </p>
     * 
     * @return singleton instance of AppProperties containing initialization parameters
     *          for application
     */
    public static DpApiConfig getInstance() {
        if (DpApiConfig.cfgInstance == null) {
            try {
                DpApiConfig.cfgInstance = CfgLoaderYaml.load(STR_CFG_FILE, DpApiConfig.class);
                
                CfgOverrideUtility.envOverride(DpApiConfig.cfgInstance);
                
            } catch (FileNotFoundException e) {
                LOGGER.error("Unable to load properties from file: {}", STR_CFG_FILE);
                LOGGER.error("  Irrecoverable error. Exiting...");
                
                System.exit(1);
                
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOGGER.error("The {} class was not properly annotated for property overrides", DpApiConfig.class.getName());
                LOGGER.error("  Cause: ", e.getClass().getName());
                LOGGER.error("  Message: {}", e.getMessage());
                LOGGER.error("  Irrecoverable error. Exiting...");

                System.exit(1);
            }
                
        }
        
        return DpApiConfig.cfgInstance;
    }
    

    //
    // Configuration Parameters
    //
    
    /** Data Platform data archive parameters */
    @ACfgOverride.Struct
    public DpArchive    archive;
    
    /** Data Platform default Query Service parameters */
    @ACfgOverride.Struct
    public DpQuery      query;
    
    /** Data Platform services connection parameters */
    @ACfgOverride.Struct
    public DpConnections connections;
    
    /**
     * Structure containing Data Platform archive parameters.
     *
     */
    public static final class DpArchive {
        
        /** Archive inception date (earliest possible timestamp) */
        @ACfgOverride.Field(name="DP_ARCHIVE_INCEPTION")
        public String   inception;

        // 
        // Object Overrides
        //
        
        /**
         *
         * @see @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            
            // Cast comparison object
            DpArchive arc;
            if (obj instanceof DpArchive)
                arc = (DpArchive)obj;
            else
                return false;
            
            // Check equivalence
            return arc.inception.equals(this.inception);
        }
    }

    
    
    
    //
    // Object Overrides
    //

    /**
     *
     * @see @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        // Cast comparison object
        DpApiConfig cfg;
        if (obj instanceof DpApiConfig)
            cfg = (DpApiConfig)obj;
        else
            return false;
        
        // Check equivalence
        return cfg.archive.equals(this.archive) &&
                cfg.query.equals(this.query) &&
                cfg.connections.equals(this.connections);
    }

    /**
     *
     * @see @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        Yaml    yml = new Yaml();
        String  str = yml.dump(this);
        
        return str;
    }
}
