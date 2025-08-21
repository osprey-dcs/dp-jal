/*
 * Project: dp-api-common
 * File:	IngestionChannelEvaluator.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IngestionChannelEvaluator
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
 * @since Aug 9, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.grpc;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.junit.Assert;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.common.IngestionResult;
import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.ingest.test.TestIngestDataRequestGenerator;
import com.ospreydcs.dp.api.model.IMessageSupplier;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * Active record class for evaluating <code>IngestionChannel</code> performance.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 9, 2024
 *
 */
public class IngestionChannelEvaluator {

    //
    // Class Types
    //
    
    /**
     * <p>
     * Record containing the configuration parameters for the <code>IngestionChannelEvaluator</code> 
     * performance evaluation.
     * </p>
     * 
     * @param   streamType  the gRPC stream type for <code>IngestionChannel</code> 
     * @param   streamCount the number of gRPC data stream for <code>IngestionChannel</code> 
     */
    public static record IngestionChannelEvaluatorConfig(DpGrpcStreamType streamType, int streamCount) {

        /**
         * <p>
         * Constructs a new instance of <code>IngestionChannelEvaluatorConfig</code>.
         * </p>
         * <p>
         * Defaults to bidirectional gRPC data stream.
         * </p>
         *
         * @param streamCount   the number of gRPC data stream for <code>IngestionChannel</code>
         */
        public IngestionChannelEvaluatorConfig(int streamCount) {
            this(DpGrpcStreamType.BIDIRECTIONAL, streamCount);
        }
        
        /**
         *
         * @see @see java.lang.Object#toString()
         */
        @Override
        public String   toString() {
            String      str = "Evaluation Configuration: stream type = " + streamType + "; data stream count = " + streamCount;
            
            return str;
        }
    }
    
    
    //
    // Class Constants
    //
    
    /** Create new payload (rather than class payload) flag */
    public static final boolean BOL_NEW_PAYLOAD = true;
    
    
    /** Test Payload source frames: count */
    public static final int     CNT_FRAMES = 15;
    
    /** Test Payload source frames: source number of table columns */
    public static final int     CNT_COLS = 1000;
    
    /** Test Payload source frames: number of table rows */
    public static final int     CNT_ROWS = 1000;
    
    
    
    //
    // Class Resources
    //
    
    /** Collection of data request messages available as payload for all tests */
    private static final List<IngestDataRequest>    LST_MSG_RQSTS = TestIngestDataRequestGenerator.createDoublesMessagesWithClock(CNT_FRAMES, CNT_COLS, CNT_ROWS);
    
    /** The number of messages within the test message payload */
    private static final int                        CNT_MSGS_PAYLOAD = LST_MSG_RQSTS.size();
    
    /** The total memory allocation of the test message payload */
    private static final long                       SZ_PAYLOAD_ALLOC = LST_MSG_RQSTS.stream().mapToLong(msg -> msg.getSerializedSize()).sum();
    
    /** The average memory allocation of a payload message */
    private static final long                       SZ_MSG_ALLOC_AVG = SZ_PAYLOAD_ALLOC / CNT_MSGS_PAYLOAD;
    
    
    //
    // Defining Attributes
    //
    
    /** The test configuration */
    private final IngestionChannelEvaluatorConfig   cfgTest;
    
    
    //
    // Test Resources
    //
    
    /** The <code>IngestionChannel</code> instance under evaluation */
    private IngestionChannel    chan = null;
    
    /** The thread containing the message buffer shutdown operation */
    private Thread              thdSBuffShdn = null;
    
    
    //
    // Performance Parameters - Set During Test
    //

    /** The instant of performance test start (includes resource creation) */
    private Instant  insStart = null;

    /** The instant the channel was activated */
    private Instant  insActivate = null;

    /** The instant all final ingest data messages are transmitted */
    private Instant  insFinish = null;

    /** The instant the message buffer is shutdown */
    private Instant  insBuffShdn = null;
    
    
    //
    // Test Results
    //
    
    /** IngestionChannel activate() result */
    private Boolean         bolActivate = null;
    
    /** IngestionChannel shutdown() result */
    private Boolean         bolShutdown = null;
    
    
    /** Ingestion operation result */
    private IngestionResult recResult = null;

    
    /** Initialization duration for the evaluation (between start and activation) */
    private Duration    durInit = null;
    
    /** Data transmission duration (between activation and shutdown) */
    private Duration    durXmit = null;
    
    /** Duration for message buffer shutdown */
    private Duration    durBuffShdn = null;
    
    /** Total test duration */
    private Duration    durTotal = null;

    
    /** Raw data transmission rate  */
    private Double      dblXmitRateRaw = null;
    
    /** Full data transmission rate (includes activation, shutdown) */
    private Double      dblXmitRateFull = null;
    
    
    //
    // State Variables
    //
    
    /** Indicates whether or not <code>{@link #evaluate(DpIngestionConnection)}</code> has been invoked */
    private Boolean         bolEvaluate = null;
    
    /** Contains the status of the evaluation */
    private ResultStatus    recStatus = null;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionChannelEvaluator</code>.
     * </p>
     *
     */
    public IngestionChannelEvaluator(IngestionChannelEvaluatorConfig cfgTest) {
        this.cfgTest = cfgTest;
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Performs the <code>IngestionChannel</code> performance evaluation using the given Ingestion Service connection.
     * </p>
     * <p>
     * The performance evaluation is performed for a <code>IngestionChannel</code> instance with the configuration
     * given at construction and the standard message payload given by <code>{@link #LST_MSG_RQSTS}</code>.
     * Results of the evaluation are then available with the result inquiry methods.
     * </p>
     * <p>
     * If a value <code>true</code> is returned then the evaluation completed successfully and all results are
     * available; specifically methods <code>{@link #getTransmissionRateRaw()}, {@link #getTransmissionRateFull()},
     * {@link #printResults(PrintStream, String)}</code>.
     * </p>
     * 
     * @param connIngest    connection to the Ingestion Service
     * 
     * @return  <code>true</code> if the evaluation completed successfully, 
     *          <code>false</code> otherwise (see {@link #getStatus()})
     */
    public boolean evaluate(DpIngestionConnection connIngest) {
        
        this.insStart = Instant.now();
        
        this.bolEvaluate = true;
        
        // Create a message buffer
        // - Load buffer with the payload and activate, shutdown thread is returned 
        IngestionMemoryBuffer buffer = this.createAndloadMessageBuffer();
        
        if (buffer == null) {
            this.recStatus = ResultStatus.newFailure("Message buffer failed to load and activate.");
            return false;
        }
        
        // Create the IngestionChannel
        this.chan = this.createAndConfigChannel(buffer, connIngest);
        
        try {
            // Activate channel starting transmission
            this.insActivate = Instant.now();
            this.bolActivate = this.chan.activate();
            
            //  Since shutdown() operation is blocking - does not return until all messages are transmitted
            this.bolShutdown = this.chan.shutdown();
            this.insFinish = Instant.now();
            this.recResult= this.chan.getIngestionResult();
        
        } catch (UnsupportedOperationException | RejectedExecutionException e) {
            this.recStatus = ResultStatus.newFailure("IngestionChannel instance failed to activate().", e);
            return false;
            
        } catch (InterruptedException e) {
            this.recStatus = ResultStatus.newFailure("Interrupted while waiting for IngestionChannel to shutdown.", e);
            return false;
        }
        
        try {
            this.thdSBuffShdn.join();
            this.insBuffShdn = Instant.now();
            
        } catch (InterruptedException e) {
            Assert.fail("Buffer shutdown thread was interrupted during join(): " + e.getMessage());
            return false;
        }
        
        // Set state variables 
        this.recStatus = ResultStatus.SUCCESS;
        
        // Compute results and return   
        this.computeResults();
        
        return true;
    }
    
    
    //
    // Results Inquiry
    //
    
    /**
     * <p>
     * Returns the status of the performance evaluation.
     * </p>
     * <p>
     * If the evaluation failed (i.e., method <code>{@link #evaluate(DpIngestionConnection)}</code>) then
     * the returns status contains a failure description and possibly the causing exception.
     * </p>
     * 
     * @return  status of the performance evaluation (i.e., success or failure)
     * 
     * @throws IllegalStateException    the evaluation was never performed
     */
    public ResultStatus  getStatus() throws IllegalStateException {
        
        // Check state
        if (this.bolEvaluate == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - The evaluation was not run.");
        
        return this.recStatus;
    }
    
    /**
     * <p>
     * Returns the raw transmission rate of the <code>IngestionChannel</code> under test.
     * </p>
     * <p>
     * The raw transmission rate is the payload memory allocation (in bytes) divided by the 
     * time duration that the <code>IngestionChannel</code> is enabled (i.e., until shut down).
     * </p>
     *  
     * @return  the raw transmission rate in Mbps
     * 
     * @throws IllegalStateException    the evaluation was not yet performed or incomplete 
     */
    public double   getTransmissionRateRaw() throws IllegalStateException {
        
        // Check state
        if (this.bolEvaluate == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - The evaluation was not run.");
        
        if (this.recStatus.isFailure())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - The evaluation failed.");
        
        return this.dblXmitRateRaw / 1_000_000;
    }
    
    /**
     * <p>
     * Returns the full transmission rate of the <code>IngestionChannel</code> under test.
     * </p>
     * <p>
     * The full transmission rate is the payload memory allocation (in bytes) divided by the 
     * time duration that the <code>IngestionChannel</code> is enabled (i.e., until shut down)
     * plus the time duration for the payload supply buffer to shut down.
     * </p>
     *  
     * @return  the full transmission rate in Mbps
     * 
     * @throws IllegalStateException    the evaluation was not yet performed or incomplete 
     */
    public double   getTransmissionRateFull() throws IllegalStateException {
        
        // Check state
        if (this.bolEvaluate == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - The evaluation was not run.");
        
        if (this.recStatus.isFailure())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - The evaluation failed.");
        
        return this.dblXmitRateFull / 1_000_000;
    }

    /**
     * <p>
     * Prints a textual description of the evaluation results to the given print stream.
     * </p>
     * <p>
     * A description of the test configuration and conditions is printed to the given output along
     * with a summary of the <code>IngestionChannel</code> activity.
     * The results of the evaluation are then provided, that is, the transmission rates.
     * </p>
     * <p>
     * If the evaluation failed a failure description is printed instead.
     * </p>
     * 
     * @param psOut     the output print stream to receive evaluation results
     * @param strHdr    optional header line or <code>null</code> if none desired
     * 
     * @throws IllegalStateException    the evaluation was not yet performed or incomplete 
     */
    public void printResults(PrintStream psOut, String strHdr) throws IllegalStateException {
        
        // Check state
        if (this.bolEvaluate == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Test was never run.");
        
        // If evaluation failed print out failure description
        if (this.recStatus.isFailure()) {
            if (strHdr != null)
                psOut.println(strHdr);
            psOut.println("  Test FAILED evaluation ");
            psOut.println("    Detail message     : " + this.recStatus.message());
            if (this.recStatus.hasCause()) {
                psOut.println("    Exception type     : " + this.recStatus.cause().getClass().getSimpleName());
                psOut.println("    Excepation message : " + this.recStatus.cause().getMessage());
            }
            
            return;
        }
        
        // Print out evaluation description and results
        if (strHdr != null)
            psOut.println(strHdr);
        psOut.println("  IngestionChannel Configuration ");
        psOut.println("    gRPC stream type       : " + this.cfgTest.streamType());
        psOut.println("    gRPC data stream count : " + this.cfgTest.streamCount());
        psOut.println("  Payload Configuration " );
        psOut.println("    Message count            : " + IngestionChannelEvaluator.CNT_MSGS_PAYLOAD);
        psOut.println("    Avg. message allocation  : " + IngestionChannelEvaluator.SZ_MSG_ALLOC_AVG);
        psOut.println("    Total payload allocation : " + IngestionChannelEvaluator.SZ_PAYLOAD_ALLOC);
        psOut.println("  Test Activity");
        psOut.println("    Transmission duration    : " + this.durXmit);
        psOut.println("    Initialization duration  : " + this.durInit);
        psOut.println("    Buffer shutdown duration : " + this.durBuffShdn);
        psOut.println("    Total test duration      : " + this.durTotal);
        psOut.println("  IngestionChannel Activity");
        psOut.println("    activate() result      : " + this.bolActivate);
        psOut.println("    shutdown() result      : " + this.bolShutdown);
        psOut.println("    Message transmit count : " + this.chan.getRequestCount());
        psOut.println("    Response message count : " + this.chan.getResponseCount());
        psOut.println("    Accepted request count : " + this.recResult.acceptedRequestCount());
        psOut.println("    Response error count   : " + this.recResult.exceptions().size());
        psOut.println("  Evaluation Results");
        psOut.println("    Full transmission rate (Mbps) : " + this.getTransmissionRateFull());
        psOut.println("    Raw transmission rate (Mbps)  : " + this.getTransmissionRateRaw());
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates and loads a message buffer with the standard evaluation message payload.
     * </p>
     * <p>
     * The buffer is first created so that it accepts the full payload without blocking (no back pressure).  
     * It is then activated so that it accepts <code>IngestDataRequest</code> messages.
     * The payload <code>{@link #LST_MSG_RQSTS}</code> is then offered to the buffer.
     * The buffer is then shut down using <code>{@link IngestionMemoryBuffer#shutdown()}</code> on
     * a separate execution thread <code>{@link #thdSBuffShdn}</code> (shutdown() is a blocking operation).  
     * The buffer will continue to supply messages until exhausted at which time the 
     * <code>{@link IngestionMemoryBuffer#isSupplying()}</code> will return <code>false</code> allowing
     * a normal shutdown of the <code>IngestionChannel</code> instance under test.
     * </p>
     * <p>
     * Note that the method sets the instance attribute <code>{@link #thdSBuffShdn}</code> thread 
     * containing the <code>{@link IngestionMemoryBuffer#shutdown()}</code> operation so that other methods 
     * can block on the shutdown operation using <code>{@link Thread#join()}</code>.
     * If the buffer creation operation fails the returned instance will be <code>null</code>.
     * </p>
     * 
     * @return  fully loaded and activated message buffer, or <code>null</code> if the loading operation failed
     */
    private IngestionMemoryBuffer createAndloadMessageBuffer() {
        
        // Create a new payload (just in case)
        List<IngestDataRequest>     lstMsgRqsts = this.obtainPayload();
        long                        szPayloadAlloc = lstMsgRqsts.stream().mapToLong(msg -> msg.getSerializedSize()).sum();
        
        // Create a message buffer
//        IngestionMemoryBuffer buffer = new IngestionMemoryBuffer(SZ_PAYLOAD_ALLOC, false);
        IngestionMemoryBuffer buffer = new IngestionMemoryBuffer(szPayloadAlloc, false);
        
        try {
            // Load buffer with the payload and activate 
            // - Sends everything all at once - the buffer will unload everything
            buffer.activate();
//            buffer.offer(LST_MSG_RQSTS);
            buffer.offer(lstMsgRqsts);
            
        } catch (IllegalStateException | InterruptedException e) {
            this.recStatus = ResultStatus.newFailure("Message buffer failed to accept payload.", e);
            return null;
        }
        
        // Shutdown buffer on separate thread since shutdown() operation is blocking
        //  NOTE: the buffer will continue to supply until all messages have been consumed
        this.thdSBuffShdn = new Thread(() -> { try { buffer.shutdown(); } catch (Exception e) {} });
        this.thdSBuffShdn.start();
        
        return buffer;
    }
    
    /**
     * <p>
     * Obtains the standard payload of <code>IngestDataRequest</code> messages.
     * </p>
     * <p>
     * This method is returns <code>{@link #LST_MSG_RQSTS}</code> if <code>{@link #BOL_NEW_PAYLOAD}</code> is 
     * <code>false</code> and creates a new payload (of same size) if <code>true</code>.
     * </p>
     * 
     * @return  a standard message payload 
     */
    private List<IngestDataRequest> obtainPayload() {
        
        // If using the class payload
        if (!BOL_NEW_PAYLOAD) {
            return IngestionChannelEvaluator.LST_MSG_RQSTS;
        }
        
        // If using new payload
        List<IngestDataRequest> lstMsgRqsts = TestIngestDataRequestGenerator.createDoublesMessagesWithClock(CNT_FRAMES, CNT_COLS, CNT_ROWS);

        return lstMsgRqsts;
    }
    
    /**
     * <p>
     * Creates and configuration a new <code>IngestionChannel</code> instance connected to the given arguments.
     * </p>
     * <p>
     * A new <code>IngestionChannel</code> instance is created which is attached to the given message supplier
     * (containing the test payload) and the given Ingestion Service connection instance.
     * The instance is then configured according to the configuration in attribute <code>{@link #cfgTest}</code>
     * provided at construction.
     * </p>
     * 
     * @param buffer        (finite) supplier of message payload 
     * @param connIngest    connection to the Ingestion Service
     * 
     * @return  a new <code>IngestionChannel</code> instance ready for activation
     * 
     * @throws IllegalStateException            ingestion channel configured after activation (should not occur)
     * @throws IllegalArgumentException         configuration contained < 1 data stream
     * @throws UnsupportedOperationException    configuration contained illegal stream type
     */
    private IngestionChannel    createAndConfigChannel(IMessageSupplier<IngestDataRequest> buffer, DpIngestionConnection connIngest) 
            throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        
        // Create the ingestion channel with message supplier and Ingestion Service connection
        IngestionChannel    chan = IngestionChannel.from(buffer, connIngest);
        
        // Configure channel
        chan.setStreamType(this.cfgTest.streamType());
        chan.setMultipleStreams(this.cfgTest.streamCount());
        
        return chan;
    }
    
    /**
     * <p>
     * Computes the results of the performance evaluation.
     * </p>
     * <p>
     * Computes the transmission rates for the evaluation payload for an <code>IngestionChannel</code>
     * instance with the configuration provided at construction.  The results are stored in the following
     * attributes:
     * <ul>
     * <li><code>{@link #dblXmitRateRaw}</code> raw transmission rate</li>
     * <li><code>{@link #dblXmitRateFull}</code> full transmission rate including buffer shutdown</li>
     * </ul>
     * </p>
     * 
     * @throws IllegalStateException    the evaluation failed or was never run
     */
    private void computeResults() throws IllegalStateException {
        
        // Check state
        if (this.bolEvaluate == null)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - The evaluation was not run.");
        
        if (this.recStatus.isFailure())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - The evaluation failed.");
        
        // Collect results
        this.durInit = Duration.between(this.insStart, this.insActivate);
        this.durXmit = Duration.between(this.insActivate, this.insFinish);
        this.durBuffShdn = Duration.between(this.insFinish, this.insBuffShdn);
        this.durTotal = Duration.between(this.insStart, this.insBuffShdn);

        // Compute intermediate parameters
        Duration durFull = this.durXmit.plus(this.durBuffShdn);
        
        double  dblDurRaw = ((double)this.durXmit.toNanos()) / 1_000_000_000;
        double  dblDurFull = ((double)durFull.toNanos()) / 1_000_000_000;
        
        // Compute results
        this.dblXmitRateRaw = ((double)IngestionChannelEvaluator.SZ_PAYLOAD_ALLOC) / dblDurRaw;
        this.dblXmitRateFull = ((double)IngestionChannelEvaluator.SZ_PAYLOAD_ALLOC) / dblDurFull;
    }
    
}
