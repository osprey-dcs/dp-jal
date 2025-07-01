/*
 * Project: dp-api-common
 * File:	DpDataRequest.java
 * Package: com.ospreydcs.dp.api.query
 * Type: 	DpDataRequest
 *
 * Copyright 2010-2022 the original author or authors.
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
 * @since Sep 23, 2022
 * @version Jan 14, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.common.TimeInterval;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.ingest.IIngestionService;
import com.ospreydcs.dp.api.ingest.IIngestionStream;
import com.ospreydcs.dp.api.model.AAdvancedApi;
import com.ospreydcs.dp.api.model.AUnavailable;
import com.ospreydcs.dp.api.model.AUnavailable.STATUS;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.DataValue;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest.QuerySpec;


/**
 * <p>
 * Builder class for constructing time-series data requests for the Data Platform <em>Query Service</em>.
 * </p>
 * <p>
 * This class is used to create Query Service data requests without direct knowledge
 * of the underlying query mechanism.  The class exposes a set of methods that 
 * can be called to create a specific time-series data request.  In this fashion the
 * query service interface can be narrowed while still providing a wide range
 * of possible queries.  
 * It also simplifies the use of the query service in that
 * the user is not subject to details of the underlying Query Service gRPC interface. 
 * Additionally, any modifications to the Query Service gRPC API can be hidden by this
 * class presenting a consistent interface to clients.
 * </p>
 * <p>
 * There are 4 categories of methods used to create the data query requests:
 * <ol>
 * <li>Set methods - prefixed with <code>set</code>: typically used to set data request
 *     metadata.  This is data this is not part of the actual query but does affect
 *     the query process.  Typically, they are performance parameters.
 *     </li>
 * <li>Selection methods - prefixed with <code>select</code>: used to select the
 *     data sources (returned data columns) within the query results.
 *     </li>
 * <li>Ranges methods - prefixed with <code>range</code>: used to restrict the time range
 *     of timestamps within the query results.
 *     </li>
 * <li>Filter methods - prefixed with <code>filter</code>: used to filter the query
 *     results based upon filter conditions.  These methods are currently unavailable.
 *     </li>
 * </ol>
 * Further details on the use of these methods are given in the <b>NOTES</b> 
 * subsection below.  Also see the method documentation for more particulars on
 * their use.  Note that the page methods are only for asynchronous requests
 * and their use is somewhat advanced.  Reasonable default values are
 * specified in the application properties, which are used whenever the methods
 * are not called.
 * </p>
 * <p>
 * It is important to note that an unconfigured data request, that is, the request 
 * specified immediately upon instance creation, always represents the "empty query."  
 * Specifically, a newly created <code>DpDataRequest</code> instance always
 * selects for no time-series data within the <em>Data Platform </em>.  By calling
 * the methods listed above, the resulting query request is "opened", either
 * by selecting the specific data sources to be included (e.g., EPICS process variables (PVs)), 
 * and by setting the range of allowable timestamps.
 * A query can be reduced in scope using the "restrictor" methods, such as filtering operations 
 * on the data values, or other attributes of the query.
 * </p>    
 * <p> 
 * Once a class instance is configured by the client using the available selector, range, and filter
 * method calls, a <em>Query Service </em> Protobuf message containing the request
 * can be created using the public <code>{@link #buildQueryRequest()}</code> method.
 * The method packages the client-configuration into the appropriate gRPC message type 
 * <code>{@link QueryRequest}</code> used by the Query Service gRPC interface.
 * The method is not typically required of general clients but is left as public access
 * for those desiring detailed interaction with the Query Service.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li> 
 * Call the {@link #create()} method to return a <em>Query Service</em> data 
 * request initialized to the "empty request" query.
 * </li>
 * <li> 
 * A new <code>DpDataRequest</code> instance will create the empty query, that is, it creates
 * the query which returns the empty results set.
 * </li>
 * <li> 
 * The first call to a <code>select</code> method sets the query to given data source names or
 * special columns.
 * However, further calls to <code>select</code> methods opens the query to additional
 * data source names.
 * </li>
 * <li> 
 * Calling a <code>range</code> method always further restricts the time range
 * of the final query according to the additional clauses.  Multiple time range
 * calls may be considered as interval "intersection" restrictions. 
 * </li>
 * <li>
 * Calling a <code>filter</code> method filters the results of the query according
 * to the filter specification.  A filter method invocation always further 
 * restricts query results to those that match the additional filter condition.
 * </li> 
 * <li>
 * This API component is still under development and is subject to change where indicated.
 * Note the method and type annotations by <code>{@link AAdvancedApi}</code> and 
 * <code>{@link AUnavailable}</code>.
 * </li>
 * 
 * <h2>The following are not currently applicable:</h2>
 * <li> 
 * Call the <code>{@link #setPageSize(Integer)}</code> method to set the size
 * (i.e., number of data rows) of the data pages returned from the <em>Query Service</em>
 * during asynchronous data streaming.  <em>This is a performance parameter</em>.  
 * This value defaults to that in the <code>AppProperties</code> configuration.
 * </li>
 * <li>
 * Call the <code>{@link #setPageStartIndex(Integer)}</code> to set the index of
 * the first data page returned by the <em>Query Service</em>.  
 * <em>Page indices are 1-based</em>, (i.e., start with index 1).
 * Setting this value to 0 instructs the <em>Query Service</em> to
 * stream all data pages for the given query request (this is the default value).
 * <b>Use at your own risk</b> as setting this value
 * will produce a resultant query request that returns only the tail of
 * the full query (from given index to the last data page).
 * </ul>
 * </p>
 * <p>
 * <h2>TODO</h2>
 * <ul>
 * <li>
 * Decide on the state of the initial query.  That is, the "open query", the "empty query", etc.
 * </li>
 * <li>
 * Figure out where the filter options go.
 * </li>
 * <li>
 * Clean this thing up!
 * </li>
 * </ul>
 *
 * @author Christopher K. Allen
 * @since Sep 23, 2022
 * @version Jan 27, 2024
 *
 * @see AAdvancedApi
 * @see AUnavailable
 */
@AAdvancedApi(status=AAdvancedApi.STATUS.DEVELOPMENT, note="DP Query Service gRPC API is still under development")
public final class DpDataRequest {

    
    //
    // Creators 
    //
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for creating time-series
     * data requests from the <em>Query Service</em>.
     * </p>
     * <p>
     * Note that the returned data request will create the 
     * "open query", which requests all time-series data currently within the 
     * <em>Data Platform</em> data archive since its inception.
     * Use the "selection" and "restrictor" methods to narrow the data request
     * results based upon specific PV names, timestamps ranges, and other 
     * data filters.
     * </p>
     * <h3>The following are not currently applicable:</h3>
     * <p>
     * Use the <code>{@link #setPageSize(Integer)}</code> to performance tweak
     * the size of the data pages in the <em>Query Service</em> data stream when
     * using paged data tables (in particular, for asynchronous data requests).
     * The default page size is given in the <i>application.yml</i> file.
     * </p>
     * <p>
     * Use the <code>{@link #setPageStartIndex(Integer)}</code> tell the 
     * <em>Query Service</em> to send the specific page index for the given
     * query.  For asynchronously streamed data (<code>QueryRequest</code>) 
     * this instructs the <em>Query Service</em> to send data pages from the given 
     * index on to the last page index.
     * <br/> <br/>
     * &nbsp; &nbsp; <i>setting this value </i>> 1<i> sends only the tail of the data request.</i> 
     * of the full request.
     * <br/><br/>
     * Thus, <b>use at your own risk</b>.
     * </p> 
     * 
     * @return  a new "open query" <code>DpDataRequest</code> instance
     */
    public static DpDataRequest create() {
        DpDataRequest bldr = new DpDataRequest();
        
        return bldr;
    }
    
    /**
     * <p>
     * Creates a new, initialized <code>DpDataRequest</code> instance defining a time-series data requests 
     * from the <em>Query Service</em>.
     * </p>
     * <p>
     * Note that the returned data request is defined by the given parameters. 
     * The returned request can be further modified using the "selection" and "restrictor" methods to narrow 
     * the returned data request based upon specific PV names, timestamps ranges, and other data filters.
     * </p>
     * <h2>NOTES:</h2>
     * <p>
     * Invocation of this method for time-series data request creation assumes familiarity with the internal
     * structure and operation of the <code>DpDataRequest</code> class.
     * Thus, <b>use at your own risk</b>.
     * </p>
     *  
     * @param strId         the request identifier used by Java API Library
     * @param enmStreamType the preferred gRPC stream type for the  
     * @param insStart      the start time of the data request
     * @param insEnd        the final time of the data request
     * @param lstPvNames    the data source names (i.e., process variable names) of the data request
     * 
     * @return  a new Query Service data request initialized with the given arguments
     * 
     * @throws  IllegalArgumentException    the given stream type is out of context
     */
    public static DpDataRequest from(String strId, DpGrpcStreamType enmStreamType, Instant insStart, Instant insEnd, List<String> lstPvNames) 
            throws IllegalArgumentException {
        
        return new DpDataRequest(strId, enmStreamType, insStart, insEnd, lstPvNames);
    }
    
    /**
     * <p>
     * Creates a new, initialized <code>DpDataRequest</code> instance defining a time-series data requests 
     * from the <em>Query Service</em>.
     * </p>
     * <p>
     * Note that the returned data request is defined by the given parameters. 
     * The returned request can be further modified using the "selection" and "restrictor" methods to narrow 
     * the returned data request based upon specific PV names, timestamps ranges, and other data filters.
     * </p>
     * <h2>NOTES:</h2>
     * <p>
     * <ul>
     * <li>
     * The request identifier parameter is set to <code>null</code>.  This parameter is used only by the 
     * Java API Library and essentially means the request is unnamed.
     * </li>
     * <li>
     * Invocation of this method for time-series data request creation assumes familiarity with the internal
     * structure and operation of the <code>DpDataRequest</code> class.
     * Thus, <b>use at your own risk</b>.
     * </li>
     * </ul>
     * </p>
     *  
     * @param enmStreamType the preferred gRPC stream type for the  
     * @param insStart      the start time of the data request
     * @param insEnd        the final time of the data request
     * @param lstPvNames    the data source names (i.e., process variable names) of the data request
     * 
     * @return  a new Query Service data request initialized with the given arguments
     * 
     * @throws  IllegalArgumentException    the given stream type is out of context
     */
    public static DpDataRequest from(DpGrpcStreamType enmStreamType, Instant insStart, Instant insEnd, List<String> lstPvNames) 
            throws IllegalArgumentException {
        
        return new DpDataRequest(null, enmStreamType, insStart, insEnd, lstPvNames);
    }
    

    //
    //  Application Resources
    //
    
    /** The default API library configuration */
    private static final DpApiConfig CFG_DEFAULT = DpApiConfig.getInstance();
    
    
    //
    // Class Types
    //
    
    
    //
    // Class Constants
    //
    
    /** The default page size to use when creating gRPC paginated queries */
    @Deprecated
    private static final Integer                SZ_PAGES = CFG_DEFAULT.query.pageSize;
    
    
    /** gRPC stream type default preference */
    private static final DpGrpcStreamType       ENM_STREAM_PREF = CFG_DEFAULT.query.data.request.stream.preference;
    
    
    /** The start of this time epoch (Jan 1, 1970) */
    private static final Instant                INS_EPOCH = Instant.EPOCH;
    
    /** Inception instant of the Data Platform archive */
    private static final Instant                INS_INCEPT = Instant.parse( CFG_DEFAULT.archive.inception );
    
    
//    /** Use decompose queries feature */
//    private static final boolean                BOL_COMPOSITE = CFG_DEFAULT.query.data.request.decompose.active;
//    
//    /** Maximum number of data sources for a decompose query */
//    private static final int                    CNT_COMPOSITE_MAX_SOURCES = CFG_DEFAULT.query.data.request.decompose.maxSources;
//    
//    /** Maximum time range duration for a decompose query */
//    private static final long                   LNG_COMPOSITE_MAX_DURATION = CFG_DEFAULT.query.dataRequest.composite.maxDuration;
//    
//    /** Time units for the maximum range duration in a decompose query */
//    private static final TimeUnit               TU_COMPOSITE_MAX_DURATION = CFG_DEFAULT.query.dataRequest.composite.unitDuration;
//    
//    /** Maximum time range duration for a decompose query */
//    private static final Duration               DUR_COMPOSITE_MAX = Duration.of(
//                                                    CFG_DEFAULT.query.data.request.decompose.maxDuration,
//                                                    CFG_DEFAULT.query.data.request.decompose.unitDuration.toChronoUnit()
//                                                    );
    
    
    //
    // Instance Attributes
    //
    
    
    /** The size of a data page, that is, the number of data rows per page */
    @Deprecated
    @SuppressWarnings("unused")
    private int szPage = SZ_PAGES;
    
    /** The initial index of first page to return, that is, the starting page number */
    @Deprecated
    @SuppressWarnings("unused")
    private int indStartPage = 0;
    
    /** Optional request identifier or name */
    private String              strRqstId = null;

    /** Optional gRPC stream type */
    private DpGrpcStreamType    enmStrmType = ENM_STREAM_PREF;
    
    /** The time range start instant */
    private Instant             insStart = INS_INCEPT;
    
    /** The time range stop instant */
    private Instant             insStop = Instant.now();
    

    //
    // Instance Resources
    //
    
    /** "SELECT" component of the query - identifies data source names and attributes */
    private final LinkedList<String>  lstSelCmps = new LinkedList<String>();
    
    /** "WHERE" components of the query - data filters to restrict time interval  */
    private final LinkedList<String>  lstWhrCmps = new LinkedList<String>();
    
    
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
//     * @see #buildCompositeRequest(RequestDecompType, int)
//     */
//    public static List<DpDataRequest>   buildCompositeRequest(DpDataRequest rqst, RequestDecompType enmType, int cntQueries) {
//        return rqst.buildCompositeRequest(enmType, cntQueries);
//    }
//    
//    
//    //
//    // Composite Requests
//    //
//    
//    /**
//     * <p>
//     * Builds and returns a decompose data request based upon the default domain decomposition 
//     * parameters.
//     * <p>
//     * <p>
//     * Computes the default query domain decomposition using <code>{@link #decomposeDomainDefault()}</code>
//     * then passes the result to <code>{@link #buildCompositeRequest(RequestDecompParams)}</code>.
//     * The current data query request instance is left unchanged.
//     * </p>
//     * 
//     * @return  an equivalent decompose query request where query domain is decomposed with default parameters 
//     * 
//     * @see #buildCompositeRequest(RequestDecompParams)
//     * @see DpQueryConfig
//     * @see DpApiConfig
//     */
//    public List<DpDataRequest> buildCompositeRequest() {
//        
//        // Get the default domain decomposition 
//        RequestDecompParams recDomains = this.decomposeDomainDefault();
//
//        return this.buildCompositeRequest(recDomains);
//    }
//    
//    /**
//     * <p>
//     * Builds and returns a decompose data request based upon the query domain decomposition 
//     * specified in the argument.
//     * </p>
//     * <p>
//     * Defers to the available <code>buildCompositeRequest(...)</code> methods based upon the
//     * contains of the argument.
//     * </p>
//     * 
//     * @param recDomains    the query domain decomposition parameters    
//     * 
//     * @return              an equivalent decompose query request where query domain is 
//     *                      decomposed by argument parameters
//     *                      
//     * @see #buildCompositeRequest(RequestDecompType, int)
//     * @see #buildCompositeRequestGrid(int, int)                      
//     */
//    public List<DpDataRequest> buildCompositeRequest(RequestDecompParams recDomains) {
//        
//        return switch (recDomains.type()) {
//        case NONE -> List.of(this);
//        case HORIZONTAL -> this.buildCompositeRequest(recDomains.type(), recDomains.cntHorizontal());
//        case VERTICAL -> this.buildCompositeRequest(recDomains.type(), recDomains.cntVertical());
//        case GRID -> this.buildCompositeRequestGrid(recDomains.cntHorizontal(), recDomains.cntVertical());
//        default -> throw new IllegalArgumentException("Unexpected value: " + recDomains.type());
//        };
//    }
//    
//    /**
//     * <p>
//     * Decomposes the current data request into component sub-query requests.
//     * </p>
//     * <p>
//     * Subdivides the query domain of the current data request to create a set of sub-queries which,
//     * in total, are equivalent to the current data request.  That is, the returned set of 
//     * <code>DpDataRequest</code> instances are a decompose of the current data request.
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
//     * <li>If the argument is odd for a grid domain, the remainder is allocated to the cntVer axis.</li>
//     * <li>The current <code>DpDataRequest</code> instance is left unchanged.</li>
//     * <li>All returned <code>DpDataRequest</code> are new, independent request objects.</li>
//     * </ul>
//     * </p> 
//     * 
//     * @param enmType       the strategy used in query domain decomposition
//     * @param cntQueries    the number of sub-queries in the returned decompose query
//     * 
//     * @return  a collection of sub-queries which, in total, are equivalent to the current data request
//     */
//    public List<DpDataRequest>  buildCompositeRequest(RequestDecompType enmType, int cntQueries) {
//
//        return switch (enmType) {
//        
//        case HORIZONTAL -> this.decomposeDomainHorizontally(cntQueries);
//        case VERTICAL -> this.decomposeDomainVertically(cntQueries);
//        case GRID -> this.decomposeDomainGridded(cntQueries);
//        case NONE -> List.of(this);
//        default -> throw new IllegalArgumentException("Unexpected value: " + enmType);
//        };
//    }
//    
//    /**
//     * <p>
//     * Decomposes the current data request into component sub-query requests with grid query domain.
//     * </p>
//     * <p>
//     * The query domain is decomposed according to the argument values.
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
//     * <li>The current <code>DpDataRequest</code> instance is left unchanged.</li>
//     * <li>All returned <code>DpDataRequest</code> are new, independent request objects.</li>
//     * </ul>
//     * </p> 
//     * 
//     * 
//     * @param cntQueriesHor number of query domain cntHor axis (data source) decompositions 
//     * @param cntQueriesVer number of query domain cntVer axis (time range) decompositions
//     * @return
//     */
//    public List<DpDataRequest>  buildCompositeRequestGrid(int cntQueriesHor, int cntQueriesVer) {
//        return this.decomposeDomainGridded(cntQueriesHor, cntQueriesVer);
//    }
//    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Returns the query builder to its initial state.  
     * </p>
     * <p>
     * <ul>
     * <li>Sets preferred gRPC stream type back to default preference.</li>
     * <li>Sets the start time to {@link #INS_INCEPT}
     * <li>Clears all previous calls to selectors, range, and restrictors.</li>
     * <li>Sets the page size to the application default (in properties).</li>
     * <li>Sets the start page index to 0 (send all data pages).</li>
     * </ul>
     */
    public void reset() {
//        this.bolCursor = false;
        this.indStartPage = 0;
        this.szPage = SZ_PAGES;

        this.strRqstId = null;
        this.enmStrmType = ENM_STREAM_PREF;
        this.insStart = INS_INCEPT;
        this.insStop = INS_INCEPT;
//        this.insStop = Instant.now();
        this.lstSelCmps.clear();
        
        this.lstWhrCmps.clear();
    }
    
    /**
     * <p>
     * Creates a shallow copy of the current <code>DpDataRequest</code> instance.
     * </p>
     * 
     * @return  a copy of the current request
     *
     * @see java.lang.Object#clone()
     */
    public DpDataRequest    clone() {
        return new DpDataRequest(this.strRqstId, this.enmStrmType, this.insStart, this.insStop, new LinkedList<String>(this.lstSelCmps));
    }
    
    /**
     * <p>
     * Creates a <em>Query Service</em> <code>QueryRequest</code> Protobuf message
     * based upon the history of calls to the selection/restrictor methods.
     * </p>
     * <p>
     * Note that the returned <code>QueryRequest</code> Protobuf message can be used to initiate a
     * gRPC data stream.  Specifically, the returned object contains the <code>QuerySpec</code>
     * Protobuf message containing the full data request.
     * Typically, when a gRPC data stream is initiate with the <em>Query Service</em> 
     * a separate thread is spawned and requested data is acquired in <i>pages</i>. 
     * </p>
     * <p>  
     * <h2>NOTES:</h2>
     * <ul>
     * <li> If no <code>range</code> methods were called then the returned query
     * defaults to the inception time defined in the applications properties.
     * </li>
     * <li> If no <code>select</code> methods were called then the returned 
     * query defaults to all PV names in the <em>Query Service</em>.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  properly formatted <em>Query Service</em> DQL gRPC request message
     * 
     * @see DpDataRequest#buildRequest()
     * @see DpDataRequest#buildDqlQueryString()
     */
    public QueryDataRequest buildQueryRequest() {
        
        // Create the query specification (the request object)
        QuerySpec.Builder bldrQry = QuerySpec.newBuilder();
        bldrQry.setBeginTime( ProtoMsg.from(this.insStart) );
        bldrQry.setEndTime( ProtoMsg.from( this.insStop) );
        bldrQry.addAllPvNames(this.lstSelCmps);
        QuerySpec msgQry = bldrQry.build();
        
        // Create a query request from the query specification
        QueryDataRequest.Builder bldrRqst = QueryDataRequest.newBuilder();
        bldrRqst.setQuerySpec(msgQry);
        
        QueryDataRequest msgRqst = bldrRqst.build();
        
        return msgRqst;
    }
    
//    /**
//     * <p>
//     * Creates a list of Query Service <code>QueryRequest</code> message for a decompose query.
//     * </p>
//     * <p>
//     * If the <code>{@link #BOL_COMPOSITE}</code> decompose request flag is <code>true</code>,
//     * the current <code>DpDataRequest</code> instance is decomposed into a decompose request.
//     * The query domain is decomposed according to the decompose query parameters within
//     * the default configuration (@see <code>{@link DpApiConfig}</code>). 
//     * Otherwise the method returns a list of one <code>QueryRequest</code> object, that 
//     * obtained from <code>{@link #buildQueryRequest()}</code>
//     * </p>
//     * <p>
//     * Composite queries are useful for large data requests, to increase query performance.  
//     * By subdividing a large request into multiple smaller requests each sub-request can be managed on 
//     * separate concurrent execution threads.  Specifically, multiple gRPC data streams can be
//     * created where each is managed on a separate thread.  Threads concurrently process the 
//     * sub-queries then assemble the results set as it arrives.
//     * </p>
//     * <p>
//     * Here the decompose request has a query domain decomposition process that is predetermined
//     * by the Query Service default configuration parameters.  Users specify their own query
//     * domain decompositions using the available <code>buildCompositeRequest(...) methods.
//     * </p>
//     * 
//     * @return  a list of <code>QueryRequest</code> objects using default query domain decomposition
//     * 
//     * @see #buildQueryRequest()
//     * @see #buildCompositeRequest()
//     * @see DpQueryConfig
//     * @see DpApiConfig
//     */
//    public List<QueryDataRequest> buildQueryRequests() {
//        
//        // If decompose requests are turned off return single request
//        if (!BOL_COMPOSITE) {
//            return List.of( this.buildQueryRequest() );
//        }
//
//        // Compute the default decompose data request
//        List<DpDataRequest> lstRequests = this.buildCompositeRequest(); 
//        
//        // Convert to Query Service request messages and return
//        return lstRequests.stream()
//                .map(rqst -> rqst.buildQueryRequest())
//                .toList();
//    }
    
    
    //
    // Query Properties and Metadata
    //
    
    /**
     * <p>
     * Returns the (optional) request identifier.
     * </p>
     * <p>
     * The time-series data request identifier is an additional property to identify request
     * data.  It is used solely by the Java API Library; that is, it is not a property of the
     * Data Platform Query Service.
     * </p>
     * <p>
     * Typically, the request ID appears within the final tabular result set once the request is
     * fully recovered and processed by the API library.  Thus, no special characteristics are 
     * required of the ID and a string name is typically sufficient.
     * </p>
     *   
     * @return  the (optional) request identifier
     */
    public String   getRequestId() {
        return this.strRqstId;
    }
    
    /**
     * <p>
     * Returns the preferred gRPC stream type used to perform the request.
     * </p>
     * <p>
     * Note that this value is only applicable if a streaming RPC operation is 
     * used to recover the query results set.  Even then, the choice of preferred
     * gRPC data stream type is not guaranteed.
     * </p>
     * 
     * @return  the preferred gRPC data stream type for the request operation
     */
    public DpGrpcStreamType   getStreamType() {
        return this.enmStrmType;
    }
    /**
     * <p>
     * Returns the total number of data sources identified in this data request.
     * </p>
     * <p>
     * Returns the number of data source names selected within the query.  That is, the
     * size of the "cntHor" query domain.
     * </p>
     * 
     * @return  number of data source names within the current query configuration
     */
    public int  getSourceCount() {
        return this.lstSelCmps.size();
    }
    
    /**
     * <p>
     * Return a list of all data sources currently specified by the data request.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Do not make critical use of this method at this time.
     * <ul>
     * <li>Currently regular expressions are not supported.</li>
     * <li>Due to the above condition the returned list is complete.</li>
     * <li>If regular expressions are supported in the future this method may be modified or removed altogether.</li>
     * </ul>
     * <p>
     * 
     * @return  (unordered) list of all data source names in the current data request configuration
     */
    public final List<String>   getSourceNames() {
        return this.lstSelCmps;
    }
    
    /**
     * <p>
     * Returns the initial time instant within the current time-series data request.
     * </p>
     * <p>
     * The returned value is the greatest lower bound end-point for the time interval describing
     * the time-series data request.  Note that this value changes according to invocations of
     * the <code>range</code> methods.
     * </p>
     *   
     * @return  initial time instant within the current time-series data request
     */
    public final Instant    getInitialTime() {
        return this.insStart;
    }
    
    /**
     * <p>
     * Returns the final time instant within the current time-series data request.
     * </p>
     * <p>
     * The returned value is the least upper bound end-point for the time interval describing
     * the time-series data request.  Note that this value changes according to invocations of
     * the <code>range</code> methods.
     * </p>
     *   
     * @return  final time instant within the current time-series data request
     */
    public final Instant    getFinalTime() {
        return this.insStop;
    }
    
    /**
     * <p>
     * Computes and returns the current range of the data request.
     * </p>
     * <p>
     * Computes the time domain interval for the current data request configuration.  That is, the "vertical"
     * domain of the current query.
     * </p>
     *  
     * @return  query time domain currently specified by data request
     */
    public TimeInterval range() {
        return TimeInterval.from(this.insStart, this.insStop);
    }
    
    /**
     * <p>
     * Computes and returns the time duration of this data request.
     * </p>
     * <p>
     * Computes and returns the total time duration for the range of the current data 
     * request.  That is, the size of the "vertical" query domain.
     * </p>
     *  
     * @return  duration of the time range within the current query configuration
     */
    public Duration rangeDuration() {
        Duration    durQuery = Duration.between(this.insStart, this.insStop);
        
        return durQuery;
    }
    
    /**
     * <p>
     * Computes and returns an approximation for the current query domain size
     * (in source count-seconds).
     * </p>
     * <p>
     * Computes the current "area" of the query domain represented by this request.
     * The returned value is in the units of "(data source count)-seconds."  The value
     * provides a rough estimate of the size of the results set for the data request.
     * </p>  
     * <p>
     * For example, suppose all data sources within the current request produced time-series
     * data at a sampling rate of <i>f</i> = 1 kHz, or 1,000 samples/second.  Then multiplying 
     * the returned value with the sampling rate produces the total number of samples in the
     * results set of the data request.  Specifically, let <i>A</i> denote the returned value,
     * <i>f</i> the sampling rate, and <i>N</i> the total number of samples in the data request
     * results set, then
     * <pre>  
     *     <i>N</i> (samples) &approx; <i>f</i> (samples/second) * <i>A</i> (source count-seconds)
     *                 &approx; <i>f</i><i>A</i> (samples)
     * </pre>  
     * since the "source count" dimension is without units.
     * Again this is an approximation, only accurate if ALL requested data sources have the
     * same sample rate <i>f</i>.
     * </p>
     * 
     * @return  the current query domain size in (data source count)-(seconds)
     */
    public long approxDomainSize() {
        // Compute the time dimension - the duration of the time range in units seconds 
        Duration    durRange = this.rangeDuration();
        long        lngRangeSize = TimeUnit.SECONDS.convert(durRange);
        
        // Compute the data source dimension
        long        cntDataSources = this.getSourceCount();
        
        // Return the product
        long        lngDomainSize = cntDataSources * lngRangeSize;
        
        return lngDomainSize;
    }
    
    /**
     * <p>
     * Computes and returns an approximation for the number of samples in the current 
     * data request results set.
     * </p>
     * <p>
     * Computes an approximation for the size of the results set <i>N</i> in units of "samples".
     * The returned value <i>N</i> is essentially the equivalent of multiply the inverse of 
     * sampling period <i>T</i> (i.e., the argument) by the value obtained from
     * <code>{@link #approxDomainSize()}</code>.  However, the method of computation
     * differs in order to better accommodate integer arithmetic.
     * </p> 
     * <p>
     * The returned value <i>N</i> is in the units of "samples."  The returned value is
     * accurate whenever all the data sources identified in the data request are sampled
     * at the given period.
     * For example, if all request data sources produce time-series data sampled at 1 kHz, 
     * then when using 
     * <code>tuSamplingPeriodUnits</code> = <code>{@link TimeUnit#MILLISECONDS}</code> 
     * the returned value actually represents the number of sample values within the results set.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * The returned value is an estimate of the number of <em>samples</em>, or data value,
     * within the results set.  Do to the heterogeneous nature of the Data Platform data
     * archive, the actual memory size, in <em>bytes</em>, of the results set can vary
     * significantly according to the data type of the data source.
     * </p>  
     * 
     * @param   tuSamplingPeriodUnits time units to use for time range
     * 
     * @return  the current query domain size in (data source count)-(<code>tuTimeRange</code> seconds)
     * 
     * @see #approxDomainSize()
     */
    public long approxRequestSamples(long lngSamplingPeriod, TimeUnit tuSamplingPeriodUnits) {
        
        // Compute the duration of the time range in units of the sampling period 
        Duration    durRange = this.rangeDuration();
        long        lngRangeSize = tuSamplingPeriodUnits.convert(durRange);
        
        // Compute N the number of samples/source within the duration
        //  Note - this is equivalent to N = [duration (in seconds)] * [f = 1/T (1/seconds)]
        //         but works better for integer arithmetic
        long        lngSamples = lngRangeSize / lngSamplingPeriod;
        
        // Compute the number of sources * number of samples
        long        lngCntSources = this.getSourceCount();
        long        lngRequestSize = lngCntSources * lngSamples;
        
        return lngRequestSize;
    }
    
    //    /**
    //     * <p>
    //     * Creates the <em>Query Service</em> query language (DQL) string based upon 
    //     * the history of calls to the range, selection, and filter methods.
    //     * </p>
    //     * <p>  
    //     * The string is formatted in the DQL language.  It is used for building
    //     * gRPC request messages by the other builder methods.
    //     * </p>
    //     * <p>
    //     * <h2>NOTES:</h2>
    //     * <ul>
    //     * <li> 
    //     * If no <code>range</code> methods were called then the returned query
    //     * defaults to the inception time defined in the applications properties.
    //     * </li>
    //     * <li> 
    //     * If no <code>select</code> methods were called then the returned 
    //     * query defaults to all PV names in the <em>Query Service</em>.
    //     * </li>
    //     * <li>
    //     * If the <code>{@link #pageSize(Integer)}</code> method was not called
    //     * then the returned query defaults the page size specified in the
    //     * <i>application.yml</i> configuration file.
    //     * </li>
    //     * <li>
    //     * If the <code>{@link #pageStartIndex(Integer)}</code> method was not called
    //     * then the returned query defaults to the zero index indicating that
    //     * the <em>Query Service</em> send all data indicated in the query request.
    //     * </li>
    //     * </ul>
    //     * </p>
    //     * 
    //     * @return  properly formatted <em>Query Service</em> DQL snapshot data request query
    //     */
    //    public String buildDqlQueryString() {
    //        StringBuffer bufQuery  = new StringBuffer();
    //
    //        // Build the SELECT phrase
    //        bufQuery.append( "SELECT ");
    //        
    //        //   If the SELECT phase list is empty select for everything
    //        if (this.lstSelCmps.isEmpty())
    //            this.lstSelCmps.add( this.createSelectCmpAllPvs() );
    //        
    //        String strSelLast = this.lstSelCmps.removeLast();
    //        this.lstSelCmps.forEach( s -> bufQuery.append(s + ", "));
    //        bufQuery.append(strSelLast + " ");
    //        this.lstSelCmps.add(strSelLast);
    //        
    //        // Build the WHERE phrase
    //        bufQuery.append( " WHERE ");
    //
    //        if (this.lstWhrCmps.isEmpty()) 
    //            try {    
    //                this.lstWhrCmps.add( this.createWhereCmpDsInception() );
    //            } catch (DsGrpcException e) {
    //                this.lstWhrCmps.add( this.createSelectCmpBeforeNow() );
    //            }
    //        
    //        String strWhrLast = this.lstWhrCmps.removeLast();
    //        this.lstWhrCmps.forEach( s -> bufQuery.append(s + " AND ") );
    //        bufQuery.append(strWhrLast);
    //        this.lstWhrCmps.add(strWhrLast);
    //        
    //        return bufQuery.toString();
    //    }
        
//    /**
//     * <p>
//     * Computes and returns the query domain decomposition parameters for a default decomposition. 
//     * </p>
//     * <p>
//     * Computes the query sub-domain parameters for a default decompose query.
//     * Query domain decomposition parameters are used to build decompose queries.
//     * This method computes and returns the parameters used for a query domain decomposition 
//     * when using the default decomposition parameters specified in the application default 
//     * parameters (i.e., rather than decomposition parameters given by users).
//     * </p>
//     * <p>
//     * The method uses the class constants <code>{@link #CNT_COMPOSITE_MAX_SOURCES}</code>
//     * and <code>{@link #DUR_COMPOSITE_MAX}</code> to compute the number of query domain
//     * sub-divisions using default decomposition.  Specifically, we compute
//     * <code>
//     * <pre>
//     *   cntQueriesHor = {@link #getSourceCount()} / {@link #CNT_COMPOSITE_MAX_SOURCES}
//     *   cntQueriesVer = {@link #rangeDuration()}.divideBy({@link #DUR_COMPOSITE_MAX})
//     * </pre>
//     * </code>
//     * Note further that if there exists a non-zero remainder for the above integer divisions
//     * the domain subdivision count must be incremented to accommodate the excess domain.
//     * We perform the following modifications
//     * <code>
//     * <pre>
//     *   cntQueriesHor += ({@link #getSourceCount()} % {@link #CNT_COMPOSITE_MAX_SOURCES} == 0) ? 0 : 1
//     *   cntQueriesVer += {{@link #rangeDuration()}.minus({@link #DUR_COMPOSITE_MAX}.multiedBy(cntQueryiesVer).isZero) ? 0 : 1
//     * </pre>
//     * </code>
//     * </p>
//     * <p>
//     * The decompose query type is also determined by the value of the above quantities
//     * according to the following:
//     * <code>
//     * <pre>
//     *   {@link RequestDecompType#NONE}       <- (cntQueriesHor == 1) && (cntQueriesVer == 1) 
//     *   {@link RequestDecompType#HORIZONTAL} <- (cntQueriesHor > 1) && (cntQueriesVer == 1) 
//     *   {@link RequestDecompType#VERTICAL}   <- (cntQueriesHor == 1) && (cntQueriesVer > 1) 
//     *   {@link RequestDecompType#GRID}       <- (cntQueriesHor > 1) && (cntQueriesVer > 1) 
//     * </pre>
//     * </code>
//     * </p>
//     * 
//     * @return  record containing sub-domain parameters when using default decomposition
//     * 
//     * @see DpApiConfig
//     * @see DpQueryConfig
//     */
//    public RequestDecompParams decomposeDomainDefault() {
//
//        // The decompose query type
//        RequestDecompType   enmType;
//
//        // Determine decompose query domain sizes
//        int     cntQueriesHor = this.getSourceCount() / CNT_COMPOSITE_MAX_SOURCES;
//        long    cntQueriesVer = this.rangeDuration().dividedBy(DUR_COMPOSITE_MAX);
//
//
//        // Note integer division, must increment count if non-zero remainder
//        if (this.getSourceCount() % CNT_COMPOSITE_MAX_SOURCES > 0)
//            cntQueriesHor++;
//        if (!this.rangeDuration().minus(DUR_COMPOSITE_MAX.multipliedBy(cntQueriesVer)).isZero())
//            cntQueriesVer++;
//
//        // The overall request is not large enough to invoke decomposition
//        if ((cntQueriesHor == 1) && (cntQueriesVer == 1)) {
//
//            enmType = RequestDecompType.NONE;
//
//            // Horizontal decompose request
//        } else if ((cntQueriesHor > 1) && (cntQueriesVer == 1)) {
//
//            enmType = RequestDecompType.HORIZONTAL;
//
//            // Vertical decompose request
//        } else if ((cntQueriesHor == 1) && (cntQueriesVer > 1)) {
//
//            enmType = RequestDecompType.VERTICAL;
//
//            // Grid decompose request
//        } else {
//
//            enmType = RequestDecompType.GRID;
//
//        }
//
//        return new RequestDecompParams(enmType, cntQueriesHor, Long.valueOf(cntQueriesVer).intValue());
//    }

//    /**
//     * <p>
//     * Computes and returns the query domain decomposition parameters for a default decomposition. 
//     * </p>
//     * <p>
//     * Query domain decomposition parameters are used to build decompose queries.
//     * This method computes and returns the parameters used for a query domain decomposition 
//     * when using the default decomposition parameters specified in the application default 
//     * parameters (i.e., rather than decomposition parameters given by users).
//     * </p>
//     *  
//     * @return  record of default query domain decomposition parameters for a decompose query.
//     * 
//     * @see DpApiConfig
//     * @see DpQueryConfig
//     */
//    public RequestDecompParams  getDecompositionDefault() {
//        return this.decomposeDomainDefault();
//    }
    
    
    // 
    // Request Metadata
    //
    
    /**
     * <p>
     * Sets the (optional) identifier for the time-series data request.
     * </p>
     * <p>
     * The request identifier is an additional property to identify requested time-series
     * data.  It is used solely by the Java API Library; that is, it is not a property of the
     * Data Platform Query Service.
     * </p>
     * <p>
     * Typically, the request ID is assigned by clients and then appears within the final (tabular) 
     * result set once the request is fully recovered and processed by the API library.  Thus, no special 
     * characteristics are required of the ID and a string name is typically sufficient.
     * </p>
     * 
     * @param strRqstId the (optional) request identifier used by the Java API Library
     */
    public void setRequestId(String strRqstId) {
        this.strRqstId = strRqstId;
    }
    
    /**
     * <p>
     * Sets the gRPC data stream type to use when recovering query results set.
     * </p>
     * <p>
     * Sets the preferred gRPC data stream "type" used for the data request operation.  The currently supported
     * Query Service data stream types are provided in the enumeration <code>{@link DpGrpcStreamType}</code>.
     * See the enumeration documentation for further details on gRPC data streams supported by the Query Service.
     * types 
     * <p> 
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Note all stream types within enumeration <code>DpGrpcStreamType</code> are applicable.  Specifically,
     * the <code>{@link DpGrpcStreamType#FORWARD}</code> <b>cannot</b> be used (it has no context) and
     * an exception will be thrown.
     * </li> 
     * <li>
     * This value is only applicable if a streaming RPC operation is used to recover the query results set.  
     * Even then, the choice of preferred gRPC data stream type is not guaranteed.
     * </li>
     * <br/>
     * <li>
     * This is a property to be set for advanced operations.  Typically, the default value is chosen for 
     * performance considerations.  The default value is current given by
     * <code>
     * <pre>
     *   {@link #ENM_STREAM_PREF} = {@value #ENM_STREAM_PREF}
     * </pre>
     * </code> 
     * The value for class constant <code>{@link #ENM_STREAM_PREF}</code> is taken from the Query Service
     * API configuration in <code>{@link DpApiConfig}</code>.
     * </li>
     * </ul>
     * </p>
     * 
     * @param enmStreamType preferred gRPC data stream type for streaming query operation
     * 
     * @throws  IllegalArgumentException    attempted to specify a unidirectional forward stream
     */
    public void setStreamType(DpGrpcStreamType enmStreamType) throws IllegalArgumentException {
        
        // Check stream type
        if (enmStreamType == DpGrpcStreamType.FORWARD)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + ": Illegal stream type: " + enmStreamType);
        
        this.enmStrmType = enmStreamType;
    }
    
    /**
     * <p>
     * Sets the size of the data pages to return from the <em>Query Service</em>
     * when using paginated requests.
     * </p>
     * <p>
     * Paginated responses to snapshot data requests are used in asynchronous
     * queries. The data is streamed back in gRPC message blocks constituting
     * data pages.  The value given here is the number of rows in each page 
     * (not the allocation size of the gRPC message).
     * </p>
     * <p>
     * If not set, this value defaults to that specified in the 
     * Application properties (in the <i>application.yml</i> file).
     * </p>
     * <p>
     * This is primarily a performance parameter to tweak.
     * Note that the maximum gRPC message size has a hard limit (currently
     * at 4 Mbytes).  For requests with large numbers of data columns each
     * row could, potentially, require significant resource allocation.
     * </p>
     * 
     * @param szPage page size of a data block returned from the <em>Query Service</em> (in rows)
     */
    @AUnavailable(status=STATUS.UNDER_REVIEW)
    public void setPageSize(Integer szPage) {
        this.szPage = szPage;
    }
    
    /**
     * <p>
     * Sets the page index of the data request.
     * We assume that a previous request has already been sent to the
     * <em>Query Service</em> to initiate a data stream.
     * Thus, the resultant query built by the <code>DpDataRequest</code>
     * contains only a request for this page under the assumption that
     * it applies to a previous snapshot data request.
     * </p>
     * <p> 
     * Defaults to index 0 which indicates that the 
     * <em>Query Service</em> should send all pages of the resultant query
     * (i.e., initiate a data stream).
     * </p>
     * <p>
     * <b>Use at your own risk</b> as calling this method changes the very
     * nature of the resultant data query request.
     * </p>  
     * 
     * @param indStartPage start page index of the streamed data
     */
    @AUnavailable(status=STATUS.UNDER_REVIEW)
    public void setPageStartIndex(Integer indStartPage) {
        this.indStartPage = indStartPage;
    }
    
    
    //
    // Time Range Selection
    //
    
    /**
     * <p>
     * Restrict the request to data with timestamps occurring within the
     * given time interval.
     * </p>
     * <p>
     * The range of the query is restricted to data acquired at or after the 
     * initial time instant and at or before the final time instant.  That is,
     * create an inclusive time interval query.
     * </p>
     * <p>
     * This method is equivalent to the paired method calls
     * <code>{@link #rangeAfter(Instant)}</code> and 
     * <code>{@link #rangeBefore(Instant)}</code> using the respective arguments.
     * </p>
     * 
     * @param insBeg    time instant at interval start
     * @param insEnd    time instant at interval end
     */
    public void rangeBetween(Instant insBeg, Instant insEnd) {
        this.rangeAfter(insBeg);
        this.rangeBefore(insEnd);
    }
    
    /**
     * <p>
     * Restricts the data request to timestamps within the interval specified
     * by the start time and an duration.
     * </p>
     * <p>
     * The data request will specify timestamps within the interval 
     * <br/><br/>
     * &nbsp; &nbsp; [insBeg, insBeg + lngDuration]
     * <br/><br/>
     * where the time units of the duration are given in the final argument.
     * </p>
     * 
     * @param insBeg        start time instant of the time range interval
     * @param lngDuration   duration value of the time range interval
     * @param tuDuration    duration time units 
     */
    public void rangeDuration(Instant insBeg, Long lngDuration, TimeUnit tuDuration) {
        ChronoUnit  cuDuration = tuDuration.toChronoUnit();
        Instant     insStop = insBeg.plus(lngDuration, cuDuration);
        
        this.rangeBetween(insBeg, insStop);
    }
    
    /**
     * <p>
     * Restrict the request to data with timestamps occurring at or before the
     * given time instant.
     * </p>
     * <p>
     * Restricts the range of viable timestamps to the time interval starting
     * from the <em>Query Service</em> inception up to and including the given 
     * time instant.
     * </p>
     * 
     * @param insEnd    largest timestamp within data request
     */
    public void rangeBefore(Instant insEnd) {
        this.insStop = insEnd;
    }
    
    /**
     * <p>
     * Restrict the request to data with timestamps occurring at or after the
     * given time instant.
     * </p>
     * Restricts the range of viable timestamps to the interval starting at the 
     * given time instant until the time of the request.
     * </p>
     * 
     * @param insBeg    smallest timestamp within data request
     */
    public void rangeAfter(Instant insBeg) {
        this.insStart = insBeg;
    }
    
    /**
     * <p>
     * Set the request time range to timestamps occurring at or after the 
     * given time offset until now.
     * </p>
     * <p>
     * Restricts the timestamp range of the query to the given time offset 
     * up to the instant of the request. 
     * All time offset values are expressed relative to <em>now</em> and, 
     * thus, they must be negative (otherwise an exception is thrown).  
     * For example, the call
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <code>rangeOffset(-5, TimeUnit.HOURS)</code>
     * <br/>
     * <br/>
     * restricts the query to all data from 5 hours ago until now.
     * </p>
     * 
     * @param lngTimeOffset time offset relative to now (must be negative)
     * @param tuUnit        time unit of time offset
     * 
     * @throws IllegalArgumentException  the time offset was not a negative value
     */
    public void rangeOffset(long lngTimeOffset, TimeUnit tuUnit) throws IllegalArgumentException {
        if (lngTimeOffset > 0)
            throw new IllegalArgumentException("Relative times must be negative");
        
        // Convert offset to a positive nanosecond duration 
        //  Subtract from now instant to yield new start time instant
        long        lngDur = -lngTimeOffset;
        ChronoUnit  cuUnit = tuUnit.toChronoUnit();
        
        this.insStop = Instant.now();
        this.insStart = this.insStop.minus(lngDur, cuUnit);
    }
    
    
    //
    // Data Source Selection
    //
    
    /**
     * <p>
     * Request archive data for the given data source name.
     * </p>
     * <p>
     * Opens the query request to return data for the given data source. 
     * This method may be called repeatedly to add multiple data sources to the request.
     * </p> 
     * <p>
     * <s>A regular expression can be used to match EPICS PV names.  Thus, the 
     * resulting request would produce multiple data columns where each column
     * contains the values for a PV matching the regular expression.
     * </s>
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>Regular expression are not currently supported.</li>
     * <li>Data source names must be unique within the archive.</li>
     * </ul>
     * </p>
     * 
     * @param strName    data source name
     */
    public void selectSource(String strName) {
        this.lstSelCmps.add(strName);
    }
    
    /**
     * <p>
     * Request archive data for the given list of data source names.
     * </p>
     * <p>
     * Opens the query to return data from data sources in the
     * given list. This method may be called repeatedly to add multiple 
     * PV name lists to the request.
     * </p>
     * <p>
     * <s>
     * A regular expression can be used to match EPICS PV names.  That is,
     * each element in the argument list can be a PV-name matching regular
     * expression.
     * Thus, for each regular expression in the list, the
     * resulting request would produce multiple data columns where each column
     * contains the values for a PV matching the regular expression.
     * </s>
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>Regular expression are not currently supported.</li>
     * <li>Data source names must be unique within the archive.</li>
     * </ul>
     * </p>
     * 
     * @param lstNames list of data source names
     */
    public void selectSources(Collection<String> lstNames) {
        
        this.lstSelCmps.addAll(lstNames);
    }
    
    /**
     * <p>
     * Requests data source attribute values for the given list of data sources.
     * </p>
     * <p>
     * A column of attribute values for the given attribute name is to be
     * added to the query results for every PV name in the given name list.
     * </p>
     * <p>
     * A regular expression can be used to match EPICS PV names.  That is,
     * each element in the argument list can be a PV-name matching regular
     * expression.
     * Thus, for each regular expression in the list, the
     * resulting request would produce multiple attribute value columns where 
     * each column contains values for a PV names matching the regular 
     * expression.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>A regular expression may also be used for the attribute name, although
     *     this feature is untested.
     *     </li>
     * <li>It appears that PV alarm status, alarm severity, and alarm messages may
     *     also be requested.  The attribute names for these cases are, respectively,
     *     <ul>
     *     <li>alarm-status</li>
     *     <li>alarm-severity</li>
     *     <li>alarm-message</li>
     *     </ul>
     *     However, this feature has not been verified.
     * </ul>
     * </p>
     * 
     * @param strPvNmRegex list of PV names, or name matching regular expression
     * @param strAttrNm    name of an attribute associated with the given PVs
     */
    @AUnavailable(status=AUnavailable.STATUS.UNDER_REVIEW)
    public void selectSourceAttr(String strPvNmRegex, String strAttrNm) {
        this.selectPvsAttr(List.of(strPvNmRegex), strAttrNm);
    }
    
    /**
     * <p>
     * Requests PV attribute values for the given list of EPICS PVs.
     * </p>
     * <p>
     * A column of attribute values for the given attribute name is to be
     * added to the query results for every PV name in the given name list.
     * </p>
     * <p>
     * A regular expression can be used to match EPICS PV names.  That is,
     * each element in the argument list can be a PV-name matching regular
     * expression.
     * Thus, for each regular expression in the list, the
     * resulting request would produce multiple attribute value columns where 
     * each column contains values for a PV names matching the regular 
     * expression.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>A regular expression may also be used for the attribute name, although
     *     this feature is untested.
     *     </li>
     * <li>It appears that PV alarm status, alarm severity, and alarm messages may
     *     also be requested.  The attribute names for these cases are, respectively,
     *     <ul>
     *     <li>alarm-status</li>
     *     <li>alarm-severity</li>
     *     <li>alarm-message</li>
     *     </ul>
     *     However, this feature has not been verified.
     * </ul>
     * </p>
     * 
     * @param lstPvNmRegex list of PV names, or name matching regular expression
     * @param strAttrNm    name of an attribute associated with the given PVs
     */
    @AUnavailable(status=AUnavailable.STATUS.UNDER_REVIEW)
    public void selectPvsAttr(List<String> lstPvNmRegex, String strAttrNm) {
        
        for (String strPvNmRegex : lstPvNmRegex) {
            String strSelQry = "`" + strPvNmRegex + ":" + strAttrNm + "`";
            
            this.lstSelCmps.add(strSelQry);
        }
    }
    
    /**
     * <p>
     * Creates a special results column for the alarm statuses for the given  
     * PV list.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This is part of the advanced (and unverified) API.
     * </p> 
     * 
     * @param lstPvNmRegex list of PV names, or name matching regular expression
     */
    @AUnavailable(status=AUnavailable.STATUS.UNDER_REVIEW)
    public void selectPvsAlarmStatus(List<String> lstPvNmRegex) {
        
        for (String strPvNmRegex : lstPvNmRegex) {
            String strSelQry = "`" + strPvNmRegex + ":alarm-status`";
            
            this.lstSelCmps.add(strSelQry);
        }
    }
    
    /**
     * <p>
     * Creates a special results column for the alarm severities for the given  
     * PV list.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This is part of the advanced (and unverified) API.
     * </p> 
     * 
     * @param lstPvNmRegex list of PV names, or name matching regular expression
     */
    @AUnavailable(status=AUnavailable.STATUS.UNDER_REVIEW)
    public void selectPvsAlarmSeverity(List<String> lstPvNmRegex) {
        
        for (String strPvNmRegex : lstPvNmRegex) {
            String strSelQry = "`" + strPvNmRegex + ":alarm-severity`";
            
            this.lstSelCmps.add(strSelQry);
        }
    }
    
    /**
     * <p>
     * Creates a special results column for the alarm messages for the given  
     * PV list.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This is part of the advanced (and unverified) API.
     * </p> 
     * 
     * @param lstPvNmRegex list of PV names, or name matching regular expression
     */
    @AUnavailable(status=AUnavailable.STATUS.UNDER_REVIEW)
    public void selectPvsAlarmMessages(List<String> lstPvNmRegex) {
        
        for (String strPvNmRegex : lstPvNmRegex) {
            String strSelQry = "`" + strPvNmRegex + ":alarm-message`";
            
            this.lstSelCmps.add(strSelQry);
        }
    }
    
    
    //
    // Filters
    //
    
    /**
     * <p>
     * Filter the request to include only results where the value of the
     * given EPICS process variable (PV) is equal to the given value.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>It is unknown if this filter is robust for regular expressions. 
     *     </li>
     * <li>The given PV must have a scalar value and must be numeric where the
     *     equal to operator (==) is well defined.
     *     </li>
     * </ul>
     * </p>
     *     
     * @param strPvName exact name of scalar EPICS process variable
     * @param numVal    numeric comparison value for the process variable 
     */
    @AUnavailable(status=AUnavailable.STATUS.UNDER_REVIEW)
    public void filterValueEquals(String strPvName, Number numVal) {
        String strWhrCmp = "`" + strPvName + ".value` == " + numVal;
        
        this.lstWhrCmps.add(strWhrCmp);
    }
    
    /**
     * <p>
     * Filter the request to include only results where the value of the
     * given EPICS process variable (PV) is less than the given value.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>It is unknown if this filter is robust for regular expressions. 
     *     </li>
     * <li>The given PV must have a scalar value and must be numeric where the
     *     less than operator (<) is well defined.
     *     </li>
     * </ul>
     * </p>
     *     
     * @param strPvName exact name of scalar EPICS process variable
     * @param numVal    numeric comparison value for the process variable 
     */
    @AUnavailable(status=AUnavailable.STATUS.UNDER_REVIEW)
    public void filterValueLessThan(String strPvName, Number numVal) {
        String strWhrCmp = "`" + strPvName + ".value` < " + numVal;
        
        this.lstWhrCmps.add(strWhrCmp);
    }
    
    /**
     * <p>
     * Filter the request to include only results where the value of the
     * given EPICS process variable (PV) is greater than the given value.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>It is unknown if this filter is robust for regular expressions. 
     *     </li>
     * <li>The given PV must have a scalar value and must be numeric where the
     *     greater than operator (>) is well defined.
     *     </li>
     * </ul>
     * </p>
     *     
     * @param strPvName exact name of scalar EPICS process variable
     * @param numVal    numeric comparison value for the process variable 
     */
    @AUnavailable(status=AUnavailable.STATUS.UNDER_REVIEW)
    public void filterValueGreaterThan(String strPvName, Number numVal) {
        String strWhrCmp = "`" + strPvName + ".value` > " + numVal;
        
        this.lstWhrCmps.add(strWhrCmp);
    }
    
    /**
     * <p>
     * Filter the request to include only results produced by the snapshot
     * data provider with the given unique identifier.
     * </p>
     * <p>
     * The snapshot data provider UID is obtained from the <em>Query Service</em>
     * ingestion service when either 1) registering a provider when using the
     * synchronous ingestion service (see <code>{@link IIngestionService}</code>)
     * 2) opening an asynchronous ingestion stream 
     * (see <code>{@link IIngestionStream}</code>).
     * The snapshot data provider UID can be obtained post ingestion using the
     * synchronous ingestion service with a call to 
     * <code>{@link IIngestionService#registerProvider(ProviderRegistrator)}</code>.
     * </p>
     * 
     * @param intSdpUid UID of the target snapshot data provider
     * 
     * @see IIngestionService#registerProvider(ProviderRegistrator)
     * @see IIngestionStream#openStream(ProviderRegistrator)
     */
    @AUnavailable(status=AUnavailable.STATUS.UNDER_REVIEW)
    public void filterProviderId(Integer intSdpUid) {
        String strWhrCmp = "`sdp-id` == " + intSdpUid;
        
        this.lstWhrCmps.add(strWhrCmp);
    }
    
    /**
     * <p>
     * Filter the request to include only the results within the snapshot
     * with the given unique identifier.
     * </p>
     * <p>
     * Snapshot UIDs are obtained from the <em>Query Service</em>
     * ingestion service when either 1) adding a data frame using the
     * synchronous ingestion service (see <code>{@link IIngestionService}</code>)
     * 2) closing an asynchronous ingestion stream 
     * (see <code>{@link IIngestionStream}</code>).
     * </p>
     * 
     * @param intSnapshotUid UID of the target snapshot
     * 
     * @see IIngestionService#addSnapshotData(ProviderId, DataFrame)
     * @see IIngestionStream#closeStream() 
     * @see IIngestionStream#closeStreamSoft() 
     */
    @AUnavailable(status=AUnavailable.STATUS.UNDER_REVIEW)
    public void filterSnapshotId(Integer intSnapshotUid) {
        String strWhrCmp = "`snapshot-id` == " + intSnapshotUid;
        
        this.lstWhrCmps.add(strWhrCmp);
    }
    
    /**
     * <p>
     * Filter the request to include only results where the alarm status
     * of selected PVs are equal to the given enumeration value.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li><b>WARNING:</b>This feature is part of the advanced API and is 
     *     unverified.</li>
     * <li>The filter may apply to all selected PVs in the current request
     *     or it may apply only to the special columns identified with 
     *     the <code>{@link #selectAlarmStatus(List)}</code> method.
     *     </li>
     * </ul>
     * </p>
     * 
     * @param enmStatus alarm status code used to filter results
     */
    @AUnavailable(status=AUnavailable.STATUS.UNDER_REVIEW)
    public void filterAlarmStatusEq(DataValue.ValueStatus.StatusCode enmStatus) {
        String strWhrCmp = "`alarm-status` == '" + enmStatus + "'";
        
        this.lstWhrCmps.add(strWhrCmp);
    }
    
    /**
     * <p>
     * Filter the request to include only results where the alarm status
     * of selected PVs are <b>not</b> equal to the given enumeration value.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li><b>WARNING:</b>This feature is part of the advanced API and is 
     *     unverified.</li>
     * <li>The filter may apply to all selected PVs in the current request
     *     or it may apply only to the special columns identified with 
     *     the <code>{@link #selectAlarmStatus(List)}</code> method.
     *     </li>
     * </ul>
     * </p>
     * 
     * @param enmStatus alarm status code used to filter results
     */
    @AUnavailable(status=AUnavailable.STATUS.UNDER_REVIEW)
    public void filterAlarmStatusNeq(DataValue.ValueStatus.StatusCode enmStatus) {
        String strWhrCmp = "`alarm-status` != '" + enmStatus + "'";
        
        this.lstWhrCmps.add(strWhrCmp);
    }
    
    /**
     * <p>
     * Filter the request to include only results where the alarm severity
     * of selected PVs are equal to the given enumeration value.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li><b>WARNING:</b>This feature is part of the advanced API and is 
     *     unverified.</li>
     * <li>The filter may apply to all selected PVs in the current request
     *     or it may apply only to the special columns identified with 
     *     the <code>{@link #selectAlarmSeverity(List)}</code> method.
     *     </li>
     * </ul>
     * </p>
     * 
     * @param enmSeverity alarm status code used to filter results
     */
    @AUnavailable(status=AUnavailable.STATUS.UNDER_REVIEW)
    public void filterAlarmSeverityEq(DataValue.ValueStatus.Severity enmSeverity) {
        String strWhrCmp = "`alarm-status` == '" + enmSeverity + "'";
        
        this.lstWhrCmps.add(strWhrCmp);
    }
    
    /**
     * <p>
     * Filter the request to include only results where the alarm severity
     * of selected PVs are <b>not</b> equal to the given enumeration value.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li><b>WARNING:</b>This feature is part of the advanced API and is 
     *     unverified.</li>
     * <li>The filter may apply to all selected PVs in the current request
     *     or it may apply only to the special columns identified with 
     *     the <code>{@link #selectAlarmSeverity(List)}</code> method.
     *     </li>
     * </ul>
     * </p>
     * 
     * @param enmSeverity alarm status code used to filter results
     */
    @AUnavailable(status=AUnavailable.STATUS.UNDER_REVIEW)
    public void filterAlarmSeverityNeq(DataValue.ValueStatus.Severity enmSeverity) {
        String strWhrCmp = "`alarm-status` != '" + enmSeverity + "'";
        
        this.lstWhrCmps.add(strWhrCmp);
    }
    
    /**
     * <p>
     * Prints out a text description of general properties of this time-series data request to the given output.
     * </p>
     * <p>
     * The high-level properties of the current request are printed out in a line-by-line format with an
     * optional padding at the left-hand side.  The <code>strPad</code> argument is assumed to be a sequence
     * of white spaces (or <code>null</code>) that is used to pad the left-hand side before each line
     * heading.  This format is useful when the description is used as part of a larger output series.
     * </p>
     * 
     * @param ps        output stream to receive time-series data request properties description
     * @param strPad    optional left-hand side whitespace padding for line headers
     */
    public void printOutProperties(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        
        ps.println(strPad + this.getClass().getSimpleName() + " " + this.getRequestId());
        ps.println(strPad + "  Data source count : " + this.getSourceCount());
        ps.println(strPad + "  Start time        : " + this.getInitialTime());
        ps.println(strPad + "  Request duration  : " + this.rangeDuration());
        ps.println(strPad + "  Domain size (byte-sec) : " + this.approxDomainSize());
    }
    

    //
    // Private Methods
    //
    
    /**
     * <p>
     * Creates a new, empty instance of <code>DpDataRequest</code>.
     * </p>
     * <p>
     * Creates the request instant then calls <code>{@link #reset()}</code> method
     * to set all parameters to initializing "empty" state. 
     * </p>
     */
    private DpDataRequest() {
        this.reset();
    }
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>DpDataRequest</code>.
     * </p>
     * <p>
     * The arguments are used to initialize the relevance parameters of the new request.
     * This method is intended for use in the creation of <em>decompose queries</em>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * All other request instance attributes are left unchanged from the default values
     * identified in their declaration.
     * </p>
     *
     * @param strId         the request identifier
     * @param enmType       the preferred gRPC data stream type
     * @param insStart      starting instant of time range
     * @param insStop       final instant of time range
     * @param lstSources    list of all data source names for the new request
     * 
     * @throws  IllegalArgumentException    the given stream type is out of context
     */
    private DpDataRequest(String strId, DpGrpcStreamType enmType, Instant insStart, Instant insStop, List<String> lstSources) throws IllegalArgumentException {
        
        // Check stream type
        if (enmType == DpGrpcStreamType.FORWARD)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Illegal stream type: " + enmType);
        
        this.indStartPage = 0;
        this.szPage = SZ_PAGES;

        this.strRqstId = strId;
        this.enmStrmType = enmType;
        this.insStart = insStart;
        this.insStop = insStop;
        
        this.lstSelCmps.addAll(lstSources);
    }
    
//    /**
//     * <p>
//     * Creates a Data Platform <code>CursorRequest</code> object based upon the history 
//     * of calls page methods.
//     * </p>
//     * <p>  
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li> If no <code>range</code> methods were called then the returned query
//     * defaults to the inception time defined in the applications properties.
//     * </li>
//     * <li> If no <code>select</code> methods were called then the returned 
//     * query defaults to all PV names in the <em>Query Service</em>.
//     * </li>
//     * </ul>
//     * </p>
//     * 
//     * @return  new <code>CursorOperation</code> message configured by history 
//     */
//    private CursorOperation buildCursorRequest() {
//        
//        CursorOperation rqst = CursorOperation.CURSOR_OP_NEXT;
//        
//        return rqst;
//    }
    
//    /**
//     * <p>
//     * Creates a <em>Query Service</em> gRPC <code>Request</code> object based upon the history 
//     * of calls to the restrictor methods.  This is essentially a convenience method
//     * which calls <code>{@link #buildQueryString()}</code> and packages the results into 
//     * a gRPC <code>Request</code> object.
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li> All references to paginated data is ignored.  Specifically calls
//     * to <code>{@link #pageSize(Integer)}</code> and <code>{@link #pageStartIndex(Integer)}</code>
//     * have no relevance to the returned result.
//     * </li>
//     * <li> If no <code>range</code> methods were called then the returned query
//     * defaults to the inception time defined in the applications properties.
//     * </li>
//     * <li> If no <code>select</code> methods were called then the returned 
//     * query defaults to all PV names in the <em>Query Service</em>.
//     * </li>
//     * </ul>
//     * </p>
//     * 
//     * @return  properly formatted <em>Query Service</em> DQL request
//     * 
//     * @see DpDataRequest#buildQueryString()
//     */
//    private QuerySpec buildQuerySpec() {
//        QuerySpec.Builder bldrQry = QuerySpec.newBuilder();
//        bldrQry.setStartTime( ProtoMsg.from(this.insStart) );
//        bldrQry.setEndTime( ProtoMsg.from( this.insStop) );
//        bldrQry.addAllColumnNames(this.lstSelCmps);
//        QuerySpec qry = bldrQry.build();
//        
//        return qry;
//    }
    
    /**
     * Creates and returns a SELECT query component which selects for 
     * all PV names in the <em>Query Service</em>.
     * 
     * @return a single SELECT component that selects for all PV names
     */
    private String createSelectCmpAllPvs() /** throws DsGrpcException */ {
        String strQuery = "`*.*`";
        
        return strQuery;
    }
    
    /**
     * <p>
     * Used whenever a "range" restrictor method is not called on the builder
     * instance.  This method builds a range query component based upon the 
     * inception date/time of the <em>Query Service</em> specified in the application
     * properties.
     * </p>
     * <p>
     * <h2>UPDATE:</h2>
     * The method now uses the start of the current time epoch as the inception
     * data of the <em>Query Service</em>.  No exceptions are thrown.
     * </p>
     * 
     * @return  default range query component
     */
    private String createWhereCmpDsInception() {
//        String strRange = CFG_DEFAULT.getInception();
//        if ( strRange==null )
//            throw new DsGrpcException("No inception time defined in Query Service properties");

//        try { 
//            // Make sure time is properly formatted
//            Instant insBeg = Instant.parse(strRange);
            Instant insBeg = INS_EPOCH;
            String strQryTm = "time >= '" + insBeg + "'";
                    
            return strQryTm;
            
//        } catch (DateTimeParseException e) {
//            throw new DsGrpcException("Bad inception time in configuration: " + strRange);
//        }
    }
    
    /**
     * Creates and returns a single WHERE query component that restricts the
     * time ranges to everything before now.  That is, essentially everything
     * currently in the Query Service.
     * 
     * @return WHERE query component specifying time range "everything up to now"
     */
    private String createSelectCmpBeforeNow() /** throws DsGrpcException */ {
        Instant insNow = Instant.now();
        String  strNow = insNow.toString();
        
        String strQryTm = "time <= '" + strNow + "'";
        
        return strQryTm;
    }
    
//    /**
//     * <p>
//     * Creates the <em>Query Service</em> query language (DQL) string based upon 
//     * the history of calls to the range, selection, and filter methods.
//     * </p>
//     * <p>  
//     * The string is formatted in the DQL language.  It is used for building
//     * gRPC request messages by the other builder methods.
//     * </p>
//     * <p>
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li> 
//     * If no <code>range</code> methods were called then the returned query
//     * defaults to the inception time defined in the applications properties.
//     * </li>
//     * <li> 
//     * If no <code>select</code> methods were called then the returned 
//     * query defaults to all PV names in the <em>Query Service</em>.
//     * </li>
//     * <li>
//     * If the <code>{@link #pageSize(Integer)}</code> method was not called
//     * then the returned query defaults the page size specified in the
//     * <i>application.yml</i> configuration file.
//     * </li>
//     * <li>
//     * If the <code>{@link #pageStartIndex(Integer)}</code> method was not called
//     * then the returned query defaults to the zero index indicating that
//     * the <em>Query Service</em> send all data indicated in the query request.
//     * </li>
//     * </ul>
//     * </p>
//     * 
//     * @return  properly formatted <em>Query Service</em> DQL snapshot data request query
//     */
//    public String buildDqlQueryString() {
//        StringBuffer bufQuery  = new StringBuffer();
//
//        // Build the SELECT phrase
//        bufQuery.append( "SELECT ");
//        
//        //   If the SELECT phase list is empty select for everything
//        if (this.lstSelCmps.isEmpty())
//            this.lstSelCmps.add( this.createSelectCmpAllPvs() );
//        
//        String strSelLast = this.lstSelCmps.removeLast();
//        this.lstSelCmps.forEach( s -> bufQuery.append(s + ", "));
//        bufQuery.append(strSelLast + " ");
//        this.lstSelCmps.add(strSelLast);
//        
//        // Build the WHERE phrase
//        bufQuery.append( " WHERE ");
//
//        if (this.lstWhrCmps.isEmpty()) 
//            try {    
//                this.lstWhrCmps.add( this.createWhereCmpDsInception() );
//            } catch (DsGrpcException e) {
//                this.lstWhrCmps.add( this.createSelectCmpBeforeNow() );
//            }
//        
//        String strWhrLast = this.lstWhrCmps.removeLast();
//        this.lstWhrCmps.forEach( s -> bufQuery.append(s + " AND ") );
//        bufQuery.append(strWhrLast);
//        this.lstWhrCmps.add(strWhrLast);
//        
//        return bufQuery.toString();
//    }
    
//    /**
//     * <p>
//     * Creates a decompose data query by decomposing the data source domain axes.
//     * </p>
//     * <p>
//     * The query domain cntHor axes is decomposed into the given number of 
//     * sub-domains, where a new <code>DpDataRequest</code> instance is created
//     * for each sub-domain.  The cntVer (time) axis of each new query object
//     * remains the same as the original request.    
//     * </p> 
//     * <p>
//     * The number of data sources is determined by the argument (number of resultant 
//     * queries) according to the following formula:
//     * <pre>
//     *   cntSources = |{sources}| &divide; cntQueries + (|{sources}| % cntQueries > 0) 1 : 0
//     * </pre>
//     * </p>
//     * 
//     * @param cntQueries    number of cntHor axis (data source) decompositions
//     * 
//     * @return  a new list of <code>DpDataRequest</code> instances composing the decompose request
//     */
//    private List<DpDataRequest> decomposeDomainHorizontally(int cntQueries) {
//        
//        List<DpDataRequest> lstRequests = new LinkedList<>();
//        
//        // Compute the maximum number of sources per query
//        int cntSources = this.lstSelCmps.size() / cntQueries;
//        int intRemainder = this.lstSelCmps.size() % cntQueries;
//        
//        if (intRemainder > 0) 
//            cntSources++;
//        
//        // Create decompose data requests by bucketing data source names
//        int indRqstStart = 0;
//        
//        for (int n=0; n<cntQueries; n++) {
//            int indRqstStop = indRqstStart + cntSources;
//            
//            // If this is the last time through we need to reduce stop index
//            if (indRqstStop > this.lstSelCmps.size())
//                indRqstStop = this.lstSelCmps.size();
//            
//            List<String>    lstRqstSrcs = this.lstSelCmps.subList(indRqstStart, indRqstStop);
//            
//            DpDataRequest rqst = new DpDataRequest(this.enmStrmType, this.insStart, this.insStop, lstRqstSrcs);
////            DpDataRequest rqst = newRequest();
////            rqst.insStart = this.insStart;
////            rqst.insStop = this.insStop;
////            rqst.lstSelCmps.addAll(lstRqstSrcs);
//            
//            lstRequests.add(rqst);
//            
//            indRqstStart = indRqstStop;
//        }
//        
//        return lstRequests;
//    }
//    
//    /**
//     * <p>
//     * Creates a decompose data query request by decomposing the time domain axis.
//     * </p>
//     * <p>
//     * The query domain cntVer axes is decomposed into the given number of 
//     * sub-domains, where a new <code>DpDataRequest</code> instance is created
//     * for each sub-domain.  The cntHor (data source) axis of each new query object
//     * remains the same as the original request.    
//     * </p> 
//     * <p>
//     * The duration of each component request is determined by the argument (number of 
//     * resultant queries) according to the following formula:
//     * <pre>
//     *   durRequest = durTotal &divide; cntQueries + (durTotal - durRequset * cntQueries)
//     * </pre>
//     * Note that due to integer arithmetic the largest remainder can be only 1 nanosecond.
//     * </p>
//     * 
//     * @param cntQueries    number of cntVer axis (time range) decompositions
//     * 
//     * @return  a new list of <code>DpDataRequest</code> instances composing the decompose request
//     */
//    private List<DpDataRequest> decomposeDomainVertically(int cntQueries) {
//        
//        List<DpDataRequest> lstRequests = new LinkedList<>();
//        
//        // Compute the time duration for each request
//        Duration    durTotal = Duration.between(this.insStart, this.insStop);
//        Duration    durRequest = durTotal.dividedBy(cntQueries);
//        Duration    durRemain = durTotal.minus(durRequest.multipliedBy(cntQueries));
//        
//        // Create decompose queries by divide time range interval
//        Instant insRqstTmStart = this.insStart;
//        
//        for (int n=0; n<cntQueries; n++) {
//            Instant insRqstTmStop = insRqstTmStart.plus(durRequest);
//            
//            if (n == (cntQueries - 1))
//                insRqstTmStop = insRqstTmStop.plus(durRemain);
//            
//            DpDataRequest   rqst = new DpDataRequest(this.enmStrmType, insRqstTmStart, insRqstTmStop, this.lstSelCmps);
////            DpDataRequest   rqst = newRequest();
////            rqst.insStart = insRqstStart;
////            rqst.insStop = insRqstStop;
////            rqst.lstSelCmps.addAll(this.lstSelCmps);
//            
//            lstRequests.add(rqst);
//            
//            insRqstTmStart = insRqstTmStop;
//        }
//        
//        return lstRequests;
//    }
//    
//    /**
//     * <p>
//     * Creates a decompose data query request by decomposing both the data source and time domain axes.
//     * </p>
//     * <p>
//     * The query domain cntHor axes is decomposed into <code>cntQueriesHor</code> 
//     * sub-domains and the query domain cntVer axes is decomposed into 
//     * <code>cntQueriesVer</code> sub-domains.  Thus, the total query domain is divided
//     * into a grid, where the total number of domains is given by the product. 
//     * </p> 
//     * <p>
//     * The domain of each component request is determined by the argument values 
//     * according to the following formula:
//     * <pre>
//     *   cntSources = |{sources}| &divide; cntQueriesHor + (|{sources}| % cntQueriesHor > 0) 1 : 0
//     *   durRequest = durTotal &divide; cntQueriesVer + (durTotal - durRequset * cntQueriesVer)
//     * </pre>
//     * Note that due to integer arithmetic the largest remainder can be only 1 nanosecond.
//     * </p>
//     * 
//     * @param cntQueriesHor number of cntHor axis (data source) decompositions
//     * @param cntQueriesVer number of cntVer axis (time range) decompositions
//     * 
//     * @return  a new list of <code>DpDataRequest</code> instances composing the decompose request
//     */
//    private List<DpDataRequest> decomposeDomainGridded(int cntQueriesHor, int cntQueriesVer) {
//        
//        List<DpDataRequest> lstRequests = new LinkedList<>();
//        
//        // Compute the sizes for each component query
//        //  Horizontal axis - number of sources/query
//        int cntSources = this.lstSelCmps.size() / cntQueriesHor;
//        
//        if (this.lstSelCmps.size() % cntQueriesHor > 0)
//            cntSources++;
//        
//        //  Vertical axis - duration of sub-queries
//        Duration    durTotal = Duration.between(this.insStart, this.insStop);
//        Duration    durRequest = durTotal.dividedBy(cntQueriesVer);
//        Duration    durRemain = durTotal.minus(durRequest.multipliedBy(cntQueriesVer));
//        
//        // Create component requests for grid
//        //  Initialize loops
//        int     indRqstSrcStart = 0;
//        Instant insRqstTmStart = this.insStart;
//        
//        //      Horizontal (source) axis
//        for (int m=0; m<cntQueriesHor; m++) {
//            
//            // Get the last source index for sublist
//            int indRqstSrcStop = indRqstSrcStart + cntSources;
//            if (indRqstSrcStop > this.lstSelCmps.size())
//                indRqstSrcStop = this.lstSelCmps.size();
//            
//            List<String>    lstRqstSrcs = this.lstSelCmps.subList(indRqstSrcStart, indRqstSrcStop);
//            
//            //  Vertical (time) axis
//            for (int n=0; n<cntQueriesVer; n++) {
//                Instant insRqstTmStop = insRqstTmStart.plus(durRequest);
//                
//                // Add in remainder (1 nanosecond at most)
//                if (n == (cntQueriesVer - 1))
//                    insRqstTmStop = insRqstTmStop.plus(durRemain);
//                
//                DpDataRequest   rqst = new DpDataRequest(this.enmStrmType, insRqstTmStart, insRqstTmStop, lstRqstSrcs);
////                DpDataRequest   rqst = newRequest();
////                rqst.insStart = insRqstTmStart;
////                rqst.insStop = insRqstTmStop;
////                rqst.lstSelCmps.addAll(lstRqstSrcs);
//                
//                lstRequests.add(rqst);
//                
//                insRqstTmStart = insRqstTmStop;
//            }
//            
//            // (Re)set loop variables
//            insRqstTmStart = this.insStart;
//            indRqstSrcStart = indRqstSrcStop;
//        }
//        
//        return lstRequests;
//    }
//
//    /**
//     * <p>
//     * Creates a decompose data query request by decomposing both the data source and time domain axes.
//     * </p>
//     * <p>
//     * The grid sub-domain is decomposed horizontally and vertically equally by the argument
//     * value divided by 2.  If the argument is odd, then the time axis receives the remainder.
//     * Thus, the result of this method is equivalent to call the method
//     * <code>
//     * <pre>
//     *      decomposeDomainGridded(cntQueries/2, cntQueries/2 + cntQueries%2);
//     * </pre>
//     * </code>
//     * </p>
//     *  
//     * @param cntQueries    total number of queries returned
//     * 
//     * @return  a new list of <code>DpDataRequest</code> instances composing the decompose request
//     * 
//     * @see #decomposeDomainGridded(int, int)
//     */
//    private List<DpDataRequest> decomposeDomainGridded(int cntQueries) {
//        
////        List<DpDataRequest> lstRequests = new LinkedList<>();
//        
//        // Compute the number of queries in each axes
//        int cntQueriesPerAxis = cntQueries / 2;
//        int cntQueriesHor = cntQueriesPerAxis;
//        int cntQueriesVer = cntQueriesPerAxis;
//        
//        // Add any remainder to the cntVer (time) axis
//        if (cntQueries % 2 > 0) 
//            cntQueriesVer = cntQueriesPerAxis + 1;
//
//        return this.decomposeDomainGridded(cntQueriesHor, cntQueriesVer);
//        
////        // Compute the sizes for each component query
////        //  Horizontal axis - number of sources/query
////        int cntSources = this.lstSelCmps.size() / cntQueriesHor;
////        
////        if (this.lstSelCmps.size() % cntQueriesHor > 0)
////            cntSources++;
////        
////        //  Vertical axis - duration of sub-queries
////        Duration    durTotal = Duration.between(this.insStart, this.insStop);
////        Duration    durRequest = durTotal.dividedBy(cntQueriesVer);
////        Duration    durRemain = durTotal.minus(durRequest.multipliedBy(cntQueriesVer));
////        
////        // Create component requests for grid
////        //  Initialize loops
////        int     indRqstSrcStart = 0;
////        Instant insRqstTmStart = this.insStart;
////        
////        //      Horizontal (source) axis
////        for (int m=0; m<cntQueriesHor; m++) {
////            
////            // Get the last source index for sublist
////            int indRqstSrcStop = indRqstSrcStart + cntSources;
////            if (indRqstSrcStop > this.lstSelCmps.size())
////                indRqstSrcStop = this.lstSelCmps.size();
////            
////            List<String>    lstRqstSrcs = this.lstSelCmps.subList(indRqstSrcStart, indRqstSrcStop);
////            
////            //  Vertical (time) axis
////            for (int n=0; n<cntQueriesVer; n++) {
////                Instant insRqstTmStop = insRqstTmStart.plus(durRequest);
////                
////                if (n == (cntQueriesVer - 1))
////                    insRqstTmStop = insRqstTmStop.plus(durRemain);
////                
////                DpDataRequest   rqst = newRequest();
////                rqst.insStart = insRqstTmStart;
////                rqst.insStop = insRqstTmStop;
////                rqst.lstSelCmps.addAll(lstRqstSrcs);
////                
////                lstRequests.add(rqst);
////                
////                insRqstTmStart = insRqstTmStop;
////            }
////            
////            // (Re)set loop variables
////            insRqstTmStart = this.insStart;
////            indRqstSrcStart = indRqstSrcStop;
////        }
////        
////        return lstRequests;
//    }

    
    //
    // Object Overrides (Debugging)
    //
    
    /**
     * Compares the class type and {<code>{@link #lstSelCmps}, {@link #insStart}, {@link #insStop}</code>} 
     * attributes for equivalence. 
     *
     * @see @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DpDataRequest rqst) {
            return this.lstSelCmps.equals(rqst.lstSelCmps) &&
                    (this.enmStrmType == rqst.enmStrmType) &&
                    this.insStart.equals(rqst.insStart)  &&
                    this.insStop.equals(rqst.insStop);
        }
        
        return false;
    }

    /**
     * Writes out the class type and {<code>{@link #lstSelCmps}, {@link #insStart}, {@link #insStop}</code>} 
     * attributes values. 
     *
     * @see @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer    buf = new StringBuffer(JavaRuntime.getMethodClassName());
        
        buf.append(" contents:\n");
        buf.append("  request UD = " + this.strRqstId + "\n");
        buf.append("  source name(s) = " + this.lstSelCmps + "\n");
        buf.append("  start time = " + this.insStart + "\n");
        buf.append("  stop time = " + this.insStop + "\n");
        buf.append("  preferred stream = " + this.enmStrmType + "\n");

        return buf.toString();
    }
    
}
