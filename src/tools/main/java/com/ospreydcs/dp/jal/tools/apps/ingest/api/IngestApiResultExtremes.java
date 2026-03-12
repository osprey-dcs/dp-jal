/*
 * Project: dp-jal
 * File:	IngestApiResultExtremes.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.api
 * Type: 	IngestApiResultExtremes
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
 * @since Mar 6, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.api;

import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.function.Function;

import com.ospreydcs.dp.jal.tools.common.score.TestResultExtremesBase;

/**
 * <p>
 * Class for computing the extreme test result responses (statistical) to changes in test parameters for a 
 * collection of <code>IngestApiTestResult</code> records.
 * </p>
 * <p>
 * The class is intended to compute the sensitivity of JAL Ingestion Service APIs performance to variations
 * in the test parameter values. Most activity is performed in base class <code>{@link TestResultExtremesBase}</code>.
 * This class simply defines the parameters to be varied and the result fields to be analyzed using the
 * abstract methods <code>{@link #assignNumberAssociations()}</code> and <code>{@link #assignDurationAssociatios()}</code>.
 * See class documentation for base class <code>{@link TestResultExtremesBase}</code> for further details.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 6, 2026
 *
 */
public class IngestApiResultExtremes extends TestResultExtremesBase<IngestApiTestParams, IngestApiTestResult> {

    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>IngestApiResultExtremes</code> instance from the given results collection.
     * </p>
     * <p>
     * The returned test result summary is fully analyzed and ready for print out with method
     * <code>{@link #printOut(java.io.PrintStream, String)}</code>.
     * </p>
     * 
     * @param conResults    collection of the test results
     * 
     * @throws IllegalArgumentException the argument collection was empty
     * @throws MissingResourceException no <code>TestResult</code> fields were identified for analysis
     * @throws NoSuchElementException   no successful results were contained in the argument collection
     */
    public static IngestApiResultExtremes   from(Collection<IngestApiTestResult> conResults) 
            throws IllegalArgumentException, MissingResourceException, NoSuchElementException {

        return new IngestApiResultExtremes(conResults);
    }
    
    //
    // Class Constants
    //
    
    /** Data transmission rate test result field description */
    public static final String      STR_RATE_XMIT = "Data rate (MBps)";
    
    /** Data transmission duration test result field description */
    public static final String      STR_DUR_XMIT = "Data processing/transmission duration";
    
    
    //
    // TestResultExtremesBase Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultExtremesBase#assignFailedResult()
     */
    @Override
    protected Function<IngestApiTestResult, Boolean> assignFailedResult() {
        return rec -> rec.recTestStatus().isFailure();
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultExtremesBase#assignNumberAssociations()
     */
    @Override
    protected List<ParamToNumField<IngestApiTestParams, IngestApiTestResult>> assignNumberAssociations() {
        List<ParamToNumField<IngestApiTestParams, IngestApiTestResult>>   lstAssocs = List.of(
                ParamToNumField.from(IngestApiTestParams.INGEST_API, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().enmApiType(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestApiTestParams.COL_SER_ENBL, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().recProcCfg().bolColSerEnbl(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestApiTestParams.DCMP_ENABLE, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().recProcCfg().bolDcmpEnbl(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestApiTestParams.DCMP_SIZE, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().recProcCfg().szDcmpMax(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestApiTestParams.MTHREAD_ENABLE, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().recProcCfg().bolMThrdEnbl(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestApiTestParams.MTHREAD_COUNT, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().recProcCfg().cntMThrdMax(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestApiTestParams.STREAM_TYPE, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().recChanCfg().enmStrmType(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestApiTestParams.MSTREAM_ENBL, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().recChanCfg().bolMStrmEnbl(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestApiTestParams.MSTREAM_CNT, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().recChanCfg().cntMStrmMax(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestApiTestParams.FRAME_CNT, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().cntFrames(), rec -> rec.dblRateXmit()),
                ParamToNumField.from(IngestApiTestParams.FRAME_DEF, STR_RATE_XMIT, NumberType.DOUBLE, rec -> rec.recTestCase().specFrame().strLabel(), rec -> rec.dblRateXmit())
                );
        
        return lstAssocs;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestResultExtremesBase#assignDurationAssociatios()
     */
    @Override
    protected List<ParamToDurField<IngestApiTestParams, IngestApiTestResult>> assignDurationAssociatios() {
        List<ParamToDurField<IngestApiTestParams, IngestApiTestResult>>   lstAssocs = List.of(
                ParamToDurField.from(IngestApiTestParams.COL_SER_ENBL, STR_DUR_XMIT, rec -> rec.recTestCase().recProcCfg().bolColSerEnbl(), rec -> rec.durTransmit()),
                ParamToDurField.from(IngestApiTestParams.STREAM_TYPE, STR_DUR_XMIT, rec -> rec.recTestCase().recChanCfg().enmStrmType(), rec -> rec.durTransmit()),
                ParamToDurField.from(IngestApiTestParams.MSTREAM_ENBL, STR_DUR_XMIT, rec -> rec.recTestCase().recChanCfg().bolMStrmEnbl(), rec -> rec.durTransmit()),
                ParamToDurField.from(IngestApiTestParams.MSTREAM_CNT, STR_DUR_XMIT, rec -> rec.recTestCase().recChanCfg().cntMStrmMax(), rec -> rec.durTransmit()),
                ParamToDurField.from(IngestApiTestParams.FRAME_DEF, STR_DUR_XMIT, rec -> rec.recTestCase().specFrame().strLabel(), rec -> rec.durTransmit())
                );
        
        return lstAssocs;
    }

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>IngestApiResultExtremes</code> instance.
     * </p>
     *
     * @param conResults    collection of the test results
     * 
     * @throws IllegalArgumentException the argument collection was empty
     * @throws MissingResourceException no <code>TestResult</code> fields were identified for analysis
     * @throws NoSuchElementException   no successful results were contained in the argument collection
     */
    private IngestApiResultExtremes(Collection<IngestApiTestResult> conResults)
            throws IllegalArgumentException, MissingResourceException, NoSuchElementException {
        super(IngestApiTestParams.class, conResults);
    }

}
