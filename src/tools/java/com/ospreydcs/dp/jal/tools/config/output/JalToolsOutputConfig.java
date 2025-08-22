/*
 * Project: dp-api-common
 * File:	JalToolsOutputConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config
 * Type: 	JalToolsOutputConfig
 *
 * Copyright 2010-2025 the original author or authors.
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
 * @since May 11, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.config.output;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing default output locations for the Java API Library tools.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since May 11, 2025
 *
 */
@ACfgOverride.Root(root="JAL_TOOLS_QUERY_OUTPUT")
public class JalToolsOutputConfig extends CfgStructure<JalToolsOutputConfig> {

    /** Default constructor require for super class */
    public JalToolsOutputConfig() { super(JalToolsOutputConfig.class); }


    //
    // Record Fields
    //

    /** Location of QueryChannel evaluation outputs */
    @ACfgOverride.Field(name="PATH")
    public String   path;

}
