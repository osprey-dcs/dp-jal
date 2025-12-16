/*
 * Project: dp-jal
 * File:	StructureFactorySpecTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.specs
 * Type: 	StructureFactorySpecTest
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
 * @since Dec 15, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.StructureIndexGenerator;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.StructureUtility;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.values.JalToolsStructFactoryConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for record <code>StructureFactorySpec</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 15, 2025
 *
 */
public class StructureFactorySpecTest {

    
    //
    // Test Constants
    //
    
    /** Tree node depth for parsing test */
    public static final Integer         INT_TREE_DEPTH_PARSE = 6;
    
    /** Tree node depth for parsing test */
    public static final Integer         INT_TREE_FANOUT_PARSE = 3;
    
    /** Unique field names enable/disable flag for parsing test */
    public static final Boolean         BOL_FLD_NMS_UNIQ_ENBL_PARSE = true;
    
    /** String prefix used for string-value generation  */
    public static final String          STR_PREFIX_1 = "str:";
    
    /** Simulated command-line argument collection for specification parsing test 1 */
    public static final String[]        ARR_STR_ARGS_PARSE_1 = { 
                                            INT_TREE_DEPTH_PARSE.toString(), 
                                            INT_TREE_FANOUT_PARSE.toString(), 
                                            BOL_FLD_NMS_UNIQ_ENBL_PARSE.toString(), 
                                            "STRING", "true", "23", "42", STR_PREFIX_1 }; 

    /** Simulated command-line argument collection for specification parsing test 2 */
    public static final String[]        ARR_STR_ARGS_PARSE_2 = { 
                                            INT_TREE_DEPTH_PARSE.toString(), 
                                            INT_TREE_FANOUT_PARSE.toString(), 
                                            BOL_FLD_NMS_UNIQ_ENBL_PARSE.toString(), 
                                            "BOOLEAN" }; 
    
    
    //
    // Tools Library Resources
    //
    
    /** The default parameters for structure value generation */
    public static final JalToolsStructFactoryConfig  CFG_DEF = JalToolsConfig.getInstance().datagen.values.structure;
    
    
    //
    // StructureFactorySpec Default Values
    //
    
    /** The default tree structure node depth */
    public static final int                    INT_TREE_DEPTH_DEF = CFG_DEF.tree.depth;
    
    /** The default tree structure node fanout */
    public static final int                     INT_TREE_FANOUT_DEF = CFG_DEF.tree.fanout;
    
    
    /** The default value structure unique field name enable/disable flag */
    public static final boolean                 BOL_FLD_NMS_UNIQ_ENBL_DEF = CFG_DEF.fieldNames.unique.enabled;
    
    /** The default scalar factory field value type */
    public static final JalScalarType           ENM_FLD_VALS_TYPE_DEF = StructureFactorySpec.ENM_FLD_VALS_TYPE_DEF;
    
    /** The default scalar factory field values random enable/disable flag */
    public static final boolean                 BOL_FLD_VALS_RAND_ENBL_DEF = StructureFactorySpec.BOL_FLD_VALS_RAND_ENBL_DEF;
    
    /** The default scalar factory field values random seed */
    public static final long                    LNG_FLD_VALS_RAND_SEED_DEF = StructureFactorySpec.LNG_FLD_VALS_RAND_SEED_DEF;

    
    // 
    // Test Resources
    //
    
    /** The default scalar factory specification used by <code>StructureFactorySpec</code> */
    public static final ScalarFactorySpec       REC_SCAL_SPEC_STRUCT_DEF = (BOL_FLD_VALS_RAND_ENBL_DEF) 
                                                                   ? ScalarFactorySpec.from(ENM_FLD_VALS_TYPE_DEF, BOL_FLD_VALS_RAND_ENBL_DEF, LNG_FLD_VALS_RAND_SEED_DEF) 
                                                                   : ScalarFactorySpec.from(ENM_FLD_VALS_TYPE_DEF, BOL_FLD_VALS_RAND_ENBL_DEF);
    
    /** The default scalar factory specification with random number generation enabled */
    public static final ScalarFactorySpec       REC_SCAL_SPEC_RAND_ENBL = ScalarFactorySpec.from(ENM_FLD_VALS_TYPE_DEF, true, LNG_FLD_VALS_RAND_SEED_DEF);
    
    /** Scalar factory specification for parsing string {@link #ARR_STR_ARGS_PARSE_1} */
    public static final ScalarFactorySpec       REC_SCAL_SPEC_PARSE_1 = ScalarFactorySpec.from(JalScalarType.STRING, true, 23, Integer.valueOf(42), STR_PREFIX_1);
    
    /** The default scalar factory specification used by <code>StructureFactorySpec</code> */
    public static final ScalarFactorySpec       REC_SCAL_SPEC_PARSE_2 = (BOL_FLD_VALS_RAND_ENBL_DEF) 
                                                                   ? ScalarFactorySpec.from(JalScalarType.BOOLEAN, BOL_FLD_VALS_RAND_ENBL_DEF, LNG_FLD_VALS_RAND_SEED_DEF) 
                                                                   : ScalarFactorySpec.from(JalScalarType.BOOLEAN, BOL_FLD_VALS_RAND_ENBL_DEF);
    
    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#from()}.
     */
    @Test
    public final void testFrom() {
        
        // Test Parameters
        final ScalarFactorySpec specScalFac = REC_SCAL_SPEC_STRUCT_DEF;
        
        final int       intDepth = INT_TREE_DEPTH_DEF;
        final int       intFanout = INT_TREE_FANOUT_DEF;
        final boolean   bolUniqFldNms = BOL_FLD_NMS_UNIQ_ENBL_DEF;
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.from();
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
        
        // Print out default configuration
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("Default StructureFactorySpec:");
        System.out.println(specTest);   // Tests toString()
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#from(int, int)}.
     */
    @Test
    public final void testFromIntInt() {
        
        // Test Parameters
        final ScalarFactorySpec specScalFac = REC_SCAL_SPEC_STRUCT_DEF;
        
        final int       intDepth = 5;
        final int       intFanout = 4;
        final boolean   bolUniqFldNms = BOL_FLD_NMS_UNIQ_ENBL_DEF;
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.from(intDepth, intFanout);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#from(int, int, boolean)}.
     */
    @Test
    public final void testFromIntIntBoolean() {
        
        // Test Parameters
        final ScalarFactorySpec specScalFac = REC_SCAL_SPEC_STRUCT_DEF;
        
        final int       intDepth = 1;
        final int       intFanout = 10;
        final boolean   bolUniqFldNms = !BOL_FLD_NMS_UNIQ_ENBL_DEF;
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.from(intDepth, intFanout, bolUniqFldNms);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#from(int, int, com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType)}.
     */
    @Test
    public final void testFromIntIntJalScalarType() {
        
        // Test Parameters
        final JalScalarType     enmType = JalScalarType.STRING;
        final ScalarFactorySpec specScalFac = ScalarFactorySpec.from(enmType, BOL_FLD_VALS_RAND_ENBL_DEF, LNG_FLD_VALS_RAND_SEED_DEF);
        
        final int       intDepth = 1;
        final int       intFanout = 10;
        final boolean   bolUniqFldNms = BOL_FLD_NMS_UNIQ_ENBL_DEF;
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.from(intDepth, intFanout, enmType);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#from(int, int, boolean, com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType)}.
     */
    @Test
    public final void testFromIntIntBooleanJalScalarType() {
        
        // Test Parameters
        final JalScalarType     enmType = JalScalarType.STRING;
        final ScalarFactorySpec specScalFac = ScalarFactorySpec.from(enmType, BOL_FLD_VALS_RAND_ENBL_DEF, LNG_FLD_VALS_RAND_SEED_DEF);
        
        final int       intDepth = 5;
        final int       intFanout = 2;
        final boolean   bolUniqFldNms = !BOL_FLD_NMS_UNIQ_ENBL_DEF;
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.from(intDepth, intFanout, bolUniqFldNms, enmType);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#from(int, int, boolean, com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean)}.
     */
    @Test
    public final void testFromIntIntBooleanJalScalarTypeBoolean() {
        
        // Test Parameters
        final JalScalarType     enmType = JalScalarType.STRING;
        final boolean           bolRandEnbl = true;
        final ScalarFactorySpec specScalFac = ScalarFactorySpec.from(enmType, bolRandEnbl, LNG_FLD_VALS_RAND_SEED_DEF);
        
        final int       intDepth = 5;
        final int       intFanout = 2;
        final boolean   bolUniqFldNms = !BOL_FLD_NMS_UNIQ_ENBL_DEF;
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.from(intDepth, intFanout, bolUniqFldNms, enmType, bolRandEnbl);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#from(int, int, boolean, com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean, long)}.
     */
    @Test
    public final void testFromIntIntBooleanJalScalarTypeBooleanLong() {
        
        // Test Parameters
        final JalScalarType     enmType = JalScalarType.STRING;
        final boolean           bolRandEnbl = true;
        final long              lngSeed = 42;
        final ScalarFactorySpec specScalFac = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed);
        
        final int       intDepth = 5;
        final int       intFanout = 2;
        final boolean   bolUniqFldNms = !BOL_FLD_NMS_UNIQ_ENBL_DEF;
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.from(intDepth, intFanout, bolUniqFldNms, enmType, bolRandEnbl, lngSeed);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#from(int, int, boolean, com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean, long, java.lang.Number)}.
     */
    @Test
    public final void testFromIntIntBooleanJalScalarTypeBooleanLongNumber() {
        
        // Test Parameters
        final JalScalarType     enmType = JalScalarType.FLOAT;
        final boolean           bolRandEnbl = true;
        final long              lngSeed = 42;
        final float             fltIncr = 1.23e-11f;
        final ScalarFactorySpec specScalFac = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed, fltIncr);
        
        final int       intDepth = 5;
        final int       intFanout = 2;
        final boolean   bolUniqFldNms = !BOL_FLD_NMS_UNIQ_ENBL_DEF;
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.from(intDepth, intFanout, bolUniqFldNms, enmType, bolRandEnbl, lngSeed, fltIncr);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#from(int, int, boolean, com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType, boolean, long, java.lang.Number, java.lang.String)}.
     */
    @Test
    public final void testFromIntIntBooleanJalScalarTypeBooleanLongNumberString() {
        
        // Test Parameters
        final JalScalarType     enmType = JalScalarType.FLOAT;
        final boolean           bolRandEnbl = true;
        final long              lngSeed = 42;
        final float             fltIncr = 1.23e-11f;
        final String            strPrefix = "Happy String";
        final ScalarFactorySpec specScalFac = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed, fltIncr, strPrefix);
        
        final int       intDepth = 5;
        final int       intFanout = 2;
        final boolean   bolUniqFldNms = !BOL_FLD_NMS_UNIQ_ENBL_DEF;
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.from(intDepth, intFanout, bolUniqFldNms, enmType, bolRandEnbl, lngSeed, fltIncr, strPrefix);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#from(int, int, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ScalarFactorySpec)}.
     */
    @Test
    public final void testFromIntIntScalarFactorySpec() {
        
        // Test Parameters
        final JalScalarType     enmType = JalScalarType.FLOAT;
        final boolean           bolRandEnbl = true;
        final long              lngSeed = 42;
        final float             fltIncr = 1.23e-11f;
        final String            strPrefix = "Happy String";
        final ScalarFactorySpec specScalFac = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed, fltIncr, strPrefix);
        
        final int       intDepth = 5;
        final int       intFanout = 2;
        final boolean   bolUniqFldNms = BOL_FLD_NMS_UNIQ_ENBL_DEF;
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.from(intDepth, intFanout, specScalFac);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#from(int, int, boolean, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ScalarFactorySpec)}.
     */
    @Test
    public final void testFromIntIntBooleanScalarFactorySpec() {
        
        // Test Parameters
        final JalScalarType     enmType = JalScalarType.FLOAT;
        final boolean           bolRandEnbl = true;
        final long              lngSeed = 42;
        final float             fltIncr = 1.23e-11f;
        final String            strPrefix = "Happy String";
        final ScalarFactorySpec specScalFac = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed, fltIncr, strPrefix);
        
        final int       intDepth = 5;
        final int       intFanout = 3;
        final boolean   bolUniqFldNms = !BOL_FLD_NMS_UNIQ_ENBL_DEF;
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.from(intDepth, intFanout, bolUniqFldNms, specScalFac);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse0() {
        
        // Test Parameters
        final ScalarFactorySpec specScalFac = REC_SCAL_SPEC_STRUCT_DEF;
        
        final int       intDepth = INT_TREE_DEPTH_DEF;
        final int       intFanout = INT_TREE_FANOUT_DEF;
        final boolean   bolUniqFldNms = BOL_FLD_NMS_UNIQ_ENBL_DEF;
        final String[]  arrArgs = { };
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.parse(arrArgs);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse1() {
        
        // Test Parameters
        final ScalarFactorySpec specScalFac = REC_SCAL_SPEC_PARSE_1;
        
        final int       intDepth = INT_TREE_DEPTH_PARSE;
        final int       intFanout = INT_TREE_FANOUT_PARSE;
        final boolean   bolUniqFldNms = BOL_FLD_NMS_UNIQ_ENBL_PARSE;
        final String[]  arrArgs = ARR_STR_ARGS_PARSE_1;
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.parse(arrArgs);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse2() {
        
        // Test Parameters
        final ScalarFactorySpec specScalFac = REC_SCAL_SPEC_PARSE_2;
        
        final int       intDepth = INT_TREE_DEPTH_PARSE;
        final int       intFanout = INT_TREE_FANOUT_PARSE;
        final boolean   bolUniqFldNms = BOL_FLD_NMS_UNIQ_ENBL_PARSE;
        final String[]  arrArgs = ARR_STR_ARGS_PARSE_2;
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.parse(arrArgs);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#newFactory()}.
     */
    @Test
    public final void testNewFactory() {
        
        // Test Parameters
        final JalScalarType     enmType = JalScalarType.FLOAT;
        final boolean           bolRandEnbl = false;
        final long              lngSeed = 42;
        final float             fltIncr = 1.23f;
        final String            strPrefix = "Happy String";
        final ScalarFactorySpec specScalFac = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed, fltIncr, strPrefix);
        
        final int       intDepth = 2;
        final int       intFanout = 3;
        final boolean   bolUniqFldNms = !BOL_FLD_NMS_UNIQ_ENBL_DEF;
        
        final DpSupportedType   enmDatumType = DpSupportedType.STRUCTURE;
        final JalComplexType    enmCmplxType = JalComplexType.STRUCTURE;
        
        final int       cntVals = 10;
        
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.from(intDepth, intFanout, bolUniqFldNms, specScalFac);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
        
        // Create structure factory and check configuration
        StructureFactory       facTest = specTest.newFactory();
        
        Assert.assertEquals(intDepth, facTest.getDepth());
        Assert.assertEquals(intFanout, facTest.getFanout());
        Assert.assertEquals(bolUniqFldNms, facTest.isUniqueFieldNamed());
        
        Assert.assertEquals(enmType, facTest.getScalarType());
        Assert.assertEquals(enmCmplxType, facTest.getComplexType());
        Assert.assertEquals(enmDatumType, facTest.getDatumType());
        
        Assert.assertEquals(lngSeed, facTest.getSeed());
        Assert.assertEquals(bolRandEnbl, facTest.isRandomValued());
        
        // Create some data and inspect
        StructureIndexGenerator     genIndices = StructureIndexGenerator.from(intDepth, intFanout);
        Float                       fltExpected = (float) lngSeed;
        int                         intNodeCntExpected = StructureUtility.computeTerminalNodeCount(intDepth, intFanout);
        for (int iVal=0; iVal<cntVals; iVal++) {
            Object  objVal = facTest.nextDatum();
            
            if (objVal instanceof Map) {
                int intNodeCnt = StructureUtility.terminalNodeCount(objVal);
                Assert.assertEquals(intNodeCntExpected, intNodeCnt);
                
                for (List<Integer> lstIndex : genIndices) {
                    Object  objFldVal = StructureUtility.extractNodeAt(lstIndex, objVal);
                    
                    if (objFldVal instanceof Float fltVal) {
                        Assert.assertEquals(fltExpected, fltVal);
                        
                        fltExpected += fltIncr;
                    } else
                        Assert.fail("Structure field value " + objFldVal + " at index " + lstIndex + " was not a Float.");
                }
                
            } else
                Assert.fail("Structure factory object " + objVal + " is not a Map.");
            
            genIndices.resetIndexIteration();
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.StructureFactorySpec#equals(java.lang.Object)}.
     */
    @Test
    public final void testEquals() {
        
        // Test Parameters
        final ScalarFactorySpec specScalFac = REC_SCAL_SPEC_STRUCT_DEF;
        
        final int       intDepth = 5;
        final int       intFanout = 10;
        final boolean   bolUniqFldNms = BOL_FLD_NMS_UNIQ_ENBL_DEF;
        
        // Create structure factory specification and test configuration
        StructureFactorySpec   specTest = StructureFactorySpec.from(intDepth, intFanout, bolUniqFldNms);
        
        Assert.assertEquals(intDepth, specTest.intDepth());
        Assert.assertEquals(intFanout, specTest.intFanout());
        Assert.assertEquals(bolUniqFldNms, specTest.bolUniqNms());
        Assert.assertEquals(specScalFac, specTest.recScalarSpec());
        
        // Create expected structure factory specification and check equivalence
        StructureFactorySpec    specExpect = StructureFactorySpec.from(intDepth, intFanout, specScalFac);
        
        Assert.assertTrue(specExpect.equals(specTest));
    }

}
