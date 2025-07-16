/*
 * Project: dp-api-common
 * File:	SampledAggregate.java
 * Package: com.ospreydcs.dp.api.query.model.assem
 * Type: 	SampledAggregate
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
 * @since Apr 13, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.assem;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.ospreydcs.dp.api.common.DpSupportedType;
import com.ospreydcs.dp.api.common.IDataColumn;
import com.ospreydcs.dp.api.common.IDataTable;
import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.model.table.StaticDataTable;
import com.ospreydcs.dp.api.query.model.coalesce.SampledBlock;
import com.ospreydcs.dp.api.query.model.coalesce.SampledTimeSeries;
import com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData;
import com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator;
import com.ospreydcs.dp.api.query.model.table.SampledAggregateTable;
import com.ospreydcs.dp.api.query.model.table.SamplingProcessTable;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Defines a collection of <code>SampledBlock</code> instances representing sampled processes returned
 * from a Query Service request.
 * </p>
 * </p> 
 * The collections contains time-series data from multiple sources over multiple time ranges.  The collection
 * represents the penultimate step in processing of Query Service responses.
 * </p>
 * <p>
 * Note that a <code>SampledBlock</code> represents the data set of a multiple sampled-time
 * processes involving multiple data sources correlated to a set of timestamps.
 * A <code>SampledAggreate</code> instance is an aggregation of disjoint, ordered 
 * <code>SampledBlock</code> objects, each block containing the time-series data for
 * a sub-range of the processes full duration.  Thus, a <code>SampledAggregate</code>
 * need not be uniform throughout its time domain.  There can be empty time ranges ("empty block"), 
 * uniform time ranges, and explicitly timestamped (e.g., "spurious") time ranges.
 * </p>
 * <p>
 * <h2>Creation and Assembly</h2>
 * The class <code>QueryResponseAssembler</code> is generally used to create an instance of <code>SampledAggregate</code>.
 * The <code>QueryResponseAssembler</code> class accepts a sorted set of <code>RawCorrelatedData</code> objects generated
 * by a <code>RawDataCorrelator</code> instance and processes them into <code>SampledBlock</code> instances.
 * Note that any time domain collisions are typically handled within the <code>QueryResponseAssembler</code> class by a 
 * <code>TimeDomainProcessor</code> component if they occur.
 * </p>
 * <p>
 * <code>SampledAggregate</code> instances are created in the empty state, they contain no data at inception.
 * The method <code>{@link #add(SampledBlock)}</code> is used to assemble the new <code>SampledAggregate</code>
 * instance from <code>SampledBlock</code> subclass instances.  <code>SampledBlock</code> instances
 * are created from raw correlated data represented by sub-classed instances of the <code>RawCorrelatedData</code>
 * class.  Thus, the <code>SampledAggregate</code> class is part of a processing pipeline for the recovery of
 * Query Service time-series data requests.
 * </p>
 * <p>
 * Note that <code>SampledAggregate</code> instances are created from the processed data
 * sets produced by the class <code>{@link RawDataCorrelator}</code>.
 * That class takes the raw data obtained from a time-series data request of the Query Service then correlates
 * into an ordered sets of <code>{@link RawCorrelatedData}</code> instances, each instance 
 * correlated to the same timestamps.  
 * These correlated results sets are obtained from 
 * <code>{@link RawDataCorrelator#getCorrelatedSet()}</code>.  The correlated results sets
 * are then used in the creation of <code>SampledAggregate</code> instances. 
 * Thus again, this class is used in the reconstruction chain for Data Platform 
 * Query Service time-series data requests.
 * </p>
 * <h2>Population</h2>
 * <code>SampledAggregate</code> instances are populated using repeated invocations of the 
 * <code>{@link #add(SampledBlock)}</code> method.  The <code>SampledBlock</code> instances are
 * ordered within the aggregate as they are added, according to their natural ordering (i.e, the
 * start time).  Thus, <code>SampledBlock</code> instances can be added in any order.  However,
 * adding a <code>SampledBlock</code> instance is necessarily an atomic operation and is thus synchronized
 * for use with multi-threading.
 * </p>
 * <p>
 * <h2>Data Verification</h2>
 * There are a set of methods for verification of the aggregate data set once populated; these methods 
 * are suffixed as <code>verify...()</code>.  Although they are intended to verification once fully populated,
 * they may be called at any time and reflect the current state of the aggregate.
 * </p>
 * <p>
 * <h2>Usage</h2>
 * Once a <code>SampledAggregate</code> object has been assembled from a collection of <code>SampledBlock</code>
 * instances using <code>{@link #add(SampledBlock)}</code>, the remaining public methods are available for inspection.
 * General properties of the aggregate are available through the "getter" methods.  Component sample blocks are
 * available via <code>{@link #sampledBlock(int)}</code> and <code>{@link #getSamplingBlocks()}</code>.
 * The methods <code>{@link #timestamps()}</code> and <code>{@link #timeSeries(String)}</code> are of particular
 * importance.  <em>These are not getter methods</em> and should be used sparingly, preferably only once.  They
 * both actively construct aggregate data from all composite sample block depending on the current state.
 * </p> 
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * It is quite possible that not every data source is represented in each component sampling
 * block.  If the time series of a data source is missing within a given sampling block we
 * represent its sample values there as <code>null</code> values.
 * </li>
 * <li>
 * The methods <code>{@link #timestamps()}</code> and <code>{@link #timeSeries(String)}</code> are available
 * at any time and will return values reflecting the current state of the <code>SampledAggregate</code>
 * instance.  If a <code>SampledAggregate</code> object has been populated with all time-series data
 * recovered from a query request, then the methods return values for the full request.
 * </li>
 * </ul>
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
 * @since Apr 13, 2025
 *
 */
public class SampledAggregate implements Iterable<SampledBlock> {
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new, empty <code>SampledAggregate</code> instance ready for population.
     * </p>
     * <p>
     * <h2>Population</h2>
     * <code>SampledAggregate</code> instances are populated using repeated invocations of the 
     * <code>{@link #add(SampledBlock)}</code> method.  The <code>SampledBlock</code> instances are
     * ordered within the aggregate as they are added, according to their natural ordering (i.e, the
     * start time).  Thus, <code>SampledBlock</code> instances can be added in any order.  However,
     * adding a <code>SampledBlock</code> instance is necessarily an atomic operation and is thus synchronized
     * for use with multi-threading.
     * </p>
     * <p>
     * <h2>Data Verification</h2>
     * There are a set of methods for verification of the aggregate data set once populated; these methods 
     * are suffixed as <code>verify...()</code>.  Although they are intended to verification once fully populated,
     * they may be called at any time and reflect the current state of the aggregate.
     * </p>
     * 
     * @return  a new, empty <code>SampledAggregate</code> instance ready for data population
     */
    public static SampledAggregate  from() {
        return new SampledAggregate();
    }
    
    /**
     * <p>
     * Creates and returns a new, empty <code>SampledAggregate</code> instance with the given request identifier.
     * </p>
     * <p>
     * <h2>Population</h2>
     * The returned instance is empty and ready for sampled block population.
     * <code>SampledAggregate</code> instances are populated using repeated invocations of the 
     * <code>{@link #add(SampledBlock)}</code> method.  The <code>SampledBlock</code> instances are
     * ordered within the aggregate as they are added, according to their natural ordering (i.e, the
     * start time).  Thus, <code>SampledBlock</code> instances can be added in any order.  However,
     * adding a <code>SampledBlock</code> instance is necessarily an atomic operation and is thus synchronized
     * for use with multi-threading.
     * </p>
     * <p>
     * <h2>Data Verification</h2>
     * There are a set of methods for verification of the aggregate data set once populated; these methods 
     * are suffixed as <code>verify...()</code>.  Although they are intended to verification once fully populated,
     * they may be called at any time and reflect the current state of the aggregate.
     * </p>
     * 
     * @param strRqstId request ID of the original time-series data request
     * 
     * @return  a new, empty <code>SampledAggregate</code> instance ready for data population
     */
    public static SampledAggregate  from(String strRqstId) {
        return new SampledAggregate(strRqstId);
    }

    
    //
    // Application Resources
    //
    
    /** The Data Platform API default configuration parameter set */
    protected static final DpQueryConfig  CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** Logging enabled flag */
    public static final boolean     BOL_LOGGING = CFG_QUERY.logging.enabled;
    
    /** Logging event level */
    public static final String      STR_LOGGING_LEVEL = CFG_QUERY.logging.level;
    
    
    /** Use advanced error checking and verification flag */
    public static final boolean     BOL_ERROR_CHK = CFG_QUERY.data.table.construction.errorChecking;
    
    
    /** Concurrency enabled flag */
    public static final boolean     BOL_CONCURRENCY = CFG_QUERY.concurrency.enabled;
    
    /** Concurrency maximum thread count */
    public static final int         CNT_CONCURRENCY_MAX_THRDS = CFG_QUERY.concurrency.maxThreads;
    
    /** Concurrency tuning parameter - pivot to parallel processing when lstMsgDataCols size hits this limit */
    public static final int        SZ_CONCURRENCY_PIVOT = CFG_QUERY.concurrency.pivotSize;
    

    //
    // Class Resources
    //
    
    /** Event logger for class */
    protected static final Logger LOGGER = LogManager.getLogger();
    

    /**
     * <p>
     * Class Initialization - Initializes the event logger, sets logging level.
     * </p>
     */
    static {
        Configurator.setLevel(LOGGER, Level.toLevel(STR_LOGGING_LEVEL, LOGGER.getLevel()));
    }
    
    
    //
    // Attributes
    //
    
    /** The optional request identifier of the original time-series data request */
    private String          strRqstId = null;

    /** The number of samples within each time-series */
    private int             cntSamples = 0;
    
//    /** The time domain of the sampling process */
//    private TimeInterval          ivlDomain;
    
    /** The first timestamp for aggregate */
    private Instant         insTmsFirst = Instant.EPOCH;
    
    /** The last timestamp for the aggregate */
    private Instant         insTmsLast = Instant.EPOCH;
    
    
    /** The set of all unique data sources names - taken from initializing correlated query data */
    private final SortedSet<String>             setSrcNms = new TreeSet<>();
    
    /** Map of data source names to their data type - taken from processed sampling blocks */
    private final Map<String, DpSupportedType>  mapSrcNmToType = new HashMap<>();
    
//    /** Ordered vector of all timestamps for sampled process */
//    private final ArrayList<Instant>            vecTms = new ArrayList<>();
    
    /** Ordered set of sampled blocks comprising the aggregated sampled process */
    private final SortedSet<SampledBlock>       setSmplBlocks = new TreeSet<>(SampledBlock.StartTimeComparator.newInstance());
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new, empty <code>SampledAggregate</code> instance.
     * </p>
     *
     */
    public SampledAggregate() {
    }
    
    /**
     * <p>
     * Constructs a new <code>SampledAggregate</code> instance with the given time-series data request identifier.
     * </p>
     *
     * @param strRqstId request identifier for the original time-series data request
     */
    public SampledAggregate(String strRqstId) {
        this.strRqstId = strRqstId;
    }
    
    
    //
    // Iterable<SampledBlock> Interface
    //
    
    /**
     * <p>
     * Returns an iterator over all the <code>SampleBlock</code> instances comprising this aggregate.
     * </p>
     * <p>
     * The returned iterator supplies all the <code>SampledBlock</code> instances currently in the aggregate.
     * The order of the <code>SampledBlock</code> instances is that of starting timestamp.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned iterator is state dependent; specifically, the iterator supplies the 
     * <code>SampledBlock</code> instances in the aggreate's current state.  Any blocks added later
     * are not considered.
     * </p>
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<SampledBlock> iterator() {
        return this.setSmplBlocks.iterator();
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Assigns the (optional) identifier of the original time-series data request.
     * </p>
     * <p>
     * <s>For proper operation invoke this method before assembling the sampled aggregate, that is,
     * before calling <code>{@link #add(SampledBlock)}</code>.  The given argument will then be
     * assigned as the request ID for each sampled block in the aggregate.
     * </s>  
     * </p>
     * <p>
     * This method can be called at any time.  However, in order that the request ID field be 
     * available for any data tables it must be called before <code>{@link #createDynamicDataTable()}</code>
     * or <code>{@link #createStaticDataTable()}</code>. 
     * <p>
     * Note that the <code>SampledBlock</code> class exposes the <code>IDataTable</code> interface 
     * through which the request identifier is available.
     * </p>
     *  
     * @param strRqstId identifier of the original time-series data request
     */
    public void setRequestId(String strRqstId) {
        this.strRqstId = strRqstId;

        // Set the original time-series request identifier for each composite sampled block
        this.setSmplBlocks.forEach(blk -> blk.setRequestId(strRqstId));
        
    }
    
    /**
     * <p>
     * Adds the given <code>SampledBlock</code> to the current aggregation of sampled blocks.
     * </p>
     * <p>
     * The argument is added to this <code>SampledAggregate</code> instance and ordered according to its
     * initial timestamp.  The data sources within the argument are recorded and saved.  
     * The data types of the data sources within the argument block are recorded; if
     * a data source within the argument has a different type that that already within the aggregate an
     * exception is thrown.
     * </p>
     * <p>
     * <code>SampledBlock</code> instances can be added in any order, they are ordered internally according to
     * their sampling start times.  However, this is necessarily an atomic operation as the state of the 
     * <code>SampledAggregate</code> instance is modified.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The methods <code>{@link #timestamps()}</code> and <code>{@link #timeSeries(String)}</code> are available
     * at any time and will return values reflecting the current state of the <code>SampledAggregate</code>
     * instance.  If a <code>SampledAggregate</code> object has been populated with all time-series data
     * recovered from a query request, then the methods return values for the full request.
     * </p>
     * 
     * @param blkNew    new sampled block to be added to aggregate
     * 
     * @return  <code>true</code> if argument was successfully added to aggregate, <code>false</code> otherwise
     * 
     * @throws IllegalArgumentException data source within argument has data type inconsistent with that already in aggregate
     */
    synchronized
    public boolean add(SampledBlock blkNew) throws IllegalArgumentException {
        
        // Extract the data types of the data sources - check for consistency
        this.extractSourceTypes(blkNew);    // throws exception
        
        // Set the original time-series request identifier
        if (this.strRqstId != null)
            blkNew.setRequestId(this.strRqstId);
        
        // Insert the sampled block at the proper index according to the start time
        this.setSmplBlocks.add(blkNew);
        
        // Increment the sampled count
        this.cntSamples += blkNew.getSampleCount();
        
        // Add the data sources names of the new block
        this.setSrcNms.addAll(blkNew.getSourceNames());
        
        // Update aggregate time range
        this.updateTimeRange(blkNew);
        
        return true;
    }

    /**
     * <p>
     * Creates and returns a new, ordered list of timestamps for all time-series data within this aggregate process.
     * </p>
     * <p>
     * The timestamps returned are for the <em>entire</em> aggregated sampling process.  It is a composite of all
     * sampling blocks within the process. 
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
     * <li>
     * The method is available at any time and will return values reflecting the current state of the 
     * <code>SampledAggregate</code> instance.  If a <code>SampledAggregate</code> object has been populated with 
     * all time-series data recovered from a query request, then the methods return values for the full request.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  ordered list of timestamp instants for all time-series data within this sampling process
     */
    public ArrayList<Instant>    timestamps() {
        
        ArrayList<Instant>   lstTms = this.setSmplBlocks
                .stream()
                .sequential()
                .collect(ArrayList::new, (lst, blk) -> lst.addAll(blk.getTimestamps()), (agg, lst) -> agg.addAll(lst));
        
        return lstTms;
    }
    
    /**
     * <p>
     * Returns the time range over which all samples were taken.
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
     * where <i>t</i><sub>0</sub> is the timestamp of the first samples within the aggregate process 
     * and <i>t</i><sub><i>N</i>-1</sub> is the timestamp the last sample.  
     * Note that <i>N</i> is the number of samples (equivalently, timestamps) returned by 
     * <code>{@link #getSampleCount()}</code>.
     * <p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The returned interval DOES NOT include the sampling period duration
     * after the final timestamp.
     * </li>
     * <li>
     * The returned <code>TimeInterval</code> instance is created on demand from the attributes
     * <code>{@link #insTmsFirst}</code> and <code>{@link #insTmsLast}</code>.
     * </li>
     * </ul>
     * </p>
     *  
     * @return  the smallest connected time interval containing all the aggregated sample timestamps
     */
    public final TimeInterval   timeRange() {
        return TimeInterval.from(this.insTmsFirst, this.insTmsLast);
    }


    /**
     * <p>
     * Returns the component <code>{@link SampledBlock}</code> at given index.
     * </p>
     * <p>
     * Internally, <code>SampledProcess</code> objects are composed of an ordered vector
     * of <code>SampledBlock</code> instances. Each sampled block instance contains
     * the time-series data for processes for a given duration and shared set of timestamps.
     * This method returns the sampling block instance at the given index.
     * </p>
     * 
     * @param index index of desired sampled block within composite sampling process
     * 
     * @return  component sampling block (within sampling process) at given index
     * 
     * @throws IndexOutOfBoundsException    the index is larger the the number of component blocks
     */
    public final SampledBlock   sampledBlock(int index) throws IndexOutOfBoundsException {
    
        // Check size
        if (index >= this.setSmplBlocks.size()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                        + " - Index " + index + " is greater than the number of component blocks " 
                        + this.setSmplBlocks.size() + ".";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IndexOutOfBoundsException(strMsg);
        }
        
        // Get the sample block at the desired index
        SampledBlock    blkIndex = this.setSmplBlocks
                .stream()
                .sequential()
                .skip(index)        // index is zero-based, so skip count discards all blocks up to index
                .findFirst()
                .get();
        
        return blkIndex;
    }

    /**
     * <p>
     * Creates and returns a new <code>{@link SampledTimeSeries}</code> instance containing all 
     * time-series data from the given data source.
     * </p>
     * <p>
     * The time-series for the given data source is returned for the <em>entire</em> sampling
     * process across all sampled blocks.  That is the returned value is a composite of all time-series data within 
     * component sampling blocks.  If a sampled block does not contain data for the given data source
     * a null time series is created for that block 
     * (see <code>{@link SampledTimeSeries#nullSeries(String, DpSupportedType, int)}</code>). 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
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
     * <li>
     * The method is available at any time and will return values reflecting the current state of the 
     * <code>SampledAggregate</code> instance.  If a <code>SampledAggregate</code> object has been populated with 
     * all time-series data recovered from a query request, then the methods return values for the full request.
     * </li>
     * </ul>
     * </p>
     * 
     * @param strSrcName     data source unique name
     * @return  new instance of <code>SampledTimeSeries</code> containing full time-series for given source
     * 
     * @throws NoSuchElementException the data source is not represented in this process
     */
    public SampledTimeSeries<Object>    timeSeries(String strSrcName) throws NoSuchElementException {
        
        // Check source name
        if (!this.hasDataSource(strSrcName)) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                        + "- Sampled process contain no data source with name " + strSrcName;
            
            if (BOL_LOGGING) 
                LOGGER.warn(strMsg);
            
            throw new NoSuchElementException();
        }
        
        // Create the full time-series data-value vector for the given data source
        DpSupportedType         enmSrcType = this.getSourceType(strSrcName);
        ArrayList<Object>       vecSrcValues = new ArrayList<>(this.getSampleCount());
        
        for (SampledBlock blk : this.setSmplBlocks) {
            
            // Check if data source is represented within current sampling block
            SampledTimeSeries<Object>   tmSeries;
            if (blk.hasSourceData(strSrcName)) {
                tmSeries = blk.getTimeSeries(strSrcName);
                
            } else {
                tmSeries = SampledTimeSeries.nullSeries(strSrcName, enmSrcType, blk.getSampleCount());
                
            }

            // Add time-series data values to vector of full time series
            vecSrcValues.addAll(tmSeries.getValues());
        }
        
        // Create the full time-series instance for the given data source and return it
        SampledTimeSeries<Object>   stmsNew = new SampledTimeSeries<Object>(strSrcName, enmSrcType, vecSrcValues);
        
        return stmsNew;
    }
    
    
    //
    // Attribute/Property Query
    //
    
    /**
     * <p>
     * Returns the request ID of the original time-series data request if available.
     * </p>
     * <p>
     * The request ID is an optional parameter within the Java API Library used to associated recovered
     * data to the original data request.  See <code>{@link IDataTable#getRequestId()}</code> for more
     * information.  
     * </p>
     * <p>
     * This attribute can be set with method <code>{@link #setRequestId(String)}</code> or during creation.
     * </p>
     * 
     * @return  the (optional) identifier associated with the original time-series data request,
     *          or <code>null</code> if unassigned 
     */
    public String   getRequestId() {
        return this.strRqstId;
    }
    
    /**
     * <p>
     * Returns the approximate memory allocation of the raw data used to create this sampled aggregate in its current state.
     * </p>
     * <p>
     * The returned value is an approximation of the allocation for all the Protocol Buffers 
     * data messages associated within all raw correlated data blocks used to create this sampled aggregate.
     * It is the summation returned by all <code>{@link SampledBlock#getRawAllocation()}</code>.
     * However, this value is computed upon demand, but all internal <code>SampledBlock</code> instances
     * pre-compute the value upon creation.
     * The returned value contains both the time-series data and the allocation required for the timestamps.
     * Not that the allocation required for timestamps described by a sampling clock is typically trivial
     * as compared to the associated time-series data.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The returned value is the allocation required for the original raw correlated data block used in
     * <code>SampledBlock</code>creation.  The allocation required for a <code>SampledBlock</code> can be 
     * different after conversion to Java objects (typically larger).  However, it does provide a minimum 
     * estimate for the heap size required by this sampled aggregation.
     * </li>
     * <li>
     * Compare this method with operation <code>{@link SampledAggregate#allocationSize()}</code> also available.
     * </li>
     * <li>
     * The returned value should be accurate and can be used for critical operations.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  the serialized memory allocation for all <code>DataColumn</code> messages within the current collection
     * 
     * @see #allocationSize()
     */
    public long getRawAllocation() {
        long    lngAlloc = this.setSmplBlocks.stream().mapToLong(blk -> blk.getRawAllocation()).sum();
        
        return lngAlloc;
    }
    
    /**
     * <p>
     * Computes and returns an estimate for the total memory allocation used for this sampled aggregate in its current state.
     * </p>
     * <p>
     * The method iterates through all composite sample blocks invoking <code>{@link IDataTable#allocationSize()}</code>
     * then sums the result.  The result is an estimate of the Java heap space (in bytes) required to store this collection
     * of <code>SampledBlock</code> components.  Note that some Java value are converted on demand (i.e., time-series data)
     * and thus the returned value can change after data is accessed.
     * Typically this is a conservative (over)estimate favoring 64-bit architecture.  
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * <ul>
     * <li>This can be an expensive operation and is computed on demand.</li>
     * <li>This method is available as a guidance and should not be used for critical operations.</li>
     * </ul>
     *   
     * @return  an estimate of the Java heap space required by this sampled aggregate
     * 
     * @see #getRawAllocation()
     */
    public long computeAllocationSize() {
        long    lngAlloc = this.setSmplBlocks.stream().mapToLong(blk -> blk.allocationSize()).sum();
        
        return lngAlloc;
    }
    
    /**
     * <p>
     * Determine whether or not the sampled process contains time-series data for the given 
     * data source.
     * </p>
     * <p>
     * Simply checks the given data source name against the internal set of represented
     * data sources.
     * </p>
     *  
     * @param strSrcName unique name of data source
     * 
     * @return  <code>true</code> if the data source with given name is represented within this sampled process,
     *          <code>false</code> otherwise
     */
    public boolean  hasDataSource(String strSrcName) {
        return this.setSrcNms.contains(strSrcName);
    }
    
    /**
     * <p>
     * Returns the number of component <code>{@link SampledBlock}</code> instances forming this process.
     * </p>
     * <p>
     * Internally, <code>SampledProcess</code> objects are composed of an ordered vector
     * of <code>SampledBlock</code> instances. Each sampling block instance contains
     * the time-series data processes for a given duration and shared set of timestamps.
     * This method returns the total number sampled block instances forming the composite
     * sampled process.
     * </p>
     * 
     * @return  number of component sampled blocks within composite sampling process
     */
    public final int    getSampledBlockCount() {
        return this.setSmplBlocks.size();
    }


    /**
     * <p>
     * Returns all component <code>{@link SampledBlock}</code> instances in order of start times.
     * </p>
     * <p>
     * Internally, <code>SampledAggregate</code> objects are composed of an ordered collection
     * of <code>SampledBlock</code> instances. Each sampling block instance contains
     * the time-series data processes for a given duration determined by its shared
     * timestamps.
     * This method returns all sampled block instances in order of start time instants.
     * </p>
     * 
     * @return the ordered collection of component sampled blocks for this aggregate process
     */
    public final SortedSet<SampledBlock>    getSamplingBlocks() {
        return this.setSmplBlocks;
    }

    /**
     * <p>
     * Returns the total number of samples within each time series of the sampled process.
     * </p>
     * <p>
     * The returned value is equivalent to the total number of timestamps for the entire aggregate
     * process.
     * </p>
     * <p>
     * Note that each time series within the sampled process must have the same size.  
     * This condition is enforced by assigning <code>null</code> values to data sources
     * missing from any sampled block.
     * </p>
     * 
     * @return  the size of each time series within the entire aggregated process
     */
    public final int getSampleCount() {
        return this.cntSamples;
    }
    
    /**
     * <p>
     * Returns the number of unique data sources within the sampled process.
     * </p>
     * <p>
     * The sampled process must containing one and only one time series from each data source
     * represented.  This condition was checked during construction.
     * To obtain the collection of unique data source names use
     * <code>{@link #getDataSourceNames()}</code>.
     * </p>
     * 
     * @return  the number of data sources contributing to this sampled process
     */
    public final int getDataSourceCount() {
        return this.setSrcNms.size();
    }
    
    /**
     * <p>
     * Returns the set of all data source names for data sources contributing to the sampled process.
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
     * @param strSrcName data source unique name
     * 
     * @return  the data type of the given data source time series, or <code>null</code> if source not present 
     *
     * @throws  NoSuchElementException  data source name is not represented in the time series data collection
     * 
     * @see #getDataSourceNames()
     * @see DpSupportedType
     */
    public final DpSupportedType    getSourceType(String strSrcName) throws NoSuchElementException {
        
        // Check argument
        if (!this.hasDataSource(strSrcName))
            throw new NoSuchElementException("Data source not represented in time-series collection: " + strSrcName);
        
        return this.mapSrcNmToType.get(strSrcName);
    }
    
    
    //
    // Data Verification
    //
    
    /**
     * <p>
     * Verifies the correct ordering of the sampling start times within each <code>SampledBlock</code> within the aggregate set.
     * </p>
     * <p>
     * Extracts all starting timestamps of each <code>SampledBlock</code> in order to create the set
     * { <i>t</i><sub>0</sub>, <i>t</i><sub>1</sub>, ..., <i>t</i><sub><i>N</i>-1</sub> } where
     * <i>t<sub>n</sub></i> is the start time for <code>SampledBlock</code> instance <i>n</i>
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
     * <li>The <code>ResultStatus</code> contains a message describing any failure.</li>
     * </ul>
     * </p>
     * 
     * @return  <code>ResultStatus</code> containing result of test, with message if failure
     */
    public ResultStatus verifyStartTimeOrdering() {
        
        // Loop through set checking that all start times are in order
        int             indCurr = 0;
        SampledBlock    blkCurr = null;
        for (SampledBlock blkNext : this.setSmplBlocks) {
            
            // Initialize the loop - first time through
            if (blkCurr == null) {
                blkCurr = blkNext;
                continue;
            }
            
            // Compare the two instants
            Instant     insCurr = blkCurr.getStartTime();
            Instant     insNext = blkNext.getStartTime();
            
            if (insCurr.compareTo(insNext) >= 0) {
                String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                        + " - Bad start time ordering at query data block " + indCurr + " with start time = "
                        + insCurr + " compared to next block start time = " + insNext;
                
                if (BOL_LOGGING)
                    LOGGER.error(strMsg);
                
                return ResultStatus.newFailure(strMsg);
            }
            
            // Update state to next instant
            indCurr++;
            insCurr = insNext;
        }
        
        return ResultStatus.SUCCESS;
    }

    /**
     * <p>
     * Verifies that sampled time domains of each <code>SampledBlock</code> within the aggregate are disjoint.
     * </p>
     * <p>
     * Extracts the set of all sampling time range intervals within the aggregate block set
     * { <i>I</i><sub>0</sub>, <i>I</i><sub>0</sub>, ..., <i>I</i><sub><i>N</i>-1</sub> } where
     * <i>I<sub>n</sub></i> is the sampling time domain for <code>SampledBlock</code> 
     * instance <i>n</i> and <i>N</i> is the size of the aggregate (i.e., number of sampled blocks).  
     * We assume closed intervals of the form 
     * <i>I</i><sub><i>n</i></sub> = [<i>t</i><sub><i>n</i>,start</sub>, <i>t</i><sub><i>n</i>,end</sub>]
     * where <i>t</i><sub><i>n</i>,start</sub> is the start time for <code>SampledBlock</code> instance <i>n</i>
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
     * <li>The <code>ResultStatus</code> contains a message describing any failure.</li>
     * </ul>
     * </p>
     * 
     * @return  <code>ResultStatus</code> containing result of test, with message if failure
     */
    public ResultStatus verifyDisjointTimeDomains() {
        
        // Check that remaining time domains are disjoint - this works if the argument is ordered correctly
        // Loop through set checking that all start times are in order
        int             indCurr = 0;
        SampledBlock    datCurr = null;
        for (SampledBlock datNext : this.setSmplBlocks) {
            
            // Initialize the loop - first time through
            if (datCurr == null) {
                datCurr = datNext;
                continue;
            }
            
            // Extract time domains and check for closed intersection
            TimeInterval    tvlCurr = datCurr.getTimeRange();
            TimeInterval    tvlNext = datNext.getTimeRange();
            
            if (tvlCurr.hasIntersectionClosed(tvlNext)) {
                String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                        + "Time range collision at correlated data block "
                        + Integer.toString(indCurr)
                        + ", interval " + tvlCurr
                        + " collides with next " + tvlNext + ".";
                
                if (BOL_LOGGING)
                    LOGGER.error(strMsg);
                
                return ResultStatus.newFailure(strMsg);
            }
            
            indCurr++;
            tvlCurr = tvlNext;
        }
        
        return ResultStatus.SUCCESS;
    }
    
    /**
     * <p>
     * Verifies that all data sources within the aggregate set produce data of the same type.
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
     * of <code>SampledBlock</code> within the aggregate collection.
     * Each pair is then checked against the data types within (previously constructed) map 
     * <code>{@link #mapSrcNmToType}</code>.  Any data sources that have a different
     * type are recorded and contained within the returned <code>ResultStatus</code> object.
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
     * <li>The <code>ResultStatus</code> contains a message describing any failure.</li>
     * </ul>
     * </p>
     * 
     * @return  <code>ResultStatus</code> containing result of test, with message if failure
     */
    public  ResultStatus verifySourceTypes() {
        
        // First check for UNSUPPORTED_TYPE in the (pv name, pv type) map
        List<String> lstBadPvs = this.mapSrcNmToType
                .entrySet()
                .stream()
                .filter(entry -> (entry.getValue() == DpSupportedType.UNSUPPORTED_TYPE))
                .<String>map(Map.Entry::getKey)
                .toList();
        
        if (!lstBadPvs.isEmpty())
            return ResultStatus.newFailure("Sampling process contains PVs with inconsistent types: " + lstBadPvs);
        
        // Local type used for intermediate results
        record Pair(String name, DpSupportedType type) {};
        
        // Get list of sources with incorrect data types
        List<Pair> lstBadSrcs;
        
        // Pivot concurrency on the size of container (always use concurrency within sampling blocks)
        if (BOL_CONCURRENCY && (this.setSmplBlocks.size() > SZ_CONCURRENCY_PIVOT) ) {
            
            // Process vector concurrently, blocks concurrently 
            lstBadSrcs = this.setSmplBlocks
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
            lstBadSrcs = this.setSmplBlocks
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
            lstBadSrcs = this.setSmplBlocks
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
        if (!lstBadSrcs.isEmpty()) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Sampled blocks contained mixed-type data sources: " + lstBadSrcs;
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            return ResultStatus.newFailure(strMsg);
        }
        
        return ResultStatus.SUCCESS;
    }
    
    
    // 
    // IDataTable<Object> Creation
    //
    
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
     * Use this method when index operations require performance and tables are relatively small.
     * </li>
     * <li>
     * The current implementation returns an <code>IDataTable</code> interface backed by class
     * <code>{@link StaticDataTable}</code>
     * </li>
     * </ul>
     * </p>  
     * 
     * @return  a static data table instance exposing <code>{@link IDataTable}</code> backed by this process data
     * 
     * @see StaticDataTable
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
        
        StaticDataTable     tblStat = new StaticDataTable(lstTms, lstCols);
        
        if (this.strRqstId != null)
            tblStat.setRequestId(this.strRqstId);
        
        return tblStat;
    }
    
    /**
     * <p>
     * Creates and returns a dynamic data table backed by all data within this process.
     * </p>
     * <p>
     * Creates a <code>IDataTable</code> implementation from this sampling process.
     * The returned implementation then provides all time-series data using dynamics lookups  
     * backed by this sampling process data.
     * Each time-series represented within the sampling blocks has dynamic row indexing for data retrieval.
     * Sampling blocks without time-series data for a data source will have all their values returned as
     * <code>null</code> when retrieved by the indexing methods of <code>IDataTable</code>.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * Dynamic indexing can be unnecessary for small sampling processes.  Consider using a static data table 
     * for these situations, available from <code>{@link #createStaticDataTable()}</code>.
     * </p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Use this method when sampling processes are large and sparse.
     * </li>
     * <li>
     * The current implementation returns an <code>IDataTable</code> interface backed by class
     * <code>{@link SamplingProcessTable}</code>
     * </li>
     * </ul>
     * </p>  
     * 
     * @return  a static data table instance exposing <code>{@link IDataTable}</code> backed by this process data
     * 
     * @see SamplingProcessTable
     */
    public IDataTable   createDynamicDataTable() {
        IDataTable  table = SampledAggregateTable.from(this);
        
        return table;
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Updates the map of data source names to data source type for all sources in the argument.
     * </p>
     * <p>
     * Inspected all data source names within the sampled blocks and updates the map
     * <code>{@link #mapSrcNmToType}</code> of (unique) sources names keyed to their data type.
     * If the map already contains an entry for a data source name the type is checked against
     * the current entry; an exception is thrown if they are different.  If the map does not
     * contain a type entry for a data source it is supplied as a new entry. 
     * </p>
     * <p>
     * The map <code>{@link #mapSrcNmToType}</code> should contain all entries for all data sources 
     * represented in the aggregate.  Thus, at all times (i.e., at all states during the building of the aggregate)
     * the key set of the returned map should be equal to the set of data source names <code>{@link #setSrcNms}</code>.
     * Thus, the map <code>{@link #mapSrcNmToType} can be used both to verify the set of data sources
     * within the <code>SampledAggregate</code>, and to verify that all sources have uniform data type. 
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * <ul>
     * <li>
     * A value <code>{@link DpSupportedType#UNSUPPORTED_TYPE}</code> will be entered into the map
     * for any data source producing time series data with inconsistent types.  This condition
     * is indicative of a corrupt archive and should be checked (i.e., <code>{@link #verifySourceTypes()}</code>).
     * </li>
     * <li>
     * <s>
     * In the current implementation an exception is thrown if an inconsistent data type is encountered.
     * </s>
     * </li>
     * </ul>
     * </p> 
     *  
     * @param blkNew    new sampled block to be added to aggregate
     * 
     * @throws IllegalArgumentException <s>data source within argument has data type inconsistent with that already in aggregate</s>
     */
    private void extractSourceTypes(SampledBlock blkNew) throws IllegalArgumentException {
        
        // Extract the data source names within the new block
        List<String>    lstPvNames = blkNew.getSourceNames();
        
        // Extract the source data types while checking
        for (String strPvNm : lstPvNames) {
            DpSupportedType     enmType = blkNew.getSourceType(strPvNm);
            
            // Data source is new to aggregate
            if (!this.mapSrcNmToType.containsKey(strPvNm)) {
                this.mapSrcNmToType.put(strPvNm, enmType);
                
                continue;
            }
            
            // Map entry already exists - check it
            DpSupportedType enmTypeCmp = this.mapSrcNmToType.get(strPvNm);
            if (enmType != enmTypeCmp) {
                String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                            + " - conflicting data types for data source " + strPvNm
                            + ": " + enmType + " established vs. " + enmTypeCmp + " new.";
                
                if (BOL_LOGGING)
                    LOGGER.warn(strMsg);
                
//                throw new IllegalArgumentException(strMsg);
                this.mapSrcNmToType.put(strMsg, DpSupportedType.UNSUPPORTED_TYPE);
            }
        }
    }
    
    /**
     * <p>
     * Compares the time range of the given argument to the current aggregate time range and adjusts if necessary.
     * </p>
     * <p>
     * The attributes <code>{@link #insTmsFirst}</code> and <code>{@link #insTmsLast}</code> are compared against
     * the time range of the given <code>SampledBlock</code> instance.  The attributes are updated as necessary.
     * </p>
     * 
     * @param blkNew    sampled block to be added to aggregate
     */
    private void updateTimeRange(SampledBlock blkNew) {
        
        Instant insFirst = blkNew.getStartTime();
        Instant insLast = blkNew.getFinalTime();
        
        // Compare and set the initial aggregate timestamp
        if (this.insTmsFirst == Instant.EPOCH)  // First time through?
            this.insTmsFirst = insFirst;
        else 
            if (insFirst.compareTo(this.insTmsFirst) == -1)
                this.insTmsFirst = insFirst;
        
        // Compare and set the final aggregate timestamp
        if (this.insTmsLast == Instant.EPOCH)   // First time through?
            this.insTmsLast = insLast;
        else
            if (insLast.compareTo(this.insTmsLast) == +1)
                this.insTmsLast = insLast;
    }

}
