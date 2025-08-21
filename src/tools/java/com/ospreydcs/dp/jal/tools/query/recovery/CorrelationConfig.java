/*
 * Project: dp-api-common
 * File:	CorrelationConfig.java
 * Package: com.ospreydcs.dp.jal.tools.query.recovery
 * Type: 	CorrelationConfig
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

/**
 * <p>
 * Record containing the correlation configuration parameters for a <code>QueryRecoveryTestCase</code> evaluation.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 20, 2025
 *
 * @param bolCorrRcvry  enable/disable raw data correlation during data recovery
 * @param bolCorrConc   enable/disable multi-threaded concurrency for correlation processing
 * @param szCorrPivot   the target set size (number of correlated blocks) triggering multi-threaded correlation when enabled
 * @param cntCorrMaxThrds   maximum number of correlator execution threads when concurrency is enabled
 */
public record CorrelationConfig(
        boolean             bolCorrRcvry,
        boolean             bolCorrConc,
        int                 szCorrPivot,
        int                 cntCorrMaxThrds
        ) 
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>CorrelationConfig</code> record populated with the test configuration within
     * argument result record.
     * </p>
     * <p>
     * The method extracts field <code>{@link QueryRecoveryTestResult#recTestCase()}</code> then defers to method
     * <code>{@link #from(QueryRecoveryTestCase)}</code>.
     * </p> 
     * 
     * @param recResult the test result record containing the test conditions
     * 
     * @return  a new <code>CorrelationConfig</code> record populated from the test conditions within the argument
     */
    public static CorrelationConfig from(QueryRecoveryTestResult recResult) {
        QueryRecoveryTestCase   recTestCase = recResult.recTestCase();
        
        return CorrelationConfig.from(recTestCase);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>CorrelationConfig</code> record populated with the configuration field values of 
     * the argument record.
     * </p>
     * 
     * @param recTestCase   test case record containing configuration field values
     * 
     * @return  a new <code>CorrelationConfig</code> record populated from the fields of the given argument
     */
    public static CorrelationConfig from(QueryRecoveryTestCase recTestCase) {
        return new CorrelationConfig(
                recTestCase.bolCorrRcvry(),
                recTestCase.bolCorrConc(),
                recTestCase.szCorrPivot(),
                recTestCase.cntCorrMaxThrds()
                );
    }
    
    
    // 
    // Record Overrides
    //

    /**
     * <p>
     * Overrides the default <code>Record</code> implementation to create an equivalence.
     * </p>
     * <p>
     * The method now checks the current record field values against that of the argument for 
     * equality.  That is, the records are not compared as instances, but as records containing
     * equivalent field values.
     * </p>
     * 
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof CorrelationConfig rec) {
            boolean bolResult = (this.bolCorrRcvry == rec.bolCorrRcvry)
                    && (this.bolCorrConc == rec.bolCorrConc)
                    && (this.szCorrPivot == rec.szCorrPivot)
                    && (this.cntCorrMaxThrds == rec.cntCorrMaxThrds);
            
            return bolResult;
        }

        return false;
    }

    //
    // Operations
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
        
        ps.println(strPad + "Correlate during recovery enabled : " + this.bolCorrRcvry);
        ps.println(strPad + "Concurrency (multi-thrd) enabled  : " + this.bolCorrConc);
        ps.println(strPad + "Concurrency target set size pivot : " + this.szCorrPivot);
        ps.println(strPad + "Concurrency maximum thread count  : " + this.cntCorrMaxThrds);
    }
    
}
