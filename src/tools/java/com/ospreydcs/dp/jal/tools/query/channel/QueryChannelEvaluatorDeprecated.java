/*
 * Project: dp-api-common
 * File:	QueryChannelEvaluatorDeprecated.java
 * Package: com.ospreydcs.dp.jal.tools.query.channel
 * Type: 	QueryChannelEvaluatorDeprecated
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
 * @since May 11, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.channel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.module.FindException;
import java.lang.module.ResolutionException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.model.CfgLoaderYaml;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactoryStatic;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.model.grpc.QueryChannel;
import com.ospreydcs.dp.api.query.model.grpc.QueryMessageBuffer;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.query.JalToolsQueryConfig;
import com.ospreydcs.dp.jal.tools.config.request.JalRequestSuiteConfig;

/**
 * <p>
 * Application for evaluating the performance of the <code>QueryChannel</code> class under predefined test suites.
 * </p>
 * <p>
 * There must be at least 1 command-line argument and at most 2 arguments.  The 1st argument indicates the
 * test suite to be executed and the 2nd (optional) argument specifies the output location.  
 * More specifically, the command line arguments for the application are as follows:
 * <pre>
 *   <code>>QueryChannelEvaluatorDeprecated name [output]</code>
 * </pre>
 * where 
 * <ul>
 * <li><code>name</code> = name of a pre-defined test suite configuration within the Java API Tools configuration.</li>
 * <li><code>name</code> = file path of a YAML file containing <code>JalRequestSuiteConfig</code> formatted test suite.</li>
 * <li><code>output</code> = path for evaluation output file (unique file is created). </li>
 * <li><code>output</code> = file path for evaluation output (any existing file is clobbered).</li> 
 * <li><code>output</code> = the string "console" - evaluation output is sent directly to console. 
 * </ul>
 * If the <code>output</code> argument is not provided, the output of the evaluation is created in
 * the default path specified in the Java API Tools configuration with value given by class constant
 * <code>{@link #STR_OUTPUT_PATH_DEF}</code>. 
 * </p>
 * <p>
 * <h2>Test Suites</h2>
 * The <code>QueryChannelEvaluatorDeprecated</code> runs "test suites" described by the <code>{@link JalRequestSuiteConfig}</code>
 * structure class.  The structure class is used to instantiate a <code>{@link QueryChannelTestSuite}</code>
 * object.  The <code>QueryChannelTestSuite</code> object generates collections of 
 * <code>{@link QueryChannelTestCase}</code> instances according to the configuration in the original structure class.
 * Each <code>QueryChannelTestCase</code> object performs an evaluation of a test <code>QueryChannel</code> object
 * under the conditions of the test.
 * </p> 
 * <p>
 * <h2>Output</h2>
 * A new output file is created for each application invocation.  The file will be located in the path
 * given by the optional 2nd argument or the default location in the configuration file.  The name of
 * the file has the form
 * <pre>
 *   <code>QueryChannelEvaluatorDeprecated-[creation instant].txt</code>
 * </pre>
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 11, 2025
 *
 */
@Deprecated(since="Jun 9, 2025", forRemoval=true)
public class QueryChannelEvaluatorDeprecated {

    
    //
    // Application Entry
    //

    /**
     * <p>
     * Entry point for <code>QueryChannelEvaluatorDeprecated</code> application tool.
     * </p>
     * 
     * @param args  application command-line arguments as described in <code>{@link QueryChannelEvaluatorDeprecated}</code>
     */
    public static void main(String[] args) {
        
        System.out.println("This application has been deprecated and is no longer viable.");
        System.out.println("  Use application QueryChannelEvaluator.");
        System.out.println("  See");
        System.out.println("  >QueryChannelEvaluator --help");
        System.out.println();
        System.out.println("Exiting...");
        System.exit(ExitCode.SUCCESS.getCode());

        // Create the evaluator from application arguments while catching and reporting any exceptions
        QueryChannelEvaluatorDeprecated   evaluator;

        try {
            evaluator = QueryChannelEvaluatorDeprecated.createEvaluator(args);
            
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.out.println(STR_USAGE);
            System.out.println();
            System.out.println("Exiting...");
            
            System.exit(ExitCode.BAD_ARG_COUNT.getCode());
            return;
            
        } catch (IllegalCallerException e) {
            System.out.println(STR_USAGE);
            
            System.exit(ExitCode.SUCCESS.getCode());
            return;

        } catch (FindException e) {
            QueryChannelEvaluatorDeprecated.reportException(e);
            
            System.exit(ExitCode.INTPUT_ARG_INVALID.getCode());
            return;

        } catch (UnsupportedOperationException e) {
            QueryChannelEvaluatorDeprecated.reportException(e);
            
            System.exit(ExitCode.OUTPUT_FAILURE.getCode());
            return;

        } catch (FileNotFoundException e) {
            QueryChannelEvaluatorDeprecated.reportException(e);
            
            System.exit(ExitCode.OUTPUT_FAILURE.getCode());
            return;

        } catch (SecurityException e) {
            QueryChannelEvaluatorDeprecated.reportException(e);
            
            System.exit(ExitCode.OUTPUT_FAILURE.getCode());
            return;

        } catch (ConfigurationException e) {
            QueryChannelEvaluatorDeprecated.reportException(e);
            
            System.exit(ExitCode.INPUT_CFG_CORRUPT.getCode());
            return;
            
        } catch (DpGrpcException e) {
            QueryChannelEvaluatorDeprecated.reportException(e);
            
            System.exit(ExitCode.GRPC_CONN_FAILURE.getCode());
            return;
        }
        
        // Run the evaluator test suite
        String  strLogErr = "Exception while running evaluator: {}. Execution terminated.";
        
        try {
            evaluator.run();
            
            evaluator.writeReport();
            
            evaluator.shutdown();

        } catch (IllegalStateException e) {
            if (BOL_LOGGING)
                LOGGER.error(strLogErr, e.getClass(), e);
            
            QueryChannelEvaluatorDeprecated.reportException(e);
            System.exit(ExitCode.EXECUTION_EXCEPTION.getCode());
            return;

        } catch (DpQueryException e) {
            if (BOL_LOGGING)
                LOGGER.error(strLogErr, e.getClass(), e);

            QueryChannelEvaluatorDeprecated.reportException(e);
            System.exit(ExitCode.EXECUTION_EXCEPTION.getCode());
            return;

        } catch (InterruptedException e) {
            if (BOL_LOGGING)
                LOGGER.error(strLogErr, e.getClass(), e);

            QueryChannelEvaluatorDeprecated.reportException(e);
            System.exit(ExitCode.EXECUTION_EXCEPTION.getCode());
            return;

        } catch (CompletionException e) {
            if (BOL_LOGGING)
                LOGGER.error(strLogErr, e.getClass(), e);

            QueryChannelEvaluatorDeprecated.reportException(e);
            System.exit(ExitCode.EXECUTION_EXCEPTION.getCode());
            return;

        } catch (ResolutionException e) {
            if (BOL_LOGGING)
                LOGGER.error(strLogErr, e.getClass(), e);

            QueryChannelEvaluatorDeprecated.reportException(e);
            System.exit(ExitCode.EXECUTION_EXCEPTION.getCode());
            return;
        }

        LOGGER.info("Execution succcessful. Exiting normally.");
        System.exit(ExitCode.SUCCESS.getCode());
    }

    //
    // Application Resources
    //
    
    /** Query tools default configuration parameters */
    private static final DpQueryConfig     CFG_DEF = DpApiConfig.getInstance().query;
    
    
    //
    // Class Types
    //
    
    /** Increment variable used for <code>ExitCode</code> enumeration values. */
    private static int  intExitCode = 0;
    
    /**
     * <p>
     * Enumeration of all possible application termination codes.
     * </p>
     */
    public static enum ExitCode {
        
        /** Successful execution */
        SUCCESS(intExitCode++),
        
        /** Bad argument count */
        BAD_ARG_COUNT(intExitCode++),
        
        /** The input argument was invalid */
        INTPUT_ARG_INVALID(intExitCode++),
        
        /** The input data pointed to by the input argument was corrupt */
        INPUT_CFG_CORRUPT(intExitCode++),
        
        /** A failure occurred in establishing the output stream */
        OUTPUT_FAILURE(intExitCode++),
        
        /** Unable to establish a gRPC connection to service */
        GRPC_CONN_FAILURE(intExitCode++),
        
        /** Exception during application execution */
        EXECUTION_EXCEPTION(intExitCode++)
        ;
        
        /** the integer value of the exit code */
        private final int   intValue;
        
        /** private constructor for enumeration constant - sets code value */
        private ExitCode(int intValue) { this.intValue = intValue; };
        
        /**
         * @return  the integer value of this exit code
         */
        public int  getCode() { return this.intValue; };
    }
    
    //
    // Class Constants
    //
    
    /** The "usage" error message for invalid application arguments */
    public static final String      STR_USAGE = 
                                                "Usage: \n"
                                              + "\n"
                                              + ">QueryChannelEvaluatorDeprecated name [output] \n" 
                                              + "  Where  \n"
                                              + "    name = 'help' returns this message, or \n"
                                              + "    name = name of pre-defined test suite configuration, or \n"
                                              + "    name = YAML file containing formatted test suite configuration. \n"
                                              + "    output = (optional) 'consoled' sends output to this console, or \n"
                                              + "    output = (optional) path location of output file (name created), or \n"
                                              + "    output = (optional) file path of output file (name given).\n"
                                              + "\n"
                                              + "  NOTE: \n"
                                              + "    Default [output] value is in Java API Tools configuration file.";

    
    //
    // Special Arguments and Default Values
    //

    /** Application argument value for help */
    public static final String      STR_INPUT_HELP = "help";

    /** Application argument value for console output */
    public static final String      STR_OUTPUT_CONSOLE = "console";

    /** The default output location (i.e., if none is provided) */
    public static final String     STR_OUTPUT_PATH_DEF = JalToolsConfig.getInstance().output.path;

    /** The default output file extension */
    public static final String     STR_OUTPUT_FILE_EXT = ".txt";


    //
    // Class Constants
    //
    
    /** The target data rate (MBps) */
    public static final double      DBL_RATE_TARGET = 100.0;
    

    //
    // Timeout Limits for Request Data Recovery (may exceed default API Library settings)
    //
    
    /** Timeout limit for requested data recovery */
    public static final long        LNG_TIMEOUT = 75L;
    
    /** Timeout limit units for requested data recovery */
    public static final TimeUnit    TU_TIMEOUT = TimeUnit.SECONDS;
    
    
    //
    // Logging Parameters
    //
    
    /** Event logging enabled flag */
    public static final boolean     BOL_LOGGING = CFG_DEF.logging.enabled;

    /** Event logging level */
    public static final String      STR_LOGGING_LEVEL = CFG_DEF.logging.level;


    /** The name of the scenario logging appender - target of scenario log entries */
    private static final String     STR_LOG_APPENDER_NAME = "QueryChannelEvaluatorAppender";


    //
    // Class Resources
    //

    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLogger(QueryChannelEvaluatorDeprecated.class, STR_LOGGING_LEVEL);
    
    
    //
    // Defining Attributes
    //
    
    /** Name of the input configuration for the test suite */
    private final String                    strInputName;
    
    /** Output report location */
    private final String                    strOutputLoc;
    
    
    /** The test suite used for <code>QueryChannel</code> evaluations */
    private final QueryChannelTestSuite     suiteEvals;
    
    /** The output stream to receive evaluation report */
    private final PrintStream               psOutput;
    
    
    //
    // Instance Resources
    //
    
    /** The collection of test cases used in evaluations */
    private final Collection<QueryChannelTestCase>      setCases;
    
    /** The collection of test results recovered from evaluations */
    private final Collection<QueryChannelTestResult>    setResults;
    
    
    /** The gRPC connection to the Query Service (default connection) */
    private final DpQueryConnection                     connQuery;
    
    /** The receiver of <code>QueryData</code> Protocol Buffers messages */
    private final QueryMessageBuffer                    bufDataMsgs;
    
    /** The <code>QueryChannel</code> object under evaluation */
    private final QueryChannel                          chanQuery;

    
    //
    // State Variables
    //
    
    /** Evaluation run() attempted */
    private boolean     bolRun = false;
    
    /** Evaluation completed flag */
    private boolean     bolCompleted = false;
    
    /** Evaluator shut down flag */
    private boolean     bolShutdown = false;
    
    /** Total duration of test suite evaluation */
    private Duration    durEval = Duration.ZERO;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>QueryChannelEvaluatorDeprecated</code> instance.
     * </p>
     *
     * @param strInputName  name of the input configuration
     * @param strOutputLoc  location of the output report
     * @param cfgSuite      the test suite configuration record 
     * @param psOutput      output stream to receive evaluation report
     * 
     * @throws ConfigurationException   there are no test requests in the given configuration
     * @throws DpGrpcException          unable to establish connection to the Data Platform Query Service
     */
    public QueryChannelEvaluatorDeprecated(String strInputName, String strOutputLoc, JalRequestSuiteConfig cfgSuite, PrintStream psOutput) throws ConfigurationException, DpGrpcException {
        this.strInputName = strInputName;
        this.strOutputLoc = strOutputLoc;
        this.psOutput = psOutput;
        
        // Create the test suite and results container from the configuration record
        this.suiteEvals = QueryChannelTestSuite.from(cfgSuite);
        this.setCases = this.suiteEvals.createTestSuite();          // throws ConfigurationException
        this.setResults = new TreeSet<>(QueryChannelTestResult.descendingRateOrdering());
        
        // Create the channel resources and channel
        this.bufDataMsgs = QueryMessageBuffer.create();
        this.connQuery = DpQueryConnectionFactoryStatic.connect();  // throws DpGrpcException
        this.chanQuery = QueryChannel.from(connQuery, bufDataMsgs);
        
        // Set timeout for channel operation and for request operation
        this.connQuery.setTimeoutLimit(LNG_TIMEOUT, TU_TIMEOUT);
        this.chanQuery.setTimeoutLimit(LNG_TIMEOUT, TU_TIMEOUT);
    }
    
    
    //
    // State/Attribute Query
    //
    
    /**
     * <p>
     * Determines whether or not the test suite has been run.
     * </p>
     * <p>
     * A return value of <code>true</code> indicates that the <code>{@link #run()}</code> method has been invoked
     * This method can be invoked only once so a <code>true</code> value indicates that the current evaluator is
     * in its final state and any results obtained are available.
     * </p>  
     * <p>
     * To determine if all test cases where evaluated successfully one must
     * invoke <code>{@link #hasCompleted()}</code>.  In other words, a returned value of <code>true</code> is 
     * necessary for evaluation success but not sufficient.
     * </p>
     * 
     * @return  <code>true</code> the <code>{@link #run()}</code> method has been invoked
     *          <code>false</code> the evaluator is still in its initial state
     */
    public boolean  hasRun() {
        return this.bolRun;
    }
    
    /**
     * <p>
     * Determines whether or not the test suite has been run and completed successfully.
     * </p>
     * <p>
     * A returned value of <code>true</code> indicates that the <code>{@link #run()}</code> method has been
     * invoked and that all test cases where successfully completed, specifically, there were no errors in
     * the <code>QueryChannel</code> evaluations performed by <code>{@link #run()}</code>.  
     * To determine if errors occurred and the <code>{@link #run()}</code>
     * method exited prematurely see <code>{@link #hasRun()}</code> which returns <code>true</code> in that case.
     * </p>
     *  
     * @return  <code>true</code> all test cases where successfully run,
     *          <code>false</code> either the evaluator has not be run or an error occurred during evaluations
     *          
     * @see #hasRun()
     */
    public boolean  hasCompleted() {
        return this.bolCompleted;
    }
    
    /**
     * <p>
     * Determines whether or not the evaluator has been shut down.
     * </p>
     * <p>
     * A returned value of <code>true</code> indicates that the <code>{@link #shutdown()}</code> method has been
     * invoked and the evaluator is no longer active, regardless or whether or not it has been run.
     * </p>
     * 
     * @return  <code>true</code> the evaluator has been shut down and is no longer active
     *          <code>false</code> the evaluator is still alive
     */
    public boolean  hasShutdown() {
        return this.bolShutdown;
    }
    
    /**
     * <p>
     * Returns the collection of test case results for the test suite.
     * </p>
     * <p>
     * This method is applicable after the invocation to method <code>{@link #run()}</code> otherwise an
     * exception is thrown.  It returns the collection of test case results that were successful from
     * the test suite evaluations.
     * </p>
     * 
     * @return  the collection of successful test case results for the evaluation, best case first ordering
     * 
     * @throws IllegalStateException    no results are available (called before <code>{@link #run()}</code>) 
     */
    public Collection<QueryChannelTestResult>  getResults() throws IllegalStateException {
        
        // Check state
        if (!this.bolRun)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + "- Test suite has not been run.");
        
        return this.setResults;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Runs all test cases within the test suite on the <code>QueryChannel</code> object under investigation.
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
        if (this.bolRun) 
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
            
            QueryChannelTestResult recResult = recCase.evaluate(this.chanQuery, this.bufDataMsgs); // throws exceptions
            
            this.setResults.add(recResult);
            indCase++;
        }
        Instant insFinish = Instant.now();

        this.durEval = Duration.between(insStart, insFinish);
        this.bolCompleted = true;

        LOGGER.info("Evaluations complete. Time to completion {}.", this.durEval);
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
        LOGGER.info("Evaluation report stored at location {}.", this.strOutputLoc);
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
        String  strHdr = this.createReportHeader();
        ps.println();
        ps.println(strHdr);
        
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
    
    /**
     * <p>
     * Shuts down the evaluator.
     * </p>
     * <p>
     * This method should be called before the evaluator is discarded, specifically, at the end of its
     * lifetime.  The Query Service connection is terminated and the output stream is flushed and closed.
     * This is a blocking operation and does not return until all resources are released.
     * </p>
     * <p>
     * If the evaluator has already been shut down (i.e., this method has already been called) nothing is done.
     * That is, it is safe to call this method multiple times.
     * </p>
     *  
     * @throws InterruptedException     interrupted while waiting for the Query Service connection to shut down
     */
    public void shutdown() throws InterruptedException {
        
        // Check state
        if (this.bolShutdown)
            return;
        
        try {
            // Shut down the message buffer and Query Service connection
            this.bufDataMsgs.shutdownNow();
            this.connQuery.shutdownSoft();
            this.connQuery.awaitTermination();

        } catch (InterruptedException e) {
            throw e;
            
        } finally {
            
            // Close the output stream
            this.psOutput.flush();
            this.psOutput.close();
            
            this.bolShutdown = true;
        }
    }
    
    
    //
    //  QueryChannelEvaluatorDeprecated Support Methods
    // 

//    /**
//     * <p>
//     * Creates the collection of test cases from the given test suite configuration structure.
//     * </p>
//     * <p>
//     * The collection of <code>{@link QueryChannelTestCase}</code> records (i.e., the test suite) is created
//     * according to the given configuration structure and then returned. The collection is used for all
//     * <code>QueryChannel</code> evaluation operations.
//     * </p>
//     * 
//     * @param cfgSuite  the request suite configuration structure class
//     * 
//     * @return  collection of all test cases to be executed
//     * 
//     * @throws ConfigurationException    there are no test requests in the given configuration
//     */
//    private Collection<QueryChannelTestCase>    initTestCases(JalRequestSuiteConfig cfgSuite) throws ConfigurationException {
//        QueryChannelTestSuite   suite = QueryChannelTestSuite.from(cfgSuite);
//        
//        Collection<QueryChannelTestCase>    setCases = suite.createTestSuite(); // throws exception
//        
//        return setCases;
//    }
//    
//    /**
//     * <p>
//     * Sends a case evaluation update message to Standard Output and the output stream.
//     * </p>
//     * 
//     * @param cntCases  total number of test cases
//     * @param indCase   index of current test case
//     */
//    private void    updateTestCase(final int cntCases, int indCase) {
//        String  strMsg = "Running test case #" + indCase + " of " + cntCases + "...";
//        
//        System.out.println(strMsg);
//        this.psOutput.println(strMsg);
//    }
    
    /**
     * <p>
     * Creates and returns a header line for evaluations report.
     * </p>
     * <p>
     * A header is created which identifies the file contents and time of creation.
     * </p>
     * 
     * @return  a header string for evaluations reports
     */
    private String   createReportHeader() {
        
        // Write header
        DateFormat  date = DateFormat.getDateTimeInstance();
        String      strDateTime = date.format(new Date());
        String      strHdr = QueryChannelEvaluatorDeprecated.class.getSimpleName() + " Output Results: " + strDateTime + "\n";

        return strHdr;
    }
    
    
    //
    // Main Method Support
    //
    
    /**
     * <p>
     * Checks that the Application command-line arguments then creates a new <code>QueryChannelEvaluatorDeprecated</code> from them.
     * </p>
     * <p>
     * The 1st argument is first checked for existence.  If the 2nd argument is included the directory 
     * location is checked for existence.  The internal path attributes are then set according to
     * the command-line arguments.
     * </p>
     * 
     * @param args   string array of application command-line arguments
     * 
     * @return  a new <code>QueryChannelEvaluatorDeprecated</code> instance initialized according to the argument resources
     *  
     * @throws IllegalArgumentException the argument was the wrong length (either 1 or 2)
     * @throws IllegalCallerException   the client requested command-line help
     * @throws FindException            the 1st argument was neither a pre-defined test suite or a valid configuration file
     * @throws UnsupportedOperationException 2nd argument file path is not associated with default provider
     * @throws FileNotFoundException    unable to create output file (see message and cause)
     * @throws SecurityException        unable to write to output file
     * @throws ConfigurationException   there are no test requests in the given configuration
     * @throws DpGrpcException          unable to establish connection to the Data Platform Query Service
     */
    private static QueryChannelEvaluatorDeprecated createEvaluator(String[] args) 
            throws IllegalArgumentException, IllegalCallerException, 
                   FindException, 
                   UnsupportedOperationException, FileNotFoundException, SecurityException, 
                   ConfigurationException, DpGrpcException  {
        
        // Check argument count
        if (args.length==0 || args.length > 2) 
            throw new IllegalArgumentException("Invalid Arguments: Bad argument count " + args.length);
        
        
        // Check for help request
        String  strHelp = args[0].toLowerCase();
        if (strHelp.equals(STR_INPUT_HELP))
            throw new IllegalCallerException("Client requests command-line help.");
        
        // Recover argument values
        String      strCfgName = args[0];
        String      strOutputLoc;
        
        if (args.length == 2)
            strOutputLoc = args[1];
        else
            strOutputLoc = STR_OUTPUT_PATH_DEF;
        
        // Recovery/create input and output resources for the QueryChannelEvaluatorDeprecated instance
        JalRequestSuiteConfig    cfgRqst = recoverTestSuiteConfig(strCfgName);   // throws FindException
        PrintStream             psOutput = createOutputStream(strOutputLoc);    // throws UnsupportedOperationException, FileNotFoundException, SecurityException

        // Direct the logging output to the output stream
        OutputStreamAppender    appdrOstr = Log4j.createOutputStreamAppender(STR_LOG_APPENDER_NAME, psOutput);
        Log4j.attachAppender(LOGGER, appdrOstr);
        
        // Create and return new evaluator
        QueryChannelEvaluatorDeprecated   evaluator = new QueryChannelEvaluatorDeprecated(strCfgName, strOutputLoc, cfgRqst, psOutput);   // throws ConfigurationException, DpGrpcException
        
        return evaluator;
    }
    
    /**
     * <p>
     * Attempts to recover the test suite configuration from the given name.
     * </p>
     * <p>
     * The argument is assumed to be either a named, pre-defined test suite configuration within the Java API Tools
     * configuration file, or a separate file containing a test suite configuration.  In the case of the latter,
     * the file must be a YAML file containing the test suite configuration in a valid format for the structure
     * class <code>{@link JalRequestSuiteConfig}</code>.  In the former case, the structure class obtained from the
     * tools library configuration file is returned.  In the latter case, the YAML file is loaded and the structure
     * class is populated from the file parameters then returned.
     * </p>
     * <p>
     * The following operations are performed in order:
     * <ol>
     * <li>
     * The Java API Tools configuration file is searched for a test suite configuration with name given by 
     * the argument.
     * </li>
     * <li>
     * If the above fails the argument is checked that it is a valid file location.  If not an exception is
     * thrown.
     * </li>
     * <li>
     * An attempt is made to read the (valid) file into a new <code>JalRequestSuiteConfig</code> structure class.
     * If that fails an exception is thrown.
     * </li>
     * <li>
     * If operation arrives here then the new <code>JalRequestSuiteConfig</code> instance is valid and it
     * is returned.
     * </li>
     * </ol>
     * 
     * @param strName   named, pre-defined test suite or path containing file with test suite configuration
     *  
     * @return  a new <code>JalRequestSuiteConfig</code> structure class with test suite configuration
     * 
     * @throws FindException the argument was neither a pre-defined test suite nor a valid configuration file
     */
    private static JalRequestSuiteConfig recoverTestSuiteConfig(String strName) throws FindException {
        
        // Check if name is a pre-defined test suite (in the API Tools configuration)
//        List<JalRequestSuiteConfig>  lstCfgsAll = CFG_DEF.testRequests.testSuites;
        List<JalRequestSuiteConfig>  lstCfgsAll = new LinkedList<>();
        
        List<JalRequestSuiteConfig>  lstCfgTarget = lstCfgsAll.stream().filter(cfg -> strName.equals( cfg.testSuite.name) ).toList();
        if (!lstCfgTarget.isEmpty())
            return lstCfgTarget.getFirst();
        
        // Check if name is a valid file location (name is not a pre-defined test suite)
        String  strErrMsg1 = JavaRuntime.getQualifiedMethodNameSimple()
                + " - Invalid test suite name '" + strName 
                + "': neither a named pre-defined test suite nor a valid file location.";
        try {
            Paths.get(strName);     // throws exceptions
            
        } catch (InvalidPathException | NullPointerException e) {
            
            if (BOL_LOGGING)
                LOGGER.error(strErrMsg1);
            
            throw new IllegalArgumentException(strErrMsg1, e);
        }
        
        // Check if name is a valid file
        File file = new File(strName);
        
        if (!file.exists() || !file.isFile()) {
            if (BOL_LOGGING)
                LOGGER.error(strErrMsg1);
            
            throw new IllegalArgumentException(strErrMsg1);
        }
       
        // Attempt to load test suite configuration from file (name is a valid file)
        try {
            JalRequestSuiteConfig cfg = CfgLoaderYaml.load(file, JalRequestSuiteConfig.class);
            
            return cfg;
            
        } catch (FileNotFoundException | SecurityException e) {
            String  strErrMsg2 = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Input " + strName + " was not a valid test suite configuration file.";
            
            if (BOL_LOGGING)
                LOGGER.error(strErrMsg2);
            
            throw new IllegalArgumentException(strErrMsg2, e);
        }
    }
    
    /**
     * <p>
     * Creates a new output stream connected to a new output file at the given path location.
     * </p>
     * <p>
     * A new output file is created with name given as a concatenation of the simple class name and the 
     * current time instant.  The time instant addition avoids collisions with any other output files at 
     * that location.
     * </p>
     * <p>
     * A new <code>PrintStream</code> object is created that is attached to the above file.  
     * <s>A header is written identifying the file contents and time of creation.</s>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned stream should be closed when no longer needed.
     * </p> 
     * 
     * @param strPath   path location for the new output file
     * 
     * @return  a new output print stream connected to the new output file
     * 
     * @throws UnsupportedOperationException    file path is not associated with default provider
     * @throws FileNotFoundException    unable to create output file (see message and cause)
     * @throws SecurityException        unable to write to file
     */
    private static PrintStream  createOutputStream(String strPath) 
            throws UnsupportedOperationException, FileNotFoundException, SecurityException {

        // Check if argument is "console"
        String  strConsole = strPath.toLowerCase();
        if (strConsole.equals(STR_OUTPUT_CONSOLE))
            return System.out;
        
        // Check if argument is directory - if so create unique file name
        Path    pathOutput = Path.of(strPath);
        
        // TODO - Remove
//        System.out.println("pathOutput = " + pathOutput);
//        System.out.println("Files.isDiretory(pathOutput) = " + Files.isDirectory(pathOutput));
        if (Files.isDirectory(pathOutput)) {
            String  strFileName = createUniqueOutputFileName();
            
            pathOutput = Paths.get(strPath, strFileName);
        } 
        
        // Open the common output file
        File        fileOutput = pathOutput.toFile();       // throws UnsupportedOperationException
        PrintStream psOutput = new PrintStream(fileOutput); // throws FileNotFoundException, SecurityException
        
        // Return new stream connected to new output file
        return psOutput;
    }
    
    /**
     * <p>
     * Creates a unique output file name for the output report.
     * </p>
     * <p>
     * The returned file name is constructed from the class simple name, the current time instant,
     * and the output file extension <code>{@link #STR_FILE_EXT_DEF}</code>.
     * </p>
     * 
     * @return  new, unique, file name for output file
     */
    private static String   createUniqueOutputFileName() {
        String      strCls = QueryChannelEvaluatorDeprecated.class.getSimpleName();
        Instant     insNow = Instant.now();
        String      strNow = insNow.toString();
        String      strExt = STR_OUTPUT_FILE_EXT;
        
        String  strFileNm  = strCls + "-" + strNow + strExt;
        
        return strFileNm;
    }
    
    /**
     * <p>
     * Reports the given exception to Standard Error and to the event logger if enabled.
     * </p>
     * 
     * @param e the exception to report
     */
    private static void reportException(Throwable e) {
        String  strHdr = QueryChannelEvaluatorDeprecated.class.getSimpleName() + " FAILURE.";
        String  strMsg = "Encountered exception " + e.getClass().getSimpleName() + ": " + e.getMessage() + ".";
        String  strBye = "Unable to continue. Exiting...";
        
        System.err.println(strHdr);
        System.err.println(strMsg);
        System.err.println(strBye);
        
        if (BOL_LOGGING)
            LOGGER.error(strMsg);
    }
    
}
