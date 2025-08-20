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

import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.jal.tools.common.ConfigScoreBase;
import com.ospreydcs.dp.jal.tools.common.ConfigScorerBase;

/**
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
    // Internal Types
    //
    
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
