/*
 * Project: dp-api-common
 * File:	UniformSampledBlock.java
 * Package: com.ospreydcs.dp.api.query.model.series
 * Type: 	UniformSampledBlock
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

import com.ospreydcs.dp.api.common.UniformSamplingClock;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.query.model.correl.UniformRawData;
import com.ospreydcs.dp.grpc.v1.common.SamplingClock;

/**
 * <p>
 * Super class of <code>SampledBlock</code> supported time-series data sampled with a uniform clock.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 24, 2025
 *
 */
public class UniformSampledBlock extends SampledBlock {
    
    
    //
    // Initializing Attributes
    //
    
    /** The raw data correlated to a uniform sampling clock */
    private final UniformRawData        datRaw;
    

    /**
     * <p>
     * Constructs a new <code>UniformSampledBlock</code> instance.
     * </p>
     *
     * @param datRaw
     */
    public UniformSampledBlock(UniformRawData datRaw) {
        super(this.datRaw = datRaw);
    }

    /**
     * @see com.ospreydcs.dp.api.query.model.series.SampledBlock#createTimestamps()
     */
    @Override
    protected ArrayList<Instant> createTimestamps() {
        
        SamplingClock           msgClock = this.datRaw.getSamplingClockMessage();
        UniformSamplingClock    clkTms = ProtoMsg.toUniformSamplingClock(msgClock);
        ArrayList<Instant>      vecTms = clkTms.createTimestamps();
        
        return vecTms;
    }

}
