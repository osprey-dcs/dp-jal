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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.junit.Assert;

import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

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

    /**
     * <p>
     * Record containing supported test query results and properties.
     * </p>
     * <p>
     * Record also contains file name where results are stored and properties 
     * of the originating test query.  The test query is assumed to be created with the 
     * <code>{@link TestDpDataRequestGenerator} utility.
     * </p>
     * 
     * @param <T>   result set Protobuf message type
     * 
     * @param   lstResults      ordered list of query results (as if streamed)
     * @param   strFileName     name of the file where persistent query result data is stored
     * @param   cntSources      number of data sources (PVs) within query result set
     * @param   indSourceFirst  index of the first data source within the list of source names
     * @param   lngDuration     the query time range duration (in seconds)
     * 
     * @see TestDpDataRequestGenerator#createRequest(int, int, long)
     */
    public static record QueryResults (
            List<QueryResponse> lstResults,
            String              strFileName, 
            int                 cntSources, 
            int                 indSourceFirst,
            long                lngDuration
            ) 
    {
        
        //
        // Record Constants
        //
        
        /** directory path to store query results (relative to project) - only used for storage operation */
        public static final String  STR_PATH_DIR_DATA = "src/test/resources/data";
        
        
        //
        // Record Resources
        //
        
        /** The single instance of the <code>TestQueryService</code> - only created if needed (is never shut down) */
        private static TestQueryService qsTestArchive = null;
        
        
        //
        // Constructors
        //
        
        /**
         * <p>
         * Canonical <code>QueryResults</code> constructor.
         * </p>
         *
         * @param   lstResults      ordered list of query results (as if streamed)
         * @param   strFileName     name of the file where persistent query result data is stored
         * @param   cntSources      number of data sources (PVs) within query result set
         * @param   indSourceFirst  index of the first data source within the list of source names
         * @param   lngDuration     the query time range duration (in seconds)
         * 
         * @see TestDpDataRequestGenerator#createRequest(int, int, long)
         */
        public QueryResults {
        }
        
        /**
         * <p>
         * Non-canonical <code>QueryResults</code> constructor.
         * </p>
         * <p>
         * Creates a new, empty <code>{@link #lstResults}</code> as <code>{@link LinkedList}</code>
         * </p> 
         *
         * @param   lstResults      ordered list of query results (as if streamed)
         * @param   strFileName     name of the file where persistent query result data is stored
         * @param   cntSources      number of data sources (PVs) within query result set
         * @param   indSourceFirst  index of the first data source within the list of source names
         * @param   lngDuration     the query time range duration (in seconds)
         * 
         * @see TestDpDataRequestGenerator#createRequest(int, int, long)
         */
        public QueryResults(String strFileName, int cntSources, int indSourceFirst, long lngDuration) {
            this(new LinkedList<>(), strFileName, cntSources, indSourceFirst, lngDuration);
        }
        
        /**
         * Creates and returns a new Query Service <code>QueryRequest</code> Protobuf message that
         * realizes the query request described by this record.
         * 
         * @return  Data Platform Query Service <code>QueryRequest</code> message
         */
        public QueryRequest createRequest() {
            DpDataRequest   dpRqst = TestDpDataRequestGenerator.createRequest(this.cntSources, this.indSourceFirst, this.lngDuration);
            QueryRequest    msgRqst = dpRqst.buildQueryRequest();
            
            return msgRqst;
        }
        
        
        //
        // Record Operations
        //
        
        /**
         * <p>
         * Recovers the result set associated with this <code>QueryResults</code> record.
         * </p>
         * <p>
         * This method attempts to acquire the associated result set by all possible means.
         * The following set of operations are performed, in order, until the results are obtained:
         * <ol>
         * <li>First checks if result set is already available within the record, return it if so.</li>
         * <li>Attempts to load the result set from the data file given in the record.</li>
         * <li>Queries the Test Archive with the query parameters within the given record.</li>
         * </ol>
         * If the above operations all fail a <code>null</code> value is returned, or an exception
         * is thrown during operations.
         * </p>
         * 
         * @return      the result set of the query operation described by record
         * 
         * @throws ClassNotFoundException   the data file does not contain a List<QueryResponse> object
         * @throws IOException              the data file is corrupt
         * @throws DpGrpcException          unable to establish <code>TestQueryService</code> connection
         */
        public List<QueryResponse> recoverQueryResults() throws ClassNotFoundException, IOException, DpGrpcException {
            
            // If the query results data is already available return it
            if (!this.lstResults.isEmpty())
                return this.lstResults;
            
            // Try to load data from existing file
            if (this.loadQueryResults())
                return this.lstResults;
            
            // Query the test archive to load data
            if (this.performQuery())
                return this.lstResults;
            
            // Everything failed - return null 
            return null;
        }

        
        //
        // Support Methods
        //
        
        /**
         * Loads the query results from the associated data file if it exists.
         * 
         * @return  <code>true</code> if the data was successfully loaded into the given record,
         *          <code>false</code> if the data file is missing
         * 
         * @throws IOException              the data file is corrupt
         * @throws ClassNotFoundException   the data file does not contain a <code>List&lt;QueryResponse&gt;</code> object
         */
        @SuppressWarnings("unchecked")
        private boolean loadQueryResults() throws IOException, ClassNotFoundException {
            
            // Use class loader to find data file
            InputStream         isFile = QueryResults.class.getClassLoader().getResourceAsStream(this.strFileName);
        
            // Check that the data file exists
            if (isFile == null)
                return false;
            
            // Read the data file as a serialized List<QueryResponse> object
            ObjectInputStream isObject = new ObjectInputStream(isFile);
        
            List<QueryResponse> lstRspMsgs = (List<QueryResponse>)isObject.readObject();
        
            isObject.close();
            isFile.close();
        
            // Add data to record
            this.lstResults.addAll(lstRspMsgs);
        
            return true;
        }

        /**
         * <p>
         * Obtain the query results from an actual query operation against the test archive.
         * </p>
         * <p>
         * <h2>WARNING:</h2>
         * This method creates the <code>{@link TestQueryService}</code> instance which is never
         * shut down in case later needed.  This instance should be shut down somewhere.
         * </p>
         * 
         * @return          <code>true</code> if query was successful and loaded into record,
         *                  <code>false</code> if query operation failed
         *                  
         * @throws DpGrpcException  unable to establish <code>TestQueryService</code> connection
         * 
         * @see TestQueryService 
         */
        private boolean performQuery() throws DpGrpcException {

            // Create the test Query Service API if necessary
            if (qsTestArchive == null)
                qsTestArchive = new TestQueryService();
            
            // Perform the query
            QueryRequest    msgRqst = this.createRequest();
            
            List<QueryResponse> lstResults = qsTestArchive.queryResponseStream(msgRqst);
            
            // Add results to record
            this.lstResults.addAll(lstResults);
            
            return true;
        }
        
        private boolean storeQueryResults() {
            
            // Check that result set is available
            if (this.lstResults.isEmpty())
                return false;
            
            // Save the result to an output file
//            String  strFilePath = STR_PATH_DIR_DATA + this.strFileName;
            Path    pathData = Paths.get(STR_PATH_DIR_DATA, this.strFileName);
            File    fileData = pathData.toFile();
            
            try {
                FileOutputStream    fos = new FileOutputStream(fileData);
                ObjectOutputStream  oos = new ObjectOutputStream(fos);
                
                oos.writeObject(this.lstResults);
                
                oos.close();
                fos.close();

            } catch (FileNotFoundException e) {
                Assert.fail("Unable to create/open output file " + pathData);
                return false;
                
            } catch (IOException e) {
                Assert.fail("Unable to write data to output file " + pathData);
                return false;
                
            }
            
            return true;
        }
    };

    
    //
    // Application Resources
    //
    
//    /** The query configuration for DP API testing */ 
//    private static final DpApiTestingConfig.TestQuery    CFG_QUERY = DpApiTestingConfig.getInstance().testQuery;
    
    
    //
    // Class Constants
    //
    
    /** directory path to store query results (relative to project) - only used for storage operation */
    public static final String  STR_PATH_DIR_DATA = "src/test/resources/data";
    
    
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
    public static final QueryResults RESULTS_RSPS_WIDE = new QueryResults("queryresults-responses-wide.dat", 0, 100, 10L);
    
    /** data and properties of the wide query results responses set */
    public static final QueryResults RESULTS_RSPS_LONG = new QueryResults("queryresults-responses-wide.dat", 0, 10, 60L);
    
    /** data and properties of the parallel query results responses set - initialized in static block */
    public static final Vector<QueryResults> RESULTS_VEC_RSPS_PARALLEL = new Vector<>(PARALLEL_CNT_THRDS * PARALLEL_CNT_SRCS_PER_THRD);

    
//    /** data and properties of the wide query results data set */
//    public static final QueryResults<QueryData> RESULTS_DATA_WIDE = new QueryResults<QueryData>("queryresults-data-wide.dat", 0, 100, 10L);
//    
//    /** data and properties of the wide query results data set */
//    public static final QueryResults<QueryData> RESULTS_DATA_LONG = new QueryResults<QueryData>("queryresults-data-wide.dat", 0, 10, 60L);

    
    /** The single instance of the <code>TestQueryService</code> - only created if needed (is never shut down) */
    private static TestQueryService qsTestArchive = null;
    

    /**
     * <p>
     * Static initialization of class resources.
     * </p>
     * <p>
     * Creates the <code>QueryResults</code> record for the parallel result sets and adds them
     * to the static vector of parallel records.
     * </p>
     */
    static {
        for (int n=0; n<PARALLEL_CNT_THRDS; n++) {
            String  strFileName = PARALLEL_STR_FILENAME_PREFIX + "-" + Integer.toString(n) + ".dat";
            int     indSourceFirst = n * PARALLEL_CNT_SRCS_PER_THRD;
            
            QueryResults rec = new QueryResults(
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
    private static QueryRequest createDataRequest(QueryResults recProps) {
        DpDataRequest   rqst = TestDpDataRequestGenerator.createRequest(recProps.cntSources, recProps.indSourceFirst, recProps.lngDuration);
        
        return rqst.buildQueryRequest();
    }
    
    
    /**
     * <p>
     * Recovers the result set associated with the given <code>QueryResults</code> record.
     * </p>
     * <p>
     * This method attempts to acquire the associated result set by all possible means.
     * The following set of operations are performed, in order, until the results are obtained:
     * <ol>
     * <li>First checks if result set is already available within the record return it if so.</li>
     * <li>Attempts to load the result set from the data file given in the record.</li>
     * <li>Queries the Test Archive with the query parameters within the given record.</li>
     * </ol>
     * If the above operations all fail a <code>null</code> value is returned, or an exception
     * is thrown during operations.
     * </p>
     * 
     * @param rec   record describing the query and possible already containing data
     * 
     * @return      the result set of the query operation described by record
     * 
     * @throws ClassNotFoundException   the data file does not contain a List<QueryResponse> object
     * @throws IOException              the data file is corrupt
     * @throws DpGrpcException          unable to establish <code>TestQueryService</code> connection
     */
    private static List<QueryResponse> recoverQueryResults(QueryResults rec) throws ClassNotFoundException, IOException, DpGrpcException {
        
        // If the query results data is already available return it
        if (!rec.lstResults.isEmpty())
            return rec.lstResults;
        
        // Try to load data from existing file
        if (TestQueryResponseGenerator.loadQueryResults(rec))
            return rec.lstResults;
        
        // Query the test archive to load data
        if (TestQueryResponseGenerator.performQuery(rec))
            return rec.lstResults;
        
        // Everything failed - return null 
        return null;
    }
    
    /**
     * Loads the query results from the associated data file if it exists.
     * 
     * @param rec   record containing data file name, and target of loading operation
     * 
     * @return  <code>true</code> if the data was successfully loaded into the given record,
     *          <code>false</code> if the data file is missing
     * 
     * @throws IOException              the data file is corrupt
     * @throws ClassNotFoundException   the data file does not contain a <code>List&lt;QueryResponse&gt;</code> object
     */
    @SuppressWarnings("unchecked")
    private static boolean loadQueryResults(QueryResults rec) throws IOException, ClassNotFoundException {
        
        // Use class loader to find data file
        InputStream         isFile = TestQueryResponseGenerator.class.getClassLoader().getResourceAsStream(rec.strFileName);
    
        // Check that the data file exists
        if (isFile == null)
            return false;
        
        // Read the data file as a serialized List<QueryResponse> object
        ObjectInputStream isObject = new ObjectInputStream(isFile);
    
        List<QueryResponse> lstRspMsgs = (List<QueryResponse>)isObject.readObject();
    
        isObject.close();
        isFile.close();
    
        // Add data to record
        rec.lstResults.addAll(lstRspMsgs);
    
        return true;
    }

    /**
     * <p>
     * Obtain the query results from an actual query operation against the test archive.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * This method creates the <code>{@link TestQueryService}</code> instance which is never
     * shut down in case later needed.  This instance should be shut down somewhere.
     * </p>
     * 
     * @param rec       record containing query parameters, and target of query results
     *  
     * @return          <code>true</code> if query was successful and loaded into record,
     *                  <code>false</code> if query operation failed
     *                  
     * @throws DpGrpcException  unable to establish <code>TestQueryService</code> connection
     * 
     * @see TestQueryService 
     */
    private static boolean performQuery(QueryResults rec) throws DpGrpcException {

        // Create the test Query Service API if necessary
        if (qsTestArchive == null)
            qsTestArchive = new TestQueryService();
        
        // Perform the query
        DpDataRequest   dpRqst = TestDpDataRequestGenerator.createRequest(rec.cntSources, rec.indSourceFirst, rec.lngDuration);
        QueryRequest    msgRqst = dpRqst.buildQueryRequest();
        
        List<QueryResponse> lstResults = qsTestArchive.queryResponseStream(msgRqst);
        
        // Add results to record
        rec.lstResults.addAll(lstResults);
        
        return true;
    }
    
    /**
     * <p>
     * Prevent construction of <code>TestQueryResponseGenerator</code> instances.
     * </p>
     *
     */
    private TestQueryResponseGenerator() {
    }

    
    
}
