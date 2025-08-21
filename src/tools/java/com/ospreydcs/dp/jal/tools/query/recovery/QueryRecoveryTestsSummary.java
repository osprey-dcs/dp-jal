/*
 * Project: dp-api-common
 * File:	QueryRecoveryTestsSummary.java
 * Package: com.ospreydcs.dp.jal.tools.query.recovery
 * Type: 	QueryRecoveryTestsSummary
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
 * @since Aug 7, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.recovery;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Collection;
import java.util.function.Function;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.jal.tools.query.assem.QueryAssemblyTestResult;

/**
 *
 * <p>
 * Record containing the summary of a collection of <code>{@link QueryRecoveryResult}</code> records.
 * </p>
 * <p>
 * The record is used for computing a summary of the results for multiple time-series data request
 * recoveries.  The recovery operation includes both the gRPC data recover operation and the raw
 * data correlation of the response data.
 * </p>
 * <p>
 * The summary fields are generally statistical properties of the <code>QueryRecoveryResult</code> collection.
 * Specifically, they contain fields representing statistical elements of the <code>QueryRecoveryResult</code>
 * fields.
 * </p>
 * <p>
 * <h2>Creation</h2>
 * Instances of <code>QueryRecoveryTestsSummary</code> are intended to be created by method 
 * <code>{@link #summarize(Collection)}</code> where the argument is collection of <code>QueryRecoveryResult</code>
 * records under inspection.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 14, 2025
 *
 * @param cntResultsTot     total number of results within the original result collection
 * 
 * @param cntCmpRqstsMin    the minimum number of composite data requests 
 * @param cntCmpRqstsMax    the maximum number of composite data requests
 * @param dblCmpRqstsAvg    the average number of composite data requests
 * @param dblCmpRqstsStd    the composite request count standard deviation
 * 
 * @param cntRcvrdMsgsMin   the minimum number of data messages for a test case
 * @param cntRcvrdMsgsMax   the maximum number of data messages for a test case
 * @param cntRcvrdMsgsAvg   the average number of data messages per test case
 * @param szAllocPrcdMin    the minimum memory allocation (in bytes) for all incoming messages  
 * @param szAllocPrcdMax    the maximum memory allocation (in bytes) for all incoming messages 
 * @param szAllocPrcdAvg    average allocation size (in bytes) of all incoming data to be processed
 * 
 * @param durDataPrcdMin    the minimum processing time duration observed for all test operations
 * @param durDataPrcdMax    the maximum processing time duration observed within the result collection
 * @param durDataPrcdAvg    the average processing time duration within the result collection
 * @param durDataPrcdStd    the request processing time standard deviation of the result collection
 * @param cntRatesPrcdGtAvg number of data rates greater than the average data rate
 * @param cntRatesPrcdGtTgt number of data rates greater than a predetermined target rate
 * @param dblRatePrcdMin    the minimum data rate seen in the result collection
 * @param dblRatePrcdMax    the maximum data rate seen in the result collection 
 * @param dblRatePrcdAvg    the average data rate within the result collection
 * @param dblRatePrcdStd    the data rate standard deviation of the result collection
 *  
 * @param cntBlksPrcdTotMin     the minimum number of processed correlated data blocks seen in a test
 * @param cntBlksPrcdTotMax     the maximum number of processed correlated data blocks seen in a test
 * @param cntBlksPrcdTotAvg     average number of processed correlated data blocks per test
 * @param cntBlksPrcdClkdMin    the minimum number of processed correlated data blocks using a sampling clock seen in a test
 * @param cntBlksPrcdClkdMax    the maximum number of processed correlated data blocks using a sampling clock seen in a test
 * @param cntBlksPrcdClkdAvg    average number of processed correlated data blocks using a sampling clock per test
 * @param cntBlksPrcdTmsLstMin  the minimum number of processed correlated data blocks using a timestamp list seen in a test
 * @param cntBlksPrcdTmsLstMax  the maximum number of processed correlated data blocks using a timestamp list seen in a test
 * @param cntBlksPrcdTmsLstAvg  average number of processed correlated data blocks using a timestamp list per test
 * @param cntBlksOrdered        number of results producing ordered raw correlated data blocks
 * @param cntBlksDisTmDoms      number of results producing disjoint time domains
 */
public record QueryRecoveryTestsSummary(
        int         cntResultsTot,
        
        int         cntCmpRqstsMin,
        int         cntCmpRqstsMax,
        double      dblCmpRqstsAvg,
        double      dblCmpRqstsStd,
        
        int         cntRcvrdMsgsMin,
        int         cntRcvrdMsgsMax,
        int         cntRcvrdMsgsAvg,
        long        szAllocPrcdMin,
        long        szAllocPrcdMax,
        long        szAllocPrcdAvg,
        
        Duration    durDataPrcdMin,
        Duration    durDataPrcdMax,
        Duration    durDataPrcdAvg,
        Duration    durDataPrcdStd,
        int         cntRatesPrcdGtAvg,
        int         cntRatesPrcdGtTgt,
        double      dblRatePrcdMin,
        double      dblRatePrcdMax,
        double      dblRatePrcdAvg,
        double      dblRatePrcdStd,
        
        int         cntBlksPrcdTotMin,
        int         cntBlksPrcdTotMax,
        int         cntBlksPrcdTotAvg,
        int         cntBlksPrcdClkdMin,
        int         cntBlksPrcdClkdMax,
        int         cntBlksPrcdClkdAvg,
        int         cntBlksPrcdTmsLstMin,
        int         cntBlksPrcdTmsLstMax,
        int         cntBlksPrcdTmsLstAvg,
        int         cntBlksOrdered,
        int         cntBlksDisTmDoms
        ) 
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates an returns a new <code>QueryRecoveryTestsSummary</code> record populated from the given arguments.
     * </p>
     * <p>
     * Note that all fields of the <code>QueryRecoveryTestsSummary</code> record appear as arguments.  Thus, this
     * creator is equivalent to the canonical constructor.  It is primarily intended for internal use.
     * </p>
     * 
     * @param cntResultsTot     total number of results within the original result collection
     * 
     * @param cntCmpRqstsMin    the minimum number of composite data requests 
     * @param cntCmpRqstsMax    the maximum number of composite data requests
     * @param dblCmpRqstsAvg    the average number of composite data requests
     * @param dblCmpRqstsStd    the composite request count standard deviation
     * 
     * @param cntRcvrdMsgsMin   the minimum number of data messages for a test case
     * @param cntRcvrdMsgsMax   the maximum number of data messages for a test case
     * @param cntRcvrdMsgsAvg   the average number of data messages per test case
     * @param szAllocPrcdMin    the minimum memory allocation (in bytes) for all incoming messages  
     * @param szAllocPrcdMax    the maximum memory allocation (in bytes) for all incoming messages 
     * @param szAllocPrcdAvg    average allocation size (in bytes) of all incoming data to be processed
     * 
     * @param durDataPrcdMin    the minimum processing time duration observed for all test operations
     * @param durDataPrcdMax    the maximum processing time duration observed within the result collection
     * @param durDataPrcdAvg    the average processing time duration within the result collection
     * @param durDataPrcdStd    the request processing time standard deviation of the result collection
     * @param cntRatesPrcdGtAvg number of data rates greater than the average data rate
     * @param cntRatesPrcdGtTgt number of data rates greater than a predetermined target rate
     * @param dblRatePrcdMin    the minimum data rate seen in the result collection
     * @param dblRatePrcdMax    the maximum data rate seen in the result collection 
     * @param dblRatePrcdAvg    the average data rate within the result collection
     * @param dblRatePrcdStd    the data rate standard deviation of the result collection
     *  
     * @param cntBlksPrcdTotMin     the minimum number of processed correlated data blocks seen in a test
     * @param cntBlksPrcdTotMax     the maximum number of processed correlated data blocks seen in a test
     * @param cntBlksPrcdTotAvg     average number of processed correlated data blocks per test
     * @param cntBlksPrcdClkdMin    the minimum number of processed correlated data blocks using a sampling clock seen in a test
     * @param cntBlksPrcdClkdMax    the maximum number of processed correlated data blocks using a sampling clock seen in a test
     * @param cntBlksPrcdClkdAvg    average number of processed correlated data blocks using a sampling clock per test
     * @param cntBlksPrcdTmsLstMin  the minimum number of processed correlated data blocks using a timestamp list seen in a test
     * @param cntBlksPrcdTmsLstMax  the maximum number of processed correlated data blocks using a timestamp list seen in a test
     * @param cntBlksPrcdTmsLstAvg  average number of processed correlated data blocks using a timestamp list per test
     * @param cntBlksOrdered        number of results producing ordered raw correlated data blocks
     * @param cntBlksDisTmDoms      number of results producing disjoint time domains
     * 
     * @return  a new <code>QueryRecoveryTestsSummary</code> instance populated with the given argument values
     */
    public static QueryRecoveryTestsSummary from(
            int         cntResultsTot,
            
            int         cntCmpRqstsMin,
            int         cntCmpRqstsMax,
            double      dblCmpRqstsAvg,
            double      dblCmpRqstsStd,
            
            int         cntRcvrdMsgsMin,
            int         cntRcvrdMsgsMax,
            int         cntRcvrdMsgsAvg,
            long        szAllocPrcdMin,
            long        szAllocPrcdMax,
            long        szAllocPrcdAvg,
            
            Duration    durDataPrcdMin,
            Duration    durDataPrcdMax,
            Duration    durDataPrcdAvg,
            Duration    durDataPrcdStd,
            int         cntRatesPrcdGtAvg,
            int         cntRatesPrcdGtTgt,
            double      dblRatePrcdMin,
            double      dblRatePrcdMax,
            double      dblRatePrcdAvg,
            double      dblRatePrcdStd,
            
            int         cntBlksPrcdTotMin,
            int         cntBlksPrcdTotMax,
            int         cntBlksPrcdTotAvg,
            int         cntBlksPrcdClkdMin,
            int         cntBlksPrcdClkdMax,
            int         cntBlksPrcdClkdAvg,
            int         cntBlksPrcdTmsLstMin,
            int         cntBlksPrcdTmsLstMax,
            int         cntBlksPrcdTmsLstAvg,
            int         cntBlksOrdered,
            int         cntBlksDisTmDoms
            )
    {
        return new QueryRecoveryTestsSummary(
                cntResultsTot,
                
                cntCmpRqstsMin,
                cntCmpRqstsMax,
                dblCmpRqstsAvg,
                dblCmpRqstsStd,
                
                cntRcvrdMsgsMin,
                cntRcvrdMsgsMax,
                cntRcvrdMsgsAvg,
                szAllocPrcdMin,
                szAllocPrcdMax,
                szAllocPrcdAvg,
                
                durDataPrcdMin,
                durDataPrcdMax,
                durDataPrcdAvg,
                durDataPrcdStd,
                cntRatesPrcdGtAvg,
                cntRatesPrcdGtTgt,
                dblRatePrcdMin,
                dblRatePrcdMax,
                dblRatePrcdAvg,
                dblRatePrcdStd,
                
                cntBlksPrcdTotMin,
                cntBlksPrcdTotMax,
                cntBlksPrcdTotAvg,
                cntBlksPrcdClkdMin,
                cntBlksPrcdClkdMax,
                cntBlksPrcdClkdAvg,
                cntBlksPrcdTmsLstMin,
                cntBlksPrcdTmsLstMax,
                cntBlksPrcdTmsLstAvg,
                cntBlksOrdered,
                cntBlksDisTmDoms
                );
    }

    
    //
    // Record Constants
    //
    
    /** The format string used for printing out data rates from a test result collection */ 
    private static final String      STR_DATA_RATE_FMTR = "  %7.3f MBps for Case #%d with %7.3f MBytes allocation";
    
    /** The function that converts <code>QueryAssemblyTestResult</code> records to strings with data rate */
    private static final Function<QueryAssemblyTestResult, String>  FNC_DATA_RATE = rec -> {
        int     intIndex = rec.recTestCase().indCase();
        double  dblRate = rec.dblRateAggAssm();
        double  dblAlloc = ((double)rec.szAllocAggrAssm())/1_000_000;
        
        String  strLine = String.format(STR_DATA_RATE_FMTR, dblRate, intIndex, dblAlloc);
        
        return strLine;
    };
        
    //
    // Record Resources
    //

    /** The target data rate (MBps) */
    public static double  DBL_RATE_TARGET = 200.0;
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Assigns a new value to the target data rate.
     * </p>
     * <p>
     * The "target data rate" is a performance indicator, indicating the number of performance results where
     * the data rate was greater than or equal to this value.  The value is used to compute the field
     * <code>{@link #cntRatesGtTgt}</code> in the <code>{@link #summarize(Collection)}</code> operation.
     * </p>
     * 
     * @param dblRateTarget the target data rate in MBps
     */
    public static void assignTargetDataRate(double dblRateTarget) {
        DBL_RATE_TARGET = dblRateTarget;
    }
    
    
    /**
     * <p>
     * Prints out the line-by-line data rates for each test result in the argument collection.
     * </p>
     * <p>
     * A line-by-line text description of each record field <code>{@link QueryRecoveryTestResult#dblRateAggAssm()}</code>
     * is written to the given output.  The test case index and total allocation are also specified.
     * The <code>strPad</code> is used to supply an optional whitespace character padding to the
     * left-hand side header for each line description.
     * </p>
     *   
     * @param ps        output stream to receive text description of record fields
     * @param strPad    white space padding for left-hand side line headings (or <code>null</code>.
     * @param setResults    collection of test results
     */
    synchronized
    public static void printOutDataRates(PrintStream ps, String strPad, Collection<QueryRecoveryTestResult> setResults) {
        final String    STR_PAD = (strPad == null) ? "" : "  ";
        
        for (QueryRecoveryTestResult recResult : setResults) {
            int     intIndex = recResult.recTestCase().indCase();
            double  dblRate = recResult.dblRatePrcd();
            double  dblAlloc = ((double)recResult.szAllocPrcd())/1_000_000;

            String  strLine = String.format(STR_DATA_RATE_FMTR, dblRate, intIndex, dblAlloc);
            
            ps.println(STR_PAD + strLine);
        }
    }

    /**
     * <p>
     * Computes a summary of the evaluation results for the given collection and returns them.
     * </p>
     * <p>
     * All the fields of a <code>QueryRecoveryTestsSummary</code> record are computed from the given 
     * collection of test result records.  The computed values are then returned in a 
     * new <code>QueryRecoveryTestsSummary</code> instance.
     * </p>
     *  
     * @param setResults    collection of test results to summarize
     * 
     * @return  a new <code>QueryRecoveryTestsSummary</code> record containing a summary of the argument results
     * 
     * @throws  IllegalArgumentException    argument collection was empty
     */
    public static QueryRecoveryTestsSummary summarize(Collection<QueryRecoveryTestResult> setResults) throws IllegalArgumentException {
        
        // Check argument - avoid divide by zero
        if (setResults.isEmpty())
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Argument collection was empty.");
        
        // Get the results size 
        int         cntResults = setResults.size();
        
        // Compute the request decomposition statistics
        double      dblCmpRqstsAvg = setResults.stream().mapToDouble(rec -> rec.lstCmpRqsts().size()).sum()/cntResults;
        int         cntCmpRqstsMax = setResults.stream().mapToInt(rec -> rec.lstCmpRqsts().size()).reduce(0, (n1, n2) -> { if (n1>n2) return n1; else return n2; } );
        int         cntCmpRqstsMin = setResults.stream().mapToInt(rec -> rec.lstCmpRqsts().size()).reduce(cntCmpRqstsMax, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        
        double      dblCmpRqstsSqrd = setResults.stream().mapToDouble(rec -> rec.lstCmpRqsts().size()).map(n -> (n-dblCmpRqstsAvg)*(n-dblCmpRqstsAvg)).sum();
        double      dblCmpRqstsStd = Math.sqrt(dblCmpRqstsSqrd/cntResults);
        
        
        // Compute the incoming test message count statistics
        int         cntRcvrdMsgsAvg = setResults.stream().mapToInt(rec -> rec.cntRcvrdMsgs()).sum()/cntResults;
        int         cntRcvrdMsgsMin = setResults.stream().mapToInt(rec -> rec.cntRcvrdMsgs()).reduce(cntRcvrdMsgsAvg, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        int         cntRcvrdMsgsMax = setResults.stream().mapToInt(rec -> rec.cntRcvrdMsgs()).reduce(cntRcvrdMsgsAvg, (n1, n2) -> { if (n1>n2) return n1; else return n2; } );
        
        // Compute the incoming test message  memory size statistics
        long        szAllocPrcdAvg = setResults.stream().mapToLong(rec -> rec.szAllocPrcd()).sum()/cntResults;
        long        szAllocPrcdMin = setResults.stream().mapToLong(rec -> rec.szAllocPrcd()).reduce(szAllocPrcdAvg, (s1, s2) -> { if (s1<s2) return s1; else return s2; } );
        long        szAllocPrcdMax = setResults.stream().mapToLong(rec -> rec.szAllocPrcd()).reduce(szAllocPrcdAvg, (s1, s2) -> { if (s1>s2) return s1; else return s2; } );

        
        // Compute the data recovery/processing duration summary results
        Duration    durDataPrcdAvg = setResults.stream().<Duration>map(rec -> rec.durDataPrcd()).reduce(Duration.ZERO, (d1,d2) -> d1.plus(d2)).dividedBy(cntResults);
        Duration    durDataPrcdMin = setResults.stream().<Duration>map(rec -> rec.durDataPrcd()).reduce(durDataPrcdAvg, (d1, d2) -> { if (d1.compareTo(d2) < 0) return d1; else return d2; } );
        Duration    durDataPrcdMax = setResults.stream().<Duration>map(rec -> rec.durDataPrcd()).reduce(durDataPrcdAvg, (d1, d2) -> { if (d1.compareTo(d2) > 0) return d1; else return d2; } );
        
        double      dblDataPrcdNsSqrd = setResults.stream().<Duration>map(rec -> rec.durDataPrcd()).mapToLong(dur -> dur.toNanos()).mapToDouble(l -> Long.valueOf(l).doubleValue()).map(ns -> ns*ns).sum()/cntResults;
        double      dblDataPrcdNsAvg = Long.valueOf(durDataPrcdAvg.toNanos() ).doubleValue();
        double      dblDataPrcdNsStd = Math.sqrt(dblDataPrcdNsSqrd - dblDataPrcdNsAvg*dblDataPrcdNsAvg);
        Duration    durDataPrcdStd = Duration.ofNanos(Double.valueOf(dblDataPrcdNsStd).longValue());

        // Compute the data recovery/processing rates statistics
        double      dblRatePrcdAvg = setResults.stream().mapToDouble(rec -> rec.dblRatePrcd()).sum()/cntResults;
        int         cntRatesPrcdGtAvg = setResults.stream().filter(rec -> rec.dblRatePrcd() >= dblRatePrcdAvg).mapToInt(rec -> 1).sum();
        int         cntRatesPrcdGtTgt = setResults.stream().filter(rec -> rec.dblRatePrcd() >= DBL_RATE_TARGET).mapToInt(rec -> 1).sum();
        
        double      dblRatePrcdMin = setResults.stream().mapToDouble(rec -> rec.dblRatePrcd()).reduce(dblRatePrcdAvg, (r1, r2) -> { if (r1<r2) return r1; else return r2; } );
        double      dblRatePrcdMax = setResults.stream().mapToDouble(rec -> rec.dblRatePrcd()).reduce(dblRatePrcdAvg, (r1, r2) -> { if (r1>r2) return r1; else return r2; } );
        double      dblRatePrcdSqrd = setResults.stream().mapToDouble(rec -> rec.dblRatePrcd()).map(r -> (r-dblRatePrcdAvg)*(r-dblRatePrcdAvg)).sum();
        double      dblRatePrcdStd = Math.sqrt(dblRatePrcdSqrd/cntResults); // compute standard deviation
        
        
        // Compute the total raw correlated data block statistics
        int         cntBlksPrcdTotAvg = setResults.stream().mapToInt(rec -> rec.cntBlksPrcdTot()).sum()/cntResults;
        int         cntBlksPrcdTotMin = setResults.stream().mapToInt(rec -> rec.cntBlksPrcdTot()).reduce(cntBlksPrcdTotAvg, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        int         cntBlksPrcdTotMax = setResults.stream().mapToInt(rec -> rec.cntBlksPrcdTot()).reduce(cntBlksPrcdTotAvg, (n1, n2) -> { if (n1>n2) return n1; else return n2; } );
        
        // Compute the raw correlated clocked block count statistics
        int         cntBlksPrcdClkdAvg = setResults.stream().mapToInt(rec -> rec.cntBlksPrcdClkd()).sum()/cntResults;
        int         cntBlksPrcdClkdMin = setResults.stream().mapToInt(rec -> rec.cntBlksPrcdClkd()).reduce(cntBlksPrcdClkdAvg, (b1, b2) -> { if (b1<b2) return b1; else return b2; } );
        int         cntBlksPrcdClkdMax = setResults.stream().mapToInt(rec -> rec.cntBlksPrcdClkd()).reduce(cntBlksPrcdClkdMin, (b1, b2) -> { if (b1>b2) return b1; else return b2; } );
        
        // Compute the raw correlated timestamp list block count statistics
        int         cntBlksTmsLstAvg = setResults.stream().mapToInt(rec -> rec.cntBlksPrcdTmsLst()).sum()/cntResults;
        int         cntBlksTmsLstMin = setResults.stream().mapToInt(rec -> rec.cntBlksPrcdTmsLst()).reduce(cntBlksTmsLstAvg, (b1, b2) -> { if (b1<b2) return b1; else return b2; } );
        int         cntBlksTmsLstMax = setResults.stream().mapToInt(rec -> rec.cntBlksPrcdTmsLst()).reduce(cntBlksTmsLstAvg, (b1, b2) -> { if (b1>b2) return b1; else return b2; } );
        
        // Count the numbers of ordered and disjoint time domain results
        int         cntOrdered = setResults.stream().<ResultStatus>map(rec -> rec.recBlksOrdered()).filter(ResultStatus::isSuccess).mapToInt(rec -> 1).sum();
        int         cntDisTmDoms = setResults.stream().<ResultStatus>map(rec -> rec.recBlksDisTmDom()).filter(ResultStatus::isSuccess).mapToInt(rec -> 1).sum();
        
        
        // Create summary record and return it
        QueryRecoveryTestsSummary   recSummary = QueryRecoveryTestsSummary.from(
                cntResults, 
                cntCmpRqstsMin, cntCmpRqstsMax, dblCmpRqstsAvg, dblCmpRqstsStd,
                cntRcvrdMsgsMin, cntRcvrdMsgsMax, cntRcvrdMsgsAvg, 
                szAllocPrcdMin, szAllocPrcdMax, szAllocPrcdAvg, 
                durDataPrcdMin, durDataPrcdMax, durDataPrcdAvg, durDataPrcdStd, 
                cntRatesPrcdGtAvg, cntRatesPrcdGtTgt, 
                dblRatePrcdMin, dblRatePrcdMax, dblRatePrcdAvg, dblRatePrcdStd, 
                cntBlksPrcdTotMin, cntBlksPrcdTotMax, cntBlksPrcdTotAvg, 
                cntBlksPrcdClkdMin, cntBlksPrcdClkdMax, cntBlksPrcdClkdAvg, 
                cntBlksTmsLstMin, cntBlksTmsLstMax, cntBlksTmsLstAvg, 
                cntOrdered, cntDisTmDoms
                );
        
        return recSummary;
    }

    /**
     * <p>
     * Prints out a text description of the record contents to the given output.
     * </p>
     * <p>
     * A line-by-line text description of each record field is written to the given output.
     * The <code>strPad</code> is used to supply an optional whitespace character padding to the
     * left-hand side header for each line description.
     * </p>
     *   
     * @param ps        output stream to receive text description of record fields
     * @param strPad    white space padding for left-hand side line headings (or <code>null</code>.
     */ 
    public void printOut(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        
        // Print out results  
        ps.println(strPad + "Query Recovery Test Results Summary ");
        ps.println(strPad + "  Total number of result cases    : " + this.cntResultsTot);
        ps.println(strPad + "  Minimum composite request count : " + this.cntCmpRqstsMin);
        ps.println(strPad + "  Maximum composite request count : " + this.cntCmpRqstsMax);
        ps.println(strPad + "  Average composite request count : " + this.dblCmpRqstsAvg);
        ps.println(strPad + "  Composite request count std dev : " + this.dblCmpRqstsStd);
        ps.println(strPad + "  Minimum no. messages processed  : " + this.cntRcvrdMsgsMin);
        ps.println(strPad + "  Maximum no. messages processed  : " + this.cntRcvrdMsgsMax);
        ps.println(strPad + "  Average no. messages processed  : " + this.cntRcvrdMsgsAvg);
        ps.println(strPad + "  Minimum allocation (MBytes)     : " + ((double)this.szAllocPrcdMin)/1.0e6);
        ps.println(strPad + "  Maximum allocation (MBytes)     : " + ((double)this.szAllocPrcdMax)/1.0e6);
        ps.println(strPad + "  Average allocation (MBytes)     : " + ((double)this.szAllocPrcdAvg)/1.0e6);
        
        ps.println(strPad + "  Minimum processing duration     : " + this.durDataPrcdMin);
        ps.println(strPad + "  Maximum processing duration     : " + this.durDataPrcdMax);
        ps.println(strPad + "  Average processing duration     : " + this.durDataPrcdAvg);
        ps.println(strPad + "  Request processing stand. dev.  : " + this.durDataPrcdStd);
        ps.println(strPad + "  Cases w/ rates >= Avg Rate      : " + this.cntRatesPrcdGtAvg);
        ps.println(strPad + "  Cases w/ rates >= Target        : " + this.cntRatesPrcdGtTgt);
        ps.println(strPad + "  Target data rate (MBps)         : " + DBL_RATE_TARGET);
        ps.println(strPad + "  Minimum data rate (MBps)        : " + this.dblRatePrcdMin);
        ps.println(strPad + "  Maximum data rate (MBps)        : " + this.dblRatePrcdMax);
        ps.println(strPad + "  Average data rate (MBps)        : " + this.dblRatePrcdAvg);
        ps.println(strPad + "  Rate standard dev. (MBps)       : " + this.dblRatePrcdStd);
        
        ps.println(strPad + "  Minimum correlated block count  : " + this.cntBlksPrcdTotMin);
        ps.println(strPad + "  Maximum correlated block count  : " + this.cntBlksPrcdTotMax);
        ps.println(strPad + "  Average correlated block count  : " + this.cntBlksPrcdTotAvg);
        ps.println(strPad + "  Minimum clocked block count     : " + this.cntBlksPrcdClkdMin);
        ps.println(strPad + "  Average clocked block count     : " + this.cntBlksPrcdClkdAvg);
        ps.println(strPad + "  Maximum clocked block count     : " + this.cntBlksPrcdClkdMax);
        ps.println(strPad + "  Minimum tms list block count    : " + this.cntBlksPrcdTmsLstMin);
        ps.println(strPad + "  Maximum tms list block count    : " + this.cntBlksPrcdTmsLstMax);
        ps.println(strPad + "  Average tms list block count    : " + this.cntBlksPrcdTmsLstAvg);
        ps.println(strPad + "  No. of cases w/ ordered blocks  : " + this.cntBlksOrdered);
        ps.println(strPad + "  No. of cases w/ disjoint tm dom : " + this.cntBlksDisTmDoms);
    }
    

}
