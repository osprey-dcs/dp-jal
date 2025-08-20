/*
 * Project: dp-api-common
 * File:	CorrelationConfigScorer.java
 * Package: com.ospreydcs.dp.jal.tools.query.recovery
 * Type: 	CorrelationConfigScorer
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
package com.ospreydcs.dp.jal.tools.query.recovery;

import java.io.PrintStream;
import java.util.Collection;

import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.jal.tools.common.ConfigScoreBase;
import com.ospreydcs.dp.jal.tools.common.ConfigScorerBase;

/**
 * <p>
 * Scorer for <code>CorrelationConfig</code> aspect of <code>QueryRecoveryTestResult</code> test result collections.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Aug 20, 2025
 *
 */
public class CorrelationConfigScorer extends 
            ConfigScorerBase<CorrelationConfig, 
                             QueryRecoveryTestResult, 
                             com.ospreydcs.dp.jal.tools.query.recovery.CorrelationConfigScorer.Score> 
{

    
    //
    // CorrelationConfigScorer Creators
    //
    
    /**
     * <p>
     * Creates a new, empty <code>CorrelationConfigScorer</code> instance ready for test configuration scoring.
     * </p>
     * <p>
     * Creates and returns an empty <code>CorrelationConfigScorer</code> instance ready for scoring the
     * <code>QueryRecoveryTestResult</code> test results against the configuration of the 
     * <code>RequestDecompConfig</code> processor configuration used for test conditions.
     * </p>
     * <p>
     * Use the <code>score(...)</code> methods to add <code>QueryRecoveryTestResult</code> records generated from
     * <code>{@link QueryRecoveryTestCase#evaluate(com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer)}</code>
     * test evaluations.
     * </p>
     * 
     * @return  a new <code>CorrelationConfigScorer</code> instance ready for use
     */
    public static CorrelationConfigScorer  create() {
        return new CorrelationConfigScorer();
    }
    
    /**
     * <p>
     * Creates a new, populated <code>CorrelationConfigScorer</code> instance.
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
     * @return  a new <code>CorrelationConfigScorer</code> instance which scores the given argument collection
     */
    public static CorrelationConfigScorer   from(Collection<QueryRecoveryTestResult> setResults) {
        CorrelationConfigScorer    scorer = CorrelationConfigScorer.create();
        
        scorer.score(setResults);
        
        return scorer;
    }

    
    //
    // Internal Types
    //
    
    /**
     * <p>
     * Class maintaining the scoring for a <code>CorrelationConfig</code> configuration record.
     * </p>
     * <p>
     * Class instances extract the the scoring from <code>QueryRecoveryTestResult</code> records
     * to maintain a score for the test conditions for the result.
     * </p>
     *
     * @author Christopher K. Allen
     * @since Aug 19, 2025
     *
     */
    public static class Score extends ConfigScoreBase<CorrelationConfig, QueryRecoveryTestResult> {

        
        //
        // Creator
        //
        
        /**
         * <p>
         * Creates and returns a new, empty <code>Score</code> instance for the given configuration.
         * </p>
         * 
         * @param recConfig the associated configuration for the returned <code>Score</code>
         *  
         * @return  a new <code>Score</code> instance ready for scoring the given configuration
         */
        public static Score from(CorrelationConfig recConfig) {
            return new Score(recConfig);
        }

        
        //
        // ConfigScoreBase Abstract Methods
        //
        
        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#extractConfiguration(java.lang.Record)
         */
        @Override
        protected CorrelationConfig extractConfiguration(QueryRecoveryTestResult recResult) {
            return CorrelationConfig.from(recResult);
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#isSuccess(java.lang.Record)
         */
        @Override
        protected boolean isSuccess(QueryRecoveryTestResult recResult) {
            return recResult.recTestStatus().isSuccess();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#extractDataRequest(java.lang.Record)
         */
        @Override
        protected DpDataRequest extractDataRequest(QueryRecoveryTestResult recResult) {
            return recResult.rqstOrg();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#extractDataRate(java.lang.Record)
         */
        @Override
        protected double extractDataRate(QueryRecoveryTestResult recResult) {
            return recResult.dblRatePrcd();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#printOutConfiguration(java.io.PrintStream, java.lang.String, java.lang.Record)
         */
        @Override
        protected void printOutConfiguration(PrintStream ps, String strPad, CorrelationConfig recConfig) {
            recConfig.printOut(ps, strPad);
        }
        
        
        //
        // State Variables
        //
        
        /** The average number of total raw correlated blocks per recovery */
        private int     cntBlksTotAvg = 0;
        
        /** The average number of clocked raw correlated blocks per recovery */
        private int     cntBlksClkdAvg = 0;
        
        /** The average number of timestamp list raw correlated blocks per recovery */
        private int     cntBlksTmsLstAvg = 0;
        
        
        /** Number of correctly ordered correlated result sets */
        private int     cntOrdered = 0;
        
        /** Number of result sets with disjoint time domains */
        private int     cntDisTmDoms = 0;
        
        
        //
        // Constructor
        //
        /**
         * <p>
         * Constructs a new <code>Score</code> instance for the given configuration.
         * </p>
         *
         * @param recCfg    the configuration associated with this <code>Score</code>
         */
        protected Score(CorrelationConfig recCfg) {
            super(recCfg);
        }

        
        //
        // State Inquiry
        //

        /**
         * @return the average number of total raw correlated blocks per recovery 
         */
        public final int getTotalRawBlockCountAvg() {
            return cntBlksTotAvg;
        }

        /**
         * @return the average number of clocked raw correlated blocks per recovery
         */
        public final int getClockedRawBlockCountAvg() {
            return cntBlksClkdAvg;
        }

        /**
         * @return the average number of timestamp list raw correlated blocks per recovery
         */
        public final int getTmsListRawBlockCountAvg() {
            return cntBlksTmsLstAvg;
        }

        /**
         * @return the number of correctly ordered correlated result sets 
         */
        public final int getOrderedRawBlockResults() {
            return cntOrdered;
        }

        /**
         * @return the number of result sets with disjoint time domains 
         */
        public final int getDisjointTmDomainRawBlockResults() {
            return cntDisTmDoms;
        }

        
        //
        // ConfigScoreBase Operation Override
        //
        
        /**
         * <p>
         * Adds the results into the local child class state variables.
         * </p>
         * <p>
         * First calls <code>{@link ConfigScoreBase#addInResult(Record)}</code> to process
         * the parent class state variables.
         * Then updates all local state variables with the given test results.
         * </p>
         * 
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#addInResult(java.lang.Record)
         */
        @Override
        public double addInResult(QueryRecoveryTestResult recResult) throws IllegalArgumentException {
            
            // Process the super class state variables
            double  dblDataRate = super.addInResult(recResult);    // throws IllegalArgumentException
            
            // Get the current hit count and de-normalize the local state variables
            int     N = super.getHitCount() - 1;
            
            if (N > 0) {
                this.cntBlksTotAvg *= N;
                this.cntBlksClkdAvg *= N;
                this.cntBlksTmsLstAvg *= N;
            }
            
            // Add in the appropriate test result values
            this.cntBlksTotAvg += recResult.cntBlksPrcdTot();
            this.cntBlksClkdAvg += recResult.cntBlksPrcdClkd();
            this.cntBlksTmsLstAvg += recResult.cntBlksPrcdTmsLst();
            
            // Re-normalize the average values
            N = N + 1;
            
            this.cntBlksTotAvg /= N;
            this.cntBlksClkdAvg /= N;
            this.cntBlksTmsLstAvg /= N;
            
            // Add in the test result properties
            this.cntOrdered += recResult.recBlksOrdered().isSuccess() ? 1 : 0;
            this.cntDisTmDoms += recResult.recBlksDisTmDom().isSuccess() ? 1 : 0;
            
            return dblDataRate;
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#printOut(java.io.PrintStream, java.lang.String)
         */
        @Override
        public void printOut(PrintStream ps, String strPad) {
            if (strPad == null)
                strPad = "";
            String strPadd = strPad + "  ";
            
            super.printOut(ps, strPad);
            ps.println(strPad + "Test Results Properties");
            ps.println(strPadd + "Average raw correlated blocks total          : " + this.cntBlksTotAvg);
            ps.println(strPadd + "Average raw correlated blocks clocked        : " + this.cntBlksClkdAvg);
            ps.println(strPadd + "Average raw correlated blocks timestamp list : " + this.cntBlksTmsLstAvg);
            ps.println(strPadd + "Results sets with ordered correlated blocks  : " + this.cntOrdered);
            ps.println(strPadd + "Results sets w/ disjoint time-domain blocks  : " + this.cntDisTmDoms);
        }
    }
    
    //
    // ConfigScorerBase Abstract Methods
    //

    /**
     * @see com.ospreydcs.dp.jal.tools.common.ConfigScorerBase#extractConfiguration(java.lang.Record)
     */
    @Override
    protected CorrelationConfig extractConfiguration(QueryRecoveryTestResult recResult) {
        return CorrelationConfig.from(recResult);
    }

    @Override
    protected com.ospreydcs.dp.jal.tools.query.recovery.CorrelationConfigScorer.Score newScore(CorrelationConfig recConfig) {
        return Score.from(recConfig);
    }
}
