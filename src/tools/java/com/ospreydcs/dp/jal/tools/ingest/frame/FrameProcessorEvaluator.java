/*
 * Project: dp-jal
 * File:	FrameProcessorEvaluator.java
 * Package: com.ospreydcs.dp.jal.tools.ingest.frame
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
package com.ospreydcs.dp.jal.tools.ingest.frame;

import java.util.List;

import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.jal.app.JalApplicationBase;
import com.ospreydcs.dp.jal.config.JalConfig;
import com.ospreydcs.dp.jal.config.ingest.JalIngestionConfig;
import com.ospreydcs.dp.jal.config.query.JalQueryConfig;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.query.correl.DataCorrelationEvaluator;

/**
 *
 * @author Christopher K. Allen
 * @since Sep 13, 2025
 *
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
        // TODO Auto-generated method stub

    }

    
    //
    // Application Resources
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
    public static final String      STR_OUTPUT_DEF = CFG_TOOLS.output + "/ingest/frame";
  
    
    /** Argument variable name identifying supplemental PV names for data request */
    public static final String      STR_VAR_PVS = "--pvs";
    
    /** Argument flag identifying a maximum thread count value */
    public static final String      STR_VAR_THRDS = "--threads";
    
    /** Argument flag identifying a concurrency pivot size */
    public static final String      STR_VAR_PIVOT = "--pivot";
    
    /** Argument flag identifying output location */
    public static final String      STR_VAR_OUTPUT = "--output";

    /** List of all the valid argument delimiters */
    public static final List<String>    LST_STR_DELIMS = List.of(
            STR_VAR_PVS,
            STR_VAR_THRDS, 
            STR_VAR_PIVOT, 
            STR_VAR_OUTPUT
            );
    
    
    //
    // Application Constants - Client Messages
    //
    
    /** Application name */
    public static final String      STR_APP_NAME = FrameProcessorEvaluator.class.getSimpleName();
    
    /** A laconic description of the application function */
    public static final String      STR_APP_DESCR = 
            STR_APP_NAME + " Description \n"
          + "- Application evaluates the performance and operation of the IngestionFrameProcessor class \n"
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
          + " [" + STR_VAR_HELP + "]"
          + " [" + STR_VAR_VERSION + "]"
          + " R1 [ ... Rn]"
          + " [" + STR_VAR_PVS + " PV1 ... PVi]"
          + " [" + STR_VAR_THRDS + " M1 ... Mj]"
          + " [" + STR_VAR_PIVOT + " P1 ... Pk]"
          + " [" + STR_VAR_OUTPUT +" Output]"
          + "\n" 
          + "  Where  \n"
          + "    " + STR_VAR_HELP + "        = print this message and return.\n"
          + "    " + STR_VAR_VERSION + "     = prints application version information and return.\n"
          + "    R1, ..., Rn   = Test request(s) to perform - TestArchiveRequest enumeration name(s). \n"
          + "    PV1, ..., PVi = Supplemental PV names to be added to requests R1 through Rn. \n"
          + "    M1, ..., Mj   = Maximum allowable number(s) of concurrent processing threads - Integer value(s). \n"
          + "    P1, ..., Pk   = Pivot size(s) triggering concurrent processing - Integer value(s). \n"
          + "    Output        = output directory w/wout file path, or '" + STR_ARG_VAL_STDOUT + "'. \n"
          + "\n"
          + "  NOTES: \n"
          + "  - All bracketed quantities [...] are optional. \n"
          + "  - PV1, ..., PVj values are strictly optional. \n"
          + "  - If values are not provided for " + STR_VAR_THRDS + " and/or " + STR_VAR_PIVOT + ", default values are provided. \n "
          + "  - Default " + STR_VAR_OUTPUT + " value is " + STR_OUTPUT_DEF + ".\n";

    
    /** The "version" message for client version requests */
    public static final String      STR_APP_VERSION = 
            STR_APP_NAME
          + " version 1.0: compatible with Java Application Library version 1.8.0 or greater.";
    
    

    //
    // JalApplicationBase Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.jal.app.JalApplicationBase#isLogging()
     */
    @Override
    protected boolean isLogging() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see com.ospreydcs.dp.jal.app.JalApplicationBase#getLogger()
     */
    @Override
    protected Logger getLogger() {
        // TODO Auto-generated method stub
        return null;
    }

    
    //
    // Application Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>FrameProcessorEvaluator</code> instance.
     * </p>
     *
     * @param clsApp
     * @param args
     */
    public FrameProcessorEvaluator(Class<FrameProcessorEvaluator> clsApp, String... args) {
        super(clsApp, args);
        // TODO Auto-generated constructor stub
    }

}
