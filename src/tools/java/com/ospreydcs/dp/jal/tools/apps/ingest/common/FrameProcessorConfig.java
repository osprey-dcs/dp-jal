/*
 * Project: dp-jal
 * File:	FrameProcessorConfig.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.common
 * Type: 	FrameProcessorConfig
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
 * @since Sep 15, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.common;

import java.io.PrintStream;

import com.ospreydcs.dp.jal.ingest.model.frame.IngestionFrameProcessor;

/**
 * <p>
 * Record containing configuration parameters for an <code>IngestionFrameProcecessor</code> instance.
 * </p>
 * <p>
 * The record contains parameters for the configuration of a <code>IngestionFrameProcessor</code> instance. 
 * </p>
 *
 *
 * @author Christopher K. Allen
 * @since Sep 15, 2025
 *
 * @param   bolDcmpFrm      Processor Configuration - enable/disable ingestion frame decomposition
 * @param   szDcmpFrmMax    Processor Configuration - maximum allocation size (bytes) when frame decomposition enabled
 * 
 * @param   bolConcEnb      Processor Configuration - enable/disable (multi-threaded) concurrency)
 * @param   cntConcMaxThrds Processor Configuration - maximum number of processing threads when concurrency is enabled
 * 
 * @param   bolSerial       Processor Configuration - enable/disable <code>DataColumn</code> serialization for transport
 */
public record FrameProcessorConfig(
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
     * Creates and returns a new <code>FrameProcessorConfig</code> record with field values given by the arguments.
     * </p>
     * 
     * @param   bolDcmpFrm      Processor Configuration - enable/disable ingestion frame decomposition
     * @param   szDcmpFrmMax    Processor Configuration - maximum allocation size (bytes) when frame decomposition enabled
     * 
     * @param   bolConcEnb      Processor Configuration - enable/disable (multi-threaded) concurrency)
     * @param   cntConcMaxThrds Processor Configuration - maximum number of processing threads when concurrency is enabled
     * 
     * @param   bolSerial       Processor Configuration - enable/disable <code>DataColumn</code> serialization for transport
     * 
     * @return  a new <code>FrameProcessorConfig</code> record populated with the given arguments.
     */
    public static FrameProcessorConfig  from(
            boolean bolDcmpFrm,
            long    szDcmpFrmMax,
            
            boolean bolConcEnb,
            int     cntConcMaxThrds,
            
            boolean bolSerial
            ) 
    {
        return new FrameProcessorConfig(bolDcmpFrm, szDcmpFrmMax, bolConcEnb, cntConcMaxThrds, bolSerial);
    }

    
    //
    // Record Overrides
    //
    
    /**
     * <p>
     * Overrides to check for record equivalence rather than strict equality of objects.
     * </p>
     * <p>
     * The argument is first confirmed to by of type <code>FrameProcessorConfig</code>. If so all its field
     * values are then checked for equality with the field values of this record.
     * </p>
     * <p>
     * @apiNote
     * The method is overriden so that the given argument can be a different object than this object.  The 
     * equality is enforced according to field values, not the record object itself.
     * </p>
     *   
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FrameProcessorConfig rec) {
            boolean bolResult = this.bolDcmpFrm == rec.bolDcmpFrm
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
        
        ps.println(strPad + "Enable ingestion frame decomposition : " + this.bolDcmpFrm);
        ps.println(strPad + "Maximum composite frame size (bytes) : " + this.szDcmpFrmMax);
        ps.println(strPad + "Enable concurrent processing         : " + this.bolConcEnb);
        ps.println(strPad + "Concurrency maximum thread count     : " + this.cntConcMaxThrds);
        ps.println(strPad + "Enable data column serialization     : " + this.bolSerial);
    }
    
    /**
     * <p>
     * Configures the given processor to the conditions of the this test case.
     * </p>
     *  
     * @param processor processor to be configured
     */
    public void    configureProcessor(IngestionFrameProcessor processor) {
        
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
