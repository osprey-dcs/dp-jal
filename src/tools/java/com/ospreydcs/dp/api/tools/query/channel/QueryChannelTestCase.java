/*
 * Project: dp-api-common
 * File:	QueryChannelTestCase.java
 * Package: com.ospreydcs.dp.api.tools.query.channel
 * Type: 	QueryChannelTestCase
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
 * @since May 9, 2025
 *
 */
package com.ospreydcs.dp.api.tools.query.channel;

import java.io.PrintStream;
import java.lang.module.ResolutionException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionException;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.model.grpc.QueryChannel;
import com.ospreydcs.dp.api.query.model.grpc.QueryMessageBuffer;
import com.ospreydcs.dp.api.query.model.request.DataRequestDecomposer;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.tools.query.request.TestArchiveRequest;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Record containing parameters for a time-series data request streaming recovery.
 * </p>
 * <p>
 * The record fields encapsulate the parameters of a <code>QueryChannel</code> streaming time-series 
 * data request recovery operation.  The performance of the recovery operation can be measured and
 * reported against the record fields.
 * </p> 
 * <p>
 * <h2>NOTES:</h2>
 * The canonical constructor should not be used as the field <code>{@link #lstCmpRqsts}</code> is supplied
 * internally.
 * The <code>{@link #lstCmpRqsts}</code> field is created within creator 
 * <code>{@link #from(DpDataRequest, RequestDecompType, DpGrpcStreamType, int)}</code> and should not be 
 * explicitly supplied.
 * </p>
 *
 * @param   indCase     the test case index
 * @param   enmRqstOrg  the original time-series data request
 * @param   enmDcmpType the request domain decomposition type
 * @param   enmStrType  the gRPC data stream type used to recover the request data
 * @param   cntStrms    the number of gRPC streams used for request data recovery
 * @param   lstCmpRqsts the resulting composite time-series data request (from creator)
 *
 * @author Christopher K. Allen
 * @since May 9, 2025
 *
 */
public record QueryChannelTestCase(
        int                 indCase,
        TestArchiveRequest  enmRqstOrg, 
        RequestDecompType   enmDcmpType, 
        DpGrpcStreamType    enmStrmType, 
        int                 cntStrms, 
        List<DpDataRequest> lstCmpRqsts
        ) 
{
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new instances of <code>TestCase</code> from the given parameters.
     * </p>
     * 
     * @param   enmRqstOrg  the original time-series data request
     * @param   enmDcmpType the request domain decomposition type
     * @param   enmStrType  the gRPC data stream type used to recover the request data
     * @param   cntStrms    the number of gRPC streams used for request data recovery
     *
     * @return  a new, initialized instance of <code>TestCase</code>
     * 
     * @throws  UnsupportedOperationException   an unexpected <code>RequestDecompType</code> enumeration was encountered    
     */
    public static QueryChannelTestCase  from(
                                            TestArchiveRequest enmRqstOrg, 
                                            RequestDecompType enmRqstDcmp, 
                                            DpGrpcStreamType enmStrmType, 
                                            int cntStrms) 
            throws UnsupportedOperationException {
    
        List<DpDataRequest> lstRqsts = PRCR_DECOMP.buildCompositeRequest(enmRqstOrg.create(), enmRqstDcmp, cntStrms);   // throws exception
        
        lstRqsts.forEach(r -> r.setRequestId(enmRqstOrg.name()));
        lstRqsts.forEach(r -> r.setStreamType(enmStrmType));
        
        return new QueryChannelTestCase(IND_CASE, enmRqstOrg, enmRqstDcmp, enmStrmType, cntStrms, lstRqsts);
    }
    
    
    //
    // Record Resources
    //
    
    /** Request decomposer used in non-canonical construction/creation */
    private static final    DataRequestDecomposer   PRCR_DECOMP = DataRequestDecomposer.create();
    
    /** Internal test case index (counter) */
    private static          int IND_CASE = 1;
    
    
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
     * @param   indCase     the test case index
     * @param   enmRqstOrg     the original time-series data request
     * @param   enmDcmpType the request domain decomposition type
     * @param   enmStrType  the gRPC data stream type used to recover the request data
     * @param   cntStrms    the number of gRPC streams used for request data recovery
     * @param   lstCmpRqsts the resulting composite time-series data request (from creator)
     */
    public QueryChannelTestCase {
        IND_CASE++;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Evaluates the given <code>QueryChannel</code> with attached <code>QueryMessageBuffer</code> for this test case.
     * </p>
     * <p>
     * The test case data recovery described by this record is performed on the given <code>QueryChannel</code>.
     * The performance of the evaluation is measured and returned as a <code>QueryChannelTestResult</code> record.
     * The provided <code>QueryMessageBuffer</code> is assumed to be attached to the <code>QueryChannel</code>
     * argument and all recovered data is likewise assumed to be present at recovery termination.   
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The given <code>QueryMessageBuffer</code> must be the <code>IMessageConsumer</code> supplied to the given
     * <code>QueryChannel</code> at its creation for a valid evaluation.
     * </li>
     * <li>
     * The <code>QueryMessageBuffer</code> instance will be shut down and empty when this method returns.
     * </li>
     * </ul>
     * </p>
     * 
     * @param chanQuery the Query Service channel under evaluation
     * @param bufMsgs   the <code>QueryData</code> message buffer receive all recovered data
     * 
     * @return  the results of this test case performance evaluation
     * 
     * @throws DpQueryException     general exception during data recovery (see message and cause)
     * @throws InterruptedException the evaluation was interrupted while waiting for completion
     * @throws CompletionException  the message buffer consumer failed (see message and cause)
     * @throws ResolutionException  the message buffer failed to empty upon completion
     */
    public QueryChannelTestResult   evaluate(QueryChannel chanQuery, QueryMessageBuffer bufMsgs) 
            throws DpQueryException, InterruptedException, CompletionException, ResolutionException {

        // We need a consumer of QueryData messages from the buffer to avoid heap exhaustion for large requests
        BufferConsumerTask  tskCnsmr = BufferConsumerTask.from(bufMsgs);
        Thread              thdMsgCnsmr = new Thread(tskCnsmr);
        
        // Activate the message buffer and consumer in order to recover data without blocking/heap exhaustion
        bufMsgs.activate();
        thdMsgCnsmr.start();
        
        // Perform timed data recovery
        Instant insStart = Instant.now();
        chanQuery.recoverRequests(this.lstCmpRqsts);  // throws DpQueryException
        Instant insFinish = Instant.now();
        
        // Shutdown buffer - all messages have been recovered
        bufMsgs.shutdown();

        // Wait for message consumer to complete and check status
        thdMsgCnsmr.join();                         // throws InterruptedException
        if (tskCnsmr.getResult().isFailure()) {
            ResultStatus    recStatus = tskCnsmr.getResult();
            
            throw new CompletionException(recStatus.message(), recStatus.cause());
        }
        
        // Recover/Compute performance parameters
        Duration    durRecovery = Duration.between(insStart, insFinish);
        int         cntMsgs = tskCnsmr.getMessageCount();
        long        szRecovery = tskCnsmr.getMemoryAllocation();
        double      dblRate = ( ((double)szRecovery) * 1000 )/durRecovery.toNanos();
        
        // Shut down message consumer hard - this is redundant
        if (bufMsgs.getQueueSize() > 0) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                           + " - Test case #" + this.indCase + " had non-empty message buffer size "
                           + bufMsgs.getQueueSize() + " after completion.";
            
            bufMsgs.shutdownNow();
            throw new ResolutionException(strMsg);
        }
        
        // Consolidate performance data to result record and return
        QueryChannelTestResult  recResult = QueryChannelTestResult.from(dblRate, cntMsgs, szRecovery, durRecovery, this);
        
        return recResult;
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
        
        ps.println(strPad + this.getClass().getSimpleName() + " " + this.indCase + ":");
        ps.println(strPad + "  Original request ID : " + this.enmRqstOrg.name());
        ps.println(strPad + "  Request decomposition type : " + this.enmDcmpType);
        ps.println(strPad + "  gRPC stream type           : " + this.enmStrmType);
        ps.print(  strPad + "  gRPC stream count          : " + this.cntStrms);
        if (this.enmDcmpType == RequestDecompType.NONE)
            ps.println(" (decomposition NONE, only 1 used)");
        else
            ps.println();
        
//        ps.println(strPad + "  Original Request Properties:");
//        this.enmRqstOrg.create().printOutProperties(ps, strPad + strPad);
    }
    
    
    //
    // Support Methods
    //
}
