package com.ospreydcs.dp.api.config.common;

import java.util.concurrent.TimeUnit;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * <p>
 * Structure class containing general timeout parameters for (gRPC) operations.
 * <p>
 */
@ACfgOverride.Root(root="DP_API_TIMEOUT")
public final class DpTimeoutConfig extends CfgStructure<DpTimeoutConfig> {
    
    /** Default constructor required for structure base class */
    public DpTimeoutConfig() {
        super(DpTimeoutConfig.class);
    }

    /** Is timeout enabled */
    @ACfgOverride.Field(name="ENABLED")
    public Boolean      enabled;
    
    /** Timeout limit */
    @ACfgOverride.Field(name="LIMIT")
    public Long         limit;
    
    /** Units for the timeout limit */
    @ACfgOverride.Field(name="UNIT")
    public TimeUnit     unit;

}