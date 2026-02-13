/*
 * Project: dp-jal
 * File:	FrameProcessorEvaluator.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.frame
 * Type: 	FrameProcessorEvaluator
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
 * @since Sep 13, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.frame;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.MalformedParametersException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.UUID;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;

import com.ospreydcs.dp.jal.appfwk.ExitCode;
import com.ospreydcs.dp.jal.appfwk.JalApplicationBase;
import com.ospreydcs.dp.jal.common.ProviderUID;
import com.ospreydcs.dp.jal.config.JalConfig;
import com.ospreydcs.dp.jal.config.ingest.JalIngestionConfig;
import com.ospreydcs.dp.jal.ingest.model.frame.IngestionFrameProcessor;
import com.ospreydcs.dp.jal.tools.apps.query.channel.QueryChannelEvaluator;
import com.ospreydcs.dp.jal.tools.common.parse.AppArgumentsParser;
import com.ospreydcs.dp.jal.tools.common.parse.AppOptionsParser;
import com.ospreydcs.dp.jal.tools.common.score.DataRateLister;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;
import com.ospreydcs.dp.jal.util.Log4j;

/**
 * <p>
 * Application for evaluating the <code>IngestionFrameProcessor</code> class under various test conditions.
 * </p>
 * <p>
 * The objective of this application is perform evaluations of the <code>{@link IngestionFrameProcessor}</code>
 * component of the JAL Ingestion API.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Sep 13, 2025
 *
 * @see #IngestionFrameProcessor
 */
public class FrameProcessorEvaluator extends JalApplicationBase<FrameProcessorEvaluator> {

    
    //
    // Application Entry 
    //
    
    /**
     * <p>
     * Entry point for the application.
     * </p>
     * <p>
     * See class documentation and class constants <code>{@link #STR_APP_DESCR}</code> for application description 
     * and <code>{@link #STR_APP_USAGE}</code> for details on command-line arguments and usage.
     * </p>
     * 
     * @param args  command-line arguments as described in <code>{@link #STR_APP_USAGE}</code>
     */
    public static void main(String[] args) {

        //
        // ------- Special Requests -------
        //
        
        // Check for client help request
        if (PARSER.hasHelpRequest(args)) {
            System.out.println(STR_APP_USAGE);
            
            System.exit(ExitCode.SUCCESS.getCode());
        }
        
        // Check for client version request
        if (PARSER.hasVersionRequest(args)) {
            System.out.println(STR_APP_VERSION);
            
            System.exit(ExitCode.SUCCESS.getCode());
        }

        //
        // ------- Application Initialization -------
        //
        
//        // Check for general command-line errors
//        try {
//            PARSER.hasOptionErrors(CNT_APP_MIN_ARGS, LST_STR_DELOPTS, args);
//
//        } catch (Exception e) {
//            JalApplicationBase.terminateWithException(FrameProcessorEvaluator.class, e, ExitCode.INPUT_CFG_CORRUPT);
//
//        }

        // Get the output location
        String      strOutputLoc = PARSER.parseOutputLocation(STR_OUT_PATH_DEF, args);
        
        // Create the test suite from the command-line arguments
        FrameProcTestSuite  suiteTests;
        try {
            suiteTests = FrameProcessorEvaluator.parseTestSuite(args);
            
        } catch (Exception e) {
            JalApplicationBase.terminateWithException(FrameProcessorEvaluator.class, e, ExitCode.INTPUT_ARG_INVALID);
            return;
            
        }
        
        //
        // ------- Application Execution -------
        //
        
        // Create the evaluator, run it while catching and reporting any exceptions
        try {
            FrameProcessorEvaluator   evaluator = new FrameProcessorEvaluator(suiteTests, strOutputLoc, args);
            
            evaluator.run();
            evaluator.writeReport();
            evaluator.close();
            
            System.out.println(STR_APP_NAME + " Execution completed in " + evaluator.getRunDuration());
            System.out.println("  Results stored at " + evaluator.getOutputFilePath().toAbsolutePath());
            System.exit(ExitCode.SUCCESS.getCode());
            
        } catch (IllegalStateException | MissingResourceException | ClassCastException | UnsupportedOperationException | IndexOutOfBoundsException | FileNotFoundException e) {

            // Creation exception
            JalApplicationBase.terminateWithException(FrameProcessorEvaluator.class, e, ExitCode.INITIALIZATION_EXCEPTION);
            return;
            
        } catch (SecurityException e) {

            // Shutdown exception
            JalApplicationBase.terminateWithException(QueryChannelEvaluator.class, e, ExitCode.SHUTDOWN_EXCEPTION);
            return;
        }
    }

    
    //
    // Library Resources
    //
    
    /** Default configuration parameters for the Ingestion Service tools */
    private static final JalIngestionConfig     CFG_INGEST = JalConfig.getInstance().ingest;
    
    /** Default configuration parameters for the JAL Tools */
    private static final JalToolsConfig         CFG_TOOLS = JalToolsConfig.getInstance();

    
    
    //
    // Application Constants - Command-Line Arguments and Messages
    //
    
    /** Minimum number of application arguments - argument name and at least one data request */
    public static final int         CNT_APP_MIN_ARGS = 1;
    
    
    /** Default output path location */
    public static final String      STR_OUT_PATH_DEF = CFG_TOOLS.output.path + "/ingest/frame";
    
    /** Argument delimited variable for data columns serialization enable/disable flags */
    public static final String      STR_PARSE_SERIAL_ENBL_DVAR = "--serial";
    
    /** Argument delimited variable for concurrent processing enable/disable flags */
    public static final String      STR_PARSE_MTHRD_ENBL_DVAR = "--mthrd";
    
    /** Argument delimited variable for frame decomposition enable/disable flags */
    public static final String      STR_PARSE_DCMP_ENBL_DVAR = "--dcmp";
  
    
    /** Argument delimited variable containing the input file location */
    public static final String      STR_PARSE_INPUT_DVAR = "--input";
    
    /** Argument delimited variable containing maximum thread count value(s) */
    public static final String      STR_PARSE_THRD_CNT_DVAR = "--threads";
    
    /** Argument delimited variable containing maximum composite ingestion frame size(s) */
    public static final String      STR_PARSE_DCMP_SZ_DVAR = "--szfrm";

    
    /** Argument delimited variable defining an ingestion frame */
    public static final String      STR_PARSE_FRM_SPEC_DVAR = "--frame";
    
    /** Argument delimited variable containing ingestion frame count */
    public static final String      STR_PARSE_FRM_CNT_DVAR = "--nfrms";
    
    
    /** Argument delimited variable containing output location */
    public static final String      STR_PARSE_OUTPUT_DVAR = "--output";

    /** List of all the valid delimited argument options */
    public static final List<String>    LST_STR_DELOPTS = List.of(
            STR_PARSE_SERIAL_ENBL_DVAR,
            STR_PARSE_MTHRD_ENBL_DVAR,
            STR_PARSE_DCMP_ENBL_DVAR,
            STR_PARSE_INPUT_DVAR,
            STR_PARSE_THRD_CNT_DVAR, 
            STR_PARSE_DCMP_SZ_DVAR, 
            STR_PARSE_FRM_SPEC_DVAR,
            STR_PARSE_FRM_CNT_DVAR,
            STR_PARSE_OUTPUT_DVAR
            );
    
    
    //
    // Application Constants - Evaluation Parameters
    //
    
    /** The targeted data rate (in MBps) - used in {@link #writeReport(PrintStream)} */
    public static final double      DBL_RATE_TARGET = 500;
    
    /** The targeted processing duration - used in {@link #writeReport(PrintStream)} */
    public static final Duration    DUR_PROC_TARGET = Duration.ofMillis(10);
    
    
    //
    // Application Constants - Client Messages
    //
    
    /** Application name */
    public static final String      STR_APP_NAME = FrameProcessorEvaluator.class.getSimpleName();
    
    /** A laconic description of the application function */
    public static final String      STR_APP_DESCR = 
            STR_APP_NAME + " Description \n"
          + "- Application evaluates the performance and operation of the IngestionFrameProcessor component class \n"
          + "    for converting a stream of IngestionFrame objects into a stream of IngestDataRequest messages. \n"
          + "- A payload of IngestionFrame objects is first created according to the command-line arguments. \n"
          + "    The payload is fed to an IngestionFrameProcessor instance as fast as accepted. The processed \n "
          + "    messages are recovered and the performance is recorded.\n"
          + "- No further processing is performed; that is, the messages are not sent to the Ingestion Service. \n";
    
    
    /** The "usage" message for client help requests or invalid application arguments */
    public static final String      STR_APP_USAGE = 
            STR_APP_NAME  + " Usage: \n"
          + "\n"
          + "% " + STR_APP_NAME
          + AppArgumentsParser.displayCommandLineHelpOptions()
          + AppArgumentsParser.displayCommandLineVersionOptions()
          + " [" + STR_PARSE_INPUT_DVAR + " input]"
          + "\n"
          + " "
          + " [" + STR_PARSE_SERIAL_ENBL_DVAR + " FALSE ... TRUE]"
          + "\n"
          + " "
          + " [" + STR_PARSE_MTHRD_ENBL_DVAR + " FALSE ... TRUE]"
          + " [" + STR_PARSE_THRD_CNT_DVAR + " T1 ... Tn]"
          + "\n"
          + " "
          + " [" + STR_PARSE_DCMP_ENBL_DVAR + " FALSE ... TRUE]"
          + " [" + STR_PARSE_DCMP_SZ_DVAR + " M1 ... Mn]"
          + "\n"
          + " "
          + " [" + STR_PARSE_FRM_SPEC_DVAR + " 'frame_1 parameters'"
          + " " + STR_PARSE_FRM_SPEC_DVAR + " 'frame_2 parameters'" 
          + " ... " + STR_PARSE_FRM_SPEC_DVAR + " 'frame_n parameters']"
          + "\n"
          + "  "
          + "[" + STR_PARSE_FRM_CNT_DVAR + " N1 ... Nn]" 
          + "\n"
          + " " + AppArgumentsParser.displayComandLineOutputLocationOption()
          + "\n\n" 
          + "  Where  \n"
          + "   " + AppArgumentsParser.displayCommandLineHelpOptions() + "    = print this message and return. \n"
          + "   " + AppArgumentsParser.displayCommandLineVersionOptions() + " = prints application version information and return. \n"
          + "    input            = Optional input file location - if present all the following arguments are contained there. \n"
          + "    " + STR_PARSE_SERIAL_ENBL_DVAR + "         = Enable/disable data column serialization in processed messages. \n"
          + "    " + STR_PARSE_MTHRD_ENBL_DVAR + "          = Enable/disable multi-threaded processing of ingestion frames (with given maximum thread count(s). \n"
          + "    T1, ..., Tn      = Maximum allowable number(s) of concurrent processing threads - Integer value(s). \n"
          + "    " + STR_PARSE_DCMP_ENBL_DVAR + "           = Enable/disable ingestion frame decomposition (with given maximum size(s)). \n"
          + "    M1, ..., Mn      = Maximum allowable composite frame size (bytes) after decomposition - Integer value(s). \n"
          + "    " + STR_PARSE_FRM_SPEC_DVAR + "          = Delimits the ingestion frame definition(s) within quotes ' ' (i.e., frame_1, ..., frame_n parameters). \n"
          + "    frame parameters = Collection of parameters defining ingestion frame (see below). \n"
          + "    N1, ..., Nn      = Number of frames to process for each ingestion frame (i.e., frame_1, ..., frame_n). \n"
          + "    output           = output directory w/wout file path, or '" + STR_ARG_VAL_STDOUT + "'. \n"
          + "\n"
          + "  GERNAL NOTES: \n"
          + "  - All bracketed quantities [...] are optional. \n"
          + "  - The " + STR_PARSE_INPUT_DVAR + " option is available for evaluations with large number of command-line parameters. \n"
          + "  - If the " + STR_PARSE_INPUT_DVAR + " option is present all other command-line parameter are ignored. \n"
          + "  - <--Boolean valued variables (e.g., " + STR_PARSE_SERIAL_ENBL_DVAR + ", " + STR_PARSE_MTHRD_ENBL_DVAR + ", etc.) default to FALSE if not present. --> \n"
          + "  - Ingestion frame definition(s) (i.e., frame_1, ..., frame_n parameters) REQUIRE single quote ' ' for containment. \n"
          + "  - <-- At least one ingestion frame must be defined; specifically, " + STR_PARSE_FRM_SPEC_DVAR + " must appear at least once on the command line. --> \n"
          + "  - If " + STR_PARSE_FRM_CNT_DVAR + " is present the values define the payload size each frame definition provided. \n"
          + "  - If any value(s) are not provided for " + STR_PARSE_THRD_CNT_DVAR + " and/or " + STR_PARSE_DCMP_SZ_DVAR + ", default values are provided. \n"
          + "  - Default 'output' value is " + STR_OUT_PATH_DEF + ".\n"
          + "  - All other default values are taken from the JAL Ingestion default configuration and the JAL Tools ingestion frame default configuration. \n"
          + "\n"
          + " INGESTION FRAME DEFINITION: \n"
          + "  Ingestion frames are defined with the 'frame parameters' section after the " + STR_PARSE_FRM_SPEC_DVAR + " delimiter. \n"
          + "  For full description of these parameters see the class documentation for record FrameFactorySpec. \n"
          + "  Briefly, we have the following format for the ingestion frame definition: \n" 
          + "\n"
          + "  'frame parameters' = \n"
          + "    [-tagsDef] [-tagsCls] [-attrsDef] [-attrsCls] \n" 
          + "    [--label prefix] [--tags tag1 ... tagN] [-Anm1=val1 ... -AnmN=valN] \n" 
          + "    [--tms [samples [period [start [CASE [delay]]]]] ] \n" 
          + "    [--cols [cnt [prefix [DTYPE [parameter(s)]]]] ] \n" 
          + "    [   ...                                       ] \n" 
          + "    [--cols [cnt [prefix [DTYPE [parameter(s)]]]] ] \n"
          + "    [--cols [colNm1 colNm2 ... colNmN] [DTYPE [parameter(s)]] ] \n"  
          + "    [   ...                                                   ] \n" 
          + "    [--cols [colNm1 colNm2 ... colNmN] [DTYPE [parameter(s)]] ] \n"
          + "\n"
          + "  Where"
          + "    -tagsDef    = Include default ingestion frame tag values within each frame. \n"
          + "    -tagsCls    = Include ingestion frame factory class tag values within each frame. \n"
          + "    -attrsDef   = Include default ingestion frame (name value) attribute pairs within each ingestion frame. \n"
          + "    -attrsCls   = Include ingestion frame factory (name, value) attribute pairs within each ingestion frame. \n"
          + "    prefix      = Ingestion frame label prefix given to each ingestion frame (full label is suffixed with index). \n"
          + "    tag1, ...,  = Additional tag values included within each ingestion frame. \n"
          + "    nm1, ...,   = Attribute names for additional (name, value) attribute pairs attached to each ingestion frame. \n"
          + "    val1, ...,  = Attribute values for addtional (name, value) attribute pairs attached to each ingestion frame. \n"
          + "    samples     = Number of sample values within each ingestion frame data column. \n"
          + "    period      = The sampling period in ISO-8601 duration format - 'PnYnMnDTnHnMn.nS'. \n"
          + "    start       = The sampling start time instant in ISO-8601 date/time format - 'Y-M-DTH:M:S.SZ' \n"
          + "    CASE        = The timestamp case - DpTimestampCase enumeration constant {SAMPLING_CLOCK, TIMESTAMP_LIST}. \n"
          + "    delay       = Sampling start time delay in ISO-8601 duration format - 'PnYnMnDTnHnMn.nS'. \n"
          + "    cnt         = Number of columns in the column factory specification - Integer format. \n"
          + "    prefix      = Column name prefix given to each data column in specification (full name is suffixed with column index). \n"
          + "    colNm1, ... = Explicit column names - alternate method for supplying names and column count. \n"
          + "    DTYPE       = Data column data type for all columns within specification - JalComplexType enumeration constant. \n"
          + "    parameters  = Data column datum factory parameters (see below). \n"
          + "\n"
          + "  FRAME DEFINITION NOTES: \n"
          + "  - Most parameters are optional and when not provided are supplied by the JAL Tools default ingestion frame definition. \n"
          + "  - Note that, along with metadata, ingestion frame definitions include both a timestamps definition using the \n"
          + "    --tms option and data columns collections definitions using the --cols option. \n"
          + "  - If the --tms option is not present the frame timestamps specification is taken from the default JAL Tools configuraiton. \n"
          + "  - Column counts and names can be specified either with the count and prefix option, or with explicit names (the number determine count). \n"
          + "\n"
          + "  DATUM FACTORY SPECIFICATION: \n"
          + "   Ingestion frame data columns are populated according to the 'parameter(s)' options collection after the --cols delimiter. \n"
          + "   Data columns are populated with simulated data generated from 'datum factories' whose data type is given by DTYPE. \n"
          + "   Thus, these parameters are particular to the datum factory specification determined by the DTYPE value. \n"
          + "   Specifically, we have the following assignments for datum factory specifications: \n"
          + "     JalComplexType#SCALAR -> ScalarFactorySpec. \n"
          + "     JalComplexType#BYTES -> ByteArrayFactorySpec. \n"
          + "     JalComplexType#TIMESTAMP-> TimestampFactorySpec. \n"
          + "     JalComplexType#IMAGE -> ImageFactorySpec. \n"
          + "     JalComplexType#TENSOR -> TensorFactorySpec. \n"
          + "     JalComplexType#STRUCTURE -> StructureFactorySpec. \n"
          + "   Thus, refer to the class documentation for the above datum factory specification for exact parameter details. \n"
          + "\n"
          + "  DATUM FACTORY NOTES: \n"
          + "  - If no parameters are given for the datum factory specification (that is, only the DTYPE parameter is present) \n"
          + "    then the datum factory is configured according to the default configuration in the JAL Tools configuration. \n"
          + "  - Typically, the default datum factory configuration is sufficient for most IngestionFrameProcessor evaluations. \n"
          ;

    
    //
    // Application Resources
    //
    
    // Create an arguments PARSER
    private static final AppOptionsParser     PARSER = AppOptionsParser.from(LST_STR_DELOPTS);
    
    
    /** The "version" message for client version requests */
    public static final String      STR_APP_VERSION = 
            STR_APP_NAME
          + " version 1.0: compatible with Java Application Library version 1.10.0 or greater.";
    
    

    //
    // Class Resources
    //
    
    /** Class event logging flag */
    private static boolean          BOL_LOGGING = CFG_INGEST.logging.enabled;
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLogger(FrameProcessorEvaluator.class, CFG_INGEST.logging.level);

    
    //
    // Defining Attributes
    //
    
    /** The test suite configuration to run */
    private final FrameProcTestSuite        suiteCases;
    
//    /** The output location to write evaluation results */
//    private final String                    strOutputLoc;
    
    
    // 
    // Instance Resources
    //
    
    /** The collection of test cases to run, i.e., the test case suite */
    private final Collection<FrameProcTestCase>     conCases;
    
    /** The collection of test case results */
    private final Collection<FrameProcTestResult>   conResults;
    
    /** The collections of test case failures */
    private final Collection<FrameProcTestResult>   conFailures;
    
    
    /** The data provider UID used in the ingestion frame processor */
    private final ProviderUID                       uidProvider = ProviderUID.from(UUID.randomUUID().toString(), STR_APP_NAME, false);
    
    /** The ingestion frame processor under evaluation */
    private final IngestionFrameProcessor           processor;
    
    
    //
    // State Variables
    //
    
    /** The total duration of the test suite evaluations */
    private Duration        durEval;
    
    
    //
    // JalApplicationBase Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.jal.appfwk.JalApplicationBase#isLogging()
     */
    @Override
    protected boolean isLogging() {
        return BOL_LOGGING;
    }

    /**
     * @see com.ospreydcs.dp.jal.appfwk.JalApplicationBase#getLogger()
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
     * Constructs a new <code>FrameProcessorEvaluator</code> instance.
     * </p>
     *
     * @param suiteCases    the test suite configuration to run
     * @param strOutputLoc  the file/path location to store the test results
     * @param args          the application command-line arguments
     * 
     * @throws IllegalStateException    invalid test suite configuration (missing at least one parameter value)
     * @throws MissingResourceException attempted to make a <code>TestCase</code> with missing parameter and/or parameter value
     * @throws ClassCastException       test case parameter value had invalid type  
     * @throws UnsupportedOperationException an unknown parameter was encountered
     * @throws IndexOutOfBoundsException     internal error - attempted to compute test case greater than the number of cases
     * @throws FileNotFoundException    unable to create output file (see message and cause)
     * @throws SecurityException        unable to write to output file
     */
    public FrameProcessorEvaluator(FrameProcTestSuite suiteCases, String strOutputLoc, String... args)
        throws IllegalStateException, MissingResourceException, ClassCastException, UnsupportedOperationException, IndexOutOfBoundsException, FileNotFoundException, SecurityException
    {
        super(FrameProcessorEvaluator.class, args);
        
        this.suiteCases = suiteCases;
//        this.strOutputLoc = strOutputLoc;
        
        // Create the ingestion frame processor under evaluation
        this.processor = IngestionFrameProcessor.create(uidProvider);
        
        // Create the collection of test cases and container for results
        this.conCases = this.suiteCases.createTestSuit();   // throws IllegalStateException, MissingResourceException, ClassCastException, UnsupportedOperationException, IndexOutOfBoundsException
        this.conResults = new TreeSet<>(FrameProcTestResult.descendingProcessedRateOrdering());
        this.conFailures = new TreeSet<>(FrameProcTestResult.caseIndexOrdering());
        
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
     * Runs all test cases within the test suite configuration on the <code>IngestionFrameProcessor</code> object under evaluation.
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
    public void run() throws IllegalStateException {
        
        // Check state
        if (super.bolRun) 
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() 
                                            + " - Evaluations have already been run.");
        
        // Initialize
        final int     CNT_CASES = this.conCases.size();
        this.bolRun = true;
        
        // Run all test cases on  QueryChannel subject
        int indCase = 1;
       
        LOGGER.info("Running {} test cases for test suite...", CNT_CASES);
        Instant insStart = Instant.now();
        for (FrameProcTestCase recCase : this.conCases) {
            LOGGER.info("Running test case #{} of {} (with index {}) ...", indCase, CNT_CASES, recCase.indCase());
            
            FrameProcTestResult recResult = recCase.evaluate(this.processor); // throws exceptions
            
            this.conResults.add(recResult);
            indCase++;
        }
        Instant insFinish = Instant.now();
        
        // Collect any test failures
        this.conFailures.addAll( this.conResults.stream().filter(rec -> rec.recTestStatus().isFailure()).toList() );

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
        
        // Print out definitions
        ps.println(this.getClass().getSimpleName() +  " Definitions");
        ps.println(strPad + "Processed Data Rate     - Total data message allocation divided by payload processing time.");
        ps.println(strPad + "Raw Data Rate           - Total payload allocation divided by payload processing time.");
        ps.println(strPad + "Payload allocation      - Total memory allocation size of all ingestion frames to be processed.");
        ps.println(strPad + "Data message allocation - Total memory allocation size of all (processed) data messages.");
        ps.println();
        
        // Print out test parameter descriptions
        EnumSet<FrameProcTestParam> setParams = EnumSet.allOf(FrameProcTestParam.class);
        int     szNmMax = setParams.stream().<String>map(Enum::name).mapToInt(String::length).max().getAsInt();
        String  strFmt = "%s%-" +  szNmMax + "s - %s.";
        ps.println("Test Parameter Descriptions");
        setParams.forEach(p -> ps.println(String.format(strFmt, strPad, p.name(), p.getParameterDescription())));
        ps.println();
        
        // Print out evaluation summary
        ps.println("Evaluation Summary");
        ps.println(strPad + "Test cases specified : " + this.conCases.size());
        ps.println(strPad + "Test cases run       : " + this.conResults.size());
        ps.println(strPad + "Test case failures   : " + this.conFailures.size());
        ps.println(strPad + "Evaluation duration  : " + this.durEval);
        ps.println(strPad + "Evaluation completed : " + this.bolCompleted);
        ps.println();
        
        
        // Print out the test suite configuration
        ps.println("Test Suite Configuration");
        this.suiteCases.printOut(ps, strPad);
        ps.println();
        
        // Print out test case data rates
        ps.println("Test Case Processed Data Rates (MBps Descending)");
        DataRateLister<FrameProcTestResult>  lstrProcRates = DataRateLister.from(
                rec -> rec.recTestCase().indCase(), 
                rec -> rec.recTestCase().specFrame().strLabel(), 
                rec -> rec.szProcessed(), 
                rec -> rec.dblRateProc()
                );
        lstrProcRates.printOut(ps, strPad, this.conResults);
        ps.println();
        
        ps.println("Test Case Raw Data Rates (MBps Descending)");
        DataRateLister<FrameProcTestResult>  lstrRawRates = DataRateLister.from(
                rec -> rec.recTestCase().indCase(), 
                rec -> rec.recTestCase().specFrame().strLabel(), 
                rec -> rec.szPayload(), 
                rec -> rec.dblRateRaw()
                );
        lstrRawRates.printOut(ps, strPad, this.conResults);
        ps.println();
        
        // Print out results summary
        ps.println("Test Results Statistics");
        FrameProcResultStats.assignTargetDataRate(DBL_RATE_TARGET);
        FrameProcResultStats.assignTargetProcessingDuration(DUR_PROC_TARGET);
        FrameProcResultStats  recSummary = FrameProcResultStats.from(this.conResults);
        recSummary.printOut(ps, strPad);
        ps.println();
        
        // Print out results extremes
        ps.println("Test Results Extremes");
        FrameProcResultExtremes  recExtremes = FrameProcResultExtremes.from(this.conResults);
        recExtremes.printOut(ps, null);
        ps.println();
        
        // Print out channel configuration scoring
        ps.println("Frame Processor Configuration Scoring");
        FrameProcConfigScorer scrChan = FrameProcConfigScorer.from(this.conResults);
        scrChan.printOutByRates(ps, strPad);
        ps.println();
        
        // Print out failed test results 
        ps.println("Failed Cases (By Index)");
        if (this.conFailures.isEmpty()) {
            ps.println(strPad + "None");
            ps.println();
            
        } else {
            for (FrameProcTestResult recFail : this.conFailures) {
                recFail.printOut(ps, strPad);
                ps.println();
            }
        }
        
        // Print out each test result
        ps.println("Individual Case Results (MBps Descending Procesed Rates)");
        for (FrameProcTestResult recResult : this.conResults) {
            recResult.printOut(ps, strPad);
            ps.println();
        }
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Parses the application command-line arguments for the test suite configuration and returns it.
     * </p>
     * <p>
     * The method iterates through the test suite parameters as enumerated in <code>{@link FrameProcTestParam}</code>.
     * The values for each parameter are extracted from the command line parameters and added to the returned
     * test suite configuration.  If the command line does not provide values for a parameter the default value
     * is assigned as given by <code>{@link FrameProcTestParam#getDefaultValue()}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The string values within the command line are converted to <code>Object</code> values of the appropriate
     * type using <code>{@link FrameProcTestParam#parseValue(String)}</code>.  This method is the source of all
     * exceptions thrown.
     * </p>
     * 
     * @param args  the application command-line arguments
     * 
     * @return  the test suite configuration according to the command-line arguments
     * 
     * @throws NoSuchMethodException    the Java class <code>{@link #getJavaType()}</code> does not contain method <code>valueOf(String)</code>
     * @throws SecurityException        the class loader denied access to method <code>valueOf(String)</code> (e.g., typically package access)
     * @throws IllegalAccessException   the method <code>valueOf(String)</code> is not accessible
     * @throws InvocationTargetException    the <code>valueOf(String)</code> method threw an exception (e.g., NumberFormatException)
     * @throws IllegalArgumentException general error (typically bad argument count or enumeration constant not recognized)
     * @throws DateTimeParseException   invalid ISO-8605 date/time/duration format for 'period', 'start', or 'delay' 
     * @throws TypeNotPresentException  invalid enumeration constant (e.g., the 1st argument was not a <code>JalComplexType</code>)
     * @throws NumberFormatException    invalid numeric expression (typically for 'lngSeed' value)
     * @throws ConfigurationException   the argument contained the wrong number of arguments for the <code>JalComplexType</code>
     * @throws UnsupportedOperationException invalid field value format (typically 'numIncr' was invalid)
     * @throws MalformedParametersException  an enumeration constant within the argument set was not recognized (IMAGE)
     * @throws NoSuchElementException   the column data type was unrecognized (i.e., 'DTYPE' was not supported)
     */
    private static FrameProcTestSuite   parseTestSuite(String...args) 
            throws UnsupportedOperationException, NoSuchMethodException, SecurityException, IllegalAccessException, 
            InvocationTargetException, DateTimeParseException, NumberFormatException, IllegalArgumentException, 
            TypeNotPresentException, ConfigurationException, MalformedParametersException 
    {
        
        // Create the empty test suite 
        FrameProcTestSuite      suite = FrameProcTestSuite.from();
        
        // For each parameter
        for (FrameProcTestParam enmParam : FrameProcTestParam.values()) {
        
            // Parse the command line for parameter values
            List<String>    lstStrVals = PARSER.parseVariable(enmParam.getDelimitedVariableName(), args);
            
            // If empty use default parameter value
            if (lstStrVals.isEmpty()) {
                suite.addParameterValue(enmParam, enmParam.getDefaultValue());
                
                continue;
            }
            
            // Otherwise convert parameter value strings to value objects and add to test suite
            for (String strVal : lstStrVals) {
                Object  objVal = enmParam.parseValue(strVal);
                
                suite.addParameterValue(enmParam, objVal);
            }
        }
        
        return suite;
    }

}
