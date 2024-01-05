/*
 * Project: datastore-admin
 * File:	CfgOverrideRec.java
 * Package: com.ospreydcs.datastore.admin.config
 * Type: 	CfgOverrideRec
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
package com.ospreydcs.dp.api.config.model;

import java.lang.reflect.Field;

/**
 * <p>
 * Record encapsulation of an variable supported by the configuration mechanism that is capable of
 * value override (e.g., by environment variable or command-line argument).
 * </p>
 * <p>
 * The supported environment variables are typically used to override the configuration parameters
 * managed by supporting structure classes.  Thus, the record attributes also contain the association
 * between the supported environment variables and the supporting structure classes (i.e., with reflection).
 * </p>
 * <p>
 * Note that override capable configuration parameters can also be overridden by the Java command line using
 * the "-D<name=value>" convention.
 * </p>
 *
 * @param varName      environment variable name
 * @param varValue     current value of the environment variable 
 * @param cfgClass   the Java class type owning the parameter 
 * @param cfgField   the Java field containing the parameter (Java reflection can get enclosing class, name, etc.)
 * @param cfgValue  the Java class object containing the Java field (can be used for reflection)
 *
 * @author Christopher K. Allen
 * @since Sep 21, 2022
 *
 */
public record CfgOverrideRec(String varName, String varValue, Class<?> cfgClass, Field cfgField, Object cfgValue) {

    /**
     * <p>Write out the contents of the record to a text string</p>
     * 
     * <p>
     * Alternative format to <code>{@link #toString()}</code>.
     * Returns the record contents as a single line string (no newline character). 
     * </p>
     * 
     * @return string representation of record contents
     */
    public String printLine() {
        StringBuffer    buf = new StringBuffer();
        
        buf.append("{");
        buf.append("varName=" + varName + ", ");
        buf.append("varValue=" + varValue + ", ");
        buf.append("cfgClass=" + cfgClass.getSimpleName() + ", ");
        buf.append("cfgField=" + cfgField.getName() + ", ");
        if (cfgValue != null)
            buf.append("cfgValue=" + cfgValue.toString());
        buf.append("}");
        
        return buf.toString();
    }
}
