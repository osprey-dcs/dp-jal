/*
 * Project: dp-data-simulator
 * File:	IngestionFrameQueue.java
 * Package: com.ospreydcs.dp.datasim.model.frame
 * Type: 	IngestionFrameQueue
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
 * @since Jun 22, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.frames;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.ospreydcs.dp.jal.ingest.IngestionFrame;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Persistent storage queue for ingestion scenario payloads.
 * </p>
 * <p>
 * This queue maintains a queued collection of <code>{@link IngestionFrame}</code> instances for consumption
 * by an ingestion scenario.  That is, instances of <code>IngestionFrameQueue</code> are designed to
 * hold the pre-generated payload for an ingestion scenario.
 * </p>
 * <h2>Persistence</h2>
 * <p>
 * To avoid Java VM heap memory problems the queue is designed to store some ingestion frames to disk.
 * Ingestion frames are loaded into active memory as the active memory buffer capacity permits (i.e.,
 * attribute <code>{@link #queActive}</code>).
 * </p>
 * <h2>Blocking and Non-Blocking Request</h2>
 * <p>
 * The queue can be configured for both blocking and non-blocking frame requests from method 
 * <code>{@link #next()}</code>.  This is done with a configuration parameter at construction.
 * <ul>
 * <li>
 * When a non-blocking configuration is specified the <code>{@link #next()}</code> method will throw
 * an exception if a request is made and the active memory buffer is empty (i.e., the next frame has
 * not yet been read from persistent memory). 
 * </li>
 * <li>
 * When the blocking configuration is specified the <code>{@link #next()}</code> method will block 
 * indefinitely until an ingestion frame is read from disk and placed into the active memory buffer.
 * </li>
 * </ul>
 * Thus, if the queue is being used for a simulated clock the blocking option can interfere with performance
 * evaluations. 
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jun 22, 2024
 *
 */
public class IngestionFrameQueue implements Iterator<IngestionFrame>, Iterable<IngestionFrame> {
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionFrameQueue</code>.
     * </p>
     * <p>
     * The capacities of the new ingestion frame queue are all specified.  The new queue must be 
     * loaded with <code>IngestionFrame</code> instances with the 
     * <code>{@link #fill(IngestionFrameFactory)}</code> method before use.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * The blocking feature dictated by the value of argument <code>bolBlocking</code> is 
     * as follows:
     * <ul>
     * <li>
     * Setting the <code>bolBlocking</code> argument to <code>true</code> causes the 
     * <code>{@link #next()}</code> to block if the active memory buffer is not ready, that is,
     * if the Disk Manager is still loading an ingestion frame from persistent storage.
     * </li>
     * <li>
     * Setting the <code>bolBlocking</code> argument to <code>false</code> cause the 
     * <code>{@link #next()}</code> method to throw an exception if the active memory buffer is
     * not ready when called (i.e., it is empty).
     * </li>
     * </ul>
     * </p>
     *
     * @param szTotal       total capacity of frame queue including persistent storage
     * @param szActive      active buffer capacity, maximum number of frame in RAM memory
     * @param bolBlocking   allow for wait blocking if active memory buffer is ready 
     * 
     * @return  a new ingestion frame queue ready to be loaded
     * 
     * @throws IllegalArgumentException must have <code>szTotal</code> >= <code>szActive</code> > 0 
     * @throws IOException              a temporary file for persistent storage could not be created
     */
    public static IngestionFrameQueue   create(int szTotal, int szActive, boolean bolBlocking) 
            throws IllegalArgumentException, IOException {
        
        return new IngestionFrameQueue(szTotal, szActive, bolBlocking);
    }
    
    
    //
    // Class Constants
    //
    
    /** Disk storage file prefix */
    private static final String     STR_DISK_BUFF_PREFIX = IngestionFrameQueue.class.getSimpleName();
    
    /** Disk storage file suffix */
    private static final String     STR_DISK_BUFF_SUFFIX = ".dat";
    
    
    /** General thread wait time - used for disk manager thread startup */
    private static final long       LNG_THD_WAIT_MS = 25;
    
//    /** Active memory queue polling timerSpec limit */
//    private static final long       LNG_TIMEOUT_POLL = 1;
//    
//    /** Active memory queue polling timerSpec units */
//    private static final TimeUnit   TU_TIMEOUT_POLL = TimeUnit.MILLISECONDS;

    
    //
    // Configuration
    //
    
    /** Total queue capacity (including disk memory) */
    private final int       szQueueTotal;
    
    /** Active buffer size - number of frame in RAM memory */
    private final int       szActiveBuffer;
    
    /** Persistent buffer size - number of frames stored in disk memory */
    private final int       szPersistBuffer;
    
    
    /** Allow blocking on active memory buffer */
    private final boolean   bolBlocking;
    
    
    //
    // Resources
    //
    
    /** The active memory storage buffer for ingestion frames */
    private final BlockingQueue<IngestionFrame> queActive;
    
    /** The location of disk storage for ingestion frames */
    private final File                          fileDiskBuffer;
    
    
    /** The input file stream containing the stored (serialized) ingestion frames */
    private FileInputStream                     isDiskBuffer = null;
    
    /** The input object stream deserializing the stored ingestion frames */
    private ObjectInputStream                   isObjectBuffer = null;
    
    /** The thread managing disk to buffer loading */
    private Thread                              thdDiskMgr = null;
    

    //
    // State Variables
    //
    
    /** Current queue capacity - number of ingestion frames available */
    private int     cntFrames = 0;
    
    /** Queue active flag - the queue contains available ingestion frames */
    private boolean bolActive = false;
    
    /** Error flag - error occurred in queue operations */
    private boolean bolError = false;
    
    
    //
    // Terminal Conditions 
    //
    
    /** Total memory allocation of last fill (bytes) */
    private long        szAllocTotal = 0;
    
    /** The total time spent waiting for a frame to become available */
    private Duration    durBlocking = Duration.ZERO;
    
    /** Any status or error message */
    private String      strStatus = "Unused queue.";
    
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionFrameQueue</code>.
     * </p>
     * <p>
     * The capacities of the new ingestion frame queue are all specified.  The new queue must be 
     * loaded with <code>IngestionFrame</code> instances with the 
     * <code>{@link #fill(IngestionFrameFactory)}</code> method before use.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * The blocking feature dictated by the value of argument <code>bolBlocking</code> is 
     * as follows:
     * <ul>
     * <li>
     * Setting the <code>bolBlocking</code> argument to <code>true</code> causes the 
     * <code>{@link #next()}</code> to block if the active memory buffer is not ready, that is,
     * if the Disk Manager is still loading an ingestion frame from persistent storage.
     * </li>
     * <li>
     * Setting the <code>bolBlocking</code> argument to <code>false</code> cause the 
     * <code>{@link #next()}</code> method to throw an exception if the active memory buffer is
     * not ready when called (i.e., it is empty).
     * </li>
     * </ul>
     * </p>
     *
     * @param szTotal       total capacity of frame queue including persistent storage
     * @param szActive      active buffer capacity, maximum number of frame in RAM memory
     * @param bolBlocking   allow for wait blocking if active memory buffer is ready 
     * 
     * @throws IllegalArgumentException must have <code>szTotal</code> >= <code>szActive</code> > 0 
     * @throws IOException              a temporary file for persistent storage could not be created
     */
    public IngestionFrameQueue(int szTotal, int szActive, boolean bolBlocking) throws IllegalArgumentException, IOException {
        
        // Check arguments
        if (szTotal < szActive || szActive < 1)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - total buffer size " 
                    + szTotal 
                    + " must be greater than active buffer size " 
                    + szActive
                    + " which must be greater than 1.");
        
        // Set configuration
        this.szQueueTotal = szTotal;
        this.szActiveBuffer = szActive;
        this.szPersistBuffer = szTotal - szActive;
        this.bolBlocking = bolBlocking;
        
        // Create resources
        this.fileDiskBuffer = File.createTempFile(STR_DISK_BUFF_PREFIX, STR_DISK_BUFF_SUFFIX);
        this.fileDiskBuffer.deleteOnExit();
        
        this.queActive = new LinkedBlockingQueue<IngestionFrame>(this.szActiveBuffer);
    }

    
    //
    // Configuration Query
    //
    
    /**
     * <p>
     * Returns the total capacity of the ingestion frame queue, that is, the true size.
     * </p>
     *  
     * @return total capacity of ingestion frame queue including both persistent memory and active memory
     */
    public int  getCapacity() {
        return this.szQueueTotal;
    }
    
    /**
     * <p>
     * Returns the capacity of the active memory buffer for the ingestion frame queue.
     * </p>
     * 
     * @return  maximum number of ingestion frames kept in active memory 
     */
    public int  getActiveBufferCapacity() {
        return this.szActiveBuffer;
    }
    
    /**
     * <p>
     * Returns the location for persistent storage.
     * </p>
     * 
     * @return  the location where serialized ingestion frames are stored within the queue
     */
    public File getPersistentBuffer() {
        return this.fileDiskBuffer;
    }
    
    /**
     * <p>
     * Returns whether or not the queue allows wait blocking for empty active memory buffer to load.
     * </p>
     * 
     * @return  <code>true</code> the <code>{@link #next()}</code> method will block if active memory buffer is not ready,
     *          <code>false</code> the <code>{@link next()}</code> method with throw exception if active memory buffer is not ready
     */
    public boolean isBlocking() {
        return this.bolBlocking;
    }

    
    //
    // State Query
    //
    
    /**
     * <p>
     * Returns the current size of the queue.
     * </p>
     *  
     * @return  the number if ingestion frames remaining in the queue
     */
    public int  getSize() {
        return this.cntFrames;
    }
    
    /**
     * <p>
     * Determine whether or not the queue is empty or depleted.
     * </p>
     * 
     * @return  <code>true</code> if the queue is empty.
     */
    public boolean isEmpty() {
        return this.getSize() == 0;
    }
    
    /**
     * <p>
     * Determines whether or not the queue is current full.
     * </p>
     * <p>
     * This method should return <code>true</code> immediately after <code>{@link #fill(IngestionFrameFactory)}</code> is 
     * invoked and no errors occurred.
     * </p>
     * 
     * @return  <code>true</code> if the queue is currently full
     */
    public boolean isFull() {
        return this.cntFrames == this.szQueueTotal;
    }
    
    /**
     * <p>
     * Returns the current persistent storage allocation (in bytes).
     * </p>
     * 
     * @return  the size of the temporary file holding serialized ingestion frames
     */
    public long getPersistentAllocation() {
        return this.getPersistentBuffer().length();
    }
    
    /**
     * <p>
     * Determines whether or not the Disk Manager thread is active.
     * </p>
     * 
     * @return  <code>true</code> if the disk manager thread is currently transferring frames to active buffer,
     *          <code>false</code> otherwise  
     */
    public boolean  isDiskActive() {
        return this.bolActive;
    }
    
    
    //
    // Initial/Terminal Conditions
    //
    
    /**
     * <p>
     * Returns an estimate for the total memory allocation (both active and persistent) for the last
     * queue fill.
     * </p>
     * <p>
     * This value is available after invoking <code>{@link #fill(IngestionFrameFactory)}</code>.
     * </p>
     *  
     * @return  return the total memory allocation used to last fill the queue (in bytes)
     */
    public long getTotalFillAllocation() {
        return this.szAllocTotal;
    }
    
    /**
     * <p>
     * Returns the current time duration spent blocking while ingestion frames are transferred to active memory.
     * </p>
     * <p>
     * The return value is valid for 1 cycle of the queue operation, that is, filling and subsequent depletion.  Note
     * that if this is a nonblocking queue the returned value is <code>{@link Duration#ZERO}</code>.
     * </p>
     * 
     * @return  total blocking time for frame retrieval from queue
     */
    public Duration getBlockingDuration() {
        return this.durBlocking;
    }
    
    /**
     * <p>
     * Returns whether or not an error has occurred during queue operations.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * Use method <code>{@link #getStatus()}</code> to acquire the status message maintained by the queue
     * and containing any error conditions encountered.
     * </p>
     * 
     * @return  <code>true</code> if an error has occurred,
     *          <code>false</code> otherwise
     *          
     * @see #getStatus()
     */
    public boolean  hasError() {
        return this.bolError;
    }
    
    /**
     * <p>
     * Return the status message for the ingestion frame queue.
     * </p>
     * <p>
     * This method is useful in debugging and error tracking if an error occurred during operation 
     * (e.g., an exception was thrown).  Typically used in conjunction with <code>{@link #hasError()}</code>.
     * </p>
     * 
     * @return  the current status message for the queue 
     * 
     * @see #hasError()
     */
    public String   getStatus() {
        return this.strStatus;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Loads the persistent queue with ingestion frames generated from the given factory and initiates the
     * disk queue.
     * </p>
     * 
     * @param facFrames     source of the <code>IngestionFrame</code> instances supplied by this queue
     * 
     * @return  <code>true</code> if queue was successfully loaded with all frames and is ready for iteration,
     *          <code>false</code> an error occurred 
     * 
     * @throws IllegalStateException    the queue already has available frames, or has error condition
     * @throws FileNotFoundException    temporary file for persistent storage cannot be created
     * @throws IOException              an I/O error occurred while writing the serialization header to disk
     * @throws InterruptedException     the main thread was interrupted while waiting to check disk manager status 
     */
    public boolean fill(IngestionFrameGeneratorDeprecated facFrames) throws IllegalStateException, FileNotFoundException, IOException, InterruptedException {
        
        // Check state
        if (this.cntFrames > 0) {
            this.strStatus = JavaRuntime.getQualifiedMethodNameSimple() + " - Attempted to fill a non-empty queue.";
            this.bolError = true;
            
            throw new IllegalStateException(this.strStatus);
        }
        if (this.bolError) {
            throw new IllegalStateException(this.strStatus);
        }

        
        // Create and load ingestion frames into the queue
        if ( !this.loadDiskQueue(facFrames ) ) {
            this.fileDiskBuffer.delete();
            this.bolError = true;
            this.strStatus = JavaRuntime.getQualifiedMethodNameSimple() + "Failed to load all ingestion frames: " + this.cntFrames + " of " + this.szQueueTotal;
            
            return false;
        }
        
        // Start the disk queue
        if ( !this.startDiskQueue() ) {
            this.fileDiskBuffer.delete();
            this.bolActive = true;
            this.strStatus = JavaRuntime.getQualifiedMethodNameSimple() + " - Initialization Failure: Disk Manager Task failed to start.";
        }
        
        // Set any state/condition variables
        this.strStatus = "IngestionFrame queue filled.";
        this.durBlocking = Duration.ZERO;
        
        return true;
    }
    
    /**
     * <p>
     * Clears the queue of all remaining <code>IngestionFrame</code> instances.
     * </p>
     * <p>
     * Shuts down the Disk Manager thread, closes all input files containing persistent data, and
     * clears the active memory buffer.  The queue will be fully depleted after returning from this
     * method.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * It is possible for an internal error to occur during shutdown operations.  Use 
     * <code>{@link #hasError()}</code> and <code>{@link #getStatus()}</code> to recover any errors.
     * </li>
     * <li>
     * If an error occurs during a clear operation the queue instance is no longer viable and must be
     * discarded.
     * </li>
     */
    public void clear() {
        
        // If the queue is depleted there is nothing to do
        if (this.cntFrames <= 0)
            return;
        
        // If the Disk Manager thread has completed simply clear out the active memory buffer
        if (!this.bolActive) {
            this.queActive.clear();
            this.cntFrames = 0;
            
            return;
        }
        
        // General Case: The disk manager is active and there are queued frames
        // - Interrupting the disk manager should clean up everything (it responds as such to interrupts)
        this.thdDiskMgr.interrupt();
        try {
            this.thdDiskMgr.join();
//            this.thdDiskMgr.join(LNG_THD_WAIT_MS);
//            Thread.sleep(LNG_THD_WAIT_MS);  // Give Disk Manager some time to clean up
            
        } catch (InterruptedException e) {

            // Exception Case: Disk Manager did not respond
            // - We must force everything down explicitly 
            // - We might want to consider this an error? 
            this.stopDiskQueue();
            this.bolError = true;
            this.bolActive = false;
            this.strStatus = JavaRuntime.getQualifiedMethodNameSimple() + " - ERROR, Disk Manager did not terminate.";
            
        } finally {

            // Clear out the active memory queue and frame counter
            this.queActive.clear();
            this.cntFrames = 0;
        }
    }

    
    //
    // Iterator<IngestionFrame> Interface
    //

    /**
     *
     * @see @see java.util.Iterator#hasNext()
     */
    @Override
    synchronized
    public boolean hasNext() {
        
        // Check for available frames and no error conditions
        if (this.cntFrames > 0 && !this.bolError) 
            return true;
        else
            return false;
    }


    /**
     *
     * @see @see java.util.Iterator#next()
     */
    @Override
    synchronized
    public IngestionFrame next() throws IllegalStateException {
        
        // Check for error condition
        if (this.bolError) {
            throw new IllegalStateException(this.strStatus);
        }

        // Check for available frames - this method should not be called if depleted
        if (this.cntFrames <= 0) {
            this.strStatus = JavaRuntime.getQualifiedMethodNameSimple() + " - Bad State: frame requested on empty queue.";
            this.bolError = true;

            throw new IllegalStateException();
        }
        
        // Non-Blocking Case: Poll the active memory buffer for next frame
        if (!this.bolBlocking) {
            IngestionFrame  frame = this.queActive.poll();

            // Fail if frame is not in active memory
            if (frame == null) {
                this.strStatus = JavaRuntime.getQualifiedMethodNameSimple() + " - Queue Failure: No ingestion frame in active memory.";
                this.bolError = true;

                throw new IllegalStateException(this.strStatus);
            }

            this.cntFrames--;
            return frame;
        }
        
      // Blocking Case:  Waits (indefinitely) for frame availability if active memory buffer is not ready
        try {
            Instant         insStart = Instant.now();
            IngestionFrame  frame = this.queActive.take();
            
            // Compute wait time and add to total
            Instant         insStop = Instant.now();
            Duration        durWait = Duration.between(insStart, insStop);
            this.durBlocking = this.durBlocking.plus(durWait);
            
            this.cntFrames--;

            return frame;
            
        } catch (InterruptedException e) {
            this.strStatus = JavaRuntime.getQualifiedMethodNameSimple() + " - SERIOUS ERROR - Interrupted while waiting on active queue.";
            this.bolError = true;
            
            throw new IllegalStateException(this.strStatus, e);
        }
    }

    
    //
    // Iterable<IngestionFrame> Interface
    //
    
    /**
     *
     * @see @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<IngestionFrame> iterator() {
        return this;
    }


    // 
    // Support Methods
    //
    
    
    /**
     * <p>
     * Loads the queue with <code>IngestionFrame</code> instances created from the given factory.
     * </p>
     * <p>
     * The given <code>IngestionFrameFactory</code> instance is used to create <code>{@link #szQueueTotal}</code>
     * <code>ingestionFrame</code> instances where the first <code>{@link #szActiveBuffer}</code> are loaded into
     * to the active memory buffer <code>{@link #queActive}</code> and the rest are serialized to disk at
     * location <code>{@link #fileDiskBuffer}</code>.  The frame counter state variable <code{@link #cntFrames}</code>
     * should be equal to the number of available frames within the queue upon return.
     * </p>
     * 
     * @param facFrames the ingestion frame factory used to create all frame and load the queue
     * 
     * @return  <code>true</code> if all ingestion frames were loaded successfully, or
     *          <code>false</code> an error occurred during the creation and storage process
     * 
     * @throws FileNotFoundException    the temporary file does not exist, cannot be created, etc. (see message)
     * @throws IOException              an I/O error occurred while writing the serialization header to disk
     */
    private boolean    loadDiskQueue(IngestionFrameGeneratorDeprecated facFrames) throws FileNotFoundException, IOException {
        
        // Initialize ingestion frame counter and allocation 
        this.cntFrames= 0;
        this.szAllocTotal = 0;
        
        // Load the active memory buffer first
        for (int i=0; i<this.szActiveBuffer; i++) {
            IngestionFrame  frame = facFrames.build();
            
            this.queActive.offer(frame);
            
            this.szAllocTotal += frame.allocationSizeFrame(); 
            this.cntFrames++;
        }
        
        // Now store the remaining frames to disk
        FileOutputStream    osFile = new FileOutputStream(this.fileDiskBuffer);
        ObjectOutputStream  osObject = new ObjectOutputStream(osFile);

        while (this.cntFrames < this.szQueueTotal) {
            IngestionFrame  frame = facFrames.build();

            osObject.writeObject(frame);
            
            this.szAllocTotal += frame.allocationSizeFrame(); 
            this.cntFrames++;
        }
        osObject.close();
        osFile.close();
        
        // Check that all ingestion frames were created and stored
        if (this.cntFrames == this.szQueueTotal)
            return true;

        // Something went wrong
        return false;
    }
    
    /**
     * <p>
     * Prepares all input stream for ingestion frame deserialization and starts the disk manager thread.
     * </p>
     * <p>
     * This method starts up the persistent storage component of the ingestion frame queue. After returning  
     * ingestion frames will be read from disk (deserialized) and offered to the active memory queue
     * <code>{@link #queActive}</code> using the "disk manager thread."  
     * frames will 
     * </p>
     * 
     * @return <code>true</code> if disk manager thread is launched and operating,
     *         <code>false</code> the disk queue operations failed to fully start
     * 
     * @throws FileNotFoundException    temporary file does not exist, is a directory, etc. (see message)
     * @throws IOException              an I/O error occurred while reading serialization header
     * @throws StreamCorruptedException the input stream header is incorrect
     * @throws InterruptedException     the main thread was interrupted while waiting to check disk manager status 
     */
    private boolean startDiskQueue() throws FileNotFoundException, StreamCorruptedException, IOException, InterruptedException {
        
        // Open the input stream to disk storage for ingestion frame deserialization
        this.isDiskBuffer = new FileInputStream(this.fileDiskBuffer);
        this.isObjectBuffer = new ObjectInputStream(this.isDiskBuffer);
        
        // Create the disk manager task and spawn it as an independent thread
        Runnable    taskDskMgr = this.createDiskManagerTask();
        
        this.thdDiskMgr = new Thread(taskDskMgr);
        this.thdDiskMgr.start();
        
        // Wait a moment before checking thread statues
        Thread.sleep(LNG_THD_WAIT_MS);;
        
//        this.bolActive = thdDiskMgr.isAlive();
        
        // Hopefully task thread will have set active flag by now
        return this.bolActive;
    }
    
    /**
     * <p>
     * Creates and returns a new Disk Manager Task as a <code>Runnable</code> interface. 
     * </p>
     * <p>
     * Creates the Disk Manager task as a lambda function implementation of the <code>{@link Runnable}</code>
     * interface for independent thread execution.  Thus, the returned Disk Manager has access to all attributes
     * of this <code>IngestionFrameQueue</code> instance.
     * </p>  
     * <p>
     * <h2>Operation</h2>
     * The Disk Manager enters into a loop that remains active so long as the following condition holds:
     * <pre>
     *   cntFrames > szActiveBuffer
     * </pre> 
     * The value <code>{@link #cntFrames}</code> is the number of ingestion frames remaining in the queue.
     * If the value is greater than the size of the active buffer then still some remain on disk;
     * Within the loop,
     * the Disk Manager reads <code>Object</code> instances from the object
     * stream <code>{@link #isObjectBuffer}</code>, converts them to <code>IngestionFrame</code> instances,
     * then offers them to the active memory queue <code>{@link #queActive}</code>.  The blocking queue
     * <code>{@link #queActive}</code> will not accept the frame until the queue is below capacity,
     * thus, the loop will block on this operation if the queue is full.
     * </p>
     * <p>
     * Once all ingestion frames have been passing to the active memory queue, the loop exists and calls
     * <code>{@link #stopDiskQueue()}</code> to closes the streams and delete the persistent storage.
     * 
     * @return  the disk manager task as a new <code>Runnable</code> interface instance
     */
    private Runnable    createDiskManagerTask() {
        
        Runnable    taskDskMgr = () -> {
            
            // Set the active flag
            this.bolActive = true;
            
            // Loop counter
            int cntReads = 0;
            
            // Continue looping until all the ingestion frames have been deserialized from disk
            while (cntReads < this.szPersistBuffer) {
                
                try {
                    // Get a frame disk then add it to the blocking queue when capacity allows
                    Object objFrame = this.isObjectBuffer.readObject();
                    IngestionFrame  frmNext = (IngestionFrame)objFrame;

                    this.queActive.put(frmNext);
                    cntReads++;

                } catch (ClassNotFoundException | ClassCastException | IOException e) {
                    
                    // Exception thrown while trying to deserialize ingestion frame 
                    // - this is an error under normal circumstances
                    // - we just shut everything down and punt
                    this.bolError = true;
                    this.strStatus = "Fatal ERROR, Shutting Down: Disk Manager Thread encountered " + e.getClass().getSimpleName() + " exception while reading frame: " + e.getMessage();
                    
                    this.stopDiskQueue();
                    this.bolActive = false;
                    
                    return;
                    
                } catch (InterruptedException e) {
                    
                    // External interruption while trying to put frame into active memory
                    // - This is probably caused by the #clear() method
                    this.strStatus = "Disk Manager interrupted while transferring frames.";
                    this.stopDiskQueue();
                    this.bolActive = false;
                    
                    return;
                }
            }
            
            this.stopDiskQueue();
            this.bolActive = false;
        };
        
        return taskDskMgr;
    }
    
    /**
     * <p>
     * Shuts down the disk operations for the ingestion frame queue.
     * </p>
     * <p>
     * Normally this method is called from the disk manager thread to close all open input streams
     * and delete the temporary file containing the serialized ingestion frames.  The disk manager lives
     * on a separate thread and can call this method from an exceptional condition or via normal exit.
     * This, the error flag <code>{@link #bolError}</code> can be either <code>true</code> or </code>false</code>. 
     * </p>
     */
    private void    stopDiskQueue() {
        
        try {
            this.isObjectBuffer.close();
            this.isDiskBuffer.close();
            this.fileDiskBuffer.delete();
            
            // If this is an error shutdown return now
            if (this.bolError)
                return;
            
            // Else everything shutdown normally
            this.strStatus = "Disk queue stopped normally.";
            
        } catch (IOException e) {
            
            this.bolError = true;
            this.strStatus = JavaRuntime.getQualifiedMethodNameSimple() + " - Exception thrown while attempting to stop disk queue: " + e.getMessage(); 
        }
    }
}
