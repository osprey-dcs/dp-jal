/*
 * Project: dp-jal
 * File:	TestSuiteGeneratorBaseTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.score
 * Type: 	TestSuiteGeneratorBaseTest
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
 * @since Jan 17, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.score;

import java.time.Duration;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.common.DpTimestampCase;
import com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBaseTest.TestCase.Param;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>TestSuiteGeneratorBase</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 17, 2026
 *
 */
public class TestSuiteGeneratorBaseTest {

    
    //
    // Test Types
    //
    
    /**
     * <p>
     * Example test case record used for generic parameter <code>TestCase</code>.
     * </p>
     */
    public static record    TestCase(
            int             index,
            int             cntFrames,
            int             cntSmpls,
            Duration        durPeriod,
            DpTimestampCase enmTmsCase,
            int             cntCols,
            String          strNmPref,
            DpSupportedType enmColType
            ) 
    {
        
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates and returns a new <code>TestCase</code> record populated with the given argument values.
         * </p>
         * 
         * @param cntFrames     number of ingestion frames
         * @param cntSmpls      number of samples per frame (i.e., timestamps)
         * @param durPeriod     sampling period
         * @param enmTmsCase    the frame timestamp type
         * @param cntCols       number of data columns per ingestion frame
         * @param strNmPref     column name prefix for each data column
         * @param enmColType    data type for each data column
         * 
         * @return a new <code>TestCase</code> record with field values given by the arguments
         */
        public static TestCase  from(
                int             cntFrames,
                int             cntSmpls,
                Duration        durPeriod,
                DpTimestampCase enmTmsCase,
                int             cntCols,
                String          strNmPref,
                DpSupportedType enmColType
                ) 
        {
            return new TestCase(IND_CASE, cntFrames, cntSmpls, durPeriod, enmTmsCase, cntCols, strNmPref, enmColType); 
        }
        
        //
        // Internal Types
        //
        
        /**
         * <p>
         * Enumeration of <code>TestCase</code> fields.
         * </p>
         */
        public static enum  Param {
            
            /**
             * Associates with <code>{@link TestCase#cntFrames()}</code>
             */
            FRAME_COUNT("cntFrames", Integer.class),
            
            /**
             * Associates with <code>{@link TestCase#cntSmpls()}</code>
             */
            SAMPLE_COUNT("cntSmpls", Integer.class),
            
            /**
             * Associates with <code>{@link TestCase#durPeriod()}</code> 
             */
            SAMPLE_PERIOD("durPeriod", Duration.class),
            
            /**
             * Associates with <code>{@link TestCase#enmTmsCase()}</code> 
             */
            TIMESTAMP_CASE("enmTmsCase", DpTimestampCase.class),
            
            /**
             * Associates with <code>{@link TestCase#cntCols()}</code>
             */
            COLUMN_COUNT("cntCols", Integer.class),
            
            /**
             * Associates with <code>{@link TestCase#strNmPref()}</code>
             */
            COLUMN_NAME("strNmPref", String.class),
            
            /**
             * Associates with <code>{@link TestCase#enmColType()}</code>
             */
            COLUMN_TYPE("enmColType", DpSupportedType.class),
            ;
            
            /** The <code>TestCase</code> field name */
            @SuppressWarnings("unused")
            private final String        strFieldName;
            
            /** The <code>TestCase</code> field type */
            private final Class<?>      clsFieldType;
            
            /** Enumeration constant constructor */ 
            private Param(String strFieldName, Class<?> clsParamType) {
                this.strFieldName = strFieldName;
                this.clsFieldType = clsParamType; 
            };
            
            /** Returns the class type of the parameter associated with this enumeration constant */
            public Class<?> getParameterType() { return this.clsFieldType; };
            
            /** Determines whether or not this parameter type is compatible with the given class */
            public boolean  isAssignable(Class<?> clsVal) { return this.clsFieldType.isAssignableFrom(clsVal); };
            
            /** Determines whether or not the given object can be used as a parameter value */
            public boolean  isInstance(Object objVal) { return this.clsFieldType.isInstance(objVal); };
        }
        
        /** Running case instance index */
        private static int IND_CASE = 1;
        
        
        /**
         * <p>
         * Canonical Constructor: Constructs a new <code>TestCase</code> instance.
         * </p>
         * <p>
         * Increments the class instance index <code>{@link #IND_CASE}</code> after construction.
         * </p>
         *
         * @param index         test case index
         * @param cntFrames     number of ingestion frames
         * @param cntSmpls      number of samples per frame (i.e., timestamps)
         * @param durPeriod     sampling period
         * @param enmTmsCase    the frame timestamp type
         * @param cntCols       number of data columns per ingestion frame
         * @param strNmPref     column name prefix for each data column
         * @param enmColType    data type for each data column
         */
        public TestCase {
            IND_CASE++;
        }
    }
    
    //
    // Test Class
    //
    
    /**
     * <p>
     * Child class of <code>TestSuiteGeneratorBase</code> - the class under test.
     * </p>
     */
    public final static class TestSuiteGenerator extends TestSuiteGeneratorBase<TestCase.Param, TestCase> {
        
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates and returns an empty <code>TestSuiteGenerator</code> instance ready for configuration.
         * </p>
         * <p>
         * Note that the returned instanced must be populated with parameter values before attempting
         * test suite generation.
         * </p>
         * 
         * @return  a new, uninitialized <code>TestSuiteGenerator</code> instance ready for configuration
         */
        public static TestSuiteGenerator    from() {
            return new TestSuiteGenerator();
        }
        
        //
        // Constructors
        //

        /**
         * <p>
         * Constructs a new <code>TestSuiteGenerator</code> instance.
         * </p>
         */
        protected TestSuiteGenerator() {
            super(Param.class);
        }

        //
        // TestSuiteGeneratorBase Abstract Implementations
        //
        
        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#isValidType(java.lang.Enum, java.lang.Object)
         */
        @Override
        protected boolean isValidType(Param enmParam, Object objVal) {
            
            return enmParam.isInstance(objVal);
        }

        /**
         * @see com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#createTestCase(java.util.Map)
         */
        @Override
        protected TestCase createTestCase(Map<Param, Object> mapTestVals) throws ClassCastException, MissingResourceException, UnsupportedOperationException {
            
            // Check for parameter completeness
            if (super.hasMissingParameters(mapTestVals))
                throw new MissingResourceException(
                            JavaRuntime.getQualifiedMethodNameSimple() + " - (Param, Value) map is missing parameter(s).",
                            Map.class.getName(), 
                            this.missingParameters(mapTestVals).toString()
                            );
            
            // Check for value completeness
            if (super.hasMissingValues(mapTestVals))
                throw new MissingResourceException(
                        JavaRuntime.getQualifiedMethodNameSimple() + " - (Param, Value) map is missing parameter value(s).",
                        Map.class.getName(), 
                        super.missingValues(mapTestVals).toString()
                        );
            
            // Make room for the TestCase field values
            Integer         cntFrames = null;
            Integer         cntSmpls = null;
            Duration        durPeriod = null;
            DpTimestampCase enmTmsCase = null;
            Integer         cntCols = null;
            String          strNmPref = null;
            DpSupportedType enmColType = null;
            
            for (Map.Entry<Param, Object> entry : mapTestVals.entrySet()) {
                Param   enmParam = entry.getKey();
                Object  objValue = entry.getValue();
                
                switch (enmParam) {
                case FRAME_COUNT:
                    cntFrames = Integer.class.cast(objValue);       // throws ClassCastException
                    break;
                case SAMPLE_COUNT:
                    cntSmpls = Integer.class.cast(objValue);        // throws ClassCastException
                    break;
                case SAMPLE_PERIOD:
                    durPeriod = Duration.class.cast(objValue);      // throws ClassCastException
                    break;
                case TIMESTAMP_CASE:
                    enmTmsCase = DpTimestampCase.class.cast(objValue);  // throws ClassCastException
                    break;
                case COLUMN_COUNT:
                    cntCols = Integer.class.cast(objValue);         // throws ClassCastException
                    break;
                case COLUMN_NAME:
                    strNmPref = String.class.cast(objValue);        // throws ClassCastException
                    break;
                case COLUMN_TYPE:
                    enmColType = DpSupportedType.class.cast(objValue);  // throws ClassCastException
                    break;
                default:
                    throw new UnsupportedOperationException(JavaRuntime.getQualifiedMethodNameSimple() + " - Unrecognized parameter " + enmParam);
                };
            }
            
            // Create new TestCase record and return it
            TestCase    recCase = TestCase.from(cntFrames, cntSmpls, durPeriod, enmTmsCase, cntCols, strNmPref, enmColType);
            
            return recCase;
        }
    }
    
    
    //
    // Test Resources
    //

    /** Example (Parameter, Value) mapping for a single test case */
    public static final Map<Param, Object>  MAP_CASE_1 = Map.of(Param.FRAME_COUNT, 5, 
                                                                Param.SAMPLE_COUNT, 100, 
                                                                Param.SAMPLE_PERIOD, Duration.ofMillis(1),
                                                                Param.TIMESTAMP_CASE, DpTimestampCase.SAMPLING_CLOCK,
                                                                Param.COLUMN_COUNT, 10,
                                                                Param.COLUMN_NAME, "JUnit1:",
                                                                Param.COLUMN_TYPE, DpSupportedType.DOUBLE
                                                                );
    
    /** Example (Parameter, Value) mapping for a single test case */
    public static final Map<Param, Object>  MAP_CASE_2 = Map.of(Param.FRAME_COUNT, 5, 
                                                                Param.SAMPLE_COUNT, 1000, 
                                                                Param.SAMPLE_PERIOD, Duration.ofMillis(1),
                                                                Param.TIMESTAMP_CASE, DpTimestampCase.TIMESTAMP_LIST,
                                                                Param.COLUMN_COUNT, 100,
                                                                Param.COLUMN_NAME, "JUnit2:",
                                                                Param.COLUMN_TYPE, DpSupportedType.INTEGER
                                                                );
    
    /** Example (Parameter, Value) mapping for a single test case */
    public static final Map<Param, Object>  MAP_CASE_3 = Map.of(Param.FRAME_COUNT, 5, 
                                                                Param.SAMPLE_COUNT, 1000, 
                                                                Param.SAMPLE_PERIOD, Duration.ofMillis(1),
                                                                Param.TIMESTAMP_CASE, DpTimestampCase.SAMPLING_CLOCK,
                                                                Param.COLUMN_COUNT, 1,
                                                                Param.COLUMN_NAME, "JUnit1:",
                                                                Param.COLUMN_TYPE, DpSupportedType.IMAGE
                                                                );
    
    /** Example list of <code>{@link Param#FRAME_COUNT}</code> parameters for test suite generation */
    public static final List<Integer>       LST_CNT_FRMS_1 = List.of(5);
    
    /** Example list of <code>{@link Param#FRAME_COUNT}</code> parameters for test suite generation */
    public static final List<Integer>       LST_CNT_FRMS_2 = List.of(5, 10);
    
    /** Example list of <code>{@link Param#FRAME_COUNT}</code> parameters for test suite generation */
    public static final List<Integer>       LST_CNT_FRMS_3 = List.of(5, 10, 15);

    
    /** Example list of <code>{@link Param#SAMPLE_COUNT}</code> parameters for test suite generation */
    public static final List<Integer>       LST_CNT_SMPLS_1 = List.of(100);
    
    /** Example list of <code>{@link Param#SAMPLE_COUNT}</code> parameters for test suite generation */
    public static final List<Integer>       LST_CNT_SMPLS_2 = List.of(100, 500);
    
    /** Example list of <code>{@link Param#SAMPLE_COUNT}</code> parameters for test suite generation */
    public static final List<Integer>       LST_CNT_SMPLS_3 = List.of(100, 500, 1000);
    
    
    /** Example list of <code>{@link Param#SAMPLE_PERIOD}</code> parameters for test suite generation */
    public static final List<Duration>      LST_PERIOD_1 = List.of(Duration.ofMillis(1));
    
    /** Example list of <code>{@link Param#SAMPLE_PERIOD}</code> parameters for test suite generation */
    public static final List<Duration>      LST_PERIOD_2 = List.of(Duration.ofMillis(1), Duration.ofMillis(10));
    
    /** Example list of <code>{@link Param#SAMPLE_PERIOD}</code> parameters for test suite generation */
    public static final List<Duration>      LST_PERIOD_3 = List.of(Duration.ofMillis(1), Duration.ofMillis(10), Duration.ofMillis(100));
    
    
    /** Example list of <code>{@link Param#TIMESTAMP_CASE}</code> parameters for test suite generation */
    public static final List<DpTimestampCase>   LST_TMS_CASE_1 = List.of(DpTimestampCase.SAMPLING_CLOCK);
    
    /** Example list of <code>{@link Param#TIMESTAMP_CASE}</code> parameters for test suite generation */
    public static final List<DpTimestampCase>   LST_TMS_CASE_2 = List.of(DpTimestampCase.SAMPLING_CLOCK, DpTimestampCase.TIMESTAMP_LIST);
    
    
    /** Example list of <code>{@link Param#COLUMN_COUNT}</code> parameters for test suite generation */
    public static final List<Integer>       LST_CNT_COLS_1 = List.of(1);
    
    /** Example list of <code>{@link Param#COLUMN_COUNT}</code> parameters for test suite generation */
    public static final List<Integer>       LST_CNT_COLS_2 = List.of(1, 10);
    
    /** Example list of <code>{@link Param#COLUMN_COUNT}</code> parameters for test suite generation */
    public static final List<Integer>       LST_CNT_COLS_3 = List.of(1, 10, 100);
    
    
    /** Example list of <code>{@link Param#COLUMN_NAME}</code> parameters for test suite generation */
    public static final List<String>        LST_NM_COLS_1 = List.of("Cols1:");
    
    /** Example list of <code>{@link Param#COLUMN_NAME}</code> parameters for test suite generation */
    public static final List<String>        LST_NM_COLS_2 = List.of("Cols1:", "Cols2:");
    
    /** Example list of <code>{@link Param#COLUMN_NAME}</code> parameters for test suite generation */
    public static final List<String>        LST_NM_COLS_3 = List.of("Cols1:", "Cols2:", "Cols3:");
    
    
    /** Example list of <code>{@link Param#COLUMN_TYPE}</code> parameters for test suite generation */
    public static final List<DpSupportedType>   LST_TYPE_COLS_1 = List.of(DpSupportedType.IMAGE);
    
    /** Example list of <code>{@link Param#COLUMN_TYPE}</code> parameters for test suite generation */
    public static final List<DpSupportedType>   LST_TYPE_COLS_2 = List.of(DpSupportedType.IMAGE, DpSupportedType.ARRAY);
    
    /** Example list of <code>{@link Param#COLUMN_TYPE}</code> parameters for test suite generation */
    public static final List<DpSupportedType>   LST_TYPE_COLS_3 = List.of(DpSupportedType.IMAGE, DpSupportedType.ARRAY, DpSupportedType.DOUBLE);
    
    
    
    
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
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#TestSuiteGeneratorBase(java.lang.Class)}.
     */
    @Test
    public final void testTestSuiteGeneratorBase() {
        
        // Create TestSuiteGenerator and check configuration
        TestSuiteGeneratorBase<Param, TestCase>  genBase = new TestSuiteGenerator();
        
        Assert.assertFalse(genBase.isValidConfiguration());
        Assert.assertEquals(0, genBase.testCaseCount());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#isValidType(java.lang.Enum, java.lang.Object)}.
     */
    @Test
    public final void testIsValidType() {
        
        // Test Parameters
        final Param     enmFrames = Param.FRAME_COUNT;
        final int       cntFrames = 10;
        final Integer   intFrames = 11;
        final Long      lngFrames = 11L;
        final Double    dblFrames = 23.0;
        
        final Param     enmPeriod = Param.SAMPLE_PERIOD;
        final int       cntPeriod = 1;
        final Integer   intPeriod = 1;
        final Double    dblPeriod = 0.001;
        final Duration  durPeriod = Duration.ofMillis(1);
        
        // Create TestSuiteGenerator and check parameter/value types
        TestSuiteGenerator  genTest = TestSuiteGenerator.from();

        Assert.assertFalse( genTest.isValidType(enmFrames, lngFrames) );
        Assert.assertFalse( genTest.isValidType(enmFrames, dblFrames) );
        Assert.assertTrue( genTest.isValidType(enmFrames, intFrames) );
        Assert.assertTrue( genTest.isValidType(enmFrames, cntFrames) );
        
        Assert.assertFalse( genTest.isValidType(enmPeriod, cntPeriod) );
        Assert.assertFalse( genTest.isValidType(enmPeriod, intPeriod) );
        Assert.assertFalse( genTest.isValidType(enmPeriod, dblPeriod) );
        Assert.assertTrue( genTest.isValidType(enmPeriod, durPeriod) );
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#createTestCase(java.util.Map)}.
     */
    @Test
    public final void testCreateTestCase() {
        
        // Test Parameters
        final List<Map<Param, Object>>  lstCaseMaps = List.of(MAP_CASE_1, MAP_CASE_2, MAP_CASE_3);
        
        // Create TestSuiteGenerator and add case maps
        TestSuiteGenerator  genTest = TestSuiteGenerator.from();
        
        List<TestCase>      lstCaseRecs = new LinkedList<>();
        try {
            for (Map<Param, Object> mapCase : lstCaseMaps) {
                TestCase recCase = genTest.createTestCase(mapCase);
                
                lstCaseRecs.add(recCase);
            }
            
        } catch (Exception e) {
            Assert.fail("TestSuiteGenerator create test case failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
        
        // Check that test suite generator is invalid
        Assert.assertFalse(genTest.isValidConfiguration());
        Assert.assertEquals(lstCaseMaps.size(), lstCaseRecs.size());
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#isValidConfiguration()}.
//     */
//    @Test
//    public final void testIsValidConfiguration() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#testCaseCount()}.
//     */
//    @Test
//    public final void testTestCaseCount() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#unassignedParameters()}.
     */
    @Test
    public final void testMissingValues() {
        
        // Test Parameters
        final int               cntFrames = 5;
        final int               cntSmpls = 1000;
        final Duration          durPeriod = Duration.ofMillis(1);
        final DpTimestampCase   enmTmsCase = DpTimestampCase.SAMPLING_CLOCK;
        final int               cntCols = 1000;
        final String            strNmPref = "Col:";
        final DpSupportedType   enmColType = DpSupportedType.DOUBLE;
        
        final Set<Param>        setParamsAll = EnumSet.allOf(Param.class);
        
        // Create test suite generator then add parameters one-by-one, checking configuration
        TestSuiteGenerator      genTest = TestSuiteGenerator.from();
        
        Set<Param>  setMissing = setParamsAll;
        Assert.assertFalse(genTest.isValidConfiguration());
        Assert.assertEquals(setMissing, genTest.unassignedParameters());
        Assert.assertEquals(0, genTest.testCaseCount());
        
        // Frame count
        genTest.addParameterValue(Param.FRAME_COUNT, cntFrames);

        setMissing.remove(Param.FRAME_COUNT);
        Assert.assertFalse(genTest.isValidConfiguration());
        Assert.assertEquals(setMissing, genTest.unassignedParameters());
        Assert.assertEquals(0, genTest.testCaseCount());
        
        // Sample count
        genTest.addParameterValue(Param.SAMPLE_COUNT, cntSmpls);
        
        setMissing.remove(Param.SAMPLE_COUNT);
        Assert.assertFalse(genTest.isValidConfiguration());
        Assert.assertEquals(setMissing, genTest.unassignedParameters());
        Assert.assertEquals(0, genTest.testCaseCount());
        
        // Sample period
        genTest.addParameterValue(Param.SAMPLE_PERIOD, durPeriod);
        
        setMissing.remove(Param.SAMPLE_PERIOD);
        Assert.assertFalse(genTest.isValidConfiguration());
        Assert.assertEquals(setMissing, genTest.unassignedParameters());
        Assert.assertEquals(0, genTest.testCaseCount());

        // Timestamp case
        genTest.addParameterValue(Param.TIMESTAMP_CASE, enmTmsCase);
        
        setMissing.remove(Param.TIMESTAMP_CASE);
        Assert.assertFalse(genTest.isValidConfiguration());
        Assert.assertEquals(setMissing, genTest.unassignedParameters());
        Assert.assertEquals(0, genTest.testCaseCount());
        
        // Column Count
        genTest.addParameterValue(Param.COLUMN_COUNT, cntCols);
        
        setMissing.remove(Param.COLUMN_COUNT);
        Assert.assertFalse(genTest.isValidConfiguration());
        Assert.assertEquals(setMissing, genTest.unassignedParameters());
        Assert.assertEquals(0, genTest.testCaseCount());
        
        // Column name
        genTest.addParameterValue(Param.COLUMN_NAME, strNmPref);
        
        setMissing.remove(Param.COLUMN_NAME);
        Assert.assertFalse(genTest.isValidConfiguration());
        Assert.assertEquals(setMissing, genTest.unassignedParameters());
        Assert.assertEquals(0, genTest.testCaseCount());
        
        // Column data type
        genTest.addParameterValue(Param.COLUMN_TYPE, enmColType);
        
        setMissing.remove(Param.COLUMN_TYPE);
        Assert.assertTrue(genTest.isValidConfiguration());
        Assert.assertEquals(setMissing, genTest.unassignedParameters());
        Assert.assertEquals(1, genTest.testCaseCount());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#addParameterValue(java.lang.Enum, java.lang.Object)}.
     */
    @Test
    public final void testAddParameterValue() {
        
        // Test Parameters
        final Param     enmParam = Param.FRAME_COUNT;
        final int       cntFrames = 10;
        final Integer   intFrames = 11;
        final Long      lngFrames = 11L;
        
        // Create TestSuiteGenerator and check values
        TestSuiteGenerator  genTest = TestSuiteGenerator.from();
        
        try {
            genTest.addParameterValue(enmParam, cntFrames);
            genTest.addParameterValue(enmParam, intFrames);
        
        } catch (Exception e) {
            Assert.fail("Parameter addition failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
        
        try {
            genTest.addParameterValue(enmParam, lngFrames);

            Assert.fail("addParameterValue() succeeded for inconsistent parameter type " + enmParam);

        } catch (Exception e) {

        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#createTestSuit()}.
     */
    @Test
    public final void testCreateTestSuit1() {
        
        // Test Parameter
        final   List<Object>   lstCntFrames = LST_CNT_FRMS_1.stream().<Object>map(i -> i).toList();
        final   List<Object>   lstCntSmpls = LST_CNT_SMPLS_1.stream().<Object>map(i -> i).toList();
        final   List<Object>   lstPeriods = LST_PERIOD_1.stream().<Object>map(dur -> dur).toList();
        final   List<Object>   lstTmsCases = LST_TMS_CASE_1.stream().<Object>map(enm -> enm).toList();
        final   List<Object>   lstCntCols = LST_CNT_COLS_1.stream().<Object>map(i -> i).toList();
        final   List<Object>   lstNmCols = LST_NM_COLS_1.stream().<Object>map(str -> str).toList();
        final   List<Object>   lstTypeCols = LST_TYPE_COLS_1.stream().<Object>map(enm -> enm).toList();
        
        final   Map<Param, List<Object>> mapParamToVals = Map.of(Param.FRAME_COUNT, lstCntFrames,
                                                                 Param.SAMPLE_COUNT, lstCntSmpls,
                                                                 Param.SAMPLE_PERIOD, lstPeriods,
                                                                 Param.TIMESTAMP_CASE, lstTmsCases,
                                                                 Param.COLUMN_COUNT, lstCntCols,
                                                                 Param.COLUMN_NAME, lstNmCols,
                                                                 Param.COLUMN_TYPE, lstTypeCols
                                                        );

        final   int     cntCases = mapParamToVals.values().stream().mapToInt(lst -> lst.size()).reduce(1, (i1, i2) -> i1*i2);
        
        // Create test suite generator and configure
        TestSuiteGenerator  genTest = TestSuiteGenerator.from();

        try {
            int     cntParams = Param.values().length;
            int     iParam = 0;
            for (Map.Entry<Param, List<Object>> entry : mapParamToVals.entrySet()) {
                Param           enmParam = entry.getKey();
                List<Object>    lstVals = entry.getValue();

                genTest.addParameterValues(enmParam, lstVals);
                iParam++;
                
                if (iParam < cntParams) {
                    Assert.assertFalse(genTest.isValidConfiguration());
                    Assert.assertEquals(0, genTest.testCaseCount());
                }
            }
            
            Assert.assertTrue(genTest.isValidConfiguration());
            Assert.assertEquals(cntCases, genTest.testCaseCount());

        } catch (Exception e) {
            Assert.fail("Parameter value addition failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }

        // Print out the test suite configuration (tests TestSuiteGeneratorBase#toString())
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("TestSuiteGenerator Configuration");
        System.out.println(genTest);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#createTestSuit()}.
     */
    @Test
    public final void testCreateTestSuit2() {
        
        // Test Parameter
        final   List<Object>   lstCntFrames = LST_CNT_FRMS_1.stream().<Object>map(i -> i).toList();
        final   List<Object>   lstCntSmpls = LST_CNT_SMPLS_1.stream().<Object>map(i -> i).toList();
        final   List<Object>   lstPeriods = LST_PERIOD_1.stream().<Object>map(dur -> dur).toList();
        final   List<Object>   lstTmsCases = LST_TMS_CASE_1.stream().<Object>map(enm -> enm).toList();
        final   List<Object>   lstCntCols = LST_CNT_COLS_2.stream().<Object>map(i -> i).toList();
        final   List<Object>   lstNmCols = LST_NM_COLS_2.stream().<Object>map(str -> str).toList();
        final   List<Object>   lstTypeCols = LST_TYPE_COLS_2.stream().<Object>map(enm -> enm).toList();
        
        final   Map<Param, List<Object>> mapParamToVals = Map.of(Param.FRAME_COUNT, lstCntFrames,
                                                                 Param.SAMPLE_COUNT, lstCntSmpls,
                                                                 Param.SAMPLE_PERIOD, lstPeriods,
                                                                 Param.TIMESTAMP_CASE, lstTmsCases,
                                                                 Param.COLUMN_COUNT, lstCntCols,
                                                                 Param.COLUMN_NAME, lstNmCols,
                                                                 Param.COLUMN_TYPE, lstTypeCols
                                                        );

        final   int     cntCases = mapParamToVals.values().stream().mapToInt(lst -> lst.size()).reduce(1, (i1, i2) -> i1*i2);
        
        // Create test suite generator and configure
        TestSuiteGenerator  genTest = TestSuiteGenerator.from();

        try {
            int     cntParams = Param.values().length;
            int     iParam = 0;
            for (Map.Entry<Param, List<Object>> entry : mapParamToVals.entrySet()) {
                Param           enmParam = entry.getKey();
                List<Object>    lstVals = entry.getValue();

                genTest.addParameterValues(enmParam, lstVals);
                iParam++;
                
                if (iParam < cntParams) {
                    Assert.assertFalse(genTest.isValidConfiguration());
                    Assert.assertEquals(0, genTest.testCaseCount());
                }
            }
            
            Assert.assertTrue(genTest.isValidConfiguration());
            Assert.assertEquals(cntCases, genTest.testCaseCount());

        } catch (Exception e) {
            Assert.fail("Parameter value addition failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
        
        // Create the test suite
        Collection<TestCase>    conCases; 
        try {
            conCases = genTest.createTestSuit();
            
            Assert.assertEquals(cntCases, conCases.size());
            
        } catch (Exception e) {
            Assert.fail("Test suite creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
            return;
        }

        // Print out the test suite configuration (tests TestSuiteGeneratorBase#toString())
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("TestSuiteGenerator Configuration");
        System.out.println(genTest);
        
        System.out.println("Test Case Collection");
        for (TestCase recCase : conCases) 
            System.out.println(recCase);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#createTestSuit()}.
     */
    @Test
    public final void testCreateTestSuit3() {
        
        // Test Parameter
        final   List<Object>   lstCntFrames = LST_CNT_FRMS_3.stream().<Object>map(i -> i).toList();
        final   List<Object>   lstCntSmpls = LST_CNT_SMPLS_3.stream().<Object>map(i -> i).toList();
        final   List<Object>   lstPeriods = LST_PERIOD_1.stream().<Object>map(dur -> dur).toList();
        final   List<Object>   lstTmsCases = LST_TMS_CASE_2.stream().<Object>map(enm -> enm).toList();
        final   List<Object>   lstCntCols = LST_CNT_COLS_2.stream().<Object>map(i -> i).toList();
        final   List<Object>   lstNmCols = LST_NM_COLS_1.stream().<Object>map(str -> str).toList();
        final   List<Object>   lstTypeCols = LST_TYPE_COLS_1.stream().<Object>map(enm -> enm).toList();
        
        final   Map<Param, List<Object>> mapParamToVals = Map.of(Param.FRAME_COUNT, lstCntFrames,
                                                                 Param.SAMPLE_COUNT, lstCntSmpls,
                                                                 Param.SAMPLE_PERIOD, lstPeriods,
                                                                 Param.TIMESTAMP_CASE, lstTmsCases,
                                                                 Param.COLUMN_COUNT, lstCntCols,
                                                                 Param.COLUMN_NAME, lstNmCols,
                                                                 Param.COLUMN_TYPE, lstTypeCols
                                                        );

        final   int     cntCases = mapParamToVals.values().stream().mapToInt(lst -> lst.size()).reduce(1, (i1, i2) -> i1*i2);
        
        // Create test suite generator and configure
        TestSuiteGenerator  genTest = TestSuiteGenerator.from();

        try {
            int     cntParams = Param.values().length;
            int     iParam = 0;
            for (Map.Entry<Param, List<Object>> entry : mapParamToVals.entrySet()) {
                Param           enmParam = entry.getKey();
                List<Object>    lstVals = entry.getValue();

                genTest.addParameterValues(enmParam, lstVals);
                iParam++;
                
                if (iParam < cntParams) {
                    Assert.assertFalse(genTest.isValidConfiguration());
                    Assert.assertEquals(0, genTest.testCaseCount());
                }
            }
            
            Assert.assertTrue(genTest.isValidConfiguration());
            Assert.assertEquals(cntCases, genTest.testCaseCount());

        } catch (Exception e) {
            Assert.fail("Parameter value addition failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
        
        // Create the test suite
        Collection<TestCase>    conCases; 
        try {
            conCases = genTest.createTestSuit();
            
            Assert.assertEquals(cntCases, conCases.size());
            
        } catch (Exception e) {
            Assert.fail("Test suite creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
            return;
        }

        // Print out the test suite configuration (tests TestSuiteGeneratorBase#toString())
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
        System.out.println("TestSuiteGenerator Configuration");
        System.out.println(genTest);
        
        System.out.println("Test Case Collection");
        for (TestCase recCase : conCases) 
            System.out.println(recCase);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#equals(java.lang.Object)}.
     */
    @Test
    public final void testEqualsObject() {
        
        // Test Parameters
        final int       cntFrames = 10;
        final int       cntFramesAdd = 20;
        final int       cntSmpls = 1000;
        final Duration  durPeriod = Duration.ofMillis(10);
        final int       cntCols = 100;
        
        // Create 2 test suite generators, configure the same and check equivalence
        TestSuiteGenerator  genTest1 = TestSuiteGenerator.from();
        genTest1.addParameterValue(Param.FRAME_COUNT, cntFrames);
        genTest1.addParameterValue(Param.SAMPLE_COUNT, cntSmpls);
        genTest1.addParameterValue(Param.SAMPLE_PERIOD, durPeriod);
        genTest1.addParameterValue(Param.COLUMN_COUNT, cntCols);
        
        Assert.assertFalse(genTest1.isValidConfiguration());
        Assert.assertEquals(cntFrames, genTest1.parameterValues(Param.FRAME_COUNT).get(0));
        Assert.assertEquals(cntSmpls, genTest1.parameterValues(Param.SAMPLE_COUNT).get(0));
        Assert.assertEquals(durPeriod, genTest1.parameterValues(Param.SAMPLE_PERIOD).get(0));
        Assert.assertEquals(cntCols, genTest1.parameterValues(Param.COLUMN_COUNT).get(0));

        TestSuiteGenerator  genTest2 = TestSuiteGenerator.from();
        genTest2.addParameterValue(Param.FRAME_COUNT, cntFrames);
        genTest2.addParameterValue(Param.SAMPLE_COUNT, cntSmpls);
        genTest2.addParameterValue(Param.SAMPLE_PERIOD, durPeriod);
        genTest2.addParameterValue(Param.COLUMN_COUNT, cntCols);
        
        Assert.assertFalse(genTest2.isValidConfiguration());
        Assert.assertEquals(cntFrames, genTest2.parameterValues(Param.FRAME_COUNT).get(0));
        Assert.assertEquals(cntSmpls, genTest2.parameterValues(Param.SAMPLE_COUNT).get(0));
        Assert.assertEquals(durPeriod, genTest2.parameterValues(Param.SAMPLE_PERIOD).get(0));
        Assert.assertEquals(cntCols, genTest2.parameterValues(Param.COLUMN_COUNT).get(0));
        
        Assert.assertEquals(genTest1, genTest2);
        
        genTest1.addParameterValue(Param.FRAME_COUNT, cntFramesAdd);
        Assert.assertEquals(cntFramesAdd, genTest1.parameterValues(Param.FRAME_COUNT).get(1));

        Assert.assertNotEquals(genTest1, genTest2);
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#toString()}.
//     */
//    @Test
//    public final void testToString() {
//        fail("Not yet implemented"); // TODO
//    }

}
