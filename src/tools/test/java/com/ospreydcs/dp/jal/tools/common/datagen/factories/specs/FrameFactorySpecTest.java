/*
 * Project: dp-jal
 * File:	FrameFactorySpecTest.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.specs
 * Type: 	FrameFactorySpecTest
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
 * @since Jan 10, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.naming.ConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.common.DpTimestampCase;
import com.ospreydcs.dp.jal.common.IDataColumn;
import com.ospreydcs.dp.jal.common.UniformSamplingClock;
import com.ospreydcs.dp.jal.ingest.IngestionFrame;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.frames.JalToolsFramesConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * JUnit test cases for record <code>FrameFactorySpec</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 10, 2026
 *
 */
public class FrameFactorySpecTest {

    
    //
    // JAL Tools Library Resources
    //
    
    /** Configuration parameters for the default ingestion frame */
    public static final JalToolsFramesConfig    CFG_FRM_DEF = JalToolsConfig.getInstance().datagen.frame;
    
    
    /** Name of the specification record - used for class attributes */
    public static final String  STR_SRC_NAME = FrameFactorySpec.class.getSimpleName();
    
    /** Environment variable for current user - used for class attributes */
    public static final String  STR_USERNAME = "USER";
    
    
    //
    // Record Constants and Resources
    //
    
    /** Enable/disable class tag values flag default configuration */
    public static final boolean                 BOL_TAGS_CLS_ENBL = CFG_FRM_DEF.tags.useClass;
    
    /** Class tag values for ingestion frames */
    public static final Set<String>             SET_TAGS_FRM_CLS = new TreeSet<>();

    
    /** Enable/disable class attribute pairs flag default configuration */
    public static final boolean                 BOL_ATTRS_CLS_ENBL = CFG_FRM_DEF.attributes.useClass;
    
    /** Class attribute pairs for ingestion frame */
    public static final Map<String, String>     MAP_ATTRS_FRM_CLS = new HashMap<>();
    
    
    /** Initialization for class tags and attributes for ingestion frames */
    static {
        SET_TAGS_FRM_CLS.add(STR_SRC_NAME);
        SET_TAGS_FRM_CLS.add(JalToolsConfig.STR_CFG_FILE_NAME);
        
        String  strUser = System.getenv(STR_USERNAME);
        Instant insNow = Instant.now();
        
        MAP_ATTRS_FRM_CLS.put("Source", STR_SRC_NAME);
        MAP_ATTRS_FRM_CLS.put("Initiated", insNow.toString());
        MAP_ATTRS_FRM_CLS.put("User", strUser);
    }
    
    
    /** Enable/disable default tag values flag default configuration */
    public static final boolean                 BOL_TAGS_DEF_ENBL = CFG_FRM_DEF.tags.useDefault;
    
    /** Default ingestion frame tag values */
    public static final Set<String>             SET_TAGS_FRM_DEF = new TreeSet<>( CFG_FRM_DEF.tags.values );
    
    
    /** Enable/disable default attribute pairs default configuration */
    public static final boolean                 BOL_ATTRS_DEF_ENBL = CFG_FRM_DEF.attributes.useDefault;
    
    /** Default ingestion frame attribute pairs */
    public static final Map<String, String>     MAP_ATTRS_FRM_DEF = new HashMap<>( CFG_FRM_DEF.attributes.pairs );

    
    /** List of attribute names to ignore in comparison tests (values will differ from default) */
    public static final List<String>            LST_ATTR_NMS_IGNORE = List.of("Initiated");
    
    
    /** Parsing creator argument collection */
    public static final String[]        ARR_ARGS_PARSE_1 = { };
    
    /** Parsing creator argument collection */
    public static final String[]        ARR_ARGS_PARSE_2 = { "--tags", "tag1", "tag2", "-Anm1=val1", "-Anm2=val2", 
                                                             "--tms", "100", "PT0.001S", "TIMESTAMP_LIST", "2026-01-12T17:48:00Z", "PT0.003S",
                                                             "--cols", "2", "Cols1:", "IMAGE",
                                                             "--tags", "tag3"
                                                             };

    /** Parsing creator argument collection */
    public static final String[]        ARR_ARGS_PARSE_3 = { "-tagsDef", "-attrsDef",  
                                                             "--tms", "100", "PT0.001S", "TIMESTAMP_LIST", "2026-01-12T17:48:00Z", "PT0.003S",
                                                             "--cols", "2", "Cols1:", "IMAGE",
                                                             "--cols", "100", "Cols2:", "SCALAR", "DOUBLE",
                                                             "--tags", "tag3"
                                                             };
    /** Parsing creator argument collection */
    public static final String[]        ARR_ARGS_PARSE_4 = { "-tagsCls", "-tagsDef", "-attrsDef",   
                                                             "--tms", "100", "PT0.001S", "TIMESTAMP_LIST", "2026-01-12T17:48:00Z", "PT0.003S",
                                                             "--cols", "2", "Cols1:", "IMAGE",
                                                             "--cols", "1", "Cols2:", "STRUCTURE", "4", "2", "true", "INTEGER", "false", "0", "2",
                                                             "--cols", "100", "Cols3:", "SCALAR", "DOUBLE",
                                                             "--tags", "tag3", "-Anm1=val1", "-Anm2=val2"
                                                             };

    /** Parsing creator argument collection */
    public static final String[]        ARR_ARGS_PARSE_5 = { "--tags", "tag1", "tag2", "-Anm1=val1", "-Anm2=val2", 
                                                             "--tms", "100", "PT0.001S", "TIMESTAMP_LIST", "2026-01-12T17:48:00Z", "PT0.003S",
                                                             "--cols", "PV1", "PV2", "PV3", "IMAGE",
                                                             "--tags", "tag3"
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
    // Test Cases
    //
    
    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#from()}.
     * @throws NoSuchElementException 
     * @throws UnsupportedOperationException 
     * @throws ConfigurationException 
     * @throws TypeNotPresentException 
     * @throws IllegalArgumentException 
     * @throws NumberFormatException 
     */
    @Test
    public final void testFrom() throws NumberFormatException, IllegalArgumentException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        
        // Test Parameters
        final Set<String>           setTags = Set.of();
        final Map<String, String>   mapAttrs = Map.of();
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.defaultFrame();
        final List<FrameColumnsSpec<Record>>    lstColsSpecs = FrameColumnsSpec.defaultFrame(); // throws exceptions
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = new TreeSet<>(lstColsSpecs); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertEquals(mapAttrs, specTest.mapAttrs());
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#from(com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec)}.
     * @throws NoSuchElementException 
     * @throws UnsupportedOperationException 
     * @throws ConfigurationException 
     * @throws TypeNotPresentException 
     * @throws IllegalArgumentException 
     * @throws NumberFormatException 
     */
    @Test
    public final void testFromFrameTimestampsSpec() throws NumberFormatException, IllegalArgumentException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        
        // Test Parameters
        final Set<String>           setTags = Set.of();
        final Map<String, String>   mapAttrs = Map.of();
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(43, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.now(), Duration.ofMillis(3));
        
        final List<FrameColumnsSpec<Record>>    lstColsSpecs = FrameColumnsSpec.defaultFrame(); // throws exceptions
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = new TreeSet<>(lstColsSpecs); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(specTms); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertEquals(mapAttrs, specTest.mapAttrs());
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#from(java.util.Set)}.
     */
    @Test
    public final void testFromSetOfFrameColumnsSpecOfRecord() {
        
        // Test Parameters
        final Set<String>           setTags = Set.of();
        final Map<String, String>   mapAttrs = Map.of();
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.defaultFrame();
        
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(1, "Cols1:", JalComplexType.IMAGE);
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(100, "Cols2:", ScalarFactorySpec.from()); 
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(setColsSpecs); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertEquals(mapAttrs, specTest.mapAttrs());
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#from(com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec, java.util.Set)}.
     */
    @Test
    public final void testFromFrameTimestampsSpecSetOfFrameColumnsSpecOfRecord() {
        
        // Test Parameters
        final Set<String>           setTags = Set.of();
        final Map<String, String>   mapAttrs = Map.of();
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(43, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.now(), Duration.ofMillis(3));
        
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(1, "Cols1:", JalComplexType.IMAGE);
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(100, "Cols2:", ScalarFactorySpec.from()); 
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(specTms, setColsSpecs); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertEquals(mapAttrs, specTest.mapAttrs());
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#from(boolean, boolean, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec, java.util.Set)}.
     */
    @Test
    public final void testFromBooleanBooleanFrameTimestampsSpecSetOfFrameColumnsSpecOfRecordDef() {
        
        // Test Parameters
        final boolean               bolMetaDef = true;
        final boolean               bolMetaCls = false;
        final Set<String>           setTags = SET_TAGS_FRM_DEF;
        final Map<String, String>   mapAttrs = MAP_ATTRS_FRM_DEF;
        
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(43, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.now(), Duration.ofMillis(3));
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(1, "Cols1:", JalComplexType.IMAGE);
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(100, "Cols2:", ScalarFactorySpec.from()); 
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(bolMetaDef, bolMetaCls, specTms, setColsSpecs); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertEquals(mapAttrs, specTest.mapAttrs());
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#from(boolean, boolean, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec, java.util.Set)}.
     */
    @Test
    public final void testFromBooleanBooleanFrameTimestampsSpecSetOfFrameColumnsSpecOfRecordCls() {
        
        // Test Parameters
        final boolean               bolMetaDef = false;
        final boolean               bolMetaCls = true;
        final Set<String>           setTags = SET_TAGS_FRM_CLS;
        final Map<String, String>   mapAttrs = MAP_ATTRS_FRM_CLS;
        
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(43, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.now(), Duration.ofMillis(3));
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(1, "Cols1:", JalComplexType.IMAGE);
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(100, "Cols2:", ScalarFactorySpec.from()); 
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(bolMetaDef, bolMetaCls, specTms, setColsSpecs); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertTrue( FrameFactorySpecTest.assertEqualsAttrs(mapAttrs, specTest) );
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#from(boolean, boolean, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec, java.util.Set)}.
     */
    @Test
    public final void testFromBooleanBooleanFrameTimestampsSpecSetOfFrameColumnsSpecOfRecordDefCls() {
        
        // Test Parameters
        final boolean               bolMetaDef = true;
        final boolean               bolMetaCls = true;
        final Set<String>           setTags = FrameFactorySpecTest.createDefaultFrameTags();
        final Map<String, String>   mapAttrs = FrameFactorySpecTest.createDefaultFrameAttributes();
        
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(43, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.now(), Duration.ofMillis(3));
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(1, "Cols1:", JalComplexType.IMAGE);
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(100, "Cols2:", ScalarFactorySpec.from()); 
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(bolMetaDef, bolMetaCls, specTms, setColsSpecs); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertTrue( FrameFactorySpecTest.assertEqualsAttrs(mapAttrs, specTest) );
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#from(boolean, boolean, boolean, boolean, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec, java.util.Set)}.
     */
    @Test
    public final void testFromBooleanBooleanBooleanBooleanFrameTimestampsSpecSetOfFrameColumnsSpecOfRecord1() {
        final boolean               bolTagsDef = true;
        final boolean               bolTagsCls = false;
        final boolean               bolAttrsDef = false;
        final boolean               bolAttrsCls = false;
        final Set<String>           setTags = SET_TAGS_FRM_DEF;
        final Map<String, String>   mapAttrs = Map.of();
        
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(43, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.now(), Duration.ofMillis(3));
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(1, "Cols1:", JalComplexType.IMAGE);
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(100, "Cols2:", ScalarFactorySpec.from()); 
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(bolTagsDef, bolTagsCls, bolAttrsDef, bolAttrsCls, specTms, setColsSpecs); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertTrue( FrameFactorySpecTest.assertEqualsAttrs(mapAttrs, specTest) );
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#from(boolean, boolean, boolean, boolean, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec, java.util.Set)}.
     */
    @Test
    public final void testFromBooleanBooleanBooleanBooleanFrameTimestampsSpecSetOfFrameColumnsSpecOfRecord2() {
        final boolean               bolTagsDef = false;
        final boolean               bolTagsCls = true;
        final boolean               bolAttrsDef = false;
        final boolean               bolAttrsCls = false;
        final Set<String>           setTags = SET_TAGS_FRM_CLS;
        final Map<String, String>   mapAttrs = Map.of();
        
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(43, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.now(), Duration.ofMillis(3));
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(1, "Cols1:", JalComplexType.IMAGE);
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(100, "Cols2:", ScalarFactorySpec.from()); 
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(bolTagsDef, bolTagsCls, bolAttrsDef, bolAttrsCls, specTms, setColsSpecs); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertTrue( FrameFactorySpecTest.assertEqualsAttrs(mapAttrs, specTest) );
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#from(boolean, boolean, boolean, boolean, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec, java.util.Set)}.
     */
    @Test
    public final void testFromBooleanBooleanBooleanBooleanFrameTimestampsSpecSetOfFrameColumnsSpecOfRecord3() {
        final boolean               bolTagsDef = false;
        final boolean               bolTagsCls = false;
        final boolean               bolAttrsDef = true;
        final boolean               bolAttrsCls = false;
        final Set<String>           setTags = Set.of();
        final Map<String, String>   mapAttrs = MAP_ATTRS_FRM_DEF;
        
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(43, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.now(), Duration.ofMillis(3));
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(1, "Cols1:", JalComplexType.IMAGE);
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(100, "Cols2:", ScalarFactorySpec.from()); 
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(bolTagsDef, bolTagsCls, bolAttrsDef, bolAttrsCls, specTms, setColsSpecs); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertTrue( FrameFactorySpecTest.assertEqualsAttrs(mapAttrs, specTest) );
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#from(boolean, boolean, boolean, boolean, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec, java.util.Set)}.
     */
    @Test
    public final void testFromBooleanBooleanBooleanBooleanFrameTimestampsSpecSetOfFrameColumnsSpecOfRecord4() {
        final boolean               bolTagsDef = false;
        final boolean               bolTagsCls = false;
        final boolean               bolAttrsDef = false;
        final boolean               bolAttrsCls = true;
        final Set<String>           setTags = Set.of();
        final Map<String, String>   mapAttrs = MAP_ATTRS_FRM_CLS;
        
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(43, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.now(), Duration.ofMillis(3));
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(1, "Cols1:", JalComplexType.IMAGE);
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(100, "Cols2:", ScalarFactorySpec.from()); 
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(bolTagsDef, bolTagsCls, bolAttrsDef, bolAttrsCls, specTms, setColsSpecs); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertTrue( FrameFactorySpecTest.assertEqualsAttrs(mapAttrs, specTest) );
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#from(boolean, boolean, boolean, boolean, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec, java.util.Set)}.
     */
    @Test
    public final void testFromBooleanBooleanBooleanBooleanFrameTimestampsSpecSetOfFrameColumnsSpecOfRecord5() {
        final boolean               bolTagsDef = true;
        final boolean               bolTagsCls = true;
        final boolean               bolAttrsDef = false;
        final boolean               bolAttrsCls = false;
        final Set<String>           setTags = FrameFactorySpecTest.createDefaultFrameTags();
        final Map<String, String>   mapAttrs = Map.of();
        
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(43, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.now(), Duration.ofMillis(3));
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(1, "Cols1:", JalComplexType.IMAGE);
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(100, "Cols2:", ScalarFactorySpec.from()); 
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(bolTagsDef, bolTagsCls, bolAttrsDef, bolAttrsCls, specTms, setColsSpecs); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertTrue( FrameFactorySpecTest.assertEqualsAttrs(mapAttrs, specTest) );
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#from(boolean, boolean, boolean, boolean, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec, java.util.Set)}.
     */
    @Test
    public final void testFromBooleanBooleanBooleanBooleanFrameTimestampsSpecSetOfFrameColumnsSpecOfRecord6() {
        final boolean               bolTagsDef = false;
        final boolean               bolTagsCls = false;
        final boolean               bolAttrsDef = true;
        final boolean               bolAttrsCls = true;
        final Set<String>           setTags = Set.of();
        final Map<String, String>   mapAttrs = FrameFactorySpecTest.createDefaultFrameAttributes();
        
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(43, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.now(), Duration.ofMillis(3));
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(1, "Cols1:", JalComplexType.IMAGE);
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(100, "Cols2:", ScalarFactorySpec.from()); 
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(bolTagsDef, bolTagsCls, bolAttrsDef, bolAttrsCls, specTms, setColsSpecs); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertTrue( FrameFactorySpecTest.assertEqualsAttrs(mapAttrs, specTest) );
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#from(java.util.Set, java.util.Map, com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameTimestampsSpec, java.util.Set)}.
     */
    @Test
    public final void testFromSetOfStringMapOfStringStringFrameTimestampsSpecSetOfFrameColumnsSpecOfRecord() {
        
        // Test Parameters
        final Set<String>           setTags = Set.of("Big", "Beautiful", "Trail");
        final Map<String, String>   mapAttrs = Map.of("Adjective", "Big", "Genetive", "Beautiful", "Nomitive", "Trail");
        
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(43, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.now(), Duration.ofMillis(3));
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(1, "Cols1:", JalComplexType.IMAGE);
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(100, "Cols2:", ScalarFactorySpec.from()); 
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(setTags, mapAttrs, specTms, setColsSpecs); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertEquals(mapAttrs, specTest.mapAttrs());
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#parse(java.lang.String[])}.
     * @throws NoSuchElementException 
     * @throws UnsupportedOperationException 
     * @throws ConfigurationException 
     * @throws TypeNotPresentException 
     * @throws IllegalArgumentException 
     * @throws NumberFormatException 
     */
    @Test
    public final void testParse1() throws NumberFormatException, IllegalArgumentException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        
        // Test Parameters
        final String[]              arrArgs = ARR_ARGS_PARSE_1;
        final Set<String>           setTags = createDefaultFrameTags();
        final Map<String, String>   mapAttrs = createDefaultFrameAttributes();
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.defaultFrame();
        
        final List<FrameColumnsSpec<Record>>    lstColsSpecs = FrameColumnsSpec.defaultFrame(); // throws exceptions
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = new TreeSet<>(lstColsSpecs); 

        // Creator the frame factory specification with the parsing creator and check configuration
        try {
            FrameFactorySpec    specTest = FrameFactorySpec.parse(arrArgs);
            
            Assert.assertEquals(setTags, specTest.setTags());
            Assert.assertTrue(FrameFactorySpecTest.assertEqualsAttrs(mapAttrs, specTest));
            Assert.assertEquals(specTms, specTest.specTms());
            Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());

        } catch (Exception e) {
            Assert.fail("Frame specification parser creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse2() {
        
        // Test Parameters
        final String[]              arrArgs = ARR_ARGS_PARSE_2;
        final Set<String>           setTags = Set.of("tag1", "tag2", "tag3");
        final Map<String, String>   mapAttrs = Map.of("nm1", "val1", "nm2", "val2");
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(100, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.parse("2026-01-12T17:48:00Z"), Duration.ofMillis(3));
        
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(2, "Cols1:", ImageFactorySpec.from());
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1); 

        // Creator the frame factory specification with the parsing creator and check configuration
        try {
            FrameFactorySpec    specTest = FrameFactorySpec.parse(arrArgs);
            
            Assert.assertEquals(setTags, specTest.setTags());
            Assert.assertTrue(FrameFactorySpecTest.assertEqualsAttrs(mapAttrs, specTest));
            Assert.assertEquals(specTms, specTest.specTms());
            Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());

        } catch (Exception e) {
            Assert.fail("Frame specification parser creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse3() {
        
        // Test Parameters
        final String[]              arrArgs = ARR_ARGS_PARSE_3;
        final Set<String>           setTags = FrameFactorySpecTest.SET_TAGS_FRM_DEF.stream().collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        setTags.add("tag3");
        final Map<String, String>   mapAttrs = FrameFactorySpecTest.MAP_ATTRS_FRM_DEF;
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(100, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.parse("2026-01-12T17:48:00Z"), Duration.ofMillis(3));
        
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(2, "Cols1:", ImageFactorySpec.from());
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(100, "Cols2:", ScalarFactorySpec.from(JalScalarType.DOUBLE));
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2); 

        // Creator the frame factory specification with the parsing creator and check configuration
        try {
            FrameFactorySpec    specTest = FrameFactorySpec.parse(arrArgs);
            
            Assert.assertEquals(setTags, specTest.setTags());
            Assert.assertTrue(FrameFactorySpecTest.assertEqualsAttrs(mapAttrs, specTest));
            Assert.assertEquals(specTms, specTest.specTms());
            Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());

        } catch (Exception e) {
            Assert.fail("Frame specification parser creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse4() {
        
        // Test Parameters
        final String[]              arrArgs = ARR_ARGS_PARSE_4;
        final Set<String>           setTags = FrameFactorySpecTest.createDefaultFrameTags();
        setTags.add("tag3");
        final Map<String, String>   mapAttrs = FrameFactorySpecTest.MAP_ATTRS_FRM_DEF.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        mapAttrs.put("nm1", "val1");
        mapAttrs.put("nm2", "val2");
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(100, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.parse("2026-01-12T17:48:00Z"), Duration.ofMillis(3));
        
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(2, "Cols1:", ImageFactorySpec.from());
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(1, "Cols2:", StructureFactorySpec.from(4, 2, true, ScalarFactorySpec.from(JalScalarType.INTEGER, false, 0, 2)));
        final FrameColumnsSpec<Record>          specCols3 = FrameColumnsSpec.from(100, "Cols3:", ScalarFactorySpec.from(JalScalarType.DOUBLE));
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2, specCols3); 

        // Creator the frame factory specification with the parsing creator and check configuration
        try {
            FrameFactorySpec    specTest = FrameFactorySpec.parse(arrArgs);
            
            Assert.assertEquals(setTags, specTest.setTags());
            Assert.assertTrue(FrameFactorySpecTest.assertEqualsAttrs(mapAttrs, specTest));
            Assert.assertEquals(specTms, specTest.specTms());
            Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());

        } catch (Exception e) {
            Assert.fail("Frame specification parser creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#parse(java.lang.String[])}.
     */
    @Test
    public final void testParse5() {
        
        // Test Parameters
        final String[]              arrArgs = ARR_ARGS_PARSE_5;
        final Set<String>           setColNms = Set.of("PV1", "PV2", "PV3");
        final Set<String>           setTags = Set.of("tag1", "tag2", "tag3");
        final Map<String, String>   mapAttrs = Map.of("nm1", "val1", "nm2", "val2");
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(100, Duration.ofMillis(1), DpTimestampCase.TIMESTAMP_LIST, Instant.parse("2026-01-12T17:48:00Z"), Duration.ofMillis(3));
        
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(setColNms, ImageFactorySpec.from());
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1); 

        // Creator the frame factory specification with the parsing creator and check configuration
        try {
            FrameFactorySpec    specTest = FrameFactorySpec.parse(arrArgs);
            
            Assert.assertEquals(setTags, specTest.setTags());
            Assert.assertTrue(FrameFactorySpecTest.assertEqualsAttrs(mapAttrs, specTest));
            Assert.assertEquals(specTms, specTest.specTms());
            Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());

        } catch (Exception e) {
            Assert.fail("Frame specification parser creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#defaultFrame()}.
     * @throws NoSuchElementException 
     * @throws UnsupportedOperationException 
     * @throws ConfigurationException 
     * @throws TypeNotPresentException 
     * @throws IllegalArgumentException 
     * @throws NumberFormatException 
     */
    @Test
    public final void testDefaultFrame() throws NumberFormatException, IllegalArgumentException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        
        // Test Parameters
        final Set<String>           setTags = createDefaultFrameTags();
        final Map<String, String>   mapAttrs = createDefaultFrameAttributes();
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.defaultFrame();
        
        final List<FrameColumnsSpec<Record>>    lstColsSpecs = FrameColumnsSpec.defaultFrame(); // throws exceptions
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = new TreeSet<>(lstColsSpecs); 

        // Create default frame factory specification and check configuration
        try {
            FrameFactorySpec    specTest = FrameFactorySpec.defaultFrame(); // throws exceptions

            Assert.assertEquals(setTags, specTest.setTags());
            Assert.assertTrue(FrameFactorySpecTest.assertEqualsAttrs(mapAttrs, specTest));
            Assert.assertEquals(specTms, specTest.specTms());
            Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());

            // Print out default frame factory specification (tests FrameFactorySpec#toString)
            System.out.println(JavaRuntime.getQualifiedMethodNameSimple());
            System.out.println("Default FrameFactorySpec Configuration");
            System.out.println(specTest);

        } catch (Exception e) {
            Assert.fail("Default frame specification creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#newFactory()}.
     */
    @Test
    public final void testNewFactory() {
        
        // Test Parameters
        final int                   cntSamples = 43;
        final int                   cntCols = 101;
        final DpTimestampCase       enmTmsCase = DpTimestampCase.TIMESTAMP_LIST;
        final Duration              durPeriod = Duration.ofMillis(1);
        final Instant               insStart = Instant.now();
        final Duration              durDelay = Duration.ofMillis(3);
        final Set<String>           setTags = Set.of("Big", "Beautiful", "Trail");
        final Map<String, String>   mapAttrs = Map.of("Adjective", "Big", "Genetive", "Beautiful", "Nomitive", "Trail");
        
        final FrameTimestampsSpec   specTms = FrameTimestampsSpec.from(cntSamples, durPeriod, enmTmsCase, insStart, durDelay);
        final FrameColumnsSpec<Record>          specCols1 = FrameColumnsSpec.from(1, "Cols1:", JalComplexType.IMAGE);
        final FrameColumnsSpec<Record>          specCols2 = FrameColumnsSpec.from(100, "Cols2:", ScalarFactorySpec.from(JalScalarType.DOUBLE, false, 0, 0.1)); 
        final Set<FrameColumnsSpec<Record>>     setColsSpecs = Set.of(specCols1, specCols2); 

        // Create default frame factory specification and check configuration
        FrameFactorySpec    specTest = FrameFactorySpec.from(setTags, mapAttrs, specTms, setColsSpecs); // throws exceptions
        
        Assert.assertEquals(setTags, specTest.setTags());
        Assert.assertEquals(mapAttrs, specTest.mapAttrs());
        Assert.assertEquals(specTms, specTest.specTms());
        Assert.assertEquals(setColsSpecs, specTest.setColsSpecs());
        
        // Create an ingestion frame factory and check configuration
        IFrameFactory       facFrames = specTest.newFactory();
        
        Assert.assertEquals(cntSamples, facFrames.getSampleCount());
        Assert.assertEquals(cntCols, facFrames.getColumnCount());
        Assert.assertEquals(enmTmsCase, facFrames.getTimestampType());
        
        // Create an ingestion frame and check configuration
        IngestionFrame  frame = facFrames.nextFrame();
        
        Assert.assertEquals(setTags, frame.getTags());
        Assert.assertEquals(mapAttrs, frame.getAttributes());
        Assert.assertEquals(cntSamples, frame.getRowCount());
        Assert.assertEquals(cntCols, frame.getColumnCount());
        
        // Check the frame timestamps
        UniformSamplingClock    clk = frame.getSamplingClock();
        List<Instant>           lstTms = frame.getTimestampList();
        
        Assert.assertNull(clk);
        
        Instant insTms = insStart.plus(durDelay);
        Instant insPrev = null;
        for (Instant insCurr : lstTms) {
            
            // Check current timestamp and advance
            Assert.assertEquals(insTms, insCurr);
            insTms = insTms.plus(durPeriod);
            
            // Check sampling interval between timestamps (if not first time through)
            if (insPrev == null) {
                insPrev = insCurr;
                continue;
            }
            
            Duration    durIval = Duration.between(insPrev, insCurr);
            Assert.assertEquals(durPeriod, durIval);
            insPrev = insCurr;
        }
        
        // Check frame columns
        IDataColumn<Object>     col1 = frame.getDataColumn(0);
        
        Assert.assertEquals("Cols1:0", col1.getName());
        Assert.assertEquals(DpSupportedType.IMAGE, col1.getType());
        Assert.assertEquals(cntSamples, col1.getSize().intValue());
        
        for (int iCol=1; iCol<cntCols; iCol++) {
            String              strNm = "Cols2:" + Integer.toString(iCol-1); 
            IDataColumn<Object> col = frame.getDataColumn(strNm);
            
            Assert.assertEquals(strNm, col.getName());
            Assert.assertEquals(DpSupportedType.DOUBLE, col.getType());
            Assert.assertEquals(cntSamples, col.getSize().intValue());
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec#equals(java.lang.Object)}.
     */
    @Test
    public final void testEquals() {
        
        // Create default frame factory specification with and w/out metadata
        try {
            FrameFactorySpec    specDef = FrameFactorySpec.defaultFrame();

            FrameFactorySpec    specTest = FrameFactorySpec.from();
            specTest.setTags().addAll(FrameFactorySpecTest.createDefaultFrameTags());
            specTest.mapAttrs().putAll(FrameFactorySpecTest.createDefaultFrameAttributes());
            
            LST_ATTR_NMS_IGNORE.forEach(key -> specDef.mapAttrs().remove(key));
            LST_ATTR_NMS_IGNORE.forEach(key -> specTest.mapAttrs().remove(key));
            
            Assert.assertEquals(specDef, specTest);

        } catch (Exception e) {
            Assert.fail("Default specification or empty specification creation failed with exception " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates and returns the set of tag values for the default ingestion frame.
     * </p>
     * 
     * @return  new set of tag values for the default ingestion frame configured by <code>FrameFactorySpec</code>
     */
    private static Set<String>  createDefaultFrameTags() {
        Set<String>     setTags = new TreeSet<>();
        
        if (BOL_TAGS_DEF_ENBL)
            setTags.addAll(SET_TAGS_FRM_DEF);
        if (BOL_TAGS_CLS_ENBL)
            setTags.addAll(SET_TAGS_FRM_CLS);
        
        return setTags;
    }
    
    /**
     * <p>
     * Creates and returns the collection of (name, value) attribute pairs for the default ingestion frame.
     * 
     * @return  new map of attribute pair collection for the default ingestion frame configured by <code>FrameFactorySpec</code>
     */
    private static Map<String, String>  createDefaultFrameAttributes() {
        Map<String, String> mapAttrs = new HashMap<>();
        
        if (BOL_ATTRS_DEF_ENBL)
            mapAttrs.putAll(MAP_ATTRS_FRM_DEF);
        if (BOL_ATTRS_CLS_ENBL)
            mapAttrs.putAll(MAP_ATTRS_FRM_CLS);
        
        return mapAttrs;
    }
    
    /**
     * <p>
     * Compares the collection of attribute value pairs against the given <code>FrameFactorySpec</code> attributes.
     * </p>
     * <p>
     * Compares for equivalence the give collection of (name, value) attribute pairs against the 
     * <code>{@link FrameFactorySpec#mapAttrs()}</code> attributes.  
     * Any attribute within the list <code>{@link #LST_ATTR_NMS_IGNORE}</code> is ignored in this comparison.
     * </p>
     * 
     * @param mapAttrsExpect    expected collection of (name, value) attribute pairs
     * @param specTest          the <code>FrameFactorySpec</code> instance under test
     * 
     * @return  <code>true</code> if the attributes are equivalent except for those named in <code>{@link #LST_ATTR_NMS_IGNORE}</code>,
     *          <code>false</code> otherwise
     */
    private static boolean assertEqualsAttrs(Map<String, String> mapAttrsExpect, FrameFactorySpec specTest) {
        
        boolean     bolResult = true;
        for (Map.Entry<String, String> entry : mapAttrsExpect.entrySet()) {
            
            // Check if this is an ignored attribute
            boolean bolIgnore = LST_ATTR_NMS_IGNORE.stream().anyMatch(key -> key.equals(entry.getKey()));
            if (bolIgnore)
                continue;
            
            // Compare attribute values
            String  strValExpect = entry.getValue();
            String  strValTest = specTest.mapAttrs().get(entry.getKey());
            
            bolResult = bolResult && strValExpect.equals(strValTest);
        }
        
        return bolResult;
    }
    
}
