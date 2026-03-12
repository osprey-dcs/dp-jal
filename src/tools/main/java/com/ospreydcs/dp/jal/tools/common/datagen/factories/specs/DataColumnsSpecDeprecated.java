/*
 * Project: dp-jal
 * File:	DataColumnsSpecDeprecated.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.frames
 * Type: 	DataColumnsSpecDeprecated
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
 * @since Nov 4, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import java.lang.reflect.MalformedParametersException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.ingest.IngestionFrame;
import com.ospreydcs.dp.jal.tools.common.datagen.IFrameColumnsFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.frames.FrameColumnsFactory;
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
 * Record specification for configuring data column factory instances.
 * </p>
 * <p>
 * The record represents a specification for data columns factories exposing the 
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
 * A <code>DataColumnsSpecDeprecated</code> record is a specification for a particular data column factory configuration.
 * A <code>DataColumnsSpecDeprecated</code> instance produces data columns factories with the <code>{@link #newFactory()}</code>
 * method.  This method can be invoked multiple times to create multiple factories, all of the same configuration.
 * </p>
 * <p> 
 * Note that the <code>{@link IFrameColumnsFactory#build(int)}</code> operation creates multiple data columns.
 * Each data column is the same size and contains simulated data of the same type.  Thus, instances of 
 * <code>DataColumnsSpecDeprecated</code> specify a fixed number of data columns with a given data type.
 * </p>
 * <p>
 * <h2>Heterogeneous Data</h2> 
 * Data column factories exposing the <code>{@link IFrameColumnsFactory}</code> produce <em>heterogeneous data</em>,
 * that is, the simulated data within a column can be of any supported type.  However, the data produced by any
 * given data column factory is all of the same type, that is, all data columns are of the same type.
 * The data type of the column data is determined by the field <code>{@link #enmColType()}</code>, which must be
 * consistent with the datum factory configuration specified in field <code>{@link #recFacSpec()}</code>.  The method
 * <code>{@link #isValid()}</code> can be used to check the consistency of these two fields.
 * </p>
 * <p>
 * Ingestion frame factories can contain multiple data column factories, see for example 
 * <code>{@link IngestionFrameFactory#addDataColumns(Collection)}</code>.  Thus, ingestion frame factories
 * can be configured to produce <code>IngestionFrame</code> instances with heterogeneous data, columns with
 * different data types.  To achieve this condition multiple <code>DataColumnsSpecDeprecated</code> instances are used
 * each with different values for <code>{@link #enmColType()}</code> and <code>{@link #recFacSpec}</code>.
 * </p>  
 * <p>
 * <h2>Datum Factories</h2>
 * The type parameter <code>{@link FactorySpec}</code> identifies the type and configuration for the
 * datum factory producing the simulated column data.  Again, the type parameters must be consistent with the
 * <code>{@link #enmColType()}</code> field.  The inclusion of both the type parameter and the datum type field
 * is necessary for the parsing operation <code>{@link #parse(String...)}</code> available for parsing application
 * command-line arguments.
 * </p>  
 * <p>
 * There are multiple record types available, one for each supported datum type.
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
 * <h2>Creators</h2>
 * There are creators available for <code>DataColumnsSpecDeprecated</code> records configured to specific datum factory types.
 * Typically, there are multiple creators available for each datum factory type where the additional creators offer
 * the use of default parameter values.  Default parameters are taken from the JAL Tools default configuration.
 * </p>
 * <p>
 * The creator <code>{@link #parse(String...)}</code> is available for the parsing of application command-line arguments.
 * It relies heavily on the like-named operations available in all of the datum factories described above
 * (i.e., of the form <code>{@link #recFacSpec}.parse(String...)</code>).
 * </p>
 * <p>
 * There is the special creator <code>{@link #defaultFrameColumns()}</code> which creates a new <code>DataColumnsSpecDeprecated</code>
 * record list according to the JAL Tools default configuration.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Nov 4, 2025
 *
 * @param   <FactorySpec>   specification for datum factory producing column data 
 * 
 * @param   cntCols         number of columns in the configuration (i.e., produced by <code>{@link IFrameColumnsFactory#build(int)}</code>)
 * @param   strNmPref       prefix given to all column names (full name appended with index)
 * @param   enmColType      the datum type for all column column data for all columns
 * @param   recFacSpec      configuration for the data factory producing the column data
 * 
 * @deprecated Replaced by DataColumnsSpecDeprecated
 */
@Deprecated(since="Dec 29, 2025", forRemoval=true)
public record DataColumnsSpecDeprecated<FactorySpec extends Record>(
        int                 cntCols,
        String              strNmPref,
        JalComplexType      enmColType,
        FactorySpec         recFacSpec
        ) 
{
    
    //
    // Creators
    // 
    
    /**
     * <p>
     * Parses and argument string to identify and create a data columns configuration (i.e., <code>DataColumnsSpecDeprecated</code> record).
     * </p>
     * <p>
     * <h2>Factory Specification Records</h2>
     * Data Type configuration records are internal record used for the <code>FactorySpec</code> generic type.  There is
     * one type for each data type supported for column data value creation. 
     * Currently there are the following:
     * <ol>
     * <li>Scalar - <code>{@link ScalarFactorySpec}</code>,</li>
     * <li>Timestamp - <code>{@link TimestampFactorySpec}</code>,</li>
     * <li>Bytes - <code>{@link ByteArrayFactorySpec}</code>,</li>
     * <li>Image - <code>{@link ImageFactorySpec}</code>.
     * <li>Array - <code>{@link TensorFactorySpec}</code>,</li>
     * <li>Structure - <code>{@link StructureFactorySpec}</code>,</li>
     * </ol> 
     * The above record fields contain the parameters necessary to configure the appropriate value generator for
     * column data.
     * </p> 
     * <p>
     * <h2>Caveat</h2>
     * Clearly it is not necessary for the arguments to be obtained from an application command line as described.  
     * So long as the stated conditions and formats are followed the method will create and return an appropriate 
     * Data Type configuration record.
     * </p> 
     * <p>
     * <h2>Usage</h2>
     * The argument is assumed to be part of an application command line.  For example, the command-line could
     * contain the delimited variable "--data" which appears as
     * <code>
     * <pre>
     * > java application --cols cnt prefix DTYPE [parameters ...]
     * </pre>
     * </code>
     * where 
     * <ul>
     * <li>DTYPE is a <code>JalComplexType</code> enumeration constant,</li>
     * <li>STYPE is a <code>JalScalarType</code> enumeration constant,</li>
     * <li>field1 is a configuration record field,</li>
     * <li>field2 is a configuration record field,</li>
     * <li>... are any additional fields.</li>
     * </ul>
     * The argument to this method is then the set of command-line strings
     * <code>
     * <pre>
     * DTYPE VTYPE field1 field2 ...
     * </pre>
     * </code>
     * The type of configuration record returned is given by the <code>DTYPE</code> value:
     * <ul>
     * <li><code>{@link JalComplexType#SCALAR}</code> - <code>{@link ScalarSpec}</code>.</li>
     * <li><code>{@link JalComplexType#TENSOR}</code> - <code>{@link TensorFactorySpec}</code>.</li>
     * <li><code>{@link JalComplexType#STRUCTURE}</code> - <code>{@link StructureFactorySpec}</code>.</li>
     * <li><code>{@link JalComplexType#IMAGE}</code> - <code>{@link ImageFactorySpec}</code>.</li>
     * </ul>
     * Thus, the number of elements within the argument string array is dependent upon the <code>JalComplexType</code>
     * identified by the first element.  If the number of arguments is not appropriate for the given type
     * a <code>ConfigurationException</code> is thrown.
     * </p>
     *  
     * @param args  argument string defining a configuration record
     * 
     * @return  the configuration record defined and populated by the arguments
     * 
     * @throws <s>IllegalArgumentException the argument contained no data</s>
     * @throws TypeNotPresentException  invalid enumeration constant (e.g., the 1st argument was not a <code>JalComplexType</code>)
     * @throws ConfigurationException   the argument contained the wrong number of arguments for the <code>JalComplexType</code>
     * @throws MalformedParametersException an enumeration constant within the argument set was not recognized (IMAGE)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static DataColumnsSpecDeprecated parse(String...args) throws TypeNotPresentException, ConfigurationException, MalformedParametersException {
        if (args.length < 1) 
            return DataColumnsSpecDeprecated.from();
        
        // Get the column count 
        int     cntCols = Integer.valueOf(args[0]);
        if (args.length < 2)
            return DataColumnsSpecDeprecated.from(cntCols);
        
        // Get the column name prefix
        String  strNmPref = args[1];
        if (args.length < 3)
            return DataColumnsSpecDeprecated.from(cntCols, strNmPref); // throws UnsupportedOperationException

        
        // Get the Datum Type of the column values
        JalComplexType  enmColType = JalComplexType.valueFrom(args[2]); // throws TypeNotPresentException
        
        // Parse the data factory parameters if provided
        String[]    arrFacCfg = (args.length < 4) ? new String[0] : Arrays.copyOfRange(args, 3, args.length);
        
        Record      recFacSpec = switch (enmColType) {
        case SCALAR -> ScalarFactorySpec.parse(arrFacCfg);    // throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException
        case BYTES -> ByteArrayFactorySpec.parse(arrFacCfg);   // throws NumberFormatException
        case IMAGE -> ImageFactorySpec.parse(arrFacCfg);       // throws NumberFormatException, TypeNotPresentException
        case TENSOR -> TensorFactorySpec.parse(arrFacCfg);      // throws IllegalArgumentException, ConfigurationException, NumberFormatException, TypeNotPresentException, UnsupportedOperationException 
        case STRUCTURE -> StructureFactorySpec.parse(arrFacCfg);  // throws ConfigurationException, NumberFormatException, TypeNotPresentException, UnsupportedOperationException
        default ->
            throw new NoSuchElementException(JavaRuntime.getQualifiedMethodNameSimple() + " - Data type not supported: " + enmColType);
        };
        
        // Create and return the data columns configuration
        return new DataColumnsSpecDeprecated(cntCols, strNmPref, enmColType, recFacSpec);
    }
 
    /**
     * <p>
     * Retrieves the default ingestion frame data columns specifications from the JAL Tools default configuration.
     * </p>
     * <p>
     * Retrieves the default column configurations contained in the <code>{@link JalToolsColumnsConfig}</code>
     * structure class list within the <code>{@link JalToolsConfig}</code> default configuration.  The parameters
     * for each column are parsed and a new <code>DataColumnsSpecDeprecated</code> record is created for each column.
     * The column configurations are returned in the order in which they appear in the default configuration.
     * </p>
     * 
     * @return  a collection of new <code>DataColumnsConfig</code> records as specified in the JAL Tools default configuration
     * 
     * @throws IllegalArgumentException general error (typically bad argument count)
     * @throws NumberFormatException    a bad numeric format was encountered (typically integer valued parameter)
     * @throws DateTimeParseException   timestamp factory was specified with bad ISO-8605 date/time/duration format
     * @throws TypeNotPresentException  unrecognized enumeration constant (scalar factory JalScalarType or image factory BufferedImage.Format)   
     * @throws ConfigurationException   tensor factory had bad shape or structure factory missing depth and/or fan-out
     * @throws UnsupportedOperationException    scalar factory had bad 'increment' parameter
     * @throws NoSuchElementException   the column type is unrecognized (unsupported) 
     */
    @SuppressWarnings("rawtypes")
    public static List<DataColumnsSpecDeprecated> defaultFrameColumns() throws NumberFormatException, IllegalArgumentException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        List<JalToolsColumnsConfig> lstColCfgDef =  CFG_FRM_DEF.columns;
        List<DataColumnsSpecDeprecated>       lstColSpec = new ArrayList<>(lstColCfgDef.size());
        
        for (JalToolsColumnsConfig cfg : lstColCfgDef) {
            int             cntCols = cfg.count;
            String          strNmPref = cfg.name;
            JalComplexType  enmType = cfg.type;
            String[]        arrArgsFactory = cfg.factory;
            
            Record  recFacSpec = switch (enmType) {
            case SCALAR -> ScalarFactorySpec.parse(arrArgsFactory);   // throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException
            case TIMESTAMP -> TimestampFactorySpec.parse(arrArgsFactory);  // throws IllegalArgumentException, NumberFormatException, DateTimeParseException
            case BYTES -> ByteArrayFactorySpec.parse(arrArgsFactory);      // throws NumberFormatException
            case IMAGE -> ImageFactorySpec.parse(arrArgsFactory);          // throws NumberFormatException, TypeNotPresentException
            case TENSOR -> TensorFactorySpec.parse(arrArgsFactory);         // throws IllegalArgumentException, NumberFormatException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException
            case STRUCTURE -> StructureFactorySpec.parse(arrArgsFactory);     // throws ConfigurationException, NumberFormatException, TypeNotPresentException, UnsupportedOperationException
            default -> throw new NoSuchElementException("Unrecognized (unsupported) column type value: " + enmType);
            };
            
            @SuppressWarnings("unchecked")
            DataColumnsSpecDeprecated recColSpec = new DataColumnsSpecDeprecated(cntCols, strNmPref, enmType, recFacSpec);
            
            lstColSpec.add(recColSpec);
        }
        
        return lstColSpec;
    }
    
    @SuppressWarnings("rawtypes")
    public static DataColumnsSpecDeprecated   from() throws NumberFormatException, UnsupportedOperationException, TypeNotPresentException, ConfigurationException, NoSuchElementException {
        return DataColumnsSpecDeprecated.from(INT_COL_CNT_DEF);
    }
    
    /**
     * @param cntCols
     * 
     * @return
     * 
     * @throws TypeNotPresentException  unknown <code>JalScalarType</code> constant
     * @throws NumberFormatException    invalid numeric format (e.g., bad 'lngSeed' value)
     * @throws ConfigurationException   tensor factory had invalid shape
     * @throws UnsupportedOperationException unable to create 'numIncr' parameter in scalar factory
     * @throws NoSuchElementException   the value of <code>{@link #ENM_COL_TYPE_DEF}</code> was unrecognized
     */
    @SuppressWarnings("rawtypes")
    public static DataColumnsSpecDeprecated   from(int cntCols) throws UnsupportedOperationException, NumberFormatException, TypeNotPresentException, ConfigurationException, NoSuchElementException {
        Record  recFac = DataColumnsSpecDeprecated.extractDefaultFactorySpec();
        
        return DataColumnsSpecDeprecated.from(cntCols, recFac);
    }
    
    @SuppressWarnings("rawtypes")
    public static DataColumnsSpecDeprecated   from(Record recFacSpec) throws UnsupportedOperationException {
        return DataColumnsSpecDeprecated.from(INT_COL_CNT_DEF, recFacSpec);
    }
    
    @SuppressWarnings("rawtypes")
    public static DataColumnsSpecDeprecated   from(int cntCols, Record recFacSpec) throws UnsupportedOperationException {
        return DataColumnsSpecDeprecated.from(cntCols, STR_NM_PREF_DEF, recFacSpec);
    }
    
    @SuppressWarnings("rawtypes")
    public static DataColumnsSpecDeprecated   from(int cntCols, String strNmPref) throws UnsupportedOperationException, NumberFormatException, TypeNotPresentException, ConfigurationException, NoSuchElementException {
        Record  recFac = DataColumnsSpecDeprecated.extractDefaultFactorySpec();
        
        return DataColumnsSpecDeprecated.from(cntCols, strNmPref, recFac);
    }
    
    @SuppressWarnings("rawtypes")
    public static DataColumnsSpecDeprecated   from(int cntCols, String strNmPref, Record recFacSpec) throws UnsupportedOperationException {

        if (recFacSpec instanceof ScalarFactorySpec spec) 
            return new DataColumnsSpecDeprecated<ScalarFactorySpec>(cntCols, strNmPref, JalComplexType.SCALAR, spec);
        
        else if (recFacSpec instanceof TimestampFactorySpec spec)
            return new DataColumnsSpecDeprecated<TimestampFactorySpec>(cntCols, strNmPref, JalComplexType.TIMESTAMP, spec);
        
        else if (recFacSpec instanceof ByteArrayFactorySpec spec)
            return new DataColumnsSpecDeprecated<ByteArrayFactorySpec>(cntCols, strNmPref, JalComplexType.BYTES, spec);
        
        else if (recFacSpec instanceof ImageFactorySpec spec)
            return new DataColumnsSpecDeprecated<ImageFactorySpec>(cntCols, strNmPref, JalComplexType.IMAGE, spec);
        
        else if (recFacSpec instanceof TensorFactorySpec spec)
            return new DataColumnsSpecDeprecated<TensorFactorySpec>(cntCols, strNmPref, JalComplexType.TENSOR, spec);
        
        else if (recFacSpec instanceof StructureFactorySpec spec)
            return new DataColumnsSpecDeprecated<StructureFactorySpec>(cntCols, strNmPref, JalComplexType.STRUCTURE, spec);
        
        else
            throw new UnsupportedOperationException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Unsupported datum factory specification: " 
                    + recFacSpec.getClass().getName());
    }
    
    public static <FactorySpec extends Record>    DataColumnsSpecDeprecated<FactorySpec>  from(int cntCols, String strPrefix, JalComplexType enmColType, FactorySpec recFacSpec) {
        return new DataColumnsSpecDeprecated<FactorySpec>(cntCols, strPrefix, enmColType, recFacSpec);
    }
    
    
//    public static DataColumnsSpecDeprecated<ScalarFactorySpec> newScalarCols(int cntCols, String strNmPref, ScalarFactorySpec recScalarSpec) {
//        DataColumnsSpecDeprecated<ScalarFactorySpec>  recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.SCALAR, recScalarSpec);
//        
//        return recColSpec;
//    }
//    
//    
//    public static DataColumnsSpecDeprecated<TimestampFactorySpec> newTimestamps(int cntCols, TimestampFactorySpec specTms) {
//        return DataColumnsSpecDeprecated.newTimestamps(cntCols, STR_NM_PREF_DEF, specTms);
//    }
//    
//    public static DataColumnsSpecDeprecated<TimestampFactorySpec> newTimestamps(int cntCols, String strNmPref, TimestampFactorySpec specTms) {
//        return new DataColumnsSpecDeprecated<TimestampFactorySpec>(cntCols, strNmPref, JalComplexType.TIMESTAMP, specTms);
//    }
//    
//    
//    public static DataColumnsSpecDeprecated<TimestampFactorySpec>    newTimestampCols(int cntCols, String strNmPref, boolean bolRand) {
//        TimestampFactorySpec                   recTmsSpec = TimestampFactorySpec.from(bolRand);
//        DataColumnsSpecDeprecated<TimestampFactorySpec>  recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.TIMESTAMP, recTmsSpec);
//        
//        return recColSpec;
//    }
//    
//    public static DataColumnsSpecDeprecated<TimestampFactorySpec>    newTimestampCols(int cntCols, String strNmPref, boolean bolRand, long lngSeed) {
//        TimestampFactorySpec                   recTmsSpec = TimestampFactorySpec.from(bolRand, lngSeed);
//        DataColumnsSpecDeprecated<TimestampFactorySpec>  recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.TIMESTAMP, recTmsSpec);
//        
//        return recColSpec;
//    }
//    
//    public static DataColumnsSpecDeprecated<TimestampFactorySpec>    newTimestampCols(int cntCols, String strNmPref, Duration durPeriod, Instant insStart) {
//        TimestampFactorySpec                   recTmsSpec = TimestampFactorySpec.from(durPeriod, insStart);
//        DataColumnsSpecDeprecated<TimestampFactorySpec>  recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.TIMESTAMP, recTmsSpec);
//        
//        return recColSpec;
//    }
//    
//    
//    public static DataColumnsSpecDeprecated<ByteArrayFactorySpec>    newByteArrayCols(int cntCols, String strNmPref) {
//        ByteArrayFactorySpec               recBytSpec = ByteArrayFactorySpec.from();
//        DataColumnsSpecDeprecated<ByteArrayFactorySpec>  recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.BYTES, recBytSpec);
//        
//        return recColSpec;
//    }
//    
//    public static DataColumnsSpecDeprecated<ByteArrayFactorySpec>    newByteArrayCols(String strNmPref, int cntCols, int cntBytes) {
//        ByteArrayFactorySpec                   recArrSpec = ByteArrayFactorySpec.from(cntBytes);
//        DataColumnsSpecDeprecated<ByteArrayFactorySpec>  recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.BYTES, recArrSpec);
//        
//        return recColSpec;
//    }
//    
//    
//    public static DataColumnsSpecDeprecated<ImageFactorySpec>    newImageCols(int cntCols, String strNmPref) {
//        ImageFactorySpec                   recImgSpec = ImageFactorySpec.from();
//        DataColumnsSpecDeprecated<ImageFactorySpec>  recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.IMAGE, recImgSpec);
//        
//        return recColSpec;
//    }
//    
//    public static DataColumnsSpecDeprecated<ImageFactorySpec>    newImageCols(int cntCols, String strNmPref, int size) {
//        ImageFactorySpec                   recImgSpec = ImageFactorySpec.from(size);
//        DataColumnsSpecDeprecated<ImageFactorySpec>  recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.IMAGE, recImgSpec);
//        
//        return recColSpec;
//    }
//    
//    public static DataColumnsSpecDeprecated<ImageFactorySpec>    newImageCols(String strNmPref, int cntCols, int size, BufferedImage.Format enmFmt) {
//        ImageFactorySpec                   recImgSpec = ImageFactorySpec.from(size, enmFmt);
//        DataColumnsSpecDeprecated<ImageFactorySpec>  recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.IMAGE, recImgSpec);
//        
//        return recColSpec;
//    }
//    
//    public static DataColumnsSpecDeprecated<ImageFactorySpec>    newImageCols(int cntCols, String strNmPref, int size, BufferedImage.Format enmFmt, String strImgPref) {
//        ImageFactorySpec                   recImgSpec = ImageFactorySpec.from(size, enmFmt, strImgPref);
//        DataColumnsSpecDeprecated<ImageFactorySpec>  recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.IMAGE, recImgSpec);
//        
//        return recColSpec;
//    }
//    
//    
//    public static DataColumnsSpecDeprecated<TensorFactorySpec>   newTensorCols(String strNmPref, int cntCols, int[] shape) {
//        TensorFactorySpec                  recTenSpec = TensorFactorySpec.from(shape);
//        DataColumnsSpecDeprecated<TensorFactorySpec> recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.TENSOR, recTenSpec);
//        
//        return recColSpec;
//    }
//    
//    public static DataColumnsSpecDeprecated<TensorFactorySpec>   newTensorCols(String strNmPref, int cntCols, int[] shape, ScalarFactorySpec recScalarSpec) {
//        TensorFactorySpec                  recTenSpec = TensorFactorySpec.from(shape, recScalarSpec);
//        DataColumnsSpecDeprecated<TensorFactorySpec> recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.TENSOR, recTenSpec);
//        
//        return recColSpec;
//    }
//    
//    
//    public static DataColumnsSpecDeprecated<StructureFactorySpec>  newStructCols(int cntCols, String strNmPref, boolean bolUniqFldNms, StructureFactoryLib enmLib) {
//        ScalarFactorySpec                       specScal = enmLib.getScalarFactorySpec();
//        StructureFactorySpec                    specStruct = StructureFactorySpec.from(cntCols, cntCols, bolUniqFldNms, specScal);
//        DataColumnsSpecDeprecated<StructureFactorySpec>   specCols = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.STRUCTURE, specStruct);
//        
//        return specCols;
//    }
//    
//    public static DataColumnsSpecDeprecated<StructureFactorySpec>   newStructCols(int cntCols, String strNmPref, int depth, int fanout, boolean bolUniqNms) {
//        StructureFactorySpec                  recStrSpec = StructureFactorySpec.from(depth, fanout, bolUniqNms);
//        DataColumnsSpecDeprecated<StructureFactorySpec> recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.STRUCTURE, recStrSpec);
//        
//        return recColSpec;
//    }
//    
//    public static DataColumnsSpecDeprecated<StructureFactorySpec>   newStructCols(int cntCols, String strNmPref, int intDepth, int intFanout, ScalarFactorySpec recScalarSpec) {
//        StructureFactorySpec                  recStrSpec = StructureFactorySpec.from(intDepth, intFanout, recScalarSpec);
//        DataColumnsSpecDeprecated<StructureFactorySpec> recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.STRUCTURE, recStrSpec);
//        
//        return recColSpec;
//    }
//    
//    public static DataColumnsSpecDeprecated<StructureFactorySpec>   newStructCols(int cntCols, String strNmPref, int intDepth, int intFanout, boolean bolUniqNms, ScalarFactorySpec recScalarSpec) {
//        StructureFactorySpec                  recStrSpec = StructureFactorySpec.from(intDepth, intFanout, bolUniqNms, recScalarSpec);
//        DataColumnsSpecDeprecated<StructureFactorySpec> recColSpec = new DataColumnsSpecDeprecated<>(cntCols, strNmPref, JalComplexType.STRUCTURE, recStrSpec);
//        
//        return recColSpec;
//    }
    
    
    //
    // Internal Types
    //

    /**
     * <p>
     * Checks if the the <code>{@link #enmColType}</code> field is consistent with the <code>{@link #recFacSpec}</code> field.
     * </p>
     * 
     * @return  <code>true</code> if this is a correctly populated <code>DataColumnsSpecDeprecated</code> record,
     *          <code>false</code> otherwise
     */
    public boolean  isValid() {
        
        return switch (this.enmColType) {
        case SCALAR -> (this.recFacSpec instanceof ScalarFactorySpec);
        case TIMESTAMP -> (this.recFacSpec instanceof TimestampFactorySpec);
        case BYTES -> (this.recFacSpec instanceof ByteArrayFactorySpec);
        case IMAGE -> (this.recFacSpec instanceof ImageFactorySpec);
        case TENSOR -> (this.recFacSpec instanceof TensorFactorySpec);
        case STRUCTURE -> (this.recFacSpec instanceof StructureFactorySpec);
        default -> false;
        };
    }
    
    public IFrameColumnsFactory<Object>  newFactory() throws ConfigurationException, UnsupportedOperationException {
        
        // Check record configuration
        boolean bolValid = this.isValid();

        if (!bolValid) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Datum type is " + this.enmColType 
                    + " but factory specification is " + this.recFacSpec.getClass().getSimpleName();
            throw new ConfigurationException(strMsg);
        };
        
        // Create the column names
        Set<String> setColNms = IntStream.range(0, this.cntCols)
                                .<String>mapToObj(i -> this.strNmPref + Integer.toString(i))
                                .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);

        // Create the data column factory
        FrameColumnsFactory  facCols;
        
        switch (this.enmColType) {
        case SCALAR:
            ScalarFactorySpec   valSpec = (ScalarFactorySpec) this.recFacSpec;
            ScalarFactory       facVals = valSpec.newFactory();
            
            facCols = FrameColumnsFactory.from(setColNms, facVals);
            break;
            
        case TIMESTAMP:
            TimestampFactorySpec tmsSpec = (TimestampFactorySpec) this.recFacSpec;
            TimestampFactory     facTms = tmsSpec.newFactory();
            
            facCols = FrameColumnsFactory.from(setColNms, facTms);
            break;
            
        case BYTES:
            ByteArrayFactorySpec arrSpec = (ByteArrayFactorySpec) this.recFacSpec;
            ByteArrayFactory    facBytes = arrSpec.newFactory();

            facCols = FrameColumnsFactory.from(setColNms, facBytes);
            break;
            
        case IMAGE:
            ImageFactorySpec    imgSpec = (ImageFactorySpec) this.recFacSpec;
            ImageFactory        facImgs = imgSpec.newFactory();
            
            facCols = FrameColumnsFactory.from(setColNms, facImgs);
            break;
            
        case TENSOR:
            TensorFactorySpec   tenSpec = (TensorFactorySpec) this.recFacSpec;
            TensorFactory       facTens = tenSpec.newFactory();
            
            facCols = FrameColumnsFactory.from(setColNms, facTens);
            break;
            
        case STRUCTURE:
            StructureFactorySpec    stcSpec = (StructureFactorySpec) this.recFacSpec;
            StructureFactory        facStruc = stcSpec.newFactory();
            
            facCols = FrameColumnsFactory.from(setColNms, facStruc);
            break;
            
        default:
            throw new UnsupportedOperationException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Datum type " + this.enmColType + " is not supported.");
        }
        
        // Return the new DataColumsFactory
        return facCols;
    }
    
    
    //
    // Record Overrides
    //
    
    /**
     * <p>
     * Provides an equivalence evaluation of the argument with this record.
     * </p>
     * <p>
     * The argument is first check to be of type <code>DataColumnsSpecDeprecated</code>.
     * If so, the field <b>values</b> of the argument then checked for <em>equivalence</em>,
     * that is, they have the same value but not necessary are the same object.
     * </p>
     * 
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof DataColumnsSpecDeprecated spec) {
            boolean bolResult = (this.cntCols == spec.cntCols)
                    && (this.strNmPref.equals(spec.strNmPref))
                    && (this.enmColType == spec.enmColType)
                    && (this.recFacSpec.equals(spec.recFacSpec));
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
        buf.append("Column count         : " + this.cntCols + "\n");
        buf.append("Column name (prefix) : " + this.strNmPref + "\n");
        buf.append("Column datum type    : " + this.enmColType + "\n");
        buf.append("Column datum factory \n");
        buf.append(this.recFacSpec.toString());
        
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
    // Support Methods
    //
    
    /**
     * <p>
     * Creates the default datum factory specification as defined in the JAL Tools default configuration.
     * </p>
     * <p>
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
    private static Record   extractDefaultFactorySpec() throws TypeNotPresentException, NumberFormatException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {

        Record recFactory = switch (ENM_COL_TYPE_DEF) {
        case SCALAR -> ScalarFactorySpec.parse(ARR_FAC_SPEC_DEF);
        case TENSOR -> TensorFactorySpec.parse(ARR_FAC_SPEC_DEF);
        case BYTES -> ByteArrayFactorySpec.parse(ARR_FAC_SPEC_DEF);
        case IMAGE -> ImageFactorySpec.parse(ARR_FAC_SPEC_DEF);
        case STRUCTURE -> StructureFactorySpec.parse(ARR_FAC_SPEC_DEF);
        case TIMESTAMP -> TimestampFactorySpec.parse(ARR_FAC_SPEC_DEF);
        default -> throw new NoSuchElementException("Unexpected value: " + ENM_COL_TYPE_DEF);
        };

        return recFactory;
    }
}
