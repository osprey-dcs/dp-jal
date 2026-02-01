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

import java.io.PrintStream;
import java.lang.reflect.MalformedParametersException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.ingest.IngestionFrame;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameColumnsFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameTimestampsFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.IngestionFrameFactory;
import com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
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
 * @param setColsSpecs  collection of frame columns factories specifications
 * @param bolTagsCls    <s>enable/disable ingestion frame factory class tag values for ingestion frames</s>
 * @param bolAttrsCls   <s>enable/diable ingestion frame factory class attribute pairs for ingestion frames</s>
 * @param bolTagsDef    <s>enable/disable default tag values (from JAL default configuration) for ingesiton frames</s>
 * @param bolAttrsDef   <s>enable/disable default attribute pairs (from JAL default configuration) for ingestion frames</s>
 */
public record FrameFactorySpec(
        Set<String>                     setTags,
        Map<String, String>             mapAttrs,
        FrameTimestampsSpec             specTms,
        Set<FrameColumnsSpec<Record>>   setColsSpecs
//        boolean                         bolTagsCls,
//        boolean                         bolAttrsCls,
//        boolean                         bolTagsDef,
//        boolean                         bolAttrsDef
        ) 
{

    
    // 
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>FrameFactorySpec</code> record with the default ingestion frame 
     * timetamps and columns specification and no metadata.
     * </p>
     * <p>
     * This method acquires the default ingestion frame timestamps specification from <code>{@link FrameTimestampsSpec#defaultFrame()}</code>
     * and the default ingestion frame columns specification from <code>{@link FrameColumnsSpec#defaultFrame()}</code> and
     * uses them to populate fields <code>{@link #specTms()}</code> and <code>{@link #setColsSpecs()}</code>, respectively.
     * The metadata fields <code>{@link #setTags()}</code> and <code>{@link #mapAttrs()}</code> are left empty
     * (they can be populated post-creation). 
     * <p>
     * <h2>NOTES:</h2>
     * This method is <b>not</b> equivalent to creator <code>{@link #defaultFrame()}</code>, 
     * which returns the default ingestion frame specification as defined in the JAL Tools default configuration.
     * There the metadata fields are populated with that specified in the JAL Tools default configuration.
     * See method documentation on <code>{@link #defaultFrame()}</code> for additional information.
     * </p>
     *  
     * @return  a new <code>FrameFactorySpec</code> record with the default ingestion frame specification sans tags and attributes
     * 
     * @throws IllegalArgumentException general error (typically bad argument count or enumeration constant not recognized)
     * @throws NumberFormatException    a bad numeric format was encountered (typically integer valued parameter)
     * @throws DateTimeParseException   timestamp factory was specified with bad ISO-8605 date/time/duration format
     * @throws TypeNotPresentException  unrecognized enumeration constant (scalar factory JalScalarType or image factory BufferedImage.Format)   
     * @throws ConfigurationException   tensor factory had bad shape or structure factory missing depth and/or fan-out
     * @throws UnsupportedOperationException    scalar factory had bad 'numIncr' parameter
     * @throws NoSuchElementException   the column type is unrecognized (unsupported) 
     */
    public static FrameFactorySpec  from() throws NumberFormatException, IllegalArgumentException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        FrameTimestampsSpec     specTms = FrameTimestampsSpec.defaultFrame();
        
        return FrameFactorySpec.from(specTms);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>FrameFactorySpec</code> record with the default ingestion frame columns collection 
     * and no metadata.
     * </p>
     * <p>
     * The default ingestion frame data columns specifications are extracted from the JAL Tools default configuration and
     * used for record field <code>{@link #setColsSpecs()}</code> (see <code>FrameColumnsSpec{@link #defaultFrame()}</code>).
     * No metadata (i.e., tag values or attribute pairs) is contained in the returned frame factory specification record.
     * </p>
     * 
     * @param specTms       frame timestamps factory specification
     * 
     * @return  a new <code>FrameFactorySpec</code> record with the given timestamps specification and default columns specifications
     * 
     * @throws IllegalArgumentException general error (typically bad argument count or enumeration constant not recognized)
     * @throws NumberFormatException    a bad numeric format was encountered (typically integer valued parameter)
     * @throws DateTimeParseException   timestamp factory was specified with bad ISO-8605 date/time/duration format
     * @throws TypeNotPresentException  unrecognized enumeration constant (scalar factory JalScalarType or image factory BufferedImage.Format)   
     * @throws ConfigurationException   tensor factory had bad shape or structure factory missing depth and/or fan-out
     * @throws UnsupportedOperationException    scalar factory had bad 'numIncr' parameter
     * @throws NoSuchElementException   the column type is unrecognized (unsupported) 
     */
    public static FrameFactorySpec  from(FrameTimestampsSpec specTms) throws NumberFormatException, IllegalArgumentException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        
        // Create the default ingestion frame columns specification collection
        Set<FrameColumnsSpec<Record>>   setColsSpecs = new TreeSet<>( FrameColumnsSpec.defaultFrame() );    // throws all exceptions
        
        return FrameFactorySpec.from(specTms, setColsSpecs);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>FrameFactorySpec</code> record with the default ingestion frame timestamps and no metadata.
     * </p>
     * <p>
     * The default ingestion frame timestamps are extracted from the JAL Tools default configuration and used
     * for the field <code>{@link #specTms()}</code> (see <code>{@link FrameTimestampsSpec#defaultFrame()}</code>).
     * No metadata (i.e., tag values or attribute pairs) is contained in the returned frame factory specification record.
     * </p>
     * 
     * @param setColsSpecs   collection of frame columns factories specifications
     * 
     * @return  a new <code>FrameFactorySpec</code> record with the given columns specifications collection and default timestamps
     */
    public static FrameFactorySpec  from(Set<FrameColumnsSpec<Record>> setColsSpec) {
        
        // Create the default ingestion frame timestamps specification 
        FrameTimestampsSpec     specTms = FrameTimestampsSpec.defaultFrame();
        
        return FrameFactorySpec.from(specTms, setColsSpec);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>FrameFactorySpec</code> record with no ingestion frame metadata specified.
     * </p>
     * <p>
     * The returned record contains no tag values or attribute pairs.  Specifically, the record fields
     * <code>{@link #setTags()}</code> and <code>{@link #mapAttrs()}</code> are empty containers.
     * The frame factory specification record can be supplemented with metadata post-creation by 
     * accessing these fields.  For example, using <code>{@link Set#add(Object)}</code> and
     * <code>{@link Map#put(Object, Object)}</code>.
     * </p>
     * 
     * @param specTms       frame timestamps factory specification
     * @param setColsSpecs  collection of frame columns factories specifications
     * 
     * @return  a new <code>FrameFactorySpec</code> record with no metadata specifications
     */
    public static FrameFactorySpec  from(FrameTimestampsSpec specTms, Set<FrameColumnsSpec<Record>> setColsSpecs) {
        
        return FrameFactorySpec.from(new TreeSet<>(), new HashMap<>(), specTms, setColsSpecs);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>FrameFactorySpec</code> record using default and record metadata options.
     * </p>
     * <p>
     * Here metadata refers to tag values and attribute pairs attached to ingestion frames.
     * This creator allows clients to create <code>FrameFactorySpec</code> record specifying ingestion frame metadata 
     * obtained from the default ingestion frame metadata and/or the record class metadata.  
     * The arguments allows gross access only to metadata source (i.e., default ingestion frame and record class).
     * Both metadata types (tag values and attribute pairs) are implied in each <code>boolean</code> argument. 
     * </p>
     * <p> 
     * Client specific metadata is not contained in the returned frame factory specification, 
     * but can be supplemented later with direct access to field <code>{@link #setTags()}</code>
     * and <code>{@link #mapAttrs()}</code>.
     * </p>
     * 
     * @param bolMetaDef    include/exclude default frame metadata for ingestion frames (both tag values and attribute pairs)
     * @param bolMetaCls    include/exclude record class metadata for ingestion frames (both tag values and attribute pairs
     * @param specTms       frame timestamps factory specification
     * @param setColsSpecs  collection of frame columns factories specifications
     * 
     * @return  a new <code>FrameFactorySpec</code> record populated according to the given argument configuration
     */
    public static FrameFactorySpec  from(boolean bolMetaDef, boolean bolMetaCls, FrameTimestampsSpec specTms, Set<FrameColumnsSpec<Record>> setColsSpecs) {
        
        return FrameFactorySpec.from(bolMetaDef, bolMetaCls, bolMetaDef, bolMetaCls, specTms, setColsSpecs);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>FrameFactorySpec</code> record using default and record metadata options.
     * </p>
     * <p>
     * Here metadata refers to tag values and attribute pairs attached to ingestion frames.
     * This creator allows clients to create <code>FrameFactorySpec</code> record specifying ingestion frame metadata 
     * obtained from the default ingestion frame metadata and/or the record class metadata. 
     * The arguments allows specific access to each metadata type (i.e., tag values and attribute pairs) and
     * metadata source (i.e., default ingestion frame and record class).
     * </p>
     * <p> 
     * Client specific metadata is not contained in the returned frame factory specification, 
     * but can be supplemented later with direct access to field <code>{@link #setTags()}</code>
     * and <code>{@link #mapAttrs()}</code>.
     * </p>
     * 
     * @param bolTagsDef    include/exclude default frame tag values (from JAL default configuration) for ingesiton frames
     * @param bolTagsCls    include/exclude ingestion frame factory specification record tag values for ingestion frames
     * @param bolAttrsDef   include/exclude default frame attribute pairs (from JAL default configuration) for ingestion frames
     * @param bolAttrsCls   include/exclude ingestion frame factory specification record attribute pairs for ingestion frames
     * @param specTms       frame timestamps factory specification
     * @param setColsSpecs  collection of frame columns factories specifications
     * 
     * @return  a new <code>FrameFactorySpec</code> record populated according to the given argument configuration
     */
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
     * @param setColsSpecs  collection of frame columns factories specifications
     * 
     * @return  a new <code>FrameFactorySpec</code> record populated with the given argument values
     */
    public static FrameFactorySpec  from(
            Set<String>                     setTags, 
            Map<String, String>             mapAttrs,
            FrameTimestampsSpec             specTms, 
            Set<FrameColumnsSpec<Record>>   setColsSpecs 
//            boolean                         bolTagsCls,
//            boolean                         bolAttrsCls,
//            boolean                         bolTagsDef,
//            boolean                         bolAttrsDef
            ) 
    {
        return new FrameFactorySpec(setTags, mapAttrs, specTms, setColsSpecs /*, bolTagsCls, bolAttrsCls, bolTagsDef, bolAttrsDef */);
    }
    
    /**
     * <p>
     * Creates a new <code>FrameFactorySpec</code> by parsing the argument as if it is a Java application command-line.
     * </p>
     * <p>
     * <h2>Formats</h2>
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
     * See <code>{@link FrameTimestampsSpec#parse(String...)}</code> for a description of the <code>--tms</code> parameters
     * and <code>{@link FrameColumnsSpec#parse(String...)}</code> for a description of the <code>--cols</code> parameters.
     * </p>
     * <p>
     * The <code>{@link FrameColumnsSpec#parse(String...)}</code> also supports an additional format for ingestion frame
     * data columns.  The alternate format for frame columns specification is given by the following:
     * <code>
     * <pre>
     *      --cols [colNm1 colNm2 ... colNmN [DTYPE [parameter(s)]]]
     * </pre>
     * </code>
     * where the collection <code>[colNm1 colNm2 ... colNmN]</code> are explicit names for the data columns.  See
     * <code>{@link FrameColumnsSpec#parse(String...)}</code> for additional information on this format.
     * </p>
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
     * @param args  collection of application command-line arguments formatted as above
     * 
     * @return  a new <code>FrameFactorySpec</code> instance populated from the given command-line arguments
     * 
     * @throws IllegalArgumentException general error (typically bad argument count or enumeration constant not recognized)
     * @throws DateTimeParseException   invalid ISO-8605 date/time/duration format for 'period', 'start', or 'delay' 
     * @throws TypeNotPresentException  invalid enumeration constant (e.g., the 1st argument was not a <code>JalComplexType</code>)
     * @throws NumberFormatException    invalid numeric expression (typically for 'lngSeed' value)
     * @throws ConfigurationException   the argument contained the wrong number of arguments for the <code>JalComplexType</code>
     * @throws UnsupportedOperationException invalid field value format (typically 'numIncr' was invalid)
     * @throws MalformedParametersException  an enumeration constant within the argument set was not recognized (IMAGE)
     * @throws NoSuchElementException   the column data type was unrecognized (i.e., 'DTYPE' was not supported)
     */
    public static FrameFactorySpec  parse(String...args) 
            throws IllegalArgumentException, DateTimeParseException, TypeNotPresentException, 
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
//            setColsSpecs = new TreeSet<>();
            
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
     * <p>
     * Creates and returns a new <code>FrameFactorySpec</code> specification for the default ingestion frame.
     * </p>
     * <p>
     * The JAL Tools default configuration <code>{@link JalToolsConfig#STR_CFG_FILE_NAME}</code> contains a 
     * specification for the default ingestion frame.  This create extracts the default ingestion frame properties
     * and uses them to create the returns specification record.
     * </p>
     * <p>
     * <h2>Default Ingestion Frame</h2>
     * Some parameters for the default ingestion frame are extracted from the JAL Tools default configuration
     * and contained in record constants.  Others are taken from the record <code>{@link FrameTimestampsSpec}</code>
     * and <code>{@link FrameColumnsSpec}</code>.  There are also tag values and attribute pairs created in
     * this record that can be added to the frame factory specification.
     * <ul>
     * <li>default tag values = <code>{@link #SET_TAGS_FRM_DEF}</code>, used when <code>{@link #BOL_TAGS_DEF_ENBL} = true</code>.</li>
     * <li>record tag values = <code>{@link #SET_TAGS_FRM_CLS}</code>, used when <code>{@link #BOL_TAGS_CLS_ENBL} = true</code>.</li>
     * <li>default attribute pairs = <code>{@link #MAP_ATTRS_FRM_DEF}</code>, used when <code>{@link #BOL_ATTRS_DEF_ENBL} = true</code>.</li>
     * <li>record attribute pairs = <code>{@link #MAP_ATTRS_FRM_CLS}</code>, used when <code>{@link #BOL_ATTRS_CLS_ENBL} = true</code>.</li>
     * <li>frame timestamps specification = <code>{@link FrameTimestampsSpec#defaultFrame()}</code>.</li>
     * <li>frame data columns specifications = <code>{@link FrameColumnsSpec#defaultFrame()}</code>.</li>
     * </ul>  
     * 
     * @return  a new <code>FrameFactorySpec</code> record containing the default ingestion frame specification
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
    
    /**
     * <p>
     * Creates and returns a new ingestion frame factory instance according to the specifications in this record.
     * </p>
     * <p>
     * The returned ingestion frame factory is configured according to the field values of this record according
     * to the following criteria:
     * <ul>
     * <li>factory tag values = <code>{@link #setTags()}</code>.</li>
     * <li>factory attribute pairs = <code>{@link #mapAttrs()}</code>.</li>
     * <li>factory timestamps &rarr; <code>{@link #specTms()}</code> = <code>{@link FrameTimestampsSpec#newFactory()}</code>.</li>
     * <li>factory columns &rarr; <code>{@link #setColsSpecs()}</code> = <code>Collection({@link FrameColumnsSpec#newFactory()})</code>.</li>
     * </p>
     * 
     * @return  a new <code>IFrameFactory</code> implementation configured according to record specifications
     */
    public IFrameFactory    newFactory() {
        IFrameTimestampsFactory             facTms = this.specTms.newFactory();
        List<IFrameColumnsFactory<Object>>  lstFacCols = this.setColsSpecs.stream().map(spec -> spec.newFactory()).toList();
        
        IngestionFrameFactory   facFrames = IngestionFrameFactory.from(this.setTags, this.mapAttrs, facTms, lstFacCols);
                
        return facFrames;
    }
    
    /**
     * <p>
     * Prints out a text description of the record fields to the given output stream.
     * </p>
     * <p>
     * A line-by-line text description of each record field is written to the given output.
     * The <code>strPad</code> is used to supply an optional whitespace character padding to the
     * left-hand side header for each line description.
     * </p>
     *   
     * @param ps        output stream to receive text description of record fields
     * @param strPad    white-space padding for each line header (or <code>null</code>)
     */
    public void printOut(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        String strPadd = strPad + "  ";
        
        ps.println(strPad + "Frame tag values           : " + this.setTags);
        ps.println(strPad + "Frame attributes           : " + this.mapAttrs);
        ps.println(strPad + "Frame column factory count : " + this.setColsSpecs.size());
        ps.println(strPad + "Frame Timestamps  ");
        this.specTms.printOut(ps, strPadd);
        int indColFac = 1;
        for (FrameColumnsSpec<Record> specCols : this.setColsSpecs) {
            ps.println("Column Factory #" + indColFac);
            specCols.printOut(ps, strPadd);
            indColFac++;
        }
    }
    
    
    //
    // Record Overrides
    //
    
    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof FrameFactorySpec spec) {
            boolean bolResult = this.setTags.equals(spec.setTags)
                              && this.mapAttrs.equals(spec.mapAttrs)
                              && this.specTms.equals(spec.specTms)
                              && this.setColsSpecs.equals(spec.setColsSpecs);
            
            return bolResult;
        }
        
        return false;
    }

    /**
     * @see java.lang.Record#toString()
     */
    @Override
    public String toString() {
        StringBuilder   buf = new StringBuilder();
        
        buf.append("Frame tag values   : " + this.setTags + "\n");
        buf.append("Frame attributes   : " + this.mapAttrs + "\n");
        buf.append("Frame Timestamps Specification \n");
        buf.append(this.specTms);
        buf.append("Frame Columns Specification Collection \n");
        this.setColsSpecs.forEach(spec -> buf.append(spec));
        
        return buf.toString();
    }

    
    //
    // JAL Library Resources
    //

    /** JAL Tools default configuration parameters for ingestion frame factories */
    private static final JalToolsFramesConfig           CFG_FRM_DEF = JalToolsConfig.getInstance().datagen.frame;
        
    
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
    public static final String  STR_USERNAME = "USER";
    
    
    /** Enable/disable default tag values flag default configuration */
    public static final boolean                 BOL_TAGS_DEF_ENBL = CFG_FRM_DEF.tags.useDefault;
    
    /** Enable/disable default attribute pairs default configuration */
    public static final boolean                 BOL_ATTRS_DEF_ENBL = CFG_FRM_DEF.attributes.useDefault;
    
    /** Enable/disable class tag values flag default configuration */
    public static final boolean                 BOL_TAGS_CLS_ENBL = CFG_FRM_DEF.tags.useClass;
    
    /** Enable/disable class attribute pairs flag default configuration */
    public static final boolean                 BOL_ATTRS_CLS_ENBL = CFG_FRM_DEF.attributes.useClass;
    

    //
    // Record Resources
    //
    
    
    /** Default ingestion frame tag values */
    private static final Set<String>            SET_TAGS_FRM_DEF = new TreeSet<>( CFG_FRM_DEF.tags.values );
    
    /** Default ingestion frame attribute pairs */
    private static final Map<String, String>    MAP_ATTRS_FRM_DEF = new HashMap<>( CFG_FRM_DEF.attributes.pairs );

    
    /** Class tag values for ingestion frames */
    private static final Set<String>            SET_TAGS_FRM_CLS = new TreeSet<>();
    
    /** Class attribute pairs for ingestion frame */
    private static final Map<String, String>    MAP_ATTRS_FRM_CLS = new HashMap<>();
    
    
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
    
}
