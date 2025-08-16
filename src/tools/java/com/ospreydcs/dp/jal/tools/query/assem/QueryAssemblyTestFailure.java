/*
 * Project: dp-api-common
 * File:	QueryAssemblyTestFailure.java
 * Package: com.ospreydcs.dp.jal.tools.query.assem
 * Type: 	QueryAssemblyTestFailure
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
 * @since Aug 16, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.assem;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Contains the status of a <code>QueryAssemblyTestCase</code> failure.
 * </p>
 * <p>
 *  If a sampled aggregate assembly failure occurs during an evaluation with 
 *  <code>{@link QueryAssemblyTestCase#evaluate(com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer)}</code>
 *  the failure status is record in the result record field <code>{@link QueryAssemblyTestResult#recTestStatus()}</code>.
 *  This record is intended for use in extracting the failure status and all the available information.
 *  </p>
 *  <p>
 *  </p>
 *  
 *
 * @author Christopher K. Allen
 * @since Aug 16, 2025
 *
 */
public record QueryAssemblyTestFailure(
        QueryAssemblyTestCase   recTestCase,
        int                     indTestCase,
        ResultStatus            recTestStatus,
        String                  strFailMsg,
        Throwable               errFailCause,
        String                  strCauseMsg,
        Throwable               errCauseCause,
        String                  strCauseCauseMsg
        ) 
{
    
    //
    // Creators
    // 
    
    /**
     * <p>
     * Creates and returns a new <code>QueryAssemblyTestFailure</code> record with field values extracted from the argument.
     * </p>
     * <p>
     * This method extracts fields <code>{@link QueryAssemblyTestResult#recTestCase()}</code> and 
     * <code>{@link QueryAssemblyTestResult#recTestStatus()}</code> from the argument then defers to
     * creator <code>{@link #from(QueryAssemblyTestCase, ResultStatus)}</code>.
     * <p>
     * 
     * @param recResult     test result that failed
     * @return  a new <code>QueryAssemblyTestFailure</code> record populated with values extracted from the argument
     * @throws IllegalArgumentException
     * 
     * @see {@link #from(QueryAssemblyTestCase, ResultStatus)}
     */
    public static QueryAssemblyTestFailure  from(QueryAssemblyTestResult recResult) throws IllegalArgumentException {
        
        // Extract test case and status
        QueryAssemblyTestCase   recTestCase = recResult.recTestCase();
        ResultStatus            recTestStatus = recResult.recTestStatus();
        
        return QueryAssemblyTestFailure.from(recTestCase, recTestStatus);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>QueryAssemblyTestFailure</code> record populated from the given arguments.
     * </p>
     * <p>
     * The <code>QueryAssemblyTestFailure</code> field values are extracted from the given arguments.
     * Note that the argument <code>recTestStatus</code> must indicate a failure condition (i.e., 
     * <code>{@link ResultStatus#isFailure()}</code> returns <code>true</code>) otherwise an exception is
     * thrown.
     * </p>
     * 
     * @param recTestCase       the test case that failed
     * @param recTestStatus     the test status for the given test case argument
     * 
     * @return  a new <code>QueryAssemblyTestFailure</code> record populated with value extracted from the arguments
     * 
     * @throws  IllegalArgumentException    the test status record indicated success
     */
    public static QueryAssemblyTestFailure  from(
            QueryAssemblyTestCase   recTestCase,
            ResultStatus            recTestStatus
            ) throws IllegalArgumentException
    {
        
        // Check Argument
        if (recTestStatus.isSuccess())
            throw new IllegalArgumentException(
                    JavaRuntime.getQualifiedMethodNameSimple()
                    + " - The status record indicates a successful test evaluation."
                    );
        
        // Extract the record field values
        int         indCase = recTestCase.indCase();
        String      strFailMsg = recTestStatus.message();
        Throwable   errFailCause = recTestStatus.cause();
        String      strCauseMsg = errFailCause.getMessage();
        Throwable   errCauseCause = errFailCause.getCause();
        
        String      strCauseCauseMsg; 
        if (errCauseCause != null)
            strCauseCauseMsg = errCauseCause.getMessage();
        else
            strCauseCauseMsg = null;
        
        // Create a new failure record and return it
        return new QueryAssemblyTestFailure(
                recTestCase, indCase, 
                recTestStatus, 
                strFailMsg, errFailCause, strCauseMsg, 
                errCauseCause, strCauseCauseMsg
                );
    }
    
    
    //
    // Tools
    //
    
    /**
     * <p>
     * Creates and returns a new comparator object that orders records by ascending index field values.
     * </p>
     * <p>
     * The returned comparator compares fields <code>{@link #indTestCase}</code> and orders records according
     * to the least value first ordering.
     * </p>
     *  
     * @return  a new comparator object ordering failure records by ascending test case indexes
     */
    public static Comparator<QueryAssemblyTestFailure>  ascendingIndexOrdering() {
        
        Comparator<QueryAssemblyTestFailure>    cmp = (r1, r2) -> {
            
            if (r1.indTestCase > r2.indTestCase)
                return 1;
            else if (r1.indTestCase == r2.indTestCase)
                return 0;
            else
                return -1;
        };
        
        return cmp;
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Extracts, creates, and returns the collection of failed test records from the given collection of test results.
     * </p>
     * <p>
     * The method inspects the field <code>{@link QueryAssemblyTestResult#recTestStatus()}</code> for test
     * failure.  Any argument record containing a failed test status is used to create a new
     * <code>QueryAssemblyTestFailure</code> record which is then included in the returned collection.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned record collection is immutable.
     * </p>
     * 
     * @param setResults    collection of test results for test failure extraction
     * 
     * @return  a collection of test failures for each failure encountered in the argument
     */
    public static Collection<QueryAssemblyTestFailure>  extractFailures(Collection<QueryAssemblyTestResult> setResults) {
        
        // Extract the failed test evaluation from the given result collection
        List<QueryAssemblyTestFailure>  lstRecFails = setResults.stream()
                .filter(rec -> rec.recTestStatus().isFailure())
                .<QueryAssemblyTestFailure>map(QueryAssemblyTestFailure::from)
                .toList();
        
        return lstRecFails;
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
        String  strPadd = strPad + "  ";
//        String  strPaddBul = strPadd + " - ";
        
        ps.println(strPad + this.getClass().getSimpleName() + " Test Case #" + this.indTestCase());
        ps.println(strPadd + "Failure message             : " + this.strFailMsg);
        ps.println(strPadd + "Failure cause type          : " + this.errFailCause.getClass().getSimpleName());
        ps.println(strPadd + "Failure cause message       : " + this.strCauseMsg);
        ps.println(strPadd + "Failure cause cause type    : " + this.errCauseCause);
        ps.println(strPadd + "Failure casue cause message : " + this.strCauseCauseMsg);
        
        ps.println(strPadd + "Failed Test Case");
        this.recTestCase.printOut(ps, strPadd);
    }
        
}
