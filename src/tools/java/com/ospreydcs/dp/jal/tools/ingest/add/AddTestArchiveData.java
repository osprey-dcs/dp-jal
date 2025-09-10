/*
 * Project: dp-api-common
 * File:	AddTestArchiveData.java
 * Package: com.ospreydcs.dp.jal.tools.ingest
 * Type: 	AddTestArchiveData
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
 * @since Jun 10, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.ingest.add;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;

import com.ospreydcs.dp.api.app.ExitCode;
import com.ospreydcs.dp.api.app.JalApplicationBase;
import com.ospreydcs.dp.api.common.DpTimestampCase;
import com.ospreydcs.dp.api.common.IngestRequestUID;
import com.ospreydcs.dp.api.common.IngestionResult;
import com.ospreydcs.dp.api.common.ProviderRegistrar;
import com.ospreydcs.dp.api.common.ProviderUID;
import com.ospreydcs.dp.api.config.JalConfig;
import com.ospreydcs.dp.api.config.ingest.JalIngestionConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.ingest.JalIngestionApiFactory;
import com.ospreydcs.dp.api.ingest.JalIngestionException;
import com.ospreydcs.dp.api.ingest.IIngestionService;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.ingest.model.frames.IngestionFrameGenerator;
import com.ospreydcs.dp.jal.tools.ingest.model.frames.SampleBlockConfig;
import com.ospreydcs.dp.jal.tools.ingest.model.values.JalScalarType;
import com.ospreydcs.dp.jal.tools.query.correl.DataCorrelationEvaluator;
import com.sun.jdi.request.InvalidRequestStateException;

/**
 * <p>
 * Application for adding custom, simulated PV samples to the Data Platform Test Archive archive.
 * </p>
 * <p>
 * The application creates <code>{@link IngestionFrame}</code> objects containing simulated data
 * for Process Variables (PVs) provided on the command line.  The timestamps for the PVs are
 * aligned to the inception time of the Data Platform Test Archive, with any desired delay provided
 * by the client.  Thus, the Data Platform Test Archive can be supplemented with custom data from 
 * clients for unique testing applications.
 * </p>
 * <p>
 * The configuration of the <code>IngestionFrame</code> instances sent to the Ingestion Service is
 * given by a record <code>{@link SampleBlockConfig}</code>, whose fields are parsed from the command line.
 * Ingestion frames are sent to the Ingestion Service using an <code>{@link IIngestionService}</code>
 * interface obtain from the connection factory using a default connection.  All data transmission
 * with this interface is done using unary gRPC operations.  Thus, this application is not intended to
 * support large data sets.
 * </p>
 * <p>
 * The application usage is described in the class constant {@link #STR_APP_USAGE}, which contains detailed
 * information on command line arguments.  This description can be displayed from the command line with
 * the single argument {@value JalApplicationBase#STR_VAR_HELP}, or specifically
 * <pre>
 * <code>
 *   % {@value AddTestArchiveData#STR_APP_NAME} {@value JalApplicationBase#STR_VAR_HELP}
 * </code>
 * </pre>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jun 10, 2025
 *
 */
public class AddTestArchiveData extends JalApplicationBase<AddTestArchiveData> {

    
    //
    // Application Entry
    //
    
    /**
     * <p>
     * Application entry point.
     * </p>
     * 
     * @param args  command-line arguments as described in <code>{@link AddTestArchiveData}</code>
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
            System.err.println(STR_APP_NAME + " FAILURE - bad flags in command line " + args + ".");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.INPUT_CFG_CORRUPT);
            
        }
        
        // Get the test suite configuration and output location from the application arguments
        SampleBlockConfig   recFrmCfg;
        int                 cntFrms;
        String              strOutputLoc;
        try {
            
            recFrmCfg = AddTestArchiveData.parseFrameConfiguration(args);
            cntFrms = AddTestArchiveData.parseFrameCount(args);
            strOutputLoc = AddTestArchiveData.parseOutputLocation(args);
            
        } catch (Exception e) {
            
            System.err.println(STR_APP_NAME + " FAILURE - command line unparseable.");
            JalApplicationBase.terminateWithException(DataCorrelationEvaluator.class, e, ExitCode.INTPUT_ARG_INVALID);
            return;
        }

        
        //
        // ------- Application Execution -------
        //
        
        // Create the data simulator and run it
        try {
            AddTestArchiveData    appSimulator = new AddTestArchiveData(recFrmCfg, strOutputLoc, args);
            
            appSimulator.run(cntFrms);
            appSimulator.writeReport();
            appSimulator.shutdown();
            
            System.out.println("Execution complete: " + appSimulator.getFrameCount() + " ingestion frames sent in " + appSimulator.getExecutionTime().toSeconds() + " seconds.");
            if (appSimulator.getOutputFilePath() != null)
                System.out.println("Execution summary stored at " + appSimulator.getOutputFilePath());
            
        } catch (IllegalArgumentException | DpGrpcException | UnsupportedOperationException | FileNotFoundException | SecurityException e) {

            System.err.println(STR_APP_NAME + " FAILURE - Unable to create application instance.");
            JalApplicationBase.terminateWithException(AddTestArchiveData.class, e, ExitCode.INITIALIZATION_EXCEPTION);

        } catch (InvalidRequestStateException | IllegalStateException | JalIngestionException e)  {
            System.err.println(STR_APP_NAME + " ERROR during application execution.");
            JalApplicationBase.terminateWithException(AddTestArchiveData.class, e, ExitCode.EXECUTION_EXCEPTION);
            
        } catch (InterruptedException e) {
            System.err.println(STR_APP_NAME + " ERROR during shutdown operation.");
            JalApplicationBase.terminateWithException(AddTestArchiveData.class, e, ExitCode.SHUTDOWN_EXCEPTION);
        }
    }
    
    
    //
    // Application Resources
    //
    
    /** Default configuration parameters for the Query Service tools */
    private static final JalIngestionConfig  CFG_INGEST = JalConfig.getInstance().ingest;
    
    /** Default configuration parameters for the JAL Tools */
    private static final JalToolsConfig     CFG_TOOLS = JalToolsConfig.getInstance();
    
    
    
    /** Application name */
    public static final String      STR_APP_NAME = AddTestArchiveData.class.getSimpleName();
    
    
    /** Map of data provider attributes for this application */
    private static final Map<String, String>    MAP_PRVDR_ATTRS = Map.of(
                                                    "Identity", "Application",
                                                    "Location", "JAL Tools",
                                                    "Function", "Simulated data",
                                                    "Target", "Data Platform Test Archive"
                                                    );
    
    /** Application data provider registration for the Ingestion Service */
    private static final ProviderRegistrar      REC_PRVDR_REG = ProviderRegistrar.from(STR_APP_NAME, MAP_PRVDR_ATTRS);

    
    //
    // Application Constants - Command-Line Argument Flags
    //
    
    /** Argument variable identifying the PV name values*/
    public static final String      STR_VAR_PVS = "--pvs";
    
    /** Argument variable identifying the data type values for all PVs */
    public static final String      STR_VAR_TYPE = "--type";
    
    /** Argument variable identifying the samples count value for each PV */
    public static final String      STR_VAR_SMPLS = "--samples";
    
    /** Argument variable identifying the sampling period value (Duration) for each PV */
    public static final String      STR_VAR_PERIOD = "--period";
    
    /** Argument variable identifying the sampling delay value (Duration) for each PV */
    public static final String      STR_VAR_DELAY = "--delay";
    
    /** Argument switch indicating the use of a uniform sampling clock for timestamp creation */
    public static final String      STR_SWITCH_CLOCK = "-clocked";
    
    /** Argument switch indicating the use of an explicit timestamp list for timestamp creation */
    public static final String      STR_SWITCH_TMS_LIST = "-tmslist";
    
    /** Optional argument variable identify the number of ingestion frames to send */
    public static final String      STR_VAR_FRAMES = "--frames";
    
    /** Optional argument variable identifying output location */
    public static final String      STR_VAR_OUTPUT = "--output";

    /** List of all the valid argument delimiters */
    public static final List<String>    LST_STR_DELIMS = List.of(
            STR_VAR_PVS, 
            STR_VAR_TYPE, 
            STR_VAR_SMPLS,
            STR_VAR_PERIOD,
            STR_VAR_DELAY,
            STR_SWITCH_CLOCK,
            STR_SWITCH_TMS_LIST,
            STR_VAR_FRAMES,
            STR_VAR_OUTPUT
            );
    
    
    //
    // Application Constants - Command-Line Argument Default Values
    //
    
    /** Minimum number of application arguments - argument name and at least one data request */
    public static final int         CNT_APP_MIN_ARGS = 8;
    
    
    /** The default value for the sampling delay if none is given */
    public static final Duration    DUR_DELAY_DEF = Duration.ZERO;
    
    /** The default value for the frame count if none is given */
    public static final int         CNT_FRAMES_DEF = 1;
    
    /** Default output path location if none given */
    public static final String      STR_OUTPUT_DEF = CFG_TOOLS.output + "/ingest/frame";
  
    
    //
    // Application Constants - Client Messages
    //
    
    /** The "usage" message for client help requests or invalid application arguments */
    public static final String      STR_APP_USAGE = 
            STR_APP_NAME  + " Usage: \n"
          + "\n"
          + "> " + STR_APP_NAME
          + " [" + STR_VAR_HELP + "]"
          + " [" + STR_VAR_VERSION + "]"
          + " " + STR_VAR_PVS + " PV1 [... PVn]"
          + " " + STR_VAR_TYPE + " TYPE"
          + " " + STR_VAR_SMPLS + " N"
          + " " + STR_VAR_PERIOD + " T"
          + " [" + STR_VAR_DELAY + " D]"
          + " [" + STR_VAR_FRAMES + " F]"
          + " [" + STR_SWITCH_CLOCK + "]"
          + " [" + STR_SWITCH_TMS_LIST + "]"
          + " [" + STR_VAR_OUTPUT + " Output]"
          + "\n\n" 
          + "  Where  \n"
          + "    " + STR_VAR_HELP + "          = print this message and return.\n"
          + "    " + STR_VAR_VERSION + "       = prints application version information and return.\n"
          + "    PV1, [..., PVn] = Name(s) of the process variables to add (e.g., 'jalTools_1', 'jatTools_2', etc.). \n"
          + "    TYPE            = Data type of all process variables - 'JalScalarType' enumeration (e.g. 'INTEGER', 'DOUBLE', etc.). \n"
          + "    N               = The number of samples for each process variable (note total duration is T*N).\n"
          + "    T               = Sampling period - numeric parsable value of format 'P[nd]DT[nh]H[nm]M[ns.n1...n9]S', \n"
          + "                      where nd = integer number of days, \n "
          + "                           nh = integer number of hours, \n" 
          + "                            nm = integer number of minutes, \n"
          + "                            ns.n1...n9 decimal number of seconds w/ 1 ns resolution. \n"
          + "                      example: '" + STR_VAR_PERIOD + " PT0.001S' specifies 1 ms sampling period.\n"
          + "    D             = Sampling delay - numeric parseable value of format 'P[nd]DT[nh]H[nm]M[ns.n1...n9]S' (as above).\n"
          + "    F             = The number of ingestion frames to send to Ingestion Service.\n"
          + "    [" + STR_SWITCH_CLOCK + "]    = specifies use of a uniform sampling clock for timestamp generation. \n"
          + "    [" + STR_SWITCH_TMS_LIST + "]    = specifies use of explicit timestamp list for timestamp generation. \n"
          + "    Output        = output directory path for execution report, or '" + STR_ARG_VAL_STDOUT + "'. \n"
          + "\n"
          + "  NOTES: \n"
          + "  - All bracketed quantities [...] are optional. \n"
          + "  - If '" + STR_VAR_DELAY + " D' does not appear the default value is  D=PT0.0S.\n"
          + "  - If '" + STR_VAR_FRAMES + " F' does not appear the default value is F=" + CNT_FRAMES_DEF + ".\n"
          + "  - If neither '" + STR_SWITCH_CLOCK + "' or '" + STR_SWITCH_TMS_LIST + "' appears, the default is a uniform sampling clock.\n"
          + "  - If '" + STR_VAR_OUTPUT + " Output' does not appear no execution report is generated.\n"
          + "  - If '" + STR_VAR_OUTPUT + " Output' is included all logging events will be mirrored to 'Output'.\n"
          + "  - If '" + STR_VAR_OUTPUT + " " + STR_ARG_VAL_STDOUT + "' appears all output is directed to standard output.\n"
          + "  - Note that the parameters {PV1, ..., PVn}, TYPE, N, T, D '" + STR_SWITCH_CLOCK + "', and '" + STR_SWITCH_TMS_LIST + "' are all used for ingestion frame configuration.";

    
    /** The "version" message for client version requests */
    public static final String      STR_APP_VERSION = 
            STR_APP_NAME
          + " version 1.0: compatible with Java Application Library version 1.8.0 or greater.";
    
    
    //
    // Class Resources
    //
    
    /** Event logging enabled/disabled flag */
    public static final boolean     BOL_LOGGING = CFG_INGEST.logging.enabled;
    
    /** Event logging level */
    public static final String      STR_LOGGING_LEVEL = CFG_INGEST.logging.level;
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLogger(AddTestArchiveData.class, STR_LOGGING_LEVEL);

    
    //
    // Defining Attributes
    //
    
    /** Record containing configuration parameters for the ingestion frames */
    private final SampleBlockConfig     recFrmCfg;
    
    /** The requested execution report path location, or null if none */
    private final String                strOutputLoc;
            
    
    //
    // Instance Resources
    //
    
    /** The Ingestion Service API */
    private final   IIngestionService           apiIngest;
    
    /** The ingestion frame generator */
    private final   IngestionFrameGenerator     genFrames;
    
    
    //
    // Instance Attributes
    //
    
    /** The data provider UID obtained from registration */
    private final   ProviderUID                 recPrvdrId;
    
    /** The ordered list of request UIDs for each ingestion frame sent to the Ingestion Service */
    private final   List<IngestRequestUID>      lstIngRqstIds;
    
    /** The ordered list of all frame ingestion results obtained during execution */ 
    private final   List<IngestionResult>       lstIngRslts;
    
    
    //
    // State Variables
    //
    
    /** The total number of ingestion frames sent to the Ingestion Service */
    private int         cntFrames = 0;
    
    /** The run() execution duration */
    private Duration    durExec = Duration.ZERO;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>AddTestArchiveData</code> instance.
     * </p>
     *
     * @param recFrmCfg     the configuration record for ingestion frames
     * @param strOutputLoc  the output file path for report generation, or <code>null</code> if no report is requested
     * @param args          application command-line arguments from main()
     * 
     * @throws DpGrpcException 
     * @throws JalIngestionException the data provider registration failed
     * @throws UnsupportedOperationException the given output file path did not exist on the file system
     * @throws FileNotFoundException         unable to create the output file (see cause and message)
     * @throws SecurityException             unable to write to the output file
     */
    public AddTestArchiveData(SampleBlockConfig recFrmCfg, String strOutputLoc, String...args) throws DpGrpcException, JalIngestionException, IllegalArgumentException, UnsupportedOperationException, FileNotFoundException, SecurityException {
        super(AddTestArchiveData.class, args);
        
        // Record defining attributes
        this.recFrmCfg = recFrmCfg;
        this.strOutputLoc = strOutputLoc;

        // Open the output file if requested and add appender
        if (strOutputLoc != null) {
            super.openOutputStream(strOutputLoc);
            
            OutputStreamAppender    appdr = Log4j.createOutputStreamAppender(STR_APP_NAME, super.psOutput);
            Log4j.attachAppender(LOGGER, appdr);
        }
        
        // Connect to the Ingestion Service API and register as provider 
        this.apiIngest = JalIngestionApiFactory.connectService();
        this.recPrvdrId = this.apiIngest.registerProvider(REC_PRVDR_REG);
        if (BOL_LOGGING) 
            LOGGER.info("Provider registration succeed with provider name={}, UID={}, and new={}", 
                    this.recPrvdrId.name(), 
                    this.recPrvdrId.uid(), 
                    this.recPrvdrId.isNew());

        // Create the ingestion frame generator and ingestion result list
        this.genFrames = IngestionFrameGenerator.from(recFrmCfg);
        
        this.lstIngRslts = new LinkedList<>(); 
        this.lstIngRqstIds = new LinkedList<>();
    }

    
    // 
    // JalApplicationBase Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.api.app.JalApplicationBase#isLogging()
     */
    @Override
    protected boolean isLogging() {
        return BOL_LOGGING;
    }

    /**
     * @see com.ospreydcs.dp.api.app.JalApplicationBase#getLogger()
     */
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
    
    
    //
    // State Inquiry
    //
    
    /**
     * <p>
     * Returns the Data Provider UID for this application as given by the Ingestion Service.
     * </p>
     * <p>
     * Each Data Provider sending ingestion frames to the Ingestion Service must first register as
     * a "Data Provider".  This process is done in application initialization at construction.  The
     * returned value is the Data Provider UID for this application as given by the Ingesiton Service.
     * </p>
     * 
     * @return  the application Data Provider UID as registered by the Ingestion Service
     */
    public ProviderUID  getProviderId() {
        return this.recPrvdrId;
    }
    
    /**
     * <p>
     * Returns the number of ingestion frames sent to the Ingestion Service.
     * </p>
     * <p>
     * If the application has not been run the value 0 is returned.
     * </p>
     * 
     * @return  the number of ingestion frames sent to the Ingestion Service, or 0 if not executed
     */
    public int  getFrameCount() {
        return this.cntFrames;
    }
    
    /**
     * <p>
     * Returns the execution duration of the application.
     * </p>
     * <p>
     * The returned value is the interval of time for which the method <code>{@link #run(int)}</code> was
     * active.  If the <code>{@link #run(int)}</code> method was not executed the value
     * <code>{@link Duration#ZERO}</code> is returned.
     * </p>
     * <p>
     * Note that the returned value does not include the initialization time required by constructor 
     * <code>{@link AddTestArchiveData#AddSimulatedData(SampleBlockConfig, String)}</code> or the shutdown
     * time from <code>{@link #shutdown()}</code>.
     * 
     * @return  the execution time of the application, 
     *          or <code>{@link Duration#ZERO}</code> if the application has not been run
     */
    public Duration getExecutionTime() {
        return this.durExec;
    }
    
    /**
     * <p>
     * Returns the request UIDs for all ingestion frames sent to the Ingestion Service.
     * </p>
     * <p>
     * Ingestion request UIDs are contained in each ingestion frame sent to the Ingestion Service.
     * They are used to identify incoming data ingestion requests by the Ingestion Service and are
     * available to the client for later inspection of ingestion results for each ingestion request.
     * </p>
     * <p>
     * The ingestion request UIDs will appear in the ingestion results 
     * (see <code>{@link #getIngestionResults()}</code>) and can be verified for success/failure.
     * The order of the returned list conforms to the sequence of ingestion frames sent to the
     * Ingestion Service.
     * </p>
     * <p>
     * If the application has not been executed (i.e., <code>{@link #run(int)}</code> has not been invoke) an
     * empty list is returned.
     * </p>
     * 
     * @return  an order list of request UIDs for each ingestion frame sent to the Ingestion Servide
     */
    public List<IngestRequestUID>   getIngestionRequestIds() {
        return this.lstIngRqstIds;
    }
    
    /**
     * <p>
     * Returns the list of results from the ingestion frame ingestion operations.
     * </p>
     * <p>
     * Since the application is using unary data frame ingestion there should be one <code>IngestionResult</code>
     * record for each <code>IngestionFrame</code> sent to the Ingestion Service.  The results are ordered by
     * the sequence of <code>IngestionFrame</code> instances sent to the Ingestion Service.  
     * </p>
     * <p>
     * If the application has not been executed (i.e., <code>{@link #run(int)}</code> has not been invoke) an
     * empty list is returned.
     * </p>
     * 
     * @return  the ordered list of ingestion results as reported by the Ingestion Service
     */
    public List<IngestionResult>    getIngestionResults() {
        return this.lstIngRslts;
    }
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Runs the application sending the given number of ingestion frames to the Ingestion Service.
     * </p>
     * <p>
     * This method can be invoked multiple times, each time sending a (timestamp) sequential set of
     * ingestion frames to the ingestion service.
     * </p>
     * 
     * @param cntFrames the number of ingestion frames to send to Ingestion Service
     * 
     * @throws IllegalStateException            unregistered data provider
     * @throws JalIngestionException             general ingestion exception (see message and cause)
     */
    public void run(int cntFrames) throws InvalidRequestStateException, IllegalStateException, JalIngestionException {
        
        // Generate ingestion frames and send them to the Ingestion Service
        Instant     insStart = Instant.now();
        for (int iFrame=0; iFrame<cntFrames; iFrame++) {

            IngestionFrame  frm = this.genFrames.build();
            this.lstIngRqstIds.add(frm.getClientRequestUid());
            
            IngestionResult recResult = this.apiIngest.ingest(frm);
            this.lstIngRslts.add(recResult);
            
            LOGGER.info("Ingestion frame {} with request UID {} sent to Ingestion Service.", frm.getFrameLabel(), frm.getClientRequestUid());
        }
        Instant     insFinish = Instant.now();
        
        // Update state variables
        this.durExec = Duration.between(insStart, insFinish);
        this.cntFrames += cntFrames;
        super.bolRun = true;
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
     * @throws IllegalStateException    no results are available (called before <code>{@link #run(int)}</code>) 
     */
    public void writeReport() throws /* ConfigurationException, */ IllegalStateException {
        this.writeReport(super.psOutput);
    }
    
    /**
     * <p>
     * Creates a text summary of the application execution and prints it to the given output stream.
     * </p>
     * <p>
     * This method is available after invoking <code>{@link #run(int)}</code>.  It prints out a report
     * of the time of execution, the number of frames sent, and the ingestion frame configuration.
     * </p>
     * 
     * @param ps    target output stream for <code>QueryChannel</code> evaluations report
     * 
     * @throws ConfigurationException   <s>no output location was specified at construction</s>
     * @throws IllegalStateException    no results are available (called before <code>{@link #run(int)}</code>) 
     */
    public void writeReport(PrintStream ps) throws /* ConfigurationException, */ IllegalStateException {
        
        // Check state
        if (!this.bolRun)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + "- Test suite has not been run.");
        
        // Check configuration
        if (super.psOutput == null) {
            
            if (BOL_LOGGING) {
                String strMsg = JavaRuntime.getQualifiedMethodNameSimple() + " - No output location was specified. Nothing to do.";
                
                LOGGER.warn(strMsg);
            }
            
            return;
        }
        
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

        // Common left-hand side padding
        String  strPad = "  ";

        // Print out execution summary
        ps.println("Data Provider name    : " + this.recPrvdrId.name());
        ps.println("Ingestion frames sent : " + this.cntFrames);
        ps.println("Execution duration    : " + this.durExec);
        ps.println();
        
        // Print out Data Provider information
        ps.println("Data Provider Information:");
        this.recPrvdrId.printOut(ps, strPad);
        ps.println();
        
        // Print out Ingestion Frame configuration
        ps.println("Ingestion frames configuration:");
        this.recFrmCfg.printOut(ps, strPad);
        ps.println();
        
        // Print out list of ingestion request UIDs for frames sent to Ingestion Service
        ps.println("Request UIDs for Ingested Frames:");
        for (IngestRequestUID recUid : this.lstIngRqstIds) {
           recUid.printOut(ps, strPad);
        }
        ps.println();
        
        // Print out ingestion results
        ps.println("Ingestion Results:");
        for (IngestionResult recResult : this.lstIngRslts) {
            recResult.printOut(this.psOutput, strPad);
            ps.println();
        }
    }
    
    /**
     * <p>
     * Shuts down the application.
     * </p>
     * <p>
     * This method should be called before exiting the application.
     * The Ingestion Service API is shut down and any open output streams are closed.
     * </p>
     * 
     * @throws InterruptedException process interruption while shutting down the Ingestion Service API
     */
    public void shutdown() throws InterruptedException {
        
        this.apiIngest.shutdown();
        this.apiIngest.awaitTermination();
        
        super.close();
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Parse the application command line for the ingestion frame configuration parameters and returns them.
     * </p>
     * <p>
     * The configuration parameters for the produced ingestion frames are supplied by the client from the command
     * line as variable values and switches.  This method parses the command line arguments recovering all the
     * configuration parameters and returns them as a <code>{@link SampleBlockConfig}</code> record. 
     * </p>
     * <p>
     * Each field of the return <code>{@link SampleBlockConfig}</code> record is recovered separately with an
     * internal support method prefixed with <code>recover...()</code> where the suffix indicates the parameter.  
     * Any exceptions in the parsing process are generated from these methods.
     * </p>
     * 
     * @param args  the application command-line argument collection
     * 
     * @return  ingestion frame configuration as described by the command-line arguments
     * 
     * @throws MissingResourceException     a variable value was missing from the argument collection
     * @throws IndexOutOfBoundsException    multiple values given for a variable expecting a single value
     * @throws IllegalArgumentException     an enumeration constant was invalid (see message)
     * @throws DateTimeParseException       the sampling period was incorrectly formatted
     * @throws NumberFormatException        a numeric argument value was incorrectly formatted (e.g., sample count)
     * @throws ConfigurationException       too many switches on the command line (both {@value #STR_SWITCH_CLOCK} and {@value #STR_SWITCH_TMS_LIST})
     * 
     * @see #recoverPvNames(String[])
     * @see #recoverPvType(String[])
     * @see #recoverSamplePeriod(String[])
     * @see #recoverSampleDelay(String[])
     * @see #recoverSampleCount(String[])
     * @see #recoverTimestampCase(String[])
     */
    private static SampleBlockConfig    parseFrameConfiguration(String[] args) 
            throws MissingResourceException, IndexOutOfBoundsException, IllegalArgumentException, DateTimeParseException, NumberFormatException, ConfigurationException {

        // Recover the PV names 
        Set<String>     setPvNms = AddTestArchiveData.recoverPvNames(args);   // throws MissingResourceException
        
        // Recover the PV type enumeration name 
        JalScalarType   enmType = AddTestArchiveData.recoverPvType(args);     // throws MissingResourceException, IndexOutOfBoundsException, IllegalArgumentException
        
        // Recover the sampling period 
        Duration    durPeriod = AddTestArchiveData.recoverSamplePeriod(args);// throws MissingResourceException, IndexOutOfBoundsException, DateTimeParseException
        
        // Recover the sampling delay
        Duration    durDelay = AddTestArchiveData.recoverSampleDelay(args);   // throws IndexOutOfBoundsException, DateTimeParseException
        
        // Recover the sample count 
        Integer cntSmpls = AddTestArchiveData.recoverSampleCount(args);       // throws MissingResourceException, IndexOutOfBoundsException, NumberFormatException
        
        // Recover the timestamp case 
        DpTimestampCase enmTmsCase = AddTestArchiveData.recoverTimestampCase(args); // throws ConfigurationException
        
        // Create the ingestion frame configuration record and return it
        SampleBlockConfig   recFrmCfg = SampleBlockConfig.from(setPvNms, enmType, enmTmsCase, cntSmpls, durPeriod, durDelay);
        
        return recFrmCfg;
    }
    
    /**
     * <p>
     * Recovers the Process Variable names from the the command-line argument collection.
     * </p>
     * <p>
     * The process variable names for ingestion frames are identified by the command-line variable
     * {@value #STR_VAR_PVS}.  There must be at least one process variable name in the variable
     * value collection, otherwise an exception is thrown.
     * </p>
     * 
     * @param args  the application command-line argument collection
     * 
     * @return  the set of PV names for each ingestion frame
     * 
     * @throws MissingResourceException variable value {@value #STR_VAR_PVS} was missing from the argument collection
     */
    private static Set<String>  recoverPvNames(String[] args) throws MissingResourceException {
        
        // Parse the PV names 
        List<String>    lstPvNms = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_PVS);
        
        // Exception checking - there should be at least one variable value
        if (lstPvNms.isEmpty())
            throw new MissingResourceException("There must be at least one PV name.", STR_APP_NAME, STR_VAR_PVS);
        
        // Create Set container for names and return
        Set<String>     setPvNms = new TreeSet<>(); 
        setPvNms.addAll(lstPvNms);

        return setPvNms;
    }
    
    /**
     * <p>
     * Recovers the process variable data type from the command line.
     * </p>
     * <p>
     * All process variables within ingestion frames have the same (scalar) data type, which is represented
     * as a <code>{@link JalScalarType}</code> enumeration constant.  The type value is taken from the
     * command-line variable {@value #STR_VAR_TYPE} which must be the <em>exact name</em> of one of the
     * <code>{@link JalScalarType}</code> enumeration constants, otherwise an exception is thrown.
     * Note also that there should be one and only one string value (enumeration constant name) for this
     * variable, otherwise an exception is thrown.
     * </p> 
     *   
     * @param args  the application command-line argument collection
     * 
     * @return  the data type for all process variables within ingestion frames
     * 
     * @throws MissingResourceException     variable value {@value #STR_VAR_TYPE} was missing from the argument collection
     * @throws IndexOutOfBoundsException    multiple values given for variable (expecting a single value)
     * @throws IllegalArgumentException     an enumeration constant was invalid (see message)
     */
    private static JalScalarType   recoverPvType(String[] args) 
            throws MissingResourceException, IndexOutOfBoundsException, IllegalArgumentException {
        
        // Parse the PV type enumeration name 
        List<String>    lstPvType = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_TYPE);
        
        // Exception checking - there should be only one variable value
        if (lstPvType.isEmpty())
            throw new MissingResourceException("No PV type given.", STR_APP_NAME, STR_VAR_TYPE);
        if (lstPvType.size() > 1)
            throw new IndexOutOfBoundsException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + ": " + STR_VAR_TYPE + " - Only one PV type allowed, parsed " + lstPvType);

        // Convert to enumeration constant and return
        JalScalarType   enmType = JalScalarType.valueOf(JalScalarType.class, lstPvType.get(0)); // throws IllegalArgumentException
        
        return enmType;
    }
    
    /**
     * <p>
     * Recovers the ingestion frame sample count from the command line arguments.
     * </p>
     * <p>
     * The sample count is the number of samples for each process variable in the ingestion frame,
     * that is, the row count for the frame.  Its value is taken from the command-line variable
     * {@value #STR_VAR_SMPLS}.  Note that there should be one and only one integer value for
     * this variable, otherwise an exception is thrown.
     * </p>
     *  
     * @param args  the application command-line argument collection
     * 
     * @return  the number of samples for each PV in the ingestion frame
     * 
     * @throws MissingResourceException     variable {@value #STR_VAR_SMPLS} was missing from the argument collection
     * @throws IndexOutOfBoundsException    multiple values given for variable (expecting a single value)
     * @throws NumberFormatException        a numeric argument value was incorrectly formatted (e.g., sample count)
     */
    private static int  recoverSampleCount(String[] args) throws MissingResourceException, IndexOutOfBoundsException, NumberFormatException {
        
        // Parse the sample count and convert to Integer
        List<String>    lstSmplCnt = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_SMPLS);
        
        // Exception checking - there should be only one variable value
        if (lstSmplCnt.isEmpty())
            throw new MissingResourceException("No sample count given.", STR_APP_NAME, STR_VAR_SMPLS);
        if (lstSmplCnt.size() > 1)
            throw new IndexOutOfBoundsException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + ": " + STR_VAR_SMPLS + " - Only one sample count allowed, parsed " + lstSmplCnt);
        
        // Convert to integer value and return
        Integer cntSmpls = Integer.parseInt(lstSmplCnt.get(0)); // throws NumberFormatException

        return cntSmpls;
    }
    
    /**
     * <p>
     * Recovers the sampling period for all process variables from the command line.
     * </p>
     * <p>
     * The sampling period is the time interval between timestamps for ingestion frames.
     * Its values is taken from the command-line variable {@value #STR_VAR_PERIOD}.  The value
     * is assumed to be formatted string parseable as time duration as described in method
     * documentation <code>{@link Duration#parse(CharSequence)}</code>.  If the format is bad
     * an exception is thrown.  Note also that there should be one and only one string value for
     * this variable, otherwise an exception is thrown.
     * </p>
     * 
     * @param args  the application command-line argument collection
     * 
     * @return  the sampling period used to generate ingestion frame timestamps
     * 
     * @throws MissingResourceException     variable {@value #STR_VAR_PERIOD} was missing from the command line
     * @throws IndexOutOfBoundsException    multiple values given for variable (expecting a single value)
     * @throws DateTimeParseException       the sampling period was incorrectly formatted
     */
    private static Duration recoverSamplePeriod(String[] args) throws MissingResourceException, IndexOutOfBoundsException, DateTimeParseException {
        
        // Parse the sampling period and convert to Instant
        List<String>    lstPeriod = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_PERIOD);
        
        // Exception checking - there should be only one variable value
        if (lstPeriod.isEmpty())
            throw new MissingResourceException("No sampling period given.", STR_APP_NAME, STR_VAR_PERIOD);
        if (lstPeriod.size() > 1)
            throw new IndexOutOfBoundsException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + ": " + STR_VAR_PERIOD + " - Only one sampling period allowed, parsed " + lstPeriod);

        // Convert to Duration instance and return
        Duration    durPeriod = Duration.parse(lstPeriod.get(0));   // throws DateTimeParseException
        
        return durPeriod;
    }
    
    /**
     * <p>
     * Recovers the sampling delay for all process variables from the command line.
     * </p>
     * <p>
     * The sampling delay is the time interval between Test Archive inception and the start of the
     * sampling process.  This value, along with {@value #STR_VAR_PERIOD} and {@value #STR_VAR_SMPLS}
     * are used to generate the timestamps for all ingestion frames.
     * Its values is taken from the command-line variable {@value #STR_VAR_DELAY}.  The value
     * is assumed to be formatted string parseable as time duration as described in method
     * documentation <code>{@link Duration#parse(CharSequence)}</code>.  If the format is bad
     * an exception is thrown.  Note also that there should be no more than one string value for
     * this variable, otherwise an exception is thrown.  If the variable {@value #STR_VAR_DELAY} does
     * not appear the default value <code>{@link #DUR_DELAY_DEF}</code> is returned.
     * </p>
     * 
     * @param args  the application command-line argument collection
     * 
     * @return  the sampling delay used to generate ingestion frame timestamps
     * 
     * @throws IndexOutOfBoundsException    multiple values given for variable (expecting a single value)
     * @throws DateTimeParseException       the sampling period was incorrectly formatted
     */
    private static Duration recoverSampleDelay(String[] args) throws IndexOutOfBoundsException, DateTimeParseException {
        
        // Parse the sampling period and convert to Instant
        List<String>    lstDelay = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_DELAY);
        
        // Check for default value
        if (lstDelay.isEmpty())
            return DUR_DELAY_DEF;
        
        // Exception checking - there should be no more than one variable value
        if (lstDelay.size() > 1)
            throw new IndexOutOfBoundsException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + ": " + STR_VAR_DELAY + " - Only one sampling delay allowed, parsed " + lstDelay);

        // Convert to Duration instance and return
        Duration    durDelay = Duration.parse(lstDelay.get(0));   // throws DateTimeParseException
        
        return durDelay;
    }
    
    /**
     * <p>
     * Recovers the desired ingestion frame timestamp representation from the command-line switches.
     * </p>
     * <p>
     * Timestamps for the ingestion frames can be expressed using either a uniform sampling clock
     * (see <code{@link DpTimestampCase#SAMPLING_CLOCK}</code>) or as an explicit list of timestamp
     * instants (see <code>{@link DpTimestampCase#TIMESTAMP_LIST}</code>).  The following command-line
     * switches determine the type of timestamp representation within ingestion frames:
     * <ul>
     * <li>{@value #STR_SWITCH_CLOCK} - returns <code>{@link DpTimestampCase#SAMPLING_CLOCK}</code></li>
     * <li>{@value #STR_SWITCH_TMS_LIST} - returns <code>{@link DpTimestampCase#TIMESTAMP_LIST}</code></li>
     * </ul>
     * If neither switch appears on the command line the default value is 
     * <code>{@link DpTimestampCase#SAMPLING_CLOCK}</code>.  If both switches appear on the command line
     * an exception is thrown.
     * </p>
     * 
     * @param args  the application command-line argument collection
     * 
     * @return  the desired timestamp representation for ingestion frames
     * 
     * @throws ConfigurationException incompatible switches on the command line (contains both {@value #STR_SWITCH_CLOCK} and {@value #STR_SWITCH_TMS_LIST}) 
     */
    private static DpTimestampCase  recoverTimestampCase(String[] args) throws ConfigurationException {
        
        // Parse command arguments for timestamp case switches
        boolean bolClk = JalApplicationBase.parseAppArgsSwitch(args, STR_SWITCH_CLOCK);
        boolean bolTmsLst = JalApplicationBase.parseAppArgsSwitch(args, STR_SWITCH_TMS_LIST);
        
        // Check for exception
        if (bolClk && bolTmsLst)
            throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() + " - Incompatible simultaneous switches " + STR_SWITCH_CLOCK + " and " + STR_SWITCH_TMS_LIST + " encountered.");
        
        // Selects for sampling clock as default
        DpTimestampCase enmTmsCase;
        if (bolTmsLst)
            enmTmsCase = DpTimestampCase.TIMESTAMP_LIST;
        else
            enmTmsCase = DpTimestampCase.SAMPLING_CLOCK;

        return enmTmsCase;
    }
    
    /**
     * <p>
     * Parses the application command-line arguments for the frame count and returns it.
     * </p>
     * <p>
     * The frame count is the number of <code>{@link IngestionFrame}</code> instances to send to the
     * Ingestion Service.  The frame count is identified by variable {@value #STR_VAR_FRAMES}.
     * This is an operation variable; if the variable is not present on the command line a default
     * value of {@value #CNT_FRAMES_DEF} is returned.  If more than one value is given for the frame
     * count variable an exception is thrown.  The frame count should be integer valued; if the value
     * cannot be parsed as such an exception is thrown.
     * </p>
     *  
     * @param args  the application command-line argument collection
     * 
     * @return  the number of ingestion frames to send to Ingestion Service
     * 
     * @throws IndexOutOfBoundsException    multiple values given for variable (expecting a single value)
     * @throws NumberFormatException        a numeric argument value was incorrectly formatted (e.g., sample count)
     */
    private static int  parseFrameCount(String[] args) throws IndexOutOfBoundsException, NumberFormatException {
        
        // Parse the command arguments for the frame count variable
        List<String>    lstCntFrms = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_FRAMES);
        
        // Default to 1 frame if not specified
        if (lstCntFrms.isEmpty())
            return CNT_FRAMES_DEF;
        
        // Exception checking - there should be only one variable value if variable is present
        if (lstCntFrms.size() > 1)
            throw new IndexOutOfBoundsException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + ": " + STR_VAR_FRAMES + " - Only one frame count allowed, parsed " + lstCntFrms);
        
        // Convert to integer value and return
        int cntFrms = Integer.parseInt(lstCntFrms.get(0));
        
        return cntFrms;
    }
    
    /**
     * <p>
     * Parses the application command-line argument collection for the output location and return it.
     * </p>
     * <p>
     * The output location, as specified by the application client, is the value of variable
     * {@value #STR_VAR_OUTPUT}.  There is only one value for this variable and any additional values
     * are will throw an exception.
     * </p>
     * <p>
     * If the variable {@value #STR_VAR_OUTPUT} is not present in the command line arguments, this is an
     * optional parameter, then a <code>null</code> values is returned.
     * </p>
     * 
     * @param args  the application command-line argument collection
     * 
     * @return  the output location as specified in the command line, 
     *          or <code>null</code> if not present
     *          
     * @throws IndexOutOfBoundsException    multiple values given for variable (expecting a single value)
     */
    private static String   parseOutputLocation(String[] args) throws IndexOutOfBoundsException {
        
        // Look for the output location on the command line
        List<String>    lstStrOutput = JalApplicationBase.parseAppArgsVariable(args, STR_VAR_OUTPUT);
    
        // If there is no user-provided output location use the default value in the JAL configuration
        if (lstStrOutput.isEmpty()) 
            return null;
    
        // Exception checking - there should be only one variable value if variable is present
        if (lstStrOutput.size() > 1)
            throw new IndexOutOfBoundsException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + ": " + STR_VAR_OUTPUT + " - Only one output location allowed, parsed " + lstStrOutput);
        
        // Else return the first element in the list
        String strOutputLoc = lstStrOutput.get(0);
        
        return strOutputLoc;
    }
    
}
