/*
 * Project: dp-api-common
 * File:    IngestionFrameProcessorEvalConfig.java
 * Package: com.ospreydcs.dp.api.ingest.model.frame
 * Type:    IngestionFrameProcessorEvalConfig
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
 * @since Jul 31, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.frame;

import java.util.List;

import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.test.TestIngestionFrameGenerator;
import com.ospreydcs.dp.api.model.ProviderUID;

/**
 * <p>
 * Active record class for specifying test configuration for <code>IngestionFrameProcessorEvaluator</code>
 * and creating test resources.
 * </p> 
 * <p>
 * Note that configuration parameters must be set before 
 */
public final class IngestionFrameProcessorEvalConfig {
    
    //
    // Class Constants
    //
    
    /** Default Data Provider UID used for testing */
    public static final ProviderUID     REC_PRV_UID = ProviderUID.from(2);

    
    //
    // Payload Parameters
    //
    
    /** Number of (table) ingestion frames within payload */
    public Integer  cntFrames = null;
    
    /** Number of table columns */
    public Integer  cntCols = null;
    
    /** Number of table rows */
    public Integer  cntRows = null;
    
    
    //
    // IngestionFrameProcessor Parameters
    //
    
    /** The Data Provider UID */
    public ProviderUID  recProviderUid = REC_PRV_UID;
    
    /** Multi-Threading enabled */
    public Boolean      bolConcurrency = null;
    
    /** Number of processing threads */
    public Integer      cntThreads = null;
    
    /** Frame Decomposition enabled */
    public Boolean      bolFrameDecomp = null;
    
    /** Maximum ingestion frame memory allocation limit */
    public Long         szMaxFrame = null;
    
    
    //
    // Test Resource Creation
    //
    
    /**
     * <p>
     * Creates the payload for the ingestion performance test.
     * </p>
     * 
     * @return  list of ingestion frames configured according to record parameters
     * 
     * @throws IllegalStateException    payload configuration not fully specified
     */
    public List<IngestionFrame> createPayload() throws IllegalStateException {
        
        // Check payload configuration parameters
        if (this.cntFrames==null ||
            this.cntCols==null ||
            this.cntRows==null
            )
            throw new IllegalStateException("Payload parameter(s) missing.");
        
        // Create payload and return it
        List<IngestionFrame>    lstFrames = TestIngestionFrameGenerator.createDoublesPayloadWithClock(this.cntFrames, this.cntCols, this.cntRows);
        
        return lstFrames;
    }
    
    /**
     * <p>
     * Creates a new instance of <code>IngestionFrameProcessor</code>.
     * </p>
     * 
     * @return  a new processor instance configured according to the record parameters
     * 
     * @throws IllegalStateException    processor configuration not fully specified
     */
    public IngestionFrameProcessor  createProcessor() throws IllegalStateException {
        
        // Check the processor configuration parameters
        if (this.bolConcurrency==null  ||
            this.cntThreads==null ||
            this.bolFrameDecomp==null ||
            this.szMaxFrame==null
            )
            throw new IllegalStateException("IngestionFrameProcessor parameter(s) missing.");
        
        
        // Create processor and configure
        IngestionFrameProcessor processor = new IngestionFrameProcessor(this.recProviderUid);
        
        if (this.bolConcurrency) {
            processor.setConcurrency(this.cntThreads);
            
        } else {
            processor.disableConcurrency();
        }
        
        if (this.bolFrameDecomp) {
            processor.setFrameDecomposition(this.szMaxFrame);
            
        } else {
            processor.disableFrameDecomposition();
        }
        
        return processor;
    }
    
}