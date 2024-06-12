/*
 * Project: dp-api-common
 * File:	ProviderUID.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	ProviderUID
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
package com.ospreydcs.dp.api.model;

/**
 * <p>
 * Data Provider unique identifier.
 * </p>
 * <p>
 * Data provider unique identifiers (UID) are created and managed by the Data Platform Ingestion Service.
 * Data providers must first register with the Ingestion Service using their unique name and optional
 * attributes.  Upon registration the Ingestion Service returns a UID for the provider which it must use
 * in all ingestion operations.  If a data provider has previously registered with the Ingestion Service its
 * original UID is returned.
 * </p>
 * <p>
 * Instances of <code>ProviderUID</code> are created by the <code>DpIngestionService</code> client interface
 * at passed to client data providers upon registration.
 * </p>
 * 
 * @param   uid     unique identifier for the data provider
 *  
 * @author Christopher K. Allen
 * @since Mar 28, 2024
 *
 */
public final record ProviderUID(int uid) {
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new <code>ProviderUID</code> instance from the given integer-valued identifier.
     * </p>
     * 
     * @param   uid     unique identifier for the data provider
     * 
     * @return new <code>ProviderUID</code> record with the given integer-value UID
     */
    public static ProviderUID   from (int uid) {
        return new ProviderUID(uid);
    }

}
