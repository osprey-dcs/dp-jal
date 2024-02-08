/*
 * Project: dp-api-common
 * File:	DpDataRequestConfig.java
 * Package: com.ospreydcs.dp.api.config.query
 * Type: 	DpDataRequestConfig
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
import com.ospreydcs.dp.api.query.model.DpQueryStreamType;

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
public class DpDataRequestConfig extends CfgStructure<DpDataRequestConfig> {

    /** Default constructor required for base structure class */
    public DpDataRequestConfig() { super(DpDataRequestConfig.class); }

    
    //
    // Configuration Fields
    //
    
    /** gRPC streaming parameters for query responses */
    @ACfgOverride.Struct(pathelem="STREAM")
    public Stream                   stream;
    
    /** Composite data query parameters */
    @ACfgOverride.Struct(pathelem="COMPOSITE")
    public CompositeQueryConfig     composite;
    

    /**
     * Structure class defining default configuration parameters for streaming data responses. 
     */
    @ACfgOverride.Root(root="DP_API_QUERY_DATA_REQUEST_STREAM")
    public static class Stream extends CfgStructure<Stream> {
        
//        /** Enumeration of data stream type preferences */
//        public static enum DpQueryStreamType {
//            /** Unidirectional stream - backward stream from Query Service to client */
//            UNIDIRECTIONAL,
//
//            /** Bidirectional data stream */ 
//            BIDIRECTIONAL;
//        }
        
        /** Default constructor required for base structure class */
        public Stream() { super(Stream.class); };
        
        
        /** Preference for gRPC data stream type */
        @ACfgOverride.Field(name="PREFERENCE")
        public DpQueryStreamType   preference;
    }
    
    /**
     *  Structure class defining default configuration parameters for composite queries. 
     */
    public static class CompositeQueryConfig extends CfgStructure<CompositeQueryConfig> {
        
        /** Default constructor required for base structure class */
        public CompositeQueryConfig() { super(CompositeQueryConfig.class);  }

        
        //
        // Configuration Fields
        //
        
        /** Is composite query decomposition active */
        @ACfgOverride.Field(name="ACTIVE")
        public Boolean      active;
        
        /** Maximum number of data sources per query - query domain horizontal "width" */
        @ACfgOverride.Field(name="MAX_SOURCES")
        public Integer      maxSources;
        
        /** Maximum duration of the time range - query domain vertical "length" */
        @ACfgOverride.Field(name="MAX_DURATION")
        public long         maxDuration;
        
        /** Maximum duration time units */
        @ACfgOverride.Field(name="UNIT_DURATION")
        public TimeUnit     unitDuration;
        
    }
    
}
