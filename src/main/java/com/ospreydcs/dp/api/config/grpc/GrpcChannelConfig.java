/*
 * Project: dp-api-common
 * File:	GrpcChannelConfig.java
 * Package: com.ospreydcs.dp.api.config.grpc
 * Type: 	GrpcChannelConfig
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

import org.yaml.snakeyaml.Yaml;

import com.ospreydcs.dp.api.config.AEnvOverride;

/**
 * <p>
 * Structure Containing gRPC Channel Parameters
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
@AEnvOverride
public final class GrpcChannelConfig {
    
    /** Optional configuration name */
    public String   name;
    
    /** Optional configuration version */
    public String   version;
    
    /** Optional description */
    public String   description;
    
    /** Optional supplemental parameters */
    public String   supplement;
    
    /** Required Channel parameters */
    @AEnvOverride.Struct
    public Channel  channel;
    
    /**
     * Structure containing gRPC Channel Parameters
     * 
     */
    public static final class Channel {
        
        /** Server host name and port address (URI) */
        public Host     host;
        
        /** gRPC parameters for channel */
        @AEnvOverride.Struct
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

            /**
             * @return the current value of property url
             */
            public String getUrl() {
                return url;
            }

            /**
             * @param url sets value of property url
             */
            public void setUrl(String url) {
                this.url = url;
            }

            /**
             * @return the current value of property port
             */
            public Integer getPort() {
                return port;
            }

            /**
             * @param port sets name value of property port 
             */
            public void setPort(Integer port) {
                this.port = port;
            }
            
            /**
             *
             * @see @see java.lang.Object#equals(java.lang.Object)
             */
            @Override
            public boolean equals(Object obj) {
                Host hstCmp;
                if (obj instanceof Host)
                    hstCmp = (Host) obj;
                else
                    return false;
                
                return hstCmp.url.equals(this.url) && hstCmp.port.equals(this.port);
            }

        } /* Host */
        
        
        /**
         * Structure Containing Common gRPC Parameters (for Channel) 
         *
         */
        public static final class Grpc {

            /** Maximum gRPC (Protobuf) message size - must be multiple of 2 */
            @AEnvOverride.Field(env="DP_API_GRPC_MAX_MESSAGE_SIZE")
            public Long     messageSize;
            
            /** Timeout limit used for connection establishment (seconds) */
            @AEnvOverride.Field(env="DP_API_GRPC_CHANNEL_TIMEOUT")
            public Integer  timeout;
            
            /** Transmit plain text (rather than binary) */
            @AEnvOverride.Field(env="DP_API_GRPC_XMIT_PLAIN_TEXT")
            public Boolean  usePlainText;
            
            /** Keep channel active even when not in use */
            @AEnvOverride.Field(env="DP_API_GRPC_CHANNEL_KEEP_ALIVE")
            public Boolean  keepAliveWithoutCalls;
            
            /** Compress transmitted message with GZIP algorithm (not recommended) */
            @AEnvOverride.Field(env="DP_API_GRPC_COMPRESS_GZIP")
            public Boolean  gzip;

            /**
             * @return the current value of property messageSize
             */
            public Long getMessageSize() {
                return messageSize;
            }

            /**
             * @param messageSize sets name value of property messageSize 
             */
            public void setMessageSize(Long messageSize) {
                this.messageSize = messageSize;
            }

            /**
             * @return the current value of property timeout
             */
            public Integer getTimeout() {
                return timeout;
            }

            /**
             * @param timeout sets name value of property timeout 
             */
            public void setTimeout(Integer timeout) {
                this.timeout = timeout;
            }

            /**
             * @return the current value of property usePlainText
             */
            public Boolean getUsePlainText() {
                return usePlainText;
            }

            /**
             * @param usePlainText sets name value of property usePlainText 
             */
            public void setUsePlainText(Boolean usePlainText) {
                this.usePlainText = usePlainText;
            }

            /**
             * @return the current value of property keepAliveWithoutCalls
             */
            public Boolean getKeepAliveWithoutCalls() {
                return keepAliveWithoutCalls;
            }

            /**
             * @param keepAliveWithoutCalls sets name value of property keepAliveWithoutCalls 
             */
            public void setKeepAliveWithoutCalls(Boolean keepAliveWithoutCalls) {
                this.keepAliveWithoutCalls = keepAliveWithoutCalls;
            }

            /**
             * @return the current value of property gzip
             */
            public Boolean getGzip() {
                return gzip;
            }

            /**
             * @param gzip sets name value of property gzip 
             */
            public void setGzip(Boolean gzip) {
                this.gzip = gzip;
            }

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
                
                // Check values
                return  grpc.messageSize.equals(this.messageSize) &&
                        grpc.timeout.equals(this.timeout) &&
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
        GrpcChannelConfig   cfg;
        
        if (obj instanceof GrpcChannelConfig)
            cfg = (GrpcChannelConfig)obj;
        else
            return false;
        
        // Check values
        if (!this.name.equals(cfg.name))
            return false;
        
        if (!this.version.equals(cfg.version))
            return false;
        
        if (!this.channel.host.equals(cfg.channel.host))
            return false;
        
        if (!this.channel.grpc.equals(cfg.channel.grpc))
            return false;
            
        return true;
    }
 
} /* GrpcChannelConfig */