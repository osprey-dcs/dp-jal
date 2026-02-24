/*
 * Project: dp-jal
 * File:	TestSuiteGeneratorBase.java
 * Package: com.ospreydcs.dp.jal.tools.common.score
 * Type: 	TestSuiteGeneratorBase
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
 * @since Jan 16, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.score;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.MalformedParametersException;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.tools.common.parse.AppOptionsParser;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Base class for classes generating test suites of test cases for evaluation and scoring.
 * </p>
 * <p>
 * The assumption is that each <code>TestCase</code> record contains a test case configuration for an
 * application performing performing a set of evaluations on a (software) component and/or system.
 * </p> 
 * <p>
 * <h2>Requirements</h2>
 * Child classes must implement the abstract operation <code>{@link #createTestCase(Map)}</code> that creates a
 * <code>{@link TestCase}</code> record from a map of (Parameter, Value) pairs.  This is the primary
 * requirement of the base class.
 * Child classes must also implement the abstract operation <code>{@link #isValidType(Enum, Object)}</code>
 * which verifies that a test parameter value is of the correct data type.
 * The former method is used in <code>{@link #addParameterValue(Enum, Object)}</code> and the latter is
 * used in <code>{@link #createTestSuit()}</code>.
 * </p>
 * <p>
 * <h2>Child Class Creation</h2>
 * The method <code>{@link #parseParameterValues(AppOptionsParser, String...)}</code> is available for
 * initializing child-class creators.  The method is capable of configuring the entire test suite instance
 * directly from the command-line arguments of a Java application.  It requires the following:
 * <ul>
 * <li>A <code>{@link AppOptionsParser}</code> configured to the command-line options for the application,</li>
 * <li>The string array of application command-line tokens.</li> 
 * <li>That the <code>Param</code> enumeration expose the <code>{@link ITestParameter}</code> interface.</li>
 * </ul>
 * If the above conditions are met then a child class creator, say <code>from(AppOptionsParser, String...args)</code>
 * can instantiate a new object of itself, populate the object using 
 * <code>{@link #parseParameterValues(AppOptionsParser, String...)}</code>, then return the fully configured
 * test suite (assuming the command-line is valid).
 * </p>  
 * <p>
 * <h2>Configuration</h2>
 * It is assumed that child classes have a well-defined configuration state.  Typically, they support
 * a pre-defined parameter set where each parameter is allow to vary over a set of parameter values.
 * This parameter set is enumerated by the generic template <code>Param</code> where each enumeration
 * constant corresponds to a supported parameter.
 * </p>  
 * <p>
 * <h2>Parameter Value Assignment</h2>
 * Parameter are assigned values (by associated <code>Param</code> constant) using method 
 * <code>{@link #addParameterValue(Enum, Object)}</code>.
 * Each parameter must be assigned at least one value for the <code>TestSuiteGeneratorBase</code>
 * to be correctly configured.  
 * Specifically, if <code>{@link #isValidConfiguration()}</code> returns <code>true</code> this condition
 * is established.  Use method <code>{@link #unassignedParameters()}</code> to obtain a collection of 
 * parameters that have yet to be assigned test values.
 * <p>
 * <h2>Test Suite Generation</h2>
 * Once configured the <code>TestSuiteGeneratorBase</code> class is capable of generating the test suite 
 * by creating the collection of <code>TestCase</code> records which cover the domain of all test parameter values.
 * The test suite is created with method <code>{@link #createTestSuit()}</code>.
 * The returned test suite contains <code>TestCase</code> records which parameter values that span all possible
 * combinations provided using <code>{@link #addParameterValue(Enum, Object)}</code>.
 * </p>
 * <p>
 * Note each parameter must have at least one test value.  If all parameters have a single value the test suite
 * contains only one test case.  If one parameter contains two values and all others are single valued the test
 * suite contains two test cases, etc.  
 * </p>
 * <p>
 * Note that the size of the test suite increases geometrically.  For example, if there are <i>n</i> parameters
 * {<i>p</i><sub>1</sub>, <i>p</i><sub>2</sub>, ..., <i>p<sub>n</sub></i>} each with <i>N<sub>i</sub></i> values
 * then the total number of test cases <i>N</i> is 
 * <pre>
 *     <i>N</i> = <i>N</i><sub>1</sub> &times; <i>N</i><sub>2</sub> &times; &#8943; &times; <i>N<sub>n</sub></i>
 * </pre>
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Jan 17, 2026
 *
 * @param <Param>       Enumeration of test case parameters
 * @param <TestCase>    Record type defining a test case 
 */
public abstract class TestSuiteGeneratorBase<Param extends Enum<Param>, TestCase extends Record> {

    
    //
    // Abstract Methods
    //
    
    /**
     * <p>
     * Checks that the given parameter value is of the correct data type for the given parameter.
     * </p>
     * <p>
     * The implementation should perform a type check of the form <code>{@link Class#isInstance(Object)}</code>
     * or, more loosely, <code>{@link Class#isAssignableFrom(Class)}</code>.
     * Typically this can be performed by equipping the <code>Param</code> enumeration with type checking
     * capability. 
     * </p>
     * 
     * @param enmParam  the parameter whose value to be assigned
     * @param objVal    the parameter value to be included in the test suite
     * 
     * @return  <code>true</code> if the given <code>Object</code> can be assigned to the given parameter 
     */
    abstract protected boolean     isValidType(Param enmParam, Object objVal);
    
    /**
     * <p>
     * Creates a <code>TestCase</code> record from the map of (Parameter, Value) pairs.
     * </p>
     * <p>
     * Creates and returned a new <code>TestCase</code> record whose fields are populated with the given map
     * of (<code>Param, Object</code>) parameter-name parameter-value pairs.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Implementation should check the types of the values before value assignment, the <code>ClassCastException</code>
     *     is available for inappropriate assignment attempts.
     * </li>
     * <li>Implementations should check that all <code>Param</code> constants are contained in the map key set.
     *     The <code>MissingResourceException</code> is available for missing parameter values.
     * </li>
     * </ul>
     * </p>
     * 
     * @param mapTestVals   map of (Parameter, Value) pairs used to populate the returned test case record
     * 
     * @return  new <code>TestCase</code> record populated with the given argument values.
     * 
     * @throws MissingResourceException a parameter and/or parameter value was missing (map incomplete)
     * @throws ClassCastException       a parameter value was of inappropriate type
     * @throws UnsupportedOperationException an unknown parameter was encountered
     */
    abstract protected  TestCase    createTestCase(Map<Param, Object> mapTestVals) throws MissingResourceException, ClassCastException, UnsupportedOperationException;
    

    //
    // Class Constants
    //
    
    /** Minimum padding between parameter name and values list when none can be determined */
    public static final int                     STR_PAD_NM_PARAM = 10;

    
    //
    // Instance Resources
    //
    
    /** The enumeration class type of the parameter set enumeration */
    protected final Class<Param>              clsParams;
    
    /** The collection of all parameter enumeration constants (obtained from clsParams) */
    protected final EnumSet<Param>            setParams;
    
    /** The (immutable) reverse-ordered collection of all parameter enumeration constants (used for case map generation) */
    protected final List<Param>               lstParamsRev;
    
    /** Map of parameter to collection of parameter test values */
    protected final Map<Param, List<Object>>  mapParamToVals;
    

    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>TestSuiteGeneratorBase</code> instance.
     * </p>
     *
     * @param clsParams the class type of the parameter enumeration
     */
    protected TestSuiteGeneratorBase(Class<Param> clsParams) {
        this.clsParams = clsParams;
        this.setParams = EnumSet.allOf(this.clsParams);
        this.lstParamsRev = this.setParams.stream().toList().reversed();
        this.mapParamToVals = new HashMap<>();
        
        this.setParams.forEach(enmParam -> this.mapParamToVals.put(enmParam, new LinkedList<>()));
    }

    
    //
    // Utility Methods
    //
    
    /**
     * <p>
     * Determines whether or not the given map of assumed (Parameter, Value) pairs has any missing <code>Param</code> entries.
     * </p>
     * <p>
     * This method is available for child-class implementations of <code>{@link #createTestCase(Map)}</code> when
     * creating <code>TestCase</code> records from mappings of (Parameter, Value) pairs).
     * It is available for exception checking before assembling the test case record.
     * </p>
     * <p>
     * This method defers to <code>{@link #missingParameters(Map)}</code> invoking <code>!{@link Set#isEmpty()}</code>
     * on the returned results of the operation.
     * To get the full set of missing parameter entries for the given map use <code>{@link #missingParameters(Map)}</code>.
     * </p>
     * 
     * @param mapTestVals   mapping of (Parameter, Value) pairs for <code>TestCase</code> record creating
     * 
     * @return  <code>true</code> if the map contains missing parameter key entries,
     *          <code>false</code> if the map is complete
     *          
     * @see #missingParameters(Map)
     */
    public boolean  hasMissingParameters(Map<Param, Object> mapTestVals) {
        return !this.missingParameters(mapTestVals).isEmpty();
    }
    
    /**
     * <p>
     * Determines whether or not the given map of assumed (Parameter, Value) pairs has any null <code>Value</code> entries.
     * </p>
     * <p>
     * This method is available for child-class implementations of <code>{@link #createTestCase(Map)}</code> when
     * creating <code>TestCase</code> records from mappings of (Parameter, Value) pairs).
     * The given map of (<code>Param</code>, <code>Object</code>) pairs is checked for any <code>null</code> values
     * within the map value set.   
     * </p>
     * <p>
     * This method defers to <code>{@link #missingValues(Map)}</code> invoking <code>!{@link Set#isEmpty()}</code>
     * on the returned results of the operation.
     * To get a set of parameters with unassigned values for the given map use <code>{@link #missingValues(Map)}</code>.
     * </p>
     * 
     * @param mapTestVals   mapping of (Parameter, Value) pairs for <code>TestCase</code> record creating
     * 
     * @return  <code>true</code> if the map contains missing parameter value entries,
     *          <code>false</code> if the map is complete
     *          
     * @see #missingValues(Map)
     */
    public boolean  hasMissingValues(Map<Param, Object> mapTestVals) {
        return !this.missingValues(mapTestVals).isEmpty();
    }
    
    /**
     * <p>
     * Checks the given map of assumed (Parameter, Value) pairs for any missing <code>Param</code> entries and returned them.
     * </p>
     * <p>
     * This method is available for child-class implementations of <code>{@link #createTestCase(Map)}</code> when
     * creating <code>TestCase</code> records from mappings of (Parameter, Value) pairs).
     * It is available for exception checking before assembling the test case record.
     * </p>
     * <p> 
     * The <code>Param</code> enumeration is assumed to contain all test parameters.
     * The given map of (<code>Param</code>, <code>Object</code>) pairs is checked for completeness of all
     * possible <code>Param</code> constants within the enumeration.  Any enumeration constants that are missing
     * are identified in the returned set.
     * </p>
     * 
     * @param mapTestVals   mapping of (Parameter, Value) pairs for <code>TestCase</code> record creating
     *  
     * @return  the set of any <code>Param</code> constants missing from the given map
     */
    public Set<Param>    missingParameters(Map<Param, Object> mapTestVals) {
//    public static <Param extends Enum<Param>> Set<Param>    missingParameters(Class<Param> clsParam, Map<Param, Object> mapTestVals) {
        EnumSet<Param>      setParams = EnumSet.allOf(this.clsParams);
        
        setParams.removeAll( mapTestVals.keySet() );
        
        return setParams;
    }
    
    /**
     * <p>
     * Checks the given map of assumed (Parameter, Value) pairs for any null <code>Value</code> entries and parameter.
     * </p>
     * <p>
     * This method is available for child-class implementations of <code>{@link #createTestCase(Map)}</code> when
     * creating <code>TestCase</code> records from mappings of (Parameter, Value) pairs).
     * </p>
     * <p> 
     * The <code>Param</code> enumeration is assumed to contain all test parameters.
     * The given map of (<code>Param</code>, <code>Object</code>) pairs is checked for any <code>null</code> values
     * within the map value set.   
     * All enumeration constant map keys that corresponds to a <code>null</code> value are identified in the returned set.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This method does <b>not</b> check for completeness of the map key set.</li>
     * <li>With regard to the above, use method <code>{@link #missingParameters(Class, Map)}</code> first.</li>
     * </ul>
     * </p>
     * 
     * @param mapTestVals   mapping of (Parameter, Value) pairs for <code>TestCase</code> record creating
     *  
     * @return  the set of parameters that have no value assignments
     */
    public Set<Param>    missingValues(Map<Param, Object> mapTestVals) {
//    public static <Param extends Enum<Param>> Set<Param>    missingValues(Class<Param> clsParam, Map<Param, Object> mapTestVals) {
        EnumSet<Param>      setParams = EnumSet.allOf(this.clsParams);
        
        Set<Param>          setMissing = setParams
                .stream()
                .filter(enmParam -> mapTestVals.get(enmParam)==null)
                .collect(Collectors.toSet());
        
        return setMissing;
    }

    
    //
    // State Inquiry
    //
    
    /**
     * <p>
     * Determines whether or not the current test suite configuration is valid.
     * </p>
     * <p>
     * A returned value of <code>true</code> indicates that all parameter in the test suite have at least
     * one test value and that the method <code>{@link #createTestSuit()}</code> can be successfully invoked.
     * </p>
     * <p>
     * This method defers to <code>{@link #unassignedParameters()}</code> invoking <code>{@link Set#isEmpty()}</code>
     * on the result.  To obtain a set of parameters with unassigned values for the current configuration 
     * use <code>{@link #unassignedParameters()}</code>.
     * </p>
     * 
     * @return  <code>true</code> if the test suite is capable of generation, <code>false</code> otherwise
     * 
     * @see #unassignedParameters()
     */
    public boolean isValidConfiguration() {
        
        // Check that all parameters have at least one value (i.e., the set of missing parameter values is empty)
        boolean bolResult = this.unassignedParameters().isEmpty();
        
        return bolResult;
    }
    
    /**
     * <p>
     * Computes and returns the current number of test cases within the test suite.
     * </p>
     * <p>
     * Iterates through the parameters of the test suite (i.e., the <code>Param</code> enumeration) summing
     * all the parameter values for each parameter. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * If the current test suite contains missing parameter values (i.e., no all parameters have been assigned values)
     * then the method returns 0.  Check this condition with <code>{@link #unassignedParameters()}</code>.
     * </p>
     * 
     * @return  total number of test cases within the current test suite, 0 if any missing parameter values 
     */
    public int testCaseCount() {
        
        // Check configuration
        if (!this.isValidConfiguration())
            return 0;
        
        int cntCases = this.mapParamToVals
                .entrySet()
                .stream()
                .mapToInt(entry -> entry.getValue().size())
                .reduce(1, (i1, i2) -> i1*i2);
        
        return cntCases;
    }
    
    /**
     * <p>
     * Computes and returns the set of parameters that have not yet been assigned test values.
     * </p>
     * <p>
     * The returned set contains all parameters (i.e., <code>Param</code> constants) which have yet
     * be assigned any test values within the current test suite.
     * If the returned set is empty (i.e., <code>{@link Set#isEmpty()} == true</code>) then the
     * test suite has a valid configuration state and the method <code>{@link #createTestSuit()}</code>
     * can be invoked successfully.
     * </p>
     *  
     * @return  set of <code>Param</code> constants indicating any parameters that have not been assigned test values
     */
    public Set<Param> unassignedParameters() {
        
        Set<Param>      setMissing = this.setParams
                .stream()
                .filter(enmParam -> this.mapParamToVals.get(enmParam).isEmpty())
                .collect(Collectors.toSet());
        
        return setMissing;
    }
    
    /**
     * <p>
     * Returns the current collection of values for the given parameter.
     * </p>
     * The returned collection is immutable list of values for the given parameter returned in the
     * order in which they were supplied.
     * </p> 
     *
     * @param enmParam  test case parameter
     * 
     * @return  current list of test case values for the given parameter
     */
    public List<Object>  parameterValues(Param enmParam) {
        List<Object>     lstVals = this.mapParamToVals.get(enmParam).stream().collect(Collectors.toList());
        
        return lstVals;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Clears all parameter values from the current collection of test values (i.e., for all parameters).
     * </p>
     * <p>
     * After invoking this operation the test suite generator is returned to its original, unconfigured
     * state upon construction.
     * </p>
     * 
     */
    synchronized
    public void clear() {
        this.mapParamToVals.values().forEach(lst -> lst.clear());
    }
    
    /**
     * <p>
     * Adds the given value to the current collection of values for the given parameter.
     * </p>
     * <p>
     * The given parameter value is added to the current collection of test values for the given parameter.
     * Duplicate parameter test values are rejected and will not be added (method returns <code>false</code>).
     * </p>
     * <p>
     * <h2>Test Case Ordering</h2>
     * Parameter value sets are ordered according to order of addition.
     * Test cases will be generated with ordering defined by the <code>Param</code> natural ordering and the ordering
     * of the test value sets.  
     * <p>
     * <h2>Type Checking</h2>
     * The value <code>Object</code> must be of the correct type for the given <code>Param</code> constant,
     * otherwise an exception is thrown.  This condition is check with the child class supplied 
     * <code>{@link #isValidType(Enum, Object)}</code>.  Hence, if the type-checking method is improperly
     * implemented the value will be added and a <code>ClassCastException</code> will likely be thrown by
     * method <code>{@link #createTestCase(Map)}</code> when attempting to create the test suite.
     * </p>
     * 
     * @param enmParam  test case parameter
     * @param objVal    parameter test value
     * 
     * @return  <code>true</code> if the parameter value was successfully added to the test value collection,
     *          <code>false</code> if the value was rejected (already present in the test value collection) 
     * 
     * @throws IllegalArgumentException    the parameter value type is not appropriate for the parameter 
     */
    synchronized
    public boolean addParameterValue(Param enmParam, Object objVal) throws IllegalArgumentException {
        
        // Check if the value is of proper type for the given parameter
        if (!this.isValidType(enmParam, objVal))
            throw new UnsupportedOperationException(JavaRuntime.getQualifiedMethodNameSimple() + " - value " + objVal + " is not compatible with parameter " + enmParam);
        
        // Get the value list for this parameter 
        List<Object>      lstVals = this.mapParamToVals.get(enmParam);
        
        // Check if value already present
        if (lstVals.contains(objVal))
            return false;
        
        lstVals.add(objVal);
        return true;
    }
    
    /**
     * <p>
     * Adds the given values to the current collection of values for the given parameter.
     * </p>
     * <p>
     * The given collection of parameter values is added to the current collection of test values for the given parameter.
     * Duplicate parameter test values are rejected and will not be added (method returns <code>false</code>).
     * </p>
     * <p>
     * This is a convenience method that performs repeated invocations of 
     * <code>{@link #addParameterValue(Enum, Object)}</code> for each parameter value in the given collection.
     * </p>
     * <p>
     * <h2>Test Case Ordering</h2>
     * Parameter value sets are ordered according to the ordering of the value collection.
     * Test cases will be generated with ordering defined by the <code>Param</code> natural ordering and the ordering
     * of the test value sets.  
     * <p>
     * <h2>Type Checking</h2>
     * The value <code>Object</code> must be of the correct type for the given <code>Param</code> constant,
     * otherwise an exception is thrown.  This condition is check with the child class supplied 
     * <code>{@link #isValidType(Enum, Object)}</code>.  Hence, if the type-checking method is improperly
     * implemented the value will be added and a <code>ClassCastException</code> will likely be thrown by
     * method <code>{@link #createTestCase(Map)}</code> when attempting to create the test suite.
     * </p>
     * 
     * @param enmParam  test case parameter
     * @param conVals   collection of test values to add to test suite 
     * 
     * @return  <code>true</code> if all parameter values were successfully added to the test value collection,
     *          <code>false</code> if any value in the given collection was rejected (already present in the test value collection) 
     * 
     * @throws IllegalArgumentException    a parameter value type was not appropriate for the parameter
     */
    synchronized
    public boolean  addParameterValues(Param enmParam, Collection<Object> conVals) throws IllegalArgumentException {
        
        boolean bolResult = true;
        
        for (Object objVal : conVals)
            bolResult = bolResult && this.addParameterValue(enmParam, objVal);
        
        return bolResult;
    }
    
    /**
     * <p>
     * Parses the application command-line arguments for the test suite configuration.
     * </p>
     * <p>
     * This method can be used only if the <code>Param</code> enumeration implements the <code>{@link ITestParameter}</code>
     * interface or an exception is thrown.
     * The method is intended for specialized creators use in child classes.
     * </p>
     * <p>
     * The method iterates through the test suite parameters as enumerated in <code>Param</code>.
     * All <code>{@link ITestParameter#getParameterDelimOption()}</code> variables are identified in the
     * command-line arguments, their values extracted, then used for parameter values in the test suite
     * configuration using <code>{@link #addParameterValue(Enum, Object)}</code>. 
     * </p>
     * <p>
     * The values for each parameter are extracted from the given <code>String[]</code> argument,
     * assumed to be the collection of command-line arguments for a Java application <code>main(String[])</code> method. 
     * The application test parameters values are extracted from the command-line arguments using the given
     * <code>{@link AppOptionsParser}</code> and added to the current test suite configuration.  
     * If the command line does not provide values for a parameter the default value
     * is assigned as given by <code>{@link ITestParameter#getDefaultValue()}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This method is the source of all
     * exceptions thrown except the <code>ClassCastException</code>, which is thrown if the <code>Param</code>
     * enumeration does not implement the <code>{@link ITestParameter}</code> interface.
     * <ul>
     * <li>The <code>AppOptionsParser</code> instance is assumed to be configured for the application command-line arguments.</li>
     * <li>All parameter values are extracted using the <code>{@link AppOptionsParser#parseVariable(String, String...)}</code>
     *     operation.</li>
     * <li>The string tokens within the command line are converted to <code>Object</code> values of the appropriate
     *     type using <code>{@link ITestParameter#parseValue(String)}</code>.</li>
     * <li>The above operation is the source of all exceptions thrown except the <code>ClassCastException</code>.</li>
     * <li>The <code>ClassCastException</code> is thrown if the <code>Param</code> enumeration does not implement the 
     *     <code>{@link ITestParameter}</code> interface.</li>
     * </p>
     * 
     * @param args  the application command-line arguments
     * 
     * @throws ClassCastException       the <code>Param</code> enumeration does not implement <code>ITestParameter</code>
     * @throws IllegalArgumentException general error (typically bad argument type, bad argument count, enumeration constant not recognized)
     * @throws NoSuchMethodException    the Java class <code>{@link #getJavaType()}</code> does not contain method <code>valueOf(String)</code>
     * @throws SecurityException        the class loader denied access to method <code>valueOf(String)</code> (e.g., typically package access)
     * @throws IllegalAccessException   the method <code>valueOf(String)</code> is not accessible
     * @throws InvocationTargetException    the <code>valueOf(String)</code> method threw an exception (e.g., NumberFormatException)
     * @throws DateTimeParseException   invalid ISO-8605 date/time/duration format for 'period', 'start', or 'delay' 
     * @throws TypeNotPresentException  invalid enumeration constant (e.g., the 1st argument was not a <code>JalComplexType</code>)
     * @throws NumberFormatException    invalid numeric expression (typically for 'lngSeed' value)
     * @throws ConfigurationException   the argument contained the wrong number of arguments for the <code>JalComplexType</code>
     * @throws UnsupportedOperationException invalid field value format (typically 'numIncr' was invalid)
     * @throws MalformedParametersException  an enumeration constant within the argument set was not recognized (IMAGE)
     * @throws NoSuchElementException   the column data type was unrecognized (i.e., 'DTYPE' was not supported)
     */
    synchronized
    public void parseParameterValues(AppOptionsParser parser, String...args) 
            throws ClassCastException, UnsupportedOperationException, NoSuchMethodException, SecurityException, IllegalAccessException, 
            InvocationTargetException, DateTimeParseException, NumberFormatException, IllegalArgumentException, 
            TypeNotPresentException, ConfigurationException, MalformedParametersException 
    {
        // Convert parameter set to ITestParameter
        @SuppressWarnings("unchecked")
        Set<ITestParameter<Param>>  setIParams = this.setParams.stream().map(p -> (ITestParameter<Param>)p).collect(Collectors.toSet());
        
        // For each parameter
        for (ITestParameter<Param> ifcParam : setIParams) {
        
            // Parse the command line for parameter values
            List<String>    lstStrVals = parser.parseVariable(ifcParam.getParameterDelimOption(), args);
            
            // If empty use default parameter value
            if (lstStrVals.isEmpty()) {
                this.addParameterValue(ifcParam.getParameterConstant(), ifcParam.getDefaultValue());  // throws IllegalArgumentException
                
                continue;
            }
            
            // Otherwise convert parameter value strings to value objects and add to test suite
            for (String strVal : lstStrVals) {
                Object  objVal = ifcParam.parseValue(strVal);   // throws all exceptions
                
                this.addParameterValue(ifcParam.getParameterConstant(), objVal);
            }
        }
    }
    
    /**
     * <p>
     * Creates and returns a test suite collection of <code>TestCase</code> records according to the current configuration.
     * </p>
     * <p>
     * This is the target method for <code>TestSuiteGeneratorBase</code> derived classes.  Once configured,
     * the method generates all test cases according to the class parameters defined by enumeration 
     * <code>Param</code> and parameter test values supplied by clients with method
     * <code>{@link #addParameterValue(Enum, Object)}</code>.  At least one test value is required for each
     * class parameter <code>Param</code> constant, otherwise an exception is thrown.
     * </p> 
     * <p>
     * <h2>Operation</h2>
     * Creates and returns the collection of <code>TestCase</code> records by iterating over all parameters and
     * parameter values within the current configuration.  The subclass-supplied method 
     * <code>{@link #createTestCase(Map)}</code> is invoked for every parameter test value combination
     * and the resulting <code>TestCase</code> record is aggregated into the returned collection.
     * </p>
     * <p>
     * <h2>Test Case Ordering</h2>
     * Parameter value sets are ordered according to order of addition with method 
     * <code>{@link #addParameterValue(Enum, Object)}</code>.
     * Test cases will be generated with ordering defined by the <code>Param</code> natural ordering and the ordering
     * within the test value sets.  
     * <p>
     * 
     * @return  a new collection of <code>TestCase</code> records spanning the domain of current parameter values
     * 
     * @throws IllegalStateException    invalid test suite configuration (missing at least one parameter value)
     * @throws MissingResourceException attempted to make a <code>TestCase</code> with missing parameter and/or parameter value
     * @throws ClassCastException       test case parameter value had invalid type  
     * @throws UnsupportedOperationException an unknown parameter was encountered
     * @throws IndexOutOfBoundsException     internal error - attempted to compute test case greater than the number of cases
     */
    synchronized
    public Collection<TestCase> createTestSuit() throws IllegalStateException, MissingResourceException, ClassCastException, UnsupportedOperationException, IndexOutOfBoundsException {

        // Check for valid configuration
        if (!this.isValidConfiguration())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - missing value(s) for parameter(s) : " + this.unassignedParameters());

        // Create the returned container of test cases
        Collection<TestCase>    conTestCases = new LinkedList<>();
        
        for (int iCase=0; iCase<this.testCaseCount(); iCase++) {
            
            Map<Param, Object>  mapTestCase = this.createCaseMap(iCase);        // throws IndexOutOfBoundsException
            TestCase            recTestCase = this.createTestCase(mapTestCase); // throws MissingResourceException, ClassCastException, UnsupportedOperationException
            
            conTestCases.add(recTestCase);
        }
        
        return conTestCases;
    }
    

    /**
     * <p>
     * Prints out text description of the current test suite configuration to the given output stream.
     * </p>
     * <p>
     * The <code>strPad</code> is assumed to be optional white space characters providing left-hand
     * side padding to the field headers.
     * </p>
     * 
     * @param ps        output stream to receive text description
     * @param strPad    optional left-hand side white space padding (or <code>null</code>)
     */
    public void printOut(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        String  strPadd = strPad + "  ";
        int     intPadNm = this.setParams.stream().map(enm -> enm.name()).mapToInt(nm -> nm.length()).max().orElse(STR_PAD_NM_PARAM);

        // Print out test case parameters
        ps.println(strPad + "Parameter set : " + this.setParams);
        
        // Print out test case parameter values
        ps.println(strPad + "Parameter Values ");
        for (Map.Entry<Param, List<Object>> entry : this.mapParamToVals.entrySet()) {
            Param           enmParam = entry.getKey();
            String          strName = enmParam.name();
            List<Object>    lstVals = entry.getValue();
            String          strLine = String.format("%s%-" + intPadNm + "s : %s", strPadd, strName, lstVals.toString());
            
            ps.println(strLine);
        }
    }
    
    
    //
    // Object Overrides
    //
 
    /**
     * <p>
     * Checks for configuration equivalence of the given test suite generator with this test suite generator.
     * </p>
     * <p>
     * The method first checks that the given argument is a sub-class of <code>TestSuiteGeneratorBase</code>.
     * The current configuration of both instances are then compared, specifically, the test values
     * for each parameter are checked for "equality" as object (i.e., <code>{@link Object#equals(Object)</code>).
     * If both test suite generators are configured the same then the method returns true.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This is not a check for strict equality of instances.  The test suite generator under test can be
     * a different instance but have the same current configuration (i.e., will produce the same test suites)
     * and the method will return true.
     * 
     * @return  <code>true</code> if the given argument is a <code>TestSuiteGeneratorBase</code> sub-class with the same configuration,
     *          <code>false</code> otherwise
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestSuiteGeneratorBase gen) {
            boolean bolResult = this.clsParams.equals(gen.clsParams)
                             && this.mapParamToVals.equals(gen.mapParamToVals);
            
            return bolResult;
        }
        
        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        int intPad = this.setParams.stream().map(enm -> enm.name()).mapToInt(nm -> nm.length()).max().orElse(STR_PAD_NM_PARAM);

        StringBuilder   buf = new StringBuilder();
        
        buf.append("Parameter enumeration  : " + this.clsParams.getName() + "\n");
        buf.append("Parameter constants    : " + this.setParams + "\n");
        buf.append("Parameter Test Values \n");
        for (Param enmParam : this.setParams) {
            String          strName = enmParam.name();
            List<Object>    lstVals = this.mapParamToVals.get(enmParam);
            String          strLine = String.format("  %-" + intPad + "s : %s\n", strName, lstVals.toString());
            
            buf.append(strLine);
        }
        
        return buf.toString();
    }


    //
    // Support Methods
    //
 
//    /**
//     * <p>
//     * Creates a new map of (Param, Value) pairs for the given parameter value index map, then advances index map.
//     * </p>
//     * <p>
//     * The given index map is first checked for completion, specifically, each parameter index is verified
//     * to be less than the parameter value collection size.
//     * The then method iterates through the collection <code>{@link #setParams}</code> of parameters, assigning
//     * the test value at index <code>mapIndexes</code> to the returned (Param, Value) map.
//     * </p>
//     * <p>
//     * <h2>Index Increment</h2>
//     * The index map is then advanced in reverse order, that is, from the last <code>Param</code> constant
//     * to the first.
//     * Indexes are incremented until the index achieves a maximum, which is the number of
//     * parameter test values - 1 (Java indices are 0 based).  
//     * The incrementing then proceeds to the next parameter (in reverse order).
//     * </p>
//     *  
//     * @param mapIndexes    map of (Param, Value List Index) pairs 
//     * 
//     * @return  map of (Param, Val[i]) where i is taken from the given map
//     * 
//     * @throws NoSuchElementException   no more test values to process, the test suite is exhausted
//     */
//    private Map<Param, Object> createCaseMap(Map<Param, Integer> mapIndexes) throws NoSuchElementException {
//        
//        // Check if complete
////        boolean bolFinish = mapIndexes.entrySet()
////                .stream()
////                .allMatch(entry -> this.mapParamToVals.get(entry.getKey()).size() >= (entry.getValue()+1));
//        
//        boolean bolFinish = this.mapParamToVals.entrySet()
//                .stream()
//                .allMatch(entry -> mapIndexes.get(entry.getKey()) + 1 >= entry.getValue().size());
//        
//        if (bolFinish)
//            throw new NoSuchElementException("The parameter value sets have been exhausted: indices=" + mapIndexes);
//        
//        
//        // Create the case map according to the current parameter index map
//        Map<Param, Object> mapCase = new HashMap<>();
//        
//        for (Param enmParam : this.setParams) {
//            Integer     indVal = mapIndexes.get(enmParam);
//            Object      objVal = this.mapParamToVals.get(enmParam).get(indVal);
//            
//            mapCase.put(enmParam, objVal);
//        }
//        
//        // Advance index map for next case (in reverse order)
//        List<Param> lstParams = this.setParams.stream().toList().reversed(); 
//        for (Param enmParam : lstParams) {
//            Integer     indCurr = mapIndexes.get(enmParam);
//            Integer     indMax = this.mapParamToVals.get(enmParam).size() - 1;
//            
//            // Check if this parameter index is complete
//            if (indCurr >= indMax)
//                continue;
//            
//            // Advance parameter index and break (only advance one parameter index at a time)  
//            indCurr = indCurr + 1;
//            mapIndexes.put(enmParam, indCurr);
//            
//            // Reset all indexes after the incremented parameter index
//            int     indReset = lstParams.lastIndexOf(enmParam) + 1;
//            if (indReset >= lstParams.size()) // This is the last parameter - do nothing
//                break;
//            
//            ListIterator<Param> itrParams = lstParams.listIterator(indReset);
//            while (itrParams.hasNext()) {
//                Param   enmReset = itrParams.next();
//                mapIndexes.put(enmReset, 0);
//            }
//            break;
//        }
//        
//        // Return the test case map
//        return mapCase;
//    }
    
    /**
     * <p>
     * Creates a new map of (Param, Value) pairs for the given parameter value index map, then advances index map.
     * </p>
     * <p>
     * The given index is first checked for completion, specifically, the index must be less than
     * <code>{@link #testCaseCount()}</code> or an exception is thrown.
     * The given index is then converted from base 10 to the "Parameter Base" representation (see below) which
     * are the natural indexes for each parameter test value (i.e., for the argument index).
     * The returned (Parameter, Value) map is then created using the natural index set for each parameter
     * in <code>Param</code>.
     * </p>
     * <p>
     * <h2>Parameter Base Representation</h2>
     * The key to this operation is the use of the "Parameter Base" representation for the given test case index.
     * All numbers can be represented in arbitrary bases, the most common are base 10, base 2, and base 16 (hexadecimal).
     * The given index is assumed to be in base 10 representation.
     * </p>
     * <p>
     * We define the <em>Parameter Base</em> representation for any number <i>N</i> as the following:
     * <pre>
     *      <i>N</i> = <i>d</i><sub><i>n</i>-1</sub>...<i>d</i><sub>1</sub><i>d</i><sub>0</sub>,
     *      
     *      <i>d<sub>i</sub></i> &in; {0, 1, ..., <i>N<sub>i</sub></i>}
     * </pre> 
     * where
     * <ul>
     * <li><i>n</i> is the number of parameters {<i>P</i><sub>0</sub>, <i>P</i><sub>0</sub>, ..., <i>P</i><sub><i>n</i>-1</sub>}.</li>
     * <li><i>N<sub>i</sub></i> is the number of values for parameter <i>P<sub>i</sub></i>.</li>
     * <li><i>d<sub>i</sub></i> is the digit representation for parameter position <i>i</i>.</li>
     * </ul> 
     * Note that converting the index to the Parameter Base representation will iterate through the parameter
     * value ordering from first addition to last addition when incrementing the given index.
     * </p>
     * <p>
     * We choose the least significant digit for <i>N</i> to be <i>d</i><sub><i>n</i>-1</sub>, the last parameter of 
     * enumeration <code>Param</code> (i.e., <i>P</i><sub><i>n</i>-1</sub>) and the most significant digit to be
     * <i>d</i><sub>0</sub> the first parameter of enumeration <code>Param</code> (i.e., <i>P</i><sub>0</sub>).
     * In this fashion the cases will vary through the last parameter values first.  The 
     * next to last parameter value is then incremented and the process moves through all last parameter values.  
     * This general process continues for all test case indexes in the set {0, ..., {@link #testCaseCount()}}.
     * </p> 
     * @implNote
     * Conversion to Parameter Base representation is done through standard division where the digit
     * <i>d</i><sub>0</sub> is the remainder division of the given index <i>I</i> by <i>N</i><sub>0</sub>
     * (i.e., the modulus <i>d</i><sub>0</sub> = <i>I</i> % <i>N</i><sub>0</sub>).  The quotient, say 
     * <i>Q</i><sub>0</sub> = <i>I</i> / <i>N</i><sub>0</sub>,
     * is then used as the dividend for the next digit <i>d</i><sub>1</sub>.  Specifically, <i>d</i><sub>1</sub>
     * = <i>Q</i><sub>0</sub> % <i>N</i><sub>1</sub> and <i>Q</i><sub>1</sub> = <i>Q</i><sub>0</sub> / <i>N</i><sub>1</sub>.
     * All digits for the Parameter Base representation are computed by repeating the process.
     * </p>
     *  
     * @param indCase   test case index for case map to generate, in set {0, ..., {@link #testCaseCount()}} 
     * 
     * @return  map of (Param, Val[d<sub>i</sub>]) where d<sub>i</sub> is the natural index for Param P<sub>i</sub> after conversion
     * 
     * @throws IndexOutOfBoundsException   no more test values to process, the test suite is exhausted (i.e., index 
     */
    private Map<Param, Object> createCaseMap(int indCase) throws IndexOutOfBoundsException {
        
        // Check if case index is out of bounds
        if (indCase >= this.testCaseCount())
            throw new IndexOutOfBoundsException("The parameter value sets have been exhausted: index=" + indCase);
        
        // Convert the case index from base 10 to base "Parameter Size"
        int[]       arrDigits = new int[this.setParams.size()]; // the index in "Base Parameter"

        boolean     bolFirst = true;
        int         intDigit = 0;
        int         intCarry = 0;
        for (Param p : this.lstParamsRev) {
            int     indDigit = this.lstParamsRev.lastIndexOf(p);
            int     szDigit = this.mapParamToVals.get(p).size();

            // First time through we need to explicitly compute the index digit and carry over against base-10 index
            if (bolFirst) {
                intDigit = indCase % szDigit;
                intCarry = indCase / szDigit;
                
                arrDigits[indDigit] = intDigit;
                
                bolFirst = false;
                continue;
            }
            
            // If there is no carry over all digits from here are zero 
            if (intCarry == 0) {
                arrDigits[indDigit] = 0;
                continue;
            }
            
            // Perform size division on carry over: remainder is index digit, quotient is next carry over 
            intDigit = intCarry % szDigit;
            intCarry = intCarry / szDigit;
            
            arrDigits[indDigit] = intDigit;
        }

        // Create the case map, populate it, then return
        Map<Param, Object>  mapValues = new HashMap<>();
        
        for (Param p : this.lstParamsRev) {
            int     indDigit = this.lstParamsRev.lastIndexOf(p);
            int     indVal = arrDigits[indDigit];
            Object  objVal = this.mapParamToVals.get(p).get(indVal);
            
            mapValues.put(p, objVal);
        }
        
        return mapValues;
    }
    
}
