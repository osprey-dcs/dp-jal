/*
 * Project: dp-jal
 * File:	FrameColumnsSpec.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.specs
 * Type: 	FrameColumnsSpec
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
 * @since Dec 20, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import java.io.PrintStream;
import java.lang.reflect.MalformedParametersException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.ingest.IngestionFrame;

import com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameColumnsFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameColumnsFactory;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.cols.JalToolsColumnsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.frames.JalToolsFramesConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Record specification for configuring ingestion frame data column factory instances.
 * </p>
 * <p>
 * The record represents a specification for ingestion frame data columns factories exposing the 
 * <code>{@link IFrameColumnsFactory}</code> interface.  
 * Implementations of the <code>{@link IFrameColumnsFactory}</code> interface
 * are typically used in ingestion frame factories for creating ingestion frames of simulated data.
 * The specifications for the column configurations and simulated data are provided here;
 * that is, the record fields are configuration parameters for creating column factories.
 * The column factories are then, in turn, used to create <code>{@link IngestionFrame}</code>
 * instances with ingestion frame factories, for example, <code>{@link IngestionFrameFactory}</code> objects.
 * </p>
 * <p>
 * <h2>Data Column Factories</h2>
 * The <code>FrameColumnsSpec</code> record contains specifications for <em>data column factory</em>, specifically,
 * implementation exposing the <code>{@link IFrameColumnsFactory}</code> interface.  
 * Once created, a <code>FrameColumnsSpec</code> object contains all the (immutable) configuration parameters
 * for a data column factory.  Data columns factories are then instantiated with the method
 * <code>{@link #newFactory()}</code>.
 * </p>  
 * <p> 
 * Note that the <code>{@link IFrameColumnsFactory#build(int)}</code> operation creates multiple data columns.
 * Each data column is the same size and contains simulated data of the same type.  Thus, instances of 
 * <code>FrameColumnsSpec</code> specify a fixed number of data columns with a given data type.
 * </p>
 * <p>
 * Note also that a <code>FrameColumnsSpec</code> record is a specification for a particular data column factory 
 * configuration.
 * A <code>FrameColumnsSpec</code> instance produces data columns factories with the <code>{@link #newFactory()}</code>
 * method.  This method can be invoked multiple times to create multiple factories, all of the same configuration.
 * </p>
 * <p>
 * <h2>Column Data</h2> 
 * Data column factories exposing the <code>{@link IFrameColumnsFactory}</code> interface produce <em>arbitrary data</em>,
 * that is, the simulated data within a column can be of any supported type of the Data Platform.
 * The Data Platform supported data types are enumerated in <code>{@link DpSupportedType}</code> and further
 * broken down into complex data and scalar data by the enumerations <code>{@link JalComplexType}</code> and
 * <code>{@link JalScalarType}</code>, respectively.  
 * Data produced by any given data column factory is all of the same type, that is, all data columns are of the same type.
 * </p>
 * <p>
 * The data type of the column data is determined by the record template parameter <code>{@link FactorySpec}</code>,
 * which specified the Java class type of the <em>Datum Factory</em> specification in the record field
 * <code>{@link #specFactory()}</code>.  The <code>{@link #specFactory()}</code> field is used to create the
 * datum factory which, then in turn, is used by the column factory for simulated column data creation.
 * </p>
 * <p>
 * The the field <code>{@link #enmType()}</code> also identifies the data column type; it must be
 * consistent with the datum factory configuration specified in field <code>{@link #specFactory()}</code>.  
 * Population of field <code>{@link #enmType}</code> is generally done internally by record creators.
 * However, the method <code>{@link #isValid()}</code> can be used to check the consistency of these two fields.
 * The inclusion of both the template parameter <code>FactorySpec</code> and the datum type field <code>{@link #enmType()}</code>
 * is necessary for the parsing operation <code>{@link #parse(String...)}</code> available for parsing application
 * command-line arguments.  
 * </p>
 * <p>
 * <h2>Heterogeneous Data</h2> 
 * As described above, data column factories (i.e., exposing the <code>{@link IFrameColumnsFactory}</code> interface)
 * produce arbitrary data types, that is, the simulated data within a column can be of any supported type.  
 * However, the data produced by any given data column factory is all of the same type.
 * </p>
 * <p>
 * Ingestion frame factories can contain multiple data column factories, see for example 
 * <code>{@link IngestionFrameFactory#addDataColumns(Collection)}</code>.  Thus, ingestion frame factories
 * can be configured to produce <code>IngestionFrame</code> instances with heterogeneous data, columns with
 * different data types.  To achieve this condition multiple <code>FrameColumnsSpec</code> instances are used
 * each with different values for <code>{@link #enmColType()}</code> and <code>{@link #recFacSpec}</code>.
 * </p>  
 * <p>
 * <h2>Datum Factories</h2>
 * Datum factories are classes exposing the <code>{@link IDatumFactory}</code> interface.  
 * The template parameter <code>{@link FactorySpec}</code> identifies the type and configuration for the
 * datum factory producing the simulated column data.  Again, the type parameters must be consistent with the
 * <code>{@link #enmType()}</code> field.  The inclusion of both the type parameter and the datum type field
 * is necessary for the parsing operation <code>{@link #parse(String...)}</code> available for parsing application
 * command-line arguments.
 * </p>  
 * <p>
 * The <code>FrameColumnsSpec</code> record supports multiple datum factory types.  Specifically, the template
 * parameter <code>FactorySpec</code> can be any of the datum factories specifications listed below.
 * There are multiple datum factory specifications available in JAL Tools, one for each supported datum type.
 * <ul>
 * <li><code>{@link ScalarFactorySpec}</code> - specifies a scalar datum factory configuration.</li>
 * <li><code>{@link TimestampFactorySpec}</code> - specifies a timestamp datum factory configuration. </li>
 * <li><code>{@link ByteArrayFactorySpec}</code> - specifies a byte array factory configuration.</li>
 * <li><code>{@link ImageFactorySpec}</code> - specifies an image factory configuration.</li>
 * <li><code>{@link TensorFactorySpec}</code> - specifies a tensor (multi-dimensional array) factory configuration.</li>
 * <li><code>{@link StructureFactorySpec}</code> - specifies a tree-structure factory configuration.</li>
 * </ul>
 * Each datum factory specification record contains creators, a command-line parser, and a method for creating 
 * its particular datum factory type.
 * </p>
 * <p>
 * When parsing command-line arguments the datum factory specification is identified by 
 * <code>{@link JalComplexType}</code> enumeration constant.
 * The supported datum factories associations are given by the following: 
 * <ul>
 * <li><code>{@link JalComplexType#SCALAR} &rarr; {@link ScalarFactorySpec}</code>.</li>
 * <li><code>{@link JalComplexType#BYTES} &rarr; {@link ByteArrayFactorySpec}</code>.</li>
 * <li><code>{@link JalComplexType#TIMESTAMP} &rarr; {@link TimestampFactorySpec}</code>.</li>
 * <li><code>{@link JalComplexType#IMAGE} &rarr; {@link ImageFactorySpec}</code>.</li>
 * <li><code>{@link JalComplexType#TENSOR} &rarr; {@link TensorFactorySpec}</code>.</li>
 * <li><code>{@link JalComplexType#STRUCTURE} &rarr; {@link StructureFactorySpec}.</code></li>
 * </ul>
 * </p>
 * <p>
 * <h2>Creators</h2>
 * All <code>FrameColumnsSpec</code> instances should be instantiated with the available creators, which 
 * generally have the name <code>from()</code>.  
 * There are multiple <code>from()</code> creators available, each with different method signatures. 
 * Missing record field values are supplied by default parameter values taken from the JAL Tools default configuration.
 * There is also a parsing creator <code>{@link #parse(String...)}</code> which
 * create <code>FrameColumnsSpec</code> instances from application command-line arguments.  The format of
 * the command line is provided in the method documentation.
 * </p>
 * <p>
 * Note that the fields <code>{@link #enmType()}</code> and <code>{@link #clsFactory()}</code> are populated 
 * internally by all the creators.  There values are taken from the available arguments, either provided directly
 * or inferred.  Thus, use of the canonical constructor 
 * <code>{@link #FrameColumnsSpec(int, String, Record, JalComplexType, Class)}</code> is discouraged.
 * However, the method <code>{@link #isValid()}</code> is available to check the consistency of the record fields
 * when this constructor is used.
 * </p>
 * <p>
 * The creator <code>{@link #parse(String...)}</code> is available for the parsing of application command-line arguments.
 * It relies heavily on the like-named operations available in all of the datum factories described above
 * (i.e., of the form <code>{@link #specFactory}.parse(String...)</code>).
 * </p>
 * <p>
 * There is the special creator <code>{@link #defaultFrame()}</code> which creates a new <code>DataColumnsSpecs</code>
 * list according to the JAL Tools default configuration for simulated ingestion frames.
 * </p>
 * <p>
 * <h2>Default Parameters</h2>
 * As discussed above there are a variety of specification record creators that supply default value for
 * record fields.  All default values are taken from the JAL Tools default configuration accessible via the
 * method <code>{@link JalToolsConfig#getInstance()}</code>.  The values specific to this columns specification record
 * are in the class constant <code>{@link #CFG_COL_DEF}</code> = <code>JalToolsConfig.getInstance().datagen.column</code>.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Dec 28, 2025
 *
 * @param <FactorySpec> record type of the datum factory specification for column data
 * 
 * @param intCols       number of data columns produced by factory
 * @param strNmPref     prefix for data column names produced by factory
 * @param specFactory   the datum factory specification used for simulated data production
 * @param enmType       data type of data columns produced by column factory
 * @param clsFactory    the class type of the datum factory specification record
 */
public record FrameColumnsSpec<FactorySpec extends Record>(
        Set<String>         setColNms,
        FactorySpec         specFactory,
        JalComplexType      enmType,
        Class<?>            clsFactory
        ) implements Comparable<FrameColumnsSpec<FactorySpec>> 
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Convenience Creator: 
     * Creates a new <code>FrameColumnsSpec</code> instance populated with all default argument values.
     * </p>
     * <p>
     * All record fields are populated with default parameter values taken from the JAL Tools default configuration.
     * This creator defers to creator <code>{@link #from(int)}</code> where the <code>int</code> argument is taken
     * from the JAL Tools default configuration.
     * Specifically, we have the following:
     * </p>
     * <ul>
     * <li><code>{@link #intCols()} &rarr; {@link #INT_COL_CNT_DEF}</code>.</li>
     * </ul>
     * </p>
     * <p>
     * <h2>Default Parameters</h2>
     * <b>WARNING:</b>. This method uses default parameters from the JAL Tools default configuration.  
     * The default parameters are used in the creation of the datum factory specification.  
     * Here the datum factory specification is defined completely by the default column factory parameters.
     * Note that all exceptions result from the creation of the default datum factory specification.
     * </p>
     *  
     * @return  a new <code>FrameColumnsSpec</code> specification as defined by the available argument values
     * 
     * @throws NumberFormatException        invalid number format (e.g., seed value for scalar factory specification)
     * @throws ConfigurationException       tensor shape was invalid
     * @throws TypeNotPresentException      an enumeration constant was not recognized
     * @throws UnsupportedOperationException unable to create 'numIncr' field in scalar factory specification
     * @throws IllegalArgumentException     the 'enmType' constant was not supported
     * 
     * @see #from(int)
     * @see #INT_COL_CNT_DEF
     */
    public static FrameColumnsSpec<Record>  from() throws NumberFormatException, IllegalArgumentException, ConfigurationException, TypeNotPresentException, UnsupportedOperationException {
        return FrameColumnsSpec.from(INT_COL_CNT_DEF);
    }
    
    /**
     * <p>
     * Convenience Creator:  
     * Creates a new <code>FrameColumnsSpec</code> instance populated from the available argument values.
     * </p>
     * <p>
     * All record fields are populated, either directory or inferred, with the argument values.
     * This creator defers to creator <code>{@link #from(int, String)}}</code> where the <code>String</code>
     * argument is taken from the JAL Tools default configuration.
     * Specifically, we have the following:
     * <ul>
     * <li><code>{@link #intCols()} &rarr; argument</code>.</li>
     * <li><code>{@link #strNmPref()} &rarr; {@link #STR_NM_PREF_DEF}</code>. </li>
     * </ul>
     * </p>
     * <p>
     * <h2>Default Parameters</h2>
     * <b>WARNING:</b>. This method uses default parameters from the JAL Tools default configuration.  
     * The datum factory specification for the data column simulated data generated is created according
     * to the default data column factory specification in the JAL Tools configuration.    
     * Note that all exceptions result from the creation of the default datum factory specification.
     * </p>
     *  
     * @param intCols       number of data columns produced by factory
     * 
     * @return  a new <code>FrameColumnsSpec</code> specification as defined by the available argument values
     * 
     * @throws NumberFormatException        invalid number format (e.g., seed value for scalar factory specification)
     * @throws ConfigurationException       tensor shape was invalid
     * @throws TypeNotPresentException      an enumeration constant was not recognized
     * @throws UnsupportedOperationException unable to create 'numIncr' field in scalar factory specification
     * @throws IllegalArgumentException     the 'enmType' constant was not supported
     * 
     * @see #from(int, String)
     * @see #STR_NM_PREF_DEF
     */
    public static FrameColumnsSpec<Record>  from(int intCols) throws NumberFormatException, IllegalArgumentException, ConfigurationException, TypeNotPresentException, UnsupportedOperationException {
        return FrameColumnsSpec.from(intCols, STR_NM_PREF_DEF);
    }
    
    /**
     * <p>
     * Convenience Creator: 
     * Creates a new <code>FrameColumnsSpec</code> instance populated from the available argument values.
     * </p>
     * <p>
     * All record fields are populated, either directory or inferred, with the argument values.
     * The column names are generated according to <code>{@link #from(int, String, Record)}</code>
     * where the <code>int</code> and <code>String</code> arguments are taken from the arguments. 
     * Specifically, we have the following:
     * <ul>
     * <li><code>intCols</code> &rarr; <code>argument</code>.</li>
     * <li><code>strNmPref</code> &rarr; <code>argument</code>. </li>
     * <li><code>{@link #specFactory()} &rarr; {@link #parseFactorySpec(JalComplexType, String...)}</code>:
     *   <ul>
     *   <li><code>JalComplexType &rarr; {@link #ENM_COL_TYPE_DEF}</code>.</li>
     *   <li><code>String... &rarr; {@link #ARR_FAC_SPEC_DEF}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * The above values are used as arguments for creator <code>{@link #from(int, String, Record)}</code>, to
     * which this creator defers.
     * </p>
     * <p>
     * The remaining record fields are populated as follows:
     * <ul>
     * <li><code>{@link #enmType()} &rarr; {@link #inferColumnType(Record)}</code>.</li>
     * <li><code>{@link #clsFactory()} &rarr; {@link Record#getClass()}</code>. </li>
     * </ul>
     * as indicated in <code>{@link #from(int, String, Record)}</code> and equivalent canonical creator
     * <code>{@link #from(Set, Record)}</code>.
     * </p>
     * <p>
     * <h2>Default Parameters</h2>
     * <b>WARNING:</b>. This method uses default parameters from the JAL Tools default configuration.  
     * Here the datum factory specification is defined by the default factory configuration 
     * as specified by the JAL Tools default column factory parameters.
     * The datum factory specification for the data column simulated data generated is created according
     * to the default data column factory specification in the JAL Tools configuration.    
     * Note that all exceptions result from the creation of the default datum factory specification.
     * </p>
     *  
     * @param intCols       number of data columns produced by factory
     * @param strNmPref     prefix for data column names produced by factory
     * 
     * @return  a new <code>FrameColumnsSpec</code> specification as defined by the available argument values
     * 
     * @throws TypeNotPresentException      an enumeration constant was not recognized
     * @throws NumberFormatException        invalid number format (e.g., seed value for scalar factory specification)
     * @throws UnsupportedOperationException unable to create 'numIncr' field in scalar factory specification
     * @throws MissingResourceException     timestamp factory had empty arguments
     * @throws DateTimeParseException       invalid format for ISO-8601 time and/or duration specification 
     * @throws ConfigurationException       tensor shape was invalid
     * @throws NoSuchElementException       the 'enmType' constant was not supported
     * 
     * @see #from(int, String, Record)
     * @see #parseFactorySpec(JalComplexType, String...)
     * @see #ENM_COL_TYPE_DEF
     * @see #ARR_FAC_SPEC_DEF
     */
    public static FrameColumnsSpec<Record>  from(int intCols, String strNmPref) throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException, MissingResourceException, DateTimeParseException, ConfigurationException, NoSuchElementException {
        Record  specFactory = FrameColumnsSpec.parseFactorySpec(ENM_COL_TYPE_DEF, ARR_FAC_SPEC_DEF);    // throws all exceptions
        
        return FrameColumnsSpec.from(intCols, strNmPref, specFactory);
    }
    
    /**
     * <p>
     * Convenience Creator: 
     * Creates a new <code>FrameColumnsSpec</code> instance populated from the available argument values.
     * </p>
     * <p>
     * All record fields are populated, either directory or inferred, with the argument values.
     * The column names are generated according to <code>{@link #from(int, String, Record)}</code>
     * where the <code>String</code> argument is taken from the JAL Tools default configuration, see below.
     * Specifically, we have the following:
     * <ul>
     * <li><code>intCols</code> &rarr; <code>{@link #INT_COL_CNT_DEF}</code>.</li>
     * <li><code>strNmPref</code> &rarr; <code>{@link #STR_NM_PREF_DEF}</code>. </li>
     * <li><code>{@link #specFactory()} &rarr; argument</code>. </li>
     * </ul>
     * Record fields not provided are populated as follows:
     * <ul>
     * <li><code>{@link #enmType()} &rarr; {@link #inferColumnType(Record)}</code>.</li>
     * <li><code>{@link #clsFactory()} &rarr; {@link Record#getClass()}</code>. </li>
     * </ul>
     * </p>
     * <p>
     * <h2>Default Parameters</h2>
     * <b>WARNING:</b>. This method uses default parameters from the JAL Tools default configuration.  
     * and infers the values of some record fields.  
     * </p>
     *  
     * @param <FactorySpec> record type of the datum factory specification for column data
     * 
     * @param intCols       number of data columns produced by factory
     * @param strNmPref     prefix for data column names produced by factory
     * @param specFactory   the datum factory specification used for simulated data production
     * 
     * @return  a new <code>FrameColumnsSpec</code> specification as defined by the available argument values
     * 
     * @see #from(int, Record)
     * @see #INT_COL_CNT_DEF
     */
    public static <FactorySpec extends Record> FrameColumnsSpec<FactorySpec>  from(FactorySpec specFactory) {
        
        return FrameColumnsSpec.from(INT_COL_CNT_DEF, specFactory);
    }
    
    /**
     * <p>
     * Convenience Creator: 
     * Creates a new <code>FrameColumnsSpec</code> instance populated from the available argument values.
     * </p>
     * <p>
     * All record fields are populated, either directory or inferred, with the argument values.
     * The column names are generated according to <code>{@link #from(int, String, Record)}</code>
     * where the <code>String</code> argument is taken from the JAL Tools default configuration, see below.
     * Specifically, we have the following:
     * <ul>
     * <li><code>intCols</code> &rarr; argument.</li>
     * <li><code>strNmPref</code> &rarr; </code>{@link #STR_NM_PREF_DEF}</code>. </li>
     * <li><code>{@link #specFactory()}</code> &rarr; argument. </li>
     * </ul>
     * Record fields not provided are populated as follows:
     * <ul>
     * <li><code>{@link #enmType()} &rarr; {@link #inferColumnType(Record)}</code>.</li>
     * <li><code>{@link #clsFactory()} &rarr; {@link Record#getClass()}</code>. </li>
     * </ul>     
     * </p>
     * <p>
     * <h2>Default Parameters</h2>
     * <b>WARNING:</b>. This method uses default parameters from the JAL Tools default configuration 
     * and infers the values of some record fields.  
     * </p>
     *  
     * @param <FactorySpec> record type of the datum factory specification for column data
     * 
     * @param intCols       number of data columns produced by factory
     * @param specFactory   the datum factory specification used for simulated data production
     * 
     * @return  a new <code>FrameColumnsSpec</code> specification as defined by the available argument values
     * 
     * @see #from(int, String, Record)
     * @see #STR_NM_PREF_DEF
     */
    public static <FactorySpec extends Record> FrameColumnsSpec<FactorySpec>  from(int intCols, FactorySpec specFactory) {
        
        return FrameColumnsSpec.from(intCols, STR_NM_PREF_DEF, specFactory);
    }
    
    /**
     * <p>
     * Convenience Creator: 
     * Creates a new <code>FrameColumnSpec</code> instance populated from the given argument values.
     * </p>
     * <p>
     * This creator defers to standard creator <code>{@link #from(Set, Record)}</code> where the
     * <code>Set</code> argument is created with the given <code>int</code> and <code>String</code> arguments.
     * The <code>{@link #setColNms()}</code> field is populated with names created from argument <code>intCols</code>
     * and <code>strNmPref</code> according to the following:
     * <pre>
     * <code>
     *   {@link #setColNms()} = {strNmPref+0, strNmPref+1, ..., strNmPref+{@link Integer#toString(int)}}
     * </code>
     * </pre>
     * where the argument to <code>{@link Integer#toString(int)}</code> is <code>intCols</code> - 1.
     * </p>
     * <p>
     * Once the column names are created the method defers to <code>{@link #from(Set, Record)}</code>.
     * Thus, all record fields are populated, either directly or inferred, with the argument values.  
     * Specifically, we have the following:
     * <ul>
     * <li><code>{@link #setColNms()}</code> &rarr; argument according to above. </li>
     * <li><code>{@link #specFactory()}</code> &rarr; argument. </li>
     * <li><code>{@link #enmType()}</code> &rarr; <code>{@link #inferColumnType(Record)}</code>.</li>
     * <li><code>{@link #clsFactory()}</code> &rarr; <code>{@link Record#getClass()}</code>. </li>
     * </ul>
     * </p>
     *  
     * @param <FactorySpec> record type of the datum factory specification for column data
     * 
     * @param intCols       number of data columns produced by factory
     * @param strNmPref     prefix for data column names produced by factory
     * @param specFactory   the datum factory specification used for simulated data production
     * 
     * @return  a new <code>FrameColumnsSpec</code> specification as defined by the available argument values
     * 
     * @see #from(Set, Record)
     */
    public static <FactorySpec extends Record>  FrameColumnsSpec<FactorySpec>    from(int intCols, String strNmPref, FactorySpec specFactory) {
        
        // Create the column names
        Set<String> setColNms = IntStream.range(0, intCols).<String>mapToObj(i -> strNmPref + Integer.toString(i)).collect(Collectors.toSet());
                
        return FrameColumnsSpec.from(setColNms, specFactory);
    }
    
    /**
     * <p>
     * Convenience Creator: 
     * Creates a new <code>FrameColumnsSpec</code> instance populated from the available argument values.
     * </p>
     * <p>
     * All record fields are populated, either directory or inferred, with the argument values.
     * This creator defers to standard creator <code>{@link #from(Set, Record)}</code> where the <code>Record</code>
     * argument is generated from the JAL Tools default configuration.
     * Specifically, we have the following:
     * <ul>
     * <li><code>{@link setColNms()}</code> &rarr; <code>argument</code>.</li>
     * <li><code>{@link #specFactory()} &rarr; {@link #parseFactorySpec(JalComplexType, String...)}</code>:
     *   <ul>
     *   <li><code>JalComplexType &rarr; {@link #ENM_COL_TYPE_DEF}</code>.</li>
     *   <li><code>String... &rarr; {@link #ARR_FAC_SPEC_DEF}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * <p>
     * The remaining record fields are populated as follows:
     * <ul>
     * <li><code>{@link #enmType()} &rarr; {@link #inferColumnType(Record)}</code>.</li>
     * <li><code>{@link #clsFactory()} &rarr; {@link Record#getClass()}</code>. </li>
     * </ul>
     * as indicated in equivalent canonical creator <code>{@link #from(Set, Record)}</code>.
     * </p>
     * <p>
     * <h2>Default Parameters</h2>
     * <b>WARNING:</b>. This method uses default parameters from the JAL Tools default configuration.  
     * Here the datum factory specification is defined by the default factory configuration 
     * as specified by the JAL Tools default column factory parameters.
     * The datum factory specification for the data column simulated data generated is created according
     * to the default data column factory specification in the JAL Tools configuration.    
     * Note that all exceptions result from the creation of the default datum factory specification.
     * </p>
     *  
     * @param setColNms     the set of unique column names produced by generated column factory
     * 
     * @return  a new <code>FrameColumnsSpec</code> specification as defined by the available argument values
     * 
     * @throws TypeNotPresentException      an enumeration constant was not recognized
     * @throws NumberFormatException        invalid number format (e.g., seed value for scalar factory specification)
     * @throws UnsupportedOperationException unable to create 'numIncr' field in scalar factory specification
     * @throws MissingResourceException     timestamp factory had empty arguments
     * @throws DateTimeParseException       invalid format for ISO-8601 time and/or duration specification 
     * @throws ConfigurationException       tensor shape was invalid
     * @throws NoSuchElementException       the 'enmType' constant was not supported
     * 
     * @see #from(Set, Record)
     * @see #parseFactorySpec(JalComplexType, String...)
     * @see #ENM_COL_TYPE_DEF
     * @see #ARR_FAC_SPEC_DEF
     */
    public static FrameColumnsSpec<Record>   from(Set<String> setColNms) throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException, MissingResourceException, DateTimeParseException, ConfigurationException, NoSuchElementException {
        Record  specFactory = FrameColumnsSpec.parseFactorySpec(ENM_COL_TYPE_DEF, ARR_FAC_SPEC_DEF);    // throws all exceptions
      
        return FrameColumnsSpec.from(setColNms, specFactory);
    };
    
    /**
     * <p>
     * Standard Creator:
     * This creator is essentially equivalent to the canonical constructor <code>{@link #FrameColumnsSpec(Set, Record, JalComplexType, Class)}</code>.
     * </p>
     * <p>
     * The record fields <code>{@link #setColNms()}</code> and <code>{@link #specFactory()}</code> are populated directly 
     * with the argument values.  The remaining field values are inferred from the given arguments according to the 
     * following:
     * <ul>
     * <li><code>{@link #setColNms()} &rarr; argument</code>.</li>
     * <li><code>{@link #specFactory()} &rarr; argument</code>. </li>
     * <li><code>{@link #enmType()} &rarr; {@link #inferColumnType(Record)}</code>.</li>
     * <li><code>{@link #clsFactory()} &rarr; {@link Record#getClass()}</code>. </li>
     * </ul>
     * </p>
     * 
     * @param <FactorySpec> record type of the datum factory specification for column data
     * 
     * @param setColNms     the set of unique column names produced by generated column factory
     * @param specFactory   the datum factory specification used for simulated data production
     * 
     * @return  a new <code>FrameColumnsSpec</code> specification as defined by the available argument values
     */
    public static <FactorySpec extends Record>  FrameColumnsSpec<FactorySpec>    from(Set<String> setColNms, FactorySpec specFactory) {
        JalComplexType  enmType = FrameColumnsSpec.inferColumnType(specFactory);
        Class<?>        clsFacSpec = specFactory.getClass();
        
        return new FrameColumnsSpec<FactorySpec>(setColNms, specFactory, enmType, clsFacSpec);
    }
    
    /**
     * <p>
     * Convenience Creator: 
     * Creates a new <code>FrameColumnsSpec</code> instance using the available argument values and the
     * default creator for the data factory specification inferred by the given datum factory type.
     * </p>
     * <p>
     * This is a convenience creator which defers to the default creator for the given 
     * <code>{@link JalComplexType}</code> constant provided.  Specifically, the field <code>{@link #specFactory()}</code>
     * is populated according to the following assignments:
     * <ul>
     * <li><code>{@link JalComplexType#SCALAR} &rarr; {@link ScalarFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#BYTES} &rarr; {@link ByteArrayFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#TIMESTAMP} &rarr; {@link TimestampFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#IMAGE} &rarr; {@link ImageFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#TENSOR} &rarr; {@link TensorFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#STRUCTURE} &rarr; {@link StructureFactorySpec#from()}.</code></li>
     * </ul>
     * </p>
     * <p>
     * <h2>Default Parameters</h2>
     * <b>WARNING:</b>. This method uses default parameters from the JAL Tools default configuration.  
     * The default parameters are used in the creation of the datum factory specification.  
     * Here the datum factory specification is completely configured to the JAL Tools default state 
     * as specified above.
     * The remaining record fields are populated as follows:
     * <ul>
     * <li><code>{@link #intCols()} &rarr; {@link #INT_COL_CNT_DEF}</code>.</li>
     * <li><code>{@link #strNmPref()} &rarr; {@link #STR_NM_PREF_DEF}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param enmType       data type of data columns produced by column factory
     * 
     * @return  a new <code>FrameColumnsSpec</code> specification as defined by the available argument values
     * 
     * @throws UnsupportedOperationException    the <code>JalComplexType</code> constant is unsupported
     *
     * @see #from(int, JalComplexType)
     * @see #INT_COL_CNT_DEF
     * @see JalComplexType
     * @see ScalarFactorySpec#from()
     * @see TimestampFactorySpec#from()
     * @see ByteArrayFactorySpec#from()
     * @see ImageFactorySpec#from()
     * @see TensorFactorySpec#from()
     * @see StructureFactorySpec#from()
     */
    public static FrameColumnsSpec<Record>    from(JalComplexType enmType) throws UnsupportedOperationException {

        return FrameColumnsSpec.from(INT_COL_CNT_DEF, enmType);
    }
    
    /**
     * <p>
     * Convenience Creator: 
     * Creates a new <code>FrameColumnsSpec</code> instance using the available argument values and the
     * default creator for the data factory specification inferred by the given datum factory type.
     * </p>
     * <p>
     * This is a convenience creator which defers to the default creator for the given 
     * <code>{@link JalComplexType}</code> constant provided.  Specifically, the field <code>{@link #specFactory()}</code>
     * is populated according to the following assignments:
     * <ul>
     * <li><code>{@link JalComplexType#SCALAR} &rarr; {@link ScalarFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#BYTES} &rarr; {@link ByteArrayFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#TIMESTAMP} &rarr; {@link TimestampFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#IMAGE} &rarr; {@link ImageFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#TENSOR} &rarr; {@link TensorFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#STRUCTURE} &rarr; {@link StructureFactorySpec#from()}.</code></li>
     * </ul>
     * </p>
     * <p>
     * <h2>Default Parameters</h2>
     * <b>WARNING:</b>. This method uses default parameters from the JAL Tools default configuration.  
     * The default parameters are used in the creation of the datum factory specification.  
     * Here the datum factory specification is completely configured to the JAL Tools default state 
     * as specified above.
     * The remaining record fields are populated as follows:
     * <ul>
     * <li><code>{@link #strNmPref()} &rarr; {@link #STR_NM_PREF_DEF}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param intCols       number of data columns produced by factory
     * @param enmType       data type of data columns produced by column factory
     * 
     * @return  a new <code>FrameColumnsSpec</code> specification as defined by the available argument values
     * 
     * @throws UnsupportedOperationException    the <code>JalComplexType</code> constant is unsupported
     *
     * @see #from(int, String, JalComplexType)
     * @see #STR_NM_PREF_DEF
     * @see JalComplexType
     * @see ScalarFactorySpec#from()
     * @see TimestampFactorySpec#from()
     * @see ByteArrayFactorySpec#from()
     * @see ImageFactorySpec#from()
     * @see TensorFactorySpec#from()
     * @see StructureFactorySpec#from()
     */
    public static FrameColumnsSpec<Record>    from(int intCols, JalComplexType enmType) throws UnsupportedOperationException {

        return FrameColumnsSpec.from(intCols, STR_NM_PREF_DEF, enmType);
    }
    
    /**
     * <p>
     * Convenience Creator: 
     * Creates a new <code>FrameColumnsSpec</code> instance using the argument values and the
     * default creator for the data factory specification inferred by the given datum factory type.
     * </p>
     * <p>
     * This creator defers to convenience creator <code>{@link #from(Set, JalComplexType)}</code> where the
     * <code>Set</code> argument is created with the given <code>int</code> and <code>String</code> arguments.
     * The <code>{@link #setColNms()}</code> field is populated with names created from argument <code>intCols</code>
     * and <code>strNmPref</code> according to the following:
     * <pre>
     * <code>
     *   {@link #setColNms()} = {strNmPref+0, strNmPref+1, ..., strNmPref+{@link Integer#toString(int)}}
     * </code>
     * </pre>
     * where the argument to <code>{@link Integer#toString(int)}</code> is <code>intCols</code> - 1.
     * </p>
     * <p>
     * Once the column names are created the method defers to <code>{@link #from(Set, JalComplexType)}</code>.
     * Thus, all record fields are inferred from the remaining argument value(s).  
     * <p>
     * <h2>NOTES:</h2>
     * This is a convenience creator which defers to the default datum factory specification creator for the given 
     * <code>{@link JalComplexType}</code> constant provided.  Specifically, the field <code>{@link #specFactory()}</code>
     * is populated according to the following assignments:
     * <ul>
     * <li><code>{@link JalComplexType#SCALAR} &rarr; {@link ScalarFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#BYTES} &rarr; {@link ByteArrayFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#TIMESTAMP} &rarr; {@link TimestampFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#IMAGE} &rarr; {@link ImageFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#TENSOR} &rarr; {@link TensorFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#STRUCTURE} &rarr; {@link StructureFactorySpec#from()}.</code></li>
     * </ul>
     * </p>
     * <p>
     * <h2>Default Parameters</h2>
     * <b>WARNING:</b>. This method uses default parameters from the JAL Tools default configuration.  
     * The default parameters are used in the creation of the datum factory specification.  
     * Here the datum factory specification is completely configured to the JAL Tools default state 
     * as specified above.
     * </p>
     * 
     * @param intCols       number of data columns produced by factory
     * @param strNmPref     prefix for data column names produced by factory
     * @param enmType       data type of data columns produced by column factory
     * 
     * @return  a new <code>FrameColumnsSpec</code> specification as defined by the available argument values
     * 
     * @throws UnsupportedOperationException scalar factory unable to create 'numIncr' parameter   
     * @throws NoSuchElementException        the <code>JalComplexType</code> constant is unsupported
     *
     * @see #from(Set, JalComplexType)
     * @see JalComplexType
     * @see ScalarFactorySpec#from()
     * @see TimestampFactorySpec#from()
     * @see ByteArrayFactorySpec#from()
     * @see ImageFactorySpec#from()
     * @see TensorFactorySpec#from()
     * @see StructureFactorySpec#from()
     */
    public static FrameColumnsSpec<Record>    from(int intCols, String strNmPref, JalComplexType enmType) throws UnsupportedOperationException, NoSuchElementException {
        
        // Create the column names
        Set<String> setColNms = IntStream.range(0, intCols).<String>mapToObj(i -> strNmPref + Integer.toString(i)).collect(Collectors.toSet());

        return FrameColumnsSpec.from(setColNms, enmType);    // throws all exceptions
    }
    
    /**
     * <p>
     * Convenience Creator: 
     * Creates a new <code>FrameColumnsSpec</code> instance using the argument values and the
     * default creator for the data factory specification inferred by the given datum factory type.
     * </p>
     * <p>
     * This creator defers to standard creator <code>{@link #from(Set, Record)}</code> where the <code>Record</code>
     * argument is created according to the <code>JalComplexType</code> argument.
     * Specifically, we defer to the default datum factory specification creator for the given 
     * <code>{@link JalComplexType}</code> constant provided.  
     * Thus, the field <code>{@link #specFactory()}</code> is populated according to the following assignments:
     * <ul>
     * <li><code>{@link JalComplexType#SCALAR} &rarr; {@link ScalarFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#BYTES} &rarr; {@link ByteArrayFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#TIMESTAMP} &rarr; {@link TimestampFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#IMAGE} &rarr; {@link ImageFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#TENSOR} &rarr; {@link TensorFactorySpec#from()}</code>.</li>
     * <li><code>{@link JalComplexType#STRUCTURE} &rarr; {@link StructureFactorySpec#from()}.</code></li>
     * </ul>
     * </p>
     * <p>
     * <h2>Default Parameters</h2>
     * <b>WARNING:</b>. This method uses default parameters from the JAL Tools default configuration.  
     * The default parameters are used in the creation of the datum factory specification.  
     * Here the datum factory specification is completely configured to the JAL Tools default state 
     * as specified above.
     * </p>
     * 
     * @param setColNms     the set of unique column names produced by generated column factory
     * @param enmType       data type of data columns produced by column factory
     * 
     * @return  a new <code>FrameColumnsSpec</code> specification as defined by the available argument values
     * 
     * @throws UnsupportedOperationException scalar factory unable to create 'numIncr' parameter   
     * @throws NoSuchElementException        the <code>JalComplexType</code> constant is unsupported
     *
     * @see #from(Set, Record)
     * @see JalComplexType
     * @see ScalarFactorySpec#from()
     * @see TimestampFactorySpec#from()
     * @see ByteArrayFactorySpec#from()
     * @see ImageFactorySpec#from()
     * @see TensorFactorySpec#from()
     * @see StructureFactorySpec#from()
     */
    public static FrameColumnsSpec<Record>   from(Set<String> setColNms, JalComplexType enmType) throws UnsupportedOperationException, NoSuchElementException {
        
        return switch (enmType) {
        case SCALAR -> FrameColumnsSpec.from(setColNms, ScalarFactorySpec.from()); // throws UnsupportedOperationException
        case TIMESTAMP -> FrameColumnsSpec.from(setColNms, TimestampFactorySpec.from());
        case BYTES -> FrameColumnsSpec.from(setColNms, ByteArrayFactorySpec.from());
        case IMAGE -> FrameColumnsSpec.from(setColNms, ImageFactorySpec.from());
        case TENSOR -> FrameColumnsSpec.from(setColNms, TensorFactorySpec.from());
        case STRUCTURE -> FrameColumnsSpec.from(setColNms, StructureFactorySpec.from());
        default -> throw new NoSuchElementException("Unexpected value: " + enmType);
        };
    }
    
    /**
     * <p>
     * Parses and argument string to identify and create a data columns specification (i.e., <code>FrameColumnsSpec</code> record).
     * </p>
     * <p>
     * The argument is assumed to be part of an application command line.  For example, the command-line could
     * contain the variable "--cols" which delimits the arguments to this method.  
     * For example, consider the following application command-line 
     * <code>
     * <pre>
     * > java application --cols cnt prefix DTYPE [parameters] [...]
     * </pre>
     * </code>
     * where 
     * <ul>
     * <li>'cnt' is the number of data columns ,</li>
     * <li>'prefix' is the prefix given to each column name,</li> 
     * <li>'DTYPE' is a <code>JalComplexType</code> enumeration constant specifying column type ,</li>
     * <li>'parameters' are the set of configuration parameters for the datum factory <code>parse(String...)</code> operation,</li>
     * <li>... are any additional application command-line parameters.</li>
     * </ul>
     * There is another supported format where the column names are given explicitly as follows:
     * <code>
     * <pre>
     * > java application --cols PV1 PV2 PV3 ... PVn DTYPE [parameters] [...]
     * </pre>
     * </code>
     * where <code>{PV1, PV2, ..., PVn}</code> are the column names and 'cnt' = <code>n</code> in this case.
     * See below for more information on both formats.
     * </p>
     * <p>
     * <h2>Caveat</h2>
     * Clearly it is not necessary for the arguments to be obtained from an application command line as described.  
     * So long as the stated conditions and formats are followed the method will create and return an appropriate 
     * Data Type configuration record.
     * </p> 
     * <p>
     * <h2>Datum Factory Specification Records</h2>
     * Datum factory specification records types are used for the <code>FactorySpec</code> generic template type.  
     * There is one type for each data type supported for frame column data value creation. 
     * Currently there are the following:
     * <ol>
     * <li>Scalar - <code>{@link ScalarFactorySpec}</code>,</li>
     * <li>Timestamp - <code>{@link TimestampFactorySpec}</code>,</li>
     * <li>Bytes - <code>{@link ByteArrayFactorySpec}</code>,</li>
     * <li>Image - <code>{@link ImageFactorySpec}</code>.
     * <li>Array - <code>{@link TensorFactorySpec}</code>,</li>
     * <li>Structure - <code>{@link StructureFactorySpec}</code>,</li>
     * </ol> 
     * The given arguments within <code>'parameters'</code> contain the parameters necessary to configure the appropriate 
     * value generator (<code>IDataumFactory</code> implementation) for column data.
     * </p> 
     * <p>
     * <h2>Argument Formats</h2>
     * There are 2 supported argument formats: 1) the format in the example given above, and 2) a format where
     * the column names are given explicitly.
     * For case 1, with reference to the above example, the format of the argument collection is given by the following:
     * <code>
     * <pre>
     *      [cnt [prefix [DTYPE [datum factory parameter(s)]]]]
     * </pre>
     * </code>
     * where the brackets indicate optional arguments and, again, the parameter values are given as follows:
     * <ul>
     * <li>'cnt' is the number of data columns, </li>
     * <li>'prefix' is the prefix given to each column name,</li> 
     * <li>'DTYPE' is a <code>JalComplexType</code> enumeration constant specifying column type,</li>
     * <li>'datum factory parameter(s)' are the configuration parameters for the datum factory <code>parse(String...)</code> operation.</li>
     * </ul>
     * For case 2 the format is given as follows:
     * <code>
     * <pre>
     *      [colNm1 colNm2 ... colNmN [DTYPE [datum factory parameter(s)]]]
     * </pre>
     * </code>
     * where the brackets indicate optional parameters and the parameter values are given as follows:
     * <ul>
     * <li>'colNm1 ... colNmN' are the explicit names of the data columns (arbitrary number), </li>
     * <li>'DTYPE' is a <code>JalComplexType</code> enumeration constant specifying column type,</li>
     * <li>'datum factory parameter(s)' are the configuration parameters for the datum factory <code>parse(String...)</code> operation.</li>
     * </ul>
     * The parser determines which format is employed according to the format of the first argument.  If it is
     * an integer format then case 1 applies.  If it is <b>not</b> an integer format (i.e., a string name) then
     * case 2 applies. 
     * </p>
     * <p>
     * <h2>Optional Values</h2>
     * Whenever parameter values are missing in the argument collection the values are taken from the JAL Tools
     * default configuration.  Note that there is a hierarchy of options.  For example, to specifically indicate
     * a data type for the datum factory all parameters preceding the <code>DTYPE</code> value must be included.
     * In the first case this requires that the column count <code>cnt</code> and column name prefix <code>prefix</code>
     * is included.  In the second case it requires that at least one column name <code>colNm1</code> is included
     * (additional names may follow).
     * </p> 
     * <p>
     * <h2>Datum Factory Parameters</h2>
     * In both formats the datum factory parameters are formatted according to the parameter <code>DTYPE</code>
     * in the argument.  Specifically, they must conform to the datum factory specification record parsing
     * creator.
     * The generic parameter type of the <code>FrameColumnsSpec</code> record returned is given by the 
     * <code>DTYPE</code> value according to the following:
     * <ul>
     * <li><code>{@link JalComplexType#SCALAR}</code> - <code>{@link ScalarFactorySpec}</code>.</li>
     * <li><code>{@link JalComplexType#BYTES}</code> - <code>{@link ByteArrayFactorySpec}</code>.</li>
     * <li><code>{@link JalComplexType#TIMESTAMP}</code> - <code>{@link TimestampFactorySpec}</code>.</li>
     * <li><code>{@link JalComplexType#IMAGE}</code> - <code>{@link ImageFactorySpec}</code>.</li>
     * <li><code>{@link JalComplexType#TENSOR}</code> - <code>{@link TensorFactorySpec}</code>.</li>
     * <li><code>{@link JalComplexType#STRUCTURE}</code> - <code>{@link StructureFactorySpec}</code>.</li>
     * </ul>
     * Thus, the number of elements within the argument string array '<code>parameters</code>' is dependent upon the 
     * <code>JalComplexType</code> identified by <code>'DTYPE'</code>.  If the number of arguments is not appropriate 
     * for the given type an exception is thrown.
     * </p>
     * <p>
     * See the documentation for the parsing method <code>parse(String...)</code> for the associated datum factory
     * specification for details on the format of its '<code>parameters</code>' argument (of type <code>String[]</code>).
     * For example, when '<code>DTYPE</code>' == <code>{@link JalComplexType#SCALAR}</code> see
     * <code>{@link ScalarFactorySpec#parse(String...)}</code>.
     * </p>
     *  
     * @param args  argument string defining the configuration for returned record
     * 
     * @return  a new <code>FrameColumnsSpec</code> specification record as defined by the parsed argument values
     * 
     * @throws TypeNotPresentException  invalid enumeration constant (e.g., the 1st argument was not a <code>JalComplexType</code>)
     * @throws NumberFormatException    invalid numeric expression (typically for 'lngSeed' value)
     * @throws ConfigurationException   the argument contained the wrong number of arguments for the <code>JalComplexType</code>
     * @throws UnsupportedOperationException invalid field value format (typically 'numIncr' was invalid)
     * @throws MalformedParametersException  an enumeration constant within the argument set was not recognized (IMAGE)
     * @throws NoSuchElementException   the column data type was unrecognized (i.e., 'DTYPE' was not supported)
     */
    public static FrameColumnsSpec<Record> parse(String...args) throws TypeNotPresentException, NumberFormatException, ConfigurationException, UnsupportedOperationException, MalformedParametersException, NoSuchElementException {
        if (args.length < 1) 
            return FrameColumnsSpec.from();
        
        // The argument index counter
        int     indArg = 0;
        
        
        // Check if the first argument is an integer 
        String      strArgFirst = args[indArg++];
        Set<String> setColNms;
        try {
            
            // The first argument is the column count
            int cntCols = Integer.valueOf(strArgFirst);
            if (indArg >= args.length)
                return FrameColumnsSpec.from(cntCols);   // throws NumberFormatException, IllegalArgumentException, ConfigurationException, TypeNotPresentException, UnsupportedOperationException 
            
            // Get the column name prefix
            String  strNmPref = args[indArg++];
            if (indArg >= args.length)
                return FrameColumnsSpec.from(cntCols, strNmPref); // throws TypeNotPresentException, NumberForamtException, UnsupportedOperationException, MissingResourceException, DateTimeParseException, ConfigurationException, NoSuchElementException  
            
            setColNms = IntStream.range(0, cntCols).<String>mapToObj(i -> strNmPref + Integer.toString(i)).collect(Collectors.toSet());
            
        } catch (NumberFormatException e) {
            
            // The first argument is a column name - followed by more column names
            setColNms = new TreeSet<>();
            setColNms.add(strArgFirst);
            
            while (indArg < args.length) {
                String strArgNext = args[indArg];
                
                if (JalComplexType.isValue(strArgNext))
                    break;
                
                setColNms.add(strArgNext);
                indArg++;
            }
            
        }
        
//        // Get the column count 
//        int     cntCols = Integer.valueOf(args[0]);
//        if (args.length < 2)
//            return FrameColumnsSpec.from(cntCols);
//        
//        // Get the column name prefix
//        String  strNmPref = args[1];
//        if (args.length < 3)
//            return FrameColumnsSpec.from(cntCols, strNmPref); // throws UnsupportedOperationException
//
        
        // Check if there are any arguments left - must start with the columns type
        if (indArg >= args.length)
            return FrameColumnsSpec.from(setColNms); // throws TypeNotPresentException, NumberForamtException, UnsupportedOperationException, MissingResourceException, DateTimeParseException, ConfigurationException, NoSuchElementException
        
        // Get the Datum Type of the column values
        String          strType = args[indArg++];
        JalComplexType  enmType = JalComplexType.valueFrom(strType); // throws TypeNotPresentException
        
        // Parse the data factory parameters if provided
        String[]    arrFacCfg = (indArg >= args.length) ? new String[0] : Arrays.copyOfRange(args, indArg, args.length);
        
        Record      specFactory = switch (enmType) {
        case SCALAR -> ScalarFactorySpec.parse(arrFacCfg);    // throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException
        case BYTES -> ByteArrayFactorySpec.parse(arrFacCfg);   // throws NumberFormatException
        case IMAGE -> ImageFactorySpec.parse(arrFacCfg);       // throws NumberFormatException, TypeNotPresentException
        case TENSOR -> TensorFactorySpec.parse(arrFacCfg);      // throws IllegalArgumentException, ConfigurationException, NumberFormatException, TypeNotPresentException, UnsupportedOperationException 
        case STRUCTURE -> StructureFactorySpec.parse(arrFacCfg);  // throws ConfigurationException, NumberFormatException, TypeNotPresentException, UnsupportedOperationException
        default ->
            throw new NoSuchElementException(JavaRuntime.getQualifiedMethodNameSimple() + " - Data type not supported: " + enmType);
        };
        
        // Create and return the data columns configuration
        return FrameColumnsSpec.from(setColNms, specFactory);
    }
 
    /**
     * <p>
     * Retrieves and returns the default ingestion frame data columns specifications for the default 
     * ingestion frame factory configuration.
     * </p>
     * <p>
     * The JAL Tools default configuration contains a default ingestion frame configuration.  This configuration is
     * used by ingestion frame factories to create <code>{@link IngestionFrame}</code> instances when no explicit
     * configuration is given.
     * </p>
     * </p>
     * The returned (ordered) list of <code>FrameColumnsSpec</code> records specifies all the data columns in the
     * default ingestion frame.  The timestamps for an ingestion frame are specified separately in 
     * <code>{@link FrameTimestampsSpec}</code> specification. 
     * </p>
     * <p>
     * The method retrieves the default data column specifications contained in the <code>{@link JalToolsColumnsConfig}</code>
     * structure class list within the <code>{@link JalToolsConfig}</code> default configuration.  The parameters
     * for each column are parsed and a new <code>FrameColumnsSpec</code> record is created for each column.
     * The column configurations are returned in the order in which they appear in the JAL Tools default configuration.
     * </p>
     * 
     * @return  a list of new <code>FrameColumnsSpec</code> records as specified in the JAL Tools default configuration
     * 
     * @throws IllegalArgumentException general error (typically bad argument count or enumeration constant not recognized)
     * @throws NumberFormatException    a bad numeric format was encountered (typically integer valued parameter)
     * @throws DateTimeParseException   timestamp factory was specified with bad ISO-8605 date/time/duration format
     * @throws TypeNotPresentException  unrecognized enumeration constant (scalar factory JalScalarType or image factory BufferedImage.Format)   
     * @throws ConfigurationException   tensor factory had bad shape or structure factory missing depth and/or fan-out
     * @throws UnsupportedOperationException    scalar factory had bad 'numIncr' parameter
     * @throws NoSuchElementException   the column type is unrecognized (unsupported) 
     */
    public static List<FrameColumnsSpec<Record>> defaultFrame() throws NumberFormatException, IllegalArgumentException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        List<JalToolsColumnsConfig>     lstColDefCfgs =  CFG_FRM_DEF.columns;
        List<FrameColumnsSpec<Record>>  lstSpecCols = new ArrayList<>(lstColDefCfgs.size());
        
        for (JalToolsColumnsConfig cfg : lstColDefCfgs) {
            FrameColumnsSpec<Record> specCol = FrameColumnsSpec.from(cfg);
            
            lstSpecCols.add(specCol);
        }
        
        return lstSpecCols;
    }
    

    //
    // Operations
    //
    
    /**
     * <p>
     * Checks if the the <code>{@link #enmType}</code> field is consistent with the <code>{@link #specFactory}</code> field.
     * </p>
     * 
     * @return  <code>true</code> if this is a correctly populated <code>FrameColumnsSpec</code> record,
     *          <code>false</code> otherwise
     */
    public boolean  isValid() {
        
        return switch (this.enmType) {
        case SCALAR -> ( (this.specFactory instanceof ScalarFactorySpec) && ScalarFactorySpec.class.isAssignableFrom(this.clsFactory) );
        case TIMESTAMP -> ( (this.specFactory instanceof TimestampFactorySpec) && TimestampFactorySpec.class.isAssignableFrom(this.clsFactory) );
        case BYTES -> ( (this.specFactory instanceof ByteArrayFactorySpec) && ByteArrayFactorySpec.class.isAssignableFrom(this.clsFactory) );
        case IMAGE -> ( (this.specFactory instanceof ImageFactorySpec) && ImageFactorySpec.class.isAssignableFrom(this.clsFactory) );
        case TENSOR -> ( (this.specFactory instanceof TensorFactorySpec) && TensorFactorySpec.class.isAssignableFrom(this.clsFactory) );
        case STRUCTURE -> ( (this.specFactory instanceof StructureFactorySpec) && StructureFactorySpec.class.isAssignableFrom(this.clsFactory) );
        default -> false;
        };
    }
    
    /**
     * <p>
     * Creates and returns a new data column factory instance according to the specifications in this record.
     * </p>
     * <p>
     * A new datum factory instance is created according to the field <code>{@link #specFactory()}</code> and
     * attached to the returned data column factory.  The data column factory will then produce 
     * <code>{@link #intCols()}</code> columns of simulated data with column names prefixed by 
     * <code>{@link #strNmPref()}</code>.
     * </p>
     * <p>
     * The returned implementation of <code>{@link IFrameColumnsFactory}</code> is determined by the field values
     * of this record.  In particular, the type of the column data produced by the column is specified by the
     * <code>{@link #specFactory()}</code> field which is of template parameter type <code>FactorySpec</code>.
     * That is, a datum factory for the data column factory is created here according to the datum factory
     * specification in <code>{@link #specFactory()}</code>.
     * If the datum factory specification within this field is not supported by <code>{@link FrameColumnsSpec}</code> 
     * (see record documentation) an exception is thrown.
     * </p>
     * <p>
     * Currently the implementation class of <code>{@link IFrameColumnsFactory}</code> is
     * <code>{@link FrameColumnsFactory}</code>.
     * </p>
     *    
     * @return  a new data column factory as specified by the current record configuration
     * 
     * @throws UnsupportedOperationException    an unrecognized datum factory was encountered
     */
    public IFrameColumnsFactory<Object>  newFactory() throws UnsupportedOperationException {
        
//        // Create the column names
//        Set<String> setColNms = IntStream.range(0, this.intCols)
//                                .<String>mapToObj(i -> this.strNmPref + Integer.toString(i))
//                                .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);

        // Create the column factory from the datum factory type case 
        if (this.specFactory instanceof ScalarFactorySpec spec) 
            return FrameColumnsFactory.from(this.setColNms, spec.newFactory());
        else if (this.specFactory instanceof TimestampFactorySpec spec)
            return FrameColumnsFactory.from(this.setColNms, spec.newFactory());
        else if (this.specFactory instanceof ByteArrayFactorySpec spec)
            return FrameColumnsFactory.from(this.setColNms, spec.newFactory());
        else if (this.specFactory instanceof ImageFactorySpec spec)
            return FrameColumnsFactory.from(this.setColNms, spec.newFactory());
        else if (this.specFactory instanceof TensorFactorySpec spec)
            return FrameColumnsFactory.from(this.setColNms, spec.newFactory());
        else if (this.specFactory instanceof StructureFactorySpec spec)
            return FrameColumnsFactory.from(this.setColNms, spec.newFactory());
        else
            throw new UnsupportedOperationException(JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Unrecognized datum factory specification: " + specFactory.getClass().getName());
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
//        String strPadd = strPad + "  ";
        
        // Create the column name list head
        final int           szHead = (this.setColNms.size() < FrameColumnsSpec.INT_MAX_COLS_HEAD) ? this.setColNms.size() : FrameColumnsSpec.INT_MAX_COLS_HEAD;
        final List<String>  lstHead = this.setColNms.stream().toList().subList(0, szHead);
        
//        // Create the datum factory description with left-hand side padding
//        String  strDescrFac = FrameColumnsSpec.insertPadding(this.specFactory.toString(), strPadd);
        
        ps.println(strPad + "Column count         : " + this.setColNms.size());
        ps.println(strPad + "Column names (head)  : " + lstHead);
        ps.println(strPad + "Column type          : " + this.enmType);
        ps.println(strPad + "Datum factory type   : " + this.clsFactory);
        ps.println(strPad + "Datum factory cfg.   : " + this.specFactory.toString());
//        ps.println(strDescrFac);
    }
    
    
    //
    // Support Operations
    //
    
    /**
     * <p>
     * Determines the data column factory specification column type from the datum factory record type.
     * </p>
     * <p>
     * Switches through the supported datum factory specification record types to determine the appropriate 
     * <code>{@link JalComlexType}</code> enumeration constant for the factory specification.  Specifically,
     * the returned value is given by the following:
     * <ul>
     * <li><code>argument == {@link ScalarFactorySpec} &rarr; {@link JalComplexType#SCALAR}</code>.</li>
     * <li><code>argument == {@link TimestampFactorySpec} &rarr; {@link JalComplexType#TIMESTAMP}</code>.</li>
     * <li><code>argument == {@link ByteArrayFactorySpec} &rarr; {@link JalComplexType#BYTES}</code>.</li>
     * <li><code>argument == {@link ImageFactorySpec} &rarr; {@link JalComplexType#IMAGE}</code>.</li>
     * <li><code>argument == {@link TensorFactorySpec} &rarr; {@link JalComplexType#TENSOR}</code>.</li>
     * <li><code>argument == {@link StructureFactorySpec} &rarr; {@link JalComplexType#STRUCTURE}</code>.</li>
     * </ul>
     * If the argument is neither of the above values an exception is thrown.
     * </p>
     * 
     * @param <FactorySpec> record type of the datum factory specification for column data
     * 
     * @param specFactory   the datum factory specification used for simulated data production
     * 
     * @return  the <code>JalComplexType</code> constant associated with the given datum factory specification
     * 
     * @throws UnsupportedOperationException    the datum factory specification was unrecognized or unsupported
     */
    public static <FactorySpec extends Record> JalComplexType  inferColumnType(FactorySpec specFactory) throws UnsupportedOperationException {
        
        // Switch through supported specification cases:
        if (specFactory instanceof ScalarFactorySpec)
            return JalComplexType.SCALAR;
        else if (specFactory instanceof TimestampFactorySpec)
            return JalComplexType.TIMESTAMP;
        else if (specFactory instanceof ByteArrayFactorySpec)
            return JalComplexType.BYTES;
        else if (specFactory instanceof ImageFactorySpec)
            return JalComplexType.IMAGE;
        else if (specFactory instanceof TensorFactorySpec)
            return JalComplexType.TENSOR;
        else if (specFactory instanceof StructureFactorySpec)
            return JalComplexType.STRUCTURE;
        else
            throw new UnsupportedOperationException(JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Unrecognized datum factory specification: " + specFactory.getClass().getName());
    }
    
    /**
     * <p>
     * Convenience method for calling the parsing creator of the datum factory specification associated with the 
     * given complex type.
     * </p>
     * <p>
     * This is a convenience method for instantiating data factory specifications according to their
     * type specification and parsing creator (i.e., <code>parse(String...)</code>).  
     * If the type of a datum factory specification is known a priori it is best to use its creators
     * directly.    
     * This method is used internally for creating the <code>{@link #specFactory()}</code> record field according 
     * to type.  Normally it would be left private but since there is no state, only associations, it is
     * left public.
     * </p>
     * <p> 
     * The method first identifies the supported datum factory specification through the 
     * associated <code>{@link JalComplexType}</code> constant.  (See the documentation for 
     * <code>{@link FrameColumnsSpec}</code> for the supported data factories and their associated
     * <code>{@link JalComplexType}</code> enumeration constants.)  The <code>parse(String...)</code>
     * creator of the datum factory specification is then called with the given arguments.
     * </p>
     * 
     * @param enmType   data type of the datum factory (i.e., data column type) 
     * @param args      arguments to the parsing creator of the datum factory specification 
     * 
     * @return  a new datum factory specification record determined by the arguments
     * 
     * @throws TypeNotPresentException      an enumeration constant was not recognized
     * @throws NumberFormatException        invalid number format (e.g., seed value for scalar factory specification)
     * @throws UnsupportedOperationException unable to create 'numIncr' field in scalar factory specification
     * @throws MissingResourceException     timestamp factory had empty arguments
     * @throws DateTimeParseException       invalid format for ISO-8601 time and/or duration specification 
     * @throws ConfigurationException       tensor shape was invalid
     * @throws NoSuchElementException       the 'enmType' constant was not supported
     */
    public static Record parseFactorySpec(JalComplexType enmType, String...args) throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException, MissingResourceException, DateTimeParseException, ConfigurationException, NoSuchElementException {
        
        Record specFactory = switch (enmType) {
        case SCALAR -> ScalarFactorySpec.parse(args);       // throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException
        case BYTES -> ByteArrayFactorySpec.parse(args);     // throws NumberFormatException
        case TIMESTAMP -> TimestampFactorySpec.parse(args); // throws MissingResourceException, NumberFormatException, DateTimeParseException
        case IMAGE -> ImageFactorySpec.parse(args);         // throws NumberFormatException, TypeNotPresentException
        case TENSOR -> TensorFactorySpec.parse(args);       // throws ConfigurationException, NumberFormatException, TypeNotPresentException, UnsupportedOperationException
        case STRUCTURE -> StructureFactorySpec.parse(args); // throws NumberFormatException, TypeNotPresentException, UnsupportedOperationException
        default -> throw new NoSuchElementException("Unexpected value: " + enmType);
        };
        
        return specFactory;
    }
    
    
    //
    // Comparable<FrameColumnsSpec> Interface
    //
    
    /**
     * <p>
     * Provides the ordering of <code>FrameColumnsSpec</code> instances within Java collections.
     * </p>
     * <p>
     * This method provides an order based upon the number of columns names in <code>{@link #setColNms()}</code>.
     * The method first checks for equivalence (i.e., <code>{@link #equals(Object)}</code> returning 0
     * if so.
     * Then, if the number of column names is less than or equal to the that of the argument a -1 value is returned,
     * otherwise a +1 is returned.
     * </p
     * <p>
     * <h2>Formula</h2>
     * The explicit formula for the the returned value for 2 <code>FrameColumnsSpec</code> instances is given below.
     * Let <code>A</code> and <code>B</code> be the records under comparison where <code>A = this</code> 
     * and <code>B</code> is the argument.
     * Then  
     * <ul>
     * <li> 0 &lArr; <code>A.equals(B) == true</code>.</li>
     * <li>-1 &lArr; <code>A.setColNms().size() <= B.setColNms().size()</code>.</li>
     * <li>+1 &lArr; <code>A.setColNms().size() > B.setColNms().size()</code>.</li>
     * </ul>
     * Note that the above formula prevents clobbering of non-equivalent <code>FrameColumnsSpec</code> instances
     * with equal column counts.
     * </p>
     * <p>
     * <h2>Reflexativity</h2>
     * Thus it is not a reflective operation as the operation does not commute.  
     * Specifically,  
     * <pre>
     * <code>
     *  A.compareTo(B) &ne; B.compareTo(A) iff A.setColNms().size() == B.setColsNms().size()
     * </code>
     * </pre> 
     * Thus, the operation is not strictly as defined in <code>{@link Comparable#compareTo(Object)}</code>.
     * However, the operation is transitive.  Specifically, for specifications <code>A, B, C</code>
     * <pre>
     * <code>
     *   A.compareTo(B) > 0 && B.compareTo(C) > 0 &rArr; A.compareTo(C) > 0
     * </code>
     * </pre> 
     * 
     * @param specCols  the <code>FrameColumnsSpec</code> instance to compare with this instance
     *  
     * @return  0 if this.{@link #equals(Object)}, -1 if this.{@link #setColNms().size()} <= specCols.setColNms().size(), else +1
     * 
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(FrameColumnsSpec<FactorySpec> specCols) {
        
        if (this.equals(specCols))
            return 0;
        
        if (this.setColNms.size() <= specCols.setColNms.size())
            return -1;
        else
            return +1;
    }

    
    //
    // Record Overrides
    //
    
    /**
     * <p>
     * Provides an equivalence evaluation of the argument with this record.
     * </p>
     * <p>
     * The argument is first check to be of type <code>FrameColumnsSpec</code>.
     * If so, the field <b>values</b> of the argument then checked for <em>equivalence</em>,
     * that is, they have the same value but not necessary are the same object.
     * </p>
     * 
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof FrameColumnsSpec spec) {
            boolean bolResult = (this.setColNms.equals(spec.setColNms))
                    && (this.enmType == spec.enmType)
                    && (this.specFactory.equals(spec.specFactory));
            return bolResult;
        }
        
        return false;
    }

    /**
     * @see java.lang.Record#toString()
     */
    @Override
    public String toString() {
        final int   szMaxNms = 5;
        
        final int           szHead = (this.setColNms.size() < szMaxNms) ? this.setColNms.size() : szMaxNms;
        final List<String>  lstHead = this.setColNms.stream().toList().subList(0, szHead);
        
        StringBuilder   buf = new StringBuilder("(");
        buf.append("Columns=" + this.setColNms.size() + ",");
        buf.append("Names (head)=" + lstHead + ", ");
        buf.append("Type=" + this.enmType + ", ");
        buf.append("Datum factory=" + this.specFactory.toString() + ")");
        
        return buf.toString();
    }

    
    //
    // JAL Library Resources
    //

    /** JAL Tools default configuration parameters for ingestion frame factories */
    private static final JalToolsFramesConfig           CFG_FRM_DEF = JalToolsConfig.getInstance().datagen.frame;
    
    /** JAL Tools default configuration parameters for column factories */
    private static final JalToolsColumnsConfig          CFG_COL_DEF = JalToolsConfig.getInstance().datagen.column;
    
    
    // 
    // Record Constants - Default Values
    //
    
    /** The default column name prefix */
    public static final String          STR_NM_PREF_DEF = CFG_COL_DEF.name;
    
    /** The default column count */
    public static final int             INT_COL_CNT_DEF = CFG_COL_DEF.count;
    
    /** The default column type */
    public static final JalComplexType  ENM_COL_TYPE_DEF = CFG_COL_DEF.type;
    
    /** The default column value factory specification parameters (parse string) */
    public static final String[]        ARR_FAC_SPEC_DEF = CFG_COL_DEF.factory;
    
    
    //
    // Record Constants
    //
    
    /** The maximum number of column names to list in operation <code>{@link #printOut(PrintStream, String)}</code> */
    public static final int             INT_MAX_COLS_HEAD = 10;
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates a new <code>FrameColumnsSpec</code> from the given data column default parameters structure class.
     * </p>
     * <p>
     * The given structure class is assumed to originate from the JAL Tools default configuration
     * <code>{@link JalToolsConfig}</code>.  This method extracts the attributes of the structure class and uses
     * them to populate the field values of the returned data column factory specification.
     * </p>
     * 
     * @param cfgCols   data column default parameters structure class
     * 
     * @return  new <code>FrameColumnsSpec</code> record populated from the argument attributes
     * 
     * @throws TypeNotPresentException      an enumeration constant was not recognized
     * @throws NumberFormatException        invalid number format (e.g., seed value for scalar factory specification)
     * @throws UnsupportedOperationException unable to create 'numIncr' field in scalar factory specification
     * @throws MissingResourceException     timestamp factory had empty arguments
     * @throws DateTimeParseException       invalid format for ISO-8601 time and/or duration specification 
     * @throws ConfigurationException       tensor shape was invalid
     * @throws NoSuchElementException       the 'enmType' constant was not supported
     */
    private static FrameColumnsSpec<Record> from(JalToolsColumnsConfig cfgCols) throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException, MissingResourceException, DateTimeParseException, ConfigurationException, NoSuchElementException {
        int             intCols = cfgCols.count;
        String          strNmPref = cfgCols.name;
        JalComplexType  enmType = cfgCols.type;
        String[]        arrFacArgs = cfgCols.factory;
        Record          recFactory = FrameColumnsSpec.parseFactorySpec(enmType, arrFacArgs);    // throws all exceptions
        
        return FrameColumnsSpec.from(intCols, strNmPref, recFactory);
    }
    
    /**
     * <p>
     * Inserts given padding string on the left-hand side of every line within the description string.
     * </p>
     * <p>
     * This method is intended for adding padding to the description string of the datum factory description strings 
     * obtained from <code>{@link Record#toString()</code>.
     * </p>
     * 
     * @param strDescr  description string of datum factory
     * @param strPad    left-hand side padding line padding
     * 
     * @return  the given description string with all lines padded on the left-hand side
     */
    @SuppressWarnings("unused")
    private static String   insertPadding(String strDescr, String strPad) {
        if (strPad == null) 
            strPad = "";
        
        StringTokenizer     st = new StringTokenizer(strDescr, "\n");
        StringBuilder       buf = new StringBuilder();
        
        while (st.hasMoreTokens()) {
            String      strToken = st.nextToken();
            String      strLine = strPad + strToken + "\n";
            
            buf.append(strLine);
        }
        
        return buf.toString();
    }
}
