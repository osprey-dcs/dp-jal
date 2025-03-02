/*
 * Project: dp-api-common
 * File:	IAnnotationService.java
 * Package: com.ospreydcs.dp.api.annotate
 * Type: 	IAnnotationService
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

import java.util.List;

import com.ospreydcs.dp.api.common.DatasetUID;
import com.ospreydcs.dp.api.common.DpDataset;
import com.ospreydcs.dp.api.grpc.model.IConnection;

/**
 * <p>
 * Data Platform Annotation Service Application Programming Interface (API).
 * </p>
 * <p>
 * This interface is the primary access point for Annotation Service clients.  It exposes all
 * the fundamental operations of the Annotation Service without details of the underlying gRPC
 * interface.
 * </p>
 * <p>
 * <h2>Data Platform Annotations</h2>
 * The are currently 3 types of annotations available from the Data Platform:
 * <ol>
 * <li>Comments - simple text descriptions attached to time-series archive data.</li>
 * <li>Associates - associations/relationships between multiple regions of the time-series data archive.</li>
 * <li>Calculations - client calculations made from existing time-series data and subsequently stored within the archive.</li>
 * </ol>
 * The <code>IAnnotationService</code> interface supports the creation and retrieval of all the above annotation types.
 * </p>
 * <p>
 * <h2>Interface Operations</h2>
 * In order to create an annotation the client must first identify the time-series data within the Data Platform
 * archive being targeted.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 27, 2025
 *
 */
public interface IAnnotationService extends IConnection {

    /**
     * <p>
     * Creates a new data set within the Data Platform time-series data archive.
     * </p>
     * <p>
     * The given argument must contain a well-defined data set within the Data Platform time-series
     * data archive.  See the class documentation for <code>{@link DpCreateDatasetRequest}</code>
     * for details on data set creation.
     * </p>
     * <p>
     * Upon successful data set creation, the unique identifier (UID) of the new data set is returned
     * to the client.  The UID is assigned by the Annotation Service.  It is used to identify the region
     * of the time-series data archive in future operations, in particular, for annotations.
     * </p>
     * 
     * @param rqst  request object defining the data set
     * 
     * @return  the Unique Identifier (UID) of the new data set
     * 
     * @throws  DpAnnotationException   general exception while attempting data set creation (e.g., empty data set)
     */
    public DatasetUID       createDataset(DpCreateDatasetRequest rqst) throws DpAnnotationException;
    
    /**
     * <p>
     * Requests all data sets within the Data Platform time-series archive with the given request criteria.
     * </p>
     * <p>
     * Data sets requests are formulated using <em>request criteria</em> specific to data sets.  Multiple
     * criteria can be included in a data sets request, they are added by criterion type.  Request criterion
     * can be either inclusive or exclusive (i.e., increase or decrease the qualifying data sets, respectively)
     * depending upon the criterion type.  For more information on data sets request criteria see the class
     * documentation for <code>{@link DpDatasetsRequest}</code>.  
     * </p>
     * <p>
     * The returned data sets can be used for further processing.  For example, any annotations associated with 
     * the data set can be recovered with the data set UID.  Additional annotations can be added to a
     * data set with the data set UID.  Note also that the <code>DpDataset</code> record contains additional
     * parameters associated with data sets including the owner, the name, an optional description, and the
     * data blocks defining the data set.
     * </p>
     * 
     * @param rqst      request object containing data sets criteria
     * 
     * @return          list of all data sets matching the given request criteria
     * 
     * @throws DpAnnotationException    general exception while attempting data sets request (i.e., empty criteria)
     */
    public List<DpDataset>  queryDatasets(DpDatasetsRequest rqst) throws DpAnnotationException;
}
