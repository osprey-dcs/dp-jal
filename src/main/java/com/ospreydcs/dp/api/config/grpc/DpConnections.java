/*
 * Project: dp-api-common
 * File:    DpConnections.java
 * Package: com.ospreydcs.dp.api.config.grpc
 * Type:    DpConnections
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
 * @since Dec 31, 2023
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config.grpc;

import com.ospreydcs.dp.api.config.model.ACfgOverride;

/**
 * Structure containing connection parameters for Data Platform services.
 *
 * @see GrpcConnectionConfig
 */
public final class DpConnections {
    
    /** Data Platform Ingestion Service connection parameters */
    @ACfgOverride.Struct
    public GrpcConnectionConfig     ingestion;
    
    /** Data Platform Query Service connection parameters */
    @ACfgOverride.Struct
    public GrpcConnectionConfig     query;

    
    //
    // Object Overrides
    //
    
    /**
     *
     * @see @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        // Cast comparison object
        DpConnections    srvs;
        if (obj instanceof DpConnections)
            srvs = (DpConnections)obj;
        else
            return false;
        
        // Check equivalence
        return srvs.ingestion.equals(this.ingestion) 
                && srvs.query.equals(this.query);
    }
}