/*
 * Project: dp-api-common
 * File:	QueryAssemblyTestsSummary.java
 * Package: com.ospreydcs.dp.jal.tools.query.assem
 * Type: 	QueryAssemblyTestsSummary
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
 * @since Aug 14, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.assem;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Collection;

import com.ospreydcs.dp.api.common.JalDataTableType;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Record containing the summary of a collection of <code>{@link QueryAssemblyResult}</code> records.
 * </p>
 * <p>
 * The record is used for computing a summary of the results for multiple sampled aggregate assembly test
 * cases.  The assembly operation includes both the sampled aggregate assembly and the time-series data
 * table creation.
 * </p>
 * <p>
 * The summary fields are generally statistical properties of the <code>QueryAssemblyResult</code> collection.
 * Specifically, they contain fields representing statistical elements of the <code>QueryAssemblyResult</code>
 * fields.
 * </p>
 * <p>
 * <h2>Creation</h2>
 * Instances of <code>QueryAssemblyTestsSummary</code> are intended to be created by method 
 * <code>{@link #summarize(Collection)}</code> where the argument is collection of <code>QueryAssemblyResult</code>
 * records under inspection.
 * </p>
 *
 *
 * @author Christopher K. Allen
 * @since Aug 14, 2025
 *
 * @param cntResults            total <code>QueryAssemblyTestResult</code> records used for the summary
 * @param cntFailures           total <code>QueryAssemblyTestResult</code> evaluation failures seen
 * 
 * @param szAllocAggrAssmMin    minimum allocation size (in bytes) seen for sampled aggregate assembly
 * @param szAllocAggrAssmMax    maximum allocation size (in bytes) seen for sampled aggregate assembly
 * @param szAllocAggrAssmAvg    average allocation size (in bytes) seen for sampled aggregate assembly
 * @param szAllocAggrAssmStd    standard deviation of the allocation size (in bytes) for sampled aggregate assembly
 * @param durAggrAssmMin        minimum time duration seen for sampled aggregate assembly
 * @param durAggrAssmMax        maximum time duration seen for sampled aggregate assembly
 * @param durAggrAssmAvg        average time duration seen for sampled aggregate assembly
 * @param durAggrAssmStd        standard deviation of time duration for sampled aggregate assembly
 * @param dblRateAggAssmMin     minimum data rate (in MBps) seen for sampled aggregate assembly
 * @param dblRateAggAssmMax     minimum data rate (in MBps) seen for sampled aggregate assembly
 * @param dblRateAggAssmAvg     average data rate (in MBps) seen for sampled aggregate assembly
 * @param dblRateAggAssmStd     standard deviate of data rates (in MBps) seen for sampled aggregate assembly
 * @param cntRatesAggAssemGtAvg number of sampled aggregate assembly data rates greater than the average rate
 * @param cntRatesAggAssemGtTgt number of sampled aggregate assembly data rates greater than the target rate
 * 
 * @param cntBlksAggTotMin      minimum number of total sampled blocks seen in assembled sampled aggregates
 * @param cntBlksAggTotMax      maximum number of total sampled blocks seen in assembled sampled aggregates
 * @param cntBlksAggTotAvg      average number of total sampled blocks seen in assembled sampled aggregates
 * @param cntBlksAggClkdMin     minimum number of clocked sampled blocks seen in assembled sampled aggregates
 * @param cntBlksAggClkdMax     maximum number of clocked sampled blocks seen in assembled sampled aggregates
 * @param cntBlksAggClkdAvg     average number of clocked sampled blocks seen in assembled sampled aggregates
 * @param cntBlksAggTmsLstMin   minimum number of timestamp list sampled blocks seen in assembled sampled aggregates
 * @param cntBlksAggTmsLstMax   maximum number of timestamp list sampled blocks seen in assembled sampled aggregates
 * @param cntBlksAggTmsLstAvg   average number of timestamp list sampled blocks seen in assembled sampled aggregates
 * @param cntBlksAggSupDomMin   minimum number of super-domain sampled blocks seen in assembled sampled aggregates
 * @param cntBlksAggSupDomMax   maximum number of super-domain sampled blocks seen in assembled sampled aggregates
 * @param cntBlksAggSupDomAvg   average number of super-domain sampled blocks seen in assembled sampled aggregates
 * 
 * @param szTblCalcMin          minimum calculated allocation size (in bytes) seen in created data tables 
 * @param szTblCalcMax          maximum calculated allocation size (in bytes) seen in created data tables
 * @param szTblCalcAvg          average calculated allocation size (in bytes) seen in created data tables
 * @param szTblCalcStd          standard deviation of calculated allocation size (in bytes) seen in created data tables
 * @param durTblBldMin          minimum time duration seen for data table creation
 * @param durTblBldMax          maximum time duration seen for data table creation
 * @param durTblBldAvg          average time duration seen for data table creation
 * @param durTblBldStd          standard deviation seen in time durations for data table creation
 * @param dblRateTblBldMin      minimum data rate seen for data table creation
 * @param dblRateTblBldMax      maximum data rate seen for data table creation
 * @param dblRateTblBldAvg      average data rate seen for data table creation
 * @param dblRateTblBldStd      standard deviation seen for data rates in data table creation (in MBps)
 * @param dblRatesTblBldGtAvg   number of assembly data rates greater than the average rate
 * @param dblRatesTblBldGtTgt   number of assembly data rates greater than target rate
 * 
 * @param cntTblStat            number of static data table implementation types seen for all test results
 * @param cntTblDyn             number of dynamic data table implementation types seen for all test results
 * @param cntTblRowsMin         minimum number of data table rows seen for all test result
 * @param cntTblRowsMax         maximum number of data table rows seen for all test result
 * @param cntTblRowsAvg         average number of data table rows seen for all test result
 * @param cntTblColsMin         minimum number of data table columns seen for all test result
 * @param cntTblColsMax         maximum number of data table columns seen for all test result
 * @param cntTblColsAvg         average number of data table columns seen for all test result
 * 
 * @param szAllocTotalMin       minimum of the allocation size used for total data rate calculation (in bytes) 
 * @param szAllocTotalMax       maximum of the allocation size used for total data rate calculation (in bytes)
 * @param szAllocTotalAvg       average of the allocation size used for total data rate calculation (in bytes)
 * @param szAllocTotalStd       standard deviation of the allocation size used for total data rate calculation (in bytes)
 * @param durTotalMin           minimum time duration for entire recovery, correlation, assembly, and table creation process
 * @param durTotalMax           maximum time duration for entire recovery, correlation, assembly, and table creation process
 * @param durTotalAvg           average time duration for entire recovery, correlation, assembly, and table creation process
 * @param durTotalStd           standard deviation of time duration for entire recovery, correlation, assembly, and table creation process
 * @param dblRateTotalMin       minimum data rate for entire recovery, correlation, assembly, and table creation process (in MBps)
 * @param dblRateTotalMax       maximum data rate for entire recovery, correlation, assembly, and table creation process (in MBps)
 * @param dblRateTotalAvg       average data rate for entire recovery, correlation, assembly, and table creation process (in MBps)
 * @param dblRateTotalStd       data rate standard deviation for entire recovery, correlation, assembly, and table creation process (in MBps)
 * @param dblRatesTotalGtAvg    number of total data rates greater than the average total rate (i.e., rate for entire process)
 * @param dblRatesTotalGtTgt    number of total data rates greater than the target data rate 
 */
public record QueryAssemblyTestsSummary(
        int         cntResults,
        int         cntFailures,
        
        long        szAllocAggrAssmMin,
        long        szAllocAggrAssmMax,
        double      szAllocAggrAssmAvg,
        double      szAllocAggrAssmStd,
        Duration    durAggrAssmMin,
        Duration    durAggrAssmMax,
        Duration    durAggrAssmAvg,
        Duration    durAggrAssmStd,
        double      dblRateAggAssmMin,
        double      dblRateAggAssmMax,
        double      dblRateAggAssmAvg,
        double      dblRateAggAssmStd,
        int         cntRatesAggAssmGtAvg,
        int         cntRatesAggAssmGtTgt,
        
        int         cntBlksAggTotMin,
        int         cntBlksAggTotMax,
        double      cntBlksAggTotAvg,
        int         cntBlksAggClkdMin,
        int         cntBlksAggClkdMax,
        double      cntBlksAggClkdAvg,
        int         cntBlksAggTmsLstMin,
        int         cntBlksAggTmsLstMax,
        double      cntBlksAggTmsLstAvg,
        int         cntBlksAggSupDomMin,
        int         cntBlksAggSupDomMax,
        double      cntBlksAggSupDomAvg,
        
        long        szTblCalcMin,
        long        szTblCalcMax,
        double      szTblCalcAvg,
        double      szTblCalcStd,
        Duration    durTblBldMin,
        Duration    durTblBldMax,
        Duration    durTblBldAvg,
        Duration    durTblBldStd,
        double      dblRateTblBldMin,
        double      dblRateTblBldMax,
        double      dblRateTblBldAvg,
        double      dblRateTblBldStd,
        int         cntRatesTblBldGtAvg,
        int         cntRatesTblBldGtTgt,
        
        int         cntTblStat,
        int         cntTblDyn,
        int         cntTblRowsMin,
        int         cntTblRowsMax,
        double      cntTblRowsAvg,
        int         cntTblColsMin,
        int         cntTblColsMax,
        double      cntTblColsAvg,
        
        long        szAllocTotalMin,
        long        szAllocTotalMax,
        double      szAllocTotalAvg,
        double      szAllocTotalStd,
        Duration    durTotalMin,
        Duration    durTotalMax,
        Duration    durTotalAvg,
        Duration    durTotalStd,
        double      dblRateTotalMin,
        double      dblRateTotalMax,
        double      dblRateTotalAvg,
        double      dblRateTotalStd,
        int         cntRatesTotalGtAvg,
        int         cntRatesTotalGtTgt
        ) 
{
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new <code>QueryAssemblyTestsSummary</code> record from the given argument values.
     * </p>
     * <p>
     * This creator requires all fields of the record and is equivalent to the canonical constructor.
     * </p>
     * 
     * @param cntResults            total <code>QueryAssemblyTestResult</code> records used for the summary
     * @param cntFailures           total <code>QueryAssemblyTestResult</code> evaluation failures seen
     * 
     * @param szAllocAggrAssmMin    minimum allocation size (in bytes) seen for sampled aggregate assembly
     * @param szAllocAggrAssmMax    maximum allocation size (in bytes) seen for sampled aggregate assembly
     * @param szAllocAggrAssmAvg    average allocation size (in bytes) seen for sampled aggregate assembly
     * @param szAllocAggrAssmStd    standard deviation of the allocation size (in bytes) for sampled aggregate assembly
     * @param durAggrAssmMin        minimum time duration seen for sampled aggregate assembly
     * @param durAggrAssmMax        maximum time duration seen for sampled aggregate assembly
     * @param durAggrAssmAvg        average time duration seen for sampled aggregate assembly
     * @param durAggrAssmStd        standard deviation of time duration for sampled aggregate assembly
     * @param dblRateAggAssmMin     minimum data rate (in MBps) seen for sampled aggregate assembly
     * @param dblRateAggAssmMax     minimum data rate (in MBps) seen for sampled aggregate assembly
     * @param dblRateAggAssmAvg     average data rate (in MBps) seen for sampled aggregate assembly
     * @param dblRateAggAssmStd     standard deviate of data rates (in MBps) seen for sampled aggregate assembly
     * @param cntRatesAggAssemGtAvg number of sampled aggregate assembly data rates greater than the average rate
     * @param cntRatesAggAssemGtTgt number of sampled aggregate assembly data rates greater than the target rate
     * 
     * @param cntBlksAggTotMin      minimum number of total sampled blocks seen in assembled sampled aggregates
     * @param cntBlksAggTotMax      maximum number of total sampled blocks seen in assembled sampled aggregates
     * @param cntBlksAggTotAvg      average number of total sampled blocks seen in assembled sampled aggregates
     * @param cntBlksAggClkdMin     minimum number of clocked sampled blocks seen in assembled sampled aggregates
     * @param cntBlksAggClkdMax     maximum number of clocked sampled blocks seen in assembled sampled aggregates
     * @param cntBlksAggClkdAvg     average number of clocked sampled blocks seen in assembled sampled aggregates
     * @param cntBlksAggTmsLstMin   minimum number of timestamp list sampled blocks seen in assembled sampled aggregates
     * @param cntBlksAggTmsLstMax   maximum number of timestamp list sampled blocks seen in assembled sampled aggregates
     * @param cntBlksAggTmsLstAvg   average number of timestamp list sampled blocks seen in assembled sampled aggregates
     * @param cntBlksAggSupDomMin   minimum number of super-domain sampled blocks seen in assembled sampled aggregates
     * @param cntBlksAggSupDomMax   maximum number of super-domain sampled blocks seen in assembled sampled aggregates
     * @param cntBlksAggSupDomAvg   average number of super-domain sampled blocks seen in assembled sampled aggregates
     * 
     * @param szTblCalcMin          minimum calculated allocation size (in bytes) seen in created data tables 
     * @param szTblCalcMax          maximum calculated allocation size (in bytes) seen in created data tables
     * @param szTblCalcAvg          average calculated allocation size (in bytes) seen in created data tables
     * @param szTblCalcStd          standard deviation of calculated allocation size (in bytes) seen in created data tables
     * @param durTblBldMin          minimum time duration seen for data table creation
     * @param durTblBldMax          maximum time duration seen for data table creation
     * @param durTblBldAvg          average time duration seen for data table creation
     * @param durTblBldStd          standard deviation seen in time durations for data table creation
     * @param dblRateTblBldMin      minimum data rate seen for data table creation
     * @param dblRateTblBldMax      maximum data rate seen for data table creation
     * @param dblRateTblBldAvg      average data rate seen for data table creation
     * @param dblRateTblBldStd      standard deviation seen for data rates in data table creation (in MBps)
     * @param dblRatesTblBldGtAvg   number of assembly data rates greater than the average rate
     * @param dblRatesTblBldGtTgt   number of assembly data rates greater than target rate
     * 
     * @param cntTblStat            number of static data table implementation types seen for all test results
     * @param cntTblDyn             number of dynamic data table implementation types seen for all test results
     * @param cntTblRowsMin         minimum number of data table rows seen for all test result
     * @param cntTblRowsMax         maximum number of data table rows seen for all test result
     * @param cntTblRowsAvg         average number of data table rows seen for all test result
     * @param cntTblColsMin         minimum number of data table columns seen for all test result
     * @param cntTblColsMax         maximum number of data table columns seen for all test result
     * @param cntTblColsAvg         average number of data table columns seen for all test result
     * 
     * @param szAllocTotalMin       minimum of the allocation size used for total data rate calculation (in bytes) 
     * @param szAllocTotalMax       maximum of the allocation size used for total data rate calculation (in bytes)
     * @param szAllocTotalAvg       average of the allocation size used for total data rate calculation (in bytes)
     * @param szAllocTotalStd       standard deviation of the allocation size used for total data rate calculation (in bytes)
     * @param durTotalMin           minimum time duration for entire recovery, correlation, assembly, and table creation process
     * @param durTotalMax           maximum time duration for entire recovery, correlation, assembly, and table creation process
     * @param durTotalAvg           average time duration for entire recovery, correlation, assembly, and table creation process
     * @param durTotalStd           standard deviation of time duration for entire recovery, correlation, assembly, and table creation process
     * @param dblRateTotalMin       minimum data rate for entire recovery, correlation, assembly, and table creation process (in MBps)
     * @param dblRateTotalMax       maximum data rate for entire recovery, correlation, assembly, and table creation process (in MBps)
     * @param dblRateTotalAvg       average data rate for entire recovery, correlation, assembly, and table creation process (in MBps)
     * @param dblRateTotalStd       data rate standard deviation for entire recovery, correlation, assembly, and table creation process (in MBps)
     * @param dblRatesTotalGtAvg    number of total data rates greater than the average total rate (i.e., rate for entire process)
     * @param dblRatesTotalGtTgt    number of total data rates greater than the target data rate 
     * 
     * @return  a new <code>QueryAssemblyTestsSummary</code> record populated with the above argument values
     */
    public static QueryAssemblyTestsSummary from(
            int         cntResults,
            int         cntFailures,
            
            long        szAllocAggrAssmMin,
            long        szAllocAggrAssmMax,
            double      szAllocAggrAssmAvg,
            double      szAllocAggrAssmStd,
            Duration    durAggrAssmMin,
            Duration    durAggrAssmMax,
            Duration    durAggrAssmAvg,
            Duration    durAggrAssmStd,
            double      dblRateAggAssmMin,
            double      dblRateAggAssmMax,
            double      dblRateAggAssmAvg,
            double      dblRateAggAssmStd,
            int         cntRatesAggAssmGtAvg,
            int         cntRatesAggAssmGtTgt,
            
            int         cntBlksAggTotMin,
            int         cntBlksAggTotMax,
            double      cntBlksAggTotAvg,
            int         cntBlksAggClkdMin,
            int         cntBlksAggClkdMax,
            double      cntBlksAggClkdAvg,
            int         cntBlksAggTmsLstMin,
            int         cntBlksAggTmsLstMax,
            double      cntBlksAggTmsLstAvg,
            int         cntBlksAggSupDomMin,
            int         cntBlksAggSupDomMax,
            double      cntBlksAggSupDomAvg,
            
            long        szTblCalcMin,
            long        szTblCalcMax,
            double      szTblCalcAvg,
            double      szTblCalcStd,
            Duration    durTblBldMin,
            Duration    durTblBldMax,
            Duration    durTblBldAvg,
            Duration    durTblBldStd,
            double      dblRateTblBldMin,
            double      dblRateTblBldMax,
            double      dblRateTblBldAvg,
            double      dblRateTblBldStd,
            int         cntRatesTblBldGtAvg,
            int         cntRatesTblBldGtTgt,
            
            int         cntTblStat,
            int         cntTblDyn,
            int         cntTblRowsMin,
            int         cntTblRowsMax,
            double      cntTblRowsAvg,
            int         cntTblColsMin,
            int         cntTblColsMax,
            double      cntTblColsAvg,
            
            long        szAllocTotalMin,
            long        szAllocTotalMax,
            double      szAllocTotalAvg,
            double      szAllocTotalStd,
            Duration    durTotalMin,
            Duration    durTotalMax,
            Duration    durTotalAvg,
            Duration    durTotalStd,
            double      dblRateTotalMin,
            double      dblRateTotalMax,
            double      dblRateTotalAvg,
            double      dblRateTotalStd,
            int         cntRatesTotalGtAvg,
            int         cntRatesTotalGtTgt
            )
    {
        return new QueryAssemblyTestsSummary(
                cntResults, cntFailures,
                
                szAllocAggrAssmMin, szAllocAggrAssmMax, szAllocAggrAssmAvg, szAllocAggrAssmStd, 
                durAggrAssmMin, durAggrAssmMax, durAggrAssmAvg, durAggrAssmStd,
                dblRateAggAssmMin, dblRateAggAssmMax, dblRateAggAssmAvg, dblRateAggAssmStd,
                cntRatesAggAssmGtAvg, cntRatesAggAssmGtTgt,
                
                cntBlksAggTotMin, cntBlksAggTotMax, cntBlksAggTotAvg, 
                cntBlksAggClkdMin, cntBlksAggClkdMax, cntBlksAggClkdAvg,
                cntBlksAggTmsLstMin, cntBlksAggTmsLstMax, cntBlksAggTmsLstAvg,
                cntBlksAggSupDomMin, cntBlksAggSupDomMax, cntBlksAggSupDomAvg,
                
                szTblCalcMin, szTblCalcMax, szTblCalcAvg, szTblCalcStd,
                durTblBldMin, durTblBldMax, durTblBldAvg, durTblBldStd,
                dblRateTblBldMin, dblRateTblBldMax, dblRateTblBldAvg, dblRateTblBldStd,
                cntRatesTblBldGtAvg, cntRatesTblBldGtTgt,
                
                cntTblStat, cntTblDyn,
                cntTblRowsMin, cntTblRowsMax, cntTblRowsAvg,
                cntTblColsMin, cntTblColsMax, cntTblColsAvg,
                
                szAllocTotalMin, szAllocTotalMax, szAllocTotalAvg, szAllocTotalStd,
                durTotalMin, durTotalMax, durTotalAvg, durTotalStd,
                dblRateTotalMin, dblRateTotalMax, dblRateTotalAvg, dblRateTotalStd,
                cntRatesTotalGtAvg, cntRatesTotalGtTgt
                );
    }
    
    
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
    

    // Operations
    //
    
    /**
     * <p>
     * Computes a summary of the evaluation results for the given collection and returns them.
     * </p>
     * <p>
     * All the fields of a <code>QueryAssemblyTestsSummary</code> record are computed from the given 
     * collection of test result records.  The computed values are then returned in a 
     * new <code>QueryAssemblyTestsSummary</code> instance.
     * </p>
     *  
     * @param setResultsAll    collection of test results to summarize
     * 
     * @return  a new <code>QueryAssemblyTestsSummary</code> record containing a summary of the argument results
     * 
     * @throws  IllegalArgumentException    argument collection was empty
     */
    public static QueryAssemblyTestsSummary summarize(Collection<QueryAssemblyTestResult> setResultsAll) {
        
        // Check argument - avoid divide by zero
        if (setResultsAll.isEmpty())
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Argument collection was empty.");
        
        // Get the results size and failures
        int         cntResultsAll = setResultsAll.size();
        int         cntFailures = setResultsAll.stream().filter(rec -> rec.recResultStatus().isFailure()).mapToInt(r -> 1).sum();
        
        // Extract the successful results and result size
        Collection<QueryAssemblyTestResult> setResults = setResultsAll.stream().filter(rec -> rec.recResultStatus().isSuccess()).toList();
        int                                 cntResults = setResults.size();
        
        // Compute the sampled aggregate assembly statistics
        long    szAllocAggrAssmMax = setResults.stream().mapToLong(rec -> rec.szAllocAggrAssm()).reduce(0,                  (s1, s2) -> { if (s1>s2) return s1; else return s2; } );
        long    szAllocAggrAssmMin = setResults.stream().mapToLong(rec -> rec.szAllocAggrAssm()).reduce(szAllocAggrAssmMax, (s1, s2) -> { if (s1<s2) return s1; else return s2; } );
        double  szAllocAggrAssmAvg = setResults.stream().mapToDouble(rec -> rec.szAllocAggrAssm()).sum()/cntResults;
        double  szAllocAggrAssmSqrd = setResults.stream().mapToDouble(rec -> rec.szAllocAggrAssm()).map(s -> (s-szAllocAggrAssmAvg)).map(s -> s*s).sum();
        double  szAllocAggrAssmStd = Math.sqrt(szAllocAggrAssmSqrd/cntResults);
        
        Duration    durAggrAssmMax = setResults.stream().<Duration>map(rec -> rec.durAggrAssm()).reduce(Duration.ZERO,  (d1, d2) -> { if (d1.compareTo(d2) > 0) return d1; else return d2; } );
        Duration    durAggrAssmMin = setResults.stream().<Duration>map(rec -> rec.durAggrAssm()).reduce(durAggrAssmMax, (d1, d2) -> { if (d1.compareTo(d2) < 0) return d1; else return d2; } );
        Duration    durAggrAssmAvg = setResults.stream().<Duration>map(rec -> rec.durAggrAssm()).reduce(Duration.ZERO,  (d1, d2) -> d1.plus(d2)).dividedBy(cntResults);
        long        nsAggrAssmSqrd = setResults.stream().<Duration>map(rec -> rec.durAggrAssm()).map(d -> d.minus(durAggrAssmAvg)).mapToLong(d -> d.toNanos()).map(l -> l*l).sum();
        long        nsAggrAssmStd = Double.valueOf(Math.sqrt(((double)nsAggrAssmSqrd)/cntResults)).longValue();
        Duration    durAggrAssmStd = Duration.ofNanos(nsAggrAssmStd);
        
        double  dblRateAggrAssmMax = setResults.stream().mapToDouble(rec -> rec.dblRateAggAssm()).reduce(0.0,                (r1, r2) -> { if (r1>r2) return r1; else return r2; } );
        double  dblRateAggrAssmMin = setResults.stream().mapToDouble(rec -> rec.dblRateAggAssm()).reduce(dblRateAggrAssmMax, (r1, r2) -> { if (r1<r2) return r1; else return r2; } );
        double  dblRateAggrAssmAvg = setResults.stream().mapToDouble(rec -> rec.dblRateAggAssm()).sum()/cntResults;
        double  dblRateAggrAssmSqrd = setResults.stream().mapToDouble(rec -> rec.dblRateAggAssm()).map(r -> r - dblRateAggrAssmAvg).map(r -> r*r).sum();
        double  dblRateAggrAssmStd = Math.sqrt(dblRateAggrAssmSqrd/cntResults);
        
        int     cntRatesAggrAssmGtAvg = setResults.stream().mapToDouble(rec -> rec.dblRateAggAssm()).filter(r -> r > dblRateAggrAssmAvg).mapToInt(r -> 1).sum();
        int     cntRatesAggrAssmGtTgt = setResults.stream().mapToDouble(rec -> rec.dblRateAggAssm()).filter(r -> r > DBL_RATE_TARGET).mapToInt(r -> 1).sum();
        
        // Compute the sampled aggregate properties
        int     cntBlksAggrTotMax = setResults.stream().mapToInt(rec -> rec.cntBlksAggTot()).reduce(0,                 (n1, n2) -> { if (n1>n2) return n1; else return n2; } );
        int     cntBlksAggrTotMin = setResults.stream().mapToInt(rec -> rec.cntBlksAggTot()).reduce(cntBlksAggrTotMax, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        double  cntBlksAggrTotAvg = setResults.stream().mapToDouble(rec -> rec.cntBlksAggTot()).sum()/cntResults;
                
        int     cntBlksAggrClkdMax = setResults.stream().mapToInt(rec -> rec.cntBlksAggClkd()).reduce(0,                 (n1, n2) -> { if (n1>n2) return n1; else return n2; } );
        int     cntBlksAggrClkdMin = setResults.stream().mapToInt(rec -> rec.cntBlksAggClkd()).reduce(cntBlksAggrTotMax, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        double  cntBlksAggrClkdAvg = setResults.stream().mapToDouble(rec -> rec.cntBlksAggClkd()).sum()/cntResults;
                
        int     cntBlksAggrTmsLstMax = setResults.stream().mapToInt(rec -> rec.cntBlksAggTmsLst()).reduce(0,                 (n1, n2) -> { if (n1>n2) return n1; else return n2; } );
        int     cntBlksAggrTmsLstMin = setResults.stream().mapToInt(rec -> rec.cntBlksAggTmsLst()).reduce(cntBlksAggrTotMax, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        double  cntBlksAggrTmsLstAvg = setResults.stream().mapToDouble(rec -> rec.cntBlksAggTmsLst()).sum()/cntResults;
                
        int     cntBlksAggrSupDomMax = setResults.stream().mapToInt(rec -> rec.cntBlksAggSupDom()).reduce(0,                 (n1, n2) -> { if (n1>n2) return n1; else return n2; } );
        int     cntBlksAggrSupDomMin = setResults.stream().mapToInt(rec -> rec.cntBlksAggSupDom()).reduce(cntBlksAggrTotMax, (n1, n2) -> { if (n1<n2) return n1; else return n2; } );
        double  cntBlksAggrSupDomAvg = setResults.stream().mapToDouble(rec -> rec.cntBlksAggSupDom()).sum()/cntResults;
                
        // Compute the data table creation statistics
        long    szTblCalcMax = setResults.stream().mapToLong(rec -> rec.szTblCalc()).reduce(0L,           (s1, s2) -> { if (s1>s2) return s1; else return s2; } );
        long    szTblCalcMin = setResults.stream().mapToLong(rec -> rec.szTblCalc()).reduce(szTblCalcMax, (s1, s2) -> { if (s1<s2) return s1; else return s2; } );
        double  szTblCalcAvg = setResults.stream().mapToDouble(rec -> rec.szTblCalc()).sum()/cntResults;
        double  szTblCalcSqrt = setResults.stream().mapToDouble(rec -> rec.szTblCalc()).map(s -> s - szTblCalcAvg).map(s -> s*s).sum();
        double  szTblCalcStd = Math.sqrt(szTblCalcSqrt/cntResults);
        
        Duration    durTblBldMax = setResults.stream().<Duration>map(rec -> rec.durTblBld()).reduce(Duration.ZERO, (d1, d2) -> { if (d1.compareTo(d2) > 0) return d1; else return d2; });
        Duration    durTblBldMin = setResults.stream().<Duration>map(rec -> rec.durTblBld()).reduce(durTblBldMax,  (d1, d2) -> { if (d1.compareTo(d2) < 0) return d1; else return d2; });
        Duration    durTblBldAvg = setResults.stream().<Duration>map(rec -> rec.durTblBld()).reduce(Duration.ZERO, (d1, d2) -> d1.plus(d2)).dividedBy(szTblCalcMin);
        long        nsTblBldSqrd = setResults.stream().<Duration>map(rec -> rec.durTblBld()).<Duration>map(d -> d.minus(durTblBldAvg)).mapToLong(Duration::toNanos).map(ns -> ns*ns).sum();
        long        nsTblBldStd = Double.valueOf( Math.sqrt( ((double)nsTblBldSqrd)/cntResults) ).longValue();
        Duration    durTblBldStd = Duration.ofNanos(nsTblBldStd);
        
        double  dblRateTblBldMax = setResults.stream().mapToDouble(rec -> rec.dblRateTblBld()).reduce(0.0,              (r1, r2) -> { if (r1>r2) return r1; else return r2; });
        double  dblRateTblBldMin = setResults.stream().mapToDouble(rec -> rec.dblRateTblBld()).reduce(dblRateTblBldMax, (r1, r2) -> { if (r1>r2) return r1; else return r2; });
        double  dblRateTblBldAvg = setResults.stream().mapToDouble(rec -> rec.dblRateTblBld()).sum()/cntResults;
        double  dblRateTblBldSqrd = setResults.stream().mapToDouble(rec -> rec.dblRateTblBld()).map(r -> r-dblRateTblBldAvg).map(r -> r*r).sum();
        double  dblRateTblBldStd = Math.sqrt(dblRateTblBldSqrd/cntResults);
        
        int     cntRatesTblBldGtAvg = setResults.stream().mapToDouble(rec -> rec.dblRateTblBld()).filter(r -> r>dblRateTblBldAvg).mapToInt(r -> 1).sum();
        int     cntRatesTblBldGtTgt = setResults.stream().mapToDouble(rec -> rec.dblRateTblBld()).filter(r -> r>DBL_RATE_TARGET).mapToInt(r -> 1).sum();
        
        // Compute the data table properties
        int     cntTblStat = setResults.stream().<JalDataTableType>map(rec -> rec.enmTblType()).filter(t -> t==JalDataTableType.STATIC).mapToInt(t -> 1).sum();
        int     cntTblDyn = setResults.stream().<JalDataTableType>map(rec -> rec.enmTblType()).filter(t -> t==JalDataTableType.DYNAMIC).mapToInt(t -> 1).sum();
        
        int     cntTblRowsMax = setResults.stream().mapToInt(rec -> rec.cntTblRows()).reduce(0,             (n1, n2) -> { if (n1>n2) return n1; else return n2; });
        int     cntTblRowsMin = setResults.stream().mapToInt(rec -> rec.cntTblRows()).reduce(cntTblRowsMax, (n1, n2) -> { if (n1<n2) return n1; else return n2; });
        double  cntTblRowsAvg = setResults.stream().mapToDouble(rec -> rec.cntTblRows()).sum()/cntResults;
        
        int     cntTblColsMax = setResults.stream().mapToInt(rec -> rec.cntTblCols()).reduce(0,             (n1, n2) -> { if (n1>n2) return n1; else return n2; });
        int     cntTblColsMin = setResults.stream().mapToInt(rec -> rec.cntTblCols()).reduce(cntTblColsMax, (n1, n2) -> { if (n1<n2) return n1; else return n2; });
        double  cntTblColsAvg = setResults.stream().mapToDouble(rec -> rec.cntTblCols()).sum()/cntResults;
        
        // Compute the total recovery, correlation, assembly, and table creation statistics
        long    szAllocTotalMax = setResults.stream().mapToLong(rec -> rec.szAllocTotal()).reduce(0L,            (s1, s2) -> { if (s1>s2) return s1; else return s1; });   
        long    szAllocTotalMin = setResults.stream().mapToLong(rec -> rec.szAllocTotal()).reduce(szAllocTotalMax, (s1, s2) -> { if (s1<s2) return s1; else return s1; });
        double  szAllocTotalAvg = setResults.stream().mapToDouble(rec -> rec.szAllocTotal()).sum()/cntResults;
        double  szAllocTotalSqrd = setResults.stream().mapToDouble(rec -> rec.szAllocTotal()).map(s -> s-szAllocTotalAvg).map(s -> s*s).sum();
        double  szAllocTotalStd = Math.sqrt(szAllocTotalSqrd/cntResults);
        
        Duration    durTotalMax = setResults.stream().<Duration>map(rec -> rec.durTotal()).reduce(Duration.ZERO, (d1, d2) -> { if (d1.compareTo(d2)>0) return d1; else return d2; });
        Duration    durTotalMin = setResults.stream().<Duration>map(rec -> rec.durTotal()).reduce(durTotalMax,   (d1, d2) -> { if (d1.compareTo(d2)<0) return d1; else return d2; });
        Duration    durTotalAvg = setResults.stream().<Duration>map(rec -> rec.durTotal()).reduce(Duration.ZERO, (d1, d2) -> d1.plus(d2)).dividedBy(cntResults);
        long        nsTotalSqrd = setResults.stream().<Duration>map(rec -> rec.durTotal()).map(d -> d.minus(durTotalAvg)).mapToLong(Duration::toNanos).map(ns -> ns*ns).sum();
        long        nsTotalStd = Double.valueOf( Math.sqrt( ((double)nsTotalSqrd)/cntResults) ).longValue();
        Duration    durTotalStd = Duration.ofNanos(nsTotalStd);
        
        double      dblRateTotalMax = setResults.stream().mapToDouble(rec -> rec.dblRateTotal()).reduce(0.0,             (r1, r2) -> { if (r1>r2) return r1; else return r2; });
        double      dblRateTotalMin = setResults.stream().mapToDouble(rec -> rec.dblRateTotal()).reduce(dblRateTotalMax, (r1, r2) -> { if (r1>r2) return r1; else return r2; });
        double      dblRateTotalAvg = setResults.stream().mapToDouble(rec ->rec.dblRateTotal()).sum()/cntResults;
        double      dblRateTotalSqrd = setResults.stream().mapToDouble(rec -> rec.dblRateTotal()).map(r -> r-dblRateTotalAvg).map(r -> r*r).sum();
        double      dblRateTotalStd = Math.sqrt(dblRateTotalSqrd/cntResults);
        
        int     cntRatesTotalGtAvg = setResults.stream().mapToDouble(rec -> rec.dblRateTotal()).filter(r -> r>dblRateTotalAvg).mapToInt(r -> 1).sum();
        int     cntRatesTotalGtTgt = setResults.stream().mapToDouble(rec -> rec.dblRateTotal()).filter(r -> r>DBL_RATE_TARGET).mapToInt(r -> 1).sum();
        
        // Create the result summary record and return it
        QueryAssemblyTestsSummary   recSummary = QueryAssemblyTestsSummary.from(
                cntResultsAll, cntFailures,
                szAllocAggrAssmMin, szAllocAggrAssmMax, szAllocAggrAssmAvg, szAllocAggrAssmStd, 
                durAggrAssmMin, durAggrAssmMax, durAggrAssmAvg, durAggrAssmStd, 
                dblRateAggrAssmMin, dblRateTblBldSqrd, dblRateAggrAssmAvg, dblRateAggrAssmStd, 
                cntRatesAggrAssmGtAvg, cntRatesAggrAssmGtTgt, 
                cntBlksAggrTotMin, cntBlksAggrTotMax, cntBlksAggrTotAvg, 
                cntBlksAggrClkdMin, cntBlksAggrClkdMax, cntBlksAggrClkdAvg, 
                cntBlksAggrTmsLstMin, cntBlksAggrTmsLstMax, cntBlksAggrTmsLstAvg, 
                cntBlksAggrSupDomMin, cntBlksAggrSupDomMax, cntBlksAggrSupDomAvg, 
                szTblCalcMin, szTblCalcMax, szTblCalcAvg, szTblCalcStd, 
                durTblBldMin, durTblBldMax, durTblBldAvg, durTblBldStd, 
                dblRateTblBldMin, dblRateTblBldMax, dblRateTblBldAvg, dblRateTblBldStd, 
                cntRatesTblBldGtAvg, cntRatesTblBldGtTgt, 
                cntTblStat, cntTblDyn, 
                cntTblRowsMin, cntTblRowsMax, cntTblRowsAvg, 
                cntTblColsMin, cntTblColsMax, cntTblColsAvg, 
                szAllocTotalMin, szAllocTotalMax, szAllocTotalAvg, szAllocTotalStd, 
                durTotalMin, durTotalMax, durTotalAvg, durTotalStd, 
                dblRateTotalMin, dblRateTotalMax, dblRateTotalAvg, dblRateTotalStd, 
                cntRatesTotalGtAvg, cntRatesTotalGtTgt
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
        String  strPadd = strPad + "  ";
        
        // Print out results  
        ps.println(strPad + "Query Assembly Test Results Summary ");
        ps.println(strPadd + "Total number of result cases       : " + this.cntResults);
        ps.println(strPadd + "Number of test evaluation failures : " + this.cntFailures);
        
        ps.println(strPadd + "Sampled Aggregate Assembly");
        ps.println(strPadd + "  Aggregate allocation size minimum (bytes)    : " + this.szAllocAggrAssmMin);
        ps.println(strPadd + "  Aggregate allocation size maximum (bytes)    : " + this.szAllocAggrAssmMax);
        ps.println(strPadd + "  Aggregate allocation size average (bytes)    : " + this.szAllocAggrAssmAvg);
        ps.println(strPadd + "  Aggregate size standard deviation (bytes)    : " + this.szAllocAggrAssmStd);
        ps.println(strPadd + "  Aggregate assembly time duration minimum     : " + this.durAggrAssmMin);
        ps.println(strPadd + "  Aggregate assembly time duration maximum     : " + this.durAggrAssmMax);
        ps.println(strPadd + "  Aggregate assembly time duration average     : " + this.durAggrAssmAvg);
        ps.println(strPadd + "  Aggregate assembly duration standard dev.    : " + this.durAggrAssmStd);
        ps.println(strPadd + "  Aggregate assembly data rate minimum (MBps)  : " + this.dblRateAggAssmMin);
        ps.println(strPadd + "  Aggregate assembly data rate maximum (MBps)  : " + this.dblRateAggAssmMax);
        ps.println(strPadd + "  Aggregate assembly data rate average (MBps)  : " + this.dblRateAggAssmAvg);
        ps.println(strPadd + "  Aggregate assembly rate standard dev.(MBps)  : " + this.dblRateAggAssmStd);
        ps.println(strPadd + "  Target assembly rate (MBps)                  : " + DBL_RATE_TARGET);
        ps.println(strPadd + "  Assembly rates greater than average          : " + this.cntRatesAggAssmGtAvg);
        ps.println(strPadd + "  Assembly rates greater than target           : " + this.cntRatesAggAssmGtTgt);
        
        ps.println(strPadd + "Sampled Aggregate Properties");
        ps.println(strPadd + "  Total sampled block count minimum            : " + this.cntBlksAggTotMin);
        ps.println(strPadd + "  Total sampled block count maximum            : " + this.cntBlksAggTotMax);
        ps.println(strPadd + "  Total sampled block count average            : " + this.cntBlksAggTotAvg);
        ps.println(strPadd + "  Clocked sampled block count minimum          : " + this.cntBlksAggClkdMin);
        ps.println(strPadd + "  Clocked sampled block count maximum          : " + this.cntBlksAggClkdMax);
        ps.println(strPadd + "  Clocked sampled block count average          : " + this.cntBlksAggClkdAvg);
        ps.println(strPadd + "  Timestamp list sampled block count minimum   : " + this.cntBlksAggTmsLstMin);
        ps.println(strPadd + "  Timestamp list sampled block count maximum   : " + this.cntBlksAggTmsLstMax);
        ps.println(strPadd + "  Timestamp list sampled block count average   : " + this.cntBlksAggTmsLstAvg);
        ps.println(strPadd + "  Super-domain sampled block count minimum     : " + this.cntBlksAggSupDomMin);
        ps.println(strPadd + "  Super-domain sampled block count maximum     : " + this.cntBlksAggSupDomMax);
        ps.println(strPadd + "  Super-domain sampled block count average     : " + this.cntBlksAggSupDomAvg);
        
        ps.println(strPadd + "Data Table Creation");
        ps.println(strPadd + "  Calculated table size minimum (bytes)        : " + this.szTblCalcMin);
        ps.println(strPadd + "  Calculated table size maximum (bytes)        : " + this.szTblCalcMax);
        ps.println(strPadd + "  Calculated table size average (bytes)        : " + this.szTblCalcAvg);
        ps.println(strPadd + "  Calculated table size standard dev. (bytes)  : " + this.szTblCalcStd);
        ps.println(strPadd + "  Table creation time duration minimum         : " + this.durTblBldMin);
        ps.println(strPadd + "  Table creation time duration maximum         : " + this.durTblBldMax);
        ps.println(strPadd + "  Table creation time duration average         : " + this.durTblBldAvg);
        ps.println(strPadd + "  Table creation time duration standard dev.   : " + this.durTblBldStd);
        ps.println(strPadd + "  Table creation data rate minimum (MBps)      : " + this.dblRateTblBldMin);
        ps.println(strPadd + "  Table creation data rate maximum (MBps)      : " + this.dblRateTblBldMin);
        ps.println(strPadd + "  Table creation data rate average (MBps)      : " + this.dblRateTblBldMin);
        ps.println(strPadd + "  Table creation data rate stand. dev. (MBps)  : " + this.dblRateTblBldMin);
        
        ps.println(strPadd + "Data Table Properties");
        ps.println(strPadd + "  Number of static table implementations       : " + this.cntTblStat);
        ps.println(strPadd + "  Number of dynamic table implementations      : " + this.cntTblDyn);
        ps.println(strPadd + "  Table row count minimum                      : " + this.cntTblRowsMin);
        ps.println(strPadd + "  Table row count maximum                      : " + this.cntTblRowsMax);
        ps.println(strPadd + "  Table row count average                      : " + this.cntTblRowsAvg);
        ps.println(strPadd + "  Table column count minimum                   : " + this.cntTblColsMin);
        ps.println(strPadd + "  Table column count maximum                   : " + this.cntTblColsMax);
        ps.println(strPadd + "  Table column count average                   : " + this.cntTblColsAvg);
        
        ps.println(strPadd + "Total Recovery, Correlation, Assembly, and Data Creation");
        ps.println(strPadd + "  Total allocation size minimum (bytes)        : " + this.szAllocTotalMin);
        ps.println(strPadd + "  Total allocation size maximum (bytes)        : " + this.szAllocTotalMax);
        ps.println(strPadd + "  Total allocation size average (bytes)        : " + this.szAllocTotalAvg);
        ps.println(strPadd + "  Total allocation size standard dev. (bytes)  : " + this.szAllocTotalStd);
        ps.println(strPadd + "  Total time duration minimum                  : " + this.durTotalMin);
        ps.println(strPadd + "  Total time duration maximum                  : " + this.durTotalMax);
        ps.println(strPadd + "  Total time duration average                  : " + this.durTotalAvg);
        ps.println(strPadd + "  Total time duration standard deviation       : " + this.durTotalStd);
        ps.println(strPadd + "  Total data rate minimum (MBps)               : " + this.dblRateTotalMin);
        ps.println(strPadd + "  Total data rate maximum (MBps)               : " + this.dblRateTotalMax);
        ps.println(strPadd + "  Total data rate average (MBps)               : " + this.dblRateTotalAvg);
        ps.println(strPadd + "  Total data rate standard deviation (MBps)    : " + this.dblRateTotalStd);
        ps.println(strPadd + "  Target data rate (MBps)                      : " + DBL_RATE_TARGET);
        ps.println(strPadd + "  Total data rates greater than average        : " + this.cntRatesTotalGtAvg);
        ps.println(strPadd + "  Total data rates greater than target         : " + this.cntRatesTotalGtTgt);
    }
}
