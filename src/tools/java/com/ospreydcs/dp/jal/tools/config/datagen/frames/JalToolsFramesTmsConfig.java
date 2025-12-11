/*
 * Project: dp-jal
 * File:	JalToolsFramesTmsConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config.datagen
 * Type: 	JalToolsFramesTmsConfig
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
 * @since Dec 1, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.config.datagen.frames;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import com.ospreydcs.dp.jal.common.DpTimestampCase;
import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing default parameters for simulated ingestion frame timestamp value generation.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 1, 2025
 *
 */
public class JalToolsFramesTmsConfig extends CfgStructure<JalToolsFramesTmsConfig> {

    /** Default constructor required of base class */
    public JalToolsFramesTmsConfig() { super(JalToolsFramesTmsConfig.class); }

    
    //
    // Attributes
    //
    
    /** The default timestamp case for the ingestion frame */
    @ACfgOverride.Field(name="TYPE")
    public DpTimestampCase          type;
    
    /** THe default starting time instant for ingestion frame timestamps (ISO-8601 date/time format string 'Y-M-DTH:M:S.s') */
    @ACfgOverride.Field(name="START")
    public String                   start;
    
    /** The default sampling period for ingestion frame timestamps (ISO-8605 duration format string 'PnDTnHnMn.nS') */
    @ACfgOverride.Field(name="PERIOD")
    public String                   period;
    
    /** The default sampling delay from the start instant (ISO-8605 duration format string 'PnDTnHnMn.nS') */
    @ACfgOverride.Field(name="DELAY")
    public String                   delay;
    
    /** The number of timestamps per ingestion frame - this is the row count for each ingestion frame */
    @ACfgOverride.Field(name="COUNT")
    public Integer                  count;
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Returns the <code>{@link #start}</code> attribute as a Java <code>{@link Instant}</code> object.
     * </p>
     * <p>
     * The <code>{@link #start}</code> attribute of this structure class is parsed as an ISO-8601 date/time
     * format string with the <code>{@link Instant#parse(CharSequence)}</code> method.  The result is returned
     * as a new <code>Instant</code> object.  The <code>{@link #start}</code> must be a valid UTC date/time
     * expression formatted such as 'Y-M-D<em>T</em>H:M:S.s'.
     * </p>
     * 
     * @return  a new Java <code>Instant</code> object parsed from the <code>{@link #toString()}</code> attribute
     * 
     * @throws DateTimeParseException   invalid ISO-8605 date/time format or <code>null</code> attribute value
     * 
     * @see {@link Instant#parse(CharSequence)}
     */
    public Instant  startInstant()  throws DateTimeParseException {
        Instant insStart = Instant.parse(this.start);    // throws DateTimeParseException
        
        return insStart;
    }
    
    /**
     * <p>
     * Returns the <code>{@link #period}</code> attribute as a Java <code>{@link Duration}</code> object.
     * </p>
     * <p>
     * The <code>{@link #period}</code> attribute of this structure class is parsed as an ISO-8601 time duration
     * format string with the <code>{@link Duration#parse(CharSequence)}</code> method.  The result is returned
     * as a new <code>Duration</code> object.  The <code>{@link #period}</code> must be a valid UTC time duration
     * expression formatted such as 'PnD<em>T</em>nH:nM:n.nS'.  The seconds resolution is to the nanosecond
     * (i.e., 'PT0.000000001S').
     * </p>
     * 
     * @return  a new Java <code>Duration</code> object parsed from the <code>{@link #period}</code> attribute
     * 
     * @throws DateTimeParseException   invalid ISO-8605 duration format or <code>null</code> attribute value
     * 
     * @see {@link Duration#parse(CharSequence)}
     */
    public Duration periodDuration() throws DateTimeParseException {
        Duration    durPeriod = Duration.parse(this.period);     // throws DateTimeParseException
        
        return durPeriod;
    }
    
    /**
     * <p>
     * Returns the <code>{@link #delay}</code> attribute as a Java <code>{@link Duration}</code> object.
     * </p>
     * <p>
     * The <code>{@link #delay}</code> attribute of this structure class is parsed as an ISO-8601 time duration
     * format string with the <code>{@link Duration#parse(CharSequence)}</code> method.  The result is returned
     * as a new <code>Duration</code> object.  The <code>{@link #delay}</code> must be a valid UTC time duration
     * expression formatted such as 'PnD<em>T</em>nH:nM:n.nS'.  The seconds resolution is to the nanosecond
     * (i.e., 'PT0.000000001S').
     * </p>
     * 
     * @return  a new Java <code>Duration</code> object parsed from the <code>{@link #delay}</code> attribute
     * 
     * @throws DateTimeParseException   invalid ISO-8605 duration format or <code>null</code> attribute value
     * 
     * @see {@link Duration#parse(CharSequence)}
     */
    public Duration delayDuration() throws DateTimeParseException {
        Duration    durDelay = Duration.parse(this.delay);      // throws DateTimeParseException
        
        return durDelay;
    }
}
