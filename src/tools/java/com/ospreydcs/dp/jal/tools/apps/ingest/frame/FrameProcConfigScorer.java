/*
 * Project: dp-jal
 * File:	FrameProcConfigScorer.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.frame
 * Type: 	FrameProcConfigScorer
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

import java.io.PrintStream;
import java.util.Collection;

import com.ospreydcs.dp.jal.tools.apps.ingest.common.FrameProcessorConfig;
import com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase;
import com.ospreydcs.dp.jal.tools.common.score.ConfigScorerBase;

/**
 * <p>
 * Configuration scorer class for <code>FrameProcTestResult</code> records.
 * </p>
 * <p>
 * Scores are based upon <code>FrameProcTestCase</code> test conditions specifically identified
 * in the <code>FrameProcessorConfig</code> record.
 * The <code>FrameProcessorConfig</code> record contains the configuration parameters for a 
 * <code>IngestionFrameProcessor</code> instance used to process ingestion data frames.
 * </p>
 * <p>
 * 
 *
 * @author Christopher K. Allen
 * @since Feb 11, 2026
 *
 */
public class FrameProcConfigScorer extends ConfigScorerBase<FrameProcessorConfig, FrameProcTestResult, com.ospreydcs.dp.jal.tools.apps.ingest.frame.FrameProcConfigScorer.ConfigScore> {

    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new, populated <code>FrameProcConfigScorer</code> instance.
     * </p>
     * <p>
     * This creator returns a fully scored <code>FrameProcConfigScorer</code> instance for the given
     * collection of <code>FrameProcTestResult</code> test results.
     * That is, the returned object contains all the scoring information for the given argument collection.
     * </p>
     * <p>
     * Note that additional test results can be added to the score using the base class methods
     * <code>{@link #score(FrameProcTestResult)}</code> and <code>{@link #score(Collection)}</code>.
     * </p>
     * 
     * @param conResults    the collection of test results to be scored
     * 
     * @return  a new <code>FrameProcConfigScorer</code> instance which scores the given argument collection
     */
    public static FrameProcConfigScorer from(Collection<FrameProcTestResult> conResults) {
        return new FrameProcConfigScorer(conResults);
    }
    
    
    //
    // Internal Types
    //
    
    /**
    /**
     * <p>
     * Class maintaining the scoring for a <code>FrameProcessorConfig</code> configuration record.
     * </p>
     * <p>
     * Class instances extract the the scoring from <code>FrameProcTestResult</code> records
     * to maintain a score for the test conditions for the result.
     * </p>
     *
     *
     * @author Christopher K. Allen
     * @since Feb 11, 2026
     *
     */
    public static class ConfigScore extends ConfigScoreBase<FrameProcessorConfig, FrameProcTestResult> {

        
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
        public static ConfigScore from(FrameProcessorConfig recCfg) {
            return new ConfigScore(recCfg);
        }

        
        //
        // ConfigScoreBase Abstract Methods
        //
        
        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#extractConfiguration(java.lang.Record)
         */
        @Override
        protected FrameProcessorConfig extractConfiguration(FrameProcTestResult recResult) {
            return recResult.recTestCase().recPrcrCfg();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#isSuccess(java.lang.Record)
         */
        @Override
        protected boolean isSuccess(FrameProcTestResult recResult) {
            return recResult.recTestStatus().isSuccess();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#extractDataRequest(java.lang.Record)
         */
        @Override
        protected String extractDataRequest(FrameProcTestResult recResult) {
            return recResult.recTestCase().specFrame().strLabel();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#extractDataRate(java.lang.Record)
         */
        @Override
        protected double extractDataRate(FrameProcTestResult recResult) {
            return recResult.dblRateRaw();
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#printOutConfiguration(java.io.PrintStream, java.lang.String, java.lang.Record)
         */
        @Override
        protected void printOutConfiguration(PrintStream ps, String strPad, FrameProcessorConfig recConfig) {
            recConfig.printOut(ps, strPad);
        }
        
        //
        // ConfigScore Overrides
        //
        
        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScoreBase#addInResult(java.lang.Record)
         */
        @Override
        public double addInResult(FrameProcTestResult recResult) throws IllegalArgumentException {

            // Process the super class state variables
            double  dblDataRate = super.addInResult(recResult);    // throws IllegalArgumentException
            
            // Get the current hit count and de-normalize the local state variables
            int     N = super.getHitCount() - 1;
            
            if (N > 0) {
                this.cntFrmsAvg *= N;
                this.cntFrmsSqrd *= N;
                this.szPayloadAvg *= N;
                this.szPayloadSqrd *= N;
                
                this.cntMsgsAvg *= N;
                this.cntMsgsSqrd *= N;
                this.szProcessedAvg *= N;
                this.szProcessedSqrd *= N;
            }
            
            // Add in the appropriate test result values
            this.cntFrmsAvg += recResult.cntFrames();
            this.cntFrmsSqrd += recResult.cntFrames() * recResult.cntFrames();
            this.szPayloadAvg += recResult.szPayload();
            this.szPayloadSqrd += recResult.szPayload() * recResult.szPayload();
            
            this.cntMsgsAvg += recResult.cntMsgs();
            this.cntMsgsSqrd += recResult.cntMsgs() * recResult.cntMsgs();
            this.szProcessedAvg += recResult.szProcessed();
            this.szProcessedSqrd += recResult.szProcessed() * recResult.szProcessed();
            
            // Re-normalize the average values
            N = N + 1;

            this.cntFrmsAvg /= N;
            this.cntFrmsSqrd /= N;
            this.szPayloadAvg /= N;
            this.szPayloadSqrd /= N;
            
            this.cntMsgsAvg /= N;
            this.cntMsgsSqrd /= N;
            this.szProcessedAvg /= N;
            this.szProcessedSqrd /= N;
            
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
            ps.println(strPadd + "Payload ingestion frame count avg.      : " + this.getPayloadFrameCountAvg());
            ps.println(strPadd + "Payload ingestion frame count std.      : " + this.payloadFrameCountStd());
            ps.println(strPadd + "Payload memory allocation avg. (MBytes) : " + this.getPayloadAllocationAvg());
            ps.println(strPadd + "Payload memory allocation std. (MBytes) : " + this.payloadAllocationStd());
            ps.println(strPadd + "Processed message count avg.            : " + this.getProcessedMessageCountAvg());
            ps.println(strPadd + "Processed message count std.            : " + this.processedMessageCountStd());
            ps.println(strPadd + "Processed data allocation avg. (MBytes) : " + this.getProcessedAllocationAvg());
            ps.println(strPadd + "Processed data allocation std. (MBytes) : " + this.processedAllocationStd());
        }
        
        
        //
        // State Variables
        //
        
        /** The payload ingestion frame count average */
        private double  cntFrmsAvg = 0.0;
        
        /** The payload ingestion frame count squared average (for standard deviation calculation) */
        private double  cntFrmsSqrd = 0.0;
        
        /** The payload allocation size average (bytes) */
        private double  szPayloadAvg = 0.0;
        
        /** The payload  allocation size squared average (for standard deviation calculation) */
        private double  szPayloadSqrd = 0.0;
        
        /** The processed data message count average */
        private double  cntMsgsAvg = 0.0;
        
        /** The processed data message count squared average (for standard deviation calculation) */
        private double  cntMsgsSqrd = 0.0;
        
        /** The processed data set allocation size average (bytes) */
        private double  szProcessedAvg = 0.0;
        
        /** The processed data set allocation size squared average (bytes^2) (for standard deviation calculation) */
        private double  szProcessedSqrd = 0.0;
        
            
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
        protected ConfigScore(FrameProcessorConfig recCfg) {
            super(recCfg);
        }


        //
        // State Inquiry
        //
        
        /**
         * @return  the payload frame count average
         */
        public final double getPayloadFrameCountAvg() {
            return this.cntFrmsAvg;
        }
        
        public final double payloadFrameCountStd() {
            double  std = Math.sqrt(this.cntFrmsSqrd - this.cntFrmsAvg*this.cntFrmsAvg);
            
            return std;
        }

        /**
         * @return the payload allocation size average (Mbytes)
         */
        public final double getPayloadAllocationAvg() {
            return this.szPayloadAvg/1.0e6;
        }
        
        /**
         * <p>
         * Computes and returns the standard deviation of the payload allocation size.
         * </p>
         * 
         * @return  returned memory allocation size standard deviation (in MBytes)
         */
        public final double payloadAllocationStd() {
            double  std = Math.sqrt(this.szPayloadSqrd - this.szPayloadAvg*this.szPayloadAvg);
            
            return std/1.0e6;
        }

        /**
         * @return the returned data message count average 
         */
        public final double getProcessedMessageCountAvg() {
            return this.cntMsgsAvg;
        }
        
        /**
         * <p>
         * Computes and returns the standard deviation for the returned data message count.
         * </p>
         * 
         * @return  the returned data message count standard deviation 
         */
        public final double processedMessageCountStd() {
            double  std = Math.sqrt(cntMsgsSqrd - this.cntMsgsAvg*this.cntMsgsAvg);
            
            return std;
        }

        /**
         * @return the processed data set allocation size average (MBytes) 
         */
        public final double getProcessedAllocationAvg() {
            return this.szProcessedAvg/1.0e6;
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
    }

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new, populated <code>FrameProcConfigScorer</code> instance.
     * </p>
     * <p>
     * The new object contains all the scoring information for the given argument collection.
     * </p>
     * 
     * @param conResults    the collection of test results to be scored
     * 
     * @return  a new <code>FrameProcConfigScorer</code> instance which scores the given argument collection
     */
    private FrameProcConfigScorer(Collection<FrameProcTestResult> conResults) {
        super(conResults);
    }

    
    //
    // ConfigScorerBase Abstract Methods
    //

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScorerBase#extractConfiguration(java.lang.Record)
     */
    @Override
    protected FrameProcessorConfig extractConfiguration(FrameProcTestResult recResult) {
        return recResult.recTestCase().recPrcrCfg();
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.ConfigScorerBase#newScore(java.lang.Record)
     */
    @Override
    protected ConfigScore newScore(FrameProcessorConfig recConfig) {
        return ConfigScore.from(recConfig);
    }
}
