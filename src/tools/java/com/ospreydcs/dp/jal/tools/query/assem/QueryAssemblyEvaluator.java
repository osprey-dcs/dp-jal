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
 * @since Jul 8, 2025
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
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.impl.QueryRequestProcessorNew;
import com.ospreydcs.dp.api.query.model.assem.QueryResponseAssembler;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.query.correl.DataCorrelationEvaluator;
import com.ospreydcs.dp.jal.tools.query.request.TestArchiveRequest;
import com.sun.jdi.request.InvalidRequestStateException;

/**
 * <p>
 * Application <code>QueryAssemblyEvaluator</code>.
 * </p>
 * <p>
 * Application for evaluating the operation and performance of the <code>{@link QueryResponseAssembler}</code> class.
 * See class constant
 *
 * @author Christopher K. Allen
 * @since Jul 8, 2025
 *
 */
public class QueryAssemblyEvaluator extends JalQueryAppBase<QueryAssemblyEvaluator> {

    /**
     * <p>
     * Application entry point.
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
            System.err.println("  see " + STR_APP_NAME + " " + STR_VAR_HELP);
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.INPUT_CFG_CORRUPT);
            
        }
        
        // Get the test suite configuration and output location from the application arguments
        String                          strOutputLoc;
        QueryAssemblyTestSuiteConfig    cfgTests;
        try {
            
            strOutputLoc = JalApplicationBase.parseOutputLocation(args, STR_OUTPUT_DEF);

            cfgTests = QueryAssemblyEvaluator.parseTestSuiteConfig(args);
            
        } catch (Exception e) {
            
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.INTPUT_ARG_INVALID);
            return;
        }

        //
        // ------- Application Execution -------
        //
        
        // Create the super-domain evaluator and run it, catch any exceptions in context
        try {
            QueryAssemblyEvaluator  appMain = new QueryAssemblyEvaluator(cfgTests, strOutputLoc, args);
            
            appMain.run();
            appMain.writeReport();
            appMain.shutdown();
            
            System.out.println(STR_APP_NAME + " Execution completed in " + appMain.getRunDuration());
            System.out.println("  Results stored at " + appMain.getOutputFilePath().toAbsolutePath());
            System.exit(ExitCode.SUCCESS.getCode());
            
        } catch (DpGrpcException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Unable to connect to Query Service.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.INITIALIZATION_EXCEPTION);
            
        } catch (ConfigurationException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Invalid time-series data request specification in command line.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.INTPUT_ARG_INVALID);
            
        } catch (UnsupportedOperationException e) {
            System.err.println(STR_APP_NAME + " creation FAILURE - Output location not found in file system: " + strOutputLoc);
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
            
        } catch (IllegalStateException e) {
            System.err.println(STR_APP_NAME + " execution FAILURE - Internal super-domain processing error.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
            
        } catch (IndexOutOfBoundsException e) {
            System.err.println(STR_APP_NAME + " execution FAILURE - Internal super-domain processing error.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.EXECUTION_EXCEPTION);
            
        } catch (DpQueryException e) {
            System.err.println(STR_APP_NAME + " data recovery ERROR - General gRPC error in data recovery.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.GRPC_EXCEPTION);
            
        } catch (InterruptedException e) {
            System.err.println(STR_APP_NAME + " application shut down ERROR - Process interrupted while waiting for termination.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.SHUTDOWN_EXCEPTION);
            
        }
    }

    //
    // Application Resources
    //
    
    /** Default configuration parameters for the JAL Tools */
    private static final JalToolsConfig     CFG_TOOLS = JalToolsConfig.getInstance();
    
    
    //
    // Application Constants - Command-Line Arguments and Messages
    //
    
    /** Minimum number of application arguments - argument name and at least one data request */
    public static final int         CNT_APP_MIN_ARGS = 2;
    
    
    /** Default output path location */
    public static final String      STR_OUTPUT_DEF = CFG_TOOLS.output + "/query/assem";
  

    /** Argument variable identifying the PV name value(s) */
    public static final String      STR_VAR_PVS = "--pvs";
    
    /** Argument variable identifying request duration (optional) */
    public static final String      STR_VAR_DUR = "--dur";
    
    /** Argument variable identifying request delay from archive inception */
    public static final String      STR_VAR_DELAY = "--delay";
    
    /** List of all the valid argument delimiters */
    public static final List<String>    LST_STR_DELIMS = List.of(
            STR_VAR_PVS, 
            STR_VAR_DUR, 
            STR_VAR_DELAY,
            STR_VAR_OUTPUT      // from base class
            );
    
    
    //
    // Application Constants - Client Messages
    //
    
    /** Application name */
    public static final String      STR_APP_NAME = QueryAssemblyEvaluator.class.getSimpleName();
    
    
    /** The "usage" message for client help requests or invalid application arguments */
    public static final String      STR_APP_USAGE = 
            STR_APP_NAME  + " Usage: \n"
          + "\n"
          + "% " + STR_APP_NAME
          + " [" + STR_VAR_HELP + "]"
          + " [" + STR_VAR_VERSION + "]"
          + " [R1 ... Rn]"
          + " " + STR_VAR_PVS + " PV1 [... PVm]"
          + " [" + STR_VAR_DUR + " T]"
          + " [" + STR_VAR_DELAY + " D]"
          + " [" + STR_VAR_OUTPUT +" Output]"
          + "\n\n" 
          + "  Where  \n"
          + "    " + STR_VAR_HELP + "        = print this message and return.\n"
          + "    " + STR_VAR_VERSION + "     = prints application version information and return.\n"
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
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLogger(QueryAssemblyEvaluator.class, JalQueryAppBase.STR_LOGGING_LEVEL);

    
    // 
    // Defining Attributes
    //
    
    /** The test suite generator used for <code>SuperDomTestCase</code> evaluations */
    private final QueryAssemblyTestSuiteConfig       cfgTests;
    
    
    //
    // Instance Resources
    //
    
    /** The time-series request processor used to recover and correlate request data - used for all test case evaluations */
    private final QueryRequestProcessorNew              procCorrelator;
    
    /** The raw correlated data processor used to build sampled aggregates - used for all test case evaluations */
    private final QueryResponseAssembler                procAssembler;
    
    
    /** The suite of test cases generated by the test suite configuration */
    private final Collection<QueryAssemblyTestCase>       setTestCases;
    
    /** The collection of test case evaluation results */
    private final Collection<QueryAssemblyTestResult>     setTestResults;
    
    
    //
    // State Variables
    //
    
    /** Total duration of test suite evaluation */
    private Duration    durEval = Duration.ZERO;
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>QueryAssemblyEvaluator</code> instance.
     * </p>
     *
     * @param cfgTests      the test suite generator (pre-configured)
     * @param strOutputLoc  directory path for the output file
     * @param args          the application command-line arguments
     * 
     * @throws DpGrpcException          unable to establish connection with the Query Service (see message and cause)
     * @throws ConfigurationException   either no test requests or supplemental PVs, or only supplemental PVs an no duration
     * @throws UnsupportedOperationException the output file is not associated with the default file system
     * @throws FileNotFoundException    unable to create output file (see message and cause)
     * @throws SecurityException        unable to write to output file
     */
    public QueryAssemblyEvaluator(QueryAssemblyTestSuiteConfig cfgTests, String strOutputLoc, String... args) 
            throws DpGrpcException, ConfigurationException, UnsupportedOperationException, FileNotFoundException, SecurityException {
        super(QueryAssemblyEvaluator.class, args);  // throws DpGrpcException
        
        this.cfgTests = cfgTests;
        
        // Create the query request processor used for request recovery and correlation
        this.procCorrelator = QueryRequestProcessorNew.from(super.connQuery);
        this.procAssembler = QueryResponseAssembler.create();
        
        // Create test case suite and test result container
        this.setTestCases = this.cfgTests.createTestSuite();    //throws ConfigurationException
        this.setTestResults = new TreeSet<>(QueryAssemblyTestResult.ascendingCaseIndexOrdering());
        
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
        // TODO Auto-generated method stub
        return null;
    }



    //
    // Operations
    //

    /**
     * <p>
     * Runs the application evaluating all test cases within the test suite configuration.
     * </p>
     * <p>
     * The test suite generated by the <code>{@link QueryAssemblyTestSuiteConfig}</code>
     * instance provided at construction are available at the time of invocation.  The
     * test suite is run and the results are stored for later reporting.
     * <ul> 
     * <li>All test cases are in container <code>{@link #setTestCases}</code>.</li>
     * <li>Test cases are represented by <code>{@link QueryAssemblyTestCase}</code> records.</li>
     * <li>Test results are obtained from method 
     * <code>{@link QueryAssemblyTestCase#evaluate(QueryRequestProcessorNew, QueryResponseAssembler)}</code>.</li>
     * <li>Test results are represented by <code>{@link QryRspAssemResult}</code> records.</li>
     * <li>All test results are saved to container <code>{@link #setTestResults}</code>.</li>
     * </ul>
     *   
     * @throws InvalidRequestStateException the application has already been run
     * @throws DpQueryException             general exception during raw data recovery (see message and cause) 
     * @throws IllegalStateException        internal super-domain processing error: attempt to recover super domain when none available
     * @throws IndexOutOfBoundsException    internal super-domain processing error: attempt to access index beyond end of processing list
     */
    public void run() throws InvalidRequestStateException, DpQueryException, IllegalStateException, IndexOutOfBoundsException {
        
        // Check state
        if (super.bolRun) {
            throw new InvalidRequestStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Application has already been run.");
        }
        super.bolRun = true;
        
        // Start counter
        System.out.print("Performing evaluations .");
        super.startExecutionTimer(1L, TimeUnit.SECONDS);

        // Iterate over each test case in the test suite 
        Instant insStart = Instant.now();
        for (QueryAssemblyTestCase recCase : this.setTestCases) {
            QueryAssemblyTestResult  recResult = recCase.evaluate(this.procCorrelator, this.procAssembler);
            
            this.setTestResults.add(recResult);
        }
        Instant insFinish = Instant.now();

        this.durEval = Duration.between(insStart, insFinish);
        super.stopExecutionTimer();
        
        // Set state variables
        super.bolCompleted = true;
        super.bolRun = true;
        
        System.out.println(" Evaluations completed in " + this.durEval.toSeconds() + " seconds.");
    }
    
    /**
     * <p>
     * Returns the duration of the test suite evaluations.
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
     * of the <code>SuperDomTestCase</code> evaluations including a summary, test suite configuration, and
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
     * of the super-domain evaluations including a summary, test suite configuration, and
     * all test case results.
     * </p>
     * 
     * @param ps    target output stream for <code>QueryChannel</code> evaluations report
     * 
     * @throws IllegalStateException    no results are available (called before <code>{@link #run()}</code>)
     * @throws NoSuchElementException   the test result container was empty
     */
    public void writeReport(PrintStream ps) throws IllegalStateException, NoSuchElementException {
        
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
        ps.println("Test cases specified : " + this.setTestCases.size() );
        ps.println("Test cases run       : " + this.setTestResults.size());
        ps.println("Evaluation duration  : " + this.durEval);
        ps.println("Evaluation completed : " + this.bolCompleted);
        ps.println();
        
        // General padding string
        String  strPad = "  ";
        
        // Print out the correlator configuration
        ps.println("Raw Data Correlator Configuration");
        this.procCorrelator.printOutConfig(ps, strPad);
        ps.println();
        
        // Print out the response assembler configuration
        ps.println("Query Response Assembler Configuration");
        this.procAssembler.printOutConfig(ps, strPad);
        ps.println();
        
        // Print out the test suite configuration
        ps.println("Test Suite Configuration");
        this.cfgTests.printOut(ps, strPad);
        ps.println();
        
        // Print out results summary
        QueryAssemblyTestResultSummary   recSummary = QueryAssemblyTestResultSummary.summarize(this.setTestResults); // throws NoSuchElementException
        recSummary.printOut(ps, null);
        ps.println();
        
        // Print out individual test case results
        ps.println("Test Case Results");
        for (QueryAssemblyTestResult recResult : this.setTestResults) {
            recResult.printOut(ps, "  ");
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
     * <li>Supplemental PVs - values identified by the variable delimiter {@value #STR_VAR_PVS}. </li> 
     * <li>Request duration override - value identified by the variable delimiter {@value #STR_VAR_DUR}. </li>
     * <li>Request delay override - value identified by the variable delimiter {@value #STR_VAR_DELAY}. </li>
     * </ol> 
     * <p>
     * The application command line is first parsed for "commands", which are the <code>{@link TestArchiveRequest}</code>
     * enumeration constants describing the time-series data requests used in the return test suite.
     * These arguments must be the first to appear on the command line and do not have delimiters.  
     * At least one request is required for valid test suite creation, otherwise an exception is thrown.  
     * If a test request has an invalid <code>{@link TestArchiveRequest}</code> name an exception is thrown.    
     * </p>
     * <p>
     * The remaining arguments are optional; a <code>SuperDomTestSuiteConfig</code> instance has default values that
     * will be supplied if any are missing.  Note that all values for variables {@value #STR_VAR_DUR} and 
     * {@value #STR_VAR_DELAY} are 'PnDTnHnMx.xS' formatted and parsed as such.  If an error occurs
     * while parsing the <code>{@link Duration}</code> value an exception is thrown.
     * </p>
     *  
     * @param args  the application command-line argument collection
     * 
     * @return  the test suite configuration as described by the command-line arguments
     * 
     * @throws NoSuchElementException   no <code>TestArchiveRequest</code> constants or supplemental PVs were found
     * @throws ConfigurationException   request duration or request delay variable contained more than a single value
     * @throws MissingResourceException a request duration was not provided when using only a PV list
     * @throws IllegalArgumentException invalid test request enumeration constant name (not in <code>TestArchiveRequest</code>)
     * @throws DateTimeParseException   an invalid time duration format was encountered (could not be converted to a <code>Duration</code> type)
     */
    private static QueryAssemblyTestSuiteConfig    parseTestSuiteConfig(String[] args) 
            throws NoSuchElementException, MissingResourceException, ConfigurationException, IllegalArgumentException, DateTimeParseException {
     
        // Parse the data requests enumerations and the supplemental PVs
        List<String>    lstRqstNms = JalApplicationBase.parseAppArgsCommands(args);
        List<String>    lstPvNms = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_PVS);
        if (lstRqstNms.isEmpty() && lstPvNms.isEmpty()) {
            String  strMsg = "The command-line arguments contained no time-series data requests or supplemental PVs. Use --help command.";

            throw new NoSuchElementException(strMsg);
        }
        
        // Parse the request duration override value
        List<String>    lstStrRqstDur = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_DUR);
        if (lstStrRqstDur.size() > 1) {
            String  strMsg = "Only one value for variable " + STR_VAR_DUR + " is allowed.  Found value " + lstStrRqstDur + ".";
            
            throw new ConfigurationException(strMsg);
        }
        
        // Parse the request start delay value
        List<String>    lstStrRqstDly = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_DELAY);
        if (lstStrRqstDly.size() > 1) {
            String  strMsg = "Only one value for variable " + STR_VAR_DELAY + " is allowed.  Found value " + lstStrRqstDly + ".";
            
            throw new ConfigurationException(strMsg);
        }
        
        // Check edge condition: no TestArchiveRequest and no request duration
        if (lstRqstNms.isEmpty() && lstStrRqstDur.isEmpty()) {
            String  strMsg = "A request duration " + STR_VAR_DUR + " must be specified for PV list " + lstPvNms + ".";
            
            throw new MissingResourceException(strMsg, QueryAssemblyTestSuiteConfig.class.getName(), STR_VAR_DUR);
        }

        // Convert to TestArchiveRequest enumeration constants
        List<TestArchiveRequest>    lstRqsts = lstRqstNms.stream()
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
        
        // Build the test suite and return it
        QueryAssemblyTestSuiteConfig cfgSuite = QueryAssemblyTestSuiteConfig.create();
        
        cfgSuite.addTestRequests(lstRqsts);
        cfgSuite.addPvNames(lstPvNms);
        if (durRange != null)
            cfgSuite.setRequestRange(durRange);
        if (durDelay != null)
            cfgSuite.setRequestDelay(durDelay);
        
        return cfgSuite;
    }

}
