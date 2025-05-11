/*
 * Project: dp-api-common
 * File:	TimeDomainProcessor.java
 * Package: com.ospreydcs.dp.api.query.model.coalesce
 * Type: 	TimeDomainProcessor
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
 * @since Mar 28, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.superdom;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Intermediate processor for correlated data blocks with time domain collisions.
 * </p>
 * <p>
 * Class instances process sorted sets of <code>RawCorrelatedData</code> instances produced by the
 * <code>RawDataCorrelator</code> class.  Note that is possible that query results from a time-series data
 * request (made of the Query Service) can contain time-range collisions for process variables that were 
 * sampled during the same time interval but at different rates.  This condition is unlikely for most use
 * cases but the possibility exists.
 * </p>
 * <p>
 * <h2>Time-Domain Collisions</h2>
 * Whenever time-domain collisions occur in a recovered time-series data set special processing is required.
 * Sampling processes are all correlated to either sample clocks or timestamp lists after collection into 
 * correlated data blocks by the <code>RawDataCorrelator</code> class instances.  For a time-domain collision 
 * there are at least two different sampling clocks or timestamp lists enabled during the same time interval.  
 * The resulting timestamps must be resolved along with the process variable values.
 * </p>
 * <p>
 * Instances of <code>TimeDomainProcessor</code> perform an intermediate form of data processing when 
 * time-domain collisions are detected.  The original set of <code>RawCorrelatedData</code> instances is
 * inspected (i.e., that set given at creation time) for time-domain collisions.  Any that are found are
 * extracted and placed into "super domain" collections.  Both the super domains and the correlated raw data
 * without time-domain collisions is available after processing via methods <code>{@link #getSuperDomains()}</code>
 * and <code>{@link #getDisjointRawData()}</code>, respectively.
 * </p> 
 * <p>
 * <h2>Super Domains</h2>
 * Super domain are collections of <code>RawCorrelatedData</code> instances that have sampling processes that are
 * enabled during the same time interval.  Note that not all correlated data blocks within the collection have
 * finite time-domain intersections, that is, there can be disjoint data blocks within the collection.  However,
 * all correlated raw data blocks within the collection have at least one finite time-domain intersection with
 * another block.  For more information on super domains see the class documentation for <code>{@link RawSuperDomData}</code>.  
 * </p>
 * <p>
 * Super domains require special processing into <code>SampledBlock</code> instances if data tables are to be made
 * of the recovered time-series data.  Clearly the timestamps must be identified for all sampled processes within
 * the super domain.  Moreover, processes that have no value (i.e., are not sampled or undefined) at timestamps
 * outside there domain must have a value created.   
 * </p> 
 * <p>
 * <h2>NOTES:</h2>
 * For sampling process values at time instances where no sampling occurs a choice must be determined. Either the value 
 * is left undefined (i.e., <code>NaN</code> for numerical values) or set to <code>null</code>.   
 * Or some other possibility adopted.  
 * This choice is likely left as a configuration parameter in the processing to follow.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 28, 2025
 *
 */
public class TimeDomainProcessor {

    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new <code>TimeDomainProcessor</code> instance initialized to the given correlated raw data set.
     * </p>
     * 
     * @param setDataOrg    the correlated raw data set under processing
     * 
     * @return  a new time domain collision processor for the given data set
     */
    public static TimeDomainProcessor  from(SortedSet<RawCorrelatedData> setRawData) {
        return new TimeDomainProcessor(setRawData);
    }
    
    
    //
    // Utilities
    //
    
    /**
     * <p>
     * Verifies the correct ordering of the sampling start times within the argument set.
     * </p>
     * <p>
     * Extracts all starting times of the sampling clock in order to create the set
     * { <i>t</i><sub>0</sub>, <i>t</i><sub>1</sub>, ..., <i>t</i><sub><i>N</i>-1</sub> } where
     * <i>t<sub>n</sub></i> is the start time for <code>RawCorrelatedData</code> instance <i>n</i>
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
     * <li>This test MUST be run before <code>{@link #verifyDisjointTimeDomains(SortedSet)}</code>.</li>
     * <li>Failure messages are written to the class logger if logging is enabled.</li>
     * <li>The <code>ResultStatus</code> contains a message describing any failure.</li>
     * </ul>
     * </p>
     * 
     * @param setRawData  the target set of processed <code>CorrelatedQueryDataOld</code> objects 
     * 
     * @return  <code>ResultStatus</code> containing result of test, with message if failure
     */
    public static ResultStatus verifyStartTimeOrdering(SortedSet<RawCorrelatedData> setRawData) {
        
        // Loop through set checking that all start times are in order
        int                 indCurr = 0;
        RawCorrelatedData   datCurr = null;
        for (RawCorrelatedData datNext : setRawData) {
            
            // Initialize the loop - first time through
            if (datCurr == null) {
                datCurr = datNext;
                continue;
            }
            
            // Compare the two instants
            Instant     insCurr = datCurr.getStartTime();
            Instant     insNext = datNext.getStartTime();
            
            if (insCurr.compareTo(insNext) >= 0) {
                String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                        + " - Bad start time ordering at argument element " + indCurr + " with start time = "
                        + insCurr + " compared to next element start time " + insNext;
                
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
     * Verifies that sampled domains within the argument are disjoint (<code>{@link ResultStatus#SUCCESS}</code>).
     * </p>
     * <p>
     * Extracts the set of all sampling time range intervals
     * { <i>I</i><sub>0</sub>, <i>I</i><sub>1</sub>, ..., <i>I</i><sub><i>N</i>-1</sub> } where
     * <i>I<sub>n</sub></i> is the sampling time domain for <code>RawCorrelatedData</code> 
     * instance <i>n</i> and <i>N</i> is the size of the argument.  
     * We assume closed intervals of the form 
     * <i>I</i><sub><i>n</i></sub> = [<i>t</i><sub><i>n</i>,start</sub>, <i>t</i><sub><i>n</i>,end</sub>]
     * where <i>t</i><sub><i>n</i>,start</sub> is the start time for <code>RawCorrelatedData</code> instance <i>n</i>
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
     * <li>A returned value of <code>{@link ResultStatus#FAIL}</code> indicate presence of time-domain collisions.</li>
     * </ul>
     * </p>
     * 
     * @param setDataOrg  the target set of <code>CorrelatedQueryDataOld</code> objects, corrected ordered 
     * 
     * @return  <code>ResultStatus</code> containing result of test, with message if failure
     */
    public static ResultStatus  verfifyDisjointTimeDomains(SortedSet<RawCorrelatedData> setRawData) {
        
        // Check that remaining time domains are disjoint - this works if the argument is ordered correctly
        // Loop through set checking that all start times are in order
        int                 indPrev = 0;
        RawCorrelatedData   datPrev = null;
        for (RawCorrelatedData datCurr : setRawData) {
            
            // Initialize the loop - first time through
            if (datPrev == null) {
                datPrev = datCurr;
                continue;
            }
            
            if (!datPrev.hasDisjointTimeRange(datCurr)) {
                String      strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                        + "Time range collision at correlated data block "
                        + Integer.toString(indPrev)
                        + ", " + datPrev.getTimeRange()
                        + " with " + datCurr.getTimeRange();
                
                return ResultStatus.newFailure(strMsg);
            }
            
            indPrev++;
            datPrev = datCurr;
        }
        
        return ResultStatus.SUCCESS;
    }

    
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
    
    
    /** Use advanced error checking and verification flag */
    public static final boolean     BOL_ERROR_CHK = CFG_QUERY.data.table.construction.errorChecking;
    
    /** Enable/disable allowing time domain collisions within correlated data */ 
    public static final boolean     BOL_DOM_COLLISION = CFG_QUERY.data.table.construction.domainCollision;
    
    
    /** Concurrency enabled flag */
    public static final boolean     BOL_CONCURRENCY = CFG_QUERY.concurrency.enabled;
    
    /** Parallelism tuning parameter - pivot to parallel processing when lstMsgDataCols size hits this limit */
    public static final int        SZ_CONCURRENCY_PIVOT = CFG_QUERY.concurrency.pivotSize;
    

    //
    // Class Resources
    //
    
    /** Event logger for class */
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

    /** The original correlated set to under processing */
    private final SortedSet<RawCorrelatedData>  setDataOrg;
    
    
    //
    //  Instance Resources
    //
    
    /** Ordered list of correlated data blocks actively being processed - will be disjoint when finished (super domains are removed) */
    private final List<RawCorrelatedData>       lstDataActive = new LinkedList<>();
    
    /** Ordered list of super domains */
    private final List<RawSuperDomData>         lstSuperDoms = new LinkedList<>();
    
    
    //
    // State Variables
    //
    
    /** Flag indicating whether or not data has been processed */
    private boolean bolProcessed;
    
    /** Flag indicating whether or not time-domain collisions were encountered */
    private boolean bolTmDomColl;
    
    /** The index of the currently processed correlated data block within <code>{@link #lstDataActive}</code> */
    private int     indListCurr;
    
    /** The last index of the correlated data set list (exclusive) in its current state */
    private int     indListLast;
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>TimeDomainProcessor</code> instance for the given set of correlated raw data.
     * </p>
     *
     * @param setDataOrg    the correlated raw data set under processing
     */
    public TimeDomainProcessor(SortedSet<RawCorrelatedData> setRawData) {
        this.setDataOrg = setRawData;
        this.lstDataActive.addAll( setRawData );
        
        this.bolProcessed = false;
        this.bolTmDomColl = false;
        this.indListCurr = 0;
        this.indListLast = this.lstDataActive.size();
    }
    
    
    //
    // State Query
    //
    
    /**
     * <p>
     * Returns whether or not the target set of raw, correlated data has been processed.
     * </p>
     * <p>
     * A returned value of <code>true</code> indicates that the method <code>{@link #process()}</code> has
     * be invoked.  This method should be invoked only once.
     * </p>
     * 
     * @return  <code>true<code> if method <code>{@link #process()}</code> has been invoked, <code>false</code> otherwise
     */
    public boolean hasProcessed() {
        return this.bolProcessed;
    }
    
    /**
     * <p>
     * Determines whether or not the target set or raw, correlated data has super domains after processing.
     * </p>
     * <p>
     * This method should only be called after invoking <code>{@link #process()}</code> otherwise an exception is
     * thrown.  A returned value of <code>true</code> indicates that the target set contained raw data with
     * intersecting time domains and the coalesced data forming any super domains is available from method 
     * <code>{@link #getSuperDomains()}</code>.  
     * </p>
     * <p>
     * The remaining raw, correlated data blocks that did not contain any time domain collisions and were naturally
     * disjoint are available from the method <code>{@link #getDisjointRawData()}</code> after processing.
     * 
     * @return
     * 
     * @throws IllegalStateException
     */
    public boolean hasSuperDomains() throws IllegalStateException {
        
        // Check state
        if (!this.bolProcessed)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Target raw, correlated data set has not been processed.");
        
        return this.bolTmDomColl;
    }
    
    
    //
    // Attribute Query
    //
    
    /**
     * <p>
     * Returns the original sorted set of correlated raw data to process.
     * </p>
     * 
     * @return  the target set of correlated raw data under processing
     */
    public SortedSet<RawCorrelatedData> getOriginalData() {
        return this.setDataOrg;
    }
    
    /**
     * <p>
     * Returns the list of <code>RawCorrelatedData</code> blocks that do not belong to any super domains.
     * </p>
     * <p>
     * The returned list is available after the <code>{@link #process()}</code> method has been invoked,
     * otherwise an exception is thrown.  The list contains the <code>RawCorrelatedData</code> instances
     * from the original target set which have no time domain collisions with any other instances.  They are
     * in the order of the original target set, with any correlated raw data block WITH collision removed
     * (those will be used to create the super domains to which they belong).
     * </p>
     * 
     * @return  ordered list of correlated raw data blocks from original set with no time range collisions
     * 
     * @throws IllegalStateException    the original data set has not yet been processed
     */
    public List<RawCorrelatedData>  getDisjointRawData() throws IllegalStateException {
        
        // Check state
        if (!this.bolProcessed) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Original data set has not been processed yet.";
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new IllegalStateException(strMsg);
        }
        
        return this.lstDataActive;
    }
    
    /**
     * <p>
     * Returns the list of disjoint super domains within the processed data set.
     * </p>
     * <p>
     * The returned list is available after the <code>{@link #process()}</code> method has been invoked,
     * otherwise an exception is thrown.  The list contains all the super domains with disjoint time ranges,
     * moreover, their time ranges are disjoint from all <code>RawCorrelatedData</code> blocks returned
     * from <code>{@link #getDisjointRawData()}</code>.  The ordering of the super domains follows the ordering
     * of the original target set presented at creation.
     * </p>
     * 
     * @return  ordered list of super domains with time ranges all disjoint from all other super domains and correlated data blocks
     * 
     * @throws IllegalStateException    the original data set has not yet been processed
     */
    public List<RawSuperDomData>    getSuperDomains() throws IllegalStateException {
        
        // Check state
        if (!this.bolProcessed) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - Original data set has not been processed yet.";
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new IllegalStateException(strMsg);
        }
        
        return this.lstSuperDoms;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Processes the target correlated raw data set to identify and extract the super domains within set.
     * </p>
     * <p>
     * This method should be invoked once and only once, after which the processed data is available via the
     * methods <code>{@link #getDisjointRawData()}</code> and <code>{@link #getSuperDomains()}</code>.  Invoking
     * the method repeatedly has no effect.
     * </p>
     * <p>
     * The method iterates through the target set of <code>RawCorrelatedData</code> instance provided at creation, 
     * identifies the super domains within the collection, and extracts them.  Once complete the set of all disjoint 
     * super domains within the original collection is available from the method <code>{@link #getSuperDomains()}</code>.
     * The collection of remaining correlated raw data block from the original target set is available from
     * the method <code>{@link #getDisjointRawData()}</code>.  Note that all super domains and all correlated raw data
     * blocks returned from the above methods then has disjoint time-range intervals.  The raw data collections are
     * then ready for further processing.
     * </p>
     * <p>
     * The ordering of the returned list follows the ordering of the original target set of correlated raw data blocks.
     * Specifically, the first super domain encountered in the target set is the first super domain in the returned list
     * and the remaining follow in order.
     * </p>
     * 
     * @return  <code>true</code> if time-domain collisions were found and processed, 
     *          <code>false</code> if no time-time collisions were found 
     * 
     * @throws IllegalStateException     internal processing error: attempt to process data when more super domains existed 
     * @throws IndexOutOfBoundsException internal processing error: attempt to access index beyond current enabled data list
     */
    public boolean process() throws IllegalStateException, IndexOutOfBoundsException {
        
        if (this.bolProcessed) {
            if (BOL_LOGGING)
                LOGGER.warn("{} - Repeated call, data has already been processed.", JavaRuntime.getQualifiedMethodNameSimple());
            
            return this.bolTmDomColl;
        }
        
        while (this.hasNext()) {
            RawSuperDomData domNext = this.extractNext();
        
            this.lstSuperDoms.add(domNext);
            this.bolTmDomColl = true;
        }
        
        this.bolProcessed = true;
        
        return this.bolTmDomColl;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Checks for any remaining super domains within the currently unprocessed correlated data blocks.
     * </p>
     * <p>
     * This method inspects the remaining (unprocessed) correlated raw data blocks within the processing list 
     * <code>{@link #lstDataActive}</code>.  It compares each element against all other elements for time 
     * domain collisions and returns <code>true</code> if one is detected.
     * </p>
     * <p>
     * <h2>IMPORTANT:</h2>
     * This method also advances the <code>{@link #indListCurr}</code> state variable during the search.
     * If a time domain collision is detected (i.e., a value <code>true</code> is returned) then the index
     * then points to the first element in the list where the collision was detected.  The index is then
     * used by the method <code>{@link #extractNext()}</code> to extract the super domain for the element
     * at the current index.
     * </p>
     * <p>
     * If a value <code>false</code> is returned then the state variable <code>{@link #indListCurr}</code>
     * has the position of the last element <code>{@link #indListLast}</code>.
     * 
     * @return  <code>true</code> if there is a super domain within the unprocessed data,
     *          <code>false</code> otherwise
     */
    private boolean hasNext() {
        
        // Check if we are at the last data block
        if (this.indListCurr >= this.indListLast)
            return false;
        
        // Check each list element at or greater than current enabled list index
        while (this.indListCurr < this.indListLast) {
            RawCorrelatedData               datSubject = this.lstDataActive.get(this.indListCurr);
            ListIterator<RawCorrelatedData> iterData = this.lstDataActive.listIterator(this.indListCurr+1);
            
            // Compare all forward data blocks against the current element
            while (iterData.hasNext()) {
                RawCorrelatedData   datCmp = iterData.next();
                
                // Check for intersection
                if (!datSubject.hasDisjointTimeRange(datCmp)) {
                    return true;    // time range collision
                }
            }
            
            // No collision - advance current index for next
            this.indListCurr++;
        }

        // If we have made it here there were no collisions, thus no super domains left
        return false;
    }
    
    /**
     * <p>
     * Extracts and creates the next super domain within the correlated raw data set (if it exists).
     * </p>
     * <p>
     * This method is used in conjunction with method <code>{@link #hasNext()}</code> to parse through the
     * list of actively processed correlated data blocks <code>{@link #lstDataActive}</code> to fully process
     * the original target set of blocks <code>{@link #setDataOrg}</code>.  After processing the attribute
     * <code>{@link #lstDataActive}</code> will contain only <code>RawCorrelatedData</code> block instances
     * with disjoint time domains.  The attribute <code>{@link #lstSuperDoms}</code> will contain super domains
     * with disjoint time domains.  Additionally, all time ranges within <code>{@link #lstDataActive}</code>
     * and <code>{@link #lstSuperDoms}</code> will be disjoint with each other.
     * </p>
     * <p>
     * Note that the returned super domain may contain correlated data blocks that have no intersection
     * with other blocks, but each block will have at least one intersection with one other block.  
     * See class documentation for <code>{@link RawSuperDomData}</code> for further details.
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * <ul>
     * <li>
     * There <b>must</b> be a domain collision within the current list of correlated data
     * blocks under processing (i.e., attribute <code>{@link #lstDataActive}</code>).  Otherwise, overall
     * processing will fail (an exception is thrown).
     * </li>
     * <li>
     * This method modifies the list of correlated data blocks under processing, <code>{@link #lstDataActive}</code>.
     * Specifically, it removes data blocks from the list that belong to the returned <code>RawSuperDomData</code>
     * instance.  Likewise, the state variable <code>{@link #indListLast}</code> is also modified to
     * reflect the missing blocks.
     * </li>
     * <li>
     * This method DOES NOT modify state variable <code>{@link #indListCurr}</code>.  It is left in its
     * current position at the time of method invocation. (Method <code>{@link #hasNext()}</code> does
     * modify this state variable.)
     * </li>
     * </p>
     * 
     * @return  the next <code>RawSuperDomData</code> collection within the correlated raw data
     *          
     * @throws  IllegalStateException       there were no more super domains to process
     * @throws  IndexOutOfBoundsException   internal processing error (severe, should not happen)
     *          
     * #see {@link #extractSuperDomain(RawCorrelatedData)}
     */
    private RawSuperDomData  extractNext() throws IllegalStateException, IndexOutOfBoundsException {
        
        // Check index
        if (this.indListCurr >= this.indListLast) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - No more correlated data blocks to process.";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalStateException(strMsg);
        }
        
        // Get the target correlated data block and create next super domain
        RawCorrelatedData   datStart = this.lstDataActive.remove(this.indListCurr);
        RawSuperDomData     domNext = RawSuperDomData.from(datStart);
        
        // Adjust processing list indices
        this.indListCurr++;
        this.indListLast--;
        
        boolean bolError = true;    // At least one block must be added or error occurred (double check)
        boolean bolRepeat = false;  // Must repeat until no more blocks are added 
        
        do {
            // Check if we have already processed all data blocks
            if (this.indListCurr >= this.indListLast) {
                
                // Good to go - all remaining blocks were part of the super domain
                if (!bolError)
                    return domNext;
                
                // Something went wrong - no blocks were added and we are at the end of the list
                else {
                    String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                            + " - Serious error: encountered end of processing list without finding domain collision, "
                            + " current index at " + this.indListCurr;
                    
                    if (BOL_LOGGING)
                        LOGGER.error(strMsg);
                    
                    throw new IndexOutOfBoundsException(strMsg);
                }
            }
            
            // Check all forward data blocks for domain collision with the current super domain
            ListIterator<RawCorrelatedData> iterData = this.lstDataActive.listIterator(this.indListCurr); // throws exception

            bolRepeat = false;
            while (iterData.hasNext()) {
                RawCorrelatedData   datCmp = iterData.next();
                
                // Check for domain collision with super domain
                if (!domNext.isDisjoint(datCmp)) {
                    domNext.add(datCmp);
                    
                    this.lstDataActive.remove(datCmp);
                    this.indListLast--;
                    bolRepeat = true;   // must do this again since super domain has changed
                    bolError = false;
                }
            }
            
        } while (bolRepeat);

        return domNext;
    }
    
    /**
     * <p>
     * Extracts all correlated data blocks forming the super domain of the argument.
     * </p>
     * <p>
     * Note that the returned super domain may contain correlated data blocks that have no intersection
     * with the given argument.  See class documentation for <code>{@link RawSuperDomData}</code> for further
     * details.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * The given argument <b>must</b> have a domain collision within the current list of correlated data
     * blocks under processing (i.e., attribute <code>{@link #lstDataActive}</code>).  Otherwise, overall
     * processing will fail.
     * </p>
     * 
     * @param datSubject    correlated data block initiating super domain
     * 
     * @return  super domain that contains the time domain of the argument
     * 
     * @deprecated Not used, replaced with {@link #hasNext()} and {@link #extractNext()}
     */
    @Deprecated(since="April 1, 2025", forRemoval=true)
    private RawSuperDomData extractSuperDomain(RawCorrelatedData datSubject) {
        RawSuperDomData setSuper = RawSuperDomData.from(datSubject);
        this.lstDataActive.remove(datSubject);
        
        boolean     bolRepeat = false;
        do {
            for (RawCorrelatedData datCurr : this.lstDataActive) {
                if (!setSuper.isDisjoint(datCurr)) {
                    setSuper.add(datCurr);
                    
                    // Advance the current list index to the next position
                    this.indListCurr = this.lstDataActive.indexOf(datCurr);
                    
                    // Remove the intersecting data block from the current disjoint list
                    this.lstDataActive.remove(datCurr);
                    
                    // Every time you modify the super domain you need to check the remaining data blocks
                    bolRepeat = true;
                    continue;
                }
                
                bolRepeat = false;
            }
            
        } while (bolRepeat);

        return setSuper;
    }
    
}
