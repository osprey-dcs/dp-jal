/*
 * Project: dp-jal
 * File:	DataRequestConfig.java
 * Package: com.ospreydcs.dp.jal.tools.apps.query.common
 * Type: 	DataRequestConfig
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
 * @since Mar 13, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.query.common;

import java.io.PrintStream;
import java.util.List;

import com.ospreydcs.dp.jal.common.DpGrpcStreamType;
import com.ospreydcs.dp.jal.query.DpDataRequest;
import com.ospreydcs.dp.jal.query.model.request.DataRequestDecomposer;
import com.ospreydcs.dp.jal.query.model.request.RequestDecompType;

/**
 * <p>
 * Configuration parameters for a Query Service time-series data request as performed by <code>QueryChannel</code> instance.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 13, 2026
 *
 * @param bolColSerEnbl enable/disable data column serialization in request data recovery
 * @param enmStrmType   gRPC data stream type used in request data recovery {BACKWARD, BIDIRECTIONAL}
 * @param enmDcmpType   the request decomposition strategy used for multi-streaming request recovery
 * @param cntDcmpMax    the maximum number of data streams for multi-streaming request data recovery
 */
public record DataRequestConfig(
        boolean             bolColSerEnbl,
        DpGrpcStreamType    enmStrmType, 
        RequestDecompType   enmDcmpType, 
        int                 cntDcmpMax
        ) 
{
    
    //
    // Creators
    //

    /**
     * <p>
     * Creates and returns a new <code>DataRequestConfig</code> instance populated with the given argument values.
     * </p>
     * <p>
     * This creator is equivalent to the canonical constructor.
     * </p>
     * 
     * @param bolColSerEnbl enable/disable data column serialization in request data recovery
     * @param enmStrmType   gRPC data stream type used in request data recovery {BACKWARD, BIDIRECTIONAL}
     * @param enmDcmpType   the request decomposition strategy used for multi-streaming request recovery
     * @param cntDcmpMax    the maximum number of data streams for multi-streaming request data recovery
     * 
     * @return  a new <code>DataRequestConfig</code> instance with fields populated with the given argument values
     */
    public static DataRequestConfig from(
            boolean             bolColSerEnbl,
            DpGrpcStreamType    enmStrmType, 
            RequestDecompType   enmDcmpType, 
            int                 cntDcmpMax
            ) 
    {
        return new DataRequestConfig(bolColSerEnbl, enmStrmType, enmDcmpType, cntDcmpMax);
    }
    
    
    //
    // Record Resources
    //
    
    /** The time-series data request decomposer used to configure individual requests */
    private static final DataRequestDecomposer      DCMP_RQSTS = DataRequestDecomposer.from();
    
    
    //
    // Record Overrides
    //
    
    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof DataRequestConfig cfg) {
            boolean bolResult = (this.bolColSerEnbl == cfg.bolColSerEnbl)
                             && (this.enmStrmType == cfg.enmStrmType)
                             && (this.enmDcmpType == cfg.enmDcmpType)
                             && (this.cntDcmpMax == cfg.cntDcmpMax);
            
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
        
        ps.println(strPad + "column serialization enabled : " + this.bolColSerEnbl);
        ps.println(strPad + "decomposition strategy       : " + this.enmDcmpType);
        ps.println(strPad + "gRPC stream type             : " + this.enmStrmType);
        ps.print(  strPad + "gRPC stream count            : " + this.cntDcmpMax);
        if (this.enmDcmpType==RequestDecompType.NONE && this.cntDcmpMax>1)
            ps.println(strPad + "  (decomposition == NONE, only 1 used)");
    }
    
    /**
     * <p>
     * Configures the given <code>DpDataRequest</code> according to the parameters of this configuration.
     * </p>
     * <p>
     * The given time-series data request is assumed to be complete as far as selection of PVs and time range.
     * The given request is decomposed into a list of composite requests according to the strategy in fields 
     * <code>{@link #enmDcmpType()}</code> and <code>{@link #cntDcmpMax()}</code>.  
     * The column serialization enable/disable flag and gRPC stream types are then assigned to all composite requests.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The originating time-series data request given is left unchanged.
     * </p>  
     * 
     * @param rqstOrg
     * @return
     * @throws UnsupportedOperationException
     */
    public List<DpDataRequest>  configure(DpDataRequest rqstOrg) throws UnsupportedOperationException {
        
        // Decompose the original request according to strategy and count
        List<DpDataRequest> lstRqsts = DCMP_RQSTS.buildCompositeRequest(rqstOrg, this.enmDcmpType, this.cntDcmpMax);
        
        // Set the gRPC stream type and column serialization flag
        for (DpDataRequest rqst : lstRqsts) {
            rqst.enableDataColumnSerialization(this.bolColSerEnbl);
            rqst.setStreamType(this.enmStrmType);
        }
        
        return lstRqsts;
    }
}
