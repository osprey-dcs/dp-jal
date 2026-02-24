/*
 * Project: dp-jal
 * File:	IngestChanResultStats.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.channel
 * Type: 	IngestChanResultStats
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
 * @since Feb 23, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.channel;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.function.Function;

import com.ospreydcs.dp.jal.tools.common.score.TestResultStatsBase;

/**
 * <p>
 * Class containing a performance summary statistics for a collection of <code>FrameProcTestResult</code> records.
 * </p>
 * <p>
 * Records should be created from <code>{@link #from(Collection)}</code> creator which computes the performance
 * summary, populates the results summary, and returns it.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 23, 2026
 *
 */
public class IngestChanResultStats extends TestResultStatsBase<IngestChanTestResult> {

    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>IngestChanResultStats</code> instance from the given results collection.
     * </p>
     * <p>
     * The returned test result summary is fully analyzed and ready for print out with method
     * <code>{@link #printOut(java.io.PrintStream, String)}</code>.
     * </p>
     * 
     * @param conResults    the collection of test results to be analyzed
     * 
     * @return  a new <code>IngestChanResultStats</code> containing the statistical summaries of the given collection
     * 
     * @throws IllegalArgumentException the argument collection was empty
     * @throws MissingResourceException no <code>TestResult</code> fields were identified for analysis
     * @throws NoSuchElementException   no successful results were contained in the argument collection
     */
    public static IngestChanResultStats from(Collection<IngestChanTestResult> conResults) 
            throws IllegalArgumentException, MissingResourceException, NoSuchElementException
    {
        return new IngestChanResultStats(conResults);
    }
    
    
    //
    // Class Methods
    //
    
    /**
     * <p>
     * Assigns the default targeted transmission rate.
     * </p>
     * 
     * @param dblRate   target data transmission for all test results (in MBps)
     */
    public static void  assignTargetTransmissionRate(double dblRate) {
        IngestChanResultStats.DBL_RATE_TGT = dblRate;
    }
    
    /**
     * <p>
     * Assigned the default targeted transmission duration.
     * </p>
     * 
     * @param durProc   target transmission duration for all test results
     */
    public static void  assignTargetProcessingDuration(Duration durProc) {
        IngestChanResultStats.DUR_XMIT_TGT = durProc;
    }
    
    
    //
    // Class Variables
    //
    
    /** The target data rate (in MBps) */
    public static double    DBL_RATE_TGT = 500.0;
    
    /** The target processing duration */
    public static Duration  DUR_XMIT_TGT = Duration.ofMillis(100);
    
    
    //
    // TestResultsSummaryBase<IngestChaneTestResult> Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultStatsBase#assignFailedResult()
     */
    @Override
    protected Function<IngestChanTestResult, Boolean> assignFailedResult() {
        Function<IngestChanTestResult, Boolean>  fnc = rec -> rec.recTestStatus().isFailure();
        
        return fnc;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultStatsBase#assignNumericFields()
     */
    @Override
    protected List<NumberField<IngestChanTestResult>> assignNumericFields() {
        List<NumberField<IngestChanTestResult>>     lstFlds = List.of(
                NumberField.from("Raw data transmission rate (MBps)", NumberType.DOUBLE, DBL_RATE_TGT, rec -> rec.dblRateXmit()),
                NumberField.from("Number of data messages transmitted", NumberType.INTEGER, null, rec -> rec.cntMsgsXmit()),
                NumberField.from("Allocation size (bytes) transmitted", NumberType.LONG, null, rec -> rec.szAllocXmit()),
                NumberField.from("Payload ingestion frame count", NumberType.INTEGER, null, rec -> rec.cntFrames()),
                NumberField.from("Payload allocation size (bytes)", NumberType.LONG, null, rec -> rec.szPayload()),
                NumberField.from("Number of UNI message responses", NumberType.INTEGER, null, rec -> rec.lstUniRsps().size()),
                NumberField.from("Number of BIDI message responses", NumberType.INTEGER, null, rec -> rec.lstBidiRsps().size())
                );
        
        return lstFlds;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultStatsBase#assignDurationFields()
     */
    @Override
    protected List<DurationField<IngestChanTestResult>> assignDurationFields() {
        List<DurationField<IngestChanTestResult>>   lstFlds = List.of(
                DurationField.from("Data transmission duration", DUR_XMIT_TGT, rec -> rec.durTransmit()),
                DurationField.from("Data processing duration", DUR_XMIT_TGT, rec -> rec.durProcessed())
                );
        
        return lstFlds;
    }

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>IngestChanResultStats</code> instance.
     * </p>
     *
     * @param conResults    the collection of test results to be analyzed
     * 
     * @throws IllegalArgumentException the argument collection was empty
     * @throws MissingResourceException no <code>TestResult</code> fields were identified for analysis
     * @throws NoSuchElementException   no successful results were contained in the argument collection
     */
    private IngestChanResultStats(Collection<IngestChanTestResult> conResults)
            throws IllegalArgumentException, MissingResourceException, NoSuchElementException {
        super(conResults);
    }

}
