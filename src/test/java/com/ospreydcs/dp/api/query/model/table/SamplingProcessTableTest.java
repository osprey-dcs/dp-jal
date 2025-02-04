/*
 * Project: dp-api-common
 * File:	SamplingProcessTableTest.java
 * Package: com.ospreydcs.dp.api.query.model.table
 * Type: 	SamplingProcessTableTest
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
 * @since Feb 29, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.table;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.SortedSet;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import javax.naming.CannotProceedException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.ranges.RangeException;

import com.ospreydcs.dp.api.common.DpSupportedType;
import com.ospreydcs.dp.api.common.IDataColumn;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.model.correl.CorrelatedQueryData;
import com.ospreydcs.dp.api.query.model.correl.QueryDataCorrelator;
import com.ospreydcs.dp.api.query.model.series.SamplingProcess;
import com.ospreydcs.dp.api.query.model.table.SamplingProcessTable;
import com.ospreydcs.dp.api.query.test.TestQueryRecord;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.query.test.TestQueryResponses.SingleQueryType;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

/**
 * JUnit test cases for class <code>{@link SamplingProcessTable}</code>.
 *
 * @author Christopher K. Allen
 * @since Feb 29, 2024
 *
 */
public class SamplingProcessTableTest {

    
    //
    // Class Constants
    //
    
    /** General Sampling rate used for row comparisons (samples per second) */
    public static final int             INT_SAMPLING_RATE = 1000;
    
    
    /** Data Platform API Query Service test request/response - 1 data source, 10 seconds */
    public static final TestQueryRecord REC_ONE = TestQueryResponses.QREC_1SRC;
    
    /** Data Platform API Query Service test request/response - 2 data sources, 2 seconds */
    public static final TestQueryRecord REC_TWO = TestQueryResponses.QREC_2SRC;
    
    /** Data Platform API Query Service test request/response - 5 data source, 60 seconds */
    public static final TestQueryRecord REC_LONG = TestQueryResponses.QREC_LONG;
    
    /** Data Platform API Query Service test request/response - 5 data source, 60 seconds */
    public static final TestQueryRecord REC_WIDE = TestQueryResponses.QREC_WIDE;
    
    /** Data Platform API Query Service test request/response - 100 data source, 60 seconds */
    public static final TestQueryRecord REC_BIG = TestQueryResponses.QREC_BIG;

    
    
    /** Sample query response for test cases */
    public static final List<QueryDataResponse>   LST_QUERY_RSP_WIDE = TestQueryResponses.queryResults(SingleQueryType.WIDE);
    
    /** Sample query response for test cases */
    public static final List<QueryDataResponse>   LST_QUERY_RSP_LONG = TestQueryResponses.queryResults(SingleQueryType.LONG);
    
    
    /** Sample query data for test cases - 1 source, 10 seconds */
    public static final List<QueryData>   LST_QUERY_DATA_ONE = TestQueryResponses.queryData(SingleQueryType.ONE_SOURCE);
    
    /** Sample query data for test cases - 2 sources, 2 seconds */
    public static final List<QueryData>   LST_QUERY_DATA_TWO = TestQueryResponses.queryData(SingleQueryType.TWO_SOURCE);
    
    /** Sample query data for test cases - 100 sources, 5 seconds */
    public static final List<QueryData>   LST_QUERY_DATA_WIDE = TestQueryResponses.queryData(SingleQueryType.WIDE);
    
    /** Sample query data for test cases - 5 sources, 60 seconds */
    public static final List<QueryData>   LST_QUERY_DATA_LONG = TestQueryResponses.queryData(SingleQueryType.LONG);
    
    
    //
    // Test Case Resources
    //
    
    /** The query results set correlator - we only need one */
    private final static QueryDataCorrelator  CORRELATOR = new QueryDataCorrelator();
    
    
    //
    // Test Fixture
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
        TestQueryResponses.shutdown();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        CORRELATOR.reset();
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#from(java.util.SortedSet)}.
     */
    @Test
    public final void testFromSortedSet() {
        
        List<QueryDataResponse> lstRspMsgs = LST_QUERY_RSP_WIDE;
        
        try {
            for (QueryDataResponse msgRsp : lstRspMsgs)
                CORRELATOR.addQueryResponse(msgRsp);

        } catch (CompletionException | ExecutionException | CannotProceedException | IllegalArgumentException e) {
            Assert.fail(failMessage("QueryDataCorrelator#insertQueryResponse()", e));
            
        }
        
        // Get the processed data and try to create SamplingProcess
        SortedSet<CorrelatedQueryData>  setPrcdData = CORRELATOR.getCorrelatedSet();
        
        try {
            SamplingProcessTable table = SamplingProcessTable.from(setPrcdData);
            
            assertTrue("Results set contained no data sources.", table.getColumnCount() > 0);
            
        } catch (MissingResourceException | IllegalArgumentException | IllegalStateException | 
                 RangeException | UnsupportedOperationException | CompletionException e) {
            
            Assert.fail(failMessage("SamplingProcessTable#from(SortedSet)", e));
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#from(SamplingProcess}.
     */
    @Test
    public final void testFromSamplingProcess() {
        
        List<QueryDataResponse> lstRspMsgs = LST_QUERY_RSP_WIDE;
        
        try {
            for (QueryDataResponse msgRsp : lstRspMsgs)
                CORRELATOR.addQueryResponse(msgRsp);

        } catch (CompletionException | ExecutionException | CannotProceedException | IllegalArgumentException e) {
            Assert.fail(failMessage("QueryDataCorrelator#insertQueryResponse()", e));
            
        }
        
        // Get the processed data and try to create SamplingProcess
        SortedSet<CorrelatedQueryData>  setPrcdData = CORRELATOR.getCorrelatedSet();
        
        try {
            SamplingProcess         process = SamplingProcess.from(setPrcdData);
            SamplingProcessTable    table = SamplingProcessTable.from(process); // method under test
            
            assertTrue("Results set contained no data sources.", table.getColumnCount() > 0);
            
        } catch (MissingResourceException | IllegalArgumentException | IllegalStateException | 
                 RangeException | UnsupportedOperationException | CompletionException e) {
            
            Assert.fail(failMessage("SamplingProcessTable#from(SamplingProcess)", e));
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#SamplingProcessTable(java.util.SortedSet)}.
     */
    @Test
    public final void testSamplingProcessTable() {
        
        List<QueryDataResponse> lstRspMsgs = LST_QUERY_RSP_WIDE;
        
        try {
            for (QueryDataResponse msgRsp : lstRspMsgs)
                CORRELATOR.addQueryResponse(msgRsp);

        } catch (CompletionException | ExecutionException | CannotProceedException |IllegalArgumentException e) {
            Assert.fail(failMessage("QueryDataCorrelator#insertQueryResponse()", e));
            
        }
        
        // Get the processed data and try to create SamplingProcess
        SortedSet<CorrelatedQueryData>  setPrcdData = CORRELATOR.getCorrelatedSet();
        
        try {
            SamplingProcessTable table = new SamplingProcessTable(setPrcdData);
            
            assertTrue("Results set contained no data sources.", table.getColumnCount() > 0);
            
        } catch (MissingResourceException | IllegalArgumentException | IllegalStateException | 
                 RangeException | UnsupportedOperationException | CompletionException e) {
            
            Assert.fail(failMessage("SamplingProcessTable#SamplingProcessTable(SortedSet)", e));
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#SamplingProcessTable(SamplingProcess)}.
     */
    @Test
    public final void testSamplingProcessTableSamplingProcess() {
        
        List<QueryDataResponse> lstRspMsgs = LST_QUERY_RSP_WIDE;
        
        try {
            for (QueryDataResponse msgRsp : lstRspMsgs)
                CORRELATOR.addQueryResponse(msgRsp);

        } catch (CompletionException | ExecutionException | CannotProceedException |IllegalArgumentException e) {
            Assert.fail(failMessage("QueryDataCorrelator#insertQueryResponse()", e));
            
        }
        
        // Get the processed data and try to create SamplingProcess
        SortedSet<CorrelatedQueryData>  setPrcdData = CORRELATOR.getCorrelatedSet();
        
        try {
            SamplingProcess      process = new SamplingProcess(setPrcdData);
            SamplingProcessTable table = new SamplingProcessTable(process);   // method under test
            
            assertTrue("Results set contained no data sources.", table.getColumnCount() > 0);
            
        } catch (MissingResourceException | IllegalArgumentException | IllegalStateException | 
                 RangeException | UnsupportedOperationException | CompletionException e) {
            
            Assert.fail(failMessage("SamplingProcessTable#SamplingProcessTable(SamplingProcess)", e));
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#isTableComplete()}.
     */
    @Test
    public final void testIsTableComplete() {
        List<QueryData>    lstRawData = LST_QUERY_DATA_LONG;
        
        SamplingProcessTable    table = this.createTable(lstRawData);
        
        Assert.assertTrue(table.isTableComplete());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#hasError()}.
     */
    @Test
    public final void testHasError() {
        List<QueryData>    lstRawData = LST_QUERY_DATA_LONG;
        
        SamplingProcessTable    table = this.createTable(lstRawData);
        
        Assert.assertFalse(table.hasError());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getRowCount()}.
     */
    @Test
    public final void testGetRowCount() {
        TestQueryRecord     recQuery = REC_ONE;
        DpDataRequest       dpRequest = recQuery.createRequest();
        long                cntSamples = INT_SAMPLING_RATE * dpRequest.approxDomainSize();
        
        try {
            List<QueryData>        lstRawData = recQuery.recoverQueryData();
            SamplingProcessTable    table = this.createTable(lstRawData);
            
            long                    cntRows = table.getRowCount();
            
            Assert.assertEquals(cntSamples, cntRows);
            
        } catch (ClassNotFoundException | IOException | DpGrpcException e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedMethodNameSimple(), e));
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumnCount()}.
     */
    @Test
    public final void testGetColumnCount() {
        TestQueryRecord     recQuery = REC_WIDE;
        DpDataRequest       dpRequest = recQuery.createRequest();
        long                cntSources = dpRequest.getSourceCount();

        try {
            List<QueryData>        lstRawData = recQuery.recoverQueryData();
            SamplingProcessTable    table = this.createTable(lstRawData);
            
            long                    cntCols = table.getColumnCount();
            
            Assert.assertEquals(cntSources, cntCols);
            
        } catch (ClassNotFoundException | IOException | DpGrpcException e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedMethodNameSimple(), e));
            
        }
        
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumnIndex(java.lang.String)}.
     */
    @Test
    public final void testGetColumnIndex() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        int             cntCols = table.getColumnCount();
        List<Integer>   lstColIndices = new ArrayList<>( IntStream.range(0, cntCols).boxed().toList() );
        List<String>    lstColNames = table.getColumnNames();
        
        for (String strColNm : lstColNames) {
            Integer     indCol = table.getColumnIndex(strColNm);
            
            Assert.assertNotEquals(null, indCol);
            Assert.assertTrue(lstColIndices.contains(indCol));
            
            // Remove index from index list
            lstColIndices.remove(indCol);
        }
        
        Assert.assertTrue("Not all indices represented in table, missing " + lstColIndices, lstColIndices.isEmpty());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumnNames()}.
     */
    @Test
    public final void testGetColumnNames() {
        TestQueryRecord     recQuery = REC_WIDE;
        DpDataRequest       dpRequest = recQuery.createRequest();
        
        List<String>        lstSrcNms = dpRequest.getSourceNames();

        List<QueryData>    lstRawData;
        try {
            lstRawData = recQuery.recoverQueryData();
        } catch (ClassNotFoundException | IOException | DpGrpcException e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedMethodName(), e) );
            return;
        }
        
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>    lstColNames = table.getColumnNames();
        
        Assert.assertTrue(lstSrcNms.containsAll(lstColNames));
        Assert.assertTrue(lstColNames.containsAll(lstSrcNms));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getTimestamps()}.
     */
    @Test
    public final void testGetTimestamps() {
        TestQueryRecord     recQuery = REC_ONE;
        DpDataRequest       dpRequest = recQuery.createRequest();
        long                cntSamples = INT_SAMPLING_RATE * dpRequest.approxDomainSize();
        
        try {
            List<QueryData>        lstRawData = recQuery.recoverQueryData();
            SamplingProcessTable    table = this.createTable(lstRawData);
            
            // Get the timestamps
            List<Instant>           lstTms = table.getTimestamps();
            
            // Check count
            Assert.assertEquals(cntSamples, lstTms.size());
            
            // Compute sampling period - should be constant
            Duration    durPeriod = Duration.between(lstTms.get(0), lstTms.get(1));
            
            // Verify values
            Integer     indPrev = 0;
            Instant     insPrev = null;
            
            for (Instant insCurr : lstTms) {
                
                // Initialize loop - first time through
                if (insPrev == null) {
                    insPrev = insCurr;
                    
                    continue;
                }
                
                Duration    durDelay = Duration.between(insPrev, insCurr);
                
                Assert.assertTrue("Timestamps misordered at index " + indPrev, insPrev.isBefore(insCurr));
                Assert.assertEquals("Bad sampling period at index " + indPrev, durDelay, durPeriod);
                
                insPrev = insCurr;
                indPrev++;
            }
            
        } catch (ClassNotFoundException | IOException | DpGrpcException e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedMethodNameSimple(), e));
            
        }
        
        
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumnName(int)}.
     */
    @Test
    public final void testGetColumnName() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        int             cntCols = table.getColumnCount();
        List<String>    lstColNms = table.getColumnNames();
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            String  strName = table.getColumnName(iCol);
            
            Assert.assertEquals("Inconsistent column name at index " + iCol, lstColNms.get(iCol), strName);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumnType(int)}.
     */
    @Test
    public final void testGetColumnTypeInt() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        int             cntCols = table.getColumnCount();
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            Integer             szCol = table.getColumnSize(iCol);
            DpSupportedType     enmType = table.getColumnType(iCol);    // method under test
            
            for (int iRow=0; iRow<szCol; iRow++) {
                Object      objVal = table.getValue(iRow, iCol);
                
                Assert.assertTrue("Bad column type for column " + iCol + " row " + iRow, enmType.isAssignableFrom(objVal.getClass()));
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumnType(java.lang.String)}.
     */
    @Test
    public final void testGetColumnTypeString() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>        lstColNms = table.getColumnNames();
        
        for (String strName : lstColNms) {
            Integer             szCol = table.getColumnSize(strName);
            DpSupportedType     enmType = table.getColumnType(strName); // method under test
            
            for (int iRow=0; iRow<szCol; iRow++) {
                Object      objVal = table.getValue(iRow, strName);
                
                Assert.assertTrue("Bad column type for column " + strName + " row " + iRow, enmType.isAssignableFrom(objVal.getClass()));
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumnSize(int)}.
     */
    @Test
    public final void testGetColumnSizeInt() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        int             cntCols = table.getColumnCount();
        Integer         cntRows = table.getRowCount();
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            Integer             szCol = table.getColumnSize(iCol);
            
            Assert.assertEquals(cntRows, szCol);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumnSize(java.lang.String)}.
     */
    @Test
    public final void testGetColumnSizeString() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>    lstColNms = table.getColumnNames();
        Integer         cntRows = table.getRowCount();
        
        for (String strName : lstColNms) {
            Integer             szCol = table.getColumnSize(strName);
         
            Assert.assertEquals(cntRows, szCol);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumn(int)}.
     */
    @Test
    public final void testGetColumnInt() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        int             cntCols = table.getColumnCount();
        Integer         cntRows = table.getRowCount();
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            Integer             szCol = table.getColumnSize(iCol);
            IDataColumn<Object> column = table.getColumn(iCol);    // method under test
            
            Assert.assertEquals(cntRows, szCol);
            Assert.assertEquals(szCol, column.getSize());
            
            // Check all column values
            for (int iRow=0; iRow<cntRows; iRow++) {
                Object objValTbl = table.getValue(iRow, iCol);
                Object objValCol = column.getValue(iRow);
                
                Assert.assertEquals("Column value not equal to equivalent table value.", objValTbl, objValCol);
            }
        }
        
        // Do it all again - table should have cached columns
        for (int iCol=0; iCol<cntCols; iCol++) {
            Integer             szCol = table.getColumnSize(iCol);
            IDataColumn<Object> column = table.getColumn(iCol);    // method under test
            
            Assert.assertEquals(cntRows, szCol);
            Assert.assertEquals(szCol, column.getSize());
            
            // Check all column values
            for (int iRow=0; iRow<cntRows; iRow++) {
                Object objValTbl = table.getValue(iRow, iCol);
                Object objValCol = column.getValue(iRow);
                
                Assert.assertEquals("Column value not equal to equivalent table value (2nd pass).", objValTbl, objValCol);
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumn(java.lang.String)}.
     */
    @Test
    public final void testGetColumnString() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>    lstColNms = table.getColumnNames();
        Integer         cntRows = table.getRowCount();
        
        for (String strName : lstColNms) {
            Integer             szCol = table.getColumnSize(strName);
            IDataColumn<Object> column = table.getColumn(strName);
            
            Assert.assertEquals(cntRows, szCol);
            Assert.assertEquals(szCol, column.getSize());
            
            for (int iRow=0; iRow<cntRows; iRow++) {
                Object  objValTbl = table.getValue(iRow, strName);
                Object  objValCol = column.getValue(iRow);

                Assert.assertEquals("Column value not equal to equivalent table value.", objValTbl, objValCol);
            }
        }

        // Do it all again - table should have cached columns
        for (String strName : lstColNms) {
            Integer             szCol = table.getColumnSize(strName);
            IDataColumn<Object> column = table.getColumn(strName);
            
            Assert.assertEquals(cntRows, szCol);
            Assert.assertEquals(szCol, column.getSize());
            
            for (int iRow=0; iRow<cntRows; iRow++) {
                Object  objValTbl = table.getValue(iRow, strName);
                Object  objValCol = column.getValue(iRow);

                Assert.assertEquals("Column value not equal to equivalent table value.", objValTbl, objValCol);
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumnSizeMin()}.
     */
    @Test
    public final void testGetColumnSizeMin() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>    lstColNms = table.getColumnNames();
        Integer         cntRows = table.getRowCount();
        Integer         szColMin = table.getColumnSizeMin();    // method under test

        // All columns should be equal sized
        Assert.assertEquals(cntRows, szColMin);
        
        for (String strName : lstColNms) {
            IDataColumn<Object>     column = table.getColumn(strName);
            
            Assert.assertEquals(szColMin, column.getSize());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumnSizeMax()}.
     */
    @Test
    public final void testGetColumnSizeMax() {
        List<QueryData> lstRawData = LST_QUERY_DATA_TWO;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>    lstColNms = table.getColumnNames();
        Integer         cntRows = table.getRowCount();
        Integer         szColMin = table.getColumnSizeMax();    // method under test

        // All columns should be equal sized
        Assert.assertEquals(cntRows, szColMin);
        
        for (String strName : lstColNms) {
            IDataColumn<Object>     column = table.getColumn(strName);
            
            Assert.assertEquals(szColMin, column.getSize());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getTimestamp(int)}.
     */
    @Test
    public final void testGetTimestamp() {
        List<QueryData> lstRawData = LST_QUERY_DATA_TWO;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        Integer         cntRows = table.getRowCount();
        List<Instant>   lstTms = table.getTimestamps();
        
        for (int iRow=0; iRow<cntRows; iRow++) {
            Instant     insTms = lstTms.get(iRow);
            Instant     insTbl = table.getTimestamp(iRow);
            
            Assert.assertEquals(insTms, insTbl);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getValue(int, int)}.
     */
    @Test
    public final void testGetValueIntInt() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>    lstColNms = table.getColumnNames();
        Integer         cntCols = table.getColumnCount();
        Integer         cntRows = table.getRowCount();
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            String                  strName = lstColNms.get(iCol);
            IDataColumn<Object>     column = table.getColumn(iCol);
            
            for (int iRow=0; iRow<cntRows; iRow++) {
                Object      objVal1 = table.getValue(iRow, iCol);       // method under test
                Object      objVal2 = table.getValue(iRow, strName);
                Object      objVal3 = column.getValue(iRow);
                
                Assert.assertEquals(objVal1, objVal2);
                Assert.assertEquals(objVal2, objVal3);
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getValue(int, java.lang.String)}.
     */
    @Test
    public final void testGetValueIntString() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>    lstColNms = table.getColumnNames();
        Integer         cntCols = table.getColumnCount();
        Integer         cntRows = table.getRowCount();
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            String                  strName = lstColNms.get(iCol);
            IDataColumn<Object>     column = table.getColumn(strName);
            
            for (int iRow=0; iRow<cntRows; iRow++) {
                Object      objVal1 = table.getValue(iRow, iCol);       
                Object      objVal2 = table.getValue(iRow, strName);    // method under test
                Object      objVal3 = column.getValue(iRow);
                
                Assert.assertEquals(objVal1, objVal2);
                Assert.assertEquals(objVal2, objVal3);
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getRowValues(int)}.
     */
    @Test
    public final void testGetRowValues() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>    lstColNms = table.getColumnNames();
        int             cntCols = table.getColumnCount();
        Integer         cntRows = table.getRowCount();
        
        for (int iRow=0; iRow<cntRows; iRow++) {
            Object[]    arrVals = table.getRowValues(iRow);     // method under test
            
            Assert.assertEquals(cntCols, arrVals.length);
            
            for (int iCol=0; iCol<cntCols; iCol++) {
                String                  strName = lstColNms.get(iCol);
                IDataColumn<Object>     column = table.getColumn(strName);

                Object  objVal1 = table.getValue(iRow, iCol);
                Object  objVal2 = column.getValue(iRow);
                
                Assert.assertEquals(objVal1, arrVals[iCol]);
                Assert.assertEquals(objVal2, arrVals[iCol]);
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getRowValuesAsList(int)}.
     */
    @Test
    public final void testGetRowValuesAsList() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>    lstColNms = table.getColumnNames();
        int             cntCols = table.getColumnCount();
        Integer         cntRows = table.getRowCount();
        
        for (int iRow=0; iRow<cntRows; iRow++) {
            List<Object>    lstVals = table.getRowValuesAsList(iRow);  // method under test
            
            Assert.assertEquals(cntCols, lstVals.size());
            
            for (int iCol=0; iCol<cntCols; iCol++) {
                String                  strName = lstColNms.get(iCol);
                IDataColumn<Object>     column = table.getColumn(strName);

                Object  objVal1 = table.getValue(iRow, iCol);
                Object  objVal2 = column.getValue(iRow);
                
                Assert.assertEquals(objVal1, lstVals.get(iCol));
                Assert.assertEquals(objVal2, lstVals.get(iCol));
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumnData(int)}.
     */
    @Test
    public final void testGetColumnDataInt() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>    lstColNms = table.getColumnNames();
        int             cntCols = table.getColumnCount();
        int             cntRows = table.getRowCount();
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            String                  strName = lstColNms.get(iCol);
            IDataColumn<Object>     column = table.getColumn(strName);
            List<Object>            lstVals = table.getColumnData(iCol);    // method under test
            
            Assert.assertEquals(cntRows, lstVals.size());
            
            for (int iRow=0; iRow<cntRows; iRow++) {
                Object      objVal1 = table.getValue(iRow, iCol);
                Object      objVal2 = column.getValue(iRow);
                
                Assert.assertEquals(objVal1, lstVals.get(iRow));
                Assert.assertEquals(objVal2, lstVals.get(iRow));
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumnData(java.lang.String)}.
     */
    @Test
    public final void testGetColumnDataString() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>    lstColNms = table.getColumnNames();
        int             cntCols = table.getColumnCount();
        int             cntRows = table.getRowCount();
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            String                  strName = lstColNms.get(iCol);
            IDataColumn<Object>     column = table.getColumn(strName);
            List<Object>            lstVals = table.getColumnData(strName);    // method under test
            
            Assert.assertEquals(cntRows, lstVals.size());
            
            for (int iRow=0; iRow<cntRows; iRow++) {
                Object      objVal1 = table.getValue(iRow, iCol);
                Object      objVal2 = column.getValue(iRow);
                
                Assert.assertEquals(objVal1, lstVals.get(iRow));
                Assert.assertEquals(objVal2, lstVals.get(iRow));
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumnDataTyped(int)}.
     */
    @Test
    public final void testGetColumnDataTypedInt() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>    lstColNms = table.getColumnNames();
        int             cntCols = table.getColumnCount();
        int             cntRows = table.getRowCount();
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            String                  strName = lstColNms.get(iCol);
            IDataColumn<Object>     column = table.getColumn(strName);
            List<Double>            lstVals = table.getColumnDataTyped(iCol);    // method under test
            
            Assert.assertEquals(cntRows, lstVals.size());
            
            for (int iRow=0; iRow<cntRows; iRow++) {
                Object      objVal1 = table.getValue(iRow, iCol);
                Object      objVal2 = column.getValue(iRow);
                
                Assert.assertEquals(objVal1, lstVals.get(iRow));
                Assert.assertEquals(objVal2, lstVals.get(iRow));
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#getColumnDataTyped(java.lang.String)}.
     */
    @Test
    public final void testGetColumnDataTypedString() {
        List<QueryData> lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>    lstColNms = table.getColumnNames();
        int             cntCols = table.getColumnCount();
        int             cntRows = table.getRowCount();
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            String                  strName = lstColNms.get(iCol);
            IDataColumn<Object>     column = table.getColumn(strName);
            List<Double>            lstVals = table.getColumnDataTyped(strName);    // method under test
            
            Assert.assertEquals(cntRows, lstVals.size());
            
            for (int iRow=0; iRow<cntRows; iRow++) {
                Object      objVal1 = table.getValue(iRow, iCol);
                Object      objVal2 = column.getValue(iRow);
                
                Assert.assertEquals(objVal1, lstVals.get(iRow));
                Assert.assertEquals(objVal2, lstVals.get(iRow));
            }
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.table.SamplingProcessTable#allocationSize()}.
     */
    @Test
    public final void testAllocationSize() {
        List<QueryData> lstRawData = LST_QUERY_DATA_LONG;
        
        SamplingProcessTable table = createTable(lstRawData);
        
        List<String>    lstColNms = table.getColumnNames();
        int             cntCols = table.getColumnCount();
        List<Instant>   lstTms = table.getTimestamps();
        
        // Get the table allocation
        long    lngTblSize = table.allocationSize();
        
        // Compute timestamps allocation
        long    lngTmsSize = lstTms.size() * 2 * Long.BYTES;

        // Aggregate column sizes
        long    lngColSize = 0;
        for (int iCol=0; iCol<cntCols; iCol++) {
            String                  strName = lstColNms.get(iCol);
            IDataColumn<Object>     column = table.getColumn(strName);
            
            lngColSize += column.allocationSize();
        }
        
        System.out.println("Table allocation size = " + lngTblSize);
        System.out.println("Aggregate column size = " + lngColSize);
        System.out.println("Timestamps size       = " + lngTmsSize);
        System.out.println("Columns + Timestamps  = " + (lngTmsSize + lngColSize));
    }

    //
    // Support Methods
    //
    
    /**
     * <p>
     * Processes the results set from a data query request into returned 
     * <code>SamplingProcessTable</code> instance.
     * </p>
     * <p>
     * Uses the <code>{@link #CORRELATOR}</code> singleton to process the given data into
     * a sorted set of correlated data.  
     * (Resets the <code>CORRELATOR</code> singleton before
     * processing.)  
     * Creates a <code>SamplingProcess</code> instance from the sorted correlated set.
     * </p>
     * <p>
     * If any exceptions originate from the internal invocation of creator
     * <code>{@link SamplingProcess#from(SortedSet)}</code>
     * a FAILURE assertion is made halting the test case using this method.
     * </p>
     *  
     * @param lstRawData    raw query results set data 
     * 
     * @return  ordered list of sampling blocks processed from given raw data
     */ 
    private SamplingProcessTable createTable(List<QueryData> lstRawData) {
        SortedSet<CorrelatedQueryData>  setPrcdData = correlate(lstRawData);
        
        try {
            SamplingProcessTable table = SamplingProcessTable.from(setPrcdData);

            return table;
            
        } catch (Exception e) {
            Assert.fail(failMessage(JavaRuntime.getQualifiedMethodNameSimple(), e));
            return null;
        }
    }

    /**
     * <p>
     * Correlates the given Query Service data into a processed data set and returns it.
     * </p>
     * <p>
     * Uses the <code>{@link #CORRELATOR}</code> singleton to process the given data into
     * a sorted set of correlated data.  Resets the <code>CORRELATOR</code> singleton before
     * processing.
     * </p>
     * 
     * @param lstRawData   raw query results set data to be correlated
     * 
     * @return  a sorted set of <code>CorrelatedQueryData</code> objects 
     */
    private SortedSet<CorrelatedQueryData>  correlate(List<QueryData> lstRawData) {

        CORRELATOR.reset();
        lstRawData.forEach(msgData -> CORRELATOR.addQueryData(msgData));

        SortedSet<CorrelatedQueryData>  setPrcdData = CORRELATOR.getCorrelatedSet();
        
        return setPrcdData;
    }
    
    /**
     * Creates an exception failure message from the given arguments
     * 
     * @param strHdr    message header
     * @param e         exception causing the failure
     * 
     * @return          message describing the exception failure
     */
    private static String  failMessage(String strHdr, Exception e) {
        String strMsg = strHdr + " - threw exception " + e.getClass().getSimpleName() + ": " + e.getMessage();
        
        return strMsg;
    }
}
