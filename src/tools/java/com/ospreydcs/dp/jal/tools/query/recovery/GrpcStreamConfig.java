/*
 * Project: dp-api-common
 * File:	GrpcStreamConfig.java
 * Package: com.ospreydcs.dp.jal.tools.query.recovery
 * Type: 	GrpcStreamConfig
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

import com.ospreydcs.dp.api.common.DpGrpcStreamType;

/**
 * <p>
 * Record containing the gRPC streaming configuration parameters from a <code>QueryRecoveryTestCase</code> record.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 19, 2025
 *
 * @param enmStrmType   the gRPC stream type used in raw time-series data recovery
 * 
 * @param bolMStrmEnbl  enable/disable use of multiple gRPC stream in raw time-series data recovery (request decomposition enabled)
 * @param szMStrmQryDom approximate request query size (in PV-Seconds) triggering multiple gRPC data streams in recovery
 * @param cntMStrmMaxStrms maximum number of data streams allowed for multiple gRPC streaming  
 *  
 */
public record GrpcStreamConfig(
        DpGrpcStreamType    enmStrmType,

        boolean             bolMStrmEnbl,
        long                szMStrmQryDom,
        int                 cntMStrmMaxStrms
        ) 
{

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>GrpcStreamConfig</code> record populated with the test configuration within
     * argument result record.
     * </p>
     * <p>
     * The method extracts field <code>{@link QueryRecoveryTestResult#recTestCase()}</code> then defers to method
     * <code>{@link #from(QueryRecoveryTestCase)}</code>.
     * </p> 
     * 
     * @param recResult the test result record containing the test conditions
     * 
     * @return  a new <code>GrpcStreamConfig</code> record populated from the test conditions within the argument
     */
    public static GrpcStreamConfig  from(QueryRecoveryTestResult recResult) {
        QueryRecoveryTestCase   recTestCase = recResult.recTestCase();
        
        return GrpcStreamConfig.from(recTestCase);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>GrpcStreamConfig</code> record populated with the configuration field values of 
     * the argument record.
     * </p>
     * 
     * @param recTestCase   test case record containing configuration field values
     * 
     * @return  a new <code>GrpcStreamConfig</code> record populated from the fields of the given argument
     */
    public static GrpcStreamConfig  from(QueryRecoveryTestCase recTestCase) {
        return new GrpcStreamConfig(
                recTestCase.enmStrmType(),
                recTestCase.bolDcmpEnbl(),
                recTestCase.szMStrmQryDom(),
                recTestCase.cntMStrmMaxStrms()
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
        
        if (obj instanceof GrpcStreamConfig rec) {
            boolean bolResult = (this.enmStrmType == rec.enmStrmType)
                    && (this.bolMStrmEnbl == rec.bolMStrmEnbl)
                    && (this.szMStrmQryDom == rec.szMStrmQryDom)
                    && (this.cntMStrmMaxStrms == rec.cntMStrmMaxStrms);
            
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
        
        ps.println(strPad + "gRPC stream type                 : " + this.enmStrmType);
        ps.println(strPad + "Multi-Streaming (MS) enabled     : " + this.bolMStrmEnbl);
        ps.println(strPad + "MS request domain size (PV-Secs) : " + this.szMStrmQryDom);
        ps.println(strPad + "MS maximum gRPC stream count     : " + this.cntMStrmMaxStrms);
    }
    
}
