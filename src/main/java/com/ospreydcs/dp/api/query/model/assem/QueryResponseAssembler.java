/*
 * Project: dp-api-common
 * File:	QueryResponseAssembler.java
 * Package: com.ospreydcs.dp.api.query.model.assem
 * Type: 	QueryResponseAssembler
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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.SortedSet;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.ranges.RangeException;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.model.coalesce.SampledBlock;
import com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData;
import com.ospreydcs.dp.api.query.model.superdom.RawSuperDomData;
import com.ospreydcs.dp.api.query.model.superdom.SampledBlockSuperDom;
import com.ospreydcs.dp.api.query.model.superdom.TimeDomainProcessor;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;

/**
 * <p>
 * Assembles raw, correlated data from a time-series data request into an aggregation of coalesced sampled
 * blocks with disjoint time domains.
 * </p>
 * <p>
 * Class accepts a sorted set of <code>RawCorrelatedData</code> objects and creates a <code>SampledAggregate</code>
 * object.  Any time-domain collisions within the raw correlated data are processed according to the configuration 
 * settings. Specifically, an exception is thrown if collisions are not permitted and one is encountered, or
 * the offending <code>RawCorrelatedData</code> instances are coalesced into a super domain
 * <code>SampledBlockSuperDom</code> instance if collisions are permitted. 
 * </p>
 * <p>
 * <h2>WARNINGS:</h2>
 * The current implement includes extensive error checking intended to mitigate external
 * implementation problems during development.   
 * The error checking is performed when creating <code>SampledAggregate</code> instances.  
 * Such error checking could present a <em>performance burden</em> during operation.
 * Developers should consider reducing the error check when mature.  There is a configuration parameter
 * in the Library configuration file that enables/disabled advanced error checking 
 * (see class attribute <code>{@link #BOL_ERROR_CHK}</code>).
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
 * @since Apr 13, 2025
 *
 */
public class QueryResponseAssembler {
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>QueryResponseAssembler</code> instance ready for configuration and processing.
     * </p>
     * <p>
     * The returned instance is ready for the processing of raw, correlated data available from <code>RawDataCorrelator</code>
     * instances.  The <code>SortedSet</code> of <code>RawCorrelatedData</code> instances produced from the correlator
     * is processed into a <code>SampledAggregate</code> instance using the <code>{@link #processWithExceptions(SortedSet)}</code>
     * method.
     * </p>
     * <p>
     * The returned assembler instance can be configured with various processing options using the <code>enable...()</code>
     * and <code>set...()</code> methods which should be invoked before using <code>{@link #processWithExceptions(SortedSet)}</code>.
     * The default configuration is set according to the Java API Library configuration file, whose values are available
     * as class constants.
     * </p>
     * 
     * @return  a new <code>QueryResponseAssembler</code> instance ready for assembly of raw, correlated response data
     */
    public static QueryResponseAssembler create() {
        return new QueryResponseAssembler();
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
    
    /** Enable/disable allowing time domain collisions within correlated data */ 
    public static final boolean     BOL_DOM_COLLISION = CFG_QUERY.data.table.construction.domainCollision;
    
    
    /** Concurrency enabled flag */
    public static final boolean     BOL_CONCURRENCY = CFG_QUERY.data.table.construction.concurrency.enabled;
    
    /** Concurrency tuning parameter - pivot to parallel processing when lstMsgDataCols size hits this limit */
    public static final int         SZ_CONCURRENCY_PIVOT = CFG_QUERY.data.table.construction.concurrency.pivotSize;
    
    /** Concurrency maximum thread count */
    public static final int         CNT_CONCURRENCY_MAX_THRDS = CFG_QUERY.data.table.construction.concurrency.maxThreads;
    
    
    /** General timeout limit */
    public static final long        LNG_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** General timeout limit units */
    public static final TimeUnit    TM_TIMEOUT = CFG_QUERY.timeout.unit;
    

    //
    // Class Resources
    //
    
    /** Event logger for class */
    private static final Logger LOGGER = Log4j.getLogger(QueryResponseAssembler.class, STR_LOGGING_LEVEL);
    

    //
    // Instance Resources
    //
    
    /** The time-domain collision processor */
    private final TimeDomainProcessor       procTmDomain;
    
    
    //
    // Instance Configuration
    //
    
    /** Enable/disable advanced error checking and verification */
    private boolean     bolErrorChk = BOL_ERROR_CHK;
    
    /** Enable/disable time-domain collisions within raw, correlated data */
    private boolean     bolTmDomColl = BOL_DOM_COLLISION;
    
    
    /** Enable/disable concurrent processing during data coalescing */
    private boolean     bolConcurrency = BOL_CONCURRENCY;
    
    /** Maximum number of allowable execution threads for concurrent processing */
    private int         cntMaxThreads = CNT_CONCURRENCY_MAX_THRDS;
    
    /** Concurrency tuning parameter - pivot to parallel processing above this value */
    private int         szConcPivot = SZ_CONCURRENCY_PIVOT;
    
    
    //
    //  Constructors
    //

    /**
     * <p>
     * Constructs a new <code>QueryResponseAssembler</code> instance.
     * </p>
     *
     */
    public QueryResponseAssembler() {
        this.procTmDomain = TimeDomainProcessor.create();
    }
    
    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Resets all configuration parameters to the default values.
     * </p>
     * <p>
     * All configuration parameters are set to the default values at creation and specified by
     * the Java API Library configuration file.  These values are all available individually from the
     * following methods:
     * <ul>
     * <li><code>{@link #hasAdvancedErrorChecking()}</code></li>
     * <li><code>{@link #hasTimeDomainCollisions()}</code></li>
     * <li><code>{@link #hasConcurrency()}</code></li>
     * <li><code>{@link #getMaxThreadCount()}</code></li>
     * <li><code>{@link #getConcurrencyPivotSize()}</code></li>
     * </ul>
     * <p>
     * 
     * @see #BOL_ERROR_CHK
     * @see #BOL_DOM_COLLISION
     * @see #BOL_CONCURRENCY
     * @see #CNT_CONCURRENCY_MAX_THRDS
     * @see #SZ_CONCURRENCY_PIVOT
     */
    public void resetDefaultConfiguration() {
        
        this.bolErrorChk = BOL_ERROR_CHK;
        this.bolTmDomColl = BOL_DOM_COLLISION;
        
        this.bolConcurrency = BOL_CONCURRENCY;
        this.cntMaxThreads = CNT_CONCURRENCY_MAX_THRDS;
        this.szConcPivot = SZ_CONCURRENCY_PIVOT;
    }
    
    /**
     * <p>
     * Enables/disables advanced error checking and verification while coalescing raw, correlated data.
     * </p>
     * <p>
     * Enabling this feature will allow the assembler to do pre-processing and verification on the target
     * data set such as type verification, time-series size verification, etc.  Errors will be caught at
     * the assembly level.  However, processing time for the target set are potentially increased.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The default values is given by class constant <code>{@link #BOL_ERROR_CHK}</code> which is taken from
     * the Java API Library configuration file.
     * </p> 
     * 
     * @param bolErrorChk   <code>true</code> to enable advanced error checking, <code>false</code> to disable
     */
    public void enableAdvancedErrorChecking(boolean bolErrorChk) {
        this.bolErrorChk = bolErrorChk;
    }
    
    /**
     * <p>
     * Enables/disables time-domain collisions within the target set of raw, correlated data.
     * </p>
     * <p>
     * <code>RawCorrelatedData</code> instances contain time-series data correlated to a common set of timestamps.
     * It is possible that multiple instances have different timestamps but occur within the same time range,
     * that is, they have "time-domain collisions."  For exampled, the case of two processes sampled with different
     * sampling clocks (i.e., different periods) during the same time interval. 
     * Enabling time-domain collisions allows request data with such time-domain collisions to be coalesced into
     * special "super domain" sampled blocks.  The blocks contain all timestamps for all data within the super domain.
     * Processes not sampled at a timestamp is given a <code>null</code> value.
     * </p>
     * <p>
     * The creation of super domain sampled blocks is an expensive process.  However, it does allow clients to recover
     * and inspect data that is cross correlated to different sampling clocks.
     * </p>
     * <p>
     * Disabling time-domain collisions expedites processing, but it also rejects requested data sets where they
     * occur.  In such cases exceptions are thrown.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The default values is given by class constant <code>{@link #BOL_DOM_COLLISION}</code> which is taken from
     * the Java API Library configuration file.
     * </p> 
     * 
     * @param bolTmDomColl  <code>true</code> to enable time-domain collisions, <code>false</code> to disable
     */
    public void enableTimeDomainCollisions(boolean bolTmDomColl) {
        this.bolTmDomColl = bolTmDomColl;
    }

    /**
     * <p>
     * Enables/disables concurrent processing of the target set of raw, correlated data.
     * </p>
     * <p>
     * It is possible to process <code>RawCorrelatedData</code> instances on separate execution threads
     * to expedite assembly.  Setting the argument <code>true</code> enables the use of multi-threading
     * and typically increases request-processing performance. 
     * </p>
     * <p>
     * Although typically faster in real-time, the use of multiple processing threads
     * can steal resources from other concurrent processes.  Since this is the penultimate stage in Query Service
     * request processing it is unlikely that competition is of great concern.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The default values is given by class constant <code>{@link #BOL_CONCURRENCY}</code> which is taken from
     * the Java API Library configuration file.
     * </p> 
     * 
     * @param bolConcurrency    <code>true</code> enable multi-threaded processing, <code>false</code> single-thread processing
     */
    public void enableConcurrency(boolean bolConcurrency) {
        this.bolConcurrency = bolConcurrency;
    }
    
    /**
     * <p>
     * Sets the maximum number of allowable execution threads when concurrent processing is enabled.
     * </p>
     * <p>
     * This method is valid when concurrent processing of the target set (i.e., <code>RawCorrelatedData</code> instances)
     * is enabled (e.g., with the call <code>{@link #enableConcurrency(boolean)}</code>.  The argument specifies the
     * maximum number of execution threads that will be used in the processing.  This is a tuning parameter that is
     * typically dependent upon the host platform, specifically, the number of CPU cores.  Increasing this value beyond
     * the number of cores can degrade performance and compete with other currently executing processes.   
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The default values is given by class constant <code>{@link #CNT_CONCURRENCY_MAX_THRDS}</code> which is taken from
     * the Java API Library configuration file.
     * </p> 
     * 
     * @param cntMaxThreads maximum number of allowable execution threads in concurrent processing
     * 
     * @see #enableConcurrency(boolean)
     */
    public void setMaxThreadCount(int cntMaxThreads) {
        this.cntMaxThreads = cntMaxThreads;
    }
    
    /**
     * <p>
     * Sets the size of the target data set triggering concurrent processing when concurrency is enabled.
     * </p>
     * <p>
     * Sets the threshold size of the target set before concurrent processing is invoked.  This is a performance tuning
     * parameter to prevent the allocation of multi-threaded resources for small target sets.  The parameter weighs the
     * cost of creating the multiple threads against the reduced resources required for single-threaded processing.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The default values is given by class constant <code>{@link #SZ_CONCURRENCY_PIVOT}</code> which is taken from
     * the Java API Library configuration file.
     * </p> 
     * 
     * @param szPivot   maximum size of the target set before multi-threaded processing is invoked
     * 
     * @see #enableConcurrency(boolean)
     */
    public void setConcurrencyPivotSize(int szPivot) {
        this.szConcPivot = szPivot;
    }
    
    /**
     * <p>
     * Returns whether or not advanced error checking and verification of target data set is enabled.
     * </p>
     * 
     * @return  <code>true</code> if advanced error checking is enabled, <code>false</code>otherwise
     * 
     * @see #enableAdvancedErrorChecking(boolean)
     */
    public boolean  hasAdvancedErrorChecking() {
        return this.bolErrorChk;
    }
    
    /**
     * <p>
     * Returns whether or not time-domain collisions within raw, correlated data are processed or rejected.
     * </p>
     * 
     * @see #enableTimeDomainCollisions(boolean)
     * 
     * @return  <code>true</code> if time-domain collisions are processed, <code>false</code> if they are rejected
     */
    public boolean  hasTimeDomainCollisions() {
        return this.bolTmDomColl;
    }
    
    /**
     * <p>
     * Determines whether or not concurrent processing of target data set is enabled.
     * </p>
     * 
     * @return  <code>true</code> if multi-threaded processing is enabled
     * 
     * @see #enableConcurrency(boolean)
     */
    public boolean  hasConcurrency() {
        return this.bolConcurrency;
    }
    
    /**
     * <p>
     * Returns the maximum number of allowable execution threads when concurrent processing is enabled.
     * </p>
     * 
     * @return  maximum number of processing threads for target data
     * 
     * @see #enableConcurrency(boolean)
     * @see #setMaxThreadCount(int)
     */
    public int  getMaxThreadCount() {
        return this.cntMaxThreads;
    }
    
    /**
     * <p>
     * Returns the maximum size of the target data set before concurrent processing is invoked when concurrency is enabled.
     * </p>
     * 
     * @return  size of the target set triggering concurrent processing
     * 
     * @see #enableConcurrency(boolean)
     * @see #setConcurrencyPivotSize(int)
     */
    public int  getConcurrencyPivotSize() {
        return this.szConcPivot;
    }
    
    /**
     * <p>
     * Prints out a text description of the current configuration to the given output stream.
     * </p>
     * <p>
     * A line-by-line text description of each configuration field is written to the given output.
     * The <code>strPad</code> is used to supply an optional whitespace character padding to the
     * left-hand side header for each line description.
     * </p>
     *   
     * @param ps        output stream to receive text description of configuration
     * @param strPad    white-space padding for each line header (or <code>null</code>)
     */
    public void printOutConfig(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        
        // Print out configuration parameters
//        ps.println(strPad + this.getClass().getSimpleName() + " Configuration");
        ps.println(strPad + "Error checking enabled         : " + this.bolErrorChk);
        ps.println(strPad + "Time-domain collisions enabled : " + this.bolTmDomColl);
        ps.println(strPad + "Concurrency enabled            : " + this.bolConcurrency);
        ps.println(strPad + "Maximum thread count           : " + this.cntMaxThreads);
        ps.println(strPad + "Concurrency pivot size         : " + this.szConcPivot);
    }
 
    
    
    
    //
    // Operations
    //

    /**
     * <p>
     * Constructs a new instance of <code>SampledAggregate</code> populated from argument data.
     * </p>
     * <p>
     * This is a convenience method that defers to <code>{@link #process(String, SortedSet)}</code> for
     * all processing.  The <code>String</code> argument of that method is set to <code>null</code>.
     * Note that method <code>{@link #process(String, SortedSet)}</code> defers in turn to 
     * <code>{@link #processWithExceptions(SortedSet)}</code> which throws a variety of exceptions.
     * Any exception thrown there is caught and packaged as a <code>{@link DpQueryException}</code>
     * to simplify <code>IQueryService</code> implementations.  The offending exception is available as
     * the cause attribute within the <code>DpQueryException</code> 
     * (i.e., see <code>{@link DpQueryException#getCause()}</code>).
     * </p>
     * <p>
     * After construction the new instance is fully populated with coalesced, sampled time-series data from
     * all data sources contained in the argument.  The returned instance is also configured according
     * to the order and the correlations within the argument.
     * The argument data must be consistent for proper time-series data construction or an exception is thrown.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The argument collection is assumed to be sorted in the order of clock starting instants</li>
     * <li>Advanced data consistency check is performed during construction unless disabled.</li>
     * <li>Any detected data inconsistencies result in an exception. </li>
     * <li>Exception type determines the nature of any data inconsistency. </li>
     * </ul>  
     * </p>
     * 
     * @param setRawData    sorted set of <code>RawCorrelatedData</code> instances used to build aggregate
     *
     * @return  ordered, aggregated of coalesced sampled blocks containing the time-series data within the argument
     *  
     * @throws DpQueryException general processing exception, see cause
     */
    public SampledAggregate process(SortedSet<RawCorrelatedData> setRawData) throws DpQueryException {
        return this.process(null, setRawData);
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>SampledAggregate</code> populated from argument data.
     * </p>
     * <p>
     * This is a convenience method that defers to <code>{@link #processWithExceptions(SortedSet)}</code> for
     * all processing.  Any exception thrown there is caught and packaged as a <code>{@link DpQueryException}</code>
     * to simplify <code>IQueryService</code> implementations.  The offending exception is available as
     * the cause attribute within the <code>DpQueryException</code> 
     * (i.e., see <code>{@link DpQueryException#getCause()}</code>).
     * </p>
     * <p>
     * The <code>String</code> argument is the request identifier of the original time-series data request
     * and is set within the returned value.  Thus, an additional convenience.
     * <p>
     * After construction the new instance is fully populated with coalesced, sampled time-series data from
     * all data sources contained in the argument.  The returned instance is also configured according
     * to the order and the correlations within the argument.
     * The argument data must be consistent for proper time-series data construction or an exception is thrown.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The argument collection is assumed to be sorted in the order of clock starting instants</li>
     * <li>Advanced data consistency check is performed during construction unless disabled.</li>
     * <li>Any detected data inconsistencies result in an exception. </li>
     * <li>Exception type determines the nature of any data inconsistency. </li>
     * </ul>  
     * </p>
     * 
     * @param strRqstId     request identifier of the original time-series data request
     * @param setRawData    sorted set of <code>RawCorrelatedData</code> instances used to build aggregate
     *
     * @return  ordered, aggregated of coalesced sampled blocks containing the time-series data within the argument
     *  
     * @throws DpQueryException general processing exception, see cause
     */
    public SampledAggregate process(String strRqstId, SortedSet<RawCorrelatedData> setRawData) throws DpQueryException {
        
        try {
            SampledAggregate    aggBlks = this.processWithExceptions(setRawData);
            aggBlks.setRequestId(strRqstId);
            
            return aggBlks;
            
            // Catch any processing exception and package it within a DpQueryException 
        } catch (IllegalArgumentException | MissingResourceException | IllegalStateException | TypeNotPresentException
                | RejectedExecutionException | ExecutionException | InterruptedException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                            + " - Exception " + e.getClass() + " during correlated data coalescing and assembly: "
                            + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpQueryException(strMsg, e);
        }
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>SampledAggregate</code> populated from argument data.
     * </p>
     * <p>
     * After construction the new instance is fully populated with coalesced, sampled time-series data from
     * all data sources contained in the argument.  The returned instance is also configured according
     * to the order and the correlations within the argument.
     * The argument data must be consistent for proper time-series data construction or an exception is thrown.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The argument collection is assumed to be sorted in the order of clock starting instants</li>
     * <li>Advanced data consistency check is performed during construction unless disabled.</li>
     * <li>Any detected data inconsistencies result in an exception. </li>
     * <li>Exception type determines the nature of any data inconsistency. </li>
     * </ul>  
     * </p>
     *
     * @param setRawData    sorted set of <code>RawCorrelatedData</code> instances used to build aggregate
     *
     * @return  ordered, aggregated of coalesced sampled blocks containing the time-series data within the argument
     *  
     * @throws RangeException           the argument has bad ordering or contains time domain collisions when none are allowed (see message)
     * @throws UnsupportedOperationException    the raw data contained neither a sampling clock or a timestamp list
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws TypeNotPresentException  an unsupported data type was detected within the argument
     * @throws CompletionException      the sampling process was corrupt after creation (see message)
     * @throws ExecutionException       general exception for serial processing for time-domain collision (see cause)
     * @throws RejectedExecutionException a fill task failed execution for concurrent processing in time-domain collisions
     * @throws InterruptedException     timeout occurred while waiting for concurrent fill tasks to complete in time-domain collisions
     */
    public SampledAggregate processWithExceptions(SortedSet<RawCorrelatedData> setRawData) 
            throws RangeException, IllegalArgumentException, MissingResourceException, IllegalStateException, TypeNotPresentException, 
                   RejectedExecutionException, ExecutionException, InterruptedException 
    {

        // Explicit time-domain collision rejections
        if (!this.bolTmDomColl) {
            ResultStatus    recResult;

            // Start time ordering must be correct for time-domain collision detection
            recResult = TimeDomainProcessor.verifyStartTimeOrdering(setRawData);
            if (recResult.isFailure())
                throw new RangeException(RangeException.BAD_BOUNDARYPOINTS_ERR, recResult.message());
            
            // Check for time-domain collisions within argument if time-domain collisions are disabled
            recResult = TimeDomainProcessor.verfifyDisjointTimeDomains(setRawData);
            if (recResult.isFailure())
                throw new RangeException(RangeException.BAD_BOUNDARYPOINTS_ERR, recResult.message());
        }
            
        // Create the empty returned aggregate and list of composite sampled blocks
        SampledAggregate    aggBlks = SampledAggregate.from();
        List<SampledBlock>  lstBlks = new LinkedList<>();
        
        // Process any time-domain collisions
        boolean bolSuperDoms = procTmDomain.process(setRawData);    // throws IllegalStateException, IndexOutOfBoundsException
        
        // If time-domain collisions were found process the raw super domains
        if (bolSuperDoms) {
            if (BOL_LOGGING)
                LOGGER.info(JavaRuntime.getQualifiedMethodNameSimple() + " - Time domain collisions detected in response data, creating super domains.");
            
            List<RawSuperDomData>   lstRawSuperDoms = procTmDomain.getSuperDomainData();        // throws IllegalStateExceptoin
            List<SampledBlock>      lstSupDomBlks = this.buildSampledBlocks(lstRawSuperDoms);   // throws IllegalArgumentException, MissingResourceException
                                                                                                // IllegalStateException, TypeNotPresentException, RejectedExecutionException
                                                                                                // ExecutionException, InterruptedException
            lstBlks.addAll(lstSupDomBlks);
        }
        
        // Get the disjoint raw data from the time-domain processor and convert them into sampled blocks
        List<RawCorrelatedData> lstRawDisjData = procTmDomain.getDisjointRawData();         // throws IllegalStateException
        List<SampledBlock>      lstSmplBlks = this.buildSampledBlocks(lstRawDisjData);      // throws UnsupportedOperationException, MissingResourceException, 
                                                                                            // IllegalArgumentException, IllegalStateException, TypeNotPresentException
        
        lstBlks.addAll(lstSmplBlks);


        // Build the sampled block aggregate
        //  All sampled blocks (whether from disjoint data or super-donains) are in the list lstBlks 
        if (this.bolConcurrency)
            lstBlks.parallelStream().forEach(blk -> aggBlks.add(blk));  // throws IllegalArgumentException
        else
            lstBlks.forEach(blk -> aggBlks.add(blk));   // throws IllegalArgumentException

        // Do data verification if requested
        if (this.bolErrorChk) {
            ResultStatus recResult = this.verifySampledAggregate(aggBlks);

            if (recResult.isFailure()) {
                if (BOL_LOGGING)
                    LOGGER.error(recResult.message());

                throw new CompletionException(recResult.message(), recResult.cause());
            }
        }

        // Return the new sampled aggregate
        return aggBlks;
    }


    //
    // Support Methods
    //
    
//    /**
//     * <p>
//     * Verifies the correct ordering of the sampling start times within the argument set.
//     * </p>
//     * <p>
//     * Extracts all starting times of the sampling clock in order to create the set
//     * { <i>t</i><sub>0</sub>, <i>t</i><sub>1</sub>, ..., <i>t</i><sub><i>N</i>-1</sub> } where
//     * <i>t<sub>n</sub></i> is the start time for <code>RawCorrelatedData</code> instance <i>n</i>
//     * and <i>N</i> is the size of the argument.   
//     * The ordered collection of start times is compared sequentially to check the following 
//     * conditions:
//     * <pre>
//     *   <i>t</i><sub>0</sub> < <i>t</i><sub>1</sub> < ... <  <i>t</i><sub><i>N</i>-1</sub>
//     * </pre>
//     * That is, we verify that the set 
//     * { <i>t</i><sub>0</sub>, <i>t</i><sub>1</sub>, ..., <i>t</i><sub><i>N</i>-1</sub> }
//     * forms a proper net.
//     * </p>
//     * <p>
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li>This test MUST be run before <code>{@link #verifyDisjointTimeDomains(SortedSet)}</code>.</li>
//     * <li>Failure messages are written to the class logger if logging is enabled.</li>
//     * <li>The <code>ResultStatus</code> contains a message describing any failure.</li>
//     * </ul>
//     * </p>
//     * 
//     * @param setRawData  the target set of processed <code>CorrelatedQueryDataOld</code> objects 
//     * 
//     * @return  <code>ResultStatus</code> containing result of test, with message if failure
//     */
//    private ResultStatus verifyStartTimeOrdering(SortedSet<RawCorrelatedData> setRawData) {
//        
//        // Loop through set checking that all start times are in order
//        int                 indCurr = 0;
//        RawCorrelatedData   datCurr = null;
//        for (RawCorrelatedData datNext : setRawData) {
//            
//            // Initialize the loop - first time through
//            if (datCurr == null) {
//                datCurr = datNext;
//                indCurr++;
//                continue;
//            }
//            
//            // Compare the two instants
//            Instant     insCurr = datCurr.getStartTime();
//            Instant     insNext = datNext.getStartTime();
//            
//            if (insCurr.compareTo(insNext) >= 0) {
//                String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
//                        + " - Bad start time ordering at argument element " + indCurr + " with start time = "
//                        + insCurr + " compared to next element start time " + insNext;
//                
//                if (BOL_LOGGING)
//                    LOGGER.error(strMsg);
//                
//                return ResultStatus.newFailure(strMsg);
//            }
//            
//            // Update state to next instant
//            indCurr++;
//            insCurr = insNext;
//        }
//        
//        return ResultStatus.SUCCESS;
//    }
//
//    /**
//     * <p>
//     * Verifies that sampled domains within the argument are disjoint.
//     * </p>
//     * <p>
//     * Extracts the set of all sampling time range intervals
//     * { <i>I</i><sub>0</sub>, <i>I</i><sub>1</sub>, ..., <i>I</i><sub><i>N</i>-1</sub> } where
//     * <i>I<sub>n</sub></i> is the sampling time domain for <code>RawCorrelatedData</code> 
//     * instance <i>n</i> and <i>N</i> is the size of the argument.  
//     * We assume closed intervals of the form 
//     * <i>I</i><sub><i>n</i></sub> = [<i>t</i><sub><i>n</i>,start</sub>, <i>t</i><sub><i>n</i>,end</sub>]
//     * where <i>t</i><sub><i>n</i>,start</sub> is the start time for <code>RawCorrelatedData</code> instance <i>n</i>
//     * and <i>t</i><sub><i>n</i>,end</sub> is the stop time.
//     * </p>
//     * <p>  
//     * The ordered collection of time domain intervals is compared sequentially to check the 
//     * following conditions:
//     * <pre>
//     *   <i>I</i><sub>0</sub> &cap; <i>I</i><sub>1</sub> = &empty;
//     *   <i>I</i><sub>1</sub> &cap; <i>I</i><sub>2</sub> = &empty;
//     *   ...
//     *   <i>I</i><sub><i>N</i>-2</sub> &cap; <i>I</i><sub><i>N</i>-1</sub> = &empty;
//     *   
//     * </pre>
//     * That is, every <em>adjacent</em> sampling time domain is disjoint.
//     * This algorithm is accurate <em>only if</em> the argument set is correctly ordered
//     * by sampling start times. 
//     * </p>
//     * <p>
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li>The argument MUST have correct start time order for this algorithm to work.</li>
//     * <li>Failure messages are written to the class logger if logging is enabled.</li>
//     * <li>The <code>ResultStatus</code> contains a message describing any failure.</li>
//     * </ul>
//     * </p>
//     * 
//     * @param setRawData  the target set of <code>CorrelatedQueryDataOld</code> objects, corrected ordered 
//     * 
//     * @return  <code>ResultStatus</code> containing result of test, with message if failure
//     */
//    private ResultStatus verifyDisjointTimeDomains(SortedSet<RawCorrelatedData> setRawData) {
//        
//        // Check that remaining time domains are disjoint - this works if the argument is ordered correctly
//        // Loop through set checking that all start times are in order
//        int                 indPrev = 0;
//        RawCorrelatedData   datPrev = null;
//        for (RawCorrelatedData datCurr : setRawData) {
//            
//            // Initialize the loop - first time through
//            if (datPrev == null) {
//                datPrev = datCurr;
//                continue;
//            }
//            
//            // Extract time domains and check for closed intersection
//            TimeInterval    tvlPrev = datPrev.getTimeRange();
//            TimeInterval    tvlCurr = datCurr.getTimeRange();
//            
//            if (tvlPrev.hasIntersectionClosed(tvlCurr)) {
//                String      strMsg = JavaRuntime.getQualifiedMethodNameSimple()
//                        + "Time range collision at correlated data block "
//                        + Integer.toString(indPrev)
//                        + ", " + tvlPrev
//                        + " with " + tvlCurr;
//                
//                if (BOL_LOGGING)
//                    LOGGER.error(strMsg);
//                
//                return ResultStatus.newFailure(strMsg);
//            }
//            
//            indPrev++;
//            tvlPrev = tvlCurr;
//        }
//        
//        return ResultStatus.SUCCESS;
//    }
//
//    /**
//     * <p>
//     * Verifies the correct ordering of the sampling start times within the argument set.
//     * </p>
//     * <p>
//     * Extracts all starting times of the sampling clock in order to create the set
//     * { <i>t</i><sub>0</sub>, <i>t</i><sub>1</sub>, ..., <i>t</i><sub><i>N</i>-1</sub> } where
//     * <i>t<sub>n</sub></i> is the start time for <code>UniformSamplingBlock</code> instance 
//     * <i>n</i> and <i>N</i> is the size of the argument.   
//     * The ordered collection of start times is compared sequentially to check the following 
//     * conditions:
//     * <pre>
//     *   <i>t</i><sub>0</sub> < <i>t</i><sub>1</sub> < ... <  <i>t</i><sub><i>N</i>-1</sub>
//     * </pre>
//     * That is, we verify that the set 
//     * { <i>t</i><sub>0</sub>, <i>t</i><sub>1</sub>, ..., <i>t</i><sub><i>N</i>-1</sub> }
//     * forms a proper net.
//     * </p>
//     * <p>
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li>This test MUST be run before <code>{@link #verifyDisjointTimeDomains(Vector)}</code>.</li>
//     * <li>Failure messages are written to the class logger if logging is enabled.</li>
//     * <li>The <code>ResultStatus</code> contains a message describing any failure.</li>
//     * </ul>
//     * </p>
//     * 
//     * @param lstBlocks the constructed list of <code>UniformSamplingBlock</code> objects for this process 
//     * 
//     * @return  <code>ResultStatus</code> containing result of test, with message if failure
//     */
//    private ResultStatus verifyStartTimes(List<SampledBlock> lstBlocks) {
//        
//        // Loop through all ordered blocks - Check that all start times are in order
//        int             indPrev = 0;
//        SampledBlock    blkPrev = null;
//        for (SampledBlock blkCurr : lstBlocks) {
//            
//            // Initialize loop - first time through
//            if (blkPrev == null) {
//                blkPrev = blkCurr;
//                continue;
//            }
//            
//            // Compare the two instants
//            Instant insPrev = blkPrev.getStartTime();
//            Instant insCurr = blkCurr.getStartTime();
//            
//            if (insPrev.compareTo(insCurr) >= 0) {
//                String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
//                        + " - Bad start time ordering for sample block " 
//                        + Integer.toString(indPrev) 
//                        + " with start time = " + insPrev;
//                
//                if (BOL_LOGGING)
//                    LOGGER.error(strMsg);
//                
//                return ResultStatus.newFailure(strMsg);
//            }
//            
//            // Update state to next instant
//            indPrev++;
//            insPrev = insCurr;
//        }
//        
//        return ResultStatus.SUCCESS;
//    }
//
//    /**
//     * <p>
//     * Verifies that sampling domains within the argument are disjoint.
//     * </p>
//     * <p>
//     * Extracts the set of all sampling time domain intervals
//     * { <i>I</i><sub>0</sub>, <i>I</i><sub>0</sub>, ..., <i>I</i><sub><i>N</i>-1</sub> } where
//     * <i>I<sub>n</sub></i> is the sampling time domain for <code>UniformSamplingBlock</code> 
//     * instance <i>n</i> and <i>N</i> is the size of the argument.  
//     * We assume closed intervals of the form 
//     * <i>I</i><sub><i>n</i></sub> = [<i>t</i><sub><i>n</i>,start</sub>, <i>t</i><sub><i>n</i>,end</sub>]
//     * where <i>t</i><sub><i>n</i>,start</sub> is the start time for <code>UniformSamplingBlock</code> instance <i>n</i>
//     * and <i>t</i><sub><i>n</i>,end</sub> is the stop time.
//     * </p>
//     * <p>  
//     * The ordered collection of time domain intervals is compared sequentially to check the 
//     * following conditions:
//     * <pre>
//     *   <i>I</i><sub>0</sub> &cap; <i>I</i><sub>1</sub> = &empty;
//     *   <i>I</i><sub>1</sub> &cap; <i>I</i><sub>2</sub> = &empty;
//     *   ...
//     *   <i>I</i><sub><i>N</i>-2</sub> &cap; <i>I</i><sub><i>N</i>-1</sub> = &empty;
//     *   
//     * </pre>
//     * That is, every <em>adjacent</em> sampling time domain is disjoint.
//     * This algorithm is accurate <em>only if</em> the argument set is correctly ordered
//     * by sampling start times. 
//     * </p>
//     * <p>
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li>The argument MUST have correct start time order for this algorithm to work.</li>
//     * <li>Failure messages are written to the class logger if logging is enabled.</li>
//     * <li>The <code>ResultStatus</code> contains a message describing any failure.</li>
//     * </ul>
//     * </p>
//     * 
//     * @param lstBlocks the constructed list of <code>UniformSamplingBlock</code> objects for this process 
//     * 
//     * @return  <code>ResultStatus</code> containing result of test, with message if failure
//     */
//    private ResultStatus verifyDisjointTimeDomains(List<SampledBlock> lstBlocks) {
//        
//        // Loop through all ordered blocks - Check that time domains are disjoint 
//        // - this works if the argument is ordered correctly
//        int             indPrev = 0;
//        SampledBlock    blkPrev = null;
//        for (SampledBlock blkCurr : lstBlocks) {
//            
//            // Initialize loop - first time through
//            if (blkPrev == null) {
//                blkPrev = blkCurr;
//                continue;
//            }
//    
//            // Extract time domains and check for closed intersection
//            TimeInterval    domPrev = blkPrev.getTimeRange();
//            TimeInterval    domCurr = blkCurr.getTimeRange();
//            
//            if (domPrev.hasIntersectionClosed(domCurr)) {
//                String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
//                        + " - Time domain collision at sample block " 
//                        + Integer.toString(indPrev) 
//                        + ": " + domPrev + " with " + domCurr;
//                
//                if (BOL_LOGGING)
//                    LOGGER.error(strMsg);
//                
//                return ResultStatus.newFailure(strMsg);
//            }
//            
//            indPrev++;
//            domPrev = domCurr;
//        }
//        
//        return ResultStatus.SUCCESS;
//    }

    /**
     * <p>
     * Creates and returns the sampling blocks for the argument data.
     * </p>
     * <p>
     * One <code>SampledBlock</code> instance is created for each <code>RawCorrelatedData</code>
     * object in the argument collection.  If concurrency is disabled 
     * (see <code>{@link #enableConcurrency(boolean)}</code>) the <code>SampledBlock</code> instances are created
     * in the same order as the argument, which is assumed to be by sampling start time.
     * If concurrency is enabled the order of the returned list is not guaranteed to be that of the argument.  
     * Note that not all data sources need contribute to each <code>RawCorrelatedData</code> 
     * instance in the argument.  The time-series data values (i.e., <code>null</code> values)
     * for missing data sources should be addressed later. 
     * </p>
     * <p>
     * <h2>Concurrency</h2>
     * If concurrency is enabled (i.e., <code>{@link #bolConcurrency}</code> = <code>true</code>),
     * this method utilizes streaming parallelism if the argument size is greater than the
     * pivot number <code>{@link #szConcPivot}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li><s>The argument is must be ordered by starting time instant, or exception is thrown.</s></li>
     * <li><s>Arguments must not contain intersecting time domain, or exception is thrown.</s></li>
     * <li>All exceptions are thrown by the internal invocation of <code>{@link SampledBlock#from}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param setRawData  set of <code>RawCorrelatedData</code> objects ordered by start time
     * 
     * @return  an unordered list of <code>SampledBlock</code> objects, one for each element in the argument 
     * 
     * @throws UnsupportedOperationException    the raw data contained neither a sampling clock or a timestamp list
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws TypeNotPresentException an unsupported data type was detected within the argument
     */
    private List<SampledBlock>    buildSampledBlocks(Collection<RawCorrelatedData> setRawData) 
            throws UnsupportedOperationException, MissingResourceException, IllegalArgumentException, IllegalStateException, TypeNotPresentException {
        
        // Create the sample blocks, pivoting to concurrency by container size
        List<SampledBlock>    lstSmplBlks;
        
        if (this.bolConcurrency && (setRawData.size() > this.szConcPivot) ) {
            
            // invoke concurrency
            lstSmplBlks = setRawData
                    .parallelStream()
                    .<SampledBlock>map(dat -> SampledBlock.from(dat))   // throws all exceptions
                    .toList();
            
        } else {
            
            // serial processing
            lstSmplBlks = setRawData
                    .stream()
                    .<SampledBlock>map(dat -> SampledBlock.from(dat))   // throws all exceptions
                    .toList();
        }
        
        return lstSmplBlks;
    }
    
    /**
     * <p>
     * Creates and returns the sampling blocks for the argument data.
     * </p>
     * <p>
     * One <code>SampledBlock</code> instance is created for each <code>RawSuperDomData</code>
     * object in the argument collection.  If concurrency is disabled 
     * (see <code>{@link #enableConcurrency(boolean)}</code>) the <code>SampledBlock</code> instances are created
     * in the same order as the argument, which is assumed to be by sampling start time.
     * If concurrency is enabled the order of the returned list is not guaranteed to be that of the argument.  
     * Note that not all data sources need contribute to each <code>RawCorrelatedData</code> 
     * instance in the argument.  The time-series data values (i.e., <code>null</code> values)
     * for missing data sources should be addressed later. 
     * </p>
     * <p>
     * <h2>Time-Domain Collisions</h2>
     * The nature of the arguments assumed that there are time-domain collisions within the time-series data contained
     * in each element.  Accordingly, creation of the returned <code>SampledBlock</code> instances requires advanced
     * processing to handle these internal timestamping conflicts.  This processing is done in the constructor of
     * the <code>SampledSuperDomBlock</code> class, which are the actual sub-types of the returned collection.
     * </p>
     * <p>
     * <h2>Concurrency</h2>
     * If concurrency is enabled (i.e., <code>{@link #bolConcurrency}</code> = <code>true</code>),
     * this method utilizes streaming parallelism if the argument size is greater than the
     * pivot number <code>{@link #szConcPivot}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>All exceptions are thrown by the internal invocation of <code>{@link SampledBlock#from}</code>.</li>
     * <li>The arguments are assumed to contain raw data with time-domain collisions characteristic of <code>RawSuperDomData</code>.</li>
     * <li>The actual class type of the returned objects is <code>SampledBlockSuperDom</code>.</li>
     * </ul>
     * </p>
     * 
     * @param lstRawSupDomData  a collection of <code>RawSuperDomData</code> instances ready for data extraction
     * 
     * @return  an unordered list of <code>SampledBlock</code> objects, one for each element in the argument 
     * 
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws TypeNotPresentException  an unsupported data type was detected within the argument
     * @throws ExecutionException       general exception for serial processing (see cause)
     * @throws RejectedExecutionException a fill task failed execution for concurrent processing
     * @throws InterruptedException     timeout occurred while waiting for concurrent fill tasks to complete
     */
    private List<SampledBlock>   buildSampledBlocks(List<RawSuperDomData> lstRawSupDomData) 
            throws IllegalArgumentException, MissingResourceException, IllegalStateException, TypeNotPresentException, 
                   RejectedExecutionException, ExecutionException, InterruptedException 
    {
        
        // Create the sample blocks, pivoting to concurrency by container size
        List<SampledBlock>    lstSmplBlks = new ArrayList<>(lstRawSupDomData.size());

        for (RawSuperDomData dat : lstRawSupDomData) {
            SampledBlock    blkNew = SampledBlockSuperDom.from(dat);    // throws all exceptions
            
            lstSmplBlks.add(blkNew);
        }
        
        return lstSmplBlks;
    }
 
    /**
     * <p>
     * Performs the optional advanced error checking and data verification of the final aggregate set.
     * </p>
     * <p>
     * This is a convenience method for invoking all the verification methods publicly available on a
     * <code>SampledAggregate</code> object (i.e., once assembled).  The method simply calls all methods
     * prefixed as <code>verify...()</code> and checks the result.  At any failure the method returns with
     * with the reported error (i.e., as a <code>ResultRecord</code> instance).  Thus, not all tests may
     * be performed if an error is detected. 
     * </p>
     * <p>
     * The order of invocation is as follows:
     * <ol>
     * <li><code>{@link SampledAggregate#verifyStartTimeOrdering()}</code></li>
     * <li><code>{@link SampledAggregate#verifyDisjointTimeDomains()}</code></li>
     * <li><code>{@link SampledAggregate#verifySourceTypes()}</code></li>
     * </ol>
     * </p>
     * 
     * @param aggBlks   the sampled aggregate under inspection
     * 
     * @return  <code>{@link ResultStatus#SUCCESS}</code> if all tests passed, failure with message otherwise
     */
    private ResultStatus    verifySampledAggregate(SampledAggregate aggBlks) {
        
        ResultStatus    recResult;
        
        recResult = aggBlks.verifyStartTimeOrdering();
        if (recResult.isFailure())
            return recResult;
        
        recResult = aggBlks.verifyDisjointTimeDomains();
        if (recResult.isFailure())
            return recResult;
        
        recResult = aggBlks.verifySourceTypes();
        if (recResult.isFailure())
            return recResult;
        
        // If we are here everything checks out
        return ResultStatus.SUCCESS;
    }
    
//    private Callable<SampledBlock>   createBuildBlockTask(RawCorrelatedData dat) {
//        
//        Callable<SampledBlock>   task = () -> { 
//            SampledBlock blk = SampledBlock.from(dat); 
//            return blk; 
//            };
//        
//        return task;
//    }
//    
//    private Callable<SampledBlock>   createBuildBlockTask(RawSuperDomData dat) {
//        
//        Callable<SampledBlock>   task = () -> { 
//            SampledBlock blk = SampledBlock.from(dat); 
//            return blk; 
//            };
//        
//        return task;
//    }
 
}
