/*
 * Project: dp-api-common
 * File:	TestDpDataRequestGenerator.java
 * Package: com.ospreydcs.dp.api.query
 * Type: 	TestDpDataRequestGenerator
 *
 * Copyright 2010-2023 the original author or authors.
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
 * @since Jan 13, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import com.ospreydcs.dp.api.config.JalUnitTestConfig;
import com.ospreydcs.dp.api.query.DpDataRequest;

/**
 * <p>
 * Utility class for creating <code>DpDataRequest</code> instances used in testing.
 * </p>
 * <p>
 * Creates <code>DpDataRequest</code> instances which are valid against the Data Platform 
 * testing archive.  Configuration of the DP test archive is provided from the DP 
 * testing configuration.
 * </p>
 * <h2>Data Platform Test Archive</h2>
 * The Data Platform test archive is populated with the utility <i>app-run-test-data-generator</i>
 * which ships with the Data Platform installation.  This utility must be run before using this
 * utility class or all query requests will fail.  The parameters for the test archive are given
 * as class constants which can be changed if the test archive is modified.
 * <h2>Data Requests</h2>
 * <p>
 * Note that there are 4 parameters to the full query request:
 * <ul>
 * <li>
 * <code>cntSources</code> - number of data sources in the query.
 * </li>
 * <li>
 * <code>indSourceFirst</code> - index of the first data source within the list of source names.
 * </li>
 * <li><code>lngDuration</code> - time duration of query (in seconds), 
 * </li>
 * <li><code>lngStartTime</code> - starting time instant (in seconds). 
 * </li>
 * </ul>
 * All <code>createRequest(...)</code> methods with fewer parameters use default values taken
 * from the <code>{@link JalUnitTestConfig#testArchive}</code> parameter set, 
 * or zero - whichever is appropriate.
 * </p> 
 * <p>
 * <h2>Composite Query</h2>
 * Large queries can be decomposed of multiple sub-queries which can then be concurrently
 * streamed on separate execution threads.  Consider the following examples:
 * <ul>
 * <li>
 * Horizontal - data sources are divided amongst multiple queries over the same time range.
 * </li>
 * <li>
 * Vertical - the time range is divided amongst multiple queries over the same data sources.
 * </li>
 * <li>
 * Grid - data sources and time ranges are both divided amongst multiple queries.
 * </li>
 * </ul>
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * The <code>cntSources</code> and <code>indSourceFirst</code> parameters must together create 
 * a valid query against the Data Platform test archive, 
 * see <code>{@link #CNT_PV_NAMES_TOTAL}</code> and <code>{@link #LST_PV_NAMES_TOTAL}</code>.
 * Specifically, the following condition must hold:
 * <br/><br/>
 *   &nbsp; &nbsp; <code>cntSources</code> + <code>indSourceFirst</code> &lt; <code>{@link #CNT_PV_NAMES_TOTAL}</code>
 * <br/><br/>
 * </li>
 * <li>
 * The <code>lngDuration</code> and <code>lngStartTime</code> parameters must together create
 * a valid query against the Data Platform test archive, see <code>{@link #LNG_RANGE}</code>.
 * Specifically, the following condition must hold:
 * <br/><br/>
 *   &nbsp; &nbsp; <code>lngStartTime</code> + <code>lngDuration</code> &le; <code>{@link #LNG_RANGE}</code>
 * <br/>
 * </li>
 * <br/>
 * <li>
 * The actual time range of the produced query (as seen by the Query Service) is the interval
 * <br/><br/>
 *   &nbsp; &nbsp; range = [<code>{@link #INS_INCEPT}, {@link #INS_INCEPT} + lngStartTime + lngDuration].
 * <br/>
 * </li>
 * </ul>
 * 
 * @author Christopher K. Allen
 * @since Jan 13, 2024
 *
 */
public class TestDpDataRequestGenerator {

    //
    // Application Resources
    //
    
    /** Default DP API library testing parameters */
    public static final JalUnitTestConfig.TestArchive      CFG_ARCHIVE = JalUnitTestConfig.getInstance().testArchive;

    
    //
    // Class Constants
    //


    /** The inception time instant of the test archive */
    private static final String STR_ISO_ARCHIVE_INCEPT = CFG_ARCHIVE.firstTimestamp; // "2023-10-31T15:51:02.000+00:00";
    
    /** The last timestamp instant within the test data archive */
    private static final String STR_ISO_ARCHIVE_LAST = CFG_ARCHIVE.lastTimestamp; // "2023-10-31T15:51:21.999+00:00"; 

    
    /** 
     * The inception time instant of the test Data Platform data archive test data set.
     * <p>
     * The value is taken from test default parameter [<code>testArchive.inception</code>].
     */
    public static final Instant     INS_INCEPT = Instant.parse(STR_ISO_ARCHIVE_INCEPT);
    
    /** 
     * The final time instant of all Data Platform data archive test data set
     * <p>
     * This is a computed value equal to <code>{@link #INS_INCEPT} + {@link #LNG_RANGE}</code>.
     */
    public static final Instant     INS_FINAL = Instant.parse(STR_ISO_ARCHIVE_LAST);
    
    /** 
     * Size of time domain (i.e., range) with Data Platform data archive test data set.
     * <p>
     * The value is taken from test default parameters [<code>{@link #INS_INCEPT} and {@link #INS_FINAL}</code>].
     */ 
    public static final Duration    DUR_RANGE = Duration.between(INS_INCEPT, INS_FINAL);

    /** 
     * Size of time domain (i.e., range) with Data Platform data archive test data set in nanoseconds.
     * <p>
     * The value is taken from test default parameters [<code>{@link #DUR_RNG_TOTAL}</code>].
     */ 
    public static final Long        LNG_RANGE = DUR_RANGE.toNanos();
 
   
    /** 
     * The total number of unique data source names within the Data Platform data archive test data set.
     * <p>
     * The value is taken from test default parameter [<code>testArchive.pvCount</code>].
     */
    public static final int         CNT_PV_NAMES = CFG_ARCHIVE.pvCount; // 4000;
    
    /**
     * The prefix for each data source name within the Data Platform test archive.
     * <p>
     * The value is taken from test default parameter [<code>testArchive.pvPrefix</code>].
     */
    public static final String      STR_PV_PREFIX = CFG_ARCHIVE.pvPrefix; // "dpTest_";
    
    /** 
     * List of all data source names within the Data Platform data archive test data set 
     * <p>
     * The prefix for the data source names is taken from test default parameter [<code>testArchive.pvPrefix</code>].
     */
    public static final List<String> LST_PV_NAMES;
    
    
    /**
     * <p>
     * Class static execution block.
     * </p>
     * <p>
     * Computes and initializes all class constants.
     * </p>
     */
    static {
//        INS_INCEPT = Instant.parse(STR_ISO_ARCHIVE_INCEPT);
//        
//        LNG_RANGE = CFG_ARCHIVE.duration;
//        INS_INCEPT = Instant.ofEpochSecond(CFG_ARCHIVE.inception);
//        INS_FINAL = INS_INCEPT.plusSeconds(CFG_ARCHIVE.duration);
//
//        CNT_PV_NAMES_TOTAL = CFG_ARCHIVE.pvCount;
//        STR_PV_PREFIX = CFG_ARCHIVE.pvPrefix;
        
        LST_PV_NAMES = IntStream
                .rangeClosed(1, CNT_PV_NAMES)
                .mapToObj( i -> STR_PV_PREFIX + Integer.toString(i))
                .toList();   
    }
    
    
    //
    // Class Methods
    //
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance that queries the entire 
     * Data Platform test archive.
     * </p>
     * 
     * @return  new <code>DpDataRequest</code> specifying the entire test archive.
     */
    public static DpDataRequest createRequest() {
        return createRequest(CNT_PV_NAMES, 0, LNG_RANGE, 0L);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance configured by the given argument.
     * </p>
     * <p>
     * The index for the first data source defaults to 0 and the time range  
     * defaults to the entire duration of the test archive.
     * </p>
     * <p>
     * The number of data sources is given to reduce the size of the query.
     * </p>
     * 
     * @param cntSources    number of data sources in the query
     * 
     * @return  new <code>DpDataRequest</code> for the first cntSources in LST_PV_NAMES_TOTAL and time range [INS_INCEPT, INS_FINAL]
     */
    public static DpDataRequest createRequest(int cntSources) throws IllegalArgumentException {
        return createRequest(cntSources, 0, LNG_RANGE, 0L);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
     * </p>
     * <p>
     * Queries for the first <code>cntSources</code> of data sources within the test archive.  
     * (The index for the first data source defaults to 0.)
     * Queries for the given duration (start time default to 0L).
     * </p>
     * <p>
     * Note that the no starting index or start time is given so the request always starts
     * at the origin of the query domain.  Not intended for decompose queries. 
     * </p>
     * 
     * @param cntSources        number of data sources in the query
     * @param lngDuration       time duration of query (in nanoseconds), range = [INS_INCEPT, INS_INCEPT + lngDuration]
     * 
     * @return  new <code>DpDataRequest</code> for the all data source in LST_PV_NAMES_TOTAL and the specified time range
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
     */
    public static DpDataRequest createRequest(int cntSources, long lngDuration) throws IllegalArgumentException {
        return createRequest(cntSources, 0, lngDuration, 0L);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
     * </p>
     * <p>
     * Queries for the entire set of data sources within the test archive.  
     * (The index for the first data source defaults to 0.)
     * </p>
     * <p>
     * Note that the start time is given so that decompose queries can
     * be created to target a larger time range (i.e., "vertical" decomposition).
     * </p>
     * 
     * @param lngDuration       time duration of query (in seconds), range = [INS_INCEPT, INS_INCEPT + lngDuration]
     * @param lngStartTime      start time of the query (in seconds) - actually an offset from <code>{@link #INS_INCEPT}</code>
     * 
     * @return  new <code>DpDataRequest</code> for the all data source in LST_PV_NAMES_TOTAL and the specified time range
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
     */
    public static DpDataRequest createRequest(long lngDuration, long lngStartTime) throws IllegalArgumentException {
        return createRequest(CNT_PV_NAMES, 0, lngDuration, lngStartTime);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
     * </p>
     * <p>
     * Queries for the first <code>cntSources</code> data sources within the test archive.
     * (The index for the first data source defaults to 0.)
     * </p>
     * <p>
     * Note that the start time is given so that decompose queries can
     * be created to target a larger time range (i.e., "vertical" decomposition).
     * The number of data sources is given to reduce the size of the overall query.
     * </p>
     * 
     * @param cntSources        number of data sources in the query
     * @param lngDuration       time duration of query (in nanoseconds), range = [INS_INCEPT, INS_INCEPT + lngDuration]
     * @param lngStartTime      start time of the query (in nanoseconds) - actually an offset from <code>{@link #INS_INCEPT}</code>
     * 
     * @return  new <code>DpDataRequest</code> for the first cntSources in LST_PV_NAMES_TOTAL and the specified time range
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
     */
    public static DpDataRequest createRequest(int cntSources, long lngDuration, long lngStartTime) throws IllegalArgumentException {
        return createRequest(cntSources, 0, lngDuration, lngStartTime);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
     * </p>
     * <p>
     * The time range of the returned query defaults to the entire duration of the test archive,
     * (the start time defaults to 0).
     * </p>
     * <p>
     * Note that the index of the first data source is given so that decompose queries can
     * be created for a larger target set of sources (i.e., "horizontal" decomposition).
     * </p>
     * 
     * @param cntSources        number of data sources in the query
     * @param indSourceFirst    index of the first data source within the list of source names
     * 
     * @return  new <code>DpDataRequest</code> for the cntSources in LST_PV_NAMES_TOTAL starting at index indSourceFirst, and entire duration of the archive
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
     */
    public static DpDataRequest createRequest(int cntSources, int indSourceFirst) throws IllegalArgumentException {

        return createRequest(cntSources, indSourceFirst, LNG_RANGE, 0L);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
     * </p>
     * <p>
     * The start time of the returned query defaults to 0.
     * </p>
     * <p>
     * Note that the index of the first data source is given so that decompose queries can
     * be created for a larger target set of sources (i.e., "horizontal" decomposition).
     * The duration is given to reduce the size of the overall query.
     * </p>
     * 
     * @param cntSources        number of data sources in the query
     * @param indSourceFirst    index of the first data source within the list of source names
     * @param lngDuration       time duration of query (in nanoseconds)
     * 
     * @return  new <code>DpDataRequest</code> for the first cntSources in LST_PV_NAMES_TOTAL and the specified time range
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
     */
    public static DpDataRequest createRequest(int cntSources, int indSourceFirst, long lngDuration) throws IllegalArgumentException {

        return createRequest(cntSources, indSourceFirst, lngDuration, 0L);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
     * </p>
     * <p>
     * This method provides the most general query possible for this utility and supports all 
     * other query request generation methods.  In particular, this method can be used for
     * "blocked" decomposition of a targeted larger query.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The index of the first data source is given so that decompose queries can
     * be created that target a larger set of sources (which can then be concurrently streamed).
     * </li>
     * <br/>
     * <li>
     * The start time is given so that decompose queries can be created that target a larger
     * time range (which can then be concurrently streamed).
     * </li>
     * <br/>
     * <li>
     * Since both query domains can be decomposed this method is appropriate for creating
     * "grid" decompose queries.
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
     * @param cntSources        number of data sources in the query
     * @param indSourceFirst    index of the first data source within the list of source names
     * @param lngDuration       time duration of query (in nanoseconds)
     * @param lngStartTime      start time of the query (in nanoseconds) - actually an offset from <code>{@link #INS_INCEPT}</code>
     * 
     * @return  new <code>DpDataRequest</code> build from the given parameters
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive (see message)
     */
    public static DpDataRequest createRequest(int cntSources, int indSourceFirst, long lngDuration, long lngStartTime) 
            throws IllegalArgumentException {
        
        // The last source index
        int indSourceLast = indSourceFirst + cntSources;
        
        // Check the argument(s)
        if (indSourceFirst + cntSources > CNT_PV_NAMES) {
            throw new IllegalArgumentException("Requested data sources final index =" + indSourceLast + " > " + CNT_PV_NAMES + " total number of data sources.");
        }
        
        if (lngStartTime + lngDuration > LNG_RANGE) {
            throw new IllegalArgumentException("Requested duration =" + lngDuration + " > " + LNG_RANGE + " total time domain size.");
        }
        
        // Create request and return it
        DpDataRequest   rqst = DpDataRequest.create();
        
        List<String>    lstNames = LST_PV_NAMES.subList(indSourceFirst, indSourceLast);
        Instant         insStart = INS_INCEPT.plusNanos(lngStartTime);
        Instant         insFinal = insStart.plusNanos(lngDuration);
        
        rqst.rangeBetween(insStart, insFinal);
        rqst.selectSources(lstNames);
        
        return rqst;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Prevent construction of <code>TestDpDataRequestGenerator</code> instances.
     * </p>
     *
     */
    private TestDpDataRequestGenerator() {
    }

}
