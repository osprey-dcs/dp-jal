/*
 * Project: dp-api-common
 * File:    RawSuperDomData.java
 * Package: com.ospreydcs.dp.api.query.model.series
 * Type:    RawSuperDomData
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
package com.ospreydcs.dp.api.query.model.aggr;

import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Represents a collection of <code>RawCorrelatedData</code> instances that have intersecting time ranges.
 * </p>
 * <p>
 * This class is intended as an intermediate object in the processing of <code>RawCorrelatedData</code> instances
 * with time domain collisions.  Once built class instances should contain a collection of all the 
 * <code>RawCorrelatedData</code> instances within a query response having a non-emtpy time range intersection.
 * Note there can be multiple but separate <code>RawSuperDomData</code> collections within the same query response.  
 * </p>
 * <p>
 * Once the <code>RawSuperDomData</code> collection is established, a "super domain" is defined.  This super domain
 * is the smallest time range containing all time ranges within the collection.
 * </p>
 * <p>
 * Note that a super domain may contain correlated data blocks that have no time range intersection.  For example
 * consider a super domain containing 3 data blocks with the time range intervals 
 * <i>I</i><sub>1</sub>, <i>I</i><sub>2</sub>, and <i>I</i><sub>3</sub>.
 * Suppose the following:
 * <pre>
 *   <i>I</i><sub>1</sub> &cap; <i>I</i><sub>2</sub> &ne; &empty;,
 *   <i>I</i><sub>2</sub> &cap; <i>I</i><sub>3</sub> &ne; &empty;,
 *   <i>I</i><sub>1</sub> &cap; <i>I</i><sub>2</sub> &cap; <i>I</i><sub>3</sub> = &empty;.
 * </pre>
 * Thus, every correlated data block has time range intersection with at least one other data block, however,
 * not all data blocks necessary intersect.  The resulting time range <i>I</i> for the above situation would be
 * <pre>
 *   <i>I</i> = [min(<i>I</i><sub>1</sub>, <i>I</i><sub>2</sub>, <i>I</i><sub>3</sub>), max(<i>I</i><sub>1</sub>, <i>I</i><sub>2</sub>, <i>I</i><sub>3</sub>)]
 * </pre>
 * where min(...) indicates the minimum element within all sets and max(...) indicates the maximum element within
 * all sets.  The value <i>I</i> would be returned by method <code>{@link #timeRange()}</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 30, 2025
 *
 */
public class RawSuperDomData implements Iterable<RawCorrelatedData> {

    
    //
    // Creators
    //
    
//    /**
//     * <p>
//     * Creates a new, empty <code>RawSuperDomData</code> instance from nothing.
//     * </p>
//     * 
//     * @return  a new <code>RawSuperDomData</code> instance containing no data (empty)
//     */
//    public static RawSuperDomData   from() {
//        return new RawSuperDomData();
//    }
    
    /**
     * <p>
     * Constructs a new <code>RawSuperDomData</code> instance initially populated with the given data block.
     * </p>
     *
     * @param datFirst  the first <code>RawCorrelatedData</code> block within the collection
     * 
     * @return  a new <code>RawSuperDomData</code> instance containing the single data block
     */
    public static RawSuperDomData   from(RawCorrelatedData datFirst) {
        return new RawSuperDomData(datFirst);
    }
    
    
    //
    // Application Resources
    //
    
    /** The Data Platform API default configuration parameter set */
    protected static final DpQueryConfig  CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Resources
    //
    
    /** Event logger */
    protected static final Logger    LOGGER = LogManager.getLogger();
    
    
    //
    // Class Constants
    //
    
    /** Serialization Identifier */
    private static final long serialVersionUID = -4143043367195908267L;

    
    /** Is logging active */
    protected static final boolean    BOL_LOGGING = CFG_QUERY.logging.active;
    
    
    /** General timeout limit  - for parallel thread pool tasks */
    protected static final long       LNG_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** General timeout units - for parallel thread pool tasks */
    protected static final TimeUnit   TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
    //
    // Instance Resources
    //
    
    /** The collection of correlated raw data with time range collisions forming the super domain */
    private final Set<RawCorrelatedData>  setRawData = new TreeSet<>(); 
    
    /** The starting time instant of the super domain */
    private Instant     insStart = null;
    
    /** The last time instant of the super domain */
    private Instant     insStop = null;
    
    
    //
    // Constructors
    //
    
//    /**
//     * <p>
//     * Constructs a new, empty <code>RawSuperDomData</code> instance.
//     * </p>
//     */
//    public RawSuperDomData() {
//    }
    
    /**
     * <p>
     * Constructs a new <code>RawSuperDomData</code> instance initially populated with the single argument.
     * </p>
     *
     * @param datFirst  the first <code>RawCorrelatedData</code> block within the collection
     */
    public RawSuperDomData(RawCorrelatedData datFirst) {
        this.setRawData.add(datFirst);
        
        this.insStart = datFirst.getStartTime();
        this.insStop = datFirst.getTimeRange().end();
    }
    
    
    // 
    // State Query
    //
    
    /**
     * <p>
     * Determines whether or not this super domain is empty.
     * </p>
     * 
     * @return  <code>true</code> if the super domain contains no data, <code>false</code> otherwise
     */
    public boolean  isEmpty() {
        return this.setRawData.isEmpty();
    }
    
    /**
     * <p>
     * Determines whether or not the given argument has a disjoint time domain with the current super domain.
     * </p>
     * 
     * @param datRaw    the <code>RawCorrelatedData</code> block under comparison
     * 
     * @return  <code>true</code> if the given data block has time range disjoint from the current collection,
     *          <code>false</code> if the given data block has non-empty intersection with at least one element in the current collection
     *          
     * @throws  IllegalStateException   the current collection is empty
     */
    public boolean  isDisjoint(RawCorrelatedData datRaw) throws IllegalStateException {

        // Check state
        if (this.setRawData.isEmpty())
            throw new IllegalStateException("The current collection is empty.");
        
        boolean bolDisjoint = this.setRawData.stream().allMatch(dat -> datRaw.hasDisjointTimeRange(dat));
        
        return bolDisjoint;
    }
    
    /**
     * <p>
     * Returns a new <code>TimeInterval</code> specifying the current time range of this 
     * <code>RawSuperDomData</code> collection in its current state.
     * </p>
     * <p>
     * Returns the smallest time interval containing all time ranges of the current collection of
     * <code>RawCorrelatedData</code> instances.
     * </p>
     * 
     * @return  the smallest time range containing all time ranges of correlated raw data within this collection,
     *          <s>or <code>{@link TimeInterval#EMPTY}</code> if this collection is empty</s>
     *          
     * @throws  IllegalStateException   the current collection is empty
     */
    public TimeInterval timeRange() throws IllegalStateException {
        
        // Check state
        if (this.setRawData.isEmpty())
            throw new IllegalStateException("The current collection is empty.");
        
        return TimeInterval.from(this.insStart, this.insStop);
    }
    
    /**
     * <p>
     * Returns the collection of correlated raw data blocks within the current super domain.
     * </p>
     * 
     * @return  Returns all <code>RawCorrelatedData</code> instances forming the current super domain.
     * 
     * @throws  IllegalStateException   the current collection is empty
     */
    public Set<RawCorrelatedData>   getSuperDomain() throws IllegalStateException {
        
        // Check state
        if (this.setRawData.isEmpty())
            throw new IllegalStateException("The current collection is empty.");

        return this.setRawData;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Adds the given correlated raw data block to the current super domain collection if not already present.
     * </p>
     * <p>
     * If the argument is <code>null</code> or already in the current super domain collection the operation
     * is refused an a value <code>false</code> is returned.  Otherwise the current collection is modified
     * with the additional argument along with the current super domain time range.
     * </p>  
     * 
     * @param datRaw    correlated raw data block to be added to the current super domain
     * 
     * @return  <code>true</code> if the data block was added, <code>false</code> if operation failed
     */
    public boolean  add(RawCorrelatedData datRaw) {
        
        // Check argument
        if (datRaw == null)
            return false;
        
        // Attempt to add the new data block
        boolean bolAdded = this.setRawData.add(datRaw);
        
        // Return false if addition failed
        if (!bolAdded)
            return false;

        // Set the new start time instant
        if (this.insStart!=null) {
            if (datRaw.getStartTime().isBefore(this.insStart))
                this.insStart = datRaw.getStartTime();
        } else {
            this.insStart = datRaw.getStartTime();
        }
        
        // Set the new final time instant
        if (this.insStop!=null) {
            if (datRaw.getTimeRange().end().isAfter(this.insStop))
                this.insStop = datRaw.getTimeRange().end();
        } else {
            this.insStop = datRaw.getTimeRange().end();
        }
        
        return true;
    }

    
    //
    // Iterable<RawCorrelatedData> Interface
    //
    
    /**
     * <p>
     * Returns an iterator that iterates over all correlated raw data blocks within the current super domain.
     * </p>
     * <p>
     * Invoking this method is the equivalent of calling <code>{@link #getSuperDomain()}.iterator()</code>.
     * Thus, it is essentially a convenience method allow <code>RawSuperDomData</code> instances to be
     * used directly in Java enhanced for loops (i.e., for-each loops).
     * </p>
     * 
     * @throws  IllegalStateException   the super domain is currently empty
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<RawCorrelatedData> iterator() throws IllegalStateException {
        
        // Check state
        if (this.setRawData.isEmpty())
            throw new IllegalStateException("The current collection is empty.");
        
        return this.setRawData.iterator();
    }
    
    
//    public SampledBlock    createSpuriousBlock() throws IllegalStateException {
//        
//        // Check state
//        if (this.setRawData.isEmpty()) {
//            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - super domain is empty.";
//            
//            if (BOL_LOGGING)
//                LOGGER.warn(strMsg);
//            
//            throw new IllegalStateException(strMsg);
//        }
//        
//        List<Instant>   lstTms = this.createTmsList(this.setRawData);
//        
//    }
    
    
    //
    // Support Methods
    //
    
//    private List<Instant>   createTmsList(Set<RawCorrelatedData> setRawData) {
//        LinkedList<Instant> lstTmsAll = new LinkedList<>();
//        
//        for (RawCorrelatedData datBlk : setRawData) {
//            List<Instant>   lstTmsBlk = datBlk.
//        }
//    }
    
}