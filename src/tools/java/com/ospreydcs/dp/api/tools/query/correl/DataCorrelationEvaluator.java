/*
 * Project: dp-api-common
 * File:	DataCorrelationEvaluator.java
 * Package: com.ospreydcs.dp.api.tools.query.correl
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
package com.ospreydcs.dp.api.tools.query.correl;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.BufferUnderflowException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator;
import com.ospreydcs.dp.api.tools.app.ExitCode;
import com.ospreydcs.dp.api.tools.app.ToolsAppBase;
import com.ospreydcs.dp.api.tools.config.JalToolsConfig;
import com.ospreydcs.dp.api.tools.config.query.JalToolsQueryConfig;
import com.ospreydcs.dp.api.tools.query.QueryToolsAppBase;
import com.ospreydcs.dp.api.tools.query.request.TestArchiveRequest;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse.QueryData;

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
 * The application expects the given collection of arguments in order:
 * <ol>
 * <li>Test Requests - commands identified by <code>{@link TestArchiveRequest}</code> enumeration constants. </li>
 * <li>Maximum Thread Counts - values identified by the variable delimiter {@value #STR_VAR_THRDS}. </li> 
 * <li>Concurrency Pivot Sizes - value identified by the variable delimiter {@value #STR_VAR_PIVOT}. </li>
 * <li>Output Location - an optional path or file identified by the variable delimiter {@value #STR_VAR_OUTPUT}. </li>
 * </ol> 
 * Additionally, the following "commands" may be provided:
 * <ul>
 * <li>{@value ToolsAppBase#STR_ARG_HELP} - responses with a the <code>{@link #STR_APP_USAGE}</code> message.</li>
 * <li>{@value ToolsAppBase#STR_ARG_VERSION} - response with the <code>{@link #STR_APP_VERSION}</code> message.</li>
 * </ul>
 * The above arguments may appear anywhere on the command line but take precedence over all other arguments.
 * <p>
 * The application command line is first parsed for "commands", which are the <code>{@link TestArchiveRequest}</code>
 * enumeration constants describing the time-series data requests used in the return test suite.
 * These arguments must be the first to appear on the command line and do not have delimiters.  
 * At least one request is required for valid test suite creation, otherwise an exception is thrown and execution fails  
 * If a test request has an invalid <code>{@link TestArchiveRequest}</code> name an exception is thrown and execution fails.    
 * </p>
 * <p>
 * The remaining arguments are optional; a <code>CorrelatorTestSuite</code> instance has default values that
 * will be supplied if any are missing.  Note that all values for variables {@value #STR_VAR_THRDS} and 
 * {@value #STR_VAR_PIVOT} are <code>Integer</code> valued and must be parse as such.  If an error occurs
 * while parse the integer value an exception is thrown.
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 27, 2025
 *
 */
public final class DataCorrelationEvaluator extends QueryToolsAppBase<DataCorrelationEvaluator> {
    
    
    //
    // Application Entry
    //

    /**
     * <p>
     * Entry point for the tool application.
     * </p>
     * 
     * @param args  command-line arguments as described in <code>{@link DataCorrelationEvaluator}</code>
     */
    public static void main(String[] args) {

        // Check for client help request
        if (ToolsAppBase.parseAppArgsHelp(args)) {
            System.out.println(STR_APP_USAGE);
            
            System.exit(ExitCode.SUCCESS.getCode());
        }
        
        // Check for client version request
        if (ToolsAppBase.parseAppArgsVersion(args)) {
            System.out.println(STR_APP_VERSION);
            
            System.exit(ExitCode.SUCCESS.getCode());
        }

        //
        // ------- Application Initialization -------
        //
        
        // Check for general command-line errors
        try {
            ToolsAppBase.parseAppArgsErrors(args, CNT_APP_MIN_ARGS, LST_STR_DELIMS);

        } catch (Exception e) {
            ToolsAppBase.reportTerminalException(DataCorrelationEvaluator.class, e);

            System.exit(ExitCode.INPUT_CFG_CORRUPT.getCode());
        }
        
        // Get the test suite configuration and output location from the application arguments
        String                  strOutputLoc;
        CorrelatorTestSuite     suite;
        try {
            
            suite = DataCorrelationEvaluator.parseTestSuiteConfig(args);
            strOutputLoc = DataCorrelationEvaluator.parseOutputLocation(args);
            
        } catch (Exception e) {
            
            ToolsAppBase.reportTerminalException(DataCorrelationEvaluator.class, e);
            System.exit(ExitCode.INTPUT_ARG_INVALID.getCode());
            return;
        }

        
        //
        // ------- Application Execution -------
        //
        
        // Create the correlation evaluator and run it
        try {
            DataCorrelationEvaluator    evaluator = new DataCorrelationEvaluator(suite, strOutputLoc);
            
        } catch (DpGrpcException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    
    //
    // Tools Resources
    //
    
    /** Default configuration parameters for the Query Service tools */
    private static final JalToolsQueryConfig    CFG_QUERY = JalToolsConfig.getInstance().query;
    
    
    //
    // Application Constants - Command-Line Arguments and Messages
    //
    
    /** Minimum number of application arguments - argument name and at least one data request */
    public static final int         CNT_APP_MIN_ARGS = 2;
    
    
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
          + "    R1, ..., Rn = Test request(s) to perform (TestArchiveRequest enumeration name(s)). \n"
          + "    M1, ..., Mj = Maximum allowable number(s) of concurrent processing threads (Integer value). \n"
          + "    P1, ..., Pk = Pivot size(s) triggering concurrent processing [Integer value]. \n"
          + "    " + STR_VAR_OUTPUT + "    = output file path, or '" + STR_ARG_VAL_STDOUT + "'. \n"
          + "\n"
          + "  NOTES: \n"
          + "  - All bracketed quantities [...] are optional. \n"
          + "  - Default " + STR_VAR_OUTPUT + " value is in Java API Tools configuration file.\n";

    
    /** The "version" message for client version requests */
    public static final String      STR_APP_VERSION = 
            DataCorrelationEvaluator.class.getSimpleName()
          + " version 1.0: compatible with Java Application Library version 1.8.0 or greater.";
    
    
    //
    // Class Constants - Default Values
    //
    
    /** Default output path location */
    public static final String      STR_OUTPUT_DEF = CFG_QUERY.output.correl.path;
    

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
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLogger(DataCorrelationEvaluator.class, QueryToolsAppBase.STR_LOGGING_LEVEL);


    // 
    // Defining Attributes
    //
    
    /** The test suite used for <code>QueryChannel</code> evaluations */
    private final CorrelatorTestSuite       suiteEvals;
    
    /** Output report location */
    private final String                    strOutputLoc;
    
    
    
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
     * Constructs a new <code>DataCorrelationEvaluator</code> instance.
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
        this.strOutputLoc = strOutputLoc;
        
        // Create the correlator
        this.toolCorrelator = RawDataCorrelator.create();
        this.toolCorrelator.setConcurrencyPivotSize(SZ_CONCURRENCY_PIVOT);
        this.toolCorrelator.enableConcurrency(BOL_CONCURRENCY);
        this.toolCorrelator.setMaxThreadCount(CNT_CONCURRENCY_THDS);
        
        // Create the collection of test cases and container for results
        this.mapCases = this.suiteEvals.createTestSuite();  // throws ConfigurationException
        this.setResults = new LinkedList<>();
        
        // Create the output stream
        super.openOutputStream(strOutputLoc); // throws SecurityException, FileNotFoundException, UnsupportedOperationException
    }

    
    //
    // ToolsAppBase Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.api.tools.app.ToolsAppBase#getLogger()
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
     * <code>{@link #mapCases}</code> key set.  The data for the test request is first recovered
     * with the super-class <code>{@link QueryToolsAppBase#chanQuery}</code> and 
     * <code>{@link QueryToolsAppBase#bufDataMsgs}</code> resources.
     * Then all <code>{@link CorrelatorTestCase} records that use the recovered data from that
     * <code>TestArchiveRequest</code> are performed.
     * </p>
     * 
     * @throws DpQueryException         general exception during test request data recovery (see message and cause)
     * @throws InterruptedException     processing interruption recovering request data in message data buffer
     * @throws IllegalStateException    data request attempt on empty message buffer
     * @throws BufferUnderflowException message buffer not fully drained
     * @throws IllegalArgumentException invalid timestamps were encountered in the recovered data set
     * @throws CompletionException      error in <code>DataBucket</code> insertion task (see cause)
     */
    public void run() throws DpQueryException, IllegalStateException, InterruptedException, BufferUnderflowException, IllegalArgumentException, CompletionException {

//        // The container of recovered data for each test request
//        //  It is cleared an reused for each test request
//        List<QueryData> lstDataMsgs = new LinkedList<>();
        
        Instant insStart = Instant.now();
        for (TestArchiveRequest enmRqst : this.mapCases.keySet()) {
            
//            // First perform the data request against the Query Service test archive
//            DpDataRequest   rqst = enmRqst.create();
//            
//            super.bufDataMsgs.activate();           // ready buffer for incoming messages
//            super.chanQuery.recoverRequest(rqst);   // blocking operation - throws DpQueryException
//            Thread  thdShutdn = new Thread( () -> { // spawn the buffer shutdown so #isSupplying() returns false when empty
//                    try {
//                        super.bufDataMsgs.shutdown();
//                    } catch (InterruptedException e) {} 
//                }
//            );
//            thdShutdn.start();
//            
//            // Transfer recovered test data from message buffer to local list
//            while (super.bufDataMsgs.isSupplying()) {
//                QueryData   msgData = super.bufDataMsgs.take(); // throws IllegalStateException, InterruptedException
//                
//                lstDataMsgs.add(msgData);
//            }
//            thdShutdn.join();                                   // throws InterruptedException
//            
//            if (super.bufDataMsgs.isSupplying())
//                throw new BufferUnderflowException();           // still have messages in buffer
//            
//            super.bufDataMsgs.shutdownNow();    // clears out the message buffer just in case

            // Perform all the test case evaluations for that test request
            List<QueryData>             lstDataMsgs = this.recoverTestData(enmRqst);
            List<CorrelatorTestCase>    lstCases = this.mapCases.get(enmRqst);
            
            this.toolCorrelator.reset();
            for (CorrelatorTestCase recCase : lstCases) {
                CorrelatorTestResult recResult = recCase.evaluate(this.toolCorrelator, lstDataMsgs);    // throws IllegalArgumentException, CompletionException
                
                this.setResults.add(recResult);
            }
            
//            // Clear out the test message set for the next test request
//            lstDataMsgs.clear();
        }
        Instant insFinish = Instant.now();
        
        this.durEval = Duration.between(insStart, insFinish);
        super.bolCompleted = true;
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
        LOGGER.info("Evaluation report stored at location {}.", super.pathOutFile);
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
        ps.println("Test cases specified : " + this.mapCases.size());
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
     * <li>Maximum Thread Counts - values identified by the variable delimiter {@value #STR_VAR_THRDS}. </li> 
     * <li>Concurrency Pivot Sizes - value identified by the variable delimiter {@value #STR_VAR_PIVOT}. </li>
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
     * will be supplied if any are missing.  Note that all values for variables {@value #STR_VAR_THRDS} and 
     * {@value #STR_VAR_PIVOT} are <code>Integer</code> valued and must be parse as such.  If an error occurs
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
        List<String>    lstRqstNms = ToolsAppBase.parseAppArgsCommands(args);
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
        List<String>    lstStrThrdCnts = ToolsAppBase.parseAppArgsVariable(args, STR_VAR_THRDS);
        List<Integer>   lstThrdCnts = lstStrThrdCnts.stream()
                .<Integer>map(strNum -> Integer.valueOf(strNum))                    // throws NumberFormatException
                .toList();
        
        // Parse the pivot sizes and convert to integers
        List<String>    lstStrSzPivots = ToolsAppBase.parseAppArgsVariable(args, STR_VAR_PIVOT);
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
        List<String>    lstStrOutput = ToolsAppBase.parseAppArgsVariable(args, STR_VAR_OUTPUT);
    
        // If there is no user-provided output location use the default value in the JAL configuration
        if (lstStrOutput.isEmpty()) 
            return STR_OUTPUT_DEF;
    
        // Else return the first element in the list
        String strOutputLoc = lstStrOutput.get(0);
        
        return strOutputLoc;
    }
    
    /**
     * <p>
     * Recovers all the raw time-series for the given test archive data request.
     * </p>
     * <p>
     * Uses the <code>QueryChannel</code> and <code>QueryMessageBuffer</code> resources of the base class to perform
     * the recovery operation for the given request.  This is somewhat involved since the message buffer has state
     * dynamics that must be considered.  Specifically, we require the follow events in order:
     * <ol>
     * <li>The buffer must be activated in order to accept the incoming data messages.</li>
     * <li>The recovery operation is then started and the buffer begins receiving response data - block until complete.</li>
     * <li>The buffer shutdown operation is started on a separate thread - it is a blocking operation.</li>
     * <li>A recovery loop is entered continuing until the buffer stops supplying. </li>
     * <li>Wait until the shutdown thread completes - should be complete when buffer is empty.</li>
     * </ol>
     * Note that once the buffer is shutdown it will no longer accept new data messages, however, we do not start
     * the shutdown thread until the recovery operation has complete and the buffer should be full.  It will continue
     * to supply message in the buffer until exhausted, after which it will refuse any additional requests
     * (by throwing an <code>IllegalStateException</code>.)
     * </p>
     *  
     * @param enmRqst   the data request to perform
     * 
     * @return  all the recovered data for the given request
     * 
     * @throws DpQueryException         general exception during test request data recovery (see message and cause)
     * @throws InterruptedException     processing interruption recovering request data in message data buffer
     * @throws IllegalStateException    data request attempt on empty message buffer
     * @throws BufferUnderflowException message buffer not fully drained
     */
    @SuppressWarnings("unused")
    private List<QueryData> recoverTestData(TestArchiveRequest enmRqst) throws DpQueryException, IllegalStateException, InterruptedException, BufferUnderflowException {
        
        // The container of recovered data for test request
        List<QueryData> lstDataMsgs = new LinkedList<>();
        
        // Buffer shutdown operation is blocking - must be performed on separate thread
        Thread  thdShutdn = new Thread( () -> { 
                try {
                    super.bufDataMsgs.shutdown();
                } catch (InterruptedException e) {} 
            }
        );
        
        // First perform the data request against the Query Service test archive
        DpDataRequest   rqst = enmRqst.create();
        
        super.bufDataMsgs.activate();           // ready buffer for incoming messages
        super.chanQuery.recoverRequest(rqst);   // blocking operation, supplies messages to buffer - throws DpQueryException
        thdShutdn.start();                      // spawn the buffer shutdown thread so #isSupplying() returns false when empty                          
        
        // Transfer recovered test data from message buffer to local list
        while (super.bufDataMsgs.isSupplying()) {
            QueryData   msgData = super.bufDataMsgs.take(); // throws IllegalStateException, InterruptedException
            
            lstDataMsgs.add(msgData);
        }
        thdShutdn.join();                                   // throws InterruptedException
        
        // Check for bad recovery state
        if (super.bufDataMsgs.isSupplying())
            throw new BufferUnderflowException();           // still have messages in buffer
        
        return lstDataMsgs;
    }
}
