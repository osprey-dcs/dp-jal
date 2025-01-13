/*
 * Project: dp-api-common
 * File:	ProviderRegistrar.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	ProviderRegistrar
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
 * @since Mar 28, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.common;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Record class for registering data providers with the <em>Data Platform</em> Ingestion Service.
 * </p>
 * <p>
 * Registration is an required action for data providers in order to archive data through the Data Platform
 * Ingestion Service.  Registration secures a unique provider ID and provides the Data Platform with optional 
 * attributes to be used as metadata within the data archive.
 * </p> 
 * <p>
 * <h2>Construction</h2>
 * A data provider must have a unique name which is supplied at record creation to the non-canonical 
 * constructor <code>{@link #ProviderRegistrar(String)}</code>.
 * The canonical constructor <code>{@link #ProviderRegistrar(String, Map)}</code> is not required for
 * general use, that is, the attribute container <code>{@link #attributes}</code> does not require explicit
 * creation and initialization as an argument.   
 * If a data provider wishes to associate optional attributes to itself during registration,
 * the method <code>{@link #addAttribute(String, String)}</code> is available.    
 * <p>
 * <h2>Provider Attributes</h2>
 * This class acts as a data record, however, one that can be modified post construction.
 * Specifically, data provider attributes can be added in the form of (name, value) pairs of strings using
 * the <code>{@link #addAttribute(String, String)}</code> method.  This method may be called as many times
 * as desired, once for each attribute pair.
 * </p>
 * 
 * @param name       name of the data provider
 * @param attributes collection of optional (name,value) attribute pairs associated with the provider
 *
 * @author Christopher K. Allen
 * @since Mar 28, 2024
 *
 */
public final record ProviderRegistrar(String name, Map<String, String> attributes) {
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new record of <code>ProviderRegistrar</code> using only the required unique 
     * provider name (no attributes).
     * </p>
     *
     * @param name       name of the data provider
     * @param attributes collection of optional (name,value) attribute pairs associated with the provider
     * 
     * @return  new <code>ProviderRegistrar</code> record populated with argument data
     */
    public static ProviderRegistrar from(String name) {
        return new ProviderRegistrar(name);
    }
    
    /**
     * <p>
     * Creates a new record of <code>ProviderRegistrar</code> using the canonical constructor.
     * </p>
     * 
     * @param name       name of the data provider
     * @param attributes collection of optional (name,value) attribute pairs associated with the provider
     * 
     * @return  new <code>ProviderRegistrar</code> record populated with argument data
     */
    public static ProviderRegistrar from(String name, Map<String, String> attributes) {
        return new ProviderRegistrar(name, attributes);
    }
    
    
    //
    // Non-Canonical Constructor
    //
    
    /**
     * <p>
     * Constructs a new record of <code>ProviderRegistrar</code> using only the required unique 
     * provider name (no attributes).
     * </p>
     *
     * @param name  the data provider unique name
     */
    public ProviderRegistrar(String name) {
        
        this(name, new HashMap<>());
    }
    
    /**
     * <p>
     * Adds optional attribute (name, value) pair to be associated with the data provider.
     * </p>
     * <p>
     * This method assigns an optional (name, value) attribute pair to the data provider that is included as 
     * metadata within the Data Platform data archive.  Multiple attributes can be assigned by repeated 
     * invocation if this method.
     * </p>
     * 
     * @param name      attribute name
     * @param value     attribute value
     */
    public void addAttribute(String name, String value) {
        this.attributes.put(name, value);
    }

}
