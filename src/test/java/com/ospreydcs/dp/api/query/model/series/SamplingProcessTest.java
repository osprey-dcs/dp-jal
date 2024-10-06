/*
 * Project: dp-api-common
 * File:	SamplingProcessTest.java
 * Package: com.ospreydcs.dp.api.query.model.series
 * Type: 	SamplingProcessTest
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
 * @since Feb 9, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.series;

import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.naming.CannotProceedException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.ranges.RangeException;

import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.model.DpSupportedType;
import com.ospreydcs.dp.api.model.IDataColumn;
import com.ospreydcs.dp.api.model.IDataTable;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData;
import com.ospreydcs.dp.api.query.model.grpc.QueryDataCorrelator;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.query.test.TestQueryResponses.SingleQueryType;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData.DataBucket;

/**
 * JUnit test cases for <code>{@link SamplingProcess}</code>.
 * 
 * @author Christopher K. Allen
 * @since Feb 9, 2024
 *
 */
public class SamplingProcessTest {

    
    //
    // Class Constants
    //
    
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.series.SamplingProcess#from(java.util.SortedSet)}.
     */
    @Test
    public final void testFrom() {
        List<QueryDataResponse> lstRspMsgs = LST_QUERY_RSP_WIDE;
        
        try {
            for (QueryDataResponse msgRsp : lstRspMsgs)
                CORRELATOR.addQueryResponse(msgRsp);

        } catch (ExecutionException | CannotProceedException e) {
            Assert.fail(failMessage("QueryDataCorrelator#insertQueryResponse()", e));
            
        }
        
        // Get the processed data and try to create SamplingProcess
        SortedSet<CorrelatedQueryData>  setPrcdData = CORRELATOR.getCorrelatedSet();
        
        try {
            SamplingProcess process = SamplingProcess.from(setPrcdData);
            
            assertTrue("Results set contained no data sources.", process.getSamplingBlockCount() > 0);
            
        } catch (MissingResourceException | IllegalArgumentException | IllegalStateException | 
                 RangeException | UnsupportedOperationException | CompletionException e) {
            
            Assert.fail(failMessage("SamplingProcess#from()", e));
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.series.SamplingProcess#SamplingProcess(java.util.SortedSet)}.
     */
    @Test
    public final void testSamplingProcess() {
        List<QueryDataResponse> lstRspMsgs = LST_QUERY_RSP_LONG;
        
        try {
            for (QueryDataResponse msgRsp : lstRspMsgs)
                CORRELATOR.addQueryResponse(msgRsp);

        } catch (ExecutionException | CannotProceedException e) {
            Assert.fail(failMessage("QueryDataCorrelator#insertQueryResponse()", e));
            
        }
        
        // Get the processed data and try to create SamplingProcess
        SortedSet<CorrelatedQueryData>  setPrcdData = CORRELATOR.getCorrelatedSet();
        
        try {
            SamplingProcess process = new SamplingProcess(setPrcdData);
            
            assertTrue("Results set contained no data sources.", process.getSamplingBlockCount() > 0);
            
        } catch (MissingResourceException | IllegalArgumentException | IllegalStateException | 
                 RangeException | UnsupportedOperationException | CompletionException e) {
            
            Assert.fail(failMessage("new SamplingProcess()", e));
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.series.SamplingProcess#getSampleCount()}.
     */
    @Test
    public final void testGetSampleCount() {
        List<QueryData>    lstRawData = LST_QUERY_DATA_ONE;
        
        // Check that raw data contains all sampling clocks
        boolean bolAllClocks = lstRawData
                .stream()
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .allMatch(msgBck -> msgBck.getDataTimestamps().hasSamplingClock());
        
        if (!bolAllClocks)
            Assert.fail("Raw data does NOT contain all sampling clocks.");
        
        // Create test process
        SamplingProcess processTest = this.process(lstRawData);
        
        // Get the total number of samples from raw data sampling clocks 
        //  - This works assuming only 1 data source
        int     cntSamplesRaw = lstRawData
                    .stream()
                    .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                    .mapToInt(msgBucket -> msgBucket.getDataTimestamps().getSamplingClock().getCount())
                    .sum();
     
        // Compare the sample counts
        int     cntSamplesPrcd = processTest.getSampleCount();
        
        Assert.assertEquals(cntSamplesRaw, cntSamplesPrcd);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.series.SamplingProcess#getTimeDomain()}.
     */
    @Test
    public final void testGetTimeDomain() {
        List<QueryData>    lstRawData = LST_QUERY_DATA_ONE;
        
        // Check that raw data contains all sampling clocks
        boolean bolAllClocks = lstRawData
                .stream()
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .allMatch(msgBck -> msgBck.getDataTimestamps().hasSamplingClock());
        
        if (!bolAllClocks)
            Assert.fail("Raw data does NOT contain all sampling clocks.");
        
        // Create test process
        SamplingProcess processTest = this.process(lstRawData);

        // Get the total number of samples from raw data sampling clocks 
        //  - This works assuming only 1 data source
        int     cntSamplesRaw = lstRawData
                    .stream()
                    .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                    .mapToInt(msgBucket -> msgBucket.getDataTimestamps().getSamplingClock().getCount())
                    .sum();
        
        // Get the start time from the raw data sampling clocks
        Instant     insBegin = lstRawData
                    .stream()
                    .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                    .<Instant>map(msgBucket -> ProtoMsg.toInstant(
                                      msgBucket.getDataTimestamps().getSamplingClock().getStartTime()
                                      )
                                  )
                    .reduce( (t1,t2) -> (t1.isBefore(t2)) ? t1 : t2 )
                    .get();
        
        // Get the sample period from the raw data sampling clock
        //  - This work only if all clocks have the same period 
        //    (otherwise it returns the smallest period)
        long        longPeriodNs = lstRawData
                .stream()
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .<Long>map(msgBucket -> msgBucket.getDataTimestamps().getSamplingClock().getPeriodNanos() )
                .reduce( (T1,T2) -> (T1 < T2) ? T1 : T2 )
                .get(); 
        
        // Create the time domain from raw data and compare
        // - Recall that sampling process time domains are the 
        //   SMALLEST, connect interval containing all sample times
        Instant         insEnd = insBegin.plusNanos( (cntSamplesRaw - 1) * longPeriodNs);
        TimeInterval    domRaw = TimeInterval.from(insBegin, insEnd);
        
        TimeInterval    domProcess = processTest.getTimeDomain();
        
        Assert.assertEquals(domRaw, domProcess);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.series.SamplingProcess#getDataSourceCount()}.
     */
    @Test
    public final void testGetDataSourceCount() {
        List<QueryData>    lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcess processTest = this.process(lstRawData);

        // Extract all data source names from raw data (including repeats)
        //  Then create set of unique data source names
        //  Then get the size of unique name set
        List<String>    lstPvNmsRaw = lstRawData
                    .stream()
                    .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                    .<String>map(msgBucket -> msgBucket.getDataColumn().getName())
                    .toList();
        
        Set<String>     setPvNmsRaw = new TreeSet<>(lstPvNmsRaw);
        int             cntPvNmsRaw = setPvNmsRaw.size();
        
        // Compare source name count from raw data and source count from sampling process
        int         cntPvsPrcs = processTest.getDataSourceCount();
        
        Assert.assertEquals(cntPvNmsRaw, cntPvsPrcs);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.series.SamplingProcess#getDataSourceNames()}.
     */
    @Test
    public final void testGetDataSourceNames() {
        List<QueryData>    lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcess processTest = this.process(lstRawData);

        // Extract all data source names from raw data (including repeats)
        //  Then create set of unique data source names
        List<String>    lstPvNmsRaw = lstRawData
                    .stream()
                    .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                    .<String>map(msgBucket -> msgBucket.getDataColumn().getName())
                    .toList();
        Set<String>     setPvNmsRaw = new TreeSet<>(lstPvNmsRaw);
        
        // Compare source names from raw data and sampling process
        Set<String>     setPvNmsPrcs = processTest.getDataSourceNames();
        
        Assert.assertEquals(setPvNmsRaw, setPvNmsPrcs);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.series.SamplingProcess#getSourceType(java.lang.String)}.
     */
    @Test
    public final void testGetSourceType() {
        List<QueryData>    lstRawData = LST_QUERY_DATA_WIDE;
        
        SamplingProcess processTest = this.process(lstRawData);

        // Create a map of {(pv name, pv data type)} pairs from raw data
        List<DataBucket>    lstBucketsRaw = lstRawData
                .stream()
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .toList();
        Map<String, DpSupportedType>    mapPvTypesRaw = lstBucketsRaw
                .stream()
                .<DataColumn>map(DataBucket::getDataColumn)
                .collect(Collectors.toMap(
                                        DataColumn::getName, 
                                        ProtoMsg::extractType, 
                                        (T1, T2) -> (T1 == T2) ? T1 : DpSupportedType.UNSUPPORTED_TYPE
                                        )
                        );

        // First check raw data for PVs with inconsistent types
        List<String>    lstBadPvs = mapPvTypesRaw
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == DpSupportedType.UNSUPPORTED_TYPE)
                .<String>map(Map.Entry::getKey)
                .toList();
        Assert.assertTrue("Raw data contained PV with inconsistent types: " + lstBadPvs, lstBadPvs.isEmpty());
        
        // Compare all sample process PV types with raw data PV types
        //  First check that all PVs are present
        Set<String>     setPvNmsRaw = mapPvTypesRaw.keySet();
        Set<String>     setPvNmsPrcs = processTest.getDataSourceNames();
        Assert.assertEquals("Process PV name set NOT EQUAL to raw data PV name set.", setPvNmsRaw, setPvNmsPrcs);
        
        //  Now check all types
        List<String>    lstBadProcessPvs = mapPvTypesRaw
                .entrySet()
                .stream()
                .filter(entry -> processTest.getSourceType(entry.getKey()) != entry.getValue())
                .<String>map(Map.Entry::getKey)
                .toList();
        
        Assert.assertTrue("Sampling process PV(s) had bad types: " + lstBadProcessPvs, lstBadProcessPvs.isEmpty());
    }
    
    /**
     * Test method for {@link SamplingProcess#timeSeries(String)}.
     */
    @Test
    public final void testTimeSeries() {
        List<QueryData>    lstRawData = LST_QUERY_DATA_ONE;
        
        SamplingProcess processTest = this.process(lstRawData);

        // Check for all sampling clocks
        if ( !lstRawData
                .stream()
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .allMatch(msgBck -> msgBck.getDataTimestamps().hasSamplingClock()) 
                )
            Assert.fail("Raw data did NOT contain all sampling clocks.");
            
        // Get the sampling values from the raw data in a map {(start time, data column)}
        Map<Instant, DataColumn>    mapStartToCol = lstRawData
                .stream()
                .<DataBucket>flatMap(msgData -> msgData.getDataBucketsList().stream())
                .collect(Collectors.toMap(
                                msgBucket -> ProtoMsg.toInstant(msgBucket.getDataTimestamps().getSamplingClock().getStartTime()), 
                                DataBucket::getDataColumn
                                )
                        );
        // Create set of ordered start times
        SortedSet<Instant>      setStartTimes = new TreeSet<>(mapStartToCol.keySet());
        
        // Build the data values list
        List<Object>        lstValsRaw = setStartTimes
                .stream()
                .flatMap(t -> ProtoMsg.extractValues(mapStartToCol.get(t)).stream())
                .toList();
        
        // Compare raw and process values
        String  strSrcNm = lstRawData.get(0).getDataBucketsList().get(0).getDataColumn().getName();
        
        List<Object>    lstValsPrcs = processTest.timeSeries(strSrcNm).getValues();
        
        Assert.assertEquals(lstValsRaw, lstValsPrcs);
    }

    /**
     * Test method for {@link SamplingProcess#createStaticDataTable()}.
     */
    @Test
    public final void   testCreateStaticTable() {
        
        // Test data request
        SingleQueryType     enmQryType = SingleQueryType.WIDE;
        
        // Get original data request for this query and extract parameters
        DpDataRequest       dpRequest = TestQueryResponses.request(enmQryType);
        TimeInterval        domRqstRng = dpRequest.range();
        List<String>        lstRqstSrcs = dpRequest.getSourceNames();
        
        // Create the static data table under test
        // - get the query results set
        // - create sampling process
        // - create static data table
        List<QueryData>    lstRawData = TestQueryResponses.queryData(enmQryType);
        SamplingProcess     processTest = process(lstRawData);
        IDataTable          tableTest = processTest.createStaticDataTable();
        
        
        // Test the table column names
        List<String> lstMissingCols = lstRqstSrcs
                .stream()
                .filter(strName -> !tableTest.hasColumn(strName))
                .toList();
        Assert.assertTrue("Table contained missing data columns " + lstMissingCols, lstMissingCols.isEmpty());
        
        // Test the table timestamp range
        List<Instant>   lstTms = tableTest.getTimestamps();
        Instant         insStart = lstTms.get(0);
        Instant         insStop = lstTms.get( lstTms.size() - 1);
        TimeInterval    domTblRng = TimeInterval.from(insStart, insStop);
        Assert.assertTrue("Table timestamp range not contained in data request range.", domRqstRng.contains(domTblRng));
        
        // Test table columns
        int     cntRows = tableTest.getRowCount();
        int     cntCols = tableTest.getColumnCount();
        Assert.assertEquals(lstTms.size(), cntRows);
        Assert.assertEquals(lstRqstSrcs.size(), cntCols);
        
        for (int iCol=0; iCol<cntCols; iCol++) {
            String      strName = tableTest.getColumnName(iCol);
            
            try {
                IDataColumn<Object>     col = tableTest.getColumn(strName);

                Assert.assertTrue("Column " + strName + " size " + col.getSize() + " not equal " + cntRows, cntRows == col.getSize());
            
            } catch (Exception e) {
                Assert.fail("tableTest.getColumn(" + strName + ") threw exception " + e);
                
            }
        }
        
        // Check table data against process data 
        int cntBlocks = processTest.getSamplingBlockCount();
        int indOffset = 0;
        
        for (int iBlock=0; iBlock<cntBlocks; iBlock++) {
            UniformSamplingBlock    block = processTest.getSamplingBlock(iBlock);
            
            int cntBlkRows = block.getRowCount();
            int cntBlkCols = block.getColumnCount();
            for (int iRow = 0; iRow < cntBlkRows; iRow++) {
                int indTblRow = indOffset + iRow;

                // check timestamp
                Instant     insBlk = block.getTimestamp(iRow);
                Instant     insTbl = tableTest.getTimestamp(indTblRow);
                
                Assert.assertEquals("Table timestamp at row index " + indTblRow + " not equal to block at " + iRow, insBlk, insTbl);
                
                // check values
                for (int iCol = 0; iCol < cntBlkCols; iCol++) {
                    String      strName = block.getColumnName(iCol);

                    Object  objBlk = block.getValue(iRow, iCol);
                    Object  objTbl = tableTest.getValue(indTblRow, iCol);

                    Assert.assertEquals("Table value at row index  " + indTblRow + " not equal to block at " +iRow + " for " + strName, objBlk, objTbl);
                }
            }

            indOffset += block.getRowCount();
        }
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Processes the results set from a data query request into returned 
     * <code>SamplingProcess</code> instance.
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
    private SamplingProcess process(List<QueryData> lstRawData) {
        SortedSet<CorrelatedQueryData>  setPrcdData = correlate(lstRawData);
        
        try {
            SamplingProcess process = SamplingProcess.from(setPrcdData);

            return process;
            
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
