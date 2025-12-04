/*
 * Project: dp-jal
 * File:	JalToolsTmsValuesConfig.java
 * Package: com.ospreydcs.dp.jal.tools.config.datagen
 * Type: 	JalToolsTmsValuesConfig
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
package com.ospreydcs.dp.jal.tools.config.datagen.values;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing default parameters for timestamp factories producing simulated timestamp data.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 24, 2025
 *
 */
public class JalToolsTmsValuesConfig extends CfgStructure<JalToolsTmsValuesConfig> {

    /** Default constructor required of base class */
    public JalToolsTmsValuesConfig() { super(JalToolsTmsValuesConfig.class); }
    
    
    //
    // Structure Fields
    //
    
    /** The default parameters for random timestamp generation */
    @ACfgOverride.Struct(pathelem="RANDOM")
    public JalToolsRandomConfig     random;
    
    /** The default parameters for incremental timestamp generation */
    @ACfgOverride.Struct(pathelem="INCREMENT")
    public Increment                increment;
    
    
    //
    // Internal Types
    //
    
    /**
     * <p>
     * Structure class containing default parameters and operations for incremental timestamp generation.
     * </p>
     */
    public static final class Increment extends CfgStructure<Increment> {
        
        /** Default constructor required of base class */
        public Increment() { super(Increment.class); };
        
        
        //
        // Structure Fields
        //
        
        /** The first timestamp value for incremental timestamp generation - in ISO-8601 format */  
        @ACfgOverride.Field(name="START")
        public String   start;
        
        /** The distance between timestamps for incremental timestamp generation - in ISO-8601 format 'PnDTnHnMnS' */
        @ACfgOverride.Field(name="PERIOD")
        public String   period;
        
        
        //
        // Structure Operations
        //
        
        /**
         * <p>
         * Converts the <code>{@link #start}</code> attribute to a Java <code>Instant</code> object and returns it.
         * </p>
         * <p>
         * The <code>{@link #start}</code> attribute is assumed to be a string in ISO-8601 time format.
         * This method parses the attribute and returns a new Java <code>Instant</code> object with
         * the parsed value.  See documentation on <code>{@link Instant#parse(CharSequence)}</code> for
         * more information.
         * </p>
         * 
         * @return  a new Java <code>Instant</code> object parsed from the <code>{@link #start}</code> field
         * 
         * @throws DateTimeParseException   the <code>{@link #start}</code> had an invalid ISO-8601 format
         */
        public Instant  startInstant() throws DateTimeParseException {
            Instant insStart = Instant.parse(this.start);   // throws DateTimeParseException
            
            return insStart;
        }
        
        /**
         * <p>
         * Converts the <code>{@link #period}</code> attribute to a Java <code>Duration</code> object and returns it.
         * </p>
         * <p>
         * The <code>{@link #period}</code> attribute is assumed to be be in ISO-8601 format, specifically,
         * as a string 'PnDTnHnMnS' where 
         * <ul>
         * <li>'n'D : 'n' is integer number of days</li>
         * <li>'n'H : 'n' is integer number hours</li>
         * <li>'n'M : 'n' is integer number of minutes</li>
         * <li>'n'S : 'n' is a decimal number of seconds</li>
         * </ul>
         * The second field has resolution down to the nanosecond, that is, 'PT0.000000001S'.
         * See <code>{@link Duration#parse(CharSequence)}</code> for more details.  
         * </p>
         * 
         * @return a new Java <code>Duration</code> object parsed from the <code>{@link #period}</code> field 
         *  
         * @throws IllegalArgumentException  the attribute <code>{@link #period}</code> had invalid ISO-8601 format
         */
        public Duration periodDuration() throws IllegalArgumentException {
            Duration    durPeriod = Duration.parse(this.period);    // throws IllegalArgumentException
            
            return durPeriod;
        }
    }

}
