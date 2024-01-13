/*
 * Project: dp-api-common
 * File:	TimeIntervalSet.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	TimeIntervalSet
 *
 * @author Christopher K. Allen
 * @since Sep 12, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>
 * This class represents a set of <code>TimeIntervals</code> where each member is
 * <ul>
 * <li>Unique</li>
 * <li>Ordered in time</li>
 * <li>Non-intersecting</li>
 * </ul>
 * That is, when viewed as a subset of the real line this class represents a 
 * collection of ordered, non-intersecting, closed intervals.
 * </p>
 * <p>
 * Practically speaking, the set is a representation of the specific time intervals 
 * when a particular data source was active.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Sep 12, 2022
 *
 */
public class TimeIntervalSet extends TreeSet<TimeInterval> {

    /** Serialization ID */
    private static final long serialVersionUID = 1L;
    

    //
    // Creation
    //

    /**
     * Creates a new, empty instance of <code>TimeIntervalSet</code>.
     *
     */
    public TimeIntervalSet() {
        super(TimeInterval.comparatorBegin());
    }

    /**
     * Creates a new instance of <code>TimeIntervalSet</code> and populates it
     * with the given collection of <code>TimeInterval</code> objects.  All ordering
     * and collection of intersecting intervals is performed during creation. 
     *
     * @param c collection of random <code>TimeInterval</code> derived objects
     */
    public TimeIntervalSet(Collection<? extends TimeInterval> c) {
        this();
        this.addAll(c);
    }

    
    // 
    // Population
    //
    
    /**
     * Add the interval to the set checking whether or not the given interval
     * intersects with any of the current intervals in the set.  If so,
     * the set is modified to contain only one interval which is the union
     * of the given argument and any intersecting sets previously contained in the
     * collection.
     */
    @Override
    public boolean add(TimeInterval tvlToAdd) {
        List<TimeInterval> lstTvlsIsct = new LinkedList<TimeInterval>();
        
        for (TimeInterval tvlMbr : this) 
            if (tvlMbr.hasIntersectionOpen(tvlToAdd)) {
                this.remove(tvlMbr);
                lstTvlsIsct.add(tvlMbr);
            }
        
        TimeInterval tvlNew = tvlToAdd;
        for (TimeInterval ivlIsct : lstTvlsIsct) 
            tvlNew = TimeInterval.intersection(tvlNew, ivlIsct);
        
        return super.add(tvlNew);
    }

    /**
     * Add the collection of <code>TimeIntervals</code> into this ordered set.
     * <ul>
     * <li>Any ordering of the given collection is not required.</li>
     * <li>Each interval in the collection is checked for intersection. </li>
     * <li>Any intersections are modified according to {@link #add(TimeInterval)}</li>
     * </ul>
     * 
     * @see #add(TimeInterval)
     */
    @Override
    public boolean addAll(Collection<? extends TimeInterval> c) {
        // Check edge conditions
        if (c.size() == 0)
            return false;
        
        // Add the collection checking for intersections
        int szStart = this.size();
        for (TimeInterval tvl : c)
            this.add(tvl);
        
        // If the size has changed the collection has definitely changed
        if (szStart == 0 || szStart != this.size())
            return true;
        
        // If the size has not changed the result is inconclusive
        //   An intersection could have occurred
        return true;
    }
    
    
    //
    //  Set Operations
    //
    
    /**
     * Returns the smallest time interval containing this set.
     * 
     * @return  the support of this set of time intervals
     */
    public TimeInterval support() {
        TimeInterval    ivlBeg = this.first();
        TimeInterval    ivlEnd = this.last();
        TimeInterval    ivlSup = TimeInterval.support(ivlBeg, ivlEnd);
        
        return ivlSup;
    }


    //
    // Overrides (Adds no functionality - just increases visibility)
    //
    
    @Override
    public NavigableSet<TimeInterval> headSet(TimeInterval toElement, boolean inclusive) {
        return super.headSet(toElement, inclusive);
    }

    @Override
    public SortedSet<TimeInterval> headSet(TimeInterval toElement) {
        return super.headSet(toElement);
    }

    @Override
    public TimeInterval higher(TimeInterval e) {
        return super.higher(e);
    }

    
}
