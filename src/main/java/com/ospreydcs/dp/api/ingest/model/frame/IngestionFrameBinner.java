/*
 * Project: dp-api-common
 * File:	IngestionFrameBinner.java
 * Package: com.ospreydcs.dp.api.ingest.model.frame
 * Type: 	IngestionFrameBinner
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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionException;

import com.ospreydcs.dp.api.ingest.IngestionFrame;

/**
 * <p>
 * Processor class used for decomposition operations on <code>IngestionFrame</code> instances.
 * </p>
 * <p>
 * Intended for use whenever gRPC ingestion messages (i.e., <code>IngestDataRequest</code> instances) 
 * are restricted in size, for example, by the gRPC message size limitation. 
 * Then ingestion frames exceeding this limitation must be divided
 * into smaller frames meeting the size requirements.
 * This class provides common operations for decomposition "binning" of one large ingestion 
 * frame into multiple smaller frames, each meeting the given allocation size requirement.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Apr 8, 2024
 *
 */
public class IngestionFrameBinner {

    
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
     * Creates a new instance of <code>IngestionFrameBinner</code> configured to the argument.
     * </p>
     *
     * @param   cntBytesMax     maximum size of any binned ingestion frame (in bytes)
     * 
     * @return  new <code>IngestionFrameBinner</code> instance produces bins no greater than the argument
     * 
     * @throws IllegalArgumentException the argument was less than or equal to zero
     */
    public static IngestionFrameBinner    from(long cntBytesMax) throws IllegalArgumentException {
        return new IngestionFrameBinner(cntBytesMax);
    }
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionFrameBinner</code> according to the given bin size.
     * </p>
     *
     * @param   cntBytesMax     maximum size of any binned ingestion frame (in bytes)
     * 
     * @throws IllegalArgumentException the argument was less than or equal to zero
     */
    public IngestionFrameBinner(long cntBytesMax) throws IllegalArgumentException {
        
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
    
    /**
     * <p>
     * <h1>Compute and return the bin parameters for ingestion frame decomposition by this instance.</h1>
     * </p>
     * 
     * @param frame target frame for binning decomposition
     * 
     * @return  the parameters used for target frame decomposition by this <code>IngestionFrameBinner</code>.
     */
    public BinParameters    computeBinParameters(IngestionFrame frame) {
        return BinParameters.from(frame, this.cntBytesMax);
    }
    
    /**
     * <p>
     * <h1>Decomposes the given ingestion frame horizontally - that is, by columns.</h1>
     * </p>
     * <p>
     * The argument is decomposed by decomposing the source ingestion frame by columns.  
     * Thus, every returned ingestion frame contains time-series data for the same time range.
     * However, every returned frame contains only a subset of the data columns (time-series data) 
     * from the original ingestion frame. 
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * The ingestion frame provided as the argument is effectively destroyed in the decomposition
     * process.  If the argument is successfully decomposed all its data is transferred to the 
     * returned frames and it is left empty.
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Horizontal decomposition (by column) of ingestion frames is more efficient than
     * vertical decomposition (by row).  For best performance use horizontal decomposition 
     * (this method) whenever possible.
     * </li>
     * <br/>
     * <li>
     * The current implementation decomposes the argument by recursively removing the left
     * most data columns from the source frame 
     * (i.e., using {@link IngestionFrame#removeColumnsByIndex(int)}</code>). 
     * </li>
     * <br/>
     * <li>
     * The order of the returned frames is determined by increasing column index of the original
     * ingestion frame.  Recall that column indexing of ingestion frames is arbitrarily determined 
     * by the ingestion frame itself.
     * </li>
     * </ul>
     * </p>
     * 
     * @param frmSource the source ingestion frame to be decomposed
     * 
     * @return ordered list of decomposed ingestion frames containing all data of the argument
     * 
     * @throws IllegalArgumentException argument column size is too large, column allocation too large for decomposition
     * @throws CompletionException      serious internal error - argument not fully decomposed
     */
    public List<IngestionFrame> decomposeHorizontally(IngestionFrame frmSource) throws IllegalArgumentException, CompletionException {
        
        BinParameters paramsBin = this.computeBinParameters(frmSource);
        
        // Return original frame if binning is not required
        if (!paramsBin.requiresBinning())
            return List.of(frmSource);

        // Check allocation size of each ingestion frame row - if too large binning is impossible
        long    szCol = frmSource.allocationSizeColumn();
        
        if (szCol > paramsBin.getBinSize())
            throw new IllegalArgumentException("Ingestion frame " 
                    + frmSource.getFrameLabel() 
                    + ": column allocation size is greater than maximum bin size.");
            
        // Compute the number of columns to satisfy bin requirements
        int     cntColsFrame = frmSource.getColumnCount();
        int     cntColsBin = cntColsFrame / paramsBin.getBinCount();
        cntColsBin += (cntColsFrame % paramsBin.getBinCount() > 0) ? 1 : 0;
        
        // Create the bins
        List<IngestionFrame>    lstBins = new LinkedList<>();
        for (int iBin=0; iBin<paramsBin.getBinCount(); iBin++) {
            IngestionFrame  frmBin = frmSource.removeColumnsByIndex(cntColsBin);
            
            lstBins.add(frmBin);
        }
        
        // Check that original frame is now empty
        if (frmSource.hasData())
            throw new CompletionException("Internal error for ingestion frame " 
                    + frmSource.getFrameLabel() + ": was not fully consumed.", 
                    new Throwable("Incomplete decomposition"));

        return lstBins;
    }
    
    /**
     * <p>
     * <h1>Decomposes the given ingestion frame vertically - that is, by rows.</h1>
     * </p>
     * <p>
     * The argument is decomposed by sub-sectioning every ingestion frame row.  Thus, every
     * returned ingestion frame has the same number of data columns and data column names.
     * Each binned frame within the returned list contains a different time range for each 
     * of the original time-series in the source frame.
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * The ingestion frame provided as the argument is effectively destroyed in the binning
     * process.  If the argument is successfully binned all its data is transferred to the 
     * returned frames and it is left empty.
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Vertical decomposition (by row) of ingestion frames is more resource intensive
     * than horizontal decomposition (by columns).  For better performance use horizontal
     * decomposition whenever possible.
     * </li>
     * <br/>
     * <li>
     * The current implementation bins the argument by recursively removing its (row) head 
     * (i.e., using the <code>{@link IngestionFrame#removeRowsAtHead(int)}</code> method).
     * </li>
     * <br/>
     * <li>
     * The order of the returned frames is determined by increasing timestamps.  Specifically,
     * the first element in the list is the head of the original frame, the remaining frames
     * follow according to timestamp ordering.
     * </li>
     * </ul>
     * </p>
     * 
     * @param frmSource the source ingestion frame to be decomposed
     * 
     * @return ordered list of decomposed ingestion frames containing all data of the argument
     * 
     * @throws IllegalArgumentException argument row size is too large, row allocation too large for decomposition
     * @throws CompletionException      serious internal error - argument not fully decomposed
     */
    public List<IngestionFrame> decomposeVertically(IngestionFrame frmSource) throws IllegalArgumentException, CompletionException {
        
        BinParameters paramsBin = this.computeBinParameters(frmSource);
        
        // Return original frame if binning is not required
        if (!paramsBin.requiresBinning())
            return List.of(frmSource);

        // Check allocation size of each ingestion frame row - if too large binning is impossible
        long    szRow = frmSource.allocationSizeRow();
        
        if (szRow > paramsBin.getBinSize())
            throw new IllegalArgumentException("Ingestion frame : " 
                    + frmSource.getFrameLabel() 
                    + " row allocation size is greater than maximum bin size.");
            
        // Compute the number of rows to satisfy bin requirements
        int     cntRowsFrame = frmSource.getRowCount();
        int     cntRowsPerBin = cntRowsFrame / paramsBin.getBinCount();
        cntRowsPerBin += (cntRowsFrame % paramsBin.getBinCount() > 0) ? 1 : 0;
        
        // Create the bins
        List<IngestionFrame>    lstBins = new LinkedList<>();
        for (int iBin=0; iBin<paramsBin.getBinCount(); iBin++) {
            IngestionFrame  frmBin = frmSource.removeRowsAtHead(cntRowsPerBin);
            
            lstBins.add(frmBin);
        }
        
        // Check that original frame is now empty
        if (frmSource.hasData())
            throw new CompletionException("Internal error for ingestion frame " 
                        + frmSource.getFrameLabel() + ": was not fully consumed.", 
                        new Throwable("Incomplete decomposition"));

        return lstBins;
    }
}
