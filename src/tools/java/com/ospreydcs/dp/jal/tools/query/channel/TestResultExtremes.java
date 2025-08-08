/*
 * Project: dp-api-common
 * File:	TestResultExtremes.java
 * Package: com.ospreydcs.dp.jal.tools.query.channel
 * Type: 	TestResultExtremes
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
 * @since May 17, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.channel;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import  java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.jal.tools.query.testrequests.TestArchiveRequest;

/**
 * <p>
 * Record containing the extreme values from a collection of <code>QueryChannelTestResult</code> collection.
 * </p>
 * <p>
 * The record is intended to encapsulate the extreme values (i.e., the "best" and the "worst") from a collection
 * of <code>QueryChannelTestResult</code> records.  The collection is assumed to be the result of a 
 * <code>QueryChannelTestSuite</code> evaluation and this record provides additional means for summarizing 
 * the results.  
 * </p>
 * <p>
 * The method <code>{@link #computeExtremes(Collection)}</code> is the preferred means for creating 
 * <code>TestResultExtremes</code> records.  This is the primary operation.
 * </p>
 * <p>
 * Note that <code>{@link #computeExtremes(Collection)}</code> relies on the internal methods
 * <code>{@link #computeBestDecompTypes(Collection)}, {@link #computeBestGrpcStreamTypes(Collection)},
 * {@link #computeBestResults(Collection)}, {@link #computeBestStreamCounts(Collection)}</code>, and 
 * <code>{@link #computeBestTestArchiveRequests(Collection)}</code> to compute the results.
 * However, these methods are left public in case they are of service.
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 17, 2025
 *
 * @param recResultBest     the best test result encountered
 * @param recResultWorst    the worst test result encountered
 * @param pairRqstTypeBest  the best test request encountered (with average data rate)
 * @param pairRqstTypeWorst the worst test request encountered (with average data rate)
 * @param pairDcmpTypeBest  the best request decomposition strategy encountered (with average data rate)
 * @param pairDcmpTypeWorst the worst request decomposition strategy encountered (with average data rate)
 * @param pairStrmCntBest   the best gRPC stream count encountered (with average data rate)
 * @param pairStrmCntWorst  the worst gRPC stream count encountered (with average data rate)
 * @param pairStrmTypeBest  the best gRPC stream type encountered (with average data rate)
 * @param pairStrmTypeWorst the worst gRPC stream type encountered (with average data rate)
 */
public record TestResultExtremes(
        QueryChannelTestResult              recResultBest,
        QueryChannelTestResult              recResultWorst,
        Entry<Double, TestArchiveRequest>   pairRqstTypeBest,
        Entry<Double, TestArchiveRequest>   pairRqstTypeWorst,
        Entry<Double, RequestDecompType>    pairDcmpTypeBest,
        Entry<Double, RequestDecompType>    pairDcmpTypeWorst,
        Entry<Double, Integer>              pairStrmCntBest,
        Entry<Double, Integer>              pairStrmCntWorst,
        Entry<Double, DpGrpcStreamType>     pairStrmTypeBest,
        Entry<Double, DpGrpcStreamType>     pairStrmTypeWorst
        ) 
{
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new <code>TestResultExtremes</code> record with fields given by the arguments.
     * </p>
     * 
     * @param recResultBest     the best test result encountered
     * @param recResultWorst    the worst test result encountered
     * @param pairRqstTypeBest  the best test request encountered (with average data rate)
     * @param pairRqstTypeWorst the worst test request encountered (with average data rate)
     * @param pairDcmpTypeBest  the best request decomposition strategy encountered (with average data rate)
     * @param pairDcmpTypeWorst the worst request decomposition strategy encountered (with average data rate)
     * @param pairStrmCntBest   the best gRPC stream count encountered (with average data rate)
     * @param pairStrmCntWorst  the worst gRPC stream count encountered (with average data rate)
     * @param pairStrmTypeBest  the best gRPC stream type encountered (with average data rate)
     * @param pairStrmTypeWorst the worst gRPC stream type encountered (with average data rate)
     * 
     * @return  a new <code>TestResultExtremes</code> record populated by the given arguments
     */
    public static TestResultExtremes    from(
            QueryChannelTestResult              recResultBest,
            QueryChannelTestResult              recResultWorst,
            Entry<Double, TestArchiveRequest>   pairRqstTypeBest,
            Entry<Double, TestArchiveRequest>   pairRqstTypeWorst,
            Entry<Double, RequestDecompType>    pairDcmpTypeBest,
            Entry<Double, RequestDecompType>    pairDcmpTypeWorst,
            Entry<Double, Integer>              pairStrmCntBest,
            Entry<Double, Integer>              pairStrmCntWorst,
            Entry<Double, DpGrpcStreamType>     pairStrmTypeBest,
            Entry<Double, DpGrpcStreamType>     pairStrmTypeWorst
            ) 
    {
        return new TestResultExtremes(
                recResultBest,
                recResultWorst,
                pairRqstTypeBest,
                pairRqstTypeWorst,
                pairDcmpTypeBest,
                pairDcmpTypeWorst,
                pairStrmCntBest,
                pairStrmCntWorst,
                pairStrmTypeBest,
                pairStrmTypeWorst
                );
    }
    
    
    // 
    // Operations
    //
    
    /**
     * <p>
     * Computes and returns the extreme values ("best" and "worst") for each record field within the given record collection.
     * </p>
     * <p>
     * The "best" and "worst" performing fields are identified for the given collection of 
     * <code>{@link QueryChannelTestResult}</code> records.  The criterion for the best and worst qualifier
     * is the average data rate for all records with a targeted field value.    
     * Once identified, these extreme field values are saved to a new <code>TestResultExtremes</code> record and returned.
     * </p>
     * <p>
     * <h2>Operation</h2>
     * The method relies on multiple internal (static) methods to create ordered <code>{@link TreeMap}</code> 
     * containers keyed by the average data rate for a particular field and field value.  That is, the map
     * has keys of average data rate, and values ranging over the valid values of the <code>QueryChannelTestCase</code>
     * record, which is the test case within each <code>QueryChannelTestResult</code> record within the argument.  
     * There is one such method for each field within <code>{@link QueryChannelTestCase}</code>, plus an additional
     * method for computing the overall <code>QueryChannelTestResult</code> extremes.  
     * Once a map for the test case field is created, the extreme values can be obtained via the 
     * <code>{@link TreeMap#lastEntry()}</code> and <code>{@link TreeMap#firstEntry()}</code> methods.
     * </p>
     *  
     * @param setResults    collection of <code>QueryChannelTestResult</code> records under inspection
     * 
     * @return  new <code>TestResultExtremes</code> record identifying the extreme field values within the argument collection
     * 
     * @throws NoSuchElementException   either the argument was empty or contained a record with invalid field value
     * 
     * @see #computeBestTestResults(Collection)
     * @see #computeBestTestArchiveRequests(Collection)
     * @see #computeBestDecompTypes(Collection)
     * @see #computeBestStreamCounts(Collection)
     * @see #computeBestStreamTypes(Collection)
     */
    public static TestResultExtremes    computeExtremes(Collection<QueryChannelTestResult> setResults) throws NoSuchElementException {
        
        // Compute all the results
        TreeMap<Double, QueryChannelTestResult> mapRateToRslt = computeBestTestResults(setResults);
        TreeMap<Double, TestArchiveRequest>     mapRateToRqstType = computeBestTestArchiveRequests(setResults);
        TreeMap<Double, RequestDecompType>      mapRateToDcmpType = computeBestDecompTypes(setResults);
        TreeMap<Double, Integer>                mapRateToStrmCnt = computeBestStreamCounts(setResults);
        TreeMap<Double, DpGrpcStreamType>       mapRateToStrmType = computeBestStreamTypes(setResults);
        
        // Pick off the best and worst values saving them to a new record
        TestResultExtremes  recExtremes = TestResultExtremes.from(
                mapRateToRslt.lastEntry().getValue(),       mapRateToRslt.firstEntry().getValue(), 
                mapRateToRqstType.lastEntry(),              mapRateToRqstType.firstEntry(), 
                mapRateToDcmpType.lastEntry(),              mapRateToDcmpType.firstEntry(), 
                mapRateToStrmCnt.lastEntry(),               mapRateToStrmCnt.firstEntry(), 
                mapRateToStrmType.lastEntry(),              mapRateToStrmType.firstEntry()
                );
        
        return recExtremes;
    }
    
    /**
     * <p>
     * Returns the number of records within the argument collection with data rates greater than or equal to the given rate.
     * </p>
     * <p>
     * The method inspects the field <code>{@link QueryChannelTestResult#dblDataRate()}</code> of the argument collection for 
     * the condition <code>{@link #dblDataRate}</code> &ge; <code>dblRateMin</code>.  The number of records satisfying this 
     * condition are counted and that value is returned.
     * </p>
     * 
     * @param setResults    collection of <code>QueryChannelTestResult</code> records under inspection
     * @param dblRateMin    the minimum data rate for consideration 
     * 
     * @return  the number of <code>ConfigResult</code> records within the collection with data rates >= to the given rate 
     */
    public static int   countRatesGreaterEqual(Collection<QueryChannelTestResult> setResults, double dblRateMin) {
        int intCnt = setResults
                .stream()
                .filter(rec -> rec.dblDataRate() >= dblRateMin)
                .mapToInt(rec -> 1)
                .sum();
        
        return intCnt;
    }
    
    /**
     * <p>
     * Prints out a text description of the current record (field-by-field) to the given output stream.
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
        
        // Print out results  
        ps.println(strPad + "Tests Result Extremes");
        ps.println(strPad + "  Best test case  = #" + this.recResultBest.recTestCase().indCase() + ": data rate = " + this.recResultBest.dblDataRate() + " (MBps)");
        ps.println(strPad + "  Worst test case = #" + this.recResultWorst.recTestCase().indCase() + ": data rate = " + this.recResultWorst.dblDataRate() + " (MBps)");
        ps.println(strPad + "  Best test request  = " + this.pairRqstTypeBest.getValue() + ": avg. rate = " + this.pairRqstTypeBest.getKey() + " (MBps)");
        ps.println(strPad + "  Worst test request = " + this.pairRqstTypeWorst.getValue() + ": avg. rate = " + this.pairRqstTypeWorst.getKey() + " (MBps)");
        ps.println(strPad + "  Best decomposition strategy  = " + this.pairDcmpTypeBest.getValue() + ": avg. rate = " + this.pairDcmpTypeBest.getKey() + " (MBps)");
        ps.println(strPad + "  Worst decomposition strategy = " + this.pairDcmpTypeWorst.getValue() + ": avg. rate = " + this.pairDcmpTypeWorst.getKey() + " (MBps)");
        ps.println(strPad + "  Best gRPC stream count  = " + this.pairStrmCntBest.getValue() + ": avg. rate = " + this.pairStrmCntBest.getKey() + " (MBps)");
        ps.println(strPad + "  Worst gRPC stream count = " + this.pairStrmCntWorst.getValue() + ": avg. rate = " + this.pairStrmCntWorst.getKey() + " (MBps)");
        ps.println(strPad + "  Best gRPC stream type  = " + this.pairStrmTypeBest.getValue() + ": avg. rate = " + this.pairStrmTypeBest.getKey() + " (MBps)");
        ps.println(strPad + "  Worst gRPC stream type = " + this.pairStrmTypeWorst.getValue() + ": avg. rate = " + this.pairStrmTypeWorst.getKey() + " (MBps)");
    }
    

    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates a new ordered map according to the data rates within the given result set.
     * </p>
     * <p>
     * The returned map has an ascending (natural) ordered by data rate for each result record in the argument collection. 
     * Thus, the best value can be obtained from <code>{@link TreeMap#lastEntry()}</code> and the worst value
     * can be obtained from <code>{@link TreeMap#firstEntry()}</code>.
     * </p>
     * 
     * @param setResults    collection of <code>QueryChannelTestResult</code> records under inspection
     * 
     * @return  the pairs (avg rate, test result) found to be the "best" amount the argument collection
     */
    private static TreeMap<Double, QueryChannelTestResult>   computeBestTestResults(Collection<QueryChannelTestResult> setResults) {
        
        TreeMap<Double, QueryChannelTestResult> mapRateToResult = new TreeMap<>();

        setResults.forEach(rec -> mapRateToResult.put(rec.dblDataRate(), rec));
        
        return mapRateToResult;
    }

    /**
     * <p>
     * Computes and returns the test archive request with the best average data rate within the given record set.
     * </p>
     * <p>
     * The result records for each test archive request (i.e., <code>{@link TestArchiveRequest}</code>
     * are isolated and the average data rate is computed for each.
     * The request with the best average data rate is identified and returned as a <code>{@link Map.Entry}</code>
     * pair where the <code>Double</code> value is the average data rate and the <code>TestArchiveRequest</code> 
     * value is the test archive request achieving that average rate (i.e., the "best" request). 
     * </p>
     * <p>
     * The returned map is ordered ascending according to the natural ordering provided by <code>Double</code>.
     * Thus, the best value can be obtained from <code>{@link TreeMap#lastEntry()}</code> and the worst value
     * can be obtained from <code>{@link TreeMap#firstEntry()}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The data rate average for each stream count is taken over only those available.  For example, if the result set
     * contains only one record with stream <code>{@link TestArchiveRequest#GENERAL_2000PVSx10SEC}</code> then that 
     * value is considered the average.  Thus, statistics can be easily skewed for small result sets.
     * </p>
     *
     * @param setResults    collection of <code>QueryChannelTestResult</code> records under inspection
     * 
     * @return  the pairs (avg rate, request type) found to be the "best" amount the argument collection
     * 
     * @throws NoSuchElementException   either the argument was empty or no valid request type existed (error)
     */
    private static TreeMap<Double, TestArchiveRequest> computeBestTestArchiveRequests(Collection<QueryChannelTestResult> setResults) throws NoSuchElementException {

        // Get the test request used - avoid iterating through all TestArchiveRequest constants
        List<TestArchiveRequest>    lstRqsts = setResults
                .stream()
                .<TestArchiveRequest>map(rec -> rec.recTestCase().enmRqstOrg())
                .toList();

        // Get the average rate for each request
        TreeMap<Double, TestArchiveRequest> mapRateToRqst = new TreeMap<>();

        for (TestArchiveRequest enmRqst : lstRqsts) {
            try {
                // Check if we have already processed request
                if (mapRateToRqst.containsValue(enmRqst))
                    continue;
                
                Double  dblRate = setResults
                        .stream()
                        .filter(rec -> rec.recTestCase().enmRqstOrg() == enmRqst)
                        .mapToDouble(rec -> rec.dblDataRate())
                        .average()
                        .orElseThrow();

                mapRateToRqst.put(dblRate, enmRqst);

            } catch (NoSuchElementException e) {
                // No entry - should not occur (i.e., this is an exception)
            }
        }

        //        // Get the request with the best average data rate and return it
        //        Map.Entry<Double, TestArchiveRequest>   entryBestRqst = mapRateToRqst.lastEntry();
        //        
        //        return entryBestRqst;
        return mapRateToRqst;
    }

    /**
     * <p>
     * Computes and returns the request decomposition strategy with the best average data rate 
     * within the given record set.
     * </p>
     * <p>
     * The result records for each request decomposition strategy (i.e., <code>{@link RequestDecompType}</code>
     * are isolated and the average data rate is computed for each.
     * The strategy with the best average data rate is identified and returned as a <code>{@link Map.Entry}</code>
     * pair where the <code>Double</code> value is the average data rate and the <code>RequestDecompType</code> 
     * value is the request decomposition strategy achieving that average rate (i.e., the "best" strategy). 
     * </p>
     * <p>
     * The returned map is ordered ascending according to the natural ordering provided by <code>Double</code>.
     * Thus, the best value can be obtained from <code>{@link TreeMap#lastEntry()}</code> and the worst value
     * can be obtained from <code>{@link TreeMap#firstEntry()}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The data rate average for each stream count is taken over only those available.  For example, if the result set
     * contains only one record with strategy <code>{@link RequestDecompType#NONE}</code> then that value is considered 
     * the average.  Thus, statistics can be easily skewed for small result sets.
     * </p>
     *
     * @param setResults    collection of <code>QueryChannelTestResult</code> records under inspection
     * 
     * @return  the pairs (avg rate, decomposition type) found to be the "best" amount the argument collection
     * 
     * @throws NoSuchElementException   either the argument was empty or no valid decomposition strategy existed (error)
     */
    private static TreeMap<Double, RequestDecompType>  computeBestDecompTypes(Collection<QueryChannelTestResult> setResults) throws NoSuchElementException {

        // Get the average data rate for each request decomposition strategy
        TreeMap<Double, RequestDecompType>  mapRateToDcmp = new TreeMap<>();

        for (RequestDecompType enmType : RequestDecompType.values()) {
            
            if (mapRateToDcmp.containsValue(enmType))
                continue;
            
            try {
                Double dblRate = setResults
                        .stream()
                        .filter(rec -> rec.recTestCase().enmDcmpType() == enmType)
                        .mapToDouble(rec -> rec.dblDataRate())
                        .average()
                        .orElseThrow();

                mapRateToDcmp.put(dblRate, enmType);

            } catch (NoSuchElementException e) {
                // No entry - maybe a decomposition type was not used considered
            }
        }

        return mapRateToDcmp;
    }

    /**
     * <p>
     * Computes and returns the stream count with the best average data rate within the given record set.
     * </p>
     * <p>
     * The result records for each data stream count are isolated and the average data rate is computed for each.
     * The stream count with the best average data rate is identified and returned as a <code>{@link Map.Entry}</code>
     * pair where the <code>Double</code> value is the average data rate and the <code>Integer</code> value is the
     * stream count achieving that average rate (i.e., the "best" stream count). 
     * </p>
     * <p>
     * The returned map is ordered ascending according to the natural ordering provided by <code>Double</code>.
     * Thus, the best value can be obtained from <code>{@link TreeMap#lastEntry()}</code> and the worst value
     * can be obtained from <code>{@link TreeMap#firstEntry()}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The data rate average for each stream count is taken over only those available.  For example, if the result set
     * contains only one record with stream count <i>N</i> then that value is considered the average.  Thus, statistics
     * can be easily skewed for small result sets.
     * </p>
     *
     * @param setResults    collection of <code>QueryChannelTestResult</code> records under inspection
     * 
     * @return  the pairs (avg rate, stream count) found to be the "best" amount the argument collection
     * 
     * @throws NoSuchElementException   either the argument was empty or no maximum stream count existed (error)
     */
    private static TreeMap<Double, Integer> computeBestStreamCounts(Collection<QueryChannelTestResult> setResults) throws NoSuchElementException {
        
        // Get the maximum stream count within the result set
        final int   intMaxStrms = setResults
                            .stream()
                            .mapToInt(rec -> rec.recTestCase().cntStrms())
                            .max()
                            .getAsInt();
        
        // Get the average data rate for each stream count
        TreeMap<Double, Integer>  mapRateToCnt = new TreeMap<>();
        
        for (int cntStrm=1; cntStrm<=intMaxStrms; cntStrm++) {
            int intTest = cntStrm;
            
            try {
                Double dblRateAvg = setResults
                        .stream()
                        .filter(rec -> rec.recTestCase().cntStrms() == intTest)
                        .mapToDouble(rec -> rec.dblDataRate())
                        .average()
                        .orElseThrow();
                
                mapRateToCnt.put(dblRateAvg, intTest);
                
            } catch (NoSuchElementException e) {
                // No entry
            }
        }
        
//        // Get the stream count for the best average rate and return it
//        Map.Entry<Double, Integer> entryBestRate = mapRateToCnt.lastEntry();
//        
//        return entryBestRate;
        return mapRateToCnt;
    }
    
    /**
     * <p>
     * Computes and returns the gRPC stream type with the best average data rate within the given record set.
     * </p>
     * <p>
     * The result records for gRPC stream type (i.e., <code>{@link DpGrpcStreamType}</code>
     * are isolated and the average data rate is computed for each.
     * The stream type with the best average data rate is identified and returned as a <code>{@link Map.Entry}</code>
     * pair where the <code>Double</code> value is the average data rate and the <code>DpGrpcStreamType</code> 
     * value is the gRPC stream type achieving that average rate (i.e., the "best" stream type). 
     * </p>
     * <p>
     * The returned map is ordered ascending according to the natural ordering provided by <code>Double</code>.
     * Thus, the best value can be obtained from <code>{@link TreeMap#lastEntry()}</code> and the worst value
     * can be obtained from <code>{@link TreeMap#firstEntry()}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The data rate average for each stream count is taken over only those available.  For example, if the result set
     * contains only one record with stream <code>{@link DpGrpcStreamType#BACKWARD}</code> then that value is considered 
     * the average.  Thus, statistics can be easily skewed for small result sets.
     * </p>
     *
     * @param setResults    collection of <code>QueryChannelTestResult</code> records under inspection
     * 
     * @return  the pairs (avg rate, stream type) found to be the "best" amount the argument collection
     * 
     * @throws NoSuchElementException   either the argument was empty or no valid stream type existed (error)
     */
    private static TreeMap<Double, DpGrpcStreamType>   computeBestStreamTypes(Collection<QueryChannelTestResult> setResults) throws NoSuchElementException {

        // Get the average data rate for each gRPC stream type
        TreeMap<Double, DpGrpcStreamType>   mapRateToType = new TreeMap<>();
        
        for (DpGrpcStreamType enmType : DpGrpcStreamType.values()) {
            try {
                Double dblRate = setResults
                        .stream()
                        .filter(rec -> rec.recTestCase().enmStrmType() == enmType)
                        .mapToDouble(rec -> rec.dblDataRate())
                        .average()
                        .orElseThrow();
                
                mapRateToType.put(dblRate, enmType);
                
            } catch (NoSuchElementException e) {
                // No Entry
            }
        }
        
//        // Get the gRPC stream type with the best average rate and return it
//        Map.Entry<Double, DpGrpcStreamType> entryBestType = mapRateToType.lastEntry();
//        
//        return entryBestType;
        return mapRateToType;
    }
    
}
