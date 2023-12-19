/*
 * Project: datastore-admin
 * File:	AppEnvVariable.java
 * Package: com.ospreydcs.datastore.admin.config
 * Type: 	AppEnvVariable
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
 * Record encapsulation of an environment variable supported by the application.  The
 * supported environment variables are typically used to override the application properties
 * managed by class <code>AppProperties</code>.  Thus, the record attributes represent the association
 * between the supported application environment variables and the application properties.
 *
 * @param varName         environment variable varName
 * @param varValue        varValue of the environment variable at application startup
 * @param propertyField   the property field (Java reflection can get enclosing class, varName, etc.)
 * @param propertyObject  the class object containing the field (can be used for reflection)
 *
 * @author Christopher K. Allen
 * @since Sep 21, 2022
 *
 */
public record AppEnvVariable(String varName, String varValue, Field propertyField, Object propertyObject) {

    public String printOut() {
        StringBuffer    buf = new StringBuffer();
        
        buf.append("varName=" + varName + ", ");
        buf.append("varValue=" + varValue + ", ");
        buf.append("field=" + propertyField.getName() + ", ");
        buf.append("class=" + propertyField.getDeclaringClass().getName());
        
        return buf.toString();
    }
}
