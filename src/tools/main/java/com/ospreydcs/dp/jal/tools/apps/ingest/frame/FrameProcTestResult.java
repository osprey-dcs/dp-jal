/*
 * Project: dp-jal
 * File:	FrameProcTestResult.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.frame
 * Type: 	FrameProcTestResult
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
 * @since Sep 15, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.frame;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Comparator;

import com.ospreydcs.dp.jal.common.ResultStatus;
import com.ospreydcs.dp.jal.ingest.model.frame.IngestionFrameProcessor;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Record containing the results of a <code>IngestionFrameProcessor</code> performance evaluation from <code>FrameProcTestCase</code>.
 * </p>
 * <p>
 * The record contains the results of a <code>IngestionFrameProcessor</code> evaluation as described by the field
 * <code>{@link #recTestCase}</code>.  The performance evaluation is typically performed by method
 * <code>{@link FrameProcTestCase#evaluate(IngestionFrameProcessor)}</code>. 
 * </p>
 *
 * @param recTestStatus status of the test case evaluation
 * @param cntFrames     number of ingestion frames in the payload used for the evaluation
 * @param szPayload     allocation size (in bytes) of the payload used for the evaluation
 * @param cntMsgs       number of <code>IngestDataRequest</code> messaged produced during the evaluation
 * @param szProcessed   allocation size (in bytes) of all produced messages during evaluation
 * @param durProcessed  processing time for the test case ingestion frame payload
 * @param dblRateRaw    processing rate (in MBps) w,r,t, ingestion frame payload
 * @param dblRateProc    processing rate (in MBps) w,r,t, produced <code>IngestDataRequest</code> messages
 * @param recTestCase   the test case defining the evaluation parameters
 *  
 * @author Christopher K. Allen
 * @since Sep 15, 2025
 *
 * @see FrameProcTestCase#evaluate(IngestionFrameProcessor)
 */
public record FrameProcTestResult(
        ResultStatus        recTestStatus,
        
        int                 cntFrames,
        long                szPayload,
        
        int                 cntMsgs,
        long                szProcessed,
        
        Duration            durProcessed,
        double              dblRateRaw,
        double              dblRateProc,
        
        FrameProcTestCase  recTestCase
        ) implements Comparable<FrameProcTestResult>
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Returns a new <code>FrameProcTestResult</code> record populated with the given arguments.
     * </p>
     * <p>
     * This creator is equivalent to the canonical constructor
     * <code>{@link #FrameProcTestResult(ResultStatus, int, long, int, long, Duration, double, double, FrameProcTestCase)}</code>.
     * The returned record is assumed to be created by the 
     * <code>{@link FrameProcTestCase#evaluate(QueryChannel, QueryMessageBuffer)</code> method.
     * </p>
     * 
     * @param recTestStatus status of the test case evaluation
     * @param cntFrames     number of ingestion frames in the payload used for the evaluation
     * @param szPayload     allocation size (in bytes) of the payload used for the evaluation
     * @param cntMsgs       number of <code>IngestDataRequest</code> messaged produced during the evaluation
     * @param szProcessed   allocation size (in bytes) of all produced messages during evaluation
     * @param durProcessed  processing time for the test case ingestion frame payload
     * @param dblRateRaw    processing rate (in MBps) for ingestion frame payload
     * @param dblRateProc    processing rate (in MBps) for produced <code>IngestDataRequest</code> messages
     * @param recTestCase   the test case defining the evaluation parameters
     *  
     * @return  a new <code>FrameProcTestResult</code> record with fields given by the above arguments
     */
    public static FrameProcTestResult   from( 
            ResultStatus        recTestStatus,
            
            int                 cntFrames,
            long                szPayload,
            
            int                 cntMsgs,
            long                szProcessed,
            
            Duration            durProcessed,
            double              dblRateRaw,
            double              dblRateProc,
            
            FrameProcTestCase  recTestCase
            )
    {
        return new FrameProcTestResult(recTestStatus, cntFrames, szPayload, cntMsgs, szProcessed, durProcessed, dblRateRaw, dblRateProc, recTestCase);
    }
    
    /**
     * <p>
     * Creates a new instance of <code>QueryChannelTestResult</code> for the case of a test evaluation failure.
     * </p>
     * <p>
     * This creator is intended for use whenever a 
     * <code>{@link FrameProcTestCase#evaluate(IngestionFrameProcessor)}</code> 
     * operation fails; that is, an exception is thrown during evaluation.  
     * The cause of the failure (and a message) should
     * be included in the <code>recTestStatus</code> argument.
     * </p>
     * 
     * @param recTestStatus the cause of the failure
     * @param recTestCase   the test case that failed
     * 
     * @return  a new <code>FrameProcTestResult</code> instance containing test evaluation failure information
     * 
     * @throws IllegalArgumentException     the status argument indicates <code>{@link ResultStatus#SUCCESS}</code>
     */
    public static FrameProcTestResult   from(ResultStatus recTestStatus, FrameProcTestCase recTestCase) throws IllegalArgumentException {
        
        // Check status argument
        if (recTestStatus.isSuccess()) 
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - The status argument indicates sucess.");
        
        // Create and return a record containing only the status and test case (status contains failure message and cause)
        return new FrameProcTestResult(recTestStatus, 0, 0L, 0, 0L, Duration.ZERO, 0.0, 0.0, recTestCase);
    }

    
    //
    // Tools
    //
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> providing a natural (ascending) ordering according to the test case index.
     * </p>
     * <p>
     * The returned comparator instance compares the index field within the <code>{@link #recTestCase()}</code> fields of two
     * <code>FrameProcTestResult</code> records, specifically the <code>{@link FrameProcTestCase#indCase()}</code> fields.
     * The natural ordering of the index values is applied; specifically, the lowest index will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned comparator never returns 0.  In the case of equal indexes the record on the left-hand side will be ordered
     * first.  This is to prevent clobbering of differing test results from the same test case.
     * </p>
     * 
     * @return  a new <code>Comparator</code> instance providing a natural ordering by test case index
     */
    public static Comparator<FrameProcTestResult>   caseIndexOrdering() {
        
        Comparator<FrameProcTestResult> cmp = (r1, r2) -> {
            if (r1.recTestCase.indCase() <= r2.recTestCase.indCase())
                return -1;
            else
                return +1;
        };
        
        return cmp;
    }
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> provide a descending (reverse) ordering according to raw data rates.
     * </p>
     * <p>
     * The returned comparator instance compares the <code>{@link #dblRateRaw()}</code> fields of two 
     * <code>FrameProcTestResult</code> records.  It provides a reverse ordering of records according
     * to the data rate fields.  Specifically, the highest data rate will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * Note that the comparator provided here is the complement of the natural order of 
     * <code>FrameProcTestResult</code> records provided by the exposed <code>Comparable</code> interface.
     * </p>  
     * 
     * @return  a new <code>Comparator</code> instance providing a reverse ordering by record raw data rates
     */
    public static Comparator<FrameProcTestResult>   descendingRawRateOrdering() {
    
        Comparator<FrameProcTestResult>   cmp = (r1, r2) -> {

            if (r1.dblRateRaw > r2.dblRateRaw)
                return -1;
            else
                return +1;
        };
        
        return cmp;
    }
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> provide an ascending (natural) ordering according to raw data rates.
     * </p>
     * <p>
     * The returned comparator instance compares the <code>{@link #dblRateRaw()}</code> fields of two 
     * <code>FrameProcTestResult</code> records.  It provides a natural ordering of records according
     * to the data rate fields.  Specifically, the lowest data rate will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * Note that the comparator provided here is the equivalent of the natural order of 
     * <code>FrameProcTestResult</code> records provided by the exposed <code>Comparable</code> interface.
     * </p>  
     * 
     * @return  a new <code>Comparator</code> instance providing a natural ordering by record raw data rates
     */
    public static Comparator<FrameProcTestResult>    ascendingRawRateOrdering() {

        Comparator<FrameProcTestResult>  cmp = (r1, r2) -> {

            if (r1.dblRateRaw < r2.dblRateRaw)
                return -1;
            else
                return +1;
        };
        
        return cmp;  
    }

    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> provide a descending (reverse) ordering according to processed data rates.
     * </p>
     * <p>
     * The returned comparator instance compares the <code>{@link #dblRateProc()}</code> fields of two 
     * <code>FrameProcTestResult</code> records.  It provides a reverse ordering of records according
     * to the data rate fields.  Specifically, the highest data rate will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * Note that the comparator provided here is the complement of the natural order of 
     * <code>FrameProcTestResult</code> records provided by the exposed <code>Comparable</code> interface.
     * </p>  
     * 
     * @return  a new <code>Comparator</code> instance providing a reverse ordering by record raw data rates
     */
    public static Comparator<FrameProcTestResult>   descendingProcessedRateOrdering() {
    
        Comparator<FrameProcTestResult>   cmp = (r1, r2) -> {

            if (r1.dblRateProc > r2.dblRateProc)
                return -1;
            else
                return +1;
        };
        
        return cmp;
    }
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> provide an ascending (natural) ordering according to processed data rates.
     * </p>
     * <p>
     * The returned comparator instance compares the <code>{@link #dblRateProc()}</code> fields of two 
     * <code>FrameProcTestResult</code> records.  It provides a natural ordering of records according
     * to the data rate fields.  Specifically, the lowest data rate will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * Note that the comparator provided here is the equivalent of the natural order of 
     * <code>FrameProcTestResult</code> records provided by the exposed <code>Comparable</code> interface.
     * </p>  
     * 
     * @return  a new <code>Comparator</code> instance providing a natural ordering by record raw data rates
     */
    public static Comparator<FrameProcTestResult>    ascendingProcessedRateOrdering() {

        Comparator<FrameProcTestResult>  cmp = (r1, r2) -> {

            if (r1.dblRateProc < r2.dblRateProc)
                return -1;
            else
                return +1;
        };
        
        return cmp;  
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Prints out a text description of the record fields to the given output stream.
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
        String  strPadd = strPad + "  ";
        
        // Check for test case failure
        if (this.recTestStatus.isFailure()) {
            ps.println(strPad + "Test Case #" + this.recTestCase.indCase());
            ps.println(strPad + "  FAILURE - " + this.recTestStatus.message());
            if (this.recTestStatus.hasCause()) {
                Throwable   e = this.recTestStatus.cause();
                ps.println(strPad + "  Cause: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
            ps.println(strPadd + "Test Case Parameters");
            this.recTestCase.printOut(ps, strPadd);
            
            return;
        }
        
        ps.println(strPad + "Test Case #" + this.recTestCase.indCase());
        ps.println(strPadd + "Data rate - raw (MBps)            : " + this.dblRateRaw);
        ps.println(strPadd + "Data rate - processed (MBps)      : " + this.dblRateProc);
        ps.println(strPadd + "Payload ingestion frame count     : " + this.cntFrames);
        ps.println(strPadd + "Payload allocation size (bytes)   : " + this.szPayload);
        ps.println(strPadd + "Processed message count           : " + this.cntMsgs);
        ps.println(strPadd + "Processed allocation size (bytes) : " + this.szProcessed);
        ps.println(strPadd + "Processing duration               : " + this.durProcessed);
        ps.println(strPadd + "Test Case Parameters");
        this.recTestCase.printOut(ps, strPadd);
    }

    
    //
    // Comparable<FrameProcTestResult> Interface
    //
    
    /**
     * <p>
     * Provides a forward order of <code>FrameProcTestResult</code> records by raw data rate.
     * </p>
     * <p>
     * The <code>{@link #dblRateRaw()}</code> field of the argument is compared against that of
     * this record.  If the data rate of this field is less than that of the argument field
     * a value -1 is returned. Otherwise a value +1 is returned.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The value 0 is never returned to avoid clobbering of records within an ordered Java
     * collection.
     * </p>
     * 
     * @param o     record under comparison
     * 
     * @return  -1 if the raw data rate of this record is less than that of the argument,
     *          +1 otherwise
     *          
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(FrameProcTestResult o) {
        
        if (this.dblRateRaw < o.dblRateRaw)
            return -1;
        else
            return +1;
    }

}
