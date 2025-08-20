/*
 * Project: dp-api-common
 * File:	GrpcStreamConfigScorer.java
 * Package: com.ospreydcs.dp.jal.tools.query.recovery
 * Type: 	GrpcStreamConfigScorer
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
 * @since Aug 19, 2025
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
 * Class for managing the gRPC stream configuration scoring for collections <code>QueryRecoveryTestResult</code>
 * test results.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Aug 19, 2025
 *
 */
public class GrpcStreamConfigScorer extends 
                ConfigScorerBase<
                GrpcStreamConfig, 
                QueryRecoveryTestResult, 
                com.ospreydcs.dp.jal.tools.query.recovery.GrpcStreamConfigScorer.Score> 
{

    
    //
    // GrpcStreamConfigScorer Creators
    //
    
    /**
     * <p>
     * Creates a new, empty <code>GrpcStreamConfigScorer</code> instance ready for test configuration scoring.
     * </p>
     * <p>
     * Creates and returns an empty <code>GrpcStreamConfigScorer</code> instance ready for scoring the
     * <code>QueryRecoveryTestResult</code> test results against the configuration of the 
     * <code>GrpcStreamConfig</code> processor configuration used for test conditions.
     * </p>
     * <p>
     * Use the <code>score(...)</code> methods to add <code>QueryRecoveryTestResult</code> records generated from
     * <code>{@link QueryRecoveryTestCase#evaluate(com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer)}</code>
     * test evaluations.
     * </p>
     * 
     * @return  a new <code>GrpcStreamConfigScorer</code> instance ready for use
     */
    public static GrpcStreamConfigScorer  create() {
        return new GrpcStreamConfigScorer();
    }
    
    /**
     * <p>
     * Creates a new, populated <code>GrpcStreamConfigScorer</code> instance.
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
     * @return  a new <code>GrpcStreamConfigScorer</code> instance which scores the given argument collection
     */
    public static GrpcStreamConfigScorer   from(Collection<QueryRecoveryTestResult> setResults) {
        GrpcStreamConfigScorer    scorer = GrpcStreamConfigScorer.create();
        
        scorer.score(setResults);
        
        return scorer;
    }

    
    //
    // Internal Types
    //
    
    /**
     * <p>
     * Class maintaining the scoring for a <code>GrpcStreamConfig</code> configuration record.
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
    public static class Score extends ConfigScoreBase<GrpcStreamConfig, QueryRecoveryTestResult> {

        
        //
        // Creators
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
        public static Score from(GrpcStreamConfig recConfig) {
            return new Score(recConfig);
        }
        
        
        //
        // ConfigScoreBase Abstract Methods
        //
        
        /**
         * @see com.ospreydcs.dp.jal.tools.common.ConfigScoreBase#extractConfiguration(java.lang.Record)
         */
        @Override
        protected GrpcStreamConfig extractConfiguration(QueryRecoveryTestResult recResult) {
            return GrpcStreamConfig.from(recResult);
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

        @Override
        protected void printOutConfiguration(PrintStream ps, String strPad, GrpcStreamConfig recConfig) {
            recConfig.printOut(ps, strPad);
        }
        
        
        //
        // State Variables
        //
        
        /** The average number of gRPC streams used */
        private double  cntStrmsAvg = 0.0;
        
        /** The square of gRPC stream count (used for standard deviation calculation) */
        private double  cntStrmsSqrd = 0.0;
        
        
        /** The average number of recovered messages per result */
        private int     cntRcvrdMsgsAvg = 0;
        
        /** The square of the average recovered message count for standard deviation calculation */
        private int     cntRcvrdMsgsSqrd = 0;
        
        
        /** The average allocation size (in bytes) of the result recovery */ 
        private long    szAllocPrcdAvg = 0;
        
        /** The square of the average allocation size (in bytes^2) for standard deviation calculation */
        private long    szAllocPrcdSqrd = 0;
        
        
        
        //
        // Constructor
        //
        
        /**
         * <p>
         * Constructs a new <code>Score</code> instance.
         * </p>
         *
         * @param recCfg    the configuration record associated with this <code>Score</code>
         */
        protected Score(GrpcStreamConfig recCfg) {
            super(recCfg);
        }

        
        //
        // State Inquiry
        //
        
        /**
         * @return  the average number of gRPC streams used per result
         */
        public final double getGrpcStreamCountAvg() {
            return this.cntStrmsAvg;
        }
        
        /**
         * <p>
         * Computes and returns the standard deviation for the number of gRPC streams used.
         * </p>
         * 
         * @return  gRPC stream count standard deviation
         */
        public final double grpcStreamCountStd() {
            double  dblStd = Math.sqrt(this.cntStrmsSqrd - this.cntStrmsAvg*this.cntStrmsAvg);
            
            return dblStd;
        }
        
        /**
         * @return the average number of recovered messages per result 
         */
        public final int    getRecoveredMessageCountAvg() {
            return this.cntRcvrdMsgsAvg;
        }
        
        /**
         * <p>
         * Computes and returns the standard deviation for the recovered message count.
         * </p>
         * 
         * @return  recovered message count standard deviation
         */
        public final double recoveredMessageCountStd() {
            double  dblStd = Math.sqrt( this.cntRcvrdMsgsSqrd - this.cntRcvrdMsgsAvg*this.cntRcvrdMsgsAvg);
            
            return dblStd;
        }

        /**
         * @return the average recovered allocation size (in Mbytes) of the result recovery 
         */
        public final long getAllocationSizeAvg() {
            return this.szAllocPrcdAvg/1_000_000;
        }


        /**
         * <p>
         * Computes and returns the standard deviation of the recovered allocation size.
         * </p>
         * 
         * @return  recovered allocation size standard deviation (in MBytes)
         */
        public final double allocationSizeStd() {
            double  dblStd = Math.sqrt( this.szAllocPrcdSqrd - this.szAllocPrcdAvg*this.szAllocPrcdAvg );

            return dblStd/(1.0e6);
            
        }

        /**
         * <p>
         * Adds the results into the child class attributes.
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
            
            // Process the super class state variables - Increments the hit count
            double  dblDataRate = super.addInResult(recResult);    // throws IllegalArgumentException
            
            // Get the current hit count and de-normalize the local state variables
            int     N = super.getHitCount() - 1;    // N = 0 for the first time through
            
            if (N > 0) {    // This is not the first time through
                this.cntStrmsAvg *= N;
                this.cntStrmsSqrd *= N;
                this.cntRcvrdMsgsAvg *= N;
                this.cntRcvrdMsgsSqrd *= N;
                this.szAllocPrcdAvg *= N;
                this.szAllocPrcdSqrd *= N;
            }
            
            // Add in the result values
            this.cntStrmsAvg += recResult.lstCmpRqsts().size();
            this.cntStrmsSqrd += recResult.lstCmpRqsts().size() * recResult.lstCmpRqsts().size();
            this.cntRcvrdMsgsAvg += recResult.cntRcvrdMsgs();
            this.cntRcvrdMsgsSqrd += recResult.cntRcvrdMsgs() * recResult.cntRcvrdMsgs();
            this.szAllocPrcdAvg += recResult.szAllocPrcd();
            this.szAllocPrcdSqrd += recResult.szAllocPrcd() * recResult.szAllocPrcd();
            
            // Re-normalize values
            N = N + 1;
            
            this.cntStrmsAvg /= N;
            this.cntStrmsSqrd /= N;
            this.cntRcvrdMsgsAvg /= N;
            this.cntRcvrdMsgsSqrd /= N;
            this.szAllocPrcdAvg /= N;
            this.szAllocPrcdSqrd /= N;
            
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
            ps.println(strPadd + "gRPC stream count avg.                  : " + this.getGrpcStreamCountAvg());
            ps.println(strPadd + "gRPC stream count std.                  : " + this.grpcStreamCountStd());
            ps.println(strPadd + "Recovered message count avg.            : " + this.getRecoveredMessageCountAvg());
            ps.println(strPadd + "Recovered message count std.            : " + this.recoveredMessageCountStd());
            ps.println(strPadd + "Recovered allocation size avg. (MBytes) : " + this.getAllocationSizeAvg());
            ps.println(strPadd + "Recovered allocation size std. (MBytes) : " + this.allocationSizeStd());
        }
        
    }
    
    
    //
    // GrpcStreamConfigScorer Abstract Methods
    //

    /**
     * @see com.ospreydcs.dp.jal.tools.common.ConfigScorerBase#extractConfiguration(java.lang.Record)
     */
    @Override
    protected GrpcStreamConfig extractConfiguration(QueryRecoveryTestResult recResult) {
        GrpcStreamConfig    recConfig = GrpcStreamConfig.from(recResult);
        
        return recConfig;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.ConfigScorerBase#newScore(java.lang.Record)
     */
    @Override
    protected com.ospreydcs.dp.jal.tools.query.recovery.GrpcStreamConfigScorer.Score newScore(GrpcStreamConfig recConfig) {
        return Score.from(recConfig);
    }
}
