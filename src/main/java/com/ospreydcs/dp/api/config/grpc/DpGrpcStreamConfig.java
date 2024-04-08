/*
 * Project: dp-api-common
 * File:	DpGrpcStreamConfig.java
 * Package: com.ospreydcs.dp.api.config.grpc
 * Type: 	DpGrpcStreamConfig
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
 * @since Feb 20, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config.grpc;

import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;
import com.ospreydcs.dp.api.config.query.DpDataResponseConfig.Multistream;
import com.ospreydcs.dp.api.model.DpGrpcStreamType;

/**
 * <p>
 * Structure class containing general parameters for gRPC data stream configuration.
 * </p>
 * <p>
 * gRPC supports a "data streaming" extension to the standard RPC model.  Rather than simply exchange messages
 * a streaming gRPC operation establishes a persistent "data stream" between client and service where multiple 
 * messages can be exchanged.  The configuration of the data stream is highly context dependent, particular to
 * the supported operation between client and service.
 * </p>
 * <p>
 * The parameters within this structure refer to general implementation considerations and may not apply in
 * the general case.
 * </p>
 *  
 * @author Christopher K. Allen
 * @since Feb 20, 2024
 *
 */
@ACfgOverride.Root(root="DP_API_STREAM")    // Note this is just for show
public class DpGrpcStreamConfig extends CfgStructure<DpGrpcStreamConfig> {

    /** Default constructor required for base class */
    public DpGrpcStreamConfig() { super(DpGrpcStreamConfig.class); }
    
    
    // 
    // Configuration Parameters
    //
    
    /** Are stream configuration parameters used */
    @ACfgOverride.Field(name="ACTIVE")
    public Boolean              active;
    
    /** preferred gRPC stream type */
    @ACfgOverride.Field(name="TYPE")
    public DpGrpcStreamType     type;

    /** gRPC stream data buffering parameters */
    @ACfgOverride.Struct(pathelem="BUFFER")
    public DataBuffer           buffer;
    
    /** gRPC stream data binning parameters */
    @ACfgOverride.Struct(pathelem="BINNING")
    public DataBinning          binning;
    
    /** gRPC multiple, concurrent data streams parameters */
    @ACfgOverride.Struct(pathelem="CONCURRENCY")
    public Concurrency          concurrency;
    
    
    /**
     * Structure class containing parameters for gRPC stream data buffering.
     */
    public static final class DataBuffer extends CfgStructure<DataBuffer> {

        /** Default constructor required for base class */
        public DataBuffer() { super(DataBuffer.class);  }
        
        // 
        // Configuration Parameters
        //
        
        /** Is data buffering active */
        @ACfgOverride.Field(name="ACTIVE")
        public Boolean      active;
        
        /** Size for fixed-length queue buffer (e.g., a blocking queue for back pressure) */
        @ACfgOverride.Field(name="SIZE")
        public Integer      size;

        /** Enable the existence of "back pressure" within gRPC streams */
        @ACfgOverride.Field(name="BACKPRESSURE")
        public Boolean      backPressure;
        
    }
    
    /**
     * Structure class containing parameters for gRPC stream data binning.
     */
    public static final class DataBinning extends CfgStructure<DataBinning> {

        /** Default constructor required for base class */
        public DataBinning() { super(DataBinning.class);  }
        
        // 
        // Configuration Parameters
        //
        
        /** Is data binning active */
        @ACfgOverride.Field(name="ACTIVE")
        public Boolean      active;
        
        /** Maximum size of (in bytes) of data bins */
        @ACfgOverride.Field(name="MAX_SIZE")
        public Integer      maxSize;

    }
    
    /**
     *  Structure class defining default configuration parameters for multiple, concurrent gRPC streams.  
     */
    public static class Concurrency extends CfgStructure<Concurrency> {

        /** Default constructor required for base structure class */
        public Concurrency() { super(Concurrency.class); }
        
        
        //
        // Configuration Fields
        //
        
        /** use multiple gRPC data stream for request recovery */
        @ACfgOverride.Field(name="ACTIVE")
        public Boolean      active;
        
        /** Optional size parameter activating multiple concurrent streams */
        @ACfgOverride.Field(name="PIVOT_SIZE")
        public Long         pivotSize;
        
        /** Maximum number of gRPC data stream to use */
        @ACfgOverride.Field(name="MAX_STREAMS")
        public Integer      maxStreams;
    }
    
}
