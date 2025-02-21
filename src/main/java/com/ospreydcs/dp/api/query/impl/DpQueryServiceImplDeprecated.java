/*
 * Project: dp-api-common
 * File:	DpQueryServiceImplDeprecated.java
 * Package: com.ospreydcs.dp.api.query
 * Type: 	DpQueryServiceImplDeprecated
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
 * @since Jan 5, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.impl;

import java.util.List;
import java.util.MissingResourceException;
import java.util.SortedSet;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import javax.naming.CannotProceedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.ranges.RangeException;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.common.IDataTable;
import com.ospreydcs.dp.api.common.MetadataRecord;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiBase;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpMetadataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.DpQueryStreamBuffer;
import com.ospreydcs.dp.api.query.IDpQueryStreamObserver;
import com.ospreydcs.dp.api.query.IQueryService;
import com.ospreydcs.dp.api.query.model.correl.CorrelatedQueryData;
import com.ospreydcs.dp.api.query.model.correl.QueryDataCorrelator;
import com.ospreydcs.dp.api.query.model.correl.QueryResponseCorrelatorDeprecated;
import com.ospreydcs.dp.api.query.model.series.SamplingProcess;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult.ExceptionalResultStatus;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryMetadataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryMetadataResponse;

/**
 * <p>
 * <h1>Data Platform Query Service Application Programming Interface (API).</h1>
 * </p>
 * <p>
 * This class is the primary access point for Query Service clients.  The class exposes all
 * the fundamental operations of the Query Service without details of the underlying gRPC
 * interface.
 * </p>
 * <p>
 * <h1>Query Service Requests</h1>
 * The are currently 3 types of operations available from the Query Service:
 * <br/>
 * <ol>
 * <li>
 * <b>Metadata Queries</b> - Information about the current query domain of the Data Archive.  
 * Currently the Query Service supports only Process Variable (PV) metadata queries.  
 * Information about all the data sources contributing times-series data to the archive is 
 * available.
 *   <ul>
 *   <li>Metadata requests are made using a <code>{@link DpMetadataRequest}</code> object.</li>
 *   <li>Metadata is returned as a <code>{@link MetadataRecord}</code> record set, where each 
 *       record contains information on a single process variable matching the request.</li>
 *   </ul>
 * </li>
 * <li>
 * <b>Data Queries</b> - Correlated, time-series data within the primary Data Archive.
 *   <ul>
 *   <li>Time-series data requests are made using a <code>{@link DpDataRequest}</code> object.</li>
 *   <li>Time-series data results are returned as a data table, typically an instance exposing
 *       the <code>{@link IDataTable}</code> interface (table implementation is hidden).</li>
 *   </ul>
 * </li>
 * <li>
 * <b>Annotation Queries</b> - Annotations of the Data Archive provided by Data Platform users.
 * <br/>
 * The annotations feature of the Data Platform is currently being developed and will be available
 * in future releases.  Thus, the <code>DpQueryServiceImplDeprecated</code> class does not currently offer
 * annotation queries.
 * </li>
 * </ol> 
 * </p>
 * <p>
 * <h1>USAGE</h1>
 * <h2>Instance Creation</h2>
 * Instance of <code>DpQueryServiceImplDeprecated</code> should be obtained from the Query Service connection
 * factory class <code>{@link DpQueryServiceFactoryDeprecated}</code>.  This is a utility class providing
 * various connection options to the Query Service, including a default connection defined in
 * the API configuration parameters.
 * <h2>Query Operations</h2>
 * All Query Service query methods are prefixed with <code>query</code>.  There are currently
 * 3 types of queries offered by class <code>DpQueryServiceImplDeprecated</code>:
 * <ul>
 * <br/>
 * <li>
 *   <b>Process Variable Metadata</b> - methods prefixed with <code>queryPvs</code>.
 *   <br/>
 *   The methods take a <code>{@link DpMetadataRequest}</code> instance defining the request and
 *   return a list of <code>{@link MetadataRecord}</code> instances matching the request.
 *   The methods block until all data is available.
 * </li>
 * <br/>
 * <li>
 *   <b>Time-Series Data</b> - methods prefixed with <code>queryData</code>. 
 *   <br/>
 *   The methods take a <code>{@link DpDataRequest}</code> instance defining the request and
 *   return an <code>{@link IDataTable}</code> implementation containing the results.
 *   The methods block until all data is available.
 * </li>
 * <br/>
 * <li>
 *   <b>Raw Time-Series Data</b> - methods prefixed with <code>queryDataStream</code>.
 *   <br/>
 *   These are advanced operations offered to clients that which to do their own data processing
 *   along with some stream management.  Here requests are again made using a 
 *   <code>{@link DpDataRequest}</code> object but the returned object is a 
 *   <code>{@link DpQueryStreamBuffer}</code> instance, not results are yet available. 
 *   Instead, the data stream is initiated with a invocation of 
 *   <code>{@link DpQueryStreamBuffer#start()}</code> after which results are dynamically 
 *   available, or <code>{@link DpQueryStreamBuffer#startAndAwaitCompletion()}</code> which does
 *   not returned until the data buffer has received all data from the Query Service. 
 * </li>
 * </ul>
 * Note that the above methods DO NOT necessarily conform to the gRPC interface operations  
 * within <code>{@link DpQueryServiceGrpc}</code>. 
 * </p>
 * <p>
 * <h2>Shutdowns</h2>
 * Always shutdown the interface when no long needed.  This action releases all internal resources
 * required of this interface ensuring maximum performance.  Shutdown operations are provided by
 * the methods <code>{@link #shutdown()}</code> and <code>{@link #shutdownNow()}</code>.
 * The methods <code>{@link #awaitTermination()}</code> and 
 * <code>{@link #awaitTermination(long, java.util.concurrent.TimeUnit)}</code> are available
 * to block until shutdown is complete (this step is not required).
 * </p>
 * <p>
 * <h1>GENERAL NOTES</h1>
 * <h2>Data Processing</h2>
 * A single data processor is used for all time-series data requests.  The processor is a single
 * <code>{@link QueryResponseCorrelatorDeprecated}</code> instance maintained by a 
 * <code>DpQuerySerice</code> class object.  The data process performs both the gRPC data 
 * streaming from the Query Service AND the correlation of incoming data.  The data processor
 * offers various configuration options where data can be simultaneously streamed and 
 * correlated, along with other multi-threading capabilities.  The data processor typically
 * uses multiple, concurrent gRPC data streams to recover results.  There are several points
 * to note about this implementation situation:
 * <ul>
 * <li>
 * Since a single data processor is used within this Query Service API, all data request
 * operations are synchronized.  Only one time-series data request is performed at any
 * instance and competing threads must wait until completed.
 * </li>
 * <li>
 * The data processor can be tuned with various configuration parameters within the 
 * <code>dp-api-config.yml</code> configuration file.  See class documentation on 
 * <code>{@link QueryResponseCorrelatorDeprecated}</code> for more information on performance tuning.
 * </li>
 * <li>
 * It is informative to note that the <code>DpQueryServiceImplDeprecated</code> class shares its single gRPC 
 * channel connection with its data processor instance.
 * </ul>
 * </p>
 * <p>
 * <h2>gRPC Connection</h2>
 * All communication to the Query Service is handled through a single gRPC channel instance.
 * These channel objects supported concurrency and multiple data streams between a client
 * and the targeted service.  However, excessive (thread) concurrency for a single 
 * <code>DpQueryServiceImplDeprecated</code> instance may over-burden the single channel.
 * </p>
 * <p>
 * <h2>Best Practices</h2>
 * <ul>  
 * <li>Due to the conditions addressed above, clients utilizing extensive concurrency should 
 *     create multiple instances of <code>DpQueryServiceImplDeprecated</code> (each containing a single gRPC
 *     channel and a data processor).</li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 5, 2024
 *
 * @deprecated  Replaced by DpQueryServiceImpl
 */
@Deprecated(since="Feb 16, 2025", forRemoval=true)
public final class DpQueryServiceImplDeprecated extends DpServiceApiBase<DpQueryServiceImplDeprecated, DpQueryConnection, DpQueryServiceGrpc, DpQueryServiceBlockingStub, DpQueryServiceFutureStub, DpQueryServiceStub> implements IQueryService {

    
    //
    // Application Resources
    //
    
    /** Default Query Service configuration parameters */
    private static final DpQueryConfig  CFG_DEFAULT = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** Logging active flag */
    private static final boolean        BOL_LOGGING = CFG_DEFAULT.logging.active;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger LOGGER = LogManager.getLogger();
    
    
    //
    // Attributes
    //
    
    /** The single query response correlator (for time-series data) used for all data requests */
    private final QueryResponseCorrelatorDeprecated   dataProcessor;
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>DpQueryServiceImplDeprecated</code> attached to the given connection.
     * </p>
     * <p>
     * The argument should be obtained from the appropriate connection factory,
     * specifically, <code>{@link DpQueryConnectionFactory}</code>.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * The returned object should be shut down when no longer needed using 
     * <code>{@link #shutdown()}</code> or <code>{@link #shutdownNow()}</code>.  
     * This action is necessary to release unused gRPC resources and maintain 
     * overall performance.  
     * </p>
     * 
     * @param connQuery  the gRPC channel connection to the desired DP Query Service
     *  
     * @return new <code>DpQueryServiceImplDeprecated</code> interfaces attached to the argument
     */
    public static DpQueryServiceImplDeprecated from(DpQueryConnection connQuery) {
        return new DpQueryServiceImplDeprecated(connQuery);
    }
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpQueryServiceImplDeprecated</code>.
     * </p>
     * <p>
     * The argument should be obtained from the appropriate connection factory,
     * specifically, <code>{@link DpQueryConnectionFactory}</code>.
     * </p>
     * 
     * @param connQuery  the gRPC channel connection to the desired DP Query Service 
     * 
     * @see DpQueryConnectionFactory
     */
    public DpQueryServiceImplDeprecated(DpQueryConnection connQuery) {
        super(connQuery);
        
        this.dataProcessor =  QueryResponseCorrelatorDeprecated.from(connQuery);
    }


    //
    // IConnection Interface
    //
    
    /**
     *
     * @see @see com.ospreydcs.dp.api.query.IQueryService#awaitTermination()
     */
    @Override
    public boolean awaitTermination() throws InterruptedException {
        return this.getConnection().awaitTermination();
    }

    
    //
    // Metadata Query Operations
    //
    
    /**
     *
     * @see @see com.ospreydcs.dp.api.query.IQueryService#queryPvs(com.ospreydcs.dp.api.query.DpMetadataRequest)
     */
    @Override
    public List<MetadataRecord> queryMeta(DpMetadataRequest rqst) throws DpQueryException {
        
        // Get the Protobuf request from the argument
        QueryMetadataRequest    msgRqst = rqst.buildQueryRequest();
        
        // Perform gRPC request
        QueryMetadataResponse   msgRsp = super.grpcConn.getStubBlock().queryMetadata(msgRqst);
        
        // Check for Query Service exception
        if (msgRsp.hasExceptionalResult()) {
            ExceptionalResult       msgExcept = msgRsp.getExceptionalResult();
            ExceptionalResultStatus enmExcept = msgExcept.getExceptionalResultStatus();
            String                  strExcept = msgExcept.getMessage();
            
            String      strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                               + " - Query Service reported an exception: status=" + enmExcept
                               + ", message=" + strExcept;
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpQueryException(strMsg);
        }
        
        // Unpack the response and return it
        try {
        List<MetadataRecord>      lstRecs = msgRsp
                .getMetadataResult()
                .getPvInfosList()
                .stream()
                .<MetadataRecord>map(ProtoMsg::toPvMetaRecord)
                .toList();
        
        
        return lstRecs;
        
        } catch (IllegalArgumentException | TypeNotPresentException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                           + " - Could not convert response to MetadataRecord: "
                           + "exception=" + e.getClass().getSimpleName()
                           + ", message=" + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpQueryException(strMsg, e);
        }
    }
    
    
    //
    // Time-Series Data Query Operations
    //
    
    /**
     *
     * @see @see com.ospreydcs.dp.api.query.IQueryService#queryDataUnary(com.ospreydcs.dp.api.query.DpDataRequest)
     */
    @Override
    synchronized
    public IDataTable queryDataUnary(DpDataRequest rqst) throws DpQueryException {
        QueryDataRequest qry = rqst.buildQueryRequest();
        
        QueryDataResponse msgRsp = super.grpcConn.getStubBlock().queryData(qry);
        
        QueryDataCorrelator correlator = new QueryDataCorrelator();
        
        try {
            correlator.addQueryResponse(msgRsp);
            
            SortedSet<CorrelatedQueryData>  setPrcdData = correlator.getCorrelatedSet();
            
            SamplingProcess process = SamplingProcess.from(setPrcdData);
            IDataTable      table = process.createStaticDataTable();
            
            return table;
            
        } catch (CompletionException | CannotProceedException | IllegalArgumentException | ExecutionException e) {
            if (BOL_LOGGING) 
                LOGGER.error("{} - Exception while correlating response: {}, {}}", JavaRuntime.getMethodName(), e.getClass().getSimpleName(), e.getMessage());

            throw new DpQueryException("Exception while correlating response: " + e.getClass() + ", " + e.getMessage(), e);
        }
    }
    
    /**
     *
     * @see @see com.ospreydcs.dp.api.query.IQueryService#queryData(com.ospreydcs.dp.api.query.DpDataRequest)
     */
    @Override
    synchronized
    public IDataTable   queryData(DpDataRequest rqst) throws DpQueryException {
        
        // Perform request and response correlation
        SortedSet<CorrelatedQueryData> setPrcdData = this.dataProcessor.processRequestStream(rqst);
        
        
        // Recover the sampling process 
        // TODO - contains extensive error checking which may be removed when stable
        try {
            SamplingProcess process = SamplingProcess.from(setPrcdData);
            IDataTable      table = process.createStaticDataTable();
            
            return table;
        
        } catch (MissingResourceException | IllegalArgumentException | IllegalStateException | RangeException | TypeNotPresentException | CompletionException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                           + " - Failed to create SamplingProcess, exception thrown: type="
                           + e.getClass().getSimpleName()
                           + ", message=" + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpQueryException(strMsg, e);
        }
    }
    
    /**
     *
     * @see @see com.ospreydcs.dp.api.query.IQueryService#queryData(java.util.List)
     */
    @Override
    synchronized
    public IDataTable   queryData(List<DpDataRequest> lstRqsts) throws DpQueryException {
        
        // Perform request and response correlation
        SortedSet<CorrelatedQueryData>  setPrcdData = this.dataProcessor.processRequestMultiStream(lstRqsts);

        // Recover the sampling process 
        // TODO - contains extensive error checking which may be removed when stable
        try {
            SamplingProcess process = SamplingProcess.from(setPrcdData);
            IDataTable      table = process.createStaticDataTable();
            
            return table;
        
        } catch (MissingResourceException | IllegalArgumentException | IllegalStateException | RangeException | TypeNotPresentException | CompletionException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                           + " - Failed to create SamplingProcess, exception thrown: type="
                           + e.getClass().getSimpleName()
                           + ", message=" + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpQueryException(strMsg, e);
        }
    }
    
    /**
     *
     * @see @see com.ospreydcs.dp.api.query.IQueryService#queryDataStream(com.ospreydcs.dp.api.query.DpDataRequest)
     */
    @Override
    public DpQueryStreamBuffer   queryDataStream(DpDataRequest rqst) {
        
        // Extract the Protobuf request message and stream type
        QueryDataRequest    msgRequest = rqst.buildQueryRequest();
        DpGrpcStreamType    enmStreamType = rqst.getStreamType();
        
        // Create the query stream buffer and return it
        DpQueryStreamBuffer buf = DpQueryStreamBuffer.create(
                enmStreamType, 
                this.getConnection().getStubAsync(),
                msgRequest,
                CFG_DEFAULT.logging.active);
        
        return buf;
    }
    
    /**
     * <p>
     * Perform a Query Service data query that returns a dynamic stream buffer accumulating the result set.
     * </p>
     * <p>
     * This operation creates a unidirectional (backward) stream from the Query Service to the returned stream
     * buffer instance.  Unidirectional streams are potentially faster but less stable.  
     * Results of the query are accessible via the stream buffer as they are available.
     * </p>
     * <p>
     * The data stream is initiated with the <code>{@link DpQueryStreamBuffer#start()}</code> method.
     * Query stream observers implementing the <code>{@link IDpQueryStreamObserver}</code> interface
     * can register with the returned object to receive callback notifications for data and stream events
     * using the <code>{@link DpQueryStreamBuffer#addStreamObserver(com.ospreydcs.dp.api.query.model.IDpQueryStreamObserver)</code>
     * method.
     * </p>
     * 
     * @param rqst  configured <code>{@link DpDataRequest]</code> request builder instance
     * 
     * @return      an active query stream buffer ready to accumulate the results set
     * 
     * @see DpQueryStreamBuffer
     * @see DpQueryStreamBuffer#start()
     * 
     * @deprecated stream type is now a parameter of <code>DpDataRequest</code>
     */
    @Deprecated(since="March 15, 2024", forRemoval=true)
    public DpQueryStreamBuffer   queryStreamUni(DpDataRequest rqst) {
        
        // Extract the Protobuf request message
        QueryDataRequest    msgRequest = rqst.buildQueryRequest();
        
        // Create the query stream buffer and return it
        DpQueryStreamBuffer buf = DpQueryStreamBuffer.create(
                DpGrpcStreamType.BACKWARD, 
                this.getConnection().getStubAsync(),
                msgRequest,
                CFG_DEFAULT.logging.active);
        
        return buf;
    }
    
    /**
     * <p>
     * Perform a Query Service data query that returns a dynamic stream buffer accumulating the result set.
     * </p>
     * <p>
     * This operation creates a bidirectional stream from the Query Service to the returned stream
     * buffer instance.  Bidirectional streams are typically more stable but potentially slower.  
     * Results of the query are accessible via the stream buffer as they are available.
     * </p>
     * <p>
     * The data stream is initiated with the <code>{@link DpQueryStreamBuffer#start()</code> method.
     * Query stream observers implementing the <code>{@link IDpQueryStreamObserver}</code> interface
     * can register with the returned object to receive callback notifications for data and stream events
     * using the <code>{@link DpQueryStreamBuffer#addStreamObserver(com.ospreydcs.dp.api.query.model.IDpQueryStreamObserver)</code>
     * method.
     * </p>
     * 
     * @param rqst          an initialized <code>{@link DpDataRequest]</code> request builder instance
     * 
     * @return  an active query stream buffer ready to accumulate the result set
     * 
     * @throws DpQueryException Query Service exception - typically results from a malformed request (see message and cause)
     * 
     * @see DpQueryStreamBuffer
     * @see DpQueryStreamBuffer#start()
     * 
     * @deprecated stream type is now a parameter of <code>DpDataRequest</code>
     */
    @Deprecated(since="March 15, 2024", forRemoval=true)
    public DpQueryStreamBuffer queryStreamBidi(DpDataRequest rqst) {
        
        // Extract the Protobuf request message
        QueryDataRequest    msgRequest = rqst.buildQueryRequest();
        
        // Create the query stream buffer and return it
        DpQueryStreamBuffer buf = DpQueryStreamBuffer.create(
                DpGrpcStreamType.BIDIRECTIONAL, 
                this.getConnection().getStubAsync(),
                msgRequest,
                CFG_DEFAULT.logging.active);
        
        return buf;
    }
    
//  /**
//  * <p>
//  * Perform a Query Service data query that returns a dynamic stream buffer accumulating the result set.
//  * </p>
//  * <p>
//  * This operation creates a unidirectional (backward) stream from the Query Service to the returned stream
//  * buffer instance.  Unidirectional streams are typically faster but potentially less stable.   
//  * Results of the query are accessible via the stream buffer as they are available.
//  * </p>
//  * <p>
//  * Explicit timeout limits are given for Query Service responses and operations.
//  * </p>
//  * 
//  * @param rqst          an initialized <code>{@link DpDataRequest]</code> request builder instance
//  * @param cntTimeout    time limit for Query Service timeout limit
//  * @param tuTimeout     time units for Query Service timeout limit
//  * 
//  * @return  an active query stream buffer currently accumulating the result set
//  * 
//  * @throws DpQueryException Query Service exception - typically results from a malformed request (see message and cause)
//  * 
//  * @see DpQueryStreamQueueBufferDeprecated
//  */
// public DpQueryStreamQueueBufferDeprecated  queryUniStream(DpDataRequest rqst, long cntTimeout, TimeUnit tuTimeout) throws DpQueryException {
//     
//     // Create the query stream buffer
//     DpQueryStreamQueueBufferDeprecated bufStr = DpQueryStreamQueueBufferDeprecated.from(super.grpcConn.getStubAsync(), cntTimeout, tuTimeout);
//             
//     // Get the query request message
//     QueryRequest    msgRqst = rqst.buildQueryRequest();
//             
//     // Initiate stream and return it
//     try {
//         bufStr.startUniStream(msgRqst);
//         
//     } catch (IllegalStateException e) {
//         String strMsg = JavaRuntime.getQualifiedCallerName() + ": IllegalStateException thrown (this should not occur) - " + e.getMessage();
//         
//         LOGGER.error(strMsg);
//         
//         throw new DpQueryException(strMsg, e);
//         
//     } catch (IllegalArgumentException e) {
//         String strMsg = JavaRuntime.getQualifiedCallerName() + ": IllegalArgumentException (malformed QueryRequest) - " + e.getMessage();
//         
//         LOGGER.error(strMsg);
//         
//         throw new DpQueryException(strMsg, e);
//     }
//     
//     return bufStr;
// }
 
//    /**
//     * <p>
//     * Perform a Query Service data query that returns a dynamic stream buffer accumulating the result set.
//     * </p>
//     * <p>
//     * This operation creates a bidirectional stream from the Query Service to the returned stream
//     * buffer instance.  Bidirectional streams are more stable but potentially slower.  
//     * Results of the query are accessible via the stream buffer as they are available.
//     * </p>
//     * <p>
//     * Explicit timeout limits are given for Query Service responses and operations.
//     * </p>
//     * 
//     * @param rqst          an initialized <code>{@link DpDataRequest]</code> request builder instance
//     * @param cntTimeout    time limit for Query Service timeout limit
//     * @param tuTimeout     time units for Query Service timeout limit
//     * 
//     * @return  an active query stream buffer currently accumulating the result set
//     * 
//     * @throws DpQueryException Query Service exception - typically results from a malformed request (see message and cause)
//     * 
//     * @see DpQueryStreamQueueBufferDeprecated
//     */
//    public DpQueryStreamQueueBufferDeprecated  queryBidiStream(DpDataRequest rqst, long cntTimeout, TimeUnit tuTimeout) throws DpQueryException {
//        
//        // Create the query stream buffer
//        DpQueryStreamQueueBufferDeprecated bufStr = DpQueryStreamQueueBufferDeprecated.from(super.grpcConn.getStubAsync(), cntTimeout, tuTimeout);
//                
//        // Get the query request message
//        QueryRequest    msgRqst = rqst.buildQueryRequest();
//                
//        // Initiate stream and return it
//        try {
//            bufStr.startBidiStream(msgRqst);
//            
//        } catch (IllegalStateException e) {
//            String strMsg = JavaRuntime.getQualifiedCallerName() + ": IllegalStateException thrown (this should not occur) - " + e.getMessage();
//            
//            LOGGER.error(strMsg);
//            
//            throw new DpQueryException(strMsg, e);
//            
//        } catch (IllegalArgumentException e) {
//            String strMsg = JavaRuntime.getQualifiedCallerName() + ": IllegalArgumentException (malformed QueryRequest) - " + e.getMessage();
//            
//            LOGGER.error(strMsg);
//            
//            throw new DpQueryException(strMsg, e);
//        }
//        
//        return bufStr;
//    }
    
    
    //
    // Support Methods
    //
    
    /**
     * Returns the <code>DpGrpcConnection</code> in super class cast to a
     * <code>DpQueryConnection</code> instance.
     * 
     * @return connection instances as a <code>DpQueryConnection</code> object
     */
    private DpQueryConnection getConnection() {
        return super.grpcConn;
    }
    
}
