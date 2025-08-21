/*
 * Project: dp-api-common
 * File:	DataTableCreator.java
 * Package: com.ospreydcs.dp.api.query.model.assem
 * Type: 	DataTableCreator
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
 * @since Aug 12, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.assem;

import java.io.PrintStream;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.IDataTable;
import com.ospreydcs.dp.api.common.JalDataTableType;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.query.DpQueryConfig;
import com.ospreydcs.dp.api.model.table.StaticDataTable;
import com.ospreydcs.dp.api.query.DpQueryException;
import com.ospreydcs.dp.api.query.model.table.SampledAggregateTable;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.api.util.Log4j;

/**
 * <p>
 * Class for creating <code>IDataTable</code> implementations from <code>SampledAggregate</code> instances.
 * </p>
 * <p>
 * Class objects create appropriate <code>IDataTable</code> implementations according to the JAL library configuration.
 * The interface <code>IDataTable</code> supports the requirements for a data table resulting from a time-series
 * data request from the Data Platform Query Service.
 * </p>
 * <p>
 * Data table implementation types are specified by the enumeration <code>{@link JalDataTableType}</code>.  Currently
 * there are 2 supported concrete implementations for <code>{@link IDataTable}</code>:
 * <ol>
 * <li>Static data tables - <code>{@link StaticDataTable}</code> </li>
 * <li>Dynamic data table - <code>{@link SampledAggregateTable}</code> </li>
 * </ol>
 * The above tables may be explicitly specified by the <code>JalDataTableType</code> constants
 * <code>{@link JalDataTableType#STATIC}</code> and <code>{@link JalDataTableType#DYNAMIC}</code>, respectively.
 * Note that the constant <code>{@link JalDataTableType#AUTO}</code> indicates that the JAL library is to
 * select the concrete implementation according to library configuration parameters.
 * </p>
 * <p>
 * <h2>Table Types:</h2>
 * Returned data table type implementation are determined by the size of the source data in
 * the <code>SampledAggregate</code> and the selected data table type.  The size of the source data can be
 * supplied explicitly with table creation method <code>{@link #createTable(SampledAggregate, long)}</code>,
 * or internally calculated with table creation method <code>{@link #createDataTable(SampledAggregate)}</code>.
 * The former method is useful when the size of the source data is already known from previous processing.
 * In that case computation of the source data size can be avoided potentially increasing performance.
 * In either case the configuration parameter in attribute <code>{@link #szTblStatMax}</code> is used for
 * comparison.
 * When using the table creation method <code>{@link #createTable(SampledAggregate, long)}</code> the configuration
 * value is compared to the second argument for table implementation type determination.
 * When using the table creation method <code>{@link #createDataTable(SampledAggregate)}</code>
 * the configuration value is compared to that given by <code>{@link SampledAggregate#getRawAllocation()}</code> to make
 * the above determination.
 * </p>
 * <p>
 * <h2>Usage:</h2>
 * <ul>
 * <li>
 * Create a new <code>DataTableCreator</code> instance using <code>{@link #create()}</code> or the default constructor.
 * </li>
 * <li>
 * Perform any costume configuration using the configuration methods
 *   <ul>
 *   <li><code>{@link #setTableType(JalDataTableType)}</code></li>
 *   <li><code>{@link #enableStaticTableDefault(boolean)}</code></li>
 *   <li><code>{@link #enableStaticTableMaxSize(boolean)}</code></li>
 *   <li><code>{@link #setStaticTableMaxSize(long)}</code></li>
 *   </ul>
 * <li>
 * Create new data tables from assembled <code>SampledAggregate</code> instances using 
 * <code>{@link #createTable(SampledAggregate)}</code> or <code>{@link #createTable(SampledAggregate, long)}</code>.
 * </li>
 * </ul>
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * A new <code>DataTableCreator</code> instance is created with the default configuration as specified in the
 * JAL library configuration default configuration.
 * </li>  
 * <li>
 * A <code>DataTableCreator</code> instance can be used indefinitely for multiple data table creations.  All
 * table creation for the instance is done according to its current configuration.
 * </li>
 * </ul>
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Aug 12, 2025
 *
 */
public class DataTableCreator {
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new <code>DataTableCreator</code> ready for <code>IDataTable</code> instance creation.
     * </p>
     * <p>
     * The returned <code>DataTableCreator</code> is configured according to JAL library configuration parameters.
     * Use the available configuration methods to customize any data table creation.
     * </p>
     * 
     * @return  new <code>DataTableCreator</code> in default configuration state
     */
    public static DataTableCreator  create() {
        return new DataTableCreator();
    }

    
    //
    // Library Resources
    //
    
    /** Query tools default configuration parameters */
    private static final DpQueryConfig      CFG_QUERY = DpApiConfig.getInstance().query;

    
    //
    // Class Constants
    //
    
    /** Event logging enabled flag */
    public static final boolean             BOL_LOGGING = CFG_QUERY.logging.enabled;
    
    /** Event logging level */
    public static final String              STR_LOGGING_LEVEL = CFG_QUERY.logging.level;
    
    
    /** The default data table type for data table creation */
    public static final JalDataTableType    ENM_TBL_TYPE_DEF = CFG_QUERY.data.table.result.type;
    
    /** The use static table as default flag in automatic data table creation */
    public static final boolean             BOL_TBL_STAT_DEF = CFG_QUERY.data.table.result.staticTbl.isDefault;
            
    /** The maximum size enable/disable flag for static data tables in automatic type creation */
    public static final boolean             BOL_TBL_STAT_MAX_DEF = CFG_QUERY.data.table.result.staticTbl.maxSizeEnable;
    
    /** The maximum size of a static data table for automatic type creation */
    public static final long                LNG_TBL_STAT_MAX_SZ_DEF = CFG_QUERY.data.table.result.staticTbl.maxSize;   

    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger     LOGGER = Log4j.getLoggerSetLevel(STR_LOGGING_LEVEL);
    
    
    //
    // Instance Attributes
    //
    
    /** Desired data table type for data table creation */
    private JalDataTableType        enmTblType = ENM_TBL_TYPE_DEF;
    
    /** Use static table as default in automatic data table creation */
    private boolean                 bolTblStatDef = BOL_TBL_STAT_DEF;
    
    /** Enable/disable maximum size limitation for static data tables */
    private boolean                 bolTblStatMax = BOL_TBL_STAT_MAX_DEF;
    
    /** Maximum static data table size (in bytes) when maximum size limit is enabled */
    private long                    szTblStatMax = LNG_TBL_STAT_MAX_SZ_DEF;
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>DataTableCreator</code> instance.
     * </p>
     *
     */
    public DataTableCreator() {
    }
    
    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Resets this <code>DataTableCreator</code> instance to the default configuration.
     * </p>
     * <p>
     * All configuration parameters for this instance are reset to the values at creation time.
     * These are the configuration parameters as specified in the JAL library default configuration.
     * </p>
     */
    public void resetDefaultConfiguration() {
        this.enmTblType = ENM_TBL_TYPE_DEF;
        this.bolTblStatDef = BOL_TBL_STAT_DEF;
        this.bolTblStatMax = BOL_TBL_STAT_MAX_DEF;
        this.szTblStatMax = LNG_TBL_STAT_MAX_SZ_DEF;
    }
    
    /**
     * <p>
     * Sets the preferred JAL data table type to the given value.
     * </p>
     * <p>
     * Sets the desired data table type to return from a time-series data request.
     * </p>
     * <p>
     * The given parameter decides the concrete implementation of <code>IDataTable</code> returned by a 
     * time-series data query operation.  The data table type has important repercussions on performance and
     * Java resource allocation (e.g., heap space).  See documentation on enumeration 
     * <code>{@link JalDataTableType}</code> for more information.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * Static data tables can create large Java heap space demand and should be used with caution (i.e., only
     * for smaller data requests).
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The enumeration <code>{@link JalDataTableType#UNSUPPORTED}</code> cannot be used.
     * <li>
     * The default value for this condition is given by class constant <code>{@link #ENM_TBL_TYPE_DEF}</code>
     * which is taken from the Java API Library configuration file.
     * </li>
     * </ul>
     * </p>
     * @implNote
     * The choice of data table implementation is performed in private method 
     * <code>{@link #selectTableImpl(SampledAggregate, long)}</code>.  
     *  
     * @param enmTblType the JAL data table type to be generated
     * 
     * @throws  UnsupportedOperationException   an illegal data table type is given
     */
    public void setTableType(JalDataTableType enmTblType) throws UnsupportedOperationException {
        
        // Check argument
        if (enmTblType == JalDataTableType.UNSUPPORTED) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Attempt to specify an illegal data table type " + enmTblType + ".";
            
            if (BOL_LOGGING)
                LOGGER.warn(strMsg);
            
            throw new UnsupportedOperationException(strMsg);
        }
        
        this.enmTblType = enmTblType;
    }
    
    /**
     * <p>
     * Sets the static data table default enable/disable flag for automatic data table creation.
     * <p>
     * <p>
     * Set this value <code>true</code> to first attempt using a static data table as the result of a 
     * time-series data request.  Note that a dynamic table can be still be created if the result set is 
     * too large and dynamic data tables are enabled. 
     * </p>
     * <p>
     * Set this value <code>false</code> to use dynamic data tables as the result for all time-series data
     * requests.  
     * </p> 
     * <p>
     * This parameter is only loosely defined at the current time.  However, the implementation functions as
     * follows:
     * <pre>
     *      If this value is <code>true</code> when automatic data table type creation is enabled 
     *      (i.e., with <code>{@link JalDataTableType#AUTO}</code>), a static data table is always 
     *      attempted first.
     * </pre>
     * Thus, this value is usually associated with automatic data table type selection and creation.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This parameter currently only has context when table type generation is set to automatic.</li>
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
     * @param bolEnable    a new static data table default flag in automatic data table type selection
     */
    public void enableStaticTableDefault(boolean bolEnable) {
        this.bolTblStatDef = bolEnable;
    }
    
    /**
     * <p>
     * Sets the static data table maximum size enable/disable flag.
     * </p>
     * <p>
     * When the static table maximum size is enabled (i.e., the argument is <code>true</code>) then the following
     * conditions are enforced:
     * <ul>
     * <li>table type = <code>{@link JalDataTableType#STATIC}</code>: If the table size is greater than a given 
     *     maximum value an exception is thrown (i.e., the table creation fails).
     * </li>
     * <li>table type = <code>{@link JalDataTableType#AUTO}</code>: If the table size is greater than a given 
     *     maximum value then the implementation pivots to a dynamics data table.
     * </li>
     * </ul>
     * </p>
     * <p>
     * Calling this method enforces a maximum size limitation for all static data tables returning the
     * results of time-series data requests.  The size limit (in bytes) is given by method
     * <code>{@link #setStaticTableMaxSize(long)}</code>.
     * If the result set size of a data request is larger than that argument value the implementation will
     * revert to a dynamic data table if automatic table creation is selected or an exception if static
     * data table creation is selected.
     * </p>
     * <p>
     * <h2>WARNING:</h2>
     * If the static table maximum size is disabled (i.e., the argument is <code>false</code> then the static table
     * is not size limited.  In this case the resultant table may consume extreme Java heap resources causing a
     * runtime error which is not caught is this class.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This parameter has context when the table type is set to <code>{@link JalDataTableType#STATIC}</code>
     *     or <code>{@link JalDataTableType#AUTO}</code>.
     * </li>
     * <li>
     * The default value for this parameter is given by class constant <code>{@link #BOL_TBL_STAT_MAX_DEF}</code>
     * which is taken from the Java API Library configuration file.
     * </li>
     * </ul>
     * </p>
     * 
     * @param bolEnable     static data table maximum size enable/disable flag
     */
    public void enableStaticTableMaxSize(boolean bolEnable) {
        this.bolTblStatDef = bolEnable;
    }
    
    /**
     * <p>
     * Sets the static data table maximum size limitation to the given value (in bytes).
     * </p>
     * <p>
     * When the static data table maximum size option is enabled this value is used to determine that maximum size.
     * See methods <code>{@link #enableStaticTableMaxSize(boolean)}</code> for a description of static maximum
     * size enforcement.
     * </p>
     * <p>
     * When using the table creation method <code>{@link #createDataTable(SampledAggregate)}</code>
     * the given value is compared to that given by <code>{@link SampledAggregate#getRawAllocation()}</code> to make
     * the above determination.
     * When using the table creation method <code>{@link #createTable(SampledAggregate, long)}</code> the given
     * value is compared to the second argument for table implementation type determination.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Maximum static table sizes should be chosen based upon platform memory and Java heap space allocation.</li>
     * <li>This parameter has context when the table type is set to <code>{@link JalDataTableType#STATIC}</code>
     *     or <code>{@link JalDataTableType#AUTO}</code>.
     * </li>
     * <li>
     * The default value for this condition is given by class constant <code>{@link #LNG_TBL_STAT_MAX_SZ_DEF}</code>
     * which is taken from the Java API Library configuration file.
     * </li>
     * </ul>
     * </p>
     * 
     * @param lngMaxSize    static data table maximum size limit
     */
    public void setStaticTableMaxSize(long lngMaxSize) {
        this.szTblStatMax = lngMaxSize;
    }
    
    
    /**
     * <p>
     * Returns the preferred data table type for data table creation.
     * </p>
     * 
     * @return  the data table type in the current configuration
     * 
     * @see #setTableType(JalDataTableType)
     */
    public JalDataTableType getTableType() {
        return this.enmTblType;
    }
    
    /**
     * <p>
     * Returns the static data table default enabled/disabled flag.
     * </p>
     * 
     * @return  the static data table enabled flag in the current configuration
     * 
     * @see #enableStaticTableDefault(boolean)
     */
    public boolean  isStaticTableDefaultEnabled() {
        return this.bolTblStatDef;
    }
    
    /**
     * <p>
     * Returns the static data table maximum size limitation enable/disable flag.
     * </p>
     * 
     * @return  the static data table maximum size limit enabled flag in the current configuration.
     * 
     * @see #enableStaticTableMaxSize(boolean)
     */
    public boolean  isStaticTableMaxSizeEnabled() {
        return this.bolTblStatMax;
    }
    
    /**
     * <p>
     * Returns the maximum size limit (in bytes) for static data table creation.
     * </p>
     * 
     * @return  maximum size limit for static tables (in bytes) in the current configuration
     * 
     * @see #setStaticTableMaxSize(long)
     */
    public long getStaticTableMaxSize() {
        return this.szTblStatMax;
    }
 
    
    /**
     * <p>
     * Prints out a text description of the current configuration to the given output stream.
     * </p>
     * <p>
     * A line-by-line text description of each configuration field is written to the given output.
     * The <code>strPad</code> is used to supply an optional whitespace character padding to the
     * left-hand side header for each line description.
     * </p>
     *   
     * @param ps        output stream to receive text description of configuration
     * @param strPad    white-space padding for each line header (or <code>null</code>)
     */
    public void printOutConfig(PrintStream ps, String strPad) {
        if (strPad == null)
            strPad = "";
        
        // Print out configuration parameters
//        ps.println(strPad + this.getClass().getSimpleName() + " Configuration");
        ps.println(strPad + "Data table type                : " + this.enmTblType.name());
        ps.println(strPad + "Static table default enabled   : " + this.bolTblStatDef);
        ps.println(strPad + "Static table max. size enabled : " + this.bolTblStatMax);
        ps.println(strPad + "Static table max. size (bytes) : " + this.szTblStatMax);
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates a new <code>IDataTable</code> implementation from the given <code>SampledAggregate</code> instance
     * and the current <code>DataTableAssembler</code> configuration.
     * </p>
     * <p>
     * This is a convenience method available whenever the size of the source data in the argument is not known.
     * This method defers to method <code>{@link #createTable(SampledAggregate, long)}</code> after computing
     * the second argument using method <code>{@link SampledAggregate#getRawAllocation()}</code>.
     * Method <code>{@link SampledAggregate#computeAllocationSize()}</code> provides a more accurate estimate
     * for source data size but is more computational intense.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>If source data size is known method <code>{@link #createTable(SampledAggregate, long)}</code> should be used.</li>
     * <li>See method documentation for <code>{@link #createTable(SampledAggregate, long)}</code> for further details
     *     on data table creation.</li>
     * </p>
     * 
     * @param blksAggr  source data for the returned <code>IDataTable</code> implementation
     * 
     * @return  a new <code>IDataTable</code> implementation based upon the current configuration
     * 
     * @throws DpQueryException table creation failed (see message and cause)
     */
    public IDataTable   createDataTable(SampledAggregate blksAggr) throws DpQueryException {

        long szData = blksAggr.getRawAllocation();
        
        return this.createTable(blksAggr, szData);
    }
    
    /**
     * <p>
     * Creates a new <code>IDataTable</code> implementation from the given <code>SampledAggregate</code> instance
     * and the current <code>DataTableAssembler</code> configuration.
     * </p>
     * <p>
     * In the current implementation,
     * the returned <code>IDataTable</code> implementation is either a static data table instance 
     * <code>{@link StaticDataTable}</code>, or a dynamic data table instance
     * <code>{@link SampledAggregateTable}</code>.  The returned implementation depends upon the
     * current configuration of this <code>DataTableCreator</code> object and the value of the
     * <code>szData</code> argument.
     * <p>
     * The <code>szData</code> argument is used for comparisons for static data table creation.
     * When automatic data table creation is selected a dynamic data table will be returned if the
     * argument is larger then the configuration limitation.  If static data table creation is selected
     * and the maximum size limitation is enabled an exception is thrown if the argument is larger
     * than the configuration limitation.  
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * This object can be used repeatedly for time-series data table creation.
     * </li>
     * <li>
     * The <code>szData</code> argument is used for comparisons for static data table creation.
     * <li>
     * All exceptions encountered during data table creation are subsequently packaged into a
     * <code>DpQueryException</code> for convenience.  See the message and cause for details
     * on the exceptional situation (most are caused by configuration violations).
     * </li>
     * <li>
     * See the class documentation for <code>{@link DataTableCreator}</code> more details 
     * on data table creation.
     * </li>
     * </ul>
     * </p>   
     * 
     * @param blksAggr  source data for the returned <code>IDataTable</code> implementation
     * @param szData    the size of the given <code>SampledAggregate</code> argument (in bytes)
     * 
     * @return  a new <code>IDataTable</code> implementation based upon the current configuration
     * 
     * @throws DpQueryException table creation failed (see message and cause)
     */
    public IDataTable   createTable(SampledAggregate blksAggr, long szData) throws DpQueryException {
        
        try {
            IDataTable  tbl = this.selectTableImpl(blksAggr, szData);
            
            return tbl;
            
        } catch (ConfigurationException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - Table creation failed with exception " + e.getClass().getSimpleName()
                    + ": " + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpQueryException(strMsg, e);
        }
    }
    

    //
    // Support Methods
    //
    
    /**
     * <p>
     * Selects an <code>IDataTable</code> implementation from the given <code>SampledAggregate</code> according to table type
     * preference parameter.
     * </p>
     * <p>
     * The returned <code>IDataTable</code> implementation is determined by the attribute <code>{@link #enmTblType}</code>.
     * A switch statement is used to selected the returned concrete instance according to the following:
     * <ul>
     * <li><code>{@link JalDataTableType#STATIC}</code> - returns <code>{@link SampledAggregate#createStaticDataTable()}</code>.</li>
     * <li><code>{@link JalDataTableType#DYNAMIC}</code> - returns <code>{@link #createTableImplStatic(SampledAggregate, long)}</code>.</li>
     * <li><code>{@link JalDataTableType#AUTO}</code> - returns <code>{@link #createTableImplAuto(SampledAggregate, long)}</code>.</li>
     * Any other enumeration value throws an exception.
     * </ul>
     * </p>
     * <p>
     * The resulting <code>IDataTable</code> implementation, either <code>StaticDataTable</code> or 
     * <code>SampledAggregateTable</code> depends upon the size of the sampling process (as specified by the argument)
     * and the configuration parameters of the Java API Library.  The different implementations are given as follows:
     * <ul>
     * <li><code>{@link StaticDataTable}</code> - data table is fully populated with timestamps and data values. </li>
     * <li><code>{@link SampledAggregateTable}</code> - data table can be sparse, timestamps and data values and can be computed
     *                                         on demanded (e.g., missing data values).</li> 
     * </ul>
     * See the class documentation for each data type for further details.
     * </p>
     * 
     * @param blksAggr  sampled aggregate created from a set of <code>RawCorrelatedData</code> objects
     * 
     * @return  an appropriate <code>IDataTable</code> implementation
     * 
     * @throws ConfigurationException    library configuration and size requirements are incompatible for table creation
     * 
     * @see StaticDataTable
     * @see SampledAggregateTable
     */
    private IDataTable  selectTableImpl(SampledAggregate blksAggr, long szData) throws ConfigurationException {
        
        return switch (this.enmTblType) {
        case DYNAMIC -> blksAggr.createDynamicDataTable();
        case STATIC -> this.createTableImplStatic(blksAggr, szData);
        case AUTO -> this.createTableImplAuto(blksAggr, szData);
        default -> throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() + " - Illegal table type " + this.enmTblType);
        };
    }
    
    /**
     * <p>
     * Creates a static data table implementation from the given arguments.
     * </p>
     * <p>
     * The method creates a <code>{@link StaticDataTable}</code> instance from method 
     * <code>{@link SampledAggregate#createStaticDataTable()}</code> if the following conditions hold:
     * <ul>
     * <li><code>{@link #bolTblStatMax}</code> = <code>false</code>. </li>
     * <li><code>{@link #bolTblStatMax}</code> = <code>true</code> AND 
     *   <code>{@link #szTblStatMax}</code> < argument <code>saData</code>. 
     * </li>
     * </ul>
     * Otherwise an exception is thrown.
     * </p>
     * 
     * @param blksAggr  sampled aggregate created from a set of <code>RawCorrelatedData</code> objects
     * @param szData    approximate size (in bytes) of sampled aggregate
     * 
     * @return  an <code>IDataTable</code> implementation of type <code>StaticDataTable</code>
     * 
     * @throws ConfigurationException    library configuration and size requirements are incompatible for table creation
     * 
     * @see StaticDataTable
     */
    private IDataTable  createTableImplStatic(SampledAggregate blksAggr, long szData) throws ConfigurationException {
        
        if (this.bolTblStatMax && (szData > this.szTblStatMax)) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Maximum static table size enabled with limit = " + this.szTblStatMax
                    + " and request size = " + szData + " is too large.";
            
            throw new ConfigurationException(strMsg);
        }
        
        return blksAggr.createStaticDataTable();
    }
    
    /**
     * <p>
     * Creates a static or dynamic data table depending upon the configuration parameters and data size.
     * </p>
     * <p>
     * This method is to be called when <code>{@link #enmTblType}</code> is equal to 
     * <code>{@link JalDataTableType#AUTO}</code>.
     * </p>
     * <p>
     * The resulting <code>IDataTable</code> implementation, either <code>StaticDataTable</code> or 
     * <code>SampledAggregateTable</code> depends upon the size of the sampling process (as specified by the argument)
     * and the configuration parameters of the Java API Library.  The different implementations are given as follows:
     * <ul>
     * <li><code>{@link StaticDataTable}</code> - data table is fully populated with timestamps and data values. </li>
     * <li><code>{@link SampledAggregateTable}</code> - data table can be sparse, timestamps and data values and can be computed
     *                                         on demanded (e.g., missing data values).</li> 
     * </ul>
     * See the class documentation for each data type for further details.
     * </p>
     * <p>
     * The <code>StaticDataTable</code> implementation is returned if the following conditions hold:
     * <ol>
     * <li><code>{@link #bolTblStatDef}</code> is <code>true</code> (static tables are the default).</li>
     * <li><code>{@link #szTblStatMax}</code> &le; <code>szData</code></li>.
     * </ol>
     * Otherwise, a <code>SampledAggregateTable</code> (dynamic table) is returned.
     * </p>
     * 
     * @param aggBlks   sampled aggregate created from a set of <code>RawCorrelatedData</code> objects
     * @param szData    approximate size (in bytes) of sampled aggregate
     * 
     * @return  an appropriate <code>IDataTable</code> implementation
     * 
     * @see StaticDataTable
     * @see SampledAggregateTable
     */
    private IDataTable  createTableImplAuto(SampledAggregate aggBlks, long szData) {

        if (this.bolTblStatDef && (szData < this.szTblStatMax)) 
            return aggBlks.createStaticDataTable();
        else 
            return aggBlks.createDynamicDataTable();
    }

}
    