package com.ospreydcs.dp.api.config.query;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * Structure class defining default configuration parameters for time-series data requests and responses
 */
@ACfgOverride.Root(root="DP_API_QUERY_DATA")
public class DpQueryRecoveryConfig extends CfgStructure<DpQueryRecoveryConfig> {
    
    /** Default constructor required for base structure class */
    public DpQueryRecoveryConfig() { super(DpQueryRecoveryConfig.class); };
    
    
    //
    // Configuration Fields
    //
    
    /** Default parameters for Query Service time-series data request queries */
    @ACfgOverride.Struct(pathelem="REQUEST")
    public DpDataRequestConfig      request;
    
    /** Default parameters for Query Service time-series data request responses */
    @ACfgOverride.Struct(pathelem="RESPONSE")
    public DpDataResponseConfig     response;
    
    /** Default parameters for Query Service time-series data table results */
    @ACfgOverride.Struct(pathelem="TABLE")
    public DpDataTableConfig        table;
}