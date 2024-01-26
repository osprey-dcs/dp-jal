/*
 * Project: dp-api-common
 * File:	DpQueryStreamQueueBufferDeprecated.java
 * Package: com.ospreydcs.dp.api.grpc.query.model
 * Type: 	DpQueryStreamQueueBufferDeprecated
 *
 * Copyright 2010-2022 the original author or authors.
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
 * @since Sep 29, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.RejectDetails;
import com.ospreydcs.dp.grpc.v1.common.RejectDetails.RejectReason;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest.CursorOperation;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest.QuerySpec;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

import io.grpc.stub.CallStreamObserver;
import io.grpc.stub.StreamObserver;

/**
 * <p>
 * A stream buffer and blocking queue for gRPC data streams to the Query Service.
 * </p> 
 * <p>
 * This class performs the actual data query from the <em>Query Service</em>.  
 * It receives the data request with a <code>start</code> method which launches the initiates the data streaming
 * from the <em>Query Service</em>.  
 * The data is received from the <em>Query Service</em> in pages  
 * and saved into a buffer for later processing (<code>{@link #lstDataBuffer}</code>).
 * </p>
 * <p>
 * To initiate the data stream call the 
 * <code>{@link #startUniStream(QueryRequest)}</code> method to initiate a unidirectional stream or 
 * <code>{@link #startBidiStream(QueryRequest)}</code> method to initiate a bidirectional stream.
 * The argument of each method contains the the desired gRPC data request message.
 * The <code>DpQueryStreamQueueBufferDeprecated</code> instance itself receives the responses through the implemented methods of the 
 * <code>StreamObserver<PaginatedResponse> interface (that is, the instance acts as the backward stream from
 * the Query Service).
 * </p>  
 * <p>
 * The <code>DpQueryStreamQueueBufferDeprecated</code> class may be used as either a <em>stream buffer</em> collecting data pages 
 * as they become available, or a <em>blocking queue</em> allowing clients to consume data pages as they become 
 * available.  Alternatively, <code>{@link IDataStreamObserver}</code> instances can be registered and then
 * will be called back to receive notifications and data pages as they become available.
 * </p>
 * <h2>Stream Buffer</h2>  
 * Use the <code>{@link #getBufferSize()}</code> method to determine the current buffer size.
 * Use the <code>{@link #getBufferPage(Integer)}</code> method to retrieve data
 * pages of a particular index.  
 * </p>
 * <p>
 * <h2>Blocking Queue</h2>
 * Use the <code>{@link #getQueueSize()}</code> method to determine the current size of the queue buffer.
 * Use the <code>{@link #removeQueuePage()}<?code> or <code>{@link #removeQueuePageTimeout()}</code> methods
 * to retrieve data pages from the queue as they become available.  These latter methods will block until
 * a data page becomes available or return immediate if one is already available.
 * </p>
 * <p>
 * <h2>Observers</h2>
 * Alternative to either stream buffer or stream queue usage, 
 * one can register a callback using <code>{@link #addStreamObserver(IQueryStreamQueueBufferObserver)}</code>
 * method which will be invoked whenever a new data page becomes available.
 * The index of that page along with the gRPC message response containing
 * the data is provided to the callback methods.  The <code>{@link IDataStreamObserver}</code> interface
 * also contains notifications for streaming events, like start, completed, and error notifications.
 * </p> 
 * <p>
 * <h2>Streaming</h2>
 * For unidirectional streaming the <em>Query Service</em> simply transmits data pages at its
 * maximum rate.
 * <br/> <br/>  
 * For bidirectional streaming a handle to the <em>Query Service</em> forward stream ({@link #hndSvrStrm}) is 
 * created from the given gRPC non-blocking service stub.  This is the handle by which we send the data page 
 * requests.  That is, in bidirectional stream each data page is requested explicitly.  This may be slower
 * but it is more reliable.
 * </p> 
 * <p>
 * Note that this class is not explicitly a separate Java thread process,
 * although it behaves much like one.  The actual thread is the 
 * <em>Query Service</em> data stream. The class serves as a receiver (backward stream) of
 * <em>Query Service</em> data messages and, thus, responds to them as they
 * occur acting much like an independent process thread.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Sep 29, 2022
 * @version Jan 10, 2024
 * 
 * @deprecated  Replaced by DpQueryStreamBuffer and DpQueryStreamQueueBufferDeprecated
 */
@Deprecated(since="Jan 16, 2024", forRemoval=true)
public class DpQueryStreamQueueBufferDeprecated implements StreamObserver<QueryResponse> /* Serializable */ {

    
    //
    // Application Resources
    //

    /** The default Query Service configuration parameters */
    private static final DpQueryConfig  CFG_DEFAULT = DpApiConfig.getInstance().query;

    
    //
    // Class Constants
    //
    
//    /** Java Serialization identifier */
//    private static final long serialVersionUID = 3279632014097816865L;

    
    //
    // Class Resources
    //
    
    /** Internal event logger */
    private static final Logger LOGGER = LogManager.getLogger(); 
    
    
    //
    // Initialization Targets
    //
    
    /** The streaming communications stub of the Query Service gRPC interface */
    private final DpQueryServiceStub    stubAsync;
    
    /** Maximum timeout limit to wait for the responses from the Query Service */
    private final Long                  cntTimeout;

    /** Maximum timeout limit units for response from the Query Service */
    private final TimeUnit              tuTimeout;
    
    
    //
    // Data Consumers
    //
    
    /** List of data consumers to be notified about streaming data and conditions */
    private List<IQueryStreamQueueBufferObserver>   lstStreamObservers = new LinkedList<>();
    
    
    //
    // Streaming Data Resources - Retrieved data pages and page monitors
    //

    /** The data page buffer, also used as a queue buffer - contains all the data responses from the Query Service */
    private final LinkedList<QueryResponse.QueryReport.QueryData>   lstDataBuffer = new LinkedList<>();

//    /** Contains all the data responses from the Query Service - indexed by data page */
//    private final Map<Integer, QueryResponse.QueryReport.QueryData>   mapRspBuffer = new HashMap<>();
//
//    /** Map of page monitors for each data page in the stream - to block on {@link #getPage(Integer)} */
//    private final Map<Integer, CountDownLatch>      mapPgMons = new HashMap<>();


    //
    // Streaming Process Resources - Page request queue and response list
    //

    /** Service stream handle of forward stream (to server) if bi-directional stream  - used to send cursor requests */
    private CallStreamObserver<QueryRequest>        hndSvrStrm = null;
    
    /** The queue of data page indices acquired so far */
    private final LinkedList<Integer>               lstPgsAcquired = new LinkedList<Integer>();
    
    /** List of page indices already requested */
    private final List<Integer>                     lstPgsRequested = new LinkedList<Integer>();
    

    //
    // Streaming State Variables - Event Monitor Latches
    //
    
    /** First response monitor from <em>Query Service</em> - we cannot stream until this is released */
    private final CountDownLatch monStrmStart = new CountDownLatch(1);
    
    /** The stream end monitor - should be released when the <code>onCompleted</code> signal is received */
    private final CountDownLatch monStrmEnd = new CountDownLatch(1);
    
    /** Data page available semaphore  */
    private final Semaphore     semPageRdy = new Semaphore(0, true);
    
    /** Synchronization locking object - CANNOT synchronize on class instance because of StreamObserver implementation */
    private final Object        objLock = new Object();

    
    //
    // Streaming State Variables - Condition Flags.
    //
    
    /** Indicates that a bidirectional stream was initiated */
    private boolean bolStreamBidi = false;
    
    /** Indicates that a data stream has been requested, i.e., a start() method has been called */
    private boolean bolStreamRequested = false;
    
    /** Streaming completed flag */
    private boolean bolStreamCompleted = false;
    
    /** Query request rejected */
    private boolean bolRequestRejected = false;
    
    /** Streaming error flag */
    private boolean bolStreamError = false;
    
    
    //
    // Streaming State - Parameters
    //
    
//    /** Size of each returned data page - set by {@link #start(Request)} */
//    private Integer szPage = null;
    
//    /** Page index of the first page in the data stream (may be different than 1) */
//    private Integer indFirstPage = null;
    
    /** Index of the last page requested - initialized by {@link #onNext(PaginatedResponse)} */
    private Integer indNextPage = null;
    
    /** The number of data pages streamed so far */
    private Integer cntPages = null;
    
    /** Size (in bytes) of the first data message - initialized by {@link #onNext(PaginatedResponse)} */
    private Long    szPage = null;
    
    /** Any error message from the Query Service - from {@link #onError(Throwable)} exception message */
    private String  strErrMsg = null;

    
    //
    // Creators
    // 
    
    /**
     * <p>
     * Creates a new initialized, instance of <code>DpQueryStreamQueueBufferDeprecated</code> with default timeout parameters.
     * </p>
     * <p>
     * Note, the data stream is initiated by calling either <code>{@link #startUniStream(QueryRequest)}</code> 
     * or <code>{@link #startUniStream(QueryRequest)}</code> method with the desired data request. 
     * </p>
     * 
     * @param stubAsync the Protobuf communications stub containing streaming Query Service gRPC interface
     *  
     * @return  new <code>DpQueryStreamQueueBufferDeprecated</code> instance ready for Query Service stream initiation
     */
    public static DpQueryStreamQueueBufferDeprecated   from(DpQueryServiceGrpc.DpQueryServiceStub stubAsync) {
        return DpQueryStreamQueueBufferDeprecated.from(stubAsync, CFG_DEFAULT.timeout.limit, CFG_DEFAULT.timeout.unit);
    }
    
    /**
     * <p>
     * Creates a new initialized, instance of <code>DpQueryStreamQueueBufferDeprecated</code> with specified timeout parameters.
     * </p>
     * <p>
     * Note, the data stream is initiated by calling either <code>{@link #startUniStream(QueryRequest)}</code> 
     * or <code>{@link #startUniStream(QueryRequest)}</code> method with the desired data request. 
     * </p>
     * 
     * @param stubAsync the Protobuf communications stub containing streaming Query Service gRPC interface 
     * @param cntTimeout timeout limit to use while waiting for <em>Query Service</em> responses or operations
     * @param tuTimeout  timeout units to use while waiting for <em>Query Service</em> responses or operations
     * 
     * @return  new <code>DpQueryStreamQueueBufferDeprecated</code> instance ready for Query Service stream initiation
     */
    public static DpQueryStreamQueueBufferDeprecated   from(DpQueryServiceGrpc.DpQueryServiceStub stubAsync, long cntTimeout, TimeUnit tuTimeout) {
        return new DpQueryStreamQueueBufferDeprecated(stubAsync, cntTimeout, tuTimeout);
    }
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Creates a new initialized, instance of <code>DpQueryStreamQueueBufferDeprecated</code>.
     * </p>
     * <p>
     * Note, the data stream is initiated by calling either <code>{@link #startUniStream(QueryRequest)}</code> 
     * or <code>{@link #startUniStream(QueryRequest)}</code> method with the desired data request. 
     * </p>
     * <p>
     * A handle to the <em>Query Service</em> stream ({@link #hndSvrStrm}) is 
     * created from the given gRPC non-blocking service stub.  This is the
     * handle by which we send the <em>Query Service</em> data requests.
     * The <code>DpQueryStreamQueueBufferDeprecated</code> instance itself received the
     * responses through the implemented methods of the 
     * <code>StreamObserver<PaginatedResponse> interface.  
     * </p> 
     *
     * @param stubAsync the Protobuf communications stub containing streaming Query Service gRPC interface 
     * @param cntTimeout timeout limit to use while waiting for <em>Query Service</em> responses or operations
     * @param tuTimeout  timeout units to use while waiting for <em>Query Service</em> responses or operations
     */
    public DpQueryStreamQueueBufferDeprecated(DpQueryServiceGrpc.DpQueryServiceStub stubAsync, long cntTimeout, TimeUnit tuTimeout) {
        this.stubAsync = stubAsync;
        this.cntTimeout = cntTimeout;
        this.tuTimeout  = tuTimeout;
    }
    
    
    /**
     * Adds the given <code>IQueryStreamQueueBufferObserver</code> interface to the
     * list of stream observers requesting notifications about streaming
     * conditions and stream data.
     * 
     * @param ifcStreamObserver subject to be notified about streaming events
     */
    public void addStreamObserver(IQueryStreamQueueBufferObserver ifcStreamObserver) {
        this.lstStreamObservers.add(ifcStreamObserver);
    }


    //
    // Operations
    //
    
    /**
     * <p>
     * Initiates the <em>Query Service</em> data request as a unidirectional stream.  
     * </p>
     * <p>
     * The given request is sent to the <em>Query Service</em> with a unidirectional streaming
     * RPC operation.  The call initiates a unidirectional data stream and incoming data is collected
     * here.  
     * </p>
     * <p>
     * The Query Service unidirectional streaming request initiates the gRPC data stream after which
     * the Query Service streams requested data at arbitrary rate.  It is up to the client to process
     * data in a timely fashion.  Bidirectional streaming provides a more synchronized data path for
     * clients with excessive processing requirements (see <code>{@link #startBidiStream(QueryRequest)}</code>).
     * <p>
     * After this method returns the streaming process becomes
     * independent and thread-like.  The requested data becomes available
     * at it arrives from the <em>Query Service</em>, and may take some time
     * after the return of this method depending upon the size of the data
     * request.
     * </p>
     * <p>
     * Notifications about stream activities is sent to any registered data stream observers
     * (see <code>{@link #addStreamObserver(IQueryStreamQueueBufferObserver)}</code>).  Stream observers may
     * then spawn thread to process available data.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This method is internally synchronized and callers could block if multiple
     * concurrent invocations occur.
     * </p>
     * 
     * @param msgRqst   data request for the <em>Query Service</em>
     * 
     * @throws IllegalStateException    gRPC stream has already been started
     * @throws IllegalArgumentException gRPC message does not contain a <code>QuerySpec</code> message
     * throws TimeoutException         timeout waiting for the <em>Query Service</em> response
     * throws InterruptedException     the process was interrupted while waiting for the <em>Query Service</em> 
     */
    public void startUniStream(QueryRequest msgRqst) throws IllegalStateException, IllegalArgumentException /*, TimeoutException, InterruptedException */ {

        synchronized (this.objLock) {
            
            // Log event
            if (isLogging()) {
                LOGGER.debug("{} called with following request:", JavaRuntime.getQualifiedCallerName());
                LOGGER.debug(msgRqst.toString());
            }

            // Check if stream has been started
            if (this.bolStreamRequested) {
                String strMsg = JavaRuntime.getQualifiedCallerName() + ": ERROR - Attempted to start a stream that has already been requested.";
                if (isLogging())
                    LOGGER.error(strMsg);

                throw new IllegalStateException(strMsg);
            }

//            // Check argument
//            if (!msgRqst.hasQuerySpec()) {
//                String  strMsg = JavaRuntime.getQualifiedCallerName() + ": argument does not contain initial query.";
//                if (isLogging())
//                    LOGGER.error(strMsg);
//
//                throw new IllegalArgumentException(strMsg);
//            }


//            // Perform the initial streaming RPC operation initiating the data stream 
//            this.bolStreamRequested = true;
//            this.stubAsync.queryResponseStream(msgRqst, this);
            this.requestDataStream(msgRqst);
        }
    }
    
    /**
     * <p>
     * Initiates the <em>Query Service</em> data request as a bidirectional (BIDI) stream.  
     * </p>
     * <p>
     * The given request is sent to the <em>Query Service</em> with a bidirectional streaming
     * RPC operation.  The call initiates a unidirectional data stream and incoming data is collected
     * here.  
     * </p>
     * <p>
     * The Query Service bidirectional streaming request requires that the client must explicitly request
     * each page of data.  This condition may be advantageous for preventing back-logging of unprocessed
     * data.  However, it may perform slower than a unidirectional request.
     * </p>
     * <p>
     * After this method returns the streaming process becomes
     * independent and thread-like.  The requested data becomes available
     * at it arrives from the <em>Query Service</em>, and may take some time
     * after the return of this method depending upon the size of the data
     * request.
     * </p>
     * <p>
     * Notifications about stream activities is sent to any registered data stream observers
     * (see <code>{@link #addStreamObserver(IQueryStreamQueueBufferObserver)}</code>).  Stream observers may
     * then spawn thread to process available data.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This method is internally synchronized and callers could block if multiple
     * concurrent invocations occur.
     * </p>
     * 
     * @param msgRqst   data request for the <em>Query Service</em>
     * 
     * @throws IllegalStateException    gRPC stream has already been started
     * @throws IllegalArgumentException gRPC message does not contain a <code>QuerySpec</code> message
     * throws TimeoutException         timeout waiting for the <em>Query Service</em> response
     * throws InterruptedException     the process was interrupted while waiting for the <em>Query Service</em> 
     */
    public void startBidiStream(QueryRequest msgRqst) throws IllegalStateException , IllegalArgumentException /*, InterruptedException */ {
        
        synchronized (this.objLock) {

            // Log event
            if (isLogging()) {
                LOGGER.debug("{} called with following request:", JavaRuntime.getQualifiedCallerName());
                LOGGER.debug(msgRqst.toString());
            }

            // Check if stream has been started
            if (this.bolStreamRequested) {
                String strMsg = JavaRuntime.getQualifiedCallerName() + ": ERROR - Attempted to start a stream that has already been requested.";
                if (isLogging())
                    LOGGER.error(strMsg);

                throw new IllegalStateException(strMsg);
            }

//            // Check argument
//            if (!msgRqst.hasQuerySpec()) {
//                String  strMsg = JavaRuntime.getQualifiedCallerName() + ": argument does not contain initial query.";
//                if (isLogging())
//                    LOGGER.error(strMsg);
//
//                throw new IllegalArgumentException(strMsg);
//            }


            this.bolStreamBidi = true;
            this.requestDataStream(msgRqst);
            
//            // Perform the initial streaming RPC operation getting the forward (to server) stream interface 
//            this.hndSvrStrm = (CallStreamObserver<QueryRequest>) this.stubAsync.queryResponseCursor(this);
//
//            // Send the first data request initializing the stream
//            this.bolStreamRequested = true;
//            this.hndSvrStrm.onNext(msgRqst);
        }
    }
    
    /**
     * <p>
     * Waits (blocks) until the start of the gRPC data stream from the Query Service.
     * </p>
     * <p>
     * Waits for the first response from the <em>Query Service</em> by latching
     * onto the stream start monitor (<code>{@link #monStrmStart}</code>).  
     * The monitor is released during the first Query Service call to the 
     * <code>{@link #onNext(QueryResponse)} method.  
     * <p>
     * <p>
     * It is assumed that either <code>{@link #startUniStream(QueryRequest)}</code> or 
     * <code>{@link #startBidiStream(QueryRequest)}</code> has previously been called.
     * Upon return from this method the stream buffer begins processing the incoming data.
     * </p>
     *  
     * @throws TimeoutException waiting time for the response expired
     * @throws InterruptedException an interruption occurred while waiting for response
     * 
     * @see #startUniStream(QueryRequest)
     * @see #startBidiStream(QueryRequest)
     */
    public void awaitStreamStart() throws TimeoutException, InterruptedException {
    
        // Log event
        if (isLogging())
            LOGGER.debug("{} - latching on monitor StreamStart for {} {}", JavaRuntime.getQualifiedCallerName(),cntTimeout, tuTimeout);
    
        boolean bolReleased = this.monStrmStart.await(this.cntTimeout, this.tuTimeout);
        
        if ( !bolReleased ) {
            String strMsg = "DpQueryStreamQueueBufferDeprecated#awaitStreamStart() - Timeout out waiting on first response from the Datastore";
    
            // Log event
            if (isLogging())
                LOGGER.debug(strMsg);
            
            throw new TimeoutException(strMsg);
        }
    }


    /**
     * <p>
     * Waits (blocks) until the gRPC data stream with the Query Service has completed.
     * </p>
     * <p> 
     * Waits for the <code>onCompleted</code> response from the 
     * <em>Query Service</em> by latching
     * onto the stream end monitor (<code>{@link #monStrmEnd}</code>) which
     * is released by the <code>{@link #onCompleted()}</code> method.  
     * </p>
     * <p>
     * Once the monitor is released the calling method unblocks and 
     * returns to the caller.  The data stream is ready for a <code>shutdown</code> method
     * call.
     * </p>
     * 
     * @throws TimeoutException waiting time for the response expired
     * @throws InterruptedException an interruption occurred while waiting for response
     *  
     * @see #shutdownSoft()
     * @see #shutdownNow()
     */
    public void awaitStreamCompleted() throws InterruptedException, TimeoutException {
        
        // Log event
        if (isLogging())
            LOGGER.debug("{} - latching on monitor StreamEnd for {} {}", JavaRuntime.getQualifiedCallerName(), this.cntTimeout, this.tuTimeout);

        boolean bolReleased = this.monStrmEnd.await(this.cntTimeout, this.tuTimeout);
        
        if ( !bolReleased ) {
            String strMsg = "DpQueryStreamQueueBufferDeprecated#awaitStreamCompleted() - Timeout out waiting onCompleted() response from the Datastore";

            // Log event
            if (isLogging())
                LOGGER.debug(strMsg);
            
            throw new TimeoutException(strMsg);
        }
    }

    /**
     * <p>
     * Request a soft shutdown of an active data streaming process.  
     * </p>
     * <p>
     * If the process is inactive nothing is done, specifically, if the 
     * <code>{@link #start(Request)}</code> has not been called or the 
     * data stream has already completed.
     * In these cases the method returns <code>false</code>.
     * </p>
     * <p>
     * If the streaming process has started then it is allowed to finish.
     * This method <b>will block</b> until all data streaming has finished,
     * or a timeout occurs while waiting for it to finish.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This method is internally synchronized and callers could block if multiple
     * concurrent invocations occur.
     * </p>
     * 
     * @return <code>true</code> if an active data streaming process was shutdown, 
     *         <code>false</code> if the data streaming was already inactive
     */
    public boolean shutdownSoft() throws InterruptedException, TimeoutException {

        synchronized (this.objLock) {

            // If a data stream has not been requested there is nothing to shutdown.
            //  - return false
            if (this.bolStreamRequested == false)
                return false;

            // If a data stream has been requested but we are waiting for it to start, 
            //  - shut it down 
            //  - release the stream start monitor 
            //  - return true
            if (this.monStrmStart.getCount() > 0) {
                this.lstPgsAcquired.clear();
                this.awaitStreamStart();
                //            this.hndSvrStrm.onCompleted();
                //            this.monStrmStart.countDown();
                this.awaitStreamCompleted();

                return true;
            }

            // If the data stream has started but not completed, 
            //  - shut it down 
            //  - release the stream start monitor 
            //  - return true
            if (this.bolStreamCompleted == false) {
                //            this.hndSvrStrm.onCompleted();
                //            this.monStrmStart.countDown();
                this.lstPgsAcquired.clear();
                this.awaitStreamCompleted();

                return true;
            }

            // If the data stream has started and completed there is nothing to do.
            //  - return false
            return false;

        }
    }
    
    /**
     * <p>
     * Perform a hard shutdown of an active data streaming process.  Any active processes
     * are interrupted.
     * </p>
     * <p>
     * If the process is inactive nothing is done, specifically, if the 
     * <code>{@link #start(Request)}</code> has not been called or the 
     * data stream has already completed.
     * In such cases the method returns <code>false</code>.
     * </p>
     * <p>
     * Use this method with caution as it sends an error message to the
     * <em>Datastore</em>.  This will inevitably cause a cascade of errors
     * and exceptions.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This method is internally synchronized and callers could block if multiple
     * concurrent invocations occur.
     * </p>
     * 
     * @return <code>true</code> if an active data streaming process was terminated, 
     *         <code>false</code> if the data streaming was already inactive
     */
    public boolean shutdownNow() {
        
        synchronized (this.objLock) {

            // If a data stream has not been requested there is nothing to shutdown.
            //  - return false
            if (this.bolStreamRequested == false)
                return false;

            // Create the exception to pass to the Datastore
            Throwable src =  new Throwable("User requested a hard shutdown of data streaming");

            // If a data stream has been requested but we are waiting for it to start, 
            //  - force it down 
            //  - release the stream start monitor 
            //  - return true
            if (this.monStrmStart.getCount() > 0) {
                this.hndSvrStrm.onError(src);
                //            this.monStrmStart.countDown();
                //            this.monStrmEnd.countDown();

                return true;
            }

            // If the data stream has started but not completed, 
            //  - force it down 
            //  - release the stream start monitor 
            //  - return true
            if (this.bolStreamCompleted == false) {
                this.hndSvrStrm.onError(src);
                //            this.monStrmStart.countDown();
                //            this.monStrmEnd.countDown();

                return true;
            }

            // If the data stream has started and completed there is nothing to do.
            //  - return false
            return false;
        }
    }
    
    
    //
    // State Query
    //
    
    /**
     * <p>
     * Indicates whether or not the data stream has been initiated with
     * a call to a <code>start</code> method.
     * </p>
     * <p>
     * If the method returns <code>true</code> the above method has been
     * called and the data stream is either
     * <ul>
     * <li>waiting for the <em>Query Service</em> to begin streaming</li>
     * <li>in the active process of streaming</li>
     * <li>finished streaming and now dormant</li>
     * </ul>
     * </p>
     * 
     * @return <code>true</code> if the stream has been initiated according to the above,
     *         <code>false</code> otherwise
     */
    public boolean isStreamInitiated() {
        return this.bolStreamRequested;
    }
    
    /**
     * <p>
     * Returns whether or not the associated data stream is currently active.
     * </p>
     * <p>
     * The returned value is the following combination of state queries:
     * <br/><br/>
     * &nbsp; &nbsp; <code>{@link #isStreamInitiated()} && <code>! {@link #isStreamComplete()}</code>
     * </p>
     * 
     * @return <code>true</code> if the Query Service data stream is active,
     *         <code>false</code> otherwise
     *         
     * @see #isStreamInitiated()
     * @see #isStreamComplete()        
     */
    public boolean isStreamActive() {
        return this.bolStreamRequested && !this.bolStreamCompleted;
    }
    
    /**
     * <p>
     * Returns the streaming completed flag.
     * </p>
     * <p>
     * This flag is always false until the streaming has received all the requested data from the 
     * <em>Query Service</em> according to the request query specified in the
     * call a <code>start</code> method.  
     * (Thus the flag is always <code>false</code> before that call).
     * This condition requires that the data stream has send an 
     * <code>onComplete()</code> to this <code>DpQueryStreamQueueBufferDeprecated</code> instance and
     * that no streaming errors have occurred.
     * </p>
     * 
     * @return <code>true</code> if all data in the original data request query has been acquire,
     *         <code>false</code> otherwise
     */
    public boolean isStreamComplete() {
        return this.bolStreamCompleted;
    }
    
    /**
     * <p>
     * Returns the query request rejected flag.
     * </p>
     * <p>
     * A value of <code>true</code> indicates that the original data request was either invalid
     * (e.g., malformed) or (possibly?) the result set is empty.
     * </p>
     * <p>
     * This flag is only <code>true</code> if and only if the following have occurred:
     * <ul>
     * <li>The data stream was initialized with a <code>start</code> method invocation.</li>
     * <li>The data stream was initiated and the first response was acquired.</li>
     * <li>The first response contained a rejected notification by the Query Service.</li>
     * <li>The stream has been terminated by the stream buffer.</li>
     * </ul>
     * <p>
     *  
     * @return <code>true</code> if the Query Service rejected the original data request,
     *         <code>false</code> otherwise
     */
    public boolean  isRequestRejected() {
        return this.bolRequestRejected;
    }
    
    /**
     * <p>
     * Returns the streaming error flag.  
     * <p>
     * <p>
     * This flag is <code>true</code> only
     * if a stream error has been sent by the <em>Query Service</em> (or gRPC directly)
     * after streaming has been initiated (i.e., after the call to 
     * <code>{@link #startUniStream(QueryRequest)}</code> or <code>{@link #startBidiStream(QueryRequest)}</code>).
     * Thus, a returned value of <code>true</code> indicates that an error 
     * condition has occurred and the stream has been terminated.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * Although the data stream was terminated if a stream error occurred
     * there may exist available data acquired before the interruption.
     * Call method <code>{@link #getBufferSize()}</code> to determine
     * if such data is available.
     * </p>
     *  
     * @return <code>true</code> if an error interrupt was received from the <em>Query Service</em>
     *         <code>false</code> otherwise
     * 
     * @see #getAvailablePageIndices()
     */
    public boolean isStreamError() {
        return this.bolStreamError;
    }
    
    /**
     * <p>
     * Returns the stream error message if present.
     * </p>
     * <p>
     * If a streaming error occurred the error message is recovered from the <em>Query Service</em>
     * (or gRPC).  To determine if an error occurred use method <code>{@link #isStreamError()}</code>.
     * </p>
     * 
     * @return  stream error message, or <code>null</code> if no streaming errors occurred
     * 
     * @see #isStreamError()
     */
    public String getStreamError() {
        return this.strErrMsg;
    }

    
    //
    // Data Query
    //
    
//    /**
//     * <p>
//     * Returns the first page index in the data stream required to fulfill 
//     * for the snapshot data request.
//     * Thus, the index set of the data pages for the request will be
//     * <br/><br/> &nbsp; &nbsp; 
//     * {<i>i</i><sub>1st</sub>, <i>i</i><sub>1st</sub>+1, ..., <i>i<sub>N</sub></i>} 
//     * <br/><br/>
//     * where <i>i</i><sub>1st</sub> is the value returned by this method
//     * and <i>i<sub>N</sub></i> is the value returned by 
//     * <code>{@link #getLastPageIndex()}</code> 
//     * 
//     * <h2>NOTE:</h2>
//     * This method will block until the data streaming starts.  Otherwise the
//     * value returned would be meaningless. 
//     * </p>
//     * 
//     * @return the first page index in the stream for the full snapshot data request  
//     * 
//     * @throws InterruptedException the process was interrupted while waiting for the data stream to start
//     * @throws TimeoutException timed out while waiting for the data stream to start
//     */
//    public Integer getFirstPageIndex() throws InterruptedException, TimeoutException {
//        // Wait for the streaming start monitor to be release. 
//        //  Capture the timeout status.
//        boolean bolReleased = this.monStrmStart.await(this.cntTimeout, TimeUnit.SECONDS);
//        
//        // If the page monitor timed out throw an exception.
//        if (!bolReleased)
//            throw new TimeoutException("Timeout occurred while waiting the data streaming to start.");
//       
//        // If the streaming has started the total page count is available
//        return this.indFirstPage;
//    }
    
//    /**
//     * <p>
//     * Returns the last page index in the data stream required to fulfill 
//     * for the snapshot data request.
//     * Thus, the index set of the data pages for the request will be
//     * <br/><br/> &nbsp; &nbsp; 
//     * {<i>i</i><sub>1st</sub>, <i>i</i><sub>1st</sub>+1, ..., <i>i<sub>N</sub></i>} 
//     * <br/><br/>
//     * where <i>i</i><sub>1st</sub> is the value returned from 
//     * <code>{@link #getFirstPageIndex()}</code> and <i>i<sub>N</sub></i>
//     * is the value returned from this method.
//     * </p>
//     * <p>
//     * <h2>NOTE:</h2>
//     * This method will block until the data streaming starts.  Otherwise the
//     * value returned would be meaningless. 
//     * </p>
//     * 
//     * @return the last page index in the stream for the full snapshot data request  
//     * 
//     * @throws InterruptedException the process was interrupted while waiting for the data stream to start
//     * @throws TimeoutException timed out while waiting for the data stream to start
//     */
//    public Integer getCurrentPageIndex() throws InterruptedException, TimeoutException {
//        // Wait for the streaming start monitor to be release. 
//        //  Capture the timeout status.
//        boolean bolReleased = this.monStrmStart.await(this.cntTimeout, TimeUnit.SECONDS);
//        
//        // If the page monitor timed out throw an exception.
//        if (!bolReleased)
//            throw new TimeoutException("Timeout occurred while waiting the data streaming to start.");
//       
//        // If the streaming has started the total page count is available
//        return this.indLastPage;
//    }
    
//    /**
//     * <p>
//     * Returns the size of each data page in the data stream in rows.  This value
//     * is extracted from the original snapshot data request and is available after
//     * <code>{@link #start(Request)}</code> has been called and returned
//     * (i.e., the data stream has started).  
//     * </p>
//     * <p>Thus, this is essentially
//     * a convenience method since the value is available in the original request.
//     * If the <code>{@link #start(Request)}</code> method has not been called this
//     * method will return <code>null</code>.
//     * </p>
//     * 
//     * @return the size (in rows) of the data pages in the stream,
//     *         or <code>null</code> if the stream has not been initiated
//     */
//    public Integer getPageSize()  {
//        return this.szPage;
//    }
    
//    /**
//     * <p>
//     * Returns the total number of pages required for all the requested data .  
//     * The returned value is currently the value
//     * <br/><br/> &nbsp; &nbsp; 
//     * <code>{@link #getLastPageIndex())}</code> - <code>{@link #getFirstPageIndex()}</code> + 1
//     * <br/><br/>
//     * since the data pages have a 1-based index.
//     * </p>
//     * <p>
//     * <h2>NOTE:</h2>
//     * This method will block until the data streaming starts.  Otherwise the
//     * value returned would be meaningless. 
//     * </p>
//     * 
//     * @return upper bound for total number of rows required for the snapshot data request  
//     * 
//     * @throws InterruptedException the process was interrupted while waiting for the data stream to start
//     * @throws TimeoutException timed out while waiting for the data stream to start
//     */
//    public Integer getPageCount() throws InterruptedException, TimeoutException {
//        return this.getLastPageIndex() - this.getFirstPageIndex() + 1;
//    }
    
    /**
     * <p>
     * Returns the number of data pages received from the Query Service so far.
     * </p>
     * <p>
     * Note that the data buffer size and data queue size may differ if this stream buffer is 
     * being used as a queue.  This number simply reflects the number of pages received so far
     * in the data stream.  If the pages are being consumed from the queue the number of available
     * pages can be smaller.
     * </p> 
     * 
     * @return the total number of data pages streamed to this buffer so far
     */
    public int getPageCount() {
        return this.cntPages;
    }

    
    /**
     * <p>
     * Returns the size of the buffered data pages (in bytes).   
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * This method will block until the data streaming starts.  Otherwise the
     * value returned would be meaningless. 
     * </p>
     * 
     * @return size (in bytes) of a requested data page  
     * 
     * @throws InterruptedException the process was interrupted while waiting for the data stream to start
     * @throws TimeoutException timed out while waiting for the data stream to start
     */
    public Long getPageSize() throws InterruptedException, TimeoutException {
        
        // Wait for the streaming start monitor to be release. 
        //  Capture the timeout status.
        boolean bolReleased = this.monStrmStart.await(this.cntTimeout, TimeUnit.SECONDS);
        
        // If the page monitor timed out throw an exception.
        if (!bolReleased)
            throw new TimeoutException("Timeout occurred while waiting the data streaming to start.");
    
        // If the streaming has started the total page count is available
        return this.szPage;
    }


    //
    // Buffer Operations
    //
    
    /**
     * <p>
     * Returns the current number of data pages in the stream buffer.
     * </p>
     * <p>
     * This method returns the size of the buffer containing data pages returned from the Query Service so far. 
     * Note that this value may change in the future if the data stream is still active.
     * </p>
     *  
     * @return  current size of the stream buffer containing requested data 
     */
    public int  getBufferSize() {
        return this.lstDataBuffer.size();
    }
    
    /**
     * <p>
     * Returns the data page buffer in its current state.
     * </p>
     * <p>
     * Note that the returned object should not be modified if the current stream is still active.
     * Results are unpredictable.
     * </p>
     * 
     * @return  the entire data page buffer in its current state   
     */
    public final List<QueryResponse.QueryReport.QueryData>  getBuffer() {
        return this.lstDataBuffer;
    }
    
    //    /**
//     * <p>
//     * Returns the index set of indices that are currently available for
//     * processing, that is, the indices of the data pages that have been
//     * acquired from the <em>Datastore</em> so far.
//     * 
//     * <h2>NOTEs:</h2>
//     * <ul>
//     * <li>
//     * This method will block until the data streaming starts.  Otherwise the
//     * value returned would be meaningless.
//     * </li><br/>
//     * <li>
//     * The index set of acquired data pages is dynamic.  The value returned will
//     * change with time as the <code>DpQueryStreamQueueBufferDeprecated</code> continues to
//     * acquire data.
//     * </li><br/> 
//     * <li>
//     * This method may be called upon after a stream error event to determine
//     * the set of data pages acquire before stream termination.
//     * </li>
//     * </p>
//     * 
//     * @return the page index set of acquired data at the time of calling  
//     * 
//     * @throws InterruptedException the process was interrupted while waiting for the data stream to start
//     * @throws TimeoutException timed out while waiting for the data stream to start
//     */
//    public IntStream getAvailablePageIndices() throws InterruptedException, TimeoutException {
//
//        // Wait for the streaming start monitor to be release. 
//        //  Capture the timeout status.
//        boolean bolReleased = this.monStrmStart.await(this.cntTimeout, TimeUnit.SECONDS);
//        
//        // If the page monitor timed out throw an exception.
//        if (!bolReleased)
//            throw new TimeoutException("Timeout occurred while waiting the data streaming to start.");
//       
//        return IntStream.range(0, this.lstRspBuffer.size());
//    }
    
    /**
     * <p>
     * Returns the given data page from the data stream buffer.
     * <p>
     * <p>
     * This method is available when using the stream buffer as a buffer, that is, when not 
     * using it as a queue.  Queue operations consume the data pages as they become available.
     * </p>
     * <p>
     * <h2>NOTEs:</h2>
     * <ul>
     * <li>
     * The provided index must be within the index of the stream buffer (which is dynamic).  
     * Otherwise an <code>IndexOutOfBoundsException</code> is thrown.
     * </li>
     * <br/> 
     * <li>
     * Another option is to register a <code>{@link IDataStreamObserver}</code>
     * callback using the method <code>{@link #addStreamObserver(IQueryStreamQueueBufferObserver)}</code>.
     * Any callback registered here will be notified whenever a data page
     * becomes available.
     * </li>
     * </ul>
     * </p>
     *  
     * @param indPage   index of data page requested  
     * 
     * @return the <em>Datastore</em> gRPC response message containing page data
     * 
     * throws InterruptedException the blocking process was interrupted while waiting for data page  
     * throws TimeoutException  the page monitor timed out while waiting for the data page to become available
     * @throws IndexOutOfBoundsException the given index is not in the index set of data pages
     */
    public QueryResponse.QueryReport.QueryData getBufferPage(Integer indPage) throws /* InterruptedException, TimeoutException, */ IndexOutOfBoundsException {
        
        return this.lstDataBuffer.get(indPage);
        
//        // Check that the index is in bounds
//        if (this.mapPgMons.get(indPage) == null)
//            throw new IndexOutOfBoundsException("Index " + indPage+ " for requested page is out of bounds");
//        
//        // Wait for the page monitor to be release (if needed). 
//        //  Capture the timeout status.
//        boolean bolReleased = this.mapPgMons.get(indPage).await(this.cntTimeout, TimeUnit.SECONDS);
//        
//        // If the page monitor timed out throw an exception.
//        if (!bolReleased)
//            throw new TimeoutException("Timeout occurred while waiting for data page " + indPage);
//        
//        // The data page is available - return it.
//        return this.mapRspBuffer.get(indPage);
    }
    
    
    // 
    // Queue Operations
    //
    
    /**
     * <p>
     * Returns the number of available data pages when using this stream buffer as a data page queue.
     * </p>
     * 
     * @return number of data pages ready for processing within the queue buffer 
     */
    public int getQueueSize() {
        return this.semPageRdy.availablePermits();
    }


    /**
     * <p>
     * Removes and returns the data page at the queue head, blocking indefinitely until one becomes available.
     * </p>
     * <p>
     * When using this buffer as a blocking queue:
     * <ul>
     * <li>The method blocks indefinitely until a data page is ready for consumption.</li>
     * <li>Competing threads will receive data pages in order of invocation.</li>
     * <li>If the stream completes or terminates unexpectedly the method continues to block.</li>
     * <li>It is the responsibility of the caller to handle exceptions in the stream.</li>
     * </ul>
     * </p>
     * 
     * @return the data page at the head of the queue buffer
     * 
     * @throws InterruptedException if the current thread is interrupted
     */
    public QueryResponse.QueryReport.QueryData removeQueuePage() throws InterruptedException {
        
        // Block (indefinitely) until a data page becomes available
        this.semPageRdy.acquire();
        
        // Remove and return the data page at the queue head
        return this.lstDataBuffer.remove();
    }
    
    /**
     * <p>
     * Removes and returns the data page at the queue head, blocking until page becomes available or timeout occurs.
     * </p>
     * <p>
     * When using this buffer as a blocking queue:
     * <ul>
     * <li>The method blocks until a data page is ready for consumption, or a timeout occurs.</li>
     * <li>Competing threads will receive data pages in order of invocation.</li>
     * <li>If the stream completes or terminates unexpectedly the method continues to block until timeout occurs.</li>
     * <li>It is the responsibility of the caller to handle exceptions in the stream.</li>
     * </ul>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Timeout parameter are taken as those provided at stream buffer construction.
     * </p>
     * 
     * @return the data page at the head of the queue buffer
     * 
     * @throws TimeoutException     no data page available within the timeout limit
     * @throws InterruptedException if the current thread is interrupted
     */
    public QueryResponse.QueryReport.QueryData removeQueuePageTimeout() throws TimeoutException, InterruptedException {
        
        // Block (with timeout) until a data page becomes available
        boolean bolResult = this.semPageRdy.tryAcquire(this.cntTimeout, this.tuTimeout);
        
        // Check if timeout occurred 
        if (!bolResult) {
            String strMsg = JavaRuntime.getQualifiedCallerName() + ": timeout occured after " + this.cntTimeout + " " + this.tuTimeout.name();
            
            // Log event
            if (isLogging())
                LOGGER.error(strMsg);
            
            throw new TimeoutException(strMsg);
        }
        
        // Remove and return the data page at the queue head
        return this.lstDataBuffer.remove();
    }
    
    
    //
    // StreamObserver<QueryResponse> Interface
    //
    
    /**
     * <p>
     * Handles incoming data from Query Service.
     * </p>
     * <p>
     * Buffers the data from the incoming <em>Query Service</em> stream response.
     * If this is a bidirectional stream then the next page RPC query message is sent to the forward 
     * stream handle.
     * </p>  
     * <p>
     * <h2>On First Invocation</h2>
     * Upon first response from the <em>Query Service</em> then the following operations are performed:
     * <ul>
     * <li>The streaming started monitor is released.</li>
     * <li>Streaming state variables are initialized.</li>
     * </ul>
     * </p>
     * 
     * @param rspRsp gRPC response message from the QueryService containing data
     * 
     * @see #awaitStreamStart()
     * @see StreamObserver#onNext(Object)
     */
    @Override
    public void onNext(QueryResponse msgRsp) {

        // Debugging
        if (isLogging()) {
            LOGGER.debug("{} called for monStrmStart = {}", JavaRuntime.getQualifiedCallerName(), this.monStrmStart);
        }
        
        // Check for Initial Response - If the stream start monitor is still latched this is the first time through
        //  - initialize the state variables 
        //  - release the 'stream start' monitor
        //  - notify observers that stream has started
        if (this.monStrmStart.getCount() > 0) {
            
            if (isLogging())
                LOGGER.debug("  doing onNext() first call operations...");
            
            // Initialize the state variables
//            this.indFirstPage = 0;
//            this.indNextPage = 0;
            this.cntPages = 0;
            this.szPage = Integer.toUnsignedLong( msgRsp.getSerializedSize() );

            // Set up the request queue and create the page monitors 
//            for (int i=this.indFirstPage; i<=this.indLastPage; i++) {
//                this.lstRequestQueue.add(i);
//                this.mapPgMons.put(i, new CountDownLatch(1));
//            }
//            this.lstRequestQueue.removeFirst();
            
            // Debugging
            if (isLogging())
                LOGGER.debug("{} releasing monitor StreamStart {}", JavaRuntime.getQualifiedCallerName(), this.monStrmStart);

            // Release the stream start monitor
            this.monStrmStart.countDown();
            
            // Notify observers
            this.notifyStart();
        }
        
        // Query Failure Check - if this is not a data message something is wrong
        //  NOTE: This should happen only on the first request response
        //  - Log the event
        //  - Shut down the stream if bidirectional
        //  - release the 'stream end' monitor
        //  - notify all stream observers
        //  - return (there is nothing else we can do)
//        if (msgRsp.getResponseType() != ResponseType.DETAIL_RESPONSE) {
        if (msgRsp.hasQueryReject()) {
            
            // Get the details of the rejection and report them
            RejectDetails   msgReject = msgRsp.getQueryReject();
            RejectReason    enmCause = msgReject.getRejectReason();
            String          strMsg = msgReject.getMessage();
            
            if (isLogging())
                LOGGER.error("{}: Query Service rejected request - {}, {}", JavaRuntime.getQualifiedCallerName(), enmCause.name(), strMsg);
            
            // Shut down the stream
            //  - If this is a bidirectional stream send the completed notification
            if(this.bolStreamBidi) 
                this.hndSvrStrm.onCompleted();
               
            //  - State associated state condition and variables
            this.bolRequestRejected = true;
            this.monStrmEnd.countDown();
            
            // Notify all observers
            this.notifyRejected(msgReject);
            
            return;
        }

        // Normal operation - Save the returned data page and request next page if bidirectional
        //  - the data is stored in the data buffer
        //  - all callback functions are notified that the page is available
        this.storePage(msgRsp.getQueryReport().getQueryData());

        // Request next page 
        //  This is active only for bidirectional streams
        this.requestNextPageBidi();
    }

    /**
     * <p>
     * Receives a terminating error from the <em>Query Service</em> concerning current data stream.
     * </p>
     * <p>
     * The data stream has stopped upon some error condition, hopefully described
     * in the given <code>Throwable</code> instance.
     * Note that there may still be data available in the in data buffers
     * which was acquired before the streaming error.
     * However, no more data messages will be received from the <em>Query Service</em> data stream. 
     * </p>
     * <p>
     * <ul>
     * <li>The error condition flag is set to <code>true</code>.</li>
     * <li>Any observers functions requesting error notification are called.</li>
     * <li>The stream start and stream end monitors are released.</li>
     * </ul>
     * </p>
     * 
     * @param   e   the error thrown by gRPC stream or Data Platform Query Service
     * 
     * @see #isStreamError()
     * @see StreamObserver#onError(Throwable)
     */
    @Override
    public void onError(Throwable e) {

        String strErrMsg = JavaRuntime.getQualifiedCallerName() + " called by gRPC or Query Service, exception message = " + e.getMessage();

        // TODO - remove
        System.err.println(strErrMsg);
        
        // Log event
        if (isLogging())
            LOGGER.error(strErrMsg);
        
        this.bolStreamError = true;
        this.strErrMsg = e.getMessage();
        this.monStrmStart.countDown();
        this.monStrmEnd.countDown();
        
        this.notifyError(strErrMsg, e);
    }

    /**
     * <p>
     * Receives a stream completed notification from the Query Service.
     * <p>
     * Message from the <em>Query Service</em> indicating that streaming has completed.
     * This message indicates that all requested data has been sent under normal operation.
     * </p>
     * <p>  
     * Performs any notifications to data consumers that the streaming process has 
     * completed and all data is available.  Releases the <code>{@link #monStrmEnd}</code>
     * monitor.
     * </p>
     * 
     * @see DpQueryStreamQueueBufferDeprecated#awaitStreamCompleted()
     * @see StreamObserver#onCompleted()
     */
    @Override
    public void onCompleted() {

        // Log event
        if (isLogging())
            LOGGER.info("{}: Stream completed with {} data page(s) received.", JavaRuntime.getCallerName(), this.getPageCount());

        // Update state variables and conditions
        this.bolStreamCompleted = true;
        this.monStrmEnd.countDown();
        
        // Notify all observers
        this.notifyCompleted();
    }

    
    //
    // Private Methods
    //
    
    /**
     * <p>
     * Initiates the data stream by performing the initial data request on the <em>Query Service</em>.
     * </p>
     * <p> 
     * Once this method is called the gRPC data stream is initiated, either unidirectional or bidirectional.
     * <ul>
     * <li>
     * For unidirectional streams the appropriate RPC operation is invoked on the communications stub which
     * both opens the data stream and initiates the streaming operations.  
     * </li>
     * <br/>
     * <li>
     * For bidirectional gRPC streaming the stream must first be created with the appropriate RPC operation, 
     * which returns the handle to the forward stream to the <em>Query Service</em> stored in
     * <code>{@link #hndSvrStrm}</code>. 
     * Then the stream is initiated by sending the query request with <code>{@link #onNext(QueryResponse)}</code>
     * on <code>{@link #hndSvrStrm}</code>
     * </li>
     * </ul> 
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * The given request must contain a <code>{@link QuerySpec}</code> message containing the data request.
     * Otherwise an exception is thrown.
     * </p>
     *
     * @param msgRqst   the gRPC data request message to send to the <em>Query Service</em>
     * 
     * @throws IllegalArgumentException gRPC message does not contain a <code>QuerySpec</code> message
     */
//    @Deprecated(since="Jan 8, 2024", forRemoval=true)
    private void requestDataStream(QueryRequest msgRqst) throws IllegalArgumentException {

        // Check argument for required data request
        if (!msgRqst.hasQuerySpec()) {
            String  strMsg = JavaRuntime.getQualifiedCallerName() + ": argument does not contain initial query.";
            if (isLogging())
                LOGGER.error(strMsg);

            throw new IllegalArgumentException(strMsg);
        }

        // Initialize the next data page indexing
        this.indNextPage = 0;
        this.lstPgsRequested.add(this.indNextPage);
        
        
        // If this is a bidirectional stream
        if (this.bolStreamBidi) {
            
            // Get the forward stream handle
            this.hndSvrStrm = (CallStreamObserver<QueryRequest>) this.stubAsync.queryResponseCursor(this);

            // Send the first data request initiating the stream
            this.hndSvrStrm.onNext(msgRqst);

        // Otherwise a unidirectional stream
        } else {
            
            // Send the data 
            this.stubAsync.queryResponseStream(msgRqst, this);
        }
        
        // Set the active request flag and return
        this.bolStreamRequested = true;
    }

    /**
     * Adds the given gRPC data response to the buffer of stored data pages.
     * 
     * @param msgRspData    gRPC response message containing data (the data page)
     */
    private void storePage(QueryResponse.QueryReport.QueryData msgRspData) {

//        this.indLastPage = this.indLastPage + 1;
//        
//        Integer indPage = this.indLastPage;
//        this.mapRspBuffer.put(indPage, msgRspData);
//        this.mapPgMons.get(indPage).countDown();
        
//        this.lstStreamObservers.forEach( o -> o.notifyDataPageReady(indPage, msgRspData) );
        
        Integer indPage = this.cntPages;
        
        this.lstDataBuffer.add(msgRspData);
        this.lstPgsAcquired.add(indPage);
        this.semPageRdy.release();
        
        this.cntPages = this.cntPages + 1;
        
        this.notifyPageReady(indPage, msgRspData);
    }
    
    /**
     * <p>
     * Requests the that the next data page be send from the <em>Query Service</em> 
     * after the stream has been started.
     * </p>
     * <p>
     * This method is only active for bidirectional streams.  If a unidirectional stream was 
     * initiated the method simply returns.
     * </p>
     * <p>
     * This method is used after the initial data request has been transmitted in a bidirectional stream.
     * The initial request to the <em>Query Service</em> stream (i.e., on {@link #hndSvrStrm})
     * contains the actual data query.
     * All following requests (generated here) contain only a "next page" request.
     * </p>
     * 
     */
    private void requestNextPageBidi() {

        // If this is not a bidirectional stream there is nothing to do
        if (!this.bolStreamBidi)
            return;

        // Increment next page then add to request list  
        this.indNextPage = this.indNextPage + 1;
        this.lstPgsRequested.add(this.indNextPage);

        // Create the next page cursor request and send it
        QueryRequest msgPgRqst = DpQueryStreamQueueBufferDeprecated.createPageRequest();

        this.hndSvrStrm.onNext(msgPgRqst);
    }

    
    //
    // IDataStreamObserver Notifications
    //
    
    /**
     * <p>
     * Notify all observers that stream has started.
     * </p>
     */
    private void notifyStart() {
        this.lstStreamObservers.forEach( o -> o.notifyStreamingStarted(this) );
    }
    
    /**
     * <p>
     * Notifies all stream observers that original data request was rejected by the Query Service.
     * </p>
     *  
     * @param msgReject request rejection details provided by the Query Service
     */
    private void notifyRejected(RejectDetails msgReject) {
        this.lstStreamObservers.forEach( o -> o.notifyRequestRejected(msgReject));
    }
    
    /**
     * <p>
     * Notify all observers that stream has completed.
     * </p>
     */
    private void notifyCompleted() {
        this.lstStreamObservers.forEach(o -> o.notifyStreamingCompleted(this));
    }
    
    /**
     * <p>
     * Notify all observers that the given data page is available.
     * </p>
     * 
     * @param indPage       page index within stream buffer
     * @param msgRspData    gRPC message containing page of requested data
     */
    private void notifyPageReady(Integer indPage, QueryResponse.QueryReport.QueryData msgRspData) {
        this.lstStreamObservers.forEach( o -> o.notifyDataPageReady(indPage, msgRspData));
    }
    
    /**
     * <p>
     * Notify all observers of a streaming error.
     * </p>
     * 
     * @param strMsg any error message send by the data stream
     * @param eSrc the source of the streaming error
     */
    private void notifyError(String strMsg, Throwable eSrc) {
        this.lstStreamObservers.forEach( o -> o.notifyStreamingError(strMsg, eSrc) );
    }
    
    
    // 
    // Utility Methods
    //
    
    /**
     * <p>
     * Builds a <code>QueryRequest</code> message requesting the next data page in bidirectional streaming.
     * </p>
     * <p>
     * This is used in bidirectional streaming after the data request has been sent in the 
     * initial request message.
     * The <code>CursorOperation</code> field of the request union is set, requesting an 
     * additional data page via database cursor movement.
     * </p>
     * 
     * @return  a <code>QueryRequest</code> encapsulating the next data page request 
     */
    private static QueryRequest createPageRequest() {
        QueryRequest.Builder bldr = QueryRequest.newBuilder();
        
        bldr.setCursorOp(CursorOperation.CURSOR_OP_NEXT);
        
        QueryRequest    rqstPage = bldr.build();
        
        return rqstPage;
    }
    
    /**
     * <p>
     * Returns whether or not logging is active.
     * </p>
     * <p>
     * Currently returns the logging active parameter in the application default parameters.
     * </p>
     * 
     * @return <code>true</code> if logging is active, <code>false</code> otherwise
     * 
     * @see DpApiConfig.query.logging.active
     */
    private static boolean isLogging() {
        return CFG_DEFAULT.logging.active;
    }

    
    //
    // Object Overrides
    //

    /**
     *
     * @see @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer    buf = new StringBuffer(JavaRuntime.getCallerClass() + "\n");
        
        // Write out contents
        buf.append("  List of page requests: " + this.lstPgsRequested + "\n");
        buf.append("  List of page responses: " + this.lstPgsAcquired + "\n");
        buf.append("  Next Page index: " + this.indNextPage + "\n");
        buf.append("  Page count: " + this.cntPages + "\n");
        buf.append("  Page size (bytes): " + this.szPage + "\n");
        buf.append("  BIDI stream flag: " + this.bolStreamBidi + "\n");
        buf.append("  Request Rejected flag: " + this.bolRequestRejected + "\n");
        buf.append("  Stream Requested flag: " + this.bolStreamRequested + "\n");
        buf.append("  Stream Completed flag: " + this.bolStreamCompleted + "\n");
        buf.append("  Stream Error flag: " + this.bolStreamError + "\n");
        buf.append("  Query Service error message: " + this.strErrMsg + "\n");
        
        return buf.toString();
    }
}
