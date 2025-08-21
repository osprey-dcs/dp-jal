/*
 * Project: dp-api-common
 * File:	ChannelConfig.java
 * Package: com.ospreydcs.dp.jal.tools.query.channel
 * Type: 	ChannelConfig
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

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;

/**
 * <p>
 * Record containing the configuration parameters from the <code>QueryChannelTestCase</code> record.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Aug 20, 2025
 *
 */
public record ChannelConfig(
        RequestDecompType   enmDcmpType, 
        DpGrpcStreamType    enmStrmType, 
        int                 cntStrms 
        ) 
{

    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>ChannelConfig</code> record populated with the test configuration within
     * argument result record.
     * </p>
     * <p>
     * The method extracts field <code>{@link QueryChannelTestResult#recTestCase()}</code> then defers to method
     * <code>{@link #from(QueryChannelTestCase)}</code>.
     * </p> 
     * 
     * @param recResult the test result record containing the test conditions
     * 
     * @return  a new <code>ChannelConfig</code> record populated from the test conditions within the argument
     */
    public static ChannelConfig from(QueryChannelTestResult recResult) {
        QueryChannelTestCase   recTestCase = recResult.recTestCase();
        
        return ChannelConfig.from(recTestCase);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ChannelConfig</code> record populated with the configuration field values of 
     * the argument record.
     * </p>
     * 
     * @param recTestCase   test case record containing configuration field values
     * 
     * @return  a new <code>ChannelConfig</code> record populated from the fields of the given argument
     */
    public static ChannelConfig from(QueryChannelTestCase recTestCase) {
        return new ChannelConfig(
                recTestCase.enmDcmpType(),
                recTestCase.enmStrmType(),
                recTestCase.cntStrms()
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
        
        if (obj instanceof ChannelConfig cfg) {
            boolean bolResult = (this.enmDcmpType == cfg.enmDcmpType)
                    && (this.enmStrmType == cfg.enmStrmType)
                    && (this.cntStrms == cfg.cntStrms);
            
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
        
        ps.println(strPad + "Decomposition strategy     : " + this.enmDcmpType);
        ps.println(strPad + "gRPC stream type           : " + this.enmStrmType);
        ps.print(  strPad + "gRPC stream count          : " + this.cntStrms);
        if (this.enmDcmpType == RequestDecompType.NONE)
            ps.println(" (decomposition NONE, only 1 used)");
        else
            ps.println();
    }
    
}
