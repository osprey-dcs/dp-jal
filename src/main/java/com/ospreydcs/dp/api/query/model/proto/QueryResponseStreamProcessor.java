/*
 * Project: dp-api-common
 * File:	QueryResponseStreamProcessor.java
 * Package: com.ospreydcs.dp.api.query.model.proto
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
package com.ospreydcs.dp.api.query.model.proto;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import com.ospreydcs.dp.api.model.ResultRecord;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.RejectDetails;
import com.ospreydcs.dp.grpc.v1.common.RejectDetails.RejectReason;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.QueryData;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.QueryStatus;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.QueryStatus.QueryStatusType;

import io.grpc.stub.StreamObserver;

/**
 * <p>
 * Query Service gRPC streaming response processor base class.
 * </p>
 * <p>
 * This class is intended as a base for the processing of gRPC data streams connected to the 
 * Data Platform Query Service.  It is also intended to be used as an independent thread
 * task as derived classes must implement the <code>{@link Runnable}</code> interface.
 * </p>
 * <p>
 * Note that this class functions as the backward stream for a streaming Query Service data 
 * request. It processes the <code>QueryResponse</code> messages obtained from the Data Platform
 * Query Service from a data request.  In fact, this class implements all that is required
 * from a <em>unidirectional</em> backwards gRPC data stream from the Query Service to this object.  
 * That is, instances of <code>QueryResponseStreamProcessor</code> represent the backward stream 
 * handle (data sink for the Query Service).   
 * </p>
 * <p>
 * <h2>Thread Task</h2>
 * Derived classes are intended to be spawned as independent threads and, consequently, must
 * implement the <code>{@link #run()}</code> operation of this <code>{@link Runnable}</code>
 * interface.  The <code>{@link #run()}</code> implementation should not return until the
 * the streaming operation is complete, either normally or terminated due to an error.
 * The <code>{@link #awaitCompletion()}</code> protected method is available to child classed
 * to block on this condition before returning.
 * <p>
 * <h2>Abstract Methods</h2>
 * Derived classes must override the following methods:
 * <ul>
 * <li><code>{@link #run()}</code> - creates and initializes the gRPC data stream.</li>
 * <li><code>{@link #requestProcessed(QueryResponse)}</code> - perform any data post processing 
 *           or data stream operations.</li>
 * </ul>
 * Note that the <code>{@link #run()}</code> implementation must BOTH create and initiate the 
 * gRPC data stream.  For unidirectional streams this action is one and the same.  
 * For bidirectional streams the data stream must be initiated explicitly.  Child classes
 * should then block on the <code>{@link #awaitCompletion()}</code> method until the task is
 * complete.
 * <br/> <br/>
 * Derived classes implementing a <em>bidirectional</em> stream from the Query Service must 
 * override the <code>{@link #requestProcessed(QueryResponse)}</code> method to sent a 
 * <code>CursorRequest</code> message to the Query Service in order to receive the next message.
 * Unidirectional streams need only implement an empty method if no post processing is required.
 * </p>  
 *
 * @author Christopher K. Allen
 * @since Feb 6, 2024
 *
 */
public abstract class QueryResponseStreamProcessor implements Runnable, StreamObserver<QueryResponse> {


    //
    // Initialization Targets
    //
    
    /** The query request being processed */
    protected final QueryRequest              msgRequest;
    
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
    private ResultRecord    recResult = null;
    
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryResponseStreamProcessor</code> ready for processing.
     * </p>
     *
     * @param msgRequest    the Query Service request to process
     * @param stubAsync     the Query Service (streaming RPC) communications stub to invoke request 
     * @param ifcDataSink   the target receiving the incoming results set from Query Service
     */
    protected QueryResponseStreamProcessor(QueryRequest msgRequest, DpQueryServiceStub stubAsync, Consumer<QueryData> ifcDataSink) {
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
     * @return  the result of the stream processing task as a <code>ResultRecord</code>
     * 
     * @see #isCompleted()
     */
    public final ResultRecord   getResult() {
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
    abstract protected void  requestProcessed(QueryResponse msgRsp) throws Exception;
    
    
    //
    // StreamObserver<QueryResponse> Interface
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
     *   <li>Extracts <code>QueryData</code> message and passes it to consumer if present.</li>
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
    public void onNext(QueryResponse msgRsp) {

        // Process the first response
        if (!this.bolStreamStarted) {
            this.bolStreamStarted = true;
            
            // Check for rejected request
            ResultRecord accepted = this.isRequestAccepted(msgRsp);
            
            // If the request was rejected
            // - save the rejected result record
            // - release the stream completed monitor
            // - return - the stream is over
            if (accepted.failed()) {
                this.recResult = accepted;
                
                this.monStreamCompleted.countDown();
                return;
            }
        }
        
        // Process the query response message
        ResultRecord    result = this.processResponse(msgRsp);
        
        // Check for processing failed - if so...
        // - save the failure result record
        // - release the stream completed monitor 
        // - return - the stream is over
        if (result.failed()) {
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
            this.recResult = ResultRecord.newFailure("Exception thrown during post-processing.", e);
            
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
        this.recResult = ResultRecord.newFailure("The gRPC stream was terminated during operation (see cause).", e);
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
        
        this.recResult = ResultRecord.SUCCESS;
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
            this.recResult = ResultRecord.newFailure(JavaRuntime.getQualifiedCallerNameSimple() + " - interrupted while waiting for task completion", e);
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
     * <code>QueryResponse</code> message containing a <code>RejectDetails</code> message
     * describing the rejection.
     * </p> 
     * <p>
     * The method Checks for a <code>RejectDatails</code> message within the query response 
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
    protected ResultRecord isRequestAccepted(QueryResponse msgRsp) {
        
        // Check for RequestRejected message
        if (msgRsp.hasQueryReject()) {
            RejectDetails   msgReject = msgRsp.getQueryReject();
            String          strCause = msgReject.getMessage();
            RejectReason    enmCause = msgReject.getRejectReason();
            
            String       strMsg = "The data request was rejected by Query Service: cause=" + enmCause + ", message=" + strCause;
            ResultRecord result = ResultRecord.newFailure(strMsg);
            
            return result;
        }
        
        // No rejection, return success
        return ResultRecord.SUCCESS;
    }
    
    /**
     * <p>
     * Processes the data within the given Query Service response message.
     * </p>
     * <p>
     * For normal operation <code>QueryData</code> message is extracted from the argument 
     * then passed to the data consumer identified in construction.  A SUCCESS result
     * record is then returned indicating successful consumption of response data.
     * </p>
     * <p>
     * If the argument contains an status error (i.e., rather than data) then the method
     * extracts the status error and accompanying message.  An error message is constructed
     * and returned via a FAILED result record.
     * </p>
     * 
     * @param msgRsp    a Query Service response message containing data or status error
     * 
     * @return  the SUCCESS record if data was extracted and consumed,
     *          otherwise a failure message containing the status error description 
     */
    protected ResultRecord processResponse(QueryResponse msgRsp) {
        
        // Extract the query report message
        QueryReport     msgReport = msgRsp.getQueryReport();
        
        // Extract the query data if present, then process
        if (msgReport.hasQueryData()) {
            QueryData   msgData = msgReport.getQueryData();
            
            this.ifcDataSink.accept(msgData);
            
            return ResultRecord.SUCCESS;
        }
        
        // Response Error - extract the details and return them
        QueryStatus msgStatus = msgReport.getQueryStatus();
        String          strStatus = msgStatus.getStatusMessage();
        QueryStatusType enmStatus = msgStatus.getQueryStatusType();
        
        String          strMsg = "Query Service reported response error: status=" + enmStatus + ", message= " + strStatus;
        ResultRecord    recErr = ResultRecord.newFailure(strMsg);
        
        return recErr;
    }
    
}
