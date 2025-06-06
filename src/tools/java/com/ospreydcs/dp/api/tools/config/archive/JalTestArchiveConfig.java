/*
 * Project: dp-api-common
 * File:    JalToolsConfig.java
 * Package: com.ospreydcs.dp.api.tools.config
 * Type:    JalTestArchiveConfig
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
package com.ospreydcs.dp.api.tools.config.archive;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure class defining Data Platform test archive parameters.
 * </p>
 */
@ACfgOverride.Root(root="JAL_TEST_ARCHIVE")
public class JalTestArchiveConfig extends CfgStructure<JalTestArchiveConfig>{

    /** Default constructor required for base class */
    public JalTestArchiveConfig() { super(JalTestArchiveConfig.class); }
    
    /** The first timestamp of all data sources within the Data Platform test archive, i.e., the archive inception instant */
    @ACfgOverride.Field(name="FIRST_TIMESTAMP")
    public String       firstTimestamp;
    
    /** The last timestamp of all data sources within the Data Platform test archive */
    @ACfgOverride.Field(name="LAST_TIMESTAMP")
    public String       lastTimestamp; 
    
    /** The prefix given to all data sources within the Data Platform test archive; actual names are appended with their index */
    @ACfgOverride.Field(name="PV_PREFIX")
    public String       pvPrefix;
    
    /** The total number of all data sources within the Data Platform test archive */
    @ACfgOverride.Field(name="PV_COUNT_TOTAL")
    public Integer      pvCountTotal;
    
    /** The number of data sources within the Data Platform test archive that are sampled with a uniform sampling clock */
    @ACfgOverride.Field(name="PV_COUNT_CLOCK")
    public Integer      pvCountClock;
    
    /** The number of data sources within the Data Platform test archive that are sampled against an explicit timestamp list */
    @ACfgOverride.Field(name="PV_COUNT_TMS_LIST")
    public Integer      pvCountTmsList;
    
    /** 
     * The index of the first data source using a uniform sampling clock
     * <p>
     * Sampling clock PVs are named {pvPrefix+pvIndexStartClock, ..., pvPrefix+(pvIndexStartClock+pvCountClock)} 
     */
    @ACfgOverride.Field(name="PV_INDEX_START_CLOCK")
    public Integer      pvIndexStartClock;
    
    /** 
     * The index of the first data source using an explicit timestamp list
     * <p>
     * Timestamp list PVs are named {pvPrefix+pvIndexStartTmsList, ..., pvPrefix+(pvIndexStartTmsList+pvCountTmsList)} 
     */
    @ACfgOverride.Field(name="PV_INDEX_START_TMS_LIST")
    public Integer      pvIndexStartTmsList;
}