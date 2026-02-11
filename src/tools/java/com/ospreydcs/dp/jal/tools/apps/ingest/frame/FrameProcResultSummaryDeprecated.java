/*
 * Project: dp-jal
 * File:	FrameProcResultSummaryDeprecated.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.frame
 * Type: 	FrameProcResultSummaryDeprecated
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
 * @since Feb 7, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.frame;

import java.time.Duration;
import java.util.Collection;

/**
 * <p>
 * Record containing a performance summary for a collection of <code>FrameProcTestResult</code> records.
 * </p>
 * <p>
 * Records should be created from <code>{@link #from(Collection)}</code> creator which computes the performance
 * summary, populates the results summary record, and returns it.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 7, 2026
 *
 * @deprecated  Replaced by FrameProcResultSummary
 */
@Deprecated(since="Feb 9, 2026", forRemoval=true)
public record FrameProcResultSummaryDeprecated(
        int         cntResultsTot,
        int         cntResultsFail,
        int         cntRatesGtAvg,
        int         cntRatesGtTgt,
        double      dblRateRawMin,
        double      dblRateRawMax,
        double      dblRateRawAvg,
        double      dblRateRawStd,
        double      dblRateProcMin,
        double      dblRateProcMax,
        double      dblRateProcAvg,
        double      dblRateProcStd,
        int         cntFrmsMin,
        int         cntFrmsMax,
        int         cntFrmsAvg,
        long        szPayloadMin,
        long        szPayloadMax,
        long        szPayloadAvg,
        int         cntMsgsMin,
        int         cntMsgsMax,
        int         cntMsgsAvg,
        long        szProcessedMin,
        long        szProcessedMax,
        long        szProcessedAvg,
        Duration    durProcessedMin,
        Duration    durProcessedMax,
        Duration    durProcessedAvg,
        Duration    durProcessedStd
        ) 
{

    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>FrameProcResultSummaryDeprecated</code> record from the given test result collection.
     * </p>
     * <p>
     * Computes a summary of the performance results for the given collection and returns them.
     * All the fields of a <code>FrameProcResultSummaryDeprecated</code> record are computed from the given 
     * collection of test result records.  The computed values are then returned in a 
     * new <code>FrameProcResultSummaryDeprecated</code> instance.
     * </p>
     *  
     * @param setResults    collection of test results
     * 
     * @return  a new <code>FrameProcResultSummaryDeprecated</code> record containing a summary of the argument results
     */
    public static FrameProcResultSummaryDeprecated   from(Collection<FrameProcTestResult> setResults) {
        
        int cntResultsTot = setResults.size();
        int cntResultsFail = setResults.stream().filter(rec -> rec.recTestStatus().isFailure()).mapToInt(rec -> 1).sum();
        int cntResults = cntResultsTot - cntResultsFail;
        
        double  dblRateRawAvg = setResults.stream().mapToDouble(rec -> rec.dblRateRaw()).sum()/cntResults;
        int     cntRatesGtAvg = setResults.stream().filter(rec -> rec.dblRateRaw() >= dblRateRawAvg).mapToInt(rec -> 1).sum();
        int     cntRatesGtTgt = setResults.stream().filter(rec -> rec.dblRateRaw() >= DBL_RATE_TARGET).mapToInt(rec -> 1).sum();
        
        double  dblRateRawMin = setResults.stream().mapToDouble(rec -> rec.dblRateRaw()).reduce(dblRateRawAvg, (r1, r2) -> { if (r1<r2) return r1; else return r2; });
        double  dblRateRawMax = setResults.stream().mapToDouble(rec -> rec.dblRateRaw()).reduce(dblRateRawAvg, (r1, r2) -> { if (r1>r2) return r1; else return r2; });
        double  dblRateRawSqrd = setResults.stream().mapToDouble(rec -> rec.dblRateRaw()).map(r -> (r-dblRateRawAvg)*(r-dblRateRawAvg)).sum();
        double  dblRateRawStd = Math.sqrt(dblRateRawSqrd/cntResults);

        return null;
    }

    
    //
    // Record Resources
    //

    /** The target data rate */
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
    
    //
    // Record Overrides - Remove
    //
    
    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see java.lang.Record#hashCode()
     */
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see java.lang.Record#toString()
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return null;
    }

}
