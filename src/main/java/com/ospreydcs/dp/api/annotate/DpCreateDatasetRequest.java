/*
 * Project: dp-api-common
 * File:	DpCreateDatasetRequest.java
 * Package: com.ospreydcs.dp.api.annotate
 * Type: 	DpCreateDatasetRequest
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.annotate.model.DpDataBlock;
import com.ospreydcs.dp.api.common.IDataTable;
import com.ospreydcs.dp.api.common.OwnerUID;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.annotate.DpAnnotationConfig;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.annotation.CreateDataSetRequest;
import com.ospreydcs.dp.grpc.v1.annotation.DataBlock;
import com.ospreydcs.dp.grpc.v1.annotation.DataSet;

/**
 * <p>
 * Builder class for constructing time-series data sets and builds data set creation request messages for the 
 * <em>Data Platform</em> Annotation Service.
 * </p>
 * <p>
 * <h2>Data Set</h2>
 * A "data set" defines a region in query space, specifically, the query space of the Data Platform 
 * time-series data archive.  <code>DpCreateDatasetRequest</code> objects are used to identify such a region
 * of query space, presumably for some type of annotation operation.
 * </p>
 * <p>
 * The <code>DpCreateDatasetRequest</code> class contains methods for defining a desired region in the time-series
 * archive domain.  Once defined, a request can be offered to the Annotation Service for formal
 * data set creation which is then identified by a Unique Identifier (UID) record <code>DatasetUID</code>.  
 * This record is used for subsequent annotation creation and retrieval.
 * </p>
 * <p>
 * <h2>Annotations</h2>
 * Annotations within the Data Platform time-series archive are attached to regions of the query space
 * described by data sets.  Thus, to create an archive annotation one must first identify the region of
 * query space being annotated.  The <code>DpCreateDatasetRequest</code> class identifies the archive region
 * and is used to build the appropriate Annotation Service request for data set definition, creation, and the 
 * return of a unique identifier (UID) for the data set.  With a valid data set UID annotations can be
 * created for the data set.  
 * </p2>
 * <p>
 * <h2>Data Blocks</h2>
 * Data sets are potentially composed of multiple "data blocks", which form a viable basis set covering 
 * the entire query domain.  Data blocks are composed of data source names and a contiguous time range 
 * interval in the query domain.  Within the Java API Library data blocks are represented by the class
 * <code>DpDataBlock</code>.  Thus, <code>DpCreateDatasetRequest</code> objects contain potentially multiple 
 * instances of <code>DpDataBlock</code> objects for their domain definition.  Typically, clients do not 
 * need to interact directly with data blocks, however, for more information on the subject see the 
 * <code>{@link DpDataBlock}</code> class documentation.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 7, 2025
 *
 * @see DpDataBlock
 */
public class DpCreateDatasetRequest {
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new, empty <code>DpCreateDatasetRequest</code> instance with the given name and owner UID.
     * </p>
     * <p>
     * The new data set request defines an empty region of time-series query space.  That is, there is no
     * region in the Data Platform time-series data archive yet defined in the new request.  All data sets
     * require a name and owner UID which is provided in the arguments.
     * </p> 
     *
     * @param strSetName    name of the new data set
     * @param recOwnerUid   UID of the new data set creator
     * 
     * @return   a new, empty <code>DpCreateDatasetRequest</code> with the given name and owner UID
     */
    public static DpCreateDatasetRequest  from(OwnerUID recOwnerUid, String strSetName) {
        return new DpCreateDatasetRequest(recOwnerUid, strSetName);
    }
    
    /**
     * <p>
     * Creates a new, empty <code>DpCreateDatasetRequest</code> instance with the given name, owner UID, and description.
     * </p>
     * <p>
     * The new data set request defines an empty region of time-series query space.  That is, there is no
     * region in the Data Platform time-series data archive yet defined in the new request.  All data sets
     * require a name and owner UID which is provided in the arguments.  The text description is an optional
     * parameter which can provide additional context for the data set.
     * </p> 
     *
     * @param strSetName    name of the new data set
     * @param recOwnerUid   UID of the new data set creator
     * @param strDescr      optional description of the new data set
     * 
     * @return   a new, empty <code>DpCreateDatasetRequest</code> with the given name, owner UID, and description
     */
    public static DpCreateDatasetRequest  from(OwnerUID recOwnerUid, String strSetName, String strDescr) {
        return new DpCreateDatasetRequest(recOwnerUid, strSetName, strDescr);
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
    
    /** Required UID for the owner of the new data set */
    private OwnerUID            recOwnerUid;
    
    /** Required name for the new data set */
    private String              strSetName;
    
    /** Optional description of the new data set */
    private String              strDescr;
    
    /** Collection of <code>DpDataBlock</code> instances forming the data set */
    private List<DpDataBlock>   lstBlocks = new LinkedList<>();
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new, empty <code>DpCreateDatasetRequest</code> instance.
     * </p>
     * <p>
     * The new data set request contains no data blocks.  That is, there is no
     * defined region in the Data Platform time-series data archive yet defined
     * in the new request.
     * </p> 
     * 
     * @param strSetName    name of the new data set
     * @param recOwnerUid   UID of the new data set creator and owner
     */
    public DpCreateDatasetRequest(OwnerUID recOwnerUid, String strSetName) {
        this(recOwnerUid, strSetName, null);
    }
    
    /**
     * <p>
     * Constructs a new, empty <code>DpCreateDatasetRequest</code> instance.
     * </p>
     * <p>
     * The new data set request contains no data blocks.  That is, there is no
     * defined region in the Data Platform time-series data archive yet defined
     * in the new request.
     * </p> 
     *
     * @param strSetName    name of the new data set
     * @param recOwnerUid   UID of the new data set creator and owner
     * @param strDescr      description of the new data set
     */
    public DpCreateDatasetRequest(OwnerUID recOwnerUid, String strSetName, String strDescr) {
        this.strSetName = strSetName;
        this.recOwnerUid = recOwnerUid;
        this.strDescr = strDescr;
    }
    
    //
    // State Query
    //
    
    /**
     * <p>
     * Returns the Unique Identifier of the data set creator and owner.
     * </p>
     * 
     * @return  data set owner UID record 
     */
    public OwnerUID getOwnerUid() {
        return this.recOwnerUid;
    }
    
    /**
     * <p>
     * Returns the name of the data set to be created.
     * </p>
     * 
     * @return  name of the data set
     */
    public String   getName() {
        return this.strSetName;
    }
    
    /**
     * <p>
     * Returns the textual description of the data set within this request.
     * </p>
     * <p>
     * Note that since data set descriptions are an optional parameter of data set request this value
     * can be empty, that is, a <code>null</code> value.
     * </p>
     * 
     * @return  description of data set, or blank.
     */
    public String   getDescription() {
        return this.strDescr;
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates a new <code>CreateDataSetRequest</code> Protocol Buffers message according to the current state of
     * this object.
     * </p>
     * <p>
     * The returned Protocol Buffers message is intended for use by the Annotation Service API in creating a new
     * data set for future annotation.  The message is used by the <code>createDataSet(CreateDataSetRequest)</code>
     * operation of the <code>DpAnnotationService</code> gRPC interface.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Empty data sets are currently invalid within the Data Platform Annotation Service and would result in an
     * exception.  Instead, this condition is caught here.  An <code>addDomain</code> method must be called at
     * least once for a valid data set.
     * </p>
     * 
     * @return  a new <code>CreateDataSetRequest</code> message populated with the current state information
     * 
     * @throws IllegalStateException    illegal data set, data set is empty
     */
    public CreateDataSetRequest buildDataSetRequest() throws IllegalStateException {
        
        if (this.lstBlocks.isEmpty()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Illegal data set, data set is empty.";
                    
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalStateException(strMsg);
        }
        
        // Create the data block messages
        List<DataBlock> lstMsgBlks = DpCreateDatasetRequest.createDataBlocks(this.lstBlocks);

        // Create data set message builder and populate
        DataSet.Builder     bldrMsgDset = DataSet.newBuilder()
                .addAllDataBlocks(lstMsgBlks)
                .setOwnerId(this.recOwnerUid.ownerUid())
                .setName(this.strSetName);
        if (this.strDescr != null)
            bldrMsgDset.setDescription(this.strDescr);
        
        DataSet             msgDset = bldrMsgDset.build();

        // Create the data set request message from the data set message
        return CreateDataSetRequest.newBuilder()
                .setDataSet(msgDset)
                .build();
    }
    
    /**
     * <p>
     * Adds the time-series query domain region described by the arguments to the current data set.
     * </p>
     * <p>
     * Adds the query domain region described by the arguments as a new data block within the current
     * data set.  This is an additive operation.  Any intersecting domains already within the data set
     * will not be duplicated.
     * </p>
     *  
     * @param strPvNames    list of data source (Process Variable) names within the region
     * @param insBegin      time range starting time instant within the query domain region
     * @param insEnd        time range final time instant within the query domain region
     */
    public void addDomain(List<String> strPvNames, Instant insBegin, Instant insEnd) {
        this.addDomain(strPvNames, TimeInterval.from(insBegin, insEnd));
    }
    
    /**
     * <p>
     * Adds the time-series query domain region described by the arguments to the current data set.
     * </p>
     * <p>
     * Adds the query domain region described by the arguments as a new data block within the current
     * data set.  This is an additive operation.  Any intersecting domains already within the data set
     * will not be duplicated.
     * </p>
     *  
     * @param strPvNames    list of data source (Process Variable) names within the region
     * @param tvlRange      time range of the query domain region
     */
    public void addDomain(List<String> strPvNames, TimeInterval tvlRange) {
        DpDataBlock   blkNew = DpDataBlock.from(strPvNames, tvlRange);
        
        this.addBlock(blkNew);;
    }
    
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
     * query domain (other that the time-range boundaries).  This method defers to 
     * <code>{@link DpDataBlock#domainDecomposition(DpDataBlock, DpDataBlock)</code> for processing
     * when the given data block has an intersection with the current collection of data blocks.
     * </p>
     * 
     * @param blkNew    data block to be added into the collection of blocks for this data set
     * 
     * @see DpDataBlock#domainDecomposition(DpDataBlock, DpDataBlock)
     */
    public void addBlock(DpDataBlock blkNew) {
        
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
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates a list of <code>DataBlock</code> Protocol Buffers message equivalent to the given argument data.
     * </p>
     * <p>
     * The returned message list is populated with all new <code>DataBlock</code> Protocol Buffers messages, one
     * for each element of the argument list.  The data block messages are all created with the companion method
     * <code>{@link #createDataBlock(DpDataBlock)}</code>.  Thus, the data block messages are direct equivalents
     * of the arguments containing equivalent data.
     * </p>
     * 
     * @param lstBlocks list of (populated) <code>DpDataBlock</code> instances
     * 
     * @return          list of equivalent <code>DataBlock</code> messages
     * 
     * @see #createDataBlock(DpDataBlock)
     */
    private static List<DataBlock>  createDataBlocks(List<DpDataBlock> lstBlocks) {
        
        // Convert API library data blocks to data block messages
        List<DataBlock> lstMsgBlks = lstBlocks
                .stream()
                .<DataBlock>map(blk -> createDataBlock(blk))
                .toList();
        
        return lstMsgBlks;
    }
    
    /**
     * <p>
     * Creates a new <code>DataBlock</code> Protocol Buffers message populated with the given argument data.
     * </p>
     * <p>
     * The returned message is essentially a direct replica of the argument value containing all the
     * equivalent data of the data block.  That is, the <code>DpDataBlock</code> argument is converted to
     * a <code>DataBlock</code> Protocol Buffers message.
     * </p> 
     * 
     * @param blk   the data block as a <code>DpDataBlock</code> object
     *  
     * @return      a <code>DataBlock</code> message equivalent to the argument data block
     */
    private static DataBlock   createDataBlock(DpDataBlock blk) {
        DataBlock.Builder   bldrMsg = DataBlock.newBuilder();
        
        bldrMsg.addAllPvNames(blk.getDataSources());
        bldrMsg.setBeginTime(ProtoMsg.from( blk.getTimeRange().begin() ));
        bldrMsg.setEndTime(ProtoMsg.from( blk.getTimeRange().end()) );
        
        return bldrMsg.build();
    }
}
