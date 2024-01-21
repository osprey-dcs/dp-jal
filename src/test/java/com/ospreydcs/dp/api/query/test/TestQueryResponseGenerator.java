/*
 * Project: dp-api-common
 * File:	TestQueryResponseGenerator.java
 * Package: com.ospreydcs.dp.api.query.test
 * Type: 	TestQueryResponseGenerator
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

import java.util.Vector;

import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest;

/**
 * <p>
 * Utility class for creating <code>QueryResponse</code> instances, streams, and components 
 * for use in testing.
 * </p>
 * <p>
 * Creates Query Service responses which would result from the Data Platform testing archive.  
 * Configuration of the DP test archive is provided from the DP testing configuration.
 * </p>
 * <p>
 * The utility can provide result sets that mimic that of an actual Query Service data request.
 * The data within the result sets is taken from data files that are included with the
 * project test resources.
 * </p>
 * <p>
 * The utility also provides methods that generate the test data from an actual Query Service
 * data request.  To use those methods an active Query Service must be deployed and attached
 * to the Data Platform test archive.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Jan 14, 2024
 *
 */
public class TestQueryResponseGenerator {
    
    //
    // Class Types
    //

//    /**
//     * <p>
//     * Record containing properties describing supported Query Service test queries, and 
//     * fields for maintaining the result sets, and operations for query and persistence.
//     * </p>
//     * <p>
//     * Record contains properties for creating a test query with the 
//     * <code>{@link TestDpDataRequestGenerator}</code> utility.  Query result sets can be 
//     * actively maintained in the field <code>{@link #lstResults}</code> and be 
//     * recovered (i.e., either through persistence or directly through the Query Service)
//     * with operation <code>{@link #recoverQueryResults()}</code>.
//     * Record contains file name where results can be stored and recovered for persistence.  
//     * The record also performs various operations associated with the query, its results set, 
//     * and persistence.
//     * </p>
//     * <p>
//     * Thus, the act of creating a <code>TestQueryResults</code> record both defines a specific
//     * QueryService query (against the test archive) and provides access to the query's 
//     * results set. Additionally, the <code>{@link #storeQueryResults()}</code> method
//     * can store the recovered results set to a data file for persistence.
//     * Saving the results sets to persistent file storage provides future testing availability 
//     * without requiring an active Query Service.  
//     * </p>
//     * <p>
//     * <h2>NOTES:</h2>
//     * The record class maintains a static <code>{@link TestQueryService}</code> instance
//     * which is created if necessary (i.e., if an actual query is performed to create the
//     * results set).  <em>This instance should be shut down when no longer needed.</em>
//     * </p>
//     * 
//     * @param   lstResults      ordered list of query results (as if streamed)
//     * @param   strFileName     name of the file where persistent query result data is stored
//     * @param   cntSources      number of data sources (PVs) within query result set
//     * @param   indSourceFirst  index of the first data source within the list of source names
//     * @param   lngDuration     the query time range duration (in seconds)
//     * 
//     * @see TestDpDataRequestGenerator#createRequest(int, int, long)
//     * @see TestQueryService#queryResponseStream(QueryRequest)
//     */
//    public static record TestQueryResults (
//            List<QueryResponse> lstResults,
//            String              strFileName, 
//            int                 cntSources, 
//            int                 indSourceFirst,
//            long                lngDuration
//            ) 
//    {
//        
//        //
//        // Record Constants
//        //
//        
//        /** 
//         * Directory path to persistent query results (relative to project).
//         * <pre>
//         *  - used only for storage operation
//         *  - recovery of persistent data is done through class loader resource locator  
//         */
//        public static final String  STR_PATH_DIR_DATA = "src/test/resources/data";
//        
//        
//        //
//        // Record Resources
//        //
//        
//        /** 
//         * The single instance of the <code>TestQueryService</code>.
//         * <pre>
//         *  - only created if needed 
//         *  - should be shutdown when not needed (use <code>shutdownQueryServiceApi()</code>) 
//         */
//        private static TestQueryService qsTestArchive = null;
//        
//        
//        //
//        // Constructors
//        //
//        
//        /**
//         * <p>
//         * Canonical <code>TestQueryResults</code> constructor.
//         * </p>
//         *
//         * @param   lstResults      ordered list of query results (as if streamed)
//         * @param   strFileName     name of the file where persistent query result data is stored
//         * @param   cntSources      number of data sources (PVs) within query result set
//         * @param   indSourceFirst  index of the first data source within the list of source names
//         * @param   lngDuration     the query time range duration (in seconds)
//         * 
//         * @see TestDpDataRequestGenerator#createRequest(int, int, long)
//         */
//        public TestQueryResults {
//        }
//        
//        /**
//         * <p>
//         * Non-canonical <code>TestQueryResults</code> constructor.
//         * </p>
//         * <p>
//         * Creates a the <code>{@link #lstResults}</code> field as an empty 
//         * <code>{@link LinkedList}</code> instance.
//         * </p> 
//         *
//         * @param   lstResults      ordered list of query results (as if streamed)
//         * @param   strFileName     name of the file where persistent query result data is stored
//         * @param   cntSources      number of data sources (PVs) within query result set
//         * @param   indSourceFirst  index of the first data source within the list of source names
//         * @param   lngDuration     the query time range duration (in seconds)
//         * 
//         * @see TestDpDataRequestGenerator#createRequest(int, int, long)
//         */
//        public TestQueryResults(String strFileName, int cntSources, int indSourceFirst, long lngDuration) {
//            this(new LinkedList<>(), strFileName, cntSources, indSourceFirst, lngDuration);
//        }
//        
//        
//        //
//        // Record Class Operations
//        //
//        
//        /**
//         * <p>
//         * Shuts down the <code>TestQueryService</code> static instance when no longer needed.
//         * </p>
//         * <p>
//         * This method should always be called (as a matter of caution) whenever all records
//         * are no longer needed.  It releases the gRPC resources required for performing
//         * requests to the test archive.
//         * </p>
//         * 
//         * @return  <code>true</code> if <code>{@link #qsTestArchive}</code> service was active and shut down,
//         *          <code>false</code> if the Query Service API was never created 
//         * 
//         */
//        public static boolean shutdownQueryServiceApi() {
//            if (TestQueryResults.qsTestArchive != null) {
//                TestQueryResults.qsTestArchive.shutdown();
//                
//                return true;
//            }
//            
//            return false;
//        }
//        
//        
//        //
//        // Record Operations
//        //
//        
//        /**
//         * <p>
//         * Recovers and/or returns the result set associated with this <code>TestQueryResults</code> record.
//         * </p>
//         * <p>
//         * This method can be used in general to acquire the results set.
//         * If the associated results set is already available (in the the field 
//         * <code>{@link #lstResults}</code>) it is simply returned.
//         * Otherwise, this method attempts to acquire the associated result set by all possible means.
//         * </p>
//         * <p>
//         * The following set of operations are performed, in order, until the results are obtained:
//         * <ol>
//         * <li>First checks if result set is already available within the record, return it if so.</li>
//         * <li>Attempts to load the result set from persistent data file with name given in the record.</li>
//         * <li>Queries the Test Archive with query described by parameters within the record.</li>
//         * </ol>
//         * If the above operations all fail, a <code>null</code> value is returned, or an exception
//         * is thrown during operations.
//         * </p>
//         * 
//         * @return      the result set of the query operation described by record
//         * 
//         * @throws ClassNotFoundException   the data file does not contain a List<QueryResponse> object
//         * @throws IOException              the data file is corrupt
//         * @throws DpGrpcException          unable to establish <code>TestQueryService</code> connection
//         */
//        public List<QueryResponse> recoverQueryResults() throws ClassNotFoundException, IOException, DpGrpcException {
//            
//            // If the query results data is already available return it
//            if (!this.lstResults.isEmpty())
//                return this.lstResults;
//            
//            // Try to load data from existing file
//            if (this.loadQueryResults())
//                return this.lstResults;
//            
//            // Query the test archive to load data
//            if (this.performQuery())
//                return this.lstResults;
//            
//            // Everything failed - return null 
//            return null;
//        }
//        
//        /**
//         * <p>
//         * Convenience method for extracting the <code>QueryData</code> messages from the
//         * results set of <code>QueryResponse</code> messages.
//         * </p>
//         * <p>
//         * This method assumes that the query results set is valid, that is, the query 
//         * was not rejected and each <code>QueryResponse</code> contains a valid 
//         * <code>QueryData</code> message.  
//         * <ul>
//         * <li>If the above conditions hold, the data is extracted and returned as a list.</li>
//         * <li>If the query request was rejected the return list contains empty data messages.</li> 
//         * <li>If a query error occurred the corresponding <code>QueryData</code> message is empty.</li>
//         * <li>If the result set is unavailable a <code>null</code> value is returned.</li>
//         * </ul>
//         * 
//         * @return  an ordered list of extracted <code>QueryData</code> messages from results set,
//         *          or <code>null</code> if result set is  not available 
//         */
//        public List<QueryResponse.QueryReport.QueryData> extractQueryResults() {
//            
//            // Check that result set is available
//            if (this.lstResults.isEmpty())
//                return null;
//            
//            List<QueryResponse.QueryReport.QueryData>   lstDataMsgs = this.lstResults
//                    .stream()
//                    .map(qr -> qr.getQueryReport().getQueryData())
//                    .toList();
//            
//            return lstDataMsgs;
//        }
//
//        /**
//         * Creates and returns a new Query Service <code>QueryRequest</code> Protobuf message that
//         * realizes the query request described by this record.
//         * 
//         * @return  Data Platform Query Service <code>QueryRequest</code> message
//         */
//        public QueryRequest createRequest() {
//            DpDataRequest   dpRqst = TestDpDataRequestGenerator.createRequest(this.cntSources, this.indSourceFirst, this.lngDuration);
//            QueryRequest    msgRqst = dpRqst.buildQueryRequest();
//            
//            return msgRqst;
//        }
//        
//        /**
//         * <p>
//         * Writes the results set to the persistent output file.
//         * </p>
//         * <p>
//         * If the result set  data is present it is written to the output file with name
//         * <code>{@link #strFileName}</code> in directory <code>{@link #STR_PATH_DIR_DATA}</code>.  
//         * The output has the binary format of a Java serialized object of type 
//         * <code>List&lt;QueryResponse&gt;</code>.
//         * </p>
//         * <p>
//         * If the result set data is not present in <code>{@link #lstResults}</code> the method 
//         * returns a value <code>false</code>.
//         * If any errors occur during the operation an exception is thrown.
//         * </p>
//         * 
//         * @return  <code>true</code> if query results were available and written to output file,
//         *          <code>false</code> if no output file was written
//         * 
//         * @throws FileNotFoundException    Unable to create/open output file
//         * @throws IOException              Unable to write data (or object stream header) to output file
//         */
//        public boolean storeQueryResults() throws FileNotFoundException, IOException {
//
//            // Check that result set is available
//            if (this.lstResults.isEmpty())
//                return false;
//
//            // Save the result to an output file
//            //            String  strFilePath = STR_PATH_DIR_DATA + this.strFileName;
//            Path    pathData = Paths.get(STR_PATH_DIR_DATA, this.strFileName);
//            File    fileData = pathData.toFile();
//
//            FileOutputStream    fos = new FileOutputStream(fileData);
//            ObjectOutputStream  oos = new ObjectOutputStream(fos);
//
//            oos.writeObject(this.lstResults);
//
//            oos.close();
//            fos.close();
//
//            return true;
//        }
//
//        
//        //
//        // Support Methods
//        //
//        
//        /**
//         * Loads the query results from the associated data file if it exists.
//         * 
//         * @return  <code>true</code> if the data was successfully loaded into the given record,
//         *          <code>false</code> if the data file is missing
//         * 
//         * @throws IOException              the data file is corrupt
//         * @throws ClassNotFoundException   the data file does not contain a <code>List&lt;QueryResponse&gt;</code> object
//         */
//        @SuppressWarnings("unchecked")
//        private boolean loadQueryResults() throws IOException, ClassNotFoundException {
//            
//            // Use class loader to find data file
//            InputStream         isFile = TestQueryResults.class.getClassLoader().getResourceAsStream(this.strFileName);
//        
//            // Check that the data file exists
//            if (isFile == null)
//                return false;
//            
//            // Read the data file as a serialized List<QueryResponse> object
//            ObjectInputStream isObject = new ObjectInputStream(isFile);
//        
//            List<QueryResponse> lstRspMsgs = (List<QueryResponse>)isObject.readObject();
//        
//            isObject.close();
//            isFile.close();
//        
//            // Add data to record
//            this.lstResults.addAll(lstRspMsgs);
//        
//            return true;
//        }
//
//        /**
//         * <p>
//         * Obtain the query results from an actual query operation against the test archive.
//         * </p>
//         * <p>
//         * <h2>WARNING:</h2>
//         * This method creates the <code>{@link TestQueryService}</code> instance which is never
//         * shut down in case later needed.  This instance should be shut down somewhere.
//         * </p>
//         * 
//         * @return          <code>true</code> if query was successful and loaded into record,
//         *                  <code>false</code> if query operation failed
//         *                  
//         * @throws DpGrpcException  unable to establish <code>TestQueryService</code> connection
//         * 
//         * @see TestQueryService 
//         */
//        private boolean performQuery() throws DpGrpcException {
//
//            // Create the test Query Service API if necessary
//            if (qsTestArchive == null)
//                qsTestArchive = new TestQueryService();
//            
//            // Perform the query
//            QueryRequest    msgRqst = this.createRequest();
//            
//            List<QueryResponse> lstResults = qsTestArchive.queryResponseStream(msgRqst);
//            
//            // Add results to record
//            this.lstResults.addAll(lstResults);
//            
//            return true;
//        }
//    };
//
    
    //
    // Application Resources
    //
    
//    /** The query configuration for DP API testing */ 
//    private static final DpApiTestingConfig.TestQuery    CFG_QUERY = DpApiTestingConfig.getInstance().testQuery;
    
    
    //
    // Class Constants
    //
    
//    /** directory path to store query results (relative to project) - only used for storage operation */
//    public static final String  STR_PATH_DIR_DATA = "src/test/resources/data";
//    
    
    /** Parallel result sets - number of threads */
    public static final int     PARALLEL_CNT_THRDS = 5;
    
    /** Parallel result sets - number of data sources per thread */
    public static final int     PARALLEL_CNT_SRCS_PER_THRD = 10;
    
    /** Parallel result sets - time range duration of query */
    public static final long    PARALLEL_LNG_DURATION = 5L;
    
    /** Parallel result sets - storage file name */
    public static final String  PARALLEL_STR_FILENAME_PREFIX = "queryresults-responses-parallel";
    
    
    //
    // Class Resources
    //
    
    /** data and properties of the wide query results responses set */
    public static final TestQueryResults RESULTS_RSPS_WIDE = new TestQueryResults("queryresults-responses-wide.dat", 0, 100, 10L);
    
    /** data and properties of the wide query results responses set */
    public static final TestQueryResults RESULTS_RSPS_LONG = new TestQueryResults("queryresults-responses-wide.dat", 0, 10, 60L);
    
    /** data and properties of the parallel query results responses set - initialized in static block */
    public static final Vector<TestQueryResults> RESULTS_VEC_RSPS_PARALLEL = new Vector<>(PARALLEL_CNT_THRDS * PARALLEL_CNT_SRCS_PER_THRD);

    
//    /** data and properties of the wide query results data set */
//    public static final TestQueryResults<QueryData> RESULTS_DATA_WIDE = new TestQueryResults<QueryData>("queryresults-data-wide.dat", 0, 100, 10L);
//    
//    /** data and properties of the wide query results data set */
//    public static final TestQueryResults<QueryData> RESULTS_DATA_LONG = new TestQueryResults<QueryData>("queryresults-data-wide.dat", 0, 10, 60L);

    
//    /** The single instance of the <code>TestQueryService</code> - only created if needed (is never shut down) */
//    private static TestQueryService qsTestArchive = null;
    

    /**
     * <p>
     * Static initialization of class resources.
     * </p>
     * <p>
     * Creates the <code>TestQueryResults</code> record for the parallel result sets and adds them
     * to the static vector of parallel records.
     * </p>
     */
    static {
        for (int n=0; n<PARALLEL_CNT_THRDS; n++) {
            String  strFileName = PARALLEL_STR_FILENAME_PREFIX + "-" + Integer.toString(n) + ".dat";
            int     indSourceFirst = n * PARALLEL_CNT_SRCS_PER_THRD;
            
            TestQueryResults rec = new TestQueryResults(
                    strFileName,
                    PARALLEL_CNT_SRCS_PER_THRD,
                    indSourceFirst,
                    PARALLEL_LNG_DURATION
                    );
            
            RESULTS_VEC_RSPS_PARALLEL.add(rec);
        }
    }
    
    
    
    
    //
    // Support Methods
    //
    
    
    @SuppressWarnings("rawtypes")
    private static QueryRequest createDataRequest(TestQueryResults recProps) {
        DpDataRequest   rqst = TestDpDataRequestGenerator.createRequest(recProps.cntSources(), recProps.indSourceFirst(), recProps.lngDuration());
        
        return rqst.buildQueryRequest();
    }
    
    
//    /**
//     * <p>
//     * Recovers the result set associated with the given <code>TestQueryResults</code> record.
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
//    private static List<QueryResponse> recoverQueryResults(TestQueryResults rec) throws ClassNotFoundException, IOException, DpGrpcException {
//        
//        // If the query results data is already available return it
//        if (!rec.lstResults.isEmpty())
//            return rec.lstResults;
//        
//        // Try to load data from existing file
//        if (TestQueryResponseGenerator.loadQueryResults(rec))
//            return rec.lstResults;
//        
//        // Query the test archive to load data
//        if (TestQueryResponseGenerator.performQuery(rec))
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
//    private static boolean loadQueryResults(TestQueryResults rec) throws IOException, ClassNotFoundException {
//        
//        // Use class loader to find data file
//        InputStream         isFile = TestQueryResponseGenerator.class.getClassLoader().getResourceAsStream(rec.strFileName);
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
//    private static boolean performQuery(TestQueryResults rec) throws DpGrpcException {
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
    
    /**
     * <p>
     * Prevent construction of <code>TestQueryResponseGenerator</code> instances.
     * </p>
     *
     */
    private TestQueryResponseGenerator() {
    }

    
    
}
