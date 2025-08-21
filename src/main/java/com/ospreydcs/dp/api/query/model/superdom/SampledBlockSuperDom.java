/*
 * Project: dp-api-common
 * File:	SampledBlockSuperDom.java
 * Package: com.ospreydcs.dp.api.query.model.assem
 * Type: 	SampledBlockSuperDom
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
 * @since Apr 10, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.superdom;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.common.DpSupportedType;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.query.model.coalesce.SampledBlock;
import com.ospreydcs.dp.api.query.model.coalesce.SampledTimeSeries;
import com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.DataValue;

/**
 * <p>
 * Subclass of <code>SampledBlock</code> supporting processing raw data in a <code>RawSuperDomData</code> instance.
 * </p>
 * <p>
 * Class provides <code>SampledBlock</code> implementation for all sampled, time-series data within a super domain
 * where correlated data can have time domain collisions.  Thus, this class has far more processing requirements
 * than sister classes <code>SampledBlockTmsList</code> and <code>SampledBlockClocked</code>.
 * </p>
 * <p>
 * Class instances are fully initialized once constructed, as required by the <code>SampledBlock</code> base class.
 * To do so many internal resources (e.g., "auxiliary data" and Java arrays) must be created and processed before
 * the base class <code>{@link #initialize()}</code> method can be called.  Note the <code>{@link #initialize()}</code>
 * method calls the abstract methods <code>{@link #createTimestampsVector()}</code> and 
 * <code>{@link #createTimeSeriesVector()}</code> which are implemented here and utilized all the internal resources.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 10, 2025
 *
 */
public class SampledBlockSuperDom extends SampledBlock {

    
    //
    // Internal Types
    //
    
    /**
     * <p>
     * Record containing Process Variable data for a data table row.
     * </p>
     * <p>
     * Record instances are intended as intermediate objects in building time-series data tables.
     * </p>
     */
    public static record RowEntry(Instant insTms, List<String> lstPvNms, List<Object> lstPvVals) implements Comparable<RowEntry> {
        
        //
        // Creator
        //
        
        /**
         * <p>
         * Creates a new <code>RowEntry</code> instance for the given timestamp.
         * </p>
         * <p>
         * The internal list containing process variable names and values are created as new 
         * <code>{@link LinkedList}</code> objects since the number of row entries is unknown.
         * </p>
         * 
         * @param insTms    the timestamp of the row entry
         * 
         * @return new <code>RowEntry</code> instance is ready for population with process variable names and sample values
         */
        public static RowEntry  from(Instant insTms) {
            return new RowEntry(insTms);
        }

        
        //
        // Non-Canonical Constructors
        //
        
        /**
         * <p>
         * Constructs a new <code>RowEntry</code> instance for the given timestamp.
         * </p>
         * <p>
         * The internal list containing process variable names and values are created as new 
         * <code>{@link LinkedList}</code> objects since the number of row entries is unknown.
         * </p>
         * <p>
         * The new <code>RowEntry</code> instance is ready for population of process variable names and sample values.
         * </p>
         *
         * @param insTms    the timestamp of the row entry
         */
        RowEntry(Instant insTms) {
            this(insTms, new LinkedList<>(), new LinkedList<>());
        }
        
        
        //
        // State Query
        //
        
        /**
         * </p>
         * Determines whether or not the given Process Variable has a value within the row entry.
         * </p>
         * 
         * @param strPvNm   process variable under inspection
         * 
         * @return  <code>true</code> if the given process variable is represented within the row entry
         *          <code>false</code> otherwise
         */
        public boolean  hasPvName(String strPvNm) {
            return this.lstPvNms.contains(strPvNm);
        }
        
        /**
         * <p>
         * Returns the number of entries within this <code>RowEntry</code> instance.
         * </p>
         * <p>
         * Returns the size of the <code>{@link #lstPvNms}</code> field, first comparing it with the size
         * of the <code>{@link #lstPvVals}</code>.  If the two sizes are unequal an exception is thrown.
         * </p>
         * 
         * @return  size of the row entry (number of PVs and values)
         * 
         * @throws IllegalStateException    internal error, the list sizes are not equal
         */
        public int  size() throws IllegalStateException {
            final int   szPvNms = this.lstPvNms.size();
            final int   szPvVals = this.lstPvVals.size();
            
            if (szPvNms != szPvVals) {
                String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                        + " - Internal state error, list sizes unequal: name list size = "
                        + szPvNms + ", value list size = " + szPvVals + ".";
                
                throw new IllegalStateException(strMsg);
            }
            
            return szPvNms;
        }

        
        //
        // Operations
        //
        
        /**
         * <p>
         * Retrieves the row index of the given Process Variable within the row entry.
         * </p>
         *  
         * @param strPvNm   process variable whose row index is retrieved
         * 
         * @return  row index of the given process variable
         * 
         * @throws IllegalArgumentException the process variable is not contained within the row entry
         */
        public int  indexPvName(String strPvNm) throws IllegalArgumentException {
            int indPv = this.lstPvNms.indexOf(strPvNm);
            
            if (indPv == -1)
                throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Row entry contains no PV with name " + strPvNm);
            
            return indPv;
        }
        
        /**
         * <p>
         * Adds the given PV name and PV value to the end of the row entry.
         * </p>
         * <p>
         * <h2>NOTE:</h2>
         * Either we throw an exception if the PV name is already present within the row entry or we can
         * clobber it; that is, get its index then overwrite the value at the index.  In the latter instance
         * it is possible that sampling clocks with different periods can align at specific timestamps
         * (i.e., the periods are NOT mutually prime).  So..., right now we are just throwing an exception.
         * </p>
         *  
         * @param strPvNm   name of the process variable
         * @param objVal    sample value of the process variable at the timestamp
         *  
         * @throws IllegalStateException    the row entry already contains a value for the given process variable
         */
        synchronized
        public void addValue(String strPvNm, Object objVal) throws IllegalStateException {
            if (this.hasPvName(strPvNm))
                throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Row already has data for PV " + strPvNm);
            
            this.lstPvNms.add(strPvNm);
            this.lstPvVals.add(objVal);
        }
        
        /**
         * <p>
         * Retrieves the row entry value for the given Process Variable.
         * </p>
         * 
         * @param strPvNm   process variable name
         * 
         * @return  row entry for the given process variable
         * 
         * @throws IllegalArgumentException the row contains no sampled data for the given process variable
         */
        synchronized
        public Object retrieveValue(String strPvNm) throws IllegalArgumentException {
            if (!this.hasPvName(strPvNm))
                throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Row has no value for PV " + strPvNm);
            
            int     indPv = this.indexPvName(strPvNm);
            Object  objVal = this.lstPvVals.get(indPv);
            
            return objVal;
        }

        
        //
        // Comparable<RowEntry> Interface
        //
        
        /**
         * <p>
         * Compares the timestamp of the row entry according to <code>{@link Instant#compareTo(Instant)</code>.
         * </p>
         * 
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(RowEntry o) {
            return this.insTms.compareTo(o.insTms);
        }
    };
        
    
    //
    // Class Constants
    //
    
    /** The phantom value used for "filling in" missing row data */
    public static final Object      OBJ_PHANTOM = null;
    
    
    /** General timeout limit */
    public static final long        LNG_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** General timeout limit units */
    public static final TimeUnit    TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
    /** Concurrency enabled flag */
    public static final boolean     BOL_CONCURRENCY = CFG_QUERY.data.table.construction.concurrency.enabled;
    
    /** Maximum number of concurrent processing threads */
    public static final int         CNT_MAX_THREADS = CFG_QUERY.data.table.construction.concurrency.maxThreads;
    
    /** Concurrency tuning parameter - pivot to parallel processing when lstMsgDataCols size hits this limit */
    public static final int         SZ_CONCURRENCY_PIVOT = CFG_QUERY.data.table.construction.concurrency.pivotSize;
    
    
    //
    // Defining Attributes
    //
    
    /** The source data for sampled block */
    private final RawSuperDomData   datRawSupDom;
    
    
    //
    // Configuration
    //
    
    /** Enable/disable concurrent processing */
    private boolean bolConcurrency = BOL_CONCURRENCY;
    
    /** Maximum number of concurrent worker threads */
    private int     cntMaxThreads = CNT_MAX_THREADS;
    
    
    //
    // Auxiliary Data
    //
    
    /** The set of all unique PV names represented within this super domain */
    private final Set<String>                   setPvNames = new TreeSet<>();
    
    /** Map of PV name to PV data type */
    private final Map<String, DpSupportedType>  mapPvNmToType = new HashMap<>();
    
    /** Map (table) of timestamps to row entries of the super domain */ 
    private final SortedMap<Instant, RowEntry>  mapTmsToRow = new TreeMap<>();
    
    
    //
    // Resources - set buildResources()
    //
    
    /** The total number of timestamps within the super domain */
    private int             cntTms = 0;
    
    /** The total number of process variables in the super domain */
    private int             cntPvs = 0;
    
    /** Java array of ordered timestamps for the super domain (for fast access) */
    private Instant[]       arrTms;
    
    /** Java array of process variable names (in order of setPvNames) */
    private String[]        arrPvNames;
    
    /** Java array of ordered sample process values for entire super domain (for fast access) */
    private Object[][]      arrVals;
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new, initialized <code>SampledBlockSuperDom</code> instance from the given argument.
     * </p>
     * 
     * @param datRawSupDom  raw, correlated data sets forming the super domain
     *
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws TypeNotPresentException  an unsupported data type was detected within the argument
     * @throws ExecutionException         general exception for serial processing (see cause)
     * @throws RejectedExecutionException a fill task failed execution for concurrent processing
     * @throws InterruptedException       timeout occurred while waiting for concurrent fill tasks to complete
     */
    public SampledBlockSuperDom(RawSuperDomData datRawSupDom) 
            throws IllegalArgumentException, MissingResourceException, 
                   IllegalStateException, TypeNotPresentException, 
                   RejectedExecutionException, ExecutionException, 
                   InterruptedException 
    {
        super();
        
        // Verify source data if flag is set
        if (BOL_ERROR_CHK)
            datRawSupDom.forEach(dat -> super.verifySourceData(dat)); // throws IllegalArgumentException
        
        this.datRawSupDom = datRawSupDom;
        
        // Initialize the child class 
        this.extractPvNames();
        this.extractPvTypes();  // throws MissingResourceException, IllegalStateException, TypeNotPresentException
        this.buildRowEntries(); // throws IllegalStateException (duplicate PV value entries within row - for the same instant)
        this.fillRowEntries();  // throws IllegalStateException, ExecutionExcetion, RejectedExecutionException, InterruptedException
        this.buildJavaArrays();
        
        // Initialize the base class
        super.initialize();     // throws MissingResourceException, IllegalStateException, TypeNotPresentException
    }

    
    //
    // Configuration
    //

    /**
     * <p>
     * Enables/disables concurrent processing of correlated raw data within super domains.
     * </p>
     * <p>
     * Concurrent processing is available for parsing of time-series data and subsequent creation
     * of data tables.
     * </p>
     * 
     * @param bolConcurrency    <code>true</code> enables concurrent processing, 
     *                          <code>false</code> disables concurrent processing
     */
    public void enableConcurrency(boolean bolConcurrency) {
        this.bolConcurrency = bolConcurrency;
    }
    
    /**
     * <p>
     * Sets the maximum number of current thread tasks when concurrent processing is enabled.
     * </p> 
     * 
     * @param cntMaxThreads maximum number of allowable thread workers for concurrent processing
     * 
     * @see #enableConcurrency(boolean)
     */
    public void setMaximumThreads(int cntMaxThreads) {
        this.cntMaxThreads = cntMaxThreads;
    }
    
    /**
     * <p>
     * Returns the maximum number of concurrent thread tasks when concurrent processing is enabled.
     * </p>
     * 
     * @return  maximum number of allowable thread workers for concurrent processing
     * 
     * @see #getConcurrency()
     */
    public int  getMaximumThreads() {
        return this.cntMaxThreads;
    }
    
    /**
     * <p>
     * Determines whether or not concurrent processing of correlated raw data is enabled or disabled.
     * </p>
     * 
     * @return  <code>true</code> if concurrent processing is enabled
     *          <code>false</code> if concurrent processing is disabled
     */
    public boolean getConcurrency() {
        return this.bolConcurrency;
    }
    

    //
    // SampledBlock Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#computeRawAllocation()
     */
    @Override
    protected long computeRawAllocation() {
        long    lngAlloc = this.datRawSupDom.computeRawAllocation();

        return lngAlloc;
    }

    /**
     * @see com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#createTimestampsVector()
     */
    @Override
    protected ArrayList<Instant> createTimestampsVector() {
        
        // Get the ket set of the row map (sorted in ascending order) and create vector from it
        Set<Instant>        setTms = this.mapTmsToRow.keySet();
        ArrayList<Instant>  vecTms = new ArrayList<>(setTms);
        
        return vecTms;
    }

    /**
     * @see com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#createTimeSeriesVector()
     */
    @Override
    protected ArrayList<SampledTimeSeries<Object>> createTimeSeriesVector()
            throws MissingResourceException, IllegalStateException, TypeNotPresentException {
        
        // The returned object
        ArrayList<SampledTimeSeries<Object>>   vecTmSeries = new ArrayList<>(this.cntPvs);
        
        // For each data source in the super domain
        for (int indPv=0; indPv<this.cntPvs; indPv++) {
            ArrayList<Object>   vecValues = new ArrayList<>(this.cntTms);
            
            String  strPvNm         = this.arrPvNames[indPv];
            DpSupportedType enmType = this.mapPvNmToType.get(strPvNm);

            // For each timestamp - fill the new value vector from the value array
            for (int indTms=0; indTms<this.cntTms; indTms++) {
                Object      objVal = this.arrVals[indTms][indPv];
                
                vecValues.add(objVal);
            }
            
            SampledTimeSeries<Object>   tmSeries = SampledTimeSeries.from(strPvNm, enmType, vecValues);
            vecTmSeries.add(tmSeries);
        }
        
        return vecTmSeries;
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Extracts all the unique data source names (PV names) and collates them into the PV name set.
     * </p>
     * <p>
     * Iterates through all the <code>RawCorrelatedData</code> objects within the super domain and extracts
     * the set of PV names for each.  Collects all PV names into attribute <code>{@link #setPvNames}</code>.
     * </p>
     */
    private void extractPvNames() {
        
        for (RawCorrelatedData blk : this.datRawSupDom) {
            Set<String>    setBlkPvNms = blk.getSourceNames();
            
            this.setPvNames.addAll(setBlkPvNms);
        }
    }
    
    /**
     * <p>
     * Extracts the data type of all process variables within the super domain and records them.
     * </p>
     * <p>
     * Iterates through all the <code>RawCorrelatedData</code> objects within the super domain and extracts
     * the data types for each data source (PV).  Also checks for consistency (uniformity) of data type
     * within PV entries in the super domain; a variety of exceptions can be thrown if any data or type
     * inconsistencies are encountered.
     * The process variable data types are recorded in the map attribute <code>{@link #mapPvNmToType}</code>.
     * </p> 
     * 
     * @throws MissingResourceException a raw correlated data column was empty (contained no data)
     * @throws IllegalStateException    process variable with non-uniform data type was encountered (see message)
     * @throws TypeNotPresentException  an unsupported data type was encountered (not of <code>DpSupportedType</code>)
     */
    private void extractPvTypes() throws MissingResourceException, IllegalStateException, TypeNotPresentException {
        
        // For each raw correlated data block in the super domain
        for (RawCorrelatedData blk : this.datRawSupDom) {
            
            // For each data column within the data block
            for (DataColumn msgCol : blk.getRawDataMessages()) {
                String          strColNm = msgCol.getName();
                DpSupportedType enmType = ProtoMsg.extractType(msgCol); // throws all exceptions

                // Check if already recorded
                if (this.mapPvNmToType.containsKey(strColNm)) {
                    DpSupportedType enmTypeCmp = this.mapPvNmToType.get(strColNm);

                    // Check the consistency if already recorded
                    if (enmType != enmTypeCmp) {
                        String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                                + " - Super domain contained inconsistent data types for data source "
                                + strColNm + ": " + enmType + " and " + enmTypeCmp + ".";

                        if (BOL_LOGGING)
                            LOGGER.error(strMsg);

                        throw new IllegalStateException(strMsg);
                    }

                    continue;
                } 

                // Else record PV data type
                this.mapPvNmToType.put(strColNm, enmType);
            }
        }
    }
    
    /**
     * <p>
     * Builds a <code>RowEntry</code> instance for all timestamps within the super domain collection.
     * </p>
     * <p>
     * Iterates through the collection of <code>RawCorrelatedData</code> objects within the super domain.
     * Creates a <code>RowEntry</code> containing the timestamp and all PV name and value entries
     * for each <code>RawCorrelatedData</code> object as necessary.  
     * </p>
     * <p>
     * Note that once completed the <code>RowEntry</code> instances only contain PV data for actual sampled data
     * that is available in the super domain.  The "phantom data" should be filled in later.  That is, row entries
     * should contain values for every PV represented in the super domain even if it does not appear in every
     * <code>RawCorrelatedData</code> instance.
     * </p>
     * 
     * @throws IllegalStateException    an attempt was made to add a PV value to a row entry where the value already existed
     */
    private void buildRowEntries() throws IllegalStateException {
        
        for (RawCorrelatedData blk : this.datRawSupDom) {
            int                 cntTms = blk.getSampleCount();
            ArrayList<Instant>  vecTms = blk.getTimestampVector();

            for (int indRow=0; indRow<cntTms; indRow++) {
                Instant insTms = vecTms.get(indRow);

                // Check if a row entry already exists for the current timestamp
                RowEntry entry;
                if (!this.mapTmsToRow.containsKey(insTms))  {
                    // Create new entry and add to map
                    entry = RowEntry.from(insTms);
                    this.mapTmsToRow.put(insTms, entry);
                    
                } else
                    entry = this.mapTmsToRow.get(insTms);

                // Fill the row entry with the sample names and values from all columns
                for (DataColumn msgCol : blk.getRawDataMessages()) {
                    String      strName = msgCol.getName();
                    DataValue   msgVal = msgCol.getDataValues(indRow);
                    Object      objVal = ProtoMsg.extractValue(msgVal);

                    entry.addValue(strName, objVal);    // throws exception
                }
            }
        }
    }
    
    /**
     * <p>
     * Adds "phantom data" to row entries so that all entries have all super domain process variables represented.
     * </p>
     * <p>
     * All row entries for each timestamp within the super domain should be built before calling this method
     * (see <code>{@link #buildRowEntries()}</code>).  This method then "fills in" phantom data (currently
     * <code>{@link #OBJ_PHANTOM}</code> values) for all process variables within the super domain that are 
     * not represented in a row entry.
     * </p>
     * <p>
     * <h2>Concurrency</h2>
     * The fill operations can be done sequentially or concurrently depending upon the value returned by
     * <code>{@link #getConcurrency()}</code>.  This method requires method 
     * <code>{@link #createPvFillTask(String, Collection)}</code> for concurrent processing, that
     * is, when <code>{@link #getConcurrency()}</code> returns <code>true</code>.
     * </p>
     *  
     * @throws IllegalStateException      duplicate PV representation in row entry
     * @throws ExecutionException         general exception for serial processing (see cause)
     * @throws RejectedExecutionException a fill task failed execution for concurrent processing
     * @throws InterruptedException       timeout occurred while waiting for concurrent fill tasks to complete
     * 
     * @see #createPvFillTask(String, Collection)
     */
    private void fillRowEntries() throws IllegalStateException, ExecutionException, RejectedExecutionException, InterruptedException {
        
        // Extract the collections of row entries 
        Collection<RowEntry>    setEntries = this.mapTmsToRow.values();
        
        // Process serially and return
        if (!this.bolConcurrency) {
            try {
                setEntries.forEach(entry -> 
                        this.setPvNames
                        .stream()
                        .filter(name -> !entry.hasPvName(name))
                        .forEach(name -> entry.addValue(name, OBJ_PHANTOM)));      // throws IllegalState exception
                return;

            } catch (Exception e) {
                String strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Serial processing general exception, see cause.";

                throw new ExecutionException(strMsg, e);

            }
        }
        
        // Else process concurrently
        ExecutorService         exeTasks = Executors.newFixedThreadPool(this.cntMaxThreads);
        List<Callable<Boolean>> lstTasks = this.setPvNames
                .stream()
                .<Callable<Boolean>>map(nm -> this.createPvFillTask(nm, setEntries))
                .toList();
        
        exeTasks.invokeAll(lstTasks, LNG_TIMEOUT, TU_TIMEOUT);  // throws InterruptedException, RejectedExecutionException
        exeTasks.shutdown();                                    // throws SecurityException
        exeTasks.awaitTermination(LNG_TIMEOUT, TU_TIMEOUT);     // throws InterruptedException
    }
    
    /**
     * <p>
     * Creates and returns a thread task that fills in "phantom data" for the given process variable name.
     * </p>
     * <p>
     * The task checks all row entries for data from the given process variable.  If no data 
     * for the PV is found for a row entries then a phantom value <code>{@link #OBJ_PHANTOM}</code> is assigned.
     * </p>
     * 
     * @param strPvNm       the process variable name
     * @param setEntries    collection of row entries to be populated with phantom data (if necessary)
     * 
     * @return  a thread task performing the phantom data filling operation over the given row entries for the given PV
     */
    private Callable<Boolean>   createPvFillTask(String strPvNm, Collection<RowEntry> setEntries) {
        
        // Create as lambda function
        Callable<Boolean>   task = () -> {
            
            setEntries.stream()
                    .filter(entry -> !entry.hasPvName(strPvNm))
                    .forEach(entry -> entry.addValue(strPvNm, OBJ_PHANTOM));
            
            return true;
        };
        
        return task;
    }
    
    
    /**
     * <p>
     * Builds and populates all the Java arrays from the auxiliary data structures.
     * </p>
     * <p>
     * Allocates memory and populates the arrays <code>{@link #arrTms}, {@link #arrPvNames}, {@link #arrVals}</code>
     * from the attributes <code>{@link #setPvNames}</code> and <code>{@link #mapTmsToRow}</code>.
     * The objective is to reduce the repeated lookups of row entries within the map and lookups of PV values
     * within the <code>RowEntry</code> instances when create the <code>SampledTimesSeries</code> objects
     * required by <code>{@link #createTimeSeriesVector()}</code>.
     * </p>
     */
    private void buildJavaArrays() {
        
        // Create the timestamp vector array
        Set<Instant>    setTms = this.mapTmsToRow.keySet();
        
        this.cntTms = setTms.size();
        
        this.arrTms = new Instant[this.cntTms];
        this.arrTms = setTms.toArray(this.arrTms);
        
        // Create the PV name vector array
        this.cntPvs = this.setPvNames.size();

        this.arrPvNames = new String[this.cntPvs];
        this.arrPvNames = this.setPvNames.toArray(this.arrPvNames);
        
        
        // Fill in the PV value matrix array
        this.arrVals = new Object[this.cntTms][this.cntPvs];
        
        for (int indTms=0; indTms<this.cntTms; indTms++) {
            Instant     insTms = this.arrTms[indTms];
            RowEntry    entry = this.mapTmsToRow.get(insTms);
            
            for (int indPv=0; indPv<this.cntPvs; indPv++) {
                String  strPvNm = this.arrPvNames[indPv];
                Object  objVal = entry.retrieveValue(strPvNm);
                
                this.arrVals[indTms][indPv] = objVal;
//                indPv++;
            }
        }
    }
}
