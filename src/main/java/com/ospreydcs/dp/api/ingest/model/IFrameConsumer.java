/*
 * Project: dp-api-common
 * File:	IFrameConsumer.java
 * Package: com.ospreydcs.dp.api.ingest.model
 * Type: 	IFrameConsumer
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
package com.ospreydcs.dp.api.ingest.model;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.ospreydcs.dp.api.ingest.IngestionFrame;

/**
 * <p>
 * Interface exposing the operations of a blocking <code>IngestionFrame</code> consumer.
 * </p>
 * <p>
 * Classes implementing this interface are assumed to provide implicit throttling at the supplier
 * side of <code>IngestionFrame</code> processing.  Specifically, an ingestion frame supplier can be
 * prevented from offering a frame to the consumer if a particular condition is not met (e.g., a buffer
 * is at capacity).
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 25, 2024
 *
 */
public interface IFrameConsumer extends Consumer<IngestionFrame> {

    /**
     * <p>
     * Determine whether or not the consumer is currently accepting <code>IngestionFrame</code> instances.
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
     * Offers the given list of ingestion frames to the consumer with the possibility of blocking.
     * </p>
     * <p>
     * This operation is intended to offer the capability of blocking if the consumer is not in
     * a predetermined state.  For example, if the consumer is a capacity-restricted queue and the
     * queue is at capacity the consumer may choose to block until queue space becomes available.
     * </p>
     *  
     * @param lstFrames     list of ingestion frames to be consumed
     * 
     * @throws InterruptedException the process was interrupted while waiting for frame to be accepted
     */
    public void offer(List<IngestionFrame> lstFrames) throws InterruptedException;
    
    /**
     * <p>
     * Offers the given list of ingestion frames to the consumer with the possibility of blocking until the given timeout.
     * </p>
     * <p>
     * This operation is intended to offer the capability of blocking if the consumer is not in
     * a predetermined state.  For example, if the consumer is a capacity-restricted queue and the
     * queue is at capacity the consumer may choose to block until queue space becomes available.
     * In contrast to <code>{@link #offer(List)}</code> this method does not block indefinitely, but
     * a given timeout limit is specified after which the operation fails return a value <code>false</code>
     * </p>
     * 
     * @param lstFrames     list of ingestion frames to be consumed
     * @param lngTimeout    timeout limit to wait for consumer acceptance
     * @param tuTimeout     timeout units to wait for consumer acceptance
     * 
     * @return  <code>true</code> if frame was accepted by consumer, <code>false</code> if timeout occurred
     * 
     * @throws InterruptedException the process was interrupted while waiting for frame to be accepted
     */
    public boolean  offer(List<IngestionFrame> lstFrames, long lngTimeout, TimeUnit tuTimeout) throws InterruptedException;

    
    //
    // Default Methods
    //
    
    /**
     * <p>
     * Default convenience operation for offering a single <code>IngestionFrame</code> instance.
     * </p>
     * <p>
     * The argument is converted to a single-element list then deferred to <code>{@link #offer(List)}</code>.
     * </p>
     * 
     * @param frame         ingestion frame to be consumed
     * 
     * @throws InterruptedException the process was interrupted while waiting for frame to be accepted
     * 
     * @see {@link #offer(List)}
     */
    default void offer(IngestionFrame frame) throws InterruptedException {
        this.offer(List.of(frame));
    }
    
    /**
     * <p>
     * Default convenience operation for offering a single <code>IngestionFrame</code> instance with timeout limit.
     * </p>
     * <p>
     * The argument is converted to a single-element list then deferred to <code>{@link #offer(List, long, TimeUnit)}</code>.
     * </p>
     * 
     * @param frame         ingestion frame to be consumed
     * 
     * @param lngTimeout    timeout limit to wait for consumer acceptance
     * @param tuTimeout     timeout units to wait for consumer acceptance
     * 
     * @return  <code>true</code> if frame was accepted by consumer, <code>false</code> if timeout occurred
     * 
     * @throws InterruptedException the process was interrupted while waiting for frame to be accepted
     */
    default boolean offer(IngestionFrame frame, long lngTimeout, TimeUnit tuTimeout) throws InterruptedException {
        return this.offer(List.of(frame), lngTimeout, tuTimeout);
    }
    
    /**
     * <p>
     * Default convenience operation allowing ingestion frame supplier to pre-process frames before acceptance.
     * </p>
     * <p>
     * This method is intended for fast pre-processing of ingestion frames, primarily the setting of properties,
     * attributes, and identifiers.  For example, the operation could set the timestamp of the ingestion
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
     * the operation <code>{@link #offer(List)}</code>.  All behavior henceforth follows from that 
     * operation.
     * </p> 
     *  
     * @param fncProcess    the pre-processing operation to be performed on all frames before submission 
     * @param lstFrames     list of ingestion frames to be processed then consumed
     * 
     * @throws InterruptedException the process was interrupted while waiting for frame to be accepted
     * 
     * @see #offer(List)
     */
    default public void processThenOffer(UnaryOperator<IngestionFrame> fncProcess, List<IngestionFrame> lstFrames) 
            throws InterruptedException {

        // Pre-process all frames in the given list with the given processing function
        List<IngestionFrame>    lstPrcdFrms = lstFrames
                .stream()
                .<IngestionFrame>map(frame -> fncProcess.apply(frame))
                .toList();
        
        // Offer processed frames to consumer
        this.offer(lstPrcdFrms);
    }
    
    /**
     * <p>
     * Default convenience operation allowing ingestion frame supplier to pre-process frames before acceptance and
     * providing a timeout limit for frame acceptance.
     * </p>
     * <p>
     * This method is intended for fast pre-processing of ingestion frames, primarily the setting of properties,
     * attributes, and identifiers.  For example, the operation could set the timestamp of the ingestion
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
     * @param lstFrames     list of ingestion frames to be processed then consumed
     * @param lngTimeout    timeout limit to wait for consumer acceptance
     * @param tuTimeout     timeout units to wait for consumer acceptance
     * 
     * @return  <code>true</code> if frame was accepted by consumer, <code>false</code> if timeout occurred
     * 
     * @throws InterruptedException the process was interrupted while waiting for frame to be accepted
     * 
     * @see #offer(IngestionFrame, long, TimeUnit)
     */
    default public boolean processThenOffer(
            UnaryOperator<IngestionFrame> fncProcess, 
            List<IngestionFrame> lstFrames, 
            long lngTimeout, 
            TimeUnit tuTimeout) 
                    throws InterruptedException {
        
        
        // Pre-process all frames in the given list with the given processing function
        List<IngestionFrame>    lstPrcdFrms = lstFrames
                .stream()
                .<IngestionFrame>map(frame -> fncProcess.apply(frame))
                .toList();
        
        // Offer processed frames to consumer
        return this.offer(lstPrcdFrms, lngTimeout, tuTimeout);
    }
    
    /**
     * <p>
     * Default convenience operation allowing a consumer to pre-process a single frame before acceptance.
     * </p>
     * <p>
     * The argument is converted to a single-element list then deferred to <code>{@link #processThenoffer(List)}</code>.
     * </p>
     * 
     * @param fncProcess    the pre-processing operation to be performed on all frames before submission 
     * @param frame         ingestion frame to be processed and consumed
     * 
     * @throws InterruptedException the process was interrupted while waiting for frame to be accepted
     */
    default void processThenOffer(UnaryOperator<IngestionFrame> fncProcess, IngestionFrame frame) throws InterruptedException {
        this.processThenOffer(fncProcess, List.of(frame));
    }
    
    /**
     * <p>
     * Default convenience operation allowing a consumer to pre-process a single frame before acceptance with timeout.
     * </p>
     * <p>
     * The argument is converted to a single-element list then deferred to <code>{@link #processThenoffer(List, long, TimeUnit)}</code>.
     * </p>
     * 
     * @param fncProcess    the pre-processing operation to be performed on all frames before submission 
     * @param frame         ingestion frame to be processed and consumed
     * @param lngTimeout    timeout limit to wait for consumer acceptance
     * @param tuTimeout     timeout units to wait for consumer acceptance
     * 
     * @return  <code>true</code> if frame was accepted by consumer, <code>false</code> if timeout occurred
     * 
     * @throws InterruptedException the process was interrupted while waiting for frame to be accepted
     * 
     * @see #processThenOffer(UnaryOperator, List, long, TimeUnit)
     */
    default boolean processThenOffer(UnaryOperator<IngestionFrame> fncProcess, IngestionFrame frame, long lngTimeout, TimeUnit tuTimeout) throws InterruptedException {
        return this.processThenOffer(fncProcess, List.of(frame), lngTimeout, tuTimeout);
    }
}
