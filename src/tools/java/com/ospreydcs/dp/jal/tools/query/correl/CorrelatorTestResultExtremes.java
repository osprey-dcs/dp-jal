/*
 * Project: dp-api-common
 * File:	CorrelatorTestResultExtremes.java
 * Package: com.ospreydcs.dp.jal.tools.query.correl
 * Type: 	CorrelatorTestResultExtremes
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
 * @since Jun 5, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.correl;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ospreydcs.dp.jal.tools.query.testrequests.TestArchiveRequest;

import java.util.NoSuchElementException;
import java.util.TreeMap;

/**
 * <p>
 * Record containing the extreme values from a collection of <code>CorrelatorTestResult</code> collection.
 * </p>
 * <p>
 * The record is intended to encapsulate the extreme values (i.e., the "best" and the "worst") from a collection
 * of <code>CorrelatorTestResult</code> records.  The collection is assumed to be the result of a 
 * <code>CorrelatorTestSuiteCreator</code> evaluation and this record provides additional means for summarizing 
 * the results.  
 * </p>
 * <p>
 * <h2>Operation</h2>
 * The method <code>{@link #computeExtremes(Collection)}</code> is the preferred means for creating 
 * <code>CorrelatorTestResultExtremes</code> records.  This is the primary operation.
 * </p>
 * <p>
 * <h2>Best and Worst</h2>
 * The notions of "best" and "worst" apply to the averaged data rates for all test results having a 
 * specific parameter under test.  Thus, the criterion is actually measuring the test case under
 * evaluation.  Each parameter value is isolated, then all test results for that parameter value are
 * collected and the data rates averaged.  Then these averaged data rates are compared against all
 * parameter values and the best and worst parameter values are chosen.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * Note that <code>{@link #computeExtremes(Collection)}</code> relies on the internal methods
 * for computing tree maps of the data rate to various test result parameters.  Thus, the returned
 * maps keys are sorted by the natural ordering of the data rates.
 * Although these are used primarily for supporting the <code>{@link #computeExtremes(Collection)}</code>
 * method, they are left public in case they are of service.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Jun 5, 2025
 *
 * @param recResultBest     the best test result encountered
 * @param recResultWorst    the worst test result encountered
 * @param pairRqstTypeBest  the best test request encountered (with average data rate)
 * @param pairRqstTypeWorst the worst test request encountered (with average data rate)
 * @param pairConcOptBest   the best concurrency enabled setting (with average data rate)
 * @param pairConcOptWorst  the worst concurrency enabled setting (with average data rate)
 * @param pairThrdCntBest   the best maximum thread count encountered for concurrency (with average data rate)
 * @param pairThrdCntWorst  the worst maximum thread count encountered for concurrency (with average data rate)
 * @param pairPivotSzBest   the best concurrency triggering pivot size limit encountered (with average data rate)
 * @param pairPivotSzWorst  the worst concurrency triggering pivot size limit encountered (with average data rate)
 */
public record CorrelatorTestResultExtremes(
        CorrelatorTestResult                recResultBest,
        CorrelatorTestResult                recResultWorst,
        Entry<Double, TestArchiveRequest>   pairRqstTypeBest,
        Entry<Double, TestArchiveRequest>   pairRqstTypeWorst,
        Entry<Double, Boolean>              pairConcOptBest,
        Entry<Double, Boolean>              pairConcOptWorst,
        Entry<Double, Integer>              pairThrdCntBest,
        Entry<Double, Integer>              pairThrdCntWorst,
        Entry<Double, Integer>              pairPivotSzBest,
        Entry<Double, Integer>              pairPivotSzWorst
        ) 
{
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new <code>CorrelatorTestResultExtremes</code> record populated with the given arguments.
     * </p>
     * <p>
     * This method is for use by method <code>{@link #computeExtremes(Collection)}</code> which is the primary method
     * for computing <code>{@link CorrelatorTestResultExtremes}</code> instances.  See class documentation for further
     * details.
     * </p>
     * 
     * @param recResultBest     the best test result encountered
     * @param recResultWorst    the worst test result encountered
     * @param pairRqstTypeBest  the best test request encountered (with average data rate)
     * @param pairRqstTypeWorst the worst test request encountered (with average data rate)
     * @param pairConcOptBest   the best concurrency enabled setting (with average data rate)
     * @param pairConcOptWorst  the worst concurrency enabled setting (with average data rate)
     * @param pairThrdCntBest   the best maximum thread count encountered for concurrency (with average data rate)
     * @param pairThrdCntWorst  the worst maximum thread count encountered for concurrency (with average data rate)
     * @param pairPivotSzBest   the best concurrency triggering pivot size limit encountered (with average data rate)
     * @param pairPivotSzWorst  the worst concurrency triggering pivot size limit encountered (with average data rate)
     * 
     * @return  a new <code>CorrelatorTestResultExtremes</code> record with fields given by the argument values
     */
    public static CorrelatorTestResultExtremes from(
            CorrelatorTestResult                recResultBest,
            CorrelatorTestResult                recResultWorst,
            Entry<Double, TestArchiveRequest>   pairRqstTypeBest,
            Entry<Double, TestArchiveRequest>   pairRqstTypeWorst,
            Entry<Double, Boolean>              pairConcOptBest,
            Entry<Double, Boolean>              pairConcOptWorst,
            Entry<Double, Integer>              pairThrdCntBest,
            Entry<Double, Integer>              pairThrdCntWorst,
            Entry<Double, Integer>              pairPivotSzBest,
            Entry<Double, Integer>              pairPivotSzWorst
            ) 
    {
        return new CorrelatorTestResultExtremes(
                recResultBest,
                recResultWorst,
                pairRqstTypeBest,
                pairRqstTypeWorst,
                pairConcOptBest,
                pairConcOptWorst,
                pairThrdCntBest,
                pairThrdCntWorst,
                pairPivotSzBest,
                pairPivotSzWorst
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
     * <code>{@link CorrelatorTestResult}</code> records.  The criterion for the best and worst qualifier
     * is the average data rate for all records with a targeted field value.    
     * Once identified, these extreme field values are saved to a new <code>CorrelatorTestResultExtremes</code> 
     * record and returned.
     * </p>
     * <p>
     * <h2>Operation</h2>
     * The method relies on multiple internal (static) methods to create ordered <code>{@link TreeMap}</code> 
     * containers, keyed by the averaged data rate for a particular field and field value.  That is, the map
     * has keys of average data rate, and values ranging over the valid values of the <code>CorrelatorTestCase</code>
     * record, which is the test case within each <code>CorrelatorTestResult</code> record within the argument.  
     * There is one such method for each field within <code>{@link CorrelatorTestCase}</code>, plus an additional
     * method for computing the overall <code>CorrelatorTestResult</code> extremes.  
     * Once a map for the test case field is created, the extreme values can be obtained via the 
     * <code>{@link TreeMap#lastEntry()}</code> and <code>{@link TreeMap#firstEntry()}</code> methods for
     * obtaining the best and worst values, respectively.
     * </p>
     *  
     * @param setResults    collection of <code>CorrelatorTestResult</code> records under inspection
     * 
     * @return  new <code>CorrelatorTestResultExtremes</code> record identifying the extreme field values within the argument collection
     * 
     * @throws NoSuchElementException   either the argument was empty or contained a record with invalid field value
     */
    public static CorrelatorTestResultExtremes    computeExtremes(Collection<CorrelatorTestResult> setResults) throws NoSuchElementException {
        
        // Compute the best/worst results here
        TreeMap<Double, CorrelatorTestResult>   mapBestResults = setResults
                .stream()
                .collect(
                        TreeMap::new, 
                        (map, rec) -> map.put(rec.dblDataRate(), rec), 
                        (map1, map2) -> map1.putAll(map2)
                        );
        
        // Compute the remaining best/worst results
        TreeMap<Double, TestArchiveRequest> mapBestRequest = computeBestRequest(setResults);
        TreeMap<Double, Boolean>            mapBestConcurrency = computeBestConcurrency(setResults);
        TreeMap<Double, Integer>            mapBestThreadCount = computeBestThreadCount(setResults);
        TreeMap<Double, Integer>            mapBestPivotSize = computeBestPivotSize(setResults);
        
        // Pick of the best and worst values saving them to a new CorrelatorTestResultExtremes record
        CorrelatorTestResultExtremes    recExtremes = CorrelatorTestResultExtremes.from(
                mapBestResults.lastEntry().getValue(),  mapBestResults.firstEntry().getValue(), 
                mapBestRequest.lastEntry(),             mapBestRequest.firstEntry(), 
                mapBestConcurrency.lastEntry(),         mapBestConcurrency.firstEntry(), 
                mapBestThreadCount.lastEntry(),         mapBestThreadCount.lastEntry(), 
                mapBestPivotSize.lastEntry(),           mapBestPivotSize.firstEntry()
                );
        
        return recExtremes;
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
        ps.println(strPad + "  Best concurrency option  = " + this.pairConcOptBest.getValue() + ": avg. rate = " + this.pairConcOptBest.getKey() + " (MBps)");
        ps.println(strPad + "  Worst concurrency option = " + this.pairConcOptWorst.getValue() + ": avg. rate = " + this.pairConcOptWorst.getKey() + " (MBps)");
        ps.println(strPad + "  Best maximum thread count  = " + this.pairThrdCntBest.getValue() + ": avg. rate = " + this.pairThrdCntBest.getKey() + " (MBps)");
        ps.println(strPad + "  Worst maximum thread count = " + this.pairThrdCntWorst.getValue() + ": avg. rate = " + this.pairThrdCntWorst.getKey() + " (MBps)");
        ps.println(strPad + "  Best concurrency pivot size = " + this.pairPivotSzBest.getValue() + ": avg. rate = " + this.pairPivotSzBest.getKey() + " (MBps)");
        ps.println(strPad + "  Worst concurrency pivot size = " + this.pairPivotSzWorst.getValue() + ": avg. rate = " + this.pairPivotSzWorst.getKey() + " (MBps)");
    }
    
    
    //
    // Support Methods
    //
    
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
     * @param setResults    collection of <code>CorrelatorTestResult</code> records under inspection
     * 
     * @return  the pairs (avg rate, request type) within the argument collection, ordered worst to best 
     * 
     * @throws NoSuchElementException   either the argument was empty or no valid request type existed (error)
     */
    private static TreeMap<Double, TestArchiveRequest> computeBestRequest(Collection<CorrelatorTestResult> setResults) throws NoSuchElementException {

        // Get the test requests used - avoid iterating through all TestArchiveRequest constants
        List<TestArchiveRequest>    lstRqsts = setResults
                .stream()
                .<TestArchiveRequest>map(rec -> rec.recTestCase().enmRqst())
                .toList();

        // The (unpopulated) returned value 
        TreeMap<Double, TestArchiveRequest> mapRateToRqst = new TreeMap<>();

        // Get the average rate for each request
        for (TestArchiveRequest enmRqst : lstRqsts) {
            
            // Check if we have already processed request
            if (mapRateToRqst.containsValue(enmRqst))
                continue;
            
            // Compute average data rate for this request and add to map
            Double  dblRate = setResults
                    .stream()
                    .filter(rec -> rec.recTestCase().enmRqst() == enmRqst)
                    .mapToDouble(rec -> rec.dblDataRate())
                    .average()
                    .orElseThrow();     // No entry - should not occur (true exception)

            mapRateToRqst.put(dblRate, enmRqst);
        }

        return mapRateToRqst;
    }
    
    /**
     * <p>
     * Computes and returns the best concurrency option, that is, the option with the best averaged data rates.
     * </p>
     * <p>
     * The results are returned as a map keyed to the averaged data rates (in their natural ordering).  Both
     * option settings are contained as values (i.e., <code>false</code> and <code>true</code>).  The best
     * option is obtained with the invocation <code>{@link TreeMap#lastEntry()}</code> and the worst option
     * is obtained with invocation <code>{@link TreeMap#firstEntry()}</code>. 
     * </p>
     * 
     * @param setResults    collection of <code>CorrelatorTestResult</code> records under inspection
     * 
     * @return  the pairs (avg rate, concurrency enabled) within the argument collection, ordered worst to best 
     * 
     * @throws NoSuchElementException   either the argument was empty or not all options were considered (this exception is never thrown here)
     */
    private static TreeMap<Double, Boolean>  computeBestConcurrency(Collection<CorrelatorTestResult> setResults) throws NoSuchElementException {
        
        TreeMap<Double, Boolean>    mapRateToConc = new TreeMap<>();
        
        // Get the averaged rate for each concurrency enabled option
        for (Boolean bolConcOpt : List.of(false, true)) {
            try {
                
                // Compute averaged data rate for concurrency option and at to map
                Double dblRate = setResults
                        .stream()
                        .filter(rec -> rec.recTestCase().bolConcOpt() == bolConcOpt)
                        .mapToDouble(rec -> rec.dblDataRate())
                        .average()
                        .orElseThrow();     // No entry - concurrency option was not considered
                
                mapRateToConc.put(dblRate, bolConcOpt);
                
            } catch (NoSuchElementException e) {
                // Concurrency option not considered - ignore
            }
        }
        
        return mapRateToConc;
    }
    
    /**
     * <p>
     * Computes and returns the best thread counts when concurrency is enabled.
     * </p>
     * <p>
     * The results are returned as a <code>{@link TreeMap}</code> where keys are averaged data rates for the
     * thread count values.   Note that results are ordered worst to last (natural order of <code>Double</code>).
     * </p>
     * <p>
     * Note that only test result records where 
     * <code>{@link CorrelatorTestResult#recTestCase()}{@link CorrelatorTestCase#bolConcOpt()} == true</code>
     * are considered.  Those for concurrency disabled can have any thread count, which has not context in those
     * cases.  We ignore such cases choosing not to thrown an exception.
     * </p> 
     * 
     * @param setResults    collection of <code>CorrelatorTestResult</code> records under inspection
     * 
     * @return  the pairs (avg rate, thread count) within the argument collection, ordered worst to best 
     * 
     * @throws NoSuchElementException   either the argument was empty or not all options were considered (this exception is never thrown)
     */
    private static TreeMap<Double, Integer> computeBestThreadCount(Collection<CorrelatorTestResult> setResults) throws NoSuchElementException {
        
        // The thread counts considered within the test results collection
        List<Integer>   lstThrdCnts = setResults
                .stream()
                .<Integer>map(rec -> rec.recTestCase().cntMaxThrds())
                .toList();
        
        TreeMap<Double, Integer>    mapRateToMaxThrds = new TreeMap<>();
        
        // Get the averaged data rate for each thread count
        for (Integer cntThrds : lstThrdCnts) {
            try {
                
                // Skip if already considered this thread count
                if (mapRateToMaxThrds.containsValue(cntThrds))
                    continue;

                // Compute the averaged data rate for this thread count when concurrency is enabled
                Double  dblRate = setResults
                        .stream()
                        .filter(rec -> rec.recTestCase().bolConcOpt() == true)  // only consider cases with concurrency enabled
                        .filter(rec -> rec.recTestCase().cntMaxThrds() == cntThrds)
                        .mapToDouble(rec -> rec.dblDataRate())
                        .average()
                        .orElseThrow();

                // Add averaged rate and thread count to map
                mapRateToMaxThrds.put(dblRate, cntThrds);

            } catch (NoSuchElementException e) {
                // No results for this thread count with concurrency enabled - ignore 
            }
        }
        
        return mapRateToMaxThrds;
    }
    
    /**
     * <p>
     * Computes and returns the best concurrency pivot size when concurrency is enabled.
     * </p>
     * <p>
     * The results are returned as a <code>{@link TreeMap}</code> where keys are averaged data rates for the
     * pivot size values.   Note that results are ordered worst to last (natural order of <code>Double</code>).
     * </p>
     * <p>
     * Note that only test result records where 
     * <code>{@link CorrelatorTestResult#recTestCase()}{@link CorrelatorTestCase#bolConcOpt()} == true</code>
     * are considered.  Those for concurrency disabled can have any pivot size, which has not context in those
     * cases.  We ignore such cases choosing not to thrown an exception.
     * </p> 
     * 
     * @param setResults    collection of <code>CorrelatorTestResult</code> records under inspection
     * 
     * @return  the pairs (avg rate, pivot size) within the argument collection, ordered worst to best 
     * 
     * @throws NoSuchElementException   either the argument was empty or not all options were considered (never thrown)
     */
    private static TreeMap<Double, Integer> computeBestPivotSize(Collection<CorrelatorTestResult> setResults) throws NoSuchElementException {
        
        // The pivot sizes considered within the test results collection
        List<Integer>   lstPivotSzs = setResults
                .stream()
                .<Integer>map(rec -> rec.recTestCase().szConcPivot())
                .toList();
        
        TreeMap<Double, Integer>    mapRateToPivotSz = new TreeMap<>();
        
        // Get the averaged data rate for each pivot size
        for (Integer szPivot : lstPivotSzs) {
            try {

                // Skip if pivot size was already considered
                if (mapRateToPivotSz.containsValue(szPivot))
                    continue;

                // Compute the averaged data rate for this pivot size if concurrency is enabled
                Double  dblRate = setResults
                        .stream()
                        .filter(rec -> rec.recTestCase().bolConcOpt() == true)  // only consider results with concurrency
                        .filter(rec -> rec.recTestCase().szConcPivot() == szPivot)
                        .mapToDouble(rec -> rec.dblDataRate())
                        .average()
                        .orElseThrow();     // throws exception
                
                // Add averaged rate and pivot size to map
                mapRateToPivotSz.put(dblRate, szPivot);

            } catch (NoSuchElementException e) {
                // No results for this pivot size with concurrency enabled - ignore
            }
        }
        
        return mapRateToPivotSz;
    }

}
