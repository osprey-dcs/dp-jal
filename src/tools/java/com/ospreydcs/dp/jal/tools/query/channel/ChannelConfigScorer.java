/*
 * Project: dp-api-common
 * File:	ChannelConfigScorer.java
 * Package: com.ospreydcs.dp.jal.tools.query.channel
 * Type: 	ChannelConfigScorer
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
package com.ospreydcs.dp.jal.tools.query.channel;

import java.io.PrintStream;
import java.util.Collection;

import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.jal.tools.common.ConfigScoreBase;
import com.ospreydcs.dp.jal.tools.common.ConfigScorerBase;

/**
 * <p>
 * Configuration scorer class for collections of <code>QueryChannelTestResult</code> records.
 * </p>
 * <p>
 * Scores are based upon <code>QueryChannelTestCase</code> test conditions specifically identified
 * in the <code>ChannelConfig</code> record.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Aug 20, 2025
 *
 */
public class ChannelConfigScorer extends 
                    ConfigScorerBase<ChannelConfig, 
                                     QueryChannelTestResult, 
                                     com.ospreydcs.dp.jal.tools.query.channel.ChannelConfigScorer.Score> 
{

    
    //
    // CorrelationConfigScorer Creators
    //
    
    /**
     * <p>
     * Creates a new, empty <code>ChannelConfigScorer</code> instance ready for test configuration scoring.
     * </p>
     * <p>
     * Creates and returns an empty <code>ChannelConfigScorer</code> instance ready for scoring the
     * <code>QueryRecoveryTestResult</code> test results against the configuration of the 
     * <code>RequestDecompConfig</code> processor configuration used for test conditions.
     * </p>
     * <p>
     * Use the <code>score(...)</code> methods to add <code>QueryRecoveryTestResult</code> records generated from
     * <code>{@link QueryChannelTestCase#evaluate(com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer)}</code>
     * test evaluations.
     * </p>
     * 
     * @return  a new <code>ChannelConfigScorer</code> instance ready for use
     */
    public static ChannelConfigScorer  create() {
        return new ChannelConfigScorer();
    }
    
    /**
     * <p>
     * Creates a new, populated <code>ChannelConfigScorer</code> instance.
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
     * @return  a new <code>ChannelConfigScorer</code> instance which scores the given argument collection
     */
    public static ChannelConfigScorer   from(Collection<QueryChannelTestResult> setResults) {
        ChannelConfigScorer    scorer = ChannelConfigScorer.create();
        
        scorer.score(setResults);
        
        return scorer;
    }

    
    //
    // Internal Types
    //
    
    /**
     * <p>
     * Class maintaining the scoring for a <code>ChannelConfig</code> configuration record.
     * </p>
     * <p>
     * Class instances extract the the scoring from <code>QueryChannelTestResult</code> records
     * to maintain a score for the test conditions for the result.
     * </p>
     *
     * @author Christopher K. Allen
     * @since Aug 19, 2025
     *
     */
    public static class Score extends ConfigScoreBase<ChannelConfig, QueryChannelTestResult> {

        
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
        public static Score from(ChannelConfig recCfg) {
            return new Score(recCfg);
        }
        
        //
        // ConfigScoreBase Abstract Methods
        //

        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#extractConfiguration(java.lang.Record)
         */
        @Override
        protected ChannelConfig extractConfiguration(QueryChannelTestResult recResult) {
            return ChannelConfig.from(recResult);
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#isSuccess(java.lang.Record)
         */
        @Override
        protected boolean isSuccess(QueryChannelTestResult recResult) {
            return recResult.recTestStatus().isSuccess();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#extractDataRequest(java.lang.Record)
         */
        @Override
        protected DpDataRequest extractDataRequest(QueryChannelTestResult recResult) {
            return recResult.recTestCase().rqstOrg();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#extractDataRate(java.lang.Record)
         */
        @Override
        protected double extractDataRate(QueryChannelTestResult recResult) {
            return recResult.dblDataRate();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#printOutConfiguration(java.io.PrintStream, java.lang.String, java.lang.Record)
         */
        @Override
        protected void printOutConfiguration(PrintStream ps, String strPad, ChannelConfig recConfig) {
            recConfig.printOut(ps, strPad);
        }
        
        
        //
        // State Variables
        //

        /** The returned data message count average */
        private double      cntMsgsAvg = 0.0;
        
        /** The returned data message count squared average - used for standard deviation calculation */
        private double      cntMsgsSqrd = 0.0;
        
        /** The returned memory allocation size average (bytes) */
        private double      szAllocAvg = 0.0;
        
        /** The returned memory allocation size squared average (bytes^2) - used in standard deviation calculation */
        private double      szAllocSqrd = 0.0;
        
        
        //
        // Constructor
        //
        
        /**
         * <p>
         * Constructs a new <code>Score</code> instance for the given configuration.
         * </p>
         *
         * @param recCfg    the configuration associated with this <code>Score</code> instance
         */
        protected Score(ChannelConfig recCfg) {
            super(recCfg);
        }

        
        //
        // State Inquiry
        //

        /**
         * @return the recovered data message count average 
         */
        public final double getMessageCountAvg() {
            return this.cntMsgsAvg;
        }
        
        /**
         * <p>
         * Computes and returns the recovered message count standard deviation.
         * </p>
         * 
         * @return  recovered message count standard deviation 
         */
        public final double messageCountStd() {
            double  std = Math.sqrt(this.cntMsgsSqrd - this.cntMsgsAvg*this.cntMsgsAvg);
            
            return std;
        }

        /**
         * @return the recovered memory allocation size average (MBytes) 
         */
        public final double getAllocationSizeAvg() {
            return this.szAllocAvg/1.0e6;
        }
        
        /**
         * <p>
         * Compute and return the recovered memory allocation standard deviation.
         * </p>
         * 
         * @return  recovered memory allocation standard deviation (in MBytes)
         */
        public final double allocationSizeStd() {
            double  std = Math.sqrt(this.szAllocSqrd - this.szAllocAvg*this.szAllocAvg);
            
            return std/1.0e6;
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
        public double addInResult(QueryChannelTestResult recResult) throws IllegalArgumentException {
            
            // Process the super class state variables
            double  dblDataRate = super.addInResult(recResult);    // throws IllegalArgumentException
            
            // Get the current hit count and de-normalize the local state variables
            int     N = super.getHitCount() - 1;
            
            if (N > 0) {
                this.cntMsgsAvg *= N;
                this.cntMsgsSqrd *= N;
                this.szAllocAvg *= N;
                this.szAllocSqrd *= N;
            }
            
            // Add in the appropriate test result values
            this.cntMsgsAvg += recResult.cntMessages();
            this.cntMsgsSqrd += recResult.cntMessages() * recResult.cntMessages();
            this.szAllocAvg += recResult.szRecovery();
            this.szAllocSqrd += recResult.szRecovery() * recResult.szRecovery();
            
            // Re-normalize the average values
            N = N + 1;
            
            this.cntMsgsAvg /= N;
            this.cntMsgsSqrd /= N;
            this.szAllocAvg /= N;
            this.szAllocSqrd /= N;
            
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
            ps.println(strPadd + "Recovered message count avg.              : " + this.getMessageCountAvg());
            ps.println(strPadd + "Recovered message count std.              : " + this.messageCountStd());
            ps.println(strPadd + "Recovered memory allocation avg. (MBytes) : " + this.getAllocationSizeAvg());
            ps.println(strPadd + "Recovered memory allocation std. (MBytes) : " + this.allocationSizeStd());
        }
    }

    
    //
    // ConfigScorerBase Abstract Methods

    /**
     * @see com.ospreydcs.dp.jal.tools.common.ConfigScorerBase#extractConfiguration(java.lang.Record)
     */
    @Override
    protected ChannelConfig extractConfiguration(QueryChannelTestResult recResult) {
        return ChannelConfig.from(recResult);
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.ConfigScorerBase#newScore(java.lang.Record)
     */
    @Override
    protected Score newScore(ChannelConfig recConfig) {
        return Score.from(recConfig);
    }

}
