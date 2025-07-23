/*
 * Project: dp-api-common
 * File:	QueryRecoveryResult.java
 * Package: com.ospreydcs.dp.jal.tools.query.assem
 * Type: 	QueryRecoveryResult
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
import java.util.SortedSet;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer;
import com.ospreydcs.dp.api.query.model.correl.RawClockedData;
import com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData;
import com.ospreydcs.dp.api.query.model.correl.RawTmsListData;
import com.ospreydcs.dp.api.query.model.superdom.TimeDomainProcessor;

/**
 * <p>
 * Record containing the general results of a time-series request recovery and correlation operation.
 * </p>
 * <p>
 * The results are generally obtained from a <code>{@link QueryRequestRecoverer#process(DpDataRequest)}</code>
 * operation.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 9, 2025
 *
 * @param strRecoveryRqstId the (optional) request ID of the original time-series data request
 * @param cntRecoveryMsgs   the number of <code>QueryData</code> message recovered from the request
 * @param szRecoveryAlloc   the total memory allocation (bytes) of the recovered data
 * @param cntRawBlksTotal   the total number of correlated data blocks after correlation processing
 * @param cntRawBlksClk     the number of clocked correlated data blocks after correlation processing
 * @param cntRawBlksTmsLst  the number of timestamp list correlated data blocks after correlation processing
 * @param durRawDataPrcd    the total time to recover and correlate the requested data
 * @param dblRateRawDataPrcd the data rate for data recovery and correlation
 * @param recOrdering       ordering status of the sorted set of recovered and correlated data  
 * @param recDisTmDom       disjoint time domain status of the sorted set of recovered and correlated data
 */
public record QueryRecoveryResult(
        String          strRecoveryRqstId,
        int             cntRecoveryMsgs,
        long            szRecoveryAlloc,
        int             cntRawBlksTotal,
        int             cntRawBlksClk,
        int             cntRawBlksTmsLst,
        Duration        durRawDataPrcd,
        double          dblRateRawDataPrcd,
        ResultStatus    recOrdering,
        ResultStatus    recDisTmDom
        ) 
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>QueryRecoveryResult</code> record with fields given by the argument values.
     * </>
     * 
     * @param strRecoveryRqstId the (optional) request ID of the original time-series data request
     * @param cntRecoveryMsgs   the number of <code>QueryData</code> message recovered from the request
     * @param szRecoveryAlloc   the total memory allocation (bytes) of the recovered data
     * @param cntRawBlksTotal   the total number of correlated data blocks after correlation processing
     * @param cntRawBlksClk     the number of clocked correlated data blocks after correlation processing
     * @param cntRawBlksTmsLst  the number of timestamp list correlated data blocks after correlation processing
     * @param durRawDataPrcd    the total time to recover and correlate the requested data
     * @param dblRateRawDataPrcd the data rate for data recovery and correlation
     * @param recOrdering       ordering status of the sorted set of recovered and correlated data  
     * @param recDisTmDom       disjoint time domain status of the sorted set of recovered and correlated data
     * 
     * @return  a new <code>QueryRecoveryResult</code> record populated with the given argument values
     */
    public static QueryRecoveryResult from(
            String          strRecoveryRqstId,
            int             cntRecoveryMsgs,
            long            szRecoveryAlloc,
            int             cntRawBlksTotal,
            int             cntRawBlksClk,
            int             cntRawBlksTmsLst,
            Duration        durRawDataPrcd,
            double          dblRateRawDataPrcd,
            ResultStatus    recOrdering,
            ResultStatus    recDisTmDom
            ) 
    {
        return new QueryRecoveryResult(
                strRecoveryRqstId, 
                cntRecoveryMsgs, 
                szRecoveryAlloc, 
                cntRawBlksTotal, 
                cntRawBlksClk, 
                cntRawBlksTmsLst, 
                durRawDataPrcd, 
                dblRateRawDataPrcd,
                recOrdering,
                recDisTmDom);
    }
    
    /**
     * <p>
     * Create and return a new <code>QueryRecoveryResult</code> record from the products of a time-series data request operation.
     * </p>
     *  
     * @param prcrRqst      the time-series data request processor that performed the operation
     * @param setRawData    the raw correlated data produced by the request processor
     * @param durRecovery   the time taken to perform the recovery and correlation
     * 
     * @return  a new <code>QueryRecoveryResult</code> record with fields extracted from the given arguments
     */
    public static QueryRecoveryResult from(QueryRequestRecoverer prcrRqst, SortedSet<RawCorrelatedData> setRawData, Duration durRecovery) {
        
        // Extract the request processor parameters
        String  strRqstId = prcrRqst.getRequestId();
        int     cntRecoveryMsgs = prcrRqst.getProcessedMessageCount();
        long    szRecovery = prcrRqst.getProcessedByteCount();
        
        // Extract the result set parameters
        int     cntRawBlksTot = setRawData.size();
        int     cntRawBlksClked = setRawData.stream().filter(data -> data instanceof RawClockedData).mapToInt(data -> 1).sum();
        int     cntRawBlksTmsLst = setRawData.stream().filter(data -> data instanceof RawTmsListData).mapToInt(data -> 1).sum();
        
        // Compute performance parameters
        double      dblRateRecovery = ( ((double)szRecovery) * 1000 )/durRecovery.toNanos();

        // Inspect raw data for ordering and collisions
        ResultStatus    recOrdering = TimeDomainProcessor.verifyStartTimeOrdering(setRawData);
        ResultStatus    recDisTmDom = TimeDomainProcessor.verfifyDisjointTimeDomains(setRawData);
        
        QueryRecoveryResult   recResult = QueryRecoveryResult.from(
                strRqstId, 
                cntRecoveryMsgs, 
                szRecovery, 
                cntRawBlksTot, 
                cntRawBlksClked, 
                cntRawBlksTmsLst, 
                durRecovery, 
                dblRateRecovery, 
                recOrdering, 
                recDisTmDom);
        
        return recResult;
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
        ps.println(strPad + "Raw Data Recovery and Correlation");
        ps.println(strPad + "  time-series data request ID                 : " + this.strRecoveryRqstId);
        ps.println(strPad + "  recovered message count                     : " + this.cntRecoveryMsgs);
        ps.println(strPad + "  recovered allocation size (bytes)           : " + this.szRecoveryAlloc);
        ps.println(strPad + "  raw correlated blocks total                 : " + this.cntRawBlksTotal);
        ps.println(strPad + "  raw correlated clocked block count          : " + this.cntRawBlksClk);
        ps.println(strPad + "  raw correlated tms list block count         : " + this.cntRawBlksTmsLst);
        ps.println(strPad + "  recovery and correlation duration           : " + this.durRawDataPrcd);
        ps.println(strPad + "  recovery and correlation rate (MBps)        : " + this.dblRateRawDataPrcd);
        ps.println(strPad + "  raw correlated data domain ordering status  : " + this.recOrdering);
        ps.println(strPad + "  raw correlated data disjoint domain status  : " + this.recDisTmDom);
    }
}
