/*
 * Project: dp-jal
 * File:	IngestChanTestCase.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.channel
 * Type: 	IngestChanTestCase
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
 * @since Feb 17, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.channel;

import java.io.PrintStream;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataResponse;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataStreamResponse;
import com.ospreydcs.dp.jal.common.DpGrpcStreamType;
import com.ospreydcs.dp.jal.common.ProviderUID;
import com.ospreydcs.dp.jal.common.ResultStatus;
import com.ospreydcs.dp.jal.config.JalConfig;
import com.ospreydcs.dp.jal.config.ingest.JalIngestionConfig;
import com.ospreydcs.dp.jal.ingest.IngestionFrame;
import com.ospreydcs.dp.jal.ingest.model.frame.IngestionFrameProcessor;
import com.ospreydcs.dp.jal.ingest.model.grpc.IngestionChannel;
import com.ospreydcs.dp.jal.ingest.model.grpc.IngestionMessageBuffer;
import com.ospreydcs.dp.jal.tools.apps.ingest.common.FrameProcessorConfig;
import com.ospreydcs.dp.jal.tools.apps.ingest.common.IngestionChannelConfig;
import com.ospreydcs.dp.jal.tools.apps.ingest.common.MessageConsumerTask;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Record containing evaluation parameters for a <code>IngestionChannelEvaluator</code> test case.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 17, 2026
 *
 * @param   indCase     test case index
 * 
 * @param   cntFrames   number of <code>IngestionFrame</code> instances composing test payload
 * @param   specFrame   specification record for <code>IngestionFrame</code> factory used to create evaluation payload
 * @param   recProcCfg  configuration record for <code>IngestionFrameProcessor</code> instance used in evaluation
 * @param   recChanCfg  configuration record for <code>IngestionChannel</code> instance used in evaluation
 */
public record IngestChanTestCase(
        int                     indCase,
        int                     cntFrames,
        FrameFactorySpec        specFrame,
        FrameProcessorConfig    recProcCfg,
        IngestionChannelConfig  recChanCfg
        ) implements Comparable<IngestChanTestCase>
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>IngestChanTestCase</code> instance from the test parameter values of 
     * application IngestionChannelEvaluator.
     * </p>
     * <p>
     * The value for field <code>{@link #recProcCfg}</code> is populated with all default values taken from the
     * JAL Tools default configuration, except for parameter <code>{@link FrameProcessorConfig#bolColSerEnbl()}</code>
     * which is taken from the arguments.
     * </p>
     * <p>
     * The value for field <code>{@link #recChanCfg}</code> is created from the arguments collection.
     * </p>
     *   
     * @param bolSerEnbl    enable/disable ingestion frame data column serialization before transmission
     * @param enmStrmType   the gRPC data stream type used for transmission {FORWARD, BIDIRECTIONAL}
     * @param bolMStrmEnbl  enable/disable multiple, concurrent gRPC data streams for data transmission
     * @param cntMStrmMax   the maximum number of concurrent gRPC data streams used in data transmission
     * @param cntFrames     number of <code>IngestionFrame</code> instances composing test payload
     * @param specFrame     specification record for <code>IngestionFrame</code> factory used to create evaluation payload
     * 
     * @return  a new <code>IngestChanTestCase</code> instance ready for evaluation
     */
    public static IngestChanTestCase    from(
            boolean             bolSerEnbl, 
            DpGrpcStreamType    enmStrmType, 
            boolean             bolMStrmEnbl, 
            int                 cntMStrmMax, 
            int                 cntFrames, 
            FrameFactorySpec    specFrame
            ) 
    {
        // Create the FrameProcessorConfig record - all fields are default values except bolSerEnbl
        boolean     bolDcmpEnbl = CFG_DEF.decompose.enabled;
        long        szFrameMax = CFG_DEF.decompose.maxSize;
        boolean     bolConcEnbl = CFG_DEF.concurrency.enabled;
        int         cntThrdsMax = CFG_DEF.concurrency.maxThreads;
        
        FrameProcessorConfig    recProcCfg = FrameProcessorConfig.from(bolSerEnbl, bolDcmpEnbl, szFrameMax, bolConcEnbl, cntThrdsMax);
        
        // Create the IngestionChannelConfig record
        IngestionChannelConfig  recChanCfg = IngestionChannelConfig.from(enmStrmType, bolMStrmEnbl, cntMStrmMax);
        
        // Create and return the IngestChanTestCase 
        return IngestChanTestCase.from(cntFrames, specFrame, recProcCfg, recChanCfg);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>IngestChanTestCase</code> instance populated with the given arguments.
     * </p>
     * <p>
     * This creator is defers to the the canonical constructor 
     * <code>{@link #IngestChanTestCase(int, int, FrameFactorySpec, FrameProcessorConfig, IngestionChannelConfig)}</code>
     * where the <code>{@link #indCase()}</code> argument is supplied by record variable <code>{@link #IND_CASE}</code>.
     * The canonical constructor increments <code>{@link #IND_CASE}</code> after every invocation.
     * </p>  
     * 
     * @param   cntFrames   number of <code>IngestionFrame</code> instances composing test payload
     * @param   specFrame   specification record for <code>IngestionFrame</code> factory used to create evaluation payload
     * @param   recProcCfg  configuration record for <code>IngestionFrameProcessor</code> instance used in evaluation
     * @param   recChanCfg  configuration record for <code>IngestionChannel</code> instance used in evaluation
     *     
     * @return  a new <code>IngestChanTestCase</code> instance populated with the given arguments
     */
    public static IngestChanTestCase    from(int cntFrames, FrameFactorySpec specFrame, FrameProcessorConfig recProcCfg, IngestionChannelConfig recChanCfg) {
        return new IngestChanTestCase(IND_CASE, cntFrames, specFrame, recProcCfg, recChanCfg);
    }
    
    
    //
    // Internal Types
    //
    
    /**
     * <p>
     * Record containing the results of the ingestion frame payload creation operation.
     * </p>
     * <p>
     * The record fields contain the ingestion frame payload and properties for this test case.
     * This record is used internally by <code>IngestChanTestCase</code> for the ingestion
     * channel evaluations.
     * Instances of this record are produced by the create payload operation and are
     * then used for further evaluation of the <code>IngestionChannel</code> under evaluation.
     * </p>
     *
     * @param recStatus the result status of the payload creation operation
     * @param cntFrames the number of ingestion frames in the payload
     * @param szPayload the memory allocation size of all ingestion frames in the payload
     * @param lstFrames the order list of payload ingestion frames
     */
    public static record PayloadCreateResult(
            ResultStatus            recStatus,
            int                     cntFrames,
            long                    szPayload,
            List<IngestionFrame>    lstFrames
            )
    {
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates and returns a new <code>PayloadCreateResult</code> instance for a failed operation.
         * </p>
         * <p>
         * This is a convenience creator for a failed payload creation operation.
         * All fields except the <code>{@link #recStatus()}</code> field are empty or zero.
         * </p>
         * 
         * @param recFail   status record containing the specifics of the failure
         * 
         * @return  a new <code>PayloadCreateResult</code> record for a failed payload creation operation
         * 
         * @throws IllegalArgumentException the given status record indicates success
         */
        public static PayloadCreateResult   from(ResultStatus recFail) throws IllegalArgumentException {
            if (recFail.isSuccess())
                throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Status record indicates success.");
                
            return PayloadCreateResult.from(recFail, 0, 0L, List.of());
        }
        
        /**
         * <p>
         * Creates and returns a new <code>PayloadCreateResult</code> populated with the given argument values.
         * </p>
         * <p>
         * This creator is equivalent to the canonical constructor.
         * </p>
         * 
         * @param recStatus the result status of the payload creation operation
         * @param cntFrames the number of ingestion frames in the payload
         * @param szPayload the memory allocation size of all ingestion frames in the payload
         * @param lstFrames the order list of payload ingestion frames
         * 
         * @return  a new <code>PayloadCreateResult</code> instance containing the payload for this test case
         */
        public static PayloadCreateResult   from(ResultStatus recStatus, int cntFrames, long szPayload, List<IngestionFrame> lstFrames) {
            return new PayloadCreateResult(recStatus, cntFrames, szPayload, lstFrames);
        }
    }
    
    /**
     * <p>
     * Record containing the results of an ingestion frame processing operation.
     * </p>
     * <p>
     * This record is used internally by <code>IngestChanTestCase</code> for the ingestion
     * channel evaluations.
     * Instances of this record are produced by the process payload operation and are
     * then used for the evaluation of the <code>IngestionChannel</code> under evaluation.
     * </p>
     *
     * @param recStatus     result status of the payload processing operation
     * @param cntMsgsProc   the number of data messages produced during processing
     * @param szAllocProc   the total memory allocation of the processed data messages
     * @param durProcess    processing time for the ingestion frame payload
     * @param dblRateProc   processing rate (in MBps) for the ingestion frame payload
     * @param lstMsgs       the collection of data messages produced from processing the payload
     */
    public static record PayloadProcessResult(
            ResultStatus    recStatus,
            int             cntMsgsProc,
            long            szAllocProc,
            Duration        durProcess,
            double          dblRateProc,
            List<IngestDataRequest> lstMsgs
            ) 
    {
        // 
        // Creators
        //
        
        /**
         * <p>
         * Creates and returns a new <code>PayloadProcessResult</code> instance for a failed operation.
         * </p>
         * <p>
         * This is a convenience creator for a failed payload processing operation.
         * All fields except the <code>{@link #recStatus()}</code> field are empty or zero.
         * </p>
         * 
         * @param recFail   status record containing the specifics of the failure
         * 
         * @return  a new <code>PayloadProcessResult</code> record for a failed payload processing operation
         * 
         * @throws IllegalArgumentException the given status record indicates success
         */
        public static PayloadProcessResult from(ResultStatus recFail) throws IllegalArgumentException {
            if (recFail.isSuccess())
                throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Status record indicates success.");
                
            return PayloadProcessResult.from(recFail, 0, 0L, Duration.ZERO, 0.0, List.of());
        }
        
        /**
         * <p>
         * Creates and returns a new <code>PayloadProcessResult</code> instance populated with the given arguments.
         * </p>
         * <p>
         * This creator is equivalent to the canonical constructor.
         * </p>
         * 
         * @param recStatus     result status of the payload processing operation
         * @param cntMsgsProc   the number of data messages produced during processing
         * @param szAllocProc   the total memory allocation of the processed data messages
         * @param durProcess    processing time for the ingestion frame payload
         * @param dblRateProc   processing rate (in MBps) for the ingestion frame payload
         * @param lstMsgs       the collection of data messages produced from processing the payload
         * 
         * @return  a new <code>PayloadProcessResult</code> instance containing the results of the payload processing
         */
        public static PayloadProcessResult    from(
                ResultStatus    recStatus,
                int             cntMsgsProc,
                long            szAllocProc,
                Duration        durProcess,
                double          dblRateProc,
                List<IngestDataRequest> lstMsgs
                ) 
        {
            return new PayloadProcessResult(recStatus, cntMsgsProc, szAllocProc, durProcess, dblRateProc, lstMsgs);
        }
        
    }

    
    //
    // Library Resources
    //
    
    /** The JAL ingest API default configuration parameters */
    private static final JalIngestionConfig     CFG_DEF = JalConfig.getInstance().ingest;
    
    
    //
    // Record Resources
    //
    
    /** The data provider UID used in the ingestion frame processor */
    private static final ProviderUID                UID_PROVIDER = ProviderUID.from(UUID.randomUUID().toString(), IngestChanTestCase.class.getSimpleName(), false);
    
    /** The ingestion frame processor used in evaluations, converts ingest frame payload into data messages */
    private static final IngestionFrameProcessor    PROC_FRAMES = IngestionFrameProcessor.from(UID_PROVIDER);

    
    //
    // Record Variables
    //
    
    /** Running index of test case - incremented upon creation/construction */
    private static int  IND_CASE = 1;

    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>IngestChanTestCase</code> instance.
     * </p>
     * <p>
     * Canonical constructor. Sets field values then increments record index counter <code>{@link #IND_CASE}</code>
     * </p>
     *
     * @param   indCase     test case index
     * 
     * @param   cntFrames   number of <code>IngestionFrame</code> instances composing test payload
     * @param   specFrame   specification record for <code>IngestionFrame</code> factory
     * @param   recProcCfg  configuration record for <code>IngestionFrameProcessor</code> instance
     * @param   recChanCfg  configuration record for <code>IngestionChannel</code> instance    
     */
    public IngestChanTestCase {
        IND_CASE++;
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
        if (obj instanceof IngestChanTestCase rec) {
            boolean bolResult = (this.indCase == rec.indCase)
                             && (this.cntFrames == rec.cntFrames)
                             && (this.specFrame.equals(rec.specFrame))
                             && (this.recProcCfg.equals(rec.recProcCfg))
                             && (this.recChanCfg.equals(rec.recChanCfg));
            
            return bolResult;
        }
        
        return false;
    }


    //
    // Tools
    //
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> providing a natural (ascending) ordering according to the test case index.
     * </p>
     * <p>
     * The returned comparator instance compares the index field within two
     * <code>IngestChanTestCase</code> records, specifically the <code>{@link IngestChanTestCase#indCase()}</code> fields.
     * The natural ordering of the index values is applied; specifically, the lowest index will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned comparator <b>returns 0</b> if the two index fields are equal.
     * In the current implementation the <code>{@link #indCase()}</code> field will always be unique for each
     * <code>IngestChanTestCase</code> instance.  Thus, the only possibility of equal index fields is equal
     * record instances.  Thus, the same test case can never appear in a Java collection asserting uniqueness.   
     * </p>
     * 
     * @return  a new <code>Comparator</code> instance providing a natural ordering by test case index
     */
    public static Comparator<IngestChanTestCase>   caseIndexOrdering() {
        
        Comparator<IngestChanTestCase> cmp = (r1, r2) -> {
            if (r1.indCase() < r2.indCase())
                return -1;
            else if (r1.indCase == r2.indCase)
                return 0;
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
     * Evaluates the given <code>IngestionChannel</code> instance according to the parameters of this test case.
     * </p>
     * <p>
     * Requires the given <code>IngestionFrameProcessor</code> for conversion of test payload into 
     * <code>IngestDataRequest</code> messages used in <code>IngestionChannel</code> evaluation.
     * </p> 
     * 
     * @param bufMsgs   the ingestion request data message buffer attached to the given ingestion channel
     * @param chanTest  ingestion channel under evaluation
     * 
     * @return  a record containing the results of the evaluations
     */
    public IngestChanTestResult    evaluate(IngestionMessageBuffer bufMsgs, IngestionChannel chanTest)  {
        
        // Create the test payload a priori
        PayloadCreateResult recPayload = this.createPayload();
        
        if (recPayload.recStatus.isFailure())
            return IngestChanTestResult.from(recPayload.recStatus, this);
        
        // Perform the payload processing
        PayloadProcessResult    recProcess = this.processPayload(recPayload.lstFrames);
        
        if (recProcess.recStatus.isFailure())
            return IngestChanTestResult.from(recProcess.recStatus, this);

        // Evaluate the ingestion channel
        this.recChanCfg.configure(chanTest);
        
        Instant     insStart;
        Instant     insFinish;
        try {
            
            // Activate buffer and channel - prepare for ingestion
            bufMsgs.activate();
            chanTest.activate();        // throws IllegalStateException, UnsupportedOperationException, RejectedExecutionException
            
            // Perform the data transmission - load the data message buffer
            insStart = Instant.now();
            bufMsgs.offer(recProcess.lstMsgs);  // throws IllegalStateException, IterruptedException
            bufMsgs.shutdown();                 // throws InterruptedException
            chanTest.shutdown();                // throws InterruptedException
            insFinish = Instant.now();

        } catch (Exception e) {
            bufMsgs.shutdownNow();
            chanTest.shutdownNow();
            
            ResultStatus    recFail = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Ingestion channel evaluation failure (see cause).", e);
            
            return IngestChanTestResult.from(recFail, this);
        }
        
        // Collect the all results and return
        int         cntFrames = recPayload.cntFrames;
        long        szPayload = recPayload.szPayload;
        
        Duration    durProcess = recProcess.durProcess;
        double      dblRateProc = recProcess.dblRateProc;
        
        Duration    durTransmit = Duration.between(insStart, insFinish);
        int         cntMsgsXmit = recProcess.cntMsgsProc;
        long        szAllocXmit = recProcess.szAllocProc;
        double      dblRateXmit = ((double)(szAllocXmit * 1_000))/durTransmit.toNanos(); // rate in MBps
        
        List<IngestDataStreamResponse>  lstUniRsps = chanTest.getIngestionUniResponses();
        List<IngestDataResponse>        lstBidiRsps = chanTest.getIngestionBidiResponses();
        
        IngestChanTestResult    recResult = IngestChanTestResult.from(ResultStatus.SUCCESS, cntFrames, szPayload, durProcess, dblRateProc, durTransmit, cntMsgsXmit, szAllocXmit, dblRateXmit, lstUniRsps, lstBidiRsps, this);

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
        String strPadd = strPad + "  ";
        String strPaddd = strPadd + "  ";
        
        ps.println(strPad + this.getClass().getSimpleName() + " " + this.indCase);
        ps.println(strPadd + "Payload Configuraiton");
        ps.println(strPaddd + "Ingestion frame count      : " + this.cntFrames);
        this.specFrame.printOut(ps, strPaddd);
        
        ps.println(strPadd + "IngestionFrameProcessor Configuration");
        this.recProcCfg.printOut(ps, strPaddd);
        
        ps.println(strPadd + "IngestionChannel Configuration");
        this.recChanCfg.printOut(ps, strPaddd);
    }
    
    
    //
    // Comparable<IngestChanTestCase> Interface
    //
    
    /**
     * <p>
     * Provides a comparison of <code>IngestChanTestCase</code> records based upon the test case index.
     * </p>
     * <p>
     * This comparison provides a natural ordering of <code>IngestChanTestCase</code> records by test case
     * index <code>{@link #indCase()}</code>.  Specifically, records with the lowest index will appear first
     * in any ordering.  No other fields are compared.
     * Records with equal <code>{@link #indCase()}</code> fields will return zero.
     * </p> 
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(IngestChanTestCase recCase) {
        
        if (this.indCase < recCase.indCase)
            return -1;
        else if (this.indCase == recCase.indCase)
            return 0;
        else
            return +1;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates and returns the <code>IngestionFrame</code> payload for the supplier evaluation.
     * </p>
     * <p>
     * Instantiates a new <code>IngestionFrameGeneratorDeprecated</code> object configured for <code>IngestionGrame</code>
     * building with the <code>{@link #recFrmCfg}</code> field.  Then <code>{@link #cntFrames}</code> 
     * <code>IngestionFrame</code> objects are built and used to populate the returned collection.
     * </p>
     * 
     * @return  a new payload of ingestion frames according to the frame count and frame generator configuration
     *  
     * @throws IllegalArgumentException     the sample count and/or the sample period was non-positive 
     * @throws IllegalStateException        there are no data columns defined in frame factory
     * @throws DateTimeException            internal <code>Instant</code> addition failed (timestamps)
     * @throws ArithmeticException          numeric overflow in <code>Instant</code> addition (timestamps)
     * @throws UnsupportedOperationException an unsupported timestamp case was encountered
     */
    private PayloadCreateResult    createPayload() /* throws IllegalArgumentException, IllegalStateException, DateTimeException, ArithmeticException, UnsupportedOperationException */ {

        // Creates frame factory and container for generated ingestion frames
        IFrameFactory           facFrames = this.specFrame.newFactory();
        List<IngestionFrame>    lstFrames = new ArrayList<>(this.cntFrames);
        
        try {
            for (int iFrm=0; iFrm<this.cntFrames; iFrm++) {
                IngestionFrame  frm = facFrames.nextFrame();    // throws IllegalArgumentException, IllegalStateException, DateTimeException, ArithmeticException, UnsupportedOperationException

                lstFrames.add(frm);

            }
            
        } catch (Exception e) {
            ResultStatus    recFail = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Failed to created payload.", e);
            
            return PayloadCreateResult.from(recFail);
        }

        // Collect results and return
        ResultStatus    recStatus = ResultStatus.SUCCESS;
        int             cntFrames = lstFrames.size();
        long            szAllocRaw = lstFrames.stream().mapToLong(frm -> frm.allocationSizeFrame()).sum();

        return PayloadCreateResult.from(recStatus, cntFrames, szAllocRaw, lstFrames);
    }
    
    /**
     * <p>
     * Performs the processing of the test case ingestion frame payload into data messages for further processing.
     * </p>
     * <p>
     * The method uses the <code>{@link #PROC_FRAMES}</code> static instance to process the given argument
     * collection into the collection of <code>IngestDataRequest</code> data messages contained in the
     * returned result record.  The data messages are then used for further processing by the <code>IngestionChannel</code>
     * instance under evaluation.
     * </p>
     *  
     * @param lstFrames the ingestion frame payload for the current test case
     * 
     * @return  the results of processing the ingestion frame payload into <code>IngestDataRequest</code> message for further processing
     */
    private PayloadProcessResult processPayload(List<IngestionFrame> lstFrames) {
        
        // Configure processor and message consumer task
        this.recProcCfg.configure(PROC_FRAMES);
        MessageConsumerTask thrdMsgSnk = MessageConsumerTask.from(PROC_FRAMES, true);
        
        // Initialize the processor and consumer task 
        try {
            PROC_FRAMES.activate();       // throws RejectedExecutionException
            thrdMsgSnk.start();          // throws IllegalThreadStateException
        
        } catch (RejectedExecutionException e) {
            PROC_FRAMES.shutdownNow();
            
            ResultStatus    recFail = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Process activation failed.", e);
            
            return PayloadProcessResult.from(recFail);
            
        } catch (IllegalThreadStateException e) {
            PROC_FRAMES.shutdownNow();
            thrdMsgSnk.terminate();

            ResultStatus    recFail = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Message consumer task already started.", e);
            
            return PayloadProcessResult.from(recFail);
        }
        
        // Perform the payload processing
        Instant     insStart;
        Instant     insFinish;
        try {
            insStart = Instant.now();
            PROC_FRAMES.submit(lstFrames);                // throws IllegalStateException
            PROC_FRAMES.shutdown();                       // throws InterruptedException
            thrdMsgSnk.join();                          // throws InterruptedException
            insFinish = Instant.now();
            
        } catch (Exception e) {
            PROC_FRAMES.shutdownNow();
            thrdMsgSnk.terminate();

            ResultStatus    recFail = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Processor evaluation failure during execution.", e);
            
            return PayloadProcessResult.from(recFail);
        }

        // Check for processing failure
        ResultStatus    recProcStatus = thrdMsgSnk.getResult();
        if (recProcStatus.isFailure())
            return PayloadProcessResult.from(recProcStatus);
        
        
        // Collect processing results 
        int             cntMsgs = thrdMsgSnk.getMessageCount();
        long            szAllocProc = thrdMsgSnk.getAllocation();
        Duration        durProcess = Duration.between(insStart, insFinish);
        double          dblRateProc = ((double)(szAllocProc * 1_000))/durProcess.toNanos(); // rate in MBps

        List<IngestDataRequest> lstMsgs = thrdMsgSnk.getRecoveredMessages();
        
        // Populate result record and return
        return PayloadProcessResult.from(ResultStatus.SUCCESS, cntMsgs, szAllocProc, durProcess, dblRateProc, lstMsgs);
    }
    
}