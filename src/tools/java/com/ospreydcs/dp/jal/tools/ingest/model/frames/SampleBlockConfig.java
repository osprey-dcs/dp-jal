/*
 * Project: dp-api-common
 * File:	SampleBlockConfig.java
 * Package: com.ospreydcs.dp.jal.tools.ingest.model.frames
 * Type: 	SampleBlockConfig
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
 * @since Jun 11, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.ingest.model.frames;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import com.ospreydcs.dp.api.common.DpTimestampCase;
import com.ospreydcs.dp.api.common.TimeAbstraction;
import com.ospreydcs.dp.jal.tools.ingest.model.values.JalScalarType;

/**
 * <p>
 * Record with fields describing the configuration of a sampling block of simulated data.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Jun 11, 2025
 *
 * @param setPvNames    collection of Process Variable names for the sampling block 
 * @param enmDataType   the data type for all PVs within the sampling block
 * @param enmTmsCase    the timestamp type used for the sampling block
 * @param cntSamples    the number of samples for each PV within the sampling block
 * @param tmaPeriod     the sample period
 * @param tmaDelay      the sampling start time delay (typically from the Test Archive inception)   
 */
public record SampleBlockConfig(
        Set<String>     setPvNames,
        JalScalarType   enmDataType,
        DpTimestampCase enmTmsCase,
        int             cntSamples,
        TimeAbstraction tmaPeriod,
        TimeAbstraction tmaDelay
        ) 

{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates new <code>SampleBlockConfig</code> instance with fields populated by the given arguments.
     * </p>
     * 
     * @param setPvNames    collection of Process Variable names for the sampling block 
     * @param enmDataType   the data type for all PVs within the sampling block
     * @param enmTmsCase    the timestamp type used for the sampling block
     * @param cntSamples    the number of samples for each PV within the sampling block
     * @param durPeriod     the sample period as a Java <code>{@link Duration}</code> instance 
     * @param durDelay      the sampling start time delay (e.g., from the Test Archive inception)   
     * 
     * @return  a new <code>SampleBlockConfig</code> record populated with the given arguments
     */
    public static SampleBlockConfig from(
            Set<String>     setPvNames, 
            JalScalarType   enmDataType, 
            DpTimestampCase enmTmsCase, 
            int             cntSamples,
            Duration        durPeriod,
            Duration        durDelay
            ) 
    {
        TimeAbstraction tmaPeriod = TimeAbstraction.from(durPeriod);
        TimeAbstraction tmaDelay = TimeAbstraction.from(durDelay);
        
        return new SampleBlockConfig(setPvNames, enmDataType, enmTmsCase, cntSamples, tmaPeriod, tmaDelay);
    }
    
    /**
     * <p>
     * Creates new <code>SampleBlockConfig</code> instance with fields populated by the given arguments.
     * </p>
     * <p>
     * The argument <code>strPvNmPrefix</code> is used to create a <code>{@link Set}</code> of <code>cntPvs</code>
     * PV name strings of the form
     * <pre>
     *      <code>setPvNames</code> = { strPvNmPrefix + "1", ..., strPvNmPrefix + Integer.toString(cntPvs) }
     * </pre>
     * Once the set <code>setPvNames</code> is created the method then defers to 
     * <code>{@link #from(Set, JalScalarType, DpTimestampCase, int, Duration, Duration)}</code>.
     * </p>
     * 
     * @param strPvNmPrefix prefix given to all Process Variable names for the sampling block, suffixed by index 
     * @param cntPvs        the number of PV names to create
     * @param enmDataType   the data type for all PVs within the sampling block
     * @param enmTmsCase    the timestamp type used for the sampling block
     * @param cntSamples    the number of samples for each PV within the sampling block
     * @param durPeriod     the sample period as a Java <code>{@link Duration}</code> instance 
     * @param durDelay      the sampling start time delay (e.g., from the Test Archive inception)   
     * 
     * @return  a new <code>SampleBlockConfig</code> record populated with the given arguments
     */
    public static SampleBlockConfig from(
            String          strPvNmPrefix,
            int             cntPvs,
            JalScalarType   enmDataType,
            DpTimestampCase enmTmsCase,
            int             cntSamples,
            Duration        durPeriod,
            Duration        durDelay
            )
    {
        Set<String> setPvNames = IntStream.rangeClosed(1, cntPvs)
                .mapToObj(i -> Integer.toString(i))
                .map(str -> strPvNmPrefix + str)
                .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        
        return SampleBlockConfig.from(setPvNames, enmDataType, enmTmsCase, cntSamples, durPeriod, durDelay);
    }
    
    /**
     * <p>
     * Creates new <code>SampleBlockConfig</code> instance with fields populated by the given arguments.
     * </p>
     * <p>
     * The argument <code>strPvNmPrefix</code> is used to create a <code>{@link Set}</code> of <code>cntPvs</code>
     * PV name strings of the form
     * <pre>
     *      <code>setPvNames</code> = { strPvNmPrefix + "1", ..., strPvNmPrefix + Integer.toString(cntPvs) }
     * </pre>
     * Once the set <code>setPvNames</code> is created the method then defers to 
     * <code>{@link #from(Set, JalScalarType, DpTimestampCase, int, Duration, Duration)}</code>.
     * </p>
     * 
     * @param strPvNmPrefix prefix given to all Process Variable names for the sampling block, suffixed by index 
     * @param cntPvs        the number of PV names to create
     * @param enmDataType   the data type for all PVs within the sampling block
     * @param enmTmsCase    the timestamp type used for the sampling block
     * @param cntSamples    the number of samples for each PV within the sampling block
     * @param durPeriod     the sample period as a Java <code>{@link Duration}</code> instance 
     * @param durDelay      the sampling start time delay (e.g., from the Test Archive inception)   
     * 
     * @return  a new <code>SampleBlockConfig</code> record populated with the given arguments
     */
    public static SampleBlockConfig from(
            String          strPvNmPrefix,
            int             cntPvs,
            JalScalarType   enmDataType,
            DpTimestampCase enmTmsCase,
            int             cntSamples,
            Duration        durPeriod
            )
    {
        return SampleBlockConfig.from(strPvNmPrefix, cntPvs, enmDataType, enmTmsCase, cntSamples, durPeriod, Duration.ZERO);
    }
    
    /**
     * <p>
     * Creates new <code>SampleBlockConfig</code> instance with fields populated by the given arguments.
     * </p>
     * <p>
     * The sampling delay defaults to <code>{@link TimeAbstraction#ZERO}</code>.
     * </p>
     * 
     * @param setPvNames    collection of Process Variable names for the sampling block 
     * @param enmDataType   the data type for all PVs within the sampling block
     * @param enmTmsCase    the timestamp type used for the sampling block
     * @param cntSamples    the number of samples for each PV within the sampling block
     * @param durPeriod     the sample period as a Java <code>{@link Duration}</code> instance 
     * 
     * @return  a new <code>SampleBlockConfig</code> record populated with the given arguments
     */
    public static SampleBlockConfig from(
            Set<String>     setPvNames, 
            JalScalarType   enmDataType, 
            DpTimestampCase enmTmsCase, 
            int             cntSamples,
            Duration        durPeriod
            ) 
    {
        TimeAbstraction tmaPeriod = TimeAbstraction.from(durPeriod);
        
        return new SampleBlockConfig(setPvNames, enmDataType, enmTmsCase, cntSamples, tmaPeriod, TimeAbstraction.ZERO);
    }
    
    /**
     * <p>
     * Creates new <code>SampleBlockConfig</code> instance with fields populated by the given arguments.
     * </p>
     * <p>
     * The sampling delay defaults to <code>{@link TimeAbstraction#ZERO}</code>.
     * </p>
     * 
     * @param setPvNames    collection of Process Variable names for the sampling block 
     * @param enmDataType   the data type for all PVs within the sampling block
     * @param enmTmsCase    the timestamp type used for the sampling block
     * @param cntSamples    the number of samples for each PV within the sampling block
     * @param lngPeriod     the sample period 
     * @param tuPeriod      the time units for the sample period
     * 
     * @return  a new <code>SampleBlockConfig</code> record populated with the given arguments
     */
    public static SampleBlockConfig from(
            Set<String>     setPvNames, 
            JalScalarType   enmDataType, 
            DpTimestampCase enmTmsCase, 
            int             cntSamples,
            long            lngPeriod,
            TimeUnit        tuPeriod,
            long            lngDelay,
            TimeUnit        tuDelay
            ) 
    {
        TimeAbstraction tmaPeriod = TimeAbstraction.from(lngPeriod, tuPeriod);
        TimeAbstraction tmaDelay = TimeAbstraction.from(lngDelay, tuDelay);
        
        return new SampleBlockConfig(setPvNames, enmDataType, enmTmsCase, cntSamples, tmaPeriod, tmaDelay);
    }
    
    /**
     * <p>
     * Creates new <code>SampleBlockConfig</code> instance with fields populated by the given arguments.
     * </p>
     * <p>
     * The sampling delay defaults to <code>{@link TimeAbstraction#ZERO}</code>.
     * </p>
     * 
     * @param setPvNames    collection of Process Variable names for the sampling block 
     * @param enmDataType   the data type for all PVs within the sampling block
     * @param enmTmsCase    the timestamp type used for the sampling block
     * @param cntSamples    the number of samples for each PV within the sampling block
     * @param lngPeriod     the sample period 
     * @param tuPeriod      the time units for the sample period
     * 
     * @return  a new <code>SampleBlockConfig</code> record populated with the given arguments
     */
    public static SampleBlockConfig from(
            Set<String>     setPvNames, 
            JalScalarType   enmDataType, 
            DpTimestampCase enmTmsCase, 
            int             cntSamples,
            long            lngPeriod,
            TimeUnit        tuPeriod
            ) 
    {
        TimeAbstraction tmaPeriod = TimeAbstraction.from(lngPeriod, tuPeriod);
        
        return new SampleBlockConfig(setPvNames, enmDataType, enmTmsCase, cntSamples, tmaPeriod, TimeAbstraction.ZERO);
    }
    
    
    //
    // Record Resources
    //
    
    /** Maximum number of PV names to print out in <code>{@link #printOut(PrintStream, String)}</code> */
    public static int   INT_PRINT_PVS_MAX = 10;
    
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Sets the maximum number of PV names that will appear in an output line within 
     * <code>{@link #printOut(PrintStream, String)}</code>.
     * </p>
     * <p>
     * If the size of the field <code>{@link #setPvNames}</code> is larger than the given value then the
     * "PV names" output line within <code>{@link #printOut(PrintStream, String)}</code> is skipped.
     * </p>
     * @param cntPvsMax
     */
    public static void  setPvCountPrintOutCutoff(int cntPvsMax) {
        INT_PRINT_PVS_MAX = cntPvsMax;
    }
    
    /**
     * <p>
     * Prints out a text description of the record fields to the given output stream.
     * </p>
     * <p>
     * A line-by-line text description of each record field is written to the given output.
     * The <code>strPad</code> is used to supply an optional whitespace character padding to the
     * left-hand side header for each line description.
     * </p>
     *   
     * @param ps        output stream to receive text description of record fields
     * @param strPad    white-space padding for each line header (or <code>null</code>)
     */
    public void printOut(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        
        if (this.setPvNames.size() <= INT_PRINT_PVS_MAX)
            ps.println(strPad + "Process Variable name(s) : " + this.setPvNames);
        ps.println(strPad + "Process Variable count   : " + this.setPvNames.size());
        ps.println(strPad + "Process Variable(s) type : " + this.enmDataType);
        ps.println(strPad + "Timestamp representation : " + this.enmTmsCase);
        ps.println(strPad + "Number of samples per PV : " + this.cntSamples);
        ps.println(strPad + "Sampling period          : " + this.tmaPeriod.duration);
        ps.println(strPad + "Sampling delay           : " + this.tmaDelay.duration);
    }

}
