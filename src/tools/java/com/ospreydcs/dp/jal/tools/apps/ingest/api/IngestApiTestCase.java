/*
 * Project: dp-jal
 * File:	IngestApiTestCase.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.api
 * Type: 	IngestApiTestCase
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
 * @since Mar 4, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.api;

import java.io.PrintStream;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.common.DpGrpcStreamType;
import com.ospreydcs.dp.jal.common.IngestionResult;
import com.ospreydcs.dp.jal.common.ProviderRegistrar;
import com.ospreydcs.dp.jal.common.ProviderUID;
import com.ospreydcs.dp.jal.common.ResultStatus;
import com.ospreydcs.dp.jal.ingest.IIngestionService;
import com.ospreydcs.dp.jal.ingest.IIngestionStream;
import com.ospreydcs.dp.jal.ingest.IngestionFrame;
import com.ospreydcs.dp.jal.ingest.impl.JalIngestionServiceImpl;
import com.ospreydcs.dp.jal.ingest.impl.JalIngestionStreamImpl;
import com.ospreydcs.dp.jal.tools.apps.ingest.common.FrameProcessorConfig;
import com.ospreydcs.dp.jal.tools.apps.ingest.common.IngestionChannelConfig;
import com.ospreydcs.dp.jal.tools.apps.ingest.common.JalIngestionApiType;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Record containing evaluation parameters for a <code>IngestionApiEvaluator</code> test case.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 4, 2026
 *
 * @param   indCase     test case index
 * 
 * @param   enmApiType      the Ingestion Service API used in evaluations
 * @param   recProcCfg  configuration record for <code>IngestionFrameProcessor</code> instance
 * @param   recChanCfg  configuration record for <code>IngestionChannel</code> instance    
 * @param   cntFrames   number of <code>IngestionFrame</code> instances composing test payload
 * @param   specFrame   specification record for <code>IngestionFrame</code> factory
 */
public record IngestApiTestCase(
        int                     indCase,
        JalIngestionApiType     enmApiType,
        FrameProcessorConfig    recProcCfg,
        IngestionChannelConfig  recChanCfg,
        int                     cntFrames,
        FrameFactorySpec        specFrame
        ) implements Comparable<IngestApiTestCase>
{

    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>IngestApiTestCase</code> instance from the given arguments.
     * </p>
     * <p>
     * All fields of the internal records <code>{@link FrameProcessorConfig}</code> and 
     * <code>{@link IngestionChannelConfig}</code> are included in the given arguments.
     * The records are created and contained within the returned instance.
     * </p>
     * 
     * @param enmApiType    the Ingestion Service API used in evaluation
     * @param bolColSerEnbl enable/disable data column serialization
     * @param bolDcmpEnbl   enable/disable ingestion frame decomposition
     * @param szDcmpMax     maximum composite ingestion frame size (in bytes)
     * @param bolMThrdEnbl  enable/disable multi-threaded processing of ingestion frames
     * @param cntMThrdMax   maximum number of concurrent ingestion frame processing threads
     * @param enmStrmType   Ingestion Service gRPC stream type (when streaming API is used)
     * @param bolMStrmEnbl  enable/disable multiple gRPC data streams (when streaming API is used)
     * @param cntMStrmMax   maximum number of concurrent gRPC data streams (when streaming API is used)
     * @param cntFrames     number of payload ingestion frames
     * @param specFrame     payload ingestion frame specification
     * 
     * @return  a new <code>IngestApiTestCase</code> instance populated with the given arguments
     */
    public static IngestApiTestCase from(
            JalIngestionApiType     enmApiType,
            
            boolean                 bolColSerEnbl, 
            boolean                 bolDcmpEnbl,
            long                    szDcmpMax,
            boolean                 bolMThrdEnbl,
            int                     cntMThrdMax,
            
            DpGrpcStreamType        enmStrmType, 
            boolean                 bolMStrmEnbl, 
            int                     cntMStrmMax, 
            
            int                     cntFrames, 
            FrameFactorySpec        specFrame
            ) 
    {
        
        // Create internal records
        FrameProcessorConfig    recProcCfg = FrameProcessorConfig.from(bolColSerEnbl, bolDcmpEnbl, szDcmpMax, bolMThrdEnbl, cntMThrdMax);
        IngestionChannelConfig  recChanCfg = IngestionChannelConfig.from(enmStrmType, bolMStrmEnbl, cntMStrmMax);
        
        return IngestApiTestCase.from(enmApiType, recProcCfg, recChanCfg, cntFrames, specFrame);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>IngestApiTestCase</code> instance populated with the given arguments.
     * </p>
     * <p>
     * This creator is defers to the the canonical constructor 
     * where the <code>{@link #indCase()}</code> argument is supplied by record variable <code>{@link #IND_CASE}</code>.
     * The canonical constructor increments <code>{@link #IND_CASE}</code> after every invocation.
     * </p>  
     * 
     * @param   enmApiType  the Ingestion Service API used in evaluations
     * @param   recProcCfg  configuration record for <code>IngestionFrameProcessor</code> instance used in evaluation
     * @param   recChanCfg  configuration record for <code>IngestionChannel</code> instance used in evaluation
     * @param   cntFrames   number of <code>IngestionFrame</code> instances composing test payload
     * @param   specFrame   specification record for <code>IngestionFrame</code> factory used to create evaluation payload
     *     
     * @return  a new <code>IngestApiTestCase</code> instance populated with the given arguments
     */
    public static IngestApiTestCase from(
            JalIngestionApiType     enmApiType,
            FrameProcessorConfig    recProcCfg,
            IngestionChannelConfig  recChanCfg,
            int                     cntFrames,
            FrameFactorySpec        specFrame
            )
    {
        return new IngestApiTestCase(IND_CASE, enmApiType, recProcCfg, recChanCfg, cntFrames, specFrame);
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
        // Internal Types
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

    
    //
    // Tools
    //
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> providing a natural (ascending) ordering according to the test case index.
     * </p>
     * <p>
     * The returned comparator instance compares the index field within two
     * <code>IngestApiTestCase</code> records, specifically the <code>{@link IngestApiTestCase#indCase()}</code> fields.
     * The natural ordering of the index values is applied; specifically, the lowest index will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned comparator <b>returns 0</b> if the two index fields are equal.
     * In the current implementation the <code>{@link #indCase()}</code> field will always be unique for each
     * <code>IngestApiTestCase</code> instance.  Thus, the only possibility of equal index fields is equal
     * record instances.  Thus, the same test case can never appear in a Java collection asserting uniqueness.   
     * </p>
     * 
     * @return  a new <code>Comparator</code> instance providing a natural ordering by test case index
     */
    public static Comparator<IngestApiTestCase>   caseIndexOrdering() {
        
        Comparator<IngestApiTestCase> cmp = (r1, r2) -> {
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
    // Record Resources
    //
    
    /** The data provider name used for ingestion API data provider registration */
    private static final String                 STR_PROVIDER_NAME = IngestApiTestCase.class.getSimpleName();
    
    /** The data provider attributes used for ingestion API data provider registration */
    private static final Map<String, String>    MAP_PROVIDER_ATTRS = Map.of(
                                                                        "Source", "IngestionApiEvaluator",
                                                                        "Type", "Application",
                                                                        "Experiment", "Performance Evaluation"
                                                                        );
    
    /** The data provider registration record used for ingestion API data provider registration */
    public static final ProviderRegistrar      REC_PROVIDER_REG = ProviderRegistrar.from(STR_PROVIDER_NAME, MAP_PROVIDER_ATTRS);
    
    
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
     * Constructs a new <code>IngestApiTestCase</code> instance.
     * </p>
     * <p>
     * Canonical constructor. Sets field values then increments record index counter <code>{@link #IND_CASE}</code>
     * </p>
     *
     * @param   indCase     test case index
     * 
     * @param   enmApiType      the Ingestion Service API used in evaluations
     * @param   cntFrames   number of <code>IngestionFrame</code> instances composing test payload
     * @param   specFrame   specification record for <code>IngestionFrame</code> factory
     * @param   recProcCfg  configuration record for <code>IngestionFrameProcessor</code> instance
     * @param   recChanCfg  configuration record for <code>IngestionChannel</code> instance    
     */
    public IngestApiTestCase {
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
     * The argument is first confirmed to by of type <code>IngestApiTestCase</code>. If so all its field
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
        if (obj instanceof IngestApiTestCase rec) {
            boolean bolResult = (this.indCase == rec.indCase)
                             && (this.enmApiType == rec.enmApiType)
                             && (this.cntFrames == rec.cntFrames)
                             && (this.specFrame.equals(rec.specFrame))
                             && (this.recProcCfg.equals(rec.recProcCfg))
                             && (this.recChanCfg.equals(rec.recChanCfg));
            
            return bolResult;
        }
        
        return false;
    }

    
    //
    // Comparable Interface
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
    public int compareTo(IngestApiTestCase recCase) {
        
        if (this.indCase < recCase.indCase)
            return -1;
        else if (this.indCase == recCase.indCase)
            return 0;
        else
            return +1;
    }
    

    //
    // Operations
    //
    
    /**
     * <p>
     * Evaluates the given Ingestion Service APIs according to the parameters of this test case.
     * </p>
     * <p>
     * The Ingestion Service API used depends upon the value of field <code>{@link #enmApiType()}</code>.
     * <ul>
     * <li><code>{@link JalIngestionApiType#UNARY</code> &rarr; <code>apiService</code>.</li>
     * <li><code>{@link JalIngestionApiType#STREAM</code> &rarr; <code>apiStream</code>.</li>
     * </ul>
     * </p> 
     * 
     * @param apiService    unary RPC API to the Ingestion Service
     * @param apiStream     streaming gRPC API to the Ingestion Service
     * 
     * @return  a record containing the results of the evaluations
     */
    public IngestApiTestResult  evaluate(IIngestionService apiService, IIngestionStream apiStream) {
        
        if (this.enmApiType == JalIngestionApiType.UNARY)
            return this.evaluateService(apiService);
        else
            return this.evaluateStream(apiStream);
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
        ps.println(strPadd + "Ingestion Service API      : " + this.enmApiType);
        
        ps.println(strPadd + "IngestionFrameProcessor Configuration");
        this.recProcCfg.printOut(ps, strPaddd);
        
        ps.println(strPadd + "IngestionChannel Configuration");
        this.recChanCfg.printOut(ps, strPaddd);
        
        ps.println(strPadd + "Payload Configuraiton");
        ps.println(strPaddd + "Ingestion frame count      : " + this.cntFrames);
        this.specFrame.printOut(ps, strPaddd);
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Performs the test case evaluation on the given unary Ingestion API for the given payload.
     * </p>
     * <p>
     * The evaluation consists of the following steps:
     * <ol>
     * <li>The evaluation payload is created with <code>{@link #createPayload()}</code>.</li>
     * <li>The Ingestion Service API is configured with <code>{@link #configure(IIngestionService)}</code>.</li>
     * <li>Data Provider registration is performed with <code>{@link #REC_PROVIDER_REG}</code>.</li>
     * <li>Payload ingestion frame are transmitted one-by-one and the ingestion results are recorded.</li>
     * <li>The evaluation results are collected and returned.</li>
     * </ol>
     * If any of the above operations fails a failed <code>IngestApiTestResult</code> is returned with a
     * failure message and cause within the <code>{@link IngestApiTestResult#recTestStatus()}</code> field.
     * </p>
     * 
     * @param recPayload    the ingestion frame payload used for the evaluation measurement
     * @param apiService    the target Ingestion API to be evaluated
     * 
     * @return  the results of the test case evaluation on the given Ingestion Service API (SUCCESS or failure)
     */
    private IngestApiTestResult evaluateService(IIngestionService apiService) {
        
        // Create payload and check result
        PayloadCreateResult recPayload = this.createPayload();
        
        if (recPayload.recStatus.isFailure())
            return IngestApiTestResult.from(recPayload.recStatus, this);
        
        // Configure API implementation
        try {
            this.configure(apiService);

        } catch (Exception e) {
            ResultStatus    recStatus = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Ingestion API configuration failed.", e);

            return IngestApiTestResult.from(recStatus, this);
        }
        
        // Register as data provider
        ProviderUID     recProvUid;
        try {
            recProvUid = apiService.registerProvider(REC_PROVIDER_REG);

        } catch (Exception e) {
            ResultStatus    recStatus = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Data provider registration failed.", e);

            return IngestApiTestResult.from(recStatus, this);
        }
        
        // Perform payload transmission and measure
        final int   cntFrames = recPayload.cntFrames;
        int         indFrame = 1;
        Instant     insStart;
        Instant     insFinish;
        List<IngestionResult>   lstResults = new LinkedList<>();
        try {
            insStart = Instant.now();
            for (IngestionFrame frm : recPayload.lstFrames) {
                IngestionResult recResult = apiService.ingest(frm);
                
                lstResults.add(recResult);
                indFrame++;
            }
            insFinish = Instant.now();
            
        } catch (Exception e) {
            ResultStatus    recStatus = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() 
                                      + " - Unary data ingestion failure for frame #" + indFrame + " of " + cntFrames + ".", e);

            return IngestApiTestResult.from(recStatus, this);
        }
        
        // Collect results and return
        long        szPayload = recPayload.szPayload;
        Duration    durTransmit = Duration.between(insStart, insFinish);
        int         cntMsgsXmit = lstResults.stream().mapToInt(rec -> rec.transmitRequestCount()).sum();
        double      dblRateXmit = ((double)(szPayload * 1_000))/durTransmit.toNanos();
        
        return IngestApiTestResult.from(ResultStatus.SUCCESS, cntFrames, szPayload, durTransmit, cntMsgsXmit, dblRateXmit, recProvUid, lstResults, this);
    }
    
    /**
     * <p>
     * Performs the test case evaluation on the given streaming Ingestion API for the given payload.
     * </p>
     * <p>
     * The evaluation consists of the following steps:
     * <ol>
     * <li>The evaluation payload is created with <code>{@link #createPayload()}</code>.</li>
     * <li>The Ingestion Service API is configured with <code>{@link #configure(IIngestionStream)}</code>.</li>
     * <li>The Ingestion Service stream is opened using the <code>{@link #REC_PROVIDER_REG}</code> provider registration.</li>
     * <li>The payload ingestion frames are transmitted in bulk while blocking on <code>{@link IIngestionStream#awaitQueueEmpty()}</code>.</li>
     * <li>The Ingestion Service stream is closed and the ingestion result recorded.</li>
     * <li>The evaluation results are collected and returned.</li>
     * </ol>
     * If any of the above operations fails a failed <code>IngestApiTestResult</code> is returned with a
     * failure message and cause within the <code>{@link IngestApiTestResult#recTestStatus()}</code> field.
     * </p>
     * 
     * @param recPayload    the ingestion frame payload used for the evaluation measurement
     * @param apiStream     the target Ingestion API to be evaluated
     * 
     * @return  the results of the test case evaluation on the given Ingestion Service API (SUCCESS or failure)
     */
    private IngestApiTestResult evaluateStream(IIngestionStream apiStream) {
        
        // Create payload and check result
        PayloadCreateResult recPayload = this.createPayload();
        
        if (recPayload.recStatus.isFailure())
            return IngestApiTestResult.from(recPayload.recStatus, this);
        
        // Configure API implementation
        try {
            this.configure(apiStream);
        
        } catch (Exception e) {
            ResultStatus    recStatus = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Ingestion API configuration failed.", e);

            return IngestApiTestResult.from(recStatus, this);
        }
        
        // Open data stream
        ProviderUID     recProvUid;
        try {
            recProvUid = apiStream.openStream(REC_PROVIDER_REG);
            
        } catch (Exception e) {
            ResultStatus    recStatus = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Data stream open failed.", e);

            return IngestApiTestResult.from(recStatus, this);
        }
        
        // Perform payload transmission and measure
        // - closeStream() blocks until transmission complete
        Instant         insStart;
        Instant         insFinish;
        IngestionResult recIngResult; 
        try {
            insStart = Instant.now();
            apiStream.ingest(recPayload.lstFrames); // throws IllegalStateException, InterruptedException, JalIngestionException
            recIngResult = apiStream.closeStream(); // throws IllegalStateException, InterruptedException, CompletionException, MissingResourceException
            insFinish = Instant.now();
            
        } catch (Exception e) {
            apiStream.closeStreamNow();
            
            ResultStatus    recStatus = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Streaming data ingestion failure.", e);

            return IngestApiTestResult.from(recStatus, this);
        }
        
//        // Close stream and collect results
//        try {
//            recIngResult = apiStream.closeStream();
//            
//        } catch (Exception e) {
//            ResultStatus    recStatus = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - Streaming API closure failed.", e);
//
//            return IngestApiTestResult.from(recStatus, this);
//        }
        
        // Collect results and return
        int         cntFrames = recPayload.cntFrames;
        long        szPayload = recPayload.szPayload;
        Duration    durTransmit = Duration.between(insStart, insFinish);
        int         cntMsgsXmit = recIngResult.transmitRequestCount();
        double      dblRateXmit = ((double)(szPayload * 1_000))/durTransmit.toNanos();
        
        return IngestApiTestResult.from(ResultStatus.SUCCESS, cntFrames, szPayload, durTransmit, cntMsgsXmit, dblRateXmit, recProvUid, List.of(recIngResult), this);
    }
    
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
        long            szPayload = lstFrames.stream().mapToLong(frm -> frm.allocationSizeFrame()).sum();

        return PayloadCreateResult.from(recStatus, cntFrames, szPayload, lstFrames);
    }
    
    /**
     * <p>
     * Configures the given Ingestion Service interface to the conditions of this test case.
     * </p>
     * <p>
     * The given interface is cast to implementation <code>{@link JalIngestionServiceImpl}</code> to
     * access the configuration methods.  The ingestion frame processor configuration within field
     * <code>{@link #recProcCfg()}</code> is then used for configuration of the given interface.
     * </p>
     * 
     * @param apiService    an unary Ingestion Service API interface
     * 
     * @throws ClassCastException   the interface implementation was not a <code>JalIngestionServiceImpl</code> instance
     */
    private void    configure(IIngestionService apiService) throws ClassCastException {
        
        // Cast the interface to its implementation 
        JalIngestionServiceImpl     implService = JalIngestionServiceImpl.class.cast(apiService);   //throws ClassCastException
        
        // Configure the interface implementation
        implService.enableDataColumnSerialization(this.recProcCfg.bolColSerEnbl());
        
        if (this.recProcCfg.bolDcmpEnbl())
            implService.enableFrameDecomposition(this.recProcCfg.szDcmpMax());
        else
            implService.disableFrameDecomposition();
    }
    
    /**
     * <p>
     * Configures the given Ingestion Service interface to the conditions of this test case.
     * </p>
     * <p>
     * The given interface is cast to implementation <code>{@link JalIngestionStreamImpl}</code> to
     * access the configuration methods.  The configuration parameters within fields
     * <code>{@link #recProcCfg()}</code> and <code>{@link #recChanCfg()}</code> are then used for 
     * configuration of the given interface.
     * </p>
     * 
     * @param apiStream     a streaming Ingestion Service API interface
     * 
     * @throws ClassCastException       the interface implementation was not a <code>JalIngestionStreamImpl</code> instance
     * @throws IllegalStateException    the interface stream is opened
     * @throws IllegalArgumentException the number of multiple gRPC streams was less than or equal to 0
     * @throws ConfigurationException   an unsupported gRPC stream type was provided
     */
    private void    configure(IIngestionStream apiStream) throws ClassCastException, IllegalStateException, IllegalArgumentException, ConfigurationException {
        
        // Cast the interface to its implementation
        JalIngestionStreamImpl      implStream = JalIngestionStreamImpl.class.cast(apiStream);
        
        // Configure the interface implementation
        // - Frame processing configuration
        if (this.recProcCfg.bolColSerEnbl())
            implStream.enableDataColumnSerialization();
        else
            implStream.disableDataColumnSerialization();
        
        if (this.recProcCfg.bolDcmpEnbl())
            implStream.setFrameDecomposition(this.recProcCfg.szDcmpMax());
        else
            implStream.disableFrameDecomposition();
        
        if (this.recProcCfg.bolMThrdEnbl())
            implStream.setFrameProcessingConcurrency(this.recProcCfg.cntMThrdMax());    // throws IllegalStateException
        
        // - Channel configuration
        implStream.setStreamType(this.recChanCfg.enmStrmType());            // throws IllegalStateException, ConfigurationException
        
        if (this.recChanCfg.bolMStrmEnbl())
            implStream.setMultipleStreams(this.recChanCfg.cntMStrmMax());   // throws IllegalStateException, IllegalArgumentException
        else
            implStream.disableMultipleStreams();                            // throws IllegalStateException
    }
    
}
