/*
 * Project: dp-api-common
 * File:	DpQueryServiceImplNew.java
 * Package: com.ospreydcs.dp.api.query.impl
 * Type: 	DpQueryServiceImplNew
 *
 * Copyright 2010-2025 the original author or authors.
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
 * @since Apr 21, 2025
 *
 */
package com.ospreydcs.dp.api.query.impl;

import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.ospreydcs.dp.api.common.DpGrpcStreamType;
import com.ospreydcs.dp.api.common.IDataTable;
import com.ospreydcs.dp.api.common.MetadataRecord;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.grpc.model.DpServiceApiBase;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnection;
import com.ospreydcs.dp.api.grpc.query.DpQueryConnectionFactory;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.model.table.StaticDataTable;
import com.ospreydcs.dp.api.query.DpDataRequest;
import com.ospreydcs.dp.api.query.DpMetadataRequest;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.DpQueryStreamBuffer;
import com.ospreydcs.dp.api.query.IQueryService;
import com.ospreydcs.dp.api.query.model.assem.QueryRequestRecoverer;
import com.ospreydcs.dp.api.query.model.assem.QueryResponseAssembler;
import com.ospreydcs.dp.api.query.model.assem.SampledAggregate;
import com.ospreydcs.dp.api.query.model.correl.RawCorrelatedData;
import com.ospreydcs.dp.api.query.model.correl.RawDataCorrelator;
import com.ospreydcs.dp.api.query.model.table.SampledAggregateTable;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;

/**
 * <p>
 * Java Library API Implementation of <code>IQueryService</code> interface.
 * </p>
 * <p>
 * This class is currently the primary implementation for Query Service clients.  The class implements all
 * the operations of the Query Service API <code>IQueryService</code>.  Moreover, it supports both 
 * timestamp lists and time-domain collisions within result sets.
 * </p>
 * <p>
 * Note that the <code>IQueryService</code> interface hides details of the underlying gRPC library 
 * resources and Protocol Buffers interfaces.
 * See the class documentation for <code>{@link IQueryService}</code> for more details.
 * </p>
 * <p>
 * <h1>USAGE</h1>
 * In general clients should not need direct access to this implementation.  All interactions should be
 * through the <code>IQueryService</code> interface.  However, we list some additional details below.
 *  
 * <h2>Instance Creation</h2>
 * Instance of <code>DpQueryServiceImplNew</code> should be obtained from the Query Service connection
 * factory class <code>{@link DpQueryServiceApiFactoryOld}</code>.  This is a utility class providing
 * various connection options to the Query Service, including a default connection defined in
 * the API configuration parameters.
 * <h2>Query Operations</h2>
 * All Query Service query methods are prefixed with <code>query</code>.  There are currently
 * 3 types of queries offered by class <code>DpQueryServiceImplNew</code>:
 * <ul>
 * <br/>
 * <li>
 *   <b>Process Variable Metadata</b> - methods prefixed with <code>queryMeta</code>.
 *   <br/>
 *   The methods take a <code>{@link DpMetadataRequest}</code> instance defining the request and
 *   return a list of <code>{@link MetadataRecord}</code> instances matching the request.
 *   The methods block until all data is available.
 * </li>
 * <br/>
 * <li>
 *   <b>Time-Series Data</b> - methods prefixed with <code>queryData</code>. 
 *   <br/>
 *   The methods take a <code>{@link DpDataRequest}</code> instance defining the request and
 *   return an <code>{@link IDataTable}</code> implementation containing the results.
 *   The methods block until all data is available.
 * </li>
 * <br/>
 * <li>
 *   <b>Raw Time-Series Data</b> - methods prefixed with <code>queryDataStream</code>.
 *   <br/>
 *   These are advanced operations offered to clients that which to do their own data processing
 *   along with some stream management.  Here requests are again made using a 
 *   <code>{@link DpDataRequest}</code> object but the returned object is a 
 *   <code>{@link DpQueryStreamBuffer}</code> instance, not results are yet available. 
 *   Instead, the data stream is initiated with a invocation of 
 *   <code>{@link DpQueryStreamBuffer#start()}</code> after which results are dynamically 
 *   available, or <code>{@link DpQueryStreamBuffer#startAndAwaitCompletion()}</code> which does
 *   not returned until the data buffer has received all data from the Query Service. 
 * </li>
 * </ul>
 * Note that the above methods DO NOT necessarily conform to the gRPC interface operations  
 * within <code>{@link DpQueryServiceGrpc}</code>. 
 * </p>
 * <p>
 * <h2>Shutdowns</h2>
 * Always shutdown the interface when no long needed.  This action releases all internal resources
 * required of this interface ensuring maximum performance.  Shutdown operations are provided by
 * the methods <code>{@link #shutdown()}</code> and <code>{@link #shutdownNow()}</code>.
 * The methods <code>{@link #awaitTermination()}</code> and 
 * <code>{@link #awaitTermination(long, java.util.concurrent.TimeUnit)}</code> are available
 * to block until shutdown is complete (this step is not required).
 * </p>
 * <p>
 * <h1>GENERAL NOTES</h1>
 * <h2>Data Recovery and Correlation</h2>
 * A single data processor is used for all time-series data request recovery and correlation.  
 * For unary data requests this is a single <code>{@link RawDataCorrelator}</code> instance. 
 * For gRPC streaming request this is a single <code>{@link QueryRequestRecoverer}</code> instance.  
 * For streaming operations the data processor performs both the gRPC data 
 * streaming from the Query Service AND the correlation of incoming data.  
 * The data processor offers various configuration options where data can be simultaneously streamed and 
 * correlated, along with other multi-threading capabilities.  The data processor typically
 * uses multiple, concurrent gRPC data streams to recover results.  There are several points
 * to note about this implementation situation:
 * </p>
 * <p>
 * <h2>Data Coalescing and Assembly</code>
 * Once the request data is recovered and correlated it is then coalesced and assembled by a single
 * <code>{@link QueryResponseAssembler}</code> instance.  This class is designed to process any time-
 * domain collisions within the correlated data and resolve them into time range "super domains".
 * Once any time-domain issues are resolved the correlated data is coalesced into "sampled blocks"
 * which each contain a collection of sampled processes (i.e., PVs) all correlated to the same
 * timestamps.  At this point all data has been extracted from Protocol Buffers messages
 * and now presented in Java format. The final action of the assembler is to aggregate the blocks into a
 * <code>SampledAggregate</code> instance which contains all data of the original request in
 * an aggregated collection of sampled blocks. 
 * </p>
 * <p>
 * <h2>Data Table Creation</h2>
 * The <code>SampledAggregate</code> class is capable of creating data tables from its internal data.
 * These are objects exposing the <code>IDataTable</code> interface.  There are 2 types of data tables
 * available:
 * <ol>
 * <li>Static Data Tables - these tables contain all data; access is fast but they require the most heap allocation.</li>
 * <li>Dynamic Data Tables - data access is done on demand and is slower; however, heap requirements are reduced.</li>
 * </ol>
 * The <code>DpQueryServiceImplNew</code> class decides which type of table to return based depending upon its
 * current configuration.  The default table configuration is taken from the Java API Library configuration file
 * but can be changed using the configuration methods.
 * </p>
 * <p> 
 * The <code>DpQueryServiceImplNew</code> architecture is also designed to handle the filtering of request data, 
 * however, this feature is not currently implemented.  Filtering would require a special "Filter" class that 
 * could recognize numeric types within recovered data and apply numeric value conditions.  The likely location
 * for request filtering would be in the request data assembly.
 * </p>
 * <p>
 * <h2>Implementation Notes</h2> 
 * <ul>
 * <li>
 * Since a single data correlator and assembler is used within this Query Service API, all data request
 * operations are synchronized.  Only one time-series data request is performed at any
 * instance and competing threads must wait until completed.
 * </li>
 * <li>
 * The data processor can be tuned with various configuration parameters within the 
 * <code>dp-api-config.yml</code> configuration file.  See class documentation on 
 * <code>{@link QueryResponseProcessorNew}</code> for more information on performance tuning.
 * </li>
 * <li>
 * It is informative to note that the <code>DpQueryServiceImplNew</code> class shares its single gRPC 
 * channel connection with its data processor instance.  Thus, any use of multiple, concurrent data
 * streams is performed over the single gRPC channel object.
 * </ul>
 * </p>
 * <p>
 * <h2>gRPC Connection</h2>
 * All communication to the Query Service is handled through a single gRPC channel instance.
 * These channel objects supported concurrency and multiple data streams between a client
 * and the targeted service.  However, excessive (thread) concurrency for a single 
 * <code>DpQueryServiceImplNew</code> instance may over-burden the single channel.
 * </p>
 * <p>
 * <h2>Best Practices</h2>
 * <ul>  
 * <li>Due to the conditions addressed above, clients utilizing extensive concurrency should 
 *     create multiple instances of <code>DpQueryServiceImplNew</code> (each containing a single gRPC
 *     channel and a data processor).</li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 21, 2025
 *
 */
public class DpQueryServiceImplNew extends
                DpServiceApiBase<DpQueryServiceImplNew, 
                DpQueryConnection, 
                DpQueryServiceGrpc, 
                DpQueryServiceBlockingStub, 
                DpQueryServiceFutureStub, 
                DpQueryServiceStub> 
    implements IQueryService {

    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>DpQueryServiceImplNew</code> attached to the given connection.
     * </p>
     * <p>
     * This method is available primarily for unit testing.  Java API Library clients should generally obtain
     * implementations of <code>IQueryService</code> from the Query Service connection factory.
     * </p> 
     * <p>
     * The argument should be obtained from the appropriate connection factory,
     * specifically, <code>{@link DpQueryConnectionFactory}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This object takes ownership of the given Query Service connection.  Do not attempt to shut down the connection
     * externally while using this instance.  Use methods described below.
     * </li>
     * <li>
     * The returned object should be shut down when no longer needed using 
     * <code>{@link #shutdown()}</code> or <code>{@link #shutdownNow()}</code>.  
     * This action is necessary to release unused gRPC resources and maintain 
     * overall performance.
     * </li>  
     * </ul>
     * </p>
     * 
     * @param connQuery  the gRPC channel connection to the desired DP Query Service
     *  
     * @return new <code>DpQueryServiceImplOld</code> interfaces attached to the argument
     */
    public static DpQueryServiceImplNew from(DpQueryConnection connQuery) {
        return new DpQueryServiceImplNew(connQuery);
    }
    
    
    //
    // Application Resources
    //
    
    /** Default Query Service configuration parameters */
    private static final DpQueryConfig  CFG_DEF = DpApiConfig.getInstance().query;
    
    
    //
    // Class Constants
    //
    
    /** Is static data table default for time-series data results */
    private static final boolean        BOL_TBL_STATIC_DEF = CFG_DEF.data.table.sstatic.isDefault;
    
    /** Do static data table have a maximum size */
    private static final boolean        BOL_TBL_STATIC_HAS_MAX = CFG_DEF.data.table.sstatic.hasMaxSize;
    
    /** Static data table maximum size (if applicable) */
    private static final int            SZ_TBL_STATIC_MAX = CFG_DEF.data.table.sstatic.maxSize;
    
    
    /** Enable dynamic data tables for time-series data results */
    private static final boolean        BOL_TBL_DYN_ENABLE = CFG_DEF.data.table.dynamic.enable;
    
//    /** Is dynamic data table default for time-series data results */
//    private static final boolean        BOL_TBL_DYN_DEF = CFG_DEF.data.table.dynamic.isDefault;
    
    
    /** Event logging enabled flag */
    private static final boolean        BOL_LOGGING = CFG_DEF.logging.enabled;
    
    /** Event logging level */
    private static final String         STR_LOGGING_LEVEL = CFG_DEF.logging.level;
    
    
    /** General query timeout limit */
    private static final long           LNG_TIMEOUT = CFG_DEF.timeout.limit;
    
    /** General query timeout units */
    private static final TimeUnit       TU_TIMEOUT = CFG_DEF.timeout.unit;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger         LOGGER = LogManager.getLogger();
    
    
    /** Class Resource Initialization - Initializes the event logger, sets logging level. */
    static { 
        Configurator.setLevel(LOGGER, Level.toLevel(STR_LOGGING_LEVEL, LOGGER.getLevel())); 
    }
    
    
    //
    // Instance Resources
    //
    
    /** The single raw data correlator used for correlating unary time-series data requests */
    private final RawDataCorrelator         prcrRspns;
    
    /** The single metadata request processor used for recovering and assembling metadata */
    private final MetadataRequestProcessor  prcrMeta;
    
    /** The single query request processor used for recovering and correlating streamed, time-series data requests */
    private final QueryRequestRecoverer  prcrRqsts;
    
    /** The single query response assembler used for assembling the recovered raw, correlated data */
    private final QueryResponseAssembler    assmRspns;
    
    
    //
    // Instance Configuration
    //
    
    /** Is static data table default for time-series data results */
    private boolean     bolTblStatDef = BOL_TBL_STATIC_DEF;
    
    /** Do static data table have a maximum size */
    private boolean     bolTblStatHasMax = BOL_TBL_STATIC_HAS_MAX;
    
    /** Static data table maximum size (if applicable) */
    private int         szTblStatMax = SZ_TBL_STATIC_MAX;
    
    /** Enable dynamic data tables for time-series data results */
    private boolean     bolTblDynEnable = BOL_TBL_DYN_ENABLE;

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DpQueryServiceImplOld</code>.
     * </p>
     * <p>
     * The argument should be obtained from the appropriate connection factory,
     * specifically, <code>{@link DpQueryConnectionFactory}</code>.
     * </p>
     * <ul>
     * <li>
     * This object takes ownership of the given Query Service connection.  Do not attempt to shut down the connection
     * externally while using this instance.
     * </li>
     * <li>
     * The returned object should be shut down when no longer needed using 
     * <code>{@link #shutdown()}</code> or <code>{@link #shutdownNow()}</code>.  
     * This action is necessary to release unused gRPC resources and maintain 
     * overall performance.
     * </li>  
     * </p>
     * 
     * @param connQuery  the gRPC channel connection to the desired DP Query Service 
     * 
     * @see DpQueryConnectionFactory
     */
    public DpQueryServiceImplNew(DpQueryConnection connQuery) {
        super(connQuery);
        
        this.prcrRspns = RawDataCorrelator.create();
        this.prcrMeta = MetadataRequestProcessor.from(connQuery);
        this.prcrRqsts = QueryRequestRecoverer.from(connQuery);
        this.assmRspns = QueryResponseAssembler.create(); 
    }
    
    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Sets all configuration parameters to their default values.
     * </p>
     * <p>
     * All configuration parameters are set to their default values as defined in the Java API Library
     * configuration file.  These values are recorded in the following class constants:
     * <ul>
     * <li><code>{@link #BOL_TBL_STATIC_DEF}</code></li>
     * <li><code>{@link #BOL_TBL_STATIC_HAS_MAX}</code></li>
     * <li><code>{@link #SZ_TBL_STATIC_MAX}</code></li>
     * <li><code>{@link #BOL_TBL_DYN_ENABLE}</code></li>
     * </ul>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method is provided primarily for unit testing and is not available through the 
     * <code>IQueryService</code> interface.
     * </li>
     * </ul>
     * </p>
     */
    public void resetDefaultConfiguration() {
        
        this.bolTblStatDef = BOL_TBL_STATIC_DEF;
        this.bolTblStatHasMax = BOL_TBL_STATIC_HAS_MAX;
        this.szTblStatMax = SZ_TBL_STATIC_MAX;
        this.bolTblDynEnable = BOL_TBL_DYN_ENABLE;
        
        this.prcrRspns.resetDefaultConfiguration();
        this.prcrRqsts.resetDefaultConfiguration();
        this.assmRspns.resetDefaultConfiguration();
    }
    
    /**
     * <p>
     * Sets/clears the "use static data table as default" flag.
     * </p>
     * <p>
     * Set this value <code>true</code> to first attempt using a static data table as the result of a 
     * time-series data request.  Note that a dynamic table can be still be created if the result set is 
     * too large and dynamic data tables are enabled. 
     * </p>
     * <p>
     * Set this value <code>false</code> to use dynamic data tables as the result of a time-series data
     * request.  Note that dynamic data tables must be enabled (see <code>{@link #enableDynamicTables(boolean)}</code>)
     * in this case, otherwise a pathological configuration exists and all time-series data requests will
     * throw exceptions.
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method is provided primarily for unit testing and is not available through the 
     * <code>IQueryService</code> interface.
     * </li>
     * <li>
     * The default value for this parameter is given by class constant <code>{@link #BOL_TBL_STATIC_DEF}</code>
     * which is taken from the Java API Library configuration file.
     * </li>
     * </ul>
     * </p>
     *  
     * @param bolDefault   set <code>true</code> to use static data tables as default, 
     *                     set <code>false</code> to use dynamic data tables 
     */
    public void setStaticTableDefault(boolean bolDefault) {
        this.bolTblStatDef = bolDefault;
    }
    
    /**
     * <p>
     * Enables static data table maximum size and specifies the size limit.
     * </p>
     * <p>
     * Calling this method enforces a maximum size limitation for all static data tables returning the
     * results of time-series data requests.  The size limit (in bytes) is given by the argument value.
     * If the result set size of a data request is larger than the argument value the implementation will
     * revert to a dynamic data table (assuming dynamic data tables are enabled).
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method is provided primarily for unit testing and is not available through the 
     * <code>IQueryService</code> interface.
     * </li>
     * <li>
     * The default value for this parameter is given by class constant <code>{@link #szTblStatMax}</code>
     * which is taken from the Java API Library configuration file.
     * </li>
     * </ul>
     * </p>
     * 
     * @param szMax maximum allowable size (in bytes) of a static data table
     */
    public void enableStaticTableMaxSize(int szMax) {
        this.bolTblStatHasMax = true;
        this.szTblStatMax = szMax;
    }
    
    /**
     * <p>
     * Disables the enforcement of a size limit for static data tables.
     * </p>
     * <p>
     * Calling this method disables size limitations for static data tables used as the result of a time-series
     * data request.  Result sets of any size will be returned as a static data table if static tables are the
     * default (see <code>{@link #setStaticTableDefault(boolean)}</code>).
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method is provided primarily for unit testing and is not available through the 
     * <code>IQueryService</code> interface.
     * </li>
     * <li>
     * The default value for this condition is given by class constant <code>{@link #bolTblStatHasMax}</code>
     * which is taken from the Java API Library configuration file.
     * </li>
     * </ul>
     * </p>
     * 
     */
    public void disableStaticTableMaxSize() {
        this.bolTblStatHasMax = false;
    }

    /**
     * <p>
     * Enables/disables the use of dynamic data tables for time-series request results.
     * </p>
     * <p>
     * Setting this value <code>true</code> ensures that dynamic data tables are available for providing the results
     * of time-series data requests.  Note that static data tables may still be available depending upon the value
     * of <code>{@link #bolTblStatDef}</code> (see <code>{@link #setStaticTableDefault(boolean)}</code>). 
     * </p>
     * <p>
     * Setting this value <code>false</code> will prevent dynamic data table from being returned.  If there is a
     * size limit on static data tables an exception will be thrown if that limit is violated.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This method is provided primarily for unit testing and is not available through the 
     * <code>IQueryService</code> interface.
     * </li>
     * <li>
     * The default value for this parameter is given by class constant <code>{@link #bolTblDynEnable}</code>
     * which is taken from the Java API Library configuration file.
     * </li>
     * </ul>
     * </p>
     * 
     * @param bolEnable set or clear the use of dynamic data tables for time-series request results
     */
    public void enableDynamicTables(boolean bolEnable) {
        this.bolTblDynEnable = bolEnable;
    }
    
    
    //
    // IConnection Interface
    //
    
    /**
     * @see com.ospreydcs.dp.api.grpc.model.IConnection#shutdown()
     */
    @Override
    public boolean shutdown() throws InterruptedException {
        return super.shutdown();
    }

    /**
     * @see com.ospreydcs.dp.api.grpc.model.IConnection#shutdownNow()
     */
    @Override
    public boolean shutdownNow() {
        return super.shutdownNow();
    }

    /**
     * @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public boolean awaitTermination(long cntTimeout, TimeUnit tuTimeout) throws InterruptedException {
        return super.awaitTermination(cntTimeout, tuTimeout);
    }

    /**
     * @see com.ospreydcs.dp.api.grpc.model.IConnection#awaitTermination()
     */
    @Override
    public boolean awaitTermination() throws InterruptedException {
        return super.awaitTermination(LNG_TIMEOUT, TU_TIMEOUT);
    }

    /**
     * @see com.ospreydcs.dp.api.grpc.model.IConnection#isShutdown()
     */
    @Override
    public boolean isShutdown() {
        return super.isShutdown();
    }

    /**
     * @see com.ospreydcs.dp.api.grpc.model.IConnection#isTerminated()
     */
    @Override
    public boolean isTerminated() {
        return super.isTerminated();
    }

    
    //
    // IQueryService Interface Implementation
    //

    /**
     * @see com.ospreydcs.dp.api.query.IQueryService#queryMeta(com.ospreydcs.dp.api.query.DpMetadataRequest)
     */
    @Override
    public List<MetadataRecord> queryMeta(DpMetadataRequest rqst) throws DpQueryException {

        List<MetadataRecord>    lstRcrds = this.prcrMeta.processRequest(rqst);
        
        return lstRcrds;
    }

    /**
     * @see com.ospreydcs.dp.api.query.IQueryService#queryDataUnary(com.ospreydcs.dp.api.query.DpDataRequest)
     */
    @Override
    public IDataTable queryDataUnary(DpDataRequest rqst) throws DpQueryException {
        
        // Perform request directly on Query Service connection
        QueryDataRequest    msgRqst = rqst.buildQueryRequest();
        QueryDataResponse   msgRsp = super.grpcConn.getStubBlock().queryData(msgRqst);
        
        // Check for request exception
        if (msgRsp.hasExceptionalResult()) {
            ExceptionalResult       msgExcept = msgRsp.getExceptionalResult();
            String                  strErrMsg = ProtoMsg.exceptionMessage(msgExcept, "Query Service");
            
            if (BOL_LOGGING)
                LOGGER.error(strErrMsg);
            
            throw new DpQueryException(strErrMsg);
        }
        
        // Process the response data
        try {
            QueryDataResponse.QueryData     msgData = msgRsp.getQueryData();

            // Correlate recovered data
            this.prcrRspns.processQueryData(msgData);   // throws IllegalArgumentException and CompletionException
            
            // Extract the correlated, raw data from the processor
            SortedSet<RawCorrelatedData>    setData = this.prcrRspns.getCorrelatedSet();
            long                            szData = this.prcrRspns.getBytesProcessed();
            
            // Assemble the raw, correlated data into data table and return
            SampledAggregate    aggBlks = this.assmRspns.process(setData);  // throws DpQueryException
            IDataTable          table = this.selectTableImpl(aggBlks, szData);
            
            // Reset correlator for next invocation 
            this.prcrRspns.reset();
            
            return table;
            
            // These exceptions are all from response data correlation
        } catch (CompletionException | IllegalArgumentException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                            + "Exception " + e.getClass() 
                            + " while correlating response data: " + e.getMessage();
            
            if (BOL_LOGGING) 
                LOGGER.error(strMsg);
            
            this.prcrRspns.reset();

            throw new DpQueryException(strMsg, e);
        }
        
    }

    /**
     * @see com.ospreydcs.dp.api.query.IQueryService#queryData(com.ospreydcs.dp.api.query.DpDataRequest)
     */
    @Override
    public IDataTable queryData(DpDataRequest rqst) throws DpQueryException {

        // Perform request and response correlation
        SortedSet<RawCorrelatedData>    setData = this.prcrRqsts.processRequest(rqst);
        long                            szData = this.prcrRqsts.getProcessedByteCount();
        String                          strRqstId = this.prcrRqsts.getRequestId();
        
        // Time domain processing and data coalescing/aggregation
        SampledAggregate    aggBlks = this.assmRspns.process(strRqstId, setData);
        IDataTable          table = this.selectTableImpl(aggBlks, szData);
        
        return table;
    }

    /**
     * @see com.ospreydcs.dp.api.query.IQueryService#queryData(java.util.List)
     */
    @Override
    public IDataTable queryData(List<DpDataRequest> lstRqsts) throws DpQueryException {

        // Perform request and response correlation
        SortedSet<RawCorrelatedData>    setData = this.prcrRqsts.processRequests(lstRqsts);
        long                            szData = this.prcrRqsts.getProcessedByteCount();
        
        // Time domain processing and data coalescing/aggregation
        SampledAggregate    aggBlks = this.assmRspns.process(setData);
        IDataTable          table = this.selectTableImpl(aggBlks, szData);
        
        return table;
    }

    /**
     * @see com.ospreydcs.dp.api.query.IQueryService#queryDataStream(com.ospreydcs.dp.api.query.DpDataRequest)
     */
    @Override
    public DpQueryStreamBuffer queryDataStream(DpDataRequest rqst) {
        
        // Extract the Protobuf request message and stream type
        QueryDataRequest    msgRequest = rqst.buildQueryRequest();
        DpGrpcStreamType    enmStreamType = rqst.getStreamType();
        
        // Create the query stream buffer and return it
        DpQueryStreamBuffer buf = DpQueryStreamBuffer.create(
                enmStreamType, 
                super.grpcConn.getStubAsync(),
                msgRequest,
                BOL_LOGGING);
        
        return buf;
    }

    //
    // Support Methods
    //
    
    /**
     * <p>
     * Selects an <code>IDataTable</code> implementation from the given <code>SampledAggregate</code> according to size.
     * </p>
     * <p>
     * The resulting <code>IDataTable</code> implementation, either <code>StaticDataTable</code> or 
     * <code>SampledAggregateTable</code> depends upon the size of the sampling process (as specified by the argument)
     * and the configuration parameters of the Java API Library.  The different implementations are given as follows:
     * <ul>
     * <li><code>StaticDataTable</code> - data table is fully populated with timestamps and data values. </li>
     * <li><code>SampledAggregateTable</code> - data table can be sparse, timestamps and data values and can be computed
     *                                         on demanded (e.g., missing data values).</li> 
     * </ul>
     * See the class documentation for each data type for further details.
     * </p>
     * <p>
     * The <code>StaticDataTable</code> implementation is returned if the following conditions hold:
     * <ol>
     * <li><code>{@link #bolTblStatDef}</code> is <code>true</code> (static tables are the default).</li>
     * <li>Either of the two conditions hold: 
     *   <ul>
     *   <li><code>{@link #bolTblStatHasMax}</code> = <code>false</code> OR</li>
     *   <li><code>{@link #bolTblStatHasMax}</code> = <code>true</code> AND <code>{@link #szTblStatMax}</code> &le; <code>szData</code></li>.
     *   </ul>
     * </li>
     * </ol>
     * Otherwise, a <code>SampledAggregateTable is returned IFF <code>{@link #bolTblDynEnable}</code> = <code>true</code>.
     * An exception is thrown if the value is <code>false</code>.
     * </p>
     * 
     * @param aggBlks   sampled aggregate created from a set of <code>RawCorrelatedData</code> objects
     * @param szData    approximate size (in bytes) of sampled aggregate
     * 
     * @return  an appropriate <code>IDataTable</code> implementation
     * 
     * @throws UnsupportedOperationException    library configuration and size requirements are incompatible for table creation
     * 
     * @see StaticDataTable
     * @see SampledAggregateTable
     */
    private IDataTable  selectTableImpl(SampledAggregate aggBlks, long szData) throws UnsupportedOperationException {

        // Attempt static data table construction
        if (this.bolTblStatDef) {
            if (!this.bolTblStatHasMax)
                return aggBlks.createStaticDataTable();
            else
                if (szData <= this.szTblStatMax)
                    return aggBlks.createStaticDataTable();
        }
        
        // Static data table creation failed, use dynamic table if enabled
        if (this.bolTblDynEnable)
            return aggBlks.createDynamicDataTable();
        
        // Could not create data table with the given configuration and table size
        else {
            String strMsg = " Result set size " + szData 
                        + " greater than maximum static table size " + this.szTblStatMax
                        + " and dynamic tables are DISABLED: Cannot create data table.";
            
            throw new UnsupportedOperationException(strMsg);
        }
    }

}
