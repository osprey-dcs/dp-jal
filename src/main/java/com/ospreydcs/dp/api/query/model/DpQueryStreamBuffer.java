/*
 * Project: dp-api-common
 * File:	DpQueryStreamBuffer.java
 * Package: com.ospreydcs.dp.api.query.model
 * Type: 	DpQueryStreamBuffer
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
 * @since Jan 14, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.RejectDetails;
import com.ospreydcs.dp.grpc.v1.common.RejectDetails.RejectReason;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest.CursorOperation;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest.QuerySpec;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

import io.grpc.stub.CallStreamObserver;
import io.grpc.stub.StreamObserver;

/**
 * <p>
 * A stream buffer and blocking queue for Query Service gRPC data streams, also provides notification services.
 * </p>
 * <p>
 * Class instances are used to accumulate query request result sets.  The can be used as either a data buffer, a
 * blocking queue, and/or a callback notification server. 
 * <p>
 * This class performs the actual data query from the <em>Query Service</em>.  
 * It receives the data request during construction.  The request is sent to the Query service when invoking 
 * the <code>start</code> method, which initiates the data streaming from the <em>Query Service</em>.  
 * The data is received from the <em>Query Service</em> in pages and saved into an internal buffer for later 
 * processing by the client.  
 * </p>
 * <p>
 * <h2>gRPC Streaming</h2>
 * The type of gRPC data stream employed is determined by the value of <code>{@link #enmStreamType}</code> set
 * during construction (see <code>{@link StreamType}</code>).
 * The <code>{@link StreamType#UNIDIRECTIONAL}</code> value creates a unidirectional stream while the 
 * <code>{@link StreamType#BIDIRECTIONAL}</code> value creates a bidirectional stream.
 * The <code>DpQueryStreamBuffer</code> instance itself receives the responses through the implemented methods of the 
 * <code>StreamObserver</code> interface (that is, the instance itself acts as the backward stream from the 
 * Query Service).
 * </p>
 * <p>
 * <h4>Unidirectional Streaming</h4>  
 * For unidirectional streaming the <em>Query Service</em> simply transmits data pages at its
 * maximum rate.
 * <h4>Bidirectional Streaming</h4>  
 * For bidirectional streaming a handle to the <em>Query Service</em> forward stream ({@link #hndSvrStrm}) is 
 * created from the given gRPC non-blocking service stub.  This is the handle by which we send the data page 
 * requests.  That is, in bidirectional stream each data page is requested explicitly.  This may be slower
 * but it is more reliable.
 * </p> 
 * <p>
 * <h2>Usage</h2>
 * A <code>DpQueryStreamBuffer</code> object can be used as a <em>stream buffer</em> collecting data pages 
 * as they become available, or a <em>blocking queue</em> allowing clients to consume data pages as they become 
 * available.  Alternatively, it can be used as a <em>callback server</em> to notify 
 * <code>{@link IDataStreamObserver}</code> instances of incoming data and events produced by the data stream.  
 * Use the <code>{@link #addStreamObserver(IDpQueryStreamObserver)}</code> method for callback registration 
 * before invoking <code>{@link #start()}</code>.
 * </p>
 * <h4>Stream Buffer</h4>  
 * Use the <code>{@link #getBufferSize()}</code> method to determine the current buffer size.
 * Use the <code>{@link #getBufferPage(Integer)}</code> method to retrieve data
 * pages of a particular index.  
 * </p>
 * <p>
 * <h4>Blocking Queue</h4>
 * Use the <code>{@link #getQueueSize()}</code> method to determine the current size of the queue buffer.
 * Use the <code>{@link #removeQueuePage()}</code> method to retrieve data pages from the queue as they become 
 * available.  The latter methods will block until a data page becomes available or return immediate if one is 
 * already available.
 * </p>
 * <p>
 * <h4>Callback Server</h4>
 * Alternative to stream buffer or queue usage, 
 * one can register for callback notifications using 
 * <code>{@link #addStreamObserver(IQueryStreamQueueBufferObserverDeprecated)}</code>
 * method which will be invoked whenever a new data page becomes available.
 * The the gRPC message response containing the data is provided to the callback methods.  The 
 * <code>{@link IDataStreamObserver}</code> interface also contains notifications for streaming events, 
 * for example start, completed, rejected, and error notifications.
 * </p> 
 * <p>
 * <h2>NOTES:</h2>
 * Active class objects are not explicitly a separate Java thread process,
 * although it behaves much like one.  The actual thread is realized as the 
 * <em>Query Service</em> backward data stream. The class serves as a receiver (backward stream) of
 * <em>Query Service</em> data messages and, thus, responds to them as they
 * occur acting much like an independent process thread.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 14, 2024
 *
 */
public class DpQueryStreamBuffer implements StreamObserver<QueryResponse> {

    
    //
    // Class Types
    //
    
    /**
     * Enumeration of gRPC data streaming services currently supported by the Query Service.
     */
    public static enum StreamType {
        
        /**
         * Unidirectional gRPC Stream.
         * <p>
         * Indicates a unidirectional (backward) stream from the Query Service to this client.
         */
        UNIDIRECTIONAL,
        
        
        /**
         * Bidirectional gRPC Stream.
         * <p>
         * Indicates a fully bidirectional stream between the Client and the Query Service.
         * Client explicitly requests each data page (within QueryResponse message) from the
         * Query Service. 
         */
        BIDIRECTIONAL;
    }
    
    
    //
    // Class Resources
    //
    
    /** Internal event logger */
    private static final Logger LOGGER = LogManager.getLogger(); 
    
    
    //
    // Initialization Targets
    //
    
    /** The gRPC data stream type this response buffer is managing */
    private final StreamType            enmStreamType;
    
    /** The (asynchronous) streaming communications stub of the Query Service gRPC interface */
    private final DpQueryServiceStub    stubAsync;
    
    /** The Query Service data request message */
    private final QueryRequest          msgRequest;
    
    /** Toggle events logging (for debugging) */
    private final boolean               bolLogging;
    
    
    //
    // Client Data Consumers
    //
    
    /** List of data consumers to be notified about streaming data and conditions */
    private List<IDpQueryStreamObserver>   lstStreamObservers = new LinkedList<>();
    
    
    //
    // Synchronization Resources 
    //

    /** Synchronization locking object - CANNOT synchronize on class instance because of StreamObserver implementation */
    private final Object    objLock = new Object();

    
    //
    // Streaming Data Resources 
    //

    /** The data page buffer, also used as a queue buffer - contains all the data responses from the Query Service */
    private final LinkedList<QueryResponse> lstBufferRsps = new LinkedList<>();


    //
    // Streaming State Variables - Event Monitors and Semaphores
    //
    
    /** First response monitor from <em>Query Service</em> - also used as a state variable, needed for request rejection */
    private final CountDownLatch monStrmStart = new CountDownLatch(1);
    
    /** The stream end monitor - released when the <code>onCompleted</code> signal is received */
    private final CountDownLatch monStrmComplete = new CountDownLatch(1);
    
    /** Data page available semaphore  */
    private final Semaphore     semPageRdy = new Semaphore(0, true);
    
    
    //
    // BIDI Streaming Process Resources 
    //

    /** Service stream handle for forward stream (to server) used to send cursor requests - BIDI streaming */
    private CallStreamObserver<QueryRequest>    hndSvrStrm = null;
    
    /** Index of the data last page requested - used by {@link #requestNextPageBidi()} */
    private Integer                             indNextPage = null;
    
    /** Size (in bytes) of the first data message - initialized by {@link #onNext(PaginatedResponse)} */
    private Long                                szPageBytes = null;


    /** List of page indices requested - BIDI streaming (debugging) */
    private final List<Integer>                 lstIndPageRequested = new LinkedList<Integer>();
    
    /** List of data page indices acquired so far (debugging) */
    private final LinkedList<Integer>           lstIndPageAcquired = new LinkedList<Integer>();
    

    //
    // Streaming Condition Variables 
    //
    
    /** Indicates that a data stream has been requested, i.e., a start() method has been called */
    private boolean bolStreamRequested = false;
    
    /** Query request rejected */
    private boolean bolRequestRejected = false;
    
    /** Streaming error flag */
    private boolean bolStreamError = false;
    
    /** Any error message from the Query Service - from {@link #onError(Throwable)} exception message */
    private String  strErrMsg = null;

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new, initialized instance of <code>DpQueryStreamBuffer</code> ready for use.
     * </p>
     * <p>
     * All internal logging for the returned instance is turned off.
     * </p>
     * 
     * @param enmStreamType the gRPC data stream type this response buffer is managing 
     * @param stubAsync     the streaming communications stub of the Query Service gRPC interface 
     * @param msgRequest    the Query Service data request message 
     * 
     * @return new <code>DpQueryStreamBuffer</code> initialized with the given arguments.
     * 
     * @see #DpQueryStreamBuffer(StreamType, DpQueryServiceStub, QueryRequest, boolean)
     * @see DpQueryStreamBuffer
     */
    public static DpQueryStreamBuffer newBuffer(StreamType enmType, DpQueryServiceStub stubAsync, QueryRequest msgRequest) {
        return DpQueryStreamBuffer.newBuffer(enmType, stubAsync, msgRequest, false);
    }
    
    /**
     * <p>
     * Creates a new, initialized instance of <code>DpQueryStreamBuffer</code> ready for use.
     * </p>
     * 
     * @param enmStreamType the gRPC data stream type this response buffer is managing 
     * @param stubAsync     the streaming communications stub of the Query Service gRPC interface 
     * @param msgRequest    the Query Service data request message 
     * @param bolLogging    set <code>true</code> to include event logging (for debugging)
     * 
     * @return new <code>DpQueryStreamBuffer</code> initialized with the given arguments.
     * 
     * @see #DpQueryStreamBuffer(StreamType, DpQueryServiceStub, QueryRequest, boolean)
     * @see DpQueryStreamBuffer
     */
    public static DpQueryStreamBuffer newBuffer(StreamType enmType, DpQueryServiceStub stubAsync, QueryRequest msgRequest, boolean bolLogging) {
        return new DpQueryStreamBuffer(enmType, stubAsync, msgRequest, bolLogging);
    }
    
    
    //
    // Construction and Initialization
    //
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>DpQueryStreamBuffer</code>.
     * </p>
     * <p>
     * Once constructed, this stream buffer instance is ready for Query Service connection 
     * and data streaming.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Clients initialize the data stream using the <code>start()</code> method.
     * </li>
     * <li>
     * Before starting the data stream clients can observe streaming process by
     * registering <code>{@link IDpQueryStreamObserver}</code> with this stream buffer.
     * </li>
     * <li>
     * The <code>awaitStreamComplete()</code> method allows clients to block until all
     * Query Service results set data is recovered into the stream buffer.
     * </li>
     * </ul>
     * </p>
     *
     * @param enmStreamType the gRPC data stream type this response buffer is managing 
     * @param stubAsync     the streaming communications stub of the Query Service gRPC interface 
     * @param msgRequest    the Query Service data request message 
     * @param bolLogging    set <code>true</code> to include event logging (for debugging)
     * 
     * @see DpQueryStreamBuffer
     */
    public DpQueryStreamBuffer(StreamType enmStreamType, DpQueryServiceStub stubAsync, QueryRequest msgRequest, boolean bolLogging) 
    {
        this.enmStreamType = enmStreamType;
        this.stubAsync = stubAsync;
        this.msgRequest = msgRequest;
        this.bolLogging = bolLogging;
    }

    /**
     * <p>
     * Register for gRPC streaming events notifications. 
     * </p>
     * <p>
     * Adds the given <code>{@link IDpQueryStreamObserver}</code> interface to the
     * list of targets requesting notifications about streaming
     * conditions and stream data.
     * </p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The implementations within the argument should not incur significant processing time.
     * </li>
     * <br/>
     * <li>
     * Notifications should be accepted and returned as soon as possible so as not to interrupt 
     * the ongoing data stream.  If the notification requires significant execution time or CPU 
     * resources a separate processing thread should be spawned and the method returned.
     * </li>
     * </ul>
     * </p>
     * 
     * @param ifcStreamObserver target receiving notification about streaming events
     * 
     * @see IDpQueryStreamObserver
     */
    public void addStreamObserver(IDpQueryStreamObserver ifcStreamObserver) {
        this.lstStreamObservers.add(ifcStreamObserver);
    }


    //
    // Operations
    //
    
    /**
     * <p>
     * Initiates the <em>Query Service</em> data request.  
     * </p>
     * <p>
     * The given request is sent to the <em>Query Service</em> with a streaming
     * RPC operation.  The call initiates the data stream and incoming data is collected within
     * the buffer.  The type of data stream, either unidirectional or bidirectional, is determined
     * by the construction parameter <code>{@link #enmStreamType}</code>.
     * </p>
     * <p>
     * The Query Service unidirectional streaming request initiates the gRPC data stream after which
     * the Query Service streams requested data at arbitrary rate.  It is up to the client to process
     * data in a timely fashion.  Bidirectional streaming provides a more synchronized data path for
     * clients with excessive processing requirements (see <code>{@link StreamType#BIDIRECTIONAL}</code>).
     * <p>
     * After this method returns the streaming process becomes
     * independent and thread-like.  The requested data becomes available
     * at it arrives from the <em>Query Service</em>, and may take some time
     * after the return of this method depending upon the size of the data
     * request.
     * </p>
     * <p>
     * Notifications about stream activities is sent to any registered query stream observers
     * (see <code>{@link #addStreamObserver(IQueryStreamQueueBufferObserverDeprecated)}</code>).  Query stream observers 
     * should spawn independent threads to process incoming data.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This method is internally synchronized and callers will block if multiple
     * concurrent invocations occur.
     * </p>
     * 
     * @throws IllegalStateException    gRPC stream has already been started
     * @throws IllegalArgumentException initializing gRPC message does not contain a <code>QuerySpec</code> message
     */
    public void start() throws IllegalStateException, IllegalArgumentException {

        synchronized (this.objLock) {
            
            // Check if stream has already been requested
            if (this.bolStreamRequested) {
                String strMsg = JavaRuntime.getQualifiedCallerName() + ": ERROR - Attempted to start a stream that has already been requested.";
                if (this.bolLogging)
                    LOGGER.error(strMsg);

                throw new IllegalStateException(strMsg);
            }

            // Log event
            if (this.bolLogging) {
                LOGGER.debug("{} called with following request:", JavaRuntime.getQualifiedCallerName());
                LOGGER.debug(this.msgRequest.toString());
            }


            // Perform the initial streaming gRPC operation initiating the data stream 
            this.requestDataStream(this.msgRequest);
        }
    }
    
    /**
     * <p>
     * Initiates the <em>Query Service</em> data request then blocks until complete.
     * </p>
     * This is a convenience method simply composed of the consecutive invocations <code>{@link #start()}</code>
     * followed by <code>{@link #awaitStreamCompleted()}</code>.  See documentation on those methods for
     * further information.
     * </p>
     * 
     * @throws IllegalStateException    gRPC stream has already been started
     * @throws IllegalArgumentException initializing gRPC message does not contain a <code>QuerySpec</code> message
     * @throws InterruptedException     an interruption occurred while waiting for stream completion
     * 
     * @see #start()
     * @see #awaitStreamCompleted()
     */
    public void startAndAwaitCompletion() throws IllegalStateException, IllegalArgumentException, InterruptedException {
        
        this.start();
        
        this.awaitStreamCompleted();
    }
    
    /**
     * <p>
     * Initiates the <em>Query Service</em> data request then blocks until complete.
     * </p>
     * This is a convenience method simply composed of the consecutive invocations <code>{@link #start()}</code>
     * followed by <code>{@link #awaitStreamCompleted(long, TimeUnit)}</code>.  See documentation on those methods for
     * further information.
     * </p>
     * 
     * @param   lngTimeout  duration of time to wait for stream completion event
     * @param   tuTimeout   the time units of the timeout parameter (e.g., MILLISECONDS, SECONDS, etc.)
     * 
     * @throws IllegalStateException    gRPC stream has already been started
     * @throws IllegalArgumentException initializing gRPC message does not contain a <code>QuerySpec</code> message
     * @throws TimeoutException         timeout for the response expired
     * @throws InterruptedException     an process interruption occurred while waiting for stream completion
     * 
     * @see #start()
     * @see #awaitStreamCompleted(long, TimeUnit)
     */
    public void startAndAwaitCompletion(long lngTimeout, TimeUnit tuTimeout) throws IllegalStateException, IllegalArgumentException, TimeoutException, InterruptedException {
        
        this.start();
        
        this.awaitStreamCompleted(lngTimeout, tuTimeout);
    }
    
    /**
     * <p>
     * Waits (blocks) indefinitely until the start of the gRPC data stream from the Query Service
     * or the process is interrupted.
     * </p>
     * <p>
     * Waits for the first response from the <em>Query Service</em> by latching
     * onto the stream start monitor (<code>{@link #monStrmStart}</code>).  
     * The monitor is released during the first Query Service call to the 
     * <code>{@link #onNext(QueryResponse)} method.  
     * <p>
     * <p>
     * It is assumed that either <code>{@link #start()}</code> has previously been called.
     * Upon return from this method the stream buffer begins acquiring the incoming data.
     * </p>
     *  
     * @throws IllegalStateException    gRPC stream has not been started (see {@link #start()})
     * @throws InterruptedException     an interruption occurred while waiting for stream start
     * 
     * @see #start()
     */
    public void awaitStreamStart() throws IllegalStateException, InterruptedException {
    
        // Check if a stream has been requested
        if (!this.bolStreamRequested) {
            String strMsg = JavaRuntime.getQualifiedCallerName() + ": ERROR - Attempted to wait on a stream that has not been requested.";
            if (this.bolLogging)
                LOGGER.error(strMsg);

            throw new IllegalStateException(strMsg);
        }

        // Log event
        if (this.bolLogging)
            LOGGER.debug("{} - latching on monitor StreamStart indefinitely.", JavaRuntime.getQualifiedCallerName());
    
        this.monStrmStart.await();
    }

    /**
     * <p>
     * Waits (blocks) until the start of the gRPC data stream from the Query Service
     * or a timeout occurs.
     * </p>
     * <p>
     * Waits for the first response from the <em>Query Service</em> by latching
     * onto the stream completed monitor (<code>{@link #monStrmStart}</code>).  
     * The monitor is released during the first Query Service call to the 
     * <code>{@link #onNext(QueryResponse)} method.  
     * <p>
     * <p>
     * It is assumed that either <code>{@link #start()}</code> has previously been called.
     * Upon return from this method the stream buffer begins acquiring the incoming data.
     * </p>
     *  
     * @param   lngTimeout  duration of time to wait for stream start event
     * @param   tuTimeout   the time units of the timeout parameter (e.g., MILLISECONDS, SECONDS, etc.)
     * 
     * @throws IllegalStateException    gRPC stream has not been started (see {@link #start()})
     * @throws TimeoutException waiting time for the response expired
     * @throws InterruptedException     an interruption occurred while waiting for stream start
     * 
     * @see #start()
     */
    public void awaitStreamStart(long lngTimeout, TimeUnit tuTimeout) throws IllegalStateException, TimeoutException, InterruptedException {
    
        // Check if a stream has been requested
        if (!this.bolStreamRequested) {
            String strMsg = JavaRuntime.getQualifiedCallerName() + ": ERROR - Attempted to wait on a stream that has not been requested.";
            if (this.bolLogging)
                LOGGER.error(strMsg);

            throw new IllegalStateException(strMsg);
        }

        // Log event
        if (this.bolLogging)
            LOGGER.debug("{} - latching on monitor StreamStart for {} {}", JavaRuntime.getQualifiedCallerName(),lngTimeout, tuTimeout);
    
        boolean bolReleased = this.monStrmStart.await(lngTimeout, tuTimeout);
        
        if ( !bolReleased ) {
            String strMsg = "DpQueryStreamQueueBufferDeprecated#awaitStreamStart() - Timeout out waiting on first response from the Datastore";
    
            // Log event
            if (this.bolLogging)
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
     * onto the stream end monitor (<code>{@link #monStrmComplete}</code>) which
     * is released by the <code>{@link #onCompleted()}</code> method.  
     * </p>
     * <p>
     * Once the monitor is released the calling method unblocks and 
     * returns to the caller.  The data stream is complete and all response data is
     * available if no errors occurred.
     * </p>
     * 
     * @throws IllegalStateException    gRPC stream has not been started (see {@link #start()})
     * @throws InterruptedException     an interruption occurred while waiting for response
     *  
     * @see #shutdownSoft()
     * @see #shutdownNow()
     */
    public void awaitStreamCompleted() throws IllegalStateException, InterruptedException {
        
        // Check if a stream has been requested
        if (!this.bolStreamRequested) {
            String strMsg = JavaRuntime.getQualifiedCallerName() + ": ERROR - Attempted to wait on a stream that has not been requested.";
            if (this.bolLogging)
                LOGGER.error(strMsg);

            throw new IllegalStateException(strMsg);
        }

        // Log event
        if (this.bolLogging)
            LOGGER.debug("{} - latching on monitor StreamEnd indefinitely.", JavaRuntime.getQualifiedCallerName());

        this.monStrmComplete.await();
    }

    /**
     * <p>
     * Waits (blocks) until the gRPC data stream with the Query Service has completed.
     * </p>
     * <p> 
     * Waits for the <code>onCompleted</code> response from the 
     * <em>Query Service</em> by latching
     * onto the stream end monitor (<code>{@link #monStrmComplete}</code>) which
     * is released by the <code>{@link #onCompleted()}</code> method.  
     * </p>
     * <p>
     * Once the monitor is released the calling method unblocks and 
     * returns to the caller.  The data stream is complete and all response data is
     * available if no errors occurred.
     * </p>
     * 
     * @param   lngTimeout  duration of time to wait for stream completion event
     * @param   tuTimeout   the time units of the timeout parameter (e.g., MILLISECONDS, SECONDS, etc.)
     * 
     * @throws IllegalStateException    gRPC stream has not been started (see {@link #start()})
     * @throws TimeoutException         timeout for the response expired
     * @throws InterruptedException     an interruption occurred while waiting for response
     *  
     * @see #shutdownSoft()
     * @see #shutdownNow()
     */
    public void awaitStreamCompleted(long lngTimeout, TimeUnit tuTimeout) throws IllegalStateException, TimeoutException, InterruptedException {
        
        // Check if a stream has been requested
        if (!this.bolStreamRequested) {
            String strMsg = JavaRuntime.getQualifiedCallerName() + ": ERROR - Attempted to wait on a stream that has not been requested.";
            if (this.bolLogging)
                LOGGER.error(strMsg);

            throw new IllegalStateException(strMsg);
        }

        // Log event
        if (this.bolLogging)
            LOGGER.debug("{} - latching on monitor StreamEnd for {} {}", JavaRuntime.getQualifiedCallerName(), lngTimeout, tuTimeout);

        boolean bolReleased = this.monStrmComplete.await(lngTimeout, tuTimeout);
        
        if ( !bolReleased ) {
            String strMsg = JavaRuntime.getQualifiedCallerName() + ": Timeout out waiting for stream to complete. ";

            // Log event
            if (this.bolLogging)
                LOGGER.debug(strMsg);
            
            throw new TimeoutException(strMsg);
        }
    }

    /**
     * <p>
     * Perform a hard shutdown of an active data streaming process.  Any active processes
     * are interrupted.
     * </p>
     * <p>
     * If the streaming process is inactive nothing is done. Specifically, 
     * <ul>
     * <li>if the <code>{@link #start()}</code> has not been called.</li>
     * <li>if the data stream has already completed.</li>
     * In such cases the method returns <code>false</code>.
     * </p>
     * <p>
     * Use this method with caution as it sends an error message to the
     * <em>Query Service</em>.  This will inevitably cause a cascade of errors
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
            if (this.bolStreamRequested == false)
                return false;
            
            // If the stream has already completed there is nothing to do
            if (this.monStrmComplete.getCount() == 0)
                return false;

            // Create the exception to pass to the Query Service
            Throwable src =  new Throwable("User requested a hard shutdown of data streaming");

            // Terminate the stream and release the latches
            this.hndSvrStrm.onError(src);
            this.monStrmStart.countDown();
            this.monStrmComplete.countDown();

            return true;
        }
    }
    

    //
    // State Query
    //
    
    /**
     * <p>
     * Indicates whether or not the data stream has been initiated with
     * a call to a <code>{@link #start()} </code> method.
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
    public boolean isStreamRequested() {
        return this.bolStreamRequested;
    }
    
    /**
     * <p>
     * Returns whether or not the associated data stream is currently active.
     * </p>
     * <p>
     * The returned value is the following combination of state queries:
     * <br/><br/>
     * &nbsp; &nbsp; <code>{@link #isStreamRequested()} && ! {@link #isStreamCompleted()}</code>
     * </p>
     * 
     * @return <code>true</code> if the Query Service data stream is active,
     *         <code>false</code> otherwise
     *         
     * @see #isStreamRequested()
     * @see #isStreamCompleted()        
     */
    public boolean isStreamActive() {
        return this.isStreamRequested() && !this.isStreamCompleted();
    }
    
    /**
     * <p>
     * Determines whether or not data streaming has completed.
     * </p>
     * <p>
     * The returned value is always <code>false</code> until the streaming has received all the requested 
     * data from the <em>Query Service</em> according to the initial request query specified in the constructor.
     * This condition requires that the data stream has send an 
     * <code>onComplete()</code> to this <code>DpQueryStreamBuffer</code> instance and
     * that no streaming errors have occurred.
     * </p>
     * 
     * @return <code>true</code> if all data in the original data request query has been acquire,
     *         <code>false</code> otherwise
     */
    public boolean isStreamCompleted() {
        return this.monStrmComplete.getCount() == 0;
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
     * <li>The data stream was initiated with a <code>start</code> method invocation.</li>
     * <li>The data stream was started and the first response was acquired.</li>
     * <li>The first response contained a rejected message by the Query Service.</li>
     * <li>A subsequent response within the data stream contained a rejection message (unusual).</li>
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
     * after streaming has been initiated (i.e., after the call to <code>{@link #start()}</code>).
     * Thus, a returned value of <code>true</code> indicates that an error 
     * condition has occurred and the stream has been terminated.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * Although the data stream was terminated there may exist available data acquired before the error.
     * Call method <code>{@link #getBufferSize()}</code> to determine if such data is available.
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
    // Buffer Operations
    //
    
    /**
     * <p>
     * Returns the size (in bytes) of the <em>first</em> buffered data page.   
     * </p>
     * <p>
     * The first data page generally has the largest size.  The remaining pages typically have
     * the same size (to maximize gRPC transmission efficiency and performance).  However,
     * the last data page is likely smaller as it contains the remainder of the results set.
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
        this.awaitStreamStart();
        
        // If the streaming has started the total page count is available
        return this.szPageBytes;
    }

    /**
     * <p>
     * Returns the current number of data pages in the stream buffer.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This method returns the <em>current</em> size of the response buffer.
     * <ul>
     * <li>If streaming is active the returned value is dynamics.</li>
     * <li>If the streaming has completed the returned value reflects the size of the results set 
     *     (number of data pages).</li>
     * </ul>
     * </p>
     *  
     * @return  current size of the stream buffer containing requested data 
     */
    public int  getBufferSize() {
        return this.lstBufferRsps.size();
    }
    
    /**
     * <p>
     * Returns the data page buffer in its current state.
     * </p>
     * <h2>NOTES:</h2>
     * This method returns response buffer, which is dynamic.
     * <ul>
     * <li>If streaming is active the data buffer may continue to expand.</li>
     * <li>If the streaming has completed the returned value is the results set.</li> 
     * </ul>
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * The returned object should not be modified if the current stream is still active.
     * Results are unpredictable.
     * </p>
     * 
     * @return  the entire data page buffer in its current state   
     */
    public final List<QueryResponse>  getBuffer() {
        return this.lstBufferRsps;
    }
    
    /**
     * <p>
     * Returns the given data page from the data stream buffer.
     * <p>
     * <p>
     * This is a convenience method which simply defers to <code>{@link List#get(int)}</code> on
     * the internal data buffer.
     * </p>
     * <p>
     * <h2>NOTEs:</h2>
     * <ul>
     * <li>
     * The provided index must be within the index of the stream buffer 
     * (which is dynamic, see <code>{@link #getBufferSize()}</code>).  
     * Otherwise an <code>IndexOutOfBoundsException</code> is thrown ().
     * </li>
     * <br/> 
     * <li>
     * Another option is to register a <code>{@link IDataStreamObserver}</code>
     * callback using the method <code>{@link #addStreamObserver(IQueryStreamObserver)}</code>.
     * Any callback registered here will be notified whenever a data page becomes available.
     * </li>
     * </ul>
     * </p>
     *  
     * @param indPage   index of data page requested  
     * 
     * @return the <em>Query Service</em> gRPC response message at buffer index
     * 
     * @throws IndexOutOfBoundsException the given index is not in the index set of data pages
     */
    public QueryResponse getBufferPage(Integer indPage) throws IndexOutOfBoundsException {
        
        return this.lstBufferRsps.get(indPage);
    }
    
    
    // 
    // Blocking Queue Operations
    //
    
    /**
     * <p>
     * Returns the number of available data pages when using this stream buffer as a blocking queue.
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
     * @throws NoSuchElementException   if the stream has completed and the queue is empty
     * @throws InterruptedException     if the current thread is interrupted while waiting
     */
    public QueryResponse removeQueuePage() throws NoSuchElementException, InterruptedException {
        
        // Block (indefinitely) until a data page becomes available
        this.semPageRdy.acquire();
        
        // Remove and return the data page at the queue head
        return this.lstBufferRsps.remove();
    }
    
    /**
     * <p>
     * Removes and returns the data page at the queue head, 
     * blocking until a page becomes available or timeout occurs.
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
     * </p>
     * 
     * @param lngTimeout    timeout duration
     * @param tuTimeout     timeout time units 
     * 
     * @return the data page at the head of the queue buffer
     * 
     * @throws TimeoutException     no data page available within the timeout limit
     * @throws InterruptedException if the current thread is interrupted
     */
    public QueryResponse removeQueuePage(long lngTimeout, TimeUnit tuTimeout) throws TimeoutException, InterruptedException {
        
        // Block (with timeout) until a data page becomes available
        boolean bolResult = this.semPageRdy.tryAcquire(lngTimeout, tuTimeout);
        
        // Check if timeout occurred 
        if (!bolResult) {
            String strMsg = JavaRuntime.getQualifiedCallerName() + ": timeout occured after " + lngTimeout + " " + tuTimeout.name();
            
            // Log event
            if (this.bolLogging)
                LOGGER.error(strMsg);
            
            throw new TimeoutException(strMsg);
        }
        
        // Remove and return the data page at the queue head
        return this.lstBufferRsps.remove();
    }
    
    

    //
    // Object Overrides (debugging)
    //

    /**
     *
     * @see @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer    buf = new StringBuffer(JavaRuntime.getCallerClass() + "\n");
        
        // Write out contents
        buf.append("  List of page request indices: " + this.lstIndPageRequested + "\n");
        buf.append("  List of page response indices: " + this.lstIndPageAcquired + "\n");
        buf.append("  Next Page index: " + this.indNextPage + "\n");
        buf.append("  Buffer size: " + this.lstBufferRsps.size() + "\n");
        buf.append("  Page size (bytes): " + this.szPageBytes + "\n");
        buf.append("  Stream type: " + this.enmStreamType + "\n");
        buf.append("  Stream Completed: " + (this.monStrmComplete.getCount() == 0) + "\n");
        buf.append("  Request Rejected flag: " + this.bolRequestRejected + "\n");
        buf.append("  Stream Requested flag: " + this.bolStreamRequested + "\n");
        buf.append("  Stream Error flag: " + this.bolStreamError + "\n");
        buf.append("  Query Service error message: " + this.strErrMsg + "\n");
        
        return buf.toString();
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
     * If this is a bidirectional stream then the next page (cursor) query message is sent to 
     * the forward stream handle.
     * </p>  
     * <p>
     * <h2>On First Invocation</h2>
     * Upon first response from the <em>Query Service</em> then the following operations are 
     * performed:
     * <ul>
     * <li>The streaming started monitor is released.</li>
     * <li>Streaming state variables are initialized.</li>
     * <li>Query stream observers are notified of stream start.</li>
     * </ul>
     * <h2>If Query is Rejected</h2>
     * <ul>
     * <li>If BIDI stream - <code>OnCompleted()</code> is sent to forward stream (shutdown stream).</li>
     * <li>The request reject flag <code>({@link #bolRequestRejected}</code> is set.</li>
     * <li>The stream end monitor is released.</li>
     * <li>Query stream observers are notified of a request rejection.</li>
     * <li>The method returns without processing.</li>
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
        if (this.bolLogging) {
            LOGGER.debug("{} called for monStrmStart = {}", JavaRuntime.getCallerName(), this.monStrmStart);
        }
        
        // Check for Initial Response - If the stream start monitor is still latched this is the first time through
        //  - initialize the state variables 
        //  - release the 'stream start' monitor
        //  - notify observers that stream has started
        if (this.monStrmStart.getCount() > 0) {
            
            // Initialize the state variables
//            this.indCurrPage = 0;
            this.szPageBytes = Integer.toUnsignedLong( msgRsp.getSerializedSize() );

            // Release the stream start monitor
            this.monStrmStart.countDown();
            
            // Debugging
            if (this.bolLogging)
                LOGGER.debug("{}: Released monitor StreamStart {}", JavaRuntime.getCallerName(), this.monStrmStart);

            // Notify stream observers
            this.notifyStart();
        }
        
        // Query Failure Check - if this is not a data message something is wrong
        //  NOTE: This should usually happen on the first response
        if (msgRsp.hasQueryReject()) {
            
            // Get the details of the rejection and report them
            RejectDetails   msgReject = msgRsp.getQueryReject();
            
            this.processRejectedRequest(msgReject);;
            
            return;
        }

        // Normal operation - Save the returned data page and request next page if bidirectional
        //  - the data is stored in the data buffer
        //  - all callback functions are notified that the page is available
        this.addPage(msgRsp);

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
     * <li>The error message is saved in <code>{@link #strErrMsg}</code>.</li>
     * <li>The stream start and stream end monitors are released.</li>
     * <li>Any observers functions requesting error notification are called.</li>
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

        String strErrMsg = JavaRuntime.getCallerName() + " called by gRPC or Query Service, exception message = " + e.getMessage();

        // Log event
        if (this.bolLogging)
            LOGGER.error(strErrMsg);
        
        this.bolStreamError = true;
        this.strErrMsg = e.getMessage();
        this.monStrmStart.countDown();
        this.monStrmComplete.countDown();
        
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
     * completed and all data is available.  Releases the <code>{@link #monStrmComplete}</code>
     * monitor.
     * </p>
     * 
     * @see #awaitStreamCompleted()
     * @see StreamObserver#onCompleted()
     */
    @Override
    public void onCompleted() {

        // Log event
        if (this.bolLogging)
            LOGGER.info("{}: Stream completed with {} data page(s) received.", JavaRuntime.getCallerName(), this.lstBufferRsps.size());

        // Update state variables and conditions
        this.monStrmComplete.countDown();
        
        // Notify all observers
        this.notifyCompleted();
    }

    
    //
    // IDpQueryStreamObserver Notifications
    //
    
    /**
     * <p>
     * Notify all observers that stream has started.
     * </p>
     * 
     * @see IDpQueryStreamObserver#notifyStreamingStarted(DpQueryStreamBuffer)
     */
    private void notifyStart() {
        this.lstStreamObservers.forEach( o -> o.notifyStreamingStarted() );
    }
    
    /**
     * <p>
     * Notifies all stream observers that original data request was rejected by the Query Service.
     * </p>
     *  
     * @param msgReject request rejection details provided by the Query Service
     * 
     * @see IDpQueryStreamObserver#notifyRequestRejected(RejectDetails)
     */
    private void notifyRejected(RejectDetails msgReject) {
        this.lstStreamObservers.forEach( o -> o.notifyRequestRejected(msgReject));
    }
    
    /**
     * <p>
     * Notify all observers that stream has completed.
     * </p>
     * 
     * @see IDpQueryStreamObserver#notifyStreamingCompleted(DpQueryStreamBuffer)
     */
    private void notifyCompleted() {
        this.lstStreamObservers.forEach(o -> o.notifyStreamingCompleted());
    }
    
    /**
     * <p>
     * Notify all observers that the given data page has been returned and is available.
     * </p>
     * 
     * @param indPage   page index within stream buffer
     * @param msgRsp    gRPC message containing page of requested results set
     * 
     * @see IDpQueryStreamObserver#notifyResponseReady(QueryResponse)
     */
    private void notifyResponse(QueryResponse msgRsp) {
        this.lstStreamObservers.forEach( o -> o.notifyResponseReady(msgRsp));
    }
    
    /**
     * <p>
     * Notify all observers of a streaming error.
     * </p>
     * 
     * @param strMsg any error message send by the data stream
     * @param eSrc the source of the streaming error
     * 
     * @see IDpQueryStreamObserver#notifyStreamingError(String, Throwable)
     */
    private void notifyError(String strMsg, Throwable eSrc) {
        this.lstStreamObservers.forEach( o -> o.notifyStreamingError(strMsg, eSrc) );
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Initiates the data stream by performing the data request on the <em>Query Service</em> stub.
     * </p>
     * <p> 
     * Once this method is called the gRPC data stream is initiated, either unidirectional or bidirectional.
     * <ul>
     * <li>
     * For unidirectional streams the appropriate RPC operation is invoked on the communications stub 
     * which both opens the data stream and initiates the streaming operations.  
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
    protected void requestDataStream(QueryRequest msgRqst) throws IllegalArgumentException {

        // Check argument for required data request
        if (!msgRqst.hasQuerySpec()) {
            String  strMsg = JavaRuntime.getQualifiedCallerName() + ": argument does not contain initial query.";
            if (this.bolLogging)
                LOGGER.error(strMsg);

            throw new IllegalArgumentException(strMsg);
        }

        // Initialize the data page monitoring (primarily for BDID streams)
        this.indNextPage = 0;
        this.lstIndPageRequested.add(this.indNextPage);
        
        
        // Initiate and start the gRPC data stream according to stream type
        switch (this.enmStreamType) {
        
        case UNIDIRECTIONAL:

            // Send the data initializing and starting the backward stream 
            this.stubAsync.queryResponseStream(msgRqst, this);
            
            break;
            
        case BIDIRECTIONAL:

            // Initiate the stream and retrieve the forward stream handle
            this.hndSvrStrm = (CallStreamObserver<QueryRequest>) this.stubAsync.queryResponseCursor(this);

            // Send the first data request starting the BIDI streaming process
            this.hndSvrStrm.onNext(msgRqst);

            break;
            
        }
        
        // Set the active request flag and return
        this.bolStreamRequested = true;
    }

    /**
     * <p>
     * Processes a rejected query request obtained from the <code>{@link #onNext(QueryResponse)}</code> method.
     * </p>
     * <p>
     * We assume that the rejection occurred on the first response from the Query Service, however, any
     * mid-stream rejections will be handled the same.
     * </p>
     * The following set of operations are performed:
     * <ul>
     * <li>The rejection is logged (if logging is active).</li>
     * <li>Shuts down the forward stream if bidirectional (with <code>{@link #hndSvrStrm#onCompleted()})</code></li>.
     * <li>Sets the 'request rejected' flag (<code>{@link #bolRequestRejected}</code>.</li>
     * <li>Release the 'stream completed monitor'</li>
     * <li>Notify all query stream observers that the request was rejected.</li>
     * </ul>
     * 
     * @param msgReject the rejection details Protobuf message within the <code>QueryResponse</code> message
     */
    protected void processRejectedRequest(RejectDetails msgReject) {
        
        // Get the details of the rejection and report them
        RejectReason    enmCause = msgReject.getRejectReason();
        String          strMsg = msgReject.getMessage();
        
        if (this.bolLogging)
            LOGGER.error("{}: Query Service rejected request - {}, {}", JavaRuntime.getCallerName(), enmCause.name(), strMsg);
        
        // Shut down the stream
        //  - If this is a bidirectional stream send the completed notification
        if(this.enmStreamType == StreamType.BIDIRECTIONAL) 
            this.hndSvrStrm.onCompleted();
           
        //  - State associated state condition and variables
        this.bolRequestRejected = true;
        this.monStrmComplete.countDown();
        
        // Notify all observers
        this.notifyRejected(msgReject);
        
        return;
    }
    
    /**
     * <p>
     * Requests the that the next data page be send from the <em>Query Service</em> 
     * when using a BIDI stream.
     * </p>
     * <p>
     * This method is only active for bidirectional streams.  If a unidirectional stream was 
     * initiated the method simply returns.
     * </p>
     * <p>
     * This method is used after the initial data request has been transmitted in a bidirectional 
     * stream.
     * <ul>
     * <li>
     * The initial data request, in the <code>{@link #msgRequest}</code> attribute, is assumed 
     * to have been previously sent to the <em>Query Service</em>.
     * </li>
     * <li>
     * All following requests (generated here) contain only a "cursor" request and are sent
     * through the forward stream handle <code>{@link #hndSvrStrm}</code>.
     * </li>
     * </p>
     * 
     */
    protected void requestNextPageBidi() {

        // If this is not a bidirectional stream there is nothing to do
        if (this.enmStreamType != StreamType.BIDIRECTIONAL)
            return;

        // Add next page index to request list  
        this.lstIndPageRequested.add(this.indNextPage);

        // Create the next page cursor request and send it
        QueryRequest msgPgRqst = QueryRequest
                .newBuilder()
                .setCursorOp(CursorOperation.CURSOR_OP_NEXT)
                .build();

        this.hndSvrStrm.onNext(msgPgRqst);
    }

    /**
     * <p>
     * Adds the given Query Service data response to the buffer of stored data pages.
     * </p>
     * <p>
     * The following operations are performed in order:
     * <ul>
     * <li>The given response message is added to the buffer tail.</li>
     * <li>Current value 'next page index' is added to the list of acquired pages.</li>
     * <li>The 'next page index' is incremented.</li>
     * <li>Query stream observers are notified of the new data.</li>
     * <li>The blocking queue buffer semaphore is given an addition permit.</li>
     * </ul>
     * 
     * @param msgRsp    gRPC response message containing data (the data page)
     */
    protected void addPage(QueryResponse msgRsp) {

        this.lstBufferRsps.add(msgRsp);

        this.lstIndPageAcquired.add(this.indNextPage);
        
        this.indNextPage = this.indNextPage + 1;
        
        this.notifyResponse(msgRsp);
        
        this.semPageRdy.release();
    }
    
}
