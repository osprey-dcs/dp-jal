/*
 * Project: dp-api-common
 * File:	SampledBlockTest.java
 * Package: com.ospreydcs.dp.api.query.model.coalesce
 * Type: 	SampledBlockTest
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
 * @since Mar 25, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.coalese;

import java.io.PrintStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.common.DpSupportedType;
import com.ospreydcs.dp.api.common.IDataColumn;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryApiFactoryNew;
import com.ospreydcs.dp.api.query.DpQueryStreamBuffer;
import com.ospreydcs.dp.api.query.IQueryService;
import com.ospreydcs.dp.api.query.model.coalesce.SampledBlock;
import com.ospreydcs.dp.api.query.model.coalesce.SampledTimeSeries;
import com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData;
import com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator;
import com.ospreydcs.dp.api.query.test.TestDpDataRequestGenerator;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

/**
 * <p>
 * JUnit test cases for class <code>SampledBlock</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 25, 2025
 *
 */
public class SampledBlockTest {

    
    //
    // Application Resources
    //
    
    /** The default Query Service configuration parameters */
    public static final DpQueryConfig          CFG_QUERY = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** General timeout limit */
    public static final long        LNG_TIMEOUT = CFG_QUERY.timeout.limit;
    
    /** General timeout units */
    public static final TimeUnit    TU_TIMEOUT = CFG_QUERY.timeout.unit;
    
    
    /** "Small" test request  */
    public static final DpDataRequest   RQST_SMALL = TestQueryResponses.QREC_LONG.createRequest();
    
    /** Wide test request (100 PVs, 5 secs) */
    public static final DpDataRequest   RQST_WIDE = TestQueryResponses.QREC_WIDE.createRequest();
    
    /** Large test request (500 PVs, 20 secs) */
    public static final DpDataRequest   RQST_BIG = TestQueryResponses.QREC_BIG.createRequest();
    
    /** Huge test request (1,000 PVs, 20 secs) */
    public static final DpDataRequest   RQST_HUGE = TestQueryResponses.QREC_HUGE.createRequest();
    
    
    /** The list of all (ordered) PV names - taken from the test request generator */
    public static final List<String>    LST_PV_NMS = TestDpDataRequestGenerator.LST_PV_NAMES;
    
    
    /** "Small" raw test data */
    public static List<QueryDataResponse.QueryData>     LST_RAW_DATA_SMALL;
    
    /** Wide raw test data */
    public static List<QueryDataResponse.QueryData>     LST_RAW_DATA_WIDE;
    
    /** Large raw test data */
    public static List<QueryDataResponse.QueryData>     LST_RAW_DATA_BIG;
    
    
    /** "Small" correlated test data */
    public static SortedSet<RawCorrelatedData>          SET_CORR_DATA_SMALL;

    /** Wide correlated test data */
    public static SortedSet<RawCorrelatedData>          SET_CORR_DATA_WIDE;

    /** Large correlated test data */
    public static SortedSet<RawCorrelatedData>          SET_CORR_DATA_BIG;

    
    //
    // Test Fixture Resources
    //
    
    /** The common Query Service API used for query test cases */
    private static IQueryService            apiTest;
    
    /** The common RawDataCorrelator used for testing cases */
    private static RawDataCorrelator        corrTest;
    
    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        // Create the Query Service connection
        apiTest = DpQueryApiFactoryNew.connect();
        
        // Recover the raw data from the test archive
        LST_RAW_DATA_SMALL = SampledBlockTest.recoverQueryData(RQST_SMALL);
        LST_RAW_DATA_WIDE = SampledBlockTest.recoverQueryData(RQST_WIDE);
        LST_RAW_DATA_BIG = SampledBlockTest.recoverQueryData(RQST_BIG);
        
        // Create the test correlator
        corrTest = RawDataCorrelator.create();
        
        // Create the correlated test data sets 
        LST_RAW_DATA_SMALL.forEach(msgData -> corrTest.processQueryData(msgData));
        SET_CORR_DATA_SMALL = new TreeSet<>( corrTest.getCorrelatedSet() );
        corrTest.reset();
        
        LST_RAW_DATA_WIDE.forEach(msgData -> corrTest.processQueryData(msgData));
        SET_CORR_DATA_WIDE = new TreeSet<>( corrTest.getCorrelatedSet() );
        corrTest.reset();
        
        LST_RAW_DATA_BIG.forEach(msgData -> corrTest.processQueryData(msgData));
        SET_CORR_DATA_BIG = new TreeSet<>( corrTest.getCorrelatedSet() );
        corrTest.reset();
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
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#from(com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData)}.
     */
    @Test
    public final void testFrom() {
        
        // Parameters
        final   RawCorrelatedData   blkRaw = SET_CORR_DATA_SMALL.getFirst();
        
        try {
            SampledBlock    blkTest = SampledBlock.from(blkRaw);
            
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
            this.printOutBlockDescr(System.out, blkTest);
        
        } catch (Exception e) {
            Assert.fail("Sampled block contruction threw exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#SampledBlock(com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData)}.
//     */
//    @Test
//    public final void testSampledBlock() {
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#insertNullTimeSeries(java.lang.String, com.ospreydcs.dp.api.common.DpSupportedType)}.
     */
    @Test
    public final void testInsertNullTimeSeries() {
        
        // Parameters
        final   RawCorrelatedData   blkRaw = SET_CORR_DATA_SMALL.getFirst();
        final   DpSupportedType     ennType = DpSupportedType.BOOLEAN;
        final   String              strPvNm = "dpTest_Null";
        
        try {
            SampledBlock    blkTest = SampledBlock.from(blkRaw);
            
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
            System.out.println("SampledBlock before null insertion:");
            this.printOutBlockDescr(System.out, blkTest);
            
            blkTest.insertNullTimeSeries(strPvNm, ennType);
            System.out.println("SampledBlock after null insertion:");
            this.printOutBlockDescr(System.out, blkTest);
            System.out.println();
        
        } catch (Exception e) {
            Assert.fail("Sampled block contruction threw exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getStartTime()}.
     */
    @Test
    public final void testGetStartTime() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_WIDE;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Print out the start times
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
            int indTms = 1;
            for (SampledBlock blkTest : lstSmplBlks) {
                System.out.println("  Block #" + indTms + " start time: " + blkTest.getStartTime());
                indTms++;
            }
            
            // Check the ordering of start times
            Instant     insPrev = Instant.EPOCH;
            for (SampledBlock blkTest : lstSmplBlks) {
                Instant insCurr = blkTest.getStartTime();
                
                Assert.assertTrue(insCurr.isAfter(insPrev));
                insPrev = insCurr;
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getTimeRange()}.
     */
    @Test
    public final void testGetTimeRange() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_WIDE;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Print out the time ranges
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
            int indTms = 1;
            for (SampledBlock blkTest : lstSmplBlks) {
                System.out.println("  Block #" + indTms + " time range: " + blkTest.getTimeRange());
                indTms++;
            }
            
            // Check for time range collisions
            TimeInterval    tvlPrev = TimeInterval.EMPTY;
            for (SampledBlock blkTest : lstSmplBlks) {
                TimeInterval tvlCurr = blkTest.getTimeRange();
                
                Assert.assertFalse(tvlCurr.hasIntersectionClosed(tvlPrev));
                tvlPrev = tvlCurr;
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getSampleCount()}.
     */
    @Test
    public final void testGetSampleCount() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_BIG;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        final   int                             cntSmplsAll = setCorrData.getFirst().getSampleCount();
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Check all sample counts
            for (SampledBlock blkTest : lstSmplBlks) {
                int     cntSmpls = blkTest.getSampleCount();
                
                Assert.assertEquals(cntSmplsAll, cntSmpls);
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getDataSourceCount()}.
     */
    @Test
    public final void testGetDataSourceCount() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_BIG;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        final   int                             cntSrcsAll = setCorrData.getFirst().getSourceCount();
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Check all sample counts
            for (SampledBlock blkTest : lstSmplBlks) {
                int     cntSrcs = blkTest.getDataSourceCount();
                
                Assert.assertEquals(cntSrcsAll, cntSrcs);
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getSourceNames()}.
     */
    @Test
    public final void testGetSourceNames() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_WIDE;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Check all sample counts
            for (SampledBlock blkTest : lstSmplBlks) {
                List<String>    lstSrcNms = blkTest.getSourceNames();
                List<String>    lstPvNms = LST_PV_NMS.subList(0, lstSrcNms.size());
                
                Assert.assertTrue(lstPvNms.containsAll( lstSrcNms ));
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getSourceType(java.lang.String)}.
     */
    @Test
    public final void testGetSourceType() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_SMALL;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Get types of first block and store
            SampledBlock                    blkFirst = lstSmplBlks.getFirst();
            Map<String, DpSupportedType>    mapSrcToType = blkFirst
                    .getSourceNames()
                    .stream()
                    .collect(
                            Collectors.toMap(strNm -> strNm, strNm -> blkFirst.getColumnType(strNm))
                            );
            
            // Check all column types for all sample blocks
            for (SampledBlock blkTest : lstSmplBlks) {
                List<String>    lstSrcNms = blkTest.getSourceNames();
                
                for (String strSrcNm : lstSrcNms) {
                    DpSupportedType enmTypeFirst = mapSrcToType.get(strSrcNm);
                    DpSupportedType enmSrcType = blkTest.getColumnType(strSrcNm);
                    
                    Assert.assertEquals(enmTypeFirst, enmSrcType);
                }
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getTimeSeries(java.lang.String)}.
     */
    @Test
    public final void testGetTimeSeries() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_SMALL;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        final   int                             cntSmples = setCorrData.getFirst().getSampleCount();
        
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Get types of first block and store
            SampledBlock                    blkFirst = lstSmplBlks.getFirst();
            Map<String, DpSupportedType>    mapSrcToType = blkFirst
                    .getSourceNames()
                    .stream()
                    .collect(
                            Collectors.toMap(strNm -> strNm, strNm -> blkFirst.getColumnType(strNm))
                            );
            
            // Check all column types for all sample blocks
            for (SampledBlock blkTest : lstSmplBlks) {
                List<String>    lstSrcNms = blkTest.getSourceNames();
                
                for (String strSrcNm : lstSrcNms) {
                    SampledTimeSeries<Object>   vecSeries = blkTest.getTimeSeries(strSrcNm);
                    
                    Assert.assertEquals(strSrcNm, vecSeries.getName());
                    Assert.assertEquals(mapSrcToType.get(strSrcNm), vecSeries.getType());
                    Assert.assertEquals(cntSmples, vecSeries.getSize().intValue());
                }
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getTimeSeriesAll()}.
     */
    @Test
    public final void testGetTimeSeriesAll() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_SMALL;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        final   int                             cntSmples = setCorrData.getFirst().getSampleCount();
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Get types of first block and store
            SampledBlock                    blkFirst = lstSmplBlks.getFirst();
            Map<String, DpSupportedType>    mapSrcToType = blkFirst
                    .getSourceNames()
                    .stream()
                    .collect(
                            Collectors.toMap(strNm -> strNm, strNm -> blkFirst.getColumnType(strNm))
                            );
            
            // Check all column types for all sample blocks
            for (SampledBlock blkTest : lstSmplBlks) {
                List<String>    lstSrcNms = blkTest.getSourceNames();
                
                for (String strSrcNm : lstSrcNms) {
                    Map<String, SampledTimeSeries<Object>>  mapSeries = blkTest.getTimeSeriesAll();
                    SampledTimeSeries<Object>               vecSeries = mapSeries.get(strSrcNm);
                            
                    Assert.assertEquals(strSrcNm, vecSeries.getName());
                    Assert.assertEquals(mapSrcToType.get(strSrcNm), vecSeries.getType());
                    Assert.assertEquals(cntSmples, vecSeries.getSize().intValue());
                }
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#hasSourceData(java.lang.String)}.
     */
    @Test
    public final void testHasSourceData() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_WIDE;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        final   Set<String>                     setSrcNms = setCorrData.getFirst().getSourceNames();
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Check all blocks for source data
            for (SampledBlock blkTest : lstSmplBlks) {

                for (String strSrcNm : setSrcNms) {
                    Assert.assertTrue(blkTest.hasSourceData(strSrcNm));
                }
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#hasDomainIntersection(com.ospreydcs.dp.api.query.model.coalesce.SampledBlock)}.
     */
    @Test
    public final void testHasDomainIntersection() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_WIDE;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Check for time range collisions
            boolean         bolFirst = true;
            SampledBlock    blkPrev = null;
            for (SampledBlock blkCurr : lstSmplBlks) {
                if (bolFirst) {
                    bolFirst = false;
                    blkPrev = blkCurr;
                    continue;
                }
                
                Assert.assertFalse(blkCurr.hasDomainIntersection(blkPrev));
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#isTableComplete()}.
     */
    @Test
    public final void testIsTableComplete() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_BIG;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Check for all data tables complete
            for (SampledBlock blkTest : lstSmplBlks) {
                Assert.assertTrue(blkTest.isTableComplete());
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#hasError()}.
     */
    @Test
    public final void testHasError() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_BIG;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Check no errors for all data tables 
            for (SampledBlock blkTest : lstSmplBlks) {
                Assert.assertFalse(blkTest.hasError());
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#clear()}.
     */
    @Test
    public final void testClear() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_BIG;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Clear all sampled blocks and check 
            for (SampledBlock blkTest : lstSmplBlks) {
                Assert.assertTrue(blkTest.getDataSourceCount() > 0);
                Assert.assertTrue(blkTest.getSampleCount() > 0);
                
                blkTest.clear();
                
                Assert.assertTrue(blkTest.getDataSourceCount() == 0);
                Assert.assertTrue(blkTest.getSampleCount() == 0);
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getRowCount()}.
     */
    @Test
    public final void testGetRowCount() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_SMALL;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        final   int                             cntSmples = setCorrData.getFirst().getSampleCount();
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Check all row counts
            for (SampledBlock blkTest : lstSmplBlks) {
                Assert.assertEquals(cntSmples, blkTest.getRowCount().intValue());
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getColumnCount()}.
     */
    @Test
    public final void testGetColumnCount() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_WIDE;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        final   int                             cntSrcs = setCorrData.getFirst().getSourceCount();
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Check all row counts
            for (SampledBlock blkTest : lstSmplBlks) {
                Assert.assertEquals(cntSrcs, blkTest.getColumnCount().intValue());
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getColumnIndex(java.lang.String)}.
     */
    @Test
    public final void testGetColumnIndex() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_WIDE;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        final   Set<String>                     setSrcNms = setCorrData.getFirst().getSourceNames();
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Check column indexes
            for (SampledBlock blkTest : lstSmplBlks) {
                
                for (String strSrcNm : setSrcNms) {
                    int     indCol = blkTest.getColumnIndex(strSrcNm);
                    String  strColNm = blkTest.getColumnName(indCol);
                    
                    Assert.assertEquals(strSrcNm, strColNm);
                }
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getColumnNames()}.
     */
    @Test
    public final void testGetColumnNames() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_WIDE;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        final   Set<String>                     setSrcNms = setCorrData.getFirst().getSourceNames();
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Check column indexes
            for (SampledBlock blkTest : lstSmplBlks) {

                List<String>    lstColNms = blkTest.getColumnNames();
                
                Assert.assertTrue(lstColNms.containsAll(setSrcNms));
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getTimestamps()}.
     */
    @Test
    public final void testGetTimestamps() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_WIDE;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        final   int                             cntSmpls = setCorrData.getFirst().getSampleCount();
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Check timestamps
            for (SampledBlock blkTest : lstSmplBlks) {

                List<Instant>    lstTms = blkTest.getTimestamps();
                Assert.assertEquals(cntSmpls, lstTms.size());
                
                Instant     insPrev = Instant.EPOCH;
                for (Instant insCurr : lstTms) {
                    Assert.assertTrue(insCurr.isAfter(insPrev));
                    insPrev = insCurr;
                }
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getColumn(int)}.
     */
    @Test
    public final void testGetColumnInt() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_WIDE;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        final   int                             cntSrcs = setCorrData.getFirst().getSourceCount();
        final   int                             cntTms = setCorrData.getFirst().getSampleCount();
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Get columns and check
            for (SampledBlock blkTest : lstSmplBlks) {

                for (int indCol=0; indCol<cntSrcs; indCol++) {
                    IDataColumn<Object>     col = blkTest.getColumn(indCol);
                    
                    Assert.assertEquals(cntTms, col.getSize().intValue());
                }
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#getColumn(java.lang.String)}.
     */
    @Test
    public final void testGetColumnString() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_WIDE;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        final   int                             cntTms = setCorrData.getFirst().getSampleCount();
        final   Set<String>                     setSrcNms = setCorrData.getFirst().getSourceNames();
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Get columns and check
            for (SampledBlock blkTest : lstSmplBlks) {

                for (String strSrcNm : setSrcNms) {
                    IDataColumn<Object>     col = blkTest.getColumn(strSrcNm);
                    
                    Assert.assertEquals(cntTms, col.getSize().intValue());
                }
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.query.model.coalesce.SampledBlock#compareTo(com.ospreydcs.dp.api.query.model.coalesce.SampledBlock)}.
     */
    @Test
    public final void testCompareTo() {
        
        // Parameters
        final   SortedSet<RawCorrelatedData>    setCorrData = SET_CORR_DATA_SMALL;
        final   List<SampledBlock>              lstSmplBlks = new ArrayList<>(setCorrData.size());
        
        try {
            for (RawCorrelatedData blkCorr : setCorrData) {
                
                SampledBlock    blkTest = SampledBlock.from(blkCorr);
                
                lstSmplBlks.add(blkTest);
            }
            
            // Get columns and check
            boolean         bolFirst = true;
            SampledBlock    blkPrev = null;
            for (SampledBlock blkCurr : lstSmplBlks) {
                if (bolFirst) {
                    bolFirst = false;
                    blkPrev = blkCurr;
                    continue;
                }
                
                Assert.assertTrue(blkCurr.compareTo(blkPrev) > 0);
            }
            
        } catch (Exception e) {
            Assert.fail("Exception " + e.getClass().getSimpleName() + " thrown: " + e.getMessage());
            
        }
    }


    //
    // Support Methods
    // 

    /**
     * <p>
     * Performs the given time-series data request and recovers the raw Query Service data.
     * </p>
     * <p>
     * Uses the class resource <code>{@link #apiTest}</code> to perform the query and a 
     * <code>DpQueryStreamBuffer</code> instance to recover the raw data.  The 
     * <code>QueryDataResponse.QueryData</code> Protocol Buffers data messages are extracted from
     * the recovered data and returned.
     * </p>
     * 
     * @param rqst  time-series data request
     * 
     * @return  the raw time-series data obtained from the Query Service 
     */
    private static List<QueryDataResponse.QueryData>   recoverQueryData(DpDataRequest rqst) {
        
        DpQueryStreamBuffer bufData = SampledBlockTest.apiTest.queryDataStream(rqst);
        
        try {
            bufData.startAndAwaitCompletion();
            
            List<QueryDataResponse> lstRsps = bufData.getBuffer();
            
            boolean bolValid = lstRsps.stream().allMatch(msg -> msg.hasQueryData());
            
            if (!bolValid) {
                String strMsg = JavaRuntime.getQualifiedMethodNameSimple() +
                        " - Query Service reported an exceptional result within the data stream.";
                
                System.err.println(strMsg);
                Assert.fail(strMsg);
            }
            
            List<QueryDataResponse.QueryData>   lstData = lstRsps
                    .stream()
                    .<QueryDataResponse.QueryData>map(msg -> msg.getQueryData())
                    .toList();
            
            return lstData;
            
        } catch (IllegalStateException | IllegalArgumentException | InterruptedException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - exception "
                    + e.getClass().getSimpleName()
                    + " during data recovery: "
                    + e.getMessage();
            
            System.err.println(strMsg);
            Assert.fail(strMsg);
            return null;
        }
    }
    
    /**
     * <p>
     * Prints out text description of the given sampled block to the given print stream.
     * </p>
     * 
     * @param ps        stream receiving sampled block description
     * @param blkTest   subject of the text description information
     */
    private void printOutBlockDescr(PrintStream ps, SampledBlock blkTest) {
        
        ps.println("  Table column count : " + blkTest.getSampleCount());
        ps.println("  Table row count    : " + blkTest.getRowCount());
        ps.println("  Source names       : " + blkTest.getSourceNames());
        ps.println("  Allocation (bytes) : " + blkTest.allocationSize());
    
    }
}
