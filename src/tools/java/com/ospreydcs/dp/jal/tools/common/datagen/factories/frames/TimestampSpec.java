/*
 * Project: dp-jal
 * File:	TimestampFactorySpec.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.frames
 * Type: 	TimestampFactorySpec
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
 * @since Dec 4, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.frames;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import com.ospreydcs.dp.jal.common.DpTimestampCase;
import com.ospreydcs.dp.jal.common.UniformSamplingClock;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.frames.JalToolsFramesTmsConfig;

/**
 * <p>
 * Timestamp definition and management for ingestion frame timestamps.
 * </p>
 * <p>
 * Record instances are intended to define the timestamps for an ingestion data frame, and to manage the
 * creation of subsequent timestamps for ingestion frames created by ingestion frame factories.
 * Timestamp are defined at creation/construction and are immutable thereafter.  
 * The record provides methods for generating sampling clocks and timestamp lists from the timestamp
 * definition.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 4, 2025
 *
 * @param cntSamples    number of samples in each data column of the ingestion frame
 * @param enmType       the timestamp type, either a uniform sampling clock or an explicit timestamp list
 * @param durPeriod     the sampling period for the timestamp collection
 * @param insStart      the starting time instant for the timestamps (i.e., the 1st timestamp value)
 * @param durDelay      sampling delay from starting time instant (i.e., the 1st timestamp will be offset by this value)
 */
public record TimestampSpec(
        int                 cntSamples,
        DpTimestampCase     enmType,
        Duration            durPeriod,
        Instant             insStart,
        Duration            durDelay
        ) 
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactorySpec</code> instance with the default configuration.
     * </p>
     * <p>
     * This creator uses all default values for the <code>TimestampFactorySpec</code> field values. 
     * The default values are taken from the JAL Tools configuration and available in the record constants:
     * <ul>
     * <li><code>{@link #cntSamples()}</code> = <code>{@link #CNT_SAMPLES_DEF}</code>.
     * <li><code>{@link #enmType()}</code> = <code>{@link #ENM_TYPE}</code>.
     * <li><code>{@link #durPeriod()}</code> = <code>{@link #DUR_PERIOD_DEF}</code>.
     * <li><code>{@link #insStart()}</code> = <code>{@link #INS_START_DEF}</code>.
     * <li><code>{@link #durDelay()}</code> = <code>{@link #DUR_DELAY_DEF}</code>.
     * </ul>
     * </p>
     * 
     * @return  a new <code>TimestampFactorySpec</code> record populated with all default arguments
     */
    public static TimestampSpec   from() {
        return TimestampSpec.from(CNT_SAMPLES_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactorySpec</code> instance configured according to the available arguments.
     * </p>
     * <p>
     * This creator uses default values for the missing <code>TimestampFactorySpec</code> field value (i.e., those not
     * included in the argument collection.  The default values are taken from the JAL Tools configuration and
     * available in the record constants:
     * <ul>
     * <li><code>{@link #enmType()}</code> = <code>{@link #ENM_TYPE}</code>.
     * <li><code>{@link #durPeriod()}</code> = <code>{@link #DUR_PERIOD_DEF}</code>.
     * <li><code>{@link #insStart()}</code> = <code>{@link #INS_START_DEF}</code>.
     * <li><code>{@link #durDelay()}</code> = <code>{@link #DUR_DELAY_DEF}</code>.
     * </ul>
     * All other record fields are given by the supplied arguments.
     * </p>
     * 
     * @param cntSamples    number of samples in each data column of the ingestion frame
     * 
     * @return  a new <code>TimestampFactorySpec</code> record populated with the given arguments
     */
    public static TimestampSpec   from(int cntSamples) {
        return TimestampSpec.from(cntSamples, ENM_TYPE);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactorySpec</code> instance configured according to the available arguments.
     * </p>
     * <p>
     * This creator uses default values for the missing <code>TimestampFactorySpec</code> field value (i.e., those not
     * included in the argument collection.  The default values are taken from the JAL Tools configuration and
     * available in the record constants:
     * <ul>
     * <li><code>{@link #durPeriod()}</code> = <code>{@link #DUR_PERIOD_DEF}</code>.
     * <li><code>{@link #insStart()}</code> = <code>{@link #INS_START_DEF}</code>.
     * <li><code>{@link #durDelay()}</code> = <code>{@link #DUR_DELAY_DEF}</code>.
     * </ul>
     * All other record fields are given by the supplied arguments.
     * </p>
     * 
     * @param cntSamples    number of samples in each data column of the ingestion frame
     * @param enmType       the timestamp type, either a uniform sampling clock or an explicit timestamp list
     * 
     * @return  a new <code>TimestampFactorySpec</code> record populated with the given arguments
     */
    public static TimestampSpec   from(int cntSamples, DpTimestampCase enmType ) {
        return TimestampSpec.from(cntSamples, enmType, DUR_PERIOD_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactorySpec</code> instance configured according to the available arguments.
     * </p>
     * <p>
     * This creator uses default values for the missing <code>TimestampFactorySpec</code> field value (i.e., those not
     * included in the argument collection.  The default values are taken from the JAL Tools configuration and
     * available in the record constants:
     * <ul>
     * <li><code>{@link #insStart()}</code> = <code>{@link #INS_START_DEF}</code>.
     * <li><code>{@link #durDelay()</code> = <code>{@link #DUR_DELAY_DEF}</code>.
     * </ul>
     * All other record fields are given by the supplied arguments.
     * </p>
     * 
     * @param cntSamples    number of samples in each data column of the ingestion frame
     * @param enmType       the timestamp type, either a uniform sampling clock or an explicit timestamp list
     * @param durPeriod     the sampling period for the timestamp collection
     * 
     * @return  a new <code>TimestampFactorySpec</code> record populated with the given arguments
     */
    public static TimestampSpec   from(int cntSamples, DpTimestampCase enmType, Duration durPeriod) {
        return TimestampSpec.from(cntSamples, enmType, durPeriod, INS_START_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactorySpec</code> instance configured according to the available arguments.
     * </p>
     * <p>
     * This creator uses default values for the missing <code>TimestampFactorySpec</code> field value (i.e., those not
     * included in the argument collection.  The default values are taken from the JAL Tools configuration and
     * available in the record constants:
     * <ul>
     * <li><code>{@link #durDelay()}</code> = <code>{@link #DUR_DELAY_DEF}</code>.
     * </ul>
     * All other record fields are given by the supplied arguments.
     * </p>
     * 
     * @param cntSamples    number of samples in each data column of the ingestion frame
     * @param enmType       the timestamp type, either a uniform sampling clock or an explicit timestamp list
     * @param durPeriod     the sampling period for the timestamp collection
     * @param insStart      the starting time instant for the timestamps (i.e., the 1st timestamp value)
     * 
     * @return  a new <code>TimestampFactorySpec</code> record populated with the given arguments
     */
    public static TimestampSpec   from(int cntSamples, DpTimestampCase enmType, Duration durPeriod, Instant insStart) {
        return TimestampSpec.from(cntSamples, enmType, durPeriod, insStart, DUR_DELAY_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactorySpec</code> instance configured according to the given arguments.
     * </p>
     * <p>
     * This creation is equivalent to the canonical constructor 
     * <code>{@link #TimestampFactorySpec(int, DpTimestampCase, Duration, Instant, Duration)}</code>.
     * There are no default field values, all field values are provided in the arguments collection.
     * </p>
     * 
     * @param cntSamples    number of samples in each data column of the ingestion frame
     * @param enmType       the timestamp type, either a uniform sampling clock or an explicit timestamp list
     * @param durPeriod     the sampling period for the timestamp collection
     * @param insStart      the starting time instant for the timestamps (i.e., the 1st timestamp value)
     * @param durDelay      sampling delay from starting time instant (i.e., the 1st timestamp will be offset by this value)
     * 
     * @return  a new <code>TimestampFactorySpec</code> record populated with the given arguments
     */
    public static TimestampSpec   from(int cntSamples, DpTimestampCase enmType, Duration durPeriod, Instant insStart, Duration durDelay) {
        return new TimestampSpec(cntSamples, enmType, durPeriod, insStart, durDelay);
    }
    
    /**
     * <p>
     * Parses the collection of arguments as values of the <code>TimestampFactorySpec</code> record fields.
     * </p>
     * <p>
     * The element strings of the argument array are parsed as formatted, string values of the record
     * field.  Typically, the argument is part of a application command-line argument collection.
     * There, the argument array elements follow a command-line (delimiting) variable, say '<code>--tms</code>'.
     * Assuming as such, the format of the argument array elements is as follows:
     * <code>
     * <pre>
     * > java application --tms [samples [type [period [start [delay]]]]] ... 
     * </pre>
     * </code>
     * where '<code>application</code> is the example application name and the brackets indicate optional
     * inclusion.
     * The strings following the variable <code>--tms</code> are the field values parsed here, in order.
     * <ol>
     * <li>'samples' &rarr; <code>{@link #cntSamples()}</code> [default <code>{@link #CNT_SAMPLES_DEF}</code>],
     * <li>'type' &rarr; <code>{@link #enmType()}</code> [default <code>{@link #ENM_TYPE}</code>],
     * <li>'period' &rarr; <code>{@link #durPeriod()}</code> [default <code>{@link #DUR_PERIOD_DEF}</code>],
     * <li>'start' &rarr; <code>{@link #insStart()}</code> [default <code>{@link #INS_START_DEF}</code>],
     * <li>'delay' &rarr; <code>{@link #durDelay()}</code> [default <code>{@link #DUR_DELAY_DEF}</code>],
     * </ol>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * If values do not appear in the argument array the fields values are populated with JAL Tools 
     * default configuration values, which are captured as the record constant indicated above.
     * </li>
     * <li>
     * The ordering of the above string values is strict.  One cannot skip field values as default then supply
     * a later value.  For example, in order to specify the delay parameter 'delay' 
     * (i.e., <code>{@link #durDelay()}</code> all other parameters must be supplied.
     * </li>
     * </ul>  
     * </p>
     * 
     * @param args  ordered collection of field values of format [samples [type [period [start [delay]]]]]
     * 
     * @return  a new <code>TimestampFactorySpec</code> record populated with the parsed argument strings
     * 
     * @throws NumberFormatException    invalid numeric format for 'samples'
     * @throws TypeNotPresentException  invalid <code>{@link DpTimestampCase}</code> constant for 'type'  
     * @throws DateTimeParseException   invalid ISO-8605 date/time/duration format for 'period', 'start', or 'delay' 
     */
    public static TimestampSpec   parse(String...args) throws NumberFormatException, TypeNotPresentException, DateTimeParseException {

        // If the argument collection is empty return the default configuration
        if (args.length < 1)
            return TimestampSpec.from();

        // Get the number of samples
        String  strCntSamples   = args[0];
        int     cntSamples = Integer.valueOf(strCntSamples);    // throws NumberFormatException
        if (args.length < 2)
            return TimestampSpec.from(cntSamples);
        
        // Get the timestamp type within the arguments list
        String  strTmsType = args[1];
        DpTimestampCase enmType = DpTimestampCase.getConstant(strTmsType);  // throws TypeNotPresentException
        if (args.length < 3)
            return TimestampSpec.from(cntSamples, enmType);
        
        // Get the period within the argument list
        String      strPeriod = args[2];
        Duration    durPeriod = Duration.parse(strPeriod);  // DateTimeParseException
        if (args.length < 4)
            return TimestampSpec.from(cntSamples, enmType, durPeriod);
        
        // Get the start instant from the argument list
        String      strStart = args[3];
        Instant     insStart = Instant.parse(strStart);     // throws DateTimeParseException
        if (args.length < 5)
            return TimestampSpec.from(cntSamples, enmType, durPeriod, insStart);
        
        // Get the start delay from the argument collection
        String      strDelay = args[4];
        Duration    durDelay = Duration.parse(strDelay);    // throws DateTimeParseException
        return TimestampSpec.from(cntSamples, enmType, durPeriod, insStart, durDelay);
    }    

    
    //
    // JAL Library Resources
    //
    
    /** Default configuration parameters for the JAL Tools */
    private static final JalToolsFramesTmsConfig     CFG_DEF = JalToolsConfig.getInstance().datagen.frames.timestamps;
    
    
    //
    // Class Constants
    //
    
    /** Default timestamp type for ingestion frames */
    public static final DpTimestampCase     ENM_TYPE = CFG_DEF.type;
    
    /** Default starting time instant for ingestion frame timestamps */
    public static final Instant             INS_START_DEF = CFG_DEF.startInstant();
    
    /** Default sampling period for ingestion frame timestamps */
    public static final Duration            DUR_PERIOD_DEF = CFG_DEF.periodDuration();
    
    /** Default sampling delay from start instant */
    public static final Duration            DUR_DELAY_DEF = CFG_DEF.delayDuration();
    
    /** Default sample count per ingestion frame */
    public static final int                 CNT_SAMPLES_DEF = CFG_DEF.count;
    

    //
    // Operations
    //
    
    /**
     * <p>
     * Computes and returns the first start instant for the timestamps according to the field values.
     * </p>
     * <p>
     * Although computed at each invocation, the returned value is always the same; it is given by
     * the formula
     * <pre>
     *      {@link #insStart} + {@link #durDelay}
     * </pre>
     * or, programmatically, 
     * <code>{@link #insStart}.plus({@link #durDelay})</code>.
     * </p>
     *  
     * @return  a new <code>Instant</code> containing the first timestamp
     * 
     * @throws DateTimeException    overflow occurred in <code>Instant</code> addition 
     * @throws ArithmeticException  <code>Instant</code> addition failed or overflow in <code>Duration</code> multiplication  
     */
    public Instant  firstStartInstant() throws DateTimeException, ArithmeticException {
        Instant insFirst = this.insStart.plus(this.durDelay);   // throws DateTimeException, ArithmeticException
        
        return insFirst;
    }
    
    /**
     * <p>
     * Computes and returns the next start time for from the given instant.
     * </p>
     * <p>
     * This method is used to advance the timestamp start time for ingestion frames.
     * The argument is assumed to be the start time of a current ingestion frame and the
     * returned value is then the start time instant for the next ingestion frame timestamps.
     * </p>
     *  
     * @param insStart  the current start time for current ingestion frame timestamps
     * 
     * @return  the start time for the next ingestion frame timestamps
     * 
     * @throws DateTimeException    overflow occurred in <code>Instant</code> addition 
     * @throws ArithmeticException  <code>Instant</code> addition failed or overflow in <code>Duration</code> multiplication  
     */
    public Instant nextStartInstant(Instant insStart) throws DateTimeException, ArithmeticException {
        
        Duration    durRange = this.durPeriod.multipliedBy(this.cntSamples);    // throws ArithmeticException
        Instant     insNext = insStart.plus(durRange);      // throws DateTimeException, ArithmeticException
        
        return insNext;
    }
    
    /**
     * <p>
     * Creates a new <code>UniformSamplingClock</code> instance for the given start time.
     * </p>
     * <p>
     * The remain parameters of the sampling clock (i.e., period and sample count) are taken from the
     * record fields given at construction.
     * </p>
     * 
     * @param insStart  the start time for the new sampling clock
     * 
     * @return  a new <code>UniformSamplingClock</code> instance for the given start time
     * 
     * @throws IllegalArgumentException the sample count was negative and/or the period was non-positive
     */
    public UniformSamplingClock    nextUniformClock(Instant insStart) throws IllegalArgumentException {
        
        UniformSamplingClock clk = UniformSamplingClock.from(insStart, this.cntSamples, this.durPeriod);
        
        return clk;
    }
    
    /**
     * <p>
     * Creates a new ordered list (vector) of timestamp instants with the given start time.
     * </p>
     * <p>
     * The number of timestamps and the interval between timestamps (i.e., the period) is taken from the
     * record fields given at construction.
     * </p>
     * 
     * @param insStart  the first timestamp instant with the returned vector
     * 
     * @return  an ordered vector of <code>Instant</code> objects representing timestamps from a uniform clock
     * 
     * @throws DateTimeException    internal <code>Instant</code> addition failed
     * @throws ArithmeticException  numeric overflow occurred in <code>Instant</code> addition 
     */
    public ArrayList<Instant>   nextTimestampVector(Instant insStart) throws DateTimeException, ArithmeticException {
        
        ArrayList<Instant>   vecTms = new ArrayList<>(this.cntSamples);
        
        Instant insCurr = insStart;
        for (int iTms=0; iTms<this.cntSamples; iTms++) {
            vecTms.add(insCurr);
            
            insCurr = insCurr.plus(this.durPeriod);     // throws DateTimeException, ArithmeticException
        }
        
        return vecTms;
    }

    
    // 
    // Record Overrides
    //
    
    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof TimestampSpec spec)  {
            boolean bolResult = (this.cntSamples == spec.cntSamples)
                    && (this.enmType == spec.enmType)
                    && (this.durPeriod.equals(spec.durPeriod))
                    && (this.insStart.equals(spec.insStart))
                    && (this.durDelay.equals(spec.durDelay));
            return bolResult;
        }
        
        return false;
    }

    /**
     * @see java.lang.Record#toString()
     */
    @Override
    public String toString() {
        String  str = "";
        str += "Sample count           : " + this.cntSamples + "\n";
        str += "Timestamp case         : " + this.enmType + "\n";
        str += "Sampling period        : " + this.durPeriod + "\n";
        str += "Sampling start instant : " + this.insStart + "\n";
        str += "Sampling start delay   : " + this.durDelay + "\n";
        
        return str;
    }
    
}
