/*
 * Project: dp-api-common
 * File:	DataCorrelationEvaluator.java
 * Package: com.ospreydcs.dp.jal.tools.query.correl
 * Type: 	DataCorrelationEvaluator
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
 * @since May 27, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.correl;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.BufferUnderflowException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;

import com.ospreydcs.dp.api.app.ExitCode;
import com.ospreydcs.dp.api.app.JalApplicationBase;
import com.ospreydcs.dp.api.app.JalQueryAppBase;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator;
import com.ospreydcs.dp.api.util.JalEnv;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.query.request.TestArchiveRequest;
import com.sun.jdi.request.InvalidRequestStateException;

/**
 * <p>
 * Tool application for evaluating the operation and performance of Java API Library raw data correlation components for
 * the Query Service interface.
 * </p>
 * <p>
 * The application command-line argument collection describes the test suite to be run during execution.  The test
 * suite itself is given by a <code>{@link CorrelatorTestSuite}</code> instance, which is created from the 
 * command-line arguments via the <code>{@link #parseTestSuiteConfig(String[])}</code>.  Once the test suite
 * configuration is recovered, an instance of <code>DataCorrelationEvaluator</code> is create, executed, and
 * the report generated.
 * </p>
 * <p>
 * <h2>Command Line Arguments</h2>
 * <p>
 * The application expects the given collection of arguments (ordering is irrelevant):
 * <ol>
 * <li>Test Requests - commands identified by <code>{@link TestArchiveRequest}</code> enumeration constants. </li>
 * <li>Maximum Thread Counts - values identified by the variable delimiter {@value #STR_VAR_RQST_DCMP}. </li> 
 * <li>Concurrency Pivot Sizes - value identified by the variable delimiter {@value #STR_VAR_STRM_CNT}. </li>
 * <li>Output Location - an optional path or file identified by the variable delimiter {@value #STR_VAR_OUTPUT}. </li>
 * </ol> 
 * Additionally, the following "commands" may be provided:
 * <ul>
 * <li>{@value JalApplicationBase#STR_ARG_HELP} - responses with a the <code>{@link #STR_APP_USAGE}</code> message.</li>
 * <li>{@value JalApplicationBase#STR_ARG_VERSION} - response with the <code>{@link #STR_APP_VERSION}</code> message.</li>
 * </ul>
 * The above arguments may appear anywhere on the command line but take precedence over all other arguments.
 * </p>
 * The full command line for the application appears as follows:
 * <pre>
 * <code>
 * >DataCorrelationEvaluator R1 [... RN] [{@value #STR_VAR_PVS} M1 ...Mi] [{@value #STR_VAR_TYPE} P1 ...Pj] [{@value #STR_VAR_OUTPUT} output]
 * </code>
 * </pre>
 * where 
 * <ul>
 * <li><code>R1...RN</code> = name(s) of a test request enumeration <code>{@link TestArchiveRequest}</code>.</li>
 * <li><code>M1...Mi</code> = number of concurrent processing thread(s).</code>.</li>
 * <li><code>P1...Pj</code> = target size(s) triggering concurrent processing. </li>
 * <li><code>output</code> = location of output file, or string "console" - evaluation output is sent directly to console. 
 * </ul>
 * If the <code>output</code> argument is not provided, the output of the evaluation is created in
 * the default path specified by class constant <code>{@link #STR_OUTPUT_DEF}</code>. 
 * </p>
 * <p>
 * The application command line is first parsed for "commands", which are the <code>{@link TestArchiveRequest}</code>
 * enumeration constants describing the time-series data requests used in the return test suite.
 * These arguments must be the first to appear on the command line and do not have delimiters.  
 * At least one request is required for valid test suite creation, otherwise an exception is thrown and execution fails  
 * If a test request has an invalid <code>{@link TestArchiveRequest}</code> name an exception is thrown and execution fails.    
 * </p>
 * <p>
 * The remaining arguments are optional; a <code>CorrelatorTestSuite</code> instance has default values that
 * will be supplied if any are missing.  Note that all values for variables {@value #STR_VAR_THREADS} and 
 * {@value #STR_VAR_TYPE} are <code>Integer</code> valued and must be parse as such.  If an error occurs
 * while parse the integer value an exception is thrown.
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 27, 2025
 *
 */
public final class DataCorrelationEvaluator extends JalQueryAppBase<DataCorrelationEvaluator> {
    
    
    //
    // Application Entry
    //

    /**
     * <p>
     * Entry point for the application.
     * </p>
     * 
     * @param args  command-line arguments as described in <code>{@link DataCorrelationEvaluator}</code>
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
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.INPUT_CFG_CORRUPT);

        }
        
        // Get the test suite configuration and output location from the application arguments
        String                  strOutputLoc;
        CorrelatorTestSuite     suite;
        try {
            
            suite = DataCorrelationEvaluator.parseTestSuiteConfig(args);
            strOutputLoc = DataCorrelationEvaluator.parseOutputLocation(args);
            
        } catch (Exception e) {
            
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.INTPUT_ARG_INVALID);
            return;
        }

        
        //
        // ------- Application Execution -------
        //
        
        // Create the correlation evaluator and run it
        try {
            DataCorrelationEvaluator    evaluator = new DataCorrelationEvaluator(suite, strOutputLoc);
            
            evaluator.run();
            evaluator.writeReport();
            evaluator.shutdown();
            
            System.out.println(STR_APP_NAME + " Execution completed in " + evaluator.getRunDuration());
            System.out.println("  Results stored at " + evaluator.getOutputFilePath().toAbsolutePath());
            System.exit(ExitCode.SUCCESS.getCode());
            
        } catch (DpGrpcException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Unable to connect to Query Service.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.INITIALIZATION_EXCEPTION);
            
        } catch (ConfigurationException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - No time-series data requests in command line.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.INTPUT_ARG_INVALID);
            
        } catch (UnsupportedOperationException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Output location invalid: " + strOutputLoc);
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.OUTPUT_ARG_INVALID);
            
        } catch (FileNotFoundException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Unable to create output file.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.OUTPUT_FAILURE);
            
        } catch (SecurityException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Unable to write to output file.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.OUTPUT_FAILURE);
            
        } catch (InvalidRequestStateException e) {
            System.err.println(STR_APP_NAME + " execution FAILURE - Application already executed.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
            
        } catch (DpQueryException e) {
            System.err.println(STR_APP_NAME + " data recovery ERROR - General gRPC error in data recovery.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.GRPC_EXCEPTION);
            
        } catch (IllegalStateException e) {
            System.err.println(STR_APP_NAME + " data recovery ERROR - Message request attempt on empty buffer.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
            
        } catch (InterruptedException e) {
            System.err.println(STR_APP_NAME + " data recovery ERROR - Process interrupted while waiting for buffer message.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
            
        } catch (BufferUnderflowException e) {
            System.err.println(STR_APP_NAME + " data recovery ERROR - message buffer not fully drained.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
            
        } catch (IllegalArgumentException e) {
            System.err.println(STR_APP_NAME + " evaluation ERROR - invalid timestamps encountered in recovered data.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
            
        } catch (CompletionException e) {
            System.err.println(STR_APP_NAME + " evaluation ERROR - data bucket insertion task failed.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
        }
    }
    
    
    //
    // Application Resources
    //
    
    /** Default configuration parameters for the Query Service tools */
//    private static final JalToolsQueryConfig    CFG_QUERY = JalToolsConfig.getInstance().query;
    private static final DpQueryConfig      CFG_QUERY = DpApiConfig.getInstance().query;
    
    /** Default configuration parameters for the JAL Tools */
    private static final JalToolsConfig     CFG_TOOLS = JalToolsConfig.getInstance();
    
    //
    // Application Constants - Command-Line Arguments and Messages
    //
    
    /** Minimum number of application arguments - argument name and at least one data request */
    public static final int         CNT_APP_MIN_ARGS = 2;
    
    
    /** Default output path location */
//  public static final String      STR_OUTPUT_DEF = CFG_QUERY.output.correl.path;
    public static final String      STR_OUTPUT_DEF = CFG_TOOLS.output + "/query/correl";
  

    /** Argument flag identifying a maximum thread count value */
    public static final String      STR_VAR_THRDS = "--threads";
    
    /** Argument flag identifying a concurrency pivot size */
    public static final String      STR_VAR_PIVOT = "--pivot";
    
    /** Argument flag identifying output location */
    public static final String      STR_VAR_OUTPUT = "--output";

    /** List of all the valid argument delimiters */
    public static final List<String>    LST_STR_DELIMS = List.of(
            STR_VAR_THRDS, 
            STR_VAR_PIVOT, 
            STR_VAR_OUTPUT
            );
    
    
    //
    // Application Constants - Client Messages
    //
    
    /** Application name */
    public static final String      STR_APP_NAME = DataCorrelationEvaluator.class.getSimpleName();
    
    
    /** The "usage" message for client help requests or invalid application arguments */
    public static final String      STR_APP_USAGE = 
            STR_APP_NAME  + " Usage: \n"
          + "\n"
          + "% " + STR_APP_NAME
          + " [" + STR_ARG_HELP + "]"
          + " [" + STR_ARG_VERSION + "]"
          + " R1 [ ... RN]"
          + " [" + STR_VAR_THRDS + " M1 ... Mj]"
          + " [" + STR_VAR_PIVOT + " P1 ... Pk]"
          + " [" + STR_VAR_OUTPUT +" Output]"
          + "\n" 
          + "  Where  \n"
          + "    " + STR_ARG_HELP + "      = print this message and return.\n"
          + "    " + STR_ARG_VERSION + "   = prints application version information and return.\n"
          + "    R1, ..., Rn = Test request(s) to perform - TestArchiveRequest enumeration name(s). \n"
          + "    M1, ..., Mj = Maximum allowable number(s) of concurrent processing threads - Integer value(s). \n"
          + "    P1, ..., Pk = Pivot size(s) triggering concurrent processing - Integer value(s). \n"
          + "    Output      = output directory w/wout file path, or '" + STR_ARG_VAL_STDOUT + "'. \n"
          + "\n"
          + "  NOTES: \n"
          + "  - All bracketed quantities [...] are optional. \n"
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
    private static final Logger     LOGGER = Log4j.getLogger(DataCorrelationEvaluator.class, JalQueryAppBase.STR_LOGGING_LEVEL);

    
    // 
    // Defining Attributes
    //
    
    /** The test suite used for <code>QueryChannel</code> evaluations */
    private final CorrelatorTestSuite       suiteEvals;
    
    
    //
    // Instance Resources
    //
    
    /** The data correlator (processor) under evaluation */
    private final RawDataCorrelator     toolCorrelator;
    
    /** The collection of test cases used in evaluations */
    private final Map<TestArchiveRequest,List<CorrelatorTestCase>>  mapCases;
    
    /** The collection of test results recovered from evaluations */
    private final Collection<CorrelatorTestResult>                  setResults;
    
    
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
     * Constructs a new <code>DataCorrelationEvaluator</code> instance for the given test suite 
     * configuration and output location.
     * </p>
     * 
     * @param   suiteEvals      the test suite configuration 
     * @param   strOutputLoc    output path for evaluation results
     * 
     * @throws DpGrpcException                  unable to connect to the Query Service (see message and cause) 
     * @throws ConfigurationException           there are no time-series data request in the given test suite configuration
     * @throws UnsupportedOperationException    the output path was invalid (not supported on the file system)
     * @throws FileNotFoundException            unable to create the output file (see message and cause)
     * @throws SecurityException                unable to write to output file
     */
    public DataCorrelationEvaluator(CorrelatorTestSuite suiteEvals, String strOutputLoc) 
            throws DpGrpcException, ConfigurationException, UnsupportedOperationException, FileNotFoundException, SecurityException {
        super(DataCorrelationEvaluator.class);
        
        this.suiteEvals = suiteEvals;
        
        // Create the correlator
        this.toolCorrelator = RawDataCorrelator.create();
        this.toolCorrelator.setConcurrencyPivotSize(SZ_CONCURRENCY_PIVOT);
        this.toolCorrelator.enableConcurrency(BOL_CONCURRENCY);
        this.toolCorrelator.setMaxThreadCount(CNT_CONCURRENCY_THDS);
        
        // Create the collection of test cases and container for results
        this.mapCases = this.suiteEvals.createTestSuite();  // throws ConfigurationException
        this.setResults = new TreeSet<>(CorrelatorTestResult.descendingRateOrdering());
        
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
     * <p>
     * The evaluations proceed according to each <code>{@link TestArchiveRequest}</code> in the
     * <code>{@link #mapCases}</code> key set.  
     * <ol>
     * <li>The data for the test request is first recovered using <code>{@link #recoverRequestData(DpDataRequest)}</code></li>
     * <li>All test cases using that data are performed with <code>{@link CorrelatorTestCase#evaluate(RawDataCorrelator, List)}</code></li>
     * <li>All test case results are stored in <code>{@link #setResults}</code> for later evaluation.</li>
     * <li>The application state variables are updated.</li>
     * </ol>
     * </p>
     * 
     * @throws InvalidRequestStateException the application has already been run
     * @throws DpQueryException         general exception during test request data recovery (see message and cause)
     * @throws IllegalStateException    message request attempt on empty message buffer
     * @throws InterruptedException     processing interruption recovering request data in message data buffer
     * @throws BufferUnderflowException message buffer not fully drained
     * @throws IllegalArgumentException invalid timestamps were encountered in the recovered data set
     * @throws CompletionException      error in <code>DataBucket</code> insertion task (see cause)
     */
    public void run() throws InvalidRequestStateException, DpQueryException, IllegalStateException, InterruptedException, BufferUnderflowException, IllegalArgumentException, CompletionException {

        // Check state
        if (super.bolRun) {
            throw new InvalidRequestStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Application has already been run.");
        }
        super.bolRun = true;
        
        // Start counter
        System.out.print("Performing evaluations .");
        super.startExecutionTimer(1L, TimeUnit.SECONDS);

        // Iterate over each test request in the test case collection (within test suite configuration)
        Instant insStart = Instant.now();
        for (TestArchiveRequest enmRqst : this.mapCases.keySet()) {

            // Recover raw data for this request
            DpDataRequest               rqst = enmRqst.create();
            
            List<QueryData>             lstDataMsgs = super.recoverRequestData(rqst);
            
            // Perform all the test case evaluations for the test request
            List<CorrelatorTestCase>    lstCases = this.mapCases.get(enmRqst);
            
            this.toolCorrelator.reset();
            for (CorrelatorTestCase recCase : lstCases) {
                CorrelatorTestResult recResult = recCase.evaluate(this.toolCorrelator, lstDataMsgs);    // throws IllegalArgumentException, CompletionException
                
                this.setResults.add(recResult);
            }
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
     * of the <code>QueryChannel</code> evaluations including a summary, test suite configuration, and
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
     * of the <code>QueryChannel</code> evaluations including a summary, test suite configuration, and
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
        
        // Print out evaluation summary
        ps.println("Test cases specified : " + this.mapCases.values().stream().mapToInt(lst -> lst.size()).sum() );
        ps.println("Test cases run       : " + this.setResults.size());
        ps.println("Evaluation duration  : " + this.durEval);
        ps.println("Evaluation completed : " + this.bolCompleted);
        ps.println();
        
        // Print out the test suite configuration
        ps.println("Test Suite Configuration");
        this.suiteEvals.printOut(ps, "  ");
        ps.println();
        
        // Print out results summary
        CorrelatorTestResultSummary.assignTargetDataRate(DBL_RATE_TARGET);
        CorrelatorTestResultSummary  recSummary = CorrelatorTestResultSummary.summarize(this.setResults);
        recSummary.printOut(ps, null);
        ps.println();
        
        // Print out results extremes
        CorrelatorTestResultExtremes  recExtremes = CorrelatorTestResultExtremes.computeExtremes(this.setResults);
        recExtremes.printOut(ps, null);
        ps.println();
        
        // Print out each test result
        ps.println("Individual Case Results:");
        for (CorrelatorTestResult recCase : this.setResults) {
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
     * <li>Concurrency Pivot Sizes - value identified by the variable delimiter {@value #STR_VAR_STRM_CNT}. </li>
     * </ol> 
     * <p>
     * The application command line is first parsed for "commands", which are the <code>{@link TestArchiveRequest}</code>
     * enumeration constants describing the time-series data requests used in the return test suite.
     * These arguments must be the first to appear on the command line and do not have delimiters.  
     * At least one request is required for valid test suite creation, otherwise an exception is thrown.  
     * If a test request has an invalid <code>{@link TestArchiveRequest}</code> name an exception is thrown.    
     * </p>
     * <p>
     * The remaining arguments are optional; a <code>CorrelatorTestSuite</code> instance has default values that
     * will be supplied if any are missing.  Note that all values for variables {@value #STR_VAR_RQST_DCMP} and 
     * {@value #STR_VAR_STRM_CNT} are <code>Integer</code> valued and must be parse as such.  If an error occurs
     * while parse the integer value an exception is thrown.
     * </p>
     *  
     * @param args  the application command-line argument collection
     * 
     * @return  the test suite configuration as described by the command-line arguments
     * 
     * @throws ConfigurationException   no time-series data request (<code>TestArchiveRequest</code> constants) were found
     * @throws IllegalArgumentException invalid test request enumeration constant name (not in <code>TestArchiveRequest</code>)
     * @throws NumberFormatException    an invalid numeric argument was encounter (could not be converted to a <code>Integer</code> type)
     */
    private static CorrelatorTestSuite    parseTestSuiteConfig(String[] args) 
            throws ConfigurationException, IllegalArgumentException, NumberFormatException {
     
        // Parse the data requests 
        List<String>    lstRqstNms = JalApplicationBase.parseAppArgsCommands(args);
        if (lstRqstNms.isEmpty()) {
            String  strMsg = "The command-line arguments contained no time-series data requests. Use --help command.";

            throw new ConfigurationException(strMsg);
        }
        
        // Convert to TestArchiveRequest enumeration constants
        List<TestArchiveRequest>    lstRqsts = lstRqstNms.stream()
                .<TestArchiveRequest>map(strNm -> 
                    TestArchiveRequest.valueOf(TestArchiveRequest.class, strNm))    // throws IllegalArgumentException
                .toList();
        
        // Parse the maximum thread counts and convert to integers
        List<String>    lstStrThrdCnts = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_THRDS);
        List<Integer>   lstThrdCnts = lstStrThrdCnts.stream()
                .<Integer>map(strNum -> Integer.valueOf(strNum))                    // throws NumberFormatException
                .toList();
        
        // Parse the pivot sizes and convert to integers
        List<String>    lstStrSzPivots = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_PIVOT);
        List<Integer>   lstSzPivots = lstStrSzPivots.stream()
                .<Integer>map(strNum -> Integer.valueOf(strNum))
                .toList();
        
        // Build the test suite and return it
        CorrelatorTestSuite suite = CorrelatorTestSuite.create();
        
        suite.addTestRequests(lstRqsts);
        suite.addMaxThreadCounts(lstThrdCnts);
        suite.addConcurrencyPivotSizes(lstSzPivots);
        
        return suite;
    }

    /**
     * <p>
     * Parses the application command-line argument collection for the output location and return it.
     * </p>
     * <p>
     * The output location, as specified by the application client, is the value of variable
     * {@value #STR_VAR_OUTPUT}.  There is only one value for this variable and any additional values
     * are ignored.
     * </p>
     * <p>
     * If the variable {@value #STR_VAR_OUTPUT} is not present in the command line arguments, this is an
     * optional parameter, then the default value given by <code>{@link #STR_OUTPUT_DEF}</code> is returned.
     * This value is taken from the JAL default configuration.
     * </p>
     * 
     * @param args  the application command-line argument collection
     * 
     * @return  the output location as specified in the command line, 
     *          or value of <code>{@link #STR_OUTPUT_DEF}</code> if not present
     */
    private static String   parseOutputLocation(String[] args) {
        
        // Look for the output location on the command line
        List<String>    lstStrOutput = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_OUTPUT);
    
        // If there is no user-provided output location use the default value in the JAL configuration
        if (lstStrOutput.isEmpty()) {
            return STR_OUTPUT_DEF;
        }
    
        // Else return the first element in the list
        String strOutputLoc = lstStrOutput.get(0);
        
        return strOutputLoc;
    }
}
