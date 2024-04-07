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
package com.ospreydcs.dp.api.model;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;

import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.grpc.v1.common.SamplingClock;

/**
 * <p>
 * Represents a finite-duration sampling clock with uniform sampling period.
 * </p> 
 * <p>
 * Contains parameters defining a uniform sampling clock active for a finite duration.  Instances can be used
 * to identify the sampling times for a uniform, time-series process.
 * The clock can be also used to generate the timestamps for a finite interval of uniform samples.
 * </p>
 * <p>
 * <h2>Protobuf Usage</h2>
 * Note that <code>UniformSamplingClock</code> instances can be created from the Protobuf
 * message representing a sampling interval using creator 
 * <code>{@link #from(SamplingClock)}</code>.
 * </p>
 * <p>
 * <h2>Comparable<Instant> Interface</h2>
 * The <code>UniformSamplingClock</code> class implements the <code>{@link Comparable}</code> interface bound to
 * the Java <code>{@link Instant}</code> class.  All comparisons are with respect to the starting time instant
 * of the sampling clock, given by <code>{@link #getStartInstant()}</code>.  Thus, <code>UniformSamplingClock</code>
 * instances can be sorted by start times.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Jan 7, 2024
 *
 * @see #from(SamplingClock)
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
     * @param msgClock    Protobuf message representing a finite-duration uniform sampling clock
     * 
     * @return  new <code>UniformSamplingClock</code> instance initialized from the argument
     * 
     * @see ProtoMsg#toUniformSamplingClock(SamplingClock)
     * 
     * @deprecated Use <code>{@link ProtoMsg#toUniformSamplingClock(SamplingClock)}</code> instead.
     */
    @Deprecated(since="April 5, 2024")
    public static UniformSamplingClock    from(SamplingClock msgClock) {
        Instant insStart = ProtoMsg.toInstant(msgClock.getStartTime());
        int     intCount = msgClock.getCount();
        long    lngPeriod = msgClock.getPeriodNanos();
        
        return new UniformSamplingClock(insStart, intCount, lngPeriod, ChronoUnit.NANOS);
    }
    
    /**
     * <p>
     * Creates a new <code>UniformSamplingClock</code> instance using the given defining parameters.
     * </p>
     *  
     * @param insStart  The starting instant of the sampling process 
     * @param intCount  The number of samples
     * @param lngPeriod The sampling period  
     * @param cuPeriod  The sampling period time units
     * 
     * @return  a new sampling clock instance with the given parameters
     * 
     * @throws IllegalArgumentException intCount < 0 or lngPeriod <= 0
     */
    public static UniformSamplingClock from(Instant insStart, int intCount, long lngPeriod, ChronoUnit cuPeriod) 
            throws IllegalArgumentException {

        return new UniformSamplingClock(insStart, intCount, lngPeriod, cuPeriod);
    }

    /**
     * <p>
     * Attempts to create a new <code>UniformSamplingClock</code> instance from an ordered list of timestamps.
     * </p>
     * <p>
     * This creator attempts to manifest a sampling clock instance that will produce the given argument with the
     * <code>{@link #createTimestamps()}</code> method.  Thus, the argument is assumed to have uniformly 
     * distributed, increasing sequence of timestamps in the order of sampling.  Additionally, to define the 
     * sampling period the argument must have at least two entries.  If any of these conditions fail an exception
     * is thrown.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The uniform sampling clock parameters are determined as follows:
     *   <ol> 
     *   <li>The start time for the clock is determined by the first entry.</li>
     *   <li>The sampling period is determined from the first two entries (i.e., the difference).</li>
     *   <li>The sample count is the size of the argument list.</li>
     *   </ol>
     * </li>
     * <li>
     * The following conditions on the argument must hold:
     *   <ol>
     *   <li>The argument must contain at least 2 entries or an exception is thrown.</li>  
     *   <li>The argument must be ordered with the earliest timestamp first or an exception is thrown.</li>
     *   <li>The argument timestamps must be equally spaced (by clock period) or an exception is thrown.</li>
     *   </ol>
     * </ul>
     * </p>
     * 
     * @param vecTms    ordered list of timestamps assumed to be uniformly distributed
     * 
     * @return a sampling clock that will produce the given argument
     * 
     * @throws MissingResourceException the argument contains less than 2 elements
     * @throws IllegalArgumentException the argument is not ordered correctly or has non-uniform distribute
     * 
     * @see #createTimestamps()
     */
    public static UniformSamplingClock  from(List<Instant> vecTms) throws MissingResourceException, IllegalArgumentException {
        
        // Check the size of the list (must be at least 2 entries to define a clock
        if (vecTms.size() <= 2)
            throw new MissingResourceException("UniformSamplingClock#from(List<Instant>) - argument must contain at least 2 elements.", List.class.getName(), "size()");

        // Get the sampling period from the first 2 entries
        Duration    durPeriod = Duration.between(vecTms.get(0), vecTms.get(1));
        
        // Check the ordering
        if (durPeriod.isNegative())
            throw new IllegalArgumentException("UniformSamplingClock#from(List<Instant>) - The argument ordering appears incorrect (earliest timestamp first)");
        
        // Check the argument for uniform sampling by looping through all timestamps
        Integer         indCurr = 0;
        Instant         insPrev = null;
        List<Integer>   lstBadIndices = new LinkedList<>();
        for (Instant insCurr : vecTms) {
            
            // Loop initialization 
            if (insPrev == null) {
                insPrev = insCurr;
                indCurr++;
                
                continue;
            }
            
            // Compare duration between current and previous timestamps with period - record if different
            //   NOTE that this also checks ordering otherwise a negative duration is produced 
            Duration durCurr = Duration.between(insPrev, insCurr);
            
            if (!durCurr.equals(durPeriod))
                lstBadIndices.add(indCurr);
            
            insPrev = insCurr;
            indCurr++;
        }
        
        // Throw exception if non-uniform sampling
        if (!lstBadIndices.isEmpty())
            throw new IllegalArgumentException("UniformSamplingClock#from(List<Instant>) - The argument indicates non-uniform sampling at indices " + lstBadIndices);

        
        // Get the sampling clock parameters
        Instant     insStart = vecTms.get(0);
        int         cntSamples = vecTms.size();
        long        lngPeriodNs = durPeriod.toNanos();
        ChronoUnit  enmPeriodUnit = ChronoUnit.NANOS;
        
        // Create the sampling clock and return it
        UniformSamplingClock    clk = new UniformSamplingClock(insStart, cntSamples, lngPeriodNs, enmPeriodUnit);
        
        return clk;
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
     * 
     * @throws IllegalArgumentException intCount < 0 or lngPeriod <= 0
     */
    public UniformSamplingClock(Instant insStart, int intCount, long lngPeriod, ChronoUnit cuPeriod) {
        
        // Check Argument
        if (intCount < 0 || lngPeriod <= 0)
            throw new IllegalArgumentException("Sampling count < 0 and/or sample periods <= 0.");
        
        this.insStart = insStart;
        this.intCount = intCount;
        this.lngPeriod = lngPeriod;
        this.cuPeriod = cuPeriod;
        
        // Compute consistent parameters
        this.durPeriod = Duration.of(lngPeriod, cuPeriod);
        
        // Special case - intCount == 0
        if (intCount == 0) {
            this.ivlDomain = TimeInterval.from(insStart, insStart);
            
            return;
        }
        
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
    // Object Overrides - Debugging
    //
    
    /**
     * @see @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object objClock) {
        
        // Check type and cast
        if (objClock instanceof UniformSamplingClock clk)
            ;
        else
            return false;
        
        // Check defining parameters
        if (!this.insStart.equals(clk.insStart))
            return false;
        if (this.intCount != clk.intCount)
            return false;
        if (this.lngPeriod != clk.lngPeriod)
            return false;
        if (this.cuPeriod != clk.cuPeriod)
            return false;
     
        return true;
    }
    
    /**
     * @see @see java.lang.Object#toString()
     */
    @Override
    public String   toString() {
        String  strText = this.getClass().getName() + ": (";
        
        strText += "Start time=" + this.insStart + ", ";
        strText += "Sample count=" + this.intCount + ", ";
        strText += "Period=" + this.lngPeriod + " " + this.cuPeriod + ", "; 
        strText += "Duration=" + this.durPeriod + ")";
        strText += "Domain=" + this.ivlDomain + ")"; 
        
        return strText;
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
