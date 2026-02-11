/*
 * Project: dp-jal
 * File:	FrameProcResultStats.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.frame
 * Type: 	FrameProcResultStats
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
 * @since Feb 9, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.frame;

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
 * @since Feb 9, 2026
 *
 */
public class FrameProcResultStats extends TestResultStatsBase<FrameProcTestResult> {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>FrameProcResultStats</code> instance from the given results collection.
     * </p>
     * <p>
     * The returned test result summary is fully analyzed and ready for print out with method
     * <code>{@link #printOut(java.io.PrintStream, String)}</code>.
     * </p>
     * 
     * @param conResults    the collection of test results to be analyzed
     * 
     * @return  a new <code>FrameProcResultStats</code> containing the statistical summaries of the given collection
     * 
     * @throws IllegalArgumentException the argument collection was empty
     * @throws MissingResourceException no <code>TestResult</code> fields were identified for analysis
     * @throws NoSuchElementException   no successful results were contained in the argument collection
     */
    public static FrameProcResultStats from(Collection<FrameProcTestResult> conResults) 
            throws IllegalArgumentException, MissingResourceException, NoSuchElementException
    {
        return new FrameProcResultStats(conResults);
    }
    
    
    //
    // Class Variables
    //
    
    /** The target data rate (in MBps) */
    public static double    DBL_RATE_TGT = 500.0;
    
    /** The target processing duration */
    public static Duration  DUR_PROC_TGT = Duration.ofMillis(100);
    
    
    //
    // TestResultsSummaryBase<FrameProcTestResult> Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultStatsBase#assignFailedResult()
     */
    @Override
    protected Function<FrameProcTestResult, Boolean> assignFailedResult() {
        Function<FrameProcTestResult, Boolean>  fnc = rec -> rec.recTestStatus().isFailure();
        
        return fnc;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultStatsBase#assignNumericFields()
     */
    @Override
    protected List<NumberField<FrameProcTestResult>> assignNumericFields() {
        List<NumberField<FrameProcTestResult>>  lstFlds = List.of(
                NumberField.from("Raw Data Processing Rate (MBps)", NumberType.DOUBLE, DBL_RATE_TGT, rec -> rec.dblRateRaw()),
                NumberField.from("Processed Data Rate (MBps)", NumberType.DOUBLE, DBL_RATE_TGT, rec -> rec.dblRateProc()),
                NumberField.from("Number of Ingestion Frames", NumberType.INTEGER, null, rec -> rec.cntFrames()),
                NumberField.from("Number of Data Messages", NumberType.INTEGER, null, rec -> rec.cntMsgs()),
                NumberField.from("Payload Allocation (bytes)", NumberType.LONG, null, rec -> rec.szPayload()),
                NumberField.from("Processed Message Allocation (bytes)", NumberType.LONG, null, rec -> rec.szProcessed())
                );
        
        return lstFlds;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultStatsBase#assignDurationFields()
     */
    @Override
    protected List<DurationField<FrameProcTestResult>> assignDurationFields() {
        List<DurationField<FrameProcTestResult>>    lstFlds = List.of(
                DurationField.from("Ingestion Frame Processing Duration", DUR_PROC_TGT, rec -> rec.durProcessed())
                );
        
        return lstFlds;
    }

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>FrameProcResultStats</code> instance.
     * </p>
     *
     * @param   conResults  the collection of all test results to be analyzed
     * 
     * @throws IllegalArgumentException the argument collection was empty
     * @throws MissingResourceException no <code>TestResult</code> fields were identified for analysis
     * @throws NoSuchElementException   no successful results were contained in the argument collection
     */
    public FrameProcResultStats(Collection<FrameProcTestResult> conResults)
            throws IllegalArgumentException, MissingResourceException, NoSuchElementException {
        super(conResults);
    }

}
