/*
 * Project: dp-api-common
 * File:    JalToolsConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config
 * Type:    JalToolsTestArchiveConfig
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
 * @since May 4, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.config.archive;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;

/**
 * <p>
 * Structure class defining Data Platform Test Archive parameters.
 * </p>
 */
@ACfgOverride.Root(root="JAL_TEST_ARCHIVE")
public class JalToolsTestArchiveConfig extends CfgStructure<JalToolsTestArchiveConfig>{

    /** Default constructor required for base class */
    public JalToolsTestArchiveConfig() { super(JalToolsTestArchiveConfig.class); }
    
    
    //
    // Structure Fields
    //
    
    /** The Data Platform Test Archive sampling range for all test PVs */
    @ACfgOverride.Struct(pathelem="RANGE")
    public SampleRange                      range;
    
    /** The Data Platform Test Archive Process Variable (PV) configuration parameters */
    @ACfgOverride.Struct(pathelem="PVS")
    public JalToolsTestArchivePvsConfig      pvs;
    
    
    
    //
    // Internal Structure Classes
    //
    
    /**
     * Structure class containing Data Platform Test Archive sampling range
     */
    public static class SampleRange extends CfgStructure<SampleRange> {
        
        /** Required default constructor for base class */
        public SampleRange() { super(SampleRange.class); };
        
        
        //
        // Structure Fields
        //
        
        /** The first timestamp of all data sources within the Data Platform test archive, i.e., the archive inception instant */
        @ACfgOverride.Field(name="START")
        public String       start;
        
        /** The last timestamp of all data sources within the Data Platform test archive */
        @ACfgOverride.Field(name="END")
        public String       end; 
        
        
        //
        // Operations
        //
        
        /**
         * <p>
         * Parses the <code>{@link #start}</code> attribute and returns the value as a Java <code>{@link Instant}</code> object.
         * </p>
         * <p>
         * The <code>{@link #start}</code> attribute must be an ISO-8601 date/time format string or an exception is thrown.
         * The general format is 'YEAR-MONTH-DAY<em>T</em>HOUR:MINUTE:SECOND.FRACTION' with resolution up to 1 nanosecond.
         * See <code>{@link Instant#parse(CharSequence)}</code> for more information on parsing ISO-8605 format strings.
         * </p>
         *   
         * @return  a new Java <code>Instant</code> parsed from the <code>{@link #start}</code> attribute
         * 
         * @throws DateTimeParseException   invalid or empty <code>{@link #start}</code> attribute
         */
        public Instant  startInstant() throws DateTimeParseException {
            Instant insStart = Instant.parse(this.start);    // throws DateTimeParseException
            
            return insStart;
        }
        
        /**
         * <p>
         * Parses the <code>{@link #end}</code> attribute and returns the value as a Java <code>{@link Instant}</code> object.
         * </p>
         * <p>
         * The <code>{@link #end}</code> attribute must be an ISO-8601 date/time format string or an exception is thrown.
         * The general format is 'YEAR-MONTH-DAY<em>T</em>HOUR:MINUTE:SECOND.FRACTION' with resolution up to 1 nanosecond.
         * See <code>{@link Instant#parse(CharSequence)}</code> for more information on parsing ISO-8605 format strings.
         * </p>
         *   
         * @return  a new Java <code>Instant</code> parsed from the <code>{@link #end}</code> attribute
         * 
         * @throws DateTimeParseException   invalid or empty <code>{@link #end}</code> attribute
         */
        public Instant  endInstant() throws DateTimeParseException {
            Instant insEnd = Instant.parse(this.end);    // throws DateTimeParseException
            
            return insEnd;
        }
        
        /**
         * <p>
         * Parses the values of <code>{@link #start}</code> and <code>{@link #end}</code> returning the difference between them.
         * </p>
         * <p>
         * The time range of the Data Platform Test Archive is computed from the <code>{@link #start}</code> and 
         * <code>{@link #end}</code> attributes and returned as a Java <code>{@link Duration}</code> object.
         * The returned value represents the entire time range over which the Test Archive has sample values.
         * </p> 
         * <p>
         * Both the <code>{@link #start}</code> and <code>{@link #end}</code> attributes must be a valid ISO-8601 date/time
         * format string or an exception is thrown.
         * See <code>{@link Instant#parse(CharSequence)}</code> for more information on parsing ISO-8605 format strings.
         * </p>
         * @return the time difference between <code>{@link #start}</code> and <code>{@link #end}</code> as a <code>Duration</code>
         * 
         * @throws DateTimeParseException   invalid or empty <code>{@link #start}</code> and/or <code>{@link #end}</code> attribute
         * @throws DateTimeException        the difference between <code>{@link #start}</code> and <code>{@link #end}</code> cannot be obtained
         * @throws ArithmeticException      the difference between <code>start</code> and <code>end</code> exceeds <code>Duration</code> capacity
         */
        public Duration rangeDuration() throws DateTimeParseException, DateTimeException, ArithmeticException {
            Duration    durRange = Duration.between(this.startInstant(), this.endInstant());
            
            return durRange;
        }
    }
}