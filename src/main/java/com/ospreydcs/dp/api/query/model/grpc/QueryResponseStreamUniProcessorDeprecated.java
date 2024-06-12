/*
 * Project: dp-api-common
 * File:    QueryResponseStreamUniProcessorDeprecated.java
 * Package: com.ospreydcs.dp.api.query.model.grpc
 * Type:    QueryResponseStreamUniProcessorDeprecated
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

import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

/**
 * <p>
 * Class for processing Query Service <em>unidirectional</em> gRPC response streams.
 * </p>
 * <p>
 * This implementation assumes a <em>unidirectional</em> backwards gRPC data stream from the
 * Query Service to this object.  That is, objects of <code>QueryResponseStreamUniProcessorDeprecated</code>
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
 * 
 * @deprecated Moved into base class source file
 */
@Deprecated(since="Feb 9, 2024", forRemoval=true)
public final class QueryResponseStreamUniProcessorDeprecated extends QueryResponseStreamProcessor {

    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>QueryResponseStreamUniProcessorDeprecated</code> ready for processing.
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
    public static QueryResponseStreamUniProcessorDeprecated  newTask(
            QueryDataRequest msgRequest, 
            DpQueryServiceStub stubAsync, 
            Consumer<QueryData> ifcDataSink) {
        return new QueryResponseStreamUniProcessorDeprecated(msgRequest, stubAsync, ifcDataSink);
    }
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryResponseStreamUniProcessorDeprecated</code> ready for processing.
     * </p>
     *
     * @param msgRequest    the Query Service request to process
     * @param stubAsync     the Query Service (streaming RPC) communications stub to invoke request 
     * @param ifcDataSink   the target receiving the incoming results set from Query Service
     */
    public QueryResponseStreamUniProcessorDeprecated(QueryDataRequest msgRequest, DpQueryServiceStub stubAsync, Consumer<QueryData> ifcDataSink) {
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