/*
 * Project: dp-api-common
 * File:	SampledAggregateResult.java
 * Package: com.ospreydcs.dp.jal.tools.query.assem
 * Type: 	SampledAggregateResult
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
 * @since Jul 9, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.assem;

import java.io.PrintStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.query.model.assem.QueryResponseAssembler;
import com.ospreydcs.dp.api.query.model.assem.SampledAggregate;
import com.ospreydcs.dp.api.query.model.coalesce.SampledBlock;
import com.ospreydcs.dp.api.query.model.coalesce.SampledBlockClocked;
import com.ospreydcs.dp.api.query.model.coalesce.SampledBlockTmsList;
import com.ospreydcs.dp.api.query.model.coalesce.SampledBlockType;
import com.ospreydcs.dp.api.query.model.superdom.SampledBlockSuperDom;

/**
 * <p>
 * Record containing the results and properties of a time-series data request data assembly operation.
 * </p>
 * <p>
 * It is assumed the the raw time-series data has been recovered and correlated.  The results contained
 * in this record reflect the operation of the <code>{@link QueryResponseAssembler}</code> component which
 * accepts the raw correlated data and creates a <code>{@link SampledAggregate}</code> object containing
 * all the fully processed data from the original request.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Jul 9, 2025
 *
 * @param strRqstId         the (optional) request ID string of the original time-series data request
 * @param cntSmpBlksTotal   the total number of <code>SampledBlock</code> instances within the <code>SampledAggregate</code>
 * @param cntSmpBlksClk     the number of <code>SampledBlockTmsList</code> instances within the <code>SampledAggreate</code>
 * @param cntSmpBlksTmsLst  the number of <code>SampledBlockClocked</code> instances within the <code>SampledAggregate</code>
 * @param cntSmpBlksSupDom  the number of <code>SampledBlockSuperDom</code> instances within the <code>SampledAggregate</code>
 * @param szAllocRaw        the memory allocation of the raw data required to make the <code>SampledAggregate</code> (Bytes)
 * @param szAllocJava       the memory allocation of the Java data required for <code>SampledAggregate</code> (heap alloc in bytes)
 * @param cntTms            the total number of timestamps within the full <code>SampledAggregate</code>
 * @param cntPvs            the total number of Process Variables within the <code>SampledAggregate</code>
 * @param recOrdering       status of the <code>SampledBlock</code> ordering within the <code>SampledAggregate</code>
 * @param recDisTmDoms      status of the <code>SampledBlock</code> time domains within the <code>SampledAggregate</code>
 * @param recPvTypes        status of all data source (PVs) data type consistency with the <code>SampledAggregate</code>
 * @param durSmpAggPrcd     the time duration required to build the <code>SampledAggregate</code> from the raw correlated data
 * @param dblRateSmpAggPrcd the processing rate for creating the <code>SampledAggregate</code> (MBps)
 * @param lstSmpBlkProps    ordered list of properties for each <code>SampledBlock</code> within the <code>SampledAggregate</code> 
 */
public record SampledAggregateResult(
        String          strRqstId,
        int             cntSmpBlksTotal,
        int             cntSmpBlksClk,
        int             cntSmpBlksTmsLst,
        int             cntSmpBlksSupDom,
        long            szAllocRaw,
        long            szAllocJava,
        int             cntTms,
        int             cntPvs,
        ResultStatus    recOrdering,
        ResultStatus    recDisTmDoms,
        ResultStatus    recPvTypes,
        Duration        durSmpAggPrcd,
        double          dblRateSmpAggPrcd,
        List<SampledBlockProps>  lstSmpBlkProps
        ) 
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>SampledAggregateResult</code> record with fields given by the argument values.
     * </p>
     * 
     * @param strRqstId         the (optional) request ID string of the original time-series data request
     * @param cntSmpBlksTotal   the total number of <code>SampledBlock</code> instances within the <code>SampledAggregate</code>
     * @param cntSmpBlksClk     the number of <code>SampledBlockTmsList</code> instances within the <code>SampledAggreate</code>
     * @param cntSmpBlksTmsLst  the number of <code>SampledBlockClocked</code> instances within the <code>SampledAggregate</code>
     * @param cntSmpBlksSupDom  the number of <code>SampledBlockSuperDom</code> instances within the <code>SampledAggregate</code>
     * @param szAllocRaw        the memory allocation of the raw data required to make the <code>SampledAggregate</code> (Bytes)
     * @param szAllocJava       the memory allocation of the Java data required for <code>SampledAggregate</code> (heap alloc in bytes)
     * @param cntTms            the total number of timestamps within the full <code>SampledAggregate</code>
     * @param cntPvs            the total number of Process Variables within the <code>SampledAggregate</code>
     * @param recOrdering       status of the <code>SampledBlock</code> ordering within the <code>SampledAggregate</code>
     * @param recDisTmDoms      status of the <code>SampledBlock</code> time domains within the <code>SampledAggregate</code>
     * @param recPvTypes        status of all data source (PVs) data type consistency with the <code>SampledAggregate</code>
     * @param durSmpAggPrcd     the time duration required to build the <code>SampledAggregate</code> from the raw correlated data
     * @param dblRateSmpAggPrcd the processing rate for creating the <code>SampledAggregate</code> (MBps)
     * @param lstSmpBlkProps    ordered list of properties for each <code>SampledBlock</code> within the <code>SampledAggregate</code> 
     * 
     * @return  a new <code>SampledAggregateResult</code> record populated with the given argument values
     */
    public static SampledAggregateResult from(
            String          strRqstId,
            int             cntSmpBlksTotal,
            int             cntSmpBlksClk,
            int             cntSmpBlksTmsLst,
            int             cntSmpBlksSupDom,
            long            szAllocRaw,
            long            szAllocJava,
            int             cntTms,
            int             cntPvs,
            ResultStatus    recOrdering,
            ResultStatus    recDisTmDoms,
            ResultStatus    recPvTypes,
            Duration        durSmpAggPrcd,
            double          dblRateSmpAggPrcd,
            List<SampledBlockProps>  lstSmpBlkProps
            ) 
    {
        return new SampledAggregateResult(
                strRqstId,
                cntSmpBlksTotal,
                cntSmpBlksClk,
                cntSmpBlksTmsLst,
                cntSmpBlksSupDom,
                szAllocRaw,
                szAllocJava,
                cntTms,
                cntPvs,
                recOrdering,
                recDisTmDoms,
                recPvTypes,
                durSmpAggPrcd,
                dblRateSmpAggPrcd,
                lstSmpBlkProps
                );
    }
    
    /**
     * <p>
     * Create and return a new <code>SampledAggregateResult</code> record with field values extracted from the given arguments.
     * </p>
     * <p>
     * The method extracts from the arguments the properties required to populate a <code>SampledAggregateResult<code> record. 
     * </p>
     *  
     * @param aggSmpBlks    the <code>SampledAggregate</code> created by the <code>{@link QueryResponseAssembler}</code>
     * @param durSmpAggPrcd the time taken to create the <code>SampledAggregate</code> from the raw correlated data
     * 
     * @return  a new <code>SampledAggregateResult</code> populated with properties taken from the given arguments
     */
    public static SampledAggregateResult    from(SampledAggregate aggSmpBlks, Duration durSmpAggPrcd) {
        
        // Extract the properties of the sampled aggregate
        String  strRqstId = aggSmpBlks.getRequestId();
        int     cntSmpBlksTotal = aggSmpBlks.getSampledBlockCount();
        long    szAllocRaw = aggSmpBlks.getRawAllocation();
        long    szAllocJava = aggSmpBlks.computeAllocationSize();
        int     cntTms = aggSmpBlks.getSampleCount();
        int     cntPvs = aggSmpBlks.getDataSourceCount();
        
        ResultStatus    recOrdering = aggSmpBlks.verifyStartTimeOrdering();
        ResultStatus    recDisTmDoms = aggSmpBlks.verifyDisjointTimeDomains();
        ResultStatus    recPvTypes = aggSmpBlks.verifySourceTypes();

        // Get the composite block types
        int     cntSmpBlksClk = 0;
        int     cntSmpBlksTmsLst = 0;
        int     cntSmpBlksSupDom = 0;
        for (SampledBlock blk : aggSmpBlks) {
            if (blk instanceof SampledBlockClocked)
                cntSmpBlksClk++;
            if (blk instanceof SampledBlockTmsList)
                cntSmpBlksTmsLst++;
            if (blk instanceof SampledBlockSuperDom)
                cntSmpBlksSupDom++;
        }
        
        // Compute performance parameters
        double      dblRateSmpAggPrcd = ( ((double)szAllocRaw) * 1000 )/durSmpAggPrcd.toNanos();

        // Extract the properties of the composite blocks
        int             indBlk = 0;
        Set<String>     setAllPvs = aggSmpBlks.getDataSourceNames();
        
        List<SampledBlockProps>     lstSmpBlkProps = new ArrayList<>(cntSmpBlksTotal);
        for (SampledBlock blk : aggSmpBlks) {
            SampledBlockProps  recBlkProps = SampledBlockProps.from(indBlk, setAllPvs, blk);
            
            lstSmpBlkProps.add(recBlkProps);
            indBlk++;
        }
        
        
        // Create the result record and return it
        return SampledAggregateResult.from(strRqstId, 
                cntSmpBlksTotal, 
                cntSmpBlksClk, 
                cntSmpBlksTmsLst, 
                cntSmpBlksSupDom, 
                szAllocRaw, 
                szAllocJava, 
                cntTms, 
                cntPvs, 
                recOrdering,
                recDisTmDoms,
                recPvTypes,
                durSmpAggPrcd, 
                dblRateSmpAggPrcd, 
                lstSmpBlkProps
                );
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
        
        // Print out test result single parameters
        ps.println(strPad + "Sampled Aggregate Assembly");
        ps.println(strPad + "  time-series data request ID           : " + this.strRqstId);
        ps.println(strPad + "  sampled blocks total count            : " + this.cntSmpBlksTotal);
        ps.println(strPad + "  sampled blocks clocked count          : " + this.cntSmpBlksClk); 
        ps.println(strPad + "  sampled blocks timestamp list count   : " + this.cntSmpBlksTmsLst); 
        ps.println(strPad + "  sampled blocks super domain count     : " + this.cntSmpBlksSupDom); 
        ps.println(strPad + "  raw correl. data allocation (bytes)   : " + this.szAllocRaw);
        ps.println(strPad + "  Java data allocation (bytes)          : " + this.szAllocJava);
        ps.println(strPad + "  aggregate start time ordering status  : " + this.recOrdering);
        ps.println(strPad + "  aggregate disjoint time domain status : " + this.recDisTmDoms);
        ps.println(strPad + "  aggregate PV type consistency status  : " + this.recPvTypes);
        ps.println(strPad + "  sampled aggregate build duration      : " + this.durSmpAggPrcd);
        ps.println(strPad + "  sampled aggregate build rate (Mbps)   : " + this.dblRateSmpAggPrcd);

        ps.println(strPad + "  Individual Sample Block Properties");
        strPad = strPad + "  ";
        for (SampledBlockProps rec : this.lstSmpBlkProps) 
            rec.printOut(ps, strPad);
    }
    
    
    // 
    // Internal Types
    //
    
    /**
     * <p>
     * Record containing properties of a <code>SampledBlock</code> instance.
     * </p>
     * <p>
     * The record fields contain properties of the individual <code>SampledBlock</code> pages within a 
     * <code>SampledAggregate</code> super structure.
     * </p>
     *
     * @author Christopher K. Allen
     * @since Jul 9, 2025
     *
     */
    public static record SampledBlockProps (
            int                 indBlk,
            String              strRqstId,
            SampledBlockType    enmType,
            long                lngAllocRaw,
            long                lngAllocJava,
            boolean             bolAllPvs,
            int                 cntRows,
            int                 cntCols,
            TimeInterval        tvlTmDom
            ) 
    {
        
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates and returns a new <code>SampledBlockProps</code> record with fields populated with the given arguments.
         * </p>
         * 
         * @param indBlk        the <code>SampledBlock</code> index within the <code>SampledAggregrate</code> super structure
         * @param strRqstId     the (optional) string ID of original time-series data request producing this <code>SampledBlock</code>
         * @param enmType       the <code>SampledBlock</code> sub-type of this sampled block
         * @param szAllocRaw   the allocation size (bytes) of the raw data used to create this <code>SampledBlock</code>
         * @param szAllocJava  the allocation size (bytes) of the Java data used within this <code>SampledBlock</code>
         * @param bolAllPvs     are all PVs of the original time-series data request contained within the <code>SampledBlock</code>
         * @param cntRows       the number of rows (timestamps) within the <code>SampledBlock</code>
         * @param cntCols       the number of data columns (time series) within the <code>SampledBlock</code>
         * @param tvlTmDom      the time domain containing all timestamps within the <code>SampledBlock</code>
         * 
         * @return  a new <code>SampledBlockProps</code> record populated with the given arguments
         */
        public static SampledBlockProps  from(
                int                 indBlk, 
                String              strRqstId, 
                SampledBlockType    enmType, 
                long                lngAllocRaw,
                long                lngAllocJava,
                boolean             bolAllPvs, 
                int                 cntRows,
                int                 cntCols,
                TimeInterval        tvlTmDom
                ) 
        {
            return new SampledBlockProps(indBlk, strRqstId, enmType, lngAllocRaw, lngAllocJava, bolAllPvs, cntRows, cntCols, tvlTmDom);
        }
        
        /**
         * <p>
         * Creates and returns a new <code>SampledBlockProps</code> record by extracting the field values from the given arguments.
         * </p>
         * 
         * @param indBlk        the index of the <code>SampledBlock</code> within the <code>SampledAggregate</code> super structure
         * @param setPvsOrg     a collection of all the PV names within the <code>SampledAggregate</code> super structure
         * @param blkSmp        the <code>SampledBlock</code> containing properties for extraction
         * 
         * @return  a new <code>SampledBlockProps</code> record with fields populated with data extracted from the arguments
         */
        public static SampledBlockProps  from(int indBlk, Collection<String> setPvsOrg, SampledBlock blkSmp) {

            String              strRqstId = blkSmp.getRequestId();
            SampledBlockType    enmType = SampledBlockType.getBlockType(blkSmp);
            long                lngAllocRaw = blkSmp.getRawAllocation();
            long                lngAllocJava = blkSmp.allocationSize();
            boolean             bolAllPvs = blkSmp.getSourceNames().containsAll(setPvsOrg);
            int                 cntRows = blkSmp.getRowCount();
            int                 cntCols = blkSmp.getColumnCount();
            TimeInterval        tvlTmDom = blkSmp.getTimeRange();
            
            SampledBlockProps    recProps = SampledBlockProps.from(indBlk, strRqstId, enmType, lngAllocRaw, lngAllocJava, bolAllPvs, cntRows, cntCols, tvlTmDom);
            
            return recProps;
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
            
            // Print out test result single parameters
            ps.println(strPad + "Properties for SampledBlock #" + this.indBlk);
            ps.println(strPad + "  time-series data request ID         : " + this.strRqstId);
            ps.println(strPad + "  sampled block type                  : " + this.enmType);
            ps.println(strPad + "  raw correl. data allocation (bytes) : " + this.lngAllocRaw);
            ps.println(strPad + "  Java data allocation (bytes)        : " + this.lngAllocJava);
            ps.println(strPad + "  contains all aggregate PVs          : " + this.bolAllPvs);
            ps.println(strPad + "  Sample block row count (timestamps) : " + this.cntRows);
            ps.println(strPad + "  sample block column count (PVs)     : " + this.cntCols);
            ps.println(strPad + "  sample block time domain            : " + this.tvlTmDom);
        }
    }

}
