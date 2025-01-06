/*
 * Project: dp-api-common
 * File:	DataRequestDecomposer.java
 * Package: com.ospreydcs.dp.api.query.model.request
 * Type: 	DataRequestDecomposer
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
 * @since Dec 28, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.request;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpDataRequestConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.query.DpDataRequest;

/**
 * <p>
 * Class for decomposing Query Service time-series data requests into decompose requests.
 * </p>
 * <p>
 * <h2>Objective</h2>
 * Within the Java API a Query Service time-series data request is represented by an instance of
 * <code>DpDataRequest</code>.  These instances contain a single <code>QueryDataRequest</code> 
 * Protocol Buffers message defining the request.  In the gRPC data streaming case only one
 * stream is support per time-series data request.  By decomposing a single query request
 * into multiple decompose requests it is possible to recover the query result set in multiple
 * gRPC data streams.
 * </p>
 * <h2>Request Decomposition</h2>
 * Query requests can be decomposed horizontal across data source axis, vertically across timestamp axis, or
 * in a grid by both axes.  The safest decomposition strategy is a horizontal decomposition.  Vertical
 * decomposition can yield unexpected results sets, specifically, repeated result sets in the
 * returned data sets. 
 * </p>
 * <p>  
 * <h2>Data Bucket Granularity</h2>
 * Note that time-series data is stored in 'data buckets' within the Data Platform
 * archive.  Data buckets have an atomic structure containing a given number of samples and
 * timestamps for a data source.  If a time-series data request specifies any timestamps within
 * a given data bucket the entire data bucket is returned. Thus, decomposing a time-series data request 
 * vertically can yield the same result set for each decompose request.  
 * </p>
 * <p>
 * Due to the design of the <code>QueryResponseCorrelator</code> the above situation will always yield the correct 
 * result set for the query. However, this situation will significantly degrade performance and should be avoided.
 * Only use vertical and grid decomposition when the structure of the data buckets is known (which requires
 * knowledge of the sampling process for the requested data sources). 
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Dec 28, 2024
 *
 */
public class DataRequestDecomposer {
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new instance of <code>DataRequestDecomposer</code> initialized to the default parameters.
     * </p>
     * <p>
     * The returned instance is configured to the default parameters specified in the Java API Library
     * configuration file <i>dp-api-config.yml</i> under the section <code>query.data.request.composite</code>.  
     * Refer to the configuration file for values and description.
     * </p> 
     * <p>
     * Note that the returned <code>DataRequestDecomposer</code> can be custom configured using the setter methods 
     * for the configuration parameters.
     * </p>
     *  
     * @return  new <code>DataRequestDecomposer</code> instance configured to default parameters
     */
    public static DataRequestDecomposer create() {
        return new DataRequestDecomposer();
    }

    
    //
    //  Application Resources
    //
    
    /** The default API library configuration for Query Service operations */
    private static final DpDataRequestConfig CFG_RQST = DpApiConfig.getInstance().query.data.request;
    
    
    //
    // Class Constants
    //
    
    /** Use decompose queries feature */
    private static final boolean                BOL_ACTIVE = CFG_RQST.decompose.active;
    
    /** Maximum number of data sources for a decompose query */
    private static final int                    CNT_MAX_SOURCES = CFG_RQST.decompose.maxSources;
    
    /** Maximum time range duration for a decompose query */
    private static final Duration               DUR_MAX = Duration.of(
                                                    CFG_RQST.decompose.maxDuration,
                                                    CFG_RQST.decompose.unitDuration.toChronoUnit()
                                                    );
    
    //
    // Configuration Parameters
    //
    
    /** Turn request decomposition on/off */
    private boolean     bolActive = BOL_ACTIVE;
    
    /** Maximum allowable number of data sources in a decompose request */
    private int         cntSrcsMax = CNT_MAX_SOURCES;
    
    /** Maximum allowable time duration in a decompose request */
    private Duration    durMax = DUR_MAX;
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DataRequestDecomposer</code> initialized to the default parameters.
     * </p>
     * <p>
     * The new <code>DataRequestDecomposer</code> can be custom configured using the setter methods for the
     * configuration parameters.
     * </p> 
     */
    public DataRequestDecomposer() {
        // Already Initialized 
    }

    
    //
    // Configuration
    //
    
    /**
     * @return <code>true</code> if request decomposition is enabled, <code>false</code> if disabled
     */
    public final boolean isActive() {
        return bolActive;
    }

    /**
     * @return the maximum allowable number of data sources per decompose request
     */
    public final int getMaxDataSources() {
        return cntSrcsMax;
    }

    /**
     * @return the maximum allowable time duration within a decompose request
     */
    public final Duration getMaxDuration() {
        return durMax;
    }
    

    /**
     * <p>
     * Enables or disables data request decomposition.
     * </p>
     * <p>
     * If data request decomposition is disabled the original request will always be returned from
     * any decomposition operation.  Specifically, the decomposition method will return a list containing
     * a single element, the original request object.
     * </p>
     * 
     * @param bolActive the toggling on/off of request decomposition 
     */
    public final void setActive(boolean bolActive) {
        this.bolActive = bolActive;
    }

    /**
     * <p>
     * Sets the maximum number of allow data sources within a decompose request.
     * </p>
     * <p>
     * Requests will be decomposed so that the number of data sources within a decompose request is always
     * less than or equal to the given value.
     * </p>
     *  
     * @param cntSrcsMax the maximum number of data sources within a decompose request 
     */
    public final void setMaxDataSources(int cntSrcsMax) {
        this.cntSrcsMax = cntSrcsMax;
    }

    /**
     * <p>
     * Sets the maximum time duration within a decompose request.
     * </p>
     * <p>
     * Requests will be decomposed so the the maximum time duration within a decompose request is always
     * less than or equal to the given value.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * Using vertical decomposition can yield unexpected result sets.  Specifically, data buckets can be
     * repeated across decompose requests.  See class documentation for further details.
     * </p>
     * 
     * @param durMax the maximum time duration within a decompose request 
     */
    public final void setMaxDuration(Duration durMax) {
        this.durMax = durMax;
    }

    
    //
    // Request Decomposition
    //
    
//    /**
//     * <p>
//     * Decomposes the given data request into component sub-query requests.
//     * </p>
//     * <p>
//     * Subdivides the query domain of the given source data request to create a set of sub-queries which,
//     * in total, are equivalent to the original request.  That is, the returned set of 
//     * <code>DpDataRequest</code> instances are a decompose of the source data request.
//     * </p>
//     * <p>
//     * Composite queries are useful for large data requests, to increase query performance.  
//     * By subdividing a large request into multiple smaller requests each sub-request can be managed on 
//     * separate concurrent execution threads.  Specifically, multiple gRPC data streams can be
//     * created where each is managed on a separate thread.  Threads concurrently process the 
//     * sub-queries then assemble the results set as it arrives.
//     * </p>
//     * <p>
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li>The source <code>DpDataRequest</code> instance is left unchanged.</li>
//     * <li>All returned <code>DpDataRequest</code> are new, independent request objects.</li>
//     * </ul>
//     * </p> 
//     * 
//     * @param rqst          source data request
//     * @param enmType       the strategy used in query domain decomposition
//     * @param cntQueries    the number of sub-queries in the returned decompose query
//     * 
//     * @return  a collection of sub-queries which, in total, are equivalent to the current data request
//     * 
//     * @see #buildCompositeRequest(DataRequestDecompType, int)
//     */
//    public static List<DpDataRequest>   buildCompositeRequest(DpDataRequest rqst, DataRequestDecompType enmType, int cntQueries) {
//        return rqst.buildCompositeRequest(enmType, cntQueries);
//    }
    
    /**
     * <p>
     * Computes and returns the query domain decomposition parameters for a preferred decomposition. 
     * </p>
     * <p>
     * Computes the query sub-domain parameters for a preferred decompose query.
     * This method computes and returns the parameters used for a query domain decomposition 
     * using the default decomposition parameters specified, but <em>prefers</em> a 
     * horizontal decomposition as horizontal decompositions are typically the most efficient.
     * </p>
     * <p>
     * The method uses the class attributes <code>{@link #cntSrcsMax}</code>
     * and <code>{@link #durMax}</code> to compute the number of query domain
     * sub-divisions using default decomposition.  Specifically, we compute
     * <code>
     * <pre>
     *   cntQueriesHor = {@link #getSourceCount()} / {@link #cntSrcsMax}
     *   cntQueriesVer = {@link #rangeDuration()}.divideBy({@link #durMax})
     * </pre>
     * </code>
     * Note further that if there exists a non-zero remainder for the above integer divisions
     * the domain subdivision count must be incremented to accommodate the excess domain.
     * We perform the following modifications
     * <code>
     * <pre>
     *   cntQueriesHor += ({@link #getSourceCount()} % {@link #cntSrcsMax} == 0) ? 0 : 1
     *   cntQueriesVer += {{@link #rangeDuration()}.minus({@link #durMax}.multiedBy(cntQueryiesVer).isZero) ? 0 : 1
     * </pre>
     * </code>
     * </p>
     * <p>
     * The decompose query type is also determined by the value of the above quantities
     * according whichever condition is found (in order):
     * <ol>
     * <code>
     *   <li>{@link DataRequestDecompType#NONE}       <- (cntQueriesHor == 1) && (cntQueriesVer == 1) </li> 
     *   <li>{@link DataRequestDecompType#HORIZONTAL} <- cntQueriesHor > 1 </li> 
     *   <li>{@link DataRequestDecompType#VERTICAL}   <- cntQueriesVer > 1 </li> 
     *   <li>{@link DataRequestDecompType#GRID}       <- else </li> 
     * </code>
     * </ol>
     * </p>
     * 
     * @param   rqst    the time-series data request to be decomposed
     * 
     * @return  record containing sub-domain parameters when using preferred decomposition
     * 
     * @see DpApiConfig
     * @see DpQueryConfig
     */
    public DataRequestDecompParams decomposeDomainPreferred(DpDataRequest rqst) {

        // Determine decompose query domain sizes
        int     cntQueriesHor = rqst.getSourceCount() / this.cntSrcsMax;
        long    cntQueriesVer = rqst.rangeDuration().dividedBy(this.durMax);


        // Note integer division, must increment count if non-zero remainder
        if (rqst.getSourceCount() % this.cntSrcsMax > 0)
            cntQueriesHor++;
        if (!rqst.rangeDuration().minus(this.durMax.multipliedBy(cntQueriesVer)).isZero())
            cntQueriesVer++;

        // The overall request is not large enough to invoke decomposition
        if ((cntQueriesHor == 1) && (cntQueriesVer == 1)) {

            return new DataRequestDecompParams(DataRequestDecompType.NONE, 1, 1);

            // Horizontal decompose request
        } else if (cntQueriesHor > 1) {
            
            return new DataRequestDecompParams(DataRequestDecompType.HORIZONTAL, cntQueriesHor, 1);

            // Vertical decompose request
        } else if (cntQueriesVer > 1) {

            return new DataRequestDecompParams(DataRequestDecompType.VERTICAL, 1, Long.valueOf(cntQueriesVer).intValue());

            // Grid decompose request
        } else {

            return new DataRequestDecompParams(DataRequestDecompType.GRID, cntQueriesHor, Long.valueOf(cntQueriesVer).intValue());
        }
    }

    /**
     * <p>
     * Computes and returns the query domain decomposition parameters for a default decomposition. 
     * </p>
     * <p>
     * Computes the query sub-domain parameters for a default decompose query.
     * Query domain decomposition parameters are used to build decompose queries.
     * This method computes and returns the parameters used for a query domain decomposition 
     * when using the default decomposition parameters specified in the application default 
     * parameters (i.e., rather than decomposition parameters given by users).
     * </p>
     * <p>
     * The method uses the class attributes <code>{@link #cntSrcsMax}</code>
     * and <code>{@link #durMax}</code> to compute the number of query domain
     * sub-divisions using default decomposition.  Specifically, we compute
     * <code>
     * <pre>
     *   cntQueriesHor = {@link #getSourceCount()} / {@link #cntSrcsMax}
     *   cntQueriesVer = {@link #rangeDuration()}.divideBy({@link #durMax})
     * </pre>
     * </code>
     * Note further that if there exists a non-zero remainder for the above integer divisions
     * the domain subdivision count must be incremented to accommodate the excess domain.
     * We perform the following modifications
     * <code>
     * <pre>
     *   cntQueriesHor += ({@link #getSourceCount()} % {@link #cntSrcsMax} == 0) ? 0 : 1
     *   cntQueriesVer += {{@link #rangeDuration()}.minus({@link #durMax}.multiedBy(cntQueryiesVer).isZero) ? 0 : 1
     * </pre>
     * </code>
     * </p>
     * <p>
     * The decompose query type is also determined by the value of the above quantities
     * according to the following:
     * <code>
     * <pre>
     *   {@link DataRequestDecompType#NONE}       <- (cntQueriesHor == 1) && (cntQueriesVer == 1) 
     *   {@link DataRequestDecompType#HORIZONTAL} <- (cntQueriesHor > 1) && (cntQueriesVer == 1) 
     *   {@link DataRequestDecompType#VERTICAL}   <- (cntQueriesHor == 1) && (cntQueriesVer > 1) 
     *   {@link DataRequestDecompType#GRID}       <- (cntQueriesHor > 1) && (cntQueriesVer > 1) 
     * </pre>
     * </code>
     * </p>
     * 
     * @param   rqst    the time-series data request to be decomposed
     * 
     * @return  record containing sub-domain parameters when using default decomposition
     * 
     * @see DpApiConfig
     * @see DpQueryConfig
     */
    public DataRequestDecompParams decomposeDomainDefault(DpDataRequest rqst) {

        // The decompose query type
        DataRequestDecompType   enmType;

        // Determine decompose query domain sizes
        int     cntQueriesHor = rqst.getSourceCount() / this.cntSrcsMax;
        long    cntQueriesVer = rqst.rangeDuration().dividedBy(this.durMax);


        // Note integer division, must increment count if non-zero remainder
        if (rqst.getSourceCount() % this.cntSrcsMax > 0)
            cntQueriesHor++;
        if (!rqst.rangeDuration().minus(this.durMax.multipliedBy(cntQueriesVer)).isZero())
            cntQueriesVer++;

        // The overall request is not large enough to invoke decomposition
        if ((cntQueriesHor == 1) && (cntQueriesVer == 1)) {

            enmType = DataRequestDecompType.NONE;

            // Horizontal decompose request
        } else if ((cntQueriesHor > 1) && (cntQueriesVer == 1)) {

            enmType = DataRequestDecompType.HORIZONTAL;

            // Vertical decompose request
        } else if ((cntQueriesHor == 1) && (cntQueriesVer > 1)) {

            enmType = DataRequestDecompType.VERTICAL;

            // Grid decompose request
        } else {

            enmType = DataRequestDecompType.GRID;

        }

        return new DataRequestDecompParams(enmType, cntQueriesHor, Long.valueOf(cntQueriesVer).intValue());
    }


    /**
     * <p>
     * Builds and returns a decompose data request based upon the preferred domain decomposition 
     * parameters.
     * <p>
     * <p>
     * Computes the default query domain decomposition using <code>{@link #decomposeDomainPreferred()}</code>
     * then passes the result to <code>{@link #buildCompositeRequestPreferred(DataRequestDecompParams)}</code>.
     * The current data query request instance is left unchanged.
     * </p>
     * 
     * @param   rqst    the time-series data request to be decomposed
     * 
     * @return  an equivalent decompose query request where query domain is decomposed with preferred parameters 
     * 
     * @see #buildCompositeRequestPreferred(DataRequestDecompParams)
     * @see DpQueryConfig
     * @see DpApiConfig
     */
    public List<DpDataRequest> buildCompositeRequestPreferred(DpDataRequest rqst) {
        
        // Get the preferred domain decomposition 
        DataRequestDecompParams recDomains = this.decomposeDomainPreferred(rqst);

        return this.buildCompositeRequest(rqst, recDomains);
    }
    
    /**
     * <p>
     * Builds and returns a decompose data request based upon the query domain decomposition 
     * specified in the argument.
     * </p>
     * <p>
     * Defers to the available <code>buildCompositeRequest(...)</code> methods based upon the
     * contains of the argument.
     * </p>
     * 
     * @param   rqst        the time-series data request to be decomposed
     * @param   recDomains  the query domain decomposition parameters    
     * 
     * @return              an equivalent decompose query request where query domain is 
     *                      decomposed by argument parameters
     *                      
     * @see #buildCompositeRequest(DataRequestDecompType, int)
     * @see #buildCompositeRequestGrid(int, int)                      
     */
    public List<DpDataRequest> buildCompositeRequest(DpDataRequest rqst, DataRequestDecompParams recDomains) {
        
        return switch (recDomains.type()) {
        case NONE -> List.of(rqst);
        case HORIZONTAL -> this.buildCompositeRequest(rqst, recDomains.type(), recDomains.cntHorizontal());
        case VERTICAL -> this.buildCompositeRequest(rqst, recDomains.type(), recDomains.cntVertical());
        case GRID -> this.buildCompositeRequestGrid(rqst, recDomains.cntHorizontal(), recDomains.cntVertical());
        default -> throw new IllegalArgumentException("Unexpected value: " + recDomains.type());
        };
    }
    
    /**
     * <p>
     * Decomposes the current data request into component sub-query requests.
     * </p>
     * <p>
     * Subdivides the query domain of the current data request to create a set of sub-queries which,
     * in total, are equivalent to the current data request.  That is, the returned set of 
     * <code>DpDataRequest</code> instances are a decompose of the current data request.
     * </p>
     * <p>
     * Composite queries are useful for large data requests, to increase query performance.  
     * By subdividing a large request into multiple smaller requests each sub-request can be managed on 
     * separate concurrent execution threads.  Specifically, multiple gRPC data streams can be
     * created where each is managed on a separate thread.  Threads concurrently process the 
     * sub-queries then assemble the results set as it arrives.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>If the argument is odd for a grid domain, the remainder is allocated to the cntVer axis.</li>
     * <li>The current <code>DpDataRequest</code> instance is left unchanged.</li>
     * <li>All returned <code>DpDataRequest</code> are new, independent request objects.</li>
     * </ul>
     * </p> 
     * 
     * @param   rqst        the time-series data request to be decomposed
     * @param   enmType     the strategy used in query domain decomposition
     * @param   cntQueries  the number of sub-queries in the returned decompose query
     * 
     * @return  a collection of decompose queries which, in total, are equivalent to the given data request
     */
    public List<DpDataRequest>  buildCompositeRequest(DpDataRequest rqst, DataRequestDecompType enmType, int cntQueries) {

        return switch (enmType) {
        
        case NONE -> List.of(rqst);
        case HORIZONTAL -> this.decomposeDomainHorizontally(rqst, cntQueries);
        case VERTICAL -> this.decomposeDomainVertically(rqst, cntQueries);
        case GRID -> this.decomposeDomainGridded(rqst, cntQueries);
        default -> throw new IllegalArgumentException("Unexpected value: " + enmType);
        };
    }
    
    /**
     * <p>
     * Decomposes the current data request into component sub-query requests with grid query domain.
     * </p>
     * <p>
     * The query domain is decomposed according to the argument values.
     * <p>
     * Composite queries are useful for large data requests, to increase query performance.  
     * By subdividing a large request into multiple smaller requests each sub-request can be managed on 
     * separate concurrent execution threads.  Specifically, multiple gRPC data streams can be
     * created where each is managed on a separate thread.  Threads concurrently process the 
     * sub-queries then assemble the results set as it arrives.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The current <code>DpDataRequest</code> instance is left unchanged.</li>
     * <li>All returned <code>DpDataRequest</code> are new, independent request objects.</li>
     * </ul>
     * </p> 
     * 
     * 
     * @param   rqst            the time-series data request to be decomposed
     * @param   cntQueriesHor   number of query domain cntHor axis (data source) decompositions 
     * @param   cntQueriesVer   number of query domain cntVer axis (time range) decompositions
     * 
     * @return  a collection of decompose queries which, in total, are equivalent to the given data request
     */
    public List<DpDataRequest>  buildCompositeRequestGrid(DpDataRequest rqst, int cntQueriesHor, int cntQueriesVer) {
        return this.decomposeDomainGridded(rqst, cntQueriesHor, cntQueriesVer);
    }
    

    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates a decompose data query by decomposing the data source domain axes.
     * </p>
     * <p>
     * The query domain cntHor axes is decomposed into the given number of 
     * sub-domains, where a new <code>DpDataRequest</code> instance is created
     * for each sub-domain.  The cntVer (time) axis of each new query object
     * remains the same as the original request.    
     * </p> 
     * <p>
     * The number of data sources is determined by the argument (number of resultant 
     * queries) according to the following formula:
     * <pre>
     *   cntSources = |{sources}| &divide; cntQueries + (|{sources}| % cntQueries > 0) 1 : 0
     * </pre>
     * </p>
     * 
     * @param rqstOrg       the time-series data request to be decomposed
     * @param cntQueries    number of cntHor axis (data source) decompositions
     * 
     * @return  a new list of <code>DpDataRequest</code> instances composing the decompose request
     */
    private List<DpDataRequest> decomposeDomainHorizontally(DpDataRequest rqstOrg, int cntQueries) {
        
        List<DpDataRequest> lstRequests = new LinkedList<>();
        
        // Compute the maximum number of sources per query
        int cntSources = rqstOrg.getSourceCount() / cntQueries;
        int intRemainder = rqstOrg.getSourceCount() % cntQueries;
        
        if (intRemainder > 0) 
            cntSources++;
        
        // Create decompose data requests by bucketing data source names
        int indRqstStart = 0;
        
        for (int n=0; n<cntQueries; n++) {
            int indRqstStop = indRqstStart + cntSources;
            
            // If this is the last time through we need to reduce stop index
            if (indRqstStop > rqstOrg.getSourceCount())
                indRqstStop = rqstOrg.getSourceCount();
            
            List<String>    lstRqstSrcs = rqstOrg.getSourceNames().subList(indRqstStart, indRqstStop);
            
            DpDataRequest rqstCmp = DpDataRequest.from(rqstOrg.getStreamType(), rqstOrg.getInitialTime(), rqstOrg.getFinalTime(), lstRqstSrcs);
            
            lstRequests.add(rqstCmp);
            
            indRqstStart = indRqstStop;
        }
        
        return lstRequests;
    }
    
    /**
     * <p>
     * Creates a decompose data query request by decomposing the time domain axis.
     * </p>
     * <p>
     * The query domain cntVer axes is decomposed into the given number of 
     * sub-domains, where a new <code>DpDataRequest</code> instance is created
     * for each sub-domain.  The cntHor (data source) axis of each new query object
     * remains the same as the original request.    
     * </p> 
     * <p>
     * The duration of each component request is determined by the argument (number of 
     * resultant queries) according to the following formula:
     * <pre>
     *   durRequest = durTotal &divide; cntQueries + (durTotal - durRequset * cntQueries)
     * </pre>
     * Note that due to integer arithmetic the largest remainder can be only 1 nanosecond.
     * </p>
     * 
     * @param rqstOrg       the time-series data request to be decomposed
     * @param cntQueries    number of cntVer axis (time range) decompositions
     * 
     * @return  a new list of <code>DpDataRequest</code> instances composing the decompose request
     */
    private List<DpDataRequest> decomposeDomainVertically(DpDataRequest rqstOrg, int cntQueries) {
        
        List<DpDataRequest> lstRequests = new LinkedList<>();
        
        // Compute the time duration for each request
        Duration    durTotal = rqstOrg.rangeDuration();
        Duration    durRequest = durTotal.dividedBy(cntQueries);
        Duration    durRemain = durTotal.minus(durRequest.multipliedBy(cntQueries));
        
        // Create decompose queries by divide time range interval
        Instant insRqstTmStart = rqstOrg.getInitialTime();
        
        for (int n=0; n<cntQueries; n++) {
            Instant insRqstTmStop = insRqstTmStart.plus(durRequest);
            
            if (n == (cntQueries - 1))
                insRqstTmStop = insRqstTmStop.plus(durRemain);
            
            DpDataRequest   rqstCmp = DpDataRequest.from(rqstOrg.getStreamType(), insRqstTmStart, insRqstTmStop, rqstOrg.getSourceNames());
            
            lstRequests.add(rqstCmp);
            
            insRqstTmStart = insRqstTmStop;
        }
        
        return lstRequests;
    }
    
    /**
     * <p>
     * Creates a decompose data query request by decomposing both the data source and time domain axes.
     * </p>
     * <p>
     * The query domain cntHor axes is decomposed into <code>cntQueriesHor</code> 
     * sub-domains and the query domain cntVer axes is decomposed into 
     * <code>cntQueriesVer</code> sub-domains.  Thus, the total query domain is divided
     * into a grid, where the total number of domains is given by the product. 
     * </p> 
     * <p>
     * The domain of each component request is determined by the argument values 
     * according to the following formula:
     * <pre>
     *   cntSources = |{sources}| &divide; cntQueriesHor + (|{sources}| % cntQueriesHor > 0) 1 : 0
     *   durRequest = durTotal &divide; cntQueriesVer + (durTotal - durRequset * cntQueriesVer)
     * </pre>
     * Note that due to integer arithmetic the largest remainder can be only 1 nanosecond.
     * </p>
     * 
     * @param rqstOrg       the time-series data request to be decomposed
     * @param cntQueriesHor number of cntHor axis (data source) decompositions
     * @param cntQueriesVer number of cntVer axis (time range) decompositions
     * 
     * @return  a new list of <code>DpDataRequest</code> instances composing the decompose request
     */
    private List<DpDataRequest> decomposeDomainGridded(DpDataRequest rqstOrg, int cntQueriesHor, int cntQueriesVer) {
        
        List<DpDataRequest> lstRequests = new LinkedList<>();
        
        // Compute the sizes for each component query
        //  Horizontal axis - number of sources/query
        int cntSources = rqstOrg.getSourceCount() / cntQueriesHor;
        
        if (rqstOrg.getSourceCount() % cntQueriesHor > 0)
            cntSources++;
        
        //  Vertical axis - duration of sub-queries
        Duration    durTotal = rqstOrg.rangeDuration();
        Duration    durRequest = durTotal.dividedBy(cntQueriesVer);
        Duration    durRemain = durTotal.minus(durRequest.multipliedBy(cntQueriesVer));
        
        // Create component requests for grid
        //  Initialize loops
        int     indRqstSrcStart = 0;
        Instant insRqstTmStart = rqstOrg.getInitialTime();
        
        //      Horizontal (source) axis
        for (int m=0; m<cntQueriesHor; m++) {
            
            // Get the last source index for sublist
            int indRqstSrcStop = indRqstSrcStart + cntSources;
            if (indRqstSrcStop > rqstOrg.getSourceCount())
                indRqstSrcStop = rqstOrg.getSourceCount();
            
            List<String>    lstRqstSrcs = rqstOrg.getSourceNames().subList(indRqstSrcStart, indRqstSrcStop);
            
            //  Vertical (time) axis
            for (int n=0; n<cntQueriesVer; n++) {
                Instant insRqstTmStop = insRqstTmStart.plus(durRequest);
                
                // Add in remainder (1 nanosecond at most)
                if (n == (cntQueriesVer - 1))
                    insRqstTmStop = insRqstTmStop.plus(durRemain);
                
                DpDataRequest   rqstCmp = DpDataRequest.from(rqstOrg.getStreamType(), insRqstTmStart, insRqstTmStop, lstRqstSrcs);
                
                lstRequests.add(rqstCmp);
                
                insRqstTmStart = insRqstTmStop;
            }
            
            // (Re)set loop variables
            insRqstTmStart = rqstOrg.getInitialTime();
            indRqstSrcStart = indRqstSrcStop;
        }
        
        return lstRequests;
    }

    /**
     * <p>
     * Creates a decompose data query request by decomposing both the data source and time domain axes.
     * </p>
     * <p>
     * The grid sub-domain is decomposed horizontally and vertically equally by the argument
     * value divided by 2.  If the argument is odd, then the time axis receives the remainder.
     * Thus, the result of this method is equivalent to call the method
     * <code>
     * <pre>
     *      decomposeDomainGridded(cntQueries/2, cntQueries/2 + cntQueries%2);
     * </pre>
     * </code>
     * </p>
     *  
     * @param rqstOrg       the time-series data request to be decomposed
     * @param cntQueries    total number of queries returned
     * 
     * @return  a new list of <code>DpDataRequest</code> instances composing the decompose request
     * 
     * @see #decomposeDomainGridded(int, int)
     */
    private List<DpDataRequest> decomposeDomainGridded(DpDataRequest rqstOrg, int cntQueries) {
        
        // Compute the number of queries in each axes
        int cntQueriesPerAxis = cntQueries / 2;
        int cntQueriesHor = cntQueriesPerAxis;
        int cntQueriesVer = cntQueriesPerAxis;
        
        // Add any remainder to the cntVer (time) axis
        if (cntQueries % 2 > 0) 
            cntQueriesVer = cntQueriesPerAxis + 1;

        return this.decomposeDomainGridded(rqstOrg, cntQueriesHor, cntQueriesVer);
    }
}
