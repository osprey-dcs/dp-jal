/*
 * Project: dp-api-common
 * File:	QueryAssemblyTestResult.java
 * Package: com.ospreydcs.dp.jal.tools.query.assem
 * Type: 	QueryAssemblyTestResult
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
 * @since Aug 10, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.assem;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Comparator;

import com.ospreydcs.dp.api.common.JalDataTableType;
import com.ospreydcs.dp.api.common.ResultStatus;

/**
 * <p>
 * Record containing the results of a <code>QueryAssemblyTestCase</code> evaluation.
 * </p>
 * <p>
 * This record is intended for creation by the evaluation method 
 * <code>{@link QueryAssemblyTestCase#evaluate(com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer)</code>.
 * That method performs the evaluation for its test case condition as described in field <code>#recTestCase()</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 10, 2025
 *
 * @param strRqstId         the ID of the original time-series data request
 * 
 * @param cntRcvrdMsgs      the number of recovered data messages from the Query Service
 * @param szAllocRawPrcd    the allocated size of the recovered and correlated data (in bytes)
 * @param durDataRawPrcd    the time duration for time-series data recovery and correlation
 * @param dblRateRawPrcd    the data rate (in MBps) for time-series data recovery and correlation 
 * @param cntBlksRawTot     the number of raw correlated data blocks after recovery and correlation
 * @param cntBlksRawClkd    the number of clocked raw correlated data blocks after recovery and correlation
 * @param cntBlksRawTmsLst  the number of timestamp list correlated data blocks after recovery and correlation
 * @param recBlksOrdered    the ordering status of the raw correlated data blocks
 * @param recBlksDisTmDom   the time-domain disjointness status of the raw correlated data blocks
 * 
 * @param durAggrAssm       the time duration for sampled block assembly into a <code>SampledAggregate</code> instance
 * @param dblRateAggAssm    the data rate (in MBps) for sampled aggregate assembly
 * @param szAllocAggrAssm   the allocation size of the sampled aggregate after assembly (in bytes)
 * @param cntBlksAggTot     the total number of <code>SampledBlock</code> instances within the sampled aggregate 
 * @param cntBlksAggClkd    the number of clocked sampled blocks in the sampled aggregate after assembly
 * @param cntBlksAggTmsLst  the number of timestamp list sampled blocks in the sampled aggregate after assembly
 * @param cntBlksAggSupDom  the number of super-domain sampled blocks in the sampled aggregate after assembly
 * 
 * @param strTblId          the ID of the created data table
 * @param durTblBld         the time duration for data table creation
 * @param dblRateTblBld     the data rate (in MBps) for the data table creation
 * @param enmTblType        the data table type created
 * @param szTblCalc         the calculated data table size (in bytes)
 * @param cntTblRows        the number of data table rows
 * @param cntTblCols        the number of data table columns
 * 
 * @param szAllocTotal      the allocation size used to compute the total data rate
 * @param durTotal          the total time duration for the entire data request, recovery, assembly, and table creation      
 * @param dblRateTotal      the total data rate for the entire data request, recovery, assembly, and table creation
 */
public record QueryAssemblyTestResult(
        String                  strRqstId,

        int                     cntRcvrdMsgs,
        long                    szAllocRawPrcd,
        Duration                durDataRawPrcd,
        double                  dblRateRawPrcd,
        int                     cntBlksRawTot,
        int                     cntBlksRawClkd,
        int                     cntBlksRawTmsLst,
        ResultStatus            recBlksOrdered,
        ResultStatus            recBlksDisTmDom,
        
        Duration                durAggrAssm,
        double                  dblRateAggAssm,
        long                    szAllocAggrAssm,
        int                     cntBlksAggTot,
        int                     cntBlksAggClkd,
        int                     cntBlksAggTmsLst,
        int                     cntBlksAggSupDom,
        
        String                  strTblId,
        Duration                durTblBld,
        double                  dblRateTblBld,
        JalDataTableType        enmTblType,
        long                    szTblCalc,
        int                     cntTblRows,
        int                     cntTblCols,
        
        long                    szAllocTotal,
        Duration                durTotal,
        double                  dblRateTotal,
        
        QueryAssemblyTestCase   recTestCase
        ) 
{
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new <code>QueryAssemblyTestResult</code> record from the given argument values.
     * </p>
     * <p>
     * This creator is a direct analog to the canonical constructor requiring arguments for all fields of the 
     * returned instance.
     * </p>
     * 
     * @param strRqstId         the ID of the original time-series data request
     * 
     * @param cntRcvrdMsgs      the number of recovered data messages from the Query Service
     * @param szAllocRawPrcd    the allocated size of the recovered and correlated data (in bytes)
     * @param durDataRawPrcd    the time duration for time-series data recovery and correlation
     * @param dblRateRawPrcd    the data rate (in MBps) for time-series data recovery and correlation 
     * @param cntBlksRawTot     the number of raw correlated data blocks after recovery and correlation
     * @param cntBlksRawClkd    the number of clocked raw correlated data blocks after recovery and correlation
     * @param cntBlksRawTmsLst  the number of timestamp list correlated data blocks after recovery and correlation
     * @param recBlksOrdered    the ordering status of the raw correlated data blocks
     * @param recBlksDisTmDom   the time-domain disjointness status of the raw correlated data blocks
     * 
     * @param durAggrAssm       the time duration for sampled block assembly into a <code>SampledAggregate</code> instance
     * @param dblRateAggAssm    the data rate (in MBps) for sampled aggregate assembly
     * @param szAllocAggrAssm   the allocation size of the sampled aggregate after assembly (in bytes)
     * @param cntBlksAggTot     the total number of <code>SampledBlock</code> instances within the sampled aggregate 
     * @param cntBlksAggClkd    the number of clocked sampled blocks in the sampled aggregate after assembly
     * @param cntBlksAggTmsLst  the number of timestamp list sampled blocks in the sampled aggregate after assembly
     * @param cntBlksAggSupDom  the number of super-domain sampled blocks in the sampled aggregate after assembly
     * 
     * @param strTblId          the ID of the created data table
     * @param durTblBld         the time duration for data table creation
     * @param dblRateTblBld     the data rate (in MBps) for the data table creation
     * @param enmTblType        the data table type created
     * @param szTblCalc         the calculated data table size (in bytes)
     * @param cntTblRows        the number of data table rows
     * @param cntTblCols        the number of data table columns
     * 
     * @param szAllocTotal      the allocation size used to compute the total data rate
     * @param durTotal          the total time duration for the entire data request, recovery, assembly, and table creation      
     * @param dblRateTotal      the total data rate for the entire data request, recovery, assembly, and table creation
     * 
     * @param recTestCase       the test case performed
     * 
     * @return  a new <code>QueryAssemblyTestResult</code> record populated from the above arguments
     */
    public static QueryAssemblyTestResult   from(
            String                  strRqstId,

            int                     cntRcvrdMsgs,
            long                    szAllocRawPrcd,
            Duration                durDataRawPrcd,
            double                  dblRateRawPrcd,
            int                     cntBlksRawTot,
            int                     cntBlksRawClkd,
            int                     cntBlksRawTmsLst,
            ResultStatus            recBlksOrdered,
            ResultStatus            recBlksDisTmDom,
            
            Duration                durAggrAssm,
            double                  dblRateAggAssm,
            long                    szAllocAggrAssm,
            int                     cntBlksAggTot,
            int                     cntBlksAggClkd,
            int                     cntBlksAggTmsLst,
            int                     cntBlksAggSupDom,
            
            String                  strTblId,
            Duration                durTblBld,
            double                  dblRateTblBld,
            JalDataTableType        enmTblType,
            long                    szTblCalc,
            int                     cntTblRows,
            int                     cntTblCols,
            
            long                    szAllocTotal,
            Duration                durTotal,
            double                  dblRateTotal,
            
            QueryAssemblyTestCase   recTestCase
            ) 
    {
        return new QueryAssemblyTestResult(
                strRqstId,
                cntRcvrdMsgs, szAllocRawPrcd, durDataRawPrcd, dblRateRawPrcd, 
                    cntBlksRawTot, cntBlksRawClkd,cntBlksRawTmsLst, 
                    recBlksOrdered, recBlksDisTmDom,
                durAggrAssm, dblRateAggAssm, szAllocAggrAssm, cntBlksAggTot, cntBlksAggClkd, cntBlksAggTmsLst, cntBlksAggSupDom,
                strTblId, durTblBld, dblRateTblBld, enmTblType, szTblCalc, cntTblRows, cntTblCols,
                szAllocTotal, durTotal, dblRateTotal,
                recTestCase
                );
                
    }
    
    
    //
    // Tools
    //
    
    /**
     * <p>
     * Creates a <code>Comparator</code> instance that orders <code>QueryAssemblyTestResult</code> instances according to
     * descending assembly rates.
     * </p>
     * <p>
     * The return comparator orders <code>QueryAssemblyTestResult</code> records according to largest assembly rates.
     * Specifically, the record with the largest <code>{@link #dblRateAggAssm}</code> field is first in any ordering.
     * </p>
     * 
     * @return  a comparator of <code>QueryAssemblyTestResult</code> records where the largest assembly rate appears first
     */
    public static Comparator<QueryAssemblyTestResult>   descendingAssmRateOrdering() {
        
        Comparator<QueryAssemblyTestResult>     cmp = (r1, r2) -> {
            
            if (r1.dblRateAggAssm > r2.dblRateAggAssm)
                return -1;
            else
                return 1;
        };
        
        return cmp;
    }

    /**
     * <p>
     * Creates a <code>Comparator</code> instance that orders <code>QueryAssemblyTestResult</code> instances according to
     * ascending assembly rates.
     * </p>
     * <p>
     * The return comparator orders <code>QueryAssemblyTestResult</code> records according to smallest assembly rates.
     * Specifically, the record with the smallest <code>{@link #dblRateAggAssm}</code> field is first in any ordering.
     * </p>
     * 
     * @return  a comparator of <code>QueryAssemblyTestResult</code> records where the smallest assembly rate appears first
     */
    public static Comparator<QueryAssemblyTestResult>   ascendingAssmRateOrdering() {
        
        Comparator<QueryAssemblyTestResult>     cmp = (r1, r2) -> {
            
            if (r1.dblRateAggAssm < r2.dblRateAggAssm)
                return -1;
            else
                return 1;
        };
        
        return cmp;
    }

    /**
     * <p>
     * Creates a <code>Comparator</code> instance that orders <code>QueryAssemblyTestResult</code> instances according to
     * descending total rates.
     * </p>
     * <p>
     * The return comparator orders <code>QueryAssemblyTestResult</code> records according to largest total rates.
     * Specifically, the record with the largest <code>{@link #dblRateTotal}</code> field is first in any ordering.
     * </p>
     * 
     * @return  a comparator of <code>QueryAssemblyTestResult</code> records where the largest total rate appears first
     */
    public static Comparator<QueryAssemblyTestResult>   descendingTotalRateOrdering() {
        
        Comparator<QueryAssemblyTestResult>     cmp = (r1, r2) -> {
            
            if (r1.dblRateTotal > r2.dblRateTotal)
                return -1;
            else
                return 1;
        };
        
        return cmp;
    }

    /**
     * <p>
     * Creates a <code>Comparator</code> instance that orders <code>QueryAssemblyTestResult</code> instances according to
     * ascending total rates.
     * </p>
     * <p>
     * The return comparator orders <code>QueryAssemblyTestResult</code> records according to smallest total rates.
     * Specifically, the record with the smallest <code>{@link #dblRateTotal}</code> field is first in any ordering.
     * </p>
     * 
     * @return  a comparator of <code>QueryAssemblyTestResult</code> records where the smallest total rate appears first
     */
    public static Comparator<QueryAssemblyTestResult>   ascendingTotalRateOrdering() {
        
        Comparator<QueryAssemblyTestResult>     cmp = (r1, r2) -> {
            
            if (r1.dblRateTotal < r2.dblRateTotal)
                return -1;
            else
                return 1;
        };
        
        return cmp;
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
        String  strPadd = strPad + "  ";
//        String  strPaddBul = strPadd + " - ";
        
        ps.println(strPad + this.getClass().getSimpleName() + " " + this.recTestCase.indCase() + ":");
        ps.println(strPadd + "Original Data Request ID           : " + this.strRqstId);

        ps.println(strPadd + "Raw Data Recovery and Correlation");
        ps.println(strPadd + "  Recovered data message count     : " + this.cntRcvrdMsgs);
        ps.println(strPadd + "  Processed data size (bytes)      : " + this.szAllocRawPrcd);
        ps.println(strPadd + "  Processed data block count total : " + this.cntBlksRawTot);
        ps.println(strPadd + "  Processed block count clocked    : " + this.cntBlksRawClkd);
        ps.println(strPadd + "  Processed block count tms list   : " + this.cntBlksRawTmsLst);
        ps.println(strPadd + "  Processed blocks ordering status : " + this.recBlksOrdered);
        ps.println(strPadd + "  Processed blocks disjoint status : " + this.recBlksDisTmDom);
        ps.println(strPadd + "  Processsing duration             : " + this.durDataRawPrcd);
        ps.println(strPadd = "  Data processing rate (MBps)      : " + this.dblRateRawPrcd);
        
        ps.println(strPadd + "Sampled Aggregate Assembly");
        ps.println(strPadd + "  Assembled data size (bytes)      : " + this.szAllocAggrAssm);
        ps.println(strPadd + "  Sampled block count total        : " + this.cntBlksAggTot);
        ps.println(strPadd + "  Sampled block count clocked      : " + this.cntBlksAggClkd);
        ps.println(strPadd + "  Sampled block count tms list     : " + this.cntBlksAggTmsLst);
        ps.println(strPadd + "  Sampled block count super-domain : " + this.cntBlksAggSupDom);
        ps.println(strPadd + "  Aggregate assembly duration      : " + this.durAggrAssm);
        ps.println(strPadd + "  Aggregate assembly rate (MBps)   : " + this.dblRateAggAssm);
        
        ps.println(strPadd + "Data Table Creation");
        ps.println(strPadd + "  Table ID                         : " + this.strTblId);
        ps.println(strPadd + "  Table implementation type        : " + this.enmTblType);
        ps.println(strPadd + "  Table calculated size (bytes)    : " + this.szTblCalc);
        ps.println(strPadd + "  Table row count                  : " + this.cntTblRows);
        ps.println(strPadd + "  Table column count               : " + this.cntTblCols);
        ps.println(strPadd + "  Table creation duration          : " + this.durTblBld);
        ps.println(strPadd + "  Table creation data rate (MBps)  : " + this.dblRateTblBld);
        
        ps.println(strPadd + "Request Recovery, Correlation, Assembly, and Table Build");
        ps.println(strPadd + "  Total allocation size (bytes)    : " + this.szAllocTotal);
        ps.println(strPadd + "  Total duration                   : " + this.durTotal);
        ps.println(strPadd + "  Total data rate (MBps)           : " + this.dblRateTotal);
        
        ps.println(strPadd + "Test Case Parameters");
        this.recTestCase.printOut(ps, strPadd + "  ");
    }
    
}
