/*
 * Project: dp-api-common
 * File:	TestIngestDataRequestGenerator.java
 * Package: com.ospreydcs.dp.api.ingest.test
 * Type: 	TestIngestDataRequestGenerator
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
 * @since Aug 4, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionException;

import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor;
import com.ospreydcs.dp.api.model.DpSupportedType;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * Utility class for generating <code>IngestDataRequest</code> gRPC messages for unit testing.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Aug 4, 2024
 *
 */
public class TestIngestDataRequestGenerator {

    
    //
    // Class Constants
    //
    
//    /** Ingestion frame label prefix */
//    private static final String         STR_FRM_LBL_PREFIX = TestIngestDataRequestGenerator.class.getSimpleName() + "-Frame-";
//    
//    /** Ingestion frame snapshot ID prefix */
//    private static final String         STR_SNST_LBL_PREFIX = TestIngestDataRequestGenerator.class.getSimpleName() + "-Snapshot-";
    
    /** Default Data Provider UID */
    public static final ProviderUID     REC_PRVD_UID = ProviderUID.from("42", TestIngestDataRequestGenerator.class.getSimpleName(), false);
    
    
    /** Double-Valued Frames: Default prefix used for data source (PV) names */
    public static final String          STR_PV_PREFIX_DBLS = "TEST_PV_DBL_";
    
//    /** Double-Valued Frames: Default attribute count */
//    public static final int             INT_CNT_ATTRS_DBLS = 3;
//    
//    /** Double-Valued Frames: Data Platform supported type */
//    public static final DpSupportedType ENM_TYPE_DBLS = DpSupportedType.DOUBLE;

    
    /** Standard Payload: frame count */
    public static final int     CNT_FRAMES = 15;
    
    /** Standard Payload: number of table columns */
    public static final int     CNT_COLS = 1000;
    
    /** Standard Payload: number of table rows */
    public static final int     CNT_ROWS = 1000;
    
    
    
    
    //
    // Class Resources
    //
    
    /** Synchronization lock for the ingestion frame processor */
    private final static Object                     objLockProcessor = new Object();
    
    /** Ingestion frame processor used to convert <code>IngestionFrame</code> instance to <code>IngestDataRequest</code> messages */
    private final static IngestionFrameProcessor    processor = new IngestionFrameProcessor(REC_PRVD_UID);
    
    
    /**
     * <p>
     * Prevent construction of <code>TestIngestDataRequestGenerator</code> instances.
     * </p>
     */
    private TestIngestDataRequestGenerator() {
    }
    

    /**
     * <p>
     * Creates a collection of <code>IngestDataRequest</code> messages from the standard payload of <code>IngestionFrame</code>
     * instances (with all default parameters).
     * </p>
     * 
     * @return  list of new ingestion data request messages populated with artificial data
     * 
     * @throws CompletionException  the internal frame-to-message processor threw an exception (see cause)
     */
    public static List<IngestDataRequest>   createDoublesMessagesWithClock() throws CompletionException {
        return createDoublesMessagesWithClock(CNT_FRAMES, CNT_COLS, CNT_ROWS);
    }
    
    /**
     * <p>
     * Creates a collection of <code>IngestDataRequest</code> messages from a payload of <code>IngestionFrame</code>
     * instances with the given parameters.
     * </p>
     * 
     * @param cntFrames     number of ingestion frame used to create messages
     * @param cntCols       width of the ingestion frame used to create messages (number of columns)
     * @param cntRows       length of the ingestion frame used to create messages (number of rows)
     * 
     * @return  list of new ingestion data request messages populated with artificial data
     * 
     * @throws CompletionException  the internal frame-to-message processor threw an exception (see cause)
     */
    public static List<IngestDataRequest>   createDoublesMessagesWithClock(int cntFrames, int cntCols, int cntRows) throws CompletionException {
        return createDoublesMessagesWithClock(cntFrames, cntCols, cntRows, false);
    }

    /**
     * <p>
     * Creates a collection of <code>IngestDataRequest</code> messages from a payload of <code>IngestionFrame</code>
     * instances with the given parameters.
     * </p>
     * 
     * @param cntFrames     number of ingestion frame used to create messages
     * @param cntCols       width of the ingestion frame used to create messages (number of columns)
     * @param cntRows       length of the ingestion frame used to create messages (number of rows)
     * @param bolAddProps   supplies artificial frame properties if <code>true</code> (end up as message properties)
     * 
     * @return  list of new ingestion data request messages populated with artificial data
     * 
     * @throws CompletionException  the internal frame-to-message processor threw an exception (see cause)
     */
    public static List<IngestDataRequest> createDoublesMessagesWithClock(int cntFrames, int cntCols, int cntRows, boolean bolAddProps) throws CompletionException {
        return createDoublesMessagesWithClock(cntFrames, STR_PV_PREFIX_DBLS, cntCols, cntRows, bolAddProps);
    }
    
    /**
     * <p>
     * Creates a collection of <code>IngestDataRequest</code> messages from a payload of <code>IngestionFrame</code>
     * instances with the given parameters.
     * </p>
     * 
     * @param cntFrames     number of ingestion frame used to create messages
     * @param strColPref    prefix given to all ingestion frame column names
     * @param cntCols       width of the ingestion frame used to create messages (number of columns)
     * @param cntRows       length of the ingestion frame used to create messages (number of rows)
     * @param bolAddProps   supplies artificial frame properties if <code>true</code> (end up as message properties)
     * 
     * @return  list of new ingestion data request messages populated with artificial data
     * 
     * @throws CompletionException  the internal frame-to-message processor threw an exception (see cause)
     */
    public static List<IngestDataRequest>  createDoublesMessagesWithClock(int cntFrames, String strPrefix, int cntCols, int cntRows, boolean bolAddProps)  throws CompletionException {
        
        List<IngestionFrame>    lstFrames = TestIngestionFrameGenerator.createDoublePayloadWithClock(cntFrames, strPrefix, cntCols, cntRows, bolAddProps);
        
        List<IngestDataRequest> lstRqstMsgs = new LinkedList<>();
        
        synchronized (objLockProcessor) {
            
            // Activate processor if this is the last time through
            if (!processor.isSupplying())
                processor.activate();
            
            processor.submit(lstFrames);
            
            try {
                while (processor.hasNext()) {

                    IngestDataRequest   msgRqst = processor.take();
                    
                    lstRqstMsgs.add(msgRqst);
                }
                
            } catch (IllegalStateException | InterruptedException e) {
                
                throw new CompletionException("IngestionFrameProcessor take() operation threw exception.", e);
            }
        }
        
        return lstRqstMsgs;
    }
}
