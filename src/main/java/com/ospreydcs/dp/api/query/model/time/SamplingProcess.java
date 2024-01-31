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
 * - None
 */
package com.ospreydcs.dp.api.query.model.time;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;

/**
 *
 * @author Christopher K. Allen
 * @since Jan 29, 2024
 *
 */
public class SamplingProcess {

    //
    // Class Types
    //
    
    public static record   SamplingPair(UniformClockDuration setTms, List<DataColumn> lstDataCols) {
        
        public static SamplingPair  of(UniformClockDuration setTms, List<DataColumn> lstDataCols) {
            return new SamplingPair(setTms, lstDataCols);
        }
    };
    
    //
    // Defining Attributes
    //
    
    /** Sorted set of <code>CorrelatedQueryData</code> used to build this process */
    private final SortedSet<CorrelatedQueryData>    setSubjectRefs;
    
    
    //
    // Products
    //
    
    /** Set of unique data source names */
    private final Set<String>           setSrcNms;
    
    /** Ordered list of <code>SamplingPair</code> instances for process build */
    private final List<SamplingPair>    lstSmplPairs;
    
    /**
     * <p>
     * Constructs a new instance of <code>SamplingProcess</code>.
     * </p>
     *
     * @param setSubjectRefs sorted set of <code>CorrelatedQueryData</code> used to build this process 
     */
    public SamplingProcess(SortedSet<CorrelatedQueryData> setSubjectRefs) {
        this.setSubjectRefs = setSubjectRefs;
        
        this.setSrcNms = this.buildSourceNameSet(setSubjectRefs);
        this.lstSmplPairs = this.buildSamplingPairs(setSubjectRefs);
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Extracts and returns a set of unique data source names for all data within target set.
     * </p>
     * <p>
     * Collects all data source names within the target set of sampling interval references and
     * adds them to the returned set of unique names.
     * </p>
     * 
     * @return  set of all data source names within the Query Service response collection 
     */
    private Set<String>        buildSourceNameSet(SortedSet<CorrelatedQueryData> setRefs) {
        Set<String> setNames = setRefs
                .stream()
                .collect(
                        TreeSet::new, 
                        (set, r) -> set.addAll(r.extractDataSourceNames()), 
                        TreeSet::addAll
                        );
        return setNames;
    }
    
    private List<SamplingPair>  buildSamplingPairs(SortedSet<CorrelatedQueryData> setRefs) throws IllegalStateException {
        List<SamplingPair>  lstPairs = new ArrayList<>(setRefs.size());
        
        CorrelatedQueryData refFirst = setRefs.first();
        
        setRefs.remove(refFirst);
        
        UniformClockDuration  setPrev = UniformClockDuration.from(refFirst.getSamplingMessage());
        lstPairs.add( SamplingPair.of(setPrev, refFirst.getAllDataMessages()) ) ;
        for (CorrelatedQueryData ref : setRefs) {
            UniformClockDuration  setCurr = UniformClockDuration.from(ref.getSamplingMessage());
            
            if (setCurr.hasIntersection(setPrev))
                throw new IllegalStateException("Collision between internal sampling domains.");
            
            lstPairs.add( SamplingPair.of(setCurr, ref.getAllDataMessages()) );
        }
        
        setRefs.add(refFirst);
        
        return lstPairs;
    }
}
