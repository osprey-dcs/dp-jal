package com.ospreydcs.dp.api.ingest;

import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiBase;
import com.ospreydcs.dp.api.grpc.model.IConnection;
import com.ospreydcs.dp.api.model.IngestRequestUID;
import com.ospreydcs.dp.api.model.IngestionResponse;
import com.ospreydcs.dp.api.model.IngestionResult;
import com.ospreydcs.dp.api.model.ProviderRegistrar;
import com.ospreydcs.dp.api.model.ProviderUID;

/**
 * <p>
 * <h1>Interface containing required operations for data ingestion by gRPC data streaming.</h1>
 * </p> 
 * <p>
 * <h2>Connection Factory</h2>
 * Under normal circumstances implementations of <code>IIngestionStream</code> are obtained from a connection factory.
 * As such the internal configuration of the implementation is determined by the default configuration specified in
 * the API library configuration (i.e., <code>DpApiConfig</code>).
 * </p>
 * <p>
 * <h2>Operation</h2>
 * <ol>
 * <li>
 * Open Stream - The <code>{@link #openStream(ProviderRegistrar)}</code> operation activates the 3 primary components and 
 * creates and activates the auxiliary transfer component.  The interface is ready for ingestion.  
 * </li>  
 * <br/>
 * <li>
 * Data Ingestion - After the <code>{@link #openStream(ProviderRegistrar)}</code> operation the interface will now accept 
 * ingestion data through the <code>{@link #ingest(IngestionFrame)}</code> or <code>{@link #ingest(List)}</code> operations.
 * If enabled, ingestion throttling, or "back pressure", from a back-logged Ingestion Service will be felt at these methods 
 * (i.e., they will block).
 *   <ul>
 *   <li>Optionally, clients can use the <code>{@link #awaitQueueReady()}</code> method to block on the back pressure
 *       condition if ingestion throttling is disabled.</li>
 *   <li>Clients can also use the <code>{@link #awaitQueueEmpty()}</code> method to block until the staging buffer is
 *       completely empty.</li>
 *   </ul>
 * The ingestion throttling feature is available in the <code>DpApiConfig</code> library 
 * configuration and with the <code>{@link #enableBackPressure()}</code> and <code>{@link #disableBackPressure()}</code>
 * configuration methods specific to this class.
 * </li> 
 * <br/>
 * <li>
 * Close Stream - Under normal operation the data stream should be closed when no longer supplying ingestion data.
 *   <ul>  
 *   <li>The <code>{@link #closeStream()}</code> operation performs an orderly
 *        shut down of all components allowing all processing to continue until completion.</li>
 *   <li>The <code>{@link #closeStreamNow()}</code> shuts down all components immediately terminating all processing and 
 *       discarding any intermediate resources.</li>
 *   </ul>
 * Once a stream is closed it will refuse to accept additional ingestion data.
 * </li>
 * <br/>
 * <li>
 * Open/Ingest/Close Cycling - After a stream closure it can again be reopened (perhaps with different configuration and provider).
 * The above cycle can be repeated as often as desired so long as a shutdown operation has not been issued.
 * </li>
 * <br/>
 * <li>
 * Interface Shutdown - Before discarding the interface it should be shut down.  That is, once all data ingestion is complete
 * and the stream closed a <code>{@link #shutdown()}</code> or <code>{@link #shutdownNow()}</code> operation should be
 * invoked.  These operations terminate the connection between the client and the Ingestion Service and release all
 * gRPC resources.
 *   <ul>
 *   <li><code>{@link #shutdown()}</code> - Performs an orderly shutdown of the gRPC connection allowing all processes
 *       to fully complete.  Additionally, if the stream has not been first closed it first issues a 
 *       <code>{@link #closeStream()}</code> operation to close the stream.
 *       This is a blocking operation.</li>
 *   <li><code>{@link #shutdownNow()}</code> - Performs a hard shutdown of all operations, interface and gRPC.  All
 *       processes are immediately terminated and intermediate resources discarded.  If the stream has not been closed
 *       a <code>{@link #closeStreamNow()}</code> operation is first invoked.
 *       This is a non-blocking operation.</li>
 *   </ul>
 * Once shut down the interface can no longer be used and must be discarded.  Shut down operations may return before
 * all gRPC resources are release.  If needed, use <code>{@link #awaitTermination()}</code> or 
 * <code>{@link #awaitTermination(long, TimeUnit)}</code> to block until gRPC is fully terminated.
 * </li>
 * </ol>
 * </p>
 *  
 * @author Christopher K. Allen
 * @since Aug 16, 2024
 *
 */
public interface IIngestionStream extends IConnection {

    /**
     * <p>
     * Opens the data stream to the Ingestion Service.
     * </p>
     * <p>
     * The data provider is first registered with the Ingestion Service to obtained its UID which
     * is attached to all incoming ingestion frames while this stream is opened.
     * The data stream is then activated so that henceforth the <code>ingest</code> operations
     * are valid.
     * </p> 
     * <p>
     * <h2>Close Stream and Shutdown Operations</h2>
     * The data stream can be closed and reopened for different data providers.
     * Use <code>{@link #closeStream()}</code> or <code>{@link #closeStreamNow()}</code>.  
     * The open/close operations may be cycled repeatedly.
     * </p>
     * <p>
     * When no longer needed the stream should be closed then shutdown.  A final shutdown operation
     * is needed to release all resources held by this stream to maintain performance.
     * </p> 
     * 
     * @param recRegistration   data provider registration information (unique name)
     * 
     * @return  the unique identifier of the data provider with the Ingestion Service
     * 
     * @throws DpIngestionException provider registration failed, or general gRPC runtime error
     * 
     * @see #closeStream()
     * @see #closeStreamNow()
     * @see #shutdown()
     * @see #shutdownNow()
     */
    public ProviderUID openStream(ProviderRegistrar recRegistration) throws DpIngestionException;

    /**
     * <p>
     * Submits the given ingestion frame for processing and transmission to the 
     * Ingestion Service.
     * </p>
     * <p>
     * The given ingestion frame is passed to the internal processor where it is
     * queued for processing and transmission.  The method returns once the
     * ingestion frame is successfully queued.  Thus, there is no guarantee that the offered
     * data has been, or will be, successfully ingested.
     * </p>
     * <p>
     * <h2>Back Pressure</h2>
     * If client back pressure is enabled, either through client API configuration or explicitly
     * with <code>{@link #enableBackPressure(int)}</code>, the method will block if the
     * outgoing message queue is at capacity and not return until the queue drops below capacity.
     * If back pressure is disabled the method simply queues up the ingestion frame for 
     * processing and transmission then returns (i.e., the queue capacity is infinite).
     * </p> 
     * 
     * @param frame ingestion frame to be processed and submitted to Ingestion Service
     * 
     * @throws IllegalStateException    attempted to submit frame to unopened data stream
     * @throws InterruptedException     external process interruption while waiting on back pressure
     * @throws DpIngestionException     general data ingestion failure (see detail)
     */
    public void ingest(IngestionFrame frame) throws IllegalStateException, InterruptedException, DpIngestionException;

    /**
     * <p>
     * Submits the given collection of ingestion frames for processing and transmission to the
     * Ingestion Service.
     * </p>
     * <p>
     * The given collection of ingestion frames is passed to the internal processor where they
     * are queued for processing and transmission.  The method returns once the
     * ingestion frames are successfully queued.  Thus, there is no guarantee that all offered
     * data has been, or will be, successfully ingested.
     * </p>
     * <p>
     * <h2>Back Pressure</h2>
     * If client back pressure is enabled, either through client API configuration or explicitly
     * with <code>{@link #enableBackPressure(int)}</code>, the method will block if the
     * outgoing message queue is at capacity and not return until the queue drops below capacity.
     * If back pressure is disabled the method simply queues up the ingestion frames for 
     * processing and transmission then returns (i.e., the queue capacity is infinite).
     * </p> 
     * 
     * @param lstFrames ingestion frames to be processed and submitted to Ingestion Service
     * 
     * @throws IllegalStateException    attempted to submit frames to unopened data stream
     * @throws InterruptedException     external process interruption while waiting on back pressure
     * @throws DpIngestionException     general data ingestion failure (see detail)
     */
    public void ingest(List<IngestionFrame> lstFrames) throws IllegalStateException, InterruptedException, DpIngestionException;

    /**
     * <p>
     * Allows clients to block until the queue buffer containing the outgoing data ingestion
     * messages is ready (below capacity).
     * </p>
     * <p>
     * This method allows clients to due their own blocking at the ingestion side rather than
     * rely on the internal back-pressure mechanism.
     * Clients can wait for the outgoing queue buffer used by the internal gRPC
     * stream processor to drop below capacity.  
     * This activity can also be useful when clients wish to due their own performance tuning.
     * Specifically, clients can add a fixed number of  ingestion frames then measure the time 
     * for the queue ready event.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method will return immediately if the outgoing data message queue is below capacity.
     * </li>
     * <li>
     * This method is thread safe and multiple clients can block on this method.
     * </li>
     * <li>
     * All clients blocking on this method will unblock when the outgoing message buffer drops below capacity.
     * </li>
     * </ul>
     * </p> 
     * 
     * @throws IllegalStateException    operation invoked while stream closed and processor inactive
     * @throws InterruptedException     operation interrupted while waiting for queue ready
     */
    public void awaitQueueReady() throws IllegalStateException, InterruptedException;

    /**
     * <p>
     * Allows clients to block until the queue buffer containing the outgoing data ingestion
     * messages empties.
     * </p>
     * <p>
     * This activity can be useful when clients wish to due their own performance tuning.
     * Clients can wait for the outgoing queue buffer used by the internal gRPC
     * stream processor to fully empty.  Specifically, clients can add a fixed number of 
     * ingestion frames then measure the time for frames to be fully processed and and 
     * transmitted to the Ingestion Service.  
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method will return immediately if the outgoing data message queue is empty.
     * </li>
     * <li>
     * This method is thread safe and multiple clients can block on this method.
     * </li>
     * <li>
     * All clients blocking on this method will unblock when the outgoing message buffer empties.
     * </li>
     * </ul>
     * </p> 
     * 
     * @throws IllegalStateException    operation invoked while stream closed and processor inactive
     * @throws InterruptedException     operation interrupted while waiting for queue ready
     */
    public void awaitQueueEmpty() throws IllegalStateException, InterruptedException;

    /**
     * <p>
     * Closes the data stream to the Ingestion Service.
     * </p>
     * <p>
     * Performs a "soft close" of the current Ingestion Service data stream.  All previously
     * submitted ingestion frames are allowed to continue processing and transmission to the
     * Ingestion Service.  However, no new ingestion frames will be accepted (unless the stream
     * is re-opened later).  
     * </p>
     * This is a blocking operation, it does not return until all previously submitted data is
     * fully transmitted.  This condition may be useful when clients wish to due performance tuning.
     * <p>
     * <h2>Ingestion Service Responses</h2>
     * The method returns a list of all responses received by the Ingestion Service during data
     * transmission, encapsulated as a client API record rather than a raw gRPC message.
     * The number of records in the list depends on several factors:
     * <ul>
     * <li>Size of the ingestion frame (i.e., memory allocation): 
     *   Large ingestion frames may be
     *   decomposed into smaller frames meeting any gRPC message size limitations.  Thus, the
     *   number of records may be larger than the number of ingestion frames in this case.
     * </li>
     * <li>gRPC message size limitation:
     *   As described above, the gRPC message size limitation will determine the number of
     *   decompose ingestion frames required to conform to the limit.
     * </li>
     * <li>gRPC data stream type (either unidirectional or bidirectional):
     *   There are two possible gRPC data stream types available for data transmission.  In the
     *   bidirectional case a response is received by the Ingestion Service for every data message
     *   transmitted.  In the unidirectional cases there is at most one response from the 
     *   Ingestion Service.
     * </li>
     * </ul>
     * </p>
     * The conditions described above are technical but all can be configured within the 
     * client API configuration parameter set in <em>dp-api-config.yml</em> and available
     * in the <code>{@link DpApiConfig}</code> configuration class.
     * </p>
     * 
     * @return  the result of the ingestion operation 
     * 
     * @throws IllegalStateException    attempted to close an unopened stream
     * @throws InterruptedException     internal processor interrupted while waiting for pending tasks
     * @throws CompletionException      internal error while shutting down resources (see detail and cause)
     * @throws MissingResourceException internal error converting ingestion responses to result (this should not occur)
     */
    public IngestionResult  closeStream() throws IllegalStateException, InterruptedException, CompletionException, MissingResourceException;

    /**
     * <p>
     * Immediately closes the data stream to the Ingestion Service.
     * </p>
     * <p>
     * Performs a "hard close" of the current Ingestion Service data stream.  This method is typically invoked
     * for exceptional situations.  The following operations are performed:
     * <ul>
     * <li>All pending operations are cancelled.</li>
     * <li>All ingestion frames queued for processing are discarded.</li>
     * <li>All processed frames awaiting transmission are discarded.</li>
     * </ul>   
     * </p>
     * <p>
     * The method returns immediately after canceling all active processes.  Upon return
     * the data stream is inactive.  No exceptions are thrown.  This method reacts according to
     * the current state of the stream.
     * </p>
     * <p>
     * <h2>Returned Value</h2>
     * <ul>
     * <li>If the stream was never opened the value <code>{@link IngestionResult#NULL}</code> is returned.</li>
     * <li>If the stream is active the returned value contains the results up to this point and are likely
     *     incomplete.</li>  
     * <li>If the stream was already shutdown nothing is done and the returned value is a copy of that
     *     returned from <code>{@link #closeStream()}</code>.</li>
     * </ul>
     * </p>
     * 
     * @return  the result of the ingestion operation as described above
     * @return  <code>true</code> if stream was closed everything was shut down,
     *          <code>false</code> if the stream was already closed or internal process failed
     */
    public IngestionResult closeStreamNow();

//    /**
//     * <p>
//     * Returns the current size of the queue buffer containing outgoing data ingestion 
//     * messages waiting for transmission. 
//     * </p>
//     * <p>
//     * Returns the current size of the ingest data request message queue buffer used by the
//     * internal gRPC stream processor.  This value can be used to estimate transmission
//     * performance by the client by timing the consumption of queued data ingestion messages.
//     * </p>
//     * <p>
//     * Technically, the value returned is the number of 
//     * <code>IngestDataRequest</code> messages that are currently queued up and waiting 
//     * on an available stream processor thread for transmission.
//     * </p> 
//     * 
//     * @return  number of <code>IngestDataRequest<code> messages in the request queue
//     * 
//     * @throws IllegalStateException    stream was never opened and processor never activated
//     */
//    int getQueueSize() throws IllegalStateException;

    /**
     * <p>
     * Returns an immutable list of "client request UIDs" taken from all the data ingestion messages
     * sent to the Ingestion Service during the current open stream session.
     * </p>
     * <p>
     * Every ingest data request message contains a "client request UID" that provides the
     * unique identifier for that request.  The Ingestion Service records the identifier for later
     * query.  These values may also be compared with the list of received request UIDs from the
     * <code>{@link IngestionResult#receivedRequestIds()}</code>.
     * </p>  
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Client request UIDs are assigned by the internal processor before transmission to the
     * Ingestion Service, if not directly assigned by the client 
     * (see <code>{@link IngestionFrame#setClientRequestUid(IngestRequestUID)}</code>). 
     * In the case of large <code>IngestionFrame</code> instances that are decomposed into
     * smaller frames (to meet gRPC message size limits), the decomposed frames may be suffixed
     * with a count number or given the same UID. 
     * </li>
     * <li>
     * The returned collection is a list of all client request IDs assigned to outgoing data
     * ingestion messages and can be used by clients to locate ingested data within the archive,
     * or query the Ingestion Service about request status post ingestion.
     * </li>
     * <li>
     * The ordering of this list does not necessarily reflect the order that ingestion frames
     * were offered to the processor.  Processed ingestion request messages do not necessarily
     * get transmitted in order.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  all client request IDs for messages transmitted to Ingestion Service.
     * 
     * @throws IllegalStateException    the stream was not opened and processor was never activated
     */
    public List<IngestRequestUID> getRequestIds() throws IllegalStateException;

    /**
     * <p>
     * Determines whether or not the stream between the Ingestion Service is currently open.
     * </p>
     * 
     * @return  <code>true</code> if open, <code>false</code> if closed.
     */
    public boolean isStreamOpen();

//    /**
//     * <p>
//     * Returns the Data Provider UID for the Data Provider registration that was used to open this data stream.
//     * </p>
//     * <p>
//     * If the data stream to the Ingestion Service was opened then subsequently closed, the Provider UID last used
//     * to open the stream is returned.
//     * </p>
//     * 
//     * @return  the UID of the data provider which opened the data stream to the Ingestion Service
//     * 
//     * @throws IllegalStateException    the data stream was never opened
//     */
//    ProviderUID getProviderUid() throws IllegalStateException;

    /**
     * <p>
     * Performs soft shutdown of this Ingestion Service interface.
     * </p>
     * <p>
     * This method is overridden to check for a currently open data stream.
     * Normally the data stream to the Ingestion Service should be explicitly closed before
     * calling this operation, either with <code>{@link #closeStream()}</code> or
     * <code>{@link #closeStreamNow()}</code>.  
     * If the data stream has not been closed this method will
     * perform a soft close <code>{@link #closeStream()}</code> operation, blocking until
     * complete.  
     * </p>
     * <p>
     * Once the data stream is closed, either explicitly or implicitly as described above,
     * the method defers to the base class for the actual interface shut down (e.i., involving
     * the gRPC channel under management).  
     * See <code>{@link DpServiceApiBase#shutdown()}</code> documentation for details.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Although this method blocks until the stream is closed (if necessary) not all gRPC
     * resources may be fully released upon return. Use <code>{@link IConnection#awaitTermination()}</code>
     * or <code>{@link IConnection#awaitTermination(long, java.util.concurrent.TimeUnit)</code> to
     * block if required.  The method <code>{@link IConnection#isTerminated()}</code> to determine
     * if the gRPC system has fully terminated.
     * </p>
     * <p>
     * <h2>WARNING</h2>
     * Do not use this interface after calling this method, this is a finalizing operation
     * before discarding.
     * This interface is no longer valid after collection and any further operations will
     * result in exceptions.  All connections with the Ingestion Service have been terminated.
     * </p>
     *
     * @return  <code>true</code> if the Ingestion Service interface was successfully shut down,
     *          <code>false</code> if an error occurred
     *          
     * @throws  IterruptedException interrupted on a close operation or gRPC connection shut down         
     *          
     * @see #closeStream()
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#shutdown()
     * @see IConnection#awaitTermination()
     */
    public boolean shutdown() throws InterruptedException;

    /**
     * <p>
     * Performs hard shutdown of this Ingestion Service interface.
     * </p>
     * <p>
     * This method is overridden to check for a currently open data stream.
     * Normally the data stream to the Ingestion Service should be explicitly closed before
     * calling this operation, either with <code>{@link #closeStream()}</code> or
     * <code>{@link #closeStreamNow()}</code>.  
     * If the data stream has not been closed this method will
     * perform a soft close <code>{@link #closeStreamNow()}</code> operation, immediately
     * terminating all active processing and data transmission. 
     * </p>
     * <p>
     * Once the data stream is closed, either explicitly or implicitly as described above,
     * the method defers to the base class for the actual interface shut down (e.i., involving
     * the gRPC channel under management).  
     * See <code>{@link DpServiceApiBase#shutdownNow()}</code> documentation for details.
     * </p>
     * <p>
     * <h2>WARNING</h2>
     * Do not use this interface after calling this method, this is a finalizing operation
     * before discarding.
     * This interface is no longer valid after collection and any further operations will
     * result in exceptions.  All connections with the Ingestion Service have been terminated.
     * </p>
     * 
     * @return  <code>true</code> if Ingestion Service interface was successfully shut down,
     *          <code>false</code> if an error occurred
     *
     * @see #closeStreamNow()
     * @see com.ospreydcs.dp.api.grpc.model.DpServiceApiBase#shutdownNow()
     */
    public boolean shutdownNow();

}