/*
 * Project: dp-api-common
 * File:	QueryAssemblyTestSuiteConfig.java
 * Package: com.ospreydcs.dp.jal.tools.query.assem
 * Type: 	QueryAssemblyTestSuiteConfig
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
 * @since Jul 8, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.assem;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.query.request.TestArchiveRequest;

/**
 * <p>
 * Class for generating collections of test cases for the <code>QueryAssemblyEvaluator</code> application.
 * </p>
 * <p>
 * Class instances are configurable to generate test cases of type <code>{@link QueryAssemblyTestCase}</code> with
 * varying parameters.
 * </p>
 * <p>
 * A <code>QueryAssemblyTestSuiteConfig</code> instance is created in an unconfigured state.  Clients
 * must configure the instance using the available configuration methods before calling the
 * <code>{@link #createTestSuite()}</code> method which returns a collection (i.e., "suite") of
 * <code>{@link QueryAssemblyTestCase}</code> instances according to the current configuration.
 * </p>
 * <p>
 * <h2>Configuration</h2>
 * The test suite configuration consists of the following properties:
 * <ul>
 * <li>A (optional) collection of <code>{@link TestArchiveRequest}</code> time-series data requests.</li>
 * <li>A (optional) collection of supplemental PV names to be added to each <code>TestArchiveRequest</code>.</li>
 * <li>An (optional) override of each time-series data request duration.</li>
 * <li>An (optional) delay time for each time-series data request.</li>
 * </ul>
 * A valid test suite configuration has the following requirements:
 * <ul>
 * <li>The configuration must contain either at least one <code>TestArchiveRequest</code> with no supplemental PVs.</li>
 * <li>If no <code>TestArchiveRequest</code> enumerations are specified then there must be at least one supplemental PV.</li>
 * <li>If no <code>TestArchiveRequest</code> enumerations are specified then the data request duration must be specified.</li>
 * <li>The last two properties are optional when <code>TestArchiveRequest</code> enumerations are specified;
 *     the time-range of each <code>TestArchiveRequest</code> remains unchanged.</li>
 * </ul> 
 * 
 * @author Christopher K. Allen
 * @since Jul 8, 2025
 *
 */
public class QueryAssemblyTestSuiteConfig {

    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new, empty <code>SuperDomTestSuiteConfig</code> ready for configuration.
     * </p>
     * 
     * @return  a new unconfigured instance of <code>SuperDomTestSuiteConfig</code>
     */
    public static QueryAssemblyTestSuiteConfig   create() {
        return new QueryAssemblyTestSuiteConfig();
    }
    

    //
    // Application Resources
    //
    
    /** Query tools default configuration parameters */
    private static final DpQueryConfig      CFG_QUERY = DpApiConfig.getInstance().query;
    
    /** JAL Tools configuration parameters */
    private static final JalToolsConfig     CFG_TOOLS = JalToolsConfig.getInstance();
    
    
    //
    // Class Constants
    //
    
    /** Class name - used for request ID when no TestArchiveRequest constants are specified */
    public static final String      STR_CLS_NAME = QueryAssemblyTestSuiteConfig.class.getSimpleName();

    
    /** Event logging enabled flag */
    public static final boolean     BOL_LOGGING = CFG_QUERY.logging.enabled;
    
    /** Event logging level */
    public static final String      STR_LOGGING_LEVEL = CFG_QUERY.logging.level;
    
    
    /** The inception time of the Data Platform Test Archive as an ISO formatted string */
    public static final String      STR_ARCHIVE_INCEPT = CFG_TOOLS.testArchive.range.start;
    
    /** The inception time of the Data Platform Test Archive as parsed from {@link #STR_ARCHIVE_INCEPT} */
    public static final Instant     INS_ARCHIVE_INCEPT = Instant.parse(STR_ARCHIVE_INCEPT);
    
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLoggerSetLevel(STR_LOGGING_LEVEL);
    
    
    //
    // Instance Resources - Configuration
    //
    
    /** Container for all test request for test suite generation */
    private final Set<TestArchiveRequest>   setTestRqsts = new TreeSet<>();
    
    /** Container for all supplemental PV names to add to requested data */
    private final Set<String>               setPvNames = new TreeSet<>();

    
    //
    // Instance Attributes - Configuration
    //
    
    /** The overriding request duration if provided - otherwise range of TestArchiveRequest */
    private Duration        durRange = null;
    
    /** The overriding request start delay if provided - otherwise ZERO */
    private Duration        durDelay = null;
    

    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>QueryAssemblyTestSuiteConfig</code> instance ready for configuration.
     * </p>
     *
     */
    public QueryAssemblyTestSuiteConfig() {
    }


    //
    // Configuration
    //

    /**
     * <p>
     * Adds a collection of time-series data requests to the test suite configuration.
     * </p>
     * <p>
     * Adds the given collection of test archive requests into the set of test requests for test case generation.
     * Test cases will be generated for the given requests according to the other test suite configuration
     * parameters.
     * </p>
     * 
     * @param setRqsts      collection of <code>TestArchiveRequest</code> targets for test suite generation
     */
    public void addTestRequests(Collection<TestArchiveRequest> setRqsts) {
        this.setTestRqsts.addAll(setRqsts);
    }
    
    /**
     * <p>
     * Adds a new time-series data request to the test suite configuration.
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
     * Adds a collection of Process Variable names to collection supplementing each <code>TestArchiveRequest</code>.
     * <p>
     * <p>
     * The given collection of PV names will be added to the time-series data request provided by each
     * <code>TestArchiveRequest</code> in the configuration.  Specifically, the data request generated by
     * <code>{@link TestArchiveRequest#create()}</code> will be supplemented with the given PV names,
     * more specifically, all PV names added to the current configuration.
     * </p>
     *  
     * @param setPvNames  collection of PV names to add to each <code>TestArchiveRequest</code> within configuration
     */
    public void addPvNames(Collection<String>   setPvNames) {
        this.setPvNames.addAll(setPvNames);
        
    }
    
    /**
     * <p>
     * Adds the given Process Variable name to collection supplementing each <code>TestArchiveRequest</code>.
     * <p>
     * <p>
     * The given PV name will be added to the time-series data request provided by each
     * <code>TestArchiveRequest</code> in the configuration.  Specifically, the data request generated by
     * <code>{@link TestArchiveRequest#create()}</code> will be supplemented with the given PV name
     * more specifically, all PV names added to the current configuration.
     * </p>
     * 
     * @param strPvName PV name to add to each <code>TestArchiveRequest</code> within configuration
     */
    public void addPvName(String strPvName) {
        this.setPvNames.add(strPvName);
    }
    
    /**
     * <p>
     * Overrides all <code>TestArchiveRequest</code> request durations in the configuration with the given value.
     * </p>
     * <p>
     * The methods <code>{@link #setRequestRange(Duration)}</code> and <code>{@link #setRequestDelay(Duration)}</code>
     * can be used to override the time-series data request time ranges of all <code>TestArchiveRequest</code> instances
     * within the current configuration.  Normally, all request start times for any <code>TestArchiveRequest</code>
     * are taken as the inception time of the Data Platform Test Archive 
     * (see <code>{@link #INS_ARCHIVE_INCEPT}</code>).
     * The end time for each request is provided in the request definition.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * If <code>{@link #setRequestDelay(Duration)}</code> is not set then each request range will be given by
     * time interval [<i>t</i><sub>incept</sub>, <i>t</i><sub>incept</sub>+<code>durRange</code>] where 
     * <i>t</i><sub>incept</sub> is the inception time of the Data Platform Test Archive.
     * </li>
     * <li>  
     * If <code>{@link #setRequestDelay(Duration)}</code> has been set then each request range will be given by
     * time interval[<i>t</i><sub>incept</sub>+<code>durDelay</code>, <i>t</i><sub>incept</sub>+<code>durDelay</code>+<code>durRange</code>] 
     * where <i>t</i><sub>incept</sub> is the inception time of the Data Platform Test Archive.
     * </li>
     * </ul>
     * </p>
     *  
     * @param durRange  time-series data request range override value
     */
    public void setRequestRange(Duration durRange) {
        this.durRange = durRange;
    }
    
    /**
     * <p>
     * Overrides all <code>TestArchiveRequest</code> request start times in the configuration with the given value.
     * </p>
     * <p>
     * The methods <code>{@link #setRequestRange(Duration)}</code> and <code>{@link #setRequestDelay(Duration)}</code>
     * can be used to override the time-series data request time ranges of all <code>TestArchiveRequest</code> instances
     * within the current configuration.  Normally, all request start times for any <code>TestArchiveRequest</code>
     * are taken as the inception time of the Data Platform Test Archive 
     * (see <code>{@link #INS_ARCHIVE_INCEPT}</code>).
     * The end time for each request is provided in the request definition.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * If <code>{@link #setRequestDelay(Duration)}</code> is not set then each request range will be given by
     * time interval [<i>t</i><sub>incept</sub>, <i>t</i><sub>incept</sub>+<code>durRange</code>] where 
     * <i>t</i><sub>incept</sub> is the inception time of the Data Platform Test Archive.
     * </li>
     * <li>  
     * If <code>{@link #setRequestDelay(Duration)}</code> has been set then each request range will be given by
     * time interval[<i>t</i><sub>incept</sub>+<code>durDelay</code>, <i>t</i><sub>incept</sub>+<code>durDelay</code>+<code>durRange</code>] 
     * where <i>t</i><sub>incept</sub> is the inception time of the Data Platform Test Archive.
     * </li>
     * </ul>
     * </p>
     * 
     * @param durDelay  time-series data request delay override value
     */
    public void setRequestDelay(Duration durDelay) {
        this.durDelay = durDelay;
    }
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates a new test suite of <code>SuperDomTestCase</code> instances according to the current configuration.
     * </p>
     * <p>
     * Note that the return value is map keyed by <code>TestArchiveRequest</code> instances.  The value of the
     * map is a container of all <code>CorrelatorTestCase</code> instances that use that time-series data request.
     * This format is provide so that the data request need only be performed once and the recovered data then
     * used for each test case evaluation within the list keyed by that request.
     * </p> 
     * <p>
     * If no time-series test request was added to the test suite an exception is thrown.  If a test suite configuration
     * parameter was not assigned then a single default value is assigned for that parameter.  See the following 
     * class constants for test suite default configuration values.
     * <ul>
     * <li><code>{@link #DUR_RQST_TM_DEF}</code> - default maximum thread count.</li> 
     * <li><code>{@link #SZ_CONC_PIVOT_DEF}</code> - default concurrency pivot size.</li>
     * </ul>
     * </p>
     * 
     * @return  an enumerated collection of test cases for the test suite parameters
     * 
     * @throws ConfigurationException    either no test requests or supplemental PVs, or only supplemental PVs an no duration
     */
    public Collection<QueryAssemblyTestCase> createTestSuite() throws ConfigurationException {
        
        // Check state
        this.checkConfiguration();  // throws ConfigurationException
        
        // Special Case - no TestArchiveDataRequest constants w/in configuration, only PV names, only one case
        if (this.setTestRqsts.isEmpty()) {
            DpDataRequest       rqst = this.createRequest(this.setPvNames);
            TestArchiveRequest  enmRqst = TestArchiveRequest.EMPTY_REQUEST;
            QueryAssemblyTestCase    recCase = QueryAssemblyTestCase.from(enmRqst, this.setPvNames, this.durRange, this.durDelay, rqst);
            
            return List.of(recCase);
        }
        
        // General Case - Supplement TestArchiveRequest constant with additional PV names
        //  Create returned container and populate it by enumerating through all test suite parameters 
        List<QueryAssemblyTestCase>  lstCases = new ArrayList<>(this.setTestRqsts.size());
        
        for (TestArchiveRequest enmRqst : this.setTestRqsts) {
            DpDataRequest       rqst = this.createRequest(enmRqst);
            QueryAssemblyTestCase    recCase = QueryAssemblyTestCase.from(enmRqst, this.setPvNames, this.durRange, this.durDelay, rqst);
            
            lstCases.add(recCase);
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
     * @param strPad    optional left-hand side white space padding, (or <code>null</code>)
     */
    public void printOut(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        
        ps.println(strPad + "Test Archive request IDs:");
        for (TestArchiveRequest enmRqst : this.setTestRqsts)
            ps.println(strPad + " - " + enmRqst.name());
        
        ps.println(strPad + "Supplementing PV names:");
        for (String strPvNm : this.setPvNames)
            ps.println(strPad + " - " + strPvNm);
        
        ps.println(strPad + "Request duration override : " + this.durRange);
        ps.println(strPad + "Request delay override    : " + this.durDelay);
    }

    //
    // Support Methods
    //
    
    /**
     * <p>
     * Checks the current test suite for invalid configuration.
     * </p>
     * <p>
     * Performs the following checks and actions:
     * <ul>
     * <li>if <code>{@link #setTestRqsts}.isEmpty() && <code>{@link #setPvNames}.isEmpty()</code> </li>
     * <li>if <code>{@link #setTestRqsts}.isEmpty() && {@link #durRange} == null)</code></li>
     * </ul>
     * </p>
     *  
     * @throws ConfigurationException   invalid test suite configuration, cannot make viable time-seres request
     */
    private void    checkConfiguration() throws ConfigurationException {
        
        if (this.setTestRqsts.isEmpty() && this.setPvNames.isEmpty()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Invalid test suite configuration, both Test Archive Request and Supplemental PV names collections are empty.";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new ConfigurationException(strMsg);
        }

        if (this.setTestRqsts.isEmpty() && this.durRange==null) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Invalid test suite configuration, Test Archive Rquest collection empty and no request duration specified.";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new ConfigurationException(strMsg);
        }
        
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance from the given test archive request and the current configuration.
     * </p>
     * <p>
     * The base request is formed using the method <code>{@link TestArchiveRequest#create()}</code>.  The following
     * modifications are then performed to the base request:
     * <ul>
     * <li>
     * If any supplemental PV names exist in the container <code>{@link #setPvNames}</code> they are added to the request.
     * </li>
     * <li>
     * If the <code>#{@link #durRange}</code> attribute is non-null the range of the returned request is changed
     * to that value.
     * </li>
     * <li>
     * If the <code>{@link #durDelay}</code> attribute is non-null the start time and final time of the request are
     * delayed by that amount.
     * </li>
     * </ul>
     * </p>  
     * 
     * @param enmRqst   the <code>TestArchiveRequest</code> constant forming the base request
     * 
     * @return  new <code>DpDataRequest</code> object formed from the given request and the current configuration parameters
     */
    private DpDataRequest   createRequest(TestArchiveRequest enmRqst) {
        
        // Create the base request
        DpDataRequest   rqst = enmRqst.create();
        
        // Add supplemental PV names
        rqst.selectSources(this.setPvNames);

        // If the request range or duration have no been modified there is nothing left to do
        if (this.durRange==null && this.durDelay==null)
            return rqst;

        // Set up the new time range for the request
        Instant     insStart = rqst.getInitialTime();
        Duration    durRqst = rqst.rangeDuration();
        
        if (this.durRange!=null)
            durRqst = this.durRange;
        
        if (this.durDelay!=null)
            insStart = insStart.plus(this.durDelay);

        // Set the time range for the request and return it
        Instant     insFinal = insStart.plus(durRqst);

        rqst.rangeBetween(insStart, insFinal);
        
        return rqst;
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> for the the given collection of PV names. 
     * </p>
     * <p>
     * This method is used when the configuration does not contain <code>TestArchiveData</code> instances, that is,
     * the collection <code>{@link #setTestRqsts}</code> is empty.
     * </p>
     * <p>
     * The returned request is built from the collection of PV names <code>{@link #setPvNames}</code> and the durations
     * <code>{@link #durRange}</code> and <code>{@link #durDelay}</code>.  If <code>{@link #durDelay}</code> is <code>null</code>
     * then the start time is the archive inception time <code>{@link #INS_ARCHIVE_INCEPT}</code>, otherwise it is
     * the inception time plus the delay.  
     * </p>
     * 
     * @param strPvNames    
     * @return
     */
    private DpDataRequest   createRequest(Set<String> strPvNames) {
        
        // Create the empty request
        DpDataRequest   rqst = DpDataRequest.create();
        rqst.setRequestId(STR_CLS_NAME + "-" + strPvNames);
        
        // Set the requested PV names
        rqst.selectSources(strPvNames);
        
        Instant     insStart = INS_ARCHIVE_INCEPT;
        Duration    durRqst = this.durRange;
        
        if (this.durDelay!=null)
            insStart = insStart.plus(durRqst);

        // Set the time range for the request and return it
        Instant     insFinal = insStart.plus(durRqst);

        rqst.rangeBetween(insStart, insFinal);
        
        return rqst;
    }

}
