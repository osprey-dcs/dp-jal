/*
 * Project: dp-api-common
 * File:	TestArchiveRequestCreator.java
 * Package: com.ospreydcs.dp.api.tools.query.request
 * Type: 	TestArchiveRequestCreator
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
 * @since May 4, 2025
 *
 */
package com.ospreydcs.dp.api.tools.query.request;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.ospreydcs.dp.api.config.common.DpLoggingConfig;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.tools.config.DpApiToolsConfig;
import com.ospreydcs.dp.api.tools.config.archive.DpTestArchiveConfig;
import com.ospreydcs.dp.api.util.JavaRuntime;

/**
 * <p>
 * Utility class for generating time-series data requests appropriate for the Data Platform test archive.
 * </p>
 * <p>
 * The <code>TestArchiveRequestCreator</code> class is a utility containing all static method for creating
 * <code>DpDataRequest</code> instances specific to the Data Platform test archive.  That is, all 
 * <code>DpDataRequest</code> instances generated here will query for time-series data within the test archive.  
 * The Data Platform test archive is populated with sample time-series data using the utility 
 * <code>app-run-test-data-generator</code> which ships within the Data Platform installation.
 * </p>
 * <p>
 * <h2>Request Types</h2>
 * There are 3 possible request types for generation:
 * <ol>
 * <li>General requests - the prototype method is <code>{@link #createRequest(int, int, Duration, Duration)}</code></li>
 * <li>Clocked requests - the prototype method is <code>{@link #createClockedRequest(int, int, Duration, Duration)}</code></li>
 * <li>Timestamp List requests - the prototype method is <code>{@link #createTmsListRequest(int, int, Duration, Duration)}</code></li>
 * </ol>
 * The above "prototype method" is the primary method for creating the request type.  All other methods creating a 
 * specific request type are simply conveniences where some or all the argument values are substituted with default 
 * values.  See method documentation for the details on argument value substitution. 
 * </p>
 * <p>
 * <h3>General Requests</h3>
 * General requests will query for time-series data from data source using any type of sampling method.  The specific
 * method is determined by the test archive format, which is, in turn, determined by the arguments of the above method
 * (i.e., the number of data sources and any index offset).  Note that the maximum number of data sources within
 * the Data Platform test archive is <code>{@link #CNT_PVS_TOTAL}</code> and the maximum request duration is given
 * by <code>{@link #DUR_RNG_TOTAL}</code>.
 * </p>
 * <p>
 * <h3>Clocked Requests</h3>
 * Clocked requests will query for time-series data from data sources sampled with uniform sampling clocks.  The
 * recovered data will identify the associated timestamps using the Protocol Buffers message <code>SampleClock</code>
 * from the Query Service gRPC communications framework.  The clocked data sources are identified within the
 * Data Platform test archive as those source names beginning with index <code>{@link #IND_FIRST_PV_CLOCKED}</code> and
 * ending with index <code>{@link #IND_LAST_PV_CLOCKED}</code>.   
 * </p>
 * <p>
 * <h3>Timestamp List Requests</h3>
 * Timestamp list requests will query for time-series data from data sources sampled using explicit timestamp lists.
 * The recovered data will identify the associated timestamps using the Protocol Buffers message 
 * <code>TimestampList</code> from the Query Service gRPC communications framework.  The timestamp list data sources
 * are identified within the Data Platform test archive as those sources with names beginning with index
 * <code>{@link #IND_FIRST_PV_TMS_LIST}</code> and ending with index <code>{@link #IND_LAST_PV_TMS_LIST}</code>. 
 * </p>  
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * Supplying request creation method arguments that would result in a query extending beyond the test archive domain,
 * or the request type domain, will result in an exception.
 * </li>
 * <li>
 * Note that within the Data Platform test archive data source names (i.e., Process Variable names) have
 * a 1-based index which is appended to the name prefix <code>{@link #STR_PV_PREFIX}</code>.  The Java lists
 * of data source names have a 0-based indexing scheme.  Thus, caution must be used when referencing
 * the class constants <code>{@link #IND_FIRST_PV_CLOCKED}</code>, <code>{@link #IND_LAST_PV_CLOCKED}</code>,
 * <code>{@link #IND_FIRST_PV_TMS_LIST}</code>, and <code>{@link #IND_LAST_PV_TMS_LIST}</code>.
 * </li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 4, 2025
 *
 */
public class TestArchiveRequestCreator {

    
    //
    // Application Resources
    //
    
    /** Default DP API library testing parameters */
    public static final DpTestArchiveConfig     CFG_ARCHIVE = DpApiToolsConfig.getInstance().query.testArchive;

    /** General Query Service tools logging configuration */
    public static final DpLoggingConfig         CFG_LOGGING = DpApiToolsConfig.getInstance().query.logging;
    
    
    //
    // Class Constants
    //

    /** The ISO 8601 format inception time instant of the test archive */
    private static final String STR_ISO_ARCHIVE_INCEPT = CFG_ARCHIVE.firstTimestamp; // "2023-10-31T15:51:02.000+00:00";
    
    /** The ISO 8601 format last timestamp instant within the test data archive */
    private static final String STR_ISO_ARCHIVE_LAST = CFG_ARCHIVE.lastTimestamp; // "2023-10-31T15:51:21.999+00:00"; 

    
    /** 
     * The inception time instant of the test Data Platform data archive test data set.
     * <p>
     * This value is parsed from class constant < ode>{@link #STR_ISO_ARCHIVE_INCEPT}</code>.
     */
    public static final Instant     INS_INCEPT = Instant.parse(STR_ISO_ARCHIVE_INCEPT);
    
    /** 
     * The final time instant of all Data Platform data archive test data set
     * <p>
     * This value is parsed from class constant <code>{@link #STR_ISO_ARCHIVE_LAST}</code>.
     */
    public static final Instant     INS_FINAL = Instant.parse(STR_ISO_ARCHIVE_LAST);
    
    /** 
     * Size of time domain (i.e., range) with Data Platform data archive test data set.
     * <p>
     * The value is computed from class constants <code>{@link #INS_INCEPT} and {@link #INS_FINAL}</code>.
     */ 
    public static final Duration    DUR_RNG_TOTAL = Duration.between(INS_INCEPT, INS_FINAL);

    /** 
     * Size of time domain (i.e., range) with Data Platform data archive test data set in nanoseconds.
     * <p>
     * The value is computed from class constant <code>{@link #DUR_RNG_TOTAL}</code>.
     */ 
    public static final Long        LNG_RANGE = DUR_RNG_TOTAL.toNanos();
 
   
    /**
     * The prefix for each data source name within the Data Platform test archive.
     * <p>
     * The value is taken from default configuration parameter <code>{@link DpTestArchiveConfig#pvPrefix}</code>.
     */
    public static final String      STR_PV_PREFIX = CFG_ARCHIVE.pvPrefix; // "dpTest_";

    
    /** 
     * The total number of unique data source within the Data Platform data archive test data set.
     * <p>
     * The value is taken from default configuration parameter <code>{@link DpTestArchiveConfig#pvCountTotal}</code>.
     */
    public static final int         CNT_PVS_TOTAL = CFG_ARCHIVE.pvCountTotal; // 4000;
    
    /**
     * The number of unique data sources that are sampled with a uniform sampling clock
     * <p>
     * The value is taken from the default configuration parameter <code>{@link DpTestArchiveConfig#pvCountClock}</code>.
     */
    public static final int         CNT_PVS_CLOCKED = CFG_ARCHIVE.pvCountClock;
    
    /**
     * The number of unique data sources that are sampled using an explicit timestamp list
     * <p>
     * The value is taken from the default configuration parameter <code>{@link DpTestArchiveConfig#pvCountTmsList}</code>.
     */
    public static final int         CNT_PVS_TMS_LIST = CFG_ARCHIVE.pvCountTmsList;
    
    
    /**
     * The starting index (i.e., first index) of the data sources using a uniform sampling clock
     * <p>
     * The value is taken from the default configuration parameter <code>{@link DpTestArchiveConfig#pvIndexStartClock}</code>
     */
    public static final int         IND_FIRST_PV_CLOCKED = CFG_ARCHIVE.pvIndexStartClock;
    
    /**
     * The index of the last data source within the Data Platform test archive using a uniform sampling clock
     * <p>
     * The value is given by <code>{@link #IND_FIRST_PV_CLOCKED} + {@link #CNT_PVS_CLOCKED}</code>
     */
    public static final int         IND_LAST_PV_CLOCKED = IND_FIRST_PV_CLOCKED + CNT_PVS_CLOCKED;
    
    /**
     * The starting index (i.e., first index) of the data sources using an explicit timestamp list
     * <p>
     * The value is taken from the default configuration parameter <code>{@link DpTestArchiveConfig#pvIndexStartTmsList}</code>
     */
    public static final int         IND_FIRST_PV_TMS_LIST = CFG_ARCHIVE.pvIndexStartTmsList;
    
    /**
     * The index of the last data source within the Data Platform test archive using an explicit timestamp list
     */
    public static final int         IND_LAST_PV_TMS_LIST = IND_FIRST_PV_TMS_LIST + CNT_PVS_TMS_LIST;

    
    //
    // Class Resources
    //
    
    /** 
     * Immutable list of all data source names within the Data Platform test archive 
     * <p>
     * The the data source name is is prefixed with class constant <code>{@link #STR_PV_PREFIX}</code> with
     * an index value suffix.  The list should contain <code>{@link #CNT_PVS_TOTAL}</code> entries ordered
     * by index.
     */
    public static final List<String>    LST_PV_NAMES_TOTAL;
    
    /**
     * Immutable list of data source names using uniform sampling clocks within the Data Platform test archive 
     * <p>
     * The the data source name is is prefixed with class constant <code>{@link #STR_PV_PREFIX}</code> with
     * an index value suffix starting with index <code>{@link #IND_FIRST_PV_CLOCKED}</code></code>.
     * The list should contain <code>{@link #CNT_PVS_CLOCKED}</code> entries ordered by index.
     */
    public static final List<String>    LST_PV_NAMES_CLOCKED;
    
    /**
     * Immutable list of data source names using explicit timestamp lists within the Data Platform test archive 
     * <p>
     * The the data source name is is prefixed with class constant <code>{@link #STR_PV_PREFIX}</code> with
     * an index value suffix starting with index <code>{@link #IND_FIRST_PV_CLOCKED}</code></code>.
     * The list should contain <code>{@link #CNT_PVS_CLOCKED}</code> entries ordered by index.
     */
    public static final List<String>    LST_PV_NAMES_TMS_LIST;
    
    
    /** The logging enabled/disabled flag */ 
    public static final boolean         BOL_LOGGING = CFG_LOGGING.enabled;
    
    /** The logging event level */
    public static final String          STR_LOG_LEVEL = CFG_LOGGING.level;
    

    /** Class logger used for exception events */
    private static final Logger LOGGER = LogManager.getLogger(); 
    
    
    /**
     * <p>
     * Class static execution block.
     * </p>
     * <p>
     * Initializes the constant lists of data source names.
     * </p>
     */
    static {
        
        // Populate the PV name lists
        LST_PV_NAMES_TOTAL = IntStream
                .rangeClosed(1, CNT_PVS_TOTAL)
                .<String>mapToObj( i -> STR_PV_PREFIX + Integer.toString(i))
                .toList();   
        
        LST_PV_NAMES_CLOCKED = IntStream
                .rangeClosed(IND_FIRST_PV_CLOCKED + 1, IND_LAST_PV_CLOCKED)
                .<String>mapToObj( i -> STR_PV_PREFIX + Integer.toString(i))
                .toList();
        
        LST_PV_NAMES_TMS_LIST = IntStream
                .rangeClosed(IND_FIRST_PV_TMS_LIST + 1, IND_LAST_PV_TMS_LIST)
                .<String>mapToObj( i -> STR_PV_PREFIX + Integer.toString(i))
                .toList();
        
        // Set the logging level according to configuration
        Level   lvlLogging = Level.toLevel(STR_LOG_LEVEL, LOGGER.getLevel());
//        Configurator.setLevel(LogManager.getLogger(TestArchiveRequestCreator.class).getName(), lvlLoggin);
        Configurator.setLevel(LOGGER, lvlLogging);
    }
    
    
    //
    // Class Universal Method
    //
    
    /**
     * <p>
     * Creates a new, general, <code>DpDataRequest</code> instance configured by the given arguments.
     * </p>
     * <p>
     * This method provides the most general query possible for each request type available (e.g., 
     * general, clocked, or timestamp list).  It defers to methods 
     * <code>{@link #createRequest(int, int, Duration, Duration)}</code>,
     * <code>{@link #createClockedRequest(int, int, Duration, Duration)}</code>,
     * or <code>{@link #createTmsListRequest(int, int, Duration, Duration)}</code> depending upon
     * the value of argument <code>enmType</code>, <code>{@link TestRequestType#GENERAL}</code>,
     * <code>{@link TestRequestType#CLOCKED}</code>, or <code>{@link TestRequestType#TMS_LIST}</code>,
     * respectively.
     * See the method documentation for further details on the request conditions.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The index offset of the first data source is given so that composite query lists can
     * be created that contain a larger set of sources.
     * </li>
     * <br/>
     * <li>
     * The start time offset is given so that composite queries can be created that contain a larger
     * time range.
     * </li>
     * <br/>
     * <li>
     * Since both query domains can be decomposed this method is appropriate for creating
     * "grid-like" composite queries.
     * </li>
     * <br/>
     * <li>
     * The actual time range of the resultant query (as seen by the Query service is given by
     * <br/><br/> 
     * &nbsp; &nbsp; [<code>{@link INS_INCEPT}, {@link INS_INCEPT} + lngStartTime + lngDuration</code>]
     * </li>
     * </ul>
     * </p>
     * 
     * @param enmType       the request type returned
     * @param cntPvs        number of data sources in the query
     * @param indPvOffset   index offset of the first data source within the list of source names
     * @param durRequest    time duration of query 
     * @param durOffset     start time offset of the query 
     * 
     * @return  new <code>DpDataRequest</code> built from the given parameters
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive (see message)
     */
    public static DpDataRequest createRequest(TestRequestType enmType, int cntPvs, int indPvOffset, Duration durRequest, Duration durOffset) 
            throws IllegalArgumentException {
        
        return switch (enmType) {
        case GENERAL -> createRequest(cntPvs, indPvOffset, durRequest, durOffset);
        case CLOCKED -> TestArchiveRequestCreator.createClockedRequest(cntPvs, indPvOffset, durRequest, durOffset);
        case TMS_LIST -> TestArchiveRequestCreator.createTmsListRequest(cntPvs, indPvOffset, durRequest, durOffset);
        case BOTH -> TestArchiveRequestCreator.createBothPvsRequest(cntPvs, indPvOffset, durRequest, durOffset);
        };
    }
    
    
    //
    // Class Methods - General Data Requests 
    //
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance querying for the entire Data Platform test archive.
     * </p>
     * <p>
     * Defers to method <code>{@link #createRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createRequest({@link #CNT_PVS_TOTAL}, 0, {@link #DUR_RNG_TOTAL}, {@link Duration#ZERO})</code>.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * The returned request returns <em>all</em> data within the Data Platform test archive.  This constitutes
     * <code>{@link #CNT_PVS_TOTAL}</code> data sources over a time range of
     * <code>{@link #DUR_RNG_TOTAL}</code>.  Assuming <code>Double</code> Java 
     * values and 1 kHz sampling the total recovered data <i>N</i> size is approximately
     * <pre>
     *      <i>N</i> = {@link #CNT_PVS_TOTAL} * {@link #DUR_RNG_TOTAL} * 1,000 (samples/second) * <code>{@link Double#BYTES}</code>
     * </pre>
     * which typically amounts to <i>N</i> in the range 2 to 4 GBytes.
     * </p>
     * 
     * @return  new <code>DpDataRequest</code> specifying the entire test archive.
     */
    public static DpDataRequest createRequest() {
        return createRequest(CNT_PVS_TOTAL, 0, DUR_RNG_TOTAL, Duration.ZERO);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for clocked data sources configured by the given argument.
     * </p>
     * <p>
     * Queries for the first <code>cntPvs</code> data sources within the clocked test archive for the
     * entire time range duration of the test archive.
     * <p>
     * Defers to method <code>{@link #createRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createRequest(cntPvs, 0, {@link #DUR_RNG_TOTAL}, {@link Duration#ZERO)}</code>.
     * </p>
     * <p>
     * The number of data sources is given to reduce the size of the query.
     * </p>
     * 
     * @param cntPvs    number of data sources in the query
     * 
     * @return  new <code>DpDataRequest</code> for the first cntPvs in LST_PV_NAMES_TOTAL and time range [INS_INCEPT, INS_FINAL]
     */
    public static DpDataRequest createRequest(int cntPvs) throws IllegalArgumentException {
        return createRequest(cntPvs, 0, DUR_RNG_TOTAL, Duration.ZERO);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
     * </p>
     * <p>
     * Queries for the first <code>cntPvs</code> data sources within the test archive  
     * (the index for the first data source defaults to 0) and the given duration 
     * (start time default to <code>{@link #INS_INCEPT}</code>).
     * </p>
     * <p>
     * Defers to method <code>{@link #createRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createRequest(cntPvs, 0, durRequest, {@link Duration#ZERO)}</code>.
     * </p>
     * <p>
     * Note that the no starting index or start time is given so the request always starts
     * at the origin of the clocked query domain.  Not intended for composite query lists. 
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param durRequest    time duration of query , range = [INS_INCEPT, INS_INCEPT + durRequest]
     * 
     * @return  new <code>DpDataRequest</code> for the all data source in LST_PV_NAMES_TOTAL and the specified time range
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
     */
    public static DpDataRequest createRequest(int cntPvs, Duration durRequest) throws IllegalArgumentException {
        return createRequest(cntPvs, 0, durRequest, Duration.ZERO);
    }
    
//    /**
//     * <p>
//     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
//     * </p>
//     * <p>
//     * Queries for the entire set of data sources within the test archive.  
//     * (The index for the first data source defaults to 0.)
//     * </p>
//     * <p>
//     * Note that the start time is given so that decompose queries can
//     * be created to target a larger time range (i.e., "vertical" decomposition).
//     * </p>
//     * 
//     * @param lngDuration       time duration of query (in seconds), range = [INS_INCEPT, INS_INCEPT + lngDuration]
//     * @param lngStartTime      start time of the query (in seconds) - actually an offset from <code>{@link #INS_INCEPT}</code>
//     * 
//     * @return  new <code>DpDataRequest</code> for the all data source in LST_PV_NAMES_TOTAL and the specified time range
//     * 
//     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
//     */
//    public static DpDataRequest createRequest(long lngDuration, long lngStartTime) throws IllegalArgumentException {
//        return createRequest(CNT_PVS_TOTAL, 0, lngDuration, lngStartTime);
//    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
     * </p>
     * <p>
     * Queries for the first <code>cntPvs</code> data sources within the test archive.
     * (The index for the first data source defaults to 0.)
     * </p>
     * <p>
     * Defers to method <code>{@link #createRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createRequest(cntPvs, 0, durRequest, durOffset}</code>.
     * </p>
     * <p>
     * Note that the start time is given so that composite query lists can
     * be created to for a larger request (i.e., "vertical" decomposition).
     * The number of data sources is given to reduce the size of the overall query.
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param durRequest    time duration of query , range = [INS_INCEPT + durOffset, INS_INCEPT + durOffset + lngDuration]
     * @param durOffset     start time offset of the query - offset from <code>{@link #INS_INCEPT}</code>
     * 
     * @return  new <code>DpDataRequest</code> for the first cntPvs in LST_PV_NAMES_TOTAL and the specified time range
     * 
     * @throws IllegalArgumentException  the arguments creates a query request outside the test archive domain
     */
    public static DpDataRequest createRequest(int cntPvs, Duration durRequest, Duration durOffset) throws IllegalArgumentException {
        return createRequest(cntPvs, 0, durRequest, durOffset);
    }
    
//    /**
//     * <p>
//     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
//     * </p>
//     * <p>
//     * The time range of the returned query defaults to the entire duration of the test archive,
//     * (the start time defaults to 0).
//     * </p>
//     * <p>
//     * Note that the index of the first data source is given so that decompose queries can
//     * be created for a larger target set of sources (i.e., "horizontal" decomposition).
//     * </p>
//     * 
//     * @param cntSources        number of data sources in the query
//     * @param indSourceFirst    index of the first data source within the list of source names
//     * 
//     * @return  new <code>DpDataRequest</code> for the cntSources in LST_PV_NAMES_TOTAL starting at index indSourceFirst, and entire duration of the archive
//     * 
//     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
//     */
//    public static DpDataRequest createRequest(int cntSources, int indSourceFirst) throws IllegalArgumentException {
//
//        return createRequest(cntSources, indSourceFirst, LNG_RANGE, 0L);
//    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
     * </p>
     * <p>
     * Queries for <code>cntPvs</code> data sources within the test archive starting at index <code>indPvOffset</code>
     * with the request duration given by <code>durRequest</code>. The start time of the request is given
     * by the inception instant of the test archive <code>{@link #INS_INCEPT}</code>.
     * </p>
     * <p>
     * Note that the index offset of the first data source is given so that composite query lists can
     * be created for a larger request (i.e., "horizontal" decomposition).
     * The duration is given to reduce the size of the overall query.
     * </p>
     * <p>
     * Defers to method <code>{@link #createRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createRequest(cntPvs, indPvOffset, durRequest, {@link Duration#ZERO}}</code>.
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param indPvOffset   index of the first data source within the list of source names
     * @param durRequest    time duration of query 
     * 
     * @return  new <code>DpDataRequest</code> for cntPvs in LST_PV_NAMES_TOTAL starting at index indPvOffset, and duration durRequest
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
     */
    public static DpDataRequest createRequest(int cntPvs, int indPvOffset, Duration durRequest) throws IllegalArgumentException {

        return createRequest(cntPvs, indPvOffset, durRequest, Duration.ZERO);
    }
    
    /**
     * <p>
     * Creates a new, general, <code>DpDataRequest</code> instance configured by the given arguments.
     * </p>
     * <p>
     * This method provides the most general query possible for this utility.
     * The returned request can contain data sources that have both clocked sampling and/or contain
     * explicit timestamp list sampling.  The sampling condition of the data sources is determined
     * by the given data source count and index offset, as well as the Data Platform test archive
     * structure (see <code>{@link #IND_FIRST_PV_CLOCKED}</code> and <code>{@link #IND_FIRST_PV_TMS_LIST}</code>).
     * Additionally, this method can be used for creation of composite data request lists that form
     * a larger query (which can then be concurrently streamed).
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The index offset of the first data source is given so that composite query lists can
     * be created that contain a larger set of sources.
     * </li>
     * <br/>
     * <li>
     * The start time offset is given so that composite queries can be created that contain a larger
     * time range.
     * </li>
     * <br/>
     * <li>
     * Since both query domains can be decomposed this method is appropriate for creating
     * "grid-like" composite queries.
     * </li>
     * <br/>
     * <li>
     * The actual time range of the resultant query (as seen by the Query service is given by
     * <br/><br/> 
     * &nbsp; &nbsp; [<code>{@link INS_INCEPT}, {@link INS_INCEPT} + lngStartTime + lngDuration</code>]
     * </li>
     * </ul>
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param indPvOffset   index offset of the first data source within the list of source names
     * @param durRequest    time duration of query 
     * @param durOffset     start time of the query - offset from <code>{@link #INS_INCEPT}</code>
     * 
     * @return  new <code>DpDataRequest</code> built from the given parameters
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive (see message)
     */
    public static DpDataRequest createRequest(int cntPvs, int indPvOffset, Duration durRequest, Duration durOffset) 
            throws IllegalArgumentException {
        
        // Parameters
        final int       cntPvsArchive = CNT_PVS_TOTAL;
        final int       indPvFirstArchive = 0;   // 0-based Java index
        final int       indPvLastArchive = indPvFirstArchive + cntPvsArchive;
        
        // Create the source indexes and check them
        int indPvFirst = indPvOffset + indPvFirstArchive;
        int indPvLast = indPvFirst + cntPvs;
        
        if (indPvLast > indPvLastArchive) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + "- Request PV final index =" + indPvLast 
                    + " (with offset index=" + indPvOffset + " and total PV count=" + cntPvs + ")"
                    + " > the maximum index of all data sources=" + cntPvsArchive + ".";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
        
        // Create first and last request instants and check them
        Instant     insStart = INS_INCEPT.plus(durOffset);
        Instant     insStop = insStart.plus(durRequest);
        
        if (insStop.isAfter(INS_FINAL)) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + "Request final instant=" + insStop
                    + " (with offset duration=" + durOffset + " and total request duration=" + durRequest + ")" 
                    + " > total archive time domain size=" + LNG_RANGE + ".";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
        
        // Create request and return it
        DpDataRequest   rqst = DpDataRequest.create();
        
        List<String>    lstNames = LST_PV_NAMES_TOTAL.subList(indPvFirst, indPvLast);
        
        rqst.rangeBetween(insStart, insStop);
        rqst.selectSources(lstNames);
        
        return rqst;
    }

    
    //
    // Class Methods - Clocked Data Requests
    //
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance querying for all clocked data sources and entire time 
     * range within the Data Platform test archive.
     * </p>
     * <p>
     * Defers to method <code>{@link #createClockedRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createClockedRequest({@link #CNT_PVS_CLOCKED}, 0, {@link #DUR_RNG_TOTAL}, {@link Duration#ZERO})</code>.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * The returned request returns <em>all</em> clocked data within the Data Platform test archive.  This constitutes
     * <code>{@link #CNT_PVS_CLOCKED}</code> data sources over a time range of
     * <code>{@link #DUR_RNG_TOTAL}</code>.  Assuming <code>Double</code> Java 
     * values and 1 kHz sampling the total recovered data <i>N</i> size is approximately
     * <pre>
     *      <i>N</i> = {@link #CNT_PVS_CLOCKED} * {@link #DUR_RNG_TOTAL} * 1,000 (samples/second) * <code>{@link Double#BYTES}</code>
     * </pre>
     * which typically amounts to <i>N</i> in the range 2 to 4 GBytes.
     * </p>
     * 
     * @return  new <code>DpDataRequest</code> specifying the entire test archive.
     */
    public static DpDataRequest createClockedRequest() {
        return createClockedRequest(CNT_PVS_CLOCKED, 0, DUR_RNG_TOTAL, Duration.ZERO);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for clocked data sources configured by the given argument.
     * </p>
     * <p>
     * Queries for the first <code>cntPvs</code> data sources using uniform sampling clocks within the test archive 
     * for the entire time range duration of the test archive.
     * <p>
     * Defers to method <code>{@link #createClockedRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createClockedRequest(cntPvs, 0, {@link #DUR_RNG_TOTAL}, {@link Duration#ZERO)}</code>.
     * </p>
     * <p>
     * The number of data sources is given to reduce the size of the query.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The value of <code>cntPvs</code> must be less than <code>{@link #CNT_PVS_CLOCKED}</code>.
     * </p>
     * 
     * @param cntPvs    number of data sources in the query
     * 
     * @return  new <code>DpDataRequest</code> for the first cntPvs in LST_PV_NAMES_CLOCKED and time range [INS_INCEPT, INS_FINAL]
     */
    public static DpDataRequest createClockedRequest(int cntPvs) throws IllegalArgumentException {
        return createClockedRequest(cntPvs, 0, DUR_RNG_TOTAL, Duration.ZERO);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for clocked data sources configured by the given arguments.
     * </p>
     * <p>
     * Queries for the first <code>cntPvs</code> data sources within the test archive using uniform sampling clocks
     * (the index for the first data source defaults to 0) with the given duration 
     * (start time default to <code>{@link #INS_INCEPT}</code>).
     * </p>
     * <p>
     * Defers to method <code>{@link #createClockedRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createClockedRequest(cntPvs, 0, durRequest, {@link Duration#ZERO)}</code>.
     * </p>
     * <p>
     * Note that the no starting index or start time is given so the request always starts
     * at the origin of the query domain.  Not intended for composite query lists. 
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param durRequest    time duration of query , range = [INS_INCEPT, INS_INCEPT + durRequest]
     * 
     * @return  new <code>DpDataRequest</code> for the all data source in LST_PV_NAMES_CLOCKED and the specified time range
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
     */
    public static DpDataRequest createClockedRequest(int cntPvs, Duration durRequest) throws IllegalArgumentException {
        return createClockedRequest(cntPvs, 0, durRequest, Duration.ZERO);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for clocked data sources configured by the given arguments.
     * </p>
     * <p>
     * Queries for the first <code>cntPvs</code> data sources within the test archive using uniform sampling clocks.
     * (The index for the first data source defaults to 0.)
     * </p>
     * <p>
     * Defers to method <code>{@link #createClockedRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createClockedRequest(cntPvs, 0, durRequest, durOffset}</code>.
     * </p>
     * <p>
     * Note that the start time is given so that composite query lists can
     * be created to for a larger request (i.e., "vertical" decomposition).
     * The number of data sources is given to reduce the size of the overall query.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The value of <code>cntPvs</code> must be less than <code>{@link #CNT_PVS_CLOCKED}</code>.
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param durRequest    time duration of query , range = [INS_INCEPT + durOffset, INS_INCEPT + durOffset + lngDuration]
     * @param durOffset     start time offset of the query - offset from <code>{@link #INS_INCEPT}</code>
     * 
     * @return  new <code>DpDataRequest</code> for the first cntPvs in LST_PV_NAMES_CLOCKED and the specified time range
     * 
     * @throws IllegalArgumentException  the arguments creates a query request outside the test archive domain
     */
    public static DpDataRequest createClockedRequest(int cntPvs, Duration durRequest, Duration durOffset) throws IllegalArgumentException {
        return createClockedRequest(cntPvs, 0, durRequest, durOffset);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for clocked data sources configured by the given arguments.
     * </p>
     * <p>
     * Queries for <code>cntPvs</code> data sources within the test archive using uniform sampling clocks 
     * The starting index for the data source is given by <code>indPvOffset</code> with the request duration given 
     * by <code>durRequest</code>.  The start time of the request is given by the inception instant of the test archive 
     * <code>{@link #INS_INCEPT}</code>.
     * </p>
     * <p>
     * Note that the index offset of the first data source is given so that composite query lists can
     * be created for a larger request (i.e., "horizontal" decomposition).
     * The duration is given to reduce the size of the overall query.
     * </p>
     * <p>
     * Defers to method <code>{@link #createClockedRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createClockedRequest(cntPvs, indPvOffset, durRequest, {@link Duration#ZERO}}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The value of <code>cntPvs</code> + <code>indPvOffset</code> must be less than 
     * <code>{@link #IND_LAST_PV_CLOCKED}</code>.
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param indPvOffset   index of the first data source within the list of source names
     * @param durRequest    time duration of query 
     * 
     * @return  new <code>DpDataRequest</code> for cntPvs in LST_PV_NAMES_CLOCKED starting at index indPvOffset, and duration durRequest
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
     */
    public static DpDataRequest createClockedRequest(int cntPvs, int indPvOffset, Duration durRequest) throws IllegalArgumentException {

        return createClockedRequest(cntPvs, indPvOffset, durRequest, Duration.ZERO);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for clocked data sources configured by the given arguments.
     * </p>
     * <p>
     * This method provides the most general query request possible for data sources sampled with uniform sampling
     * clocks.
     * It also supports all other methods creating clocked data source query request.  
     * Additionally, this method can be used for creating composite request lists forming a 
     * larger overall query (which, potentially, can then be concurrently streamed).
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The index offset of the first data source is given so that composite query lists can
     * be created that contain a larger set of sources.
     * </li>
     * <br/>
     * <li>
     * The start time offset is given so that composite queries can be created that contain a larger
     * time range.
     * </li>
     * <br/>
     * <li>
     * Since both query domains can be decomposed this method is appropriate for creating
     * "grid-like" composite queries.
     * </li>
     * <br/>
     * <li>
     * The actual time range of the resultant query (as seen by the Query service is given by
     * <br/><br/> 
     * &nbsp; &nbsp; [<code>{@link INS_INCEPT}, {@link INS_INCEPT} + durRequest + durOffset</code>]
     * </li>
     * </ul>
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param indPvOffset   index offset of the first data source within the list of source names
     * @param durRequest    time duration of query 
     * @param durOffset     start time of the query - offset from <code>{@link #INS_INCEPT}</code>
     * 
     * @return  new <code>DpDataRequest</code> built from the given parameters
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the clocked test archive domain (see message)
     */
    public static DpDataRequest createClockedRequest(int cntPvs, int indPvOffset, Duration durRequest, Duration durOffset) 
            throws IllegalArgumentException {
        
        // Parameters
        final int       cntPvsClockedArchive = CNT_PVS_CLOCKED;
        final int       indPvFirstArchive = IND_FIRST_PV_CLOCKED;   // adjust for 0-based Java index
        final int       indPvLastArchive = indPvFirstArchive + cntPvsClockedArchive;
        
        // Create the source indexes and check them
        int indPvFirst = indPvOffset + indPvFirstArchive;
        int indPvLast = indPvFirst + cntPvs;
        
        if (indPvLast > indPvLastArchive) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + "- Request PV final index =" + indPvLast 
                    + " (with offset index=" + indPvOffset + " and total PV count=" + cntPvs + ")"
                    + " > the maximum index of clocked data sources=" + cntPvsClockedArchive + ".";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
        
        // Create first and last request instants and check them
        Instant     insStart = INS_INCEPT.plus(durOffset);
        Instant     insStop = insStart.plus(durRequest);
        
        if (insStop.isAfter(INS_FINAL)) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + "Request final instant=" + insStop
                    + " (with offset duration=" + durOffset + " and total request duration=" + durRequest + ")" 
                    + " > total archive time domain size=" + LNG_RANGE + ".";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
        
        // Create request and return it
        DpDataRequest   rqst = DpDataRequest.create();
        
        List<String>    lstNames = LST_PV_NAMES_CLOCKED.subList(indPvFirst, indPvLast);
        
        rqst.rangeBetween(insStart, insStop);
        rqst.selectSources(lstNames);
        
        return rqst;
    }
    
    
    //
    // Class Methods - Timestamp List Data Requests
    //
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance querying for all data sources using explicit timestamp lists 
     * and entire time range within the Data Platform test archive.
     * </p>
     * <p>
     * Defers to method <code>{@link #createTmsListRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createTmsListRequest({@link #CNT_PVS_CLOCKED}, 0, {@link #DUR_RNG_TOTAL}, {@link Duration#ZERO})</code>.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * The returned request returns <em>all</em> clocked data within the Data Platform test archive.  This constitutes
     * <code>{@link #CNT_PVS_TMS_LIST}</code> data sources over a time range of
     * <code>{@link #DUR_RNG_TOTAL}</code>.  Assuming <code>Double</code> Java 
     * values and 1 kHz sampling the total recovered data <i>N</i> size is approximately
     * <pre>
     *      <i>N</i> = {@link #CNT_PVS_TMS_LIST} * {@link #DUR_RNG_TOTAL} * 1,000 (samples/second) * <code>{@link Double#BYTES}</code>
     * </pre>
     * which typically amounts to <i>N</i> in the range of 0.5 GBytes.
     * </p>
     * 
     * @return  new <code>DpDataRequest</code> specifying the entire test archive.
     */
    public static DpDataRequest createTmsListRequest() {
        return createTmsListRequest(CNT_PVS_CLOCKED, 0, DUR_RNG_TOTAL, Duration.ZERO);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for data sources using explicit timestamp lists and 
     * configured by the given argument.
     * </p>
     * <p>
     * Queries for the first <code>cntPvs</code> data sources using explicit timestamp lists within the test archive 
     * for the entire time range duration of the test archive.
     * <p>
     * Defers to method <code>{@link #createTmsListRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createTmsListRequest(cntPvs, 0, {@link #DUR_RNG_TOTAL}, {@link Duration#ZERO)}</code>.
     * </p>
     * <p>
     * The number of data sources is given to reduce the size of the query.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The value of <code>cntPvs</code> must be less than <code>{@link #CNT_PVS_TMS_LIST}</code>.
     * </p>
     * 
     * @param cntPvs    number of data sources in the query
     * 
     * @return  new <code>DpDataRequest</code> for the first cntPvs in LST_PV_NAMES_TMS_LIST and time range [INS_INCEPT, INS_FINAL]
     */
    public static DpDataRequest createTmsListRequest(int cntPvs) throws IllegalArgumentException {
        return createTmsListRequest(cntPvs, 0, DUR_RNG_TOTAL, Duration.ZERO);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for clocked data sources using explicit timestamp lists 
     * and configured by the given arguments.
     * </p>
     * <p>
     * Queries for the first <code>cntPvs</code> data sources within the test archive using explicit timestamp lists
     * (the index for the first data source defaults to 0) with the given duration 
     * (start time default to <code>{@link #INS_INCEPT}</code>).
     * </p>
     * <p>
     * Defers to method <code>{@link #createTmsListRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createTmsListRequest(cntPvs, 0, durRequest, {@link Duration#ZERO)}</code>.
     * </p>
     * <p>
     * Note that the no starting index or start time is given so the request always starts
     * at the origin of the timestamp list query domain.  Not intended for composite query lists. 
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param durRequest    time duration of query , range = [INS_INCEPT, INS_INCEPT + durRequest]
     * 
     * @return  new <code>DpDataRequest</code> for the all data source in LST_PV_NAMES_TMS_LIST and the specified time range
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
     */
    public static DpDataRequest createTmsListRequest(int cntPvs, Duration durRequest) throws IllegalArgumentException {
        return createTmsListRequest(cntPvs, 0, durRequest, Duration.ZERO);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for data sources using explicit timestamp lists 
     * and configured by the given arguments.
     * </p>
     * <p>
     * Queries for the first <code>cntPvs</code> data sources within the test archive using explicit timestamp lists.
     * (The index for the first data source defaults to 0.)
     * </p>
     * <p>
     * Defers to method <code>{@link #createTmsListRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createTmsListRequest(cntPvs, 0, durRequest, durOffset}</code>.
     * </p>
     * <p>
     * Note that the start time is given so that composite query lists can
     * be created to for a larger request (i.e., "vertical" decomposition).
     * The number of data sources is given to reduce the size of the overall query.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The value of <code>cntPvs</code> must be less than <code>{@link #CNT_PVS_TMS_LIST}</code>.
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param durRequest    time duration of query , range = [INS_INCEPT + durOffset, INS_INCEPT + durOffset + lngDuration]
     * @param durOffset     start time offset of the query - offset from <code>{@link #INS_INCEPT}</code>
     * 
     * @return  new <code>DpDataRequest</code> for the first cntPvs in LST_PV_NAMES_TMS_LIST and the specified time range
     * 
     * @throws IllegalArgumentException  the arguments creates a query request outside the test archive domain
     */
    public static DpDataRequest createTmsListRequest(int cntPvs, Duration durRequest, Duration durOffset) throws IllegalArgumentException {
        return createTmsListRequest(cntPvs, 0, durRequest, durOffset);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for data sources using explicit timestamp lists and 
     * configured by the given arguments.
     * </p>
     * <p>
     * Queries for <code>cntPvs</code> data sources within the test archive using explicit timestamp lists.
     * The starting index for the data source is given by <code>indPvOffset</code> with the request duration given 
     * by <code>durRequest</code>.  The start time of the request is given by the inception instant of the test archive 
     * <code>{@link #INS_INCEPT}</code>.
     * </p>
     * <p>
     * Note that the index offset of the first data source is given so that composite query lists can
     * be created for a larger request (i.e., "horizontal" decomposition).
     * The duration is given to reduce the size of the overall query.
     * </p>
     * <p>
     * Defers to method <code>{@link #createTmsListRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createClockedRequest(cntPvs, indPvOffset, durRequest, {@link Duration#ZERO}}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The value of <code>cntPvs</code> + <code>indPvOffset</code> must be less than 
     * <code>{@link #IND_LAST_PV_TMS_LIST}</code>.
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param indPvOffset   index of the first data source within the list of source names
     * @param durRequest    time duration of query 
     * 
     * @return  new <code>DpDataRequest</code> for cntPvs in LST_PV_NAMES_TMS_LIST starting at index indPvOffset, and duration durRequest
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
     */
    public static DpDataRequest createTmsListRequest(int cntPvs, int indPvOffset, Duration durRequest) throws IllegalArgumentException {

        return createTmsListRequest(cntPvs, indPvOffset, durRequest, Duration.ZERO);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for data sources using explicit timestamp lists and
     * configured by the given arguments.
     * </p>
     * <p>
     * This method provides the most general query request possible for data sources sampled using explicit
     * timestamp lists.
     * It also supports all other methods creating timestamp list data source query requests.  
     * Additionally, this method can be used for creating composite request lists forming a 
     * larger overall query (which, potentially, can then be concurrently streamed).
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The index offset of the first data source is given so that composite query lists can
     * be created that contain a larger set of sources.
     * </li>
     * <br/>
     * <li>
     * The start time offset is given so that composite queries can be created that contain a larger
     * time range.
     * </li>
     * <br/>
     * <li>
     * Since both query domains can be decomposed this method is appropriate for creating
     * "grid-like" composite queries.
     * </li>
     * <br/>
     * <li>
     * The actual time range of the resultant query (as seen by the Query service is given by
     * <br/><br/> 
     * &nbsp; &nbsp; [<code>{@link INS_INCEPT}, {@link INS_INCEPT} + durRequest + durOffset</code>]
     * </li>
     * </ul>
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param indPvOffset   index offset of the first data source within the list of source names
     * @param durRequest    time duration of query 
     * @param durOffset     start time of the query - offset from <code>{@link #INS_INCEPT}</code>
     * 
     * @return  new <code>DpDataRequest</code> built from the given parameters
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the timestamp list test archive domain (see message)
     */
    public static DpDataRequest createTmsListRequest(int cntPvs, int indPvOffset, Duration durRequest, Duration durOffset) 
            throws IllegalArgumentException {
        
        // Parameters
        final int       cntPvsClockedArchive = CNT_PVS_TMS_LIST;
        final int       indPvFirstArchive = IND_FIRST_PV_TMS_LIST;   // adjust for 0-based Java index
        final int       indPvLastArchive = indPvFirstArchive + cntPvsClockedArchive;
        
        // Create the source indexes and check them
        int indPvFirst = indPvOffset + indPvFirstArchive;
        int indPvLast = indPvFirst + cntPvs;
        
        if (indPvLast > indPvLastArchive) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + "- Request PV final index =" + indPvLast 
                    + " (with offset index=" + indPvOffset + " and total PV count=" + cntPvs + ")"
                    + " > the maximum index of timestamp list data sources=" + cntPvsClockedArchive + ".";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
        
        // Create first and last request instants and check them
        Instant     insStart = INS_INCEPT.plus(durOffset);
        Instant     insStop = insStart.plus(durRequest);
        
        if (insStop.isAfter(INS_FINAL)) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + "Request final instant=" + insStop
                    + " (with offset duration=" + durOffset + " and total request duration=" + durRequest + ")" 
                    + " > total archive time domain size=" + LNG_RANGE + ".";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
        
        // Create request and return it
        DpDataRequest   rqst = DpDataRequest.create();
        
        List<String>    lstNames = LST_PV_NAMES_TMS_LIST.subList(indPvFirst, indPvLast);
        
        rqst.rangeBetween(insStart, insStop);
        rqst.selectSources(lstNames);
        
        return rqst;
    }
    

    //
    // Class Methods - Both Timestamp List and Clocked
    //
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for data sources using both uniform sampling clocks and
     * explicit timestamp lists (configured by the given arguments).
     * </p>
     * <p>
     * Queries for the first <code>cntPvs</code> data sources within the test archive using explicit timestamp lists.
     * (The index for the first data source defaults to 0 and the start time defaults to 0.)
     * </p>
     * <p>
     * Defers to method <code>{@link #createBothRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createBothRequest(cntPvs, 0, durRequest, {@link Duration#ZERO)}</code>.
     * </p>
     * <p>
     * Note that the start time is given so that composite query lists can
     * be created to for a larger request (i.e., "vertical" decomposition).
     * The number of data sources is given to reduce the size of the overall query.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The value of <code>cntPvs</code> must be less than <code>{@link #CNT_PVS_TMS_LIST}/2</code>.
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param durRequest    time duration of query , range = [INS_INCEPT + durOffset, INS_INCEPT + durOffset + lngDuration]
     * 
     * @return  new <code>DpDataRequest</code> for the first cntPvs in LST_PV_NAMES_TMS_LIST and the specified time range
     * 
     * @throws IllegalArgumentException  either offset index >= cntPvs/2, or query request outside the archive domain (see message)
     */
    public static DpDataRequest createBothRequest(int cntPvs, Duration durRequest) throws IllegalArgumentException {
        return createTmsListRequest(cntPvs, 0, durRequest, Duration.ZERO);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for data sources using both uniform sampling clocks and
     * explicit timestamp lists (configured by the given arguments).
     * </p>
     * <p>
     * Queries for the first <code>cntPvs</code> data sources within the test archive using explicit timestamp lists.
     * (The index for the first data source defaults to 0.)
     * </p>
     * <p>
     * Defers to method <code>{@link #createBothRequest(int, int, Duration, Duration)}</code> using default values
     * for arguments not supplied.
     * This method is equivalent to invoking 
     * <code>createBothRequest(cntPvs, 0, durRequest, durOffset}</code>.
     * </p>
     * <p>
     * Note that the start time is given so that composite query lists can
     * be created to for a larger request (i.e., "vertical" decomposition).
     * The number of data sources is given to reduce the size of the overall query.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The value of <code>cntPvs</code> must be less than <code>{@link #CNT_PVS_TMS_LIST}/2</code>.
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param durRequest    time duration of query , range = [INS_INCEPT + durOffset, INS_INCEPT + durOffset + lngDuration]
     * @param durOffset     start time offset of the query - offset from <code>{@link #INS_INCEPT}</code>
     * 
     * @return  new <code>DpDataRequest</code> for the first cntPvs in LST_PV_NAMES_TMS_LIST and the specified time range
     * 
     * @throws IllegalArgumentException  either offset index >= cntPvs/2, or query request outside the archive domain (see message)
     */
    public static DpDataRequest createBothRequest(int cntPvs, Duration durRequest, Duration durOffset) throws IllegalArgumentException {
        return createTmsListRequest(cntPvs, 0, durRequest, durOffset);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for data sources using both uniform sampling clocks and
     * explicit timestamp lists while configured by the given arguments.
     * </p>
     * <p>
     * This method provides the most general query request possible for both types of data sources.
     * It also supports all other methods creating both sampling clock and explicit timestamp list 
     * data source query requests.  
     * Additionally, this method can be used for creating composite request lists forming a 
     * larger overall query (which, potentially, can then be concurrently streamed).
     * </p>
     * <p>
     * <h2>Request Creation</h2>
     * The inclusion of both types of data sources is made possible by inspection of the data source name
     * list at the boundary <code>{@link #IND_FIRST_PV_TMS_LIST}</code>.  For a returned request where parameter
     * <code>indPvOffset = 0</code> we take <code>cntPvs</code> names from list <code>{@link #LST_PV_NAMES_TOTAL}</code>
     * starting at index <code>{@link #IND_FIRST_PV_TMS_LIST} - cntPvs/2</code>.  If <code>indPvOffset &ne; 0</code>
     * then we offset the returned name sublist from <code>{@link #LST_PV_NAMES_TOTAL}</code> by that amount.
     * Note that if <code>indPvOffset &ge; cntPvs/2</code> then this technique fails and an exception is thrown. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The index offset of the first data source is given so that composite query lists can
     * be created that contain a larger set of sources.
     * </li>
     * <br/>
     * <li>
     * The start time offset is given so that composite queries can be created that contain a larger
     * time range.
     * </li>
     * <br/>
     * <li>
     * Since both query domains can be decomposed this method is appropriate for creating
     * "grid-like" composite queries.
     * </li>
     * <br/>
     * <li>
     * The actual time range of the resultant query (as seen by the Query service is given by
     * <br/><br/> 
     * &nbsp; &nbsp; [<code>{@link INS_INCEPT}, {@link INS_INCEPT} + durRequest + durOffset</code>]
     * </li>
     * </ul>
     * </p>
     * 
     * @param cntPvs        number of data sources in the query
     * @param indPvOffset   index offset of the first data source within the list of source names
     * @param durRequest    time duration of query 
     * @param durOffset     start time of the query - offset from <code>{@link #INS_INCEPT}</code>
     * 
     * @return  new <code>DpDataRequest</code> built from the given parameters
     * 
     * @throws IllegalArgumentException  either offset index >= cntPvs/2, or query request outside the archive domain (see message)
     */
    public static DpDataRequest createBothPvsRequest(int cntPvs, int indPvOffset, Duration durRequest, Duration durOffset) 
            throws IllegalArgumentException {
        
        // Check PV offset index and count
        if (indPvOffset >= cntPvs/2) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - PV offset index=" + indPvOffset + " >= (PV count)/2=" + cntPvs/2;
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
        
        // Parameters
        final int       cntPvsBothArchive = 2 * CNT_PVS_TMS_LIST;
        final int       indPvBothFirstArchive = IND_FIRST_PV_TMS_LIST - CNT_PVS_TMS_LIST;   
        final int       indPvBothLastArchive = indPvBothFirstArchive + cntPvsBothArchive;
        
        // Create the source indexes and check them
        int indPvFirst = IND_FIRST_PV_TMS_LIST - cntPvs/2;
        int indPvLast = indPvFirst + cntPvs;
        
        if (indPvLast > indPvBothLastArchive) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + "- Request PV final index =" + indPvLast 
                    + " (with offset index=" + indPvOffset + " and total PV count=" + cntPvs + ")"
                    + " > the maximum index of both data sources=" + cntPvsBothArchive + ".";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
        
        // Create first and last request instants and check them
        Instant     insStart = INS_INCEPT.plus(durOffset);
        Instant     insStop = insStart.plus(durRequest);
        
        if (insStop.isAfter(INS_FINAL)) {
            String strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + "Request final instant=" + insStop
                    + " (with offset duration=" + durOffset + " and total request duration=" + durRequest + ")" 
                    + " > total archive time domain size=" + LNG_RANGE + ".";
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new IllegalArgumentException(strMsg);
        }
        
        // Create request and return it
        DpDataRequest   rqst = DpDataRequest.create();
        
        List<String>    lstNames = LST_PV_NAMES_TOTAL.subList(indPvFirst, indPvLast);
        
        rqst.rangeBetween(insStart, insStop);
        rqst.selectSources(lstNames);
        
        return rqst;
    }
    
    
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Prevent construction <code>TestArchiveRequestCreator</code> instances.
     * </p>
     */
    private TestArchiveRequestCreator() {
    }

}
