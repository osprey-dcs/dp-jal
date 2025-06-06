/*
 * Project: dp-api-common
 * File:	QueryToolsAppBase.java
 * Package: com.ospreydcs.dp.api.tools.app
 * Type: 	QueryToolsAppBase
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
 * @since May 28, 2025
 *
 */
package com.ospreydcs.dp.api.tools.query;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.module.FindException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.model.CfgLoaderYaml;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactoryStatic;
import com.ospreydcs.dp.api.query.model.grpc.QueryChannel;
import com.ospreydcs.dp.api.query.model.grpc.QueryMessageBuffer;
import com.ospreydcs.dp.api.tools.app.ToolsAppBase;
import com.ospreydcs.dp.api.tools.config.JalToolsConfig;
import com.ospreydcs.dp.api.tools.config.query.JalToolsQueryConfig;
import com.ospreydcs.dp.api.tools.config.request.JalRequestSuiteConfig;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Base class for Java API Library tools applications for the Data Platform Query Service.
 * </p>
 * <p>
 * This class contains common resources for JAL tools applications requiring the Query Service.  For example,
 * there are methods for creating a <code>{@link QueryChannel}</code> along with the required 
 * <code>{@link QueryMessageBuffer}</code>.  
 * </p> 
 *
 * @author Christopher K. Allen
 * @since May 28, 2025
 *
 */
public abstract class QueryToolsAppBase<T extends QueryToolsAppBase<T>> extends ToolsAppBase<T> {

    

    //
    // Application Resources
    //
    
    /** Query tools default configuration parameters */
    private static final JalToolsQueryConfig     CFG_DEF = JalToolsConfig.getInstance().query;
    

    //
    // Class Constants
    //
    
    /** The default output location (i.e., if none is provided) */
    public static final String     STR_OUTPUT_PATH_DEF = CFG_DEF.output.channel.path;

    
    //
    // Timeout Limits for Request Data Recovery (may exceed default API Library settings)
    //
    
    /** Timeout limit for requested data recovery */
    public static final long        LNG_TIMEOUT = CFG_DEF.timeout.limit;
    
    /** Timeout limit units for requested data recovery */
    public static final TimeUnit    TU_TIMEOUT = CFG_DEF.timeout.unit;
    
    
    
    //
    // Logging Parameters
    //
    
    /** Event logging enabled flag */
    public static final boolean     BOL_LOGGING = CFG_DEF.logging.enabled;

    /** Event logging level */
    public static final String      STR_LOGGING_LEVEL = CFG_DEF.logging.level;


    
    //
    // Instance Resources
    //
    
    /** The gRPC connection to the Query Service (default connection) */
    protected final DpQueryConnection                     connQuery;
    
    /** The receiver of <code>QueryData</code> Protocol Buffers messages */
    protected final QueryMessageBuffer                    bufDataMsgs;
    
    /** The <code>QueryChannel</code> object available for data recover and/or evaluations */
    protected final QueryChannel                          chanQuery;


    //
    // State Variables
    //
    
    /** Application tool resources shut down flag */
    protected boolean   bolShutdown = false;
    

    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>QueryToolsAppBase</code> instance.
     * </p>
     *
     * @param clsApp    the class instance of the final application
     * 
     * @throws DpGrpcException unable to establish connection to the Query Service (see message and cause)
     */
    protected QueryToolsAppBase(Class<T> clsApp) throws DpGrpcException {
        super(clsApp);
        
        // Create the channel resources and channel
        this.bufDataMsgs = QueryMessageBuffer.create();
        this.connQuery = DpQueryConnectionFactoryStatic.connect();  // throws DpGrpcException
        this.chanQuery = QueryChannel.from(connQuery, bufDataMsgs);
        
        // Set timeout for channel operation and for request operation
        this.connQuery.setTimeoutLimit(LNG_TIMEOUT, TU_TIMEOUT);
        this.chanQuery.setTimeoutLimit(LNG_TIMEOUT, TU_TIMEOUT);
    }

    
    //
    // ToolsAppBase Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.api.tools.app.ToolsAppBase#isLogging()
     */
    @Override
    protected boolean   isLogging() {
        return BOL_LOGGING;
    }
    
    
    //
    // Operations
    //

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
     * Shuts down the application resources.
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
            
            // Close the application (output stream)
            super.close();
            
            this.bolShutdown = true;
        }
    }   

    
    //
    // Subclass Support Methods
    //
    
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
    protected JalRequestSuiteConfig recoverTestSuiteConfig(String strName) throws FindException {
        
        // Check if name is a pre-defined test suite (in the API Tools configuration)
        List<JalRequestSuiteConfig>  lstCfgsAll = CFG_DEF.testRequests.testSuites;
        
        List<JalRequestSuiteConfig>  lstCfgTarget = lstCfgsAll.stream().filter(cfg -> strName.equals( cfg.testSuite.name) ).toList();
        if (!lstCfgTarget.isEmpty())
            return lstCfgTarget.getFirst();
        
        // Check if name is a valid file location (name is not a pre-defined test suite)
//        try {
//            Paths.get(strName);     // throws InvalidPathException 
//            
//        } catch (InvalidPathException | NullPointerException e) {
//            
//            if (BOL_LOGGING)
//                LOGGER.error(strErrMsg1);
//            
//            throw new FindException(strErrMsg1, e);
//        }
        
        // Check if name is a valid file
        File file = new File(strName);
        
        if (!file.exists() || !file.isFile()) {
            String  strErrMsg1 = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Invalid test suite name '" + strName 
                    + "': neither a named pre-defined test suite nor a valid file location.";

            if (BOL_LOGGING)
                this.getLogger().error(strErrMsg1);
            
            throw new FindException(strErrMsg1);
        }
       
        // Attempt to load test suite configuration from file (name is a valid file)
        try {
            JalRequestSuiteConfig cfg = CfgLoaderYaml.load(file, JalRequestSuiteConfig.class);
            
            return cfg;
            
        } catch (FileNotFoundException | SecurityException e) {
            String  strErrMsg2 = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Input " + strName + " was not a valid test suite configuration file.";
            
            if (BOL_LOGGING)
                this.getLogger().error(strErrMsg2);
            
            throw new IllegalArgumentException(strErrMsg2, e);
        }
    }
    
}
