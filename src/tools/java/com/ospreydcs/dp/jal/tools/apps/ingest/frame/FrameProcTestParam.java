/*
 * Project: dp-jal
 * File:	FrameProcTestParam.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.frame
 * Type: 	FrameProcTestParam
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
import java.lang.reflect.Method;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.config.JalConfig;
import com.ospreydcs.dp.jal.config.ingest.JalIngestionConfig;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
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
public enum FrameProcTestParam {
    
    /**
     * Enable/disable the use of data column serialization for <code>IngestDataRequest</code> messages.
     */
    COL_SER_ENBL("Enable data column serialization.", FrameProcessorEvaluator.STR_PARSE_SERIAL_ENBL_DVAR, Boolean.class, DefaultCfg.VALS.serialize.enabled),
    
    /**
     * Enable/disable concurrency (i.e., multi-threaded processing) in ingestion frame processing.
     */
    MTHREAD_ENABLE("Enable multi-threaded processing", FrameProcessorEvaluator.STR_PARSE_MTHRD_ENBL_DVAR, Boolean.class, DefaultCfg.VALS.concurrency.enabled),
    
    /**
     * Maximum number of allowable concurrent processing thread in concurrent ingestion frame processing.
     */
    MTHREAD_COUNT("Maximum number of concurrent processing threads", FrameProcessorEvaluator.STR_PARSE_THRD_CNT_DVAR, Integer.class, DefaultCfg.VALS.concurrency.maxThreads),
    
    /**
     * Enable/disable ingestion frame decomposition (i.e., to conform to maximum gRPC message size limits). 
     */
    DCMP_ENABLE("Enable ingestion frame decomposition", FrameProcessorEvaluator.STR_PARSE_DCMP_ENBL_DVAR, Boolean.class, DefaultCfg.VALS.decompose.enabled),
    
    /**
     * Maximum allowable composite ingestion frame size (in bytes) when using ingestion frame decomposition. 
     */
    DCMP_SIZE("Maximum composite ingestion frame size (bytes)", FrameProcessorEvaluator.STR_PARSE_DCMP_SZ_DVAR, Integer.class, DefaultCfg.VALS.decompose.maxSize),
    
    /**
     * The test case ingestion frame specification (definition).  
     */
    FRAME_DEF("Ingestion data frame configuration", FrameProcessorEvaluator.STR_PARSE_FRM_SPEC_DVAR, FrameFactorySpec.class, DefaultCfg.SPEC_FRM),
    
    /**
     * Number of ingestion frames forming the test case payload. 
     */
    FRAME_CNT("Number of ingestion data frames in payload", FrameProcessorEvaluator.STR_PARSE_FRM_CNT_DVAR, Integer.class, DefaultCfg.CNT_FRMS),
    
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
    private static final class DefaultCfg {
    
        /** Handle to the JAL default ingestion configuration values */
        private static final JalIngestionConfig     VALS = JalConfig.getInstance().ingest;
        
        /** The default ingestion frame payload size taken from the JAL Tools default configuration */
        private static final Integer                CNT_FRMS = JalToolsConfig.getInstance().datagen.frame.count;
        
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
     * Constructs a new <code>FrameProcTestParam</code> instance.
     * </p>
     *
     * @param strParamDesc       the parameter string description
     * @param strDelVarNm   the delimited variable name used to identify parameter values on the command-line
     * @param clsParamType  the parameter class type
     * @param objValueDef   the parameter default value
     */
    private FrameProcTestParam(String strDesc, String strDelVarNm, Class<?> clsParamType, Object objValueDef) {
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
     * Returns the <code>FrameProcTestParam</code> enumeration constant with the given name.
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
     * @return  the <code>FrameProcTestParam</code> constant with the given name
     * 
     * @throws TypeNotPresentException  the argument was <code>null</code> or an invalid enumeration constant name  
     */
    public static FrameProcTestParam    valueFrom(String strName) throws TypeNotPresentException {
        
        try {
            FrameProcTestParam  enmParam = FrameProcTestParam.valueOf(FrameProcTestParam.class, strName);

            return enmParam;
            
        } catch (Exception e) {
            throw new TypeNotPresentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Unrecognized enumeration constant: " + strName, e);
        }
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
        EnumSet<FrameProcTestParam> setParams = EnumSet.allOf(FrameProcTestParam.class);
        
        // Compute maximum field sizes
        int     szNmMax = setParams.stream().<String>map(Enum::name).mapToInt(String::length).max().getAsInt();
        int     szOptMax = setParams.stream().<String>map(FrameProcTestParam::getDelimitedVariableName).mapToInt(String::length).max().getAsInt();
        int     szTypeMax = setParams.stream().<Class<?>>map(FrameProcTestParam::getParameterType).<String>map(Class::getSimpleName).mapToInt(String::length).max().getAsInt();
        
        // Create the format string for each output line
        String  strFmt = "%s%-" +  szNmMax + "s : "
                        + "Command-line variable = %" + szOptMax + "s, " 
                        + "Type = %" + szTypeMax + "s, " 
                        + "Description = %s.";
        
        // Print out line-by-line text description of each constant
        setParams.forEach(p -> ps.println(
                String.format(strFmt, 
                        strPad, 
                        p.name(), 
                        p.getDelimitedVariableName(), 
                        p.getParameterType().getSimpleName(), 
                        p.getParameterDescription()
                        )
                ));
    }
    
    
    //
    // Constant Operations
    //
    
    /**
     * <p>
     * Returns the string description of the test parameter associated with this enumeration constant.
     * </p>
     * 
     * @return  a string description of this parameter
     */
    public String   getParameterDescription() {
        return this.strParamDesc;
    }
    
    /**
     * <p> 
     * Returns the class type of the parameter associated with this enumeration constant
     * </p>
     * 
     * @return  the Java class type of the associated parameter
     */
    public Class<?> getParameterType() { 
        return this.clsParamType; 
    };
    
    /**
     * <p>
     * Returns the delimited variable name used to identify the parameter values on the command line.
     * </p>
     * 
     * @return  delimited variable name identifying parameter values on the application command line
     */
    public String   getDelimitedVariableName()  { 
        return this.strDelVarNm;
    }
    
    /**
     * <p>
     * Returns the default value of the parameter associated with this enumeration constant
     * </p>
     * <p>
     * Default values are taken from the JAL default configuration and the JAL Tools default
     * configuration available in enclosed class <code>{@link DefaultCfg}</code>.
     * </p>
     *  
     * @return  the default parameter value assigned at constant construction
     */
    public Object   getDefaultValue() {
        return this.objValueDef;
    }
    
    /**
     * <p>
     * Determines whether or not this parameter type is compatible with the given class (i.e. via assignment).
     * </p>
     * 
     * @return <code>true</code> if the associated parameter value is assignable from the given class type,
     *         <code>false</code> otherwise
     */
    public boolean  isAssignable(Class<?> clsVal) { 
        return this.clsParamType.isAssignableFrom(clsVal); 
    };
    
    /**
     * <p>
     * Determines whether or not the given object is a valid parameter value.
     * </p>
     * 
     * @return  <code>true</code> if the associated parameter can be assigned to the given object value,
     *          <code>false</code> otherwise
     */
    public boolean  isInstance(Object objVal) { 
        return this.clsParamType.isInstance(objVal); 
    };

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
        
        // Parse the string using the 'valueOf(String)' method for each numeric class
        Method mthValue = this.clsParamType.getMethod("valueOf", String.class);  // throws NoSuchMethodException, SecurityException
        Object objValue = mthValue.invoke(null, strValue);                      // throws IllegalAccessException, InvocationTargetException

        return objValue;
    }

}
