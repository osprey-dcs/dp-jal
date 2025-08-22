package com.ospreydcs.dp.jal.tools.config.query;

import com.ospreydcs.dp.api.config.common.DpConcurrencyConfig;
import com.ospreydcs.dp.api.config.common.DpLoggingConfig;
import com.ospreydcs.dp.api.config.common.DpTimeoutConfig;
import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;
import com.ospreydcs.dp.api.config.query.DpQueryRecoveryConfig;
import com.ospreydcs.dp.jal.tools.config.request.JalTestRequestConfigDep;

/**
 * <p>
 * Structure class containing configuration parameters for tools query operations.
 * </p>
 * 
 * @deprecated Not used
 */
@Deprecated(since="Aug 22, 2025")
@ACfgOverride.Root(root="JAL_TOOLS_QUERY") 
public class JalToolsQueryConfigDep extends CfgStructure<JalToolsQueryConfigDep> {

    /** Default constructor required for base class */
    public JalToolsQueryConfigDep() { super(JalToolsQueryConfigDep.class); }

    
    //
    // Structure Fields
    //
    
//    /** Query Service tools common output location */
//    @ACfgOverride.Struct(pathelem="OUTPUT")
//    public JalToolsOutputConfig   output;
    
    /** General Query Service tools logging configuration */
    @ACfgOverride.Struct(pathelem="LOGGING")
    public DpLoggingConfig          logging;
    
    /** General tools testing timeout limits */
    @ACfgOverride.Struct(pathelem="TIMEOUT")
    public DpTimeoutConfig          timeout;
    
    /** General concurrency parameters */
    @ACfgOverride.Struct(pathelem="CONCURRENCY")
    public DpConcurrencyConfig      concurrency;
    
//    /** The Data Platform test archive configuration parameter set */
//    @ACfgOverride.Struct(pathelem="TEST_ARCHIVE")
//    public JalTestArchiveConfig      testArchive;

    /** Collection of common test requests and and request suites for tool use */
    @ACfgOverride.Struct(pathelem="TEST_REQUESTS")
    public JalTestRequestConfigDep      testRequests;
    
    /** The default configuration for data request recoveries */ 
    @ACfgOverride.Struct(pathelem="RECOVERY")
    public DpQueryRecoveryConfig    recovery;
    
}

