/*
 * Project: dp-api-common
 * File:	UniformSamplingClock.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	UniformSamplingClock
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
import java.util.ArrayList;

import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.model.TimeInterval;
import com.ospreydcs.dp.grpc.v1.common.FixedIntervalTimestampSpec;

/**
 * <p>
 * Represents a sampling clock with uniform sampling and active for a finite duration.
 * </p> 
 * <p>
 * Contains parameters defining a uniform sampling clock active for a finite duration.
 * The clock can be used to generate the timestamps for a finite interval of uniform samples.
 * </p>
 * <p>
 * Note that <code>UniformSamplingClock</code> instances can be created from the Protobuf
 * message representing a sampling interval using creator 
 * <code>{@link #from(FixedIntervalTimestampSpec)}</code>.
 *
 * @author Christopher K. Allen
 * @since Jan 7, 2024
 *
 * @see #from(FixedIntervalTimestampSpec)
 */
public class UniformSamplingClock implements Comparable<Instant> { 

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
     * Create a new, initialized instance of <code>UniformSamplingClock</code> from the given
     * Protobuf message representing a sampling interval.
     * </p>
     * 
     * @param msgTms    Protobuf message representing a uniform sampling interval
     * 
     * @return  new <code>UniformSamplingClock</code> instance initialized from the argument
     */
    public static UniformSamplingClock    from(FixedIntervalTimestampSpec msgTms) {
        Instant insStart = ProtoMsg.toInstant(msgTms.getStartTime());
        int     intCount = msgTms.getNumSamples();
        long    lngPeriod = msgTms.getSampleIntervalNanos();
        
        return new UniformSamplingClock(insStart, intCount, lngPeriod, ChronoUnit.NANOS);
    }
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>UniformSamplingClock</code>.
     * </p>
     *
     * @param insStart  The starting instant of the sampling process 
     * @param intCount  The number of samples
     * @param lngPeriod The sampling period  
     * @param cuPeriod  The sampling period time units
     */
    public UniformSamplingClock(Instant insStart, int intCount, long lngPeriod, ChronoUnit cuPeriod) {
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
     * Returns the starting time instant of the sampling interval
     * 
     * @return sampling interval start instant
     */
    public Instant getStartInstant() {
        return this.insStart;
    }
    
    /**
     * Returns the number of samples recorded by this clock.
     * 
     * @return  the total sample count where clock is active
     */
    public int getSampleCount() {
        return this.intCount;
    }
    
    /**
     * <p>
     * Returns the sample period.
     * </p>
     * <p>
     * Use the method <code>{@link #getSamplePeriodUnits()}</code> to recover the
     * sample period time units.
     * </p>
     * 
     * @return  return the sample period for the clock
     * 
     * @see #getSamplePeriodUnits()
     */
    public long getSamplePeriod() {
        return this.lngPeriod;
    }
    
    /**
     * Returns the time units of the sample period.
     * 
     * @return  unit of time for the sample period
     * 
     * @see #getSamplePeriod()
     */
    public ChronoUnit   getSamplePeriodUnits() {
        return this.cuPeriod;
    }
    
    /**
     * <p>
     * Returns the time domain over which this clock is active.
     * </p>
     * <p>
     * The returned interval is given by 
     * <pre>
     *      [<i>t</i><sub>0</sub>, <i>t</i><sub><i>N</i>-1</sub>]
     * </pre>
     * where <i>t</i><sub>0</sub> is the first timestamp of the clock duration 
     * (given by <code>{@link #getStartInstant()}</code>) and <i>t</i><sub><i>N</i>-1</sub>
     * is the last timestamp of the clock duration.  Note that <i>N</i> is the number
     * of timestamps returned by <code>{@link #getSampleCount()}</code>.
     * <p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned interval DOES NOT include the sampling period duration
     * after the final timestamp.
     * </p>
     *  
     * @return  the time domain which the clock is active, minus the final period duration
     */
    public TimeInterval getTimeDomain() {
        return this.ivlDomain;
    }
    
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
    public boolean  hasDomainIntersection(UniformSamplingClock domCmp) {
        return this.ivlDomain.hasIntersectionClosed(domCmp.ivlDomain);
    }

    
    //
    // Operations
    // 
    
    /**
     * <p>
     * Creates a vector (<code>ArrayList</code>) of time instants embodied by this <code>UniformSamplingClock</code>.
     * </p>
     * <p>
     * An ordered vector of Java <code>{@link Instant}</code> objects is created according
     * to the parameters of this <code>UniformSamplingClock</code>.
     * </p>
     * 
     * @return  a new vector of timestamp instants represented by this object
     */
    public ArrayList<Instant>  createTimestamps() {
        ArrayList<Instant>     vecTms = new ArrayList<>(this.intCount);
        
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
