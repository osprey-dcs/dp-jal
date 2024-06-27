/*
 * Project: dp-api-common
 * File:	IngestionFrameTest.java
 * Package: com.ospreydcs.dp.api.ingest.model
 * Type: 	IngestionFrameTest
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
 * @since Apr 5, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.model.DpSupportedType;
import com.ospreydcs.dp.api.model.IDataColumn;
import com.ospreydcs.dp.api.model.UniformSamplingClock;
import com.ospreydcs.dp.api.model.table.StaticDataColumn;
import com.ospreydcs.dp.api.util.JavaSize;


/**
 * <p>
 * JUnit test cases for class <code>IngestionFrame</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 5, 2024
 *
 * @see IngestionFrame
 */
public class IngestionFrameTest {
    
    
    //
    // Test Constants
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
    // Text Fixture
    //

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    
    //
    // Test Cases
    //
    
    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.model.IngestionFrame#from(ArrayList, ArrayList}.
     */
    @Test
    public final void testFrom() {
        
        // Parameters
        final int       cntCols = 100;
        final int       cntRows = 100;
        final double    dblSeed = 0.0;
        final double    dblIncr = 0.001;
        
        // Create the timestamps
        ArrayList<Instant>  vecTms = this.createTimestampList(INS_START_DBLS, DUR_PERIOD_DBLS, cntRows);
        
        // Create the time-series data
        ArrayList<IDataColumn<Object>>  vecColData = new ArrayList<>(cntCols);
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            String          strName = STR_PV_PREFIX_DBLS + Integer.toString(iCol);
            Double          dblColSeed = dblSeed + iCol;
            
            IDataColumn<Object> col = this.createDoubleColumn(strName, cntRows, dblColSeed, dblIncr);
            vecColData.add(col);
        }
        
        // Create the ingestion frame and check it
        IngestionFrame frame = IngestionFrame.from(vecTms, vecColData);
        
        Assert.assertEquals(cntCols, frame.getColumnCount());
        Assert.assertEquals(cntRows, frame.getRowCount());
        Assert.assertTrue(frame.checkFrameConsistency().isSuccess());
        
        List<String>    lstColNms = this.createColumnNames(STR_PV_PREFIX_DBLS, cntCols);
        List<String>    lstBadNms = lstColNms.stream().filter(s -> !frame.hasDataColumn(s)).toList();
        
        if (!lstBadNms.isEmpty())
            Assert.fail("Ingestion frame missing columns " + lstBadNms);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#IngestionFrame()}.
     */
    @Test
    public final void testIngestionFrame() {
        IngestionFrame  frame = new IngestionFrame();
        
        Assert.assertFalse(frame.hasSamplingClock());
        Assert.assertFalse(frame.hasTimestampList());
        Assert.assertFalse(frame.hasData());
        Assert.assertFalse(frame.hasAttributes());
        
        try {
            frame.getDataColumn(0);
            Assert.fail("Ingestion frame getDataColumn(0) should throw exception.");
            
        } catch (IllegalStateException e) {}

        try {
            frame.getRowCount();
            Assert.fail("Ingestion frame getRowCount() should throw exception.");
            
        } catch (IllegalStateException e) {}
        
        try {
            frame.getDataColumns();
            Assert.fail("Ingestion frame getDataColumns() should throw exception.");
            
        } catch (IllegalStateException e) {}
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#IngestionFrame(com.ospreydcs.dp.api.model.UniformSamplingClock, java.util.ArrayList)}.
     */
    @Test
    public final void testIngestionFrameUniformSamplingClockArrayListOfIDataColumnOfObject() {

        // Parameters
        final int       cntCols = 10;
        final int       cntRows = 10;
        final double    dblSeed = 0.0;
        final double    dblIncr = 0.1;
        
        UniformSamplingClock    clk = UniformSamplingClock.from(INS_START_DBLS, cntRows, LNG_PERIOD_DBLS, CU_PERIOD_DBLS);
        
        ArrayList<IDataColumn<Object>> lstCols = new ArrayList<>(cntCols);
        List<String>                    lstNms = this.createColumnNames(STR_PV_PREFIX_DBLS, cntCols);
        for (int iCol=0; iCol<cntCols; iCol++) {
            String                  strName = lstNms.get(iCol);
            double                  dblColSeed = dblSeed + iCol;
            IDataColumn<Object>     col = this.createDoubleColumn(strName, cntRows, dblColSeed, dblIncr);

            lstCols.add(col);
        }
        
        // Create frame and check it
        IngestionFrame  frame = IngestionFrame.from(clk, lstCols);
        
        Assert.assertEquals(cntCols, frame.getColumnCount());
        Assert.assertEquals(cntRows, frame.getRowCount());
        
        List<String>    lstBadNms = lstNms.stream().filter(s -> !frame.hasDataColumn(s)).toList();
        
        if (!lstBadNms.isEmpty())
            Assert.fail("Ingestion frame missing data columns " + lstBadNms);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#IngestionFrame(com.ospreydcs.dp.api.model.UniformSamplingClock, java.util.ArrayList, java.util.Map)}.
     */
    @Test
    public final void testIngestionFrameUniformSamplingClockArrayListOfIDataColumnOfObjectMapOfStringString() {
        
        // Parameters
        final int       cntAttrs = 5;
        final int       cntCols = 10;
        final int       cntRows = 10;
        final double    dblSeed = 0.0;
        final double    dblIncr = 0.1;
        
        // Create sampling clock
        UniformSamplingClock    clk = UniformSamplingClock.from(INS_START_DBLS, cntRows, LNG_PERIOD_DBLS, CU_PERIOD_DBLS);
        
        // Create time-series data
        ArrayList<IDataColumn<Object>> lstCols = new ArrayList<>(cntCols);
        List<String>                    lstNms = this.createColumnNames(STR_PV_PREFIX_DBLS, cntCols);
        for (int iCol=0; iCol<cntCols; iCol++) {
            String                  strName = lstNms.get(iCol);
            double                  dblColSeed = dblSeed + iCol;
            IDataColumn<Object>     col = this.createDoubleColumn(strName, cntRows, dblColSeed, dblIncr);

            lstCols.add(col);
        }
        
        // Create attributes
        Map<String, String> mapAttrs = this.createAttributes(cntAttrs);
        
        // Create frame and check it
        IngestionFrame  frame = new IngestionFrame(clk, lstCols, mapAttrs);
        
        Assert.assertEquals(cntCols, frame.getColumnCount());
        Assert.assertEquals(cntRows, frame.getRowCount());
        
        List<String>    lstBadNms = lstNms.stream().filter(s -> !frame.hasDataColumn(s)).toList();
        
        if (!lstBadNms.isEmpty())
            Assert.fail("Ingestion frame missing data columns " + lstBadNms);

        Assert.assertEquals(mapAttrs, frame.getAttributes());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#IngestionFrame(java.util.ArrayList, java.util.ArrayList)}.
     */
    @Test
    public final void testIngestionFrameArrayListOfInstantArrayListOfIDataColumnOfObject() {

        // Parameters
        final int       cntCols = 10;
        final int       cntRows = 10;
        final double    dblSeed = 0.0;
        final double    dblIncr = 0.1;
        
        // Create timestamps
        ArrayList<Instant>   lstTms = this.createTimestampList(INS_START_DBLS, DUR_PERIOD_DBLS, cntRows);
        
        // Create the time-series data
        ArrayList<IDataColumn<Object>> lstCols = new ArrayList<>(cntCols);
        List<String>                    lstNms = this.createColumnNames(STR_PV_PREFIX_DBLS, cntCols);
        for (int iCol=0; iCol<cntCols; iCol++) {
            String                  strName = lstNms.get(iCol);
            double                  dblColSeed = dblSeed + iCol;
            IDataColumn<Object>     col = this.createDoubleColumn(strName, cntRows, dblColSeed, dblIncr);

            lstCols.add(col);
        }
        
        // Create frame and check it
        IngestionFrame  frame = IngestionFrame.from(lstTms, lstCols);
        
        Assert.assertEquals(cntCols, frame.getColumnCount());
        Assert.assertEquals(cntRows, frame.getRowCount());
        
        List<String>    lstBadNms = lstNms.stream().filter(s -> !frame.hasDataColumn(s)).toList();
        
        if (!lstBadNms.isEmpty())
            Assert.fail("Ingestion frame missing data columns " + lstBadNms);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#IngestionFrame(java.util.ArrayList, java.util.ArrayList, java.util.Map)}.
     */
    @Test
    public final void testIngestionFrameArrayListOfInstantArrayListOfIDataColumnOfObjectMapOfStringString() {

        // Parameters
        final int       cntAttrs = 5;
        final int       cntCols = 10;
        final int       cntRows = 10;
        final double    dblSeed = 0.0;
        final double    dblIncr = 0.1;
        
        // Create timestamps
        ArrayList<Instant>   lstTms = this.createTimestampList(INS_START_DBLS, DUR_PERIOD_DBLS, cntRows);
        
        // Create attributes
        Map<String, String> mapAttrs = this.createAttributes(cntAttrs);

        // Create the time-series data
        ArrayList<IDataColumn<Object>> lstCols = new ArrayList<>(cntCols);
        List<String>                    lstNms = this.createColumnNames(STR_PV_PREFIX_DBLS, cntCols);
        for (int iCol=0; iCol<cntCols; iCol++) {
            String                  strName = lstNms.get(iCol);
            double                  dblColSeed = dblSeed + iCol;
            IDataColumn<Object>     col = this.createDoubleColumn(strName, cntRows, dblColSeed, dblIncr);

            lstCols.add(col);
        }
        
        // Create frame and check it
        IngestionFrame  frame = new IngestionFrame(lstTms, lstCols, mapAttrs);
        
        Assert.assertEquals(cntCols, frame.getColumnCount());
        Assert.assertEquals(cntRows, frame.getRowCount());
        
        List<String>    lstBadNms = lstNms.stream().filter(s -> !frame.hasDataColumn(s)).toList();
        
        if (!lstBadNms.isEmpty())
            Assert.fail("Ingestion frame missing data columns " + lstBadNms);
        
        Assert.assertEquals(mapAttrs, frame.getAttributes());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#assignSamplingClock(com.ospreydcs.dp.api.model.UniformSamplingClock)}.
     */
    @Test
    public final void testAssignSamplingClock() {

        final int   cntRows = 100;
        
        // Create sampling clock
        UniformSamplingClock    clk = UniformSamplingClock.from(INS_START_DBLS, cntRows, LNG_PERIOD_DBLS, CU_PERIOD_DBLS);
        
        // Create uninitialized frame, assign clock, and check
        IngestionFrame  frame = new IngestionFrame();
        
        Assert.assertFalse(frame.hasSamplingClock());
        frame.assignSamplingClock(clk);
        Assert.assertTrue(frame.hasSamplingClock());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#assignTimestampList(java.util.ArrayList)}.
     */
    @Test
    public final void testAssignTimestampList() {

        final int   cntRows = 100;
        
        // Create timestamps
        ArrayList<Instant>   lstTms = this.createTimestampList(INS_START_DBLS, DUR_PERIOD_DBLS, cntRows);
        
        // Create uninitialized frame, assign timestamp list, and check
        IngestionFrame  frame = new IngestionFrame();
        
        Assert.assertFalse(frame.hasTimestampList());
        frame.assignTimestampList(lstTms);
        Assert.assertTrue(frame.hasTimestampList());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#addColumn(com.ospreydcs.dp.api.model.IDataColumn)}.
     */
    @Test
    public final void testAddColumn() {
        
        // Parameters
        final int       cntCols = 10;
        final int       cntRows = 15;
        final String    strColName = "TEST_ADD_COL";
        
        IngestionFrame frame = this.createDoublesFrame(cntCols, cntRows);
        
        IDataColumn<Object> col = this.createDoubleColumn(strColName, cntRows, cntCols+1, 0.01);
        
        // Add column and test
        try {
            frame.addColumn(col);
            
            Assert.assertEquals(cntCols+1, frame.getColumnCount());
            Assert.assertEquals(cntRows, frame.getRowCount());
            Assert.assertTrue(frame.hasDataColumn(strColName));

        } catch (IllegalStateException | IllegalArgumentException e) {
            Assert.fail("IngestionFrame#addColumn() threw exception: type=" + e.getClass().getName() + ", message=" + e.getMessage());
        }
        
        // Attempt to add column again
        try {
            frame.addColumn(col);

            Assert.fail("IngestionFrame#addColumn() - Added same column twice and no exception");
            
        } catch (IllegalStateException | IllegalArgumentException e) {}
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#addColumns(java.util.Collection)}.
     */
    @Test
    public final void testAddColumns() {

        // Parameters
        final int       cntCols = 10;
        final int       cntRows = 15;
        final double    dblSeed = 0.0;
        final double    dblIncr = 0.01;
        final String    strPrefix = "TEST_ADD_COL_";
        
        // Create test frame
        IngestionFrame  frame = this.createDoublesFrame(cntCols, cntRows);
        
        // Create additional data columns
        List<IDataColumn<Object>>   lstCols = new LinkedList<>();
        List<String>                lstColNmsNew = new LinkedList<>();
        for (int iCol=0; iCol<cntCols; iCol++) {
            String      strName = strPrefix + Integer.toString(iCol);
            double      dblColSeed = dblSeed + iCol;
            
            IDataColumn<Object> col = this.createDoubleColumn(strName, cntRows, dblColSeed, dblIncr);
            
            lstCols.add(col);
            lstColNmsNew.add(col.getName());
        }
        
        // Attempt to add columns and check
        try {
            frame.addColumns(lstCols);
            
            Assert.assertEquals(2*cntCols, frame.getColumnCount());
            Assert.assertEquals(cntRows, frame.getRowCount());
            Assert.assertTrue(lstColNmsNew.stream().allMatch(s -> frame.hasDataColumn(s)));
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            Assert.fail("IngestionFrame#addColumns() threw exception: type=" + e.getClass().getName() + ", message=" + e.getMessage());
        }
        
        // Attempt to add columns again
        try {
            frame.addColumns(lstCols);

            Assert.fail("IngestionFrame#addColumns() - Added same columns twice with no exception");
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("IngestionFrame#addColumns() correctly threw exception: type=" + e.getClass().getName() + ", message=" + e.getMessage());

        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#setFrameLabel(java.lang.String)}.
     */
    @Test
    public final void testSetFrameLabel() {

        // Parameters
        final String    strLabel = "TEST_LABEL";
        
        IngestionFrame  frame = new IngestionFrame();
        
        frame.setFrameLabel(strLabel);
        Assert.assertEquals(strLabel, frame.getFrameLabel());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#setFrameTimestamp(java.time.Instant)}.
     */
    @Test
    public final void testSetFrameTimestamp() {
        
        // Parameters
        final Instant   insTms = Instant.now();
        
        IngestionFrame  frame = new IngestionFrame();
        
        frame.setFrameTimestamp(insTms);
        Assert.assertEquals(insTms, frame.getFrameTimestamp());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#addAttribute(java.lang.String, java.lang.String)}.
     */
    @Test
    public final void testAddAttribute() {
        
        // Parameters
        final int                   cntAttrs = 20;
        final Map<String, String>   mapAttrs = this.createAttributes(cntAttrs);
        
        IngestionFrame  frame = new IngestionFrame();
        
        for (Map.Entry<String, String> entry : mapAttrs.entrySet()) {
            frame.addAttribute(entry.getKey(), entry.getValue());
        }
        
        Assert.assertEquals(mapAttrs, frame.getAttributes());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#addAttributes(java.util.Map)}.
     */
    @Test
    public final void testAddAttributes() {

        // Parameters
        final int                   cntAttrs = 20;
        final Map<String, String>   mapAttrs = this.createAttributes(cntAttrs);
        
        IngestionFrame  frame = new IngestionFrame();

        frame.addAttributes(mapAttrs);
        Assert.assertEquals(mapAttrs, frame.getAttributes());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#checkFrameConsistency()}.
     */
    @Test
    public final void testCheckFrameConsistency() {

        // Parameters
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        
        // Create test frame
        IngestionFrame  frame = this.createDoublesFrame(cntCols, cntRows);
        
        Assert.assertTrue(frame.checkFrameConsistency().isSuccess());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#allocationSizeFrame()}.
     */
    @Test
    public final void testAllocationSize() {

        // Parameters
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        
        final int       intSzMem = (int) (cntCols * cntRows * JavaSize.SZ_Double);
        
        // Create test frame
        IngestionFrame  frame = this.createDoublesFrame(cntCols, cntRows);

        long    lngSzSer = frame.allocationSizeFrame();
        
        Assert.assertTrue(intSzMem > lngSzSer);
        
        System.out.println("Allocation size for " + cntCols + "x" + cntRows + " ingestion frame:\n"
                + "  memory = " + intSzMem + " bytes\n"
                + "  serial = " + lngSzSer + " bytes");
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#removeRowsAtHead(int)}.
     */
    @Test
    public final void testRemoveHead() {

        // Parameters
        final String    strLabel = "TEST_SOURCE_FRAME";
        final Instant   insTms = Instant.now();
        final int       cntAttrs = 10;
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        final int       cntFrames = 4;
        
        final int       cntRowsPerFrame = cntRows/cntFrames;
        
        // Create source frame
        IngestionFrame      frmSource = this.createDoublesFrame(cntCols, cntRows);
        
        Map<String, String> mapAttrs = this.createAttributes(cntAttrs);
        frmSource.setFrameLabel(strLabel);
        frmSource.setFrameTimestamp(insTms);
        frmSource.addAttributes(mapAttrs);
        
        // Bin the source frame
        List<IngestionFrame>    lstFrmBin = new LinkedList<>();
        for (int iFrame=0; iFrame<cntFrames; iFrame++) {
            
            try {
                IngestionFrame  frmBin = frmSource.removeRowsAtHead(cntRowsPerFrame); // method under test
                
                lstFrmBin.add(frmBin);
                
            } catch (IllegalArgumentException | IllegalStateException e) {
            
                Assert.fail("IngestionFrame#removeHead() threw exception: type=" + e.getClass().getName() + ", message=" + e.getMessage());
            }
        }
        
        // Check source frame - should be empty
        if (frmSource.hasData())
            Assert.fail("Source frame still has data.");
        
        // Check all binned frames
        IngestionFrame  frmPrev = null;
        for (IngestionFrame frmCurr : lstFrmBin) {
            Assert.assertEquals(cntCols, frmCurr.getColumnCount());
            Assert.assertEquals(cntRowsPerFrame, frmCurr.getRowCount());
            Assert.assertEquals(frmSource.getFrameLabel(), frmCurr.getFrameLabel());
            Assert.assertEquals(frmSource.getFrameTimestamp(), frmCurr.getFrameTimestamp());
            Assert.assertEquals(frmSource.getAttributes(), frmCurr.getAttributes());

            if (frmPrev != null)
                Assert.assertEquals(frmPrev.getColumnNames(), frmCurr.getColumnNames());
            
            if (frmPrev == null)
                frmPrev = frmCurr;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#removeRowsAtTail(int)}.
     */
    @Test
    public final void testRemoveTail() {

        // Parameters
        final String    strLabel = "TEST_SOURCE_FRAME";
        final Instant   insTms = Instant.now();
        final int       cntAttrs = 10;
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        final int       cntFrames = 4;
        
        final int       cntRowsPerFrame = cntRows/cntFrames;
        
        // Create source frame
        IngestionFrame      frmSource = this.createDoublesFrame(cntCols, cntRows);
        
        Map<String, String> mapAttrs = this.createAttributes(cntAttrs);
        frmSource.setFrameLabel(strLabel);
        frmSource.setFrameTimestamp(insTms);
        frmSource.addAttributes(mapAttrs);
        
        // Bin the source frame
        List<IngestionFrame>    lstFrmBin = new LinkedList<>();
        for (int iFrame=0; iFrame<cntFrames; iFrame++) {
            
            try {
                IngestionFrame  frmBin = frmSource.removeRowsAtTail(cntRowsPerFrame);  // method under test
                
                lstFrmBin.add(frmBin);
                
            } catch (IllegalArgumentException | IllegalStateException e) {
            
                Assert.fail("IngestionFrame#removeTail() threw exception: type=" + e.getClass().getName() + ", message=" + e.getMessage());
            }
        }
        
        // Check source frame - should be empty
        if (frmSource.hasData())
            Assert.fail("Source frame still has data.");
        
        // Check all binned frames
        IngestionFrame  frmPrev = null;
        for (IngestionFrame frmCurr : lstFrmBin) {
            Assert.assertEquals(cntCols, frmCurr.getColumnCount());
            Assert.assertEquals(cntRowsPerFrame, frmCurr.getRowCount());
            Assert.assertEquals(frmSource.getFrameLabel(), frmCurr.getFrameLabel());
            Assert.assertEquals(frmSource.getFrameTimestamp(), frmCurr.getFrameTimestamp());
            Assert.assertEquals(frmSource.getAttributes(), frmCurr.getAttributes());

            if (frmPrev != null)
                Assert.assertEquals(frmPrev.getColumnNames(), frmCurr.getColumnNames());
            
            if (frmPrev == null)
                frmPrev = frmCurr;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#removeColumnsByName(Collection)}.
     */
    @Test
    public final void testRemoveColumnsByName() {

        // Parameters
        final String    strLabel = "TEST_SOURCE_FRAME";
        final Instant   insTms = Instant.now();
        final int       cntAttrs = 10;
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        final int       cntFrames = 4;
        
        final int       cntColsPerFrame = cntCols/cntFrames;
        
        // Create source frame
        IngestionFrame      frmSource = this.createDoublesFrame(cntCols, cntRows);
        
        Map<String, String> mapAttrs = this.createAttributes(cntAttrs);
        frmSource.setFrameLabel(strLabel);
        frmSource.setFrameTimestamp(insTms);
        frmSource.addAttributes(mapAttrs);
        
        // Bin the source frame
        Integer                 indStart = 0;
        Integer                 indStop = cntColsPerFrame;
        ArrayList<String>       vecNmsSource = new ArrayList<>(frmSource.getColumnNames());
        List<IngestionFrame>    lstFrmBin = new LinkedList<>();
        for (int iFrame=0; iFrame<cntFrames; iFrame++) {
            
            List<String>    lstNmsBin = vecNmsSource.subList(indStart, indStop);
            indStart += cntColsPerFrame;
            indStop += cntColsPerFrame;
            
            try {
                IngestionFrame  frmBin = frmSource.removeColumnsByName(lstNmsBin);  // method under test
                
                lstFrmBin.add(frmBin);
                
            } catch (IllegalArgumentException | IllegalStateException e) {
            
                Assert.fail("IngestionFrame#removeColumnsByName() threw exception: type=" + e.getClass().getName() + ", message=" + e.getMessage());
            }
        }
        
        // Check source frame - should be empty
        if (frmSource.hasData())
            Assert.fail("Source frame still has data.");
        
        // Check all binned frames
        IngestionFrame  frmPrev = null;
        for (IngestionFrame frmCurr : lstFrmBin) {
            Assert.assertEquals(cntColsPerFrame, frmCurr.getColumnCount());
            Assert.assertEquals(cntRows, frmCurr.getRowCount());
            Assert.assertEquals(frmSource.getFrameLabel(), frmCurr.getFrameLabel());
            Assert.assertEquals(frmSource.getFrameTimestamp(), frmCurr.getFrameTimestamp());
            Assert.assertEquals(frmSource.getAttributes(), frmCurr.getAttributes());

            if (frmPrev != null) {
                Set<String>     setPrevNms = frmPrev.getColumnNames();
                
                for (String strNmPrev : setPrevNms)
                    Assert.assertFalse( frmCurr.getColumnNames().contains(strNmPrev) );
            }
            
            if (frmPrev == null)
                frmPrev = frmCurr;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#removeColumnsByIndex(int)}.
     */
    @Test
    public final void testRemoveColumnsByIndexFromInt() {

        // Parameters
        final String    strLabel = "TEST_SOURCE_FRAME";
        final Instant   insTms = Instant.now();
        final int       cntAttrs = 10;
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        final int       cntFrames = 4;
        
        final int       cntColsPerFrame = cntCols/cntFrames;
        
        // Create source frame
        IngestionFrame      frmSource = this.createDoublesFrame(cntCols, cntRows);
        
        Map<String, String> mapAttrs = this.createAttributes(cntAttrs);
        frmSource.setFrameLabel(strLabel);
        frmSource.setFrameTimestamp(insTms);
        frmSource.addAttributes(mapAttrs);
        
        // Bin the source frame
        List<IngestionFrame>    lstFrmBin = new LinkedList<>();
        for (int iFrame=0; iFrame<cntFrames; iFrame++) {
            
            try {
                IngestionFrame  frmBin = frmSource.removeColumnsByIndex(cntColsPerFrame);  // method under test
                
                lstFrmBin.add(frmBin);
                
            } catch (IllegalArgumentException | IllegalStateException | IndexOutOfBoundsException e) {
            
                Assert.fail("IngestionFrame#removeHead() threw exception: type=" + e.getClass().getName() + ", message=" + e.getMessage());
            }
        }
        
        // Check source frame - should be empty
        if (frmSource.hasData())
            Assert.fail("Source frame still has data.");
        
        // Check all binned frames
        IngestionFrame  frmPrev = null;
        for (IngestionFrame frmCurr : lstFrmBin) {
            Assert.assertEquals(cntColsPerFrame, frmCurr.getColumnCount());
            Assert.assertEquals(cntRows, frmCurr.getRowCount());
            Assert.assertEquals(frmSource.getFrameLabel(), frmCurr.getFrameLabel());
            Assert.assertEquals(frmSource.getFrameTimestamp(), frmCurr.getFrameTimestamp());
            Assert.assertEquals(frmSource.getAttributes(), frmCurr.getAttributes());

            if (frmPrev != null) {
                Set<String>     setPrevNms = frmPrev.getColumnNames();
                
                for (String strNmPrev : setPrevNms)
                    Assert.assertFalse( frmCurr.getColumnNames().contains(strNmPrev) );
            }
            
            if (frmPrev == null)
                frmPrev = frmCurr;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#removeColumnsByIndex(Collection)}.
     */
    @Test
    public final void testRemoveColumnsByIndexFromCollection() {

        // Parameters
        final String    strLabel = "TEST_SOURCE_FRAME";
        final Instant   insTms = Instant.now();
        final int       cntAttrs = 10;
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        final int       cntFrames = 4;
        
        final int       cntColsPerFrame = cntCols/cntFrames;
        
        // Create source frame
        IngestionFrame      frmSource = this.createDoublesFrame(cntCols, cntRows);
        
        Map<String, String> mapAttrs = this.createAttributes(cntAttrs);
        frmSource.setFrameLabel(strLabel);
        frmSource.setFrameTimestamp(insTms);
        frmSource.addAttributes(mapAttrs);
        
        // Bin the source frame
        List<Integer>           lstIndsBin = IntStream.range(0, cntColsPerFrame).boxed().toList();
        List<IngestionFrame>    lstFrmBin = new LinkedList<>();
        for (int iFrame=0; iFrame<cntFrames; iFrame++) {
            
            try {
                IngestionFrame  frmBin = frmSource.removeColumnsByIndex(lstIndsBin);  // method under test
                
                lstFrmBin.add(frmBin);
                
            } catch (IllegalArgumentException | IllegalStateException | IndexOutOfBoundsException e) {
            
                Assert.fail("IngestionFrame#removeHead() threw exception: type=" + e.getClass().getName() + ", message=" + e.getMessage());
            }
        }
        
        // Check source frame - should be empty
        if (frmSource.hasData())
            Assert.fail("Source frame still has data.");
        
        // Check all binned frames
        IngestionFrame  frmPrev = null;
        for (IngestionFrame frmCurr : lstFrmBin) {
            Assert.assertEquals(cntColsPerFrame, frmCurr.getColumnCount());
            Assert.assertEquals(cntRows, frmCurr.getRowCount());
            Assert.assertEquals(frmSource.getFrameLabel(), frmCurr.getFrameLabel());
            Assert.assertEquals(frmSource.getFrameTimestamp(), frmCurr.getFrameTimestamp());
            Assert.assertEquals(frmSource.getAttributes(), frmCurr.getAttributes());

            if (frmPrev != null) {
                Set<String>     setPrevNms = frmPrev.getColumnNames();
                
                for (String strNmPrev : setPrevNms)
                    Assert.assertFalse( frmCurr.getColumnNames().contains(strNmPrev) );
            }
            
            if (frmPrev == null)
                frmPrev = frmCurr;
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#copy()}.
     */
    @Test
    public final void testCopy() {

        // Parameters
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        
        // Create source frame
        IngestionFrame      frmSource = this.createDoublesFrame(cntCols, cntRows);

        // Create the copy and compare
        IngestionFrame      frmCopy = frmSource.copy();
        
        Assert.assertEquals(frmSource, frmCopy);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#hasSamplingClock()}.
     */
    @Test
    public final void testHasSamplingClock() {
        
        UniformSamplingClock    clk = UniformSamplingClock.from(INS_START_DBLS, 100, LNG_PERIOD_DBLS, CU_PERIOD_DBLS);
        
        IngestionFrame  frame = IngestionFrame.newFrame();
        frame.assignSamplingClock(clk);
        
        Assert.assertTrue(frame.hasSamplingClock());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#hasTimestampList()}.
     */
    @Test
    public final void testHasTimestampList() {

        ArrayList<Instant>  vecTms = this.createTimestampList(INS_START_DBLS, DUR_PERIOD_DBLS, 100);

        IngestionFrame      frame = IngestionFrame.newFrame();
        frame.assignTimestampList(vecTms);
        
        Assert.assertTrue(frame.hasTimestampList());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#hasData()}.
     */
    @Test
    public final void testHasData() {

        // Parameters
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        
        // Create test frame
        IngestionFrame      frame = this.createDoublesFrame(cntCols, cntRows);

        Assert.assertTrue(frame.hasData());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#hasAttributes()}.
     */
    @Test
    public final void testHasAttributes() {

        // Parameters
        final int       cntAttrs = 10;
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        final Map<String, String> mapAttrs = this.createAttributes(cntAttrs);
        
        // Create test frame
        IngestionFrame      frame = this.createDoublesFrame(cntCols, cntRows);
        frame.addAttributes(mapAttrs);
        
        Assert.assertTrue(frame.hasAttributes());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#getFrameLabel()}.
     */
    @Test
    public final void testGetFrameLabel() {
        
        final String    strLabel = "TEST_FRAME_LABEL";
        
        IngestionFrame  frame = IngestionFrame.newFrame();
        frame.setFrameLabel(strLabel);
        
        Assert.assertEquals(strLabel, frame.getFrameLabel());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#getFrameTimestamp()}.
     */
    @Test
    public final void testGetFrameTimestamp() {
        
        final Instant   insTms = Instant.now();
        
        IngestionFrame  frame = IngestionFrame.newFrame();
        frame.setFrameTimestamp(insTms);
        
        Assert.assertEquals(insTms, frame.getFrameTimestamp());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#getAttributes()}.
     */
    @Test
    public final void testGetAttributes() {

        // Parameters
        final int       cntAttrs = 10;
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        final Map<String, String> mapAttrs = this.createAttributes(cntAttrs);
        
        // Create test frame
        IngestionFrame      frame = this.createDoublesFrame(cntCols, cntRows);
        frame.addAttributes(mapAttrs);
        
        Assert.assertEquals(mapAttrs, frame.getAttributes());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#getSamplingClock()}.
     */
    @Test
    public final void testGetSamplingClock() {
        
        UniformSamplingClock    clk = UniformSamplingClock.from(INS_START_DBLS, 100, LNG_PERIOD_DBLS, CU_PERIOD_DBLS);
        
        IngestionFrame  frame = IngestionFrame.newFrame();
        frame.assignSamplingClock(clk);
        
        Assert.assertEquals(clk, frame.getSamplingClock());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#getTimestampList()}.
     */
    @Test
    public final void testGetTimestampList() {

        ArrayList<Instant>  vecTms = this.createTimestampList(INS_START_DBLS, DUR_PERIOD_DBLS, 100);

        IngestionFrame      frame = IngestionFrame.newFrame();
        frame.assignTimestampList(vecTms);
        
        Assert.assertEquals(vecTms, frame.getTimestampList());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#getColumnNames()}.
     */
    @Test
    public final void testGetColumnNames() {
        
        // Parameters
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        
        // Create test frame
        IngestionFrame      frame = this.createDoublesFrame(cntCols, cntRows);
        
        // Create column names and compare
        Set<String>     setNames = new TreeSet<>();
        for (int iCol=0; iCol<cntCols; iCol++) {
            String      strName = STR_PV_PREFIX_DBLS + Integer.toString(iCol);
            
            setNames.add(strName);
        }
        
        Assert.assertEquals(setNames, frame.getColumnNames());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#getColumnCount()}.
     */
    @Test
    public final void testGetColumnCount() {

        // Parameters
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        
        // Create test frame
        IngestionFrame      frame = this.createDoublesFrame(cntCols, cntRows);
        
        Assert.assertEquals(cntCols, frame.getColumnCount());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#getRowCount()}.
     */
    @Test
    public final void testGetRowCount() {

        // Parameters
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        
        // Create test frame
        IngestionFrame      frame = this.createDoublesFrame(cntCols, cntRows);
        
        Assert.assertEquals(cntRows, frame.getRowCount());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#getDataColumn(int)}.
     */
    @Test
    public final void testGetDataColumnInt() {

        // Parameters
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        
        // Create test frame
        IngestionFrame      frame = this.createDoublesFrame(cntCols, cntRows);
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            String  strColNm = STR_PV_PREFIX_DBLS + Integer.toString(iCol);
            
            IDataColumn<Object> col = frame.getDataColumn(iCol);
            
            Assert.assertEquals(cntRows, col.getSize().intValue());
            Assert.assertEquals(strColNm, col.getName());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#getDataColumn(java.lang.String)}.
     */
    @Test
    public final void testGetDataColumnString() {

        // Parameters
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        
        // Create test frame
        IngestionFrame      frame = this.createDoublesFrame(cntCols, cntRows);
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            String  strColNm = STR_PV_PREFIX_DBLS + Integer.toString(iCol);
            
            IDataColumn<Object> col = frame.getDataColumn(strColNm);
            
            Assert.assertEquals(cntRows, col.getSize().intValue());
            Assert.assertEquals(strColNm, col.getName());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.ingest.IngestionFrame#getDataColumns()}.
     */
    @Test
    public final void testGetDataColumns() {

        // Parameters
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        
        // Create test frame
        IngestionFrame      frame = this.createDoublesFrame(cntCols, cntRows);

        // Get all data columns and check
        List<IDataColumn<Object>>   lstCols = frame.getDataColumns();
        
        Set<String>    setColNms = new TreeSet<>( frame.getColumnNames() );
        
        for (IDataColumn<Object> col : lstCols) {
            Assert.assertEquals(cntRows, col.getSize().intValue());
            
            setColNms.remove(col.getName());
        }
        
        Assert.assertTrue("IngestionFrame#getColumnNames() did not return the column: " + setColNms, setColNms.isEmpty());
    }

    /**
     * Test method for {@link java.lang.Object#equals(java.lang.Object)}.
     */
    @Test
    public final void testEquals() {

        // Parameters
        final String    strLabel = "TEST_SOURCE_FRAME";
        final Instant   insTms = Instant.now();
        final int       cntAttrs = 10;
        final int       cntCols = 1000;
        final int       cntRows = 1000;
        final Map<String, String> mapAttrs = this.createAttributes(cntAttrs);
        
        // Create test frame
        IngestionFrame      frmSource = this.createDoublesFrame(cntCols, cntRows);
        frmSource.setFrameLabel(strLabel);
        frmSource.setFrameTimestamp(insTms);
        frmSource.addAttributes(mapAttrs);
        
        // Create copy and compare
        IngestionFrame      frmCopy = frmSource.copy();
        
        Assert.assertTrue(frmCopy.equals(frmSource));
    }

    /**
     * Test method for {@link java.lang.Object#toString()}.
     */
    @Test
    public final void testToString() {

        // Parameters
        final String    strLabel = "TEST_SOURCE_FRAME";
        final Instant   insTms = Instant.now();
        final int       cntAttrs = 3;
        final int       cntCols = 5;
        final int       cntRows = 5;
        final Map<String, String> mapAttrs = this.createAttributes(cntAttrs);
        
        // Create test frame
        IngestionFrame      frame = this.createDoublesFrame(cntCols, cntRows);
        frame.setFrameLabel(strLabel);
        frame.setFrameTimestamp(insTms);
        frame.addAttributes(mapAttrs);
        
        System.out.println(frame.toString());
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Create an ingestion frame with the given dimensions populated with simulated data.
     * </p>
     * 
     * @param cntCols   width of the ingestion frame (number of columns)
     * @param cntRows   length of the ingestion frame (number of rows)
     * 
     * @return  a new ingestion frame populated with artificial data
     */
    private IngestionFrame  createDoublesFrame(int cntCols, int cntRows) {

        // Parameters
        final double    dblColSeed = 0.0;
        final double    dblRowIncr = 0.001;
        
        // The Returned object
        ArrayList<IDataColumn<Object>>  vecColData = new ArrayList<>(cntCols);
     
        // Create time-series column data
        for (int iCol=0; iCol<cntCols; iCol++) {
            String              strColNm = STR_PV_PREFIX_DBLS + Integer.toString(iCol);
            DpSupportedType     enmType = ENM_TYPE_DBLS;
//            ArrayList<Object>   lstVals = new ArrayList<>(cntRows); 
//            
            Double  dblVal = dblColSeed + iCol;
//            for (int iRow=0; iRow<cntRows; iRow++) {
//                lstVals.add(dblVal);
//                
//                dblVal += dblRowIncr;
//            }
            
            // Create data column and add to collection 
//            IDataColumn<Object> col = StaticDataColumn.from(strColNm, enmType, lstVals);
            IDataColumn<Object> col = this.createDoubleColumn(strColNm, cntRows, dblVal, dblRowIncr);
            vecColData.add(col);
        }
        
        // Create sampling clock
        UniformSamplingClock    ckl = UniformSamplingClock.from(INS_START_DBLS, cntRows, LNG_PERIOD_DBLS, CU_PERIOD_DBLS);
        
        // Create ingestion frame and return it
        IngestionFrame  frame = IngestionFrame.from(ckl, vecColData);
        
        return frame;
    }
    
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
    private IDataColumn<Object> createDoubleColumn(String strName, int cntRows, double dblSeed, double dblIncr) {
        
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
     * Creates a new timestamp list for uniform sampling according to the given parameters.
     * </p>
     * 
     * @param insStart  start time of the timestamp list (i.e., the first element)
     * @param durPeriod the period of sampling (time duration between timestamps)     
     * @param cntRows   the number of timestamps within the list (i.e., size)
     * 
     * @return  a new, ordered list of timestamps with uniform distribution
     */
    private ArrayList<Instant>  createTimestampList(Instant insStart, Duration durPeriod, int cntRows) {
        
        // The returned object
        ArrayList<Instant>  vecTms = new ArrayList<>(cntRows);
        
        // Populate the timestamp list
        Instant insRow = insStart;
        for (int iRow=0; iRow<cntRows; iRow++) {
            vecTms.add(insRow);
            
            insRow = insRow.plus(durPeriod);
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
    private List<String> createColumnNames(String strPrefix, int cntCols) {
        
        // Returned object
        ArrayList<String>   lstNames = new ArrayList<>(cntCols);
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            String      strName = strPrefix + Integer.toString(iCol);
            
            lstNames.add(strName);
        }
        
        return lstNames;
    }
    
    /**
     * <p>
     * Creates a collection of ingestion frame attributes of the given size.
     * </p>
     * 
     * @param cntAttrs  number of (name, value) attribute pairs to create
     * 
     * @return  map containing (name, value) attribute pairs as the entries
     */
    private Map<String, String> createAttributes(int cntAttrs) {
        
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
}
