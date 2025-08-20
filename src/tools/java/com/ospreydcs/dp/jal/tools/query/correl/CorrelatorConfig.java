/*
 * Project: dp-api-common
 * File:	CorrelatorConfig.java
 * Package: com.ospreydcs.dp.jal.tools.query.correl
 * Type: 	CorrelatorConfig
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

/**
 * <p>
 * Record containing the configuration parameters within a <code>CorrelatorTestCase</code>
 * test case record.
 * </p>
 * <p>
 * Extracts the test case conditions for the <code>RawDataCorrelator</code> object used in
 * <code>{@link CorrelatorTestCase#evaluate(com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator, java.util.List)}</code>
 * test evaluations.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Aug 20, 2025
 *
 * @param bolConcOpt    enable/disable multi-threaded concurrency while correlation processing
 * @param cntMaxThrds   maximum number of execution threads when concurrency is enabled
 * @param szConcPivot   the target set size triggering multi-threading when enabled
 */
public record CorrelatorConfig(
        boolean             bolConcOpt,
        int                 cntMaxThrds,
        int                 szConcPivot
        ) 
{

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>CorrelatorConfig</code> record populated with the test configuration within
     * argument result record.
     * </p>
     * <p>
     * The method extracts field <code>{@link CorrelatorTestResult#recTestCase()}</code> then defers to method
     * <code>{@link #from(CorrelatorTestCase)}</code>.
     * </p> 
     * 
     * @param recResult the test result record containing the test conditions
     * 
     * @return  a new <code>CorrelatorConfig</code> record populated from the test conditions within the argument
     */
    public static CorrelatorConfig from(CorrelatorTestResult recResult) {
        CorrelatorTestCase   recTestCase = recResult.recTestCase();
        
        return CorrelatorConfig.from(recTestCase);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>CorrelatorConfig</code> record populated with the configuration field values of 
     * the argument record.
     * </p>
     * 
     * @param recTestCase   test case record containing configuration field values
     * 
     * @return  a new <code>CorrelatorConfig</code> record populated from the fields of the given argument
     */
    public static CorrelatorConfig from(CorrelatorTestCase recTestCase) {
        return new CorrelatorConfig(
                recTestCase.bolConcOpt(),
                recTestCase.cntMaxThrds(),
                recTestCase.szConcPivot()
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
        
        if (obj instanceof CorrelatorConfig cfg) {
            boolean bolResult = (this.bolConcOpt == cfg.bolConcOpt)
                    && (this.cntMaxThrds == cfg.cntMaxThrds)
                    && (this.szConcPivot == cfg.szConcPivot);
            
            return bolResult;
        }
        
        return false;
    }

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
        
        ps.println(strPad + "Concurrent processing enabled     : " + this.bolConcOpt);
        ps.println(strPad + "Maximum concurrency thread count  : " + this.cntMaxThrds);
        ps.println(strPad + "Pivot size triggering concurrency : " + this.szConcPivot);
    }

}
