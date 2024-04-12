/*
 * Project: dp-api-common
 * File:	IngestDataRequestProcessor.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IngestDataRequestProcessor
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
 * @since Apr 10, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.grpc;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.ingest.model.IngestionFrame;
import com.ospreydcs.dp.api.ingest.model.frame.FrameBinner;
import com.ospreydcs.dp.api.model.DpGrpcStreamType;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataResponse;

/**
 *
 * @author Christopher K. Allen
 * @since Apr 10, 2024
 *
 */
public final class IngestDataRequestProcessor {

    //
    // Internal Types
    //
    
    /**
     * <p>
     * Facilitates ingestion frame decomposition on independent execution thread.
     * </p>
     * 
     * @author Christopher K. Allen
     * @since Apr 10, 2024
     *
     */
    class BinnerThread implements Runnable {

        // 
        // Creator
        //
        
//        public static BinnerThread from(final FrameBinner binner, IngestionFrame frame) {
//            return IngestDataRequestProcessor.this.new BinnerThread(binner, frame);
//        }
        
        //
        // Initialization Targets
        //
        
        /** The active frame decomposition processing */
        private final FrameBinner       binner;
        
        /** The target ingestion frame to be decomposed */
        private final IngestionFrame    frame;
        
        BinnerThread(final FrameBinner binner, IngestionFrame frame) {
            this.binner = binner;
            this.frame = frame;
        }
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    //
    // Application Resources
    //
    
    /** The Ingestion Service client API default configuration */
    private static final DpIngestionConfig  CFG_DEFAULT = DpApiConfig.getInstance().ingest; 
    
    
    //
    // Class Constants
    //
    
    /** Is logging active */
    private static final Boolean    BOL_LOGGING = CFG_DEFAULT.logging.active;

    
    /** Are general concurrency active - used for ingestion frame decomposition */
    private static final Boolean    BOL_CONCURRENCY_ACTIVE = CFG_DEFAULT.concurrency.active;
    
    /** Thresold in which to pivot to concurrent processing */
    private static final Integer    INT_CONCURRENCY_PIVOT_SZ = CFG_DEFAULT.concurrency.pivotSize;
    
    /** Maximum number of concurrent processing threads */
    private static final Integer    INT_CONCURRENCY_CNT_THREADS = CFG_DEFAULT.concurrency.threadCount;
    
    
    /** Are general timeout active */
    private static final Boolean    BOL_TIMEOUT_ACTIVE = CFG_DEFAULT.timeout.active;
    
    /** General timeout limit for operations */
    private static final Long      LNG_TIMEOUT = CFG_DEFAULT.timeout.limit;
    
    /** General timeout time units */
    private static final TimeUnit  TU_TIMEOUT = CFG_DEFAULT.timeout.unit;
    
    
    /** Allow gRPC data streaming - this is ignored for all streaming operations */
    private static final Boolean            BOL_STREAM_ACTIVE = CFG_DEFAULT.stream.active;
    
    /** The preferred gRPC stream type to Ingestion Service */
    private static final DpGrpcStreamType   ENM_STREAM_PREF = CFG_DEFAULT.stream.type;
    
    
    /** Use ingestion frame buffering from client to gRPC stream */
    private static final Boolean    BOL_BUFFER_ACTIVE = CFG_DEFAULT.stream.buffer.active;
    
    /** Size of the ingestion frame queue buffer */
    private static final Integer    INT_BUFFER_SIZE = CFG_DEFAULT.stream.buffer.size;
    
    /** Allow back pressure to client from queue buffer */
    private static final Boolean    BOL_BUFFER_BACKPRESSURE = CFG_DEFAULT.stream.buffer.backPressure;
    
    
    /** Perform ingestion frame decomposition (i.e., "binning") */
    private static final Boolean    BOL_BINNING_ACTIVE = CFG_DEFAULT.stream.binning.active;
    
    /** Maximum size limit (in bytes) of decomposed ingestion frame */
    private static final Integer    LNG_BINNING_MAX_SIZE = CFG_DEFAULT.stream.binning.maxSize;
    
    
    /** Use multiple gRPC data stream to transmit ingestion frames */
    private static final Boolean    BOL_MULTISTREAM_ACTIVE = CFG_DEFAULT.stream.concurrency.active;
    
    /** When the number of frames available exceeds this value multiple gRPC data streams are used */ 
    private static final Long       LNG_MULTISTREAM_PIVOT = CFG_DEFAULT.stream.concurrency.pivotSize;
    
    /** The maximum number of gRPC data stream used to transmit ingestion data */
    private static final Integer    INT_MULTISTREAM_MAX = CFG_DEFAULT.stream.concurrency.maxStreams;
    
    
    //
    // Class Resources
    //
    
    /** Class logger */
    protected static final Logger     LOGGER = LogManager.getLogger();
    
    
    //
    // Defining Attributes
    //
    
    /** The gRPC connection to the Ingestion Service */
    private final DpIngestionConnection     connIngest;
    
    /** The data provider UID */
    private final int                       intProviderId;
    
    
    //
    // Instance Resources
    //
    
//    /** Incoming ingestion frame buffer */
//    private final Queue<IngestionFrame>             bufFrames = new LinkedList<>();
//    
//    /** Pool of ingestion frame decomposers (frame binners) */
//    private final BlockingQueue<FrameBinner>        polBinners = new LinkedBlockingQueue<>(INT_CONCURRENCY_CNT_THREADS);
//    
//    /** Outgoing ingestion request queue */
//    private final BlockingQueue<IngestDataRequest>  queRequests = new LinkedBlockingQueue(INT_BUFFER_SIZE);
//    
    /** Collection of all incoming ingestion responses */
    private final Collection<IngestDataResponse>    setResponses = new LinkedList<>();
    
    
    /** Source of outgoing ingestion requests */
    private final IMessageSupplier<IngestDataRequest>   fncDataSource;
    
    /** Source for incoming response messages */
    private final Consumer<IngestDataResponse>          fncDataSink = (rsp) -> { this.setResponses.add(rsp); };
    
    /** The pool of (multiple) gRPC streaming thread(s) for recovering requests */
    private final ExecutorService           exeThreadPool = Executors.newFixedThreadPool(INT_MULTISTREAM_MAX);
    
    
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestDataRequestProcessor</code>.
     * </p>
     *
     */
    public IngestDataRequestProcessor(DpIngestionConnection connIngest, int intProviderId) {
        this.connIngest = connIngest;
        this.intProviderId = intProviderId;
        
        this.fncDataSource = new IngestDataRequestSupplier(intProviderId);
    }
    
    
    //
    // Operations
    //
    
    

    //
    // Support Methods
    //
    
    /**
     * <p>
     * Determines whether or not the original data request was accepted by the Query Service.
     * </p>
     * <p>
     * This method should be called only once, with the argument being the first response
     * from the Query Service after stream initiation.  If the original data query
     * request was invalid or corrupt the Query Service streams back a single 
     * <code>QueryResponse</code> message containing a <code>ExceptionalResult</code> message
     * describing the rejection.
     * </p> 
     * <p>
     * The method Checks for a <code>ExceptionalResult</code> message within the query response 
     * argument.  If present then the original data request was rejected by the Query Service.
     * The details of the rejection are then extracted from the argument and returned in
     * a FAILED result record.  Otherwise (i.e., the request was accepted) the method 
     * returns the SUCCESS result record.
     * </p> 
     * 
     * @param msgRsp    the first response from the Query Service data stream
     * 
     * @return  the SUCCESS record if query was accepted, 
     *          otherwise a failure message containing a description of the rejection 
     */
    protected ResultStatus isRequestAccepted(IngestDataResponse msgRsp) {

        // Check for RequestRejected message
        if (msgRsp.hasExceptionalResult()) {
            ExceptionalResult           msgException = msgRsp.getExceptionalResult();
            String                      strCause = msgException.getMessage();
            ExceptionalResult.ExceptionalResultStatus     enmCause = msgException.getExceptionalResultStatus();

            String       strMsg = "The data request was rejected by Query Service: cause=" + enmCause + ", message=" + strCause;
            ResultStatus result = ResultStatus.newFailure(strMsg);

            return result;
        }

        // No rejection, return success
        return ResultStatus.SUCCESS;
    }

    /**
     * <p>
     * Processes the data within the given Ingestion Service response message.
     * </p>
     * <p>
     * For normal operation, a <code>AckResult</code> message is confirmed within the argument.
     * If confirmed the argument is then passed to the message consumer identified at construction.
     * A SUCCESS result record is then returned indicating successful consumption of response data.
     * </p>
     * <p>
     * If the argument contains an exception(i.e., rather than data) then the method
     * extracts the status and accompanying message.  An error message is constructed
     * and returned via a FAILED result record.
     * </p>
     * 
     * @param msgRsp    an Ingestion Service response message containing acknowledgment or status error
     * 
     * @return  the SUCCESS record if acknowledgment was confirmed,
     *          otherwise a failure message containing the status error description 
     */
    protected ResultStatus processResponse(IngestDataResponse msgRsp) {

        // Confirm acknowledgment and pass to message consumer
        if (msgRsp.hasAckResult()) {
            this.fncDataSink.accept(msgRsp);

            return ResultStatus.SUCCESS;
        }

        // Response Error - extract the details and return them
        ExceptionalResult   msgException = msgRsp.getExceptionalResult();
        String              strStatus = msgException.getMessage();
        ExceptionalResult.ExceptionalResultStatus enmStatus = msgException.getExceptionalResultStatus();

        String          strMsg = "Ingestion Service reported response error: status=" + enmStatus + ", message= " + strStatus;
        ResultStatus    recErr = ResultStatus.newFailure(strMsg);

        return recErr;
    }

}
