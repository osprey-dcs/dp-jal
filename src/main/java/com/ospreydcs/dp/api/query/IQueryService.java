package com.ospreydcs.dp.api.query;

import java.util.List;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.common.IDataTable;
import com.ospreydcs.dp.api.common.MetadataRecord;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.grpc.model.IConnection;
import com.ospreydcs.dp.api.query.impl.QueryRequestProcessor;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;

/**
 * <p>
 * <h2>Data Platform Query Service Application Programming Interface (API).</h2>
 * </p>
 * <p>
 * This interface is the primary access point for Query Service clients.  It exposes all
 * the fundamental operations of the Query Service without details of the underlying gRPC
 * interface.
 * </p>
 * <p>
 * <h2>Data Platform Request Types</h2>
 * The are currently 3 types of request operations available from the Data Platform:
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
 * <b>Time-Series Data Queries</b> - Correlated, time-series data within the Data Platform data archive.
 *   <ul>
 *   <li>Time-series data requests are made using a <code>{@link DpDataRequest}</code> object.</li>
 *   <li>Time-series data results are returned as a data table, typically an instance exposing
 *       the <code>{@link IDataTable}</code> interface (table implementation is hidden).</li>
 *   </ul>
 * </li>
 * <li>
 * <b>Annotation Queries</b> - Annotations of the data archive provided by Data Platform users.
 * <br/>
 * Annotations and annotation queries are available from the Annotation Service.
 * Thus, the <code>IQueryService</code> interface does not offer annotation queries.
 * </li>
 * </ol> 
 * </p>
 * <p>
 * <h2>USAGE</h2>
 * <h3>Instance Creation</h3>
 * Instance of <code>IQueryService</code> should be obtained from the Query Service connection
 * factory class <code>{@link DpQueryApiFactory}</code>.  This is a utility class providing
 * various connection options to the Query Service, including a default connection defined in
 * the API configuration parameters.
 * <h2>Query Operations</h2>
 * All Query Service query methods are prefixed with <code>query</code>.  There are currently
 * 3 types of queries offered by interface <code>IQueryService</code>:
 * <ul>
 * <br/>
 * <li>
 *   <b>Process Variable Metadata</b> - methods prefixed with <code>queryMeta</code>.
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
 *   return an <code>{@link IDataTable}</code> implementation containing the time-series results.
 *   The methods block until all data is available.
 * </li>
 * <br/>
 * <li>
 *   <b>Raw Time-Series Data</b> - methods prefixed with <code>queryDataStream</code>.
 *   <br/>
 *   These are advanced operations offered to clients that which to do their own data processing
 *   along with some stream management.  Here requests are again made using a 
 *   <code>{@link DpDataRequest}</code> object but the returned object is a 
 *   <code>{@link DpQueryStreamBuffer}</code> instance, results are not yet available. 
 *   Instead, the data stream is initiated with a invocation of 
 *   <code>{@link DpQueryStreamBuffer#start()}</code> after which results are dynamically 
 *   available, or by invocation of <code>{@link DpQueryStreamBuffer#startAndAwaitCompletion()}</code> 
 *   which blocks until the data buffer has received all data from the Query Service.
 *   See <code>{@link DpQueryStreamBuffer}</code> documentation for details on using a raw,
 *   time-series data stream. 
 * </li>
 * </ul>
 * Note that the above methods DO NOT necessarily conform to the gRPC interface operations  
 * defined within the Protocol Buffers <code>{@link DpQueryServiceGrpc}</code> interface. 
 * </p>
 * <p>
 * <h3>Shutdowns</h3>
 * Always shutdown the interface when no long needed.  This action releases all internal resources
 * required of this interface ensuring library maximum performance.  Shutdown operations are provided by
 * the methods <code>{@link #shutdown()}</code> and <code>{@link #shutdownNow()}</code>.
 * The methods <code>{@link #awaitTermination()}</code> and 
 * <code>{@link #awaitTermination(long, java.util.concurrent.TimeUnit)}</code> are available
 * to block until shutdown is complete (this step is not required).
 * </p>
 * <p>
 * <h2>GENERAL NOTES</h2>
 * <h3>Data Processing</h3>
 * Generally, a single data processor is used for all time-series data requests.  The processor is a single
 * <code>{@link QueryResponseProcessor}</code> instance maintained by the <code>IQueryService</code>
 * implementation object. 
 * The data process performs both the gRPC data 
 * streaming from the Query Service AND the correlation of incoming data.  The data processor
 * offers various configuration options where data can be simultaneously streamed and 
 * correlated, along with other multi-threading capabilities.  The data processor typically
 * uses multiple, concurrent gRPC data streams to recover results.  There are several points
 * to note about this implementation situation:
 * <ul>
 * <li>
 * Since a single data processor is used within this Query Service API, all time-series data request operations 
 * within an <code>IQueryService</code> interface are synchronized.  That is, only one time-series data request 
 * is performed at any instance and competing threads must wait until completed.
 * </li>
 * <li>
 * The data processor can be tuned with various configuration parameters within the 
 * <code>dp-api-config.yml</code> configuration file.  See class documentation on 
 * <code>{@link QueryResponseProcessor}</code> for more information on performance tuning.
 * </li>
 * <li>
 * It is informative to note that the <code>DpQueryResponseProcessor</code> class may use multiple gRPC data
 * streams for request data recovery but maintains a single gRPC channel connection.
 * </ul>
 * </p>
 * <p>
 * <h3>gRPC Connection</h3>
 * All communication to the Query Service is handled through a single gRPC channel instance.
 * These channel objects supported concurrency and multiple data streams between a client
 * and the targeted service.  However, excessive (thread) concurrency for a  
 * <code>IQueryService</code> interface may over-burden the single channel.
 * </p>
 * <p>
 * <h2>Best Practices</h2>
 * <ul>  
 * <li>Due to the conditions addressed above, clients utilizing extensive concurrency should 
 *     create multiple instances of <code>IQueryService</code> (each containing a single gRPC
 *     channel and a data processor).</li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 27, 2024
 *
 */
public interface IQueryService extends IConnection {

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
    public List<MetadataRecord> queryMeta(DpMetadataRequest rqst) throws DpQueryException;

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
     * <li>If the request is too large an exceptional result is returned.</li>
     * <li>Since result sets are limited by gRPC message size, implementations will generally return a static data table.</li>
     * </ul>
     * </p>
     * 
     * @param rqst  an initialized <code>{@link DpDataRequest]</code> request builder instance
     * 
     * @return      fully populated (static) data table.
     * 
     * @throws  DpQueryException    general exception during query or data reconstruction (see cause)
     */
    public IDataTable queryDataUnary(DpDataRequest rqst) throws DpQueryException;

    /**
     * <p>
     * Performs the given data request and returns the results as a table.
     * </p>
     * <p>
     * This is the preferred method for Query Service time-series data requests.  This method 
     * supports query result sets of any size, since the underlying data transport mechanism is
     * through gRPC data streams.  All results set data is transported back to the client,
     * processed into appropriate times-series, then assembled into the returned data table. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The data streaming and reconstruction can be performed simultaneously, with various
     * levels of concurrency.  Interface implementations typically employ an internal processor of 
     * type <code>{@link QueryRequestProcessor}</code>.  See the class documentation for details
     * on tuning the instance with the various performance parameters.
     * </li>
     * <br/>
     * <li>
     * The Query Service configuration section within the API configuration parameters 
     * (i.e., those of <code>{@link DpApiConfig}</code> and associated resource file),
     * provide access to the tuning parameters for internal processing and, thus, this method.
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
    public IDataTable queryData(DpDataRequest rqst) throws DpQueryException;

    /**
     * <p>
     * Performs the given list of data requests concurrently and returns all results within a 
     * single table.
     * </p>
     * <p>
     * This data request option is available for advanced clients wishing to subvert the default multi-streaming
     * mechanism available with <code>{@link #queryData(DpDataRequest)}</code>.  A this method directly determines
     * the number of gRPC data streams which is given as the size of the argument list.  
     * <em>Use this method at your own discretion.</em>
     * </p>
     * <p>   
     * Note that all requested data is returned on simultaneous gRPC data streams regardless of the default
     * setting in the library configuration.  The query results are returned are all returned in the same table. 
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
    public IDataTable queryData(List<DpDataRequest> lstRqsts) throws DpQueryException;

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
     * using the <code>{@link DpQueryStreamBuffer#addStreamObserver(com.ospreydcs.dp.api.query.IDpQueryStreamObserver)}</code>
     * method.
     * For more information on use of the returned stream buffer see documentation on 
     * <code>{@link DpQueryStreamBuffer}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Choose the gRPC data stream type using 
     * <code>{@link DpDataRequest#setStreamType(DpGrpcStreamType)}</code>.
     *   <ul>
     *   <li>Unidirectional streams are potentially faster but less stable.</li>
     *   <li>Bidirectional streams are typically more stable but potentially slower.</li>
     *   <li>The default is a unidirectional stream <code>{@link DpGrpcStreamType#BACKWARD}</code>.</li>
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
    public DpQueryStreamBuffer queryDataStream(DpDataRequest rqst);

}