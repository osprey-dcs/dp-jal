/*
 * Project: dp-api-common
 * File:	QueryResponseCorrelatorDeprecated.java
 * Package: com.ospreydcs.dp.api.query.model.grpc
 * Type: 	QueryResponseCorrelatorDeprecated
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
package com.ospreydcs.dp.api.query.model.correl;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.SortedSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.naming.CannotProceedException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.model.coalesce.SamplingProcess;
import com.ospreydcs.dp.api.query.model.grpc.QueryStream;
import com.ospreydcs.dp.api.query.model.request.RequestDecompParams;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.query.model.request.DataRequestDecomposer;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

/**
 * <p>
 * Class for performing Query Service data requests and correlation of request data.
 * </p>
 * <p>
 * <code>QueryResponseCorrelatorDeprecated</code> objects performs Data Platform Query Service data requests embodied with
 * <code>{@link DpDataRequest}</code> objects, then correlate the query responses into sorted sets of 
 * <code>{@link CorrelatedQueryDataOld}</code> objects. That is, class objects take inputs of type 
 * <code>DpDataRequest</code> and then output <code>{@link SortedSet}</code> collections containing the
 * correlated results sets.  All gRPC data streaming and correlation processing are performed internally.
 * </p>
 * <p>
 * The motivation for combining request data recovery and data correlation operations is performance.  For large
 * data requests gRPC streaming operation can extend over significant real-time.  By starting the data correlation
 * processing on the request data that is currently available, the overall data recovery and correlation time 
 * can be reduced.  However, it is important that the data correlation operation does not interfere with the
 * gRPC streaming, for example, by allocating too many CPU resources.
 * </p>
 * <p>
 * <h2>Query Requests</h2>
 * Within the Query Service API library, data requests are represented by <code>{@link DpDataRequest}</code> 
 * objects.  These objects contain the defining parameters for a Query Service data request, then transform the 
 * client request into <code>{@QueryDataRequest}</code> Protobuf messages recognizable by the Query Service gRPC 
 * interface.
 * </p>
 * <p>
 * All supported Query Service data requests within <code>QueryResponseCorrelatorDeprecated</code> are implemented by 
 * methods prefixed with <code>processRequest</code>.  The method suffix methods indicates the gRPC technique
 * used to recover the request data (i.e. "Unary", "Stream", etc.). All data request methods return sorted sets of
 * <code>{@CorrelatedQueryDataOld}</code> objects containing the results set of the offered data query request.
 * The sorted set contains all requested time-series data, correlated by single sampling clock, and sorted by
 * sampling clock start timestamp.
 * </p>
 * <p>
 * Once a <code>{@link SortedSet}</code> of <code>{@link CorrelatedQueryDataOld}</code> objects is obtained from
 * a request method, it is ready for subsequent processing of the results set.  Each <code>CorrelatedQueryDataOld</code>
 * object contains a sampling clock Protobuf message and all the data column (time-series) messages correlated
 * to that sampling clock.  
 * The class <code>{@link SamplingProcess}</code> is available for subsequent processing of request method outputs
 * into table-like containers for the results set, however, clients may implement their own final processing.
 * </p>
 * <p>
 * <h2>Final Processing</h2>
 * It is important to note that clients of the <code>QueryResponseCorrelatorDeprecated</code> data request methods must 
 * perform all final processing of the correlated output BEFORE invoking another data request.  Correlated data
 * output sets are <em>owned</em> by the <code>QueryResponseCorrelatorDeprecated</code> object performing the request.
 * Existing correlated data output sets will be destroyed (i.e., cleared by the internal 
 * <code>{@link QueryDataCorrelatorOld}</code> instance) whenever a subsequent data request is performed.
 * Either copy the output data to a new container or fully process the output sets before invoking additional
 * data requests.   
 * </p>
 * <p>
 * <h2>Streaming/Processing Configuration</h2>
 * For performance considerations, the streaming and processing operations within a 
 * <code>QueryResponseCorrelatorDeprecated</code> object are configurable.  Optimal configurations are determined by
 * the hosting platform and the types (e.g., sizes) of the typical data request.  
 * The default configuration is obtained from the Data Platform API Query Service configuration within 
 * <code>{@link DpApiConfig}</code>.
 * </p>
 * <p>
 * There are several methods that can be used to modify the default streaming/processing configuration of a 
 * <code>QueryResponseCorrelatorDeprecated</code> object.  All configuration methods are prefixed with <code>set</code>
 * and suffixed by the action they perform.
 * <ul>
 * <li>
 * <code>{@link #setMultiStreamingResponse(boolean)}</code>: Toggles the use of multiple gRPC data streams for recovering
 * Query Service request data. Query requests are decomposed for multi-streaming via an internal algorithm. 
 * </li>
 * <br/>
 * <li>
 * <code>{@link #setCorrelationConcurrency(boolean)}</code>: Toggles the use of concurrent, multi-threaded processing 
 * for the results set data correlation.  Parallelism improves correlation performance for large request sets but
 * can also sequester CPU resources.  
 * </li>
 * <br/>
 * <li>
 * <code>{@link #setCorrelateWhileStreaming(boolean)}</code>: Toggles the function of performing data correlation while
 * concurrently data streaming; otherwise data is correlated separately, after gRPC data stream completion.
 * </li>
 * </ul>
 * In each case see the method documentation for details of the operation and its effects.
 * </p>
 * <p>
 * <h2>Query Service Connection</h2>
 * A single <code>{@link DpQueryConnection}</code> object is required by a <code>QueryResponseCorrelatorDeprecated</code>
 * objects, which is provided at construction.  The <code>DpQueryConnection</code> object contains the gRPC 
 * channel connection to the Data Platform Query Service used for all gRPC data query and data streaming 
 * operations. <code>QueryResponseCorrelatorDeprecated</code> objects DO NOT take ownership of the Query Service connection.
 * (Ownership is assumed to be that of the client using the <code>QueryResponseCorrelatorDeprecated</code> object.)
 * Thus, the <code>DpQueryConnection</code> service connection is NOT shutdown here.  
 * </p>
 * <p>
 * <h2>gRPC Unary Requests</h2>
 * Unary gRPC requests (i.e., standard "RPC" invocation) are available with the method 
 * <code>{@link #processRequestUnary(DpDataRequest)}</code>.  Unary requests are simple and efficient for small
 * data requests as they accommodate single message exchanges.  However, they should be used for ONLY for data 
 * requests where small results sets are expected.  For large results sets always use a gRPC streaming operation.
 * </p>
 * <p>
 * If a results set is larger than the current gRPC message size limit, a unary request operation will fail with
 * an exception.  The default message size limit for gRPC is 4 MBytes, however, this value is configurable 
 * within the Data Platform API default configuration for the Query Service connection.  (Note, however, that
 * the Query Service must also be explicitly configured for message size limits greater than the gRPC default 
 * value.)  
 * <p>
 * <h2>gRPC Data Streaming</h2>
 * The gRPC data streaming operations should always be used whenever large results sets are expected.  Currently,
 * the following methods implement gRPC data streaming:
 * <ul>
 * <li>
 * <code>{@link #processRequestStream(DpDataRequest)}</code> - standard gRPC data streaming request method.
 * Attempts to recovery the query result set using multiple data streams. The request defined in the argument 
 * is decomposed into smaller decompose requests, each recovered on a separate gRPC data stream.  
 * The preferred request decomposition is used 
 * (see <code>{@link DataRequestDecomposer#decomposeDomainPreferred(DpDataRequest)}</code>) unless otherwise
 * specified.  Multiple response streams are only triggered if the request domain size >= the minimum domain size,
 * or <code>{@link DpDataRequest#approxDomainSize()} >= {@link #geMultiStreamingDomainSize()}</code>. 
 * </li>
 * <br/>
 * <li>
 * <code>{@link #processRequestMultiStream(List)}</code> - Explicitly multi-streaming of data requests according
 * to client-provided argument list.  The client supplies a list of data requests each of which are recovered
 * on a separate data stream.
 * </li>
 * </ul>
 * See the documentation for each method for further details.
 * </p>
 * <p>
 * Regardless of the above streaming request method used, the number of concurrent data streams will always
 * be limited by the value of constant <code>{@link #CNT_MULTISTREAM}</code>.  If a client explicitly requests
 * a stream count larger than that value (i.e., with <code>{@link #processRequestMultiStream(List)}</code>),
 * the multiple requests will still be streamed independently on separate thread, but no more than
 * <code>{@link #CNT_MULTISTREAM}</code> threads will be enabled any any given instant.  The maximum number of
 * data streams can be set using the <code>{@link DpApiConfig}</code> default API configuration.  It is a 
 * tuning parameter dependent upon gRPC, the number of platform processor cores, and local network traffic.    
 * </p>
 * <p>
 * The <em>type</em> of data stream used for the above request can be specified by the client using the 
 * <code>{@link DpDataRequest#setStreamType(com.com.ospreydcs.dp.api.common.DpGrpcStreamType)}</code> method
 * within the data request object.  See the documentation for the method for further details on available gRPC 
 * data stream types (or see enumeration <code>{@link com.com.ospreydcs.dp.api.common.DpGrpcStreamType}</code>.
 * (This is a performance option.)
 * </p>
 * <p>
 * <h2>Data Correlation</h2>
 * All data correlation of the incoming Query Service data is performed by a single 
 * <code>{@link QueryDataCorrelatorOld}</code> instance within each <code>QueryResponseCorrelatorDeprecated</code> object.
 * The <code>QueryDataCorrelatorOld</code> attribute is used to correlate all data, regardless of recovery method
 * (e.g., unary request, streaming request, etc.).  The <code>QueryDataCorrelatorOld</code> attribute is reused,
 * that is, the same instance is used for all data requests performed while the <code>QueryResponseCorrelatorDeprecated</code>
 * is alive.
 * </p>
 * <p>
 * Consequently, and because <code>QueryDataCorrelatorOld</code> objects own their processed data sets, output data
 * sets are destroyed whenever a new data request is performed (i.e., using a <code>processRequest...</code> 
 * method.  As previously mentioned, any sorted set of correlated output data MUST be fully processed or copied
 * before invoking another data request. 
 * </p>
 * <p>
 * See the class documentation for <code>{@link QueryDataCorrelatorOld}</code> concerning details about the data
 * correlation process and the resulting correlated data sets it produces.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 5, 2024
 *
 * @see DpDataRequest
 * @see CorrelatedQueryDataOld
 * @see QueryStream
 * @see QueryDataCorrelatorOld
 * 
 * @deprecated  Replaced by QueryRequestProcessorOld
 */
@Deprecated(since="Jan 15, 2024")
public class QueryResponseCorrelatorDeprecated {
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new <code>QueryResponseCorrelatorDeprecated</code> instance connected to the given Data Platform Query
     * Service.
     * <p>
     * The returned <code>QueryResponseCorrelatorDeprecated</code> instance is ready for the recovery of data requests.
     * No shutdown operations are required as the instance does not take ownership of the argument.
     * See the class documentation <code>{@link QueryResponseCorrelatorDeprecated}</code> for instructions and use of the
     * returned object.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The Query Service connection object is provided to the <code>QueryResponseCorrelatorDeprecated</code>
     * at construction.  This connection is used for all subsequent data request gRPC operations.
     * Since a single gRPC <code>{@link Channel}</code> object can support multiple concurrent data streams, 
     * only one connection is needed. 
     * </li>
     * <br/>
     * <li>
     * The <code>QueryReponseCorrelator</code> instance DOES NOT take ownership of the argument.  No 
     * shutdown operations are performed on the connection here.
     * </li>
     * <br/>
     * <li>
     * All data request processing methods (i.e., methods prefixed with <code>processRequest</code>) do not return 
     * until the request is fully processed.  Thus, multiple <code>{@link Channel}</code> instances are not 
     * required.
     * </li>
     * <br/>
     * <li>
     * Be sure to fully process or copy all processed results sets returned from the data request methods BEFORE
     * invoking additional data requests.  Correlated output data sets are owned by this instance and are
     * destroyed whenever additional data requests are invoked.
     * </li>
     * </ul>
     * </p>
     *  
     * @param connQuery connection to the Data Platform Query Service 
     * 
     * @return          new <code>QueryResponseCorrelatorDeprecated</code> ready for request and processing operations
     */
    public static QueryResponseCorrelatorDeprecated   from(DpQueryConnection connQuery) {
        return new QueryResponseCorrelatorDeprecated(connQuery);
    }
    
    
    //
    // Class Types
    //
    
    /**
     * <p>
     * Data consumer operation for <code>{@link QueryStream}</code> objects.
     * </p>
     * <p>
     * Implementation of <code>{@link Consumer}</code> interface bound to the 
     * <code>{@link QueryData}</code> Protobuf message type.
     * Transfers request data from a gRPC data stream implemented by 
     * <code>{@link QueryStream}</code> to a data buffer of type 
     * <code>{@link Queue}</code collecting response data.
     * </p>
     * <p> 
     * The <code>{@link #accept(QueryData)}</code> method of this class simply transfers a 
     * <code>BucketData</code> message to a <code>{@link Queue}</code> reference, assumed 
     * to be the data buffer of a <code>{@link QueryResponseCorrelatorDeprecated}</code> object.
     * The current number of <code>BucketData</code> messages transferred is available through 
     * the <code>{@link #getMessageCount()}</code> method.
     * </p>
     * <p>
     * Instances are intended to be passed to newly created 
     * <code>{@link QuerResponseStreamProcessor}</code> instances via the creator 
     * <code>{@link QueryStream#from(DpDataRequest, DpQueryServiceStub, Consumer)}</code>.
     * There should be one instance of this function for every enabled data stream.
     * </p>
     *  
     * @author Christopher K. Allen
     * @since Feb 8, 2024
     *
     */
    private static final class QueryDataConsumer implements Consumer<QueryDataResponse.QueryData> {

        
        // 
        // Creator/Constructor
        //
        
        /**
         * <p>
         * Creates a new instance of <code>QueryDataConsumer</code> which passes data messages
         * to the given argument.
         * </p>
         * 
         * @param queDataBuffer data buffer to receive all accepted data messages via <code>{@link Queue#add(Object)}</code> 
         * 
         * @return  new <code>QueryDataConsumer</code> attached to the given data buffer
         */
        public static QueryDataConsumer newInstance(Queue<QueryData>   queDataBuffer) {
            return new QueryDataConsumer(queDataBuffer);
        }
        
        /**
         * <p>
         * Constructs a new instance of <code>QueryDataConsumer</code> attached to the target
         * data buffer.
         * </p>
         *
         * @param queDataBuffer     data buffer to receive all accepted data messages
         */
        public QueryDataConsumer(Queue<QueryData> queDataBuffer) {
            this.queDataBuffer = queDataBuffer;
        }

        //
        // Initialization Target
        //
        
        /** The queue buffer to receive all accepted data messages - assumes ordering of incoming data */
        private final Queue<QueryData>     queDataBuffer;
        
        
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
        @SuppressWarnings("unused")
        public final int getMessageCount() {
            return this.cntMsgs;
        }
        
        
        //
        // Consumer<BucketData> Interface
        //
        
        /**
         * Adds the given message to the data queue buffer <code>{@link queDataBuffer}</code>.
         * 
         * @param msgData   data message obtained from the associated <code>{@link QueryStream}</code>.
         * 
         * @see java.util.function.Consumer#accept(java.lang.Object)
         */
        @Override
        public void accept(QueryData msgData) {
            this.queDataBuffer.add(msgData);
            
            this.cntMsgs++;
        }
    }
    
    /**
     * <p>
     * Independent data processing threads for the <code>QueryResponseCorrelatorDeprecated</code> class.
     * </p>
     * <p>
     * This task is performed on a separate thread to decouple the gRPC streaming of
     * <code>QueryResponse</code> messages from the Query Service from the operation of
     * correlating the response data.  We do not want to interrupt the data streaming but
     * do want to began processing the data as soon as possible.
     * </p>
     * <p>
     * The method <code>{@link setMaxMessages(int)}</code> must be invoked to set the total
     * number of data messages to be processed.  The method must called be either before
     * starting the thread or while the thread is enabled.  The internal processing loop exits 
     * properly whenever the internal message counter <code>{@link #cntMsgsProcessed}</code>
     * is equal to the value supplied to this method.  Otherwise the processing loop
     * continues indefinitely, or until interrupted (e.g., with a call to 
     * <code>{@link #terminate()}</code> or <code>{@link Thread#interrupt()}</code>).
     * </p>
     * <p>
     * Message data processing is performed on a continuous loop within the thread's 
     * internal <code>{@link #run}</code> method invoked by <code>{@link Thread#start()}</code>. 
     * There raw response data messages (type <code>BucketData</code>) are polled from the 
     * queue buffer <code>{@link #queDataBuffer}</code>, blocking until they become available 
     * or a timeout occurs (see <code>{@link #LNG_TIMEOUT}, {@link #TU_TIMEOUT}</code>).
     * Data messages are then passed to the data correlator <code>{@link #theCorrelator}</code>
     * for processing.  The loop is exited whenever the number of messages processed
     * <code>{@link #cntMsgsProcessed}</code> is equal to the maximum message count 
     * <code>{@link #cntMsgsMax}</code>. 
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>
     * For concurrent gRPC data streaming and data correlation, the processing thread should be 
     * started (i.e., with the <code>{@link Thread#start()}</code> method) BEFORE the gRPC data 
     * stream(s).  The <code>{@link #setMaxMessages(int)}</code>
     * method should then be called once the data stream(s) have completed and the number of data
     * message retrieved is known.
     * </li>
     * <br/>
     * <li>
     * For separate gRPC data streaming and data correlation, the processing thread should be
     * started AFTER the gRPC data stream(s) (i.e., with the <code>{@link Thread#start()}</code> method).  
     * Before starting the thread, call the 
     * <code>{@link #setMaxMessages(int)}</code> method to supply the number of data messages
     * to process (i.e., so the processing loop exits normally).
     * </li> 
     * <br/>
     * <li>
     * Only one processing thread instance is required per data request, regardless of the
     * number of gRPC data streams used, or whether the processing is done currently/serially.  
     * All data streams should specify 
     * <code>{@link #queDataBuffer}</code> as the target of their data consumer function.
     * Due to the nature of Java a processing thread (inheriting from <code>{@link Thread}</code>)
     * can only be used once.
     * </li>
     * <li>
     * The external <code>{@link QueryResponseCorrelatorDeprecated}</code> object should block on the
     * <code>{@link Thread#join()}</code> method to wait for this processing thread to 
     * complete.  <code>{@link Thread#join()}</code> will return once the processing loop
     * exits normally.
     * </li>
     * <br/>
     * <li>
     * If an error occurs with the gRPC data streaming, the processing thread should be
     * terminated with <code>{@link #terminate()}</code>.  This action will also release
     * any threads waiting on the <code>{@link Thread#join()}</code> method.
     * </li>
     * <br/>
     * <li>
     * If a timeout occurs during queue polling the processing loop simply restarts, 
     * thus re-checking the number of processed messages against the maximum message count 
     * (if available).  The processing loop exits if the values are equal, or polls for 
     * another message otherwise.
     * </li>
     * <br/>
     * <li>
     * The polling timeout limit described by <code>{@link #LNG_TIMEOUT}</code> and 
     * <code>{@link #TU_TIMOUT}</code> can be used as tuning parameters for the processing
     * loop.
     * </li>
     * <ul>
     * </p>
     */
    private static final class QueryDataProcessor extends Thread {
        
        
        //
        // Creator/Constructor
        //
        
        /**
         * <p>
         * Creates a new instance of <code>QueryDataProcessor</code> attached to the arguments 
         * and ready for thread start.
         * </p>
         * 
         * @param queDataBuffer     source of raw data for correlation processing
         * @param theCorrelator     data correlator to receive raw data
         * 
         * @return  new <code>QueryDataProcessor</code> attached to the given arguments
         */
        public static QueryDataProcessor newThread(BlockingQueue<QueryData> queDataBuffer, QueryDataCorrelatorOld theCorrelator) {
            return new QueryDataProcessor(queDataBuffer, theCorrelator);
        }

        
        /**
         * <p>
         * Constructs a new instance of <code>QueryDataProcessor</code> attached to the 
         * raw data buffer and data correlator.
         * </p>
         *
         * @param queDataBuffer     source of raw data for correlation processing
         * @param theCorrelator     data correlator to receive raw data
         */
        public QueryDataProcessor(BlockingQueue<QueryData> queDataBuffer, QueryDataCorrelatorOld theCorrelator) {
            this.queDataBuffer = queDataBuffer;
            this.theCorrelator = theCorrelator;
        }
        
        
        // 
        // Class Constants
        //
        
        /** Queue buffer polling timeout limit */
        public static final long        LNG_TIMEOUT = 15;
        
        /** Polling timeout units */
        public static final TimeUnit    TU_TIMEOUT = TimeUnit.MILLISECONDS;
        
        
        // 
        // Initializing Resources
        //
        
        /** The external queued data buffer supplying data messages for correlation */
        private final BlockingQueue<QueryData>  queDataBuffer;
        
        /** The external data correlator doing the data message processing */
        private final QueryDataCorrelatorOld       theCorrelator;
        
        
        //
        // State Variables
        //
        
        /** thread started flag */
        private boolean         bolStarted = false;
        
        /** thread external termination flag */
        private boolean         bolTerminated = false;
        
        /** Number of data messages to process (unknown until set) */
        private Integer         cntMsgsMax = null;
        
        /** Number of data messages processed so far (internal counter) */
        private int             cntMsgsProcessed = 0;
        
        /** The result of the thread task */
        private ResultStatus    recResult = null;
        
        
        //
        // Operations
        //
        
        /**
         * <p>
         * Sets the maximum number of <code>BucketData</code> messages to process.
         * </p>
         * <p>
         * This method MUST be called during the life of the processing thread in order to
         * exit the processing loop normally.
         * <p>
         * Note that this value is typically unknown until the gRPC data streaming completes.
         * Thus we assume this value is set after the <code>QueryDataProcessor</code> thread
         * has already started whenever concurrent streaming and data processing is used.
         * </p>
         * <p>
         * The <code>{@link #cntMsgsMax}</code> attribute is set to the argument value. 
         * The processing loop checks this value against the internal counter
         * <code>{@link #cntMsgsProcessed}</code> after every iteration.
         * If the maximum number of messages has already been processed, the processing loop
         * is exits normally and the result is set to SUCCESS.
         * </p>
         * 
         * @param cntMsgsMax    maximum number of <code>BucketData</code> messages to process
         */
        public void setMaxMessages(int cntMsgsMax) {
            this.cntMsgsMax = cntMsgsMax;
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
         * <ul>
         * <li>
         * This is the proper way to terminate an enabled processing thread.  DO NOT use
         * the method <code>{@link Thread#interrupt()}</code> as it will leave a 
         * <code>null</code> valued <code>{@link ResultStatus}</code> potentially corrupting
         * the normal (external) processing operations.
         * </li>
         * <br/>
         * <li> 
         * Terminating a processing thread that has not started has no effect and will not 
         * affect any future processing or events.  This action is illegal and anticipated.
         * </li> 
         * </p>
         */
        public void terminate() {
            if (!this.bolStarted)
                return;

            this.bolTerminated = true;
            
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - The data processing thread was terminted externally.";
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            this.recResult = ResultStatus.newFailure(strMsg);
        }
        
        //
        // State Queries
        //
        
        /**
         * <p>
         * Determines whether or not the processing thread has been started.
         * </p>
         * <p>
         * Note that whenever a processing thread is started by the <code>{@link Thread#start()}</code>
         * method, the <code>{@link #bolStarted}</code> flag is set to <code>true</code>
         * within the <code>{@link #run()}</code> task that is consequently invoked.
         * </p>
         * 
         * @return  <code>true</code> if the <code>{@link Thread#start()}</code> has been called,
         *          <code>false</code> otherwise.
         */
        public boolean isStarted() {
            return this.bolStarted;
        }
        
//        /**
//         * <p>
//         * Returns whether or not the processing thread was terminated externally.
//         * </p>
//         * <p>
//         * If the returned value is <code>true</code> this indicates that the 
//         * <code>{@link #terminate()}</code> method has been invoked while the processor
//         * was running.
//         * </p>
//         * 
//         * @return  <code>true</code> if the processor was terminated externally, <code>false</code> otherwise
//         */
//        public boolean isCancelled() {
//            return this.bolTerminated;
//        }
        
        /**
         * <p>
         * Returns the <code>{@link ResultStatus}</code> containing the status of this processing 
         * task.
         * </p>
         * <p>
         * The returned value will be <code>{@link ResultStatus#SUCCESS}</code> only if the 
         * the processing thread has exited normally.  This value implies the following 
         * conditions:
         * <ul>
         * <li>The maximum number of messages to process has been set with <code>{@link #setMaxMessages(int)}</code>.
         * </li>
         * <li>All queued messages have been successfully correlated.</li>
         * <li>The processing loop within <code>{@link #run()}</code> has exited and the thread 
         * is dead.</li>
         * </ul>
         * <p>
         * A FAILURE status also indicates that the thread is currently dead.  The only condition
         * where this occurs is when the <code>{@link #terminate()}</code> method is invoked. 
         * </p> 
         * <p>
         * <h2>NOTES:</h2>
         * <ul>
         * <li>
         * This method does not block.  That is, it does not wait for the processing loop to
         * complete but returns the status of the thread in its current state (or <code>null</code>).
         * </li>
         * <br/>
         * <li>
         * The returned value will be <code>null</code> if the processing thread has not been 
         * started or has terminated abnormally (e.g., was interrupted other than by
         * <code>#terminate()</code>.
         * </li>
         * </ul>
         * </p>
         * 
         * @return  <code>{@link ResultStatus#SUCCESS}</code> if the thread has exited normally,
         *          <code>{@link ResultStatus#isFailure()}</code> result if terminated,
         *          <code>null</code> otherwise
         */
        public ResultStatus getResult() {
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
         * Runs a continuous loop that polls the 
         * <code>{@link QueryResponseCorrelatorDeprecated#queStreamBuffer}</code> data buffer
         * for <code>BucketData</code> data messages.  When available, the data messages are
         * passed to the <code>{@link #theCorrelator}</code> for processing.
         * </p>
         * <p>
         * The loop continues until the number of data messages obtained from the buffer is
         * equal to the parameter <code>{@link #cntMsgsMax}</code>, which must eventually be set.
         * The loop also terminates if the <code>{@link #terminate()}</code> method is 
         * invoked.  Otherwise the processing loop continue indefinitely or until interrupted
         * externally (e.g., with a <code>{@link Thread#interrupt()}</code>.
         * </p>
         * <p>
         * If the timeout limit is reached during data queue polling, as specified by constants 
         * <code>{@link #LNG_TIMEOUT}</code> and <code>{@link #TU_TIMEOUT}</code>,
         * the loop simply restarts.  In restarting, the <code>{@link #cntMsgsProcessed}</code>
         * value is checked against <code>{@link #cntMsgsMax}</code> to see if the loop can
         * exit normally. 
         * </p>
         * <p>
         * <h2>NOTES:</h2>
         * <ul>
         * <li>
         * The <code>{@link #bolStarted}</code> flag is set to <code>true</code> upon initial
         * invocation.
         * </li>
         * <br/>
         * <li>
         * Use <code>{@link #getResult()}</code> to return the status of the processing thread, 
         * or the cause of any errors.
         * </li>
         * </ul>
         * </p>
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            this.bolStarted = true;
            
            while ((this.cntMsgsMax == null) || (this.cntMsgsProcessed < this.cntMsgsMax) && !this.bolTerminated) {
                try {
                    QueryDataResponse.QueryData msgData = this.queDataBuffer.poll(LNG_TIMEOUT, TU_TIMEOUT);

                    // If we timeout try again
                    if (msgData == null) {

                        continue;
                    }

                    // Note that this operation will block until completed
                    this.theCorrelator.addQueryData(msgData);
                    this.cntMsgsProcessed++;

                    // TODO - Remove
//                    if (BOL_LOGGING)
//                        LOGGER.debug("{} - Processed message {}", JavaRuntime.getQualifiedMethodNameSimple(), this.cntMsgsProcessed);

                } catch (InterruptedException e) {
                    if (this.cntMsgsMax != null && (this.cntMsgsProcessed >= this.cntMsgsMax)) {
                        this.recResult = ResultStatus.SUCCESS;
                        
                        if (BOL_LOGGING)
                            LOGGER.warn("Interrupted but successful.");
                        
                        return;
                    }
                    
                    String  strErrMsg = JavaRuntime.getQualifiedMethodNameSimple() +
                            " - Process interrupted externally and FAILED while waiting for data buffer.";
                    
                    if (BOL_LOGGING) 
                        LOGGER.error(strErrMsg);
                    
                    this.recResult = ResultStatus.newFailure(strErrMsg, e);

                    return;
                }
            }

            if (BOL_LOGGING)
                LOGGER.debug("{} - Exiting processing loop.", JavaRuntime.getQualifiedMethodNameSimple());
            
            this.recResult = ResultStatus.SUCCESS;
        }
    }
    
    
    //
    // Application Resources
    //
    
    /** The Data Platform Query Service default parameters */
    private static final DpQueryConfig CFG_QUERY = DpApiConfig.getInstance().query;
    
    
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
    public static final long        CNT_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** Timeout unit for query operation */
    public static final TimeUnit    TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
    /** Use multi-threading for query data correlation */
    public static final boolean     BOL_CORRELATE_CONCURRENCY = CFG_QUERY.data.response.correlate.useConcurrency;
    
    /** Perform data correlation while gRPC streaming - otherwise do it post streaming */
    public static final boolean     BOL_CORRELATE_MIDSTREAM = CFG_QUERY.data.response.correlate.whileStreaming;
    
    
    /** Is response multi-streaming enabled? */
    public static final boolean     BOL_MULTISTREAM = CFG_QUERY.data.response.multistream.enabled;
    
    /** Maximum number of open data streams to Query Service */
    public static final int         CNT_MULTISTREAM = CFG_QUERY.data.response.multistream.maxStreams;
    
    /** Query domain size triggering multiple streaming (if enabled) */
    public static final long        SIZE_DOMAIN_MULTISTREAM = CFG_QUERY.data.response.multistream.sizeDomain;
    
    
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
    private final DpQueryConnection         connQuery;

    
    //
    // Instance Resources
    //
    
    /** The data request decomposer needed for multi-streaming requests */
    private final DataRequestDecomposer     rqstDecomp = DataRequestDecomposer.create();
    
    /** The queue buffering all response messages for data correlation processing */
    private final BlockingQueue<QueryData>  queStreamBuffer = new LinkedBlockingQueue<>();
    
    /** The single query data theCorrelator used to process all request data */
    private final QueryDataCorrelatorOld       theCorrelator = new QueryDataCorrelatorOld();  
    
    
    /** The pool of (multiple) gRPC streaming thread(s) for recovering requests */
    private ExecutorService           exeThreadPool = Executors.newFixedThreadPool(CNT_MULTISTREAM > 1 ? CNT_MULTISTREAM : 1);
    
    /** The query response processor thread */
    private QueryDataProcessor        thdDataProcessor = null;

    
    //
    //  Configuration
    //
    
    /** Multi-streaming query response toggle */
    private boolean bolMultiStream = BOL_MULTISTREAM;
    
    /** The request domain size (PV count - time) triggering multi-streaming */
    private long    szDomainMultiStream = SIZE_DOMAIN_MULTISTREAM;
    
    /** Number of data streams for multi-streaming response recovery */
    private int     cntStreams = CNT_MULTISTREAM;

    /** Use multi-threading for query data correlation */
    private boolean bolCorrelateConcurrency = BOL_CORRELATE_CONCURRENCY;
    
    /** Perform query data correlation concurrent while gRPC streaming */
    private boolean bolCorrelateMidstream = BOL_CORRELATE_MIDSTREAM;
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryResponseCorrelatorDeprecated</code> ready for request processing.
     * </p>
     * <p>
     * The Query Service connection object is provided to the <code>QueryResponseCorrelatorDeprecated</code>
     * at construction.  This connection is used for all subsequent data request gRPC operations.
     * Since a single gRPC <code>{@link Channel}</code> object can support multiple concurrent
     * data streams, only one connection is needed. Notes that all request processing methods 
     * (i.e., methods prefixed with <code>process</code>) do not returned until the request is 
     * fully processed.  Thus, multiple <code>{@link Channel}</code> instances are not required.
     * </p>
     * 
     * @param connQuery     the single Query Service connection used for all subsequent data requests 
     */
    public QueryResponseCorrelatorDeprecated(DpQueryConnection connQuery) {
        this.connQuery = connQuery;
        
        this.theCorrelator.setConcurrency(this.bolCorrelateConcurrency);
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
     * large data requests into decompose request according to settings in the default
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
     * data  request is decomposed using the default decompose query mechanism.  The components
     * of the decompose request are then separate performed on independent gRPC data streams.
     * The full result set is then assembled from the multiple data streams.
     * </p>
     * <p>
     * The default value is taken from the Java API Library configuration file
     * (see <code>{@link #BOL_MULTISTREAM}</code>).  This value can be recovered from 
     * <code>{@link #isMultiStreamingResponse()}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Only affects the operation of <code>{@link #processRequestStream(DpDataRequest)}</code>.</li>
     * <li>All decompose query configuration parameters are taken from the default API parameters.</li>
     * <li>All gRPC multi-streaming parameters are taken from the default API parameters.</li>
     * </ul>
     * </p>  
     * 
     * @param bolMultiStream    turn on/off the use of multiple gRPC streams for query response recovery
     */
    public void setMultiStreamingResponse(boolean bolMultiStream) {
        this.bolMultiStream = bolMultiStream;
    }
    
    /**
     * <p>
     * Sets the request domain size threshold to trigger multi-streaming of request results.
     * </p>
     * <p>
     * If the multi-streaming feature is turned on (see <code>{@link #isMultiStreamingResponse()}</code>)
     * it will only be triggered if the request domain size is greater than the given value.
     * The approximate domain size of a request is given by the method 
     * <code>{@link DpDataRequest#approxDomainSize()}</code>.  
     * </p>
     * <p>
     * Requiring that data requests have a given domain size is a performance issue and the given value is
     * thus a performance parameter.  The ideal is to limit the use of multiple, concurrent data streams for
     * large requests.  Creating multiple gRPC data streams for a small request can usurp unnecessary 
     * resources.
     * </p>
     *  
     * @param szDomain  the request domain size (in source-count seconds) threshold where multis-treaming is triggered
     */
    public void setMultiStreamingDomainSize(long szDomain) {
        this.szDomainMultiStream = szDomain;
    }
    
    /**
     * <p>
     * Sets the number of gRPC data stream used for multi-streaming query responses.
     * </p>
     * <p>
     * This method should be called before any query response processing has started, if at all.  The default value
     * in the Java API Library configuration file should be used as a matter of course.  However, this method is
     * available for performance evaluations. 
     * </p>
     * 
     * @param cntStreams    number of gRPC data streams to use in multi-streaming data request recovery
     * 
     * @throws IllegalStateException    method called while actively processing
     */
    synchronized 
    public void setMultiStreamCount(int cntStreams) throws IllegalStateException {
//        if (this.theCorrelator.sizeCorrelatedSet() > 0 || (this.thdDataProcessor!=null && this.thdDataProcessor.isStarted())) {
        if (this.thdDataProcessor!=null && this.thdDataProcessor.isStarted()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Cannot change stream count while processing.";
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new IllegalStateException(strMsg);
        }
        
        this.cntStreams = cntStreams;
        this.exeThreadPool = Executors.newFixedThreadPool(cntStreams);
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
     * The internal <code>{@link QueryDataCorrelatorOld}</code> instance used to correlate the
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
        this.theCorrelator.setConcurrency(bolCorrelateConcurrently);
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
    public void setCorrelateWhileStreaming(boolean bolCorrelateMidstream) {
        this.bolCorrelateMidstream = bolCorrelateMidstream;
    }
    
    
    /**
     * <p>
     * Specifies whether or not multi-streaming of query responses is enabled.
     * </p>
     * <p>
     * The default value is taken from the Java API Library configuration file
     * (see {@link #BOL_MULTISTREAM}).
     * </p>
     * 
     * @return <code>true</code> if multi-streaming is enabled, <code>false</code> if disabled
     * 
     * @see #BOL_MULTISTREAM
     */
    public final boolean isMultiStreamingResponse() {
        return this.bolMultiStream;
    }

    /**
     * <p>
     * Returns the minimum request domain size which triggers query response multi-streaming.
     * </p>
     * <p>
     * The returned value has context only if query response multi-streaming has context, specifically,
     * <code>{@link #isMultiStreamingResponse()}</code> returns <code>true</code>.  Otherwise a single
     * response stream is always used.  If multi-streaming responses are enabled then multiple gRPC
     * data streams will only be used if the returned value is greater than the size of the 
     * query request domain, which is given by <code>{@link DpDataRequest#approxDomainSize()}</code>.
     * </p>
     * <p>
     * The requirement for a minimum domain size is used as a performance criterion.  Minimum domain
     * sizes prevent the use of multiple gRPC data streams for small data requests, preventing unnecessary 
     * resource allocation.
     * </p>
     * <p>
     * The default value is taken from the Java API Library configuration file
     * (see {@link #SIZE_DOMAIN_MULTISTREAM}).
     * </p>
     * 
     * @return the minimum query domain size triggering multi-streaming of response data (in data source * seconds)
     */
    public final long geMultiStreamingDomainSize() {
        return this.szDomainMultiStream;
    }

    /**
     * <p>
     * Returns the number of gRPC data streams used for multi-streaming of query responses.
     * </p>
     * <p>
     * The returned value is the number of gRPC data streams always used for a multi-streaming response.
     * Note that the returned value has context only when multi-streaming is enabled, specificially,
     * <code>{@link #isMultiStreamingResponse()}</code> returns <code>true</code>.
     * </p>
     * 
     * @return the number of concurrent gRPC data streams used to recover query responses when enabled
     */
    public final int getMultiStreamCount() {
        return this.cntStreams;
    }

    /**
     * <p>
     * Returns whether or not concurrent correlation is enabled.
     * </p>
     * <p>
     * If the returned value is <code>true</code> then the correlation of query response data
     * is performed using multi-threading.  This is a performance feature which can be turned
     * on/off.  In general, allowing concurrent processing of response data will increase performance
     * so long as processing threads do not interfere with other activities.
     * </p>
     * 
     * @return <code>true</code> if concurrent correlation of response data is enabled, <code>false</code> otherwise
     */
    public final boolean isCorrelatingConcurrently() {
        return this.bolCorrelateConcurrency;
    }

    /**
     * <p>
     * Returns whether or not correlation of response data is allowed while it is being streamed back from the
     * Query Service.
     * </p>
     * <p>
     * If the returned value is <code>true</code> then the correlation of query response data is allowed
     * to proceed while it simultaneously being streamed back from the Query Service.  This is a performance
     * feature increasing concurrency of the correlation process.  Specifically, correlation does not need to
     * wait until all query data is available.  However, the over use of concurrency can cause performance
     * degradation and use of this feature should be used with caution.
     * </p>
     * 
     * @return <code>true</code> if query response data can be correlated as it is being streamed back, 
     *         <code>false</code> otherwise
     */
    public final boolean isCorrelatingWhileStreaming() {
        return this.bolCorrelateMidstream;
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
     * @return  a sorted set (according to start time) of correlated data obtained from given request 
     * 
     * @throws DpQueryException general exception during data recovery and/or processing (see cause)
     */
    public SortedSet<CorrelatedQueryDataOld>   processRequestUnary(DpDataRequest dpRequest) throws DpQueryException {

        // Reset the data theCorrelator
        this.theCorrelator.reset();
        
        // Create the data request message
        QueryDataRequest    msgRequest = dpRequest.buildQueryRequest();
        
        // Perform the unary data request directly on blocking stub
        QueryDataResponse   msgResponse = this.connQuery.getStubBlock().queryData(msgRequest);
        
        // Process the request data catching any Query Service errors
        try {
            this.theCorrelator.addQueryResponse(msgResponse);
            
        } catch (ExecutionException e) {
            if (BOL_LOGGING)
                LOGGER.error("{}: Query request was rejected by Query Service - {}", JavaRuntime.getMethodName(), e.getMessage());
            
            throw new DpQueryException(e);
            
        } catch (CannotProceedException e) {
            if (BOL_LOGGING)
                LOGGER.error("{}: Query response contained error - {}", JavaRuntime.getMethodName(), e.getMessage());
            
            throw new DpQueryException(e);
        }
        
        // Return the processed data
        SortedSet<CorrelatedQueryDataOld> setDataPrcd = this.theCorrelator.getCorrelatedSet();
        
        return setDataPrcd;
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
     * @return  a sorted set (according to start time) of correlated data obtained from given request 
     * 
     * @throws DpQueryException general exception during data recovery and/or processing (see cause)
     * 
     * @see #setCorrelateWhileStreaming(boolean)
     * @see #setCorrelationConcurrency(boolean)
     * @see #setMultiStreamingResponse(boolean)
     */
    public SortedSet<CorrelatedQueryDataOld>   processRequestStream(DpDataRequest dpRequest) throws DpQueryException {

        // Create request list according to multi-streaming configuration
        List<DpDataRequest>     lstRequests;
        
        // If multi-streaming is turned off return original request
        if (!this.bolMultiStream)
            lstRequests = List.of(dpRequest);
        else
            lstRequests = this.attemptRequestDecomposition(dpRequest);
        
        // Defer to the multi-streaming processor method
        return this.processRequestMultiStream(lstRequests);
    }
    
    /**
     * <p>
     * Use multiple gRPC data streams explicitly described by argument list (to recover request data).
     * </p>
     * <p>
     * This method allows clients to explicitly determine the concurrent gRPC data streams used by the
     * <code>QueryRequestProcessorOld</code>.  To use the default multi-streaming mechanism method
     * <code>{@link #processRequestStream(DpDataRequest)}</code> is available.
     * <p>
     * A separate gRPC data stream is established for each data request within the argument list and concurrent
     * data streams are used to recover the request data.
     * At most <code>{@link #CNT_MULTISTREAM}</code> concurrent data streams are enabled at any instant.
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
    public SortedSet<CorrelatedQueryDataOld>   processRequestMultiStream(List<DpDataRequest> lstRequests) throws DpQueryException {
        
        // Reset the data theCorrelator
        this.theCorrelator.reset();
        
        // Create streaming task for each component request
        List<QueryStream>  lstStrmTasks = this.createStreamingTasks(lstRequests);
        
        // Create the data correlation thread task
        this.thdDataProcessor = QueryDataProcessor.newThread(this.queStreamBuffer, this.theCorrelator);

        
        // Start data processing thread if mid-stream processing true
        if (this.bolCorrelateMidstream)
            this.thdDataProcessor.start();
        
        // Start all streaming tasks and wait for completion 
        int     cntMsgs;    // returns number of response messages streamed
        
        try {
            cntMsgs = this.executeStreamingTasks(lstStrmTasks);
            
        } catch (InterruptedException e) {
            this.thdDataProcessor.terminate();
            
            if (BOL_LOGGING)
                LOGGER.error("{}: InterruptedException while streaming request results - {}", JavaRuntime.getMethodName(), e.getMessage());
            
            throw new DpQueryException(e);

        } catch (TimeoutException e) {
            this.thdDataProcessor.terminate();
            
            if (BOL_LOGGING)
                LOGGER.error("{}: TimeoutException while streaming requst results - {}", JavaRuntime.getMethodName(), e.getMessage());
            
            throw new DpQueryException(e);

        } catch (CompletionException e) {
            this.thdDataProcessor.terminate();
        
            if (BOL_LOGGING)
                LOGGER.error("{}: ExecutionException while streaming request results - {}", JavaRuntime.getMethodName(), e.getMessage());
            
            throw new DpQueryException(e);
        }
        
        // Return to request results data processing
        // - Tell the data processor task the number of messages to process
        // - Start if it has not already been started (i.e., via mid-stream processing)
        // - Wait for it to finish
        this.thdDataProcessor.setMaxMessages(cntMsgs);
        
        if (!this.thdDataProcessor.isStarted())
            this.thdDataProcessor.start();
        
        try {
            this.thdDataProcessor.join(timeoutLimitDefaultMillis());
            
        } catch (InterruptedException e) {
            if (BOL_LOGGING)
                LOGGER.error("{}: InterruptedException while processing request data - {}", JavaRuntime.getMethodName(), e.getMessage());
            
            throw new DpQueryException(e);
        }
    
        // Check for successful processing then return processed data
        ResultStatus    recProcessed = this.thdDataProcessor.getResult();
        if (recProcessed == null || recProcessed.isFailure()) {
            if (BOL_LOGGING)
                LOGGER.error("{}: Data correlation processing FAILED - {}", JavaRuntime.getMethodName(), recProcessed.message());

            throw new DpQueryException("Data correlation processing FAILED: " + recProcessed.message());
        }
        this.thdDataProcessor = null;
        
        SortedSet<CorrelatedQueryDataOld>  setPrcdData = this.theCorrelator.getCorrelatedSet();
        return setPrcdData;
    }
    
    
    //
    // State Inquiry
    //
    
    /**
     * <p>
     * Returns the number of bytes processed during a query request.
     * </p>
     * <p>
     * Returns the number of bytes queried and processed from the Query Service for a query request
     * operation initiated by a <code>processRequest...()</code> method.
     * </p>
     * <p>
     * The returned value has context immediately after a query request.  It is reset after each 
     * called to a <code>processRequest...()</code> method in the same fashion as the returned
     * object is reset.
     * </p>
     * 
     * @return  number of bytes processed in the most recent query request
     */
    public long getBytesProcessed() {
        return this.theCorrelator.getBytesProcessed();
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Decomposing the given query request into a composite request.
     * </p>
     * <p>
     * The given request is decomposed only if the request domain is greater than the current
     * minimum request domain size; specifically, only if 
     * <code>{@link DpDataRequest#approxDomainSize()} > {@link #geMultiStreamingDomainSize()}</code>.
     * Otherwise the returned list contains a single element, the original request.  
     * </p>
     * <p>
     * The decomposition is performed using the preferred decomposition as specified by the
     * <code>DataRequestDecomposer</code> class.  This decomposition is given by the method
     * <code>{@link DataRequestDecomposer#decomposeDomainPreferred(DpDataRequest)}</code>.
     * </p>
     * <h2>WARNING:</h2>
     * Note that the number of composite request can be larger than the number of gRPC streams used
     * in multi-streaming.  In that case the thread pool continues executing, creating new gRPC
     * streams for each request but no more than the maximum at a single instance.  This condition
     * may not be optimal.
     * 
     * @param rqst  data request to be decomposed for multi-streaming 
     * 
     * @return  the decompose data request obtained from the above decomposition strategy
     */
    private List<DpDataRequest> decomposeRequest(DpDataRequest rqst) {
        
        // Check if request size approximation is large enough to pivot to multi-streaming
        long    szDomain = rqst.approxDomainSize();

        if (szDomain < this.szDomainMultiStream)
            return List.of(rqst);


        // If we are here - we are doing multiple gRPC streams requiring a decompose query
        List<DpDataRequest>     lstCmpRqsts;    // decompose request to be returned

        // Use the preferred domain decomposition, regardless of the number of requests produced
        RequestDecompParams recDomain = this.rqstDecomp.decomposeDomainPreferred(rqst);

        lstCmpRqsts = this.rqstDecomp.buildCompositeRequest(rqst, recDomain);
        
        return lstCmpRqsts;
    }
    
    /**
     * <p>
     * Attempts to decompose the given data request into a composite request (i.e., for multi-streaming).
     * </p>
     * <p>
     * Attempts a decomposition of the given request into a decompose request collection of no more than
     * <code>{@link #getMultiStreamCount()}</code> elements.  
     * This method is part of the DEFAULT multi-streaming mechanism within <code>QueryResponseCorrelatorDeprecated</code>.
     * </p>
     * <p>
     * <h2>Request Size</h2>
     * The <code>DpDataRequest</code> class provides a request size estimate given by
     * <code>{@link DpDataRequest#approxDomainSize()}</code>.  This method returned the 
     * estimated domain size within the given query request (in data sources * seconds).  If this size
     * estimate is less than <code>{@link #geMultiStreamingDomainSize()}</code> nothing is done and a list 
     * containing the original data request is returned.
     * </p>
     * <p>
     * <s>The sampling period for the size estimate method is given as a single unit of the 
     * <code>{@link TimeUnit}</code> enumeration specified by class constant 
     * <code>{@link #TU_MULTISTREAM_PERIOD}</code>.  That is, the arguments of the above method are 1L and
     * <code>{@link #TU_MULTISTREAM_PERIOD}</code>, respectively.</s>
     * </p>
     * <p>
     * <h2>Decomposition Strategy</h2>
     * If the data request size estimate is larger than the cutoff limit, a series of evaluations is performed on 
     * the data request to determine a suitable decomposition.  The following conditions, and subsequent actions,
     * are evaluated in order:
     * <ol>
     * <li>
     * <h3>Preferred Decomposition</h3>
     * Does the <code>DpDataRequest</code> preferred decomposition provided by 
     * <code>{@link DpDataRequest#decomposeDomainPreferred()}</code> yield a query domain decomposition where
     * the number of domain covers &le; <code>{@link #getMultiStreamCount()}</code>?  If so, this query domain
     * decomposition is used to produce the returned decompose query for multi-streaming.
     * </li>
     * <li>
     * <h3>Horizontal Decomposition</h3>
     * If the number of data sources within the given data request &ge; <code>{@link #getMultiStreamCount()}</code>
     * then a "horizontal" query domain decomposition is used to generate the returned decompose request.
     * Specifically, the returned value is given by 
     * <code>{@link DpDataRequest#buildCompositeRequest(RequestDecompType, int)}</code> where the arguments are
     * <code>{@link RequestDecompType#HORIZONTAL}</code> and <code>{@link #getMultiStreamCount()}</code>, respectively.
     * </li>
     * <li>
     * <h3>Vertical Decomposition</h3>
     * If the estimated number of samples within the request &ge; 
     * <code>{@link #getMultiStreamCount()}</code> * <code>{@link #geMultiStreamingDomainSize()}</code> then a "vertical"
     * query domain decomposition is used to create the returned decompose request.  Specifically, the returned
     * value is given by 
     * <code>{@link DpDataRequest#buildCompositeRequest(RequestDecompType, int)}</code> where the arguments are
     * <code>{@link RequestDecompType#VERTICAL}</code> and <code>{@link #getMultiStreamCount()}</code>, respectively.
     * </li>
     * <li>
     * <h3>Grid Decomposition</h3>
     * If the number of data sources within the request &le; <code>{@link #getMultiStreamCount()}</code> / 2 then a
     * "grid" query domain decomposition is used to create the returned decompose request.  Specifically, the
     * returned value is given by 
     * <code>{@link DpDataRequest#buildCompositeRequest(RequestDecompType, int)}</code> where the arguments are
     * <code>{@link RequestDecompType#GRID}</code> and <code>{@link #getMultiStreamCount()}</code>, respectively.
     * </li>
     * </ol>
     * If all the above conditions fail, the data request is consider "undecomposable".  
     * The returned value is then a one-element list containing the original data request.  
     * </p>
     * 
     * @param dpRequest data request to be decomposed for multi-streaming 
     * 
     * @return  the decompose data request obtained from the above decomposition strategy
     * 
     */
    private List<DpDataRequest> attemptRequestDecomposition(DpDataRequest dpRequest) {
        
        // Check if request size approximation is large enough to pivot to multi-streaming
        long    szDomain = dpRequest.approxDomainSize();
        
        if (szDomain < this.szDomainMultiStream)
            return List.of(dpRequest);
        
        
        // If we are here - we are doing multiple gRPC streams requiring a decompose query
        List<DpDataRequest>     lstCmpRqsts;    // decompose request to be returned
        
        // Try default query domain decomposition will work
        RequestDecompParams recDomain = this.rqstDecomp.decomposeDomainPreferred(dpRequest);
        
        if (recDomain.totalCovers() < this.cntStreams) {
            lstCmpRqsts = this.rqstDecomp.buildCompositeRequest(dpRequest, recDomain);
            
            return lstCmpRqsts;
        }
        
        // Try horizontal query domain decomposition (by data sources)
        //  Works when request source count is greater than the stream count
        if (dpRequest.getSourceCount() > this.cntStreams) {
            lstCmpRqsts = this.rqstDecomp.buildCompositeRequest(dpRequest, RequestDecompType.HORIZONTAL, this.cntStreams);
        
            return lstCmpRqsts;
        }
        
        // Try vertical query domain decomposition (by time domain)
        //  First compute the number of samples per request
        long lngDomPerRqst = szDomain / this.szDomainMultiStream;
        int  szDomPerRqst = Long.valueOf(lngDomPerRqst).intValue();
        
        //  Add any remainder (just in case)
        szDomPerRqst += (szDomain % this.szDomainMultiStream > 0) ? 1 : 0;
        
        if (szDomPerRqst < this.cntStreams) {
            lstCmpRqsts = this.rqstDecomp.buildCompositeRequest(dpRequest, RequestDecompType.VERTICAL, this.cntStreams);
            
            return lstCmpRqsts;
        }
        
        // Try a grid-based query domain decomposition
        //  Works when the source count is at least half of the stream count
        if (dpRequest.getSourceCount() > (this.cntStreams/2)) {
            lstCmpRqsts = this.rqstDecomp.buildCompositeRequest(dpRequest, RequestDecompType.GRID, this.cntStreams);
            
            return lstCmpRqsts;
        }
        
        
        // We cannot find any domain decomposition - Default back to single data request
        return List.of(dpRequest);
    }
    
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
//            DpGrpcStreamType       enmStreamType = dpRequest.getStreamType();
//            QueryDataRequest        msgRequest = dpRequest.buildQueryRequest();
            DpQueryServiceStub      stubAsync = this.connQuery.getStubAsync();
            QueryDataConsumer       fncConsumer = QueryDataConsumer.newInstance(this.queStreamBuffer);
            
            QueryStream    taskStrm = QueryStream.from(
//                    enmStreamType,
//                    msgRequest,
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
            ResultStatus recResult = lstStrmTasks
                                    .stream()
                                    .filter(p  -> p.getResult().isFailure())
                                    .<ResultStatus>map(p -> p.getResult())
                                    .findAny()
                                    .get();
            String strErr = recResult.message()+ ( (recResult.hasCause()) ? ", cause=" + recResult.cause() : "");
            String strMsg = "At least one gRPC streaming error: message=" + strErr;  
            
            throw new CompletionException(strMsg, recResult.cause());
        }
        
        // Recover the total number of messages streamed and return it
        int cntMsgs = lstStrmTasks.stream().mapToInt(p -> p.getResponseCount()).sum();
        
        return cntMsgs;
    }
    
    
    /**
     * <p>
     * Computes and returns the default timeout limit in unites of milliseconds
     * </p>
     * <p>
     * The returned value is intended to provide a timeout limit for thread operations waiting on a 
     * <code>{@link Thread#join(long)}</code> invocation.  Note that the returned value is that specified
     * by the default timeout class constants <code>{@link #CNT_TIMEOUT}</code> and 
     * <code>{@link #TU_TIMEOUT}</code>.
     * </p> 
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
    
//    private SortedSet<CorrelatedQueryDataOld> processSingleStream(DpDataRequest dpRequest) throws InterruptedException, TimeoutException, ExecutionException {
//
//        QueryStream    taskStream = QueryStream.newTask(
//                dpRequest, 
//                this.connQuery.getStubAsync(), 
//                QueryDataConsumer.newInstance(this.queStreamBuffer)
//                );
//
//        Thread              thdStream = new Thread(taskStream);
//        QueryDataProcessor  thdProcess = QueryDataProcessor.newThread(this.queStreamBuffer, this.theCorrelator);
//
//        // Stream then correlate
//        if (!this.bolCorrelateMidstream) {
//            thdStream.start();
//            thdStream.join(timeoutLimitDefaultMillis());
//
//            // Check for timeout limit
//            if (!taskStream.isCompleted()) {
//                String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() + " - Time limit expired while gRPC data streaming.";
//
//                if (BOL_LOGGING)
//                    LOGGER.error(strMsg);
//                
//                throw new TimeoutException(strMsg);
//            }
//            
//            // Check for streaming error
//            if (!taskStream.isSuccess()) {
//                ResultStatus    recResult = taskStream.getResult();
//                String          strMsg = JavaRuntime.getQualifiedCallerNameSimple()
//                        + " - gRPC streaming error; message="
//                        + recResult.message();
//                if (recResult.hasCause())
//                    strMsg += ", cause=" + recResult.cause();
//                
//                if (BOL_LOGGING)
//                    LOGGER.error(strMsg);
//                
//                thdProcess.terminate();
//                
//                throw new ExecutionException(strMsg, recResult.cause());
//            }
//            
//            // Set the number of messages to process
//            thdProcess.setMaxMessages(taskStream.getResponseCount());
//            thdProcess.start();
//            thdProcess.join(timeoutLimitDefaultMillis());
//            
//            SortedSet<CorrelatedQueryDataOld>  setPrcdData = this.theCorrelator.getCorrelatedSet();
//            return setPrcdData;
//        }
//        
//        // Do it concurrently
//        try {
//            thdProcess.start();
//            thdStream.start();
//
//            thdStream.join(timeoutLimitDefaultMillis());
//            
//            // Check for timeout limit
//            if (!taskStream.isCompleted()) {
//                String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() + " - Time limit expired while gRPC data streaming.";
//
//                if (BOL_LOGGING)
//                    LOGGER.error(strMsg);
//                
//                throw new TimeoutException(strMsg);
//            }
//
//            // Check for streaming error
//            if (!taskStream.isSuccess()) {
//                ResultStatus    recResult = taskStream.getResult();
//                String          strMsg = JavaRuntime.getQualifiedCallerNameSimple()
//                        + " - gRPC streaming error; message="
//                        + recResult.message();
//                if (recResult.hasCause())
//                    strMsg += ", cause=" + recResult.cause();
//                
//                if (BOL_LOGGING)
//                    LOGGER.error(strMsg);
//                
//                thdProcess.terminate();
//                
//                throw new ExecutionException(strMsg, recResult.cause());
//            }
//            
//            // Set the number of messages to process
//            thdProcess.setMaxMessages(taskStream.getResponseCount());
//
//            thdProcess.join(timeoutLimitDefaultMillis());
//            
//        } catch (InterruptedException e) {
//            if (BOL_LOGGING)
//                LOGGER.error("{}: Interrupted while waiting for data streaming/processing to complete - {}.", JavaRuntime.getCallerName(), e.getMessage());
//            
//            throw e;
//        }
//        
//        SortedSet<CorrelatedQueryDataOld>  setPrcdData = this.theCorrelator.getCorrelatedSet();
////        SamplingProcess sp = SamplingProcess.from(setPrcdData);
//        
//        return setPrcdData;
//    }
//    
//    private SortedSet<CorrelatedQueryDataOld> processMultiStream(List<DpDataRequest> lstRequests) throws InterruptedException, TimeoutException, ExecutionException {
//        
//        // Create the multiple stream processing tasks
//        List<QueryStream>  lstStrmTasks = new LinkedList<>();
//        for (DpDataRequest dpRequest : lstRequests) {
//            QueryStream    taskStrm = QueryStream.newTask(
//                    dpRequest, 
//                    this.connQuery.getStubAsync(), 
//                    QueryDataConsumer.newInstance(this.queStreamBuffer)
//                    );
//            
//            lstStrmTasks.add(taskStrm);
//        }
//        
//        // Create the single data processing task thread
//        //  Then start the thread so its ready for incoming data from streams
//        QueryDataProcessor  thdProcessor = QueryDataProcessor.newThread(this.queStreamBuffer, this.theCorrelator);
//        
//        thdProcessor.start();
//        
//        // Now start all data stream tasks and wait for completion
//        this.exeThreadPool.invokeAll(lstStrmTasks, CNT_TIMEOUT, TU_TIMEOUT);
//        
//        // Check for timeout - not all data streams will have completed.
//        boolean bolCompleted = lstStrmTasks.stream().allMatch(p -> p.isCompleted());
//        
//        if (!bolCompleted) {
//            thdProcessor.terminate();
//            
//            String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() + " - Time limit expired while gRPC data streaming.";
//
//            if (BOL_LOGGING)
//                LOGGER.error(strMsg);
//            
//            throw new TimeoutException(strMsg);
//        }
//
//        // Check for streaming error
//        boolean bolSuccess = lstStrmTasks.stream().allMatch(p -> p.isSuccess());
//        
//        if (!bolSuccess) {
//            thdProcessor.terminate();
//            
//            ResultStatus recResult = lstStrmTasks
//                                    .stream()
//                                    .filter(p  -> p.getResult().isFailure())
//                                    .<ResultStatus>map(p -> p.getResult())
//                                    .findAny()
//                                    .get();
//            String strMsg = JavaRuntime.getQualifiedCallerNameSimple() 
//                    + " - At least one gRPC streaming error: message=" 
//                    + recResult.message()
//                    + ( (recResult.hasCause()) ? ", cause=" + recResult.cause() : "");
//            
//            if (BOL_LOGGING)
//                LOGGER.error(strMsg);
//            
//            throw new ExecutionException(strMsg, recResult.cause());
//        }
//        
//        // Recover the total number of messages streamed 
//        int cntMsgs = lstStrmTasks.stream().mapToInt(p -> p.getResponseCount()).sum();
//        
//        // Set the total number of data messages to process then wait for completion
//        thdProcessor.setMaxMessages(cntMsgs);
//        
//        thdProcessor.join(timeoutLimitDefaultMillis());
//
//        // Create the sampling process and return it
//        SortedSet<CorrelatedQueryDataOld>  setPrcdData = this.theCorrelator.getCorrelatedSet();
////        SamplingProcess sp = SamplingProcess.from(setPrcdData);
//        
//        return setPrcdData;
//    }
//    
//    private Thread  newStreamProcessor(DpDataRequest dpRequest) {
//        QueryStream    taskProcessor = QueryStream.newTask(
//                dpRequest, 
//                this.connQuery.getStubAsync(), 
//                QueryDataConsumer.newInstance(this.queStreamBuffer)
//                );
//        
//        Thread  thdProcessor = new Thread(taskProcessor);
//        
//        return thdProcessor;
//    }
}
