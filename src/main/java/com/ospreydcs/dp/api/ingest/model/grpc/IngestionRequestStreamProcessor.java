/*
 * Project: dp-api-common
 * File:	IngestionRequestStreamProcessor.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	IngestionRequestStreamProcessor
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

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.model.AUnavailable;
import com.ospreydcs.dp.api.model.AUnavailable.STATUS;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult;
import com.ospreydcs.dp.grpc.v1.ingestion.DpIngestionServiceGrpc.DpIngestionServiceStub;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataResponse;

import io.grpc.StatusException;
import io.grpc.stub.CallStreamObserver;
import io.grpc.stub.StreamObserver;

/**
 *
 * @author Christopher K. Allen
 * @since Apr 9, 2024
 *
 */
public abstract class IngestionRequestStreamProcessor implements Runnable, Callable<Boolean> {

    
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
    private static final Boolean    BOL_LOGGING = CFG_INGEST.logging.active;
    
    
    //
    // Class Resources
    //
    
    /** Class logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    
    //
    // Defining Attributes
    //
    
    /** The supplier of <code>IngestDataRequest</code> messages */
    private final IMessageSupplier<IngestDataRequest>   fncDataSource;
    
    
    //
    // Stream Resources
    //
    
    /** Interface handle to the forward gRPC data stream (i.e., ingestion stream) to the Ingestion Service */
    private CallStreamObserver<IngestDataRequest>       hndForwardStream = null;

    
    //
    // State Variables and Conditions
    //
    
    /** Has the gRPC data stream started ? */
    protected boolean         bolStreamStarted = false;
    
    /** Has the gRPC data stream completed ? */
    protected boolean         bolStreamComplete = false;
    
    /** Can be set by child classes to indicate an error and terminate stream */
    protected boolean         bolStreamError = false;
    
    
    /** The current number of requests sent forward through the stream */
    protected int             cntRequests = 0;
    
    /** The current number of responses (acknowledgments) received from the gRPC stream */
    protected int             cntResponses = 0;
    
//    /** Has the gRPC data stream completed ? */
//    private CountDownLatch  monStreamCompleted = new CountDownLatch(1);
    
    /** Was successful ? Were there any errors during streaming ? */
    protected ResultStatus    recResult = null;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance, initialized of <code>IngestionRequestStreamProcessor</code>.
     * </p>
     *
     * @param fncDataSource the supplier of <code>IngestDataRequest</code> messages 
     */
    protected IngestionRequestStreamProcessor(IMessageSupplier<IngestDataRequest> fncDataSource) { 
        this.fncDataSource = fncDataSource;
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
     * 
     * @return the status of the stream completion
     */
    abstract protected ResultStatus awaitCompletion();
    
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
     * @param msgRqst    the Ingestion Service request message that was just transmitted
     * 
     * @throws Exception    general exception thrown by base class to indicate error
     */
    abstract protected void  requestTransmitted(IngestDataRequest msgRqst) throws Exception;
    
    
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
        return this.bolStreamComplete;
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
     * <code>{@link #getResult()}</code> 
     * are available to determined the cause of the failure. 
     * </p>
     *   
     * @return  <code>true</code> if the streaming task completed normally and was successful
     * 
     * @see #isStreamStarted()
     * @see #isCompleted()
     * @see #getResult()
     */
    public boolean isSuccess() {
        
        // Must first check if result has been assigned yet
        if (this.recResult == null)
            return false;
        
        // If so return the success flag
        return this.recResult.success();
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
     * @see #getResult()
     */
    public final int getResponseCount() {
        return this.cntResponses;
    }
    
    /**
     * <p>
     * Returns the result of the streaming task, or <code>null</code> if incomplete.
     * </p>
     * <p>
     * Returns the current value of the <code>{@link #recResult}</code> attribute.  If
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
    public final ResultStatus   getResult() {
        return this.recResult;
    }

    
    //
    // Runnable Interface
    //
    
    /**
     *
     * @see @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        
        // Initiate the gRPC data stream
        this.hndForwardStream = this.initiateGrpcStream();
        this.bolStreamStarted = true;

        // Enter message transmission loop
        try {
            // Send all available data messages to Ingestion Service
            while (this.fncDataSource.hasNext() && !this.bolStreamError) {
                
                // Get the next message to transmit
                IngestDataRequest   msgRqst = this.fncDataSource.poll(INT_TIMEOUT, TU_TIMEOUT);

                if (msgRqst == null)
                    continue;
                
                this.hndForwardStream.onNext(msgRqst);
                this.cntRequests++;
            }

        // gRPC threw unexpected runtime error while streaming
        } catch (io.grpc.StatusRuntimeException e) {
            String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() 
                            + "gRPC threw runtime exception during streaming: " 
                            + e.getMessage();

            this.hndForwardStream.onError(e);
            
            this.bolStreamError = true;
            this.recResult = ResultStatus.newFailure(strMsg, e);
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            return;

        // The message supplier was polled while empty - this should not happen normally
        } catch (IllegalStateException e) {
            String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() 
                            + ": Internal error - Message supplier polled when empty: "
                            + e.getMessage();

            this.bolStreamError = true;
            this.recResult = ResultStatus.newFailure(strMsg, e);
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            return;
        }
            
        
        // Completed normally 
        // - Terminate the data stream 
        // - Set state variables
        // - Await the subclass completion
        this.hndForwardStream.onCompleted();
        this.bolStreamComplete = true;
        this.recResult = this.awaitCompletion();
    }
    
    
    //
    // Callable<Boolean> Interface
    //
    
    /**
     * Defers to method <code>{@link #run()}</code> and return the status.
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Boolean call() throws Exception {
        this.run();
        
        return this.recResult.isSuccess();
    }
}

/**
 * <p>
 * Implements a forward gRPC data stream to the Ingestion Service.
 * </p>
 * <p>
 * <h2>NOT IMPLEMENTED</h2>
 * The Ingestion Service current does NOT support single-stream ingestion and, therefore,
 * this class will not function.  The <code>{@link #initiateGrpcStream()}</code> operation
 * simply returns <code>null</code>.
 * </p>
 * <p>
 * TODO 
 * Implement single-stream ingestion with Ingestion Service.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Apr 9, 2024
 *
 */
@AUnavailable(status=STATUS.UNKNOWN, note="Usefulness of single-stream ingestion is currently undetermined.")
final class IngestionRequestUniStreamProcessor extends IngestionRequestStreamProcessor {

    
    //
    // Defining Attributes
    //
    
    /** The asynchronous communications stub to the Ingestion Service */
    private final DpIngestionServiceStub                stubAsync;
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionRequestUniStreamProcessor</code>.
     * </p>
     *
     * @param stubAsync     the asynchronous communications stub to the Ingestion Service 
     * @param fncDataSource the supplier of <code>IngestDataRequest</code> messages 
     */
    @AUnavailable(status=STATUS.UNKNOWN, note="Do not create instances of this class.")
    protected IngestionRequestUniStreamProcessor(DpIngestionServiceStub stubAsync, IMessageSupplier<IngestDataRequest> fncDataSource) {
        super(fncDataSource);
        
        this.stubAsync = stubAsync;
    }

    
    // 
    // IngestionStreamRequestProcessor Abstract Methods
    //
    
    /**
     * This method is inactive.
     * 
     * @return  <code>null</code>
     *
     * @see com.ospreydcs.dp.api.ingest.model.grpc.IngestionRequestStreamProcessor#initiateGrpcStream()
     */
    @AUnavailable(status=STATUS.UNKNOWN, note="A null value is always returned.")
    @Override
    protected CallStreamObserver<IngestDataRequest> initiateGrpcStream() {
        // CallStreamObserver<IngestDataRequest>   hndFoward = this.stubAsync.
        return null;
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.ingest.model.grpc.IngestionRequestStreamProcessor#requestTransmitted(com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest)
     */
    @Override
    protected void requestTransmitted(IngestDataRequest msgRqst) throws Exception {
        
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.ingest.model.grpc.IngestionRequestStreamProcessor#awaitCompletion()
     */
    @Override
    protected ResultStatus awaitCompletion() {
        
        return ResultStatus.SUCCESS;
    }
    
}

/**
 * <p>
 * Implements a bidirectional gRPC data stream with the Ingestion Service.
 * </p>
 * <p>
 * The forward gRPC data stream to the Ingestion Service is established when the stream is
 * initiated with <code>{@link #initiateGrpcStream()}</code>.  This class acts as the
 * reverse gRPC data stream receiving acknowledgments from the Ingestion Service and passing
 * them to the data sink supplied during construction.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 9, 2024
 *
 */
final class IngestionRequestBidiStreamProcessor extends IngestionRequestStreamProcessor implements StreamObserver<IngestDataResponse> {

    
    //
    // Defining Attributes
    //
    
    /** The asynchronous communications stub to the Ingestion Service */
    private final DpIngestionServiceStub                stubAsync;
    
    /** The consumer of <code>IngestDataResponse</code> messages */
    private final Consumer<IngestDataResponse>          fncDataSink;

    
    //
    // State Variables and Conditions
    //
    
    /** Has the gRPC data stream completed ? */
    private CountDownLatch  monStreamCompleted = new CountDownLatch(1);
    
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionRequestBidiStreamProcessor</code>.
     * </p>
     *
     * @param stubAsync     the asynchronous communications stub to the Ingestion Service 
     * @param fncDataSource the supplier of <code>IngestDataRequest</code> messages 
     * @param fncDataSink   the consumer of <code>IngestDataResponse</code> messages 
     */
    protected IngestionRequestBidiStreamProcessor(DpIngestionServiceStub stubAsync, IMessageSupplier<IngestDataRequest> fncDataSource, Consumer<IngestDataResponse> fncDataSink) {
        super(fncDataSource);

        this.stubAsync = stubAsync;
        this.fncDataSink = fncDataSink;
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
     * @see com.ospreydcs.dp.api.ingest.model.grpc.IngestionRequestStreamProcessor#initiateGrpcStream()
     */
    @Override
    protected CallStreamObserver<IngestDataRequest> initiateGrpcStream() {
        
        // Initiate the bidirectional data stream
        StreamObserver<IngestDataRequest>   ifcForward = this.stubAsync.ingestDataStream(this);
        
        // Cast forward stream interface to implementing class and return it
        CallStreamObserver<IngestDataRequest>   hndForward = (CallStreamObserver<IngestDataRequest>)ifcForward;
        
        return hndForward;
    }

    /**
     * <p>
     * Blocks until the gRPC data stream has completed, either normally or from error.
     * </p>
     * <p>
     * This operation is intended to be called from the <code>{@link #run()}</code> method
     * which starts initiates the stream processing task as an independent thread. 
     * This method blocks on the <code>{@link #monStreamCompleted}</code> stream completed 
     * monitor until it is released, either through normal streaming operations or from
     * an error encountered during streaming/processing.  In either event, the task is
     * finished and the <code>{@link #run()}</code> method can return.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * If the (thread) process is interrupted while waiting for the task to complete, the
     * <code>{@link #recResult}</code> is set to failure and includes a description along with
     * the causing exception.
     * </p>
     * 
     * @return the status of the stream completion
     */
    @Override
    protected ResultStatus  awaitCompletion() {
    
        // Block until the streaming operation is complete
        try {
            this.monStreamCompleted.await();
            
            return ResultStatus.SUCCESS;
    
        } catch (InterruptedException e) {
    
            // The wait was interrupted - interpret this an error (perhaps originating elsewhere)
            return ResultStatus.newFailure(JavaRuntime.getQualifiedCallerNameSimple() + " - interrupted while waiting for task completion", e);
        }
    }

    @Override
    protected void requestTransmitted(IngestDataRequest msgRqst) throws Exception {
        // TODO Auto-generated method stub

    }

    //
    // StreamObserver<IngestDataResponse> Interface
    //

    @Override
    public void onNext(IngestDataResponse msgRsp) {

        // Process the first response
        if (!this.bolStreamStarted) {
            this.bolStreamStarted = true;

            // Check for rejected request
            ResultStatus accepted = this.isRequestAccepted(msgRsp);

            // If the request was rejected
            // - save the rejected result record
            // - release the stream completed monitor
            // - return - the stream is over
            if (accepted.isFailure()) {
                this.recResult = accepted;

                this.monStreamCompleted.countDown();
                return;
            }
        }

        // Process the query response message
        ResultStatus    result = this.processResponse(msgRsp);

        // Check for processing failed - if so...
        // - save the failure result record
        // - release the stream completed monitor 
        // - return - the stream is over
        if (result.isFailure()) {
            this.recResult = result;

            this.monStreamCompleted.countDown();
            return;
        }

        // Increment the data pages counter
        this.cntResponses++;

        // Inform child class that request has been processed
        //  - perform any post-precessing within child class
        //  - send any acknowledgments to the Query Service
        try {
//            this.requestProcessed(msgRsp);

        } catch (Exception e) {
            this.recResult = ResultStatus.newFailure("Exception thrown during post-processing.", e);

            this.monStreamCompleted.countDown();
            return;
        }
    }

    /**
     * <p>
     * Handles a gRPC streaming error.
     * </p>
     * <p>
     * This operation is typically called by gRPC to signal a general streaming error.
     * However, it can be called by the Query Service to signal an unanticipated exception.
     * In either event, the following actions ensue:
     * <ol>  
     * <li>The result record <code>{@link #recResult}</code> is set to a failure with message and exception.</li>
     * <li>The stream completed monitor <code>{@link #monStreamCompleted}</code> is released.</li>
     * </ol>
     * The data stream is officially terminated whenever this operation is called so all
     * data processing is terminated.
     * </p>
     *
     * @param   e   the terminating exception causing invocation, contains error message
     * 
     * @see io.grpc.stub.StreamObserver#onError(java.lang.Throwable)
     */
    @Override
    public void onError(Throwable e) {
        this.recResult = ResultStatus.newFailure("The gRPC stream was terminated during operation (see cause).", e);
        this.monStreamCompleted.countDown();
    }

    /**
     * <p>
     * Handles the gRPC streaming completed notification.
     * </p>
     * <p>
     * This operation is invoked by the Ingestion Service to signal that it received the 
     * <code>onCompleted()</code> signal from the forward stream and the stream is no longer 
     * active.  
     * The method performs the following actions:
     * <ol>  
     * <li>The result record <code>{@link #recResult}</code> is set to SUCCESS.</li>
     * <li>The stream completed monitor <code>{@link #monStreamCompleted}</code> is released.</li>
     * </ol>
     * The data stream is officially terminated whenever this operation is called and all
     * data processing should be complete.
     * </p>
     *
     * @see @see io.grpc.stub.StreamObserver#onCompleted()
     */
    @Override
    public void onCompleted() {

        this.recResult = ResultStatus.SUCCESS;
        this.monStreamCompleted.countDown();
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Determines whether or not the original data request was accepted by the Query Service.
     * </p>
     * <p>
     * This method should be called only once, with the argument being the first response
     * from the Query Service after stream initiation.  If the original data query
     * request was invalid or corrupt the Query Service streams back a single 
     * <code>QueryResponse</code> message containing a <code>ExceptionalResult</code> message
     * describing the rejection.
     * </p> 
     * <p>
     * The method Checks for a <code>ExceptionalResult</code> message within the query response 
     * argument.  If present then the original data request was rejected by the Query Service.
     * The details of the rejection are then extracted from the argument and returned in
     * a FAILED result record.  Otherwise (i.e., the request was accepted) the method 
     * returns the SUCCESS result record.
     * </p> 
     * 
     * @param msgRsp    the first response from the Query Service data stream
     * 
     * @return  the SUCCESS record if query was accepted, 
     *          otherwise a failure message containing a description of the rejection 
     */
    protected ResultStatus isRequestAccepted(IngestDataResponse msgRsp) {

        // Check for RequestRejected message
        if (msgRsp.hasExceptionalResult()) {
            ExceptionalResult           msgException = msgRsp.getExceptionalResult();
            String                      strCause = msgException.getMessage();
            ExceptionalResult.ExceptionalResultStatus     enmCause = msgException.getExceptionalResultStatus();

            String       strMsg = "The data request was rejected by Query Service: cause=" + enmCause + ", message=" + strCause;
            ResultStatus result = ResultStatus.newFailure(strMsg);

            return result;
        }

        // No rejection, return success
        return ResultStatus.SUCCESS;
    }

    /**
     * <p>
     * Processes the data within the given Ingestion Service response message.
     * </p>
     * <p>
     * For normal operation, a <code>AckResult</code> message is confirmed within the argument.
     * If confirmed the argument is then passed to the message consumer identified at construction.
     * A SUCCESS result record is then returned indicating successful consumption of response data.
     * </p>
     * <p>
     * If the argument contains an exception(i.e., rather than data) then the method
     * extracts the status and accompanying message.  An error message is constructed
     * and returned via a FAILED result record.
     * </p>
     * 
     * @param msgRsp    an Ingestion Service response message containing acknowledgment or status error
     * 
     * @return  the SUCCESS record if acknowledgment was confirmed,
     *          otherwise a failure message containing the status error description 
     */
    protected ResultStatus processResponse(IngestDataResponse msgRsp) {

        // Confirm acknowledgment and pass to message consumer
        if (msgRsp.hasAckResult()) {
            this.fncDataSink.accept(msgRsp);

            return ResultStatus.SUCCESS;
        }

        // Response Error - extract the details and return them
        ExceptionalResult   msgException = msgRsp.getExceptionalResult();
        String              strStatus = msgException.getMessage();
        ExceptionalResult.ExceptionalResultStatus enmStatus = msgException.getExceptionalResultStatus();

        String          strMsg = "Ingestion Service reported response error: status=" + enmStatus + ", message= " + strStatus;
        ResultStatus    recErr = ResultStatus.newFailure(strMsg);

        return recErr;
    }

}
