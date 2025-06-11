/*
 * Project: dp-api-common
 * File:	AddSimulatedData.java
 * Package: com.ospreydcs.dp.jal.tools.ingest
 * Type: 	AddSimulatedData
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
package com.ospreydcs.dp.jal.tools.ingest;

import java.util.List;

import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.app.ExitCode;
import com.ospreydcs.dp.api.app.JalApplicationBase;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.query.correl.DataCorrelationEvaluator;

/**
 * <p>
 * Application for adding simulated PV samples to the Data Platform archive.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jun 10, 2025
 *
 */
public class AddSimulatedData extends JalApplicationBase<AddSimulatedData> {

    
    //
    // Application Entry
    //
    
    /**
     * <p>
     * Application entry point.
     * </p>
     * 
     * @param args  command-line arguments as described in <code>{@link AddSimulatedData}</code>
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
            JalApplicationBase.reportTerminalException(DataCorrelationEvaluator.class, e);

            System.exit(ExitCode.INPUT_CFG_CORRUPT.getCode());
        }
        
    }
    
    
    //
    // Application Resources
    //
    
    /** Default configuration parameters for the Query Service tools */
    private static final DpIngestionConfig  CFG_INGEST = DpApiConfig.getInstance().ingest;
    
    /** Default configuration parameters for the JAL Tools */
    private static final JalToolsConfig     CFG_TOOLS = JalToolsConfig.getInstance();
    
    
    //
    // Application Constants - Command-Line Arguments and Messages
    //
    
    /** Minimum number of application arguments - argument name and at least one data request */
    public static final int         CNT_APP_MIN_ARGS = 8;
    
    
    /** Argument variable identifying the PV name values*/
    public static final String      STR_VAR_PVS = "--pvs";
    
    /** Argument variable identifying the data type values for all PVs */
    public static final String      STR_VAR_TYPE = "--type";
    
    /** Argument variable identifying the sampling period value (Instant) for each PV */
    public static final String      STR_VAR_PERIOD = "--period";
    
    /** Argument variable identifying the samples count value for each PV */
    public static final String      STR_VAR_COUNT = "--count";
    
    /** Argument switch indicating the use of a uniform sampling clock for timestamp creation */
    public static final String      STR_SWITCH_CLOCK = "-clocked";
    
    /** Argument switch indicating the use of an explicit timestamp list for timestamp creation */
    public static final String      STR_SWITCH_TMS_LIST = "-tmslist";
    
    /** List of all the valid argument delimiters */
    public static final List<String>    LST_STR_DELIMS = List.of(
            STR_VAR_PVS, 
            STR_VAR_TYPE, 
            STR_VAR_PERIOD,
            STR_VAR_COUNT,
            STR_SWITCH_CLOCK,
            STR_SWITCH_TMS_LIST
            );
    
    
    //
    // Application Constants - Client Messages
    //
    
    /** Application name */
    public static final String      STR_APP_NAME = AddSimulatedData.class.getSimpleName();
    
    
    /** The "usage" message for client help requests or invalid application arguments */
    public static final String      STR_APP_USAGE = 
            STR_APP_NAME  + " Usage: \n"
          + "\n"
          + "% " + STR_APP_NAME
          + " [" + STR_ARG_HELP + "]"
          + " [" + STR_ARG_VERSION + "]"
          + " " + STR_VAR_PVS + " PV1 [... PVn]"
          + " " + STR_VAR_TYPE + " TYPE"
          + " " + STR_VAR_COUNT + " N"
          + " " + STR_VAR_PERIOD + " T"
          + " [" + STR_SWITCH_CLOCK + "]"
          + " [" + STR_SWITCH_TMS_LIST + "]"
          + "\n" 
          + "  Where  \n"
          + "    " + STR_ARG_HELP + "        = print this message and return.\n"
          + "    " + STR_ARG_VERSION + "     = prints application version information and return.\n"
          + "    PV1, ..., PVn = Name(s) of the process variables to add. \n"
          + "    TYPE          = Data type of all process variables - JalScalarType enumeration. \n"
          + "    N             = The number of samples for each process variable (note total duration is T*N).\n"
          + "    T             = Sampling period - numeric parsable value of format 'P[nd]DT[nh]H[nm]M.[m1...m9]S' \n"
          + "                      where nd = number of days, \n "
          + "                           nh = number of hours, \n" 
          + "                            nm = number of minutes, \n"
          + "                            m1...m9 (up to 9 digits) integer - number of seconds w/ 1 ns resolution. \n"
          + "    [" + STR_SWITCH_CLOCK + "]    = specifies use of a uniform sampling clock for timestamp generation. \n"
          + "    [" + STR_SWITCH_TMS_LIST + "]    = specifies use of explicit timestamp list for timestamp generation. \n"
          + "\n"
          + "  NOTES: \n"
          + "  - All bracketed quantities [...] are optional. \n"
          + "  - If neither " + STR_SWITCH_CLOCK + " or " + STR_SWITCH_TMS_LIST + " appear, the default is a uniform sampling clock.\n";

    
    /** The "version" message for client version requests */
    public static final String      STR_APP_VERSION = 
            STR_APP_NAME
          + " version 1.0: compatible with Java Application Library version 1.8.0 or greater.";
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>AddSimulatedData</code> instance.
     * </p>
     *
     * @param clsApp
     */
    public AddSimulatedData(Class<AddSimulatedData> clsApp) {
        super(clsApp);
        // TODO Auto-generated constructor stub
    }

    
    // 
    // JalApplicationBase Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.api.app.JalApplicationBase#isLogging()
     */
    @Override
    protected boolean isLogging() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see com.ospreydcs.dp.api.app.JalApplicationBase#getLogger()
     */
    @Override
    protected Logger getLogger() {
        // TODO Auto-generated method stub
        return null;
    }

}
