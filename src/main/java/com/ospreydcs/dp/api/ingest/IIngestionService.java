package com.ospreydcs.dp.api.ingest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.model.IngestionResponse;
import com.ospreydcs.dp.api.model.ProviderRegistrar;
import com.ospreydcs.dp.api.model.ProviderUID;

public interface IIngestionService {

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
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Clients must first register with the Ingestion Service before invoking this method otherwise
     * an exception is thrown.
     * </li>
     * <li>
     * A list of Ingestion Service response is returned in order to accommodate all cases.
     *   <ul>
     *   <li>Frame Decomposition: If the frame has allocation larger than the maximum it is decomposed into composite 
     *       frames each receiving a response upon transmission.</li>
     *   <li>No Decomposition: If the frame has allocation less than the maximum limit a single response is
     *       contained in the list.</li>
     *   </ul>
     * </li>
     * </p>
     * 
     * @param frame ingestion frame containing data for transmission to the Ingestion Service
     * 
     * @return  collection of Ingestion Service responses to data within ingestion frame
     * 
     * @throws IllegalStateException    unregistered Data Provider
     * @throws DpIngestionException     general ingestion exception (see detail and cause)
     */
    public List<IngestionResponse> ingest(IngestionFrame frame) throws IllegalStateException, DpIngestionException;

    /**
     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination()
     */
    public boolean awaitTermination() throws InterruptedException;

    /**
     * @see @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination(long, TimeUnit)
     */
    public boolean awaitTermination(long cntTimeout, TimeUnit tuTimeout) throws InterruptedException;
    
}