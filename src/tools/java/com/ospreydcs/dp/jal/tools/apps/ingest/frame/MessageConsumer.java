package com.ospreydcs.dp.jal.tools.apps.ingest.frame;

import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;
import com.ospreydcs.dp.jal.common.ResultStatus;
import com.ospreydcs.dp.jal.model.IMessageSupplier;

/**
 * <p>
 * Independent thread for consuming messages from an <code>IMessageSupplier</code> instance under evaluation.
 * </p>
 * <p>
 * This implementation simply retrieves <code>IngestDataRequest</code> messages from the target 
 * <code>IMessageSupplier</code> interface as fast as possible.  No further processing is performed,
 * the messages are simply discarded.  The objective is to clear any message buffer as fast as possible
 * so that only the processing speed is observed.
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
 */
public class MessageConsumer extends Thread {

    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>MessageConsumer</code> ready for execution.
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
        return new MessageConsumer(supplier);
    }
    
    //
    // Defining Attributes
    //
    
    /** The ingestion frame supplier - source of IngestDataRequest Protobuff messages */
    private final IMessageSupplier<IngestDataRequest>   supplier;
    
    
    //
    // State Variables
    //
    
    /** The number of <code>IngestDataRequest</code> messages consumed. */
    private int     cntMsgs = 0;
    
    /** The data size recovered (in bytes) */
    private long    szAlloc = 0;
    
    /** Thread start flag (i.e., entered consumer loop) */
    private boolean bolStart = false;
    
    /** Thread finished flag (regardless of normal or abnormal exit) */
    private boolean bolFinish = false;
    
    /** Thread termination request */
    private boolean bolTerminate = false;
    
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
     */
    public MessageConsumer(IMessageSupplier<IngestDataRequest> processor) {
        this.supplier = processor;
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
    public long getAllocation() { return this.szAlloc; };
    
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
                
                if (msgRqst != null) {
                    this.szAlloc += msgRqst.getSerializedSize();
                    this.cntMsgs++;
                }
                
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