/*
 * Project: dp-api-common
 * File:	QueryChannelTestsSummary.java
 * Package: com.ospreydcs.dp.jal.tools.query
 * Type: 	QueryChannelTestsSummary
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
 * @since May 9, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.channel;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.ospreydcs.dp.jal.tools.query.testrequests.TestArchiveRequest;

/**
 * <p>
 * Record for used for summarizing collections of performance results from time-series data request recovery and processing.
 * </p> 
 * <p>
 * This record takes collections of various test result records, computes a summary set, which is then available as the 
 * record fields.
 * </p> 
 * <p>
 * The summary fields contain the statistical properties of the data rates seen with the result collection,
 * up to second order (i.e., standard deviation).  The summary also contains the number of results above
 * the average value and above a predetermined target value.  A map containing (request, hit count) pairs
 * is also available for population.  It contains the number of hits for each request seen in the result
 * collection.
 * </p> 
 * <p>
 * The "allocation size" fields have different meaning in different context.  For correlator test the allocation
 * size is the number of bytes processed.  For query channel tests the allocation size is the total memory allocation
 * (in bytes) of the recovered data.
 * </p>
 * <p>
 * The "block count" fields have different meanings for different situations.  For example, in data correlator
 * tests the block count is the number of correlated blocks.  For query channel tests the block count is the
 * number of recovered <code>QueryData</code> messages.
 * </p>
 *
 * @param cntResultsTot total number of results within the original result collection
 * @param cntRatesGtAvg number of data rates greater than the average data rate
 * @param cntRatesGtTgt number of data rates greater than a predetermined target rate
 * @param dblRateMin    the minimum data rate seen in the result collection
 * @param dblRateMax    the maximum data rate seen in the result collection 
 * @param dblRateAvg    the average data rate within the result collection
 * @param dblRateStd    the data rate standard deviation of the result collection
 * @param cntMsgsMin    the minimum number of recovered data messages seen in a test
 * @param cntMsgsMax    the maximum number of recovered data messages seen in a test
 * @param cntMsgsAvg    average number of correlated data blocks per test
 * @param szRcvryMin    the minimum recovered allocation size (in bytes)
 * @param szRcvryMax    the maximum recovered allocation size (in bytes)
 * @param szRcvryAvg    average allocation size (in bytes) of recovered for each request 
 * @param durRqstMin    the minimum request time duration observed within the result collection
 * @param durRqstMax    the maximum request time duration observed within the result collection
 * @param durRqstAvg    the average request time duration within the result collection
 * @param durRqstStd    the request duration standard deviation of the result collection
 * @param mapRqstHits   map of (request, hit count) pairs specifying the number of times a request appears in the collection
 *
 * @author Christopher K. Allen
 * @since May 9, 2025
 *
 */
public record QueryChannelTestsSummary(
        int         cntResultsTot,
        int         cntRatesGtAvg,
        int         cntRatesGtTgt,
        double      dblRateMin,
        double      dblRateMax,
        double      dblRateAvg,
        double      dblRateStd,
        int         cntMsgsMin,
        int         cntMsgsMax,
        int         cntMsgsAvg,
        long        szRcvryMin,
        long        szRcvryMax,
        long        szRcvryAvg,
        Duration    durRqstMin,
        Duration    durRqstMax,
        Duration    durRqstAvg,
        Duration    durRqstStd,
        Map<TestArchiveRequest, Integer> mapRqstHits
        ) 
{

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates an returns a new <code>QueryChannelTestsSummary</code> record populated from the given arguments.
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
     * @param cntMsgsMin    the minimum number of correlated data blocks seen in a test
     * @param cntMsgsMax    the maximum number of correlated data blocks seen in a test
     * @param cntMsgsAvg    average number of correlated data blocks per test
     * @param szRcvryMin   the minimum memory allocation size (in bytes)
     * @param szRcvryMax   the maximum memory allocation size (in bytes)
     * @param szRcvryAvg    average allocation size (in bytes) of recovered/processed data 
     * @param durRqstMin    the minimum request time duration observed for all test operations
     * @param durRqstMax    the maximum request time duration observed within the result collection
     * @param durRqstAvg    the average request time duration within the result collection
     * @param durRqstStd    the request duration standard deviation of the result collection
     * 
     * @return  a new <code>QueryChannelTestsSummary</code> record populated with the given arguments
     */
    public static QueryChannelTestsSummary from(
            int         cntResultsTot,
            int         cntRatesGtAvg,
            int         cntRatesGtTgt,
            double      dblRateMin,
            double      dblRateMax,
            double      dblRateAvg,
            double      dblRateStd,
            int         cntMsgsMin,
            int         cntMsgsMax,
            int         cntMsgsAvg,
            long        szRcvryMin,
            long        szRcvryMax,
            long        szRcvryAvg,
            Duration    durRqstMin,
            Duration    durRqstMax,
            Duration    durRqstAvg,
            Duration    durRqstStd
            ) 
    {
        return new QueryChannelTestsSummary(
                cntResultsTot,
                cntRatesGtAvg,
                cntRatesGtTgt,
                dblRateMin,
                dblRateMax,
                dblRateAvg,
                dblRateStd,
                cntMsgsMin,
                cntMsgsMax,
                cntMsgsAvg,
                szRcvryMin,
                szRcvryMax,
                szRcvryAvg,
                durRqstMin,
                durRqstMax,
                durRqstAvg,
                durRqstStd,
                new HashMap<TestArchiveRequest, Integer>());
    }
    
    /**
     * <p>
     * Creates an returns a new <code>QueryChannelTestsSummary</code> record populated from the given arguments.
     * </p>
     * <p>
     * Note that all fields of the <code>QueryChannelTestsSummary</code> record appear as arguments.  Thus, this
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
     * @param cntMsgsMin    the minimum number of correlated data blocks seen in a test
     * @param cntMsgsMax    the maximum number of correlated data blocks seen in a test
     * @param cntMsgsAvg    average number of correlated data blocks per test
     * @param szRcvryMin   the minimum memory allocation size (in bytes)
     * @param szRcvryMax   the maximum memory allocation size (in bytes)
     * @param szRcvryAvg    average allocation size (in bytes) of recovered/processed data 
     * @param durRqstMin    the minimum request time duration observed for all test operations
     * @param durRqstMax    the maximum request time duration observed within the result collection
     * @param durRqstAvg    the average request time duration within the result collection
     * @param durRqstStd    the request duration standard deviation of the result collection
     * @param mapRqstHits   map of (request, hit count) pairs specifying the number of times a request appears in the collection
     * 
     * @return  a new <code>ResultSummary</code> record populated with the given arguments
     */
    public static QueryChannelTestsSummary from(
            int         cntResultsTot,
            int         cntRatesGtAvg,
            int         cntRatesGtTgt,
            double      dblRateMin,
            double      dblRateMax,
            double      dblRateAvg,
            double      dblRateStd,
            int         cntMsgsMin,
            int         cntMsgsMax,
            int         cntMsgsAvg,
            long        szRcvryMin,
            long        szRcvryMax,
            long        szRcvryAvg,
            Duration    durRqstMin,
            Duration    durRqstMax,
            Duration    durRqstAvg,
            Duration    durRqstStd,
            Map<TestArchiveRequest, Integer> mapRqstHits) 
    {
        return new QueryChannelTestsSummary(
                cntResultsTot,
                cntRatesGtAvg,
                cntRatesGtTgt,
                dblRateMin,
                dblRateMax,
                dblRateAvg,
                dblRateStd,
                cntMsgsMin,
                cntMsgsMax,
                cntMsgsAvg,
                szRcvryMin,
                szRcvryMax,
                szRcvryAvg,
                durRqstMin,
                durRqstMax,
                durRqstAvg,
                durRqstStd,
                mapRqstHits);
    }
    
    
    //
    // Record Constants
    //
    
    /** The format string used for printing out data rates from a test result collection */ 
    private static final String      STR_DATA_RATE_FMTR = "  %7.3f MBps for Case #%d with %7.3f MBytes allocation";
    
    /** The function that converts <code>QueryChannelTestResult</code> records to strings with data rate */
    private static final Function<QueryChannelTestResult, String>  FNC_DATA_RATE = rec -> {
        int     intIndex = rec.recTestCase().indCase();
        double  dblRate = rec.dblDataRate();
        double  dblAlloc = ((double)rec.szRecovery())/1_000_000;
        
        String  strLine = String.format(STR_DATA_RATE_FMTR, dblRate, intIndex, dblAlloc);
        
        return strLine;
    };
        
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
    
    /**
     * <p>
     * Prints out the line-by-line data rates for each test result in the argument collection.
     * </p>
     * <p>
     * A line-by-line text description of each record field <code>{@link QueryChannelTestResult#dblDataRate()}</code>
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
    public static void printOutDataRates(PrintStream ps, String strPad, Collection<QueryChannelTestResult> setResults) {
        final String    STR_PAD = (strPad == null) ? "" : "  ";

        setResults.stream().<String>map(FNC_DATA_RATE).forEach(str -> ps.println(STR_PAD + str));
    }

    /**
     * <p>
     * Computes a summary of the performance results for the given collection and returns them.
     * </p>
     * <p>
     * All the fields of a <code>QueryChannelTestsSummary</code> record are computed from the given 
     * collection of test result records.  The computed values are then returned in a 
     * new <code>QueryChannelTestsSummary</code> instance.
     * </p>
     *  
     * @param setResults    collection of test results
     * 
     * @return  a new <code>QueryChannelTestsSummary</code> record containing a summary of the argument results
     */
    public static QueryChannelTestsSummary summarize(Collection<QueryChannelTestResult> setResults) {
        
        // Compute general summary results 
        int         cntResults = setResults.size();
        
        double      dblRateAvg = setResults.stream().mapToDouble(rec -> rec.dblDataRate()).sum()/cntResults;
        int         cntRatesGtAvg = setResults.stream().filter(rec -> rec.dblDataRate() >= dblRateAvg).mapToInt(rec -> 1).sum();
        int         cntRatesGtTgt = setResults.stream().filter(rec -> rec.dblDataRate() >= DBL_RATE_TARGET).mapToInt(rec -> 1).sum();
        
        double      dblRateMin = setResults.stream().mapToDouble(rec -> rec.dblDataRate()).reduce(dblRateAvg, (r1, r2) -> { if (r1<r2) return r1; else return r2; } );
        double      dblRateMax = setResults.stream().mapToDouble(rec -> rec.dblDataRate()).reduce(dblRateAvg, (r1, r2) -> { if (r1>r2) return r1; else return r2; } );
        double      dblRateSqrd = setResults.stream().mapToDouble(rec -> rec.dblDataRate()).map(r -> (r-dblRateAvg)*(r-dblRateAvg)).sum();
        double      dblRateStd = Math.sqrt(dblRateSqrd/cntResults); // compute standard deviation
        
        // Compute the recovered message count statistics
        int         cntMsgsAvg = setResults.stream().mapToInt(rec -> rec.cntMessages()).sum()/cntResults;
        int         cntMsgsMin = setResults.stream().mapToInt(rec -> rec.cntMessages()).reduce(cntMsgsAvg, (b1, b2) -> { if (b1<b2) return b1; else return b2; } );
        int         cntMsgsMax = setResults.stream().mapToInt(rec -> rec.cntMessages()).reduce(cntMsgsMin, (b1, b2) -> { if (b1>b2) return b1; else return b2; } );
        
        // Compute the recovered memory size statistics
        long        szRcvryAvg = setResults.stream().mapToLong(rec -> rec.szRecovery()).sum()/cntResults;
        long        szRcvryMin = setResults.stream().mapToLong(rec -> rec.szRecovery()).reduce(szRcvryAvg, (s1, s2) -> { if (s1<s2) return s1; else return s2;} );
        long        szRcvryMax = setResults.stream().mapToLong(rec -> rec.szRecovery()).reduce(szRcvryAvg, (s1, s2) -> { if (s1>s2) return s1; else return s2;} );
        
        // Compute the request duration summary results
        Duration    durRqstAvg = setResults.stream().<Duration>map(rec -> rec.durRecovery()).reduce(Duration.ZERO, (d1,d2) -> d1.plus(d2)).dividedBy(cntResults);
        Duration    durRqstMin = setResults.stream().<Duration>map(rec -> rec.durRecovery()).reduce(durRqstAvg, (d1, d2) -> { if (d1.compareTo(d2) < 0) return d1; else return d2; } );
        Duration    durRqstMax = setResults.stream().<Duration>map(rec -> rec.durRecovery()).reduce(durRqstAvg, (d1, d2) -> { if (d1.compareTo(d2) > 0) return d1; else return d2; } );
        
        double      dblRqstNsSqrd = setResults.stream().<Duration>map(rec -> rec.durRecovery()).mapToLong(dur -> dur.toNanos()).mapToDouble(l -> Long.valueOf(l).doubleValue()).map(ns -> ns*ns).sum()/cntResults;
        double      dblRqstNsAvg = Long.valueOf(durRqstAvg.toNanos() ).doubleValue();
        double      dblRqstNsStd = Math.sqrt(dblRqstNsSqrd - dblRqstNsAvg*dblRqstNsAvg);
        Duration    durRqstStd = Duration.ofNanos(Double.valueOf(dblRqstNsStd).longValue());
        
        // Compute request hit counts
        Map<TestArchiveRequest, Integer>    mapRqstHits = new HashMap<>();
        
        for (QueryChannelTestResult recResult : setResults) {
            TestArchiveRequest  rqst = recResult.recTestCase().enmRqstOrg();
            Integer             intCnt = mapRqstHits.get(rqst);
            
            if (intCnt == null) {
                Integer intFirst = 1;
                mapRqstHits.put(rqst, intFirst);
            } else {
                Integer intNewCnt = ++intCnt;
                mapRqstHits.put(rqst, intNewCnt);
            }
        }
        
        // Create summary record and return it
        QueryChannelTestsSummary   recSummary = QueryChannelTestsSummary.from(
                cntResults, 
                cntRatesGtAvg, 
                cntRatesGtTgt, 
                dblRateMin, 
                dblRateMax, 
                dblRateAvg, 
                dblRateStd, 
                cntMsgsMin,
                cntMsgsMax,
                cntMsgsAvg,
                szRcvryMin,
                szRcvryMax,
                szRcvryAvg,
                durRqstMin,
                durRqstMax,
                durRqstAvg,
                durRqstStd,
                mapRqstHits);
        
        return recSummary;
    }
    
//    /**
//     * <p>
//     * Computes a summary of the performance results for the given collection and returns them.
//     * </p>
//     * <p>
//     * All the fields of a <code>QueryChannelTestsSummary</code> record are computed from the given 
//     * collection of test result records.  The computed values are then returned in a 
//     * new <code>QueryChannelTestsSummary</code> instance.
//     * </p>
//     *  
//     * @param setResults    collection of test results
//     * 
//     * @return  a new <code>QueryChannelTestsSummary</code> record containing a summary of the argument results
//     */
//    public static QueryChannelTestsSummary summarizeCorrelatorResults(Collection<CorrelatorTestResult> setResults) {
//        
//        // Compute general summary results 
//        int         cntResults = setResults.size();
//        
//        double      dblRateAvg = setResults.stream().mapToDouble(rec -> rec.dblDataRate()).sum()/cntResults;
//        int         cntRatesGtAvg = setResults.stream().filter(rec -> rec.dblDataRate() >= dblRateAvg).mapToInt(rec -> 1).sum();
//        int         cntRatesGtTgt = setResults.stream().filter(rec -> rec.dblDataRate() >= DBL_RATE_TARGET).mapToInt(rec -> 1).sum();
//        
//        double      dblRateMin = setResults.stream().mapToDouble(rec -> rec.dblDataRate()).reduce(dblRateAvg, (r1, r2) -> { if (r1<r2) return r1; else return r2; } );
//        double      dblRateMax = setResults.stream().mapToDouble(rec -> rec.dblDataRate()).reduce(dblRateAvg, (r1, r2) -> { if (r1>r2) return r1; else return r2; } );
//        double      dblRateSqrd = setResults.stream().mapToDouble(rec -> rec.dblDataRate()).map(r -> (r-dblRateAvg)*(r-dblRateAvg)).sum();
//        double      dblRateStd = Math.sqrt(dblRateSqrd/cntResults); // compute standard deviation
//        
//        // Compute the recovered memory size statistics
//        long        szPrcdAvg = setResults.stream().mapToLong(rec -> rec.szProcessed()).sum()/cntResults;
//        long        szPrcdMin = setResults.stream().mapToLong(rec -> rec.szProcessed()).reduce(szPrcdAvg, (s1, s2) -> { if (s1<s2) return s1; else return s2;} );
//        long        szPrcdMax = setResults.stream().mapToLong(rec -> rec.szProcessed()).reduce(szPrcdAvg, (s1, s2) -> { if (s1>s2) return s1; else return s2;} );
//        
//        // Compute the recovered block count (message count) statistics
//        int         cntMsgsAvg = setResults.stream().mapToInt(rec -> rec.cntCorrelSet()).sum()/cntResults;
//        int         cntMsgsMin = setResults.stream().mapToInt(rec -> rec.cntCorrelSet()).reduce(cntMsgsAvg, (b1, b2) -> { if (b1<b2) return b1; else return b2; } );
//        int         cntMsgsMax = setResults.stream().mapToInt(rec -> rec.cntCorrelSet()).reduce(cntMsgsMin, (b1, b2) -> { if (b1>b2) return b1; else return b2; } );
//        
//        // Compute the request duration summary results
//        Duration    durPrcdAvg = setResults.stream().<Duration>map(rec -> rec.durProcessed()).reduce(Duration.ZERO, (d1,d2) -> d1.plus(d2)).dividedBy(cntResults);
//        Duration    durPrcdMin = setResults.stream().<Duration>map(rec -> rec.durProcessed()).reduce(durPrcdAvg, (d1, d2) -> { if (d1.compareTo(d2) < 0) return d1; else return d2; } );
//        Duration    durPrcdMax = setResults.stream().<Duration>map(rec -> rec.durProcessed()).reduce(durPrcdAvg, (d1, d2) -> { if (d1.compareTo(d2) > 1) return d1; else return d2; } );
//        
//        double      dblPrcdNsSqrd = setResults.stream().<Duration>map(rec -> rec.durProcessed()).mapToLong(dur -> dur.toNanos()).mapToDouble(l -> Long.valueOf(l).doubleValue()).map(ns -> ns*ns).sum()/cntResults;
//        double      dblPrcdNsAvg = Long.valueOf(durPrcdAvg.toNanos() ).doubleValue();
//        double      dblPrcdNsStd = Math.sqrt(dblPrcdNsSqrd - dblPrcdNsAvg*dblPrcdNsAvg);
//        Duration    durPrcdStd = Duration.ofNanos(Double.valueOf(dblPrcdNsStd).longValue());
//        
//        // Compute request hit counts
//        Map<TestArchiveRequest, Integer>    mapRqstHits = new HashMap<>();
//        
//        for (CorrelatorTestResult recResult : setResults) {
//            TestArchiveRequest  rqst = recResult.recTestCase().enmRqst();
//            Integer             intCnt = mapRqstHits.get(rqst);
//            
//            if (intCnt == null) {
//                Integer intFirst = 1;
//                mapRqstHits.put(rqst, intFirst);
//            } else {
//                Integer intNewCnt = ++intCnt;
//                mapRqstHits.put(rqst, intNewCnt);
//            }
//        }
//        
//        // Create summary record and return it
//        QueryChannelTestsSummary   recSummary = QueryChannelTestsSummary.from(
//                cntResults, 
//                cntRatesGtAvg, 
//                cntRatesGtTgt, 
//                dblRateMin, 
//                dblRateMax, 
//                dblRateAvg, 
//                dblRateStd, 
//                szPrcdMin,
//                szPrcdMax,
//                szPrcdAvg,
//                cntMsgsMin,
//                cntMsgsMax,
//                cntMsgsAvg,
//                durPrcdMin,
//                durPrcdMax,
//                durPrcdAvg,
//                durPrcdStd,
//                mapRqstHits);
//        
//        return recSummary;
//    }
//    
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
    public void printOutChannelSummary(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        
        // Print out results  
        ps.println(strPad + "Tests Result Summary ");
        ps.println(strPad + "  Total number of result cases   : " + this.cntResultsTot);
        ps.println(strPad + "  Cases w/ rates >= Avg Rate     : " + this.cntRatesGtAvg);
        ps.println(strPad + "  Cases w/ rates >= Target       : " + this.cntRatesGtTgt);
        ps.println(strPad + "  Target data rate (MBps)        : " + DBL_RATE_TARGET);
        ps.println(strPad + "  Minimum data rate (MBps)       : " + this.dblRateMin);
        ps.println(strPad + "  Maximum data rate (MBps)       : " + this.dblRateMax);
        ps.println(strPad + "  Average data rate (MBps)       : " + this.dblRateAvg);
        ps.println(strPad + "  Rate standard dev. (MBps)      : " + this.dblRateStd);
        ps.println(strPad + "  Minimum recovered msg count    : " + this.cntMsgsMin);
        ps.println(strPad + "  Maximum recovered msg count    : " + this.cntMsgsMax);
        ps.println(strPad + "  Average recovered msg count    : " + this.cntMsgsAvg);
        ps.println(strPad + "  Minimum recovery size (MBytes) : " + ((double)this.szRcvryMin)/1.0e6);
        ps.println(strPad + "  Maximum recovery size (MBytes) : " + ((double)this.szRcvryMax)/1.0e6);
        ps.println(strPad + "  Average recovery size (MBytes) : " + ((double)this.szRcvryAvg)/1.0e6);
        ps.println(strPad + "  Minimum request duration       : " + this.durRqstMin);
        ps.println(strPad + "  Maximum request duration       : " + this.durRqstMax);
        ps.println(strPad + "  Average request duration       : " + this.durRqstAvg);
        ps.println(strPad + "  Request duration stand. dev.   : " + this.durRqstStd);
        
        // Print out the request hit counts
        for (Map.Entry<TestArchiveRequest, Integer> entry : mapRqstHits.entrySet()) {
            String  strRqstId = entry.getKey().name();
            Integer cntHits = entry.getValue();
            
            ps.println(strPad + "  Number of " + strRqstId + " requests : " + cntHits);
        }
    }

    

}
