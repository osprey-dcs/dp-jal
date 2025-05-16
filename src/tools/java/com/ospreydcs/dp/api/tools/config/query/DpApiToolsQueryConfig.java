package com.ospreydcs.dp.api.tools.config.query;

import com.ospreydcs.dp.api.config.common.DpLoggingConfig;
import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;
import com.ospreydcs.dp.api.config.query.DpQueryRecoveryConfig;
import com.ospreydcs.dp.api.tools.config.archive.DpTestArchiveConfig;
import com.ospreydcs.dp.api.tools.config.output.DpApiQueryOutputConfig;
import com.ospreydcs.dp.api.tools.config.request.DpTestRequestConfig;

/**
 * <p>
 * Structure class containing configuration parameters for tools query operations.
 * </p>
 */
@ACfgOverride.Root(root="DP_API_TOOLS_QUERY") 
public class DpApiToolsQueryConfig extends CfgStructure<DpApiToolsQueryConfig> {

    /** Default constructor required for base class */
    public DpApiToolsQueryConfig() { super(DpApiToolsQueryConfig.class); }

    
    //
    // Structure Fields
    //
    
    /** Query Service tools output locations */
    @ACfgOverride.Struct(pathelem="OUTPUT")
    public DpApiQueryOutputConfig   output;
    
    /** General Query Service tools logging configuration */
    @ACfgOverride.Struct(pathelem="LOGGING")
    /** The Data Platform test archive configuration parameter set */
    public DpLoggingConfig          logging;
    
    @ACfgOverride.Struct(pathelem="TEST_ARCHIVE")
    public DpTestArchiveConfig      testArchive;
    
    @ACfgOverride.Struct(pathelem="TEST_REQUESTS")
    public DpTestRequestConfig      testRequests;
    
    @ACfgOverride.Struct(pathelem="DATA")
    public DpQueryRecoveryConfig    recovery;
    
}

