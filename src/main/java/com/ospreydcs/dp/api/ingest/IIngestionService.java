package com.ospreydcs.dp.api.ingest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.grpc.model.IConnection;
import com.ospreydcs.dp.api.model.IngestRequestUID;
import com.ospreydcs.dp.api.model.IngestionResult;
import com.ospreydcs.dp.api.model.ProviderRegistrar;
import com.ospreydcs.dp.api.model.ProviderUID;

public interface IIngestionService extends IConnection {

    /**
     * <p>
     * Register the Data Provider with the Ingestion Service for later data ingestion operations.
     * </p>
     * <p>
     * Data providers must first register with the Ingestion Service using their unique name and optional
     * attributes.  Upon registration the Ingestion Service returns a UID for the provider which it must use
     * for all subsequent ingestion operations.  If a data provider has previously registered with the Ingestion 
     * Service its original UID is returned.
     * </p>
     * <p>
     * <h2>Provider Attributes</h2>
     * Data provider attributes can be added in the form of (name, value) pairs (of strings) during registration.
     * Use the <code>{@link ProviderRegistrar#addAttribute(String, String)}</code> method to assignment attribute
     * pairs to a data provider (this method may be called as many times as desired, once for each attribute pair).
     * Data Provider attributes are used as metadata to garnish the Data Platform data archive.
     * </p>
     * 
     * @param recRegistration   record containing data provider unique name and any optional attributes
     * 
     * @return                  record containing the data provider UID
     *         
     * @throws DpIngestionException     registration failure or general communications exception (see details)
     */
    public ProviderUID registerProvider(ProviderRegistrar recRegistration) throws DpIngestionException;

    /**
     * <p>
     * Blocking, synchronous, unary ingestion of the given ingestion frame.
     * </p>
     * This is the primary, and only, data ingestion operation of the <code>IIngestionService</code> interface.
     * The argument is converted to <code>IngestDataRequest</code> message(s) then transmitted to the Ingestion
     * Service one message at a time using a single,blocking RPC operation.  This is inherently a safe and robust
     * operation but at the cost of performance.  It is appropriate for small to moderate data transmission.
     * Clients requiring large data transmission or continuous and sustained transmission should consider the
     * <code>IIngestionStream</code> interface.  
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Clients must first register with the Ingestion Service before invoking this method otherwise
     * an exception is thrown.
     * </li>
     * <li>
     * A list of Ingestion Service response is returned in order to accommodate all the following cases.
     *   <ul>
     *   <li>Frame Decomposition: If the frame has allocation larger than the maximum it is decomposed into composite 
     *       frames.  Each composite frame is converted into a message that receives a response upon transmission.
     *       The returned list will contain an Ingestion Service response for each composite message.
     *   </li>
     *   <li>No Decomposition: If the frame has allocation less than the maximum limit a single response is
     *       contained in the list.
     *   </li>
     *   <li>
     *   If automatic frame-decomposition is disabled and the offered frame results in a gRPC message larger than
     *   the current gRPC message size limit an exception will be thrown (i.e., nothing is returned).
     *   </li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     * 
     * @param frame ingestion frame containing data for transmission to the Ingestion Service
     * 
     * @return  the result of the ingestion operation
     * 
     * @throws IllegalStateException    unregistered Data Provider
     * @throws DpIngestionException     general ingestion exception (see detail and cause)
     */
    public IngestionResult ingest(IngestionFrame frame) throws IllegalStateException, DpIngestionException;
    
    /**
     * <p>
     * Returns a list of "client request UIDs" taken from all the data ingestion messages
     * sent to the Ingestion Service during the current session.
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
     * @return  all client request IDs for messages transmitted to Ingestion Service so far
     * 
     * @throws IllegalStateException    likely from unregistered interface state
     */
    public List<IngestRequestUID>   getRequestIds() throws IllegalStateException;

    /**
     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination()
     */
    public boolean awaitTermination() throws InterruptedException;

    /**
     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination(long, TimeUnit)
     */
    public boolean awaitTermination(long cntTimeout, TimeUnit tuTimeout) throws InterruptedException;
    
}