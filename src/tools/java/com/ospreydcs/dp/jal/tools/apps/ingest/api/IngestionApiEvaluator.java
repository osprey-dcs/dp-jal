/*
 * Project: dp-jal
 * File:	IngestionApiEvaluator.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.api
 * Type: 	IngestionApiEvaluator
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
 * @since Mar 6, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.api;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.jal.config.JalConfig;
import com.ospreydcs.dp.jal.config.ingest.JalIngestionConfig;
import com.ospreydcs.dp.jal.grpc.model.DpGrpcConnectionFactoryBase;
import com.ospreydcs.dp.jal.grpc.model.DpGrpcException;
import com.ospreydcs.dp.jal.ingest.IIngestionService;
import com.ospreydcs.dp.jal.ingest.IIngestionStream;
import com.ospreydcs.dp.jal.ingest.JalIngestionApiFactory;
import com.ospreydcs.dp.jal.tools.appfwk.DpServiceAddress;
import com.ospreydcs.dp.jal.tools.appfwk.DpServiceAddress.DpService;
import com.ospreydcs.dp.jal.tools.appfwk.ExitCode;
import com.ospreydcs.dp.jal.tools.appfwk.JalApplicationBase;
import com.ospreydcs.dp.jal.tools.common.parse.AppOptionsParser;
import com.ospreydcs.dp.jal.tools.common.score.DataRateLister;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;
import com.ospreydcs.dp.jal.util.Log4j;

/**
 * <p>
 * Application for evaluating the performance of the JAL Ingestion Service APIs.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Mar 6, 2026
 *
 */
public class IngestionApiEvaluator extends JalApplicationBase<IngestionApiEvaluator> {

    
    //
    // Application Entry Point
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

        // Check for input file declaration
        if (PARSER.hasVariable(STR_DVAR_INPUT, args)) {
            String  strInputFile = PARSER.parseVariable(STR_DVAR_INPUT, args).get(0);
            
            try {
                args = JalApplicationBase.readInputFileArguments(strInputFile);
                
            } catch (Exception e) {
                JalApplicationBase.terminateWithException(IngestionApiEvaluator.class, e, ExitCode.INTPUT_ARG_INVALID);
                return;
                
            }
        }

        
        //
        // ------- Application Initialization -------
        //
        
        // Get the application constructor arguments
        DpServiceAddress    addrHost;
        IngestApiTestSuite  suiteCases;
        String              strOutputLoc;
        try {
            addrHost = DpServiceAddress.parse(DpService.INGESTION, args);
            suiteCases = IngestApiTestSuite.parse(args);
            strOutputLoc = PARSER.parseOutputLocation(STR_OUT_PATH_DEF, args);
            
        } catch (Exception e) {
            JalApplicationBase.terminateWithException(IngestionApiEvaluator.class, e, ExitCode.INTPUT_ARG_INVALID);
            return;
            
        }
        
        //
        // ------- Application Execution -------
        //
        
        // Create the evaluator, run it while catching and reporting any exceptions
        try {
            IngestionApiEvaluator   evaluator = new IngestionApiEvaluator(addrHost, suiteCases, strOutputLoc, args);
            
            evaluator.run();
            evaluator.writeReport();
            evaluator.shutdown();
            
            System.out.println(STR_APP_NAME + " Execution completed in " + evaluator.getRunDuration());
            System.out.println("  Results stored at " + evaluator.getOutputFilePath().toAbsolutePath());
            System.exit(ExitCode.SUCCESS.getCode());
            
        } catch (DpGrpcException e) {
            
            // Creation exception
            JalApplicationBase.terminateWithException(IngestionApiEvaluator.class, e, ExitCode.GRPC_CONN_FAILURE);
            return;
            
        } catch (IllegalStateException | MissingResourceException | ClassCastException | UnsupportedOperationException | IndexOutOfBoundsException e) {

            // Creation exception
            JalApplicationBase.terminateWithException(IngestionApiEvaluator.class, e, ExitCode.INITIALIZATION_EXCEPTION);
            return;
            
        } catch (FileNotFoundException | SecurityException e) {

            // Output exception
            JalApplicationBase.terminateWithException(IngestionApiEvaluator.class, e, ExitCode.OUTPUT_FAILURE);
            return;
            
        } catch (InterruptedException e) {

            // Shutdown exception
            JalApplicationBase.terminateWithException(IngestionApiEvaluator.class, e, ExitCode.SHUTDOWN_EXCEPTION);
            return;
            
        }
        
    }


    //
    // JAL Library Resources
    //
    
    /** Default configuration parameters for the Ingestion Service API */
    private static final JalIngestionConfig     CFG_INGEST = JalConfig.getInstance().ingest;
    
    /** Default configuration parameters for the JAL Tools */
    private static final JalToolsConfig         CFG_TOOLS = JalToolsConfig.getInstance();

    
    //
    // Application Constants
    //
    
    /** Minimum number of application arguments - argument name and at least one data request */
    public static final int         CNT_APP_MIN_ARGS = 1;
    
    
    /** Default output path location */
    public static final String      STR_OUT_PATH_DEF = CFG_TOOLS.output.path + "/ingest/api";
    
    
    
    /** Argument delimited variable containing the input file location */
    public static final String      STR_DVAR_INPUT = "--input";
    
    
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
    public static final String      STR_APP_NAME = IngestionApiEvaluator.class.getSimpleName();
    
    
    /** The "version" message for client version requests */
    public static final String      STR_APP_VERSION = 
            STR_APP_NAME
          + " version 1.0: compatible with Java Application Library version 1.10.0 or greater.";
    
    /** A laconic description of the application function */
    public static final String      STR_APP_DESCR = 
            STR_APP_NAME + " Description \n"
          + "- Application evaluates the performance and operation of the JAL Ingestion Service APIs \n"
          + "    for transmitting a payload of IngestionFrame instances to the Data Platform Ingestion Service. \n"
          + "- A payload of IngestionFrame objects is first created according to the command-line arguments. \n"
          + "    The payload is converted into IngestDataRequest message by an IngestionFrameProcessor instance. \n"
          + "    The IngestionFrameProcessor uses all default configuration except for column serialization, an option. \n"
          + "    The ingestion frame processing is recorded and available in the results. \n "
          + "- The processed IngestDataRequest Protocol Buffers messages are then all offered to the IngestionChannel \n"
          + "    instance via a message queue buffer, the IngestionChannel is allows to send messages as fast as possible. \n"
          + "    The performance of the IngestionChannel is recorded and written to the output results. \n";
    
    
    /** The "usage" message for client help requests or invalid application arguments */
    public static final String      STR_APP_USAGE = 
            STR_APP_NAME  + " Usage: \n"
          + "\n"
          + "% " + STR_APP_NAME
          + AppOptionsParser.displayCommandLineHelpOptions()
          + AppOptionsParser.displayCommandLineVersionOptions()
          + " [" + STR_DVAR_INPUT + " input]"
          + "\n"
          + "  "
          + DpServiceAddress.displayCommandLineOptions()
          + "\n"
          + " "
          + " [" + IngestApiTestParams.INGEST_API.getParameterDelimOption() + " UNARY STREAM]"
          + "\n"
          + " "
          + " [" + IngestApiTestParams.COL_SER_ENBL.getParameterDelimOption() + " FALSE TRUE]"
          + "\n"
          + " "
          + " [" + IngestApiTestParams.DCMP_ENABLE.getParameterDelimOption() + " FALSE TRUE]"
          + " [" + IngestApiTestParams.DCMP_SIZE.getParameterDelimOption() + " D1 ... Dn]"
          + " [" + IngestApiTestParams.MTHREAD_ENABLE.getParameterDelimOption() + " FALSE TRUE]"
          + " [" + IngestApiTestParams.MTHREAD_COUNT.getParameterDelimOption() + " T1 ... Tn]"
          + "\n"
          + " "
          + " [" + IngestApiTestParams.STREAM_TYPE.getParameterDelimOption() + " FORWARD BIDIRECTIONAL]"
          + " [" + IngestApiTestParams.MSTREAM_ENBL.getParameterDelimOption() + " FALSE TRUE]"
          + " [" + IngestApiTestParams.MSTREAM_CNT.getParameterDelimOption() + " S1 ... Sn]"
          + "\n"
          + " "
          + " [" + IngestApiTestParams.FRAME_CNT.getParameterDelimOption() + " N1 ... Nn]" 
          + "\n"
          + " "
          + " [" + IngestApiTestParams.FRAME_DEF.getParameterDelimOption() + " 'frame_1 parameters'"
          + " " + IngestApiTestParams.FRAME_DEF.getParameterDelimOption() + " 'frame_2 parameters'" 
          + " ... " + IngestApiTestParams.FRAME_DEF.getParameterDelimOption() + " 'frame_n parameters']"
          + "\n"
          + " " + AppOptionsParser.displayComandLineOutputLocationOption()
          + "\n\n" 
          + "  Where  \n"
          + "   " + AppOptionsParser.displayCommandLineHelpOptions() + "    = print this message and return. \n"
          + "   " + AppOptionsParser.displayCommandLineVersionOptions() + " = prints application version information and return. \n"
          + "    input            = Optional input file location - if present all the following arguments are contained there. \n"
          + "    URL              = Ingestion Service location. \n"
          + "    port             = Ingestion Service server port to connect. \n"
          + "    " + IngestApiTestParams.COL_SER_ENBL.getParameterDelimOption() + "         = Enable/disable data column serialization in processed messages {FALSE TRUE}. \n"
          + "    D1 ... Dn        = Maximum composite ingestion frame size (bytes). \n"
          + "    " + IngestApiTestParams.MTHREAD_ENABLE.getParameterDelimOption() + "       = Enable/disable multi-threaded ingestion frame processing. \n"
          + "    T1 ... Tn        = Maximum number of concurrent ingestion frame processing threads - Integer value(s). \n"
          + "    " + IngestApiTestParams.STREAM_TYPE.getParameterDelimOption() + "         = gRPC data stream type {FORWARD BIDIRECTIONAL}. \n"
          + "    " + IngestApiTestParams.MSTREAM_ENBL.getParameterDelimOption() + "         = Enable/disable multiple, concurrent gRPC data streams {TRUE FALSE}. \n"
          + "    S1 ... Sn        = Maximum number of concurrent gRPC data streams - Integer value(s). \n"
          + "    N1, ..., Nn      = Number of payload frames for each frame type (i.e., frame_1, ..., frame_n). \n"
          + "    frame parameters = Collection of parameters defining ingestion frame (see below). \n"
          + "    output           = output directory w/wout file path, or '" + STR_ARG_VAL_STDOUT + "'. \n"
          + "\n"
          + "  GERNAL NOTES: \n"
          + "  - All bracketed quantities [...] are optional. \n"
          + "  - All optional quantities not provided default to those in the JAL API configuration or JAL Tools configuration. \n"
          + "  - The " + STR_DVAR_INPUT + " option is available for evaluations with large number of command-line parameters. \n"
          + "  - If the " + STR_DVAR_INPUT + " option is present all other command-line parameter are ignored. \n"
          + "  - Ingestion frame definition(s) (i.e., frame_1, ..., frame_n parameters) REQUIRE single quote ' ' for containment. \n"
          + "  - The N1 ... Nn values define the payload size each frame definition provided. \n"
          + "  - Default 'output' value is '" + STR_OUT_PATH_DEF + "'.\n"
          + "  - All other default values are taken from the JAL Ingestion default configuration and the JAL Tools ingestion frame default configuration. \n"
          + "\n"
          + " INGESTION FRAME DEFINITION: \n"
          + "  Ingestion frames are defined with the 'frame parameters' section after the " + IngestApiTestParams.FRAME_DEF.getParameterDelimOption() + " delimiter. \n"
          + "  For full description of these parameters see the class documentation for record FrameFactorySpec. \n"
          + "  Briefly, we have the following format for the ingestion frame definition: \n" 
          + "\n"
          + "  'frame parameters' = \n"
          + "    [-tagsDef] [-tagsCls] [-attrsDef] [-attrsCls] \n" 
          + "    [--label prefix] [--tags tag1 ... tagN] [-Anm1=val1 ... -AnmN=valN] \n" 
          + "    [--tms [samples [period [TCASE [start [delay]]]]] ] \n" 
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
          + "    TCASE       = The timestamp case - DpTimestampCase enumeration constant {SAMPLING_CLOCK, TIMESTAMP_LIST}. \n"
          + "    delay       = Sampling start time delay in ISO-8601 duration format - 'PnYnMnDTnHnMn.nS'. \n"
          + "    cnt         = Number of columns in the column factory specification - Integer format. \n"
          + "    prefix      = Column name prefix given to each data column in specification (full name is suffixed with column index). \n"
          + "    colNm1, ... = Explicit column names - alternate method for supplying names and column count. \n"
          + "    DTYPE       = Data column data type for all columns within specification - JalComplexType enumeration constant. \n"
          + "    parameters  = Data column datum factory parameters (see below). \n"
          + "\n"
          + "  FRAME DEFINITION NOTES: \n"
          + "  - Most parameters are optional and when not provided are supplied by the JAL Tools default ingestion frame definition. \n"
          + "  - Note that, along with metadata, ingestion frame definitions include both a timestamps definition using the --tms \n"
          + "    option and data columns collections definitions using the --cols option. \n"
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
    
    /** List of all the valid delimited argument options */
    public static final List<String>        LST_STR_DELOPTS = IngestApiTestParams.validDelimOptions();
    
    /** The application arguments parser - note that all predefined command-line options are also added */
    private static final AppOptionsParser   PARSER = AppOptionsParser.from(LST_STR_DELOPTS);
    
    
    //
    // Class Resources
    //
    
    /** Class event logging flag */
    private static final boolean    BOL_LOGGING = CFG_INGEST.logging.enabled;
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLogger(IngestionApiEvaluator.class, CFG_INGEST.logging.level);

    
    //
    // Defining Attributes
    //
    
    /** The Ingestion Service address */
    private final DpServiceAddress          addrHost;
    
    /** The test suite configuration to run */
    private final IngestApiTestSuite        suiteCases;
    
    
    //
    // Instance Resources
    //
    
    /** The unary Ingestion Service API */
    private final IIngestionService         apiService;
    
    /** The streaming Ingestion Service API */
    private final IIngestionStream          apiStream;
    
    
    /** The collection of test cases to run, i.e., the test case suite */
    private final Collection<IngestApiTestCase>    conCases;
    
    /** The collection of test case results */
    private final Collection<IngestApiTestResult>  conResults;
    
    /** The collections of test case failures */
    private final Collection<IngestApiTestResult>  conFailures;
    
    
    //
    // State Variables
    //
    
    /** The total duration of the test suite evaluations */
    private Duration        durEval;
    
    
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
     * Constructs a new <code>IngestionApiEvaluator</code> instance.
     * </p>
     * <p>
     * All application initialization is performed.
     * <ul>
     * <li>Interfaces to Ingestion Service APIs are created - used for all evaluations.</li>
     * <li>Test case test suite is generated.</li>
     * <li>Output file is created and opened.</li>
     * </ul>
     * </p>
     * <p>
     * After construction the application test suite can be evaluated using <code>{@link #run()}</code>.
     * Once evaluations are completed the results can be written to the output file using 
     * <code>{@link #writeReport()}</code>. 
     * Finally, the application should be shut down using 
     * <code>{@link #shutdown()}</code> or <code>{@link #shutdownNow()}</code> to release all resources
     * used in the evaluations and output.
     * </p>
     *
     * @param addrHost      the Ingestion Service host address
     * @param suiteCases    the test suite configuration
     * @param args          the application command-line arguments
     * 
     * @throws DpGrpcException general gRPC resource or connection exception
     * @throws IllegalStateException    invalid test suite configuration (missing at least one parameter value)
     * @throws MissingResourceException attempted to make a <code>TestCase</code> with missing parameter and/or parameter value
     * @throws ClassCastException       test case parameter value had invalid type  
     * @throws IndexOutOfBoundsException     internal error - attempted to compute test case greater than the number of cases
     * @throws UnsupportedOperationException either unknown parameter encountered, or output file path is not associated with default file system
     * @throws FileNotFoundException    unable to create output file (see message and cause)
     * @throws SecurityException        unable to write to output file
     */
    public IngestionApiEvaluator(DpServiceAddress addrHost, IngestApiTestSuite suiteCases, String strOutputLoc, String... args) 
            throws DpGrpcException,
                    IllegalStateException, MissingResourceException, ClassCastException,
                    IndexOutOfBoundsException, UnsupportedOperationException, 
                    FileNotFoundException, SecurityException {
        super(IngestionApiEvaluator.class, args);
        
        this.addrHost = addrHost;
        this.suiteCases = suiteCases;
        
        // Create the output stream and attach Logger to it - records fatal errors to output file
        super.openOutputStream(strOutputLoc); // throws SecurityException, FileNotFoundException, UnsupportedOperationException
        
        // Attach the class loggers of application components
        super.appendLoggingFor(DpGrpcConnectionFactoryBase.class);
//        super.appendLoggingFor(IngestionMemoryBuffer.class);
//        super.appendLoggingFor(IngestionMessageBuffer.class);
//        super.appendLoggingFor(IngestionStream.class);
        
        // Create the Ingestion Service APIs
        this.apiService = JalIngestionApiFactory.connectService(addrHost.strUrl(), addrHost.intPort());
        this.apiStream = JalIngestionApiFactory.connectStream(addrHost.strUrl(), addrHost.intPort());
        
        // Create the collection of test cases and container for results
        this.conCases = this.suiteCases.createTestSuit();   // throws IllegalStateException, MissingResourceException, ClassCastException, UnsupportedOperationException, IndexOutOfBoundsException
        this.conResults = new TreeSet<>(IngestApiTestResult.descendingTransmissionRateOrdering());
        this.conFailures = new TreeSet<>(IngestApiTestResult.caseIndexOrdering());
    }


    //
    //  Operations
    //
    
    /**
     * <p>
     * Runs all test cases within the test suite configuration on the JAL Ingestion Service API objects under evaluation.
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
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Evaluations have already been run.");
        
        // Initialize
        final int     CNT_CASES = this.conCases.size();
        this.bolRun = true;
        
        // Run all test cases on  IngestionChannel subject
        int indCase = 1;
       
        LOGGER.info("Running {} test cases for test suite...", CNT_CASES);
        Instant insStart = Instant.now();
        for (IngestApiTestCase recCase : this.conCases) {
            LOGGER.info("Running test case #{} of {} (with index {}) ...", indCase, CNT_CASES, recCase.indCase());
            
            IngestApiTestResult recResult = recCase.evaluate(this.apiService, this.apiStream); 
            
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
        LOGGER.info("Results stored at " + super.getOutputFilePath().toAbsolutePath());
    }
    
    /**
     * <p>
     * Performs a soft shutdown of the application.
     * </p>
     * <p>
     * Either this method or <code>{@link #shutdownNow()}</code> must be called after the 
     * application is no longer need, whether the <code>{@link #run()}</code> method was
     * invoked or not.  Several resources are created during construction which must be
     * released before garbage collection.
     * </p>
     * <p>
     * All application resources are shut down normally.  If all test evaluations were executed correctly
     * (with either success or failure) the resources should shut down quickly.
     * This is a blocking operation and does not return until all resources are released.
     * </p>
     * 
     * @return  <code>true</code> if the shutdown operation completed normally,
     *          <code>false</code> if there was an error in the operation
     *          
     * @throws InterruptedException process interrupted while waiting for completion
     */
    public boolean shutdown() throws InterruptedException {

        boolean     bolResult = true;
        bolResult = bolResult && this.apiService.shutdown();        // throws InterruptedException
        bolResult = bolResult && this.apiStream.shutdown();         // throws InterruptedException
        
        bolResult = bolResult && this.apiService.awaitTermination();// throws InterruptedException
        bolResult = bolResult && this.apiStream.awaitTermination(); // throws InterruptedException
        
        super.close();
        
        return bolResult;
    }
    
    /**
     * <p>
     * Performs a hard shutdown of the application.
     * </p>
     * <p>
     * Either this method or <code>{@link #shutdownNow()}</code> must be called after the 
     * application is no longer need, whether the <code>{@link #run()}</code> method was
     * invoked or not.  Several resources are created during construction which must be
     * released before garbage collection.
     * </p>
     * 
     */
    public void shutdownNow() {
        this.apiService.shutdownNow();
        this.apiStream.shutdownNow();
        
        super.close();
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
        
        // Print out Ingestion Service host address
        ps.println("Ingestion Service Host DpServiceAddress");
        this.addrHost.printOut(ps, strPad);
        ps.println();
        
        // Print out definitions
        ps.println(this.getClass().getSimpleName() +  " Definitions");
        ps.println(strPad + "Data Rate              - Total payload data allocation (bytes) divided by processing/transmission time.");
        ps.println(strPad + "Payload allocation     - Total memory allocation (bytes) of the IngestionFrame payload (may differ from message allocation).");
        ps.println(strPad + "  NOTE: Payload are processed into data messages concurrent with data transmission.");
        ps.println();
        ps.println(strPad + "Payload frame count    - Number of IngestionFrame objects comprising payload.");
        ps.println(strPad + "Transmitted messages   - Number of IngestDataRequest messages processed and transmitted to Ingestion Service.");
        ps.println();
        ps.println(strPad + "Ingestion Service API  - The API used for data transmission {UNARY, STREAM}.");
        ps.println(strPad + "Data column serialize  - Enable/disable the a priori serialization of IngestDataRequest DataColumn messages.");
        ps.println();
        ps.println(strPad + "Enable frame decomposition - Enable/disable decomposition of IngestionFrame object to meet gRPC size limits.");
        ps.println(strPad + "Max composite frame size   - Maximum composite IngestionFrame size (in bytes).");
        ps.println(strPad + "Enable multi-thread processing - Enable/disable multi-threaded processing of IngestionFrame objects to IngestDataRequest messages.");
        ps.println(strPad + "Max processing threads         - Maximum number of IngestionFrame concurrent processing threads.");
        ps.println();
        ps.println(strPad + "gRPC data stream type   - gRPC data stream type used for data transmission {FORWARD, BIDIRECTIONAL}.");
        ps.println(strPad + "Enable multiple streams - Enable/disable use of multiple, concurrent gRPC data streams for transmission.");
        ps.println(strPad + "Max gRPC data streams   - Maximum number of concurrent gRPC data streams used for transmission.");
        ps.println();
        
        // Print out test parameter descriptions
        ps.println("Test Parameter Descriptions");
        IngestApiTestParams.printOut(ps, strPad);
        ps.println();
        
        // Print out evaluation summary
        ps.println("Evaluation Summary");
        ps.println(strPad + "Test parameter combinations   : " + this.suiteCases.testCaseCount());
        ps.println(strPad + "Test cases specified (unique) : " + this.conCases.size());
        ps.println(strPad + "Test cases run                : " + this.conResults.size());
        ps.println(strPad + "Test case failures            : " + this.conFailures.size());
        ps.println(strPad + "Evaluation duration           : " + this.durEval);
        ps.println(strPad + "Evaluation completed          : " + this.bolCompleted);
        ps.println();
        
        // Print out the test suite configuration
        ps.println("Test Suite Configuration");
        this.suiteCases.printOut(ps, strPad);
        ps.println();
        
        // Print out test case data rates
        ps.println("Test Case Data Rates (MBps Descending)");
        DataRateLister<IngestApiTestResult>  lstrRates = DataRateLister.from(
                rec -> rec.recTestCase().indCase(), 
                rec -> rec.recTestCase().specFrame().strLabel(), 
                rec -> rec.szPayload(), 
                rec -> rec.dblRateXmit()
                );
        lstrRates.printOut(ps, strPad, this.conResults);
        ps.println();
        
        // Print out statistical results summary
        ps.println("Test Results Statistics");
        IngestApiResultStats.assignTargetTransmissionRate(DBL_RATE_TARGET);
        IngestApiResultStats.assignTargetProcessingDuration(DUR_PROC_TARGET);
        IngestApiResultStats  statSummary = IngestApiResultStats.from(this.conResults);
        statSummary.printOut(ps, strPad);
        ps.println();
        
        // Print out results extremes
        ps.println("Test Results Extremes");
        IngestApiResultExtremes  recExtremes = IngestApiResultExtremes.from(this.conResults);
        recExtremes.printOut(ps, null);
        ps.println();
        
        // Print out channel configuration scoring
        ps.println("Ingestion Service API Configuration Scoring");
        IngestApiConfigScorer scrChan = IngestApiConfigScorer.from(this.conResults);
        scrChan.printOutByRates(ps, strPad);
        ps.println();
        
        // Print out failed test results 
        ps.println("Failed Cases (By Index)");
        if (this.conFailures.isEmpty()) {
            ps.println(strPad + "None");
            ps.println();
            
        } else {
            for (IngestApiTestResult recFail : this.conFailures) {
                recFail.printOut(ps, strPad);
                ps.println();
            }
        }
        
        // Print out each test result
        ps.println("Individual Case Results (MBps Descending Transmission Rates)");
        for (IngestApiTestResult recResult : this.conResults) {
            recResult.printOut(ps, strPad);
            ps.println();
        }
        
        // Print out the execution log entries
        String  strLogging = super.retrieveExecutionLogEntries();
        ps.println("Execution Log Entries");
        ps.println(strLogging);
        ps.println();
        
    }

}
