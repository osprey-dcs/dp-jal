/*
 * Project: dp-jal
 * File:	DataRequestSpec.java
 * Package: com.ospreydcs.dp.jal.tools.common.requests
 * Type: 	DataRequestSpec
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
 * @since Mar 13, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.requests;

import java.io.PrintStream;
import java.lang.reflect.MalformedParametersException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.query.DpDataRequest;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.archive.JalToolsTestArchiveConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Specifications record for creation of <code>DpDataRequest</code> instances.
 * </p>
 * <p>
 * The utility of this record comes from its creators which can generate specifications
 * for <code>DpDataRequest</code> instances from the most general, to very specific depending
 * upon the number of default arguments.
 * </p>
 * <p> 
 * <h2>Creator Default Parameters</h2> 
 * Default arguments are tailored to the Data Platform Test Archive.  However, unlike the
 * predefined library of Test Archive requests, <code>{@link TestArchiveRequest}</code>,
 * a <code>DataRequestSpec</code> can describe any arbitrary request, including those from
 * the Test Archive.
 * </p>  
 * <p>
 * <h2>Parsing</h2>
 * The availability of the parsing creator <code>{@link #parse(String[])}</code> allows clients to
 * create <code>DataRequestSpec</code> instances from command-line arguments, that is, when clients
 * are applications.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * The parameters for the gRPC stream type and data column serialization are not applicable.
 * All <code>DpDataRequest</code> instances returned from <code>{@link #createRequest()}</code>
 * have the default configuration for these parameters.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 13, 2026
 *
 * @param strRqstId the time-series data request identifier
 * @param setPvNms  the set of requested data source (Process Variable) names 
 * @param insStart  the start time of the data request
 * @param durRange  the duration of the data request
 */
public record DataRequestSpec(
        String          strRqstId,
        Set<String>     setPvNms,
        Instant         insStart,
        Duration        durRange
        ) 
{

    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>DataRequestSpec</code> instance populated according to the arguments specification.
     * </p>
     * <p>
     * This creator builds the field <code>{@link #setPvNms()}</code> with <code>cntPvs</code> names. 
     * The names are created using the PV name prefix <code>strPvPrfx</code> suffixed by the index of creation.  
     * Specifically, all PV names are given as
     * <pre>
     * setPvNms = {strPvPrfx<sub>0</sub>, strPvPrfx<sub>1</sub>, ..., strPvPrfx<sub>cntPvs - 1</sub>}
     * </pre>
     * </p>
     * <p>
     * <h2>Default Values</h2>
     * This creator uses default values from the JAL Tools default configuration, which are captured as record constants.  
     * Field values not provided are given by the following default values:
     * <ul>
     * <li><code>strPvPrfx = {@link #STR_ARC_PV_PRFX}</code> = {@value #STR_ARC_PV_PRFX}.</li>
     * <li><code>{@link #insStart()} = {@link #INS_ARC_START}</code> = {@value #INS_ARC_START}.</li>
     * <li><code>{@link #durRange()} = {@link #DUR_ARC_RANGE}</code> = {@value #DUR_ARC_RANGE}.</li>
     * </ul>
     * </p> 
     * 
     * @param strRqstId the time-series data request identifier
     * @param strPvPrfx prefix given to all PV names in <code>{@link #setPvNms()}</code>
     * @param cntPvs    number of PV names in <code>{@link #setPvNms()}</code>.
     * 
     * @return  a new <code>DataRequestSpec</code> instance with fields populated by the given argument values
     */
    public static DataRequestSpec   from(String strRqstId, int cntPvs) {
        return DataRequestSpec.from(strRqstId, cntPvs, STR_ARC_PV_PRFX);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DataRequestSpec</code> instance populated according to the arguments specification.
     * </p>
     * <p>
     * This creator builds the field <code>{@link #setPvNms()}</code> with <code>cntPvs</code> names. 
     * The names are created using the PV name prefix <code>strPvPrfx</code> suffixed by the index of creation.  
     * Specifically, all PV names are given as
     * <pre>
     * setPvNms = {strPvPrfx<sub>0</sub>, strPvPrfx<sub>1</sub>, ..., strPvPrfx<sub>cntPvs - 1</sub>}
     * </pre>
     * </p>
     * <p>
     * <h2>Default Values</h2>
     * This creator uses default values from the JAL Tools default configuration, which are captured as record constants.  
     * Field values not provided are given by the following default values:
     * <ul>
     * <li><code>{@link #insStart()} = {@link #INS_ARC_START}</code> = {@value #INS_ARC_START}.</li>
     * <li><code>{@link #durRange()} = {@link #DUR_ARC_RANGE}</code> = {@value #DUR_ARC_RANGE}.</li>
     * </ul>
     * </p> 
     * 
     * @param strRqstId the time-series data request identifier
     * @param cntPvs    number of PV names in <code>{@link #setPvNms()}</code>.
     * @param strPvPrfx prefix given to all PV names in <code>{@link #setPvNms()}</code>
     * 
     * @return  a new <code>DataRequestSpec</code> instance with fields populated by the given argument values
     */
    public static DataRequestSpec   from(String strRqstId,  int cntPvs, String strPvPrfx) {
        return DataRequestSpec.from(strRqstId, cntPvs, strPvPrfx, DUR_ARC_RANGE);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DataRequestSpec</code> instance populated according to the arguments specification.
     * </p>
     * <p>
     * This creator builds the field <code>{@link #setPvNms()}</code> with <code>cntPvs</code> names. 
     * The names are created using the PV name prefix <code>strPvPrfx</code> suffixed by the index of creation.  
     * Specifically, all PV names are given as
     * <pre>
     * setPvNms = {strPvPrfx<sub>0</sub>, strPvPrfx<sub>1</sub>, ..., strPvPrfx<sub>cntPvs - 1</sub>}
     * </pre>
     * </p>
     * <p>
     * <h2>Default Values</h2>
     * This creator uses default values from the JAL Tools default configuration, which are captured as record constants.  
     * Field values not provided are given by the following default values:
     * <ul>
     * <li><code>{@link #durRange()} = {@link #DUR_ARC_RANGE}</code> = {@value #DUR_ARC_RANGE}.</li>
     * </ul>
     * </p> 
     * 
     * @param strRqstId the time-series data request identifier
     * @param cntPvs    number of PV names in <code>{@link #setPvNms()}</code>.
     * @param strPvPrfx prefix given to all PV names in <code>{@link #setPvNms()}</code>
     * @param insStart  the start time of the data request
     * 
     * @return  a new <code>DataRequestSpec</code> instance with fields populated by the given argument values
     */
    public static DataRequestSpec   from(String strRqstId, int cntPvs, String strPvPrfx, Instant insStart) {
        return DataRequestSpec.from(strRqstId, cntPvs, strPvPrfx, insStart, DUR_ARC_RANGE);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DataRequestSpec</code> instance populated according to the arguments specification.
     * </p>
     * <p>
     * This creator builds the field <code>{@link #setPvNms()}</code> with <code>cntPvs</code> names. 
     * The names are created using the PV name prefix <code>strPvPrfx</code> suffixed by the index of creation.  
     * Specifically, all PV names are given as
     * <pre>
     * setPvNms = {strPvPrfx<sub>0</sub>, strPvPrfx<sub>1</sub>, ..., strPvPrfx<sub>cntPvs - 1</sub>}
     * </pre>
     * </p>
     * <p>
     * <h2>Default Values</h2>
     * This creator uses default values from the JAL Tools default configuration, which are captured as record constants.  
     * Field values not provided are given by the following default values:
     * <ul>
     * <li><code>{@link #insStart()} = {@link #INS_ARC_START}</code> = {@value #INS_ARC_START}.</li>
     * </ul>
     * </p> 
     *   
     * @param strRqstId the time-series data request identifier
     * @param cntPvs    number of PV names in <code>{@link #setPvNms()}</code>.
     * @param strPvPrfx prefix given to all PV names in <code>{@link #setPvNms()}</code>
     * @param durRange  the duration of the data request
     * 
     * @return  a new <code>DataRequestSpec</code> instance with fields populated by the given argument values
     */
    public static DataRequestSpec   from(String strRqstId, int cntPvs, String strPvPrfx, Duration durRange) {
        return DataRequestSpec.from(strRqstId, cntPvs, strPvPrfx, INS_ARC_START, durRange);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DataRequestSpec</code> instance populated according to the arguments specification.
     * </p>
     * <p>
     * This creator builds the field <code>{@link #setPvNms()}</code> with <code>cntPvs</code> names. 
     * The names are created using the PV name prefix <code>strPvPrfx</code> suffixed by the index of creation.  
     * Specifically, all PV names are given as
     * <pre>
     * setPvNms = {strPvPrfx<sub>0</sub>, strPvPrfx<sub>1</sub>, ..., strPvPrfx<sub>cntPvs - 1</sub>}
     * </pre>
     * </p>  
     * <p>
     * This creator is computes the <code>{@link #durRange()}</code> field as the difference between the
     * start and end instants.
     * </p>
     * 
     * @param strRqstId the time-series data request identifier
     * @param cntPvs    number of PV names in <code>{@link #setPvNms()}</code>.
     * @param strPvPrfx prefix given to all PV names in <code>{@link #setPvNms()}</code>
     * @param insStart  the start time of the data request
     * @param durRange  the duration of the data request
     * 
     * @return  a new <code>DataRequestSpec</code> instance with fields populated by the given argument values
     * 
     * @throws DateTimeException    the difference between the <code>Instant</code> objects cannot be obtained
     * @throws ArithmeticException  the calculation exceeds the capacity of <code>Duration</code>
     */
    public static DataRequestSpec   from(String strRqstId, int cntPvs, String strPvPrfx, Instant insStart, Instant insEnd) {
        
        return DataRequestSpec.from(strRqstId, cntPvs, strPvPrfx, insStart, Duration.between(insStart, insEnd)); // throws DateTimeException, ArithmeticException
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DataRequestSpec</code> instance populated according to the arguments specification.
     * </p>
     * <p>
     * This creator builds the field <code>{@link #setPvNms()}</code> with <code>cntPvs</code> names. 
     * The names are created using the PV name prefix <code>strPvPrfx</code> suffixed by the index of creation.  
     * Specifically, all PV names are given as
     * <pre>
     * setPvNms = {strPvPrfx<sub>0</sub>, strPvPrfx<sub>1</sub>, ..., strPvPrfx<sub>cntPvs - 1</sub>}
     * </pre>
     * </p>  
     * 
     * @param strRqstId the time-series data request identifier
     * @param cntPvs    number of PV names in <code>{@link #setPvNms()}</code>.
     * @param strPvPrfx prefix given to all PV names in <code>{@link #setPvNms()}</code>
     * @param insStart  the start time of the data request
     * @param durRange  the duration of the data request
     * 
     * @return  a new <code>DataRequestSpec</code> instance with fields populated by the given argument values
     */
    public static DataRequestSpec   from(String strRqstId, int cntPvs, String strPvPrfx, Instant insStart, Duration durRange) {
        
        // Create PV name set
        Set<String> setPvNms = IntStream.range(0, cntPvs).mapToObj(i -> strPvPrfx + Integer.toString(i)).collect(Collectors.toSet());
        
        return DataRequestSpec.from(strRqstId, setPvNms, insStart, durRange);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DataRequestSpec</code> instance populated with the given arguments.
     * </p>
     * <p>
     * <h2>Default Values</h2>
     * This creator uses default values from the JAL Tools default configuration, which are captured as record constants.  
     * Field values not provided are given by the following default values:
     * <ul>
     * <li><code>{@link #insStart()} = {@link #INS_ARC_START}</code> = {@value #INS_ARC_START}.</li>
     * <li><code>{@link #durRange()} = {@link #DUR_ARC_RANGE}</code> = {@value #DUR_ARC_RANGE}.</li>
     * </ul>
     * </p> 
     * 
     * @param strRqstId the time-series data request identifier
     * @param setPvNms  the set of requested data source (Process Variable) names 
     * 
     * @return  a new <code>DataRequestSpec</code> instance with fields populated by the given argument values
     */
    public static DataRequestSpec   from(String strRqstId, Set<String> setPvNms) {
        return DataRequestSpec.from(strRqstId, setPvNms, DUR_ARC_RANGE);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DataRequestSpec</code> instance populated with the given arguments.
     * </p>
     * <p>
     * <h2>Default Values</h2>
     * This creator uses default values from the JAL Tools default configuration, which are captured as record constants.  
     * Field values not provided are given by the following default values:
     * <ul>
     * <li><code>{@link #durRange()} = {@link #DUR_ARC_RANGE}</code> = {@value #DUR_ARC_RANGE}.</li>
     * </ul>
     * </p> 
     * 
     * @param strRqstId the time-series data request identifier
     * @param setPvNms  the set of requested data source (Process Variable) names 
     * @param insStart  the start time of the data request
     * 
     * @return  a new <code>DataRequestSpec</code> instance with fields populated by the given argument values
     */
    public static DataRequestSpec   from(String strRqstId, Set<String> setPvNms, Instant insStart) {
        return DataRequestSpec.from(strRqstId, setPvNms, insStart, DUR_ARC_RANGE);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DataRequestSpec</code> instance populated with the given arguments.
     * </p>
     * <p>
     * <h2>Default Values</h2>
     * This creator uses default values from the JAL Tools default configuration, which are captured as record constants.  
     * Field values not provided are given by the following default values:
     * <ul>
     * <li><code>{@link #insStart()} = {@link #INS_ARC_START}</code> = {@value #INS_ARC_START}.</li>
     * </ul>
     * </p> 
     * 
     * @param strRqstId the time-series data request identifier
     * @param setPvNms  the set of requested data source (Process Variable) names 
     * @param durRange  the duration of the data request
     * 
     * @return  a new <code>DataRequestSpec</code> instance with fields populated by the given argument values
     */
    public static DataRequestSpec   from(String strRqstId, Set<String> setPvNms, Duration durRange) {
        return DataRequestSpec.from(strRqstId, setPvNms, INS_ARC_START, durRange);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DataRequestSpec</code> instance populated with the given arguments.
     * </p>
     * <p>
     * This creator is computes the <code>{@link #durRange()}</code> field as the difference between the
     * start and end instants.
     * </p>
     * 
     * @param strRqstId the time-series data request identifier
     * @param setPvNms  the set of requested data source (Process Variable) names 
     * @param insStart  the start time of the data request
     * @param insEnd    the end time of the data request
     * 
     * @return  a new <code>DataRequestSpec</code> instance with fields populated by the given argument values
     * 
     * @throws DateTimeException    the difference between the <code>Instant</code> objects cannot be obtained
     * @throws ArithmeticException  the calculation exceeds the capacity of <code>Duration</code>
     */
    public static DataRequestSpec   from(String strRqstId, Set<String> setPvNms, Instant insStart, Instant insEnd) {
        return DataRequestSpec.from(strRqstId, setPvNms, insStart, Duration.between(insStart, insEnd)); // throws DateTimeException, ArithmeticException
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DataRequestSpec</code> instance populated with the given arguments.
     * </p>
     * <p>
     * This creator is equivalent to the canonical constructor.
     * </p>
     * 
     * @param strRqstId the time-series data request identifier
     * @param setPvNms  the set of requested data source (Process Variable) names 
     * @param insStart  the start time of the data request
     * @param durRange  the duration of the data request
     * 
     * @return  a new <code>DataRequestSpec</code> instance with fields populated by the given argument values
     */
    public static DataRequestSpec   from(String strRqstId, Set<String> setPvNms, Instant insStart, Duration durRange) {
        return new DataRequestSpec(strRqstId, setPvNms, insStart, durRange);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DataRequestSpec</code> instance by parsing the given (command-line) arguments.
     * </p>
     * <p>
     * <h2>Formats</h2>
     * There are 2 valid parsing formats and either one will be recognized automatically:
     * <ol> 
     * <li>PV names are given explicitly,</li>
     * <li>PV names are specified by prefix and number.</li>
     * </ol>
     * For the 1st case the format is as follows:
     * <pre>
     *  rqstID [PV1 ... PVn [duration [start]]]
     * </pre>
     * where
     * <ul>
     * <li>'rqstID' = <code>{@link #strRqstId()}</code>.</li>
     * <li>{'PV1', ..., 'PVn'} = <code>{@link #setPvNms()}</code>.</li>
     * <li>'duration' = <code>{@link #durRange()}</code>.</li>
     * <li>'start' = <code>{@link #insStart()}</code>.</li>
     * </ul>
     * The brackets [] indicate optional arguments which default to the JAL Tools default configuration for
     * the Data Platform Test Archive.
     * </p>
     * <p>
     * For the 2nd case the format is as follows:
     * <pre>
     *  rqstID n [PvPrfx [duration [start]]]
     * </pre>
     * where
     * <ul>
     * <li>'rqstID' = <code>{@link #strRqstId()}</code>.</li>
     * <li>{'PvPrfx<sub>0</sub>', ..., 'PvPrfx<sub>n-1</sub>'} = <code>{@link #setPvNms()}</code>.</li>
     * <li>'duration' = <code>{@link #durRange()}</code>.</li>
     * <li>'start' = <code>{@link #insStart()}</code>.</li>
     * </ul>
     * Again, the brackets [] indicate optional arguments defaulting to the JAL Tools default configuration for
     * the Test Archive.
     * </p>
     * <p>
     * <h2>Duration and Start Instant</h2>
     * The duration 'duration' and start instant 'start' strings must be in ISO-8601 date/time format, otherwise
     * an exception is thrown.
     * See <code>{@link Duration#parse(CharSequence)}</code> and <code>{@link Instant#parse(CharSequence)}</code>
     * for information on the correct ISO-8601 formats for these parameters.
     * </p>
     * <p>
     * <h2>Optional Arguments</h2>
     * Field values not provided, that is for bracketed optional arguments, are given by the following 
     * default values:
     * <ul>
     * <li>'PV<sub>i</sub> = <code>{@link #STR_ARC_PV_PRFX}</code><sub>i</sub>.</li>
     * <li>'PvPrfx' = <code>{@link #STR_ARC_PV_PRFX}</code> = {@value #STR_ARC_PV_PRFX}.</li>
     * <li><code>{@link #durRange()} = {@link #DUR_ARC_RANGE}</code> = {@value #DUR_ARC_RANGE}.</li>
     * <li><code>{@link #insStart()} = {@link #INS_ARC_START}</code> = {@value #INS_ARC_START}.</li>
     * </ul>
     * </ul>
     * </p>
     *  
     * @param args  application command-line arguments in required format
     * 
     * @return  a new <code>DataRequestSpec</code> instance with fields populated from the parsed argument values
     * 
     * @throws IllegalArgumentException the argument contained the wrong number of arguments
     * @throws ArrayIndexOutOfBoundsException internal error - attempted to access argument beyond argument array bounds
     * @throws ConfigurationException   the argument contained the wrong number of arguments 
     * @throws NumberFormatException    invalid numeric expression (bad PV name count)
     * @throws UnsupportedOperationException invalid field value format (typically 'numIncr' was invalid)
     * @throws MalformedParametersException  an enumeration constant within the argument set was not recognized (IMAGE)
     * @throws NoSuchElementException   the column data type was unrecognized (i.e., 'DTYPE' was not supported)
     */
    public static DataRequestSpec   parse(String...args) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, ConfigurationException, NumberFormatException, MalformedParametersException {

        // Parser Resources
        final int       CNT_ARGS_MIN = 2;   // minimum number of arguments
        int             indArg = 0;         // running argument index
        
        // Check arguments size
        if (args.length < CNT_ARGS_MIN)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Arguments size < " + CNT_ARGS_MIN + ": " + Arrays.asList(args).toString());

        
        // --- Parse request ID and save ---
        String  strRqstId = args[indArg++]; // throws ArrayIndexOutOfBoundsException
        
        // --- Extract the PV name set and request duration ---
        String  strToken = args[indArg++];  // throws ArrayIndexOutOfBoundsException

        Set<String>     setPvNms;
        Duration        durRange = null;
        try {
            // Format - PV Name Count and Prefix 
            int cntPvs = Integer.valueOf(strToken); // PV name count is provided
            
            String  strPrfx;
            if (args.length <= indArg)
                strPrfx = STR_ARC_PV_PRFX;
            else
                strPrfx = args[indArg++];   // throws ArrayIndexOutOfBoundsException
            
            setPvNms = IntStream
                    .range(0, cntPvs)
                    .<String>mapToObj(i -> strPrfx + Integer.toString(i))
                    .collect(Collectors.toSet());
            
            if (args.length <= indArg)
                return DataRequestSpec.from(strRqstId, setPvNms);
            
            // --- Extract the duration ---
            strToken = args[indArg++];              // throws ArrayIndexOutOfBoundsException
            durRange = Duration.parse(strToken);    // throws DateTimeParseException
            
            if (args.length <= indArg)
                return DataRequestSpec.from(strRqstId, setPvNms, durRange);
            
        } catch (NumberFormatException e) {
            // Format - Explicit PV names are provided
            setPvNms = new HashSet<>();
            
            setPvNms.add(strToken);
            while (indArg < args.length) {  // Continue parsing until end of arguments or Duration is found
                strToken = args[indArg++];      // Advance to next argument
                
                try {
                    // --- Extract the duration ---
                    durRange = Duration.parse(strToken);    // Duration is found
                    
                    if (args.length <= indArg)
                        return DataRequestSpec.from(strRqstId, setPvNms, durRange);
                    else
                        break;
                    
                } catch (DateTimeParseException e2) {
                    // Not a Duration - another PV name
                    setPvNms.add(strToken);     // Argument is a PV name
                }
            }
            
            // Duration was never found - end of PV names
            if (args.length <= indArg)
                return DataRequestSpec.from(strRqstId, setPvNms);
        }

        // --- Get the start time instant ---
        strToken = args[indArg++];                      // throws ArrayIndexOutOfBoundsException
        Instant insStart = Instant.parse(strToken);     // throws DateTimeParseException 
        
        return DataRequestSpec.from(strRqstId, setPvNms, insStart, durRange);
    }
    
    
    
    // 
    // JAL Resources
    //
    
    /** The Data Platform Test Archive default configuration */
    private static final JalToolsTestArchiveConfig  CFG_ARC = JalToolsConfig.getInstance().testArchive;
    
    
    //
    // Record Constants
    //
    
    /** The default data request ID prefix when none is provided */
    public static final String      STR_RQST_ID_PRFX = DataRequestSpec.class.getSimpleName() + ":";
    
    /** The maximum number of PV names to list in <code> </code> (i.e., the head size) */
    public static final int         SZ_PV_NMS_HEAD = 10;
    
    
    /** The default PV prefix used for data sources within the Test Archive */
    public static final String      STR_ARC_PV_PRFX = CFG_ARC.pvs.prefix;
    
    /** The total number of PV names in the Test Archive */
    public static final int         CNT_ARC_PV_NMS = CFG_ARC.pvs.count.total;
    
    
    /** The default start time instant of the Test Archive */
    public static final Instant     INS_ARC_START = CFG_ARC.range.startInstant();
    
    /** The default time range duration of the Test Archive */
    public static final Duration    DUR_ARC_RANGE = CFG_ARC.range.rangeDuration();

    
    //
    // Record Variables
    //
    
    /** The record creation index (used for request ID when none is provided) */
    private static int      INT_INDEX = 1;
    
    
    //
    // Record Overrides
    //
    
    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof DataRequestSpec spec) {
            boolean bolResult = (this.strRqstId.equals(spec.strRqstId))
                             && (this.setPvNms.equals(spec.setPvNms))
                             && (this.insStart.equals(spec.insStart))
                             && (this.durRange.equals(spec.durRange));
            
            return bolResult;
        }
        return false;
    }


    //
    // Operations
    //

    /**
     * <p>
     * Computes and returns the end time instant of the data request specification. 
     * </p>
     * <p>
     * The returned result is computed by adding the duration <code>{@link #durRange()}</code>
     * to the start time instance <code>{@link #insStart()}</code>.
     * </p>
     * 
     * @return  the final time instant of the specified time-series data request
     */
    public Instant  endTime() {
        return this.insStart.plus(this.durRange);
    }
    
    /**
     * <p>
     * Returns the collection of unique Process Variable names as a mutable list.
     * </p>
     * The returned list is a mutable collection that can be modified independent of field <code>{@link #setPvNms()}</code>.
     * </p>
     * 
     * @return  the collection of data request PV names as a mutable list
     */
    public List<String> pvNamesList() {
        return this.setPvNms.stream().collect(Collectors.toList());
    }
    
    /**
     * <p>
     * Creates and returns a new <code>DpDataRequest</code> instance according to the properties of this specification.
     * </p>
     * <p>
     * The method can be called repeatedly to create multiple instances of a <code>DpDataRequest</code>, however,
     * all returned instances will be identical and conform to the specifications of this record.
     * Returned instances can, of course, be configured for different gRPC stream types and data column serialization
     * post creation.
     * </p>
     *  
     * @return  a new <code>DpDataRequest</code> instance conforming to this specification
     */
    public DpDataRequest    createRequest() {
        DpDataRequest   rqst = DpDataRequest.from(this.strRqstId, this.insStart, this.endTime(), this.pvNamesList());
        
        return rqst;
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

        // Create the PV names list head
        final int szHead = (SZ_PV_NMS_HEAD < this.setPvNms.size()) ? SZ_PV_NMS_HEAD : this.setPvNms.size();
        List<String>    lstHead = this.pvNamesList().subList(0, szHead);
        
        ps.println(strPad + "data request ID    : " + this.strRqstId());
        ps.println(strPad + "PV names (head)    : " + lstHead);
        ps.println(strPad + "request start time : " + this.insStart());
        ps.println(strPad + "request end time   : " + this.endTime());
        ps.println(strPad + "request duration   : " + this.durRange());
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates and returns a new data request identifier when none is provided.
     * </p>
     * <p>
     * The returned request ID is created by concatenating the record constant prefix
     * <code>{@link #STR_RQST_ID_PRFX}</code> to the current index <code>{@link #INT_INDEX}</code>.
     * The value of <code>{@link #INT_INDEX}</code> is then incremented.
     * </p>
     * 
     * @return  a new data request identifier
     */
    @SuppressWarnings("unused")
    private static String   createRequestId() {
        String  strRqstId = STR_RQST_ID_PRFX + INT_INDEX++;
        
        return strRqstId;
    }
}
