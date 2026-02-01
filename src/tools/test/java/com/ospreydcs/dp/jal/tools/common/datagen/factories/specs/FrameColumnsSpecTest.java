/*
 * Project: dp-jal
 * File:	FrameColumnsSpecTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.frames
 * Type: 	FrameColumnsSpecTest
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
 * @since Dec 29, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import javax.naming.ConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.common.IDataColumn;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameColumnsFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.cols.JalToolsColumnsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.frames.JalToolsFramesConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for record <code>FrameColumsSpec</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 29, 2025
 *
 */
public class FrameColumnsSpecTest {

    
    //
    // JAL Tools Resources
    //
    
    /** The column factory default configuration */
    private static final JalToolsColumnsConfig  CFG_COLS_DEF = JalToolsConfig.getInstance().datagen.column;
    
    /** The ingestion frame default configuration (includes default data columns) */
    private static final JalToolsFramesConfig   CFG_FRM_DEF = JalToolsConfig.getInstance().datagen.frame;
    
    
    //
    // Class Constants
    //
    
    /** The default column factory column count */
    public static final int             INT_COLS_CNT_DEF = CFG_COLS_DEF.count;
    
    /** The default column name prefix */
    public static final String          STR_COLS_NAME_DEF = CFG_COLS_DEF.name;
    
    /** The default column datum type */
    public static final JalComplexType  ENM_COL_TYPE_DEF = CFG_COLS_DEF.type;
    
    /** The default column datum factory configuration (parse string) */
    public static final String[]        ARR_COL_FAC_DEF = CFG_COLS_DEF.factory;
    
    
    /** StructureFactorySpec parsing creator test string */
    public static final String[]        ARR_ARGS_STRUCT_PARSE_0 = { "5", "2", "false", "INTEGER", "false", "11", "9", "Structure" };
    
    /** TensorFactorySpec parsing creator test string */
    public static final String[]        ARR_ARGS_TENSOR_PARSE_0 = { "1", "2", "3", "BOOLEAN", "false", "1", "1", "Tensor" };
    
    
    /** The structure factory specification matching the structure factory parsing creator string */
    public static final StructureFactorySpec    SPEC_STRUCT_0 = StructureFactorySpec.from(5, 2, false, JalScalarType.INTEGER, false, 11, Integer.valueOf(9), "Structure");
    
    /** The tensor factory specification matching the tensor factory parsing creator string */
    public static final int[]                   ARR_AXES_TENSOR_0 = {1, 2, 3};
    public static final TensorFactorySpec       SPEC_TENSOR_0 = TensorFactorySpec.from(ARR_AXES_TENSOR_0, JalScalarType.BOOLEAN, false, 1, Integer.valueOf(1), "Tensor");
    
    
    /** FrameColumnsSpec parsing test string array */
    public static final String[]        ARR_ARGS_PARSE_0 = { "10", "Parse0_PV:", "STRUCTURE" };
    
    /** FrameColumnsSpec parsing test string array */
    public static final String[]        ARR_ARGS_PARSE_1 = { "100", "Parse1_PV:", "SCALAR", "DOUBLE", "false", "0", "10.1", "SillyString" };
    
    /** FrameColumnsSpec parsing test string array */
    public static final String[]        ARR_ARGS_PARSE_2 = { "PV1", "PV2", "PV3", "SCALAR", "DOUBLE", "false", "0", "10.1", "SillyString" };
    
    
    //
    // Test Resources
    //
    
    /** The scalar factory specification for parsing string 0 */
    public static final StructureFactorySpec                SPEC_DATUM_FAC_PARSE_0 = StructureFactorySpec.from();
    
    /** The scalar factory specification for parsing string 1 */
    public static final ScalarFactorySpec                   SPEC_DATUM_FAC_PARSE_1 = ScalarFactorySpec.from(JalScalarType.DOUBLE, false, 0, Double.valueOf(10.1), "SillyString");
    
    /** The frame columns specification for parsing string 0 */
    public static final FrameColumnsSpec<StructureFactorySpec>  SPEC_COLS_PARSE_0 = FrameColumnsSpec.from(10, "Parse0_PV:", SPEC_DATUM_FAC_PARSE_0);
    
    /** The frame columns specification for parsing string 1 */
    public static final FrameColumnsSpec<ScalarFactorySpec>  SPEC_COLS_PARSE_1 = FrameColumnsSpec.from(100, "Parse1_PV:", SPEC_DATUM_FAC_PARSE_1);
    
    /** The frame columns specification for parsing string 2 */
    public static final FrameColumnsSpec<ScalarFactorySpec>  SPEC_COLS_PARSE_2 = FrameColumnsSpec.from(Set.of("PV1", "PV2", "PV3"), SPEC_DATUM_FAC_PARSE_1);
    
    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#from()}.
     * @throws NoSuchElementException 
     * @throws UnsupportedOperationException 
     * @throws ConfigurationException 
     * @throws TypeNotPresentException 
     * @throws NumberFormatException 
     */
    @Test
    public final void testFrom() throws NumberFormatException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        
        // Test Parameters
        final   Record      specFac = FrameColumnsSpecTest.parseDefaultFactorySpec();   // throws all exceptions
        final   int         intCols = INT_COLS_CNT_DEF;
        final   String      strNmPref = STR_COLS_NAME_DEF;
        
        final   JalComplexType  enmType = FrameColumnsSpec.inferColumnType(specFac);
        final   Class<?>        clsSpec = specFac.getClass();
        
        try {
            // Create new column factory specification and check field values
            FrameColumnsSpec<Record>    specTest = FrameColumnsSpec.from();

//            Assert.assertEquals(intCols, specTest.setColNms().size());
//            Assert.assertEquals(strNmPref, specTest.strNmPref());
            Assert.assertTrue(specTest.isValid());
            Assert.assertTrue( specTest.setColNms().stream().allMatch(nm -> nm.startsWith(strNmPref)) );
            Assert.assertEquals(intCols, specTest.setColNms().size());
            Assert.assertEquals(specFac, specTest.specFactory());
            Assert.assertEquals(enmType, specTest.enmType());
            Assert.assertEquals(clsSpec, specTest.clsFactory());
            
            // Print out default configuration (tests DataColumnsSpecDeprecated.toString() )
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
            System.out.println("DataColumnsSpecDeprecated Default Configuration");
            System.out.println(specTest);
            
        } catch (Exception e) {
            Assert.fail("Creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#from(int)}.
     * @throws NoSuchElementException 
     * @throws UnsupportedOperationException 
     * @throws ConfigurationException 
     * @throws TypeNotPresentException 
     * @throws NumberFormatException 
     */
    @Test
    public final void testFromInt() throws NumberFormatException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        
        // Test Parameters
        final   Record      specFac = FrameColumnsSpecTest.parseDefaultFactorySpec();   // throws all exceptions
        final   int         intCols = 11;
        final   String      strNmPref = STR_COLS_NAME_DEF;
        
        final JalComplexType    enmType = FrameColumnsSpec.inferColumnType(specFac);
        final Class<?>          clsSpec = specFac.getClass();
        
        try {
            // Create new column factory specification and check field values
            FrameColumnsSpec<Record>    specTest = FrameColumnsSpec.from(intCols);

//            Assert.assertTrue(specTest.isValid());
//            Assert.assertEquals(intCols, specTest.intCols());
//            Assert.assertEquals(strNmPref, specTest.strNmPref());
            Assert.assertTrue(specTest.isValid());
            Assert.assertTrue( specTest.setColNms().stream().allMatch(nm -> nm.startsWith(strNmPref)) );
            Assert.assertEquals(intCols, specTest.setColNms().size());
            Assert.assertEquals(specFac, specTest.specFactory());
            Assert.assertEquals(enmType, specTest.enmType());
            Assert.assertEquals(clsSpec, specTest.clsFactory());
            
        } catch (Exception e) {
            Assert.fail("Creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#from(int, java.lang.String)}.
     * @throws NoSuchElementException 
     * @throws UnsupportedOperationException 
     * @throws ConfigurationException 
     * @throws TypeNotPresentException 
     * @throws NumberFormatException 
     */
    @Test
    public final void testFromIntString() throws NumberFormatException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        
        // Test Parameters
        final   Record      specFac = FrameColumnsSpecTest.parseDefaultFactorySpec();   // throws all exceptions
        final   int         intCols = 11;
        final   String      strNmPref = "Happy String";
        
        final JalComplexType    enmType = FrameColumnsSpec.inferColumnType(specFac);
        final Class<?>          clsSpec = specFac.getClass();
        
        try {
            // Create new column factory specification and check field values
            FrameColumnsSpec<Record>    specTest = FrameColumnsSpec.from(intCols, strNmPref);

//            Assert.assertTrue(specTest.isValid());
//            Assert.assertEquals(intCols, specTest.intCols());
//            Assert.assertEquals(strNmPref, specTest.strNmPref());
            Assert.assertTrue(specTest.isValid());
            Assert.assertTrue( specTest.setColNms().stream().allMatch(nm -> nm.startsWith(strNmPref)) );
            Assert.assertEquals(intCols, specTest.setColNms().size());
            Assert.assertEquals(specFac, specTest.specFactory());
            Assert.assertEquals(enmType, specTest.enmType());
            Assert.assertEquals(clsSpec, specTest.clsFactory());
            
        } catch (Exception e) {
            Assert.fail("Creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#from(java.lang.Record)}.
     */
    @Test
    public final void testFromFactorySpec() {
        
        // Test Parameters
        final   ScalarFactorySpec   specFac = SPEC_DATUM_FAC_PARSE_1;
        final   int                 intCols = INT_COLS_CNT_DEF;
        final   String              strNmPref = STR_COLS_NAME_DEF;
        
        final   JalComplexType      enmType = FrameColumnsSpec.inferColumnType(specFac);
        final   Class<?>            clsSpec = specFac.getClass();
        
        try {
            // Create new column factory specification and check field values
            FrameColumnsSpec<ScalarFactorySpec>    specTest = FrameColumnsSpec.from(intCols, specFac);

//            Assert.assertTrue(specTest.isValid());
//            Assert.assertEquals(intCols, specTest.intCols());
//            Assert.assertEquals(strNmPref, specTest.strNmPref());
            Assert.assertTrue(specTest.isValid());
            Assert.assertTrue( specTest.setColNms().stream().allMatch(nm -> nm.startsWith(strNmPref)) );
            Assert.assertEquals(intCols, specTest.setColNms().size());
            Assert.assertEquals(specFac, specTest.specFactory());
            Assert.assertEquals(enmType, specTest.enmType());
            Assert.assertEquals(clsSpec, specTest.clsFactory());
            
        } catch (Exception e) {
            Assert.fail("Creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#from(int, java.lang.Record)}.
     */
    @Test
    public final void testFromIntFactorySpec() {
        
        // Test Parameters
        final   ScalarFactorySpec   specFac = SPEC_DATUM_FAC_PARSE_1;
        final   int                 intCols = 11;
        final   String              strNmPref = STR_COLS_NAME_DEF;
        
        final   JalComplexType      enmType = FrameColumnsSpec.inferColumnType(specFac);
        final   Class<?>            clsSpec = specFac.getClass();
        
        try {
            // Create new column factory specification and check field values
            FrameColumnsSpec<ScalarFactorySpec>    specTest = FrameColumnsSpec.from(intCols, specFac);

//            Assert.assertTrue(specTest.isValid());
//            Assert.assertEquals(intCols, specTest.intCols());
//            Assert.assertEquals(strNmPref, specTest.strNmPref());
            Assert.assertTrue(specTest.isValid());
            Assert.assertTrue( specTest.setColNms().stream().allMatch(nm -> nm.startsWith(strNmPref)) );
            Assert.assertEquals(intCols, specTest.setColNms().size());
            Assert.assertEquals(specFac, specTest.specFactory());
            Assert.assertEquals(enmType, specTest.enmType());
            Assert.assertEquals(clsSpec, specTest.clsFactory());
            
        } catch (Exception e) {
            Assert.fail("Creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#from(int, java.lang.String, java.lang.Record)}.
     */
    @Test
    public final void testFromIntStringFactorySpec() {
        
        // Test Parameters
        final   ScalarFactorySpec   specFac = SPEC_DATUM_FAC_PARSE_1;
        final   int                 intCols = 11;
        final   String              strNmPref = "Happy String";
        
        final   JalComplexType      enmType = FrameColumnsSpec.inferColumnType(specFac);
        final   Class<?>            clsSpec = specFac.getClass();
        
        try {
            // Create new column factory specification and check field values
            FrameColumnsSpec<ScalarFactorySpec>    specTest = FrameColumnsSpec.from(intCols, strNmPref, specFac);

//            Assert.assertTrue(specTest.isValid());
//            Assert.assertEquals(intCols, specTest.intCols());
//            Assert.assertEquals(strNmPref, specTest.strNmPref());
            Assert.assertTrue(specTest.isValid());
            Assert.assertTrue( specTest.setColNms().stream().allMatch(nm -> nm.startsWith(strNmPref)) );
            Assert.assertEquals(intCols, specTest.setColNms().size());
            Assert.assertEquals(specFac, specTest.specFactory());
            Assert.assertEquals(enmType, specTest.enmType());
            Assert.assertEquals(clsSpec, specTest.clsFactory());
            
        } catch (Exception e) {
            Assert.fail("Creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#from(com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType)}.
     */
    @Test
    public final void testFromJalComplexType() {
        
        // Test Parameters
        final   JalComplexType          enmType = JalComplexType.STRUCTURE;
        
        final   StructureFactorySpec    specFac = StructureFactorySpec.from();
        final   Class<?>                clsSpec = specFac.getClass();
        
        final   int                     intCols = INT_COLS_CNT_DEF;
        final   String                  strNmPref = STR_COLS_NAME_DEF;
        
        try {
            // Create new column factory specification and check field values
            FrameColumnsSpec<Record>    specTest = FrameColumnsSpec.from(enmType);

//            Assert.assertTrue(specTest.isValid());
//            Assert.assertEquals(intCols, specTest.intCols());
//            Assert.assertEquals(strNmPref, specTest.strNmPref());
            Assert.assertTrue(specTest.isValid());
            Assert.assertTrue( specTest.setColNms().stream().allMatch(nm -> nm.startsWith(strNmPref)) );
            Assert.assertEquals(intCols, specTest.setColNms().size());
            Assert.assertEquals(specFac, specTest.specFactory());
            Assert.assertEquals(enmType, specTest.enmType());
            Assert.assertEquals(clsSpec, specTest.clsFactory());
            
        } catch (Exception e) {
            Assert.fail("Creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#from(int, com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType)}.
     */
    @Test
    public final void testFromIntJalComplexType() {
        
        // Test Parameters
        final   JalComplexType          enmType = JalComplexType.TENSOR;
        
        final   TensorFactorySpec       specFac = TensorFactorySpec.from();
        final   Class<?>                clsSpec = specFac.getClass();
        
        final   int                     intCols = 23;
        final   String                  strNmPref = STR_COLS_NAME_DEF;
        
        try {
            // Create new column factory specification and check field values
            FrameColumnsSpec<Record>    specTest = FrameColumnsSpec.from(intCols, enmType);

//            Assert.assertTrue(specTest.isValid());
//            Assert.assertEquals(intCols, specTest.intCols());
//            Assert.assertEquals(strNmPref, specTest.strNmPref());
            Assert.assertTrue(specTest.isValid());
            Assert.assertTrue( specTest.setColNms().stream().allMatch(nm -> nm.startsWith(strNmPref)) );
            Assert.assertEquals(intCols, specTest.setColNms().size());
            Assert.assertEquals(specFac, specTest.specFactory());
            Assert.assertEquals(enmType, specTest.enmType());
            Assert.assertEquals(clsSpec, specTest.clsFactory());
            
        } catch (Exception e) {
            Assert.fail("Creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#from(int, java.lang.String, com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType)}.
     */
    @Test
    public final void testFromIntStringJalComplexType() {
        
        // Test Parameters
        final   JalComplexType          enmType = JalComplexType.BYTES;
        
        final   ByteArrayFactorySpec    specFac = ByteArrayFactorySpec.from();
        final   Class<?>                clsSpec = specFac.getClass();
        
        final   int                     intCols = 23;
        final   String                  strNmPref = "Byte me.";
        
        try {
            // Create new column factory specification and check field values
            FrameColumnsSpec<Record>    specTest = FrameColumnsSpec.from(intCols, strNmPref, enmType);

//            Assert.assertTrue(specTest.isValid());
//            Assert.assertEquals(intCols, specTest.intCols());
//            Assert.assertEquals(strNmPref, specTest.strNmPref());
            Assert.assertTrue(specTest.isValid());
            Assert.assertTrue( specTest.setColNms().stream().allMatch(nm -> nm.startsWith(strNmPref)) );
            Assert.assertEquals(intCols, specTest.setColNms().size());
            Assert.assertEquals(specFac, specTest.specFactory());
            Assert.assertEquals(enmType, specTest.enmType());
            Assert.assertEquals(clsSpec, specTest.clsFactory());
            
        } catch (Exception e) {
            Assert.fail("Creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse0() {
        
        // Test Parameters
        String[]                                  arrArgs = ARR_ARGS_PARSE_0;
        FrameColumnsSpec<StructureFactorySpec>    specExpect = SPEC_COLS_PARSE_0;
        
        try {
            FrameColumnsSpec<Record> specTest = FrameColumnsSpec.parse(arrArgs);
            
            Assert.assertEquals(specExpect, specTest);
            
        } catch (Exception e) {
            Assert.fail(JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Parsing creation failed with exception "
                    + e.getClass().getName()
                    + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse1() {
        
        // Test Parameters
        String[]                                arrArgs = ARR_ARGS_PARSE_1;
        FrameColumnsSpec<ScalarFactorySpec>     specExpect = SPEC_COLS_PARSE_1;
        
        try {
            FrameColumnsSpec<Record> specTest = FrameColumnsSpec.parse(arrArgs);
            
            Assert.assertEquals(specExpect, specTest);
            
        } catch (Exception e) {
            Assert.fail(JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Parsing creation failed with exception "
                    + e.getClass().getName()
                    + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse2() {
        
        // Test Parameters
        String[]                                arrArgs = ARR_ARGS_PARSE_2;
        FrameColumnsSpec<ScalarFactorySpec>     specExpect = SPEC_COLS_PARSE_2;
        
        try {
            FrameColumnsSpec<Record> specTest = FrameColumnsSpec.parse(arrArgs);
            
            Assert.assertEquals(specExpect, specTest);
            
        } catch (Exception e) {
            Assert.fail(JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Parsing creation failed with exception "
                    + e.getClass().getName()
                    + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#defaultFrame()}.
     * @throws UnsupportedOperationException 
     * @throws TypeNotPresentException 
     * @throws ConfigurationException 
     * @throws IllegalArgumentException 
     * @throws NumberFormatException 
     */
    @Test
    public final void testDefaultFrame() throws NumberFormatException, IllegalArgumentException, ConfigurationException, TypeNotPresentException, UnsupportedOperationException {
        
        // Test Parameters
        List<FrameColumnsSpec<Record>>  lstSpecsExpect = extractDefaultFrameColumns();
        
        List<FrameColumnsSpec<Record>>  lstSpecsTest = FrameColumnsSpec.defaultFrame();
        
        Assert.assertEquals(lstSpecsExpect, lstSpecsTest);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#newFactory()}.
     */
    @Test
    public final void testNewFactory() {
        
        // Test Parameters
        final   TensorFactorySpec   specFac = SPEC_TENSOR_0;   
        final   int         intCols = 5;
        final   String      strNmPref = "PV:";
        
        final   JalComplexType  enmType = FrameColumnsSpec.inferColumnType(specFac);
        final   Class<?>        clsSpec = specFac.getClass();
        
        final  Set<String>      setColNms = IntStream.range(0, intCols).mapToObj(i -> strNmPref + Integer.toString(i)).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        
        final  int              szCol = 10;
        final  int              cntFrms = 5;
        
        try {
            // Create new column factory specification and check field values
            FrameColumnsSpec<TensorFactorySpec>    specTest = FrameColumnsSpec.from(intCols, strNmPref, specFac);

//            Assert.assertTrue(specTest.isValid());
//            Assert.assertEquals(intCols, specTest.intCols());
//            Assert.assertEquals(strNmPref, specTest.strNmPref());
            Assert.assertTrue(specTest.isValid());
            Assert.assertTrue( specTest.setColNms().stream().allMatch(nm -> nm.startsWith(strNmPref)) );
            Assert.assertEquals(intCols, specTest.setColNms().size());
            Assert.assertEquals(specFac, specTest.specFactory());
            Assert.assertEquals(enmType, specTest.enmType());
            Assert.assertEquals(clsSpec, specTest.clsFactory());

            // Create a frame column factory and check configuration
            IFrameColumnsFactory<Object> facTest = specTest.newFactory();
            
            Assert.assertEquals(intCols, facTest.getColumnCount());
            Assert.assertEquals(enmType.getDpType(), facTest.getColumnType());
            Assert.assertEquals(setColNms, facTest.getColumnNames());
            
            // Create some columns and inspect values
            for (int iFrm=0; iFrm<cntFrms; iFrm++) {
                ArrayList<IDataColumn<Object>> lstCols = facTest.build(szCol);
                
                Assert.assertEquals(intCols, lstCols.size());
                for (IDataColumn<Object> col : lstCols) {
                    Assert.assertEquals(Integer.valueOf(szCol), col.getSize());
                    
                    for (Object obj : col) {
                        Assert.assertTrue(obj instanceof ArrayList);
                    }
                }
            }
            
            
        } catch (Exception e) {
            Assert.fail("Creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#inferColumnType(java.lang.Record)}.
     */
    @Test
    public final void testInferColumnType() {
        Assert.assertEquals(JalComplexType.SCALAR, FrameColumnsSpec.inferColumnType(ScalarFactorySpec.from()));
        Assert.assertEquals(JalComplexType.TIMESTAMP, FrameColumnsSpec.inferColumnType(TimestampFactorySpec.from()));
        Assert.assertEquals(JalComplexType.BYTES, FrameColumnsSpec.inferColumnType(ByteArrayFactorySpec.from()));
        Assert.assertEquals(JalComplexType.IMAGE, FrameColumnsSpec.inferColumnType(ImageFactorySpec.from()));
        Assert.assertEquals(JalComplexType.TENSOR, FrameColumnsSpec.inferColumnType(TensorFactorySpec.from()));
        Assert.assertEquals(JalComplexType.STRUCTURE, FrameColumnsSpec.inferColumnType(StructureFactorySpec.from()));
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#parseFactorySpec(com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType, java.lang.String[])}.
     */
    @Test
    public final void testParseFactorySpec0() {
        
        // Test Parameters
        final JalComplexType    enmType = JalComplexType.TENSOR;
        final String[]          arrArgs = ARR_ARGS_TENSOR_PARSE_0;
        
        final Record            specExpect = SPEC_TENSOR_0;
        
        try {
            Record  specTest = FrameColumnsSpec.parseFactorySpec(enmType, arrArgs);
            
            Assert.assertEquals(specExpect, specTest);
            
        } catch (Exception e) {
            Assert.fail(JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Datum factory parsing creation failed with exception "
                    + e.getClass().getName()
                    + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#parseFactorySpec(com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType, java.lang.String[])}.
     */
    @Test
    public final void testParseFactorySpec1() {
        
        // Test Parameters
        final JalComplexType    enmType = JalComplexType.STRUCTURE;
        final String[]          arrArgs = ARR_ARGS_STRUCT_PARSE_0;
        
        final Record            specExpect = SPEC_STRUCT_0;
        
        try {
            Record  specTest = FrameColumnsSpec.parseFactorySpec(enmType, arrArgs);
            
            Assert.assertEquals(specExpect, specTest);
            
        } catch (Exception e) {
            Assert.fail(JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Datum factory parsing creation failed with exception "
                    + e.getClass().getName()
                    + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameColumnsSpec#equals(java.lang.Object)}.
     * @throws TypeNotPresentException      an enumeration constant was not recognized
     * @throws NumberFormatException        invalid number format (e.g., seed value for scalar factory specification)
     * @throws UnsupportedOperationException unable to create 'numIncr' field in scalar factory specification
     * @throws MissingResourceException     timestamp factory had empty arguments
     * @throws DateTimeParseException       invalid format for ISO-8601 time and/or duration specification 
     * @throws ConfigurationException       tensor shape was invalid
     * @throws NoSuchElementException       the 'enmType' constant was not supported
     */
    @Test
    public final void testEquals() throws NumberFormatException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        
        // Test Parameters
        final   Record      specFac = FrameColumnsSpecTest.parseDefaultFactorySpec();   // throws all exceptions
        final   int         intCols = INT_COLS_CNT_DEF;
        final   String      strNmPref = STR_COLS_NAME_DEF;
        
        final FrameColumnsSpec<Record>  specExpect = FrameColumnsSpec.from(intCols, strNmPref, specFac);
        
        try {
            // Create new column factory specification and check field values
            FrameColumnsSpec<Record>    specTest = FrameColumnsSpec.from();

            Assert.assertTrue(specExpect.equals(specTest));
            
        } catch (Exception e) {
            Assert.fail("Creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }


    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates the default datum factory specification as defined in the JAL Tools default configuration.
     * </p>
     * The default datum factory is created by parsing the <code>{@link #ARR_FAC_SPEC_DEF}</code> configuration 
     * against the supported cases of <code>{@link #ENM_COL_TYPE_DEF}</code>.
     * </p>
     * 
     * @return the default datum factory specification of the JAL Tools default configuration 
     * 
     * @throws TypeNotPresentException  unknown <code>JalScalarType</code> constant
     * @throws NumberFormatException    invalid numeric format (e.g., bad 'lngSeed' value)
     * @throws ConfigurationException   tensor factory had invalid shape
     * @throws UnsupportedOperationException unable to create 'numIncr' parameter in scalar factory
     * @throws NoSuchElementException   the value of <code>{@link #ENM_COL_TYPE_DEF}</code> was unrecognized
     */
    private static Record   parseDefaultFactorySpec() throws TypeNotPresentException, NumberFormatException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {

        Record recFactory = switch (ENM_COL_TYPE_DEF) {
        case SCALAR -> ScalarFactorySpec.parse(ARR_COL_FAC_DEF);
        case TENSOR -> TensorFactorySpec.parse(ARR_COL_FAC_DEF);
        case BYTES -> ByteArrayFactorySpec.parse(ARR_COL_FAC_DEF);
        case IMAGE -> ImageFactorySpec.parse(ARR_COL_FAC_DEF);
        case STRUCTURE -> StructureFactorySpec.parse(ARR_COL_FAC_DEF);
        case TIMESTAMP -> TimestampFactorySpec.parse(ARR_COL_FAC_DEF);
        default -> throw new NoSuchElementException("Unexpected value: " + ENM_COL_TYPE_DEF);
        };

        return recFactory;
    }
    
    /**
     * <p>
     * Extracts the list of data column specifications for the default ingestion frame from the JAL Tools default configuration.
     * </p>
     * 
     * @return  list of default ingestion frame columns specifications
     * 
     * @throws TypeNotPresentException      an enumeration constant was not recognized
     * @throws NumberFormatException        invalid number format (e.g., seed value for scalar factory specification)
     * @throws UnsupportedOperationException unable to create 'numIncr' field in scalar factory specification
     * @throws MissingResourceException     timestamp factory had empty arguments
     * @throws DateTimeParseException       invalid format for ISO-8601 time and/or duration specification 
     * @throws ConfigurationException       tensor shape was invalid
     * @throws NoSuchElementException       the 'enmType' constant was not supported
     */
    private static List<FrameColumnsSpec<Record>>   extractDefaultFrameColumns() throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException, MissingResourceException, DateTimeParseException, ConfigurationException, NoSuchElementException {
        
        List<JalToolsColumnsConfig>     lstCfgCols = CFG_FRM_DEF.columns;
        List<FrameColumnsSpec<Record>>  lstSpecCols = new ArrayList<>(lstCfgCols.size());
        
        for (JalToolsColumnsConfig cfg : lstCfgCols) {
            int             intCols = cfg.count;
            String          strNmPref = cfg.name;
            JalComplexType  enmType = cfg.type;
            
            String[]        arrFacArgs = cfg.factory;
            Record          specFac = FrameColumnsSpec.parseFactorySpec(enmType, arrFacArgs); // throws all exceptions
            
            FrameColumnsSpec<Record>    specCols = FrameColumnsSpec.from(intCols, strNmPref, specFac);
            
            lstSpecCols.add(specCols);
        }
        
        return lstSpecCols;
    }
}
