/*
 * Project: dp-api-common
 * File:	GrpcConnectionConfig.java
 * Package: com.ospreydcs.dp.api.config.grpc
 * Type: 	GrpcConnectionConfig
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

import org.yaml.snakeyaml.Yaml;

import com.ospreydcs.dp.api.config.model.ACfgOverride;

/**
 * <p>
 * Structure Containing gRPC Connection Parameters
 * </p>
 * <p>
 * Contains common parameters used when creating gRPC <code>Channel</code> objects 
 * (i.e. <code>ManagedChannel</code> in Java gRPC). 
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 18, 2023
 *
 */
@ACfgOverride
public final class GrpcConnectionConfig {
    
    /** Optional configuration name */
    public String   name;
    
    /** Optional configuration version */
    public String   version;
    
    /** Optional description */
    public String   description;
    
    /** Optional supplemental parameters */
    public String   supplement;
    
    /** Required Channel parameters */
    @ACfgOverride.Struct
    public Channel  channel;

    /** General timeout properties */
    @ACfgOverride.Struct
    public Timeout  timeout;
    
    
    /**
     * Structure containing timeout parameters
     *
     */
    public static final class Timeout {
        
        /** Is timeout active */
        @ACfgOverride.Field(name="DP_API_CONNECTION_TIMEOUT_ACTIVE")
        public Boolean      active;
        
        /** Timeout limit */
        @ACfgOverride.Field(name="DP_API_CONNECTION_TIMEOUT_LIMIT")
        public Long         limit;
        
        /** Units for the timeout limit */
        @ACfgOverride.Field(name="DP_API_CONNECTION_TIMEOUT_UNIT")
        public TimeUnit     unit;

        /**
         *
         * @see @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            
            // Cast comparison object
            Timeout tmtCmp;
            if (obj instanceof Timeout)
                tmtCmp = (Timeout)obj;
            else
                return false;
            
            // Check equivalence
            return (tmtCmp.active == this.active) &&
                    tmtCmp.limit.equals(this.limit) &&
                    tmtCmp.unit.equals(this.unit);
        }
        
    }
    
    /**
     * Structure containing gRPC Channel Parameters
     * 
     */
    public static final class Channel {
        
        /** Server host name and port address (URI) */
        public Host     host;
        
        /** TLS security parameters */
        @ACfgOverride.Struct
        public TLS      tls;
        
        /** gRPC parameters for channel */
        @ACfgOverride.Struct
        public Grpc     grpc;
        
        /**
         * Structure for Host Server Identification
         *
         */
        public static final class Host {
            
            /** Host network URL */
            public String   url;
            
            /** Port address of service */
            public Integer  port;

//            /**
//             * @return the current value of property url
//             */
//            public String getUrl() {
//                return url;
//            }
//
//            /**
//             * @param url sets value of property url
//             */
//            public void setUrl(String url) {
//                this.url = url;
//            }
//
//            /**
//             * @return the current value of property port
//             */
//            public Integer getPort() {
//                return port;
//            }
//
//            /**
//             * @param port sets name value of property port 
//             */
//            public void setPort(Integer port) {
//                this.port = port;
//            }
            
            /**
             *
             * @see @see java.lang.Object#equals(java.lang.Object)
             */
            @Override
            public boolean equals(Object obj) {
                
                // Cast comparison object
                Host hstCmp;
                if (obj instanceof Host)
                    hstCmp = (Host) obj;
                else
                    return false;
                
                return hstCmp.url.equals(this.url) && hstCmp.port.equals(this.port);
            }

        } /* Host */
        
        
        public static final class TLS {
            
            /** Is TLS security used */
            @ACfgOverride.Field(name="DP_API_CONNECTION_TLS_ACTIVE")
            public Boolean  active;
            
            /** Use default TLS security */
            @ACfgOverride.Field(name="DP_API_CONNECTION_TLS_DEFAULT")
            public Boolean  defaultTls;
            
            /** File path locations for TLS certificates and keys */
            @ACfgOverride.Struct
            public FilePaths    filepaths;
            
            /**
             * Structure of file path locations for TLS resources.
             *
             */
            public static final class FilePaths {
                
                /** Collection of all trusted sources */
                @ACfgOverride.Field(name="DP_API_CONNECTION_TLS_FILE_TRUSTED_CERTS")
                public String   trustedCerts;
                
                /** Client certificates chain */
                @ACfgOverride.Field(name="DP_API_CONNECTION_TLS_FILE_CLIENT_CERTS")
                public String   clientCerts;
                
                /** Client private key */
                @ACfgOverride.Field(name="DP_API_CONNECTION_TLS_FILE_CLIENT_KEY")
                public String   clientKey;

                /**
                 *
                 * @see @see java.lang.Object#equals(java.lang.Object)
                 */
                @Override
                public boolean equals(Object obj) {
                    
                    // Cast comparison object
                    FilePaths   paths;
                    if (obj instanceof FilePaths)
                        paths = (FilePaths)obj;
                    else
                        return false;
                    
                    // Check equivalence
                    return paths.trustedCerts.equals(this.trustedCerts) &&
                            paths.clientCerts.equals(this.clientCerts) &&
                            paths.clientKey.equals(this.clientKey);
                }
            }

            /**
             *
             * @see @see java.lang.Object#equals(java.lang.Object)
             */
            @Override
            public boolean equals(Object obj) {
                
                // Cast comparison object
                TLS tls;
                if (obj instanceof TLS)
                    tls = (TLS)obj;
                else
                    return false;
                
                // Check equivalence
                return (tls.active == this.active) &&
                        (tls.defaultTls = this.defaultTls) &&
                        tls.filepaths.equals(this.filepaths);
            }
        }
        
        /**
         * Structure Containing Common gRPC Parameters (for Channel) 
         *
         */
        public static final class Grpc {

            /** Timeout limit used for connection establishment */
            @ACfgOverride.Field(name="DP_API_GRPC_CHANNEL_TIMEOUT_LIMIT")
            public Long     timeoutLimit;
            
            /** Timeout limit units */
            @ACfgOverride.Field(name="DP_API_GRPC_CHANNEL_TIMEOUT_UNIT")
            public TimeUnit timeoutUnit;
            
            /** Maximum gRPC (Protobuf) message size - must be multiple of 2 */
            @ACfgOverride.Field(name="DP_API_GRPC_MESSAGE_SIZE_MAX")
            public Integer  messageSizeMax;
            
            /** Transmit plain text (rather than binary) */
            @ACfgOverride.Field(name="DP_API_GRPC_XMIT_PLAIN_TEXT")
            public Boolean  usePlainText;
            
            /** Keep channel active even when not in use */
            @ACfgOverride.Field(name="DP_API_GRPC_CHANNEL_KEEP_ALIVE")
            public Boolean  keepAliveWithoutCalls;
            
            /** Compress transmitted message with GZIP algorithm (not recommended) */
            @ACfgOverride.Field(name="DP_API_GRPC_COMPRESS_GZIP")
            public Boolean  gzip;

//            /**
//             * @return the current value of property messageSize
//             */
//            public Long getMessageSizeMax() {
//                return messageSizeMax;
//            }
//
//            /**
//             * @param messageSizeMax sets name value of property messageSizeMax 
//             */
//            public void setMessageSizeMax(Long messageSizeMax) {
//                this.messageSizeMax = messageSizeMax;
//            }
//
//            /**
//             * @return the current value of property timeoutCount
//             */
//            public Long getTimeoutCount() {
//                return timeoutCount;
//            }
//
//            /**
//             * @param timeoutCount sets name value of property timeoutCount 
//             */
//            public void setTimeout(Long timeoutCount) {
//                this.timeoutCount = timeoutCount;
//            }
//
//            /**
//             * @return the current value of property timeoutUnit
//             */
//            public TimeUnit getTimeoutUnit() {
//                return timeoutUnit;
//            }
//
//            /**
//             * @param timeoutUnit sets name value of property timeoutUnit 
//             */
//            public void setTimeoutUnit(TimeUnit timeoutUnit) {
//                this.timeoutUnit = timeoutUnit;
//            }
//            
//            /**
//             * @return the current value of property usePlainText
//             */
//            public Boolean getUsePlainText() {
//                return usePlainText;
//            }
//
//            /**
//             * @param usePlainText sets name value of property usePlainText 
//             */
//            public void setUsePlainText(Boolean usePlainText) {
//                this.usePlainText = usePlainText;
//            }
//
//            /**
//             * @return the current value of property keepAliveWithoutCalls
//             */
//            public Boolean getKeepAliveWithoutCalls() {
//                return keepAliveWithoutCalls;
//            }
//
//            /**
//             * @param keepAliveWithoutCalls sets name value of property keepAliveWithoutCalls 
//             */
//            public void setKeepAliveWithoutCalls(Boolean keepAliveWithoutCalls) {
//                this.keepAliveWithoutCalls = keepAliveWithoutCalls;
//            }
//
//            /**
//             * @return the current value of property gzip
//             */
//            public Boolean getGzip() {
//                return gzip;
//            }
//
//            /**
//             * @param gzip sets name value of property gzip 
//             */
//            public void setGzip(Boolean gzip) {
//                this.gzip = gzip;
//            }

            /**
             *
             * @see @see java.lang.Object#equals(java.lang.Object)
             */
            @Override
            public boolean equals(Object obj) {
                
                // Cast comparison object
                Grpc    grpc;
                if (obj instanceof Grpc)
                    grpc = (Grpc)obj;
                else
                    return false;
                
                // Check equivalence
                return  grpc.messageSizeMax.equals(this.messageSizeMax) &&
//                        grpc.timeoutCount.equals(this.timeoutCount) &&
//                        grpc.timeoutUnit.equals(this.timeoutUnit) &&
                        (grpc.usePlainText == this.usePlainText) &&
                        (grpc.keepAliveWithoutCalls == this.keepAliveWithoutCalls) &&
                        (grpc.gzip == this.gzip);
            }

        } /* Grpc */
        
    } /* Channel */

    /**
     *
     * @see @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        
        Yaml    yaml = new Yaml();
        
        String  strCfg = yaml.dump(this);
        return strCfg;
    }

    /**
     *
     * @see @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        // Cast comparison object
        GrpcConnectionConfig   cfg;
        
        if (obj instanceof GrpcConnectionConfig)
            cfg = (GrpcConnectionConfig)obj;
        else
            return false;
        
        // Check values
        if (!this.name.equals(cfg.name))
            return false;
        
        if (!this.version.equals(cfg.version))
            return false;
        
        if (!this.channel.host.equals(cfg.channel.host))
            return false;
        
        if (!this.timeout.equals(cfg.timeout))
            return false;
        
        if (!this.channel.grpc.equals(cfg.channel.grpc))
            return false;
            
        return true;
    }
 
} /* GrpcConnectionConfig */