/*
 * Project: dp-jal
 * File:	TimestampFactory.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.values
 * Type: 	TimestampFactory
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
 * @since Nov 23, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.values;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.Random;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalComplexType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.values.JalToolsTmsFactoryConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Class for generating simulated sequences of timestamp data.
 * </p> 
 * <p>
 * Class objects <code>TimestampFactory</code> can be configured for either <em>random</em> timestamp generation or
 * <em>incremental</em> timestamp generation.  After creation/construction the timestamp factory object configuration
 * cannot be changed.  There are separate constructors for each configuration, and several creators that support
 * these constructors with various default values taken from the JAL Tools default configuration.
 * </p>
 * <p>
 * The returned values of are Java type <code>{@link Instant}</code>.  These values are readily converted to
 * the Data Platform <code>Timestamp</code> message, which can be done with the utility <code>ProtoMsg</code>.
 * </p>
 * <p>
 * <h2>Random Generation</h2>
 * If the timestamp factory is configured for random timestamp generation then the returned values are 
 * randomly generated offsets from the current epoch <code>{@link Instant#EPOCH}</code>.  Specifically,
 * <code>long</code> values are randomly generated then added to the current epoch with units
 * given by <code>{@link #CU_RND_EPOCH_ADD}</code>.  The random sequence represents noise values.
 * </p>
 * <p>
 * <h2>Incremental Generation</h2>
 * If the timestamp factory is configured for incremental timestamp generation then the returned values
 * are an incrementing sequence of timestamps with differences given by the 'period' provided at
 * creation/construction.  The first timestamp is determined by the 'start' instance provided at
 * creation/construction.
 * </p> 
 *
 *  
 * @author Christopher K. Allen
 * @since Nov 23, 2025
 *
 */
public class TimestampFactory implements IDatumFactory {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactory</code> instance with default configuration.
     * </p>
     * <p>
     * The configuration of the returned <code>TimestampFactory</code> instance is determined completely
     * by the JAL Tools default settings.  Specifically, the returned instance is given by the following
     * criteria:
     * <ul>
     * <li><If code>{@link #BOL_RND_ENBL_DEF}</code> = <code>true</code> then <code>{@link #from(boolean, long)}</code> with arguments
     *   <ul>
     *   <li><code>{@link #BOL_RND_ENBL_DEF}</code></li>
     *   <li><code>{@link #LNG_RND_SEED_DEF}</code></li>
     *   </ul>
     * </li>
     * <li><If code>{@link #BOL_RND_ENBL_DEF}</code> = <code>false</code> then <code>{@link #from(Duration, Instant)}</code> with arguments
     *    <ul>
     *    <li><code>{@link #DUR_INCR_PERIOD_DEF}</code></li>
     *    <li><code>{@link #INS_INCR_START_DEF}</code></li>
     *    </ul>
     * <li>
     * </ul>
     * See the documentation for <code>{@link #from(boolean, long)}</code> and <code>{@link #from(boolean, long)}</code
     * for further information.
     * </p>
     * 
     * @return  a new <code>TimestampFactory</code> instance ready for timestamp value creation
     */
    public static TimestampFactory  from() {
        
        if (BOL_RND_ENBL_DEF)
            return TimestampFactory.from(BOL_RND_ENBL_DEF, LNG_RND_SEED_DEF);
        else
            return TimestampFactory.from(DUR_INCR_PERIOD_DEF, INS_INCR_START_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactory</code> instance for randomly generated timestamps.
     * </p>
     * <p>
     * The returned <code>TimestampFactory</code> instance is configured according to the given argument
     * value.  The argument seed define the randomness of the timestamp sequence generated by the returned
     * factory.
     * </p>
     * <p>
     * <h2>Random Values</h2>
     * This creator returns <code>TimestampFactory</code> instances configured for random timestamp generation.  
     * Specifically, the timestamp factory generates randomly valued timestamps beyond the instant 
     * <code>{@link Instant#EPOCH}</code>.
     * Thus, the timestamps generated represent noisy values from the current epoch.
     * Long values are randomly generated and used to create offsets from the current epoch value
     * (i.e., <code>from {@link Instant#EPOCH}</code>) with the units <code>{@link #CU_RND_EPOCH_ADD}</code>.
     * </p>
     * <p>
     * <h2>Seed Value</h2>
     * The argument seed value is used to seed the internal random number generator.  Use of value of 0
     * to generate a random seed value.  In that case the random number generator will appear to create
     * random numbers in a seemingly random sequence.  Providing a nonzero value will create a sequence
     * with the same sequence of "random" values.
     * </p> 
     *
     * @param bolRandom random generation timestamp generation enable/disable flag
     * @param lngSeed   seed value for the random number generator (use 0 for random seed)
     * 
     * @return  a new <code>TimestampFactory</code> instance ready for timestamp value creation
     */
    public static TimestampFactory  from(long lngSeed) {
        return TimestampFactory.from(true, lngSeed);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactory</code> instance for randomly generated timestamps.
     * </p>
     * <p>
     * The returned <code>TimestampFactory</code> instance is configured according to the given argument
     * value.  The argument define the randomness of the timestamp sequence generated by the returned
     * factory (i.e., whether random or not - see below).
     * </p>
     * <p>
     * <h2>Random Values</h2>  
     * Set the <code>boolean</code> argument to <code>true</code> to create a timestamp factory
     * that generates randomly valued timestamps beyond the instant <code>{@link Instant#EPOCH}</code>.
     * Specifically, the timestamps generated represent noisy values from the current epoch.
     * Long values are randomly generated and used to create offsets from the current epoch value
     * (i.e., <code>from {@link Instant#EPOCH}</code>) with the units <code>{@link #CU_RND_EPOCH_ADD}</code>.
     * </p>
     * <p>
     * <h2>Seed Value</h2>
     * The seed value is taken from the JAL Tools default configuration <code>{@link #LNG_RND_SEED_DEF}</code>.
     * The seed value is used to seed the internal random number generator.  Use of value of 0
     * to generate a random seed value.  In that case the random number generator will appear to create
     * random numbers in a seemingly random sequence.  Providing a nonzero value will create a sequence
     * with the same sequence of "random" values.
     * </p> 
     * <p>
     * <h2>NOTE:</h2>
     * If the boolean argument is <code>false</code> the seed argument is ignored and the returned 
     * <code>TimestampFactory</code> instance will generated all timestamps with value 
     * <code>{@link Instant#EPOCH}</code>.
     * </p>
     *
     * @param bolRandom random generation timestamp generation enable/disable flag
     * 
     * @return  a new <code>TimestampFactory</code> instance ready for timestamp value creation
     */
    public static TimestampFactory  from(boolean bolRandom) {
        return TimestampFactory.from(bolRandom, LNG_RND_SEED_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactory</code> instance for randomly generated timestamps.
     * </p>
     * <p>
     * The returned <code>TimestampFactory</code> instance is configured according to the given argument
     * values.  The arguments define the randomness of the timestamp sequence generated by the returned
     * factory.
     * </p>
     * <p>
     * <h2>Random Values</h2>  
     * Set the <code>boolean</code> argument to <code>true</code> to create a timestamp factory
     * that generates randomly valued timestamps beyond the instant <code>{@link Instant#EPOCH}</code>.
     * Specifically, the timestamps generated represent noisy values from the current epoch.
     * Long values are randomly generated and used to create offsets from the current epoch value
     * (i.e., <code>from {@link Instant#EPOCH}</code>) with the units <code>{@link #CU_RND_EPOCH_ADD}</code>.
     * </p>
     * <p>
     * <h2>Seed Value</h2>
     * The argument seed value is used to seed the internal random number generator.  Use of value of 0
     * to generate a random seed value.  In that case the random number generator will appear to create
     * random numbers in a seemingly random sequence.  Providing a nonzero value will create a sequence
     * with the same sequence of "random" values.
     * </p> 
     * <p>
     * <h2>NOTE:</h2>
     * If the boolean argument is <code>false</code> the seed argument is ignored and the returned 
     * <code>TimestampFactory</code> instance will generated all timestamps with value 
     * <code>{@link Instant#EPOCH}</code>.
     * </p>
     *
     * @param bolRandom random generation timestamp generation enable/disable flag
     * @param lngSeed   seed value for the random number generator (use 0 for random seed)
     * 
     * @return  a new <code>TimestampFactory</code> instance ready for timestamp value creation
     */
    public static TimestampFactory  from(boolean bolRandom, long lngSeed) {
        return new TimestampFactory(bolRandom, lngSeed);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactory</code> instance for incremental timestamp value generation.
     * </p>
     * <p>
     * The returned <code>TimestampFactory</code> instance is configured according to the given argument
     * value.  The arguments define the starting instance of the timestamp sequence generated by the returned
     * factory and the time difference between them (i.e., the 'period') is given by the JAL Tools default
     * configuration <code>{@link #DUR_INCR_PERIOD_DEF}</code>.
     * </p>
     * <p>
     * <h2>Period</h2>
     * The 'period' is the distance, or difference, between timestamps in the generated sequence.  It is analogous
     * to the sampling period in a uniformly sampled process.
     * </p>
     * <p>
     * <h2>Start Instant</h2>
     * The start instance is the first timestamp value in the generated sequence.  All further timestamps in the
     * sequence will be separated by a distance given by the period.
     * </p>
     * 
     * @param durPeriod the difference (period) between generated timestamp values in the incremental sequence 
     * @param insStart  the first timestamp value in the incremental sequence
     * 
     * @return  a new <code>TimestampFactory</code> instance ready for timestamp value generation
     */
    public static TimestampFactory  from(Instant insStart) {
        return TimestampFactory.from(DUR_INCR_PERIOD_DEF, insStart);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactory</code> instance for incremental timestamp value generation.
     * </p>
     * <p>
     * The returned <code>TimestampFactory</code> instance is configured according to the given argument
     * value.  The argument defines distance between timestamps within the generated sequence. The start time
     * instant is given by the JAL Tools default configuration <code>{@link #INS_INCR_START_DEF}</code>.
     * </p>
     * <p>
     * <h2>Period</h2>
     * The 'period' is the distance, or difference, between timestamps in the generated sequence.  It is analogous
     * to the sampling period in a uniformly sampled process.
     * </p>
     * <p>
     * <h2>Start Instant</h2>
     * The start instance is the first timestamp value in the generated sequence.  All further timestamps in the
     * sequence will be separated by a distance given by the period.
     * </p>
     * 
     * @param durPeriod the difference (period) between generated timestamp values in the incremental sequence 
     * 
     * @return  a new <code>TimestampFactory</code> instance ready for timestamp value generation
     */
    public static TimestampFactory  from(Duration durPeriod) {
        return TimestampFactory.from(durPeriod, INS_INCR_START_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TimestampFactory</code> instance for incremental timestamp value generation.
     * </p>
     * <p>
     * The returned <code>TimestampFactory</code> instance is configured according to the given argument
     * values.  The arguments define the starting instance of the timestamp sequence generated by the returned
     * factory and the time difference between them (i.e., the 'period').
     * </p>
     * <p>
     * <h2>Period</h2>
     * The 'period' is the distance, or difference, between timestamps in the generated sequence.  It is analogous
     * to the sampling period in a uniformly sampled process.
     * </p>
     * <p>
     * <h2>Start Instant</h2>
     * The start instance is the first timestamp value in the generated sequence.  All further timestamps in the
     * sequence will be separated by a distance given by the period.
     * </p>
     * 
     * @param durPeriod the difference (period) between generated timestamp values in the incremental sequence 
     * @param insStart  the first timestamp value in the incremental sequence
     * 
     * @return  a new <code>TimestampFactory</code> instance ready for timestamp value generation
     */
    public static TimestampFactory  from(Duration durPeriod, Instant insStart) {
        return new TimestampFactory(durPeriod, insStart);
    }
    
    /**
     * <p>
     * Parses the argument collection for the field values of the returned <code>TimestampFactory</code> instance.
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
     * @return  a new <code>TimestampFactory</code> instance configured with the parsed argument values
     * 
     * @throws MissingResourceException the argument collection was empty (must have at least 1 element - bolRand)
     * @throws NumberFormatException    the 'seed' value could not be parsed
     * @throws DateTimeParseException   the 'period' or 'instant' value could not be parsed
     */
    public static TimestampFactory  parse(String...args) throws MissingResourceException, NumberFormatException, DateTimeParseException {
        
        if (args.length < 1)
            throw new MissingResourceException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Argument must contain at least one argument: " 
                    + Arrays.asList(args), 
                    TimestampFactory.class.getName(),  "parse()");

        // Get the random generation enable/disable flag
        boolean bolRand = Boolean.valueOf(args[0]);
        
        // Populate record according to random enable/disable flag
        if (bolRand) {  
            // Random timestamp factory
            if (args.length < 2)
                return TimestampFactory.from(bolRand);
            
            long    lngSeed = Long.valueOf(args[1]);    // throws NumberFormatException
            return TimestampFactory.from(bolRand, lngSeed);
            
            
        } else {        
            // Incremental timestamp factory
            if (args.length < 2)
                return TimestampFactory.from();

            Duration    durPeriod = Duration.parse(args[1]);    // throws DateTimeParseException
            if (args.length < 3) 
                return TimestampFactory.from(durPeriod);
            
            Instant     insStart = Instant.parse(args[2]);      // throws DateTimeParseException
            return TimestampFactory.from(durPeriod, insStart);
        }
    }
    
    
    //
    // Library Resources
    //
    
    /** The default configuration parameters for simulated timestamp value generation */
    private static final JalToolsTmsFactoryConfig    CFG_DEF = JalToolsConfig.getInstance().datagen.values.timestamp;
    
    
    //
    // Class Constant
    //
    
    /** The datum type of all data produced by this data factory */
    public static final DpSupportedType     ENM_DATUM_TYPE = DpSupportedType.TIMESTAMP;
    
    /** The scalar type of all data produced by this data factory */
    public static final JalScalarType       ENM_SCALAR_TYPE = JalScalarType.UNSUPPORTED;
    
    /** The complex type of all data produced by this data factory */
    public static final JalComplexType       ENM_CMPLX_TYPE = JalComplexType.TIMESTAMP;
    
    
    /** The units for randomly generated long values added to <code>{@link Instant#EPOCH}</code> for random timestamp values*/
    public static final ChronoUnit  CU_RND_EPOCH_ADD = ChronoUnit.NANOS;

    
    /** Default random enable/disable timestamp generation flag */
    public static final boolean     BOL_RND_ENBL_DEF = CFG_DEF.random.enabled;
    
    /** Default random generator seed value - use '0' for a random seed value */
    public static final long        LNG_RND_SEED_DEF = CFG_DEF.random.seed;
    
    
    /** The default start time instant for incremental timestamp generation */
    public static final Instant     INS_INCR_START_DEF = CFG_DEF.increment.startInstant();
    
    /** The default timestamp period for incremental timestamp generation */
    public static final Duration    DUR_INCR_PERIOD_DEF = CFG_DEF.increment.periodDuration();

    
    //
    // Defining Attributes - Configuration
    //
    
    /** The random timestamp generation enable/disable flag */
    private final boolean   bolRandom;
    
    /** The seed value for the random number generator */
    private final long      lngRndSeed;

    
    /** The first timestamp instant for incremental timestamp generation */
    private final Instant   insStart;
    
    /** The timestamp differences (periods) for incremental timestamp generation */
    private final Duration  durPeriod;
    
    
    //
    // Resources
    //
    
    /** The random number generator used for random-valued timestamp generation, or <code>null</code> if incremental */
    private final Random    genRandom;
    
    
    //
    // State Variables
    //
    
    /** The next timestamp value to be returned */ 
    private Instant     insNext;
    
    
    //
    // Constructors
    // 
    
    /**
     * <p>
     * Constructs a new <code>TimestampFactory</code> instance for randomly generated timestamps.
     * </p>
     * <p>
     * Note that if the boolean argument is <code>false</code> the constructed <code>TimestampFactory</code>
     * instance will generated all timestamps with value <code>{@link Instant#EPOCH}</code>.
     * </p>
     *
     * @param bolRandom random generation timestamp generation enable/disable flag
     * @param lngSeed   seed value for the random number generator (use 0 for random seed)
     */
    public TimestampFactory(boolean bolRandom, long lngSeed) {
        this.bolRandom = bolRandom;
        this.lngRndSeed = lngSeed;
        
        this.insStart = Instant.EPOCH;
        this.durPeriod = Duration.ZERO;
        
        // Create the random number generator
        this.genRandom = this.initRandomGenerator(this.bolRandom, this.lngRndSeed);
        
        // Initialize the current timestamp
        this.insNext = this.initNextTimestamp(this.bolRandom, this.insStart);
    }
    
    /**
     * <p>
     * Constructs a new <code>TimestampFactory</code> instance for incremental timestamp generation.
     * </p>
     *
     * @param durPeriod the difference (period) between generated timestamp values in the incremental sequence 
     * @param insStart  the first timestamp value in the incremental sequence
     */
    public TimestampFactory(Duration durPeriod, Instant insStart) {
        this.durPeriod = durPeriod;
        this.insStart = insStart;
        
        this.bolRandom = false;
        this.lngRndSeed = 0;
        
        // Create the random number generator
        this.genRandom = this.initRandomGenerator(this.bolRandom, this.lngRndSeed);
        
        // Initialize the current timestamp
        this.insNext = this.initNextTimestamp(this.bolRandom, this.insStart);
    }
    
    
    //
    // Configuration Inquiry
    //
    
    /**
     * <p>
     * Determines whether or not the generated sequence of timesetamps is random.
     * </p>
     * <p>
     * The returned value indicates whether or not the current timestamp factory is a random timestamp factory, that is,
     * the timestamp factory generates random timestamps.  Otherwise, the current timestamp factory is an incremental
     * timestamp factory producing incremental timestamp sequences.
     * </p>
     * 
     * @return  <code>true</code> if the generated timestamps are random, <code>false</code> if they are incremental
     */
    public boolean  isRandom() {
        return this.bolRandom;
    }
    
    /**
     * <p>
     * Returns the seed value for the internal random number generator of random timetamps factories.
     * </p>
     * <p>
     * If the timestamp factory is configured for random timestamp generation then the returned value represents
     * the seed value used to initialize the random number generator.  In that case, if the returned value is 0 
     * then the random number generator was initialized with a "random" value.  There, the random sequence starts
     * at an apparent random value.  If the seed value is non-zero then the value is used to seed the random
     * number generator with that value.  There, the "random" sequence is reproducible; this situation may be warranted
     * in some cases.
     * </p>
     * <p>
     * If the timestamp is not configured for random timestamp generation the returned value is 0, although
     * here it is meaningless.
     * </p>  
     * 
     * @return  the seed value for the random number generator if used, when used a 0 value indicates a random seed value
     * 
     * @see #isRandom()
     */
    public long getRandomSeedValue() {
        return this.lngRndSeed;
    }
    
    /**
     * <p>
     * Returns the start timestamp instant for incremental timestamp factories.
     * </p>
     * <p>
     * The returned value is the first timestamp producted by <code>{@link #nextDatum()}</code> when the timestamp factory 
     * is configured for incremental timestamp value generation.  When the timestamp factory is configured for random
     * timestamp value generation the returned value is <code>{@link Instant#EPOCH}</code>, however, the value is
     * never used.
     * </p>
     * 
     * @return  the first timestamp instance used for incremental timestamp generation
     * 
     * @see #isRandom()
     */
    public Instant  getStartInstant() {
        return this.insStart;
    }
    
    /**
     * <p>
     * Returns the time difference between timestamps for incremental timestamp factories.
     * </p>
     * <p>
     * For an incremental timestamp factory the returned value represents the time difference between the
     * generated sequence of timestamp instants.  This value is analogous to the sampling 'period' of a uniform
     * sampling process.
     * </p>
     * <p>
     * For random timestamp factories the returned value is <code>{@link Duration#ZERO}</code> and is not used.
     * </p>
     * 
     * @return  the distance between timestamps for an incremental timestamp factory
     * 
     * @see #isRandom()
     */
    public Duration getPeriod() {
        return this.durPeriod;
    }
    
    
    //
    // IDatumFactory Interface
    //

    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory#getDatumType()
     */
    @Override
    public DpSupportedType getDatumType() {
        return ENM_DATUM_TYPE;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory#getScalarType()
     */
    @Override
    public JalScalarType getScalarType() {
        return ENM_SCALAR_TYPE;
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory#getComplexType()
     */
    @Override
    public JalComplexType getComplexType() {
        return ENM_CMPLX_TYPE;
    }
        
    /**
     * <p>
     * Returns the next timestamp in the sequence of generated timestamp values.
     * </p>
     * <p>
     * The returned values of are Java type <code>{@link Instant}</code>.  These values are readily converted to
     * the Data Platform <code>Timestamp</code> message, which can be done with the utility <code>ProtoMsg</code>.
     * </p>
     * <p>
     * <h2>Random Values</h2>
     * If the timestamp factory is configured for random timestamp generation then the returned values are 
     * randomly generated offsets from the current epoch <code>{@link Instant#EPOCH}</code>.  Specifically,
     * <code>long</code> values are randomly generated then added to the current epoch with units
     * given by <code>{@link #CU_RND_EPOCH_ADD}</code>.  The random sequence represents noise values.
     * </p>
     * <p>
     * <h2>Incremental Values</h2>
     * If the timestamp factory is configured for incremental timestamp generation then the returned values
     * are an incrementing sequence of timestamps with differences given by the 'period' provided at
     * creation/construction.  The first timestamp is determined by the 'start' instance provided at
     * creation/construction.
     * </p> 
     * 
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory#nextDatum()
     */
    @Override
    public Object nextDatum() {
        
        // Locally store the timestamp value to be returned
        Instant     insVal = this.insNext;
        
        // Compute the next timestamp value
        if (this.bolRandom) {
            long    lngNext = this.genRandom.nextLong();
            
            this.insNext = Instant.EPOCH.plus(lngNext, CU_RND_EPOCH_ADD);
            
        } else {
            this.insNext = this.insNext.plus(this.durPeriod);
        }
        
        // Return the current timestamp value
        return insVal;
    }

    
    //
    // Object Overrides
    //
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimestampFactory fac) {
            boolean bolResult = (this.bolRandom == fac.bolRandom)
                             && (this.lngRndSeed == fac.lngRndSeed)
                             && this.insStart.equals(fac.insStart)
                             && this.durPeriod.equals(fac.durPeriod);
            
            return bolResult;
        }
        
        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder   buf = new StringBuilder();
        
        buf.append("IDatumFactory Implementation : " + this.getClass().getName() + "\n");
        buf.append("Random timestamps enabled   : " + this.bolRandom + "\n");
        buf.append("Random seed value           : " + this.lngRndSeed + "\n");
        buf.append("Timestamp generation period : " + this.durPeriod + "\n");
        buf.append("Start time (1st timestamp)  : " + this.insStart + "\n");
        
        return buf.toString();
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates and returns the random number generator configured to the given arguments.
     * </p>
     * <p>
     * The returned <code>{@link Random}</code> object is determined according to the following
     * conditions:
     * <ul>
     * <li><code>bolRandom = false</code>: returns <code>null</code>,</li>
     * <li><code>bolRandom = true</code>: 
     *   <br/>returns the following:</li>
     *   <ul>
     *   <li><code>lngSeed = 0</code>: returns new <code>Random()</code> with random seed value,</li>
     *   <li><code>lngSeed &ne; 0</code>: returns <code>Random(lngSeed)</code> with given (non-zero) seed value.</li>
     *   </ul>
     * </ul>
     * </p>
     * 
     * @param bolRandom random timestamp generation enable/disable flag
     * @param lngSeed   the random number generator seed value (0 indicates random seed)
     * 
     * @return  a new <code>Random</code> object configured according to the given arguments
     */
    private Random  initRandomGenerator(boolean bolRandom, long lngSeed) {
        
        if (!bolRandom)
            return null;
        
        if (lngSeed == 0)
            return new Random();
        else
            return new Random(lngSeed);
    }
    
    /**
     * <p>
     * Initializes the next timestamp value depending upon the random generation enable/disable flag.
     * </p>
     * <p>
     * The returned value is simply the <code>Instant</code> argument value if the <code>boolean</code>
     * argument is <code>false</code>.  If the <code>boolean</code> argument is <code>true</code> then
     * the return value is randomly generated using a randomly generated <code>long</code> value added
     * to the <code>{@link Instant#EPOCH}</code> value with units <code>{@link #CU_RND_EPOCH_ADD}</code>
     * = {@value #CU_RND_EPOCH_ADD}. 
     * </p>
     * <p>
     * <h2>WARNING:<h2>
     * This method must be called after initializing the random number generator, that is, after invoking
     * <code>{@link #initRandomGenerator(boolean, long)}</code>.  The internal random number generator is
     * used whenever the boolean argument is <code>true</code>.
     * </p>
     * 
     * @param bolRandom
     * @param insStart
     * @return
     */
    private Instant initNextTimestamp(boolean bolRandom, Instant insStart) {
        
        // If incremental timetamp generation
        if (!bolRandom)
            return insStart;
        
        // Random timestamp generation start value
        long    lngVal = this.genRandom.nextLong();
        Instant insVal = Instant.EPOCH.plus(lngVal, CU_RND_EPOCH_ADD);
        
        return insVal;
    }
    
}
