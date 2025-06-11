package com.ospreydcs.dp.jal.tools.query.channel;

import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.model.IMessageSupplier;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

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
public class BufferConsumerTask implements Runnable {

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
    static final long       LNG_TIMEOUT = 15;
    
    /** the polling timeout units */
    static final TimeUnit   TU_TIMEOUT = TimeUnit.MILLISECONDS;
    
    
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