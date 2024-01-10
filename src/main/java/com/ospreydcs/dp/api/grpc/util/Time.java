/*
 * Project: dp-api-common
 * File:	Time.java
 * Package: com.ospreydcs.dp.api.grpc.util
 * Type: 	Time
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
 * @since Jan 7, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.util;

import java.time.Instant;

import com.ospreydcs.dp.grpc.v1.common.FixedIntervalTimestampSpec;
import com.ospreydcs.dp.grpc.v1.common.Timestamp;

/**
 * <p>
 * Utility class for dealing with timestamps and general time concerns within time-correlated data
 * sets.
 * </p>  
 *
 * @author Christopher K. Allen
 * @since Jan 7, 2024
 *
 */
public final class Time {
    
    //
    // Class Constants
    //
    
    /** Least upper bound for the Timestamp nanosecond offset (co-size of equivalence classes) */
    public static final long    LNG_TMS_NSECS_LUB = 1_000_000_000L;

    
    //
    // Utility Methods
    //
    
    /**
     * <p>
     * Returns the normalized <code>{@link Timestamp}</code> message of the argument.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * - If the argument is already normal then the argument is returned.
     * <br/><br/>
     * - If the nanosecond offset of the given <code>Timestamp</code> message is larger than
     * 10<sup>9</sup> then a new <code>Timestamp</code> message is created and populated with
     * the normalized time instant.
     * </p>
     * <h2>DEFINITION:</h2>
     * Let (SECS, NANOSECS) be the normal representative of all equivalence classes of numeric pairs 
     * (secs, nanosecs) defining a time instant in the current epoch.
     * We define the normal of any such pair (secs, nsecs) by 
     * <br/><br/>
     * &nbsp; &nbsp; SECS = secs + (nanosecs / 1_000_000_000L)
     * <br/>
     * &nbsp; &nbsp; NANOSECS = nanosecs % 1_000_000_000L
     * <br/></br>
     * where all quantities are assumed long integers.
     * </p>
     * 
     * @param tms   Protobuf message representing a time instant in the current epoch
     * 
     * @return new normalized <code>Timestamp</code> if argument is NOT normal,
     *         the original <code>Timestamp</code> message if argument is normal
     */
    public static Timestamp normalize(Timestamp tms) {
        long    lngSecs = tms.getEpochSeconds();
        long    lngNsecs = tms.getNanoseconds();
        
        // Check if timestamp is already normalized
        if (lngNsecs < Time.LNG_TMS_NSECS_LUB)
            return tms;
        
        
        // Create the normal representative of the Timestamp equivalence class and return
        long    lngNrmSecs = lngSecs + lngNsecs / Time.LNG_TMS_NSECS_LUB;
        long    lngNrmNsecs = lngNsecs % Time.LNG_TMS_NSECS_LUB;
        
        Timestamp.Builder bldr = Timestamp.newBuilder();
        bldr.setEpochSeconds(lngNrmSecs);
        bldr.setNanoseconds(lngNrmNsecs);
        
        return bldr.build();
    }
    
    /**
     * <p>
     * Compares two <code>{@link Timestamp}</code> messages for equivalence.
     * </p>
     * <p>
     * The need for this method (over the Java <code>{@link Object#equals(Object)})</code> arises because of  
     * the pathological case where two timestamps have different field values but describe the same time instant.
     * This occurs whenever {@link Timestamp#getNanoseconds()} >= 10<sup>9</sup>.
     * </p>  
     * <p>
     * <h2>NOTE</h2>
     * The reduction of any such pathological case (secs, nanosecs) to the normalized representative (SECS, NANOSECS) 
     * of the equivalence class of timestamps (secs, nanosecs) where nanosecs < 10<sup>9</sup> is given by
     * <br/><br/>
     * &nbsp; &nbsp; SECS = secs + (nanosecs / 1_000_000_000L)
     * <br/>
     * &nbsp; &nbsp; NANOSECS = nanosecs % 1_000_000_000L
     * <br/></br>
     * where all quantities are assumed long integers.
     * </p>
     * 
     * @param tms1  first Timestamp message for comparison
     * @param tms2  second Timestamp message for comparison
     * 
     * @return  <code>true</code> if the two timestamps are of the same equivalence class
     *          <code>false</code> otherwise
     */
    public static boolean equals(Timestamp tms1, Timestamp tms2) {
        Instant tm1 = ProtoMsg.toInstant(tms1);
        Instant tm2 = ProtoMsg.toInstant(tms2);
        
        return (tm1.compareTo(tm2) == 0);
    }
    
    /**
     * <p>
     * Compares the two Protobuf messages for equivalence.
     * </p>
     * <p>
     * The Protobuf message describe uniform sampling times.  They are "equal" if the following hold
     * (which are tested in order):
     * <ol>
     * <li>They have the same sampling period.</li>
     * <li>Their starting times are equivalent <code>{@link #equals(Timestamp, Timestamp)}</code> == <code>true</code>.</li>
     * <li>They have the same sample count.</li>
     * </ol>
     * <p>
     * 
     * @param msg1  first Protobuf message for comparison
     * @param msg2  second Protobuf message for comparison
     * 
     * @return  <code>true</code> if both messages describe the same sampling set,
     *          <code>false</code> otherwise 
     */
    public static boolean equals(FixedIntervalTimestampSpec msg1, FixedIntervalTimestampSpec msg2) {
        
        // Foremost the sampling periods must be equal
        long    lngPer1 = msg1.getSampleIntervalNanos();
        long    lngPer2 = msg2.getSampleIntervalNanos();
        
        if (lngPer1 != lngPer2)
            return false;
        
        // The must start at the same time instant
        if (!Time.equals(msg1.getStartTime(), msg2.getStartTime()))
            return false;
        
        // They must have the same number of samples
        if (msg1.getNumSamples() != msg2.getNumSamples())
            return false;
        
        // If we are here they are equal
        return true;
    }
    
    /**
     * <p>
     * Prevent construction of <code>Time</code> instances.
     * </p>
     */
    private Time() {
    }

}
