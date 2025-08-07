/*
 * Project: dp-api-common
 * File:	QueryRecoveryTestResult.java
 * Package: com.ospreydcs.dp.jal.tools.query.recovery
 * Type: 	QueryRecoveryTestResult
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
 * @since Jul 23, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.recovery;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.query.DpDataRequest;

/**
 * <p>
 * Record containing results of a <code>QueryRecoveryTestCase</code> evaluation.
 * </p>
 * <p>
 * Instances of <code>QueryRecoveryTestResult</code> are intended for creation by the 
 * <code>{@link QueryRecoveryTestCase#evaluate(QueryResponseRecoverer)}</code> method.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 23, 2025
 *
 * @param strRqstId         the (optional) request ID of the original time-series data request
 * @param lstCmpRqsts       the list of composite requests offered to the request processor
 * @param cntRcvrdMsgs      the number of <code>QueryData</code> message recovered from the request
 * @param szAllocPrcd       the total memory allocation (bytes) of the recovered/processed data
 * @param durDataPrcd       the total time duration to recover and correlate the requested data
 * @param dblRatePrcd       the data processing rate for data recovery and correlation
 * @param cntBlksPrcdTot    the total number of raw correlated data blocks after correlation processing
 * @param cntBlksPrcdClkd   the number of clocked raw correlated data blocks after correlation processing
 * @param cntBlksPrcdTmsLst the number of timestamp list raw correlated data blocks after correlation processing
 * @param recBlksOrdered    ordering status of the sorted set of processed (correlated) raw data blocks   
 * @param recBlksDisTmDom   disjoint time domain status of the sorted set of recovered and correlated raw data blocks
 */
public record QueryRecoveryTestResult(
        String                  strRqstId,
        List<DpDataRequest>     lstCmpRqsts,
        int                     cntRcvrdMsgs,
        long                    szAllocPrcd,
        Duration                durDataPrcd,
        double                  dblRatePrcd,
        int                     cntBlksPrcdTot,
        int                     cntBlksPrcdClkd,
        int                     cntBlksPrcdTmsLst,
        ResultStatus            recBlksOrdered,
        ResultStatus            recBlksDisTmDom,
        QueryRecoveryTestCase   recTestCase
        ) 
{

    // 
    // Creators
    //

    /**
     * <p>
     * Creates a new <code>QueryRecoveryTestCase</code> record with fields given by the argument values.
     * </p>
     * <p>
     * This creator is essentially the canonical constructor in creator form.
     * </p>
     * 
     * @param strRqstId         the (optional) request ID of the original time-series data request
     * @param lstCmpRqsts       the list of composite requests offered to the request processor
     * @param cntRcvrdMsgs      the number of <code>QueryData</code> message recovered from the request
     * @param szAllocPrcd       the total memory allocation (bytes) of the recovered/processed data
     * @param durDataPrcd       the total time duration to recover and correlate the requested data
     * @param dblRatePrcd       the data processing rate for data recovery and correlation
     * @param cntBlksPrcdTot    the total number of raw correlated data blocks after correlation processing
     * @param cntBlksPrcdClkd   the number of clocked raw correlated data blocks after correlation processing
     * @param cntBlksPrcdTmsLst the number of timestamp list raw correlated data blocks after correlation processing
     * @param recBlksOrdered    ordering status of the sorted set of processed (correlated) raw data blocks   
     * @param recBlksDisTmDom   disjoint time domain status of the sorted set of recovered and correlated raw data blocks
     * 
     * @return  a new <code>QueryRecoveryTestResult</code> record populated from the given arguments
     */
    public static QueryRecoveryTestResult   from(
            String                  strRqstId,
            List<DpDataRequest>     lstCmpRqsts,
            int                     cntRcvrdMsgs,
            long                    szAllocPrcd,
            Duration                durDataPrcd,
            double                  dblRatePrcd,
            int                     cntBlksPrcdTot,
            int                     cntBlksPrcdClkd,
            int                     cntBlksPrcdTmsLst,
            ResultStatus            recBlksOrdered,
            ResultStatus            recBlksDisTmDom,
            QueryRecoveryTestCase   recTestCase
            ) 
    {
        return new QueryRecoveryTestResult(
                strRqstId,
                lstCmpRqsts,
                cntRcvrdMsgs,
                szAllocPrcd,
                durDataPrcd,
                dblRatePrcd,
                cntBlksPrcdTot,
                cntBlksPrcdClkd,
                cntBlksPrcdTmsLst,
                recBlksOrdered,
                recBlksDisTmDom,
                recTestCase
                );
    }
    
    
    //
    // Tools
    //
    
    /**
     * <p>
     * Creates and returns a <code>Comparator</code> that sorts records in descending order of data rate values.
     * </p>
     * <p> 
     * Creates a <code>{@link Comparator}</code> of <code>QueryRecoveryTestResult</code> records based upon the
     * field <code>{@link #dblRatePrcd}</code>.
     * The returned comparator sorts records in descending order of the data rate value, that is, from largest
     * to smallest.
     * </p>
     * 
     * @return  a new <code>Comparator</code> instance that sorts records with largest <code>{@link #dblRatePrcd}</code> first
     */
    public static Comparator<QueryRecoveryTestResult>   descendingDataRateOrdering() {
        
        Comparator<QueryRecoveryTestResult>     cmp = (r1, r2) -> {
          
            if (r1.dblRatePrcd > r2.dblRatePrcd)
                return -1;
            else
                return 1;
        };
        
        return cmp;
    }
    
    /**
     * <p>
     * Creates and returns a <code>Comparator</code> that sorts records in ascending order of data rate values.
     * </p>
     * <p> 
     * Creates a <code>{@link Comparator}</code> of <code>QueryRecoveryTestResult</code> records based upon the
     * field <code>{@link #dblRatePrcd}</code>.
     * The returned comparator sorts records in ascending order of the data rate value, that is, from smallest
     * to largest.
     * </p>
     * 
     * @return  a new <code>Comparator</code> instance that sorts records with smallest <code>{@link #dblRatePrcd}</code> first
     */
    public static Comparator<QueryRecoveryTestResult>   ascendingDataRateOrdering() {
        
        Comparator<QueryRecoveryTestResult>     cmp = (r1, r2) -> {
          
            if (r1.dblRatePrcd < r2.dblRatePrcd)
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
        
        ps.println(strPad + this.getClass().getSimpleName() + " for test case # " + this.recTestCase.indCase() + ":");
        ps.println(strPad + "  Original Data Request ID            : " + this.strRqstId);
        ps.println(strPad + "  Request Supplemental PVs            : " + this.recTestCase.setSupplPvs());
        ps.println(strPad + "  Composite request count             : " + this.lstCmpRqsts.size());
        ps.println(strPad + "  Recovered data message count        : " + this.cntRcvrdMsgs);
        ps.println(strPad + "  Recovered allocation size (bytes)   : " + this.szAllocPrcd);
        ps.println(strPad + "  Recovery and processing duration    : " + this.durDataPrcd);
        ps.println(strPad + "  Recovery and processing rate (Mbps) : " + this.dblRatePrcd);
        ps.println(strPad + "  Raw correlated blocks total         : " + this.cntBlksPrcdTot);
        ps.println(strPad + "  Raw correlated blocks clocked       : " + this.cntBlksPrcdClkd);
        ps.println(strPad + "  Raw correlated blocks tms list      : " + this.cntBlksPrcdTmsLst);
        ps.println(strPad + "  Raw correlated block ordered status : " + this.recBlksOrdered);
        ps.println(strPad + "  Raw correlated block disj. tm. dom. : " + this.recBlksDisTmDom);
        ps.println(strPad + "  Test Case");
        this.recTestCase.printOut(ps, strPadd);
    }

}


