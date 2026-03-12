/*
 * Project: dp-jal
 * File:	IngestApiConfigScorer.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.api
 * Type: 	IngestApiConfigScorer
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
 * @since Mar 9, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.api;

import java.io.PrintStream;
import java.util.Collection;

import com.ospreydcs.dp.jal.common.DpGrpcStreamType;
import com.ospreydcs.dp.jal.tools.apps.ingest.common.FrameProcessorConfig;
import com.ospreydcs.dp.jal.tools.apps.ingest.common.IngestionChannelConfig;
import com.ospreydcs.dp.jal.tools.apps.ingest.common.JalIngestionApiType;
import com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase;
import com.ospreydcs.dp.jal.tools.common.score.ConfigScorerBase;

/**
 * <p>
 * Configuration scorer class for <code>IngestApiTestResult</code> records.
 * </p>
 * <p>
 * Scores are based upon <code>IngestAiTestCase</code> test conditions specifically identified
 * in the enclosed record <code>{@link TestConfig}</code>, whose field contain the configuration
 * parameters being scored. 
 * The <code>TestConfig</code> record contains the configuration parameters for a 
 * <code>IngestionChannel</code> instance used to transmit ingestion data, and some properties of the
 * <code>IngestAiTestResult</code> (specifically <code>{@link TestConfig#szAllocXmit()}</code>.
 * </p>
 * <p>
 * Configuration score are managed by the enclosed class <code>{@link ConfigScore}</code>, which accumulates all
 * scores from collections of <code>{@link IngestAiTestResult}</code> collections and performs all the required
 * computations.
 * </p>
 * <p>
 * <h2>Creation</h2>
 * Use <code>{@link #from(Collection)}</code> to create a new, fully configured <code>IngestChanConfigScorer</code>
 * instance ready for output using methods <code>{@link #printOutByHits(PrintStream, String)}</code> or
 * <code>{@link #printOutByRates(PrintStream, String)}</code>.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Mar 9, 2026
 *
 */
public class IngestApiConfigScorer extends ConfigScorerBase<
                com.ospreydcs.dp.jal.tools.apps.ingest.api.IngestApiConfigScorer.TestConfig, 
                IngestApiTestResult, 
                com.ospreydcs.dp.jal.tools.apps.ingest.api.IngestApiConfigScorer.ConfigScore> 
{
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Creates a new, populated <code>IngestApiConfigScorer</code> instance.
     * </p>
     * <p>
     * This creator returns a fully scored <code>IngestChanConfigScorer</code> instance for the given
     * collection of <code>IngestApiTestResult</code> test results.
     * That is, the returned object contains all the scoring information for the given argument collection.
     * </p>
     * <p>
     * Note that additional test results can be added to the score using the base class methods
     * <code>{@link #score(IngestApiTestResult)}</code> and <code>{@link #score(Collection)}</code>.
     * </p>
     * 
     * @param conResults    the collection of test results to be scored
     * 
     * @return  a new <code>IngestApiConfigScorer</code> instance which scores the given argument collection
     */
    public static IngestApiConfigScorer from(Collection<IngestApiTestResult> conResults) {
        return new IngestApiConfigScorer(conResults);
    }
    
    //
    // Internal Type
    //
    
    /**
     * <p>
     * Configuration record for the <code>ConfigScore</code> class.
     * </p>
     * <p>
     * This record contains the subset of test parameters within <code>{@link IngestApiTestCase}</code>
     * that are evaluated in the scoring.  The fields of this record are used in the scoring
     * within <code>{@link ConfigScore}</code>.
     * </p>
     *
     * @param enmApiType    the JAL Ingestion Service API type
     * @param recProcCfg    the ingetion frame processor configuration
     * @param recChanCfg    the ingestion channel configuration
     */
    public static record TestConfig(
            JalIngestionApiType     enmApiType,
            FrameProcessorConfig    recProcCfg,
            IngestionChannelConfig  recChanCfg
            ) 
    {
        
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates and returns a new <code>TestConfig</code> instance with fields populated from the given arguments.
         * </p>
         * <p>
         * The internal configuration field records <code>{@link #recProcCfg()}</code> and <code>{@link #recChanCfg()}</code>
         * are created from the explicit argument values then used in creator 
         * <code>{@link #from(JalIngestionApiType, FrameProcessorConfig, IngestionChannelConfig)}</code>.
         * </p>
         * 
         * @param enmApiType    the JAL Ingestion Service API type
         * @param bolColSerEnbl enable/disable data column serialization in frame processing
         * @param bolDcmpEnbl   enable/disable frame decomposition in frame processing
         * @param szDcmpMax     maximum composite frame size in frame processing
         * @param bolMThrdEnbl  enable/disable multi-threading in frame processing
         * @param cntMThrdMax   maximum number of concurrent processing threads in frame processing
         * @param enmStrmType   the gRPC data stream type used in data transmission {FORWARD, BIDIRECTIONAL} 
         * @param bolMStrmEnbl  enable/disable multiple, concurrent gRPC data streams
         * @param cntMStrmMax   maximum number of concurrent gRPC data streams
         * 
         * @return  a new <code>TestConfig</code> instance with fields populated from the given argument values
         */
        public static TestConfig    from(
                JalIngestionApiType     enmApiType,
                
                boolean                 bolColSerEnbl,
                boolean                 bolDcmpEnbl,
                long                    szDcmpMax,
                boolean                 bolMThrdEnbl,
                int                     cntMThrdMax,
                
                DpGrpcStreamType        enmStrmType,
                boolean                 bolMStrmEnbl,
                int                     cntMStrmMax
                )
        {
            // Create internal records
            FrameProcessorConfig        recProcCfg = FrameProcessorConfig.from(bolColSerEnbl, bolDcmpEnbl, szDcmpMax, bolMThrdEnbl, cntMThrdMax);
            IngestionChannelConfig      recChanCfg = IngestionChannelConfig.from(enmStrmType, bolMStrmEnbl, cntMStrmMax);
            
            // Create test configuration and return
            TestConfig      recCfg = TestConfig.from(enmApiType, recProcCfg, recChanCfg);
            
            return recCfg;
        }
        
        /**
         * <p>
         * Creates and returns a new <code>TestConfig</code> instance with field populated from the given test case.
         * </p>
         * <p>
         * This is the primary constructor used by 
         * <code>{@link IngestApiConfigScorer#extractConfiguration(IngestApiTestResult)}</code>.
         * </p>
         * 
         * @param recTestCase   the test case from which the configuration is derived
         * 
         * @return  the configuration extracted from the given test case
         */
        public static TestConfig    from(IngestApiTestCase recTestCase) {
            return TestConfig.from(recTestCase.enmApiType(), recTestCase.recProcCfg(), recTestCase.recChanCfg());
        }
        
        /**
         * <p>
         * Creates and returns a new <code>TestConfig</code> instance populated with the given argument values.
         * </p>
         * <p>
         * This creator is equivalent to the canonical constructor.
         * </p>
         * 
         * @param enmApiType    the JAL Ingestion Service API type
         * @param recProcCfg    the ingetion frame processor configuration
         * @param recChanCfg    the ingestion channel configuration
         * 
         * @return  a new <code>TestConfig</code> instance with fields populated from the given arguments
         */
        public static TestConfig    from(JalIngestionApiType enmApiType, FrameProcessorConfig recProcCfg, IngestionChannelConfig recChanCfg) {
            return new TestConfig(enmApiType, recProcCfg, recChanCfg);
        }
        
        
        //
        //  Operations 
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
            String  strPadd = strPad + "  ";
            
            ps.println(strPad + "JAL Ingestion Service API : " + this.enmApiType);
            ps.println(strPad + "IngestionFrameProcessor Configuration");
            this.recProcCfg.printOut(ps, strPadd);
            ps.println(strPad + "IngestionChannel Configuration");
            this.recChanCfg.printOut(ps, strPadd);
        }
        

        //
        // Record Overrides
        //

        /**
         * <p>
         * Provides equivalence of <code>TestConfig</code> record ignore field <code>{@link #szAllocXmit}</code>.
         * </p>
         * 
         * @see java.lang.Record#equals(java.lang.Object)
         */
        @Override
        public boolean  equals(Object obj) {
            if (obj instanceof TestConfig rec) {
                boolean bolResult = (this.enmApiType == rec.enmApiType)
                                 && (this.recProcCfg.equals(rec.recProcCfg))
                                 && (this.recChanCfg.equals(rec.recChanCfg));
                
                return bolResult;
            }
            
            return false;
        }
    }
    
    
    /**
     * <p>
     * Class managing the scoring for a <code>TestConfig</code> configuration record.
     * </p>
     * <p>
     * Class that maintains a score for the test conditions of the test results. 
     * Scoring for <code>{@link IngestApiTestResult}</code> instances are accumulated using the 
     * <code>{@link #addInResult(IngestApiTestResult)}</code> method where score are managed on the fly.
     * Class instances extract the the configuration <code>{@link TestConfig}</code> configuration 
     * from <code>IngestApiTestResult</code> records and compute the scoring for that result.
     * </p>
     *
     */
    public static class ConfigScore extends ConfigScoreBase<TestConfig, IngestApiTestResult> {

        
        //
        // Creator
        //
        
        /**
         * <p>
         * Creates and returns a new <code>ConfigScore</code> instance for the given configuration.
         * </p>
         * 
         * @param recCfg    configuration associated with the returned <code>ConfigScore</code> instance
         * 
         * @return  a new <code>ConfigScore</code> instance for the given configuration
         */
        public static ConfigScore   from(TestConfig recCfg) {
            return new ConfigScore(recCfg);
        }
        
        
        //
        // ConfigScoreBase Abstract Methods
        //
        
        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#extractConfiguration(java.lang.Record)
         */
        @Override
        protected TestConfig extractConfiguration(IngestApiTestResult recResult) {
            IngestApiTestCase   recCase = recResult.recTestCase();
            TestConfig          recCfg = TestConfig.from(recCase.enmApiType(), recCase.recProcCfg(), recCase.recChanCfg());
            
            return recCfg;
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#isSuccess(java.lang.Record)
         */
        @Override
        protected boolean isSuccess(IngestApiTestResult recResult) {
            return recResult.recTestStatus().isSuccess();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#extractDataSourceId(java.lang.Record)
         */
        @Override
        protected String extractDataSourceId(IngestApiTestResult recResult) {
            return recResult.recTestCase().specFrame().strLabel();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#extractDataRate(java.lang.Record)
         */
        @Override
        protected double extractDataRate(IngestApiTestResult recResult) {
            return recResult.dblRateXmit();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#printOutConfiguration(java.io.PrintStream, java.lang.String, java.lang.Record)
         */
        @Override
        protected void printOutConfiguration(PrintStream ps, String strPad, TestConfig recConfig) {

            recConfig.printOut(ps, strPad);
        }
        

        //
        // ConfigScoreBase Overrides
        //
        
        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#addInResult(java.lang.Record)
         */
        @Override
        public double addInResult(IngestApiTestResult recResult) throws IllegalArgumentException {

            // Process the super class state variables
            double  dblDataRate = super.addInResult(recResult);    // throws IllegalArgumentException
            
            // Get the current hit count and de-normalize the local state variables
            int     N = super.getHitCount() - 1;
            
            if (N > 0) {
                this.szPayloadAvg *= N;
                this.szPayloadSqrd *= N;
                this.cntMStrmsAvg *= N;
                this.cntMStrmsSqrd *= N;
            }
            
            // Add in the appropriate test result values
            this.szPayloadAvg += recResult.szPayload();
            this.szPayloadSqrd += recResult.szPayload() * recResult.szPayload();
            this.cntMStrmsAvg += recResult.recTestCase().recChanCfg().cntMStrmMax();
            this.cntMStrmsSqrd += recResult.recTestCase().recChanCfg().cntMStrmMax() * recResult.recTestCase().recChanCfg().cntMStrmMax();
            
            // Re-normalize the average values
            N = N + 1;

            this.szPayloadAvg /= N;
            this.szPayloadSqrd /= N;
            this.cntMStrmsAvg /= N;
            this.cntMStrmsSqrd /= N;
            
            return dblDataRate;
        }
        
        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#printOut(java.io.PrintStream, java.lang.String)
         */
        @Override
        public void printOut(PrintStream ps, String strPad) {
            strPad = (strPad == null) ? "" : strPad;
            String strPadd = strPad + "  ";
            
            super.printOut(ps, strPad);
            ps.println(strPad + "Test Results Properties");
            ps.println(strPadd + "Payload data allocation avg. (MBytes) : " + this.getPayloadAllocationAvg());
            ps.println(strPadd + "Payload data allocation std. (MBytes) : " + this.payloadAllocationStd());
            ps.println(strPadd + "Maximum concurrent data streams avg.  : " + this.getMaximDataStreamsAvg());
            ps.println(strPadd + "Maximum concurrent data streams std.  : " + this.maximumDataStreamsStd());
        }
        
        
        //
        // State Variables
        //
        
        /** The processed data set allocation size average (bytes) */
        private double  szPayloadAvg = 0.0;
        
        /** The processed data set allocation size squared average (bytes^2) (for standard deviation calculation) */
        private double  szPayloadSqrd = 0.0;

        /** The processed data message count average */
        private double  cntMStrmsAvg = 0.0;
        
        /** The processed data message count squared average (for standard deviation calculation) */
        private double  cntMStrmsSqrd = 0.0;
        
        
        //
        // Constructor
        //
        
        /**
         * <p>
         * Constructs a new <code>ConfigScore</code> instance.
         * </p>
         *
         * @param recCfg    the test configuration associated with this score
         */
        protected ConfigScore(TestConfig recCfg) {
            super(recCfg);
        }
        

        //
        // State Inquiry
        //
        
        /**
         * @return the maximum gRPC data stream count average 
         */
        public final double getMaximDataStreamsAvg() {
            return this.cntMStrmsAvg;
        }
        
        /**
         * <p>
         * Computes and returns the standard deviation for the returned data message count.
         * </p>
         * 
         * @return  the returned data message count standard deviation 
         */
        public final double maximumDataStreamsStd() {
            double  std = Math.sqrt(cntMStrmsSqrd - this.cntMStrmsAvg*this.cntMStrmsAvg);
            
            return std;
        }

        /**
         * @return the payload memory allocation size average (MBytes) 
         */
        public final double getPayloadAllocationAvg() {
            return this.szPayloadAvg/1.0e6;
        }
        
        /**
         * <p>
         * Computes and returns the standard deviation of the payload memory allocation size.
         * </p>
         * 
         * @return  the transmitted memory allocation size standard deviation (MBytes)
         */
        public final double payloadAllocationStd() {
            double  std = Math.sqrt(this.szPayloadSqrd - this.szPayloadAvg*this.szPayloadAvg);
            
            return std/1.0e6;
        }
        
    }

    
    //
    // ConfigScorerBase Abstract Methods
    //

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScorerBase#extractConfiguration(java.lang.Record)
     */
    @Override
    protected TestConfig extractConfiguration(IngestApiTestResult recResult) {
        IngestApiTestCase       recCase = recResult.recTestCase();
        
        return TestConfig.from(recCase);
    }


    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScorerBase#newScore(java.lang.Record)
     */
    @Override
    protected ConfigScore newScore(TestConfig recConfig) {
        return ConfigScore.from(recConfig);
    }

    
    //
    // IngestApiConfigScorer Constructors
    //
    
    /**
     * <p>
     * Constructs a new, empty <code>IngestApiConfigScorer</code> instance.
     * </p>
     * <p>
     * This constructor is hidden and not available.
     * </p>
     */
    private IngestApiConfigScorer() {
        super();
    }
    
    /**
     * <p>
     * Constructs a new, fully initialized <code>IngestApiConfigScorer</code> instance.
     * </p>
     * <p>
     * This constructor is available for public creator <code>{@link #from(Collection)}</code>.
     * </p> 
     *
     * @param conResults    the collection of test results whose configurations are to be scored
     */
    private IngestApiConfigScorer(Collection<IngestApiTestResult> conResults) {
        super(conResults);
    }

}
