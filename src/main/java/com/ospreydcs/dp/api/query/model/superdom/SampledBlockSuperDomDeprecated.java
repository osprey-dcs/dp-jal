/*
 * Project: dp-api-common
 * File:	SampledBlockSuperDomDeprecated.java
 * Package: com.ospreydcs.dp.api.query.model.coalesce
 * Type: 	SampledBlockSuperDomDeprecated
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
 * @since Apr 2, 2025
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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.ospreydcs.dp.api.common.DpSupportedType;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.query.model.coalesce.SampledBlock;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 *
 * @author Christopher K. Allen
 * @since Apr 2, 2025
 *
 * @deprecated Prototype for SampledBlockSuperDom
 */
@Deprecated(since="April 10, 2025", forRemoval=true)
public class SampledBlockSuperDomDeprecated {
    
    
    //
    // Internal Types
    //
    
//    /**
//     * <p>
//     * Record containing a Process Variable name (i.e., data source) and its data type.
//     * </p>
//     */
//    public static record    PvEntry(String strName, DpSupportedType enmType) {
//        
//        public static PvEntry   from(String strName, DpSupportedType enmType) {
//            return new PvEntry(strName, enmType);
//        }
//    };
    
    /**
     * <p>
     * Record containing the Process Variable data for a table column.
     * </p>
     * <p>
     * Record instances are intended as intermediate objects in building time-series data tables.
     * </p>
     */
    public static record    ColEntry(String strName, DpSupportedType enmType, List<Instant> lstTms, List<Object> lstVals) {
        
        public static ColEntry  from(String strName, DpSupportedType enmType) {
            return new ColEntry(strName, enmType);
        }
        
        ColEntry(String strName, DpSupportedType enmType) {
            this(strName, enmType, new LinkedList<>(), new LinkedList<>());
        }
        
        public boolean  hasValueAt(Instant insTms) {
            return this.lstTms.contains(insTms);
        }
        
        public int  indexTimestamp(Instant insTms) throws IllegalArgumentException {
            int indTms = this.lstTms.indexOf(insTms);
            
            if (indTms == -1)
                throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Column entry contains no value for timestamp " + insTms);
            
            return indTms;
        }
        
        public void addValueAt(int index, Instant insTms, Object objVal) {
            this.lstTms.add(index, insTms);
            this.lstVals.add(index, objVal);
        }
    }
    
    /**
     * <p>
     * Record containing Process Variable data for a data table row.
     * </p>
     * <p>
     * Record instances are intended as intermediate objects in building time-series data tables.
     * </p>
     */
    public static record    RowEntry(Instant insTms, List<String> lstPvNms, List<Object> lstPvVals) implements Comparable<RowEntry> {
        
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
        // Operations
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
    // Application Resources
    //
    
    /** The Data Platform API default configuration parameter set */
    private static final DpQueryConfig  CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** Event logging enabled flag */
    public static final boolean     BOL_LOGGING = CFG_QUERY.logging.enabled;
    
    /** Event logging level */
    public static final String      STR_LOGGING_LEVEL = CFG_QUERY.logging.level;
    
    
    /** General timeout limit */
    public static final long        LNG_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** General timeout limit units */
    public static final TimeUnit    TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
    /** Concurrency enabled flag */
    public static final boolean     BOL_CONCURRENCY = CFG_QUERY.concurrency.enabled;
    
    /** Maximum number of concurrent processing threads */
    public static final int         CNT_MAX_THREADS = CFG_QUERY.concurrency.maxThreads;
    
    /** Concurrency tuning parameter - pivot to parallel processing when lstMsgDataCols size hits this limit */
    public static final int         SZ_CONCURRENCY_PIVOT = CFG_QUERY.concurrency.pivotSize;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger LOGGER = LogManager.getLogger();

    
    /**
     * <p>
     * Class Resource Initialization - Initializes the event logger, sets logging level.
     * </p>
     */
    static {
        Configurator.setLevel(LOGGER, Level.toLevel(STR_LOGGING_LEVEL, LOGGER.getLevel()));
    }
    
    
    //
    // Defining Attributes
    //
    
    /** The super domain from which this sampled block is built */
    private final RawSuperDomData    datSupDomOrg;
    
    
    //
    // Configuration
    //
    
    /** Enable/disable concurrent processing */
    private boolean bolConcurrency = BOL_CONCURRENCY;
    
    /** Maximum number of concurrent worker threads */
    private int     cntMaxThreads = CNT_MAX_THREADS;
    
    
    //
    // Resources
    //
    
//    /** Ordered list of clocked data extracted from original data */
//    private final List<RawClockedData>  lstClkData = new LinkedList<>();
//    
//    /** Ordered list of clocked data extracted from original data */
//    private final List<RawTmsListData>  lstTmsLstData = new LinkedList<>();
    
    
    /** Sampled blocks created from the original super domain */
    private final List<SampledBlock>    lstSmplBlks;
    
    
    /** Sorted set (by time instant) of all timestamps within super domain */
    private final SortedSet<Instant>    setTmsAll; // = new TreeSet<>();
    
    /** Map of Process Variable names to Process Variable types */
    private final Map<String, DpSupportedType>  mapPvToType;
    
//    /** Set of all Process Variable name with their data type */
//    private final Set<PvEntry>          setPvs; // = new HashSet<>();
    
    
    /** Map (table) of timestamps to row entries of the super domain */
    private final TreeMap<Instant, RowEntry>    mapTmsToRow; // = new TreeMap<>();
    
    
//    /** The concurrent task executor service */
//    private ExecutorService     exeFillTasks;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new, initialized <code>SampledBlockSuperDomDeprecated</code> instance.
     * </p>
     *
     * @param datRaw    the super domain from which this sampled block is initialized
     * 
     * @throws ExecutionException         file phantom data task general exception for serial processing (see cause)
     * @throws RejectedExecutionException fill phantom data task failed execution for concurrent processing
     * @throws InterruptedException       timeout occurred while waiting for concurrent fill tasks to complete
     */
    public SampledBlockSuperDomDeprecated(RawSuperDomData datRaw) throws RejectedExecutionException, ExecutionException, InterruptedException {
        this.datSupDomOrg = datRaw;

        this.lstSmplBlks = new ArrayList<>(datRaw.getSuperDomain().size());
        
        this.setTmsAll = new TreeSet<>();
        this.mapPvToType = new HashMap<>();
//        this.setPvs = new HashSet<>();
        this.mapTmsToRow = new TreeMap<>();
        
        this.initialize();
        this.buildRowEntries();
        this.fillRowEntries();
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
    

//    /**
//     * @see com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#createTimestampsVector()
//     */
////    @Override
//    protected ArrayList<Instant> createTimestamps() {
//        
//        LinkedList<Instant> lstTmsAll = new LinkedList<>();
//
//        return lstTmsAll;
//    }
//
//
//    /**
//     * @see com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#createTimeSeriesVector(java.util.List)
//     */
////    @Override
//    protected ArrayList<SampledTimeSeries<Object>> createTimeSeriesVector(List<DataColumn> lstMsgDataCols)
//            throws MissingResourceException, IllegalStateException, TypeNotPresentException {
//
//    }


    //
    // Support Methods
    //
    
    /**
     * <p>
     * Initializes all the intermediate resources for super domain processing.
     * </p>
     * 
     */
    private void initialize() {
        
//        for (RawCorrelatedData datRaw : this.datSupDomOrg) {
//            if (datRaw instanceof RawClockedData dat)
//                this.lstClkData.add(dat);
//            
//            if (datRaw instanceof RawTmsListData dat) 
//                this.lstTmsLstData.add(dat);
//        }

        // Initialize lstSmplBlks
        this.datSupDomOrg.forEach(datRaw -> this.lstSmplBlks.add(SampledBlock.from(datRaw)));
        
        // Initialize setTmsAll
        this.lstSmplBlks.forEach(blk -> this.setTmsAll.addAll(blk.getTimestamps()));
        
        // Initialize mapPvToType
        this.lstSmplBlks.forEach(blk -> blk.getColumnNames().forEach(nm -> this.mapPvToType.put(nm, blk.getColumnType(nm))));
        
//        // Initialize setPvs
//        this.lstSmplBlks.forEach(blk -> blk.getColumnNames().forEach(nm -> this.setPvs.add(PvEntry.from(nm, blk.getColumnType(nm)))));
    }
    

    /**
     * <p>
     * Builds a <code>RowEntry</code> instance for all timestamps within the collection of sample blocks.
     * </p>
     * <p>
     * Iterates through the collection of <code>SampledBlock</code> objects within the super domain and
     * creates a <code>RowEntry</code> containing the timestamp and all PV name and value entries
     * for the <code>SampledBlock</code>.  
     * </p>
     * <p>
     * Note that once completed the <code>RowEntry</code> instances only contain PV data for actual sampled data
     * that is available in the sampled blocks.  The "phantom data" should be filled in later.
     * </p>
     * 
     * @throws IllegalStateException    an attempt was made to add a PV value to a row entry where the value already existed
     */
    private void buildRowEntries() throws IllegalStateException {
        
        for (SampledBlock blk : this.lstSmplBlks) {
            int cntTms = blk.getSampleCount();

            for (int indRow=0; indRow<cntTms; indRow++) {
                Instant insTms = blk.getTimestamp(indRow);

                // Check if a row entry already exists for the current timestamp
                RowEntry entry;
                if (!this.mapTmsToRow.containsKey(insTms))  {
                    // Create new entry and add to map
                    entry = RowEntry.from(insTms);
                    this.mapTmsToRow.put(insTms, entry);
                    
                } else
                    entry = this.mapTmsToRow.get(insTms);

                // Fill the row entry with the sample names and values from all columns
                for (String strName : blk.getColumnNames()) {
                    Object  objVal = blk.getValue(indRow, strName);

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
     * <code>null</code> values) for all process variables within the super domain that are not represented
     * in a row entry.
     * </p>
     *  
     * @throws ExecutionException         general exception for serial processing (see cause)
     * @throws RejectedExecutionException a fill task failed execution for concurrent processing
     * @throws InterruptedException       timeout occurred while waiting for concurrent fill tasks to complete
     */
    private void fillRowEntries() throws ExecutionException, RejectedExecutionException, InterruptedException {
        
        // Extract the collections of PV names and row entries 
        Set<String>             setPvNms = this.mapPvToType.keySet();
        Collection<RowEntry>    setEntries = this.mapTmsToRow.values();
        
        // Process serially and return
        if (!this.bolConcurrency) {
            try {
                setEntries.forEach(entry -> setPvNms.stream()
                        .filter(nm -> !entry.hasPvName(nm))
                        .forEach(nm -> entry.addValue(nm, null)));      // throws exception
                //            for (RowEntry entry : setEntries) {
                //                setPvNms.stream().filter(nm -> !entry.hasPvName(nm)).forEach(nm -> entry.addValue(nm, null));
                //            }
                return;

            } catch (Exception e) {
                String strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Serial processing general exception, see cause.";

                throw new RejectedExecutionException(strMsg, e);

            }
        }
        
        // Else process concurrently
        ExecutorService         exeTasks = Executors.newFixedThreadPool(this.cntMaxThreads);
        List<Callable<Boolean>> lstTasks = setPvNms
                .stream()
                .<Callable<Boolean>>map(nm -> this.createPvFillTask(nm, setEntries))
                .toList();
        
        exeTasks.invokeAll(lstTasks, LNG_TIMEOUT, TU_TIMEOUT);
        exeTasks.shutdown();
        exeTasks.awaitTermination(LNG_TIMEOUT, TU_TIMEOUT);
    }
    
    /**
     * <p>
     * Creates and returns a thread task that fills in "phantom data" for the given process variable name.
     * </p>
     * <p>
     * The task checks all row entries for data from the given process variable.  If no data is found
     * for the PV is found for a row entries then a phantom value <code>null</code> is assigned.
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
                    .filter(entry -> entry.hasPvName(strPvNm))
                    .forEach(entry -> entry.addValue(strPvNm, null));
            
            return true;
        };
        
        return task;
    }
    
    /**
     * <p>
     * Creates and returns a thread task that fills in "phantom data" for the given row entry for all the given PVs.
     * </p>
     * <p>
     * The task checks the row entry for all the named process variables in the argument collection.  If the row
     * entry does not contain a value for the given process variable a phantom value of <code>null</code> is added
     * to the row entry. 
     * </p>
     * 
     * @param entry     row entry to process (add phantom data if necessary)
     * @param setPvNms  set of all process variable names within the super domain
     * 
     * @return  a thread task performing the phantom data filling operation for the given row entry and all super domain PVs
     */
    @SuppressWarnings("unused")
    private Callable<Boolean>   createRowFillTask(RowEntry entry, Set<String> setPvNms) {
        
        // Create as lambda function
        Callable<Boolean>   task = () -> {
            
            for (String strPvNm : setPvNms) {
                if (!entry.hasPvName(strPvNm))
                    entry.addValue(strPvNm, null);
            };
            
            return true;
        };
        
        return task;
    }

}
