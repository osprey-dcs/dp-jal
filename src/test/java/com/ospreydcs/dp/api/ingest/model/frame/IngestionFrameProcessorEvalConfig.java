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

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.common.ProviderUID;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.test.TestIngestionFrameGenerator;

/**
 * <p>
 * Active record class for specifying test configuration for <code>IngestionFrameProcessorEvaluatorDep</code>
 * and creating test resources.
 * </p> 
 * <p>
 * Note that configuration parameters must be set before 
 */
public final class IngestionFrameProcessorEvalConfig {
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new, initialized, instance of <code>IngestionFrameProcessorEvalConfig</code> configured with
     * the given parameters.
     * </p>
     * <p>
     * The Data Provider UID defaults to the class constant <code>{@link #REC_PRV_UID}</code>.
     * </p>
     *
     * @param cntFrames         number of ingestion frames in the payload
     * @param cntCols           number of columns within each ingestion frame
     * @param cntRows           number of rows within each ingestion frame
     * @param recProviderUid    Data Provider UID for request message creation
     * @param bolConcurrency    multi-threaded processing enabled flag
     * @param cntThreads        number of concurrent threads (each for decomposition and conversion)
     * @param bolFrameDecomp    automatic frame decomposition enabled flag
     * @param szMaxFrame        maximum allowable allocation size of ingestion frame (in bytes)
     * @param lngTimeout        timeout limit for polling operations
     * @param tuTimeout         timeout units for polling operations  
     *   
     * @return  a new processor evaluation instance ready for resource creation
     * 
     * @see #createPayload()
     * @see #createProcessor()
     */
    public static IngestionFrameProcessorEvalConfig from(
            int cntFrames, int cntCols, int cntRows, 
            boolean bolConcurrency, int cntThreads, 
            boolean bolFrameDecomp, long szMaxFrame,
            long lngTimeout, TimeUnit tuTimeout
            )
    {
        return new IngestionFrameProcessorEvalConfig(cntFrames, cntCols, cntRows, 
                bolConcurrency, cntThreads, bolFrameDecomp, szMaxFrame,
                lngTimeout, tuTimeout);
    }
    
    /**
     * <p>
     * Creates a new, initialized, instance of <code>IngestionFrameProcessorEvalConfig</code> configured with
     * the given parameters.
     * </p>
     *
     * @param cntFrames         number of ingestion frames in the payload
     * @param cntCols           number of columns within each ingestion frame
     * @param cntRows           number of rows within each ingestion frame
     * @param recProviderUid    Data Provider UID for request message creation
     * @param bolConcurrency    multi-threaded processing enabled flag
     * @param cntThreads        number of concurrent threads (each for decomposition and conversion)
     * @param bolFrameDecomp    automatic frame decomposition enabled flag
     * @param szMaxFrame        maximum allowable allocation size of ingestion frame (in bytes)
     * @param lngTimeout        timeout limit for polling operations
     * @param tuTimeout         timeout units for polling operations  
     *   
     * @return  a new processor evaluation instance ready for resource creation
     * 
     * @see #createPayload()
     * @see #createProcessor()
     */
    public static IngestionFrameProcessorEvalConfig from(
            int cntFrames, int cntCols, int cntRows, 
            ProviderUID recProviderUid, 
            boolean bolConcurrency, int cntThreads, 
            boolean bolFrameDecomp, long szMaxFrame,
            long lngTimeout, TimeUnit tuTimeout
            )
    {
        return new IngestionFrameProcessorEvalConfig(cntFrames, cntCols, cntRows, 
                recProviderUid, bolConcurrency, cntThreads, bolFrameDecomp, szMaxFrame,
                lngTimeout, tuTimeout);
    }
    
    
    //
    // Class Constants
    //
    
    /** Common configuration name prefix - used in {@link #createConfigName()} */
    public static final String          STR_CONFIG_PREFIX = "IngestionFrameProcessorEvaluatorConfig-";
    
    
    /** Common output file name prefix - used in {@link #createOutputFileName()} */
    public static final String          STR_FILENAME_PREFIX = "TestResults-";
    
    /** Common output file name suffix - used in {@link #createOutputFileName()} */
    public static final String          STR_FILENAME_SUFFIX = ".txt";
    
    
    /** Default Data Provider UID used for testing */
    public static final ProviderUID     REC_PRV_UID = ProviderUID.from("42", IngestionFrameProcessorEvalConfig.class.getSimpleName(), false);

    
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
    // Polling Parameters
    //
    
    /** Polling timeout limit */
    public Long         lngTimeout = null;
    
    /** Polling timeout units */
    public TimeUnit     tuTimeout = null;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionFrameProcessorEvalConfig</code>.
     * </p>
     * <p>
     * Default constructor creating uninitialized instance.  Must set configuration parameters
     * directly or using <code>{@link #setPayloadConfig(int, int, int)}</code> and
     * <code>{@link #setProcessorConfig(ProviderUID, boolean, int, boolean, long)}</code>.
     * </p>
     *
     * @see #setPayloadConfig(int, int, int)
     * @see #setProcessorConfig(boolean, int, boolean, long)
     * @see #setProcessorConfig(ProviderUID, boolean, int, boolean, long)
     * @see #setPollingConfig(long, TimeUnit)
     */
    public IngestionFrameProcessorEvalConfig() {
    }
    
    /**
     * <p>
     * Constructs a new, initialized, instance of <code>IngestionFrameProcessorEvalConfig</code>.
     * </p>
     * <p>
     * The Data Provider UID defaults to the class constant <code>{@link #REC_PRV_UID}</code>.
     * </p>
     *
     * @param cntFrames         number of ingestion frames in the payload
     * @param cntCols           number of columns within each ingestion frame
     * @param cntRows           number of rows within each ingestion frame
     * @param recProviderUid    Data Provider UID for request message creation
     * @param bolConcurrency    multi-threaded processing enabled flag
     * @param cntThreads        number of concurrent threads (each for decomposition and conversion)
     * @param bolFrameDecomp    automatic frame decomposition enabled flag
     * @param szMaxFrame        maximum allowable allocation size of ingestion frame (in bytes)  
     * @param lngTimeout        timeout limit for polling operations
     * @param tuTimeout         timeout units for polling operations  
     */
    public IngestionFrameProcessorEvalConfig(int cntFrames, int cntCols, int cntRows, 
            boolean bolConcurrency, int cntThreads, 
            boolean bolFrameDecomp, long szMaxFrame,
            long lngTimeout, TimeUnit tuTimeout
            ) 
    {
        this(cntFrames, cntCols, cntRows, REC_PRV_UID, bolConcurrency, cntThreads, bolFrameDecomp, szMaxFrame, lngTimeout, tuTimeout);
    }
    
    /**
     * <p>
     * Constructs a new, initialized, instance of <code>IngestionFrameProcessorEvalConfig</code>.
     * </p>
     *
     * @param cntFrames         number of ingestion frames in the payload
     * @param cntCols           number of columns within each ingestion frame
     * @param cntRows           number of rows within each ingestion frame
     * @param recProviderUid    Data Provider UID for request message creation
     * @param bolConcurrency    multi-threaded processing enabled flag
     * @param cntThreads        number of concurrent threads (each for decomposition and conversion)
     * @param bolFrameDecomp    automatic frame decomposition enabled flag
     * @param szMaxFrame        maximum allowable allocation size of ingestion frame (in bytes)
     * @param lngTimeout        timeout limit for polling operations
     * @param tuTimeout         timeout units for polling operations  
     */
    public IngestionFrameProcessorEvalConfig(int cntFrames, int cntCols, int cntRows, 
            ProviderUID recProviderUid, 
            boolean bolConcurrency, int cntThreads, 
            boolean bolFrameDecomp, long szMaxFrame,
            long lngTimeout, TimeUnit tuTimeout
            ) 
    {
        this.cntFrames = cntFrames;
        this.cntCols = cntCols;
        this.cntRows = cntRows;
        
        this.recProviderUid = recProviderUid;
        this.bolConcurrency = bolConcurrency;
        this.cntThreads = cntThreads;
        this.bolFrameDecomp = bolFrameDecomp;
        this.szMaxFrame = szMaxFrame;
        
        this.lngTimeout = lngTimeout;
        this.tuTimeout = tuTimeout;
    }
    

    //
    // Configuration
    //
    
    /**
     * <p>
     * Configures the payload for the <code>IngestionFrameProcessorEvaluatorDep</code> test.
     * </p>
     *  
     * @param cntFrames number of ingestion frames in the payload
     * @param cntCols   number of columns within each ingestion frame
     * @param cntRows   number of rows within each ingestion frame
     */
    public void setPayloadConfig(int cntFrames, int cntCols, int cntRows) {
        this.cntFrames = cntFrames;
        this.cntCols = cntCols;
        this.cntRows = cntRows;
    }
    
    /**
     * <p>
     * Configures the <code>IngestionFrameProcessor</code> for the <code>IngestionFrameProcessorEvalutaor</code> test.
     * </p>
     * <p>
     * The Data Provider UID defaults to the class constant <code>{@link #REC_PRV_UID}</code>.
     * </p>
     * 
     * @param bolConcurrency    multi-threaded processing enabled flag
     * @param cntThreads        number of concurrent threads (each for decomposition and conversion)
     * @param bolFrameDecomp    automatic frame decomposition enabled flag
     * @param szMaxFrame        maximum allowable allocation size of ingestion frame (in bytes)  
     */
    public void setProcessorConfig(boolean bolConcurrency, int cntThreads, boolean bolFrameDecomp, long szMaxFrame) {
        this.setProcessorConfig(REC_PRV_UID, bolConcurrency, cntThreads, bolFrameDecomp, szMaxFrame);
    }
    
    /**
     * <p>
     * Configures the <code>IngestionFrameProcessor</code> for the <code>IngestionFrameProcessorEvalutaor</code> test.
     * </p>
     * 
     * @param recProviderUid    Data Provider UID for request message creation
     * @param bolConcurrency    multi-threaded processing enabled flag
     * @param cntThreads        number of concurrent threads (each for decomposition and conversion)
     * @param bolFrameDecomp    automatic frame decomposition enabled flag
     * @param szMaxFrame        maximum allowable allocation size of ingestion frame (in bytes)  
     */
    public void setProcessorConfig(ProviderUID recProviderUid, boolean bolConcurrency, int cntThreads, boolean bolFrameDecomp, long szMaxFrame) {
        this.recProviderUid = recProviderUid;
        this.bolConcurrency = bolConcurrency;
        this.cntThreads = cntThreads;
        this.bolFrameDecomp = bolFrameDecomp;
        this.szMaxFrame = szMaxFrame;
    }
    
    /**
     * <p>
     * Sets the configuration polling parameters for <code>IngestionFrameProcessor</code> message recovery.
     * </p>
     * 
     * @param lngTimeout        timeout limit for polling operations
     * @param tuTimeout         timeout units for polling operations  
     */
    public void setPollingConfig(long lngTimeout, TimeUnit tuTimeout) {
        this.lngTimeout = lngTimeout;
        this.tuTimeout = tuTimeout;
    }
    
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
    
    /**
     * <p>
     * Creates a string name for the configuration based upon the configuration parameters.
     * </p>
     * <p>
     * This method is intended for use in creating headers for the evaluation results outputs
     * </p>
     * 
     * @return  a descriptive name for the configuration 
     * 
     * @thows   IllegalStateException   configuration undefined - configuration parameter(s) not set 
     */
    public String   createConfigName() throws IllegalStateException {
        
        // Get the configuration description
        String  strDscr = this.printConfigDescrip();
        
        // Create a string buffer and populate it
        StringBuilder   buf = new StringBuilder(STR_CONFIG_PREFIX);
        
        buf.append(strDscr);
        
        return buf.toString();
    }
    
    /**
     * <p>
     * Creates a descriptive file name for the test output.
     * </p>
     * <p>
     * The returned string contains the defining parameters for the test configuration
     * as well as the date and time of invocation.
     * </p>
     * 
     * @return  a unique, description file name for the test results output
     * 
     * @thows   IllegalStateException   configuration undefined - configuration parameter(s) not set 
     */
    public String   createOutputFileName() throws IllegalStateException {

        // Get the current time and format it into a string
        Instant insNow = Instant.now();
        String  strTime = insNow.toString();
        
        // Get the configuration description
        String  strDscr = this.printConfigDescrip();
        
        // Create a string buffer and populate it
        StringBuilder   buf = new StringBuilder(STR_FILENAME_PREFIX);

        buf.append(strDscr);
//        buf.append(this.cntFrames + "frames-");
//        if (this.bolConcurrency)
//            buf.append(this.cntThreads + "threads-");
//        else
//            buf.append("snglthread-");
//        if (this.bolFrameDecomp)
//            buf.append(this.szMaxFrame + "maxalloc-");
//        else
//            buf.append("nodecomp-");
        
        buf.append(strTime);
        buf.append(STR_FILENAME_SUFFIX);
        
        return buf.toString();
    }
    
    
    // 
    // Support Methods
    //
    
    /**
     * <p>
     * Create a brief string description of the configuration.
     * </p>
     * 
     * @return  a string description of configuration including some parameter values
     * 
     * @thows   IllegalStateException   configuration undefined - configuration parameter(s) not set 
     */
    private String  printConfigDescrip() throws IllegalStateException {
        
        // Check parameters
        if (this.bolConcurrency==null ||
            this.cntThreads==null ||
            this.bolFrameDecomp==null ||
            this.szMaxFrame==null
            )
            throw new IllegalStateException("Undefined Configuration: Configuration parameter(s) not specified.");
            
        
        // Create a string buffer and populate it
        StringBuilder   buf = new StringBuilder();
        
        buf.append(this.cntFrames + "frames-");
        if (this.bolConcurrency)
            buf.append(this.cntThreads + "threads-");
        else
            buf.append("snglthread-");
        if (this.bolFrameDecomp)
            buf.append(this.szMaxFrame + "maxalloc-");
        else
            buf.append("nodecomp-");
        
        return buf.toString();
    }
    
}