/*
 * Project: dp-api-common
 * File:	JalDataRequestConfig.java
 * Package: com.ospreydcs.dp.api.config.query
 * Type: 	JalDataRequestConfig
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
 * @since Feb 5, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config.query;

import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;

/**
 * <p>
 * Structure class containing default parameters for Data Platform Query Service time-series data requests.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Feb 5, 2024
 *
 */
@ACfgOverride.Root(root="DP_API_QUERY_DATA_REQUEST")
public class JalDataRequestConfig extends CfgStructure<JalDataRequestConfig> {

    /** Default constructor required for base structure class */
    public JalDataRequestConfig() { super(JalDataRequestConfig.class); }

    
    //
    // Configuration Fields
    //
    
    /** Composite data query parameters */
    @ACfgOverride.Struct(pathelem="DECOMPOSE")
    public CompositeConfig          decompose;
    

    /**
     *  Structure class defining default configuration parameters for decompose queries. 
     */
    public static class CompositeConfig extends CfgStructure<CompositeConfig> {
        
        /** Default constructor required for base structure class */
        public CompositeConfig() { super(CompositeConfig.class);  }

        
        //
        // Configuration Fields
        //
        
        /** Is decompose query decomposition enabled */
        @ACfgOverride.Field(name="ENABLED")
        public Boolean              enabled;
        
        /** The preferred decomposition strategy */
        @ACfgOverride.Field(name="PREFERRED")
        public RequestDecompType    preferred;
        
        /** Maximum number of data sources per query - query domain horizontal "width" */
        @ACfgOverride.Field(name="MAX_SOURCES")
        public Integer              maxSources;
        
        /** Maximum duration of the time range - query domain vertical "length" */
        @ACfgOverride.Field(name="MAX_DURATION")
        public long                 maxDuration;
        
        /** Maximum duration time units */
        @ACfgOverride.Field(name="DURATION_UNIT")
        public TimeUnit             durationUnit;
        
    }
    
}
