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

import com.ospreydcs.dp.api.config.test.DpApiTestingConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest.CursorOperation;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

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
    public class CursorResponseBuffer implements StreamObserver<QueryResponse> {
        
        //
        // Resources
        //
        
        /** Accumulation buffer for responses */
        private final List<QueryResponse>   lstRspBuffer = new LinkedList<>();
        
        /** Completed monitor */
        private final CountDownLatch        monCompleted = new CountDownLatch(1);   

        /** The single cursor query request (send next message) */
        private final QueryRequest          msgCursorRqst = QueryRequest.newBuilder().setCursorOp(CursorOperation.CURSOR_OP_NEXT).build();

        
        //
        // Attributes
        //
        
        /** Forward data stream handle to the Query Service */
        private StreamObserver<QueryRequest>    strmForward = null;
        
        /** Error flag */
        private boolean                         bolError = false;
        
        /** Any exception thrown by the forward stream */
        private Throwable                       excpError = null;
        
        
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
        public void start(QueryRequest msgRqst) {
            this.strmForward = TestQueryService.this.connTestArchive.getStubAsync().queryResponseCursor(this);
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
        public final List<QueryResponse>  getResults() {
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
        public void onNext(QueryResponse value) {
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
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>TestQueryService</code>.
     * </p>
     * 
     * @throws DpGrpcException   unable to connect to test archive Query Service 
     */
    public TestQueryService() throws DpGrpcException {
        this.facTestArchive = DpQueryConnectionFactory.newFactory(CFG_QUERY.connection);
        this.connTestArchive = this.facTestArchive.connect();
    }

    
    //
    // Operations
    
    
    /**
     * Performs the <code>queryResponseSingle</code> operation on the <code>DpQueryServiceGrpc</code>
     * blocking stub and returns the result set (a single response message).
     * 
     * @param msgRqst   query request Protobuf message
     * 
     * @return          result set of query (single response message)
     */
    public QueryResponse    queryResponseSingle(QueryRequest msgRqst) {
        return this.connTestArchive.getStubBlock().queryResponseSingle(msgRqst);
    }
    
    /**
     * Performs the <code>queryResponseStream</code> operation on the <code>DpQueryServiceGrpc</code>
     * blocking stub and returns the result set as an ordered list.
     * 
     * @param msgRqst   query request Protobuf message
     * 
     * @return          result set of query request
     */
    public List<QueryResponse> queryResponseStream(QueryRequest msgRqst) {
        List<QueryResponse>     lstRsps = new LinkedList<>();
        
        Iterator<QueryResponse> iterRsps = this.connTestArchive.getStubBlock().queryResponseStream(msgRqst);
        while (iterRsps.hasNext()) {
            QueryResponse   msgRsp = iterRsps.next();
            
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
    public List<QueryResponse>  queryResponseCursor(QueryRequest msgRqst) throws CompletionException, InterruptedException {
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
