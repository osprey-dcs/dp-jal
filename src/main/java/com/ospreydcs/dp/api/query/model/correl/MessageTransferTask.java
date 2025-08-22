/*
 * Project: dp-api-common
 * File:	MessageTransferTask.java
 * Package: com.ospreydcs.dp.api.query.model.correl
 * Type: 	MessageTransferTask
 *
 * Copyright 2010-2023 the original author or authors.
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
 * @since Jan 15, 2025
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.correl;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.model.IMessageSupplier;
import com.ospreydcs.dp.api.query.model.grpc.QueryMessageBuffer;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

/**
 * <p>
 * Independent processing thread for the <code>RawDataCorrelator</code> class.
 * </p>
 * <p>
 * This task is performed on a separate thread to decouple the gRPC streaming of
 * <code>QueryResponse</code> messages from the Query Service from the operation of
 * correlating the response data.  We do not want to interrupt the data streaming but
 * do want to began processing the data as soon as possible.
 * </p>
 * <p>
 * <h2>Data Correlator</h2>
 * In the current implementation either a <code>QueryDataCorrelator</code> or a 
 * <code>RawDataCorrelator</code> can be used for the <code>QueryData</code> Protocol Buffers
 * message sink.  Specifically, either class type is supported for raw data correlation.
 * The specific type is determined at <code>MessageTransferTask</code> creation.  Note that
 * <code>QueryDataCorrelator</code> is new deprecated and support there will likely be discontinued. 
 * The difference between the two is the following:
 * <ul>
 * <li><code>QueryDataCorrelator</code> - supports only raw request data with <code>SampleClock</code> messages.</li>
 * <li><code>RawDataCorrelator</code> - supports both <code>SampleClock</code> and <code>TimestampList</code> messages. </li>
 * </ul>
 * </p>
 * <p>
 * <s>
 * The method <code>{@link setMaxMessages(int)}</code> must be invoked to set the total
 * number of data messages to be processed.  The method must called be either before
 * starting the thread or while the thread is enabled.  The internal processing loop exits 
 * properly whenever the internal message counter <code>{@link #cntMsgsXferred}</code>
 * is equal to the value supplied to this method.  Otherwise the processing loop
 * continues indefinitely, or until interrupted (e.g., with a call to 
 * <code>{@link #terminate()}</code> or <code>{@link Thread#interrupt()}</code>).
 * </s>
 * </p>
 * <p>
 * <h2>Processing Loop</h2>
 * Message data processing is performed on a continuous loop within the thread's 
 * internal <code>{@link #run}</code> method (e.g., invoked by <code>{@link Thread#start()}</code>). 
 * There raw response data messages (type <code>QueryData</code>) are polled from the 
 * message supplier <code>{@link IMessageSupplier#poll(long, TimeUnit)}</code>, blocking until they 
 * become available or a timeout occurs (see <code>{@link #LNG_TIMEOUT}, {@link #TU_TIMEOUT}</code>).
 * Data messages are then passed to the data correlator <code>{@link #prcrCorrelatorOld}</code>
 * for processing.  The loop exits whenever the supplier is no longer enabled, the task is
 * terminated (i.e., with the <code>{@link #terminate()}</code> method), or an exception occurs.
 * </p>
 * <p>
 * <h2>NOTES</h2>
 * <ul>
 * <li>
 * For concurrent gRPC data streaming and data correlation, the processing task should be 
 * started (e.g., with the <code>{@link Thread#start()}</code> method) BEFORE the gRPC data 
 * stream(s).  
 * </li>
 * <br/>
 * <li>
 * For separate gRPC data streaming and data correlation, the processing thread should be
 * started AFTER the gRPC data stream(s) (e.g., with the <code>{@link Thread#start()}</code> method).  
 * </li> 
 * <br/>
 * <li>
 * Only one processing thread instance is required per data request, regardless of the
 * number of gRPC data streams used, or whether the processing is done currently/serially.  
 * All data streams should specify the passed  
 * <code>{@link IMessageSupplier}</code> as the target of their data consumer function.
 * Due to the nature of Java a processing thread (e.g., <code>{@link Thread}</code> class)
 * task instances can only be used once.
 * </li>
 * <li>
 * The external <code>{@link RawDataCorrelator}</code> object should block on the
 * <code>{@link Thread#join()}</code> method to wait for this processing thread to 
 * complete.  <code>{@link Thread#join()}</code> will return once the processing loop
 * exits normally.
 * </li>
 * <br/>
 * <li>
 * If an error occurs with the gRPC data streaming, the processing thread should be
 * terminated with <code>{@link #terminate()}</code>.  This action will also release
 * any threads waiting on the <code>{@link Thread#join()}</code> method.
 * </li>
 * <br/>
 * <li>
 * If a timeout occurs during queue polling the processing loop simply restarts, 
 * thus re-checking the number of processed messages against the maximum message count 
 * (if available).  The processing loop exits if the values are equal, or polls for 
 * another message otherwise.
 * </li>
 * <br/>
 * <li>
 * The polling timeout limit described by <code>{@link #LNG_TIMEOUT}</code> and 
 * <code>{@link #TU_TIMOUT}</code> can be used as tuning parameters for the processing
 * loop.
 * </li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 15, 2025
 *
 */
public class MessageTransferTask extends Thread implements Runnable, Callable<Boolean> {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new instance of <code>MessageTransferTask</code> attached to the arguments 
     * and ready for task start.
     * </p>
     * 
     * @param srcRspMsgs        source of request data messages for transfer to given correlator
     * @param prcrCorrelator    data correlator to receive data messages
     * 
     * @return  new <code>MessageTransferTask</code> attached to the given arguments
     */
    public static MessageTransferTask from(IMessageSupplier<QueryData> srcRspMsgs, RawDataCorrelator prcrCorrelator) {
        return new MessageTransferTask(srcRspMsgs, prcrCorrelator);
    }
    
    /**
     * <p>
     * Creates a new <code>Thread</code> instance which executes an instance of <code>MessageTransferTask</code>
     * attached to the given arguments.
     * </p>
     * <p>
     * This is a convenience method for explicitly creating thread tasks.  The returned instance is started
     * with the <code>{@link Thread#start()}</code> method and terminates according to the conditions described
     * in the <code>{@link #run()}</code> method documentation.
     * </p>
     *  
     * @param srcRspMsgs     source of request data messages for transfer to given correlator
     * @param prcrCorrelator data correlator to receive data messages
     * 
     * @return  a new thread task ready for execution.
     */
    public static Thread    newThread(IMessageSupplier<QueryData> srcRspMsgs, RawDataCorrelator prcrCorrelator) {
        MessageTransferTask task = MessageTransferTask.from(srcRspMsgs, prcrCorrelator);
        Thread              thdTask = new Thread(task);
        
        return thdTask;
    }
    
//    /**
//     * <p>
//     * Creates a new instance of <code>MessageTransferTask</code> attached to the arguments 
//     * and ready for task start.
//     * </p>
//     * 
//     * @param srcRspMsgs        source of request data messages for transfer to given correlator
//     * @param prcrCorrelator    data correlator to receive data messages
//     * 
//     * @return  new <code>MessageTransferTask</code> attached to the given arguments
//     */
//    public static MessageTransferTask from(IMessageSupplier<QueryData> srcRspMsgs, QueryDataCorrelatorOld prcrCorrelator) {
//        return new MessageTransferTask(srcRspMsgs, prcrCorrelator);
//    }
//    
//    /**
//     * <p>
//     * Creates a new <code>Thread</code> instance which executes an instance of <code>MessageTransferTask</code>
//     * attached to the given arguments.
//     * </p>
//     * <p>
//     * This is a convenience method for explicitly creating thread tasks.  The returned instance is started
//     * with the <code>{@link Thread#start()}</code> method and terminates according to the conditions described
//     * in the <code>{@link #run()}</code> method documentation.
//     * </p>
//     *  
//     * @param srcRspMsgs     source of request data messages for transfer to given correlator
//     * @param prcrCorrelator data correlator to receive data messages
//     * 
//     * @return  a new thread task ready for execution.
//     */
//    public static Thread    newThread(IMessageSupplier<QueryData> srcRspMsgs, QueryDataCorrelatorOld prcrCorrelator) {
//        MessageTransferTask task = MessageTransferTask.from(srcRspMsgs, prcrCorrelator);
//        Thread              thdTask = new Thread(task);
//        
//        return thdTask;
//    }
    

    //
    // Application Resources
    //
    
    /** The Data Platform Query Service default parameters */
    private static final DpQueryConfig CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants - Initialized from API Library configuration
    //
    
    /** Is logging enabled? */
    public static final boolean     BOL_LOGGING = CFG_QUERY.logging.enabled;
    
    /** Event logging level */
    public static final String      STR_LOGGING_LEVEL = CFG_QUERY.logging.level;
    
    
    //
    // Class Constants - Performance Tuning
    
    /** Message supplier polling timeout limit */
    public static final long        LNG_TIMEOUT = 15;
    
    /** Message supplier polling timeout units */
    public static final TimeUnit    TU_TIMEOUT = TimeUnit.MILLISECONDS;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    
    /**
     * <p>
     * Class Initialization
     * </p>
     * <p>
     * Initializes the event logger - sets logging level.
     * </p>
     */
    static {
        Configurator.setLevel(LOGGER, Level.toLevel(STR_LOGGING_LEVEL, LOGGER.getLevel()));
    }
    
    
    // 
    // Initializing Resources
    //
    
    /** The external queued data buffer supplying data messages for correlation */
    private final IMessageSupplier<QueryData>   queDataBuffer;
    
//    /** The external data correlator doing toe data processing (message sink) */
//    private final QueryDataCorrelatorOld        prcrCorrelatorOld;
    
    /** The external data correlator doing toe data processing (message sink) */
    private final RawDataCorrelator             prcrCorrelatorNew;
    
    
    //
    // State Variables
    //
    
    /** thread started flag */
    private boolean         bolStarted = false;
    
    /** thread external termination flag */
    private boolean         bolTerminated = false;
    
    /** Number of data messages transferred so far (internal counter) */
    private int             cntMsgsXferred = 0;
    
    /** The result of the thread task */
    private ResultStatus    recResult = null;
    
    
    /**
     * <p>
     * Constructs a new instance of <code>MessageTransferTask</code> attached to the 
     * raw data buffer and data correlator.
     * </p>
     *
     * @param queDataBuffer     source of raw data for correlation processing
     * @param prcrCorrelator    data correlator to receive raw data
     */
    public MessageTransferTask(IMessageSupplier<QueryData> queDataBuffer, RawDataCorrelator theCorrelator) {
        this.queDataBuffer = queDataBuffer;
//        this.prcrCorrelatorOld = null;
        this.prcrCorrelatorNew = theCorrelator;
    }
    
//    /**
//     * <p>
//     * Constructs a new instance of <code>MessageTransferTask</code> attached to the 
//     * raw data buffer and data correlator.
//     * </p>
//     *
//     * @param queDataBuffer     source of raw data for correlation processing
//     * @param prcrCorrelator    data correlator to receive raw data
//     */
//    public MessageTransferTask(IMessageSupplier<QueryData> queDataBuffer, QueryDataCorrelatorOld theCorrelator) {
//        this.queDataBuffer = queDataBuffer;
////        this.prcrCorrelatorOld = theCorrelator;
//        this.prcrCorrelatorNew = null;
//    }
    
    
    //
    // State Queries
    //
    
    /**
     * <p>
     * Determines whether or not the transfer task has been started.
     * </p>
     * <p>
     * Note that whenever a processing thread is started (i.e., by the <code>{@link Thread#start()}</code>
     * method). 
     * The <code>{@link #bolStarted}</code> flag is set to <code>true</code>
     * within the <code>{@link #run()}</code> task that is invoked when the task begins.
     * </p>
     * 
     * @return  <code>true</code> if the <code>{@link #run()}</code> has been called,
     *          <code>false</code> otherwise.
     */
    public boolean isStarted() {
        return this.bolStarted;
    }
    
    /**
     * <p>
     * Returns whether or not the transfer task was terminated externally.
     * </p>
     * <p>
     * If the returned value is <code>true</code> this indicates that the 
     * <code>{@link #terminate()}</code> method has been invoked while the task
     * was running.
     * </p>
     * 
     * @return  <code>true</code> if the task was terminated externally, <code>false</code> otherwise
     */
    public boolean hasTerminated() {
        return this.bolTerminated;
    }
    
    /**
     * <p>
     * Returns the number of messages transferred to the 
     * </p>
     * 
     * @return  the number of messages transferred from the supplier to the correlator
     */
    public int  getMessagesTransferred() {
        return this.cntMsgsXferred;
    }
    
    /**
     * <p>
     * Returns the <code>{@link ResultStatus}</code> containing the status of this transfer task.
     * </p>
     * <p>
     * The returned value will be <code>{@link ResultStatus#SUCCESS}</code> only if the 
     * the transfer task has exited normally.  This value implies the following 
     * conditions:
     * <ul>
     * <li>The <code>{@link IMessageSupplier#isSupplying()}</code> returned <code>false</code>.
     * </li>
     * <li>All queued messages have been successfully correlated (i.e., there are no pending correlations).</li>
     * <li>The processing loop within <code>{@link #run()}</code> has exited and the task is dead.</li>
     * </ul>
     * <p>
     * A FAILURE status also indicates that the task is currently dead and that an exception occurred.  
     * The conditions where this situation can occur are the following:
     * <ul>
     * <li>
     * The task was terminated externally (i.e., with the <code>{@link #terminate()}</code> method).
     * </li>
     * <li>
     * The message supplier was never activated (e.g., <code>{@link QueryMessageBuffer#activate()}</code> was never called).
     * </li>
     * <li>
     * The message supplier polling operation was interrupted while waiting.
     * </li> 
     * <li>
     * A data bucket within a <code>QueryData</code> message contained no timestamp information.
     * </li>
     * <li>
     * A data bucket insertion task failed (i.e., a data correlation failure). 
     * </li>
     * </ul>
     * If the <code>{@link #terminate()}</code> method was invoked the returned value is a FAILURE status.
     * If the task has not started or not completed the returned value will be <code>null</code>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method does not block.  That is, it does not wait for the processing loop to
     * complete but returns the status of the thread in its current state (or <code>null</code>).
     * </li>
     * <br/>
     * <li>
     * The returned value will be <code>null</code> if the processing thread has not been 
     * started or has terminated abnormally (e.g., was interrupted by <code>#terminate()</code>.)
     * </li>
     * </ul>
     * </p>
     * 
     * @return  <code>{@link ResultStatus#SUCCESS}</code> if the task has exited normally,
     *          <code>{@link ResultStatus#isFailure()}</code> if task failed or was terminated,
     *          <code>null</code> if incomplete or not started
     */
    public ResultStatus getResult() {
        return this.recResult;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Terminates the message transfer immediately.
     * </p>
     * <p>
     * The main tranfer loop within <code>{@link #run()}</code> is interrupted
     * and the result is set to FAILURE.  All processing activity is aborted
     * and left in its current state.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This is the proper way to terminate an enabled processing thread.  DO NOT use
     * the method <code>{@link Thread#interrupt()}</code> as it will leave a 
     * <code>null</code> valued <code>{@link ResultStatus}</code> potentially corrupting
     * the normal (external) processing operations.
     * </li>
     * <br/>
     * <li> 
     * Terminating a processing thread that has not started has no effect and will not 
     * affect any future processing or events.  This action is illegal and anticipated.
     * </li> 
     * </p>
     */
    public void terminate() {
        if (!this.bolStarted)
            return;

        this.bolTerminated = true;
        
        String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - The data processing thread was terminted externally.";
        
        if (BOL_LOGGING)
            LOGGER.warn(strMsg);
        
        this.recResult = ResultStatus.newFailure(strMsg);
    }
    
    
    //
    // Runnable Interface
    //
    
    /**
     * <p>
     * Executes the processing loop.
     * </p>
     * <p>
     * Runs a continuous loop that polls the message supplier
     * (i.e., <code>{@link IMessageSupplier#poll(long, TimeUnit)}</code>) 
     * for <code>QueryData</code> request data messages.  When available, the data messages are
     * passed to the associated correlator <code>{@link #prcrCorrelatorOld}</code> for processing.
     * </p>
     * <p>
     * The loop continues until the message supplier no longer has messages; specifically,
     * when <code>{@link IMessageSupplier#isSupplying()}</code> returns <code>false</code>.
     * The loop also terminates if the <code>{@link #terminate()}</code> method is 
     * invoked.  Otherwise the processing loop continue indefinitely or until an exception
     * occurs.  See method documentation for <code>{@link #getResult()}</code> for a list of
     * possible exceptions.
     * </p>
     * <p>
     * If the timeout limit is reached during message supplier polling, as specified by constants 
     * <code>{@link #LNG_TIMEOUT}</code> and <code>{@link #TU_TIMEOUT}</code>,
     * the loop simply restarts.  In restarting, the <code>{@link IMessageSupplier#isSupplying()}</code>
     * method and the <code>{@link #bolTerminated}</code> flag is checked to see if the loop can
     * exit. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The <code>{@link #bolStarted}</code> flag is set to <code>true</code> upon initial
     * invocation.
     * </li>
     * <br/>
     * <li>
     * Use <code>{@link #getResult()}</code> to return the status of the transfer task, 
     * and the cause of any exceptions.
     * </li>
     * </ul>
     * </p>
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        this.bolStarted = true;
        
        while (this.queDataBuffer.isSupplying() && !this.bolTerminated) {
            
            QueryDataResponse.QueryData     msgData;
            
            // Poll message supplier for next available query data
            try {
                msgData = this.queDataBuffer.poll(LNG_TIMEOUT, TU_TIMEOUT);

                // If we timeout try again
                if (msgData == null) {

                    continue;
                }

            // Error - Message supplier was not supplying
            } catch (IllegalStateException e) {      
                
                String strMsg = JavaRuntime.getQualifiedMethodNameSimple()  
                        + " - Illegal state exception during polling after "
                        + this.cntMsgsXferred 
                        + " transfers :"
                        + e.getMessage();
                
                this.recResult = ResultStatus.newFailure(strMsg, e);
                
                if (BOL_LOGGING)
                    LOGGER.error(strMsg);
                
                return;
            
            // (Nonterminal?) Exception - polling operation was interrupted while waiting for message 
            } catch (InterruptedException e) {
                
                if (this.cntMsgsXferred > 0) {
                    this.recResult = ResultStatus.SUCCESS;
                    
                    if (BOL_LOGGING)
                        LOGGER.warn("{} - Message transfer loop interrupted after {} transfers, assuming successful.",
                                JavaRuntime.getQualifiedMethodNameSimple(), this.cntMsgsXferred);
                    
                    return;
                }
                
                String  strErrMsg = JavaRuntime.getQualifiedMethodNameSimple() +
                        " - Process interrupted externally and FAILED while waiting for data buffer.";
                
                this.recResult = ResultStatus.newFailure(strErrMsg, e);

                if (BOL_LOGGING) 
                    LOGGER.warn(strErrMsg);
                
                return;
            }

            // Add query data to the correlator - supports either correlator type set at construction
            try {
                    
                // Note that this operation will block until completed
//                if (this.prcrCorrelatorOld != null) {
//                    this.prcrCorrelatorOld.addQueryData(msgData);
//                    this.cntMsgsXferred++;
//                } else {
                    this.prcrCorrelatorNew.processQueryData(msgData);
                    this.cntMsgsXferred++;
//                }

            // Error - Data bucket did not contain timestamps 
            } catch (IllegalArgumentException e) {
                
                String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                            + " - Missing timestamps in data bucket after "
                            + this.cntMsgsXferred 
                            + " message transfers: "
                            + e.getMessage();
                
                this.recResult = ResultStatus.newFailure(strMsg, e);
                
                if (BOL_LOGGING)
                    LOGGER.error(strMsg);
                
                return;
                
            // Error - Error in data bucket insertion task 
            } catch (CompletionException e) {
                
                String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                            + " - Error in data bucket insertion task after "
                            + this.cntMsgsXferred
                            + " message transfers: "
                            + e.getMessage();
                
                this.recResult = ResultStatus.newFailure(strMsg, e);
                
                if (BOL_LOGGING)
                    LOGGER.error(strMsg);
            } 
            
        } // while suppling and not terminated

        if (!this.bolTerminated) {
            this.recResult = ResultStatus.SUCCESS;
            
            if (BOL_LOGGING)
                LOGGER.debug("{} - Exiting message transfer loop normally.", JavaRuntime.getQualifiedMethodNameSimple());

        } else {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Message transfer terminated externally after "
                    + this.cntMsgsXferred 
                    + " transfers.";
            
            this.recResult = ResultStatus.newFailure(strMsg);
            
            if (BOL_LOGGING) {
                LOGGER.error(strMsg);
            }
        }
    }
    

    //
    // Callable<Boolean> Interface
    //
    
    /**
     * <p>
     * Executes the main message transfer loop for the task.
     * </p>
     * <p>
     * Defers to <code>{@link #run()}</code> then returns the status of <code>{@link #recResult}</code>
     * upon completion.
     * </p> 
     *
     * @see java.util.concurrent.Callable#call()
     * @see #run()
     */
    @Override
    public Boolean  call() {
        
        this.run();
        
        return this.recResult.isSuccess();
    }

}
