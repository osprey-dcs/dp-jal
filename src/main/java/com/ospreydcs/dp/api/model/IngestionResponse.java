/*
 * Project: dp-api-common
 * File:	IngestionResponse.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	IngestionResponse
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
 * @since Apr 17, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model;

import java.time.Instant;

/**
 * <p>
 * Record encapsulation of an <code>IngestDataResponse</code> message.
 * </p>
 * <p>
 * This record is essentially the client API equivalent of the Ingestion Service Protocol
 * Buffers message <code>IngestDataResponse</code>.  To convert from the message response
 * to this record see <code>{@link ProtoMsg#toIngestionResponse(IngestDataResponse)}</code>.
 * </p>
 * 
 * @param   providerId      unique identifier of the data provider supplying the data message
 * @param   clientRequestId identifier within the ingestion request message in reference to response
 * @param   responseTime    timestamp of the response, produced by the Ingestion Service
 * @param   acknowledge     acknowledgment of ingest data request produced by the Ingestion Service
 * @param   exception       any reported exception to the referenced ingest data request 
 *
 * @author Christopher K. Allen
 * @since Apr 17, 2024
 *
 */
public record IngestionResponse(
        int         providerId, 
        String      clientRequestId, 
        Instant     responseTime,
        Acknowledge acknowledge,
        Exception   exception
        ) 
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>IngestionResponse</code> record with an acknowledgement.
     * </p>
     * 
     * @param   providerId      unique identifier of the data provider supplying the data message
     * @param   clientRequestId identifier within the ingestion request message in reference to response
     * @param   responseTime    timestamp of the response, produced by the Ingestion Service
     * @param   colCount number of columns within the ingest data request (reported by Ingestion Service)
     * @param   rowCount number of rows within the ingest data request (reported by Ingestion Service)
     * 
     * @return  <code>IngestionResponse</code> record containing Ingestion Service acknowledgment
     */
    public static IngestionResponse from(int providerId, String clientRequestId, Instant responseTime, int colCount, int rowCount) {
        return new IngestionResponse(providerId, clientRequestId, responseTime, colCount, rowCount);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>IngestionResponse</code> record with an exception.
     * </p>
     * 
     * @param   providerId      unique identifier of the data provider supplying the data message
     * @param   clientRequestId identifier within the ingestion request message in reference to response
     * @param   responseTime    timestamp of the response, produced by the Ingestion Service
     * @param   type            the exception type
     * @param   message         exception detail message
     * 
     * @return  <code>IngestionResponse</code> record containing Ingestion Service exception
     */
    public static IngestionResponse from(int providerId, String clientRequestId, Instant responseTime, String type, String message) {
        return new IngestionResponse(providerId, clientRequestId, responseTime, type, message);
    }

    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionResponse</code> with an acknowledgment.
     * </p>
     *
     * @param   providerId      unique identifier of the data provider supplying the data message
     * @param   clientRequestId identifier within the ingestion request message in reference to response
     * @param   responseTime    timestamp of the response, produced by the Ingestion Service
     * @param   colCount number of columns within the ingest data request (reported by Ingestion Service)
     * @param   rowCount number of rows within the ingest data request (reported by Ingestion Service)
     */
    public IngestionResponse(int providerId, String clientRequestId, Instant responseTime, int colCount, int rowCount) {
        this(providerId, clientRequestId, responseTime, new Acknowledge(colCount, rowCount), new Exception());
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionResponse</code> with an exception.
     * </p>
     *
     * @param   providerId      unique identifier of the data provider supplying the data message
     * @param   clientRequestId identifier within the ingestion request message in reference to response
     * @param   responseTime    timestamp of the response, produced by the Ingestion Service
     * @param   type            the exception type
     * @param   message         exception detail message
     */
    public IngestionResponse(int providerId, String clientRequestId, Instant responseTime, String type, String message) {
        this(providerId, clientRequestId, responseTime, new Acknowledge(), new Exception(type, message));
    }
    
    /**
     * <p>
     * Returns whether or not an exception was reported by the Ingestion Service in reference
     * to the corresponding ingest data request.
     * </p>
     * 
     * @return  <code>true</code> if an exception was reported by the Ingestion Service,
     *          <code>false</code> the response contains an acknowledgment 
     */
    public boolean hasException() {
        return this.exception.hasException();
    }
    
    
    //
    // Internal Types
    //
    
    /**
     * <p>
     * Encapsulation of an <code>AckResult</code> Ingestion Service message within a 
     * response message.
     * </p>
     * 
     * @param   colCount number of columns within the ingest data request (reported by Ingestion Service)
     * @param   rowCount number of rows within the ingest data request (reported by Ingestion Service)
     */
    public static record Acknowledge(Integer colCount, Integer rowCount) {

        /**
         * <p>
         * Constructs a new instance of <code>Acknowledge</code> for the exceptional case.
         * </p>
         */
        public Acknowledge() {
            this(null, null);
        }
        
        /**
         * <p>
         * Returns whether or not an acknowledgment has been assigned by the Ingestion Service.
         * </p>
         * 
         * @return  <code>true</code> if the record contains an Ingestion Service acknowledgment,
         *          <code>false</code> the acknowledgment is empty (e.g., an exception was reported)
         */
        public boolean hasAcknowledge() {
            return this.colCount!=null && this.rowCount!=null;
        }
    };
    
    /**
     * <p>
     * Encapsulation of an Ingestion Service exception within a response message.
     * </p>
     * 
     * @param   flag    exception reported flag
     * @param   type    the exception type
     * @param   message exception detail message
     */
    public static record Exception(boolean flag, String type, String message) {
        
        /**
         * <p>
         * Constructs a new instance of <code>Exception</code> for the no exception case.
         * </p>
         */
        public Exception() {
            this(false, null, null);
        }
        
        /**
         * <p>
         * Constructs a new instance of <code>Exception</code> for the given exception arguments.
         * </p>
         *
         * @param   type    the exception type
         * @param   message exception detail message
         */
        public Exception(String type, String message) {
            this(true, type, message);
        }
        
        /**
         * <p>
         * Returns whether or not an exception was reported by the Ingestion Service in reference
         * to the corresponding ingest data request.
         * </p>
         * <p>
         * If the method returns <code>true</code> the canonical constructor should have been
         * called and field <code>{@link #type}</code> and <code>{@link #message}</code> should
         * be available.  If the method returns <code>false</code> the above fields should be
         * <code>null</code>.
         * </p>
         * 
         * @return  <code>true</code> if an exception was reported by the Ingestion Service,
         *          <code>false</code> otherwise
         */
        public boolean hasException() {
            return this.flag;
        }
    }

}
