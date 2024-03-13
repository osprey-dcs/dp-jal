/*
 * Project: dp-api-common
 * File:	TestQueryService.java
 * Package: com.ospreydcs.dp.api.query.test
 * Type: 	TestQueryService
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
 * @since Jan 16, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;

import com.ospreydcs.dp.api.config.DpApiTestingConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest.CursorOperation;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest.CursorOperation.CursorOperationType;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

import io.grpc.stub.StreamObserver;

/**
 * <p>
 * Basic Data Platform Query Service interface.
 * </p>
 * Provides basic Query Service requests for testing result set processing.
 * This interface is not optimized for performance.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * Instances should be shutdown when no longer needed.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 16, 2024
 *
 */
public class TestQueryService {

    //
    // Application Resources
    //
    
    /** The query configuration for DP API testing */ 
    public static final DpApiTestingConfig.TestQuery    CFG_QUERY = DpApiTestingConfig.getInstance().testQuery;
    

    //
    // Class Types
    //

    /**
     *  Backward stream handler supporting cursor query requests to Query Service.
     *  Result sets are accumulated in a response buffer available at stream
     *  completion.
     */
    public class CursorResponseBuffer implements StreamObserver<QueryDataResponse> {
        
        //
        // Resources
        //
        
        /** Accumulation buffer for responses */
        private final List<QueryDataResponse>   lstRspBuffer = new LinkedList<>();
        
        /** Completed monitor */
        private final CountDownLatch        monCompleted = new CountDownLatch(1);   

        /** The single cursor query request (send next message) */
        private final QueryDataRequest      msgCursorRqst = QueryDataRequest
                .newBuilder()
                .setCursorOp(
                        CursorOperation
                        .newBuilder()
                        .setCursorOperationType(CursorOperationType.CURSOR_OP_NEXT)
                        .build()
                        )
                .build();

        
        //
        // Attributes
        //
        
        /** Forward data stream handle to the Query Service */
        private StreamObserver<QueryDataRequest>    strmForward = null;
        
        /** Error flag */
        private boolean                             bolError = false;
        
        /** Any exception thrown by the forward stream */
        private Throwable                           excpError = null;
        
        
        //
        // Operations
        //
        
        /**
         * Starts the bidirectional stream with the given query message on the 
         * <code>DpQueryServiceGrpc</code> asynchronous stub within the enclosing class
         * instance.
         * 
         * @param msgRqst   the query request containing the <code>QuerySpec</code> message
         */
        public void start(QueryDataRequest msgRqst) {
            
            // Perform RPC operation to get the backward stream handle
            this.strmForward = TestQueryService.this.connTestArchive.getStubAsync().queryDataBidiStream(this);
            
            // Send the data request on the handle starting the stream
            this.strmForward.onNext(msgRqst);
        }
        
        /**
         * Waits for the streaming operation to complete.
         * 
         * @return  <code>true</code> if streaming was successful, <code>false</code> otherwise
         * 
         * @throws InterruptedException     process interruption while waiting for stream to complete
         */
        public boolean  awaitCompletion() throws InterruptedException {
            this.monCompleted.await();
            
            return !this.bolError;
        }
        
        /**
         * Return the results of the query operation after {@link #awaitCompletion()} releases.
         * 
         * @return  the accumulation buffer containing the result set 
         */
        public final List<QueryDataResponse>  getResults() {
            return this.lstRspBuffer;
        }
        
        /**
         * Returns any exception thrown by the backward stream.
         * 
         * @return  exception recovered by {@link #onError(Throwable)}
         */
        public Throwable    getStreamError() {
            return this.excpError;
        }
        
        
        //
        // StreamObserver<QueryResponse> Interface
        //
        
        @Override
        public void onNext(QueryDataResponse value) {
            this.lstRspBuffer.add(value);
            this.strmForward.onNext(this.msgCursorRqst);
        }

        @Override
        public void onError(Throwable t) {
            this.excpError = t;
            this.bolError = true;
            this.monCompleted.countDown();
        }

        @Override
        public void onCompleted() {
            this.monCompleted.countDown();
        }
        
    }
    
    
    //
    // Instance Resources
    //
    
    /** Query Service connection factory to the test archive */ 
    private final DpQueryConnectionFactory  facTestArchive;
    
    /** Query Service connection to test archive */
    private final DpQueryConnection         connTestArchive;
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>TestQueryService</code>.
     * </p>
     * <p>
     * The returned instance is a Query Service API connected to the test archive
     * as defined in <code>{@link DpApiTestingConfig.TestQuery}</code>.
     * <p>
     * <p>
     * <h2>NOTES:</h2>
     * When no longer needed the returned instance should be shutdown to release
     * gRPC resources.  See <code>{@link #shutdown()}</code>.
     * </p>
     * 
     * @return  a new Query Service interface connected to the test archive
     * 
     * @throws DpGrpcException  unable to connect to test archive Query Service (see message)
     * 
     * @see #shutdown()
     */
    public static TestQueryService  newService() throws DpGrpcException {
        return new TestQueryService();
    }
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>TestQueryService</code>.
     * </p>
     * <p>
     * Instance should be shutdown using <code>{@link #shutdown()}</code> when
     * no longer needed.
     * </p>
     * 
     * @throws DpGrpcException   unable to connect to test archive Query Service 
     */
    public TestQueryService() throws DpGrpcException {
        this.facTestArchive = DpQueryConnectionFactory.newFactory(CFG_QUERY.connection);
//        this.facTestArchive = DpQueryConnectionFactory.FACTORY;
        this.connTestArchive = this.facTestArchive.connect();
    }

    
    //
    // Operations
    //
    
    /**
     * Performs the <code>queryResponseSingle</code> operation on the <code>DpQueryServiceGrpc</code>
     * blocking stub and returns the result set (a single response message).
     * 
     * @param msgRqst   query request Protobuf message
     * 
     * @return          result set of query (single response message)
     */
    public QueryDataResponse    queryResponseSingle(QueryDataRequest msgRqst) {
        return this.connTestArchive.getStubBlock().queryData(msgRqst);
    }
    
    /**
     * Performs the <code>queryResponseStream</code> operation on the <code>DpQueryServiceGrpc</code>
     * blocking stub and returns the result set as an ordered list.
     * 
     * @param msgRqst   query request Protobuf message
     * 
     * @return          result set of query request
     */
    public List<QueryDataResponse> queryResponseStream(QueryDataRequest msgRqst) {
        List<QueryDataResponse>     lstRsps = new LinkedList<>();
        
        Iterator<QueryDataResponse> iterRsps = this.connTestArchive.getStubBlock().queryDataStream(msgRqst);
        while (iterRsps.hasNext()) {
            QueryDataResponse   msgRsp = iterRsps.next();
            
            lstRsps.add(msgRsp);
        }
        
        return lstRsps;
    }
    
    /**
     * Performs the <code>queryResponseCursor</code> operation on the <code>DpQueryServiceGrpc</code>
     * asynchronous stub and returns the result set as an ordered list.
     * 
     * @param msgRqst   query request Protobuf message
     * 
     * @return          result set of query request
     * 
     * @throws CompletionException  an error occurred during the bidirectional streaming
     * @throws InterruptedException process interrupted by bidirectional stream active
     */
    public List<QueryDataResponse>  queryResponseCursor(QueryDataRequest msgRqst) throws CompletionException, InterruptedException {
        CursorResponseBuffer    strmBackward = new CursorResponseBuffer();
        
        strmBackward.start(msgRqst);
        if (strmBackward.awaitCompletion())
            return strmBackward.getResults();
        
        throw new CompletionException(strmBackward.getStreamError());
    }
    
    /**
     * Shuts down the connection to the Query Service.
     * The instance is no longer active.
     */
    public void shutdown() {
        this.connTestArchive.shutdownSoft();
    }
    
}
