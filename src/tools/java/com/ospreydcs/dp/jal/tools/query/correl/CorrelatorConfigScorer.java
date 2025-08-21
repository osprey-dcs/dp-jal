/*
 * Project: dp-api-common
 * File:	CorrelatorConfigScorer.java
 * Package: com.ospreydcs.dp.jal.tools.query.correl
 * Type: 	CorrelatorConfigScorer
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
 * @since Aug 20, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.correl;

import java.io.PrintStream;
import java.util.Collection;

import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.jal.tools.common.ConfigScoreBase;
import com.ospreydcs.dp.jal.tools.common.ConfigScorerBase;

/**
 * <p>
 * Configuration scorer class for <code>CorrelatorTestResult</code> records.
 * </p>
 * <p>
 * Scores are based upon <code>CorrelatorTestCase</code> test conditions specifically identified
 * in the <code>CorrelatorConfig</code> record.
 * The <code>CorrelatorConfig</code> record contains the configuration parameters for a 
 * <code>RawDataCorrelator</code> instance used to correlate raw time-series data recovered
 * from a time-series data request.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Aug 20, 2025
 *
 */
public class CorrelatorConfigScorer extends 
            ConfigScorerBase<CorrelatorConfig, 
                             CorrelatorTestResult, 
                             com.ospreydcs.dp.jal.tools.query.correl.CorrelatorConfigScorer.Score> {

    
    //
    // CorrelationConfigScorer Creators
    //
    
    /**
     * <p>
     * Creates a new, empty <code>CorrelatorConfigScorer</code> instance ready for test configuration scoring.
     * </p>
     * <p>
     * Creates and returns an empty <code>CorrelatorConfigScorer</code> instance ready for scoring the
     * <code>QueryRecoveryTestResult</code> test results against the configuration of the 
     * <code>RequestDecompConfig</code> processor configuration used for test conditions.
     * </p>
     * <p>
     * Use the <code>score(...)</code> methods to add <code>QueryRecoveryTestResult</code> records generated from
     * <code>{@link CorrelatorTestCase#evaluate(com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator, java.util.List)}</code>
     * test evaluations.
     * </p>
     * 
     * @return  a new <code>CorrelatorConfigScorer</code> instance ready for use
     */
    public static CorrelatorConfigScorer  create() {
        return new CorrelatorConfigScorer();
    }
    
    /**
     * <p>
     * Creates a new, populated <code>CorrelatorConfigScorer</code> instance.
     * </p>
     * <p>
     * This is a convenience creator which is a combination of the following operations:
     * <ol>
     * <li><code>{@link #create()}</code>
     * <li><code>{@link #score(Collection)}</code>
     * </ol>
     * Thus, the returned object contains all the scoring information for the given argument collection.
     * </p>
     * 
     * @param setResults    the collection of test results to be scored
     * 
     * @return  a new <code>CorrelatorConfigScorer</code> instance which scores the given argument collection
     */
    public static CorrelatorConfigScorer   from(Collection<CorrelatorTestResult> setResults) {
        CorrelatorConfigScorer    scorer = CorrelatorConfigScorer.create();
        
        scorer.score(setResults);
        
        return scorer;
    }

    
    //
    // Internal Types
    //
    
    /**
     * <p>
     * Class maintaining the scoring for a <code>CorrelatorConfig</code> configuration record.
     * </p>
     * <p>
     * Class instances extract the the scoring from <code>CorrelatorTestResult</code> records
     * to maintain a score for the test conditions for the result.
     * </p>
     *
     * @author Christopher K. Allen
     * @since Aug 19, 2025
     *
     */
    public static class Score extends ConfigScoreBase<CorrelatorConfig, CorrelatorTestResult> {

        
        //
        // Creator
        //
        
        /**
         * <p>
         * Creates and returns a new <code>Score</code> instance for the given configuration.
         * </p>
         * 
         * @param recCfg    configuration associated with the returned <code>Score</code> instance
         * 
         * @return  a new <code>Score</code> instance for the given configuration
         */
        public static Score from(CorrelatorConfig recCfg) {
            return new Score(recCfg);
        }
        
        
        //
        // ConfigueScoreBase Abstract Methods
        //
        
        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#extractConfiguration(java.lang.Record)
         */
        @Override
        protected CorrelatorConfig extractConfiguration(CorrelatorTestResult recResult) {
            return CorrelatorConfig.from(recResult);
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#isSuccess(java.lang.Record)
         */
        @Override
        protected boolean isSuccess(CorrelatorTestResult recResult) {
            return recResult.recTestStatus().isSuccess();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#extractDataRequest(java.lang.Record)
         */
        @Override
        protected DpDataRequest extractDataRequest(CorrelatorTestResult recResult) {
            return recResult.recTestCase().rqstOrg();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#extractDataRate(java.lang.Record)
         */
        @Override
        protected double extractDataRate(CorrelatorTestResult recResult) {
            return recResult.dblDataRate();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#printOutConfiguration(java.io.PrintStream, java.lang.String, java.lang.Record)
         */
        @Override
        protected void printOutConfiguration(PrintStream ps, String strPad, CorrelatorConfig recConfig) {
            recConfig.printOut(ps, strPad);
        }
        
        
        //
        // State Variables
        //
        
        /** The returned data message count average */
        private double  cntRspMsgsAvg = 0.0;
        
        /** The returned data message count squared average (for standard deviation calculation) */
        private double  cntRspMsgsSqrd = 0.0;
        
        /** The returned allocation size average (bytes) */
        private double  szRspMsgsAvg = 0.0;
        
        /** The returned allocation size squared average (for standard deviation calculation) */
        private double  szRspMsgsSqrd = 0.0;
        
        /** The raw correlated data set size average (blocks) */
        private double  cntCorrelSetAvg = 0.0;
        
        /** The raw correlated data set size squared average (blocks^2) (for standard deviation calculation) */
        private double  cntCorrelSetSqrd = 0.0;
        
        /** The processed data set allocation size average (bytes) */
        private double  szProcessedAvg = 0.0;
        
        /** The processed data set allocation size squared average (bytes^2) (for standard deviation calculation) */
        private double  szProcessedSqrd = 0.0;
        
        
        //
        // Constructor
        //
        
        /**
         * <p>
         * Constructs a new <code>Score</code> instance for the given configuration.
         * </p>
         *
         * @param recCfg    configuration associated with this score
         */
        protected Score(CorrelatorConfig recCfg) {
            super(recCfg);
        }


        //
        // State Inquiry
        //

        /**
         * @return the returned data message count average 
         */
        public final double getResponseMessageCountAvg() {
            return this.cntRspMsgsAvg;
        }
        
        /**
         * <p>
         * Computes and returns the standard deviation for the returned data message count.
         * </p>
         * 
         * @return  the returned data message count standard deviation 
         */
        public final double responseMessageCountStd() {
            double  std = Math.sqrt(cntRspMsgsSqrd - this.cntRspMsgsAvg*this.cntRspMsgsAvg);
            
            return std;
        }

        /**
         * @return the returned allocation size average (Mbytes)
         */
        public final double getResponseAllocationAvg() {
            return this.szRspMsgsAvg/1.0e6;
        }
        
        /**
         * <p>
         * Computes and returns the standard deviation of the returned allocation size.
         * </p>
         * 
         * @return  returned memory allocation size standard deviation (in MBytes)
         */
        public final double responseAllocationStd() {
            double  std = Math.sqrt(this.szRspMsgsSqrd - this.szRspMsgsAvg*this.szRspMsgsAvg);
            
            return std/1.0e6;
        }

        /**
         * @return the raw correlated data set size average (blocks) 
         */
        public final double getCorrelateSetSizeAvg() {
            return cntCorrelSetAvg;
        }
        
        /**
         * <p>
         * Computes and returns the standard deviation of the raw correlated data set size.
         * </p>
         *  
         * @return  raw correlated data set size standard deviation (blocks)
         */
        public final double correlatedSetSizeStd() {
            double  std = Math.sqrt(this.cntCorrelSetSqrd - this.cntCorrelSetAvg*this.cntCorrelSetAvg);
            
            return std;
        }

        /**
         * @return the processed data set allocation size average (MBytes) 
         */
        public final double getProcessedAllocationAvg() {
            return szProcessedAvg/1.0e6;
        }
        
        /**
         * <p>
         * Computes and returns the standard deviation of the processed memory allocation size.
         * </p>
         * 
         * @return  the processed memory allocation size standard deviation (MBytes)
         */
        public final double processedAllocationStd() {
            double  std = Math.sqrt(this.szProcessedSqrd - this.szProcessedAvg*this.szProcessedAvg);
            
            return std/1.0e6;
        }

        
        //
        // ConfigScore Overrides
        //
        
        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#addInResult(java.lang.Record)
         */
        @Override
        public double addInResult(CorrelatorTestResult recResult) throws IllegalArgumentException {

            // Process the super class state variables
            double  dblDataRate = super.addInResult(recResult);    // throws IllegalArgumentException
            
            // Get the current hit count and de-normalize the local state variables
            int     N = super.getHitCount() - 1;
            
            if (N > 0) {
                this.cntRspMsgsAvg *= N;
                this.cntRspMsgsSqrd *= N;
                this.szRspMsgsAvg *= N;
                this.szRspMsgsSqrd *= N;
                
                this.cntCorrelSetAvg *= N;
                this.cntCorrelSetSqrd *= N;
                this.szProcessedAvg *= N;
                this.szProcessedSqrd *= N;
            }
            
            // Add in the appropriate test result values
            this.cntRspMsgsAvg += recResult.cntRspMsgs();
            this.cntRspMsgsSqrd += recResult.cntRspMsgs() * recResult.cntRspMsgs();
            this.szRspMsgsAvg += recResult.szRspMsgs();
            this.szRspMsgsSqrd += recResult.szRspMsgs() * recResult.szRspMsgs();
            
            this.cntCorrelSetAvg += recResult.cntCorrelSet();
            this.cntCorrelSetSqrd += recResult.cntCorrelSet() * recResult.cntCorrelSet();
            this.szProcessedAvg += recResult.szProcessed();
            this.szProcessedSqrd += recResult.szProcessed() * recResult.szProcessed();
            
            // Re-normalize the average values
            N = N + 1;
            
            this.cntRspMsgsAvg /= N;
            this.cntRspMsgsSqrd /= N;
            this.szRspMsgsAvg /= N;
            this.szRspMsgsSqrd /= N;
            
            this.cntCorrelSetAvg /= N;
            this.cntCorrelSetSqrd /= N;
            this.szProcessedAvg /= N;
            this.szProcessedSqrd /= N;
            
            return dblDataRate;
        }


        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#printOut(java.io.PrintStream, java.lang.String)
         */
        @Override
        public void printOut(PrintStream ps, String strPad) {
            strPad = (strPad == null) ? "" : strPad;
            String strPadd = strPad + "  ";
            
            super.printOut(ps, strPad);
            ps.println(strPad + "Test Results Properties");
            ps.println(strPadd + "Recovered message count avg.                : " + this.getResponseMessageCountAvg());
            ps.println(strPadd + "Recovered message count std.                : " + this.responseMessageCountStd());
            ps.println(strPadd + "Recovered memory allocation avg. (MBytes)   : " + this.getResponseAllocationAvg());
            ps.println(strPadd + "Recovered memory allocation std. (MBytes)   : " + this.responseAllocationStd());
            ps.println(strPadd + "Processed data set size avg. (blocks)       : " + this.getCorrelateSetSizeAvg());
            ps.println(strPadd + "Processed data set size std. (blocks)       : " + this.correlatedSetSizeStd());
            ps.println(strPadd + "Processed data set allocation avg. (MBytes) : " + this.getProcessedAllocationAvg());
            ps.println(strPadd + "Processed data set allocation std. (MBytes) : " + this.processedAllocationStd());
        }
        
    }
    
    
    //
    // ConfigScorerBase Abstract Methods
    //

    /**
     * @see com.ospreydcs.dp.jal.tools.common.ConfigScorerBase#extractConfiguration(java.lang.Record)
     */
    @Override
    protected CorrelatorConfig extractConfiguration(CorrelatorTestResult recResult) {
        return CorrelatorConfig.from(recResult);
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.ConfigScorerBase#newScore(java.lang.Record)
     */
    @Override
    protected Score newScore(CorrelatorConfig recConfig) {
        return Score.from(recConfig);
    }
}
