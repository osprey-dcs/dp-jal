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
import java.util.concurrent.TimeUnit;

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
    // Operations
    //
    
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
        
        ps.println(strPad + "Process Variable name(s) : " + this.setPvNames);
        ps.println(strPad + "Process Variable(s) type : " + this.enmDataType);
        ps.println(strPad + "Timestamp representation : " + this.enmTmsCase);
        ps.println(strPad + "Number of samples per PV : " + this.cntSamples);
        ps.println(strPad + "Sampling period          : " + this.tmaPeriod.duration);
        ps.println(strPad + "Sampling delay           : " + this.tmaDelay.duration);
    }

}
