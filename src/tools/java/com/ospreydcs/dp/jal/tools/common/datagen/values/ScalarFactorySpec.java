/*
 * Project: dp-jal
 * File:	ScalarFactorySpec.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.values
 * Type: 	ScalarFactorySpec
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
 * @since Nov 7, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.naming.ConfigurationException;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.values.JalToolsScalarValuesConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Record containing specification for a datum factory producing scalar values.
 * </p> 
 * <p>
 * The record fields values contain the configuration parameters for a <code>{@link ScalarFactory}</code>
 * instance.  The scalar type of the datum factory is given by the field <code>{@link #enmType()}</code>.
 * There are 2 basic types of scalar factories: 1) a scalar factory producing random values, and 2) a scalar
 * factory producing incremental values.  The type of scalar factory is determined by the value of field
 * <code>{@link #bolRandEnbl}</code>.  The interpretation of the remaining fields <code>#lngSeed()}</code>
 * and <code>{@link #numIncr()}</code> is determined by the value of <code>{@link #bolRandEnbl()}</code>.
 * </p>
 * <p>
 * <h2>The {@link #strPrefix()} Field</h2>
 * The field <code>{@link #strPrefix()}</code> is used only when 
 * <code>{@link #enmType()} = {@link JalScalarType#STRING}</code>.  In that case the field contains
 * the prefix of all generated string values, with the suffix given by the string value index. 
 * </p>
 * <p>
 * <h2>Random Factories</h2>
 * When the random value generation is enabled, scalar factories produce a 'random' sequence of datum values
 * according to the datum type.  The field values are interpreted as follows:
 * <ul>
 * <li><code>{@link #bolRandEnbl()} = true</code>.</li>
 * <li><code>{@link #lngSeed()} = 0</code> - random number lngSeed is 'randomly' generated.</li>
 * <li><code>{@link #lngSeed()} &ne; 0</code> - random number generator lngSeed is <code>{@link #lngSeed()}</code> producing 
 *     repeatable 'random' sequences.</li>
 * <li><code>{@link #numIncr()}</code> - ignored.</li>
 * </ul>  
 * Note that random scalar factories are more expensive than incremental factories as they require the use of a 
 * random number generator.  For large data sets this can be computationally significant.
 * </p>
 * <p>
 * <h2>Incremental Factories</h2>
 * When the random value generation is disabled, scalar factories produce incremental sequence of datum values
 * according to the datum type and the seed value.  The field values are interpreted as follows:
 * <ul>
 * <li><code>{@link #bolRandEnbl()} = false</code>.</li>
 * <li><code>{@link #lngSeed()}</code> - 1st value of the numeric sequence or 1st suffix of a string sequence.</li>
 * <li><code>{@link #numIncr()}</code> - the numeric increment for numeric sequence or suffix increment for strings.</li>
 * </ul>
 * Note that the <code>{@link #numIncr()}</code> type is interpreted according to the value of 
 * <code>{@link #enmType()}</code>.  When <code>{@link #enmType()} = {@link JalScalarType#STRING}</code>
 * the increment is assumed to be an integer.  
 * Incremental factories can be significantly faster than random factories since only an arithmetic operation is required.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 7, 2025
 * 
 * @param   enmType     the data type of the scalar values to generate
 * @param   bolRandEnbl enable/disable the use of random number generation for scalar values
 * @param   lngSeed     seed value for random number generation or start value for incremental value generation   
 * @param   numIncr     numeric incremental value used when random generation is disabled (type depends upon data type)
 * @param   strPrefix   prefix used for all string value generation (suffix given by integer value)
 */
public record ScalarFactorySpec(
        JalScalarType   enmType,
        boolean         bolRandEnbl,
        long            lngSeed,
        Number          numIncr,
        String          strPrefix
        ) 
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Parses argument string array to identify and create a <code>ScalarFactorySpec</code> configuration record.
     * </p>
     * <p>
     * The argument is typically part of an application command-line argument set obtained by parsing a delimited 
     * variable such as "--stype INTEGER FALSE 0 1".  The method parses the variable parameters returning the
     * corresponding configuration record.  An exception is thrown if the argument collection is not properly
     * formatted as described below.
     * </p> 
     * <p>
     * <h2>Caveat</h2>
     * Clearly it is not necessary for the arguments to be obtained from an application command line as described.  
     * So long as the stated conditions and formats are followed the method will create and return an appropriate 
     * <code>ScalarFactorySpec</code> configuration record.
     * </p> 
     * <p>
     * <h2>Usage</h2>
     * The argument is assumed to be part of an application command line.  For example, the command-line could
     * contain the delimited variable "--stype" which appears as
     * <code>
     * <pre>
     * > java application --stype STYPE bolRandEnbl lngSeed numIncr strPrefix
     * </pre>
     * </code>
     * where 
     * <ul>
     * <li>'STYPE' is a <code>JalScalarType</code> enumeration constant,</li>
     * <li>'bolRandEnbl' is the <code>{@link #bolRandEnbl()}</code> record field,</li>
     * <li>'lngSeed' is the <code>{@link #lngSeed()}</code> record field,</li>
     * <li>'numIncr' is the <code>{@link #numIncr()}</code> record field,</li>
     * <li>'strPrefix' is the <code>{@link #strPrefix()}</code> record field.</li>
     * </ul>
     * The argument to this method is then the set of command-line strings
     * <code>
     * <pre>
     * [STYPE [bolRandEnbl [lngSeed [numIncr [strPrefix]]]]]
     * </pre>
     * </code>
     * The brackets '[...]' indicate optional inclusion.
     * Note that the options are nested. For example, if the 'lngSeed' field is included then the 'bolRandEnbl'
     * field must also be included.  If the 'strPrefix' field is included then all fields must be included.
     * </p>
     * <p>
     * <s>  
     * Thus, the field 'STYPE' must always be included
     * in the arguments (i.e., the argument length must be greater than or equal to 1), or an exception is thrown.
     * </s>
     * </p>
     * <p>
     * <h2>Optional Fields</h2>
     * All optional field values not included in the argument collection are taken from the JAL Tools default
     * configuration.  Note that the nesting of optional field values is necessary because all argument values
     * are strings, thus, type cannot be determined at runtime but must be inferred.
     * </p> 
     * 
     * @apiNote
     * The implementation has been modified so that an empty argument collection returns the default 
     * <code>ScalarFactorySpec</code> given by <code>{@link #from()}</code>.  This configuration is completely
     * determined by the JAL Tools default configuration for scalar value factories. 
     *  
     * @param args  argument string defining a configuration record
     * 
     * @return  a new configuration record populated by the parsed argument elements
     * 
     * @throws TypeNotPresentException          the 1st element was not a <code>JalScalarType</code> enumeration constant
     * @throws NumberFormatException            invalid numeric format (e.g., 'lngSeed', 'numIncr')
     * @throws UnsupportedOperationException    unable to create <code>{@link #numIcr()}</code> field for numeric value type  
     */
    public static ScalarFactorySpec   parseArgs(String...args) throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException {

        // Check argument length
        if (args.length < 1)
            return ScalarFactorySpec.from();
//            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() 
//                    + " - Argument must contain at least one argument: " 
//                    + args);

        // Get the value type of the scalars to generate
        String          strValueType = args[0];
        JalScalarType   enmValueType = JalScalarType.getConstant(strValueType);     // throws TypeNotPresentException
        
        // --- Parse the random number generation enable/disable flag ---
        if (args.length < 2)
            return ScalarFactorySpec.from(enmValueType);
        Boolean bolRandEnable = Boolean.valueOf(args[1]);
        
        // --- Parse the lngSeed field value ---
        if (args.length < 3)
            return ScalarFactorySpec.from(enmValueType, bolRandEnable);
        Long    lngSeed = Long.valueOf(args[2]);    // throws NumberFormatException
        
        // --- Parse the increment field value ---
        if (args.length < 4)
            return ScalarFactorySpec.from(enmValueType, bolRandEnable, lngSeed);
        String  strIncr = args[3];
        
        Number  numIncr = switch (enmValueType) {
        case BOOLEAN -> Integer.valueOf(strIncr);   // throws NumberFormatException
        case INTEGER -> Integer.valueOf(strIncr);   // throws NumberFormatException
        case LONG -> Long.valueOf(strIncr);         // throws NumberFormatException
        case DOUBLE -> Double.valueOf(strIncr);     // throws NumberFormatException
        case FLOAT -> Float.valueOf(strIncr);       // throws NumberFormatException
        case STRING -> Integer.valueOf(strIncr);    // throws NumberFormatException
        default -> throw new UnsupportedOperationException("Increment value not available for type: " + enmValueType);
        };
        
        // --- Parse the string prefix field value ---
        if (args.length < 5)
            return ScalarFactorySpec.from(enmValueType, bolRandEnable, lngSeed, numIncr);
        
        String  strPrefix = args[4];
        return ScalarFactorySpec.from(enmValueType, bolRandEnable, lngSeed, numIncr, strPrefix);
    }
    
    /**
     * <p>
     * Parses the input stream as if it were a single YAML document containing the record field values.
     * </p>
     * <p>
     * A Snake YAML parse is used to create a <code>{@link ScalarFactoryYamlSpec}</code> class instance to recover
     * the field values from the given input stream. 
     * Note that the argument stream is assumed to represent a single YAML document and the stream is 
     * thus exhausted after calling this method.
     * </p>
     * <p>
     * <h2>Format</h2>
     * The format of the YAML document is given in the enclose class <code>{@link ScalarFactoryYamlSpec}</code> documentation.
     * </p>  
     *  
     * @param is    input stream to a single YAML document
     * 
     * @return  a new <code>ScalarFactorySpec</code> record with field populated by the YAML document
     * 
     * @throws  YAMLException               error occurred while parsing class <code>{@link ScalarFactoryYamlSpec}</code>
     * @throws IllegalStateException        intermediate structure class was not populated (internal error)
     * @throws UnsupportedOperationException    unable to create <code>{@link #numIncr()}</code> field for value type  
     * 
     * @see ScalarFactoryYamlSpec
     */
    public static ScalarFactorySpec   parseYamlDoc(InputStream is) throws YAMLException, IllegalStateException, UnsupportedOperationException {
        
        Yaml    yaml = new Yaml();
        
        ScalarFactoryYamlSpec   struct = yaml.loadAs(is, ScalarFactoryYamlSpec.class); // throws YAMLException
        ScalarFactorySpec recCfg = struct.createRecord();

        return recCfg;
    }
    
    /**
     * <p>
     * Parses a single YAML node for the field values of a <code>ScalarFactorySpec</code> record.
     * </p>
     * <p>
     * The input stream is parsed for a single node containing the fields of a <code>ScalarFactorySpec</code>
     * record.  The stream is left in the line position directly after the node parsed (i.e., no further
     * parsing is pursued). 
     * The input stream is parsed according to the following:
     * <ul>
     * <li>A single YAML node is parsed within the input stream.</li>
     * <li>Assuming correct formatting, the input stream is left in the position after the YAML node.</li>
     * <li>Lines are continuously parsed until all field values are found.</li>
     * <li>Empty lines are skipped.</li>
     * <li>Field labels are pre-fixed by YAML delimiter {@value #STR_YAML_DEL_LBL}.</li>
     * <li>Field values are post-fixed by YAML delimiter {@value #STR_YAML_DEL_LBL}.</li>
     * <li>All comments are ignored, whether in-line or full-line, identified by YAML delimiter {@value #STR_YAML_DEL_CMT}.</li>
     * </ul> 
     * </p>
     * <p>
     * <h2>Format</h2>
     * The format of the YAML node is given according to the following:
     * <code>
     * <pre>
     *   # comment - Scalar number generation configuration parameters 
     *   type: TYPE                 # comment
     *   stringPrefix: prefix       # comment
     *   random:                    # comment
     *      enabled: rand_flag      # comment
     *      seed: seed_value        # comment
     *   increment:                 # comment
     *      start: start_value      # comment
     *      value: incr_value       # comment
     * </pre>
     * </code>
     * where
     * <ul>
     * <li>'comment' is an arbitrary comment string,</li>
     * <li><code>TYPE</code> = <code>JalScalarType</code> enumeration constant,</li>
     * <li>'prefix' = <code>String</code> value prefix given to all generated string value types,</li>
     * <li>'rand_flag' = <code>boolean</code> value enabling/disabling random number generation,</li>
     * <li>'seed_value' = <code>long</code> value used as random number seed (use 0 for random seed),</li>
     * <li>'start_value' = <code>long</code> value used for incremental number generation seed value,</li>
     * <li>'incr_value' = <code>Number</code> value used for incremental number generation increment value.</li>
     * </ul>
     * The field labels do not need to appear in the given order, however, the internal nodes 'random' and
     * 'increment' must contain their respective field labels.
     * The argument stream is advanced to the location of the last line containing the last field
     * value found. 
     * </p>
     * 
     * @param is    input stream containing YAML scalar value generation configuration node
     * 
     * @return  a new <code>ScalarFactorySpec</code> record with field populated by the YAML node
     * 
     * @throws IOException                  I/O error occurred while reading line from argument input stream
     * @throws IndexOutOfBoundsException    bad 'label: value' format  
     * @throws TypeNotPresentException      an invalid <code>JalScalarType</code> was encountered 
     * @throws NumberFormatException        a bad format for a string represented number was encountered
     * @throws ConfigurationException       missing or corrupt field values
     */
    public static ScalarFactorySpec   parseYamlNode(InputStream is) 
            throws IOException, IndexOutOfBoundsException, TypeNotPresentException, NumberFormatException, ConfigurationException 
    {

        // --- Field value labels ---
        final String    STR_TYPE = "type";
        final String    STR_PREF = "stringPrefix";  // String prefix used for any string value 
        final String    STR_RAND_ENBL = "enabled";  // Random number field labels
        final String    STR_RAND_SEED = "seed";     // Random number field labels
        final String    STR_INCR_START = "start";   // Increment value field labels
        final String    STR_INCR_VALUE = "value";   // Increment value field labels

        // --- Node labels ---
        final String    STR_RAND = "random";
        final String    STR_INCR = "increment";
        
        // Number of field values
        final int       CNT_VALUES = 6;
        
        // Field values
        JalScalarType   enmValueType = JalScalarType.UNSUPPORTED;
        String          stringPref = null;
        Boolean         randBolEnbl = null;
        Long            randLngSeed = null;
        Long            incrLngStart = null;
        Number          incrNumValue = null;
        String          incrStrValue = null;
        
        // Create a reader for the input stream for line-based parsing
        InputStreamReader   rdrStrm = new InputStreamReader(is);
        BufferedReader      rdrBuff = new BufferedReader(rdrStrm);

        // Initialize the loop then continue reading lines until all field labels are found
        int     iValue=0;
        String  strLabel;
        String  strValue;
        do {
            String      strLine = rdrBuff.readLine().strip();           // throws IOException
            
            // Extract the field label (note that comment lines are skipped)
            int         indDel = strLine.indexOf(STR_YAML_DEL_LBL);
            if (indDel < 0)
                continue;
            strLabel = strLine.substring(0, indDel);
            
            // Extract the field value if present (empty values are empty string)
            strValue = strLine.substring(indDel+1);                     // throws IndexOutOfBoundsException
            strValue = strValue.split(STR_YAML_DEL_CMT)[0];             // throws ArrayIndexOutOfBoundsException
            strValue = strValue.strip();
            
            switch (strLabel) {
            
            // The scalar value type
            case STR_TYPE:
                enmValueType = JalScalarType.getConstant(strValue); // throws TypeNotPresentException
                iValue++;
                break;
                
            // The string prefix (for string types) 
            case STR_PREF:
                strValue = strValue.replaceAll("\'", "");
                strValue = strValue.replaceAll("\"", "");
                stringPref = strValue;
                iValue++;
                break;
                
            // Skip node labels
            case STR_RAND:
                break;
                
            // Skip node labels
            case STR_INCR:
                break;
                
            // Random number generation enable/disable flag
            case STR_RAND_ENBL:
                randBolEnbl = Boolean.valueOf(strValue);
                iValue++;
                break;
                
            // Random number generator seed value
            case STR_RAND_SEED:
                randLngSeed = Long.valueOf(strValue);                       // throws NumberFormatException
                iValue++;
                break;
                
            // Incremental generator seed value 
            case STR_INCR_START:
                incrLngStart = Long.valueOf(strValue);                     // throws NumberFormatException
                iValue++;
                break;
               
            // Incremental generator increment value
            case STR_INCR_VALUE:
                incrStrValue = strValue;
                iValue++;
                break;
            }
            
        } while (iValue < CNT_VALUES);  // keep parsing until all field labels are found
        
        // Check for missing field values 
        if (enmValueType == JalScalarType.UNSUPPORTED)
            throw ScalarFactorySpec.missingFieldLabel(STR_TYPE);
        if (stringPref == null)
            throw ScalarFactorySpec.missingFieldLabel(STR_PREF);
        if (randBolEnbl == null)
            throw ScalarFactorySpec.missingFieldLabel(STR_RAND_ENBL);
        if (randLngSeed == null)
            throw ScalarFactorySpec.missingFieldLabel(STR_RAND_SEED);
        if (incrLngStart == null)
            throw ScalarFactorySpec.missingFieldLabel(STR_INCR_START);
        if (incrStrValue == null)
            throw ScalarFactorySpec.missingFieldLabel(STR_INCR_VALUE);
        
        if (enmValueType != JalScalarType.STRING)
            try {
                incrNumValue = (Number) enmValueType.parseValue(incrStrValue);
                
            } catch (Exception e) {
                throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() 
                        + " - Increment value " + incrStrValue + " cannot be converted to type " + enmValueType 
                        + ": " + e.getMessage());
            }
        else 
            incrNumValue = Integer.valueOf(incrStrValue);  // throws NumberFormatException

        // Create and return the record according to random number enable/disable flag
        if (randBolEnbl)
            return ScalarFactorySpec.from(enmValueType, randBolEnbl, randLngSeed, incrNumValue, stringPref);
        else
            return ScalarFactorySpec.from(enmValueType, randBolEnbl, incrLngStart, incrNumValue, stringPref);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactory</code> record with all default field values.
     * </p>
     * <p>
     * All field values of the returned instance are taken from the JAL Tools default configuration.
     * Use with discretion.
     * </p>
     * 
     * @return  a new <code>ScalarFactorySpec</code> record populated with all default field values
     * 
     * @throws UnsupportedOperationException    unable to create <code>{@link #numIncr()}</code> field for value type  
     */
    public static ScalarFactorySpec   from() throws UnsupportedOperationException {
        return ScalarFactorySpec.from(ENM_TYPE_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactorySpec</code> record from the given arguments.
     * </p>
     * <p>
     * Record fields not supplied are taken from the JAL Tools default configuration.
     * <ul>
     * <li>Field <code>{@link #bolRandEnbl()}</code> is taken directly from the JAL default configuration.</li>
     * <li>Field <code>{@link #lngSeed()}</code> is determined by the random enable/disable flag.</li>
     * <li>Field <code>{@link #numIncr()}</code> is determined by the <code>JalScalarType</code> argument.</li>
     * <li>Field <code>{@link #strPrefix()}</code> is taken from the configuration <code>{@link #STR_PREFIX}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   enmType    the data type of the scalar values to generate
     * 
     * @return  a new <code>ScalarFactorySpec</code> record populated with the given argument values
     * 
     * @throws UnsupportedOperationException    unable to create <code>{@link #numIncr()}</code> field for value type  
     */
    public static ScalarFactorySpec   from(JalScalarType enmValueType) throws UnsupportedOperationException {
        
        boolean bolRandEnable = BOL_RAND_ENBL_DEF;
        
        return ScalarFactorySpec.from(enmValueType, bolRandEnable);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactorySpec</code> record from the given arguments.
     * </p>
     * <p>
     * Record fields not supplied are taken from the JAL Tools default configuration.
     * <ul>
     * <li>Field <code>{@link #bolRandEnbl()}</code> is taken directly from the JAL default configuration.</li>
     * <li>Field <code>{@link #numIncr()}</code> is determined by the <code>JalScalarType</code> argument.</li>
     * <li>Field <code>{@link #strPrefix()}</code> is taken from the configuration <code>{@link #STR_PREFIX}</code>.</li>
     * </ul>
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * Use this creator with caution as the 'lngSeed' value is interpreted according to the default value of
     * the 'bolRandEnbl' parameter (i.e., <code>{@link #BOL_RAND_ENBL_DEF}</code>).
     * </p>
     * 
     * @param   enmType    the data type of the scalar values to generate
     * @param   lngSeed    lngSeed value for random number generation or start value for incremental value generation   
     * 
     * @return  a new <code>ScalarFactorySpec</code> record populated with the given argument values
     * 
     * @throws UnsupportedOperationException    unable to create <code>{@link #numIncr()}</code> field for value type  
     */
    public static ScalarFactorySpec   from(JalScalarType enmValueType, long lngSeed) throws UnsupportedOperationException {
        
        boolean bolRandEnable = BOL_RAND_ENBL_DEF;
        
        return ScalarFactorySpec.from(enmValueType, bolRandEnable, lngSeed);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactorySpec</code> record from the given arguments.
     * </p>
     * <p>
     * Record fields not supplied are taken from the JAL Tools default configuration.
     * <ul>
     * <li>Field <code>{@link #lngSeed()}</code> is determined by the random enable/disable flag.</li>
     * <li>Field <code>{@link #numIncr()}</code> is determined by the <code>JalScalarType</code> argument.</li>
     * <li>Field <code>{@link #strPrefix()}</code> is taken from the configuration <code>{@link #STR_PREFIX}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   enmType    the data type of the scalar values to generate
     * @param   bolRandEnbl   enable/disable the use of random number generation for scalar values
     * 
     * @return  a new <code>ScalarFactorySpec</code> record populated with the given argument values
     * 
     * @throws UnsupportedOperationException    unable to create <code>{@link #numIncr()}</code> field for value type  
     */
    public static ScalarFactorySpec   from(JalScalarType enmValueType, boolean bolRandEnable) throws UnsupportedOperationException {
        
        long    seed;
        if (bolRandEnable)
            seed = LNG_RAND_SEED_DEF;
        else
            seed = LNG_INCR_SEED_DEF;
            
        return ScalarFactorySpec.from(enmValueType, bolRandEnable, seed);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactorySpec</code> record from the given arguments.
     * </p>
     * <p>
     * Record fields not supplied are taken from the JAL Tools default configuration.
     * <ul>
     * <li>Field <code>{@link #numIncr()()}</code> is determined by the <code>JalScalarType</code> argument.</li>
     * <li>Field <code>{@link #strPrefix()}</code> is taken from the configuration <code>{@link #STR_PREFIX}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   enmType       the data type of the scalar values to generate
     * @param   bolRandEnbl   enable/disable the use of random number generation for scalar values
     * @param   lngSeed       lngSeed value for random number generation or start value for incremental value generation   
     * 
     * @return  a new <code>ScalarFactorySpec</code> record populated with the given argument values
     * 
     * @throws UnsupportedOperationException    unable to create <code>{@link #numIncr()}</code> field for value type  
     */
    public static ScalarFactorySpec   from(JalScalarType enmValueType, boolean bolRandEnable, long lngSeed) throws UnsupportedOperationException {
        
        // Extract the incremental value from the JAL default parameters
        Number numIncr = switch (enmValueType) {
        case BOOLEAN -> Integer.valueOf(0);
        case INTEGER -> INT_INCR_DEF;
        case LONG -> LNG_INCR_DEF;
        case FLOAT -> FLT_INCR_DEF;
        case DOUBLE -> DBL_INCR_DEF;
        case STRING -> INT_STR_INCR_DEF;
        default -> throw new UnsupportedOperationException("Increment value not available for type: " + enmValueType);
        };
        
        return ScalarFactorySpec.from(enmValueType, bolRandEnable, lngSeed, numIncr);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactorySpec</code> record from the given arguments.
     * </p>
     * <p>
     * Record fields not supplied are taken from the JAL Tools default configuration.
     * <ul>
     * <li>Field <code>{@link #strPrefix()}</code> is taken from the configuration <code>{@link #STR_PREFIX}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   enmType       the data type of the scalar values to generate
     * @param   bolRandEnbl   enable/disable the use of random number generation for scalar values
     * @param   lngSeed       lngSeed value for random number generation or start value for incremental value generation   
     * @param   numIncr       numeric incremental value used when random generation is disabled (type depends upon data type)
     * 
     * @return  a new <code>ScalarFactorySpec</code> record populated with the given argument values
     */
    public static ScalarFactorySpec   from(JalScalarType enmValueType, boolean bolRandEnable, long seed, Number numIncr) {
        
        return ScalarFactorySpec.from(enmValueType, bolRandEnable, seed, numIncr, STR_PREFIX_DEF); 
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactorySpec</code> record from the given arguments.
     * </p>
     * <p>
     * This creator is equivalent to the canonical constructors.
     * </p>
     * 
     * @param   enmType       the data type of the scalar values to generate
     * @param   bolRandEnbl   enable/disable the use of random number generation for scalar values
     * @param   lngSeed       lngSeed value for random number generation or start value for incremental value generation   
     * @param   numIncr       numeric incremental value used when random generation is disabled (type depends upon data type)
     * @param   strPrefix     prefix used for all string value generation (suffix given by integer value)
     * 
     * @return  a new <code>ScalarFactorySpec</code> record populated with the given argument values
     */
    public static ScalarFactorySpec   from(JalScalarType enmValueType, boolean bolRandEnable, long lngSeed, Number numIncr, String strPrefix) {
        return new ScalarFactorySpec(enmValueType, bolRandEnable, lngSeed, numIncr, strPrefix);
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactory</code> instance with this configuration.
     * </p>
     * <p>
     * A new <code>{@link ScalarFactory}</code> instance is created according to the configuration parameters in 
     * this record.  The <code>ScalarFactory</code> is then returned in its initialized state, ready for scalar
     * value creation.
     * </p>
     * 
     * @return  a new <code>ScalarFactory</code> instance ready for scalar value creation
     */
    public ScalarFactory    newFactory() {
        ScalarFactory   fac = ScalarFactory.from(this.enmType, this.bolRandEnbl, this.lngSeed, this.numIncr, this.strPrefix);
        
        return fac;
    }
    
    
    //
    // Record Overrides
    //
    
    /**
     * <p>
     * Overrides <code>{@link Record#equals(Object)}</code> to provide equivalence rather than equals.
     * </p>
     * <p>
     * Tests the given argument for equivalence, that is, do all the record fields have the same value.
     * The argument is first tested for correct data type, specifically, it must be of type
     * <code>ScalarFactorySpec</code> or the method returns <code>false</code>.
     * Note that the argument can be a different record instance but if all field values are equal then 
     * the method returns <code>true</code>.
     * </p>
     * 
     * @param   objCmp  object under equivalence comparison
     * 
     * @return  <code>true</code> if the argument is a <code>ScalarFactorySpec</code> record with equal field values,
     *          <code>false</code> otherwise
     * 
     * @see Record#equals(Object)
     */
    @Override
    public boolean equals(Object objCmp) {
        
        if (objCmp instanceof ScalarFactorySpec rec) {
            if (this.enmType == rec.enmType
                    && this.bolRandEnbl == rec.bolRandEnbl
                    && this.lngSeed == rec.lngSeed 
                    && this.numIncr == rec.numIncr
                    && this.strPrefix.equals(rec.strPrefix))
                return true;
        }
        
        return false;
    }
    
    /**
     * <p>
     * Returns a line-by-line string representation of the record fields.
     * </p>
     * 
     * @return  a string with each line representing a field value
     *  
     * @see java.lang.Record#toString()
     */
    @Override
    public String   toString() {
        String  str = "";
        str += "Scalar value type: " + this.enmType + "\n";
        str += "Random enabled   : " + this.bolRandEnbl + "\n";
        str += "Seed value       : " + this.lngSeed + "\n";
        str += "Increment value  : " + this.numIncr + "\n";
        str += "String prefix    : " + this.strPrefix + "\n";
        
        return str;
    }
    
    
    //
    // Internal Types
    //
    
    
    /**
     * <p>
     * Structure class used for Snake YAML parsing.
     * </p>
     * <p>
     * Instances of this class are created during the parsing of YAML files or YAML formatted 
     * string in method <code>{@link ScalarFactorySpec#parseYamlDoc(InputStream)}</code> of
     * the enclosing record.  Once parsed the class instance contains all the field values
     * of the <code>{@link ScalarFactorySpec}</code> record.  The instance is then used
     * to create a new record.
     * </p>
     * <p>
     * <h2>Format</h2>
     * The YAML document must have the format of this structure class.  Specifically, the
     * YAML document is the following:
     * <code>
     * <pre>
     *   # comment - Scalar number generation configuration parameters 
     *   type: TYPE                 # comment
     *   stringPrefix: prefix       # comment
     *   random:                    # comment
     *      enabled: rand_flag      # comment
     *      seed: seed_value        # comment
     *   increment:                 # comment
     *      start: start_value      # comment
     *      value: incr_value       # comment
     * </pre>
     * </code>
     * where
     * <ul>
     * <li>'comment' is an arbitrary comment string,</li>
     * <li><code>TYPE</code> = <code>JalScalarType</code> enumeration constant,</li>
     * <li>'prefix' = <code>String</code> value prefix given to all generated string value types,</li>
     * <li>'rand_flag' = <code>boolean</code> value enabling/disabling random number generation,</li>
     * <li>'seed_value' = <code>long</code> value used as random number seed (use 0 for random seed),</li>
     * <li>'start_value' = <code>long</code> value used for incremental number generation seed value,</li>
     * <li>'incr_value' = <code>Number</code> value used for incremental number generation increment value.</li>
     * </ul>
     * The field labels do not need to appear in the given order, however, the internal nodes 'random' and
     * 'increment' must contain their respective field labels.
     * </p>
     *
     */
    public static final class ScalarFactoryYamlSpec extends CfgStructure<ScalarFactoryYamlSpec> {
        
        /**
         * <p>
         * Creates and returns a new <code>ScalarFactorySpec</code> record populated with the attributes of this structure.
         * </p>
         * <p>
         * This instance must be fully populated before method invocation or an exception is thrown.
         * The assumed to be populated using a Snake YAML document parsing operation.
         * </p>
         * 
         * @return  a new <code>ScalarFactorySpec</code> record populated with this structure's attributes
         * 
         * @throws IllegalStateException            method called before structure class was populated
         * @throws UnsupportedOperationException    unable to create <code>{@link #numIncr()}</code> field for value type  
         */
        public ScalarFactorySpec  createRecord() throws IllegalStateException, UnsupportedOperationException {
            
            //  Check state
            if (this.stringPrefix==null && type == JalScalarType.UNSUPPORTED)
                throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Structure class not populated.");

            long    lngSeed = (this.random.enabled) ? this.random.seed : this.increment.start;
            Number  numIncr = switch (this.type) {
            case INTEGER -> this.increment.value.intValue();
            case BOOLEAN -> this.increment.value.intValue();
            case DOUBLE -> this.increment.value.doubleValue();
            case FLOAT -> this.increment.value.floatValue();
            case LONG -> this.increment.value.longValue();
            case STRING -> this.increment.value.intValue();
            case UNSUPPORTED -> throw new UnsupportedOperationException("Increment value unsupported for type: " + this.type);
            };
            
            return ScalarFactorySpec.from(type, this.random.enabled, lngSeed, numIncr, stringPrefix);
        }
        
        /** Default constructor required from base class */
        public ScalarFactoryYamlSpec() { super(ScalarFactoryYamlSpec.class); };
        
        @ACfgOverride.Field(name="STRING_PREFIX")
        public String           stringPrefix = null;
        
        @ACfgOverride.Field(name="TYPE")
        public JalScalarType    type = JalScalarType.UNSUPPORTED;
        
        @ACfgOverride.Struct(pathelem="RANDOM")
        public Random           random;
        
        @ACfgOverride.Struct(pathelem="INCREMENT")
        public Increment        increment;
        
        
        public static final class Random extends CfgStructure<Random> {
            
            /** Default constructor required of base class */
            public Random() { super(Random.class); };
            
            @ACfgOverride.Field(name="ENABLED")
            public Boolean      enabled;
            
            @ACfgOverride.Field(name="SEED")
            public Long         seed;
        };
        
        public static final class Increment extends CfgStructure<Increment> {
            
            /** Default constructor required of base class */
            public Increment()  { super(Increment.class); };
            
            
            @ACfgOverride.Field(name="START")
            public Long         start;
            
            @ACfgOverride.Field(name="VALUE")
            public Number       value;
        };
                
    }

    
    //
    // Record Constants
    //
    
    /** YAML line field label and token delimiters */
    public static final String    STR_YAML_DEL_LBL = ":";
    
    /** YAML line comment delimiter */
    public static final String    STR_YAML_DEL_CMT = "#";
    
    
    //
    // Library Resources
    //
    
    /** The default parameters for scalar-valued simulated data generation */
    private static final JalToolsScalarValuesConfig     CFG_DEF = JalToolsConfig.getInstance().datagen.values.scalar;
    
    
    //
    // Record Constants - Default Arguments
    //
    
    /** The default scalar value type when none is given */
    public final static JalScalarType  ENM_TYPE_DEF = CFG_DEF.type;
    
    /** The default string value prefix */
    public final static String         STR_PREFIX_DEF = CFG_DEF.stringPrefix;

    
    /** The default enable/disable random number generator */
    public static final boolean BOL_RAND_ENBL_DEF = CFG_DEF.random.enabled;

    /** The default random number generator seed value */
    public static final long    LNG_RAND_SEED_DEF = CFG_DEF.random.seed;

    /** The default incremental seed value */
    public static final long    LNG_INCR_SEED_DEF = CFG_DEF.increment.seed;
    
    
    //
    // Record Constants - Scalar increment values
    //
    
    /** Integer value default increment */
    public final static Integer     INT_INCR_DEF = CFG_DEF.increment.integerv;
    
    /** Long value default increment */
    public final static Long        LNG_INCR_DEF = CFG_DEF.increment.longv;
    
    /** Float value default increment */
    public final static Float       FLT_INCR_DEF = CFG_DEF.increment.floatv;
    
    /** Double value default increment */
    public final static Double      DBL_INCR_DEF = CFG_DEF.increment.doublev;
    
    /** String value increment */
    public final static Integer     INT_STR_INCR_DEF = CFG_DEF.increment.stringv;
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates and returns a new <code>ConfigurationException</code> for the case of a missing field label.
     * </p>
     * 
     * @param strFldNm  name of the field label
     * 
     * @return  new <code>ConfigurationException</code> with message indication the missing field label
     */
    private static ConfigurationException   missingFieldLabel(String strFldNm) {
        String  strMsg = JavaRuntime.getQualifiedCallerNameSimple() 
                + " - Field "
                + "'" + strFldNm + "'"
                + " missing from YAML labels.";
        
        return new ConfigurationException(strMsg);
    }
    
}
