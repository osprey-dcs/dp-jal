/*
 * Project: dp-api-common
 * File:	DpDataRecoveryConfig.java
 * Package: com.ospreydcs.dp.api.config.query
 * Type: 	DpDataRecoveryConfig
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

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.config.common.DpConcurrencyConfig;
import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing default parameters for Data Platform Query Service time-series data recovery.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Feb 7, 2024
 *
 */
@ACfgOverride.Root(root="DP_API_QUERY_DATA_RECOVERY")
public class DpDataRecoveryConfig extends CfgStructure<DpDataRecoveryConfig> {

    /** Default constructor required for base structure class */
    public DpDataRecoveryConfig() { super(DpDataRecoveryConfig.class); };
    
    //
    // Configuration Fields
    //
    
    /** gRPC streaming parameters for query responses */
    @ACfgOverride.Struct(pathelem="STREAM")
    public Stream                   stream;
    
    /** Multi-streaming parameters for query responses */
    @ACfgOverride.Struct(pathelem="MULTISTREAM")
    public Multistream      multistream;

    /** Query response data correlation parameters */
    @ACfgOverride.Struct(pathelem="CORRELATE")
    public Correlate        correlate;
    
    
    /**
     * Structure class defining default configuration parameters for streaming data responses. 
     */
    @ACfgOverride.Root(root="DP_API_QUERY_DATA_REQUEST_STREAM")
    public static class Stream extends CfgStructure<Stream> {
        
        /** Default constructor required for base structure class */
        public Stream() { super(Stream.class); };
        
//      /** Default bucket count per page when using cursor requests */
//      @AUnavailable(status=STATUS.UNDER_REVIEW)
//      @ACfgOverride.Field(name="PAGE_SIZE")
//      public Integer              pageSize;
        
        /** Preference for gRPC data stream type */
        @ACfgOverride.Field(name="PREFERRED")
        public DpGrpcStreamType   preferred;
        
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
    
    /**
     * Structure class defining default configuration parameters for query response data correlation
     */
    @ACfgOverride.Root(root="DP_API_QUERY_DATA_RECOVERY_CORRELATE")
    public static class Correlate extends CfgStructure<Correlate> {
        
        /** Default constructor required for base structure class */
        public Correlate() { super(Correlate.class); };
        
        
        //
        // Configuration Fields
        //
        
        /** Correlate raw time-series data while gRPC streaming (otherwise after stream completion) */
        @ACfgOverride.Field(name="WHILE_STREAMING")
        public Boolean              whileStreaming;
        
        /** Raw time-series data correlation concurrency parameters (multi-threaded correlation) */
        @ACfgOverride.Struct(pathelem="CONCURRENCY")
        public DpConcurrencyConfig  concurrency;
    }
    
}
