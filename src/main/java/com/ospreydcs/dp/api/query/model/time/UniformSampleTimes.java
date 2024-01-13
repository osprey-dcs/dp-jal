/*
 * Project: dp-api-common
 * File:	UniformSampleTimes.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	UniformSampleTimes
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

import java.time.Instant;

import com.ospreydcs.dp.api.model.TimeInterval;

/**
 * <p>
 * Contains parameters defining the timestamps for a finite interval of uniform samples.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Jan 7, 2024
 *
 */
public class UniformSampleTimes {

    //
    // Defining Attributes
    //
    
    /** The starting instant of the sampling process */
    private final Instant   tmStart;
    
    /** The sampling period in nanoseconds */
    private final long      lngPeriod;
    
    /** The number of samples */
    private final int       intCount;
    
    
    //
    // Consistent Parameters
    //
    
    /** The time domain of the sampling process */
    private final TimeInterval  ivlDomain;
    
    
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>UniformSampleTimes</code>.
     * </p>
     *
     * @param insStart  The starting instant of the sampling process 
     * @param lngPeriod The sampling period in nanoseconds 
     * @param intCount  The number of samples
     */
    public UniformSampleTimes(Instant insStart, long lngPeriod, int intCount) {
        this.tmStart = insStart;
        this.lngPeriod = lngPeriod;
        this.intCount = intCount;
        
        Instant tmStop = this.tmStart.plusNanos(lngPeriod);
        this.ivlDomain = TimeInterval.from(insStart, tmStop);
    }

}
