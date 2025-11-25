/*
 * Project: dp-jal
 * File:	JalTimeRangeConfig.java
 * Package: com.ospreydcs.dp.jal.config.common
 * Type: 	JalTimeRangeConfig
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
 * @since Nov 24, 2025
 *
 */
package com.ospreydcs.dp.jal.config.common;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;

/**
 * <p>
 * Common structure class containing parameters and operations defining a finite time range.
 * </p>
 * <p>
 * Note that the fields <code>{@link #start}</code> and <code>{@link #end}</code> are string valued
 * but should be parse-able by 
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 24, 2025
 *
 */
public class JalTimeRangeConfig extends CfgStructure<JalTimeRangeConfig> {

    /**
     * Default constructor required of base class.
     */
    public JalTimeRangeConfig() { super(JalTimeRangeConfig.class);  }

    
    //
    // Structure Fields
    //
    
    /** The start time instant of the time range - must be parse-able by <code>{@link Instant#parse(String)}</code>. */
    @ACfgOverride.Field(name="START")
    public String       start;
    
    /** The end time instant of the time range - must be parse-able by <code>{@link Instant#parse(String)}</code>. */
    @ACfgOverride.Field(name="END")
    public String       end;
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Parses the <code>{@link #start}</code> attribute as an ISO formatted time representation.
     * </p>
     * <p>
     * Obtains an instance of Instant from a text string such as 2007-12-03T10:15:30.00Z.
     * The string must represent a valid instant in UTC and is parsed using DateTimeFormatter.ISO_INSTANT.
     * </p>
     *
     * @return  a new Java <code>Instant</code> object parsed from the <code>{@link #start}</code> attribute
     * 
     * @throws DateTimeParseException   the <code>{@link #start}</code> attribute had an invalid time format
     */
    public Instant  startInstant() throws DateTimeParseException {
        Instant insStart = Instant.parse(this.start);   // throws DateTimeParseException
        
        return insStart;
    }
    
    /**
     * <p>
     * Parses the <code>{@link #end}</code> attribute as an ISO formatted time representation.
     * </p>
     * <p>
     * Obtains an instance of Instant from a text string such as 2007-12-03T10:15:30.00Z.
     * The string must represent a valid instant in UTC and is parsed using DateTimeFormatter.ISO_INSTANT.
     * </p>
     *
     * @return  a new Java <code>Instant</code> object parsed from the <code>{@link #end}</code> attribute
     * 
     * @throws DateTimeParseException   the <code>{@link #end}</code> attribute had an invalid time format
     */
    public Instant  endInstant() throws DateTimeParseException {
        Instant insEnd = Instant.parse(this.end);   // throws DateTimeParseException
        
        return insEnd;
    }
    
    /**
     * <p>
     * Computes and returns the time duration of this time range.
     * </p>
     * <p>
     * The method first invokes <code>{@link #startInstant()}</code> and <code>{@link #endInstant()}</code>
     * to obtain the time instants of this time range.  The method then defers to 
     * <code>{@link Duration#between(java.time.temporal.Temporal, java.time.temporal.Temporal)}</code>
     * to compute the returned <code>Duration</code> object.
     * </p>
     * 
     * @return  a new Java <code>Duration</code> object created from the time range duration 
     * 
     * @throws DateTimeParseException   <code>{@link #start}}</code> and/or <code>{@link #end}</code> had invalid time format
     * @throws DateTimeException        seconds between time range start/end could not be obtained
     * @throws ArithmeticException      the calculation exceeds the capacity of <code>Duration</code>
     */
    public Duration duration() throws DateTimeParseException, DateTimeException, ArithmeticException {
        Instant     insStart = this.startInstant(); // throwsDateTimeParseException
        Instant     insEnd = this.endInstant();     // throwsDateTimeParseException
        Duration    durRng = Duration.between(insStart, insEnd); // throws DateTimeException, ArithmeticException
        
        return durRng;
    }
}
