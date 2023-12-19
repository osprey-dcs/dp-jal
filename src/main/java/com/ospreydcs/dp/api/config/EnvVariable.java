/*
 * Project: datastore-admin
 * File:	EnvVariable.java
 * Package: com.ospreydcs.datastore.admin.config
 * Type: 	EnvVariable
 *
 * Copyright 2010-2022 the original author or authors.
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
 * @since Sep 21, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.config;

import java.lang.reflect.Field;

/**
 * <p>
 * Record encapsulation of an environment variable supported by the configuration mechanism.
 * </p>
 * The supported environment variables are typically used to override the configuration parameters
 * managed by supporting structure classes.  Thus, the record attributes also contain the association
 * between the supported environment variables and the supporting structure classes (i.e., with reflection).
 * </p>
 *
 * @param varName      environment variable name
 * @param varValue     current value of the environment variable 
 * @param paramField   the property field (Java reflection can get enclosing class, varName, etc.)
 * @param paramObject  the class object containing the field (can be used for reflection)
 *
 * @author Christopher K. Allen
 * @since Sep 21, 2022
 *
 */
public record EnvVariable(String varName, String varValue, Field paramField, Object paramObject) {

    public String printOut() {
        StringBuffer    buf = new StringBuffer();
        
        buf.append("varName=" + varName + ", ");
        buf.append("varValue=" + varValue + ", ");
        buf.append("field=" + paramField.getName() + ", ");
        buf.append("class=" + paramField.getDeclaringClass().getName());
        
        return buf.toString();
    }
}
