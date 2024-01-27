/*
 * Project: dp-api-common
 * File:	IQueryStreamQueueBufferObserverDeprecated.java
 * Package: com.ospreydcs.dp.api.query.model
 * Type: 	IQueryStreamQueueBufferObserverDeprecated
 *
 * Copyright 2010-2022 the original author or authors.
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
 * @since Oct 1, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model;

import com.ospreydcs.dp.grpc.v1.common.RejectDetails;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

/**
 * <p>
 * Interface implemented by types wishing to receive notifications and data from
 * a <em>Query Service</em> data stream (e.g., the class <code>DpQueryStreamQueueBufferDeprecated</code>).
 * </p>
 * <p>
 * The data stream can send messages to the implementer of this class about streaming
 * conditions, errors, stream completion, and data availability.
 * </p>
 * <p>
 * <h2>NOTE:</h2>
 * The implementation of these methods should not incur significant processing time.
 * The notification should be accepted and returned so as not to interrupt the 
 * data stream.  If the notification requires
 * significant CPU resources a separate processing thread should be spawned and
 * the method returned.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Oct 1, 2022
 *
 * @deprecated  Replaced by IDpQueryStreamObserver
 */
@Deprecated(since="Jan 27, 2024", forRemoval=true)
public interface IQueryStreamQueueBufferObserverDeprecated {

    /**
     * <p>
     * Called to notify that the data streaming has started.
     * </p>
     * <p>
     * The argument for the method is the data streaming buffer, which provides the
     * recipient with a handle for retrieving the acquire data pages.
     * </p>
     * 
     * @param bufStrm stream buffer that has started and will contain buffered data
     */
    public void notifyStreamingStarted(DpQueryStreamQueueBufferDeprecated bufStrm);
    
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
    public void notifyRequestRejected(RejectDetails msgReject);
    
    /**
     * <p>
     * Called to notify that the given data set is ready.
     * </p>
     * <p>
     * The arguments for the method are the data page index within the stream buffer and the data page 
     * itself, respectively.  
     * </p>
     * 
     * @param indPage       index of the data page in the stream
     * @param msgRspData    gRPC message containing page of requested data
     */
    public void notifyDataPageReady(Integer indPage, QueryResponse.QueryReport.QueryData msgRspData);
    
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
    public void notifyStreamingCompleted(DpQueryStreamQueueBufferDeprecated bufStrm);
    
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
