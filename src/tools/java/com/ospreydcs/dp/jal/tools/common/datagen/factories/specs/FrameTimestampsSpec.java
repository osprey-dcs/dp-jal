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
package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import com.ospreydcs.dp.jal.common.DpTimestampCase;
import com.ospreydcs.dp.jal.ingest.IngestionFrame;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameTimestampsFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameTimestampsFactory;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.frames.JalToolsFramesTmsConfig;

/**
 * <p>
 * Timestamp specification (and management) for ingestion frame timestamps.
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
 * @param durPeriod     the sampling period for the timestamp collection
 * @param insStart      the starting time instant for the timestamps (i.e., the 1st timestamp value)
 * @param enmType       the timestamp type, either a uniform sampling clock or an explicit timestamp list
 * @param durDelay      sampling delay from starting time instant (i.e., the 1st timestamp will be offset by this value)
 */
public record FrameTimestampsSpec(
        int                 cntSamples,
        Duration            durPeriod,
        Instant             insStart,
        DpTimestampCase     enmType,
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
     * <li><code>{@link #cntSamples()}</code> = <code>{@link #CNT_SAMPLES_DEF}</code>.</li>
     * <li><code>{@link #insStart()}</code> = <code>{@link #INS_START_DEF}</code>.</li>
     * <li><code>{@link #enmType()}</code> = <code>{@link #ENM_TMS_TYPE_DEF}</code>.</li>
     * <li><code>{@link #durPeriod()}</code> = <code>{@link #DUR_PERIOD_DEF}</code>.</li>
     * <li><code>{@link #durDelay()}</code> = <code>{@link #DUR_DELAY_DEF}</code>.</li>
     * </ul>
     * </p>
     * 
     * @return  a new <code>TimestampFactorySpec</code> record populated with all default arguments
     */
    public static FrameTimestampsSpec   from() {
        return FrameTimestampsSpec.from(CNT_SAMPLES_DEF);
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
     * <li><code>{@link #durPeriod()}</code> = <code>{@link #DUR_PERIOD_DEF}</code>.</li>
     * <li><code>{@link #insStart()}</code> = <code>{@link #INS_START_DEF}</code>.</li>
     * <li><code>{@link #enmType()}</code> = <code>{@link #ENM_TMS_TYPE_DEF}</code>.</li>
     * <li><code>{@link #durDelay()}</code> = <code>{@link #DUR_DELAY_DEF}</code>.</li>
     * </ul>
     * All other record fields are given by the supplied arguments.
     * </p>
     * 
     * @param cntSamples    number of samples in each data column of the ingestion frame
     * 
     * @return  a new <code>TimestampFactorySpec</code> record populated with the given arguments
     */
    public static FrameTimestampsSpec   from(int cntSamples) {
        return FrameTimestampsSpec.from(cntSamples, DUR_PERIOD_DEF);
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
     * <li><code>{@link #insStart()}</code> = <code>{@link #INS_START_DEF}</code>.</li>
     * <li><code>{@link #enmType()}</code> = <code>{@link #ENM_TMS_TYPE_DEF}</code>.</li>
     * <li><code>{@link #durDelay()}</code> = <code>{@link #DUR_DELAY_DEF}</code>.</li>
     * </ul>
     * All other record fields are given by the supplied arguments.
     * </p>
     * 
     * @param cntSamples    number of samples in each data column of the ingestion frame
     * @param durPeriod     the sampling period for the timestamp collection
     * 
     * @return  a new <code>TimestampFactorySpec</code> record populated with the given arguments
     */
    public static FrameTimestampsSpec   from(int cntSamples, Duration durPeriod) {
        return FrameTimestampsSpec.from(cntSamples, durPeriod, INS_START_DEF);
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
     * <li><code>{@link #enmType()}</code> = <code>{@link #ENM_TMS_TYPE_DEF}</code>.</li>
     * <li><code>{@link #durDelay()}</code> = <code>{@link #DUR_DELAY_DEF}</code>.</li>
     * </ul>
     * All other record fields are given by the supplied arguments.
     * </p>
     * 
     * @param cntSamples    number of samples in each data column of the ingestion frame
     * @param durPeriod     the sampling period for the timestamp collection
     * @param insStart      the starting time instant for the timestamps (i.e., the 1st timestamp value)
     * 
     * @return  a new <code>TimestampFactorySpec</code> record populated with the given arguments
     */
    public static FrameTimestampsSpec   from(int cntSamples, Duration durPeriod, Instant insStart) {
        return FrameTimestampsSpec.from(cntSamples, durPeriod, insStart, ENM_TMS_TYPE_DEF);
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
     * <li><code>{@link #durDelay()}</code> = <code>{@link #DUR_DELAY_DEF}</code>.</li>
     * </ul>
     * All other record fields are given by the supplied arguments.
     * </p>
     * 
     * @param cntSamples    number of samples in each data column of the ingestion frame
     * @param durPeriod     the sampling period for the timestamp collection
     * @param insStart      the starting time instant for the timestamps (i.e., the 1st timestamp value)
     * @param enmType       the timestamp type, either a uniform sampling clock or an explicit timestamp list
     * 
     * @return  a new <code>TimestampFactorySpec</code> record populated with the given arguments
     */
    public static FrameTimestampsSpec   from(int cntSamples, Duration durPeriod, Instant insStart, DpTimestampCase enmType) {
        return FrameTimestampsSpec.from(cntSamples, durPeriod, insStart, enmType, DUR_DELAY_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactorySpec</code> instance configured according to the given arguments.
     * </p>
     * <p>
     * This creation is equivalent to the canonical constructor 
     * <code>{@link #TimestampFactorySpec(int, Duration, Instant, DpTimestampCase, Duration)}</code>.
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
    public static FrameTimestampsSpec   from(int cntSamples, Duration durPeriod, Instant insStart, DpTimestampCase enmType, Duration durDelay) {
        return new FrameTimestampsSpec(cntSamples, durPeriod, insStart, enmType, durDelay);
    }
    
    /**
     * <p>
     * Parses the collection of arguments as values of the <code>TimestampFactorySpec</code> record fields.
     * </p>
     * <p>
     * The element strings of the argument array are parsed as formatted, string values of the record
     * field.  Typically, the argument is part of a application command-line argument collection.
     * There, the argument array elements typically follow a command-line (delimited) variable, for example 
     * '<code>--tms</code>'.
     * </p>
     * <p>
     * <h2>Format</h2>
     * Assume that the <code>FrameTimestampsSpec</code> parameters are identified with the <code>--tms</code> delimited
     * variable name.  Let the java command-line argument for an example application <code>MyApp</code> then appear as  
     * <code>
     * <pre>
     * > java MyApp ... --tms [samples [period [start [type [delay]]]]] ... 
     * </pre>
     * </code>
     * where again <code>MyApp</code> is the example application name, <code>...</code> indicates additional application
     * command-line parameters not relevant here, and the brackets indicate optional inclusion of the 
     * <code>FrameTimestampSpec</code> parameters.  The arguments passed to this method is then the following collection:
     * <code>
     * <pre>
     *  [samples [period [start [type [delay]]]]]
     * </pre>
     * </code>
     * The field values of <code>FrameTimestampSpec</code> are taken from the above collection of strings. 
     * The strings are parsed here, in order.  The list is given below along with the default value when
     * the parameter is missing.
     * <ol>
     * <li>'samples' &rarr; <code>{@link #cntSamples()}</code> [default <code>{@link #CNT_SAMPLES_DEF}</code>],
     * <li>'period' &rarr; <code>{@link #durPeriod()}</code> [default <code>{@link #DUR_PERIOD_DEF}</code>],
     * <li>'start' &rarr; <code>{@link #insStart()}</code> [default <code>{@link #INS_START_DEF}</code>],
     * <li>'type' &rarr; <code>{@link #enmType()}</code> [default <code>{@link #ENM_TMS_TYPE_DEF}</code>],
     * <li>'delay' &rarr; <code>{@link #durDelay()}</code> [default <code>{@link #DUR_DELAY_DEF}</code>],
     * </ol>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * If values do not appear in the argument array the fields values are populated with JAL Tools 
     * default configuration values, which are captured as the record constants indicated above.
     * </li>
     * <li>
     * The ordering of the above string values is strict.  One cannot skip field values as default then supply
     * a later value.  For example, in order to specify the delay parameter 'delay' 
     * (i.e., <code>{@link #durDelay()}</code>) all other parameters must be supplied.
     * </li>
     * <li>
     * If the argument is <code>null</code> or empty, the timstamps for the default ingestion frame are
     * returned (i.e., via method <code>{@link #from()}</code>).
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
    public static FrameTimestampsSpec   parse(String...args) throws NumberFormatException, TypeNotPresentException, DateTimeParseException {

        // If the argument collection is empty return the default configuration
        if (args==null || args.length < 1)
            return FrameTimestampsSpec.from();
        
        int     indArg = 0;

        // Get the number of samples
        String  strCntSamples   = args[indArg++];
        int     cntSamples = Integer.valueOf(strCntSamples);    // throws NumberFormatException
        if (args.length < (indArg+1))
            return FrameTimestampsSpec.from(cntSamples);
        
        // Get the period within the argument list
        String      strPeriod = args[indArg++];
        Duration    durPeriod = Duration.parse(strPeriod);  // DateTimeParseException
        if (args.length < (indArg+1))
            return FrameTimestampsSpec.from(cntSamples, durPeriod);
        
        // Get the start instant from the argument list
        String      strStart = args[indArg++];
        Instant     insStart = Instant.parse(strStart);     // throws DateTimeParseException
        if (args.length < (indArg+1))
            return FrameTimestampsSpec.from(cntSamples, durPeriod, insStart);
        
        // Get the timestamp type within the arguments list
        String  strTmsType = args[indArg++];
        DpTimestampCase enmType = DpTimestampCase.valueFrom(strTmsType);  // throws TypeNotPresentException
        if (args.length < (indArg+1))
            return FrameTimestampsSpec.from(cntSamples, durPeriod, insStart, enmType);
        
        // Get the start delay from the argument collection
        String      strDelay = args[indArg++];
        Duration    durDelay = Duration.parse(strDelay);    // throws DateTimeParseException
        return FrameTimestampsSpec.from(cntSamples, durPeriod, insStart, enmType, durDelay);
    }    
    
    /**
     * <p>
     * Retrieves the default ingestion frame timestamp specifications for the default ingestion frame factory configuration.
     * </p>
     * <p>
     * The JAL Tools default configuration contains a default ingestion frame configuration.  This configuration is
     * used by ingestion frame factories to create <code>{@link IngestionFrame}</code> instances when no explicit
     * configuration is given.
     * </p>
     * The returned <code>FrameTimestampsSpec</code> record specifies the timestamps in the 
     * default ingestion frame.  The data column specifications for the default ingestion frame can be
     * obtained from the <code>{@link DataColumns2}</code> record.  
     * </p>
     * <p>
     * The method retrieves the default timestamps specifications contained in the <code>{@link JalToolsFramesTmsConfig}</code>
     * structure class list within the <code>{@link JalToolsConfig}</code> default configuration.  The parameters
     * for the timestamp specification are extracted and a new <code>FrameTimestampsSpec</code> record is created.
     * </p>
     * @apiNote
     * This method is essentially equivalent to the creator <code>{@link #from()}</code> which uses all default 
     * parameters for record field values.
     * 
     * @return  a new <code>FrameTimestampsSpec</code> record as specified in the JAL Tools default configuration
     */
    public static FrameTimestampsSpec    defaultFrame() {
        return FrameTimestampsSpec.from(CNT_SAMPLES_DEF, DUR_PERIOD_DEF, INS_START_DEF, ENM_TMS_TYPE_DEF, DUR_DELAY_DEF);
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates and returns a new ingestion frame timestamp factory instance configured according to the current specifications.
     * </p>
     * <p>
     * The returned frame timestamps factory is fully configured and ready for simulated timestamp generation.  Note 
     * that all timestamps, whether by uniform sampling clock or explicit timestamp list, are generated uniformly.
     * The time interval between timestamps is given by field <code>{@link #durPeriod()}</code> and the number of
     * timestamps produced for each ingestion frame is given by field <code>{@link #cntSamples()}</code>.
     * </p>
     * <p>
     * @apiNote
     * The current implementat for the returned interface <code>{@link IFrameTimestampsFactory}</code> is class
     * <code>{@link FrameTimestampsFactory}</code>.  See class documentation for more details.
     * 
     * @return  a new frame timestamps factory instance ready for simulated ingestion frame timestamp creation
     */
    public IFrameTimestampsFactory  newFactory() {
        FrameTimestampsFactory  facTms = FrameTimestampsFactory.from(this.cntSamples, this.durPeriod, this.insStart, this.enmType, this.durDelay);
        
        return facTms;
    }
    
//    /**
//     * <p>
//     * Computes and returns the first start instant for the timestamps according to the field values.
//     * </p>
//     * <p>
//     * Although computed at each invocation, the returned value is always the same; it is given by
//     * the formula
//     * <pre>
//     *      {@link #insStart} + {@link #durDelay}
//     * </pre>
//     * or, programmatically, 
//     * <code>{@link #insStart}.plus({@link #durDelay})</code>.
//     * </p>
//     *  
//     * @return  a new <code>Instant</code> containing the first timestamp
//     * 
//     * @throws DateTimeException    overflow occurred in <code>Instant</code> addition 
//     * @throws ArithmeticException  <code>Instant</code> addition failed or overflow in <code>Duration</code> multiplication  
//     */
//    public Instant  firstStartInstant() throws DateTimeException, ArithmeticException {
//        Instant insFirst = this.insStart.plus(this.durDelay);   // throws DateTimeException, ArithmeticException
//        
//        return insFirst;
//    }
//    
//    /**
//     * <p>
//     * Computes and returns the next start time for from the given instant.
//     * </p>
//     * <p>
//     * This method is used to advance the timestamp start time for ingestion frames.
//     * The argument is assumed to be the start time of a current ingestion frame and the
//     * returned value is then the start time instant for the next ingestion frame timestamps.
//     * </p>
//     *  
//     * @param insStart  the current start time for current ingestion frame timestamps
//     * 
//     * @return  the start time for the next ingestion frame timestamps
//     * 
//     * @throws DateTimeException    overflow occurred in <code>Instant</code> addition 
//     * @throws ArithmeticException  <code>Instant</code> addition failed or overflow in <code>Duration</code> multiplication  
//     */
//    public Instant nextStartInstant(Instant insStart) throws DateTimeException, ArithmeticException {
//        
//        Duration    durRange = this.durPeriod.multipliedBy(this.cntSamples);    // throws ArithmeticException
//        Instant     insNext = insStart.plus(durRange);      // throws DateTimeException, ArithmeticException
//        
//        return insNext;
//    }
//    
//    /**
//     * <p>
//     * Creates a new <code>UniformSamplingClock</code> instance for the given start time.
//     * </p>
//     * <p>
//     * The remain parameters of the sampling clock (i.e., period and sample count) are taken from the
//     * record fields given at construction.
//     * </p>
//     * 
//     * @param insStart  the start time for the new sampling clock
//     * 
//     * @return  a new <code>UniformSamplingClock</code> instance for the given start time
//     * 
//     * @throws IllegalArgumentException the sample count was negative and/or the period was non-positive
//     */
//    public UniformSamplingClock    nextUniformClock(Instant insStart) throws IllegalArgumentException {
//        
//        UniformSamplingClock clk = UniformSamplingClock.from(insStart, this.cntSamples, this.durPeriod);
//        
//        return clk;
//    }
//    
//    /**
//     * <p>
//     * Creates a new ordered list (vector) of timestamp instants with the given start time.
//     * </p>
//     * <p>
//     * The number of timestamps and the interval between timestamps (i.e., the period) is taken from the
//     * record fields given at construction.
//     * </p>
//     * 
//     * @param insStart  the first timestamp instant with the returned vector
//     * 
//     * @return  an ordered vector of <code>Instant</code> objects representing timestamps from a uniform clock
//     * 
//     * @throws DateTimeException    internal <code>Instant</code> addition failed
//     * @throws ArithmeticException  numeric overflow occurred in <code>Instant</code> addition 
//     */
//    public ArrayList<Instant>   nextTimestampVector(Instant insStart) throws DateTimeException, ArithmeticException {
//        
//        ArrayList<Instant>   vecTms = new ArrayList<>(this.cntSamples);
//        
//        Instant insCurr = insStart;
//        for (int iTms=0; iTms<this.cntSamples; iTms++) {
//            vecTms.add(insCurr);
//            
//            insCurr = insCurr.plus(this.durPeriod);     // throws DateTimeException, ArithmeticException
//        }
//        
//        return vecTms;
//    }

    
    // 
    // Record Overrides
    //
    
    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof FrameTimestampsSpec spec)  {
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
    
    //
    // JAL Library Resources
    //
    
    /** Default configuration parameters for the JAL Tools */
    private static final JalToolsFramesTmsConfig     CFG_DEF = JalToolsConfig.getInstance().datagen.frames.timestamps;
    
    
    //
    // Class Constants
    //
    
    /** Default timestamp type for ingestion frames */
    public static final DpTimestampCase     ENM_TMS_TYPE_DEF = CFG_DEF.type;
    
    /** Default starting time instant for ingestion frame timestamps */
    public static final Instant             INS_START_DEF = CFG_DEF.startInstant();
    
    /** Default sampling period for ingestion frame timestamps */
    public static final Duration            DUR_PERIOD_DEF = CFG_DEF.periodDuration();
    
    /** Default sampling delay from start instant */
    public static final Duration            DUR_DELAY_DEF = CFG_DEF.delayDuration();
    
    /** Default sample count per ingestion frame */
    public static final int                 CNT_SAMPLES_DEF = CFG_DEF.count;
    
}
