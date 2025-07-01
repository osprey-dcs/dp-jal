/*
 * Project: dp-api-common
 * File:	JalQueryAppBase.java
 * Package: com.ospreydcs.dp.api.app
 * Type: 	JalQueryAppBase
 *
 * Copyright 2010-2025 the original author or authors.
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
 * @since May 28, 2025
 *
 */
package com.ospreydcs.dp.api.app;

import java.nio.BufferUnderflowException;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactoryStatic;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData;
import com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator;
import com.ospreydcs.dp.api.query.model.grpc.QueryChannel;
import com.ospreydcs.dp.api.query.model.grpc.QueryMessageBuffer;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

/**
 * <p>
 * Base class for applications built from the Java API Library and using the Data Platform Query Service.
 * </p>
 * <p>
 * This class contains common resources for JAL applications requiring the Query Service.  For example,
 * there are methods for creating a <code>{@link QueryChannel}</code> along with the required 
 * <code>{@link QueryMessageBuffer}</code>.  
 * </p> 
 *
 * @author Christopher K. Allen
 * @since May 28, 2025
 *
 */
public abstract class JalQueryAppBase<T extends JalQueryAppBase<T>> extends JalApplicationBase<T> {

    

    //
    // Application Resources
    //
    
    /** Query tools default configuration parameters */
    private static final DpQueryConfig     CFG_DEF = DpApiConfig.getInstance().query;
    

    //
    // Class Constants
    //
    
    /** Timeout limit for message queue buffer polling operation */
    public static final long        LNG_TIMEOUT_POLL = 15;
    
    /** Timeout limit units for message queue buffer polling operation */
    public static final TimeUnit    TU_TIMEOUT_POLL = TimeUnit.MILLISECONDS;
    
    
    //
    // Timeout Limits for Request Data Recovery (may exceed default API Library settings)
    //
    
    /** Timeout limit for requested data recovery */
    public static final long        LNG_TIMEOUT = CFG_DEF.timeout.limit;
    
    /** Timeout limit units for requested data recovery */
    public static final TimeUnit    TU_TIMEOUT = CFG_DEF.timeout.unit;
    
    
    
    //
    // Logging Parameters
    //
    
    /** Event logging enabled flag */
    public static final boolean     BOL_LOGGING = CFG_DEF.logging.enabled;

    /** Event logging level */
    public static final String      STR_LOGGING_LEVEL = CFG_DEF.logging.level;

    
    //
    // Instance Resources
    //
    
    /** The gRPC connection to the Query Service (default connection) */
    protected final DpQueryConnection       connQuery;
    
    
    /** The receiver of <code>QueryData</code> Protocol Buffers messages */
    protected final QueryMessageBuffer      bufDataMsgs;
    
    /** The <code>QueryChannel</code> object available for data recover and/or evaluations */
    protected final QueryChannel            chanQuery;


    /** Raw request data correlator */
    protected final RawDataCorrelator       prcrRawData;
    
    
    //
    // State Variables
    //
    
    /** Application tool resources shut down flag */
    protected boolean   bolShutdown = false;
    

    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>JalQueryAppBase</code> instance.
     * </p>
     *
     * @param clsApp    the class instance of the final application
     * 
     * @throws DpGrpcException unable to establish connection to the Query Service (see message and cause)
     */
    protected JalQueryAppBase(Class<T> clsApp) throws DpGrpcException {
        super(clsApp);
        
        // Create the channel resources and channel
        this.bufDataMsgs = QueryMessageBuffer.create();
        this.connQuery = DpQueryConnectionFactoryStatic.connect();  // throws DpGrpcException
        this.chanQuery = QueryChannel.from(this.connQuery, this.bufDataMsgs);
        
        // Set timeout for channel operation and for request operation
        this.connQuery.setTimeoutLimit(LNG_TIMEOUT, TU_TIMEOUT);
        this.chanQuery.setTimeoutLimit(LNG_TIMEOUT, TU_TIMEOUT);
        
        // Create the raw request data correlator
        this.prcrRawData = RawDataCorrelator.create();
    }

    
    //
    // JalApplicationBase Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.api.app.JalApplicationBase#isLogging()
     */
    @Override
    protected boolean   isLogging() {
        return BOL_LOGGING;
    }
    
    
    //
    // Operations
    //

    /**
     * <p>
     * Determines whether or not the evaluator has been shut down.
     * </p>
     * <p>
     * A returned value of <code>true</code> indicates that the <code>{@link #shutdown()}</code> method has been
     * invoked and the evaluator is no longer active, regardless or whether or not it has been run.
     * </p>
     * 
     * @return  <code>true</code> the evaluator has been shut down and is no longer active
     *          <code>false</code> the evaluator is still alive
     */
    public boolean  hasShutdown() {
        return this.bolShutdown;
    }

    /**
     * <p>
     * Shuts down the application resources.
     * </p>
     * <p>
     * This method should be called before the evaluator is discarded, specifically, at the end of its
     * lifetime.  The Query Service connection is terminated and the output stream is flushed and closed.
     * This is a blocking operation and does not return until all resources are released.
     * </p>
     * <p>
     * If the evaluator has already been shut down (i.e., this method has already been called) nothing is done.
     * That is, it is safe to call this method multiple times.
     * </p>
     *  
     * @throws InterruptedException     interrupted while waiting for the Query Service connection to shut down
     */
    public void shutdown() throws InterruptedException {
        
        // Check state
        if (this.bolShutdown)
            return;
        
        try {
            // Shut down the message buffer and Query Service connection
            this.bufDataMsgs.shutdownNow();
            this.connQuery.shutdownSoft();
            this.connQuery.awaitTermination();

        } catch (InterruptedException e) {
            throw e;
            
        } finally {
            
            // Close the application (output stream)
            super.close();
            
            this.bolShutdown = true;
        }
    }   

    
    //
    // Subclass Support Methods
    //

    /**
     * <p>
     * Recovers all the raw time-series for the given time-series data request.
     * </p>
     * <p>
     * Uses the <code>QueryChannel</code> and <code>QueryMessageBuffer</code> resources of the base class to perform
     * the recovery operation for the given request.  This is somewhat involved since the message buffer has state
     * dynamics that must be considered.  Specifically, we require the follow events in order:
     * <ol>
     * <li>The buffer must be activated in order to accept the incoming data messages.</li>
     * <li>The recovery operation is then started and the buffer begins receiving response data - block until complete.</li>
     * <li>The buffer shutdown operation is started on a separate thread - it is a blocking operation.</li>
     * <li>A recovery loop is entered continuing until the buffer stops supplying. </li>
     * <li>Wait until the shutdown thread completes - should be complete when buffer is empty.</li>
     * </ol>
     * Note that once the buffer is shutdown it will no longer accept new data messages, however, we do not start
     * the shutdown thread until the recovery operation has complete and the buffer should be full.  It will continue
     * to supply message in the buffer until exhausted, after which it will refuse any additional requests
     * (by throwing an <code>IllegalStateException</code>.)
     * </p>
     *  
     * @param rqst   the data request to perform
     * 
     * @return  all the recovered data for the given request
     * 
     * @throws DpQueryException         general exception during test request data recovery (see message and cause)
     * @throws IllegalStateException    data request attempt on empty message buffer
     * @throws InterruptedException     processing interruption recovering request data in message data buffer
     * @throws BufferUnderflowException message buffer not fully drained
     */
    protected List<QueryData> recoverRequestData(DpDataRequest rqst)
            throws DpQueryException, IllegalStateException, InterruptedException, BufferUnderflowException {

        // The container of recovered data for test request
        List<QueryData> lstDataMsgs = new LinkedList<>();

        // Create the buffer shutdown thread
        //  buffer shutdown operation is blocking - must be performed on separate thread
        Thread  thdShutdn = new Thread( () ->    
        { 
            try {
                this.bufDataMsgs.shutdown();
            } catch (InterruptedException e) {} 
        }
                );

        // Perform the data request to the Query Service 
        this.bufDataMsgs.activate();           // ready buffer for incoming messages
        this.chanQuery.recoverRequest(rqst);   // blocking operation, supplies messages to buffer - throws DpQueryException
        thdShutdn.start();                     // spawn the buffer shutdown thread so #isSupplying() returns false when empty                          

        // Transfer recovered data from message buffer to local list
        while (this.bufDataMsgs.isSupplying()) {
//            QueryData   msgData = this.bufDataMsgs.take(); // throws IllegalStateException, InterruptedException
            QueryData   msgData = this.bufDataMsgs.poll(); // throws IllegalStateException
            if (msgData == null)
                continue;

            lstDataMsgs.add(msgData);
        }
        thdShutdn.join();                                  // throws InterruptedException

        // Check for bad recovery state
        if (this.bufDataMsgs.isSupplying())
            throw new BufferUnderflowException();          // still have messages in buffer

        return lstDataMsgs;
    }

//    protected SortedSet<RawCorrelatedData>  correlateRequestData(DpDataRequest rqst) {
//        
//    }
}
