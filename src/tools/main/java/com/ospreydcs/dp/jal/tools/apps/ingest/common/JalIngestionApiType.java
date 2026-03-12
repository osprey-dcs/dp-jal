/*
 * Project: dp-jal
 * File:	JalIngestionApiType.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.api
 * Type: 	JalIngestionApiType
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
 * @since Mar 4, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.common;

import java.util.NoSuchElementException;

import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Enumeration of the JAL Ingestion Service APIs.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 4, 2026
 *
 */
public enum JalIngestionApiType {
    
    /**
     * Unary Ingestion Service API <code>IIngestionService</code>.
     */
    UNARY,
    
    /**
     * Streaming Ingestion Service API <code>IIngestionStream</code>.
     */
    STREAM,
    
    ;
    
    
    //
    // Enumeration Operations
    //
    
    /**
     * <p>
     * Returns the enumeration constant with the given name.
     * </p>
     * <p>
     * This is a convenience method invoking <code>{@link Enum#valueOf(Class, String)</code> using the
     * class type of this enumeration.
     * </p>
     * 
     * @param strName   name of the constant to return
     * 
     * @return  the enumeration constant with the given name
     * 
     * @throws NoSuchElementException   invalid enumeration constant name
     * 
     * @see Enum#valueOf(Class, String)Í
     */
    public static JalIngestionApiType   valueFrom(String strName) throws NoSuchElementException {
        
        try {
            JalIngestionApiType enmType = Enum.valueOf(JalIngestionApiType.class, strName);
            
            return enmType;
                    
        } catch (Exception e) {
            throw new NoSuchElementException(JavaRuntime.getQualifiedMethodNameSimple() + " - Unrecognized constant name: " + strName, e);
            
        }
    }
}
