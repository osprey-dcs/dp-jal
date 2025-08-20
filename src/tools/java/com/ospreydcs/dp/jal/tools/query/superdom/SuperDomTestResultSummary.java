/*
 * Project: dp-api-common
 * File:	SuperDomTestResultSummary.java
 * Package: com.ospreydcs.dp.jal.tools.query.superdom
 * Type: 	SuperDomTestResultSummary
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
 * @since Jun 30, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.superdom;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Collection;
import java.util.NoSuchElementException;

import com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Record used in summarizing a collection of super-domain processing test case evaluation results.
 * </p>
 * <p>
 * When a battery of test cases is performed for super-domain testing, this record is intended to hold a 
 * summary of the results. 
 * It essentially contains the statistical properties of <code>{@link SuperDomTestResult}</code> collections.
 * </p>
 * <p>
 * <p>Test Suites and Summaries</h2> 
 * As described in the <code>{@link SuperDomTestCase}</code> record document, super-domain test case evaluations
 * contain both the recovery and correlation of raw time-series data and the super-domain processing following
 * correlation.  See the record class documentation for more details.
 * </p>
 * </p>
 * The hierarchy for performing a battery of super domain test cases is as follows:
 * <ul>  
 * <li>The <code>{@link SuperdomTestSuiteConfig}</code> class generates a suite (battery) of test cases.</li> 
 * <li>The parameters for a super-domain test case are stored in a <code>{@link SuperDomTestCase}</code> record.</li>
 * <li>A super-domain test case evaluation is performed by method <code>{@link SuperDomTestCase#evaluate(QueryRequestRecoverer)}}</code>.</li>
 * <li>The results of a super-domain test case evaluation are stored in a <code>{@link SuperDomTestResult}</code> record.</li>
 * <li>A summary for a suite of super-domain test results is created by method <code>{@link SuperDomTestResultSummary#summarize(Collection)}</code>.</li>
 * <li>The summary for a suite of super-domain test results is stored in a <code>{@link SuperDomTestResultSummary}</code> record.</li>
 * </ul>
 * Use the <code>{@link #printOut(PrintStream, String)}</code> method to print out a text description of a record instance
 * once populated.
 * </p> 
 *
 * @param cntResultsTot         the total number of <code>SuperDomTestResult</code> instances used in the summary
 * @param cntRcvMsgsMin         the minimum number of <code>QueryData</code> messages received for request recoveries
 * @param cntRcvMsgsMax         the maximum number of <code>QueryData</code> messages received for request recoveries
 * @param cntRcvMsgsAvg         the average number of <code>QueryData</code> messages received for request recoveries
 * @param szRcvAllocMin         the minimum allocation size (in Bytes) for request recovery
 * @param szRcvAllocMax         the maximum allocation size (in Bytes) for request recovery
 * @param szRcvAllocAvg         the average allocation size (in Bytes) for request recovery
 * @param cntRawBlksClkMin      the minimum number of raw correlated data blocks using a sampling clock
 * @param cntRawBlksClkMax      the maximum number of raw correlated data blocks using a sampling clock
 * @param cntRawBlksClkAvg      the average number of raw correlated data blocks using a sampling clock
 * @param cntRawBlksTmsLstMin   the minimum number of raw correlated data blocks using an explicit timestamp list
 * @param cntRawBlksTmsLstMax   the maximum number of raw correlated data blocks using an explicit timestamp list
 * @param cntRawBlksTmsLstAvg   the average number of raw correlated data blocks using an explicit timestamp list
 * @param cntRatesRawDataGtAvg  the number of data rates greater than the average rate for recovery and correlation
 * @param cntRatesRawDataGtTgt  the number of data rates greater than the target rate for recovery and correlation
 * @param durRawDataPrcdMin     the minimum duration for raw time-series data recovery and correlation
 * @param durRawDataPrcdMax     the maximum duration for raw time-series data recovery and correlation
 * @param durRawDataPrcdAvg     the average duration for raw time-series data recovery and correlation
 * @param durRawDataPrcdStd     the standard deviation for durations for raw time-series data recovery and correlation
 * @param dblRateRawDataPrcdMin the minimum data rate for raw time-series data recovery and correlation
 * @param dblRateRawDataPrcdMax the maximum data rate for raw time-series data recovery and correlation
 * @param dblRateRawDataPrcdAvg the average data rate for raw time-series data recovery and correlation
 * @param dblRateRawDataPrcdStd the standard deviation for data rates for raw time-series data recovery and correlation
 * @param cntDisBlkTotMin       the minimum number of total disjoint blocks after super-domain processing
 * @param cntDisBlkTotMax       the maximum number of total disjoint blocks after super-domain processing
 * @param cntDisBlkTotAvg       the average number of total disjoint blocks after super-domain processing
 * @param cntDisRawBlksMin      the minimum number of disjoint raw data blocks after super-domain processing
 * @param cntDisRawBlksMax      the maximum number of disjoint raw data blocks after super-domain processing
 * @param cntDisRawBlksAvg      the average number of disjoint raw data blocks after super-domain processing
 * @param cntSupDomBlksMin      the minimum number of disjoint super-domain data blocks after super-domain processing
 * @param cntSupDomBlksMax      the maximum number of disjoint super-domain data blocks after super-domain processing
 * @param cntSupDomBlksAvg      the average number of disjoint super-domain data blocks after super-domain processing
 * @param cntRatesSupDomGtAvg   the number of super-domain processing data rates greater than the average rate
 * @param durSupDomPrcdMin      the minimum super-domain processing duration 
 * @param durSupDomPrcdMax      the maximum super-domain processing duration
 * @param durSupDomPrcdAvg      the average super-domain processing duration
 * @param durSupDomPrcdStd      the standard deviation for super-domain processing durations
 * @param dblRateSupDomPrcdMin  the minimum super-domain processing data rate
 * @param dblRateSupDomPrcdMax  the maximum super-domain processing data rate
 * @param dblRateSupDomPrcdAvg  the average super-domain processing data rate
 * @param dblRateSupDomPrcdStd  the standard deviation for super-domain processing data rates
 * 
 * @author Christopher K. Allen
 * @since Jun 30, 2025
 *
 */
public record SuperDomTestResultSummary(
        int         cntResultsTot,
        int         cntRcvMsgsMin,
        int         cntRcvMsgsMax,
        int         cntRcvMsgsAvg,
        long        szRcvAllocMin,
        long        szRcvAllocMax,
        long        szRcvAllocAvg,
        int         cntRawBlksTotMin,
        int         cntRawBlksTotMax,
        int         cntRawBlksTotAvg,
        int         cntRawBlksClkMin,
        int         cntRawBlksClkMax,
        int         cntRawBlksClkAvg,
        int         cntRawBlksTmsLstMin,
        int         cntRawBlksTmsLstMax,
        int         cntRawBlksTmsLstAvg,
        int         cntRatesRawDataGtAvg,
        int         cntRatesRawDataGtTgt,
        Duration    durRawDataPrcdMin,
        Duration    durRawDataPrcdMax,
        Duration    durRawDataPrcdAvg,
        Duration    durRawDataPrcdStd,
        double      dblRateRawDataPrcdMin,
        double      dblRateRawDataPrcdMax,
        double      dblRateRawDataPrcdAvg,
        double      dblRateRawDataPrcdStd,
        int         cntDisBlksTotMin,
        int         cntDisBlksTotMax,
        int         cntDisBlksTotAvg,
        int         cntDisRawBlksMin,
        int         cntDisRawBlksMax,
        int         cntDisRawBlksAvg,
        int         cntSupDomBlksMin,
        int         cntSupDomBlksMax,
        int         cntSupDomBlksAvg,
        int         cntRatesSupDomGtAvg,
        Duration    durSupDomPrcdMin,
        Duration    durSupDomPrcdMax,
        Duration    durSupDomPrcdAvg,
        Duration    durSupDomPrcdStd,
        double      dblRateSupDomPrcdMin,
        double      dblRateSupDomPrcdMax,
        double      dblRateSupDomPrcdAvg,
        double      dblRateSupDomPrcdStd
        ) 
{
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new <code>SuperDomTestResultSummary</code> instance with field populated by the given arguments.
     * </p>
     * 
     * @param cntResultsTot         the total number of <code>SuperDomTestResult</code> instances used in the summary
     * @param cntRcvMsgsMin         the minimum number of <code>QueryData</code> messages received for request recoveries
     * @param cntRcvMsgsMax         the maximum number of <code>QueryData</code> messages received for request recoveries
     * @param cntRcvMsgsAvg         the average number of <code>QueryData</code> messages received for request recoveries
     * @param szRcvAllocMin         the minimum allocation size (in Bytes) for request recovery
     * @param szRcvAllocMax         the maximum allocation size (in Bytes) for request recovery
     * @param szRcvAllocAvg         the average allocation size (in Bytes) for request recovery
     * @param cntRawBlksTotMin      the minimum number of raw correlated data blocks total (both clocked and timestamp list)
     * @param cntRawBlksTotMax      the maximum number of raw correlated data blocks total (both clocked and timestamp list)
     * @param cntRawBlksTotAvg      the average number of raw correlated data blocks total (both clocked and timestamp list)
     * @param cntRawBlksClkMin      the minimum number of raw correlated data blocks using a sampling clock
     * @param cntRawBlksClkMax      the maximum number of raw correlated data blocks using a sampling clock
     * @param cntRawBlksClkAvg      the average number of raw correlated data blocks using a sampling clock
     * @param cntRawBlksTmsLstMin   the minimum number of raw correlated data blocks using an explicit timestamp list
     * @param cntRawBlksTmsLstMax   the maximum number of raw correlated data blocks using an explicit timestamp list
     * @param cntRawBlksTmsLstAvg   the average number of raw correlated data blocks using an explicit timestamp list
     * @param cntRatesRawDataGtAvg  the number of data rates greater than the average rate for recovery and correlation
     * @param cntRatesRawDataGtTgt  the number of data rates greater than the target rate for recovery and correlation
     * @param durRawDataPrcdMin     the minimum duration for raw time-series data recovery and correlation
     * @param durRawDataPrcdMax     the maximum duration for raw time-series data recovery and correlation
     * @param durRawDataPrcdAvg     the average duration for raw time-series data recovery and correlation
     * @param durRawDataPrcdStd     the standard deviation for durations for raw time-series data recovery and correlation
     * @param dblRateRawDataPrcdMin the minimum data rate for raw time-series data recovery and correlation
     * @param dblRateRawDataPrcdMax the maximum data rate for raw time-series data recovery and correlation
     * @param dblRateRawDataPrcdAvg the average data rate for raw time-series data recovery and correlation
     * @param dblRateRawDataPrcdStd the standard deviation for data rates for raw time-series data recovery and correlation
     * @param cntDisBlkTotMin       the minimum number of total disjoint blocks after super-domain processing
     * @param cntDisBlkTotMax       the maximum number of total disjoint blocks after super-domain processing
     * @param cntDisBlkTotAvg       the average number of total disjoint blocks after super-domain processing
     * @param cntDisRawBlksMin      the minimum number of disjoint raw data blocks after super-domain processing
     * @param cntDisRawBlksMax      the maximum number of disjoint raw data blocks after super-domain processing
     * @param cntDisRawBlksAvg      the average number of disjoint raw data blocks after super-domain processing
     * @param cntSupDomBlksMin      the minimum number of disjoint super-domain data blocks after super-domain processing
     * @param cntSupDomBlksMax      the maximum number of disjoint super-domain data blocks after super-domain processing
     * @param cntSupDomBlksAvg      the average number of disjoint super-domain data blocks after super-domain processing
     * @param cntRatesSupDomGtAvg   the number of super-domain processing data rates greater than the average rate
     * @param durSupDomPrcdMin      the minimum super-domain processing duration 
     * @param durSupDomPrcdMax      the maximum super-domain processing duration
     * @param durSupDomPrcdAvg      the average super-domain processing duration
     * @param durSupDomPrcdStd      the standard deviation for super-domain processing durations
     * @param dblRateSupDomPrcdMin  the minimum super-domain processing data rate
     * @param dblRateSupDomPrcdMax  the maximum super-domain processing data rate
     * @param dblRateSupDomPrcdAvg  the average super-domain processing data rate
     * @param dblRateSupDomPrcdStd  the standard deviation for super-domain processing data rates
     * 
     * @return  new <code>SuperDomTestResultSummary</code> record with fields given by the above arguments
     */
    public static SuperDomTestResultSummary from( 
            int         cntResultsTot,
            int         cntRcvMsgsMin,
            int         cntRcvMsgsMax,
            int         cntRcvMsgsAvg,
            long        szRcvAllocMin,
            long        szRcvAllocMax,
            long        szRcvAllocAvg,
            int         cntRawBlksTotMin,
            int         cntRawBlksTotMax,
            int         cntRawBlksTotAvg,
            int         cntRawBlksClkMin,
            int         cntRawBlksClkMax,
            int         cntRawBlksClkAvg,
            int         cntRawBlksTmsLstMin,
            int         cntRawBlksTmsLstMax,
            int         cntRawBlksTmsLstAvg,
            int         cntRatesRawDataGtAvg,
            int         cntRatesRawDataGtTgt,
            Duration    durRawDataPrcdMin,
            Duration    durRawDataPrcdMax,
            Duration    durRawDataPrcdAvg,
            Duration    durRawDataPrcdStd,
            double      dblRateRawDataPrcdMin,
            double      dblRateRawDataPrcdMax,
            double      dblRateRawDataPrcdAvg,
            double      dblRateRawDataPrcdStd,
            int         cntDisBlksTotMin,
            int         cntDisBlksTotMax,
            int         cntDisBlksTotAvg,
            int         cntDisRawBlksMin,
            int         cntDisRawBlksMax,
            int         cntDisRawBlksAvg,
            int         cntSupDomBlksMin,
            int         cntSupDomBlksMax,
            int         cntSupDomBlksAvg,
            int         cntRatesSupDomGtAvg,
            Duration    durSupDomPrcdMin,
            Duration    durSupDomPrcdMax,
            Duration    durSupDomPrcdAvg,
            Duration    durSupDomPrcdStd,
            double      dblRateSupDomPrcdMin,
            double      dblRateSupDomPrcdMax,
            double      dblRateSupDomPrcdAvg,
            double      dblRateSupDomPrcdStd
            )
    {
        return new SuperDomTestResultSummary(
                cntResultsTot,
                cntRcvMsgsMin,
                cntRcvMsgsMax,
                cntRcvMsgsAvg,
                szRcvAllocMin,
                szRcvAllocMax,
                szRcvAllocAvg,
                cntRawBlksTotMin,
                cntRawBlksTotMax,
                cntRawBlksTotAvg,
                cntRawBlksClkMin,
                cntRawBlksClkMax,
                cntRawBlksClkAvg,
                cntRawBlksTmsLstMin,
                cntRawBlksTmsLstMax,
                cntRawBlksTmsLstAvg,
                cntRatesRawDataGtAvg,
                cntRatesRawDataGtTgt,
                durRawDataPrcdMin,
                durRawDataPrcdMax,
                durRawDataPrcdAvg,
                durRawDataPrcdStd,
                dblRateRawDataPrcdMin,
                dblRateRawDataPrcdMax,
                dblRateRawDataPrcdAvg,
                dblRateRawDataPrcdStd,
                cntDisBlksTotMin,
                cntDisBlksTotMax,
                cntDisBlksTotAvg,
                cntDisRawBlksMin,
                cntDisRawBlksMax,
                cntDisRawBlksAvg,
                cntSupDomBlksMin,
                cntSupDomBlksMax,
                cntSupDomBlksAvg,
                cntRatesSupDomGtAvg,
                durSupDomPrcdMin,
                durSupDomPrcdMax,
                durSupDomPrcdAvg,
                durSupDomPrcdStd,
                dblRateSupDomPrcdMin,
                dblRateSupDomPrcdMax,
                dblRateSupDomPrcdAvg,
                dblRateSupDomPrcdStd
                );
    }
    
    
    //
    // Record Resources
    //

    /** The target data rate for raw data recovery and correlation (MBps) */
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
     * Computes a summary of the given collection of evaluation results and returns them as a record.
     * </p>
     * <p>
     * All the fields of a <code>SuperDomTestResultSummary</code> record are computed from the given 
     * collection of test result records.  The computed values are then returned in a 
     * new <code>SuperDomTestResultSummary</code> instance.
     * </p>
     * <p>
     * Generally the statistics are computed for each record field within the argument collection.  The statistics
     * are up to second order including minimum values, maximum values, average values, and standard deviations
     * (standard deviations are included for durations and rates).
     * See the class documentation for <code>{@link SuperDomTestResultSummary}</code> for a description of each
     * field within the returned results summary.
     * </p> 
     *  
     * @param setResults    collection of super-domain test results
     * 
     * @return  a new <code>SuperDomTestResultSummary</code> record containing a summary of the argument results
     * 
     * @throws  NoSuchElementException    argument collection was empty
     */
    public static SuperDomTestResultSummary    summarize(Collection<SuperDomTestResult> setResults) throws NoSuchElementException {
        
        // Check argument - avoid divide by zero
        if (setResults.isEmpty())
            throw new NoSuchElementException(JavaRuntime.getQualifiedMethodNameSimple() + " - Argument collection was empty.");
        
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
        
        // Compute the total raw correlated block count statistics
        int         cntRawBlksTotAvg = setResults.stream().mapToInt(rec -> rec.cntRawBlksTotal()).sum()/cntResults;
        int         cntRawBlksTotMin = setResults.stream().mapToInt(rec -> rec.cntRawBlksTotal()).reduce(cntRawBlksTotAvg, (b1, b2) -> { if (b1<b2) return b1; else return b2; } );
        int         cntRawBlksTotMax = setResults.stream().mapToInt(rec -> rec.cntRawBlksTotal()).reduce(cntRawBlksTotMin, (b1, b2) -> { if (b1>b2) return b1; else return b2; } );
        
        // Compute the clocked raw correlated block count statistics
        int         cntRawBlksClkAvg = setResults.stream().mapToInt(rec -> rec.cntRawBlksClk()).sum()/cntResults;
        int         cntRawBlksClkMin = setResults.stream().mapToInt(rec -> rec.cntRawBlksClk()).reduce(cntRawBlksClkAvg, (b1, b2) -> { if (b1<b2) return b1; else return b2; } );
        int         cntRawBlksClkMax = setResults.stream().mapToInt(rec -> rec.cntRawBlksClk()).reduce(cntRawBlksClkMin, (b1, b2) -> { if (b1>b2) return b1; else return b2; } );
        
        // Compute the timestamp list raw correlated block count statistics
        int         cntRawBlksTmsLstAvg = setResults.stream().mapToInt(rec -> rec.cntRawBlksTmsLst()).sum()/cntResults;
        int         cntRawBlksTmsLstMin = setResults.stream().mapToInt(rec -> rec.cntRawBlksTmsLst()).reduce(cntRawBlksTmsLstAvg, (b1, b2) -> { if (b1<b2) return b1; else return b2; } );
        int         cntRawBlksTmsLstMax = setResults.stream().mapToInt(rec -> rec.cntRawBlksTmsLst()).reduce(cntRawBlksTmsLstMin, (b1, b2) -> { if (b1>b2) return b1; else return b2; } );
        
        // Compute the request recovery and correlation duration summary results
        Duration    durRawDataPrcdAvg = setResults.stream().<Duration>map(rec -> rec.durRawDataPrcd()).reduce(Duration.ZERO, (d1,d2) -> d1.plus(d2)).dividedBy(cntResults);
        Duration    durRawDataPrcdMin = setResults.stream().<Duration>map(rec -> rec.durRawDataPrcd()).reduce(durRawDataPrcdAvg, (d1, d2) -> { if (d1.compareTo(d2) < 0) return d1; else return d2; } );
        Duration    durRawDataPrcdMax = setResults.stream().<Duration>map(rec -> rec.durRawDataPrcd()).reduce(durRawDataPrcdAvg, (d1, d2) -> { if (d1.compareTo(d2) > 0) return d1; else return d2; } );
        
        double      dblPrcdNsSqrd = setResults.stream().<Duration>map(rec -> rec.durRawDataPrcd()).mapToLong(dur -> dur.toNanos()).mapToDouble(l -> Long.valueOf(l).doubleValue()).map(ns -> ns*ns).sum()/cntResults;
        double      dblRawDataPrcdNsAvg = Long.valueOf(durRawDataPrcdAvg.toNanos() ).doubleValue();
        double      dblRawDataPrcdNsStd = Math.sqrt(dblPrcdNsSqrd - dblRawDataPrcdNsAvg*dblRawDataPrcdNsAvg);
        Duration    durRawDataPrcdStd = Duration.ofNanos(Double.valueOf(dblRawDataPrcdNsStd).longValue());
        
        // Compute the raw time-series data recovery and correlation rates statistics
        double      dblRateRawDataPrcdAvg = setResults.stream().mapToDouble(rec -> rec.dblRateRawDataPrcd()).sum()/cntResults;
        int         cntRatesRawDataGtAvg = setResults.stream().filter(rec -> rec.dblRateRawDataPrcd() >= dblRateRawDataPrcdAvg).mapToInt(rec -> 1).sum();
        int         cntRatesRawDataGtTgt = setResults.stream().filter(rec -> rec.dblRateRawDataPrcd() >= DBL_RATE_TARGET).mapToInt(rec -> 1).sum();
        
        double      dblRateRawDataPrcdMin = setResults.stream().mapToDouble(rec -> rec.dblRateRawDataPrcd()).reduce(dblRateRawDataPrcdAvg, (r1, r2) -> { if (r1<r2) return r1; else return r2; } );
        double      dblRateRawDataPrcdMax = setResults.stream().mapToDouble(rec -> rec.dblRateRawDataPrcd()).reduce(dblRateRawDataPrcdAvg, (r1, r2) -> { if (r1>r2) return r1; else return r2; } );
        double      dblRateRawDataPrcdSqrd = setResults.stream().mapToDouble(rec -> rec.dblRateRawDataPrcd()).map(r -> (r-dblRateRawDataPrcdAvg)*(r-dblRateRawDataPrcdAvg)).sum();
        double      dblRateRawDataPrcdStd = Math.sqrt(dblRateRawDataPrcdSqrd/cntResults); // compute standard deviation
     
        // Compute the total disjoint raw data blocks (after super-domain processing) statistics
        int         cntDisBlksTotAvg = setResults.stream().mapToInt(rec -> rec.cntDisBlksTotal()).sum()/cntResults;
        int         cntDisBlksTotMin = setResults.stream().mapToInt(rec -> rec.cntDisBlksTotal()).reduce(cntDisBlksTotAvg, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        int         cntDisBlksTotMax = setResults.stream().mapToInt(rec -> rec.cntDisBlksTotal()).reduce(cntDisBlksTotAvg, (n1, n2) -> { if (n1>n2) return n1; else return n2; } );

        // Compute the disjoint raw data blocks (after super-domain processing) statistics
        int         cntDisRawBlksAvg = setResults.stream().mapToInt(rec -> rec.cntDisRawBlks()).sum()/cntResults;
        int         cntDisRawBlksMin = setResults.stream().mapToInt(rec -> rec.cntDisRawBlks()).reduce(cntDisRawBlksAvg, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        int         cntDisRawBlksMax = setResults.stream().mapToInt(rec -> rec.cntDisRawBlks()).reduce(cntDisRawBlksAvg, (n1, n2) -> { if (n1>n2) return n1; else return n2; } );
        
        // Compute the super domain blocks (after super-domain processing) statistics
        int         cntSupDomBlksAvg = setResults.stream().mapToInt(rec -> rec.cntSupDomBlks()).sum()/cntResults;
        int         cntSupDomBlksMin = setResults.stream().mapToInt(rec -> rec.cntSupDomBlks()).reduce(cntSupDomBlksAvg, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        int         cntSupDomBlksMax = setResults.stream().mapToInt(rec -> rec.cntSupDomBlks()).reduce(cntSupDomBlksAvg, (n1, n2) -> { if (n1>n2) return n1; else return n2; } );

        // Compute the super-domain processing duration summary results
        Duration    durSupDomPrcdAvg = setResults.stream().<Duration>map(rec -> rec.durSupDomPrcd()).reduce(Duration.ZERO, (d1,d2) -> d1.plus(d2)).dividedBy(cntResults);
        Duration    durSupDomPrcdMin = setResults.stream().<Duration>map(rec -> rec.durSupDomPrcd()).reduce(durSupDomPrcdAvg, (d1, d2) -> { if (d1.compareTo(d2) < 0) return d1; else return d2; } );
        Duration    durSupDomPrcdMax = setResults.stream().<Duration>map(rec -> rec.durSupDomPrcd()).reduce(durSupDomPrcdAvg, (d1, d2) -> { if (d1.compareTo(d2) > 0) return d1; else return d2; } );
        
        double      dblSupDomPrcdNsSqrd = setResults.stream().<Duration>map(rec -> rec.durSupDomPrcd()).mapToLong(dur -> dur.toNanos()).mapToDouble(l -> Long.valueOf(l).doubleValue()).map(ns -> ns*ns).sum()/cntResults;
        double      dblSupDomPrcdNsAvg = Long.valueOf(durSupDomPrcdAvg.toNanos() ).doubleValue();
        double      dblSupDomPrcdNsStd = Math.sqrt(dblSupDomPrcdNsSqrd - dblSupDomPrcdNsAvg*dblSupDomPrcdNsAvg);
        Duration    durSupDomPrcdStd = Duration.ofNanos(Double.valueOf(dblSupDomPrcdNsStd).longValue());
        
        // Compute the super-domain processing rates statistics
        double      dblRateSupDomPrcdAvg = setResults.stream().mapToDouble(rec -> rec.dblRateSupDomPrcd()).sum()/cntResults;
        int         cntRatesSupDomGtAvg = setResults.stream().filter(rec -> rec.dblRateSupDomPrcd() >= dblRateSupDomPrcdAvg).mapToInt(rec -> 1).sum();
        
        double      dblRateSupDomPrcdMin = setResults.stream().mapToDouble(rec -> rec.dblRateSupDomPrcd()).reduce(dblRateSupDomPrcdAvg, (r1, r2) -> { if (r1<r2) return r1; else return r2; } );
        double      dblRateSupDomPrcdMax = setResults.stream().mapToDouble(rec -> rec.dblRateSupDomPrcd()).reduce(dblRateSupDomPrcdAvg, (r1, r2) -> { if (r1>r2) return r1; else return r2; } );
        double      dblRateSupDomPrcdSqrd = setResults.stream().mapToDouble(rec -> rec.dblRateSupDomPrcd()).map(r -> (r-dblRateSupDomPrcdAvg)*(r-dblRateSupDomPrcdAvg)).sum();
        double      dblRateSupDomPrcdStd = Math.sqrt(dblRateSupDomPrcdSqrd/cntResults); // compute standard deviation
     

        // Create the summary record and return it
        SuperDomTestResultSummary   recSummary = SuperDomTestResultSummary.from(
                cntResults, 
                cntMsgsMin, cntMsgsMax, cntMsgsAvg, 
                szAllocMin, szAllocMax, szAllocAvg, 
                cntRawBlksTotMin, cntRawBlksTotMax, cntRawBlksTotAvg, 
                cntRawBlksClkMin, cntRawBlksClkMax, cntRawBlksClkAvg, 
                cntRawBlksTmsLstMin, cntRawBlksTmsLstMax, cntRawBlksTmsLstAvg, 
                cntRatesRawDataGtAvg, cntRatesRawDataGtTgt, 
                durRawDataPrcdMin, durRawDataPrcdMax, durRawDataPrcdAvg, durRawDataPrcdStd, 
                dblRateRawDataPrcdMin, dblRateRawDataPrcdMax, dblRateRawDataPrcdAvg, dblRateRawDataPrcdStd, 
                cntDisBlksTotMin, cntDisBlksTotMax, cntDisBlksTotAvg, 
                cntDisRawBlksMin, cntDisRawBlksMax, cntDisRawBlksAvg, 
                cntSupDomBlksMin, cntSupDomBlksMax, cntSupDomBlksAvg, 
                cntRatesSupDomGtAvg, 
                durSupDomPrcdMin, durSupDomPrcdMax, durSupDomPrcdAvg, durSupDomPrcdStd, 
                dblRateSupDomPrcdMin, dblRateSupDomPrcdMax, dblRateSupDomPrcdAvg, dblRateSupDomPrcdStd
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
        ps.println(strPad + "Tests Result Summary ");
        ps.println(strPad + "  Total number of result cases    : " + this.cntResultsTot);
        ps.println(strPad + "  Minimum number of messages recovered   : " + this.cntRcvMsgsMin);
        ps.println(strPad + "  Maximum number of messages recovered   : " + this.cntRcvMsgsMax);
        ps.println(strPad + "  Average number of messages recovered   : " + this.cntRcvMsgsAvg);
        ps.println(strPad + "  Minimum recovery allocation (bytes)    : " + this.szRcvAllocMin);
        ps.println(strPad + "  Maximum recovery allocation (bytes)    : " + this.szRcvAllocMax);
        ps.println(strPad + "  Average recovery allocation (bytes)    : " + this.szRcvAllocAvg);
        ps.println(strPad + "  Minimum total raw data block count     : " + this.cntRawBlksTotMin);
        ps.println(strPad + "  Maximum total raw data block count     : " + this.cntRawBlksTotMax);
        ps.println(strPad + "  Average total raw data block count     : " + this.cntRawBlksTotAvg);
        ps.println(strPad + "  Minimum clocked raw data block count   : " + this.cntRawBlksClkMin);
        ps.println(strPad + "  Maximum clocked raw data block count   : " + this.cntRawBlksClkMax);
        ps.println(strPad + "  Average clocked raw data block count   : " + this.cntRawBlksClkAvg);
        ps.println(strPad + "  Minimum timestamp list raw block count : " + this.cntRawBlksTmsLstMin);
        ps.println(strPad + "  Maximum timestamp list raw block count : " + this.cntRawBlksTmsLstMax);
        ps.println(strPad + "  Average timestamp list raw block count : " + this.cntRawBlksTmsLstAvg);
        ps.println(strPad + "  Minimum raw data and correlation duration : " + this.durRawDataPrcdMin);
        ps.println(strPad + "  Maximum raw data and correlation duration : " + this.durRawDataPrcdMax);
        ps.println(strPad + "  Average raw data and correlation duration : " + this.durRawDataPrcdAvg);
        ps.println(strPad + "  Minimum raw data and correlate data rate (MBps)   : " + this.dblRateRawDataPrcdMin);
        ps.println(strPad + "  Maximum raw data and correlate data rate (MBps)   : " + this.dblRateRawDataPrcdMax);
        ps.println(strPad + "  Average raw data and correlate data rate (MBps)   : " + this.dblRateRawDataPrcdAvg);
        ps.println(strPad + "  Raw data and correlate data rate std. (MBps)      : " + this.dblRateRawDataPrcdStd);
        ps.println(strPad + "  Raw data and correlate data rates > average       : " + this.cntRatesRawDataGtAvg);
        ps.println(strPad + "  Raw data and correlate data rates > target        : " + this.cntRatesRawDataGtTgt);
        ps.println(strPad + "  Minimum total disjoint data block count       : " + this.cntDisBlksTotMin);
        ps.println(strPad + "  Maximum total disjoint data block count       : " + this.cntDisBlksTotMax);
        ps.println(strPad + "  Average total disjoint data block count       : " + this.cntDisBlksTotAvg);
        ps.println(strPad + "  Minimum disjoint correl. raw data block count : " + this.cntDisBlksTotMin);
        ps.println(strPad + "  Maximum disjoint correl. raw data block count : " + this.cntDisBlksTotMax);
        ps.println(strPad + "  Average disjoint correl. raw data block count : " + this.cntDisBlksTotAvg);
        ps.println(strPad + "  Minimum disjoint super-domain block count     : " + this.cntSupDomBlksMin);
        ps.println(strPad + "  Maximum disjoint super-domain block count     : " + this.cntSupDomBlksMax);
        ps.println(strPad + "  Average disjoint super-domain block count     : " + this.cntSupDomBlksAvg);
        ps.println(strPad + "  Minimum super-domain processing duration      : " + this.durSupDomPrcdMin);
        ps.println(strPad + "  Maximum super-domain processing duration      : " + this.durSupDomPrcdMax);
        ps.println(strPad + "  Average super-domain processing duration      : " + this.durSupDomPrcdAvg);
        ps.println(strPad + "  Minimum super-domain processing rate (Blks/sec) : " + this.dblRateSupDomPrcdMin);
        ps.println(strPad + "  Maximum super-domain processing rate (Blks/sec) : " + this.dblRateSupDomPrcdMax);
        ps.println(strPad + "  Average super-domain processing rate (Blks/sec) : " + this.dblRateSupDomPrcdAvg);
        ps.println(strPad + "  Super-domain processing rates greater than avg. : " + this.cntRatesSupDomGtAvg);
    }
}
