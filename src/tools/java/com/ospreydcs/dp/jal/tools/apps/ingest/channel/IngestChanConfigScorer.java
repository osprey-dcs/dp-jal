/*
 * Project: dp-jal
 * File:	IngestChanConfigScorer.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.channel
 * Type: 	IngestChanConfigScorer
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

import java.io.PrintStream;
import java.util.Collection;

import com.ospreydcs.dp.jal.common.DpGrpcStreamType;
import com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase;
import com.ospreydcs.dp.jal.tools.common.score.ConfigScorerBase;

/**
 * <p>
 * Configuration scorer class for <code>IngestChanTestResult</code> records.
 * </p>
 * <p>
 * Scores are based upon <code>IngestChanTestCase</code> test conditions specifically identified
 * in the enclosed record <code>{@link TestConfig}</code>, whose field contain the configuration
 * parameters being scored. 
 * The <code>TestConfig</code> record contains the configuration parameters for a 
 * <code>IngestionChannel</code> instance used to transmit ingestion data, and some properties of the
 * <code>IngestChanTestResult</code> (specifically <code>{@link TestConfig#szAllocXmit()}</code>.
 * </p>
 * <p>
 * Configuration score are managed by the enclosed class <code>{@link ConfigScore}</code>, which accumulates all
 * scores from collections of <code>{@link IngestChanTestResult}</code> collections and performs all the required
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
 * @since Feb 23, 2026
 *
 */
public class IngestChanConfigScorer extends 
        ConfigScorerBase<com.ospreydcs.dp.jal.tools.apps.ingest.channel.IngestChanConfigScorer.TestConfig, 
                         IngestChanTestResult, 
                         com.ospreydcs.dp.jal.tools.apps.ingest.channel.IngestChanConfigScorer.ConfigScore> 
{
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new, populated <code>IngestChaneConfigScorer</code> instance.
     * </p>
     * <p>
     * This creator returns a fully scored <code>IngestChanConfigScorer</code> instance for the given
     * collection of <code>IngestChanTestResult</code> test results.
     * That is, the returned object contains all the scoring information for the given argument collection.
     * </p>
     * <p>
     * Note that additional test results can be added to the score using the base class methods
     * <code>{@link #score(IngestChanTestResult)}</code> and <code>{@link #score(Collection)}</code>.
     * </p>
     * 
     * @param conResults    the collection of test results to be scored
     * 
     * @return  a new <code>IngestChanConfigScorer</code> instance which scores the given argument collection
     */
    public static IngestChanConfigScorer    from(Collection<IngestChanTestResult> conResults) {
        return new IngestChanConfigScorer(conResults);
    }

    
    //
    // Internal Types
    //
    
    /**
     * <p>
     * Configuration record for the <code>ConfigScore</code> class.
     * </p>
     * <p>
     * This record contains the subset of test parameters within <code>{@link IngestChanTestCase}</code>
     * that are evaluated in the scoring.  The fields of this record are used in the scoring
     * within <code>{@link ConfigScore}</code>.
     * </p>
     *
     * @param bolColSerEnbl enable/disable data column serialization before transmission
     * @param enmStrmType   the gRPC stream type used for transmission (FORWARD, BIDIRECTIONAL)
     * @param bolMStrmEnbl  enable/disable use of multiple, concurrent gRPC data streams for transmission
     * @param cntMStrmMax   maximum number of concurrent gRPC data streams used for transmission
     * @param szAllocXmit   the memory allocation size (bytes) of transmitted data
     */
    public static record TestConfig(
            boolean             bolColSerEnbl,
            DpGrpcStreamType    enmStrmType,
            boolean             bolMStrmEnbl,
            int                 cntMStrmMax,
            long                szAllocXmit
            ) 
    {
        
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates and returns a new <code>TestConfig</code> instance populated from fields of the given argument.
         * </p>
         * <p>
         * Extracts the test case configuration fields of the given <code>{@link IngestChanTestCase}</code> argument
         * that are relevant to <code>{@link TestConfig}</code> and uses them to populated the returned record.
         * </p>
         *  
         * @param recResult   the test case result record containing the configuration parameters
         * 
         * @return  a new <code>TestConfig</code> record populated from the given test case
         */
        public static TestConfig    from(IngestChanTestResult recResult) {
            boolean             bolColSerEnbl = recResult.recTestCase().recProcCfg().bolColSerEnbl();
            DpGrpcStreamType    enmStrmType = recResult.recTestCase().recChanCfg().enmStrmType();
            boolean             bolMStrmEnbl = recResult.recTestCase().recChanCfg().bolMStrmEnbl();
            int                 cntMStrmMax = recResult.recTestCase().recChanCfg().cntMaxStrms();
            long                szAllocXmit = recResult.szAllocXmit();
            
            return TestConfig.from(bolColSerEnbl, enmStrmType, bolMStrmEnbl, cntMStrmMax, szAllocXmit);
        }
        
        /**
         * <p>
         * Creates and returns a new <code>TestConfig</code> instance populated with the argument values.
         * </p>
         * <p>
         * This creator is equivalent to the canonical constructor.
         * </p>
         * 
         * @param bolColSerEnbl enable/disable data column serialization before transmission
         * @param enmStrmType   the gRPC stream type used for transmission (FORWARD, BIDIRECTIONAL)
         * @param bolMStrmEnbl  enable/disable use of multiple, concurrent gRPC data streams for transmission
         * @param cntMStrmMax   maximum number of concurrent gRPC data streams used for transmission
         * @param szAllocXmit   the memory allocation size (bytes) of transmitted data
         * 
         * @return  a new <code>TestConfig</code> instance populated with the given arguments
         */
        public static TestConfig    from(boolean bolColSerEnbl, DpGrpcStreamType enmStrmType, boolean bolMStrmEnbl, int cntMStrmMax, long szAllocXmit) {
            return new TestConfig(bolColSerEnbl, enmStrmType, bolMStrmEnbl, cntMStrmMax, szAllocXmit);
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
            
            ps.println(strPad + "Data column serialization enabled   : " + this.bolColSerEnbl);
            ps.println(strPad + "gRPC data stream type               : " + this.enmStrmType);
            ps.println(strPad + "Concurrent data streams enabled     : " + this.bolMStrmEnbl);
            ps.println(strPad + "Maximum concurrent data streams     : " + this.cntMStrmMax);
            ps.println(strPad + "Transmitted data allocation (bytes) : " + this.szAllocXmit);
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
                boolean bolResult = (this.bolColSerEnbl == rec.bolColSerEnbl)
                                 && (this.enmStrmType == rec.enmStrmType)
                                 && (this.bolMStrmEnbl == rec.bolMStrmEnbl)
                                 && (this.cntMStrmMax == rec.cntMStrmMax);
                
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
     * Scoring for <code>{@link IngestChanTestResult}</code> instances are accumulated using the 
     * <code>{@link #addInResult(IngestChanTestResult)}</code> method where score are managed on the fly.
     * Class instances extract the the configuration <code>{@link TestConfig}</code> configuration 
     * from <code>IngestChanTestResult</code> records and compute the scoring for that result.
     * </p>
     *
     */
    public static class ConfigScore extends ConfigScoreBase<TestConfig, IngestChanTestResult> {

        
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
        public static ConfigScore from(TestConfig recCfg) {
            return new ConfigScore(recCfg);
        }

        
        //
        // ConfigScoreBase Abstract Methods
        //
        
        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#extractConfiguration(java.lang.Record)
         */
        @Override
        protected TestConfig extractConfiguration(IngestChanTestResult recResult) {
            return TestConfig.from(recResult);
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#isSuccess(java.lang.Record)
         */
        @Override
        protected boolean isSuccess(IngestChanTestResult recResult) {
            return recResult.recTestStatus().isSuccess();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#extractDataRequest(java.lang.Record)
         */
        @Override
        protected String extractDataRequest(IngestChanTestResult recResult) {
            return recResult.recTestCase().specFrame().strLabel();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#extractDataRate(java.lang.Record)
         */
        @Override
        protected double extractDataRate(IngestChanTestResult recResult) {
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
        public double addInResult(IngestChanTestResult recResult) throws IllegalArgumentException {

            // Process the super class state variables
            double  dblDataRate = super.addInResult(recResult);    // throws IllegalArgumentException
            
            // Get the current hit count and de-normalize the local state variables
            int     N = super.getHitCount() - 1;
            
            if (N > 0) {
                this.cntMStrmsAvg *= N;
                this.cntMStrmsSqrd *= N;
                this.szAllocXmitAvg *= N;
                this.szAllocXmitSqrd *= N;
            }
            
            // Add in the appropriate test result values
            this.cntMStrmsAvg += recResult.recTestCase().recChanCfg().cntMaxStrms();
            this.cntMStrmsSqrd += recResult.recTestCase().recChanCfg().cntMaxStrms() * recResult.recTestCase().recChanCfg().cntMaxStrms();
            this.szAllocXmitAvg += recResult.szAllocXmit();
            this.szAllocXmitSqrd += recResult.szAllocXmit() * recResult.szAllocXmit();
            
            // Re-normalize the average values
            N = N + 1;

            this.cntMStrmsAvg /= N;
            this.cntMStrmsSqrd /= N;
            this.szAllocXmitAvg /= N;
            this.szAllocXmitSqrd /= N;
            
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
            ps.println(strPadd + "Maximum concurrent data streams avg.      : " + this.getMaximDataStreamsAvg());
            ps.println(strPadd + "Maximum concurrent data streams std.      : " + this.maximumDataStreamsStd());
            ps.println(strPadd + "Transmitted data allocation avg. (MBytes) : " + this.getTransmittedAllocationAvg());
            ps.println(strPadd + "Transmitted data allocation std. (MBytes) : " + this.transmittedAllocationStd());
        }
        
        
        //
        // State Variables
        //
        
        /** The processed data message count average */
        private double  cntMStrmsAvg = 0.0;
        
        /** The processed data message count squared average (for standard deviation calculation) */
        private double  cntMStrmsSqrd = 0.0;
        
        /** The processed data set allocation size average (bytes) */
        private double  szAllocXmitAvg = 0.0;
        
        /** The processed data set allocation size squared average (bytes^2) (for standard deviation calculation) */
        private double  szAllocXmitSqrd = 0.0;
        
            
        //
        // Constructor
        //
        
        /**
         * <p>
         * Constructs a new <code>ConfigScore</code> instance for the given configuration.
         * </p>
         *
         * @param recCfg    configuration associated with this score
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
         * @return the transmitted memory allocation size average (MBytes) 
         */
        public final double getTransmittedAllocationAvg() {
            return this.szAllocXmitAvg/1.0e6;
        }
        
        /**
         * <p>
         * Computes and returns the standard deviation of the transmitted memory allocation size.
         * </p>
         * 
         * @return  the transmitted memory allocation size standard deviation (MBytes)
         */
        public final double transmittedAllocationStd() {
            double  std = Math.sqrt(this.szAllocXmitSqrd - this.szAllocXmitAvg*this.szAllocXmitAvg);
            
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
    protected TestConfig extractConfiguration(IngestChanTestResult recResult) {
        return TestConfig.from(recResult);
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScorerBase#newScore(java.lang.Record)
     */
    @Override
    protected ConfigScore newScore(TestConfig recConfig) {
        return ConfigScore.from(recConfig);
    }
    
    
    //
    // IngestChanConfigScorer Constructors
    //
    
    /**
     * <p>
     * Constructs a new, empty <code>IngestChanConfigScorer</code> instance.
     * </p>
     * <p>
     * This constructor is hidden and not available.
     * </p>
     */
    private IngestChanConfigScorer() {
        super();
    }
    
    /**
     * <p>
     * Constructs a new, fully initialized <code>IngestChanConfigScorer</code> instance.
     * </p>
     * <p>
     * This constructor is available for public creator <code>{@link #from(Collection)}</code>.
     * </p> 
     *
     * @param conResults    the collection of test results whose configurations are to be scored
     */
    private IngestChanConfigScorer(Collection<IngestChanTestResult> conResults) {
        super(conResults);
    }

}
