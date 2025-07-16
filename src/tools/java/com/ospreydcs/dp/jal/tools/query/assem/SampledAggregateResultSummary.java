/*
 * Project: dp-api-common
 * File:	SampledAggregateResultSummary.java
 * Package: com.ospreydcs.dp.jal.tools.query.assem
 * Type: 	SampledAggregateResultSummary
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
package com.ospreydcs.dp.jal.tools.query.assem;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Collection;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Record containing a summary of a collection of <code>{@link SampledAggregateResult}</code> records.
 * </p>
 * <p>
 * The record primarily contains statistical properties for the <code>SampledAggregateResult</code> fields
 * within a given collection.
 * </p>
 * <p>
 * <h2>Creation</h2>
 * Instances of <code>SampledAggregateResultSummary</code> are intended for creation by static method
 * <code>{@link #summarize(Collection)}</code> where the argument is the target collection of 
 * <code>SampledAggregateResult</code> records.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 14, 2025
 *
 * @param cntResultsTot     the total number of result records used for the summary
 * @param cntSmpBlksTotMin  the minimum number of total sampled blocks within a <code>SampledAggregate</code>
 * @param cntSmpBlksTotMax  the maximum number of total sampled blocks within a <code>SampledAggregate</code>
 * @param cntSmpBlksTotAvg  the average number of total sampled blocks within a <code>SampledAggregate</code>
 * @param cntSmpBlksClkMin  the minimum number of <code>SampledBlockClocked</code> instances within a <code>SampledAggregate</code>
 * @param cntSmpBlksClkMax  the maximum number of <code>SampledBlockClocked</code> instances within a <code>SampledAggregate</code>
 * @param cntSmpBlksClkAvg  the average number of <code>SampledBlockClocked</code> instances within a <code>SampledAggregate</code>
 * @param cntSmpBlksTmsLstMin the minimum number of <code>SampledBlockTmsList</code> instances within a <code>SampledAggregate</code>
 * @param cntSmpBlksTmsLstMax the maximum number of <code>SampledBlockTmsList</code> instances within a <code>SampledAggregate</code>
 * @param cntSmpBlksTmsLstAvg the average number of <code>SampledBlockTmsList</code> instances within a <code>SampledAggregate</code>
 * @param szAllocRawMin     the minimum raw correlated data allocation size used to build a <code>SampledAggregate</code>
 * @param szAllocRawMax     the maximum raw correlated data allocation size used to build a <code>SampledAggregate</code>
 * @param szAllocRawAvg     the average raw correlated data allocation size used to build a <code>SampledAggregate</code>
 * @param szAllocJavaMin    the minimum Java heap allocation size used within a <code>SampledAggregate</code>
 * @param szAllocJavaMax    the maximum Java heap allocation size used within a <code>SampledAggregate</code>
 * @param szAllocJavaAvg    the average Java heap allocation size used within a <code>SampledAggregate</code>
 * @param cntTmsMin         the minimum number of timestamps within a <code>SampledAggregate</code>
 * @param cntTmsMax         the maximum number of timestamps within a <code>SampledAggregate</code>
 * @param cntTmsAvg         the average number of timestamps within a <code>SampledAggregate</code>
 * @param cntColsMin        the minimum number of data columns (time series) within a <code>SampledAggregate</code>
 * @param cntColsMax        the maximum number of data columns (time series) within a <code>SampledAggregate</code>
 * @param cntColsAvg        the average number of data columns (time series) within a <code>SampledAggregate</code>
 * @param cntOrdered        number of sampled aggregate results with ordered sampled blocks
 * @param cntDisTmDoms      number of sampled aggregate results with disjoint time domains sampled block
 * @param cntPvTypeCorrect  number of sampled aggregate results with consistent PV type time-series
 * @param durSmpAggPrcdMin  the minimum processing time required to build a <code>SampledAggregate</code>
 * @param durSmpAggPrcdMax  the maximum processing time required to build a <code>SampledAggregate</code>
 * @param durSmpAggPrcdAvg  the average processing time required to build a <code>SampledAggregate</code>
 * @param durSmpAggPrcdStd  the processing time standard deviation for building a <code>SampledAggregate</code>
 * @param cntRatesSmpAggGtAvg   the number of processing data rates greater than the average rate (for building <code>SampledAggregate</code>s)
 * @param dblRateSmpAggPrcdMin  the minimum processing data rate for building a <code>SampledAggregate</code>
 * @param dblRateSmpAggPrcdMax  the maximum processing data rate for building a <code>SampledAggregate</code>
 * @param dblRateSmpAggPrcdAvg  the average processing data rate for building a <code>SampledAggregate</code>
 * @param dblRateSmpAggPrcdStd  the processing data rate standard deviation for building <code>SampledAggregate</code>s
 */
public record SampledAggregateResultSummary(
        int         cntResultsTot,
        int         cntSmpBlksTotMin,
        int         cntSmpBlksTotMax,
        int         cntSmpBlksTotAvg,
        int         cntSmpBlksClkMin,
        int         cntSmpBlksClkMax,
        int         cntSmpBlksClkAvg,
        int         cntSmpBlksTmsLstMin,
        int         cntSmpBlksTmsLstMax,
        int         cntSmpBlksTmsLstAvg,
        int         cntSmpBlksSupDomMin,
        int         cntSmpBlksSupDomMax,
        int         cntSmpBlksSupDomAvg,
        long        szAllocRawMin,
        long        szAllocRawMax,
        long        szAllocRawAvg,
        long        szAllocJavaMin,
        long        szAllocJavaMax,
        long        szAllocJavaAvg,
        int         cntTmsMin,
        int         cntTmsMax,
        int         cntTmsAvg,
        int         cntColsMin,
        int         cntColsMax,
        int         cntColsAvg,
        int         cntOrdered,
        int         cntDisTmDoms,
        int         cntPvTypeCorrect,
        Duration    durSmpAggPrcdMin,
        Duration    durSmpAggPrcdMax,
        Duration    durSmpAggPrcdAvg,
        Duration    durSmpAggPrcdStd,
        int         cntRatesSmpAggGtAvg,
        double      dblRateSmpAggPrcdMin,
        double      dblRateSmpAggPrcdMax,
        double      dblRateSmpAggPrcdAvg,
        double      dblRateSmpAggPrcdStd
        ) 
{

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>SampledAggregateResultSummary</code> record with fields values given by the arguments.
     * </p>
     * 
     * @param cntResultsTot     the total number of result records used for the summary
     * @param cntSmpBlksTotMin  the minimum number of total sampled blocks within a <code>SampledAggregate</code>
     * @param cntSmpBlksTotMax  the maximum number of total sampled blocks within a <code>SampledAggregate</code>
     * @param cntSmpBlksTotAvg  the average number of total sampled blocks within a <code>SampledAggregate</code>
     * @param cntSmpBlksClkMin  the minimum number of <code>SampledBlockClocked</code> instances within a <code>SampledAggregate</code>
     * @param cntSmpBlksClkMax  the maximum number of <code>SampledBlockClocked</code> instances within a <code>SampledAggregate</code>
     * @param cntSmpBlksClkAvg  the average number of <code>SampledBlockClocked</code> instances within a <code>SampledAggregate</code>
     * @param cntSmpBlksTmsLstMin the minimum number of <code>SampledBlockTmsList</code> instances within a <code>SampledAggregate</code>
     * @param cntSmpBlksTmsLstMax the maximum number of <code>SampledBlockTmsList</code> instances within a <code>SampledAggregate</code>
     * @param cntSmpBlksTmsLstAvg the average number of <code>SampledBlockTmsList</code> instances within a <code>SampledAggregate</code>
     * @param cntSmpBlksSupDomMin the minimum number of <code>SampledBlockSuperDom</code> instances within a <code>SampledAggregate</code>
     * @param cntSmpBlksSupDomMax the maximum number of <code>SampledBlockSuperDom</code> instances within a <code>SampledAggregate</code>
     * @param cntSmpBlksSupDomAvg the average number of <code>SampledBlockSuperDom</code> instances within a <code>SampledAggregate</code>
     * @param szAllocRawMin     the minimum raw correlated data allocation size used to build a <code>SampledAggregate</code>
     * @param szAllocRawMax     the maximum raw correlated data allocation size used to build a <code>SampledAggregate</code>
     * @param szAllocRawAvg     the average raw correlated data allocation size used to build a <code>SampledAggregate</code>
     * @param szAllocJavaMin    the minimum Java heap allocation size used within a <code>SampledAggregate</code>
     * @param szAllocJavaMax    the maximum Java heap allocation size used within a <code>SampledAggregate</code>
     * @param szAllocJavaAvg    the average Java heap allocation size used within a <code>SampledAggregate</code>
     * @param cntTmsMin         the minimum number of timestamps within a <code>SampledAggregate</code>
     * @param cntTmsMax         the maximum number of timestamps within a <code>SampledAggregate</code>
     * @param cntTmsAvg         the average number of timestamps within a <code>SampledAggregate</code>
     * @param cntColsMin        the minimum number of data columns (time series) within a <code>SampledAggregate</code>
     * @param cntColsMax        the maximum number of data columns (time series) within a <code>SampledAggregate</code>
     * @param cntColsAvg        the average number of data columns (time series) within a <code>SampledAggregate</code>
     * @param cntOrdered        number of sampled aggregate results with ordered sampled blocks
     * @param cntDisTmDoms      number of sampled aggregate results with disjoint time domains sampled block
     * @param cntPvTypeCorrect  number of sampled aggregate results with consistent PV type time-series
     * @param durSmpAggPrcdMin  the minimum processing time required to build a <code>SampledAggregate</code>
     * @param durSmpAggPrcdMax  the maximum processing time required to build a <code>SampledAggregate</code>
     * @param durSmpAggPrcdAvg  the average processing time required to build a <code>SampledAggregate</code>
     * @param durSmpAggPrcdStd  the processing time standard deviation for building a <code>SampledAggregate</code>
     * @param cntRatesSmpAggGtAvg   the number of processing data rates greater than the average rate (for building <code>SampledAggregate</code>s)
     * @param dblRateSmpAggPrcdMin  the minimum processing data rate for building a <code>SampledAggregate</code>
     * @param dblRateSmpAggPrcdMax  the maximum processing data rate for building a <code>SampledAggregate</code>
     * @param dblRateSmpAggPrcdAvg  the average processing data rate for building a <code>SampledAggregate</code>
     * @param dblRateSmpAggPrcdStd  the processing data rate standard deviation for building <code>SampledAggregate</code>s
     * 
     * @return  a new <code>SampledAggregateResultSummary</code> record populated with the given argument values
     */
    public static SampledAggregateResultSummary from(
            int         cntResultsTot,
            int         cntSmpBlksTotMin,
            int         cntSmpBlksTotMax,
            int         cntSmpBlksTotAvg,
            int         cntSmpBlksClkMin,
            int         cntSmpBlksClkMax,
            int         cntSmpBlksClkAvg,
            int         cntSmpBlksTmsLstMin,
            int         cntSmpBlksTmsLstMax,
            int         cntSmpBlksTmsLstAvg,
            int         cntSmpBlksSupDomMin,
            int         cntSmpBlksSupDomMax,
            int         cntSmpBlksSupDomAvg,
            long        szAllocRawMin,
            long        szAllocRawMax,
            long        szAllocRawAvg,
            long        szAllocJavaMin,
            long        szAllocJavaMax,
            long        szAllocJavaAvg,
            int         cntTmsMin,
            int         cntTmsMax,
            int         cntTmsAvg,
            int         cntColsMin,
            int         cntColsMax,
            int         cntColsAvg,
            int         cntOrdered,
            int         cntDisTmDoms,
            int         cntPvTypeCorrect,
            Duration    durSmpAggPrcdMin,
            Duration    durSmpAggPrcdMax,
            Duration    durSmpAggPrcdAvg,
            Duration    durSmpAggPrcdStd,
            int         cntRatesSmpAggGtAvg,
            double      dblRateSmpAggPrcdMin,
            double      dblRateSmpAggPrcdMax,
            double      dblRateSmpAggPrcdAvg,
            double      dblRateSmpAggPrcdStd
            )
    {
        return new SampledAggregateResultSummary(
                cntResultsTot,
                cntSmpBlksTotMin,
                cntSmpBlksTotMax,
                cntSmpBlksTotAvg,
                cntSmpBlksClkMin,
                cntSmpBlksClkMax,
                cntSmpBlksClkAvg,
                cntSmpBlksTmsLstMin,
                cntSmpBlksTmsLstMax,
                cntSmpBlksTmsLstAvg,
                cntSmpBlksSupDomMin,
                cntSmpBlksSupDomMax,
                cntSmpBlksSupDomAvg,
                szAllocRawMin,
                szAllocRawMax,
                szAllocRawAvg,
                szAllocJavaMin,
                szAllocJavaMax,
                szAllocJavaAvg,
                cntTmsMin,
                cntTmsMax,
                cntTmsAvg,
                cntColsMin,
                cntColsMax,
                cntColsAvg,
                cntOrdered,
                cntDisTmDoms,
                cntPvTypeCorrect,
                durSmpAggPrcdMin,
                durSmpAggPrcdMax,
                durSmpAggPrcdAvg,
                durSmpAggPrcdStd,
                cntRatesSmpAggGtAvg,
                dblRateSmpAggPrcdMin,
                dblRateSmpAggPrcdMax,
                dblRateSmpAggPrcdAvg,
                dblRateSmpAggPrcdStd
                );
    }
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Computes a summary of the performance results for the given collection and returns them.
     * </p>
     * <p>
     * All the fields of a <code>SampledAggregateResultSummary</code> record are computed from the given 
     * collection of test result records.  The computed values are then returned in a 
     * new <code>SampledAggregateResultSummary</code> instance.
     * </p>
     *  
     * @param setResults    collection of test results to summarize
     * 
     * @return  a new <code>SampledAggregateResultSummary</code> record containing a summary of the argument results
     * 
     * @throws  IllegalArgumentException    argument collection was empty
     */
    public static SampledAggregateResultSummary summarize(Collection<SampledAggregateResult> setResults) throws IllegalArgumentException {
        
        // Check argument - avoid divide by zero
        if (setResults.isEmpty())
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Argument collection was empty.");
        
        // Get the results size 
        int         cntResults = setResults.size();
        
        // Compute the total sampled block count statistics
        int         cntSmpBlksTotAvg = setResults.stream().mapToInt(rec -> rec.cntSmpBlksTotal()).sum()/cntResults;
        int         cntSmpBlksTotMin = setResults.stream().mapToInt(rec -> rec.cntSmpBlksTotal()).reduce(cntSmpBlksTotAvg, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        int         cntSmpBlksTotMax = setResults.stream().mapToInt(rec -> rec.cntSmpBlksTotal()).reduce(cntSmpBlksTotAvg, (n1, n2) -> { if (n1>n2) return n1; else return n2; } );
        
        // Computed the clocked sampled block count statistics
        int         cntSmpBlksClkAvg = setResults.stream().mapToInt(rec -> rec.cntSmpBlksClk()).sum()/cntResults;
        int         cntSmpBlksClkMin = setResults.stream().mapToInt(rec -> rec.cntSmpBlksClk()).reduce(cntSmpBlksClkAvg, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        int         cntSmpBlksClkMax = setResults.stream().mapToInt(rec -> rec.cntSmpBlksClk()).reduce(cntSmpBlksClkAvg, (n1, n2) -> { if (n1>n2) return n1; else return n2; } );

        // Compute the timestamp list sampled block count statistics
        int         cntSmpBlksTmsLstAvg = setResults.stream().mapToInt(rec -> rec.cntSmpBlksTmsLst()).sum()/cntResults;
        int         cntSmpBlksTmsLstMin = setResults.stream().mapToInt(rec -> rec.cntSmpBlksTmsLst()).reduce(cntSmpBlksTmsLstAvg, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        int         cntSmpBlksTmsLstMax = setResults.stream().mapToInt(rec -> rec.cntSmpBlksTmsLst()).reduce(cntSmpBlksTmsLstAvg, (n1, n2) -> { if (n1>n2) return n1; else return n2; } );
        
        // Compute the super domain sampled block count statistics
        int         cntSmpBlksSupDomAvg = setResults.stream().mapToInt(rec -> rec.cntSmpBlksSupDom()).sum()/cntResults;
        int         cntSmpBlksSupDomMin = setResults.stream().mapToInt(rec -> rec.cntSmpBlksSupDom()).reduce(cntSmpBlksSupDomAvg, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        int         cntSmpBlksSupDomMax = setResults.stream().mapToInt(rec -> rec.cntSmpBlksSupDom()).reduce(cntSmpBlksSupDomAvg, (n1, n2) -> { if (n1>n2) return n1; else return n2; } );
        
        // Compute raw correlated data size statistics
        long        szAllocRawAvg = setResults.stream().mapToLong(rec -> rec.szAllocRaw()).sum()/cntResults;
        long        szAllocRawMin = setResults.stream().mapToLong(rec -> rec.szAllocRaw()).reduce(szAllocRawAvg, (s1, s2) -> { if (s1<s2) return s1; else return s2; } );
        long        szAllocRawMax = setResults.stream().mapToLong(rec -> rec.szAllocRaw()).reduce(szAllocRawAvg, (s1, s2) -> { if (s1>s2) return s1; else return s2; } );
        
        // Compute Java heap size statistics
        long        szAllocJavaAvg = setResults.stream().mapToLong(rec -> rec.szAllocJava()).sum()/cntResults;
        long        szAllocJavaMin = setResults.stream().mapToLong(rec -> rec.szAllocJava()).reduce(szAllocJavaAvg, (s1, s2) -> { if (s1<s2) return s1; else return s2; } );
        long        szAllocJavaMax = setResults.stream().mapToLong(rec -> rec.szAllocJava()).reduce(szAllocJavaAvg, (s1, s2) -> { if (s1>s2) return s1; else return s2; } );
        
        // Compute sampled count (timestamps) statistics
        int         cntTmsAvg = setResults.stream().mapToInt(rec -> rec.cntTms()).sum()/cntResults;
        int         cntTmsMin = setResults.stream().mapToInt(rec -> rec.cntTms()).reduce(cntTmsAvg, (t1, t2) -> { if (t1<t2) return t1; else return t2; } );
        int         cntTmsMax = setResults.stream().mapToInt(rec -> rec.cntTms()).reduce(cntTmsAvg, (t1, t2) -> { if (t1>t2) return t1; else return t2; } );
        
        // Compute data column count statistics
        int         cntColsAvg = setResults.stream().mapToInt(rec -> rec.cntPvs()).sum()/cntResults;
        int         cntColsMin = setResults.stream().mapToInt(rec -> rec.cntPvs()).reduce(cntColsAvg, (c1, c2) -> { if (c1<c2) return c1; else return c2; } );
        int         cntColsMax = setResults.stream().mapToInt(rec -> rec.cntPvs()).reduce(cntColsAvg, (c1, c2) -> { if (c1>c2) return c1; else return c2; } );
        
        // Get the sampled aggregate data verification counts
        int         cntOrdered = setResults.stream().<ResultStatus>map(rec -> rec.recOrdering()).filter(ResultStatus::isSuccess).mapToInt(r -> 1).sum();
        int         cntDisTmDoms = setResults.stream().<ResultStatus>map(rec -> rec.recDisTmDoms()).filter(ResultStatus::isSuccess).mapToInt(r -> 1).sum();
        int         cntPvTypeCorrect = setResults.stream().<ResultStatus>map(rec -> rec.recPvTypes()).filter(ResultStatus::isSuccess).mapToInt(r -> 1).sum();
        
        // Compute the sampled aggregate build duration summary results
        Duration    durSmpAggPrcdAvg = setResults.stream().<Duration>map(rec -> rec.durSmpAggPrcd()).reduce(Duration.ZERO, (d1,d2) -> d1.plus(d2)).dividedBy(cntResults);
        Duration    durSmpAggPrcdMin = setResults.stream().<Duration>map(rec -> rec.durSmpAggPrcd()).reduce(durSmpAggPrcdAvg, (d1, d2) -> { if (d1.compareTo(d2) < 0) return d1; else return d2; } );
        Duration    durSmpAggPrcdMax = setResults.stream().<Duration>map(rec -> rec.durSmpAggPrcd()).reduce(durSmpAggPrcdAvg, (d1, d2) -> { if (d1.compareTo(d2) > 0) return d1; else return d2; } );
        
        double      dblSmpAggPrcdNsSqrd = setResults.stream().<Duration>map(rec -> rec.durSmpAggPrcd()).mapToLong(dur -> dur.toNanos()).mapToDouble(l -> Long.valueOf(l).doubleValue()).map(ns -> ns*ns).sum()/cntResults;
        double      dblSmpAggPrcdNsAvg = Long.valueOf(durSmpAggPrcdAvg.toNanos() ).doubleValue();
        double      dblSmpAggPrcdNsStd = Math.sqrt(dblSmpAggPrcdNsSqrd - dblSmpAggPrcdNsAvg*dblSmpAggPrcdNsAvg);
        Duration    durSmpAggPrcdStd = Duration.ofNanos(Double.valueOf(dblSmpAggPrcdNsStd).longValue());
        
        // Compute the sampled aggregate build data rate statistics
        double      dblRateSmpAggPrcdAvg = setResults.stream().mapToDouble(rec -> rec.dblRateSmpAggPrcd()).sum()/cntResults;
        int         cntRatesSmpAggGtAvg = setResults.stream().filter(rec -> rec.dblRateSmpAggPrcd() >= dblRateSmpAggPrcdAvg).mapToInt(rec -> 1).sum();
        
        double      dblRateSmpAggPrcdMin = setResults.stream().mapToDouble(rec -> rec.dblRateSmpAggPrcd()).reduce(dblRateSmpAggPrcdAvg, (r1, r2) -> { if (r1<r2) return r1; else return r2; } );
        double      dblRateSmpAggPrcdMax = setResults.stream().mapToDouble(rec -> rec.dblRateSmpAggPrcd()).reduce(dblRateSmpAggPrcdAvg, (r1, r2) -> { if (r1>r2) return r1; else return r2; } );
        double      dblRateSmpAggPrcdSqrd = setResults.stream().mapToDouble(rec -> rec.dblRateSmpAggPrcd()).map(r -> (r-dblRateSmpAggPrcdAvg)*(r-dblRateSmpAggPrcdAvg)).sum();
        double      dblRateSmpAggPrcdStd = Math.sqrt(dblRateSmpAggPrcdSqrd/cntResults); // compute standard deviation
        
        
        // Create the summary record and return it
        return SampledAggregateResultSummary.from(
                cntResults, 
                cntSmpBlksTotMin, 
                cntSmpBlksTotMax, 
                cntSmpBlksTotAvg, 
                cntSmpBlksClkMin, 
                cntSmpBlksClkMax, 
                cntSmpBlksClkAvg, 
                cntSmpBlksTmsLstMin, 
                cntSmpBlksTmsLstMax, 
                cntSmpBlksTmsLstAvg, 
                cntSmpBlksSupDomMin, 
                cntSmpBlksSupDomMax, 
                cntSmpBlksSupDomAvg, 
                szAllocRawMin, 
                szAllocRawMax, 
                szAllocRawAvg, 
                szAllocJavaMin, 
                szAllocJavaMax, 
                szAllocJavaAvg, 
                cntTmsMin, 
                cntTmsMax, 
                cntTmsAvg, 
                cntColsMin, 
                cntColsMax, 
                cntColsAvg,
                cntOrdered,
                cntDisTmDoms,
                cntPvTypeCorrect,
                durSmpAggPrcdMin, 
                durSmpAggPrcdMax, 
                durSmpAggPrcdAvg, 
                durSmpAggPrcdStd, 
                cntRatesSmpAggGtAvg, 
                dblRateSmpAggPrcdMin, 
                dblRateSmpAggPrcdMax, 
                dblRateSmpAggPrcdAvg, 
                dblRateSmpAggPrcdStd
                );
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
        ps.println(strPad + "Sampled Aggregate Result Summary ");
        ps.println(strPad + "  Total number of result cases      : " + this.cntResultsTot);
        ps.println(strPad + "  Minimum total sampled block count : " + this.cntSmpBlksTotMin);
        ps.println(strPad + "  Maximum total sampled block count : " + this.cntSmpBlksTotMax);
        ps.println(strPad + "  Average total sampled block count : " + this.cntSmpBlksTotAvg);
        ps.println(strPad + "  Minimum clocked sampled block count        : " + this.cntSmpBlksClkMin);
        ps.println(strPad + "  Maximum clocked sampled block count        : " + this.cntSmpBlksClkMax);
        ps.println(strPad + "  Average clocked sampled block count        : " + this.cntSmpBlksClkAvg);
        ps.println(strPad + "  Minimum timestamp list sampled block count : " + this.cntSmpBlksTmsLstMin);
        ps.println(strPad + "  Maximum timestamp list sampled block count : " + this.cntSmpBlksTmsLstMax);
        ps.println(strPad + "  Average timestamp list sampled block count : " + this.cntSmpBlksTmsLstAvg);
        ps.println(strPad + "  Minimum super domain sampled block count   : " + this.cntSmpBlksTmsLstMin);
        ps.println(strPad + "  Maximum super domain sampled block count   : " + this.cntSmpBlksTmsLstMax);
        ps.println(strPad + "  Average super domain sampled block count   : " + this.cntSmpBlksTmsLstAvg);
        ps.println(strPad + "  Minimum raw data allocation (MBytes)       : " + ((double)this.szAllocRawMin)/1.0e6);
        ps.println(strPad + "  Maximum raw data allocation (MBytes)       : " + ((double)this.szAllocRawMax)/1.0e6);
        ps.println(strPad + "  Average raw data allocation (MBytes)       : " + ((double)this.szAllocRawAvg)/1.0e6);
        ps.println(strPad + "  Minimum Java heap allocation (MBytes)      : " + ((double)this.szAllocJavaMin)/1.0e6);
        ps.println(strPad + "  Maximum Java heap allocation (MBytes)      : " + ((double)this.szAllocJavaMax)/1.0e6);
        ps.println(strPad + "  Average Java heap allocation (MBytes)      : " + ((double)this.szAllocJavaAvg)/1.0e6);
        ps.println(strPad + "  Minimum number of timestamps w/in aggregate   : " + this.cntTmsMin);
        ps.println(strPad + "  Maximum number of timestamps w/in aggregate   : " + this.cntTmsMax);
        ps.println(strPad + "  Average number of timestamps w/in aggregate   : " + this.cntTmsAvg);
        ps.println(strPad + "  Minimum number of data columns w/in aggregate : " + this.cntColsMin);
        ps.println(strPad + "  Maximum number of data columns w/in aggregate : " + this.cntColsMax);
        ps.println(strPad + "  Average number of data columns w/in aggregate : " + this.cntColsAvg);
        ps.println(strPad + "  Cases with correct sampled block ordering     : " + this.cntOrdered);
        ps.println(strPad + "  Cases with sampled block disjoint time domain : " + this.cntDisTmDoms);
        ps.println(strPad + "  Cases with consistent PV data types           : " + this.cntPvTypeCorrect);
        ps.println(strPad + "  Minimum data processing duration for aggregate     : " + this.durSmpAggPrcdMin);
        ps.println(strPad + "  Maximum data processing duration for aggregate     : " + this.durSmpAggPrcdMax);
        ps.println(strPad + "  Average data processing duration for aggregate     : " + this.durSmpAggPrcdAvg);
        ps.println(strPad + "  Data processing stand. dev. to build aggregate     : " + this.durSmpAggPrcdStd);
        ps.println(strPad + "  Cases w/ rates >= Avg Rate                         : " + this.cntRatesSmpAggGtAvg);
        ps.println(strPad + "  Minimum data rate for aggregate build (MBps)       : " + this.dblRateSmpAggPrcdMin);
        ps.println(strPad + "  Maximum data rate for aggregate build (MBps)       : " + this.dblRateSmpAggPrcdMax);
        ps.println(strPad + "  Average data rate for aggregate build (MBps)       : " + this.dblRateSmpAggPrcdAvg);
        ps.println(strPad + "  Data rate standard dev. for aggregate build (MBps) : " + this.dblRateSmpAggPrcdStd);
    }
    
}
