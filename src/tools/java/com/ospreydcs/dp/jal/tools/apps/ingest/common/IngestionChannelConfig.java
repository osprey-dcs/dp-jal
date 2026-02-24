/*
 * Project: dp-jal
 * File:	IngestionChannelConfig.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.common
 * Type: 	IngestionChannelConfig
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
 * @since Feb 17, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.common;

import java.io.PrintStream;

import com.ospreydcs.dp.jal.common.DpGrpcStreamType;
import com.ospreydcs.dp.jal.ingest.model.grpc.IngestionChannel;

/**
 * <p>
 * Record containing configuration parameters for an <code>IngestionChannel</code> component.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 17, 2026
 *
 * @param enmStrmType   the gRPC data stream type used ({@link DpGrpcStreamType#FORWARD} or {@link DpGrpcStreamType#BIDIRECTIONAL})
 * @param bolMStrmEnbl  enable/disable the use of concurrent gRPC data stream for transmission
 * @param cntMaxStrms   maximum number of concurrent gRPC data streams allowed
 */
public record IngestionChannelConfig(
        DpGrpcStreamType    enmStrmType,
        boolean             bolMStrmEnbl,
        int                 cntMaxStrms
        ) 
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>IngestionChannelConfig</code> record with field values given by the arguments.
     * </p>
     * <p>
     * This is a convenience creator equivalent to the canonical constructor
     * <code>{@link #IngestionChannelConfig(DpGrpcStreamType, boolean, int)}</code>.
     * </p> 
     * 
     * @param enmStrmType   the gRPC data stream type used ({@link DpGrpcStreamType#FORWARD} or {@link DpGrpcStreamType#BIDIRECTIONAL})
     * @param bolMStrmEnbl  enable/disable the use of concurrent gRPC data stream for transmission
     * @param cntMaxStrms   maximum number of concurrent gRPC data streams allowed
     * 
     * @return  a new <code>IngestionChannelConfig</code> record populated with the given arguments.
     */
    public static IngestionChannelConfig    from(
            DpGrpcStreamType    enmStrmType,
            boolean             bolMStrmEnbl,
            int                 cntMaxStrms
            ) 
    {
        return new IngestionChannelConfig(enmStrmType, bolMStrmEnbl, cntMaxStrms);
    }

    
    //
    // Record Overrides
    //
    
    /**
     * <p>
     * Overrides to check for record equivalence rather than strict equality of objects.
     * </p>
     * <p>
     * The argument is first confirmed to by of type <code>IngestionChannelConfig</code>. If so all its field
     * values are then checked for equality with the field values of this record.
     * </p>
     * <p>
     * @apiNote
     * The method is overridden so that the given argument can be a different object than this object.  The 
     * equality is enforced according to field values, not the record object itself.
     * </p>
     * 
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IngestionChannelConfig rec) {
            boolean bolResult = (this.enmStrmType == rec.enmStrmType)
                             && (this.bolMStrmEnbl == rec.bolMStrmEnbl)
                             && (this.cntMaxStrms == rec.cntMaxStrms);
            
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
        
        ps.println(strPad + "gRPC stream type             : " + this.enmStrmType);
        ps.println(strPad + "Enable multiple data streams : " + this.bolMStrmEnbl);
        ps.println(strPad + "Maximum data stream count    : " + this.cntMaxStrms);
    }
    
    /**
     * <p>
     * Configures the given ingestion channel to the conditions of the this configuration record.
     * </p>
     *  
     * @param chanIngest    the ingestion channel to be configured
     */
    public void configure(IngestionChannel chanIngest) {
        chanIngest.setStreamType(this.enmStrmType);
        
        if (this.bolMStrmEnbl)
            chanIngest.setMultipleStreams(this.cntMaxStrms);
        else
            chanIngest.disableMultipleStreams();
            
    }
    
}
