/*
 * Project: dp-api-common
 * File:	QueryRecoveryTestSuiteCreator.java
 * Package: com.ospreydcs.dp.jal.tools.query.recovery
 * Type: 	QueryRecoveryTestSuiteCreator
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
package com.ospreydcs.dp.jal.tools.query.recovery;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.common.TimeAbstraction;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.model.request.DataRequestDecomposer;
import com.ospreydcs.dp.api.query.model.request.RequestDecompType;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;
import com.ospreydcs.dp.jal.tools.query.testrequests.TestArchiveRequest;

/**
 * <p>
 * Configurable class that creates collections of <code>QueryRecoveryTestCase</code> instances.
 * </p>
 * <p>
 * Generates collections of <code>{@link QueryRecoveryTestCase}</code> instances, or "test suites",
 * according to dynamic configuration.  Specifically, a new <code>QueryRecoveryTestSuiteCreator</code> instance
 * is first configured using methods
 * <ul>
 * <li><code>{@link #addTestRequest(TestArchiveRequest)}</code></li>
 * <li><code>{@link #addCorrelMaxThreadCount(int)}</code></li>
 * <li><code>{@link #addCorrelConcurrencyPivotSize(int)}</code></li>
 * </ul>
 * which all can be called repeatedly to create a configuration.
 * Once configured, method <code>{@link #createTestSuite()}</code> is used create collections of
 * <code>{@link QueryRecoveryTestCase}</code> mapped to the <code>{@link TestArchiveRequest}</code>
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
public class QueryRecoveryTestSuiteCreator {
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new, empty <code>QueryRecoveryTestSuiteCreator</code> ready for configuration.
     * </p>
     * 
     * @return  a new unconfigured instance of <code>QueryRecoveryTestSuiteCreator</code>
     */
    public static QueryRecoveryTestSuiteCreator   create() {
        return new QueryRecoveryTestSuiteCreator();
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
    
    
    /** The default value for request decomposition enabled */
    public static final boolean             BOL_DCMP_ENABLED_DEF = CFG_DEF.data.request.decompose.enabled;
    
    /** The default value for automatic request decomposition (using max PVs and max time range) */
    public static final boolean             BOL_DCMP_AUTO_ENABLED_DEF = false;
    
    /** The default value for request decomposition strategy (if auto is disabled) */
    public static final RequestDecompType   ENM_DCMP_TYPE_DEF = CFG_DEF.data.request.decompose.preferred;
    
    /** The default value for maximum PV count in composite requests (for automatic decomposition) */
    public static final int                 INT_DCMP_MAX_PVS_DEF = CFG_DEF.data.request.decompose.maxSources;

    /** The default value for maximum time range in a composite request (for automatic decomposition) */
    public static final TimeAbstraction     TA_DCMP_MAX_RNG_DEF = TimeAbstraction.from(
            CFG_DEF.data.request.decompose.maxDuration, 
            CFG_DEF.data.request.decompose.durationUnit);

    /** The default value for maximum time range for composite requests as a Java <code>Duration</code> */
    public static final Duration            DUR_DCMP_MAX_RNG_DEF = TA_DCMP_MAX_RNG_DEF.getDuration();
    
    
    /** The default value used for gRPC stream type in raw data recovery */
    public static final DpGrpcStreamType    ENM_STRM_TYPE_DEF = CFG_DEF.data.recovery.stream.preferred;

    
    /** The default value for gRPC multi-streaming enabled flag in raw data recovery */
    public static final boolean     BOL_MSTRM_ENABLED_DEF = CFG_DEF.data.recovery.multistream.enabled;
    
    /** The default value used for request domain size triggering multi-streaming for raw data recovery */
    public static final long        LNG_MSTRM_DOM_SZ_DEF = CFG_DEF.data.recovery.multistream.sizeDomain;
    
    /** The default value for maximum gRPC stream count in raw data recovery */
    public static final int         INT_MSTRM_MAX_CNT_DEF = CFG_DEF.data.recovery.multistream.maxStreams;
    
    
    /** The default value enabling/disabling raw data correlation while simultaneously recovering data */
    public static final boolean     BOL_CORR_RCVRY_ENABLE_DEF = CFG_DEF.data.recovery.correlate.whileStreaming;
    
    /** The default value enabling/disabling concurrency (multi-threading) in raw data correlation */
    public static final boolean     BOL_CORR_CONC_ENABLE_DEF = CFG_DEF.data.recovery.correlate.concurrency.enabled;
    
    /** Default value for the minimum target set of raw correlated data block triggering concurrency in correlation */
    public static final int         SZ_CORR_PIVOT_SZ_DEF = CFG_DEF.data.recovery.correlate.concurrency.pivotSize;
    
    /** Default value for raw data correlation maximum thread count when concurrency is enabled */
    public static final int         CNT_CORR_MAX_THRDS_DEF = CFG_DEF.data.recovery.correlate.concurrency.maxThreads;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLoggerSetLevel(STR_LOGGING_LEVEL);
    
    
    
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
    
    
    /** Collection of time-series data request decomposition enable/disable flags */
    private final Set<Boolean>              setBolDcmp = new TreeSet<>();
    
    /** Collection of automatic request decomposition enable/disable flags */
    private final Set<Boolean>              setBolDmpAuto = new TreeSet<>();
    
    /** Collection of time-series data request decomposition types (for explicit, non-auto decomposition) */
    private final Set<RequestDecompType>    setEnmDcmpTypes = new TreeSet<>();
    
    /** Collection of request decomposition maximum PV count per composite request for automatic request decomposition */
    private final Set<Integer>              setIntDcmpMaxPvs = new TreeSet<>();
    
    /** Collection of request decomposition maximum time range duration per composite request for automatic request decomposition */
    private final Set<Duration>             setDurDcmpMaxRngs = new TreeSet<>();
    
    
    /** Collection of gRPC stream types used for raw data recovery */
    private final Set<DpGrpcStreamType>     setEnmStrmTypes = new TreeSet<>();

    
    /** Collection of gRPC multi-streaming enable/disable flags */
    private final Set<Boolean>              setBolMStrm = new TreeSet<>();
    
    /** Collection of request domain sizes triggering gRPC multi-streaming for raw data recovery */
    private final Set<Long>                 setLngMStrmDomSzs = new TreeSet<>();
    
    /** Collection of maximum gRPC stream counts for raw data recovery */
    private final Set<Integer>              setIntMStrmMaxCnts = new TreeSet<>();

    
    /** Collection of raw data correlation correlate during data recovery enable/disable flags */
    private final Set<Boolean>              setBolCorrRcvry = new TreeSet<>();
    
    /** Collection of raw data correlation use concurrency (multi-threading) flags */
    private final Set<Boolean>              setBolCorrConc = new TreeSet<>();
    
    /** Collection of raw data correlation correlated block size triggering concurrency */
    private final Set<Integer>              setIntCorrPivotSzs = new TreeSet<>();
    
    /** Collection of raw data correlation maximum thread counts when concurrency is enabled */
    private final Set<Integer>              setIntCorrMaxThrds = new TreeSet<>();
    

    //
    //  Constructors
    // 
    
    /**
     * <p>
     * Constructs a new, unconfigured <code>QueryRecoveryTestSuiteCreator</code> instance.
     * </p>
     *
     */
    public QueryRecoveryTestSuiteCreator() {
    }

    
    //
    // Configuration
    //

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
     * Add a request decomposition enable flag to the test suite configuration.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>Time-series data request decomposition must be enabled for multi-streaming.</li>
     * </ul>
     * </p>
     *  
     * @param bolEnable time-series data request decomposition enable flag 
     */
    public void addRequestDecompEnable(boolean bolEnable) {
        this.setBolDcmp.add(bolEnable);
    }
    
    /**
     * <p>
     * Adds a collection of request decomposition enable flags to the test suite configuration.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>Time-series data request decomposition must be enabled for multi-streaming.</li>
     * <li>Repeated values in the argument collection are ignored.</li>
     * </ul>
     * </p>
     *  
     * @param setEnables a collection of time-series request decomposition enable flags
     */
    public void addRequestDecompEnables(Collection<Boolean> setEnables) {
        this.setBolDcmp.addAll(setEnables);
    }
    
    /**
     * <p>
     * Add a new automatic request decomposition enable/disable flag to the test suite configuration.
     * </p>
     * <p>
     * When automatic request decomposition is enabled, the parameters <code>{@link #addRequestDecompMaxPvCnt(int)}</code>
     * and <code>{@link #addRequestDecompMaxTimeRange(Duration)}</code> are used to find the "preferred" decomposition
     * conforming to these values.  Specifically, 
     * the <code>{@link DataRequestDecomposer#buildCompositeRequestPreferred(DpDataRequest)}</code> method is used to 
     * decompose the request after the decomposer has been configured with the above parameters.
     * </p>
     * <p>
     * Automatic decomposition is in contrast to an explicit request decomposition where the decomposition 
     * strategy is specified (i.e., <code>{@link #addRequestDecompStrategy(RequestDecompType)}</code> and the request
     * is decomposed into <code>{@link #addMultiStreamMaxStreamCount(int)}</code> composite requests (at most). 
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>Repeated values in the argument collection are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param bolEnable automatic request decomposition enable/disable flag 
     */
    public void addRequestDecompAutoEnable(boolean bolEnable) {
        this.setBolDmpAuto.add(bolEnable);
    }
    
    /**
     * <p>
     * Add a collection of new automatic request decomposition enable/disable flags to the test suite configuration.
     * </p>
     * <p>
     * When automatic request decomposition is enabled, the parameters <code>{@link #addRequestDecompMaxPvCnt(int)}</code>
     * and <code>{@link #addRequestDecompMaxTimeRange(Duration)}</code> are used to find the "preferred" decomposition
     * conforming to these values.  Specifically, 
     * the <code>{@link DataRequestDecomposer#buildCompositeRequestPreferred(DpDataRequest)}</code> method is used to 
     * decompose the request after the decomposer has been configured with the above parameters.
     * </p>
     * <p>
     * Automatic decomposition is in contrast to an explicit request decomposition where the decomposition 
     * strategy is specified (i.e., <code>{@link #addRequestDecompStrategy(RequestDecompType)}</code> and the request
     * is decomposed into <code>{@link #addMultiStreamMaxStreamCount(int)}</code> composite requests (at most). 
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>Time-series data request decomposition must be enabled for multi-streaming.</li>
     * <li>Repeated values in the argument collection are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param setEnsbles    collection of new automatic request decomposition enable/disable flag
     */
    public void addRequestDecompAutoEnables(Collection<Boolean> setEnsbles) {
        this.setBolDmpAuto.addAll(setEnsbles);
    }
    
    /**
     * <p>
     * Adds a time-series data request decomposition strategy to the test suite configuration.
     * </p>
     * <p>
     * Request decomposition strategies are used in explicit decomposition (i.e., non-automatic).
     * The request is decomposed according to the given strategy so that the number of composite
     * requests is not greater than parameter <code>{@link #addMultiStreamMaxStreamCount(int)}</code>.
     * </p>
     * 
     * @param enmDcmpType   a new request decomposition strategy
     * 
     * @see RequestDecompType
     */
    public void addRequestDecompStrategy(RequestDecompType enmDcmpType) {
        this.setEnmDcmpTypes.add(enmDcmpType);
    }
    
    /**
     * <p>
     * Adds a collection of time-series data request decomposition strategies to the test suite configuration.
     * </p>
     * <p>
     * Request decomposition strategies are used in explicit decomposition (i.e., non-automatic).
     * The request is decomposed according to the given strategy so that the number of composite
     * requests is not greater than parameter <code>{@link #addMultiStreamMaxStreamCount(int)}</code>.
     * </p>
     *  
     * @param setDcmpTypes  a collection of new request decomposition strategies
     * 
     * @see RequestDecompType
     */
    public void addRequestDecompStrategies(Collection<RequestDecompType> setDcmpTypes) {
        this.setEnmDcmpTypes.addAll(setDcmpTypes);
    }
    
    /**
     * <p>
     * Adds a new composite request maximum PV count value to the test suite configuration.
     * </p>
     * <p>
     * This parameter is used in <b>automatic</b> request decomposition.
     * </p>
     * <p>
     * In automatic time-series data request decomposition the original request is decomposed into multiple composite 
     * requests. The maximum PV count per composite request applies to the <code>{@link RequestDecompType#HORIZONTAL}</code>
     * and <code>{@link RequestDecompType#GRID}</code> decomposition strategies.
     * </p>
     *  
     * @param cntDcmpMaxPvs new maximum PV count per composite request
     */
    public void addRequestDecompMaxPvCnt(int cntDcmpMaxPvs) {
        this.setIntDcmpMaxPvs.add(cntDcmpMaxPvs);
    }
    
    /**
     * <p>
     * Adds a collection of new composite request maximum PV counts to the test suite configuration.
     * </p>
     * <p>
     * This parameter is used in <b>automatic</b> request decomposition.
     * </p>
     * <p>
     * In time-series data request decomposition the original request is decomposed into multiple composite requests.
     * The maximum PV count per composite request applies to the <code>{@link RequestDecompType#HORIZONTAL}</code>
     * and <code>{@link RequestDecompType#GRID}</code> decomposition strategies.
     * </p>
     * 
     * @param setDcmpMaxPvs collection of new maximum PV counts per composite request
     */
    public void addRequestDecompMaxPvCnts(Collection<Integer> setDcmpMaxPvs) {
        this.setIntDcmpMaxPvs.addAll(setDcmpMaxPvs);
    }
    
    /**
     * <p>
     * Adds a new composite request maximum time duration to the test suite configuration.
     * </p>
     * <p>
     * This parameter is used in <b>automatic</b> request decomposition.
     * </p>
     * <p>
     * In time-series data request decomposition the original request is decomposed into multiple composite requests.
     * The maximum time range duration per composite request applies to the 
     * <code>{@link RequestDecompType#VERTICAL}</code> and <code>{@link RequestDecompType#GRID}</code> decomposition 
     * strategies.
     * </p>
     * 
     * @param durMaxTmRng   new maximum time range duration per composite request
     */
    public void addRequestDecompMaxTimeRange(Duration durMaxTmRng) {
        this.setDurDcmpMaxRngs.add(durMaxTmRng);
    }
    
    /**
     * <p>
     * Adds a collection of new composite request maximum time durations to the test suite configuration.
     * </p>
     * <p>
     * This parameter is used in <b>automatic</b> request decomposition.
     * </p>
     * <p>
     * In time-series data request decomposition the original request is decomposed into multiple composite requests.
     * The maximum time range duration per composite request applies to the 
     * <code>{@link RequestDecompType#VERTICAL}</code> and <code>{@link RequestDecompType#GRID}</code> decomposition 
     * strategies.
     * </p>
     * 
     * @param setMaxTmRngs  collection of new maximum time range durations per composite request
     */
    public void addRequestDecompMaxTimeRanges(Collection<Duration> setMaxTmRngs) {
        this.setDurDcmpMaxRngs.addAll(setMaxTmRngs);
    }
    
    
    /**
     * <p>
     * Adds a new <code>DpGrpcStreamType</code> constant to the test suite configuration.
     * </p>
     * <p>
     * The <code>QueryRequestRecoverer</code> class uses gRPC data streams to recover raw time-series data 
     * from a time-series data request.  There are 2 viable gRPC stream types for data recovery,
     * <ol>
     * <li><code>{@link DpGrpcStreamType#BACKWARD}</code> - Unidirectional data flow from the Query Service to the client,</li>
     * <li><code>{@link DpGrpcStreamType#BIDIRECTIONAL}</code> - Bidirectional where the client acknowledges data receipt.</li>
     * </ol>
     * The 3rd gRPC stream type, <code>{@link DpGrpcStreamType#FORWARD}</code> is illegal and will throw
     * an exception.
     * </p>
     *  
     * @param enmStrmType   a new gRPC stream type used for raw time-series data recovery
     * 
     * @see DpGrpcStreamType
     */
    public void addGrpcStreamType(DpGrpcStreamType enmStrmType) {
        this.setEnmStrmTypes.add(enmStrmType);
    }
    
    /**
     * <p>
     * Adds a collection of new <code>DpGrpcStreamType</code> constants to the test suite configuration.
     * </p>
     * <p>
     * The <code>QueryRequestRecoverer</code> class uses gRPC data streams to recover raw time-series data 
     * from a time-series data request.  There are 2 viable gRPC stream types for data recovery,
     * <ol>
     * <li><code>{@link DpGrpcStreamType#BACKWARD}</code> - Unidirectional data flow from the Query Service to the client,</li>
     * <li><code>{@link DpGrpcStreamType#BIDIRECTIONAL}</code> - Bidirectional where the client acknowledges data receipt.</li>
     * </ol>
     * The 3rd gRPC stream type, <code>{@link DpGrpcStreamType#FORWARD}</code> is illegal and will throw
     * an exception.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Repeated enumeration constant values will be ignored.
     * </p>
     *  
     * @param setStrmTypes  a collection of new gRPC stream types for raw time-series data recovery
     * 
     * @see DpGrpcStreamType
     */
    public void addGrpcStreamTypes(Collection<DpGrpcStreamType> setStrmTypes) {
        this.setEnmStrmTypes.addAll(setStrmTypes);
    }
    
    
    /**
     * <p>
     * Adds a new gRPC multi-streaming enable/disable flag to the test suite configuration.
     * </p>
     * <p>
     * The <code>QueryRequestRecoverer</code> request processor supports concurrent, multiple gRPC
     * data streams for request raw data recovery.  This feature is supported in conjunction with
     * time-series data request decomposition.  
     * </p>
     * <p>
     * The gRPC multi-streaming feature can be toggled on/off using this parameter.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>Time-series data request decomposition must be enabled for multi-streaming.</li>
     * </ul>
     * </p>
     * 
     * @param bolMultiStrm  new gRPC multi-streaming enable/disable flag
     */
    public void addMultiStreamEnable(boolean bolMultiStrm) {
        this.setBolMStrm.add(bolMultiStrm);
    }
    
    /**
     * <p>
     * Adds a new gRPC multi-streaming enable/disable flag to the test suite configuration.
     * </p>
     * <p>
     * The <code>QueryRequestRecoverer</code> request processor supports concurrent, multiple gRPC
     * data streams for request raw data recovery.  This feature is supported in conjunction with
     * time-series data request decomposition.  
     * </p>
     * <p>
     * The gRPC multi-streaming feature can be toggled on/off using this parameter.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>Time-series data request decomposition must be enabled for multi-streaming.</li>
     * <li>Repeated values in the argument collection are ignored.</li>
     * </ul>
     * </p>
     * 
     * @param setMultiStrms collection of new gRPC multi-streaming enable/disable flags
     */
    public void addMultiStreamEnables(Collection<Boolean> setMultiStrms) {
        this.setBolMStrm.addAll(setMultiStrms);
    }
    
    /**
     * <p>
     * Adds a new gRPC multi-streaming time-series data request domain size to the test suite configuration.
     * </p>
     * <p>
     * An approximation for the query domain size of any <code>{@link DpDataRequest}</code> time-series data request
     * is available from method <code>{@link DpDataRequest#approxDomainSize()}</code>.  The domain size(s) provided
     * here are used as a boundary limit for initiating gRPC multi-streaming.  Specifically, if a request has
     * a domain size greater than the given value(s) then multi-streaming is triggered, otherwise a single gRPC
     * data stream is used for data recovery.
     * </p>
     *   
     * @param lngRqstDomSize    a new request domain size triggering gRPC multi-streaming for raw data recovery
     * 
     * @see DpDataRequest#approxDomainSize()
     */
    public void addMultiStreamRequestDomainSize(long lngRqstDomSize) {
        this.setLngMStrmDomSzs.add(lngRqstDomSize);
    }
    
    /**
     * <p>
     * Adds a collection of new gRPC multi-streaming time-series data request domain size to the test suite configuration.
     * </p>
     * <p>
     * An approximation for the query domain size of any <code>{@link DpDataRequest}</code> time-series data request
     * is available from method <code>{@link DpDataRequest#approxDomainSize()}</code>.  The domain size(s) provided
     * here are used as a boundary limit for initiating gRPC multi-streaming.  Specifically, if a request has
     * a domain size greater than the given value(s) then multi-streaming is triggered, otherwise a single gRPC
     * data stream is used for data recovery.
     * </p>
     *   
     * @param setRqstDomSizes   collection of new request domain sizes triggering multi-streaming for raw data recovery
     * 
     * @see DpDataRequest#approxDomainSize()
     */
    public void addMultiStreamRequestDomainSizes(Collection<Long> setRqstDomSizes) {
        this.setLngMStrmDomSzs.addAll(setRqstDomSizes);
    }
    
    /**
     * <p>
     * Adds a new maximum gRPC stream count to the test suite configuration.
     * </p>
     * <p>
     * The given value(s) determine the maximum number of allowable gRPC data streams used to recover
     * the raw data for a time-series data request.  The number of gRPC data streams will never exceed this
     * limit regardless of the request size.  The number of gRPC data stream may, however, be less than this
     * value of the request is small (e.g., the request domain size is less than the multi-stream triggering
     * limit).
     * </p>
     * 
     * @param cntMaxStrms   new maximum gRPC data stream count used to recover raw, time-series data
     */
    public void addMultiStreamMaxStreamCount(int cntMaxStrms) {
        this.setIntMStrmMaxCnts.add(cntMaxStrms);
    }
    
    /**
     * <p>
     * Adds a collection of new maximum gRPC stream counts to the test suite configuration.
     * </p>
     * <p>
     * The given value(s) determine the maximum number of allowable gRPC data streams used to recover
     * the raw data for a time-series data request.  The number of gRPC data streams will never exceed this
     * limit regardless of the request size.  The number of gRPC data stream may, however, be less than this
     * value of the request is small (e.g., the request domain size is less than the multi-stream triggering
     * limit).
     * </p>
     * 
     * @param setMaxStrmCnts    collection of new maximum gRPC data stream counts used to recover raw, time-series data
     */
    public void addMultiStreamMaxStreamCounts(Collection<Integer> setMaxStrmCnts) {
        this.setIntMStrmMaxCnts.addAll(setMaxStrmCnts);
    }
    
    
    /**
     * <p>
     * Adds a new raw data correlation during raw data recovery enable/disable flag to the test suite configuration.
     * </p>
     * <p>
     * The process of raw data correlation can begin before all raw data is recovered.  This may or may not increase performance
     * depending upon resource competition.  
     * <ul>
     * <li>Setting this flag <code>false</code> disables concurrent recovery and processing, all raw data is recovered first.</li>
     * <li>Setting this flag <code>true</code> enables concurrent recovery and processing, raw data is processed as soon as it is available.</li>
     * </ul>
     * </p>
     * 
     * @param bolEnable a new concurrent recovery and correlation enable/disable flag
     */
    public void addCorrelDuringRecoveryEnable(boolean bolEnable) {
        this.setBolCorrRcvry.add(bolEnable);
    }
    
    /**
     * <p>
     * Adds a new collection of raw data correlation during raw data recovery enable/disable flags to the test suite configuration.
     * </p>
     * <p>
     * The process of raw data correlation can begin before all raw data is recovered.  This may or may not increase performance
     * depending upon resource competition.  
     * <ul>
     * <li>Setting this flag <code>false</code> disables concurrent recovery and processing, all raw data is recovered first.</li>
     * <li>Setting this flag <code>true</code> enables concurrent recovery and processing, raw data is processed as soon as it is available.</li>
     * </ul>
     * </p>
     * 
     * @param setEnables    collection of new concurrent recovery and correlation enable/disable flags
     */
    public void addCorrelDuringRecoveryEnables(Collection<Boolean> setEnables) {
        this.setBolCorrRcvry.addAll(setEnables);
    }
    
    /**
     * <p>
     * Adds a new concurrent raw data correlation enable/disable flag to the test suite configuration.
     * </p>
     * <p>
     * The task of raw data correlation can be done using multiple processing threads, that is, using concurrent processing.
     * The enable/disable flag(s) turn concurrent correlation processing on/off.
     * </p>
     *  
     * @param bolEnable a new multi-threaded correlation enable/disable flag
     */
    public void addCorrelConcurrentEnable(boolean bolEnable) {
        this.setBolCorrConc.add(bolEnable);
    }
    
    /**
     * <p>
     * Adds a collection of new concurrent raw data correlation enable/disable flags to the test suite configuration.
     * </p>
     * <p>
     * The task of raw data correlation can be done using multiple processing threads, that is, using concurrent processing.
     * The enable/disable flag(s) turn concurrent correlation processing on/off.
     * </p>
     *  
     * @param setEnables    collection of new multi-threaded correlation enable/disable flags
     */
    public void addCorrelConcurrentEnables(Collection<Boolean> setEnables) {
        this.setBolCorrConc.addAll(setEnables);
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
    public void addCorrelConcurrencyPivotSize(int szPivot) {
        this.setIntCorrPivotSzs.add(szPivot);
    }
    
    /**
     * <p>
     * Adds a new collection concurrency pivot size limits to the test suite configuration.
     * </p>
     * <p>
     * Note that "concurrency pivot sizes" are available to avoid wasting computation on multi-threading overhead 
     * resources for trivial processing tasks. The typical situation is if a target set has size less than the 
     * pivot size all processing of the target set is done serially; otherwise it is done on multiple threads. 
     * </p>
     * 
     * @param setPivotSizes collection of concurrency pivot sizes for test suite generation
     */
    public void addCorrelConcurrencyPivotSizes(Collection<Integer> setPivotSizes) {
        this.setIntCorrPivotSzs.addAll(setPivotSizes);
    }
    
    /**
     * <p>
     * Adds a new maximum allowable thread count value for concurrent raw data correlation to 
     * the test suite configuration.
     * </p>
     * 
     * @param cntMaxThrds new maximum allowable thread count value for test suite generation
     */
    public void addCorrelMaxThreadCount(int cntMaxThrds) {
        this.setIntCorrMaxThrds.add(cntMaxThrds);
    }
    
    /**
     * <p>
     * Adds a collection of maximum allowable thread count values for concurrent raw data correlation to 
     * the test suite configuration.
     * </p>
     * 
     * @param setIntCorrMaxThrds    collection of maximum allowable thread count value for test suite generation
     */
    public void addCorrelMaxThreadCounts(Collection<Integer> setMaxThrdCnts) {
        this.setIntCorrMaxThrds.addAll(setMaxThrdCnts);
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates a new test suite of <code>QueryRecoveryTestCase</code> instances according to the current configuration.
     * </p>
     * <p>
     * The return value is a collection of <code>QueryRecoveryTestCase</code> records.  Each record in the 
     * <code>QueryRecoveryTestCase</code> collection refers to a specific time-series data request.
     * </p> 
     * <p>
     * The <code>TestArchiveRequest</code> instances within the records will all be augmented by the collection
     * of supplemental PV names assigned by <code>{@link #addSupplementalPv(String)}</code> or 
     * <code>{@link #addSupplementalPvs(Collection)}</code> (if any).  This is done in the method
     * <code>{@link QueryRecoveryTestCase#evaluate(com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer)}</code>.
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
    public Collection<QueryRecoveryTestCase> createTestSuite() throws ConfigurationException {
        
        // Check state
        this.defaultConfiguration();    // throws ConfigurationException
        
        // Create returned container and populate it by enumerating through all test suite parameters 
        Collection<QueryRecoveryTestCase>   setCases = new LinkedList<>();
        
        for (TestArchiveRequest enmRqst : this.setTestRqsts) 
            for (boolean bolDcmpEnbl : this.setBolDcmp)
                for (boolean bolDcmpAuto : this.setBolDmpAuto)
                    for (RequestDecompType enmDcmpType : this.setEnmDcmpTypes)
                        for (int cntDcmpMaxPvs : this.setIntDcmpMaxPvs)
                            for (Duration durDcmpMaxRng : this.setDurDcmpMaxRngs)
                                for (DpGrpcStreamType enmStrmType : this.setEnmStrmTypes)
                                    for (long lngMStrmDomSz : this.setLngMStrmDomSzs)
                                        for (int cntMStrmMaxCnt : this.setIntMStrmMaxCnts)
                                            for (boolean bolCorrRcvry : this.setBolCorrRcvry)
                                                for (boolean bolCorrConc : this.setBolCorrConc)
                                                    for (int intCorrPivotSz : this.setIntCorrPivotSzs)
                                                        for (int intCorrMaxThrds : this.setIntCorrMaxThrds) {
                                                            QueryRecoveryTestCase   recCase = QueryRecoveryTestCase.from(
                                                                    enmRqst, this.setSupplPvs, 
                                                                    bolDcmpEnbl, bolDcmpAuto, enmDcmpType, cntDcmpMaxPvs, durDcmpMaxRng, 
                                                                    enmStrmType, 
                                                                    lngMStrmDomSz, cntMStrmMaxCnt, 
                                                                    bolCorrRcvry, bolCorrConc, intCorrPivotSz, intCorrMaxThrds
                                                                    );
                                                            
                                                            setCases.add(recCase);
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
     * @param strPad    optional left-hand side white space padding (or <code>null</code>)
     */
    public void printOut(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        
        // Common Resources: Supplemental PV name request ID augmentation, bullet padding
        String  strPadBul = strPad + "- ";
        String  strPadd = strPad + "  ";
        String  strPaddBul = strPad + "  - ";
        
        String  strIdAug = "";
        if (!this.setSupplPvs.isEmpty())
            strIdAug = "+" + this.setSupplPvs;
        
        // Test request configuration 
        ps.println(strPad + "Request IDs:");
        for (TestArchiveRequest enmRqst : this.setTestRqsts)
            ps.println(strPadBul + enmRqst.name() + strIdAug);
        
        ps.println(strPad + "Supplemental PV Names:");
        for (String strPvNm : this.setSupplPvs)
            ps.println(strPadBul + strPvNm);
        
        
        // Request decomposition configuration
        ps.println(strPad + "Request Decomposition");
        
        ps.println(strPadd + "Decomposition Enabled:");
        for (Boolean bolEnbl : this.setBolDcmp)
            ps.println(strPaddBul + bolEnbl);
        
        ps.println(strPadd + "Decomposition Automatic:");
        for (Boolean bolAuto : this.setBolDmpAuto)
            ps.println(strPaddBul + bolAuto);
        
        ps.println(strPadd + "Decomposition Strategy(ies):");
        for (RequestDecompType enmType : this.setEnmDcmpTypes)
            ps.println(strPaddBul + enmType);
        
        ps.println(strPadd + "Maximum Composite Request PV Count(s):");
        for (Integer cntMaxPvs : this.setIntDcmpMaxPvs)
            ps.println(strPaddBul + cntMaxPvs);
        
        ps.println(strPadd + "Maximum Composite Time Duration(s):");
        for (Duration durMaxRng : this.setDurDcmpMaxRngs)
            ps.println(strPaddBul + durMaxRng);
        
        
        // Raw data recovery
        ps.println(strPad + "Raw Data Recovery");
        
        ps.println(strPadd + "gRPC Stream Types:");
        for (DpGrpcStreamType enmType : this.setEnmStrmTypes)
            ps.println(strPaddBul + enmType);
        
        ps.println(strPadd + "Multi-Streaming Enabled:");
        for (Boolean bolEnable : this.setBolMStrm)
            ps.println(strPaddBul + bolEnable);
        
        ps.println(strPadd + "Multi-Stream Request Domain Size(s):");
        for (Long lngDomSz : this.setLngMStrmDomSzs)
            ps.println(strPaddBul + lngDomSz);
        
        ps.println(strPadd + "Multi-Stream Maximum Stream Count(s):");
        for (Integer cntMaxStrms : this.setIntMStrmMaxCnts)
            ps.println(strPaddBul + cntMaxStrms);
        
        
        // Raw data correlation configuration
        ps.println(strPad + "Raw Data Correlation");
        
        ps.println(strPadd + "Correlate During Data Recovery:");
        for (Boolean bolRcvry : this.setBolCorrRcvry)
            ps.println(strPaddBul + bolRcvry);
        
        ps.println(strPadd + "Multi-Threaded Correlation Enabled:");
        for (Boolean bolConc : this.setBolCorrConc)
            ps.println(strPaddBul + bolConc);
        
        ps.println(strPad + "Correlation Concurrency Pivot Sizes:");
        for (Integer szPivot : this.setIntCorrPivotSzs)
            ps.println(strPaddBul + szPivot);
        
        ps.println(strPad + "Maximum Thread Counts:");
        for (Integer cntMaxThrds : this.setIntCorrMaxThrds)
            ps.println(strPaddBul + cntMaxThrds);
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Checks the configuration collections and assigns a default configuration if not initialized.
     * </p>
     * <p>
     * Checks all the parameter collections for elements.  
     * <ul>
     * <li>If <code>{@link #setTestRqsts}</code> is empty an exception is thrown.</li>
     * <li>The container <code>{@link #setSupplPvs}</code> is ignored.</li>
     * <li>If any other container is empty a single, default value is supplied.</li>
     * <li>All default values are taken from the JAL configuration file as class constants.</li>
     * </ul>
     * </p>
     *  
     * @throws ConfigurationException    there are no test requests within the test suite
     */
    private void    defaultConfiguration() throws ConfigurationException {
        
        // Test request - check for illegal configuration
        if (this.setTestRqsts.isEmpty()) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Invalid test suite configuration, the test request collection is empty.";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new ConfigurationException(strMsg);
        }
        
        // Request decomposition parameters
        if (this.setBolDcmp.isEmpty())
            this.setBolDcmp.add(QueryRecoveryTestSuiteCreator.BOL_DCMP_ENABLED_DEF);
        
        if (this.setBolDmpAuto.isEmpty())
            this.setBolDmpAuto.add(QueryRecoveryTestSuiteCreator.BOL_DCMP_AUTO_ENABLED_DEF);
        
        if (this.setEnmDcmpTypes.isEmpty())
            this.setEnmDcmpTypes.add(QueryRecoveryTestSuiteCreator.ENM_DCMP_TYPE_DEF);
        
        if (this.setIntDcmpMaxPvs.isEmpty())
            this.setIntDcmpMaxPvs.add(QueryRecoveryTestSuiteCreator.INT_DCMP_MAX_PVS_DEF);
        
        if (this.setDurDcmpMaxRngs.isEmpty())
            this.setDurDcmpMaxRngs.add(QueryRecoveryTestSuiteCreator.DUR_DCMP_MAX_RNG_DEF);

        // Recovery parameters
        if (this.setEnmStrmTypes.isEmpty())
            this.setEnmStrmTypes.add(QueryRecoveryTestSuiteCreator.ENM_STRM_TYPE_DEF);
        
        // gRPC multi-streaming parameters
        if (this.setBolMStrm.isEmpty())
            this.setBolMStrm.add(QueryRecoveryTestSuiteCreator.BOL_MSTRM_ENABLED_DEF);
        
        if (this.setLngMStrmDomSzs.isEmpty())
            this.setLngMStrmDomSzs.add(QueryRecoveryTestSuiteCreator.LNG_MSTRM_DOM_SZ_DEF);
        
        if (this.setIntMStrmMaxCnts.isEmpty())
            this.setIntMStrmMaxCnts.add(QueryRecoveryTestSuiteCreator.INT_MSTRM_MAX_CNT_DEF);
        
        // Raw data correlation parameters
        if (this.setBolCorrRcvry.isEmpty())
            this.setBolCorrRcvry.add(QueryRecoveryTestSuiteCreator.BOL_CORR_RCVRY_ENABLE_DEF);
        
        if (this.setBolCorrConc.isEmpty())
            this.setBolCorrConc.add(QueryRecoveryTestSuiteCreator.BOL_CORR_CONC_ENABLE_DEF);
        
        if (this.setIntCorrPivotSzs.isEmpty())
            this.setIntCorrPivotSzs.add(SZ_CORR_PIVOT_SZ_DEF);
        
        if (this.setIntCorrMaxThrds.isEmpty())
            this.setIntCorrMaxThrds.add(CNT_CORR_MAX_THRDS_DEF);
    }

}
