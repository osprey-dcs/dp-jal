/*
 * Project: dp-api-common
 * File:	ProtoTime.java
 * Package: com.ospreydcs.dp.api.grpc.util
 * Type: 	ProtoTime
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

import java.time.Duration;
import java.time.Instant;

import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.grpc.v1.common.DataTimestamps;
import com.ospreydcs.dp.grpc.v1.common.SamplingClock;
import com.ospreydcs.dp.grpc.v1.common.Timestamp;

/**
 * <p>
 * Utility class for Data Platform Protobuf messages containing time information.
 * </p>  
 * <p>
 * Provides methods for dealing with timestamps and general time concerns within time-correlated data sets.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 7, 2024
 *
 */
public final class ProtoTime {
    
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
     * Computes the sum of the <code>{@link Timestamp}</code> message and the given number of nanoseconds.
     * </p>
     * <p>
     * Returns a new <code>{@link Timestamp}</code> message which represents the sum of the
     * arguments.  The returned value is normalized.  Specifically, the returned timestamp
     * has the nanosecond offset less than 
     * <code>{@link #LNG_TMS_NSECS_LUB}</code> = {@value #LNG_TMS_NSECS_LUB}.
     * </p> 
     * 
     * @param tms               addend
     * @param lngAddendNanos    number of nanoseconds to be added to addend
     * 
     * @return  new <code>{@link Timestamp} representing the sum of the argument values
     */
    public static Timestamp addNanos(Timestamp tms, long lngAddendNanos) {
        long    lngSecs = tms.getEpochSeconds();
        long    lngNanos = tms.getNanoseconds();
        
        // Sum in the addend
        lngNanos += lngAddendNanos;
        
        // Normalize if necessary
        if (lngNanos >= ProtoTime.LNG_TMS_NSECS_LUB) {
            lngSecs += lngNanos / ProtoTime.LNG_TMS_NSECS_LUB;
            lngNanos = lngNanos % ProtoTime.LNG_TMS_NSECS_LUB;
        }
        
        Timestamp   tmsSum = Timestamp.newBuilder()
                .setEpochSeconds(lngSecs)
                .setNanoseconds(lngNanos)
                .build();
        
        return tmsSum;
    }
    
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
        if (lngNsecs < ProtoTime.LNG_TMS_NSECS_LUB)
            return tms;
        
        
        // Create the normal representative of the Timestamp equivalence class and return
        long    lngNrmSecs = lngSecs + lngNsecs / ProtoTime.LNG_TMS_NSECS_LUB;
        long    lngNrmNsecs = lngNsecs % ProtoTime.LNG_TMS_NSECS_LUB;
        
        Timestamp.Builder bldr = Timestamp.newBuilder();
        bldr.setEpochSeconds(lngNrmSecs);
        bldr.setNanoseconds(lngNrmNsecs);
        
        return bldr.build();
    }
    
    /**
     * <p>
     * Compares two <code>{@link Timestamp}</code> Protobuf messages for strict equality.
     * </p>
     * <p>
     * Two <code>Timestamp</code> messages are strictly equal if both field values are exactly equal.
     * Specifically, the following conditions are tested in order:
     * <ul>
     * <li>The epoch seconds fields are compared for equality.</li>
     * <li>The nanoseconds offset fields are compared for equality.</li>
     * </ul>
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * The <code>{@link #equals(Timestamp, Timestamp)}</code> comparison is faster than the 
     * <code>{@link #equivalence(Timestamp, Timestamp)}</code> comparison but not as inclusive.
     * </p>
     * 
     * @param tms1  first Timestamp message for comparison
     * @param tms2  second Timestamp message for comparison
     * 
     * @return  <code>true</code> if the two timestamps message are strictly equal with regards to field values
     *          <code>false</code> otherwise
     *          
     * @see #equivalence(Timestamp, Timestamp)
     */
    public static boolean equals(Timestamp tms1, Timestamp tms2) {
        
        if (tms1.getEpochSeconds() != tms2.getEpochSeconds())
            return false;
        if (tms1.getNanoseconds() != tms2.getNanoseconds())
            return false;
        
        return true;
    }

    
    /**
     * <p>
     * Compares two <code>{@link Timestamp}</code> Protobuf messages for equivalence.
     * </p>
     * <p>
     * The need for this method (over the Java <code>{@link ProtoTime#equals(Timestamp, Timestamp)})</code> 
     * arises because of the pathological case where two timestamps have different field values but describe 
     * the same time instant. This occurs whenever {@link Timestamp#getNanoseconds()} >= 10<sup>9</sup>.
     * Thus, two <code>Timestamp</code> messages can be equivalent but not equal.
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
     *          
     * @see #equals(Timestamp, Timestamp)
     */
    public static boolean equivalence(Timestamp tms1, Timestamp tms2) {
        return (ProtoTime.compare(tms1, tms2) == 0);
    }
    
    /**
     * <p>
     * Ordered comparison of the two <code>{@link Timestamp}</code> Protobuf messages.
     * </p>
     * <p>
     * The method returns the Java standard comparison result for two ordered objects.
     * In this case the result is determined as follows:
     * <ul>
     * <li>negative integer - iff <code>tms1</code> occurs <em>before</em> <code>tme2</code>.</li> 
     * <li>0 - iff <code>tms1</code> occurs <em>exactly at</em> <code>tme2</code>.</li>
     * <li>positive integer - iff <code>tms1</code> occurs <em>after</em> <code>tme2</code>.</li>
     * </ul>
     * The magnitude of the result can reflect the difference in values, although this is not
     * guaranteed.
     * </p>
     * <p>
     * Internally the two arguments are compared as their equivalent Java <code>{@link Instant}</code>
     * types.
     * </p>
     *  
     * @param tms1  first Timestamp message for comparison
     * @param tms2  second Timestamp message for comparison
     * 
     * @return comparison result as would be returned by <code>{@link Comparator}</code> interface 
     */
    public static int   compare(Timestamp tms1, Timestamp tms2) {
        Instant ins1 = ProtoMsg.toInstant(tms1);
        Instant ins2 = ProtoMsg.toInstant(tms2);
        
        return ins1.compareTo(ins2);
    }
    
    /**
     * <p>
     * Compares the two Protobuf messages for strict equality.
     * </p>
     * <p>
     * The Protobuf message describe uniform sampling times.  They are "equal" if the following conditions hold
     * (which are tested in order):
     * <ol>
     * <li>They have equal sampling period.</li>
     * <li>They have equal sample count.</li>
     * <li>Their starting times are equal <code>{@link #equals(Timestamp, Timestamp)}</code> == <code>true</code>.</li>
     * </ol>
     * <p>
     * 
     * @param msg1  first Protobuf message for comparison
     * @param msg2  second Protobuf message for comparison
     * 
     * @return  <code>true</code> if both messages describe the same sampling set,
     *          <code>false</code> otherwise 
     */
    public static boolean equals(SamplingClock msg1, SamplingClock msg2) {
        
        // Foremost the sampling periods must be equal
        long    lngPer1 = msg1.getPeriodNanos();
        long    lngPer2 = msg2.getPeriodNanos();
        
        if (lngPer1 != lngPer2)
            return false;
        
        // They must have the same number of samples
        if (msg1.getCount() != msg2.getCount())
            return false;
        
        // The must start at the same time instant
        if (!ProtoTime.equals(msg1.getStartTime(), msg2.getStartTime()))
            return false;
        
        // If we are here they are equal
        return true;
    }
    
    /**
     * <p>
     * Compares the two Protobuf messages for equivalence.
     * </p>
     * <p>
     * The Protobuf message describe uniform sampling times.  The equivalence qualifier applies to the
     * sampling start time field.
     * The two arguments are "equivalent" if the following hold (which are tested in order):
     * <ol>
     * <li>They have equal sampling period.</li>
     * <li>They have equal sample count.</li>
     * <li>Their starting times are equivalent <code>{@link #equivalence(Timestamp, Timestamp)}</code> == <code>true</code>.</li>
     * </ol>
     * <p>
     * 
     * @param msg1  first Protobuf message for comparison
     * @param msg2  second Protobuf message for comparison
     * 
     * @return  <code>true</code> if both messages describe the equivalent sampling set,
     *          <code>false</code> otherwise
     *          
     * @see #equivalence(Timestamp, Timestamp)
     */
    public static boolean equivalence(SamplingClock msg1, SamplingClock msg2) {
        
        // Foremost the sampling periods must be equal
        long    lngPer1 = msg1.getPeriodNanos();
        long    lngPer2 = msg2.getPeriodNanos();
        
        if (lngPer1 != lngPer2)
            return false;
        
        // They must have the same number of samples
        if (msg1.getCount() != msg2.getCount())
            return false;
        
        // The must start at the same time instant
        if (!ProtoTime.equivalence(msg1.getStartTime(), msg2.getStartTime()))
            return false;
        
        // If we are here they are equivalent
        return true;
    }
    
    /**
     * <p>
     * Determines whether or not the two Protobuf messages describe sampling intervals with intersecting time domains.
     * </p>
     * 
     * @param msg1  first Protobuf message for comparison
     * @param msg2  second Protobuf message for comparison
     * 
     * @return  <code>true</code> if sampling intervals have intersecting time domains
     *          <code>false</code> if the sampling intervals are disjoint
     */
    public static boolean hasIntersection(SamplingClock msg1, SamplingClock msg2) {
        TimeInterval    ivl1 = ProtoTime.domain(msg1);
        TimeInterval    ivl2 = ProtoTime.domain(msg2);
        
        return ivl1.hasIntersectionClosed(ivl2);
    }
    
    /**
     * <p>
     * Computes and returns the time domain of the sampling interval described by the argument.
     * </p>
     * 
     * @param msgTms    Protobuf message describing a uniform sampling interval
     * 
     * @return  the time domain of the given sampling interval
     */
    public static TimeInterval domain(SamplingClock msgTms) {
        Instant     insStart = ProtoMsg.toInstant(msgTms.getStartTime());
        Duration    dur = Duration.ofNanos(msgTms.getCount() * msgTms.getPeriodNanos());
        Instant     insStop = insStart.plus(dur);
        
        TimeInterval    ivl = TimeInterval.from(insStart, insStop);
        
        return ivl;
    }
    
    
    //
    // Private Methods
    //
    
    /**
     * <p>
     * Prevent construction of <code>ProtoTime</code> instances.
     * </p>
     */
    private ProtoTime() {
    }

}
