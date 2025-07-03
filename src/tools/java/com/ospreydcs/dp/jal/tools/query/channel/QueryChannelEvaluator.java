/*
 * Project: dp-api-common
 * File:	QueryChannelEvaluator.java
 * Package: com.ospreydcs.dp.jal.tools.query.channel
 * Type: 	QueryChannelEvaluator
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
 * @since Jun 9, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.channel;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.module.ResolutionException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CompletionException;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;

import com.ospreydcs.dp.api.app.ExitCode;
import com.ospreydcs.dp.api.app.JalApplicationBase;
import com.ospreydcs.dp.api.app.JalQueryAppBase;
import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.query.correl.DataCorrelationEvaluator;
import com.ospreydcs.dp.jal.tools.query.request.TestArchiveRequest;

/**
 * <p>
 * Application for evaluating the performance of the <code>QueryChannel</code> class under test suites 
 * described in the argument.
 * </p>
 * <p>
 * The application command-line argument collection describes the test suite to be run during execution.  The test
 * suite itself is given by a <code>{@link QueryChannelTestSuite}</code> instance, which is created from the 
 * command-line arguments via the <code>{@link #parseTestSuiteConfig(String[])}</code>.  Once the test suite
 * configuration is recovered, an instance of <code>QueryChannelEvaluator</code> is create, executed, and
 * the report generated.
 * </p>
 * <p>
 * The application expects the given collection of arguments (ordering is irrelevant):
 * <ol>
 * <li>Test Request(s) - command(s) identified by <code>{@link TestArchiveRequest}</code> enumeration constants. </li>
 * <li>Data Request Decomposition Strategy(ies)- values identified by the variable delimiter {@value #STR_VAR_RQST_DCMP}. </li>
 * <li>gRPC Stream Type(s) - value(s) identified by the variable {@value #STR_VAR_STRM_TYPE}. </li>
 * <li>gRPC Stream Count(s) - value(s) identified by the variable {@value #STR_VAR_STRM_CNT}. </li>
 * <li>Output Location - an optional path or file identified by the variable {@value #STR_VAR_OUTPUT}. </li>
 * </ol> 
 * Additionally, the following "commands" may be provided:
 * <ul>
 * <li>{@value JalApplicationBase#STR_VAR_HELP} - responses with a the <code>{@link #STR_APP_USAGE}</code> message.</li>
 * <li>{@value JalApplicationBase#STR_VAR_VERSION} - response with the <code>{@link #STR_APP_VERSION}</code> message.</li>
 * </ul>
 * The above arguments may appear anywhere on the command line but take precedence over all other arguments.
 * </p>
 * <p>
 * There must be at least 1 command-line argument, a valid  <code>{@link TestArchiveRequest}</code> constant name.  
 * There must be at least one test request for a valid test suite configuration, there are default values for all
 * other parameters.
 * More specifically, the command line arguments for the application are as follows:
 * <pre>
 * <code>>
 * >QueryChannelEvaluator R1 [... RN] [{@value #STR_VAR_RQST_DCMP} D1 ...Di] [{@value #STR_VAR_STRM_TYPE} S1 ...Sj] [{@value #STR_VAR_STRM_CNT} N1 ...Nk] [{@value #STR_VAR_OUTPUT} output]
 * </code>
 * </pre>
 * where 
 * <ul>
 * <li><code>R1...RN</code> = name(s) of a test request enumeration <code>{@link TestArchiveRequest}</code>.</li>
 * <li><code>D1...Di</code> = name(s) of request decomposition enumeration <code>{@link RequestDecompType}</code></code>.</li>
 * <li><code>S1...Sj</code> = names(s) of gRPC stream type enumerations <code>{@link DpGrpcStreamType}</code>. </li>
 * <li><code>N1...Nk</code> = number(s) of gRPC streams for request data recovery.</li> 
 * <li><code>output</code> = location of output file, or string "console" - evaluation output is sent directly to console. 
 * </ul>
 * If the <code>output</code> argument is not provided, the output of the evaluation is created in
 * the default path specified by class constant <code>{@link #STR_OUTPUT_DEF}</code>. 
 * </p>
 * <p>
 * <h2>Test Suites</h2>
 * The <code>QueryChannelEvaluator</code> runs "test suites" described by the command-line argument
 * The command-line arguments are used to to instantiate a <code>{@link QueryChannelTestSuite}</code>
 * object which configures the test suite.  The <code>QueryChannelTestSuite</code> object generates collections of 
 * <code>{@link QueryChannelTestCase}</code> instances according to the configuration in application arguments.
 * Each <code>QueryChannelTestCase</code> object performs an evaluation of a test <code>QueryChannel</code> object
 * under the conditions of the test.
 * </p> 
 * <p>
 * <h2>Output</h2>
 * A new output file is created for each application invocation.  The file will be located in the path
 * given by the optional 2nd argument or the default location <code>{@link #STR_OUTPUT_DEF}</code> .  
 * The name of the file has the form
 * <pre>
 *   <code>QueryChannelEvaluator-[creation instant].txt</code>
 * </pre>
 * </p>
*
 * @author Christopher K. Allen
 * @since Jun 9, 2025
 *
 */
public class QueryChannelEvaluator extends JalQueryAppBase<QueryChannelEvaluator> {

    //
    // Application Entry
    //

    /**
     * <p>
     * Entry point for <code>QueryChannelEvaluator</code> application tool.
     * </p>
     * 
     * @param args  application command-line arguments as described in <code>{@link QueryChannelEvaluator}</code>
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
        QueryChannelTestSuite   suiteEvals;
        try {
            
            suiteEvals = QueryChannelEvaluator.parseTestSuiteConfig(args);
            strOutputLoc = QueryChannelEvaluator.parseOutputLocation(args);
            
        } catch (Exception e) {
            
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.INTPUT_ARG_INVALID);
            return;
        }

        
        //
        // ------- Application Execution -------
        //
        
        // Create the evaluator, run it while catching and reporting any exceptions
        try {
            QueryChannelEvaluator   evaluator = new QueryChannelEvaluator(suiteEvals, strOutputLoc, args);
            
            evaluator.run();
            evaluator.writeReport();
            evaluator.shutdown();
            
            System.out.println(STR_APP_NAME + " Execution completed in " + evaluator.getRunDuration());
            System.out.println("  Results stored at " + evaluator.getOutputFilePath().toAbsolutePath());
            System.exit(ExitCode.SUCCESS.getCode());
            
        } catch (ConfigurationException | UnsupportedOperationException | FileNotFoundException | SecurityException 
                | DpGrpcException e) {

            // Creation exception
            JalApplicationBase.terminateWithException(QueryChannelEvaluator.class, e, ExitCode.INITIALIZATION_EXCEPTION);
            return;
            
        } catch (IllegalStateException | CompletionException | ResolutionException | DpQueryException
                | InterruptedException e) {
            // Run, write, shut down exception
            JalApplicationBase.terminateWithException(QueryChannelEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
            return;
            
        }
    }
    
    //
    // Application Resources
    //
    
    /** Default configuration parameters for the Query Service tools */
//    private static final JalToolsQueryConfig    CFG_QUERY = JalToolsConfig.getInstance().query;
//    private static final DpQueryConfig      CFG_QUERY = DpApiConfig.getInstance().query;
    
    /** Default JAL tools configuration parameters */
    private static final JalToolsConfig     CFG_TOOLS = JalToolsConfig.getInstance();
    
    
    //
    // Application Constants - Command-Line Arguments and Messages
    //
    
    /** Minimum number of application arguments - argument name and at least one data request */
    public static final int         CNT_APP_MIN_ARGS = 2;

    
    /** Default output path location */
//  public static final String      STR_OUTPUT_DEF = CFG_QUERY.output.correl.path;
    public static final String      STR_OUTPUT_DEF = CFG_TOOLS.output.path + "/query/correl";
  
    
    /** Argument variable name identifying data request decomposition types */
    public static final String      STR_VAR_RQST_DCMP = "--decomp";
    
    /** Argument variable name identifying gRPC stream types */
    public static final String      STR_VAR_STRM_TYPE = "--stypes";
    
    /** Argument variable name identifying gRPC stream sizes */
    public static final String      STR_VAR_STRM_CNT = "--scnts";
    
    /** Argument variable name identifying output location */
    public static final String      STR_VAR_OUTPUT = "--output";

    /** List of all the valid argument delimiters */
    public static final List<String>    LST_STR_DELIMS = List.of(
            STR_VAR_RQST_DCMP,
            STR_VAR_STRM_TYPE,
            STR_VAR_STRM_CNT, 
            STR_VAR_OUTPUT
            );
    
    
    //
    // Application Constants - Client Messages
    //
    
    /** Application name */
    public static final String      STR_APP_NAME = QueryChannelEvaluator.class.getSimpleName();
    
    
    /** The "usage" message for client help requests or invalid application arguments */
    public static final String      STR_APP_USAGE = 
            STR_APP_NAME  + " Usage: \n"
          + "\n"
          + "% " + STR_APP_NAME
          + " [" + STR_VAR_HELP + "]"
          + " [" + STR_VAR_VERSION + "]"
          + " R1 [ ... RN]"
          + " [" + STR_VAR_RQST_DCMP + " D1 ... Di]"
          + " [" + STR_VAR_STRM_TYPE + " S1 ... Sj]"
          + " [" + STR_VAR_STRM_CNT + " N1 ... Nk]"
          + " [" + STR_VAR_OUTPUT +" Output]"
          + "\n" 
          + "  Where  \n"
          + "    " + STR_VAR_HELP + "      = print this message and return.\n"
          + "    " + STR_VAR_VERSION + "   = prints application version information and return.\n"
          + "    R1, ..., Rn = Test request(s) to perform - TestArchiveRequest enumeration name(s). \n"
          + "    D1, ..., Di = Request decomposition type(s) - RequestDecompType enumeration name(s). \n"
          + "    S1, ..., Sj = gRPC stream type(s) - DpGrpcStreamType enumeration name(s). \n"
          + "    N1, ..., Nk = gRPC stream count(s) peforming request recovery - Integer value(s). \n"
          + "    " + STR_VAR_OUTPUT + "    = output directory w/wout file path, or '" + STR_ARG_VAL_STDOUT + "'. \n"
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
    public static final double      DBL_RATE_TARGET = 200.0;


    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLogger(DataCorrelationEvaluator.class, JalQueryAppBase.STR_LOGGING_LEVEL);

    
    //
    // Defining Attributes
    //
    
    /** Name of the input configuration for the test suite */
    private final String                    strInputName;
    
    
    /** The test suite used for <code>QueryChannel</code> evaluations */
    private final QueryChannelTestSuite     suiteEvals;
    
    
    //
    // Instance Resources
    //
    
    /** The collection of test cases used in evaluations */
    private final Collection<QueryChannelTestCase>      setCases;
    
    /** The collection of test results recovered from evaluations */
    private final Collection<QueryChannelTestResult>    setResults;
    
    
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
     * Constructs a new <code>QueryChannelEvaluator</code> instance.
     * </p>
     * <p>
     * Creates and initializes a new <code>QueryChannelEvaluator</code> instance.  
     * All instance resources are created.  Query Service connections are established.
     * Exceptions are thrown if any resource creation fails.
     * </p>
     *
     * @param suiteEvals    the test suite configuration to be run
     * @param strOutputLoc  the location of the evaluations report
     * @param args          the command-line arguments
     * 
     * @throws DpGrpcException                  unable to establish connection to the Query Service (see message and cause)
     * @throws ConfigurationException           no data requests contained in test suite configuration
     * @throws UnsupportedOperationException    the output file path did not belong to the file system
     * @throws FileNotFoundException            unable to create the output file
     * @throws SecurityException                unable to write to the output file                 
     */
    public QueryChannelEvaluator(QueryChannelTestSuite suiteEvals, String strOutputLoc, String...args) throws DpGrpcException, ConfigurationException, UnsupportedOperationException, FileNotFoundException, SecurityException {
        super(QueryChannelEvaluator.class, args);
        
        // Get the defining attributes
        this.suiteEvals = suiteEvals;
        this.strInputName = suiteEvals.getName();
        
        // Create the collection of test cases and container for results
        this.setCases = this.suiteEvals.createTestSuite();  // throws ConfigurationException
        this.setResults = new TreeSet<>(QueryChannelTestResult.descendingRateOrdering());
        
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
     * Runs all test cases within the test suite configuration on the <code>QueryChannel</code> object under investigation.
     * </p>
     * <p>
     * Runs all test cases in resource <code>{@link #setCases}</code> (i.e., specified in the test suite configuration) 
     * and records the results in resource <code>{@link #setResults}</code>.  Updates progress to the Standard Output
     * and to the output file.
     * </p>
     * <p>
     * This method can only be called once; either it succeeds where all test cases are completed and the 
     * 
     * This is a blocking operation and does not return until completed.
     * </p>
     * 
     * @throws IllegalStateException    the <code>{@link #run()}</code> method has already been called
     * @throws DpQueryException         a Query Service exception was encountered during the evaluations
     * @throws InterruptedException     the evaluation was interrupted while waiting for completion
     * @throws CompletionException      the message buffer consumer failed (see message and cause)
     * @throws ResolutionException      the message buffer failed to empty upon completion
     */
    public void run() throws IllegalStateException, DpQueryException, InterruptedException, CompletionException, ResolutionException {
        
        // Check state
        if (super.bolRun) 
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() 
                                            + " - QueryChannel evaluations have already been run.");
        
        // Initialize
        final int     CNT_CASES = this.setCases.size();
        this.bolRun = true;
        
        // Run all test cases on  QueryChannel subject
        int indCase = 1;
       
        LOGGER.info("Running {} test cases for test suite {}...", CNT_CASES, this.strInputName);
        Instant insStart = Instant.now();
        for (QueryChannelTestCase recCase : this.setCases) {
            LOGGER.info("Running test case #{} of {} (with index {}) ...", indCase, CNT_CASES, recCase.indCase());
            
            QueryChannelTestResult recResult = recCase.evaluate(super.chanQuery, super.bufDataMsgs); // throws exceptions
            
            this.setResults.add(recResult);
            indCase++;
        }
        Instant insFinish = Instant.now();

        // Update state variables
        this.durEval = Duration.between(insStart, insFinish);
        this.bolCompleted = true;

        LOGGER.info("Evaluations complete. Time to completion {}.", this.durEval);
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
        this.writeReport(this.psOutput);
        LOGGER.info("Evaluation report stored at location {}.", super.getOutputFilePath());
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
        if (!super.bolRun)
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
        ps.println("Test cases specified : " + this.setCases.size());
        ps.println("Test cases run       : " + this.setResults.size());
        ps.println("Evaluation duration  : " + this.durEval);
        ps.println("Evaluation completed : " + this.bolCompleted);
        ps.println();
        
        // Print out the test suite configuration
        ps.println("Test Suite Configuration");
        this.suiteEvals.printOut(ps, "  ");
        ps.println();
        
        // Print out results summary
        TestResultSummary.assignTargetDataRate(DBL_RATE_TARGET);
        TestResultSummary  recSummary = TestResultSummary.summarize(this.setResults);
        recSummary.printOutChannelSummary(ps, null);
        ps.println();
        
        // Print out results extremes
        TestResultExtremes  recExtremes = TestResultExtremes.computeExtremes(setResults);
        recExtremes.printOut(ps, null);
        ps.println();
        
        // Print out each test result
        ps.println("Individual Case Results:");
        for (QueryChannelTestResult recCase : this.setResults) {
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
     * <li>Request Decomposition Strategies - values identified by the variable delimiter {@value #STR_VAR_RQST_DCMP}. </li> 
     * <li>gRPC Stream Type - value identified by the variable delimiter {@value #STR_VAR_STRM_TYPE}. </li>
     * <li>gRPC Stream Counts - value identified by the variable delimiter {@value #STR_VAR_STRM_CNT}. </li>
     * </ol> 
     * <p>
     * The application command line is first parsed for "commands", which are the <code>{@link TestArchiveRequest}</code>
     * enumeration constants describing the time-series data requests used in the return test suite.
     * These arguments must be the first to appear on the command line and do not have delimiters.  
     * At least one request is required for valid test suite creation, otherwise <code>ConfigurationException</code> 
     * is thrown. If a test request has an invalid <code>{@link TestArchiveRequest}</code> name an exception is thrown.    
     * </p>
     * <p>
     * The remaining arguments are optional; a <code>QueryChannelTestSuite</code> instance has default values that
     * will be supplied if any are missing.  
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * All values for variables {@value #STR_VAR_RQST_DCMP} and {@value #STR_VAR_STRM_TYPE} are the names of
     * enumeration constants.  If an error occurs while parsing the enumeration constant names an 
     * <code>IllegalArgumenttException</code> is thrown.
     * </li>
     * <li>
     * All values for variable {@value #STR_VAR_STRM_CNT} are <code>Integer</code> valued and must be parse as such.  
     * If an error occurs while parsing the integer value an exception is thrown.
     * </li>
     * </ul>  
     * </p>
     *  
     * @param args  the application command-line argument collection
     * 
     * @return  the test suite configuration as described by the command-line arguments
     * 
     * @throws ConfigurationException   no time-series data request (<code>TestArchiveRequest</code> constants) were found
     * @throws IllegalArgumentException invalid enumeration constant name (not in <code>TestArchiveRequest, RequestDecompType</code>, etc.)
     * @throws NumberFormatException    an invalid numeric argument was encounter (could not be converted to a <code>Integer</code> type)
     */
    private static QueryChannelTestSuite    parseTestSuiteConfig(String[] args) 
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
        
        // Parse the request decomposition types and convert to enumeration constants
        List<String>            lstStrRqstDcmps = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_RQST_DCMP);
        List<RequestDecompType> lstEnmRqstDcmps = lstStrRqstDcmps.stream()
                .<RequestDecompType>map(strNm -> RequestDecompType.valueOf(RequestDecompType.class, strNm)) // throws IllegalArgumentException
                .toList();
        
        // Parse the gRPC stream types and convert to enumeration constants
        List<String>            lstStrStrmTypes = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_STRM_TYPE);
        List<DpGrpcStreamType>  lstEnmStrmTypes = lstStrStrmTypes.stream()
                .<DpGrpcStreamType>map(strNm -> DpGrpcStreamType.valueOf(DpGrpcStreamType.class, strNm)) // throws IllegalArgumentException
                .toList();
        
        // Parse the gRPC stream count and convert to integer values
        List<String>    lstStrStrmCnts = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_STRM_CNT);
        List<Integer>   lstStrmCnts = lstStrStrmCnts.stream()
                .<Integer>map(strNum -> Integer.valueOf(strNum))    // throws NumberFormatException
                .toList();
        
        // Build the test suite and return it
        QueryChannelTestSuite suite = QueryChannelTestSuite.create(STR_APP_NAME + " Command Line");
        
        suite.addTestRequests(lstRqsts);
        suite.addRequestDecompositions(lstEnmRqstDcmps);
        suite.addStreamTypes(lstEnmStrmTypes);
        suite.addStreamCounts(lstStrmCnts);
        
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
        if (lstStrOutput.isEmpty()) 
            return STR_OUTPUT_DEF;
    
        // Else return the first element in the list
        String strOutputLoc = lstStrOutput.get(0);
        
        return strOutputLoc;
    }
}
