/*
 * Project: dp-jal
 * File:	ListTestRequests.java
 * Package: com.ospreydcs.dp.jal.tools.apps.query.listrqst
 * Type: 	ListTestRequests
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
 * @since Mar 3, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.query.listrqst;

import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;

import com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser;
import com.ospreydcs.dp.jal.tools.common.requests.TestArchiveRequest;

/**
 * <p>
 * Record containing the processed command-line arguments for application <code>ListTestArchiveRequests</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 3, 2026
 *
 * @param bolAll    list all Test Archive requests enumerated in <code>{@link TestArchiveRequest}</code>
 * @param lstRegex  list of regular expressions for matching constants in <code>{@link TestArchiveRequest}</code>
 * @param lstRqsts  list of constants in <code>{@link TestArchiveRequest}</code> for property display
 */
public record ListTestRequests(
        boolean                     bolAll,
        List<String>                lstRegex,
        List<TestArchiveRequest>    lstRqsts
        ) 
{
    
    // 
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>ListTestRequests</code> instance populated with the given arguments.
     * </p>
     * <p>
     * This creator is equivalent to the canonical constructor <code>{@link #ListTestRequests(boolean, List, List)}</code>.
     * </p>
     * 
     * @param bolAll    list all Test Archive requests enumerated in <code>{@link TestArchiveRequest}</code>
     * @param lstRegex  list of regular expressions for matching constants in <code>{@link TestArchiveRequest}</code>
     * @param lstRqsts  list of constants in <code>{@link TestArchiveRequest}</code> for property display
     * 
     * @return  a new <code>ListTestRequests</code> instance with field values given by the arguments
     */
    public static ListTestRequests  from(boolean bolAll, List<String> lstRegex, List<TestArchiveRequest> lstRqsts) {
        return new ListTestRequests(bolAll, lstRegex, lstRqsts);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ListTestRequests</code> instance by parsing the given application command-line arguments.
     * </p>
     * <p>
     * The argument is assumed to be the command-line arguments for the <code>ListTestArchiveRequests</code> application.
     * Fields of the <code>ListTestRequest</code> record are parsed for switches and variable names defined in the
     * <code>{@link ListTestRequestOptions}</code> enumeration.  Field values are populated according to the values
     * in the argument collection.
     * </p>
     * 
     * @param args  application command-line arguments
     * 
     * @return  a new <code>ListTestRequest</code> instance populated from the command-line arguments
     * 
     * @throws NoSuchElementException   a <code>{@link ListTestRequestOptions}</code> constant was invalid
     */
    public static ListTestRequests  parse(String...args) throws NoSuchElementException {
        
        // Check for "list all" switch
        boolean         bolAll;
        if (PARSER.hasSwitch(ListTestRequestOptions.ALL.getParameterDelimOption(), args))
            bolAll = true;
        else
            bolAll = false;
        
        // Extract regular expressions
        List<String>    lstRegex = PARSER.parseVariable(ListTestRequestOptions.REGEX.getParameterDelimOption(), args);
        
        // Extract TestArchiveRequest constant names
        List<String>    lstRqstNms = PARSER.parseVariable(ListTestRequestOptions.REQUEST.getParameterDelimOption(), args);

        // Converts TestArchiveRequest names to constants
        List<TestArchiveRequest>    lstRqsts = lstRqstNms.stream()
                .<TestArchiveRequest>map(strNm -> TestArchiveRequest.valueFrom(strNm))  // throws NoSuchElementException
                .toList();
        
        return ListTestRequests.from(bolAll, lstRegex, lstRqsts);
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
        String strPadd = strPad + "  ";
        
        ps.println(strPad + this.getClass().getSimpleName());
        ps.println(strPadd + "List all requests   : " + this.bolAll);
        ps.println(strPadd + "Regular expressions : " + this.lstRegex);
        ps.println(strPadd + "Request constants   : " + this.lstRqsts);
    }
    
    
    //
    // Record Overrides
    //

    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ListTestRequests rec) {
            boolean bolResult = (this.bolAll == rec.bolAll)
                             && (this.lstRegex.equals(rec.lstRegex))
                             && (this.lstRqsts.equals(rec.lstRqsts));
            
            return bolResult;
        }
        return false;
    }

    /**
     * @see java.lang.Record#toString()
     */
    @Override
    public String toString() {
        StringBuilder   buf = new StringBuilder();
        
        buf.append("List all requests   : " + this.bolAll + "\n");
        buf.append("Regular expressions : " + this.lstRegex + "\n");
        buf.append("Request constants   : " + this.lstRqsts + "\n");
        
        return buf.toString();
    }
    
    //
    // Record Resources
    //
    
    /** Application command-line parser */
    private static final AppArgumentsParser     PARSER = AppArgumentsParser.from();

}
