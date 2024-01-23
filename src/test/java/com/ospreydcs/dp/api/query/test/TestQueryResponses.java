/*
 * Project: dp-api-common
 * File:	TestQueryResponses.java
 * Package: com.ospreydcs.dp.api.query.test
 * Type: 	TestQueryResponses
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
 * @since Jan 14, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

/**
 * <p>
 * Utility class for maintaining simulated Query Service results sets.
 * </p>
 * <p>
 * The utility can provide result sets that mimic that of an actual Query Service data request.
 * The utility maintains persistent lists of <code>QueryResponse</code> instances within file storage.
 * These lists can be recovered to mimic query response streams used for unit testing.
 * </p>
 * <p>
 * The utility is used to manage persistent Query Service responses which would result from 
 * querying the Data Platform testing archive.  
 * The data within the result sets is taken from data files that are included with the
 * project test resources.
 * </p>
 * <p>
 * The utility also provides methods that create the test data from an actual Query Service
 * data request and saves it to persistent file storage.  To use this method an active Query 
 * Service must be deployed and attached to the Data Platform test archive.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Jan 14, 2024
 *
 */
public class TestQueryResponses {
    
    
    //
    // Class Types
    //

    /**
     * <p>
     * Enumeration of available single query types within the managed set.
     * </p>
     */
    public static enum SingleQueryType {

        /**
         * Query for a single data source over a limited time-range duration.
         * 
         * @see TestQueryResponses#QREC_SNGL
         */
        SINGLE(TestQueryResponses.QREC_SNGL),
        
        /**
         * Query for many data sources over a limited time-range duration.
         * 
         * @see TestQueryResponses#QREC_WIDE
         */
        WIDE(TestQueryResponses.QREC_WIDE),
        
        /**
         * Query for multiple data sources over the entire time-range duration
         * 
         * @see TestQueryResponses#QREC_LONG
         */
        LONG(TestQueryResponses.QREC_LONG);
        
        /** The record instance for the query type */
        private TestQueryRecord recQuery;
        
        /** Enumeration constant constructor */
        private SingleQueryType(TestQueryRecord rec) { this.recQuery = rec; }
        
        /** @return the query record instance for this query type */
        public TestQueryRecord    getQueryRecord() { return this.recQuery; };
    }
    
    
    /**
     * <p>
     * Enumeration of available composite query types within the managed set.
     * </p>
     */
    public static enum CompositeQueryType {
        
        /**
         * Composite query for a horizontal query domain decomposition (by data sources).
         * 
         * @see TestQueryResponses#CQRECS_HOR
         */
        HORIZONTAL(TestQueryResponses.CQRECS_HOR),
        
        /**
         * Composite query for a vertical query domain decomposition (by time range).
         * 
         * @see TestQueryResponses#CQRECS_VER
         */
        VERTICAL(TestQueryResponses.CQRECS_VER),
        
        /**
         * Composite query for a grid query domain decomposition (by data sources and time range).
         * 
         * @see TestQueryResponses#CQRECS_GRID
         */
        GRID(TestQueryResponses.CQRECS_GRID);
        
        /** The composite of record instances for the query type */
        private TestQueryCompositeRecords   recsQuery;
        
        /** Enumeration constant constructor */
        private CompositeQueryType(TestQueryCompositeRecords recs) { this.recsQuery = recs; };

        /** @return the number of sub-queries in this composite query */
        public int                  getQueryCount()    { return this.recsQuery.getQueryCount(); };
        
        /**
         * Returns the query record instance for this constant by composite index.
         * 
         * @param index  index into the composite record vector (< {@link #getQueryCount()})
         * 
         * @return the query record instance for this query type and given index
         *  
         * @throws IndexOutOfBoundsException    index is >= {@link #getQueryCount()}
         */
        public TestQueryRecord      getQueryRecord(int index) throws IndexOutOfBoundsException { 
            return this.recsQuery.getQueryRecord(index); 
        };
    }
    
    
    
    //
    // Application Resources
    //
    
//    /** The query configuration for DP API testing */ 
//    private static final DpApiTestingConfig.TestQuery    CFG_QUERY = DpApiTestingConfig.getInstance().testQuery;
    
    //
    // Class Resources
    //
    
    /** class logger - logs persistence and query operations for debugging */
    private static final Logger LOGGER = LogManager.getLogger();
    
    
    //
    // Concurrent Results Sets common parameters
    //
    
    /** concurrent results set - number of threads */
    public static final int     CONC_CNT_THRDS = 5;
    
    /** concurrent results set - number of data sources per thread */
    public static final int     CONC_CNT_SRCS_PER_THRD = 10;
    
    /** concurrent results set - time range duration of query (in seconds) */
    public static final long    CONC_LNG_DURATION = 5L;
    
    /** concurrent results set - persistent storage file name */
    public static final String  CONC_STR_FILENAME_PREFIX = "query-results-concurrent";
    
    
    //
    // Available Queries and Responses
    //
    
    /** 
     * Query definition record for "single" query  
     * <p>
     * 1 data source(s)<br/>
     * 10 second(s) duration
     */
    public static final TestQueryRecord     QREC_SNGL = new TestQueryRecord(
                                                "queryresults-single-single.dat", 
                                                1, 
                                                0, 
                                                10L, 
                                                0L);
    
    /** 
     * Query definition record for "wide" query  
     * <p>
     * 100 data source(s)<br/>
     * 5 second(s) duration
     */
    public static final TestQueryRecord     QREC_WIDE = new TestQueryRecord(
                                                "queryresults-single-wide.dat", 
                                                100, 
                                                0, 
                                                5L, 
                                                0L);
    
    /** 
     * Query definition record for "long" query  
     * <p>
     * 5 data source(s)<br/>
     * 60 second(s) duration
     */
    public static final TestQueryRecord     QREC_LONG = new TestQueryRecord(
                                                "queryresults-single-long.dat", 
                                                5, 
                                                0, 
                                                60L, 
                                                0L);

    
    /**
     * Query definition records for "horizontal" composite query
     * <p>
     * 5 sub-queries <br/>
     * 50 data source(s) total - 10 per sub-query <br/>
     * 5 second(s) duration
     */
    public static final TestQueryCompositeRecords   CQRECS_HOR = new TestQueryCompositeRecords(
                                                        TestQueryCompositeRecords.CompositeStrategy.HORIZONTAL,
                                                        5,
                                                        50,
                                                        5L,
                                                        "queryresults-composite-horizontal");
    
    /**
     * Query definition record for "vertical" composite query
     * <p>
     * 5 sub-queries <br/>
     * 10 data source(s) <br/>
     * 5 second(s) total duration - 1 second per sub-query 
     */
    public static final TestQueryCompositeRecords   CQRECS_VER = new TestQueryCompositeRecords(
                                                        TestQueryCompositeRecords.CompositeStrategy.VERTICAL,
                                                        5,
                                                        50,
                                                        5L,
                                                        "queryresults-composite-vertical");
    
    /**
     * Query definition record for "vertical" composite query
     * <p>
     * 10 sub-queries <br/>
     * 50 data source(s) - 10 per sub-query<br/>
     * 5 second(s) total duration - 1 second per sub-query 
     */
    public static final TestQueryCompositeRecords   CQRECS_GRID = new TestQueryCompositeRecords(
                                                        TestQueryCompositeRecords.CompositeStrategy.VERTICAL,
                                                        10,
                                                        50,
                                                        5L,
                                                        "queryresults-composite-gridded");
    
    
    /** 
     * Vector of data and persistent storage locations for "concurrent" results sets 
     * - initialized in static block 
     * <p>
     * {@value #CONC_CNT_THRDS} results sets (i.e., number of concurrent query threads) <br/>
     * {@value #CONC_CNT_SRCS_PER_THRD} separate data sources per results set<br/>
     * {@value #CONC_LNG_DURATION} second(s) duration each results set (same time range)
     */
    public static final Vector<TestQueryRecord> VEC_QRECS_CONC = new Vector<>(CONC_CNT_THRDS * CONC_CNT_SRCS_PER_THRD);

    
    /** collection of all query results in class - used for persistent storage */
    public static final Collection<TestQueryRecord>    SET_QRECS_ALL;
    
//    /** data and properties of the wide query results data set */
//    public static final TestQueryRecord<QueryData> RESULTS_DATA_WIDE = new TestQueryRecord<QueryData>("queryresults-data-wide.dat", 0, 100, 10L);
//    
//    /** data and properties of the wide query results data set */
//    public static final TestQueryRecord<QueryData> RESULTS_DATA_LONG = new TestQueryRecord<QueryData>("queryresults-data-wide.dat", 0, 10, 60L);

    
//    /** The single instance of the <code>TestQueryService</code> - only created if needed (is never shut down) */
//    private static TestQueryService qsTestArchive = null;
    

    /**
     * <p>
     * Static initialization of class resources.
     * </p>
     * <p>
     * Creates the <code>TestQueryRecord</code> record for the parallel result sets and adds them
     * to the static vector of parallel records.
     * </p>
     */
    static {
        
        // Create the parallel results sets
        for (int n=0; n<CONC_CNT_THRDS; n++) {
            String  strFileName = CONC_STR_FILENAME_PREFIX + "-" + Integer.toString(n) + ".dat";
            int     indSourceFirst = n * CONC_CNT_SRCS_PER_THRD;
            
            TestQueryRecord rec = new TestQueryRecord(
                    strFileName,
                    CONC_CNT_SRCS_PER_THRD,
                    indSourceFirst,
                    CONC_LNG_DURATION,
                    0L
                    );
            
            VEC_QRECS_CONC.add(rec);
        }
        
        // Create collects of all results sets and add them all
        SET_QRECS_ALL = new LinkedList<>();
        
        SET_QRECS_ALL.add(QREC_SNGL);
        SET_QRECS_ALL.add(QREC_WIDE);
        SET_QRECS_ALL.add(QREC_LONG);
        
        SET_QRECS_ALL.addAll(CQRECS_HOR.getQueryRecordsAll());
        SET_QRECS_ALL.addAll(CQRECS_VER.getQueryRecordsAll());
        SET_QRECS_ALL.addAll(CQRECS_GRID.getQueryRecordsAll());
        
//        SET_QRECS_ALL.addAll(VEC_QRECS_CONC);
    }
    
    
    //
    // Public Operations
    // 
    
    /**
     * <p>
     * Recover and return the result set for the given single query.
     * </p>
     * <p>
     * The results sets for all the supported single query cases are available here.
     * </p>
     * <p>
     * The method tries <em>by all means</em> to recover the results set, even attempting
     * an actual Query Service operation if persistent storage is not available.
     * If all attempts to acquire the results set fail then a <code>null</code> value
     * is return.  Cause(s) of the failure is available in the class logs.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * To expedite results set recovery the method <code>{@link #storePersistentData()}</code>
     * should be invoked once, after the Data Platform test archive is established and
     * a <code>{@link TestQueryService}</code> is able to connect.  Afterwards, this utility
     * class can load results sets into heap memory from persistent storage whenever necessary.
     * </p>
     *  
     * @param enmType   enumeration constant specifying the desired single query type
     * 
     * @return  the results set for the desired query type
     * 
     * @see SingleQueryType
     */
    public static List<QueryResponse>   queryResults(SingleQueryType enmType) {
        TestQueryRecord rec = enmType.getQueryRecord();
        
        return TestQueryResponses.recoverQueryResults(rec);
    }
    
    /**
     * <p>
     * Recovers and returns the results sets for the given composite query.
     * </p>
     * <p>
     * All the results sets for the composite query cases are available here.  The
     * composite cases assumes that a larger query operation was divided into multiple
     * query operations each running on a separate thread, each querying for a subset of
     * the original data source set.
     * </p>
     * <p>
     * The method tries <em>by all means</em> to recover the results set, even attempting
     * an actual Query Service operation if persistent storage is not available.
     * If all attempts to acquire the results set fail then a <code>null</code> value
     * is return.  Cause(s) of the failure is available in the class logs.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * To expedite results set recovery the method <code>{@link #storePersistentData()}</code>
     * should be invoked once, after the Data Platform test archive is established and
     * a <code>{@link TestQueryService}</code> is able to connect.  Afterwards, this utility
     * class can load results sets into heap memory from persistent storage whenever necessary.
     * </p>
     * 
     * @param enmType   enumeration constant specifying the desired composite query type
     * @param index index of the component query in the composite query set
     * 
     * @return  the results set of the given (indexed) sub-query within the composite query
     * 
     * @throws IndexOutOfBoundsException    the index was >= the number of sub-queries
     * 
     * @see CompositeQueryType
     */
    public static List<QueryResponse>   queryResults(CompositeQueryType enmType, int index) throws IndexOutOfBoundsException {
        
        // Check index value
        if (index >= enmType.getQueryCount())
            throw new IndexOutOfBoundsException("Given index value = " + index + " >= number of available results sets " + enmType.getQueryCount());
        
        // Get the composite query record
        TestQueryRecord rec = enmType.getQueryRecord(index);
        
        return TestQueryResponses.recoverQueryResults(rec);
    }
    
    /**
     * <p>
     * Shuts down any <code>{@link TestQueryService}</code> instance that may have been created.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * This method should always be invoked when the managed query responses are no longer needed.
     * Any gRPC resource allocations required to use these managed query responses is transparent
     * to the user and calling this method releases them.
     * There is no cost to this method invocation if no gRPC resources where needed.
     * 
     * @return  <code>true</code> if gRPC resources were allocated and released,
     *          <code>false</code> if no gRPC resources were ever allocated
     */
    public static boolean shutdown() {
        return TestQueryRecord.shutdownQueryServiceApi();
    }
    
    /**
     * <p>
     * Stores results sets to persistent data files for all managed query response data.
     * </p>
     * <p>
     * The results sets for each maintained query response are queried then stored into the
     * persistent data file for later recovery.  That is, all <code>{@link TestQueryRecord}</code> 
     * instances in <code>{@link #SET_QRECS_ALL}</code> are processed for their query operation
     * then their storage operation, respectively.
     * </p>
     * <p>
     * A value <code>true</code> is returned if all operations were successful for all managed 
     * query responses.  A returned value <code>false</code> indicates that at least one operation
     * failed, but the query/storage process was able to continue for the remaining query records.
     * The specific record(s) causing the failure are available in the logs.
     * If an exception occurs the query/storage process stops at the offending record and the
     * remaining records are left unprocessed.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Ideally this method should be called once after establishing the Data Platform test archive.
     * It loads all simulated query results sets into persistent storage so that the Query Service
     * is no longer required and the managed data of this utility class can be accessed off line.
     * </li>
     * <br/>
     * <li>
     * If the method returns with a value <code>true</code> it indicates that all result sets are 
     * currently available and all persistent data files have been created.
     * </li>
     * <br/>
     * <li>
     * The <code>{@link #shutdown()}</code> method has already been called if the method
     * returns <code>true</code> since the <code>{@link TestQueryService}</code> is no longer needed.
     * </li>
     * </ul>
     * </p>
     * 
     * @return  <code>true</code> if results sets for all query records were successfully queried and written to storage,
     *          <code>false</code> if at least one query operation or file storage operation failed
     * 
     * @throws DpGrpcException          Unable to establish TestQueryService connection
     * @throws FileNotFoundException    Unable to create/open output file
     * @throws IOException              Unable to write data (or object stream header) to output file
     */
    public static boolean storePersistentData() throws DpGrpcException, FileNotFoundException, IOException {
        
        boolean bolSuccess = true;
        
        for (TestQueryRecord rec : TestQueryResponses.SET_QRECS_ALL) {
            
            if (!rec.performQuery()) {
                LOGGER.error("{}: query operation failed for record {}.", JavaRuntime.getCallerName(), rec.strFileName());
                
                bolSuccess = false;
            }
            
            if (!rec.storeQueryResults()) {
                LOGGER.error("{}: results set not available for storage operation in record {}.", JavaRuntime.getCallerName(), rec.strFileName());
                
                bolSuccess = false;
            }
        }

        if (bolSuccess)
            TestQueryResponses.shutdown();
        
        return bolSuccess;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Attempts to recover the results set for the given managed query record.
     * </p>
     * <p>
     * The operation <code>{@link TestQueryRecord#recoverQueryResults()</code> is invoked
     * on the argument to recover the results set associated with the argument.
     * </p>
     * <p>
     * This method catches all exceptions thrown by 
     * <code>{@link TestQueryRecord#recoverQueryResults()</code> and logs them to the
     * class logger as errors.  This behavior allows users to invoke the 
     * <code>queryResults(...)</code> methods without implementing exception handling
     * while still recording all exceptions into the log for diagnosis and debugging.
     * </p>
     * 
     * @param rec   target query data record whose query response is to be recovered
     * 
     * @return  the results set for the given managed query data record, 
     *          or the value <code>null</code> if any errors or exceptions occurred (see log)
     */
    private static List<QueryResponse>  recoverQueryResults(TestQueryRecord rec) {
        
        try {
            List<QueryResponse> lstRspMsgs = rec.recoverQueryResults();
            
            if (lstRspMsgs == null)
                LOGGER.error("{}: the {} recover query results operation returned null for record {}.", 
                        JavaRuntime.getCallerName(), 
                        TestQueryRecord.class.getSimpleName(), 
                        rec.strFileName());
            
            return lstRspMsgs;
            
        } catch (IOException e) {
            LOGGER.error("{}: the {} recover query results operation threw IOException for record {}", 
                    JavaRuntime.getCallerName(), 
                    TestQueryRecord.class.getSimpleName(), 
                    rec.strFileName());
            
        } catch (ClassNotFoundException e) {
            LOGGER.error("{}: the {} recover query results operation threw ClassNotFoundException for record {}", 
                    JavaRuntime.getCallerName(), 
                    TestQueryRecord.class.getSimpleName(), 
                    rec.strFileName());
            
        } catch (DpGrpcException e) {
            LOGGER.error("{}: the {} recover query results operation threw DpGrpcException for record {}", 
                    JavaRuntime.getCallerName(), 
                    TestQueryRecord.class.getSimpleName(), 
                    rec.strFileName());
            
        }
        
        return null;
    }

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Prevent construction of <code>TestQueryResponses</code> instances.
     * </p>
     *
     */
    private TestQueryResponses() {
    }
    
//  /**
//  * <p>
//  * Record containing properties describing supported Query Service test queries, and 
//  * fields for maintaining the result sets, and operations for query and persistence.
//  * </p>
//  * <p>
//  * Record contains properties for creating a test query with the 
//  * <code>{@link TestDpDataRequestGenerator}</code> utility.  Query result sets can be 
//  * actively maintained in the field <code>{@link #lstResults}</code> and be 
//  * recovered (i.e., either through persistence or directly through the Query Service)
//  * with operation <code>{@link #recoverQueryResults()}</code>.
//  * Record contains file name where results can be stored and recovered for persistence.  
//  * The record also performs various operations associated with the query, its results set, 
//  * and persistence.
//  * </p>
//  * <p>
//  * Thus, the act of creating a <code>TestQueryRecord</code> record both defines a specific
//  * QueryService query (against the test archive) and provides access to the query's 
//  * results set. Additionally, the <code>{@link #storeQueryResults()}</code> method
//  * can store the recovered results set to a data file for persistence.
//  * Saving the results sets to persistent file storage provides future testing availability 
//  * without requiring an active Query Service.  
//  * </p>
//  * <p>
//  * <h2>NOTES:</h2>
//  * The record class maintains a static <code>{@link TestQueryService}</code> instance
//  * which is created if necessary (i.e., if an actual query is performed to create the
//  * results set).  <em>This instance should be shut down when no longer needed.</em>
//  * </p>
//  * 
//  * @param   lstResults      ordered list of query results (as if streamed)
//  * @param   strFileName     name of the file where persistent query result data is stored
//  * @param   cntSources      number of data sources (PVs) within query result set
//  * @param   indSourceFirst  index of the first data source within the list of source names
//  * @param   lngDuration     the query time range duration (in seconds)
//  * 
//  * @see TestDpDataRequestGenerator#createRequest(int, int, long)
//  * @see TestQueryService#queryResponseStream(QueryRequest)
//  */
// public static record TestQueryRecord (
//         List<QueryResponse> lstResults,
//         String              strFileName, 
//         int                 cntSources, 
//         int                 indSourceFirst,
//         long                lngDuration
//         ) 
// {
//     
//     //
//     // Record Constants
//     //
//     
//     /** 
//      * Directory path to persistent query results (relative to project).
//      * <pre>
//      *  - used only for storage operation
//      *  - recovery of persistent data is done through class loader resource locator  
//      */
//     public static final String  STR_PATH_DIR_DATA = "src/test/resources/data";
//     
//     
//     //
//     // Record Resources
//     //
//     
//     /** 
//      * The single instance of the <code>TestQueryService</code>.
//      * <pre>
//      *  - only created if needed 
//      *  - should be shutdown when not needed (use <code>shutdownQueryServiceApi()</code>) 
//      */
//     private static TestQueryService qsTestArchive = null;
//     
//     
//     //
//     // Constructors
//     //
//     
//     /**
//      * <p>
//      * Canonical <code>TestQueryRecord</code> constructor.
//      * </p>
//      *
//      * @param   lstResults      ordered list of query results (as if streamed)
//      * @param   strFileName     name of the file where persistent query result data is stored
//      * @param   cntSources      number of data sources (PVs) within query result set
//      * @param   indSourceFirst  index of the first data source within the list of source names
//      * @param   lngDuration     the query time range duration (in seconds)
//      * 
//      * @see TestDpDataRequestGenerator#createRequest(int, int, long)
//      */
//     public TestQueryRecord {
//     }
//     
//     /**
//      * <p>
//      * Non-canonical <code>TestQueryRecord</code> constructor.
//      * </p>
//      * <p>
//      * Creates a the <code>{@link #lstResults}</code> field as an empty 
//      * <code>{@link LinkedList}</code> instance.
//      * </p> 
//      *
//      * @param   lstResults      ordered list of query results (as if streamed)
//      * @param   strFileName     name of the file where persistent query result data is stored
//      * @param   cntSources      number of data sources (PVs) within query result set
//      * @param   indSourceFirst  index of the first data source within the list of source names
//      * @param   lngDuration     the query time range duration (in seconds)
//      * 
//      * @see TestDpDataRequestGenerator#createRequest(int, int, long)
//      */
//     public TestQueryRecord(String strFileName, int cntSources, int indSourceFirst, long lngDuration) {
//         this(new LinkedList<>(), strFileName, cntSources, indSourceFirst, lngDuration);
//     }
//     
//     
//     //
//     // Record Class Operations
//     //
//     
//     /**
//      * <p>
//      * Shuts down the <code>TestQueryService</code> static instance when no longer needed.
//      * </p>
//      * <p>
//      * This method should always be called (as a matter of caution) whenever all records
//      * are no longer needed.  It releases the gRPC resources required for performing
//      * requests to the test archive.
//      * </p>
//      * 
//      * @return  <code>true</code> if <code>{@link #qsTestArchive}</code> service was active and shut down,
//      *          <code>false</code> if the Query Service API was never created 
//      * 
//      */
//     public static boolean shutdownQueryServiceApi() {
//         if (TestQueryRecord.qsTestArchive != null) {
//             TestQueryRecord.qsTestArchive.shutdown();
//             
//             return true;
//         }
//         
//         return false;
//     }
//     
//     
//     //
//     // Record Operations
//     //
//     
//     /**
//      * <p>
//      * Recovers and/or returns the result set associated with this <code>TestQueryRecord</code> record.
//      * </p>
//      * <p>
//      * This method can be used in general to acquire the results set.
//      * If the associated results set is already available (in the the field 
//      * <code>{@link #lstResults}</code>) it is simply returned.
//      * Otherwise, this method attempts to acquire the associated result set by all possible means.
//      * </p>
//      * <p>
//      * The following set of operations are performed, in order, until the results are obtained:
//      * <ol>
//      * <li>First checks if result set is already available within the record, return it if so.</li>
//      * <li>Attempts to load the result set from persistent data file with name given in the record.</li>
//      * <li>Queries the Test Archive with query described by parameters within the record.</li>
//      * </ol>
//      * If the above operations all fail, a <code>null</code> value is returned, or an exception
//      * is thrown during operations.
//      * </p>
//      * 
//      * @return      the result set of the query operation described by record
//      * 
//      * @throws ClassNotFoundException   the data file does not contain a List<QueryResponse> object
//      * @throws IOException              the data file is corrupt
//      * @throws DpGrpcException          unable to establish <code>TestQueryService</code> connection
//      */
//     public List<QueryResponse> recoverQueryResults() throws ClassNotFoundException, IOException, DpGrpcException {
//         
//         // If the query results data is already available return it
//         if (!this.lstResults.isEmpty())
//             return this.lstResults;
//         
//         // Try to load data from existing file
//         if (this.loadQueryResults())
//             return this.lstResults;
//         
//         // Query the test archive to load data
//         if (this.performQuery())
//             return this.lstResults;
//         
//         // Everything failed - return null 
//         return null;
//     }
//     
//     /**
//      * <p>
//      * Convenience method for extracting the <code>QueryData</code> messages from the
//      * results set of <code>QueryResponse</code> messages.
//      * </p>
//      * <p>
//      * This method assumes that the query results set is valid, that is, the query 
//      * was not rejected and each <code>QueryResponse</code> contains a valid 
//      * <code>QueryData</code> message.  
//      * <ul>
//      * <li>If the above conditions hold, the data is extracted and returned as a list.</li>
//      * <li>If the query request was rejected the return list contains empty data messages.</li> 
//      * <li>If a query error occurred the corresponding <code>QueryData</code> message is empty.</li>
//      * <li>If the result set is unavailable a <code>null</code> value is returned.</li>
//      * </ul>
//      * 
//      * @return  an ordered list of extracted <code>QueryData</code> messages from results set,
//      *          or <code>null</code> if result set is  not available 
//      */
//     public List<QueryResponse.QueryReport.QueryData> extractQueryResults() {
//         
//         // Check that result set is available
//         if (this.lstResults.isEmpty())
//             return null;
//         
//         List<QueryResponse.QueryReport.QueryData>   lstDataMsgs = this.lstResults
//                 .stream()
//                 .map(qr -> qr.getQueryReport().getQueryData())
//                 .toList();
//         
//         return lstDataMsgs;
//     }
//
//     /**
//      * Creates and returns a new Query Service <code>QueryRequest</code> Protobuf message that
//      * realizes the query request described by this record.
//      * 
//      * @return  Data Platform Query Service <code>QueryRequest</code> message
//      */
//     public QueryRequest createRequest() {
//         DpDataRequest   dpRqst = TestDpDataRequestGenerator.createRequest(this.cntSources, this.indSourceFirst, this.lngDuration);
//         QueryRequest    msgRqst = dpRqst.buildQueryRequest();
//         
//         return msgRqst;
//     }
//     
//     /**
//      * <p>
//      * Writes the results set to the persistent output file.
//      * </p>
//      * <p>
//      * If the result set  data is present it is written to the output file with name
//      * <code>{@link #strFileName}</code> in directory <code>{@link #STR_PATH_DIR_DATA}</code>.  
//      * The output has the binary format of a Java serialized object of type 
//      * <code>List&lt;QueryResponse&gt;</code>.
//      * </p>
//      * <p>
//      * If the result set data is not present in <code>{@link #lstResults}</code> the method 
//      * returns a value <code>false</code>.
//      * If any errors occur during the operation an exception is thrown.
//      * </p>
//      * 
//      * @return  <code>true</code> if query results were available and written to output file,
//      *          <code>false</code> if no output file was written
//      * 
//      * @throws FileNotFoundException    Unable to create/open output file
//      * @throws IOException              Unable to write data (or object stream header) to output file
//      */
//     public boolean storeQueryResults() throws FileNotFoundException, IOException {
//
//         // Check that result set is available
//         if (this.lstResults.isEmpty())
//             return false;
//
//         // Save the result to an output file
//         //            String  strFilePath = STR_PATH_DIR_DATA + this.strFileName;
//         Path    pathData = Paths.get(STR_PATH_DIR_DATA, this.strFileName);
//         File    fileData = pathData.toFile();
//
//         FileOutputStream    fos = new FileOutputStream(fileData);
//         ObjectOutputStream  oos = new ObjectOutputStream(fos);
//
//         oos.writeObject(this.lstResults);
//
//         oos.close();
//         fos.close();
//
//         return true;
//     }
//
//     
//     //
//     // Support Methods
//     //
//     
//     /**
//      * Loads the query results from the associated data file if it exists.
//      * 
//      * @return  <code>true</code> if the data was successfully loaded into the given record,
//      *          <code>false</code> if the data file is missing
//      * 
//      * @throws IOException              the data file is corrupt
//      * @throws ClassNotFoundException   the data file does not contain a <code>List&lt;QueryResponse&gt;</code> object
//      */
//     @SuppressWarnings("unchecked")
//     private boolean loadQueryResults() throws IOException, ClassNotFoundException {
//         
//         // Use class loader to find data file
//         InputStream         isFile = TestQueryRecord.class.getClassLoader().getResourceAsStream(this.strFileName);
//     
//         // Check that the data file exists
//         if (isFile == null)
//             return false;
//         
//         // Read the data file as a serialized List<QueryResponse> object
//         ObjectInputStream isObject = new ObjectInputStream(isFile);
//     
//         List<QueryResponse> lstRspMsgs = (List<QueryResponse>)isObject.readObject();
//     
//         isObject.close();
//         isFile.close();
//     
//         // Add data to record
//         this.lstResults.addAll(lstRspMsgs);
//     
//         return true;
//     }
//
//     /**
//      * <p>
//      * Obtain the query results from an actual query operation against the test archive.
//      * </p>
//      * <p>
//      * <h2>WARNING:</h2>
//      * This method creates the <code>{@link TestQueryService}</code> instance which is never
//      * shut down in case later needed.  This instance should be shut down somewhere.
//      * </p>
//      * 
//      * @return          <code>true</code> if query was successful and loaded into record,
//      *                  <code>false</code> if query operation failed
//      *                  
//      * @throws DpGrpcException  unable to establish <code>TestQueryService</code> connection
//      * 
//      * @see TestQueryService 
//      */
//     private boolean performQuery() throws DpGrpcException {
//
//         // Create the test Query Service API if necessary
//         if (qsTestArchive == null)
//             qsTestArchive = new TestQueryService();
//         
//         // Perform the query
//         QueryRequest    msgRqst = this.createRequest();
//         
//         List<QueryResponse> lstResults = qsTestArchive.queryResponseStream(msgRqst);
//         
//         // Add results to record
//         this.lstResults.addAll(lstResults);
//         
//         return true;
//     }
// };
//    
//    /**
//     * <p>
//     * Recovers the result set associated with the given <code>TestQueryRecord</code> record.
//     * </p>
//     * <p>
//     * This method attempts to acquire the associated result set by all possible means.
//     * The following set of operations are performed, in order, until the results are obtained:
//     * <ol>
//     * <li>First checks if result set is already available within the record return it if so.</li>
//     * <li>Attempts to load the result set from the data file given in the record.</li>
//     * <li>Queries the Test Archive with the query parameters within the given record.</li>
//     * </ol>
//     * If the above operations all fail a <code>null</code> value is returned, or an exception
//     * is thrown during operations.
//     * </p>
//     * 
//     * @param rec   record describing the query and possible already containing data
//     * 
//     * @return      the result set of the query operation described by record
//     * 
//     * @throws ClassNotFoundException   the data file does not contain a List<QueryResponse> object
//     * @throws IOException              the data file is corrupt
//     * @throws DpGrpcException          unable to establish <code>TestQueryService</code> connection
//     */
//    private static List<QueryResponse> recoverQueryResults(TestQueryRecord rec) throws ClassNotFoundException, IOException, DpGrpcException {
//        
//        // If the query results data is already available return it
//        if (!rec.lstResults.isEmpty())
//            return rec.lstResults;
//        
//        // Try to load data from existing file
//        if (TestQueryResponses.loadQueryResults(rec))
//            return rec.lstResults;
//        
//        // Query the test archive to load data
//        if (TestQueryResponses.performQuery(rec))
//            return rec.lstResults;
//        
//        // Everything failed - return null 
//        return null;
//    }
//    
//    /**
//     * Loads the query results from the associated data file if it exists.
//     * 
//     * @param rec   record containing data file name, and target of loading operation
//     * 
//     * @return  <code>true</code> if the data was successfully loaded into the given record,
//     *          <code>false</code> if the data file is missing
//     * 
//     * @throws IOException              the data file is corrupt
//     * @throws ClassNotFoundException   the data file does not contain a <code>List&lt;QueryResponse&gt;</code> object
//     */
//    @SuppressWarnings("unchecked")
//    private static boolean loadQueryResults(TestQueryRecord rec) throws IOException, ClassNotFoundException {
//        
//        // Use class loader to find data file
//        InputStream         isFile = TestQueryResponses.class.getClassLoader().getResourceAsStream(rec.strFileName);
//    
//        // Check that the data file exists
//        if (isFile == null)
//            return false;
//        
//        // Read the data file as a serialized List<QueryResponse> object
//        ObjectInputStream isObject = new ObjectInputStream(isFile);
//    
//        List<QueryResponse> lstRspMsgs = (List<QueryResponse>)isObject.readObject();
//    
//        isObject.close();
//        isFile.close();
//    
//        // Add data to record
//        rec.lstResults.addAll(lstRspMsgs);
//    
//        return true;
//    }
//
//    /**
//     * <p>
//     * Obtain the query results from an actual query operation against the test archive.
//     * </p>
//     * <p>
//     * <h2>WARNING:</h2>
//     * This method creates the <code>{@link TestQueryService}</code> instance which is never
//     * shut down in case later needed.  This instance should be shut down somewhere.
//     * </p>
//     * 
//     * @param rec       record containing query parameters, and target of query results
//     *  
//     * @return          <code>true</code> if query was successful and loaded into record,
//     *                  <code>false</code> if query operation failed
//     *                  
//     * @throws DpGrpcException  unable to establish <code>TestQueryService</code> connection
//     * 
//     * @see TestQueryService 
//     */
//    private static boolean performQuery(TestQueryRecord rec) throws DpGrpcException {
//
//        // Create the test Query Service API if necessary
//        if (qsTestArchive == null)
//            qsTestArchive = new TestQueryService();
//        
//        // Perform the query
//        DpDataRequest   dpRqst = TestDpDataRequestGenerator.createRequest(rec.cntSources, rec.indSourceFirst, rec.lngDuration);
//        QueryRequest    msgRqst = dpRqst.buildQueryRequest();
//        
//        List<QueryResponse> lstResults = qsTestArchive.queryResponseStream(msgRqst);
//        
//        // Add results to record
//        rec.lstResults.addAll(lstResults);
//        
//        return true;
//    }
    
}
