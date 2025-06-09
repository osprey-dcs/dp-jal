/*
 * Project: dp-api-common
 * File:	CorrelatorTestResult.java
 * Package: com.ospreydcs.dp.api.tools.query.correl
 * Type: 	CorrelatorTestResult
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
 * @since May 30, 2025
 *
 */
package com.ospreydcs.dp.api.tools.query.correl;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Comparator;

/**
 * <p>
 * Record contains the results of a <code>CorrelatorTestCase</code> evaluation.
 * </p>
 *
 * @param cntRspMsgs    the number of <code>QueryData</code> response messages to process
 * @param szRspMsgs     the total memory allocation of the incoming response message set (in bytes)
 * @param cntCorrelSet  the final number raw correlated data blocks produced during processing
 * @param szProcessed   the total number of bytes processed 
 * @param durProcessed  the time taken to process the incoming <code>QueryData</code> message set 
 * @param dblDataRate   the data processing rate achieved in evaluation
 * @param recTestCase   the test case record for the current result
 * 
 * @author Christopher K. Allen
 * @since May 30, 2025
 *
 */
public record CorrelatorTestResult(
        int                 cntRspMsgs,
        long                szRspMsgs,
        int                 cntCorrelSet,
        long                szProcessed,
        Duration            durProcessed,
        double              dblDataRate,
        CorrelatorTestCase  recTestCase
        ) 
{

    //
    // Creator
    //
    
    /**
     * <p>
     * Returns a new <code>CorrelatorTestResult</code> record populated with the given arguments.
     * </p>
     * <p>
     * The returned record is assumed to be created by the 
     * <code>{@link CorrelatorTestCase#evaluate(com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator, java.util.List)</code> 
     * method.
     * </p>
     * 
     * @param cntRspMsgs    the number of <code>QueryData</code> response messages to process
     * @param szRspMsgs     the total memory allocation of the incoming response message set (in bytes)
     * @param cntCorrelSet  the final number raw correlated data blocks produced during processing
     * @param szProcessed   the total number of bytes processed 
     * @param durProcessed  the time taken to process the incoming <code>QueryData</code> message set 
     * @param dblDataRate   the data processing rate achieved in evaluation
     * @param recTestCase   the test case record for the current result
     * 
     * @return  a new <code>CorrelatorTestResult</code> record populated with the given argument values
     */
    public static CorrelatorTestResult  from(
            int                 cntRspMsgs,
            long                szRspMsgs,
            int                 cntCorrelSet,
            long                szProcessed,
            Duration            durProcessed,
            double              dblDataRate,
            CorrelatorTestCase  recTestCase
            )
    {
        return new CorrelatorTestResult(cntRspMsgs, szRspMsgs, cntCorrelSet, szProcessed, durProcessed, dblDataRate, recTestCase);
    }
    
    
    //
    // Tools
    //
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> provide a descending (reverse) ordering according to data rates.
     * </p>
     * <p>
     * The returned comparator instance compares the <code>{@link #dblDataRate}</code> fields of two 
     * <code>CorrelatorTestResult</code> records.  It provides a reverse ordering of records according
     * to the data rate fields.  Specifically, the highest data rate will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * Note that the comparator provided here is the complement of the natural order of 
     * <code>CorrelatorTestResult</code> records provided by the exposed <code>Comparable</code> interface.
     * </p>  
     * 
     * @return  a new <code>Comparator</code> instance providing a reverse ordering by record data rates
     */
    public static Comparator<CorrelatorTestResult>   descendingRateOrdering() {
    
        Comparator<CorrelatorTestResult>   cmp = (r1, r2) -> {

            if (r1.dblDataRate > r2.dblDataRate)
                return -1;
            else
                return +1;
        };
        
        return cmp;
    }
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> provide an ascending (natural) ordering according to data rates.
     * </p>
     * <p>
     * The returned comparator instance compares the <code>{@link #dblDataRate}</code> fields of two 
     * <code>CorrelatorTestResult</code> records.  It provides a natural ordering of records according
     * to the data rate fields.  Specifically, the lowest data rate will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * Note that the comparator provided here is the equivalent of the natural order of 
     * <code>CorrelatorTestResult</code> records provided by the exposed <code>Comparable</code> interface.
     * </p>  
     * 
     * @return  a new <code>Comparator</code> instance providing a natural ordering by record data rates
     */
    public static Comparator<CorrelatorTestResult>    ascendingRateOrdering() {

        Comparator<CorrelatorTestResult>  cmp = (r1, r2) -> {

            if (r1.dblDataRate < r2.dblDataRate)
                return -1;
            else
                return +1;
        };
        
        return cmp;  
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
        
        ps.println(strPad + "Results of Test Case #" + this.recTestCase.indCase());
        ps.println(strPad + "  Processing data rate (MBps)  : " + this.dblDataRate);
        ps.println(strPad + "  Incoming message count       : " + this.cntRspMsgs);
        ps.println(strPad + "  Incoming message total bytes : " + this.szRspMsgs);
        ps.println(strPad + "  Correlated blocks processed  : " + this.cntCorrelSet);
        ps.println(strPad + "  Total bytes processed        : " + this.szProcessed);
        ps.println(strPad + "  Processing duration          : " + this.durProcessed);
        ps.println(strPad + "  Test Case Parameters:");
        this.recTestCase.printOut(ps, strPad + "  ");
    }

}
