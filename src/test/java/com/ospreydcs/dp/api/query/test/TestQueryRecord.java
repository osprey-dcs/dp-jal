/*
 * Project: dp-api-common
 * File:	TestQueryRecord.java
 * Package: com.ospreydcs.dp.api.query.test
 * Type: 	TestQueryRecord
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
 * @since Jan 21, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import com.ospreydcs.dp.api.config.test.DpApiTestingConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

/**
 * <p>
 * Record containing properties describing supported Query Service test queries, and 
 * fields for maintaining the result sets, and operations for query and persistence.
 * </p>
 * <p>
 * Record contains properties for creating a test query with the 
 * <code>{@link TestDpDataRequestGenerator}</code> utility.  Query result sets can be 
 * actively maintained in the field <code>{@link #lstResults}</code> and be 
 * recovered (i.e., either through persistence or directly through the Query Service)
 * with operation <code>{@link #recoverQueryResults()}</code>.
 * Record contains file name where results can be stored and recovered for persistence.  
 * The record also performs various operations associated with the query, its results set, 
 * and persistence.
 * </p>
 * <p>
 * Thus, the act of creating a <code>TestQueryRecord</code> record both defines a specific
 * QueryService query (against the test archive) and provides access to the query's 
 * results set. Additionally, the <code>{@link #storeQueryResults()}</code> method
 * can store the recovered results set to a data file for persistence.
 * Saving the results sets to persistent file storage provides future testing availability 
 * without requiring an active Query Service.  
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * The record class maintains a static <code>{@link TestQueryService}</code> instance
 * which is created if necessary (i.e., if an actual query is performed to create the
 * results set).  <em>This instance should be shut down when no longer needed.</em>
 * </p>
 * 
 * @param   lstResults      ordered list of query results (as if streamed)
 * @param   strFileName     name of the file where persistent query result data is stored
 * @param   cntSources      number of data sources (PVs) within query result set
 * @param   indSourceFirst  index of the first data source within the list of source names
 * @param   lngDuration     the query time range duration (in seconds)
 * @param   lngStartTime    start time of the time range (in seconds)
 * 
 * @see TestDpDataRequestGenerator#createRequest(int, int, long)
 * @see TestQueryService#queryResponseStream(QueryRequest)
 */
public final record TestQueryRecord (
        List<QueryResponse> lstResults,
        String              strFileName, 
        int                 cntSources, 
        int                 indSourceFirst,
        long                lngDuration,
        long                lngStartTime
        ) 
{
    
    // 
    // Application Resources
    //
    
    /** Data Platform Testing configuration parameters */
    private static final    DpApiTestingConfig CFG_TESTING = DpApiTestingConfig.getInstance();
    
    
    //
    // Record Constants
    //
    
    /** 
     * Directory path to persistent query results (relative to project).
     * <pre>
     *  - used only for storage operation
     *  - recovery of persistent data is done through class loader resource locator  
     */
//    public static final String  STR_PATH_DIR_DATA = "src/test/resources/data";
    public static final String  STR_PATH_DIR_DATA = CFG_TESTING.testQuery.data.persistence;
    
    
    //
    // Record Resources
    //
    
    /** 
     * The single instance of the <code>TestQueryService</code>.
     * <pre>
     *  - only created if needed 
     *  - should be shutdown when not needed (use <code>shutdownQueryServiceApi()</code>) 
     */
    private static TestQueryService qsTestArchive = null;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Canonical <code>TestQueryRecord</code> constructor.
     * </p>
     *
     * @param   lstResults      ordered list of query results (as if streamed)
     * @param   strFileName     name of the file where persistent query result data is stored
     * @param   cntSources      number of data sources (PVs) within query result set
     * @param   indSourceFirst  index of the first data source within the list of source names
     * @param   lngDuration     the query time range duration (in seconds)
     * @param   lngStartTime    start time of the time range (in seconds)
     * 
     * @see TestDpDataRequestGenerator#createRequest(int, int, long)
     */
    public TestQueryRecord {
    }
    
    /**
     * <p>
     * Non-canonical <code>TestQueryRecord</code> constructor.
     * </p>
     * <p>
     * Creates a the <code>{@link #lstResults}</code> field as an empty 
     * <code>{@link LinkedList}</code> instance.
     * </p> 
     *
     * @param   strFileName     name of the file where persistent query result data is stored
     * @param   cntSources      number of data sources (PVs) within query result set
     * @param   indSourceFirst  index of the first data source within the list of source names
     * @param   lngDuration     the query time range duration (in seconds)
     * @param   lngStartTime    start time of the time range (in seconds)
     * 
     * @see TestDpDataRequestGenerator#createRequest(int, int, long)
     */
    public TestQueryRecord(String strFileName, int cntSources, int indSourceFirst, long lngDuration, long lngStartTime) {
        this(new LinkedList<>(), strFileName, cntSources, indSourceFirst, lngDuration, lngStartTime);
    }
    
    
    //
    // Record Class Operations
    //
    
    /**
     * <p>
     * Shuts down the <code>TestQueryService</code> static instance when no longer needed.
     * </p>
     * <p>
     * This method should always be called (as a matter of caution) whenever all records
     * are no longer needed.  It releases the gRPC resources required for performing
     * requests to the test archive.
     * </p>
     * 
     * @return  <code>true</code> if <code>{@link #qsTestArchive}</code> service was active and shut down,
     *          <code>false</code> if the Query Service API was never created 
     * 
     */
    public static boolean shutdownQueryServiceApi() {
        if (TestQueryRecord.qsTestArchive != null) {
            TestQueryRecord.qsTestArchive.shutdown();
            TestQueryRecord.qsTestArchive = null;

            return true;
        }
        
        return false;
    }
    
    
    //
    // Record Operations
    //
    
    /**
     * <p>
     * Returns whether or not the results set field <code>{@link #lstResults}</code> has been populated.
     * </p>
     * 
     * @return <code>true</code> if <code>{@link #lstResults}</code> is non-empty,
     *         <code>false</code> otherwise
     */
    public boolean hasQueryResults() {
        return !this.lstResults.isEmpty();
    }
    
    /**
     * <p>
     * Returns the results set field <code>{@link #lstResults}</code> this record if available.
     * </p>
     * <p>
     * Note that the method <code>{@link #recoverQueryResults()}</code> will load the results
     * set from persistent storage or query the Query Service to create results set if it
     * is not already available.
     * </p>
     * 
     * @return  the value of <code>{@link #lstResults}</code> if non-empty,
     *          otherwise <code>null</code> is returned
     *          
     * @see #recoverQueryResults()
     */
    public List<QueryResponse>  getQueryResults() {
        if (this.hasQueryResults())
            return this.lstResults;
        
        return null;
    }
    
    /**
     * <p>
     * Recovers and/or returns the results set associated with this <code>TestQueryRecord</code> record.
     * </p>
     * <p>
     * This method can be used in general to acquire the results set.
     * If the associated results set is already available (in the the field 
     * <code>{@link #lstResults}</code>) it is simply returned.
     * Otherwise, this method attempts to acquire the associated result set by all possible means.
     * </p>
     * <p>
     * The following set of operations are performed, in order, until the results are obtained:
     * <ol>
     * <li>First checks if result set is already available within the record, return it if so.</li>
     * <li>Attempts to load the result set from persistent data file with name given in the record.</li>
     * <li>Queries the Test Archive with query described by parameters within the record.</li>
     * </ol>
     * If the above operations all fail, a <code>null</code> value is returned or an exception
     * is thrown.  A <code>null</code> value indicates that the persistent data file was missing
     * AND the query operation failed.  
     * </p>
     * 
     * @return      the result set of the query operation described by record,
     *              or <code>null</code> if the data file is missing and query operation failed
     * 
     * @throws IOException              the data file is corrupt
     * @throws ClassNotFoundException   the data file does not contain a List<QueryResponse> object
     * @throws DpGrpcException          unable to establish <code>TestQueryService</code> connection
     */
    public List<QueryResponse> recoverQueryResults() throws IOException, ClassNotFoundException, DpGrpcException {
        
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
    
    /**
     * <p>
     * Convenience method for extracting the <code>QueryData</code> messages from the
     * results set of <code>QueryResponse</code> messages.
     * </p>
     * <p>
     * This method assumes that the query results set is valid, that is, the query 
     * was not rejected and each <code>QueryResponse</code> contains a valid 
     * <code>QueryData</code> message.  
     * <ul>
     * <li>If the above conditions hold, the data is extracted and returned as a list.</li>
     * <li>If the query request was rejected the return list contains empty data messages.</li> 
     * <li>If a query error occurred the corresponding <code>QueryData</code> message is empty.</li>
     * <li>If the result set is unavailable a <code>null</code> value is returned.</li>
     * </ul>
     * 
     * @return  an ordered list of extracted <code>QueryData</code> messages from results set,
     *          or <code>null</code> if result set is  not available 
     */
    public List<QueryResponse.QueryReport.QueryData> extractQueryData() {
        
        // Check that result set is available
        if (this.lstResults.isEmpty())
            return null;
        
        List<QueryResponse.QueryReport.QueryData>   lstDataMsgs = this.lstResults
                .stream()
                .map(qr -> qr.getQueryReport().getQueryData())
                .toList();
        
        return lstDataMsgs;
    }

    /**
     * Creates and returns a new Query Service <code>QueryRequest</code> Protobuf message that
     * realizes the query request described by this record.
     * 
     * @return  Data Platform Query Service <code>QueryRequest</code> message
     */
    public QueryRequest createRequest() {
        DpDataRequest   dpRqst = TestDpDataRequestGenerator.createRequest(this.cntSources, this.indSourceFirst, this.lngDuration, this.lngStartTime);
        QueryRequest    msgRqst = dpRqst.buildQueryRequest();
        
        return msgRqst;
    }
    
    /**
     * <p>
     * Writes the results set to the persistent output file.
     * </p>
     * <p>
     * If the result set  data is present it is written to the output file with name
     * <code>{@link #strFileName}</code> in directory <code>{@link #STR_PATH_DIR_DATA}</code>.  
     * The output has the binary format of a Java serialized object of type 
     * <code>List&lt;QueryResponse&gt;</code>.
     * </p>
     * <p>
     * If the result set data is not present in <code>{@link #lstResults}</code> the method 
     * returns a value <code>false</code>.
     * If any errors occur during the operation an exception is thrown.
     * </p>
     * 
     * @return  <code>true</code> if query results set was available and successfully written to output file,
     *          <code>false</code> query results set was not available
     * 
     * @throws FileNotFoundException    Unable to create/open output file
     * @throws IOException              Unable to write data (or object stream header) to output file
     */
    boolean storeQueryResults() throws FileNotFoundException, IOException {

        // Check that result set is available
        if (this.lstResults.isEmpty())
            return false;

        // Save the result to an output file
        //            String  strFilePath = STR_PATH_DIR_DATA + this.strFileName;
        Path    pathData = Paths.get(STR_PATH_DIR_DATA, this.strFileName);
        File    fileData = pathData.toFile();

        FileOutputStream    fos = new FileOutputStream(fileData);
        ObjectOutputStream  oos = new ObjectOutputStream(fos);

        oos.writeObject(this.lstResults);

        oos.close();
        fos.close();

        return true;
    }

    
    //
    // Support Methods - Package Accessible
    //
    
    /**
     * Loads the query results from the associated data file if it exists.
     * 
     * @return  <code>true</code> if the data was successfully loaded into the given record,
     *          <code>false</code> if the data file is missing
     * 
     * @throws SecurityException        access to persistent data directory denied
     * @throws IOException              the data file is corrupt
     * @throws ClassNotFoundException   the data file does not contain a <code>List&lt;QueryResponse&gt;</code> object
     */
    @SuppressWarnings("unchecked")
    boolean loadQueryResults() throws SecurityException, IOException, ClassNotFoundException {
        
//        // Use class loader to find data file
//        InputStream         isFile = TestQueryRecord.class.getClassLoader().getResourceAsStream(this.strFileName);
    
        // Check that the data file exists
//      if (isFile == null)
//          return false;
      
        //            String  strFilePath = STR_PATH_DIR_DATA + this.strFileName;
        Path    pathData = Paths.get(STR_PATH_DIR_DATA, this.strFileName);

        // Check that the data file exists
        if (!Files.exists(pathData))
            return false;
        
        // Open input stream to data file
        File        fileData = pathData.toFile();
        InputStream isFile = new FileInputStream(fileData);

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
    boolean performQuery() throws DpGrpcException {

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
    
    /**
     * <p>
     * Deletes the file containing the persistent storage of the query results set.
     * </p>
     * <p>
     * This operation is essentially the complement of <code>{@link #storeQueryResults()}</code>.
     * It erases the persistent storage associated with the results set of the query request.
     * If the file does not exist the method does nothing, returned a value <code>false</code>.
     * </p>
     * 
     * @return  <code>true</code> if the persistent storage file existed and was deleted,
     *          <code>false</code> if the file did not exist, or existed and the deletion failed
     *          
     * @throws IOException          general I/O error deleting file
     * @throws SecurityException    file access denied by security manager
     */
    boolean deletePersistence() throws IOException, SecurityException {
        
        // Create path to data file
        Path    pathFile = Paths.get(STR_PATH_DIR_DATA, this.strFileName);

        // Check that the data file exists
        if (!Files.exists(pathFile))
            return false;

        // Attempt to delete the file
        boolean bolDeleted = Files.deleteIfExists(pathFile);

        return bolDeleted;
    }
};

