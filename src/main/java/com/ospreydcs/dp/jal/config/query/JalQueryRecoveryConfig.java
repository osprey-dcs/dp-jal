package com.ospreydcs.dp.jal.config.query;

import com.ospreydcs.dp.jal.config.common.JalColSerializeConfig;
import com.ospreydcs.dp.jal.config.model.ACfgOverride;
import com.ospreydcs.dp.jal.config.model.CfgStructure;

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
    
    /** Default parameters for Query Service data recovery serialization use */
    @ACfgOverride.Struct(pathelem="SERIALIZE")
    public JalColSerializeConfig       serialize;
    
    /** Default parameters for Query Service time-series data request queries */
    @ACfgOverride.Struct(pathelem="REQUEST")
    public JalDataRequestConfig      request;
    
    /** Default parameters for Query Service time-series data request responses */
    @ACfgOverride.Struct(pathelem="RECOVERY")
    public JalDataRecoveryConfig     recovery;
    
    /** Default parameters for Query Service time-series data table results */
    @ACfgOverride.Struct(pathelem="TABLE")
    public JalDataTableConfig        table;
    
    
    /**
     * <p>
     * Structure class containing parameters for data recovery serialization.
     * </p>
     */
    public static final class Serialize extends CfgStructure<Serialize> {
        
        /** Default constructor required for base class */
        public Serialize() { super(Serialize.class);  }
        
        // 
        // Configuration Parameters
        //
        
        /** Is serialization enabled */
        @ACfgOverride.Field(name="ENABLED")
        public Boolean      enabled;
    }
    
    
}