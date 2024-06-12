/*
 * Project: dp-api-common
 * File:	DpMetadataRequest.java
 * Package: com.ospreydcs.dp.api.query
 * Type: 	DpMetadataRequest
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
 * @since Mar 13, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query;

import java.util.LinkedList;
import java.util.List;

import com.ospreydcs.dp.grpc.v1.query.PvNameList;
import com.ospreydcs.dp.grpc.v1.query.PvNamePattern;
import com.ospreydcs.dp.grpc.v1.query.QueryMetadataRequest;

/**
 * <p>
 * Builder class for constructing metadata requests for the Data Platform <em>Query Service</em>.
 * </p>
 * <p>
 * Query Service <em>metadata</em> refers to properties describing or inherent to the time-series data
 * archive.  The primary purpose of a metadata query is to identify characteristics of the data archive
 * used to narrow time-series data queries.
 * </p>
 * <p>
 * <h2>Metadata</h2>   
 * Currently only <em>Process Variable</em> (PV) metadata queries are available, which target the names
 * of data sources (i.e., PV names).
 * These are the names of the data sources producing the correlated time-series data within the archive.
 * In the future release other properties may become available, such as user-supplied keywords, or attributes.
 * Note, however, that the Annotation Service will provide more detailed perspectives on the data archive, 
 * in particular, properties provided by Data Platform users.
 * </p>
 * <p>
 * <h2>Objective</h2>
 * This class is used to create Query Service metadata requests without direct knowledge
 * of the underlying query mechanism.  The class exposes a set of methods that 
 * can be called to create a specific metadata request.  In this fashion the
 * query service interface can be narrowed while still providing a wide range
 * of possible queries.  
 * It also simplifies the use of the query service in that
 * the user is not subject to details of the underlying Query Service gRPC interface. 
 * Additionally, any modifications to the Query Service gRPC API can be hidden by this
 * class, presenting a consistent interface to clients.
 * </p>
 * <p>
 * <h2>Usage</h2>
 * <ul>
 * <li>
 * Currently, the only metadata available is information about process variables contributing to the Data
 * Archive.  Process variables have a unique name used as an identifier.
 * </li>
 * <br/>
 * <li>
 * Newly created <code>DpMetadataRequest</code> instances always select for the "empty query".  That is,
 * a new request will always produce a an empty result set.
 * </li>
 * <br/>
 * <li>  
 * There are current 2 types of process variable metadata queries:
 * <ol>
 * <li>Regular expression matching for process variable names.</li>
 * <li>Specifying process variables by exact name.</li>
 * </ol>
 * Regular expressions take precedence.  If a regular expression is provided for PV name matching than all 
 * previous PV name selections are ignored.  Multiple PV names may be provided when requesting information
 * using exact PV names.
 * </li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 13, 2024
 *
 */
public class DpMetadataRequest {
    
    //
    // Class Constants
    //
    
    /** Size of character buffer used for PV name regular expressions */
    public static final int     SZ_REGEX_BUFFER = 250;
    
    
    //
    // Attributes
    //
    
    /** Regular expression for match PV names */
    private String              strPvNameRegex;
    
    /** Current list of PV names for metadata request */
    private final List<String>  lstPvNames;

    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>DpMetadataRequest</code> ready for configuration.
     * </p>
     * <p>
     * Note that all newly created <code>DpMetadataRequest</code> instances are configured for the
     * "empty query".  The client must provide PV names or a regular expression to open the query.
     * </p>
     * 
     * @return  a new instance of <code>DpMetadataRequest</code> 
     */
    public static DpMetadataRequest newRequest() {
        return new DpMetadataRequest();
    }
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpMetadataRequest</code> configured for the empty request.
     * </p>
     *
     */
    public DpMetadataRequest() {
//        this.bufPvNameRegex = CharBuffer.allocate(SZ_REGEX_BUFFER);
        this.strPvNameRegex = "";
        this.lstPvNames = new LinkedList<>();
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Resets the metadata request to its initial state, the empty query.
     * </p>
     */
    public void reset() {
//        this.bufPvNameRegex.clear();
        this.strPvNameRegex = "";
        this.lstPvNames.clear();
    }

    /**
     * <p>
     * Creates a Query Service metadata request based upon the current configuration of the the request
     * builder.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>If the method <code>{@link #setPvRegex(String)}</code> was invoked the returned request specifies a regex.</li>
     * <li>Otherwise the metadata requests information on the list of PV names.</li>
     * </ul>
     * </p>
     * 
     * @return metadata request Protobuf message used by Query Service
     */
    public QueryMetadataRequest buildQueryRequest() {

        // Create request specification builder for either case
        QueryMetadataRequest.Builder    bldrRqst = QueryMetadataRequest.newBuilder();
        
        // If this is a regular expression it takes precedence
        if (!this.strPvNameRegex.isEmpty()) {
            
            bldrRqst.setPvNamePattern(PvNamePattern.newBuilder()
                    .setPattern(this.strPvNameRegex)
                    .build()
                    );
            
        // A list of PV names    
        } else {
            
            bldrRqst.setPvNameList(PvNameList
                    .newBuilder()
                    .addAllPvNames(this.lstPvNames)
                    .build()
                    );
            
        }
        
        // Create the metadata request message containing above query specification and return it
        QueryMetadataRequest    msgRqst = bldrRqst.build();
        
        return msgRqst;
    }

    
    //
    // Data Source Selection
    //
    
    /**
     * <p>
     * Set the given regular expression for process variable name matching.
     * </p>
     * <p>
     * The metadata request will return metadata records for all process variables with names matching the
     * given regular expression. The metadata request is now exclusively configured to a regular expression
     * matching request.
     * </p>  
     * <p>
     * <h2>WARNING</h2>
     * This method takes precedence over all other process variable name selection
     * operations.  That is, after invoking this method all previous metadata request configuration operations
     * are ignored.
     * </p>
     * 
     * @param strNameRegex  regular expression match process variable names
     */
    public void setPvRegex(String strNameRegex) {
//        this.bufPvNameRegex.clear();
//        this.bufPvNameRegex.put(strNameRegex);
        this.strPvNameRegex = strNameRegex;
    }
    
    /**
     * <p>
     * Request metadata for the given data source name, or Process Variable (PV) name.
     * </p>
     * <p>
     * Opens the query request to return metadata for the given data source. 
     * This method may be called repeatedly to add multiple process variables to the request.
     * </p> 
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>Regular expression cannot be used here.</li>
     * <li>Data source names must be unique within the archive.</li>
     * </ul>
     * </p>
     * 
     * @param strName    data source name
     */
    public void selectPv(String strName) {
        this.lstPvNames.add(strName);
    }
    
    /**
     * <p>
     * Request archive data for the given list of data source names.
     * </p>
     * <p>
     * Opens the query to return all metadata from data sources in the
     * given list. This method may be called repeatedly to add multiple 
     * PV name lists to the request.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>Regular expression cannot be used here.</li>
     * <li>Data source names must be unique within the archive.</li>
     * </ul>
     * </p>
     * 
     * @param lstNames list of data source names
     */
    public void selectPvs(List<String> lstNames) {
        
        this.lstPvNames.addAll(lstNames);
    }
    
}
