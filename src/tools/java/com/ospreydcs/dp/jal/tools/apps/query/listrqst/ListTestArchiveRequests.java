/*
 * Project: dp-jal
 * File:	ListTestArchiveRequests.java
 * Package: com.ospreydcs.dp.jal.tools.apps.query.listrqst
 * Type: 	ListTestArchiveRequests
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.jal.config.JalConfig;
import com.ospreydcs.dp.jal.config.query.JalQueryConfig;
import com.ospreydcs.dp.jal.tools.appfwk.ExitCode;
import com.ospreydcs.dp.jal.tools.appfwk.JalApplicationBase;
import com.ospreydcs.dp.jal.tools.common.parse.AppOptionsParser;
import com.ospreydcs.dp.jal.tools.common.requests.TestArchiveRequest;
import com.ospreydcs.dp.jal.tools.common.requests.TestRequestType;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.output.JalToolsOutputConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;
import com.ospreydcs.dp.jal.util.Log4j;

/**
 * <p>
 * Application for displaying the available, pre-configured Data Platform Test Archive time-series data requests.
 * </p>
 * <p>
 * Application provides several method for listing the available Test Archive time-series data requests in enumeration 
 * <code>{@link TestArchiveRequest}</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 3, 2026
 *
 */
public class ListTestArchiveRequests extends JalApplicationBase<ListTestArchiveRequests> {

    
    //
    // Application Entry Point
    //
    
    /**
     * <p>
     * Entry point for application <code>ListTestArchiveRequests</code>.
     * </p>
     * 
     * @param args  command-line arguments for application
     */
    public static void main(String[] args) {

        //
        // ------- Special Requests -------
        //
        
        // Check for client help request
        if (PARSER.hasHelpRequest(args)) {
            System.out.println(STR_APP_DESCR);
            System.out.println(STR_APP_USAGE);
            
            System.exit(ExitCode.SUCCESS.getCode());
        }
        
        // Check for client version request
        if (PARSER.hasVersionRequest(args)) {
            System.out.println(STR_APP_VERSION);
            System.out.println(STR_APP_DESCR);
            
            System.exit(ExitCode.SUCCESS.getCode());
        }

        // Error check
        try {
            PARSER.hasOptionErrors(CNT_APP_MIN_ARGS, LST_STR_DELOPTS, args);

        } catch (IllegalArgumentException | IllegalCallerException | UnsupportedOperationException e) {
            System.out.println(STR_APP_USAGE);
            JalApplicationBase.terminateWithException(ListTestArchiveRequests.class, e, ExitCode.INTPUT_ARG_INVALID);
            return;
            
        }

        //
        // ----- Application Initialization -----
        //
        
        // Get constructor arguments
        ListTestRequests    recRqsts;
        String              strOutputLoc;
        try {
            recRqsts = ListTestRequests.parse(args);
            strOutputLoc = PARSER.parseOutputLocation(STR_OUT_PATH_DEF, args);
            
        } catch(Exception e) {
            JalApplicationBase.terminateWithException(ListTestArchiveRequests.class, e, ExitCode.INTPUT_ARG_INVALID);
            return;
            
        }

        //
        // ------- Application Execution -------
        //
        
        // Create the evaluator, run it while catching and reporting any exceptions
        try {
            ListTestArchiveRequests   evaluator = new ListTestArchiveRequests(recRqsts, strOutputLoc, args);
            
            evaluator.run();
            evaluator.writeReport();
            
            System.out.println(STR_APP_NAME + " Execution completed.");
            System.out.println("  Results stored at " + evaluator.getOutputFilePath().toAbsolutePath());
            System.exit(ExitCode.SUCCESS.getCode());
            
        } catch (IllegalStateException | UnsupportedOperationException e) {

            // Creation exception
            JalApplicationBase.terminateWithException(ListTestArchiveRequests.class, e, ExitCode.INITIALIZATION_EXCEPTION);
            return;
            
        } catch (FileNotFoundException | SecurityException e) {

            // Output exception
            JalApplicationBase.terminateWithException(ListTestArchiveRequests.class, e, ExitCode.OUTPUT_FAILURE);
            return;
        }            
    }
    
    
    //
    // JAL Library Resources
    //
    
    /** The default configuration for the JAL Query API */
    private static final JalQueryConfig         CFG_QUERY   = JalConfig.getInstance().query;
    
    /** The default JAL Tools location for output */ 
    private static final JalToolsOutputConfig   CFG_OUTPUT  = JalToolsConfig.getInstance().output;

    
    //
    // Application Constants - Command-Line Arguments and Messages
    //
    
    /** Minimum number of application arguments - argument name and at least one data request */
    public static final int         CNT_APP_MIN_ARGS = 1;
    
    /** Default output path location */
    public static final String      STR_OUT_PATH_DEF = CFG_OUTPUT.path + "/query/request";
    
    
    /** List of all the valid delimited argument options */
    public static final List<String>    LST_STR_DELOPTS = Stream.concat(
                                                                ListTestRequestOptions.validDelimOptions().stream(), 
                                                                AppOptionsParser.getPredefinedOptions().stream()
                                                            ).toList();
    
    
    //
    // Application Constants - Client Messages
    //
    
    /** Application name */
    public static final String      STR_APP_NAME = ListTestArchiveRequests.class.getSimpleName();
    
    
    /** The "version" message for client version requests */
    public static final String      STR_APP_VERSION = 
            STR_APP_NAME
          + " version 1.0: compatible with Java Application Library version 1.10.0 or greater.";

    
    /** A laconic description of the application function */
    public static final String      STR_APP_DESCR = 
          "\n"
          + STR_APP_NAME + " Description \n"
          + " - Convenience application for listing all available, pre-defined time-series data requests from the \n"
          + "     Data Platform Test Archive. \n"
          + "     The Data Platform Test Archive is populated with application 'app-run-test-data-generator' which \n"
          + "     ships with the Data Platform installation archive. \n"
          + " - Predefined time-series data requests are available in enumeration \n" 
          + "     " + TestArchiveRequest.class.getName() + ". \n"
          + "     The application provides several method for listing the available Test Archive time-series data requests \n"
          + "     in enumeration TestArchiveRequest.  Requests can be listed by explicit name(s), regular expression, and/or \n"
          + "     the entire enumeration. \n"
          + " - Properties of a request are listed in detail when explicitly specifying request name. \n";
    
    
    /** The "usage" message for client help requests or invalid application arguments */
    public static final String      STR_APP_USAGE = 
            STR_APP_NAME  + " Usage: \n"
          + "\n"
          + "% " + STR_APP_NAME
          + AppOptionsParser.displayCommandLineHelpOptions()
          + AppOptionsParser.displayCommandLineVersionOptions()
          + " [" + ListTestRequestOptions.ALL.getParameterDelimOption() + "]"
          + " [" + ListTestRequestOptions.REGEX.getParameterDelimOption() + " P1 ... Pn]"
          + " [" + ListTestRequestOptions.REQUEST.getParameterDelimOption() + " R1 ... Rn]"
          + " " + AppOptionsParser.displayComandLineOutputLocationOption()
          + "\n\n" 
          + "  Where  \n"
          + "   " + AppOptionsParser.displayCommandLineHelpOptions() + "    = print this message and return. \n"
          + "   " + AppOptionsParser.displayCommandLineVersionOptions() + " = prints application version information and return. \n"
          + "    " + ListTestRequestOptions.ALL.getParameterDelimOption() + "             = lists all predefined request names in TestArchiveRequest. \n"
          + "    P1 ... Pn        = Reqular expression pattern for matching listed TestArchiveRequest name(s). \n"
          + "    R1 ... Rn        = TestArchiveRequest constant name(s) - displays request properties. \n"
          + "    output           = output directory w/wout file path, or '" + STR_ARG_VAL_STDOUT + "'. \n"
          + "\n"
          + "  GERNAL NOTES: \n"
          + "  - All bracketed quantities [...] are optional. \n"
          + "  - If switch " + ListTestRequestOptions.ALL.getParameterDelimOption() + " appears on command line all other commands are ignored. \n"
          + "  - Default 'output' value is " + STR_OUT_PATH_DEF + ".\n"
          + "  - All other default values are taken from the JAL Ingestion default configuration and the JAL Tools ingestion frame default configuration. \n"
          ;
    
    
    //
    // Application Resources
    //
    
    /** The application command-line parser */
    private static final AppOptionsParser     PARSER = AppOptionsParser.from(LST_STR_DELOPTS);
    
    
    /** The largest <code>TestArchiveRequest</code> name */
    private static final int SZ_MAX_NAME = EnumSet.allOf(TestArchiveRequest.class).stream().map(Enum::name).mapToInt(String::length).max().orElse(10);
    
    /** Format string for listing <code>TestArchiveRequest</code> constants */
    private static final String STR_FMTR_NAME = "%" + SZ_MAX_NAME + "s - %s";
    
    
    //
    // Class Resources
    //
    
    /** Class event logging flag */
    private static boolean          BOL_LOGGING = CFG_QUERY.logging.enabled;
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLogger(ListTestArchiveRequests.class, CFG_QUERY.logging.level);

    
    //
    // Defining Attributes
    //
    
    /** The Test Archive requests requested by client from command line */
    private final ListTestRequests  recRqsts;
    
    
    //
    // Instance Resources
    //
    
    /** List of <code>TestArchiveRequest</code> constants */
    private final List<String>      lstRqsts;
    
    /** List of <code>TestArchiveRequest</code> constants with properties */
    private final List<String>      lstProps;
    
    
    //
    // JalApplicationBase Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.jal.tools.appfwk.JalApplicationBase#isLogging()
     */
    @Override
    protected boolean isLogging() {
        return BOL_LOGGING;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.appfwk.JalApplicationBase#getLogger()
     */
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }


    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>ListTestArchiveRequests</code> application.
     * </p>
     * <p>
     * After construction the application output is created using <code>{@link #run()}</code>.
     * Once output is created the results can be written to the output file using <code>{@link #writeReport()}</code>. 
     * </p>
     *
     * @param recRqsts  record defining client requests
     * @param strOutputLoc  output location for results
     * @param args          application command-line arguments
     * 
     * @throws SecurityException 
     * @throws FileNotFoundException 
     * @throws UnsupportedOperationException 
     */
    public ListTestArchiveRequests(ListTestRequests recRqsts, String strOutputLoc, String... args) throws UnsupportedOperationException, FileNotFoundException, SecurityException {
        super(ListTestArchiveRequests.class, args);
        
        this.recRqsts = recRqsts;
        
        // Create output containers
        this.lstRqsts = new LinkedList<>();
        this.lstProps = new LinkedList<>();
        
        // Create the output stream and attach Logger to it - records fatal errors to output file
        super.openOutputStream(strOutputLoc); // throws SecurityException, FileNotFoundException, UnsupportedOperationException
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Runs all test cases within the test suite configuration on the <code>IngestionChannel</code> object under evaluation.
     * </p>
     * <p>
     * Runs all test cases in resource <code>{@link #conCases}</code> (i.e., specified in the test suite configuration) 
     * and records the results in resource <code>{@link #conResults}</code>.  Updates progress to the Standard Output
     * and to the output file.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This method can only be called once, regardless of the success or failure of any test cases.</li> 
     * <li>This is a blocking operation and does not return until completed.</li>
     * <li>Test case failures are recorded and reported in the output.</li>
     * </p>
     * 
     * @throws IllegalStateException    the <code>{@link #run()}</code> method has already been called
     */
    public void run() {

        // Check state
        if (super.bolRun) 
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Output has already been created.");
        
        // Create list of all TestArchiveRequests
        List<TestArchiveRequest>    lstRqstsAll = Arrays.asList(TestArchiveRequest.values());
        
        // Check for "list all" switch
        if (this.recRqsts.bolAll()) {
            List<String>    lstLines = this.createLineDescription(lstRqstsAll);
            
            this.lstRqsts.addAll(lstLines);
            super.bolRun = true;
            
            return;
        }

        // Match any regular expressions and add to constant display list
        List<TestArchiveRequest>    lstMatchesAll = new LinkedList<>();
        
        for (String strRegex : this.recRqsts.lstRegex()) {
            List<TestArchiveRequest>    lstMatches = lstRqstsAll.stream().filter(rqst -> rqst.name().matches(strRegex)).toList();
            
            lstMatchesAll.addAll(lstMatches);
        }
        this.lstRqsts.addAll(this.createLineDescription(lstMatchesAll));
        
        // Extract the TestArchiveRequest properties from any explicit requests
        String                      strPad = "  ";
        ByteArrayOutputStream       osBuffer = new ByteArrayOutputStream();
        PrintStream                 psLines = new PrintStream(osBuffer);
        
        for (TestArchiveRequest enmRqst : this.recRqsts.lstRqsts()) {
            enmRqst.printOut(psLines, strPad);
            
            String  strProps = osBuffer.toString();
            osBuffer.reset();
            this.lstProps.add(strProps);
        }
        
        super.bolRun = true;
    }
    
    /**
     * <p>
     * Creates a text report of the test suite evaluations and prints it to the output file.
     * </p>
     * <p>
     * This method is available after invoking <code>{@link #run()}</code>.  It prints out a report
     * of the application evaluations including a summary, test suite configuration, and
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
     * of the application evaluations including a summary, test suite configuration, and
     * all test case results.
     * </p>
     * 
     * @param ps    target output stream for evaluations report
     * 
     * @throws IllegalStateException    no results are available (called before <code>{@link #run()}</code>) 
     */
    public void writeReport(PrintStream ps) throws IllegalStateException {
        
        // Check state
        if (!super.bolRun)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + "- Test suite has not been run.");
        
        String  strPad = "  ";
        
        // Print out header
        String  strHdr = super.createReportHeader();
        ps.println(strHdr);
        ps.println();
        
        // Print out command line
        String  strCmdLn = super.createCommandLine();
        ps.println("Application Execution");
        ps.println(strCmdLn);
        ps.println();
        
        // Print out application options descriptions
        ps.println("Application Options Descriptions");
        ListTestRequestOptions.printOut(ps, strPad);
        ps.println();
        
        // Print out the execution log entries
        String  strLogging = super.retrieveExecutionLogEntries();
        ps.println("Execution Log Entries");
        ps.println(strLogging);
        ps.println();
        
        // Print out client request parameters
        ps.println("Client Request Parameters");
        this.recRqsts.printOut(ps, strPad);
        ps.println();
        
        // Print out the TestArchiveRequest constants
        if (!this.lstRqsts.isEmpty()) {
            ps.println("TestArchiveRequest Constants");
            for (String strLine : this.lstRqsts) 
                ps.println(strPad + strLine);
            ps.println();
        }

        // Print out the TestArchiveRequest constant properties
        if (!this.lstProps.isEmpty()) {
            ps.println("TestArchiveRequest Properties");
            for (String strProps : this.lstProps)
                ps.println(strPad + strProps);
            ps.println();
        }
    }   
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Converts each <code>TestArchiveRequest</code> constants into a string description with newline character.
     * </p>
     *  
     * @param lstRqsts  list of <code>TestArchiveRequest</code> constants for conversion
     * 
     * @return  a list of description lines for each constant
     */
    private List<String> createLineDescription(List<TestArchiveRequest> lstRqsts) {
        
        List<String>    lstLines = new LinkedList<>();
        
        for (TestArchiveRequest enmRqst : lstRqsts) {
            String          strName = enmRqst.name();
            TestRequestType enmType = enmRqst.getRequestType();
            String          strDesc = enmType.getDescription();
            
            String  strLine = String.format(STR_FMTR_NAME, strName, strDesc);
            lstLines.add(strLine);
        }
        
        return lstLines;
    }
}
