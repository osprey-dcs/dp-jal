/*
 * Project: dp-api-common
 * File:    CorrelatedQueryData.java
 * Package: com.ospreydcs.dp.api.query.model.grpc
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
package com.ospreydcs.dp.api.query.model.grpc;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.ospreydcs.dp.api.common.ResultRecord;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.grpc.util.ProtoTime;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.SamplingClock;
import com.ospreydcs.dp.grpc.v1.common.Timestamp;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

/**
 * <p>
 * Class used for time correlation of Query Service data requests using regular sampling (i.e., sampling clocks).
 * </p>
 * <p>
 * Class instances are used to correlate all time-series data within a data request response 
 * (i.e., data contained in Protobuf <code>{@link DataColumn}</code> messages).  In its
 * intended operation, each <code>CorrelatedQueryData</code> instance maintains a reference to 
 * ONE sampling clock for the entire results set.  Note that sampling clocks are represented
 * by the Protobuf message <code>{@link FixedIntervalTimestampSpec}</code>, which has finite
 * duration.  All time-series data within the results set for that sampling clock and duration
 * are then referenced within a <code>CorrelatedQueryData</code> instance.  
 * </p>
 * <p>
 * Instances of this class are generated and maintained by the class 
 * <code>{@link QueryDataCorrelator}</code>.  The latter class performs the correlation
 * operation on the stream of <code>{@link QueryResponse}</code> Protobuf messages produced
 * by a Query Service data request. 
 * Each instance of <code>CorrelatedQueryData</code>, in its final form, should contain a uniform 
 * sampling interval and all time-series data from data sources that were sampled during that 
 * interval.
 * </p>
 * <p>
 * <h2>Ordering</code>
 * The class implements the <code>{@link Comparable}</code> interface for ordering by sample 
 * clock initial start time instant.
 * That is, within collections of <code>CorrelatedQueryData</code> objects, instances are ordered 
 * according to the sampling start time.  This is not an equivalence operation; it is possible 
 * that sampling intervals can overlap and this condition should be checked.
 * </p>
 * <p>
 * The enclosed class <code>{@link StartTimeComparator}</code>, implementing the 
 * <code>{@link Comparator}</code> interface, is also provided for ordering by sampling clock
 * start times. It is equivalent in function the <code>{@link Comparable}</code> interface.  
 * It is provided for explicit comparison in Java container construction.
 * </p> 
 * <p>
 * <h2>WARNINGS:</h2>
 * <ul>
 * <li>
 * It is assumed that the Data Platform archive is consistent!  If correlated 
 * data was archived simultaneously with different sample clocks unpredictable 
 * behavior in reconstruction is possible.
 * </li>
 * <br/>
 * <li>
 * This class is only applicable to Query Service data responses whose timestamps are defined by a 
 * sampling clock.  Irregular sampling described with timestamp lists is NOT processed with 
 * <code>CorrelatedQueryData</code> instances. 
 * </li>
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Jan 11, 2024
 *
 * @see Comparable
 * @see QueryDataCorrelator
 */
public class CorrelatedQueryData implements Comparable<CorrelatedQueryData> {
    
    
    //
    //  Class Types
    //
    
    /**
     * <p>
     * <code>{@link Comparator}</code> implementation for ordered comparison of two 
     * <code>CorrelatedQueryData</code> instances.
     * </p>
     * <p>
     * The comparison here is performed with the start times of the sampling intervals 
     * represented by <code>{@link CorrelatedQueryData}</code> time <code>{@link Instant}</code>
     * returned from the <code>{@link CorrelatedQueryData#getStartInstant()</code> method.
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
        // Comparator<CorrelatedQueryData> Interface
        //
        
        /**
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
    // Defining Attributes 
    //
    
    /** The uniform sampling interval Protobuf message - the correlation subject */
    private final SamplingClock     msgSmplClk; 
    
    /** List of all data column Protobuf messages correlated within sampling interval - correlated objects */
    private final List<DataColumn>  lstMsgCols = new LinkedList<>();
    
    
    //
    // Resources
    //
    
    /** The initial sampling time instant - taken from <code>msgSmpIval</code> */
    private final Instant                       insStart;
    
    /** The time domain of contained time series data - computed from <code>msgSmpIval</code> */
    private final TimeInterval                  domTimeRange;
    
    /** Set of all unique data source names active within sampling interval - taken from incoming data messages */
    private final Set<String>                   setSrcNms = new TreeSet<>();
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new, initialized instance of <code>CorrelatedQueryData</code>.
     * </p>
     * <p>
     * Initializes the returned <code>CorrelatedQueryData</code> instance with the data
     * taken from the argument.  Specifically,
     * <ul>
     * <li>The immutable sampling clock message is set.</li>
     * <li>The collection of data column messages is initialized, contains the argument data column.</li>
     * <li>All resources are initialized.</li>
     * </ul>
     * </p> 
     * 
     * @param msgBucket source of initialization data
     * 
     * @return  new <code>CorrelatedQueryData</code> instance initialized with data from the argument
     * 
     * @throws IllegalArgumentException     the <code>DataBucket</code> message did not contain a sampling clock
     */
    public static CorrelatedQueryData   from(QueryDataResponse.QueryData.DataBucket msgBucket) 
            throws IllegalArgumentException {
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
     * 
     * @throws IllegalArgumentException     the <code>DataBucket</code> message did not contain a sampling clock
     */
    public CorrelatedQueryData(QueryDataResponse.QueryData.DataBucket msgBucket) {
        
        // Check argument
        if (!msgBucket.getDataTimestamps().hasSamplingClock())
            throw new IllegalArgumentException(JavaRuntime.getQualifiedCallerNameSimple() 
                    + ": Argument does not contain a sampling clock.");
        
        // Extract sampling clock
        this.msgSmplClk = msgBucket.getDataTimestamps().getSamplingClock();
        
//        Timestamp   msgTmsStart = msgBucket.getSamplingInterval().getStartTime();
        Timestamp   msgTmsStart = this.msgSmplClk.getStartTime();
        DataColumn  msgDataCol = msgBucket.getDataColumn(); 
    
        this.insStart = ProtoMsg.toInstant(msgTmsStart);
        this.setSrcNms.add(msgDataCol.getName());
        this.lstMsgCols.add(msgDataCol);
        
        // Compute time domain
        int     cntSamples = this.msgSmplClk.getCount();
        long    lngPeriodNs = this.msgSmplClk.getPeriodNanos();
        Duration    durPeriod = Duration.ofNanos(lngPeriodNs);
        Duration    durDomain = durPeriod.multipliedBy(cntSamples - 1);
        Instant     insStop = this.insStart.plus(durDomain);
        
        this.domTimeRange = TimeInterval.from(this.insStart, insStop);
    }
    
    
    // 
    // Attribute Getters
    //
    
    /**
     * <p>
     * Returns the number of samples for each data column as specified in the sampling interval message.
     * </p>
     * <p>
     * Obtains the returned value from the Protobuf message describing the sampling clock.
     * Specifically, returns the field value containing the number of samples.
     * All correlated data columns <em>should</em> have the same number of data values (i.e.,
     * the returned value) if the Data Platform archive is consistent. 
     * </p>
     * 
     * @return  the size of each correlated data column within the correlated time-series collection
     */
    public final int    getSampleCount() {
        return this.msgSmplClk.getCount();
    }
    
    /**
     * <p>
     * Returns the starting time instant of the sampling interval.
     * </p>
     * <p>
     * All time-series data within this correlated data set should have the same start
     * time (i.e., the returned value) as specified by the sampling clock Protobuf message.
     * </p>
     * 
     * @return sampling interval start instant
     */
    public final Instant getStartInstant() {
        return this.insStart;
    }
    
    /**
     * <p>
     * Returns the sampling period of the sampling clock in nanoseconds.
     * </p>
     * <p>
     * The returned value is taken directly from the associated sampling clock Protobuf message.
     * All time-series data contained in this correlated data set should have the 
     * same sampling period. 
     * </p>
     * 
     * @return  the sampling period (in nanoseconds) of the associated sampling clock message.
     */
    public final long getSamplingPeriod() {
        return this.msgSmplClk.getPeriodNanos();
    }
    
    /**
     * <p>
     * Returns the time domain of the sampling interval.
     * </p>
     * <p>
     * The time domain is the <em>smallest</em>, connected time interval that contains
     * all sample points.  Thus, the returned interval DOES NOT include the last sample
     * period.
     * </p>
     * <p>
     * The returned interval left end point is the start time of the sampling interval 
     * returned by <code>{@link #getStartInstant()}</code>.  The right end point is the
     * last timestamp of the sampled data within the query data.  Specifically, it 
     * DOES NOT include the sample period duration following the last timestamp.
     * Specifically, the returned time interval <i>I</i> is given by
     * <pre>
     *   <i>I</i> = [<i>t</i><sub>0</sub>, <i>t</i><sub>0</sub> + (<i>N</i> - 1)<i>T</i>] 
     * </pre>
     * where <i>t</i><sub>0</sub> is the clock start time 
     * (returned by <code>{@link #getStartInstant()}</code>), <i>N</i> is the number of 
     * samples (returned by <code>{@link #getSampleCount()}</code>), and <i>T</i> is the
     * sampling period (returned by <code>{@link #getSamplingPeriod()}</code>).
     * </p>
     * 
     * @return  the time domain of correlated query data [first timestamp, last timestamp]
     */
    public final TimeInterval getTimeDomain() {
        return this.domTimeRange;
    }

    /**
     * <p>
     * Returns the number of unique data source names within the current collection of data messages.
     * </p>
     * <p>
     * <code>CorrelatedQueryData</code> objects maintain a set of unique data source names
     * for all Query Service response data successfully inserted into the correlated collection.
     * All data sources within the collection should be unique for a consistent data archive. 
     * </p>
     * 
     * @return  number of unique data source names encountered so far
     * 
     * @see #getSourceNames()
     */
    public final int    getSourceCount() {
        return this.setSrcNms.size();
    }
    
    /**
     * <p>
     * Returns the set of unique names for all the data sources that are active within the
     * associated sampling interval.
     * </p>
     * <p>
     * <code>CorrelatedQueryData</code> objects maintain a set of unique data source names
     * for all Query Service response data successfully inserted into the correlated collection.
     * All data sources within the collection should be unique for a consistent data archive. 
     * </p>
     *  
     * @return  set of unique data source names for current collection of correlated data 
     */
    public final Set<String>    getSourceNames() {
        return this.setSrcNms;
    }
    
    /**
     * <p>
     * Returns the Protobuf message describing the sampling clock for all correlated data.
     * </p>
     * <p>
     * The returned message described the sampling clock to which all data message are
     * correlated.  That is, the returned message is applicable to all data returned
     * by <code>{@link #getAllDataMessages()}</code>.
     * 
     * @return Protobuf message describing the time-series sampling clock
     * 
     * @see #getAllDataMessages()
     */
    public final SamplingClock getSamplingMessage() {
        return this.msgSmplClk;
    }


    /**
     * <p>
     * Returns the entire collection of correlated Protobuf data column message associated 
     * with this sampling interval.
     * </p>
     * <p>
     * The returned collection of data messages are all sampled according to the sampling
     * clock described by <code>{@link #getSamplingMessage()}</code>.  It represents the
     * correlated data set of this <code>CorrelatedQueryData</code> instance.
     * </p>
     * 
     * @return all correlated data currently associated with the sampling interval 
     */
    public final List<DataColumn> getAllDataMessages() {
        return this.lstMsgCols;
    }
    
    /**
     * <p>
     * Extracts and returns all unique data source names from the contained <code>DataColumn</code> 
     * Protobuf messages.
     * </p>
     * <p>
     * <h2>Deprecated</h2>
     * This method is expensive and has been replaced by <code>{@link #getSourceNames()}</code>.
     * The implementation now maintains the collection <code>{@link #setSrcNms}</code> of
     * unique names, rather than compute them on demand.
     * </p>
     * 
     * @return set of all data source names within the referenced data
     * 
     * @deprecated replaced by <code>{@link #getSourceNames()}</code>
     */
    @Deprecated(since="Jan 31, 2024", forRemoval=true)
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
     * @return  result of the verification check, containing the cause if failure
     */
    public ResultRecord verifySourceUniqueness() {
        
        // Create list of all (potentially repeating) data source names within column collection
        List<String>    lstSrcNms = this.lstMsgCols.stream().map(DataColumn::getName).toList();
        List<String>    vecSrcNms = new ArrayList<>(lstSrcNms);
        
        // Remove from the above vector - each unique data source name recorded so far
        boolean bolAllRemoved = this.setSrcNms
                            .stream()
                            .allMatch(strNm -> vecSrcNms.remove(strNm));

        // Check for registered data sources that are missing from the data columns list
        if (!bolAllRemoved)
            return ResultRecord.newFailure("Serious Error: data column list was missing at least one data source");
        
        // Check for repeated data source entries within the data columns list
        if (!vecSrcNms.isEmpty())
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
        int cntSamples = this.msgSmplClk.getCount();
        
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
     * Adds the <code>DataColumn</code> message within the argument to the collection of 
     * correlated data ONLY IF sampling intervals are equivalent AND the data column is not 
     * already referenced.
     * </p>
     * <p>
     * <h2>Operation</h2>
     * This is the primary processing operation for the <code>CorrelatedQueryData</code>
     * class.  It performs the following operations:
     * <ol>
     * <li>
     * Checks for an equivalent sampling interval within the argument and, if so, continues
     * to the next step.  
     * If the sampling intervals are NOT equivalent then nothing is done and a value 
     * <code>false</code> is returned.
     * </li>
     * <br/>
     * <li>
     * If the argument has an equivalent sampling interval, the argument data column is then
     * checked to see if already exists within the referenced collection.  If it is already
     * present in the collection nothing is done and the method returns <code>false</code>.
     * </li>
     * </ol>
     * </p>
     * <p>
     * <h2>Concurrency</h2>
     * This operation is <em>almost</em> atomic and can be performed concurrently for multiple
     * <code>DataBucket</code> messages on separate execution threads.  Doing so will provide
     * consistent results so as NO duplicate data sources within the same sampling clock are
     * processed.  
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The correlated data set can only have ONE data source entry.  Attempting to insert
     * a duplicate time-series for the SAME sampling clock indicates a serious error with the 
     * Query Service data request response, or the data archive itself.  Especially egregious 
     * are duplicate data source with different heterogeneous types.  
     * However, this error is not caught here; only the value <code>false</code> is returned.
     * </li>
     * <br/>
     * <li>
     * If duplicates are present, the data source entry encountered FIRST will be entered into 
     * the correlated data set.
     * </li>
     * </p>
     * 
     * @param msgBucket Protobuf message containing sampling interval and column data
     * 
     * @return      <code>true</code> only if argument data was successfully added to this reference,
     *              <code>false</code> otherwise (nothing done)
     *              
     * @throws IllegalArgumentException     the <code>DataBucket</code> message did not contain a sampling clock
     */
    public boolean insertBucketData(QueryDataResponse.QueryData.DataBucket msgBucket) {
        
        // Check argument
        if (!msgBucket.getDataTimestamps().hasSamplingClock())
            throw new IllegalArgumentException(JavaRuntime.getQualifiedCallerNameSimple() 
                    + ": Argument does not contain a sampling clock.");
            
        SamplingClock   msgClock = msgBucket.getDataTimestamps().getSamplingClock();
        DataColumn      msgCol = msgBucket.getDataColumn();
        
        // Check if list addition is possible 
        // - must have same sampling interval
        if (!ProtoTime.equals(this.msgSmplClk, msgClock)) 
            return false;

        // - must not be already present
        if (this.lstMsgCols.contains(msgCol))
            return false;
        
        // Add the data column and record its data source 
        this.lstMsgCols.add( msgCol );
        this.setSrcNms.add( msgCol.getName() );
        
        return true;
    }
    
    
    //
    // Comparable<CorrelatedQueryData> Interface
    //
    
    /**
     * <p>
     * Compares the initial timestamp attribute <code>{@link #insStart}</code>.
     * </p>
     * <p>
     * Used for ordering Java sets and maps of <code>CorrelatedQueryData</code> objects.
     * </p>
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