/*
 * Project: dp-jal
 * File:	FrameFactorySpec.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.specs
 * Type: 	FrameFactorySpec
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
 * @since Jan 4, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import java.lang.reflect.MalformedParametersException;
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

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory;
import com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.cols.JalToolsColumnsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.frames.JalToolsFramesConfig;

/**
 * <p>
 * Record specification for configuration ingestion frame factory instances.
 * </p>
 * <p>
 * Record contains configuration parameters for ingestion frame factories, which are implementation of the 
 * <code>{@link IFrameFactory}</code> interface.  Ingestion frame factories provide simulated ingestion data for
 * testing and evaluation of the Data Platform Ingestion Service.
 * </p>
 * <p>
 * <h2>Ingestion Frame Factories</h2>
 * Ingestion frame factories produce simulated data as <code>{@link IngestionFrame}</code> instances recognized by the
 * JAL Library as the unit of ingestion.  These "ingestion frames" contain heterogeneous columns of data aligned with
 * a common set of timestamps.  Thus, ingestion frame factories are typically composed of a single frame timestamps factory
 * and a collection of frame columns factories producing the ingestion frame data columns.
 * </p>   
 *
 * @author Christopher K. Allen
 * @since Jan 4, 2026
 *
 * @param setTags       optional set of tag value for generated ingestion frames
 * @param mapAttrs      optional set of (name, value) attribute pairs for generated ingestion frames
 * @param specTms       frame timestamps factory specification
 * @param setColSpecs   collection of frame columns factories specifications
 * @param bolTagsCls    <s>enable/disable ingestion frame factory class tag values for ingestion frames</s>
 * @param bolAttrsCls   <s>enable/diable ingestion frame factory class attribute pairs for ingestion frames</s>
 * @param bolTagsDef    <s>enable/disable default tag values (from JAL default configuration) for ingesiton frames</s>
 * @param bolAttrsDef   <s>enable/disable default attribute pairs (from JAL default configuration) for ingestion frames</s>
 */
public record FrameFactorySpec(
        Set<String>                     setTags,
        Map<String, String>             mapAttrs,
        FrameTimestampsSpec             specTms,
        Set<FrameColumnsSpec<Record>>   setColSpecs
//        boolean                         bolTagsCls,
//        boolean                         bolAttrsCls,
//        boolean                         bolTagsDef,
//        boolean                         bolAttrsDef
        ) 
{

    
    // 
    // Creators
    //
    
    public static FrameFactorySpec  from(boolean bolTagsDef, boolean bolTagsCls, boolean bolAttrsDef, boolean bolAttrsCls, FrameTimestampsSpec specTms, Set<FrameColumnsSpec<Record>> setColSpecs) {
        
        // Create the frame tag values set
        Set<String> setTags = new TreeSet<>();
        if (bolTagsDef)
            setTags.addAll(SET_TAGS_FRM_DEF);
        if (bolTagsCls)
            setTags.addAll(SET_TAGS_FRM_CLS);
        
        // Create the frame attribute pairs map
        Map<String, String> mapAttrs = new HashMap<>();
        if (bolAttrsDef)
            mapAttrs.putAll(MAP_ATTRS_FRM_DEF);
        if (bolAttrsCls)
            mapAttrs.putAll(MAP_ATTRS_FRM_CLS);

        return FrameFactorySpec.from(setTags, mapAttrs, specTms, setColSpecs);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>FrameFactorySpec</code> record from the given argument values.
     * </p>
     * <p>
     * This creator is equivalent to the canonical constructor
     * <code>{@link #FrameFactorySpec(FrameTimestampsSpec, Set, Set, Map, boolean, boolean, boolean, boolean)}</code>
     * containing arguments for all record field values.
     * </p>
     * 
     * @param setTags       optional set of tag value for generated ingestion frames
     * @param mapAttrs      optional set of (name, value) attribute pairs for generated ingestion frames
     * @param specTms       frame timestamps factory specification
     * @param setColSpecs   collection of frame columns factories specifications
     * 
     * @return  a new <code>FrameFactorySpec</code> record populated with the given argument values
     */
    public static FrameFactorySpec  from(
            Set<String>                     setTags, 
            Map<String, String>             mapAttrs,
            FrameTimestampsSpec             specTms, 
            Set<FrameColumnsSpec<Record>>   setColSpecs 
//            boolean                         bolTagsCls,
//            boolean                         bolAttrsCls,
//            boolean                         bolTagsDef,
//            boolean                         bolAttrsDef
            ) 
    {
        return new FrameFactorySpec(setTags, mapAttrs, specTms, setColSpecs /*, bolTagsCls, bolAttrsCls, bolTagsDef, bolAttrsDef */);
    }
    
    /**
     * <p>
     * Creates a new <code>FrameFactorySpec</code> by parsing the argument as if it is a Java application command-line.
     * </p>
     * <p>
     * <h2>Format</h2>
     * The format of the arguments collection is assumed to be as follows:
     * <code>
     * <pre>
     *   -tagsDef - tagsCls -attrsDef - attrsCls [--tags tag1 ... tagN] [-Aname1=val1 ... -AnameN=valN] 
     *     --tms [samples [period [start [type [delay]]]]] 
     *     --cols [cnt [prefix [DTYPE [parameters]]]] 
     *       ... 
     *     --cols [cnt [prefix [DTYPE [parameters]]]]  
     * </pre>
     * </code> 
     * where 
     * <ul>
     * <li><code>-tagsDef</code> = use default ingestion frame tag values switch (i.e., <code>true</code> if present).</li>
     * <li><code>-tagsCls</code> = use ingestion frame factory class tag values switch (i.e., <code>true</code> if present).</li>
     * <li><code>-attrsDef</code> = use default ingestion frame attribute pairs switch (i.e., <code>true</code> if present).</li>
     * <li><code>-attrsCls</code> = use ingestion frame factory class attribute pairs switch (i.e., <code>true</code> if present).</li>
     * <li><code>[--tags tag1 ... tagN]</code> = optional ingestion frame tag values (i.e., values <code>tag1 ... tagN</code>).</li>
     * <li><code>[-Aname1=val1 ... -AnameN=valN]</code> = optional ingestion frame attribute pairs (i.e., (name1, val1) ... (nameN, valN)).</li>
     * <li><code>--tms [samples [period [start [type [delay]]]]]</code> = ingestion frame timestamps specification.</li>
     * <li><code>--cols [cnt [prefix [DTYPE [parameters]]]]</code> = ingestion frame column specification.</li>
     * </ul>
     * <p>
     * <h2>Delimiters</h2>
     * The delimiters used above are set as record constants and may change in future releases.  Thus, we list below these
     * constants along with their current values as they stand.
     * <ul>
     * <li><code>-tagsDef = {@link #STR_PARSE_TAGS_DEF_SWITCH}</code> = {@value #STR_PARSE_TAGS_DEF_SWITCH}.</li>
     * <li><code>-tagsCls = {@link #STR_PARSE_TAGS_CLS_SWITCH}</code> = {@value #STR_PARSE_TAGS_CLS_SWITCH}.</li>
     * <li><code>-attrsDef = {@link #STR_PARSE_ATTRS_DEF_SWITCH}</code> = {@value #STR_PARSE_ATTRS_DEF_SWITCH}.</li>
     * <li><code>-attrsCls = {@link #STR_PARSE_ATTRS_CLS_SWITCH}</code> = {@value #STR_PARSE_ATTRS_CLS_SWITCH}.</li>
     * <li><code>--tags = {@link #STR_PARSE_TAGS_DVAR}</code> = {@value #STR_PARSE_TAGS_DVAR}.</li>
     * <li><code>-A = {@link #STR_PARSE_ATTRS_DPROP}</code> = {@value #STR_PARSE_ATTRS_DPROP}.</li>
     * <li><code>--tms = {@link #STR_PARSE_TMS_DVAR}</code> = {@value #STR_PARSE_TMS_DVAR}.</li>
     * <li><code>--cols = {@link #STR_PARSE_COLS_DVAR}</code> = {@value #STR_PARSE_COLS_DVAR}.</li>
     * </ul>
     * </p>
     * <p> 
     * <h2>NOTES:</h2>
     * <ul>
     * <li>For further information on frame timestamps specification format see <code>{@link FrameTimestampsSpec#parse(String...)}</code>.</li>
     * <li>For further information on frame columns specifications format see <code>{@link FrameColumnsSpec#parse(String...)}</code>.</li>
     * <li>If the <code>--tms</code> variable is not present the default ingestion frame timestamps are used 
     *     (see <code>{@link FrameTimestampsSpec#defaultFrame()})</code>.</li>
     * <li>If the <code>--cols</code> variable is not present the default ingestion frame data columns are used
     *     (see <code>{@link FrameColumnsSpec#defaultFrame()})</code>.</li>
     * </ul>  
     * </p>
     * 
     * @param args  collection of application command-line arguments
     * 
     * @return  a new <code>FrameFactorySpec</code> instance populated from the given command-line arguments
     * 
     * @throws MissingResourceException missing required parameters - frame timestamps and/or at least one data column  
     * @throws DateTimeParseException   invalid ISO-8605 date/time/duration format for 'period', 'start', or 'delay' 
     * @throws TypeNotPresentException  invalid enumeration constant (e.g., the 1st argument was not a <code>JalComplexType</code>)
     * @throws NumberFormatException    invalid numeric expression (typically for 'lngSeed' value)
     * @throws ConfigurationException   the argument contained the wrong number of arguments for the <code>JalComplexType</code>
     * @throws UnsupportedOperationException invalid field value format (typically 'numIncr' was invalid)
     * @throws MalformedParametersException  an enumeration constant within the argument set was not recognized (IMAGE)
     * @throws NoSuchElementException   the column data type was unrecognized (i.e., 'DTYPE' was not supported)
     */
    public static FrameFactorySpec  parse(String...args) 
            throws MissingResourceException, DateTimeParseException, TypeNotPresentException, 
                   NumberFormatException, ConfigurationException, UnsupportedOperationException, 
                   MalformedParametersException 
    {
        // Check arguments
        if (args==null || args.length==0)
            return FrameFactorySpec.defaultFrame();
        
        // Create default application command-line parser used for extracting all specifications parameters
        AppArgumentsParser  parser = AppArgumentsParser.fromDefault();

        // Extract the frame timestamp parameters and create specification
        final int           cntTmsSpec = parser.parseVariableCount(STR_PARSE_TMS_DVAR, args);
        FrameTimestampsSpec specTms;

        if (cntTmsSpec==0)
            specTms = FrameTimestampsSpec.defaultFrame();
        
        else {
            List<String>    lstTmsArgs = parser.parseVariable(STR_PARSE_TMS_DVAR, args);
            String[]        arrTmsArgs = lstTmsArgs.toArray(new String[lstTmsArgs.size()]);
            specTms = FrameTimestampsSpec.parse(arrTmsArgs);    // throws NumberFormatException, TypeNotPresentException, DateTimeParseException
        }
        
        // Extract frame columns parameters and create specifications collection
        final int                       cntColsSpecs = parser.parseVariableCount(STR_PARSE_COLS_DVAR, args);
        Set<FrameColumnsSpec<Record>>   setColsSpecs = new TreeSet<>();
        
        if (cntColsSpecs==0)
            setColsSpecs.addAll(FrameColumnsSpec.defaultFrame()); // throws IllegalArgumentException, NumberForamtException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, TypeNotPresentException
        
        else {
            setColsSpecs = new TreeSet<>();
            
            for (int iCols=0; iCols<cntColsSpecs; iCols++) {
                List<String>    lstColsArgs = parser.parseVariable(STR_PARSE_COLS_DVAR, iCols, args);
                String[]        arrColsArgs = lstColsArgs.toArray(new String[lstColsArgs.size()]);
                FrameColumnsSpec<Record>    specCols = FrameColumnsSpec.parse(arrColsArgs);
                
                setColsSpecs.add(specCols);
            }
        }
        
        // Extract optional frame tag values and attribute pairs
        Set<String>         setTags = new TreeSet<>(parser.parseVariable(STR_PARSE_TAGS_DVAR, args));
        Map<String, String> mapAttrs = parser.parseProperty(STR_PARSE_ATTRS_DPROP, args);   // throws ConfigurationException
        
        // Supplement optional frame tag value with default and class values if flagged
        if (parser.parseSwitch(STR_PARSE_TAGS_DEF_SWITCH, args))
            setTags.addAll(SET_TAGS_FRM_DEF);
        if (parser.parseSwitch(STR_PARSE_TAGS_CLS_SWITCH, args))
            setTags.addAll(SET_TAGS_FRM_CLS);
        
        // Supplement optional attribute pairs with default and class values if flagged
        if (parser.parseSwitch(STR_PARSE_ATTRS_DEF_SWITCH, args))
            mapAttrs.putAll(MAP_ATTRS_FRM_DEF);
        if (parser.parseSwitch(STR_PARSE_ATTRS_CLS_SWITCH, args))
            mapAttrs.putAll(MAP_ATTRS_FRM_CLS);
            
        
        return FrameFactorySpec.from(setTags, mapAttrs, specTms, setColsSpecs);
    }
    
    /**
     * @return
     * 
     * @throws TypeNotPresentException  invalid enumeration constant (e.g., the 1st argument was not a <code>JalComplexType</code>)
     * @throws NumberFormatException    invalid numeric expression (typically for 'lngSeed' value)
     * @throws ConfigurationException   the argument contained the wrong number of arguments for the <code>JalComplexType</code>
     * @throws UnsupportedOperationException invalid field value format (typically 'numIncr' was invalid)
     * @throws MalformedParametersException  an enumeration constant within the argument set was not recognized (IMAGE)
     * @throws NoSuchElementException   the column data type was unrecognized (i.e., 'DTYPE' was not supported)
     */
    public static FrameFactorySpec  defaultFrame() throws NumberFormatException, IllegalArgumentException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        
        // Create the default ingestion frame tag values set
        Set<String> setTags = new TreeSet<>();
        if (FrameFactorySpec.BOL_TAGS_DEF_ENBL)
            setTags.addAll(SET_TAGS_FRM_DEF);
        if (FrameFactorySpec.BOL_TAGS_CLS_ENBL)
            setTags.addAll(SET_TAGS_FRM_CLS);
        
        // Create the default ingestion frame attribute pairs map
        Map<String, String> mapAttrs = new HashMap<>();
        if (FrameFactorySpec.BOL_ATTRS_DEF_ENBL)
            mapAttrs.putAll(MAP_ATTRS_FRM_DEF);
        if (FrameFactorySpec.BOL_ATTRS_CLS_ENBL)
            mapAttrs.putAll(MAP_ATTRS_FRM_CLS);

        // Create the default ingestion frame timestamps specification and columns specification collection
        FrameTimestampsSpec             specTms = FrameTimestampsSpec.defaultFrame();
        Set<FrameColumnsSpec<Record>>   setColsSpec = new TreeSet<>(FrameColumnsSpec.defaultFrame());  // throws all exceptions
        
        return FrameFactorySpec.from(setTags, mapAttrs, specTms, setColsSpec);
    }
    
    
    //
    // Record Overrides
    //
    
    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see java.lang.Record#toString()
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return null;
    }

    
    //
    // JAL Library Resources
    //

    /** JAL Tools default configuration parameters for ingestion frame factories */
    private static final JalToolsFramesConfig           CFG_FRM_DEF = JalToolsConfig.getInstance().datagen.frames;
    
    /** JAL Tools default configuration parameters for column factories */
    private static final JalToolsColumnsConfig          CFG_COL_DEF = JalToolsConfig.getInstance().datagen.columns;
    
    
    //
    // Record Constants 
    //
    
    /** The frame timestamps specification variable parsing delimiter */
    public static final String  STR_PARSE_TMS_DVAR = "--tms";
    
    /** The frame columns specification variable parsing delimiter */
    public static final String  STR_PARSE_COLS_DVAR = "--cols";
    
    /** The frame optional tags variable parsing delimiter */
    public static final String  STR_PARSE_TAGS_DVAR = "--tags";
    
    /** The frame optional attributes property delimiter */
    public static final String  STR_PARSE_ATTRS_DPROP = "-A";
    
    /** The ingestion frame factory default tags option switch */
    public static final String  STR_PARSE_TAGS_DEF_SWITCH = "-tagsDef";
    
    /** The ingestion frame factory class tags option switch */
    public static final String  STR_PARSE_TAGS_CLS_SWITCH = "-tagsCls";
    
    /** The ingestion frame factory default properties option switch */
    public static final String  STR_PARSE_ATTRS_DEF_SWITCH = "-attrsDef";
    
    /** The ingestion frame factory class properties option switch */
    public static final String  STR_PARSE_ATTRS_CLS_SWITCH = "-attrsCls";
    
    
    /** Name of the specification record - used for class attributes */
    public static final String  STR_SRC_NAME = FrameFactorySpec.class.getSimpleName();
    
    /** Environment variable for current user - used for class attributes */
    public static final String  STR_USERNAME = "USERNAME";
    
    
    //
    // Record Constants and Resources
    //
    
    /** Enable/disable class tag values flag default configuration */
    public static final boolean                 BOL_TAGS_CLS_ENBL = CFG_FRM_DEF.tags.useClass;
    
    /** Class tag values for ingestion frames */
    private static final Set<String>            SET_TAGS_FRM_CLS = new TreeSet<>();

    
    /** Enable/disable class attribute pairs flag default configuration */
    public static final boolean                 BOL_ATTRS_CLS_ENBL = CFG_FRM_DEF.attributes.useClass;
    
    /** Class attribute pairs for ingestion frame */
    private static final Map<String, String>    MAP_ATTRS_FRM_CLS = new HashMap<>();
    
    
    /** Initialization for class tags and attributes for ingestion frames */
    static {
        SET_TAGS_FRM_CLS.add(STR_SRC_NAME);
        SET_TAGS_FRM_CLS.add(JalToolsConfig.STR_CFG_FILE_NAME);
        
        String  strUser = System.getenv(STR_USERNAME);
        Instant insNow = Instant.now();
        
        MAP_ATTRS_FRM_CLS.put("Source", STR_SRC_NAME);
        MAP_ATTRS_FRM_CLS.put("Initiatiated", insNow.toString());
        MAP_ATTRS_FRM_CLS.put("User", strUser);
    }
    
    
    /** Enable/disable default tag values flag default configuration */
    public static final boolean                 BOL_TAGS_DEF_ENBL = CFG_FRM_DEF.tags.useDefault;
    
    /** Default ingestion frame tag values */
    private static final Set<String>            SET_TAGS_FRM_DEF = new TreeSet<>( CFG_FRM_DEF.tags.values );
    
    
    /** Enable/disable default attribute pairs default configuration */
    public static final boolean                 BOL_ATTRS_DEF_ENBL = CFG_FRM_DEF.attributes.useDefault;
    
    /** Default ingestion frame attribute pairs */
    private static final Map<String, String>    MAP_ATTRS_FRM_DEF = new HashMap<>( CFG_FRM_DEF.attributes.pairs );
    
}
