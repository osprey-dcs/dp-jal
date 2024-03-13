/*
 * Project: dp-api-common
 * File:	SamplingProcess.java
 * Package: com.ospreydcs.dp.api.query.model.process
 * Type: 	SamplingProcess
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
 * @since Jan 29, 2024
 *
 * TODO:
 * - See documentation
 */
package com.ospreydcs.dp.api.query.model.process;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.CompletionException;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.ranges.RangeException;

import com.ospreydcs.dp.api.common.ResultRecord;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.model.DpSupportedType;
import com.ospreydcs.dp.api.model.IDataColumn;
import com.ospreydcs.dp.api.model.IDataTable;
import com.ospreydcs.dp.api.query.model.data.SamplingProcessTable;
import com.ospreydcs.dp.api.query.model.data.StaticDataTable;
import com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData;
import com.ospreydcs.dp.api.query.model.grpc.QueryDataCorrelator;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Defines a sampling process containing time-series data from multiple sources over multiple clock durations.
 * </p>
 * <p>
 * A <code>SamplingProcess</code> is intended to represent the data set of a general sampled-time
 * process involving multiple data sources over an extended time range.
 * A <code>SamlingProcess</code> instance is an aggregation of disjoint, ordered 
 * <code>UniformSamplingBlock</code> objects, each block containing the time-series data for
 * a sub-range of the full duration within the process.  Thus, a <code>SamplingProcess</code>
 * need not be uniform throughout its time domain.  However, it is uniform across each time 
 * sub-time domain within a <code>UniformSamplingBlock</code>.
 * </p>
 * <p>
 * <h2>Creation</h2>
 * Note that <code>SamplingProcess</code> instances are created from the processed data
 * sets produced by the class <code>{@link QueryDataCorrelator}</code>.
 * The raw data obtained from a data request of the Query Service is then correlated
 * into an ordered sets of <code>{@link CorrelatedQueryData}</code> instances, each instance 
 * all represent query results all within the same sampling clock.  
 * These correlated results sets are obtained from 
 * <code>{@link QueryDataCorrelator#getCorrelatedSet()}</code>.  The correlated results sets
 * are then used in the creation of <code>SamplingProcess</code> instances. 
 * Thus, this class can be used in the reconstruction process of Data Platform 
 * Query Service data requests.
 * </p>  
 * <p>
 * <h2>WARNINGS:</h2>
 * The current implement includes extensive error checking intended to mitigate external
 * implementation problems during development.   
 * The error checking is performed when creating <code>SamplingProcess</code> instances.  
 * Such error checking could present a <em>performance burden</em> during operation.
 * Developers should consider reducing the error check when mature.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * It is quite possible that not every data source is represented in each component sampling
 * block.  If the time series of a data source is missing within a given sampling block we
 * represent its sample values there as <code>null</code> values.
 * </p>
 * <p>
 * <h2>TODO</h2>
 * <ul>
 * <li><s>Implement a possible interface <code>IDataTable</code> for table-like data retrieval.</s></li>
 * <li>Reduce exception checking when mature - for performance consideration.</li>
 * </ul>
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Jan 29, 2024
 */
public class SamplingProcess {


    //
    // Application Resources
    //
    
    /** The Data Platform API default configuration parameter set */
    protected static final DpQueryConfig  CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** Logging active flag */
    public static final boolean    BOL_LOGGING = CFG_QUERY.logging.active;
    
    
    /** Concurrency active flag */
    public static final boolean     BOL_CONCURRENCY = CFG_QUERY.concurrency.active;
    
    /** Parallelism tuning parameter - pivot to parallel processing when lstMsgDataCols size hits this limit */
    public static final int        SZ_CONCURRENCY_PIVOT = CFG_QUERY.concurrency.pivotSize;
    

    //
    // Class Resources
    //
    
    /** Event logger for class */
    protected static final Logger LOGGER = LogManager.getLogger();
    

    //
    // Attributes
    //

    /** The number of samples within each time-series */
    private final int                   cntSamples;
    
    /** The time domain of the sampling process */
    private final TimeInterval          ivlDomain;
    
    
    /** The set of all unique data sources names - taken from initializing correlated query data */
    protected final SortedSet<String>               setSrcNms;
    
    /** Map of data source names to their data type - taken from processed sampling blocks */
    protected final Map<String, DpSupportedType>    mapSrcNmToType;
    
    /** Ordered vector of uniform sampling blocks comprising the sampling process */
    protected final ArrayList<UniformSamplingBlock> vecSmplBlocks;
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new instance of <code>SamplingProcess</code> fully populated from argument data.
     * </p>
     * <p>
     * After creation the new instance is fully populated with sampled time-series data from
     * all data sources contained in the argument.  The new instance is also configured according
     * to the order and the correlations within the argument.
     * The argument data must be consistent for proper
     * time-series data construction or an exception is thrown.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The argument collection is assumed to be sorted in the order of clock starting instants</li>
     * <li>Extensive data consistency check is performed during construction.</li>
     * <li>Any detected data inconsistencies result in an exception. </li>
     * <li>Exception type determines the nature of any Data inconsistency. </li>
     * </ul>  
     *
     * @param setTargetData sorted set of <code>CorrelatedQueryData</code> used to build this process
     * 
     * @return a new instance of <code>SamplingProcess</code> fully populated and configured with argument data
     *  
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws RangeException           the argument contains time domain collisions
     * @throws TypNotPresentException   an unsupported data type was detected within the argument
     * @throws CompletionException      the sampling process was corrupt after creation (see message)
     */
    public static SamplingProcess from(SortedSet<CorrelatedQueryData> setTargetData) 
            throws  MissingResourceException, 
                    IllegalArgumentException, 
                    IllegalStateException, 
                    RangeException, 
                    TypeNotPresentException, 
                    CompletionException 
    {
        return new SamplingProcess(setTargetData);
    }
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>SamplingProcess</code> fully populated from argument data.
     * </p>
     * <p>
     * After construction the new instance is fully populated with sampled time-series data from
     * all data sources contained in the argument.  The new instance is also configured according
     * to the order and the correlations within the argument.
     * The argument data must be consistent for proper
     * time-series data construction or an exception is thrown.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The argument collection is assumed to be sorted in the order of clock starting instants</li>
     * <li>Extensive data consistency check is performed during construction.</li>
     * <li>Any detected data inconsistencies result in an exception. </li>
     * <li>Exception type determines the nature of any Data inconsistency. </li>
     * </ul>  
     * </p>
     *
     * @param setTargetData sorted set of <code>CorrelatedQueryData</code> used to build this process
     *  
     * @throws RangeException           the argument has bad ordering or contains time domain collisions (see message)
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws TypeNotPresentException  an unsupported data type was detected within the argument
     * @throws CompletionException      the sampling process was corrupt after creation (see message)
     */
    public SamplingProcess(SortedSet<CorrelatedQueryData> setTargetData) 
            throws  RangeException, 
                    MissingResourceException, 
                    IllegalArgumentException, 
                    IllegalStateException, 
                    TypeNotPresentException, 
                    CompletionException 
    {
        
        // First verify consistency of source data
        ResultRecord    result;
        
        //  Check start time ordering
        result = this.verifyStartTimes(setTargetData);
        if (result.isFailure())
            throw new RangeException(RangeException.BAD_BOUNDARYPOINTS_ERR, result.message());
        
        //  Check for time domain intersections - works if start time ordering is correct 
        result = this.verifyTimeDomains(setTargetData);
        if (result.isFailure())
            throw new RangeException(RangeException.BAD_BOUNDARYPOINTS_ERR, result.message());
        
        // Build sampling blocks and get source names from target data set
        this.vecSmplBlocks = this.buildSamplingBlocks(setTargetData);
        this.setSrcNms = this.buildSourceNameSet(setTargetData);
        
        // Create the auxiliary resources from the new sampling blocks
        this.mapSrcNmToType = this.buildSourceNameToTypeMap(this.vecSmplBlocks);
        this.ivlDomain = this.computeTimeDomain(this.vecSmplBlocks);
        this.cntSamples = this.computeSampleCount(this.vecSmplBlocks);
        
        // Verify the consistency of the sampling blocks
        result = this.verifyStartTimes(this.vecSmplBlocks);
        if (result.isFailure())
            throw new CompletionException(result.message(), result.cause());
        
        result = this.verifyTimeDomains(this.vecSmplBlocks);
        if (result.isFailure())
            throw new CompletionException(result.message(), result.cause());
        
        result = this.verifySourceTypes(vecSmplBlocks);
        if (result.isFailure())
            throw new CompletionException(result.message(), result.cause());
    }

    
    //
    // Attribute/Property Query
    //
    
    /**
     * <p>
     * Determine whether or not the sampling process contains time-series data for the given 
     * data source.
     * </p>
     * <p>
     * Simply checks the given data source name against the internal set of represented
     * data sources.
     * </p>
     *  
     * @param strSourceName unique name of data source
     * 
     * @return  <code>true</code> if the data source is represented within this sampling process,
     *          <code>false</code> otherwise
     */
    public boolean  hasSourceData(String strSourceName) {
        return this.setSrcNms.contains(strSourceName);
    }
    
    /**
     * <p>
     * Returns the number of component <code>{@link UniformSamplingBlock}</code> forming this process.
     * </p>
     * <p>
     * Internally, <code>SamplingProcess</code> objects are composed of an ordered vector
     * of <code>UniformSamplingBlock</code> instances. Each sampling block instance contains
     * the time-series data for the process for a given duration and uniform sampling clock.
     * This method returns the total number sampling block instances forming the composite 
     * sampling process.
     * </p>
     * 
     * @return  number of component sampling blocks within composite sampling process
     */
    public final int    getSamplingBlockCount() {
        return this.vecSmplBlocks.size();
    }


    /**
     * <p>
     * Returns the component <code>{@link UniformSamplingBlock}</code> at given index.
     * </p>
     * <p>
     * Internally, <code>SamplingProcess</code> objects are composed of an ordered vector
     * of <code>UniformSamplingBlock</code> instances. Each sampling block instance contains
     * the time-series data for the process for a given duration and uniform sampling clock.
     * This method returns the sampling block instance at the given index.
     * </p>
     * 
     * @param index index of desired sampling block within composite sampling process
     * 
     * @return  component sampling block (within sampling process) at given index
     * 
     * @throws IndexOutOfBoundsException    the index is larger the the number of component blocks
     */
    public final UniformSamplingBlock   getSamplingBlock(int index) throws IndexOutOfBoundsException {
        return this.vecSmplBlocks.get(index);
    }
    
    /**
     * <p>
     * Returns all component <code>{@link UniformSamplingBlock}</code> instances in order of
     * sampling clock start times.
     * </p>
     * <p>
     * Internally, <code>SamplingProcess</code> objects are composed of an ordered vector
     * of <code>UniformSamplingBlock</code> instances. Each sampling block instance contains
     * the time-series data for the process for a given duration determined by its uniform 
     * sampling clock.
     * This method returns all sampling block instances in order of time.
     * </p>
     * 
     * @return the ordered collection of component sampling blocks for this process
     */
    public final ArrayList<UniformSamplingBlock>    getSamplingBlocks() {
        return this.vecSmplBlocks;
    }

    /**
     * <p>
     * Returns the total number of samples within all time series of the sampling process.
     * </p>
     * <p>
     * The returned value is equivalent to the total number of timestamps for the entire sampling 
     * process.
     * </p>
     * <p>
     * Note that each time series within the sampling process must have the same size.  
     * This condition is enforced by assigning <code>null</code> values to data sources
     * missing from any sampling block.
     * </p>
     * 
     * @return  the size of each time series within the sampling process
     */
    public final int getSampleCount() {
        return this.cntSamples;
    }
    
    /**
     * <p>
     * Returns the time domain over which samples were taken.
     * </p>
     * <p>
     * Returns the smallest connected interval [<i>t</i><sub>0</sub>, <i>t</i><sub><i>N</i>-1</sub>]
     * that contains all sample timestamps.
     * </p>
     * <p>
     * The returned interval is given by 
     * <pre>
     *      [<i>t</i><sub>0</sub>, <i>t</i><sub><i>N</i>-1</sub>]
     * </pre>
     * where <i>t</i><sub>0</sub> is the timestamp of the first samples within the process 
     * and <i>t</i><sub><i>N</i>-1</sub> is the timestamp the last sample.  
     * Note that <i>N</i> is the number of samples (equivalently, timestamps) returned by 
     * <code>{@link #getSampleCount()}</code>.
     * <p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned interval DOES NOT include the sampling period duration
     * after the final timestamp.
     * </p>
     *  
     * @return  the smallest connected time interval containing all the sample timestamps
     */
    public final TimeInterval   getTimeDomain() {
        return this.ivlDomain;
    }

    /**
     * <p>
     * Returns the number of unique data sources within the sampling process.
     * </p>
     * <p>
     * The sampling process must containing one and only one time series from each data source
     * represented.  This condition was checked during construction.
     * To obtain the collection of unique data source names use
     * <code>{@link #getDataSourceNames()}</code>.
     * </p>
     * 
     * @return  the number of data sources contributing to this sampling process
     */
    public final int getDataSourceCount() {
        return this.setSrcNms.size();
    }
    
    /**
     * <p>
     * Returns the set of all data source names for data sources contributing to the sampling process.
     * </p>
     * <p>
     * Each data source name should be unique.  After construction there should be only one
     * time series data set for each data source.  Time series data may be recovered by
     * name.
     * </p>
     * <p>
     * <h2>NOTES:<h2>
     * Do not modify the returned set, it is owned by this instance.
     * </p>
     *  
     * @return  set of all data sources (by name) contributing time-series data to this process
     */
    public final Set<String>  getDataSourceNames() {
        return this.setSrcNms;
    }
    
    /**
     * <p>
     * Returns the data type of the time series with the given data source name.
     * </p>
     * <p>
     * Data sources must be unique, and they must produce samples all of the same data type.
     * The sample process maintains a map of data source name to data source type which was
     * created during construction.
     * Data source uniqueness and type consistency should have been checked during 
     * construction.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>If the given data source is not contained within the process an exception is thrown.</li>
     * <li>Use <code>{@link #getDataSourceNames()}</code> to obtain all data sources names in process.</li>
     * </ul>
     * </p>
     * 
     * @param strSourceName data source unique name
     * 
     * @return  the data type of the given data source time series, or <code>null</code> if source not present 
     *
     * @throws  NoSuchElementException  data source name is not represented in the time series data collection
     * 
     * @see #getDataSourceNames()
     * @see DpSupportedType
     */
    public final DpSupportedType    getSourceType(String strSourceName) throws NoSuchElementException {
        
        // Check argument
        if (!this.hasSourceData(strSourceName))
            throw new NoSuchElementException("Data source not represented in time-series collection: " + strSourceName);
        
        return this.mapSrcNmToType.get(strSourceName);
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates and returns a new, ordered list of timestamps for all time-series data within this process.
     * </p>
     * <p>
     * The timestamps returned are for the <em>entire</em> stampling process.  It is a composite of all
     * sampling blocks within the sampling process. 
     * <p>
     * <h2>NOTES</h2>
     * This method should be called once.
     * <ul>
     * <li>
     * The returned timestamp list is <em>created</em> dynamically from all the sampling 
     * block components within the sampling process.  Thus, avoid repeated calls to this method. 
     * </li>
     * <br/>
     * <li>
     * If the timestamps are to be used repeatedly, the returned value should be saved locally.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  ordered list of timestamp instants for all time-series data within this sampling process
     */
    public ArrayList<Instant>    timestamps() {
        
        ArrayList<Instant>   lstTms = this.vecSmplBlocks
                .stream()
                .sequential()
                .collect(ArrayList::new, (lst, blk) -> lst.addAll(blk.getTimestamps()), (agg, lst) -> agg.addAll(lst));
        
        return lstTms;
    }
    
    /**
     * <p>
     * Creates and returns a new <code>{@link SampledTimeSeries}</code> instance containing all 
     * time-series data from the given data source.
     * </p>
     * <p>
     * The time-series for the given data source is returned for the <em>entire</em> sampling
     * process.  The returned value is a composite of all time-series data within component 
     * sampling blocks.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * This method should be called at most once for each data source.
     * <ul>
     * <li>
     * The returned time-series data set is <em>created</em> dynamically from all the sampling 
     * block components within the sampling process.  Thus, avoid repeated calls to this 
     * method for the same data source.
     * </li>
     * <br/>
     * <li>
     * If the time series for the given data source is to be used repeatedly, the returned value
     * should be saved locally.
     * </li>
     * </ul>
     * </p>
     * 
     * @param strSourceName     data source unique name
     * @return  new instance of <code>SampledTimeSeries</code> containing full time-series for given source
     * 
     * @throws NoSuchElementException the data source is not represented in this process
     */
    public SampledTimeSeries<Object>    timeSeries(String strSourceName) throws NoSuchElementException {
        
        // Check source name
        if (!this.hasSourceData(strSourceName))
            throw new NoSuchElementException("SamplingProcess contain no data source " + strSourceName);
        
        // Create the full time-series data-value vector for the given data source
        DpSupportedType         enmSrcType = this.getSourceType(strSourceName);
        ArrayList<Object>       vecSrcValues = new ArrayList<>(this.getSampleCount());
        
        for (UniformSamplingBlock blk : this.vecSmplBlocks) {
            
            // Check if data source is represented within current sampling block
            SampledTimeSeries<Object>   stms;
            if (blk.hasSourceData(strSourceName)) {
                stms = blk.getTimeSeries(strSourceName);
                
            } else {
                stms = SampledTimeSeries.nullSeries(strSourceName, enmSrcType, blk.getSampleCount());
                
            }

            // Add time-series data values to vector of full time series
            vecSrcValues.addAll(stms.getValues());
        }
        
        // Create the full time-series instance for the given data source and return it
        SampledTimeSeries<Object>   stmsNew = new SampledTimeSeries<Object>(strSourceName, enmSrcType, vecSrcValues);
        
        return stmsNew;
    }
    
    /**
     * <p>
     * Creates and returns a static data table backed by all data within this process.
     * </p>
     * <p>
     * Copies all time-series data into a static implementation of <code>{@link IDataTable}</code>.
     * The static table will contain columns for each time-series represented within the sampling blocks.
     * Sampling blocks without time-series data for a data source will have all their values set to
     * <code>null</code> within the table column.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This can be a resource intensive operation for large sampling processes.  Consider using a 
     * <code>{@link SamplingProcessTable}</code> instance for large processes.
     * </p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Use this method when index operations require performance.
     * </li>
     * <li>
     * The current implementation returns an <code>IDataTable</code> interface backed by class
     * <code>{@link StaticDataTable}</code>
     * </li>
     * </ul>
     * </p>  
     * 
     * @return  a static data table instance exposing <code>{@link IDataTable}</code> backed by this process data
     */
    public IDataTable createStaticDataTable() {
    
        // Create the collection of full time series for each data source
        List<IDataColumn<Object>>    lstCols;
        
        if (BOL_CONCURRENCY && (this.getDataSourceCount() > SZ_CONCURRENCY_PIVOT)) {
            lstCols = this.setSrcNms
                    .parallelStream()
                    .<IDataColumn<Object>>map(strNm -> this.timeSeries(strNm))
                    .toList();
            
        } else {
            lstCols = this.setSrcNms
                    .stream()
                    .<IDataColumn<Object>>map(strNm -> this.timeSeries(strNm))
                    .toList();
            
        }
        
        // Create the data table and return it
        List<Instant>   lstTms = this.timestamps();
        
        StaticDataTable     tblProcess = new StaticDataTable(lstTms, lstCols);
        
        return tblProcess;
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Verifies the correct ordering of the sampling start times within the argument set.
     * </p>
     * <p>
     * Extracts all starting times of the sampling clock in order to create the set
     * { <i>t</i><sub>0</sub>, <i>t</i><sub>1</sub>, ..., <i>t</i><sub><i>N</i>-1</sub> } where
     * <i>t<sub>n</sub></i> is the start time for <code>CorrelatedQueryData</code> instance <i>n</i>
     * and <i>N</i> is the size of the argument.   
     * The ordered collection of start times is compared sequentially to check the following 
     * conditions:
     * <pre>
     *   <i>t</i><sub>0</sub> < <i>t</i><sub>1</sub> < ... <  <i>t</i><sub><i>N</i>-1</sub>
     * </pre>
     * That is, we verify that the set 
     * { <i>t</i><sub>0</sub>, <i>t</i><sub>1</sub>, ..., <i>t</i><sub><i>N</i>-1</sub> }
     * forms a proper net.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This test MUST be run before <code>{@link #verifyTimeDomains(SortedSet)}</code>.</li>
     * <li>Failure messages are written to the class logger if logging is enabled.</li>
     * <li>The <code>ResultRecord</code> contains a message describing any failure.</li>
     * </ul>
     * </p>
     * 
     * @param setPrcdData  the target set of processed <code>CorrelatedQueryData</code> objects 
     * 
     * @return  <code>ResultRecord</code> containing result of test, with message if failure
     */
    private ResultRecord verifyStartTimes(SortedSet<CorrelatedQueryData> setPrcdData) {
        
        
        // Loop through set checking that all start times are in order
        int                 indPrev = 0;
        CorrelatedQueryData cqdPrev = null;
        for (CorrelatedQueryData cqdCurr : setPrcdData) {
            
            // Initialize the loop - first time through
            if (cqdPrev == null) {
                cqdPrev = cqdCurr;
                continue;
            }
            
            // Compare the two instants
            Instant     insPrev = cqdPrev.getStartInstant();
            Instant     insCurr = cqdCurr.getStartInstant();
            
            if (insPrev.compareTo(insCurr) >= 0) {
                if (BOL_LOGGING)
                    LOGGER.error("{}: Bad start time ordering at query data block {} with start time = {}", JavaRuntime.getCallerName(), Integer.toString(indPrev), insPrev);
                
                return ResultRecord.newFailure("Bad start time ordering for processed data block " + Integer.toString(indPrev) + " with start time = " + insPrev);
            }
            
            // Update state to next instant
            indPrev++;
            insPrev = insCurr;
        }
        
        return ResultRecord.SUCCESS;
    }

    /**
     * <p>
     * Verifies that sampling domains within the argument are disjoint.
     * </p>
     * <p>
     * Extracts the set of all sampling time domain intervals
     * { <i>I</i><sub>0</sub>, <i>I</i><sub>0</sub>, ..., <i>I</i><sub><i>N</i>-1</sub> } where
     * <i>I<sub>n</sub></i> is the sampling time domain for <code>CorrelatedQueryData</code> 
     * instance <i>n</i> and <i>N</i> is the size of the argument.  
     * We assume closed intervals of the form 
     * <i>I</i><sub><i>n</i></sub> = [<i>t</i><sub><i>n</i>,start</sub>, <i>t</i><sub><i>n</i>,end</sub>]
     * where <i>t</i><sub><i>n</i>,start</sub> is the start time for <code>CorrelatedQueryData</code> instance <i>n</i>
     * and <i>t</i><sub><i>n</i>,end</sub> is the stop time.
     * </p>
     * <p>  
     * The ordered collection of time domain intervals is compared sequentially to check the 
     * following conditions:
     * <pre>
     *   <i>I</i><sub>0</sub> &cap; <i>I</i><sub>1</sub> = &empty;
     *   <i>I</i><sub>1</sub> &cap; <i>I</i><sub>2</sub> = &empty;
     *   ...
     *   <i>I</i><sub><i>N</i>-2</sub> &cap; <i>I</i><sub><i>N</i>-1</sub> = &empty;
     *   
     * </pre>
     * That is, every <em>adjacent</em> sampling time domain is disjoint.
     * This algorithm is accurate <em>only if</em> the argument set is correctly ordered
     * by sampling start times. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The argument MUST have correct start time order for this algorithm to work.</li>
     * <li>Failure messages are written to the class logger if logging is enabled.</li>
     * <li>The <code>ResultRecord</code> contains a message describing any failure.</li>
     * </ul>
     * </p>
     * 
     * @param setPrcdData  the target set of <code>CorrelatedQueryData</code> objects, corrected ordered 
     * 
     * @return  <code>ResultRecord</code> containing result of test, with message if failure
     */
    private ResultRecord verifyTimeDomains(SortedSet<CorrelatedQueryData> setPrcdData) {
        
        // Check that remaining time domains are disjoint - this works if the argument is ordered correctly
        // Loop through set checking that all start times are in order
        int                 indPrev = 0;
        CorrelatedQueryData cqdPrev = null;
        for (CorrelatedQueryData cqdCurr : setPrcdData) {
            
            // Initialize the loop - first time through
            if (cqdPrev == null) {
                cqdPrev = cqdCurr;
                continue;
            }
            
            // Extract time domains and check for closed intersection
            TimeInterval    domPrev = cqdPrev.getTimeDomain();
            TimeInterval    domCurr = cqdCurr.getTimeDomain();
            
            if (domPrev.hasIntersectionClosed(domCurr)) {
                if (BOL_LOGGING)
                    LOGGER.error("{}: Collision between time domains at query data block {}, {} with {}.", JavaRuntime.getCallerName(), Integer.toString(indPrev), domCurr, domPrev);
                
                return ResultRecord.newFailure("Time domain collision at query data block " + Integer.toString(indPrev) + ": " + domCurr + " with " + domPrev);
            }
            
            indPrev++;
            domPrev = domCurr;
        }
        
        return ResultRecord.SUCCESS;
    }

    /**
     * @param setQueryData
     * @return
     * 
     * @deprecated replaced by {@link #verifyStartTimes(SortedSet)} and {@link #verifyTimeDomains(SortedSet)}
     */
    @Deprecated(since="Feb 2, 2024", forRemoval=true)
    private ResultRecord verifyTimeRanges(SortedSet<CorrelatedQueryData> setQueryData) {
        
        // Check all query data for correct ordering and time domain collisions
        CorrelatedQueryData cqdFirst = setQueryData.first();
        
        setQueryData.remove(cqdFirst);
        CorrelatedQueryData cqdPrev = cqdFirst;
        for (CorrelatedQueryData cqdCurr : setQueryData) {
            
            // Check sampling start time order
            Instant insPrev = cqdPrev.getStartInstant();
            Instant insCurr = cqdCurr.getStartInstant();
            
            if (insPrev.compareTo(insCurr) >= 0) {
                setQueryData.add(cqdFirst);
    
                if (BOL_LOGGING)
                    LOGGER.error("{}: Bad ordering in start times, {} ordred after {}.", JavaRuntime.getCallerName(), insPrev, insCurr);
                
                return ResultRecord.newFailure("Bad ordering in start times " + insPrev + " ordered after " + insCurr);
            }
    
            // Since collection is ordered, only need to check proximal domains
            TimeInterval    domPrev = cqdPrev.getTimeDomain();
            TimeInterval    domCurr = cqdCurr.getTimeDomain();
            
            if (domPrev.hasIntersectionClosed(domCurr)) {
                setQueryData.add(cqdFirst);
    
                if (BOL_LOGGING)
                    LOGGER.error("{}: Collision between sampling domains {} and {}.", JavaRuntime.getCallerName(), domPrev, domCurr);
                
                return ResultRecord.newFailure("Collision between sampling domains " + domPrev + " and " + domCurr);
            }
                
        }
        setQueryData.add(cqdFirst);
    
        return ResultRecord.SUCCESS;
    }

    /**
     * <p>
     * Creates all the sampling blocks for this sampling process, according to the argument data.
     * </p>
     * <p>
     * One <code>UniformSamplingBlock</code> instance is created for each <code>CorrelatedQueryData</code>
     * object in the argument collection.  The <code>UniformSamplingBlock</code> instances are created
     * in the same order as the argument, which is assumed to be by sampling start time.  The order
     * should have been enforced during query response correlation.
     * Note that not all data sources need contribute to each <code>CorrelatedQueryData</code> 
     * instance in the argument.  The time-series data values (i.e., <code>null</code> values)
     * for missing data sources should be addressed later. 
     * </p>
     * <p>
     * <h2>Concurrency</h2>
     * If concurrency is enabled (i.e., <code>{@link #BOL_CONCURRENCY}</code> = <code>true</code>),
     * this method utilizes streaming parallelism if the argument size is greater than the
     * pivot number <code>{@link #SZ_CONCURRENCY_PIVOT}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li><s>The argument is must be ordered by starting time instant, or exception is thrown.</s></li>
     * <li><s>Arguments must not contain intersecting time domain, or exception is thrown.</s></li>
     * <li>All exceptions are thrown by the internal invocation of <code>{@link UniformSamplingBlock#from}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param setQueryData  set of <code>CorrelatedQueryData</code> objects ordered by start time
     * 
     * @return  vector of <code>UniformSamplingBlock</code> objects ordered according to argument
     * 
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * throws RangeException           the argument has bad ordering or contains time domain collisions
     * @throws TypeNotPresentException an unsupported data type was detected within the argument
     */
    private ArrayList<UniformSamplingBlock>    buildSamplingBlocks(SortedSet<CorrelatedQueryData> setQueryData) 
            throws MissingResourceException, IllegalArgumentException, IllegalStateException, /* RangeException,*/ TypeNotPresentException {
        
        // Create the sample blocks, pivoting to concurrency by container size
        ArrayList<UniformSamplingBlock>    vecSmplBlocks;
        
        if (BOL_CONCURRENCY && (setQueryData.size() > SZ_CONCURRENCY_PIVOT) ) {
            // invoke concurrency
            UniformSamplingBlock           arrSmplBlocks[] = new UniformSamplingBlock[setQueryData.size()];
//            vecSmplBlocks = new ArrayList<>(setQueryData.size());
            ArrayList<CorrelatedQueryData> vecQueryData = new ArrayList<>(setQueryData);
            
            IntStream.range(0, setQueryData.size())
                .parallel()
                .forEach(
                        i -> {arrSmplBlocks[i] = UniformSamplingBlock.from(vecQueryData.get(i)); } 
//                        i -> {vecSmplBlocks.set(i, UniformSamplingBlock.from(vecQueryData.get(i))); } 
                        );

//            vecSmplBlocks = Arrays.asList(arrSmplBlocks);
          vecSmplBlocks = new ArrayList<>(setQueryData.size());
          for (UniformSamplingBlock block : arrSmplBlocks) {
              vecSmplBlocks.add(block);
          }
            
        } else {
            // serial processing
            vecSmplBlocks = new ArrayList<>(setQueryData.size());

            setQueryData.stream()
                .sequential()
                .forEachOrdered(
                        cqd -> vecSmplBlocks.add(UniformSamplingBlock.from(cqd))
                        );
        }
        
        return vecSmplBlocks;
    }
    
    /**
     * <p>
     * Extracts and returns the collection of unique data source names for all data within the argument.
     * </p>
     * <p>
     * Collects all data source names within the set of correlated data and creates a set
     * of unique sources names.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Since the returned set of data source names is obtained directly from the initializing
     * data, it can be used in verification against the process sampling blocks created from
     * the same data.
     * </p>
     * 
     * @param setQueryData  set of <code>CorrelatedQueryData</code> objects used to initialize process
     * 
     * @return  set of all data source names within the Query Service correlated data collection 
     */
    private SortedSet<String>    buildSourceNameSet(SortedSet<CorrelatedQueryData> setQueryData) {
        SortedSet<String> setNames = setQueryData
                .stream()
                .collect(
                        TreeSet::new, 
                        (set, r) -> set.addAll(r.getSourceNames()), 
                        TreeSet::addAll
                        );
        return setNames;
    }
    
    /**
     * <p>
     * Builds and returns a map of data source names to data source type for all sources in 
     * the argument.
     * </p>
     * <p>
     * Collects all data source names within the collection of sampling blocks and creates a 
     * map of (unique) sources names keyed to their data type.
     * </p>
     * <p>
     * The returned map contains entries for all data sources represented in the argument.
     * Not that the key set of the returned map should be equal to the set of data source
     * name obtained from the initializing collection of <code>CorrelatedQueryData</code>
     * objects.  Thus, the returned map can be used both to verify the set of data sources
     * within the process, and to verify that all sources have uniform data type. 
     * <p>
     * <h2>Concurrency</h2>
     * If concurrency is enabled (i.e., <code>{@link #BOL_CONCURRENCY}</code> = <code>true</code>),
     * this method utilizes streaming parallelism if the argument size is greater than the
     * pivot number <code>{@link #SZ_CONCURRENCY_PIVOT}</code>.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * The map will contain the value <code>{@link DpSupportedType#UNSUPPORTED_TYPE}</code> 
     * for any data source producing time series data with inconsistent types.  This condition
     * is indicative of a corrupt archive and should be checked.
     * </p> 
     *  
     * @param vecBlocks the entire collection of sampling blocks within this process
     * 
     * @return  map of {(source name, source type)} entries for all data sources within argument
     */
    private Map<String, DpSupportedType>    buildSourceNameToTypeMap(List<UniformSamplingBlock> vecBlocks) {
        
        // Local type used for intermediate results
        record Pair(String name, DpSupportedType type) {};
        
        // Note that <Pair>flatMap operation is implementing the following function
        @SuppressWarnings("unused")
        Function<UniformSamplingBlock, Stream<Pair>> f = (usb) -> {
            return usb.
                    getSourceNames()
                    .stream()
                    .<Pair>map(name -> new Pair(name, usb.getSourceType(name)));
        };

        // Mapping function for value merging when multiple keys are encountered
        BinaryOperator<DpSupportedType> bopValMerger = (valOld, valNew) -> {
            if (valOld == null)
                return valNew;
            if (valNew == null)
                return valOld;
            if (valNew == valOld)
                return valOld;

            // If we are here - This indicates different types for sample PV 
            return DpSupportedType.UNSUPPORTED_TYPE;
        };
        
        // Create the {(source name, source type)} map, pivoting to concurrency on container size  
        Map<String, DpSupportedType>    mapSrcToType; // = new HashMap<>();
        
        if (BOL_CONCURRENCY && (vecBlocks.size() > SZ_CONCURRENCY_PIVOT) ) {
            // invoke concurrency in UniformSamplingBlock instance processing
            mapSrcToType = vecBlocks
                    .parallelStream()
                    .<Pair>flatMap(usb -> usb
                                            .getSourceNames()
                                            .stream()
                                            .map( name -> new Pair(name, usb.getSourceType(name)) )
                                )
                    .collect(
                            Collectors.toConcurrentMap(pair -> pair.name, pair -> pair.type, bopValMerger)
                            );
            
        } else {
            // process serially
            mapSrcToType = vecBlocks
                    .stream()
                    .<Pair>flatMap(usb -> usb
                                            .getSourceNames()
                                            .stream()
                                            .<Pair>map(name -> new Pair(name, usb.getSourceType(name)))
                                )
                    .collect(
                            Collectors.toMap(pair -> pair.name, pair->pair.type, bopValMerger)
                            );

        }
        
        // Return the populated map
        return mapSrcToType;
    }
    
    /**
     * <p>
     * Computes the time domain of the sampling process from the ordered collection of
     * sampling blocks within the process.
     * </p>
     * <p>
     * The returned time interval is determined from the starting instant of the first
     * sampling block and the time domain of the last sampling block (i.e., the right
     * end point of the time interval).
     * </p>
     * 
     * @param vecBlocks the entire (ordered) collection of sampling blocks within this process
     * 
     * @return  the time domain over which this sample process has values
     */
    private TimeInterval    computeTimeDomain(List<UniformSamplingBlock> vecBlocks) {
        UniformSamplingBlock    blkFirst = vecBlocks.get(0);
        UniformSamplingBlock    blkLast = vecBlocks.get(vecBlocks.size() - 1);
        
        Instant insStart = blkFirst.getStartInstant();
        Instant insStop = blkLast.getTimeDomain().end();
        
        return TimeInterval.from(insStart, insStop);
    }
    
    /**
     * <p>
     * Computes the total size of each time series represented in the sampling process.
     * </p>
     * <p>
     * The total size of each time series within the sample process is assumed to be the same.
     * The returned value is computed by summing the number of clock samples within each sampling
     * block.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * It is quite possible that not every data source is represented in each component sampling
     * block.  If the time series of a data source is missing within a given sampling block we
     * represent its sample values there as <code>null</code> values.
     * </p>
     * 
     * @param vecBlocks the collection of sampling blocks composing this sample process
     * 
     * @return  total size of each time series within the sample process
     */
    private int computeSampleCount(List<UniformSamplingBlock> vecBlocks) {
        int cntSamples = vecBlocks
                .stream()
                .mapToInt(cqd -> cqd.getSampleCount())
                .reduce(0, Integer::sum);
        
        return cntSamples;
    }
    
    /**
     * <p>
     * Verifies the correct ordering of the sampling start times within the argument set.
     * </p>
     * <p>
     * Extracts all starting times of the sampling clock in order to create the set
     * { <i>t</i><sub>0</sub>, <i>t</i><sub>1</sub>, ..., <i>t</i><sub><i>N</i>-1</sub> } where
     * <i>t<sub>n</sub></i> is the start time for <code>UniformSamplingBlock</code> instance 
     * <i>n</i> and <i>N</i> is the size of the argument.   
     * The ordered collection of start times is compared sequentially to check the following 
     * conditions:
     * <pre>
     *   <i>t</i><sub>0</sub> < <i>t</i><sub>1</sub> < ... <  <i>t</i><sub><i>N</i>-1</sub>
     * </pre>
     * That is, we verify that the set 
     * { <i>t</i><sub>0</sub>, <i>t</i><sub>1</sub>, ..., <i>t</i><sub><i>N</i>-1</sub> }
     * forms a proper net.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This test MUST be run before <code>{@link #verifyTimeDomains(Vector)}</code>.</li>
     * <li>Failure messages are written to the class logger if logging is enabled.</li>
     * <li>The <code>ResultRecord</code> contains a message describing any failure.</li>
     * </ul>
     * </p>
     * 
     * @param lstBlocks the constructed list of <code>UniformSamplingBlock</code> objects for this process 
     * 
     * @return  <code>ResultRecord</code> containing result of test, with message if failure
     */
    private ResultRecord verifyStartTimes(List<UniformSamplingBlock> lstBlocks) {
        
        // Loop through all ordered blocks - Check that all start times are in order
        int                     indPrev = 0;
        UniformSamplingBlock    blkPrev = null;
        for (UniformSamplingBlock blkCurr : lstBlocks) {
            
            // Initialize loop - first time through
            if (blkPrev == null) {
                blkPrev = blkCurr;
                continue;
            }
            
            // Compare the two instants
            Instant insPrev = blkPrev.getStartInstant();
            Instant insCurr = blkCurr.getStartInstant();
            
            if (insPrev.compareTo(insCurr) >= 0) {
                if (BOL_LOGGING)
                    LOGGER.error("{}: Bad start time ordering at sample block {} with start time = {}", JavaRuntime.getCallerName(), Integer.toString(indPrev), insPrev);
                
                return ResultRecord.newFailure("Bad start time ordering for sample block " + Integer.toString(indPrev) + " with start time = " + insPrev);
            }
            
            // Update state to next instant
            indPrev++;
            insPrev = insCurr;
        }
        
        return ResultRecord.SUCCESS;
    }
    
    /**
     * <p>
     * Verifies that sampling domains within the argument are disjoint.
     * </p>
     * <p>
     * Extracts the set of all sampling time domain intervals
     * { <i>I</i><sub>0</sub>, <i>I</i><sub>0</sub>, ..., <i>I</i><sub><i>N</i>-1</sub> } where
     * <i>I<sub>n</sub></i> is the sampling time domain for <code>UniformSamplingBlock</code> 
     * instance <i>n</i> and <i>N</i> is the size of the argument.  
     * We assume closed intervals of the form 
     * <i>I</i><sub><i>n</i></sub> = [<i>t</i><sub><i>n</i>,start</sub>, <i>t</i><sub><i>n</i>,end</sub>]
     * where <i>t</i><sub><i>n</i>,start</sub> is the start time for <code>UniformSamplingBlock</code> instance <i>n</i>
     * and <i>t</i><sub><i>n</i>,end</sub> is the stop time.
     * </p>
     * <p>  
     * The ordered collection of time domain intervals is compared sequentially to check the 
     * following conditions:
     * <pre>
     *   <i>I</i><sub>0</sub> &cap; <i>I</i><sub>1</sub> = &empty;
     *   <i>I</i><sub>1</sub> &cap; <i>I</i><sub>2</sub> = &empty;
     *   ...
     *   <i>I</i><sub><i>N</i>-2</sub> &cap; <i>I</i><sub><i>N</i>-1</sub> = &empty;
     *   
     * </pre>
     * That is, every <em>adjacent</em> sampling time domain is disjoint.
     * This algorithm is accurate <em>only if</em> the argument set is correctly ordered
     * by sampling start times. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The argument MUST have correct start time order for this algorithm to work.</li>
     * <li>Failure messages are written to the class logger if logging is enabled.</li>
     * <li>The <code>ResultRecord</code> contains a message describing any failure.</li>
     * </ul>
     * </p>
     * 
     * @param lstBlocks the constructed list of <code>UniformSamplingBlock</code> objects for this process 
     * 
     * @return  <code>ResultRecord</code> containing result of test, with message if failure
     */
    private ResultRecord verifyTimeDomains(List<UniformSamplingBlock> lstBlocks) {
        
        // Loop through all ordered blocks - Check that time domains are disjoint 
        // - this works if the argument is ordered correctly
        int                     indPrev = 0;
        UniformSamplingBlock    blkPrev = null;
        for (UniformSamplingBlock blkCurr : lstBlocks) {
            
            // Initialize loop - first time through
            if (blkPrev == null) {
                blkPrev = blkCurr;
                continue;
            }

            // Extract time domains and check for closed intersection
            TimeInterval    domPrev = blkPrev.getTimeDomain();
            TimeInterval    domCurr = blkCurr.getTimeDomain();
            
            if (domPrev.hasIntersectionClosed(domCurr)) {
                if (BOL_LOGGING)
                    LOGGER.error("{}: Collision between time domains at sampling block {}, {} with {}.", JavaRuntime.getCallerName(), Integer.toString(indPrev), domPrev, domCurr);
                
                return ResultRecord.newFailure("Time domain collision at sample block " + Integer.toString(indPrev) + ": " + domPrev + " with " + domCurr);
            }
            
            indPrev++;
            domPrev = domCurr;
        }
        
        return ResultRecord.SUCCESS;
    }
    
    /**
     * <p>
     * Verifies that all data sources within the argument set produce data of the same type.
     * </p>
     * <p>
     * Performs the following verification checks:
     * <ol>
     * <li>
     * First the attribute <code>{@link #mapSrcNmToType}</code> is inspected for values
     * of <code>{@link DpSupportedType#UNSUPPORTED_TYPE}</code>, which indicates a 
     * time series with inconsistent data types.  If that value is found a FAILURE result
     * is returned immediately.
     * </li>
     * <br/>
     * <li>
     * Then the method extracts pairs all pairs {(source name, source type)} from each instance 
     * of <code>UniformSamplingBlock</code> within the argument collection.
     * Each pair is then checked against the data types within (previously constructed) map 
     * <code>{@link #mapSrcNmToType}</code>.  Any data sources that have a different
     * type are recorded and contained within the returned <code>ResultRecord</code> object.
     * </li>
     * </ol>
     * </p>
     * <h2>Concurrency</h2>
     * <ul>
     * <li>
     * If concurrency is enabled (i.e., <code>{@link #BOL_CONCURRENCY}</code> = <code>true</code>),
     * this method utilizes forked streaming parallelism for each 
     * <code>UniformSamplingBlock</code> if the argument size is greater 
     * than the pivot number <code>{@link #SZ_CONCURRENCY_PIVOT}</code>.
     * </li>
     * <br/>
     * <li>
     * If concurrency is enabled (i.e., <code>{@link #BOL_CONCURRENCY}</code> = <code>true</code>),
     * this method ALWAYS applies concurrency while processing each data source within each
     * <code>UniformSamplingBlock</code> instance.
     * </li>
     * <br/>
     * <li>
     * If concurrency is disabled (i.e., <code>{@link #BOL_CONCURRENCY}</code> = <code>false</code>)
     * all processing is done serially (using Java streams).
     * </li>
     * </ul>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The internal resource <code>{@link #mapSrcNmToType}</code> MUST be constructed before use.</li>
     * <li>Set ordering here is not relevant, but does affect any failure message produced.</li>
     * <li>Failure messages are written to the class logger if logging is enabled.</li>
     * <li>The <code>ResultRecord</code> contains a message describing any failure.</li>
     * </ul>
     * </p>
     * 
     * @param lstBlocks the constructed list of <code>UniformSamplingBlock</code> objects for this process 
     * 
     * @return  <code>ResultRecord</code> containing result of test, with message if failure
     */
    private  ResultRecord verifySourceTypes(List<UniformSamplingBlock> lstBlocks) {
        
        // First check for UNSUPPORTED_TYPE in the (pv name, pv type) map
        List<String> lstBadPvs = this.mapSrcNmToType
                .entrySet()
                .stream()
                .filter(entry -> (entry.getValue() == DpSupportedType.UNSUPPORTED_TYPE))
                .<String>map(Map.Entry::getKey)
                .toList();
        
        if (!lstBadPvs.isEmpty())
            return ResultRecord.newFailure("Sampling process contains PVs with inconsistent types: " + lstBadPvs);
        
        // Local type used for intermediate results
        record Pair(String name, DpSupportedType type) {};
        
        // Get list of sources with incorrect data types
        List<Pair> lstBadSrcs;
        
        // Pivot concurrency on the size of container (always use concurrency within sampling blocks)
        if (BOL_CONCURRENCY && (lstBlocks.size() > SamplingProcess.SZ_CONCURRENCY_PIVOT) ) {
            
            // Process vector concurrently, blocks concurrently 
            lstBadSrcs = lstBlocks
                    .parallelStream()
                    .<Pair>flatMap(usb -> usb
                            .getSourceNames()
                            .parallelStream()
                            .<Pair>map(name -> new Pair(name, usb.getSourceType(name)) )
                            )
                    .filter(pair -> this.getSourceType(pair.name) != pair.type)
                    .toList();
            
        } else if (BOL_CONCURRENCY) {

            // Process vector serially, blocks concurrently 
            lstBadSrcs = lstBlocks
                    .stream()
                    .<Pair>flatMap(usb -> usb
                            .getSourceNames()
                            .parallelStream()
                            .<Pair>map(name -> new Pair(name, usb.getSourceType(name)) )
                            )
                    .filter(pair -> this.getSourceType(pair.name) != pair.type)
                    .toList();
            
            
        } else {
            
            // Process everything serially
            lstBadSrcs = lstBlocks
                    .stream()
                    .<Pair>flatMap(usb -> usb
                            .getSourceNames()
                            .stream()
                            .<Pair>map(name -> new Pair(name, usb.getSourceType(name)) )
                            )
                    .filter(pair -> this.getSourceType(pair.name) != pair.type)
                    .toList();
            
        }

        // If the list is non-empty, there are inconsistent data types
        if (!lstBadSrcs.isEmpty())
            return ResultRecord.newFailure("Sampling blocks contained mixed-type data sources: " + lstBadSrcs);
        
        return ResultRecord.SUCCESS;
    }
    
//    private Vector<Instant> computeTimestamps(Vector<UniformSamplingBlock> vecSmplBlocks) {
//        int cntTms = vecSmplBlocks.stream().map(usb -> usb.getSampleCount()).reduce(0, Integer::sum);
//    }
    
//    private List<SamplingPair>  buildSamplingPairs(SortedSet<CorrelatedQueryData> setRefs) throws IllegalStateException {
//        List<SamplingPair>  lstPairs = new ArrayList<>(setRefs.size());
//        
//        CorrelatedQueryData refFirst = setRefs.first();
//        
//        setRefs.remove(refFirst);
//        
//        UniformSamplingClock  setPrev = UniformSamplingClock.from(refFirst.getSamplingMessage());
//        lstPairs.add( SamplingPair.of(setPrev, refFirst.getAllDataMessages()) ) ;
//        for (CorrelatedQueryData ref : setRefs) {
//            UniformSamplingClock  setCurr = UniformSamplingClock.from(ref.getSamplingMessage());
//            
//            if (setCurr.hasDomainIntersection(setPrev))
//                throw new IllegalStateException("Collision between internal sampling domains.");
//            
//            lstPairs.add( SamplingPair.of(setCurr, ref.getAllDataMessages()) );
//        }
//        
//        setRefs.add(refFirst);
//        
//        return lstPairs;
//    }
}
