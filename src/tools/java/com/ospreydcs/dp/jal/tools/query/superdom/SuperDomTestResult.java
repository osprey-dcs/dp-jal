/*
 * Project: dp-api-common
 * File:	SuperDomTestResult.java
 * Package: com.ospreydcs.dp.jal.tools.query.superdom
 * Type: 	SuperDomTestResult
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
 * @since Jun 17, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.superdom;

import java.io.PrintStream;
import java.time.Duration;
import java.util.List;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.common.TimeInterval;

/**
 * <p>
 * Record containing the results of a super domain test case.
 * </p>
 * <p>
 * <h2>Evaluation Cases and Results</h2>
 * Super domain test cases are contained in <code{@link SuperDomTestCase}</code> records.  The results of
 * a test case are provided by the method 
 * <code>@link SuperDomTestCase#evaluate(com.ospreydcs.dp.api.query.impl.QueryRequestProcessorNew)}</code>
 * which performs the test case evaluation.
 * The results of the method are returned in a <code>SuperDomTestResult</code> record. 
 * </p>
 * <p>
 * <h2>Test Case Evaluation</h2>
 * There are essentially two operation within a <code>SuperDomTestCase</code>: 1) recovery and correlation
 * of the time-series data request, and 2) super domain processing of the correlated data.
 * The recovery and correlation recovers all raw data from the Query Service as <code>QueryData</code>
 * Protocol Buffers messages, then correlates the data by timestamps.  That is, all data belonging to
 * a set of timestamps (either a sample clock or an explicit timestamp list) is identified and assigned
 * to the timestamps.
 * The super domain processing inspects the timestamps of all correlated data for time-domain collisions.
 * Time-domain collisions are typically caused by process variables sampled during the same time interval
 * but with different sample periods.  The super domain processor identifies all correlated data with time-domain
 * collisions and separates them into "super domains."  The remaining correlated data (i.e., without collisions
 * is left as is.  The result is a collection of data with disjoint time domains, either standard timestamped
 * domains, or super domains.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jun 17, 2025
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
 * @param cntDisBlksTotal   total number of disjoint data blocks (raw and super domain) after super domain processing
 * @param cntDisRawBlks     the number of disjoint raw data blocks after super domain processing
 * @param cntSupDomBlks     the number of super domain blocks after super domain processing
 * @param durSupDomPrcd     the time taken to perform super domain processing
 * @param dblRateSupDomPrcd the rate of super domain processing (blocks/second)
 * @param lstRawDoms        list of raw data time domains after super domain processing
 * @param lstSupDoms        list of super domains after super domain processing
 * @param recCase           the test case performed
 */
public record SuperDomTestResult(
        String          strRecoveryRqstId,
        int             cntRecoveryMsgs,
        long            szRecoveryAlloc,
        int             cntRawBlksTotal,
        int             cntRawBlksClk,
        int             cntRawBlksTmsLst,
        Duration        durRawDataPrcd,
        double          dblRateRawDataPrcd,
        ResultStatus    recOrdering,
        ResultStatus    recDisTmDom,
        int             cntDisBlksTotal,
        int             cntDisRawBlks,
        int             cntSupDomBlks,
        Duration        durSupDomPrcd,
        double          dblRateSupDomPrcd,
        List<TimeInterval>  lstRawDoms,
        List<TimeInterval>  lstSupDoms,
        SuperDomTestCase    recCase
        ) 

{
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new <code>SuperDomTestResult</code> record populated with the given arguments.
     * </p>
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
     * @param cntDisBlksTotal   total number of disjoint data blocks (raw and super domain) after super domain processing
     * @param cntDisRawBlks     the number of disjoint raw data blocks after super domain processing
     * @param cntSupDomBlks     the number of super domain blocks after super domain processing
     * @param durSupDomPrcd     the time taken to perform super domain processing
     * @param dblRateSupDomPrcd the rate of super domain processing (blocks/second)
     * @param lstRawDoms        list of raw data time domains after super domain processing
     * @param lstSupDoms        list of super domains after super domain processing
     * @param recCase           the test case performed
     * 
     * @return
     */
    public static SuperDomTestResult    from(
            String          strRecoveryRqstId,
            int             cntRecoveryMsgs,
            long            szRecoveryAlloc,
            int             cntRawBlksTotal,
            int             cntRawBlksClk,
            int             cntRawBlksTmsLst,
            Duration        durRawDataPrcd,
            double          dblRateRawDataPrcd,
            ResultStatus    recOrdering,
            ResultStatus    recDisTmDom,
            int             cntDisBlksTotal,
            int             cntDisRawBlks,
            int             cntSupDomBlks,
            Duration        durSupDomPrcd,
            double          dblRateSupDomPrcd,
            List<TimeInterval>  lstRawDoms,
            List<TimeInterval>  lstSupDoms,
            SuperDomTestCase    recCase
            )
    {
        return new SuperDomTestResult(
                strRecoveryRqstId,
                cntRecoveryMsgs,
                szRecoveryAlloc,
                cntRawBlksTotal,
                cntRawBlksClk,
                cntRawBlksTmsLst,
                durRawDataPrcd,
                dblRateRawDataPrcd,
                recOrdering,
                recDisTmDom,
                cntDisBlksTotal,
                cntDisRawBlks,
                cntSupDomBlks,
                durSupDomPrcd,
                dblRateSupDomPrcd,
                lstRawDoms,
                lstSupDoms,
                recCase
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
        ps.println(strPad + "Test Results for case #" + this.recCase.indCase());
        ps.println(strPad + "  Time-series data request ID            : " + this.strRecoveryRqstId);
        ps.println(strPad + "  Recovery message count                 : " + this.cntRecoveryMsgs);
        ps.println(strPad + "  Recovery allocation (bytes)            : " + this.szRecoveryAlloc);
        ps.println(strPad + "  Raw correlated blocks total            : " + this.cntRawBlksTotal);
        ps.println(strPad + "  Raw correlated blocks clocked          : " + this.cntRawBlksClk);
        ps.println(strPad + "  Raw correlated blocks timestamp list   : " + this.cntRawBlksTmsLst);
        ps.println(strPad + "  Recovery and correlation duration      : " + this.durRawDataPrcd);
        ps.println(strPad + "  Recovery and correlation rate (MBps)   : " + this.dblRateRawDataPrcd);
        ps.println(strPad + "  Raw correlated data ordering status    : " + this.recOrdering);
        ps.println(strPad + "  Raw correlated data time-doamin status : " + this.recDisTmDom);
        ps.println(strPad + "  Disjoint data blocks total (raw & sd)  : " + this.cntDisBlksTotal);
        ps.println(strPad + "  Disjoint raw correlated data blocks    : " + this.cntDisRawBlks);
        ps.println(strPad + "  Disjoint super-domain raw data blocks  : " + this.cntSupDomBlks);
        ps.println(strPad + "  Super-domain processing duration       : " + this.durSupDomPrcd);
        ps.println(strPad + "  Super-domain processing rate (MBps)    : " + this.dblRateSupDomPrcd);
        
        // Print out disjoint raw correlated data time domains
        ps.println(strPad + "  Disjoint raw correlated data time domains:");
        for (TimeInterval tvlDomain : this.lstRawDoms) 
            ps.println(strPad + "    " + tvlDomain);
        
        // Print out disjoint raw data super-domains
        ps.println(strPad + "  Disjoint super-domain time domains:");
        for (TimeInterval tvlDomain : this.lstSupDoms)
        ps.println(strPad + "    " + tvlDomain);
        
        // Print out the test case record
        ps.println(strPad + "  Test Case #" + this.recCase.indCase());
        this.recCase.printOut(ps, strPad + "  ");
    }
}
