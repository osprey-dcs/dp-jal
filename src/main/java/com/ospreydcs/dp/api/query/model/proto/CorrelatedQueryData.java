/*
 * Project: dp-api-common
 * File:    CorrelatedQueryData.java
 * Package: com.ospreydcs.dp.api.query.model.proto
 * Type:    CorrelatedQueryData
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

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.grpc.util.ProtoTime;
import com.ospreydcs.dp.api.model.ResultRecord;
import com.ospreydcs.dp.api.model.TimeInterval;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.FixedIntervalTimestampSpec;
import com.ospreydcs.dp.grpc.v1.common.Timestamp;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

/**
 * <p>
 * Maintains a reference between a uniform sampling interval Protobuf message and all associated 
 * data column messages within query data.
 * </p>
 * <p>
 * Instances of this class can be used to build rows of data columns from a single query result set.  That is,
 * Each instance, in its final form, should contain a uniform sampling interval and all data columns from
 * data sources that were sampled during that interval.
 * </p>
 * <p>
 * The class provides a <code>{@link Comparable}</code> interface to order instance by initial timestamp.
 * That is, within collections of <code>CorrelatedQueryData</code> objects, instances are ordered according to
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
 * It is assumed that the Data Platform archive is consistent!  If correlated 
 * data was archived simultaneously with different sample clocks unpredictable 
 * behavior in reconstruction is possible.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Jan 11, 2024
 *
 * @see Comparable
 */
public class CorrelatedQueryData implements Comparable<CorrelatedQueryData> {
    
    
    //
    //  Class Types
    //
    
    /**
     * <p>
     * <code>{@link Comparator}</code> class for ordered comparison of two <code>CorrelatedQueryData</code> instances.
     * </p>
     * <p>
     * The comparison here is performed with the start times of the sampling intervals represented by 
     * <code>{@link CorrelatedQueryData}</code> instances returned from the 
     * <code>{@link CorrelatedQueryData#getStartInstant()</code> method.
     * Instances are intended for use in Java container construction.
     * </p>
     *
     * @author Christopher K. Allen
     * @since Jan 12, 2024
     *
     */
    public static class StartTimeComparator implements Comparator<CorrelatedQueryData> {

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
        public int compare(CorrelatedQueryData o1, CorrelatedQueryData o2) {
            Instant t1 = o1.getStartInstant();
            Instant t2 = o2.getStartInstant();
            
            return t1.compareTo(t2);
        }
    }

    
    // 
    // Attributes 
    //
    
    /** The uniform sampling interval Protobuf message - the correlation subject */
    private final FixedIntervalTimestampSpec    msgSmplClk; 
    
    /** List of all data column Protobuf messages correlated within sampling interval - correlated objects */
    private final List<DataColumn>              lstMsgCols = new LinkedList<>();
    
    
    //
    // Resources
    //
    
    /** The initial sampling time instant - taken from <code>msgSmpIval</code> */
    private final Instant                       insStart;
    
    /** The time domain of contained query data - computed from <code>msgSmpIval</code> */
    private final TimeInterval                  ivlTimeDomain;
    
    /** Set of all unique data source names active within sampling interval - taken from data messages */
    private final Set<String>                   setSrcNms = new TreeSet<>();
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new, initialized instance of <code>CorrelatedQueryData</code>.
     * </p>
     * 
     * @param msgBucket source of initialization data
     * 
     * @return  new <code>CorrelatedQueryData</code> instance initialized with data from the argument
     */
    public static CorrelatedQueryData   from(QueryResponse.QueryReport.QueryData.DataBucket msgBucket) {
        return new CorrelatedQueryData(msgBucket);
    }
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>CorrelatedQueryData</code>.
     * </p>
     *
     * @param msgBucket source of initializing data
     */
    public CorrelatedQueryData(QueryResponse.QueryReport.QueryData.DataBucket msgBucket) {
        Timestamp   msgTmsStart = msgBucket.getSamplingInterval().getStartTime();
        DataColumn  msgDataCol = msgBucket.getDataColumn(); 
    
        this.insStart = ProtoMsg.toInstant(msgTmsStart);
        this.msgSmplClk = msgBucket.getSamplingInterval();
        this.setSrcNms.add(msgDataCol.getName());
        this.lstMsgCols.add(msgDataCol);
        
        // Compute time domain
        int     cntSamples = this.msgSmplClk.getNumSamples();
        long    lngPeriodNs = this.msgSmplClk.getSampleIntervalNanos();
        Duration    durPeriod = Duration.ofNanos(lngPeriodNs);
        Duration    durDomain = durPeriod.multipliedBy(cntSamples - 1);
        Instant     insStop = this.insStart.plus(durDomain);
        
        this.ivlTimeDomain = TimeInterval.from(this.insStart, insStop);
    }
    
    
    // 
    // Attribute Getters
    //
    
    /**
     * <p>
     * Returns the number of samples for each data column as specified in the sampling interval message.
     * </p>
     * <p>
     * Obtains the returned value from the Protobuf message describing the sampling interval.
     * Specifically, returns the field value containing the number of samples.
     * All correlated data columns <em>should</em> have the same number of data values,
     * the returned value, if the Data Platform archive is consistent. 
     * </p>
     * 
     * @return  the size of each correlated data column
     */
    public final int    getSampleCount() {
        return this.msgSmplClk.getNumSamples();
    }
    
    /**
     * Returns the starting time instant of the sampling interval
     * 
     * @return sampling interval start instant
     */
    public final Instant getStartInstant() {
        return this.insStart;
    }
    
    /**
     * <p>
     * Returns the time domain of the sampling interval.
     * </p>
     * <p>
     * The returned interval left end point is the start time of the sampling interval 
     * returned by <code>{@link #getStartInstant()}</code>.  The right end point is the
     * last timestamp of the sampled data within the query data.  Specifically, it 
     * DOES NOT include the sample period duration following the last timestamp.
     * </p>
     * 
     * @return  the time domain of contained query data [first timestamp, last timestamp]
     */
    public final TimeInterval getTimeDomain() {
        return this.ivlTimeDomain;
    }

    /**
     * Returns the set of unique names for all the data sources that are active within the
     * subject sampling interval.
     *  
     * @return
     */
    public final Set<String>    getSourceNames() {
        return this.setSrcNms;
    }
    
    /**
     * Returns the Protobuf message describing the sampling interval
     * 
     * @return sampling interval Protobuf message
     */
    public final FixedIntervalTimestampSpec getSamplingMessage() {
        return this.msgSmplClk;
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
     * 
     * @deprecated replaced by <code>{@link #getSourceNames()}</code>
     */
    @Deprecated(since="Jan 31, 2024", forRemoval=true)
    private final Set<String>    extractDataSourceNames() {
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
    // Data Verifications
    //
    
    /**
     * <p>
     * Verifies that the given set of correlated data has unique data source names.
     * </p>
     * <p>
     * Note that the data source name for each inserted <code>BucketData</code> message is 
     * recorded during insertion.  This method verifies that the current set of data inserted
     * into this instance is consistent and ready for further processing.
     * </p>
     * <p>
     * The following conditions are checked:
     * <ul>
     * <li>Each registered data source has only one contribution to the correlated data set.</li>
     * <li>Each registered data source has at least one contribution to the correlated data set.</li>
     * </ul>
     * If the current correlated data set fails the verification check, the cause of the failure
     * is included in the result.  Otherwise no cause message is provided.
     * </p> 
     * 
     * @return  result of the verification check, containing the cause if failed
     */
    public ResultRecord verifySourceUniqueness() {
        
        // Create list of all (potentially repeating) data source names within column collection
        List<String>    lstSrcNms = this.lstMsgCols.stream().map(DataColumn::getName).toList();
        
        // Remove from the above list each unique data source name recorded so far
        boolean bolMissingSource = this.setSrcNms
                            .stream()
                            .allMatch(strNm -> lstSrcNms.remove(strNm));

        // Check for registered data sources that are missing from the data columns list
        if (!bolMissingSource)
            return ResultRecord.newFailure("Serious Error: data column list was missing at least one data source");
        
        // Check for repeated data source entries within the data columns list
        if (!lstSrcNms.isEmpty())
            return ResultRecord.newFailure("Data column list contains multiple entries for following data sources: " + lstSrcNms);
        
        return ResultRecord.SUCCESS;
    }
    
    /**
     * <p>
     * Verifies that the given set of correlated data has source data sets all of same size.
     * </p>
     * <p>
     * Note that the data sources should all have data sets of the same size.  Further, the
     * size should equal the number of samples specified in the sampling interval message.  
     * This method verifies that the current set of data inserted
     * into this instance is consistent with the above and ready for further processing.
     * </p>
     * <p>
     * The following conditions are checked:
     * <ul>
     * <li>Each data source has size equal to the number data samples in sampling message.</li>
     * </ul>
     * If the current correlated data set fails the verification check, the cause of the failure
     * is included in the result.  Otherwise no cause message is provided.
     * </p> 
     * 
     * @return  result of the verification check, containing the cause if failed
     */
    public ResultRecord verifySourceSizes() {
        
        // Each source should provide the same number of data samples
        int cntSamples = this.msgSmplClk.getNumSamples();
        
        // Get list of all data columns with different size
        List<DataColumn> lstBadCols = this.lstMsgCols
                .stream()
                .filter(msg -> msg.getDataValuesCount() != cntSamples)
                .toList();
        
        // If the list is empty we passed the test
        if (lstBadCols.isEmpty())
            return ResultRecord.SUCCESS;
        
        // Test failed - return failure with list of source names and count
        List<String> lstFailedSrcs = lstBadCols
                .stream()
                .map(msg -> msg.getName() + ": " + Integer.toString(msg.getDataValuesCount()))
                .toList();
        
        return ResultRecord.newFailure("Data column(s) had value count != " + Integer.toString(cntSamples) + ": " + lstFailedSrcs);
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
        if (ProtoTime.equals(this.msgSmplClk, msgBucket.getSamplingInterval())) {
            this.lstMsgCols.add( msgBucket.getDataColumn() );
            this.setSrcNms.add( msgBucket.getDataColumn().getName() );
            
            return true;
        }
        
        return false;
    }
    
    
    //
    // Comparable<CorrelatedQueryData> Interface
    //
    
    /**
     * <p>
     * Compares the initial timestamp attribute <code>{@link #insStart}</code>.
     * </p>
     * <p>
     * Used for ordering sets and maps of <code>CorrelatedQueryData</code> objects.
     * 
     * <h2>NOTE:</h2>
     * This does not compare for equivalence.  The compared instance may have overlapping
     * sampling domains, which should be checked (eventually).
     * </p>
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(CorrelatedQueryData o) {
        return this.insStart.compareTo(o.insStart);
    }


    
}