/*
 * Project: dp-api-common
 * File:	AggrAssemblyTestConfig.java
 * Package: com.ospreydcs.dp.jal.tools.query.assem
 * Type: 	AggrAssemblyTestConfig
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
 * @since Aug 15, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.assem;

import java.io.PrintStream;

/**
 * <p>
 * Record containing the configuration parameters for a <code>QueryAssemblyTestCase</code>.
 * </p>
 * <p>
 * This record is intended for use in configuration scoring.  It contains the configuration parameters of the
 * <code>QueryResponseAssembler</code> used in test evaluation with the method 
 * <code>{@link QueryAssemblyTestCase#evaluate(com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer)}</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 15, 2025
 *
 * @param bolAggrErrChk     enable/disable advanced error checking/verification in sampled aggregate assembly
 * @param bolAggrTmDomColl  enable/disable time-domain collisions in sampled aggregate assembly
 * @param bolAggrConcEnbl   enable/disable concurrency in sampled aggregate assembly
 * @param szAggrConcPivot   pivot size in current sampled aggregate assembly
 * @param cntAggrConcMaxThrds maximum thread count in concurrent sampled aggregate assembly
 */
public record AggrAssemblyTestConfig(
        boolean             bolAggrErrChk,
        boolean             bolAggrTmDomColl,
        boolean             bolAggrConcEnbl,
        int                 szAggrConcPivot,
        int                 cntAggrConcMaxThrds
        ) 

{
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new <code>AggrAssemblyTestConfig</code> record with fields given by the argument values.
     * </p>
     * <p>
     * This creator contains all required record field values is equivalent to the canonical constructor.
     * </p>
     * 
     * @param bolAggrErrChk     enable/disable advanced error checking/verification in sampled aggregate assembly
     * @param bolAggrTmDomColl  enable/disable time-domain collisions in sampled aggregate assembly
     * @param bolAggrConcEnbl   enable/disable concurrency in sampled aggregate assembly
     * @param szAggrConcPivot   pivot size in current sampled aggregate assembly
     * @param cntAggrConcMaxThrds maximum thread count in concurrent sampled aggregate assembly
     * 
     * @return  a new <code>AggrAssemblyTestConfig</code> record populated with the above arguments
     */
    public static AggrAssemblyTestConfig    from(
            boolean             bolAggrErrChk,
            boolean             bolAggrTmDomColl,
            boolean             bolAggrConcEnbl,
            int                 szAggrConcPivot,
            int                 cntAggrConcMaxThrds
            ) 
    {
        return new AggrAssemblyTestConfig(bolAggrErrChk, bolAggrTmDomColl, bolAggrConcEnbl, szAggrConcPivot, cntAggrConcMaxThrds);
    }
    
    /**
     * <p>
     * Creates a new <code>AggrAssemblyTestConfig</code> record by extracting the test case conditions from the argument.
     * </p>
     * <p>
     * This method populates the returned record by first extracting the field 
     * <code>{@link QueryAssemblyTestResult#recTestCase()}</code> then deferring to creator 
     * <code>{@link #from(QueryAssemblyTestCase)}</code>.
     * </p>
     * 
     * @param recTestResult test result containing test case conditions for sampled aggregate assembly
     * 
     * @return  a new <code>AggrAssemblyTestConfig</code> record populated with the test case conditions within the argument
     * 
     * @see #from(QueryAssemblyTestCase)
     */
    public static AggrAssemblyTestConfig    from(QueryAssemblyTestResult recTestResult) {
        QueryAssemblyTestCase   recTestCase = recTestResult.recTestCase();
        
        return AggrAssemblyTestConfig.from(recTestCase);
    }
    
    /**
     * <p>
     * Creates a new <code>AggrAssemblyTestConfig</code> record by extracting field values from the argument.
     * </p>
     * <p>
     * The sampled aggregate assembly configuration parameters are
     * extracted from the argument test case conditions and used to populate the field values of the returned
     * record. That is, the test conditions specifying the <code>QueryResponseAssembler</code> configuration
     * are extracted and used for the record field value population.
     * </p>
     * 
     * @param recTestCase   test case containing the sampled aggregate assembly configuration parameters
     * 
     * @return  a new <code>AggrAssemblyTestConfig</code> record populated with the aggregate assembly
     */
    public static AggrAssemblyTestConfig    from(QueryAssemblyTestCase recTestCase) {
        AggrAssemblyTestConfig  recCfg = AggrAssemblyTestConfig.from(
                recTestCase.bolAggrErrChk(), 
                recTestCase.bolAggrTmDomColl(), 
                recTestCase.bolAggrConcEnbl(), 
                recTestCase.szAggrConcPivot(), 
                recTestCase.cntAggrConcMaxThrds()
                );
        
        return recCfg;
    }
    
    
    // 
    // Object Overrides
    //

    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AggrAssemblyTestConfig rec) {
            boolean bolResult = (rec.bolAggrErrChk == this.bolAggrErrChk)
                    && (rec.bolAggrTmDomColl == this.bolAggrTmDomColl)
                    && (rec.bolAggrConcEnbl == this.bolAggrConcEnbl)
                    && (rec.szAggrConcPivot == this.szAggrConcPivot)
                    && (rec.cntAggrConcMaxThrds == this.cntAggrConcMaxThrds);
            
            return bolResult;
        }
        
        return false;
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
        String  strPadd = strPad + "  ";
        
        ps.println(strPadd + "Sampled Aggregate Assembly Configuration");
        ps.println(strPadd + "  advanced error checking enabled : " + this.bolAggrErrChk);
        ps.println(strPadd + "  time-domain collisions enabled  : " + this.bolAggrTmDomColl);
        ps.println(strPadd + "  concurrent processing enabled   : " + this.bolAggrConcEnbl);
        ps.println(strPadd + "  concurrency pivot size          : " + this.szAggrConcPivot);
        ps.println(strPadd + "  concurrency max. thread count   : " + this.cntAggrConcMaxThrds);
    }

}
