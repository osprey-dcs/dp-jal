/*
 * Project: dp-api-common
 * File:    TestQueryCompositeRecord.java
 * Package: com.ospreydcs.dp.api.query.test
 * Type:    TestQueryCompositeRecord
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
 *
 * @author Christopher K. Allen
 * @org    OspreyDCS
 * @since Jan 22, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import com.ospreydcs.dp.api.grpc.model.DpGrpcException;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

/**
 * <p>
 * Record class defining a data query composed multiple sub-queries assumed to be launched on 
 * separate threads.
 * </p>
 * <p>
 * This class both defines a composite target query and creates the <code>{@link TestQueryRecord}</code>
 * instances describing each component query of the larger composite.
 * The target query is described by its the query domain [data sources]&times;[time],
 * where [data sources] is the horizontal axis and [time] is the vertical axis.
 * The component queries are built by sub-dividing the query domain into disjoint sets that
 * together cover the full domain of the target query.
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
 * To prevent remainders in integer division note the following:
 * <ul>
 * <li>
 * choose the number of data sources as a multiple of the number of sub-queries 
 * when using the <code>HORIZONTAL</code> strategy.
 * </li>
 * <br/>
 * <li>
 * choose the total duration as a multiple of the number of sub-queries 
 * when using the <code>VERTICAL</code> strategy.
 * </li>
 * <br/>
 * <li>
 * choose the number of sub-queries as a multiple of 2 and both the number of data sources 
 * and the total duration as multiples of sub-query count/2 
 * when using the <code>GRID</code> strategy.
 * </li>
 * </ul>
 * </p>
 * 
 * @param   enmStrategy     Strategy for decomposing the primary data query 
 * @param   cntQueries      Number of sub-queries composing the primary query 
 * @param   cntSources      Number of data sources within the primary query 
 * @param   lngDuration     Time range duration of the primary query 
 * @param   strFilePrefix   File name prefix used in persistent storage - full names are suffixed with query index 
 *
 * @author Christopher K. Allen
 * @since Jan 22, 2024
 *
 */
public class TestQueryCompositeRecord {

    //
    // Class Types
    //
    
    /**
     * <p>
     * Enumeration of possible concurrent query decomposition strategies.
     * </p>
     */
    public static enum CompositeStrategy {

        /**
         * Query domain is decomposed horizontally (by data sources).
         * <p>
         * The data sources are divided equally amongst the concurrent queries. <br/>
         * That is, each separate query is for a different set of data sources. <br/>
         * All queries have the same time range. 
         */
        HORIZONTAL,

        /**
         * Query domain is decomposed vertically (in time).
         * <p>
         * The time range is divided equally amongst the concurrent queries. <br/>
         * That is, each separate query is for a different time range. <br/>
         * All queries have the same data sources.
         */
        VERTICAL,
        
        /**
         * Query domain is decomposed into a grid (i.e., of blocks in source and time).
         * <p>
         * Both the data sources and time range is divided equally amongst the concurrent queries.<br/>
         * That is, each separate query contains a subset of data sources and subset of time ranges.<br/> 
         */
        GRID;
    };

    
    //
    // Defining Attributes
    //
    
    /** Strategy for decomposing the primary data query */
    private final CompositeStrategy  enmStrategy;
    
    /** Number of sub-queries composing the primary query */
    private final int       cntQueries;
    
    /** Number of data sources within the primary query */
    private final int       cntSources; 
    
    /** Time range duration of the primary query */
    private final long      lngDuration;
    
    /** File name prefix used in persistent storage - full names are suffixed with query index */
    private final String    strFilePrefix;

    
    // 
    // Composite Resources
    //
    
    /** Indexed vector of all sub-queries comprising the primary query */
    private final Vector<TestQueryRecord> vecRecords;


    //
    // Canonical Constructor
    //
    
    /**
     * <p>
     * Defining Constructor.
     * </p>
     * <p>
     * Constructs a new, fully-initialized instance of <code>TestQueryCompositeRecord</code>.
     * All <code>TestQueryRecord</code> instances are created according to the given strategy.
     * </p>
     *
     * @param enmStrategy   CompositeStrategy for decomposing the primary data query 
     * @param cntQueries    Number of sub-queries composing the primary query 
     * @param cntSources    Number of data sources within the primary query
     * @param lngDuration   Time range duration of the primary query
     * @param strFilePrefix File name prefix used in persistent storage - full names are suffixed with query index
     */
    public TestQueryCompositeRecord(CompositeStrategy enmStrategy, int cntThreads, int cntSources, long lngDuration, String strFilePrefix) {
        this.enmStrategy = enmStrategy;
        this.cntQueries = cntThreads;
        this.cntSources = cntSources;
        this.lngDuration = lngDuration;
        this.strFilePrefix = strFilePrefix;
        
        this.vecRecords = new Vector<>(cntThreads);
        
        this.buildSubQueryRecords(enmStrategy);
    }
    
    
    //
    // Property Getters
    //
    
    /**
     * @return the current value of property {@link #enmStrategy}
     */
    public final CompositeStrategy getCompositeStrategy() {
        return enmStrategy;
    }


    /**
     * @return the current value of property {@link #cntQueries}
     */
    public final int getQueryCount() {
        return cntQueries;
    }


    /**
     * @return the current value of property {@link #cntSources}
     */
    public final int getSourceCount() {
        return cntSources;
    }


    /**
     * @return the current value of property {@link #lngDuration}
     */
    public final long getTimeDuration() {
        return lngDuration;
    }


    /**
     * @return the current value of property {@link #strFilePrefix}
     */
    public final String getFilePrefix() {
        return strFilePrefix;
    }
    
    /**
     * @return  the entire collection of composite query records
     */
    public final Vector<TestQueryRecord>    getQueryRecordsAll() {
        return this.vecRecords;
    }

    /**
     * <p>
     * Returns the composite query record at the given index.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * Indices are zero-based and run from 0, 1, ..., {@link #cntQueries} - 1.
     * </p>
     * 
     * @param index index into the vector of composite query records ({@link #vecRecords})
     * 
     * @return      the composite query record at given index
     * 
     * @throws IndexOutOfBoundsException    if the index is greater than the number of composite queries - 1
     */
    public final TestQueryRecord    getQueryRecord(int index) throws IndexOutOfBoundsException {
        return this.vecRecords.elementAt(index);
    }
    
    /**
     * <p>
     * Recovers and/or returns the results set associated with this 
     * <code>TestQueryCompositeRecord</code> record.
     * </p>
     * <p>
     * This method can be used in general to acquire the results set.
     * The query responses are recovered from all the component 
     * <code>{@link TestQueryRecord}</code> records using a 
     * <code>{@link TestQueryRecord#recoverQueryResponses()}</code> invocation.
     * Note that all exceptions are produced by this invocation.
     * </p> 
     * 
     * @return      the result set of the query operation described by record,
     *              or <code>null</code> if the data file is missing and query operation failed
     * 
     * @throws IOException              the data file is corrupt
     * @throws ClassNotFoundException   the data file does not contain a List<QueryResponse> object
     * @throws DpGrpcException          unable to establish <code>TestQueryService</code> connection
     * 
     * @see {@link TestQueryRecord#recoverQueryResponses()}
     */
    public List<QueryDataResponse> recoverQueryResponses() throws IOException, ClassNotFoundException, DpGrpcException {
        List<QueryDataResponse>    lstResponses = new LinkedList<>();
        
        for (TestQueryRecord rec : this.vecRecords) {
            List<QueryDataResponse> lstCmpRsps = rec.recoverQueryResponses();
            
            lstResponses.addAll(lstCmpRsps);
        }
        
        return lstResponses;
    }
        
    /**
     * <p>
     * Convenience method for extracting the <code>BucketData</code> messages from the
     * results set of <code>QueryResponse</code> messages.
     * </p>
     * <p>
     * The query data are recovered from all the component 
     * <code>{@link TestQueryRecord}</code> records using a 
     * <code>{@link TestQueryRecord#recoverQueryData()}</code> invocation.
     * Note that all exceptions are produced by this invocation.
     * </p>
     *  
     * @return  an ordered list of extracted <code>BucketData</code> messages from results set,
     *          or <code>null</code> if result set is  not available
     *           
     * @throws IOException              the data file is corrupt
     * @throws ClassNotFoundException   the data file does not contain a List<QueryResponse> object
     * @throws DpGrpcException          unable to establish <code>TestQueryService</code> connection
     * 
     * @see TestQueryRecord#recoverQueryData()
     */
    public List<QueryDataResponse.QueryResult.QueryData> recoverQueryData() throws IOException, ClassNotFoundException, DpGrpcException {
        List<QueryDataResponse.QueryResult.QueryData>  lstData = new LinkedList<>();
        
        for (TestQueryRecord rec : this.vecRecords) {
            List<QueryDataResponse.QueryResult.QueryData> lstCmpData = rec.recoverQueryData();
            
            lstData.addAll(lstCmpData);
        }
        
        return lstData;
    }
    
    /**
     * <p>
     * Creates and returns the composite request defined by this composite record.
     * </p>
     * <p>
     * Calls the <code>{@link TestQueryRecord#createRequest()}</code> method for component
     * record within this composite record.
     * </p>
     *  
     * @return  list of <code>{@link DpDataRequest}</code> objects for each component record
     */
    public List<DpDataRequest>  createCompositeRequest() {
        List<DpDataRequest> lstRequests = this.vecRecords
                .stream()
                .<DpDataRequest>map(TestQueryRecord::createRequest)
                .toList();
        
        return lstRequests;
    }
    

    //
    // Support Methods
    //
    
    /**
     * <p>
     * Builds all the <code>TestQueryRecord</code> instances for each composite query.
     * </p>
     * <p>
     * The composite query records are built according to the given <code>CompositeStrategy</code>
     * argument.
     * </p>
     * 
     * @param enmStrategy   strategy used to divide domain of target query
     * 
     * @see CompositeStrategy
     */
    private void buildSubQueryRecords(CompositeStrategy enmStrategy) {
        
        switch (enmStrategy) {

        case HORIZONTAL -> createRecordsHorizontal();
        case VERTICAL -> createRecordsVertical();
        case GRID -> createRecordsGrid();
        };
    }
    
    /**
     * Divides the target query domain horizontally, that is, by data source.
     */
    private void    createRecordsHorizontal() {
        
        int cntSourcesPerQuery = this.cntSources / this.cntQueries;
        
        for (int n=0; n<this.cntQueries; n++) {
            String  strFileName = this.buildFileName(n);
            int     indSourceFirst = n * cntSourcesPerQuery;
            
            TestQueryRecord rec = new TestQueryRecord(
                    strFileName, 
                    cntSourcesPerQuery, 
                    indSourceFirst, 
                    this.lngDuration, 
                    0L);

            this.vecRecords.add(rec);
        }
    }
    
    /**
     * Divides the target query domain vertically, that is, in time. 
     */
    private void    createRecordsVertical() {
        
        long lngDurationPerQuery = this.lngDuration / this.cntQueries;
        
        for (int m=0; m<this.cntQueries; m++) {
            String  strFileName = this.buildFileName(m);
            Long    lngTimeStart = m * lngDurationPerQuery;
            
            TestQueryRecord rec = new TestQueryRecord(
                    strFileName, 
                    this.cntSources, 
                    0, 
                    lngDurationPerQuery, 
                    lngTimeStart);

            this.vecRecords.add(rec);
        }
    }
    
    /**
     * Divides the target query domain in both horizontally and vertically, that is, 
     * by data sources and time. Creates a "grid" query domain.
     */
    private void    createRecordsGrid() {
        
        int cntQueriesBySource = this.cntQueries / 2;
        int cntQueriesByTime = this.cntQueries / 2;
        
        int     cntSourcesPerQuery = this.cntSources / cntQueriesBySource;
        long    lngDurationPerQuery = this.lngDuration / cntQueriesByTime;
        
        int indRecord = 0;
        
        for (int m=0; m<cntQueriesByTime; m++)
            for (int n=0; n<cntQueriesBySource; n++) {
                String  strFileName = this.buildFileName(indRecord);
                int     indSourceFirst = n * cntSourcesPerQuery;
                Long    lngTimeStart = m * lngDurationPerQuery;
                
                TestQueryRecord rec = new TestQueryRecord(
                        strFileName, 
                        cntSourcesPerQuery, 
                        indSourceFirst, 
                        lngDurationPerQuery, 
                        lngTimeStart);
                
                this.vecRecords.add(rec);
                indRecord++;
            }
    }
    
    /**
     * <p>
     * Builds the full filename for the persistent storage of query record.
     * </p>
     * <p>
     * File names are built by appending the index of the record to the file prefix property.
     * A ".dat" file extension is added.
     * </p>
     * 
     * @param i index of the query record in the vector of query records
     * 
     * @return full file name for the query record at given index
     */
    private String  buildFileName(int i) {
        String strFileName = this.strFilePrefix + "-" + Integer.toString(i) + ".dat"; 
     
        return strFileName;
    }
    
}
