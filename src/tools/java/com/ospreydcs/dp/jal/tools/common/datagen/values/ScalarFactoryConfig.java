/*
 * Project: dp-jal
 * File:	ScalarFactoryConfig.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.values
 * Type: 	ScalarFactoryConfig
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
import com.ospreydcs.dp.jal.tools.config.datagen.JalToolsScalarValuesConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Record containing configuration parameters for a <code>{@link ScalarFactory}</code> instance.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Nov 7, 2025
 * 
 * @param   enmValueType    the data type of the scalar values to generate
 * @param   bolRandEnable   enable/disable the use of random number generation for scalar values
 * @param   seed            seed value for random number generation or start value for incremental value generation   
 * @param   increment       numeric incremental value used when random generation is disabled (type depends upon data type)
 * @param   strPrefix       prefix used for all string value generation (suffix given by integer value)
 */
public record ScalarFactoryConfig(
        JalScalarType   enmValueType,
        boolean         bolRandEnable,
        long            seed,
        Number          increment,
        String          strPrefix
        ) 
{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Parses argument string array to identify and create a <code>ScalarFactoryConfig</code> configuration record.
     * </p>
     * <p>
     * The argument is typically part of an application command-line argument set obtained by parsing a delimited 
     * variable such as "--vtype INTEGER FALSE 0 1".  The method parses the variable parameters returning the
     * corresponding configuration record.  An exception is thrown if the argument collection is not properly
     * formatted as described below.
     * </p> 
     * <p>
     * <h2>Caveat</h2>
     * Clearly it is not necessary for the arguments to be obtained from an application command line as described.  
     * So long as the stated conditions and formats are followed the method will create and return an appropriate 
     * <code>ScalarFactoryConfig</code> configuration record.
     * </p> 
     * <p>
     * <h2>Usage</h2>
     * The argument is assumed to be part of an application command line.  For example, the command-line could
     * contain the delimited variable "--vtype" which appears as
     * <code>
     * <pre>
     * > java application --vtype VTYPE bolRandEnable seed increment strPrefix
     * </pre>
     * </code>
     * where 
     * <ul>
     * <li>'VTYPE' is a <code>JalScalarType</code> enumeration constant,</li>
     * <li>'bolRandEnable' is the <code>{@link #bolRandEnable()} record field,</li>
     * <li>'seed' is the <code>{@link #seed()}</code> record field,</li>
     * <li>'increment' is the <code>{@link #increment()} record field,</li>
     * <li>'strPrefix' is the <code>{@link #strPrefix()}</code> record field.</li>
     * </ul>
     * The argument to this method is then the set of command-line strings
     * <code>
     * <pre>
     * VTYPE [bolRandEnable [seed [increment [strPrefix]]]]
     * </pre>
     * </code>
     * The brackets '[...]' indicate optional inclusion.  Thus, the field 'VTYPE' must always be included
     * in the arguments (i.e., the argument length must be greater than or equal to 1), or an exception is thrown.
     * Note that the options are nested. For example, if the 'seed' field is included then the 'bolRandEnable'
     * field must also be included.  If the 'strPrefix' field is included then all fields must be included.
     * </p>
     * <p>
     * <h2>Optional Fields<h2>
     * All optional field values not included in the argument collection are taken from the JAL Tools default
     * configuration.  Note that the nesting of optional field values is necessary because all argument values
     * are strings, thus, type cannot be determined at runtime but must be inferred.
     * </p> 
     * 
     * Thus, the number of elements within the argument string array is dependent upon the <code>JalHeteroType</code>
     * identified by the first element.  If the number of arguments is not appropriate for the given type
     * a <code>ConfigurationException</code> is thrown.
     * </p>
     *  
     * @param args  argument string defining a configuration record
     * 
     * @return  a new configuration record populated by the parsed argument elements
     * 
     * @throws IllegalArgumentException the argument contained no data (length = 0)
     * @throws TypeNotPresentException  the 1st element was not a <code>JalHeteroType</code> enumeration constant
     * @throws UnsupportedOperationException    unable to create <code>{@link #increment}</code> field for value type  
     */
    public static ScalarFactoryConfig   parseArgs(String...args) throws IllegalArgumentException, TypeNotPresentException, UnsupportedOperationException {

        // Check argument length
        if (args.length < 1)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Argument must contain at least one argument: " 
                    + args);

        // Get the value type of the scalars to generate
        String          strValueType = args[0];
        JalScalarType   enmValueType = JalScalarType.getConstant(strValueType);     // throws TypeNotPresentException
        
        // --- Parse the random number generation enable/disable flag ---
        if (args.length < 2)
            return ScalarFactoryConfig.from(enmValueType);
        Boolean bolRandEnable = Boolean.valueOf(args[1]);
        
        // --- Parse the seed field value ---
        if (args.length < 3)
            return ScalarFactoryConfig.from(enmValueType, bolRandEnable);
        Long    lngSeed = Long.valueOf(args[2]);
        
        // --- Parse the increment field value ---
        if (args.length < 4)
            return ScalarFactoryConfig.from(enmValueType, bolRandEnable, lngSeed);
        String  strIncr = args[3];
        
        Number  numIncr = switch (enmValueType) {
        case BOOLEAN -> Integer.valueOf(strIncr);
        case INTEGER -> Integer.valueOf(strIncr);
        case LONG -> Long.valueOf(strIncr);
        case DOUBLE -> Double.valueOf(strIncr);
        case FLOAT -> Float.valueOf(strIncr);
        case STRING -> Integer.valueOf(strIncr);
        default -> throw new UnsupportedOperationException("Increment value not available for type: " + enmValueType);
        };
        
        // --- Parse the string prefix field value ---
        if (args.length < 5)
            return ScalarFactoryConfig.from(enmValueType, bolRandEnable, lngSeed, numIncr);
        
        return ScalarFactoryConfig.from(enmValueType, bolRandEnable, lngSeed, numIncr, args[4]);
    }
    
    /**
     * <p>
     * Parses the input stream as if it were a single YAML document containing the record field values.
     * </p>
     * <p>
     * A Snake YAML parse is used to create a <code>{@link ScalarFactoryYaml}</code> class instance to recover
     * the field values from the given input stream. 
     * Note that the argument stream is assumed to represent a single YAML document and the stream is 
     * thus exhausted after calling this method.
     * </p>
     * <p>
     * <h2>Format</h2>
     * The format of the YAML document is given in the enclose class <code>{@link ScalarFactoryYaml}</code> documentation.
     * </p>  
     *  
     * @param is    input stream to a single YAML document
     * 
     * @return  a new <code>ScalarFactoryConfig</code> record with field populated by the YAML document
     * 
     * @throws  YAMLException               error occurred while parsing class <code>{@link ScalarFactoryYaml}</code>
     * @throws IllegalStateException        intermediate structure class was not populated (internal error)
     * @throws UnsupportedOperationException    unable to create <code>{@link #increment}</code> field for value type  
     * 
     * @see ScalarFactoryYaml
     */
    public static ScalarFactoryConfig   parseYamlDoc(InputStream is) throws YAMLException, IllegalStateException, UnsupportedOperationException {
        
        Yaml    yaml = new Yaml();
        
        ScalarFactoryYaml   struct = yaml.loadAs(is, ScalarFactoryYaml.class); // throws YAMLException
        ScalarFactoryConfig recCfg = struct.createRecord();

        return recCfg;
    }
    
    /**
     * <p>
     * Parses a single YAML node for the field values of a <code>ScalarFactoryConfig</code> record.
     * </p>
     * <p>
     * The input stream is parsed for a single node containing the fields of a <code>ScalarFactoryConfig</code>
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
     * @return  a new <code>ScalarFactoryConfig</code> record with field populated by the YAML node
     * 
     * @throws IOException                  I/O error occurred while reading line from argument input stream
     * @throws IndexOutOfBoundsException    bad 'label: value' format  
     * @throws TypeNotPresentException      an invalid <code>JalScalarType</code> was encountered 
     * @throws NumberFormatException        a bad format for a string represented number was encountered
     * @throws ConfigurationException       missing or corrupt field values
     */
    public static ScalarFactoryConfig   parseYamlNode(InputStream is) 
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
        Boolean         bolRandEnable = null;
        Long            lngSeed = null;
        Long            incrStart = null;
        Number          incrValue = null;
        String          strIncrValue = null;
        
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
                bolRandEnable = Boolean.valueOf(strValue);
                iValue++;
                break;
                
            // Random number generator seed value
            case STR_RAND_SEED:
                lngSeed = Long.valueOf(strValue);                       // throws NumberFormatException
                iValue++;
                break;
                
            // Incremental generator seed value 
            case STR_INCR_START:
                incrStart = Long.valueOf(strValue);                     // throws NumberFormatException
                iValue++;
                break;
               
            // Incremental generator increment value
            case STR_INCR_VALUE:
                strIncrValue = strValue;
                iValue++;
                break;
            }
            
        } while (iValue < CNT_VALUES);  // keep parsing until all field labels are found
        
        // Check for missing field values 
        if (enmValueType == JalScalarType.UNSUPPORTED)
            throw ScalarFactoryConfig.missingFieldLabel(STR_TYPE);
        if (stringPref == null)
            throw ScalarFactoryConfig.missingFieldLabel(STR_PREF);
        if (bolRandEnable == null)
            throw ScalarFactoryConfig.missingFieldLabel(STR_RAND_ENBL);
        if (lngSeed == null)
            throw ScalarFactoryConfig.missingFieldLabel(STR_RAND_SEED);
        if (incrStart == null)
            throw ScalarFactoryConfig.missingFieldLabel(STR_INCR_START);
        if (strIncrValue == null)
            throw ScalarFactoryConfig.missingFieldLabel(STR_INCR_VALUE);
        
        if (enmValueType != JalScalarType.STRING)
            try {
                incrValue = (Number) enmValueType.parseValue(strIncrValue);
                
            } catch (Exception e) {
                throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() 
                        + " - Increment value " + strIncrValue + " cannot be converted to type " + enmValueType 
                        + ": " + e.getMessage());
            }
        else 
            incrValue = Integer.valueOf(strIncrValue);  // throws NumberFormatException

        // Create and return the record according to random number enable/disable flag
        if (bolRandEnable)
            return ScalarFactoryConfig.from(enmValueType, bolRandEnable, lngSeed, incrValue, stringPref);
        else
            return ScalarFactoryConfig.from(enmValueType, bolRandEnable, incrStart, incrValue, stringPref);
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
     * @return  a new <code>ScalarFactoryConfig</code> record populated with all default field values
     * 
     * @throws UnsupportedOperationException    unable to create <code>{@link #increment}</code> field for value type  
     */
    public static ScalarFactoryConfig   from() throws UnsupportedOperationException {
        return ScalarFactoryConfig.from(ENM_TYPE_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactoryConfig</code> record from the given arguments.
     * </p>
     * <p>
     * Record fields not supplied are taken from the JAL Tools default configuration.
     * <ul>
     * <li>Field <code>{@link #bolRandEnable()}</code> is taken directly from the JAL default configuration.</li>
     * <li>Field <code>{@link #seed()}</code> is determined by the random enable/disable flag.</li>
     * <li>Field <code>{@link #increment()}</code> is determined by the <code>JalScalarType</code> argument.</li>
     * <li>Field <code>{@link #strPrefix()}</code> is taken from the configuration <code>{@link #STR_PREFIX_DEF}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   enmValueType    the data type of the scalar values to generate
     * @param   seed            seed value for random number generation or start value for incremental value generation   
     * 
     * @return  a new <code>ScalarFactoryConfig</code> record populated with the given argument values
     * 
     * @throws UnsupportedOperationException    unable to create <code>{@link #increment}</code> field for value type  
     */
    public static ScalarFactoryConfig   from(JalScalarType enmValueType) throws UnsupportedOperationException {
        
        boolean bolRandEnable = BOL_RAND_ENBL_DEF;
        
        return ScalarFactoryConfig.from(enmValueType, bolRandEnable);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactoryConfig</code> record from the given arguments.
     * </p>
     * <p>
     * Record fields not supplied are taken from the JAL Tools default configuration.
     * <ul>
     * <li>Field <code>{@link #bolRandEnable()}</code> is taken directly from the JAL default configuration.</li>
     * <li>Field <code>{@link #increment()}</code> is determined by the <code>JalScalarType</code> argument.</li>
     * <li>Field <code>{@link #strPrefix()}</code> is taken from the configuration <code>{@link #STR_PREFIX_DEF}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   enmValueType    the data type of the scalar values to generate
     * @param   seed            seed value for random number generation or start value for incremental value generation   
     * 
     * @return  a new <code>ScalarFactoryConfig</code> record populated with the given argument values
     * 
     * @throws UnsupportedOperationException    unable to create <code>{@link #increment}</code> field for value type  
     */
    public static ScalarFactoryConfig   from(JalScalarType enmValueType, long seed) throws UnsupportedOperationException {
        
        boolean bolRandEnable = BOL_RAND_ENBL_DEF;
        
        return ScalarFactoryConfig.from(enmValueType, bolRandEnable, seed);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactoryConfig</code> record from the given arguments.
     * </p>
     * <p>
     * Record fields not supplied are taken from the JAL Tools default configuration.
     * <ul>
     * <li>Field <code>{@link #seed()}</code> is determined by the random enable/disable flag.</li>
     * <li>Field <code>{@link #increment()}</code> is determined by the <code>JalScalarType</code> argument.</li>
     * <li>Field <code>{@link #strPrefix()}</code> is taken from the configuration <code>{@link #STR_PREFIX_DEF}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   enmValueType    the data type of the scalar values to generate
     * @param   bolRandEnable   enable/disable the use of random number generation for scalar values
     * 
     * @return  a new <code>ScalarFactoryConfig</code> record populated with the given argument values
     * 
     * @throws UnsupportedOperationException    unable to create <code>{@link #increment}</code> field for value type  
     */
    public static ScalarFactoryConfig   from(JalScalarType enmValueType, boolean bolRandEnable) throws UnsupportedOperationException {
        
        long    seed;
        if (bolRandEnable)
            seed = LNG_RAND_SEED_DEF;
        else
            seed = LNG_INCR_SEED_DEF;
            
        return ScalarFactoryConfig.from(enmValueType, bolRandEnable, seed);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactoryConfig</code> record from the given arguments.
     * </p>
     * <p>
     * Record fields not supplied are taken from the JAL Tools default configuration.
     * <ul>
     * <li>Field <code>{@link #increment()}</code> is determined by the <code>JalScalarType</code> argument.</li>
     * <li>Field <code>{@link #strPrefix()}</code> is taken from the configuration <code>{@link #STR_PREFIX_DEF}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   enmValueType    the data type of the scalar values to generate
     * @param   bolRandEnable   enable/disable the use of random number generation for scalar values
     * @param   seed            seed value for random number generation or start value for incremental value generation   
     * 
     * @return  a new <code>ScalarFactoryConfig</code> record populated with the given argument values
     * 
     * @throws UnsupportedOperationException    unable to create <code>{@link #increment}</code> field for value type  
     */
    public static ScalarFactoryConfig   from(JalScalarType enmValueType, boolean bolRandEnable, long seed) throws UnsupportedOperationException {
        
        // Extract the incremental value from the JAL default parameters
        Number increment = switch (enmValueType) {
        case BOOLEAN -> Integer.valueOf(0);
        case INTEGER -> INT_INCR_DEF;
        case LONG -> LNG_INCR_DEF;
        case FLOAT -> FLT_INCR_DEF;
        case DOUBLE -> DBL_INCR_DEF;
        case STRING -> INT_STR_INCR_DEF;
        default -> throw new UnsupportedOperationException("Increment value not available for type: " + enmValueType);
        };
        
        return ScalarFactoryConfig.from(enmValueType, bolRandEnable, seed, increment);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactoryConfig</code> record from the given arguments.
     * </p>
     * <p>
     * Record fields not supplied are taken from the JAL Tools default configuration.
     * <ul>
     * <li>Field <code>{@link #strPrefix()}</code> is taken from the configuration <code>{@link #STR_PREFIX_DEF}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   enmValueType    the data type of the scalar values to generate
     * @param   bolRandEnable   enable/disable the use of random number generation for scalar values
     * @param   seed            seed value for random number generation or start value for incremental value generation   
     * @param   increment       numeric incremental value used when random generation is disabled (type depends upon data type)
     * 
     * @return  a new <code>ScalarFactoryConfig</code> record populated with the given argument values
     */
    public static ScalarFactoryConfig   from(JalScalarType enmValueType, boolean bolRandEnable, long seed, Number increment) {
        
        return ScalarFactoryConfig.from(enmValueType, bolRandEnable, seed, increment, STR_PREFIX_DEF); 
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactoryConfig</code> record from the given arguments.
     * </p>
     * <p>
     * This creator is equivalent to the canonical constructors.
     * </p>
     * 
     * @param   enmValueType    the data type of the scalar values to generate
     * @param   bolRandEnable   enable/disable the use of random number generation for scalar values
     * @param   seed            seed value for random number generation or start value for incremental value generation   
     * @param   increment       numeric incremental value used when random generation is disabled (type depends upon data type)
     * @param   strPrefix       prefix used for all string value generation (suffix given by integer value)
     * 
     * @return  a new <code>ScalarFactoryConfig</code> record populated with the given argument values
     */
    public static ScalarFactoryConfig   from(JalScalarType enmValueType, boolean bolRandEnable, long seed, Number increment, String strPrefix) {
        return new ScalarFactoryConfig(enmValueType, bolRandEnable, seed, increment, strPrefix);
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
     * <code>ScalarFactoryConfig</code> or the method returns <code>false</code>.
     * Note that the argument can be a different record instance but if all field values are equal then 
     * the method returns <code>true</code>.
     * </p>
     * 
     * @param   objCmp  object under equivalence comparison
     * 
     * @return  <code>true</code> if the argument is a <code>ScalarFactoryConfig</code> record with equal field values,
     *          <code>false</code> otherwise
     * 
     * @see Record#equals(Object)
     */
    @Override
    public boolean equals(Object objCmp) {
        
        if (objCmp instanceof ScalarFactoryConfig rec) {
            if (this.enmValueType == rec.enmValueType
                    && this.bolRandEnable == rec.bolRandEnable
                    && this.seed == rec.seed 
                    && this.increment == rec.increment
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
        str += "Scalar value type: " + this.enmValueType + "\n";
        str += "Random enabled   : " + this.bolRandEnable + "\n";
        str += "Seed value       : " + this.seed + "\n";
        str += "Increment value  : " + this.increment + "\n";
        
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
     * string in method <code>{@link ScalarFactoryConfig#parseYamlDoc(InputStream)}</code> of
     * the enclosing record.  Once parsed the class instance contains all the field values
     * of the <code>{@link ScalarFactoryConfig}</code> record.  The instance is then used
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
    public static final class ScalarFactoryYaml extends CfgStructure<ScalarFactoryYaml> {
        
        /**
         * <p>
         * Creates and returns a new <code>ScalarFactoryConfig</code> record populated with the attributes of this structure.
         * </p>
         * <p>
         * This instance must be fully populated before method invocation or an exception is thrown.
         * The assumed to be populated using a Snake YAML document parsing operation.
         * </p>
         * 
         * @return  a new <code>ScalarFactoryConfig</code> record populated with this structure's attributes
         * 
         * @throws IllegalStateException            method called before structure class was populated
         * @throws UnsupportedOperationException    unable to create <code>{@link #increment}</code> field for value type  
         */
        public ScalarFactoryConfig  createRecord() throws IllegalStateException, UnsupportedOperationException {
            
            //  Check state
            if (this.stringPrefix==null && type == JalScalarType.UNSUPPORTED)
                throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Structure class not populated.");

            long    lngSeed = (this.random.enabled) ? this.random.seed : this.increment.start;
            Number  increment = switch (this.type) {
            case INTEGER -> this.increment.value.intValue();
            case BOOLEAN -> this.increment.value.intValue();
            case DOUBLE -> this.increment.value.doubleValue();
            case FLOAT -> this.increment.value.floatValue();
            case LONG -> this.increment.value.longValue();
            case STRING -> this.increment.value.intValue();
            case UNSUPPORTED -> throw new UnsupportedOperationException("Increment value unsupported for type: " + this.type);
            };
            
            return ScalarFactoryConfig.from(type, this.random.enabled, lngSeed, increment, stringPrefix);
        }
        
        /** Default constructor required from base class */
        public ScalarFactoryYaml() { super(ScalarFactoryYaml.class); };
        
        @ACfgOverride.Field(name="stringPrefix")
        public String           stringPrefix = null;
        
        @ACfgOverride.Field(name="enmValueType")
        public JalScalarType    type = JalScalarType.UNSUPPORTED;
        
        @ACfgOverride.Struct(pathelem="random")
        public Random           random;
        
        @ACfgOverride.Struct(pathelem="increment")
        public Increment        increment;
        
        
        public static final class Random extends CfgStructure<Random> {
            
            /** Default constructor required of base class */
            public Random() { super(Random.class); };
            
            @ACfgOverride.Field(name="enabled")
            public Boolean      enabled;
            
            @ACfgOverride.Field(name="seed")
            public Long         seed;
        };
        
        public static final class Increment extends CfgStructure<Increment> {
            
            /** Default constructor required of base class */
            public Increment()  { super(Increment.class); };
            
            
            @ACfgOverride.Field(name="start")
            public Long         start;
            
            @ACfgOverride.Field(name="value")
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
    private final static JalScalarType  ENM_TYPE_DEF = CFG_DEF.type;
    
    /** The default string value prefix */
    private final static String         STR_PREFIX_DEF = CFG_DEF.stringPrefix;

    
    /** The default enable/disable random number generator */
    public static final boolean BOL_RAND_ENBL_DEF = CFG_DEF.random.enabled;

    /** The default random number generator seed value */
    public static final long    LNG_RAND_SEED_DEF = CFG_DEF.random.seed;

    /** The default incremental seed value */
    public static final long    LNG_INCR_SEED_DEF = CFG_DEF.increment.seed;
    
    
    //
    // Class Constants - Scalar increment values
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
    
//    /**
//     * <p>
//     * Perform a direct parsing of an input stream in YAML format.
//     * </p>
//     * 
//     * @param is
//     * 
//     * @return
//     * 
//     * @throws IOException
//     * @throws IndexOutOfBoundsException
//     * @throws TypeNotPresentException
//     * @throws ConfigurationException
//     */
//    public static ScalarFactoryConfig   parseYamlNode(InputStream is) throws IOException, IndexOutOfBoundsException, TypeNotPresentException, ConfigurationException {
//
//        // Line delimiters
//        final String    STR_DELM = ":";
//        final String    STR_CMMT = "#";
//        
//        // Field labels
//        final String    STR_TYPE = "type";
//        final String    STR_PREF = "stringPrefix";
//        final String    STR_RAND = "random";
//        final String    STR_INCR = "increment";
//        
//        // Random number field labels
//        final String    STR_RAND_ENBL = "enabled";
//        final String    STR_RAND_SEED = "seed";
//        
//        // Increment value field labels
//        final String    STR_INCR_START = "start";
//        final String    STR_INCR_VALUE = "value";
//        
//        // Field values
//        JalScalarType   enmValueType;
//        String          stringPref;
//        Boolean         bolRandEnable;
//        long            seedRand;
//        Number          increment;
//        long            seedIncr;
//        
//        // Create a reader for the input stream for line-based parsing
//        InputStreamReader   rdrStrm = new InputStreamReader(is);
//        BufferedReader      rdrBuff = new BufferedReader(rdrStrm);
//
//        for (int iGroup=0; iGroup<4; iGroup++) {
//            String      strLine = rdrBuff.readLine();               // throws IOException
//            String[]    arrTokens = strLine.split(STR_DELM);
//            
//            switch (arrTokens[0]) {
//            
//            // The scalar value type
//            case STR_TYPE:
//                String  strTypeNm = arrTokens[1].split(STR_CMMT)[0];    // throws IndexOutofBoundsException
//                enmValueType = JalScalarType.getConstant(strTypeNm);    // throws TypeNotPresentException
//                break;
//                
//            // The string prefix (for string types) 
//            case STR_PREF:
//                stringPref = arrTokens[1].split(STR_CMMT)[0];           // throws IndexOutOfBoundsException   
//                break;
//               
//            // The random number generator parameters
//            case STR_RAND:
//                for (int i=0; i<2; i++) {
//                    strLine = rdrBuff.readLine();
//                    arrTokens = strLine.split(STR_DELM);
//                    switch (arrTokens[0]) {
//                    case STR_RAND_ENBL:
//                        String  strBolRandEnable = arrTokens[1].split(STR_CMMT)[0]; // throws IndexOutOfBoundsException
//                        bolRandEnable = Boolean.valueOf(strBolRandEnable);
//                        break;
//                    case STR_RAND_SEED:
//                        String strSeedRand = arrTokens[1].split(STR_CMMT)[0];       // throws IndexOutOfBoundsException
//                        seedRand = Long.valueOf(strSeedRand);                       // throws NumberFormatException
//                        break;
//                    default:
//                        throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() + " - Unknown " + STR_RAND + " parameter: " + arrTokens[0]);
//                    }
//                }
//                break;
//                
//            // The incremental number generator parameters
//            case STR_INCR:
//                for (int i=0; i<2; i++) {
//                    strLine = rdrBuff.readLine();
//                    arrTokens = strLine.split(STR_DELM);
//                    switch (arrTokens[0]) {
//                    case STR_INCR_START: 
//                        String strIncrSeed = arrTokens[1].split(STR_CMMT)[0];       // throws IndexOutOfBoundsException
//                        seedIncr = Long.valueOf(strIncrSeed);                       // throws NumberFormatException
//                        break;
//                    case STR_INCR_VALUE:
//                        String strIncrValue = arrTokens[1].split(STR_CMMT)[0];      // throws IndexOutOfBoundsException
//                        if (enmValueType != JalScalarType.STRING)
//                            increment = (Number) enmValueType.parseValue(strIncrValue);
//                        else 
//                            increment = Integer.valueOf(strIncrValue);
//                        break;
//                    default:
//                        throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() + " - Unknown " + STR_RAND + " parameter: " + arrTokens[0]);
//                    }
//                }
//                break;
//                
//            default:
//                throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() + " - Unknown parameter: " + arrTokens[0]);
//            }
//        }   // for iGroup
//
//        // Create and return the configuration record
//        if (bolRandEnable) 
//            return ScalarFactoryConfig.from(enmValueType, bolRandEnable, seedRand, increment, stringPref);
//        else
//            return ScalarFactoryConfig.from(enmValueType, bolRandEnable, seedIncr, increment, stringPref);
//    }
    
}
