/*
 * Project: dp-api-common
 * File:	QueryChannelTestResult.java
 * Package: com.ospreydcs.dp.jal.tools.query.channel
 * Type: 	QueryChannelTestResult
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
 * @since May 9, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.channel;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Comparator;

import com.ospreydcs.dp.api.common.ResultStatus;
import com.ospreydcs.dp.api.query.model.grpc.QueryChannel;
import com.ospreydcs.dp.api.query.model.grpc.QueryMessageBuffer;
import com.ospreydcs.dp.api.util.JavaRuntime;


/**
 * <p>
 * Record containing the results of a <code>QueryChannel</code> performance evaluation from <code>QueryChannelTestCase</code>.
 * </p>
 * <p>
 * The record contains the results of a <code>QueryChannel</code> evaluation as described by the field
 * <code>{@link #recTestCase}</code>.  The performance evaluation is typically performed by method
 * <code>{@link QueryChannelTestCase#evaluate(QueryChannel, QueryMessageBuffer)}</code>. 
 * </p>
 * 
 * @param strRqstId     the ID of the original data request
 * @param recTestStatus the success/fail status of the test evaluation
 * 
 * @param dblDataRate   the data rate seen during the request recovery (in MBps)
 * @param cntMessages   the number of <code>QueryData</code> messages recovered
 * @param szRecovery    the memory allocation size of the recovered data (in MBytes) 
 * @param durRecovery   the time duration required for the data recovery
 * 
 * @param recTestCase   the test case creating the above results parameters
 *
 * @author Christopher K. Allen
 * @since May 9, 2025
 *
 * @see QueryChannelTestCase#evaluate(com.ospreydcs.dp.api.query.model.grpc.QueryChannel, com.ospreydcs.dp.api.query.model.grpc.QueryMessageBuffer)
 */
public record QueryChannelTestResult(
        String          strRqstId,
        ResultStatus    recTestStatus,
        
        double          dblDataRate, 
        int             cntMessages, 
        long            szRecovery, 
        Duration        durRecovery,
        
        QueryChannelTestCase recTestCase 
        ) implements Comparable<QueryChannelTestResult>
{
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Returns a new <code>QueryChannelTestResult</code> record populated with the given arguments.
     * </p>
     * <p>
     * The returned record is assumed to be created by the 
     * <code>{@link QueryChannelTestCase#evaluate(QueryChannel, QueryMessageBuffer)</code> method.
     * </p>
     * 
     * @param strRqstId     the ID of the original data request
     * @param recTestStatus the success/fail status of the test evaluation
     * 
     * @param dblDataRate   the data rate seen during the request recovery (in MBps)
     * @param cntMessages   the number of <code>QueryData</code> messages recovered
     * @param szRecovery    the memory allocation size of the recovered data  
     * @param durRecovery   the time duration required for the data recovery
     * 
     * @param recTestCase   the test case creating the above results parameters
     * 
     * @return  a new <code>QueryChannelTestResult</code> record with fields given by the above arguments
     */
    public static QueryChannelTestResult from(
            String  strRqstId,
            ResultStatus    recTestStatus,
            
            double dblDataRate, 
            int cntMessages, 
            long szRecovery, 
            Duration durRecovery,
            
            QueryChannelTestCase recTestCase
            ) 
    {
        return new QueryChannelTestResult(strRqstId, recTestStatus, dblDataRate, cntMessages, szRecovery, durRecovery, recTestCase);
    }
    
    /**
     * <p>
     * Creates a new instance of <code>QueryChannelTestResult</code> for the case of a test evaluation failure.
     * </p>
     * <p>
     * This creator is intended for use whenever a 
     * <code>{@link QueryChannelTestCase#evaluate(QueryChannel, QueryMessageBuffer)}</code> 
     * operation fails; that is, an exception is thrown internally.  The cause of the failure (and a message) should
     * be included in the <code>recTestStatus</code> argument.
     * </p>
     * 
     * @param strRqstId     identifier of the original time-series data request
     * @param recTestStatus the cause of the failure
     * @param recTestCase   the test case that failed
     * 
     * @return  a new <code>QueryAssemblyTestResult</code> instance containing test evaluation failure information
     * 
     * @throws IllegalArgumentException     the status argument indicates <code>{@link ResultStatus#SUCCESS}</code>
     */
    public static QueryChannelTestResult    from(String strRqstId, ResultStatus recTestStatus, QueryChannelTestCase recTestCase) throws IllegalArgumentException {
        // Check status argument
        if (recTestStatus.isSuccess()) 
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - The status argument indicates sucess.");
        
        // Create and return an empty record
        return new QueryChannelTestResult(
                strRqstId, recTestStatus,
                0.0, 0, 0L, Duration.ZERO,
                recTestCase
                );
    }
    
    
    //
    // Tools
    //
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> provide a descending (reverse) ordering according to data rates.
     * </p>
     * <p>
     * The returned comparator instance compares the <code>{@link #dblDataRate}</code> fields of two 
     * <code>QueryChannelTestResult</code> records.  It provides a reverse ordering of records according
     * to the data rate fields.  Specifically, the highest data rate will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * Note that the comparator provided here is the complement of the natural order of 
     * <code>QueryChannelTestResult</code> records provided by the exposed <code>Comparable</code> interface.
     * </p>  
     * 
     * @return  a new <code>Comparator</code> instance providing a reverse ordering by record data rates
     */
    public static Comparator<QueryChannelTestResult>   descendingRateOrdering() {
    
        Comparator<QueryChannelTestResult>   cmp = (r1, r2) -> {

            if (r1.dblDataRate > r2.dblDataRate)
                return -1;
            else
                return +1;
        };
        
        return cmp;
    }
    
    /**
     * <p>
     * Creates and returns a new <code>Comparator</code> provide an ascending (natural) ordering according to data rates.
     * </p>
     * <p>
     * The returned comparator instance compares the <code>{@link #dblDataRate}</code> fields of two 
     * <code>QueryChannelTestResult</code> records.  It provides a natural ordering of records according
     * to the data rate fields.  Specifically, the lowest data rate will appear first in any ordered
     * Java collection.
     * </p>
     * <p>
     * Note that the comparator provided here is the equivalent of the natural order of 
     * <code>QueryChannelTestResult</code> records provided by the exposed <code>Comparable</code> interface.
     * </p>  
     * 
     * @return  a new <code>Comparator</code> instance providing a natural ordering by record data rates
     */
    public static Comparator<QueryChannelTestResult>    ascendingRateOrdering() {

        Comparator<QueryChannelTestResult>  cmp = (r1, r2) -> {

            if (r1.dblDataRate < r2.dblDataRate)
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
        
        ps.println(strPad + "Test Case #" + this.recTestCase.indCase());
        ps.println(strPad + "  Data rate (MBps)        : " + this.dblDataRate);
        ps.println(strPad + "  Recovered message count : " + this.cntMessages);
        ps.println(strPad + "  Recovered data size     : " + this.szRecovery);
        ps.println(strPad + "  Recovery duration       : " + this.durRecovery);
        ps.println(strPad + "  Test Case Parameters:");
        this.recTestCase.printOut(ps, strPad + "  ");
    }


    //
    // Comparable<QueryChannelTestResult> Interface
    //
    
    /**
     * <p>
     * Provides a forward order of <code>QueryChannelTestResult</code> records by data rate.
     * </p>
     * <p>
     * The <code>{@link #dblDataRate}</code> field of the argument is compared against that of
     * this record.  If the data rate of this field is less than that of the argument field
     * a value -1 is returned. Otherwise a value +1 is returned.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The value 0 is never returned to avoid clobbering of records within an ordered Java
     * collection.
     * </p>
     * 
     * @param o     record under comparison
     * 
     * @return  -1 if the data rate of this record is less than that of the argument,
     *          +1 otherwise
     *          
     * @see Comparable#compareTo(Object)         
     */
    @Override
    public int compareTo(QueryChannelTestResult o) {
        
        if (this.dblDataRate < o.dblDataRate)
            return -1;
        else
            return +1;
    }

}
