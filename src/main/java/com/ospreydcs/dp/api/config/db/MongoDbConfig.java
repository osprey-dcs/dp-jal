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

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

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
@ACfgOverride.Root(root="DP_API_MONGODB")
public final class MongoDbConfig extends CfgStructure<MongoDbConfig>{

    /** Default constructor for base class */
    public MongoDbConfig() {
        super(MongoDbConfig.class);
    }


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
    
    /** Require MongoDB host parameters */
    @ACfgOverride.Struct(pathelem="HOST")
    public Host     host;
    
    /** Required MongoDB client credentials */
    @ACfgOverride.Struct(pathelem="CLIENT")
    public Client   client;
    
    /**
     * Structure for MongoDB Host Server Identification
     *
     */
    @ACfgOverride.Root(root="DP_API_MONGODB_HOST")
    public static final class Host extends CfgStructure<Host> {
        
        /** Default constructor for base class */
        public Host() {
            super(Host.class);
        }

        /** Host network URL */
        @ACfgOverride.Field(name="URL")
        public String   url;
        
        /** Port address of service */
        @ACfgOverride.Field(name="PORT")
        public Integer  port;

    } /* Host */
    
    
    /**
     * Structure for MongoDB Client Identification (i.e., client credentials).
     *
     */
    @ACfgOverride.Root(root="DP_API_MONGODB_CLIENT")
    public static final class Client extends CfgStructure<Client> {
        
        /** Default constructor for base class */
        public Client() {
            super(Client.class);
        }

        /** Client user ID */
        @ACfgOverride.Field(name="USERID")        
        public String   user;
        
        /** Client password */
        @ACfgOverride.Field(name="PASSWORD")
        public String   password;
        
    } /* Client */


    //
    // Object Overrides
    //
    
//    /**
//     *
//     * @see @see java.lang.Object#toString()
//     */
//    @Override
//    public String toString() {
//        Yaml    yml = new Yaml();
//        
//        String  str = yml.dump(this);
//        
//        return str;
//    }

}
