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

import com.ospreydcs.dp.api.common.DatasetUID;
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
     * 
     * @param rqst  request object defining the data set
     * 
     * @return  the Unique Identifier (UID) of the new data set
     */
    public DatasetUID   createDataset(DpDataset rqst) throws DpAnnotationException;
}
