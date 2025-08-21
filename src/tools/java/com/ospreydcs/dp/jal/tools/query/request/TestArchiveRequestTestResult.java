/*
 * Project: dp-api-common
 * File:	TestArchiveRequestTestResult.java
 * Package: com.ospreydcs.dp.jal.tools.query.request
 * Type: 	TestArchiveRequestTestResult
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
 * @since Jul 8, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.request;

import java.io.PrintStream;
import java.util.Comparator;

/**
 * <p>
 * Record containing the results of a time-series data request recovery and sampled aggregate assembly operation.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 8, 2025
 *
 * @param strRqstIdOrg      the (optional) string identifier of the original request
 * @param recRecoveryResult the results of the time-series data request recovery and correlation operation
 * @param recSmpAggResult   the results of the sampled data block creation and aggregation
 * @param recTestCase       the test case for which these results apply
 */
public record TestArchiveRequestTestResult(
        String                      strRqstIdOrg,
        QueryRecoveryResult         recRecoveryResult,
        SampledAggregateResult      recSmpAggResult,
        TestArchiveRequestTestCase  recTestCase
        ) 
{
    
    //
    // Creators
    //

    /**
     * <p>
     * Creates and returns a new <code>TestArchiveRequestTestResult</code> record with fields given by the argument values.
     * </p>
     * 
     * @param strRqstIdOrg      the (optional) string identifier of the original request
     * @param recRecoveryResult the results of the time-series data request recovery and correlation operation
     * @param recSmpAggResult   the results of the sampled data block creation and aggregation
     * @param recTestCase       the test case for which these results apply
     * 
     * @return  a new <code>TestArchiveRequestTestResult</code> record populated with the given argument values
     */
    public static TestArchiveRequestTestResult from(
            String                      strRqstIdOrg,
            QueryRecoveryResult         recRecoveryResult,
            SampledAggregateResult      recSmpAggResult,
            TestArchiveRequestTestCase  recTestCase
            ) 
    {
        return new TestArchiveRequestTestResult(strRqstIdOrg, recRecoveryResult, recSmpAggResult, recTestCase);
    }


    //
    // Tools
    //
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> provide a descending (reverse) ordering according to 
     * test case index.
     * </p>
     * <p>
     * The returned comparator instance compares the <code>{@link #recTestCase}</code> fields of two 
     * <code>SuperDomTestResult</code> records.  It provides a reverse ordering of records according
     * to the test case index fields (i.e., the <code>{@link TestArchiveRequestTestCase#indCase()}</code> fields).  
     * Specifically, the highest case index will appear first in any ordered Java collection.
     * </p>
     * 
     * @return  a new <code>Comparator</code> instance providing a reverse ordering by raw data rates
     */
    public static Comparator<TestArchiveRequestTestResult>   descendingCaseIndexOrdering() {
    
        Comparator<TestArchiveRequestTestResult>   cmp = (r1, r2) -> {

            if (r1.recTestCase.indCase() > r2.recTestCase.indCase())
                return -1;
            else
                return +1;
        };
        
        return cmp;
    }
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> provide an ascending (natural) ordering according to 
     * test case index.
     * </p>
     * <p>
     * The returned comparator instance compares the <code>{@link #recTestCase}</code> fields of two 
     * <code>SuperDomTestResult</code> records.  It provides a natural ordering of records according
     * to the test case index fields (i.e., the <code>{@link TestArchiveRequestTestCase#indCase()}</code> fields).  
     * Specifically, the lowest case index will appear first in any ordered Java collection.
     * </p>
     * 
     * @return  a new <code>Comparator</code> instance providing a natural ordering by raw data rates
     */
    public static Comparator<TestArchiveRequestTestResult>    ascendingCaseIndexOrdering() {

        Comparator<TestArchiveRequestTestResult>  cmp = (r1, r2) -> {

            if (r1.recTestCase.indCase() < r2.recTestCase.indCase())
                return -1;
            else
                return +1;
        };
        
        return cmp;  
    }
    

    //
    // Operations
    //
    
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
        
        // Print out test result single parameters
        ps.println(strPad + "QueryResponseAssembler Results for Test #" + this.recTestCase.indCase());
        ps.println(strPad + "  time-series data request ID       : " + this.strRqstIdOrg);
        this.recRecoveryResult.printOut(ps, strPad + "  ");
        this.recSmpAggResult.printOut(ps, strPad + "  ");
    }
    
    
}
