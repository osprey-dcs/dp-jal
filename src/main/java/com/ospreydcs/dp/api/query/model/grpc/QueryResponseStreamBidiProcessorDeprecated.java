/*
 * Project: dp-api-common
 * File:	QueryResponseStreamBidiProcessorDeprecated.java
 * Package: com.ospreydcs.dp.api.query.model.grpc
 * Type: 	QueryResponseStreamBidiProcessorDeprecated
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

import java.util.function.Consumer;

import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest.CursorOperation;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest.CursorOperation.CursorOperationType;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

import io.grpc.stub.CallStreamObserver;

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
 * 
 * @deprecated Moved into base class source file
 */
@Deprecated(since="Feb 9, 2024", forRemoval=true)
public class QueryResponseStreamBidiProcessorDeprecated extends QueryResponseStreamProcessor {

    
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
     * Creates and returns a new instance of <code>QueryResponseStreamBidiProcessorDeprecated</code> ready for processing.
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
    public static QueryResponseStreamBidiProcessorDeprecated  newTask(QueryDataRequest msgRequest, 
            DpQueryServiceStub stubAsync,
            Consumer<QueryData> ifcDataSink) {
        return new QueryResponseStreamBidiProcessorDeprecated(msgRequest, stubAsync, ifcDataSink);
    }
    

    // 
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryResponseStreamBidiProcessorDeprecated</code> ready for processing.
     * </p>
     *
     * @param msgRequest    the Query Service request to process
     * @param stubAsync     the Query Service (streaming RPC) communications stub to invoke request 
     * @param ifcDataSink   the target receiving the incoming results set from Query Service
     */
    public QueryResponseStreamBidiProcessorDeprecated(QueryDataRequest msgRequest, 
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
            throw new Exception(JavaRuntime.getQualifiedCallerNameSimple() + ": Serious streaming error - forward stream handle is null!");
        
        // Create the cursor operation query request message
        CursorOperation     msgCursor = CursorOperation
                .newBuilder()
                .setCursorOperationType(CursorOperationType.CURSOR_OP_NEXT)
                .build();
        
        QueryDataRequest    msgRqst = QueryDataRequest
                .newBuilder()
                .setCursorOp(msgCursor)
                .build();
        
        // Send cursor operation message to Query Service on forward stream
        this.hndQueryService.onNext(msgRqst);
    }

}
