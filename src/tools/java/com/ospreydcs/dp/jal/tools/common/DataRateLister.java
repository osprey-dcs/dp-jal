/*
 * Project: dp-api-common
 * File:	DataRateLister.java
 * Package: com.ospreydcs.dp.jal.tools.common
 * Type: 	DataRateLister
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
 * @since Aug 21, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common;

import java.io.PrintStream;
import java.util.Collection;

/**
 * <p>
 * Class for printing out the data rates for a collection of test result records of type <code>Result</code>.
 * <p>
 * <p>
 * Class instances must be supplied a set of interface objects upon creation which extract
 * the following properties from the <code>Result</code> record:
 * <ul>
 * <li><code>{@link Index}</code> - extracts test case index from <code>Result</code> records. </li>
 * <li><code>{@link RequestId}</code> - extracts time-series data request ID from <code>Result</code> record. </li>
 * <li><code>{@link Allocation}</code> - extracts memory allocation size (bytes) from <code>Result</code> records. </li>
 * <li><code>{@link DataRate}</code> - extracts data rate (MBps) from <code>Result</code> records. </li>
 * </ul>
 * These interfaces are defined as internally within the class and are typically supplied to 
 * <code>DataRateLister</code> object creators and constructors as lambda functions.
 * </p>  
 * <p>
 * <h2>Usage</h2>
 * Once created, a use the method <code>{@link #printOut(PrintStream, String, Collection)}</code> to print out
 * listings of the data rates for a collection of <code>Result</code> records.  Each line will contain the 
 * data rate for the test result, along with the test case index and the memory allocation for the test.
 * The string formatter for the lines in the data rate listing is given by class constant
 * <code>{@link #STR_LINE_FMTR}</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 21, 2025
 *
 * @param <Result>  test result record containing data rate, test case index, and memory allocation
 */
public class DataRateLister<Result extends Record> {

    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new <code>DataRateLister</code> ready for data rate listing.
     * </p>
     * <p>
     * Use the <code>{@link #printOut(PrintStream, String, Collection)}</code> method of the returned
     * <code>DataRateLister</code> object to print out listings of the data rates for collections
     * of <code>Result</code> test result records.
     * </p>
     * 
     * @param <Result>  test result record containing data rate, test case index, and memory allocation
     * 
     * @param fncIndex  lambda function extracting test case index from <code>Result</code> records
     * @param fncRqstId lambda function extracting test case request ID from <code>Result</code> records
     * @param fncAlloc  lambda function extracting memory allocation (bytes) from <code>Result</code> records
     * @param fncRate   lambda function extracting data rate (MBps) from <code>Result</code> records
     * 
     * @return  a new <code>DataRateLister</code> object ready for data listing of <code>Result</code> records.
     */
    public static <Result extends Record> DataRateLister<Result>    from(Index<Result> fncIndex, RequestId<Result> fncRqstId, Allocation<Result> fncAlloc, DataRate<Result> fncRate) {
        return new DataRateLister<Result>(fncIndex, fncRqstId, fncAlloc, fncRate);
    }
    
    //
    // Internal Types
    //
    
    /** Interface definition for lambda function that extracts test case indexes from <code>Result</code> records */   
    public static interface Index<Result extends Record>        { public int  extract(Result recResult);  };
    
    /** Interface definition for lambda function that extracts test case request ID from <code>Result</code> records */
    public static interface RequestId<Result extends Record>    { public String extract(Result recResult); };
    
    /** Interface definition for lambda function that extracts memory allocation (bytes) from <code>Result</code> records */   
    public static interface Allocation<Result extends Record>   { public long extract(Result recResult); };
    
    /** Interface definition for lambda function that extracts data rates (MBps) from <code>Result</code> records */   
    public static interface DataRate<Result extends Record>     { public double extract(Result recResult); };
    
    
    //
    // Class Constants
    //
    
    /** The format string used for creating output lines within the data rate listing */ 
    public static final String      STR_LINE_FMTR = "  %7.3f MBps for Case #%d with %7.3f MBytes allocation from request %s";
    
    
    //
    // Defining Attributes
    //
    
    /** Function that extracts the test case index from the result record */
    private final Index<Result>         fncTestIndex;
    
    /** Function that extracts the test case time-series data request ID */
    private final RequestId<Result>     fncRequestId;
    
    /** Function that extracts the memory allocation size (bytes) from the result record */
    private final Allocation<Result>    fncAllocSize;
        
    /** Function that extracts the data rate (Bps) from the result record */
    private final DataRate<Result>      fncDataRate;

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>DataRateLister</code> instance with the given extract functions.
     * </p>
     * <p>
     * Use the <code>{@link #printOut(PrintStream, String, Collection)}</code> method of the new
     * <code>DataRateLister</code> object to print out listings of the data rates for collections
     * of <code>Result</code> test result records.
     * </p>
     *
     * @param fncIndex  lambda function extracting test case index from <code>Result</code> records
     * @param fncRqstId lambda function extracting test case request ID from <code>Result</code> records
     * @param fncAlloc  lambda function extracting memory allocation (bytes) from <code>Result</code> records
     * @param fncRate   lambda function extracting data rate (MBps) from <code>Result</code> records
     */
    public DataRateLister(Index<Result> fncTestIndex, RequestId<Result> fncRqstId, Allocation<Result> fncAlloc, DataRate<Result> fncRate) {
        this.fncTestIndex = fncTestIndex;
        this.fncRequestId = fncRqstId;
        this.fncAllocSize = fncAlloc;
        this.fncDataRate = fncRate;
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Prints out a text listing of the record data rates to the given output stream.
     * </p>
     * <p>
     * A line-by-line text description of each record data rate and associated test index and
     * memory allocation is written to the given output.
     * The <code>strPad</code> is used to supply an optional whitespace character padding to the
     * left-hand side header for each line description.
     * </p>
     *   
     * @param ps        output stream to receive text description of record fields
     * @param strPad    white-space padding for each line header (or <code>null</code>)
     * @param setResults collection of test result records whose data rates are to be listed
     */
    public void printOut(PrintStream ps, String strPad, Collection<Result> setResults) {
        final String    STR_PAD = (strPad == null) ? "" : "  ";

        for (Result recResult : setResults) {
            String  strLine = this.createLine(recResult);
            
            ps.println(STR_PAD + strLine);
        }
    }
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates the data rate listing line for the given record.
     * </p>
     * <p>
     * Extracts the line data from the argument record using the extraction functions supplied
     * at construction.  These data are then used, along with the string formatter <code>{@link #STR_LINE_FMTR}</code>
     * to create the final text line listing.
     * </p>
     * 
     * @param recResult result record whose data is to be extracted and formatted
     * 
     * @return  a string representation of the data within the given result record
     */
    private String  createLine(Result recResult) {
        
        int     intIndex = this.fncTestIndex.extract(recResult);
        String  strRqstId = this.fncRequestId.extract(recResult);
        double  dblAlloc = ((double)this.fncAllocSize.extract(recResult))/1.0e6;
        double  dblRate = this.fncDataRate.extract(recResult);
        
        String  strLine = String.format(STR_LINE_FMTR, dblRate, intIndex, dblAlloc, strRqstId);
        
        return strLine;
    }
}
