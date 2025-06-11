package com.ospreydcs.dp.api.config.common;

import java.time.DateTimeException;
import java.time.Duration;
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

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates a new, equivalent <code>{@link Duration}</code> instance from the field values.
     * </p>
     * <p>
     * The returned value is created from the fields <code>{@link #limit}</code> and <code>{@link #unit}</code>.
     * If the fields have not be populated or they represent an invalid time duration an exception is thrown.
     * </p>
     * 
     * @return  a new, equivalent <code>Duration</code> instance for the timeout limit
     * 
     * @throws DateTimeException    the time unit had an estimated duration (could not be converted)
     * @throws ArithmeticException  an arithmetic overflow occurred 
     */
    public Duration asDuration() throws DateTimeException, ArithmeticException {
        return Duration.of(this.limit, this.unit.toChronoUnit());
    }
}