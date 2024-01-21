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

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import com.ospreydcs.dp.api.config.test.DpApiTestingConfig;
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
 * <p>
 * Note that there are three parameters to the query:
 * <ul>
 * <li>
 * <code>cntSources</code> - number of data sources in the query.
 * </li>
 * <li>
 * <code>indSourceFirst</code> - index of the first data source within the list of source names,
 *     see <code>{@link #LST_PV_NAMES}</code>.
 * </li>
 * <li><code>lngDuration</code> - time duration of query (in seconds), 
 *     range = [INS_INCEPT, INS_INCEPT + lngDuration].
 * </li>
 * </ul>
 * All <code>createRequest(...)</code> methods with fewer parameters use default values taken
 * from the <code>{@link DpApiTestingConfig#testArchive}</code> parameter set.
 * </p> 
 *  
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
    public static final DpApiTestingConfig.TestArchive      CFG_ARCHIVE = DpApiTestingConfig.getInstance().testArchive;

    
    //
    // Class Constants
    //

    /** Size of time domain with Data Platform data archive test data set */ 
    public static final Long        LNG_DURATION;
    
    /** The inception time instant of the test Data Platform data archive test data set*/
    public static final Instant     INS_INCEPT;
    
    /** The final time instant of all Data Platform data archive test data set */
    public static final Instant     INS_FINAL;
    
    
    /** The total number of unique data source names within the Data Platform data archive test data set */
    public static final int         CNT_PV_NAMES;
    
    /** List of all data source names within the Data Platform data archive test data set */
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
        LNG_DURATION = CFG_ARCHIVE.duration;
        INS_INCEPT = Instant.ofEpochSecond(CFG_ARCHIVE.inception);
        INS_FINAL = INS_INCEPT.plusSeconds(CFG_ARCHIVE.duration);

        CNT_PV_NAMES = CFG_ARCHIVE.pvCount;
        LST_PV_NAMES = IntStream
                .rangeClosed(1, CNT_PV_NAMES)
                .mapToObj( i -> "pv_" + Integer.toString(i))
                .toList();   
    }
    
    
    //
    // Class Methods
    //
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance configured by the given argument.
     * </p>
     * <p>
     * The index for the first data source defaults to 0 and the time range  
     * defaults to the entire duration of the test archive.
     * </p>
     * <p>
     * Note that the index of the first data source is given so that multiple queries can
     * be created for a larger target set of sources (which can then be parallel streamed).
     * </p>
     * 
     * @param cntSources    number of data sources in the query
     * 
     * @return  new <code>DpDataRequest</code> for the first cntSources in LST_PV_NAMES and time range [INS_INCEPT, INS_FINAL]
     */
    public static DpDataRequest createRequest(int cntSources) throws IllegalArgumentException {
        return createRequest(cntSources, 0, LNG_DURATION);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
     * </p>
     * <p>
     * The index for the first data source defaults to 0.
     * </p>
     * <p>
     * Note that the index of the first data source is given so that multiple queries can
     * be created for a larger target set of sources (which can then be parallel streamed).
     * </p>
     * 
     * @param cntSources        number of data sources in the query
     * @param lngDuration       time duration of query (in seconds), range = [INS_INCEPT, INS_INCEPT + lngDuration]
     * 
     * @return  new <code>DpDataRequest</code> for the first cntSources in LST_PV_NAMES and the specified time range
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
     */
    public static DpDataRequest createRequest(int cntSources, long lngDuration) throws IllegalArgumentException {
        return createRequest(cntSources, 0, lngDuration);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
     * </p>
     * <p>
     * The time range of the returned query defaults to the entire duration of the test archive.
     * </p>
     * <p>
     * Note that the index of the first data source is given so that multiple queries can
     * be created for a larger target set of sources (which can then be parallel streamed).
     * </p>
     * 
     * @param cntSources        number of data sources in the query
     * @param indSourceFirst    index of the first data source within the list of source names
     * 
     * @return  new <code>DpDataRequest</code> for the first cntSources in LST_PV_NAMES and the specified time range
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
     */
    public static DpDataRequest createRequest(int cntSources, int indSourceFirst) throws IllegalArgumentException {

        return createRequest(cntSources, indSourceFirst, LNG_DURATION);
    }
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance configured by the given arguments.
     * </p>
     * <p>
     * This method provides the most general query possible for this utility and supports all 
     * other query request generation methods.
     * </p>
     * <p>
     * Note that the index of the first data source is given so that multiple queries can
     * be created for a larger target set of sources (which can then be parallel streamed).
     * </p>
     * 
     * @param cntSources        number of data sources in the query
     * @param indSourceFirst    index of the first data source within the list of source names
     * @param lngDuration       time duration of query (in seconds), range = [INS_INCEPT, INS_INCEPT + lngDuration]
     * 
     * @return  new <code>DpDataRequest</code> for the first cntSources in LST_PV_NAMES and the specified time range
     * 
     * @throws IllegalArgumentException  the arguments create a query request outside the test archive
     */
    public static DpDataRequest createRequest(int cntSources, int indSourceFirst, long lngDuration) 
            throws IllegalArgumentException {
        
        // The last source index
        int indSourceLast = indSourceFirst + cntSources;
        
        // Check the argument(s)
        if (indSourceFirst + cntSources > CNT_PV_NAMES) {
            throw new IllegalArgumentException("Requested data sources final index =" + indSourceLast + " > " + CNT_PV_NAMES + " total number of data sources.");
        }
        
        if (lngDuration > LNG_DURATION) {
            throw new IllegalArgumentException("Requested duration =" + lngDuration + " > " + LNG_DURATION + " total time domain size.");
        }
        
        // Create request and return it
        DpDataRequest   rqst = DpDataRequest.newRequest();
        
        List<String>    lstNames = LST_PV_NAMES.subList(indSourceFirst, indSourceLast);
        Instant         insFinal = INS_INCEPT.plusSeconds(lngDuration);
        
        rqst.rangeBetween(INS_INCEPT, insFinal);
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
