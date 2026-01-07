/*
 * Project: dp-jal
 * File:	IngestionFrameFactory.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.frames
 * Type: 	IngestionFrameFactory
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
 * @since Jan 2, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.frames;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.common.DpTimestampCase;
import com.ospreydcs.dp.jal.common.IDataColumn;
import com.ospreydcs.dp.jal.ingest.IngestionFrame;
import com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameColumnsFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameTimestampsFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ByteArrayFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ImageFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.cols.JalToolsColumnsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.frames.JalToolsFramesConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.frames.JalToolsFramesTmsConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Class for generating <code>IngestionFrame</code> instances containing simulated data.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 2, 2026
 *
 */
public class IngestionFrameFactory implements IFrameFactory {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>IngestionFrameFactory</code> instance with all default configuration.
     * </p>
     * <p>
     * This creator uses default arguments from the JAL Tools default configuration.  Default arguments are taken
     * from the default ingestion frame configuration parameters.  We have the following:
     * <ul>
     * <li><code>facTms = {@link #CFG_DEF}.timestamps</code>.</li>
     * <li><code>conFacCols = {@link #CFG_DEF}.columns</code>.</li>
     * </ul>
     * </p>
     * <p>
     * This method creates ingestion frame factories producing the default ingestion frame as specified in the
     * JAL Tools default configuration.  It is equivalent to the creator <code>{@link #defaultFrame()}</code>.
     * </p> 
     * 
     * @return  a new <code>IngestionFrameFactory</code> ready for simulated ingestion frame creation
     * 
     * @throws ConfigurationException           the tensor shape was invalid (e.g., an axis size could not be parsed, non-positive axis size, etc.)
     * @throws TypeNotPresentException          unknown <code>JalScalarType</code> enumeration constant
     * @throws NumberFormatException            invalid numeric format (bad 'numIncr' or 'lngSeed') 
     * @throws UnsupportedOperationException    unable to create <code>{@link #numIncr}</code> field for numeric value type
     * @throws NoSuchElementException           unrecognized <code>{@link JalComplexType}</code> constant in argument  
     */
    public static IngestionFrameFactory from() throws NumberFormatException, ConfigurationException, TypeNotPresentException, UnsupportedOperationException, NoSuchElementException {
        return IngestionFrameFactory.defaultFrame();    // throws all exceptions
    }
    
    /**
     * <p>
     * Creates and returns a new <code>IngestionFrameFactory</code> instance configured from the given arguments.
     * </p>
     * <p>
     * This creator uses default arguments from the JAL Tools default configuration.  Default arguments are taken
     * from the default ingestion frame configuration parameters.  We have the following:
     * <ul>
     * <li><code>conFacCols = {@link #CFG_DEF}.columns</code>.</li>
     * </ul>
     * 
     * @param facTims       the frame timestamps factory used to generate ingestion frame timestamps
     * 
     * @return  a new <code>IngestionFrameFactory</code> ready for simulated ingestion frame creation
     * 
     * @throws ConfigurationException           the tensor shape was invalid (e.g., an axis size could not be parsed, non-positive axis size, etc.)
     * @throws TypeNotPresentException          unknown <code>JalScalarType</code> enumeration constant
     * @throws NumberFormatException            invalid numeric format (bad 'numIncr' or 'lngSeed') 
     * @throws UnsupportedOperationException    unable to create <code>{@link #numIncr}</code> field for numeric value type
     * @throws NoSuchElementException           unrecognized <code>{@link JalComplexType}</code> constant in argument  
     */
    public static IngestionFrameFactory from(IFrameTimestampsFactory facTms) throws NumberFormatException, ConfigurationException, TypeNotPresentException, UnsupportedOperationException, NoSuchElementException {
        Collection<IFrameColumnsFactory<Object>>    conFacCols = IngestionFrameFactory.extractDefaultColumns(); // throws all exceptions

        return IngestionFrameFactory.from(facTms, conFacCols);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>IngestionFrameFactory</code> instance configured from the given arguments.
     * </p>
     * <p>
     * This creator is equivalent to the canonical constructor 
     * <code>{@link #IngestionFrameFactory(IFrameTimestampsFactory, Collection)}</code>.
     * </p>
     * 
     * @param facTims       the frame timestamps factory used to generate ingestion frame timestamps
     * @param conFacCols    the frame columns factories used to generate ingestion frames data columns
     * 
     * @return  a new <code>IngestionFrameFactory</code> ready for simulated ingestion frame creation
     */
    public static IngestionFrameFactory from(IFrameTimestampsFactory facTims, Collection<IFrameColumnsFactory<Object>> conFacCols) {
        return new IngestionFrameFactory(facTims, conFacCols);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>IngestionFrameFactory</code> instance producing the default ingestion frame.
     * </p>
     * <p>
     * The JAL Tools default configuration contains a default ingestion frame configuration.  This configuration is
     * used by ingestion frame factories to create <code>{@link IngestionFrame}</code> instances when no explicit
     * configuration is given.
     * This creator retrieves the default ingestion frame configuration and creates the returned ingestion frame
     * factory using these configuration parameters.
     * </p>
     * <p>
     * The method retrieves the default timestamp configuration in the <code>{@link JalToolsFramesTmsConfig}</code>
     * structure class, and the data column specifications contained in the <code>{@link JalToolsColumnsConfig}</code>
     * structure class list within the <code>{@link JalToolsConfig}</code> default configuration.
     * The parameters for the frame timestamps are parsed and the frame timestamps factory is created.  
     * The parameters for each column are parsed and a frame columns factory is created for each column.
     * The column configurations are returned in the order in which they appear in the JAL Tools default configuration.
     * </p>
     * 
     * @return  a new <code>IngestionFrameFactory</code> configured as in the JAL Tools default configuration
     * 
     * @throws ConfigurationException           the tensor shape was invalid (e.g., an axis size could not be parsed, non-positive axis size, etc.)
     * @throws TypeNotPresentException          unknown <code>JalScalarType</code> enumeration constant
     * @throws NumberFormatException            invalid numeric format (bad 'numIncr' or 'lngSeed') 
     * @throws UnsupportedOperationException    unable to create <code>{@link #numIncr}</code> field for numeric value type
     * @throws NoSuchElementException           unrecognized <code>{@link JalComplexType}</code> constant in argument  
     */
    public static IngestionFrameFactory defaultFrame() throws NumberFormatException, ConfigurationException, TypeNotPresentException, UnsupportedOperationException, NoSuchElementException {
        IFrameTimestampsFactory                     facTms = IngestionFrameFactory.extractDefaultTimestamps();
        Collection<IFrameColumnsFactory<Object>>    conFacCols = IngestionFrameFactory.extractDefaultColumns(); // throws all exceptions
        
        return IngestionFrameFactory.from(facTms, conFacCols);
    }
    
    
    //
    // JAL Library Resources
    //
    
    /** Default configuration parameters for the JAL Tools */
    private static final JalToolsFramesConfig     CFG_DEF = JalToolsConfig.getInstance().datagen.frames;
    
    
    //
    // Class Constants
    //
    
    /** Name of the frame generator */
    public static final String  STR_SRC_NAME = IngestionFrameFactory.class.getSimpleName();
    
    /** Environment variable for current user */
    public static final String  STR_USERNAME = "USERNAME";
    
    
    /** Default ingestion frame tag values */
    @SuppressWarnings("unused")
    private static final Set<String>            SET_FRM_TAGS_DEF = new TreeSet<>( CFG_DEF.tags.values );
    
    /** Default ingestion frame attribute pairs */
    @SuppressWarnings("unused")
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
        MAP_FRM_ATTRS_CLS.put("Initiatiated", insNow.toString());
        MAP_FRM_ATTRS_CLS.put("User", strUser);
    }
    
    
    //
    // Defining Attributes
    //
    
    /** The ingestion frame timestamps factory */
    private final IFrameTimestampsFactory               facTms;
    
    /** The ingestion frame data columns factories */
    private final Set<IFrameColumnsFactory<Object>>     setFacCols = new TreeSet<>();;
    
    
    //
    // Consistent Attributes
    //
    
    /** The number of timestamps in each generated ingestion frame */
    private final int               cntSamples;
    
    /** The timestamp type of each generated ingestion frame */
    private final DpTimestampCase   enmTmsCase;
    
    
    //
    // Optional Attributes
    //
    
    /** Optional tag values attached to each ingestion frame */
    private final Set<String>           setTags = new TreeSet<>();
    
    /** Optional attribute (name, value) pairs attached to each ingestion frame */
    private final Map<String, String>   mapAttrs = new HashMap<>();
    
    
    //
    // State Variables
    //
    
    /** The current ingestion frame index (i.e., the number of ingestion frames created) */
    private int     indFrame;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>IngestionFrameFactory</code> instance.
     * </p>
     *
     * @param facTims       the frame timestamps factory used to generate ingestion frame timestamps
     * @param conFacCols    the frame columns factories used to generate ingestion frames data columns
     */
    public IngestionFrameFactory(IFrameTimestampsFactory facTms, Collection<IFrameColumnsFactory<Object>> conFacCols) {
        this.facTms = facTms;
        this.setFacCols.addAll(conFacCols);
        
        this.cntSamples = facTms.getSampleCount();
        this.enmTmsCase = facTms.getTimestampCase();
    }
    
    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Attaches the default ingestion frame tag values to all generated ingestion frames henceforth.
     * </p>
     * <p>
     * The tag values of the default ingestion frame specified in the JAL Tools default configuration are
     * added to the current tag value set for all ingestion frames.  These values are extracted from the default 
     * configuration and contained in the class constant <code>{@link #SET_FRM_TAGS_DEF}</code>.
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This method can be called at any time.</li>
     * <li>Repeated invocation has no effect as tag values are unique.</li>
     * </ul>
     * </p>
     */
    public void attachDefaultTags() {
        this.setTags.addAll(SET_FRM_TAGS_DEF);
    }
    
    /**
     * <p>
     * Attaches the class ingestion frame tag values to all generated ingestion frames henceforth.
     * </p>
     * <p>
     * The class tag values for <code>IngestionFrameFactory</code> are added to the current tag value set for all 
     * ingestion frames.
     * These values are created by the <code>IngestionFrameFactory</code> class and contained in the class constant 
     * <code>{@link #SET_FRM_TAGS_CLS}</code>.
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This method can be called at any time.</li>
     * <li>Repeated invocation has no effect as tag values are unique.</li>
     * </ul>
     * </p>
     */
    public void attachClassTags() {
        this.setTags.addAll(SET_FRM_TAGS_CLS);
    }
    
    /**
     * <p>
     * Attaches the given tag value to all generated ingestion frames henceforth.
     * </p>
     * <p>
     * The given tag value is added to the current set of tag values for all ingestion frames.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This method can be called at any time.</li>
     * <li>Repeated tag values are ignored as tag values are unique.</li>
     * </ul>
     * </p>
     * 
     * @param strTag    tag value to be added to current tag value set
     */
    public void attachTag(String strTag) {
        this.setTags.add(strTag);
    }
    
    /**
     * <p>
     * Attaches the given collection of tag values to all generated ingestion frames henceforth.
     * <p>
     * <p>
     * The given collection of tag values are added to the current tag value set for all ingestion frames.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This method can be called at any time.</li>
     * <li>Repeated tag values are ignored as tag values are unique.</li>
     * </ul>
     * </p>
     *  
     * @param conTags   collection of tag values to be added to current tag value set
     */
    public void attachTags(Collection<String> conTags) {
        this.setTags.addAll(conTags);
    }
    
    /**
     * <p>
     * Attaches the default ingestion frame attribute pairs to all generated ingestion frames henceforth.
     * </p>
     * <p>
     * The attribute (name, value) pairs of the default ingestion frame specified in the JAL Tools default configuration 
     * are added to the current attribute collection for all ingestion frames.  These pairs are extracted from the default 
     * configuration and contained in the class constant <code>{@link #MAP_FRM_ATTRS_DEF}</code>.
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This method can be called at any time.</li>
     * <li>Repeated invocation has no effect as attribute names are unique.</li>
     * </ul>
     * </p>
     */
    public void attachDefaultAttributes() {
        this.mapAttrs.putAll(MAP_FRM_ATTRS_DEF);
    }
    
    /**
     * <p>
     * Attaches the class ingestion frame attribute pairs to all generated ingestion frames henceforth.
     * </p>
     * <p>
     * The class (name, value) attribute pairs for <code>IngestionFrameFactory</code> are added to the current 
     * attribute pair collection for all ingestion frames.
     * These pairs are created by the <code>IngestionFrameFactory</code> class and contained in the class constant 
     * <code>{@link #MAP_FRM_ATTRS_CLS}</code>.
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This method can be called at any time.</li>
     * <li>Repeated invocation has no effect as attribute names are unique.</li>
     * </ul>
     * </p>
     */
    public void attachClassAttributes() {
        this.mapAttrs.putAll(MAP_FRM_ATTRS_CLS);
    }
    
    /**
     * <p>
     * Adds the given (name, value) attribute pair to all generated ingestion frames henceforth.
     * </p>
     * <p>
     * The given (name, value) attribute pair is added to the current collection of attribute pairs for all 
     * ingestion frames.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This method can be called at any time.</li>
     * <li>Attributes with same names are overwritten as they must be unique.</li>
     * </ul>
     * </p>
     * 
     * @param strName   attribute name
     * @param strValue  attribute value
     */
    public void attachAttribute(String strName, String strValue) {
        this.mapAttrs.put(strName, strValue);
    }
    
    /**
     * <p>
     * Attaches the given collection of (name, value) attribute pairs to all generated ingestion frames henceforth.
     * </p>
     * <p>
     * The given collection of (name, value) attribute pairs are added to the current collection of attribute pairs
     * for all ingestion frames.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This method can be called at any time.</li>
     * <li>Attributes with same names are overwritten as they must be unique.</li>
     * </ul>
     * </p>
     * 
     * @param mapAttrs
     */
    public void attachAttributes(Map<String, String> mapAttrs) {
        this.mapAttrs.putAll(mapAttrs);
    }
    
    /**
     * <p>
     * Adds the given frame columns factory to the current collection of ingestion frame columns factories.
     * </p>
     * <p>
     * The given frame columns factory will be used henceforth in the creation of all ingestion frames henceforth.
     * That is, after invocation, all ingestion frames produced by <code>{@link #nextFrame()}</code> will contain
     * data columns generated by the given frame columns factory.
     * </p>
     * 
     * @param facCols   frame columns factory to be added to current columns factories
     */
    public void addFrameColumns(IFrameColumnsFactory<Object> facCols) {
        this.setFacCols.add(facCols);
    }
    
    /**
     * <p>
     * Adds the given collection of frame columns factory to the current collection of ingestion frame columns factories.
     * </p>
     * <p>
     * The given frame columns factories will be used henceforth in the creation of all ingestion frames henceforth.
     * That is, after invocation, all ingestion frames produced by <code>{@link #nextFrame()}</code> will contain
     * data columns generated by the given frame columns factories.
     * </p>
     * 
     * @param conFacCols    collection of frame columns factory to be added to current columns factories
     */
    public void addFrameColumns(Collection<IFrameColumnsFactory<Object>> conFacCols) {
        this.setFacCols.addAll(conFacCols);
    }

    
    // 
    // State Inquiry
    //
    
    /**
     * <p>
     * Retrieves and returns the total column count for each ingestion frame produced.
     * </p>
     * <p>
     * Iterates through the current collection of frame column factories and retrieves the column 
     * count from each frame column factory.
     * The counts for each factory are then summed and returned.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This is not a strict getter operation and, thus, requires some computational resources to perform.
     * </p>  
     * 
     * @return  the total number of columns for each ingestion frame produced
     */
    public int  retrieveCountCount() {
        int     cntCols = this.setFacCols.stream().mapToInt(fac -> fac.getColumnCount()).sum();
        
        return cntCols;
    }
    
    /**
     * <p>
     * Retrieves and returns all the ingestion frame column names from the frame column factories.
     * </p>
     * <p>
     * Iterates through the current collection of frame column factories and retrieves the collection
     * of frame column names from each factory.  The entire set of column names is then aggregated and
     * returned.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This is not a strict getter operation and, thus, requires some computational resources to perform.
     * </p>  
     * 
     * @return  immutable set of all column names for each ingestion frame produced
     */
    public Set<String>  retrieveColumnNames() {
        Set<String> setColNms = this.setFacCols
                .stream()
                .<Set<String>>map(fac -> fac.getColumnNames())
                .<String>flatMap(set -> set.stream())
                .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        
        return setColNms;
    }
    
    /**
     * <p>
     * Retrieves and returns the various frame column data types for each ingestion frame produced.
     * </p>
     * <p>
     * Iterates through the current collection of frame column factories and retrieves the
     * data type it produces.  The data types are then aggregated and returned.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This is not a strict getter operation and, thus, requires some computational resources to perform.
     * </p>  
     *  
     * @return  immutable set of data types contained in each ingestion frame produced
     */
    public Set<DpSupportedType>    retrieveColumnTypes() {
        Set<DpSupportedType>   setColTypes = this.setFacCols
                .stream()
                .map(fac -> fac.getColumnType())
                .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        
        return setColTypes;
    }
    
    /**
     * <p>
     * Retrieves and returns the number of frame data columns of each column type in the produced ingestion frames.
     * </p>
     * <p>
     * Iterates through the current collection of frame column factories and extracts the data type and number of
     * columns for each factory.  The number of columns for each data type (regardless of column factory) are summed
     * and put as the mapped column count for that data type.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This is not a strict getter operation and, thus, requires some computational resources to perform.
     * </p>  
     * 
     * @return  immutable map of data type to column counts of that type
     */
    public Map<DpSupportedType, Integer>    retrieveColumnTypeCount() {
        Map<DpSupportedType, Integer>   mapTypeToCnt = this.setFacCols
                .stream()
                .collect(
                        Collectors.toMap(fac -> fac.getColumnType(),    // key map 
                                         fac -> fac.getColumnCount(),   // value map
                                         (i1, i2) -> i1 + i2)           // value merge function
                        );
        
        return mapTypeToCnt;
    }
    
    /**
     * <p>
     * Retrieves and returns the set of frame data column names for each column type in the produced ingestion frames.
     * </p>
     * <p>
     * Iterates through the current collection of frame column factories and extracts the data type and set of column names
     * for each factory.  The set of column names for each data type is aggregated (regardless of column factory) 
     * then put as the mapped name set for that data type.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This is not a strict getter operation and, thus, requires some computational resources to perform.
     * </p>  
     * @return
     */
    public Map<DpSupportedType, Set<String>>   retrieveColumnTypeNames() {
        Map<DpSupportedType, Set<String>>  mapTypeToNms = this.setFacCols
                .stream()
                .collect(
                        Collectors.toMap(fac -> fac.getColumnType(), 
                                         fac -> fac.getColumnNames(), 
                                         (set1, set2) -> { 
                                             TreeSet<String> set3 = new TreeSet<>(set1);
                                             set3.addAll(set2);
                                             return set3;
                                             }
                                         )
                        );
        
        return mapTypeToNms;
    }
    
    
    //
    // IFrameFactory Interface
    //
    
    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IFrameFactory#getSampleCount()
     */
    @Override
    public int getSampleCount() {
        return this.cntSamples;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IFrameFactory#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return this.setFacCols.stream().mapToInt(fac -> fac.getColumnCount()).sum();
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IFrameFactory#getTimestampType()
     */
    @Override
    public DpTimestampCase getTimestampType() {
        return this.enmTmsCase;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IFrameFactory#nextFrame()
     */
    @Override
    public IngestionFrame nextFrame() throws IllegalArgumentException, IllegalStateException, DateTimeException,
            ArithmeticException, UnsupportedOperationException {
        
        // Check state
        if (this.setFacCols.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - There are no frame column factories for column creation.");
        
        String                          strLabel = this.nextFrameLabel();
        Instant                         insFrmStart = this.facTms.nextFrameStart();
        ArrayList<IDataColumn<Object>>  vecCols = this.nextColumns();
        
        IngestionFrame frmNext = switch (this.enmTmsCase) {
        case SAMPLING_CLOCK -> IngestionFrame.from(this.facTms.nextUniformClock(), vecCols);   // throws IllegalArgumentException
        case TIMESTAMP_LIST -> IngestionFrame.from(this.facTms.nextTimestampVector(), vecCols);// throws DateTimeException, ArithmeticException
        case UNSUPPORTED_CASE -> {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Timestamp case " + this.enmTmsCase + " is not supported.";
            
            throw new UnsupportedOperationException(strMsg);
            }
        default -> { 
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Timestamp case " + this.enmTmsCase + " is not supported.";
            
            throw new UnsupportedOperationException(strMsg);
            }
        };
        
        frmNext.setFrameLabel(strLabel);
        frmNext.setFrameTimestamp(insFrmStart);
        frmNext.addTags(this.setTags);
        frmNext.addAttributes(this.mapAttrs);
        
        return frmNext;
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates a new collection of simulated data columns for the ingestion frame.
     * </p>
     * <p>
     * Uses the collection <code>{@link #lstColFacs}</code> to create each <code>IDataColumn</code>
     * containing simulated data.  One data column is created for each <code>IDataColumnFactory</code>
     * instance.  The returned vector of contains the data column created from each data column
     * factory currently maintained by this ingestion frame factory.
     * </p>   
     *  
     * @return  a new vector of <code>IDataColumn</code> instances containing simulated data
     * 
     * @throws ConfigurationException   created a data column with the wrong size (sample count) 
     */
    private ArrayList<IDataColumn<Object>>  nextColumns() /* throws ConfigurationException */ {
        
        // Create the returned vector container
        ArrayList<IDataColumn<Object>>  vecCols = new ArrayList<>(this.getColumnCount());

        // Populate the vector of columns - check size as we go
        for (IFrameColumnsFactory<Object> fac : this.setFacCols) {
            ArrayList<IDataColumn<Object>>     cols = fac.build(this.cntSamples);
            
            vecCols.addAll(cols);
        }
        
        return vecCols;
    }
    
    /**
     * <p>
     * Creates and returns a new ingestion frame label.
     * </p>
     * The ingestion frame label is created by concatenating the class name <code>{@link #STR_SRC_NAME}</code>
     * with the current frame index <code>{@link #indFrame}</code> value.  The frame index is then
     * incremented for the next frame label.
     * </p>
     * 
     * @return  the next label for an ingestion frame
     */
    private String  nextFrameLabel() {
        String  strLabel = STR_SRC_NAME + "-" + Integer.toString(this.indFrame);
        
        this.indFrame++;
        
        return strLabel;
    }

    /**
     * <p>
     * Creates and returns a new frame timestamps factory for the default ingestion frame.
     * </p>
     * <p>
     * Extracts the timestamps parameters from the default ingestion frame configuration and
     * uses them to create a new <code>{@link IFrameTimstampsFactory}</code> implementation.
     * </p>
     *  
     * @return  a new <code>IFrameTimestampsFactory</code> implementation configured from the default ingestion frame
     */
    private static IFrameTimestampsFactory  extractDefaultTimestamps() {
        JalToolsFramesTmsConfig  cfgTms = CFG_DEF.timestamps;
        
        int             cntSmpls = cfgTms.count;
        Duration        durPeriod = cfgTms.periodDuration();
        Instant         insStart = cfgTms.startInstant();
        DpTimestampCase enmCase = cfgTms.type;
        Duration        durDelay = cfgTms.delayDuration();
        
        FrameTimestampsFactory   facTms = FrameTimestampsFactory.from(cntSmpls, durPeriod, insStart, enmCase, durDelay);
        
        return facTms;
    }

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
     * @throws ConfigurationException           the tensor shape was invalid (e.g., an axis size could not be parsed, non-positive axis size, etc.)
     * @throws TypeNotPresentException          unknown <code>JalScalarType</code> enumeration constant
     * @throws NumberFormatException            invalid numeric format (bad 'numIncr' or 'lngSeed') 
     * @throws UnsupportedOperationException    unable to create <code>{@link #numIncr}</code> field for numeric value type
     * @throws NoSuchElementException           unrecognized <code>{@link JalComplexType}</code> constant in argument  
     */
    private static Collection<IFrameColumnsFactory<Object>>  extractDefaultColumns() 
            throws NumberFormatException, ConfigurationException, TypeNotPresentException, UnsupportedOperationException, NoSuchElementException 
    {
        List<JalToolsColumnsConfig> lstCfgCols = CFG_DEF.columns;
        List<IFrameColumnsFactory<Object>> lstFacCols = new ArrayList<>(lstCfgCols.size());
        
        for (JalToolsColumnsConfig cfg : lstCfgCols) {
            String          strNmPref = cfg.name;
            int             cntCols = cfg.count;
            JalComplexType  enmType = cfg.type;
            String[]        arrFacParse = cfg.factory;
            
            Set<String>     setColNms = IntStream.range(0, cntCols).<String>mapToObj(i -> strNmPref + Integer.toString(i)).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
            IDatumFactory   facDatum = IngestionFrameFactory.createDatumFactory(enmType, arrFacParse);  // throws all exceptions

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
     * This is an auxiliary method used by <code>{@link #extractDefaultColumns()}</code>. 
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
     * @throws ConfigurationException           the tensor shape was invalid (e.g., an axis size could not be parsed, non-positive axis size, etc.)
     * @throws TypeNotPresentException          unknown <code>JalScalarType</code> enumeration constant
     * @throws NumberFormatException            invalid numeric format (bad 'numIncr' or 'lngSeed') 
     * @throws UnsupportedOperationException    unable to create <code>{@link #numIncr}</code> field for numeric value type
     * @throws NoSuchElementException           unrecognized <code>{@link JalComplexType}</code> constant in argument  
     */
    private static IDatumFactory    createDatumFactory(JalComplexType enmType, String[] arrArgs) 
            throws NumberFormatException, ConfigurationException, TypeNotPresentException, UnsupportedOperationException, NoSuchElementException 
    {
        return switch (enmType) {
        case SCALAR -> ScalarFactory.parse(arrArgs);
        case TIMESTAMP -> TimestampFactory.parse(arrArgs);
        case BYTES -> ByteArrayFactory.parse(arrArgs);
        case IMAGE -> ImageFactory.parse(arrArgs);
        case TENSOR -> TensorFactory.parse(arrArgs);
        case STRUCTURE -> StructureFactory.parse(arrArgs);
        default -> throw new NoSuchElementException("Unexpected value: " + enmType);
        };
    }
}
