/*
 * Project: dp-jal
 * File:	IFrameFactory.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen
 * Type: 	IFrameFactory
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
 * @since Jan 2, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen;

import java.time.DateTimeException;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.common.DpTimestampCase;
import com.ospreydcs.dp.jal.ingest.IngestionFrame;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.SampleBlockConfigDep;

/**
 * <p>
 * Required operations for ingestion frame factories producing simulated ingestion data for the Data Platform.
 * </p>
 * <p>
 * <h2>Ingestion Frames</h2>
 * <ul>
 * <li><b>Data Columns</b>
 * <br/>
 * The number columns within each in ingestion frame is given by the size of the 
 * <code>{@link #getColumnCount()}</code>.  Each column has a name taken from the given set.
 * </li>
 * <li><b>Data Types</b>
 * <br/>
 * Ingestion frame factory should supported data types of that described by the enumeration 
 * <code>{@link DpSupportedType}</code>.  The data type of data columns within a returned ingestion frame can
 * vary, that is, they can be heterogeneous.  
 * <li><b>Timestamps</b>
 * <br/>
 * The timestaps are generated internally according to the number of samples specified by 
 * <code>{@link #getSampleCount()}</code> method. 
 * The sampling period can vary, for example, when the ingestion frame factory is configurated for explicit
 * timestamp lists.
 * </li>
 * <li><b>Sample Count</b>
 * <br/>
 * The number of samples for each sample process within each ingestion frame created is given by the method
 * <code>{@link #getSampleCount()}</code>.  That is, all data columns within the generated
 * ingestion frames have size .  That is, the generated 
 * ingestion frames have <code>{@link #getSampleCount()}</code> rows.
 * </li>
 * <li><b>Sample Period</b>
 * <br/>
 * All timestamps within produced ingestion frames are typically generated with sampling period .  
 * The timestamps themselves are represented either by  
 *   <ul>
 *   <li>
 *   a uniform sampling clock if 
 *   <code>{@link #getTimestampType()}</code> == <code>{@link DpTimestampCase#SAMPLING_CLOCK}</code>
 *   </li>
 *   <li> 
 *   or an explicit timestamp list of
 *   <code>{@link #getTimestampType()}</code> == <code>{@link DpTimestampCase#TIMESTAMP_LIST}</code>.
 *   </li>
 *   </ul>
 * </li>
 * </ul>
 * <p>
 * <h2>Timestamps</h2>
 * By default the start time (first timestamp) of the first ingestion frame produced 
 * (i.e., via the <code>{@link #nextFrame()}</code> method) 
 * will be the inception time of the Data Platform Test Archive contained in class constant <code>{@link #INS_TMS_START_DEF}</code>.  
 * In all subsequent ingestion frames the initial timestamp is advanced such that it follows directly from the last timestamp 
 * of the previous ingestion frame.  The interval between timestamps (i.e., the "period") is given by the field
 * <code>{@link SampleBlockConfigDep#tmaPeriod()}</code> within the configuration record.
 * </p>
 * <p>
 * The method used to express timestamps for all generated ingestion frames is given by the field 
 * <code>{@link SampleBlockConfigDep#enmTmsCase()}</code>.
 * Within <code>IngestionFrame</code> instances timestamps can be specified with either a <code>UniformSamplingClock</code>
 * object (for sampling processes that have a constant period) or with an explicit list of timestamp <code>Instant</code>
 * values (i.e., a <code>List&lt;Instant&gt;</code> object).  Clearly the former method is less expensive but not
 * as general as the latter method.   (Note that this has direct correspondence to the Data Platform gRPC messages
 * for timestamps given by <code>SamplingClock</code> and <code>TimestampList</code>, respectively, both attributes
 * of a <code>DataTimestamps</code> message).  
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 2, 2026
 *
 */
public interface IFrameFactory {
    
    
    //
    // Configuration
    //

    /**
     * <p>
     * Returns the number of samples within each ingestion frame (i.e., the row count).
     * </p>
     * 
     * @return   the number of sample values in each data column
     */
    public int  getSampleCount();
    
    /**
     * <p>
     * Returns the current number of data columns in each ingestion frame.
     * </p>
     * 
     * @return  the number of data columns in each ingestion frame
     */
    public int  getColumnCount();
    
    /**
     * <p>
     * Returns the timestamp representation used for each ingestion frame.
     * </p>
     * <p>
     * Timestamp can be represented as uniform sampling clocks (i.e., <code>{@link DpTimestampCase#SAMPLING_CLOCK}</code>)
     * or as explicit timestamp lists (i.e., <code>{@link DpTimestampCase#TIMESTAMP_LIST}</code>).  Explicit timestamp
     * lists are more general but require more memory and processing resources.
     * </p>
     * 
     * @return  the timestamp representation for all ingestion frames as a <code>{@link DpTimestampCase}</code> constant
     * 
     * @see DpTimestampCase
     */
    public DpTimestampCase  getTimestampType(); 
    
//    /**
//     * <p>
//     * Returns the current timestamp start time instance for the next ingestion frame.
//     * </p>
//     * <p>
//     * Note that this value is a state variable and increases with the number of ingestion
//     * frames produced.
//     * </p>
//     * 
//     * @return  the timestamp start time of the next ingestion frame
//     */
//    public Instant  getSampleStartTime();
    

    //
    // Operations
    //
    
    /**
     * <p>
     * Creates a new <code>IngestionFrame</code> instance containing simulated data.
     * </p>
     * <p>
     * The returned ingestion frame is configured according to the configuration record provided 
     * at construction.  This method can be invoked multiple times each time provided a new ingestion
     * frame with different data, and timestamps starting from the previous invocation.
     * <p>
     * <h2>NOTES:</h2>
     * Any exceptions here originate in the configuration record provided at construction.
     * </p>
     * 
     * @return  a new <code>IngestionFrame</code> containing simulated data
     * 
     * @throws IllegalArgumentException     the sample count was negative and/or the period was non-positive
     * @throws IllegalStateException        there are no <code>IDataColumnFactory</code> instances
     * @throws ConfigurationException       <s>created a data column with the wrong size (sample count)</s> 
     * @throws DateTimeException            internal <code>Instant</code> addition failed
     * @throws ArithmeticException          numeric overflow occurred in <code>Instant</code> addition 
     * @throws UnsupportedOperationException an unsupported timestamp case was encountered
     */
    public IngestionFrame   nextFrame() throws IllegalArgumentException, IllegalStateException, DateTimeException, ArithmeticException, UnsupportedOperationException;
    
}
