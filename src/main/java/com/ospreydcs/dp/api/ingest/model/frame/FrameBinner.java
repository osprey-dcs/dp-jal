/*
 * Project: dp-api-common
 * File:	FrameBinner.java
 * Package: com.ospreydcs.dp.api.ingest.model.frame
 * Type: 	FrameBinner
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
 * @since Apr 8, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.frame;

import java.nio.BufferUnderflowException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionException;

import com.ospreydcs.dp.api.ingest.model.IngestionFrame;

/**
 * <p>
 * Processor class used for binning operations on <code>IngestionFrame</code> instances.
 * </p>
 * <p>
 * Intended for use whenever gRPC ingestion messages (i.e., <code>IngestDataRequest</code> instances) 
 * are restricted in size, for example, by the gRPC message size limitation. 
 * Then ingestion frames exceeding this limitation must be divided
 * into smaller frames meeting the size requirements.
 * This class provides common operations for "binning" one large ingestion frame into
 * multiple smaller frames each meeting the given size requirement.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Apr 8, 2024
 *
 */
public class FrameBinner {

    
    //
    // Class Types
    //
    
    /**
     * <p>
     * Class for computing binning parameters for an ingestion frame.
     * </p>
     *  
     * @author Christopher K. Allen
     * @since Apr 8, 2024
     *
     */
    private static class BinParameters {
        
        //
        // Defining Attributes
        //
        
        /** Target ingestion frame - i.e., frame to be binned */
        private final IngestionFrame frame; 
        
        /** Size (in bytes) of target ingestion frame */
        private final long           lngBinSizeMax;
        
        //
        // Derived Parameters
        //
        
        /** The size (in bytes) of the target ingestion frame */
        private final long  lngFrameSize;
        
        /** The required number of binned frames to meet bin size requirements */
        private final int   intBinCount; 
        
        /** The maximum size of each binned ingestion frame */
        private final int   intBinSize;
        
        
        /**
         * <p>
         * Creates a new instance of <code>BinParameters</code> ready for query.
         * </p>
         *
         * @param frame         the target ingestion frame
         * @param lngBinSizeMax maximum size of ingestion frame allowable (in bytes)
         *  
         * @return  new instance of bin parameters 
         */
        public static BinParameters from(IngestionFrame frame, long lngBinSizeMax) {
            return new BinParameters(frame, lngBinSizeMax);
        }
        
        /**
         * <p>
         * Constructs a new instance of <code>BinParameters</code> ready for query.
         * </p>
         *
         * @param frame         the target ingestion frame
         * @param lngBinSizeMax maximum size of ingestion frame allowable (in bytes) 
         */
        public BinParameters(IngestionFrame frame, long lngBinSizeMax) {
            this.frame = frame;
            this.lngBinSizeMax = lngBinSizeMax;

            this.lngFrameSize = frame.allocationSizeFrame();
            this.intBinCount = this.computeBinCount();
            this.intBinSize = this.computeBinSize();
        }

        /**
         * @return <code>true</code> if the target frame requires binning to meet size requirements
         *         <code>false</code> otherwise
         */
        public boolean requiresBinning() {
            return this.lngFrameSize > this.lngBinSizeMax;
        }
        /**
         * @return the number of binned ingestion frames required to meet maximum bin size
         */
        public int getBinCount() {
            return this.intBinCount;
        }
        
        /**
         * @return the minimum size required for each binned ingestion frame
         */
        public int getBinSize() {
            return this.intBinSize;
        }
        
        // 
        // Support Methods
        //
        
        /**
         * Computes the number of bins required.
         * 
         * @return the number of binned ingestion frames required to meet maximum bin size
         */
        private int computeBinCount() {
            
            // Compute minimum number of bins to meet size requirements and approximate bin size
            long    lngCntBins = this.lngFrameSize / this.lngBinSizeMax;

            // account for any remainder
            lngCntBins += (this.lngFrameSize % this.lngBinSizeMax > 0) ? 1 : 0; 

            return Math.toIntExact(lngCntBins);
        }
        
        /**
         * Computes the minimum size of each ingestion frame bin
         * 
         * @return  minimum size of binned frame (in bytes)
         */
        private int computeBinSize() {
            long szBins = this.lngFrameSize / this.intBinCount;
            
            return Math.toIntExact(szBins);
        }
    }

    
    //
    // Defining Attributes
    //
    
    /** the maximum bin size in bytes */
    private final long      cntBytesMax;
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new instance of <code>FrameBinner</code> configured to the argument.
     * </p>
     *
     * @param   cntBytesMax     maximum size of any binned ingestion frame (in bytes)
     * 
     * @return  new <code>FrameBinner</code> instance produces bins no greater than the argument
     * 
     * @throws IllegalArgumentException the argument was less than or equal to zero
     */
    public static FrameBinner    from(long cntBytesMax) throws IllegalArgumentException {
        return new FrameBinner(cntBytesMax);
    }
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>FrameBinner</code> according to the given bin size.
     * </p>
     *
     * @param   cntBytesMax     maximum size of any binned ingestion frame (in bytes)
     * 
     * @throws IllegalArgumentException the argument was less than or equal to zero
     */
    public FrameBinner(long cntBytesMax) throws IllegalArgumentException {
        
        // Check argument
        if (cntBytesMax <= 0) 
            throw new IllegalArgumentException("Bin sizes must be greater than 0.");
        
        this.cntBytesMax = cntBytesMax;
    }

    
    //
    // Attribute Query (Getters)
    //
    
    /**
     * <p>
     * Returns the maximum size of any binned ingestion frame (in bytes).
     * </p>
     * 
     * @return  maximum allowable bin size in bytes 
     */
    public long getMaximumBinSize() {
        return this.cntBytesMax;
    }
    
    //
    // Operations
    //
    
    public List<IngestionFrame> binVertically(IngestionFrame frame) throws IllegalArgumentException, CompletionException {
        
        BinParameters binParams = BinParameters.from(frame, this.cntBytesMax);
        
        // Return original frame if binning is not required
        if (!binParams.requiresBinning())
            return List.of(frame);

        // Check allocation size of each ingestion frame row - if too large binning is impossible
        long    szRow = frame.allocationSizeRow();
        
        if (szRow >= this.cntBytesMax)
            throw new IllegalArgumentException("Ingestion frame row allocation size is greater than maximum bin size.");
            
        // Compute the number of rows to satisfy bin requirements
        int     cntRowsFrame = frame.getRowCount();
        int     cntRowsPerBin = cntRowsFrame / binParams.getBinCount();
        cntRowsPerBin += (cntRowsFrame % binParams.getBinCount() > 0) ? 1 : 0;
        
        // Create the bins
        List<IngestionFrame>    lstBins = new LinkedList<>();
        for (int iBin=0; iBin<binParams.getBinCount(); iBin++) {
            IngestionFrame  frmBin = frame.removeRowsAtHead(cntRowsPerBin);
            
            lstBins.add(frmBin);
        }
        
        // Check that original frame is now empty
        if (frame.hasData())
            throw new CompletionException("Internal error - original ingestion frame was not fully consumed.", new Throwable());

        return lstBins;
    }
}
