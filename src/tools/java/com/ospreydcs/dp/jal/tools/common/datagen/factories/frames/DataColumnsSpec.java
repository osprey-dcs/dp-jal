/*
 * Project: dp-jal
 * File:	DataColumnsSpec.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.frames
 * Type: 	DataColumnsSpec
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
package com.ospreydcs.dp.jal.tools.common.datagen.factories.frames;

import java.lang.reflect.MalformedParametersException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.common.BufferedImage;
import com.ospreydcs.dp.jal.tools.common.datagen.IDataColumnsFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ByteArrayFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ImageFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactorySpec;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarGeneratorDeprecated;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.JalToolsDataGenConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.frames.JalToolsColumnsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.frames.JalToolsFramesConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Record containing parameters for configuring data column factory instances.
 * </p>
 * <p>
 * The record represents a specification for data columns factories exposing the 
 * <code>{@link IDataColumnsFactory}</code> interface.  
 * Implementations of the <code>{@link IDataColumnsFactory}</code> interface
 * are typically used in ingestion frame factories for creating ingestion frames of simulated data.
 * The specifications for the column configurations and simulated data are provided here;
 * that is, the record fields are configuration parameters for creating column factories.
 * The column factories are then, in turn, used to create <code>{@link IngestionFrame}</code>
 * instances with ingestion frame factories, for example, <code>{@link IngestionFrameFactory}</code> objects.
 * </p>
 * <p>
 * <h2>Data Column Factories</h2>
 * A <code>DataColumnsSpec</code> record is a specification for a particular data column factory configuration.
 * A <code>DataColumnsSpec</code> instance produces data columns factories with the <code>{@link #newFactory()}</code>
 * method.  This method can be invoked multiple times to create multiple factories, all of the same configuration.
 * </p>
 * <p> 
 * Note that the <code>{@link IDataColumnsFactory#build(int)}</code> operation creates multiple data columns.
 * Each data column is the same size and contains simulated data of the same type.  Thus, instances of 
 * <code>DataColumnsSpec</code> specify a fixed number of data columns with a given data type.
 * </p>
 * <p>
 * <h2>Heterogeneous Data</h2> 
 * Data column factories exposing the <code>{@link IDataColumnsFactory}</code> produce <em>heterogeneous data</em>,
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
 * different data types.  To achieve this condition multiple <code>DataColumnsSpec</code> instances are used
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
 * <li><code>{@link TimestampFacSpec}</code> - specifies a timestamp datum factory configuration. </li>
 * <li><code>{@link ByteArrayFacSpec}</code> - specifies a byte array factory configuration.</li>
 * <li><code>{@link ImageFacSpec}</code> - specifies an image factory configuration.</li>
 * <li><code>{@link TensorFacSpec}</code> - specifies a tensor (multi-dimensional array) factory configuration.</li>
 * <li><code>{@link StructFacSpec}</code> - specifies a tree-structure factory configuration.</li>
 * </ul>
 * Each datum factory specification record contains creators, a command-line parser, and a method for creating 
 * its particular datum factory type.
 * </p>
 * <p>
 * <h2>Creators</h2>
 * There are creators available for <code>DataColumnsSpec</code> records configured to specific datum factory types.
 * Typically, there are multiple creators available for each datum factory type where the additional creators offer
 * the use of default parameter values.  Default parameters are taken from the JAL Tools default configuration.
 * </p>
 * <p>
 * The creator <code>{@link #parse(String...)}</code> is available for the parsing of application command-line arguments.
 * It relies heavily on the like-named operations available in all of the datum factories described above
 * (i.e., of the form <code>{@link #recFacSpec}.parse(String...)</code>).
 * </p>
 * <p>
 * There is the special creator <code>{@link #newDefaultCols()}</code> which creates a new <code>DataColumnsSpec</code>
 * record list according to the JAL Tools default configuration.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Nov 4, 2025
 *
 * @param   <FactorySpec>   specification for datum factory producing column data 
 * 
 * @param   cntCols         number of columns in the configuration (i.e., produced by <code>{@link IDataColumnsFactory#build(int)}</code>)
 * @param   strNmPref       prefix given to all column names (full name appended with index)
 * @param   enmColType      the datum type for all column column data for all columns
 * @param   recFacSpec      configuration for the data factory producing the column data
 */
public record DataColumnsSpec<FactorySpec extends Record>(
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
     * Parses and argument string to identify and create a data columns configuration (i.e., <code>DataColumnsSpec</code> record).
     * </p>
     * <p>
     * <h2>DataConfig Records</h2>
     * Data Type configuration records are internal record used for the <code>DataConfig</code> generic type.  There is
     * one type for each data type supported for column data value creation. 
     * Currently there are the following:
     * <ol>
     * <li>Scalar - <code>{@link ScalarGeneratorDeprecated.ScalarSpec}</code>,</li>
     * <li>Array - <code>{@link TensorFacSpec}</code>,</li>
     * <li>Structure - <code>{@link StructFacSpec}</code>,</li>
     * <li>Image - <code>{@link ImageFacSpec}</code>.
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
     * > java application --data DTYPE STYPE field1 field2 ... --var2 param ...
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
     * <li><code>{@link JalComplexType#ARRAY}</code> - <code>{@link TensorFacSpec}</code>.</li>
     * <li><code>{@link JalComplexType#STRUCTURE}</code> - <code>{@link StructFacSpec}</code>.</li>
     * <li><code>{@link JalComplexType#IMAGE}</code> - <code>{@link ImageFacSpec}</code>.</li>
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
     * @throws IllegalArgumentException the argument contained no data
     * @throws TypeNotPresentException  invalid enumeration constant (e.g., the 1st argument was not a <code>JalComplexType</code>)
     * @throws ConfigurationException   the argument contained the wrong number of arguments for the <code>JalComplexType</code>
     * @throws MalformedParametersException an enumeration constant within the argument set was not recognized (IMAGE)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static DataColumnsSpec parse(String...args) throws IllegalArgumentException, TypeNotPresentException, ConfigurationException, MalformedParametersException {
        if (args.length < 2)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Argument must contain at least two arguments (column count and name prefix): " 
                    + args);
        
        // Get the column count and the column name prefix
        int     cntCols = Integer.valueOf(args[0]);
        String  strNmPref = args[1];
        if (args.length < 3)
            return new DataColumnsSpec<ScalarFactorySpec>(cntCols, strNmPref, JalComplexType.SCALAR, ScalarFactorySpec.from()); // throws UnsupportedOperationException

        
        // Get the Datum Type of the column values
        JalComplexType  enmColType = JalComplexType.getConstant(args[2]); // throws TypeNotPresentException
        
        // Parse the data factory parameters if provided
        String[]    arrFacCfg = (args.length > 3) ? Arrays.copyOfRange(args, 3, args.length) : new String[0];
        
        Record      recFacSpec = switch (enmColType) {
        case SCALAR -> ScalarFactorySpec.parseArgs(arrFacCfg);    // throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException
        case BYTES -> ByteArrayFacSpec.parse(arrFacCfg);   // throws NumberFormatException
        case IMAGE -> ImageFacSpec.parse(arrFacCfg);       // throws NumberFormatException, TypeNotPresentException
        case ARRAY -> TensorFacSpec.parse(arrFacCfg);      // throws IllegalArgumentException, ConfigurationException, NumberFormatException, TypeNotPresentException, UnsupportedOperationException 
        case STRUCTURE -> StructFacSpec.parse(arrFacCfg);  // throws ConfigurationException, NumberFormatException, TypeNotPresentException, UnsupportedOperationException
        default ->
            throw new NoSuchElementException(JavaRuntime.getQualifiedMethodNameSimple() + " - Data type not supported: " + enmColType);
        };
        
        // Create and return the data columns configuration
        return new DataColumnsSpec(cntCols, strNmPref, enmColType, recFacSpec);
    }
 
    /**
     * <p>
     * Retrieves the default data columns configurations from the JAL Tools default configuration.
     * </p>
     * <p>
     * Retrieves the default column configurations contained in the <code>{@link JalToolsColumnsConfig}</code>
     * structure class list within the <code>{@link JalToolsConfig}</code> default configuration.  The parameters
     * for each column are parsed and a new <code>DataColumnsSpec</code> record is created for each column.
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
    public static List<DataColumnsSpec> newDefaultCols() throws NumberFormatException, IllegalArgumentException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException, NoSuchElementException {
        List<JalToolsColumnsConfig> lstColCfgDef =  CFG_FRM_DEF.columns;
        List<DataColumnsSpec>       lstColSpec = new ArrayList<>(lstColCfgDef.size());
        
        for (JalToolsColumnsConfig cfg : lstColCfgDef) {
            int             cntCols = cfg.count;
            String          strNmPref = cfg.name;
            JalComplexType  enmType = cfg.type;
            String          strFactory = cfg.factory;
            
            Record  recFacSpec = switch (enmType) {
            case SCALAR -> ScalarFactorySpec.parseArgs(strFactory);   // throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException
            case TIMESTAMP -> TimestampFacSpec.parse(strFactory);  // throws IllegalArgumentException, NumberFormatException, DateTimeParseException
            case BYTES -> ByteArrayFacSpec.parse(strFactory);      // throws NumberFormatException
            case IMAGE -> ImageFacSpec.parse(strFactory);          // throws NumberFormatException, TypeNotPresentException
            case ARRAY -> TensorFacSpec.parse(strFactory);         // throws IllegalArgumentException, NumberFormatException, TypeNotPresentException, ConfigurationException, UnsupportedOperationException
            case STRUCTURE -> StructFacSpec.parse(strFactory);     // throws ConfigurationException, NumberFormatException, TypeNotPresentException, UnsupportedOperationException
            default -> throw new NoSuchElementException("Unrecognized (unsupported) column type value: " + enmType);
            };
            
            @SuppressWarnings("unchecked")
            DataColumnsSpec recColSpec = new DataColumnsSpec(cntCols, strNmPref, enmType, recFacSpec);
            
            lstColSpec.add(recColSpec);
        }
        
        return lstColSpec;
    }
    
    
    public static DataColumnsSpec<ScalarFactorySpec> newScalarCols(int cntCols, String strNmPref, ScalarFactorySpec recScalarSpec) {
        DataColumnsSpec<ScalarFactorySpec>  recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.SCALAR, recScalarSpec);
        
        return recColSpec;
    }
    
    
    public static DataColumnsSpec<TimestampFacSpec>    newTimestampCols(int cntCols, String strNmPref, boolean bolRand) {
        TimestampFacSpec                   recTmsSpec = TimestampFacSpec.from(bolRand);
        DataColumnsSpec<TimestampFacSpec>  recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.TIMESTAMP, recTmsSpec);
        
        return recColSpec;
    }
    
    public static DataColumnsSpec<TimestampFacSpec>    newTimestampCols(int cntCols, String strNmPref, boolean bolRand, long lngSeed) {
        TimestampFacSpec                   recTmsSpec = TimestampFacSpec.from(bolRand, lngSeed);
        DataColumnsSpec<TimestampFacSpec>  recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.TIMESTAMP, recTmsSpec);
        
        return recColSpec;
    }
    
    public static DataColumnsSpec<TimestampFacSpec>    newTimestampCols(int cntCols, String strNmPref, Duration durPeriod, Instant insStart) {
        TimestampFacSpec                   recTmsSpec = TimestampFacSpec.from(durPeriod, insStart);
        DataColumnsSpec<TimestampFacSpec>  recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.TIMESTAMP, recTmsSpec);
        
        return recColSpec;
    }
    
    
    public static DataColumnsSpec<ByteArrayFacSpec>    newByteArrayCols(int cntCols, String strNmPref) {
        ByteArrayFacSpec               recBytSpec = ByteArrayFacSpec.from();
        DataColumnsSpec<ByteArrayFacSpec>  recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.BYTES, recBytSpec);
        
        return recColSpec;
    }
    
    public static DataColumnsSpec<ByteArrayFacSpec>    newByteArrayCols(String strNmPref, int cntCols, int cntBytes) {
        ByteArrayFacSpec                   recArrSpec = ByteArrayFacSpec.from(cntBytes);
        DataColumnsSpec<ByteArrayFacSpec>  recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.BYTES, recArrSpec);
        
        return recColSpec;
    }
    
    
    public static DataColumnsSpec<ImageFacSpec>    newImageCols(int cntCols, String strNmPref) {
        ImageFacSpec                   recImgSpec = ImageFacSpec.from();
        DataColumnsSpec<ImageFacSpec>  recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.IMAGE, recImgSpec);
        
        return recColSpec;
    }
    
    public static DataColumnsSpec<ImageFacSpec>    newImageCols(int cntCols, String strNmPref, int size) {
        ImageFacSpec                   recImgSpec = ImageFacSpec.from(size);
        DataColumnsSpec<ImageFacSpec>  recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.IMAGE, recImgSpec);
        
        return recColSpec;
    }
    
    public static DataColumnsSpec<ImageFacSpec>    newImageCols(String strNmPref, int cntCols, int size, BufferedImage.Format enmFmt) {
        ImageFacSpec                   recImgSpec = ImageFacSpec.from(size, enmFmt);
        DataColumnsSpec<ImageFacSpec>  recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.IMAGE, recImgSpec);
        
        return recColSpec;
    }
    
    public static DataColumnsSpec<ImageFacSpec>    newImageCols(int cntCols, String strNmPref, int size, BufferedImage.Format enmFmt, String strImgPref) {
        ImageFacSpec                   recImgSpec = ImageFacSpec.from(size, enmFmt, strImgPref);
        DataColumnsSpec<ImageFacSpec>  recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.IMAGE, recImgSpec);
        
        return recColSpec;
    }
    
    
    public static DataColumnsSpec<TensorFacSpec>   newTensorCols(String strNmPref, int cntCols, int[] shape) {
        TensorFacSpec                  recTenSpec = TensorFacSpec.from(shape);
        DataColumnsSpec<TensorFacSpec> recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.ARRAY, recTenSpec);
        
        return recColSpec;
    }
    
    public static DataColumnsSpec<TensorFacSpec>   newTensorCols(String strNmPref, int cntCols, int[] shape, ScalarFactorySpec recScalarSpec) {
        TensorFacSpec                  recTenSpec = TensorFacSpec.from(shape, recScalarSpec);
        DataColumnsSpec<TensorFacSpec> recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.ARRAY, recTenSpec);
        
        return recColSpec;
    }
    
    
    public static DataColumnsSpec<StructFacSpec>   newStructCols(String strNmPref, int cntCols, int depth, int fanout, boolean bolUniqNms) {
        StructFacSpec                  recStrSpec = StructFacSpec.from(depth, fanout, bolUniqNms);
        DataColumnsSpec<StructFacSpec> recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.STRUCTURE, recStrSpec);
        
        return recColSpec;
    }
    
    public static DataColumnsSpec<StructFacSpec>   newStructCols(String strNmPref, int cntCols, int depth, int fanout, ScalarFactorySpec recScalarSpec) {
        StructFacSpec                  recStrSpec = StructFacSpec.from(depth, fanout, recScalarSpec);
        DataColumnsSpec<StructFacSpec> recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.STRUCTURE, recStrSpec);
        
        return recColSpec;
    }
    
    public static DataColumnsSpec<StructFacSpec>   newStructCols(String strNmPref, int cntCols, int depth, int fanout, boolean bolUniqNms, ScalarFactorySpec recScalarSpec) {
        StructFacSpec                  recStrSpec = StructFacSpec.from(depth, fanout, bolUniqNms, recScalarSpec);
        DataColumnsSpec<StructFacSpec> recColSpec = new DataColumnsSpec<>(cntCols, strNmPref, JalComplexType.STRUCTURE, recStrSpec);
        
        return recColSpec;
    }
    
    
    //
    // Internal Types
    //

    /** 
     * <p>
     * Record containing <code>TimestampFactory</code> configuration parameters.
     * </p>
     * <p>
     * There are 2 possible configurations for timestamp factory configuration: 1) a random timestamp generation
     * factory, and 2) an incremental timestamp generation factory.  Random factories create random-valued
     * timestamps in the current epoch.  Incremental timestamp factories create timestamps with a given start
     * time and sampling period.  The type of timestamp factory is given by the <code>{@link #bolRand()}</code> field value.
     * </p>
     * <p>
     * Timestamp factories are created with method <code>{@link #newFactory()}</code>.
     * Only two field values are used for <code>TimestampFactory</code> instance creation, this depends upon the
     * value of <code>{@link #bolRand()}</code>.
     * <ul>
     * <li><code>{@link #bolRand()} = true</code> &rarr; <code>{@link TimestampFactory#from(boolean, long)}</code></li>.
     * <li><code>{@link #bolRand()} = true</code> &rarr; <code>{@link TimestampFactory#from(Duration, Instant)}</code></li>.
     * </ul>
     * The above conditions are consistent with the constructors of the <code>{@link TimestampFactory}</code> class.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Random timestamp factories are more expensive than incremental timestamp factories, requiring a random number
     * generator for timestamp production.
     * </li>
     * <li>
     * Random timestamp factories produce timestamps using randomly generated <code>long</code> values.  The long
     * value is then used as a nanosecond offset past the given epoch <code>{@link Instant#EPOCH}</code>.
     * </li>
     * <li>
     * If a random timestamp factory configuration creator is used with the 'random' parameter set to <code>false</code>
     * the result timestamp factory produces a sequence of timestamps with contant value <code>{@link Instant#EPOCH}</code>.
     * </li>
     * </ul>
     * </p> 
     * 
     * @param bolRand   random generation timestamp generation enable/disable flag
     * @param lngSeed   seed value for the random number generator (use 0 for random seed)
     * @param durPeriod the difference (period) between generated timestamp values in the incremental sequence 
     * @param insStart  the first timestamp value in the incremental sequence
     * 
     * @see TimestampFactory
     */
    public static record TimestampFacSpec(boolean bolRand, long lngSeed, Duration durPeriod, Instant insStart) {
        
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates and returns a new <code>TimestampFacSpec</code> configuration for an incremental <code>TimestampFactory</code>
         * with all default arguments.
         * </p>
         * <p>
         * All <code>{@link TimestampFactory}</code> instances created from the returned produce sequences of timestamps
         * from the given <code>{@link #insStart}</code> value.  The following timestamp values are then separated by the
         * time interval <code>{@link #durPeriod}</code>.
         * </p>
         * <p>
         * <h2>Default Parameters</h2>
         * This creator uses default parameters values from the JAL Tools default configuration.
         * The following values are used:  
         * <ul>
         * <li><code>{@link #durPeriod} = {@link DataColumnsSpec#DUR_TMS_INCR_PERIOD_DEF}</code>.</li>
         * <li><code>{@link #insStart} = {@link DataColumnsSpec#INS_TMS_INCR_START_DEF}</code>.</li>
         * </ul>
         * </p>
         *  
         * @return  a new random <code>TimestampFacSpec</code> configuration populated with all default arguments 
         */
        public static TimestampFacSpec from() {
            return TimestampFacSpec.from(DUR_TMS_INCR_PERIOD_DEF);
        }
        
        /**
         * <p>
         * Creates and returns a new <code>TimestampFacSpec</code> configuration for an incremental <code>TimestampFactory</code>.
         * </p>
         * <p>
         * All <code>{@link TimestampFactory}</code> instances created from the returned produce sequences of timestamps
         * from the given <code>{@link #insStart}</code> value.  The following timestamp values are then separated by the
         * time interval <code>{@link #durPeriod}</code>.
         * </p>
         * <p>
         * <h2>Default Parameters</h2>
         * This creator uses default parameters values from the JAL Tools default configuration.
         * The following values are used:  
         * <ul>
         * <li><code>{@link #insStart} = {@link DataColumnsSpec#INS_TMS_INCR_START_DEF}</code>.</li>
         * </ul>
         * </p>
         *  
         * @param durPeriod the interval of time between generated timestamps (i.e., the sampling period)
         * 
         * @return  a new random <code>TimestampFacSpec</code> configuration populated with the given arguments 
         */
        public static TimestampFacSpec from(Duration durPeriod) {
            return TimestampFacSpec.from(durPeriod, INS_TMS_INCR_START_DEF);
        }
        
        /**
         * <p>
         * Creates and returns a new <code>TimestampFacSpec</code> configuration for an incremental <code>TimestampFactory</code>.
         * </p>
         * <p>
         * All <code>{@link TimestampFactory}</code> instances created from the returned produce sequences of timestamps
         * from the given <code>{@link #insStart}</code> value.  The following timestamp values are then separated by the
         * time interval <code>{@link #durPeriod}</code>.
         * </p>
         *  
         * @param durPeriod the interval of time between generated timestamps (i.e., the sampling period)
         * @param insStart  the start time of the timestamp sequence (i.e., the 1st timestamp value)
         * 
         * @return  a new random <code>TimestampFacSpec</code> configuration populated with the given arguments 
         */
        public static TimestampFacSpec from(Duration durPeriod, Instant insStart) {
            return new TimestampFacSpec(false, 0, durPeriod, insStart);
        }
        
        
        /**
         * <p>
         * Creates and returns a new <code>TimestampFacSpec</code> configuration for a random <code>TimestampFactory</code>.
         * </p>
         * <p>
         * When the boolean argument is <code>true</code> the returned configuration is for a random timestamp factory
         * with the given seed value.  If the seed value is '0' then the seed is generated 'randomly' and each new
         * factory starts with a different seed value.  Setting a nonzero seed value creates timestamp factories that
         * all produce the same 'random' sequence.
         * </p>
         * <p>
         * <h2>Default Parameters</h2>
         * This creator uses default parameters values from the JAL Tools default configuration.
         * The following values are used:  
         * <ul>
         * <li><code>{@link #lngSeed} = {@link DataColumnsSpec#LNG_TMS_RND_SEED_DEF}</code>.</li>
         * </ul>
         * </p>
         * <p>
         * <h2>NOTES:</h2>
         * <ul>
         * <li>
         * Random timestamp factories are more expensive than incremental timestamp factories, requiring a random number
         * generator for timestamp production.
         * </li>
         * <li>
         * Random timestamp factories produce timestamps using randomly generated <code>long</code> values.  The long
         * value is then used as a nanosecond offset past the given epoch <code>{@link Instant#EPOCH}</code>.
         * </li>
         * <li>
         * If a random timestamp factory configuration creator is used with the 'random' parameter set to <code>false</code>
         * the result timestamp factory produces a sequence of timestamps with contant value <code>{@link Instant#EPOCH}</code>.
         * </li>
         * </ul>
         * </p> 
         * 
         * @param bolRand   random generation timestamp generation enable/disable flag
         * 
         * @return  a new random <code>TimestampFacSpec</code> configuration populated with the given arguments 
         */
        public static TimestampFacSpec from(boolean bolRand) {
            return TimestampFacSpec.from(bolRand, LNG_TMS_RND_SEED_DEF);
        }
        
        /**
         * <p>
         * Creates and returns a new <code>TimestampFacSpec</code> configuration for a random <code>TimestampFactory</code>.
         * </p>
         * <p>
         * When the boolean argument is <code>true</code> the returned configuration is for a random timestamp factory
         * with the given seed value.  If the seed value is '0' then the seed is generated 'randomly' and each new
         * factory starts with a different seed value.  Setting a nonzero seed value creates timestamp factories that
         * all produce the same 'random' sequence.
         * </p>
         * <p>
         * <h2>NOTES:</h2>
         * <ul>
         * <li>
         * Random timestamp factories are more expensive than incremental timestamp factories, requiring a random number
         * generator for timestamp production.
         * </li>
         * <li>
         * Random timestamp factories produce timestamps using randomly generated <code>long</code> values.  The long
         * value is then used as a nanosecond offset past the given epoch <code>{@link Instant#EPOCH}</code>.
         * </li>
         * <li>
         * If a random timestamp factory configuration creator is used with the 'random' parameter set to <code>false</code>
         * the result timestamp factory produces a sequence of timestamps with contant value <code>{@link Instant#EPOCH}</code>.
         * </li>
         * </ul>
         * </p> 
         * 
         * @param bolRand   random generation timestamp generation enable/disable flag
         * @param lngSeed   seed value for the random number generator (use 0 for random seed)
         * 
         * @return  a new random <code>TimestampFacSpec</code> configuration populated with the given arguments 
         */
        public static TimestampFacSpec from(boolean bolRand, long lngSeed) {
            
            return new TimestampFacSpec(bolRand, lngSeed, Duration.ZERO, Instant.EPOCH);
        }
        
        /**
         * <p>
         * Parses the argument collection for the field values of the returned <code>TimestampFacSpec</code> instance.
         * </p>
         * <p>
         * The argument collection is assumed to originate from an application command-line argument collection.
         * There are 2 possibilities for a <code>{@link TimestampFactory}</code>: 1) a random timestamp factory and,
         * 2) and incremental timestamp factory.  In the first case there is 1 parameter, the random number 'seed'
         * value.  In the second case there are two parameters, the 'period' and the 'start' time instant.
         * </p>
         * <h2>Format</h2>
         * The format of the arguments is either of the following 2 possibilities:
         * <ol>
         * <pre>
         * <li>  > false [seed]</li>
         *    or
         * <li>  > true [period [start]]</li>
         * </pre>
         * </ol>
         * where
         * <ul>
         * <li>'seed' = seed value for the random number generation, where 0 indicates random seed (long value),</li>
         * <li>'period' = sampling period of an incremental timestamp factory (ISO-8605 duration format),</li>
         * <li>'start' = start time for an incremental timestamp factory (ISO-8605 date/time format).</li>
         * </ul>
         * </p>
         * <p>
         * <h2>Optional Arguments</h2>
         * The brackets indicate optional values in the argument collection.  If not present they are populated with
         * the default values of the JAL Tools default configuration.
         * <ul>
         * <li>'seed' = <code>{@link DataColumnsSpec#LNG_TMS_RND_SEED_DEF}</code>.</li>
         * <li>'period' = <code>{@link DataColumnsSpec#DUR_TMS_INCR_PERIOD_DEF}</code>.</li>
         * <li>'start' = <code>{@link DataColumnsSpec#INS_TMS_INCR_START_DEF}</code>.</li>
         * </ul>
         * </p>
         * 
         * @param args  argument collection to be parsed, format as described above
         * 
         * @return  a new <code>TimestampFacSpec</code> record populated with the parsed argument values
         * 
         * @throws IllegalArgumentException the argument collection was empty (must have at least 1 element - bolRand)
         * @throws NumberFormatException    the 'seed' value could not be parsed
         * @throws DateTimeParseException   the 'period' or 'instant' value could not be parsed
         */
        public static TimestampFacSpec parse(String...args) throws IllegalArgumentException, NumberFormatException, DateTimeParseException {
            
            if (args.length < 1)
                throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Argument must contain at least one argument: " + args);

            // Get the random generation enable/disable flag
            boolean bolRand = Boolean.valueOf(args[0]);
            
            // Populate record according to random enable/disable flag
            if (bolRand) {  
                // Random timestamp factory
                if (args.length < 2)
                    return TimestampFacSpec.from(bolRand);
                
                long    lngSeed = Long.valueOf(args[1]);    // throws NumberFormatException
                return TimestampFacSpec.from(bolRand, lngSeed);
                
                
            } else {        
                // Incremental timestamp factory
                if (args.length < 2)
                    return TimestampFacSpec.from();

                Duration    durPeriod = Duration.parse(args[2]);    // throws DateTimeParseException
                if (args.length < 3) 
                    return TimestampFacSpec.from(durPeriod);
                
                Instant     insStart = Instant.parse(args[3]);      // throws DateTimeParseException
                return TimestampFacSpec.from(durPeriod, insStart);
            }
        }
        
        //
        // Operations
        //
        
        /**
         * <p>
         * Create and return a new <code>TimestampFactory</code> instance according to this configuration.
         * </p>
         * <p>
         * Only two field values are used for <code>TimestampFactory</code> instance creation, this depends upon the
         * value of <code>{@link #bolRand()}</code>.
         * <ul>
         * <li><code>{@link #bolRand()} = true</code> &rarr; <code>{@link TimestampFactory#from(boolean, long)}</code></li>.
         * <li><code>{@link #bolRand()} = true</code> &rarr; <code>{@link TimestampFactory#from(Duration, Instant)}</code></li>.
         * </ul>
         * </p>
         * 
         * @return  a new <code>TimestampFactory</code> instance ready for simulated timestamp value creation
         */
        public TimestampFactory newFactory() {
            
            // Create and return factory 
            if (this.bolRand)
                return TimestampFactory.from(this.bolRand, this.lngSeed);
            else
                return TimestampFactory.from(this.durPeriod, this.insStart);
        }
    };
    
    
    /**
     * <p> 
     * Record containing <code>ByteArrayFactory</code> configuration parameters.
     * </p>
     * <p>
     * The <code>{@link ByteArrayFactory}</code> class has a simple configuration requiring only a single
     * parameter the array 'size'.  This parameter is the number of bytes contained in each byte array
     * produced.  Each byte array contains random values, the byte values of the heap at the time of allocation.
     * </p>  
     * 
     * @param cntBytes  the number of bytes in each byte array
     * 
     * @see ByteArrayFactory
     */
    public static record ByteArrayFacSpec(int cntByes) {
        
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates and returns a new <code>ByteArrayFacSpec</code> configuration for a <code>ByteArrayFactory</code>
         * with default parameters.
         * </p>
         * <p>
         * The <code>ByteArrayFactory</code> instances produced from this configuration 
         * (i.e., see <code>{@link #newFactory()}</code> all produce byte arrays with size <code>{@link #cntByes}</code>.
         * </p> 
         * <p>
         * <h2>Default Parameters</h2>
         * This creator uses default parameters values from the JAL Tools default configuration.
         * The following values are used:  
         * <ul>
         * <li><code>{@link #cntByes} = {@link DataColumnsSpec#INT_BYTES_SIZE_DEF}</code>.</li>
         * </ul>
         * </p>
         * 
         * @param cntBytes  the number of bytes (array size) of all byte arrays produced by factory configuration
         * 
         * @return  a new <code>ByteArrayFacSpec</code> populated with default argument
         */
        public static ByteArrayFacSpec from() {
            return ByteArrayFacSpec.from(INT_BYTES_SIZE_DEF);
        }
        
        /**
         * <p>
         * Creates and returns a new <code>ByteArrayFacSpec</code> configuration for a <code>ByteArrayFactory</code>.
         * </p>
         * <p>
         * The <code>ByteArrayFactory</code> instances produced from this configuration 
         * (i.e., see <code>{@link #newFactory()}</code> all produce byte arrays with size <code>{@link #cntByes}</code>.
         * </p> 
         * 
         * @param cntBytes  the number of bytes (array size) of all byte arrays produced by factory configuration
         * 
         * @return  a new <code>ByteArrayFacSpec</code> populated with the given argument
         */
        public static ByteArrayFacSpec from(int cntBytes) {
            return new ByteArrayFacSpec(cntBytes);
        }
        
        /**
         * <p>
         * Parses the argument collection for the field values of the returned <code>ByteArrayFacSpec</code> instance.
         * </p>
         * <p>
         * The argument collection is assumed to originate from an application command-line argument collection.
         * The <code>{@link ByteArrayFactory}</code> class is quite simple requiring only a single configuration
         * parameter, the size of the arrays produced. 
         * <p>
         * <h2>Format</h2>
         * The format of the argument collection is assumed to be
         * <pre>
         * > [size]
         * </pre>
         * where
         * <ul>
         * <li>'size' = number of bytes in each byte array.</li>
         * </ul>
         * </p>
         * <p>
         * <h2>Optional Arguments</h2>
         * The brackets indicate optional arguments.  If not present the argument is populated with the default
         * values within the JAL Tools default configuration <code>{@link DataColumnsSpec#INT_BYTES_SIZE_DEF}</code>.
         * </p>
         * 
         * @param args  argument collection to be parsed, format as described above
         * 
         * @return  a new <code>ByteArrayFacSpec</code> record populated with the parsed argument values
         * 
         * @throws NumberFormatException    the 'seed' value could not be parsed
         */
        public static ByteArrayFacSpec parse(String...args) throws NumberFormatException {
        
            if (args.length < 1)
                return ByteArrayFacSpec.from();
            
            int cntBytes = Integer.valueOf(args[0]);    // throws NumberFormatException
            return ByteArrayFacSpec.from(cntBytes);
        }
        
        //
        // Operations
        //
        
        /**
         * <p>
         * Creates and returns a new <code>ByteArrayFactory</code> according to this configuration.
         * </p>
         * 
         * @return  a new <code>ByteArrayFactory</code> instance ready for simulated byte array creation
         */
        public ByteArrayFactory newFactory() {
            return ByteArrayFactory.from(this.cntByes);
        }
    };
    
    
    /**
     * <p> 
     * Record containing <code>ImageFactory</code> configuration parameters.
     * </p>
     * <p>
     * The <code>ImageFactory</code> class requires 3 parameters: 1) the image 'size', the image 'format', and
     * 3) the image name 'prefix'.
     * <ul>
     * <li>'size' is the number of bytes required of each image and is contained in field <code>{@link #size}</code>.</li>
     * <li>'format' is the file format of the image and contained in field <code>{@link #enmFmt}</code>.</li>
     * <li>'prefix' is the name prefix given to each image (full name suffixed by index) and contained in field <code>{@link #strPref()}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   size    image size (in bytes)
     * @param   enmFmt  image format
     * @param   strPref image name prefix (full image name is appended with index)
     */
    public static record ImageFacSpec(int size, BufferedImage.Format enmFmt, String strPref) {
        
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates and returns a new <code>ImageFacSpec</code> configuration for a <code>ImageFactory</code>
         * with all default parameters.
         * </p>
         * <h2>Default Parameters</h2>
         * This creator uses default parameters values from the JAL Tools default configuration.
         * The following values are used:  
         * <ul>
         * <li><code>{@link #size} = {@link DataColumnsSpec#INT_IMG_SIZE_DEF}</code>.</li>
         * <li><code>{@link #enmFmt} = {@link DataColumnsSpec#ENM_IMG_FMT_DEF}</code>.</li>
         * <li><code>{@link #strPref} = {@link DataColumnsSpec#STR_IMG_PREFIX_DEF}</code>.</li>
         * </ul>
         * </p>
         * 
         * @return  a new <code>ImageFacSpec</code> configuration populated with all default parameters
         */
        public static ImageFacSpec from() {
            return ImageFacSpec.from(INT_IMG_SIZE_DEF);
        }
        
        /**
         * <p>
         * Creates and returns a new <code>ImageFacSpec</code> configuration for a <code>ImageFactory</code>.
         * </p>
         * <h2>Default Parameters</h2>
         * This creator uses default parameters values from the JAL Tools default configuration.
         * The following values are used:  
         * <ul>
         * <li><code>{@link #enmFmt} = {@link DataColumnsSpec#ENM_IMG_FMT_DEF}</code>.</li>
         * <li><code>{@link #strPref} = {@link DataColumnsSpec#STR_IMG_PREFIX_DEF}</code>.</li>
         * </ul>
         * </p>
         * 
         * @param   size    image size (in bytes)
         * 
         * @return  a new <code>ImageFacSpec</code> configuration populated with the given arguments
         */
        public static ImageFacSpec from(int size) {
            return ImageFacSpec.from(size, ENM_IMG_FMT_DEF);
        }
        
        /**
         * <p>
         * Creates and returns a new <code>ImageFacSpec</code> configuration for a <code>ImageFactory</code>.
         * </p>
         * <h2>Default Parameters</h2>
         * This creator uses default parameters values from the JAL Tools default configuration.
         * The following values are used:  
         * <ul>
         * <li><code>{@link #strPref} = {@link DataColumnsSpec#STR_IMG_PREFIX_DEF}</code>.</li>
         * </ul>
         * </p>
         * 
         * @param   size    image size (in bytes)
         * @param   enmFmt  image format
         * 
         * @return  a new <code>ImageFacSpec</code> configuration populated with the given arguments
         */
        public static ImageFacSpec from(int size, BufferedImage.Format enmFmt) {
            return ImageFacSpec.from(size, enmFmt, STR_IMG_PREFIX_DEF);
        }
        
        /**
         * <p>
         * Creates and returns a new <code>ImageFacSpec</code> configuration for a <code>ImageFactory</code>.
         * </p>
         * <p>
         * This creator is equivalent to the canonical constructor containing all required field values.
         * </p>
         * 
         * @param   size    image size (in bytes)
         * @param   enmFmt  image format
         * @param   strPref image name prefix (full image name is appended with index)
         * 
         * @return  a new <code>ImageFacSpec</code> configuration populated with the given arguments
         */
        public static ImageFacSpec from(int size, BufferedImage.Format enmFmt, String strPref) {
            return new ImageFacSpec(size, enmFmt, strPref);
        }
        
        /**
         * <p>
         * Parses the argument collection for the field values of the returned <code>ImageFacSpec</code> instance.
         * </p>
         * <p>
         * The argument collection is assumed to originate from an application command-line argument collection.
         * The <code>{@link ImageFactory}</code> class has three parameters: 1) image 'size', 2) image 'format', and
         * 3) image name 'prefix'.
         * </p>
         * <p>
         * <h2>Format</h2>
         * The format of the arguments is the following :
         * <ul>
         * <pre>
         * <li>  > [size [format [prefix]]]</li>
         * </pre>
         * </ul>
         * where
         * <ul>
         * <li>'size' = size of images produced (in bytes) (int value),</li>
         * <li>'format' = image format (<code>{@link BufferedImage#Format})</code>,</li>
         * <li>'prefix' = name prefix given to all images produced (full name appended by index).</li>
         * </ul>
         * </p>
         * <p>
         * <h2>Optional Arguments</h2>
         * The brackets indicate optional values in the argument collection.  If not present they are populated with
         * the default values of the JAL Tools default configuration.
         * <ul>
         * <li>'size' = <code>{@link DataColumnsSpec#INT_IMG_SIZE_DEF}</code>.</li>
         * <li>'format' = <code>{@link DataColumnsSpec#ENM_IMG_FMT_DEF}</code>.</li>
         * <li>'prefix' = <code>{@link DataColumnsSpec#STR_IMG_PREFIX_DEF}</code>.</li>
         * </ul>
         * </p>
         * <p>
         * Note that optional parameters are ordered and nested.  Due to the nature of string parsing the 
         * ordering must be respected.  For example, to include the 'prefix' parameter all other parameters
         * must be supplied.
         * </p>  
         * 
         * @param args  argument collection to be parsed, format as described above
         * 
         * @return  a new <code>TimestampFacSpec</code> record populated with the parsed argument values
         * 
         * @throws NumberFormatException    invalid numeric format for the 'size' parameter 
         * @throws TypeNotPresentException  the 'format' was unrecognized (i.e., not a {@link BufferedImage#Format} constant) 
         */
        public static ImageFacSpec parse(String...args) throws NumberFormatException, TypeNotPresentException {
            
            if (args.length < 1)
                return ImageFacSpec.from();
            
            int     intSize = Integer.valueOf(args[0]);     // throws NumberFormatException
            if (args.length < 2)
                return ImageFacSpec.from(intSize);
            
            BufferedImage.Format    enmFmt = BufferedImage.Format.getConstant(args[1]); // throws TypeNotPresentException
            if (args.length < 3) 
                return ImageFacSpec.from(intSize, enmFmt);
            
            String  strPref = args[2];
            return ImageFacSpec.from(intSize, enmFmt, strPref);
        }
        
        //
        // Operations
        //
        
        /**
         * <p>
         * Creates and returns a new <code>ImageFactory</code> according to this configuration.
         * </p>
         * 
         * @return  a new <code>ImageFactory</code> instance ready for simulated image creation
         */
        public ImageFactory newFactory() {
            return ImageFactory.from(this.size, this.enmFmt, this.strPref);
        }
    };

    
    /** 
     * <p>
     * Record containing <code>TensorFactory</code> configuration parameters 
     * </p>
     * The <code>{@link TensorFactory}</code> class requires 2 parameters: 1) the 'shape' of the tensors
     * produced, and 2) the scalar factory producing the elements of the tensors.
     * <ul>
     * <li>'shape' = an int[] array containing the axes sizes { n1, n2, n3, ...}.</li>
     * <li>'recScalarSpec = an <code>{@link ScalarFactorySpec}</code> record specifying the scalar factory configuration.</li>
     * </ul>
     * </p>
     * 
     * @param   shape           the shape of the tensors produced 
     * @param   recScalarSpec   the scalar factory configuration for tensor element values
     */
    public static record TensorFacSpec(int[] shape, ScalarFactorySpec recScalarSpec) {
        
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates and returns a new <code>TensorFacSpec</code> record configured according to the given arguments.
         * </p>
         * <p>
         * This creator uses default values taken from the JAL Tools default configuration for fields not contained 
         * in the arguments.
         * <ul>
         * <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from()}.</code></li>
         * </ul>
         * </p>
         * 
         * @param   shape           the shape of the tensors produced 
         * @param   recScalarSpec   the scalar factory configuration for tensor element values
         * 
         * @return  a new <code>TensorFacSpec</code> instance populated with the above arguments
         */
        public static TensorFacSpec    from(int[] shape) {
            return TensorFacSpec.from(shape, ScalarFactorySpec.from());
        }
        
        /**
         * <p>
         * Creates and returns a new <code>TensorFacSpec</code> record configured according to the given arguments.
         * </p>
         * <p>
         * This creator is equivalent to the canonical constructor requiring all field values of the record.
         * </p>
         * 
         * @param   shape           the shape of the tensors produced 
         * @param   recScalarSpec   the scalar factory configuration for tensor element values
         * 
         * @return  a new <code>TensorFacSpec</code> instance populated with the above arguments
         */
        public static TensorFacSpec    from(int[] shape, ScalarFactorySpec recScalarSpec) {
            return new TensorFacSpec(shape, recScalarSpec);
        }
        
        /**
         * <p>
         * Parses the argument collection for the field values of the returned <code>TensorFacSpec</code> instance.
         * </p>
         * <p>
         * The argument collection is assumed to originate from an application command-line argument collection.
         * The <code>{@link TensorFactory}</code> class requires a 'shape' parameter and a 
         * <code>{@link ScalarFactory}</code> to create its element values.
         * </p>
         * <h2>Scalar Factory</h2>
         * The <code>{@link TensorFactory}</code> class requires a <code>{@link ScalarFactory}</code> instance.
         * The scalar factory is used to generate the field values
         * of all tree structure fields produced by the structure factory described by this configuration.
         * Note the configuration for the <code>{@link ScalarFactorySpec}</code> field <code>{@link #recScalarSpec}</code>
         * is potentially included in the argument collection; it not a default scalar factory is supplied.
         * </p>  
         * <p>
         * <h2>Format</h2>
         * The format of the argument collection is assumed to be
         * <pre>
         * > n1 [n2 [n3 ...]]...] [recScalarSpec]
         * </pre>
         * where
         * <ul>
         * <li>'n1' = size of the 1st axis.</li>
         * <li>'n2' = size of the 1st axis.</li>
         * <li>'n3' = size of the 1st axis.</li>
         * <li>'...' = sizes of the remaining axes.</li>
         * <li>'recScalarSpec' = configuration record for the scalar factory producing tensor element values.</li>
         * </ul>
         * Note that the tensor shape is determined by the values
         * 'n1, 'n2', 'n3', ..., etc.  These values are then used to pack the shape array 
         * <code>{@link #shape}</code> = { n1, n2, n3, ... }.
         * Thus, at least one argument element is required to specify a tensor shape, otherwise an exception is thrown.
         * </p>
         * <h2>Optional Arguments</h2>
         * The brackets indicate optional arguments.  The arguments are interpreted as follows:  
         * <ul>
         * <li>If the 'recScalarSpec' value is not present the argument is populated with the default scalar factory
         *     <code>{@link ScalarFactorySpec#from()}</code>.
         * </li>
         * <li>The shape of the tensor is determined by the number an values within the set {n1, n2, n3, ...}.  A tensor
         *     must have at least one axis, thus, the value 'n1' is required and are optional, 
         *     indicating higher-dimensional tensors.
         * </li>
         * </ul>
         * </p>
         * 
         * @param args  argument collection to be parsed, format as described above
         * 
         * @return  a new <code>TensorFacSpec</code> record populated with the parsed argument values
         * 
         * @throws IllegalArgumentException         the argument collection must contain at least one element
         * @throws ConfigurationException           the tensor shape was missing or invalid
         * @throws NumberFormatException            an axis size could not be parsed, invalid integer format string
         * @throws TypeNotPresentException          unknown <code>JalScalarType</code> enumeration constant
         * @throws UnsupportedOperationException    unable to create <code>{@link #increment}</code> field for numeric value type  
         */
        public static TensorFacSpec    parse(String...args) throws IllegalArgumentException, ConfigurationException, NumberFormatException, TypeNotPresentException, UnsupportedOperationException {
            
            // Check arguments
            if (args.length < 1)
                throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Arguments must have at least 1 element: " + args);
            
            // Parse through the argument values extracting the shape
            //  Parsing continues until a non-integer value is found (the beginning of the scalar factory configuration)
            List<Integer>   lstAxes = new LinkedList<>();
            int             indAxes = 0;
            for (String strArg : args) {
                
                try {
                    Integer intAxis = Integer.valueOf(strArg);
                    lstAxes.add(intAxis);
                    indAxes++;
                    
                } catch (NumberFormatException e) {
                    break;
                }
            }
            
            // Check that at least one axis size was provided and sizes are positive
            if (indAxes == 0)
                throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() + " - Arguments did not contain shape description: " + args);
            if (!lstAxes.stream().allMatch(i -> (i > 0)))
                throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() + " - Shape specification contained non-positive axis size: " + lstAxes);
            
            // Converted the list of axes sizes to an int array
            int[]       shape = lstAxes.stream().mapToInt(i -> i).toArray();

            // Extract the remaining arguments from the original argument set
            //  These are the configuration parameters for the scalar factory
            String[]            arrScalCfg = Arrays.copyOfRange(args, indAxes, args.length);
            ScalarFactorySpec   recScalarSpec  = ScalarFactorySpec.parseArgs(arrScalCfg); // throws TypeNotPresentException, NumericFormatException, UnsupportedOperationException
            
            return TensorFacSpec.from(shape, recScalarSpec);
        }
        
        //
        // Operations
        //
       
        /**
         * <p>
         * Creates and returns a new <code>TensorFactory</code> according to this configuration.
         * </p>
         * 
         * @return  a new <code>TensorFactory</code> instance ready for simulated byte array creation
         */
        public TensorFactory    newFactory() {
            return TensorFactory.from(this.shape, this.recScalarSpec);
        }
        
    };
    
    
    /** 
     * <p>
     * Record containing <code>StructureFactory</code> configuration parameters 
     * </p>
     * <p>
     * The fields of this configuration record contain the parameters of the <code>StructureFactory</code> class.
     * A <code>{@link StructureFactory}</code> class contains 4 parameters: 
     * <ol>
     * <li>'depth' = the node depth of the tree structure (how many tree nodes until the terminal nodes are reached).</li>
     * <li>'fanout' = the number of sub-nodes for each tree structure node (until terminal nodes are reached).</li>
     * <li>'unique names' = enable/disable unique field names for each tree structure produced by factory.</li>
     * <li>'recScalarSpec' = the <code>{@link ScalarFactorySpec}</code> configuration scalar factory producing field values.</li>
     * </ol>
     * </p>
     * <p>
     * The fields of this configuration record contain the parameters of the <code>StructureFactory</code> class.
     * </p>  
     * 
     * @param depth         node depth of tree structures produced
     * @param fanout        node fan-out of tree structure
     * @param bolUniqNms    enable/disable creation of unique field names by structure factory 
     * @param recScalarSpec configuration for scalar factory producing structure field values
     */
    public static record StructFacSpec(int depth, int fanout, boolean bolUniqNms, ScalarFactorySpec recScalarSpec) {
        
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates and returns a new <code>TensorFacSpec</code> record configured with the given argument values.
         * </p>
         * <p>
         * This creator is uses the default values taken from the JAL Tools
         * default configuration and listed below:
         * <ul>
         * <li><code>{@link #bolUniqNms()} = {@link DataColumnsSpec#BOL_STRUCT_UNIQ_FLD_NM_DEF}</code>.
         * <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from()}</code>.
         * </ul>
         * </p>
         * 
         * @param depth         node depth of tree structures produced
         * @param fanout        node fan-out of tree structure
         * 
         * @return  a new <code>TensorFacSpec</code> instance populated with the above argument values
         */
        public static StructFacSpec    from(int depth, int fanout) {
            return StructFacSpec.from(depth, fanout, BOL_STRUCT_UNIQ_FLD_NM_DEF, ScalarFactorySpec.from());
        }
        
        /**
         * <p>
         * Creates and returns a new <code>TensorFacSpec</code> record configured with the given argument values.
         * </p>
         * <p>
         * This creator is uses the default values taken from the JAL Tools
         * default configuration and listed below:
         * <ul>
         * <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from()}</code>.
         * </ul>
         * </p>
         * 
         * @param depth         node depth of tree structures produced
         * @param fanout        node fan-out of tree structure
         * @param bolUniqNms    enable/disable creation of unique field names by structure factory 
         * 
         * @return  a new <code>TensorFacSpec</code> instance populated with the above argument values
         */
        public static StructFacSpec    from(int depth, int fanout, boolean bolUniqNms) {
            return StructFacSpec.from(depth, fanout, bolUniqNms, ScalarFactorySpec.from());
        }
        
        /**
         * <p>
         * Creates and returns a new <code>TensorFacSpec</code> record configured with the given argument values.
         * </p>
         * <p>
         * This creator is uses the default values taken from the JAL Tools
         * default configuration and listed below:
         * <ul>
         * <li><code>{@link #bolUniqNms()} = {@link DataColumnsSpec#BOL_STRUCT_UNIQ_FLD_NM_DEF}</code>.
         * </ul>
         * </p>
         * 
         * @param depth         node depth of tree structures produced
         * @param fanout        node fan-out of tree structure
         * @param recScalarSpec configuration for scalar factory producing structure field values
         * 
         * @return  a new <code>TensorFacSpec</code> instance populated with the above argument values
         */
        public static StructFacSpec    from(int depth, int fanout, ScalarFactorySpec recScalarSpec) {
            return StructFacSpec.from(depth, fanout, BOL_STRUCT_UNIQ_FLD_NM_DEF, recScalarSpec);
        }
        
        /**
         * <p>
         * Creates and returns a new <code>TensorFacSpec</code> record configured with the given argument values.
         * </p>
         * <p>
         * This creator is equivalent to the canonical constructor where the argument collection contains all 
         * field values.
         * </p>
         * 
         * @param depth         node depth of tree structures produced
         * @param fanout        node fan-out of tree structure
         * @param bolUniqNms    enable/disable creation of unique field names by structure factory 
         * @param recScalarSpec configuration for scalar factory producing structure field values
         * 
         * @return  a new <code>TensorFacSpec</code> instance populated with the above argument values
         */
        public static StructFacSpec    from(int depth, int fanout, boolean bolUniqNms, ScalarFactorySpec recScalarSpec) {
            return new StructFacSpec(depth, fanout, bolUniqNms, recScalarSpec);
        }
        
        /**
         * <p>
         * Parses the argument collection for the field values of the returned <code>StructFacSpec</code> instance.
         * </p>
         * <p>
         * The argument collection is assumed to originate from an application command-line argument collection.
         * The <code>{@link TensorFactory}</code> class requires a 'shape' parameter and a 
         * <code>{@link ScalarFactory}</code> to create its element values.
         * </p>
         * <p>
         * <h2>Scalar Factory</h2>
         * The <code>{@link TensorFactory}</code> class requires a <code>{@link ScalarFactory}</code> instance.
         * The scalar factory is used to generate the elements
         * values of all tensors produced by the tensor factory described by this configuration.
         * Note the configuration for the <code>{@link ScalarFactorySpec}</code> field <code>{@link #recScalarSpec}</code>
         * is potentially included in the argument collection; if not provided a default scalar factory is supplied.
         * </p>  
         * <p>
         * <h2>Format</h2>
         * The format of the argument collection is assumed to be
         * <pre>
         * > depth fanout [bolUniqNms] [recScalarSpec]
         * </pre>
         * where
         * <ul>
         * <li>'depth' = tree structure node depth.</li>
         * <li>'fanout' = tree structure node fanout at each non-terminal node.</li>
         * <li>'bolUniqNms' = size of the 1st axis.</li>
         * <li>'recScalarSpec' = configuration record for the scalar factory producing tensor element values.</li>
         * </ul>
         * Note that 'depth' and 'fanout' are required parameters, thus, there must be at least 2 argument elements
         * or an exception is thrown.
         * </p>
         * <h2>Optional Arguments</h2>
         * The brackets indicate optional arguments.  The arguments are interpreted as follows:  
         * <ul>
         * <li>If the 'recScalarSpec' value is not present the argument is populated with the default scalar factory
         *     <code>{@link ScalarFactorySpec#from()}</code>.
         * </li>
         * <li>If the 'bolUniqNms' value is not present the value is taken from the JAL Tools default configuration
         *     with value <code>{@link DataColumnsSpec#BOL_STRUCT_UNIQ_FLD_NM_DEF}</code>
         * </li>
         * </ul>
         * </p>
         * 
         * @param args  argument collection to be parsed, format as described above
         * 
         * @return  a new <code>StructFacSpec</code> record populated with the parsed argument values
         * 
         * @throws ConfigurationException       argument must have at least 2 elements; 'depth' and 'fanout' parameters
         * @throws NumberFormatException        invalid numeric format (e.g., non-parseable 'depth', 'fanout', or scalar factory configuration)
         * @throws TypeNotPresentException      scalar factory configuration had unrecognized <code>JalScalarType</code> constant
         * @throws UnsupportedOperationException scalar factory configuration count not create 'increment' field
         * 
         * @see ScalarFactorySpec
         */
        public static StructFacSpec    parse(String...args) throws ConfigurationException, NumberFormatException, TypeNotPresentException, UnsupportedOperationException {
            
            // Check argument size
            if (args.length < 2)
                throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() 
                        + " - Arguments must contain at least 2 elements, depth and fan-out: " + args);
            
            // Parse the depth and fan-out parameters
            int     depth = Integer.valueOf(args[0]);   // throws NumberFormatException
            int     fanout = Integer.valueOf(args[1]);  // throws NumberFormatException
            if (args.length < 3)
                return StructFacSpec.from(depth, fanout);
            
            // Check if 3rd argument is a JalScalarType constant, ie., start of scalar factory configuration
            int     indScalCfg = 0; // the starting argument index for the scalar factory configuration (if it exists)
            boolean bolUniqNms;     // the enable/disable unique field names flag
            try {
                @SuppressWarnings("unused")
                JalScalarType   enmType = JalScalarType.getConstant(args[2]);   // throws TypeNotPresentException

                // The scalar factory configuration exists and starts here (at index 2)
                //  No enable/disable unique field name provided - use default
                indScalCfg = 2;
                bolUniqNms = BOL_STRUCT_UNIQ_FLD_NM_DEF;
                
            } catch (TypeNotPresentException e) {

                // The 3rd argument was not a JalScalarType
                //  Assume enable/disable unique field name flag and parse it
                indScalCfg = 3;
                bolUniqNms = Boolean.valueOf(args[2]);
            }
            
            String[]            arrScalCfg = Arrays.copyOfRange(args, indScalCfg, args.length);
            ScalarFactorySpec   recScalarSpec = ScalarFactorySpec.parseArgs(arrScalCfg); // throws TypeNotPresentException, NumericFormatException, UnsupportedOperationException

            return StructFacSpec.from(depth, fanout, bolUniqNms, recScalarSpec);
        }
        
        //
        // Operations
        //
        
        /**
         * <p>
         * Creates and returns a new <code>StructureFactory</code> according to this configuration.
         * </p>
         * 
         * @return  a new <code>StructureFactory</code> instance ready for simulated byte array creation
         */
        public StructureFactory newFactory() {
            return StructureFactory.from(this.depth, this.fanout, this.bolUniqNms, this.recScalarSpec);
        }
    };
    


    //
    // Operations
    //
    
    /**
     * <p>
     * Checks if the the <code>{@link #enmColType}</code> field is consistent with the <code>{@link #recFacSpec}</code> field.
     * </p>
     * 
     * @return  <code>true</code> if this is a correctly populated <code>DataColumnsSpec</code> record,
     *          <code>false</code> otherwise
     */
    public boolean  isValid() {
        
        return switch (this.enmColType) {
        case SCALAR -> (this.recFacSpec instanceof ScalarFactorySpec);
        case TIMESTAMP -> (this.recFacSpec instanceof TimestampFacSpec);
        case BYTES -> (this.recFacSpec instanceof ByteArrayFacSpec);
        case IMAGE -> (this.recFacSpec instanceof ImageFacSpec);
        case ARRAY -> (this.recFacSpec instanceof TensorFacSpec);
        case STRUCTURE -> (this.recFacSpec instanceof StructFacSpec);
        default -> false;
        };
    }
    
    public IDataColumnsFactory<Object>  newFactory() throws ConfigurationException, UnsupportedOperationException {
        
        // Check record configuration
        boolean bolValid = this.isValid();

        if (!bolValid) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Datum type is " + JalComplexType.SCALAR 
                    + " but DatumSpec is " + this.recFacSpec.getClass().getSimpleName();
            throw new ConfigurationException(strMsg);
        };
        
        // Create the column names
        Set<String> setColNms = IntStream.range(0, this.cntCols)
                                .<String>mapToObj(i -> this.strNmPref + Integer.toString(i))
                                .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);

        // Create the data column factory
        DataColumnsFactory  facCols;
        
        switch (this.enmColType) {
        case SCALAR:
            ScalarFactorySpec   valSpec = (ScalarFactorySpec) this.recFacSpec;
            ScalarFactory       facVals = valSpec.newFactory();
            
            facCols = DataColumnsFactory.from(setColNms, facVals);
            break;
            
        case TIMESTAMP:
            TimestampFacSpec       tmsSpec = (TimestampFacSpec) this.recFacSpec;
            TimestampFactory    facTms;
            if (tmsSpec.bolRand)
                facTms = TimestampFactory.from(tmsSpec.bolRand, tmsSpec.lngSeed);
            else
                facTms = TimestampFactory.from(tmsSpec.durPeriod, tmsSpec.insStart);
            
            facCols = DataColumnsFactory.from(setColNms, facTms);
            break;
            
        case BYTES:
            ByteArrayFacSpec       arrSpec = (ByteArrayFacSpec) this.recFacSpec;
            ByteArrayFactory    facBytes = ByteArrayFactory.from(arrSpec.cntByes);

            facCols = DataColumnsFactory.from(setColNms, facBytes);
            break;
            
        case IMAGE:
            ImageFacSpec           imgSpec = (ImageFacSpec) this.recFacSpec;
            ImageFactory        facImgs = ImageFactory.from(imgSpec.size, imgSpec.enmFmt, imgSpec.strPref);
            
            facCols = DataColumnsFactory.from(setColNms, facImgs);
            break;
            
        case ARRAY:
            TensorFacSpec          tenSpec = (TensorFacSpec) this.recFacSpec;
            ScalarFactory       facElem = tenSpec.recScalarSpec.newFactory();
            TensorFactory       facTens = TensorFactory.from(tenSpec.shape, facElem);
            
            facCols = DataColumnsFactory.from(setColNms, facTens);
            break;
            
        case STRUCTURE:
            StructFacSpec          strSpec = (StructFacSpec) this.recFacSpec;
            ScalarFactory       facFlds = strSpec.recScalarSpec.newFactory();
            StructureFactory    facStruc = StructureFactory.from(strSpec.depth, strSpec.fanout, strSpec.bolUniqNms, facFlds);
            
            facCols = DataColumnsFactory.from(setColNms, facStruc);
            break;
            
        default:
            throw new UnsupportedOperationException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Datum type " + this.enmColType + " is not supported.");
        }
        
        // Return the new DataColumsFactory
        return facCols;
    }
    
    
    //
    // JAL Library Resources
    //
    
    /** JAL Tools default configuration parameters for ingestion frame factories */
    private static final JalToolsFramesConfig           CFG_FRM_DEF = JalToolsConfig.getInstance().datagen.frames;
    
    /** JAL Tools default configuration parameters for datum factories */
    private static final JalToolsDataGenConfig.Values   CFG_VAL_DEF = JalToolsConfig.getInstance().datagen.values;
    
    
    // 
    // Record Constants - Default Values
    //
    
    /** Timestamp factory random value generation default value (i.e., generate noise) */
    public static final boolean     BOL_TMS_RND_ENBL_DEF = CFG_VAL_DEF.timestamp.random.enabled;
    
    /** Timestamp factory random number generator seed value default */
    public static final long        LNG_TMS_RND_SEED_DEF = CFG_VAL_DEF.timestamp.random.seed;
    
    /** Timestamp factory default period for incremental timestamp generation */
    public static final Duration    DUR_TMS_INCR_PERIOD_DEF = CFG_VAL_DEF.timestamp.increment.periodDuration();
    
    /** Timestamp factory default starting instant for incremental timestamp generation */
    public static final Instant     INS_TMS_INCR_START_DEF = CFG_VAL_DEF.timestamp.increment.startInstant();
    
    
    /** Byte array factory default byte array size (in bytes) */
    public static final int         INT_BYTES_SIZE_DEF = CFG_VAL_DEF.bytes.size;
    
    
    /** Image factory default value for image prefix */
    public static final String      STR_IMG_PREFIX_DEF = CFG_VAL_DEF.image.namePrefix;
    
    /** Image factory default image format */
    public static final BufferedImage.Format    ENM_IMG_FMT_DEF = CFG_VAL_DEF.image.format;
    
    /** Image factory default image size (in bytes) */
    public static final int         INT_IMG_SIZE_DEF = CFG_VAL_DEF.image.size;
    
    
    /** Structure factory default value for unique field name creation */
    public static final boolean     BOL_STRUCT_UNIQ_FLD_NM_DEF = CFG_VAL_DEF.structure.fieldNames.unique.enabled;
    
    
    //
    // Support Methods
    //
    
}
