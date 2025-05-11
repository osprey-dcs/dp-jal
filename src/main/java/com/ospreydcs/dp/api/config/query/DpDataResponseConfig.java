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
    
    /** Query response data correlation parameters */
    @ACfgOverride.Struct(pathelem="CORRELATE")
    public Correlate        correlate;
    
    /** Multi-streaming parameters for query responses */
    @ACfgOverride.Struct(pathelem="MULTISTREAM")
    public Multistream      multistream;

    
    /**
     * Structure class defining default configuration parameters for query response data correlation
     */
    @ACfgOverride.Root(root="DP_API_QUERY_DATA_RESPONSE_CORRELATE")
    public static class Correlate extends CfgStructure<Correlate> {
        
        /** Default constructor required for base structure class */
        public Correlate() { super(Correlate.class); };
        
        
        //
        // Configuration Fields
        //
        
        /** Use concurrency during query data correlation */
        @ACfgOverride.Field(name="CONCURRENCY")
        public Boolean  useConcurrency;
        
        /** Correlate query data while gRPC streaming (otherwise after stream completion) */
        @ACfgOverride.Field(name="WHILE_STREAMING")
        public Boolean  whileStreaming;
    }
    
    /**
     *  Structure class defining default configuration parameters for multi-streaming data responses.
     */
    @ACfgOverride.Root(root="DP_API_QUERY_DATA_RESPONSE_MULTISTREAM")
    public static class Multistream extends CfgStructure<Multistream> {

        /** Default constructor required for base structure class */
        public Multistream() { super(Multistream.class); }
        
        
        //
        // Configuration Fields
        //
        
        /** use multiple gRPC data stream for request recovery */
        @ACfgOverride.Field(name="ENABLED")
        public Boolean      enabled;
        
        /** Maximum number of gRPC data stream to use for request recovery */
        @ACfgOverride.Field(name="MAX_STREAMS")
        public Integer      maxStreams;
        
        /** Query domain size (dimensions of sources-time) activating multiple streaming of responses */
        @ACfgOverride.Field(name="DOMAIN_SIZE")
        public Long         sizeDomain;
        
//        /** Time units used for pivotSize */
//        @ACfgOverride.Field(name="PIVOT_PERIOD")
//        public TimeUnit     pivotPeriod;
    }
    
}
