/*
 * Project: dp-api-common
 * File:	EnvParser.java
 * Package: com.ospreydcs.dp.api.config
 * Type: 	EnvParser
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
package com.ospreydcs.dp.api.config;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


/**
 *
 * @author Christopher K. Allen
 * @since Dec 18, 2023
 *
 */
public final class EnvParser {

    /**
     * <p>
     * Parses the properties in the <code>AppProperties</code> for the 
     * <code>AOverrideCapable</code> annotations to determine the supported environment
     * variables for the application.  The application then returns a list of these
     * variables which it found (along with the current values, the properties object,
     * etc. as an <code>AppEnvVariable</code> records).
     * It is assumed the argument is
     * the <code>AppProperties</code> instance which has already been loaded from the
     * <em>application.yml</em> configuration file.  
     * <h2>NOTE:</h2>
     * This is a recursive function so the method signature must be of type
     * <code>Object</code> to function correctly.  However, the argument of initial invocation
     * is expected to be of type <code>AppProperties</code>. 
     * </p>
     * 
     * @param objProps  the <code>AppProperties</code> instance to be overridden
     * 
     * @return a list of environment variables (records) found when parsing the argument 
     * 
     * @throws IllegalArgumentException encountered a null argument during recursion
     * @throws IllegAccessException unable to get a structure field object (java.lang.reflect.Field#get)
     */
    public static List<AppEnvVariable> parse(Object objProps) throws IllegalArgumentException, IllegalAccessException {
        
        if ( Objects.isNull(objProps) )
            throw new IllegalArgumentException("Properties object is null");
        
        List<AppEnvVariable> lstVars = new LinkedList<AppEnvVariable>();

        Class<?> clsProps = objProps.getClass();

        for (Field fld : clsProps.getDeclaredFields()) {
            if (fld.isAnnotationPresent(AEnvOverride.Struct.class)) {
                fld.setAccessible(true);
                Object objFld = fld.get(objProps);
                
                lstVars.addAll( EnvParser.parse(objFld) );
            }
            
            if (fld.isAnnotationPresent(AEnvOverride.Field.class)) {
                AEnvOverride.Field annFld = fld.getAnnotation(AEnvOverride.Field.class);
                if ( !annFld.env().isBlank() ) {
                    String strVarNm  = annFld.env();
                    String strVarVal = System.getenv(annFld.env());
                    
                    AppEnvVariable var = new AppEnvVariable(strVarNm, strVarVal, fld, objProps);
                   
                    lstVars.add(var);
                }
            }
            
        }        
     
        return lstVars;
    }

}
