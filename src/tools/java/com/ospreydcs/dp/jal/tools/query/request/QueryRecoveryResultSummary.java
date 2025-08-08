/*
 * Project: dp-api-common
 * File:	QueryRecoveryResultSummary.java
 * Package: com.ospreydcs.dp.jal.tools.query.request
 * Type: 	QueryRecoveryResultSummary
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
 * @since Jul 14, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.request;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Collection;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
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
 * Instances of <code>QueryRecoveryResultSummary</code> are intended to be created by method 
 * <code>{@link #summarize(Collection)}</code> where the argument is collection of <code>QueryRecoveryResult</code>
 * records under inspection.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 14, 2025
 *
 * @param cntResultsTot total number of results within the original result collection
 * @param cntMsgsMin    the minimum number of data messages for a test case
 * @param cntMsgsMax    the maximum number of data messages for a test case
 * @param cntMsgsAvg    the average number of data messages per test case
 * @param szAllocMin    the minimum memory allocation (in bytes) for all incoming messages  
 * @param szAllocMax    the maximum memory allocation (in bytes) for all incoming messages 
 * @param szAllocAvg    average allocation size (in bytes) of all incoming data to be processed 
 * @param cntBlksTotMin the minimum number of processed correlated data blocks seen in a test
 * @param cntBlksTotMax the maximum number of processed correlated data blocks seen in a test
 * @param cntBlksTotAvg average number of processed correlated data blocks per test
 * @param cntBlksClkMin the minimum number of processed correlated data blocks using a sampling clock seen in a test
 * @param cntBlksClkMax the maximum number of processed correlated data blocks using a sampling clock seen in a test
 * @param cntBlksClkAvg average number of processed correlated data blocks using a sampling clock per test
 * @param cntBlksTmsLstMin the minimum number of processed correlated data blocks using a timestamp list seen in a test
 * @param cntBlksTmsLstMax the maximum number of processed correlated data blocks using a timestamp list seen in a test
 * @param cntBlksTmsLstAvg average number of processed correlated data blocks using a timestamp list per test
 * @param cntOrdered    number of results producing ordered raw correlated data blocks
 * @param cntDisTmDoms  number of results producing disjoint time domains
 * @param durPrcdMin    the minimum processing time duration observed for all test operations
 * @param durPrcdMax    the maximum processing time duration observed within the result collection
 * @param durPrcdAvg    the average processing time duration within the result collection
 * @param durPrcdStd    the request processing time standard deviation of the result collection
 * @param cntRatesGtAvg number of data rates greater than the average data rate
 * @param cntRatesGtTgt number of data rates greater than a predetermined target rate
 * @param dblRateMin    the minimum data rate seen in the result collection
 * @param dblRateMax    the maximum data rate seen in the result collection 
 * @param dblRateAvg    the average data rate within the result collection
 * @param dblRateStd    the data rate standard deviation of the result collection
 */
public record QueryRecoveryResultSummary(
        int         cntResultsTot,
        int         cntMsgsMin,
        int         cntMsgsMax,
        int         cntMsgsAvg,
        long        szAllocMin,
        long        szAllocMax,
        long        szAllocAvg,
        int         cntBlksTotMin,
        int         cntBlksTotMax,
        int         cntBlksTotAvg,
        int         cntBlksClkMin,
        int         cntBlksClkMax,
        int         cntBlksClkAvg,
        int         cntBlksTmsLstMin,
        int         cntBlksTmsLstMax,
        int         cntBlksTmsLstAvg,
        int         cntOrdered,
        int         cntDisTmDoms,
        Duration    durPrcdMin,
        Duration    durPrcdMax,
        Duration    durPrcdAvg,
        Duration    durPrcdStd,
        int         cntRatesGtAvg,
        int         cntRatesGtTgt,
        double      dblRateMin,
        double      dblRateMax,
        double      dblRateAvg,
        double      dblRateStd
        ) 
{

    //
    // Creators
    //
    
    /**
     * <p>
     * Creates an returns a new <code>QueryRecoveryResultSummary</code> record populated from the given arguments.
     * </p>
     * <p>
     * Note that all fields of the <code>QueryRecoveryResultSummary</code> record appear as arguments.  Thus, this
     * creator is equivalent to the canonical constructor.
     * </p>
     * 
     * @param cntResultsTot total number of results within the original result collection
     * @param cntMsgsMin    the minimum number of data messages for a test case
     * @param cntMsgsMax    the maximum number of data messages for a test case
     * @param cntMsgsAvg    the average number of data messages per test case
     * @param szAllocMin    the minimum memory allocation (in bytes) for all incoming messages  
     * @param szAllocMax    the maximum memory allocation (in bytes) for all incoming messages 
     * @param szAllocAvg    average allocation size (in bytes) of all incoming data to be processed 
     * @param cntBlksTotMin the minimum number of processed correlated data blocks seen in a test
     * @param cntBlksTotMax the maximum number of processed correlated data blocks seen in a test
     * @param cntBlksTotAvg average number of processed correlated data blocks per test
     * @param cntBlksClkMin the minimum number of processed correlated data blocks using a sampling clock seen in a test
     * @param cntBlksClkMax the maximum number of processed correlated data blocks using a sampling clock seen in a test
     * @param cntBlksClkAvg average number of processed correlated data blocks using a sampling clock per test
     * @param cntBlksTmsLstMin the minimum number of processed correlated data blocks using a timestamp list seen in a test
     * @param cntBlksTmsLstMax the maximum number of processed correlated data blocks using a timestamp list seen in a test
     * @param cntBlksTmsLstAvg average number of processed correlated data blocks using a timestamp list per test
     * @param cntOrdered    number of results producing ordered raw correlated data blocks
     * @param cntDisTmDoms  number of results producing disjoint time domains
     * @param durPrcdMin    the minimum processing time duration observed for all test operations
     * @param durPrcdMax    the maximum processing time duration observed within the result collection
     * @param durPrcdAvg    the average processing time duration within the result collection
     * @param durPrcdStd    the request processing time standard deviation of the result collection
     * @param cntRatesGtAvg number of data rates greater than the average data rate
     * @param cntRatesGtTgt number of data rates greater than a predetermined target rate
     * @param dblRateMin    the minimum data rate seen in the result collection
     * @param dblRateMax    the maximum data rate seen in the result collection 
     * @param dblRateAvg    the average data rate within the result collection
     * @param dblRateStd    the data rate standard deviation of the result collection
     * 
     * @return  a new <code>QueryRecoveryResultSummary</code> record populated with the given arguments
     */
    public static QueryRecoveryResultSummary from(
            int         cntResultsTot,
            int         cntMsgsMin,
            int         cntMsgsMax,
            int         cntMsgsAvg,
            long        szAllocMin,
            long        szAllocMax,
            long        szAllocAvg,
            int         cntBlksTotMin,
            int         cntBlksTotMax,
            int         cntBlksTotAvg,
            int         cntBlksClkMin,
            int         cntBlksClkMax,
            int         cntBlksClkAvg,
            int         cntBlksTmsLstMin,
            int         cntBlksTmsLstMax,
            int         cntBlksTmsLstAvg,
            int         cntOrdered,
            int         cntDisTmDoms,
            Duration    durPrcdMin,
            Duration    durPrcdMax,
            Duration    durPrcdAvg,
            Duration    durPrcdStd,
            int         cntRatesGtAvg,
            int         cntRatesGtTgt,
            double      dblRateMin,
            double      dblRateMax,
            double      dblRateAvg,
            double      dblRateStd
            ) 
    {
        return new QueryRecoveryResultSummary(
                cntResultsTot,
                cntMsgsMin,
                cntMsgsMax,
                cntMsgsAvg,
                szAllocMin,
                szAllocMax,
                szAllocAvg,
                cntBlksTotMin,
                cntBlksTotMax,
                cntBlksTotAvg,
                cntBlksClkMin,
                cntBlksClkMax,
                cntBlksClkAvg,
                cntBlksTmsLstMin,
                cntBlksTmsLstMax,
                cntBlksTmsLstAvg,
                cntOrdered,
                cntDisTmDoms,
                durPrcdMin,
                durPrcdMax,
                durPrcdAvg,
                durPrcdStd,
                cntRatesGtAvg,
                cntRatesGtTgt,
                dblRateMin,
                dblRateMax,
                dblRateAvg,
                dblRateStd
                );
    }
    
    
    //
    // Record Resources
    //

    /** The target data rate (MBps) */
    public static double  DBL_RATE_TARGET = 500.0;
    
    
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
     * Computes a summary of the performance results for the given collection and returns them.
     * </p>
     * <p>
     * All the fields of a <code>QueryRecoveryResultSummary</code> record are computed from the given 
     * collection of test result records.  The computed values are then returned in a 
     * new <code>QueryRecoveryResultSummary</code> instance.
     * </p>
     *  
     * @param setResults    collection of test results to summarize
     * 
     * @return  a new <code>QueryRecoveryResultSummary</code> record containing a summary of the argument results
     * 
     * @throws  IllegalArgumentException    argument collection was empty
     */
    public static QueryRecoveryResultSummary summarize(Collection<QueryRecoveryResult> setResults) throws IllegalArgumentException {
        
        // Check argument - avoid divide by zero
        if (setResults.isEmpty())
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Argument collection was empty.");
        
        // Get the results size 
        int         cntResults = setResults.size();
        
        // Compute the incoming test message count statistics
        int         cntMsgsAvg = setResults.stream().mapToInt(rec -> rec.cntRecoveryMsgs()).sum()/cntResults;
        int         cntMsgsMin = setResults.stream().mapToInt(rec -> rec.cntRecoveryMsgs()).reduce(cntMsgsAvg, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        int         cntMsgsMax = setResults.stream().mapToInt(rec -> rec.cntRecoveryMsgs()).reduce(cntMsgsAvg, (n1, n2) -> { if (n1>n2) return n1; else return n2; } );
        
        // Compute the incoming test message  memory size statistics
        long        szAllocAvg = setResults.stream().mapToLong(rec -> rec.szRecoveryAlloc()).sum()/cntResults;
        long        szAllocMin = setResults.stream().mapToLong(rec -> rec.szRecoveryAlloc()).reduce(szAllocAvg, (s1, s2) -> { if (s1<s2) return s1; else return s2; } );
        long        szAllocMax = setResults.stream().mapToLong(rec -> rec.szRecoveryAlloc()).reduce(szAllocAvg, (s1, s2) -> { if (s1>s2) return s1; else return s2; } );
        
        // Compute the total raw correlated data block statistics
        int         cntBlksTotAvg = setResults.stream().mapToInt(rec -> rec.cntRawBlksTotal()).sum()/cntResults;
        int         cntBlksTotMin = setResults.stream().mapToInt(rec -> rec.cntRawBlksTotal()).reduce(cntBlksTotAvg, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        int         cntBlksTotMax = setResults.stream().mapToInt(rec -> rec.cntRawBlksTotal()).reduce(cntBlksTotAvg, (n1, n2) -> { if (n1>n2) return n1; else return n2; } );
        
        // Compute the raw correlated clocked block count statistics
        int         cntBlksClkAvg = setResults.stream().mapToInt(rec -> rec.cntRawBlksClk()).sum()/cntResults;
        int         cntBlksClkMin = setResults.stream().mapToInt(rec -> rec.cntRawBlksClk()).reduce(cntBlksClkAvg, (b1, b2) -> { if (b1<b2) return b1; else return b2; } );
        int         cntBlksClkMax = setResults.stream().mapToInt(rec -> rec.cntRawBlksClk()).reduce(cntBlksClkMin, (b1, b2) -> { if (b1>b2) return b1; else return b2; } );
        
        // Compute the raw correlated timestamp list block count statistics
        int         cntBlksTmsLstAvg = setResults.stream().mapToInt(rec -> rec.cntRawBlksTmsLst()).sum()/cntResults;
        int         cntBlksTmsLstMin = setResults.stream().mapToInt(rec -> rec.cntRawBlksTmsLst()).reduce(cntBlksTmsLstAvg, (b1, b2) -> { if (b1<b2) return b1; else return b2; } );
        int         cntBlksTmsLstMax = setResults.stream().mapToInt(rec -> rec.cntRawBlksTmsLst()).reduce(cntBlksTmsLstAvg, (b1, b2) -> { if (b1>b2) return b1; else return b2; } );
        
        // Count the numbers of ordered and disjoint time domain results
        int         cntOrdered = setResults.stream().<ResultStatus>map(rec -> rec.recOrdering()).filter(ResultStatus::isSuccess).mapToInt(rec -> 1).sum();
        int         cntDisTmDoms = setResults.stream().<ResultStatus>map(rec -> rec.recDisTmDom()).filter(ResultStatus::isSuccess).mapToInt(rec -> 1).sum();
        
        // Compute the recovery duration summary results
        Duration    durPrcdAvg = setResults.stream().<Duration>map(rec -> rec.durRawDataPrcd()).reduce(Duration.ZERO, (d1,d2) -> d1.plus(d2)).dividedBy(cntResults);
        Duration    durPrcdMin = setResults.stream().<Duration>map(rec -> rec.durRawDataPrcd()).reduce(durPrcdAvg, (d1, d2) -> { if (d1.compareTo(d2) < 0) return d1; else return d2; } );
        Duration    durPrcdMax = setResults.stream().<Duration>map(rec -> rec.durRawDataPrcd()).reduce(durPrcdAvg, (d1, d2) -> { if (d1.compareTo(d2) > 0) return d1; else return d2; } );
        
        double      dblPrcdNsSqrd = setResults.stream().<Duration>map(rec -> rec.durRawDataPrcd()).mapToLong(dur -> dur.toNanos()).mapToDouble(l -> Long.valueOf(l).doubleValue()).map(ns -> ns*ns).sum()/cntResults;
        double      dblPrcdNsAvg = Long.valueOf(durPrcdAvg.toNanos() ).doubleValue();
        double      dblPrcdNsStd = Math.sqrt(dblPrcdNsSqrd - dblPrcdNsAvg*dblPrcdNsAvg);
        Duration    durPrcdStd = Duration.ofNanos(Double.valueOf(dblPrcdNsStd).longValue());
        
        // Compute the rates statistics
        double      dblRateAvg = setResults.stream().mapToDouble(rec -> rec.dblRateRawDataPrcd()).sum()/cntResults;
        int         cntRatesGtAvg = setResults.stream().filter(rec -> rec.dblRateRawDataPrcd() >= dblRateAvg).mapToInt(rec -> 1).sum();
        int         cntRatesGtTgt = setResults.stream().filter(rec -> rec.dblRateRawDataPrcd() >= DBL_RATE_TARGET).mapToInt(rec -> 1).sum();
        
        double      dblRateMin = setResults.stream().mapToDouble(rec -> rec.dblRateRawDataPrcd()).reduce(dblRateAvg, (r1, r2) -> { if (r1<r2) return r1; else return r2; } );
        double      dblRateMax = setResults.stream().mapToDouble(rec -> rec.dblRateRawDataPrcd()).reduce(dblRateAvg, (r1, r2) -> { if (r1>r2) return r1; else return r2; } );
        double      dblRateSqrd = setResults.stream().mapToDouble(rec -> rec.dblRateRawDataPrcd()).map(r -> (r-dblRateAvg)*(r-dblRateAvg)).sum();
        double      dblRateStd = Math.sqrt(dblRateSqrd/cntResults); // compute standard deviation
        
        
        // Create summary record and return it
        QueryRecoveryResultSummary   recSummary = QueryRecoveryResultSummary.from(
                cntResults, 
                cntMsgsMin,
                cntMsgsMax,
                cntMsgsAvg,
                szAllocMin,
                szAllocMax,
                szAllocAvg,
                cntBlksTotMin,
                cntBlksTotMax,
                cntBlksTotAvg,
                cntBlksClkMin,
                cntBlksClkMax,
                cntBlksClkAvg,
                cntBlksTmsLstMin,
                cntBlksTmsLstMax,
                cntBlksTmsLstAvg,
                cntOrdered,
                cntDisTmDoms,
                durPrcdMin,
                durPrcdMax,
                durPrcdAvg,
                durPrcdStd,
                cntRatesGtAvg, 
                cntRatesGtTgt, 
                dblRateMin, 
                dblRateMax, 
                dblRateAvg, 
                dblRateStd); 
        
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
        ps.println(strPad + "Query Recovery Result Summary ");
        ps.println(strPad + "  Total number of result cases    : " + this.cntResultsTot);
        ps.println(strPad + "  Minimum no. messages processed  : " + this.cntMsgsMin);
        ps.println(strPad + "  Maximum no. messages processed  : " + this.cntMsgsMax);
        ps.println(strPad + "  Average no. messages processed  : " + this.cntMsgsAvg);
        ps.println(strPad + "  Minimum allocation (MBytes)     : " + ((double)this.szAllocMin)/1.0e6);
        ps.println(strPad + "  Maximum allocation (MBytes)     : " + ((double)this.szAllocMax)/1.0e6);
        ps.println(strPad + "  Average allocation (MBytes)     : " + ((double)this.szAllocAvg)/1.0e6);
        ps.println(strPad + "  Minimum correlated block count  : " + this.cntBlksTotMin);
        ps.println(strPad + "  Maximum correlated block count  : " + this.cntBlksTotMax);
        ps.println(strPad + "  Average correlated block count  : " + this.cntBlksTotAvg);
        ps.println(strPad + "  Minimum clocked block count     : " + this.cntBlksClkMin);
        ps.println(strPad + "  Average clocked block count     : " + this.cntBlksClkAvg);
        ps.println(strPad + "  Maximum clocked block count     : " + this.cntBlksClkMax);
        ps.println(strPad + "  Minimum tms list block count    : " + this.cntBlksTmsLstMin);
        ps.println(strPad + "  Maximum tms list block count    : " + this.cntBlksTmsLstMax);
        ps.println(strPad + "  Average tms list block count    : " + this.cntBlksTmsLstAvg);
        ps.println(strPad + "  Number of ordered data sets     : " + this.cntOrdered);
        ps.println(strPad + "  Number of disjoint time domains : " + this.cntDisTmDoms);
        ps.println(strPad + "  Minimum processing duration     : " + this.durPrcdMin);
        ps.println(strPad + "  Maximum processing duration     : " + this.durPrcdMax);
        ps.println(strPad + "  Average processing duration     : " + this.durPrcdAvg);
        ps.println(strPad + "  Request processing stand. dev.  : " + this.durPrcdStd);
        ps.println(strPad + "  Cases w/ rates >= Avg Rate      : " + this.cntRatesGtAvg);
        ps.println(strPad + "  Cases w/ rates >= Target        : " + this.cntRatesGtTgt);
        ps.println(strPad + "  Target data rate (MBps)         : " + DBL_RATE_TARGET);
        ps.println(strPad + "  Minimum data rate (MBps)        : " + this.dblRateMin);
        ps.println(strPad + "  Maximum data rate (MBps)        : " + this.dblRateMax);
        ps.println(strPad + "  Average data rate (MBps)        : " + this.dblRateAvg);
        ps.println(strPad + "  Rate standard dev. (MBps)       : " + this.dblRateStd);
    }
    
}
