/*
 * Project: dp-jal
 * File:	IFrameTimestampsFactory.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen
 * Type: 	IFrameTimestampsFactory
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
 * @since Dec 30, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;

import com.ospreydcs.dp.jal.common.DpTimestampCase;
import com.ospreydcs.dp.jal.common.UniformSamplingClock;
import com.ospreydcs.dp.jal.ingest.IngestionFrame;

/**
 * <p>
 * Required operations for all ingestion frame timestamp factories.
 * </p> 
 * <p>
 * <h2>Frame Timestamp Factories</h2>
 * Implementation classes produce the assigned timestamps for ingestion frames <code>{@link IngestionFrame}</code>.  
 * The timestamps should be incremental sequenced blocks and be created as sampling clocks or explicit vectors of
 * <code>{@link Instant}</code> instances.  Each block of timestamps is assigned to a single ingestion frame
 * and the start time of each block should be advanced accordingly.
 * </p>
 * <p>
 * <h2>Ingestion Frame Factories</h2>
 * Implementations of this interface are assumed to be utilized in ingestion frame factories.  Ingestion frame
 * factories produce ingestion frame instances <code>{@link IngestionFrame}</code> containing simulated data
 * for evaluation of the Data Platform Ingestion Service operations.
 * </p>  
 *
 * @author Christopher K. Allen
 * @since Dec 30, 2025
 *
 */
public interface IFrameTimestampsFactory {


    //
    // Configuration
    //
    
    /**
     * <p>
     * Returns the number of timestamps generated for each ingestion frame.
     * </p>
     * <p>
     * The returned value is the number of timestamps assigned to each ingestion frame, whether by
     * uniform sampling clock or explicit timestamp list.  That is, it is the sample count contained
     * in each ingestion frame.
     * </p>
     * <p>
     * This value is typically used in frame column factories to assigned the size of the columns
     * generated.
     * </p>
     * 
     * @return  the number of aligned samples in each ingestion frame
     */
    public int  getSampleCount();
        
    /**
     * <p>
     * Returns the preferred timestamp case for ingestion frames populated by the frame timestamp factory.
     * </p>
     * <p>
     * The returned value is a configuration parameter for the frame timestamp factory.
     * The actual timestamps used for an ingestion frame are determined by the methods
     * <code>{@link #nextUniformClock(Instant)}</code> and <code>{@link #nextTimestampVector(Instant)}</code>.
     * (Note that these methods can be used interchangeably.)
     * </p>   
     * 
     * @return  the preferred timestamp case as configured by the frame timestamp factory
     */
    public DpTimestampCase  getTimestampCase();
    
//    /**
//     * <p>
//     * Returns the first start instant for the frame timestamp factory.
//     * </p>
//     * <p>
//     * The returned value is a configuration parameter for the frame timestamp factory.
//     * Note that this value is typically used only once.
//     * Although this value may be computed, the returned value is always the same.
//     * </p>
//     *  
//     * @return  an <code>Instant</code> containing the first timestamp for the factory
//     * 
//     * @throws DateTimeException    overflow occurred in <code>Instant</code> addition 
//     * @throws ArithmeticException  <code>Instant</code> addition failed or overflow in <code>Duration</code> multiplication  
//     */
//    public Instant  getStartInstant() throws DateTimeException, ArithmeticException;

    
    //
    // Operations
    //
    
//    /**
//     * <p>
//     * Computes and returns the start time for the next frame timestamp block from the given instant.
//     * </p>
//     * <p>
//     * This method is used to advance the timestamp start time for ingestion frames.
//     * The argument is assumed to be the start time of a current ingestion frame and the
//     * returned value is then the start time instant for the next ingestion frame timestamps.
//     * </p>
//     *  
//     * @param insStart  the current start time for current ingestion frame timestamps
//     * 
//     * @return  the start time for the next ingestion frame timestamps
//     * 
//     * @throws DateTimeException    overflow occurred in <code>Instant</code> addition 
//     * @throws ArithmeticException  <code>Instant</code> addition failed or overflow in <code>Duration</code> multiplication  
//     */
//    public Instant nextFrameStart(Instant insStart) throws DateTimeException, ArithmeticException;
    
    /**
     * <p>
     * Creates a new <code>UniformSamplingClock</code> instance for the given start time.
     * </p>
     * <p>
     * The remain parameters of the sampling clock (i.e., period and sample count) are taken from the
     * configuration of the timestamp factory given at construction.
     * </p>
     * 
     * @param insStart  the start time for the new sampling clock
     * 
     * @return  a new <code>UniformSamplingClock</code> instance for the given start time
     * 
     * @throws IllegalArgumentException the sample count was negative and/or the period was non-positive
     */
    public UniformSamplingClock    nextUniformClock() throws IllegalArgumentException;
    
    /**
     * <p>
     * Creates a new ordered list (vector) of timestamp instants with the given start time.
     * </p>
     * <p>
     * The number of timestamps and the interval between timestamps (i.e., the period) is taken from the
     * configuration of the timestamp factory given at construction.
     * </p>
     * 
     * @param insStart  the first timestamp instant with the returned vector
     * 
     * @return  an ordered vector of <code>Instant</code> objects representing timestamps from a uniform clock
     * 
     * @throws DateTimeException    internal <code>Instant</code> addition failed
     * @throws ArithmeticException  numeric overflow occurred in <code>Instant</code> addition 
     */
    public ArrayList<Instant>   nextTimestampVector() throws DateTimeException, ArithmeticException;

}
