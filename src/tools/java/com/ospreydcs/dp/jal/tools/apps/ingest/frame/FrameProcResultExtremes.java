/*
 * Project: dp-jal
 * File:	FrameProcResultExtremes.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.frame
 * Type: 	FrameProcResultExtremes
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
 * @since Feb 11, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.frame;

import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.function.Function;

import com.ospreydcs.dp.jal.tools.common.score.TestResultExtremesBase;

/**
 * <p>
 * Class containing a summary of extreme statistics for a collection of <code>FrameProcTestResult</code> records.
 * </p>
 * <p>
 * Records should be created from <code>{@link #from(Collection)}</code> creator which computes the performance
 * summary, populates the results summary, and returns it.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 11, 2026
 *
 */
public class FrameProcResultExtremes extends TestResultExtremesBase<FrameProcTestParams, FrameProcTestResult> {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>FrameProcResultExtremes</code> instance from the given results collection.
     * </p>
     * <p>
     * The returned test result summary is fully analyzed and ready for print out with method
     * <code>{@link #printOut(java.io.PrintStream, String)}</code>.
     * </p>
     * 
     * @param conResults    collection of the test results
     * 
     * @return  a fully configured <code>FrameProcResultExtremes</code> instance ready for <code>{@link #printOut(java.io.PrintStream, String)}</code>
     * 
     * @throws IllegalArgumentException the argument collection was empty
     * @throws MissingResourceException no <code>TestResult</code> fields were identified for analysis
     * @throws NoSuchElementException   no successful results were contained in the argument collection
     */
    public static FrameProcResultExtremes from(Collection<FrameProcTestResult> conResults) 
            throws IllegalArgumentException, MissingResourceException, NoSuchElementException 
    {
        return new FrameProcResultExtremes(conResults);
    }
    
    
    //
    // TestResultExtremesBase<FrameProcTestParams, FrameProcTestResult> Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultExtremesBase#assignFailedResult()
     */
    @Override
    protected Function<FrameProcTestResult, Boolean> assignFailedResult() {
        Function<FrameProcTestResult, Boolean>  fnc = rec -> rec.recTestStatus().isFailure();
        
        return fnc;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultExtremesBase#assignNumberAssociations()
     */
    @Override
    protected List<ParamToNumField<FrameProcTestParams, FrameProcTestResult>> assignNumberAssociations() {
        List<ParamToNumField<FrameProcTestParams, FrameProcTestResult>>  lstAssocNum = List.of(
                ParamToNumField.from(FrameProcTestParams.COL_SER_ENBL, "Raw Data Rate (MBps)", NumberType.DOUBLE, rec -> rec.recTestCase().recPrcrCfg().bolColSerEnbl() , rec -> rec.dblRateRaw()),
                ParamToNumField.from(FrameProcTestParams.COL_SER_ENBL, "Proc Data Rate (MBps)", NumberType.DOUBLE, rec -> rec.recTestCase().recPrcrCfg().bolColSerEnbl() , rec -> rec.dblRateProc()),
                ParamToNumField.from(FrameProcTestParams.MTHREAD_ENABLE, "Raw Data Rate (MBps)", NumberType.DOUBLE, rec -> rec.recTestCase().recPrcrCfg().bolConcEnbl(), rec -> rec.dblRateRaw()),
                ParamToNumField.from(FrameProcTestParams.MTHREAD_COUNT, "Raw Data Rate (MBps)", NumberType.DOUBLE, rec -> rec.recTestCase().recPrcrCfg().cntMaxThrds(), rec -> rec.dblRateRaw()),
                ParamToNumField.from(FrameProcTestParams.MTHREAD_COUNT, "Proc Data Rate (MBps)", NumberType.DOUBLE, rec -> rec.recTestCase().recPrcrCfg().cntMaxThrds(), rec -> rec.dblRateProc()),
                ParamToNumField.from(FrameProcTestParams.DCMP_ENABLE, "Raw Data Rate (MBps)", NumberType.DOUBLE, rec -> rec.recTestCase().recPrcrCfg().bolConcEnbl(), rec -> rec.dblRateRaw()),
                ParamToNumField.from(FrameProcTestParams.FRAME_DEF, "Raw Data Rate (MBps)", NumberType.DOUBLE, rec -> rec.recTestCase().specFrame().strLabel(), rec -> rec.dblRateRaw()),
                ParamToNumField.from(FrameProcTestParams.FRAME_DEF, "Processed Message Count", NumberType.INTEGER, rec -> rec.recTestCase().specFrame().strLabel(), rec -> rec.cntMsgs())
                );
        
        return lstAssocNum;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultExtremesBase#assignDurationAssociatios()
     */
    @Override
    protected List<ParamToDurField<FrameProcTestParams, FrameProcTestResult>> assignDurationAssociatios() {
        List<ParamToDurField<FrameProcTestParams, FrameProcTestResult>>  lstAssocDur = List.of(
                ParamToDurField.from(FrameProcTestParams.FRAME_CNT, "Processing Duration", rec -> rec.cntFrames(), rec -> rec.durProcessed())
                );
        
        return lstAssocDur;
    }
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>FrameProcResultExtremes</code> instance.
     * </p>
     *
     * @param conResults    collection of the test results
     * 
     * @throws IllegalArgumentException the argument collection was empty
     * @throws MissingResourceException no <code>TestResult</code> fields were identified for analysis
     * @throws NoSuchElementException   no successful results were contained in the argument collection
     */
    public FrameProcResultExtremes(Collection<FrameProcTestResult> conResults)
            throws IllegalArgumentException, MissingResourceException, NoSuchElementException {
        super(FrameProcTestParams.class, conResults);
    }

}
