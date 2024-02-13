/*
 * Project: dp-api-common
 * File:	QueryResponseAssembler.java
 * Package: com.ospreydcs.dp.api.query.model.proto
 * Type: 	QueryResponseAssembler
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
 * @since Feb 5, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.proto;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.model.ResultRecord;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpDataRequest.CompositeType;
import com.ospreydcs.dp.api.query.model.DpQueryStreamType;
import com.ospreydcs.dp.api.query.model.time.SamplingProcess;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.BucketData;

/**
 *
 * @author Christopher K. Allen
 * @since Feb 5, 2024
 *
 */
public class QueryResponseAssembler {
    
    //
    // Class Types
    //
    
    /**
     * <p>
     * Implementation of <code>{@link Consumer}</code> interface which transfers data from gRPC
     * stream to <code>{@link QueryResponseAssembler}</code> data buffer.
     * </p>
     * <p>
     * The <code>{@link #accept(BucketData)}</code> method of this class transfers the given
     * message to the data queue buffer <code>{@link QueryResponseAssembler#queRspData}</code>
     * of the enclosing class instance.  The current number of <code>BucketData</code> messages
     * transferred is available through the <code>{@link #getMessageCount()}</code> method. 
     * </p>
     * <p>
     * Instances are intended to be passed to newly created <code>{@link QuerResponseStreamProcessor}</code>
     * instances via the creator 
     * <code>{@link QueryResponseStreamProcessor#newTask(DpDataRequest, DpQueryServiceStub, Consumer)}</code>.
     * Thus, they simply take the extracted <code>BucketData</code> messages from the stream processor
     * and add them to the queue buffer of the enclosing class instance.
     * </p>
     *  
     * @author Christopher K. Allen
     * @since Feb 8, 2024
     *
     */
    private final class QueryDataConsumer implements Consumer<QueryResponse.QueryReport.BucketData> {

        
        // 
        // Creator
        //
        
        /**
         * Creates a new instance of <code>BucketDataConsumer</code> for the given enclosing class instance.
         * 
         * @param parent  <code>QueryResponseAssemble</code> instance target of consumer
         * 
         * @return  new <code>BucketDataConsumer</code> for the given instance
         */
        public static QueryDataConsumer newInstance(QueryResponseAssembler parent) {
            return parent.new QueryDataConsumer();
        }
        
        //
        // State Variables
        //
        
        /** Number of data messages consumed so far */
        private int             cntMsgs = 0;

        
        /**
         * <p>
         * Returns the number of <code>BucketData</code> messages transferred to the queue buffer.
         * </p>
         * 
         * @return  current number of data messages passed from the stream processor to the queue buffer
         */
        public final int getMessageCount() {
            return this.cntMsgs;
        }
        
        //
        // Consumer<BucketData> Interface
        //
        
        /**
         * Adds the given message to the data queue buffer <code>{@link QueryResponseAssembler#queRspData}</code>
         * 
         * @param msgData   data message obtained from the associated <code>{@link QueryResponseStreamProcessor}</code>.
         * 
         * @see java.util.function.Consumer#accept(java.lang.Object)
         */
        @Override
        public void accept(BucketData msgData) {
            QueryResponseAssembler.this.queRspData.add(msgData);
            
            this.cntMsgs++;
        }
        
    }
    
    /**
     * <p>
     * Independent data processing threads for the <code>QueryResponseAssembler</code>.
     * </p>
     * <p>
     * This task is performed on a separate thread to decouple the gRPC streaming of
     * <code>QueryResponse</code> messages from the Query Service from the process of
     * correlating the responses.  We do not want to interrupt the data streaming but
     * do want to began processing the data as soon as possible.
     * </p>
     * <p>
     * Performs the transfer of <code>BucketData</code> messages from the queue buffer to
     * the response correlator <code>{@link QueryResponseAssembler#qrcDataCorrelator}</code>.
     * Note that the queue buffer <code>@link {@link QueryResponseAssembler#queRspData}</code>
     * is populated by the <code>Consumer<DataQuery></code> implementation passed to any 
     * <code>{@link QueryResponseStreamProcessor}</code>.
     * </p>
     *
     * @author Christopher K. Allen
     * @since Feb 8, 2024
     *
     */
    private final class QueryDataProcessor extends Thread {
        
        
        //
        // Creator
        //
        
        /**
         * Creates a new instance of <code>QueryDataProcessor</code> for the given enclosing class instance.
         * 
         * @param parent  <code>QueryResponseAssemble</code> instance target of processor
         * 
         * @return  new <code>QueryDataProcessor</code> for the given instance
         */
        public static QueryDataProcessor newThread(QueryResponseAssembler parent) {
            return parent.new QueryDataProcessor();
        }

        //
        // State Variables
        //
        
        /** Number of data messages to process (unknown until set) */
        private Integer         cntMsgsMax = null;
        
        /** Number of data messages polled so far */
        private int             cntMsgsProcessed = 0;
        
        /** The result of the thread task */
        private ResultRecord    recResult = null;
        
        
        //
        // Operations
        //
        
        /**
         * <p>
         * Sets the maximum number of <code>BucketData</code> messages to process.
         * </p>
         * <p>
         * Note that this value is typically unknown until the stream processor completes.
         * Thus we assume this value is set after the <code>QueryDataProcessor</code> thread
         * has already started.
         * </p>
         * <p>
         * The <code>{@link #cntMsgsMax}</code> attribute is set to the argument value then
         * the <code>{@link #cntMsgsProcessed}</code> attribute is checked against the argument value.
         * If the maximum number of messages has already been processed, the processing loop
         * is interrupted and the result is considered SUCCESS.
         * </p>
         * 
         * @param cntMsgsMax    maximum number of <code>BucketData</code> messages to process
         */
        public void setMaxMessages(int cntMsgsMax) {
            this.cntMsgsMax = cntMsgsMax;
            
            if (this.cntMsgsProcessed >= this.cntMsgsMax) {
                Thread.currentThread().interrupt();
                
                this.recResult = ResultRecord.SUCCESS;
            }
        }

        /**
         * <p>
         * Terminates the data processing immediately.
         * </p>
         * <p>
         * The data processing loop within <code>{@link #run()}</code> is interrupted
         * and the result is set to FAILURE.  All processing activity is aborted
         * and left in its current state.
         * </p>
         */
        public void terminate() {
            Thread.currentThread().interrupt();
            
            this.recResult = ResultRecord.newFailure("The data processing thread was terminted externally.");
        }
        
        //
        // State Queries
        //
        
        public boolean isSuccess() {
            if (this.recResult == null)
                return false;
            
            return this.recResult.isSuccess();
        }
        
        public ResultRecord getResult() {
            return this.recResult;
        }
        
        
        //
        // Runnable Interface
        //
        
        /**
         * <p>
         * Executes the processing loop.
         * </p>
         * <p>
         * Runs a loop that polls the <code>{@link QueryResponseAssembler#queRspData}</code> buffer
         * for <code>BucketData</code> data messages.  When available, the data messages are
         * passed to the <code>{@link QueryResponseAssembler#qrcDataCorrelator}</code> for
         * processing.
         * </p>
         * <p>
         * The loop continues until the number of data messages obtained from the buffer is
         * equal to the parameter <code>{@link #cntMsgsMax}</code>, which must eventually be set.
         * The loop will also terminated if a timeout limit is reached while waiting for a
         * message to be available.  Use <code>{@link #getResult()}</code> to return the 
         * cause of any errors.
         * </p>
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            while (this.cntMsgsMax == null || this.cntMsgsProcessed < this.cntMsgsMax) {
                try {
                    QueryResponse.QueryReport.BucketData msgData = QueryResponseAssembler.this.queRspData.poll(CNT_TIMEOUT, TU_TIMEOUT);

                    if (msgData == null) {
                        this.recResult = ResultRecord.newFailure("A timeout occurred while waiting for the data buffer.");

                        return;
                    }

                    // Note that this operation will block until completed
                    QueryResponseAssembler.this.qrcDataCorrelator.insertQueryData(msgData);
                    this.cntMsgsProcessed++;

                } catch (InterruptedException e) {
                    if (this.cntMsgsMax != null && (this.cntMsgsProcessed >= this.cntMsgsMax)) {
                        this.recResult = ResultRecord.SUCCESS;
                        
                        return;
                    }
                    
                    this.recResult = ResultRecord.newFailure("Process interrupted externally while waiting for the data buffer.", e);

                    return;
                }
            }

            this.recResult = ResultRecord.SUCCESS;
        }
    }
    
    
    //
    // Application Resources
    //
    
    /** The Data Platform Query Service default parameters */
    private static final DpQueryConfig CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** Is logging active? */
    public static final boolean     BOL_LOGGING = CFG_QUERY.logging.active;
    
    
    /** Is response processing concurrency active? */
    public static final boolean     BOL_CONCURRENCY = CFG_QUERY.concurrency.active;
    
    /** Number of gRPC data streams to use for single query */
    public static final int         CNT_CONCURRENCY_THREADS = CFG_QUERY.concurrency.threadCount;
    
    
    /** Is response multi-streaming active? */
    public static final boolean     BOL_MULTISTREAM = CFG_QUERY.data.response.multistream.active;
    
    /** Maximum number of open data streams to Query Service */
    public static final int         CNT_MULTISTREAM = CFG_QUERY.data.response.multistream.maxStreams;
    
    /** Query domain size triggering multiple streaming (if active) */
    public static final long        LNG_MULTISTREAM_PIVOT = CFG_QUERY.data.response.multistream.pivotSize;
    
    /** Query domain time units used in multi-streaming pivot size */
    public static final TimeUnit    TU_MULTISTREAM_PIVOT = CFG_QUERY.data.response.multistream.pivotUnits;
    
    
    /** Is timeout limit active ? */
    public static final boolean     BOL_TIMEOUT = CFG_QUERY.timeout.active;
    
    /** Timeout limit for query operation */
    public static final long        CNT_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** Timeout unit for query operation */
    public static final TimeUnit    TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    //
    // Initializing Attributes
    //
    
//    /** The data request to be processed */
//    private final DpDataRequest         rqstTarget;
    
    /** The Query Service asynchronous communications stub */
    private final DpQueryServiceStub    stubAsync;
    
    
    //
    // Instance Resources
    //
    
    /** The queue buffering all response messages for data correlation processing */
    private final BlockingQueue<BucketData>  queRspData = new LinkedBlockingQueue<>();
    
    /** The thread pool service for processing composite requests */
    private final ExecutorService           exeThreadPool = Executors.newFixedThreadPool(CNT_MULTISTREAM);
    
    /** The Query Service response data correlator */
    private final QueryResponseCorrelator   qrcDataCorrelator = new QueryResponseCorrelator();  
    
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryResponseAssembler</code>.
     * </p>
     *
     */
    public QueryResponseAssembler(DpQueryServiceStub stubAsync) {
        this.stubAsync = stubAsync;
//        this.rqstTarget = rqstTarget;
    }

    
    //
    // Operations
    //
    
    public SamplingProcess  performRequest(DpDataRequest dpRequest) throws InterruptedException, TimeoutException, ExecutionException {
        
        // Process with a single data stream if multi-streaming is not active
        if (!BOL_MULTISTREAM)
            return processSingleStream(dpRequest);

        // Check if request domain size is large enough to pivot to multi-streaming
        long szRequest = dpRequest.getDomainSize(TU_MULTISTREAM_PIVOT);
        
        if (szRequest < LNG_MULTISTREAM_PIVOT)
            return processSingleStream(dpRequest);
        
        // Okay - we are doing multiple gRPC streams
        List<DpDataRequest>     lstCmpRqsts;    // composite request
        
        //  - Check if default query domain decomposition will work
        DpDataRequest.DomainDecomposition recDomain = dpRequest.getDecompositionDefault();
        
        if (recDomain.totalCovers() < CNT_MULTISTREAM) {
            lstCmpRqsts = dpRequest.buildCompositeRequest(recDomain);
            
            this.processMultiStream(lstCmpRqsts);
        }
        
        //  - Check if we can decompose by data sources
        if (dpRequest.getSourceCount() > CNT_MULTISTREAM) {
            lstCmpRqsts = dpRequest.buildCompositeRequest(CompositeType.HORIZONTAL, CNT_MULTISTREAM);
        
            return processMultiStream(lstCmpRqsts);
        }
        
        //  - Decompose vertically in sizes of the pivot number
        long cntRqsts = szRequest / LNG_MULTISTREAM_PIVOT + ((szRequest % LNG_MULTISTREAM_PIVOT > 0) ? 1 : 0);
        
        if (cntRqsts < CNT_MULTISTREAM) {
            lstCmpRqsts = dpRequest.buildCompositeRequest(CompositeType.VERTICAL, Long.valueOf(cntRqsts).intValue());
            
            this.processMultiStream(lstCmpRqsts);
        }
        
        //  - Attempt a grid decomposition
        if (dpRequest.getSourceCount() > (CNT_MULTISTREAM/2)) {
            lstCmpRqsts = dpRequest.buildCompositeRequest(CompositeType.GRID, CNT_MULTISTREAM);
            
            this.processMultiStream(lstCmpRqsts);
        }
        
        
        // We cannot find a decomposition - Default back to single stream processing  
        return this.processSingleStream(dpRequest);
        
    }
    
    
    //
    // Support Methods
    //
    
    private SamplingProcess processSingleStream(DpDataRequest dpRequest) throws InterruptedException, TimeoutException, ExecutionException {

        QueryResponseStreamProcessor    taskStream = QueryResponseStreamProcessor.newTask(
                dpRequest, 
                this.stubAsync, 
                QueryDataConsumer.newInstance(this)
                );

        Thread              thdStream = new Thread(taskStream);
        QueryDataProcessor  thdProcess = QueryDataProcessor.newThread(this);

        try {
            thdProcess.start();
            thdStream.start();

            thdStream.join(timeoutLimitDefaultMillis());
            
            // Check for timeout limit
            if (!taskStream.isCompleted()) {
                String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() + " - Time limit expired while gRPC data streaming.";

                if (BOL_LOGGING)
                    LOGGER.error(strMsg);
                
                throw new TimeoutException(strMsg);
            }

            // Check for streaming error
            if (!taskStream.isSuccess()) {
                ResultRecord    recResult = taskStream.getResult();
                String          strMsg = JavaRuntime.getQualifiedCallerNameSimple()
                        + " - gRPC streaming error; message="
                        + recResult.message();
                if (recResult.hasCause())
                    strMsg += ", cause=" + recResult.cause();
                
                if (BOL_LOGGING)
                    LOGGER.error(strMsg);
                
                thdProcess.terminate();
                
                throw new ExecutionException(strMsg, recResult.cause());
            }
            
            // Set the number of messages to process
            thdProcess.setMaxMessages(taskStream.getResponseCount());

            thdProcess.join(timeoutLimitDefaultMillis());
            
        } catch (InterruptedException e) {
            if (BOL_LOGGING)
                LOGGER.error("{}: Interrupted while waiting for data streaming/processing to complete - {}.", JavaRuntime.getCallerName(), e.getMessage());
            
            throw e;
        }
        
        SamplingProcess sp = SamplingProcess.from(this.qrcDataCorrelator.getTargetSet());
        
        return sp;
    }
    
    private SamplingProcess processMultiStream(List<DpDataRequest> lstRequests) throws InterruptedException, TimeoutException, ExecutionException {
        
        // Create the multiple stream processing tasks
        List<QueryResponseStreamProcessor>  lstStrmTasks = new LinkedList<>();
        for (DpDataRequest dpRequest : lstRequests) {
            QueryResponseStreamProcessor    taskStrm = QueryResponseStreamProcessor.newTask(
                    dpRequest, 
                    this.stubAsync, 
                    QueryDataConsumer.newInstance(this)
                    );
            
            lstStrmTasks.add(taskStrm);
        }
        
        // Create the single data processing task thread
        //  Then start the thread so its ready for incoming data from streams
        QueryDataProcessor  thdProcessor = QueryDataProcessor.newThread(this);
        
        thdProcessor.start();
        
        // Now start all data stream tasks and wait for completion
        this.exeThreadPool.invokeAll(lstStrmTasks, CNT_TIMEOUT, TU_TIMEOUT);
        
        // Check for timeout - not all data streams will have completed.
        boolean bolCompleted = lstStrmTasks.stream().allMatch(p -> p.isCompleted());
        
        if (!bolCompleted) {
            thdProcessor.terminate();
            
            String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() + " - Time limit expired while gRPC data streaming.";

            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new TimeoutException(strMsg);
        }

        // Check for streaming error
        boolean bolSuccess = lstStrmTasks.stream().allMatch(p -> p.isSuccess());
        
        if (!bolSuccess) {
            thdProcessor.terminate();
            
            ResultRecord recResult = lstStrmTasks
                                    .stream()
                                    .filter(p  -> p.getResult().isFailure())
                                    .<ResultRecord>map(p -> p.getResult())
                                    .findAny()
                                    .get();
            String strMsg = JavaRuntime.getQualifiedCallerNameSimple() 
                    + " - At least one gRPC streaming error: message=" 
                    + recResult.message()
                    + ( (recResult.hasCause()) ? ", cause=" + recResult.cause() : "");
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new ExecutionException(strMsg, recResult.cause());
        }
        
        // Recover the total number of messages streamed 
        int cntMsgs = lstStrmTasks.stream().mapToInt(p -> p.getResponseCount()).sum();
        
        // Set the total number of data messages to process then wait for completion
        thdProcessor.setMaxMessages(cntMsgs);
        
        thdProcessor.join(timeoutLimitDefaultMillis());

        // Create the sampling process and return it
        SamplingProcess sp = SamplingProcess.from(this.qrcDataCorrelator.getTargetSet());
        
        return sp;
    }
    
    private Thread  newStreamProcessor(DpDataRequest dpRequest) {
        QueryResponseStreamProcessor    taskProcessor = QueryResponseStreamProcessor.newTask(
                dpRequest, 
                this.stubAsync, 
                QueryDataConsumer.newInstance(this)
                );
        
        Thread  thdProcessor = new Thread(taskProcessor);
        
        return thdProcessor;
    }
    
    /**
     * Computes and returns the default timeout limit in unites of milliseconds 
     * 
     * @return  default timeout limit in milliseconds
     * 
     * @see #CNT_TIMEOUT
     * @see #TU_TIMEOUT
     * @see TimeUnit#MILLISECONDS
     */
    private static long    timeoutLimitDefaultMillis() {
        long    lngTimeoutMs = TU_TIMEOUT.convert(CNT_TIMEOUT, TimeUnit.MILLISECONDS);
        
        return lngTimeoutMs;
    }
}
