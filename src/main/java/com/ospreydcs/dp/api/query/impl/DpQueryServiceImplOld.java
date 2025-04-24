/*
 * Project: dp-api-common
 * File:	DpQueryServiceImplOld.java
 * Package: com.ospreydcs.dp.api.query.impl
 * Type: 	DpQueryServiceImplOld
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
 * @since Feb 17, 2025
 *
 */
package com.ospreydcs.dp.api.query.impl;

import java.util.List;
import java.util.MissingResourceException;
import java.util.SortedSet;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.naming.CannotProceedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.ranges.RangeException;

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
import com.ospreydcs.dp.api.query.model.coalesce.SamplingProcess;
import com.ospreydcs.dp.api.query.model.correl.CorrelatedQueryDataOld;
import com.ospreydcs.dp.api.query.model.correl.QueryDataCorrelatorOld;
import com.ospreydcs.dp.api.query.model.table.SamplingProcessTable;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.ExceptionalResult;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceBlockingStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceFutureStub;
import com.ospreydcs.dp.grpc.v1.query.DpQueryServiceGrpc.DpQueryServiceStub;
import com.ospreydcs.dp.grpc.v1.query.QueryDataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryDataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryMetadataRequest;
import com.ospreydcs.dp.grpc.v1.query.QueryMetadataResponse;

/**
 * <p>
 * Java Library API Implementation of <code>IQueryService</code> interface.
 * </p>
 * <p>
 * This class is currently the secondary implementation for Query Service clients.  The class implements all
 * the operations of the Query Service API <code>IQueryService</code>, however, it does not support 
 * timestamp lists or time-domain collisions within result sets.
 * </p>
 * <p>
 * Note that the <code>IQueryService</code> interface hides details of the underlying gRPC library 
 * resources and Protocol Buffers interfaces.
 * See the class documentation for <code>{@link IQueryService}</code> for more details.
 * </p>
 * <p>
 * <h1>USAGE</h1>
 * In general clients should not need direct access to this implementation.  All interactions should be
 * through the <code>IQueryService</code> interface.  However, we list some details below.
 *  
 * <h2>Instance Creation</h2>
 * Instance of <code>DpQueryServiceImplOld</code> can be obtained from its creator.
 * The preferred Query Service connection factory class <code>{@link DpQueryServiceApiFactoryOld}</code> now
 * supplies <code>DpQueryServiceImplNew</code> instances by default.  The deprecated connection factory
 * still supplies <code>DpQueryServiceImplOld</code> instances and can also be used for instance
 * creation.  
 * <h2>Query Operations</h2>
 * All Query Service query methods are prefixed with <code>query</code>.  There are currently
 * 3 types of queries offered by class <code>DpQueryServiceImplOld</code>:
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
 * <h2>Data Processing</h2>
 * A single data processor is used for all time-series data requests.  For unary data requests this
 * is a single <code>{@link QueryDataCorrelatorOld}</code> instance. 
 * For gRPC streaming request this is a single <code>{@link QueryRequestProcessorOld}</code> instance .  
 * For streaming operations the data processor performs both the gRPC data 
 * streaming from the Query Service AND the correlation of incoming data.  It is also designed to handle
 * filtering of request data, however, this feature is not currently implemented.
 * The data processor offers various configuration options where data can be simultaneously streamed and 
 * correlated, along with other multi-threading capabilities.  The data processor typically
 * uses multiple, concurrent gRPC data streams to recover results.  There are several points
 * to note about this implementation situation:
 * <ul>
 * <li>
 * Since a single data processor is used within this Query Service API, all data request
 * operations are synchronized.  Only one time-series data request is performed at any
 * instance and competing threads must wait until completed.
 * </li>
 * <li>
 * The data processor can be tuned with various configuration parameters within the 
 * <code>dp-api-config.yml</code> configuration file.  See class documentation on 
 * <code>{@link QueryResponseProcessorOld}</code> for more information on performance tuning.
 * </li>
 * <li>
 * It is informative to note that the <code>DpQueryServiceImplOld</code> class shares its single gRPC 
 * channel connection with its data processor instance.  Thus, any use of multiple, concurrent data
 * streams is performed over the single gRPC channel object.
 * </ul>
 * </p>
 * <p>
 * <h2>gRPC Connection</h2>
 * All communication to the Query Service is handled through a single gRPC channel instance.
 * These channel objects supported concurrency and multiple data streams between a client
 * and the targeted service.  However, excessive (thread) concurrency for a single 
 * <code>DpQueryServiceImplOld</code> instance may over-burden the single channel.
 * </p>
 * <p>
 * <h2>Best Practices</h2>
 * <ul>  
 * <li>Due to the conditions addressed above, clients utilizing extensive concurrency should 
 *     create multiple instances of <code>DpQueryServiceImplOld</code> (each containing a single gRPC
 *     channel and a data processor).</li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Feb 17, 2025
 * 
 * @deprecated Replaced by DpQueryServiceNew
 */
@Deprecated(since="April 21, 2025")
public class DpQueryServiceImplOld extends 
    DpServiceApiBase<DpQueryServiceImplOld, 
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
     * Creates and returns a new instance of <code>DpQueryServiceImplOld</code> attached to the given connection.
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
    public static DpQueryServiceImplOld from(DpQueryConnection connQuery) {
        return new DpQueryServiceImplOld(connQuery);
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
    
    
    /** Logging active flag */
    private static final boolean        BOL_LOGGING = CFG_DEF.logging.active;
    
    
    /** General query timeout limit */
    private static final long           LNG_TIMEOUT = CFG_DEF.timeout.limit;
    
    /** General query timeout units */
    private static final TimeUnit       TU_TIMEOUT = CFG_DEF.timeout.unit;
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger         LOGGER = LogManager.getLogger();
    
    
    //
    // Instance Resources
    //
    
    /** The single query data correlator used for unary time-series data requests */
    private final QueryDataCorrelatorOld   prcrRspns;
    
    /** The single query request processor used for streaming time-series data requests */
    private final QueryRequestProcessorOld prcrRqsts;
    
    
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
    public DpQueryServiceImplOld(DpQueryConnection connQuery) {
        super(connQuery);
        
        this.prcrRspns = QueryDataCorrelatorOld.create();
        this.prcrRqsts = QueryRequestProcessorOld.from(connQuery);
    }
    
    
    //
    // Configuration
    //
    
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
    public void setDefaultConfiguration() {
        
        this.bolTblStatDef = BOL_TBL_STATIC_DEF;
        this.bolTblStatHasMax = BOL_TBL_STATIC_HAS_MAX;
        this.szTblStatMax = SZ_TBL_STATIC_MAX;
        this.bolTblDynEnable = BOL_TBL_DYN_ENABLE;
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
        return this.awaitTermination(LNG_TIMEOUT, TU_TIMEOUT);
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
    // IQueryService Interface
    //
    
    /**
     * @see com.ospreydcs.dp.api.query.IQueryService#queryMeta(com.ospreydcs.dp.api.query.DpMetadataRequest)
     */
    @Override
    public List<MetadataRecord> queryMeta(DpMetadataRequest rqst) throws DpQueryException {
        
        // Get the Protobuf request from the argument
        QueryMetadataRequest    msgRqst = rqst.buildQueryRequest();
        
        // Perform gRPC request
        QueryMetadataResponse   msgRsp = super.grpcConn.getStubBlock().queryMetadata(msgRqst);
        
        // Check for Query Service exception
        if (msgRsp.hasExceptionalResult()) {
            ExceptionalResult       msgExcept = msgRsp.getExceptionalResult();
            String                  strErrMsg = ProtoMsg.exceptionMessage(msgExcept, "Query Service");
            
            if (BOL_LOGGING)
                LOGGER.error(strErrMsg);
            
            throw new DpQueryException(strErrMsg);
        }
        
        // Unpack the response and return it
        try {
            List<MetadataRecord>      lstRecs = msgRsp
                    .getMetadataResult()
                    .getPvInfosList()
                    .stream()
                    .<MetadataRecord>map(ProtoMsg::toPvMetaRecord)
                    .toList();


            return lstRecs;

        } catch (IllegalArgumentException | TypeNotPresentException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                           + " - Could not convert response to MetadataRecord: "
                           + "exception=" + e.getClass().getSimpleName()
                           + ", message=" + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpQueryException(strMsg, e);
        }
    }

    /**
     * @see com.ospreydcs.dp.api.query.IQueryService#queryDataUnary(com.ospreydcs.dp.api.query.DpDataRequest)
     */
    @Override
    public IDataTable queryDataUnary(DpDataRequest rqst) throws DpQueryException {
        
        // Perform request directly on Query Service connection
        QueryDataRequest    msgRqst = rqst.buildQueryRequest();
        QueryDataResponse   msgRsp = super.grpcConn.getStubBlock().queryData(msgRqst);
        
        // Process the response data
        try {
            this.prcrRspns.addQueryResponse(msgRsp);
            
        // These exceptions are all from response data processing
        } catch (CompletionException | CannotProceedException | IllegalArgumentException | ExecutionException e) {
            if (BOL_LOGGING) 
                LOGGER.error("{} - Exception while recovering/correlating response: {}, {}}", JavaRuntime.getMethodName(), e.getClass().getSimpleName(), e.getMessage());

            throw new DpQueryException("Exception while recovering/correlating response: " + e.getClass() + ", " + e.getMessage(), e);
        }
        
        SortedSet<CorrelatedQueryDataOld>  setData = this.prcrRspns.getCorrelatedSet();
        long                            szData = this.prcrRspns.getBytesProcessed();
        
        IDataTable table = this.createTable(setData, szData);
        this.prcrRspns.reset();
        
        return table;
    }

    /**
     * @see com.ospreydcs.dp.api.query.IQueryService#queryData(com.ospreydcs.dp.api.query.DpDataRequest)
     */
    @Override
    synchronized
    public IDataTable queryData(DpDataRequest rqst) throws DpQueryException {
        
        // Perform request and response correlation
        SortedSet<CorrelatedQueryDataOld>  setData = this.prcrRqsts.processRequest(rqst);
        long                            szData = this.prcrRqsts.getProcessedByteCount();
        
        return this.createTable(setData, szData);
        
//        // Recover the sampling process, create data table, and return
//        // TODO - contains extensive error checking which may be removed when stable
//        try {
//            SamplingProcess process = SamplingProcess.from(setPrcdData);
//            IDataTable      table = this.selectTableImpl(process, szData);
//            
//            return table;
//        
//        // These exceptions are all from sampling process creation error checking
//        } catch (MissingResourceException | IllegalArgumentException | IllegalStateException | RangeException | TypeNotPresentException | CompletionException e) {
//            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
//                           + " - Failed to create SamplingProcess, exception thrown: type="
//                           + e.getClass().getSimpleName()
//                           + ", message=" + e.getMessage();
//            
//            if (BOL_LOGGING)
//                LOGGER.error(strMsg);
//            
//            throw new DpQueryException(strMsg, e);
//            
//        // Data table creation exception
//        } catch (UnsupportedOperationException e) {
//
//            if (BOL_LOGGING)
//                LOGGER.error("{} - Exception creating data table: {}", JavaRuntime.getQualifiedMethodNameSimple(), e);
//
//            throw new DpQueryException("Exception creating data table.", e);
//        }
    }

    /**
     * @see com.ospreydcs.dp.api.query.IQueryService#queryData(java.util.List)
     */
    @Override
    synchronized
    public IDataTable queryData(List<DpDataRequest> lstRqsts) throws DpQueryException {

        // Perform request and response correlation
        SortedSet<CorrelatedQueryDataOld>  setData = this.prcrRqsts.processRequests(lstRqsts);
        long                            szData = this.prcrRqsts.getProcessedByteCount();
        
        return this.createTable(setData, szData);
        
//        // Recover the sampling process, create data table, and return
//        // TODO - contains extensive error checking which may be removed when stable
//        try {
//            SamplingProcess process = SamplingProcess.from(setData);
//            IDataTable      table = this.selectTableImpl(process, szData);
//            
//            return table;
//        
//        // These exceptions are all from sampling process creation error checking
//        } catch (MissingResourceException | IllegalArgumentException | IllegalStateException | RangeException | TypeNotPresentException | CompletionException e) {
//            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
//                           + " - Failed to create SamplingProcess, exception thrown: type="
//                           + e.getClass().getSimpleName()
//                           + ", message=" + e.getMessage();
//            
//            if (BOL_LOGGING)
//                LOGGER.error(strMsg);
//            
//            throw new DpQueryException(strMsg, e);
//            
//        // Data table creation exception
//        } catch (UnsupportedOperationException e) {
//
//            if (BOL_LOGGING)
//                LOGGER.error("{} - Exception creating data table: {}", JavaRuntime.getQualifiedMethodNameSimple(), e);
//
//            throw new DpQueryException("Exception creating data table.", e);
//        }
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
                CFG_DEF.logging.active);
        
        return buf;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates and returns an <code>IDataTable</code> implementation containing for the given data set.
     * </p>
     * <p>
     * The method first creates a <code>SamplingProcess</code> instance from the given data set.  This action
     * can result in a variety of exceptions, most all due to error checking.  These exceptions are caught here
     * and all are returned within a <code>DpQueryException</code> instance.
     * Once the <code>SamplingProcess</code> instance is created, the appropriate <code>IDataTable</code> 
     * implementation is selected by deferring to internal method 
     * <code>{@link #selectTableImpl(SamplingProcess, long)}</code>.  This method throws 
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * There is extensive error checking within the <code>SamplingProcess</code> class.  To improve performance,
     * disabling the error checking may be warranted once the API is stabilized.  See class documentation for
     * <code>{@link SamplingProcess}</code> for further details.
     * </p>
     * 
     * @param setData   processed time-series request data set
     * @param szData    approximate size (in bytes) of processed data
     * 
     * @return  an appropriate <code>IDataTable</code> implementation
     * 
     * @throws DpQueryException    table creation failure (see cause - either SamplingProcess failure or failed table creation)
     * 
     * @see {@link #selectTableImpl(SamplingProcess, long)}
     * @see SamplingProcess
     * @see StaticDataTable
     * @see SamplingProcessTable
     */
    private IDataTable  createTable(SortedSet<CorrelatedQueryDataOld> setData, long szData) throws DpQueryException {
        
        // Recover the sampling process, create data table, and return
        // TODO - contains extensive error checking which may be removed when stable
        try {
            SamplingProcess process = SamplingProcess.from(setData);
            IDataTable      table = this.selectTableImpl(process, szData);
            
            return table;
        
        // These exceptions are all from sampling process creation error checking
        } catch (MissingResourceException | IllegalArgumentException | IllegalStateException | RangeException | TypeNotPresentException | CompletionException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                           + " - Failed to create SamplingProcess, exception thrown: type="
                           + e.getClass().getSimpleName()
                           + ", message=" + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpQueryException(strMsg, e);
            
        // Data table creation exception
        } catch (UnsupportedOperationException e) {

            if (BOL_LOGGING)
                LOGGER.error("{} - Exception creating data table: {}", JavaRuntime.getQualifiedMethodNameSimple(), e);

            throw new DpQueryException("Exception creating data table.", e);
        }
    }
    
    /**
     * <p>
     * Selects an <code>IDataTable</code> implementation from the given <code>SamplingProcess</code> according to size.
     * </p>
     * <p>
     * The resulting <code>IDataTable</code> implementation, either <code>StaticDataTable</code> or 
     * <code>SamplingProcessTable</code> depends upon the size of the sampling process (as specified by the argument)
     * and the configuration parameters of the Java API Library.  The different implementations are given as follows:
     * <ul>
     * <li><code>StaticDataTable</code> - data table is fully populated with timestamps and data values. </li>
     * <li><code>SamplingProcessTable</code> - data table can be sparse, timestamps and data values and can be computed
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
     * Otherwise, a <code>SamplingProcessTable is returned IFF <code>{@link #bolTblDynEnable}</code> = <code>true</code>.
     * An exception is thrown if the value is <code>false</code>.
     * </p>
     * 
     * @param process   sampling process created from a set of <code>CorrelatedQueryDataOld</code> objects
     * @param szData    approximate size (in bytes) of sampling process
     * 
     * @return  an appropriate <code>IDataTable</code> implementation
     * 
     * @throws UnsupportedOperationException    library configuration and size requirements are incompatible for table creation
     * 
     * @see StaticDataTable
     * @see SamplingProcessTable
     */
    private IDataTable  selectTableImpl(SamplingProcess process, long szData) throws UnsupportedOperationException {

        // Attempt static data table construction
        if (this.bolTblStatDef) {
            if (!this.bolTblStatHasMax)
                return process.createStaticDataTable();
            else
                if (szData <= this.szTblStatMax)
                    return process.createStaticDataTable();
        }
        
        // Static data table creation failed, use dynamic table if enabled
        if (this.bolTblDynEnable)
            return process.createDynamicDataTable();
        
        // Could not create data table with the given configuration and table size
        else {
            String strMsg = " Result set size " + szData 
                        + " greater than maximum static table size " + this.szTblStatMax
                        + " and dynamic tables are DISABLED: Cannot create data table.";
            
            throw new UnsupportedOperationException(strMsg);
        }
    }

}
