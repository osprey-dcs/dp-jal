/*
 * Project: dp-api-common
 * File:	QueryAssemblyEvaluator.java
 * Package: com.ospreydcs.dp.jal.tools.query.assem
 * Type: 	QueryAssemblyEvaluator
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
 * @since Aug 8, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.assem;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;

import com.ospreydcs.dp.api.app.ExitCode;
import com.ospreydcs.dp.api.app.JalApplicationBase;
import com.ospreydcs.dp.api.app.JalQueryAppBase;
import com.ospreydcs.dp.api.common.JalDataTableType;
import com.ospreydcs.dp.api.config.JalConfig;
import com.ospreydcs.dp.api.config.query.JalQueryConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer;
import com.ospreydcs.dp.api.query.model.assem.QueryResponseAssembler;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;
import com.ospreydcs.dp.jal.tools.common.DataRateLister;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.query.testrequests.TestArchiveRequest;
import com.sun.jdi.request.InvalidRequestStateException;

/**
 * <p>
 * Application for evaluating the operation and performance of the <code>{@link QueryResponseAssembler}</code> class.
 * </p>
 * <p>
 * See class description and usage strings <code>{@link #STR_APP_DESCR}</code> and <code>{@link #STR_APP_USAGE}</code>
 * for application details.
 * <p>
 *
 * @author Christopher K. Allen
 * @since Aug 8, 2025
 *
 */
public class QueryAssemblyEvaluator extends JalQueryAppBase<QueryAssemblyEvaluator> {

    
    //
    // Application Main
    //
    
    /**
     * <p>
     * Entry point for the <code>QueryAssemblyEvaluator</code> application.
     * </p>
     * 
     * @param args  application command-line arguments
     */
    public static void main(String[] args) {

        //
        // ------- Special Requests -------
        //
        
        // Check for client help request
        if (JalApplicationBase.parseAppArgsHelp(args)) {
            System.out.println();
            System.out.println(STR_APP_DESCR);
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
            JalApplicationBase.terminateWithExceptionAndMessage(QueryAssemblyEvaluator.class, e, STR_APP_USAGE, ExitCode.INPUT_CFG_CORRUPT);

        }
        
        // Get the test suite configuration and output location from the application arguments
        String                          strOutputLoc;
        QueryAssemblyTestSuiteCreator   cfgTestSuite;
        try {
            
            cfgTestSuite = QueryAssemblyEvaluator.parseTestSuiteConfig(args);
            strOutputLoc = JalApplicationBase.parseOutputLocation(args, STR_OUTPUT_DEF);
            
        } catch (Exception e) {
            
            JalApplicationBase.terminateWithException(QueryAssemblyEvaluator.class, e, ExitCode.INTPUT_ARG_INVALID);
            return;
        }

        
        //
        // ------- Application Execution -------
        //
        
        // Create the query assembly evaluator and run it
        try {
            QueryAssemblyEvaluator    evaluator = new QueryAssemblyEvaluator(cfgTestSuite, strOutputLoc, args);
            
            evaluator.run();
            evaluator.writeReport();
            evaluator.shutdown();
            
            System.out.println(STR_APP_NAME + " Execution completed in " + evaluator.getRunDuration());
            System.out.println("  Results stored at " + evaluator.getOutputFilePath().toAbsolutePath());
            System.exit(ExitCode.SUCCESS.getCode());
            
        } catch (DpGrpcException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Unable to connect to Query Service.");
            JalApplicationBase.terminateWithException(QueryAssemblyEvaluator.class, e, ExitCode.INITIALIZATION_EXCEPTION);
            
        } catch (ConfigurationException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - No time-series data requests in command line.");
            JalApplicationBase.terminateWithException(QueryAssemblyEvaluator.class, e, ExitCode.INTPUT_ARG_INVALID);
            
        } catch (UnsupportedOperationException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Output location not found in file system: " + strOutputLoc);
            JalApplicationBase.terminateWithException(QueryAssemblyEvaluator.class, e, ExitCode.OUTPUT_ARG_INVALID);
            
        } catch (FileNotFoundException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Unable to create output file.");
            JalApplicationBase.terminateWithException(QueryAssemblyEvaluator.class, e, ExitCode.OUTPUT_FAILURE);
            
        } catch (SecurityException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Unable to write to output file.");
            JalApplicationBase.terminateWithException(QueryAssemblyEvaluator.class, e, ExitCode.OUTPUT_FAILURE);
            
        } catch (InvalidRequestStateException e) {
            System.err.println(STR_APP_NAME + " execution FAILURE - Application already executed.");
            JalApplicationBase.terminateWithException(QueryAssemblyEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
            
        } catch (IllegalStateException e) {
            System.err.println(STR_APP_NAME + " data recovery ERROR - Message request attempt on empty buffer.");
            JalApplicationBase.terminateWithException(QueryAssemblyEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
            
        } catch (InterruptedException e) {
            System.err.println(STR_APP_NAME + " data recovery ERROR - Process interrupted while waiting for buffer message.");
            JalApplicationBase.terminateWithException(QueryAssemblyEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
            
        }
        
    }

    
    //
    // Application Resources
    //
    
    /** Default configuration parameters for the JAL library */
    private static final JalQueryConfig      CFG_QRY = JalConfig.getInstance().query;
    
    /** Default configuration parameters for the JAL Tools */
    private static final JalToolsConfig     CFG_TOOLS = JalToolsConfig.getInstance();
    
    
    //
    // Application Constants - Command-Line Arguments and Messages
    //
    
    /** Minimum number of application arguments - argument name and at least one data request */
    public static final int         CNT_APP_MIN_ARGS = 2;
    
    
    /** Default output path location */
    public static final String      STR_OUTPUT_DEF = CFG_TOOLS.output + "/query/assem";
  
  
    /** Default data table build enable/disable flag */
    public static final boolean             BOL_TBL_BLD_ENBL_DEF = false;
    
    /** Default data table type */
    public static final JalDataTableType    ENM_TBL_TYPE_DEF = CFG_QRY.data.table.result.type;
    

    /** Argument variable identifying the PV name value(s) */
    public static final String      STR_VAR_RQST_PVS = "--pvs";
    
    /** Argument variable identifying request duration (optional) */
    public static final String      STR_VAR_RQST_DUR = "--dur";
    
    /** Argument variable identifying request delay from archive inception */
    public static final String      STR_VAR_RQST_DELAY = "--delay";
    
    
    /** Argument variable identifying advanced error checking enable/disable flags for sampled aggregate assembly */
    public static final String      STR_VAR_AGGR_ERR_CHK_ENBL = "--aggr_errchk";
    
    /** Argument variable identifying time-domain collision enable/disable flags for sampled aggregate assembly */
    public static final String      STR_VAR_AGGR_TMDOM_COLL_ENBL = "--aggr_tmdom";
    
    /** Argument variable identifying concurrent sampled aggregate assembly enable/disable flags */
    public static final String      STR_VAR_AGGR_CONC_ENBL = "--aggr_conc";
    
    /** Argument variable identifying concurrency pivot size enable/disable in concurrent sampled aggregate assembly */
    public static final String      STR_VAR_AGGR_CONC_PVT_SZ = "--aggr_pvtsz";
    
    /** Argument variable identifying maximum thread counts in concurrent sampled aggregate assembly */
    public static final String      STR_VAR_AGGR_CONC_MAX_THRDS = "--aggr_maxthrds";

    
    /** Argument variable identifying the build table enable/disable flags */
    public static final String      STR_VAR_TBL_BLD_ENBL = "--tbl_bld";
    
    /** Argument variable identifying the final result table type to generate */
    public static final String      STR_VAR_TBL_TYPE = "--tbl_type";
    
    /** Argument variable identifying static table default in AUTO table creation */
    public static final String      STR_VAR_TBL_STAT_DEF = "--tbl_statdef";
    
    /** Argument variable identifying static table maximum size enable/disable flag in AUTO table creation */
    public static final String      STR_VAR_TBL_STAT_MAX_ENBL = "--tbl_statmaxenbl";
    
    /** Argument variable identifying static table maximum size in AUTO table creation */
    public static final String      STR_VAR_TBL_STAT_MAX_SZ = "--tbl_statmaxsz";
    
    
    /** List of all the valid argument delimiters */
    public static final List<String>    LST_STR_DELIMS = List.of(
            STR_VAR_RQST_PVS, 
            STR_VAR_RQST_DUR, 
            STR_VAR_RQST_DELAY,
            
            STR_VAR_AGGR_ERR_CHK_ENBL,
            STR_VAR_AGGR_TMDOM_COLL_ENBL,
            STR_VAR_AGGR_CONC_ENBL,
            STR_VAR_AGGR_CONC_PVT_SZ,
            STR_VAR_AGGR_CONC_MAX_THRDS,
            
            STR_VAR_TBL_BLD_ENBL,
            STR_VAR_TBL_TYPE,
            STR_VAR_TBL_STAT_DEF,
            STR_VAR_TBL_STAT_MAX_ENBL,
            STR_VAR_TBL_STAT_MAX_SZ,
            
            STR_VAR_OUTPUT      // from base class
            );
    
    

    //
    // Application Constants - Client Messages
    //
    
    /** Application name */
    public static final String      STR_APP_NAME = QueryAssemblyEvaluator.class.getSimpleName();
    
    
    /** A laconic description of the application function */
    public static final String      STR_APP_DESCR = 
            STR_APP_NAME + " Description \n"
          + "- Application evaluates the performance and operation of the QueryResponseAssembler class \n"
          + "    for and assembling time-series data after request data recovery and correlation. \n"
          + "- The assembly processes consists of the following 3 steps: \n"
          + "    1. Recovery and correlation of raw time-series data from Query Service. \n"
          + "    2. Conversion and assembly of raw correlated data blocks into an aggregation of 'sampled blocks'. \n"
          + "    3. Creation of an IDataTable concrete implementation from the above SampledAggregate instance. \n"
          + "\n"
          + "- The Data Platform must be populated with data from the Test Archive by running application \n"
          + "    'app-run-test-data-generator'.\n"
          + "    Additional time-series sample processes can be added using JAL application 'AddTestArchiveData'. \n"
          + "- Data Platform Test Archive requests are first performed where the data is recovered \n"
          + "    and correlated into raw data blocks associated with a common timestamp message. \n"
          + "- The recovery and correlation activity is not targeted, the data is simply collected for the table \n"
          + "    assembly process using the QueryRequestRecoverer class. \n"
          + "    Note that the recovery/correlation process does require processing resources and will be seen in \n"
          + "    real time."
          + "\n"
          + "- The focus of the application is the assembly of raw, correlated data into SampledAggregate instances, \n"
          + "    and the data tables that are created from the SampledAggregate. \n"
          + "- The raw data is assembled into sampled data blocks where time-domain collisions are \n"
          + "    identified and addressed. This is typically the source of most real-time processing for \n"
          + "    data table assembly.  Thus, performance is typically best when all request processes have identical \n"
          + "    timestamps. \n"
          + "- The sampled blocks are aggregated into a collection of all time-series request data than can \n"
          + "    be used to create both static and dynamic data tables.\n"
          + "- The operation is monitored, results are computed and stored in the output file.\n";
    
    /** The "usage" message for client help requests or invalid application arguments */
    public static final String      STR_LINE_BREAK = "\n                        "; 
    public static final String      STR_APP_USAGE = 
            STR_APP_NAME  + " Usage: \n"
          + "\n"
          + "% " + STR_APP_NAME
          + " [" + STR_VAR_HELP + "]"
          + " [" + STR_VAR_VERSION + "]"
          + STR_LINE_BREAK
          + " [R1 ... Rn]"
          + " " + STR_VAR_RQST_PVS + " [PV1 ... PVn]"
          + " [" + STR_VAR_RQST_DUR + " R]"
          + " [" + STR_VAR_RQST_DELAY + " D]"
          + STR_LINE_BREAK
          + " [" + STR_VAR_AGGR_ERR_CHK_ENBL + " FALSE ... TRUE]"
          + " [" + STR_VAR_AGGR_TMDOM_COLL_ENBL + " FALSE ... TRUE]"
          + " [" + STR_VAR_AGGR_CONC_ENBL + " FALSE ... TRUE]"
          + " [" + STR_VAR_AGGR_CONC_PVT_SZ + " P1 ... Pn]"
          + " [" + STR_VAR_AGGR_CONC_MAX_THRDS + " M1 ... Mn]"
          + STR_LINE_BREAK
          + " [" + STR_VAR_TBL_BLD_ENBL + " FALSE TRUE]"
          + " [" + STR_VAR_TBL_TYPE + " T1 ... Tn]"
          + " [" + STR_VAR_TBL_STAT_DEF + " FALSE ... TRUE]"
          + " [" + STR_VAR_TBL_STAT_MAX_ENBL + " FALSE TRUE]"
          + " [" + STR_VAR_TBL_STAT_MAX_SZ + "S1 ... Sn]"
          + STR_LINE_BREAK
          + " [" + STR_VAR_OUTPUT +" Output]"
          + "\n\n" 
          + "  Where  \n"
          + "    " + STR_VAR_HELP + "        = print this message and application description.\n"
          + "    " + STR_VAR_VERSION + "     = prints application version information and return.\n"
          + "    R1, ..., Rn   = Test request(s) to perform - 'TestArchiveRequest' enumeration name(s).\n"
          + "    PV1, ..., PVn = Additional PV name(s) to add to request(s).\n"
          + "    R             = Override of request duration - parseable duration of format 'P[nd]DT[nh]H[nm]M[ds]S',\n"
          + "                      where nd = integer number of days, \n "
          + "                           nh = integer number of hours, \n" 
          + "                            nm = integer number of minutes, \n"
          + "                            ds = decimal number of seconds w/ 1 ns resolution. \n"
          + "                      example: '" + STR_VAR_RQST_DUR + " PT5.150S' specifies 5,150 ms duration.\n"
          + "    D             = Request delay, override of request start time - parseable duration w/ format 'P[nd]DT[nh]H[nm]M[ds]S'. \n"
          + "    " + STR_VAR_AGGR_ERR_CHK_ENBL + " = Enable/disable advanced error checking in sampled aggregate assembly - { FALSE, TRUE}. \n"
          + "    " + STR_VAR_AGGR_TMDOM_COLL_ENBL + "  = Enable/disable time-domain collisions in sampled aggregate assembly (requires super-domains) - { FALSE, TRUE}. \n"
          + "    " + STR_VAR_AGGR_CONC_ENBL + "   = Enable/disable concurrency (multi-threading) in sampled aggregrate assembly - { FALSE, TRUE }. \n"
          + "    P1, ..., Pn   = Sampled block count triggering concurrency in sampled aggregate assembly (pivot size) - Integer value(s). \n"
          + "    M1, ..., Mn   = Maximum thread counts for concurrent sampled aggregate assembly - Integer value(s). \n"
          + "    " + STR_VAR_TBL_BLD_ENBL + "     = Enable/disable the creation of a data table after sampled aggregate assembly - { FALSE, TRUE }. \n"
          + "    T1, ..., Tn   = Data table type(s) - 'JalDataTableType' enumeration name(s). \n"
          + "    " + STR_VAR_TBL_STAT_DEF + " = Use static tables as default in JalDataTableType.AUTO table creation - { FALSE, TRUE}. \n"
          + "    " + STR_VAR_TBL_STAT_MAX_ENBL + " = Enable/disable maximum size enforcement for static tables - { FALSE, TRUE }. \n"
          + "    S1, ..., Sn   = Maximum allowable static data table size - Long value(s). \n"
          + "    Output        = output directory path, or '" + STR_ARG_VAL_STDOUT + "'. \n"
          + "\n"
          + "  NOTES: \n"
          + "  - All bracketed quantities [...] are optional. \n"
          + "  - If no R1,...,Rn are included, " + STR_VAR_RQST_PVS + " and " + STR_VAR_RQST_DUR + " must be present.\n"
          + "  - The default " + STR_VAR_TBL_BLD_ENBL + " value is '" + BOL_TBL_BLD_ENBL_DEF + "'. \n"
          + "  - Currently the option " + STR_VAR_TBL_BLD_ENBL + " is ignored, that is, data tables are always created. \n"
          + "  - The default " + STR_VAR_TBL_TYPE + " value is '" + ENM_TBL_TYPE_DEF + "'. \n"
          + "  - Default " + STR_VAR_OUTPUT + " value is " + STR_OUTPUT_DEF + ".\n";

    
    /** The "version" message for client version requests */
    public static final String      STR_APP_VERSION = 
            STR_APP_NAME
          + " version 1.0: compatible with Java Application Library version 1.8.0 or greater.";
    
    
    //
    // Class Constants - Default Values
    //
    
    /** The target correlation data rate (MBps) */
    public static final double      DBL_RATE_TARGET = 200.0;


    // 
    // Defining Attributes
    //
    
    /** The (pre-configured) test suite creator used for <code>QueryAssemblyTestCase</code> instance generation */
    private final QueryAssemblyTestSuiteCreator       cfgTestSuite;
    
    
    //
    // Instance Resources
    //
    
    /** The time-series data recoverer/processor used for raw data used for assembly test evaluations */
    private final QueryRequestRecoverer                 prcrRqstRcvry;
    
    
    /** The collection of test cases used in evaluations */
    private final Collection<QueryAssemblyTestCase>     setTestCases;
    
    /** The collection of test results recovered from evaluations */
    private final Collection<QueryAssemblyTestResult>   setTestResults;
    
    /** The collection of test failures seen in evaluations */
    private final Collection<QueryAssemblyTestFailure>  setTestFails;
    
    
    //
    // State Variables
    //
    
    /** Total duration of test suite evaluation */
    private Duration    durEval = Duration.ZERO;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLogger(QueryAssemblyEvaluator.class, JalQueryAppBase.STR_LOGGING_LEVEL);

    
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

    
    //
    // Application Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>QueryAssemblyEvaluator</code> instance.
     * </p>
     *
     * @param strOutputLoc  path location for the output file
     * @param args          application command-line arguments
     * 
     * @throws DpGrpcException          unable to establish connection to the Query Service (see message and cause)
     * @throws ConfigurationException   the test suite configuration was invalid
     * @throws UnsupportedOperationException the output path is not associated with the default file system 
     * @throws FileNotFoundException    unable to create output file (see message and cause)
     * @throws SecurityException        unable to write to output file
     */
    public QueryAssemblyEvaluator(QueryAssemblyTestSuiteCreator cfgTestSuite, String strOutputLoc, String... args) 
            throws DpGrpcException, 
                   ConfigurationException, 
                   UnsupportedOperationException, FileNotFoundException, SecurityException 
    {
        super(QueryAssemblyEvaluator.class, args);
        
        this.cfgTestSuite = cfgTestSuite;
        
        // Create resources
        this.prcrRqstRcvry = QueryRequestRecoverer.from(super.connQuery);
        
        // Create test containers
        this.setTestCases = cfgTestSuite.createTestSuite();
        this.setTestResults = new TreeSet<>(QueryAssemblyTestResult.descendingAssmRateOrdering());
        this.setTestFails = new TreeSet<>(QueryAssemblyTestFailure.ascendingIndexOrdering());
        
        // Create the output stream and attach Logger to it - records fatal errors to output file
        super.openOutputStream(strOutputLoc); // throws SecurityException, FileNotFoundException, UnsupportedOperationException
        
        OutputStreamAppender    appAppErrs = Log4j.createOutputStreamAppender(STR_APP_NAME, super.psOutput);
        Log4j.attachAppender(LOGGER, appAppErrs);
    }


    //
    // Operations
    //
    
    /**
     * <p>
     * Runs the application evaluating all test cases within the test suite configuration.
     * </p>
     * 
     */
    public void run() {

        // Check state
        if (super.bolRun) {
            throw new InvalidRequestStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Application has already been run.");
        }
        super.bolRun = true;
        
        // Start counter
        System.out.print("Performing evaluations (" + this.setTestCases.size() + " cases) .");
        super.startExecutionTimer(1L, TimeUnit.SECONDS);

        // Iterate over each test case in test suite
        Instant insStart = Instant.now();
        for (QueryAssemblyTestCase recTestCase : this.setTestCases) {
            QueryAssemblyTestResult recTestResult = recTestCase.evaluate(this.prcrRqstRcvry); // throws DpQueryException
            
            this.setTestResults.add(recTestResult);
        }
        
        // Collect any test failures
        this.setTestFails.addAll( QueryAssemblyTestFailure.extractFailures(this.setTestResults) );
        
        Instant insFinish = Instant.now();

        this.durEval = Duration.between(insStart, insFinish);
        super.stopExecutionTimer();
        System.out.println(" Evaluations completed in " + this.durEval.toSeconds() + " seconds.");
        
        // Set state variables
        super.bolCompleted = true;
        super.bolRun = true;
        
    }
    
    /**
     * <p>
     * Returns the duration of the evaluations.
     * </p>
     * 
     * @return  the duration of the <code>{@link #run()}</code> operation
     * 
     * @throws IllegalStateException    the evaluations have not been executed.
     */
    public Duration getRunDuration() throws IllegalStateException {
        
        // Check state
        if (!super.bolRun)
            throw new IllegalStateException("Evaluations have not been executed.");
        
        return this.durEval;
    }
    
    /**
     * <p>
     * Creates a text report of the test suite evaluations and prints it to the output file.
     * </p>
     * <p>
     * This method is available after invoking <code>{@link #run()}</code>.  It prints out a report
     * of the <code>CorrelatorTestCase</code> evaluations including a summary, test suite configuration, and
     * all test case results.
     * </p>
     * <p>
     * This method defers to <code>{@link #writeReport(PrintStream)}</code> supplying the output file stream
     * for this evaluator as the argument.
     * </p>
     * 
     * @throws IllegalStateException    no results are available (called before <code>{@link #run()}</code>) 
     */
    public void writeReport() throws IllegalStateException {
        this.writeReport(super.psOutput);
    }
    
    /**
     * <p>
     * Creates a text report of the test suite evaluations and prints it to the given output stream.
     * </p>
     * <p>
     * This method is available after invoking <code>{@link #run()}</code>.  It prints out a report
     * of the raw data recovery and correlation evaluations including a summary, test suite configuration, and
     * all test case results.
     * </p>
     * 
     * @param ps    target output stream for <code>QueryChannel</code> evaluations report
     * 
     * @throws IllegalStateException    no results are available (called before <code>{@link #run()}</code>) 
     */
    public void writeReport(PrintStream ps) throws IllegalStateException {
        
        // Check state
        if (!this.bolRun)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + "- Test suite has not been run.");
        
        // Create left-hand side padding
        String  strPad = "  ";
        
        // Print out header
        String  strHdr = super.createReportHeader();
        ps.println();
        ps.println(strHdr);
        ps.println();
        
        // Print out command line
        String  strCmdLn = super.createCommandLine();
        ps.println("Execution");
        ps.println(strCmdLn);
        ps.println();
        
        // Print out evaluation summary
        ps.println("Test cases specified : " + this.setTestCases.size());
        ps.println("Test cases run       : " + this.setTestResults.size());
        ps.println("Test case failures   : " + this.setTestFails.size());
        ps.println("Evaluation duration  : " + this.durEval);
        ps.println("Evaluation completed : " + this.bolCompleted);
        ps.println();
        
        // Print out raw data processor configuration
        ps.println("QueryRequestRecoverer Configuration");
        this.prcrRqstRcvry.printOutConfig(ps, strPad);
        ps.println();
        
        // Print out the test suite configuration
        ps.println("Test Suite Configuration");
        this.cfgTestSuite.printOut(ps, "  ");
        ps.println();
        
        // Print out test case data rates
        ps.println("Test Case Data Rates - Raw Data Recovery/Correlation");
        DataRateLister<QueryAssemblyTestResult>  lstrRawRates = DataRateLister.from(
                rec -> rec.recTestCase().indCase(), 
                rec -> rec.strRqstId(), 
                rec -> rec.szAllocRawPrcd(), 
                rec -> rec.dblRateRawPrcd()
                );
        lstrRawRates.printOut(ps, strPad, this.setTestResults);
        ps.println();
        
        ps.println("Test Case Data Rates - Sampled Aggregate Assembly");
        DataRateLister<QueryAssemblyTestResult> lstrAssmRates = DataRateLister.from(
                rec -> rec.recTestCase().indCase(), 
                rec -> rec.strRqstId(), 
                rec -> rec.szAllocAggrAssm(), 
                rec -> rec.dblRateAggAssm()
                );
        lstrAssmRates.printOut(ps, strPad, this.setTestResults);
        ps.println();
        
        ps.println("Test Case Data Rates - Table Creation");
        DataRateLister<QueryAssemblyTestResult> lstrTblRates = DataRateLister.from(
                rec -> rec.recTestCase().indCase(), 
                rec -> rec.strRqstId(), 
                rec -> rec.szTblCalc(), 
                rec -> rec.dblRateTblBld()
                );
        lstrTblRates.printOut(ps, strPad, this.setTestResults);
        ps.println();

        ps.println("Test Case Data Rates - Total Requst");
        DataRateLister<QueryAssemblyTestResult> lstrTotRates = DataRateLister.from(
                rec -> rec.recTestCase().indCase(), 
                rec -> rec.strRqstId(), 
                rec -> rec.szAllocTotal(), 
                rec -> rec.dblRateTotal()
                );
        lstrTotRates.printOut(ps, strPad, this.setTestResults);
        ps.println();
        
        
        // Summarize the results and print out
//        ps.println("Test Results Summary");
        QueryAssemblyTestsSummary.assignTargetDataRate(DBL_RATE_TARGET);
        QueryAssemblyTestsSummary  recSummary = QueryAssemblyTestsSummary.summarize(this.setTestResults);
        recSummary.printOut(ps, null);
        ps.println();
        
        // Print out any failed tests
        if (!this.setTestFails.isEmpty()) {
            ps.println("Test Failures");
            this.setTestFails.forEach(rec -> rec.printOut(ps, strPad));
            ps.println();
        }
        
        // Score the test configurations and print out
        ps.println("QueryResponseAssembler Configuration Scoring");
        AggrAssemblyConfigScore score = AggrAssemblyConfigScore.from(this.setTestResults);
        score.printOutByRates(ps, strPad);
        ps.println();
        
        // Print out each test result
        ps.println("Individual Case Results:");
        for (QueryAssemblyTestResult recCase : this.setTestResults) {
            recCase.printOut(ps, strPad);
            ps.println();
        }
    }    

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Parse the application command-line argument for the test suite generation configuration and returns it.
     * </p>
     * <p>
     * The method parse the given collection of arguments for the following information in order:
     * <ol>
     * <li>Test Requests - commands identified by <code>{@link TestArchiveRequest}</code> enumeration constants. </li>
     * <li>Supplemental PVs - values identified by the variable delimiter {@value #STR_VAR_RQST_PVS}. </li>
     * <li>Request duration override - single value identified by delimiter {@value #STR_VAR_RQST_DUR}. </li>
     * <li>Request delay override - single value identified by delimiter {@value #STR_VAR_RQST_DELAY}. </li>
     * <li>Assembly advanced error checking/verification flags - values identified by delimiter {@value #STR_VAR_AGGR_ERR_CHK_ENBL}. </li>
     * <li>Assembly time-domain collision enable flags - values identified by delimiter {@value #STR_VAR_AGGR_TMDOM_COLL_ENBL}. </li>
     * <li>Concurrent assembly enable flags - values identified by delimiter {@value #STR_VAR_AGGR_CONC_ENBL}. </li>
     * <li>Concurrent assembly pivot sizes - values identified by delimiter {@value #STR_VAR_AGGR_CONC_PVT_SZ}. </li>
     * <li>Concurrent assembly maximum thread counts - values identified by delimiter {@value #STR_VAR_AGGR_CONC_MAX_THRDS}. </li>
     * <li>Result table types - values identified by the delimiter {@value #STR_VAR_TBL_TYPE}. </li>
     * </ol> 
     * <p>
     * The application command line is first parsed for "commands", which are the <code>{@link TestArchiveRequest}</code>
     * enumeration constants describing the time-series data requests used in the return test suite.
     * These arguments must be the first to appear on the command line and do not have delimiters.  
     * At least one request is required for valid test suite creation, or a list of supplemental PVs with request duration.
     * Otherwise an exception is thrown.  
     * If a test request has an invalid <code>{@link TestArchiveRequest}</code> name an exception is thrown.
     * If a result table type has an invalid <code>{@link JalDataTableType}</code> name an exception is thrown,    
     * </p>
     * <p>
     * The remaining arguments are optional; the returned  <code>QueryAssemblyTestSuiteCreator</code> instance has default 
     * values that will be supplied if any are missing.  
     * Note that all values for numeric variables must be parseable. For example,
     * {@value #STR_VAR_AGGR_CONC_PVT_SZ} and {@value #STR_VAR_AGGR_CONC_MAX_THRDS} are <code>Integer</code> valued and 
     * must be parsed as such.  If an error occurs while parsing a numeric value an exception is thrown.
     * </p>
     *  
     * @param args  the application command-line argument collection
     * 
     * @return  the test suite configuration as described by the command-line arguments
     * 
     * @throws NoSuchElementException   no <code>TestArchiveRequest</code> constants or supplemental PVs were found
     * @throws ConfigurationException   request duration or request delay variable contained more than a single value
     * @throws MissingResourceException a request duration was not provided when using only a supplemental PV list
     * @throws IllegalArgumentException invalid test request enumeration constant name (not in <code>TestArchiveRequest</code>)
     * @throws NumberFormatException    an invalid numeric argument was encounter (could not be converted to a <code>Integer</code> type)
     * @throws DateTimeParseException   an invalid time duration format was encountered ('PnDTnHnMd.dS')
     */
    private static QueryAssemblyTestSuiteCreator    parseTestSuiteConfig(String[] args) 
            throws NoSuchElementException, MissingResourceException, ConfigurationException, IllegalArgumentException, NumberFormatException, DateTimeParseException {
     
        //
        // --- Test Requests ---
        //
        
        // Parse the data requests enumerations and the supplemental PVs
        List<String>    lstStrRqstNms = JalApplicationBase.parseAppArgsCommands(args);
        List<String>    lstStrSupplPvs = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_RQST_PVS);
        if (lstStrRqstNms.isEmpty() && lstStrSupplPvs.isEmpty()) {
            String  strMsg = "The command-line arguments contained no time-series data requests or supplemental PVs. Use --help command.";

            throw new NoSuchElementException(strMsg);
        }
        
        // Parse the request duration override value
        List<String>    lstStrRqstDur = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_RQST_DUR);
        if (lstStrRqstDur.size() > 1) {
            String  strMsg = "Only one value for variable " + STR_VAR_RQST_DUR + " is allowed.  Found value " + lstStrRqstDur + ".";
            
            throw new ConfigurationException(strMsg);
        }
        
        // Parse the request start delay value
        List<String>    lstStrRqstDly = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_RQST_DELAY);
        if (lstStrRqstDly.size() > 1) {
            String  strMsg = "Only one value for variable " + STR_VAR_RQST_DELAY + " is allowed.  Found value " + lstStrRqstDly + ".";
            
            throw new ConfigurationException(strMsg);
        }
        
        // Check edge condition: no TestArchiveRequest and no request duration
        if (lstStrRqstNms.isEmpty() && lstStrRqstDur.isEmpty()) {
            String  strMsg = "A request duration " + STR_VAR_RQST_DUR + " must be specified for PV list " + lstStrSupplPvs + ".";
            
            throw new MissingResourceException(strMsg, TestArchiveRequest.class.getName(), STR_VAR_RQST_DUR);
        }

        // Convert to TestArchiveRequest enumeration constants
        List<TestArchiveRequest>    lstEnmRqsts = lstStrRqstNms.stream()
                .<TestArchiveRequest>map(strNm -> 
                    TestArchiveRequest.valueOf(TestArchiveRequest.class, strNm))    // throws IllegalArgumentException
                .toList();
        
        // Convert request duration if present
        Duration    durRange = null;
        if (!lstStrRqstDur.isEmpty())  {
            String  strRqstDur = lstStrRqstDur.get(0);
            
            durRange = Duration.parse(strRqstDur);      // throws DateTimeParseException
        }
        
        // Convert the request delay if present
        Duration    durDelay = null;
        if (!lstStrRqstDly.isEmpty()) {
            String  strRqstDly = lstStrRqstDly.get(0);
            
            durDelay = Duration.parse(strRqstDly);      // throws DataTimeParseException
        }
        
        
        //
        // --- Sampled Aggregate Assembly ---
        //
        
        // Parse the assembly advanced error checking/verification flags if present and convert to Boolean
        List<String>    lstStrAssmErrChkEnbls = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_AGGR_ERR_CHK_ENBL);
        List<Boolean>   lstBolAssmErrChkEnbls = lstStrAssmErrChkEnbls.stream()
                .<Boolean>map(str -> Boolean.parseBoolean(str))     // no exception - defaults to 'false'
                .toList();
        
        // Parse the assembly time-domain collision enable/disable flags if present and convert to Boolean
        List<String>    lstStrAssmTmDomCollEnbls = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_AGGR_TMDOM_COLL_ENBL);
        List<Boolean>   lstBolAssmTmDomCollEnbls = lstStrAssmTmDomCollEnbls.stream()
                .<Boolean>map(str -> Boolean.parseBoolean(str))     // no exception - defaults to 'false'
                .toList();
        
        // Parse the assembly concurrent enable/disable flags if present and convert to Boolean
        List<String>    lstStrAssmConcEnbls = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_AGGR_CONC_ENBL);
        List<Boolean>   lstBolAssmConcEnbls = lstStrAssmConcEnbls.stream()
                .<Boolean>map(str -> Boolean.parseBoolean(str))     // no exception - defaults to 'false'
                .toList();
        
        // Parse the concurrent assembly pivot sizes if present and convert to Integer
        List<String>    lstStrAssmConcPvtSzs = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_AGGR_CONC_PVT_SZ);
        List<Integer>   lstIntAssmConcPvtSzs = lstStrAssmConcPvtSzs.stream()
                .<Integer>map(str -> Integer.parseInt(str))         // throws NumberFormatException
                .toList();
        
        // Parse the concurrent assembly maximum thread count if present and convert to Integer
        List<String>    lstStrAssmConcMaxThrdCnts = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_AGGR_CONC_MAX_THRDS);
        List<Integer>   lstIntAssmConcMaxThrdCnts = lstStrAssmConcMaxThrdCnts.stream()
                .<Integer>map(str -> Integer.parseInt(str))         // throws NumberFormatException
                .toList();
        
        
        //
        // Data Table Creation
        //
        
        // Parse the result table types if present and convert to JalDataTableType enumerations
        List<String>            lstStrTblTypes = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_TBL_TYPE);
        List<JalDataTableType>  lstEnmTblTypes = lstStrTblTypes.stream()
                .<JalDataTableType>map(str -> JalDataTableType.valueOf(JalDataTableType.class, str))    // throws IllegalArgumentException
                .toList();
        
        // Parse the static table default flags if present and convert to Boolean
        List<String>    lstStrTblStatDefs = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_TBL_STAT_DEF);
        List<Boolean>   lstBolTblStatDefs = lstStrTblStatDefs.stream()
                .<Boolean>map(str -> Boolean.parseBoolean(str))         // no exception - default to 'false'
                .toList();
        
        // Parse the static table maximum size enable flags if present and convert to Boolean
        List<String>    lstStrTblStatMaxSzEnbls = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_TBL_STAT_MAX_ENBL);
        List<Boolean>   lstBolTblStatMaxSzEnbls = lstStrTblStatMaxSzEnbls.stream()
                .<Boolean>map(str -> Boolean.parseBoolean(str))         // no exception - defaults to 'false'
                .toList();
        
        // Parse the static table maximum sizes if present and convert to Long 
        List<String>    lstStrTblStatMaxSzs = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_TBL_STAT_MAX_SZ);
        List<Long>      lstLngTblStatMaxSzs = lstStrTblStatMaxSzs.stream()
                .<Long>map(str -> Long.parseLong(str))                  // throws NumberFormatException
                .toList();
        
        
        // Build the test suite generator and return it
        QueryAssemblyTestSuiteCreator suite = QueryAssemblyTestSuiteCreator.create();
        
        suite.addTestRequests(lstEnmRqsts);
        suite.addSupplementalPvs(lstStrSupplPvs);
        if (durRange != null)
            suite.setRequestRange(durRange);
        if (durDelay != null)
            suite.setRequestDelay(durDelay);
        
        suite.addAggregateErrorCheckingEnables(lstBolAssmErrChkEnbls);
        suite.addAggregateTimeDomainCollisionsEnables(lstBolAssmTmDomCollEnbls);
        suite.addAggregateConcurrentEnables(lstBolAssmConcEnbls);
        suite.addAggregateConcurentPivotSizes(lstIntAssmConcPvtSzs);
        suite.addAggregateConcurrentMaxThreadCounts(lstIntAssmConcMaxThrdCnts);
        
        suite.addTableTypes(lstEnmTblTypes);
        suite.addTableStaticDefaults(lstBolTblStatDefs);
        suite.addTableStaticMaxSizeEnables(lstBolTblStatMaxSzEnbls);
        suite.addTableStaticMaxSizes(lstLngTblStatMaxSzs);
        
        return suite;
    }

}
