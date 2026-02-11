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
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.stream.Collectors;

import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Base class for classes generating test suites of test cases for evaluation and scoring.
 * </p>
 * <p>
 * Child classes must implement the operation <code>{@link #createTestCase(Map)}</code> that creates a
 * <code>{@link TestCase}</code> record from a map of (Parameter, Value) pairs.
 * Each <code>TestCase</code> record contains a test case configuration.  The assumption
 * is that of an application performing a set of evaluations on a (software) component and/or system.
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
 * is established.  Use method <code>{@link #missingValues()}</code> to obtain a collection of 
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
    // Utility Methods
    //
    
    /**
     * <p>
     * Checks the given map of assumed (Parameter, Value) pairs for any missing <code>Param</code> entries and returned them.
     * </p>
     * <p>
     * This method is available for child-class implementations of <code>{@link #createTestCase(Map)}</code> for
     * creating <code>TestCase</code> records from mappings of (Parameter, Value) pairs).
     * </p>
     * <p> 
     * The <code>Param</code> enumeration is assumed to be that of the class generic parameter <code>Param</code>.
     * The given map of (<code>Param</code>, <code>Object</code>) pairs is checked for completeness of all
     * possible <code>Param</code> constants within the enumeration.  Any enumeration constants that are missing
     * are identified in the returned set.
     * </p>
     * 
     * @param <Param>       Enumeration of test case parameters
     * 
     * @param clsParam      class type of <code>Param</code> enumeration
     * @param mapTestVals   mapping of (Parameter, Value) pairs for <code>TestCase</code> record creating
     *  
     * @return  the set of any <code>Param</code> constants missing from the given map
     */
    public static <Param extends Enum<Param>> Set<Param>    missingParameters(Class<Param> clsParam, Map<Param, Object> mapTestVals) {
        EnumSet<Param>      setParams = EnumSet.allOf(clsParam);
        
        setParams.removeAll( mapTestVals.keySet() );
        
        return setParams;
    }
    
    /**
     * <p>
     * Checks the given map of assumed (Parameter, Value) pairs for any null <code>Value</code> entries and parameter.
     * </p>
     * <p>
     * This method is available for child-class implementations of <code>{@link #createTestCase(Map)}</code> for
     * creating <code>TestCase</code> records from mappings of (Parameter, Value) pairs).
     * </p>
     * <p> 
     * The <code>Param</code> enumeration is assumed to be that of the class generic parameter <code>Param</code>.
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
     * @param <Param>       Enumeration of test case parameters
     * 
     * @param clsParam      class type of <code>Param</code> enumeration
     * @param mapTestVals   mapping of (Parameter, Value) pairs for <code>TestCase</code> record creating
     *  
     * @return
     */
    public static <Param extends Enum<Param>> Set<Param>    missingValues(Class<Param> clsParam, Map<Param, Object> mapTestVals) {
        EnumSet<Param>      setParams = EnumSet.allOf(clsParam);
        
        Set<Param>          setMissing = setParams
                .stream()
                .filter(enmParam -> mapTestVals.get(enmParam)==null)
                .collect(Collectors.toSet());
        
        return setMissing;
    }

    
    //
    // Class Constants
    //
    
    /** Minimum padding between parameter name and values list when none can be determined */
    public static final int STR_PAD_NM_PARAM = 10;

    
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
     * 
     * @return  <code>true</code> if the test suite is capable of generation, <code>false</code> otherwise
     */
    public boolean isValidConfiguration() {
        
        // Check that all parameters have at least one value (i.e., the set of missing parameter values is empty)
        boolean bolResult = this.missingValues().isEmpty();
        
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
     * then the method returns 0.  Check this condition with <code>{@link #missingValues()}</code>.
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
    public Set<Param> missingValues() {
        
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
        Set<Param>  setMissing = this.missingValues();
        if (!setMissing.isEmpty())
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - missing value(s) for parameter(s) : " + setMissing);

        // Create the returned container of test cases
        Collection<TestCase>    conTestCases = new LinkedList<>();
        
//        // Create the map of initial (Param, Index) pairs used to iterated through all parameter values
//        //  Note that this map is modified by createCaseMap() - indices are incremented bottom up
//        Map<Param, Integer> mapIndices = this.setParams
//                .stream()
//                .collect(Collectors.toMap(enmParam -> enmParam, enmParam -> Integer.valueOf(0)) 
//                 );
//                
//        // Initialize the test case creation loop
//        boolean     bolComplete = false;    // Test case completion flag
//        while (!bolComplete) {              // Create test cases until createCaseMap() signals exhaustion
//            
//            try {
//                Map<Param, Object>  mapTestCase = this.createCaseMap(mapIndices);   // throws NoSuchElementException
//                TestCase            recTestCase = this.createTestCase(mapTestCase); // throws MissingResourceException, ClassCastException, UnsupportedOperationException
//                
//                conTestCases.add(recTestCase);
//                
//            } catch (NoSuchElementException e) {
//                bolComplete = true;
//            }
//        }
        
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
