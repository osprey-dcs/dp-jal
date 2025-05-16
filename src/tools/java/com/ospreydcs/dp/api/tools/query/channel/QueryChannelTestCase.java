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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.model.IMessageSupplier;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.model.grpc.QueryChannel;
import com.ospreydcs.dp.api.query.model.grpc.QueryMessageBuffer;
import com.ospreydcs.dp.api.query.model.request.DataRequestDecomposer;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.tools.query.request.TestArchiveRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

/**
 * <p>
 * Record containing all the parameters of a time-series data request streaming data recovery.
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
        
        lstRqsts.forEach(r -> r.setStreamType(enmStrmType));
        
        return new QueryChannelTestCase(IND_CASE, enmRqstOrg, enmRqstDcmp, enmStrmType, cntStrms, lstRqsts);
    }
    
    
    //
    // Record Resources
    //
    
    /** Request decomposer used in non-canonical construction/creation */
    private static final    DataRequestDecomposer   PRCR_DECOMP = DataRequestDecomposer.create();
    
    /** Internal test case index (counter) */
    private static          int IND_CASE = 0;
    
    
    //
    // Record Types
    //
    
    /**
     * <p>
     * Task implementation that consumes <code>{@link QueryDataResponse.QueryData}</code> messages from a supplier.
     * </p> 
     * <p>
     * The task simply consumes <code>QueryData</code> Protocol Buffers message from an attached 
     * <code>{@link IMessageSupplier}</code> instance supplying them.  The message are recorded then
     * discarded so as to avoid heap space issues for large data streams.
     * </p>
     * <p>
     * <h2>Operation</h2>
     * When activated, the task enters a continuous loop guarded by <code>{@link IMessageSupplier#isSupplying()}</code>
     * and begins a timed polling of the supplier using <code>{@link IMessageSupplier#poll(long, TimeUnit)}</code>.
     * The timeout limits are given by class constants <code>{@link #LNG_TIMEOUT}</code> and 
     * <code>{@link #TU_TIMEOUT}</code>.  The timed polling is used to avoid indefinite block on a 
     * <code>{@link IMessageSupplier#take()}</code> that would occur if the state of the supplier changed while
     * waiting.
     * </p> 
     *
     * @author Christopher K. Allen
     * @since May 16, 2025
     *
     */
    public static class BufferConsumerTask implements Runnable {

        //
        // Creators
        //
        
        /**
         * <p>
         * Creates a new <code>BufferConsumerTask</code> instance attached to the given message supplier.
         * </p>
         * <p>
         * The returned instance is ready for activation as an independent thread task.  The given message
         * supplier must be active before the returned task is run.  Specifically, the value returned
         * by <code>{@link IMessageSupplier#isSupplying()}</code> must be <code>true</code> before 
         * executing the returned task.
         * </p>
         * 
         * @param fncSupplier   the supplier (source) of <code>QueryData</code> Protocol Buffers messages
         * 
         * @return  a new <code>BufferConsumerTask</code> ready for task execution 
         */
        public static BufferConsumerTask    from(IMessageSupplier<QueryDataResponse.QueryData> fncSupplier) {
            return new BufferConsumerTask(fncSupplier);
        }
        
        /**
         * <p>
         * Creates a new <code>Thread</code> instance for independent execution of a <code>BufferConsumerTask</code> task.
         * </p>
         * <p>
         * This is a convenience creator that creates a new <code>{@link Thread}</code> instance that executes
         * a new <code>{@link BufferConsumerTask}</code> instance.  That is, a <code>BufferConsumerTask</code>
         * is first created then supplied to the constructor <code>{@link Thread#Thread(Runnable)}</code>.
         * </p>
         * 
         * @param fncSupplier   the supplier (source) of <code>QueryData</code> Protocol Buffers messages
         * 
         * @return  a new <code>Thread</code> instance supporting a <code>BufferConsumerTask</code>
         * 
         * @see #from(IMessageSupplier)
         */
        public static Thread    threadFrom(IMessageSupplier<QueryDataResponse.QueryData> fncSupplier) {
            BufferConsumerTask  task = new BufferConsumerTask(fncSupplier);
            Thread              thdTask = new Thread(task);
            
            return thdTask;
        }
        
        //
        // Class Constants
        //
        
        /** The polling timeout limit */
        private static final long       LNG_TIMEOUT = 15;
        
        /** the polling timeout units */
        private static final TimeUnit   TU_TIMEOUT = TimeUnit.MILLISECONDS;
        
        
        //
        // Initializing Attributes
        //
        
        /** The supplier of Query Service data messages - typically a <code>QueryMessageBuffer</code> */
        private final IMessageSupplier<QueryDataResponse.QueryData> fncSupplier;
        
        
        //
        // State Variables
        //
        
        /** The number of messages recovered */
        private int     cntMsgs = 0;
        
        /** The total memory allocation recovered */
        private long    szAlloc = 0;
        
        
        /** The executed flag */
        private boolean         bolRun = false;
        
        /** The task success flag */
        private ResultStatus    recStatus;
        
        
        
        //
        // Constructors
        //
        
        /**
         * <p>
         * Constructs a new <code>BufferConsumerTask</code> instance attached to the given message supplier.
         * </p>
         *
         * @param fncSupplier   the supplier (source) of <code>QueryData</code> Protocol Buffers messages
         */
        public BufferConsumerTask(IMessageSupplier<QueryDataResponse.QueryData> fncSupplier) {
            this.fncSupplier = fncSupplier;
        }
        
        
        //
        // State and Status Inquiry
        //
        
        /**
         * <p>
         * Returns the number of <code>QueryData</code> messages recovered during task execution.
         * </p>
         * 
         * @return  the message count obtained by the buffer consumption task
         * 
         * @throws IllegalStateException    the task has not been executed
         */
        public int   getMessageCount() throws IllegalStateException {
            
            // Check state
            if (!this.bolRun)
                throw new IllegalStateException("Task has not been executed.");
            
            return this.cntMsgs;
        }
        
        /**
         * <p>
         * Returns the total memory allocation recovered during task execution.
         * </p>
         * 
         * @return  the memory size (in Bytes) obtained by the buffer consumption task
         * 
         * @throws IllegalStateException    the task has not been executed
         */
        public long getMemoryAllocation() throws IllegalStateException {
            
            // Check state
            if (!this.bolRun)
                throw new IllegalStateException("Task has not been executed.");
            
            return this.szAlloc;
        }
        
        /**
         * <p
         * Returns the result of the buffer consumption task.
         * </p>
         * 
         * @return  <code>{@link ResultStatus#SUCCESS}</code> if successful, 
         *          otherwise a failure message and exception cause
         *          
         * @throws IllegalStateException    the task has not been executed
         */
        public ResultStatus getResult() throws IllegalStateException {
            
            // Check state
            if (!this.bolRun)
                throw new IllegalStateException("Task has not been executed.");

            return this.recStatus;
        }
        
        
        //
        // Runnable Interface
        //
        
        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            
            while (fncSupplier.isSupplying()) {
                try {
                    QueryDataResponse.QueryData msgData = fncSupplier.poll(LNG_TIMEOUT, TU_TIMEOUT);
                    
                    if (msgData == null)
                        continue;
                    
                    this.szAlloc += msgData.getSerializedSize();
                    this.cntMsgs++;
                    
                } catch (IllegalStateException e) {
                    this.recStatus = ResultStatus.newFailure("The message supplier reported NOT READY.", e);
                    return;
                    
                } catch (InterruptedException e) {
                    this.recStatus = ResultStatus.newFailure("Polling was interrupted while waiting for message.", e);
                    return;
                }
            }
            
            this.bolRun = true;
            this.recStatus = ResultStatus.SUCCESS;
        }
        
    }
    
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
     */
    public QueryChannelTestResult   evaluate(QueryChannel chanQuery, QueryMessageBuffer bufMsgs) 
            throws DpQueryException, InterruptedException, CompletionException {
        
        // Activate the message consumer in order to receive recovered data without blocking
        BufferConsumerTask  tskCnsmr = BufferConsumerTask.from(bufMsgs);
        Thread              thdMsgCnsmr = new Thread(tskCnsmr);
        
        bufMsgs.activate();
        thdMsgCnsmr.start();
        
        // Perform timed data recovery
        Instant insStart = Instant.now();
        int     cntMsgs = chanQuery.recoverRequests(this.lstCmpRqsts);  // throws DpQueryException
        Instant insFinish = Instant.now();
        
        // Wait for message consumer to complete and check status
        bufMsgs.shutdown();
        thdMsgCnsmr.join();                         // throws InterruptedException
        if (tskCnsmr.getResult().isFailure()) {
            ResultStatus    recStatus = tskCnsmr.getResult();
            
            throw new CompletionException(recStatus.message(), recStatus.cause());
        }
        
        // Recover/Compute performance parameters
        Duration    durRecovery = Duration.between(insStart, insFinish);
        long        szRecovery = tskCnsmr.getMemoryAllocation();
//        long        szRecovery = bufMsgs.computeQueueAllocation();
        double      dblRate = ( ((double)szRecovery) * 1000 )/durRecovery.toNanos();
        
        // Shut down message consumer hard, throws away all recovered data
        bufMsgs.shutdownNow();
        
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
