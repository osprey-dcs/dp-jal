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
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.config.JalConfig;
import com.ospreydcs.dp.api.config.query.JalQueryConfig;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.model.IMessageConsumer;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult.ExceptionalResultStatus;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

/**
 * <p>
 * Class for recovering time-series data requests using a single gRPC Channel and (potentially multiple) gRPC data streams.
 * </p>
 * <p>
 * Class instances utilize a single gRPC Channel object, contained with a <code>{@link DpQueryConnection}</code> object to
 * perform time-series data request recoveries from the Data Platform Query Service.  The <code>QueryChannel</code>
 * class is capable of managing multiple <code>QueryStream</code> instances for expedited data recoveries.  All
 * <code>QueryStream</code> instances are generated from the single gRPC Channel.
 * </p>
 * <p>
 * <h2>Operation</h2>
 * The interface to <code>QueryChannel</code> is intentionally narrow and simple.  There are 3 data recovery operations,
 * 1 unary and 2 streaming operations.
 * <ul>
 * <li><code>{@link #recoverRequestUnary(DpDataRequest)}</code> - a unary gRPC time-series data request recovery.</li>
 * <li><code>{@link #recoverRequest(DpDataRequest)}</code> - a single-streaming time-series data request recovery.</li>
 * <li><code>{@link #recoverRequests(List)}</code> - a multiple-streaming time-series data request recovery.</li>
 * </ul> 
 * These are all blocking operations and do not return until all requested data has been recovered and passed to the
 * message consumer provided at construction.
 * </p>
 * <p> 
 * There are configuration operations for setting timeout limits for the data recovery operations 
 * (default values are used if not explicitly specified).  Recall that these are blocking operations and if the
 * recovery exceeds the timeout the data recovery fails with exception.
 * </p>
 * <p>
 * <h2>gRPC Channel</h2>
 * The <code>DpQueryConnection</code> instance provided at construction must be active and connected to a Data Platform
 * Query Service server.
 * <code>QueryChannel</code> class instances do not assume ownership of the gRPC Channel provided at construction.  That is,
 * the <code>DpQueryConnection</code> object is not owned by any <code>QueryChannel</code> object.  It is only used to
 * create <code>{@link QueryStream}</code> stream instances with finite lifetimes.  Thus, the <code>DpQueryConnection</code>
 * must be shutdown externally when no longer required.
 * </p>
 * <p>
 * <h2>Query Streams</h2>
 * All streaming operations execute on independent threads.  Even the single-stream recovery operation 
 * <code>{@link #recoverRequest(DpDataRequest)}</code> utilizes an independent execution thread for the recovery
 * of request data.  See the class documentation on <code>{@link QueryStream}</code> for additional details.
 * </p>
 * <p>
 * <h2>Message Consumer</h2>
 * All <code>QueryData</code> Protocol Buffers messages for a request operation are passed to the 
 * <code>{@link IMessageConsumer}</code> interface provided at construction.  The performance of the interface 
 * implementation necessary affects the performance of any streaming recovery operation.  If the 
 * <code>IMessageConsumer</code> fills or otherwise blocks during recovery,
 * the independent gRPC streaming tasks will likewise block until the <code>QueryData</code> message is accepted.
 * Thus, choice of <code>IMessageConsumer</code> implementation and operation is important for the upstream operations
 * here.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Jan 9, 2025
 *
 * @see DpQueryConnection
 * @see IMessageConsumer
 * @see QueryStream
 */
public class QueryChannel {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new instance of <code>QueryChannel</code> ready for time-series data request recovery.
     * </p>
     * <p>
     * Creates and returns a new <code>QueryChannel</code> instance ready for time-series data request recovery.
     * Note that all recovery operations are blocking operations.  Timeout limits for streaming recovery operations
     * can be assigned with <code>{@link #setTimeoutLimit(long, TimeUnit)}</code>, otherwise default timeout limits
     * are used.
     * </p> 
     * <p>
     * <h2>gRPC Channel</h2>
     * The <code>DpQueryConnection</code> instance provided here must be active and connected to a Data Platform
     * Query Service server.
     * <code>QueryChannel</code> class instances do not assume ownership of the gRPC Channel provided at construction.  That is,
     * the <code>DpQueryConnection</code> object is not owned by any <code>QueryChannel</code> object.  It is only used to
     * create <code>{@link QueryStream}</code> stream instances with finite lifetimes.  Thus, the given 
     * <code>DpQueryConnection</code> must be shutdown externally when no longer required.
     * </p>
     * <p>
     * <h2>Message Consumer</h2>
     * All <code>QueryData</code> Protocol Buffers messages for a request operation are passed to the 
     * <code>{@link IMessageConsumer}</code> interface provided here.  The performance of the interface 
     * implementation necessary affects the performance of any streaming recovery operation.  If the 
     * <code>IMessageConsumer</code> fills or otherwise blocks during recovery,
     * the independent gRPC streaming tasks will likewise block until the <code>QueryData</code> messages are accepted.
     * Thus, choice of <code>IMessageConsumer</code> implementation and operation is important for the upstream operations
     * here.
     * </p>
     *
     * @param connQuery     enabled connection to the Data Platform Query Service
     * @param snkRspMsgs    consumer of recovered time-series data request data
     * 
     * @return a new <code>QueryChannel</code> object ready for time-series data request recovery
     */
    public static QueryChannel  from(DpQueryConnection connQuery, IMessageConsumer<QueryData> snkRspMsgs) {
        return new QueryChannel(connQuery, snkRspMsgs);
    }
    
    
    //
    // Application Resources
    //
    
    /** The Data Platform Query Service default parameters */
    private static final JalQueryConfig CFG_QUERY = JalConfig.getInstance().query;
    
    
    //
    // Class Constants - Initialized from API configuration
    //
    
    /** Is event logging enabled? */
    public static final boolean     BOL_LOGGING = CFG_QUERY.logging.enabled;
    
    /** Event logging level */
    public static final String      STR_LOGGING_LEVEL = CFG_QUERY.logging.level;
    
    
    /** Is timeout limit enabled ? */
    public static final boolean     BOL_TIMEOUT = CFG_QUERY.timeout.enabled;
    
    /** Timeout limit for query operation */
    public static final long        LNG_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** Timeout unit for query operation */
    public static final TimeUnit    TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    /**
     * <p>
     * Class Resource Initialization - Initializes the event logger, sets logging level.
     * </p>
     */
    static {
        Configurator.setLevel(LOGGER, Level.toLevel(STR_LOGGING_LEVEL, LOGGER.getLevel()));
    }
    
    
    //
    // Initializing Attributes
    //
    
    /** The single connection to the Query Service (used for all data requests) */
    private final DpQueryConnection             connQuery;
    
    /** The consumer of recovered data messages */
    private final IMessageConsumer<QueryData>   snkRspMsgs;

    
    //
    // Instance Resources
    //
    
    /** The pool of (multiple) gRPC streaming thread(s) for recovering requests */
    private final ExecutorService           exeThreadPool = Executors.newCachedThreadPool();
    
    
    //
    //  Configuration
    //
    
    /** Timeout limit to wait for (data recovery) streaming tasks execution */
    private long        lngTimeout = LNG_TIMEOUT;
    
    /** Timeout unit for timeout operation */
    private TimeUnit    tuTimeout = TU_TIMEOUT;
    
    
    //
    // State Variables
    //
    
    /** Activated flag - no longer needed, now all blocking operations */
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
     * @param connQuery     enabled connection to the Data Platform Query Service
     * @param snkRspMsgs    consumer of recovered time-series data request data
     */
    public QueryChannel(DpQueryConnection connQuery, IMessageConsumer<QueryData> snkRspMsgs) {
        this.connQuery = connQuery;
        this.snkRspMsgs = snkRspMsgs;
    }

    
    //
    // Configuration and State
    //
    
    /**
     * <p>
     * Sets the timeout limit for streaming data recovery operations.
     * </p>
     * <p>
     * Sets the maximum time limit for operations <code>{@link #recoverRequests(List)}</code> and
     * <code>{@link #recoverRequests(List)}</code>.  These are blocking operations that will fail
     * if the recovery operation exceeds the given limit.
     * </p>
     * <p>
     * The default values for the given arguments are specified in the Java API Library configuration
     * file and available as the class constants <code>{@link #LNG_TIMEOUT}</code> and
     * <code>{@link #TU_TIMEOUT}</code>, respectively.
     * </p>
     * 
     * @param lngTimeout    the timeout duration for recovery operations
     * @param tuTimeout     the timeout units used for the timeout duration
     */
    public void setTimeoutLimit(long lngTimeout, TimeUnit tuTimeout) {
        this.lngTimeout = lngTimeout;
        this.tuTimeout = tuTimeout;
        
        if (BOL_LOGGING) {
            LOGGER.info("Request recover timeout limit set to {} {}", lngTimeout, tuTimeout);
        }
    }
    
    /**
     * <p>
     * Returns the (unitless) timeout duration for execution of streaming request recoveries.
     * </p>
     * <p>
     * The time units for the timeout duration can be recovered from method <code>{@link #getTimeoutUnit()}</code>.
     * </p>
     * 
     * @return  the timeout duration for all data recovery operations
     */
    public long getTimeoutDuration() {
        return this.lngTimeout;
    }
    
    /**
     * <p>
     * Returns the time units used to express the timeout length returned by <code>{@link #getTimeoutDuration()}</code>.
     * </p>
     * 
     * @return  the timeout units associated with the timeout duration
     */
    public TimeUnit getTimeoutUnit() {
        return this.tuTimeout;
    }
    
    /**
     * <p>
     * Returns the number of <code>QueryData</code> Protocol Buffers messages received during the last streaming 
     * recovery operation.
     * </p>
     * <p>
     * The returned value is obtained from an internal counter which is reset at each invocation to 
     * <code>{@link #recoverRequest(DpDataRequest)}</code> or <code>{@link #recoverRequests(List)}</code>.
     * It should be equal to the value returned by those methods, however, <em>this is an independent calculation</em>.
     * Thus, the returned value here can be compared against the returned value of the above operations for
     * validation checking. 
     * </p>
     * 
     * @return the number of data messages returned in the last streaming data recovery operation
     */
    public int  getReceivedMessageCount() {
        return this.cntMsgsRcvd;
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
     * <code>{@link #enableCorrelateConcurrency(boolean)}</code>. 
     * </p>
     * <p>
     * All data recovery is performed within this method.
     * Due to the nature of unary gRPC requests all request data is recovered first,
     * blocking until the single <code>{@link QueryResponse}</code> data message is returned.
     * The request data is extracted and sent to the <code>IMessageConsumer</code>. 
     * <p>
     * <h2>WARNINGS:</h2>
     * <ul>
     * <li>
     * This method should be used ONLY for small results sets.
     * The method will fail (with exception) if the data request is larger than the 
     * default gRPC message size.  Use a streaming request for large results sets.
     * </li>
     * </ul>
     * </p>
     * 
     * @param dpRequest data request to be recovered from Query Service and processed
     * 
     * @throws DpQueryException general exception during data recovery and/or processing (see cause)
     */
    public void recoverRequestUnary(DpDataRequest dpRequest) throws DpQueryException {

        // Create the data request message
        QueryDataRequest    msgRqst = dpRequest.buildQueryRequest();
        
        // Perform the data request catching any Query Service errors
        try {
            QueryDataResponse   msgRsp = this.connQuery.getStubBlock().queryData(msgRqst);
            
            // Check for response exception
            if (msgRsp.hasExceptionalResult()) {
                String strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                        + " - Query response contained an exception; "
                        + this.extractExceptionInfo(msgRsp);
                
                if (BOL_LOGGING)
                    LOGGER.error(strMsg);
                
                throw new DpQueryException(strMsg);
            }
            
            // Extract response and send to consumer
            QueryData   msgRspData = msgRsp.getQueryData();
            
            this.snkRspMsgs.offer(msgRspData);
            
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
     * Performs the given request using gRPC data stream and blocks until request and 
     * processing are complete.  
     * As a blocking operation it will not return until all requested data is recovered and available
     * from the <code>IMessageConsumer</code> provided at construction.
     * </p>  
     * <p>
     * The type of data stream used, either a unidirectional stream or a bidirectional stream, 
     * is determined by the value of <code>{@link DpDataRequest#getStreamType()}</code> provided
     * within the argument.  
     * Note that performance can be affected by choice of data stream type. 
     * </p>
     * <p> 
     * <h2>Configuration Options</h2>
     * The <code>QueryChannel</code> class instances are no longer configurable.  Use of multiple gRPC data
     * streams is now done explicitly with methods <code>{@link #recoverRequest(DpDataRequest)}</code>
     * and <code>{@link #recoverRequests(List)}</code>.  
     * Correlation has been moved downstream to separate classes.
     * </p>
     * <p>  
     * <s>
     * Various data streaming and processing options are available for this method.  See the
     * following methods for more information:
     * <ul>
     * <li><code>{@link #enableCorrelateWhileStreaming(boolean)}</code></li>
     * <li><code>{@link #enableCorrelateConcurrency(boolean)}</code></li>
     * <li><code>{@link #enableMultiStreaming(boolean)}</code></li>
     * </ul>
     * These are all performance options that should be tuned for specific platforms.
     * The default values for these options are set in the Data Platform API configuration
     * (see {@link JalConfig}</code>).  
     * </p>
     * <p>
     * <h2>Multi-Streaming</h2>
     * The method attempts to decompose large data requests into decompose request according to 
     * settings in the default Data Platform API configuration (see <code>{@link JalConfig}</code>).  
     * The <code>{@link #enableMultiStreaming(boolean)}</code> operation can be used to turn off 
     * the default behavior.
     * </p>
     * </s>
     * <p>
     * All data processing is now done downstream of data recovery.  The following commments
     * no longer apply.
     * </p>
     * <s>
     * <h2>WARNINGS:</h2>
     * The returned data set is owned by the internal data theCorrelator of this object and will
     * be destroyed whenever a subsequent data request is made.
     * Do not make additional data requests until the returned object is fully processed.
     * </s>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method defers to method <code>{@link #processRequestMultiStream(List)}</code> for 
     * the actual data request operation.  
     * </li>
     * <li>
     * Again, <em>this is a blocking operation</em> and does not return until all requested data
     * is recovered and transfered to the <code>{@link IMessageConsumer}</code> provided to the 
     * creator/constructor. 
     * </p> 
     * 
     * @param dpRequest data request to be recovered from Query Service and processed
     * 
     * @return  the number of <code>QueryData</code> messages recovered and passed to the message consumer 
     * 
     * @throws DpQueryException general exception during data recovery (see message and cause)
     * 
     * @see #recoverRequests(List)
     */
    public int recoverRequest(DpDataRequest dpRequest) throws DpQueryException {

        // Create request list and defer to recoverRequest(List)
        List<DpDataRequest>     lstRequests = List.of(dpRequest);
        
        // Defer to the multi-streaming processor method
        return this.recoverRequests(lstRequests);
    }
    
    /**
     * <p>
     * Use multiple gRPC data streams explicitly described by argument list (to recover request data).
     * </p>
     * <p>
     * This method allows clients to explicitly determine the use of concurrent gRPC data streams 
     * (e.g., used by request processors/correlators).
     * </p>
     * <p>
     * This is a blocking operation.  It will not return until all requested data is recovered and available
     * from the <code>IMessageConsumer</code> provided at construction.
     * </p>  
     * <p>
     * A separate gRPC data stream is established for each data request within the argument list and concurrent
     * data streams are used to recover the request data.
     * The number of gRPC data streams is determined by the number of time-series data requests in the argument
     * list; there is no limit.
     * </p>
     * <p>
     * The type of data stream used, either a unidirectional stream or a bidirectional stream, 
     * is determined by the value of <code>{@link DpDataRequest#getStreamType()}</code> provided
     * within the argument.  
     * Note that performance can be affected by choice of data stream type. 
     * </p>
     * <p>
     * <s>
     * At most <code>{@link #CNT_MULTISTREAM}</code> concurrent data streams are enabled at any instant.
     * If the number of data requests in the argument is larger than <code>{@link #CNT_MULTISTREAM}</code>
     * then streaming threads are run in a fixed size thread pool until all requests are completed.
     * </s>
     * </p>
     * <p>
     * The type of data stream used, either a unidirectional stream or a bidirectional stream, 
     * is determined by the value of <code>{@link DpDataRequest#getStreamType()}</code> provided
     * within the argument.  
     * Note that performance can be affected by choice of data stream type. 
     * </p>
     * <p> 
     * <h2>Configuration Options</h2>
     * The <code>QueryChannel</code> class instances are no longer configurable.  Use of multiple gRPC data
     * streams is now done explicitly with methods <code>{@link #recoverRequest(DpDataRequest)}</code>
     * and <code>{@link #recoverRequests(List)}</code>.  
     * Correlation has been moved downstream to separate classes.
     * </p>
     * <p>  
     * <s>
     * Various data streaming and processing options are available for this method.  See the
     * following methods for more information:
     * <ul>
     * <li><code>{@link #enableCorrelateWhileStreaming(boolean)}</code></li>
     * <li><code>{@link #enableCorrelateConcurrency(boolean)}</code></li>
     * <li><code>{@link #enableMultiStreaming(boolean)}</code></li>
     * </ul>
     * Note that the <code>{@link #enableMultiStreaming(boolean)}</code> has no effect here.
     * These performance options should be tuned for specific platforms.
     * The default values for these options are set in the Data Platform API configuration
     * (see {@link JalConfig}</code>).
     * </s>  
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * All recovered data messages will be passed to the <code>IMessageConsumer</code> instance provided at construction.
     * </li>
     * <li>
     * The returned value (returned message count) emphasizes that this is a blocking operation.
     * </li>
     * </ul>
     * </p> 
     *  
     * @param lstRequests   list of data requests, each to be recovered on separate gRPC data streams
     * 
     * @return  the number of <code>QueryData</code> messages recovered and passed to the message consumer 
     * 
     * @throws DpQueryException general exception during data recovery (see message and cause)
     */
    public int recoverRequests(List<DpDataRequest> lstRequests) throws DpQueryException {
        
        // Create streaming task for each component request
        List<QueryStream>  lstStrmTasks = this.createStreamingTasks(lstRequests);
        
        // Start all streaming tasks and wait for completion 
        int     cntMsgs;    // returns number of response messages streamed
        
        try {
            this.cntMsgsRcvd = 0;
            cntMsgs = this.executeStreamingTasks(lstStrmTasks);  // this is a blocking operation
            
            return cntMsgs;
            
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
    // Support Methods
    //
    
    /**
     * <p>
     * Convenience method for creating gRPC stream processing tasks for all elements of the argument.
     * </p>
     * <p>
     * Creates a <code>{@link QueryResponseStreamProcess}</code> task object for each data requests within the 
     * argument.  
     * This method is part of the multi-streaming mechanism within <code>QueryResponseCorrelatorDeprecated</code>.
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
                    this.snkRspMsgs.offer(msgData);
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
     * This method is part of the multi-streaming mechanism within <code>QueryResponseCorrelatorDeprecated</code>.
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
     * <code>{@link QueryDataProcessor}</code> thread (currently enabled or as yet unstarted).
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
        this.exeThreadPool.invokeAll(lstStrmTasks, this.lngTimeout, this.tuTimeout);
        
        // Check for timeout - not all data streams will have completed.
        boolean bolCompleted = lstStrmTasks.stream().allMatch(p -> p.isCompleted());
        
        if (!bolCompleted) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Timeout limit expired while gRPC data streaming: " + LNG_TIMEOUT + " " + TU_TIMEOUT;
            
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

    /**
     * <p>
     * Extract the <code>ExceptionResult</code> from the given message and create a error string.
     * </p>
     * <p>
     * Extracts the exception message from the given argument and creates a string describing the
     * exception.  If not exceptional result is contained in the argument a <code>null</code> value
     * is returned.
     * </p>
     * 
     * @param msgRsp    time-series data request message from Query Service 
     *  
     * @return  string describing the Query Service exception
     */
    private String  extractExceptionInfo(QueryDataResponse msgRsp) {
        
        if (!msgRsp.hasExceptionalResult())
            return null;
        
        ExceptionalResult       msgErr = msgRsp.getExceptionalResult();
        ExceptionalResultStatus enmStatus = msgErr.getExceptionalResultStatus();
        String                  strErrMsg = msgErr.getMessage();
        
        String  strMsg = "Query Service reported exception " + enmStatus + ": " + strErrMsg;
        
        return strMsg;
        
    }
}
