/*
 * Project: dp-api-common
 * File:	IDpQueryStreamObserver.java
 * Package: com.ospreydcs.dp.api.query.model
 * Type: 	IDpQueryStreamObserver
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
 * @since Jan 14, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model;

import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

/**
 * <p>
 * Interface implemented by types wishing to receive notifications and data from
 * a <em>Query Service</em> data stream.
 * </p>
 * <p>
 * The data stream can send messages to the implementer of this class about streaming
 * conditions, errors, stream completion, and data availability.
 * </p>
 * <p>
 * This interface is used by <code>DpQueryStreamBuffer</code> instances to allow clients to receive
 * notifications concerning streaming conditions.
 * <p>
 * <h2>NOTE:</h2>
 * The implementation of these methods should not incur significant processing time.
 * The notification should be accepted and returned so as not to interrupt the 
 * data stream.  If the notification requires
 * significant CPU resources a separate processing thread should be spawned and
 * the method returned.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 14, 2024
 */
public interface IDpQueryStreamObserver {

    /**
     * <p>
     * Called to notify that the data streaming has started.
     * </p>
     * <p>
     * The argument for the method is the data streaming buffer, which provides the
     * recipient with a handle for retrieving the buffered streaming responses.
     * </p>
     * 
     * @param bufStrm stream buffer that has contains response data
     */
    public void notifyStreamingStarted(/* DpQueryStreamBuffer bufStrm */);
    
    /**
     * <p>
     * Called whenever the original data request was rejected by Query Service.
     * </p>
     * <p>
     * If this method is called the initiating data request was rejected by the Query Service.
     * Either the request was malformed (e.g., a field was missing, invalid time range, etc.) or
     * the result set may have been empty.  See the argument for the rejection details provided
     * by the Query Service.
     * </p>
     * 
     * @param msgReject Query Service rejection cause and message
     */
    public void notifyRequestRejected(ExceptionalResult msgReject);
    
    /**
     * <p>
     * Called to notify that the given Query Service response was recovered.
     * </p>
     * <p>
     * The given data page of the results set has been streamed and is ready for
     * processing.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * Any data processing of the argument should be performed on a separate thread!
     * Clients should accept the data and return as soon as possible.  The streaming
     * service may be interrupted if the notification does not return promptly.
     * </p>
     * 
     * @param msgRsp    Query Service gRPC response message containing page of requested data
     */
    public void notifyResponseReady(QueryDataResponse msgRsp);
    
    /**
     * <p>
     * Called by the data stream to notify that streaming has completed and all data is available for
     * processing.
     * </p>
     * <p>
     * The argument for the method is the data stream buffer, which provides the
     * recipient with a handle for retrieving the acquire data pages.
     * </p>
     * 
     * @param bufStrm stream buffer that has completed and contains all requested data
     */
    public void notifyStreamingCompleted(/* DpQueryStreamBuffer bufStrm */);
    
    /**
     * <p>
     * Called to notify that an error occurred while streaming.
     * </p>
     * <p>
     * Indicates that data stream received a terminated error from the
     * <em>Query Service</em> and the data stream has stopped upon some error 
     * condition.  
     * The error condition should be described in the given in either the 
     * <code>String</code> argument or the <code>Throwable</code> argument, 
     * or both.
     * </p>
     * <p>
     * Note that there still may be data available from the data stream buffer even
     * if this method is invoked by it, data acquired before the error
     * occurred.
     * </p>
     * <p>
     * The arguments for the method 
     * are an error message string from the data stream
     * and the originating exception provided by
     * the <em>Query Service</em> causing the streaming error, respectively.
     * </p>
     * 
     * @param strMsg any error message send by the data stream
     * @param excptSrc the source of the streaming error
     */
    public void notifyStreamingError(String strMsg, Throwable excptSrc);
    
}
