/*
 * Project: dp-api-common
 * File:	DpGrpcConnectionConfig.java
 * Package: com.ospreydcs.dp.api.config.grpc
 * Type: 	DpGrpcConnectionConfig
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
 * @since Dec 18, 2023
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config.grpc;

import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.common.DpTimeoutConfig;
import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure Containing gRPC Connection Parameters
 * </p>
 * <p>
 * A "connection" is an API abstraction for the gRPC channel.  This abstraction includes additional notations
 * beyond the the standard channel properties and functionality.  Connections also contain information about
 * the gRPC "service interface" for which the channel is connected, specifically, the Protocol Buffers 
 * generated "communication stub" used to perform the RPC operations of the interface.  These stubs rely
 * on the gRPC channel for the actual Protocal Buffers message transport.  Within Protocol
 * Buffers, the communication stubs can be further configured beyond their underlying gRPC channel configuration.
 * For example, timeout limits can be set, compression algorithms can be specified, etc.
 * </p>  
 * <p>
 * Contains common parameters used when creating gRPC <code>Channel</code> objects 
 * (i.e. <code>ManagedChannel</code> in Java gRPC).   Note that these parameters are
 * all contained in the <code>{@link #channel}</code> field.  Additional, higher-level
 * field are included for configuration management and for external timeout operations.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 18, 2023
 *
 */
@ACfgOverride.Root(root="DP_API_CONNECTION")        // Note this is just for show
public final class DpGrpcConnectionConfig extends CfgStructure<DpGrpcConnectionConfig>{
    
    /** Default constructor required for structure base class */
    public DpGrpcConnectionConfig() {
        super(DpGrpcConnectionConfig.class);
    }

    
    // 
    // Configuration Parameters
    //
    
    /** Optional configuration name */
    @ACfgOverride.Field(name="NAME")
    public String   name;
    
    /** Optional configuration version */
    @ACfgOverride.Field(name="VERSION")
    public String   version;
    
    /** Optional description */
    @ACfgOverride.Field(name="DESCRIPTION")
    public String   description;
    
    /** Optional supplemental parameters */
    @ACfgOverride.Field(name="SUPPLEMENT")
    public String   supplement;
    
    /** General timeout properties used external to channel connection (i.e., on channel objects) */
    @ACfgOverride.Struct(pathelem="TIMEOUT")
    public DpTimeoutConfig  timeout;
    
    /** Required Channel parameters */
    @ACfgOverride.Struct(pathelem="CHANNEL")
    public Channel  channel;

    
    /**
     * Structure containing gRPC Channel configuration parameters
     * 
     */
    @ACfgOverride.Root(root="DP_API_CONNECTION_CHANNEL")
    public static final class Channel extends CfgStructure<Channel> {
        
        /** Default constructor required for structure base class */
        public Channel() {
            super(Channel.class);
        }

        // 
        // Configuration Parameters
        //
        
        /** Server host identification - name and port address (URI) */
        @ACfgOverride.Struct(pathelem="HOST")
        public Host     host;
        
        /** TLS security parameters */
        @ACfgOverride.Struct(pathelem="TLS")
        public TLS      tls;
        
        /** gRPC parameters for channel */
        @ACfgOverride.Struct(pathelem="GRPC")
        public Grpc     grpc;
        
        /**
         * Structure containing server host identification information
         *
         */
        @ACfgOverride.Root(root="DP_API_CONNECTION_CHANNEL_HOST")
        public static final class Host extends CfgStructure<Host> {
            
            /** Default constructor required for structure base class */
            public Host() {
                super(Host.class);
            }

            
            // 
            // Configuration Parameters
            //
            
            /** Host network URL */
            @ACfgOverride.Field(name="URL")
            public String   url;
            
            /** Port address of service */
            @ACfgOverride.Field(name="PORT")
            public Integer  port;

        } /* Channel.Host */
        
        
        /**
         * Structure containing Transport Layer Security (TLS) configuration parameters
         *
         */
        @ACfgOverride.Root(root="DP_API_CONNECTION_CHANNEL_TLS")
        public static final class TLS extends CfgStructure<TLS>{
            
            /** Default constructor required for structure base class */
            public TLS() {
                super(TLS.class);
            }

            // 
            // Configuration Parameters
            //
            
            /** Is TLS security used */
            @ACfgOverride.Field(name="ENABLED")
            public Boolean      enabled;
            
            /** Use default TLS security */
            @ACfgOverride.Field(name="DEFAULT")
            public Boolean      defaultTls;
            
            /** File path locations for TLS certificates and keys */
            @ACfgOverride.Struct(pathelem="FILE")
            public FilePaths    filepaths;
            
            /**
             * Structure of file path locations for TLS configurations.
             *
             */
            @ACfgOverride.Root(root="DP_API_CONNECTION_CHANNEL_TLS_FILE")
            public static final class FilePaths extends CfgStructure<FilePaths> {
                
                /** Default constructor required for structure base class */
                public FilePaths() {
                    super(FilePaths.class);
                }

                // 
                // Configuration Parameters
                //
                
                /** Collection of all trusted sources */
                @ACfgOverride.Field(name="TRUSTED_CERTS")
                public String   trustedCerts;
                
                /** Client certificates chain */
                @ACfgOverride.Field(name="CLIENT_CERTS")
                public String   clientCerts;
                
                /** Client private key */
                @ACfgOverride.Field(name="CLIENT_KEY")
                public String   clientKey;

            } /* Channel.TLS.FilePaths */

        } /* Channel.TLS */
        
        /**
         * Structure containing gRPC parameters specific to a channel configuration  
         *
         */
        @ACfgOverride.Root(root="DP_API_CONNECTION_CHANNEL_GRPC")
        public static final class Grpc extends CfgStructure<Grpc> {

            /** Default constructor required for structure base class */
            public Grpc() {
                super(Grpc.class);
            }

            // 
            // Configuration Parameters
            //
            
            /** Timeout limit used for connection establishment */
            @ACfgOverride.Field(name="TIMEOUT_LIMIT")
            public Long     timeoutLimit;
            
            /** Timeout limit units */
            @ACfgOverride.Field(name="TIMEOUT_UNIT")
            public TimeUnit timeoutUnit;
            
            /** Maximum gRPC (Protobuf) message size - must be multiple of 2 */
            @ACfgOverride.Field(name="MESSAGE_SIZE_MAX")
            public Integer  messageSizeMax;
            
            /** Transmit plain text (rather than binary) */
            @ACfgOverride.Field(name="XMIT_PLAIN_TEXT")
            public Boolean  usePlainText;
            
            /** Keep channel enabled even when not in use - WARNING: This can sequester significant gRPC resources */
            @ACfgOverride.Field(name="KEEP_ALIVE")
            public Boolean  keepAliveWithoutCalls;
            
            /** Compress transmitted message with GZIP algorithm (not recommended) */
            @ACfgOverride.Field(name="COMPRESS_GZIP")
            public Boolean  gzip;

        } /* Channel.Grpc */
        
    } /* Channel */

} /* DpGrpcConnectionConfig */
