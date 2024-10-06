/*
 * Project: dp-api-common
 * File:	DpQueryService.java
 * Package: com.ospreydcs.dp.api.query
 * Type: 	DpQueryService
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
package com.ospreydcs.dp.api.query;

import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.MissingResourceException;

import javax.naming.CannotProceedException;

import org.w3c.dom.ranges.RangeException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiBase;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.model.IDataTable;
import com.ospreydcs.dp.api.model.PvMetaRecord;
import com.ospreydcs.dp.api.query.model.DpQueryException;
import com.ospreydcs.dp.api.query.model.DpQueryStreamBuffer;
import com.ospreydcs.dp.api.query.model.DpQueryStreamType;
import com.ospreydcs.dp.api.query.model.IDpQueryStreamObserver;
import com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData;
import com.ospreydcs.dp.api.query.model.grpc.QueryDataCorrelator;
import com.ospreydcs.dp.api.query.model.grpc.QueryResponseCorrelator;
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
 *   <li>Metadata is returned as a <code>{@link PvMetaRecord}</code> record set, where each 
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
 * in future releases.  Thus, the <code>DpQueryService</code> class does not currently offer
 * annotation queries.
 * </li>
 * </ol> 
 * </p>
 * <p>
 * <h1>USAGE</h1>
 * <h2>Instance Creation</h2>
 * Instance of <code>DpQueryService</code> should be obtained from the Query Service connection
 * factory class <code>{@link DpQueryServiceFactory}</code>.  This is a utility class providing
 * various connection options to the Query Service, including a default connection defined in
 * the API configuration parameters.
 * <h2>Query Operations</h2>
 * All Query Service query methods are prefixed with <code>query</code>.  There are currently
 * 3 types of queries offered by class <code>DpQueryService</code>:
 * <ul>
 * <br/>
 * <li>
 *   <b>Process Variable Metadata</b> - methods prefixed with <code>queryPvs</code>.
 *   <br/>
 *   The methods take a <code>{@link DpMetadataRequest}</code> instance defining the request and
 *   return a list of <code>{@link PvMetaRecord}</code> instances matching the request.
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
 * <code>{@link QueryResponseCorrelator}</code> instance maintained by a 
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
 * <code>{@link QueryResponseCorrelator}</code> for more information on performance tuning.
 * </li>
 * <li>
 * It is informative to note that the <code>DpQueryService</code> class shares its single gRPC 
 * channel connection with its data processor instance.
 * </ul>
 * </p>
 * <p>
 * <h2>gRPC Connection</h2>
 * All communication to the Query Service is handled through a single gRPC channel instance.
 * These channel objects supported concurrency and multiple data streams between a client
 * and the targeted service.  However, excessive (thread) concurrency for a single 
 * <code>DpQueryService</code> instance may over-burden the single channel.
 * </p>
 * <p>
 * <h2>Best Practices</h2>
 * <ul>  
 * <li>Due to the conditions addressed above, clients utilizing extensive concurrency should 
 *     create multiple instances of <code>DpQueryService</code> (each containing a single gRPC
 *     channel and a data processor).</li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 5, 2024
 *
 */
public final class DpQueryService extends DpServiceApiBase<DpQueryService, DpQueryConnection, DpQueryServiceGrpc, DpQueryServiceBlockingStub, DpQueryServiceFutureStub, DpQueryServiceStub> {

    
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
    private final QueryResponseCorrelator   dataProcessor;
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>DpQueryService</code> attached to the given connection.
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
     * @return new <code>DpQueryService</code> interfaces attached to the argument
     */
    public static DpQueryService from(DpQueryConnection connQuery) {
        return new DpQueryService(connQuery);
    }
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpQueryService</code>.
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
    public DpQueryService(DpQueryConnection connQuery) {
        super(connQuery);
        
        this.dataProcessor =  QueryResponseCorrelator.from(connQuery);
    }


    //
    // IConnection Interface
    //
    
    /**
     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination()
     */
    @Override
    public boolean awaitTermination() throws InterruptedException {
        return this.getConnection().awaitTermination();
    }

    
    //
    // Metadata Query Operations
    //
    
    /**
     * <p>
     * Performs a Process Variable metadata request to the Query Service.
     * </p>
     * <p>
     * Available metadata includes information for data sources, or Process Variables (PVs) contributing to
     * the current data archive.  Metadata is returned in the form of record lists containing information on
     * each process variable matching the provided request.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * PV metadata requests are performed using synchronous, unary gRPC connections.  Due to this condition
     * metadata results sets are size limited by the current maximum gRPC message size.
     * </li>
     * </ul>
     * </p> 
     * 
     * @param rqst  the metadata request to be performed
     * 
     * @return      the results set of the given metadata request
     * 
     * @throws DpQueryException     the Query Service reported an error (see message)
     */
    public List<PvMetaRecord> queryPvs(DpMetadataRequest rqst) throws DpQueryException {
        
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
        List<PvMetaRecord>      lstRecs = msgRsp
                .getMetadataResult()
                .getPvInfosList()
                .stream()
                .<PvMetaRecord>map(ProtoMsg::toPvMetaRecord)
                .toList();
        
        
        return lstRecs;
        
        } catch (IllegalArgumentException | TypeNotPresentException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                           + " - Could not convert response to PvMetaRecord: "
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
     * <p>
     * Performs a unary data request to the Query Service.
     * </p>
     * <p>
     * The method performs a blocking unary request to the Query Service and does not return until
     * the result set is recovered and used to fully populate the returned data table.
     * The gRPC resource overhead is smallest for this operation and is best used for
     * situations where many small requests are invoked. 
     * </p>
     * <p>
     * <h2>WARNING</h2>
     * This operation should be used only when small results sets are expected.
     * The unary request is such that the entire result set must be contained in a single gRPC 
     * message, which is size limited.  Note that the gRPC default size is 4 MBytes, but the 
     * value is configurable.
     * If the result set of the request is larger than the current gRPC message size limit,
     * an exception is thrown (or the request is truncated if the Query Service does not 
     * recognize the error).
     * </p>
     * <p>
     * <ul>
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The <code>{@link DpDataRequest#getStreamType()}</code> property is unused.</li>
     * <li>If the request is too large there are 2 possible outcomes:
     *   <ol>
     *   <li>An exception is thrown.</li>
     *   <li>The result is truncated. </li>
     *   </ol>
     *   TODO: We need to verify which of the above is consistent</li>
     * </ul>
     * </p>
     * 
     * @param rqst  an initialized <code>{@link DpDataRequest]</code> request builder instance
     * 
     * @return      fully populated (static) data table.
     * 
     * @throws  DpQueryException    general exception during query or data reconstruction (see cause)
     */
//    @AUnavailable(status=STATUS.ACCEPTED, note="The operation is beta tested and available.")
    synchronized
    public IDataTable queryDataSingle(DpDataRequest rqst) throws DpQueryException {
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
     * <p>
     * Performs the given data request and returns the results as a table.
     * </p>
     * <p>
     * This is the primary method for Query Service time-series data requests.  This method 
     * supports query result sets of any size, since the underlying data transport mechanism is
     * through gRPC data streams.  All results set data is transported back to the client,
     * processed into appropriate times-series, then assembled into the returned data table. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The data streaming and reconstruction can be performed simultaneously, with various
     * levels of concurrency, with an internal instance of 
     * <code>{@link QueryResponseCorrelator}</code>.  See the class documentation for details
     * on tuning the instance with the various performance parameters.
     * </li>
     * <br/>
     * <li>
     * The Query Service configuration section within the API configuration parameters 
     * (i.e., those of <code>{@link DpApiConfig}</code> and associated resource file),
     * provide access to the tuning parameters for this <code>QueryResponseCorrelator</code>
     * instance and, thus, this method.
     * </li>
     * </ul>
     * </p>
     * 
     * @param rqst  the data request, a configured <code>{@link DpDataRequest]</code> request instance
     * 
     * @return  a data table containing the results set of the given data request
     * 
     * @throws DpQueryException general exception during query or data reconstruction (see cause)
     */
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
     * <p>
     * Performs the given list of data requests concurrently and returns all results within a 
     * single table.
     * </p>
     * <p>
     * This data request option is available for clients wishing to perform simultaneous data
     * requests where results are all returned in the same table. 
     * Note that a separate gRPC data stream is established for each request within the argument.
     * Thus, <b>DO NOT</b> use large argument lists (although large requests are well supported).  
     * Doing so stresses gRPC resources and can potentially create excessive network traffic.  
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * All data requests within the argument are allocated a separate gRPC data stream and are
     * recovered concurrently.  DO NOT use an excessive number of requests in the argument.
     * See configuration parameter <code>query.data.response.multistream.maxStreams</code> within
     * configuration file <em>dp-api-config.yml</em> for guidance.
     * </li>
     * <br/>
     * <li>
     * All NOTES of <code>{@link #queryData(DpDataRequest)}</code> applies to this method except
     * any configuration options limiting the number of gRPC data streams.
     * </li>
     * </ul>
     * </p>
     * 
     * @param lstRqsts unordered list of data requests to perform concurrently on separate streams
     * 
     * @return  a data table containing the results sets of all the given data requests
     * 
     * @throws DpQueryException general exception during query or data reconstruction (see cause)
     */
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
     * <p>
     * Perform a Query Service data query that returns a dynamic stream buffer accumulating the result set.
     * </p>
     * <p>
     * This operation creates a gRPC data stream from the Query Service to the returned stream
     * buffer instance.  The type of data stream is determined by the value of 
     * <code>{@link DpDataRequest#getStreamType()}</code> returned by the argument.
     * Results of the query are accessible via the stream buffer as they are available; that is,
     * the returned stream buffer is dynamically loaded with the results set after initiating 
     * the stream.
     * </p>
     * <p>
     * The data stream is initiated with the <code>{@link DpQueryStreamBuffer#start()}</code> method
     * within the returned object.
     * Query stream observers implementing the <code>{@link IDpQueryStreamObserver}</code> interface
     * can register with the returned object to receive callback notifications for data and stream events
     * using the <code>{@link DpQueryStreamBuffer#addStreamObserver(com.ospreydcs.dp.api.query.model.IDpQueryStreamObserver)}</code>
     * method.
     * For more information on use of the returned stream buffer see documentation on 
     * <code>{@link DpQueryStreamBuffer}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Choose the gRPC data stream type using 
     * <code>{@link DpDataRequest#setStreamType(DpQueryStreamType)}</code>.
     *   <ul>
     *   <li>Unidirectional streams are potentially faster but less stable.</li>
     *   <li>Bidirectional streams are typically more stable but potentially slower.</li>
     *   <li>The default is a unidirectional stream <code>{@link DpQueryStreamType#UNIDIRECTIONAL}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>  
     * 
     * @param rqst  configured <code>{@link DpDataRequest]</code> request builder instance
     * 
     * @return      an active query stream buffer ready to accumulate the results set
     * 
     * @see DpQueryStreamBuffer
     * @see DpQueryStreamBuffer#start()
     * @see DpQueryStreamBuffer#startAndAwaitCompletion()
     */
    public DpQueryStreamBuffer   queryDataStream(DpDataRequest rqst) {
        
        // Extract the Protobuf request message and stream type
        QueryDataRequest    msgRequest = rqst.buildQueryRequest();
        DpQueryStreamType   enmStreamType = rqst.getStreamType();
        
        // Create the query stream buffer and return it
        DpQueryStreamBuffer buf = DpQueryStreamBuffer.newBuffer(
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
        DpQueryStreamBuffer buf = DpQueryStreamBuffer.newBuffer(
                DpQueryStreamType.UNIDIRECTIONAL, 
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
        DpQueryStreamBuffer buf = DpQueryStreamBuffer.newBuffer(
                DpQueryStreamType.BIDIRECTIONAL, 
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
