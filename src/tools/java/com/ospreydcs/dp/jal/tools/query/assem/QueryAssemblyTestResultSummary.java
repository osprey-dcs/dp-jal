/*
 * Project: dp-api-common
 * File:	QueryAssemblyTestResultSummary.java
 * Package: com.ospreydcs.dp.jal.tools.query.assem
 * Type: 	QueryAssemblyTestResultSummary
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
import java.util.Collection;

/**
 * <p>
 * Record containing summary results for a collection of <code>QueryAssemblyTestResult</code> records.
 * </p>
 * <p>
 * A <code>QueryAssemblyTestResult</code> record contains the results of a query assembly processors test
 * case encapsulated by a <code>QueryAssemblyTestCase</code> record.  There are 2 parts to a query assembly
 * test case:
 * <ol>
 * <li>Raw time-series data recovery and correlation into raw data blocks</li>
 * <li>Assembly of raw correlated data into an ordered aggregate of sampled blocks, each representing a "page" of request data.</li>
 * </ol>
 * The <code>QueryAssemblyTestResult</code> record contains 2 sub-records, one for each result of the
 * above operations.  In turn, the <code>QueryAssemblyTestResultSummary</code> record also contains two sub-records.
 * The summary records primarily contain statistical information about the result record fields.
 * </p>  
 * <p>
 * <h2>Summary Creation</h2>
 * Record instances are intended to be created from the <code>{@link #summarize(Collection)}</code> static method where
 * the argument is a collection of <code>QueryAssemblyTestResult</code> records.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 14, 2025
 *
 * @param cntResults            total number of <code>QueryAssemblyTestResult</code> records considered
 * @param recRecoverySummary    record containing a summary of the raw time-series data recovery and correlation 
 * @param recAssemblySummary    record containing a summary of the ordered sampled aggregate assembly
 */
public record QueryAssemblyTestResultSummary(
        int                             cntResults,
        QueryRecoveryResultSummary      recRecoverySummary,
        SampledAggregateResultSummary   recAssemblySummary
        ) 
{
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new <code>QueryAssemblyTestResultSummary</code> record from the given argument values.
     * </p>
     * 
     * @param cntResults            total number of <code>QueryAssemblyTestResult</code> records considered
     * @param recRecoverySummary    record containing a summary of the raw time-series data recovery and correlation 
     * @param recAssemblySummary    record containing a summary of the ordered sampled aggregate assembly
     * 
     * @return  a new <code>QueryAssemblyTestResultSummary</code> record populated from the given arguments
     */
    public static QueryAssemblyTestResultSummary    from(
            int                             cntResults,
            QueryRecoveryResultSummary      recRecoverySummary, 
            SampledAggregateResultSummary   recAssemblySummary) 
    {
        return new QueryAssemblyTestResultSummary(cntResults, recRecoverySummary, recAssemblySummary);
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Computes a summary of the results collection within the argument.
     * </p>
     * <p>
     * The summary parameters are mostly statistical properties of the record fields (e.g., minimum, maximum, average).
     * Use the <code>{@link #printOut(PrintStream, String)}</code> method to print out a text description of the
     * record.
     * </p>
     * 
     * @param setResults    the <code>QueryAssemblyRestResult</code> collection to summarize
     * 
     * @return  a summary of the argument collection
     */
    public static QueryAssemblyTestResultSummary    summarize(Collection<QueryAssemblyTestResult> setResults) {
        
        int                                 cntResults = setResults.size();
        Collection<QueryRecoveryResult>     setRecResults = setResults.stream().<QueryRecoveryResult>map(rec -> rec.recRecoveryResult()).toList();
        Collection<SampledAggregateResult>  setAggResults = setResults.stream().<SampledAggregateResult>map(rec -> rec.recSmpAggResult()).toList();
        
        QueryRecoveryResultSummary      recRecSum = QueryRecoveryResultSummary.summarize(setRecResults);
        SampledAggregateResultSummary   recAssSum = SampledAggregateResultSummary.summarize(setAggResults);
        
        return QueryAssemblyTestResultSummary.from(cntResults, recRecSum, recAssSum);
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
        ps.println(strPad + "Query Assembly Results Summary");
        ps.println(strPad + "  Total number of result cases      : " + this.cntResults);
        strPad = strPad + "  ";
        this.recRecoverySummary.printOut(ps, strPad);
        this.recAssemblySummary.printOut(ps, strPad);
    }

}
