/*
 * Project: dp-api-common
 * File:	IDatasetCriterion.java
 * Package: com.ospreydcs.dp.api.annotate.model
 * Type: 	IDatasetCriterion
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
 * @since Feb 28, 2025
 *
 */
package com.ospreydcs.dp.api.annotate.model;

import com.ospreydcs.dp.api.common.DatasetUID;
import com.ospreydcs.dp.api.common.OwnerUID;
import com.ospreydcs.dp.grpc.v1.annotation.QueryDataSetsRequest;
import com.ospreydcs.dp.grpc.v1.annotation.QueryDataSetsRequest.QueryDataSetsCriterion;

/**
 * <p>
 * Interface exposing method for creating Protocol Buffers criterion message appropriate for a
 * particular data set request operation. 
 * </p>
 * <p>
 * Enclosed record types represent the various criterion types.  The records implement the
 * <code>{@link #build()}</code> method for the appropriate data set criterion message.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 28, 2025
 *
 */
public interface IDatasetCriterion {

    /**
     * <p>
     * Builds a Protocol Buffers criterion message appropriate for the class/record type.
     * </p>
     * 
     * @return  populated <code>QueryDataSetsCriterion</code> message 
     */
    public QueryDataSetsRequest.QueryDataSetsCriterion  build();
    
    
    //
    //  Enclosed Types
    //
    
    /** 
     * Record encapsulating data set UID criterion parameters 
     */
    public static record   IdCriterion(DatasetUID recDatasetUid) implements IDatasetCriterion {

        /**
         * @see com.ospreydcs.dp.api.annotate.model.IDatasetCriterion#build()
         */
        @Override
        public QueryDataSetsCriterion build() {
            QueryDataSetsCriterion.IdCriterion  msgUid = QueryDataSetsCriterion.IdCriterion
                    .newBuilder()
                    .setId(this.recDatasetUid.datasetUid())
                    .build();
            
            return QueryDataSetsCriterion.newBuilder().setIdCriterion(msgUid).build();
        }
    }
    
    /** 
     * Record encapsulating data set name pattern criterion parameters 
     */
    public static record   NameCriterion(String strNameRegex) implements IDatasetCriterion {

        /**
         * @see com.ospreydcs.dp.api.annotate.model.IDatasetCriterion#build()
         */
        @Override
        public QueryDataSetsCriterion build() {
            QueryDataSetsCriterion.PvNameCriterion  msgName = QueryDataSetsCriterion.PvNameCriterion
                    .newBuilder()
                    .setName(this.strNameRegex)
                    .build();
            
            return QueryDataSetsCriterion.newBuilder().setPvNameCriterion(msgName).build();
        }
    }
    
    /** 
     * Record encapsulating data set description criterion parameters 
     */
    public static record   TextCriterion(String strDescr) implements IDatasetCriterion {

        /**
         * @see com.ospreydcs.dp.api.annotate.model.IDatasetCriterion#build()
         */
        @Override
        public QueryDataSetsCriterion build() {
            QueryDataSetsCriterion.TextCriterion msgDescr = QueryDataSetsCriterion.TextCriterion
                    .newBuilder()
                    .setText(this.strDescr)
                    .build();
            
            return QueryDataSetsCriterion.newBuilder().setTextCriterion(msgDescr).build();
        }
    }
    
    /** 
     * Record encapsulating data set owner criterion parameters.  
     */
    public static record   OwnerCriterion(OwnerUID recOwnerUid) implements IDatasetCriterion {

        /** @see com.ospreydcs.dp.api.annotate.DpDatasetsRequest.ICriterion#build() */
        @Override
        public QueryDataSetsCriterion build() {
            QueryDataSetsCriterion.OwnerCriterion msgOwn = QueryDataSetsCriterion.OwnerCriterion
                    .newBuilder()
                    .setOwnerId(this.recOwnerUid.ownerUid())
                    .build();

            QueryDataSetsCriterion  msgCrt = QueryDataSetsCriterion
                    .newBuilder()
                    .setOwnerCriterion(msgOwn)
                    .build();

            return msgCrt;
        }
    };

}
