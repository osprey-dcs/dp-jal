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
 * @since Feb 7, 2025
 *
 */
package com.ospreydcs.dp.api.annotate;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import com.ospreydcs.dp.api.annotate.model.DpDataBlock;
import com.ospreydcs.dp.api.common.IDataTable;

/**
 * <p>
 * Builder class for constructing Dataset requests for the <em>Data Platform</em> Annotation Service.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 7, 2025
 *
 */
public class DpDatasetRequest {
    
    
    //
    // Internal Types
    //
    
    

    
    //
    // Instance Attributes
    //
    
    /** Required name for the new data set */
    private String              strName;
    
    /** Required UID for the owner of the new data set */
    private String              strOwnerUid;
    
    /** Optional description of the new data set */
    private String              strDescr;
    
    /** Collection of <code>DpDataBlock</code> instances forming the data set */
    private List<DpDataBlock>   lstBlocks = new LinkedList<>();
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new, empty <code>DpDatasetRequest</code> instance.
     * </p>
     * <p>
     * The new data set request contains no data blocks.  That is, there is no
     * defined region in the Data Platform time-series data archive yet defined
     * in the new request.
     * </p> 
     * 
     */
    public DpDatasetRequest(String strName, String strOwnerUid) {
        this(strName, strOwnerUid, "");
    }
    
    /**
     * <p>
     * Constructs a new, empty <code>DpDatasetRequest</code> instance.
     * </p>
     * <p>
     * The new data set request contains no data blocks.  That is, there is no
     * defined region in the Data Platform time-series data archive yet defined
     * in the new request.
     * </p> 
     *
     * @param strName       name of the new data set
     * @param strOwnerUid   UID of the new data set creator
     * @param strDescr      description of the new data set
     */
    public DpDatasetRequest(String strName, String strOwnerUid, String strDescr) {
        this.strName = strName;
        this.strOwnerUid = strOwnerUid;
        this.strDescr = strDescr;
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Adds the time-series query domain described by the given data table to the current data set.
     * </p>
     * <p>
     * The query domain of the given data table is described by the first and last timestamps of the table,
     * along with the collection of column names available from <code>{@link IDataTable#getColumnNames()}</code>.
     * </p>
     * 
     * @param tblBlock  data table defining the time-series query domain region added to current data set
     */
    public void addDomain(IDataTable tblBlock) {
        int             cntRows = tblBlock.getRowCount();
        List<String>    lstPvNms = tblBlock.getColumnNames();
        
        Instant insFirst = tblBlock.getTimestamp(0);
        Instant insLast = tblBlock.getTimestamp(cntRows-1);
        
        DpDataBlock blkNew = DpDataBlock.from(lstPvNms, insFirst, insLast);
        
        this.addBlock(blkNew);
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Adds the given data block to the current collection of data blocks within the data set.
     * </p>
     * <p>
     * If the given data block is disjoint from all other blocks within the current data set it is 
     * added directly to the current collection of blocks.  Otherwise, the given data block has a 
     * query domain intersection with at least one data block in the current collection and must
     * be processed.
     * </p>
     * <p>
     * The processing of data blocks ensures that every data block within the collection defines a unique
     * query domain (other that the time-range boundaries).
     * </p>
     * 
     * @param blkNew    data block to be added into the collection of blocks for this data set
     */
    private void addBlock(DpDataBlock blkNew) {
        
        // Check if new block is disjoint from all current blocks
        boolean bolDisjoint = this.lstBlocks.stream().allMatch(blk -> blk.isDisjoint(blkNew));
        
        if (bolDisjoint) {
            this.lstBlocks.add(blkNew);
         
            return;
        }
        
        // Must enforce that all blocks have unique query domains
        List<DpDataBlock>   lstBlksTmp = new LinkedList<>(this.lstBlocks);
        this.lstBlocks.clear();
        
        for (DpDataBlock blk : lstBlksTmp) {
            List<DpDataBlock>   lstBlksDcmp = DpDataBlock.domainDecomposition(blk, blkNew);
            
            this.lstBlocks.addAll(lstBlksDcmp);
        }
    }
}
