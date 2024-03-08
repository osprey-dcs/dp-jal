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

import com.ospreydcs.dp.api.config.grpc.DpConnectionsConfig;
import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgOverrideUtility;
import com.ospreydcs.dp.api.config.model.CfgStructure;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
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
 * Default configuration parameters are taken from the YAML file identified by class
 * constant <code>{@link #STR_CFG_FILE}</code> with current value {@value #STR_CFG_FILE}.
 * The file is parsed and the values are used to populate the fields of the structure class
 * singleton instance.
 * <p>
 * The structure class is based upon the configuration model used in the DP API project.
 * System variables can override the default parameter values as prescribed in the YAML default
 * configuration file.  System variables are first taken from the environment, then the
 * command line (Java System properties).  For more information on the configuration model
 * see the documentation for annotation <code>{@link ACfgOverride}</code>.
 * </p> 
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li><b>WARNING</b> - all attributes have public accessibility.  Modifying attribute values
 *     affects all systems using these parameters.
 * </li>
 * <li>Default configuration values are contained in the YAML file indicated by {@link #STR_CFG_FILE}.
 * </li>
 * <li>Values within the YAML file can be overridden by supported system variables as marked by
 *     the <code>{@link ACfgOverride}</code> annotations.
 * </li>
 * </ul> 
 * 
 * @author Christopher K. Allen
 * @since Dec 21, 2023
 *
 * @see CfgLoaderYaml
 * @see CfgOverrideUtility
 * @see CfgStructure
 * @see ACfgOverride
 * @see DpConnectionsConfig
 */
@ACfgOverride.Root(root="DP_API")
public final class DpApiConfig extends CfgStructure<DpApiConfig> {
    
    
    //
    // Application Resources
    //
    
    /** Location of the application properties file (relative path and name) */
    public static final String      STR_CFG_FILE = "dp-api-config.yml";
//    public static final String      STR_CFG_FILE = "src/main/resources/dp-api-config.yml";
    
    
    // 
    // Class Resources
    //
    
    /** Singular instance of this class */
    private static DpApiConfig      cfgInstance = null;
    
    /** The class logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    /**
     * <p>
     * Returns the singleton instance of the <code>DpApiConfig</code> class.  
     * </p>
     * <p>
     * If this is the first call to this method the instance is created using the
     * <code>CfgLoaderYaml</code> utility class.  The properties loader reads
     * the properties file, creates the instance, and populates it.
     * Any properties that are annotated as override capable (using the 
     * <code>ACfgOverride</code> annotation class) are then overridden
     * with any environment variables that have been set.
     * </p>
     * 
     * @return singleton instance of DpApiCnofig containing initialization parameters
     *          for application
     */
    public static DpApiConfig getInstance() {
        if (DpApiConfig.cfgInstance == null) {
            try {
                DpApiConfig.cfgInstance = CfgLoaderYaml.load(STR_CFG_FILE, DpApiConfig.class);
                
                CfgOverrideUtility.overrideRoot(DpApiConfig.cfgInstance, CfgOverrideUtility.SOURCE.ENVIRONMENT);
                CfgOverrideUtility.overrideRoot(DpApiConfig.cfgInstance, CfgOverrideUtility.SOURCE.PROPERTIES);
                
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
    @ACfgOverride.Struct(pathelem="ARCHIVE")
    public DpArchiveConfig    archive;
    
    /** Data Platform default Query Service parameters */
    @ACfgOverride.Struct(pathelem="QUERY")
    public DpQueryConfig      query;
    
    /** Data Platform services connection parameters */
    @ACfgOverride.Struct(pathelem="CONNECTION")
    public DpConnectionsConfig connections;
    
    /**
     * Structure containing Data Platform archive configuration parameters.
     *
     */
    @ACfgOverride.Root(root="DP_API_ARCHIVE")
    public static final class DpArchiveConfig extends CfgStructure<DpArchiveConfig> {
        
        /** Default constructor require for base structure class */
        public DpArchiveConfig() {
            super(DpArchiveConfig.class);
        }

        /** TestArchive inception date (earliest possible timestamp) */
        @ACfgOverride.Field(name="INCEPTION")
        public String   inception;

    }

    
    //
    // Object Overrides
    //

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

    
    //
    // Private Methods
    //

    /**
     * <p>
     * Allow only internal construction of <code>DpApiConfig</code> instance.
     * </p>
     */
    public DpApiConfig() {
        super(DpApiConfig.class);
    }

}
