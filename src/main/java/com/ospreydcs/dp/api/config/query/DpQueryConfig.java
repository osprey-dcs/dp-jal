/*
 * Project: dp-api-common
 * File:	DpQueryConfig.java
 * Package: com.ospreydcs.dp.api.config.query
 * Type: 	DpQueryConfig
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
package com.ospreydcs.dp.api.config.query;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing default configuration parameters for Data Platform Query Service operations.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 31, 2023
 *
 */
@ACfgOverride.Root(root="DP_API_QUERY")
public final class DpQueryConfig extends CfgStructure<DpQueryConfig> {

    /** Default constructor require for base structure class */
    public DpQueryConfig() {
        super(DpQueryConfig.class);
    }


    /** Default bucket count per page when using cursor requests */
    @ACfgOverride.Field(name="PAGE_SIZE")
    public Integer  pageSize;

    
//    //
//    // Object Overrides
//    //
//    
//    /**
//     *
//     * @see @see java.lang.Object#equals(java.lang.Object)
//     */
//    @Override
//    public boolean equals(Object obj) {
//        
//        // Cast comparison object
//        DpQueryConfig qry;
//        if (obj instanceof DpQueryConfig)
//            qry = (DpQueryConfig)obj;
//        else
//            return false;
//        
//        // Check equivalence
//        return qry.pageSize.equals(this.pageSize);
//    }
//    

}
