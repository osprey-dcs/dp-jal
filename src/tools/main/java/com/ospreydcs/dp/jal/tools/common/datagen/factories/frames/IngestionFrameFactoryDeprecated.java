/*
 * Project: dp-jal
 * File:	IngestionFrameFactoryDeprecated.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.frames
 * Type: 	IngestionFrameFactoryDeprecated
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
 * @since Dec 1, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.frames;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.common.DpTimestampCase;
import com.ospreydcs.dp.jal.common.IDataColumn;
import com.ospreydcs.dp.jal.common.UniformSamplingClock;
import com.ospreydcs.dp.jal.ingest.IngestionFrame;
import com.ospreydcs.dp.jal.tools.common.datagen.IDataColumnFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.frames.JalToolsFramesConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Class for generating <code>IngestionFrame</code> instances containing simulated data.
 * </p>
 * <p>
 * A single class instance of <code>IngestionFrameFactoryDeprecated</code> can create multiple <code>IngestionFrame</code>
 * objects, which will contain different data and sequential time stamps.  Use the <code>{@link #nextFrame()}</code>
 * for <code>IngestionFrame</code> generation after class instantiation.
 * </p>
 * <p>
 * <h2>Configuration</h2>
 * Instances of <code>IngestionFrameFactoryDeprecated</code> are configured upon creation/construction.  A record object of
 * type <code>{@link SampleBlockConfigDep}</code> is required for instantiation.  The record contains all fields necessary
 * for full configuration and all <code>IngestionFrameFactoryDeprecated</code> objects are ready for ingestion frame creation
 * (i.e., invoking the <code>{@link #nextFrame()}</code> method) after instantiation.
 * <p> 
 * </p>
 * <p>
 * <h2>Ingest Frames</h2>
 * <ul>
 * <li><b>Data Columns</b>
 * <br/>
 * The number columns within each in ingestion frame is given by the size of the 
 * <code>{@link SampleBlockConfigDep#setPvNames()}</code> set.  Each column has a name taken from the given set.
 * </li>
 * <li><b>Data Types</b>
 * <br/>
 * This class currently supports only scalar data types of that described by the enumeration 
 * <code>{@link JalScalarType}</code>, and given by the field <code>{@link SampleBlockConfigDep#enmDataType()}</code>
 * within the creation configuration record. The data types of all data within each generated ingestion frame will 
 * all be identical (although the data values will not).
 * <li><b>Timestamps</b>
 * <br/>
 * The timestaps are generated internally according to the number of samples specified by 
 * <code>{@link SampleBlockConfigDep#cntSamples()}</code>, the sampling period <code>{@link SampleBlockConfigDep#tmaPeriod()}</code>,
 * and by the desired timestamp representation <code>{@link SampleBlockConfigDep#enmTmsCase()}</code>.  
 * See more details below.
 * </li>
 * <li><b>Sample Count</b>
 * <br/>
 * The number of samples for each sample process within each ingestion frame created is given by the field
 * <code>{@link SampleBlockConfigDep#cntSamples()}</code>.  That is, all data columns within the generated
 * ingestion frames have size <code>{@link SampleBlockConfigDep#cntSamples()}</code>.  That is, the generated 
 * ingestion frames have <code>{@link SampleBlockConfigDep#cntSamples()}</code> rows.
 * </li>
 * <li><b>Sample Period</b>
 * <br/>
 * All timestamps within produced ingestion frames are generated  with period given by 
 * <code>{@link SampleBlockConfigDep#tmaPeriod()}</code>.  The timestamps themselves are represented either by  
 *   <ul>
 *   <li>
 *   a uniform sampling clock if 
 *   <code>{@link SampleBlockConfigDep#enmTmsCase()}</code> == <code>{@link DpTimestampCase#SAMPLING_CLOCK}</code>
 *   </li>
 *   <li> 
 *   or an explicit timestamp list of
 *   <code>{@link SampleBlockConfigDep#enmTmsCase()}</code> == <code>{@link DpTimestampCase#TIMESTAMP_LIST}</code>.
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
 * The start time can be modified by using the <code>{@link setStartTime}</code> method before building.
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
 *
 * @author Christopher K. Allen
 * @since Dec 1, 2025
 * 
 * @deprecated  Replaced by IngestionFrameFactory
 */
@Deprecated(since="Dec 5, 2025", forRemoval=true)
public class IngestionFrameFactoryDeprecated {

    
    //
    // JAL Library Resources
    //
    
    /** Default configuration parameters for the JAL Tools */
    private static final JalToolsFramesConfig     CFG_DEF = JalToolsConfig.getInstance().datagen.frame;
    
    
    //
    // Class Constants
    //
    
    /** Name of the frame generator */
    public static final String  STR_SRC_NAME = IngestionFrameFactoryDeprecated.class.getSimpleName();
    
    /** Environment variable for current user */
    public static final String  STR_USERNAME = "USERNAME";
    
    
    /** Default timestamp type for ingestion frames */
    public static final DpTimestampCase     ENM_TMS_TYPE = CFG_DEF.timestamps.type;
    
    /** Default starting time instant for ingestion frame timestamps */
    public static final Instant             INS_TMS_START_DEF = CFG_DEF.timestamps.startInstant();
    
    /** Default sampling period for ingestion frame timestamps */
    public static final Duration            DUR_TMS_PER_DEF = CFG_DEF.timestamps.periodDuration();
    
    /** Default sample count per ingestion frame */
    public static final int                 INT_TMS_CNT_DEF = CFG_DEF.timestamps.count;
    
    
    /** Default ingestion frame tag values */
    private static final List<String>        LST_FRM_TAGS_DEF = new ArrayList<>( CFG_DEF.tags.values );
    
    /** Default ingestion frame attribute pairs */
    private static final Map<String, String> MAP_FRM_ATTRS_DEF = new HashMap<>( CFG_DEF.attributes.pairs );
    
    
    /** Common ingestion frame tag values */
    private static final List<String>       LST_FRM_TAGS_CMN = new LinkedList<>();
    
    /** Common ingestion frame attribute pairs */
    private static final Map<String, String> MAP_FRM_ATTRS_CMN = new HashMap<>();
    
    
    /** Common tags and attributes for ingestion frames */
    static {
        LST_FRM_TAGS_CMN.add(STR_SRC_NAME);
        
        String  strUser = System.getenv(STR_USERNAME);
        Instant insNow = Instant.now();
        
        MAP_FRM_ATTRS_CMN.put("Source", STR_SRC_NAME);
        MAP_FRM_ATTRS_CMN.put("Initiatiated", insNow.toString());
        MAP_FRM_ATTRS_CMN.put("User", strUser);
    }
    
    
    //
    // Defining Attributes
    //
    
    /** The number of samples for each process variable in the ingestion frame */
    private final int               cntSamples;
    
    /** The sample period for each process variable in ingestion frames */
    private final Duration          durPeriod;
    
    /** The method of specifying timestamps within each ingestion frame */
    private final DpTimestampCase   enmTmsCase;
    
    
    //
    // Instance Resources
    //
    
    /** The data column generator used for creating sample processes within each ingestion frame - configured from input record */
    private final List<IDataColumnFactory<Object>>    lstColFacs = new LinkedList<>();
    
    
    //
    // State Variables
    //
    
    /** Index counter for ingestion frames created - used for frame label creation */
    private int         indFrame; // = 0;
    
    /** The start time for each ingestion frame - advanced after each frame created */
    private Instant     insStart; // = INS_TMS_START_DEF;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>IngestionFrameFactoryDeprecated</code> instance.
     * </p>
     *
     */
    public IngestionFrameFactoryDeprecated(Instant insStart, int cntSamples, Duration durPeriod, DpTimestampCase enmTmsCase, Collection<IDataColumnFactory<Object>> setColFacs) {
        this.insStart = insStart;
        this.cntSamples = cntSamples;
        this.durPeriod = durPeriod;
        this.enmTmsCase = enmTmsCase;
        
        this.lstColFacs.addAll(setColFacs);
        
        this.indFrame = 0;
    }

    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Sets the initial timestamp start time for the next <code>IngestionFrame</code> build operation (i.e., <code>{@link #nextFrame()}</code>).
     * </p>
     * <p>
     * Overrides the current value of the next ingestion frame's start time.  By default the start time
     * at the time of creation/construction is that of <code>{@link #INS_TMS_START_DEF}</code>, which is the
     * inception time of the Data Platform Test Archive.
     * </p>
     * 
     * @param insStart  new start time for the next <code>IngestionFrame</code> build operation 
     */
    public void setStartTime(Instant insStart) {
        this.insStart = insStart;
    }
    
    /**
     * <p>
     * Adds a new column to the ingestion frame factory column factory collection.
     * </p>
     * <p>
     * Adds the column factory to the current collection of column factories.  The given column
     * factory is then used for all subsequent invocations of <code>{@link #nextFrame()}</code>;
     * ingestion frames will contain the additional data column at the end of the returned
     * data column vector.  
     * The given column factory must create columns of the appropriate size or an exception is
     * thrown.
     * </p>
     * 
     * @param facCol    new column factory for ingestion frame column creation
     * 
     * @throws ConfigurationException   <s>data column factory has the wrong size (sample count)</s> 
     */
    public void addDataColumn(IDataColumnFactory<Object> facCol) /*throws ConfigurationException */{
        
        // Check factory configuration
//        if (facCol.getColumnSize() != this.getSampleCount())
//            throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() 
//                        + " Column factory " + facCol.getColumnName() 
//                        + " has wrong size " + facCol.getColumnSize()
//                        + " != " + this.getSampleCount()
//                        );
        
        this.lstColFacs.add(facCol);
    }
    
    
    /**
     * <p>
     * Returns the number of samples within each ingestion frame (i.e., the row count).
     * </p>
     * 
     * @return   the number of sample values in each data column
     */
    public int  getSampleCount() {
        return this.cntSamples;
    }
    
    /**
     * <p>
     * Returns the current timestamp start time instance for the next ingestion frame.
     * </p>
     * <p>
     * Note that this value is a state variable and increases with the number of ingestion
     * frames produced.
     * </p>
     * 
     * @return  the timestamp start time of the next ingestion frame
     */
    public Instant  getSampleStartTime() {
        return this.insStart;
    }
    
    /**
     * <p>
     * Returns the sampling period for the timestamps in each ingestion frame.
     * </p>
     * <p>
     * For explicit timestamp lists this value is the time interval between timestamps.
     * </p> 
     * 
     * @return  the sampling period for sample values 
     */
    public Duration getSamplePeriod() {
        return this.durPeriod;
    }
    
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
    public DpTimestampCase  getTimestampType() {
        return this.enmTmsCase;
    }
    
    /**
     * <p>
     * Returns the current number of data columns in each ingestion frame.
     * </p>
     * 
     * @return  the number of data columns in each ingestion frame
     */
    public int  getColumnCount() {
        return this.lstColFacs.size();
    }
    

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
    public IngestionFrame   nextFrame() throws IllegalArgumentException, IllegalStateException, /* ConfigurationException,*/ DateTimeException, ArithmeticException, UnsupportedOperationException {
        
        // Check state
        if (this.lstColFacs.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - There are no IDataColumnFactory for column creation.");
        
        ArrayList<IDataColumn<Object>>  vecCols = this.nextColumns();
        
        IngestionFrame frmNext = switch (this.enmTmsCase) {
        case SAMPLING_CLOCK -> IngestionFrame.from(this.nextUniformClock(insStart), vecCols);   // throws IllegalArgumentException
        case TIMESTAMP_LIST -> IngestionFrame.from(this.nextTimestampVector(insStart), vecCols);// throws DateTimeException, ArithmeticException
        case UNSUPPORTED_CASE -> {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Timestamp case " + this.enmTmsCase + " is not viable.";
            
            throw new UnsupportedOperationException(strMsg);
            }
        default -> { 
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Timestamp case " + this.enmTmsCase + " is not supported.";
            
            throw new UnsupportedOperationException(strMsg);
            }
        };
        
        frmNext.setFrameLabel(this.nextFrameLabel());
        frmNext.setFrameTimestamp(this.insStart);
        frmNext.addTags(LST_FRM_TAGS_CMN);
        frmNext.addAttributes(MAP_FRM_ATTRS_CMN);
        
        this.insStart = this.nextStartInstant(insStart);    // throws DateTimeException, ArithmeticException
        
        return frmNext;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates a new collection of simulated data columns for the ingestion frame.
     * </p>
     * <p>
     * Uses the collection <code>{@link #lstColFacs}</code> to create each <code>IDataColumn</code>
     * containing simulated data.  One data column is created for each <code>IDataColumnFactory</code>
     * instance.  The returned vector of contains the data column created from each data column
     * factory currently maintained by this ingestion frame factory.
     * </p>   
     *  
     * @return  a new vector of <code>IDataColumn</code> instances containing simulated data
     * 
     * @throws ConfigurationException   created a data column with the wrong size (sample count) 
     */
    private ArrayList<IDataColumn<Object>>  nextColumns() /* throws ConfigurationException */ {
        
        // Create the returned vector container
        ArrayList<IDataColumn<Object>>  vecCols = new ArrayList<>(this.getColumnCount());

        // Populate the vector of columns - check size as we go
        for (IDataColumnFactory<Object> fac : this.lstColFacs) {
//            if (fac.getColumnSize() != this.cntSamples)
//                throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple()
//                            + " - Column " + fac.getColumnName() + " has wrong size "
//                            + fac.getColumnSize() + " != " + this.cntSamples
//                            );
//            
//            IDataColumn<Object>     col = fac.nextColumn();
            IDataColumn<Object>     col = fac.nextColumn(this.cntSamples);
            
            vecCols.add(col);
        }
        
        return vecCols;
    }
    
    /**
     * <p>
     * Creates a new <code>UniformSamplingClock</code> instance for the given start time.
     * </p>
     * <p>
     * The remain parameters of the sampling clock (i.e., period and sample count) are taken from the
     * configuration record given at construction.
     * </p>
     * 
     * @param insStart  the start time of the new sampling clock
     * 
     * @return  a new <code>UniformSamplingClock</code> instance with the given start time
     * 
     * @throws IllegalArgumentException the sample count was negative and/or the period was non-positive
     */
    private UniformSamplingClock    nextUniformClock(Instant insStart) throws IllegalArgumentException {
        
        UniformSamplingClock clk = UniformSamplingClock.from(insStart, this.cntSamples, this.durPeriod);
        
        return clk;
    }
    
    /**
     * <p>
     * Creates a new ordered list (vector) of timestamp instants with the given start time.
     * </p>
     * <p>
     * The number of timestamps and the interval between timestamps (i.e., the period) is taken from the
     * configuration record given at construction.
     * </p>
     * 
     * @param insStart  the first timestamp instant with the returned vector
     * 
     * @return  an ordered vector of <code>Instant</code> objects representing timestamps from a uniform clock
     * 
     * @throws DateTimeException    internal <code>Instant</code> addition failed
     * @throws ArithmeticException  numeric overflow occurred in <code>Instant</code> addition 
     */
    private ArrayList<Instant>   nextTimestampVector(Instant insStart) throws DateTimeException, ArithmeticException {
        
        ArrayList<Instant>   vecTms = new ArrayList<>(this.cntSamples);
        
        Instant insCurr = insStart;
        for (int iTms=0; iTms<this.cntSamples; iTms++) {
            vecTms.add(insCurr);
            
            insCurr = insCurr.plus(this.durPeriod);
        }
        
        this.insStart = insCurr;
        
        return vecTms;
    }
    
    /**
     * <p>
     * Computes and returns the next start time for the next ingestion frame.
     * </p>
     * <p>
     * If multiple ingestion frames are create in sequence this method should be used to advance the
     * timestamp start time for each consecutive frame.
     * </p>
     *  
     * @param insStart  the current start time for current ingestion frame timestamps
     * 
     * @return  the start time for the next ingestion frame timestamps
     * 
     * @throws DateTimeException    internal <code>Instant</code> addition failed
     * @throws ArithmeticException  numeric overflow occurred in <code>Instant</code> addition 
     */
    private Instant nextStartInstant(Instant insStart) throws DateTimeException, ArithmeticException {
        
        Duration    durRange = this.durPeriod.multipliedBy(this.cntSamples);
        Instant     insNext = insStart.plus(durRange);
        
        return insNext;
    }
    
    /**
     * <p>
     * Creates and returns a new ingestion frame label.
     * </p>
     * The ingestion frame label is created by concatenating the class name <code>{@link #STR_SRC_NAME}</code>
     * with the current frame index <code>{@link #indFrame}</code> value.  The frame index is then
     * incremented for the next frame label.
     * </p>
     * 
     * @return  the next label for an ingestion frame
     */
    private String  nextFrameLabel() {
        String  strLabel = STR_SRC_NAME + "-" + Integer.toString(this.indFrame);
        
        this.indFrame++;
        
        return strLabel;
    }

}
