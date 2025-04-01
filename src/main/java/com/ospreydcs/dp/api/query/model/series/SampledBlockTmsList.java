/*
 * Project: dp-api-common
 * File:	SampledBlockTmsList.java
 * Package: com.ospreydcs.dp.api.query.model.series
 * Type: 	SampledBlockTmsList
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
 * @since Mar 24, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.series;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.query.model.correl.RawDataTmsList;
import com.ospreydcs.dp.grpc.v1.common.TimestampList;

/**
 * <p>
 * Superclass of <code>SampledBlock</code> supporting generalized sampling with timestamp lists.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Mar 24, 2025
 *
 */
public class SampledBlockTmsList extends SampledBlock {
    
    
    //
    // Initializing Attributes
    //
    
    /** The raw data correlated to a timestamp list */
    private final RawDataTmsList       datRaw;

    
    /**
     * <p>
     * Constructs a new <code>SampledBlockTmsList</code> instance.
     * </p>
     *
     * @param datRaw    raw, time-series data correlated to a timestamp list
     */
    public SampledBlockTmsList(RawDataTmsList datRaw) {
        super(this.datRaw = datRaw);
    }

    /**
     * @see com.ospreydcs.dp.api.query.model.series.SampledBlock#createTimestamps()
     */
    @Override
    protected ArrayList<Instant> createTimestamps() {
        
        TimestampList       msgTms = datRaw.getTimestampListMessage();
        List<Instant>       lstTms = ProtoMsg.toInstantList(msgTms);
        ArrayList<Instant>  vecTms = new ArrayList<>(lstTms);
        
        return vecTms;
    }

}
