/*
 * Project: dp-api-common
 * File:    SamplingIntervalRef.java
 * Package: com.ospreydcs.dp.api.query.model.proto
 * Type:    SamplingIntervalRef
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
 * @since Jan 11, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.proto;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.grpc.util.ProtoTime;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.FixedIntervalTimestampSpec;
import com.ospreydcs.dp.grpc.v1.common.Timestamp;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse.QueryReport;

/**
 * <p>
 * Maintains a reference between a uniform sampling interval Protobuf message and all associated 
 * data column messages.
 * </p>
 * <p>
 * Instances of this class can be used to build rows of data columns from a single query result set.  That is,
 * Each instance, in its final form, should contain a uniform sampling interval and all data columns from
 * data sources that were sampled during that interval.
 * </p>
 * <p>
 * The class provides a <code>{@link Comparable}</code> interface to order instance by initial timestamp.
 * That is, within collections of <code>SamplingIntervalRef</code> objects, instances are ordered according to
 * the sampling start time.  This is not an equivalence operation; it is possible that sampling intervals
 * can overlap and this condition should be checked.
 * </p>
 * <p>
 * The enclosed class <code>{@link StartTimeComparator}</code>, derived from <code>{@link Comparator}</code>, 
 * is also provided. It is equivalent in function the <code>{@link Comparable}</code> interface.  It is
 * offered for explicit comparison in Java container construction.
 * </p> 
 * <p>
 * <h2>WARNING</h2>
 * It is assumed that the Data TestArchive is consistent!  If correlated data was archived simultaneously with 
 * different sample clocks unpredictable behavior in reconstruction is possible.
 * </p> 
 * 
 *
 * @author Christopher K. Allen
 * @since Jan 11, 2024
 *
 * @see Comparable
 */
public class SamplingIntervalRef implements Comparable<SamplingIntervalRef> {
    
    
    //
    //  Class Types
    //
    
    /**
     * <p>
     * <code>{@link Comparator}</code> class for ordered comparison of two <code>SamplingIntervalRef</code> instances.
     * </p>
     * <p>
     * The comparison here is performed with the start times of the sampling intervals represented by 
     * <code>{@link SamplingIntervalRef}</code> instances returned from the 
     * <code>{@link SamplingIntervalRef#getStartInstant()</code> method.
     * Instances are intended for use in Java container construction.
     * </p>
     *
     * @author Christopher K. Allen
     * @since Jan 12, 2024
     *
     */
    public static class StartTimeComparator implements Comparator<SamplingIntervalRef> {

        //
        // Creators
        //
        
        /**
         * Creates a new <code>StartTimeComparator</code> instance.
         * 
         * @return  new  <code>StartTimeComparator</code> instance ready for use
         */
        public static StartTimeComparator   newInstance() {
            return new StartTimeComparator();
        }
        
        //
        // Comparator Interface
        //
        
        /**
         *
         * @see @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(SamplingIntervalRef o1, SamplingIntervalRef o2) {
            Instant t1 = o1.getStartInstant();
            Instant t2 = o2.getStartInstant();
            
            return t1.compareTo(t2);
        }
    }
    
    // 
    // Attributes and Resources
    //
    
    /** The initial sampling time instant */
    private final Instant                       insStart;
    
    /** The uniform sampling interval Protobuf message */
    private final FixedIntervalTimestampSpec    msgSmpIvl; 
    
    /** List of all applicable data column Protobuf messages */
    private final List<DataColumn>              lstMsgCols = new LinkedList<>();
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new, initialized instance of <code>SamplingIntervalRef</code>.
     * </p>
     * 
     * @param msgBucket source of initialization data
     * 
     * @return  new <code>SamplingIntervalRef</code> instance initialized with data from the argument
     */
    public static SamplingIntervalRef   from(QueryResponse.QueryReport.QueryData.DataBucket msgBucket) {
        return new SamplingIntervalRef(msgBucket);
    }
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>SamplingIntervalRef</code>.
     * </p>
     *
     * @param msgBucket source of initializing data
     */
    public SamplingIntervalRef(QueryResponse.QueryReport.QueryData.DataBucket msgBucket) {
        Timestamp   msgTmsStart = msgBucket.getSamplingInterval().getStartTime();
        DataColumn  msgDataCol = msgBucket.getDataColumn(); 
        
        this.insStart = ProtoMsg.toInstant(msgTmsStart);
        this.msgSmpIvl = msgBucket.getSamplingInterval();
        this.lstMsgCols.add(msgDataCol);
    }
    
    
    // 
    // Attribute Getters
    //
    
    /**
     * Returns the starting time instant of the sampling interval
     * 
     * @return sampling interval start instant
     */
    public final Instant getStartInstant() {
        return this.insStart;
    }


    /**
     * Returns the Protobuf message describing the sampling interval
     * 
     * @return sampling interval Protobuf message
     */
    public final FixedIntervalTimestampSpec getSamplingMessage() {
        return this.msgSmpIvl;
    }


    /**
     * Returns a list of all Protobuf data column message associated with this sampling interval.
     * 
     * @return all data currently associated with the sampling interval 
     */
    public final List<DataColumn> getAllDataMessages() {
        return this.lstMsgCols;
    }
    
    /**
     * <p>
     * Extracts and returns all unique data source names from the referenced <code>DataColumn</code> 
     * Protobuf messages.
     * </p>
     * 
     * @return set of all data source names within the referenced data
     */
    public final Set<String>    extractDataSourceNames() {
        Set<String> setNames = this.lstMsgCols
                .stream()
                .collect( 
                        TreeSet::new, 
                        (set, msg) -> set.add(msg.getName()), 
                        TreeSet::addAll
                        );
        
        return setNames;
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Adds the <code>DataColumn</code> within the argument to the list of referenced data ONLY IF 
     * sampling intervals are equivalent.
     * </p>
     * <p>
     * Checks for an equivalent sampling interval within the argument and, if so, adds the data column
     * within the argument bucket to the list of associated data.  
     * If the sampling intervals are NOT equivalent then nothing is done and a value 
     * <code>false</code> is returned.
     * </p>
     * 
     * @param msgBucket Protobuf message containing sampling interval and column data
     * 
     * @return      <code>true</code> only if argument data was successfully added to this reference,
     *              <code>false</code> otherwise (nothing done)
     */
    public boolean insertBucketData(QueryResponse.QueryReport.QueryData.DataBucket msgBucket) {
        
        // Check if list addition is possible - must have same sampling interval
        if (ProtoTime.equals(this.msgSmpIvl, msgBucket.getSamplingInterval())) {
            this.lstMsgCols.add(msgBucket.getDataColumn());
            
            return true;
        }
        
        return false;
    }
    
    
    //
    // Comparable<SamplingIntervalRef> Interface
    //
    
    /**
     * <p>
     * Compares the initial timestamp attribute <code>{@link #insStart}</code>.
     * </p>
     * <p>
     * Used for ordering sets and maps of <code>SamplingIntervalRef</code> objects.
     * 
     * <h2>NOTE:</h2>
     * This does not compare for equivalence.  The compared instance may have overlapping
     * sampling domains, which should be checked (eventually).
     * </p>
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(SamplingIntervalRef o) {
        return this.insStart.compareTo(o.insStart);
    }


    
}