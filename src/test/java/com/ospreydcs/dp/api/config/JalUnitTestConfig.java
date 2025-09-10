/*
 * Project: dp-api-common
 * File:	JalUnitTestConfig.java
 * Package: com.ospreydcs.dp.api.config.test
 * Type: 	JalUnitTestConfig
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
 * @since Jan 13, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.common.JalTimeoutConfig;
import com.ospreydcs.dp.api.config.grpc.DpGrpcConnectionConfig;
import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgLoaderYaml;
import com.ospreydcs.dp.api.config.model.CfgOverrideUtility;
import com.ospreydcs.dp.api.config.model.CfgStructure;
import com.ospreydcs.dp.api.config.test.TestQueryDataConfig;

/**
 * <p>
 * Contains configuration and common parameters for Java API Library unit testing.
 * </p> 
 * <p>
 * The Java API Library tools configuration parameters are contained in the file with name given
 * by <code>{@link #STR_CFG_FILE_NAME}</code>.  The location of the file should be in the resources
 * sub-directory of the project unit testing directory.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 13, 2024
 *
 */
@ACfgOverride.Root(root="DP_API_TEST")
public class JalUnitTestConfig extends CfgStructure<JalUnitTestConfig> {
    
    
    //
    // Class Constants
    //
    
    /** Name of file containing the Java API Library unit testing configuration parameters */
    public static final String  STR_CFG_FILE_NAME = "dp-api-test-config.yml";
    

    // 
    // Class Resources
    //
    
    /** The class logger */
    private static final Logger         LOGGER = LogManager.getLogger();

    
    //
    // Class Attributes
    //
    
    /** Singleton instance of this class */
    private static JalUnitTestConfig   cfgInstance = null;
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Returns the singleton instance of the <code>JalUnitTestConfig</code> class.  
     * </p>
     * <p>
     * If this is the first call to this method, the instance <code>{@link #cfgInstance}</code> is created using the
     * <code>CfgLoaderYaml</code> utility class.  The properties loader reads the properties file, creates the instance, 
     * then populates it. Any properties that are annotated as override capable (using the <code>ACfgOverride</code> 
     * annotation class) are then overridden with any environment variables that have been set.
     * </p>
     * 
     * @return singleton instance of <code>DpApiUnitTestingConfig</code> containing initialization parameters
     *          for application
     */
    public static JalUnitTestConfig getInstance() {
        if (JalUnitTestConfig.cfgInstance == null) {
            try {
                JalUnitTestConfig.cfgInstance = CfgLoaderYaml.load(STR_CFG_FILE_NAME, JalUnitTestConfig.class);
                
                CfgOverrideUtility.overrideRoot(JalUnitTestConfig.cfgInstance, CfgOverrideUtility.SOURCE.ENVIRONMENT);
                CfgOverrideUtility.overrideRoot(JalUnitTestConfig.cfgInstance, CfgOverrideUtility.SOURCE.PROPERTIES);
                
            } catch (FileNotFoundException e) {
                LOGGER.error("Unable to load properties from file: {}", STR_CFG_FILE_NAME);
                LOGGER.error("  Irrecoverable error. Exiting...");
                
                System.exit(1);
                
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOGGER.error("The {} class was not properly annotated for property overrides", JalUnitTestConfig.class.getName());
                LOGGER.error("  Cause: ", e.getClass().getName());
                LOGGER.error("  Message: {}", e.getMessage());
                LOGGER.error("  Irrecoverable error. Exiting...");

                System.exit(1);
            }
                
        }
        
        return JalUnitTestConfig.cfgInstance;
    }
    

    
    //
    // Configuration Parameters
    //
    
    @ACfgOverride.Struct(pathelem="ARCHIVE")
    public TestArchive      testArchive;

    @ACfgOverride.Struct(pathelem="QUERY")
    public TestQuery        testQuery;
    
    
    /**
     * <p>
     * Structure class defining Data Platform test archive parameters.
     * </p>
     */
    @ACfgOverride.Root(root="DP_API_TEST_ARCHIVE")
    public static class TestArchive extends CfgStructure<TestArchive>{

        /** Default constructor required for base class */
        public TestArchive() {
            super(TestArchive.class);
        }
        
        @ACfgOverride.Field(name="FIRST_TIMESTAMP")
        public String       firstTimestamp;
        
        @ACfgOverride.Field(name="LAST_TIMESTAMP")
        public String       lastTimestamp; 
        
        @ACfgOverride.Field(name="PV_COUNT")
        public Integer      pvCount;
        
        @ACfgOverride.Field(name="PV_PREFIX")
        public String       pvPrefix;
    }
    

    /**
     * Defines test query parameters
     *
     */
    @ACfgOverride.Root(root="DP_APIT_TEST_QUERY")
    public static class TestQuery extends CfgStructure<TestQuery>{

        /** Default constructor required for base class */
        public TestQuery() {
            super(TestQuery.class);
        }
        
        @ACfgOverride.Struct(pathelem="DATA")
        public TestQueryDataConfig  data;
     
        @ACfgOverride.Struct(pathelem="TIMEOUT_SHORT")
        public JalTimeoutConfig      timeoutShort;
        
        @ACfgOverride.Struct(pathelem="TIMEOUT_LONG")
        public JalTimeoutConfig      timeoutLong;
        
        @ACfgOverride.Struct(pathelem="CONNECTION")
        public DpGrpcConnectionConfig connection;
    }
    
    
    /** Default constructor required for base class */
    public JalUnitTestConfig() {
        super(JalUnitTestConfig.class);
    }
}
