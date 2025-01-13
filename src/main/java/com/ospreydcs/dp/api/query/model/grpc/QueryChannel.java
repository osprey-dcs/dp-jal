/*
 * Project: dp-api-common
 * File:	QueryChannel.java
 * Package: com.ospreydcs.dp.api.query.model.grpc
 * Type: 	QueryChannel
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
 * @since Jan 9, 2025
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.grpc;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.model.IMessageSupplier;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

/**
 * <p>
 * Class for recovering time-series data requests using multiple gRPC data streams.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 9, 2025
 *
 */
public class QueryChannel implements IMessageSupplier<QueryDataResponse.QueryData> {

    
    //
    // Application Resources
    //
    
    /** The Data Platform Query Service default parameters */
    private static final DpQueryConfig CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants - Initialized from API configuration
    //
    
    /** Is logging active? */
    public static final boolean     BOL_LOGGING = CFG_QUERY.logging.active;
    
    
    /** Is timeout limit active ? */
    public static final boolean     BOL_TIMEOUT = CFG_QUERY.timeout.active;
    
    /** Timeout limit for query operation */
    public static final long        CNT_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** Timeout unit for query operation */
    public static final TimeUnit    TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
//    /** Is response multi-streaming active? */
//    public static final boolean     BOL_MULTISTREAM = CFG_QUERY.data.response.multistream.active;
//    
//    /** Maximum number of open data streams to Query Service */
//    public static final int         CNT_MULTISTREAM = CFG_QUERY.data.response.multistream.maxStreams;
//    
//    /** Query domain size triggering multiple streaming (if active) */
//    public static final long        SIZE_DOMAIN_MULTISTREAM = CFG_QUERY.data.response.multistream.sizeDomain;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    //
    // Initializing Attributes
    //
    
    /** The single connection to the Query Service (used for all data requests) */
    private final DpQueryConnection         connQuery;

    
//    //
//    //  Configuration
//    //
//    
//    /** Multi-streaming query response toggle */
//    private boolean bolMultiStream = BOL_MULTISTREAM;
//    
//    /** The request domain size (PV count - time) triggering multi-streaming */
//    private long    szRqstDomain = SIZE_DOMAIN_MULTISTREAM;
//    
//    /** Number of data streams for multi-streaming response recovery */
//    private int     cntMultiStreams = CNT_MULTISTREAM;

    
    //
    // Instance Resources
    //
    
    /** The queue buffering all response messages for data correlation processing */
    private final BlockingQueue<QueryData>  queStreamBuffer = new LinkedBlockingQueue<>();
    
    /** The pool of (multiple) gRPC streaming thread(s) for recovering requests */
    private final ExecutorService           exeThreadPool = Executors.newCachedThreadPool();
    
    
    //
    // State Variables
    //
    
    /** Activated flag */
    private boolean     bolActivated = false;
    
    /** Activation lock */ 
    
    /** Total received message counter */
    private int         cntMsgsRcvd = 0;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryChannel</code>.
     * </p>
     *
     */
    public QueryChannel(DpQueryConnection connQuery) {
        this.connQuery = connQuery;
    }

    
    //
    // Configuration and State
    //
    
//    /**
//     * <p>
//     * Toggles the use of multiple query response gRPC data streams in
//     * <code>{@link #processRequestStream(DpDataRequest)}</code>.
//     * </p>
//     * <p>
//     * The method <code>{@link #processRequestStream(DpDataRequest)}</code>
//     * is capable of using multiple gRPC data streams.  The method attempts to decompose
//     * large data requests into decompose request according to settings in the default
//     * Data Platform API configuration (see <code>{@link DpApiConfig}</code>).  This
//     * operation can turn off the default behavior.
//     * </p>
//     * <p>
//     * This is a performance parameter where a potentially large results set can be recovered
//     * from the Query Service on concurrent gRPC data streams.  Any performance increase is
//     * dependent upon the size of the results set, the network and its current traffic, and
//     * the host platform configuration.
//     * </p> 
//     * <p>
//     * Query data can be recovered using multiple gRPC data streams.  There, the domain of the
//     * data  request is decomposed using the default decompose query mechanism.  The components
//     * of the decompose request are then separate performed on independent gRPC data streams.
//     * The full result set is then assembled from the multiple data streams.
//     * </p>
//     * <p>
//     * The default value is taken from the Java API Library configuration file
//     * (see <code>{@link #BOL_MULTISTREAM}</code>).  This value can be recovered from 
//     * <code>{@link #isMultiStreamingResponse()}</code>.
//     * </p>
//     * <p>
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li>Only affects the operation of <code>{@link #processRequestStream(DpDataRequest)}</code>.</li>
//     * <li>All decompose query configuration parameters are taken from the default API parameters.</li>
//     * <li>All gRPC multi-streaming parameters are taken from the default API parameters.</li>
//     * </ul>
//     * </p>  
//     * 
//     * @param bolMultiStream    turn on/off the use of multiple gRPC streams for query response recovery
//     */
//    public void setMultiStreamingResponse(boolean bolMultiStream) {
//        this.bolMultiStream = bolMultiStream;
//    }
//    
//    /**
//     * <p>
//     * Sets the request domain size threshold to trigger multi-streaming of request results.
//     * </p>
//     * <p>
//     * If the multi-streaming feature is turned on (see <code>{@link #isMultiStreamingResponse()}</code>)
//     * it will only be triggered if the request domain size is greater than the given value.
//     * The approximate domain size of a request is given by the method 
//     * <code>{@link DpDataRequest#approxDomainSize()}</code>.  
//     * </p>
//     * <p>
//     * Requiring that data requests have a given domain size is a performance issue and the given value is
//     * thus a performance parameter.  The ideal is to limit the use of multiple, concurrent data streams for
//     * large requests.  Creating multiple gRPC data streams for a small request can usurp unnecessary 
//     * resources.
//     * </p>
//     *  
//     * @param szDomain  the request domain size (in source-count seconds) threshold where multis-treaming is triggered
//     */
//    public void setMultiStreamingDomainSize(long szDomain) {
//        this.szRqstDomain = szDomain;
//    }
//    
//    /**
//     * <p>
//     * Sets the number of gRPC data stream used for multi-streaming query responses.
//     * </p>
//     * <p>
//     * This method should be called before any query response processing has started, if at all.  The default value
//     * in the Java API Library configuration file should be used as a matter of course.  However, this method is
//     * available for performance evaluations. 
//     * </p>
//     * 
//     * @param cntMultiStreams    number of gRPC data streams to use in multi-streaming data request recovery
//     * 
//     * @throws IllegalStateException    method called while actively processing
//     */
//    synchronized 
//    public void setMultiStreamCount(int cntStreams) throws IllegalStateException {
////        if (this.theCorrelator.sizeCorrelatedSet() > 0 || (this.thdDataProcessor!=null && this.thdDataProcessor.isStarted())) {
//        if (this.thdDataProcessor!=null && this.thdDataProcessor.isStarted()) {
//            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Cannot change stream count while processing.";
//            
//            if (BOL_LOGGING)
//                LOGGER.warn(strMsg);
//            
//            throw new IllegalStateException(strMsg);
//        }
//        
//        this.cntMultiStreams = cntStreams;
//    }
//    
//    
//    /**
//     * <p>
//     * Specifies whether or not multi-streaming of query responses is enabled.
//     * </p>
//     * <p>
//     * The default value is taken from the Java API Library configuration file
//     * (see {@link #BOL_MULTISTREAM}).
//     * </p>
//     * 
//     * @return <code>true</code> if multi-streaming is enabled, <code>false</code> if disabled
//     * 
//     * @see #BOL_MULTISTREAM
//     */
//    public final boolean isMultiStreamingResponse() {
//        return this.bolMultiStream;
//    }
//
//    /**
//     * <p>
//     * Returns the minimum request domain size which triggers query response multi-streaming.
//     * </p>
//     * <p>
//     * The returned value has context only if query response multi-streaming has context, specifically,
//     * <code>{@link #isMultiStreamingResponse()}</code> returns <code>true</code>.  Otherwise a single
//     * response stream is always used.  If multi-streaming responses are enabled then multiple gRPC
//     * data streams will only be used if the returned value is greater than the size of the 
//     * query request domain, which is given by <code>{@link DpDataRequest#approxDomainSize()}</code>.
//     * </p>
//     * <p>
//     * The requirement for a minimum domain size is used as a performance criterion.  Minimum domain
//     * sizes prevent the use of multiple gRPC data streams for small data requests, preventing unnecessary 
//     * resource allocation.
//     * </p>
//     * <p>
//     * The default value is taken from the Java API Library configuration file
//     * (see {@link #SIZE_DOMAIN_MULTISTREAM}).
//     * </p>
//     * 
//     * @return the minimum query domain size triggering multi-streaming of response data (in data source * seconds)
//     */
//    public final long geMultiStreamingDomainSize() {
//        return this.szRqstDomain;
//    }
//
//    /**
//     * <p>
//     * Returns the number of gRPC data streams used for multi-streaming of query responses.
//     * </p>
//     * <p>
//     * The returned value is the number of gRPC data streams always used for a multi-streaming response.
//     * Note that the returned value has context only when multi-streaming is enabled, specificially,
//     * <code>{@link #isMultiStreamingResponse()}</code> returns <code>true</code>.
//     * </p>
//     * 
//     * @return the number of concurrent gRPC data streams used to recover query responses when enabled
//     */
//    public final int getMultiStreamCount() {
//        return this.cntMultiStreams;
//    }
    
    
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
     * All data recovery and subsequent correlation is performed within this method.
     * Due to the nature of unary gRPC requests all request data is recovered first,
     * blocking until the single <code>{@link QueryResponse}</code> data message is returned.
     * The request data is extracted and then correlated to produce the returned data set. 
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
     * The returned data set is owned by the internal data theCorrelator of this object and will
     * be destroyed whenever a subsequent data request is made.
     * Do not make additional data requests until the returned object is fully processed.
     * </li>
     * </ul>
     * </p>
     * 
     * @param dpRequest data request to be recovered from Query Service and processed
     * 
     * @throws DpQueryException general exception during data recovery and/or processing (see cause)
     */
    public void recoveryRequestUnary(DpDataRequest dpRequest) throws DpQueryException {

        // Create the data request message
        QueryDataRequest    msgRqst = dpRequest.buildQueryRequest();
        
        // Perform the data request catching any Query Service errors
        try {
            QueryDataResponse   msgRsp = this.connQuery.getStubBlock().queryData(msgRqst);
            
            this.queStreamBuffer.put(msgRsp.getQueryData());
            
        } catch (Exception e) {
            if (BOL_LOGGING)
                LOGGER.error("{} - Query request threw {} exception: {}", JavaRuntime.getMethodName(),e.getClass().getSimpleName(), e.getMessage());
            
            throw new DpQueryException(e);
        }
    }
    
    /**
     * <p>
     * Use gRPC data streaming to recover the given request.
     * </p>
     * <p>
     * Performs the given request using gRPC data streaming and blocks until request and 
     * processing are complete.  The given data request is potentially decomposed to create multiple
     * gRPC data streams unless multi-streaming is explicitly turned off using
     * <code>{@link #setMultiStreamingResponse(boolean)}</code> before calling this method.
     * </p>
     * <p>
     * The type of data stream used, either a unidirectional stream or a bidirectional stream, 
     * is determined by the value of <code>{@link DpDataRequest#getStreamType()}</code> provided
     * within the argument.  
     * Note that performance can be affected by choice of data stream type. 
     * </p>
     * <p> 
     * <h2>Configuration Options</h2>
     * Various data streaming and processing options are available for this method.  See the
     * following methods for more information:
     * <ul>
     * <li><code>{@link #setCorrelateWhileStreaming(boolean)}</code></li>
     * <li><code>{@link #setCorrelationConcurrency(boolean)}</code></li>
     * <li><code>{@link #setMultiStreamingResponse(boolean)}</code></li>
     * </ul>
     * These are all performance options that should be tuned for specific platforms.
     * The default values for these options are set in the Data Platform API configuration
     * (see {@link DpApiConfig}</code>).  
     * </p>
     * <p>
     * <h2>Multi-Streaming</h2>
     * The method attempts to decompose large data requests into decompose request according to 
     * settings in the default Data Platform API configuration (see <code>{@link DpApiConfig}</code>).  
     * The <code>{@link #setMultiStreamingResponse(boolean)}</code> operation can be used to turn off 
     * the default behavior.
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * The returned data set is owned by the internal data theCorrelator of this object and will
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
     * <code>{@link #attemptRequestDecomposition(DpDataRequest)}</code>.  If this method
     * fails to find an adequate decomposition (e.g., the request size is too small)
     * it simply returns a list containing only the original request.
     * </li>
     * <br/>
     * <li>
     * The sorted set of correlated request data returned here should be fully processed, or copied,
     * before invoking another data request.  This instance retains ownership of the returned data set and
     * will destroy it to provide resources for the next data request.
     * </ul>
     * </p> 
     *  
     * @param dpRequest data request to be recovered from Query Service and processed
     * 
     * @throws DpQueryException general exception during data recovery and/or processing (see cause)
     */
    public void recoverRequest(DpDataRequest dpRequest) throws DpQueryException {

        // Create request list and defer to recoverRequest(List)
        List<DpDataRequest>     lstRequests = List.of(dpRequest);
        
        // Defer to the multi-streaming processor method
        this.recoverRequest(lstRequests);
    }
    
    /**
     * <p>
     * Use multiple gRPC data streams explicitly described by argument list (to recover request data).
     * </p>
     * <p>
     * This method allows clients to explicitly determine the concurrent gRPC data streams used by the
     * <code>QueryResponseProcessor</code>.  To use the default multi-streaming mechanism method
     * <code>{@link #processRequestStream(DpDataRequest)}</code> is available.
     * <p>
     * A separate gRPC data stream is established for each data request within the argument list and concurrent
     * data streams are used to recover the request data.
     * At most <code>{@link #CNT_MULTISTREAM}</code> concurrent data streams are active at any instant.
     * If the number of data requests in the argument is larger than <code>{@link #CNT_MULTISTREAM}</code>
     * then streaming threads are run in a fixed size thread pool until all requests are completed.
     * </p>
     * <p>
     * The type of data stream used, either a unidirectional stream or a bidirectional stream, 
     * is determined by the value of <code>{@link DpDataRequest#getStreamType()}</code> provided
     * within the argument.  
     * Note that performance can be affected by choice of data stream type. 
     * </p>
     * <p> 
     * <h2>Configuration Options</h2>
     * Various data streaming and processing options are available for this method.  See the
     * following methods for more information:
     * <ul>
     * <li><code>{@link #setCorrelateWhileStreaming(boolean)}</code></li>
     * <li><code>{@link #setCorrelationConcurrency(boolean)}</code></li>
     * <li><code>{@link #setMultiStreamingResponse(boolean)}</code></li>
     * </ul>
     * Note that the <code>{@link #setMultiStreamingResponse(boolean)}</code> has no effect here.
     * These performance options should be tuned for specific platforms.
     * The default values for these options are set in the Data Platform API configuration
     * (see {@link DpApiConfig}</code>).  
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The sorted set of correlated request data returned here should be fully processed, or copied,
     * before invoking another data request.  This instance retains ownership of the returned data set and
     * will destroy it to provide resources for the next data request.
     * </ul>
     * </p> 
     *  
     * @param lstRequests   list of data requests, each to be recovered on separate gRPC data streams
     * 
     * @return  a sorted set (according to start time) of correlated data obtained from given request 
     * 
     * @throws DpQueryException general exception during data recovery and/or processing (see cause)
     * 
     * @see #setCorrelateWhileStreaming(boolean)
     * @see #setCorrelationConcurrency(boolean)
     */
    public void recoverRequest(List<DpDataRequest> lstRequests) throws DpQueryException {
        
        // Create streaming task for each component request
        List<QueryStream>  lstStrmTasks = this.createStreamingTasks(lstRequests);
        
        // Start all streaming tasks and wait for completion 
        int     cntMsgs;    // returns number of response messages streamed
        
        try {
            cntMsgs = this.executeStreamingTasks(lstStrmTasks);  // this is a blocking operation
            
        } catch (InterruptedException e) {

            if (BOL_LOGGING)
                LOGGER.error("{}: InterruptedException while streaming request results - {}", JavaRuntime.getMethodName(), e.getMessage());
            
            throw new DpQueryException(e);

        } catch (TimeoutException e) {
            
            if (BOL_LOGGING)
                LOGGER.error("{}: TimeoutException while streaming requst results - {}", JavaRuntime.getMethodName(), e.getMessage());
            
            throw new DpQueryException(e);

        } catch (CompletionException e) {
        
            if (BOL_LOGGING)
                LOGGER.error("{}: ExecutionException while streaming request results - {}", JavaRuntime.getMethodName(), e.getMessage());
            
            throw new DpQueryException(e);
        }
    }
    
    
    //
    // IMessageSupplier<QueryDataResponse.QueryData> Interface
    //

    /**
     * @see com.ospreydcs.dp.api.model.IMessageSupplier#isSupplying()
     */
    @Override
    public boolean isSupplying() {
        return !this.queStreamBuffer.isEmpty();
    }

    /**
     * @see com.ospreydcs.dp.api.model.IMessageSupplier#take()
     */
    @Override
    public QueryData take() throws IllegalStateException, InterruptedException {
        if (!this.isSupplying()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Attempt to acquire from a non-supplying supplier.";
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new IllegalStateException(strMsg);
        }
        
        return this.queStreamBuffer.take();
    }

    /**
     * @see com.ospreydcs.dp.api.model.IMessageSupplier#poll()
     */
    @Override
    public QueryData poll() throws IllegalStateException {
        if (!this.isSupplying()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Attempt to acquire from a non-supplying supplier.";
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new IllegalStateException(strMsg);
        }
        
        return this.queStreamBuffer.poll();
    }

    /**
     * @see com.ospreydcs.dp.api.model.IMessageSupplier#poll(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public QueryData poll(long cntTimeout, TimeUnit tuTimeout) throws IllegalStateException, InterruptedException {
        if (!this.isSupplying()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Attempt to acquire from a non-supplying supplier.";
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new IllegalStateException(strMsg);
        }
        
        return this.queStreamBuffer.poll(cntTimeout, tuTimeout);
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Convenience method for creating gRPC stream processing tasks for all elements of the argument.
     * </p>
     * <p>
     * Creates a <code>{@link QueryResponseStreamProcess}</code> task object for each data requests within the 
     * argument.  
     * This method is part of the multi-streaming mechanism within <code>QueryResponseCorrelator</code>.
     * </p>
     * <p>
     * Each task object returned has the following properties:
     * <ul>
     * <li>Performs the data request on an independent gRPC data stream whose type is specified by the 
     * stream type preference <code>{@link DpDataRequest#getStreamType()}</code>.
     * </li>
     * <br/>  
     * <li>
     * Uses the <code>{@link #connQuery}</code> connection attribute (provided at construction) to create the
     * gRPC data stream.
     * </li>
     * <br/>
     * <li>
     * Forwards all data recovered by the gRPC stream to the <code>{@link #queStreamBuffer}</code> attribute
     * collecting ALL request data from ALL streams.  Specifically, a <code>{@link QueryDataConsumer}</code>
     * object is created for the Query Service <code>{@link QueryResponse}</code> message forwarding.
     * </li>
     * </ul>  
     * </p>
     * 
     * @param lstRequests   decompose data request, one request for each data stream
     * 
     * @return  collection of gRPC stream processor tasks suitable for thread execution
     */
    private List<QueryStream>  createStreamingTasks(List<DpDataRequest> lstRequests) {
        
        // Container for multiple stream processing tasks
        List<QueryStream>  lstStrmTasks = new LinkedList<>();
        
        // Create a gRPC stream processor data for each component request within the argument
        for (DpDataRequest dpRequest : lstRequests) {
            DpQueryServiceStub      stubAsync = this.connQuery.getStubAsync();
            Consumer<QueryData>     fncConsumer = (msgData) -> {
                try {
                    this.queStreamBuffer.put(msgData);
                    this.cntMsgsRcvd++;
                    
                } catch (InterruptedException e) {
                    if (BOL_LOGGING)
                        LOGGER.error("{} - Failed to added QueryData message {} to queue.", JavaRuntime.getQualifiedMethodNameSimple(), this.cntMsgsRcvd);
                }
            };
            
            QueryStream    taskStrm = QueryStream.from(
                    dpRequest,
                    stubAsync, 
                    fncConsumer
                    );
            
            lstStrmTasks.add(taskStrm);
        }
        
        return lstStrmTasks;
    }
    
    /**
     * <p>
     * Concurrently executes all data request streaming tasks within the argument.
     * </p>
     * <p>
     * Executes all data streaming tasks within the argument with the maximum concurrency allowed by the
     * fixed-size thread pool executor <code>{@link #exeThreadPool}</code>.  Returns the number of
     * <code>{@link QueryResponse}</code> messages successfully recovered by ALL data streams.
     * This method is part of the multi-streaming mechanism within <code>QueryResponseCorrelator</code>.
     * </p>
     * <p>
     * Under normal operation, this method blocks until all streaming tasks within the argument have completed,
     * after which it returns the total number of Query Service response messages recovered. 
     * After successful thread pool execution (i.e., neither interrupted externally or timed out while waiting
     * for completion), the streaming tasks are all checked for successful completion.  If any one task failed
     * to complete normally (e.g., its data request was rejected or failed to recover all data), an
     * <code>{@link CompletionException}</code> is thrown.
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The returned value is used to set the number of response messages to be correlated by any 
     * <code>{@link QueryDataProcessor}</code> thread (currently active or as yet unstarted).
     * </li>
     * </ul>
     * </p>
     * 
     * @param lstStrmTasks collection of data request streaming tasks to be executed concurrently
     * 
     * @return the number of <code>QueryResponse</code> messages recovered by ALL data stream tasks
     * 
     * @throws InterruptedException thread pool execution was interrupted externally
     * @throws TimeoutException     thread pool execution exceeded default timeout limit
     * @throws CompletionException  at least one streaming operation did not complete successfully (see message)
     */
    private int executeStreamingTasks(List<QueryStream> lstStrmTasks) 
            throws InterruptedException, TimeoutException, CompletionException {
        
        // Start all data stream tasks and wait for completion
        this.exeThreadPool.invokeAll(lstStrmTasks, CNT_TIMEOUT, TU_TIMEOUT);
        
        // Check for timeout - not all data streams will have completed.
        boolean bolCompleted = lstStrmTasks.stream().allMatch(p -> p.isCompleted());
        
        if (!bolCompleted) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Timeout limit expired while gRPC data streaming: " + CNT_TIMEOUT + " " + TU_TIMEOUT;
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);

            throw new TimeoutException(strMsg);
        }

        // Check for streaming error
        boolean bolSuccess = lstStrmTasks.stream().allMatch(p -> p.isSuccess());
        
        if (!bolSuccess) {
            // Get a cause of the streaming error and create message
            ResultStatus recResult = lstStrmTasks
                                    .stream()
                                    .filter(p  -> p.getResult().isFailure())
                                    .<ResultStatus>map(p -> p.getResult())
                                    .findAny()
                                    .get();
            String strErr = recResult.message()+ ( (recResult.hasCause()) ? ", cause=" + recResult.cause() : "");
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple()  
                    + " - At least one gRPC streaming error: message="  
                    + strErr;  
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new CompletionException(strMsg, recResult.cause());
        }
        
        // Recover the total number of messages streamed and return it
        int cntMsgs = lstStrmTasks.stream().mapToInt(p -> p.getResponseCount()).sum();
        
        return cntMsgs;
    }

}
