/*
 * Project: dp-api-common
 * File:	RequestDecompConfig.java
 * Package: com.ospreydcs.dp.jal.tools.query.recovery
 * Type: 	RequestDecompConfig
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
 * @since Aug 18, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.recovery;

import java.io.PrintStream;
import java.time.Duration;

import com.ospreydcs.dp.api.query.model.request.RequestDecompType;

/**
 * <p>
 * Record containing the time-series data request decomposition configuration for a <code>QueryRecoveryTestCase</code> record.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 18, 2025
 *
 * @param bolDcmpEnbl   time-series data request decomposition enable/disable
 * @param bolDcmpAuto   use automatic request decomposition based upon maximum PV count {@link #cntDcmpMaxPvs} and maximum request duration {@link #durDcmpMaxRng} 
 * @param enmDcmpType   request decomposition strategy used in explicit (non-auto) decomposition (conforms to number of requests {@link #cntReqsts})
 * @param cntCmpRqsts   maximum number of component requests used in explicit (non-auto) decompositions (conforms to the number of gRPC streams)
 * @param cntDcmpMaxPvs the maximum number of PV names within a composite data request; used in automatic decomposition
 * @param durDcmpMaxRng the maximum time-range duration for a composite data request; used in automatic decomposition
 */
public record RequestDecompConfig(
        boolean             bolDcmpEnbl,
        boolean             bolDcmpAuto,
        RequestDecompType   enmDcmpType,
        int                 cntCmpRqsts,
        int                 cntDcmpMaxPvs,
        Duration            durDcmpMaxRng
        ) 
{

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>RequestDecompConfig</code> record populated with the test configuration within
     * argument result record.
     * </p>
     * <p>
     * The method extracts field <code>{@link QueryRecoveryTestResult#recTestCase()}</code> then defers to method
     * <code>{@link #from(QueryRecoveryTestCase)}</code>.
     * </p> 
     * 
     * @param recResult the test result record containing the test conditions
     * 
     * @return  a new <code>RequestDecompConfig</code> record populated from the test conditions within the argument
     */
    public static RequestDecompConfig   from(QueryRecoveryTestResult recResult) {
        
        // Extract test case and defer to from(QueryRecoveryTestCase)
        QueryRecoveryTestCase   recTestCase = recResult.recTestCase();
        
        return RequestDecompConfig.from(recTestCase);
    }
    /**
     * <p>
     * Creates and returns a new <code>RequestDecompConfig</code> record populated with the configuration field values of 
     * the argument record.
     * </p>
     * 
     * @param recTestCase   test case record containing configuration field values
     * 
     * @return  a new <code>RequestDecompConfig</code> record populated from the fields of the given argument
     */
    public static RequestDecompConfig   from(QueryRecoveryTestCase recTestCase) {
        return new RequestDecompConfig(
                recTestCase.bolDcmpEnbl(), 
                recTestCase.bolDcmpAuto(), 
                recTestCase.enmDcmpType(), 
                recTestCase.cntMStrmMaxStrms(),
                recTestCase.cntDcmpMaxPvs(), 
                recTestCase.durDcmpMaxRng()
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
        
        if (obj instanceof RequestDecompConfig rec) {
            boolean bolResult = (this.bolDcmpEnbl == rec.bolDcmpEnbl)
                    && (this.bolDcmpAuto == rec.bolDcmpAuto)
                    && (this.enmDcmpType == rec.enmDcmpType)
                    && (this.cntCmpRqsts == rec.cntCmpRqsts)
                    && (this.cntDcmpMaxPvs == rec.cntDcmpMaxPvs)
                    && (this.durDcmpMaxRng.equals(rec.durDcmpMaxRng));
            return bolResult;

        } else {
            return false;
        }
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
        
        ps.println(strPad + "Decomposition enabled        : " + this.bolDcmpEnbl);
        ps.println(strPad + "Non-auto decomp. strategy    : " + this.enmDcmpType);
        ps.println(strPad + "Non-auto maximum composites  : " + this.cntCmpRqsts);
        ps.println(strPad + "Automatic decomp. enabled    : " + this.bolDcmpAuto);
        ps.println(strPad + "Auto max composite PV count  : " + this.cntDcmpMaxPvs);
        ps.println(strPad + "Auto max composite time rng. : " + this.durDcmpMaxRng);
    }
    
}
