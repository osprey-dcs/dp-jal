/*
 * Project: dp-api-common
 * File:	IngestionFrameGenerator.java
 * Package: com.ospreydcs.dp.jal.tools.ingest.frame
 * Type: 	IngestionFrameGenerator
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
package com.ospreydcs.dp.jal.tools.ingest.frame;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.DpTimestampCase;
import com.ospreydcs.dp.api.common.IDataColumn;
import com.ospreydcs.dp.api.common.UniformSamplingClock;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;

/**
 *
 * @author Christopher K. Allen
 * @since Jun 11, 2025
 *
 */
public class IngestionFrameGenerator {
    
    
    //
    // Creators
    //
    
    /**
    /**
     * <p>
     * Creates and returns a new <code>IngestionFrameGenerator</code> instance configured from the given record.
     * </p>
     * <p>
     * The new <code>IngestionFrameGenerator</code> is fully operational after construction.  Use the
     * <code>{@link #build()}</code> method to create new <code>IngestionFrame</code> instances as
     * desired.
     * </p>
     *
     * @param recCfg    record containing the configuration parameters for ingestion frame creation 
     * 
     * @return  a new <code>IngestionFrameGenerator</code> instance ready from <code>IngestionFrame</code> creation
     * 
     * @throws IllegalArgumentException invalid and/or inconsistent record configuration (see message and cause) 
     */
    public static IngestionFrameGenerator   from(SampleBlockConfig recCfg) throws IllegalArgumentException {
        return new IngestionFrameGenerator(recCfg);
    }

    
    //
    // Application Resources
    //
    
    /** Default configuration parameters for the Query Service tools */
    private static final DpIngestionConfig  CFG_INGEST = DpApiConfig.getInstance().ingest;
    
    /** Default configuration parameters for the JAL Tools */
    private static final JalToolsConfig     CFG_TOOLS = JalToolsConfig.getInstance();
    
    
    //
    // Class Constants
    //
    
    /** Name of the frame generator */
    public static final String  STR_NAME = IngestionFrameGenerator.class.getSimpleName();
    
    
    /** The ISO formatted inception time of the Data Platform Test Archive */
    public static final String STR_TM_START = CFG_TOOLS.testArchive.range.start;
    
    /** The ISO formatted final time of the Data Platform Test Archive */
    public static final String STR_TM_FINAL = CFG_TOOLS.testArchive.range.end;
    
    
    /** The inception time instant of the Data Platform Test Archive */
    public static final Instant    INS_START = Instant.parse(STR_TM_START);
    
    /** The final time instant within the Data Platform Test Archive */
    public static final Instant    INS_FINAL = Instant.parse(STR_TM_FINAL);
    
    
    /** Common attributes for each ingestion frame */
    public static final Map<String, String>     MAP_ATTRS = Map.of(
            "Source", STR_NAME, 
            "Initiated", Instant.now().toString(),
            "Values", "Simulated"
            ); 
    
    
    //
    // Class Resources
    //
    
    /** The event logging enabled/disabled flag */
    private static final boolean    BOL_LOGGING = CFG_INGEST.logging.enabled;
    
    /** The event logging level */
    private static final String     STR_LOGGING_LEVEL = CFG_INGEST.logging.level;
    
    
    /** The class event logger - used for ISO parsing */
    private static final Logger LOGGER = Log4j.getLogger(IngestionFrameGenerator.class, STR_LOGGING_LEVEL);
    
    
    
    //
    // Defining Attributes
    //
    
//    /** Record containing configuration parameters for generating ingestion frames */
//    private final SampleBlockConfig recCfg;
    
    /** The number of samples for each process variable in the ingestion frame */
    private final int               cntSamples;
    
    /** The sample period for each process variable in ingestion frames */
    private final Duration          durPeriod;
    
    /** The method of specifying timestamps within each ingestion frame */
    private final DpTimestampCase   enmTmsCase;
    
    
    //
    // Instance Resources
    //
    
    /** The data column generator used for creating sample processes within each ingestion frame - configured from input record */
    private final DataColumnGenerator   genCols;
    
    
    //
    // State Variables
    //
    
    /** Index counter for ingestion frames created - used for frame label creation */
    private int         indFrame = 0;
    
    /** The start time for each ingestion frame - advanced after each frame created */
    private Instant     insStart = INS_START;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>IngestionFrameGenerator</code> instance configured from the given record.
     * </p>
     * <p>
     * The new <code>IngestionFrameGenerator</code> is fully operational after construction.  Use the
     * <code>{@link #build()}</code> method to create new <code>IngestionFrame</code> instances as
     * desired.
     * </p>
     *
     * @param recCfg    record containing the configuration parameters for ingestion frame creation
     *  
     * @throws IllegalArgumentException invalid and/or inconsistent record configuration (see message and cause) 
     */
    public IngestionFrameGenerator(SampleBlockConfig recCfg) throws IllegalArgumentException {
//        this.recCfg = recCfg;
        
        this.cntSamples = recCfg.cntSamples();
        this.durPeriod = recCfg.tmaPeriod().getDuration();
        this.enmTmsCase = recCfg.enmTmsCase();
        
        this.genCols = DataColumnGenerator.from(recCfg);
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates a new <code>IngestionFrame</code> instance containing simulated data.
     * </p>
     * <p>
     * The returned ingestion frame is configured according to the configuration record provided 
     * at construction.  This method can be invoked multiple times each time provided a new ingestion
     * frame with different data, and timestamps starting from the previous invocation.
     * <p>
     * <h2>NOTES:</h2>
     * Any exceptions here originate in the configuration record provided at construction.
     * </p>
     * 
     * @return  a new <code>IngestionFrame</code> containing simulated data
     * 
     * @throws IllegalArgumentException     the sample count was negative and/or the period was non-positive
     * @throws DateTimeException            internal <code>Instant</code> addition failed
     * @throws ArithmeticException          numeric overflow occurred in <code>Instant</code> addition 
     * @throws UnsupportedOperationException an unsupported timestamp case was encountered
     */
    public IngestionFrame   build() throws IllegalArgumentException, DateTimeException, ArithmeticException, UnsupportedOperationException {
        
        ArrayList<IDataColumn<Object>>  vecCols = this.genCols.build();
        
        IngestionFrame frmNext = switch (this.enmTmsCase) {
        case SAMPLING_CLOCK -> IngestionFrame.from(this.nextUniformClock(insStart), vecCols);   // throws IllegalArgumentException
        case TIMESTAMP_LIST -> IngestionFrame.from(this.nextTimestampVector(insStart), vecCols);// throws DateTimeException, ArithmeticException
        case UNSUPPORTED_CASE -> {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Timestamp case " + this.enmTmsCase + " is not viable.";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);

            throw new UnsupportedOperationException(strMsg);
            }
        default -> { 
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Timestamp case " + this.enmTmsCase + " is not supported.";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);

            throw new UnsupportedOperationException(strMsg);
            }
        };
        
        frmNext.setFrameLabel(this.nextFrameLabel());
        frmNext.setFrameTimestamp(this.insStart);
        frmNext.addAttributes(MAP_ATTRS);
        
        this.insStart = this.nextStartInstant(insStart);
        
        return frmNext;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates a new <code>UniformSamplingClock</code> instance for the given start time.
     * </p>
     * <p>
     * The remain parameters of the sampling clock (i.e., period and sample count) are taken from the
     * configuration record given at construction.
     * </p>
     * 
     * @param insStart  the start time of the new sampling clock
     * 
     * @return  a new <code>UniformSamplingClock</code> instance with the given start time
     * 
     * @throws IllegalArgumentException the sample count was negative and/or the period was non-positive
     */
    private UniformSamplingClock    nextUniformClock(Instant insStart) throws IllegalArgumentException {
        
        UniformSamplingClock clk = UniformSamplingClock.from(insStart, this.cntSamples, this.durPeriod);
        
        return clk;
    }
    
    /**
     * <p>
     * Creates a new ordered list (vector) of timestamp instants with the given start time.
     * </p>
     * <p>
     * The number of timestamps and the interval between timestamps (i.e., the period) is taken from the
     * configuration record given at construction.
     * </p>
     * 
     * @param insStart  the first timestamp instant with the returned vector
     * 
     * @return  an ordered vector of <code>Instant</code> objects representing timestamps from a uniform clock
     * 
     * @throws DateTimeException    internal <code>Instant</code> addition failed
     * @throws ArithmeticException  numeric overflow occurred in <code>Instant</code> addition 
     */
    private ArrayList<Instant>   nextTimestampVector(Instant insStart) throws DateTimeException, ArithmeticException {
        
        ArrayList<Instant>   vecTms = new ArrayList<>(this.cntSamples);
        
        Instant insCurr = insStart;
        for (int iTms=0; iTms<this.cntSamples; iTms++) {
            vecTms.add(insCurr);
            
            insCurr = insCurr.plus(this.durPeriod);
        }
        
        this.insStart = insCurr;
        
        return vecTms;
    }
    
    /**
     * <p>
     * Computes and returns the next start time for the next ingestion frame.
     * </p>
     * <p>
     * If multiple ingestion frames are create in sequence this method should be used to advance the
     * timestamp start time for each consecutive frame.
     * </p>
     *  
     * @param insStart  the current start time for current ingestion frame timestamps
     * 
     * @return  the start time for the next ingestion frame timestamps
     * 
     * @throws DateTimeException    internal <code>Instant</code> addition failed
     * @throws ArithmeticException  numeric overflow occurred in <code>Instant</code> addition 
     */
    private Instant nextStartInstant(Instant insStart) throws DateTimeException, ArithmeticException {
        
        Duration    durRange = this.durPeriod.multipliedBy(this.cntSamples);
        Instant     insNext = insStart.plus(durRange);
        
        return insNext;
    }

    
    /**
     * <p>
     * Creates and returns a new ingestion frame label.
     * </p>
     * The ingestion frame label is created by concatenating the class name <code>{@link #STR_NAME}</code>
     * with the current frame index <code>{@link #indFrame}</code> value.  The frame index is then
     * incremented for the next frame label.
     * </p>
     * 
     * @return  the next label for an ingestion frame
     */
    private String  nextFrameLabel() {
        String  strLabel = STR_NAME + "-" + Integer.toString(this.indFrame);
        
        this.indFrame++;
        
        return strLabel;
    }

}
