/*
 * Project: dp-api-common
 * File:	TestIngestionFrameGenerator.java
 * Package: com.ospreydcs.dp.api.ingest.test
 * Type: 	TestIngestionFrameGenerator
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
 * @since Apr 13, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ospreydcs.dp.api.ingest.model.IngestionFrame;
import com.ospreydcs.dp.api.model.DpSupportedType;
import com.ospreydcs.dp.api.model.IDataColumn;
import com.ospreydcs.dp.api.model.UniformSamplingClock;
import com.ospreydcs.dp.api.model.table.StaticDataColumn;

/**
 * <p>
 * Utility class for generating <code>IngestionFrame</code> instances used in testing.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Apr 13, 2024
 *
 */
public class TestIngestionFrameGenerator {

    //
    // Class Constants
    //
    
    /** Double-Valued Frames: Prefix used for data source (PV) names */
    public static final String          STR_PV_PREFIX_DBLS = "TEST_PV_DBL_";
    
    /** Double-Valued Frames: Data Platform supported type */
    public static final DpSupportedType ENM_TYPE_DBLS = DpSupportedType.DOUBLE;

    
    /** Double-Valued Frames: Start time for sampling clock. */
    public static final Instant         INS_START_DBLS = Instant.parse("2024-04-06T00:00:00.0Z");
    
    /** Double-Valued Frames: The sampling period used to defined the sampling clock */ 
    public static final long            LNG_PERIOD_DBLS = 1L;
    
    /** Double-Valued Frames: The sampling period (time) units used to defined the sampling clock */ 
    public static final ChronoUnit      CU_PERIOD_DBLS = ChronoUnit.MILLIS;
    
    /** Double-Valued Frames: The sampling period (time) units used to defined the sampling clock */ 
    public static final Duration        DUR_PERIOD_DBLS = Duration.of(LNG_PERIOD_DBLS, CU_PERIOD_DBLS);

    
    //
    // Class Resources
    //
    
    /** The number of double-valued ingestion frames created so far */
    private static long     cntFramesDbl = 0;
    
    /** The numeric seed for each double-valued ingestion frame */
    private static double   dblFrameDblSeed = 0.0;
    
    /** The row-value increment for each double-valued column value */
    private static double   dBlRowDblIncr = DUR_PERIOD_DBLS.toMillis();
    
    
    /** The ingestion frame start time seed value */
    private static Instant  insStartTmSeed = Instant.now();
    
    
    /**
     * <p>
     * Constructs a new instance of <code>TestIngestionFrameGenerator</code>.
     * </p>
     *
     */
    public TestIngestionFrameGenerator() {
        // TODO Auto-generated constructor stub
    }


    //
    // Support Methods
    //
    
    /**
     * <p>
     * Create an ingestion frame with the given dimensions, populated with double-valued 
     * simulated data, and using a uniform sampling clock.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This method increments frame counter <code>{@link #cntFramesDbl}</code> and seed value
     * <code>{@link #dblFrameDblSeed}</code> which must be done atomically, thus, the
     * explicit synchronization.
     * </p>
     * 
     * @param cntCols   width of the ingestion frame (number of columns)
     * @param cntRows   length of the ingestion frame (number of rows)
     * 
     * @return  a new ingestion frame populated with artificial data
     */
    synchronized
    public static IngestionFrame  createDoublesFrameWithClock(int cntCols, int cntRows) {

        // The returned time-series data
        ArrayList<IDataColumn<Object>>  vecColData = new ArrayList<>(cntCols);
     
        // Create time-series data colums
        for (int iCol=0; iCol<cntCols; iCol++) {
            String              strColNm = STR_PV_PREFIX_DBLS + Integer.toString(iCol);
            Double              dblVal = TestIngestionFrameGenerator.dblFrameDblSeed + iCol;
            Double              dblRowIncr = TestIngestionFrameGenerator.dBlRowDblIncr;

            // Create data column and add to collection 
            IDataColumn<Object> col = TestIngestionFrameGenerator.createDoubleColumn(strColNm, cntRows, dblVal, dblRowIncr);
            vecColData.add(col);
        }
        
        // Create sampling clock
        UniformSamplingClock    clock = TestIngestionFrameGenerator.createSamplingClock(cntRows, DUR_PERIOD_DBLS);
        
        // Create ingestion frame 
        IngestionFrame  frame = IngestionFrame.from(clock, vecColData);
        
        // Increment the frame counter and seeds
        TestIngestionFrameGenerator.dblFrameDblSeed += TestIngestionFrameGenerator.cntFramesDbl;
        TestIngestionFrameGenerator.cntFramesDbl++;
        
        return frame;
    }

    /**
     * <p>
     * Create an ingestion frame with the given dimensions, populated with double-valued 
     * simulated data, and using a uniform sampling clock.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This method increments frame counter <code>{@link #cntFramesDbl}</code> and seed value
     * <code>{@link #dblFrameDblSeed}</code> which must be done atomically, thus, the
     * explicit synchronization.
     * </p>
     * 
     * @param cntCols   width of the ingestion frame (number of columns)
     * @param cntRows   length of the ingestion frame (number of rows)
     * 
     * @return  a new ingestion frame populated with artificial data
     */
    synchronized
    public static IngestionFrame  createDoublesFrameWithTimestampList(int cntCols, int cntRows) {

        // The returned time-series data
        ArrayList<IDataColumn<Object>>  vecColData = new ArrayList<>(cntCols);
     
        // Create time-series data colums
        for (int iCol=0; iCol<cntCols; iCol++) {
            String              strColNm = STR_PV_PREFIX_DBLS + Integer.toString(iCol);
            Double              dblVal = TestIngestionFrameGenerator.dblFrameDblSeed + iCol;
            Double              dblRowIncr = TestIngestionFrameGenerator.dBlRowDblIncr;

            // Create data column and add to collection 
            IDataColumn<Object> col = TestIngestionFrameGenerator.createDoubleColumn(strColNm, cntRows, dblVal, dblRowIncr);
            vecColData.add(col);
        }
        
        // Create sampling clock
        ArrayList<Instant>  vecTms = TestIngestionFrameGenerator.createTimestampList(cntRows, DUR_PERIOD_DBLS);
        
        // Create ingestion frame 
        IngestionFrame  frame = IngestionFrame.from(vecTms, vecColData);
        
        // Increment the frame counter and seeds
        TestIngestionFrameGenerator.dblFrameDblSeed += TestIngestionFrameGenerator.cntFramesDbl;
        TestIngestionFrameGenerator.cntFramesDbl++;
        
        return frame;
    }

    /**
     * <p>
     * Creates an arbitrary collection of ingestion frame attributes of the given size.
     * </p>
     * 
     * @param cntAttrs  number of (name, value) attribute pairs to create
     * 
     * @return  map containing (name, value) attribute pairs as the entries
     */
    public static Map<String, String> createAttributes(int cntAttrs) {
        
        // Parameters
        String      strNamePrefix = "TEST_AttrName_";
        String      strValuePrefix = "TEST_AttrValue_";
        
        // Returned object
        Map<String, String> mapAttrs = new HashMap<>();
        
        for (int iAttr=0; iAttr<cntAttrs; iAttr++) {
            String  strName = strNamePrefix + Integer.toString(iAttr);
            String  strValue = strValuePrefix + Integer.toString(iAttr);
            
            mapAttrs.put(strName, strValue);
        }
        
        return mapAttrs;
    }
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates an <code>IDataColumn<Double></code> instance of the given size populated with simulated data.
     * </p>
     * <p>
     * The time-series column values start with argument <code>dblSeed</code> and are incremented sequential 
     * with argument <code>dblIncr</code> until <code>cntRows</code> values are produced.
     * </p>
     * 
     * @param strName   name of the returned data column
     * @param cntRows   number of column rows (i.e., column size)
     * @param dblSeed   numeric "seed" for time-series values (i.e., the start value)
     * @param dblIncr   numeric increment value for all time-series values following the seed value
     * 
     * @return  new instance of <code>IDataColumn<Double></code> populated according to the arguments
     */
    private static IDataColumn<Object> createDoubleColumn(String strName, int cntRows, double dblSeed, double dblIncr) {
        
        DpSupportedType     enmType = DpSupportedType.DOUBLE;
        ArrayList<Object>   lstVals = new ArrayList<>(cntRows); 
        
        Double  dblVal = dblSeed;
        for (int iRow=0; iRow<cntRows; iRow++) {
            lstVals.add(dblVal);
            
            dblVal += dblIncr;
        }
        
        // Create data column and add to collection 
        IDataColumn<Object> col = StaticDataColumn.from(strName, enmType, lstVals);
        
        return col;
    }
    
    /**
     * <p>
     * Creates a new uniform sampling clock according to the given parameters.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This method increments the start timestamp seed <code>{@link #insStartTmSeed}</code>
     * which is done atomically by locking on that object.
     * </p>
     * 
     * @param cntSamples the number of timestamps within the list (i.e., size)
     * @param durPeriod  the period of sampling (time duration between timestamps)     
     * @return
     */
    private static UniformSamplingClock createSamplingClock(int cntSamples, Duration durPeriod) {

        // Create clock parameters
        Instant     insStart    = TestIngestionFrameGenerator.insStartTmSeed;
        long        lngPeriodNs = durPeriod.toNanos();
        ChronoUnit  cuPeriodNs  = ChronoUnit.NANOS;
        
        // Create sampling clock
        UniformSamplingClock    clock = UniformSamplingClock.from(insStart, cntSamples, lngPeriodNs, cuPeriodNs);
        
        // Increment the start time seed value
        synchronized (TestIngestionFrameGenerator.insStartTmSeed) {

            Duration    durClock = durPeriod.multipliedBy(cntSamples);
            TestIngestionFrameGenerator.insStartTmSeed = TestIngestionFrameGenerator.insStartTmSeed.plus(durClock);
        }

        return clock;
    }
    
    /**
     * <p>
     * Creates a new timestamp list for uniform sampling according to the given parameters.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This method increments the start timestamp seed <code>{@link #insStartTmSeed}</code>
     * which is done atomically by locking on that object.
     * </p>
     * 
     * @param cntSamples the number of timestamps within the list (i.e., size)
     * @param durPeriod  the period of sampling (time duration between timestamps)     
     * 
     * @return  a new, ordered list of timestamps with uniform distribution
     */
    private static ArrayList<Instant>  createTimestampList(int cntSamples, Duration durPeriod) {
        
        // Create timestamp list parameters
        Instant     insStart    = TestIngestionFrameGenerator.insStartTmSeed;
        
        // The returned object
        ArrayList<Instant>  vecTms = new ArrayList<>(cntSamples);
        
        // Populate the timestamp list
        Instant insRow = insStart;
        for (int iRow=0; iRow<cntSamples; iRow++) {
            vecTms.add(insRow);
            
            insRow = insRow.plus(durPeriod);
        }
        
        // Increment the start time seed value
        synchronized (TestIngestionFrameGenerator.insStartTmSeed) {
            Duration    durClock = durPeriod.multipliedBy(cntSamples);
            TestIngestionFrameGenerator.insStartTmSeed = TestIngestionFrameGenerator.insStartTmSeed.plus(durClock);
        }
        
        return vecTms;
    }
    
    /**
     * <p>
     * Create and return an ordered list of data column names of the given size with the given name prefix.
     * </p>
     * <p>
     * Names are create by simply by appending the index value (as a string) to the given prefix.  Names
     * are ordered by index.
     * </p>
     * 
     * @param strPrefix prefix given to all returned names
     * @param cntCols   number of column names to create (i.e., size of returned list)
     * 
     * @return  ordered list of names
     */
    private static List<String> createColumnNames(String strPrefix, int cntCols) {
        
        // Returned object
        ArrayList<String>   lstNames = new ArrayList<>(cntCols);
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            String      strName = strPrefix + Integer.toString(iCol);
            
            lstNames.add(strName);
        }
        
        return lstNames;
    }
    
}
