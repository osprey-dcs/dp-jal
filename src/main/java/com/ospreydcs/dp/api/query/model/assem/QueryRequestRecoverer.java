/*
 * Project: dp-api-common
 * File:	QueryRequestRecoverer.java
 * Package: com.ospreydcs.dp.api.query.impl
 * Type: 	QueryRequestRecoverer
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
 * @since Apr 22, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.assem;

import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;

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
import com.ospreydcs.dp.api.query.model.correl.MessageTransferTask;
import com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData;
import com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator;
import com.ospreydcs.dp.api.query.model.grpc.QueryChannel;
import com.ospreydcs.dp.api.query.model.grpc.QueryMessageBuffer;
import com.ospreydcs.dp.api.query.model.grpc.QueryStream;
import com.ospreydcs.dp.api.query.model.request.DataRequestDecomposer;
import com.ospreydcs.dp.api.query.model.request.RequestDecompParams;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.TimestampList;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;

/**
 * <p>
 * Class for recovering Query Service time-series data requests and time correlating the result set.
 * </p>
 * <p>
 * The <code>QueryRequestRecoverer</code> class performs 2 functions: 1) time-series data request recovery 
 * and 2) time correlation of the requested result set.  Internally, 
 * a <code>QueryChannel</code> object performs Data Platform Query Service data requests embodied with
 * the offered <code>{@link DpDataRequest}</code> objects, recovering the request data.  
 * Then a <code>QueryDataCorrelatorOld</code> object correlates the query responses into sorted sets of 
 * <code>{@link RawCorrelatedData}</code> objects. That is, <code>QueryRequestRecoverer</code> class 
 * objects take inputs of type <code>DpDataRequest</code> and then output 
 * <code>{@link SortedSet}</code> collections containing the correlated results sets.  
 * All gRPC data streaming and correlation processing are performed internally.
 * </p>
 * <p>
 * The motivation for combining request data recovery and data correlation operations is both 1) for performance and
 * 2) for post-processing of query results, if necessary.  
 * <ol>
 * <li>
 * For large data requests gRPC streaming operation can extend over significant real-time.  
 * By starting the data correlation processing on the request data that is currently available, 
 * the overall data recovery and correlation time can be reduced.  However, it is important that the data 
 * correlation operation does not interfere with the gRPC streaming, for example, by allocating too many CPU resources.
 * </li>
 * <li>
 * Although not presently implemented, we may wish to implement the ability to filter the query results according to
 * parameters within the original data request within the initiating <code>DpDataRequst</code> instance.  Thus, we
 * require access to the original <code>DpDataRequest</code> instance to do so.
 * </li>
 * </ol>
 * </p>
 * <p>
 * <h2>Query Requests</h2>
 * Within the Query Service API library, time-series data requests are represented by <code>{@link DpDataRequest}</code> 
 * objects.  These objects contain the defining parameters for a Query Service data request, then transform the 
 * client request into <code>{@link QueryDataRequest}</code> Protocol Buffers messages recognizable by the Query Service gRPC 
 * interface.
 * </p>
 * <p>
 * All supported Query Service data requests within <code>QueryRequestRecoverer</code> are implemented by 
 * methods prefixed with <code>processRequest</code>.  All data request methods return sorted sets of
 * <code>{@RawCorrelatedData}</code> objects containing the results set of the offered data query request.
 * The sorted set contains all requested time-series data, correlated by single sampling clock, and sorted by
 * sampling clock start timestamp.
 * <p>
 * The ability to support timestamp lists within <code>{@link QueryDataRequest}</code> message is implemented within
 * the <code>QueryRequestRecoverer</code>.  Thus, all timestamp situations are now supported.
 * </p>
 * <p>
 * Once a <code>{@link SortedSet}</code> of <code>{@link RawCorrelatedData}</code> objects is obtained from
 * a request method, it is ready for subsequent processing of the results set.  Each <code>RawCorrelatedData</code>
 * object contains either a sampling clock Protocol Buffers message (i.e., <code>{@link SampleClock}</code>) or
 * an explicit timestamp list (i.e., <code>{@link TimestampList}</code> and all the data column (time-series) 
 * messages correlated to the timestamps.  
 * The class <code>{@link QueryResponseAssembler}</code> is available for subsequent processing of request method 
 * outputs into table-like containers for the results set, however, clients may implement their own final processing.
 * </p>
 * <p>
 * <h2>Final Processing</h2>
 * It is important to note that clients of the <code>QueryRequestRecoverer</code> data request methods must 
 * perform all final processing of the correlated output BEFORE invoking another data request.  Correlated data
 * output sets are <em>owned</em> by the <code>QueryRequestRecoverer</code> instance performing the request.
 * Existing correlated data output sets will be destroyed (i.e., cleared by the internal 
 * <code>{@link RawDataCorrelator}</code> instance) whenever a subsequent data request is performed.
 * Either copy the output data to a new container or fully process the output sets before invoking additional
 * data requests.   
 * </p>
 * <p>
 * <h2>Streaming/Processing Configuration</h2>
 * For performance considerations, the streaming and processing operations within a 
 * <code>QueryRequestRecoverer</code> object are configurable.  Optimal configurations are determined by
 * the hosting platform and the types (e.g., sizes) of the typical data request.  
 * The default configuration is obtained from the Data Platform API Query Service configuration within 
 * <code>{@link DpApiConfig}</code>.
 * </p>
 * <p>
 * There are several methods that can be used to modify the default streaming/processing configuration of a 
 * <code>QueryResultProcessor</code> object.  All configuration methods are prefixed with either an 
 * <code>enable</code> or <code>set</code> and suffixed by the action they perform.
 * <ul>
 * <li>
 * <code>{@link #enableMultiStreaming(boolean)}</code>: Toggles the use of multiple gRPC data streams for recovering
 * Query Service request data. Query requests are decomposed for multi-streaming via an internal algorithm. 
 * </li>
 * <br/>
 * <li>
 * <code>{@link #enableCorrelationConcurrency(boolean)}</code>: Toggles the use of concurrent, multi-threaded processing 
 * for the results set data correlation.  Parallelism improves correlation performance for large request sets but
 * can also sequester CPU resources.  
 * </li>
 * <br/>
 * <li>
 * <code>{@link #enableCorrelateWhileStreaming(boolean)}</code>: Toggles the function of performing data correlation while
 * concurrently data streaming; otherwise data is correlated separately, after gRPC data stream completion.
 * </li>
 * <li>
 * <code>{{@link #setMultiStreamingMaxStreamCount(int)}</code>: Sets the maximum allowable number of gRPC data streams for data recovery
 * when multi-streaming is enabled.
 * </li>
 * </ul>
 * In each case see the method documentation for details of the operation and its effects.
 * </p>
 * <p>
 * <h2>Query Service Connection</h2>
 * A single <code>{@link DpQueryConnection}</code> object is required by a <code>QueryRequestRecoverer</code>
 * object, which is provided at construction.  The <code>DpQueryConnection</code> object contains the gRPC 
 * channel connection to the Data Platform Query Service used for all gRPC data query and data streaming 
 * operations. <code>QueryRequestRecoverer</code> objects DO NOT take ownership of the Query Service connection.
 * (Ownership is assumed to be that of the client using the <code>QueryRequestRecoverer</code> object.)
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
 * <code>{@link #processRequests(List)}</code> - Explicitly multi-streaming of data requests according
 * to client-provided argument list.  The client supplies a list of data requests each of which are recovered
 * on a separate data stream.
 * </li>
 * </ul>
 * See the documentation for each method for further details.
 * </p>
 * <p>
 * When using method <code>{@link #processRequest(DpDataRequest)}</code> , the number of concurrent data streams will always
 * be limited by the value of constant <code>{@link #CNT_MULTISTREAM_MAX_STRMS}</code>.  
 * If a client explicitly requests a stream count larger than that value (i.e., with 
 * <code>{@link #processRequests(List)}</code>), the number of data stream is equal to the number of requests 
 * in the argument list.  Use this method at your
 * own risk.  Method <code>{@link #processRequest(DpDataRequest)}</code> is the preferred request method. 
 * The maximum number of data streams can be set using the <code>{@link DpApiConfig}</code> default API configuration.  
 * It is a tuning parameter dependent upon gRPC, the number of platform processor cores, and local network traffic.    
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
 * <code>{@link RawDataCorrelator}</code> instance within each <code>QueryRequestRecoverer</code> object.
 * The <code>RawDataCorrelator</code> attribute is used to correlate all data, regardless of recovery method
 * (e.g., unary request, streaming request, etc.).  The <code>RawDataCorrelator</code> attribute is reused,
 * that is, the same instance is used for all data requests performed while the <code>QueryRequestRecoverer</code>
 * is alive.
 * </p>
 * <p>
 * Consequently, and because <code>RawDataCorrelator</code> objects own their processed data sets, output data
 * sets are destroyed whenever a new data request is performed (i.e., using a <code>processRequest...</code> 
 * method.  As previously mentioned, any sorted set of correlated output data MUST be fully processed or copied
 * before invoking another data request. 
 * </p>
 * <p>
 * See the class documentation for <code>{@link RawDataCorrelator}</code> concerning details about the data
 * correlation process and the resulting correlated data sets it produces.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 22, 2025
 *
 * @see DpDataRequest
 * @see RawDataCorrelator
 * @see RawCorrelatedData
 * @see QueryStream
 * @see QueryChannel
 *
 */
public class QueryRequestRecoverer {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new instance of <code>QueryRequestProcessorOld</code>.
     * </p>
     * <p>
     * The returned instance is configured according to the default configuration specified in the 
     * Java API Library  (see <code>{@link DpApiConfig}</code>).  There are available setter and
     * enable methods for re-configuration.
     * </p>
     * <p>
     * Note that the instance does not assume ownership of the given connection.  It must
     * be currently connected to the Query Service and must be shown down externally when
     * no longer needed.
     * </p>
     *
     * @return  a new <code>QueryRequestProcessorNex</code> instance ready for recovery and correlation of data requests
     */
    public static QueryRequestRecoverer    from(DpQueryConnection connQuery) {
        return new QueryRequestRecoverer(connQuery);
    }
    
    //
    // Application Resources
    //
    
    /** The Data Platform Query Service default parameters */
    private static final DpQueryConfig CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants - Initialized from JAL configuration file
    //
    
    /** Event logging enabled flag */
    public static final boolean     BOL_LOGGING = CFG_QUERY.logging.enabled;
    
    /** Event logging level */
    public static final String      STR_LOGGING_LEVEL = CFG_QUERY.logging.level;
    
    
    /** Is timeout limit enabled ? */
    public static final boolean     BOL_TIMEOUT = CFG_QUERY.timeout.enabled;
    
    /** Timeout limit for query operation */
    public static final long        CNT_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** Timeout unit for query operation */
    public static final TimeUnit    TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
    /** Is response multi-streaming enabled? */
    public static final boolean     BOL_MULTISTREAM_ENABLE = CFG_QUERY.data.recovery.multistream.enabled;
    
    /** Maximum number of open data streams to Query Service */
    public static final int         CNT_MULTISTREAM_MAX_STRMS = CFG_QUERY.data.recovery.multistream.maxStreams;
    
    /** Query domain size triggering multiple streaming (if enabled) */
    public static final long        SIZE_MULTISTREAM_DOMAIN = CFG_QUERY.data.recovery.multistream.sizeDomain;
    
    
    /** Perform data correlation while gRPC streaming - otherwise do it post streaming */
    public static final boolean     BOL_CORRELATE_MIDSTREAM = CFG_QUERY.data.recovery.correlate.whileStreaming;
    
    /** Use multi-threading for query data correlation */
    public static final boolean     BOL_CORRELATE_CONCURRENCY = CFG_QUERY.data.recovery.correlate.concurrency.enabled;
    
    /** The correlator raw-data target set limit before multi-threading is invoked (if enabled) */
    public static final int         BOL_CORRELATE_SZ_PIVOT = CFG_QUERY.data.recovery.correlate.concurrency.pivotSize;
    
    /** Maximum number of processing threads for concurrent query data correlation */
    public static final int         CNT_CORRELATE_MAX_THRDS = CFG_QUERY.data.recovery.correlate.concurrency.maxThreads;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    /** Class Resource Initialization - Initializes the event logger, sets logging level. */
    static { 
        Configurator.setLevel(LOGGER, Level.toLevel(STR_LOGGING_LEVEL, LOGGER.getLevel())); 
    }
    
    
    //
    // Initializing Attributes
    //
    
    /** The single connection to the Query Service (used for all data requests) */
    private final DpQueryConnection     connQuery;

    
    //
    //  Configuration
    //
    
    /** Multi-streaming query response toggle */
    private boolean bolMultiStream = BOL_MULTISTREAM_ENABLE;
    
    /** The request domain size (PV count - time) triggering multi-streaming */
    private long    szDomainMultiStream = SIZE_MULTISTREAM_DOMAIN;
    
    /** Number of data streams for multi-streaming response recovery */
    private int     cntMaxStreams = CNT_MULTISTREAM_MAX_STRMS;

    
//    /** Use multi-threading for query data correlation */
//    private boolean bolCorrelateConcurrenly = BOL_CORRELATE_CONCURRENCY;
    
    /** Perform query data correlation concurrent while gRPC streaming */
    private boolean bolCorrelateMidstream = BOL_CORRELATE_MIDSTREAM;
    
//    /** Maximum number of execution threads for concurrent query data correlation */
//    private int     cntCorrelateMaxThrds = CNT_CORRELATE_MAX_THRDS;
    
    
    //
    // Instance Resources
    //
    
    /** Response Buffer - The queue buffering all response messages for data correlation processing */
    private final QueryMessageBuffer        queMsgBuffer;
    
    /** The multi-stream channel connection with the Query Service - for request data recovery */
    private final QueryChannel              chanQuery;
    
    /** The data request decomposer needed for multi-streaming requests */
    private final DataRequestDecomposer     prcrDecomposer;
    
    /** The single query data theCorrelator used to process all request data */
    private final RawDataCorrelator         prcrCorrelator;
    
    
    /** The independent task transferring response messages to the recovery buffer */
    private MessageTransferTask             thdMsgXferTask;
    

    //
    // Process/State Variables
    //
    
    /** The request identifier of the original time-series data request */
    private String                  strRqstId = null;
    
    /** The composite request created from the current request (when {@link #processRequest(DpDataRequest)} was invoked) */
    private List<DpDataRequest>     lstCompRqsts = null;
    
    /** The number of messages recovered and processed from the current request */
    private int                     cntMsgsProcessed = 0;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryRequestProcessorOld</code>.
     * </p>
     * <p>
     * Note that the instance does not assume ownership of the given connection.  It must
     * be currently connected to the Query Service and must be shown down externally when
     * no longer needed.
     * </p>
     *
     * @param connQuery enabled connection to the Query Service
     */
    public QueryRequestRecoverer(DpQueryConnection connQuery) {
        this.connQuery = connQuery;
        
        this.queMsgBuffer = QueryMessageBuffer.create();
        this.prcrDecomposer = DataRequestDecomposer.create();
        this.prcrCorrelator = RawDataCorrelator.create();

        this.chanQuery = QueryChannel.from(this.connQuery, this.queMsgBuffer);
    }

    
    //
    // Configuration 
    //
    
    /**
     * <p>
     * Resets all configuration parameters to the default values.
     * </p>
     * <p>
     * All configuration parameters are set to the default values at creation and specified by
     * the Java API Library configuration file.  These values are all available individually from the
     * following methods:
     * <ul>
     * <li><code>{@link #isMultiStreaming()}</code></li>
     * <li><code>{@link #getMultiStreamingDomainSize()}</code></li>
     * <li><code>{@link #getMultiStreamingMaxStreamCount()}</code></li>
     * <li><code>{@link #getRequestDecompMaxPvCount()}</code></li>
     * <li><code>{@link #getRequestDecompMaxTimeRange()}</code></li>
     * <li><code>{@link #isCorrelateConcurrencyEnabled()}</code></li>
     * <li><code>{@link #isCorrelatingWhileStreaming()}</code></li>
     * <li><code>{@link #getCorrelateMaxThreads()}</code></li>
     * </ul>
     * </p>
     * 
     * @see #BOL_MULTISTREAM_ENABLE
     * @see #SIZE_MULTISTREAM_DOMAIN
     * @see #CNT_MULTISTREAM_MAX_STRMS
     * @see #BOL_CORRELATE_CONCURRENCY
     * @see #BOL_CORRELATE_CONCURRENCY
     * @see #CNT_CORRELATE_MAX_THRDS
     */
    public void resetDefaultConfiguration() {
        
        this.bolMultiStream = BOL_MULTISTREAM_ENABLE;
        this.szDomainMultiStream = SIZE_MULTISTREAM_DOMAIN;
        this.cntMaxStreams = CNT_MULTISTREAM_MAX_STRMS;
        
//        this.bolCorrelateConcurrenly = BOL_CORRELATE_CONCURRENCY;
        this.bolCorrelateMidstream = BOL_CORRELATE_MIDSTREAM;
//        this.cntCorrelateMaxThrds = CNT_CORRELATE_MAX_THRDS;
        
        this.prcrCorrelator.resetDefaultConfiguration();
        this.prcrDecomposer.resetDefaultCofiguration();
    }
    
    /**
     * <p>
     * Enables or disables data request decomposition.
     * </p>
     * <p>
     * If data request decomposition is disabled the original request will always be returned from
     * any decomposition operation.  Specifically, the decomposition method will return a list containing
     * a single element, the original request object.
     * </p>
     * <p>
     * This is a configuration state variable for the internal <code>DataRequestDecomposer</code> instance.
     * </p>
     * 
     * @param bolEnable the toggling on/off of request decomposition 
     */
    public void enableRequestDecomposition(boolean bolEnable) {
        this.prcrDecomposer.enable(bolEnable);;
    }
    
    /**
     * <p>
     * Sets the maximum number of data sources allowed in a composite data request.
     * </p>
     * <p>
     * Time-series data requests are decomposed horizontally (by data source) using this value when invoking
     * the <code>{@link #processRequest(DpDataRequest)}</code> method and multi-streaming is enabled.  
     * No composite request will have the number of data sources larger than this value if decomposition 
     * was successful.
     * </p>
     * <p>
     * The default value for this parameter is assigned to the <code>{@link DataRequestDecomposer}</code> 
     * instance used in decomposition.  Its value is taken from the Java API Library configuration file and
     * available at <code>{@link DataRequestDecomposer#CNT_MAX_SOURCES}</code>.
     * </p>
     * <p>
     * <h2>USER NOTE:</h2>
     * This parameter affects the operation of the <code>{@link #processRequest(DpDataRequest)}</code>
     * method only.  This method performs any request decomposition on the offered time-series data
     * request then offers the resulting list of composite requests to the <code>{@link #processRequests(List)}</code>
     * method.  
     * The method <code>{@link #processRequests(List)}</code> always performs request recovery on all 
     * offered <code>DpDataRequest</code> instances as they are (i.e., using multiple gRPC data streams).
     * </p>
     * 
     * @param cntMaxSources maximum number of data sources allowed in composite requests when multi-streaming
     */
    public void setRequestDecompMaxPvCount(int cntMaxSources) {
        this.prcrDecomposer.setMaxDataSources(cntMaxSources);
    }
    
    /**
     * <p>
     * Sets the maximum time range allowed in a composite data request.
     * </p>
     * <p>
     * Time-series data requests are decomposed vertically (by time range) using this value when invoking
     * the <code>{@link #processRequest(DpDataRequest)}</code> method and multi-streaming is enabled.
     * No composite request will have the time range of the request larger than this value if decomposition
     * was successful.
     * </p>
     * <p>
     * The default value for this parameter is assigned to the <code>{@link DataRequestDecomposer}</code> 
     * instance used in decomposition.  Its value is taken from the Java API Library configuration file and
     * available at <code>{@link DataRequestDecomposer#DUR_MAX}</code>.
     * </p>
     * <p>
     * <h2>USER NOTE:</h2>
     * This parameter affects the operation of the <code>{@link #processRequest(DpDataRequest)}</code>
     * method only.  This method performs any request decomposition on the offered time-series data
     * request then offers the resulting list of composite requests to the <code>{@link #processRequests(List)}</code>
     * method.  
     * The method <code>{@link #processRequests(List)}</code> always performs request recovery on all 
     * offered <code>DpDataRequest</code> instances as they are (i.e., using multiple gRPC data streams).
     * </p>
     * 
     * @param durRange  the maximum time duration allowed in composite requests when multi-streaming
     */
    public void setRequestDecompMaxTimeRange(Duration durRange) {
        this.prcrDecomposer.setMaxDuration(durRange);
    }
    
    /**
     * <p>
     * Enables/disables the use of concurrency (i.e. multi-threading) for correlation of raw query data.
     * </p>
     * <p>
     * This is a performance parameter where correlation of query data is performed using
     * parallelism.  Due to the nature of data correlation, multiple data sets can be
     * processed simultaneously.  Thus, speedup is directly proportional to the number of
     * CPU cores available.
     * </p> 
     * <p>
     * The internal <code>{@link RawDataCorrelator}</code> instance used to correlate the
     * results set of a Query Service data request has parallel processing capability.  This
     * function can be toggled on or off.  Parallel processing of request data can greatly
     * enhance performance, especially for large results sets.  However, it can also allocates
     * processor resources (i.e., cores) from other concurrent, possibly critical, processes (e.g., 
     * streaming resources).  Thus, in some situations the overall effect could be performance
     * degradation.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The number of correlation processing threads is taken from the API default parameters.</li>
     * <li>Concurrency is only invoked if data sizes are greater than the "pivot size" in the API default parameters.</li>
     * </ul>
     * </p>
     * 
     * @param bolCorrelateConcurrently   enable/disable concurrency for data correlation
     */
    public void enableCorrelateConcurrency(boolean bolCorrelateConcurrently) {
//        this.bolCorrelateConcurrenly = bolCorrelateConcurrently;
        this.prcrCorrelator.enableConcurrency(bolCorrelateConcurrently);
    }
    
    /**
     * <p>
     * Sets the maximum number of allowable processing threads for raw data correlation when correlation 
     * concurrency is enabled.
     * </p>
     * <p>
     * The argument specifies maximum number of execution threads used by the internal <code>RawDataCorrelator</code>
     * instance when processing the recovered raw data.  The parameter only has context when concurrent query data
     * correlation is enabled (see <code>{@link #enableCorrelateConcurrency(boolean)}</code>).
     * </p>
     * <p>  
     * This is a tuning parameter that is likely dependent upon
     * the host platform and other configuration parameters.  In particular, if concurrent correlation and data
     * recovery is enabled (see <code>{@link #enableCorrelateWhileStreaming(boolean)}</code>) a large argument
     * could create a competition between the <code>QueryChannel</code> instance and the <code>RawDataCorrelator</code>
     * instance.
     * </p>
     * <p>
     * The default value taken from the Java API Library configuration file
     * (see <code>{@link #CNT_CORRELATE_MAX_THRDS}</code>).  This value can be recovered from 
     * <code>{@link #getCorrelateMaxThreads()}</code>.  The default value for concurrent processing is available
     * from <code>{@link #bolCorrelateConcurrenly}</code> while the current configuration value is given by
     * <code>{@link #isCorrelateConcurrencyEnabled()}</code>.  
     * The default value for the maximum number of gRPC data streams is found at <cod>{@link #CNT_MULTISTREAM_MAX_STRMS}</code>.
     * </p>
     * 
     * @param cntMaxThreads the maximum number of allowable concurrent processing threads for data correlation when enabled  
     */
    public void setCorrelateConcurrencyMaxThreads(int cntMaxThreads) {
//        this.cntCorrelateMaxThrds = cntMaxThreads;
        this.prcrCorrelator.setMaxThreadCount(cntMaxThreads);
    }
    
    /**
     * <p>
     * Sets the size of the correlated raw data set (i.e., number of blocks) triggering concurrent processing.
     * </p>
     * <p>
     * Specifies the raw data set size limit triggering concurrency in the internal <code>RawDataCorrelator</code>
     * processor.  This parameter is enabled only if concurrent processing is enabled 
     * (see <code>{@link #enableCorrelateConcurrency(boolean)}</code>).
     * This is a tuning parameter which prevent concurrent processing for correlated data set sizes below the
     * given value.  The intent is to prevent the allocation of independent thread resources if none are required.
     * A value of 1 will ensure concurrent processing for all situations.
     * </p>
     * <p>
     * The internal <code>{@link RawDataCorrelator}</code> instance used to correlate the
     * results set of a Query Service data request has parallel processing capability.  This
     * function can be to set a size limit of the target set of raw correlated data blocks before 
     * multi-threaded (parallel) processing is triggered.  Parallel processing of request data can greatly
     * enhance performance, especially for large results sets.  However, it can also allocates
     * processor resources (i.e., cores) from other concurrent, possibly critical, processes (e.g., 
     * streaming resources).  Thus, in some situations the overall effect could be performance
     * degradation.
     * </p>
     * <p>
     * The default value for this parameter is given by the constant <code>{@link #BOL_CORRELATE_SZ_PIVOT}</code> which
     * is taken from the Java API Library configuration parameters.
     * </p>   
     * 
     * @param szPivot   set of the correlated data set triggering concurrent processing
     */
    public void setCorrelateConcurrencyPivotSize(int szPivot) {
        this.prcrCorrelator.setConcurrencyPivotSize(szPivot);
    }
    
    /**
     * <p>
     * Toggles the use of multiple query response gRPC data streams in request data recovery.
     * </p>
     * <p>
     * This class is capable of using multiple gRPC data streams to recover query result sets.  
     * When enabled, large data requests are decomposed into composite requests.  The composite
     * requests are then recovered on separate gRPC data streams and collected in the response 
     * message buffer.  This operation can enable/disable this feature.
     * </p>
     * <p>
     * <h2>Multi-stream Request Recovery</h2>
     * Query data result sets can be recovered using multiple gRPC data streams.  There, the domain of the
     * data  request is decomposed using the decompose query mechanism and the current settings
     * (see <code>{@link DataRequestDecomposer}</code>).  
     * The components of the decompose request are then separate performed on independent gRPC data streams.
     * The full result set is then assembled from the multiple data streams.
     * </p>
     * <p>
     * <h2>Multi-stream Settings</h2>
     * The default multi-streaming settings are taken from the default Java API Library configuration 
     * (see <code>{@link DpApiConfig}</code>).  The class offers multiple methods for changing
     * the default parameters. The maximum number of gRPC data streams is also taken from the default 
     * Java API Library configuration.
     * </p>
     * <p>
     * The default value taken from the Java API Library configuration file
     * (see <code>{@link #BOL_MULTISTREAM_ENABLE}</code>).  This value can be recovered from 
     * <code>{@link #isMultiStreaming()}</code>.  The default value for the maximum number of 
     * gRPC data streams is found at <cod>{@link #CNT_MULTISTREAM_MAX_STRMS}</code>.
     * </p>
     * <p>
     * <h2>Performance</h2>
     * This is a performance parameter where a potentially large results set can be recovered
     * from the Query Service on concurrent gRPC data streams.  Any performance increase is
     * dependent upon the size of the results set, the network and its current traffic, and
     * the host platform configuration.
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
     */
    public void enableMultiStreaming(boolean bolMultiStream) {
        this.bolMultiStream = bolMultiStream;
    }
    
    /**
     * <p>
     * Sets the request domain size threshold to trigger multi-streaming of request result sets.
     * </p>
     * <p>
     * If the multi-streaming feature is enabled (see <code>{@link #isMultiStreaming()}</code>)
     * it will only be triggered if the request domain size is greater than the given value.
     * The approximate domain size of a request is given by the method 
     * <code>{@link DpDataRequest#approxDomainSize()}</code>.  
     * </p>
     * <p>
     * Requiring that data requests have a given domain size is a performance issue and the given value is
     * thus a performance parameter.  The idea is to limit the use of multiple, concurrent data streams for
     * large requests.  Creating multiple gRPC data streams for a small request can allocate unnecessary 
     * resources.
     * </p>
     * <p>
     * The default value is taken from the Java API Library configuration parameters and is available at
     * <code>{@link #SIZE_MULTISTREAM_DOMAIN}</code>.
     * </p> 
     * <p>
     * <h2>USER NOTE:</h2>
     * This parameter affects the operation of the <code>{@link #processRequest(DpDataRequest)}</code>
     * method only.  This method performs any request decomposition on the offered time-series data
     * request then offers the resulting list of composite requests to the <code>{@link #processRequests(List)}</code>
     * method.  
     * The method <code>{@link #processRequests(List)}</code> always performs request recovery on all 
     * offered <code>DpDataRequest</code> instances as they are (i.e., using multiple gRPC data streams).
     * </p>
     *  
     * @param szDomain  the request domain size (in source-count seconds) threshold where multis-treaming is triggered
     */
    public void setMultiStreamingDomainSize(long szDomain) {
        this.szDomainMultiStream = szDomain;
    }
    
    /**
     * <p>
     * Sets the maximum number of gRPC data stream used for multi-streaming query responses.
     * </p>
     * <p>
     * The value is used by <code>{@link #processRequest(DpDataRequest)}</code> to limit the number of
     * composite data requests.  This value is ignored by <code>{@link #processRequests(List)}</code>.
     * </p>
     * <p>
     * This method should be called before any query response processing has started, if at all.  The default value
     * in the Java API Library configuration file should be used as a matter of course.  However, this method is
     * available for performance evaluations. 
     * </p>
     * <p>
     * <h2>USER NOTE:</h2>
     * This parameter affects the operation of the <code>{@link #processRequest(DpDataRequest)}</code>
     * method only.  This method performs any request decomposition on the offered time-series data
     * request then offers the resulting list of composite requests to the <code>{@link #processRequests(List)}</code>
     * method.  
     * The method <code>{@link #processRequests(List)}</code> always performs request recovery on all 
     * offered <code>DpDataRequest</code> instances as they are (i.e., using multiple gRPC data streams).
     * </p>
     * 
     * @param cntMaxStreams    maximum number of gRPC data streams to use in multi-streaming data request recovery
     * 
     * @throws IllegalStateException    method called while actively processing
     */
    synchronized 
    public void setMultiStreamingMaxStreamCount(int cntStreams) throws IllegalStateException {

        if (this.thdMsgXferTask!=null && this.thdMsgXferTask.isAlive()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Cannot change stream count while processing.";
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new IllegalStateException(strMsg);
        }
        
        this.cntMaxStreams = cntStreams;
    }
    
    /**
     * <p>
     * Toggles the application of query data correlation during data recovery (i.e., gRPC streaming operations).
     * </p>
     * <p>
     * This is a performance parameter where the correlation of query data is performed 
     * simultaneously with raw data recovery (i.e., while gRPC data streaming).  The data stream(s) for large 
     * results set can extend over significant real time.  Thus, performing correlation while receiving
     * data can decrease overall processing/transmission time.
     * </p>
     * <p>
     * If this feature is disabled then all query data correlation is done after gRPC streaming completed 
     * and all raw query results have been recovered.
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
    public void enableCorrelateWhileStreaming(boolean bolCorrelateMidstream) {
        this.bolCorrelateMidstream = bolCorrelateMidstream;
    }
    
    
    //
    // Configuration Inquiry
    //
    
    /**
     * <p>
     * Determines whether or not data request decomposition is enabled.  
     * </p>
     * <p>If the returned value is <code>false</code> no decomposition is performed, the original request is used.
     * </p>
     * 
     * @return <code>true</code> if request decomposition is enabled, <code>false</code> if disabled
     */
    public boolean  isRequestDecompositionEnabled() {
        return this.prcrDecomposer.isEnabled();
    }
    
    /**
     * <p>
     * Returns the maximum allowable number of data sources in a composite request.
     * </p>
     * <p>
     * The returned value is used in horizontal request decomposition (i.e., by data source) in the
     * <code>{@link #processRequest(DpDataRequest)}</code> method.  No composite request will have 
     * a data source count larger than this value if request decomposition was successful and multi-streaming
     * is enabled.
     * </p>
     * <p>
     * The default value for this parameter is assigned to the <code>{@link DataRequestDecomposer}</code> 
     * instance used in decomposition.  Its value is taken from the Java API Library configuration file and
     * available at <code>{@link DataRequestDecomposer#CNT_MAX_SOURCES}</code>.
     * </p>
     * <p>
     * <h2>USER NOTE:</h2>
     * This parameter affects the operation of the <code>{@link #processRequest(DpDataRequest)}</code>
     * method only.  This method performs any request decomposition on the offered time-series data
     * request then offers the resulting list of composite requests to the <code>{@link #processRequests(List)}</code>
     * method.  
     * The method <code>{@link #processRequests(List)}</code> always performs request recovery on all 
     * offered <code>DpDataRequest</code> instances as they are (i.e., using multiple gRPC data streams).
     * </p>
     * 
     * @return  the maximum allowable number of data sources in any composite request when multi-streaming
     */
    public final int getRequestDecompMaxPvCount() {
        return this.prcrDecomposer.getMaxDataSources();
    }
    
    /**
     * <p>
     * Returns the maximum allowing time range duration in a composite request.
     * </p>
     * <p>
     * The returned value is used in vertical request decomposition (i.e., by time range) in the 
     * <code>{@link #processRequest(DpDataRequest)}</code> method.  No composite request will have time range
     * greater than the given duration if request decomposition is succesful and multi-streaming is enabled.
     * </p>
     * <p>
     * The default value for this parameter is assigned to the <code>{@link DataRequestDecomposer}</code> 
     * instance used in decomposition.  Its value is taken from the Java API Library configuration file and
     * available at <code>{@link DataRequestDecomposer#DUR_MAX}</code>.
     * </p>
     * <p>
     * <h2>USER NOTE:</h2>
     * This parameter affects the operation of the <code>{@link #processRequest(DpDataRequest)}</code>
     * method only.  This method performs any request decomposition on the offered time-series data
     * request then offers the resulting list of composite requests to the <code>{@link #processRequests(List)}</code>
     * method.  
     * The method <code>{@link #processRequests(List)}</code> always performs request recovery on all 
     * offered <code>DpDataRequest</code> instances as they are (i.e., using multiple gRPC data streams).
     * </p>
     * 
     * @return  the maximum allowable time range in any composite request when multi-streaming
     */
    public final Duration getRequestDecompMaxTimeRange() {
        return this.prcrDecomposer.getMaxDuration();
    }
    
    /**
     * <p>
     * Returns whether or not parallelism (multi-threading) is enabled for request data correlation.
     * </p>
     * <p>
     * If the returned value is <code>true</code> then the correlation processing of query response data
     * is performed using multi-threading.  This is a performance feature which can be enabled/disabled.
     * In general, allowing concurrent processing of response data will increase performance
     * so long as processing threads do not interfere with other activities (such as gRPC streaming).
     * </p>
     * 
     * @return <code>true</code> if multi-threading of response data correlation is enabled, <code>false</code> otherwise
     */
    public final boolean isCorrelateConcurrencyEnabled() {
        return this.prcrCorrelator.isConcurrencyEnabled();
    }
    
    /**
     * <p>
     * Returns the maximum number of allowable concurrent threads for correlation processing of raw time-series data.
     * </p>
     * <p>
     * The returned value reflect the configuration state of the internal <code>RawDataCorrelator</code>
     * processor instance.
     * </p>
     * <p>
     * Note that this value only has context if concurrent processing is enabled.  
     * See method <code>{@link #isCorrelateConcurrencyEnabled()}</code>.
     * </p>
     *   
     * @return  the maximum number of concurrent processing threads used in correlation
     * 
     * @see #setCorrelateConcurrencyMaxThreads(int)
     */
    public final int    getCorrelateConcurrencyMaxThreads() {
        return this.prcrCorrelator.getConcurrencytMaxThreads();
    }
    
    /**
     * <p>
     * Returns the size of the correlated data set which triggers concurrent processing.
     * </p>
     * <p>
     * The returned value reflect the configuration state of the internal <code>RawDataCorrelator</code>
     * processor instance.
     * </p>
     * <p>
     * Note that this value only has context if concurrent processing is enabled.  
     * See method <code>{@link #isCorrelateConcurrencyEnabled()}</code>.
     * </p>
     * 
     * @return  the size limit of the correlated raw data set required for concurrent processing 
     * 
     * @see #setCorrelateConcurrencyPivotSize(int)
     */
    public final int    getCorrelateConcurrencyPivotSize() {
        return this.prcrCorrelator.getConcurrencyPivotSize();
    }

    /**
     * <p>
     * Specifies whether or not multi-streaming of query responses is enabled.
     * </p>
     * <p>
     * The default value is taken from the Java API Library configuration file
     * (see {@link #BOL_MULTISTREAM_ENABLE}).
     * </p>
     * 
     * @return <code>true</code> if multi-streaming is enabled, <code>false</code> if disabled
     * 
     * @see #BOL_MULTISTREAM_ENABLE
     */
    public final boolean isMultiStreaming() {
        return this.bolMultiStream;
    }

    /**
     * <p>
     * Returns the minimum request domain size which triggers query response multi-streaming.
     * </p>
     * <p>
     * The returned value has context only if query response multi-streaming is enabled, specifically,
     * <code>{@link #isMultiStreaming()}</code> returns <code>true</code>.  Otherwise a single
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
     * (see {@link #SIZE_MULTISTREAM_DOMAIN}).
     * </p>
     * <p>
     * <h2>USER NOTE:</h2>
     * This parameter affects the operation of the <code>{@link #processRequest(DpDataRequest)}</code>
     * method only.  This method performs any request decomposition on the offered time-series data
     * request then offers the resulting list of composite requests to the <code>{@link #processRequests(List)}</code>
     * method.  
     * The method <code>{@link #processRequests(List)}</code> always performs request recovery on all 
     * offered <code>DpDataRequest</code> instances as they are (i.e., using multiple gRPC data streams).
     * </p>
     * 
     * @return the minimum query domain size triggering multi-streaming of response data (in data source * seconds)
     */
    public final long getMultiStreamingDomainSize() {
        return this.szDomainMultiStream;
    }

    /**
     * <p>
     * Returns the maximum number of gRPC data streams used for multi-streaming of query responses.
     * </p>
     * <p>
     * The returned value is the number of gRPC data streams always used for a multi-streaming response.
     * Note that the returned value has context only when multi-streaming is enabled, specifically,
     * <code>{@link #isMultiStreaming()}</code> returns <code>true</code>.
     * </p>
     * <p>
     * <h2>USER NOTE:</h2>
     * This parameter affects the operation of the <code>{@link #processRequest(DpDataRequest)}</code>
     * method only.  This method performs any request decomposition on the offered time-series data
     * request then offers the resulting list of composite requests to the <code>{@link #processRequests(List)}</code>
     * method.  
     * The method <code>{@link #processRequests(List)}</code> always performs request recovery on all 
     * offered <code>DpDataRequest</code> instances as they are (i.e., using multiple gRPC data streams).
     * </p>
     * 
     * @return the maximum number of concurrent gRPC data streams used to recover query responses when enabled
     */
    public final int getMultiStreamingMaxStreamCount() {
        return this.cntMaxStreams;
    }
    
    /**
     * <p>
     * Returns whether or not correlation of response data is allowed while it is streamed back from the
     * Query Service.
     * </p>
     * <p>
     * If the returned value is <code>true</code> then the correlation of query response data is allowed
     * to proceed while simultaneously being streamed back from the Query Service.  This is a performance
     * feature providing concurrency of the correlation/streaming process.  Specifically, correlation begin before 
     * all query data is available.  However, the over use of concurrency can cause performance
     * degradation and use of this feature should be used with caution.
     * </p>
     * 
     * @return <code>true</code> if query response data can be correlated as it is being streamed back, 
     *         <code>false</code> otherwise
     */
    public final boolean isCorrelatingWhileStreaming() {
        return this.bolCorrelateMidstream;
    }

//    /**
//     * <p>
//     * Returns the maximum number of allowable execution threads for concurrent query data correlation when enabled.
//     * </p>
//     * <p>
//     * The returned value is maximum number of execution threads used by the internal <code>RawDataCorrelator</code>
//     * instance when processing the recovered raw data.
//     * </p>
//     * 
//     * @return  maximum number of concurrent processing threads used for raw query data correlation
//     */
//    public final int getCorrelateMaxThreads() {
//        return this.prcrCorrelator.getConcurrencytMaxThreads();
//    }
    
    /**
     * <p>
     * Prints out a text description of the current processor configuration to the given output stream.
     * </p>
     * <p>
     * A line-by-line text description is given of the current processor configuration, along with the sub
     * processor configurations.  Each line contains a header identifying the configuration field and the
     * field value.  Optional left-hand-side padding is available to indent all output if the configuration
     * information is part of a larger set.
     * </p>
     *  
     * @param ps        output stream to receive configuration text description
     * @param strPad    optional left-hand-side padding for output lines, or <code>null</code> if none desired
     */
    public void printOutConfig(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        
        ps.println(strPad + "Data Request Decomposition");
        ps.println(strPad + "  request decomposition enabled    : " + this.prcrDecomposer.isEnabled());
        ps.println(strPad + "  maximum PV count per composite   : " + this.prcrDecomposer.getMaxDataSources());
        ps.println(strPad + "  maximum time range per composite : " + this.prcrDecomposer.getMaxDuration());
        ps.println(strPad + "Request Recovery (gRPC Streaming)");
        ps.println(strPad + "  gRPC multi-streaming enabled     : " + this.bolMultiStream);
        ps.println(strPad + "  multi-stream domain size trigger : " + this.szDomainMultiStream);
        ps.println(strPad + "  maximum gRPC stream count        : " + this.cntMaxStreams);
        ps.println(strPad + "  correlate while streaming enable : " + this.bolCorrelateMidstream);
        ps.println(strPad + "Raw Data Correlation");
        ps.println(strPad + "  correlation concurrency enabled  : " + this.prcrCorrelator.isConcurrencyEnabled());
        ps.println(strPad + "  concurrency trigger pivot size   : " + this.prcrCorrelator.getConcurrencyPivotSize());
        ps.println(strPad + "  maximum correlator thread count  : " + this.prcrCorrelator.getConcurrencytMaxThreads());
    }
    
    //
    // Process State Inquiry
    //
    
    /**
     * <p>
     * Returns the (optional) time-series data request identifier associated with the last processed request.
     * </p>
     * <p>
     * The time-series data request identifier is an additional property used to identify requested
     * data.  It is used solely by the Java API Library; that is, it is not a property of the
     * Data Platform Query Service.  
     * </p>
     * <p>
     * The returned request identifier is taken from the last request offered to 
     * <code>{@link #processRequest(DpDataRequest)}</code>.  Thus, this is a convenience method for passing the
     * value down the processing line.
     * </p>
     * <p>
     * Typically, the request ID is assigned in the original request 
     * (see <code>{@link DpDataRequest#setRequestId(String)}</code>). 
     * If assigned it appears here within the final tabular result set once the request is
     * fully recovered and processed by the API library.  Thus, no special characteristics are 
     * required of the ID and a string name is typically sufficient.
     * </p>
     * 
     * @return  the (optional) request identifier or <code>null</code> if unassigned
     */
    public String   getRequestId() {
        return this.strRqstId;
    }
    
    /**
     * <p>
     * Returns the list of composite queries generated for the last <code>{@link #processRequest(DpDataRequest)}</code> request.
     * </p>
     * <p>
     * The returned list is the composite requests generated by the <code>{@link #attemptRequestDecomp(DpDataRequest)}</code>
     * internal method when using the <code>{@link #processRequest(DpDataRequest)}</code> method.  It is the thus the
     * list of composite queries used in the last invocation of <code>{@link #processRequest(DpDataRequest)}</code>.
     * If the method has not be invoked the returned value will be <code>null</code>.
     * </p>
     * 
     * @return  the list of composite queries used to process data by {@link #processRequest(DpDataRequest)}, or <code>null</code> 
     */
    public List<DpDataRequest>  getProcessedCompositeRequest() {
        return this.lstCompRqsts;
    }
    
    /**
     * <p>
     * Returned the number of bytes the internal correlator has processed for the last request.
     * </p>
     * <p>
     * The returned value is the serialized size of all Protocol Buffers messages recovered from the
     * Query Service.  It is also the number of bytes processed by the correlator for the last request
     * if if was successful. 
     * The value provides an estimate of size of any sampling process or results set from the last data 
     * request processed.
     * </p>
     * <p>
     * This value has context after a <code>{@link #processRequest(DpDataRequest)}</code> or 
     * <code>{@link #processRequests(List)}</code> operation upon return.  It is reset after every
     * request invocation.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Actual processed data sizes (in bytes) are typically larger than serialized size (> ~110%).</li>
     * <li>This value is reset to 0 after invoking <code>{@link #reset()}</code>.</li>
     * </ul>
     * </p>
     * 
     * @return      the number of serialized bytes processed by the correlator 
     */
    public final long   getProcessedByteCount() {
        return this.prcrCorrelator.getBytesProcessed();
    }
    
    /**
     * <p>
     * Returns the number of response messages recovered and processed for the last request.
     * </p>
     * <p>
     * The returned value has context after invoking the <code>{@link #processRequest(DpDataRequest)}</code>
     * or <code>{@link #processRequests(List)}</code> operations.  It is the number of time-series
     * data messages returned by the Query Service then successfully processed by the internal
     * correlator.  It is reset after every request invocation.
     * </p>
     * 
     * @return  the number of response messages recovered from the Query Service and processed
     */
    public final int    getProcessedMessageCount() {
        return this.cntMsgsProcessed;
    }


    //
    // Operations
    //
    
    /**
     * <p>
     * Use gRPC data streaming to recover the given request.
     * </p>
     * <p>
     * Performs the given request using gRPC data streaming and blocks until request and 
     * processing are complete.  The given data request is potentially decomposed to create multiple
     * gRPC data streams unless multi-streaming is explicitly turned off using
     * <code>{@link #enableMultiStreaming(boolean)}</code> before calling this method.
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
     * <li><code>{@link #setCorrelatingWhileStreaming(boolean)}</code></li>
     * <li><code>{@link #setCorrelatingConcurrently(boolean)}</code></li>
     * <li><code>{@link #setMultiStreaming(boolean)}</code></li>
     * </ul>
     * These are all performance options that should be tuned for specific platforms.
     * The default values for these options are set in the Data Platform API configuration
     * (see {@link DpApiConfig}</code>).  
     * </p>
     * <p>
     * <h2>Multi-Streaming</h2>
     * The method attempts to decompose large data requests into decompose request according to 
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
     * <code>{@link #processRequest(List)}</code>.  If the default multi-streaming
     * option is disabled a list containing the single request <code>dpRequest</code> is
     * passed, otherwise a query decomposition is attempted.
     * </li>
     * <br/>
     * <li>
     * The default query decomposition attempt is performed by the support method
     * <code>{@link #attemptRequestDecomp(DpDataRequest)}</code>.  If this method
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
     * @return  a sorted set (ordered by start time) of correlated data obtained from given request 
     * 
     * @throws DpQueryException general exception during data recovery and/or processing (see cause)
     * 
     * @see #setCorrelateWhileStreaming(boolean)
     * @see #setCorrelationConcurrency(boolean)
     * @see #setMultiStreamingResponse(boolean)
     */
    public SortedSet<RawCorrelatedData>   processRequest(DpDataRequest dpRequest) throws DpQueryException {

        // Get the request ID
        this.strRqstId = dpRequest.getRequestId();
        
        // If multi-streaming is disabled use a single stream
        if (!this.bolMultiStream || this.cntMaxStreams==1)
            this.lstCompRqsts = List.of(dpRequest);
        
        // Else attempt to decompose request domain
        else {
            this.lstCompRqsts = this.attemptRequestDecomp(dpRequest);
            this.lstCompRqsts.forEach(rqst -> rqst.setRequestId(strRqstId));
        }
        
        // Defer to the multi-streaming processor method
        return this.processRequests(this.lstCompRqsts);
    }
    
    /**
     * <p>
     * Use multiple gRPC data streams explicitly described by argument list (to recover request data).
     * </p>
     * <p>
     * This method allows clients to explicitly determine the concurrent gRPC data streams used by the
     * <code>QueryRequestProcessorOld</code>.  To use the default multi-streaming mechanism with request 
     * decomposition the method <code>{@link #processRequest(DpDataRequest)}</code> should be used; this is
     * the intended operation for this class.  Use this method at your own risk.
     * </p>
     * <p>
     * A separate gRPC data stream is established for each data request within the argument list and concurrent
     * data streams are used to recover the request data.
     * At most <code>{@link #CNT_MULTISTREAM_MAX_STRMS}</code> concurrent data streams are enabled at any instant.
     * If the number of data requests in the argument is larger than <code>{@link #CNT_MULTISTREAM_MAX_STRMS}</code>
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
     * @see #setCorrelateConcurrently(boolean)
     */
    public SortedSet<RawCorrelatedData>   processRequests(List<DpDataRequest> lstRequests) throws DpQueryException {
        
        // Reset the data theCorrelator
        this.prcrCorrelator.reset();

        // Activate the transfer buffer
        boolean bolActive = this.queMsgBuffer.activate();
        
        if (!bolActive) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - FAILURE cannot enabled reponse message buffer.";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpQueryException(strMsg);
        }
        
        // Create the message transfer thread task
        this.thdMsgXferTask = MessageTransferTask.from(this.queMsgBuffer, this.prcrCorrelator);

        // Start data processing thread if mid-stream processing true
        if (this.bolCorrelateMidstream)
            this.thdMsgXferTask.start();

        // Recover the Query Service response data using the query channel
        this.cntMsgsProcessed = this.recoverResponses(lstRequests);    // this is a blocking operation
        
        // Start the message transfer task if it has not already been started (i.e., via mid-stream processing)
        if (!this.thdMsgXferTask.isStarted())
            this.thdMsgXferTask.start();
        

        // Wait for all recovered query data to be correlated
        int cntMsgsXferred = this.correlateResponseData();      // this is a blocking operation
        
        // Check that all recovered messages were processed
        if (this.cntMsgsProcessed != cntMsgsXferred) {
            
            if (BOL_LOGGING)
                LOGGER.warn("{} - INCOMPLETE PROCESSING! Only {} message processed of {} messages received.", 
                        JavaRuntime.getQualifiedMethodNameSimple(), cntMsgsXferred, this.cntMsgsProcessed);
        }
        
        // Recover the correlated data and return it
        this.thdMsgXferTask = null;
        
        SortedSet<RawCorrelatedData>  setPrcdData = this.prcrCorrelator.getCorrelatedSet();
        
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
     * <code>{@link #getMultiStreamCount()}</code> elements.  
     * This method is part of the DEFAULT multi-streaming mechanism within <code>QueryResponseProcessorNew</code>.
     * </p>
     * <p>
     * <h2>Request Size</h2>
     * The <code>DpDataRequest</code> class provides a request size estimate given by
     * <code>{@link DpDataRequest#approxDomainSize()}</code>.  This method returned the 
     * estimated domain size within the given query request (in data sources * seconds).  If this size
     * estimate is less than <code>{@link #getMultiStreamingDomainSize()}</code> nothing is done and a list 
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
     * <h3>Grid Decomposition</h3>
     * If the number of data sources within the request &le; <code>{@link #getMultiStreamCount()}</code> / 2 then a
     * "grid" query domain decomposition is used to create the returned decompose request.  Specifically, the
     * returned value is given by 
     * <code>{@link DpDataRequest#buildCompositeRequest(RequestDecompType, int)}</code> where the arguments are
     * <code>{@link RequestDecompType#GRID}</code> and <code>{@link #getMultiStreamCount()}</code>, respectively.
     * The returned composite list must be checked for size as grid decomposition may add an additional query to
     * match the grid size if necessary - this condition would be a failure.
     * </li>
     * <li>
     * <h3>Vertical Decomposition</h3>
     * As a last result, a "vertical" query domain decomposition is used to create the returned decompose request.  
     * Specifically, the returned value is given by 
     * <code>{@link DpDataRequest#buildCompositeRequest(RequestDecompType, int)}</code> where the arguments are
     * <code>{@link RequestDecompType#VERTICAL}</code> and <code>{@link #getMultiStreamCount()}</code>, respectively.
     * </li>
     * </ol>
     * <s>
     * If all the above conditions fail, the data request is consider "undecomposable".  
     * The returned value is then a one-element list containing the original data request.
     * </s>  
     * </p>
     * 
     * @param dpRequest data request to be decomposed for multi-streaming 
     * 
     * @return  the decompose data request obtained from the above decomposition strategy
     * 
     */
    private List<DpDataRequest> attemptRequestDecomp(DpDataRequest dpRequest) {
        
        // Check if request size approximation is large enough to pivot to multi-streaming
        long    szDomain = dpRequest.approxDomainSize();
        
        if (szDomain < this.szDomainMultiStream)
            return List.of(dpRequest);
        
        
        // If we are here - we are doing multiple gRPC streams requiring a decompose query
        List<DpDataRequest>     lstCmpRqsts;    // decompose request to be returned
        
        // See if the default configuration query domain decomposition will work
        RequestDecompParams recDomain = this.prcrDecomposer.decomposeDomainPreferred(dpRequest);
        
        if (recDomain.totalCovers() <= this.cntMaxStreams) {
            lstCmpRqsts = this.prcrDecomposer.buildCompositeRequest(dpRequest, recDomain);
            
            return lstCmpRqsts;
        }
        
        // Try horizontal query domain decomposition (by data sources)
        //  - Works when request source count is greater than the stream count
        //  - This should get most requests that fail the above
        if (dpRequest.getSourceCount() >= this.cntMaxStreams) {
            lstCmpRqsts = this.prcrDecomposer.buildCompositeRequest(dpRequest, RequestDecompType.HORIZONTAL, this.cntMaxStreams);
        
            return lstCmpRqsts;
        }
        
        // Try a grid-based query domain decomposition
        //  - Applies when the source count is at least half of the stream count
        //  - Must check that composite request count is less that number of streams - will add 1 extra query if necessary
        if (dpRequest.getSourceCount() > (this.cntMaxStreams/2)) {
            lstCmpRqsts = this.prcrDecomposer.buildCompositeRequest(dpRequest, RequestDecompType.GRID, this.cntMaxStreams);
            
            if (lstCmpRqsts.size() <= this.cntMaxStreams)
                return lstCmpRqsts;
        }
        
        // Last choice - Use a vertical query domain decomposition (by time domain)
        lstCmpRqsts = this.prcrDecomposer.buildCompositeRequest(dpRequest, RequestDecompType.VERTICAL, this.cntMaxStreams);
        return lstCmpRqsts;
    }
    
    /**
     * <p>
     * Initiate the process of Query Data recovering for the given request list.
     * </p>
     * <p>
     * A separate gRPC channel is used to recover each data request within the argument.  All recovered
     * data messages are collected by the <code>{@link #queMsgBuffer}</code> instance initialized at
     * construction.  This method blocks until all request data is recovered by the gRPC data streams
     * (using the <code>{@link #chanQuery}</code> instance) and available in the message queue buffer.
     * However, data correlation can still be enabled after returning (depending upon the state of the message
     * transfer task).
     * </p>
     * <p>
     * The following conditions must hold before invoking:
     * <ul>
     * <li>The correlator should be reset and ready for new query data.</li>
     * <li>The message queue buffer must be activated.</li>
     * <li>The message transfer task should be started if correlation is to occur during streaming.</li>
     * </ul>
     * </p>
     * 
     * @param lstRequests   list of data requests, each to be recovered on separate gRPC data streams
     * 
     * @return  the number of <code>QueryData</code> messages recovered from the Query Service 
     * 
     * @throws DpQueryException general exception during data recovery and/or processing (see cause)
     */
    private int recoverResponses(List<DpDataRequest> lstRequests) throws DpQueryException {
        
        int     cntMsgsRcvd;    // the number of response messages recovered
        
        // Start all streaming tasks and wait for all data messages to be recovered
        try {
            cntMsgsRcvd = this.chanQuery.recoverRequests(lstRequests);   // this is a blocking operation
            
        } catch (DpQueryException e) {
            
            this.thdMsgXferTask.terminate();
            this.queMsgBuffer.shutdownNow();
            
            if (BOL_LOGGING)
                LOGGER.error("{} - DpQueryException error while streaming request results: {}", JavaRuntime.getMethodName(), e.getMessage());
            
            throw e;
        }
        
        return cntMsgsRcvd;
    }
    
    /**
     * <p>
     * Blocks until all query request data has been correlated.
     * </p>
     * <p>
     * This method should be invoked after all query data has been recovered from the Query Service and is
     * available within the message queue buffer <code>{@link #queMsgBuffer}</code>.  The method performs
     * the following series of blocking operations:
     * <ol>
     * <li>Shut down the queue buffer.  The buffer continues to supply messages until exhausted, then unblocks.</li>
     * <li>Wait for completion of the message transfer task.  Once completed all messages have been passed to the correlator.</li>
     * </ol>
     * As a final check the method inspects the message transfer task for a SUCCESS status value.  If any of these
     * operations fail an exception is thrown.
     * </p>
     * 
     * @return  the number of <code>QueryData</code> messages transferred to the correlator
     * 
     * @throws DpQueryException general exception during data recovery and/or processing (see cause)
     */
    private int correlateResponseData() throws DpQueryException {
        
        // Perform a shutdown of the message buffer 
        //  - the isSupplying() method will return true until all message are consumed
        try {
            boolean bolShutdown = this.queMsgBuffer.shutdown(); // this is a blocking operation

            if (!bolShutdown) {
                String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Message recovery buffer failed to shut down.";
                
                if (BOL_LOGGING)
                    LOGGER.error(strMsg);
                
                throw new DpQueryException(strMsg);
            }
            
        } catch (InterruptedException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                        + " - Message recovery buffer interrupted while shutting down: "
                        + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpQueryException(strMsg, e);
        } 
            
        // Wait for the message transfer task to finish
        //  - All response data should be processed at that instant
        try {
            this.thdMsgXferTask.join( QueryRequestRecoverer.timeoutLimitDefaultMillis() );
            
        } catch (InterruptedException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - InterruptedException while processing request data: "
                    + e.getMessage();

            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpQueryException(e);
        }
    
        // Check for successful processing then return processed data
        ResultStatus    recProcessed = this.thdMsgXferTask.getResult();
        
        if (recProcessed == null || recProcessed.isFailure()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                        + " - Data correlation processing FAILED: " 
                        + recProcessed.message();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);

            throw new DpQueryException(strMsg);
        }
        
        int cntMsgsXferred = this.thdMsgXferTask.getMessagesTransferred();
        
        return cntMsgsXferred;
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
    
    
}
