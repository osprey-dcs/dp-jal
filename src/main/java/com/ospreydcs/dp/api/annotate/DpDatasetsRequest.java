/*
 * Project: dp-api-common
 * File:	DpDatasetsRequest.java
 * Package: com.ospreydcs.dp.api.annotate
 * Type: 	DpDatasetsRequest
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
 * @since Feb 27, 2025
 *
 */
package com.ospreydcs.dp.api.annotate;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.annotate.model.IDatasetCriterion;
import com.ospreydcs.dp.api.common.DatasetUID;
import com.ospreydcs.dp.api.common.OwnerUID;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.annotate.DpAnnotationConfig;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.annotation.QueryDataSetsRequest;
import com.ospreydcs.dp.grpc.v1.annotation.QueryDataSetsRequest.QueryDataSetsCriterion;

/**
 * <p>
 * Class defining a request for data sets within the Data Platform time-series data archive.
 * </p>
 * <p>
 * Data sets are regions within the Data Platform time-series data archive.  They are identified by a
 * unique identifier (UID) and queried according to a variety of criteria.  These query criteria are 
 * encapsulated within this class.  For more information on data sets see the class documentation for
 * <code>{@link DpCreateDatasetRequest}</code>.
 * </p>
 * <p>
 * <h2>Query Criteria</h2>
 * Data set queries are defined by <em>request criteria</em>.  The following type of criteria are available
 * to formulate a request:
 * <ul>
 * <li>UID - a single data set is identified by its unique identifier assigned by the Annotation Service.</li>
 * <li>name - any data set identified by the name assigned by the client.</li>
 * <li>description - any data set matching the description provided by the client.</li>
 * <li>Owner - all data sets created by a given owner (i.e., creator) who is identified by UID.</li>
 * </ul>
 * A data set request can contain multiple criteria, and of any type.  All data sets matching the available
 * criteria of the request will be returned by the Annotation Service.  For example, clients may create
 * a data set request contain multiple data set UIDs directly identifying the collection of data sets
 * to be returned.  Alternately, an owner and name criteria can be specified in which case any data sets
 * created by the owner with the given name will be returned.  Thus, the criteria assigned to a data set
 * request can be either additive or reductive.
 * </p>
 * <p>
 * <h2>Query Creation</h2>
 * Data set query criteria are added to an <code>DpDatasetsRequest</code> instance with the methods prefixed
 * <code>addCriterion...()</code>.  The suffix indicates the criterion type being added to the query request
 * (see above).  These methods can be called repeated to create the final data set request for presentation
 * to the Annotation Service API <code>IAnnotationService</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 27, 2025
 *
 */
public class DpDatasetsRequest {
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new, empty <code>DpDatasetsRequest</code> instance.
     * </p>
     * <p>
     * The returned instance contains an empty query and must be populated with request
     * criteria before offering it to the Annotation Service API.
     * </p>
     * 
     * @return a new, empty data set request instance
     */
    public static DpDatasetsRequest  create() {
        return new DpDatasetsRequest();
    }
    
    
    //
    // Application Resources
    //
    
    /** The default Annotation Service API configuration parameters */
    private static final DpAnnotationConfig     CFG_DEF = DpApiConfig.getInstance().annotation;

    
    //
    // Class Constants
    //
    
    /** Is event logging active */
    private static final boolean    BOL_LOGGING = CFG_DEF.logging.active;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    

    //
    // Instance Attributes
    //
    
    /** List of all the data sets criteria forming the request */
    private final List<IDatasetCriterion>   lstCriteria = new LinkedList<>();
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new, empty <code>DpDatasetsRequest</code> instance.
     * </p>
     *
     */
    public DpDatasetsRequest() {
    }

    
    // 
    // Operations
    //
    
    /**
     * <p>
     * Builds and returns a Protocol Buffers message populated with the current data set request criteria.
     * </p>
     * <p>
     * This method is intended for use by the Annotation Service API <code>IAnnotationService</code> for creation
     * the Protocol Buffers message equivalent to this data set request instance.  Clients need not interact with
     * the low-level gRPC interface directly, however, the method is available if desired.
     * </p>
     *  
     * @return  a <code>QueryDataSetsRequest</code> Protocol Buffers message populated with the current criteria
     * 
     * @throws IllegalStateException    empty request, no criteria
     */
    public QueryDataSetsRequest build() throws IllegalStateException {
        
        // Check for empty request
        if (this.lstCriteria.isEmpty()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Illegal operation, empty request, no criteria.";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalStateException(strMsg);
        }
        
        // Create the request builder and populate it with current collection of criteria
        QueryDataSetsRequest.Builder    bldrRqst = QueryDataSetsRequest.newBuilder();
        
        for (IDatasetCriterion criterion : this.lstCriteria) {
            QueryDataSetsCriterion  msg = criterion.build();
            
            bldrRqst.addCriteria(msg);
        }
        
        return bldrRqst.build();
    }
    
    /**
     * <p>
     * Clears all criteria within the current data set request.
     * </p>
     * <p>
     * This operation returns the data set request instance to its initial state, that of
     * the empty request.
     * </p>
     */
    public void clear() {
        this.lstCriteria.clear();
    }
    
    
    //
    // Request Criteria 
    //
    
    /**
     * <p>
     * Adds the given data set UID to the collection of data set request criteria.
     * </p>
     * <p>
     * The data set with the given UID is directly requested and, if valid, will be returned with
     * the requested collection.
     * </p>  
     * 
     * @param recUid    the data set UID being requested 
     */
    public void addCriterionUid(DatasetUID recUid) {
        IDatasetCriterion   criterion = new IDatasetCriterion.IdCriterion(recUid);
        
        this.lstCriteria.add(criterion);
    }
    
    /**
     * <p>
     * Adds the given name to the collection of data set request criteria.
     * </p>
     * <p>
     * The given name string is assumed to be a regular expression.  Thus, the query criterion
     * is that identifying all data sets with names matching the argument as a regular expression
     * pattern.
     * </p> 
     * 
     * @param strNameRegex  the data set name pattern being requested
     */
    public void addCriterionName(String strNameRegex) {
        IDatasetCriterion  criterion = new IDatasetCriterion.NameCriterion(strNameRegex);
        
        this.lstCriteria.add(criterion);
    }
    
    /**
     * <p>
     * Adds the given data set description to the collection of data set request criteria.
     * </p>
     * <p>
     * The given description is assumed to be a regular expression.  Thus, any data sets
     * with a non-null description field matching the argument will be returned with the
     * final request.
     * </p>
     * 
     * @param strDescrRegex the data set description pattern being requested
     */
    public void addCriterionDescription(String strDescrRegex) {
        IDatasetCriterion  criterion = new IDatasetCriterion.DescrCriterion(strDescrRegex);
        
        this.lstCriteria.add(criterion);
    }
    
    /**
     * <p>
     * Adds the given owner UID to the collection of data set request criteria.
     * </p>
     * <p>
     * All data sets with the given owner UID will be identified within the final request.
     * </p>
     * 
     * @param recOwnerUid   the owner UID for all data sets being requested
     */
    public void addCriteriaOwner(OwnerUID recOwnerUid) {
        IDatasetCriterion  criterion = new IDatasetCriterion.OwnerCriterion(recOwnerUid);
        
        this.lstCriteria.add(criterion);
    }
}
