/*
 * Project: dp-api-common
 * File:	UniformSamplingBlock.java
 * Package: com.ospreydcs.dp.api.query.model.time
 * Type: 	UniformSamplingBlock
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
 * @since Jan 30, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.time;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.ospreydcs.dp.api.query.model.data.SampledTimeSeries;
import com.ospreydcs.dp.api.query.model.proto.CorrelatedQueryData;

/**
 *
 * @author Christopher K. Allen
 * @since Jan 30, 2024
 *
 */
public class UniformSamplingBlock {
    
    //
    // Defining Attributes
    //
    
    /** Protobuf message describing uniform sampling duration */
    private final   com.ospreydcs.dp.grpc.v1.common.FixedIntervalTimestampSpec  msgClockParams;
    
    /** Protobuf sampled (correlated) data sets for sampling duration */
    private final   List<com.ospreydcs.dp.grpc.v1.common.DataColumn>    lstMsgDataCols;

    //
    // Constructed Attributes
    //
    
    /** The uniform clock duration for this sample block */
    private final   UniformClockDuration    clkParams;
    
    /** Set of data source names for sample block */
    private final   Set<String>             setSourceNames;
    
    /** Map of data source name to data source sample values */
    private final   Map<String, SampledTimeSeries>  mapSrcToSmpls;
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>UniformSamplingBlock</code>.
     * </p>
     *
     */
    public UniformSamplingBlock(CorrelatedQueryData refSampleBlock) {
        this.msgClockParams = refSampleBlock.getSamplingMessage();
        this.lstMsgDataCols = refSampleBlock.getAllDataMessages();
        
        this.clkParams = UniformClockDuration.from(this.msgClockParams);
        this.setSourceNames = refSampleBlock.extractDataSourceNames();
        this.mapSrcToSmpls = new HashMap<>();
    }

    //
    // Support Methods
    //
    
    private void createTimeSeries() {
        
    }
    
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
    
}
