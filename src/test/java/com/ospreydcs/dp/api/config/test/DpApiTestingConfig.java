/*
 * Project: dp-api-common
 * File:	DpApiTestingConfig.java
 * Package: com.ospreydcs.dp.api.config.test
 * Type: 	DpApiTestingConfig
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
package com.ospreydcs.dp.api.config.test;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.common.DpTimeoutConfig;
import com.ospreydcs.dp.api.config.grpc.GrpcConnectionConfig;
import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgLoaderYaml;
import com.ospreydcs.dp.api.config.model.CfgOverrideUtility;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Contains configuration and common parameters for Data Platform API library testing.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Jan 13, 2024
 *
 */
@ACfgOverride.Root(root="DP_API_TEST")
public class DpApiTestingConfig extends CfgStructure<DpApiTestingConfig> {
    
    
    //
    // Application Resources
    //
    
    /** File name of the Data Platform testing configuration file */
    public static final String  STR_CFG_FILE_NAME = "dp-api-test-config.yml";
    

    // 
    // Class Resources
    //
    
    /** Singular instance of this class */
    private static DpApiTestingConfig   cfgInstance = null;
    
    /** The class logger */
    private static final Logger         LOGGER = LogManager.getLogger();
    
    
    /**
     * <p>
     * Returns the singleton instance of the <code>DpApiTestingConfig</code> class.  
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
     * @return singleton instance of <code>DpApiTestingCnofig</code> containing initialization parameters
     *          for application
     */
    public static DpApiTestingConfig getInstance() {
        if (DpApiTestingConfig.cfgInstance == null) {
            try {
                DpApiTestingConfig.cfgInstance = CfgLoaderYaml.load(STR_CFG_FILE_NAME, DpApiTestingConfig.class);
                
                CfgOverrideUtility.overrideRoot(DpApiTestingConfig.cfgInstance, CfgOverrideUtility.SOURCE.ENVIRONMENT);
                CfgOverrideUtility.overrideRoot(DpApiTestingConfig.cfgInstance, CfgOverrideUtility.SOURCE.PROPERTIES);
                
            } catch (FileNotFoundException e) {
                LOGGER.error("Unable to load properties from file: {}", STR_CFG_FILE_NAME);
                LOGGER.error("  Irrecoverable error. Exiting...");
                
                System.exit(1);
                
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOGGER.error("The {} class was not properly annotated for property overrides", DpApiTestingConfig.class.getName());
                LOGGER.error("  Cause: ", e.getClass().getName());
                LOGGER.error("  Message: {}", e.getMessage());
                LOGGER.error("  Irrecoverable error. Exiting...");

                System.exit(1);
            }
                
        }
        
        return DpApiTestingConfig.cfgInstance;
    }
    

    
    //
    // Configuration Parameters
    //
    
    @ACfgOverride.Struct(pathelem="ARCHIVE")
    public TestArchive      testArchive;

    @ACfgOverride.Struct(pathelem="QUERY")
    public TestQuery        testQuery;
    
    
    
    
    /**
     * Defines test archive parameters
     *
     */
    @ACfgOverride.Root(root="DP_API_TEST_ARCHIVE")
    public static class TestArchive extends CfgStructure<TestArchive>{

        /** Default constructor required for base class */
        public TestArchive() {
            super(TestArchive.class);
        }
        
        @ACfgOverride.Field(name="INCEPTION")
        public Long         inception;
        
        @ACfgOverride.Field(name="DURATION")
        public Long         duration; 
        
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
        public Data                 data;
     
        @ACfgOverride.Struct(pathelem="TIMEOUT_SHORT")
        public DpTimeoutConfig      timeoutShort;
        
        @ACfgOverride.Struct(pathelem="TIMEOUT_LONG")
        public DpTimeoutConfig      timeoutLong;
        
        @ACfgOverride.Struct(pathelem="CONNECTION")
        public GrpcConnectionConfig connection;
        
        
        /**
         * Defines test query data parameters
         */
        public static class Data extends CfgStructure<Data> {

            /** Default constructor required for base class */
            public Data() {
                super(Data.class);
            }
         
            @ACfgOverride.Field(name="PERSISTENCE")
            public  String  persistence;
        }
    }
    
    
    /** Default constructor required for base class */
    public DpApiTestingConfig() {
        super(DpApiTestingConfig.class);
    }
}
