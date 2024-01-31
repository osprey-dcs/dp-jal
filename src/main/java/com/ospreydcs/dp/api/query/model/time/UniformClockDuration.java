/*
 * Project: dp-api-common
 * File:	UniformClockDuration.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	UniformClockDuration
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
package com.ospreydcs.dp.api.query.model.time;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Vector;

import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.model.TimeInterval;
import com.ospreydcs.dp.grpc.v1.common.FixedIntervalTimestampSpec;

/**
 * <p>
 * Contains parameters defining the timestamps for a finite interval of uniform samples.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Jan 7, 2024
 *
 */
public class UniformClockDuration implements Comparable<Instant> { 

    //
    // Defining Attributes
    //
    
    /** The starting instant of the sampling process */
    private final Instant       insStart;
    
    /** The number of samples */
    private final int           intCount;
    
    /** The sampling period duration */
    private final long          lngPeriod;
    
    /** The sampling period time units */
    private final ChronoUnit    cuPeriod;
    
    
    //
    // Consistent Parameters
    //
    
    /** The sampling period as a <code>Duration</code> instance */
    private final Duration      durPeriod;
    
    /** The time domain of the sampling process */
    private final TimeInterval  ivlDomain;
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Create a new, initialized instance of <code>UniformClockDuration</code> from the given
     * Protobuf message.
     * </p>
     * 
     * @param msgTms    Protobuf message representing a uniform sampling interval
     * 
     * @return  new <code>UniformClockDuration</code> instance initialized from the argument
     */
    public static UniformClockDuration    from(FixedIntervalTimestampSpec msgTms) {
        Instant insStart = ProtoMsg.toInstant(msgTms.getStartTime());
        int     intCount = msgTms.getNumSamples();
        long    lngPeriod = msgTms.getSampleIntervalNanos();
        
        return new UniformClockDuration(insStart, intCount, lngPeriod, ChronoUnit.NANOS);
    }
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>UniformClockDuration</code>.
     * </p>
     *
     * @param insStart  The starting instant of the sampling process 
     * @param intCount  The number of samples
     * @param lngPeriod The sampling period  
     * @param cuPeriod  The sampling period time units
     */
    public UniformClockDuration(Instant insStart, int intCount, long lngPeriod, ChronoUnit cuPeriod) {
        this.insStart = insStart;
        this.intCount = intCount;
        this.lngPeriod = lngPeriod;
        this.cuPeriod = cuPeriod;
        
        // Compute consistent parameters
        this.durPeriod = Duration.of(lngPeriod, cuPeriod);
        
        Duration durIvl = this.durPeriod.multipliedBy(intCount - 1);
        Instant insStop = this.insStart.plus(durIvl);
        this.ivlDomain = TimeInterval.from(insStart, insStop);
    }

    //
    // Queries
    //
    
    /**
     * <p>
     * Compares the sampling domain of the given sampling set with this one.
     * </p>
     * <p>
     * Returns <code>true</code> if the given sampling domain has a non-empty set intersection
     * with this one.  Domains are treated as closed intervals, so if domain intervals share
     * a common end point a value <code>true</code> is returned.
     * </p>
     * 
     * @param domCmp uniform sampling set under domain comparison 
     * 
     * @return <code>true</code> if the given sampling set collides with this one,
     *         <code>false</code> otherwise
     */
    public boolean  hasIntersection(UniformClockDuration domCmp) {
        return this.ivlDomain.hasIntersectionClosed(domCmp.ivlDomain);
    }

    
    //
    // Operations
    // 
    
    /**
     * <p>
     * Creates a vector of time instants embodied by this <code>UniformClockDuration</code>.
     * </p>
     * <p>
     * An ordered vector of Java <code>{@link Instant}</code> objects is created according
     * to the parameters of this <code>UniformClockDuration</code>.
     * </p>
     * 
     * @return  a new vector of timestamp instants represented by this object
     */
    public Vector<Instant>  createTimestamps() {
        Vector<Instant>     vecTms = new Vector<>(this.intCount);
        
        Instant insTms = this.insStart;
        for (int n=0; n<this.intCount; n++) {
            vecTms.add(insTms);
            
            insTms = insTms.plus(this.durPeriod);
        }
        
        return vecTms;
    }
    
    
    //
    // Comparable Interface
    //

    /**
     *
     * @see @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Instant o) {
        return this.insStart.compareTo(o);
    }

}
