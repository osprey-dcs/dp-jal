/*
 * Project: dp-api-common
 * File:	QueryChannelTestSuite.java
 * Package: com.ospreydcs.dp.api.tools.query.channel
 * Type: 	QueryChannelTestSuite
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
 * @since May 10, 2025
 *
 */
package com.ospreydcs.dp.api.tools.query.channel;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.tools.config.DpApiToolsConfig;
import com.ospreydcs.dp.api.tools.config.query.DpApiToolsQueryConfig;
import com.ospreydcs.dp.api.tools.config.request.DpRequestSuiteConfig;
import com.ospreydcs.dp.api.tools.query.request.TestArchiveRequest;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Generator for collections of <code>QueryChannelTestCase</code> instances.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since May 10, 2025
 *
 */
public class QueryChannelTestSuite {
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new <code>QueryChannelTestSuite</code> initialized from the given configuration object.
     * </p>
     * <p>
     * The <code>DpRequestSuiteConfig</code> structure class is parsed for configuration parameters which
     * are used to populate the returned test suite.  Note that there must be at least one named
     * <code>TestArchiveRequest</code> enumeration within the argument for valid <code>QueryChannelTestSuite</code>
     * creation.
     * </p>  
     * 
     * @param cfgSuite  request suite configuration structure class containing initialization parameters
     * 
     * @return  a new <code>QueryChannelTestSuite</code> instance configured according to the argument
     */
    public static QueryChannelTestSuite from(DpRequestSuiteConfig cfgSuite) {
        
        QueryChannelTestSuite   suite = new QueryChannelTestSuite(cfgSuite.testSuite.name);
        
        if (cfgSuite.testSuite.requestIds != null)
            cfgSuite.testSuite.requestIds.forEach(enmRqst -> suite.addTestRequest(enmRqst));
        
        if (cfgSuite.testSuite.streamCounts != null)
            cfgSuite.testSuite.streamCounts.forEach(cntStrms -> suite.addStreamCount(cntStrms));
        
        if (cfgSuite.testSuite.requestComposites != null)
            cfgSuite.testSuite.requestComposites.forEach(enmCmp -> suite.addRequestDecomposition(enmCmp));
        
        if (cfgSuite.testSuite.streamTypes != null)
            cfgSuite.testSuite.streamTypes.forEach(enmType -> suite.addStreamType(enmType));
        
        return suite;
    }

    
    //
    // Application Resources
    //
    
    /** Query tools default configuration parameters */
    private static final DpApiToolsQueryConfig     CFG_DEF = DpApiToolsConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** Event logging enabled flag */
    public static final boolean     BOL_LOGGING = CFG_DEF.logging.enabled;
    
    /** Event logging level */
    public static final String      BOL_LOGGING_LEVEL = CFG_DEF.logging.level;
    
    
    /** Default gRPC stream type */
    public static final DpGrpcStreamType    ENM_STRM_TYPE_DEF = CFG_DEF.recovery.request.stream.preference;
    
    /** Default request decomposition strategy */
    public static final RequestDecompType   ENM_RQST_DCMP_DEF = RequestDecompType.NONE;
    
    /** Default gRPC stream count */
    public static final Integer             INT_STRM_CNT_DEF = 1;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = LogManager.getLogger();
    
    static {
        Configurator.setLevel(LOGGER, Level.toLevel(BOL_LOGGING_LEVEL, LOGGER.getLevel()));
    }
    
    
    //
    // Defining Attributes
    //
    
    /** The test suite name */
    private final String        strName;
    
    
    //
    // Instance Resources
    //
    
    /** Collection of stream counts for test suite */
    private final Set<Integer>              setStrmCnts = new TreeSet<>();
    
    /** Collection of request decomposition strategies for test suite */
    private final Set<RequestDecompType>    setDcmpType = new TreeSet<>();
    
    /** Collection of gRPC stream types for test suite */
    private final Set<DpGrpcStreamType>     setStrmType = new TreeSet<>();
    
    /** Collection of all test request for test suite */
    private final Set<TestArchiveRequest>   setTestRqsts = new TreeSet<>();

    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new, emtpy <code>QueryChannelTestSuite</code> instance with the given name.
     * </p>
     *
     * @param strName   name of the test suite
     */
    public QueryChannelTestSuite(String strName) {
        this.strName = strName;
    }
    
    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Adds a new time-series data request into the test suite.
     * </p>
     * <p>
     * Adds the given test archive request into the set of test requests for test case generation.
     * Test cases will be generated for the given request according to the other test suite configuration
     * parameters.
     * </p>
     * 
     * @param enmTestRqst   new <code>TestArchiveRequest</code> target for test suite generation 
     */
    public void addTestRequest(TestArchiveRequest enmTestRqst) {
        this.setTestRqsts.add(enmTestRqst);
    }
    
    /**
     * <p>
     * Adds a new gRPC stream count to be added to the test suite.
     * </p>
     * <p>
     * Note that stream counts only apply to test cases where the request decomposition strategy
     * is not equal to <code>{@link RequestDecompType#NONE}</code>.
     * </p>
     * 
     * @param cntStrms  the gRPC stream count to be included in test case generations
     */
    public void addStreamCount(int cntStrms) {
        this.setStrmCnts.add(cntStrms);
    }
    
    /**
     * <p>
     * Adds a new time-series data request decomposition strategy to the test suite.
     * </p>
     * 
     * @param enmRqstType   the request decomposition strategy to be added to generated test cases
     */
    public void addRequestDecomposition(RequestDecompType enmRqstType) {
        this.setDcmpType.add(enmRqstType);
    }
    
    /**
     * <p>
     * Adds a new gRPC stream type to the test suite.
     * </p>
     * 
     * @param enmStrmType   the gRPC stream type to be included in test case generation
     * 
     * @throws IllegalArgumentException Illegal gRPC stream type for query operations 
     */
    public void addStreamType(DpGrpcStreamType enmStrmType) throws IllegalArgumentException {
        
        // Check argument
        if (enmStrmType == DpGrpcStreamType.FORWARD) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - gRPC stream type " + enmStrmType.name() + " not allowed in query operations";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
        
        this.setStrmType.add(enmStrmType);
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates a new test suite of <code>QueryChannelTestCase</code> instances according to the current configuration.
     * </p>
     * <p>
     * If no test data request was added to the test suite an exception is thrown.  If a test suite configuration
     * parameter was not assigned then a singled default value is assigned for that parameter.  See the following 
     * class constants for test suite default configuration values.
     * <ul>
     * <li><code>{@link #INT_STRM_CNT_DEF}</code> - default stream count.</li> 
     * <li><code>{@link #ENM_RQST_DCMP_DEF}</code> - default request decomposition strategy.</li>
     * <li><code>{@link #ENM_STRM_TYPE_DEF}</code> - default gRPC stream type.</li>
     * </ul>
     * </p>
     * 
     * @return  an enumerated collection of test cases for the test suite parameters
     * 
     * @throws ConfigurationException    there are no test requests in the current test suite
     */
    public Collection<QueryChannelTestCase> createTestSuite() throws ConfigurationException {
        
        // Check state
        this.defaultConfiguration();
        
        // Create returned container and populate it by enumerating through all test suite parameters 
        List<QueryChannelTestCase>  lstCases = new LinkedList<>();
        
        for (TestArchiveRequest enmRqst : this.setTestRqsts)
            for (DpGrpcStreamType enmStrmType : this.setStrmType)
                for (RequestDecompType enmDcmpType : this.setDcmpType) {
                    
                    if (enmDcmpType == RequestDecompType.NONE) {
                        QueryChannelTestCase    recCase = QueryChannelTestCase.from(enmRqst, enmDcmpType, enmStrmType, 1);
                        
                        lstCases.add(recCase);
                        
                    } else {
                        for (Integer cntStrms : this.setStrmCnts) {

                            QueryChannelTestCase    recCase = QueryChannelTestCase.from(enmRqst, enmDcmpType, enmStrmType, cntStrms);
                            lstCases.add(recCase);
                        }
                    }
                }
        
        return lstCases;
    }
    
    /**
     * <p>
     * Prints out text description of the current test suite configuration to the given output stream.
     * </p>
     * <p>
     * The <code>strPad</code> is assumed to be optional white space characters providing left-hand
     * side padding to the field headers.
     * </p>
     * 
     * @param ps        output stream to receive text description
     * @param strPad    optional left-hand side white space padding (or <code>null</code>)
     */
    public void printOut(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        
        ps.println(strPad + "Name    : " + this.strName);
        
        ps.println(strPad + "Request IDs:");
        for (TestArchiveRequest enmRqst : this.setTestRqsts)
            ps.println(strPad + "- " + enmRqst.name());
        
        ps.println(strPad + "Request Decomposition Strategies:");
        for (RequestDecompType enmType : this.setDcmpType)
            ps.println(strPad + "- " + enmType.name());
        
        ps.println(strPad + "gRPC Stream Counts:");
        for (Integer intCnt : this.setStrmCnts)
            ps.println(strPad + "- " + intCnt);
        
        ps.println(strPad + "gRPC Stream Types:");
        for (DpGrpcStreamType enmType : this.setStrmType)
            ps.println(strPad + "- " + enmType.name());
    }

    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Checks the configuration collections and assigns a default configuration if not initialized.
     * </p>
     * <p>
     * Performs the following checks and actions:
     * <ul>
     * <li>if <code>{@link #setTestRqsts}.isEmpty() &rArr; throw IllegalStateException</code></li>
     * <li>if <code>{@link #setDcmpType}.isEmpty() &rArr; {@link #setDcmpType}.add({@link #ENM_RQST_DCMP_DEF})</code></li>
     * <li>if <code>{@link #setStrmCnts}.isEmpty() &rArr; {@link #setStrmCnts}.add({@link #INT_STRM_CNT_DEF})</code></li>
     * <li>if <code>{@link #setStrmType}.isEmpty() &rArr; {@link #setStrmType}.add({@link #ENM_STRM_TYPE_DEF})</code></li>
     * </ul>
     * </p>
     *  
     * @throws ConfigurationException    there are no test requests within the test suite
     */
    private void    defaultConfiguration() throws ConfigurationException {
        
        if (this.setTestRqsts.isEmpty()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Invalid test suite configuration, the test request collection is empty.";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new ConfigurationException(strMsg);
        }
        
        if (this.setDcmpType.isEmpty())
            this.setDcmpType.add(ENM_RQST_DCMP_DEF);
        
        if (this.setStrmCnts.isEmpty())
            this.setStrmCnts.add(INT_STRM_CNT_DEF);
        
        if (this.setStrmType.isEmpty())
            this.setStrmType.add(ENM_STRM_TYPE_DEF);
    }

}
