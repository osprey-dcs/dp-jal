/*
 * Project: dp-jal
 * File:	IngestChanTestParams.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.channel
 * Type: 	IngestChanTestParams
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
 * @since Feb 18, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.channel;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.MalformedParametersException;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.common.DpGrpcStreamType;
import com.ospreydcs.dp.jal.tools.appfwk.ITestParameter;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Enumeration of test case parameters for application <code>IngestionChannelEvaluator</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 18, 2026
 *
 */
public enum IngestChanTestParams implements ITestParameter<IngestChanTestParams> {
    
    /**
     * Enable/disable the use of data column serialization for <code>IngestDataRequest</code> messages.
     */
    COL_SER_ENBL("Enable/disable data column serialization.", "--colser", Boolean.class, DefaultCfg.API.ingest.serialize.enabled),
    
    /**
     * The gRPC data stream type used for ingest channel transmission {FORWARD, BIDIRECTIONAL} 
     */
    STREAM_TYPE("gRPC stream type {FORWARD, BIDIRECTIONAL}.", "--strmtype", DpGrpcStreamType.class, DefaultCfg.API.ingest.stream.type),
    
    /**
     * Enable/disable multiple, concurrent gRPC data streams in ingestion channel transmission.
     */
    MSTREAM_ENBL("Enable/disable multi-stream ingestion.", "--mstrm", Boolean.class, DefaultCfg.API.ingest.stream.concurrency.enabled),
    
    /**
     * Maximum number of allowable concurrent gRPC data streams in ingestion channel transmission.
     */
    MSTREAM_CNT("gRPC multi-stream count maximum.", "--strmcnt", Integer.class, DefaultCfg.API.ingest.stream.concurrency.maxStreams),
    
    /**
     * Number of ingestion frames forming the test case payload. 
     */
    FRAME_CNT("Number of ingestion data frames in payload.", "--frmcnt", Integer.class, DefaultCfg.TOOLS.datagen.frame.count),
    
    /**
     * The test case ingestion frame specification (definition).  
     */
    FRAME_DEF("Ingestion data frame configuration.", "--frame", FrameFactorySpec.class, DefaultCfgLoc.FRM_DEF),
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
        private static FrameFactorySpec             FRM_DEF;
      
        static {
                try {
                    FRM_DEF = FrameFactorySpec.defaultFrame();
                    
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
     * Constructs a new <code>IngestChanTestParams</code> instance.
     * </p>
     *
     * @param strParamDesc  the parameter string description
     * @param strDelVarNm   the delimited variable name used to identify parameter values on the command-line
     * @param clsParamType  the parameter class type
     * @param objValueDef   the parameter default value
     */
    private IngestChanTestParams(String strParamDesc, String strDelVarNm, Class<?> clsParamType, Object objValueDef) {
        this.strParamDesc = strParamDesc;
        this.strDelVarNm = strDelVarNm;
        this.clsParamType = clsParamType;
        this.objValueDef = objValueDef;
    }

    
    //
    // Enumeration Operations
    //
    
    /**
     * <p>
     * Returns the <code>IngestChanTestParams</code> enumeration constant with the given name.
     * </p>
     * <p>
     * Convenience method deferring to <code>{@link ITestParameter#valueFrom(Class, String)}</code> with 
     * the <code>IngestChanTestParams</code> class object and given name.
     * </p>
     * 
     * @param strName   name of the enumeration constant
     * 
     * @return  the enumeration constant with the given name 
     * 
     * @throws NoSuchElementException   invalid constant name
     * 
     * @see ITestParameter#valueFrom(Class, String)
     */
    public static IngestChanTestParams  valueFrom(String strName) throws NoSuchElementException {
        return ITestParameter.valueFrom(IngestChanTestParams.class, strName);
    }
    
    /**
     * <p>
     * Prints out a line-by-line description of this enumeration.
     * </p>
     * <p>
     * Defers to <code>{@link ITestParameter#printOut(Class, PrintStream, String)}</code> with the 
     * <code>IngestChanTestParams</code> class object and given arguments.
     * </p>
     * 
     * @param ps        output stream to receive text description of record fields
     * @param strPad    white space padding for left-hand side line headings (or <code>null</code>.
     * 
     * @throws ClassCastException       enumeration type does not implement <code>ITestParameter</code> interface
     * @throws NoSuchElementException   a maximum value could not be found for the constant name, delimited variable name, or class type name
     * 
     * @see ITestParameter#printOut(Class, PrintStream, String)
     */
    public static void  printOut(PrintStream ps, String strPad) throws ClassCastException, NoSuchElementException {
        ITestParameter.printOut(IngestChanTestParams.class, ps, strPad);
    }
    
    /**
     * <p>
     * Returns an ordered list of all valid, delimited command-line options according to the enumeration.
     * </p>
     * <p>
     * Defers to <code>{@link ITestParameter#validDelimOptions(Class)}</code> with the 
     * <code>IngestChanTestParams</code> class object.
     * 
     * @return  the ordered list of all valid delimited command-line options for the enumeration 
     * 
     * @throws ClassCastException       enumeration type does not implement <code>ITestParameter</code> interface
     * 
     * @see ITestParameter#validDelimOptions(Class)
     */
    public static List<String>  validDelimOptions() throws ClassCastException {
        return ITestParameter.validDelimOptions(IngestChanTestParams.class);
    }
    
    
    //
    // ITestParameter Interface
    //

    /**
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getParameterConstant()
     */
    @Override
    public IngestChanTestParams getParameterConstant() {
        return this;
    }
    
    /**
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getParameterDescription()
     */
    @Override
    public String getParameterDescription() {
        return this.strParamDesc;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getParameterType()
     */
    @Override
    public Class<?> getParameterType() {
        return this.clsParamType;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getParameterDelimOption()
     */
    @Override
    public String getParameterDelimOption() {
        return this.strDelVarNm;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.appfwk.ITestParameter#getDefaultValue()
     */
    @Override
    public Object getDefaultValue() {
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
                   NoSuchElementException, ConfigurationException, TypeNotPresentException, ConfigurationException, MalformedParametersException 
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
