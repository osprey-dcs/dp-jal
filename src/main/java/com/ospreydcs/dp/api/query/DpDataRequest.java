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
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.model.AAdvancedApi;
import com.ospreydcs.dp.api.model.AUnavailable;
import com.ospreydcs.dp.api.model.AUnavailable.STATUS;
import com.ospreydcs.dp.grpc.v1.common.DataValue;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest.CursorOperation;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest.QuerySpec;


/**
 * <p>
 * Utility class for constructing snapshot data requests for the <em>Datastore</em>
 * query service.
 * </p>
 * <p>
 * This class is used to create snapshot data requests without direct knowledge
 * of the underlying query mechanism.  The class exposes a set of methods that 
 * can be called to create a specific snapshot data request.  In this fashion the
 * query service interface can be narrowed while still providing a wide range
 * of possible queries.  
 * It also simplifies the use of the query service in that
 * the user is not subject to the breadth of the <em>Datastore</em> query 
 * language.
 * Additionally, modifications to the query service will be seen in this 
 * concrete class, rather than the query interfaces.
 * </p>
 * <p>
 * There are four classes of methods used to create the snapshot data query:
 * <ul>
 * <li>Selection methods - prefixed with <code>select</code>: used to select the
 *     returned data columns within the query results.
 *     </li>
 * <li>Ranges methods - prefixed with <code>range</code>: used to restrict the range
 *     of timestamps within the query results.
 *     </li>
 * <li>Filter methods - prefixed with <code>filter</code>: used to filter the query
 *     results based upon filter conditions.
 *     </li>
 * <li>Page methods - prefixed with <code>page</code>: used to specify parameters of 
 *     returned data pages for asynchronous queries.
 *     </li>
 * </ul>
 * Further details on the use of these methods are given in the <b>NOTES</b> 
 * subsection below.  Also see the method documentation for more particulars on
 * their use.  Note that the page methods are only for asynchronous requests
 * and their use is somewhat advanced.  Reasonable default values are
 * specified in the application properties, which are used whenever the methods
 * are not called.
 * </p>
 * <p>
 * It is important to note that an unconfigured data request, that is, the request 
 * specified immediately upon instance creation, always represents the "open
 * query."  Specifically, a newly created <code>DpDataRequest</code> instance always
 * selects for all the snapshot data within the <em>Datastore</em>.  By calling
 * the methods listed above, the resulting query request is "restricted", either
 * by selecting the specific EPICS process variables (PVs) to be included, reducing
 * the range of allowable timestamps, or filtering on the PV values or other
 * attributes of the query.
 * </p>    
 * <p> 
 * Once a class instance is configured using the available restrictor 
 * method calls, a properly formatted <em>Datastore</em> Query Language</code> (DQL)
 * query string is created from the configuration.  The DQL statement can be
 * packaged into the appropriate gRPC message type using a <code>build</code>
 * method (used by the query service).
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li> 
 * Call the {@link #newRequest()} method to return a <em>Datastore</em> data 
 * request initialized to the "open request" query.
 * </li>
 * <li> 
 * A new <code>DpDataRequest</code> instance will create the open query, that is, it creates
 * the query which returns all snapshot data in the <em>Datastore</em>.
 * </li>
 * <li> 
 * Calling a <code>select</code> method restricts the query to given PV names or
 * special columns.
 * However, further calls to <code>select</code> methods opens the query to additional
 * PV names.
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
 * Call the <code>{@link #pageSize(Integer)}</code> method to set the size
 * (i.e., number of data rows) of the data pages returned from the <em>Datastore</em>
 * during asynchronous data streaming.  <em>This is a performance parameter</em>.  
 * This value defaults to that in the <code>AppProperties</code> configuration.
 * </li>
 * <li>
 * Call the <code>{@link #pageStartIndex(Integer)}</code> to set the index of
 * the first data page returned by the <em>Datastore</em>.  
 * <em>Page indices are 1-based</em>, (i.e., start with index 1).
 * Setting this value to 0 instructs the <em>Datastore</em> to
 * stream all data pages for the given query request (this is the default value).
 * <b>Use at your own risk</b> as setting this value
 * will produce a resultant query request that returns only the tail of
 * the full query (from given index to the last data page).
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Sep 23, 2022
 *
 * @see AAdvancedApi
 */
@AAdvancedApi(status=AAdvancedApi.STATUS.DEVELOPMENT, note="DP Query Service gRPC API is still under development")
public final class DpDataRequest {

    //
    //  Application Resources
    //
    
    /** The default API library configuration */
    private static final DpApiConfig CFG_DEFAULT = DpApiConfig.getInstance();
    
    
    //
    // Class Constants
    //
    
    /** The start of this time epoch (Jan 1, 1970) */
    private static final Instant                INS_EPOCH = Instant.EPOCH;
    
    /** Inception instant of the Data Platform archive */
    private static final Instant                INS_INCEPT = Instant.parse( CFG_DEFAULT.archive.inception );
    
    /** The default page size to use when creating gRPC paginated queries */
    private static final Integer                SZ_PAGES = CFG_DEFAULT.query.pageSize;
    
    
    //
    // Instance Attributes
    //
    
    
    /** Flag indicating a paging cursor request */
    private boolean bolCursor = false;
    
    /** The size of a data page, that is, the number of data rows per page */
    private int szPage = SZ_PAGES;
    
    /** The initial index of first page to return, that is, the starting page number */
    @SuppressWarnings("unused")
    private int indStartPage = 0;

    
    /** The time range start instant */
    private Instant insStart = INS_INCEPT;
    
    /** The time range stop instant */
    private Instant insStop = Instant.now();
    

    //
    // Instance Resources
    //
    
    /** "SELECT" component of the query - identifies data source names and attributes */
    private final LinkedList<String>  lstSelCmps = new LinkedList<String>();
    
    /** "WHERE" components of the query - data filters to restrict time interval  */
    private final LinkedList<String>  lstWhrCmps = new LinkedList<String>();
    
    
    //
    // Creators and Constructors
    //
    
    /**
     * <p>
     * Creates a new <code>DpDataRequest</code> instance for creating snapshot
     * data requests from the <em>Datastore</em>.
     * </p>
     * <p>
     * Note that the returned data request will create the 
     * "open query", which requests all snapshot currently within the 
     * <em>Datastore</em> since its inception.
     * Use the "selection" and "restrictor" methods to narrow the data request
     * results based upon specific PV names, timestamps ranges, and other 
     * data filters.
     * </p>
     * <p>
     * Use the <code>{@link #pageSize(Integer)}</code> to performance tweak
     * the size of the data pages in the <em>Datastore</em> data stream when
     * using paged data tables (in particular, for asynchronous data requests).
     * The default page size is given in the <i>application.yml</i> file.
     * </p>
     * <p>
     * Use the <code>{@link #pageStartIndex(Integer)}</code> tell the 
     * <em>Datastore</em> to send the specific page index for the given
     * query.  For asynchronously streamed data (<code>PaginatedRequest</code>) 
     * this instructs the <em>Datastore</em> to send data pages from the given 
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
    public static DpDataRequest newRequest() {
        DpDataRequest bldr = new DpDataRequest();
        
        return bldr;
    }
    
    /**
     * Creates a new, open request instance of <code>DpDataRequest</code>.
     *
     */
    private DpDataRequest() {
        this.reset();
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates a <em>Datastore</em> gRPC <code>PaginatedRequest</code> object 
     * based upon the history of calls to the restrictor methods.
     * </p>
     * <p>
     * Note tha <code>PaginatedRequest</code> gRPC messages are specific to dynamic
     * data requests, that is, rather than wait for the full request to return
     * from the <em>Datastore</em> a data stream is initiated.  The stream continues
     * as a separate thread and requested snapshot data is acquired in <i>pages</i>. 
     * </p>
     * <p>
     * This is essentially a convenience method
     * which calls <code>{@link #buildRequest()}</code> and packages the results 
     * into a gRPC <code>PaginatedRequest</code> object.
     * </p>
     * <p>  
     * <h2>NOTES:</h2>
     * <ul>
     * <li> If no <code>range</code> methods were called then the returned query
     * defaults to the inception time defined in the applications properties.
     * </li>
     * <li> If no <code>select</code> methods were called then the returned 
     * query defaults to all PV names in the <em>Datastore</em>.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  properly formatted <em>Datastore</em> DQL gRPC request message
     * 
     * @see DpDataRequest#buildRequest()
     * @see DpDataRequest#buildDqlQueryString()
     */
    public QueryRequest buildQueryRequest() {
        
        
        // Create a query request from the given request
        QueryRequest.Builder bldrRqst = QueryRequest.newBuilder();
        
        // Select appropriate request type
        if (this.bolCursor) 
            bldrRqst.setCursorOp(this.buildCursorRequest());
        else
            bldrRqst.setQuerySpec(this.buildQuerySpec());
        
        QueryRequest msgRqst = bldrRqst.build();
        
        return msgRqst;
    }
    
    /**
     * <p>
     * Returns the query builder to its initial state.  
     * </p>
     * <p>
     * <ul>
     * <li>Clears all previous calls to restrictors.</li>
     * <li>Sets the page size to the application default (in properties).</li>
     * <li>Sets the start page index to 0 (send all data pages).</li>
     * </ul>
     */
    public void reset() {
        this.bolCursor = false;
        this.indStartPage = 0;
        this.szPage = SZ_PAGES;

        this.insStart = INS_INCEPT;
        this.insStop = Instant.now();
        this.lstSelCmps.clear();
        
        this.lstWhrCmps.clear();
    }
    
    
    // 
    // Cursor Query Configuration
    //
    
    /**
     * <p>
     * Sets the size of the data pages to return from the <em>Datastore</em>
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
     * @param szPage page size of a data block returned from the <em>Datastore</em> (in rows)
     */
    @AUnavailable(status=STATUS.UNDER_REVIEW)
    public void pageSize(Integer szPage) {
        this.szPage = szPage;
    }
    
    /**
     * <p>
     * Sets the page index of the data request.
     * We assume that a previous request has already been sent to the
     * <em>Datastore</em> to initiate a data stream.
     * Thus, the resultant query built by the <code>DpDataRequest</code>
     * contains only a request for this page under the assumption that
     * it applies to a previous snapshot data request.
     * </p>
     * <p> 
     * Defaults to index 0 which indicates that the 
     * <em>Datastore</em> should send all pages of the resultant query
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
    public void pageStartIndex(Integer indStartPage) {
        this.indStartPage = indStartPage;
    }
    
    
    //
    // Time Intervals
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
     * Restrict the request to data with timestamps occurring at or before the
     * given time instant.
     * </p>
     * <p>
     * Restricts the range of viable timestamps to the time interval starting
     * from the <em>Datastore</em> inception up to and including the given 
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
     * Restrict the request to data with timestamps occurring at or after the 
     * given time offset.
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
    public void rangeOffset(int lngTimeOffset, TimeUnit tuUnit) throws IllegalArgumentException {
        if (lngTimeOffset > 0)
            throw new IllegalArgumentException("Relative times must be negative");
        
        // Convert offset to a positive nanosecond duration 
        //  Subtract from now instant to yield new start time instant
        long    lngDur = -lngTimeOffset;
        long    lngNsecs = TimeUnit.NANOSECONDS.convert(lngDur, tuUnit);
        
        this.insStart = Instant.now().minusNanos(lngNsecs);
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
    public void selectSources(List<String> lstNames) {
        
        this.lstSelCmps.addAll(lstNames);
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
     * The snapshot data provider UID is obtained from the <em>Datastore</em>
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
     * Snapshot UIDs are obtained from the <em>Datastore</em>
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
    

    //
    // Private Methods
    //
    
    /**
     * <p>
     * Creates a Data Platform <code>CursorRequest</code> object based upon the history 
     * of calls page methods.
     * </p>
     * <p>  
     * <h2>NOTES:</h2>
     * <ul>
     * <li> If no <code>range</code> methods were called then the returned query
     * defaults to the inception time defined in the applications properties.
     * </li>
     * <li> If no <code>select</code> methods were called then the returned 
     * query defaults to all PV names in the <em>Datastore</em>.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  new <code>CursorOperation</code> message configured by history 
     */
    private CursorOperation buildCursorRequest() {
        
//        CursorOperation.Builder bldrRqst = CursorRequest.newBuilder();
//        bldrRqst.setNumBuckets(this.szPage);
////        bldrRqst.setPage(this.indStartPage);  // Unavailable
//        CursorRequest rqst = bldrRqst.build();
        
        CursorOperation rqst = CursorOperation.CURSOR_OP_NEXT;
        
        return rqst;
    }
    
    /**
     * <p>
     * Creates a <em>Datastore</em> gRPC <code>Request</code> object based upon the history 
     * of calls to the restrictor methods.  This is essentially a convenience method
     * which calls <code>{@link #buildQueryString()}</code> and packages the results into 
     * a gRPC <code>Request</code> object.
     * <h2>NOTES:</h2>
     * <ul>
     * <li> All references to paginated data is ignored.  Specifically calls
     * to <code>{@link #pageSize(Integer)}</code> and <code>{@link #pageStartIndex(Integer)}</code>
     * have no relevance to the returned result.
     * </li>
     * <li> If no <code>range</code> methods were called then the returned query
     * defaults to the inception time defined in the applications properties.
     * </li>
     * <li> If no <code>select</code> methods were called then the returned 
     * query defaults to all PV names in the <em>Datastore</em>.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  properly formatted <em>Datastore</em> DQL request
     * 
     * @see DpDataRequest#buildQueryString()
     */
    private QuerySpec buildQuerySpec() {
        QuerySpec.Builder bldrQry = QuerySpec.newBuilder();
        bldrQry.setStartTime( ProtoMsg.from(this.insStart) );
        bldrQry.setEndTime( ProtoMsg.from( this.insStop) );
        bldrQry.addAllColumnNames(this.lstSelCmps);
        QuerySpec qry = bldrQry.build();
        
        return qry;
    }
    
    /**
     * Creates and returns a SELECT query component which selects for 
     * all PV names in the <em>Datastore</em>.
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
     * inception date/time of the <em>Datastore</em> specified in the application
     * properties.
     * </p>
     * <p>
     * <h2>UPDATE:</h2>
     * The method now uses the start of the current time epoch as the inception
     * data of the <em>Datastore</em>.  No exceptions are thrown.
     * </p>
     * 
     * @return  default range query component
     */
    private String createWhereCmpDsInception() {
//        String strRange = CFG_DEFAULT.getInception();
//        if ( strRange==null )
//            throw new DsGrpcException("No inception time defined in Datastore properties");

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
     * currently in the Datastore.
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
//     * Creates the <em>Datastore</em> query language (DQL) string based upon 
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
//     * query defaults to all PV names in the <em>Datastore</em>.
//     * </li>
//     * <li>
//     * If the <code>{@link #pageSize(Integer)}</code> method was not called
//     * then the returned query defaults the page size specified in the
//     * <i>application.yml</i> configuration file.
//     * </li>
//     * <li>
//     * If the <code>{@link #pageStartIndex(Integer)}</code> method was not called
//     * then the returned query defaults to the zero index indicating that
//     * the <em>Datastore</em> send all data indicated in the query request.
//     * </li>
//     * </ul>
//     * </p>
//     * 
//     * @return  properly formatted <em>Datastore</em> DQL snapshot data request query
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
    
}
