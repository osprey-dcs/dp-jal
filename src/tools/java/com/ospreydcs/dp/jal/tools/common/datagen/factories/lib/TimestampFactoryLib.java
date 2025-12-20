/*
 * Project: dp-jal
 * File:	TimestampFactoryLib.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.lib
 * Type: 	TimestampFactoryLib
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
 * @since Dec 17, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.lib;

import java.time.Duration;
import java.time.Instant;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.TimestampFactorySpec;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TimestampFactory;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.values.JalToolsTmsFactoryConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * An enumeration (library) of pre-defined timestamp factories available for testing and evaluations.
 * </p>
 * <p>
 * The collection of pre-defined timestamp factories provides a variety of common sampling periods, as well
 * as a random factory and a default factory.
 * </p>
 * <p>
 * <h2>Timestamp Factories</h2>
 * The Timestamp factories available here are of type <code>{@link TimestampFactory}</code>.  The 
 * <code>TimestampFactory</code> class instances can be configured to produce a variety of timestamps sequences
 * and this library collection provides quick access to some common configurations. 
 * For more information on tensor factories see the class documentation <code>{@link TimestampFactory}</code>.
 * </p>
 * <p>
 * <h2>Factory Configuration</h2>
 * Each enumeration constant represents a particular configuration, or "specification" of timestamp factory, 
 * identifying the type of timestamp sequences the factory produces.
 * There are 2 type of timestamp factories:
 * <ol>
 * <li>Incremental factories (clocks) - produces an incremental sequence of timestamps as if from a sample clock.</li>
 * <li>Random factories - produces random timestamp values from the current epoch simulating noise.</li>
 * </ol> 
 * Timestamp factory configuration are represented internally as a <code>{@link TimestampFactorySpec}</code>
 * record containing the required configuration specification.
 * Note that specification parameters required for <code>TimestampFactory</code> configuration include the 
 * timestamp sequence start time and the sampling period for incremental factories, and random number seed
 * value for random factories.
 * </p>
 * <p>
 * <h2>Factory Creation</h2>
 * Timestamp factories for an enumeration constant are created with following methods
 * <ul> 
 * <li><code>{@link #newFactory()}</code> - timestmap factory configured to constant parameters.</li>
 * <li><code>{@link #newFactory(Instant)}</code> - incremental timestamp factory configured to constant parameters and given start instant.</li>
 * </ul>
 * Note that timestamp factories require no other resources for operation (e.g., a scalar factory).
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 17, 2025
 *
 */
public enum TimestampFactoryLib {
    
    /**
     * The default timestamp factory as defined in the JAL Tools default configuration.
     * 
     * @see JalToolsTmsFactoryConfig
     */
    DEFAULT(TimestampFactorySpec.from()),
    
    /**
     * A random timestamp factory using a 'randomly' generated seed for the random number generator. 
     */
    RANDOM_SEED_RAND(TimestampFactorySpec.from(true, 0)),
    
    /**
     * A random timestamp factory using a seed value of 1.  
     * <p>
     * Random timestamp factories with given seed values yield the same sequence of 'random' timestamps.
     * They are useful in evaluations and unit testing.
     */
    RANDOM_SEED_1(TimestampFactorySpec.from(true, 1)),
    
    /**
     * A random timestamp factory using a seed value of 2.  
     * <p>
     * Random timestamp factories with given seed values yield the same sequence of 'random' timestamps.
     * They are useful in evaluations and unit testing.
     */
    RANDOM_SEED_2(TimestampFactorySpec.from(true, 2)),
    
    /**
     * An incremental timestamp factory (i.e., a "clock") with the following parameters:
     * <ul>
     * <li>period = 1 nanosecond.</li>
     * <li>start instant = Data Platform Test Archive start instant.</li>
     * </ul>
     */
    CLOCK_1_NANOSEC( TimestampFactorySpec.from(Duration.ofNanos(1), JalToolsConfig.getInstance().testArchive.range.startInstant()) ),
    
    /**
     * An incremental timestamp factory (i.e., a "clock") with the following parameters:
     * <ul>
     * <li>period = 1 microsecond.</li>
     * <li>start instant = Data Platform Test Archive start instant.</li>
     * </ul>
     */
    CLOCK_1_MICROSEC( TimestampFactorySpec.from(Duration.ofNanos(1_000), JalToolsConfig.getInstance().testArchive.range.startInstant()) ),
    
    /**
     * An incremental timestamp factory (i.e., a "clock") with the following parameters:
     * <ul>
     * <li>period = 10 microsecond.</li>
     * <li>start instant = Data Platform Test Archive start instant.</li>
     * </ul>
     */
    CLOCK_10_MICROSEC( TimestampFactorySpec.from(Duration.ofNanos(10_000), JalToolsConfig.getInstance().testArchive.range.startInstant()) ),
    
    /**
     * An incremental timestamp factory (i.e., a "clock") with the following parameters:
     * <ul>
     * <li>period = 100 microsecond.</li>
     * <li>start instant = Data Platform Test Archive start instant.</li>
     * </ul>
     */
    CLOCK_100_MICOSEC( TimestampFactorySpec.from(Duration.ofNanos(100_000), JalToolsConfig.getInstance().testArchive.range.startInstant()) ),
    
    /**
     * An incremental timestamp factory (i.e., a "clock") with the following parameters:
     * <ul>
     * <li>period = 1 millisecond.</li>
     * <li>start instant = Data Platform Test Archive start instant.</li>
     * </ul>
     */
    CLOCK_1_MILLISEC( TimestampFactorySpec.from(Duration.ofMillis(1), JalToolsConfig.getInstance().testArchive.range.startInstant()) ),
    
    /**
     * An incremental timestamp factory (i.e., a "clock") with the following parameters:
     * <ul>
     * <li>period = 10 millisecond.</li>
     * <li>start instant = Data Platform Test Archive start instant.</li>
     * </ul>
     */
    CLOCK_10_MILLISEC( TimestampFactorySpec.from(Duration.ofMillis(10), JalToolsConfig.getInstance().testArchive.range.startInstant()) ),
    
    /**
     * An incremental timestamp factory (i.e., a "clock") with the following parameters:
     * <ul>
     * <li>period = 100 millisecond.</li>
     * <li>start instant = Data Platform Test Archive start instant.</li>
     * </ul>
     */
    CLOCK_100_MILLISEC( TimestampFactorySpec.from(Duration.ofMillis(100), JalToolsConfig.getInstance().testArchive.range.startInstant()) ),
    
    /**
     * An incremental timestamp factory (i.e., a "clock") with the following parameters:
     * <ul>
     * <li>period = 1 second.</li>
     * <li>start instant = Data Platform Test Archive start instant.</li>
     * </ul>
     */
    CLOCK_1_SECOND( TimestampFactorySpec.from(Duration.ofSeconds(1), JalToolsConfig.getInstance().testArchive.range.startInstant()) ),
    
    /**
     * An incremental timestamp factory (i.e., a "clock") with the following parameters:
     * <ul>
     * <li>period = 10 seconds.</li>
     * <li>start instant = Data Platform Test Archive start instant.</li>
     * </ul>
     */
    CLOCK_10_SECONDS( TimestampFactorySpec.from(Duration.ofSeconds(10), JalToolsConfig.getInstance().testArchive.range.startInstant()) )
    
    ;
    
    
    //
    // Enumeration Collection Constants
    //
    
    /** The Data Platform supported type consistent with the timestamp factory */
    public static final DpSupportedType ENM_DP_TYPE = DpSupportedType.TIMESTAMP;
    
    
    //
    // Enumeration Constant Resources
    //
    
    /** The specification for the timestamp factories created by this constant */
    private final TimestampFactorySpec  recTmsFacSpec;
    
    
    //
    // Constant Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>TimestampFactoryLib</code> constant with the given factory specification.
     * </p>
     *
     * @param recTmsFacSpec timestamp factory specification for this constant
     */
    private TimestampFactoryLib(TimestampFactorySpec recTmsFacSpec) {
        this.recTmsFacSpec = recTmsFacSpec;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Returns the Data Platform type of the datum values for all timestamps generated by the factories.
     * </p>
     * <p>
     * This is a convenience method which returns the value of <code>{@link #ENM_DP_TYPE}</code> = {@value #ENM_DP_TYPE}. 
     * </p>
     * 
     * @return  the data type of all factory elements produced by all associated factories as a <code>DpSupportedType</code>
     */
    public DpSupportedType  getDpScalarType() {
        return TimestampFactoryLib.ENM_DP_TYPE;
    }
    

    /**
     * <p>
     * Creates new <code>TimestampFactory</code> instances configured according to this constant.
     * </p>
     * <p>
     * The returned <code>{@link TimestampFactory}</code> produces timestamp sequences according to this constant
     * parameter configuration.
     * That is, the timestamp sequence and value generation strategy is determined by the this enumeration constant.  
     * </p>
     * 
     * @return  new <code>TimestampFactory</code> ready for simulated-valued tensor creation
     */
    public TimestampFactory newFactory() {
        return this.recTmsFacSpec.newFactory();
    }
    /**
     * <p>
     * Creates new <code>TimestampFactory</code> instances configured according to this constant with given start instant.
     * </p>
     * <p>
     * The returned <code>{@link TimestampFactory}</code> produces timestamp sequences according to this constant
     * parameter configuration.
     * That is, the timestamp sequence and value generation strategy is determined by the this enumeration constant.  
     * </p>
     * <p>
     * <h2>Start Instant</h2>
     * This is a special (convenience) method that overrides the internal <code>{@link TimestampFactorySpec}</code>
     * instance, creating a new one with the given start time.  If this constant represents a random timestamp
     * factory an exception is thrown.
     * </p>
     * 
     * @param insStart  the starting time instant for the timestamp sequence.
     * 
     * @return  new <code>TensorFactory</code> ready for simulated-valued tensor creation
     * 
     * @throws ConfigurationException   the enumeration constant represents a random timestamp factory
     */
    public TimestampFactory newFactory(Instant insStart) throws ConfigurationException {
        
        // Check timestamp factory type
        if (this.recTmsFacSpec.bolRand())
            throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Cannot specify start time for a random timestamp factory: " + this.name());
        
        TimestampFactorySpec   recSpec = TimestampFactorySpec.from(this.recTmsFacSpec.durPeriod(), insStart);
        
        return recSpec.newFactory();
    }
    

    /**
     * <p>
     * Returns the <code>TimestampFactoryLib</code> enumeration constant with the given name.
     * </p>
     * <p>
     * This a a convenience method that simply calls the method <code>{@link Enum#valueOf(Class, String)}</code>
     * with first argument given by <code>TimestampFactoryLib.class</code> and the second argument given by
     * the argument of this method.  Any exception thrown is caught an returned as a 
     * <code>{@link TypeNotPresentException}</code>.
     * </p>
     * 
     * @param strName   name of the <code>TimestampFactoryLib</code> enumeration constant
     * 
     * @return  the <code>TimestampFactoryLib</code> constant with the given name
     * 
     * @throws TypeNotPresentException  the name was invalid
     */
    public static TimestampFactoryLib  valueFrom(String strName) throws TypeNotPresentException {
        
        try {
            TimestampFactoryLib    enmConst = TimestampFactoryLib.valueOf(TimestampFactoryLib.class, strName);
            
            return enmConst;
            
        } catch (Exception e) {
            throw new TypeNotPresentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Unrecognized name: " + strName, e);
        }
    }
}
