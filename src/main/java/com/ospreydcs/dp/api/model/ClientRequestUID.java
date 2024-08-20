/*
 * Project: dp-api-common
 * File:	ClientRequestUID.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	ClientRequestUID
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
 * @since Apr 16, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model;

import java.util.UUID;

/**
 * <p>
 * Record for encapsulating the ingest data request unique identifier.
 * </p>
 * <p>
 * The ingest data request UID, or client request ID, is an identifier provided by Ingestion
 * Service clients to identify each ingest data request it sends.  This record is intended to
 * abstract the notion since the exact form of the identifier may change in the future.
 * </p>
 * 
 * @param   requestId    the client ingest data request identifier (as a string)
 * 
 * @author Christopher K. Allen
 * @since Apr 16, 2024
 *
 */
public record ClientRequestUID(String requestId) {
    
    //
    // Constants
    //
    
    /** Prefix given to randomly generated client UIDs */
    public static final String  STR_PREFIX = "dp-api-java-";
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new <code>ClientRequestUID</code> record instance exactly from the given argument.
     * </p>
     * <p>
     * <h2>WARNING</h2>
     * The given argument must be universally unique within the request space of the Ingestion Service.
     * All client ingest data requests must contain a unique identifier for later determination of 
     * successful ingestion.  Thus, it is highly recommended that alternate creators are used for 
     * generation of client UIDs in normal operations.
     * </p> 
     * 
     * @param strRequestId  client request ID used for record
     * 
     * @return  new record initialized with the given argument
     */
    public static ClientRequestUID   from(String strRequestId) {
        return new ClientRequestUID(strRequestId);
    }
    
    /**
     * <p>
     * Creates a new <code>ClientRequestUID</code> instance from the given arguments.
     * </p>
     * <p>
     * The returned client request UUID is assembled from the given request UUID argument
     * and the given suffix.  Specifically, the returned client request ingestion request
     * "unique identifier" is formed by appending the <code>String</code> argument to the 
     * <code>ClientRequestUID</code> argument.
     * </p>
     *  
     * @param uidMain   the main body of the Universally Unique IDentifier
     * @param strSuffix the suffix within the returned client request UUID
     * 
     * @return  a new <code>ClientRequestUID</code> instance formed by appending the given string to the given UUID
     */
    public static ClientRequestUID  from(ClientRequestUID uidMain, String strSuffix) {
        String  strUid = uidMain.requestId + strSuffix;
        
        return new ClientRequestUID(strUid);
    }
    
    /**
     * <p>
     * Creates a pseudo-random unique client request identifier.
     * </p>
     * <p>
     * This is the preferred creator for client request UIDs.
     * </p>
     * <p>
     * The returned request UID is built using the Java Universal Unique IDentifier static generation
     * class <code>{@link UUID}</code>.  Specifically, the method <code>{@link UUID#randomUUID()}</code>
     * is used to create the main body of the request UID.  The prefix of the returned UID is given
     * by the constant <code>{@link #STR_PREFIX}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The length of the returned client UID string is 36 plus the length of the class prefix string
     * <code>{@link #STR_PREFIX}</code>.  See <code>{@link #random(String)}</code> for further details.
     * </p>
     * 
     * @return  a new <code>ClientRequestUID</code> instance with prefix <code>{@link #STR_PREFIX}</code> and 
     *          randomly generated UUID body
     *          
     * @see #random(String)
     */
    public static ClientRequestUID   random() {
        return ClientRequestUID.random(STR_PREFIX);
    }

    /**
     * <p>
     * Creates a pseudo-random unique client request identifier with the given prefix.
     * </p>
     * <p>
     * The returned request UID is built using the Java Universal Unique IDentifier static generation
     * class <code>{@link UUID}</code>.  Specifically, the method <code>{@link UUID#randomUUID()}</code>
     * is used to create the main body of the request UID.  The prefix of the returned UID is given
     * by the argument.
     * </p>
     * <p>
     * <p>
     * <h2>NOTES</h2>
     * A UUID has a 128 bit representation, which is equivalent to 2 Java long integers, or 32 hexadecimal values.
     * The Java string representation is of the form
     * <pre>
     *      [time_low] "-" [time_mid] "-" [time_high_and_version] "-" [variant_and_sequence] "-" [node]
     * where 
     *   [time_low] is 8 characters
     *   [time_mid] is 4 characters
     *   [time_high_and_version] is 4 characters
     *   [variant_and_sequence]  is 4 characters
     *   [node] is 12 characters
     *   
     * and each character is a hexadecimal digit in { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, A, B, C, D, E, F}.
     * </pre>
     * Thus, the string length of the UUID body is 36 characters.  The total string length of the request
     * UID is 36 plus the argument prefix length. 
     * </p>
     * 
     * @param strPrefix prefix string for the randomly generated UUID string
     * 
     * @return  a new <code>ClientRequestUID</code> instance with given prefix and randomly generated UUID body
     */
    public static ClientRequestUID   random(String strPrefix) {
        UUID    uuidRqst = UUID.randomUUID();
        String  strRqstId = strPrefix + uuidRqst.toString();
        
        return new ClientRequestUID(strRqstId);
    }
    
}
