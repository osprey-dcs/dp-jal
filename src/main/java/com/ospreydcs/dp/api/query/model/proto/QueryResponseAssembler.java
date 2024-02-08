/*
 * Project: dp-api-common
 * File:	QueryResponseAssembler.java
 * Package: com.ospreydcs.dp.api.query.model.proto
 * Type: 	QueryResponseAssembler
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
 * @since Feb 5, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.proto;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.model.ResultRecord;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.model.DpQueryStreamType;
import com.ospreydcs.dp.api.query.model.time.SamplingProcess;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport.QueryData;

/**
 *
 * @author Christopher K. Allen
 * @since Feb 5, 2024
 *
 */
public class QueryResponseAssembler {
    
    //
    // Class Types
    //
    
    /**
     * <p>
     * Implementation of <code>{@link Consumer}</code> interface which transfers data from gRPC
     * stream to <code>{@link QueryResponseAssembler}</code> data buffer.
     * </p>
     * <p>
     * The <code>{@link #accept(QueryData)}</code> method of this class transfers the given
     * message to the data queue buffer <code>{@link QueryResponseAssembler#queRspData}</code>
     * of the enclosing class instance.  The current number of <code>QueryData</code> messages
     * transferred is available through the <code>{@link #getMessageCount()}</code> method. 
     * </p>
     * <p>
     * Instances are intended to be passed to newly created <code>{@link QuerResponseStreamProcessor}</code>
     * instances via the creator 
     * <code>{@link QueryResponseStreamProcessor#newTask(DpDataRequest, DpQueryServiceStub, Consumer)}</code>.
     * Thus, they simply take the extracted <code>QueryData</code> messages from the stream processor
     * and add them to the queue buffer of the enclosing class instance.
     * </p>
     *  
     * @author Christopher K. Allen
     * @since Feb 8, 2024
     *
     */
    private final class QueryDataConsumer implements Consumer<QueryResponse.QueryReport.QueryData> {

        
        // 
        // Creator
        //
        
        /**
         * Creates a new instance of <code>QueryDataConsumer</code> for the given enclosing class instance.
         * 
         * @param instance  <code>QueryResponseAssemble</code> instance target of consumer
         * 
         * @return  new <code>QueryDataConsumer</code> for the given instance
         */
        public static QueryDataConsumer newConsumer(QueryResponseAssembler instance) {
            return instance.new QueryDataConsumer();
        }
        
        //
        // State Variables
        //
        
        /** Number of data messages consumed so far */
        private int             cntMsgs = 0;

        
        /**
         * <p>
         * Returns the number of <code>QueryData</code> messages transferred to the queue buffer.
         * </p>
         * 
         * @return  current number of data messages passed from the stream processor to the queue buffer
         */
        public final int getMessageCount() {
            return this.cntMsgs;
        }
        
        //
        // Consumer<QueryData> Interface
        //
        
        /**
         * Adds the given message to the data queue buffer <code>{@link QueryResponseAssembler#queRspData}</code>
         * 
         * @param msgData   data message obtained from the associated <code>{@link QueryResponseStreamProcessor}</code>.
         * 
         * @see java.util.function.Consumer#accept(java.lang.Object)
         */
        @Override
        public void accept(QueryData msgData) {
            QueryResponseAssembler.this.queRspData.add(msgData);
            
            this.cntMsgs++;
        }
        
    }
    
    /**
     * <p>
     * Independent data processing threads for the <code>QueryResponseAssembler</code>.
     * </p>
     * <p>
     * This task is performed on a separate thread to decouple the gRPC streaming of
     * <code>QueryResponse</code> messages from the Query Service from the process of
     * correlating the responses.  We do not want to interrupt the data streaming but
     * do want to began processing the data as soon as possible.
     * </p>
     * <p>
     * Performs the transfer of <code>QueryData</code> messages from the queue buffer to
     * the response correlator <code>{@link QueryResponseAssembler#qrcDataCorrelator}</code>.
     * Note that the queue buffer <code>@link {@link QueryResponseAssembler#queRspData}</code>
     * is populated by the <code>Consumer<DataQuery></code> implementation passed to any 
     * <code>{@link QueryResponseStreamProcessor}</code>.
     * </p>
     *
     * @author Christopher K. Allen
     * @since Feb 8, 2024
     *
     */
    private final class QueryDataProcessor extends Thread {
        
        
        //
        // Creator
        //
        
        /**
         * Creates a new instance of <code>QueryDataProcessor</code> for the given enclosing class instance.
         * 
         * @param instance  <code>QueryResponseAssemble</code> instance target of processor
         * 
         * @return  new <code>QueryDataProcessor</code> for the given instance
         */
        public static QueryDataProcessor newProcessor(QueryResponseAssembler instance) {
            return instance.new QueryDataProcessor();
        }

        //
        // State Variables
        //
        
        /** Number of data messages to process (unknown until set) */
        private Integer         cntMsgsMax = null;
        
        /** Number of data messages polled so far */
        private int             cntMsgsPolled = 0;
        
        /** The result of the thread task */
        private ResultRecord    recResult = null;
        
        
        //
        // Configuration
        //
        
        public void setMaxMessages(int cntMsgsMax) {
            this.cntMsgsMax = cntMsgsMax;
        }
        
        public boolean isSuccess() {
            return this.recResult.success();
        }
        
        public ResultRecord getResult() {
            return this.recResult;
        }
        
        
        //
        // Runnable Interface
        //
        
        /**
         * <p>
         * Executes the processing loop.
         * </p>
         * <p>
         * Runs a loop that polls the <code>{@link QueryResponseAssembler#queRspData}</code> buffer
         * for <code>QueryData</code> data messages.  When available, the data messages are
         * passed to the <code>{@link QueryResponseAssembler#qrcDataCorrelator}</code> for
         * processing.
         * </p>
         * <p>
         * The loop continues until the number of data messages obtained from the buffer is
         * equal to the parameter <code>{@link #cntMsgsMax}</code>, which must eventually be set.
         * The loop will also terminated if a timeout limit is reached while waiting for a
         * message to be available.  Use <code>{@link #getResult()}</code> to return the 
         * cause of any errors.
         * </p>
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            while (this.cntMsgsMax == null || this.cntMsgsPolled < this.cntMsgsMax) {
                try {
                    QueryResponse.QueryReport.QueryData msgData = QueryResponseAssembler.this.queRspData.poll(CNT_TIMEOUT, TU_TIMEOUT);
                    this.cntMsgsPolled++;

                    if (msgData == null) {
                        this.recResult = ResultRecord.newFailure("A timeout occurred while waiting for the data buffer.");

                        return;
                    }

                    // Note that this operation will block until completed
                    QueryResponseAssembler.this.qrcDataCorrelator.insertQueryData(msgData);

                } catch (InterruptedException e) {
                    this.recResult = ResultRecord.newFailure("Process interrupted while waiting for the data buffer.", e);

                    return;
                }
            }

            this.recResult = ResultRecord.SUCCESS;
        }
    }
    
    
    //
    // Application Resources
    //
    
    /** The Data Platform Query Service default parameters */
    private static final DpQueryConfig CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** Is logging active? */
    public static final boolean     BOL_LOGGING = CFG_QUERY.logging.active;
    
    
    /** Is response processing concurrency active? */
    public static final boolean     BOL_CONCURRENCY = CFG_QUERY.concurrency.active;
    
    /** Number of gRPC data streams to use for single query */
    public static final int         CNT_CONCURRENCY_THREADS = CFG_QUERY.concurrency.threadCount;
    
    
    /** Is response multi-streaming active? */
    public static final boolean     BOL_MULTISTREAM = CFG_QUERY.data.response.multistream.active;
    
    /** Maximum number of open data streams to Query Service */
    public static final int         CNT_MULTISTREAM = CFG_QUERY.data.response.multistream.maxStreams;
    
    /** Query domain size triggering multiple streaming (if active) */
    public static final long        LNG_MULTISTREAM_PIVOT = CFG_QUERY.data.response.multistream.pivotSize;
    
    /** Query domain time units used in multi-streaming pivot size */
    public static final TimeUnit    TU_MULTISTREAM_PIVOT = CFG_QUERY.data.response.multistream.pivotUnits;
    
    
    /** Is timeout limit active ? */
    public static final boolean     BOL_TIMEOUT = CFG_QUERY.timeout.active;
    
    /** Timeout limit for query operation */
    public static final long        CNT_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** Timeout unit for query operation */
    public static final TimeUnit    TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    //
    // Initializing Attributes
    //
    
//    /** The data request to be processed */
//    private final DpDataRequest         rqstTarget;
    
    /** The Query Service asynchronous communications stub */
    private final DpQueryServiceStub    stubAsync;
    
    
    //
    // Instance Resources
    //
    
    /** The queue buffering all response messages for data correlation processing */
    private final BlockingQueue<QueryData>  queRspData = new LinkedBlockingQueue<>();
    
    /** The thread pool service for processing composite requests */
    private final ExecutorService           exeThreadPool = Executors.newFixedThreadPool(CNT_MULTISTREAM);
    
    /** The Query Service response data correlator */
    private final QueryResponseCorrelator   qrcDataCorrelator = new QueryResponseCorrelator();  
    
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryResponseAssembler</code>.
     * </p>
     *
     */
    public QueryResponseAssembler(DpQueryServiceStub stubAsync) {
        this.stubAsync = stubAsync;
//        this.rqstTarget = rqstTarget;
    }

    
    //
    // Operations
    //
    
    public SamplingProcess  processRequest(DpDataRequest dpRequest) {
        
        long szRequest = dpRequest.getDomainSize(TU_MULTISTREAM_PIVOT);
        
        if (BOL_MULTISTREAM && (szRequest > LNG_MULTISTREAM_PIVOT))
            return processMultiStream(dpRequest);
        
        return processSingleStream(dpRequest);
    }
    
    
    //
    // Support Methods
    //
    
    private SamplingProcess processSingleStream(DpDataRequest dpRequest) {

        Consumer<QueryData> ifcDataSink = msgData -> { this.queRspData.add(msgData); };
        QueryDataConsumer   actDataSink = QueryDataConsumer.newConsumer(this);
        
        QueryResponseStreamProcessor    prcStream = QueryResponseStreamProcessor.newTask(dpRequest, this.stubAsync, actDataSink);
        QueryDataProcessor              prcData = QueryDataProcessor.newProcessor(this);

        prcData.start();
        prcStream.run();
        prcData.setMaxMessages(prcStream.getResponseCount());
        
        try {
            prcData.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        SamplingProcess sp = SamplingProcess.from(this.qrcDataCorrelator.getTargetSet());
        
        return sp;
    }
    
    private SamplingProcess processMultiStream(DpDataRequest dpRequest) {
        
        return null;
    }
}
