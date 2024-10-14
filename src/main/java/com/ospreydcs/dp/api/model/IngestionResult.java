/*
 * Project: dp-api-common
 * File:	IngestionResult.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	IngestionResult
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
 * @since Oct 9, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

/**
 * <h1>
 * Record encapsulation of an Ingestion Service data ingestion operation.
 * </h1>
 * <p>
 * This record contains the summary results of an ingestion operation by the Data Platform 
 * Ingestion Service.  There are 3 ingestion cases which it covers:
 * <ol>
 * <li>
 * Unary RPC ingestion: A single data frame is transmitted synchronous and a single response is recovered.
 * In this case the record is the Java client API abridgment of the Protocol
 * Buffers message <code>IngestDataResponse</code> returned by the Ingestion Service.
 * </li>
 * <br/>
 * <li>
 * Unidirectional gRPC ingestion: An open data stream is maintained with the Ingestion Service where 
 * an asynchronous stream of data frames is transmitted (in the forward direction) by the client.
 * Once completed, the Ingestion Service returns a single <code>{@link IngestDataStreamResponse}</code>
 * message containing the summary of that operation.  This record is essentially the Java client
 * API equivalent of that message.
 * </li>
 * <br/>
 * <li>
 * Bidirectional gRPC ingestion: An open data stream is maintained with the Ingestion Service where
 * an asynchronous stream of data frames is transmitted (in the forward direction) by the client, and
 * an asynchronous stream of responses is transmitted (in the backward direction) by the service.
 * The Ingestion Service sends one response for each data frame it receives.  This record contains
 * the aggregated result of all Ingestion Service responses.
 * </li>
 * </ol> 
 * </p>
 * <p>
 * <h2>Ingestion Responses</h2>
 * As described above, this record is intended to summarize the results of 3 very different ingestion
 * operations.
 * <h4>Unary</h4>
 * A unary response from the Ingestion Service signals acceptance or rejection of the single data frame.
 * The response contains the shape of the ingestion frame if it was accepted (i.e., the number of rows and
 * columns), otherwise it contains an exception.
 * <h4>Unidirectional</h4>
 * The single response from a unidirectional data stream contains a summary of the ingestion operations
 * while the stream was open.  This includes each ingestion frame client request ID for all frames
 * received by the Ingestion Service, and a list of the client request IDs that were rejected.
 * If any ingestion request was rejected the response contains an exception.
 * <h4>Bidirectional</h4>
 * A single bidirectional response from the Ingestion Service is a rudimentary acknowledgement of an
 * ingestion data request, specifically the request given by the <code>clientRequestId</code>
 * field.  The response, if successful, contains the number of data columns ingested along
 * with the number of rows in each column.  Here, these fields are ignored in the record as they only
 * have meaning in the context of the individual data frame.  Rather, this record should contain
 * a summary of all Ingestion responses collected during streaming.
 * </p>  
 * <p>
 * <h2>Ingestion Request UIDs</h2>
 * Within the Java Client API library the client ingestion request UID is encapsulated by the
 * <code>com.ospreydcs.dp.api.model.ClientRequestUID</code> record.  These records are generated
 * randomly for a <code>com.ospreydcs.dp.api.ingest.IngestionFrame</code> instance upon creation,
 * however, they can be explicitly set. Note that ingestion processing within the Data Platform
 * continues past the initial acceptance of an ingestion frame, thus, the data within a frame
 * may fail to archive after receipt of a <code>IngestionResponse</code>.
 * </p> 
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * Clients desiring a more detailed determination of data processing and archiving post receipt of an
 * ingestion response should retain the client request ID.  The Ingestion Service provides an API
 * for querying the ultimate fate of the ingestion.
 * </li>
 * <li>To convert from a collection of <code>{@link IngestDataResponse}</code> response messages to this record 
 * see <code>{@link ProtoMsg#toIngestionResult(List)}</code>.
 * </li>
 * <ul>
 * </p>
 *
 * @param   mode                    the mode of data ingestion 
 * @param   responseTime            timestamp of the response provided by the Ingestion Service
 * @param   acceptedRequestCount    number of accepted requests within the ingestion operation
 * @param   receivedRequestIds      collection of all client request UIDs received and reported by the Ingestion Service
 * @param   rejectedRequestIds      collection of all client request UIDs rejected during injestion process
 * @param   exceptions              a list of any reported exception to the referenced ingest data request (empty if none)
 * @param   acknowledgments         the list of acknowledgments for ingest data requests produced by the Ingestion Service
 *  
 * @author Christopher K. Allen
 * @since Oct 9, 2024
 *
 */
public record IngestionResult(
        Mode                    mode,
        Instant                 responseTime,
        int                     acceptedRequestCount,
        List<ClientRequestUID>  receivedRequestIds,
        List<ClientRequestUID>  rejectedRequestIds,
        List<Exception>         exceptions,
        List<Acknowledge>       acknowledgments
        ) 
{
    
    //
    // Constants
    //
    
    /** The single <code>NULL</code> result to be used when an ingestion operation was failed, was not performed, or undefined. */
    public static final IngestionResult     NULL = new IngestionResult(Mode.NULL, Instant.MIN, 0, List.of(), List.of(), List.of(), List.of());

    
    //
    // Creators
    //
    
    /**
     * <h1>
     * Creates a new <code>IngestionResult</code> for a <em>successful</em> unary ingestion operation.
     * </h1>
     * <p>
     * Convenience creator returning a new, immutable <code>IngestionResult</code> record with a 
     * single acknowledgment field.
     * </p>
     * 
     * @param responseTime  timestamp of the response provided by the Ingestion Service
     * @param requestId     the client request UID of the accepted operation
     * @param colCount      the number of columns within the accepted data frame 
     * @param rowCount      the number of rows within the accepted data frame
     * 
     * @return  new <code>IngestionResult</code> populated with the given ingestion result
     */
    public static IngestionResult   newUnarySuccess(Instant responseTime, ClientRequestUID requestId, int colCount, int rowCount) {
        return new IngestionResult(
                Mode.UNARY, 
                responseTime, 
                1, 
                List.of(requestId), 
                List.of(), 
                List.of(), 
                List.of(new Acknowledge(requestId, colCount, rowCount))
                );
    }
    
    /**
     * <h1>
     * Creates a new <code>IngestionResult</code> for a <em>failed</em> unary ingestion operation.
     * </h1>
     * <p>
     * Convenience creator returning a new, immutable <code>IngestionResult</code> record with a single 
     * exceptions field.
     * </p>
     * 
     * @param responseTime  timestamp of the response provided by the Ingestion Service
     * @param requestId     the client request UID of the accepted operation
     * @param strExcepType  name of the exception enumeration constant 
     * @param strExcepMsg   detail message for the exception (provided by Ingestion Service)
     * 
     * @return  new <code>IngestionResult</code> populated with the given ingestion result
     */
    public static IngestionResult   newUnaryFailed(Instant responseTime, ClientRequestUID requestId, String strExcepType, String strExcepMsg) {
        return new IngestionResult(
                Mode.UNARY, 
                responseTime, 
                0, 
                List.of(), 
                List.of(requestId), 
                List.of(new Exception(requestId, strExcepType, strExcepMsg)), 
                List.of());
    }
    
    /**
     * <h1>
     * Creates a new <code>IngestionResult</code> for a streaming ingestion operation <em>without</em> any exceptions.
     * </h1>
     * <p>
     * Convenience creator for the case of a streaming ingestion operation reporting no exceptions; specifically,
     * the <code>exceptions</code> field cannot be modified.  The provided argument list of client request UIDs
     * is used as the list of received request UIDs and the list of rejected UIDs is assumed empty (and immutable).      
     * However, the returned record <b>is not</b> immutable; acknowledgments can be added post creation using
     * <code>{@link #addAcknowledgment(ClientRequestUID, int, int)}</code>.
     * </p>
     * 
     * @param   mode                    mode of data ingestion (i.e., <code>{@link Mode#UNIDIRECTIONAL}</code> or <code>{@link Mode#BIDIRECTIONAL}</code>)
     * @param   responseTime            timestamp of the response provided by the Ingestion Service
     * @param   acceptedRequestCount    number of accepted requests within the ingestion operation
     * @param   receivedRequestIds      collection of all client request UIDs received and reported by the Ingestion Service
     * 
     * @return  new <code>IngestionResult</code> populated with the given ingestion summary results
     */
    public static IngestionResult   newStreamSuccess(Mode mode, Instant responseTime, int acceptedRequestCount, List<ClientRequestUID> receivedRequestIds) {
        return new IngestionResult(mode, 
                responseTime, 
                acceptedRequestCount, 
                receivedRequestIds, 
                List.of(), 
                List.of(), 
                new LinkedList<Acknowledge>()
                );
    }
    
    /**
     * <h1>
     * Creates a new <code>IngestionResult<code> for a streaming ingestion operation containing exceptions.
     * </h1>
     * <p>
     * Convenience creator for the case of a streaming ingestion operation containing rejected requests.
     * Creates a new, mutable <code>IngestionResult</code> record emtpy exceptions and acknowledgments fields.
     * These fields are to be populated post creation with the exceptions and
     * </p>
     * 
     * @param   mode                    mode of data ingestion (i.e., <code>{@link Mode#UNIDIRECTIONAL}</code> or <code>{@link Mode#BIDIRECTIONAL}</code>)
     * @param   responseTime            timestamp of the response provided by the Ingestion Service
     * @param   acceptedRequestCount    number of accepted requests within the ingestion operation
     * @param   receivedRequestIds      collection of all client request UIDs received and reported by the Ingestion Service
     * @param   rejectedRequestIds      collection of all client request UIDs rejected during injestion process
     * 
     * @return  new <code>IngestionResult</code> populated with the given ingestion summary results and exception
     */
    public static IngestionResult   newStreamFailed(Mode mode, Instant responseTime, int acceptedRequestCount, List<ClientRequestUID> receivedRequestIds, List<ClientRequestUID> rejectedRequestIds) {
        return new IngestionResult(
                mode, 
                responseTime, 
                acceptedRequestCount, 
                receivedRequestIds, 
                rejectedRequestIds, 
                new LinkedList<Exception>(), 
                new LinkedList<Acknowledge>()
                );
    }
    
    
    //
    // Condition Query
    //
    
    /**
     * <h1>
     * Determines whether or not an exception occurred during ingestion.
     * </h1>
     * <p>
     * If the method returns <code>true</code> the <code>exceptions</code> field is non-empty and contain
     * details of exceptions as reported by the Ingestion Service.  Otherwise the <code>exceptions</code>
     * field will be empty and the method returns <code>false</code>.
     * </p>
     * 
     * @return  <code>true</code> if any exceptions occurred during ingestion, <code>false</code> otherwise
     */
    public boolean hasException() {
        if (exceptions.isEmpty())
            return false;
        else 
            return true;
    }
    
    /**
     * <h1>
     * Determines whether or not the <code>acknowledge</code> field is populated.
     * </h2>
     * <p>
     * The <code>acknowledge</code> field has context in the cases of unary and bidirectional operations.  Thus, this 
     * method returns <code>true</code> only in the case where the <code>mode</code> field is not
     * <code>{@link Mode#UNIDIRECTIONAL}</code> and at least one ingestion frame was accepted by the Ingestion Service.
     * Otherwise the method returns <code>false</code> and the <code>acknowledge</code> field is empty.
     * </p>
     * 
     * @return  <code>true</code> for a successful unary operation, <code>false</code> otherwise
     */
    public boolean hasAcknowledge() {
        if (acknowledgments.isEmpty())
            return false;
        else
            return true;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <h1>Adds a new exception to the list of ingestion result exceptions</h1>
     * <p>
     * Used for general exceptions where the request UID is unavailable for the given exception.
     * </p>
     * <p>
     * <h2>WARNING</h2>
     * This method is only available if the mutable creator was used for the current <code>IngestionResult</code>
     * record.
     * </p>
     * 
     * @param   status      the exception status name (i.e., the name of the exception enumeration constant)
     * @param   message     exception detail message
     * 
     * @throws  UnsupportedOperationException   record was not created with <code>newStreamFailed()</code>
     */
    public void addException(String status, String message) throws UnsupportedOperationException {
        this.addException(ClientRequestUID.NULL, status, message);
    }
    
    /**
     * <h1>Adds a new exception to the list of ingestion result exceptions</h1>
     * <p>
     * Used when the request UID is available for the given exception.
     * </p>
     * <p>
     * <h2>WARNING</h2>
     * This method is only available if the mutable creator was used for the current <code>IngestionResult</code>
     * record.
     * </p>
     *  
     * @param   requestId   the ingestion request unique identifier
     * @param   status      the exception status name (i.e., the name of the exception enumeration constant)
     * @param   message     exception detail message
     * 
     * @throws  UnsupportedOperationException   record was not created with <code>newStreamFailed()</code>
     */
    public void addException(ClientRequestUID requestId, String status, String message) throws UnsupportedOperationException {
        this.exceptions.add(new Exception(requestId, status, message));
    }
    
    /**
     * <h1>Adds a new acknowledgment to the list of ingestion results acknowledgments</h1>
     * <p>
     * <h2>WARNING</h2>
     * This method is only available if the mutable creator was used for the current <code>IngestionResult</code>
     * record.
     * </p>
     * 
     * @param   requestId   the ingestion request unique identifier
     * @param   colCount    number of columns within the ingest data request (reported by Ingestion Service)
     * @param   rowCount    number of rows within the ingest data request (reported by Ingestion Service)
     * 
     * @throws UnsupportedOperationException    record was not created with <code>newStreamSuccess()</code> or <code>newStreamFailed</code>
     */
    public void addAcknowledgment(ClientRequestUID requestId, int colCount, int rowCount) throws UnsupportedOperationException {
        this.acknowledgments.add(new Acknowledge(requestId, colCount, rowCount));
    }
    
    
    //
    // Internal Types
    //
    
    /**
     * <h1>
     * Enumeration of all possible data ingestion modes.
     * <h1>
     * 
     */
    public static enum Mode {

        /**
         * Synchronous unary ingestion of a single ingestion data frame. 
         */
        UNARY,
        
        /**
         * Asynchronous, unidirectional streaming of ingestion data frames from the client to the Ingestion Service. 
         */
        UNIDIRECTIONAL,
        
        /**
         * Asynchronous, bidirectional streaming of ingestion data frames from the client and corresponding responses 
         * from the Ingestion Service. 
         */
        BIDIRECTIONAL,
        
        /**
         * Mode constant used for the <code>NULL</code> ingestion result.
         */
        NULL;
    }

    /**
     * <p>
     * Encapsulation of an Ingestion Service exception within a response message.
     * </p>
     * 
     * @param   requestId   the ingestion request unique identifier
     * @param   status      the exception status name (i.e., the name of the exception enumeration constant)
     * @param   message     exception detail message
     */
    public static record Exception(ClientRequestUID requestId, String status, String message) {
        
        /**
         * <p>
         * Constructs a new instance of <code>Exception</code> for the no exception case.
         * </p>
         */
        public Exception() {
            this(null, null, null);
        }
        
    }
    
    /**
     * <p>
     * Encapsulation of an <code>AckResult</code> Ingestion Service message within a 
     * response message.
     * </p>
     * 
     * @param   requestId   the ingestion request unique identifier
     * @param   colCount    number of columns within the ingest data request (reported by Ingestion Service)
     * @param   rowCount    number of rows within the ingest data request (reported by Ingestion Service)
     */
    public static record Acknowledge(ClientRequestUID requestId, Integer colCount, Integer rowCount) {

        /**
         * <p>
         * Constructs a new instance of <code>FrameShape</code> for the exceptional case.
         * </p>
         */
        public Acknowledge() {
            this(null, null, null);
        }
        
    };
    
}
