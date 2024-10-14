/*
 * Project: dp-api-common
 * File:	IngestionStream.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IngestionStream
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
 * @since Apr 9, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.grpc;

import java.security.ProviderException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.AUnavailable;
import com.ospreydcs.dp.api.common.AUnavailable.STATUS;
import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.ingest.model.IMessageSupplier;
import com.ospreydcs.dp.api.model.ClientRequestUID;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceStub;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataResponse;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataStreamResponse;

import io.grpc.stub.CallStreamObserver;
import io.grpc.stub.StreamObserver;

/**
 * <p>
 * <h1>Base class implementing Ingestion Service gRPC stream processor.</h1>
 * </p>
 * <p>
 * Performs the basic operations required for establishing a gRPC data stream between with the
 * Ingestion Service and transmitting <code>{@link IngestDataRequest}</code> messages.
 * Subclasses implement the specific details depending upon whether the desired gRPC stream is
 * unidirectional or bidirectional.  Note that these subclasses are hidden and cannot be 
 * instantiated externally.
 * The creators for <code>IngestionStream</code> provide concrete instances of the subclasses
 * that clients can use for stream processing.
 * </p>
 * <p>
 * <h2>Stream Processing</h2>
 * The method <code>{@link #run()}</code> initiates the stream processing and does not return until
 * processing is complete.  The method <code>{@link #isSuccess()}</code> returns whether or not
 * the streaming operation was fully successful. The status of the stream processing is available 
 * with method </code>{@link #getStatus()}</code> which will include a detailed error message
 * if the processing was not successful.
 * There are other "getter" methods for returning the state and properties of the stream processor 
 * either during processing or at completion. 
 * </p>
 * <p>  
 * The type of gRPC data stream used to transmit ingestion data is determined by the creator
 * used to instantiate the processor.  Use 
 * <code>{@link #newUniProcessor(DpIngestionServiceStub, IMessageSupplier)}</code> for a unidirectional
 * data stream (currently unavailable) or 
 * <code>{@link #newBidiProcessor(DpIngestionServiceStub, IMessageSupplier, Consumer)}</code> for a
 * bidirectional data stream. 
 * </p>
 * <p>
 * <h2>Independent Thread Execution</h2>
 * The class implements the <code>{@link Runnable}</code> and <code>{@link Callable}</code> interfaces
 * for executing the streaming operations on independent threads.  The <code>{@link #run()}</code>
 * method of the <code>Runnable</code> interface is the entry into the stream processor execution.
 * The <code>{@link #call()}</code> method of the <code>Callable</code> interface simply defers
 * to the <code>run()</code> method then returns the status of the stream processing.
 * </p>
 * <p>
 * <h2>Exceptions</h2>
 * This class no longer monitors the data stream for ingestion exceptions signaled by the Ingestion Service.
 * The only exception handling within this class is for the gRPC streaming.  It is the responsibility of the 
 * client to determine whether or not the transmitted data was successfully ingested, specifically by
 * inspection of the responses provided by the Ingestion Service.
 * </p>
 * <p>
 * Having said the above, the <code>IngestionStream</code> class does record all client request 
 * UIDs of the incoming ingestion data.  These UIDs are available from method <code>{@link #getRequestIds()}</code>.
 * These client request UIDs can be used to query the Data Platform for success/failure of the supplied data.
 * </p>
 * <p>
 * <h2>Unidirectional gRPC Streams</h2>
 * Unidirectional gRPC data streams requires both a supplier of <code>IngestDataRequest</code> messages
 * and a consumer for the single <code>IngestDataStreamResponse</code> message recovered by the data stream.  
 * The unidirectional stream will transmit all data obtained from the supplier as fast as possible.
 * When all data has been transmitted (i.e., the <code>{@link IMessageSupplier#isSupplying()}</code> returns
 * <code>false</code>) the client signals a "half-closed" event to the Ingestion Service (i.e., invokes the
 * <code>{@link StreamObserver#onCompleted()}</code> method).  Under normal operations the Ingestion Service
 * 1) transmits the single <code>IngestDataStreamResponse</code> which is passed to the message consumer, then
 * 2) signals the final "half-closed" event terminating the data stream.
 * </p> 
 * <p>
 * <h2>Bidirectional gRPC Streams</h2>
 * Bidirectional gRPC data streams require both a supplier of <code>IngestDataRequest</code> messages
 * and a consumer of <code>IngestDataResponse</code> messages.  The bidirectional stream will
 * transmit all data obtained from the supplier as fast as possible, then collect all responses as
 * fast as they arrive.  The bidirectional stream will not terminate (i.e., the <code>run()</code> method
 * will not return) until the Ingestion Service has signaled that there are no more responses (i.e., it receives
 * the <code>{@link StreamObserver#onCompleted()}</code> "half-close" event).
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Apr 9, 2024
 *
 */
public abstract class IngestionStream implements Runnable, Callable<Boolean> {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a bidirectional gRPC data stream processor for the Ingestion Service.
     * </p>
     * <p>
     * The gRPC data stream to the Ingestion Service is established when the stream upon 
     * invocation of <code>{@link #run()}</code> or </code>{@link #call()}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * <s>
     * In the current implementation we monitor Ingestion Service responses for ingestion exceptions.
     * In the case of an ingestion exception we stop the streaming and shut everything down.
     * This behavior may be overreaching and unwarranted.
     * </s>
     * </li>
     * <br/>
     * <li>
     * An alternate implementation may wish to collect the identifiers of the rejected requests and
     * pass it up to the base class as the result status message.
     * </li>
     * </ul>
     * </p>
     * 
     * @param stubAsync     the asynchronous communications stub to the Ingestion Service 
     * @param fncDataSrc    the supplier of <code>IngestDataRequest</code> messages 
     * @param fncRspSink   the consumer of <code>IngestDataResponse</code> messages
     *  
     * @return  new bidirectional ingestion stream processor ready for independent thread spawn
     */
    public static IngestionStream newBidiProcessor(DpIngestionServiceStub stubAsync, IMessageSupplier<IngestDataRequest> fncDataSrc, Consumer<IngestDataResponse> fncRspSink) {
        
        return new IngestionBidiStream(stubAsync, fncDataSrc, fncRspSink);
    }
    
    /**
     * <p>
     * Creates and returns a new unidirectional ingestion stream processor for the Ingestion Service.
     * </p>
     * <p>
     * The gRPC data stream to the Ingestion Service is established when the stream upon 
     * invocation of <code>{@link #run()}</code> or </code>{@link #call()}</code>.
     * </p>
     * 
     * @param stubAsync     the asynchronous gRPC communications stub to the Ingestion Service
     * @param fncDataSrc    supplier of <code>IngestDataRequest</code> messages
     * @param fncRspSink    the consumer of <code>IngestDataStreamResponse</code> messages
     * 
     * @return  new unidirectional ingestion stream processor ready for independent thread spawn
     */
    public static IngestionStream newUniProcessor(DpIngestionServiceStub stubAsync, IMessageSupplier<IngestDataRequest> fncDataSrc, Consumer<IngestDataStreamResponse> fncRspSink) {
    
        return new IngestionUniStream(stubAsync, fncDataSrc, fncRspSink);
    }
    
    
    //
    // Application Resources
    //
    
    /** The Ingestion Service client API default configuration */
    private static final DpIngestionConfig  CFG_INGEST = DpApiConfig.getInstance().ingest; 
    
    
    //
    // Class Constants
    //
    
    /** Timeout limit to wait for message supplier */
    private static final int       INT_TIMEOUT = 15;
    
    /** Timeout units to wait for message supplier */
    private static final TimeUnit  TU_TIMEOUT = TimeUnit.MILLISECONDS;
    
    
    /** Is logging active */
    protected static final Boolean    BOL_LOGGING = CFG_INGEST.logging.active;
    
    
    //
    // Class Resources
    //
    
    /** Class logger */
    protected static final Logger     LOGGER = LogManager.getLogger();
    
    
    
    //
    // Defining Attributes
    //
    
    /** The asynchronous communications stub to the Ingestion Service */
    protected final DpIngestionServiceStub                  stubAsync;
    
    /** The supplier of <code>IngestDataRequest</code> messages */
    protected final IMessageSupplier<IngestDataRequest>     fncDataSource;
    
//    /** The consumer of <code>IngestDataResponse</code> messages */
//    protected final Consumer<IngestDataResponse>            fncRspSink;

    
    //
    // Stream Resources
    //
    
    /** Interface handle to the forward gRPC data stream (i.e., ingestion stream) to the Ingestion Service */
    private CallStreamObserver<IngestDataRequest>   hndForwardStream = null;

    /** Collection of all outgoing message client request IDs */
    private final List<ClientRequestUID>            lstClientIds = new LinkedList<>();
    
    
    //
    // State Variables and Conditions
    //
    
    /** Has the gRPC data stream started ? */
    protected boolean         bolStreamStarted = false;
    
//    /** Has the gRPC data stream completed ? */
//    protected boolean         bolStreamComplete = false;
    
    /** Can be set by child classes to indicate an error and terminate stream */
    protected boolean         bolStreamError = false;
    
    /** Has the backward gRPC data stream completed */
    protected CountDownLatch    monStreamCompleted = new CountDownLatch(1);
    
    
    //
    // Result Conditions
    //
    
    /** The current number of requests sent forward through the stream */
    protected int             cntRequests = 0;
    
    /** The current number of responses (acknowledgments) received from the gRPC stream */
    protected int             cntResponses = 0;
    
    /** Was successful ? Were there any errors during streaming ? */
    protected ResultStatus    recStatus = null;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance, initialized of <code>IngestionStream</code>.
     * </p>
     *
     * @param stubAsync     the asynchronous communications stub to the Ingestion Service 
     * @param fncDataSource the supplier of <code>IngestDataRequest</code> messages 
     * @param fncRspSink    the consumer of <code>IngestDataResponse</code> messages 
     */
    protected IngestionStream(DpIngestionServiceStub stubAsync, IMessageSupplier<IngestDataRequest> fncDataSource /*, Consumer<IngestDataResponse> fncRspSink */) {
        this.stubAsync = stubAsync;
        this.fncDataSource = fncDataSource;
//        this.fncRspSink = fncRspSink;
    }

    
    //
    // Required Abstract Overrides
    //

    /**
     * <p>
     * Initiate the gRPC data stream and return the interface handle to the forward stream.
     * </p>
     * <p>
     * Child classes must initiate the gRPC data stream to the Ingestion Service.
     * They do so according to the gRPC stream type, either unidirectional or 
     * bidirectional.
     * </p>
     * <p>
     * <h2>Unidirectional gRPC Streams</h2>
     * In this case the returned gRPC stream interface is the only data stream.  The
     * <code>StreamObserver<IngestDataResponse</code> interface operations of this base class
     * are not used.
     * </p>
     * <p>
     * <h2>Bidirectional gRPC Streams</h2>
     * In this case the child class furnishes the forward stream using this operation and the
     * base class functions as the backward stream receiving acknowledgments.
     * </p>
     *  
     * @return  the forward stream handle to the Ingestion Service (i.e., the ingestion stream)
     */
    abstract protected CallStreamObserver<IngestDataRequest>  initiateGrpcStream();
    
    /**
     * <p>
     * Signal to child classes that the request has been transmitted successfully.
     * </p>
     * <p>
     * This method is called immediately after the given argument has been transmitted
     * to the Ingestion Service via the <code>{@link CallStreamObserver#onNext(Object)}</code>
     * of the forward stream handle.
     * Child class may perform any post-processing and/or send acknowledgments and/or 
     * notifications.
     * </p>
     * <p>
     * <H2>NOTES:</h2>
     * <ul>
     * <li>
     * Any post-processing must be expedient, the data stream is delayed until this method
     * returns.
     * </li>
     * <br/>
     * <li>
     * If an exception is thrown by the child class the stream is assumed to be terminated.
     * Child classes must terminate any bidirectional streaming operation.
     * </li>
     * </ul>
     * </p>
     * 
     * @param msgRqst    the Ingestion Service request message that was transmitted
     * 
     * @throws ProviderException    general exception thrown by subclass to indicate internal error
     */
    abstract protected void  requestTransmitted(IngestDataRequest msgRqst) throws ProviderException;
    
    
    //
    // State Queries
    //
    
    /**
     * <p>
     * Indicates whether or not the gRPC data stream has started.
     * </p>
     * <p>
     * Returns the <code>{@link #bolStreamStarted}</code> flag.  This value is initialized to
     * <code>false</code> upon construction.  It is set <code>true</code> upon the first 
     * invocation of <code>{@link #onNext(QueryResponse)}</code> by the Query Service.
     * </p>
     * <p>
     * Note that if the value <code>true</code> is returned BOTH the following conditions apply:
     * <ol>
     * <li>The implied task has been started with a <code>{@link #run()}</code> invocation.</li>
     * <li>The gRPC data stream has been created and initiated.</li>
     * </ol>
     * If a value <code>false</code> is returned it is possible (although unlikely) that the
     * task has been started by {@link #run()} but the gRPC data stream has not yet begun.
     * </p> 
     *   
     * @return  <code>true</code> if the gRPC data stream has started returning data,
     *          <code>false</code> otherwise
     */
    public boolean  isStreamStarted() {
        return this.bolStreamStarted;
    }
    
    /**
     * <p>
     * Indicates whether or not the gRPC data stream has completed, either normally or due to error.
     * </p>
     * <p>
     * The method checks the count of the streaming completed monitor <code>{@link #monStreamCompleted}</code>.
     * If the value is equal to 0 then the gRPC data stream has been terminated and this object
     * is no longer receiving data from the Query Service.  
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * A returned value of <code>true</code> does not indicate SUCCESS, only that the data stream
     * has completed.  Use <code>{@link #isSuccess()}</code> to determine the success or failure
     * of the data stream processing operation.
     * </li>
     * <br/>
     * <li>
     * In its intended implementation, a returned value of <code>true</code> implies that the
     * <code>{@link #run()}</code> method has returned and the task is complete (whether 
     * successful or not).  It is up to derived classes to enforce this condition. 
     * Use the protected method <code>{@link #awaitCompletion()}</code>) within 
     * <code>{@link #run()} implementations to block until complete.
     * </li> 
     * </p>
     * 
     * @return  <code>true</code> if the gRPC data stream has completed normally or been terminated due to error
     * 
     * @see #isSuccess()
     */
    public boolean  isCompleted() {
        return this.monStreamCompleted.getCount() == 0;
    }
    
    /**
     * <p>
     * Indicates whether or not the stream processing task has completed successfully.
     * </p>
     * <p>
     * If the returned value is <code>true</code> this indicates all of the following 
     * conditions:  
     * <ol>
     * <li>The data stream processing task has been started with <code>{@link #run()}</code>.</li>
     * <li>The gRPC data stream has completed normally.</li>
     * <li>All data processing was successful.</li>
     * </ol>
     * A returned value of <code>false</code> indicates the complement of any of the above
     * conditions.  Methods 
     * <code>{@link #isStreamStarted()}</code>, 
     * <code>{@link #isCompleted()}</code>, and
     * <code>{@link #getStatus()}</code> 
     * are available to determined the cause of the failure. 
     * </p>
     *   
     * @return  <code>true</code> if the streaming task completed normally and was successful
     * 
     * @see #isStreamStarted()
     * @see #isCompleted()
     * @see #getStatus()
     */
    public boolean isSuccess() {
        
        // Must first check if result has been assigned yet
        if (this.recStatus == null)
            return false;
        
        // If so return the success flag
        return this.recStatus.success();
    }
    
    /**
     * <p>
     * Returns the number of requests transmitted to the Ingestion Service.
     * </p>
     * <p>
     * Returns the current value of attribute <code>{@link #cntRequests}</code> at the time
     * of invocation.  If streaming is completed the value is the total number of requests
     * transmitted to the Ingestion service.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * It is possible that a ingest data request message was successfully transmitted but the
     * data was not correctly processed.  This condition is only detectable through a 
     * bidirectional stream thet responds with acknowledgments of data processing.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  the number of request messages sent so far
     */
    public final int getRequestCount() {
        return this.cntRequests;
    }
    
    /**
     * <p>
     * Returns the number of responses received from the Ingestion Service so far.
     * </p>
     * <p>
     * Returns the value of attribute <code>{@link #cntResponses}</code> at the time of 
     * invocation.  If the streaming task has completed the value is the total number of 
     * <code>IngestDataResponse</code> messages received from the Ingestion Service before the
     * data stream was completed or terminated due to error.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Subclasses are expected to increment the <code>{@link #cntResponses}</code> attribute.
     * </li>
     * <li>
     * This value may not apply to unidirectional data streams, in which case the returned value
     * is 0.
     * </li>
     * <li>
     * It is possible that gRPC data streaming was interrupted due to error but some response 
     * messages were processed before stream termination.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  the number of <code>QueryResponse</code> messages received and processed so far
     * 
     * @see #isCompleted()
     * @see #isSuccess()
     * @see #getStatus()
     */
    public final int getResponseCount() {
        return this.cntResponses;
    }
    
    /**
     * <p>
     * Returns an ordered list of client request identifiers for all the <code>IngestDataRequest</code>
     * messages transmitted to the Ingestion Service.
     * </p>
     * <p>
     * The returned collection can be used to cross check with the Ingestion Service that all
     * data for the given requests was successfully processed and archived.
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * The size of the returned collection should be that returned by 
     * <code>{@link #getRequestCount()}</code>.
     * </p>
     *  
     * @return  collection of request IDs for the ingest data request messages send to the Ingestion Service
     */
    public final List<ClientRequestUID>  getRequestIds() {
        return this.lstClientIds;
    }
    
    /**
     * <p>
     * Returns the status of the streaming task upon completion, or <code>null</code> if incomplete.
     * </p>
     * <p>
     * Returns the current value of the <code>{@link #recStatus}</code> attribute.  If
     * the stream has completed this record is available for inspection, otherwise a 
     * value <code>null</code> is returned indicated that the stream has not started or
     * has not completed.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This method is intended for error condition recovering when the <code>{@link #isSuccess()}</code>
     * method returns <code>false</code>.
     * </p>
     * 
     * @return  the result of the stream processing task as a <code>ResultStatus</code>
     * 
     * @see #isCompleted()
     */
    public final ResultStatus   getStatus() {
        return this.recStatus;
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Blocks until the gRPC data stream has completed, either normally or from error.
     * </p>
     * <p>
     * This operation is intended to be called from the <code>{@link #run()}</code> method
     * which initiates the stream and performs all data transmission (as a thread).
     * After transmitted all data the <code>{@link #run()}</code> calls this method to
     * perform any cleanup operations, or wait for all responses to be recovered if the
     * stream is bidirectional.    
     * When this method returns  the streaming is considered complete and the 
     * <code>{@link #run()}</code> method can return.
     * </p>
     * <p>
     * The method is make public as multiple outside processes can also block on the
     * stream completion event.
     * </p> 
     * 
     * @return the status of the stream operation and completion
     */
    public ResultStatus awaitCompletion() {
        
        
        // Block until the streaming operation is complete
        try {
            this.monStreamCompleted.await();
            
            // If an error occurred during streaming the status should be set - return it
            if (this.bolStreamError && this.recStatus!=null)
                return this.recStatus;
            
            // If we are here everything worked without error
            return ResultStatus.SUCCESS;
            
        } catch (InterruptedException e) {
            
            // The wait was interrupted - interpret this an error (perhaps originating elsewhere)
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - interrupted while waiting for Ingestion Stream gRPC stream half-closed event.";

            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            return ResultStatus.newFailure(strMsg, e);
        }
    }
    
    /**
     * <p>
     * External termination of a data stream if currently active.
     * </p>
     * <p>
     * This method terminates an active data stream thread by setting the <code>{@link #bolStreamError}</code>
     * to <code>true</code>.  This action causes the processing loop within <code>{@link #run()}</code> to exit.
     * The gRPC stream is then terminated with an <code>{@link StreamObserver#onError(Throwable)}</code> invocation.
     * </p>
     * 
     * @return  <code>true</code> if the data stream task was successfully terminated,
     *          <code>false</code> otherwise (e.g., the stream was never started or has already completed)
     */
    public boolean  terminate() {
        
        if (!this.isStreamStarted() || this.isCompleted() || this.bolStreamError)
            return false;
        
        // Create the error message, status, and exception
        String      strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Data stream terminated by client.";
        Throwable   expGrpc = new Throwable(strMsg);
        
        this.recStatus = ResultStatus.newFailure(strMsg);
        
        // Set the error flag (exiting processing loop) and terminate the gRPC data stream
        this.bolStreamError = true;
        this.hndForwardStream.onError(expGrpc);
        
        return true;
    }
    
    
    //
    // Runnable Interface
    //
    
    /**
     * <p>
     * Begins ingestion data request stream processing.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This method is a requirement of the <code>{@link Runnable}</code> interface for
     * independent thread execution in Java. 
     * </p>
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        
        // TODO - Remove
        if (BOL_LOGGING)
            LOGGER.debug("{} - Started thread, about to enter processing loop.", JavaRuntime.getQualifiedCallerNameSimple());
        
        // Initiate the gRPC data stream
        this.hndForwardStream = this.initiateGrpcStream();
        this.bolStreamStarted = true;

        // Enter message transmission loop which includes exception handling
        try {
            // Send all data messages to Ingestion Service while available and no errors
            while (this.fncDataSource.isSupplying() && !this.bolStreamError) {
                
                // Poll for the next message to transmit - throws IllegalStateException
                IngestDataRequest   msgRqst = this.fncDataSource.poll(INT_TIMEOUT, TU_TIMEOUT);

                // If timeout try again
                if (msgRqst == null)
                    continue;
                
//                // TODO - Remove
//                LOGGER.debug(JavaRuntime.getQualifiedCallerNameSimple() + " - Attempting to send ingestion request message with client ID "+ msgRqst.getClientRequestId());

                // Transmit data message request and notify subclasses
                this.hndForwardStream.onNext(msgRqst);  // throws StatusRuntimeException
                
                String strClientId = msgRqst.getClientRequestId();
                
//                // TODO - Remove
//                LOGGER.debug(JavaRuntime.getQualifiedCallerNameSimple() + " - Transmitted message with client ID "+ strClientId);

                this.lstClientIds.add(ClientRequestUID.from(strClientId));
                this.cntRequests++;
                this.requestTransmitted(msgRqst);       // throws ProviderException
            }

        // Unexpected gRPC runtime error while streaming
        } catch (io.grpc.StatusRuntimeException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                            + "gRPC threw runtime exception during streaming: " 
                            + e.getMessage();

            this.hndForwardStream.onError(e);
            
            this.bolStreamError = true;
            this.recStatus = ResultStatus.newFailure(strMsg, e);
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            return;

        // The message supplier was polled while empty - this should not happen normally
        } catch (IllegalStateException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                            + ": Internal error - Message supplier polled when empty: "
                            + e.getMessage();

            this.hndForwardStream.onError(e);
            
            this.bolStreamError = true;
            this.recStatus = ResultStatus.newFailure(strMsg, e);
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            return;

        // The supplier polling was interrupted while waiting
        } catch (InterruptedException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + ": External error - Message supplier polling was interrupted: "
                    + e.getMessage();

            this.hndForwardStream.onError(e);
            
            this.bolStreamError = true;
            this.recStatus = ResultStatus.newFailure(strMsg, e);

            if (BOL_LOGGING)
                LOGGER.error(strMsg);

            return;

        // The child class objected when notified of message transmission
        } catch (ProviderException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + ": Subclass exception upon request transmission notification: "
                    + e.getMessage();

            this.hndForwardStream.onError(e);
            
            this.bolStreamError = true;
            this.recStatus = ResultStatus.newFailure(strMsg, e);

            if (BOL_LOGGING)
                LOGGER.error(strMsg);

            return;
        }
            
        
        // Check if completed normally 
        // - Terminate the data stream (send half-close at client end) 
        // - Set state variables
        // - Await the data stream completion (await half-close at server end)
        if (!this.bolStreamError) {
            
            this.hndForwardStream.onCompleted();
            this.recStatus = this.awaitCompletion();
//            this.bolStreamComplete = true;
            
            // Log stream successful event
            if (BOL_LOGGING)
                LOGGER.debug("{} - stream finished with {} message transmissions.", JavaRuntime.getQualifiedMethodNameSimple(), this.cntRequests);
        }
    }
    
    
    //
    // Callable<Boolean> Interface
    //
    
    /**
     * <p>
     * Begins ingestion data request stream processing.
     * </p>
     * <p>
     * This method is provided to satisfy the <code>{@link Callable}</code> interface for
     * independent thread execution in Java.  It is an alternate entry point for ingestion
     * streaming execution.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This method defers to method <code>{@link #run()}</code> and, when completed,
     * returns the SUCCESS attribute of the status record generated during stream processing.
     * </p>
     * 
     * @see java.util.concurrent.Callable#call()
     * @see #run()
     */
    @Override
    public Boolean call() throws Exception {
        this.run();
        
        return this.recStatus.isSuccess();
    }
}

//
// Hidden Classes
//

/**
 * <p>
 * Implements a forward, unidirectional gRPC data stream to the Ingestion Service.
 * </p>
 * <p>
 * <h2>Unidirectional Streaming</h2>
 * For a unidirectional gRPC stream the client sends ingest data requests as fast as they are available
 * from the data source <code>{@link #fncDataSource}</code> and accepted by the Ingestion Service; 
 * it does not collect any ingest data responses during streaming.  Only a single ingest data response
 * is received from the Ingestion Service upon completion of the stream (i.e., a single invocation of
 * <code>{@link StreamObserver#onNext(Object)}</code> by the Ingestion Service before it calls
 * <code>{@link StreamObserver#onCompleted()}</code>). 
 * </p>
 * <p>
 * The forward gRPC data stream to the Ingestion Service is established when the stream is
 * initiated with <code>{@link #initiateGrpcStream()}</code>.  This class acts as the single
 * response collector for gRPC data stream receiving a single acknowledgment from the Ingestion Service and passing
 * it to the data sink supplied during construction.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 9, 2024
 *
 */
final class IngestionUniStream extends IngestionStream implements StreamObserver<IngestDataStreamResponse> {

    
    //
    // Defining Attributes
    //
    
//    /** The asynchronous communications stub to the Ingestion Service */
//    private final DpIngestionServiceStub                stubAsync;
    
    /** The consumer of <code>IngestDataResponse</code> messages */
    private final Consumer<IngestDataStreamResponse>  fncRspSink;

    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionUniStream</code>.
     * </p>
     *
     * @param stubAsync     the asynchronous communications stub to the Ingestion Service 
     * @param fncRqstSrc    the supplier of <code>IngestDataRequest</code> messages 
     * @param fncRspSink    the consumer of <code>IngestDataResponse</code> messages 
     */
    protected IngestionUniStream(DpIngestionServiceStub stubAsync, IMessageSupplier<IngestDataRequest> fncRqstSrc, Consumer<IngestDataStreamResponse> fncRspSink) {
        super(stubAsync, fncRqstSrc /*, fncRspSink */);
        
        this.fncRspSink = fncRspSink;
//        this.stubAsync = stubAsync;
    }

    
    // 
    // IngestionStreamRequestProcessor Abstract Methods
    //
    
    /**
     * <p>
     * Initiates the bidirectional gRPC data stream and returns the handle to the forward
     * stream to the Ingestion Service.
     * </p>
     * <p>
     * The method invokes the <code>{@link DpIngestionServiceStub#ingestDataStream(StreamObserver)}</code>
     * on the asynchronous communication stub to open the data stream, which also returns the
     * forward stream interface.  The returned interface is cast to the base class and
     * returned.
     * </p>
     * 
     * @return  the handle to the forward gRPC ingestion stream
     *
     * @see com.ospreydcs.dp.api.ingest.model.grpc.IngestionStream#initiateGrpcStream()
     */
    protected CallStreamObserver<IngestDataRequest> initiateGrpcStream() {
        
        // Initiate the unidirectional data stream
        StreamObserver<IngestDataRequest>       ifcForward = super.stubAsync.ingestDataStream(this);
        
        // Cast forward stream interface to implementing class and return it
        CallStreamObserver<IngestDataRequest>   hndForward = (CallStreamObserver<IngestDataRequest>)ifcForward;
        
        return hndForward;
    }

    /**
     * <p>
     * Accept notification of data message transmission.
     * </p>
     * <p>
     * There is nothing to do here.
     * </p>
     * 
     * @see com.ospreydcs.dp.api.ingest.model.grpc.IngestionStream#requestTransmitted(com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest)
     */
    @Override
    protected void requestTransmitted(IngestDataRequest msgRqst) throws ProviderException {
    }

    
    //
    // StreamObserver<IngestDataResponse> Interface
    //
    
    /**
     * <h1>
     * Recovers the single response from the Ingestion Service.
     * </h1>
     * <p>
     * Under normal operations this method is invoked once by the Ingestion Service, after an 
     * <code>{@link StreamObserver#onCompleted()}</code> signal is sent to the Ingestion Service.
     * The signal is send in the base class processing thread (i.e., the <code>{@link Runnable#run()}</code>
     * method) and indicates that the client has finished all data transmission.
     * </p>
     * Once the single response message has been recovered the Ingestion Service should signal the final
     * half-closed event on this interface (i.e., <code>{@link StreamObserver#onCompleted()}</code>.
     * </p>
     *
     * @see io.grpc.stub.StreamObserver#onNext(java.lang.Object)
     */
    @Override
    public void onNext(IngestDataStreamResponse msgRsp) {

        super.cntResponses++;
        this.fncRspSink.accept(msgRsp);
    }


    /**
     * <h1>
     * Handles a gRPC streaming error.
     * </h1>
     * <p>
     * This operation is typically called by gRPC to signal a general streaming error.
     * However, it can be called by the Query Service to signal an unanticipated exception.
     * In either event, the following actions ensue:
     * <ol>  
     * <li>An error message is sent to the class logger (if active).</li>
     * <li>The stream error flag <code>{@link IngestionStream#bolStreamError}</code> is set <code>true</code>.
     * <li>The result record <code>{@link #recStatus}</code> is set to a failure with message and exception.</li>
     * <li>The stream completed monitor <code>{@link #monStreamCompleted}</code> is released.</li>
     * </ol>
     * The data stream is officially terminated whenever this operation is called so all
     * data processing is terminated.
     * </p>
     * <p>
     * TODO
     * See that the returned result is not overwritten.
     * </p>
     *
     * @param   e   the terminating exception causing invocation, contains error message
     * 
     * @see io.grpc.stub.StreamObserver#onError(java.lang.Throwable)
     */
    @Override
    public void onError(Throwable e) {
        String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                + "The gRPC stream was terminated during operation with cause: "
                + e.getMessage();

        if (BOL_LOGGING)
            LOGGER.error(strMsg);

        super.bolStreamError = true;
        super.recStatus = ResultStatus.newFailure(strMsg, e);
        super.monStreamCompleted.countDown();
    }


    /**
     * <h1>
     * Handles the gRPC streaming completed notification.
     * </h1>
     * <p>
     * This operation is invoked by the Ingestion Service to signal that no more ingestion
     * responses are available.  This occurs when the base class has transmitted all its
     * data and has signaled an <code>{@link StreamObserver#onCompleted()}</code> operation
     * (i.e., a "half-closed" stream event) on the forward stream.
     * </p>
     * <p>
     * The method performs the following actions:
     * <ol>  
     * <li>The stream completed monitor <code>{@link #monStreamCompleted}</code> is released.</li>
     * </ol>
     * Releasing the <code>{@link #monStreamCompleted}</code> latch unblocks the 
     * <code>{@link #awaitCompletion()}</code> lock which then allows it to return the result 
     * of the backward response streaming operation.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The data stream is officially terminated whenever this operation is called and all
     * data processing should be complete.
     * </p>
     *
     * @see io.grpc.stub.StreamObserver#onCompleted()
     */
    @Override
    public void onCompleted() {
        super.monStreamCompleted.countDown();
    }
    
}

/**
 * <p>
 * Implements a bidirectional gRPC data stream with the Ingestion Service.
 * </p>
 * <p>
 * <h2>Bidirectional Streaming</h2>
 * The forward gRPC data stream to the Ingestion Service is established when the stream is
 * initiated with <code>{@link #initiateGrpcStream()}</code>.  The forward stream handle 
 * is passed to the base class and established as attribute <code>{@link IngestionStream#hndForwardStream}</code>. 
 * This class acts as the reverse gRPC data stream receiving acknowledgments from the Ingestion Service and passing
 * them to the data sink supplied during construction.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * In the current implementation we monitor Ingestion Service responses for ingestion exceptions.
 * In the case of an ingestion exception we stop the streaming and shut everything down.
 * This behavior may be overreaching and unwarranted.
 * </li>
 * <br/>
 * <li>
 * An alternate implementation may wish to collect the identifiers of the rejected requests and
 * pass it up to the base class as the result status message.
 * </li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 9, 2024
 *
 */
final class IngestionBidiStream extends IngestionStream implements StreamObserver<IngestDataResponse> {

    
    //
    // Defining Attributes
    //
    
//    /** The asynchronous communications stub to the Ingestion Service */
//    private final DpIngestionServiceStub        stubAsync;
    
    /** The consumer of <code>IngestDataResponse</code> messages */
    private final Consumer<IngestDataResponse>  fncRspSink;

    
    //
    // Instance Resources
    //
    
//    /** Collection of responses that have indicated rejected request by the Ingestion Service */
//    private final List<IngestDataResponse>      lstBadRsps = new LinkedList<>();
    
    
    //
    // State Variables and Conditions
    //
    
//    /** Has the backward gRPC data stream completed */
//    private CountDownLatch      monStreamCompleted = new CountDownLatch(1);
    
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionBidiStream</code>.
     * </p>
     *
     * @param stubAsync     the asynchronous communications stub to the Ingestion Service 
     * @param fncDataSrc the supplier of <code>IngestDataRequest</code> messages 
     * @param fncRspSink    the consumer of <code>IngestDataResponse</code> messages 
     */
    protected IngestionBidiStream(DpIngestionServiceStub stubAsync, IMessageSupplier<IngestDataRequest> fncDataSrc, Consumer<IngestDataResponse> fncRspSink) {
        super(stubAsync, fncDataSrc /*, fncRspSink */);

//        this.stubAsync = stubAsync;
        this.fncRspSink = fncRspSink;
    }

    
    // 
    // IngestionStreamRequestProcessor Abstract Methods
    //
    
    /**
     * <p>
     * Initiates the bidirectional gRPC data stream and returns the handle to the forward
     * stream to the Ingestion Service.
     * </p>
     * <p>
     * The method invokes the <code>{@link DpIngestionServiceStub#ingestDataStream(StreamObserver)}</code>
     * on the asynchronous communication stub to open the data stream, which also returns the
     * forward stream interface.  The returned interface is cast to the base class and
     * returned.
     * </p>
     * 
     * @return  the handle to the forward gRPC ingestion stream
     * 
     * @see com.ospreydcs.dp.api.ingest.model.grpc.IngestionStream#initiateGrpcStream()
     */
    @Override
    protected CallStreamObserver<IngestDataRequest> initiateGrpcStream() {
        
        // Initiate the bidirectional data stream
        StreamObserver<IngestDataRequest>       ifcForward = this.stubAsync.ingestDataBidiStream(this);
        
        // Cast forward stream interface to implementing class and return it
        CallStreamObserver<IngestDataRequest>   hndForward = (CallStreamObserver<IngestDataRequest>)ifcForward;
        
        return hndForward;
    }

//    /**
//     * <p>
//     * Blocks until the gRPC data stream has completed, either normally or from error.
//     * </p>
//     * <p>
//     * This operation is intended to be called from the <code>{@link #run()}</code> method
//     * which starts initiates the stream processing task as an independent thread. 
//     * This method blocks on the <code>{@link #monStreamCompleted}</code> stream completed 
//     * monitor until it is released, either through normal streaming operations or from
//     * an error encountered during streaming/processing.  In either event, the task is
//     * finished and the <code>{@link #run()}</code> method can return.
//     * </p>
//     * <p>
//     * <h2>NOTES</h2>
//     * If the (thread) process is interrupted while waiting for the task to complete, the
//     * <code>{@link #recStatus}</code> is set to failure and includes a description along with
//     * the causing exception.
//     * </p>
//     * 
//     * @return the status of the stream completion
//     */
//    @Override
//    protected ResultStatus  awaitCompletion() {
//    
//        // Wait for the stream to finish
//        ResultStatus recStatus = super.awaitCompletion();
//        
//        // If bad requests were encountered report them in status message
//        if (!this.lstBadRsps.isEmpty() && recStatus.isSuccess()) {
//            String  strMsg = this.createRejectedRequestsMessage();
//            
//            if (BOL_LOGGING)
//                LOGGER.warn(strMsg);
//            
//            return ResultStatus.newFailure(strMsg);
//        }
//        
//        return recStatus;
//    }

    /**
     * <p>
     * Accept notification of data message transmission.
     * </p>
     * <p>
     * There is nothing to do here.
     * </p>
     * 
     * @see com.ospreydcs.dp.api.ingest.model.grpc.IngestionStream#requestTransmitted(com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest)
     */
    @Override
    protected void requestTransmitted(IngestDataRequest msgRqst) throws ProviderException {
    }

    
    //
    // StreamObserver<IngestDataResponse> Interface
    //

    /**
     * <p>
     * Processes the incoming ingestion response message from the backward stream.
     * </p>
     * <p>
     * Calls the <code>{@link #processResponse(IngestDataResponse)}</code> support method
     * which sends response to message consumer identified at construction, then
     * increments the response counter.
     * </p>
     *
     * @see io.grpc.stub.StreamObserver#onNext(java.lang.Object)
     */
    @Override
    public void onNext(IngestDataResponse msgRsp) {

        // Process the ingestion response message and increment counter
//        this.processResponse(msgRsp);
        this.fncRspSink.accept(msgRsp);
        super.cntResponses++;
    }

    /**
     * <h1>
     * Handles a gRPC streaming error.
     * </h1>
     * <p>
     * This operation is typically called by gRPC to signal a general streaming error.
     * However, it can be called by the Query Service to signal an unanticipated exception.
     * In either event, the following actions ensue:
     * <ol>  
     * <li>An error message is sent to the class logger (if active).</li>
     * <li>The stream error flag <code>{@link IngestionStream#bolStreamError}</code> is set <code>true</code>.
     * <li>The result record <code>{@link #recStatus}</code> is set to a failure with message and exception.</li>
     * <li>The stream completed monitor <code>{@link #monStreamCompleted}</code> is released.</li>
     * </ol>
     * The data stream is officially terminated whenever this operation is called so all
     * data processing is terminated.
     * </p>
     * <p>
     * TODO
     * See that the returned result is not overwritten.
     * </p>
     *
     * @param   e   the terminating exception causing invocation, contains error message
     * 
     * @see io.grpc.stub.StreamObserver#onError(java.lang.Throwable)
     */
    @Override
    public void onError(Throwable e) {
        String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                        + "The gRPC stream was terminated during operation with cause: "
                        + e.getMessage();

        if (BOL_LOGGING)
            LOGGER.error(strMsg);
        
        super.bolStreamError = true;
        super.recStatus = ResultStatus.newFailure(strMsg, e);
        super.monStreamCompleted.countDown();
    }

    /**
     * <h1>
     * Handles the gRPC streaming completed notification.
     * </h1>
     * <p>
     * This operation is invoked by the Ingestion Service to signal that no more ingestion
     * responses are available.  This occurs when the base class has transmitted all its
     * data and has signaled an <code>{@link StreamObserver#onCompleted()}</code> operation
     * (i.e., a "half-closed" stream event) on the forward stream.
     * </p>
     * <p>
     * The method performs the following actions:
     * <ol>  
     * <li>The stream completed monitor <code>{@link #monStreamCompleted}</code> is released.</li>
     * </ol>
     * Releasing the <code>{@link #monStreamCompleted}</code> latch unblocks the 
     * <code>{@link #awaitCompletion()}</code> lock which then allows it to return the result 
     * of the backward response streaming operation.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The data stream is officially terminated whenever this operation is called and all
     * data processing should be complete.
     * </p>
     *
     * @see io.grpc.stub.StreamObserver#onCompleted()
     */
    @Override
    public void onCompleted() {
        super.monStreamCompleted.countDown();
    }

    
    //
    // Support Methods
    //
    
//    /**
//     * <p>
//     * Determines whether or not the original data request was accepted by the Query Service.
//     * </p>
//     * <p>
//     * This method should be called only once, with the argument being the first response
//     * from the Query Service after stream initiation.  If the original data query
//     * request was invalid or corrupt the Query Service streams back a single 
//     * <code>QueryResponse</code> message containing a <code>ExceptionalResult</code> message
//     * describing the rejection.
//     * </p> 
//     * <p>
//     * The method Checks for a <code>ExceptionalResult</code> message within the query response 
//     * argument.  If present then the original data request was rejected by the Query Service.
//     * The details of the rejection are then extracted from the argument and returned in
//     * a FAILED result record.  Otherwise (i.e., the request was accepted) the method 
//     * returns the SUCCESS result record.
//     * </p> 
//     * 
//     * @param msgRsp    the first response from the Query Service data stream
//     * 
//     * @return  the SUCCESS record if query was accepted, 
//     *          otherwise a failure message containing a description of the rejection 
//     */
//    protected ResultStatus isRequestAccepted(IngestDataResponse msgRsp) {
//
//        // Check for RequestRejected message
//        if (msgRsp.hasExceptionalResult()) {
//            ExceptionalResult           msgException = msgRsp.getExceptionalResult();
//            String                      strCause = msgException.getMessage();
//            ExceptionalResult.ExceptionalResultStatus     enmCause = msgException.getExceptionalResultStatus();
//
//            String       strMsg = "The data request was rejected by Query Service: cause=" + enmCause + ", message=" + strCause;
//            ResultStatus result = ResultStatus.newFailure(strMsg);
//
//            return result;
//        }
//
//        // No rejection, return success
//        return ResultStatus.SUCCESS;
//    }
//
//    /**
//     * <p>
//     * Processes the ingestion response message.
//     * </p>
//     * <p>
//     * For normal operation, a <code>AckResult</code> message is confirmed within the argument.
//     * If confirmed the argument is then passed to the message consumer identified at construction.
//     * A SUCCESS result record is then returned indicating successful consumption of response data.
//     * </p>
//     * <p>
//     * The argument is passed to the message consumer identified at construction.
//     * If the argument contains an exception(i.e., rather than data) then the message
//     * is added to the list of bad responses.
//     * </p>
//     * 
//     * @param msgRsp    an Ingestion Service response message containing acknowledgment or status error
//     * 
//     * @return  the SUCCESS record if acknowledgment was confirmed,
//     *          otherwise a failure message containing the status error description 
//     */
//    private void processResponse(IngestDataResponse msgRsp) {
//
//        // Check for exception then pass to message consumer
//        if (msgRsp.hasExceptionalResult()) {
//
//            this.lstBadRsps.add(msgRsp);
//        }
//        
//        this.fncRspSink.accept(msgRsp);
//    }
//
//    /**
//     * <p>
//     * Creates a status message in the case where requests where rejected by the Ingestion Service.
//     * </p>
//     * <p>
//     * Creates and returns a string describing the condition which contains a list of all the
//     * ingestion request IDs that have been rejected 
//     * (see <code>{@link IngestDataRequest#getClientRequestId()}</code>).
//     * </p>
//     * 
//     * @return  message describing the rejected ingestion requests condition
//     */
//    private String  createRejectedRequestsMessage() {
//        
//        // Collect the request IDs of rejected requests
//        List<String>    lstRqstIds = this.lstBadRsps
//                .stream()
//                .<String>map(IngestDataResponse::getClientRequestId)
//                .toList();
//        
//        // Create the status message and return it
//        String  strMsg = "Ingestion Service rejected IngestionDataRequest message(s) with ID(s) "
//                        + lstRqstIds;
//
//        return strMsg;
//    }
}
