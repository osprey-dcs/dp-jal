/*
 * Project: dp-api-common
 * File:	DpDataTableConfig.java
 * Package: com.ospreydcs.dp.api.config.query
 * Type: 	DpDataTableConfig
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
 * @since Feb 18, 2025
 *
 */
package com.ospreydcs.dp.api.config.query;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing default configuration parameters for Query Service response recovery tables.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 18, 2025
 *
 */
@ACfgOverride.Root(root="DP_API_QUERY_DATA_TABLE")
public class DpDataTableConfig extends CfgStructure<DpDataTableConfig> {

    /** Default constructor required for base structure class */
    public DpDataTableConfig() { super(DpDataTableConfig.class); }

    
    // 
    // Configuration Fields
    //
    
    @ACfgOverride.Struct(pathelem="CONSTRUCTION")
    public Construction  construction;
    
    /** Default configuration parameters for time-series query results static data tables */ 
    @ACfgOverride.Struct(pathelem="STATIC")
    public Static       sstatic;
    
    /** Default configuration parameters for time-series query results dynamic data tables */ 
    @ACfgOverride.Struct(pathelem="DYNAMIC")
    public Dynamic      dynamic;
    
    @ACfgOverride.Root(root="DP_API_QUERY_DATA_TABLE_RECONSTRUCT")
    public static class Construction extends CfgStructure<Construction> {
        
        /** Default constructor required for base structure class */
        public Construction() { super(Construction.class); }
        
        /** Use advanced error checking and verification */
        @ACfgOverride.Field(name="ERROR_CHECKING")
        public Boolean      errorChecking;
        
        /** Enable/disable time domain collision within correlated blocks */
        @ACfgOverride.Field(name="DOMAIN_COLLISION")
        public Boolean      domainCollision;
    }
    
    @ACfgOverride.Root(root="DP_API_QUERY_DATA_TABLE_STATIC")
    public static class Static extends CfgStructure<Static> {

        /** Default constructor required for base structure class */
        public Static() { super(Static.class); }

        /** Is static data table the default preference */
        @ACfgOverride.Field(name="DEFAULT")
        public Boolean      isDefault;
        
        /** Do static data tables have a maximum size limit */
        @ACfgOverride.Field(name="HAS_MAX_SIZE")
        public Boolean      hasMaxSize;
        
        /** Maximum allowable size (in bytes) of a static data table */
        @ACfgOverride.Field(name="MAX_SIZE")
        public Integer      maxSize;
    }
    
    @ACfgOverride.Root(root="DP_API_QUERY_DATA_TABLE_DYNAMIC")
    public static class Dynamic extends CfgStructure<Dynamic> {
        
        /** Default constructor required for base class */
        public Dynamic() { super(Dynamic.class); };
        
        /** Enable dynamic data tables */
        @ACfgOverride.Field(name="ENABLE")
        public Boolean      enable;
    }
}
