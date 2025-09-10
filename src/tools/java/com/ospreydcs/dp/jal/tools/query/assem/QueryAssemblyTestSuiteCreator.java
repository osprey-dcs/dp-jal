/*
 * Project: dp-api-common
 * File:	QueryAssemblyTestSuiteCreator.java
 * Package: com.ospreydcs.dp.jal.tools.query.assem
 * Type: 	QueryAssemblyTestSuiteCreator
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
 * @since Aug 8, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.assem;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.JalDataTableType;
import com.ospreydcs.dp.api.config.JalConfig;
import com.ospreydcs.dp.api.config.query.JalQueryConfig;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.query.testrequests.TestArchiveRequest;

/**
 * <p>
 * Class for generating collections of test cases for the <code>QueryAssemblyEvaluator</code> application.
 * </p>
 * <p>
 * Class instances are configurable to generate test cases of type <code>{@link QueryAssemblyTestCase}</code> with
 * varying parameters.
 * </p>
 * <p>
 * A <code>QueryAssemblyTestSuiteCreator</code> instance is created in an unconfigured state.  Clients
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
 * <li>An (optional) collection of data table types.</li>
 * </ul>
 * A valid test suite configuration has the following requirements:
 * <ul>
 * <li>The configuration must contain either at least one <code>TestArchiveRequest</code> with no supplemental PVs.</li>
 * <li>If no <code>TestArchiveRequest</code> enumerations are specified then there must be at least one supplemental PV.</li>
 * <li>If no <code>TestArchiveRequest</code> enumerations are specified then the data request duration must be specified.</li>
 * <li>The last two properties are optional when <code>TestArchiveRequest</code> enumerations are specified;
 *     the time-range of each <code>TestArchiveRequest</code> remains unchanged.</li>
 * </ul> 
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Aug 8, 2025
 *
 */
public class QueryAssemblyTestSuiteCreator {
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new, empty <code>QueryAssemblyTestSuiteCreator</code> ready for configuration.
     * </p>
     * 
     * @return  a new unconfigured instance of <code>QueryAssemblyTestSuiteCreator</code>
     */
    public static QueryAssemblyTestSuiteCreator create() {
        return new QueryAssemblyTestSuiteCreator();
    }
    

    //
    // Library Resources
    //
    
    /** Query tools default configuration parameters */
    private static final JalQueryConfig      CFG_QUERY = JalConfig.getInstance().query;
    
    /** JAL Tools configuration parameters */
    private static final JalToolsConfig     CFG_TOOLS = JalToolsConfig.getInstance();
    
    
    //
    // Class Constants
    //
    
    /** Class name - used for request ID when no TestArchiveRequest constants are specified */
    public static final String      STR_CLS_NAME = QueryAssemblyTestSuiteCreator.class.getSimpleName();

    
    /** Event logging enabled flag */
    public static final boolean     BOL_LOGGING = CFG_QUERY.logging.enabled;
    
    /** Event logging level */
    public static final String      STR_LOGGING_LEVEL = CFG_QUERY.logging.level;
    
    
    /** The inception time of the Data Platform Test Archive as an ISO formatted string */
    public static final String      STR_ARCHIVE_INCEPT = CFG_TOOLS.testArchive.range.start;
    
    /** The inception time of the Data Platform Test Archive as parsed from {@link #STR_ARCHIVE_INCEPT} */
    public static final Instant     INS_ARCHIVE_INCEPT = Instant.parse(STR_ARCHIVE_INCEPT);
    
    
    /** The default value for assemble advanced error checking enable/disable */
    public static final boolean     BOL_ASSM_ERRCHK_ENBL_DEF = CFG_QUERY.data.table.construction.errorChecking;
    
    /** The default value for assembly time-domain collision enable/disable */
    public static final boolean     BOL_ASSM_TMDOM_COLL_ENBL_DEF = CFG_QUERY.data.table.construction.domainCollision;
    
    /** The default value for assembly concurrency enable/disable */
    public static final boolean     BOL_ASSM_CONC_ENBL_DEF = CFG_QUERY.data.table.construction.concurrency.enabled;
    
    /** The default pivot size for concurrent sampled aggregate assembly */
    public static final int         INT_ASSM_CONC_PVT_SZ_DEF = CFG_QUERY.data.table.construction.concurrency.pivotSize;
    
    /** The default maximum thread count for concurrent sampled aggregate assembly */
    public static final int         INT_ASSM_CONC_MAX_THRDS_DEF = CFG_QUERY.data.table.construction.concurrency.maxThreads;
    
    
    /** The default data table type for data table creation */
    public static final JalDataTableType    ENM_TBL_TYPE_DEF = CFG_QUERY.data.table.result.type;
    
    /** The use static table as default flag in automatic data table creation */
    public static final boolean             BOL_TBL_STAT_DEF = CFG_QUERY.data.table.result.staticTbl.isDefault;
            
    /** The maximum size enable/disable flag for static data tables in automatic type creation */
    public static final boolean             BOL_TBL_STAT_MAX_ENBL_DEF = CFG_QUERY.data.table.result.staticTbl.maxSizeEnable;
    
    /** The maximum size of a static data table for automatic type creation */
    public static final long                LNG_TBL_STAT_MAX_SZ_DEF = CFG_QUERY.data.table.result.staticTbl.maxSize;   

    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLoggerSetLevel(STR_LOGGING_LEVEL);
    
    
    //
    // Instance Resources - Configuration
    //
    
    /** Container for all test request for test suite generation */
    private final Set<TestArchiveRequest>   setEnmTestRqsts = new TreeSet<>();
    
    /** Container for all supplemental PV names to add to requested data */
    private final Set<String>               setStrSupplPvs = new TreeSet<>();
    
    
    /** Container for all sampled aggregate assembly advanced error checking enable/disable flags */
    private final Set<Boolean>              setBolAggrErrEnbls = new TreeSet<>();
    
    /** Container for all sampled aggregate assembly time-domain collision enable/disable flags */
    private final Set<Boolean>              setBolAggrTmDmCollEnbls = new TreeSet<>();
    
    /** Container for all sampled aggregate assembly concurrency enable/disable flags */
    private final Set<Boolean>              setBolAggrConcEnbls = new TreeSet<>();
    
    /** Container for pivot sizes in concurrent sampled aggregate assembly */
    private final Set<Integer>              setIntAggrConcPvtSzs = new TreeSet<>();
    
    /** Container for maximum thread counts in concurrent sampled aggregate assembly */
    private final Set<Integer>              setIntAggrConcMaxThrds = new TreeSet<>();
    
    
    /** Container for all data table creation enable/disable flags */
    private final Set<Boolean>              setBolTblBldEnbls = new TreeSet<>();
    
    /** Container for all data table types for test suite generation */
    private final Set<JalDataTableType>     setEnmTblTypes = new TreeSet<>();
    
    /** Container for all data table static table default enable/disable in automatic generation */
    private final Set<Boolean>              setBolTblStatDefs = new TreeSet<>();
    
    /** Container for static data table enforce maximum size limit enable/disable flags */
    private final Set<Boolean>              setBolTblStatMaxEnbls = new TreeSet<>();
    
    /** Container for static data table maximum sizes */
    private final Set<Long>                 setLngTblStatMaxSzs = new TreeSet<>();

    
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
     * Constructs a new <code>QueryAssemblyTestSuiteCreator</code> instance ready for configuration.
     * </p>
     *
     */
    public QueryAssemblyTestSuiteCreator() {
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
        this.setEnmTestRqsts.addAll(setRqsts);
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
        this.setEnmTestRqsts.add(enmTestRqst);
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
     * @param setStrSupplPvs  collection of PV names to add to each <code>TestArchiveRequest</code> within configuration
     */
    public void addSupplementalPvs(Collection<String>   setPvNames) {
        this.setStrSupplPvs.addAll(setPvNames);
        
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
    public void addSupplementalPv(String strPvName) {
        this.setStrSupplPvs.add(strPvName);
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
     * @param durRange  time-series data request range override value for test suite configuration
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
     * @param durDelay  time-series data request delay override value for test suite configuration
     */
    public void setRequestDelay(Duration durDelay) {
        this.durDelay = durDelay;
    }
    
    
    /**
     * <p>
     * Adds a new collection of advanced error checking/verification enable/disable flags to the test suite configuration.
     * </p>
     * <p>
     * The <code>QueryResponseAssembler</code> class supports advanced error checking of an assembled
     * <code>SampledAggregate</code> instance.  If advanced error checking is enabled the 
     * <code>QueryResponseAssembler</code> will subject any constructed <code>SampledAggregate</code>
     * instance to a battery of error and verification tests. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The error and verification checking methods are actually supplied by <code>SampledAggregate</code>.</li> 
     * <li>Any duplicate values in the argument are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param setEnables    collection of new advanced error checking flags for sampled aggregate assembly
     */
    public void addAggregateErrorCheckingEnables(Collection<Boolean> setEnables) {
        this.setBolAggrErrEnbls.addAll(setEnables);
    }
    
    /**
     * <p>
     * Adds a new advanced error checking/verification enable/disable flag to the test suite configuration.
     * </p>
     * <p>
     * The <code>QueryResponseAssembler</code> class supports advanced error checking of an assembled
     * <code>SampledAggregate</code> instance.  If advanced error checking is enabled the 
     * <code>QueryResponseAssembler</code> will subject any constructed <code>SampledAggregate</code>
     * instance to a battery of error and verification tests. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The error and verification checking methods are actually supplied by <code>SampledAggregate</code>.</li> 
     * </ul>
     * </p>
     * 
     * @param bolEnable new advanced error checking flag for sampled aggregate assembly
     */
    public void addAggregateErrorCheckingEnable(boolean bolEnable) {
        this.setBolAggrErrEnbls.add(bolEnable);
    }
    
    /**
     * <p>
     * Adds a new collection of time-domain collision enable/disable flags to the test suite configuration..
     * </p>
     * <p>
     * Time-domain collisions occur when raw correlated data blocks have intersecting timestamp collections
     * where timestamps are not aligned.  This is generally the result of sampling at different clock rates
     * during the same time interval.
     * </p>
     * <p>
     * Processing of time-domain collisions is supported by the <code>QueryResponseAssembler</code>, but can
     * be computationally and resource expensive.  Thus, there is the option to disable it where any request
     * containing time-domain collisions is reject with an exception.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Any duplicate values in the argument are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param setEnables    a collection of new time-domain collision enable/disable flags for sampled aggregate assembly
     */
    public void addAggregateTimeDomainCollisionsEnables(Collection<Boolean> setEnables) {
        this.setBolAggrTmDmCollEnbls.addAll(setEnables);
    }
    
    /**
     * <p>
     * Adds a new collection of time-domain collision enable/disable flags to the test suite configuration.
     * </p>
     * <p>
     * Time-domain collisions occur when raw correlated data blocks have intersecting timestamp collections
     * where timestamps are not aligned.  This is generally the result of sampling at different clock rates
     * during the same time interval.
     * </p>
     * <p>
     * Processing of time-domain collisions is supported by the <code>QueryResponseAssembler</code>, but can
     * be computationally and resource expensive.  Thus, there is the option to disable it where any request
     * containing time-domain collisions is reject with an exception.
     * </p>
     * 
     * @param bolEnable a new time-domain collision enable/disable flag for sampled aggregate assembly
     */
    public void addAggregateTimeDomainCollisionsEnable(boolean bolEnable) {
        this.setBolAggrTmDmCollEnbls.add(bolEnable);
    }
    
    /**
     * <p>
     * Adds a new collection of assemble concurrently enable/disable flags to the test suite configuration.
     * </p>
     * <p>
     * The assembly of <code>SampledAggregate</code> instances from time-series request data can be done using
     * multi-threaded concurrency.  Specifically, the composite <code>SampledBlock</code> instances may employ
     * concurrent processing. Concurrent assembly can increase performance on multi-core platforms, however,
     * this action may compute with other active processes.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Any duplicate values in the argument are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param setEnables    a new collection of sampled aggregate assembly using concurrency disable/enable flags 
     */
    public void addAggregateConcurrentEnables(Collection<Boolean> setEnables) {
        this.setBolAggrConcEnbls.addAll(setEnables);
    }
    
    /**
     * <p>
     * Adds a new assemble concurrently enable/disable flag to the test suite configuration.
     * </p>
     * <p>
     * The assembly of <code>SampledAggregate</code> instances from time-series request data can be done using
     * multi-threaded concurrency.  Specifically, the composite <code>SampledBlock</code> instances may employ
     * concurrent processing. Concurrent assembly can increase performance on multi-core platforms, however,
     * this action may compute with other active processes.
     * </p>
     * 
     * @param bolEnable     a new sampled aggregate assembly using concurrency disable/enable flag
     */
    public void addAggregateConcurrentEnable(boolean bolEnable) {
        this.setBolAggrConcEnbls.add(bolEnable);
    }
    
    /**
     * <p>
     * Adds a new collection of assemble concurrently pivot sizes to the test suite configuration.
     * </p>
     * <p>
     * When concurrent assembly is enable the <code>QueryResponseAssembler</code> does not actually switch
     * (or "pivot") to concurrent processing until the target set of <code>SampledBlock</code> instances
     * is greater than a given "pivot size."  Thus, the "pivot size" is a performance parameter which 
     * attempts to minimize the concurrency overhead for smaller requests, or in the initial stages of
     * assembly when processing demands are lighter.
     * </p>  
     * 
     * @param setPivotSizes a new collection of concurrency pivot sizes for sampled aggregate assembly
     */
    public void addAggregateConcurentPivotSizes(Collection<Integer> setPivotSizes) {
        this.setIntAggrConcPvtSzs.addAll(setPivotSizes);
    }
    
    /**
     * <p>
     * Adds a new assemble concurrently pivot size to the test suite configuration.
     * </p>
     * <p>
     * When concurrent assembly is enable the <code>QueryResponseAssembler</code> does not actually switch
     * (or "pivot") to concurrent processing until the target set of <code>SampledBlock</code> instances
     * is greater than a given "pivot size."  Thus, the "pivot size" is a performance parameter which 
     * attempts to minimize the concurrency overhead for smaller requests, or in the initial stages of
     * assembly when processing demands are lighter.
     * </p>  
     * 
     * @param intPivotSize  a new concurrency pivot size for sampled aggregate assembly
     */
    public void addAggregateConcurrentPivotSize(int intPivotSize) {
        this.setIntAggrConcPvtSzs.add(intPivotSize);
    }
    
    /**
     * <p>
     * Adds a new collection of maximum allowable thread counts for concurrent assembly to the test suite configuration.
     * </p>
     * <p>
     * When current assembly is enabled the <code>QueryResponseAssembler</code> will limit the number of processing
     * threads to the given value(s).  This is a performance parameter meant to prevent the limit the processing
     * resources during assembly.
     * </p>
     * 
     * @param setMaxThreads a new collection of maximum thread counts for concurrent sampled aggregate assembly
     */
    public void addAggregateConcurrentMaxThreadCounts(Collection<Integer> setMaxThreads) {
        this.setIntAggrConcMaxThrds.addAll(setMaxThreads);
    }
    
    /**
     * <p>
     * Adds a maximum allowable thread count for concurrent assembly to the test suite configuration.
     * </p>
     * <p>
     * When current assembly is enabled the <code>QueryResponseAssembler</code> will limit the number of processing
     * threads to the given value(s).  This is a performance parameter meant to prevent the limit the processing
     * resources during assembly.
     * </p>
     * 
     * @param setMaxThread a new maximum thread count for concurrent sampled aggregate assembly
     */
    public void addAggregateConcurrentMaxThreadCount(int cntMaxThreads) {
        this.setIntAggrConcMaxThrds.add(cntMaxThreads);
    }
    

    /**
     * <p>
     * Adds a new collection of JAL data table build enable/disable flags to the test suite configuration.
     * </p>
     * <p>
     * The following test conditions hold for the given table creation flags:
     * <ul>
     * <li><code>false</code> - the test evaluation stops after <code>SampledAggregate</code> assembly.</li>
     * <li><code>true</code> - the test evaluation attempts to build a data table according to the test conditions.</li>
     * </ul>
     * <p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Any duplicate values in the argument (or test suite collection) are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param setEnables    a new collection of data table creation enable/disable flags  
     */
    public void addTableCreateEnables(Collection<Boolean> setEnables) {
        this.setBolTblBldEnbls.addAll(setEnables);
    }
    
    /**
     * <p>
     * Adds a new collection of JAL data table build enable/disable flags to the test suite configuration.
     * </p>
     * <p>
     * The following test conditions hold for the given table creation flags:
     * <ul>
     * <li><code>false</code> - the test evaluation stops after <code>SampledAggregate</code> assembly.</li>
     * <li><code>true</code> - the test evaluation attempts to build a data table according to the test conditions.</li>
     * </ul>
     * <p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Any duplicate values in the argument (or test suite collection) are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param bolEnable     a new data table creation enable/disable flag
     */
    public void addTableCreateEnable(boolean bolEnable) {
        this.setBolTblBldEnbls.add(bolEnable);
    }
    
    /**
     * <p>
     * Adds a new collection of JAL data table types to the test suite configuration.
     * </p>
     * <p>
     * Specifies that a test case is created for each table type given, and for each data request made.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * Static data tables can create large Java heap space demand and should be used with caution (i.e., only
     * for smaller data requests).
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The enumeration <code>{@link JalDataTableType#UNSUPPORTED}</code> should not be used.
     * <li>Any duplicate values in the argument are ignored.</li>
     * </ul>
     * </p>
     *  
     * @param setTblTypes   collection of new JAL data table types to be added to the test suite configuration
     */
    public void addTableTypes(Collection<JalDataTableType> setTblTypes) {
        this.setEnmTblTypes.addAll(setTblTypes);
    }
    
    /**
     * <p>
     * Adds a new JAL data table type to the test suite configuration.
     * </p>
     * <p>
     * Specifies that a test case is created for the table type given, and for each data request made.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * Static data tables can create large Java heap space demand and should be used with caution (i.e., only
     * for smaller data requests).
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The enumeration <code>{@link JalDataTableType#UNSUPPORTED}</code> should not be used.
     * </ul>
     * </p>
     *  
     * @param enmTblType new JAL data table type to be added to the test suite configuration
     */
    public void addTableType(JalDataTableType enmTblType) {
        this.setEnmTblTypes.add(enmTblType);
    }
    
    /**
     * <p>
     * Adds a new collection of static data table default enable/disable flags to the test suite configuration.
     * <p>
     * <p>
     * This parameter is only loosely defined at the current time.  However, most implementations function as
     * follows:
     * <pre>
     *      If this value is <code>true</code> when automatic data table type creation is enabled 
     *      (i.e., with <code>{@link JalDataTableType#AUTO}</code>), a static data table is always 
     *      attempted first.
     * </pre>
     * Thus, this value is usually associated with automatic data table type selection and creation.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Any duplicate values in the argument (and the test suite collection) are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param setDefaults   a new collection of static data table default flags in automatic data table type selection
     */
    public void addTableStaticDefaults(Collection<Boolean> setDefaults) {
        this.setBolTblStatDefs.addAll(setDefaults);
    }
    
    /**
     * <p>
     * Adds a static data table default enable/disable flag to the test suite configuration.
     * <p>
     * <p>
     * This parameter is only loosely defined at the current time.  However, most implementations function as
     * follows:
     * <pre>
     *      If this value is <code>true</code> when automatic data table type creation is enabled 
     *      (i.e., with <code>{@link JalDataTableType#AUTO}</code>), a static data table is always 
     *      attempted first.
     * </pre>
     * Thus, this value is usually associated with automatic data table type selection and creation.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Any duplicate values in the test suite collection are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param bolDefault    a new static data table default flag in automatic data table type selection
     */
    public void addTableStaticDefault(boolean bolDefault) {
        this.setBolTblStatDefs.add(bolDefault);
    }
    
    /**
     * <p>
     * Adds a new collection of static data table maximum size enable/disable flags to the test suite configuration.
     * </p>
     * <p>
     * When the static table maximum size is enabled (i.e., the argument is <code>true</code>) then the following
     * conditions are generally enforced:
     * <ul>
     * <li>table type = <code>{@link JalDataTableType#STATIC}</code>: If the table size is greater than a given 
     *     maximum value an exception is thrown (i.e., the query fails).
     * </li>
     * <li>table type = <code>{@link JalDataTableType#AUTO}</code>: If the table size is greater than a given 
     *     maximum value then the implementation pivots to a dynamics data table.
     * </li>
     * </ul>
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * If the static table maximum size is disabled (i.e., the argument is <code>false</code> then the static table
     * is not size limited.  In this case the resultant table may consume extreme Java heap resources causing a
     * runtime error.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Any duplicate values in the argument (and the test suite collection) are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param setEnables    a new collection of static data table maximum size enable/disable flags
     */
    public void addTableStaticMaxSizeEnables(Collection<Boolean> setEnables) { 
        this.setBolTblStatMaxEnbls.addAll(setEnables);
    }
    
    /**
     * <p>
     * Adds a new static data table maximum size enable/disable flag to the test suite configuration.
     * </p>
     * <p>
     * When the static table maximum size is enabled (i.e., the argument is <code>true</code>) then the following
     * conditions are generally enforced:
     * <ul>
     * <li>table type = <code>{@link JalDataTableType#STATIC}</code>: If the table size is greater than a given 
     *     maximum value an exception is thrown (i.e., the query fails).
     * </li>
     * <li>table type = <code>{@link JalDataTableType#AUTO}</code>: If the table size is greater than a given 
     *     maximum value then the implementation pivots to a dynamics data table.
     * </li>
     * </ul>
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * If the static table maximum size is disabled (i.e., the argument is <code>false</code> then the static table
     * is not size limited.  In this case the resultant table may consume extreme Java heap resources causing a
     * runtime error.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Any duplicate values in the test suite collection are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param bolEnable     a new static data table maximum size enable/disable flag
     */
    public void addTableStaticMaxSizeEnable(boolean bolEnable) {
        this.setBolTblStatMaxEnbls.add(bolEnable);
    }
    
    /**
     * <p>
     * Adds a new collection of static data table maximum size limitations to the test suite configuration.
     * </p>
     * <p>
     * When the static data table maximum size option is enabled this value is used to determine that maximum size.
     * See methods <code>{@link #addTableStaticMaxSizeEnables(Collection)}</code> for a description of static maximum
     * size enforcement.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Maximum static table sizes should be chosen based upon platform memory and Java heap space allocation.</li>
     * <li>Any duplicate values in the arguments (and the test suite collection) are ignored.</li>
     * </ul>
     * </p>
     *  
     * @param setMaxSizes   a new collection of static data table maximum size limits
     */
    public void addTableStaticMaxSizes(Collection<Long> setMaxSizes) {
        this.setLngTblStatMaxSzs.addAll(setMaxSizes);
    }
    
    /**
     * <p>
     * Adds a new static data table maximum size limitation to the test suite configuration.
     * </p>
     * <p>
     * When the static data table maximum size option is enabled this value is used to determine that maximum size.
     * See methods <code>{@link #addTableStaticMaxSizeEnables(Collection)}</code> for a description of static maximum
     * size enforcement.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Maximum static table sizes should be chosen based upon platform memory and Java heap space allocation.</li>
     * <li>Any duplicate values in the arguments (and the test suite collection) are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param lngMaxSize    a new static data table maximum size limit
     */
    public void addTableStaticMaxSize(long lngMaxSize) {
        this.setLngTblStatMaxSzs.add(lngMaxSize);
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates a new test suite of <code>QueryAssemblyTestCase</code> instances according to the current configuration.
     * </p>
     * <p>
     * Note that the return value is map keyed by <code>DpDataRequest</code> instances.  The value of the
     * map is a container of all <code>QueryRecoveryTestCase</code> instances that use that time-series data request.
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
     * <li><code>{@link #SZ_CORR_PIVOT_SZ_DEF}</code> - default concurrency pivot size.</li>
     * </ul>
     * </p>
     * 
     * @return  an enumerated collection of test cases for the test suite parameters
     * 
     * @throws ConfigurationException    there are no test requests in the current test suite
     */
    public Collection<QueryAssemblyTestCase> createTestSuite() throws ConfigurationException {
        
        // Check configuration state and set any default values
        this.checkConfiguration();    // throws configuration exception
        
        // Check special case - no TestArchiveRequest's 
        //  Yields a single request with range given by 'duration' and 'delay' fields 
        if (this.setEnmTestRqsts.isEmpty()) {
            TestArchiveRequest  enmRqst = TestArchiveRequest.EMPTY_REQUEST;
            DpDataRequest       rqst = this.createRequest(this.setStrSupplPvs);
            
            Collection<QueryAssemblyTestCase> setCases = this.createTestCasesFor(enmRqst, rqst);
            
            return setCases;
        }
        
        // Create returned container and populate it by enumerating through all test suite parameters 
        Collection<QueryAssemblyTestCase>   setCases = new LinkedList<>();
        
        for (TestArchiveRequest enmRqst : this.setEnmTestRqsts) {
            DpDataRequest   rqst = this.createRequest(enmRqst);

            Collection<QueryAssemblyTestCase> lstSubCases = this.createTestCasesFor(enmRqst, rqst);
            
            setCases.addAll(lstSubCases);
        }
            
        
        return setCases;
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
        String strPadd = strPad + "  ";
//        String strPadBul = strPad + " - ";
        String strPaddBul = strPadd + " - ";
        
        ps.println(strPad + "Test Archive Requests");
        ps.println(strPadd + "TestArchiveRequest enumerations");
        this.setEnmTestRqsts.forEach(enm -> ps.println(strPaddBul + enm.name()));
        ps.println(strPadd + "Supplementing PV names");
        this.setStrSupplPvs.forEach(str -> ps.println(strPaddBul + str));
        ps.println(strPadd + "Request duration override : " + this.durRange);
        ps.println(strPadd + "Request delay override    : " + this.durDelay);
        
        ps.println(strPad + "Sampled Aggregate Assembly");
        ps.println(strPadd + "Advance error checking enabled");
        this.setBolAggrErrEnbls.forEach(bol -> ps.println(strPaddBul + bol));
        ps.println(strPadd + "Time-domain collision enable(s)");
        this.setBolAggrTmDmCollEnbls.forEach(bol -> ps.println(strPaddBul + bol));
        ps.println(strPadd + "Concurrent assembly enable(s)");
        this.setBolAggrConcEnbls.forEach(bol -> ps.println(strPaddBul + bol));
        ps.println(strPadd + "Concurrency pivot size(s) (Sampled Blocks)");
        this.setIntAggrConcPvtSzs.forEach(i -> ps.println(strPaddBul + i));
        ps.println(strPadd + "Concurrency maximum thread count(s)");
        this.setIntAggrConcMaxThrds.forEach(i -> ps.println(strPaddBul + i));
        
        ps.println(strPad + "JAL Data Table Creation");
        ps.println(strPadd + "Build JAL data table enable(s)");
        this.setBolTblBldEnbls.forEach(bol -> ps.println(strPaddBul + bol));
        ps.println(strPadd + "JAL data table type(s)");
        this.setEnmTblTypes.forEach(enm -> ps.println(strPaddBul + enm));
        ps.println(strPadd + "Static data table default(s)");
        this.setBolTblStatDefs.forEach(bol -> ps.println(strPaddBul + bol));
        ps.println(strPadd + "Static data table maximum size enable(s)");
        this.setBolTblStatMaxEnbls.forEach(bol -> ps.println(strPaddBul + bol));
        ps.println(strPadd + "Static data table maximum size(s)");
        this.setLngTblStatMaxSzs.forEach(sz -> ps.println(strPaddBul + sz));
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
     * <li>if <code>{@link #setEnmTestRqsts}.isEmpty() && <code>{@link #setStrSupplPvs}.isEmpty()</code> throws exception. </li>
     * <li>if <code>{@link #setEnmTestRqsts}.isEmpty() && {@link #durRange} == null)</code> throws exception. </li>
     * </ul>
     * </p>
     *  
     * @throws ConfigurationException   invalid test suite configuration, cannot make viable time-seres request
     */
    private void    checkConfiguration() throws ConfigurationException {
        
        //
        // Check for invalid test suite configuration
        //
        
        if (this.setEnmTestRqsts.isEmpty() && this.setStrSupplPvs.isEmpty()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Invalid test suite configuration, both Test Archive Request and Supplemental PV names collections are empty.";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new ConfigurationException(strMsg);
        }

        if (this.setEnmTestRqsts.isEmpty() && this.durRange==null) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Invalid test suite configuration, Test Archive Rquest collection empty and no request duration specified.";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new ConfigurationException(strMsg);
        }
        
        //
        // Add default values to empty configuration collections
        //
        
        // Assembly parameters
        if (this.setBolAggrErrEnbls.isEmpty())
            this.setBolAggrErrEnbls.add(BOL_ASSM_ERRCHK_ENBL_DEF);
        
        if (this.setBolAggrTmDmCollEnbls.isEmpty())
            this.setBolAggrTmDmCollEnbls.add(BOL_ASSM_TMDOM_COLL_ENBL_DEF);
        
        if (this.setBolAggrConcEnbls.isEmpty())
            this.setBolAggrConcEnbls.add(BOL_ASSM_CONC_ENBL_DEF);
        
        if (this.setIntAggrConcPvtSzs.isEmpty())
            this.setIntAggrConcPvtSzs.add(INT_ASSM_CONC_PVT_SZ_DEF);
        
        if (this.setIntAggrConcMaxThrds.isEmpty())
            this.setIntAggrConcMaxThrds.add(INT_ASSM_CONC_MAX_THRDS_DEF);
        
        // Data table parameters
        if (this.setBolTblBldEnbls.isEmpty())
            this.setBolTblBldEnbls.add(QueryAssemblyEvaluator.BOL_TBL_BLD_ENBL_DEF);
        
        if (this.setEnmTblTypes.isEmpty())
            this.setEnmTblTypes.add(ENM_TBL_TYPE_DEF);
        
        if (this.setBolTblStatDefs.isEmpty())
            this.setBolTblStatDefs.add(BOL_TBL_STAT_DEF);
        
        if (this.setBolTblStatMaxEnbls.isEmpty())
            this.setBolTblStatMaxEnbls.add(BOL_TBL_STAT_MAX_ENBL_DEF);
        
        if (this.setLngTblStatMaxSzs.isEmpty())
            this.setLngTblStatMaxSzs.add(LNG_TBL_STAT_MAX_SZ_DEF);
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
     * If any supplemental PV names exist in the container <code>{@link #setStrSupplPvs}</code> they are added to the request.
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
        rqst.selectSources(this.setStrSupplPvs);

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
     * the collection <code>{@link #setEnmTestRqsts}</code> is empty.
     * </p>
     * <p>
     * The returned request is built from the collection of PV names <code>{@link #setStrSupplPvs}</code> and the durations
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

    /**
     * <p>
     * Creates the subset collection of all test suite test cases for the request specified in the argument.
     * </p>
     * <p>
     * Creates test cases for the remaining test suite configuration parameters; that is, those, not involving
     * the test suite parameters <code>{@link #setEnmTestRqsts}, {@link #setStrSupplPvs}, 
     * {@link #durDelay}</code>, and <code>{@link #durDelay}</code>.
     * Iterates through all the following test suite parameter containers for the given request (in order):
     * <ol>
     * <li><code>{@link #setBolAggrErrEnbls}</code></li>
     * <li><code>{@link #setBolAggrTmDmCollEnbls}</code></li>
     * <li><code>{@link #setIntAggrConcPvtSzs}</code></li>
     * <li><code>{@link #setIntAggrConcMaxThrds}</code></li>
     * <li><code>{@link #setBolTblBldEnbls}</code></li>
     * <li><code>{@link #setEnmTblTypes}</code></li>
     * <li><code>{@link #setBolTblStatDefs}</code></li>
     * <li><code>{@link #setBolTblStatMaxEnbls}</code></li>
     * <li><code>{@link #setLngTblStatMaxSzs}</code></li>
     * </ol>
     * Note that the following fields of all returned records are given as follows:
     * <ul>
     * <li><code>{@link QueryAssemblyTestCase#enmRqstOrg()}</code> = argument <code>enmRqst</code>.</li>
     * <li><code>{@link QueryAssemblyTestCase#rqstFinal()}</code> = argument <code>rqst</code>.</li>
     * <li><code>{@link QueryAssemblyTestCase#setSupplPvs()}</code> = <code>{@link #setStrSupplPvs}</code>.</li>
     * <li><code>{@link QueryAssemblyTestCase#durRqstRange()}</code> = <code>{@link #durRange}</code>.</li>
     * <li><code>{@link QueryAssemblyTestCase#durRqstDelay()}</code> = <code>{@link #durDelay}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param enmRqst   the <code>TestArchiveRequest</code> for all returned test cases
     * @param rqst      the <code>DpDataRequest</code> for all returned test cases
     * 
     * @return  the collection of all test suite test cases for the given time-series data request. 
     */
    private Collection<QueryAssemblyTestCase>   createTestCasesFor(TestArchiveRequest enmRqst, DpDataRequest rqst) {
        
        Collection<QueryAssemblyTestCase>   setCases = new LinkedList<>();
        
        for (boolean bolAggrErrChk : this.setBolAggrErrEnbls)
            for (boolean bolAggrTmDomColl : this.setBolAggrTmDmCollEnbls)
                for (boolean bolAggrConc : this.setBolAggrConcEnbls)
                    for (int szAggConcPivot : this.setIntAggrConcPvtSzs)
                        for (int cntAggConcMaxThrds : this.setIntAggrConcMaxThrds)
                            for (boolean bolTblBld : this.setBolTblBldEnbls)
                                for (JalDataTableType enmTblType : this.setEnmTblTypes)
                                    for (boolean bolTblStatDef : this.setBolTblStatDefs)
                                        for (boolean bolTblStatMaxSz : this.setBolTblStatMaxEnbls)
                                            for (long szTblStatMaxSz : this.setLngTblStatMaxSzs) {
                                                QueryAssemblyTestCase   recCase = QueryAssemblyTestCase.from(
                                                        enmRqst, this.setStrSupplPvs, this.durRange, this.durDelay, rqst, 
                                                        bolAggrErrChk, bolAggrTmDomColl, bolAggrConc, szAggConcPivot, cntAggConcMaxThrds, 
                                                        bolTblBld, enmTblType, bolTblStatDef, bolTblStatMaxSz, szTblStatMaxSz
                                                        );
                                                
                                                setCases.add(recCase);
                                            }
        
        return setCases;
    }
}
