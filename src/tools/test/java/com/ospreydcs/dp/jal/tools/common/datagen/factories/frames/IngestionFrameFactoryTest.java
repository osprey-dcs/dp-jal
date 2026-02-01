/*
 * Project: dp-jal
 * File:	IngestionFrameFactoryTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.frames
 * Type: 	IngestionFrameFactoryTest
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
 * @since Jan 14, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.frames;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.naming.ConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.common.DpTimestampCase;
import com.ospreydcs.dp.jal.common.UniformSamplingClock;
import com.ospreydcs.dp.jal.ingest.IngestionFrame;
import com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameColumnsFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ByteArrayFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ImageFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.cols.JalToolsColumnsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.frames.JalToolsFramesConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for class <code>IngestionFrameFactory</code>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 14, 2026
 *
 */
public class IngestionFrameFactoryTest {

    
    //
    // JAL Library Resources
    //
    
    /** Default configuration parameters for the JAL Tools */
    private static final JalToolsFramesConfig     CFG_DEF = JalToolsConfig.getInstance().datagen.frame;
    
    
    //
    // Test Constants
    //
    
    /** Name of the frame generator */
    public static final String  STR_SRC_NAME = IngestionFrameFactory.class.getSimpleName();
    
    /** Environment variable for current user */
    public static final String  STR_USERNAME = "USER";
    
    
    /** Enable/disable default tag values flag default configuration */
    public static final boolean                 BOL_TAGS_DEF_ENBL = CFG_DEF.tags.useDefault;
    
    /** Enable/disable default attribute pairs default configuration */
    public static final boolean                 BOL_ATTRS_DEF_ENBL = CFG_DEF.attributes.useDefault;
    
    /** Enable/disable class tag values flag default configuration */
    public static final boolean                 BOL_TAGS_CLS_ENBL = CFG_DEF.tags.useClass;
    
    /** Enable/disable class attribute pairs flag default configuration */
    public static final boolean                 BOL_ATTRS_CLS_ENBL = CFG_DEF.attributes.useClass;
    
    
    //
    // Test Resources
    //
    
    /** Default ingestion frame tag values */
    private static final Set<String>            SET_FRM_TAGS_DEF = new TreeSet<>( CFG_DEF.tags.values );
    
    /** Default ingestion frame attribute pairs */
    private static final Map<String, String>    MAP_FRM_ATTRS_DEF = new HashMap<>( CFG_DEF.attributes.pairs );
    
    
    /** Class ingestion frame tag values */
    private static final Set<String>            SET_FRM_TAGS_CLS = new TreeSet<>();
    
    /** Class ingestion frame attribute pairs */
    private static final Map<String, String>    MAP_FRM_ATTRS_CLS = new HashMap<>();
    
    
    /** Initialization for class tags and attributes for ingestion frames */
    static {
        SET_FRM_TAGS_CLS.add(STR_SRC_NAME);
        
        String  strUser = System.getenv(STR_USERNAME);
        Instant insNow = Instant.now();
        
        MAP_FRM_ATTRS_CLS.put("Source", STR_SRC_NAME);
        MAP_FRM_ATTRS_CLS.put("Initiated", insNow.toString());
        MAP_FRM_ATTRS_CLS.put("User", strUser);
    }
    
    
    /** List of attribute names to ignore in comparison tests (values will differ from default) */
    public static final List<String>            LST_ATTR_NMS_IGNORE = List.of("Initiated");
    
    
    /** Parsing creator argument collection */
    public static final String[]        ARR_ARGS_PARSE_1 = { };
    
    /** Parsing creator argument collection */
    public static final String[]        ARR_ARGS_PARSE_2 = { "--tags", "tag1", "tag2", "-Anm1=val1", "-Anm2=val2", 
                                                             "--tms", "100", "PT0.001S", "2026-01-12T17:48:00Z", "TIMESTAMP_LIST", "PT0.003S",
                                                             "--cols", "2", "Cols1:", "IMAGE",
                                                             "--tags", "tag3"
                                                             };

    /** Parsing creator argument collection */
    public static final String[]        ARR_ARGS_PARSE_3 = { "-tagsDef", "-attrsDef",  
                                                             "--tms", "100", "PT0.001S", "2026-01-12T17:48:00Z", "TIMESTAMP_LIST", "PT0.003S",
                                                             "--cols", "2", "Cols1:", "IMAGE",
                                                             "--cols", "100", "Cols2:", "SCALAR", "DOUBLE",
                                                             "--tags", "tag3"
                                                             };
    /** Parsing creator argument collection */
    public static final String[]        ARR_ARGS_PARSE_4 = { "-tagsCls", "-tagsDef", "-attrsDef",   
                                                             "--tms", "100", "PT0.001S", "2026-01-12T17:48:00Z", "TIMESTAMP_LIST", "PT0.003S",
                                                             "--cols", "2", "Cols1:", "IMAGE",
                                                             "--cols", "1", "Cols2:", "STRUCTURE", "4", "2", "true", "INTEGER", "false", "0", "2",
                                                             "--cols", "100", "Cols3:", "SCALAR", "DOUBLE",
                                                             "--tags", "tag3", "-Anm1=val1", "-Anm2=val2"
                                                             };

    
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
    //  Test Cases
    //
    
    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#from()}.
     * @throws NoSuchElementException 
     * @throws ConfigurationException 
     * @throws MissingResourceException 
     * @throws UnsupportedOperationException 
     * @throws TypeNotPresentException 
     * @throws DateTimeParseException 
     * @throws NumberFormatException 
     */
    @Test
    public final void testFrom() throws NumberFormatException, DateTimeParseException, TypeNotPresentException, UnsupportedOperationException, MissingResourceException, ConfigurationException, NoSuchElementException {
        
        // Test Parameters
        final int                   cntSamples = CFG_DEF.timestamps.count;
        final DpTimestampCase       enmTmsCase = CFG_DEF.timestamps.type;
        final int                   cntCols = CFG_DEF.columns.stream().mapToInt(cfg -> cfg.count).sum();
        final Set<String>           setColNms = CFG_DEF.columns.stream().flatMap(cfg -> IntStream.range(0, cfg.count).<String>mapToObj(i -> cfg.name + Integer.toString(i))).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        final Set<String>           setTags = Set.of();
        final Map<String, String>   mapAttrs = Map.of();
        
        final Collection<IFrameColumnsFactory<Object>> conColsFacs = IngestionFrameFactoryTest.extractDefaultFrameColumns(); // throws exceptions
        final Set<DpSupportedType>          setColTypes = conColsFacs.stream().<DpSupportedType>map(fac -> fac.getColumnType()).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        final Map<DpSupportedType, Integer> mapColTypeToCnt = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnCount()));
        final Map<DpSupportedType, Set<String>> mapColTypeToNms = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnNames()));
        
        // Create ingestion frame factory and check configuration
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.from();
            
            Assert.assertEquals(cntSamples, facTest.getSampleCount());
            Assert.assertEquals(cntCols, facTest.getColumnCount());
            Assert.assertEquals(enmTmsCase, facTest.getTimestampType());
            Assert.assertEquals(setColNms, facTest.retrieveColumnNames());
            Assert.assertEquals(setTags, facTest.getTags());
            Assert.assertEquals(mapAttrs, facTest.getAttributes());
            Assert.assertEquals(setColTypes, facTest.retrieveColumnTypes());
            Assert.assertEquals(mapColTypeToCnt, facTest.retrieveColumnTypeCount());
            Assert.assertEquals(mapColTypeToNms, facTest.retrieveColumnTypeNames());
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#from(java.util.Collection)}.
     */
    @Test
    public final void testFromCollectionOfIFrameColumnsFactoryOfObject() {
        
        // Test Parameters
        final int                   cntSamples = CFG_DEF.timestamps.count;
        final DpTimestampCase       enmTmsCase = CFG_DEF.timestamps.type;
        final int                   cntCols = CFG_DEF.columns.stream().mapToInt(cfg -> cfg.count).sum();
        final String                strNmPref = "JUnit:";
        final Set<String>           setColNms = IntStream.range(0, cntCols).<String>mapToObj(i -> strNmPref + Integer.toString(i)).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        final Set<String>           setTags = Set.of();
        final Map<String, String>   mapAttrs = Map.of();
        final ScalarFactory         facValues = ScalarFactory.from();
        
        final Collection<IFrameColumnsFactory<Object>> conColsFacs = Set.of( FrameColumnsFactory.from(cntCols, strNmPref, facValues) );
        final Set<DpSupportedType>          setColTypes = conColsFacs.stream().<DpSupportedType>map(fac -> fac.getColumnType()).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        final Map<DpSupportedType, Integer> mapColTypeToCnt = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnCount()));
        final Map<DpSupportedType, Set<String>> mapColTypeToNms = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnNames()));
        
        // Create ingestion frame factory and check configuration
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.from(conColsFacs);
            
            Assert.assertEquals(cntSamples, facTest.getSampleCount());
            Assert.assertEquals(cntCols, facTest.getColumnCount());
            Assert.assertEquals(enmTmsCase, facTest.getTimestampType());
            Assert.assertEquals(setColNms, facTest.retrieveColumnNames());
            Assert.assertEquals(setTags, facTest.getTags());
            Assert.assertEquals(mapAttrs, facTest.getAttributes());
            Assert.assertEquals(setColTypes, facTest.retrieveColumnTypes());
            Assert.assertEquals(mapColTypeToCnt, facTest.retrieveColumnTypeCount());
            Assert.assertEquals(mapColTypeToNms, facTest.retrieveColumnTypeNames());
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#from(com.ospreydcs.dp.jal.tools.common.datagen.IFrameTimestampsFactory)}.
     * @throws NoSuchElementException 
     * @throws ConfigurationException 
     * @throws MissingResourceException 
     * @throws UnsupportedOperationException 
     * @throws TypeNotPresentException 
     * @throws DateTimeParseException 
     * @throws NumberFormatException 
     */
    @Test
    public final void testFromIFrameTimestampsFactory() throws NumberFormatException, DateTimeParseException, TypeNotPresentException, UnsupportedOperationException, MissingResourceException, ConfigurationException, NoSuchElementException {
        
        // Test Parameters
        final int                   cntSamples = 42;
        final Duration              durPeriod = Duration.ofMillis(1);
        final Instant               insStart = Instant.now();
        final DpTimestampCase       enmTmsCase = DpTimestampCase.TIMESTAMP_LIST;
        final Duration              durDelay = Duration.ofMillis(3);
        final FrameTimestampsFactory facTms = FrameTimestampsFactory.from(cntSamples, durPeriod, insStart, enmTmsCase, durDelay);
        
        final int                   cntCols = CFG_DEF.columns.stream().mapToInt(cfg -> cfg.count).sum();
        final Set<String>           setColNms = CFG_DEF.columns.stream().flatMap(cfg -> IntStream.range(0, cfg.count).<String>mapToObj(i -> cfg.name + Integer.toString(i))).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        final Set<String>           setTags = Set.of();
        final Map<String, String>   mapAttrs = Map.of();
        
        final Collection<IFrameColumnsFactory<Object>> conColsFacs = IngestionFrameFactoryTest.extractDefaultFrameColumns(); // throws exceptions
        final Set<DpSupportedType>          setColTypes = conColsFacs.stream().<DpSupportedType>map(fac -> fac.getColumnType()).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        final Map<DpSupportedType, Integer> mapColTypeToCnt = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnCount()));
        final Map<DpSupportedType, Set<String>> mapColTypeToNms = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnNames()));
        
        // Create ingestion frame factory and check configuration
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.from(facTms);
            
            Assert.assertEquals(cntSamples, facTest.getSampleCount());
            Assert.assertEquals(cntCols, facTest.getColumnCount());
            Assert.assertEquals(enmTmsCase, facTest.getTimestampType());
            Assert.assertEquals(setColNms, facTest.retrieveColumnNames());
            Assert.assertEquals(setTags, facTest.getTags());
            Assert.assertEquals(mapAttrs, facTest.getAttributes());
            Assert.assertEquals(setColTypes, facTest.retrieveColumnTypes());
            Assert.assertEquals(mapColTypeToCnt, facTest.retrieveColumnTypeCount());
            Assert.assertEquals(mapColTypeToNms, facTest.retrieveColumnTypeNames());
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
        
        // Create ingestion frame factory and check configuration
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.from(facTms);
            
            Assert.assertEquals(cntSamples, facTest.getSampleCount());
            Assert.assertEquals(cntCols, facTest.getColumnCount());
            Assert.assertEquals(enmTmsCase, facTest.getTimestampType());
            Assert.assertEquals(setColNms, facTest.retrieveColumnNames());
            Assert.assertEquals(setTags, facTest.getTags());
            Assert.assertEquals(mapAttrs, facTest.getAttributes());
            Assert.assertEquals(setColTypes, facTest.retrieveColumnTypes());
            Assert.assertEquals(mapColTypeToCnt, facTest.retrieveColumnTypeCount());
            Assert.assertEquals(mapColTypeToNms, facTest.retrieveColumnTypeNames());
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#from(com.ospreydcs.dp.jal.tools.common.datagen.IFrameTimestampsFactory, java.util.Collection)}.
     */
    @Test
    public final void testFromIFrameTimestampsFactoryCollectionOfIFrameColumnsFactoryOfObject() {
        
        // Test Parameters
        final int                   cntSamples = 42;
        final Duration              durPeriod = Duration.ofMillis(1);
        final Instant               insStart = Instant.now();
        final DpTimestampCase       enmTmsCase = DpTimestampCase.TIMESTAMP_LIST;
        final Duration              durDelay = Duration.ofMillis(3);
        final FrameTimestampsFactory facTms = FrameTimestampsFactory.from(cntSamples, durPeriod, insStart, enmTmsCase, durDelay);
        
        final int                   cntCols1 = 2;
        final String                strNmPref1 = "Cols1:";
        final IDatumFactory         facVals1 = StructureFactory.from(3, 2, true, JalScalarType.INTEGER, true, 1);
        final IFrameColumnsFactory<Object>  facCols1 = FrameColumnsFactory.from(cntCols1, strNmPref1, facVals1);
        
        final int                   cntCols2 = 100;
        final String                strNmPref2 = "Cols2:";
        final IDatumFactory         facVals2 = ScalarFactory.from(JalScalarType.DOUBLE, false, 1, 0.1);
        final IFrameColumnsFactory<Object>  facCols2 = FrameColumnsFactory.from(cntCols2, strNmPref2, facVals2);
        
        final Collection<IFrameColumnsFactory<Object>> conColsFacs = Set.of( facCols1, facCols2 );
        
        final int                   cntColsTot = cntCols1 + cntCols2;
        final Set<String>           setTags = Set.of();
        final Map<String, String>   mapAttrs = Map.of();
        
        final Set<String>                   setColNms = Stream.concat(IntStream.range(0, cntCols1).<String>mapToObj(i -> strNmPref1 + Integer.toString(i)), IntStream.range(0, cntCols2).<String>mapToObj(i -> strNmPref2 + Integer.toString(i))).collect(Collectors.toCollection(TreeSet::new));
        final Set<DpSupportedType>          setColTypes = conColsFacs.stream().<DpSupportedType>map(fac -> fac.getColumnType()).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        final Map<DpSupportedType, Integer> mapColTypeToCnt = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnCount()));
        final Map<DpSupportedType, Set<String>> mapColTypeToNms = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnNames()));
        
        // Create ingestion frame factory and check configuration
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.from(facTms, conColsFacs);
            
            Assert.assertEquals(cntSamples, facTest.getSampleCount());
            Assert.assertEquals(cntColsTot, facTest.getColumnCount());
            Assert.assertEquals(enmTmsCase, facTest.getTimestampType());
            Assert.assertEquals(setColNms, facTest.retrieveColumnNames());
            Assert.assertEquals(setTags, facTest.getTags());
            Assert.assertEquals(mapAttrs, facTest.getAttributes());
            Assert.assertEquals(setColTypes, facTest.retrieveColumnTypes());
            Assert.assertEquals(mapColTypeToCnt, facTest.retrieveColumnTypeCount());
            Assert.assertEquals(mapColTypeToNms, facTest.retrieveColumnTypeNames());
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#from(java.util.Set, com.ospreydcs.dp.jal.tools.common.datagen.IFrameTimestampsFactory, java.util.Collection)}.
     */
    @Test
    public final void testFromSetOfStringIFrameTimestampsFactoryCollectionOfIFrameColumnsFactoryOfObject() {
        
        // Test Parameters
        final Set<String>           setTags = Set.of("tag1", "tag2", "tag3");
        final Map<String, String>   mapAttrs = Map.of();
        
        final int                   cntSamples = 42;
        final Duration              durPeriod = Duration.ofMillis(1);
        final Instant               insStart = Instant.now();
        final DpTimestampCase       enmTmsCase = DpTimestampCase.TIMESTAMP_LIST;
        final Duration              durDelay = Duration.ofMillis(3);
        final FrameTimestampsFactory facTms = FrameTimestampsFactory.from(cntSamples, durPeriod, insStart, enmTmsCase, durDelay);
        
        final int                   cntCols1 = 2;
        final String                strNmPref1 = "Cols1:";
        final IDatumFactory         facVals1 = StructureFactory.from(3, 2, true, JalScalarType.INTEGER, true, 1);
        final IFrameColumnsFactory<Object>  facCols1 = FrameColumnsFactory.from(cntCols1, strNmPref1, facVals1);
        
        final int                   cntCols2 = 100;
        final String                strNmPref2 = "Cols2:";
        final IDatumFactory         facVals2 = ScalarFactory.from(JalScalarType.DOUBLE, false, 1, 0.1);
        final IFrameColumnsFactory<Object>  facCols2 = FrameColumnsFactory.from(cntCols2, strNmPref2, facVals2);
        
        final Collection<IFrameColumnsFactory<Object>> conColsFacs = Set.of( facCols1, facCols2 );
        
        final int                   cntColsTot = cntCols1 + cntCols2;
        final Set<String>                   setColNms = Stream.concat(IntStream.range(0, cntCols1).<String>mapToObj(i -> strNmPref1 + Integer.toString(i)), IntStream.range(0, cntCols2).<String>mapToObj(i -> strNmPref2 + Integer.toString(i))).collect(Collectors.toCollection(TreeSet::new));
        final Set<DpSupportedType>          setColTypes = conColsFacs.stream().<DpSupportedType>map(fac -> fac.getColumnType()).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        final Map<DpSupportedType, Integer> mapColTypeToCnt = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnCount()));
        final Map<DpSupportedType, Set<String>> mapColTypeToNms = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnNames()));
        
        // Create ingestion frame factory and check configuration
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.from(setTags, facTms, conColsFacs);
            
            Assert.assertEquals(cntSamples, facTest.getSampleCount());
            Assert.assertEquals(cntColsTot, facTest.getColumnCount());
            Assert.assertEquals(enmTmsCase, facTest.getTimestampType());
            Assert.assertEquals(setColNms, facTest.retrieveColumnNames());
            Assert.assertEquals(setTags, facTest.getTags());
            Assert.assertEquals(mapAttrs, facTest.getAttributes());
            Assert.assertEquals(setColTypes, facTest.retrieveColumnTypes());
            Assert.assertEquals(mapColTypeToCnt, facTest.retrieveColumnTypeCount());
            Assert.assertEquals(mapColTypeToNms, facTest.retrieveColumnTypeNames());
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#from(java.util.Map, com.ospreydcs.dp.jal.tools.common.datagen.IFrameTimestampsFactory, java.util.Collection)}.
     */
    @Test
    public final void testFromMapOfStringStringIFrameTimestampsFactoryCollectionOfIFrameColumnsFactoryOfObject() {
        
        // Test Parameters
        final Set<String>           setTags = Set.of();
        final Map<String, String>   mapAttrs = Map.of("name1", "value1", "name2", "value2", "name3", "value3");
        
        final int                   cntSamples = 42;
        final Duration              durPeriod = Duration.ofMillis(1);
        final Instant               insStart = Instant.now();
        final DpTimestampCase       enmTmsCase = DpTimestampCase.TIMESTAMP_LIST;
        final Duration              durDelay = Duration.ofMillis(3);
        final FrameTimestampsFactory facTms = FrameTimestampsFactory.from(cntSamples, durPeriod, insStart, enmTmsCase, durDelay);
        
        final int                   cntCols1 = 2;
        final String                strNmPref1 = "Cols1:";
        final IDatumFactory         facVals1 = StructureFactory.from(3, 2, true, JalScalarType.INTEGER, true, 1);
        final IFrameColumnsFactory<Object>  facCols1 = FrameColumnsFactory.from(cntCols1, strNmPref1, facVals1);
        
        final int                   cntCols2 = 100;
        final String                strNmPref2 = "Cols2:";
        final IDatumFactory         facVals2 = ScalarFactory.from(JalScalarType.DOUBLE, false, 1, 0.1);
        final IFrameColumnsFactory<Object>  facCols2 = FrameColumnsFactory.from(cntCols2, strNmPref2, facVals2);
        
        final Collection<IFrameColumnsFactory<Object>> conColsFacs = Set.of( facCols1, facCols2 );
        
        final int                   cntColsTot = cntCols1 + cntCols2;
        final Set<String>                   setColNms = Stream.concat(IntStream.range(0, cntCols1).<String>mapToObj(i -> strNmPref1 + Integer.toString(i)), IntStream.range(0, cntCols2).<String>mapToObj(i -> strNmPref2 + Integer.toString(i))).collect(Collectors.toCollection(TreeSet::new));
        final Set<DpSupportedType>          setColTypes = conColsFacs.stream().<DpSupportedType>map(fac -> fac.getColumnType()).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        final Map<DpSupportedType, Integer> mapColTypeToCnt = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnCount()));
        final Map<DpSupportedType, Set<String>> mapColTypeToNms = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnNames()));
        
        // Create ingestion frame factory and check configuration
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.from(mapAttrs, facTms, conColsFacs);
            
            Assert.assertEquals(cntSamples, facTest.getSampleCount());
            Assert.assertEquals(cntColsTot, facTest.getColumnCount());
            Assert.assertEquals(enmTmsCase, facTest.getTimestampType());
            Assert.assertEquals(setColNms, facTest.retrieveColumnNames());
            Assert.assertEquals(setTags, facTest.getTags());
            Assert.assertEquals(mapAttrs, facTest.getAttributes());
            Assert.assertEquals(setColTypes, facTest.retrieveColumnTypes());
            Assert.assertEquals(mapColTypeToCnt, facTest.retrieveColumnTypeCount());
            Assert.assertEquals(mapColTypeToNms, facTest.retrieveColumnTypeNames());
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#from(java.util.Set, java.util.Map, com.ospreydcs.dp.jal.tools.common.datagen.IFrameTimestampsFactory, java.util.Collection)}.
     */
    @Test
    public final void testFromSetOfStringMapOfStringStringIFrameTimestampsFactoryCollectionOfIFrameColumnsFactoryOfObject() {
        
        // Test Parameters
        final Set<String>           setTags = Set.of("tag1", "tag2", "tag3");
        final Map<String, String>   mapAttrs = Map.of("name1", "value1", "name2", "value2", "name3", "value3");
        
        final int                   cntSamples = 42;
        final Duration              durPeriod = Duration.ofMillis(1);
        final Instant               insStart = Instant.now();
        final DpTimestampCase       enmTmsCase = DpTimestampCase.TIMESTAMP_LIST;
        final Duration              durDelay = Duration.ofMillis(3);
        final FrameTimestampsFactory facTms = FrameTimestampsFactory.from(cntSamples, durPeriod, insStart, enmTmsCase, durDelay);
        
        final int                   cntCols1 = 2;
        final String                strNmPref1 = "Cols1:";
        final IDatumFactory         facVals1 = StructureFactory.from(3, 2, true, JalScalarType.INTEGER, true, 1);
        final IFrameColumnsFactory<Object>  facCols1 = FrameColumnsFactory.from(cntCols1, strNmPref1, facVals1);
        
        final int                   cntCols2 = 100;
        final String                strNmPref2 = "Cols2:";
        final IDatumFactory         facVals2 = ScalarFactory.from(JalScalarType.DOUBLE, false, 1, 0.1);
        final IFrameColumnsFactory<Object>  facCols2 = FrameColumnsFactory.from(cntCols2, strNmPref2, facVals2);
        
        final Collection<IFrameColumnsFactory<Object>> conColsFacs = Set.of( facCols1, facCols2 );
        
        final int                   cntColsTot = cntCols1 + cntCols2;
        final Set<String>                   setColNms = Stream.concat(IntStream.range(0, cntCols1).<String>mapToObj(i -> strNmPref1 + Integer.toString(i)), IntStream.range(0, cntCols2).<String>mapToObj(i -> strNmPref2 + Integer.toString(i))).collect(Collectors.toCollection(TreeSet::new));
        final Set<DpSupportedType>          setColTypes = conColsFacs.stream().<DpSupportedType>map(fac -> fac.getColumnType()).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        final Map<DpSupportedType, Integer> mapColTypeToCnt = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnCount()));
        final Map<DpSupportedType, Set<String>> mapColTypeToNms = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnNames()));
        
        // Create ingestion frame factory and check configuration
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.from(setTags, mapAttrs, facTms, conColsFacs);
            
            Assert.assertEquals(cntSamples, facTest.getSampleCount());
            Assert.assertEquals(cntColsTot, facTest.getColumnCount());
            Assert.assertEquals(enmTmsCase, facTest.getTimestampType());
            Assert.assertEquals(setColNms, facTest.retrieveColumnNames());
            Assert.assertEquals(setTags, facTest.getTags());
            Assert.assertEquals(mapAttrs, facTest.getAttributes());
            Assert.assertEquals(setColTypes, facTest.retrieveColumnTypes());
            Assert.assertEquals(mapColTypeToCnt, facTest.retrieveColumnTypeCount());
            Assert.assertEquals(mapColTypeToNms, facTest.retrieveColumnTypeNames());
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#defaultFrame()}.
     * @throws NoSuchElementException 
     * @throws ConfigurationException 
     * @throws MissingResourceException 
     * @throws UnsupportedOperationException 
     * @throws TypeNotPresentException 
     * @throws DateTimeParseException 
     * @throws NumberFormatException 
     */
    @Test
    public final void testDefaultFrame() throws NumberFormatException, DateTimeParseException, TypeNotPresentException, UnsupportedOperationException, MissingResourceException, ConfigurationException, NoSuchElementException {
        
        // Test Parameters
        final int                   cntSamples = CFG_DEF.timestamps.count;
        final DpTimestampCase       enmTmsCase = CFG_DEF.timestamps.type;
        final int                   cntCols = CFG_DEF.columns.stream().mapToInt(cfg -> cfg.count).sum();
        final Set<String>           setColNms = CFG_DEF.columns.stream().flatMap(cfg -> IntStream.range(0, cfg.count).<String>mapToObj(i -> cfg.name + Integer.toString(i))).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        final Set<String>           setTags = IngestionFrameFactoryTest.extractDefaultFrameTags();
        final Map<String, String>   mapAttrs = IngestionFrameFactoryTest.extractDefaultFrameAttributes();   
        
        final Collection<IFrameColumnsFactory<Object>> conColsFacs = IngestionFrameFactoryTest.extractDefaultFrameColumns(); // throws exceptions
        final Set<DpSupportedType>          setColTypes = conColsFacs.stream().<DpSupportedType>map(fac -> fac.getColumnType()).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        final Map<DpSupportedType, Integer> mapColTypeToCnt = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnCount()));
        final Map<DpSupportedType, Set<String>> mapColTypeToNms = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnNames()));
        
        // Create ingestion frame factory and check configuration
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.defaultFrame();
            
            Assert.assertEquals(cntSamples, facTest.getSampleCount());
            Assert.assertEquals(cntCols, facTest.getColumnCount());
            Assert.assertEquals(enmTmsCase, facTest.getTimestampType());
            Assert.assertEquals(setColNms, facTest.retrieveColumnNames());
            Assert.assertEquals(setTags, facTest.getTags());
            Assert.assertTrue(IngestionFrameFactoryTest.assertEqualsAttrs(mapAttrs, facTest));
            Assert.assertEquals(setColTypes, facTest.retrieveColumnTypes());
            Assert.assertEquals(mapColTypeToCnt, facTest.retrieveColumnTypeCount());
            Assert.assertEquals(mapColTypeToNms, facTest.retrieveColumnTypeNames());
            
            // Print out default ingestion frame factory configuration (tests IngestionFrameFactory#toString)
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
            System.out.println("Default Ingestion Frame Factory Configuration");
            System.out.println(facTest);
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#parse(java.lang.String[])}.
     * @throws NoSuchElementException 
     * @throws ConfigurationException 
     * @throws MissingResourceException 
     * @throws UnsupportedOperationException 
     * @throws TypeNotPresentException 
     * @throws DateTimeParseException 
     * @throws NumberFormatException 
     */
    @Test
    public final void testParse1() throws NumberFormatException, DateTimeParseException, TypeNotPresentException, UnsupportedOperationException, MissingResourceException, ConfigurationException, NoSuchElementException {
        
        // Test Parameters
        final String[]              arrArgs = ARR_ARGS_PARSE_1;
        final IngestionFrameFactory facExpect = IngestionFrameFactory.defaultFrame();   // throws all exception
        
        // Create test factory parser and compare with expected factory
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.parse(arrArgs);
            
            Assert.assertEquals(facExpect, facTest);
            Assert.assertTrue(facExpect.equals(facTest));
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory parser creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse2() {
        
        // Test Parameters
        final String[]                  arrArgs = ARR_ARGS_PARSE_2;
        
        final Set<String>               setTags = Set.of("tag1", "tag2", "tag3");
        final Map<String, String>       mapAttrs = Map.of("nm1", "val1", "nm2", "val2");
        
        final int                       cntSamples = 100;
        final Duration                  durPeriod = Duration.ofMillis(1);
        final Duration                  durDelay = Duration.ofMillis(3);
        final DpTimestampCase           enmTmsCase = DpTimestampCase.TIMESTAMP_LIST;
        final Instant                   insStart = Instant.parse("2026-01-12T17:48:00Z");
        final FrameTimestampsFactory    facTms = FrameTimestampsFactory.from(cntSamples, durPeriod, insStart, enmTmsCase, durDelay);
        
        final int                       cntCols = 2;
        final String                    strNmPref = "Cols1:";
        final JalComplexType            enmColType = JalComplexType.IMAGE;
        final FrameColumnsFactory       facCols = FrameColumnsFactory.from(cntCols, strNmPref, enmColType);
        
        final Collection<IFrameColumnsFactory<Object>>  conColsFacs = Set.of(facCols);
        
        final IngestionFrameFactory     facExpect = IngestionFrameFactory.from(setTags, mapAttrs, facTms, conColsFacs);
        
        // Create test factory parser and compare with expected factory
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.parse(arrArgs);
            
            Assert.assertEquals(facExpect, facTest);
            Assert.assertTrue(facExpect.equals(facTest));
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory parser creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse3() {
        
        // Test Parameters
        final String[]                  arrArgs = ARR_ARGS_PARSE_3;
        
        final Set<String>               setTags = new TreeSet<>(SET_FRM_TAGS_DEF); 
                                        setTags.add("tag3");
        final Map<String, String>       mapAttrs = new HashMap<>(MAP_FRM_ATTRS_DEF);
        
        final int                       cntSamples = 100;
        final Duration                  durPeriod = Duration.ofMillis(1);
        final Duration                  durDelay = Duration.ofMillis(3);
        final DpTimestampCase           enmTmsCase = DpTimestampCase.TIMESTAMP_LIST;
        final Instant                   insStart = Instant.parse("2026-01-12T17:48:00Z");
        final FrameTimestampsFactory    facTms = FrameTimestampsFactory.from(cntSamples, durPeriod, insStart, enmTmsCase, durDelay);
        
        final int                       cntCols1 = 2;
        final String                    strNmPref1 = "Cols1:";
        final JalComplexType            enmColType1 = JalComplexType.IMAGE;
        final FrameColumnsFactory       facCols1 = FrameColumnsFactory.from(cntCols1, strNmPref1, enmColType1);
        
        final int                       cntCols2 = 100;
        final String                    strNmPref2 = "Cols2:";
        final IDatumFactory             facVals2 = ScalarFactory.from(JalScalarType.DOUBLE);
        final FrameColumnsFactory       facCols2 = FrameColumnsFactory.from(cntCols2, strNmPref2, facVals2);
        
        final Collection<IFrameColumnsFactory<Object>>  conColsFacs = Set.of(facCols1, facCols2);
        
        final IngestionFrameFactory     facExpect = IngestionFrameFactory.from(setTags, mapAttrs, facTms, conColsFacs);
        
        // Create test factory parser and compare with expected factory
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.parse(arrArgs);
            
            Assert.assertEquals(facExpect, facTest);
            Assert.assertTrue(facExpect.equals(facTest));
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory parser creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse4() {
        
        // Test Parameters
        final String[]                  arrArgs = ARR_ARGS_PARSE_4;
        
        final Set<String>               setTags = new TreeSet<>(IngestionFrameFactoryTest.extractDefaultFrameTags()); 
                                        setTags.add("tag3");
        final Map<String, String>       mapAttrs = new HashMap<>(MAP_FRM_ATTRS_DEF);
                                        mapAttrs.put("nm1", "val1");
                                        mapAttrs.put("nm2", "val2");
        
        final int                       cntSamples = 100;
        final Duration                  durPeriod = Duration.ofMillis(1);
        final Duration                  durDelay = Duration.ofMillis(3);
        final DpTimestampCase           enmTmsCase = DpTimestampCase.TIMESTAMP_LIST;
        final Instant                   insStart = Instant.parse("2026-01-12T17:48:00Z");
        final FrameTimestampsFactory    facTms = FrameTimestampsFactory.from(cntSamples, durPeriod, insStart, enmTmsCase, durDelay);
        
        final int                       cntCols1 = 2;
        final String                    strNmPref1 = "Cols1:";
        final JalComplexType            enmColType1 = JalComplexType.IMAGE;
        final FrameColumnsFactory       facCols1 = FrameColumnsFactory.from(cntCols1, strNmPref1, enmColType1);

        final int                       cntCols2 = 1;
        final String                    strNmPref2 = "Cols2:";
        final ScalarFactory             facScal2 = ScalarFactory.from(JalScalarType.INTEGER, false, 0, 2);
        final IDatumFactory             facVals2 = StructureFactory.from(4, 2, true, facScal2);
        final FrameColumnsFactory       facCols2 = FrameColumnsFactory.from(cntCols2, strNmPref2, facVals2);
        
        final int                       cntCols3 = 100;
        final String                    strNmPref3 = "Cols3:";
        final IDatumFactory             facVals3 = ScalarFactory.from(JalScalarType.DOUBLE);
        final FrameColumnsFactory       facCols3 = FrameColumnsFactory.from(cntCols3, strNmPref3, facVals3);
        
        final Collection<IFrameColumnsFactory<Object>>  conColsFacs = Set.of(facCols1, facCols2, facCols3);
        
        final IngestionFrameFactory     facExpect = IngestionFrameFactory.from(setTags, mapAttrs, facTms, conColsFacs);
        
        // Create test factory parser and compare with expected factory
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.parse(arrArgs);
            
            Assert.assertEquals(facExpect, facTest);
            Assert.assertTrue(facExpect.equals(facTest));
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory parser creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#IngestionFrameFactory(java.util.Set, java.util.Map, com.ospreydcs.dp.jal.tools.common.datagen.IFrameTimestampsFactory, java.util.Collection)}.
     */
    @Test
    public final void testIngestionFrameFactory() {
        
        // Test Parameters
        final Set<String>           setTags = Set.of("tag1", "tag2", "tag3");
        final Map<String, String>   mapAttrs = Map.of("name1", "value1", "name2", "value2", "name3", "value3");
        
        final int                   cntSamples = 42;
        final Duration              durPeriod = Duration.ofMillis(1);
        final Instant               insStart = Instant.now();
        final DpTimestampCase       enmTmsCase = DpTimestampCase.TIMESTAMP_LIST;
        final Duration              durDelay = Duration.ofMillis(3);
        final FrameTimestampsFactory facTms = FrameTimestampsFactory.from(cntSamples, durPeriod, insStart, enmTmsCase, durDelay);
        
        final int                   cntCols1 = 2;
        final String                strNmPref1 = "Cols1:";
        final IDatumFactory         facVals1 = StructureFactory.from(3, 2, true, JalScalarType.INTEGER, true, 1);
        final IFrameColumnsFactory<Object>  facCols1 = FrameColumnsFactory.from(cntCols1, strNmPref1, facVals1);
        
        final int                   cntCols2 = 100;
        final String                strNmPref2 = "Cols2:";
        final IDatumFactory         facVals2 = ScalarFactory.from(JalScalarType.DOUBLE, false, 1, 0.1);
        final IFrameColumnsFactory<Object>  facCols2 = FrameColumnsFactory.from(cntCols2, strNmPref2, facVals2);
        
        final Collection<IFrameColumnsFactory<Object>> conColsFacs = Set.of( facCols1, facCols2 );
        
        final int                   cntColsTot = cntCols1 + cntCols2;
        final Set<String>                   setColNms = Stream.concat(IntStream.range(0, cntCols1).<String>mapToObj(i -> strNmPref1 + Integer.toString(i)), IntStream.range(0, cntCols2).<String>mapToObj(i -> strNmPref2 + Integer.toString(i))).collect(Collectors.toCollection(TreeSet::new));
        final Set<DpSupportedType>          setColTypes = conColsFacs.stream().<DpSupportedType>map(fac -> fac.getColumnType()).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        final Map<DpSupportedType, Integer> mapColTypeToCnt = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnCount()));
        final Map<DpSupportedType, Set<String>> mapColTypeToNms = conColsFacs.stream().collect(Collectors.toMap(fac -> fac.getColumnType(), fac -> fac.getColumnNames()));
        
        // Create ingestion frame factory and check configuration
        try {
            IngestionFrameFactory   facTest = new IngestionFrameFactory(setTags, mapAttrs, facTms, conColsFacs);
            
            Assert.assertEquals(cntSamples, facTest.getSampleCount());
            Assert.assertEquals(cntColsTot, facTest.getColumnCount());
            Assert.assertEquals(enmTmsCase, facTest.getTimestampType());
            Assert.assertEquals(setColNms, facTest.retrieveColumnNames());
            Assert.assertEquals(setTags, facTest.getTags());
            Assert.assertEquals(mapAttrs, facTest.getAttributes());
            Assert.assertEquals(setColTypes, facTest.retrieveColumnTypes());
            Assert.assertEquals(mapColTypeToCnt, facTest.retrieveColumnTypeCount());
            Assert.assertEquals(mapColTypeToNms, facTest.retrieveColumnTypeNames());
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#attachDefaultTags()}.
//     */
//    @Test
//    public final void testAttachDefaultTags() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#attachClassTags()}.
//     */
//    @Test
//    public final void testAttachClassTags() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#attachTag(java.lang.String)}.
//     */
//    @Test
//    public final void testAttachTag() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#attachTags(java.util.Collection)}.
//     */
//    @Test
//    public final void testAttachTags() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#attachDefaultAttributes()}.
//     */
//    @Test
//    public final void testAttachDefaultAttributes() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#attachClassAttributes()}.
//     */
//    @Test
//    public final void testAttachClassAttributes() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#attachAttribute(java.lang.String, java.lang.String)}.
//     */
//    @Test
//    public final void testAttachAttribute() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#attachAttributes(java.util.Map)}.
//     */
//    @Test
//    public final void testAttachAttributes() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#addFrameColumns(com.ospreydcs.dp.jal.tools.common.datagen.IFrameColumnsFactory)}.
     */
    @Test
    public final void testAddFrameColumnsIFrameColumnsFactoryOfObject() {
        
        // Test Parameters
        final int                   cntSamples = 42;
        final Duration              durPeriod = Duration.ofMillis(1);
        final Instant               insStart = Instant.now();
        final DpTimestampCase       enmTmsCase = DpTimestampCase.TIMESTAMP_LIST;
        final Duration              durDelay = Duration.ofMillis(3);
        final FrameTimestampsFactory facTms = FrameTimestampsFactory.from(cntSamples, durPeriod, insStart, enmTmsCase, durDelay);
        
        final int                   cntCols1 = 2;
        final String                strNmPref1 = "Cols1:";
        final IDatumFactory         facVals1 = StructureFactory.from(3, 2, true, JalScalarType.INTEGER, true, 1);
        final IFrameColumnsFactory<Object>  facCols1 = FrameColumnsFactory.from(cntCols1, strNmPref1, facVals1);
        
        final int                   cntCols2 = 100;
        final String                strNmPref2 = "Cols2:";
        final IDatumFactory         facVals2 = ScalarFactory.from(JalScalarType.DOUBLE, false, 1, 0.1);
        final IFrameColumnsFactory<Object>  facCols2 = FrameColumnsFactory.from(cntCols2, strNmPref2, facVals2);
        
        final Collection<IFrameColumnsFactory<Object>> conColsFacs = Set.of( facCols1 );
        
        final int                   cntColsTot = cntCols1 + cntCols2;
        final Set<String>           setTags = Set.of();
        final Map<String, String>   mapAttrs = Map.of();
        
        final Set<String>                   setColNms = Stream.concat(IntStream.range(0, cntCols1).<String>mapToObj(i -> strNmPref1 + Integer.toString(i)), IntStream.range(0, cntCols2).<String>mapToObj(i -> strNmPref2 + Integer.toString(i))).collect(Collectors.toCollection(TreeSet::new));
        final Set<DpSupportedType>          setColTypes = Set.of(DpSupportedType.STRUCTURE, DpSupportedType.DOUBLE); 
        final Map<DpSupportedType, Integer> mapColTypeToCnt = Map.of(DpSupportedType.STRUCTURE, 2, DpSupportedType.DOUBLE, 100);
        final Map<DpSupportedType, Set<String>> mapColTypeToNms = Map.of(DpSupportedType.STRUCTURE, facCols1.getColumnNames(), DpSupportedType.DOUBLE, facCols2.getColumnNames());
        
        // Create ingestion frame factory and check configuration
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.from(facTms, conColsFacs);
            
            facTest.addFrameColumns(facCols2);
            
            Assert.assertEquals(cntSamples, facTest.getSampleCount());
            Assert.assertEquals(cntColsTot, facTest.getColumnCount());
            Assert.assertEquals(enmTmsCase, facTest.getTimestampType());
            Assert.assertEquals(setColNms, facTest.retrieveColumnNames());
            Assert.assertEquals(setTags, facTest.getTags());
            Assert.assertEquals(mapAttrs, facTest.getAttributes());
            Assert.assertEquals(setColTypes, facTest.retrieveColumnTypes());
            Assert.assertEquals(mapColTypeToCnt, facTest.retrieveColumnTypeCount());
            Assert.assertEquals(mapColTypeToNms, facTest.retrieveColumnTypeNames());
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#addFrameColumns(java.util.Collection)}.
     */
    @Test
    public final void testAddFrameColumnsCollectionOfIFrameColumnsFactoryOfObject() {
        
        // Test Parameters
        final Set<String>               setTags = new TreeSet<>(IngestionFrameFactoryTest.extractDefaultFrameTags()); 
                                        setTags.add("tag3");
        final Map<String, String>       mapAttrs = new HashMap<>(MAP_FRM_ATTRS_DEF);
                                        mapAttrs.put("nm1", "val1");
                                        mapAttrs.put("nm2", "val2");
        
        final int                       cntSamples = 100;
        final Duration                  durPeriod = Duration.ofMillis(1);
        final Duration                  durDelay = Duration.ofMillis(3);
        final DpTimestampCase           enmTmsCase = DpTimestampCase.TIMESTAMP_LIST;
        final Instant                   insStart = Instant.parse("2026-01-12T17:48:00Z");
        final FrameTimestampsFactory    facTms = FrameTimestampsFactory.from(cntSamples, durPeriod, insStart, enmTmsCase, durDelay);
        
        final int                       cntCols1 = 2;
        final String                    strNmPref1 = "Cols1:";
        final JalComplexType            enmColType1 = JalComplexType.IMAGE;
        final FrameColumnsFactory       facCols1 = FrameColumnsFactory.from(cntCols1, strNmPref1, enmColType1);

        final int                       cntCols2 = 1;
        final String                    strNmPref2 = "Cols2:";
        final ScalarFactory             facScal2 = ScalarFactory.from(JalScalarType.INTEGER, false, 0, 2);
        final IDatumFactory             facVals2 = StructureFactory.from(4, 2, true, facScal2);
        final FrameColumnsFactory       facCols2 = FrameColumnsFactory.from(cntCols2, strNmPref2, facVals2);
        
        final int                       cntCols3 = 100;
        final String                    strNmPref3 = "Cols3:";
        final IDatumFactory             facVals3 = ScalarFactory.from(JalScalarType.DOUBLE);
        final FrameColumnsFactory       facCols3 = FrameColumnsFactory.from(cntCols3, strNmPref3, facVals3);
        
        final Collection<IFrameColumnsFactory<Object>>  conColsFacsExp = Set.of(facCols1, facCols2, facCols3);
        final Collection<IFrameColumnsFactory<Object>>  conColsFacsOrg = Set.of(facCols1);
        final Collection<IFrameColumnsFactory<Object>>  conColsFacsAdd = Set.of(facCols2, facCols3);
        
        final IngestionFrameFactory     facExpect = IngestionFrameFactory.from(setTags, mapAttrs, facTms, conColsFacsExp);
        
        // Create test factory, add columns set, and compare with expected factory
        try {
            IngestionFrameFactory   facTest = IngestionFrameFactory.from(setTags, mapAttrs, facTms, conColsFacsOrg);
            
            facTest.addFrameColumns(conColsFacsAdd);
            
            Assert.assertEquals(facExpect, facTest);
            Assert.assertTrue(facExpect.equals(facTest));
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#retrieveCountCount()}.
//     */
//    @Test
//    public final void testRetrieveCountCount() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#retrieveColumnNames()}.
//     */
//    @Test
//    public final void testRetrieveColumnNames() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#retrieveColumnTypes()}.
//     */
//    @Test
//    public final void testRetrieveColumnTypes() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#retrieveColumnTypeCount()}.
//     */
//    @Test
//    public final void testRetrieveColumnTypeCount() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#retrieveColumnTypeNames()}.
//     */
//    @Test
//    public final void testRetrieveColumnTypeNames() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#getSampleCount()}.
//     */
//    @Test
//    public final void testGetSampleCount() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#getColumnCount()}.
//     */
//    @Test
//    public final void testGetColumnCount() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#getTimestampType()}.
//     */
//    @Test
//    public final void testGetTimestampType() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#nextFrame()}.
     */
    @Test
    public final void testNextFrame() {
        
        // Test Parameters
        final Set<String>               setTags = new TreeSet<>(IngestionFrameFactoryTest.extractDefaultFrameTags()); 
                                        setTags.add("tag3");
        final Map<String, String>       mapAttrs = new HashMap<>(MAP_FRM_ATTRS_DEF);
                                        mapAttrs.put("nm1", "val1");
                                        mapAttrs.put("nm2", "val2");
        
        final int                       cntSamples = 100;
        final Duration                  durPeriod = Duration.ofMillis(1);
        final Duration                  durDelay = Duration.ofMillis(3);
        final DpTimestampCase           enmTmsCase = DpTimestampCase.TIMESTAMP_LIST;
        final Instant                   insStart = Instant.parse("2026-01-12T17:48:00Z");
        final FrameTimestampsFactory    facTms = FrameTimestampsFactory.from(cntSamples, durPeriod, insStart, enmTmsCase, durDelay);
        
        final int                       cntCols1 = 2;
        final String                    strNmPref1 = "Cols1:";
        final JalComplexType            enmColType1 = JalComplexType.IMAGE;
        final FrameColumnsFactory       facCols1 = FrameColumnsFactory.from(cntCols1, strNmPref1, enmColType1);

        final int                       cntCols2 = 1;
        final String                    strNmPref2 = "Cols2:";
        final ScalarFactory             facScal2 = ScalarFactory.from(JalScalarType.INTEGER, false, 0, 2);
        final IDatumFactory             facVals2 = StructureFactory.from(4, 2, true, facScal2);
        final FrameColumnsFactory       facCols2 = FrameColumnsFactory.from(cntCols2, strNmPref2, facVals2);
        
        final int                       cntCols3 = 100;
        final String                    strNmPref3 = "Cols3:";
        final IDatumFactory             facVals3 = ScalarFactory.from(JalScalarType.DOUBLE);
        final FrameColumnsFactory       facCols3 = FrameColumnsFactory.from(cntCols3, strNmPref3, facVals3);
        
        final Collection<IFrameColumnsFactory<Object>>  conColsFacs = Set.of(facCols1, facCols2, facCols3);
        
        final int                           cntColsTot = cntCols1 + cntCols2 + cntCols3;
        
        final Set<String>                   setColNms = Set.of(facCols1.getColumnNames(), facCols2.getColumnNames(), facCols3.getColumnNames()).stream().<String>flatMap(set -> set.stream()).collect(Collectors.toCollection(TreeSet::new));
        final Set<DpSupportedType>          setColTypes = Set.of(DpSupportedType.IMAGE, DpSupportedType.STRUCTURE, DpSupportedType.DOUBLE); 
        final Map<DpSupportedType, Integer> mapColTypeToCnt = Map.of(DpSupportedType.IMAGE, 2, DpSupportedType.STRUCTURE, 1, DpSupportedType.DOUBLE, 100);
        final Map<DpSupportedType, Set<String>> mapColTypeToNms = Map.of(DpSupportedType.IMAGE, facCols1.getColumnNames(), DpSupportedType.STRUCTURE, facCols2.getColumnNames(), DpSupportedType.DOUBLE, facCols3.getColumnNames());
        
        try {
            // Create test factory and check configuration
            IngestionFrameFactory   facTest = IngestionFrameFactory.from(setTags, mapAttrs, facTms, conColsFacs);
            
            Assert.assertEquals(cntSamples, facTest.getSampleCount());
            Assert.assertEquals(cntColsTot, facTest.getColumnCount());
            Assert.assertEquals(enmTmsCase, facTest.getTimestampType());
            Assert.assertEquals(setColNms, facTest.retrieveColumnNames());
            Assert.assertEquals(setTags, facTest.getTags());
            Assert.assertEquals(mapAttrs, facTest.getAttributes());
            Assert.assertEquals(setColTypes, facTest.retrieveColumnTypes());
            Assert.assertEquals(mapColTypeToCnt, facTest.retrieveColumnTypeCount());
            Assert.assertEquals(mapColTypeToNms, facTest.retrieveColumnTypeNames());
            
            // Create an ingestion frame and check configuration
            IngestionFrame  frmTest = facTest.nextFrame();
            
            Assert.assertEquals(cntSamples, frmTest.getRowCount());
            Assert.assertEquals(cntColsTot, frmTest.getColumnCount());
            Assert.assertEquals(setColNms, frmTest.getColumnNames());
            
            UniformSamplingClock    clkTest = frmTest.getSamplingClock();
            List<Instant>           lstTms = frmTest.getTimestampList();
            
            Assert.assertNull(clkTest);
            Assert.assertEquals(cntSamples, lstTms.size());
            
            boolean bolColTypes = mapColTypeToNms
                    .entrySet()
                    .stream()
                    .allMatch(
                            entry -> entry.getValue().stream().allMatch(
                                    colNm -> frmTest.getDataColumn(colNm).getType() == entry.getKey()
                                    )
                            );
            Assert.assertTrue(bolColTypes);
            
        } catch (Exception e) {
            Assert.fail("IngestionFrameFactory parser creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#equals(java.lang.Object)}.
//     */
//    @Test
//    public final void testEqualsObject() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory#toString()}.
//     */
//    @Test
//    public final void testToString() {
//        fail("Not yet implemented"); // TODO
//    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates and returns a new <code>Set</code> containing the default ingestion frame tag values.
     * </p>
     * <p>
     * Extracts the tag values from the default ingestion frame configuration (in the JAL Tools default configuration)
     * and populates a new <code>{@link Set}</code> collection with the values.
     * </p>
     * 
     * @return  a new <code>Set</code> containing the default ingestion frame tag values
     */
    private static Set<String>  extractDefaultFrameTags() {
        Set<String>     setTags = new TreeSet<>();
        
        if (BOL_TAGS_DEF_ENBL)
            setTags.addAll(SET_FRM_TAGS_DEF);
        if (BOL_TAGS_CLS_ENBL)
            setTags.addAll(SET_FRM_TAGS_CLS);
        
        return setTags;
    }
    
    /**
     * <p>
     * Creates and returns a new <code>Map</code> containing the default ingestion frame (name, value) attribute pairs.
     * </p>
     * <p>
     * Extracts the (name, value) attribute pairs from the default ingestion frame configuration (in the JAL Tools default
     * configuration) and populates a new <code>{@link Map}</code> collection with the pairs.
     * </p>
     *   
     * @return  a new <code>Map</code> containing the default ingestion frame (name, value) attribute pairs
     */
    private static Map<String, String>  extractDefaultFrameAttributes() {
        Map<String, String> mapAttrs = new HashMap<>();
        
        if (BOL_ATTRS_DEF_ENBL)
            mapAttrs.putAll(MAP_FRM_ATTRS_DEF);
        if (BOL_ATTRS_CLS_ENBL)
            mapAttrs.putAll(MAP_FRM_ATTRS_CLS);
        
        return mapAttrs;
    }

    /**
     * <p>
     * Compares the collection of attribute value pairs against the given <code>IngestionFrameFactory</code> attributes.
     * </p>
     * <p>
     * Compares for equivalence the give collection of (name, value) attribute pairs against the 
     * <code>{@link FrameFactorySpec#mapAttrs()}</code> attributes.  
     * Any attribute within the list <code>{@link #LST_ATTR_NMS_IGNORE}</code> is ignored in this comparison.
     * </p>
     * 
     * @param mapAttrsExpect    expected collection of (name, value) attribute pairs
     * @param facTest           the <code>IngestionFrameFactory</code> instance under test
     * 
     * @return  <code>true</code> if the attributes are equivalent except for those named in <code>{@link #LST_ATTR_NMS_IGNORE}</code>,
     *          <code>false</code> otherwise
     */
    private static boolean assertEqualsAttrs(Map<String, String> mapAttrsExpect, IngestionFrameFactory facTest) {
        
        boolean     bolResult = true;
        for (Map.Entry<String, String> entry : mapAttrsExpect.entrySet()) {
            
            // Check if this is an ignored attribute
            boolean bolIgnore = LST_ATTR_NMS_IGNORE.stream().anyMatch(key -> key.equals(entry.getKey()));
            if (bolIgnore)
                continue;
            
            // Compare attribute values
            String  strValExpect = entry.getValue();
            String  strValTest = facTest.getAttributes().get(entry.getKey());
            
            bolResult = bolResult && strValExpect.equals(strValTest);
        }
        
        return bolResult;
    }
    
//    /**
//     * <p>
//     * Creates and returns a new frame timestamps factory for the default ingestion frame.
//     * </p>
//     * <p>
//     * Extracts the timestamps parameters from the default ingestion frame configuration and
//     * uses them to create a new <code>{@link IFrameTimstampsFactory}</code> implementation.
//     * </p>
//     *  
//     * @return  a new <code>IFrameTimestampsFactory</code> implementation configured from the default ingestion frame
//     */
//    private static IFrameTimestampsFactory  extractDefaultFrameTimestamps() {
//        JalToolsFramesTmsConfig  cfgTms = CFG_DEF.timestamps;
//        
//        int             cntSmpls = cfgTms.count;
//        Duration        durPeriod = cfgTms.periodDuration();
//        Instant         insStart = cfgTms.startInstant();
//        DpTimestampCase enmCase = cfgTms.type;
//        Duration        durDelay = cfgTms.delayDuration();
//        
//        FrameTimestampsFactory   facTms = FrameTimestampsFactory.from(cntSmpls, durPeriod, insStart, enmCase, durDelay);
//        
//        return facTms;
//    }

    /**
     * <p>
     * Creates and returns a collection of frame columns factory implementations according to the default ingestion frame configuration.
     * </p>
     * <p>
     * Extracts the data columns parameters from the default ingestion frame configuration and uses them to create
     * new <code>{@link IFrameColumnsFactory}</code> implementations.
     * </p>
     * 
     * @return  a new collection of <code>IFrameColumnsFactory</code> implementations configured from the default ingestion frame
     * 
     * @throws TypeNotPresentException          unknown <code>JalScalarType</code> enumeration constant (scalar factory)
     * @throws NumberFormatException            invalid numeric format (e.g., scalar factory bad 'numIncr' or 'lngSeed') 
     * @throws UnsupportedOperationException    unable to create 'numIncr' field for numeric value type (scalar factory)
     * @throws MissingResourceException         timestamp factory had empty arguments
     * @throws DateTimeParseException           bad ISO-8601 time and/or duration format (e.g., timestamp factory period, start, etc.) 
     * @throws ConfigurationException           the tensor shape was invalid (e.g., an axis size could not be parsed, non-positive axis size, etc.)
     * @throws NoSuchElementException           unrecognized <code>{@link JalComplexType}</code> constant in argument  
     */
    private static Collection<IFrameColumnsFactory<Object>>  extractDefaultFrameColumns() 
            throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException, MissingResourceException, DateTimeParseException, ConfigurationException, NoSuchElementException 
    {
        List<JalToolsColumnsConfig> lstCfgCols = CFG_DEF.columns;
        List<IFrameColumnsFactory<Object>> lstFacCols = new ArrayList<>(lstCfgCols.size());
        
        for (JalToolsColumnsConfig cfg : lstCfgCols) {
            String          strNmPref = cfg.name;
            int             cntCols = cfg.count;
            JalComplexType  enmType = cfg.type;
            String[]        arrFacParse = cfg.factory;
            
            Set<String>     setColNms = IntStream.range(0, cntCols).<String>mapToObj(i -> strNmPref + Integer.toString(i)).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
            IDatumFactory   facDatum = IngestionFrameFactoryTest.createDatumFactory(enmType, arrFacParse);  // throws all exceptions

            IFrameColumnsFactory<Object>    facCols = FrameColumnsFactory.from(setColNms, facDatum);
            
            lstFacCols.add(facCols);
        }
        
        return lstFacCols;
    }
    
    /**
     * <p>
     * Creates and returns a new <code>IDatumFactory</code> implementation with configuration given by the arguments.
     * </p>
     * <p>
     * This is an auxiliary method used by <code>{@link #extractDefaultFrameColumns()}</code>. 
     * The data produced by the <code>{@link IDatumFactory}</code> has type given by <code>{@link JalComplexType}</code>,
     * that is, the datum factory is determined by the <code>JalComplexType</code> argument.
     * The string array is used as the argument for the datum factory parsing constructor.
     * </p>
     *  
     * @param enmType   type of datum factory returned
     * @param arrArgs   arguments for the datum factory parsing constructor
     * 
     * @return  a new <code>IDatumFactory</code> implementation configured from the given arguments
     * 
     * @throws TypeNotPresentException          unknown <code>JalScalarType</code> enumeration constant (scalar factory)
     * @throws NumberFormatException            invalid numeric format (e.g., scalar factory bad 'numIncr' or 'lngSeed') 
     * @throws UnsupportedOperationException    unable to create 'numIncr' field for numeric value type (scalar factory)
     * @throws MissingResourceException         timestamp factory had empty arguments
     * @throws DateTimeParseException           bad ISO-8601 time and/or duration format (e.g., timestamp factory period, start, etc.) 
     * @throws ConfigurationException           the tensor shape was invalid (e.g., an axis size could not be parsed, non-positive axis size, etc.)
     * @throws NoSuchElementException           unrecognized <code>{@link JalComplexType}</code> constant in argument  
     */
    private static IDatumFactory    createDatumFactory(JalComplexType enmType, String[] arrArgs) 
            throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException, MissingResourceException, DateTimeParseException, ConfigurationException, NoSuchElementException 
    {
        return switch (enmType) {
        case SCALAR -> ScalarFactory.parse(arrArgs);        // throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException
        case BYTES -> ByteArrayFactory.parse(arrArgs);      // throws NumberFormatException
        case TIMESTAMP -> TimestampFactory.parse(arrArgs);  // throws MissingResourceException, NumberForamtException, DateTimeParseException
        case IMAGE -> ImageFactory.parse(arrArgs);          // throws NumberFormatException, TypeNotPresentException
        case TENSOR -> TensorFactory.parse(arrArgs);        // throws ConfigurationException, NumberFormatException, TypeNotPresentException, UnsupportedOperationException
        case STRUCTURE -> StructureFactory.parse(arrArgs);  // throws NumberFormatException, TypeNotPresentException, UnsupportedOperationException
        default -> throw new NoSuchElementException("Unexpected value: " + enmType);
        };
    }
    

}
