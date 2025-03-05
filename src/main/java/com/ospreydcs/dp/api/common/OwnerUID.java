/*
 * Project: dp-api-common
 * File:	OwnerUID.java
 * Package: com.ospreydcs.dp.api.common
 * Type: 	OwnerUID
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
 * @since Feb 25, 2025
 *
 */
package com.ospreydcs.dp.api.common;

import java.util.UUID;

/**
 * <p>
 * Record encapsulating the Unique Identifier of a Data Platform or Java API Library resource owner.
 * </p>
 * <p>
 * Owner UIDs may be required for various Data Platform services such as data set creation, and annotation 
 * creation.  This record is intended to abstract such UID in the event the UID type is modified in the
 * future.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Feb 25, 2025
 *
 */
public record OwnerUID(String ownerUid) {

    //
    // Constants
    //
    
    /** The universal null request UID, used when an UID is required but none is specified */
    public static final OwnerUID    NULL = OwnerUID.from("null");
    
    
    /** Prefix given to all randomly generated Java Client API request UIDs */
    public static final String  STR_PREFIX = "dp-api-java-";
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new <code>OwnerUID</code> record instance exactly from the given argument.
     * </p>
     * <p>
     * <h2>WARNING</h2>
     * The given argument should be universally unique within whatever context it is being used.
     * Thus, it is highly recommended that alternate creators are used for generation of owner UIDs 
     * when unique UIDs are required.
     * </p> 
     * 
     * @param strOwnerId  client request ID used for record
     * 
     * @return  new owner UID record initialized with the given argument
     */
    public static OwnerUID   from(String strOwnerId) {
        return new OwnerUID(strOwnerId);
    }
    
    /**
     * <p>
     * Creates a new <code>OwnerUID</code> instance from the given arguments.
     * </p>
     * <p>
     * The returned owner UUID is assembled from the given owner UUID argument
     * and the given suffix.  Specifically, the returned owner 
     * "unique identifier" is formed by appending the <code>String</code> argument to the 
     * <code>OwnerUID</code> argument.
     * </p>
     *  
     * @param uidMain   the main body of the Universally Unique IDentifier
     * @param strSuffix the suffix within the returned owner UUID
     * 
     * @return  a new <code>IngestRequestUID</code> instance formed by appending the given string to the given UUID
     */
    public static OwnerUID  from(OwnerUID uidMain, String strSuffix) {
        String  strUid = uidMain.ownerUid + strSuffix;
        
        return new OwnerUID(strUid);
    }
    
    /**
     * <p>
     * Creates a pseudo-random owner unique identifier.
     * </p>
     * <p>
     * This is the preferred creator for owner UIDs.
     * </p>
     * <p>
     * The returned owner UID is built using the Java Universal Unique IDentifier static generation
     * class <code>{@link UUID}</code>.  Specifically, the method <code>{@link UUID#randomUUID()}</code>
     * is used to create the main body of the request UID.  The prefix of the returned UID is given
     * by the constant <code>{@link #STR_PREFIX}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This method defers to <code>{@link #random(String)}</code> using the prefix <code>{@link #STR_PREFIX}</code>.
     * Thus, the length of the returned owner UID string is 36 plus the length of the class prefix string
     * <code>{@link #STR_PREFIX}</code>.  See <code>{@link #random(String)}</code> for further details.
     * </p>
     * 
     * @return  a new <code>IngestRequestUID</code> instance with prefix <code>{@link #STR_PREFIX}</code> and 
     *          randomly generated UUID body
     *          
     * @see #random(String)
     */
    public static OwnerUID   random() {
        return OwnerUID.random(STR_PREFIX);
    }

    /**
     * <p>
     * Creates a pseudo-random unique owner unique identifier with the given prefix.
     * </p>
     * <p>
     * The returned owner UID is built using the Java Universal Unique IDentifier static generation
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
     * Thus, the string length of the UUID body is 36 characters.  The total string length of the owner 
     * UID is 36 plus the argument prefix length. 
     * </p>
     * 
     * @param strPrefix prefix string for the randomly generated UUID string
     * 
     * @return  a new <code>OwnerUID</code> instance with given prefix and randomly generated UUID body
     */
    public static OwnerUID   random(String strPrefix) {
        UUID    uuidRqst = UUID.randomUUID();
        String  strRqstId = strPrefix + uuidRqst.toString();
        
        return new OwnerUID(strRqstId);
    }

    
    //
    // record Overrides
    //
    
    /**
     * <h1>
     * Compares the owner UID field for equality.
     * </h2>
     * <p>
     * This override is required to avoid Java equality comparison as Objects.
     * </p>
     * 
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof OwnerUID rec)
            return this.ownerUid.equals(rec.ownerUid);
        
        return false;
    }
    
}
