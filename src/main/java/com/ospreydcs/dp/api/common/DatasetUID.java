/*
 * Project: dp-api-common
 * File:	DatasetUID.java
 * Package: com.ospreydcs.dp.api.common
 * Type: 	DatasetUID
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
package com.ospreydcs.dp.api.common;

/**
 * <p>
 * Record for the Unique Identifier of a data set within the Data Platform time-series archive.
 * </p>
 * <p>
 * The Data Platform requires "data sets" within the Annotation Service.  The definition and availability of
 * data set unique identifiers (UIDs) is required to create annotations within the time-series data archive.  
 * Specifically, annotations apply to data sets within the archive, and the data UID is used to identify the
 * data set being annotated.  Thus, it is necessary to first define a data set using the Annotation Service
 * API from which the data UID is recovered.  This record represents that data set UID. 
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * Data set UIDs are currently string valued within the Data Platform, however, this record type is used to
 * mask this low-level type in case of future modifications.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 27, 2025
 *
 */
public final record DatasetUID(String datasetUid) {

    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new <code>DatasetUID</code> record from the given argument.
     * </p>
     * 
     * @param datasetUid    data set unique identifier string
     * 
     * @return  new <code>DatasetUID</code> record initialized from the argument
     */
    public static final DatasetUID  from(String datasetUid) {
        return new DatasetUID(datasetUid);
    }
}
