/*
 * Project: dp-api-common
 * File:	QueryResponseCorrelator.java
 * Package: com.ospreydcs.dp.api.query.model.proto
 * Type: 	QueryResponseCorrelator
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
import java.util.SortedSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.naming.CannotProceedException;
import javax.naming.OperationNotSupportedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.model.ResultRecord;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpDataRequest.CompositeType;
import com.ospreydcs.dp.api.query.model.DpQueryException;
import com.ospreydcs.dp.api.util.JavaRuntime;
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
public class QueryResponseCorrelator {
    
    //
    // Class Types
    //
    
    /**
     * <p>
     * Implementation of <code>{@link Consumer}</code> interface which transfers data from gRPC
     * stream to <code>{@link QueryResponseCorrelator}</code> data buffer.
     * </p>
     * <p>
     * The <code>{@link #accept(BucketData)}</code> method of this class transfers the given
     * message to the data queue buffer <code>{@link QueryResponseCorrelator#queRspData}</code>
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
        public static QueryDataConsumer newInstance(QueryResponseCorrelator parent) {
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
         * Adds the given message to the data queue buffer <code>{@link QueryResponseCorrelator#queRspData}</code>
         * 
         * @param msgData   data message obtained from the associated <code>{@link QueryResponseStreamProcessor}</code>.
         * 
         * @see java.util.function.Consumer#accept(java.lang.Object)
         */
        @Override
        public void accept(BucketData msgData) {
            QueryResponseCorrelator.this.queRspData.add(msgData);
            
            this.cntMsgs++;
        }
        
    }
    
    /**
     * <p>
     * Independent data processing threads for the <code>QueryResponseCorrelator</code>.
     * </p>
     * <p>
     * This task is performed on a separate thread to decouple the gRPC streaming of
     * <code>QueryResponse</code> messages from the Query Service from the process of
     * correlating the responses.  We do not want to interrupt the data streaming but
     * do want to began processing the data as soon as possible.
     * </p>
     * <p>
     * Performs the transfer of <code>BucketData</code> messages from the queue buffer to
     * the response correlator <code>{@link QueryResponseCorrelator#qcdCorrelator}</code>.
     * Note that the queue buffer <code>@link {@link QueryResponseCorrelator#queRspData}</code>
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
        public static QueryDataProcessor newThread(QueryResponseCorrelator parent) {
            return parent.new QueryDataProcessor();
        }

        
        // 
        // Class Constants
        //
        
        /** Polling timeout limit */
        public static final long        LNG_TIMEOUT = 15;
        
        /** Polling timeout units */
        public static final TimeUnit    TU_TIMEOUT = TimeUnit.MILLISECONDS;
        
        //
        // State Variables
        //
        
        /** thread started flag */
        private boolean         bolStarted = false;
        
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
            
//            if (this.cntMsgsProcessed >= this.cntMsgsMax) {
//                Thread.currentThread().interrupt();
//                
//                this.recResult = ResultRecord.SUCCESS;
//            }
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
         * <p>
         * <h2>NOTES:</h2>
         * Terminating a processing thread that has not started has no effect.
         * </p>
         */
        public void terminate() {
            if (!this.bolStarted)
                return;
            
            Thread.currentThread().interrupt();
            
            this.recResult = ResultRecord.newFailure("The data processing thread was terminted externally.");
        }
        
        //
        // State Queries
        //
        
        public boolean isStarted() {
            return this.bolStarted;
        }
        
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
         * Runs a loop that polls the <code>{@link QueryResponseCorrelator#queRspData}</code> buffer
         * for <code>BucketData</code> data messages.  When available, the data messages are
         * passed to the <code>{@link QueryResponseCorrelator#qcdCorrelator}</code> for
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
            this.bolStarted = true;
            
            while (this.cntMsgsMax == null || this.cntMsgsProcessed < this.cntMsgsMax) {
                try {
                    QueryResponse.QueryReport.BucketData msgData = QueryResponseCorrelator.this.queRspData.poll(LNG_TIMEOUT, TU_TIMEOUT);
//                    QueryResponse.QueryReport.BucketData msgData = QueryResponseCorrelator.this.queRspData.take();

                    if (msgData == null) {
//                        this.recResult = ResultRecord.newFailure("A timeout occurred while waiting for the data buffer.");
//
//                        return;
                        continue;
                    }

                    // Note that this operation will block until completed
                    QueryResponseCorrelator.this.qcdCorrelator.insertQueryData(msgData);
                    this.cntMsgsProcessed++;
                    
                    // TODO - remove
                    System.out.println("Processed message " + this.cntMsgsProcessed);

                } catch (InterruptedException e) {
                    if (this.cntMsgsMax != null && (this.cntMsgsProcessed >= this.cntMsgsMax)) {
                        this.recResult = ResultRecord.SUCCESS;
                        
                        // TODO - remove
                        System.out.println("Interrupted but successful.");
                        
                        return;
                    }
                    
                    // TODO - remove
                    System.out.println("HA HA HA HA - Interrupted and FAILED.");
                    
                    this.recResult = ResultRecord.newFailure("Process interrupted externally while waiting for the data buffer.", e);

                    return;
                }
            }

            // TODO - remove
            System.out.println(JavaRuntime.getQualifiedCallerNameSimple() + ": Exiting processing loop.");
            
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
    
    
    /** Is timeout limit active ? */
    public static final boolean     BOL_TIMEOUT = CFG_QUERY.timeout.active;
    
    /** Timeout limit for query operation */
    public static final long        CNT_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** Timeout unit for query operation */
    public static final TimeUnit    TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
//    /** Is response processing concurrency active? */
//    public static final boolean     BOL_CONCURRENCY = CFG_QUERY.concurrency.active;
//    
//    /** Number of gRPC data streams to use for single query */
//    public static final int         CNT_CONCURRENCY_THREADS = CFG_QUERY.concurrency.threadCount;

    /** Use multi-threading for query data correlation */
    public static final boolean     BOL_CORRELATE_CONCURRENCY = CFG_QUERY.data.response.correlate.useConcurrency;
    
    /** Perform data correlation while gRPC streaming - otherwise do it post streaming */
    public static final boolean     BOL_CORRELATE_MIDSTREAM = CFG_QUERY.data.response.correlate.whileStreaming;
    
    
    /** Is response multi-streaming active? */
    public static final boolean     BOL_MULTISTREAM = CFG_QUERY.data.response.multistream.active;
    
    /** Maximum number of open data streams to Query Service */
    public static final int         CNT_MULTISTREAM = CFG_QUERY.data.response.multistream.maxStreams;
    
    /** Query domain size triggering multiple streaming (if active) */
    public static final long        LNG_MULTISTREAM_PIVOT = CFG_QUERY.data.response.multistream.pivotSize;
    
    /** Query domain time units used in multi-streaming pivot size */
    public static final TimeUnit    TU_MULTISTREAM_PERIOD = CFG_QUERY.data.response.multistream.pivotPeriod;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    //
    // Initializing Attributes
    //
    
    /** The connection to the Query Service (contains communications stubs) */
    private final DpQueryConnection         connQuery;

    
    //
    // Instance Resources
    //
    
    /** The queue buffering all response messages for data correlation processing */
    private final BlockingQueue<BucketData> queRspData = new LinkedBlockingQueue<>();
    
    /** The Query Service response data correlator */
    private final QueryDataCorrelator       qcdCorrelator = new QueryDataCorrelator();  
    
    /** The thread pool service for processing composite requests */
    private final ExecutorService           exeThreadPool = Executors.newFixedThreadPool(CNT_MULTISTREAM);
    
    
    //
    //  State and Configuration
    //
    
    /** Multi-streaming query response toggle */
    private boolean bolMultiStream = BOL_MULTISTREAM;

    /** Use multi-threading for query data correlation */
    private boolean bolCorrelateConcurrency = BOL_CORRELATE_CONCURRENCY;
    
    /** Perform query data correlation concurrent while gRPC streaming */
    private boolean bolCorrelateMidstream = BOL_CORRELATE_MIDSTREAM;
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryResponseCorrelator</code>.
     * </p>
     *  
     */
    public QueryResponseCorrelator(DpQueryConnection connQuery) {
        this.connQuery = connQuery;
        
        this.qcdCorrelator.setConcurrency(this.bolCorrelateConcurrency);
    }

    //
    // State and Configuration
    //
    
    /**
     * <p>
     * Toggles the use of multiple query response gRPC data streams in
     * <code>{@link #processRequestStream(DpDataRequest)}</code>.
     * </p>
     * <p>
     * The method <code>{@link #processRequestStream(DpDataRequest)}</code>
     * is capable of using multiple gRPC data streams.  The method attempts to decompose
     * large data requests into composite request according to settings in the default
     * Data Platform API configuration (see <code>{@link DpApiConfig}</code>).  This
     * operation can turn off the default behavior.
     * </p>
     * <p>
     * This is a performance parameter where a potentially large results set can be recovered
     * from the Query Service on concurrent gRPC data streams.  Any performance increase is
     * dependent upon the size of the results set, the network and its current traffic, and
     * the host platform configuration.
     * </p> 
     * <p>
     * Query data can be recovered using multiple gRPC data streams.  There, the domain of the
     * data  request is decomposed using the default composite query mechanism.  The components
     * of the composite request are then separate performed on independent gRPC data streams.
     * The full result set is then assembled from the multiple data streams.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Only affects the operation of <code>{@link #processRequestStream(DpDataRequest)}</code>.</li>
     * <li>All composite query configuration parameters are taken from the default API parameters.</li>
     * <li>All gRPC multi-streaming parameters are taken from the default API parameters.</li>
     * </ul>
     * </p>  
     * 
     * @param bolMultiStream    turn on/off the use of multiple gRPC streams for query response recovery
     */
    public void setMultiStreaming(boolean bolMultiStream) {
        this.bolMultiStream = bolMultiStream;
    }
    
    /**
     * <p>
     * Toggles the use of multi-threading for correlation of query data.
     * </p>
     * <p>
     * This is a performance parameter where correlation of query data is performed using
     * parallelism.  Due to the nature of data correlation, multiple data sets can be
     * processed simultaneously.  Thus, speedup is directly proportional to the number of
     * CPU cores employed.
     * </p> 
     * <p>
     * The internal <code>{@link QueryDataCorrelator}</code> instance used to correlate the
     * results set of a Query Service data request has parallel processing capability.  This
     * function can be toggled on or off.  Parallel processing of request data can greatly
     * enhance performance, especially for large results sets.  However, it can also abscond 
     * processor resources (i.e., cores) from other concurrent, critical processes (e.g., 
     * streaming resources).  Thus, in some situations the overall effect could be performance
     * degradation.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The number of correlation processing threads is taken from the API default parameters.</li>
     * <li>Concurrency is only invoked if data sizes are greater than the pivot size in the API default parameters.</li>
     * </ul>
     * </p>
     * 
     * @param bolCorrelateConcurrently   turn on/off the use of parallelism for data correlation
     */
    public void setCorrelationConcurrency(boolean bolCorrelateConcurrently) {
        this.bolCorrelateConcurrency = bolCorrelateConcurrently;
        this.qcdCorrelator.setConcurrency(bolCorrelateConcurrently);
    }
    
    /**
     * <p>
     * Toggles the application of query data correlation during gRPC streaming operations.
     * </p>
     * <p>
     * This is a performance parameter where the correlation of query data is performed 
     * simultaneously while gRPC data streaming.  The data stream(s) for large results set
     * can extend over significant real time.  Thus, performing correlation while receiving
     * data can decrease overall processing/transmission time.
     * </p>
     * <p>
     * If this feature is turned off then all query data correlation is done after gRPC data 
     * streaming has completed and all query results have been recovered.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Correlation processing can potentially interfere with gRPC streaming.</li>
     * <li>Use of correlation parallelism could potentially steal resources from gRPC. </li>
     * <li>This feature should be tuned in consideration with other performance parameters.</li>
     * </ul>
     * </p>
     *   
     * @param bolCorrelateMidstream turn on/off the use of correlation processing while gRPC streaming
     */
    public void setCorrelateMidstream(boolean bolCorrelateMidstream) {
        this.bolCorrelateMidstream = bolCorrelateMidstream;
    }
    
    
    //
    // Operations
    //
    
    
    /**
     * <p>
     * Use a unary gRPC operation to recover the request data.
     * </p>
     * <p>
     * Recovers the query data using a unary gRPC operation blocking until all data is
     * recovered and processed.  Note that all raw query data is processed post recovery.  
     * The only processing option affecting this operation is 
     * <code>{@link #setCorrelationConcurrency(boolean)}</code>. 
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * <ul>
     * <li>
     * This method should be used ONLY for small results sets.
     * The method will fail (with exception) if the data request is larger than the 
     * default gRPC message size.  Use a streaming request for large results sets.
     * </li>
     * <br/>
     * <li>
     * The returned data set is owned by the internal data correlator of this object and will
     * be destroyed whenever a subsequent data request is made.
     * Do not make additional data requests until the returned object is fully processed.
     * </li>
     * </ul>
     * </p>
     * 
     * @param dpRequest data request to be recovered from Query Service and processed
     * 
     * @return  a sorted set (according to start time) of correlated data obtained from given request 
     * 
     * @throws DpQueryException general exception during data recovery and/or processing (see cause)
     */
    public SortedSet<CorrelatedQueryData>   processRequestUnary(DpDataRequest dpRequest) throws DpQueryException {

        // Reset the data correlator
        this.qcdCorrelator.reset();
        
        // Create the data request message
        QueryRequest    msgRequest = dpRequest.buildQueryRequest();
        
        // Perform the unary data request directly on blocking stub
        QueryResponse msgResponse = this.connQuery.getStubBlock().queryResponseSingle(msgRequest);
        
        // Process the request data
        try {
            this.qcdCorrelator.insertQueryResponse(msgResponse);
            
        } catch (OperationNotSupportedException e) {
            if (BOL_LOGGING)
                LOGGER.error("{}: Query request was rejected by Query Service - {}", JavaRuntime.getCallerName(), e.getMessage());
            
            throw new DpQueryException(e);
            
        } catch (CannotProceedException e) {
            if (BOL_LOGGING)
                LOGGER.error("{}: Query response contained error - {}", JavaRuntime.getCallerName(), e.getMessage());
            
            throw new DpQueryException(e);
        }
        
        // Return the processed data
        SortedSet<CorrelatedQueryData> setDataPrcd = this.qcdCorrelator.getProcessedSet();
        
        return setDataPrcd;
    }
    
    /**
     * <p>
     * Use gRPC data streaming to recover the given request.
     * </p>
     * <p>
     * Performs the given request using gRPC data streaming and blocks until request and 
     * processing are complete.  The type of data stream used,
     * either a unidirectional stream or a bidirectional stream, 
     * is determined by the value of 
     * <code>{@link DpDataRequest#getStreamType()}</code>.  
     * Note that performance can be affected by choice of data stream type. 
     * </p>
     * <p> 
     * Various data correlation processing options are available for this method.  See the
     * following methods for more information:
     * <ul>
     * <li><code>{@link #setCorrelateMidstream(boolean)}</code></li>
     * <li><code>{@link #setCorrelationConcurrency(boolean)}</code></li>
     * <li><code>{@link #setMultiStreaming(boolean)}</code></li>
     * </ul>
     * These are all performance options that should be tuned for specific platforms.
     * The default values for these options are set in the Data Platform API configuration
     * (see {@link DpApiConfig}</code>).  
     * </p>
     * <p>
     * <h2>Multi-Streaming</h2>
     * The method attempts to decompose large data requests into composite request according to 
     * settings in the default Data Platform API configuration (see <code>{@link DpApiConfig}</code>).  
     * The <code>{@link #setMultiStreaming(boolean)}</code> operation can be used to turn off 
     * the default behavior.
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * The returned data set is owned by the internal data correlator of this object and will
     * be destroyed whenever a subsequent data request is made.
     * Do not make additional data requests until the returned object is fully processed.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The data request and subsequent data correlation are done in method
     * <code>{@link #processRequestMultiStream(List)}</code>.  If the default multi-streaming
     * option is turned off a list containing the single request <code>dpRequest</code> is
     * passed, otherwise a default query decomposition is attempted.
     * </li>
     * <br/>
     * <li>
     * The default query decomposition attempt is performed by the support method
     * <code>{@link #attemptCompositeRequest(DpDataRequest)}</code>.  If this method
     * fails to find an adequate decomposition (e.g., the request size is too small)
     * it simply returns a list containing only the original request.
     * </li>
     * </ul>
     * </p> 
     *  
     * @param dpRequest data request to be recovered from Query Service and processed
     * 
     * @return  a sorted set (according to start time) of correlated data obtained from given request 
     * 
     * @throws DpQueryException general exception during data recovery and/or processing (see cause)
     * 
     * @see #setCorrelateMidstream(boolean)
     * @see #setCorrelationConcurrency(boolean)
     * @see #setMultiStreaming(boolean)
     */
    public SortedSet<CorrelatedQueryData>   processRequestStream(DpDataRequest dpRequest) throws DpQueryException {

        // Create request list according to multi-streaming configuration
        List<DpDataRequest>     lstRequests;
        
        // If multi-streaming is turned off return original request
        if (!this.bolMultiStream)
            lstRequests = List.of(dpRequest);
        else
            lstRequests = this.attemptCompositeRequest(dpRequest);
        
        // Defer to the multi-streaming processor method
        return this.processRequestMultiStream(lstRequests);
        
//        // Create streaming task for each component request
//        List<QueryResponseStreamProcessor>  lstStrmTasks = this.createStreamingTasks(lstRequests);
//        
//        // Create the data correlation thread task
//        QueryDataProcessor  thdDataProcessor = QueryDataProcessor.newThread(this);
//
//        
//        // Start data processing thread if mid-stream processing true
//        if (this.bolCorrelateMidstream)
//            thdDataProcessor.start();
//        
//        
//        // Start all streaming tasks and wait for completion 
//        int     cntMsgs;    // returns number of response messages streamed
//        
//        try {
//            cntMsgs = this.performDataStreaming(lstStrmTasks);
//            
//        } catch (InterruptedException e) {
//            thdDataProcessor.terminate();
//            
//            if (BOL_LOGGING)
//                LOGGER.error("{}: InterruptedException while streaming request results - {}", e.getMessage());
//            
//            throw new DpQueryException(e);
//
//        } catch (TimeoutException e) {
//            thdDataProcessor.terminate();
//            
//            if (BOL_LOGGING)
//                LOGGER.error("{}: TimeoutException while streaming requst results - {}", JavaRuntime.getCallerName(), e.getMessage());
//            
//            throw new DpQueryException(e);
//
//        } catch (ExecutionException e) {
//            thdDataProcessor.terminate();
//        
//            if (BOL_LOGGING)
//                LOGGER.error("{}: ExecutionException while streaming request results - {}", JavaRuntime.getCallerName(), e.getMessage());
//            
//            throw new DpQueryException(e);
//        }
//        
//        // Return to request results data processing
//        // - Tell the data processor task the number of messages to process
//        // - Start if it has not already been started (i.e., via mid-stream processing)
//        // - Wait for it to finish
//        thdDataProcessor.setMaxMessages(cntMsgs);
//        
//        if (!thdDataProcessor.isStarted())
//            thdDataProcessor.start();
//        
//        try {
//            thdDataProcessor.join(timeoutLimitDefaultMillis());
//            
//        } catch (InterruptedException e) {
//            if (BOL_LOGGING)
//                LOGGER.error("{}: InterruptedException while processing request data - {}", JavaRuntime.getCallerName(), e.getMessage());
//            
//            throw new DpQueryException(e);
//        }
//    
//        // Check for successful processing then return processed data
//        ResultRecord    recProcessed = thdDataProcessor.getResult();
//        if (recProcessed.isFailure()) {
//            if (BOL_LOGGING)
//                LOGGER.error("{}: Data correlation processing FAILED - {}", JavaRuntime.getCallerName(), recProcessed.message());
//
//            throw new DpQueryException("Data correlation processing FAILED: " + recProcessed.message());
//        }
//        
//        SortedSet<CorrelatedQueryData>  setPrcdData = this.qcdCorrelator.getProcessedSet();
//        return setPrcdData;
    }
    
    public SortedSet<CorrelatedQueryData>   processRequestMultiStream(List<DpDataRequest> lstRequests) throws DpQueryException {
        
        // Reset the data correlator
        this.qcdCorrelator.reset();
        
        // Create streaming task for each component request
        List<QueryResponseStreamProcessor>  lstStrmTasks = this.createStreamingTasks(lstRequests);
        
        // Create the data correlation thread task
        QueryDataProcessor  thdDataProcessor = QueryDataProcessor.newThread(this);

        
        // Start data processing thread if mid-stream processing true
        if (this.bolCorrelateMidstream)
            thdDataProcessor.start();
        
        // Start all streaming tasks and wait for completion 
        int     cntMsgs;    // returns number of response messages streamed
        
        try {
            cntMsgs = this.performDataStreaming(lstStrmTasks);
            
        } catch (InterruptedException e) {
            thdDataProcessor.terminate();
            
            if (BOL_LOGGING)
                LOGGER.error("{}: InterruptedException while streaming request results - {}", e.getMessage());
            
            throw new DpQueryException(e);

        } catch (TimeoutException e) {
            thdDataProcessor.terminate();
            
            if (BOL_LOGGING)
                LOGGER.error("{}: TimeoutException while streaming requst results - {}", JavaRuntime.getCallerName(), e.getMessage());
            
            throw new DpQueryException(e);

        } catch (ExecutionException e) {
            thdDataProcessor.terminate();
        
            if (BOL_LOGGING)
                LOGGER.error("{}: ExecutionException while streaming request results - {}", JavaRuntime.getCallerName(), e.getMessage());
            
            throw new DpQueryException(e);
        }
        
        // Return to request results data processing
        // - Tell the data processor task the number of messages to process
        // - Start if it has not already been started (i.e., via mid-stream processing)
        // - Wait for it to finish
        thdDataProcessor.setMaxMessages(cntMsgs);
        
        if (!thdDataProcessor.isStarted())
            thdDataProcessor.start();
        
        try {
            thdDataProcessor.join(timeoutLimitDefaultMillis());
            
        } catch (InterruptedException e) {
            if (BOL_LOGGING)
                LOGGER.error("{}: InterruptedException while processing request data - {}", JavaRuntime.getCallerName(), e.getMessage());
            
            throw new DpQueryException(e);
        }
    
        // Check for successful processing then return processed data
        ResultRecord    recProcessed = thdDataProcessor.getResult();
        if (recProcessed.isFailure()) {
            if (BOL_LOGGING)
                LOGGER.error("{}: Data correlation processing FAILED - {}", JavaRuntime.getCallerName(), recProcessed.message());

            throw new DpQueryException("Data correlation processing FAILED: " + recProcessed.message());
        }
        
        SortedSet<CorrelatedQueryData>  setPrcdData = this.qcdCorrelator.getProcessedSet();
        return setPrcdData;
    }
    
//    public SortedSet<CorrelatedQueryData>  performRequest(DpDataRequest dpRequest) throws InterruptedException, TimeoutException, ExecutionException {
//        
//        // Process with a single data stream if multi-streaming is not active
//        if (!this.bolMultiStream)
//            return processSingleStream(dpRequest);
//
//        // Check if request domain size is large enough to pivot to multi-streaming
//        long szRequest = dpRequest.approxRequestSize(TU_MULTISTREAM_PERIOD);
//        
//        if (szRequest < LNG_MULTISTREAM_PIVOT)
//            return processSingleStream(dpRequest);
//        
//        // Okay - we are doing multiple gRPC streams
//        List<DpDataRequest>     lstCmpRqsts;    // composite request
//        
//        //  - Check if default query domain decomposition will work
//        DpDataRequest.DomainDecomposition recDomain = dpRequest.getDecompositionDefault();
//        
//        if (recDomain.totalCovers() < CNT_MULTISTREAM) {
//            lstCmpRqsts = dpRequest.buildCompositeRequest(recDomain);
//            
//            this.processMultiStream(lstCmpRqsts);
//        }
//        
//        //  - Check if we can decompose by data sources
//        if (dpRequest.getSourceCount() > CNT_MULTISTREAM) {
//            lstCmpRqsts = dpRequest.buildCompositeRequest(CompositeType.HORIZONTAL, CNT_MULTISTREAM);
//        
//            return processMultiStream(lstCmpRqsts);
//        }
//        
//        //  - Decompose vertically in sizes of the pivot number
//        long cntRqsts = szRequest / LNG_MULTISTREAM_PIVOT + ((szRequest % LNG_MULTISTREAM_PIVOT > 0) ? 1 : 0);
//        
//        if (cntRqsts < CNT_MULTISTREAM) {
//            lstCmpRqsts = dpRequest.buildCompositeRequest(CompositeType.VERTICAL, Long.valueOf(cntRqsts).intValue());
//            
//            this.processMultiStream(lstCmpRqsts);
//        }
//        
//        //  - Attempt a grid decomposition
//        if (dpRequest.getSourceCount() > (CNT_MULTISTREAM/2)) {
//            lstCmpRqsts = dpRequest.buildCompositeRequest(CompositeType.GRID, CNT_MULTISTREAM);
//            
//            this.processMultiStream(lstCmpRqsts);
//        }
//        
//        
//        // We cannot find a decomposition - Default back to single stream processing  
//        return this.processSingleStream(dpRequest);
//        
//    }
    
    
    //
    // Support Methods
    //
    
    /*
     * Should make a method to create streaming tasks list, either one or many for multi-streaming.
     * Then the tasks are executed either serially, or simultaneously according to bolCorrelateMidstream.
     */
    
    /**
     * @param dpRequest
     * @return
     */
    private List<DpDataRequest> attemptCompositeRequest(DpDataRequest dpRequest) {
        
        // Check if request size approximation is large enough to pivot to multi-streaming
        long szRequest = dpRequest.approxRequestSamples(1, TU_MULTISTREAM_PERIOD);
        
        if (szRequest < LNG_MULTISTREAM_PIVOT)
            return List.of(dpRequest);
        
        
        // If we are here - we are doing multiple gRPC streams requiring a composite query
        List<DpDataRequest>     lstCmpRqsts;    // composite request to be returned
        
        // Try default query domain decomposition will work
        DpDataRequest.DomainDecomposition recDomain = dpRequest.decomposeDomainDefault();
        
        if (recDomain.totalCovers() < CNT_MULTISTREAM) {
            lstCmpRqsts = dpRequest.buildCompositeRequest(recDomain);
            
            return lstCmpRqsts;
        }
        
        // Try horizontal query domain decomposition (by data sources)
        //  Works when request source count is greater than the stream count
        if (dpRequest.getSourceCount() > CNT_MULTISTREAM) {
            lstCmpRqsts = dpRequest.buildCompositeRequest(CompositeType.HORIZONTAL, CNT_MULTISTREAM);
        
            return lstCmpRqsts;
        }
        
        // Try vertical query domain decomposition (by time domain)
        //  First compute the number of samples per request
        long lngSmpsPerRqst = szRequest / LNG_MULTISTREAM_PIVOT;
        int  cntSmpsPerRqst = Long.valueOf(lngSmpsPerRqst).intValue();
        
        //  Add any remainder
        cntSmpsPerRqst += (szRequest % LNG_MULTISTREAM_PIVOT > 0) ? 1 : 0;
        
        if (cntSmpsPerRqst < CNT_MULTISTREAM) {
            lstCmpRqsts = dpRequest.buildCompositeRequest(CompositeType.VERTICAL, cntSmpsPerRqst);
            
            return lstCmpRqsts;
        }
        
        // Try a grid-based query domain decomposition
        //  Works when the source count is at least half of the stream count
        if (dpRequest.getSourceCount() > (CNT_MULTISTREAM/2)) {
            lstCmpRqsts = dpRequest.buildCompositeRequest(CompositeType.GRID, CNT_MULTISTREAM);
            
            return lstCmpRqsts;
        }
        
        
        // We cannot find any domain decomposition - Default back to single data request
        return List.of(dpRequest);
    }
    
    /**
     * @param lstRequests
     * @return
     */
    private List<QueryResponseStreamProcessor>  createStreamingTasks(List<DpDataRequest> lstRequests) {
        
        // Create the multiple stream processing tasks
        List<QueryResponseStreamProcessor>  lstStrmTasks = new LinkedList<>();
        
        for (DpDataRequest dpRequest : lstRequests) {
            QueryResponseStreamProcessor    taskStrm = QueryResponseStreamProcessor.newTask(
                    dpRequest, 
                    this.connQuery.getStubAsync(), 
                    QueryDataConsumer.newInstance(this)
                    );
            
            lstStrmTasks.add(taskStrm);
        }
        
        return lstStrmTasks;
    }
    
    /**
     * @param lstStrmTasks
     * @return
     * @throws TimeoutException
     * @throws ExecutionException
     */
    private int performDataStreaming(List<QueryResponseStreamProcessor> lstStrmTasks) throws InterruptedException, TimeoutException, ExecutionException {
        
        // Start all data stream tasks and wait for completion
        this.exeThreadPool.invokeAll(lstStrmTasks, CNT_TIMEOUT, TU_TIMEOUT);
        
        // Check for timeout - not all data streams will have completed.
        boolean bolCompleted = lstStrmTasks.stream().allMatch(p -> p.isCompleted());
        
        if (!bolCompleted) {
            String  strMsg = "Time limit expired while gRPC data streaming: " + CNT_TIMEOUT + " " + TU_TIMEOUT;

            throw new TimeoutException(strMsg);
        }

        // Check for streaming error
        boolean bolSuccess = lstStrmTasks.stream().allMatch(p -> p.isSuccess());
        
        if (!bolSuccess) {
            // Get a cause of the streaming error and create message
            ResultRecord recResult = lstStrmTasks
                                    .stream()
                                    .filter(p  -> p.getResult().isFailure())
                                    .<ResultRecord>map(p -> p.getResult())
                                    .findAny()
                                    .get();
            String strErr = recResult.message()+ ( (recResult.hasCause()) ? ", cause=" + recResult.cause() : "");
            String strMsg = "At least one gRPC streaming error: message=" + strErr;  
            
            throw new ExecutionException(strMsg, recResult.cause());
        }
        
        // Recover the total number of messages streamed and return it
        int cntMsgs = lstStrmTasks.stream().mapToInt(p -> p.getResponseCount()).sum();
        
        return cntMsgs;
    }
    
    private SortedSet<CorrelatedQueryData> processSingleStream(DpDataRequest dpRequest) throws InterruptedException, TimeoutException, ExecutionException {

        QueryResponseStreamProcessor    taskStream = QueryResponseStreamProcessor.newTask(
                dpRequest, 
                this.connQuery.getStubAsync(), 
                QueryDataConsumer.newInstance(this)
                );

        Thread              thdStream = new Thread(taskStream);
        QueryDataProcessor  thdProcess = QueryDataProcessor.newThread(this);

        // Stream then correlate
        if (!this.bolCorrelateMidstream) {
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
            thdProcess.start();
            thdProcess.join(timeoutLimitDefaultMillis());
            
            SortedSet<CorrelatedQueryData>  setPrcdData = this.qcdCorrelator.getProcessedSet();
            return setPrcdData;
        }
        
        // Do it concurrently
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
        
        SortedSet<CorrelatedQueryData>  setPrcdData = this.qcdCorrelator.getProcessedSet();
//        SamplingProcess sp = SamplingProcess.from(setPrcdData);
        
        return setPrcdData;
    }
    
    private SortedSet<CorrelatedQueryData> processMultiStream(List<DpDataRequest> lstRequests) throws InterruptedException, TimeoutException, ExecutionException {
        
        // Create the multiple stream processing tasks
        List<QueryResponseStreamProcessor>  lstStrmTasks = new LinkedList<>();
        for (DpDataRequest dpRequest : lstRequests) {
            QueryResponseStreamProcessor    taskStrm = QueryResponseStreamProcessor.newTask(
                    dpRequest, 
                    this.connQuery.getStubAsync(), 
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
        SortedSet<CorrelatedQueryData>  setPrcdData = this.qcdCorrelator.getProcessedSet();
//        SamplingProcess sp = SamplingProcess.from(setPrcdData);
        
        return setPrcdData;
    }
    
    private Thread  newStreamProcessor(DpDataRequest dpRequest) {
        QueryResponseStreamProcessor    taskProcessor = QueryResponseStreamProcessor.newTask(
                dpRequest, 
                this.connQuery.getStubAsync(), 
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
