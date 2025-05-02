/*
 * Project: dp-api-common
 * File:    ResultSummary.java
 * Package: com.ospreydcs.dp.api.query.impl
 * Type:    ResultSummary
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
 * @since Apr 30, 2025
 *
 */
package com.ospreydcs.dp.api.query.impl;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ospreydcs.dp.api.query.DpDataRequest;

/**
 * <p>
 * Record for used for summarizing collections of performance results from data request processors.
 * </p> 
 * <p>
 * A request processor performance result is contained in a <code>ConfigResult</code> record.  This
 * record takes collections of such results and computes a summary which is then available as the fields
 * of the record.  
 * </p> 
 * <p>
 * The summary fields contain the statistical properties of the data rates seen with the result collection,
 * up to second order (i.e., standard deviation).  The summary also contains the number of results above
 * the average value and above a predetermined target value.  A map containing (request, hit count) pairs
 * is also available for population.  It contains the number of hits for each request seen in the result
 * collection.
 * </p> 
 *
 * @param cntResultsTot total number of results within the original result collection
 * @param cntRatesGtAvg number of data rates greater than the average data rate
 * @param cntRatesGtTgt number of data rates greater than a predetermined target rate
 * @param dblRateMin    the minimum data rate seen in the result collection
 * @param dblRateMax    the maximum data rate seen in the result collection 
 * @param dblRateAvg    the average data rate within the result collection
 * @param dblRateStd    the data rate standard deviation of the result collection
 * @param szRqstAvg     average size (in bytes) of recovered data per request
 * @param cntRqstBlksAvg average number of correlated data blocks recovered per request
 * @param durRqstMin    the minimum request time duration observed within the result collection
 * @param durRqstMax    the maximum request time duration observed within the result collection
 * @param durRqstAvg    the average request time duration within the result collection
 * @param durRqstStd    the request duration standard deviation of the result collection
 * @param mapRqstHits   map of (request, hit count) pairs specifying the number of times a request appears in the collection
 * 
 * @author Christopher K. Allen
 * @since Apr 30, 2025
 *
 */
public record ResultSummary(
        int         cntResultsTot,
        int         cntRatesGtAvg,
        int         cntRatesGtTgt,
        double      dblRateMin,
        double      dblRateMax,
        double      dblRateAvg,
        double      dblRateStd,
        long        szRqstAvg,
        int         cntRqstBlksAvg,
        Duration    durRqstMin,
        Duration    durRqstMax,
        Duration    durRqstAvg,
        Duration    durRqstStd,
        Map<DpDataRequest, Integer> mapRqstHits
        ) 
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates an returns a new <code>ResultSummary</code> record populated from the given arguments.
     * </p>
     * <p>
     * Note that the <code>{@link #mapRqstHits}</code> field is created here and available to clients for
     * population as a public field.
     * </p>
     * 
     * @param cntResultsTot total number of results within the original result collection
     * @param cntRatesGtAvg number of data rates greater than the average data rate
     * @param cntRatesGtTgt number of data rates greater than a predetermined target rate
     * @param dblRateMin    the minimum data rate seen in the result collection
     * @param dblRateMax    the maximum data rate seen in the result collection 
     * @param dblRateAvg    the average data rate within the result collection
     * @param dblRateStd    the data rate standard deviation of the result collection
     * @param szRqstAvg     average size (in bytes) of recovered data per request
     * @param cntRqstBlksAvg average number of correlated data blocks recovered per request
     * @param durRqstMin    the minimum request time duration observed within the result collection
     * @param durRqstMax    the maximum request time duration observed within the result collection
     * @param durRqstAvg    the average request time duration within the result collection
     * @param durRqstStd    the request duration standard deviation of the result collection
     * 
     * @return  a new <code>ResultSummary</code> record populated with the given arguments
     */
    public static ResultSummary from(
            int         cntResultsTot,
            int         cntRatesGtAvg,
            int         cntRatesGtTgt,
            double      dblRateMin,
            double      dblRateMax,
            double      dblRateAvg,
            double      dblRateStd,
            long        szRqstAvg,
            int         cntRqstBlksAvg,
            Duration    durRqstMin,
            Duration    durRqstMax,
            Duration    durRqstAvg,
            Duration    durRqstStd
            ) 
    {
        return new ResultSummary(
                cntResultsTot, 
                cntRatesGtAvg, 
                cntRatesGtTgt, 
                dblRateMin, 
                dblRateMax, 
                dblRateAvg, 
                dblRateStd, 
                szRqstAvg,
                cntRqstBlksAvg,
                durRqstMin,
                durRqstMax,
                durRqstAvg, 
                durRqstStd,
                new HashMap<DpDataRequest, Integer>());
    }
    
    /**
     * <p>
     * Creates an returns a new <code>ResultSummary</code> record populated from the given arguments.
     * </p>
     * <p>
     * Note that all fields of the <code>ResultSummary</code> record appear as arguments.  Thus, this
     * creator is equivalent to the canonical constructor.
     * </p>
     * 
     * @param cntResultsTot total number of results within the original result collection
     * @param cntRatesGtAvg number of data rates greater than the average data rate
     * @param cntRatesGtTgt number of data rates greater than a predetermined target rate
     * @param dblRateMin    the minimum data rate seen in the result collection
     * @param dblRateMax    the maximum data rate seen in the result collection 
     * @param dblRateAvg    the average data rate within the result collection
     * @param dblRateStd    the data rate standard deviation of the result collection
     * @param szRqstAvg     average size (in bytes) of recovered data per request
     * @param cntRqstBlksAvg average number of correlated data blocks recovered per request
     * @param durRqstMin    the minimum request time duration observed within the result collection
     * @param durRqstMax    the maximum request time duration observed within the result collection
     * @param durRqstAvg    the average request time duration within the result collection
     * @param durRqstStd    the request duration standard deviation of the result collection
     * @param mapRqstHits   map of (request, hit count) pairs specifying the number of times a request appears in the collection
     * 
     * @return  a new <code>ResultSummary</code> record populated with the given arguments
     */
    public static ResultSummary from(
            int         cntResultsTot,
            int         cntRatesGtAvg,
            int         cntRatesGtTgt,
            double      dblRateMin,
            double      dblRateMax,
            double      dblRateAvg,
            double      dblRateStd,
            long        szRqstAvg,
            int         cntRqstBlksAvg,
            Duration    durRqstMin,
            Duration    durRqstMax,
            Duration    durRqstAvg,
            Duration    durRqstStd,
            Map<DpDataRequest, Integer> mapRqstHits) 
    {
        return new ResultSummary(
                cntResultsTot, 
                cntRatesGtAvg, 
                cntRatesGtTgt, 
                dblRateMin, 
                dblRateMax, 
                dblRateAvg, 
                dblRateStd, 
                szRqstAvg,
                cntRqstBlksAvg,
                durRqstMin,
                durRqstMax,
                durRqstAvg, 
                durRqstStd,
                mapRqstHits);
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
        ResultSummary.DBL_RATE_TARGET = dblRateTarget;
    }
    
    /**
     * <p>
     * Computes a summary of the performance results for the given collection and returns them.
     * </p>
     * <p>
     * All the fields of a <code>ResultSummary</code> record are computed from the given 
     * <code>ConfigResult</code> collection.  The computed values are then returned in a 
     * new <code>ConfigResult</code> instance.
     * </p>
     *  
     * @param setResults    collection of request processor results
     * 
     * @return  a new <code>ResultSummary</code> record containing a summary of the argument results
     */
    public static ResultSummary summarize(Collection<ConfigResult> setResults) {
        
        // Compute general summary results 
        int         cntResults = setResults.size();
        double      dblRateAvg = setResults.stream().mapToDouble(rec -> rec.dblRate()).sum()/cntResults;
        int         cntRatesGtAvg = setResults.stream().filter(rec -> rec.dblRate() >= dblRateAvg).mapToInt(rec -> 1).sum();
        int         cntRatesGtTgt = setResults.stream().filter(rec -> rec.dblRate() >= DBL_RATE_TARGET).mapToInt(rec -> 1).sum();
        
        // Compute the request size summaries
        long        szRqstAvg = setResults.stream().mapToLong(rec -> rec.szAlloc()).sum()/cntResults;
        int         cntRqstBlksAvg = setResults.stream().mapToInt(rec -> rec.cntBlocks()).sum()/cntResults;
        
        // Compute the request duration summary results
        Duration    durRqstAvg = setResults.stream().<Duration>map(rec -> rec.durRequest()).reduce(Duration.ZERO, (d1,d2) -> d1.plus(d2)).dividedBy(cntResults);
        Duration    durRqstMin = setResults.stream().<Duration>map(rec -> rec.durRequest()).reduce(durRqstAvg, (d1, d2) -> { if (d1.compareTo(d2) < 0) return d1; else return d2; } );
        Duration    durRqstMax = setResults.stream().<Duration>map(rec -> rec.durRequest()).reduce(durRqstAvg, (d1, d2) -> { if (d1.compareTo(d2) > 1) return d1; else return d2; } );
        
        double      dblRqstNsSqrd = setResults.stream().<Duration>map(rec -> rec.durRequest()).mapToLong(dur -> dur.toNanos()).mapToDouble(l -> Long.valueOf(l).doubleValue()).map(ns -> ns*ns).sum()/cntResults;
        double      dblRqstNsAvg = Long.valueOf(durRqstAvg.toNanos() ).doubleValue();
        double      dblRqstNsStd = Math.sqrt(dblRqstNsSqrd - dblRqstNsAvg*dblRqstNsAvg);
        Duration    durRqstStd = Duration.ofNanos(Double.valueOf(dblRqstNsStd).longValue());
        
        // Compute data rate statistics
        Double  dblRateMax = null;
        Double  dblRateMin = null;
        double  dblRateSqrd = 0.0;
        for (ConfigResult recResult : setResults) {
            double  dblRate = recResult.dblRate();
            
            // Initialize extremal values - first time through loop
            if (dblRateMax == null)
                dblRateMax = dblRate;
            if (dblRateMin == null)
                dblRateMin = dblRate;
            
            // Update min and max value
            if (dblRate > dblRateMax)
                dblRateMax = dblRate;
            if (dblRate < dblRateMin)
                dblRateMin = dblRate;
            
            // Running sum for standard deviation
            dblRateSqrd += (dblRate - dblRateAvg)*(dblRate - dblRateAvg);
        }
        double dblRateStd = Math.sqrt(dblRateSqrd/cntResults); // compute standard deviation

        // Compute request hit counts
        Map<DpDataRequest, Integer>     mapRqstHits = new HashMap<>();
        
        for (ConfigResult recResult : setResults) {
            DpDataRequest   rqst = recResult.request();
            Integer         intCnt = mapRqstHits.get(rqst);
            
            if (intCnt == null) {
                Integer intFirst = 1;
                mapRqstHits.put(rqst, intFirst);
            } else {
                Integer intNewCnt = ++intCnt;
                mapRqstHits.put(rqst, intNewCnt);
            }
        }
        
        // Create summary record and return it
        ResultSummary   recSummary = ResultSummary.from(
                cntResults, 
                cntRatesGtAvg, 
                cntRatesGtTgt, 
                dblRateMin, 
                dblRateMax, 
                dblRateAvg, 
                dblRateStd, 
                szRqstAvg,
                cntRqstBlksAvg,
                durRqstMin,
                durRqstMax,
                durRqstAvg,
                durRqstStd,
                mapRqstHits);
        
        return recSummary;
    }
    
    /**
     * <p>
     * Prints out a text description of the record contents to the given output.
     * </p>
     * 
     * @param ps    output sink to receive text description
     */ 
    public void printOut(PrintStream ps) {
        
        // Print out results  
        ps.println(" Result Summary ");
        ps.println("    Total number of result cases  : " + this.cntResultsTot);
        ps.println("    Cases w/ rates >= Avg Rate    : " + this.cntRatesGtAvg);
        ps.println("    Cases w/ rates >= Target      : " + this.cntRatesGtTgt);
        ps.println("    Target data rate (MBps)       : " + DBL_RATE_TARGET);
        ps.println("    Average data rate (MBps)      : " + this.dblRateAvg);
        ps.println("    Minimum data rate (MBps)      : " + this.dblRateMin);
        ps.println("    Maximum data rate (MBps)      : " + this.dblRateMax);
        ps.println("    Rate standard dev. (MBps)     : " + this.dblRateStd);
        ps.println("    Average request size (MBytes) : " + ((double)this.szRqstAvg)/1.0e6);
        ps.println("    Average request block count   : " + this.cntRqstBlksAvg);
        ps.println("    Minimum request duration      : " + this.durRqstMin);
        ps.println("    Maximum request duration      : " + this.durRqstMax);
        ps.println("    Average request duration      : " + this.durRqstAvg);
        ps.println("    Request duration stand. dev.  : " + this.durRqstStd);
        
        // Print out the request hit counts
        for (Map.Entry<DpDataRequest, Integer> entry : mapRqstHits.entrySet()) {
            String  strRqstId = entry.getKey().getRequestId();
            Integer cntHits = entry.getValue();
            
            ps.println("    Number of " + strRqstId + " requests : " + cntHits);
        }
    }

}
