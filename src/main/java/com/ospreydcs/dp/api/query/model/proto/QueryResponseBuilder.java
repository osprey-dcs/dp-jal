/*
 * Project: dp-api-common
 * File:	QueryResponseBuilder.java
 * Package: com.ospreydcs.dp.api.query.model.proto
 * Type: 	QueryResponseBuilder
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.model.time.SamplingProcess;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

/**
 *
 * @author Christopher K. Allen
 * @since Feb 5, 2024
 *
 */
public class QueryResponseBuilder {
    
    //
    // Class Types
    //
    
    
    
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
    private final BlockingQueue<QueryResponse>  queResponses = new LinkedBlockingQueue<>();
    
    /** The thread pool service for processing composite requests */
    private final ExecutorService           exeThreadPool = Executors.newFixedThreadPool(CNT_MULTISTREAM);
    
    /** The Query Service response data correlator */
    private final QueryResponseCorrelator   qrcDataCorrelator = new QueryResponseCorrelator();  
    
    
    /**
     * <p>
     * Constructs a new instance of <code>QueryResponseBuilder</code>.
     * </p>
     *
     */
    public QueryResponseBuilder(DpQueryServiceStub stubAsync) {
        this.stubAsync = stubAsync;
//        this.rqstTarget = rqstTarget;
    }

    
    //
    // Operations
    //
    
    public SamplingProcess  processRequest(DpDataRequest dpRqst) {
        
        return null;
    }
    
    
    //
    // Support Methods
    //
    
    private SamplingProcess processSingleStream() {
        
        return null;
    }
}
