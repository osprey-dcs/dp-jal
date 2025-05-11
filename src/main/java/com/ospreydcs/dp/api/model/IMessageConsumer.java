/*
 * Project: dp-api-common
 * File:	IMessageConsumer.java
 * Package: com.ospreydcs.dp.api.ingest.model
 * Type: 	IMessageConsumer
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
 * @since Jul 25, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import com.google.protobuf.GeneratedMessage;
import com.ospreydcs.dp.api.ingest.IngestionFrame;

/**
 * <p>
 * Interface exposing the operations of a blocking <code>T</code> resource consumer.
 * </p>
 * <p>
 * Classes implementing this interface are assumed to provide implicit throttling at the supplier
 * side of <code>T</code> processing.  For example, an ingestion frame producer can be
 * prevented from offering a frame to the consumer if a particular condition is not met (e.g., a buffer
 * is at capacity).
 * </p>
 * 
 * @param   <T>     resource type being consumed
 *
 * @author Christopher K. Allen
 * @since Jul 25, 2024
 *
 */
public interface IMessageConsumer<T extends GeneratedMessage> {

    /**
     * <p>
     * Activates the consumer of <code>T</code> resources.
     * </p>
     * <p>
     * After invoking this method the consumer instance is ready for resource acceptance and
     * processing.  The method <code>{@link #isAccepting()}</code> should return <code>true</code>
     * after returning.
     * </p>
     * <h2>Operation</h2>
     * This method starts all resource acceptance and processing which are continuously enabled
     * until explicitly shut down.  
     * </p>
     * <p>
     * <h2>Shutdowns</h2>
     * Typically for proper operation consumers requires a shutdown operation when no longer needed.  
     * Use either <code>{@link #shutdown()}</code> or <code>{@link #shutdownNow()}</code> to shutdown the 
     * consumer.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Often consumers can be cycled through activations and shutdowns. 
     * </li>
     * <li>
     * A shutdown operation should always be invoked when the consumer is no longer needed.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  <code>true</code> if the consumer was successfully activated,
     *          <code>false</code> if the consumer was already enabled
     *          
     * @throws RejectedExecutionException   processing tasks could not be scheduled for execution
     * @return
     */
    public boolean  activate();
    
    /**
     * <p>
     * Determine whether or not the consumer is currently accepting <code>T</code> type resource instances.
     * </p>
     * <p>
     * If the value <code>true</code> is returned then the method <code>{@link #offer(IngestionFrame)}</code>
     * should immediately accept any offered ingestion frame.  Otherwise, blocking will occur until the 
     * consumer is in the proper state (determined by context).
     * </p>
     * 
     * @return  <code>true</code> if the consumer is accepting ingestion frames, <code>false</code> otherwise
     */
    public boolean isAccepting();
    
    /**
     * <p>
     * Offers the given list of Protocol Buffers messages to the consumer with the possibility of blocking.
     * </p>
     * <p>
     * This operation is intended to offer the capability of blocking if the consumer is not in
     * a predetermined state.  For example, if the consumer is a capacity-restricted queue and the
     * queue is at capacity the consumer may choose to block until queue space becomes available.
     * </p>
     *  
     * @param lstMsgs     list of messages to be consumed
     * 
     * @throws IllegalStateException    consumer is not accepting resources 
     * @throws InterruptedException     the process was interrupted while waiting for resources to be accepted
     */
    public void offer(List<T> lstMsgs) throws IllegalStateException, InterruptedException;
    
    /**
     * <p>
     * Offers the given list of Protocol Buffers messages to the consumer with the possibility of blocking until 
     * the given timeout.
     * </p>
     * <p>
     * This operation is intended to offer the capability of blocking if the consumer is not in
     * a predetermined state.  For example, if the consumer is a capacity-restricted queue and the
     * queue is at capacity the consumer may choose to block until queue space becomes available.
     * In contrast to <code>{@link #offer(List)}</code> this method does not block indefinitely, but
     * a given timeout limit is specified after which the operation fails return a value <code>false</code>
     * </p>
     * 
     * @param lstMsgs       list of messages to be consumed
     * @param lngTimeout    timeout limit to wait for consumer acceptance
     * @param tuTimeout     timeout units to wait for consumer acceptance
     * 
     * @return  <code>true</code> if resource was accepted by consumer, <code>false</code> if timeout occurred
     * 
     * @throws IllegalStateException    consumer is not accepting resources 
     * @throws InterruptedException the process was interrupted while waiting for resources to be accepted
     */
    public boolean  offer(List<T> lstMsgs, long lngTimeout, TimeUnit tuTimeout) throws IllegalStateException, InterruptedException;

    /**
     * <p>
     * Performs an orderly shut down of the resource consumer.
     * </p>
     * <p>
     * This method is available for producers of <code>T</code> resources to signal a "completion" event
     * and that no more resources will be offered.
     * After invoking this method all internal consumption processing tasks are allowed to finish 
     * but no new <code>T</code> resources will be accepted.  Specifically, the <code>{@link #isAccepting()}</code>
     * method will returned <code>false</code> internal processing should be allowed to continue.
     * </p>
     * <p>
     * This method will block until all currently executing processing tasks have finished.
     * The queue of <code>T</code> messages may still contain available messages however. 
     * </p> 
     * <p>
     * To force a hard shutdown of the resource consumer where all internal processing is immediately terminated 
     * and any remaining (uncompleted) products are discarded use method <code>#shutdownNow()</code>.  
     * </p>
     * 
     * @return <code>true</code> if the consumer was successfully shutdown,
     *         <code>false</code> if the consumer was not enabled or shutdown operation failed
     * 
     * @throws InterruptedException interrupted while waiting for all messages to be consumed
     */
    public boolean  shutdown() throws InterruptedException;
    
    /**
     * <p>
     * Performs a hard shutdown of the resource consumer.
     * </p>
     * <p>
     * All currently executing processing tasks are terminated and all
     * internal resources are cleared.  (Any outgoing resources are also cleared.)  
     * The method returns immediately upon terminating all activity.
     * </p>
     */
    public void shutdownNow();
    
    
    //
    // Default Methods
    //
    
    /**
     * <p>
     * Default convenience operation for offering a single <code>T</code> type message instance.
     * </p>
     * <p>
     * The argument is converted to a single-element list then deferred to <code>{@link #offer(List)}</code>.
     * </p>
     * 
     * @param msg         message instance to be consumed
     * 
     * @throws InterruptedException the process was interrupted while waiting for resource to be accepted
     * 
     * @see {@link #offer(List)}
     */
    default void offer(T msg) throws InterruptedException {
        this.offer(List.of(msg));
    }
    
    /**
     * <p>
     * Default convenience operation for offering a single <code>T</code> type message instance with timeout limit.
     * </p>
     * <p>
     * The argument is converted to a single-element list then deferred to <code>{@link #offer(List, long, TimeUnit)}</code>.
     * </p>
     * 
     * @param msg          message instance to be consumed
     * 
     * @param lngTimeout    timeout limit to wait for consumer acceptance
     * @param tuTimeout     timeout units to wait for consumer acceptance
     * 
     * @return  <code>true</code> if frame was accepted by consumer, <code>false</code> if timeout occurred
     * 
     * @throws InterruptedException the process was interrupted while waiting for resource to be accepted
     */
    default boolean offer(T msg, long lngTimeout, TimeUnit tuTimeout) throws InterruptedException {
        return this.offer(List.of(msg), lngTimeout, tuTimeout);
    }
    
    /**
     * <p>
     * Default convenience operation allowing message supplier to pre-process message before consuming.
     * </p>
     * <p>
     * This method is intended for fast pre-processing of messages before offering, primarily the setting of properties,
     * attributes, and identifiers.  
     * For example, in the case of <code>T = IngestionFrame</code> the operation could set the provider UID of the ingestion
     * frame before it is given to the consumer.  In that case the <code>UnaryOperator</code> argument can be
     * defined as the following lambda function:
     * <pre>
     *      UnaryOperation<IngestionFrame> fnc = (frame) -> {
     *          frame.setProviderUid(recUid);
     *          
     *          return frame;
     *      }
     * </pre> 
     * where the variable <code>recUid</code> is known at the time of frame consumption.
     * </p>
     * <p>
     * Once the given resources are processed they are then offered to the consumer by deferring to
     * the operation <code>{@link #offer(List)}</code>.  All behavior henceforth follows from that 
     * operation.
     * </p> 
     *  
     * @param fncProcess    the pre-processing operation to be performed on all resources before submission 
     * @param lstMsgs       list of message instances to be processed then consumed
     * 
     * @throws InterruptedException the process was interrupted while waiting for resources to be accepted
     * 
     * @see #offer(List)
     */
    default public void processThenOffer(UnaryOperator<T> fncProcess, List<T> lstMsgs) 
            throws InterruptedException {

        // Pre-process all resources in the given list with the given processing function
        List<T> lstPrcdFrms = lstMsgs
                .stream()
                .<T>map(frame -> fncProcess.apply(frame))
                .toList();
        
        // Offer processed resources to consumer
        this.offer(lstPrcdFrms);
    }
    
    /**
     * <p>
     * Default convenience operation allowing message supplier to pre-process messages before consuming and
     * providing a timeout limit for acceptance.
     * </p>
     * <p>
     * This method is intended for fast pre-processing of messages, primarily the setting of properties,
     * attributes, and identifiers.  
     * For example, in the <code>T = IngestionFrame</code> the operation could set the provider UID of the ingestion
     * frame before it is given to the consumer.  In that case the <code>UnaryOperator</code> argument can be
     * defined as the following lambda function:
     * <pre>
     *      UnaryOperation<IngestionFrame> fnc = (frame) -> {
     *          frame.setProviderUid(recUid);
     *          
     *          return frame;
     *      }
     * </pre> 
     * where the variable <code>recUid</code> is known at the time of frame consumption.
     * </p>
     * <p>
     * Once the given ingestion frames are processed they are then offered to the consumer by deferring to
     * the operation <code>{@link #offer(List, long, TimeUnit)}</code>.  All behavior henceforth follows from that 
     * operation.
     * </p> 
     * 
     * @param fncProcess    the pre-processing operation to be performed on all frames before submission 
     * @param lstMsgs       list of message instances to be processed then consumed
     * @param lngTimeout    timeout limit to wait for consumer acceptance
     * @param tuTimeout     timeout units to wait for consumer acceptance
     * 
     * @return  <code>true</code> if frame was accepted by consumer, <code>false</code> if timeout occurred
     * 
     * @throws InterruptedException the process was interrupted while waiting for resources to be accepted
     * 
     * @see #offer(IngestionFrame, long, TimeUnit)
     */
    default public boolean processThenOffer(UnaryOperator<T> fncProcess, List<T> lstMsgs, long lngTimeout, TimeUnit tuTimeout) 
                    throws InterruptedException {
        
        // Pre-process all resources in the given list with the given processing function
        List<T> lstPrcdFrms = lstMsgs
                .stream()
                .<T>map(frame -> fncProcess.apply(frame))
                .toList();
        
        // Offer processed resources to consumer
        return this.offer(lstPrcdFrms, lngTimeout, tuTimeout);
    }
    
    /**
     * <p>
     * Default convenience operation allowing a supplier to pre-process a single message instance before consuming.
     * </p>
     * <p>
     * The argument is converted to a single-element list then deferred to <code>{@link #processThenoffer(List)}</code>.
     * </p>
     * 
     * @param fncProcess    the pre-processing operation to be performed on all frames before submission 
     * @param msg           resource instance to be processed and consumed
     * 
     * @throws InterruptedException the process was interrupted while waiting for resource to be accepted
     */
    default void processThenOffer(UnaryOperator<T> fncProcess, T msg) throws InterruptedException {
        this.processThenOffer(fncProcess, List.of(msg));
    }
    
    /**
     * <p>
     * Default convenience operation allowing a consumer to pre-process a single resource instance before consuming
     * with timeout.
     * </p>
     * <p>
     * The argument is converted to a single-element list then deferred to <code>{@link #processThenoffer(List, long, TimeUnit)}</code>.
     * </p>
     * 
     * @param fncProcess    the pre-processing operation to be performed on all frames before submission 
     * @param msg           message instance to be processed and consumed
     * @param lngTimeout    timeout limit to wait for consumer acceptance
     * @param tuTimeout     timeout units to wait for consumer acceptance
     * 
     * @return  <code>true</code> if frame was accepted by consumer, <code>false</code> if timeout occurred
     * 
     * @throws InterruptedException the process was interrupted while waiting for resource to be accepted
     * 
     * @see #processThenOffer(UnaryOperator, List, long, TimeUnit)
     */
    default boolean processThenOffer(UnaryOperator<T> fncProcess, T msg, long lngTimeout, TimeUnit tuTimeout) throws InterruptedException {
        return this.processThenOffer(fncProcess, List.of(msg), lngTimeout, tuTimeout);
    }
}
