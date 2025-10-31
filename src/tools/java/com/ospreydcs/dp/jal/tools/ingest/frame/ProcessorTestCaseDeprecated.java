/*
 * Project: dp-jal
 * File:	ProcessorTestCaseDeprecated.java
 * Package: com.ospreydcs.dp.jal.tools.ingest.frame
 * Type: 	ProcessorTestCaseDeprecated
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
 * @since Sep 13, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.ingest.frame;

import java.io.PrintStream;

import com.ospreydcs.dp.api.ingest.model.frame.IngestionFrameProcessor;

/**
 * <p>
 * Record containing the parameters for a single <code>FrameProcessorEvaluator</code> application test case.
 * </p>
 * <p>
 * The record contains parameters for both the configuration for a payload of <code>IngestionFrame</code> objects
 * and the configuration for the <code>IngestionFrameProcessor</code> instance under evaluation.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Sep 13, 2025
 *
 * @param   indCase         Test case index
 * 
 * @param   cntFrmRows      Payload - number of rows within each ingestion frame
 * @param   cntFrmCols      Payload - number of columns within each ingestion frame
 * @param   cntFrmRows      Payload - number of ingestion frames for entire payload
 * 
 * @param   bolDcmpFrm      Processor Configuration - enable/disable ingestion frame decomposition
 * @param   szDcmpFrmMax    Processor Configuration - maximum allocation size (bytes) when frame decomposition enabled
 * 
 * @param   bolConcEnb      Processor Configuration - enable/disable (multi-threaded) concurrency)
 * @param   cntConcMaxThrds Processor Configuration - maximum number of processing threads when concurrency is enabled
 * 
 * @param   bolSerial       Processor Configuration - enable/disable <code>DataColumn</code> serialization for transport
 * 
 * @deprecated  Replaced by ProcessorTestCase which uses <code>SampleBlockConfig</code>
 */
@Deprecated(since="Sept 15, 2025", forRemoval=true)
public record ProcessorTestCaseDeprecated(
        int     indCase,
        
        int     cntFrmRows,
        int     cntFrmCols,
        int     cntFrames,
        
        boolean bolDcmpFrm,
        long    szDcmpFrmMax,
        
        boolean bolConcEnb,
        int     cntConcMaxThrds,
        
        boolean bolSerial
        ) 
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>ProcessorTestCaseDeprecated</code> record populated from the given arguments.
     * </p>
     * <p>
     * The <code>{@link #indCase}</code> field is assigned with the current value of <code>{@link #IND_CASE}</code>
     * which is allows incremented in the canonical constructor.
     * </p>
     * 
     * @param   cntFrmRows      Payload - number of rows within each ingestion frame
     * @param   cntFrmCols      Payload - number of columns within each ingestion frame
     * @param   cntFrmRows      Payload - number of ingestion frames for entire payload
     * 
     * @param   bolDcmpFrm      Processor Configuration - enable/disable ingestion frame decomposition
     * @param   szDcmpFrmMax    Processor Configuration - maximum allocation size (bytes) when frame decomposition enabled
     * 
     * @param   bolConcEnb      Processor Configuration - enable/disable (multi-threaded) concurrency)
     * @param   cntConcMaxThrds Processor Configuration - maximum number of processing threads when concurrency is enabled
     * 
     * @param   bolSerial       Processor Configuration - enable/disable <code>DataColumn</code> serialization for transport
     * 
     * @return  a new <code>ProcessorTestCaseDeprecated</code> record populated with the given argument values.
     */
    public static ProcessorTestCaseDeprecated from(
            int     cntFrmRows,
            int     cntFrmCols,
            int     cntFrames,
            
            boolean bolDcmpFrm,
            long    szDcmpFrmMax,
            
            boolean bolConcEnb,
            int     cntConcMaxThrds,
            
            boolean bolSerial
            )
    {
        return new ProcessorTestCaseDeprecated(IND_CASE, 
                cntFrmRows, cntFrmCols, cntFrames, 
                bolDcmpFrm, szDcmpFrmMax, 
                bolConcEnb, cntConcMaxThrds, 
                bolSerial);
    }
    
    
    //
    // Record Resources
    //
    
    /** Running index of test case - incremented upon creation/construction */
    private static int  IND_CASE = 1;

    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>ProcessorTestCaseDeprecated</code> instance.
     * </p>
     * <p>
     * Canonical constructor. Sets field values then increments record index counter <code>{@link #IND_CASE}</code>
     * </p>
     *
     * @param   indCase         Test case index
     * 
     * @param   cntFrmRows      Payload - number of rows within each ingestion frame
     * @param   cntFrmCols      Payload - number of columns within each ingestion frame
     * @param   cntFrmRows      Payload - number of ingestion frames for entire payload
     * 
     * @param   bolDcmpFrm      Processor Configuration - enable/disable ingestion frame decomposition
     * @param   szDcmpFrmMax    Processor Configuration - maximum allocation size (bytes) when frame decomposition enabled
     * 
     * @param   bolConcEnb      Processor Configuration - enable/disable (multi-threaded) concurrency)
     * @param   cntConcMaxThrds Processor Configuration - maximum number of processing threads when concurrency is enabled
     * 
     * @param   bolSerial       Processor Configuration - enable/disable <code>DataColumn</code> serialization for transport
     */
    public ProcessorTestCaseDeprecated {
        IND_CASE++;
    }
    
    
    //
    // Record Overrides
    /**
     * <p>
     * Checks argument type then compares all non-<code>{@link #indCase}</code> fields for strict equality.
     * Returns <code>true</code> only if argument is a <code>ProcessorTestCaseDeprecated</code> instance with all
     * fields having strict equality with this record, except field <code>{@link #indCase}</code>.
     * </p>
     * 
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProcessorTestCaseDeprecated rec) {
            boolean bolResult = this.cntFrmRows == rec.cntFrmRows
                    && this.cntFrmCols == rec.cntFrmCols
                    && this.cntFrames == rec.cntFrames
                    && this.bolDcmpFrm == rec.bolDcmpFrm
                    && this.szDcmpFrmMax == rec.szDcmpFrmMax
                    && this.bolConcEnb == rec.bolConcEnb
                    && this.cntConcMaxThrds == rec.cntConcMaxThrds
                    && this.bolSerial == rec.bolSerial;
                    
            return bolResult;
        }
        
        return false;
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Prints out a text description of the record fields to the given output stream.
     * </p>
     * <p>
     * A line-by-line text description of each record field is written to the given output.
     * The <code>strPad</code> is used to supply an optional whitespace character padding to the
     * left-hand side header for each line description.
     * </p>
     *   
     * @param ps        output stream to receive text description of record fields
     * @param strPad    white-space padding for each line header (or <code>null</code>)
     */
    public void printOut(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        String strPadd = strPad + "  ";
        String strPaddd = strPadd + "  ";
        
        ps.println(strPad + this.getClass().getSimpleName() + " " + this.indCase + ":");
        ps.println(strPadd + "Payload Configuraiton");
        ps.println(strPaddd + "ingestion frame row count            : " + this.cntFrmRows);
        ps.println(strPaddd + "ingestion frame column count         : " + this.cntFrmCols);
        ps.println(strPaddd + "total ingestion frame count          : " + this.cntFrames);
        ps.println(strPaddd + "IngestionFrameProcessor Configuration");
        ps.println(strPaddd + "enable ingestion frame decomposition : " + this.bolDcmpFrm);
        ps.println(strPaddd + "maximum composite frame size (bytes) : " + this.szDcmpFrmMax);
        ps.println(strPaddd + "enable concurrent processing         : " + this.bolConcEnb);
        ps.println(strPaddd + "concurrency maximum thread count     : " + this.cntConcMaxThrds);
        ps.println(strPaddd + "enable data column serialization     : " + this.bolSerial);
    }
    
    
    //
    // Support Methods
    //
    
//    private List<IngestionFrame>    createPayload() {
//        
//    }
    
    /**
     * <p>
     * Configures the given processor to the conditions of the this test case.
     * </p>
     *  
     * @param processor processor to be configured
     */
    private void    configureProcessor(IngestionFrameProcessor processor) {
        
        // Configure the processor
        if (this.bolDcmpFrm) 
            processor.setFrameDecomposition(this.szDcmpFrmMax);
        else
            processor.disableFrameDecomposition();
        
        if (this.bolConcEnb)
            processor.setConcurrency(this.cntConcMaxThrds);
        else
            processor.disableConcurrency();

        processor.enableSerialization(this.bolSerial);
    }
    

}
