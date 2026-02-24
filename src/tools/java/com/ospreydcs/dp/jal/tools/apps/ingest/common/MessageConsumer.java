package com.ospreydcs.dp.jal.tools.apps.ingest.common;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;
import com.ospreydcs.dp.jal.common.ResultStatus;
import com.ospreydcs.dp.jal.model.IMessageSupplier;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Independent thread for consuming messages from an <code>IMessageSupplier</code> instance under evaluation.
 * </p>
 * <p>
 * This implementation simply retrieves <code>IngestDataRequest</code> messages from the target 
 * <code>IMessageSupplier</code> interface as fast as possible.  No further processing is performed,
 * in typical operation (single argument creator) the messages are simply discarded.  
 * The objective is to clear any message buffer as fast as possible
 * so that only the processing speed is observed.
 * With the double argument creator the instance saves the acquired <code>IngestDataRequest</code>
 * messages for recovery once the task has completed.
 * </p>
 * <p>
 * <h2>Operation</h2>
 * Class instances are attached to a single <code>IMessageSupplier</code> object at creation/construction.
 * Upon the invocation of <code>{@link Thread#start()}</code> the <code>MessageConsumer</code> instance will
 * begin requesting <code>IngestDataRequest</code> messages using the <code>{@link IMessageSupplier#take()}</code>.
 * The thread terminates when the <code>{@link IMessageSupplier#isSupplying()}</code> returns <code>false</code>
 * or the thread is terminated by the external client with a <code>{@link #terminate()}</code> invocation.
 * </p>
 * <p>
 * <h2>Usage</h2>
 * Before starting a <code>MessageConsumer</code> thread it is imperative that the attached message supplier be 
 * activated.  Specifically, the <code>{@link IMessageSupplier#isSupplying()}</code> method must return <code>true</code>.
 * Otherwise the consumer loop will simply pass through and no messages will be consumed.
 * </p>
 * <p>
 * <h2>Message Storage</h2>
 * </p>
 */
public class MessageConsumer extends Thread {

    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>MessageConsumer</code> task ready for execution.
     * </p>
     * <p>
     * Creates a new <code>MessageConsumer</code> task that does not store the <code>IngestDataRequest</code>
     * message obtained from the given supplier.  The size of any acquired message is added into the
     * total allocation size accumulator then discarded (i.e., left to the JVM garbage collection).
     * This process relieves stress on Java heap storage for large collections of processed messages.
     * </p>
     * <p>
     * <h2>Usage</h2>
     * Before starting a <code>MessageConsumer</code> thread it is imperative that the given message supplier be 
     * activated.  Specifically, the <code>{@link IMessageSupplier#isSupplying()}</code> method must return 
     * <code>true</code>.
     * Otherwise the consumer loop will simply pass through and no messages will be consumed.
     * </p>
     * 
     * @param supplier the <code>IMessageSupplier</code> interface supplying <code>IngestDataRequest</code> messages
     * 
     * @return  a new <code>MessageConsumer</code> instance attached to the given mesasge supplier
     */
    public static MessageConsumer   from(IMessageSupplier<IngestDataRequest> supplier) {
        return MessageConsumer.from(supplier, false);
    }
    
    /**
     * <p>
     * Creates and returns a new instance of <code>MessageConsumer</code> task ready for execution.
     * </p>
     * <p>
     * Creates a new <code>MessageConsumer</code> task that potentially stores the <code>IngestDataRequest</code>
     * message obtained from the given supplier.  The size of any acquired message is added into the
     * total allocation size accumulator stored into the local message buffer if the argument
     * storage enable/disable flag is <code>true</code>, or simply discarded (i.e., left to JVM garbage
     * collection) if the flag is <code>false</code>.
     * </p>
     * <p>
     * <h2>Usage</h2>
     * Before starting a <code>MessageConsumer</code> thread it is imperative that the given message supplier be 
     * activated.  Specifically, the <code>{@link IMessageSupplier#isSupplying()}</code> method must return 
     * <code>true</code>.
     * Otherwise the consumer loop will simply pass through and no messages will be consumed.
     * </p>
     * <p>
     * The recovered <code>IngestDataRequest</code> messages can be recovered with the
     * <code>{@link #getRecoveredMessages()}</code> method, which should be called after task completion.
     * The messages can then be used for further processing.
     * </p>
     * 
     * @param supplier the <code>IMessageSupplier</code> interface supplying <code>IngestDataRequest</code> messages
     * @param bolStore enable/disable <code>IngestDataRequest</code> message storage for later processing
     * 
     * @return  a new <code>MessageConsumer</code> instance attached to the given mesasge supplier
     */
    public static MessageConsumer   from(IMessageSupplier<IngestDataRequest> supplier, boolean bolStore) {
        return new MessageConsumer(supplier, bolStore);
    }
    
    
    //
    // Class Constants
    //
    
    /** The <code>{@link IMessageSupplier#poll(long, java.util.concurrent.TimeUnit)}</code> timeout limit */ 
    @SuppressWarnings("unused")
    private final static long       LNG_POLL_TMOUT = 15;
    
    /** The <code>{@link IMessageSupplier#poll(long, java.util.concurrent.TimeUnit)}</code> timeout units */
    @SuppressWarnings("unused")
    private final static TimeUnit   TU_POLL_TMOUT = TimeUnit.MILLISECONDS;
    
    
    //
    // Defining Attributes
    //
    
    /** The ingestion frame supplier - source of IngestDataRequest Protobuff messages */
    private final IMessageSupplier<IngestDataRequest>   supplier;
    
    /** Enable/disable message local storage */
    private final boolean                               bolStore;
    
    
    //
    // Instance Resources
    //
    
    /** The collection of recovered data messages from the supplier */
    private final List<IngestDataRequest>               lstMsgs;
    
    
    //
    // State Variables
    //
    
    /** Thread start flag (i.e., entered consumer loop) */
    private boolean bolStart = false;
    
    /** Thread finished flag (regardless of normal or abnormal exit) */
    private boolean bolFinish = false;
    
    /** Thread termination request */
    private boolean bolTerminate = false;
    

    /** The number of <code>IngestDataRequest</code> messages consumed. */
    private int     cntMsgs = 0;
    
    /** The data size recovered (in bytes) */
    private long    szAlloc = 0;
    
    
    /** Result of thread execution */
    private ResultStatus    recStatus = null;
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>MessageConsumer</code> instance attached to the given ingestion frame supplier.
     * </p>
     *
     * @param supplier ingestion frame supplier producing <code>IngestDataRequest</code> messages
     * @param bolStore message storage enable/disable flag
     */
    protected MessageConsumer(IMessageSupplier<IngestDataRequest> processor, boolean bolStore) {
        this.supplier = processor;
        this.bolStore = bolStore;
        
        if (this.bolStore)
            this.lstMsgs = new LinkedList<>();
        else 
            this.lstMsgs = null;
    }
    
    
    //
    // Operations
    //
    
    /**
     * Terminates thread execution or prevents execution if not already running.
     */
    public void terminate() { this.bolTerminate = true; }
    
    
    //
    // State Inquiry
    //
    
    /**
     * @return  <code>true</code> if thread has started execution, <code>false</code> otherwise
     */
    public boolean  hasStarted() { return this.bolStart; }
    
    /**
     * @return  <code>true</code> if thread has finished execution (normal or abnormal), <code>false</code> otherwise
     */
    public boolean  hasFinished() { return this.bolFinish; };
    
    /**
     * @return  status of thread execution or <code>null</code> if not completed
     */
    public ResultStatus getResult() { return this.recStatus; };
    
    /**
     * @return  the current number of messages consumed
     */
    public int  getMessageCount()   { return this.cntMsgs; };
    
    /**
     * @return  the current recovered message allocation size 
     */
    public long getAllocation() { 
        return this.szAlloc;
//        return this.conMsgs.stream().mapToLong(IngestDataRequest::getSerializedSize).sum();
    };
    
    /**
     * <p>
     * Returns the collection of <code>IngestDataRequest</code> messages recovered from the supplier.
     * </p>
     * <p>
     * This method should be invoked after the task has been executed, otherwise an incomplete or
     * empty collection is returned.  However, it is possible to recover an intermediate collection
     * while the task is executing. 
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This method is not thread safe.
     * </p>
     * 
     * @return  the ordered list of <code>IngestDataRequest</code> messages recovered from the supplier
     * 
     * @throws IllegalStateException    the task was not configured for message storage
     */
    public List<IngestDataRequest> getRecoveredMessages() throws IllegalStateException {
        
        // Check state
        if (!this.bolStore)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Task not configured for message storage.");
        
        return this.lstMsgs;
    }
    
    //
    // Thread Overrides
    //
    
    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        this.bolStart = true;
        
        while (supplier.isSupplying() && !this.bolTerminate) {
            
            try {
                IngestDataRequest   msgRqst = supplier.take();
//                IngestDataRequest   msgRqst = supplier.poll(LNG_POLL_TMOUT, TU_POLL_TMOUT);
                
                if (msgRqst == null)
                    continue;
                this.szAlloc += msgRqst.getSerializedSize();
                this.cntMsgs++;

                if (this.bolStore)
                    this.lstMsgs.add(msgRqst);
                
                
            } catch (IllegalStateException e) {
                this.recStatus = ResultStatus.newFailure("Exception thrown during take() operation.", e);
                this.bolFinish = true;
                return;
                
            } catch (InterruptedException e) {
                this.recStatus = ResultStatus.newFailure("Exception thrown during take() operation.", e);
                this.bolFinish = true;
                return;
            }
        }
        
        if (this.bolTerminate) {
            this.recStatus = ResultStatus.newFailure("Thread execution terminated by client.");
            this.bolFinish = true;
            return;
        }
        
        this.recStatus = ResultStatus.SUCCESS;
        this.bolFinish = true;
    }
}