/*
 * Project: dp-api-common
 * File:	IngestionFrame.java
 * Package: com.ospreydcs.dp.api.ingest.model
 * Type: 	IngestionFrame
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
 * @since Mar 29, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.model.IDataColumn;
import com.ospreydcs.dp.api.model.UniformSamplingClock;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * <h1>The unit of data ingestion for the Ingestion Service client API.</h1>
 * </p>
 * </p>
 * Defines a unit of correlated, heterogeneous, time-series data for data ingestion by the Data Platform
 * Ingestion Service.  The basic structure is a table where a table column represent each independent time-series 
 * plus an additional column of timestamps for each of the table columns.  Being correlated, each timestamp applies
 * to all table columns according to row position.
 * </p>
 * <p>
 * <h2>Sampling Clocks</h2>
 * Rather than explicitly provide the table timestamps, a <em>sampling clock</em> can be assigned.  In this case
 * the sampling is assumed to be uniform with the sampling clock specifying the sampling period and start time 
 * instant.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Mar 29, 2024
 *
 */
public class IngestionFrame {

    
    //
    // Instance Resources
    //
    
    /** Vector of column data timestamps for each data column of time-series data */
    private ArrayList<Instant>                  vecTms = null;
    
    /** Alternate sampling clock for data columns of time series data */
    private UniformSamplingClock                clkTms = null;

    
    /** Array of column data for the table - for fast column index lookup */
    private ArrayList<IDataColumn<Object>>      vecColData = null;
    
    /** Map of column names to the column data - for column name lookup  */
    private Map<String, IDataColumn<Object>>    mapNmToCol = new HashMap<>();

    
    /** Collection of (name, value) attribute pairs for the data frame */
    private Map<String, String>                 mapAttributes = new HashMap<>();

    
    /**
     * <p>
     * Constructs a new, uninitialized, instance of <code>IngestionFrame</code>.
     * </p>
     * <p>
     * Before offering the frame for ingestion the following must be established in order:
     * <ol>
     * <li>
     * The timestamps must be identified post-construction <b>either</b> by a sampling clock <b>or</b> with
     * an ordered list of timestamps.
     * </li>
     * <li>  
     * A non-empty collection of data columns must be supplied to the frame before ingestion.
     * </li>
     * <li>
     * Use <code>{@link #checkFrameConsistency()}</code> before ingestion to check frame data consistency.
     * </li>
     * </ol>
     * </p> 
     *
     */
    public IngestionFrame() {
        this.vecColData = new ArrayList<>();
    }
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>IngestionFrame</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Using this constructor creates an ingestion frame where the timestamps are identified implicitly with a 
     * uniform sampling clock.
     * </li>
     * <br/>
     * <li>
     * The arguments <b>must be consistent</b> or an exception is thrown.  The following conditions must hold:
     *   <ul>
     *   <li>The size of each data column within the argument collection must be identical.</li>  
     *   <li>The size of all data columns must be equal to the number of samples specified in the sampling clock.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     *
     * @param clk       uniform sampling clock identifying timestamps for all data columns
     * @param lstCols   data columns containing time-series data for unique data sources (identified by name)
     * 
     * @throws  IllegalArgumentException  the argument data was inconsistent (see detail message)
     * 
     * @see #checkFrameConsistency()
     */
    public IngestionFrame(UniformSamplingClock clk, ArrayList<IDataColumn<Object>> lstCols) throws IllegalArgumentException {
        this.clkTms = clk;
        this.vecColData = lstCols;
        this.mapNmToCol = this.createNmToColMap(lstCols);
        
        // Inspect argument data for inconsistency
        ResultStatus    status = this.checkFrameConsistency();
        
        if (status.isFailure())
            throw new IllegalArgumentException(status.message());
    }

    /**
     * <p>
     * Constructs a new, initialized instance of <code>IngestionFrame</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Using this constructor creates an ingestion frame where the timestamps are identified implicitly with a 
     * uniform sampling clock. Additionally, a collection of (name, value) attributes pairs is established.
     * </li>
     * <br/>
     * <li>
     * The arguments <b>must be consistent</b> or an exception is thrown.  The following conditions must hold:
     *   <ul>
     *   <li>The size of each data column within the argument collection must be identical.</li>  
     *   <li>The size of all data columns must be equal to the number of samples specified in the sampling clock.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     *
     * @param clk       uniform sampling clock identifying timestamps for all data columns
     * @param lstCols   data columns containing time-series data for unique data sources (identified by name)
     * @param mapAttrs  a map of (name,value) pairs are used as attributes for the ingestion frame   
     * 
     * @throws  IllegalArgumentException  the argument data was inconsistent (see detail message)
     * 
     * @see #checkFrameConsistency()
     */
    public IngestionFrame(UniformSamplingClock clk, ArrayList<IDataColumn<Object>> lstCols, Map<String, String> mapAttrs) 
            throws IllegalArgumentException {
        this.clkTms = clk;
        this.vecColData = lstCols;
        this.mapAttributes.putAll(mapAttrs);
        this.mapNmToCol = this.createNmToColMap(lstCols);
        
        // Inspect argument data for inconsistency
        ResultStatus    status = this.checkFrameConsistency();
        
        if (status.isFailure())
            throw new IllegalArgumentException(status.message());
    }
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>IngestionFrame</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Using this constructor creates an ingestion frame where the timestamps are identified explicitly with a 
     * an ordered list of timestamp instants.   
     * </li>
     * <br/>
     * <li>
     * The arguments <b>must be consistent</b> or an exception is thrown.  The following conditions must hold:
     *   <ul>
     *   <li>The size of each data column within the argument collection must be identical.</li>  
     *   <li>The size of all data columns must be equal to the size of the timestamp list.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     *
     * @param vecTms    an ordered vector of timestamp instances, one for each data column row
     * @param lstCols   data columns containing time-series data for unique data sources (identified by name)
     * @param mapAttrs  a map of (name,value) pairs are used as attributes for the ingestion frame   
     * 
     * @throws  IllegalArgumentException  the argument data was inconsistent (see detail message)
     * 
     * @see #checkFrameConsistency()
     */
    public IngestionFrame(ArrayList<Instant> vecTms, ArrayList<IDataColumn<Object>> lstCols) throws IllegalArgumentException {
        this(vecTms, lstCols, new HashMap<>());
    }
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>IngestionFrame</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Using this constructor creates an ingestion frame where the timestamps are identified explicitly with a 
     * an ordered list of timestamp instants.  Additionally, a collection of (name, value) attributes pairs is 
     * established.
     * </li>
     * <br/>
     * <li>
     * The arguments <b>must be consistent</b> or an exception is thrown.  The following conditions must hold:
     *   <ul>
     *   <li>The size of each data column within the argument collection must be identical.</li>  
     *   <li>The size of all data columns must be equal to the size of the timestamp list.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     *
     * @param vecTms    an ordered vector of timestamp instances, one for each data column row
     * @param lstCols   data columns containing time-series data for unique data sources (identified by name)
     * @param mapAttrs  a map of (name,value) pairs are used as attributes for the ingestion frame   
     * 
     * @throws  IllegalArgumentException  the argument data was inconsistent (see detail message)
     * 
     * @see #checkFrameConsistency()
     */
    public IngestionFrame(ArrayList<Instant> vecTms, ArrayList<IDataColumn<Object>> lstCols, Map<String, String> mapAttrs) 
            throws IllegalArgumentException {
     
        this.vecTms = vecTms;
        this.vecColData = lstCols;
        this.mapAttributes.putAll(mapAttrs);
        this.mapNmToCol = this.createNmToColMap(lstCols);

        // Inspect argument data for inconsistency
        ResultStatus    status = this.checkFrameConsistency();
        
        if (status.isFailure())
            throw new IllegalArgumentException(status.message());
    }

    //
    // Frame Population
    //
    
    /**
     * <p>
     * Assigns a sampling clock to this (uninitialized) ingestion frame.
     * </p>
     * <p>
     * This method appropriate when the frame is created using the default constructor 
     * <code>{@link #IngestionFrame()}</code>.  The timestamps for the ingestion frame must be assigned
     * by either this method or <code>{@link #assignTimestampList(ArrayList)}</code>.  
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>
     * This must be the first action when populating an ingestion frame after default construction.
     * </li>
     * <li>
     * All data columns added to the frame henceforth must have size equal to the parameter 
     * <code>{@link UniformSamplingClock#getSampleCount()}</code>.
     * </li>
     * </ul>
     * </p>
     * 
     * @param clk   the uniform sampling clock used to assign timestamps for the frame.
     * 
     * @throws IllegalStateException    timestamps have already been assigned or the frame contains data (see message)
     */
    public void assignSamplingClock(UniformSamplingClock clk) throws IllegalStateException {
        
        // Check for previous timestamp specification
        if (this.clkTms != null)
            throw new IllegalStateException("Sampling clock has already been assigned for this frame.");
        if (this.vecTms != null)
            throw new IllegalStateException("A timestamp list has already been assigned for this frame.");
        if (this.vecColData!=null && !this.vecColData.isEmpty())
            throw new IllegalStateException("Cannot assign sampling clock, frame has established data columns.");
        
        this.clkTms = clk;
    }
    
//    public ResultStatus setSamplingClock(UniformSamplingClock clk) {
//        
//        // Check for previous timestamp specification
//        if (this.clkTms != null)
//            return ResultStatus.newFailure("Sampling clock has already been assigned for this frame.");
//        if (this.vecTms != null)
//            return ResultStatus.newFailure("A timestamp list has already been assigned for this frame.");
//        if (this.vecColData!=null && !this.vecColData.isEmpty())
//            return ResultStatus.newFailure("Cannot assign sampling clock, frame has established data columns.");
//        
//        this.clkTms = clk;
//        
//        return ResultStatus.SUCCESS;
//    }

    /**
     * <p>
     * Assigns all timestamps for the this (uninitialized) ingestion frame.
     * </p>
     * <p>
     * This method appropriate when the frame is created using the default constructor 
     * <code>{@link #IngestionFrame()}</code>.  The timestamps for the ingestion frame must be assigned
     * by either this method or <code>{@link #assignSamplingClock(UniformSamplingClock)}</code>.  
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>
     * This must be the first action when populating an ingestion frame after default construction.
     * </li>
     * <li>
     * All data columns added to the frame henceforth must have size equal to the size of the timestamp list
     * argument.
     * </li>
     * </ul>
     * </p>
     * 
     * @param vecTms    ordered list of explicit timestamps for all data columns 
     * 
     * @throws IllegalStateException    timestamps have already been assigned or the frame contains data (see message)
     */
    public void assignTimestampList(ArrayList<Instant> vecTms) throws IllegalStateException {
        
        // Check for previous timestamp assignment
        if (this.vecTms != null)
            throw new IllegalStateException("A timestamp list has already been assigned for this frame.");
        if (this.clkTms != null)
            throw new IllegalStateException("A sampling clock has already been assigned for this frame.");
        if (this.vecColData!=null && !this.vecColData.isEmpty())
            throw new IllegalStateException("Cannot assign assign timestamp list, frame has established data columns.");

        this.vecTms = vecTms;
    }
    
    /**
     * <p>
     * Adds a single data column of time-series data to the ingestion frame.
     * </p>
     * <p>
     * This method appropriate when the frame is created using the default constructor 
     * <code>{@link #IngestionFrame()}</code>, but can also be used to add time-series data after an
     * initializing construction.  
     * The timestamps for the ingestion frame must be assigned first
     * before invoking this method or an exception is thrown.  The argument must have the correct size 
     * (i.e., the timestamp count) or an exception is thrown.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>
     * This action must follow that of timestamp assignment when populating an ingestion frame after default 
     * construction.
     * </li>
     * <li>
     * All data columns added to the frame must have the same size, and that size must equal to number of 
     * timestamp within the frame.
     * </li>
     * <li>
     * This method can be called as many times as necessary to fully populate the data frame with time-series
     * data.
     * </ul>
     * </p>
     * 
     * @param col   column of time-series data to be added to ingestion frame
     * 
     * @throws IllegalStateException    timestamps for the ingestion frame have not been previously assigned
     * @throws IllegalArgumentException the argument has the wrong size (not equal to number of frame timestamps)
     */
    public void addColumn(IDataColumn<Object> col) throws IllegalStateException, IllegalArgumentException {

        this.addColumns( List.of(col) );
//        // Check for timestamp assignment 
//        if (this.clkTms==null && this.vecTms==null)
//            throw new IllegalStateException("Cannot add data column, no timestamps have been assigned for this frame.");
//        
//        // Check column size
//        Integer     szCol;
//        if (this.clkTms!=null)
//            szCol = this.clkTms.getSampleCount();
//        else
//            szCol = this.vecTms.size();
//        
//        if (col.getSize() != szCol)
//            throw new IllegalArgumentException("Column has invalid size " + col.getSize() + ", should be " + szCol);
//        
//        // Add column
//        this.vecColData.add(col);
//        this.mapNmToCol.put(col.getName(), col);
    }
    
    /**
     * <p>
     * Adds a collection of time-series data columns to the ingestion frame.
     * </p>
     * <p>
     * This method appropriate when the frame is created using the default constructor 
     * <code>{@link #IngestionFrame()}</code>, but can also be used to add time-series data after an
     * initializing construction.  
     * The timestamps for the ingestion frame must be assigned first
     * before invoking this method or an exception is thrown.  The argument must have the correct size 
     * (i.e., the timestamp count) or an exception is thrown.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>
     * This action must follow that of timestamp assignment when populating an ingestion frame after default 
     * construction.
     * </li>
     * <li>
     * All data columns added to the frame must have the same size, and that size must equal to number of 
     * timestamp within the frame.
     * </li>
     * <li>
     * This method can be called as many times as necessary to fully populate the data frame with time-series
     * data.
     * </ul>
     * </p>
     * 
     * @param setCols   unordered collection of time-series data columns to be added to ingestion frame
     * 
     * @throws IllegalStateException    timestamps for the ingestion frame have not been previously assigned
     * @throws IllegalArgumentException the argument has the wrong size (not equal to number of frame timestamps)
     */
    public void addColumns(Collection<IDataColumn<Object>> setCols) throws IllegalStateException, IllegalArgumentException {
        
        // Check for timestamp assignment 
        if (this.clkTms==null && this.vecTms==null)
            throw new IllegalStateException("Cannot add data column, no timestamps have been assigned for this frame.");
        
        // Get the correct column size
        Integer     szCol;
        if (this.clkTms!=null)
            szCol = this.clkTms.getSampleCount();
        else
            szCol = this.vecTms.size();
        
        // Check all arguments for proper column size
        List<String>    lstBadColNms = setCols
                .stream()
                .filter(col -> { return col.getSize() != szCol; })
                .<String>map(IDataColumn::getName)
                .toList();
        
        if (!lstBadColNms.isEmpty())
            throw new IllegalArgumentException("Column size should be " + szCol + ", Column(s) have invalid size " + lstBadColNms);
        
        // Add columns to ingestion frame
        for (IDataColumn<Object> col : setCols) {
            this.vecColData.add(col);
            this.mapNmToCol.put(col.getName(), col);
        }
    }
    
    /**
     * <p>
     * Adds a single (name, value) attribute pair to the ingestion frame.
     * </p>
     * <p>
     * Attributes can be optionally assigned to an ingestion frame which then appear as metadata within the
     * time-series archive.  Attributes do not affect the ingestion of data by the Data Platform.  However,
     * they can potentially be used to search and query for time-series data by metadata properties.
     * </p>  
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This method can be called repeatedly to established multiple attributes for an ingestion frame.</li>
     * </ul>
     * </p>
     * 
     * @param name      attribute name
     * @param value     attribute value
     */
    public void addAttribute(String name, String value) {
        this.mapAttributes.put(name, value);
    }
    
    /**
     * <p>
     * Adds a collection of (name, value) attribute pairs to the ingestion frame.
     * </p>
     * <p>
     * The argument is expected to contain a collection of attributes as (key, value) pairs within the
     * map.  A map <em>key</em> is taken as the attribute name while its corresponding <em>value</em> is
     * its associated attribute value.
     * </p>
     * <p>
     * Attributes can be optionally assigned to an ingestion frame which then appear as metadata within the
     * time-series archive.  Attributes do not affect the ingestion of data by the Data Platform.  However,
     * they can potentially be used to search and query for time-series data by metadata properties.
     * </p>  
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This method can be called repeatedly to established multiple attributes for an ingestion frame.</li>
     * </ul>
     * </p>
     * 
     * @param mapAttrs  map with entry pairs (name, value) where name is the attribute name and value 
     */
    public void addAttributes(Map<String, String> mapAttrs) {
        this.mapAttributes.putAll(mapAttrs);
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * <h1>Checks the current ingestion frame for internal data consistency.</h1>
     * </p>
     * <p>
     * Called by clients to do a frame data consistency check before offering for ingestion.
     * The following checks are performed:
     * <ul>
     * <li>Either a sampling clock or a timestamp list has been set.</li>
     * <li>A non-empty collection of data columns has been established.</li>
     * <li>All data columns have the same size, that specified by the clock or timestamp list.</li>
     * </ul>
     * </p>
     * 
     * @return  the status of this check (either SUCCESS or FAILURE with detail message)
     */
    public ResultStatus    checkFrameConsistency() {

        // Determine the number of rows for each data column
        int cntRows;
        
        if (this.clkTms != null) 
            cntRows = this.clkTms.getSampleCount();
        else if (this.vecTms != null)
            cntRows = this.vecTms.size();
        else {
            String      strMsg = JavaRuntime.getQualifiedCallerNameSimple() 
                               + " - Neither a sampling clock or list of timestamps was set for the ingestion frame."; 
            
            return ResultStatus.newFailure(strMsg);
        }
        
        // Check that data columns were established
        if (this.vecColData == null || this.vecColData.isEmpty()) {
            String      strMsg = JavaRuntime.getQualifiedCallerNameSimple() 
                    + " - The ingestion frame has no data columns."; 
 
            return ResultStatus.newFailure(strMsg);
        }
            
        // Create a list of names for all data columns having the incorrect size
        List<String> lstBadColNms = this.vecColData
                .stream()
                .filter(col -> { return col.getSize() != cntRows; })
                .<String>map(IDataColumn::getName)
                .toList();
        
        // If the list is empty everything is okay
        if (lstBadColNms.isEmpty())
            return ResultStatus.SUCCESS;
        
        // Create the failure message and return failed status
        String  strMsg = "The ingestion frame contained inconsistent sized data columns (size != " 
                       + cntRows + "): " 
                       + lstBadColNms;
        
        return ResultStatus.newFailure(strMsg);
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates the column name to data column map from the give collection of data columns.
     * </p>
     * <p>
     * The <code>IngestionFrame</code> class maintains a map of (name, column) pairs where <em>name</em> is the
     * column name and <em>column</em> is the data column itself.  This method creates that map.
     * </p>
     * 
     * @param setColData    unordered collection of data columns for this ingestion frame 
     * 
     * @return      the (name, column) map used for retrieving data columns by name
     */
    private Map<String, IDataColumn<Object>> createNmToColMap(Collection<IDataColumn<Object>> setColData) {
        
        // Create the (name, column) map to be returned
        Map<String, IDataColumn<Object>>    mapTarget = new HashMap<>();
        
        // Populate the map and return it
        setColData.stream().forEach(col -> mapTarget.put(col.getName(), col));
        
        return mapTarget;
    }
    
}
