/*
 * Project: dp-api-common
 * File:	JalToolsConfig.java
 * Package: com.ospreydcs.dp.api.tools.config
 * Type: 	JalToolsConfig
 *
 * Copyright 2010-2025 the original author or authors.
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
 * @since May 4, 2025
 *
 */
package com.ospreydcs.dp.api.tools.config;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgLoaderYaml;
import com.ospreydcs.dp.api.config.model.CfgOverrideUtility;
import com.ospreydcs.dp.api.config.model.CfgStructure;
import com.ospreydcs.dp.api.tools.config.query.JalToolsQueryConfig;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Structure class containing configuration parameters for the Java API Library tools.
 * </p>
 * <p>
 * The Java API Library tools configuration parameters are contained in the file with name given
 * by <code>{@link #STR_CFG_FILE_NAME}</code>.  The location of the file should be in the resources
 * sub-directory of the project tools directory.
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 4, 2025
 *
 */
@ACfgOverride.Root(root="JAL_TOOLS")
public class JalToolsConfig extends CfgStructure<JalToolsConfig> {

    
    //
    // Class Constants
    //
    
    /** Name of file containing the Java API Library tools configuration parameters */
    public static final String  STR_CFG_FILE_NAME = "jal-tools-config.yml";
    

    // 
    // Class Resources
    //
    
    /** The class logger */
    private static final Logger         LOGGER = LogManager.getLogger();

    
    //
    // Class Attributes
    //
    
    /** Singleton instance of this class */
    private static JalToolsConfig   cfgInstance = null;

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Returns the singleton instance of the <code>JalToolsConfig</code> class.  
     * </p>
     * <p>
     * If this is the first call to this method, the instance <code>{@link #cfgInstance}</code> is created using the
     * <code>CfgLoaderYaml</code> utility class.  The properties loader reads the properties file, creates the instance, 
     * then populates it.  Any properties that are annotated as override capable (using the <code>ACfgOverride</code> 
     * annotation class) are then overridden with any environment variables that have been set.
     * </p>
     * 
     * @return singleton instance of <code>JalToolsConfig</code> containing initialization parameters
     *          for application
     */
    public static JalToolsConfig getInstance() {
        if (JalToolsConfig.cfgInstance == null) {
            try {
//                // TODO - Remove
//                String strCfgPath = JalToolsConfig.class.getClassLoader().getResource(STR_CFG_FILE_NAME).getPath();
//                System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + " called from " + JavaRuntime.getQualifiedCallerName());
//                System.out.println("  The location of file " + STR_CFG_FILE_NAME + " is " + strCfgPath);
//                
//                ClassLoader ldrClass = CfgLoaderYaml.class.getClassLoader();
//                strCfgPath = ldrClass.getResource(STR_CFG_FILE_NAME).getPath();
//                System.out.println("  The location as found from " + ldrClass.getName() + " is " + strCfgPath);
//                // -------
//                
//                JalToolsConfig.cfgInstance = CfgLoaderYaml.load(strCfgPath, JalToolsConfig.class);
                JalToolsConfig.cfgInstance = CfgLoaderYaml.load(STR_CFG_FILE_NAME, JalToolsConfig.class);
                
                CfgOverrideUtility.overrideRoot(JalToolsConfig.cfgInstance, CfgOverrideUtility.SOURCE.ENVIRONMENT);
                CfgOverrideUtility.overrideRoot(JalToolsConfig.cfgInstance, CfgOverrideUtility.SOURCE.PROPERTIES);
                
            } catch (FileNotFoundException e) {
                LOGGER.error("Unable to load properties from file: {}", STR_CFG_FILE_NAME);
                LOGGER.error("  Irrecoverable error. Exiting...");
                
                System.exit(1);
                
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOGGER.error("The {} class was not properly annotated for property overrides", JalToolsConfig.class.getName());
                LOGGER.error("  Cause: ", e.getClass().getName());
                LOGGER.error("  Message: {}", e.getMessage());
                LOGGER.error("  Irrecoverable error. Exiting...");

                System.exit(1);
            }
                
        }
        
        return JalToolsConfig.cfgInstance;
    }
    
    /** Default constructor required for super class */
    public JalToolsConfig() { super(JalToolsConfig.class); }

    
    //
    // Configuration Parameters
    //
    
//    /** Default output locations of Java API Library tools */
//    @ACfgOverride.Struct(pathelem="OUTPUT")
//    public JalQueryOutputConfig   output;
    
    /** Default configuration parameters for Java API Library query service tools */
    @ACfgOverride.Struct(pathelem="QUERY")
    public JalToolsQueryConfig    query;
    

}
