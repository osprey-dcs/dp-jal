/*
 * Project: dp-jal
 * File:	IngestChanTestResult.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.channel
 * Type: 	IngestChanTestResult
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
 * @since Feb 18, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.channel;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataResponse;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataStreamResponse;
import com.ospreydcs.dp.jal.common.ResultStatus;
import com.ospreydcs.dp.jal.ingest.model.frame.IngestionFrameProcessor;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Record containing the results from an <code>IngestChanTestCase</code> evaluation.
 * </p>
 * <p>
 * Instances of this record are created from the 
 * <code>{@link IngestChanTestCase#evaluate(com.ospreydcs.dp.jal.ingest.model.grpc.IngestionMessageBuffer, com.ospreydcs.dp.jal.ingest.model.grpc.IngestionChannel)}</code>
 * operation.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 18, 2026
 *
 * @param recTestStatus status of the test case evaluation
 * @param cntFrames     number of ingestion frames in the payload used for the evaluation
 * @param szPayload     allocation size (in bytes) of the payload used for the evaluation
 * @param durProcessed  processing time for data message conversion of test case ingestion frame payload
 * @param dblRateProc   processing rate (in MBps) for data frames into data messages ({@link #szAllocXmit()}/{@link #durProcessed()})
 * @param durTransmit   transmission time for all data messages produced
 * @param cntMsgsXmit   number of <code>IngestDataRequest</code> messaged processed and transmitted  
 * @param szAllocXmit   allocation size (in bytes) of all data messages processed and transmitted 
 * @param dblRateXmit   transmission rate (in MBps) for ingestion data messages
 * @param recTestCase   the test case defining the evaluation parameters
 */
public record IngestChanTestResult(
        ResultStatus        recTestStatus,
        
        int                 cntFrames,
        long                szPayload,
        
        Duration            durProcessed,
        double              dblRateProc,
        
        Duration            durTransmit,
        int                 cntMsgsXmit,
        long                szAllocXmit,
        double              dblRateXmit,
        
        List<IngestDataStreamResponse>  lstUniRsps,
        List<IngestDataResponse>        lstBidiRsps,
        
        IngestChanTestCase  recTestCase
        ) implements Comparable<IngestChanTestResult> 
{

    //
    // Creators
    //
    
    /**
     * <p>
     * Returns a new <code>IngestChanTestResult</code> record populated with the given arguments.
     * </p>
     * <p>
     * This creator is equivalent to the canonical constructor
     * <code>{@link #IngestChanTestResult(ResultStatus, int, long, int, long, Duration, double, double, IngestChanTestCase)}</code>.
     * The returned record is assumed to be created by the 
     * <code>{@link IngestChanTestCase#evaluate(QueryChannel, QueryMessageBuffer)}</code> method.
     * </p>
     * 
     * @param recTestStatus status of the test case evaluation
     * @param cntFrames     number of ingestion frames in the payload used for the evaluation
     * @param szPayload     allocation size (in bytes) of the payload used for the evaluation
     * @param durProcessed  processing time for data message conversion of test case ingestion frame payload
     * @param dblRateProc   processing rate (in MBps) for data frames into data messages ({@link #szAllocXmit()}/{@link #durProcessed()})
     * @param durTransmit   transmission time for all data messages produced
     * @param cntMsgsXmit   number of <code>IngestDataRequest</code> messaged processed and transmitted  
     * @param szAllocXmit   allocation size (in bytes) of all data messages processed and transmitted 
     * @param dblRateXmit   transmission rate (in MBps) for ingestion data messages
     * @param recTestCase   the test case defining the evaluation parameters
     *  
     * @return  a new <code>IngestChanTestResult</code> record with fields given by the above arguments
     */
    public static IngestChanTestResult   from( 
            ResultStatus        recTestStatus,
            
            int                 cntFrames,
            long                szPayload,
            
            Duration            durProcessed,
            double              dblRateProc,
            
            Duration            durTransmit,
            int                 cntMsgsXmit,
            long                szAllocXmit,
            double              dblRateXmit,
            
            List<IngestDataStreamResponse>  lstUniRsps,
            List<IngestDataResponse>        lstBidiRsps,
            
            IngestChanTestCase  recTestCase
            )
    {
        return new IngestChanTestResult(
                recTestStatus,
                
                cntFrames,
                szPayload,
                
                durProcessed,
                dblRateProc,
                
                durTransmit,
                cntMsgsXmit,
                szAllocXmit,
                dblRateXmit,
                
                lstUniRsps,
                lstBidiRsps,
                
                recTestCase
                );
    }
    
    /**
     * <p>
     * Creates a new instance of <code>IngestChanTestResult</code> for the case of a test evaluation failure.
     * </p>
     * <p>
     * This creator is intended for use whenever a 
     * <code>{@link IngestChanTestCase#evaluate(IngestionFrameProcessor)}</code> 
     * operation fails; that is, an exception is thrown during evaluation.  
     * The cause of the failure (and a message) should
     * be included in the <code>recTestStatus</code> argument.
     * </p>
     * 
     * @param recTestStatus the cause of the failure
     * @param recTestCase   the test case that failed
     * 
     * @return  a new <code>IngestChanTestResult</code> instance containing test evaluation failure information
     * 
     * @throws IllegalArgumentException     the status argument indicates <code>{@link ResultStatus#SUCCESS}</code>
     */
    public static IngestChanTestResult   from(ResultStatus recTestStatus, IngestChanTestCase recTestCase) throws IllegalArgumentException {
        
        // Check status argument
        if (recTestStatus.isSuccess()) 
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - The status argument indicates sucess.");
        
        // Create and return a record containing only the status and test case (status contains failure message and cause)
        return IngestChanTestResult.from(recTestStatus, 0, 0L, Duration.ZERO, 0.0, Duration.ZERO, 0, 0L, 0.0, List.of(), List.of(), recTestCase);
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
     * <code>IngestChanTestResult</code> records, specifically the <code>{@link IngestChanTestCase#indCase()}</code> fields.
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
    public static Comparator<IngestChanTestResult>   caseIndexOrdering() {
        
        Comparator<IngestChanTestResult> cmp = (r1, r2) -> {
            if (r1.recTestCase.indCase() <= r2.recTestCase.indCase())
                return -1;
            else
                return +1;
        };
        
        return cmp;
    }
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> provide a descending (reverse) ordering according to data transmission rates.
     * </p>
     * <p>
     * The returned comparator instance compares the <code>{@link #dblRateXmit()}</code> fields of two 
     * <code>IngestChanTestResult</code> records.  It provides a reverse ordering of records according
     * to the data rate fields.  Specifically, the highest data rate will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * Note that the comparator provided here is the complement of the natural order of 
     * <code>IngestChanTestResult</code> records provided by the exposed <code>Comparable</code> interface.
     * </p>  
     * 
     * @return  a new <code>Comparator</code> instance providing a reverse ordering by record raw data rates
     */
    public static Comparator<IngestChanTestResult>   descendingTransmissionRateOrdering() {
    
        Comparator<IngestChanTestResult>   cmp = (r1, r2) -> {

            if (r1.dblRateXmit > r2.dblRateXmit)
                return -1;
            else
                return +1;
        };
        
        return cmp;
    }
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> provide an ascending (natural) ordering according to data transmission rates.
     * </p>
     * <p>
     * The returned comparator instance compares the <code>{@link #dblRateXmit()}</code> fields of two 
     * <code>IngestChanTestResult</code> records.  It provides a natural ordering of records according
     * to the data rate fields.  Specifically, the lowest data rate will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * Note that the comparator provided here is the equivalent of the natural order of 
     * <code>IngestChanTestResult</code> records provided by the exposed <code>Comparable</code> interface.
     * </p>  
     * 
     * @return  a new <code>Comparator</code> instance providing a natural ordering by record raw data rates
     */
    public static Comparator<IngestChanTestResult>    ascendingTransmissionRateOrdering() {

        Comparator<IngestChanTestResult>  cmp = (r1, r2) -> {

            if (r1.dblRateXmit < r2.dblRateXmit)
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
     * <code>IngestChanTestResult</code> records.  It provides a reverse ordering of records according
     * to the data rate fields.  Specifically, the highest data rate will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * Note that the comparator provided here is the complement of the natural order of 
     * <code>IngestChanTestResult</code> records provided by the exposed <code>Comparable</code> interface.
     * </p>  
     * 
     * @return  a new <code>Comparator</code> instance providing a reverse ordering by record raw data rates
     */
    public static Comparator<IngestChanTestResult>   descendingProcessedRateOrdering() {
    
        Comparator<IngestChanTestResult>   cmp = (r1, r2) -> {

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
     * <code>IngestChanTestResult</code> records.  It provides a natural ordering of records according
     * to the data rate fields.  Specifically, the lowest data rate will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * Note that the comparator provided here is the equivalent of the natural order of 
     * <code>IngestChanTestResult</code> records provided by the exposed <code>Comparable</code> interface.
     * </p>  
     * 
     * @return  a new <code>Comparator</code> instance providing a natural ordering by record raw data rates
     */
    public static Comparator<IngestChanTestResult>    ascendingProcessedRateOrdering() {

        Comparator<IngestChanTestResult>  cmp = (r1, r2) -> {

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
        ps.println(strPadd + "Payload ingestion frame count     : " + this.cntFrames);
        ps.println(strPadd + "Payload allocation size (bytes)   : " + this.szPayload);
        ps.println(strPadd + "Processing duration               : " + this.durProcessed);
        ps.println(strPadd + "Payload processing rate (MBps)    : " + this.dblRateProc);
        ps.println(strPadd + "Processed message count           : " + this.cntMsgsXmit);
        ps.println(strPadd + "Processed allocation size (bytes) : " + this.szAllocXmit);
        ps.println(strPadd + "Transmission duration             : " + this.durTransmit);
        ps.println(strPadd + "Tranmission rate  (MBps)          : " + this.dblRateXmit);
        if (this.lstUniRsps!=null && !this.lstUniRsps.isEmpty())
            ps.println(strPadd + "Unidirectional stream responses    : " + this.lstUniRsps.size());
        if (this.lstBidiRsps!=null && !this.lstBidiRsps.isEmpty())
            ps.println(strPadd + "Bidirectional stream responses     : " + this.lstUniRsps.size());
        ps.println(strPadd + "Test Case Parameters");
        this.recTestCase.printOut(ps, strPadd);
    }

    
    //
    // Comparable<IngestChanTestResult>
    //
    
    /**
     * <p>
     * Provides a forward order of <code>IngestChanTestResult</code> records by raw data rate.
     * </p>
     * <p>
     * The <code>{@link #dblRateXmit()}</code> field of the argument is compared against that of
     * this record.  If the data rate of this field is less than that of the argument field
     * a value -1 is returned. Otherwise a value +1 is returned.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The value 0 is never returned to avoid clobbering of records within an ordered Java
     * collection.
     * </p>
     * 
     * @param recResult     record under comparison
     * 
     * @return  -1 if the raw data rate of this record is less than that of the argument,
     *          +1 otherwise
     *          
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    
    @Override
    public int compareTo(IngestChanTestResult recResult) {
        if (this.dblRateXmit < recResult.dblRateXmit)
            return -1;
        else
            return +1;
    }

    
    // 
    // Record Overrides
    //
    
    /**
     * <p>
     * Overrides to check for record equivalence rather than strict equality of objects.
     * </p>
     * <p>
     * The argument is first confirmed to by of type <code>IngestChanTestCase</code>. If so all its field
     * values are then checked for equality with the field values of this record.
     * </p>
     * <p>
     * @apiNote
     * The method is overridden so that the given argument can be a different object than this object.  The 
     * equality is enforced according to field values, not the record object itself.
     * </p>
     *   
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IngestChanTestResult rec) {
            boolean bolResult = (this.recTestStatus.equals(rec.recTestStatus))
                             && (this.cntFrames == rec.cntFrames)
                             && (this.szPayload == rec.szPayload)
                             && (this.durProcessed.equals(rec.durProcessed))
                             && (this.cntMsgsXmit == rec.cntMsgsXmit)
                             && (this.szAllocXmit == rec.szAllocXmit)
                             && (this.dblRateProc == rec.dblRateProc)
                             && (this.durTransmit.equals(rec.durTransmit))
                             && (this.dblRateXmit == rec.dblRateXmit)
                             && (this.lstUniRsps.equals(rec.lstUniRsps))
                             && (this.lstBidiRsps.equals(rec.lstBidiRsps));
            
            return bolResult;
        }
        
        return false;
    }


}
