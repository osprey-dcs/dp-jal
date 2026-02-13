/*
 * Project: dp-jal
 * File:	TestResultStatsBase.java
 * Package: com.ospreydcs.dp.jal.tools.common.score
 * Type: 	TestResultStatsBase
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
 * @since Feb 9, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.common.score;

import java.io.PrintStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.function.Function;

import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Base class for performing common operations in the statistical analysis of test results collections.
 * </p>
 * <p>
 * The class instance is completely configured after construction.
 * All results are analyzed within the class constructor.
 * After construction the statistical results can be printed out with method
 * <code>{@link #printOut(PrintStream, String)}</code>.
 * </p>
 * <p>
 * It is the responsibility of sub-classes to defined the <code>TestResult</code> record fields to be
 * analyzed.  This is action is realized by implementing the abstract methods
 * <ul>
 * <li><code>{@link #assignFailedResult()}</code> - flags a <code>TestResult</code> record for failed test evaluation.</li>
 * <li><code>{@link #assignNumericFields()}</code> - identifies all <code>TestResult</code> numeric fields for statistical analysis.</li>
 * <li><code>{@link #assignDurationFields()}</code> - identifies all <code>TestResult Duration</code> fields for statistical analysis.</li>
 * </ul>
 * </p>
 * <p>
 * There are enclosed enumerations and record types used to identify <code>TestResult</code> fields, 
 * which are required by the above methods. We have the following:
 * <ul>
 * <li><code>{@link NumberType}</code> - Enumeration of supported numeric field types.</li>
 * <li><code>{@link NumberField}</code> - Record identifying a <code>TestResult</code> numeric field.</li>
 * <li><code>{@link DurationField}</code> - Record identifying a <code>TestResult Duration</code> field.</li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 9, 2026
 *
 * @param <TestResult>  record of test results
 */
public abstract class TestResultStatsBase<TestResult extends Record> {

    //
    // Internal Types - Child Class
    //
    
    /**
     * <p>
     * Enumeration of allowable numeric types for the <code>{@link NumberField}</code> record.
     * </p>
     */
    protected enum NumberType {
        INTEGER,
        LONG,
        DOUBLE,
        ;
    }

    /**
     * <p>
     * Record containing the specifications of the <code>TestResult</code> record field with numeric type to be analyzed.
     * </p>
     * <p>
     * This record is applicable to <code>TestResult</code> record fields of type <code>int, long, double</code>
     * as enumerated in <code>{@link NumberType}</code>.
     * </p> 
     *
     * @param <TestResult>  record of test results
     * 
     * @param strDesc   text description of the field (may include units)
     * @param enmType   the numeric type of the record field
     * @param numTgt    target value of field in converted units - optional, not used if <code>null</code>
     * @param fncFld    lambda function extracting field value from <code>TestResult</code> record (may include unit conversion)
     */
    protected static record NumberField<TestResult extends Record>(
            String                          strDesc,
            NumberType                      enmType,
            Number                          numTgt,
            Function<TestResult, Number>    fncFld
            )
    {

        /**
         * <p>
         * Creates and returns a new <code>NumberField</code> record specification populated with the given argument values.
         * </p>
         * <p>
         * Instances of <code>NumberField</code> are used to identify numeric fields of the <code>TestResult</code> for
         * statistical analysis.  They should be created in the constructor of child classes of <code>TestResultStatsBase</code>
         * and supplied to the base class using method <code>{@link TestResultStatsBase#assignNumericFields()}</code>.
         * </p>
         * 
         * @param <TestResult>  record of test results
         * 
         * @param strDesc   text description of the field (may include units)
         * @param enmType   the numeric type of the record field
         * @param numTgt    target value of field in converted units - optional, not used if <code>null</code>
         * @param fncFld    lambda function extracting field value from <code>TestResult</code> record (may include unit conversion)
         * 
         * @return  a new <code>NumberField</code> record for use by <code>{@link TestResultStatsBase#assignNumericFields()}</code>.
         */
        public static <TestResult extends Record> NumberField<TestResult> from(
                String                      strDesc, 
                NumberType                  enmType,
                Number                      numTgt, 
                Function<TestResult, Number> fncFld 
                ) 
        {
            return new NumberField<TestResult>(strDesc, enmType, numTgt, fncFld);
        }
    }

    /**
     * <p>
     * Record containing the specifications of the <code>TestResult</code> record field with duration type to be analyzed.
     * </p>
     * <p>
     * This record is applicable to <code>TestResult</code> record fields of type <code>Duration</code>.
     * </p> 
     *
     * @param <TestResult>  record of test results
     * 
     * @param strDesc   text description of the field (may include units)
     * @param durTgt    target value of field in converted units - optional, not used if <code>null</code>
     * @param fncFld    lambda function extracting field value from <code>TestResult</code> record (may include unit conversion)
     */
    protected static record DurationField<TestResult extends Record>(
            String                          strDecr,
            Duration                        durTgt,
            Function<TestResult, Duration>  fncFld
            )
    {

        /**
         * <p>
         * Creates and returns a new <code>NumberField</code> record specification populated with the given argument values.
         * </p>
         * <p>
         * Instances of <code>DurationField</code> are used to identify <code>Duration</code> fields of the <code>TestResult</code> 
         * record for statistical analysis.  They should be created in the constructor of child classes of <code>TestResultStatsBase</code>
         * and supplied to the base class using method <code>{@link TestResultStatsBase#assignDurationFields()}</code>.
         * </p>
         * 
         * @param <TestResult>  record of test results
         * 
         * @param strDesc   text description of the field (may include units)
         * @param durTgt    target value of field in converted units - optional, not used if <code>null</code>
         * @param fncFld    lambda function extracting field value from <code>TestResult</code> record (may include unit conversion)
         * 
         * @return  a new <code>DurationField</code> record for use by <code>{@link TestResultStatsBase#assignDurationFields()}</code>.
         */
        public static <TestResult extends Record> DurationField<TestResult> from(
                String                          strDesc, 
                Duration                        durTgt,
                Function<TestResult, Duration>  fncFld 
                ) 
        {
            return new DurationField<TestResult>(strDesc, durTgt, fncFld);
        }
    }

    
    //
    // Abstract Methods
    //
    
//  /**
//  * <p>
//  * Assigns the (lambda) function for extracting test case indexes from test result records.
//  * </p>
//  * <p>
//  * This method is called within the base class constructor.
//  * This is a non-vital function but allows the index of the test 
//  * case to be displayed in the output of the analysis.
//  * </p>
//  * 
//  * @return  a lambda function for extracting the test case index from a <code>TestResult</code> record
//  */
// protected abstract Function<TestResult, Integer>    assignIndexFunction();
    
    /**
     * <p>
     * Sets the lambda function identifying failed test results.
     * </p>
     * <p>
     * Called from base-class constructor to configure the instance for determination of
     * any failed <code>TestResult</code> instances.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Sub-class implementation must be essentially static and well-defined at construction.</li>
     * </p>
     * 
     * @return  lambda function that identifies result failure, i.e., rec -> rec.recStatus.isFailure() 
     */
    protected abstract  Function<TestResult, Boolean>   assignFailedResult();
    
    /**
     * <p>
     * Defines the list of all numeric fields within <code>TestResult</code> to be analyzed.
     * </p>
     * <p>
     * Called from base-class constructor to configure the instance for statistical analysis
     * of the given fields.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Sub-class implementation must be essentially static and well-defined at construction.</li>
     * <li>Computed output will appear in the order of the returned list.</li>
     * </p>
     *   
     * @return  ordered list of numeric fields to be analyzed
     */
    protected abstract List<NumberField<TestResult>>    assignNumericFields();
    
    /**
     * <p>
     * Defines the list of all <code>Duration</code> fields within <code>TestResult</code> to be analyzed.
     * </p>
     * <p>
     * Called from base-class constructor to configure the instance for statistical analysis
     * of the given fields.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Sub-class implementation must be essentially static and well-defined at construction.</li>
     * <li>Computed output will appear in the order of the returned list.</li>
     * </p>
     * 
     * @return  ordered list of <code>Duration</code> fields to be analyzed
     */
    protected abstract List<DurationField<TestResult>>  assignDurationFields();

    
    //
    // Defining Attributes
    //
    
    /** The collection of <code>TestResult</code> records to analyze */
    private final Collection<TestResult>            conResults;
    

    // 
    // Instances Attributes
    //

//  /** Lambda function for extracting the test case index from a <code>TestResult</code> record. */
//  private final Function<TestResult, Integer>                 fncIndex;
    
    /** lambda function that identifies result failure (i.e., rec -> rec.recStatus.isFailure() ) */
    private final Function<TestResult, Boolean>     fncFail;

    /** List of specifications for numeric fields */
    private final List<NumberField<TestResult>>     lstNumFlds;

    /** List of specification for <code>Duration</code> valued fields */
    private final List<DurationField<TestResult>>   lstDurFlds;


    //
    // Instance Resources
    //
    
    /** The collection of numeric-valued statistical results */
    private final List<NumberStats<TestResult>>      lstNumStats;
    
    /** The collection of duration-valued statistical results */
    private final List<DurationStats<TestResult>>   lstDurStats;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>TestResultStatsBase</code> instance.
     * </p>
     * <p>
     * The class instance is completely configured after construction.
     * All results are analyzed within this constructor.
     * </p>
     * <p>
     * It is the responsibility of sub-classes to defined the <code>TestResult</code> record fields to be
     * analyzed.  This is action is realized by implementing the abstract methods
     * <ul>
     * <li><code>{@link #assignFailedResult()}</code> - flags a <code>TestResult</code> record for failed test evaluation.</li>
     * <li><code>{@link #assignNumericFields()}</code> - identifies all <code>TestResult</code> numeric fields for statistical analysis.</li>
     * <li><code>{@link #assignDurationFields()}</code> - identifies all <code>TestResult Duration</code> fields for statistical analysis.</li>
     * </ul>
     * </p>
     *  
     * @param   conResults  the collection of all test results to be analyzed
     * 
     * @throws IllegalArgumentException the argument collection was empty
     * @throws MissingResourceException no <code>TestResult</code> fields were identified for analysis
     * @throws NoSuchElementException   no successful results were contained in the argument collection
     */
    protected TestResultStatsBase(Collection<TestResult> conResults) throws IllegalArgumentException, MissingResourceException, NoSuchElementException {
        
        // Check argument
        if (conResults.isEmpty())
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Test result record collection was empty.");

        // Acquire target fields and failure checks
        this.fncFail = this.assignFailedResult();
        this.lstNumFlds = this.assignNumericFields();
        this.lstDurFlds = this.assignDurationFields();
        
        if (this.lstNumFlds.isEmpty() && this.lstNumFlds.isEmpty())
            throw new MissingResourceException("No record fields were identified for analysis.", this.getClass().getName(), JavaRuntime.getQualifiedMethodNameSimple());
     
        // Set test results collection and compute the statistics
        this.conResults = conResults;

        this.lstNumStats = this.computeNumberStatistics(conResults);    // throws NoSuchElementException
        this.lstDurStats = this.computeDurationStatistics(conResults);  // throws NoSuchElementException
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
        
        int     cntTotal = this.conResults.size();
        int     cntFail = this.conResults.stream().filter(rec -> this.fncFail.apply(rec)).mapToInt(rec -> 1).sum();
        
        // Print out results  
        ps.println(strPad + "Number of result cases - TOTAL  : " + cntTotal);
        ps.println(strPad + "Number of result cases - FAILED : " + cntFail);
        
        for (NumberStats<TestResult> recStats : this.lstNumStats) {
            recStats.printOut(ps, strPad);
        }
        for (DurationStats<TestResult> recStats : this.lstDurStats) {
            recStats.printOut(ps, strPad);
        }
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Computes the statistics for all numeric <code>TestResult</code> fields identified by <code>{@link #assignNumericFields()}</code>.
     * </p> 
     * 
     * @param conResults    collection of all test results
     * 
     * @return  ordered list of statistical results for assigned numeric fields  
     * 
     * @throws NoSuchElementException   there were no successful test results in the argument collection
     */
    private List<NumberStats<TestResult>>   computeNumberStatistics(Collection<TestResult> conResults) throws NoSuchElementException {
        
        // Check configuration 
        if (this.lstNumFlds.isEmpty())
            return List.of();
        
        // Extract the successful results and check
        List<TestResult>    lstGood = conResults.stream().filter(rec -> !this.fncFail.apply(rec)).toList();
        if (lstGood.isEmpty())
            throw new NoSuchElementException(JavaRuntime.getQualifiedMethodNameSimple() + " - Test result record collection contained all FAILED results.");
        
        // Compute the statistics for the numeric fields
        List<NumberStats<TestResult>>   lstStats = new ArrayList<>(this.lstNumFlds.size());
        
        for (NumberField<TestResult> recFld : this.lstNumFlds) {
            
            NumberStats<TestResult> recStats = switch (recFld.enmType) {
            case INTEGER -> NumberStats.forInteger(recFld, lstGood);    // throws NoSuchElementException
            case LONG -> NumberStats.forLong(recFld, lstGood);          // throws NoSuchElementException
            case DOUBLE -> NumberStats.forDouble(recFld, lstGood);      // throws NoSuchElementException
            };
            
            lstStats.add(recStats);
        }
        
        return lstStats;
    }
    
    /**
     * <p>
     * Computes the statistics for all <code>Duration TestResult</code> fields identified by <code>{@link #assignDurationFields()}</code>.
     * </p> 
     * 
     * @param conResults    collection of all test results
     * 
     * @return  ordered list of statistical results for assigned duration fields  
     * 
     * @throws NoSuchElementException   there were no successful test results in the argument collection
     */
    private List<DurationStats<TestResult>> computeDurationStatistics(Collection<TestResult> conResults) throws NoSuchElementException {
        
        // Extract the successful results and check
        List<TestResult>    lstGood = conResults.stream().filter(rec -> !this.fncFail.apply(rec)).toList();
        if (lstGood.isEmpty())
            throw new NoSuchElementException(JavaRuntime.getQualifiedMethodNameSimple() + " - Test result record collection contained all FAILED results.");
        
        // Compute the statistics for the numeric fields
        List<DurationStats<TestResult>>   lstStats = this.lstDurFlds.stream().<DurationStats<TestResult>>map(rec -> DurationStats.from(rec, lstGood)).toList();
        
        return lstStats;
    }
    
    //
    // Internal Types - Private
    //
    
    /**
     * <p>
     * Record containing statistics for a numeric-valued field within the <code>TestResult</code> record.
     * </p>
     *
     * @param <TestResult>  record of test results
     * 
     * @param   strDesc     description of field used in printing output
     * @param   enmTpe      numeric type of the field
     * @param   dblTgt      (optional) target value - ignored if <code>null</code>
     * 
     * @param   cntGtAvg    number of field values greater than or equal to average
     * @param   cntGtTgt    number of field values greater than or equal to target
     * @param   numMin      the minimum value of the field
     * @param   numMax      the maximum value of the field
     * @param   dblAvg      the average value of the field
     * @param   dblStd      the standard deviation of the field
     */
    public static record NumberStats<TestResult extends Record> (
        String      strDesc,
        NumberType  enmType,
        Number      numTgt,
        
        int     cntGtAvg,
        int     cntGtTgt,
        
        Number  numMin,
        Number  numMax,
        double  dblAvg,
        double  dblStd
        )
    {
        
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates and returns a new <code>NumberStats</code> record for an integer-typed target field.
         * </p>
         * <p>
         * The statistical analysis of the target field is performed here and the values obtained used to populate
         * the returned record.
         * </p>
         * 
         * @param <TestResult>  record of test results
         * 
         * @param   recFld      descriptor for numeric field under analysis 
         * @param   conResults  the collection of test results to be analyzed
         * 
         * @return  a new <code>NumberStats</code> record containing the statistics of the given collection of test results
         * 
         * @throws NoSuchElementException   minimum, maximum, or average value of target field undetermined
         */
        public static <TestResult extends Record> NumberStats<TestResult> forInteger(NumberField<TestResult> recFld, Collection<TestResult> conResults) throws NoSuchElementException 
        {
            // Extract parameters from field descriptor
            String                          strDesc = recFld.strDesc();
            Number                          numTgt = recFld.numTgt();
            Function<TestResult, Number>    fncFld = recFld.fncFld();
                    
            int     cntTotal = conResults.size();

            // Compute the field value statistics
            double  dblAvg = conResults.stream().mapToInt(rec -> fncFld.apply(rec).intValue()).average().getAsDouble(); // throws NoSuchElementException
            int     intMin = conResults.stream().mapToInt(rec -> fncFld.apply(rec).intValue()).min().getAsInt();        // throws NoSuchElementException
            int     intMax = conResults.stream().mapToInt(rec -> fncFld.apply(rec).intValue()).max().getAsInt();        // throws NoSuchElementException
            double  dblSqrd = conResults.stream().mapToDouble(rec -> fncFld.apply(rec).intValue()).map(v -> (v - dblAvg)*(v - dblAvg)).sum();
            double  dblStd = Math.sqrt(dblSqrd/cntTotal);
            
            // Get case indexes
//            int     indMin = conResults.stream().filter(rec -> fncFld.apply(rec) == inMin).findFirst().
            
            // Compute the field value counts
            int     cntGtAvg = conResults.stream().mapToInt(rec -> fncFld.apply(rec).intValue()).filter(v -> v >= dblAvg).map(v -> 1).sum();
            int     cntGtTgt = (numTgt==null) ? 0 : conResults.stream().mapToInt(rec -> fncFld.apply(rec).intValue()).filter(v -> v >= numTgt.intValue()).map(v -> 1).sum();
            
            // Create populated record and return it
            NumberStats<TestResult> recStats = new NumberStats<TestResult>(strDesc, NumberType.INTEGER, numTgt, cntGtAvg, cntGtTgt, intMin, intMax, dblAvg, dblStd);
            
            return recStats;
        }
        
        /**
         * <p>
         * Creates and returns a new <code>NumberStats</code> record for a long-typed target field.
         * </p>
         * <p>
         * The statistical analysis of the target field is performed here and the values obtained used to populate
         * the returned record.
         * </p>
         * 
         * @param <TestResult>  record of test results
         * 
         * @param   recFld      descriptor for numeric field under analysis 
         * @param   conResults  the collection of test results to be analyzed
         * 
         * @return  a new <code>NumberStats</code> record containing the statistics of the given collection of test results
         * 
         * @throws NoSuchElementException   minimum, maximum, or average value of target field undetermined
         */
        public static <TestResult extends Record> NumberStats<TestResult> forLong(NumberField<TestResult> recFld, Collection<TestResult> conResults) throws NoSuchElementException 
        {
            // Extract parameters from field descriptor
            String                          strDesc = recFld.strDesc();
            Number                          numTgt = recFld.numTgt();
            Function<TestResult, Number>    fncFld = recFld.fncFld();
                    
            int     cntResults = conResults.size();

            // Compute the field value statistics
            double  dblAvg = conResults.stream().mapToLong(rec -> fncFld.apply(rec).longValue()).average().getAsDouble();   // throws NoSuchElementException
            long    lngMin = conResults.stream().mapToLong(rec -> fncFld.apply(rec).longValue()).min().getAsLong();         // throws NoSuchElementException
            long    lngMax = conResults.stream().mapToLong(rec -> fncFld.apply(rec).longValue()).max().getAsLong();         // throws NoSuchElementException
            double  dblSqrd = conResults.stream().mapToDouble(rec -> fncFld.apply(rec).longValue()).map(v -> (v - dblAvg)*(v - dblAvg)).sum();
            double  dblStd = Math.sqrt(dblSqrd/cntResults);
            
            // Compute the field value counts
            int     cntGtAvg = conResults.stream().mapToLong(rec -> fncFld.apply(rec).longValue()).filter(v -> v >= dblAvg).mapToInt(v -> 1).sum();
            int     cntGtTgt = (numTgt==null) ? 0 : conResults.stream().mapToLong(rec -> fncFld.apply(rec).longValue()).filter(v -> v >= numTgt.longValue()).mapToInt(v -> 1).sum();
            
            // Create populated record and return it
            NumberStats<TestResult> recStats = new NumberStats<TestResult>(strDesc, NumberType.LONG, numTgt, cntGtAvg, cntGtTgt, lngMin, lngMax, dblAvg, dblStd);
            
            return recStats;
        }
        
        /**
         * <p>
         * Creates and returns a new <code>NumberStats</code> record for a double-typed target field.
         * </p>
         * <p>
         * The statistical analysis of the target field is performed here and the values obtained used to populate
         * the returned record.
         * </p>
         * 
         * @param <TestResult>  record of test results
         * 
         * @param   strDesc     description of field used in printing output
         * @param   numTgt      target value (optional - ignored if <code>null</code>  
         * @param   fncField    lambda function that extracts the target field (i.e., rec -> rec.dblField() )
         * @param   conResults  the collection of test results to be analyzed
         * 
         * @return  a new <code>NumberStats</code> record containing the statistics of the given collection of test results
         * 
         * @throws NoSuchElementException   minimum, maximum, or average value of target field undetermined
         */
        public static <TestResult extends Record> NumberStats<TestResult> forDouble(NumberField<TestResult> recFld, Collection<TestResult> conResults) throws NoSuchElementException 
        {
            // Extract parameters from field descriptor
            String                          strDesc = recFld.strDesc();
            Number                          numTgt = recFld.numTgt();
            Function<TestResult, Number>    fncFld = recFld.fncFld();
                    
            int     cntResults = conResults.size();

            // Compute the field value statistics
            double  dblAvg = conResults.stream().mapToDouble(rec -> fncFld.apply(rec).doubleValue()).average().getAsDouble();   // throws NoSuchElementException
            double  dblMin = conResults.stream().mapToDouble(rec -> fncFld.apply(rec).doubleValue()).min().getAsDouble();       // throws NoSuchElementException
            double  dblMax = conResults.stream().mapToDouble(rec -> fncFld.apply(rec).doubleValue()).max().getAsDouble();       // throws NoSuchElementException
            double  dblSqrd = conResults.stream().mapToDouble(rec -> fncFld.apply(rec).doubleValue()).map(v -> (v - dblAvg)*(v - dblAvg)).sum();
            double  dblStd = Math.sqrt(dblSqrd/cntResults);
            
            // Compute the field value counts
            int     cntGtAvg = conResults.stream().mapToDouble(rec -> fncFld.apply(rec).doubleValue()).filter(v -> v >= dblAvg).mapToInt(v -> 1).sum();
            int     cntGtTgt = (numTgt==null) ? 0 : conResults.stream().mapToDouble(rec -> fncFld.apply(rec).doubleValue()).filter(v -> v >= numTgt.doubleValue()).mapToInt(v -> 1).sum();
            
            // Create populated record and return it
            NumberStats<TestResult> recStats = new NumberStats<TestResult>(strDesc, NumberType.DOUBLE, numTgt, cntGtAvg, cntGtTgt, dblMin, dblMax, dblAvg, dblStd);
            
            return recStats;
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
        public void printOut(PrintStream ps, String strPad) {
            if (strPad == null)
                strPad = "";
            String  strPadd = strPad + "  ";
            
            // Print out results
            ps.println(strPad + this.strDesc);
            ps.println(strPadd + "field data type                 : " + this.enmType);
            if (this.numTgt == null) {
                ps.println(strPadd + "cases w/ valuts >= target value : Undefined");
                ps.println(strPadd + "cases w/ valuts >= target value : Undefined");
            } else {
                ps.println(strPadd + "target value                    : " + this.numTgt);
                ps.println(strPadd + "cases w/ valuts >= target value : " + this.cntGtTgt);
            }
            ps.println(strPadd + "cases w/ values >= avg value    : " + this.cntGtAvg);
            ps.println(strPadd + "minimum value                   : " + this.numMin);
            ps.println(strPadd + "maximum value                   : " + this.numMax);
            ps.println(strPadd + "average value                   : " + this.dblAvg);
            ps.println(strPadd + "standard dev.                   : " + this.dblStd);
        }
    }
    
    /**
     * <p>
     * Record containing statistics for a <code>Duration</code> field within the <code>TestResult</code> record.
     * </p>
     *
     * @param <TestResult>  record of test results
     * 
     * @param   strDesc     description of field used in printing output
     * @param   durTgt      (optional) target value - ignored if <code>null</code>
     * 
     * @param   cntLtAvg    number of field values less than or equal to average
     * @param   cntLtTgt    number of field values less than or equal to target
     * @param   durMin      the minimum duration of the field
     * @param   durMax      the maximum duration of the field
     * @param   durAvg      the average duration of the field
     * @param   durStd      the standard deviation of the field
     */
    public static record DurationStats<TestResult extends Record> (
            String      strDesc,
            Duration    durTgt,
            
            int         cntLtAvg,
            int         cntLtTgt,
            
            Duration    durMin,
            Duration    durMax,
            Duration    durAvg,
            Duration    durStd
            )  
    {
        
        //
        // Creators
        //
        
        /**
         * <p>
         * Creates and returns a new <code>DurationStats</code> record for an integer-typed target field.
         * </p>
         * <p>
         * The statistical analysis of the target field is performed here and the values obtained used to populate
         * the returned record.
         * </p>
         * 
         * @param <TestResult>  record of test results
         * 
         * @param   strDesc     description of field used in printing output
         * @param   durTgt      target value (optional - ignored if <code>null</code>
         * @param   fncField    lambda function that extracts the target field (i.e., rec -> rec.dblField() )
         * @param   conResults  the collection of test results to be analyzed
         * 
         * @return  a new <code>DurationStats</code> record containing the statistics of the given collection of test results
         */
        public static <TestResult extends Record> DurationStats<TestResult> from(DurationField<TestResult> recFld, Collection<TestResult> conResults) 
        {
            // Extract parameters from field descriptor
            String                          strDesc = recFld.strDecr();
            Duration                        durTgt = recFld.durTgt();
            Function<TestResult, Duration>  fncFld = recFld.fncFld();
                    
            int     cntResults = conResults.size();
            
            // Compute the field duration statistics
            Duration    durAvg = conResults.stream().<Duration>map(rec -> fncFld.apply(rec)).reduce(Duration.ZERO, (d1,d2) -> d1.plus(d2)).dividedBy(cntResults);
            Duration    durMin = conResults.stream().<Duration>map(rec -> fncFld.apply(rec)).reduce(durAvg, (d1, d2) -> { if (d1.compareTo(d2) < 0) return d1; else return d2; } );
            Duration    durMax = conResults.stream().<Duration>map(rec -> fncFld.apply(rec)).reduce(durAvg, (d1, d2) -> { if (d1.compareTo(d2) > 0) return d1; else return d2; } );
            
            double      dblNsSqrd = conResults.stream().<Duration>map(rec -> fncFld.apply(rec)).mapToLong(dur -> dur.toNanos()).mapToDouble(l -> Long.valueOf(l).doubleValue()).map(ns -> ns*ns).sum()/cntResults;
            double      dblNsAvg = Long.valueOf( durAvg.toNanos() ).doubleValue();
            double      dblNsStd = Math.sqrt(dblNsSqrd - dblNsAvg*dblNsAvg);
            Duration    durStd = Duration.ofNanos( Double.valueOf(dblNsStd).longValue() );
            
            // Compute the field value counts
            int     cntLtAvg = conResults.stream().<Duration>map(rec -> fncFld.apply(rec)).filter(d -> d.compareTo(durAvg) <= 0).mapToInt(d -> 1).sum();
            int     cntLtTgt = (durTgt==null) ? 0 : conResults.stream().<Duration>map(rec -> fncFld.apply(rec)).filter(d -> d.compareTo(durTgt) <= 0).mapToInt(d -> 1).sum();
            
            // Create populated record and return it
            DurationStats<TestResult>   recResult = new DurationStats<TestResult>(strDesc, durTgt, cntLtAvg, cntLtTgt, durMin, durMax, durAvg, durStd);
            
            return recResult;
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
        public void printOut(PrintStream ps, String strPad) {
            if (strPad == null)
                strPad = "";
            String  strPadd = strPad + "  ";
            
            // Print out results
            ps.println(strPad + this.strDesc);
            if (this.durTgt == null) {
                ps.println(strPadd + "target value                    : Undefined");
                ps.println(strPadd + "cases w/ values <= target value : Undefined");
            } else {
                ps.println(strPadd + "target value                    : " + this.durTgt);
                ps.println(strPadd + "cases w/ values <= target value : " + this.cntLtTgt);
            }
            ps.println(strPadd + "cases w/ values <= avg value    : " + this.cntLtAvg);
            ps.println(strPadd + "minimum value                   : " + this.durMin);
            ps.println(strPadd + "maximum value                   : " + this.durMax);
            ps.println(strPadd + "average value                   : " + this.durAvg);
            ps.println(strPadd + "standard dev.                   : " + this.durStd);
        }
        
    }

}
