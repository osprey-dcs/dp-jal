/*
 * Project: dp-api-common
 * File:	DpDataResponseConfig.java
 * Package: com.ospreydcs.dp.api.config.query
 * Type: 	DpDataResponseConfig
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
 * @since Feb 7, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config.query;

import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing default parameters for Data Platform Query Service time-series data responses.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Feb 7, 2024
 *
 */
@ACfgOverride.Root(root="DP_API_QUERY_DATA_RESPONSE")
public class DpDataResponseConfig extends CfgStructure<DpDataResponseConfig> {

    /** Default constructor required for base structure class */
    public DpDataResponseConfig() { super(DpDataResponseConfig.class); };
    
    //
    // Configuration Fields
    //
    
    /** Multi-streaming parameters for query responses */
    @ACfgOverride.Struct(pathelem="MULTISTREAM")
    public Multistream      multistream;
    
    
    /**
     *  Structure class defining default configuration parameters for multi-streaming data responses.
     */
    @ACfgOverride.Root(root="DP_API_QUERY_")
    public static class Multistream extends CfgStructure<Multistream> {

        /** Enumeration of data stream type preferences */
        public static enum StreamType {
            /** Unidirectional stream - backward stream from Query Service to client */
            UNIDIRECTIONAL,

            /** Bidirectional data stream */ 
            BIDIRECTIONAL;
        }
        
        /** Default constructor required for base structure class */
        public Multistream() { super(Multistream.class); }
        
        
        //
        // Configuration Fields
        //
        
        /** use multiple gRPC data stream for request recovery */
        @ACfgOverride.Field(name="ACTIVE")
        public Boolean      active;
        
        /** Query domain size (dimensions of sources-time) activating multiple streaming of responses */
        @ACfgOverride.Field(name="PIVOT_SIZE")
        public Long         pivotSize;
        
        /** Time units used for pivotSize */
        @ACfgOverride.Field(name="PIVOT_UNITS")
        public TimeUnit     pivotUnits;
        
        /** Maximum number of gRPC data stream to use for request recover */
        @ACfgOverride.Field(name="MAX_STREAMS")
        public Integer      maxStreams;
        
        /** Preference for gRPC data stream type */
        @ACfgOverride.Field(name="PREFERENCE")
        public StreamType   preference;
    }
    
}
