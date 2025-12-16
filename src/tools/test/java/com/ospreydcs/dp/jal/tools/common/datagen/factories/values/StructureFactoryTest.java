/*
 * Project: dp-jal
 * File:	StructureFactoryTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.values
 * Type: 	StructureFactoryTest
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
 * @since Nov 14, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.values;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ScalarFactorySpec;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.StructureIndexGenerator;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.StructureIndexGenerator.NodeSet;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.StructureUtility;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.StructureUtility.IndexValuePair;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.StructureUtility.NameValuePair;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.values.JalToolsStructFactoryConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>StructureFactory</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 14, 2025
 *
 */
public class StructureFactoryTest {

    
    //
    // Tools Library Resources
    //
    
    /** The default parameters for structure value generation */
    public static final JalToolsStructFactoryConfig  CFG_DEF = JalToolsConfig.getInstance().datagen.values.structure;
    
    
    //
    // Test Resources
    //
    
    /** The default value structure unique field name enable/disable flag */
    public static final boolean                 BOL_UNIQ_FLD_NM_ENBL = CFG_DEF.fieldNames.unique.enabled;
    
    
    /** String prefix used for string-value generation */
    public static final String                  STR_PREFIX = "str:";
    

    /** Configuration for a unit increment string-value scalar factory */
    public static final ScalarFactorySpec     REC_CFG_STR_1 = ScalarFactorySpec.from(JalScalarType.STRING, false, 0, Integer.valueOf(1), STR_PREFIX);
    
    /** Configuration for an incremental boolean-value scalar factory */
    public static final ScalarFactorySpec     REC_CFG_BOL_1 = ScalarFactorySpec.from(JalScalarType.BOOLEAN, false, 0, Integer.valueOf(1));
    
    /** Configuration for an incremental boolean-value scalar factory */
    public static final ScalarFactorySpec     REC_CFG_BOL_RND = ScalarFactorySpec.from(JalScalarType.BOOLEAN, true, 0, Integer.valueOf(0));
    
    /** Configuration for a unit increment integer-value scalar factory */
    public static final ScalarFactorySpec     REC_CFG_INT_1 = ScalarFactorySpec.from(JalScalarType.INTEGER, false, 0, Integer.valueOf(1));
    
    /** Configuration for a 2 increment integer-value scalar factory */
    public static final ScalarFactorySpec     REC_CFG_INT_2 = ScalarFactorySpec.from(JalScalarType.INTEGER, false, 0, Integer.valueOf(2));
    
    /** Configuration for an incremental double-value scalar factory */
    public static final ScalarFactorySpec     REC_CFG_DBL_1 = ScalarFactorySpec.from(JalScalarType.DOUBLE, false, 0, Double.valueOf(1.602e-19));
    
    /** Configuration for a random double-value scalar factory */
    public static final ScalarFactorySpec     REC_CFG_DBL_RND = ScalarFactorySpec.from(JalScalarType.DOUBLE, true, 0);
    

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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#from(int, int, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ScalarFactorySpec)}.
     */
    @Test
    public final void testFromIntIntScalarFactoryConfigIllegal() {
        
        // Test Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_STR_1;
        final ScalarFactory       facVals = recCfg.newFactory();
        
        final int depthGood = 3;
        final int fanoutGood = 5;
        final int depthBad = 0;
        final int fanoutBad = 0;
        
        try {
            @SuppressWarnings("unused")
            StructureFactory    facStruct = StructureFactory.from(depthBad, fanoutGood, facVals);
            Assert.fail("StructureFactory creator illegal depth did not throw exception.");
            
        } catch (IllegalArgumentException e) {
            // Should throw exception
        }
        
        try {
            @SuppressWarnings("unused")
            StructureFactory    facStruct = StructureFactory.from(depthGood, fanoutBad, facVals);
            Assert.fail("StructureFactory creator illegal fanout did not throw exception.");
            
        } catch (IllegalArgumentException e) {
            // Should throw exception
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#from(int, int, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ScalarFactorySpec)}.
     */
    @Test
    public final void testFromIntIntScalarFactoryConfig() {
        
        // Test Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_BOL_1;
        final ScalarFactory       facVals = recCfg.newFactory();
        
        final int           cntDepth = 1;
        final int           cntFanout = 5;
        final boolean       bolUniqNms = BOL_UNIQ_FLD_NM_ENBL;
        
        StructureFactory facTest = StructureFactory.from(cntDepth, cntFanout, facVals);
        
        Assert.assertEquals(recCfg.bolRandEnbl(), facTest.isRandomValued());
        Assert.assertEquals(bolUniqNms, facTest.isUniqueFieldNamed());
        
        Assert.assertEquals(cntDepth, facTest.getDepth());
        Assert.assertEquals(cntFanout, facTest.getFanout());
        Assert.assertEquals(recCfg.enmType(), facTest.getScalarType());
        Assert.assertEquals(recCfg.lngSeed(), facTest.getSeed());
    }
        
    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#from(int, int, boolean, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ScalarFactorySpec)}.
     */
    @Test
    public final void testFromIntIntBooleanScalarFactoryConfig() {
        
        // Test Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_DBL_RND;
        final ScalarFactory       facVals = recCfg.newFactory();
        
        final int           cntDepth = 1;
        final int           cntFanout = 5;
        final boolean       bolUniqNms = true;
        
        StructureFactory facTest = StructureFactory.from(cntDepth, cntFanout, bolUniqNms, facVals);
        
        Assert.assertEquals(recCfg.bolRandEnbl(), facTest.isRandomValued());
        Assert.assertEquals(bolUniqNms, facTest.isUniqueFieldNamed());
        
        Assert.assertEquals(cntDepth, facTest.getDepth());
        Assert.assertEquals(cntFanout, facTest.getFanout());
        Assert.assertEquals(recCfg.enmType(), facTest.getScalarType());
        Assert.assertEquals(recCfg.lngSeed(), facTest.getSeed());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#StructureFactory(int, int, boolean, com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactory)}.
     */
    @Test
    public final void testStructureFactory() {
        // Test Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_INT_1;
        final ScalarFactory         facVals = recCfg.newFactory();
        
        final int           cntDepth = 1;
        final int           cntFanout = 5;
        final boolean       bolUniqNms = true;
        
        StructureFactory facTest = new StructureFactory(cntDepth, cntFanout, bolUniqNms, facVals);
        
        Assert.assertEquals(recCfg.bolRandEnbl(), facTest.isRandomValued());
        Assert.assertEquals(bolUniqNms, facTest.isUniqueFieldNamed());
        
        Assert.assertEquals(cntDepth, facTest.getDepth());
        Assert.assertEquals(cntFanout, facTest.getFanout());
        Assert.assertEquals(recCfg.enmType(), facTest.getScalarType());
        Assert.assertEquals(recCfg.lngSeed(), facTest.getSeed());
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#hasRandomValues()}.
//     */
//    @Test
//    public final void testHasRandomValues() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#hasUniqueFieldNames()}.
//     */
//    @Test
//    public final void testHasUniqueFieldNames() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#getDepth()}.
//     */
//    @Test
//    public final void testGetDepth() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#getFanout()}.
//     */
//    @Test
//    public final void testGetFanout() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#getType()}.
//     */
//    @Test
//    public final void testGetType() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#getSeed()}.
//     */
//    @Test
//    public final void testGetSeed() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#nextDatum()}.
     */
    @Test
    public final void testNextValue1_firstElement() {
        
        // Test Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_INT_1;
        final ScalarFactory       facVals = recCfg.newFactory();
        
        final int           cntDepth = 1;
        final int           cntFanout = 5;
        final boolean       bolUniqFldNms = false;

        final JalScalarType enmType = recCfg.enmType();
        
        StructureFactory    facTest = StructureFactory.from(cntDepth, cntFanout, bolUniqFldNms, facVals);

        Object  objStruct = facTest.nextDatum();
        
        int cntTermFlds = this.computeNumTerminalFields(cntDepth, cntFanout);
        int cntNodes = this.computeNumNodesTotal(cntDepth, cntFanout);

        Assert.assertEquals(cntTermFlds, StructureUtility.terminalNodeCount(objStruct));
        Assert.assertEquals(cntNodes, StructureUtility.totalNodeCount(objStruct));
        
        Assert.assertTrue(StructureUtility.isTerminalValueOfType(objStruct, enmType));
        
        Object  objElem = StructureUtility.extractFirstTerminalValue(objStruct);
        Assert.assertTrue(enmType.isAssignable(objElem));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#nextDatum()}.
     */
    @Test
    public final void testNextValue2_extractFieldNames() {
        
        // Test Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_INT_2;
        final ScalarFactory       facVals = recCfg.newFactory();
        
        final int           cntDepth = 2;
        final int           cntFanout = 3;
        final JalScalarType enmType = recCfg.enmType();
        
        StructureFactory facTest = StructureFactory.from(cntDepth, cntFanout, facVals);

        Object  objStruct = facTest.nextDatum();
        
        int cntTermFlds = this.computeNumTerminalFields(cntDepth, cntFanout);
        int cntNodes = this.computeNumNodesTotal(cntDepth, cntFanout);

        Assert.assertEquals(cntTermFlds, StructureUtility.terminalNodeCount(objStruct));
        Assert.assertEquals(cntNodes, StructureUtility.totalNodeCount(objStruct));
        
        Assert.assertTrue(StructureUtility.isTerminalValueOfType(objStruct, enmType));
        
        Object  objElem = StructureUtility.extractFirstTerminalValue(objStruct);
        Assert.assertTrue(enmType.isAssignable(objElem));

        // Write out the field names
        this.writeOutFieldNames(cntDepth, cntFanout, objStruct);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#nextDatum()}.
     */
    @Test
    public final void testNextValue3_extractFieldNamesUnique() {
        
        // Test Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_BOL_1;
        final ScalarFactory       facVals = recCfg.newFactory();
        
        final int           cntDepth = 3;
        final int           cntFanout = 2;
        final boolean       bolUniqFldNms = true;
        
        final JalScalarType enmType = recCfg.enmType();
        
        StructureFactory facTest = StructureFactory.from(cntDepth, cntFanout, bolUniqFldNms, facVals);

        Object  objStruct = facTest.nextDatum();
        
        int cntTermFlds = this.computeNumTerminalFields(cntDepth, cntFanout);
        int cntNodes = this.computeNumNodesTotal(cntDepth, cntFanout);

        Assert.assertEquals(cntTermFlds, StructureUtility.terminalNodeCount(objStruct));
        Assert.assertEquals(cntNodes, StructureUtility.totalNodeCount(objStruct));
        
        Assert.assertTrue(StructureUtility.isTerminalValueOfType(objStruct, enmType));
        
        Object  objElem = StructureUtility.extractFirstTerminalValue(objStruct);
        Assert.assertTrue(enmType.isAssignable(objElem));

        // Write out the field names
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        
        this.writeOutFieldNames(cntDepth, cntFanout, objStruct);
        
        // Do it again
        objStruct = facTest.nextDatum();
        this.writeOutFieldNames(cntDepth, cntFanout, objStruct);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#nextDatum()}.
     */
    @Test
    public final void testNextValue4_extractValueAt() {
        
        // Test Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_DBL_1;
        final ScalarFactory       facVals = recCfg.newFactory();
        
        final int           cntDepth = 3;
        final int           cntFanout = 2;
        final JalScalarType enmType = recCfg.enmType();
        
        StructureFactory facTest = StructureFactory.from(cntDepth, cntFanout, facVals);

        Object  objStruct = facTest.nextDatum();
        
        int cntTermFlds = this.computeNumTerminalFields(cntDepth, cntFanout);
        int cntNodes = this.computeNumNodesTotal(cntDepth, cntFanout);

        Assert.assertEquals(cntTermFlds, StructureUtility.terminalNodeCount(objStruct));
        Assert.assertEquals(cntNodes, StructureUtility.totalNodeCount(objStruct));
        
        Assert.assertTrue(StructureUtility.isTerminalValueOfType(objStruct, enmType));
        
        Object  objElem = StructureUtility.extractFirstTerminalValue(objStruct);
        Assert.assertTrue(enmType.isAssignable(objElem));

        // Create indexes according to structure parameters
        List<Integer>       lstIndex1 = new LinkedList<>();
        List<Integer>       lstIndex2 = new LinkedList<>();
        
        for (int iLvl=0; iLvl<cntDepth; iLvl++) {
            lstIndex1.add(0);
            lstIndex2.add(cntFanout - 1);
        }
        
        Object  objNode1 = StructureUtility.extractNodeAt(lstIndex1, objStruct);
        Object  objNode2 = StructureUtility.extractNodeAt(lstIndex2, objStruct);
        
        Assert.assertTrue(enmType.isAssignable(objNode1));
        Assert.assertTrue(enmType.isAssignable(objNode2));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#nextDatum()}.
     */
    @Test
    public final void testNextValue5_extractTerminalValuesWithIndexMap() {
        
        // Test Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_BOL_1;
        final ScalarFactory       facVals = recCfg.newFactory();
        
        final int           cntDepth = 3;
        final int           cntFanout = 2;
        final boolean       bolUniqFldNms = false;
        final JalScalarType enmType = recCfg.enmType();
        
        StructureFactory    facTest = StructureFactory.from(cntDepth, cntFanout, bolUniqFldNms, facVals);

        Object  objStruct = facTest.nextDatum();
        
        int cntTermFlds = this.computeNumTerminalFields(cntDepth, cntFanout);
        int cntNodes = this.computeNumNodesTotal(cntDepth, cntFanout);

        Assert.assertEquals(cntTermFlds, StructureUtility.terminalNodeCount(objStruct));
        Assert.assertEquals(cntNodes, StructureUtility.totalNodeCount(objStruct));
        
        Assert.assertTrue(StructureUtility.isTerminalValueOfType(objStruct, enmType));
        
        Object  objElem = StructureUtility.extractFirstTerminalValue(objStruct);
        Assert.assertTrue(enmType.isAssignable(objElem));

        Map<List<Integer>, Object>  mapValues = StructureUtility.extractTerminalValuesWithIndexAsMap(objStruct);
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Structure: depth=" + cntDepth + ", fanout=" + cntFanout);
        System.out.println("  values: " + mapValues);
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#nextDatum()}.
     */
    @Test
    public final void testNextValue6_extractTerminalValuesWithNameAsMap() {
        
        // Test Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_STR_1;
        final ScalarFactory       facVals = recCfg.newFactory();
        
        final int           cntDepth = 3;
        final int           cntFanout = 2;
        final boolean       bolUniqFldNms = true;
        final JalScalarType enmType = recCfg.enmType();
        
        StructureFactory facTest = new StructureFactory(cntDepth, cntFanout, bolUniqFldNms, facVals);

        Object  objStruct = facTest.nextDatum();
        
        int cntTermFlds = this.computeNumTerminalFields(cntDepth, cntFanout);
        int cntNodes = this.computeNumNodesTotal(cntDepth, cntFanout);

        Assert.assertEquals(cntTermFlds, StructureUtility.terminalNodeCount(objStruct));
        Assert.assertEquals(cntNodes, StructureUtility.totalNodeCount(objStruct));
        
        Assert.assertTrue(StructureUtility.isTerminalValueOfType(objStruct, enmType));
        
        Object  objElem = StructureUtility.extractFirstTerminalValue(objStruct);
        Assert.assertTrue(enmType.isAssignable(objElem));

        Map<String, Object>  mapValues1 = StructureUtility.extractTerminalValuesWithNameAsMap(objStruct);
        Map<String, Object>  mapValues2 = StructureUtility.extractTerminalValuesWithNameAsMap(facTest.nextDatum());
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Structure: depth=" + cntDepth + ", fanout=" + cntFanout);
        System.out.println("  S0 values: " + mapValues1);
        System.out.println("  S1 values: " + mapValues2);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#nextDatum()}.
     */
    @Test
    public final void testNextValue7_extractTerminalValuesWithIndex() {
        
        // Test Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_DBL_1; 
        final ScalarFactory       facVals = recCfg.newFactory();

        final int           cntDepth = 3;
        final int           cntFanout = 2;
        final boolean       bolUniqFldNms = false;
        final JalScalarType enmType = recCfg.enmType();
        
        StructureFactory facTest = StructureFactory.from(cntDepth, cntFanout, bolUniqFldNms, facVals);

        Object  objStruct = facTest.nextDatum();
        
        int cntTermFlds = this.computeNumTerminalFields(cntDepth, cntFanout);
        int cntNodes = this.computeNumNodesTotal(cntDepth, cntFanout);

        Assert.assertEquals(cntTermFlds, StructureUtility.terminalNodeCount(objStruct));
        Assert.assertEquals(cntNodes, StructureUtility.totalNodeCount(objStruct));
        
        Assert.assertTrue(StructureUtility.isTerminalValueOfType(objStruct, enmType));
        
        Object  objElem = StructureUtility.extractFirstTerminalValue(objStruct);
        Assert.assertTrue(enmType.isAssignable(objElem));

        @SuppressWarnings("unchecked")
        List<IndexValuePair>  lstIndValPairs = StructureUtility.extractTerminalValuesWithIndex((Map<String, Object>)objStruct);
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Structure: depth=" + cntDepth + ", fanout=" + cntFanout);
        System.out.println("  values: " + lstIndValPairs);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#nextDatum()}.
     */
    @Test
    public final void testNextValue8_extractTerminalValuesWithName() {
        
        // Test Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_INT_1;
        final ScalarFactory       facVals = recCfg.newFactory();

        final int           cntDepth = 3;
        final int           cntFanout = 2;
        final boolean       bolUniqFldNms = false;
        final JalScalarType enmType = recCfg.enmType();
        
        StructureFactory    facTest = StructureFactory.from(cntDepth, cntFanout, bolUniqFldNms, facVals);

        Object  objStruct = facTest.nextDatum();
        
        int cntTermFlds = this.computeNumTerminalFields(cntDepth, cntFanout);
        int cntNodes = this.computeNumNodesTotal(cntDepth, cntFanout);

        Assert.assertEquals(cntTermFlds, StructureUtility.terminalNodeCount(objStruct));
        Assert.assertEquals(cntNodes, StructureUtility.totalNodeCount(objStruct));
        
        Assert.assertTrue(StructureUtility.isTerminalValueOfType(objStruct, enmType));
        
        Object  objElem = StructureUtility.extractFirstTerminalValue(objStruct);
        Assert.assertTrue(enmType.isAssignable(objElem));

        @SuppressWarnings("unchecked")
        List<NameValuePair> lstNmValPairs = StructureUtility.extractTerminalValuesWithName((Map<String, Object>) objStruct);
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("  Structure: depth=" + cntDepth + ", fanout=" + cntFanout);
        System.out.println("  values: " + lstNmValPairs);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#nextDatum()}.
     */
    @Test
    public final void testNextValue9_extractNodeWithName() {
        
        // Test Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_INT_2;
        final ScalarFactory       facVals = recCfg.newFactory();
        
        final int           cntDepth = 3;
        final int           cntFanout = 2;
        final boolean       bolUniqFldNms = false;
        final JalScalarType enmType = recCfg.enmType();
        
        StructureFactory facTest = StructureFactory.from(cntDepth, cntFanout, bolUniqFldNms, facVals);

        Object  objStruct = facTest.nextDatum();
        
        int cntTermFlds = this.computeNumTerminalFields(cntDepth, cntFanout);
        int cntNodes = this.computeNumNodesTotal(cntDepth, cntFanout);

        Assert.assertEquals(cntTermFlds, StructureUtility.terminalNodeCount(objStruct));
        Assert.assertEquals(cntNodes, StructureUtility.totalNodeCount(objStruct));
        
        Assert.assertTrue(StructureUtility.isTerminalValueOfType(objStruct, enmType));
        
        Object  objElem = StructureUtility.extractFirstTerminalValue(objStruct);
        Assert.assertTrue(enmType.isAssignable(objElem));

        @SuppressWarnings("unchecked")
        List<NameValuePair> lstNmValPairs = StructureUtility.extractTerminalValuesWithName((Map<String, Object>) objStruct);

        for (NameValuePair recPair : lstNmValPairs) {
            String  strName = recPair.name();
            Object  objValue = recPair.value();
            
            Object  objValExtract = StructureUtility.extractNodeWithName(strName, objStruct);
            
            Assert.assertEquals(objValue, objValExtract);
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory#nextDatum()}.
     */
    @Test
    public final void testNextValue10_extractNodeByIndex() {
        
        // Test Parameters
        final ScalarFactorySpec   recCfg = REC_CFG_INT_2; 
        final ScalarFactory       facVals = recCfg.newFactory();

        final int           cntDepth = 3;
        final int           cntFanout = 2;
        final boolean       bolUniqFldNms = false;
        NodeSet             enmNodeSet = NodeSet.TERMINAL;
        final JalScalarType enmType = recCfg.enmType();
        
        // Create the structure generator
        StructureFactory facTest = StructureFactory.from(cntDepth, cntFanout, bolUniqFldNms, facVals);
        
        // Create the index generator
        StructureIndexGenerator genIndexes = new StructureIndexGenerator(cntDepth, cntFanout, enmNodeSet);

        Object  objStruct = facTest.nextDatum();
        
        // Iterate through selected index set
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());;
        System.out.println("  Structure: depth=" + cntDepth + ", fanout=" + cntFanout);
        System.out.print("  node set " + enmNodeSet + ": ");
        for (List<Integer> lstIndex : genIndexes) {
            Object      objVal = StructureUtility.extractNodeAt(lstIndex, objStruct);
            Assert.assertTrue(enmType.isAssignable(objVal));
            
            System.out.print(lstIndex + "=" + objVal.toString() + ", ");
        }
        System.out.print("\n");
        
        // Reset and do it again
        objStruct = facTest.nextDatum();
        genIndexes.resetIndexIteration();
        System.out.print("  node set " + enmNodeSet + ": ");
        while(genIndexes.hasNext()) {
            List<Integer>   lstIndex = genIndexes.nextIndex();
            Object          objVal = StructureUtility.extractNodeAt(lstIndex, objStruct);
            Assert.assertTrue(enmType.isAssignable(objVal));
            
            System.out.print(lstIndex + "=" + objVal.toString() + ", ");
        }
        System.out.print("\n");
            
        // Reset and check internal nodes
        enmNodeSet = NodeSet.INTERNAL;
        objStruct = facTest.nextDatum();
        genIndexes.resetIndexIteration(enmNodeSet);
        System.out.print("  node set " + enmNodeSet + ": ");
        for (List<Integer> lstIndex : genIndexes) {
            Object      objVal = StructureUtility.extractNodeAt(lstIndex, objStruct);
            Assert.assertTrue(objVal instanceof Map);
            
            System.out.print(lstIndex + "=" + objVal.toString() + ", ");
        }
        System.out.print("\n");
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Writes out the field names of the given structure (as object) to the standard output.
     * </p>
     * 
     * @param intDepth  depth of the given structure
     * @param intFanout fan out of the given structure
     * @param objStruct tree structure as Java <code>Object</code>
     */
    private void writeOutFieldNames(int intDepth, int intFanout, Object objStruct) {
        
        List<List<String>>  lstLstFldNms = StructureUtility.extractFieldNames(objStruct);
        System.out.println("  Structure: depth=" + intDepth + ", fanout=" + intFanout);
        for (List<String> lstLvlNms : lstLstFldNms) 
            System.out.println("  " + lstLvlNms);
    }
    
    /**
     * <p>
     * Computes the number of terminal nodes <i>n</i> of a symmetric tree structure according to the 
     * formula
     * <pre>
     *   <i>n</i> = <i>F</i><sup><i>D</i></sup>
     * </pre>
     * where <i>D</i> = <code>cntDepth</code> and <i>F</i> = <code>Fanout</code>.
     * </p>
     *  
     * @param cntDepth  tree structure depth
     * @param cntFanout structure node fan out (number of sub-nodes per node)
     * 
     * @return  number of terminal fields for symmetric tree structure with given properties
     */
    private int computeNumTerminalFields(int cntDepth, int cntFanout) {
        int     cntFlds = 1;
        
        for (int i=0; i<cntDepth; i++)
            cntFlds *= cntFanout;
        
        return cntFlds;
    }
    
    /**
     * <p>
     * Computes the total number of nodes <i>N</i> within a symmetric tree structure according to the
     * formula
     * <pre/>
     *   <i>N</i> = (<i>F</i><sup><i>D</i>+1</sup> - 1) / (<i>F</i> - 1)
     * </pre>
     * where <i>D</i> = <code>cntDepth</code> and <i>F</i> = <code>Fanout</code>.
     * </p>
     * 
     * @param cntDepth  tree structure depth
     * @param cntFanout structure node fan out (number of sub-nodes per node)
     * 
     * @return  total number of nodes within a symmetric tree structure with given properties
     */
    private int computeNumNodesTotal(int cntDepth, int cntFanout) {
        int     cntNodes = cntFanout * this.computeNumTerminalFields(cntDepth, cntFanout) - 1;
        
        cntNodes = cntNodes / (cntFanout - 1);
        
        return cntNodes;
    }
}
