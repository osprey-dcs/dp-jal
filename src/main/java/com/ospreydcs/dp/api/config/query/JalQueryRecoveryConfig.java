package com.ospreydcs.dp.api.config.query;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * Structure class defining default configuration parameters for time-series data requests and responses
 */
@ACfgOverride.Root(root="DP_API_QUERY_DATA")
public class JalQueryRecoveryConfig extends CfgStructure<JalQueryRecoveryConfig> {
    
    /** Default constructor required for base structure class */
    public JalQueryRecoveryConfig() { super(JalQueryRecoveryConfig.class); };
    
    
    //
    // Configuration Fields
    //
    
    /** Default parameters for Query Service time-series data request queries */
    @ACfgOverride.Struct(pathelem="REQUEST")
    public JalDataRequestConfig      request;
    
    /** Default parameters for Query Service time-series data request responses */
    @ACfgOverride.Struct(pathelem="RECOVERY")
    public JalDataRecoveryConfig     recovery;
    
    /** Default parameters for Query Service time-series data table results */
    @ACfgOverride.Struct(pathelem="TABLE")
    public JalDataTableConfig        table;
}