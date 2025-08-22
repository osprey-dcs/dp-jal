/*
 * Project: dp-api-common
 * File:	DpCreateDatasetRequestTest.java
 * Package: com.ospreydcs.dp.api.annotate
 * Type: 	DpCreateDatasetRequestTest
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
 * @since Mar 3, 2025
 *
 */
package com.ospreydcs.dp.api.annotate;

import java.time.Instant;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.annotate.model.DpDataBlock;
import com.ospreydcs.dp.api.common.IDataTable;
import com.ospreydcs.dp.api.common.OwnerUID;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryApiFactoryNew;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.IQueryService;
import com.ospreydcs.dp.api.query.test.TestDpDataRequestGenerator;
import com.ospreydcs.dp.api.query.test.TestQueryResponses;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.annotation.CreateDataSetRequest;
import com.ospreydcs.dp.grpc.v1.annotation.DataBlock;
import com.ospreydcs.dp.grpc.v1.annotation.DataSet;

/**
 * <p>
 * JUnit test cases for class <code>DpCreateDatasetRequest</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 3, 2025
 *
 */
public class DpCreateDatasetRequestTest {

    
    //
    // Class Constants
    //
    
    /** The start instant of the Data Platform test archive */
    public static final Instant         INS_INCEPT = TestDpDataRequestGenerator.INS_INCEPT;
    
    /** The final time instant of the Data Platform test archive */
    public static final Instant         INS_FINAL = TestDpDataRequestGenerator.INS_FINAL;
    
    /** The list of all PV names within the Data Platform test archive */
    public static final List<String>    LST_PV_NMS = TestDpDataRequestGenerator.LST_PV_NAMES;
    
    
    /** "Small" test request  */
    public static final DpDataRequest   RQST_SMALL = TestQueryResponses.QREC_LONG.createRequest();
    
    /** Wide test request (500 PVs, 5 secs) */
    public static final DpDataRequest   RQST_WIDE = TestQueryResponses.QREC_WIDE.createRequest();
    
    /** Large test request */
    public static final DpDataRequest   RQST_BIG = TestQueryResponses.QREC_BIG.createRequest();
    
    /** Huge test request */
    public static final DpDataRequest   RQST_HUGE = TestQueryResponses.QREC_HUGE.createRequest();
    
    /** The data request for the half the test archive - used for timing responses */
    public static final DpDataRequest   RQST_HALF_SRC = TestQueryResponses.QREC_HALF_SRC.createRequest();

    /** The data request for the half the test archive - used for timing responses */
    public static final DpDataRequest   RQST_HALF_RNG = TestQueryResponses.QREC_HALF_RNG.createRequest();

    /** The data request for the entire test archive - used for timing responses */
    public static final DpDataRequest   RQST_ALL = TestQueryResponses.QREC_ALL.createRequest();

    
    //
    // Class Resources
    //
    
    /** Class-based owner UID used for data set creation */
    public static final OwnerUID        REC_OWNER1 = OwnerUID.from(DpCreateDatasetRequestTest.class.getSimpleName() + "_1");
    
    /** Class-based owner UID used for data set creation */
    public static final OwnerUID        REC_OWNER2 = OwnerUID.from(DpCreateDatasetRequestTest.class.getSimpleName() + "_2");

    
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
     * Test method for {@link com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest#from(com.ospreydcs.dp.api.common.OwnerUID, java.lang.String)}.
     */
    @Test
    public final void testFromOwnerUIDString() {
        
        // Parameters
        final OwnerUID      recOwner = REC_OWNER1;
        final String        strName = JavaRuntime.getQualifiedMethodNameSimple();
        
        // Create request and verify
        DpCreateDatasetRequest  rqst = DpCreateDatasetRequest.from(recOwner, strName);
        
        Assert.assertEquals(recOwner, rqst.getOwnerUid());
        Assert.assertEquals(strName, rqst.getName());
        Assert.assertEquals(null, rqst.getDescription());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest#from(com.ospreydcs.dp.api.common.OwnerUID, java.lang.String, java.lang.String)}.
     */
    @Test
    public final void testFromOwnerUIDStringString() {
        
        // Parameters
        final OwnerUID      recOwner = REC_OWNER1;
        final String        strName = JavaRuntime.getQualifiedMethodNameSimple();
        final String        strDescr = JavaRuntime.getQualifiedMethodName();
        
        // Create request and verify
        DpCreateDatasetRequest  rqst = DpCreateDatasetRequest.from(recOwner, strName, strDescr);
        
        Assert.assertEquals(recOwner, rqst.getOwnerUid());
        Assert.assertEquals(strName, rqst.getName());
        Assert.assertEquals(strDescr, rqst.getDescription());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest#DpCreateDatasetRequest(com.ospreydcs.dp.api.common.OwnerUID, java.lang.String)}.
     */
    @Test
    public final void testDpCreateDatasetRequestOwnerUIDString() {
        
        // Parameters
        final OwnerUID      recOwner = REC_OWNER2;
        final String        strName = JavaRuntime.getQualifiedMethodNameSimple();
        
        // Create request and verify
        DpCreateDatasetRequest  rqst = new DpCreateDatasetRequest(recOwner, strName);
        
        Assert.assertEquals(recOwner, rqst.getOwnerUid());
        Assert.assertEquals(strName, rqst.getName());
        Assert.assertEquals(null, rqst.getDescription());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest#DpCreateDatasetRequest(com.ospreydcs.dp.api.common.OwnerUID, java.lang.String, java.lang.String)}.
     */
    @Test
    public final void testDpCreateDatasetRequestOwnerUIDStringString() {
        
        // Parameters
        final OwnerUID      recOwner = REC_OWNER2;
        final String        strName = JavaRuntime.getQualifiedMethodNameSimple();
        final String        strDescr = JavaRuntime.getQualifiedMethodName();
        
        // Create request and verify
        DpCreateDatasetRequest  rqst = new DpCreateDatasetRequest(recOwner, strName, strDescr);
        
        Assert.assertEquals(recOwner, rqst.getOwnerUid());
        Assert.assertEquals(strName, rqst.getName());
        Assert.assertEquals(strDescr, rqst.getDescription());
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest#getOwnerUid()}.
//     */
//    @Test
//    public final void testGetOwnerUid() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest#getName()}.
//     */
//    @Test
//    public final void testGetName() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest#getDescription()}.
//     */
//    @Test
//    public final void testGetDescription() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest#buildRequestMessage()}.
     */
    @Test
    public final void testBuildDataSetRequest() {
        
        // Parameters 
        final OwnerUID      recOwner = REC_OWNER1;
        final String        strName = JavaRuntime.getQualifiedMethodNameSimple();
        final String        strDescr = JavaRuntime.getQualifiedMethodName();
        
        final TimeInterval  tvlRng = TimeInterval.from(INS_INCEPT, INS_FINAL);
        final int           cntPvs = 10;
        final List<String>  lstPvNms = LST_PV_NMS.subList(0, cntPvs);
        
        // Create request and verify
        DpCreateDatasetRequest  rqst = new DpCreateDatasetRequest(recOwner, strName, strDescr);
        
        Assert.assertEquals(recOwner, rqst.getOwnerUid());
        Assert.assertEquals(strName, rqst.getName());
        Assert.assertEquals(strDescr, rqst.getDescription());
        
        // Add archive domain 
        rqst.addDomain(lstPvNms, tvlRng);
        
        // Create request message and verify
        CreateDataSetRequest    msgRqst = rqst.buildRequestMessage();
        DataSet                 msgDataset = msgRqst.getDataSet();
        
        Assert.assertEquals(recOwner.ownerUid(), msgDataset.getOwnerId());
        Assert.assertEquals(strName, msgDataset.getName());
        Assert.assertEquals(strDescr, msgDataset.getDescription());
        
        List<DataBlock>   lstMsgBlks = msgDataset.getDataBlocksList();
        
        Assert.assertTrue("Data blocks list not equal to 1", lstMsgBlks.size() == 1);
        
        DataBlock   msgBlk = lstMsgBlks.get(0);
        DpDataBlock blkOrg = rqst.getDataBlocks().get(0);
        DpDataBlock blkTest = ProtoMsg.toDataBlock(msgBlk);
        
        Assert.assertEquals(blkOrg, blkTest);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest#addDomain(java.util.List, java.time.Instant, java.time.Instant)}.
     */
    @Test
    public final void testAddDomainListOfStringInstantInstant() {
        
        // Parameters 
        final OwnerUID      recOwner = REC_OWNER1;
        final String        strName = JavaRuntime.getQualifiedMethodNameSimple();
        final String        strDescr = JavaRuntime.getQualifiedMethodName();
        
        final Instant       insBegin = INS_INCEPT;
        final Instant       insEnd = INS_FINAL;
        final int           cntPvs = 10;
        final List<String>  lstPvNms = LST_PV_NMS.subList(0, cntPvs);
        
        // Create request and verify
        DpCreateDatasetRequest  rqst = new DpCreateDatasetRequest(recOwner, strName, strDescr);
        
        Assert.assertEquals(recOwner, rqst.getOwnerUid());
        Assert.assertEquals(strName, rqst.getName());
        Assert.assertEquals(strDescr, rqst.getDescription());
        
        // Add archive domain and verify
        rqst.addDomain(lstPvNms, insBegin, insEnd);
        
        List<DpDataBlock>   lstBlks = rqst.getDataBlocks();
        
        Assert.assertEquals("Number of data blocks not equal to 1", 1, lstBlks.size());
        
        DpDataBlock blk = lstBlks.get(0);
        Assert.assertEquals(insBegin, blk.getTimeRange().begin());
        Assert.assertEquals(insEnd, blk.getTimeRange().end());
        Assert.assertEquals(lstPvNms, blk.getDataSources());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest#addDomain(java.util.List, com.ospreydcs.dp.api.common.TimeInterval)}.
     */
    @Test
    public final void testAddDomainListOfStringTimeInterval() {
        
        // Parameters 
        final OwnerUID      recOwner = REC_OWNER1;
        final String        strName = JavaRuntime.getQualifiedMethodNameSimple();
        final String        strDescr = JavaRuntime.getQualifiedMethodName();
        
        final Instant       insBegin = INS_INCEPT;
        final Instant       insEnd = INS_FINAL;
        final TimeInterval  tvlRng = TimeInterval.from(insBegin, insEnd);
        final int           cntPvs = 10;
        final List<String>  lstPvNms = LST_PV_NMS.subList(0, cntPvs);
        
        // Create request and verify
        DpCreateDatasetRequest  rqst = new DpCreateDatasetRequest(recOwner, strName, strDescr);
        
        Assert.assertEquals(recOwner, rqst.getOwnerUid());
        Assert.assertEquals(strName, rqst.getName());
        Assert.assertEquals(strDescr, rqst.getDescription());
        
        // Add archive domain and verify
        rqst.addDomain(lstPvNms, tvlRng);
        
        List<DpDataBlock>   lstBlks = rqst.getDataBlocks();
        
        Assert.assertEquals("Number of data blocks not equal to 1", 1, lstBlks.size());
        
        DpDataBlock blk = lstBlks.get(0);
        Assert.assertEquals(tvlRng, blk.getTimeRange());
        Assert.assertEquals(lstPvNms, blk.getDataSources());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest#addDomain(com.ospreydcs.dp.api.common.IDataTable)}.
     */
    @Test
    public final void testAddDomainIDataTable() {
        
        // Parameters
        final DpDataRequest     rqstData = RQST_SMALL;
        final List<String>      lstPvNms = rqstData.getSourceNames();
        final TimeInterval      tvlRng = rqstData.range();
        
        // Open a Query Service API connection and perform data request
        IDataTable      tblTest = null;
        try {
            IQueryService   apiQuery = DpQueryApiFactoryNew.connect();
            
            tblTest = apiQuery.queryData(rqstData);
            
            apiQuery.shutdown();
            
        } catch (DpGrpcException e) {
            Assert.fail("Query Service connection exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
            
        } catch (DpQueryException e) {
            Assert.fail("Query Service query exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
            
        } catch (InterruptedException e) {
            Assert.fail("Query Service shut down exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
            
        }
        
        // Parameters 
        final OwnerUID      recOwner = REC_OWNER1;
        final String        strName = JavaRuntime.getQualifiedMethodNameSimple();
        final String        strDescr = JavaRuntime.getQualifiedMethodName();
        
        // Create request and add the data table to the data set domain 
        DpCreateDatasetRequest  rqstDset = DpCreateDatasetRequest.from(recOwner, strName, strDescr);  
        rqstDset.addDomain(tblTest);
        
        // Recover data block and verify
        Assert.assertTrue("Create data set request did not have 1 data block", rqstDset.getDataBlocks().size() == 1);
        DpDataBlock blkTest = rqstDset.getDataBlocks().get(0);
        
        Assert.assertEquals(tvlRng, blkTest.getTimeRange());
        Assert.assertEquals(lstPvNms, blkTest.getDataSources());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.annotate.DpCreateDatasetRequest#addBlock(com.ospreydcs.dp.api.annotate.model.DpDataBlock)}.
     */
    @Test
    public final void testAddBlock() {
        
        // Parameters 
        final OwnerUID      recOwner = REC_OWNER1;
        final String        strName = JavaRuntime.getQualifiedMethodNameSimple();
        final String        strDescr = JavaRuntime.getQualifiedMethodName();
        
        final Instant       insBegin = INS_INCEPT;
        final Instant       insEnd = INS_FINAL;
        final TimeInterval  tvlRng = TimeInterval.from(insBegin, insEnd);
        final int           cntPvs = 10;
        final List<String>  lstPvNms = LST_PV_NMS.subList(0, cntPvs);
        
        final DpDataBlock   blkTest = DpDataBlock.from(lstPvNms, tvlRng);
        
        // Create request and verify
        DpCreateDatasetRequest  rqst = new DpCreateDatasetRequest(recOwner, strName, strDescr);
        
        Assert.assertEquals(recOwner, rqst.getOwnerUid());
        Assert.assertEquals(strName, rqst.getName());
        Assert.assertEquals(strDescr, rqst.getDescription());
        
        // Add archive domain and verify
        rqst.addBlock(blkTest);
        
        List<DpDataBlock>   lstBlks = rqst.getDataBlocks();
        
        Assert.assertEquals("Number of data blocks not equal to 1", 1, lstBlks.size());
        
        DpDataBlock blkRcvr = lstBlks.get(0);
        
        Assert.assertEquals(blkTest, blkRcvr);
    }

}
