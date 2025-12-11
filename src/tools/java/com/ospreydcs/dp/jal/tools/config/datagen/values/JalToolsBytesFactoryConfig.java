/*
 * Project: dp-jal
 * File:	JalToolsBytesFactoryConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config.datagen.values
 * Type: 	JalToolsBytesFactoryConfig
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
 * @since Dec 5, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.config.datagen.values;

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing default configuration parameters for <code>ByteArrayFactory</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 5, 2025
 *
 */
public class JalToolsBytesFactoryConfig extends CfgStructure<JalToolsBytesFactoryConfig> {

    /** Default constructor required of base class  */
    public JalToolsBytesFactoryConfig() { super(JalToolsBytesFactoryConfig.class); }

    
    //
    // Attributes
    //
    
    /** Default byte array size (in bytes) */
    @ACfgOverride.Field(name="SIZE")
    public Integer          size;
    
}
