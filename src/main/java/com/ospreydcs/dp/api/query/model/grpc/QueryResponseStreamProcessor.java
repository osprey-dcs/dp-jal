/*
 * Project: dp-api-common
 * File:	QueryResponseStreamProcessor.java
 * Package: com.ospreydcs.dp.api.query.model.grpc
 * Type: 	QueryResponseStreamProcessor
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
 * @since Feb 6, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.grpc;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.model.DpQueryStreamType;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest.CursorOperation;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

import io.grpc.stub.CallStreamObserver;
import io.grpc.stub.StreamObserver;

/**
 * <p>
 * Query Service gRPC streaming response processor base class.
 * </p>
 * <p>
 * Subclasses function as gRPC <em>stream processors</em>, no query data processing is performed.
 * The gRPC data stream is maintained, the query data is extracted from the data stream, and
 * any Query Service rejections and errors are intercepted here.  Clients of this class and
 * sub-classes must supply a data sink for the query data at creation.
 * </p>
 * <p>
 * This class is intended as a base for the processing of gRPC data streams connected to the 
 * Data Platform Query Service.  It is also intended to be used as an independent thread
 * task as derived classes must implement the <code>{@link Runnable}</code> interface.
 * </p>
 * <p>
 * Note that this class functions as the backward gRPC stream for a streaming Query Service data 
 * request, implementing the <code>{@link StreamObserver}</code> interface bound to 
 * <code>{@link QueryRequest}</code> Protobuf messages received from the Query Service. 
 * It processes the <code>QueryResponse</code> messages obtained from the Data Platform
 * Query Service from a data request.  In fact, this class implements all that is required
 * from a <em>unidirectional</em> backwards gRPC data stream from the Query Service to this object.  
 * That is, instances of <code>QueryResponseStreamProcessor</code> represent the backward stream 
 * handle (data sink for the Query Service).   
 * </p>
 * <p>
 * <h2>Data Processing</h2>
 * An implementation of the <code>{@link Consumer}</code> interface bound to the 
 * <code>{@link QueryResponse.QueryReport.BucketData}</code> type must be provided at
 * creation to receive the incoming data from the Query Service.  The implementation
 * should perform <em>all</em> actual and specific data processing.
 * <br/><br/>
 * <code>QueryResponseStreamProcessor</code> objects extract the <code>BucketData</code>
 * messages from the streaming <code>QueryReponse</code> messages and pass only the data
 * messages to the <code>Consumer</code> object.  Thus, all errors and exceptions are
 * processed by the <code>QueryResponseStreamProcess</code> object. However, it does not
 * do any actual processing of the incoming data.
 * </p>
 * <p>
 * <h2>Streaming Exceptions</h2>
 * As described above, the <code>QueryResponseStreamProcessor</code> processes the gRPC
 * <em>data stream</em>.  Any errors returned by the Query Service are handled here, as
 * well as exceptions thrown by gRPC regarding streaming.  The following error conditions
 * are processed:
 * <ul>
 * <li>
 * gRPC streaming errors - gRPC will signal a streaming error with the 
 * <code>{@link #onError(Throwable)}</code> operation of <code>{@link StreamObserver}</code>.
 * The Query Service can also signal an error in this fashion.  The <code>Throwable</code>
 * argument contains information as to the cause of the error.
 * </li>
 * <br/>
 * <li>
 * Request Rejected - The Query Service will send a <code>QueryResponse</code> message containing
 * a <code>RejectDetails</code> message if the original data request was invalid or corrupt.
 * The message contains information on why the request was rejected.
 * </li>
 * <br/>
 * <li>
 * Query Error - If an error occurs during the query operation the Query Service will send a 
 * <code>QueryStatus</code> message within the <code>QueryReport</code> message 
 * (i.e., rather than <code>BucketData</code> containing data). The message contains information
 * concerning the cause of the error.
 * </li>
 * </ul>
 * In the event of any of the above, the <code>QueryResponseStreamProcessor</code> reacts 
 * essentially the same.  The following actions occur in order:
 * <ol>
 * <li>The gRPC data stream is terminated.</li>
 * <li>The cause of the error is recorded in the result record.</li>
 * <li>The stream completed monitor is released causing the <code>{@link #run()}</code> method to return.</li>
 * </ol>
 * The cause of any stream processing error is recoverable with the <code>{@link #getResult()}</code>
 * method, which contains the error message and possibly an exception 
 * (e.g., if <code>{@link #onError(Throwable)}</code> was invoked).  
 * <p>
 * <h2>Thread Task</h2>
 * Concrete classes are intended to be spawned as independent threads and, consequently, must
 * implement the <code>{@link #run()}</code> operation of this <code>{@link Runnable}</code>
 * interface.  The <code>{@link #run()}</code> implementation should not return until the
 * the streaming operation is complete, either normally or terminated due to an error.
 * (The <code>{@link #awaitCompletion()}</code> protected method is available to child classed
 * to block on this condition before returning.)
 * The success of the gRPC stream processing can be determined by calling 
 * <code>{@link #isSuccess()}</code> at this point.
 * <br/> <br/>
 * The class also implements the <code>{@link Callable}</code> bound to the <code>Boolean</code>
 * Java type.  Thus, instances can also be run within <code>ExecutorService</code> objects.
 * The <code>{@link #call()}</code> implementation simply invokes the <code>{@link #run()}</code>
 * abstract method then returns the result <code>{@link #isSuccess()}</code>.
 * </p>
 * <p>
 * <h2>Abstract Methods</h2>
 * Derived classes must override the following methods:
 * <ul>
 * <li><code>{@link #run()}</code> - creates and initializes the gRPC data stream.</li>
 * <li><code>{@link #requestTransmitted(QueryResponse)}</code> - perform any data post processing 
 *           or data stream operations.</li>
 * </ul>
 * Note that the <code>{@link #run()}</code> implementation must BOTH create and initiate the 
 * gRPC data stream.  For unidirectional streams this action is one and the same.  
 * For bidirectional streams the data stream must be initiated explicitly.  Child classes
 * should then block on the <code>{@link #awaitCompletion()}</code> method until the task is
 * complete.
 * <br/> <br/>
 * Derived classes implementing a <em>bidirectional</em> stream from the Query Service must 
 * override the <code>{@link #requestTransmitted(QueryResponse)}</code> method to sent a 
 * <code>CursorRequest</code> message to the Query Service in order to receive the next message.
 * Unidirectional streams need only implement an empty method if no post processing is required.
 * </p>  
 *
 * @author Christopher K. Allen
 * @since Feb 6, 2024
 *
 */
public abstract class QueryResponseStreamProcessor implements Runnable, Callable<Boolean>, StreamObserver<QueryDataResponse> {


    //
    // Initialization Targets
    //
    
    /** The query request being processed */
    protected final QueryDataRequest          msgRequest;
    
    /** The asynchronous (streaming) Protobuf communication stub to Query Service */
    protected final DpQueryServiceStub        stubAsync;
    
    /** The query data consumer receiving all incoming Query Service data */
    protected final Consumer<QueryData>       ifcDataSink;
    
    
    //
    // State Variables and Conditions
    //
    
    /** Has the gRPC data stream started ? */
    private boolean         bolStreamStarted = false;
    
    /** The current number of responses (data pages) received from the gRPC stream */
    private int             cntResponses = 0;
    
    /** Has the gRPC data stream completed ? */
    private CountDownLatch  monStreamCompleted = new CountDownLatch(1);
    
    /** Was successful ? Were there any errors during streaming ? */
    private ResultStatus    recResult = null;
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new concrete instance of <code>QueryResponseStreamProcessor</code> 
     * ready for processing.
     * </p>
     * <p>
     * The <code>{@link QueryRequest}</code> message is extracted from the argument <code>dpRequest</code>
     * using <code>{@link DpDataRequest#buildQueryRequest()}</code>.
     * The specific implementation of the <code>QueryResponseStreamProcessor</code> depends upon
     * the preferred gRPC data stream indicated by 
     * <code>{@link DpDataRequest#getStreamType()}</code>.
     * The concrete type is given according to the following:
     * <ul>
     * <li><code>{@link DpQueryStreamType#UNIDIRECTIONAL}</code> -> <code>{@link QueryResponseUniStreamProcessor}</code>.</li>
     * <li><code>{@link DpQueryStreamType#BIDIRECTIONAL}</code> -> <code>{@link QueryResponseBidiStreamProcessor}</code>.</li>
     * </li>
     * </ul>
     * Both concrete implementations above are contained within this source file. 
     * </p>
     * <p>
     * <h2>USE</h2>
     * The returned instance is ready for independent thread execution via the 
     * <code>{@link #run()}</code> method.  Once that method returns the gRPC stream processing
     * task is complete, either normally or terminated due to error.  At that point use the 
     * <code>{@link #isSuccess()}</code> method to determine success/failure.
     * </p>
     * 
     * @param dpRequest     the Query Service <code>DpDataRequest</code> request to process
     * @param stubAsync     the Query Service (streaming RPC) communications stub to invoke request 
     * @param ifcDataSink   the target receiving the incoming results set from Query Service
     * 
     * @return  a new unidirectional Query Service data stream processor ready for starting
     * 
     * @see #isSuccess()
     * 
     * @see QueryResponseUniStreamProcessor
     * @see QueryResponseBidiStreamProcessor
     */
    public static QueryResponseStreamProcessor  newTask(
            DpDataRequest dpRequest,
            DpQueryServiceStub  stubAsync,
            Consumer<QueryDataResponse.QueryData> ifcDataSink) 
    {
        // Extract the preferred stream type and the Protobuf query result message
        DpQueryStreamType   enmType = dpRequest.getStreamType();
        QueryDataRequest    msgRqst = dpRequest.buildQueryRequest();
        
        return switch (enmType) {
        case UNIDIRECTIONAL -> QueryResponseUniStreamProcessor.newUniTask(msgRqst, stubAsync, ifcDataSink);
        case BIDIRECTIONAL -> QueryResponseBidiStreamProcessor.newBidiTask(msgRqst, stubAsync, ifcDataSink);
        default -> throw new IllegalArgumentException("Unexpected value: " + enmType);
        };
    }
    
    /**
     * <p>
     * Creates and returns a new concrete instance of <code>QueryResponseStreamProcessor</code> 
     * ready for processing.
     * </p>
     * <p>
     * The specific implementation of the <code>QueryResponseStreamProcessor</code> depends upon
     * the argument <code>enmStreamType</code>. 
     * The concrete type is given according to the following:
     * <ul>
     * <li><code>{@link DpQueryStreamType#UNIDIRECTIONAL}</code> -> <code>{@link QueryResponseUniStreamProcessor}</code>.</li>
     * <li><code>{@link DpQueryStreamType#BIDIRECTIONAL}</code> -> <code>{@link QueryResponseBidiStreamProcessor}</code>.</li>
     * </li>
     * </ul>
     * Both concrete implementations above are contained within this source file. 
     * </p>
     * <p>
     * <h2>USE</h2>
     * The returned instance is ready for independent thread execution via the 
     * <code>{@link #run()}</code> method.  Once that method returns the gRPC stream processing
     * task is complete, either normally or terminated due to error.  At that point use the 
     * <code>{@link #isSuccess()}</code> method to determine success/failure.
     * </p>
     * 
     * @param enmStreamType data stream type to use for gRPC streaming
     * @param msgRequest    the Query Service <code>DpDataRequest</code> request to process
     * @param stubAsync     the Query Service (streaming RPC) communications stub to invoke request 
     * @param ifcDataSink   the target receiving the incoming results set from Query Service
     * 
     * @return  a new unidirectional Query Service data stream processor ready for starting
     * 
     * @see #isSuccess()
     * 
     * @see QueryResponseUniStreamProcessor
     * @see QueryResponseBidiStreamProcessor
     */
    public static QueryResponseStreamProcessor  newTask(
            DpQueryStreamType   enmStreamType,
            QueryDataRequest    msgRequest,
            DpQueryServiceStub  stubAsync,
            Consumer<QueryDataResponse.QueryData> ifcDataSink) 
    {
        // Choose stream process implementation based upon requested stream type
        return switch (enmStreamType) {
        case UNIDIRECTIONAL -> QueryResponseUniStreamProcessor.newUniTask(msgRequest, stubAsync, ifcDataSink);
        case BIDIRECTIONAL -> QueryResponseBidiStreamProcessor.newBidiTask(msgRequest, stubAsync, ifcDataSink);
        default -> throw new IllegalArgumentException("Unexpected value: " + enmStreamType);
        };
    }
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryResponseStreamProcessor</code> ready for processing.
     * </p>
     *
     * @param msgRequest    the Query Service request to process
     * @param stubAsync     the Query Service (streaming RPC) communications stub to invoke request 
     * @param ifcDataSink   the target receiving the incoming results set from Query Service
     */
    protected QueryResponseStreamProcessor(QueryDataRequest msgRequest, DpQueryServiceStub stubAsync, Consumer<QueryData> ifcDataSink) {
        this.msgRequest = msgRequest;
        this.stubAsync = stubAsync;
        this.ifcDataSink = ifcDataSink;
    }

    
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
        return (this.monStreamCompleted.getCount() == 0);
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
     * Returns the number of responses received from the Query Service so far.
     * </p>
     * <p>
     * Returns the value of attribute <code>{@link #cntResponses}</code> at the time of 
     * invocation.  If the streaming task has completed the value is the total number of 
     * <code>QueryResponse</code> messages received from the Query Service before the
     * data stream was completed or terminated due to error.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * It is possible that gRPC data streaming was interrupted due to error but some response 
     * messages were processed before stream termination.
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
    // Abstract Overrides
    //
    
    /**
     *
     * @see @see java.lang.Runnable#run()
     */
    @Override
    abstract public void run();

    /**
     * <p>
     * Signal to child classes that the request has been processed.
     * </p>
     * <p>
     * This method is called immediately after the given argument has been accepted
     * by the data consumer assigned at construction.
     * Child class may perform any post-processing and/or send acknowledgments to the
     * Query Service for further streaming operations.
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
     * If an exception is thrown by the child class the stream is assumed to be terminated
     * and the <code>{@link #monStreamCompleted}</code> is released consequently.  Child classes
     * must terminate any bidirectional streaming operation.
     * </li>
     * </ul>
     * </p>
     * 
     * @param msgRsp    the Query Service response message that was just processed
     * 
     * @throws Exception    general exception thrown by base class to indicate error
     */
    abstract protected void  requestProcessed(QueryDataResponse msgRsp) throws Exception;
    
    
    //
    // Callable<Boolean> Interface
    //

    /**
     * <p>
     * Implementation of the <code>{@link Callable}</code> interface for <code>ExecutorService</code> execution.
     * </p>
     * <p>
     * This method defers to the abstract implementation <code>{@link #run()}</code> to 
     * start the gRPC data stream then wait for completion.  The value returned by
     * <code>{@link #isSuccess()}</code> is returned.
     * </p>
     * 
     * @return  the value of <code>{@link #isSuccess()}</code>
     * 
     * @see Callable#call()
     * @see #isSuccess()
     */
    @Override
    public Boolean call() {
        this.run();
        
        return this.isSuccess();
    }
    
    
    //
    // StreamObserver<QueryDataResponse> Interface
    //
    
    /**
     * <p>
     * Processes incoming response data within the backward gRPC data stream from Query Service.
     * </p>
     * <p>
     * This method performs 2 operations:
     * <ol>
     * <li>Checks for and processes a rejected request if this is the first invocation.</li>
     * <li>Processes all data within the argument.</li>
     *   <ol>
     *   <li>Checks for response status error and processes it if present.</li>
     *   <li>Extracts <code>BucketData</code> message and passes it to consumer if present.</li>
     *   <li>Increments the data page counter if a data message was successfully consumed.</li>
     *   </ol>
     * </ol>
     * </p> 
     * <p>
     * If any errors are identified within the processing the following actions are invoked:
     * <ol>
     * <li>The result record <code>{@link #recResult}</code> is set to a failure with message.</li>
     * <li>The stream completed monitor <code>{@link #monStreamCompleted}</code> is released.</li>
     * <li>The method returns as the stream is assumed terminated.
     * </ol>
     * Note that the cause of any error can be recovered from the result record attribute.
     * </p>
     * 
     * @param msgRsp    current response message from the Query Service gRPC data stream
     *
     * @see io.grpc.stub.StreamObserver#onNext(java.lang.Object)
     */
    @Override
    public void onNext(QueryDataResponse msgRsp) {

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
            this.requestProcessed(msgRsp);
            
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
     * This operation is invoked by the Query Service to signal that all data has been
     * transmitted and the stream is no longer active.  The method performs the following
     * actions:
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
     */
    protected void  awaitCompletion() {
        
        // Block until the streaming operation is complete
        try {
            this.monStreamCompleted.await();
            
        } catch (InterruptedException e) {
            
            // The wait was interrupted - interpret this an error (perhaps originating elsewhere)
            this.recResult = ResultStatus.newFailure(JavaRuntime.getQualifiedMethodNameSimple() + " - interrupted while waiting for task completion", e);
        }
    }
    
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
    protected ResultStatus isRequestAccepted(QueryDataResponse msgRsp) {
        
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
     * Processes the data within the given Query Service response message.
     * </p>
     * <p>
     * For normal operation, a <code>QueryData</code> message is extracted from the argument 
     * then passed to the data consumer identified in construction.  A SUCCESS result
     * record is then returned indicating successful consumption of response data.
     * </p>
     * <p>
     * If the argument contains an exception(i.e., rather than data) then the method
     * extracts the status and accompanying message.  An error message is constructed
     * and returned via a FAILED result record.
     * </p>
     * 
     * @param msgRsp    a Query Service response message containing data or status error
     * 
     * @return  the SUCCESS record if data was extracted and consumed,
     *          otherwise a failure message containing the status error description 
     */
    protected ResultStatus processResponse(QueryDataResponse msgRsp) {
        
//        // Extract the query report message
//        QueryResult     msgReport = msgRsp.getQueryResult();
//        
//        // Extract the query data if present, then process
//        if (msgReport.hasQueryData()) {
//            QueryData   msgData = msgReport.getQueryData();
//            
//            this.ifcDataSink.accept(msgData);
//            
//            return ResultStatus.SUCCESS;
//        }
        
        if (msgRsp.hasQueryData()) {
            QueryData   msgData = msgRsp.getQueryData();
            
            this.ifcDataSink.accept(msgData);
            
            return ResultStatus.SUCCESS;
        }
        // Response Error - extract the details and return them
        ExceptionalResult   msgException = msgRsp.getExceptionalResult();
        String              strStatus = msgException.getMessage();
        ExceptionalResult.ExceptionalResultStatus enmStatus = msgException.getExceptionalResultStatus();
        
        String          strMsg = "Query Service reported response error: status=" + enmStatus + ", message= " + strStatus;
        ResultStatus    recErr = ResultStatus.newFailure(strMsg);
        
        return recErr;
    }
    
}


//
// Concrete Sub-Class Implementations
//

/**
 * <p>
 * Class for processing Query Service <em>unidirectional</em> gRPC response streams.
 * </p>
 * <p>
 * This implementation assumes a <em>unidirectional</em> backwards gRPC data stream from the
 * Query Service to this object.  That is, objects of <code>QueryResponseUniStreamProcessor</code>
 * represent the backward stream handle (data sink for the Query Service). All processing
 * is done in the base class <code>{@link QueryResponseStreamProcessor}</code>.
 * No processing or streaming operations are performed in the 
 * <code>{@link #requestTransmitted(QueryResponse)}</code> override.
 * </p>
 * <p>
 * This class implements the <code>{@link #run()}</code> base class requirement for independent
 * thread tasks.  The implementation here simply creates the unidirectional gRPC data stream
 * using the super class asynchronous communications stub (which also initiates the data stream)
 * then blocks on the <code>{@link #awaitCompletion()}</code> method.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 5, 2024
 *
 * @see QueryResponseStreamProcessor
 */
final class QueryResponseUniStreamProcessor extends QueryResponseStreamProcessor {

    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>QueryResponseUniStreamProcessor</code> ready for processing.
     * </p>
     * <p>
     * The returned instance is ready for independent thread execution via the 
     * <code>{@link #run()}</code> method.  Once that method returns the gRPC stream processing
     * task is complete, either normally or terminated due to error.  At that point use the 
     * <code>{@link #isSuccess()}</code> method to determine success/failure.
     * </p>
     * 
     * @param msgRequest    the Query Service request to process
     * @param stubAsync     the Query Service (streaming RPC) communications stub to invoke request 
     * @param ifcDataSink   the target receiving the incoming results set from Query Service
     * 
     * @return  a new unidirectional Query Service data stream processor ready for starting
     * 
     * @see #isSuccess()
     */
    public static QueryResponseUniStreamProcessor  newUniTask(
            QueryDataRequest msgRequest, 
            DpQueryServiceStub stubAsync, 
            Consumer<QueryData> ifcDataSink) {
        return new QueryResponseUniStreamProcessor(msgRequest, stubAsync, ifcDataSink);
    }
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryResponseUniStreamProcessor</code> ready for processing
     * unidirectional Query Service data stream.
     * </p>
     *
     * @param msgRequest    the Query Service request to process
     * @param stubAsync     the Query Service (streaming RPC) communications stub to invoke request 
     * @param ifcDataSink   the target receiving the incoming results set from Query Service
     */
    public QueryResponseUniStreamProcessor(QueryDataRequest msgRequest, DpQueryServiceStub stubAsync, Consumer<QueryData> ifcDataSink) {
        super(msgRequest, stubAsync, ifcDataSink);
    }
    
    
    // 
    // QueryResponseStreamProcessor Overrides
    //
    
    /**
     * <p>
     * Initiates the data processing task and does not return until complete or error.
     * </p>
     * <p>
     * The data process is started and then becomes autonomous.  Henceforth, incoming data
     * is extracted and forwarded to the data sink (specified at construction) until the
     * data stream is completed, or an error occurs.  
     * </p>
     * <p>
     * The Query Service request is executed on the streaming communications stub to
     * create the gRPC data stream.  For unidirectional streaming this action also initiates
     * the data stream with the Query Service, from which the results set is acquired.
     * After creating and initiating the gRPC unidirectional data stream, the method then
     * blocks on the <code>{@link #monStreamCompleted}</code> monitor until the latch
     * releases.  Thus, upon return, the task is complete (either successfully or 
     * because of an error).
     * </p>
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        
        // Invoke the gRPC data request creating and initiating the gRPC data stream
        super.stubAsync.queryDataStream(super.msgRequest, this);
        
        // Block until the streaming operation is complete
        super.awaitCompletion();
    }

    /**
     * <p>
     * Performs no operation.
     * </p>
     * <p>
     * Nothing further needs to be done for this unidirectional stream.
     * </p>
     *
     * @param   msgRsp  response message that was just processed (unused)
     * 
     * @see com.ospreydcs.dp.api.query.model.grpc.QueryResponseStreamProcessor#requestTransmitted(com.ospreydcs.dp.grpc.v1.query.QueryResponse)
     */
    @Override
    protected void requestProcessed(QueryDataResponse msgRsp) {
    }
}

/**
 * <p>
 * Class for processing Query Service <em>bidirectional</em> gRPC response streams.
 * </p>
 * <p>
 * This implementation assumes a <em>bidirectional</em> gRPC data stream to/from the
 * Query Service to this object.  Note that all subclasses of 
 * <code>QueryResponseStreamProcessor</code> represent the backward stream handle 
 * (data sink for the Query Service).  The forward stream handle is maintained as an attribute
 * within this subclass.
 * </p>
 * <p>
 * All data processing is done in the base class 
 * <code>{@link QueryResponseStreamProcessor}</code>.
 * The forward streaming operations are performed in the 
 * <code>{@link #requestTransmitted(QueryResponse)}</code> override. There a 
 * <code>{@link QueryRequest</code> message containing a <code>{@link CursorOperation}</code> 
 * message is sent to the Query Service in order to signal acknowledgment for the next 
 * response message.
 * </p>
 * <p>
 * This class implements the <code>{@link #run()}</code> base class requirement for independent
 * thread tasks.  The implementation creates the bidirectional gRPC data stream
 * using the super class asynchronous communications stub. 
 * The data stream is then initiated by calling the <code>onNext(CursorRequest)</code>
 * operation on the forward stream handle.
 * The <code>{@link #run()}</code> method then blocks on the 
 * <code>{@link #awaitCompletion()}</code> method to wait for stream completion.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 6, 2024
 *
 * @see QueryResponseStreamProcessor
 */
final class QueryResponseBidiStreamProcessor extends QueryResponseStreamProcessor {

    
    // 
    // Instance Resources
    //
    
    /** the handle to the forward gRPC stream (to Query Service) */
    private CallStreamObserver<QueryDataRequest>   hndQueryService = null;
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>QueryResponseBidiStreamProcessor</code> ready for processing
     * a bidirectional Query Service data stream.
     * </p>
     * <p>
     * The returned instance is ready for independent thread execution via the 
     * <code>{@link #run()}</code> method.  Once that method returns the gRPC stream processing
     * task is complete, either normally or terminated due to error.  At that point use the 
     * <code>{@link #isSuccess()}</code> method to determine success/failure.
     * </p>
     * 
     * @param msgRequest    the Query Service request to process
     * @param stubAsync     the Query Service (streaming RPC) communications stub to invoke request 
     * @param ifcDataSink   the target receiving the incoming results set from Query Service
     * 
     * @return  a new bidirectional Query Service data stream processor ready for starting
     * 
     * @see #isSuccess()
     */
    public static QueryResponseBidiStreamProcessor  newBidiTask(QueryDataRequest msgRequest, 
            DpQueryServiceStub stubAsync,
            Consumer<QueryData> ifcDataSink) {
        return new QueryResponseBidiStreamProcessor(msgRequest, stubAsync, ifcDataSink);
    }
    

    // 
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryResponseBidiStreamProcessor</code> ready for processing
     * a bidirectional Query Service data stream.
     * </p>
     *
     * @param msgRequest    the Query Service request to process
     * @param stubAsync     the Query Service (streaming RPC) communications stub to invoke request 
     * @param ifcDataSink   the target receiving the incoming results set from Query Service
     */
    public QueryResponseBidiStreamProcessor(QueryDataRequest msgRequest, 
            DpQueryServiceStub stubAsync,
            Consumer<QueryData> ifcDataSink) {
        super(msgRequest, stubAsync, ifcDataSink);
    }

    
    //
    // QueryResponseStreamProcessor Overrides
    //
    
    /**
     * <p>
     * Initiates the data processing task and does not return until complete or error.
     * </p>
     * <p>
     * The data process is started and then becomes autonomous.  Henceforth, incoming data
     * is extracted and forwarded to the data sink (specified at construction) until the
     * data stream is completed, or an error occurs.  
     * </p>
     * <p>
     * The gRPC bidirectional data stream is created by executing the 
     * <code>queryResponseCursor</code> operation on the asynchronous communication stub
     * within the base class.  This operation returns the forward stream handle to the
     * Query Service, this instance is the backward stream receiving response messages.
     * After creating the bidirectional stream, the query request contained in the base
     * class is sent to the Query Service on the forward stream; this action initiates the
     * data stream and responses are acquired with the <code>{@link #onNext(QueryResponse)}</code>
     * method of the base class.
     * The method then blocks on the <code>{@link #monStreamCompleted}</code> monitor until the 
     * latch releases.  Thus, upon return, the task is complete (either successfully or 
     * because of an error).
     * </p>
     *
     * @see com.ospreydcs.dp.api.query.model.grpc.QueryResponseStreamProcessor#run()
     */
    @Override
    public void run() {
        
        // Create the bidirectional gRPC data stream
        this.hndQueryService = (CallStreamObserver<QueryDataRequest>)super.stubAsync.queryDataBidiStream(this);

        // Initiate the data stream by sending the data request
        this.hndQueryService.onNext(super.msgRequest);
        
        // Block until the stream operation is complete or terminated
        this.awaitCompletion();
    }

    /**
     * <p>
     * Requests the next <code>QueryResponse</code> message from the Query Service.
     * </p>
     * <p>
     * Sends a <code>QueryRequest</code> message containing a <code>CursorOperation</code> 
     * message using the forward stream handle. If the forward stream handle 
     * <code>{@link #hndQueryService}</code> is <code>null</code> this indicates a serious
     * streaming error and this method responds by throwing an exception.
     * </p>
     * 
     * @param msgRsp    response message just processed (unused)
     * 
     * @throws Exception    the <code>{@link #hndQueryService}</code> instance is <code>null<?coce>
     * 
     * @see com.ospreydcs.dp.api.query.model.grpc.QueryResponseStreamProcessor#requestTransmitted(com.ospreydcs.dp.grpc.v1.query.QueryResponse)
     */
    @Override
    protected void requestProcessed(QueryDataResponse msgRsp) throws Exception {
        
        // Check that forward stream handle has been initialized
        if (this.hndQueryService == null) 
            throw new Exception(JavaRuntime.getQualifiedMethodNameSimple() + ": Serious streaming error - forward stream handle is null!");
        
        // Create the cursor operation query request message
        CursorOperation     msgCursor = CursorOperation
                .newBuilder()
                .setCursorOperationType(CursorOperation.CursorOperationType.CURSOR_OP_NEXT)
                .build();
        
        QueryDataRequest    msgRqst = QueryDataRequest
                .newBuilder()
                .setCursorOp(msgCursor)
                .build();
        
        // Send cursor operation message to Query Service on forward stream
        this.hndQueryService.onNext(msgRqst);
    }

}
