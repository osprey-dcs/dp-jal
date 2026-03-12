/*
 * Project: dp-jal
 * File:	FrameProcTestParams.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.frame
 * Type: 	FrameProcTestParams
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
 * @since Feb 5, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.frame;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.MalformedParametersException;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.tools.appfwk.ITestParameter;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Enumeration of the test parameters for the <code>FrameProcessorEvaluator</code> application.
 * </p>
 * <p>
 * Enumerates the parameters within a <code>FrameProcTestCase</code> record which define test cases for the
 * <code>FrameProcessorEvaluator</code> application.  The parameters enumeration is used for test suite creation
 * by <code>FrameProcTestSuite</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 5, 2026
 *
 */
public enum FrameProcTestParams implements ITestParameter<FrameProcTestParams> {
    
    /**
     * Enable/disable the use of data column serialization for <code>IngestDataRequest</code> messages.
     */
    COL_SER_ENBL("Enable data column serialization.", FrameProcessorEvaluator.STR_PARSE_SERIAL_ENBL_DVAR, Boolean.class, DefaultCfg.API.ingest.serialize.enabled),
    
    /**
     * Enable/disable concurrency (i.e., multi-threaded processing) in ingestion frame processing.
     */
    MTHREAD_ENABLE("Enable multi-threaded processing", FrameProcessorEvaluator.STR_PARSE_MTHRD_ENBL_DVAR, Boolean.class, DefaultCfg.API.ingest.concurrency.enabled),
    
    /**
     * Maximum number of allowable concurrent processing thread in concurrent ingestion frame processing.
     */
    MTHREAD_COUNT("Maximum number of concurrent processing threads", FrameProcessorEvaluator.STR_PARSE_THRD_CNT_DVAR, Integer.class, DefaultCfg.API.ingest.concurrency.maxThreads),
    
    /**
     * Enable/disable ingestion frame decomposition (i.e., to conform to maximum gRPC message size limits). 
     */
    DCMP_ENABLE("Enable ingestion frame decomposition", FrameProcessorEvaluator.STR_PARSE_DCMP_ENBL_DVAR, Boolean.class, DefaultCfg.API.ingest.decompose.enabled),
    
    /**
     * Maximum allowable composite ingestion frame size (in bytes) when using ingestion frame decomposition. 
     */
    DCMP_SIZE("Maximum composite ingestion frame size (bytes)", FrameProcessorEvaluator.STR_PARSE_DCMP_SZ_DVAR, Integer.class, DefaultCfg.API.ingest.decompose.maxSize),
    
    /**
     * Number of ingestion frames forming the test case payload. 
     */
    FRAME_CNT("Number of ingestion data frames in payload", FrameProcessorEvaluator.STR_PARSE_FRM_CNT_DVAR, Integer.class, DefaultCfg.TOOLS.datagen.frame.count),
    
    /**
     * The test case ingestion frame specification (definition).  
     */
    FRAME_DEF("Ingestion data frame configuration", FrameProcessorEvaluator.STR_PARSE_FRM_SPEC_DVAR, FrameFactorySpec.class, DefaultCfgLoc.SPEC_FRM),
    
    ;
    
    /**
     * <p>
     * Internal class supplying default values for the enumerated parameters.
     * </p>
     * <p>
     * Internal class required to obtain dynamic values for enumeration constant constructor. 
     * Class extracts default values from the JAL default configuration and the JAL Tools default configuration.
     * </p>
     */
    private static final class DefaultCfgLoc {
        
        /** The default ingestion frame specification - created here to manage creation exceptions */
        private static FrameFactorySpec             SPEC_FRM;
      
        static {
                try {
                    SPEC_FRM = FrameFactorySpec.defaultFrame();
                    
                } catch (Exception e) {
                    System.err.println(JavaRuntime.getQualifiedMethodNameSimple() + " - Unable to create default ingestion frame specification.");
                    System.err.println("  Exception: " + e.getClass().getName());
                    System.err.println("  Message  : " + e.getMessage());
                    System.exit(1);
                }
        }
    }
    
    //
    //  Constant Attributes
    //
    
    /** The parameter description */
    private final String    strParamDesc;
    
    /** The command-line delimited variable name containing the parameter value(s) */
    private final String    strDelVarNm;
    
    /** The parameter type */
    private final Class<?>  clsParamType;
    
    /** The parameter default value */
    private final Object    objValueDef;
    
    
    //
    // Constant Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>FrameProcTestParams</code> instance.
     * </p>
     *
     * @param strParamDesc  the parameter string description
     * @param strDelVarNm   the delimited variable name used to identify parameter values on the command-line
     * @param clsParamType  the parameter class type
     * @param objValueDef   the parameter default value
     */
    private FrameProcTestParams(String strDesc, String strDelVarNm, Class<?> clsParamType, Object objValueDef) {
        this.strParamDesc = strDesc;
        this.strDelVarNm = strDelVarNm;
        this.clsParamType = clsParamType;
        this.objValueDef = objValueDef;
    }
    
    //
    // Enumeration Operations
    //
    
    /**
     * <p>
     * Returns the <code>FrameProcTestParams</code> enumeration constant with the given name.
     * </p>
     * <p>
     * This is a convenience method which defers to <code>{@link Enum#valueOf(Class, String)}</code> by
     * supplying the first argument with the class type of this enumeration.  Any exception is caught
     * and returned as a <code>{@link TypeNotPresentException}</code> which includes the originating 
     * exception as the cause.
     * </p>
     * 
     * @param strName   name of the enumeration constant
     * 
     * @return  the <code>FrameProcTestParams</code> constant with the given name
     * 
     * @throws NoSuchElementException  the argument was <code>null</code> or an invalid enumeration constant name  
     */
    public static FrameProcTestParams    valueFrom(String strName) throws NoSuchElementException {
        return ITestParameter.valueFrom(FrameProcTestParams.class, strName);
    }
    
    /**
     * <p>
     * Prints out a text description of all the enumeration constants (as test parameters) to the given output.
     * </p>
     * <p>
     * A line-by-line text description of each enumeration constant is written to the given output.
     * The <code>strPad</code> is used to supply an optional whitespace character padding to the
     * left-hand side header for each line description.
     * </p>
     *   
     * @param ps        output stream to receive text description of record fields
     * @param strPad    white space padding for left-hand side line headings (or <code>null</code>.
     * 
     * @throws NoSuchElementException   a maximum value could not be found for the constant name, delimited variable name, or class type name
     */
    public static void  printOut(PrintStream ps, String strPad) throws NoSuchElementException {
        ITestParameter.printOut(FrameProcTestParams.class, ps, strPad);
    }
    
    
    //
    // ITestParameter Interface
    //
    
    /**
     * <p>
     * Returns the <code>TestParams</code> enumeration constant exposing this interface.
     * </p>
     * 
     * @return  the <code>TestParams</code> enumeration constant
     * 
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getParameterConstant()
     */
    @Override
    public FrameProcTestParams   getParameterConstant() {
        return this;
    }
    /**
     * <p>
     * Returns the string description of the test parameter associated with this enumeration constant.
     * </p>
     * 
     * @return  a string description of this parameter
     * 
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getParameterDescription()
     */
    @Override
    public String   getParameterDescription() {
        return this.strParamDesc;
    }
    
    /**
     * <p> 
     * Returns the class type of the parameter associated with this enumeration constant
     * </p>
     * 
     * @return  the Java class type of the associated parameter
     * 
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getParameterType()
     */
    @Override
    public Class<?> getParameterType() { 
        return this.clsParamType; 
    };
    
    /**
     * <p>
     * Returns the delimited variable name used to identify the parameter values on the command line.
     * </p>
     * 
     * @return  delimited variable name identifying parameter values on the application command line
     * 
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getParameterDelimOption()
     */
    @Override
    public String   getParameterDelimOption()  { 
        return this.strDelVarNm;
    }
    
    /**
     * <p>
     * Returns the default value of the parameter associated with this enumeration constant
     * </p>
     * <p>
     * Default values are taken from the JAL default configuration and the JAL Tools default
     * configuration available in enclosed class <code>{@link DefaultCfgLoc}</code>.
     * </p>
     *  
     * @return  the default parameter value assigned at constant construction
     * 
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getDefaultValue()
     */
    @Override
    public Object   getDefaultValue() {
        return this.objValueDef;
    }
    
    /**
     * <p>
     * Parse the string argument and convert it to an object of the proper type for the constant.
     * </p>
     * <p>
     * If the constant represents a numeric type (i.e., any constant exception <code>{@link #STRING}</code>)
     * it is converted to the appropriate Java numeric type using the <code>valueOf(String)</code> method 
     * using reflection.  If the constant is of type <code>{@link #STRING}</code> the argument simply passes
     * through.
     * </p>
     * 
     * @param strValue  typically a string representation of a numeric type, or any string if <code>this</code> is <code>{@link #STRING}</code>
     * 
     * @return  the Java numeric type after parsing and conversion, or the argument itself if <code>this</code> is <code>{@link #STRING}</code>
     * 
     * @throws NoSuchMethodException    the Java class <code>{@link #getJavaType()}</code> does not contain method <code>valueOf(String)</code>
     * @throws SecurityException        the class loader denied access to method <code>valueOf(String)</code> (e.g., typically package access)
     * @throws IllegalAccessException   the method <code>valueOf(String)</code> is not accessible
     * @throws InvocationTargetException    the <code>valueOf(String)</code> method threw an exception (e.g., NumberFormatException)
     * @throws IllegalArgumentException general error (typically bad argument count or enumeration constant not recognized)
     * @throws DateTimeParseException   invalid ISO-8605 date/time/duration format for 'period', 'start', or 'delay' 
     * @throws TypeNotPresentException  invalid enumeration constant (e.g., the 1st argument was not a <code>JalComplexType</code>)
     * @throws NumberFormatException    invalid numeric expression (typically for 'lngSeed' value)
     * @throws ConfigurationException   the argument contained the wrong number of arguments for the <code>JalComplexType</code>
     * @throws UnsupportedOperationException invalid field value format (typically 'numIncr' was invalid)
     * @throws MalformedParametersException  an enumeration constant within the argument set was not recognized (IMAGE)
     * @throws NoSuchElementException   the column data type was unrecognized (i.e., 'DTYPE' was not supported)
     */
    @Override
    public Object   parseValue(String strValue) 
            throws UnsupportedOperationException, NoSuchMethodException, SecurityException, IllegalAccessException, 
                   InvocationTargetException, DateTimeParseException, NumberFormatException, IllegalArgumentException, 
                   TypeNotPresentException, ConfigurationException, MalformedParametersException 
    {
        // Special case for FRAME_DEF - must parse arguments within 'frame parameters'
        if (this == FRAME_DEF) {
            StringTokenizer     tokenizer = new StringTokenizer(strValue, " '");
            List<String>        lstTokens = new LinkedList<>();
            while (tokenizer.hasMoreTokens()) {
                String  strToken = tokenizer.nextToken().strip();
                
                lstTokens.add(strToken);
            }
            String[]            arrArgs = lstTokens.toArray(new String[lstTokens.size()]);
            FrameFactorySpec    specFrm = FrameFactorySpec.parse(arrArgs); // throws IllegalArgumentException, DateTimeParseException, TypeNotPresentException, NumberFormatException, ConfigurationException, UnsupportedOperationException, MalformedParametersException
            
            return specFrm;
        }

        // Otherwise defer to the default implementation
        return ITestParameter.super.parseValue(strValue);
    }

}
