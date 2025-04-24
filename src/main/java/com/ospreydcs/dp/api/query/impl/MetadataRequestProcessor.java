/*
 * Project: dp-api-common
 * File:	MetadataRequestProcessor.java
 * Package: com.ospreydcs.dp.api.query.impl
 * Type: 	MetadataRequestProcessor
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
 * @since Apr 23, 2025
 *
 */
package com.ospreydcs.dp.api.query.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.MetadataRecord;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpMetadataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult;
import com.ospreydcs.dp.grpc.v1.query.QueryMetadataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryMetadataResponse;

/**
 * <p>
 * Class for recovering Query Service metadata requests creating the result set.
 * </p>
 * <p>
 * The <code>MetadataRequestProcessor</code> class performs 2 functions: 1) metadata request recovery 
 * and 2) result set record creation.  Internally, the operations are straightforward since metadata
 * requests are strictly unary gRPC operations.  The processing consists of the following steps:
 * <ol>
 * <li>Extraction of the <code>QueryMetadataRequest</code> message from the <code>DpMetadataRequest</code> object.</li>
 * <li>Perform the gRPC metadata request <code>QueryMetadata()</code> operation directly on the communication stub.</li>
 * <li>Extraction of the metadata from the <code>QueryMetadataResponse</code> message.</li>
 * <li>Conversion of metadata into appropriate Java <code>MetadataRecord</code> instances matching the response data.
 * </ol>
 * </p> 
 * <p>
 * <h2>Supported Metadata</h2>
 * Currently only Process Variable (PV, or data source) metadata is supported by the Data Platform.  This data is
 * returned in <code>MetadataRecord</code> instances.  Additional metadata is intended for future support, for
 * example, data providers and attributes.
 * </p>
 * <p>
 * <h2>Query Service Connection</code>
 * A <code>DpQueryConnection</code> instance must be provided to a <code>MetadataRequestProcessor</code>
 * object upon creation.  The connection instance is used for all communication with the Query Service and must
 * be active at creation.  <code>MetadataRequestProcessor</code> objects DO NOT maintain ownership of the
 * <code>QueryConnection</code> instance and, thus, it must be shut down externally when no longer needed.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Apr 23, 2025
 *
 */
public class MetadataRequestProcessor {

    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new <code>MetadataRequestProcessor</code> ready for request processing.
     * </p>
     * <p>
     * The returned instance is initialized and ready for metadata request processing.  The argument
     * must be active and connected to the Query Service.  The returned <code>MetadataRequestProcessor</code>
     * DOES NOT assume ownership of the <code>DpQueryConnection</code> argument instance, thus, it must
     * be shut down externally when no longer required.
     * </p>
     * 
     * @param connQuery active connection to the Data Platform Query Service
     * 
     * @return  a new <code>MetadataRequestProcessor</code> ready for Query Service metadata request processing
     */
    public static MetadataRequestProcessor  from(DpQueryConnection connQuery) {
        return new MetadataRequestProcessor(connQuery);
    }
    
    
    //
    // Application Resources
    //
    
    /** Default Query Service configuration parameters */
    private static final DpQueryConfig  CFG_DEF = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** Logging active flag */
    private static final boolean        BOL_LOGGING = CFG_DEF.logging.active;
    
    
    /** General query timeout limit */
    private static final long           LNG_TIMEOUT = CFG_DEF.timeout.limit;
    
    /** General query timeout units */
    private static final TimeUnit       TU_TIMEOUT = CFG_DEF.timeout.unit;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger         LOGGER = LogManager.getLogger();
    
    
    //
    // Instance Resources
    //
    
    /** The single Query Service connection used for recovering unary metadata requests */
    private final DpQueryConnection         connQuery;
    
    
    /** The number of metadata bytes recovered so far */ 
    private long    cntBytesRecovered;
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>MetadataRequestProcessor</code> ready for request processing.
     * </p>
     * <p>
     * The returned instance is initialized and ready for metadata request processing.  The argument
     * must be active and connected to the Query Service.  The returned <code>MetadataRequestProcessor</code>
     * DOES NOT assume ownership of the <code>DpQueryConnection</code> argument instance, thus, it must
     * be shut down externally when no longer required.
     * </p>
     * 
     * @param connQuery active connection to the Data Platform Query Service
     */
    public MetadataRequestProcessor(DpQueryConnection connQuery) {
        this.connQuery = connQuery;
        
        this.cntBytesRecovered = 0;
    }

    
    //
    // State Inquiry
    //

    /**
     * <p>
     * Returned the number of bytes recovered for the last request.
     * </p>
     * <p>
     * The returned value is the serialized size of all Protocol Buffers messages recovered from the
     * Query Service.   
     * The value provides an estimate of size of any results set from the last metadata request processed.
     * </p>
     * <p>
     * This value has context after a <code>{@link #processRequest(DpMetadataRequest)}</code>. 
     * It is reset after every request invocation.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Actual returned data record sizes (in bytes) are typically larger than serialized size (> ~110%).</li>
     * <li>This value is reset after invoking <code>{@link #processRequest(DpDataRequest)}</code>.</li>
     * </ul>
     * </p>
     * 
     * @return      the number of serialized bytes processed by the correlator 
     */
    public final long   getRecoveredByteCount() {
        return this.cntBytesRecovered;
    }
    

    //
    // Operations
    //
    
    /**
     * <p>
     * Recover the given metadata request, unpack Protocol Buffers message data, and create metadata records.
     * </p>
     * <p>
     * Performs the given request using gRPC unary operation blocking until request and 
     * processing of results are complete.  
     * </p>
     *  
     * @param rqst  metadata request to be recovered from Query Service and processed
     * 
     * @return  an unordered collection of <code>MetadataRecord</code> records matching the given request 
     * 
     * @throws DpQueryException general exception during data recovery and/or processing (see cause)
     */
    public List<MetadataRecord>   processRequest(DpMetadataRequest rqst) throws DpQueryException {
        
        // Get the Protobuf request from the argument
        QueryMetadataRequest    msgRqst = rqst.buildQueryRequest();
        
        // Perform gRPC request
        QueryMetadataResponse   msgRsp = this.connQuery.getStubBlock().queryMetadata(msgRqst);
        
        // Check for Query Service exception
        if (msgRsp.hasExceptionalResult()) {
            ExceptionalResult       msgExcept = msgRsp.getExceptionalResult();
            String                  strErrMsg = ProtoMsg.exceptionMessage(msgExcept, "Query Service");
            
            if (BOL_LOGGING)
                LOGGER.error(strErrMsg);
            
            throw new DpQueryException(strErrMsg);
        }
        
        // Get the number of bytes recovered
        this.cntBytesRecovered = msgRsp.getSerializedSize();

        // Unpack the response and return it
        try {
            List<MetadataRecord>      lstRecs = msgRsp
                    .getMetadataResult()
                    .getPvInfosList()
                    .stream()
                    .<MetadataRecord>map(ProtoMsg::toPvMetaRecord)
                    .toList();


            return lstRecs;

            // Any exception unpacking the Protocol Buffers messages
        } catch (IllegalArgumentException | TypeNotPresentException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                           + " - Could not convert response to MetadataRecord: "
                           + "exception=" + e.getClass().getSimpleName()
                           + ", message=" + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpQueryException(strMsg, e);
        }
    }
    
}
