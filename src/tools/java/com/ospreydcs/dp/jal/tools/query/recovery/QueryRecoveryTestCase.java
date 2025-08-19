/*
 * Project: dp-api-common
 * File:	QueryRecoveryTestCase.java
 * Package: com.ospreydcs.dp.jal.tools.query.correl
 * Type: 	QueryRecoveryTestCase
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
 * @since May 30, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.recovery;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer;
import com.ospreydcs.dp.api.query.model.correl.RawClockedData;
import com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData;
import com.ospreydcs.dp.api.query.model.correl.RawTmsListData;
import com.ospreydcs.dp.api.query.model.request.DataRequestDecomposer;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.query.model.superdom.TimeDomainProcessor;
import com.ospreydcs.dp.jal.tools.query.testrequests.TestArchiveRequest;

/**
 * <p>
 * Record containing parameters for a raw, time-series recovery and correlation test case.
 * </p>
 * <p>
 * Record contains parameters for evaluating the operation and performance of a <code>{@link QueryRequestRecoverer}</code>
 * instance under a variety of conditions defined in the record fields. 
 * The test case record contains fields in 3 specific categories:
 * <ol>
 * <li>Time-series data request definition.</li>
 * <li>Time-series data request decomposition (i.e., for gRPC multi-streaming).</li>
 * <li>Configuration parameters for the <code>{@link QueryRequestRecoverer}</code> instance under evaluation.</li>
 * </ol>
 * </p>
 * <p>
 * The record specifies a time-series data request for the test case, which is
 * taken from the <code>{@link TestArchiveRequest}</code> enumeration.  Note that additional Process Variables (PVs)
 * may also be augmented to the request (although the time range is currently fixed).
 * This request can be decomposed using the record resource <code>{@link #PRCR_RQST_DCMP}</code> according to the
 * request decomposition fields. 
 * Finally, the record contains configuration parameters for raw, time-series data recovery and correlation done by the 
 * <code>QueryRequestRecoverer</code> instance used in the evaluation method 
 * <code>{@link #evaluate(QueryRequestRecoverer)}</code>.
 * </p>
 * <p>
 * <h2>Request Definition</h2>
 * The time-series data request is defined with the following fields:
 * <ul>
 * <li><code>{@link #enmRqst}</code> - a pre-defined Data Platform test archive request from the <code>{@link TestArchiveRequest}</code> enumeration.</li>
 * <li><code>{@link #setSupplPvs}</code> - a collection of unique PV names to augment the above request.
 * <li><code>{@link #enmStrmType}</code> - the gRPC stream type used to recover the request data as described by the <code>{@link DpGrpcStreamType}</code> enumeration.</li>
 * </ul>
 * Note that the gRPC stream type is an attribute of the <code>{@link DpDataRequest}</code> request instance.  However, this 
 * attribute could also be considered a raw data recovery parameter.  
 * Note also that the stream type <code>{@link DpGrpcStreamType#FORWARD}</code> is not allowed for query operations and 
 * will throw an exception.  
 * </p>
 * <p>
 * <h2>Request Decomposition</h2>
 * In order to utilize multiple gRPC data stream for raw time-series data recovery the original time-series data request
 * must be decomposed in smaller composite requests.  Each composite request queries the Data Platform for a subset
 * of the original request and is recovered on a separate, concurrent, gRPC data stream.  The following fields described 
 * the request decomposition process:
 * <ul>
 * <li><code>{@link #bolDcmpEnbl}</code> - enables/disables request decomposition (must be <code>true</code> for gRPC multi-streaming.</li>
 * <li><code>{@link #enmDcmpType}</code> - the request decomposition strategy given by the <code>{@link RequestDecompType}</code> enumeration.</li>
 * <li><code>{@link #cntDcmpMaxPvs}</code> - the maximum allowable number of PVs per composite request.</li>
 * <li><code>{@link #durDcmpMaxRng}</code> - the maximum allowable time range duration per composite request.</li>
 * </ul>
 * Note that all the above fields are used to configure the record resource <code>{@link #PRCR_RQST_DCMP}</code> for 
 * decomposition of the time-series data request defined in this test record.
 * </p>
 * <p>
 * <h2>Raw Data Recovery and Correlation</h2>
 * The remaining <code>QueryRecoveryTestCase</code> fields are used to configure the time-series data request processor,
 * that is, the <code>{@link QueryRequestRecoverer}</code> instance used in method <code>#evaluate(QueryRequestRecoverer)}</code>.
 * The following fields configure the request processor for the use of multiple, concurrent gRPC data streams in raw 
 * data recovery:
 * <ul>
 * <li><code>{@link #bolMStrmEnbl}</code> - enables/disables the use of multiple gRPC data streams in request recovery.</li>
 * <li><code>{@link #szMStrmQryDom}</code> - the minimum (approximate) request query domain size (in PV-Seconds) required for multiple gRPC streams.</li>
 * <li><code>{@link #cntMStrmMaxStrms}</code> - the maximum allowable number of gRPC data streams used for raw data recovery.</li>
 * </ul>
 * The following fields configure the request processor for the raw data correlation operations:
 * <ul>
 * <li><code>{@link #bolCorrRcvry}</code> - enable/disable raw data correlation while data recovery is active.</li>
 * <li><code>{@link #bolCorrConc}</code> - enable/disable concurrency (i.e., multi-threaded) in raw data correlation.</li>
 * <li><code>{@link #szCorrPivot}</code> - minimum target set size required for concurrency, that is, the minimum number of correlated blocks.</li>
 * <li><code>{@link #cntCorrMaxThrds}</code> - maximum allowable number of concurrent processing threads used in raw data correlation.</li>
 * </ul>
 * Note that the concurrency pivot described by field <code>{@link #szCorrPivot}</code> is dynamic.  The processor will "pivot"
 * to concurrent processing when the number of raw, correlated block exceeds this value.  Thus, typically, the correlation
 * process starts as a single thread then pivots to multiple threads whenever the target set exceeds the limit.
 * </p>     
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * The <code>{@link #indCase}</code> field is supplied within creators <code>from(TestArchiveRequest, ...)</code> 
 * and should not be explicitly supplied.  That is, use of the canonical constructor should be avoided.
 * </li>
 * <li>
 * For multiple gRPC data streams to be used in raw data recovery both the <code>{@link #bolDcmpEnbl}</code> and
 * <code>{@link #bolMStrmEnbl}</code> fields must be <code>true</code>.
 * </li>
 * </ul>
 * </p>  
 *
 * @param indCase       the index of the test case
 * 
 * @param enmRqst       the time-series data request supplying raw data
 * @param setSupplPvs   (optional) collection of PV names to be added to the given time-series data request
 * 
 * @param bolDcmpEnbl   time-series data request decomposition enable/disable
 * @param bolDcmpAuto   use automatic request decomposition based upon maximum PV count {@link #cntDcmpMaxPvs} and maximum request duration {@link #durDcmpMaxRng} 
 * @param enmDcmpType   request decomposition strategy used in explicit (non-auto) decomposition (conforms to number of gRPC streams {@link #cntMStrmMaxStrms})
 * @param cntDcmpMaxPvs the maximum number of PV names within a composite data request; used in automatic decomposition
 * @param durDcmpMaxRng the maximum time-range duration for a composite data request; used in automatic decomposition
 * 
 * @param enmStrmType   the gRPC stream type used in raw time-series data recovery
 * 
 * @param bolMStrmEnbl  <s>enable/disable use of multiple gRPC stream in raw time-series data recovery (request decomposition enabled)</s>
 * @param szMStrmQryDom approximate request query size (in PV-Seconds) triggering multiple gRPC data streams in recovery
 * @param cntMStrmMaxStrms maximum number of data streams allowed for multiple gRPC streaming  
 *  
 * @param bolCorrRcvry  enable/disable raw data correlation during data recovery
 * @param bolCorrConc   enable/disable multi-threaded concurrency for correlation processing
 * @param szCorrPivot   the target set size (number of correlated blocks) triggering multi-threaded correlation when enabled
 * @param cntCorrMaxThrds   maximum number of correlator execution threads when concurrency is enabled
 * 
 * @author Christopher K. Allen
 * @since May 30, 2025
 * 
 */
public record QueryRecoveryTestCase(
        int                 indCase,
        TestArchiveRequest  enmRqst,
        Set<String>         setSupplPvs,
        
        boolean             bolDcmpEnbl,
        boolean             bolDcmpAuto,
        RequestDecompType   enmDcmpType,
        int                 cntDcmpMaxPvs,
        Duration            durDcmpMaxRng,
        
        DpGrpcStreamType    enmStrmType,
        
//        boolean             bolMStrmEnbl,
        long                szMStrmQryDom,
        int                 cntMStrmMaxStrms,
        
        boolean             bolCorrRcvry,
        boolean             bolCorrConc,
        int                 szCorrPivot,
        int                 cntCorrMaxThrds
        ) 
{

    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new <code>QueryRecoveryTestCase</code> instance populated from the argument values.
     * </p>
     * <p>
     * This is a convenience creator where no supplementing PV names are added to the given time-series
     * data request.  The field <code>{@link #setSupplPvs}</code> is empty in the returned record.
     * </p>
     * 
     * @param enmRqst       the time-series data request supplying raw data
     * 
     * @param bolDcmpEnbl   time-series data request decomposition enable/disable
     * @param bolDcmpAuto   use automatic request decomposition based upon maximum PV count {@link #cntDcmpMaxPvs} and maximum request duration {@link #durDcmpMaxRng} 
     * @param enmDcmpType   request decomposition strategy used in explicit (non-auto) decomposition (conforms to number of gRPC streams {@link #cntMStrmMaxStrms})
     * @param cntDcmpMaxPvs the maximum number of PV names within a composite data request; used in automatic decomposition
     * @param durDcmpMaxRng the maximum time-range duration for a composite data request; used in automatic decomposition
     * 
     * @param enmStrmType   the gRPC stream type used in raw time-series data recovery
     * 
     * @param bolMStrmEnbl  <s>enable/disable use of multiple gRPC stream in raw time-series data recovery (request decomposition enabled)</s>
     * @param szMStrmQryDom approximate request query size (in PV-Seconds) triggering multiple gRPC data streams in recovery
     * @param cntMStrmMaxStrms maximum number of data streams allowed for multiple gRPC streaming  
     *  
     * @param bolCorrRcvry  enable/disable raw data correlation during data recovery
     * @param bolCorrConc   enable/disable multi-threaded concurrency for correlation processing
     * @param szCorrPivot   the target set size (number of correlated blocks) triggering multi-threaded correlation when enabled
     * @param cntCorrMaxThrds   maximum number of correlator execution threads when concurrency is enabled
     * 
     * @return  a new <code>QueryRecoveryTestCase</code> record with fields given by the arguments
     */
    public static QueryRecoveryTestCase    from(
            TestArchiveRequest  enmRqst,
            
            boolean             bolDcmpEnbl,
            boolean             bolDcmpAuto,
            RequestDecompType   enmDcmpType,
            int                 cntDcmpMaxPvs,
            Duration            durDcmpMaxRng,
            
            DpGrpcStreamType    enmStrmType,
            
//            boolean             bolMStrmEnbl,
            long                szMStrmQryDom,
            int                 cntMStrmMaxStrms,
            
            boolean             bolCorrRcvry,
            boolean             bolCorrConc,
            int                 szCorrPivot,
            int                 cntCorrMaxThrds
            )
    {
        return QueryRecoveryTestCase.from(
                enmRqst, Set.of(), 
                bolDcmpEnbl, bolDcmpAuto, enmDcmpType, cntDcmpMaxPvs, durDcmpMaxRng, 
                enmStrmType, 
                /* bolMStrmEnbl, */ szMStrmQryDom, cntMStrmMaxStrms, 
                bolCorrRcvry, bolCorrConc, szCorrPivot, cntCorrMaxThrds
                ); 
    }
    
    /**
     * <p>
     * Creates a new <code>QueryRecoveryTestCase</code> instance populated from the argument values.
     * </p>
     * <p>
     * The field <code>{@link #indCase}</code> is taken from the record static variable <code>{@link #IND_CASE}</code>
     * The value of <code>{@link #IND_CASE}</code> is incremented in the canonical constructor.
     * </p>
     * 
     * @param enmRqst       the time-series data request supplying raw data
     * @param setSupplPvs   (optional) collection of PV names to be added to the given time-series data request
     * 
     * @param bolDcmpEnbl   time-series data request decomposition enable/disable
     * @param bolDcmpAuto   use automatic request decomposition based upon maximum PV count {@link #cntDcmpMaxPvs} and maximum request duration {@link #durDcmpMaxRng} 
     * @param enmDcmpType   request decomposition strategy used in explicit (non-auto) decomposition (conforms to number of gRPC streams {@link #cntMStrmMaxStrms})
     * @param cntDcmpMaxPvs the maximum number of PV names within a composite data request; used in automatic decomposition
     * @param durDcmpMaxRng the maximum time-range duration for a composite data request; used in automatic decomposition
     * 
     * @param enmStrmType   the gRPC stream type used in raw time-series data recovery
     * 
     * @param bolMStrmEnbl  <s>enable/disable use of multiple gRPC stream in raw time-series data recovery (request decomposition enabled)</s>
     * @param szMStrmQryDom approximate request query size (in PV-Seconds) triggering multiple gRPC data streams in recovery
     * @param cntMStrmMaxStrms maximum number of data streams allowed for multiple gRPC streaming  
     *  
     * @param bolCorrRcvry  enable/disable raw data correlation during data recovery
     * @param bolCorrConc   enable/disable multi-threaded concurrency for correlation processing
     * @param szCorrPivot   the target set size (number of correlated blocks) triggering multi-threaded correlation when enabled
     * @param cntCorrMaxThrds   maximum number of correlator execution threads when concurrency is enabled
     * 
     * @return  a new <code>QueryRecoveryTestCase</code> record with fields given by the arguments
     */
    public static QueryRecoveryTestCase    from(
            TestArchiveRequest  enmRqst,
            Set<String>         setSupplPvs,
            
            boolean             bolDcmpEnbl,
            boolean             bolDcmpAuto,
            RequestDecompType   enmDcmpType,
            int                 cntDcmpMaxPvs,
            Duration            durDcmpMaxRng,
            
            DpGrpcStreamType    enmStrmType,
            
//            boolean             bolMStrmEnbl,
            long                szMStrmQryDom,
            int                 cntMStrmMaxStrms,
            
            boolean             bolCorrRcvry,
            boolean             bolCorrConc,
            int                 szCorrPivot,
            int                 cntCorrMaxThrds
            )
    {
        return new QueryRecoveryTestCase(
                IND_CASE,
                enmRqst,
                setSupplPvs,
                bolDcmpEnbl,
                bolDcmpAuto,
                enmDcmpType,
                cntDcmpMaxPvs,
                durDcmpMaxRng,
                enmStrmType,
//                bolMStrmEnbl,
                szMStrmQryDom,
                cntMStrmMaxStrms,
                bolCorrRcvry,
                bolCorrConc,
                szCorrPivot,
                cntCorrMaxThrds
                );
    }
    
    
    //
    // Record Resources
    //
    
    /** Internal test case index (counter) */
    private static int                          IND_CASE = 1;
    
    
    /** Time-series data request decomposer */
    private static final DataRequestDecomposer  PRCR_RQST_DCMP = DataRequestDecomposer.create();
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>QueryChannelTestCase</code> instance.
     * </p>
     * <p>
     * Canonical constructor.  Sets field values and increments record index counter <code>{@link #IND_CASE}</code>
     * </p>
     *
     * @param enmRqst       the time-series data request supplying raw data 
     * @param bolCorrRcvry    enable/disable multi-threaded concurrency while correlation processing
     * @param cntCorrMaxThrds   maximum number of execution threads when concurrency is enabled
     * @param szCorrPivot   the target set size triggering multi-threading when enabled
     */
    public QueryRecoveryTestCase {
        IND_CASE++;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Evaluates the given request processor for the current test case.
     * </p>
     * 
     * @param processor     query request processor under evaluation
     * 
     * @return  the results of the evaluation
     */
    public QueryRecoveryTestResult evaluate(QueryRequestRecoverer processor) {

        // Configure the processor
        this.configureRecoverer(processor);

        // Create the request ID
        String      strRqstId = this.enmRqst.name();
        if (!this.setSupplPvs.isEmpty())
            strRqstId += " + " + this.setSupplPvs;

        // Create the list of composite requests 
        //  - List contains only a single request if request decomposition is disabled
        //  - Contains any supplemental PVs
        //  - Specifies the gRPC stream type
        DpDataRequest       rqstOrg = this.createRequestOriginal();
        List<DpDataRequest> lstRqsts = this.createRequests(rqstOrg);


        // Attempt the evaluation
        try {
            Instant     insStart = Instant.now();
            SortedSet<RawCorrelatedData>  setRawData = processor.processRequests(lstRqsts); // throws DpQueryException
            Instant     insFinish = Instant.now();

            // Collect the results
            Duration    durDataPrcd = Duration.between(insStart, insFinish);
            long        szAllocPrcd = processor.getProcessedByteCount();
            double      dblRatePrcd = ( ((double)szAllocPrcd) * 1000 )/durDataPrcd.toNanos();

            int         cntRcvrdMsgs = processor.getProcessedMessageCount();
            int         cntBlksPrcdTot = setRawData.size();
            int         cntBlksPrcdClkd = setRawData.stream().filter(blk -> blk instanceof RawClockedData).mapToInt(blk -> 1).sum();
            int         cntBlksPrcdTmsLst = setRawData.stream().filter(blk -> blk instanceof RawTmsListData).mapToInt(blk -> 1).sum();

            // Inspect raw data for ordering and collisions
            ResultStatus    recBlksOrdered = TimeDomainProcessor.verifyStartTimeOrdering(setRawData);
            ResultStatus    recBlksDisTmDom = TimeDomainProcessor.verfifyDisjointTimeDomains(setRawData);


            // Return the test results
            QueryRecoveryTestResult    recResult = QueryRecoveryTestResult.from(
                    strRqstId, ResultStatus.SUCCESS, 
                    rqstOrg, lstRqsts, 
                    cntRcvrdMsgs, szAllocPrcd, 
                    durDataPrcd, dblRatePrcd, 
                    cntBlksPrcdTot, cntBlksPrcdClkd, cntBlksPrcdTmsLst, 
                    recBlksOrdered, recBlksDisTmDom, 
                    this);

            return recResult;

        } catch (Exception e) {

            // Create test failure status
            String          strErrMsg = e.getCause().getMessage();
            ResultStatus    recFailStatus = ResultStatus.newFailure(strErrMsg, e);

            // Return a failed test result
            QueryRecoveryTestResult recResult = QueryRecoveryTestResult.from(strRqstId, recFailStatus, rqstOrg, lstRqsts, this);

            return recResult;
        }
    }
    
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
        
        ps.println(strPad + this.getClass().getSimpleName() + " " + this.indCase + " Conditions");
        ps.println(strPad + "  Original Data Request ID          : " + this.enmRqst.name());
        ps.println(strPad + "  Supplemental PV names             : " + this.setSupplPvs);
        ps.println(strPad + "  Request Decomposition");
        ps.println(strPadd + "  enabled                      : " + this.bolDcmpEnbl);
        ps.println(strPadd + "  automatic decomposition      : " + this.bolDcmpAuto);
        ps.println(strPadd + "  non-auto decomp. strategy    : " + this.enmDcmpType);
        ps.println(strPadd + "  auto max composite PV count  : " + this.cntDcmpMaxPvs);
        ps.println(strPadd + "  auto max composite time rng. : " + this.durDcmpMaxRng);
        ps.println(strPad + "  Raw Data Recovery");
        ps.println(strPadd + "  gRPC stream type                  : " + this.enmStrmType);
//        ps.println(strPadd + "  multi-Streaming (MS) enabled      : " + this.bolMStrmEnbl);
        ps.println(strPadd + "  multi-Streaming (MS) enabled      : " + this.bolDcmpEnbl);
        ps.println(strPadd + "  MS request domain size (PV-Secs)  : " + this.szMStrmQryDom);
        ps.println(strPadd + "  MS maximum gRPC stream count      : " + this.cntMStrmMaxStrms);
        ps.println(strPad + "  Raw Data Correlation");
        ps.println(strPadd + "  correlate during recovery enabled : " + this.bolCorrRcvry);
        ps.println(strPadd + "  concurrency (multi-thrd) enabled  : " + this.bolCorrConc);
        ps.println(strPadd + "  concurrency target set size pivot : " + this.szCorrPivot);
        ps.println(strPadd + "  concurrency maximum thread count  : " + this.cntCorrMaxThrds);
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Configures the given query request processor to the conditions described in this test case.
     * </p>
     * <p>
     * The <code>{@link QueryRequestRecoverer}</code> instance used in <code>{@link #evaluate(QueryRequestRecoverer)}</code>
     * should be passed to this method for configuration using the record field values.
     * <p>
     * <h2>Raw Data Recovery and Correlation</h2>
     * Some <code>QueryRecoveryTestCase</code> fields are used to configure the time-series data request processor,
     * specifically, the <code>{@link QueryRequestRecoverer}</code> instance used in method <code>#evaluate(QueryRequestRecoverer)}</code>.
     * </p>
     * <p>
     * The following field(s) configure the request processor to allow for request decomposition:
     * <ul>
     * <li><code>{@link #bolDcmpEnbl}</code> - enables/disables request decomposition (must be <code>true</code> for gRPC multi-streaming.</li>
     * </ul>
     * The following fields configure the request processor for the use of multiple, concurrent gRPC data streams in raw 
     * data recovery:
     * <ul>
     * <li><code>{@link #bolMStrmEnbl}</code> - enables/disables the use of multiple gRPC data streams in request recovery.</li>
     * <li><code>{@link #szMStrmQryDom}</code> - the minimum (approximate) request query domain size (in PV-Seconds) required for multiple gRPC streams.</li>
     * <li><code>{@link #cntMStrmMaxStrms}</code> - the maximum allowable number of gRPC data streams used for raw data recovery.</li>
     * </ul>
     * The following fields configure the request processor for the raw data correlation operations:
     * <ul>
     * <li><code>{@link #bolCorrRcvry}</code> - enable/disable raw data correlation while data recovery is active.</li>
     * <li><code>{@link #bolCorrConc}</code> - enable/disable concurrency (i.e., multi-threaded) in raw data correlation.</li>
     * <li><code>{@link #szCorrPivot}</code> - minimum target set size required for concurrency, that is, the minimum number of correlated blocks.</li>
     * <li><code>{@link #cntCorrMaxThrds}</code> - maximum allowable number of concurrent processing threads used in raw data correlation.</li>
     * </ul>
     * </p>
     * 
     * @param prcr  the request processor used in the <code>{@link #evaluate(QueryRequestRecoverer)}</code> method
     */
    private void configureRecoverer(QueryRequestRecoverer prcr) {
        
        // Request decomposition configuration 
        prcr.enableRequestDecomposition(this.bolDcmpEnbl);
        prcr.setRequestDecompMaxPvCount(this.cntDcmpMaxPvs);
        prcr.setRequestDecompMaxTimeRange(this.durDcmpMaxRng);
        
        // Raw data recovery configuration
//        prcr.enableMultiStreaming(this.bolMStrmEnbl);
        prcr.enableMultiStreaming(this.bolDcmpEnbl);
        prcr.setMultiStreamingDomainSize(this.szMStrmQryDom);
        prcr.setMultiStreamingMaxStreamCount(this.cntMStrmMaxStrms);
        
        // Raw data correlation configuration
        prcr.enableCorrelateWhileStreaming(this.bolCorrRcvry);
        prcr.enableCorrelateConcurrency(this.bolCorrConc);
        prcr.setCorrelateConcurrencyPivotSize(this.szCorrPivot);
        prcr.setCorrelateConcurrencyMaxThreads(this.cntCorrMaxThrds);
    }
    
    /**
     * <p>
     * Creates the originating time-series data request.
     * </p>
     * <p>
     * Performs the following steps to create the originating data request:
     * <ol>
     * <li>Creates a <code>{@link DpDataRequest}</code> instance from <code>{@link TestArchiveRequest#create()}</code>.</li>
     *   <ul>
     *   <li>Adds all supplemental PVs to <code>DpDataRequest</code> from <code>{@link #setSupplPvs}</code>. </li>
     *   <li>Sets the gRPC stream type for the request from <code>{@link #enmStrmType}</code>. </li> 
     *   </ul>
     * </ol>
     * </p>
     *  
     * @return  the originating time-series data request for the test case
     */
    private DpDataRequest   createRequestOriginal() {
        
        // Create the original request and add any supplemental PVs
        DpDataRequest   rqst = this.enmRqst.create();
        rqst.setStreamType(this.enmStrmType);
        rqst.selectSources(this.setSupplPvs);
        
        return rqst;
    }
    
    /**
     * <p>
     * Creates the list of composite requests for the test case.
     * </p>
     * <p>
     * Creates a list of composite time-series data requests according to the test case configuration.
     * Performs the following steps to create the returned composite request list:
     * <ol>
     * <li>Creates a <code>{@link DpDataRequest}</code> instance from <code>{@link #createRequestOriginal()}</code> .</li>
     * <li>If the <code>{@link #bolDcmpEnbl()}</code> flag is <code>false</code> then the above <code>DpDataRequest</code> is returned 
     *     within a single-element <code>List</code>.
     * </li>
     * <li>If the <code>{@link #bolDcmpAuto()}</code> is <code>true</code> then the following actions are performed:
     *   <ol>
     *   <li>The resource <code>{@link #PRCR_RQST_DCMP}</code> is configured with <code>{@link #configureDecomposer(DataRequestDecomposer)}</code>. </li>
     *   <li>The request is decomposed with <code>{@link DataRequestDecomposer#buildCompositeRequestPreferred(DpDataRequest)}</code>. </li>
     *   <li>The composite requests are returned as a <code>List</code>. </li>
     *   </ol>
     * <li>If we are here then the request is decomposed using <code>{@link DataRequestDecomposer#buildCompositeRequest(DpDataRequest, RequestDecompType, int)}</code>
     *     where the last arguments are <code>{@link #enmDcmpType}</code> and <code>{@link #cntMStrmMaxStrms}</code>.
     * </li>
     * </ol>
     * </p>
     * 
     * @return  a list of composite time-series data requests that create the original request defined by the test case 
     */
    private List<DpDataRequest> createRequests(DpDataRequest rqstOrg) {
        
        // Create the original request and add any supplemental PVs
//        DpDataRequest   rqst = this.enmRqst.create();
//        rqst.setStreamType(this.enmStrmType);
//        rqst.selectSources(this.setSupplPvs);
        
        // If request decomposition is disabled return 
        if (!this.bolDcmpEnbl)
            return List.of(rqstOrg);
        
        // Else decompose request automatically if enabled
        if (this.bolDcmpAuto()) {
            this.configureDecomposer(QueryRecoveryTestCase.PRCR_RQST_DCMP);
            List<DpDataRequest> lstRqsts = QueryRecoveryTestCase.PRCR_RQST_DCMP.buildCompositeRequestPreferred(rqstOrg);
            
            return lstRqsts;
        }
            
        // Else decompose request explicitly
        List<DpDataRequest> lstRqsts = QueryRecoveryTestCase.PRCR_RQST_DCMP.buildCompositeRequest(rqstOrg, this.enmDcmpType, this.cntMStrmMaxStrms);
        
        return lstRqsts;
    }

    /**
     * <p>
     * Configures the given request decomposer to the conditions described in this test case.
     * </p>
     * <p>
     * <ul>
     * <li><code>{@link #bolDcmpEnbl}</code> - enables/disables request decomposition (must be <code>true</code> for gRPC multi-streaming.</li>
     * <li><code>{@link #cntDcmpMaxPvs}</code> - the maximum allowable number of PVs per composite request.</li>
     * <li><code>{@link #durDcmpMaxRng}</code> - the maximum allowable time range duration per composite request.</li>
     * </ul>
     * </p>
     * 
     * @param prcr  the time-series data request decomposer to be configured
     */
    private void configureDecomposer(DataRequestDecomposer prcr) {
        
        // Request decomposer configuration
        prcr.enable(this.bolDcmpEnbl);
        prcr.setMaxDataSources(this.cntDcmpMaxPvs);
        prcr.setMaxDuration(this.durDcmpMaxRng);
    }
    
}
