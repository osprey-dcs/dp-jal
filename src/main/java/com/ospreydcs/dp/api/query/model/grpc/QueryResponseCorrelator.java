/*
 * Project: dp-api-common
 * File:	QueryResponseCorrelator.java
 * Package: com.ospreydcs.dp.api.query.model.grpc
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
package com.ospreydcs.dp.api.query.model.grpc;

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
import javax.naming.OperationNotSupportedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.ResultRecord;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpDataRequest.CompositeType;
import com.ospreydcs.dp.api.query.model.DpQueryException;
import com.ospreydcs.dp.api.query.model.DpQueryStreamType;
import com.ospreydcs.dp.api.query.model.process.SamplingProcess;
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
 * <code>QueryResponseCorrelator</code> objects performs Data Platform Query Service data requests embodied with
 * <code>{@link DpDataRequest}</code> objects, then correlate the query responses into sorted sets of 
 * <code>{@link CorrelatedQueryData}</code> objects. That is, class objects take inputs of type 
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
 * client request into <code>{@QueryRequest}</code> Protobuf messages recognizable by the Query Service gRPC 
 * interface.
 * </p>
 * <p>
 * All supported Query Service data requests within <code>QueryResponseCorrelator</code> are implemented by 
 * methods prefixed with <code>processRequest</code>.  The method suffix methods indicates the gRPC technique
 * used to recover the request data (i.e. "Unary", "Stream", etc.). All data request methods return sorted sets of
 * <code>{@CorrelatedQueryData}</code> objects containing the results set of the offered data query request.
 * The sorted set contains all requested time-series data, correlated by single sampling clock, and sorted by
 * sampling clock start timestamp.
 * </p>
 * <p>
 * Once a <code>{@link SortedSet}</code> of <code>{@link CorrelatedQueryData}</code> objects is obtained from
 * a request method, it is ready for subsequent processing of the results set.  Each <code>CorrelatedQueryData</code>
 * object contains a sampling clock Protobuf message and all the data column (time-series) messages correlated
 * to that sampling clock.  
 * The class <code>{@link SamplingProcess}</code> is available for subsequent processing of request method outputs
 * into table-like containers for the results set, however, clients may implement their own final processing.
 * </p>
 * <p>
 * <h2>Final Processing</h2>
 * It is important to note that clients of the <code>QueryResponseCorrelator</code> data request methods must 
 * perform all final processing of the correlated output BEFORE invoking another data request.  Correlated data
 * output sets are <em>owned</em> by the <code>QueryResponseCorrelator</code> object performing the request.
 * Existing correlated data output sets will be destroyed (i.e., cleared by the internal 
 * <code>{@link QueryDataCorrelator}</code> instance) whenever a subsequent data request is performed.
 * Either copy the output data to a new container or fully process the output sets before invoking additional
 * data requests.   
 * </p>
 * <p>
 * <h2>Streaming/Processing Configuration</h2>
 * For performance considerations, the streaming and processing operations within a 
 * <code>QueryResponseCorrelator</code> object are configurable.  Optimal configurations are determined by
 * the hosting platform and the types (e.g., sizes) of the typical data request.  
 * The default configuration is obtained from the Data Platform API Query Service configuration within 
 * <code>{@link DpApiConfig}</code>.
 * </p>
 * <p>
 * There are several methods that can be used to modify the default streaming/processing configuration of a 
 * <code>QueryResponseCorrelator</code> object.  All configuration methods are prefixed with <code>set</code>
 * and suffixed by the action they perform.
 * <ul>
 * <li>
 * <code>{@link #setMultiStreaming(boolean)}</code>: Toggles the use of multiple gRPC data streams for recovering
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
 * <code>{@link #setCorrelateMidstream(boolean)}</code>: Toggles the function of performing data correlation while
 * concurrently data streaming; otherwise data is correlated separately, after gRPC data stream completion.
 * </li>
 * </ul>
 * In each case see the method documentation for details of the operation and its effects.
 * </p>
 * <p>
 * <h2>Query Service Connection</h2>
 * A single <code>{@link DpQueryConnection}</code> object is required by a <code>QueryResponseCorrelator</code>
 * objects, which is provided at construction.  The <code>DpQueryConnection</code> object contains the gRPC 
 * channel connection to the Data Platform Query Service used for all gRPC data query and data streaming 
 * operations. <code>QueryResponseCorrelator</code> objects DO NOT take ownership of the Query Service connection.
 * (Ownership is assumed to be that of the client using the <code>QueryResponseCorrelator</code> object.)
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
 * Attempts to use multiple data streams with the default configuration.  The request defined in the argument 
 * is decomposed into smaller composite requests, each recovered on a separate gRPC data stream.
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
 * <code>{@link #CNT_MULTISTREAM}</code> threads will be active any any given instant.  The maximum number of
 * data streams can be set using the <code>{@link DpApiConfig}</code> default API configuration.  It is a 
 * tuning parameter dependent upon gRPC, the number of platform processor cores, and local network traffic.    
 * </p>
 * <p>
 * The <em>type</em> of data stream used for the above request can be specified by the client using the 
 * <code>{@link DpDataRequest#setStreamType(com.ospreydcs.dp.api.query.model.DpQueryStreamType)}</code> method
 * within the data request object.  See the documentation for the method for further details on available gRPC 
 * data stream types (or see enumeration <code>{@link com.ospreydcs.dp.api.query.model.DpQueryStreamType}</code>.
 * (This is a performance option.)
 * </p>
 * <p>
 * <h2>Data Correlation</h2>
 * All data correlation of the incoming Query Service data is performed by a single 
 * <code>{@link QueryDataCorrelator}</code> instance within each <code>QueryResponseCorrelator</code> object.
 * The <code>QueryDataCorrelator</code> attribute is used to correlate all data, regardless of recovery method
 * (e.g., unary request, streaming request, etc.).  The <code>QueryDataCorrelator</code> attribute is reused,
 * that is, the same instance is used for all data requests performed while the <code>QueryResponseCorrelator</code>
 * is alive.
 * </p>
 * <p>
 * Consequently, and because <code>QueryDataCorrelator</code> objects own their processed data sets, output data
 * sets are destroyed whenever a new data request is performed (i.e., using a <code>processRequest...</code> 
 * method.  As previously mentioned, any sorted set of correlated output data MUST be fully processed or copied
 * before invoking another data request. 
 * </p>
 * <p>
 * See the class documentation for <code>{@link QueryDataCorrelator}</code> concerning details about the data
 * correlation process and the resulting correlated data sets it produces.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 5, 2024
 *
 * @see DpDataRequest
 * @see CorrelatedQueryData
 * @see QueryResponseStreamProcessor
 * @see QueryDataCorrelator
 */
public class QueryResponseCorrelator {
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new <code>QueryResponseCorrelator</code> instance connected to the given Data Platform Query
     * Service.
     * <p>
     * The returned <code>QueryResponseCorrelator</code> instance is ready for the recovery of data requests.
     * No shutdown operations are required as the instance does not take ownership of the argument.
     * See the class documentation <code>{@link QueryResponseCorrelator}</code> for instructions and use of the
     * returned object.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The Query Service connection object is provided to the <code>QueryResponseCorrelator</code>
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
     * @return          new <code>QueryResponseCorrelator</code> ready for request and processing operations
     */
    public static QueryResponseCorrelator   from(DpQueryConnection connQuery) {
        return new QueryResponseCorrelator(connQuery);
    }
    
    //
    // Class Types
    //
    
    /**
     * <p>
     * Data consumer operation for <code>{@link QueryResponseStreamProcessor}</code> objects.
     * </p>
     * <p>
     * Implementation of <code>{@link Consumer}</code> interface bound to the 
     * <code>{@link BucketData}</code> Protobuf message type.
     * Transfers request data from a gRPC data stream implemented by 
     * <code>{@link QueryResponseStreamProcessor}</code> to a data buffer of type 
     * <code>{@link Queue}</code collecting response data.
     * </p>
     * <p> 
     * The <code>{@link #accept(BucketData)}</code> method of this class simply transfers a 
     * <code>BucketData</code> message to a <code>{@link Queue}</code> reference, assumed 
     * to be the data buffer of a <code>{@link QueryResponseCorrelator}</code> object.
     * The current number of <code>BucketData</code> messages transferred is available through 
     * the <code>{@link #getMessageCount()}</code> method.
     * </p>
     * <p>
     * Instances are intended to be passed to newly created 
     * <code>{@link QuerResponseStreamProcessor}</code> instances via the creator 
     * <code>{@link QueryResponseStreamProcessor#newTask(DpDataRequest, DpQueryServiceStub, Consumer)}</code>.
     * There should be one instance of this function for every active data stream.
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
         * @param msgData   data message obtained from the associated <code>{@link QueryResponseStreamProcessor}</code>.
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
     * Independent data processing threads for the <code>QueryResponseCorrelator</code> class.
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
     * starting the thread or while the thread is active.  The internal processing loop exits 
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
     * The external <code>{@link QueryResponseCorrelator}</code> object should block on the
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
        public static QueryDataProcessor newThread(BlockingQueue<QueryData> queDataBuffer, QueryDataCorrelator theCorrelator) {
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
        public QueryDataProcessor(BlockingQueue<QueryData> queDataBuffer, QueryDataCorrelator theCorrelator) {
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
        private final BlockingQueue<QueryData>     queDataBuffer;
        
        /** The external data correlator doing the data message processing */
        private final QueryDataCorrelator           theCorrelator;
        
        
        //
        // State Variables
        //
        
        /** thread started flag */
        private boolean         bolStarted = false;
        
        /** Number of data messages to process (unknown until set) */
        private Integer         cntMsgsMax = null;
        
        /** Number of data messages processed so far (internal counter) */
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
         * This is the proper way to terminate an active processing thread.  DO NOT use
         * the method <code>{@link Thread#interrupt()}</code> as it will leave a 
         * <code>null</code> valued <code>{@link ResultRecord}</code> potentially corrupting
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
            
            Thread.currentThread().interrupt();
            
            this.recResult = ResultRecord.newFailure("The data processing thread was terminted externally.");
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
        
//        public boolean isSuccess() {
//            if (this.recResult == null)
//                return false;
//            
//            return this.recResult.isSuccess();
//        }
        
        /**
         * <p>
         * Returns the <code>{@link ResultRecord}</code> containing the status of this processing 
         * task.
         * </p>
         * <p>
         * The returned value will be <code>{@link ResultRecord#SUCCESS}</code> only if the 
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
         * @return  <code>{@link ResultRecord#SUCCESS}</code> if the thread has exited normally,
         *          <code>{@link ResultRecord#isFailure()}</code> result if terminated,
         *          <code>null</code> otherwise
         */
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
         * Runs a continuous loop that polls the 
         * <code>{@link QueryResponseCorrelator#queStreamBuffer}</code> data buffer
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
            
            while (this.cntMsgsMax == null || this.cntMsgsProcessed < this.cntMsgsMax) {
                try {
                    QueryDataResponse.QueryData msgData = this.queDataBuffer.poll(LNG_TIMEOUT, TU_TIMEOUT);

                    // If we timeout try again
                    if (msgData == null) {

                        continue;
                    }

                    // Note that this operation will block until completed
                    this.theCorrelator.addQueryData(msgData);
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
    
    
    /** Use multi-threading for query data correlation */
    public static final boolean     BOL_CORRELATE_CONCURRENCY = CFG_QUERY.data.response.correlate.useConcurrency;
    
    /** Perform data correlation while gRPC streaming - otherwise do it post streaming */
    public static final boolean     BOL_CORRELATE_MIDSTREAM = CFG_QUERY.data.response.correlate.whileStreaming;
    
    
    /** Is response multi-streaming active? */
    public static final boolean     BOL_MULTISTREAM = CFG_QUERY.data.response.multistream.active;
    
    /** Maximum number of open data streams to Query Service */
    public static final int         CNT_MULTISTREAM = CFG_QUERY.data.response.multistream.maxStreams;
    
    /** Query domain size triggering multiple streaming (if active) */
    public static final long        SIZE_MULTISTREAM_PIVOT = CFG_QUERY.data.response.multistream.pivotSize;
    
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
    
    /** The single connection to the Query Service (used for all data requests) */
    private final DpQueryConnection         connQuery;

    
    //
    // Instance Resources
    //
    
    /** The pool of (multiple) gRPC streaming thread(s) for recovering requests */
    private final ExecutorService           exeThreadPool = Executors.newFixedThreadPool(CNT_MULTISTREAM);
    
    /** The queue buffering all response messages for data correlation processing */
    private final BlockingQueue<QueryData>  queStreamBuffer = new LinkedBlockingQueue<>();
    
    /** The single query data theCorrelator used to process all request data */
    private final QueryDataCorrelator       theCorrelator = new QueryDataCorrelator();  
    
    
    //
    //  Configuration
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
     * Constructs a new instance of <code>QueryResponseCorrelator</code> ready for request processing.
     * </p>
     * <p>
     * The Query Service connection object is provided to the <code>QueryResponseCorrelator</code>
     * at construction.  This connection is used for all subsequent data request gRPC operations.
     * Since a single gRPC <code>{@link Channel}</code> object can support multiple concurrent
     * data streams, only one connection is needed. Notes that all request processing methods 
     * (i.e., methods prefixed with <code>process</code>) do not returned until the request is 
     * fully processed.  Thus, multiple <code>{@link Channel}</code> instances are not required.
     * </p>
     * 
     * @param connQuery     the single Query Service connection used for all subsequent data requests 
     */
    public QueryResponseCorrelator(DpQueryConnection connQuery) {
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
    public SortedSet<CorrelatedQueryData>   processRequestUnary(DpDataRequest dpRequest) throws DpQueryException {

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
                LOGGER.error("{}: Query request was rejected by Query Service - {}", JavaRuntime.getCallerName(), e.getMessage());
            
            throw new DpQueryException(e);
            
        } catch (CannotProceedException e) {
            if (BOL_LOGGING)
                LOGGER.error("{}: Query response contained error - {}", JavaRuntime.getCallerName(), e.getMessage());
            
            throw new DpQueryException(e);
        }
        
        // Return the processed data
        SortedSet<CorrelatedQueryData> setDataPrcd = this.theCorrelator.getCorrelatedSet();
        
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
     * <code>{@link #setMultiStreaming(boolean)}</code> before calling this method.
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
     * <li><code>{@link #setCorrelateMidstream(boolean)}</code></li>
     * <li><code>{@link #setCorrelationConcurrency(boolean)}</code></li>
     * <li><code>{@link #setMultiStreaming(boolean)}</code></li>
     * </ul>
     * Note that the <code>{@link #setMultiStreaming(boolean)}</code> has no effect here.
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
     * @see #setCorrelateMidstream(boolean)
     * @see #setCorrelationConcurrency(boolean)
     */
    public SortedSet<CorrelatedQueryData>   processRequestMultiStream(List<DpDataRequest> lstRequests) throws DpQueryException {
        
        // Reset the data theCorrelator
        this.theCorrelator.reset();
        
        // Create streaming task for each component request
        List<QueryResponseStreamProcessor>  lstStrmTasks = this.createStreamingTasks(lstRequests);
        
        // Create the data correlation thread task
        QueryDataProcessor  thdDataProcessor = QueryDataProcessor.newThread(this.queStreamBuffer, this.theCorrelator);

        
        // Start data processing thread if mid-stream processing true
        if (this.bolCorrelateMidstream)
            thdDataProcessor.start();
        
        // Start all streaming tasks and wait for completion 
        int     cntMsgs;    // returns number of response messages streamed
        
        try {
            cntMsgs = this.executeStreamingTasks(lstStrmTasks);
            
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

        } catch (CompletionException e) {
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
        if (recProcessed == null || recProcessed.isFailure()) {
            if (BOL_LOGGING)
                LOGGER.error("{}: Data correlation processing FAILED - {}", JavaRuntime.getCallerName(), recProcessed.message());

            throw new DpQueryException("Data correlation processing FAILED: " + recProcessed.message());
        }
        
        SortedSet<CorrelatedQueryData>  setPrcdData = this.theCorrelator.getCorrelatedSet();
        return setPrcdData;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Attempts to decompose the given data request into a composite request (i.e., for multi-streaming).
     * </p>
     * <p>
     * Attempts a decomposition of the given request into a composite request collection of no more than
     * <code>{@link #CNT_MULTISTREAM}</code> elements.  
     * This method is part of the DEFAULT multi-streaming mechanism within <code>QueryResponseCorrelator</code>.
     * </p>
     * <p>
     * <h2>Request Size</h2>
     * The <code>DpDataRequest</code> class provides a request size estimate given by
     * <code>{@link DpDataRequest#approxRequestSamples(long, TimeUnit)}</code>.  This method returned the 
     * estimated number of data samples within the request given the sampling period.  If this size
     * estimate is less than <code>{@link #SIZE_MULTISTREAM_PIVOT}</code> nothing is done and a list 
     * containing the original data request is returned.
     * </p>
     * <p>
     * The sampling period for the size estimate method is given as a single unit of the 
     * <code>{@link TimeUnit}</code> enumeration specified by class constant 
     * <code>{@link #TU_MULTISTREAM_PERIOD}</code>.  That is, the arguments of the above method are 1L and
     * <code>{@link #TU_MULTISTREAM_PERIOD}</code>, respectively.
     * </p>
     * <p>
     * <h2>Decomposition Strategy</h2>
     * If the data request size estimate is larger than the cutoff limit, a series of evaluations is performed on 
     * the data request to determine a suitable decomposition.  The following conditions, and subsequent actions,
     * are evaluated in order:
     * <ol>
     * <li>
     * <h3>Default Decomposition</h3>
     * Does the <code>DpDataRequest</code> default decomposition provided by 
     * <code>{@link DpDataRequest#decomposeDomainDefault()}</code> yield a query domain decomposition where
     * the number of domain covers &le; <code>{@link #CNT_MULTISTREAM}</code>?  If so, this query domain
     * decomposition is used to produce the returned composite query for multi-streaming.
     * </li>
     * <li>
     * <h3>Horizontal Decomposition</h3>
     * If the number of data sources within the given data request &ge; <code>{@link #CNT_MULTISTREAM}</code>
     * then a "horizontal" query domain decomposition is used to generate the returned composite request.
     * Specifically, the returned value is given by 
     * <code>{@link DpDataRequest#buildCompositeRequest(CompositeType, int)}</code> where the arguments are
     * <code>{@link CompositeType#HORIZONTAL}</code> and <code>{@link #CNT_MULTISTREAM}</code>, respectively.
     * </li>
     * <li>
     * <h3>Vertical Decomposition</h3>
     * If the estimated number of samples within the request &ge; 
     * <code>{@link #CNT_MULTISTREAM}</code> * <code>{@link #SIZE_MULTISTREAM_PIVOT}</code> then a "vertical"
     * query domain decomposition is used to create the returned composite request.  Specifically, the returned
     * value is given by 
     * <code>{@link DpDataRequest#buildCompositeRequest(CompositeType, int)}</code> where the arguments are
     * <code>{@link CompositeType#VERTICAL}</code> and <code>{@link #CNT_MULTISTREAM}</code>, respectively.
     * </li>
     * <li>
     * <h3>Grid Decomposition</h3>
     * If the number of data sources within the request &le; <code>{@link #CNT_MULTISTREAM}</code> / 2 then a
     * "grid" query domain decomposition is used to create the returned composite request.  Specifically, the
     * returned value is given by 
     * <code>{@link DpDataRequest#buildCompositeRequest(CompositeType, int)}</code> where the arguments are
     * <code>{@link CompositeType#GRID}</code> and <code>{@link #CNT_MULTISTREAM}</code>, respectively.
     * </li>
     * </ol>
     * If all the above conditions fail, the data request is consider "undecomposable".  
     * The returned value is then a one-element list containing the original data request.  
     * </p>
     * 
     * @param dpRequest data request to be decomposed for multi-streaming 
     * 
     * @return  the composite data request obtained from the above decomposition strategy
     */
    private List<DpDataRequest> attemptRequestDecomposition(DpDataRequest dpRequest) {
        
        // Check if request size approximation is large enough to pivot to multi-streaming
        long szRequest = dpRequest.approxRequestSamples(1, TU_MULTISTREAM_PERIOD);
        
        if (szRequest < SIZE_MULTISTREAM_PIVOT)
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
        long lngSmpsPerRqst = szRequest / SIZE_MULTISTREAM_PIVOT;
        int  cntSmpsPerRqst = Long.valueOf(lngSmpsPerRqst).intValue();
        
        //  Add any remainder (just in case)
        cntSmpsPerRqst += (szRequest % SIZE_MULTISTREAM_PIVOT > 0) ? 1 : 0;
        
        if (cntSmpsPerRqst < CNT_MULTISTREAM) {
            lstCmpRqsts = dpRequest.buildCompositeRequest(CompositeType.VERTICAL, CNT_MULTISTREAM);
            
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
     * @param lstRequests   composite data request, one request for each data stream
     * 
     * @return  collection of gRPC stream processor tasks suitable for thread execution
     */
    private List<QueryResponseStreamProcessor>  createStreamingTasks(List<DpDataRequest> lstRequests) {
        
        // Container for multiple stream processing tasks
        List<QueryResponseStreamProcessor>  lstStrmTasks = new LinkedList<>();
        
        // Create a gRPC stream processor data for each component request within the argument
        for (DpDataRequest dpRequest : lstRequests) {
            DpQueryStreamType       enmStreamType = dpRequest.getStreamType();
            QueryDataRequest        msgRequest = dpRequest.buildQueryRequest();
            DpQueryServiceStub      stubAsync = this.connQuery.getStubAsync();
            QueryDataConsumer       fncConsumer = QueryDataConsumer.newInstance(this.queStreamBuffer);
            
            QueryResponseStreamProcessor    taskStrm = QueryResponseStreamProcessor.newTask(
                    enmStreamType,
                    msgRequest, 
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
    private int executeStreamingTasks(List<QueryResponseStreamProcessor> lstStrmTasks) 
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
            ResultRecord recResult = lstStrmTasks
                                    .stream()
                                    .filter(p  -> p.getResult().isFailure())
                                    .<ResultRecord>map(p -> p.getResult())
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
    
//    private SortedSet<CorrelatedQueryData> processSingleStream(DpDataRequest dpRequest) throws InterruptedException, TimeoutException, ExecutionException {
//
//        QueryResponseStreamProcessor    taskStream = QueryResponseStreamProcessor.newTask(
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
//                ResultRecord    recResult = taskStream.getResult();
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
//            SortedSet<CorrelatedQueryData>  setPrcdData = this.theCorrelator.getCorrelatedSet();
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
//                ResultRecord    recResult = taskStream.getResult();
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
//        SortedSet<CorrelatedQueryData>  setPrcdData = this.theCorrelator.getCorrelatedSet();
////        SamplingProcess sp = SamplingProcess.from(setPrcdData);
//        
//        return setPrcdData;
//    }
//    
//    private SortedSet<CorrelatedQueryData> processMultiStream(List<DpDataRequest> lstRequests) throws InterruptedException, TimeoutException, ExecutionException {
//        
//        // Create the multiple stream processing tasks
//        List<QueryResponseStreamProcessor>  lstStrmTasks = new LinkedList<>();
//        for (DpDataRequest dpRequest : lstRequests) {
//            QueryResponseStreamProcessor    taskStrm = QueryResponseStreamProcessor.newTask(
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
//            ResultRecord recResult = lstStrmTasks
//                                    .stream()
//                                    .filter(p  -> p.getResult().isFailure())
//                                    .<ResultRecord>map(p -> p.getResult())
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
//        SortedSet<CorrelatedQueryData>  setPrcdData = this.theCorrelator.getCorrelatedSet();
////        SamplingProcess sp = SamplingProcess.from(setPrcdData);
//        
//        return setPrcdData;
//    }
//    
//    private Thread  newStreamProcessor(DpDataRequest dpRequest) {
//        QueryResponseStreamProcessor    taskProcessor = QueryResponseStreamProcessor.newTask(
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
