/*
 * Project: dp-jal
 * File:	ITestParameter.java
 * Package: com.ospreydcs.dp.jal.tools.common.score
 * Type: 	ITestParameter
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
package com.ospreydcs.dp.jal.tools.common.score;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.MalformedParametersException;
import java.lang.reflect.Method;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.config.JalConfig;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Interface for exposure by application test parameter enumeration constants.
 * </p>
 * <p>
 * The interface defines the required methods for all test parameter enumeration constants.
 * We have the following:
 * <ul>
 * <li><code>{@link #getParameterDescription()}</code>,</li>
 * <li><code>{@link #getParameterType()}</code>,</li>
 * <li><code>{@link #getParameterDelimOption()}</code>,</li>
 * <li><code>{@link #getDefaultValue()}</code>.</li>
 * </ul>
 * From the above operations multiple default method implementations are available.
 * <p>
 * <h2>Enumeration Operation</h2>
 * Multiple convenience operations are provided for the entire enumeration of test parameters
 * This are static methods that require the class object of the enumeration.  
 * </p>
 * <p>
 * <h2>Default Values</h2>
 * The enclosed class <code>{@link DefaultCfg}</code> is available to implementing enumerations
 * to retrieve values from the JAL API configuration and the JAL Tools configuration for
 * setting default values at constant construction.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 18, 2026
 *
 * @param <TestParams>  the enumeration of all application test parameters 
 */
public interface ITestParameter<TestParams extends Enum<TestParams>> {

    
    //
    // Enumeration Operations
    //
    
    /**
     * <p>
     * Returns all constants within this enumeration within a <code>Set</code> container.
     * </p>
     * <p>
     * This is a convenience method calling the Java <code>{@link EnumSet#allOf(Class)}</code> operation.
     * The returned container is ordered according to the ordering of the enumeration constants in the definition.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * As with most Java collections the returned set is not thread safe.
     * </p>
     *  
     * @param <TestParams>  the enumeration of all application test parameters
     *  
     * @param clsEnum   the class object of the <code>TestParams</code> enumeration
     * 
     * @return  ordered collection of all constants within the enumeration
     * 
     * @see EnumSet#allOf(Class)
     */
    public static <TestParams extends Enum<TestParams>> Set<TestParams>   paramSet(Class<TestParams> clsEnum) {
        EnumSet<TestParams>  setParams = EnumSet.allOf(clsEnum);
        
        return setParams;
    }

    /**
     * <p>
     * Returns the <code>TestParams</code> enumeration constant with the given name.
     * </p>
     * <p>
     * This is a convenience method which defers to <code>{@link Enum#valueOf(Class, String)}</code> by
     * supplying the second argument with the class type of the first argument.  Any exception is caught
     * and returned as a <code>{@link TypeNotPresentException}</code> which includes the originating 
     * exception as the cause.
     * </p>
     * 
     * @param <TestParams>  the enumeration of all application test parameters
     *  
     * @param clsEnum   the class object of the <code>TestParams</code> enumeration
     * @param strName   name of the enumeration constant
     * 
     * @return  the <code>TestParams</code> enumeration constant with the given name
     * 
     * @throws TypeNotPresentException  the argument was <code>null</code> or an invalid enumeration constant name  
     */
    public static <TestParams extends Enum<TestParams>> TestParams valueFrom(Class<TestParams> clsEnum, String strName) throws TypeNotPresentException {
        
        try {
            TestParams  enmParam = Enum.valueOf(clsEnum, strName);

            return enmParam;
            
        } catch (Exception e) {
            throw new TypeNotPresentException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Unrecognized enumeration constant for : " + clsEnum.getSimpleName() 
                    + ": " + strName, e);
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
     * <p>
     * <h2>NOTES:</h2>
     * The enumeration <code>TestParams</code> must implement the <code>ITestParameters</code> interface or an
     * exception is thrown.
     * </p>
     *   
     * @param <TestParams>  the enumeration of all application test parameters
     *  
     * @param clsEnum   the class object of the <code>TestParams</code> enumeration
     * @param ps        output stream to receive text description of record fields
     * @param strPad    white space padding for left-hand side line headings (or <code>null</code>.
     * 
     * @throws ClassCastException       enumeration type <code>TestParams</code> does not implement <code>ITestParameter</code> interface
     * @throws NoSuchElementException   a maximum value could not be found for the constant name, delimited variable name, or class type name
     */
    public static <TestParams extends Enum<TestParams>> void  printOut(Class<TestParams> clsEnum,PrintStream ps, String strPad) throws ClassCastException, NoSuchElementException {
        EnumSet<TestParams>              setEnum = EnumSet.allOf(clsEnum);
        @SuppressWarnings("unchecked")
        Set<ITestParameter<TestParams>>  setParams = setEnum.stream().map(p -> (ITestParameter<TestParams>)p ).collect(Collectors.toSet());
        
        // Compute maximum field sizes
        int     szNmMax = setEnum.stream().<String>map(Enum::name).mapToInt(String::length).max().getAsInt();
        int     szOptMax = setParams.stream().<String>map(ITestParameter::getParameterDelimOption).mapToInt(String::length).max().getAsInt();
        int     szTypeMax = setParams.stream().<Class<?>>map(ITestParameter::getParameterType).<String>map(Class::getSimpleName).mapToInt(String::length).max().getAsInt();
        
        // Create the format string for each output line
        String  strFmt = "%s%-" +  szNmMax + "s : "
                        + "Command-line variable = %" + szOptMax + "s, " 
                        + "Type = %" + szTypeMax + "s, " 
                        + "Description = %s";
        
        // Print out line-by-line text description of each constant
        setParams.forEach(p -> ps.println(
                String.format(strFmt, 
                        strPad, 
                        p.name(), 
                        p.getParameterDelimOption(), 
                        p.getParameterType().getSimpleName(), 
                        p.getParameterDescription()
                        )
                ));
    }
    
    /**
     * <p>
     * Returns the ordered list of valid command-line options as specified by the <code>TestParams</code> enumeration.
     * </p>
     * <p>
     * The method iterates through all constants within the <code>TestParams</code> enumeration collecting the
     * results of the <code>{@link #getParameterDelimOption()}</code> operation.  The list is then returned;
     * it contains the options in the order of the enumeration constant definitions.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The enumeration <code>TestParams</code> must implement the <code>ITestParameters</code> interface or an
     * exception is thrown.
     * </p>
     *  
     * @param <TestParams>  the enumeration of all application test parameters
     *  
     * @param clsEnum   the class object of the <code>TestParams</code> enumeration
     * 
     * @return  the ordered list of all valid delimited command-line options for the <code>TestParams</code> enumeration 
     * 
     * @throws ClassCastException       enumeration type <code>TestParams</code> does not implement <code>ITestParameter</code> interface
     */
    public static <TestParams extends Enum<TestParams>> List<String>    validDelimOptions(Class<TestParams> clsEnum) throws ClassCastException {
        EnumSet<TestParams>                 setEnum = EnumSet.allOf(clsEnum);
        @SuppressWarnings("unchecked")
        List<ITestParameter<TestParams>>    lstParams = setEnum.stream().map(p -> (ITestParameter<TestParams>)p ).toList();
        List<String>                        lstDelOpts = lstParams.stream().<String>map(ITestParameter::getParameterDelimOption).toList();
        
        return lstDelOpts;
    }
    
    
    //
    // Required Enumeration Constant Operations
    //
    
    /**
     * <p>
     * Returns the <code>TestParams</code> enumeration constant exposing this interface.
     * </p>
     * 
     * @return  the <code>TestParams</code> enumeration constant
     */
    public TestParams   getParameterConstant();
    
    /**
     * <p>
     * Returns the string description of the test parameter associated with this enumeration constant.
     * </p>
     * 
     * @return  a string description of this parameter
     */
    public String   getParameterDescription();
    
    /**
     * <p> 
     * Returns the class type of the parameter value associated with this enumeration constant
     * </p>
     * 
     * @return  the Java class type of the associated parameter
     */
    public Class<?> getParameterType();
    
    /**
     * <p>
     * Returns the delimited option name used to identify the parameter values on the command line.
     * </p>
     * 
     * @return  delimited variable name identifying parameter values on the application command line
     */
    public String   getParameterDelimOption();
    
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
    public Object   getDefaultValue();
    
    
    //
    // Enumeration Constant Default Implementations
    //
    
    /**
     * <p>
     * Returns the name of this enumeration constant.
     * </p>
     * </p>
     * This operation simply exposes the <code>{@link Enum#name()}</code> method to the
     * <code>ITestParameters</code> interface.
     * </p>
     *  
     * @return  the name of this enumeration constant
     */
    default public String   name() {
        return this.name();
    }
    
    /**
     * <p>
     * Determines whether or not this parameter type is compatible with the given class (i.e. via assignment).
     * </p>
     * <p>
     * The returned value is computed directly from the class type returned from <code>{@link #getParameterType()}</code>
     * and the operation <code>{@link Class#isAssignableFrom(Class)}</code>.
     * </p>
     * 
     * @return <code>true</code> if the associated parameter value is assignable from the given class type,
     *         <code>false</code> otherwise
     *         
     * @see Class#isAssignableFrom(Class)
     */
    default public boolean  isAssignable(Class<?> clsVal) { 
        return this.getParameterType().isAssignableFrom(clsVal); 
    };
    
    /**
     * <p>
     * Determines whether or not the given object is a valid parameter value.
     * </p>
     * <p>
     * The returned value is the exacted result returned by the Java <code>istanceof</code.
     * keyword where the left-hand side is the argument and the right-hand side is the class 
     * type returned by <code>{@link #getParameterType()}</code>
     * </p>
     * 
     * @return  <code>true</code> if the associated parameter can be assigned to the given object value,
     *          <code>false</code> otherwise
     *          
     * @see Class#isInstance(Object)
     */
    default public boolean  isInstance(Object objVal) { 
        return this.getParameterType().isInstance(objVal); 
    };

    /**
     * <p>
     * Parse the string argument and convert it to an object of the proper type for the enumeration constant.
     * </p>
     * <p>
     * If the constant represents a numeric type (i.e., any constant exception <code>{@link #STRING}</code>)
     * it is converted to the appropriate Java numeric type using the <code>valueOf(String)</code> method 
     * using reflection.  If the constant is of type <code>{@link #STRING}</code> the argument simply passes
     * through.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This default implementation correctly handles Java data type having a
     *     <code>valueOf(String)</code> method, or <code>String</code> types.
     *     </li>
     * <li>Enumerations containing constants representing complex types (e.g., such as <code>FrameFactorySpec</code>
     *     must override to handle the case.  
     *     </li>
     * <li>The quantity of available exceptions are available to method overrides to handle most situations.
     *     </li>
     * </ul>
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
    default public Object   parseValue(String strValue) 
            throws UnsupportedOperationException, NoSuchMethodException, SecurityException, IllegalAccessException, 
                   InvocationTargetException, DateTimeParseException, NumberFormatException, IllegalArgumentException, 
                   TypeNotPresentException, ConfigurationException, MalformedParametersException 
    {
        // Special case for type == String.class
        if (this.getParameterType() == String.class)
            return strValue;
        
        // Parse the string using the 'valueOf(String)' method for each numeric class
        Method mthValue = this.getParameterType().getMethod("valueOf", String.class);  // throws NoSuchMethodException, SecurityException
        Object objValue = mthValue.invoke(null, strValue);                      // throws IllegalAccessException, InvocationTargetException

        return objValue;
    }

    
    
    //
    // Test Parameter Enumeration Support 
    //
    
    /**
     * <p>
     * Enclosed class supplying default values for the enumerated parameters.
     * </p>
     * <p>
     * Enclosed class required to obtain static values for enumeration constant constructors.
     * The internal class provides enumeration constants access to the JAL API default configuration
     * and the JAL Tools default configuration.  These default values may be required for setting
     * the enumeration constant default value returned by <code>{@link ITestParameter#getDefaultValue()}</code>.
     * </p> 
     * <p>
     * Within the enumeration, this enclosed class is instantiated first, before enumeration constant constructors are 
     * called. Thus, the static constants <code>{@link #API}</code> and <code>{@link #TOOLS}</code> are available to
     * enumeration constant constructors.
     * </p>
     */
    public static final class DefaultCfg {
    
        /** Handle to the JAL API library default configuration values */
        public static final JalConfig      API = JalConfig.getInstance();
        
        /** Handle to the JAL Tools library default configuration values*/
        public static final JalToolsConfig TOOLS = JalToolsConfig.getInstance();
        
        /** The default ingestion frame specification - created here to manage creation exceptions */
        protected static FrameFactorySpec     SPEC_FRM;
      
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
    

}
