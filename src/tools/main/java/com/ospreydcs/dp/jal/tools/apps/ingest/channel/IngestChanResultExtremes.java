/*
 * Project: dp-jal
 * File:	IngestChanResultExtremes.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.channel
 * Type: 	IngestChanResultExtremes
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

import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.function.Function;

import com.ospreydcs.dp.jal.tools.common.score.TestResultExtremesBase;

/**
 * <p>
 * Class for computing the extreme test result responses (statistical) to changes in test parameters for a 
 * collection of <code>IngestChanTestResult</code> records.
 * </p>
 * <p>
 * The class is intended to compute the sensitivity of <code>IngestionChannel</code> performance to variations
 * in the test parameter values. Most activity is performed in base class <code>{@link TestResultExtremesBase}</code>.
 * This class simply defines the parameters to be varied and the result fields to be analyzed using the
 * abstract methods <code>{@link #assignNumberAssociations()}</code> and <code>{@link #assignDurationAssociatios()}</code>.
 * See class documentation for base class <code>{@link TestResultExtremesBase}</code> for further details.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 23, 2026
 *
 */
public class IngestChanResultExtremes extends TestResultExtremesBase<IngestChanTestParams, IngestChanTestResult> {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>IngestChanResultExtremes</code> instance from the given results collection.
     * </p>
     * <p>
     * The returned test result summary is fully analyzed and ready for print out with method
     * <code>{@link #printOut(java.io.PrintStream, String)}</code>.
     * </p>
     * 
     * @param conResults    collection of the test results
     * 
     * @return  a fully configured <code>IngestChanResultExtremes</code> instance ready for <code>{@link #printOut(java.io.PrintStream, String)}</code>
     * 
     * @throws IllegalArgumentException the argument collection was empty
     * @throws MissingResourceException no <code>TestResult</code> fields were identified for analysis
     * @throws NoSuchElementException   no successful results were contained in the argument collection
     */
    public static IngestChanResultExtremes from(Collection<IngestChanTestResult> conResults) 
            throws IllegalArgumentException, MissingResourceException, NoSuchElementException 
    {
        return new IngestChanResultExtremes(conResults);
    }
    
    
    //
    // Class Constants
    //
    
    /** Data transmission rate test result field description */
    public static final String      STR_RATE_XMIT = "Data transmission rate (MBps)";
    
    /** Data transmission duration test result field description */
    public static final String      STR_DUR_XMIT = "Data transmission duration";
    
    
    //
    // TestResultExtremesBase<IngestChanTestParams, IngestChanTestResult> Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultExtremesBase#assignFailedResult()
     */
    @Override
    protected Function<IngestChanTestResult, Boolean> assignFailedResult() {
        return rec -> rec.recTestStatus().isFailure();
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultExtremesBase#assignNumberAssociations()
     */
    @Override
    protected List<ParamToNumField<IngestChanTestParams, IngestChanTestResult>> assignNumberAssociations() {
        List<ParamToNumField<IngestChanTestParams, IngestChanTestResult>>   lstAssocs = List.of(
                ParamToNumField.from(IngestChanTestParams.COL_SER_ENBL, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().recProcCfg().bolColSerEnbl(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestChanTestParams.STREAM_TYPE, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().recChanCfg().enmStrmType(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestChanTestParams.MSTREAM_ENBL, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().recChanCfg().bolMStrmEnbl(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestChanTestParams.MSTREAM_CNT, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().recChanCfg().cntMStrmMax(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestChanTestParams.FRAME_CNT, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().cntFrames(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestChanTestParams.FRAME_DEF, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().specFrame().strLabel(), rec -> rec.dblRateXmit())
                );
        
        return lstAssocs;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultExtremesBase#assignDurationAssociatios()
     */
    @Override
    protected List<ParamToDurField<IngestChanTestParams, IngestChanTestResult>> assignDurationAssociatios() {
        List<ParamToDurField<IngestChanTestParams, IngestChanTestResult>>   lstAssocs = List.of(
                ParamToDurField.from(IngestChanTestParams.COL_SER_ENBL, STR_DUR_XMIT, rec -> rec.recTestCase().recProcCfg().bolColSerEnbl(), rec -> rec.durTransmit()),
                ParamToDurField.from(IngestChanTestParams.STREAM_TYPE, STR_DUR_XMIT, rec -> rec.recTestCase().recChanCfg().enmStrmType(), rec -> rec.durTransmit()),
                ParamToDurField.from(IngestChanTestParams.MSTREAM_ENBL, STR_DUR_XMIT, rec -> rec.recTestCase().recChanCfg().bolMStrmEnbl(), rec -> rec.durTransmit()),
                ParamToDurField.from(IngestChanTestParams.MSTREAM_CNT, STR_DUR_XMIT, rec -> rec.recTestCase().recChanCfg().cntMStrmMax(), rec -> rec.durTransmit()),
                ParamToDurField.from(IngestChanTestParams.FRAME_DEF, STR_DUR_XMIT, rec -> rec.recTestCase().specFrame().strLabel(), rec -> rec.durTransmit())
                );
        
        return lstAssocs;
    }

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>IngestChanResultExtremes</code> instance.
     * </p>
     *
     * @param conResults    collection of the test results
     * 
     * @throws IllegalArgumentException the argument collection was empty
     * @throws MissingResourceException no <code>TestResult</code> fields were identified for analysis
     * @throws NoSuchElementException   no successful results were contained in the argument collection
     */
    private IngestChanResultExtremes(Collection<IngestChanTestResult> conResults)
            throws IllegalArgumentException, MissingResourceException, NoSuchElementException {
        super(IngestChanTestParams.class, conResults);
    }

}
