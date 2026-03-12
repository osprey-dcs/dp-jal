/*
 * Project: dp-jal
 * File:	TestResultExtremesBase.java
 * Package: com.ospreydcs.dp.jal.tools.common.score
 * Type: 	TestResultExtremesBase
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
 * @since Feb 10, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.score;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import  java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ospreydcs.dp.jal.util.JavaRuntime;;

/**
 * <p>
 * Base class containing common functions for analyzing the extreme test result field values to test parameter values.
 * </p>
 * <p>
 * The objective is to monitor the variation of test result field value (i.e., within <code>TestResult</code>) for the
 * variations of a test parameter values (test parameters are identified in enumeration <code>Param</code>).
 * Test parameter to field value associations are defined in the child class using abstract methods
 * <code>{@link #assignNumberAssociations()}</code> and <code>{@link #assignDurationAssociatios()}</code>.
 * These implementation must be well-defined at construction as they are called within the constructor
 * of this base class.
 * </p>
 * <p>
 * All computations are performed at construction and results are immediately available.  Use method
 * <code>{@link #printOut(PrintStream, String)}</code> to print out computational results to the
 * given output stream.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Feb 10, 2026
 *
 * @param <Param>       The full parameter set of the test evaluations
 * @param <TestResult>  Record containing test results of an evaluation
 */
public abstract class TestResultExtremesBase<Param extends Enum<Param>, TestResult extends Record> {

    
    //
    // Internal Types
    //
    
    /**
     * <p>
     * Enumeration of supported numeric field types for extreme field value statistical calculations.
     * </p>
     */
    public static enum NumberType {
        INTEGER,
        LONG,
        DOUBLE,
        ;
    }
    
//    /**
//     * <p>
//     * Record defining a test result field with numeric type for overall statistical extreme values calculations.
//     * </p>  
//     *
//     * @param <TestResult>  Record containing test results of an evaluation
//     * 
//     * @param strFldDesc    description of the target field, i.e., field name 
//     * @param enmFldType    the field type of the <code>TestResult</code> field associated with the parameter  
//     * @param fncCaseInd    lambda function the test case index from a <code>TestRsult</code> record
//     * @param fncFldVal     lambda function extracting the field value from a <code>TestResult</code> record
//     */
//    protected static record ResultNumberField<TestResult extends Record>   (
//            String                          strFldDesc,
//            NumberType                      enmFldType,
//            Function<TestResult, Integer>   fncCaseInd,
//            Function<TestResult, Number>    fncFldVal
//            )
//    {
//        /**
//         * <p>
//         * Creates and returns a new <code>ResultNumberField</code> record populated with the given arguments.
//         * </p>
//         * <p>
//         * These records are used to configure to base class <code>TestResultExtremesBase</code> at construction
//         * using abstract method <code>{@link TestResultExtremesBase#assignNumberFields()}</code>.
//         * </p>
//         * 
//         * @param <TestResult>  Test result record containing field values for analysis
//         * 
//         * @param strFldDesc    description of the target field, i.e., field name 
//         * @param enmFldType    the field type of the <code>TestResult</code> field associated with the parameter  
//         * @param fncCaseInd    lambda function the test case index from a <code>TestRsult</code> record
//         * @param fncFldVal     lambda function extracting the field value from a <code>TestResult</code> record
//         *  
//         * @return  a new <code>ResultNumberField</code> record configured with the given arguments
//         */
//        public static <TestResult extends Record> ResultNumberField<TestResult> from(
//                String                          strFldDesc,
//                NumberType                      enmFldType,
//                Function<TestResult, Integer>   fncCaseInd,
//                Function<TestResult, Number>    fncFldVal
//                )
//        {
//            return new ResultNumberField<TestResult>(strFldDesc, enmFldType, fncCaseInd, fncFldVal);
//        }
//        
//    }
//    
//    /**
//     * <p>
//     * Record defining a test result field with duration type for overall statistical extreme values calculations.
//     * </p>  
//     *
//     * @param <TestResult>  Record containing test results of an evaluation
//     */
//    protected static record ResultDurationField<TestResult extends Record>   (
//            String                          strFldDesc,
//            Function<TestResult, Integer>   fncCaseInd,
//            Function<TestResult, Duration>  fncFldVal
//            )
//    {
//        /**
//         * <p>
//         * Creates and returns a new <code>ResultDurationField</code> record populated with the given arguments.
//         * </p>
//         * <p>
//         * These records are used to configure to base class <code>TestResultExtremesBase</code> at construction
//         * using abstract method <code>{@link TestResultExtremesBase#assignDurationFields()}</code>.
//         * </p>
//         * 
//         * @param <TestResult>  Test result record containing field values for analysis
//         * 
//         * @param strFldDesc    description of the target field, i.e., field name 
//         * @param enmFldType    the field type of the <code>TestResult</code> field associated with the parameter  
//         * @param fncCaseInd    lambda function the test case index from a <code>TestRsult</code> record
//         * @param fncFldVal     lambda function extracting the field value from a <code>TestResult</code> record
//         *  
//         * @return  a new <code>ResultNumberField</code> record configured with the given arguments
//         */
//        public static <TestResult extends Record> ResultDurationField<TestResult> from(
//                String                          strFldDesc,
//                Function<TestResult, Integer>   fncCaseInd,
//                Function<TestResult, Duration>  fncFldVal
//                )
//        {
//            return new ResultDurationField<TestResult>(strFldDesc, fncCaseInd, fncFldVal);
//        }
//    }
    
    /**
     * <p>
     * Record defining an association between test parameter and targeted test result field with numeric type.
     * </p>
     *
     * @param <Param>       Enumeration of the test parameters 
     * @param <TestResult>  Record containing test results of an evaluation
     * 
     * @param enmParam      the target test parameter whose extremes are to be identified
     * @param strFldDesc    description of the target field 
     * @param enmFldType    the field type of the <code>TestResult</code> field associated with the parameter  
     * @param fncParmVal    lambda function extracting the parameter value from a <code>TestRsult</code> record
     * @param fncFldVal     lambda function extracting the field value from a <code>TestResult</code> record
     */
    protected static record ParamToNumField<Param extends Enum<Param>, TestResult extends Record> (
            Param                           enmParam,
            String                          strFldDesc,
            NumberType                      enmFldType,
            Function<TestResult, Object>    fncParmVal,
            Function<TestResult, Number>    fncFldVal
            ) 
    {
        /**
         * <p>
         * Creates and returns a new <code>ParamToNumField</code> record populated with the given arguments.
         * </p>
         * <p>
         * These records are used to configure to base class <code>TestResultExtremesBase</code> at construction
         * using abstract method <code>{@link TestResultExtremesBase#assignNumberAssociations()}</code>.
         * </p>
         * 
         * @param <Param>       Enumeration of the test parameters 
         * @param <TestResult>  Test result record containing field values for analysis
         * 
         * @param enmParam      the target test parameter whose extremes are to be identified
         * @param strFldDesc    description of the target field 
         * @param enmFldType    the field type of the <code>TestResult</code> field associated with the parameter  
         * @param fncParmVal    lambda function extracting the parameter value from a <code>TestRsult</code> record
         * @param fncFldVal     lambda function extracting the field value from a <code>TestResult</code> record
         *  
         * @return  a new <code>ParamToNumField</code> record configured with the given arguments
         */
        public static <Param extends Enum<Param>, TestResult extends Record> ParamToNumField<Param, TestResult> from(
                Param                           enmParam,
                String                          strFldDesc,
                NumberType                      enmFldType,
                Function<TestResult, Object>    fncParmVal,
                Function<TestResult, Number>    fncFldVal
                )
        {
            return new ParamToNumField<Param, TestResult>(enmParam, strFldDesc, enmFldType, fncParmVal, fncFldVal);
        }
    }
    
    /**
     * <p>
     * Record defining an association between test parameter and targeted test result field with <code>Duration</code> type.
     * </p>
     *
     * @param <Param>       Enumeration of the test parameters 
     * @param <TestResult>  Record containing test results of an evaluation
     * 
     * @param enmParam      the target test parameter whose extremes are to be identified
     * @param strFldDesc    description of the target field 
     * @param fncParmVal    lambda function extracting the parameter value from a <code>TestRsult</code> record
     * @param fncFldVal     lambda function extracting the field value from a <code>TestResult</code> record
     */
    protected static record ParamToDurField<Param extends Enum<Param>, TestResult extends Record> (
            Param                           enmParam,
            String                          strFldDesc,
            Function<TestResult, Object>    fncParmVal,
            Function<TestResult, Duration>  fncFldVal
            ) 
    {
        /**
         * <p>
         * Creates and returns a new <code>ParamToDurField</code> record populated with the given arguments.
         * </p>
         * <p>
         * These records are used to configure to base class <code>TestResultExtremesBase</code> at construction
         * using abstract method <code>{@link TestResultExtremesBase#assignDurationParameters()}</code>.
         * </p>
         * 
         * @param <Param>       Enumeration of the test parameters 
         * @param <TestResult>  Test result record containing field values for analysis
         * 
         * @param enmParam      the target test parameter whose extremes are to be identified
         * @param strFldDesc    description of the target field 
         * @param fncParmVal    lambda function extracting the parameter value from a <code>TestRsult</code> record
         * @param fncFldVal     lambda function extracting the field value from a <code>TestResult</code> record
         *  
         * @return  a new <code>ParamToNumField</code> record configured with the given arguments
         */
        public static <Param extends Enum<Param>, TestResult extends Record> ParamToDurField<Param, TestResult> from(
                Param                           enmParam,
                String                          strFldDesc,
                Function<TestResult, Object>    fncParmVal,
                Function<TestResult, Duration>  fncFldVal
                )
        {
            return new ParamToDurField<Param, TestResult>(enmParam, strFldDesc, fncParmVal, fncFldVal);
        }
    }
    
    
    //
    // Abstract methods
    //
    
//    /**
//     * <p>
//     * Assigns the (lambda) function for extracting test case indexes from test result records.
//     * </p>
//     * <p>
//     * This method is called within the base class constructor.
//     * This is a non-vital function but allows the index of the test 
//     * case to be displayed in the output of the analysis.
//     * </p>
//     * 
//     * @return  a lambda function for extracting the test case index from a <code>TestResult</code> record
//     */
//    protected abstract Function<TestResult, Integer>    assignIndexFunction();
    
    /**
     * <p>
     * Sets the lambda function identifying failed test results.
     * </p>
     * <p>
     * Called from base-class constructor.  This is essential for determining any failed results
     * to be excluded from analysis.
     * </p>
     * 
     * @return  lambda function that identifies result failure, i.e., rec -> rec.recStatus.isFailure() 
     */
    protected abstract  Function<TestResult, Boolean>   assignFailedResult();
    
//    /**
//     * <p>
//     * Assigns the test result field for statistical extremes analysis for numeric field types.
//     * <p>
//     * <p>
//     * This method is called within the base class constructor to configure the class instance
//     * for extreme field values analysis.
//     * </p>
//     * <p>
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li>Sub-class implementation must be essentially static and well-defined at construction.</li>
//     * <li>Computed output will appear in the order of the returned list.</li>
//     * </p>
//     *  
//     * @return  ordered list of numeric test result fields for analysis
//     */
//    protected abstract  List<ResultNumberField<TestResult>> assignNumberFields();
//    
//    /**
//     * <p>
//     * Assigns the test result field for statistical extremes analysis for duration field types.
//     * <p>
//     * <p>
//     * This method is called within the base class constructor to configure the class instance
//     * for extreme field values analysis.
//     * </p>
//     * <p>
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li>Sub-class implementation must be essentially static and well-defined at construction.</li>
//     * <li>Computed output will appear in the order of the returned list.</li>
//     * </p>
//     *  
//     * @return  ordered list of duration test result fields for analysis
//     */
//    protected abstract  List<ResultDurationField<TestResult>> assignDurationFields();
    
    /**
     * <p>
     * Assigns the test parameter to field association for numeric field types.
     * <p>
     * <p>
     * This method is called within the base class constructor to configure the class instance
     * for extreme field values analysis.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Sub-class implementation must be essentially static and well-defined at construction.</li>
     * <li>Computed output will appear in the order of the returned list.</li>
     * </p>
     *  
     * @return  ordered list of all parameter to numeric test result field associations for analysis
     */
    protected abstract List<ParamToNumField<Param, TestResult>> assignNumberAssociations();
    
    /**
     * <p>
     * Assigns the test parameter to field association for <code>Duration</code field types.
     * <p>
     * <p>
     * This method is called within the base class constructor to configure the class instance
     * for extreme field values analysis.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Sub-class implementation must be essentially static and well-defined at construction.</li>
     * <li>Computed output will appear in the order of the returned list.</li>
     * </p>
     *  
     * @return  ordered list of all parameter to duration test result field associations for analysis
     */
    protected abstract List<ParamToDurField<Param, TestResult>> assignDurationAssociatios();
    
    
    //
    // Class Constants
    //
    
    /** Minimum padding between parameter name and values list when none can be determined */
    public static final int                 STR_PAD_NM_PARAM = 10;
    
    /** Minimum padding between field description and value when none can be determined */
    public static final int                 STR_PAD_DESC_FIELD = 10;


    //
    // Defining Attributes
    //
    
    /** The enumeration class type of the parameter set enumeration */
    protected final Class<Param>              clsParams;
    
    /** The collection of <code>TestResult</code> records to analyze */
    private final Collection<TestResult>      conResults;
    

    // 
    // Instance Attributes
    //
    
    /** The collection of all parameter enumeration constants (obtained from clsParams) */
    protected final EnumSet<Param>                  setParams;
    
    
//    /** Lambda function for extracting the test case index from a <code>TestResult</code> record. */
//    private final Function<TestResult, Integer>                 fncIndex;
    
    /** lambda function that identifies result failure (i.e., rec -> rec.recStatus.isFailure() ) */
    private final Function<TestResult, Boolean>                 fncFail;

    
//    /** Ordered list of all numeric test result fields for analysis */
//    private final List<ResultNumberField<TestResult>>           lstNumFlds;
//    
//    /** Ordered list of all duration test result fields for analysis */
//    private final List<ResultDurationField<TestResult>>         lstDurFlds;
    
    /** Ordered list of all parameter to test result field associations for numeric field types */
    private final List<ParamToNumField<Param, TestResult>>      lstAssocNum;
    
    /** Ordered list of all parameter to test result field associations for duration field types */
    private final List<ParamToDurField<Param, TestResult>>      lstAssocDur;

    
    //
    // Instance Resources
    //
    
    /** The order list of computed parameter value to test result field extremes for numeric field types */ 
    private final List<ParmToNumExtremes<Param, TestResult>>    lstNumExtremes;
    
    /** The order list of computed parameter value to test result field extremes for duration field types */ 
    private final List<ParmToDurExtremes<Param, TestResult>>    lstDurExtremes;
    
    /**
     * <p>
     * Constructs a new <code>TestResultExtremesBase</code> instance.
     * </p>
     *
     * @throws IllegalArgumentException the argument collection was empty
     * @throws MissingResourceException no <code>TestResult</code> fields were identified for analysis
     * @throws NoSuchElementException   no successful results were contained in the argument collection
     */
    protected TestResultExtremesBase(Class<Param> clsParams, Collection<TestResult> conResults) throws IllegalArgumentException, MissingResourceException, NoSuchElementException {
        
        // Check argument
        if (conResults.isEmpty())
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Test result record collection was empty.");

        // Assign parameters 
        this.clsParams = clsParams;
        this.setParams = EnumSet.allOf(clsParams);
        
        // Acquire target associations and failure checks
//        this.fncIndex = this.assignIndexFunction();
        this.fncFail = this.assignFailedResult();
//        this.lstNumFlds = this.assignNumberFields();
//        this.lstDurFlds = this.assignDurationFields();
        this.lstAssocNum = this.assignNumberAssociations();
        this.lstAssocDur = this.assignDurationAssociatios();
        
        if (this.lstAssocNum.isEmpty() && this.lstAssocDur.isEmpty())
            throw new MissingResourceException("No parameter associations were identified for analysis.", this.getClass().getName(), JavaRuntime.getQualifiedMethodNameSimple());
        
        // Set the test results collection and compute the extremes
        this.conResults = conResults;
        
        this.lstNumExtremes = this.computeParmToNumExtremes(conResults);    // throws NoSuchElementException
        this.lstDurExtremes = this.computeParmToDurExtremes(conResults);    // throws NoSuchElementException
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Prints out a text description of the statistical results the given output.
     * </p>
     * <p>
     * A line-by-line text description of each record field is written to the given output.
     * The <code>strPad</code> is used to supply an optional whitespace character padding to the
     * left-hand side header for each line description.
     * </p>
     *   
     * @param ps        output stream to receive text description of record fields
     * @param strPad    white space padding for left-hand side line headings (or <code>null</code>.
     */ 
    public void printOut(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        
        int         cntTotal = this.conResults.size();
        int         cntFail = this.conResults.stream().filter(rec -> this.fncFail.apply(rec)).mapToInt(rec -> 1).sum();
        Set<Param>  setParms = Stream.concat(this.lstAssocNum.stream().map(rec -> rec.enmParam), this.lstAssocDur.stream().map(rec -> rec.enmParam)).collect(Collectors.toSet());
        
        // Print out results  
        ps.println(strPad + "Number of result cases - TOTAL  : " + cntTotal);
        ps.println(strPad + "Number of result cases - FAILED : " + cntFail);
        ps.println(strPad + "Parameters under evaluation     : " + setParms);
        
        for (ParmToNumExtremes<Param, TestResult> recExtremes : this.lstNumExtremes) {
            recExtremes.printOut(ps, strPad);
        }
        for (ParmToDurExtremes<Param, TestResult> recStats : this.lstDurExtremes) {
            recStats.printOut(ps, strPad);
        }
    }
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Computes the extremes for all numeric <code>TestResult</code> fields identified by <code>{@link #assignNumericAssociations()}</code>.
     * </p> 
     * 
     * @param conResults    collection of all test results
     * 
     * @return  ordered list of extremes for assigned parameter to test result fields with numeric types
     * 
     * @throws NoSuchElementException   there were no successful test results in the argument collection
     */
    private List<ParmToNumExtremes<Param, TestResult>>   computeParmToNumExtremes(Collection<TestResult> conResults) throws NoSuchElementException {
        
        // Check configuration 
        if (this.lstAssocNum.isEmpty())
            return List.of();
        
        // Extract the successful results and check
        List<TestResult>    lstGood = conResults.stream().filter(rec -> !this.fncFail.apply(rec)).toList();
        if (lstGood.isEmpty())
            throw new NoSuchElementException(JavaRuntime.getQualifiedMethodNameSimple() + " - Test result record collection contained all FAILED results.");
        
        // Compute the statistics for the numeric fields
        List<ParmToNumExtremes<Param, TestResult>>   lstExtremes = this.lstAssocNum
                .stream()
                .map(recParm -> ParmToNumExtremes.from(recParm, lstGood))
                .toList();
        
        return lstExtremes;
    }
    
    /**
     * <p>
     * Computes the extremes for all duration <code>TestResult</code> fields identified by <code>{@link #assignDurationAssociations()}</code>.
     * </p> 
     * 
     * @param conResults    collection of all test results
     * 
     * @return  ordered list of extremes for assigned parameter to test result fields with duration types
     * 
     * @throws NoSuchElementException   there were no successful test results in the argument collection
     */
    private List<ParmToDurExtremes<Param, TestResult>>   computeParmToDurExtremes(Collection<TestResult> conResults) throws NoSuchElementException {
        
        // Check configuration 
        if (this.lstAssocNum.isEmpty())
            return List.of();
        
        // Extract the successful results and check
        List<TestResult>    lstGood = conResults.stream().filter(rec -> !this.fncFail.apply(rec)).toList();
        if (lstGood.isEmpty())
            throw new NoSuchElementException(JavaRuntime.getQualifiedMethodNameSimple() + " - Test result record collection contained all FAILED results.");
        
        // Compute the statistics for the numeric fields
        List<ParmToDurExtremes<Param, TestResult>>   lstExtremes = this.lstAssocDur
                .stream()
                .map(recParm -> ParmToDurExtremes.from(recParm, lstGood))
                .toList();
        
        return lstExtremes;
    }
    
    
    //
    // Internal Types - Support
    //
    
//    protected static record NumberFieldExtremes<TestResult extends Record> (
//            ResultNumberField<TestResult>   recFld,
//            Number  numMin,
//            Number  numMax,
//            double  dblAvg,
//            double  dblStd
//            )
//    {
//        protected static <TestResult extends Record> NumberFieldExtremes<TestResult>    from(
//                ResultNumberField<TestResult>   recFld,
//                Collection<TestResult>          conResults
//                )
//        {
//            
//        }
//        
//    }
    
    /**
     * <p>
     * Record containing the field value statistical extremes for a given parameter value.
     * </p>
     *
     * @param <Param>       the enumeration of all test parameters
     * @param <TestResult>  the record containing test result values
     * 
     * @param   recParm         the test parameter to test value association under analysis
     * @param   objValAvgMin    the test parameter value producing the minimum average field value
     * @param   objValAvgMax    the test parameter value producing the maximum average field value
     * @param   objValStdMin    the test parameter value producing the minimum standard deviation of the field value
     * @param   objValStdMax    the test parameter value producing the maximum standard deviation of the field value
     * @param   dblAvgMin       the minimum average field value produced by <code>objValAvgMin</code>
     * @param   dblAvgMax       the maximum average field value produced by <code>objValAvgMax</code>
     * @param   dblStdMin       the minimum standard deviation field value produced by <code>objValStdMin</code>
     * @param   dblStdMax       the maximum standard deviation field value produced by <code>objValStdMax</code>
     */
    protected static record ParmToNumExtremes<Param extends Enum<Param>, TestResult extends Record> (
            ParamToNumField<Param, TestResult>  recParm,
            
            Object  objValAvgMin,
            Object  objValAvgMax,
            Object  objValStdMin,
            Object  objValStdMax,
            
            double  dblAvgMin,
            double  dblAvgMax,
            double  dblStdMin,
            double  dblStdMax
            )
    {
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates a new <code>ParmToNumExtremes</code> record with field values computed from the given arguments.
         * </p>
         * <p>
         * The numeric extremes analysis is performed on the given test result collection for the parameter to 
         * field value association defined by the <code>{@link ParamToNumField}</code> argument.  The analysis results
         * are used to populate a new <code>ParmToNumExtremes</code> record which is then returned.
         * </p>
         *  
         * @param <Param>       Enumeration of the test parameters 
         * @param <TestResult>  Record containing test results of an evaluation
         * 
         * @param recParm       the target parameter to test value association 
         * @param conResults    the test results collection under analysis
         * 
         * @return  a new <code>ParmToNumExtremes</code> record containing the results of the analysis
         */
        protected static <Param extends Enum<Param>, TestResult extends Record> ParmToNumExtremes<Param, TestResult> from(
                ParamToNumField<Param, TestResult>  recParm, 
                Collection<TestResult>              conResults
                ) 
        {
            
            // Get the set of unique parameter values
            Set<Object> setParmVals = conResults
                    .stream()
                    .<Object>map(rec -> recParm.fncParmVal.apply(rec))
                    .collect(Collectors.toSet());

            // Get the average values for each unique parameter value and organize them into a tree map
            TreeMap<Double, Object>     mapAvgToParmVal = new TreeMap<>();
            TreeMap<Double, Object>     mapStdToParmVal = new TreeMap<>();
            
            for (Object objVal : setParmVals) {
                
                // Create the list of field values with corresponding to the given parameter value
                List<Number> lstFldVals = conResults
                        .stream()
                        .filter(rec -> recParm.fncParmVal.apply(rec).equals(objVal))
                        .<Number>map(rec -> recParm.fncFldVal.apply(rec))
                        .toList();
                
                // Average the field values and record
                double dblAvg = switch (recParm.enmFldType) {
                case INTEGER -> lstFldVals.stream().mapToInt(Number::intValue).average().orElse(0);
                case LONG -> lstFldVals.stream().mapToLong(Number::longValue).average().orElse(0);
                case DOUBLE -> lstFldVals.stream().mapToDouble(Number::doubleValue).average().orElse(0);
                };
                mapAvgToParmVal.put(dblAvg, objVal);
                
                // Compute the standard deviation of field values and record
                double dblSqrd = lstFldVals.stream().mapToDouble(Number::doubleValue).map(v -> (v - dblAvg)*(v - dblAvg)).sum();
                double dblStd = Math.sqrt(dblSqrd/lstFldVals.size());
                
                mapStdToParmVal.put(dblStd, objVal);
            }
            
            // Extract the extreme values from the tree map by key value 
            Object      objValAvgMin = mapAvgToParmVal.firstEntry().getValue();
            Object      objValAvgMax = mapAvgToParmVal.lastEntry().getValue();
            Object      objValStdMin = mapStdToParmVal.firstEntry().getValue();
            Object      objValStdMax = mapStdToParmVal.lastEntry().getValue();
            double      dblAvgMin = mapAvgToParmVal.firstEntry().getKey();
            double      dblAvgMax = mapAvgToParmVal.lastEntry().getKey();
            double      dblStdMin = mapStdToParmVal.firstEntry().getKey();
            double      dblStdMax = mapStdToParmVal.lastEntry().getKey();
            
            return ParmToNumExtremes.from(recParm, objValAvgMin, objValAvgMax, objValStdMin, objValStdMax, dblAvgMin, dblAvgMax, dblStdMin, dblStdMax);
        }
        
        /**
         * <p>
         * Creates and returns a new <code>ParmToNumExtremes</code> record from the given arguments.
         * </p>
         * 
         * @param <Param>       Enumeration of the test parameters 
         * @param <TestResult>  Record containing test results of an evaluation
         * 
         * @param   recParm         the test parameter to field value association under analysis
         * @param   objValAvgMin    the test parameter value producing the minimum average field value
         * @param   objValAvgMax    the test parameter value producing the maximum average field value
         * @param   objValStdMin    the test parameter value producing the minimum standard deviation of the field value
         * @param   objValStdMax    the test parameter value producing the maximum standard deviation of the field value
         * @param   dblAvgMin       the minimum average field value produced by <code>objValAvgMin</code>
         * @param   dblAvgMax       the maximum average field value produced by <code>objValAvgMax</code>
         * @param   dblStdMin       the minimum standard deviation field value produced by <code>objValStdMin</code>
         * @param   dblStdMax       the maximum standard deviation field value produced by <code>objValStdMax</code>
         * 
         * @return  a new <code>ParmToNumExtremes</code> record with fields populated from the given arguments
         */
        protected static <Param extends Enum<Param>, TestResult extends Record> ParmToNumExtremes<Param, TestResult> from(
                ParamToNumField<Param, TestResult>  recParm,
                
                Object  objValAvgMin,
                Object  objValAvgMax,
                Object  objValStdMin,
                Object  objValStdMax,
                double  dblAvgMin,
                double  dblAvgMax,
                double  dblStdMin,
                double  dblStdMax
                ) 
        {
            return new ParmToNumExtremes<Param, TestResult>(recParm, objValAvgMin, objValAvgMax, objValStdMin, objValStdMax, dblAvgMin, dblAvgMax, dblStdMin, dblStdMax);
        }
        
        //
        // Operations
        //
        
        /**
         * <p>
         * Prints out a text description of the record contents to the given output.
         * </p>
         * <p>
         * A line-by-line text description of each record field is written to the given output.
         * The <code>strPad</code> is used to supply an optional whitespace character padding to the
         * left-hand side header for each line description.
         * </p>
         *   
         * @param ps        output stream to receive text description of record fields
         * @param strPad    white space padding for left-hand side line headings (or <code>null</code>.
         */ 
        protected void  printOut(PrintStream ps, String strPad) {

            if (strPad == null)
                strPad = "";
            String  strPadd = strPad + "  ";
            
            int     szMaxLen = this.computeMaxParamValDisplay();
            String  strFmt = "%s%s for %s = %-" + szMaxLen + "s: %s = %g";
            String  strParmNm = this.recParm.enmParam.name();
            String  strFldNm = this.recParm.strFldDesc;
            
            ps.println(strPad + "Parameter " + strParmNm + " variation to " + strFldNm + " response");
            ps.println(String.format(strFmt, strPadd, "minimum average value", strParmNm, this.objValAvgMin.toString(), strFldNm, this.dblAvgMin));
            ps.println(String.format(strFmt, strPadd, "maximum average value", strParmNm, this.objValAvgMax.toString(), strFldNm, this.dblAvgMax));
            ps.println(String.format(strFmt, strPadd, "minimum standard dev.", strParmNm, this.objValStdMin.toString(), strFldNm, this.dblStdMin));
            ps.println(String.format(strFmt, strPadd, "maximum standard dev.", strParmNm, this.objValStdMax.toString(), strFldNm, this.dblStdMax));
        }
        
        //
        // Support Methods
        //
        
        /**
         * <p>
         * Computes the maximum length (in characters) of all parameter values displayed as strings.
         * </p>
         * 
         * @return  maximum value of <code>{@link String#length()}</code> for all parameter (object) values
         */
        private int computeMaxParamValDisplay() {
            Stream.Builder<Object>  bldr = Stream.builder();

            bldr.add(this.objValAvgMin).add(this.objValAvgMax).add(this.objValStdMin).add(this.objValStdMax);
            
            int     szMaxLen = bldr.build().<String>map(Object::toString).mapToInt(String::length).max().orElse(STR_PAD_NM_PARAM);
            
            return szMaxLen;
        }
    }
    
    /**
     * <p>
     * Record containing the field value statistical extremes for a given test parameter.
     * </p>
     *
     * @param <Param>       the enumeration of all test parameters
     * @param <TestResult>  the record containing test result values
     * 
     * @param   recParm         the test parameter to test value association under analysis
     * @param   objValAvgMin    the test parameter value producing the minimum average field value
     * @param   objValAvgMax    the test parameter value producing the maximum average field value
     * @param   objValStdMin    the test parameter value producing the minimum standard deviation of the field value
     * @param   objValStdMax    the test parameter value producing the maximum standard deviation of the field value
     * @param   durAvgMin       the minimum average field value produced by <code>objValAvgMin</code>
     * @param   durAvgMax       the maximum average field value produced by <code>objValAvgMax</code>
     * @param   durStdMin       the minimum standard deviation field value produced by <code>objValStdMin</code>
     * @param   durStdMax       the maximum standard deviation field value produced by <code>objValStdMax</code>
     */
    protected static record ParmToDurExtremes<Param extends Enum<Param>, TestResult extends Record> (
            ParamToDurField<Param, TestResult>  recParm,
            
            Object      objValAvgMin,
            Object      objValAvgMax,
            Object      objValStdMin,
            Object      objValStdMax,
            
            Duration    durAvgMin,
            Duration    durAvgMax,
            Duration    durStdMin,
            Duration    durStdMax
            )
    {
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates a new <code>ParmToNumExtremes</code> record with field values computed from the given arguments.
         * </p>
         * <p>
         * The numeric extremes analysis is performed on the given test result collection for the parameter to 
         * field value association defined by the <code>{@link ParamToNumField}</code> argument.  The analysis results
         * are used to populate a new <code>ParmToNumExtremes</code> record which is then returned.
         * </p>
         *  
         * @param <Param>       Enumeration of the test parameters 
         * @param <TestResult>  Record containing test results of an evaluation
         * 
         * @param recParm       the target parameter to test value association 
         * @param conResults    the test results collection under analysis
         * 
         * @return  a new <code>ParmToNumExtremes</code> record containing the results of the analysis
         */
        protected static <Param extends Enum<Param>, TestResult extends Record> ParmToDurExtremes<Param, TestResult> from(
                ParamToDurField<Param, TestResult>  recParm, 
                Collection<TestResult>              conResults
                ) 
        {
            
            // Get the set of unique parameter values
            Set<Object> setParmVals = conResults
                    .stream()
                    .<Object>map(rec -> recParm.fncParmVal.apply(rec))
                    .collect(Collectors.toSet());

            // Get the average values for each unique parameter value and organize them into a tree map
            TreeMap<Duration, Object>     mapAvgToParmVal = new TreeMap<>();
            TreeMap<Duration, Object>     mapStdToParmVal = new TreeMap<>();
            
            for (Object objVal : setParmVals) {
                
                // Create the list of field values with corresponding to the given parameter value
                List<Duration> lstFldVals = conResults
                        .stream()
                        .filter(rec -> recParm.fncParmVal.apply(rec).equals(objVal))
                        .<Duration>map(rec -> recParm.fncFldVal.apply(rec))
                        .toList();
                
                // Average the field values and record
                int         cntVals = lstFldVals.size();
                Duration    durAvg = lstFldVals.stream().reduce(Duration.ZERO, (d1, d2) -> d1.plus(d2)).dividedBy(cntVals); 
                        
                mapAvgToParmVal.put(durAvg, objVal);
                
                // Compute the standard deviation of field values and record
                double      dblNsAvg = Long.valueOf( durAvg.toNanos() ).doubleValue();
                double      dblNsSqrd = lstFldVals.stream().mapToLong(Duration::toNanos).mapToDouble(l -> Long.valueOf(l).doubleValue()).map(ns -> (ns - dblNsAvg)*(ns - dblNsAvg)).sum();
                double      dblNsStd = Math.sqrt(dblNsSqrd/cntVals);
                Duration    durStd = Duration.ofNanos( Double.valueOf(dblNsStd).longValue() );
                
                mapStdToParmVal.put(durStd, objVal);
            }
            
            // Extract the extreme values from the tree map by key value 
            Object      objValAvgMin = mapAvgToParmVal.firstEntry().getValue();
            Object      objValAvgMax = mapAvgToParmVal.lastEntry().getValue();
            Object      objValStdMin = mapStdToParmVal.firstEntry().getValue();
            Object      objValStdMax = mapStdToParmVal.lastEntry().getValue();
            Duration    dblAvgMin = mapAvgToParmVal.firstEntry().getKey();
            Duration    durAvgMax = mapAvgToParmVal.lastEntry().getKey();
            Duration    durStdMin = mapStdToParmVal.firstEntry().getKey();
            Duration    durStdMax = mapStdToParmVal.lastEntry().getKey();
            
            return ParmToDurExtremes.from(recParm, objValAvgMin, objValAvgMax, objValStdMin, objValStdMax, dblAvgMin, durAvgMax, durStdMin, durStdMax);
        }
        
        /**
         * <p>
         * Creates and returns a new <code>ParmToDurExtremes</code> record from the given arguments.
         * </p>
         * 
         * @param <Param>       Enumeration of the test parameters 
         * @param <TestResult>  Record containing test results of an evaluation
         * 
         * @param   recParm         the test parameter to field value association under analysis
         * @param   objValAvgMin    the test parameter value producing the minimum average field value
         * @param   objValAvgMax    the test parameter value producing the maximum average field value
         * @param   objValStdMin    the test parameter value producing the minimum standard deviation of the field value
         * @param   objValStdMax    the test parameter value producing the maximum standard deviation of the field value
         * @param   durAvgMin       the minimum average field value produced by <code>objValAvgMin</code>
         * @param   durAvgMax       the maximum average field value produced by <code>objValAvgMax</code>
         * @param   durStdMin       the minimum standard deviation field value produced by <code>objValStdMin</code>
         * @param   durStdMax       the maximum standard deviation field value produced by <code>objValStdMax</code>
         * 
         * @return  a new <code>ParmToNumExtremes</code> record with fields populated from the given arguments
         */
        protected static <Param extends Enum<Param>, TestResult extends Record> ParmToDurExtremes<Param, TestResult> from(
                ParamToDurField<Param, TestResult>  recParm,
                
                Object      objValAvgMin,
                Object      objValAvgMax,
                Object      objValStdMin,
                Object      objValStdMax,
                Duration    durAvgMin,
                Duration    durAvgMax,
                Duration    durStdMin,
                Duration    durStdMax
                ) 
        {
            return new ParmToDurExtremes<Param, TestResult>(recParm, objValAvgMin, objValAvgMax, objValStdMin, objValStdMax, durAvgMin, durAvgMax, durStdMin, durStdMax);
        }
        
        //
        // Operations
        //
        
        /**
         * <p>
         * Prints out a text description of the record contents to the given output.
         * </p>
         * <p>
         * A line-by-line text description of each record field is written to the given output.
         * The <code>strPad</code> is used to supply an optional whitespace character padding to the
         * left-hand side header for each line description.
         * </p>
         *   
         * @param ps        output stream to receive text description of record fields
         * @param strPad    white space padding for left-hand side line headings (or <code>null</code>.
         */ 
        protected void  printOut(PrintStream ps, String strPad) {

            if (strPad == null)
                strPad = "";
            String  strPadd = strPad + "  ";
            
            int     szMaxLen = this.computeMaxParamValDisplay();
            String  strFmt = "%s%s for %s = %-" + szMaxLen + "s: %s = %s";
            String  strParmNm = this.recParm.enmParam.name();
            String  strFldNm = this.recParm.strFldDesc;
            
            ps.println(strPad + "Parameter " + strParmNm + " variation to " + strFldNm + " response");
            ps.println(String.format(strFmt, strPadd, "minimum average value", strParmNm, this.objValAvgMin.toString(), strFldNm, this.durAvgMin.toString()));
            ps.println(String.format(strFmt, strPadd, "maximum average value", strParmNm, this.objValAvgMax.toString(), strFldNm, this.durAvgMax.toString()));
            ps.println(String.format(strFmt, strPadd, "minimum standard dev.", strParmNm, this.objValStdMin.toString(), strFldNm, this.durStdMin.toString()));
            ps.println(String.format(strFmt, strPadd, "maximum standard dev.", strParmNm, this.objValStdMax.toString(), strFldNm, this.durStdMax.toString()));
        }
        
        //
        // Support Methods
        //
        
        /**
         * <p>
         * Computes the maximum length (in characters) of all parameter values displayed as strings.
         * </p>
         * 
         * @return  maximum value of <code>{@link String#length()}</code> for all parameter (object) values
         */
        private int computeMaxParamValDisplay() {
            Stream.Builder<Object>  bldr = Stream.builder();

            bldr.add(this.objValAvgMin).add(this.objValAvgMax).add(this.objValStdMin).add(this.objValStdMax);
            
            int     szMaxLen = bldr.build().<String>map(Object::toString).mapToInt(String::length).max().orElse(STR_PAD_NM_PARAM);
            
            return szMaxLen;
        }
    }
    
}
