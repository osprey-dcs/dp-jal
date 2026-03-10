/*
 * Project: dp-jal
 * File:	IngestApiTestResult.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.api
 * Type: 	IngestApiTestResult
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
 * @since Mar 5, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.api;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

import com.ospreydcs.dp.jal.common.IngestionResult;
import com.ospreydcs.dp.jal.common.ProviderUID;
import com.ospreydcs.dp.jal.common.ResultStatus;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Record containing the results from an <code>IngestApiTestCase</code> evaluation.
 * </p>
 * <p>
 * Instances of this record are created from the test case evaluation operation
 * <code>{@link IngestApiTestCase#evaluate(com.ospreydcs.dp.jal.ingest.IIngestionService, com.ospreydcs.dp.jal.ingest.IIngestionStream)}</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 5, 2026
 *
 * @param recTestStatus status of the test case evaluation
 * @param cntFrames     number of ingestion frames in the payload used for the evaluation
 * @param szPayload     allocation size (in bytes) of the payload used for the evaluation
 * @param durTransmit   transmission time for the ingestion payload
 * @param cntMsgsXmit   number of <code>IngestDataRequest</code> messaged processed and transmitted  
 * @param <s>szAllocXmit   allocation size (in bytes) of all data messages processed and transmitted</s> 
 * @param dblRateXmit   transmission rate (in MBps) for ingestion payload
 * @param recProvUid    Data Provider registration UID created by Ingestion Service
 * @param lstResults    results of ingestion operations (as reported by Ingestion Service)
 * @param recTestCase   the test case defining the evaluation parameters
 */
public record IngestApiTestResult(
        ResultStatus            recTestStatus,
        
        int                     cntFrames,
        long                    szPayload,
        
        Duration                durTransmit,
        int                     cntMsgsXmit,
        double                  dblRateXmit,
        
        ProviderUID             recProvUid,
        List<IngestionResult>   lstResults,
        
        IngestApiTestCase       recTestCase
        ) implements Comparable<IngestApiTestResult>
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>IngestApiTestResult</code> instance populated from the given arguments.
     * </p>
     * <p>
     * This creator is equivalent to the canonical constructor
     * </p>
     * 
     * @param recTestStatus status of the test case evaluation
     * @param cntFrames     number of ingestion frames in the payload used for the evaluation
     * @param szPayload     allocation size (in bytes) of the payload used for the evaluation
     * @param durTransmit   transmission time for the ingestion payload
     * @param cntMsgsXmit   number of <code>IngestDataRequest</code> messaged processed and transmitted  
     * @param dblRateXmit   transmission rate (in MBps) for ingestion payload
     * @param recProvUid    Data Provider registration UID created by Ingestion Service
     * @param lstResults    results of ingestion operations (as reported by Ingestion Service)
     * @param recTestCase   the test case defining the evaluation parameters
     *
     * @return  a new <code>IngestApiTestResult</code> instances with field values given by the arguments
     */
    public static IngestApiTestResult   from(
            ResultStatus            recTestStatus,
            
            int                     cntFrames,
            long                    szPayload,
            
            Duration                durTransmit,
            int                     cntMsgsXmit,
            double                  dblRateXmit,
            
            ProviderUID             recProvUid,
            List<IngestionResult>   lstResults,
            
            IngestApiTestCase       recTestCase
            ) 
    {
        return new IngestApiTestResult(recTestStatus, cntFrames, szPayload, durTransmit, cntMsgsXmit, dblRateXmit, recProvUid, lstResults, recTestCase);
    }

    /**
     * <p>
     * Creates and returns a new <code>IngestApiTestResult</code> instance for a failed test evaluation.
     * </p>
     * <p>
     * This is a convenience creator available for failed test case evaluations.  Fields <code>{@link #recTestStatus()}</code>
     * and <code>{@link #recTestCase()}</code> are populated with the given arguments while all other fields are set to 
     * zero.
     * </p>
     * 
     * @param recTestStatus failed test status with message and possible cause 
     * @param recTestCase   the test case that failed
     * 
     * @return  a new <code>IngestApiTestResult</code> instance for a failed test case
     * 
     * @throws IllegalArgumentException the test status record indicated SUCCESS
     */
    public static IngestApiTestResult    from(ResultStatus recTestStatus, IngestApiTestCase recTestCase) throws IllegalArgumentException {
        
        // Check argument
        if (recTestStatus.isSuccess())
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Test status indicates SUCCESS.");
        
        // Return record with empty result fields
        return IngestApiTestResult.from(recTestStatus, 0, 0L, Duration.ZERO, 0, 0, REC_PROV_UID_EMPTY, List.of(), recTestCase);
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
     * <code>IngestApiTestResult</code> records, specifically the <code>{@link IngestApiTestCase#indCase()}</code> fields.
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
    public static Comparator<IngestApiTestResult>   caseIndexOrdering() {
        
        Comparator<IngestApiTestResult> cmp = (r1, r2) -> {
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
     * <code>IngestApiTestResult</code> records.  It provides a reverse ordering of records according
     * to the data rate fields.  Specifically, the highest data rate will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * Note that the comparator provided here is the complement of the natural order of 
     * <code>IngestApiTestResult</code> records provided by the exposed <code>Comparable</code> interface.
     * </p>  
     * 
     * @return  a new <code>Comparator</code> instance providing a reverse ordering by record raw data rates
     */
    public static Comparator<IngestApiTestResult>   descendingTransmissionRateOrdering() {
    
        Comparator<IngestApiTestResult>   cmp = (r1, r2) -> {

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
     * <code>IngestApiTestResult</code> records.  It provides a natural ordering of records according
     * to the data rate fields.  Specifically, the lowest data rate will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * Note that the comparator provided here is the equivalent of the natural order of 
     * <code>IngestApiTestResult</code> records provided by the exposed <code>Comparable</code> interface.
     * </p>  
     * 
     * @return  a new <code>Comparator</code> instance providing a natural ordering by record raw data rates
     */
    public static Comparator<IngestApiTestResult>    ascendingTransmissionRateOrdering() {

        Comparator<IngestApiTestResult>  cmp = (r1, r2) -> {

            if (r1.dblRateXmit < r2.dblRateXmit)
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
        String  strPaddd = strPadd + "  ";
        
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
        ps.println(strPadd + "Transmission duration             : " + this.durTransmit);
        ps.println(strPadd + "Transmitted message count         : " + this.cntMsgsXmit);
        ps.println(strPadd + "Tranmission rate  (MBps)          : " + this.dblRateXmit);
        if (this.lstResults!=null && !this.lstResults.isEmpty()) {
            ps.println(strPadd + "Ingestion result count            : " + this.lstResults.size());
            
            int     indResult = 1;
            for (IngestionResult recResult : this.lstResults) {
                ps.println(strPaddd + "Ingestion Result #" + Integer.toString(indResult));
                recResult.printOut(ps, strPaddd);
                indResult++;
            }
        }
        ps.println(strPadd + "Test Case Parameters");
        this.recTestCase.printOut(ps, strPadd);
    }


    //
    // Comparable Interface
    //
    
    /**
     * <p>
     * Provides a forward order of <code>IngestApiTestResult</code> records by raw data rate.
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
    public int compareTo(IngestApiTestResult recResult) {
        if (this.dblRateXmit < recResult.dblRateXmit)
            return -1;
        else
            return +1;
    }

    
    //
    // Record Overrides
    //
    
    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof IngestApiTestResult rec) {
            boolean bolResult = (this.recTestStatus.equals(rec.recTestStatus))
                             && (this.cntFrames == rec.cntFrames)
                             && (this.szPayload == rec.szPayload)
                             && (this.durTransmit.equals(rec.durTransmit))
                             && (this.cntMsgsXmit == rec.cntMsgsXmit)
                             && (this.dblRateXmit == rec.dblRateXmit)
                             && (this.lstResults.equals(rec.lstResults))
                             && (this.recTestCase.equals(rec.recTestCase));
            
            return bolResult;
        }
        
        return false;
    }
    
    
    //
    // Record Resources
    //
    
    /** Data Provider name */
    public static final String          STR_PROVIDER_NAME = IngestApiTestCase.REC_PROVIDER_REG.name();
    
    /** The empty Data Provider registration UID */
    public static final ProviderUID     REC_PROV_UID_EMPTY = ProviderUID.from("FAILED_REGISTRATION", STR_PROVIDER_NAME, false); 

}
