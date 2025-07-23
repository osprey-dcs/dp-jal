/*
 * Project: dp-api-common
 * File:	CorrelatorTestSuiteConfig.java
 * Package: com.ospreydcs.dp.jal.tools.query.correl
 * Type: 	CorrelatorTestSuiteConfig
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
 * @since May 31, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.correl;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;
import com.ospreydcs.dp.jal.tools.query.request.TestArchiveRequest;

/**
 * <p>
 * Configurable class that creates collections of <code>CorrelatorTestCase</code> instances.
 * </p>
 * <p>
 * Generates collections of <code>{@link CorrelatorTestCase}</code> instances, or "test suites",
 * according to dynamic configuration.  Specifically, a new <code>CorrelatorTestSuiteConfig</code> instance
 * is first configured using methods
 * <ul>
 * <li><code>{@link #addTestRequest(TestArchiveRequest)}</code></li>
 * <li><code>{@link #addMaxThreadCount(int)}</code></li>
 * <li><code>{@link #addConcurrencyPivotSize(int)}</code></li>
 * </ul>
 * which all can be called repeatedly to create a configuration.
 * Once configured, method <code>{@link #createTestSuite()}</code> is used create collections of
 * <code>{@link CorrelatorTestCase}</code> mapped to the <code>{@link TestArchiveRequest}</code>
 * which they use.  Of course the <code>{@link #createTestSuite()}</code> method can be called
 * at any point.   
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * At least one <code>{@link TestArchiveRequest}</code> constant must be added to the configuration before
 * test suite creation.  
 * </li>
 * <li>
 * Default values will be supplied for the rest of the above parameters if they are not provided.
 * </li>
 * </ul>
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since May 31, 2025
 *
 */
public class CorrelatorTestSuiteConfig {
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new, empty <code>CorrelatorTestSuiteConfig</code> ready for configuration.
     * </p>
     * 
     * @return  a new unconfigured instance of <code>CorrelatorTestSuiteConfig</code>
     */
    public static CorrelatorTestSuiteConfig   create() {
        return new CorrelatorTestSuiteConfig();
    }
    

    //
    // Application Resources
    //
    
    /** Query tools default configuration parameters */
    private static final DpQueryConfig     CFG_DEF = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** Event logging enabled flag */
    public static final boolean     BOL_LOGGING = CFG_DEF.logging.enabled;
    
    /** Event logging level */
    public static final String      STR_LOGGING_LEVEL = CFG_DEF.logging.level;
    
    
    /** Default maximum thread count */
    public static final int         CNT_MAX_THRD_DEF = CFG_DEF.data.recovery.correlate.concurrency.maxThreads;
    
    /** Default concurrency pivot size */
    public static final int         SZ_CONC_PIVOT_DEF = CFG_DEF.data.recovery.correlate.concurrency.pivotSize;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLoggerSetLevel(STR_LOGGING_LEVEL);
    
    
    /** Standard "concurrency enabled" selections **/
    private static final Set<Boolean>       SET_ENABLE = Set.of(false, true);
    
    
    //
    // Defining Attributes
    //
    
    
    //
    // Instance Configuration
    //
    
    /** Collection of all test request for test suite */
    private final Set<TestArchiveRequest>   setTestRqsts = new TreeSet<>();
    
    /** Collection of supplementary PV names to add to all test requests */
    private final Set<String>               setSupplPvs = new TreeSet<>();

    /** Collection of maximum thread counts for test suite */
    private final Set<Integer>              setMaxThrdCnts = new TreeSet<>();
    
    /** Collection of request decomposition strategies for test suite */
    private final Set<Integer>              setPivotSize = new TreeSet<>();
    

    //
    //  Constructors
    // 
    
    /**
     * <p>
     * Constructs a new, unconfigured <code>CorrelatorTestSuiteConfig</code> instance.
     * </p>
     *
     */
    public CorrelatorTestSuiteConfig() {
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
     * Adds a new PV name to the collection of supplemental PV names.
     * </p>
     * <p>
     * Supplemental PVs are added to all Data Platform Test Archive Requests <code>TestArchiveRequest</code>
     * defined in this configuration.  Thus, the actual time-series data request performed is the original
     * request augment by the collection of supplemental PV names within the configuration.
     * </p>
     * <p>
     * Any duplicate PV names will only appear once in the augmented time-series data request.
     * </p>
     * 
     * @param strPvName     PV name to be supplemented to the final time-series data request
     */
    public void addSupplementalPv(String strPvName) {
        this.setSupplPvs.add(strPvName);
    }
    
    /**
     * <p>
     * Adds a collection of PV names to the collection of supplemental PV names.
     * </p>
     * <p>
     * Supplemental PVs are added to all Data Platform Test Archive Requests <code>TestArchiveRequest</code>
     * defined in this configuration.  Thus, the actual time-series data request performed is the original
     * request augment by the collection of supplemental PV names within the configuration.
     * </p>
     * <p>
     * Any duplicate PV names will only appear once in the augmented time-series data request.
     * </p>
     * 
     * @param setPvNames    a collection of PV names to be supplemented to the final time-series data request
     */
    public void addSupplementalPvs(Collection<String> setPvNames) {
        this.setSupplPvs.addAll(setPvNames);
    }
    
    /**
     * <p>
     * Adds a collection of maximum allowable thread count values to the test suite configuration.
     * </p>
     * 
     * @param setMaxThrdCnts    collection of maximum allowable thread count value for test suite generation
     */
    public void addMaxThreadCounts(Collection<Integer> setMaxThrdCnts) {
        this.setMaxThrdCnts.addAll(setMaxThrdCnts);
    }
    
    /**
     * <p>
     * Adds a new maximum allowable thread count value to the test suite configuration.
     * </p>
     * 
     * @param cntMaxThrds new maximum allowable thread count value for test suite generation
     */
    public void addMaxThreadCount(int cntMaxThrds) {
        this.setMaxThrdCnts.add(cntMaxThrds);
    }
    
    /**
     * <p>
     * Adds a new colleciton concurrency pivot size limits to the test suite configuration.
     * </p>
     * <p>
     * Note that "concurrency pivot sizes" are available to avoid wasting computation on multi-threading overhead 
     * resources for trivial processing tasks. The typical situation is if a target set has size less than the 
     * pivot size all processing of the target set is done serially; otherwise it is done on multiple threads. 
     * </p>
     * 
     * @param setPivotSizes collection of concurrency pivot sizes for test suite generation
     */
    public void addConcurrencyPivotSizes(Collection<Integer> setPivotSizes) {
        this.setPivotSize.addAll(setPivotSizes);
    }
    
    /**
     * <p>
     * Adds a new concurrency pivot size limit to the test suite configuration.
     * </p>
     * <p>
     * Note that "concurrency pivot sizes" are available to avoid wasting computation on multi-threading overhead 
     * resources for trivial processing tasks. The typical situation is if a target set has size less than the 
     * pivot size all processing of the target set is done serially; otherwise it is done on multiple threads. 
     * </p>
     * 
     * @param szPivot   a new concurrency pivot size for test suite generation
     */
    public void addConcurrencyPivotSize(int szPivot) {
        this.setPivotSize.add(szPivot);
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates a new test suite of <code>CorrelatorTestCase</code> instances according to the current configuration.
     * </p>
     * <p>
     * Note that the return value is map keyed by <code>DpDataRequest</code> instances.  The value of the
     * map is a container of all <code>CorrelatorTestCase</code> instances that use that time-series data request.
     * This format is provide so that the data request need only be performed once and the recovered data then
     * used for each test case evaluation within the list keyed by that request.
     * </p> 
     * <p>
     * The <code>DpDataRequest</code> instances forming the map keys will all be augmented by the collection
     * of supplemental PV names assigned by <code>{@link #addSupplementalPv(String)}</code> or 
     * <code>{@link #addSupplementalPvs(Collection)}</code> (if any).
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
     * @throws ConfigurationException    there are no test requests in the current test suite
     */
    public Map<DpDataRequest, List<CorrelatorTestCase>> createTestSuite() throws ConfigurationException {
        
        // Check state
        this.defaultConfiguration();
        
        // Create returned container and populate it by enumerating through all test suite parameters 
        Map<DpDataRequest, List<CorrelatorTestCase>>  mapCases = new HashMap<>();
        
        for (TestArchiveRequest enmRqst : this.setTestRqsts) {
            
            // Create the data request name
            String          strIdAug = "";
            if (!this.setSupplPvs.isEmpty())
                strIdAug = "+" + this.setSupplPvs;
            
            // Create the data request
            DpDataRequest   rqst = enmRqst.create();
            rqst.selectSources(this.setSupplPvs);
            rqst.setRequestId(strIdAug);
            
            // Create the list of test cases and populate if for this request
            List<CorrelatorTestCase>    lstCases = new LinkedList<>();
            
            for (Boolean bolEnable : SET_ENABLE) {
                if (!bolEnable) {
                    CorrelatorTestCase  recCase = CorrelatorTestCase.from(enmRqst, this.setSupplPvs, false, 1, 1);

                    lstCases.add(recCase);

                } else {
                    for (Integer cntMaxThrds : this.setMaxThrdCnts)
                        for (Integer szPivot : this.setPivotSize) { 
                            CorrelatorTestCase  recCase = CorrelatorTestCase.from(enmRqst, this.setSupplPvs, true, cntMaxThrds, szPivot);

                            lstCases.add(recCase);
                        }
                }
            }
            
            mapCases.put(rqst, lstCases);
        }
        
        return mapCases;
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
        
        // Supplemental PV name request ID augmentation
        String  strIdAug = "";
        if (!this.setSupplPvs.isEmpty())
            strIdAug = "+" + this.setSupplPvs;
        
        ps.println(strPad + "Request IDs:");
        for (TestArchiveRequest enmRqst : this.setTestRqsts)
            ps.println(strPad + "- " + enmRqst.name() + strIdAug);
        
        ps.println(strPad + "Supplemental PV Names:");
        for (String strPvNm : this.setSupplPvs)
            ps.println(strPad + "- " + strPvNm);
        
        ps.println(strPad + "Maximum Thread Counts:");
        for (Integer cntMaxThrds : this.setMaxThrdCnts)
            ps.println(strPad + "- " + cntMaxThrds);
        
        ps.println(strPad + "Concurrency Pivot Sizes:");
        for (Integer szPivot : this.setPivotSize)
            ps.println(strPad + "- " + szPivot);
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
     * <li>if <code>{@link #setMaxThrdCnts}.isEmpty() &rArr; {@link #setDcmpType}.add({@link #DUR_RQST_TM_DEF})</code></li>
     * <li>if <code>{@link #setPvitSize}.isEmpty() &rArr; {@link #setPivotSize}.add({@link #SZ_CONC_PIVOT_DEF})</code></li>
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
        
        if (this.setMaxThrdCnts.isEmpty())
            this.setMaxThrdCnts.add(CNT_MAX_THRD_DEF);
        
        if (this.setPivotSize.isEmpty())
            this.setPivotSize.add(SZ_CONC_PIVOT_DEF);
    }

}
