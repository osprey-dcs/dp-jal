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
package com.ospreydcs.dp.api.ingest;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.stream.IntStream;

import org.epics.nt.NTTable;
import org.epics.pvdata.pv.PVStructure;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.model.ClientRequestUID;
import com.ospreydcs.dp.api.model.DpSupportedType;
import com.ospreydcs.dp.api.model.IDataColumn;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.model.UniformSamplingClock;
import com.ospreydcs.dp.api.model.table.StaticDataColumn;
import com.ospreydcs.dp.api.util.Epics;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.JavaSize;

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
 * <p>
 * <h2>Optional Parameters</h2>
 * An ingestion frame includes additional optional parameters than can be archived as metadata
 * concerning the ingestion process.  Specifically, the ingestion frame can contain a frame label,
 * a frame timestamp, and optional (name, value) attribute pairs.
 * </p> 
 * <p> 
 * <h2>Snapshots</h2>
 * Ingestion frames support the mechanism for establishing snapshots.
 * A <em>snapshot</em> is an abstraction offered to clients.  It allows clients to
 * archive additional metadata associated with the data ingestion process that can be 
 * queried against later.
 * </p>
 * <p>
 * A snapshot is composed of one or more ingestion frames that are associated in some
 * context.  For example, a snapshot may refer to all data collected from detectors 
 * during a single experimental event.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Mar 29, 2024
 *
 */
public class IngestionFrame implements Serializable {

    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new, initialized <code>IngestionFrame</code> from an EPICS <code>PVStructure</code> object which
     * is assumed to be EPICS <code>NTTable</code> compatible.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The argument must be able to be "wrapped" by an <code>{@link NTTable}</code> EPICS type, otherwise an
     * exception is thrown.  
     * </li>
     * <li>The argument must contain a timestamps column to define the ingestion frame timestamps, or an exception
     * is thrown.
     * </li>
     * <li>
     * The argument must contain at least one column of time-series data (consistent with the timestamp column)
     * or an exception is thrown.
     * </li>
     * <li>
     * The timestamp column is checked for uniform sampling.  If uniform sampling is detected then a sampling
     * clock is assigned to the return ingestion frame.  For non-uniform sampling a timestamp list is assigned to
     * to the ingestion frame (i.e., the argument's timestamp column).
     * </li> 
     * </ul>
     * </p>
     *  
     * @param pvsTable  PV structure assumed to be NTTable compatible
     * 
     * @return  new ingestion frame populated with the argument data 
     * 
     * @throws IllegalArgumentException argument is not NTTable compatible
     * @throws MissingResourceException argument is missing data to create valid ingestion frame 
     */
    public static IngestionFrame from(PVStructure pvsTable) throws IllegalArgumentException, MissingResourceException {
        
        // Check the argument for compatibility
        NTTable ntTable = NTTable.wrap(pvsTable);
        if (ntTable == null)
            throw new IllegalArgumentException("IngestionFrame#from(PVStructure) - argument was not compatible with NTTable");

        // Extract the timestamps and data
        ArrayList<Instant>              vecTms = Epics.extractTableTimestamps(pvsTable);
        ArrayList<IDataColumn<Object>>  vecCols = Epics.extractTableColumns(pvsTable);
        
        // Attempt to convert timestamp list to a uniform sampling clock (for performance)
        IngestionFrame frame = null;
        try {
            UniformSamplingClock clk = UniformSamplingClock.from(vecTms);
            
            frame = new IngestionFrame(clk, vecCols);
            
        } catch(MissingResourceException | IllegalArgumentException e) {
            
            frame = new IngestionFrame(vecTms, vecCols);
        }
        
        // Extract optional parameters and assign them
        Instant     insTblTms = Epics.extractTableTimestamp(pvsTable);
        String      strTblLbl = Epics.extractName(pvsTable);
        
        frame.setFrameLabel(strTblLbl);
        frame.setFrameTimestamp(insTblTms);
        
        return frame;
    }
    
    /**
     * <p>
     * Creates a new, initialized instance of <code>IngestionFrame</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This creator returns an ingestion frame where the timestamps are identified implicitly with a 
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
     * @return  new <code>IngestionFrame</code> instance populated with the argument data
     * 
     * @throws IllegalArgumentException  the argument data was inconsistent (see detail message)
     * 
     * @see #IngestionFrame(UniformSamplingClock, ArrayList)
     * @see #checkFrameConsistency()
     */
    public static IngestionFrame from(UniformSamplingClock clk, ArrayList<IDataColumn<Object>> vecColData) 
            throws IllegalArgumentException {
        
        return new IngestionFrame(clk, vecColData);
    }
    
    /**
     * <p>
     * Creates a new, initialized instance of <code>IngestionFrame</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This creator returns an ingestion frame where the timestamps are identified explicitly with a 
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
     * @return  new <code>IngestionFrame</code> instance populated with the argument data
     * 
     * @throws  IllegalArgumentException  the argument data was inconsistent (see detail message)
     * 
     * @see #IngestionFrame(ArrayList, ArrayList)
     * @see #checkFrameConsistency()
     */
    public static IngestionFrame from(ArrayList<Instant> vecTms, ArrayList<IDataColumn<Object>> vecColData) 
            throws IllegalArgumentException {

        return new IngestionFrame(vecTms, vecColData);
    }
    
    /**
     * <p>
     * Creates a new, uninitialized, instance of <code>IngestionFrame</code>.
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
     * @return  new, uninitialized ingestion frame ready timestamp assignment and data population 
     */
    public static IngestionFrame newFrame() {
        return new IngestionFrame();
    }
    
    
    //
    // Class Constants
    //
    
    /** Serialization UID for interface <code>Serializable</code> */
    private static final long serialVersionUID = -7002454987009867231L;

    
    //
    // Defining Parameters
    //
    
    /** Vector of column data timestamps for each data column of time-series data */
    private ArrayList<Instant>      vecTms = null;
    
    /** Alternate sampling clock for data columns of time series data */
    private UniformSamplingClock    clkTms = null;
    
    /** Array of column data for the table - for fast column index lookup */
    private ArrayList<IDataColumn<Object>>      vecColData = null;

    
    //
    // Optional Parameters
    //
    
    /** The data provider UID (unique name) */
    private ProviderUID         recProviderUid = null;
    
    /** The client request unique identifier */
    private ClientRequestUID     recClientUid = null;
    
    
    /** Optional name for ingestion frame */
    private String              strLabelFrame = null;
    
    /** Optional timestamp for ingestion frame itself */
    private Instant             insTmsFrame = null;
    
    /** Optional collection of (name, value) attribute pairs for the data frame */
    private Map<String, String> mapAttributes = new HashMap<>();

    
    /** Optional snapshot domain */
    private TimeInterval        domSnapshot = null;
    
    /** Optional snapshot UID */
    private String              strSnapshotUid = null;

    
    //
    // Instance Resources 
    //
    
    /** Map of column names to the column data - for column name lookup  */
    private Map<String, IDataColumn<Object>>    mapNmToCol = new HashMap<>();
    
    /** Map of column names to column index - for column index lookup */
    private Map<String, Integer>                mapNmToInd = new HashMap<>();

    
    //
    // Constructors
    //
    
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
     */
    public IngestionFrame() {
        this.recClientUid = ClientRequestUID.random();
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
        this.recClientUid = ClientRequestUID.random();

        this.clkTms = clk;
        this.vecColData = lstCols;
        this.mapNmToCol = this.createNmToColMap(lstCols);
        this.mapNmToInd = this.createNmToColIndMap(lstCols);
        
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
        this.recClientUid = ClientRequestUID.random();

        this.clkTms = clk;
        this.vecColData = lstCols;
        this.mapAttributes.putAll(mapAttrs);
        this.mapNmToCol = this.createNmToColMap(lstCols);
        this.mapNmToInd = this.createNmToColIndMap(lstCols);
        
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

        this.recClientUid = ClientRequestUID.random();
     
        this.vecTms = vecTms;
        this.vecColData = lstCols;
        this.mapAttributes.putAll(mapAttrs);
        this.mapNmToCol = this.createNmToColMap(lstCols);
        this.mapNmToInd = this.createNmToColIndMap(lstCols);

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
     * The ingestion frame takes ownership of the argument container - do not modify it.
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
     * </li>
     * <li>
     * The ingestion frame takes ownership of the given argument - do not modify until after ingestion.
     * </li>
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
     * </li>
     * <li>
     * The ingestion frame takes ownership of all columns in the argument - do not modify until after ingestion.
     * </li>
     * </ul>
     * </p>
     * 
     * @param setDataCols   unordered collection of time-series data columns to be added to ingestion frame
     * 
     * @throws IllegalStateException    timestamps for the ingestion frame have not been previously assigned
     * @throws IllegalArgumentException the argument has the wrong size or name already exists within column collection
     */
    public void addColumns(Collection<IDataColumn<Object>> setDataCols) throws IllegalStateException, IllegalArgumentException {
        
        // Check for timestamp assignment 
        if (this.clkTms==null && this.vecTms==null)
            throw new IllegalStateException("Cannot add data column, no timestamps have been assigned for this frame.");
        
        // Check for existing data column with same name
        Set<String>     setColNmsCurr = this.getColumnNames();
        List<String>    lstDupColNms = setDataCols
                .stream()
                .filter(col -> setColNmsCurr.contains(col.getName())) // filter for columns with names in current collection
                .<String>map(IDataColumn::getName)
                .toList();
        
        if (!lstDupColNms.isEmpty())
            throw new IllegalArgumentException("Ingestion frame already contains column(s) with name(s) " + lstDupColNms);
        
        // Get the correct column size
        Integer     szCol;
        if (this.clkTms!=null)
            szCol = this.clkTms.getSampleCount();
        else
            szCol = this.vecTms.size();
        
        // Check all arguments for proper column size
        List<String>    lstBadColNms = setDataCols
                .stream()
                .filter(col -> (col.getSize().intValue() != szCol) )  // filter for columns with incorrect size
                .<String>map(IDataColumn::getName)
                .toList();
        
        if (!lstBadColNms.isEmpty())
            throw new IllegalArgumentException("Column size should be " + szCol + ", Column(s) have invalid size " + lstBadColNms);
        
        // Add columns to ingestion frame
        for (IDataColumn<Object> col : setDataCols) {
            Integer indCol = this.vecColData.size();
            
            this.vecColData.add(col);
            this.mapNmToCol.put(col.getName(), col);
            this.mapNmToInd.put(col.getName(), indCol);
        }
    }
    
    
    //
    // Optional Properties
    //
    
    /**
     * <p>
     * Copies all the optional parameters from the given ingestion frame into this one.
     * </p>
     * <p>
     * This is a convenience method for assigning optional properties to
     * a stream of ingestion frames related in some way.
     * </p>
     * 
     * @param frmSource ingestion frame used as the source of optional properties 
     */
    public void copyOptionalProperties(final IngestionFrame frmSource) {
        
        // Assign any optional properties from source to this ingestion frame
        this.setProviderUid(frmSource.recProviderUid);
        this.setClientRequestUid(frmSource.recClientUid);
        this.setFrameLabel(frmSource.strLabelFrame);
        this.setFrameTimestamp(frmSource.insTmsFrame);
        this.addAttributes(frmSource.mapAttributes);
        
        // Assign snapshot properties from source to this ingestion frame
        this.setSnapshotId(frmSource.strSnapshotUid);
        this.setSnapshotDomain(frmSource.domSnapshot);
    }
    
    /**
     * <p>
     * Sets the Data Provider UID for the ingestion frame.
     * </p>
     * 
     * @param recProviderUid    UID record for provider producing this frame
     */
    public void setProviderUid(ProviderUID recProviderUid) {
        this.recProviderUid = recProviderUid;
    }
    
    /**
     * <p>
     * Sets the client request UID for the ingestion frame.
     * </p>
     * <p>
     * Under normal operation the request UID is set during construction of the ingestion frame
     * and does not need to be modified.  However, this method is available for ingestion frame
     * decomposition when frame with memory allocation larger than maximum are decomposed into
     * composite frames meeting the limit.  Each new frame requires a new request UID.   
     * </p>
     * <h2>WARNING</h2>
     * The given argument must be universally unique within the request space of the Ingestion Service.
     * All client ingest data requests must contain a unique identifier for later determination of 
     * successful ingestion.  Be extremely careful when setting client request UIDs.  
     * </p> 
     * 
     * @param recRequestUid new universally unique request ID 
     */
    public void setClientRequestUid(ClientRequestUID recRequestUid) {
        this.recClientUid = recRequestUid;
    }
    
    /**
     * <p>
     * Sets the optional ingestion frame label.
     * </p>
     * 
     * @param strLabel  label for ingestion frame itself
     */
    public void setFrameLabel(final String strLabel) {
        this.strLabelFrame = strLabel;
    }
    
    /**
     * <p>
     * Sets the optional ingestion frame timestamp.
     * </p>
     * 
     * @param insTmsFrame   timestamp for the ingestion frame itself
     */
    public void setFrameTimestamp(final Instant insTmsFrame) {
        this.insTmsFrame = insTmsFrame;
    }
    
    /**
     * <p>
     * Set the optional snapshot unique identifier.
     * </p>
     * <p>
     * The snapshot UID identifies all ingestion frames within the target snapshot.  
     * This value should be set for every ingestion frame defining the snapshot.
     * </p>
     * 
     * @param strSnapshotUid    unique identifier for the snapshot to which this frame belongs 
     */
    public void setSnapshotId(final String strSnapshotUid) {
        this.strSnapshotUid = strSnapshotUid;
    }
    
    /**
     * <p>
     * Set the optional snapshot domain.
     * </p>
     * <p>
     * The snapshot domain assignment assignment should at least include all timestamps within 
     * this ingestion frame.  It should include the time range of all ingestion frames composing
     * the snapshot.
     * </p>
     * <p>
     * <h2>Snapshots</h2>
     * A <em>snapshot</em> is an abstraction offered to clients.  It allows clients to
     * archive additional metadata associated with the data ingestion process that can be 
     * queried against later.
     * </p>
     * <p>
     * A snapshot is composed of one or more ingestion frames that are associated in some
     * context.  For example, a snapshot may refer to all data collected from detectors 
     * during a single experimental event.
     * </p>
     * 
     * @param domSnapshot   time range of the snapshot to which this frame belongs 
     */
    public void setSnapshotDomain(final TimeInterval domSnapshot) {
        this.domSnapshot = domSnapshot;
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
            String      strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                               + " - Neither a sampling clock or list of timestamps was set for the ingestion frame."; 
            
            return ResultStatus.newFailure(strMsg);
        }
        
        // Check that data columns were established
        if (this.vecColData == null || this.vecColData.isEmpty()) {
            String      strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - The ingestion frame has no data columns."; 
 
            return ResultStatus.newFailure(strMsg);
        }

        
        // Create a list of names for all data columns having the incorrect size
        List<String> lstBadColSzs = this.vecColData
                .stream()
                .filter(col -> { return col.getSize() != cntRows; })
                .<String>map(IDataColumn::getName)
                .toList();
        
        // If the list is not empty there are columns with incorrect sizes
        if (!lstBadColSzs.isEmpty()) {
            // Create the failure message and return failed status
            String  strMsg = "The ingestion frame contained inconsistent sized data columns (size != " 
                           + cntRows + "): " 
                           + lstBadColSzs;
            
            return ResultStatus.newFailure(strMsg);
        }
        
        // Check that all data values have compatible types
        List<String> lstBadColTypes = new LinkedList<>();
        for (IDataColumn<Object> col : this.vecColData) {
            String          strName = col.getName();
            DpSupportedType enmType = col.getType();
            
            if (!col.getValues().stream().allMatch(objVal -> enmType.isAssignableFrom(objVal)))
                lstBadColTypes.add(strName);
        }
        
        if (!lstBadColTypes.isEmpty()) 
            return ResultStatus.newFailure("The ingestion frame contained data columns with inconsistent data types: " + lstBadColTypes); 
     
        // If we are here everything checks
        return ResultStatus.SUCCESS;
    }
    
    /**
     * <p>
     * Computes and returns an estimate for the memory allocation size for each row of 
     * this ingestion frame (in bytes).
     * </p>
     * <p>
     * The estimate proceeds by collecting the data value for the first row using
     * <code>{@link #getRowValues(int)}</code> then computing its serialized size using
     * <code>{@link JavaSize#serialSizeof(java.io.Serializable)}</code>.
     * If a timestamp list is used, then the allocation for a single timestamp is included.
     * The estimate does not include allocation for the optional parameters.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The returned value is more appropriately identified as the <em>serialization size</em>.
     * Allocation estimates are typically computed by serializing components to Java byte 
     * streams.
     * </li>
     * <li>
     * Concerning the above, the returned value typically <em>underestimates</em> the memory
     * allocation size for the Java container and <em>overestimates</em> a Protocol Buffers
     * serialization size.
     * </li>
     * </ul>
     * </p> 
     * 
     * @return  a conservative estimate of the memory allocation size for one row of this frame (in bytes)
     */
    public long allocationSizeRow() {
        
        // Check for exceptional cases
        if (this.vecColData == null || this.vecColData.isEmpty())
            return 0L;

        // Get the first row and compute its allocation size
        ArrayList<Object>   lstRowVals = this.getRowValues(0);
        long                szRow = JavaSize.serialSizeof(lstRowVals);

        if (this.vecTms!=null)
            szRow += 2 * JavaSize.SZ_Long;
        
        return szRow;
    }
    
    /**
     * <p>
     * Computes and returns an estimate for the memory allocation size for each column of 
     * this ingestion frame (in bytes).
     * </p>
     * <p>
     * The estimate proceeds by computing the allocate size for the first data column of 
     * this ingestion frame using <code>{@link IDataColumn#allocationSize()}</code>.
     * The estimate does not include allocation for the optional parameters.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The returned value is more appropriately identified as the <em>serialization size</em>.
     * Allocation estimates are typically computed by serializing components to Java byte 
     * streams.
     * </li>
     * <li>
     * Concerning the above, the returned value typically <em>underestimates</em> the memory
     * allocation size for the Java container and <em>overestimates</em> a Protocol Buffers
     * serialization size.
     * </li>
     * </ul>
     * </p> 
     * 
     * @return  a conservative estimate of the memory allocation size for one columnn of this frame (in bytes)
     */
    public long allocationSizeColumn() {
        
        // Check for exceptional cases
        if (this.vecColData == null || this.vecColData.isEmpty())
            return 0L;

        // Get the first column and get its allocation size
        IDataColumn<Object>     col = this.vecColData.get(0);
        long                    szCol = col.allocationSize();
        
        return szCol;
    }
    /**
     * <p>
     * Computes and returns an estimate for the total memory allocation size for the entire 
     * ingestion frame (in bytes).
     * </p>
     * <p>
     * The estimate proceeds by summing the allocation size for each times-series data contained in the frame.
     * If a timestamp list is used, then the allocation for the timestamps is include.
     * The estimate does not include allocation for the optional parameters.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The returned value is more appropriately identified as the <em>serialization size</em>.
     * Allocation estimates are typically computed by serializing components to Java byte 
     * streams.
     * </li>
     * <li>
     * Concerning the above, the returned value typically <em>underestimates</em> the memory
     * allocation size for the Java container and <em>overestimates</em> a Protocol Buffers
     * serialization size.
     * </li>
     * </ul>
     * </p> 
     * 
     * @return  a conservative estimate of the memory allocation size for this frame (in bytes)
     */
    public long allocationSizeFrame() {
        
        // Check for exceptional cases
        if (this.vecColData == null || this.vecColData.isEmpty())
            return 0L;
        
        // Compute the total allocation for the ingestion frame
        long    szBytes = 0;
        for (IDataColumn<Object> col : this.vecColData) 
            szBytes += col.allocationSize();
        
        // Add in timestamp list allocation if present
        if (this.vecTms != null && !this.vecTms.isEmpty())
            szBytes += 2 * Long.BYTES * this.vecTms.size();
        
        return szBytes;
    }
    
    
    /**
     * <p>
     * Removes the first <code>cntCols</code> data columns from the current ingestion frame and returns them as a
     * new ingestion frame. 
     * </p>
     * <p>
     * This method is intended for use in ingestion frame <em>binning</em>.  Specifically, if the allocation
     * size of the ingestion frame is larger than the current gRPC message size limit then it must be 
     * transmitted over several smaller messages.  This provided a mean by which large ingestion frames can
     * be decomposed into a collection of smaller frames meeting the gRPC transmission size limitation.
     * </p>
     * <p>
     * Splits this data frame in two by removing the the initial collection of data columns (size given
     * by argument).
     * After the method invocation the current data frame will no longer contain the given
     * data columns.
     * </p> 
     * <p>
     * The method returns the a new ingestion frame containing the given column indices and
     * the same optional properties as the current frame.
     * The returned frame will have identical timestamp characteristics as the 
     * current frame (i.e., sampling clock or timestamp list). 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The internal ordering of the data columns within the ingestion frame is not guaranteed.
     * Generally, however, if the frame used an initializing creator or constructor the ordering
     * is given by the column ordering there.  Additionally, columns added post construction generally
     * maintain their ordering.
     * </li>
     * <br/>
     * <li>
     * <s>If the argument is greater than the number of data columns then the original frame is returned.</s>
     * If the argument is greater than the number of data columns then an exception is thrown.
     * </ul>
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * If the given argument identifies all data columns the contents of the original frame is 
     * returned and the original frame is left empty.  That is, the returned frame
     * is a copy of the original frame and the current frame is empty.
     * </p> 
     * 
     * @param cntCols   number of data column to be removed from this ingestion frame
     * 
     * @return  new ingestion frame with the given columns (by indexes) with all other attributes identical
     * 
     * @throws IllegalStateException    the data frame has not been initialized and/or populated
     * @throws IndexOutOfBoundsException the argument contains at least one index out of bounds
     */
    public IngestionFrame removeColumnsByIndex(int cntCols) throws IllegalStateException, IndexOutOfBoundsException {
        
        // Check that frame has been initialized
        if (this.clkTms != null && this.vecTms != null)
            throw new IllegalStateException("IngestionFrame#removeColumnsByIndex(int) - No timestamps have been assigned.");
        
        if (this.vecColData.isEmpty())
            throw new IllegalStateException("IngestionFrame#removeColumnsByIndex(int) - No data has been assigned.");
    
        // Check argument
        if (cntCols > this.getColumnCount())
            throw new IndexOutOfBoundsException("IngestionFrame#removeColumnsByIndex(int) - Argument is greater than the column count.");
        
        // Returned ingestion frame
        IngestionFrame  frmSplit = new IngestionFrame();
    
        // Set the timestamps
        // - If we are using a sampling clock
        if (this.clkTms != null) {
            
            frmSplit.assignSamplingClock(this.clkTms);
        }
        // - If we are using a timestamp list
        if (this.vecTms != null) {
    
            frmSplit.assignTimestampList(this.vecTms);
        }

        // Extract the specified columns from the ingestion frame and add to new frame
        List<IDataColumn<Object>>   lstColsRmvd = this.vecColData.subList(0, cntCols);
        
        frmSplit.addColumns(lstColsRmvd);

        // Now assign any optional data
        frmSplit.copyOptionalProperties(this);
        
        // We need to reset the current ingestion frame data column maps
        int     szFrame = this.getColumnCount();
        this.vecColData = new ArrayList<>( this.vecColData.subList(cntCols, szFrame) );
        this.mapNmToCol = this.createNmToColMap(this.vecColData);
        this.mapNmToInd = this.createNmToColIndMap(this.vecColData);
        
        // Return the split ingestion frame         
        return frmSplit;
    }
    
    /**
     * <p>
     * Removes the given collection of data columns from the current data frame (by indexes) 
     * and returns them as a new ingestion frame. 
     * </p>
     * <p>
     * This method is intended for use in ingestion frame <em>binning</em>.  Specifically, if the allocation
     * size of the ingestion frame is larger than the current gRPC message size limit then it must be 
     * transmitted over several smaller messages.  This provided a mean by which large ingestion frames can
     * be decomposed into a collection of smaller frames meeting the gRPC transmission size limitation.
     * </p>
     * <p>
     * Splits this data frame in two by removing the the given collection of data columns.
     * After the method invocation the current data frame will no longer contain the given
     * data columns.
     * </p> 
     * <p>
     * The method returns the a new ingestion frame containing the given column indices and
     * the same optional properties as the current frame.
     * The returned frame will have identical timestamp characteristics as the 
     * current frame (i.e., sampling clock or timestamp list). 
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * If the given argument identifies all data columns the contents of the original frame is 
     * returned and the original frame is left empty.  That is, the returned frame
     * is a copy of the original frame and the current frame is empty.
     * </p> 
     * 
     * @param colIndices unordered collection of frame column indexes to be removed from this frame 
     * 
     * @return  new ingestion frame with the given columns (by indexes) with all other attributes identical
     * 
     * @throws IllegalStateException    the data frame has not been initialized and/or populated
     * @throws IndexOutOfBoundsException the argument contains at least one index out of bounds
     */
    public IngestionFrame removeColumnsByIndex(Collection<Integer> colIndices) throws IllegalStateException, IndexOutOfBoundsException {
        
        // Check that frame has been initialized
        if (this.clkTms != null && this.vecTms != null)
            throw new IllegalStateException("IngestionFrame#removeColumnsByIndex(Collection) - No timestamps have been assigned.");
        
        if (this.vecColData.isEmpty())
            throw new IllegalStateException("IngestionFrame#removeColumnsByIndex(Collection) - No data has been assigned.");
    
        // Check argument
        if (!colIndices.stream().allMatch(i -> (i>=0 && i<this.getColumnCount()) ) )
            throw new IndexOutOfBoundsException("IngestionFrame#removeColumnsByIndex(Collection) - Argument contains column index(s) out of bounds.");
        
        // Returned ingestion frame
        IngestionFrame  frmSplit = new IngestionFrame();
    
        // Set the timestamps
        // - If we are using a sampling clock
        if (this.clkTms != null) {
            
            frmSplit.assignSamplingClock(this.clkTms);
        }
        // - If we are using a timestamp list
        if (this.vecTms != null) {
    
            frmSplit.assignTimestampList(this.vecTms);
        }
        
        // Extract the specified columns from the ingestion frame and add to new frame
        List<IDataColumn<Object>>   lstColsRmvd = colIndices
                .stream()
                .<IDataColumn<Object>>map(i -> this.vecColData.get(i.intValue()))
                .toList();
    
        frmSplit.addColumns(lstColsRmvd);
        
        // Now assign any optional data
        frmSplit.copyOptionalProperties(this);
        
        // We need to reset the current ingestion frame data column vector and maps
        List<Integer>               lstIndsSaved = new LinkedList<>(IntStream.range(0, this.getColumnCount()).boxed().toList());
        lstIndsSaved.removeAll(colIndices);
        
        List<IDataColumn<Object>>   lstColsSaved = lstIndsSaved.stream().map(i -> this.vecColData.get(i)).toList();
        this.vecColData = new ArrayList<>(lstColsSaved);
        this.mapNmToCol = this.createNmToColMap(this.vecColData);
        this.mapNmToInd = this.createNmToColIndMap(this.vecColData);
        
        // Return the split ingestion frame         
        return frmSplit;
    }

    /**
     * <p>
     * Removes the given collection of data columns from the current data frame (by name) 
     * and returns them as a new ingestion frame. 
     * </p>
     * <p>
     * This method is intended for use in ingestion frame <em>binning</em>.  Specifically, if the allocation
     * size of the ingestion frame is larger than the current gRPC message size limit then it must be 
     * transmitted over several smaller messages.  This provided a mean by which large ingestion frames can
     * be decomposed into a collection of smaller frames meeting the gRPC transmission size limitation.
     * </p>
     * <p>
     * Splits this data frame in two by removing the the given collection of data columns.
     * After the method invocation the current data frame will no longer contain the given
     * data columns.
     * </p> 
     * <p>
     * The method returns the a new ingestion frame containing the given column names and
     * the same optional properties as the current frame.
     * The returned frame will have identical timestamp characteristics as the 
     * current frame (i.e., sampling clock or timestamp list). 
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * If the given argument identifies all data columns the contents of the original frame is 
     * returned and the original frame is left empty.  That is, the returned frame
     * is a copy of the original frame and the current frame is empty.
     * </p> 
     * 
     * @param colColNms unordered collection column names to be remove from this ingestion frame
     * 
     * @return  new ingestion frame with the given columns (by name) with all other attributes identical
     * 
     * @throws IllegalStateException    the data frame has not been initialized and/or populated
     * @throws IllegalArgumentException the argument contains columns not within this ingestion frame
     */
    public IngestionFrame   removeColumnsByName(Collection<String> colColNms) throws IllegalStateException, IllegalArgumentException {
        
        // Check that frame has been initialized
        if (this.clkTms != null && this.vecTms != null)
            throw new IllegalStateException("IngestionFrame#removeColumnsByName(Collection) - No timestamps have been assigned.");
        
        if (this.vecColData.isEmpty())
            throw new IllegalStateException("IngestionFrame#removeColumnsByName(Collection) - No data has been assigned.");

        // Check argument
        if (!this.getColumnNames().containsAll(colColNms))
            throw new IllegalArgumentException("IngestionFrame#removeColumnsByName(Collection) - Argument contains column names not in ingestion frame.");
        
        // Returned ingestion frame
        IngestionFrame  frmSplit = new IngestionFrame();

        // Set the timestamps
        // - If we are using a sampling clock
        if (this.clkTms != null) {
            
            frmSplit.assignSamplingClock(this.clkTms);
        }

        // - If we are using a timestamp list
        if (this.vecTms != null) {

            frmSplit.assignTimestampList(this.vecTms);
        }
        
        // Extract the specified columns from the ingestion frame and add to new frame
        List<Integer>               lstIndsRmvd = colColNms
                .stream()
                .<Integer>map(s -> this.mapNmToInd.get(s))
                .toList();
        
        List<IDataColumn<Object>>  lstColsRmvd = lstIndsRmvd
                .stream()
                .<IDataColumn<Object>>map(i -> this.vecColData.get(i.intValue()))
                .toList();
                
        frmSplit.addColumns(lstColsRmvd);
        
        // Now assign any optional data
        frmSplit.copyOptionalProperties(this);
        
        // We need to reset the current ingestion frame data column vector and maps
        List<Integer>               lstIndsSaved = new LinkedList<>(IntStream.range(0, this.getColumnCount()).boxed().toList());
        lstIndsSaved.removeAll(lstIndsRmvd);
        
        List<IDataColumn<Object>>   lstColsSaved = lstIndsSaved.stream().map(i -> this.vecColData.get(i)).toList();
        this.vecColData = new ArrayList<>(lstColsSaved);
        this.mapNmToCol = this.createNmToColMap(this.vecColData);
        this.mapNmToInd = this.createNmToColIndMap(this.vecColData);
        
        // Return the split ingestion frame         
        return frmSplit;
    }
    
    /**
     * <p>
     * Removes the head (whose size is given by the argument) of the current ingestion frame, leaving the 
     * remaining tail.
     * </p>
     * <p>
     * This method is intended for use in ingestion frame <em>binning</em>.  Specifically, if the allocation
     * size of the ingestion frame is larger than the current gRPC message size limit then it must be 
     * transmitted over several smaller messages.  This provided a mean by which large ingestion frames can
     * be decomposed into a collection of smaller frames meeting the gRPC transmission size limitation.
     * </p>
     * <p>
     * Splits this ingestion frame in two by removing the first <code>cntRows</code>
     * number of data rows.  The size of the data frame after the call will 
     * be given by the size before the call minus the argument.  The first data
     * row will be the data row with index <code>cntRows</code> before the
     * call.
     * </p> 
     * <p>
     * The method <em>returns the head</em> of the data frame, that is, the portion of
     * the frame that was removed.  The size of the head is given by the the
     * argument.
     * All properties and attributes of the returned frame will be identical
     * with the original (e.i., the timestamp, and the attributes collection).
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * If the size given in the argument is larger than or equal to the current size 
     * of the data frame, all the data is removed from this frame and an exact copy
     * is returned.  The current frame is then empty. 
     * </p> 
     * 
     * @param cntRows the number of data rows to remove at the head of this frame
     * 
     * @return  the head of the original data frame, or <code>null</code> if head count too large
     * 
     * @throws IllegalArgumentException the argument was negative
     * @throws IllegalStateException    the current frame has not been populated (see message)
     */
    public IngestionFrame removeRowsAtHead(int cntRows) throws IllegalArgumentException, IllegalStateException {

        // Check argument
        if (cntRows < 0)
            throw new IllegalArgumentException("IngestionFrame#removeHead(int) - Argument was negative.");

        // Check that frame has been initialized
        if (this.clkTms == null && this.vecTms == null)
            throw new IllegalStateException("IngestionFrame#removeHead(int) - No timestamps have been assigned.");

        if (this.vecColData.isEmpty())
            throw new IllegalStateException("IngestionFrame#removeHead(int) - No data has been assigned.");


        // Special Case: Check if the head size is greater than or equal to this size
        if (cntRows >= this.getRowCount()) {

            // Returned ingestion frame
            IngestionFrame  frmHead = this.copy();

            // Clear out all current data - the current frame is now empty
            if (this.clkTms != null)
                this.clkTms = this.createReducedSampleClock(0);

            if (this.vecTms != null)
                this.vecTms.clear();

            if (this.vecColData != null) 
                this.vecColData.clear();

            return frmHead;
        }

        // --- General Case ---
        // Compute the start and stop row indices for the head
        Integer indStart = 0;
        Integer indStop  = cntRows;
        Integer szFrame  = this.getRowCount();

        // Returned ingestion frame
        IngestionFrame  frmHead = new IngestionFrame();

        // Set the timestamps
        // - If we are using a sampling clock
        if (this.clkTms != null) {
            UniformSamplingClock    clkHead = this.createReducedSampleClock(cntRows);

            frmHead.assignSamplingClock(clkHead);

            // Need to reset the current sampling clock
            this.clkTms = this.createReducedSampleClock(szFrame - cntRows);
        }

        // - If we are using a timestamp list
        if (this.vecTms != null) {
            // Take the head and tail of the timestamp list
            List<Instant>   vecHeadTms = this.vecTms.subList(indStart, indStop);
            List<Instant>   vecTailTms = this.vecTms.subList(indStop, szFrame);

            frmHead.assignTimestampList(new ArrayList<>(vecHeadTms));

            // Need to reset current timestamp list to tail
            this.vecTms = new ArrayList<>(vecTailTms);
        }


        // Identify the head and tail of the data columns 
        // - creates new columns for head
        List<IDataColumn<Object>> lstHeadCols = this.vecColData
                .stream()
                .<IDataColumn<Object>>map( c -> StaticDataColumn.from(
                        c.getName(),
                        c.getType(),
                        c.getValues().subList(indStart, indStop)
                        )  
                        ).toList();

        // - creates new columns for tail
        List<IDataColumn<Object>> lstTailCols = this.vecColData
                .stream()
                .<IDataColumn<Object>>map( c -> StaticDataColumn.from(
                        c.getName(),
                        c.getType(),
                        c.getValues().subList(indStop, szFrame)
                        )  
                        ).toList();

        // Assign the new frame head data
        frmHead.addColumns(lstHeadCols);

        // Now assign any optional data
        frmHead.copyOptionalProperties(this);

        // We need to reset the current ingestion frame data column vector and maps
        this.vecColData = new ArrayList<IDataColumn<Object>>(lstTailCols);
        this.mapNmToCol = this.createNmToColMap(this.vecColData);
        this.mapNmToInd = this.createNmToColIndMap(this.vecColData);

        // Return the head of this ingestion frame         
        return frmHead;
    }

    /**
     * <p>
     * Removes the tail of the current data frame, whose size is given by the 
     * argument, leaving only the head.
     * </p>
     * <p>
     * This method is intended for use in ingestion frame <em>binning</em>.  Specifically, if the allocation
     * size of the ingestion frame is larger than the current gRPC message size limit then it must be 
     * transmitted over several smaller messages.  This provided a mean by which large ingestion frames can
     * be decomposed into a collection of smaller frames meeting the gRPC transmission size limitation.
     * </p>
     * <p>
     * Splits this data frame in two by removing the last <code>cntRows</code>
     * number of data rows.  That is, the data frame after the call will have 
     * the size before the call minus the argument.
     * </p> 
     * <p>
     * The method returns the tail of the data frame, that is, the portion of
     * the frame that was removed.  The size of the tail is given by the argument.
     * The returned frame will have identical properties and attributes as the 
     * original frame (e.g., timestamps and attributes collection). 
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * If the size given in the argument is larger than or equal to the current size 
     * of the data frame, the contents of the original frame is returned
     * as the tail and the original frame is left empty.  The returned frame
     * is a copy of the original frame.
     * </p> 
     * 
     * @param cntRows number of data rows to remove from the tail 
     * 
     * @return  the tail of the current data frame
     * 
     * @throws IllegalArgumentException the argument was negative
     * @throws IllegalStateException    the current frame has not been populated (see message)
     */
    public IngestionFrame removeRowsAtTail(int cntRows) throws IllegalArgumentException, IllegalStateException {

        // Check argument
        if (cntRows < 0)
            throw new IllegalArgumentException("IngestionFrame#removeTail(int) - Argument was negative.");

        // Check that frame has been initialized
        if (this.clkTms != null && this.vecTms != null)
            throw new IllegalStateException("IngestionFrame#removeTail(int) - No timestamps have been assigned.");

        if (this.vecColData.isEmpty())
            throw new IllegalStateException("IngestionFrame#removeTail(int) - No data has been assigned.");


        // Special Case: Check if the head size is greater than or equal to this size
        if (cntRows >= this.getRowCount()) {

            // Returned ingestion frame
            IngestionFrame  frmTail = this.copy();

            // Clear out all current data - the current frame is now empty
            if (this.clkTms != null)
                this.clkTms = this.createReducedSampleClock(0);

            if (this.vecTms != null)
                this.vecTms.clear();

            if (this.vecColData != null) 
                this.vecColData.clear();

            return frmTail;
        }

        // --- General Case ---
        // Compute the start and stop row indices for the head
        Integer szFrame  = this.getRowCount();
        Integer indStart = szFrame - cntRows;
        Integer indStop  = szFrame;

        // Returned ingestion frame
        IngestionFrame  frmTail = new IngestionFrame();

        // Set the timestamps
        // - If we are using a sampling clock
        if (this.clkTms != null) {
            UniformSamplingClock    clkTail = this.createReducedSampleClock(cntRows);

            frmTail.assignSamplingClock(clkTail);

            // Need to reset the current sampling clock
            this.clkTms = this.createReducedSampleClock(szFrame - cntRows);
        }

        // - If we are using a timestamp list
        if (this.vecTms != null) {
            // Take the head and tail of the timestamp list
            List<Instant>   vecHeadTms = this.vecTms.subList(0, indStart);
            List<Instant>   vecTailTms = this.vecTms.subList(indStart, indStop);

            frmTail.assignTimestampList(new ArrayList<>(vecTailTms));

            // Need to reset current timestamp list to head
            this.vecTms = new ArrayList<>(vecHeadTms);
        }

        // Identify the head and tail of the data columns 
        // - creates new columns for head
        List<IDataColumn<Object>> lstHeadCols = this.vecColData
                .stream()
                .<IDataColumn<Object>>map( c -> StaticDataColumn.from(
                        c.getName(),
                        c.getType(),
                        c.getValues().subList(0, indStart)
                        )  
                        ).toList();

        // - creates new columns for tail
        List<IDataColumn<Object>> lstTailCols = this.vecColData
                .stream()
                .<IDataColumn<Object>>map( c -> StaticDataColumn.from(
                        c.getName(),
                        c.getType(),
                        c.getValues().subList(indStart, indStop)
                        )  
                        ).toList();

        // Assign the new frame head data
        frmTail.addColumns(lstTailCols);

        // Now assign any optional data
        frmTail.copyOptionalProperties(this);

        // We need to reset the current ingestion frame data column vector and maps
        this.vecColData = new ArrayList<IDataColumn<Object>>(lstHeadCols);
        this.mapNmToCol = this.createNmToColMap(this.vecColData);
        this.mapNmToInd = this.createNmToColIndMap(this.vecColData);

        // Return the head of this ingestion frame         
        return frmTail;
    }

    /**
     * <p>
     * Performs a shallow copy of the current ingestion frame and returns it.
     * </p>
     * <p>
     * Specifically, the returned ingestion frame has all the same data objects as the current frame,
     * the time-series values, the timestamps (if timestamp list), and the sampling clock are NOT copied.  
     * However, all container objects within the returned ingestion frame are independent.  Data may be
     * cleared from the original frame without affecting the copy.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * Modifying a data column, timestamp, or sampling clock within the original ingestion frame WILL ALSO
     * modify that of the copy.
     * </p>
     * 
     * @return  a shallow copy of the current ingestion frame
     */
    public IngestionFrame copy() {
        
        // The returned object
        IngestionFrame  frmCopy = new IngestionFrame();
        
        // Set the timestamps
        if (this.clkTms != null)
            frmCopy.assignSamplingClock(this.clkTms);
        if (this.vecTms != null)
            frmCopy.assignTimestampList(new ArrayList<>(this.vecTms));
        
        // Set the data columns
        frmCopy.addColumns(this.vecColData);
        
        // Add optional data
        frmCopy.setFrameLabel(this.strLabelFrame);
        frmCopy.setFrameTimestamp(this.insTmsFrame);
        frmCopy.addAttributes(this.mapAttributes);
        
        return frmCopy;
    }
    
    //
    // Attributes and Data Query
    //
    
    /**
     * <p>
     * Determines whether or not a sampling clock has been assigned for this ingestion frame.
     * </p>
     * 
     * @return  <code>true</code> if a sampling clock has been assigned, <code>false</code> otherwise
     */
    public boolean  hasSamplingClock() {
        return this.clkTms != null;
    }
    
    /**
     * <p>
     * Determines whether or not a timestamp list has been assigned for this ingestion frame.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Either a sampling clock or a timestamp list must be assigned to an ingestion frame in order to 
     * populate it with data.
     * </p>
     * 
     * @return  <code>true</code> if a timestamp list has been assigned to the ingestion frame.
     */
    public boolean  hasTimestampList() {
        return this.vecTms != null;
    }
    
    /**
     * <p>
     * Determines whether or not the ingestion frame contains time-series data columns.
     * </p>
     * 
     * @return  <code>true</code> if frame contains a non-empty collection of times-series data columns,
     *          <code>false</code> if the collection is empty
     */
    public boolean hasData() throws IllegalStateException {
        if (this.vecColData == null)
            return false;
        
        return !this.vecColData.isEmpty();
    }
    
    /**
     * <p>
     * Determines whether or not the ingestion frame contains a data column with the given name.
     * </p>
     * 
     * @param strName   name of desired data column
     * 
     * @return  <code>true</code> if ingestion frame contains the data column,
     *          <code>false</code> otherwise
     *          
     * @throws IllegalStateException    no data has been assigned to ingestion frame (uninitialized state)
     */
    public boolean hasDataColumn(String strName) throws IllegalStateException {
        
        // Check for data assignment
        if (this.vecColData == null)
            throw new IllegalStateException("IngestionFrame#hasDataColumn(String) - no data has been assigned.");
        
        return this.mapNmToCol.containsKey(strName);
    }
    
    /**
     * <p>
     * Determines whether or not optional attributes have been assigned to the ingestion frame.
     * </p>
     * 
     * @return  <code>true</code> if frame contains a non-empty collection of (name, value) attribute pairs,
     *          <code>false</code> if the frame contains no attributes
     */
    public boolean hasAttributes() {
        return !this.mapAttributes.isEmpty();
    }
    
    /**
     * <p>
     * Returns the Data Provider UID for this ingestion frame.
     * </p>
     * 
     * @return  UID for the Data Provider that produced this ingestion frame
     */
    public ProviderUID  getProviderUid() {
        return this.recProviderUid;
    }
    
    /**
     * <p>
     * Returns the universally unique data ingestion request identifier for the ingestion frame.
     * </p>
     *  
     * @return  request UUID for the ingestion frame 
     */
    public ClientRequestUID getClientRequestUid() {
        return this.recClientUid;
    }
    
    /**
     * <p>
     * Returns the optional ingestion frame label.
     * </p>
     * 
     * @return the label for the ingestion frame itself
     */
    public String   getFrameLabel() {
        return this.strLabelFrame;
    }
    
    /**
     * <p>
     * Returns the optional ingestion frame timestamp.
     * </p>
     * 
     * @return  timestamp for the ingestion frame itself
     */
    public Instant  getFrameTimestamp() {
        return this.insTmsFrame;
    }
    
    /**
     * <p>
     * Returns the collection of optional (name, value) attribute pairs for the ingestion frame.
     * </p>
     * 
     * @return  map containing (name, value) attribute pairs as entries (can be empty)
     */
    public Map<String, String>  getAttributes() {
        return this.mapAttributes;
    }
    
    /**
     * <p>
     * Returns the unique identifier for the snapshot which this ingestion frame belongs.
     * </p>
     * 
     * @return  snapshot UID for ingestion frame snapshot,
     *          or <code>null</code> if it has not been set
     */
    public String   getSnapshotId() {
        return this.strSnapshotUid;
    }
    
    /**
     * <p>
     * Return the time domain for the entire snapshot which this ingestion frame belongs.
     * </p>
     * 
     * @return  time range (start and stop times) for the overall snapshot,
     *          or <code>null</code> if it has not been set
     */
    public TimeInterval getSnapshotDomain() {
        return this.domSnapshot;
    }
    
    /**
     * <p>
     * Returns the sampling clock for this ingestion frame (if assigned).
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The method returns value <code>null</code> if no sampling clock was assigned for this frame.
     * This can be the case when a timestamp list was assigned instead, or if no timestamps have been
     * assigned (indicating error or incomplete population).
     * </p>
     * 
     * @return  the sampling clock for all time-series data,
     *          or <code>null</code> if no clock was assigned
     */
    public UniformSamplingClock getSamplingClock() {
        return this.clkTms;
    }
    
    /**
     * <p>
     * Returns the timestamp list for this ingestion frame (if assigned).
     * </p>
     * <h2>NOTES:</h2>
     * The method returns value <code>null</code> if no timestamp list was assigned for this frame.
     * This can be the case when a sampling clock list was assigned instead, or if no timestamps have been
     * assigned (indicating error or incomplete population).
     * </p>
     * 
     * @return the timestamp list for the ingestion frame,
     *         or <code>null</code> if not assigned
     */
    public ArrayList<Instant>   getTimestampList() {
        return this.vecTms;
    }
    
    /**
     * <p>
     * Get the set of unique data column names for all time-series data columns within the ingestion frame.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Column names are not ordered.</li>
     * <li>Column names are unique within an ingestion frame.</li>
     * </ul>
     * <p>
     * 
     * @return  set of names for all data columns within the ingestion frame
     */
    public Set<String>  getColumnNames() {
        return this.mapNmToCol.keySet();
    }
    
    /**
     * <p>
     * Returns the number of time-series data columns with the ingestion frame.
     * </p>
     * 
     * @return  number of data columns currently within the ingestion frame 
     * 
     * throws IllegalStateException        the ingestion frame has not been assigned data
     */
    public int  getColumnCount() /* throws IllegalStateException */ {

//        // Check the frame
//        if (this.vecColData == null)
//            throw new IllegalStateException("IngestionFrame#getColumnCount() - Ingestion has not been initialized with data.");
        
        return this.vecColData.size();
    }
    
    /**
     * <p>
     * Returns the number of rows in each time-series data column within the ingestion frame.
     * </p>
     * <p>
     * Note this value can be zero if the head or tail has been taken.
     * </p>
     * 
     * @return  the number of rows in each data column (i.e., the number of samples in each time-series)
     * 
     * @throws IllegalStateException the ingestion frame has not been assigned timestamps (either clock or list)
     */
    public int getRowCount() throws IllegalStateException {

        // Check for sampling clock
        if (this.clkTms != null)
            return this.clkTms.getSampleCount();
        
        // Check for timestamp list
        if (this.vecTms != null)
            return this.vecTms.size();
        
        // Throw exception - no timestamps have been assigned
        throw new IllegalStateException("IngestionFrame#getRowCount() - Frame timestamps have not been assigned.");
    }
    
    /**
     * <p>
     * Collects and returns all the ingestion frame values for the given row index.
     * </p>
     * <p>
     * The ordering of the data values within the returned list is consistent with the current 
     * column indexing.  Note that the data types of the returned objects is dependent upon
     * the corresponding column data types.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Data column indices are arbitrary within an ingestion frame.  They are assigned internally by the frame
     * when populating data.  
     * </p>
     *  
     * @param indRow    index of the ingestion frame row
     * 
     * @return  all the data column values at the given row index
     * 
     * @throws IllegalStateException        the ingestion frame has not been assigned data
     * @throws IndexOutOfBoundsException    index is not within the range 0 &le; index &lt; {@link #getRowCount()}
     */
    public ArrayList<Object> getRowValues(int indRow) throws IllegalStateException, IndexOutOfBoundsException {
        
        // Check the frame
        if (this.vecColData == null || this.vecColData.isEmpty())
            throw new IllegalStateException("IngestionFrame#getRowValues(int) - Ingestion frame contains no data.");
        
        // Check the argument
        if (indRow < 0 || indRow >= this.getRowCount())
            throw new IndexOutOfBoundsException("IngestionFrame#getRowValues(int) - argument is < 0 or >= " + this.getRowCount());
        
        // Collect and return the ingestion frame row values
        ArrayList<Object> lstVals = new ArrayList<>(this.getColumnCount());
        this.vecColData.forEach(col -> lstVals.add(col.getValue(indRow)));
        
        return lstVals;
    }
    
    /**
     * <p>
     * Returns the time-series data column at the given index within the ingestion frame.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Data column indices are arbitrary within an ingestion frame.  They are assigned internally by the frame
     * when populating data.
     * </p>
     * 
     * @param index index of data column (index &in; [0, {@link #getColumnCount()}])
     * 
     * @return  time-series data column at index 
     * 
     * @throws IllegalStateException        the ingestion frame has not been assigned data
     * @throws IndexOutOfBoundsException    index is not within the range 0 &le; index &lt; {@link #getColumnCount()}
     */
    public IDataColumn<Object>  getDataColumn(int index) throws IllegalStateException, IndexOutOfBoundsException {
        
        // Check the frame
        if (this.vecColData == null || this.vecColData.isEmpty())
            throw new IllegalStateException("IngestionFrame#getDataColumn(int) - Ingestion frame contains no data.");
        
        return this.vecColData.get(index);
    }
    
    /**
     * <p>
     * Returns the time-series data column with the given name.
     * </p>
     * @param strName   name of the desired data column
     * 
     * @return  time-series data column with the given name
     * 
     * @throws IllegalStateException    the ingestion frame has not been assigned data
     * @throws IllegalArgumentException ingestion frame contains no data column with given name
     */
    public IDataColumn<Object>  getDataColumn(String strName) throws IllegalStateException, IllegalArgumentException {
        
        // Check the frame
        if (this.vecColData == null || this.vecColData.isEmpty())
            throw new IllegalStateException("IngestionFrame#getDataColumn(String) - Ingestion frame contains no data.");
        
        // Check argument 
        if (!this.mapNmToCol.containsKey(strName))
            throw new IllegalArgumentException("IngestionFrame#getDataColumn(String) - frame contains no data column with name " + strName);
        
        return this.mapNmToCol.get(strName);
    }
    
    /**
     * <p>
     * Returns the entire collection of time-series data columns managed by the ingestion frame.
     * </p>
     * 
     * @return  list of all time-series data columns within the frame
     * 
     * @throws IllegalStateException    the ingestion frame has not been assigned data
     */
    public final ArrayList<IDataColumn<Object>>   getDataColumns() throws IllegalStateException {
        
        // Check the frame
        if (this.vecColData == null || this.vecColData.isEmpty())
            throw new IllegalStateException("IngestionFrame#getDataColumns() - Ingestion frame contains no data.");
        
        return this.vecColData;
    }
    
    
    //
    // Object Overrides - Debugging
    //
    
    /**
     *
     * @see @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object objFrame) {
        
        // Check type and cast
        if (objFrame instanceof IngestionFrame frm)
            ;
        else
            return false;
        
        // Check timestamps
        if (this.clkTms!=null && !this.clkTms.equals(frm.clkTms))
            return false;
        if (this.vecTms!=null && !this.vecTms.equals(frm.vecTms))
            return false;
        
        // Check time-series data types, values, and column names
        for (IDataColumn<Object> colThis : this.vecColData) {
            String              strName = colThis.getName();
            IDataColumn<Object> colCmp = frm.getDataColumn(strName);
            
            DpSupportedType enmTypeThis = colThis.getType();
            DpSupportedType enmTypeCmp = colCmp.getType();
            if (enmTypeThis != enmTypeCmp)
                return false;
            
            List<Object>    lstValsThis = colThis.getValues();
            List<Object>    lstValsCmp = colCmp.getValues();
            if (!lstValsThis.equals(lstValsCmp))
                return false;
        }
        
        // Check optional attributes
        if (this.strLabelFrame!=null && !this.strLabelFrame.equals(frm.strLabelFrame))
            return false;
        if (this.insTmsFrame!=null && !this.insTmsFrame.equals(frm.insTmsFrame))
            return false;
        if (!this.mapAttributes.equals(frm.mapAttributes))
            return false;
        
        // Everything checks
        return true;
    }
    
    /**
     *
     * @see @see java.lang.Object#toString()
     */
    @Override
    public String   toString() {
        
        String  strText = this.getClass().getName() + ":\n";
        strText += "  Label: " + this.strLabelFrame + "\n";
        strText += "  Timestamp: " + this.insTmsFrame + "\n";
        strText += "  Attributes: " + this.mapAttributes + "\n";
        if (this.clkTms != null) 
            strText += "  Sampling Clock: " + this.clkTms + "\n";

        // Get the timestamps to display, or quite if not assigned
        ArrayList<Instant>  vecRowTms; 
        if (this.clkTms!=null)
            vecRowTms = this.clkTms.createTimestamps();
        
        else if (this.vecTms!=null)
            vecRowTms = this.vecTms;
        
        else {
            strText += "  Timestamps were never assigned. \n";
            
            return strText;
        }
            
        // Write out data in table format
        // - Create and write table header
        String  strTblHdr = "  timestamps\t";
        for (IDataColumn<Object> col : this.vecColData) {
            strTblHdr += "\t" + col.getName();
        }
        strText += strTblHdr + "\n";
        
        // - Write out table timestamps and value
        int cntRows = this.getRowCount();
        
        for (int iRow=0; iRow<cntRows; iRow++) {
            String  strRow = "  ";
            Instant insTms = vecRowTms.get(iRow);
            
            strRow += insTms.getEpochSecond() + ":" + insTms.getNano();
            
            for (IDataColumn<Object> col : this.vecColData) {
                Object  objVal = col.getValue(iRow);
                String  strVal = String.format("%10s", objVal.toString());
                
//                strRow += "\t" + strVal;
                strRow += "\t" + objVal;
            }
            
            strText += strRow + "\n";
        }
        
        return strText;
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
    
    /**
     * <p>
     * Creates the column name to data column index map from the given collection of data columns
     * which is assumed to be ordered.
     * </p>
     * 
     * @param setColData    ordered collection of data columns for this ingestion frame 
     * 
     * @return      the (name, index) map used for retrieving data column indices by name
     */
    private Map<String, Integer>    createNmToColIndMap(ArrayList<IDataColumn<Object>> setColData) {
        
        // Create the (name, index) map to be returned
        Map<String, Integer>    mapTarget = new HashMap<>();
        
        // Populate the map and return it
        Integer     indCurr = 0;
        for (IDataColumn<Object> col : setColData) {
            mapTarget.put(col.getName(), indCurr);
            
            indCurr++;
        }
        
        return mapTarget;
    }
    
    /**
     * <p>
     * Creates a new uniform sampling clock with identical parameter to the current clock, except for the 
     * sample count which is given.
     * </p>
     * 
     * @param cntSamples    number of samples within the returned clock
     * 
     * @return  a new <code>UniformSamplingClock</code> instance identical to the current except for given sample count
     */
    private UniformSamplingClock    createReducedSampleClock(int cntSamples) {
        Instant     insStart = this.clkTms.getStartInstant();
        long        lngPeriod = this.clkTms.getSamplePeriod();
        ChronoUnit  cuPeriod = this.clkTms.getSamplePeriodUnits();
        
        UniformSamplingClock    clkReduced = UniformSamplingClock.from(insStart, cntSamples, lngPeriod, cuPeriod);
        
        return clkReduced;
    }
    
}
