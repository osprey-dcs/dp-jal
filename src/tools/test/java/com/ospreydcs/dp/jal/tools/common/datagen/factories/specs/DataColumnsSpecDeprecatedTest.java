/*
 * Project: dp-jal
 * File:	DataColumnsSpecDeprecatedTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.specs
 * Type: 	DataColumnsSpecDeprecatedTest
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
 * @since Dec 18, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import java.util.List;
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

import com.ospreydcs.dp.jal.tools.common.datagen.IFrameColumnsFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.cols.JalToolsColumnsConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for record <code>DataColumnsSpecDeprecated</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 18, 2025
 * 
 * @deprecated DataColumnsSpec was replaced by a newer version
 */
@Deprecated(since="Dec 29, 2025", forRemoval=true)
public class DataColumnsSpecDeprecatedTest {

    
    //
    // JAL Tools Resources
    //
    
    /** The column factory default configuration */
    private static final JalToolsColumnsConfig  CFG_COLS_DEF = JalToolsConfig.getInstance().datagen.columns;
    
    
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
    
    
    /** Parsing test string array */
    public static final String[]        ARR_ARGS_PARSE_0 = { "10", "Parse0_PV:", "STRUCTURE" };
    
    /** Parsing test string array */
    public static final String[]        ARR_ARGS_PARSE_1 = { "100", "Parse1_PV:", "SCALAR", "DOUBLE", "false", "0", "10.1", "SillyString" };
    
    
    //
    // Test Resources
    //
    
    /** The scalar factory specification for parsing string 0 */
    public static final StructureFactorySpec                SPEC_DATUM_FAC_PARSE_0 = StructureFactorySpec.from();
    
    /** The scalar factory specification for parsing string 1 */
    public static final ScalarFactorySpec                   SPEC_DATUM_FAC_PARSE_1 = ScalarFactorySpec.from(JalScalarType.DOUBLE, false, 0, Double.valueOf(10.1), "SillyString");
    
    /** The data columns specification for parsing string 0 */
    @SuppressWarnings("unchecked")
    public static final DataColumnsSpecDeprecated<StructureFactorySpec>  SPEC_COLS_PARSE_0 = DataColumnsSpecDeprecated.from(10, "Parse0_PV:", SPEC_DATUM_FAC_PARSE_0);
    
    /** The data columns specification for parsing string 1 */
    @SuppressWarnings("unchecked")
    public static final DataColumnsSpecDeprecated<ScalarFactorySpec>  SPEC_COLS_PARSE_1 = DataColumnsSpecDeprecated.from(100, "Parse1_PV:", SPEC_DATUM_FAC_PARSE_1);
    
    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.DataColumnsSpecDeprecated#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse0() {
        
        // Test Parameters
        String[]        arrArgs = ARR_ARGS_PARSE_0;
        DataColumnsSpecDeprecated specExpect = SPEC_COLS_PARSE_0;
        
        try {
            DataColumnsSpecDeprecated specCols = DataColumnsSpecDeprecated.parse(arrArgs);
            
            Assert.assertEquals(specExpect, specCols);
            
        } catch (Exception e) {
            Assert.fail(JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Parsing creation failed with exception "
                    + e.getClass().getName()
                    + ": " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.DataColumnsSpecDeprecated#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse1() {
        
        // Test Parameters
        String[]        arrArgs = ARR_ARGS_PARSE_1;
        DataColumnsSpecDeprecated specExpect = SPEC_COLS_PARSE_1;
        
        try {
            DataColumnsSpecDeprecated specCols = DataColumnsSpecDeprecated.parse(arrArgs);
            
            Assert.assertEquals(specExpect, specCols);
            
        } catch (Exception e) {
            Assert.fail(JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Parsing creation failed with exception "
                    + e.getClass().getName()
                    + ": " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.DataColumnsSpecDeprecated#defaultFrameColumns()}.
     */
    @Test
    public final void testDefaultFrameColumns() {
        
        try {
            @SuppressWarnings("rawtypes")
            List<DataColumnsSpecDeprecated>    lstColSpecs = DataColumnsSpecDeprecated.defaultFrameColumns();
        
            // Print out ingestion frame default columns specifications 
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
            System.out.println("Default Ingestion Frame Data Columns Specifications");
            for (@SuppressWarnings("rawtypes") DataColumnsSpecDeprecated spec : lstColSpecs) {
                System.out.println(spec);
            }
        
        } catch (Exception e) {
            Assert.fail(JavaRuntime.getQualifiedMethodNameSimple() 
                    + " Data columns specification creation failed with exception "
                    + e.getClass().getName()
                    + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.DataColumnsSpecDeprecated#from()}.
     * @throws NoSuchElementException 
     * @throws UnsupportedOperationException 
     * @throws ConfigurationException 
     * @throws TypeNotPresentException 
     * @throws NumberFormatException 
     */
    @Test
    public final void testFrom() throws NumberFormatException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        
        // Test Parameters
        final   Record      specFac = DataColumnsSpecDeprecatedTest.parseDefaultFactorySpec();
        final   int         cntCols = INT_COLS_CNT_DEF;
        final   String      strNmPref = STR_COLS_NAME_DEF;
        
        try {
            // Create new column factory specification and check field values
            @SuppressWarnings("rawtypes")
            DataColumnsSpecDeprecated specCols = DataColumnsSpecDeprecated.from();

            Assert.assertTrue(specCols.isValid());
            Assert.assertEquals(cntCols, specCols.cntCols());
            Assert.assertEquals(strNmPref, specCols.strNmPref());
            Assert.assertEquals(specFac, specCols.recFacSpec());
            
            // Print out default configuration (tests DataColumnsSpecDeprecated.toString() )
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
            System.out.println("DataColumnsSpecDeprecated Default Configuration");
            System.out.println(specCols);
            
        } catch (Exception e) {
            Assert.fail("DataColumnsSpecDeprecated creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.DataColumnsSpecDeprecated#from(int)}.
     * @throws NoSuchElementException 
     * @throws UnsupportedOperationException 
     * @throws ConfigurationException 
     * @throws TypeNotPresentException 
     * @throws NumberFormatException 
     */
    @Test
    public final void testFromInt() throws NumberFormatException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        
        // Test Parameters
        final   Record      specFac = DataColumnsSpecDeprecatedTest.parseDefaultFactorySpec();
        final   int         cntCols = 23;
        final   String      strNmPref = STR_COLS_NAME_DEF;
        
        try {
            // Create new column factory specification and check field values
            @SuppressWarnings("rawtypes")
            DataColumnsSpecDeprecated specCols = DataColumnsSpecDeprecated.from(cntCols);

            Assert.assertTrue(specCols.isValid());
            Assert.assertEquals(cntCols, specCols.cntCols());
            Assert.assertEquals(strNmPref, specCols.strNmPref());
            Assert.assertEquals(specFac, specCols.recFacSpec());
            
        } catch (Exception e) {
            Assert.fail("DataColumnsSpecDeprecated creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.DataColumnsSpecDeprecated#from(java.lang.Record)}.
     */
    @Test
    public final void testFromRecord() {
        
        // Test Parameters
        final   StructureFactorySpec    specFac = StructureFactorySpec.from();
        final   int                     cntCols = INT_COLS_CNT_DEF;
        final   String                  strNmPref = STR_COLS_NAME_DEF;
        
        try {
            // Create new column factory specification and check field values
            @SuppressWarnings("rawtypes")
            DataColumnsSpecDeprecated specCols = DataColumnsSpecDeprecated.from(specFac);

            Assert.assertTrue(specCols.isValid());
            Assert.assertEquals(cntCols, specCols.cntCols());
            Assert.assertEquals(strNmPref, specCols.strNmPref());
            Assert.assertEquals(specFac, specCols.recFacSpec());
            
        } catch (Exception e) {
            Assert.fail("DataColumnsSpecDeprecated creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.DataColumnsSpecDeprecated#from(int, java.lang.Record)}.
     */
    @Test
    public final void testFromIntRecord() {
        
        // Test Parameters
        final   StructureFactorySpec    specFac = StructureFactorySpec.from();
        final   int                     cntCols = 101;
        final   String                  strNmPref = STR_COLS_NAME_DEF;
        
        try {
            // Create new column factory specification and check field values
            @SuppressWarnings("rawtypes")
            DataColumnsSpecDeprecated specCols = DataColumnsSpecDeprecated.from(cntCols, specFac);

            Assert.assertTrue(specCols.isValid());
            Assert.assertEquals(cntCols, specCols.cntCols());
            Assert.assertEquals(strNmPref, specCols.strNmPref());
            Assert.assertEquals(specFac, specCols.recFacSpec());
            
        } catch (Exception e) {
            Assert.fail("DataColumnsSpecDeprecated creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.DataColumnsSpecDeprecated#from(int, java.lang.String, java.lang.Record)}.
     */
    @Test
    public final void testFromIntStringRecord() {
        
        // Test Parameters
        final   TimestampFactorySpec    specFac = TimestampFactorySpec.from();
        final   int                     cntCols = 101;
        final   String                  strNmPref = "JUnitTest:";
        
        try {
            // Create new column factory specification and check field values
            @SuppressWarnings("rawtypes")
            DataColumnsSpecDeprecated specCols = DataColumnsSpecDeprecated.from(cntCols, strNmPref, specFac);

            Assert.assertTrue(specCols.isValid());
            Assert.assertEquals(cntCols, specCols.cntCols());
            Assert.assertEquals(strNmPref, specCols.strNmPref());
            Assert.assertEquals(specFac, specCols.recFacSpec());
            
        } catch (Exception e) {
            Assert.fail("DataColumnsSpecDeprecated creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.DataColumnsSpecDeprecated#newFactory()}.
     */
    @Test
    public final void testNewFactory() {
        
        // Test Parameters
        final   TimestampFactorySpec    specFac = TimestampFactorySpec.from();
        final   int                     cntCols = 101;
        final   String                  strNmPref = "JUnitTest:";
        
        // Create new column factory specification and check field values
        @SuppressWarnings("rawtypes")
        DataColumnsSpecDeprecated specCols; 
        try {
            specCols = DataColumnsSpecDeprecated.from(cntCols, strNmPref, specFac);

            Assert.assertTrue(specCols.isValid());
            Assert.assertEquals(cntCols, specCols.cntCols());
            Assert.assertEquals(strNmPref, specCols.strNmPref());
            Assert.assertEquals(specFac, specCols.recFacSpec());
            
        } catch (Exception e) {
            Assert.fail("DataColumnsSpecDeprecated creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
            return;
        }
        
        // Create data columns factory and check configuration
        Set<String>     setColNms = IntStream.range(0, cntCols).<String>mapToObj(i -> strNmPref + Integer.toString(i)).collect(TreeSet::new, Set::add, Set::addAll);
        try {
            IFrameColumnsFactory facCols = specCols.newFactory();
            
            Assert.assertEquals(cntCols, facCols.getColumnCount());
            Assert.assertEquals(setColNms, facCols.getColumnNames());
            
        } catch (ConfigurationException | UnsupportedOperationException e) {
            Assert.fail("IFrameColumnsFactory creation from DataColumnsSpecDeprecated failed with exception " + e.getClass().getName() + ": " + e.getMessage());
            return;
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
}
