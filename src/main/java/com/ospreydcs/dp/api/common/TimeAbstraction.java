/*
 * Project: dp-api-common
 * File:	TimeAbstraction.java
 * Package: com.ospreydcs.dp.api.common
 * Type: 	TimeAbstraction
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
 * @since Jul 6, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.common;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Record class abstracting a time duration.
 * </p>
 * <p>
 * This is a convenience class supplementing the Java representations for a time duration.
 * There are 3 common method supported by Java for representing a time duration:
 * <ol>
 * <li>
 * The pair (<code>long, ChronoUnit)</code> where <code>long</code> is the duration and enumeration
 * <code>{@link ChronoUnit}</code> contains the units for the duration.
 * </li>
 * <li>
 * The pair (<code>long, TimeUnit)</code> where <code>long</code> is the duration and enumeration
 * <code>{@link TimeUnit}</code> contains the units for the duration.
 * </li>
 * <li>
 * The class <code>{@link Duration}</code> containing the time duration which can be expressed in
 * various units according to its exposed method API. 
 * </li>
 * </ol>
 * This record provides quick conversion between the 3 methods offer all options after creation.
 * The objective is to create time durations using arbitrary (amount, unit) pairs then
 * convert to a <code>Duration</code> object for unit conversion.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * This class behaves as a <code>record</code> instance where once created it cannot be modified.
 * That is, it has no state.
 * </li>
 * <li>
 * Class <code>{@link Duration}</code> explicitly abstracts a time duration providing the
 * amount of time in arbitrary units.  This record is essentially an extension of that class where the
 * original time units are preserved.
 * </li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 6, 2024
 * 
 * @param amount        the time duration quantity 
 * @param chronoUnit    the time duration units as <code>{@link ChronoUnit}</code> enumeration
 * @param timeUnit      the time duration units as <code>{@link TimeUnit}</code> enumeration
 * @param duration      The equivalent <code>Duration</code> instance  
 */
public class TimeAbstraction {
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new, initialized <code>TimeAbstraction</code> instance with the given properties.
     * </p>
     * 
     * @param amount    time duration amount
     * @param enmUnits  time duration units as <code>ChronoUnit</code> enumeration constant
     * 
     * @return  new <code>TimeAbstraction</code> instance with the given amount of time
     */
    public static TimeAbstraction  from(long amount, ChronoUnit enmUnits) {
        return new TimeAbstraction(amount, enmUnits);
    }
    
    /**
     * <p>
     * Creates a new, initialized <code>TimeAbstraction</code> instance with the given properties.
     * </p>
     * 
     * @param amount    time duration amount
     * @param enmUnits  time duration units as <code>TimeUnit</code> enumeration constant
     * 
     * @return  new <code>TimeAbstraction</code> instance with the given amount of time
     */
    public static TimeAbstraction  from(long amount, TimeUnit enmUnits) {
        return new TimeAbstraction(amount, enmUnits);
    }
    
    /**
     * <p>
     * Creates a new, initialized <code>TimeAbstraction</code> instance from the given duration.
     * </p>
     * <p>
     * The units of the time duration are available through <code>{@link #getChronoUnit()}</code> or
     * <code>{@link #getTimeUnit()}</code>.  Use <code>{@link #getDuration()}</code> to recover the
     * time duration in abstract form.
     * </p>
     * 
     * @param duration  the time duration
     * 
     * @return  new <code>TimeAbstraction</code> instance with the given duration
     */
    public static TimeAbstraction from(Duration duration) {
        return new TimeAbstraction(duration);
    }
    
    
    //
    // Class Constants
    //
    
    /** The default time units used for construction by <code>Duration</code> instance. */
    public static final TimeUnit            UNIT_DEF = TimeUnit.NANOSECONDS;
    
    /** The zero valued time duration */
    public static final TimeAbstraction     ZERO = TimeAbstraction.from(Duration.ZERO);
    
    
    //
    // Instance Attributes
    //
    
    /** The time duration quantity */
    public final long          amount;
    
    /** The time duration units as <code>{@link ChronoUnit}</code> enumeration */
    public final ChronoUnit    chronoUnit;
    
    /** The time duration units as <code>{@link TimeUnit}</code> enumeration */
    public final TimeUnit      timeUnit;
    
    /** The equivalent <code>Duration</code> instance */
    public final Duration      duration;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>TimeAbstraction</code> from the arguments.
     * </p>
     *
     * @param amount    the time duration quantity
     * @param enmUnits  the time duration units as <code>ChronoUnit</code> enumeration
     */
    public TimeAbstraction(long amount, ChronoUnit enmUnits) {
        this.amount = amount;
        this.chronoUnit = enmUnits;
        this.timeUnit = TimeUnit.of(enmUnits);
        this.duration = Duration.of(amount, enmUnits);
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>TimeAbstraction</code> from the arguments.
     * </p>
     *
     * @param amount    the time duration amount
     * @param enmUnits  the time duration units as <code>TimeUnit</code> enumeration
     */
    public TimeAbstraction(long amount, TimeUnit enmUnits) {
        this(amount, enmUnits.toChronoUnit());
    }
    
    /**
     * <p>
     * Constructs a new <code>TimeAbstraction</code> instance from the argument.
     * </p>
     * <p>
     * The time units used for the internal representation of the time duration are given
     * by the class constant <code>{@link #UNIT_DEF}</code> = {@value #UNIT_DEF}.
     * </p>
     *
     * @param duration  the time duration
     */
    public TimeAbstraction(Duration duration) {
        this(UNIT_DEF.convert(duration), UNIT_DEF);
    }

    
    //
    // Attribute Query
    //
    
    /**
     * <p>
     * Return the time duration amount as specified at construction.
     * </p>
     * <p>
     * The units of the time duration are available through <code>{@link #getChronoUnit()}</code> or
     * <code>{@link #getTimeUnit()}</code>.  Use <code>{@link #getDuration()}</code> to recover the
     * time duration in abstract form.
     * </p>
     * 
     * @return  the length of time within the duration
     */
    public long getAmount() {
        return this.amount;
    }
    
    /**
     * <p>
     * Return the time duration units as a Java <code>{@link ChronoUnit}</code> enumeration constant.
     * </p>
     * <p>
     * The returned value is that given at construction/creation, or as nanoseconds if a <code>Duration</code>
     * instance was provided.
     * </p>
     * 
     * @return  the units of the time duration corresponding to the amount <code>{@link #getAmount()}</code>
     */
    public ChronoUnit getChronoUnit() {
        return this.chronoUnit;
    }
    
    /**
     * <p>
     * Return the time duration units as a Java <code>{@link TimeUnit}</code> enumeration constant.
     * </p>
     * <p>
     * The returned value is that given at construction/creation, or as nanoseconds if a <code>Duration</code>
     * instance was provided.
     * </p>
     * 
     * @return  the units of the time duration corresponding to the amount <code>{@link #getAmount()}</code>
     */
    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }
    
    /**
     * <p>
     * Return time duration as Java <code>{@link Duration}</code> instance.
     * </p>
     * <p>
     * Note that the <code>Duration</code> class maintains the duration amount in arbitrary units.
     * The returned quantity may be readily converted between time units.
     * </p>
     *   
     * @return  the time duration as a Java <code>Duration</code> instance
     */
    public Duration getDuration() {
        return this.duration;
    }
}
