package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.JalToolsDataGenConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/** 
 * <p>
 * Record containing <code>TimestampFactory</code> configuration parameters.
 * </p>
 * <p>
 * There are 2 possible configurations for timestamp factory configuration: 1) a random timestamp generation
 * factory, and 2) an incremental timestamp generation factory.  Random factories create random-valued
 * timestamps in the current epoch.  Incremental timestamp factories create timestamps with a given start
 * time and sampling period.  The type of timestamp factory is given by the <code>{@link #bolRand()}</code> field value.
 * </p>
 * <p>
 * Timestamp factories are created with method <code>{@link #newFactory()}</code>.
 * Only two field values are used for <code>TimestampFactory</code> instance creation, this depends upon the
 * value of <code>{@link #bolRand()}</code>.
 * <ul>
 * <li><code>{@link #bolRand()} = true</code> &rarr; <code>{@link TimestampFactory#from(boolean, long)}</code></li>.
 * <li><code>{@link #bolRand()} = true</code> &rarr; <code>{@link TimestampFactory#from(Duration, Instant)}</code></li>.
 * </ul>
 * The above conditions are consistent with the constructors of the <code>{@link TimestampFactory}</code> class.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * Random timestamp factories are more expensive than incremental timestamp factories, requiring a random number
 * generator for timestamp production.
 * </li>
 * <li>
 * Random timestamp factories produce timestamps using randomly generated <code>long</code> values.  The long
 * value is then used as a nanosecond offset past the given epoch <code>{@link Instant#EPOCH}</code>.
 * </li>
 * <li>
 * If a random timestamp factory configuration creator is used with the 'random' parameter set to <code>false</code>
 * the result timestamp factory produces a sequence of timestamps with contant value <code>{@link Instant#EPOCH}</code>.
 * </li>
 * </ul>
 * </p> 
 * 
 * @param bolRand   random generation timestamp generation enable/disable flag
 * @param lngSeed   seed value for the random number generator (use 0 for random seed)
 * @param durPeriod the difference (period) between generated timestamp values in the incremental sequence 
 * @param insStart  the first timestamp value in the incremental sequence
 * 
 * @see TimestampFactory
 */
public record TimestampFactorySpec(boolean bolRand, long lngSeed, Duration durPeriod, Instant insStart) {
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactorySpec</code> configuration for an incremental <code>TimestampFactory</code>
     * with all default arguments.
     * </p>
     * <p>
     * All <code>{@link TimestampFactory}</code> instances created from the returned produce sequences of timestamps
     * from the given <code>{@link #insStart}</code> value.  The following timestamp values are then separated by the
     * time interval <code>{@link #durPeriod}</code>.
     * </p>
     * <p>
     * <h2>Default Parameters</h2>
     * This creator uses default parameters values from the JAL Tools default configuration.
     * The following values are used:  
     * <ul>
     * <li><code>{@link #durPeriod} = {@link #DUR_INCR_PERIOD_DEF}</code>.</li>
     * <li><code>{@link #insStart} = {@link #INS_INCR_START_DEF}</code>.</li>
     * </ul>
     * </p>
     *  
     * @return  a new random <code>TimestampFactorySpec</code> configuration populated with all default arguments 
     */
    public static TimestampFactorySpec from() {
        return TimestampFactorySpec.from(DUR_INCR_PERIOD_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactorySpec</code> configuration for an incremental <code>TimestampFactory</code>.
     * </p>
     * <p>
     * All <code>{@link TimestampFactory}</code> instances created from the returned produce sequences of timestamps
     * from the given <code>{@link #insStart}</code> value.  The following timestamp values are then separated by the
     * time interval <code>{@link #durPeriod}</code>.
     * </p>
     * <p>
     * <h2>Default Parameters</h2>
     * This creator uses default parameters values from the JAL Tools default configuration.
     * The following values are used:  
     * <ul>
     * <li><code>{@link #insStart} = {@link #INS_INCR_START_DEF}</code>.</li>
     * </ul>
     * </p>
     *  
     * @param durPeriod the interval of time between generated timestamps (i.e., the sampling period)
     * 
     * @return  a new random <code>TimestampFactorySpec</code> configuration populated with the given arguments 
     */
    public static TimestampFactorySpec from(Duration durPeriod) {
        return TimestampFactorySpec.from(durPeriod, INS_INCR_START_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactorySpec</code> configuration for an incremental <code>TimestampFactory</code>.
     * </p>
     * <p>
     * All <code>{@link TimestampFactory}</code> instances created from the returned produce sequences of timestamps
     * from the given <code>{@link #insStart}</code> value.  The following timestamp values are then separated by the
     * time interval <code>{@link #durPeriod}</code>.
     * </p>
     *  
     * @param durPeriod the interval of time between generated timestamps (i.e., the sampling period)
     * @param insStart  the start time of the timestamp sequence (i.e., the 1st timestamp value)
     * 
     * @return  a new random <code>TimestampFactorySpec</code> configuration populated with the given arguments 
     */
    public static TimestampFactorySpec from(Duration durPeriod, Instant insStart) {
        return new TimestampFactorySpec(false, 0, durPeriod, insStart);
    }
    
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactorySpec</code> configuration for a random <code>TimestampFactory</code>.
     * </p>
     * <p>
     * When the boolean argument is <code>true</code> the returned configuration is for a random timestamp factory
     * with the given seed value.  If the seed value is '0' then the seed is generated 'randomly' and each new
     * factory starts with a different seed value.  Setting a nonzero seed value creates timestamp factories that
     * all produce the same 'random' sequence.
     * </p>
     * <p>
     * <h2>Default Parameters</h2>
     * This creator uses default parameters values from the JAL Tools default configuration.
     * The following values are used:  
     * <ul>
     * <li><code>{@link #lngSeed} = {@link #LNG_RND_SEED_DEF}</code>.</li>
     * </ul>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Random timestamp factories are more expensive than incremental timestamp factories, requiring a random number
     * generator for timestamp production.
     * </li>
     * <li>
     * Random timestamp factories produce timestamps using randomly generated <code>long</code> values.  The long
     * value is then used as a nanosecond offset past the given epoch <code>{@link Instant#EPOCH}</code>.
     * </li>
     * <li>
     * If a random timestamp factory configuration creator is used with the 'random' parameter set to <code>false</code>
     * the result timestamp factory produces a sequence of timestamps with contant value <code>{@link Instant#EPOCH}</code>.
     * </li>
     * </ul>
     * </p> 
     * 
     * @param bolRand   random generation timestamp generation enable/disable flag
     * 
     * @return  a new random <code>TimestampFactorySpec</code> configuration populated with the given arguments 
     */
    public static TimestampFactorySpec from(boolean bolRand) {
        return TimestampFactorySpec.from(bolRand, LNG_RND_SEED_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactorySpec</code> configuration for a random <code>TimestampFactory</code>.
     * </p>
     * <p>
     * When the boolean argument is <code>true</code> the returned configuration is for a random timestamp factory
     * with the given seed value.  If the seed value is '0' then the seed is generated 'randomly' and each new
     * factory starts with a different seed value.  Setting a nonzero seed value creates timestamp factories that
     * all produce the same 'random' sequence.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Random timestamp factories are more expensive than incremental timestamp factories, requiring a random number
     * generator for timestamp production.
     * </li>
     * <li>
     * Random timestamp factories produce timestamps using randomly generated <code>long</code> values.  The long
     * value is then used as a nanosecond offset past the given epoch <code>{@link Instant#EPOCH}</code>.
     * </li>
     * <li>
     * If a random timestamp factory configuration creator is used with the 'random' parameter set to <code>false</code>
     * the result timestamp factory produces a sequence of timestamps with contant value <code>{@link Instant#EPOCH}</code>.
     * </li>
     * </ul>
     * </p> 
     * 
     * @param bolRand   random generation timestamp generation enable/disable flag
     * @param lngSeed   seed value for the random number generator (use 0 for random seed)
     * 
     * @return  a new random <code>TimestampFactorySpec</code> configuration populated with the given arguments 
     */
    public static TimestampFactorySpec from(boolean bolRand, long lngSeed) {
        
        return new TimestampFactorySpec(bolRand, lngSeed, Duration.ZERO, Instant.EPOCH);
    }
    
    /**
     * <p>
     * Parses the argument collection for the field values of the returned <code>TimestampFactorySpec</code> instance.
     * </p>
     * <p>
     * The argument collection is assumed to originate from an application command-line argument collection.
     * There are 2 possibilities for a <code>{@link TimestampFactory}</code>: 1) a random timestamp factory and,
     * 2) and incremental timestamp factory.  In the first case there is 1 parameter, the random number 'seed'
     * value.  In the second case there are two parameters, the 'period' and the 'start' time instant.
     * </p>
     * <h2>Format</h2>
     * The format of the arguments is either of the following 2 possibilities:
     * <ol>
     * <pre>
     * <li>  > false [seed]</li>
     *    or
     * <li>  > true [period [start]]</li>
     * </pre>
     * </ol>
     * where
     * <ul>
     * <li>'seed' = seed value for the random number generation, where 0 indicates random seed (long value),</li>
     * <li>'period' = sampling period of an incremental timestamp factory (ISO-8605 duration format),</li>
     * <li>'start' = start time for an incremental timestamp factory (ISO-8605 date/time format).</li>
     * </ul>
     * </p>
     * <p>
     * <h2>Optional Arguments</h2>
     * The brackets indicate optional values in the argument collection.  If not present they are populated with
     * the default values of the JAL Tools default configuration.
     * <ul>
     * <li>'seed' = <code>{@link #LNG_RND_SEED_DEF}</code>.</li>
     * <li>'period' = <code>{@link #DUR_INCR_PERIOD_DEF}</code>.</li>
     * <li>'start' = <code>{@link #INS_INCR_START_DEF}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param args  argument collection to be parsed, format as described above
     * 
     * @return  a new <code>TimestampFactorySpec</code> record populated with the parsed argument values
     * 
     * @throws IllegalArgumentException the argument collection was empty (must have at least 1 element - bolRand)
     * @throws NumberFormatException    the 'seed' value could not be parsed
     * @throws DateTimeParseException   the 'period' or 'instant' value could not be parsed
     */
    public static TimestampFactorySpec parse(String...args) throws IllegalArgumentException, NumberFormatException, DateTimeParseException {
        
        if (args.length < 1)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Argument must contain at least one argument: " 
                    + Arrays.asList(args) );

        // Get the random generation enable/disable flag
        boolean bolRand = Boolean.valueOf(args[0]);
        
        // Populate record according to random enable/disable flag
        if (bolRand) {  
            // Random timestamp factory
            if (args.length < 2)
                return TimestampFactorySpec.from(bolRand);
            
            long    lngSeed = Long.valueOf(args[1]);    // throws NumberFormatException
            return TimestampFactorySpec.from(bolRand, lngSeed);
            
            
        } else {        
            // Incremental timestamp factory
            if (args.length < 2)
                return TimestampFactorySpec.from();

            Duration    durPeriod = Duration.parse(args[1]);    // throws DateTimeParseException
            if (args.length < 3) 
                return TimestampFactorySpec.from(durPeriod);
            
            Instant     insStart = Instant.parse(args[2]);      // throws DateTimeParseException
            return TimestampFactorySpec.from(durPeriod, insStart);
        }
    }
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Create and return a new <code>TimestampFactory</code> instance according to this configuration.
     * </p>
     * <p>
     * Only two field values are used for <code>TimestampFactory</code> instance creation, this depends upon the
     * value of <code>{@link #bolRand()}</code>.
     * <ul>
     * <li><code>{@link #bolRand()} = true</code> &rarr; <code>{@link TimestampFactory#from(boolean, long)}</code></li>.
     * <li><code>{@link #bolRand()} = true</code> &rarr; <code>{@link TimestampFactory#from(Duration, Instant)}</code></li>.
     * </ul>
     * </p>
     * 
     * @return  a new <code>TimestampFactory</code> instance ready for simulated timestamp value creation
     */
    public TimestampFactory newFactory() {
        
        // Create and return factory 
        if (this.bolRand)
            return TimestampFactory.from(this.bolRand, this.lngSeed);
        else
            return TimestampFactory.from(this.durPeriod, this.insStart);
    }
    
    
    // 
    // Record Overrides
    //
    
    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof TimestampFactorySpec spec)  {
            boolean bolResult = (this.bolRand == spec.bolRand)
                    && (this.lngSeed == spec.lngSeed)
                    && (this.durPeriod.equals(spec.durPeriod))
                    && (this.insStart.equals(spec.insStart));
            return bolResult;
        }
        
        return false;
    }

    /**
     * @see java.lang.Record#toString()
     */
    @Override
    public String toString() {
        String  str = "";
        str += "Random generation enabled : " + this.bolRand + "\n";
        str += "Random generator seed     : " + this.lngSeed + "\n";
        str += "Sampling period           : " + this.durPeriod + "\n";
        str += "Sampling start instant    : " + this.insStart + "\n";
        
        return str;
    }
    
    //
    // JAL Library Resources
    //
    
    /** JAL Tools default configuration parameters for datum factories */
    private static final JalToolsDataGenConfig.Values   CFG_DEF = JalToolsConfig.getInstance().datagen.values;
    
    
    // 
    // Record Constants - Default Values
    //
    
    /** Timestamp factory random value generation default value (i.e., generate noise) */
    public static final boolean     BOL_RND_ENBL_DEF = CFG_DEF.timestamp.random.enabled;
    
    /** Timestamp factory random number generator seed value default */
    public static final long        LNG_RND_SEED_DEF = CFG_DEF.timestamp.random.seed;
    
    /** Timestamp factory default period for incremental timestamp generation */
    public static final Duration    DUR_INCR_PERIOD_DEF = CFG_DEF.timestamp.increment.periodDuration();
    
    /** Timestamp factory default starting instant for incremental timestamp generation */
    public static final Instant     INS_INCR_START_DEF = CFG_DEF.timestamp.increment.startInstant();
    
}