/*
 * Project: dp-api-common
 * File:    IngestionFrameProcessorEvalResults.java
 * Package: com.ospreydcs.dp.api.ingest.model.frame
 * Type:    IngestionFrameProcessorEvalResults
 *
 * Copyright 2010-2023 the original author or authors.
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
 * @since Jul 31, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.frame;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * Active record class for computing <code>IngestionFrameProcessorEvaluator</code> test results 
 * from intermediate performance parameters.
 * </p> 
 * <p>
 * Note that all performance parameters must be set before invoking <code>{@link #computeResults()}</code>.
 * Once <code>{@link #computeResults()}</code> has returned without exception all performance
 * results are available.
 * </p> 
 */
public final class IngestionFrameProcessorEvalResults {


    //
    // Performance Parameters 
    //

    /** The instant of performance test start (includes payload creation) */
    public Instant  insMark = null;

    /** The instant the processor was activated */
    public Instant  insActivate = null;

    /** The instant the payload was submitted */
    public Instant  insSubmit = null;

    /** The instant all final ingest data messages are consumed */
    public Instant  insFinish = null;

    /** The instant the processor is shutdown */
    public Instant  insShutdown = null;


    //
    // Payload Parameters 
    //

    /** The number of ingestion frames within the payload */
    public Integer  cntPayloadFrames = null;

    /** The number of columns within each payload frame */
    public Integer  cntPayloadFrameCols = null;

    /** The number of rows within each payload frame */
    public Integer  cntPayloadFrameRows = null;

    /** Average allocate size of each payload frame (in bytes) */
    public Long     szPayloadFrameBytes = null;

    /** The total allocation of the payload (in bytes) */
    public Long     szPayloadTotalBytes = null;


    //
    // Processing Parameters
    //

    /** The number of polling attempts */
    public Integer  cntMessagePolls = null;

    /** The number of resulting <code>IngestDataRequest</code> messages */
    public Integer  cntProducedMessages = null;

    /** The average allocation size of each request message (in bytes) */
    public Long     szProducedMsgBytes = null;

    /** The total number of bytes comprising all the produced messages */
    public Long     szProducedTotalBytes = null;


    //
    // Internal Results
    //

    /** Approximate duration for payload creation (between test start and activate() operation) */
    private Duration durPayload = null;

    /** Duration processing ingestion frames (between submit() and finish event) */
    private Duration durProcessing = null;

    /** Duration required for processor shutdown */
    private Duration durShutdown = null;

    /** Duration processor was active (between activate() and shutdown()) */
    private Duration durActive = null;

    /** Total duration of performance test (between test start and shutdown() operation) */
    private Duration durTotalTest = null;


    /** Processing rate seen at the front end - by payload size */
    private Double dblPrcdRatePayload = null;

    /** Processing rate seen at the back end - by serialized message size */
    private Double dblPrcdRateMessages = null;

    /** Processing rate at front end including activation and shutdown */
    private Double dblActvRatePayload = null;

    /** Processing rate at back end including activation and shutdown */
    private Double dblActvRateMessage = null;

    
    //
    // State Variables
    //
    
    /** Test results computed (i.e., {@link #computeResults()} invoked). */
    private boolean bolResultsComputed = false;

    
    //
    // Configuration
    //

    /**
     * <p>
     * Sets the payload parameters from the given payload.
     * </p>
     * <p>
     * This method should be called immediately after creating payload.
     * In case of frame decomposition the payload is destroyed.
     * </p>
     * <p>
     * Sets <code>{@link #cntPayloadFrames}</code>, 
     * <code>{@link #cntPayloadFrameCols}</code>,
     * <code>{@link #cntPayloadFrameRows}</code>
     * <code>{@link #szPayloadFrameBytes}<?code>,
     * and 
     * <code>{@link #szPayloadTotalBytes}</code>,
     * parameters.
     * </p>
     * 
     * @param lstFrames payload used in the test
     */
    public void setPayloadParameters(List<IngestionFrame> lstFrames) {

        // Get example frame
        IngestionFrame  frame = lstFrames.get(0);
        this.cntPayloadFrameCols = frame.getColumnCount();
        this.cntPayloadFrameRows = frame.getRowCount();

        this.cntPayloadFrames = lstFrames.size();
        this.szPayloadTotalBytes = lstFrames.stream().mapToLong(f -> f.allocationSizeFrame()).sum();
        this.szPayloadFrameBytes = this.szPayloadTotalBytes / this.cntPayloadFrames;
    }

    /**
     * <p>
     * Sets the message production parameters from the collection of produced messages.
     * </p>
     * <p>
     * Sets <code>{@link #cntProducedMessages}</code>, <code>{@link #szProducedMsgBytes}</code>,
     * and <code>{@link #szProducedTotalBytes}</code> 
     * parameters.
     * </p>
     * 
     * @param setRqstMsgs   collection of ingestion request messages produced by the processor
     */
    public void setMessageParameters(Collection<IngestDataRequest> setRqstMsgs) {
        this.cntProducedMessages = setRqstMsgs.size();
        this.szProducedTotalBytes = setRqstMsgs.stream().mapToLong(msg -> msg.getSerializedSize()).sum();
        this.szProducedMsgBytes = this.szProducedTotalBytes / this.cntProducedMessages;
    }

    /**
     * <p>
     * Sets the number of times the processor was polled for an ingestion request message.
     * </p>
     * <p>
     * Sets parameter <code>{@link #cntMessagePolls}</code>.
     * </p>
     * 
     * @param cntPolls  number of polling actions required for full message consumption
     */
    public void setMessagePollingCount(int cntPolls) {
        this.cntMessagePolls = cntPolls;
    }


    //
    // Performance operations
    //

    /**
     * <p>
     * Marks the start of the performance test.
     * </p>
     */
    public void start() {
        this.insMark = Instant.now();
    }

    /**
     * <p>
     * Marks the instant of processor activation.
     * </p>
     */
    public void activate() {
        this.insActivate = Instant.now();
    }

    /**
     * <p>
     * Marks the instant of payload submission.
     * </p>
     */
    public void submit() {
        this.insSubmit = Instant.now();
    }

    /**
     * <p>
     * Marks the instant of payload processing completion.
     * </p>
     */
    public void finish() {
        this.insFinish = Instant.now();
    }

    /**
     * <p> 
     * Marks the instant of processor shutdown.
     * </p>
     */
    public void shutdown() {
        this.insShutdown = Instant.now();
    }


    //
    // Operations
    //

    /**
     * <p>
     * Computes the performance results once all performance parameters have been set.
     * </p>
     * 
     * @throws IllegalStateException    missing performance parameter(s)
     */
    public void computeResults() throws IllegalStateException {

        // Check for performance parameters
        if (this.insMark==null || 
                this.insActivate==null ||
                this.insSubmit==null ||
                this.insFinish==null ||
                this.insShutdown==null ||
                this.cntPayloadFrames==null ||
                //                this.cntRqstMsgPolls==null || 
                this.cntProducedMessages==null ||
                this.szPayloadTotalBytes==null ||
                this.szProducedTotalBytes==null
                )
            throw new IllegalStateException("Missing required parameter(s) - Not all results parameters were specified.");

        // Compute test durations
        this.durPayload = Duration.between(this.insMark, this.insActivate);
        this.durProcessing = Duration.between(this.insSubmit, this.insFinish);
        this.durShutdown = Duration.between(this.insFinish, this.insShutdown);
        this.durActive = Duration.between(this.insActivate, this.insShutdown);
        this.durTotalTest = Duration.between(this.insMark, this.insShutdown);

        // Compute processing durations as double values
        double      dblDurProcessing  = ((double)durProcessing.toNanos()) / 1.0e9;
        double      dblDurActive      = ((double)durActive.toNanos()) / 1.0e9;

        this.dblPrcdRatePayload = ((double)this.szPayloadTotalBytes) / dblDurProcessing;
        this.dblPrcdRateMessages = ((double)this.szProducedTotalBytes) / dblDurProcessing;
        this.dblActvRatePayload = ((double)this.szPayloadTotalBytes) / dblDurActive;
        this.dblActvRateMessage = ((double)this.szProducedTotalBytes) / dblDurActive;
        
        // Set state variable
        this.bolResultsComputed = true;
    }


    //
    // Test Results
    //

    /**
     * @return the front-end (payload) processing rate (in Mbps)
     * 
     * @throws  IllegalStateException   test results were not computed - invoke {@link #computeResults()} before invoking
     */
    public double   getPayloadProcessingRateRawMbps() throws IllegalStateException {
        
        // Check state
        if (!this.bolResultsComputed)
            throw new IllegalStateException("Test results not computed - invoke computeResults() before invoking.");
        
        return this.dblPrcdRatePayload / 1_000_000;
    }

    /**
     * @return  the back-end (serialized request message) processing rate (in Mbps)
     * 
     * @throws  IllegalStateException   test results were not computed - invoke {@link #computeResults()} before invoking
     */
    public double   getMessageProcessingRateRawMbps() throws IllegalStateException {
        
        // Check state
        if (!this.bolResultsComputed)
            throw new IllegalStateException("Test results not computed - invoke computeResults() before invoking.");
        
        return this.dblPrcdRateMessages;
    }

    /**
     * @return the front-end (payload) processing rate including shutdown (in Mbps)
     * 
     * @throws  IllegalStateException   test results were not computed - invoke {@link #computeResults()} before invoking
     */
    public double   getPayloadProcessingRateFullMbps() throws IllegalStateException {
        
        // Check state
        if (!this.bolResultsComputed)
            throw new IllegalStateException("Test results not computed - invoke computeResults() before invoking.");
        
        return this.dblActvRatePayload;
    }

    /**
     * @return  the back-end (serialized request message) processing rate including shutdown (in Mbps)
     * 
     * @throws  IllegalStateException   test results were not computed - invoke {@link #computeResults()} before invoking
     */
    public double   getMessageProcessingRateFullMbps() throws IllegalStateException {
        
        // Check state
        if (!this.bolResultsComputed)
            throw new IllegalStateException("Test results not computed - invoke computeResults() before invoking.");
        
        return this.dblActvRateMessage;
    }


    /**
     * @return  the duration required to create the payload
     * 
     * @throws  IllegalStateException   test results were not computed - invoke {@link #computeResults()} before invoking
     */
    public Duration getDurationPayloadCreate() throws IllegalStateException {
        
        // Check state
        if (!this.bolResultsComputed)
            throw new IllegalStateException("Test results not computed - invoke computeResults() before invoking.");
        
        return this.durPayload;
    }

    /**
     * @return  the duration required to process all ingestion frames, not including activation or shutdown
     * 
     * @throws  IllegalStateException   test results were not computed - invoke {@link #computeResults()} before invoking
     */
    public Duration getDurationProcessing() throws IllegalStateException {
        
        // Check state
        if (!this.bolResultsComputed)
            throw new IllegalStateException("Test results not computed - invoke computeResults() before invoking.");
        
        return this.durProcessing;
    }

    /**
     * @return  the duration while the processor was active (between activate() and shutdown())
     * 
     * @throws  IllegalStateException   test results were not computed - invoke {@link #computeResults()} before invoking
     */
    public Duration getDurationActive() throws IllegalStateException {
        
        // Check state
        if (!this.bolResultsComputed)
            throw new IllegalStateException("Test results not computed - invoke computeResults() before invoking.");
        
        return this.durActive;
    }

    /**
     * @return  the duration required to shut down the processor
     * 
     * @throws  IllegalStateException   test results were not computed - invoke {@link #computeResults()} before invoking
     */
    public Duration getDurationShutdown() throws IllegalStateException {
        
        // Check state
        if (!this.bolResultsComputed)
            throw new IllegalStateException("Test results not computed - invoke computeResults() before invoking.");
        
        return this.durShutdown;
    }

    /**
     * @return  the total duration required for the full performance test
     * 
     * @throws  IllegalStateException   test results were not computed - invoke {@link #computeResults()} before invoking
     */
    public Duration getDurationTotal() throws IllegalStateException {
        
        // Check state
        if (!this.bolResultsComputed)
            throw new IllegalStateException("Test results not computed - invoke computeResults() before invoking.");
        
        return this.durTotalTest;
    }


    //
    // Convenience Methods
    //

    /**
     * <p>
     * Prints out the configuration of the given <code>IngestionFrameProcessor</code> to the given <code>PrintStream</code>.
     * </p>
     * <p>
     * Prints out a textual description of the ingestion frame processor instance in its current configuration.
     * </p>
     * 
     * @param os        print stream (output stream) to receive text output description
     * @param processor target <code>IngestionFrameProcessor</code> used in test
     * @param strHdr    optional header line to be included in description (if non-<code>null</code>)
     * 
     * @throws  IllegalStateException   test results were not computed - invoke {@link #computeResults()} before invoking
     */
    public void printProcessorConfig(PrintStream os, IngestionFrameProcessor processor, String strHdr) throws IllegalStateException {

        // Check state
        if (!this.bolResultsComputed)
            throw new IllegalStateException("Test results not computed - invoke computeResults() before invoking.");
        
        if (strHdr != null)
            os.println(strHdr);
        os.println("  Concurrency         : " + processor.hasConcurrency());
        os.println("    Thread count      : " + processor.getConcurrencyCount());
        os.println("  Frame decomposition : " + processor.hasFrameDecomposition());
        os.println("    Max frame size    : " + processor.getMaxFrameSize());

    }

    /**
     * <p>
     * Prints out the payload configuration (i.e., the ingestion frame list) to the given <code>PrintStream</code>.
     * </p>
     * <p>
     * Prints out a textual description of the given <code>List&lt;IngestionFrame&gt;</code> instance.
     * </p> 
     *   
     * @param os        print stream (output stream) to receive text output description
     * @param strHdr    optional header line to be included in description (if non-<code>null</code>)
     * 
     * @throws  IllegalStateException   test results were not computed - invoke {@link #computeResults()} before invoking
     */
    public void printPayloadConfig(PrintStream os, String strHdr) throws IllegalStateException {
        
        // Check state
        if (!this.bolResultsComputed)
            throw new IllegalStateException("Test results not computed - invoke computeResults() before invoking.");
        
        if (strHdr != null)
            os.println(strHdr);
        os.println("  Payload - IngestionFrame instances ");
        os.println("    Number of frames       : " + this.cntPayloadFrames);
        os.println("    Columns per frame      : " + this.cntPayloadFrameCols);
        os.println("    Rows per frame         : " + this.cntPayloadFrameRows);
        os.println("    Avg. allocation (bytes): " + this.szPayloadFrameBytes);
        os.println("  Total allocation (bytes) : " + this.szPayloadTotalBytes);
    }

    /**
     * <p>
     * Prints out a textual description of the activity for the given test results.
     * </p>
     * <p>
     * Prints out the test parameters and the time durations for each activity.
     * </p>
     * 
     * @param os        print stream (output stream) to receive text output description
     * @param strHdr    optional header line to be included in description if (non-<code>null</code>)
     * 
     * @throws  IllegalStateException   test results were not computed - invoke {@link #computeResults()} before invoking
     */
    public void    printTestActivity(PrintStream os, String strHdr) throws IllegalStateException {
        
        // Check state
        if (!this.bolResultsComputed)
            throw new IllegalStateException("Test results not computed - invoke computeResults() before invoking.");
        
        if (strHdr != null)
            os.println(strHdr);
        if (this.cntMessagePolls!=null)
            os.println("  Polling attempts      : " + this.cntMessagePolls);
        os.println("  Messages produced     : " + this.cntProducedMessages);
        os.println("  Total bytes produced  : " + this.szProducedTotalBytes);
        os.println("  Time processing       : " + this.getDurationProcessing());
        os.println("  Time for shutdown     : " + this.getDurationShutdown());
        os.println("  Time processor active : " + this.getDurationActive());
        os.println("  Tie for total test    : " + this.getDurationTotal());
    }

    /**
     * <p>
     * Prints out a textual description of the results within the given test result record.
     * </p>
     * 
     * @param os        print stream (output stream) to receive text output description
     * @param strHdr    optional header line to be included in description if (non-<code>null</code>)
     * 
     * @throws  IllegalStateException   test results were not computed - invoke {@link #computeResults()} before invoking
     */
    public void    printTestResults(PrintStream os, String strHdr) throws IllegalStateException {
        
        // Check state
        if (!this.bolResultsComputed)
            throw new IllegalStateException("Test results not computed - invoke computeResults() before invoking.");
        
        if (strHdr != null)
            os.println(strHdr);
        System.out.println("  Full Processing Rate (with activation and shutdown)");
        System.out.println("    Payload (frontend): " + this.getPayloadProcessingRateFullMbps() + " Mbps");
        System.out.println("    Message  (backend): " + this.getMessageProcessingRateFullMbps() + " Mbps");
        System.out.println("  Raw Processing Rate (w/out activation and shutdown (Mbps)");
        System.out.println("    Payload (frontend): " + this.getPayloadProcessingRateRawMbps() + " Mbps");
        System.out.println("    Message  (backend): " + this.getMessageProcessingRateRawMbps() + " Mbps");
    }
    
    /**
     * <p>
     * Convenience method for printing all available test descriptions.
     * </p>
     * <p>
     * Invokes all print method in order of declaration.  The optional header string is used
     * only for the first method <code>{@link #printProcessorConfig(PrintStream, IngestionFrameProcessor, String)}</code>.
     * </p>
     * 
     * @param os        print stream (output stream) to receive text output description
     * @param processor target <code>IngestionFrameProcessor</code> used in test
     * @param strHdr    optional header line to be included in description (if non-<code>null</code>)
     * 
     * @throws  IllegalStateException   test results were not computed - invoke {@link #computeResults()} before invoking
     * 
     * @see #printProcessorConfig(PrintStream, IngestionFrameProcessor, String)
     * @see #printPayloadConfig(PrintStream, String)
     * @see #printTestActivity(PrintStream, String)
     * @see #printTestResults(PrintStream, String)
     */
    public void printAll(PrintStream os, IngestionFrameProcessor processor, String strHdr) throws IllegalStateException {
        
        // Check state
        if (!this.bolResultsComputed)
            throw new IllegalStateException("Test results not computed - invoke computeResults() before invoking.");
        
        this.printProcessorConfig(os, processor, strHdr);
        this.printPayloadConfig(os, null);
        this.printTestActivity(os, null);
        this.printTestResults(os, null);
    }

}