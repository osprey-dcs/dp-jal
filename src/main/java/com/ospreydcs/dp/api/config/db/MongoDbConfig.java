/*
 * Project: dp-api-common
 * File:	MongoDbConfig.java
 * Package: com.ospreydcs.dp.api.config.db
 * Type: 	MongoDbConfig
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
package com.ospreydcs.dp.api.config.db;

import org.yaml.snakeyaml.Yaml;

import com.ospreydcs.dp.api.config.AEnvOverride;

/**
 * <p>
 * Structure Class Containing MongoDB Configuration Parameters
 * </p>
 * <p>
 * Contains parameters for identifying a MongoDB host server and required client
 * credentials for user authorization.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Dec 18, 2023
 *
 */
@AEnvOverride
public final class MongoDbConfig {

    /** Optional configuration name */
    public String   name;
    
    /** Optional configuration version */
    public String   version;
    
    /** Optional description */
    public String   description;
    
    /** Optional supplemental parameters */
    public String   supplement;
    
    /** Require MongoDB host parameters */
    @AEnvOverride.Struct
    public Host     host;
    
    /** Required MongoDB client credentials */
    @AEnvOverride.Struct
    public Client   client;
    
    /**
     * Structure for MongoDB Host Server Identification
     *
     */
    public static final class Host {
        
        /** Host network URL */
        @AEnvOverride.Field(env="DP_API_MONGODB_HOST_URL")
        public String   url;
        
        /** Port address of service */
        @AEnvOverride.Field(env="DP_API_MONGODB_HOST_PORT")
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
            
            // Cast comparison object
            Host hstCmp;
            if (obj instanceof Host)
                hstCmp = (Host) obj;
            else
                return false;
            
            // Check equivalence
            return hstCmp.url.equals(this.url) && hstCmp.port.equals(this.port);
        }

    } /* Host */
    
    
    /**
     * Structure for MongoDB Client Identification (i.e., client credentials).
     *
     */
    public static final class Client {
        
        /** Client user ID */
        @AEnvOverride.Field(env="DP_API_MONGODB_CLIENT_USERID")        
        public String   user;
        
        /** Client password */
        @AEnvOverride.Field(env="DP_API_MONGODB_CLIENT_PASSWORD")
        public String   password;

        /**
         * @return the current value of property user
         */
        public String getUser() {
            return user;
        }

        /**
         * @param user sets name value of property user 
         */
        public void setUser(String user) {
            this.user = user;
        }

        /**
         * @return the current value of property password
         */
        public String getPassword() {
            return password;
        }

        /**
         * @param password sets name value of property password 
         */
        public void setPassword(String password) {
            this.password = password;
        }

        /**
         *
         * @see @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {

            // Cast comparison object
            Client  cltCmp;
            if (obj instanceof Client)
                cltCmp = (Client)obj;
            else
                return false;
            
            // Check equivalence
            return cltCmp.user.equals(this.user) && cltCmp.password.equals(this.password);
        }
        
    } /* Client */


    //
    // Object Overrides
    //
    
    /**
     *
     * @see @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        Yaml    yml = new Yaml();
        
        String  str = yml.dump(this);
        
        return str;
    }


    /**
     *
     * @see @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        // Cast to comparison object
        MongoDbConfig   cfg;
        
        if (obj instanceof MongoDbConfig)
            cfg = (MongoDbConfig)obj;
        else
            return false;
        
        // Check equivalence
        if (!this.name.equals(cfg.name))
            return false;
        
        if (!this.version.equals(cfg.version))
            return false;
        
        if (!this.host.equals(cfg.host))
            return false;
        
        if (!this.client.equals(cfg.client))
            return false;
            
        return true;
    }
    
    
}
