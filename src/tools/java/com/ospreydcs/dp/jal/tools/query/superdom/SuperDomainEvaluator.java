/*
 * Project: dp-api-common
 * File:	SuperDomainEvaluator.java
 * Package: com.ospreydcs.dp.jal.tools.query.superdom
 * Type: 	SuperDomainEvaluator
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
 * @since Jun 14, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.superdom;

import java.util.List;

import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.app.ExitCode;
import com.ospreydcs.dp.api.app.JalApplicationBase;
import com.ospreydcs.dp.api.app.JalQueryAppBase;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.util.Log4j;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.query.correl.DataCorrelationEvaluator;

/**
 * <p>
 * JAL Tools Application for evaluating the operation and performance of super domain creation from
 * collections of raw, correlated data.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Jun 14, 2025
 *
 */
public class SuperDomainEvaluator extends JalQueryAppBase<SuperDomainEvaluator> {

    /**
     * <p>
     * Application entry point.
     * </p>
     * 
     * @param args  command-line arguments for <code>SuperDomainEvaluator</code> application
     */
    public static void main(String[] args) {
     
        //
        // ------- Special Requests -------
        //
        
        // Check for client help request
        if (JalApplicationBase.parseAppArgsHelp(args)) {
            System.out.println(STR_APP_USAGE);
            
            System.exit(ExitCode.SUCCESS.getCode());
        }
        
        // Check for client version request
        if (JalApplicationBase.parseAppArgsVersion(args)) {
            System.out.println(STR_APP_VERSION);
            
            System.exit(ExitCode.SUCCESS.getCode());
        }

        //
        // ------- Application Initialization -------
        //
        
        // Check for general command-line errors
        try {
            JalApplicationBase.parseAppArgsErrors(args, CNT_APP_MIN_ARGS, LST_STR_DELIMS);

        } catch (Exception e) {
            System.err.println(STR_APP_NAME + ": STARTUP FAILURE - bad command line arguments " + args + ".");
            System.err.println("  see " + STR_APP_NAME + " " + STR_ARG_HELP);
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.INPUT_CFG_CORRUPT);
            
        }
        

    }

    
    //
    // Application Resources
    //
    
    /** Default configuration parameters for the Query Service tools */
    private static final DpQueryConfig      CFG_QUERY = DpApiConfig.getInstance().query;
    
    /** Default configuration parameters for the JAL Tools */
    private static final JalToolsConfig     CFG_TOOLS = JalToolsConfig.getInstance();
    
    
    //
    // Application Constants - Command-Line Arguments and Messages
    //
    
    /** Minimum number of application arguments - argument name and at least one data request */
    public static final int         CNT_APP_MIN_ARGS = 2;
    
    
    /** Default output path location */
    public static final String      STR_OUTPUT_DEF = CFG_TOOLS.output + "/query/superdom";
  

    /** Argument variable identifying the PV name value(s) */
    public static final String      STR_VAR_PVS = "--pvs";
    
    /** Argument variable identifying request duration (optional) */
    public static final String      STR_VAR_DUR = "--dur";
    
    /** Argument variable identifying request delay from archive inception */
    public static final String      STR_VAR_DELAY = "--delay";
    
    /** Argument flag identifying output location */
    public static final String      STR_VAR_OUTPUT = "--output";

    /** List of all the valid argument delimiters */
    public static final List<String>    LST_STR_DELIMS = List.of(
            STR_VAR_PVS, 
            STR_VAR_DUR, 
            STR_VAR_DELAY,
            STR_VAR_OUTPUT
            );
    
    
    //
    // Application Constants - Client Messages
    //
    
    /** Application name */
    public static final String      STR_APP_NAME = SuperDomainEvaluator.class.getSimpleName();
    
    
    /** The "usage" message for client help requests or invalid application arguments */
    public static final String      STR_APP_USAGE = 
            STR_APP_NAME  + " Usage: \n"
          + "\n"
          + "% " + STR_APP_NAME
          + " [" + STR_ARG_HELP + "]"
          + " [" + STR_ARG_VERSION + "]"
          + " [R1 ... Rn]"
          + " " + STR_VAR_PVS + " PV1 [... PVm]"
          + " [" + STR_VAR_DUR + " T]"
          + " [" + STR_VAR_DELAY + " D]"
          + " [" + STR_VAR_OUTPUT +" Output]"
          + "\n\n" 
          + "  Where  \n"
          + "    " + STR_ARG_HELP + "        = print this message and return.\n"
          + "    " + STR_ARG_VERSION + "     = prints application version information and return.\n"
          + "    R1, ..., Rn   = Test request(s) to perform - 'TestArchiveRequest' enumeration name(s).\n"
          + "    PV1, ..., PVm = Additional PV name(s) to add to request(s).\n"
          + "    T             = Override of request duration - parseable duration of format 'P[nd]DT[nh]H[nm]M[ds]S',\n"
          + "                      where nd = integer number of days, \n "
          + "                           nh = integer number of hours, \n" 
          + "                            nm = integer number of minutes, \n"
          + "                            ds = decimal number of seconds w/ 1 ns resolution. \n"
          + "                      example: '" + STR_VAR_DUR + " PT5.150S' specifies 5,150 ms duration.\n"
          + "    D             = Request delay, override of request start time - parseable duration w/ format 'P[nd]DT[nh]H[nm]M[ds]S'. \n"
          + "    Output        = output directory path, or '" + STR_ARG_VAL_STDOUT + "'. \n"
          + "\n"
          + "  NOTES: \n"
          + "  - All bracketed quantities [...] are optional. \n"
          + "  - If no R1,...,Rn are included, " + STR_VAR_PVS + " and " + STR_VAR_DUR + " must be present.\n"
          + "  - Default " + STR_VAR_OUTPUT + " value is " + STR_OUTPUT_DEF + ".\n";

    
    /** The "version" message for client version requests */
    public static final String      STR_APP_VERSION = 
            STR_APP_NAME
          + " version 1.0: compatible with Java Application Library version 1.8.0 or greater.";
    
    
    //
    // Class Constants - Default Values
    //
    
    /** The target correlation data rate (MBps) */
    public static final double      DBL_RATE_TARGET = 500.0;

    
    //
    // Class Constants - Correlator Initial Configuration
    //
    
    /** Concurrency enabled flag */
    public static final boolean     BOL_CONCURRENCY = CFG_QUERY.concurrency.enabled;
    
    /** Parallelism tuning parameter - pivot to parallel processing when target set size hits this limit */
    public static final int         SZ_CONCURRENCY_PIVOT = CFG_QUERY.concurrency.pivotSize;
    
    /** Parallelism tuning parameter - default number of independent processing threads */
    public static final int         CNT_CONCURRENCY_THDS = CFG_QUERY.concurrency.maxThreads;
    
//    /** Concurrency enabled flag */
//    public static final boolean     BOL_CONCURRENCY = CFG_DEF.concurrency.enabled;
//    
//    /** Parallelism tuning parameter - pivot to parallel processing when target set size hits this limit */
//    public static final int         SZ_CONCURRENCY_PIVOT = CFG_DEF.concurrency.pivotSize;
//    
//    /** Parallelism tuning parameter - default number of independent processing threads */
//    public static final int         CNT_CONCURRENCY_THDS = CFG_DEF.concurrency.maxThreads;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLogger(SuperDomainEvaluator.class, JalQueryAppBase.STR_LOGGING_LEVEL);

    
    //
    //  Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>SuperDomainEvaluator</code> instance.
     * </p>
     *
     * @param clsApp
     * @throws DpGrpcException
     */
    public SuperDomainEvaluator() throws DpGrpcException {
        super(SuperDomainEvaluator.class);
    }

    
    //
    // JalApplicationBase Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.api.app.JalApplicationBase#getLogger()
     */
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
