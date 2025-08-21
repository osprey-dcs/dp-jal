/*
 * Project: dp-api-common
 * File:    JalTestArchivePvsConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config
 * Type:    JalTestArchivePvsConfig
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
package com.ospreydcs.dp.jal.tools.config.archive;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.common.DpSupportedType;
import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing parameters for all Process Variables within the Data Platform Test Archive.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jun 10, 2025
 */
public class JalTestArchivePvsConfig extends CfgStructure<JalTestArchivePvsConfig> {
    
    /** Required default constructor for base class */
    public JalTestArchivePvsConfig() { super(JalTestArchivePvsConfig.class); };
    
    
    //
    // Structure Fields
    //
    
    /** Prefix given to all Process Variable names in test archive */
    @ACfgOverride.Field(name="PREFIX")
    public String                   prefix;
    
    /** Data types used for Process Variables */
    @ACfgOverride.Field(name="TYPES")
    public List<DpSupportedType>    types;
    
    /** Sampling clock parameters for all Process Variables in the test archive */
    @ACfgOverride.Struct(pathelem="CLOCK")
    public SampleClock              clock;
    
    /** The number and distribution of Test Archive PVs with respect to their timestamps */
    @ACfgOverride.Struct(pathelem="COUNT")
    public PvTmsCounts              count;
    
    /** The (0-based Java) indexes of the Test Archive PVs with respect to their timestamps */
    @ACfgOverride.Struct(pathelem="INDEXES")
    public PvTmsIndexes             indexes;
    
    
    //
    // Internal Structure Classes
    //
    
    /**
     * Structure class containing sampling clock parameters for all Test Archive PVs
     */
    public static class SampleClock extends CfgStructure<SampleClock> {
        
        /** Required default constructor for base class */
        public SampleClock() { super(SampleClock.class); };
        
        
        //
        // Structure Fields
        //
        
        /** Sample clock period */
        @ACfgOverride.Field(name="PERIOD")
        public Long         period;
        
        /** Sample clock period time units */
        @ACfgOverride.Field(name="UNITS")
        public TimeUnit     units;
    }
    
    /**
     * Structure class containing the number and distribution PVs with respect to timestamps used
     */
    public static class PvTmsCounts extends CfgStructure<PvTmsCounts> {
        
        /** Default constructor required for base class support */
        public PvTmsCounts() { super(PvTmsCounts.class); };
        
        
        //
        // Structure Fields
        //
        
        /** Total number of PVs within the Test Archive data set */
        @ACfgOverride.Field(name="TOTAL")
        public Integer      total;
        
        /** Number of PVs sampled with a uniform sampling clock */
        @ACfgOverride.Field(name="CLOCKED")
        public Integer      clocked;
        
        /** Number of PVs sampled with an explicit timestamp list */
        @ACfgOverride.Field(name="TMS_LIST")
        public Integer      tmsList;
    }
    
    /**
     * Structure class containing the Test Archive PV indexes with respect to timestamps used 
     */
    public static class PvTmsIndexes extends CfgStructure<PvTmsIndexes> {
        
        /** Default constructor required for base class */
        public PvTmsIndexes() { super(PvTmsIndexes.class); };
        
        
        //
        // Structure Fields
        //
        
        /** Start index for the first PV with a uniform sampling clock (0-based Java index) */
        @ACfgOverride.Field(name="CLOCKED")
        public Integer      clocked;
        
        /** Start index for the first PV using an explicit timestamp list (0-based Java index) */
        @ACfgOverride.Field(name="TMS_LIST")
        public Integer      tmsList;
    }
}