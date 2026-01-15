/*
 * Project: dp-jal
 * File:	ScalarFactory.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.values
 * Type: 	ScalarFactory
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
 * @since Nov 6, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.values;

import java.util.Random;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.IScalarFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.ScalarFactorySpec;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.values.JalToolsScalarFactoryConfig;

/**
 * <p>
 * Generates a sequence of scalar values either randomly or incrementally.
 * </p> 
 * <p>
 * Creates a sequence of scalar values meant to supply artificial, or "simulated", data.  Scalar value types
 * supported are all those enumerated in <code>{@link JalScalarType}</code>.  There are 2 basic types of 
 * <code>ScalarFactory</code> configurations: 1) random sequence factories, and 2) incremental sequence factories.
 * <ol>
 * <li>Random Factories - produce sequences of random data values using a random number generator.</li>
 * <li>Incremental Factories - produce incremental sequences of data values starting from a seed value.</li>
 * </ol>
 * Random factories can be more computationally expensive due to their use of an internal random
 * number generator.  This condition can significant when creating large data sets.  Best practice is to prefer
 * incremental factories, especially when simulated data values are essentially irrelevant.  
 * </p>
 * <p>
 * <h2>Configuration</h2>
 * A <code>ScalarFactory</code> instance requires 5 parameters for configuration.  
 * <ol>
 * <li>'enmType' = the datum type for the simulated data, specified as a <code>{@link JalScalarType}</code> constant.</li>
 * <li>'bolRandEnbl' = enable/disable flag; <code>true</code> indicates random factory, <code>false</code> indicates incremental factory.</li>
 * <li>'lngSeed' = seed value for random number generator (0 indicates 'random' seed), or 1st value for incremental sequence.</li>
 * <li>'numIncr' = the increment value for incremental factories, this value is ignored for random factories.</li>
 * <li>'strPrefix' = the prefix given to string values when 'enmType' = <code>{@link JalScalarType#STRING}</code>, ignored for all others.</li>
 * </ol>
 * Note that the parameters are interpreted differently, or ignored completely, depending upon the value of parameters 
 * 'enmType' and 'bolRandEnbl'. 
 * </p>
 * <p>
 * <h2>Specification Records</h2>
 * The <code>ScalarFactory</code> class instances can be configured by <code>{@link ScalarFactorySpec}</code> records, 
 * which can also be used for creation/construction and parsing of application command-line arguments.  
 * This record contains all parameters required for instance configuration.  The
 * record is able to configure scalar factories for a wide variety of situations.  See the record documentation
 * for <code>{@link ScalarFactorySpec}</code> for instruction on record creation and configuration of scalar
 * factory instances. 
 * </p>
 * <p>
 * <h2>Simulated Data</h2>
 * Scalar value sequences have the following properties:
 * <ul>
 * <li>All scalar values are returns as Java <code>Object</code> instances.</li>
 * <li>Scalar value types (i.e. <code>Object</code> types) are determined by enumeration <code>{@link JalScalarType}</code>.</li>
 * <li>Sequences are generated incrementally or randomly according to configuration at creation/construction.</li>
 * </ul>
 * Values in the sequence are obtained using repeated invocations of <code>{@link #nextDatum()}</code>.
 * The data type of the simulated data is obtained as a <code>{@link DpSupportedType}</code> enumeration constant
 * through the method <code>{@link #getDatumType()}</code>
 * </p>  
 *
 * @author Christopher K. Allen
 * @since Nov 6, 2025
 *
 * @see ScalarFactorySpec
 * @see IScalarFactory
 */
public class ScalarFactory implements IScalarFactory {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactory</code> instance configured with all default parameters.
     * </p>
     * <p>
     * This creator uses all default parameter values from the JAL Tools default configuration.  The following
     * parameters are supplied from the default configuration:
     * <ul>
     * <li>'enmType' = <code>{@link #ENM_TYPE_DEF}</code>.</li>
     * <li>'bolRandEnbl' = <code>{@link #BOL_RAND_ENBL_DEF}</code>.</li>
     * <li>'lngSeed' is dependent upon the value of 'bolRandEnbl'. We have the following:
     *   <ul>
     *   <li>'bolRandEnbl' = <code>false</code> &rarr; 'lngSeed' = <code>{@link #LNG_INCR_SEED_DEF}</code>.</li>
     *   <li>'bolRandEnbl' = <code>true</code> &rarr; 'numIncr' = <code>{@link #LNG_RAND_SEED_DEF}</code>.</li>
     *   </ul>
     * </li>
     * <li>'numIncr' is dependent upon the value of 'enmType'. For example, we have the following:
     *   <ul>
     *   <li>'enmType' = <code>{@link JalScalarType#BOOLEAN}</code> &rarr; 'numIncr' = <code>{@link #BOL_INCR_DEF}</code>.</li>
     *   <li>'enmType' = <code>{@link JalScalarType#INTEGER}</code> &rarr; 'numIncr' = <code>{@link #INT_INCR_DEF}</code>.</li>
     *   <li>'enmType' = <code>{@link JalScalarType#LONG}</code> &rarr; 'numIncr' = <code>{@link #LONG_INCR_DEF}</code>.</li>
     *   <li> &#8942; </li>
     *   <li>'enmType' = <code>{@link JalScalarType#STRING}</code> &rarr; 'numIncr' = <code>{@link #INT_STR_INCR_DEF}</code>.</li>
     *   </ul>
     * </li>
     * <li>'strPrefix' = <code>{@link #STR_PREFIX_DEF}</code>.</li>
     * </ul>  
     * Note that when the parameter 'bolRandEnbl' is set to <code>true</code> the 
     * parameter 'numIncr' is ignored but populated according to the above. 
     * </p>
     * <p>
     * The new <code>ScalarFactory</code> instance is fully configured and ready for simulated scalar value
     * creation using the <code>{@link IDataFactory}</code> interface.
     * </p>
     *
     * @return  a new <code>ScalarFactory</code> instance ready for simulated data production
     */
    public static ScalarFactory from() {

        return ScalarFactory.from(ENM_TYPE_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactory</code> instance configured with the given argument(s).
     * </p>
     * <p>
     * This creator uses default parameter values from the JAL Tools default configuration.  The following
     * parameters are supplied from the default configuration:
     * <ul>
     * <li>'bolRandEnbl' = <code>{@link #BOL_RAND_ENBL_DEF}</code>.</li>
     * <li>'lngSeed' is dependent upon the value of 'bolRandEnbl'. We have the following:
     *   <ul>
     *   <li>'bolRandEnbl' = <code>false</code> &rarr; 'lngSeed' = <code>{@link #LNG_INCR_SEED_DEF}</code>.</li>
     *   <li>'bolRandEnbl' = <code>true</code> &rarr; 'numIncr' = <code>{@link #LNG_RAND_SEED_DEF}</code>.</li>
     *   </ul>
     * </li>
     * <li>'numIncr' is dependent upon the value of 'enmType'. For example, we have the following:
     *   <ul>
     *   <li>'enmType' = <code>{@link JalScalarType#BOOLEAN}</code> &rarr; 'numIncr' = <code>{@link #BOL_INCR_DEF}</code>.</li>
     *   <li>'enmType' = <code>{@link JalScalarType#INTEGER}</code> &rarr; 'numIncr' = <code>{@link #INT_INCR_DEF}</code>.</li>
     *   <li>'enmType' = <code>{@link JalScalarType#LONG}</code> &rarr; 'numIncr' = <code>{@link #LONG_INCR_DEF}</code>.</li>
     *   <li> &#8942; </li>
     *   <li>'enmType' = <code>{@link JalScalarType#STRING}</code> &rarr; 'numIncr' = <code>{@link #INT_STR_INCR_DEF}</code>.</li>
     *   </ul>
     * </li>
     * <li>'strPrefix' = <code>{@link #STR_PREFIX_DEF}</code>.</li>
     * </ul>  
     * Note that when the parameter 'bolRandEnbl' is set to <code>true</code> the 
     * parameter 'numIncr' is ignored but populated according to the above. 
     * </p>
     * <p>
     * The new <code>ScalarFactory</code> instance is fully configured and ready for simulated scalar value
     * creation using the <code>{@link IDataFactory}</code> interface.
     * </p>
     *
     * @param enmType       the data type of the simulated data produced
     * 
     * @return  a new <code>ScalarFactory</code> instance ready for simulated data production
     */
    public static ScalarFactory from(JalScalarType enmType) {

        return ScalarFactory.from(enmType, BOL_RAND_ENBL_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactory</code> instance configured with the given argument(s).
     * </p>
     * <p>
     * This creator uses default parameter values from the JAL Tools default configuration.  The following
     * parameters are supplied from the default configuration:
     * <ul>
     * <li>'lngSeed' is dependent upon the value of 'bolRandEnbl'. We have the following:
     *   <ul>
     *   <li>'bolRandEnbl' = <code>false</code> &rarr; 'lngSeed' = <code>{@link #LNG_INCR_SEED_DEF}</code>.</li>
     *   <li>'bolRandEnbl' = <code>true</code> &rarr; 'numIncr' = <code>{@link #LNG_RAND_SEED_DEF}</code>.</li>
     *   </ul>
     * </li>
     * <li>'numIncr' is dependent upon the value of 'enmType'. For example, we have the following:
     *   <ul>
     *   <li>'enmType' = <code>{@link JalScalarType#BOOLEAN}</code> &rarr; 'numIncr' = <code>{@link #BOL_INCR_DEF}</code>.</li>
     *   <li>'enmType' = <code>{@link JalScalarType#INTEGER}</code> &rarr; 'numIncr' = <code>{@link #INT_INCR_DEF}</code>.</li>
     *   <li>'enmType' = <code>{@link JalScalarType#LONG}</code> &rarr; 'numIncr' = <code>{@link #LONG_INCR_DEF}</code>.</li>
     *   <li> &#8942; </li>
     *   <li>'enmType' = <code>{@link JalScalarType#STRING}</code> &rarr; 'numIncr' = <code>{@link #INT_STR_INCR_DEF}</code>.</li>
     *   </ul>
     * </li>
     * <li>'strPrefix' = <code>{@link #STR_PREFIX_DEF}</code>.</li>
     * </ul>  
     * Note that when the parameter 'bolRandEnbl' is set to <code>true</code> the 
     * parameter 'numIncr' is ignored but populated according to the above. 
     * </p>
     * <p>
     * The new <code>ScalarFactory</code> instance is fully configured and ready for simulated scalar value
     * creation using the <code>{@link IDataFactory}</code> interface.
     * </p>
     *
     * @param enmType       the data type of the simulated data produced
     * @param bolRandEnbl   enable/disable the use of random sequence generation, <code>false</code> indicates an incremental factory
     * 
     * @return  a new <code>ScalarFactory</code> instance ready for simulated data production
     */
    public static ScalarFactory from(JalScalarType enmType, boolean bolRandEnbl) {

        long    lngSeed;
        if (bolRandEnbl)
            lngSeed = LNG_RAND_SEED_DEF;
        else
            lngSeed = LNG_INCR_SEED_DEF;
            
        return ScalarFactory.from(enmType, bolRandEnbl, lngSeed);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactory</code> instance configured with the given argument(s).
     * </p>
     * <p>
     * This creator uses default parameter values from the JAL Tools default configuration.  The following
     * parameters are supplied from the default configuration:
     * <ul>
     * <li>'numIncr' is dependent upon the value of 'enmType'. For example, we have the following:
     *   <ul>
     *   <li>'enmType' = <code>{@link JalScalarType#BOOLEAN}</code> &rarr; 'numIncr' = <code>{@link #BOL_INCR_DEF}</code>.</li>
     *   <li>'enmType' = <code>{@link JalScalarType#INTEGER}</code> &rarr; 'numIncr' = <code>{@link #INT_INCR_DEF}</code>.</li>
     *   <li>'enmType' = <code>{@link JalScalarType#LONG}</code> &rarr; 'numIncr' = <code>{@link #LONG_INCR_DEF}</code>.</li>
     *   <li> &#8942; </li>
     *   <li>'enmType' = <code>{@link JalScalarType#STRING}</code> &rarr; 'numIncr' = <code>{@link #INT_STR_INCR_DEF}</code>.</li>
     *   </ul>
     * </li>
     * <li>'strPrefix' = <code>{@link #STR_PREFIX_DEF}</code>.</li>
     * </ul>  
     * Note that when the parameter 'bolRandEnbl' is set to <code>true</code> the 
     * parameter 'numIncr' is ignored but populated according to the above. 
     * </p>
     * <p>
     * The new <code>ScalarFactory</code> instance is fully configured and ready for simulated scalar value
     * creation using the <code>{@link IDataFactory}</code> interface.
     * </p>
     *
     * @param enmType       the data type of the simulated data produced
     * @param bolRandEnbl   enable/disable the use of random sequence generation, <code>false</code> indicates an incremental factory
     * @param lngSeed       seed value for random number generator (0 indicates 'random' seed) or 1st incremental value
     * 
     * @return  a new <code>ScalarFactory</code> instance ready for simulated data production
     */
    public static ScalarFactory from(JalScalarType enmType, boolean bolRandEnbl, long lngSeed) {
        
        // Extract the incremental value from the JAL default parameters
        Number numIncr = switch (enmType) {
        case BOOLEAN -> INT_BOL_INCR_DEF;
        case INTEGER -> INT_INCR_DEF;
        case LONG -> LNG_INCR_DEF;
        case FLOAT -> FLT_INCR_DEF;
        case DOUBLE -> DBL_INCR_DEF;
        case STRING -> INT_STR_INCR_DEF;
        case UNSUPPORTED -> throw new UnsupportedOperationException("Unimplemented case: " + enmType);
        default -> throw new IllegalArgumentException("Unexpected value: " + enmType);
        };
        
        return ScalarFactory.from(enmType, bolRandEnbl, lngSeed, numIncr);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactory</code> instance configured with the given argument(s).
     * </p>
     * <p>
     * This creator uses default parameter values from the JAL Tools default configuration.  The following
     * parameters are supplied from the default configuration:
     * <ul>
     * <li>'strPrefix' = <code>{@link #STR_PREFIX_DEF}</code>.</li>
     * </ul>  
     * Note that when the parameter 'bolRandEnbl' is set to <code>true</code> the 
     * parameter 'numIncr' is ignored. 
     * </p>
     * <p>
     * The new <code>ScalarFactory</code> instance is fully configured and ready for simulated scalar value
     * creation using the <code>{@link IDataFactory}</code> interface.
     * </p>
     *
     * @param enmType       the data type of the simulated data produced
     * @param bolRandEnbl   enable/disable the use of random sequence generation, <code>false</code> indicates an incremental factory
     * @param lngSeed       seed value for random number generator (0 indicates 'random' seed) or 1st incremental value
     * @param numIncr       increment value for incremental factories, ignored for random factories
     * 
     * @return  a new <code>ScalarFactory</code> instance ready for simulated data production
     */
    public static ScalarFactory from(JalScalarType enmType, boolean bolRandEnbl, long lngSeed, Number numIncr) {
     
        return ScalarFactory.from(enmType, bolRandEnbl, lngSeed, numIncr, STR_PREFIX_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactory</code> instance configured with the given arguments.
     * </p>
     * <p>
     * This creator is equivalent to the default (canonical) constructor 
     * <code>{@link #ScalarFactory(JalScalarType, boolean, long, Number, String)}</code> requiring all configuration 
     * parameter values.  There are no default values.
     * </p>
     * <p>
     * Note that when the parameter 'bolRandEnbl' is set to <code>true</code> the 
     * parameter 'numIncr' is ignored but populated according to the above. 
     * </p> 
     * <p>
     * The new <code>ScalarFactory</code> instance is fully configured and ready for simulated scalar value
     * creation using the <code>{@link IDataFactory}</code> interface.
     * </p>
     *
     * @param enmType       the data type of the simulated data produced
     * @param bolRandEnbl   enable/disable the use of random sequence generation, <code>false</code> indicates an incremental factory
     * @param lngSeed       seed value for random number generator (0 indicates 'random' seed) or 1st incremental value
     * @param numIncr       increment value for incremental factories, ignored for random factories
     * @param strPrefix     prefix given to all string values when <code>enmType = {@link JalScalarType#STRING}</code>
     * 
     * @return  a new <code>ScalarFactory</code> instance ready for simulated data production
     */
    public static ScalarFactory from(JalScalarType enmType, boolean bolRandEnbl, long lngSeed, Number numIncr, String strPrefix) {
     
        return new ScalarFactory(enmType, bolRandEnbl, lngSeed, numIncr, strPrefix);
    }

    /**
     * <p>
     * Parses argument string array to identify and create a <code>ScalarFactory</code> instance.
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
     * <li>'bolRandEnbl' is the <code>{@link #bolRandEnbl</code> attribute,</li>
     * <li>'lngSeed' is the <code>{@link #lngSeed}</code> attribute,</li>
     * <li>'numIncr' is the <code>{@link #numIncr}</code> attribute,</li>
     * <li>'strPrefix' is the <code>{@link #strPrefix}</code> attribute.</li>
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
     * <code>ScalarFactory</code> given by <code>{@link #from()}</code>.  This configuration is completely
     * determined by the JAL Tools default configuration for scalar value factories. 
     *  
     * @param args  argument string defining the scalar factory
     * 
     * @return  a new configuration record populated by the parsed argument elements
     * 
     * @throws TypeNotPresentException          the 1st element was not a <code>JalScalarType</code> enumeration constant
     * @throws NumberFormatException            invalid numeric format (e.g., 'lngSeed', 'numIncr')
     * @throws UnsupportedOperationException    unable to create <code>{@link #numIcr}</code> field for numeric value type  
     */
    public static ScalarFactory parse(String...args) throws TypeNotPresentException, NumberFormatException, UnsupportedOperationException {

        // Check argument length
        if (args.length < 1)
            return ScalarFactory.from();

        // Get the value type of the scalars to generate
        String          strValueType = args[0];
        JalScalarType   enmValueType = JalScalarType.valueFrom(strValueType);     // throws TypeNotPresentException
        
        // --- Parse the random number generation enable/disable flag ---
        if (args.length < 2)
            return ScalarFactory.from(enmValueType);
        Boolean bolRandEnable = Boolean.valueOf(args[1]);
        
        // --- Parse the lngSeed field value ---
        if (args.length < 3)
            return ScalarFactory.from(enmValueType, bolRandEnable);
        Long    lngSeed = Long.valueOf(args[2]);    // throws NumberFormatException
        
        // --- Parse the increment field value ---
        if (args.length < 4)
            return ScalarFactory.from(enmValueType, bolRandEnable, lngSeed);
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
            return ScalarFactory.from(enmValueType, bolRandEnable, lngSeed, numIncr);
        
        String  strPrefix = args[4];
        return ScalarFactory.from(enmValueType, bolRandEnable, lngSeed, numIncr, strPrefix);
    }
    
    
    //
    // Library Resources
    //
    
    /** The default parameters for scalar-valued simulated data generation */
    private static final JalToolsScalarFactoryConfig     CFG_DEF = JalToolsConfig.getInstance().datagen.values.scalar;
    
    
    //
    // Constants - Default Arguments
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
    // Constants - Scalar increment values
    //
    
    /** Boolean value default increment */
    public final static Integer     INT_BOL_INCR_DEF = CFG_DEF.increment.booleanv;
    
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
    // Resources
    //
    
    /** Random number generator used to create random values */
    private final Random        facRandom;
    

    //
    // Configuration
    //
    
    /** String value prefix (suffix is numeric) */
    private final String        strPrefix;
    

    /** The field value type */
    private final JalScalarType enmType;
    
    /** Random generated value generation enabled/disabled */
    private final boolean       bolRandEnbl;
    
    /** The seed value for generated value sequence (random number generation or 1st value) */
    private final long          lngSeed;
    
    /** The increment value for incremental scalar factories */
    private final Number        numIncr;

    
    /** Boolean increment value */
    private final Integer       bolIncr;
    
    /** Integer increment value */
    private final Integer       intIncr;
    
    /** Long increment value */
    private final Long          lngIncr;
    
    /** Float increment value */
    private final Float         fltIncr;
    
    /** Double increment value */
    private final Double        dblIncr;
    
    /** String increment value */
    private final Integer       strIncr;
    
    
    //
    // State Variables
    //
    
    /** Current Boolean value (incremental) */
    private Boolean     bolValue = false;
    
    /** Current Integer value (incremental) */
    private Integer     intValue = 0;
    
    /** Current Long value (incremental) */
    private Long        lngValue = 0L;
    
    /** Current Float value (incremental) */
    private Float       fltValue = 0.0F;
    
    /** Current Double value (incremental) */
    private Double      dblValue = 0.0;
    
    /** Current string suffix (incremental) */
    private Integer     strValue = 0;
    
    
    //
    // Constructors 
    //
    
    /**
     * <p>
     * Constructs a new <code>ScalarFactory</code> instance configured according to the given arguments.
     * </p>
     * <p>
     * The new <code>ScalarFactory</code> instance is fully configured and ready for simulated scalar value
     * creation using the <code>{@link IDataFactory}</code> interface.
     * </p>
     *
     * @param enmType       the data type of the simulated data produced
     * @param bolRandEnbl   enable/disable the use of random sequence generation, <code>false</code> indicates an incremental factory
     * @param lngSeed       seed value for random number generator (0 indicates 'random' seed) or 1st incremental value
     * @param numIncr       increment value for incremental factories, ignored for random factories
     * @param strPrefix     prefix given to all string values when <code>enmType = {@link JalScalarType#STRING}</code>
     */
    public ScalarFactory(JalScalarType enmType, boolean bolRandEnbl, long lngSeed, Number numIncr, String strPrefix) {
        this.enmType = enmType;
        this.bolRandEnbl = bolRandEnbl;
        this.lngSeed = lngSeed;
        this.numIncr = numIncr;
        this.strPrefix = strPrefix;
        
        this.bolIncr = numIncr.intValue() % 2;
        this.intIncr = numIncr.intValue();
        this.lngIncr = numIncr.longValue();
        this.fltIncr = numIncr.floatValue();
        this.dblIncr = numIncr.doubleValue();
        this.strIncr = numIncr.intValue();
        
        this.facRandom = this.initRandomGenator(bolRandEnbl, lngSeed);
        this.initCurrentValues(bolRandEnbl, lngSeed);
    }

    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Determines whether or not the scalar values are generated randomly.
     * </p>
     * <p>
     * Scalar values are generated using a random number generator if the method returns <code>true</code>.
     * Otherwise, the values are generated incrementally according to the configuration record
     * provided at creation/construction.
     * </p>
     * 
     * @return  <code>true</code> if scalar values are generated randomly, 
     *          <code>false</code> if they are generated incrementally
     */
    public boolean  isRandom() {
        return this.bolRandEnbl;
    }
    
    /**
     * <p>
     * Returns the seed parameter provided at creation/construction.
     * </p>
     * 
     * @return  the seed value used for random number generator or 1st incremental value
     */
    public long getSeed() {
        return this.lngSeed;
    }
    
    /**
     * <p>
     * Returns the numeric increment parameter provided at creation/construction.
     * </p>
     * 
     * @return  the numeric increment value used for incremental factories
     */
    public Number   getIncrement() {
        return this.numIncr;
    }
    
    /**
     * <p>
     * Returns the prefix used for string value generation.
     * </p>
     * <p>
     * The returned value only has context when the scalar factory is configured for string value generation.
     * The configuration is confirmed when the method <code>{@link #getScalarType()}</code> 
     * returns <code>{@link JalScalarType#STRING}</code>.
     * </p>
     * <p>
     * Note that string values are the concatenation of the returned value with an index integer which is
     * generated incrementally or randomly depending upon the scalar factory configuration.
     * </p>
     * 
     * @return  prefix all all generated string values when this factory produces string values
     */
    public String   getStringPrefix() {
        return this.strPrefix;
    }

    
    //
    // IScalarFactory Interface
    //
    
    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IScalarFactory#getDatumType()
     */
    @Override
    public DpSupportedType  getDatumType() {
        return this.getScalarType().getDpType();
    }
    
    /**
     * <p>
     * Returns the scalar value type produced by this scalar factory.
     * </p>
     * 
     * @return  the scalar value type as represented by a <code>{@link JalScalarType}</code> constant
     */
    @Override
    public JalScalarType    getScalarType() {
        return this.enmType;
    }
    
    /**
     * <p>
     * Returns the next simulated scalar value in the sequence according to the internal configuration.
     * </p>
     * 
     * @return  next scalar value as a Java <code>Object</code>
     * 
     * @throws  UnsupportedOperationException   the scalar type is <code>{@link JalScalarType#UNSUPPORTED}</code>
     */
    @Override
    public Object nextDatum() throws UnsupportedOperationException {
        
        // Get the current value as object
        // - This should be the seed value if first invocation and random=false
        Object objCurr = this.currentValue();   // throws UnsupportedOperationException
        
        // Increment to the next value
        if (this.bolRandEnbl)
            this.nextRandomValue();             // throws UnsupportedOperationException
        else
            this.nextIncrementalValue();        // throws UnsuppotedOperationException
        
        // Return the current object
        return objCurr;
    }
    
    
    //
    // Object Overrides
    //
    
    /**
     * <p>
     * Provides an equivalence comparison of this scalar factory against the given scalar factory.
     * </p>
     * <p>
     * The argument is first check for correct type <code>ScalarFactory</code> after which the 
     * configuration is compared against this configuration.  The state variable of the two 
     * factories are ignored.  
     * </p>
     *  
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScalarFactory fac) {
            boolean bolResult = this.strPrefix.equals(fac.strPrefix)
                            && (this.enmType == fac.enmType)
                            && (this.bolRandEnbl == fac.bolRandEnbl)
                            && (this.lngSeed == fac.lngSeed)
                            && (this.numIncr.equals(fac.numIncr));
            
            return bolResult;
        }
        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder   buf = new StringBuilder();
        
        buf.append("IDatumFactory implementation       : " + this.getClass().getName() + "\n");
        buf.append("Scalar value type                  : " + this.enmType + "\n");
        buf.append("Random value generation enabled    : " + this.bolRandEnbl + "\n");
        buf.append("Seed value (incremental or random) : " + this.lngSeed + "\n");
        buf.append("Increment value                    : " + this.numIncr + "\n");
        buf.append("String value prefix (if used)      : " + this.strPrefix + "\n");
        
        return buf.toString();
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Initializes the random number generator according to the argument values.
     * </p>
     * <p>
     * The returned value is given according to the following conditions on the argument:
     * <ul>
     * <li><code>{@link bolRandEnbl}</code> = <code>false</code>: <code>null</code>, </li>
     * <li><code>{@link bolRandEnbl}</code> = <code>true</code>:
     *     <ul>
     *     <li><code>{@link lngSeed}</code> = <code>0</code>: new <code>Random</code> instance with random seed.</li>
     *     <li><code>{@link lngSeed}</code> &ne; <code>0</code>: new <code>Random</code> instance with given seed value.</li>
     *     </ul>
     * </li>
     * </ul>
     * <p>
     * 
     * @implSpec
     * This method must be invoked <em>before</em> <code>{@link #initCurrentValues(long)}</code>. 
     * If random number generation is enabled that method requires the <code>{@link #facRandom}</code>
     * resource for initial value generation.  
     * 
     * @param bolRandEnbl   enable/disable flag for random number generation
     * @param lngSeed       seed value for random number generator if random enabled (0L indicates "random" seed)
     * 
     * @return  new <code>Random</code> instance, or <code>null</code> if <code>bolRandEnbl</code> is <code>false</code> 
     */
    private Random  initRandomGenator(boolean bolRandEnbl, long lngSeed) {
        
        // If random number generation is disabled return null
        if (!bolRandEnbl)
            return null;
        
        // Create random number generator according to seed value
        if (lngSeed == 0L)
            return new Random();
        
        return new Random(lngSeed);
    }
    
    /**
     * <p>
     * Initializes all sequence values for incremental scalar generation.
     * </p>
     * 
     * @param lngIncrSeed   the initial value for the scalar sequence  
     * 
     * @throws  ArithmeticException the argument was too large to convert to an integer (overflow condition)
     */
    private void initCurrentValues(boolean bolRandEnbl, long lngSeed) throws ArithmeticException {

        if (bolRandEnbl) {
            this.bolValue = this.facRandom.nextBoolean();
            this.intValue = this.facRandom.nextInt();
            this.lngValue = this.facRandom.nextLong();
            this.fltValue = this.facRandom.nextFloat();
            this.dblValue = this.facRandom.nextDouble();
            this.strValue = this.facRandom.nextInt();
            
        } else {
            this.bolValue = (Math.toIntExact(lngSeed) % 2 == 0) ? false : true;
            this.intValue = Math.toIntExact(lngSeed);   // throws ArithmeticException
            this.lngValue = lngSeed;
            this.fltValue = (float)lngSeed;
            this.dblValue = (double)lngSeed;
            this.strValue = Math.toIntExact(lngSeed);   // throws ArithmeticException
        }
    }
    
    /**
     * <p>
     * Returns the current scalar value as a Java <code>Object</code>.
     * </p>
     * 
     * @return  current scalar value
     * 
     * @throws  UnsupportedOperationException   the scalar type is <code>{@link JalScalarType#UNSUPPORTED}</code>
     */
    private Object currentValue() throws UnsupportedOperationException {
        
        return switch (this.enmType) {
        case BOOLEAN -> this.bolValue;
        case INTEGER -> this.intValue;
        case LONG -> this.lngValue;
        case FLOAT -> this.fltValue;
        case DOUBLE -> this.dblValue;
        case STRING -> this.strPrefix + Integer.toString(this.strValue);
        case UNSUPPORTED -> throw new UnsupportedOperationException("Unsupported type case: " + this.enmType);
        };
    }
    
    /**
     * <p>
     * Generate the next scalar value incrementally according to value type and stores it.
     * </p>
     * 
     * @throws  UnsupportedOperationException   the scalar type is <code>{@link JalScalarType#UNSUPPORTED}</code>
     */
    private void nextIncrementalValue() throws UnsupportedOperationException {
        
        switch (this.enmType) {
        case BOOLEAN:
            this.bolValue = (this.bolIncr==0) ? this.bolValue : !this.bolValue;
            break;
        case INTEGER:
            this.intValue += this.intIncr;
            break;
        case LONG: 
            this.lngValue += this.lngIncr;
            break;
        case FLOAT:
            this.fltValue += this.fltIncr;
            break;
        case DOUBLE:
            this.dblValue += this.dblIncr;
            break;
        case STRING:
            this.strValue += this.strIncr;
            break;
        case UNSUPPORTED:
            throw new UnsupportedOperationException("Unsupported type case: " + this.enmType);
        };
    }
    
    /**
     * <p>
     * Generate the next scalar value randomly according to value type and stores it.
     * </p>
     * 
     * @throws  UnsupportedOperationException   the scalar type is <code>{@link JalScalarType#UNSUPPORTED}</code>
     */
    private void nextRandomValue() throws UnsupportedOperationException {
        
        switch (this.enmType) {
        case BOOLEAN:
            this.bolValue = facRandom.nextBoolean();
            break;
        case INTEGER:
            this.intValue = facRandom.nextInt();
            break;
        case LONG:
            this.lngValue = facRandom.nextLong();
            break;
        case FLOAT:
            this.fltValue = facRandom.nextFloat();
            break;
        case DOUBLE:
            this.dblValue = facRandom.nextDouble();
            break;
        case STRING:
            this.strValue = facRandom.nextInt();
            break;
        case UNSUPPORTED:
            throw new UnsupportedOperationException("Unsupported type case: " + this.enmType);
        };
    }
    
    
    // 
    // Creator Support Methods
    //
    
}
