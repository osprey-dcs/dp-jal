/*
 * Project: dp-api-common
 * File:	DpDatasetRequest.java
 * Package: com.ospreydcs.dp.api.annotate
 * Type: 	DpDatasetRequest
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

import com.ospreydcs.dp.api.common.OwnerUID;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.annotate.DpAnnotationConfig;
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
 * <code>{@link DpDataset}</code>.
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
 *
 * @author Christopher K. Allen
 * @since Feb 27, 2025
 *
 */
public class DpDatasetRequest {
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new, empty <code>DpDatasetRequest</code> instance.
     * </p>
     * <p>
     * The returned instance contains an empty query and must be populated with request
     * criteria before offering it to the Annotation Service API.
     * </p>
     * 
     * @return a new, empty data set request instance
     */
    public static DpDatasetRequest  create() {
        return new DpDatasetRequest();
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
    
    /** List of all the data sets criteria messages forming the request */ 
    private final List<QueryDataSetsCriterion>  lstMsgCriteria = new LinkedList<>();
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new, empty <code>DpDatasetRequest</code> instance.
     * </p>
     *
     */
    public DpDatasetRequest() {
    }

    
    // 
    // Operations
    //
    
    public void addCriteriaOwner(OwnerUID recOwnerUid) {
        
        QueryDataSetsCriterion.OwnerCriterion msgOwn = QueryDataSetsCriterion.OwnerCriterion
                .newBuilder()
                .setOwnerId(recOwnerUid.ownerUid())
                .build();

        QueryDataSetsCriterion  msgCrt = QueryDataSetsCriterion
                .newBuilder()
                .setOwnerCriterion(msgOwn)
                .build();
        
        this.lstMsgCriteria.add(msgCrt);
    }
}
