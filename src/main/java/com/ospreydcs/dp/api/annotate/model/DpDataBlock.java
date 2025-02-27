/*
 * Project: dp-api-common
 * File:    DpDataBlock.java
 * Package: com.ospreydcs.dp.api.annotate.model
 * Type:    DpDataBlock
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
package com.ospreydcs.dp.api.annotate.model;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.ospreydcs.dp.api.common.TimeInterval;

/**
 * <p>
 * Represents a query domain region of time-series data within the Data Platform archive.
 * </p>
 * <p>
 * Data blocks form <em>basis sets</em> for the Data Platform time-series query domain.
 * Each data block itself essentially defining a query request.  Thus, a collection
 * of data blocks, termed a "data set" is a collection of basis sets defining an arbitrary 
 * region in the time-series query domain.   
 * </p>
 * <p>
 * Data blocks are composed of a list of data source names (Process Variable names) and a contiguous
 * time range interval within the query domain.  Thus, the entire time-series query domain can be
 * decomposed into a collection of data blocks.  Or in other words, the set of all data blocks
 * covers the entire time-series query domain.
 * </p>
 */
public class DpDataBlock {
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new <code>DpDataBlock</code> instance with the given set of PV names and time range.
     * </p>
     * 
     * @param setPvNames    data source names within the data block
     * @param tvlRange      time range of the data block (as a time interval)
     * 
     * @return  new <code>DpDataBlock</code> instance initialized with the given arguments
     */
    public static DpDataBlock from(Collection<String> setPvNames, TimeInterval tvlRange) {
        return new DpDataBlock(setPvNames, tvlRange);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataBlock</code> instance with the given set of PV names with first and last timestamps.
     * </p>
     * 
     * @param setPvNames    data source names within the data block
     * @param insTmsFirst   first timestamp within the data block (earliest)
     * @param insTmsLast    last timestamp within the data block (latest)
     * 
     * @return  new <code>DpDataBlock</code> instance initialized with the given arguments
     */
    public static DpDataBlock from(Collection<String> setPvNames, Instant insTmsFirst, Instant insTmsLast) {
        return new DpDataBlock(setPvNames, insTmsFirst, insTmsLast);
    }

    
    //
    // Class Constants
    //
    
    /** The empty data block */
    public static final DpDataBlock   EMPTY = DpDataBlock.from(List.of(), TimeInterval.EMPTY);
    
    
    //
    // Set Operations
    //
    
    /**
     * <p>
     * Determines whether or not two data blocks are disjoint within the query domain.
     * </p>
     * <p>
     * If the returned value is <code>false</code> then the two data blocks share a common region in 
     * query space, that is, they have a common intersection.  This means that they meet the following
     * conditions: 
     * <ol>
     * <li>Have at least one common data source name.</li>
     * <li>Have a finite time interval intersection.
     * </ol>
     * </p>
     * 
     * @param blk1  first data block for comparison
     * @param blk2  second data block for comparison
     * 
     * @return  <code>true</code> if data block are disjoint, 
     *          <code>false</code> if data blocks share a common region in query space   
     */
    public static boolean isDisjoint(DpDataBlock blk1, DpDataBlock blk2) {
        
        // Check if intervals are disjoint
        if (TimeInterval.isDisjoint(blk1.tvlRange, blk2.tvlRange))
            return true;
        
        // Check if given block contains a PV name within this block
        boolean bolPvMatch = blk1.lstPvNames.stream().anyMatch(pvName -> blk2.lstPvNames.contains(pvName));
        
        if (bolPvMatch == false)
            return true;
        
        // If we are here then the given arguments are not disjoint: there is 
        //  - a common time range intersection
        //  - at least one common data source
        return false;
    }
    
    /**
     * <p>
     * Determines whether or not two data blocks are equivalent.
     * </p>
     * <p>
     * Equivalent data blocks describe the same region in the time-series query domain.  The
     * two arguments can be different instances but define the same query domain.  This method
     * defers to override <code>{@link #equals(Object)}</code> which computes equivalence.
     * </p>
     * 
     * @param blk1  data block under test
     * @param blk2  data block under test
     * 
     * @return  <code>true</code> if both arguments describe the same query domain, <code>false</code> otherwise
     */
    public static boolean   isEquivalent(DpDataBlock blk1, DpDataBlock blk2) {
        
        if (blk1.equals(blk2))
            return true;
        else
            return false;
    }
    
    /**
     * <p>
     * Performs a query domain decomposition for the two data blocks so that the returned blocks are all disjoint.
     * </p>
     * <p>
     * Decomposes the query domain defined by the given arguments into an equivalent set of data blocks having
     * disjoint query domains.  This operation thus does nothing for two disjoint data blocks, returning the
     * original blocks if so.  If there is a finite query domain intersection then the domain is decomposed so
     * that each data block within the returned collection describes a unique domain region, except for domain
     * boundaries of the data blocks.
     * </p> 
     * <p>
     * The domain decomposition first checks for a finite query domain intersection between the two data blocks.
     * If none is found (i.e., the two data blocks are disjoint) then the original data blocks are returned.
     * If the data blocks have a common query domain region, up to 3 data blocks can be returned depending upon
     * the nature of the domain intersection.   
     * </p>
     * <p>
     * To identify the result first consider the following definitions:
     * <pre>
     *   &Phi;<sub>1</sub> &#8796; {data source names within argument <code>blk1</code>}
     *   &Phi;<sub>2</sub> &#8796; {data source names within argument <code>blk2</code>}
     *   &Phi;<sub>&cap;</sub> &#8796; &Phi;<sub>1</sub> &cap; &Phi;<sub>2</sub>
     *   
     *   &phi;<sub>1</sub> &#8796; &Phi;<sub>1</sub> \ &Phi;<sub><i>i</i></sub> (data source names unique to argument <code>blk1</code>)
     *   &phi;<sub>2</sub> &#8796; &Phi;<sub>2</sub> \ &Phi;<sub><i>i</i></sub> (data source names unique to argument <code>blk2</code>)
     *   
     *   I<sub>1</sub> &#8796; time range of data block 1
     *   I<sub>2</sub> &#8796; time range of data block 2
     *   I<sub>&cap;</sub> &#8796; I<sub>1</sub> &cap; I<sub>2</sub>
     *   I<sub>&cup;</sub> &#8796; I<sub>1</sub> &cup; I<sub>2</sub>
     *   
     *   B<sub>1</sub> &#8796; {&Phi;<sub>1</sub>, I<sub>1</sub>} = data block 1 (argument <code>blk1</code>)
     *   B<sub>2</sub> &#8796; {&Phi;<sub>2</sub>, I<sub>2</sub>} = data block 2 (argument <code>blk2</code>)
     *   B<sub>&cap;</sub> &#8796; B<sub>1</sub> &cap; B<sub>2</sub> = {&Phi;<sub>&cap</sub>, I<sub>&cap;</sub>}
     *   
     *   b<sub>1</sub> &#8796; {&phi;<sub>1</sub>, I<sub>1</sub>}
     *   B<sub>*</sub> &#8796; {&Phi;<sub>&cap;</sub>, I<sub>&cup;</sub>}
     *   b<sub>2</sub> &#8796; {&phi;<sub>2</sub>, I<sub>2</sub>}
     * </pre>  
     * Note that B<sub>&cap;</sub> &ne; &empty; necessarily implies that I<sub>&cap;</sub> &ne; &empty; and
     * that I<sub>&cup;</sub> is a contiguous interval containing both I<sub>1</sub> and I<sub>2</sub>.
     * That is, B<sub>*</sub> is well-defined and non-empty (&Phi;<sub>&cap;</sub> &ne; &empty).
     * </p>
     * <p>
     * Then the returned collection is given according to the conditions
     * <pre>
     *   B<sub>&cap;</sub> = &empty; &rArr; {B<sub>1</sub>, B<sub>2</sub>}
     *   B<sub>&cap;</sub> &ne; &empty; &rArr; {b<sub>1</sub>, B<sub>*</sub>, b<sub>2</sub>} 
     * </pre>
     * In the latter case it is possible that b<sub>1</sub> = &empty; and/or b<sub>2</sub> = &empty;
     * (equivalently, &phi;<sub>1</sub> = &empty; and/or &phi;<sub>2</sub> = &empty;).
     * In the event that block b<sub>1</sub> or b<sub>2</sub> is empty it will not be included in the
     * returned collection.
     * </p>
     * 
     * @param blk1  first data block
     * @param blk2  second data block
     * 
     * @return  collection of disjoint data blocks with equivalent query domain as arguments
     */
    public static List<DpDataBlock>  domainDecomposition(DpDataBlock blk1, DpDataBlock blk2) {
        
        // Check for disjoint data blocks
        if (DpDataBlock.isDisjoint(blk1, blk2))
            return List.of(blk1, blk2);
        
        // Data blocks have a common intersection...
        
        // Get the list of common PV names
        List<String>    lstPvsCom = blk1.lstPvNames
                .stream()
                .filter(pv -> blk2.lstPvNames.contains(pv))
                .toList();
        
        // Get list of PV names unique to block 1
        List<String>    lstPvs1 = new LinkedList<>(blk1.lstPvNames);
        lstPvs1.removeAll(lstPvsCom);
        
        // Get list of PV names unique to block 2
        List<String>    lstPvs2 = new LinkedList<>(blk2.lstPvNames);
        lstPvs2.removeAll(lstPvsCom);
        
        // Create the time range interval for the common PV names (must be common time interval intersection)
        TimeInterval    tvlCmn = TimeInterval.unionCommon(blk1.tvlRange, blk2.tvlRange);
        
        // Create all the new data blocks and return them
        List<DpDataBlock> lstBlks = new LinkedList<>();   // returned collection
        
        if (!lstPvs1.isEmpty())
            lstBlks.add(DpDataBlock.from(lstPvs1, blk1.tvlRange));
        if (!lstPvsCom.isEmpty())
            lstBlks.add(DpDataBlock.from(lstPvsCom, tvlCmn));
        if (!lstPvs2.isEmpty())
            lstBlks.add(DpDataBlock.from(lstPvs2, blk2.tvlRange));
        
        return lstBlks;
    }
    
    
    //
    // Instance Attributes
    //
    
    /** Collection of data source names within the new data block */
    private List<String>            lstPvNames = new LinkedList<>();
    
    /** The time interval of the new data block */
    private TimeInterval            tvlRange = null;

    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>DpDataBlock</code> instance initialized with the argument values.
     * </p>
     *
     * @param setPvNames    collection of data source names
     * @param tvlRange      time range of the data block (as a time interval)
     */
    public DpDataBlock(Collection<String> setPvNames, TimeInterval tvlRange) {
        this.lstPvNames.addAll(setPvNames);
        this.tvlRange = TimeInterval.from(tvlRange);
    }
    
    /**
     * <p>
     * Constructs a new <code>DpDataBlock</code> instance initialized with the argument values.
     * </p>
     *
     * @param setPvNames    collection of data source names
     * @param insTmsFirst   first timestamp within the data block (earliest)
     * @param insTmsLast    last timestamp within the data block (latest)
     */
    public DpDataBlock(Collection<String> setPvNames, Instant insTmsFirst, Instant insTmsLast) {
        this(setPvNames, TimeInterval.from(insTmsFirst, insTmsLast));
    }
    
    
    //
    // State Query
    //
    
    /**
     * <p>
     * Returns the collection of data source names within the data block.
     * </p>
     * 
     * @return  all data source names (PV names) within data block
     */
    public final Collection<String> getDataSources() {
        return this.lstPvNames;
    }
    
    /**
     * <p>
     * Returns the time range of the data block
     * </p>
     * 
     * @return  time range of the data block as a time interval
     */
    public final TimeInterval   getTimeRange() {
        return this.tvlRange;
    }

    
    //
    // Set Operations
    //
    
    /**
     * <p>
     * Determines whether or not the given data block is disjoint (in query domain) to this one.
     * </p>
     * <p>
     * The method defers to <code>{@link #isDisjoint(DpDataBlock, DpDataBlock)}</code> for returned
     * value.
     * </p>
     * 
     * @param blk   data block under test
     * 
     * @return  <code>true</code> if given data block is disjoint from this one, <code>false</code> otherwise
     * 
     * @see #isDisjoint(DpDataBlock, DpDataBlock)
     */
    public boolean  isDisjoint(DpDataBlock blk) {
        return DpDataBlock.isDisjoint(this, blk);
    }
    
    //
    // Object Overrides
    //
    
    /**
     *
     * @see @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DpDataBlock blk) {
            if (!blk.tvlRange.equals(this.tvlRange))
                return false;
            if (!blk.lstPvNames.equals(this.lstPvNames))
                return false;
            
            return true;
        }
        
        return false;
    }
    
    
}