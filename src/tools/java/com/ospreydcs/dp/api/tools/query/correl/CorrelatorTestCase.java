/*
 * Project: dp-api-common
 * File:	CorrelatorTestCase.java
 * Package: com.ospreydcs.dp.api.tools.query.correl
 * Type: 	CorrelatorTestCase
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
 * @since May 30, 2025
 *
 */
package com.ospreydcs.dp.api.tools.query.correl;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionException;

import com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator;
import com.ospreydcs.dp.api.tools.query.request.TestArchiveRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

/**
 * <p>
 * Record containing parameter for a raw, time-series correlation test case.
 * </p>
 * <p>
 * The record contains configuration parameters for raw, time-series data correlators.
 * It also contains the time-series data request supplying the test data, which is
 * taken from the <code>{@link TestArchiveRequest}</code> enumeration.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * The <code>{@link #indCase}</code> field is created within creator 
 * <code>{@link #from(TestArchiveRequest, boolean, int, int)}</code> and should not be 
 * explicitly supplied.  That is, use of the canonical constructor should be avoided.
 * </p>  
 *
 * @param enmRqst       the time-series data request supplying raw data 
 * @param bolConcOpt    enable/disable multi-threaded concurrency while correlation processing
 * @param cntMaxThrds   maximum number of execution threads when concurrency is enabled
 * @param szConcPivot   the target set size triggering multi-threading when enabled
 * 
 * @author Christopher K. Allen
 * @since May 30, 2025
 * 
 */
public record CorrelatorTestCase(
        int                 indCase,
        TestArchiveRequest  enmRqst,
        boolean             bolConcOpt,
        int                 cntMaxThrds,
        int                 szConcPivot
        ) 
{

    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new <code>CorrelatorTestCase</code> instance populated from the argument values.
     * </p>
     * 
     * @param enmRqst       the time-series data request supplying raw data 
     * @param bolConcOpt    enable/disable multi-threaded concurrency while correlation processing
     * @param cntMaxThrds   maximum number of execution threads when concurrency is enabled
     * @param szConcPivot   the target set size triggering multi-threading when enabled
     * 
     * @return  a new <code>CorrelatorTestCase</code> record with fields given by the arguments
     */
    public static CorrelatorTestCase    from(
            TestArchiveRequest  enmRqst,
            boolean             bolConcOpt,
            int                 cntMaxThrds,
            int                 szConcPivot
            )
    {
        return new CorrelatorTestCase(IND_CASE, enmRqst, bolConcOpt, cntMaxThrds, szConcPivot); 
    }
    
    
    //
    // Record Resources
    //
    
    /** Internal test case index (counter) */
    private static          int IND_CASE = 1;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>QueryChannelTestCase</code> instance.
     * </p>
     * <p>
     * Canonical constructor.  Sets field values and increments record index counter <code>{@link #IND_CASE}</code>
     * </p>
     *
     * @param enmRqst       the time-series data request supplying raw data 
     * @param bolConcOpt    enable/disable multi-threaded concurrency while correlation processing
     * @param cntMaxThrds   maximum number of execution threads when concurrency is enabled
     * @param szConcPivot   the target set size triggering multi-threading when enabled
     */
    public CorrelatorTestCase {
        IND_CASE++;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Evaluates the given correlator for the given set of recovered raw, time-series data messages.
     * </p>
     * 
     * @param processor     correlator under evaluation
     * @param lstRspMsgs    the collection of <code>QueryData</code> response message to correlate
     * 
     * @return  the results of the evaluation
     * 
     * @throws IllegalArgumentException the argument contained invalid timestamps
     * @throws CompletionException      error in <code>DataBucket</code> insertion task execution (see cause)
     */
    public CorrelatorTestResult evaluate(RawDataCorrelator processor, List<QueryDataResponse.QueryData> lstRspMsgs) 
            throws IllegalArgumentException, CompletionException {
        
        // Save processor configuration
        final boolean   bolMultThrd = processor.isConcurrencyEnabled();
        final int       cntMaxThrds = processor.getConcurrencytMaxThreads();
        final int       szConcPivot = processor.getConcurrencyPivotSize();
        
        // Configure the processor
        processor.enableConcurrency(this.bolConcOpt);
        processor.setMaxThreadCount(this.cntMaxThrds);
        processor.setConcurrencyPivotSize(this.szConcPivot);
        
        // Perform the evaluation
        processor.reset();
        
        Instant     insStart = Instant.now();
        for (QueryDataResponse.QueryData msgData : lstRspMsgs) {
            processor.processQueryData(msgData);
        }
        Instant     insFinish = Instant.now();
        
        // Collect the results
        long        szRspMsgs = lstRspMsgs.stream().mapToLong(msg -> msg.getSerializedSize()).sum();
        
        Duration    durProcessed = Duration.between(insStart, insFinish);
        int         cntRspMsgs = lstRspMsgs.size();
        int         cntCorrelSet = processor.sizeCorrelatedSet();
        long        szProcessed = processor.getBytesProcessed();
        double      dblDataRate = ( ((double)szProcessed) * 1000 )/durProcessed.toNanos();
        
        // Restore the processor to its original state and configuration
        processor.reset();
        processor.enableConcurrency(bolMultThrd);
        processor.setMaxThreadCount(cntMaxThrds);
        processor.setConcurrencyPivotSize(szConcPivot);
        
        // Return the test results
        CorrelatorTestResult    recResult = CorrelatorTestResult.from(cntRspMsgs, szRspMsgs, cntCorrelSet, szProcessed, durProcessed, dblDataRate, this);
        
        return recResult;
    }
    
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
        
        ps.println(strPad + this.getClass().getSimpleName() + " " + this.indCase + ":");
        ps.println(strPad + "  Data Request ID                   : " + this.enmRqst.name());
        ps.println(strPad + "  Concurrent processing enabled     : " + this.bolConcOpt);
        ps.println(strPad + "  Maximum concurrency thread count  : " + this.cntMaxThrds);
        ps.print(  strPad + "  Pivot size triggering concurrency : " + this.szConcPivot);
    }

}
