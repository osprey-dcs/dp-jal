/*
 * Project: dp-api-common
 * File:	TimeDuration.java
 * Package: com.ospreydcs.dp.api.common
 * Type: 	TimeDuration
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
 * Class representing a duration of time.
 * </p>
 * <p>
 * This is a convenience record supplementing the Java <code>{@link Duration}</code> class (which abstracts
 * a time duration).  The objective is to create time durations using arbitrary (amount, unit) pairs then
 * convert to <code>Duration</code> objects.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 6, 2024
 *
 */
public class TimeDuration {
    
    /** The time duration quantity */
    private final long          lngAmount;
    
    /** The time duration units as <code>{@link ChronoUnit}</code> enumeration */
    private final ChronoUnit    enmChronoUnit;
    
    /** The time duration units as <code>{@link TimeUnit}</code> enumeration */
    private final TimeUnit      enmTimeUnit;
    
    /** The equivalent <code>Duration</code> instance */
    private final Duration      duration;
    
    /**
     * <p>
     * Canonical Constructor: Constructs a new instance of <code>TimeDuration</code>.
     * </p>
     *
     * @param amount    the time duration quantity
     * @param enmUnits  the time duration units as <code>ChronoUnit</code> enumeration
     */
    public TimeDuration(long amount, ChronoUnit enmUnits) {
        this.lngAmount = amount;
        this.enmChronoUnit = enmUnits;
        this.enmTimeUnit = TimeUnit.of(enmUnits);
        this.duration = Duration.of(amount, enmUnits);
    }
    
    /**
     * <p>
     * Alternate Constructor: Constructs a new instance of <code>TimeDuration</code>.
     * </p>
     *
     * @param amount    the time duration quantity
     * @param tuUnits   the time duration units as <code>TimeUnit</code> enumeration
     */
    public TimeDuration(long amount, TimeUnit tuUnits) {
        this(amount, tuUnits.toChronoUnit());
    }

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
     * @return  the amount of time within the duration
     */
    public long getAmount() {
        return this.lngAmount;
    }
    
    /**
     * <p>
     * Return the time duration units as a <code>{@link ChronoUnit}</code> enumeration constant.
     * </p>
     * 
     * @return  the units of the time duration corresponding to the amount <code>{@link #getAmount()}</code>
     */
    public ChronoUnit getChronoUnit() {
        return this.enmChronoUnit;
    }
    
    /**
     * <p>
     * Return the time duration units as a <code>{@link TimeUnit}</code> enumeration constant.
     * </p>
     * 
     * @return  the units of the time duration corresponding to the amount <code>{@link #getAmount()}</code>
     */
    public TimeUnit getTimeUnit() {
        return this.enmTimeUnit;
    }
    
    /**
     * <p>
     * Return time duration as an abstract <code>{@link Duration}</code> instance.
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
