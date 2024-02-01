/*
 * Project: dp-api-common
 * File:	SamplingProcess.java
 * Package: com.ospreydcs.dp.api.query.model.time
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
package com.ospreydcs.dp.api.query.model.time;

import java.time.Instant;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.ranges.RangeException;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.model.TimeInterval;
import com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData;
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
 * Note that <code>SamplingProcess</code> instances can be created from the correlated results
 * set of a Query Service data request, as obtained from 
 * <code>{@link QueryResponseCorrelator#getTargetSet()</code>.  Thus, this class can be used
 * in the reconstruction process of Data Platform Query Service data requests.
 * </p>  
 * <p>
 * <h2>TODO</h2>
 * <ul>
 * <li>Implement a possible interface <code>IDataTable</code> for table-like data retrieval.</li>
 * </ul>
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Jan 29, 2024
 */
public class SamplingProcess {

    //
    // Class Types
    //
    
    public static class TimestampBlock extends Vector<Instant> {

        private static final long serialVersionUID = 656239993644617465L;
        
        
        public TimestampBlock(Vector<Instant> vecTms) {
            super(vecTms);
        }
    }
    
//    public static record   SamplingPair(UniformClockDuration setTms, List<DataColumn> lstDataCols) {
//        
//        public static SamplingPair  of(UniformClockDuration setTms, List<DataColumn> lstDataCols) {
//            return new SamplingPair(setTms, lstDataCols);
//        }
//    };
    

    //
    // Application Resources
    //
    
    /** The Data Platform API default configuration parameter set */
    private static final DpQueryConfig  CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** Is logging active */
    public static final boolean    BOL_LOGGING = CFG_QUERY.logging.active;
    
    /** Parallelism tuning parameter - pivot to parallel processing when lstMsgDataCols size hits this limit */
    public static final int        SZ_COLLECTION_PIVOT = CFG_QUERY.concurrency.pivotSize;
    

    //
    // Class Resources
    //
    
    /** Event logger for class */
    private static final Logger LOGGER = LogManager.getLogger();
    

    //
    // Defining Attributes
    //
    
//    /** Sorted set of <code>CorrelatedQueryData</code> used to build this process */
//    private final SortedSet<CorrelatedQueryData>    setSubjectRefs;
    
    
    //
    // Attributes
    //

    /** The time domain of the sampling process */
    private final TimeInterval          ivlDomain;
    
    /** Set of unique data source names */
    private final Set<String>           setSourceNames;
    
//    /** Ordered list of <code>SamplingPair</code> instances for process build */
//    private final List<SamplingPair>    lstSmplPairs;
    
    /** Ordered vector of uniform sampling blocks comprising the sampling process */
    private final Vector<UniformSamplingBlock>  vecSmplBlock;
    
    
    //
    // Creator
    //
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>SamplingProcess</code> fully populated from argument data.
     * </p>
     * <p>
     * After construction the new instance is fully populated with sampled time-series data from
     * all data sources contained in the argument.  The argument data must be consistent for proper
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
     * @param setSubjectRefs sorted set of <code>CorrelatedQueryData</code> used to build this process
     *  
     * @throws IllegalArgumentException the argument data is corrupt: missing data column, empty data column, or duplicate data source names (see message)
     * @throws IllegalStateException    the argument data contains duplicate data source names
     * @throws RangeException           the argument data contains time domain collisions
     * @throws UnsupportedOperationException an unsupported data type was detected within the argument data
     */
    public SamplingProcess(SortedSet<CorrelatedQueryData> setSubjectRefs) 
            throws IllegalArgumentException, IllegalStateException, RangeException, UnsupportedOperationException {
        
        this.setSourceNames = this.buildSourceNameSet(setSubjectRefs);
        this.vecSmplBlock = this.buildSamplingBlocks(setSubjectRefs);
        this.ivlDomain = this.computeTimeDomain(this.vecSmplBlock);
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Extracts and returns a set of unique data source names for all data within the argument.
     * </p>
     * <p>
     * Collects all data source names within the set of correlated data and creates a set
     * of unique names which is then returned.
     * </p>
     * 
     * @return  set of all data source names within the Query Service correlted data collection 
     */
    private Set<String>        buildSourceNameSet(SortedSet<CorrelatedQueryData> setRefs) {
        Set<String> setNames = setRefs
                .stream()
                .collect(
                        TreeSet::new, 
//                        (set, r) -> set.addAll(r.extractDataSourceNames()), 
                        (set, r) -> set.addAll(r.getDataSourceNames()), 
                        TreeSet::addAll
                        );
        return setNames;
    }
    
    /**
     * @param setQueryData  set of <code>CorrelatedQueryData</code> objects ordered by start time
     * 
     * @return  vector of <code>UniformSamplingBlock</code> objects ordered according to argument
     * 
     * @throws IllegalArgumentException the argument is corrupt: missing data column, empty data column, or duplicate data source names (see message)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws RangeException           the argument contains time domain collisions
     * @throws UnsupportedOperationException an unsupported data type was detected within the argument
     */
    private Vector<UniformSamplingBlock>    buildSamplingBlocks(SortedSet<CorrelatedQueryData> setQueryData) 
            throws IllegalArgumentException, IllegalStateException, RangeException, UnsupportedOperationException {
        
        // First check all query data for time domain collisions
        CorrelatedQueryData cqdFirst = setQueryData.first();
        
        setQueryData.remove(cqdFirst);
        CorrelatedQueryData cqdPrev = cqdFirst;
        for (CorrelatedQueryData cqdCurr : setQueryData) {

            // Since collection is ordered, only need to check proximal domains
            TimeInterval    domPrev = cqdPrev.getTimeDomain();
            TimeInterval    domCurr = cqdCurr.getTimeDomain();
            
            if (domPrev.hasIntersectionClosed(domCurr)) {
                if (BOL_LOGGING)
                    LOGGER.error("{}: Collision between sampling domains {} and {}.", JavaRuntime.getCallerName(), domPrev, domCurr);
                
                throw new RangeException(RangeException.BAD_BOUNDARYPOINTS_ERR, "Collision between sampling domains " + domPrev + " and " + domCurr);
            }
                
        }
        setQueryData.add(cqdFirst);
        
        // Create the sample blocks, pivoting to concurrency by container size
        Vector<UniformSamplingBlock>    vecSmplBlocks = new Vector<>(setQueryData.size());
        
        if (setQueryData.size() > SZ_COLLECTION_PIVOT) {
            Vector<CorrelatedQueryData> vecQueryData = new Vector<>(setQueryData);
            
            IntStream.range(0, setQueryData.size())
                .parallel()
                .forEach(
                        i -> vecSmplBlocks.add(i, UniformSamplingBlock.from(vecQueryData.elementAt(i)))
                        );
            
        } else {
            setQueryData.stream()
                .forEachOrdered(
                        cqd -> vecSmplBlocks.add(UniformSamplingBlock.from(cqd))
                        );
        }
        
        return vecSmplBlocks;
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
     * @param vecSmplBlocks the entire (ordered) collection of sampling blocks withing this process
     * 
     * @return  the time domain over which this sample process has values
     */
    private TimeInterval    computeTimeDomain(Vector<UniformSamplingBlock> vecSmplBlocks) {
        UniformSamplingBlock    blkFirst = vecSmplBlocks.firstElement();
        UniformSamplingBlock    blkLast = vecSmplBlocks.lastElement();
        
        Instant insStart = blkFirst.getStartInstant();
        Instant insStop = blkLast.getTimeDomain().end();
        
        return TimeInterval.from(insStart, insStop);
    }
    
//    private List<SamplingPair>  buildSamplingPairs(SortedSet<CorrelatedQueryData> setRefs) throws IllegalStateException {
//        List<SamplingPair>  lstPairs = new ArrayList<>(setRefs.size());
//        
//        CorrelatedQueryData refFirst = setRefs.first();
//        
//        setRefs.remove(refFirst);
//        
//        UniformClockDuration  setPrev = UniformClockDuration.from(refFirst.getSamplingMessage());
//        lstPairs.add( SamplingPair.of(setPrev, refFirst.getAllDataMessages()) ) ;
//        for (CorrelatedQueryData ref : setRefs) {
//            UniformClockDuration  setCurr = UniformClockDuration.from(ref.getSamplingMessage());
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
