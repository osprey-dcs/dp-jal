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
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

/**
 * <p>
 * Utility class for maintaining simulated Query Service results sets.
 * </p>
 * <p>
 * The utility can provide result sets that mimic that of an actual Query Service data request.
 * The utility maintains persistent lists of <code>QueryResponse</code> instances within file storage.
 * These lists can be recovered to mimic query response streams used for unit testing
 * when the Query Service is off line.
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
         * Query for a single Data Platform archive "bucket" (1 data source, over ~1 second)
         * 
         * @see TestQueryResponses#QREC_BCKT
         */
        BUCKET(TestQueryResponses.QREC_BCKT),
        
        /**
         * Query for a single data source over an extended time-range duration.
         * 
         * @see TestQueryResponses#QREC_1SRC
         */
        ONE_SOURCE(TestQueryResponses.QREC_1SRC),
        
        /**
         * Query for two data sources over an extended time-range duration
         * 
         * @see TestQueryResponses#QREC_2SRC
         */
        TWO_SOURCE(TestQueryResponses.QREC_2SRC),
        
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
        
//        /**
//         * Query for many data sources over the entire time-range duration
//         * 
//         * @see TestQueryResponses#QREC_BIG
//         */
//        BIG(TestQueryResponses.QREC_BIG);
        
        /** The record instance for the query type */
        private TestQueryRecord recQuery;
        
        /** Enumeration constant constructor */
        private SingleQueryType(TestQueryRecord rec) { this.recQuery = rec; }
        
        /** @return the query record instance for this query type */
        public TestQueryRecord    getQueryRecord() { return this.recQuery; };
    }
    
    
    /**
     * <p>
     * Enumeration of available decompose query types within the managed set.
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
        
        /** The decompose of record instances for the query type */
        private TestQueryCompositeRecord   recsQuery;
        
        /** Enumeration constant constructor */
        private CompositeQueryType(TestQueryCompositeRecord recs) { this.recsQuery = recs; };

        /** @return the number of sub-queries in this decompose query */
        public int                  getQueryCount()    { return this.recsQuery.getQueryCount(); };
        
        /** @return the decompose query record instance for this decompose query type */
        public TestQueryCompositeRecord    getCompositeRecord() { return this.recsQuery; };
        
        /**
         * Returns the query record instance for this constant by decompose index.
         * 
         * @param index  index into the decompose record vector (< {@link #getQueryCount()})
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
    // Class Resources
    //
    
    /** class logger - logs persistence and query operations for debugging */
    private static final Logger LOGGER = LogManager.getLogger();
    
    
    //
    // The managed set of query records defining query results sets
    //
    
    /** 
     * Query definition record for "one bucket" query  
     * <p>
     * 1 data source(s)<br/>
     * 1 second(s) duration
     */
    public static final TestQueryRecord     QREC_BCKT = new TestQueryRecord(
                                                "queryresults-single-bucket.dat", 
                                                1, 
                                                0, 
                                                1_000_000_000L, 
                                                0L);
    
    /** 
     * Query definition record for "1 source" single data source query  
     * <p>
     * 1 data source(s)<br/>
     * 10 second(s) duration
     */
    public static final TestQueryRecord     QREC_1SRC = new TestQueryRecord(
                                                "queryresults-single-1source.dat", 
                                                1, 
                                                0, 
                                                10_000_000_000L, 
                                                0L);
    
    /** 
     * Query definition record for "2 source" two data source query  
     * <p>
     * 2 data source(s)<br/>
     * 2 second(s) duration
     */
    public static final TestQueryRecord     QREC_2SRC = new TestQueryRecord(
                                                "queryresults-single-2source.dat", 
                                                2, 
                                                0, 
                                                2_000_000_000L, 
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
                                                5_000_000_000L, 
                                                0L);
    
    /** 
     * Query definition record for "long" query  
     * <p>
     * 5 data source(s)<br/>
     * 20 second(s) duration (<code>TestDpDataRequestGenerator.LNG_RANGE</code>)
     */
    public static final TestQueryRecord     QREC_LONG = new TestQueryRecord(
                                                "queryresults-single-long.dat", 
                                                5, 
                                                0, 
                                                TestDpDataRequestGenerator.LNG_RANGE, 
                                                0L);
    
    /**
     * Query definition record for "big" query
     * <p>
     * 500 data source(s)<br/>
     * 20 seconds(s) duration (<code>TestDpDataRequestGenerator.LNG_RANGE</code>)
     */
    public static final TestQueryRecord     QREC_BIG = new TestQueryRecord(
                                                "queryresults-single-big.dat",
                                                500,
                                                0,
                                                TestDpDataRequestGenerator.LNG_RANGE,
                                                0L);
    
    /**
     * Query definition record for "huge" query
     * <p>
     * 1000 data source(s)<br/>
     * 20 seconds(s) duration (<code>TestDpDataRequestGenerator.LNG_RANGE</code>)
     */
    public static final TestQueryRecord     QREC_HUGE = new TestQueryRecord(
                                                "queryresults-single-huge.dat",
                                                1000,
                                                0,
                                                TestDpDataRequestGenerator.LNG_RANGE,
                                                0L);
    
    /**
     * Query definition record for half the text archive
     * <p>
     * 2000 data source(s)<br/>
     * 20 seconds(s) duration (<code>TestDpDataRequestGenerator.LNG_RANGE</code>)
     */
    public static final TestQueryRecord     QREC_HALF_SRC = new TestQueryRecord(
                                                "queryresults-single-half_src.dat",
                                                TestDpDataRequestGenerator.CNT_PV_NAMES/2,
                                                0,
                                                TestDpDataRequestGenerator.LNG_RANGE,
                                                0L);
    
    /**
     * Query definition record for half the text archive
     * <p>
     * All data source(s)<br/>
     * 10 seconds(s) duration (<code>TestDpDataRequestGenerator.LNG_RANGE</code>)
     */
    public static final TestQueryRecord     QREC_HALF_RNG = new TestQueryRecord(
                                                "queryresults-single-half_src.dat",
                                                TestDpDataRequestGenerator.CNT_PV_NAMES,
                                                0,
                                                TestDpDataRequestGenerator.LNG_RANGE/2,
                                                0L);
    
    /**
     * Query definition record for entire test archive query
     * <p>
     * 100 data source(s)<br/>
     * 20 seconds(s) duration (<code>TestDpDataRequestGenerator.LNG_RANGE</code>)
     */
    public static final TestQueryRecord     QREC_ALL = new TestQueryRecord(
                                                "queryresults-single-all.data",
                                                TestDpDataRequestGenerator.CNT_PV_NAMES,
                                                0,
                                                TestDpDataRequestGenerator.LNG_RANGE,
                                                0L
                                                );

    
    /**
     * Query definition records for "horizontal" decompose query
     * <p>
     * 5 sub-queries <br/>
     * 50 data source(s) total - 10 per sub-query <br/>
     * 5 second(s) duration
     */
    public static final TestQueryCompositeRecord   CQRECS_HOR = new TestQueryCompositeRecord(
                                                        TestQueryCompositeRecord.CompositeStrategy.HORIZONTAL,
                                                        5,
                                                        50,
                                                        5_000_000_000L,
                                                        "queryresults-decompose-horizontal");
    
    /**
     * Query definition record for "vertical" decompose query
     * <p>
     * 5 sub-queries <br/>
     * 10 data source(s) <br/>
     * 5 second(s) total duration - 1 second per sub-query 
     */
    public static final TestQueryCompositeRecord   CQRECS_VER = new TestQueryCompositeRecord(
                                                        TestQueryCompositeRecord.CompositeStrategy.VERTICAL,
                                                        5,
                                                        50,
                                                        5_000_000_000L,
                                                        "queryresults-decompose-vertical");
    
    /**
     * Query definition record for "vertical" decompose query
     * <p>
     * 10 sub-queries <br/>
     * 50 data source(s) - 10 per sub-query<br/>
     * 5 second(s) total duration - 1 second per sub-query 
     */
    public static final TestQueryCompositeRecord   CQRECS_GRID = new TestQueryCompositeRecord(
                                                        TestQueryCompositeRecord.CompositeStrategy.GRID,
                                                        10,
                                                        50,
                                                        5_000_000_000L,
                                                        "queryresults-decompose-grid");
    
    
    //
    // Class Attributes
    //
    
    /** collection of all query results in class - used for persistent storage */
    public static final Collection<TestQueryRecord>    SET_QRECS_ALL;
    

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
        
        // Create collects of all results sets and add them all
        SET_QRECS_ALL = new LinkedList<>();
        
        SET_QRECS_ALL.add(QREC_BCKT);
        SET_QRECS_ALL.add(QREC_1SRC);
        SET_QRECS_ALL.add(QREC_2SRC);
        SET_QRECS_ALL.add(QREC_WIDE);
        SET_QRECS_ALL.add(QREC_LONG);
//        SET_QRECS_ALL.add(QREC_BIG);
        
        SET_QRECS_ALL.addAll(CQRECS_HOR.getQueryRecordsAll());
        SET_QRECS_ALL.addAll(CQRECS_VER.getQueryRecordsAll());
        SET_QRECS_ALL.addAll(CQRECS_GRID.getQueryRecordsAll());
    }
    
    
    //
    // Public Operations
    // 
    
    /**
     * <p>
     * Creates and returns a new <code>QueryRequest</code> Protobuf message for the given query.
     * </p>
     * <p>
     * Recovers the <code>{@link TestQueryRecord}<?code> from the argument, using it to
     * create the Query Service data request message for the query.
     * </p>
     * 
     * @param enmType   enumeration constant specifying the desired single query type
     * 
     * @return  the <code>QueryRequest</code> message defining the Query Service data request
     */
    public static QueryDataRequest  requestMessage(SingleQueryType enmType) {
        return enmType.getQueryRecord().createRequestMessage();
    }
    
    /**
     * <p>
     * Creates and returns a new <code>{@link DpDataRequest}</code> Data Platform API request 
     * for the given query.
     * </p>
     * <p>
     * Recovers the <code>{@link TestQueryRecord}<?code> from the argument, using it to
     * create the DP API data request object for the query.
     * </p>
     * 
     * @param enmType   enumeration constant specifying the desired single query type
     * 
     * @return  the <code>DpDataRequest</code> object defining the data request
     */
    public static DpDataRequest  request(SingleQueryType enmType) {
        return enmType.getQueryRecord().createRequest();
    }
    
    /**
     * <p>
     * Creates and returns a new list of <code>{@link DpDataRequest}</code> objects representing
     * the decompose data query of the given decompose query.
     * </p>
     * <p>
     * Recovers the <code>{@link TestCompositeQueryRecord}<?code> from the argument, using it to
     * create the DP API data request objects for the query.
     * </p>
     *  
     * @param enmType   enumeration constant specifying the desired decompose query type
     * 
     * @return  list of <code>DpDataRequest</code> objects defining the decompose data request
     */
    public static List<DpDataRequest>   request(CompositeQueryType enmType) {
        return enmType.getCompositeRecord().createCompositeRequest();
    }
    
    /**
     * <p>
     * Recovers the results set for the given query, extracts the data buckets and returns them.
     * </p>
     * <p>
     * The results sets <em>data buckets</em> for all the supported single query cases are available here.
     * </p>
     * <p>
     * This method defers to <code>{@link #queryData(SingleQueryType)}</code> to recover the 
     * the results set data for the given query.  The <code>DataBucket</code> messages are extracted
     * and returned in order.
     * </p>
     *  
     * @param enmType   enumeration constant specifying the desired single query type
     * 
     * @return  the collection of ordered data buckets within the results set of the argument
     * 
     * @see #queryData(SingleQueryType)
     * @see SingleQueryType
     */
    public static List<QueryDataResponse.QueryData.DataBucket> queryBuckets(SingleQueryType enmType) {
        
        // Recover results set and extract data
        List<QueryDataResponse.QueryData>   lstData = TestQueryResponses.queryData(enmType);
        
        // Extract all data buckets
        List<QueryDataResponse.QueryData.DataBucket> lstBuckets = lstData
                .stream()
                .flatMap(
                        msgData -> msgData.getDataBucketsList().stream()
                        )
                .toList();
        
        return lstBuckets;
    }
    
    /**
     * <p>
     * Recovers the results set for the given query, extracts the data and returns it.
     * </p>
     * <p>
     * The results sets <em>data</em> for all the supported single query cases are available here.
     * </p>
     * <p>
     * This method defers to <code>{@link #queryResults(SingleQueryType)}</code> to recover the 
     * the results set for the given query.  The <code>BucketData</code> messages are extracted
     * and returned in order.
     * </p>
     *  
     * @param enmType   enumeration constant specifying the desired single query type
     * 
     * @return  the collection of ordered data within the results set of the argument
     * 
     * @see #queryResults(SingleQueryType)
     * @see SingleQueryType
     */
    public static List<QueryDataResponse.QueryData>    queryData(SingleQueryType enmType) {
        
        // Recover the results set
        List<QueryDataResponse> lstRsps = TestQueryResponses.queryResults(enmType);
        
        // Extract the BucketData messages
        List<QueryDataResponse.QueryData> lstData = lstRsps
                .stream()
                .map(msgRsp -> msgRsp.getQueryData())
                .toList();
        
        return lstData;
    }
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
    public static List<QueryDataResponse>   queryResults(SingleQueryType enmType) {
        TestQueryRecord rec = enmType.getQueryRecord();
        
        return TestQueryResponses.recoverQueryResults(rec);
    }
    
    /**
     * <p>
     * Recovers and returns the results sets for the given decompose query.
     * </p>
     * <p>
     * All the results sets for the decompose query cases are available here.  The
     * decompose cases assumes that a larger query operation was divided into multiple
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
     * @param enmType   enumeration constant specifying the desired decompose query type
     * @param index     index of the component query in the decompose query set
     * 
     * @return  the results set of the given (indexed) sub-query within the decompose query
     * 
     * @throws IndexOutOfBoundsException    the index was >= the number of sub-queries
     * 
     * @see CompositeQueryType
     */
    public static List<QueryDataResponse>   queryResults(CompositeQueryType enmType, int index) throws IndexOutOfBoundsException {
        
        // Check index value
        if (index >= enmType.getQueryCount())
            throw new IndexOutOfBoundsException("Given index value = " + index + " >= number of available results sets " + enmType.getQueryCount());
        
        // Get the decompose query record
        TestQueryRecord rec = enmType.getQueryRecord(index);
        
        return TestQueryResponses.recoverQueryResults(rec);
    }
    
    /**
     * <p>
     * Returns the number of sub-queries within the decompose query.
     * </p>
     * <p>
     * This method is used to obtain the number of (sub)queries within a decompose query
     * to avoid exceptions in <code>{@link #queryResults(CompositeQueryType, int)}</code>.
     * </p>
     * 
     * @param enmType   enumeration constant specifying the desired decompose query type
     * 
     * @return  the number of sub-queries within the given, managed decompose query
     * 
     * @see TestQueryCompositeRecord#getQueryCount()
     */
    public static int   getSubQueryCount(CompositeQueryType enmType) {
       
        // Get the number of decompose sub-queries
        int cntSubQueries = enmType.getQueryCount();
        
        return cntSubQueries;
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
     * Stores all managed query results sets to persistent data files.
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
            
            // Clear any current results set
            if (rec.hasQueryResults())
                rec.clearQueryResults();
            
            // Query new results set
            if (!rec.performQuery()) {
                LOGGER.error("{}: query operation failed for record {}.", JavaRuntime.getMethodName(), rec.strFileName());
                
                bolSuccess = false;
            }
            
            // Store the results set to disk
            if (!rec.storeQueryResults()) {
                LOGGER.error("{}: results set not available for storage operation in record {}.", JavaRuntime.getMethodName(), rec.strFileName());
                
                bolSuccess = false;
            }
        }

        if (bolSuccess)
            TestQueryResponses.shutdown();
        
        return bolSuccess;
    }
    
    /**
     * <p>
     * Deletes persistent data files for all managed query results sets.
     * </p>
     * <p>
     * This method is essentially the complement of the method <code>{@link #storePersistentData()}</code>. 
     * It is used to clear all persistent data created by that method.
     * </p>
     * <p>
     * A returned value of <code>true</code> indicates that the repository of persistent data was
     * fully populated before invocation, and is now completely empty upon return.  If the value 
     * <code>false</code> is returned it indicates that either the persistence repository was not
     * fully populated, or not all data files could be deleted.  However, a value <code>false</code>
     * does not necessary mean that the repository still contains data - it should be checked
     * manually.
     * </p>
     * 
     * @return  <code>true</code> if the persistence repository was fully populated and is now empty,
     *          <code>false</code> not all data files were deleted (some may not have existed)
     * 
     * @throws SecurityException    class loader resource locator provided an invalid URL for the file
     * @throws URISyntaxException   general I/O error deleting file
     * @throws IOException          file access denied by security manager
     */
    public static boolean deletePersistentData() throws SecurityException, URISyntaxException, IOException {
        
        boolean bolSuccess = true;
        
        for (TestQueryRecord rec : TestQueryResponses.SET_QRECS_ALL) {
            
            if (!rec.deletePersistence())
                bolSuccess = false;
        }
        
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
     * The operation <code>{@link TestQueryRecord#recoverQueryResponses()}</code> is invoked
     * on the argument to recover the results set associated with the argument.
     * </p>
     * <p>
     * This method catches all exceptions thrown by 
     * <code>{@link TestQueryRecord#recoverQueryResponses()}</code> and logs them to the
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
    private static List<QueryDataResponse>  recoverQueryResults(TestQueryRecord rec) {
        
        try {
            List<QueryDataResponse> lstRspMsgs = rec.recoverQueryResponses();
            
            if (lstRspMsgs == null)
                LOGGER.error("{}: the {} recover query results operation returned null for record {}.", 
                        JavaRuntime.getMethodName(), 
                        TestQueryRecord.class.getSimpleName(), 
                        rec.strFileName());
            
            return lstRspMsgs;
            
        } catch (IOException e) {
            LOGGER.error("{}: the {} recover query results operation threw IOException for record {}", 
                    JavaRuntime.getMethodName(), 
                    TestQueryRecord.class.getSimpleName(), 
                    rec.strFileName());
            
        } catch (ClassNotFoundException e) {
            LOGGER.error("{}: the {} recover query results operation threw ClassNotFoundException for record {}", 
                    JavaRuntime.getMethodName(), 
                    TestQueryRecord.class.getSimpleName(), 
                    rec.strFileName());
            
        } catch (DpGrpcException e) {
            LOGGER.error("{}: the {} recover query results operation threw DpGrpcException for record {}", 
                    JavaRuntime.getMethodName(), 
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
    
}
