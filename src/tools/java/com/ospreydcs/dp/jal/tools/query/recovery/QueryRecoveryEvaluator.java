/*
 * Project: dp-api-common
 * File:	QueryRecoveryEvaluator.java
 * Package: com.ospreydcs.dp.jal.tools.query.recovery
 * Type: 	QueryRecoveryEvaluator
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
 * @since Jul 19, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.recovery;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;

import com.ospreydcs.dp.api.app.ExitCode;
import com.ospreydcs.dp.api.app.JalApplicationBase;
import com.ospreydcs.dp.api.app.JalQueryAppBase;
import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.query.testrequests.TestArchiveRequest;
import com.sun.jdi.request.InvalidRequestStateException;

/**
 * <p>
 * Application for evaluating the operation and performance of the <code>{@link QueryRequestRecoverer}</code> class.
 * </p>
 * <p>
 * See class description and usage strings <code>{@link #STR_APP_DESCR}</code> and <code>{@link #STR_APP_USAGE}</code>
 * for application details.
 * <p>
 *
 * @author Christopher K. Allen
 * @since Jul 19, 2025
 *
 */
public class QueryRecoveryEvaluator extends JalQueryAppBase<QueryRecoveryEvaluator> {

    /**
     * <p>
     * Application entry point.
     * </p>
     * 
     * @param args  the ordered collection of command-line arguments
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
            JalApplicationBase.terminateWithExceptionAndMessage(QueryRecoveryEvaluator.class, e, STR_APP_USAGE, ExitCode.INPUT_CFG_CORRUPT);

        }
        
        // Get the test suite configuration and output location from the application arguments
        String                          strOutputLoc;
        QueryRecoveryTestSuiteCreator    suite;
        try {
            
            suite = QueryRecoveryEvaluator.parseTestSuiteConfig(args);
            strOutputLoc = JalApplicationBase.parseOutputLocation(args, STR_OUTPUT_DEF);
            
        } catch (Exception e) {
            
            JalApplicationBase.terminateWithException(QueryRecoveryEvaluator.class, e, ExitCode.INTPUT_ARG_INVALID);
            return;
        }

        
        //
        // ------- Application Execution -------
        //
        
        // Create the application and run it
        try {
            QueryRecoveryEvaluator    evaluator = new QueryRecoveryEvaluator(suite, strOutputLoc, args);
            
            evaluator.run();
            evaluator.writeReport();
            evaluator.shutdown();
            
            System.out.println(STR_APP_NAME + " Execution completed in " + evaluator.getRunDuration());
            System.out.println("  Results stored at " + evaluator.getOutputFilePath().toAbsolutePath());
            System.exit(ExitCode.SUCCESS.getCode());
            
        } catch (DpGrpcException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Unable to connect to Query Service.");
            JalApplicationBase.terminateWithException(QueryRecoveryEvaluator.class, e, ExitCode.INITIALIZATION_EXCEPTION);
            
        } catch (ConfigurationException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - No time-series data requests in command line.");
            JalApplicationBase.terminateWithException(QueryRecoveryEvaluator.class, e, ExitCode.INTPUT_ARG_INVALID);
            
        } catch (UnsupportedOperationException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Output location not found in file system: " + strOutputLoc);
            JalApplicationBase.terminateWithException(QueryRecoveryEvaluator.class, e, ExitCode.OUTPUT_ARG_INVALID);
            
        } catch (FileNotFoundException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Unable to create output file.");
            JalApplicationBase.terminateWithException(QueryRecoveryEvaluator.class, e, ExitCode.OUTPUT_FAILURE);
            
        } catch (SecurityException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Unable to write to output file.");
            JalApplicationBase.terminateWithException(QueryRecoveryEvaluator.class, e, ExitCode.OUTPUT_FAILURE);
            
        } catch (InvalidRequestStateException e) {
            System.err.println(STR_APP_NAME + " execution FAILURE - Application already executed.");
            JalApplicationBase.terminateWithException(QueryRecoveryEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
            
//        } catch (DpQueryException e) {
//            System.err.println(STR_APP_NAME + " data recovery ERROR - General gRPC error in data recovery.");
//            JalApplicationBase.terminateWithException(QueryRecoveryEvaluator.class, e, ExitCode.GRPC_EXCEPTION);
            
        } catch (IllegalStateException e) {
            System.err.println(STR_APP_NAME + " data recovery ERROR - Message request attempt on empty buffer.");
            JalApplicationBase.terminateWithException(QueryRecoveryEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
            
        } catch (InterruptedException e) {
            System.err.println(STR_APP_NAME + " data recovery ERROR - Process interrupted while waiting for buffer message.");
            JalApplicationBase.terminateWithException(QueryRecoveryEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
            
//        } catch (BufferUnderflowException e) {
//            System.err.println(STR_APP_NAME + " data recovery ERROR - message buffer not fully drained.");
//            JalApplicationBase.terminateWithException(QueryRecoveryEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
//            
//        } catch (IllegalArgumentException e) {
//            System.err.println(STR_APP_NAME + " evaluation ERROR - invalid timestamps encountered in recovered data.");
//            JalApplicationBase.terminateWithException(QueryRecoveryEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
//            
//        } catch (CompletionException e) {
//            System.err.println(STR_APP_NAME + " evaluation ERROR - data bucket insertion task failed.");
//            JalApplicationBase.terminateWithException(QueryRecoveryEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
        }
    }

    
    //
    // Application Resources
    //
    
    /** Default JAL tools configuration parameters */
    private static final JalToolsConfig     CFG_TOOLS = JalToolsConfig.getInstance();
    
    
    //
    // Application Constants - Command-Line Arguments and Messages
    //
    
    /** Minimum number of application arguments - argument name and at least one data request */
    public static final int         CNT_APP_MIN_ARGS = 2;

    
    /** Default output path location */
    public static final String      STR_OUTPUT_DEF = CFG_TOOLS.output.path + "/query/recovery";

    
    /** Argument variable name identifying supplemental PV names for data request */
    public static final String      STR_VAR_SUPPL_PVS = "--pvs";
    
    
    /** Argument variable name for enabling/disabling request decomposition for multi-streaming */
    public static final String      STR_VAR_DCMP_ENABLE = "--dcmp";
    
    /** Argument variable name for enabling/disabling automatic request decomposition */
    public static final String      STR_VAR_DCMP_AUTO = "--dcmp_auto";
    
    /** Argument variable name identifying data request decomposition types */
    public static final String      STR_VAR_DCMP_TYPES = "--dcmp_types";
    
    /** Argument variable name identifying data request decomposition maximum PV count */
    public static final String      STR_VAR_DCMP_MAX_PVS = "--dcmp_maxpvs";
    
    /** Argument variable name identifying data request decomposition maximum time range duration */
    public static final String      STR_VAR_DCMP_MAX_RNG = "--dcmp_maxrng";
    
    
    /** Argument variable name identifying gRPC stream types used for raw data recovery */
    public static final String      STR_VAR_STRM_TYPES = "--strm_types";

    
//    /** Argument variable name for enabling/disabling gRPC multi-streaming for raw data recovery */
//    public static final String      STR_VAR_MSTRM_ENABLE = "--mstrm";
    
    /** Argument variable name identifying request domain size triggering gRPC multi-streaming for raw data recovery */
    public static final String      STR_VAR_MSTRM_DOM_SZ = "--mstrm_domsz";
    
    /** Argument variable name identifying maximum number of gRPC streams for raw data recovery*/
    public static final String      STR_VAR_MSTRM_MAX_CNTS = "--mstrm_cnts";
    
    
    /** Argument variable name enabling/disabling raw data correlation while recovering data */
    public static final String      STR_VAR_CORR_RCVRY = "--corr_rcvry";
    
    /** Argument variable name enabling/disabling raw data correlation multi-threaded (concurrent) processing */
    public static final String      STR_VAR_CORR_CONC = "--corr_conc";
    
    /** Argument variable name identifying raw data correlation concurrency target set pivot size */
    public static final String      STR_VAR_CORR_PIVOT_SZ = "--corr_pvtsz";
    
    /** Argument variable name identifying raw data correlation muli-threaded (concurrent) processing maximum thread count */
    public static final String      STR_VAR_CORR_MAX_THRDS = "--corr_maxthrds";
    
    
    /** Argument variable name identifying output location */
    public static final String      STR_VAR_OUTPUT = "--output";

    /** List of all the valid argument delimiters */
    public static final List<String>    LST_STR_DELIMS = List.of(
            STR_VAR_SUPPL_PVS,
            STR_VAR_DCMP_ENABLE,
            STR_VAR_DCMP_AUTO,
            STR_VAR_DCMP_TYPES,
            STR_VAR_DCMP_MAX_PVS,
            STR_VAR_DCMP_MAX_RNG,
            STR_VAR_STRM_TYPES,
//            STR_VAR_MSTRM_ENABLE,
            STR_VAR_MSTRM_DOM_SZ,
            STR_VAR_MSTRM_MAX_CNTS, 
            STR_VAR_CORR_RCVRY,
            STR_VAR_CORR_CONC,
            STR_VAR_CORR_PIVOT_SZ,
            STR_VAR_CORR_MAX_THRDS,
            STR_VAR_OUTPUT
            );
    
    
    //
    // Application Constants - Client Messages
    //
    
    /** Application name */
    public static final String      STR_APP_NAME = QueryRecoveryEvaluator.class.getSimpleName();
    
    /** A laconic description of the application function */
    public static final String      STR_APP_DESCR = 
            STR_APP_NAME + " Description \n"
          + "- Application evaluates the performance and operation of the QueryRequestRecoverer class \n"
          + "    for recovering and correlating raw time-series data. \n"
          + "- Data Platform Test Archive requests are performed, the data is recovered \n"
          + "    as gRPC messages and correlated into raw data blocks associated with a \n"
          + "    common timestamp message.  \n"
          + "    Thus, the Data Platform archive must be populated with the Test Archive data \n"
          + "    supplied by application 'app-run-test-data-generator'. \n"
          + "- Note the grouping of configuration parameters for the test conditions, specifically, \n"
          + "    by request decomposition, by gRPC stream type and configuration, and by correlator \n"
          + "    configuration. \n"
          + "    Each configuration is scored by performance (data rate) in the application output. \n"
          + "- Appropriate strategy is to hold one configuration parameter set constant then vary the \n"
          + "    parameters of other configuration parameter sets. \n"
          + "- The operation is monitored, results are computed and stored in the output file.\n";
    
    /** The "usage" message for client help requests or invalid application arguments */
    public static final String      STR_LINE_BREAK = "\n                        "; 
    public static final String      STR_APP_USAGE = 
            STR_APP_NAME  + " Usage: \n"
          + "\n"
          + "% " + STR_APP_NAME
          + " [" + STR_VAR_HELP + "]"
          + " [" + STR_VAR_VERSION + "]"
          + " R1 [ ... Rn]"
          + " [" + STR_VAR_SUPPL_PVS + " PV1 ... PVn]"
          + STR_LINE_BREAK
          + " [" + STR_VAR_DCMP_ENABLE + " FALSE ... TRUE]"
          + " [" + STR_VAR_DCMP_AUTO + " FALSE ... TRUE]"
          + " [" + STR_VAR_DCMP_TYPES + " D1 ... Dn]"
          + " [" + STR_VAR_DCMP_MAX_PVS + " M1 ... Mn]"
          + " [" + STR_VAR_DCMP_MAX_RNG + " R1 ... Rn]"
          + STR_LINE_BREAK
          + " [" + STR_VAR_STRM_TYPES + " S1 ... Sn]"
          + STR_LINE_BREAK
//          + " [" + STR_VAR_MSTRM_ENABLE + " FALSE TRUE]"
          + " [" + STR_VAR_MSTRM_DOM_SZ + " Q1 ... Qn]"
          + " [" + STR_VAR_MSTRM_MAX_CNTS + " N1 ... Nn]"
          + STR_LINE_BREAK
          + " [" + STR_VAR_CORR_RCVRY + " FALSE ... TRUE]"
          + " [" + STR_VAR_CORR_CONC + " FALSE ... TRUE]"
          + " [" + STR_VAR_CORR_PIVOT_SZ + " P1 ... Pn]"
          + " [" + STR_VAR_CORR_MAX_THRDS + " T1 ... Tp]"
          + STR_LINE_BREAK
          + " [" + STR_VAR_OUTPUT +" Output] \n"
          + "\n" 
          + "  Where  \n"
          + "    " + STR_VAR_HELP + "        = prints this message and application description.\n"
          + "    " + STR_VAR_VERSION + "     = prints application version information and return.\n"
          + "    R1, ..., Rn   = Test request(s) to perform - TestArchiveRequest enumeration name(s). \n"
          + "    PV1, ..., PVn = Supplemental PV names to be added to requests R1 through Rn. \n"
          + "    " + STR_VAR_DCMP_ENABLE + "        = Enable/disable time-series data request decomposition {FALSE, TRUE}. \n"
          + "    " + STR_VAR_DCMP_AUTO + "   = Enable/disable time-series data request automatic decomposition {FALSE, TRUE}. \n"
          + "    D1, ..., Dn   = Request decomposition strategy(ies) - RequestDecompType enumeration name(s) for explicit deomposition {HORIZONTAL, VERTICAL, GRID}. \n"
          + "    M1, ..., Mn   = Maximum number of PVs per composite request in automatic decomposition - Integer value(s). \n"
          + "    R1, ..., Rn   = Maximum time range per composite request in automatic - 'PnDTnHnMd.dS' duration format(s). \n"
          + "    S1, ..., Sn   = gRPC stream type(s) - DpGrpcStreamType enumeration name(s) {BACKWARD, BIDRECTIONAL}. \n"
//          + "   " + STR_VAR_MSTRM_ENABLE + "         = Enable/disable multiple gRPC data streaming in raw data recovery {FALSE, TRUE}. \n"
          + "    Q1, ..., Qn   = Request domain size(s) (in PVs-Seconds) triggering gRPC multi-streaming. \n"
          + "    N1, ..., Nn   = gRPC stream count(s) peforming request recovery - Integer value(s). \n"
          + "   " + STR_VAR_CORR_RCVRY + "   = Enable/disable raw data correlation during data recovery {FALSE, TRUE}. \n"
          + "   " + STR_VAR_CORR_CONC + "    = Enable/disable multi-threaded (concurrent) raw data correlation {FALSE, TRUE}. \n"
          + "   P1, ..., Pn    = Raw data target set size(s) (block count) triggering concurrency - Long value(s). \n"
          + "   T1, ..., Tn    = Maximum thread count(s) for concurrent raw data correlation - Integer value(s). \n"
          + "    " + STR_VAR_OUTPUT + "      = output directory w/wout file path, or '" + STR_ARG_VAL_STDOUT + "'. \n"
          + "\n"
          + "  NOTES: \n"
          + "  - At least one TestArchiveRequest enumeration is required (i.e., R1). \n"
          + "  - All bracketed quantities [...] are optional. \n"
          + "  - PV1, ..., PVn values are strictly optional - if not present R1 through Rn are unmodified. \n"
          + "  - The default values for all other variables except " + STR_VAR_DCMP_AUTO + " are taken from the JAL configuration file. \n"
          + "  - Automatic request decomposition uses parameter M1, ..., Mn and R1, ..., Rn. \n"
          + "  - Explicit request decomposition (i.e., non-auto) uses parameters D1, ..., Dn and N1, ..., Nn. \n"
          + "  - The default value for " + STR_VAR_DCMP_AUTO + " is '" + QueryRecoveryTestSuiteCreator.BOL_DCMP_AUTO_ENABLED_DEF + "'. \n"
          + "  - The Boolean values FALSE and TRUE are not case sensitive. \n"
          + "  - Request decomposition enable " + STR_VAR_DCMP_ENABLE + " must be TRUE for gRPC multi-streaming. \n"
//          + "  - gRPC multi-streaming enable " + STR_VAR_MSTRM_ENABLE + " must be TRUE for gRPC multi-streaming. \n"
          + "  - Default " + STR_VAR_OUTPUT + " value is " + STR_OUTPUT_DEF + ". \n"
          + "  - If a file name is not included in the output path, a unique file name is generated for the output. \n" ;

    
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
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLogger(QueryRecoveryEvaluator.class, JalQueryAppBase.STR_LOGGING_LEVEL);

    
    // 
    // Defining Attributes
    //
    
    /** The (pre-configured) test suite creator used for <code>QueryRecoveryTestCase</code> generation */
    private final QueryRecoveryTestSuiteCreator       cfgTestSuite;
    
    
    //
    // Instance Resources
    //
    
    /** The time-series data re(processor) under evaluation */
    private final QueryRequestRecoverer                 prcrRqstRcvry;
    
    /** The collection of test cases used in evaluations */
    private final Collection<QueryRecoveryTestCase>     setTestCases;
    
    /** The collection of test results recovered from evaluations */
    private final Collection<QueryRecoveryTestResult>   setTestResults;
    
    
    //
    // State Variables
    //
    
    /** Total duration of test suite evaluation */
    private Duration    durEval = Duration.ZERO;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>QueryRecoveryEvaluator</code> instance.
     * </p>
     *
     * @param cfgSuite      the pre-configured test suite generator 
     * @param strOutputLoc  path location for the output file, or "console"
     * @param args          the application command-line arguments.
     * 
     * @throws DpGrpcException  unable to establish connection to the Query Service (see message and cause)
     * @throws UnsupportedOperationException    the file path is not associated with the default file system
     * @throws FileNotFoundException            unable to create output file
     * @throws SecurityException                unable to write to output file
     * @throws ConfigurationException 
     */
    public QueryRecoveryEvaluator(QueryRecoveryTestSuiteCreator cfgSuite, String strOutputLoc, String... args) 
            throws DpGrpcException, ConfigurationException, UnsupportedOperationException, FileNotFoundException, SecurityException {
        super(QueryRecoveryEvaluator.class, args);
        
        this.cfgTestSuite = cfgSuite;
        

        // Create resources
        this.prcrRqstRcvry = QueryRequestRecoverer.from(super.connQuery);
        
        this.setTestCases = this.cfgTestSuite.createTestSuite();    // throws ConfigurationException
        this.setTestResults = new TreeSet<>(QueryRecoveryTestResult.descendingDataRateOrdering());
        
        
        // Create the output stream and attach Logger to it - records fatal errors to output file
        super.openOutputStream(strOutputLoc); // throws SecurityException, FileNotFoundException, UnsupportedOperationException
        
        OutputStreamAppender    appAppErrs = Log4j.createOutputStreamAppender(STR_APP_NAME, super.psOutput);
        Log4j.attachAppender(LOGGER, appAppErrs);
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
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Runs the application evaluating all test cases within the test suite configuration.
     * </p>
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
        for (QueryRecoveryTestCase recTestCase : this.setTestCases) {
            QueryRecoveryTestResult recTestResult = recTestCase.evaluate(this.prcrRqstRcvry); 
            
            this.setTestResults.add(recTestResult);
        }
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
        ps.println("Evaluation duration  : " + this.durEval);
        ps.println("Evaluation completed : " + this.bolCompleted);
        ps.println();
        
        // Print out raw data processor configuration
        ps.println("Final QueryRequestRecoverer Configuration (last case)");
        this.prcrRqstRcvry.printOutConfig(ps, "  ");
        ps.println();
        
        // Print out the test suite configuration
        ps.println("Test Suite Configuration");
        this.cfgTestSuite.printOut(ps, "  ");
        ps.println();
        
        // Print out results summary
        QueryRecoveryTestsSummary.assignTargetDataRate(DBL_RATE_TARGET);
        QueryRecoveryTestsSummary  recSummary = QueryRecoveryTestsSummary.summarize(this.setTestResults);
        recSummary.printOut(ps, null);
        ps.println();
        
        // Print out request decomposition configuration scoring
        ps.println("Request Decomposition Configuration Scoring");
        RequestDecompConfigScorer   dcmpScorer = RequestDecompConfigScorer.from(this.setTestResults);
        dcmpScorer.printOutByRates(ps, "  ");
        ps.println();
        
        // Print out gRPC stream configuration scoring
        ps.println("gRPC Stream Configuration Scoring");
        GrpcStreamConfigScorer  strmScorer = GrpcStreamConfigScorer.from(this.setTestResults);
        strmScorer.printOutByRates(ps, "  ");
        ps.println();
        
        // Print out each test result
        ps.println("Individual Case Results:");
        for (QueryRecoveryTestResult recCase : this.setTestResults) {
            recCase.printOut(ps, "  ");
            ps.println();
        }
    }    

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Parse the application command-line argument for the test suite configuration and returns it.
     * </p>
     * <p>
     * The method parse the given collection of arguments for the following information in order:
     * <ol>
     * <li>Test Requests - commands identified by <code>{@link TestArchiveRequest}</code> enumeration constants. </li>
     * <li>Maximum Thread Counts - values identified by the variable delimiter {@value #STR_VAR_RQST_DCMP}. </li> 
     * <li>Concurrency Pivot Sizes - value identified by the variable delimiter {@value #STR_VAR_MSTRM_MAX_CNTS}. </li>
     * </ol> 
     * <p>
     * The application command line is first parsed for "commands", which are the <code>{@link TestArchiveRequest}</code>
     * enumeration constants describing the time-series data requests used in the return test suite.
     * These arguments must be the first to appear on the command line and do not have delimiters.  
     * At least one request is required for valid test suite creation, otherwise an exception is thrown.  
     * If a test request has an invalid <code>{@link TestArchiveRequest}</code> name an exception is thrown.    
     * </p>
     * <p>
     * The remaining arguments are optional; a <code>QueryRecoveryTestSuiteCreator</code> instance has default values that
     * will be supplied if any are missing.  Note that all values for variables {@value #STR_VAR_RQST_DCMP} and 
     * {@value #STR_VAR_MSTRM_MAX_CNTS} are <code>Integer</code> valued and must be parse as such.  If an error occurs
     * while parse the integer value an exception is thrown.
     * </p>
     *  
     * @param args  the application command-line argument collection
     * 
     * @return  the test suite configuration as described by the command-line arguments
     * 
     * @throws MissingResourceException no time-series data request (<code>TestArchiveRequest</code> constants) were found
     * @throws IllegalArgumentException invalid test request enumeration constant name (not in <code>TestArchiveRequest</code>)
     * @throws NumberFormatException    an invalid numeric argument was encounter (could not be converted to a <code>Integer</code> type)
     * @throws DateTimeParseException   an invalid time duration format was encountered ('PnDTnHnMd.dS')
     */
    private static QueryRecoveryTestSuiteCreator    parseTestSuiteConfig(String[] args) 
            throws MissingResourceException, IllegalArgumentException, NumberFormatException, DateTimeParseException {
     
        //
        // --- Test Requests ---
        //
        
        // Parse the data requests 
        List<String>    lstRqstNms = JalApplicationBase.parseAppArgsCommands(args);
        if (lstRqstNms.isEmpty()) {
            String  strMsg = "The command-line arguments contained no time-series data requests. Use --help command.";

            throw new MissingResourceException(strMsg, QueryRecoveryEvaluator.class.getSimpleName(), TestArchiveRequest.class.getName());
        }
        
        // Convert to TestArchiveRequest enumeration constants
        List<TestArchiveRequest>    lstRqsts = lstRqstNms.stream()
                .<TestArchiveRequest>map(strNm -> TestArchiveRequest.valueOf(TestArchiveRequest.class, strNm))    // throws IllegalArgumentException
                .toList();
        
        // Parse any supplemental PV names
        List<String>    lstSupplPvs = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_SUPPL_PVS);
        
        
        //
        // --- Request Decomposition ---
        //
        
        // Parse the request decomposition enable/disable values and convert to Boolean if present
        List<String>    lstStrDcmps = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_DCMP_ENABLE);
        List<Boolean>   lstBolDcmps = lstStrDcmps.stream()
                .<Boolean>map(str -> Boolean.parseBoolean(str))     // no exception - defaults to 'false'
                .toList();
        
        // Parse the automatic request decomposition flags if present
        List<String>    lstStrDcmpAutos = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_DCMP_AUTO);
        List<Boolean>   lstBolDcmpAutos = lstStrDcmpAutos.stream()
                .<Boolean>map(str -> Boolean.parseBoolean(str))     // no exception - defaults to 'false'
                .toList();
        
        // Parse the request decomposition types and convert to enumeration constants if present
        List<String>            lstStrDcmpTypes = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_DCMP_TYPES);
        List<RequestDecompType> lstEnmDcmpTypes = lstStrDcmpTypes.stream()
                .<RequestDecompType>map(str -> RequestDecompType.valueOf(RequestDecompType.class, str))
                .toList();
        
        // Parse the decomposition maximum composite request PV count and convert to Integer if present
        List<String>    lstStrDcmpMaxPvs = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_DCMP_MAX_PVS);
        List<Integer>   lstIntDcmpMaxPvs = lstStrDcmpMaxPvs.stream()
                .<Integer>map(str -> Integer.parseInt(str))     // throws NumberFormatException
                .toList();
        
        // Parse the decomposition maximum composite request time range and convert to Duration if present
        List<String>    lstStrDcmpMaxRng = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_DCMP_MAX_RNG);
        List<Duration>  lstDurDcmpMaxRng = lstStrDcmpMaxRng.stream()
                .<Duration>map(str -> Duration.parse(str))      // throws DateTimeParseException
                .toList();
        
        
        //
        // --- Request Recovery ---
        //
        
//        // Parse the gRPC multi-streaming enable/disable values and convert to Boolean if present
//        List<String>    lstStrStrm = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_MSTRM_ENABLE);
//        List<Boolean>   lstBolStrm = lstStrStrm.stream()
//                .<Boolean>map(str -> Boolean.parseBoolean(str))
//                .toList();
        
        // Parse the gRPC stream types used for data recovery then convert to DpGrpcStreamType enumeration if present
        List<String>            lstStrStrmTypes = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_STRM_TYPES);
        List<DpGrpcStreamType>  lstEnmStrmTypes = lstStrStrmTypes.stream()
                .<DpGrpcStreamType>map(str -> DpGrpcStreamType.valueOf(DpGrpcStreamType.class, str))    // throws IllegalArgumentException
                .toList();
        
        
        // Parse the request domain sizes triggering gRPC multi-streaming and convert to Long if present
        List<String>    lstStrMStrmDomSzs = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_MSTRM_DOM_SZ);
        List<Long>      lstLngMStrmDomSzs = lstStrMStrmDomSzs.stream()
                .<Long>map(str -> Long.parseLong(str))                // throws NumberFormatException
                .toList();
        
        // Parse the maximum number of gRPC streams allowed in time-series request data recovery
        List<String>    lstStrMStrmMaxCnts = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_MSTRM_MAX_CNTS);
        List<Integer>   lstIntMStrmMaxCnts = lstStrMStrmMaxCnts.stream()
                .<Integer>map(str -> Integer.parseInt(str))         // throws NumberFormatException
                .toList();
        
        
        //
        // --- Raw Data Correlation ---
        //
        
        // Check for raw data correlation during recovery and convert to Boolean if present
        List<String>    lstStrCorrRcvry = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_CORR_RCVRY);
        List<Boolean>   lstBolCorrRcvry = lstStrCorrRcvry.stream()
                .<Boolean>map(str -> Boolean.parseBoolean(str))     // no exception - defaults to 'false'
                .toList();
        
        // Check for raw data correlation using and multi-threaded (concurrency) and convert to Boolean if present
        List<String>    lstStrCorrConc = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_CORR_CONC);
        List<Boolean>   lstBolCorrConc = lstStrCorrConc.stream()
                .<Boolean>map(str -> Boolean.parseBoolean(str))     // no exception - defaults to 'false'
                .toList();
        
        
        // Parse the raw data correlation maximum concurrent thread counts and convert to integers
        List<String>    lstStrCorrMaxThrds = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_CORR_MAX_THRDS);
        List<Integer>   lstIntCorrMaxThrds = lstStrCorrMaxThrds.stream()
                .<Integer>map(strNum -> Integer.valueOf(strNum))    // throws NumberFormatException
                .toList();
        
        // Parse the raw data correlation correlated set sizes triggering (pivoting to) concurrency and convert to integers
        List<String>    lstStrPivotSzs = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_CORR_PIVOT_SZ);
        List<Integer>   lstIntCorrPivotSzs = lstStrPivotSzs.stream()
                .<Integer>map(strNum -> Integer.valueOf(strNum))    // throws NumberFormatException
                .toList();
        
        
        // Build the test suite generator and return it
        QueryRecoveryTestSuiteCreator suite = QueryRecoveryTestSuiteCreator.create();
        
        suite.addTestRequests(lstRqsts);
        suite.addSupplementalPvs(lstSupplPvs);
        
        suite.addRequestDecompEnables(lstBolDcmps);
        suite.addRequestDecompAutoEnables(lstBolDcmpAutos);
        suite.addRequestDecompStrategies(lstEnmDcmpTypes);
        suite.addRequestDecompMaxPvCnts(lstIntDcmpMaxPvs);
        suite.addRequestDecompMaxTimeRanges(lstDurDcmpMaxRng);
        
        suite.addGrpcStreamTypes(lstEnmStrmTypes);
        
        suite.addMultiStreamRequestDomainSizes(lstLngMStrmDomSzs);
        suite.addMultiStreamMaxStreamCounts(lstIntMStrmMaxCnts);
        
        suite.addCorrelConcurrentEnables(lstBolCorrConc);
        suite.addCorrelDuringRecoveryEnables(lstBolCorrRcvry);
        suite.addCorrelConcurrencyPivotSizes(lstIntCorrPivotSzs);
        suite.addCorrelMaxThreadCounts(lstIntCorrMaxThrds);
        
        return suite;
    }

}
