/*
 * Project: dp-jal
 * File:	ScalarFactory.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.values
 * Type: 	ScalarFactory
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
 * @since Nov 6, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.values;

import java.util.Random;

import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;

/**
 * <p>
 * Generates a sequence of scalar values either randomly or incrementally.
 * </p> 
 * <p>
 * Creates a sequence of scalar values meant to supply artificial, or "simulated", data.  Scalar value types
 * supported are all those enumerated in <code>{@link JalScalarType}</code>.
 * </p>
 * <p>
 * Scalar value sequences have the following properties:
 * <ul>
 * <li>All scalar values are returns as Java <code>Object</code> instances.</li>
 * <li>Scalar value types (i.e. <code>Object</code> types) are determined by enumeration <code>{@link JalScalarType}</code>.</li>
 * <li>Sequences are generated incrementally or randomly according to configuration at creation/construction.</li>.
 * </ul>
 * Values in the sequence are obtained using repeated invocations of <code>{@link #nextValue()}</code>.
 * </p>  
 *
 *
 * @author Christopher K. Allen
 * @since Nov 6, 2025
 *
 */
public class ScalarFactory implements IDataValueGenerator {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>ScalarFactory</code> instance configured according to the argument.
     * </p>
     * <p>
     * The returned scalar factory is configured, initialized, and ready to produce scalar values with operation
     * <code>{@link #nextValue()}</code> of the <code>{@link IDataValueGenerator}</code> interface.
     * Note that <code>ScalarFactory</code> instances cannot be dynamically configured.  The configuration given
     * at creation is maintained throughout its lifetime.
     * </p>
     * 
     * @param recConfig record containing configuration fields for the returned scalar factory
     * 
     * @return  a new <code>ScalarFactory</code> instance with the given configuration.
     * 
     * @see ScalarFactoryConfig
     */
    public static ScalarFactory from(ScalarFactoryConfig recConfig) {
        return new ScalarFactory(recConfig);
    }
    
    
    //
    // Resources
    //
    
    /** Random number generator used to create random values */
    private final Random        genRandNumbers;
    

    //
    // Configuration
    //
    
    /** The configuration record provided at construction */
    private final ScalarFactoryConfig   recConfig;
    
    /** String value prefix (suffix is numeric) */
    private final String                strPrefix;
    

    /** The field value type */
    private final JalScalarType enmValueType;
    
    /** Random generated value generation enabled/disabled */
    private final boolean       bolRandEnable;

    
    /** Boolean increment value */
    private final Integer       bolIncr;
    
    /** Integer increment value */
    private final Integer       intIncr;
    
    /** Long increment value */
    private final Long          lngIncr;
    
    /** Float increment value */
    private final Float         fltIncr;
    
    /** Double increment value */
    private final Double        dblIncr;
    
    /** String increment value */
    private final Integer       strIncr;
    
    
    //
    // State Variables
    //
    
    /** Current Boolean value (incremental) */
    private Boolean     bolValue = false;
    
    /** Current Integer value (incremental) */
    private Integer     intValue = 0;
    
    /** Current Long value (incremental) */
    private Long        lngValue = 0L;
    
    /** Current Float value (incremental) */
    private Float       fltValue = 0.0F;
    
    /** Current Double value (incremental) */
    private Double      dblValue = 0.0;
    
    /** Current string suffix (incremental) */
    private Integer     strValue = 0;
    
    
    //
    // Constructors 
    //
    
    /**
     * <p>
     * Constructs a new <code>ScalarFactory</code> instance configured to the given argument.
     * </p>
     *
     * @param recConfig the configuration for the new <code>ScalarFactory</code>
     */
    public ScalarFactory(ScalarFactoryConfig recConfig) {
        this.recConfig = recConfig;
        
        this.enmValueType = recConfig.enmValueType();
        this.bolRandEnable = recConfig.bolRandEnable();
        this.strPrefix = recConfig.strPrefix();
        
        this.bolIncr = recConfig.increment().intValue() % 2;
        this.intIncr = recConfig.increment().intValue();
        this.lngIncr = recConfig.increment().longValue();
        this.fltIncr = recConfig.increment().floatValue();
        this.dblIncr = recConfig.increment().doubleValue();
        this.strIncr = recConfig.increment().intValue();
        
        this.genRandNumbers = this.initRandomGenator(this.recConfig);
        this.initCurrentValues(this.recConfig);
    }
    

    
    //
    // IDataValueGenerator Interface
    //
    
    /**
     * <p>
     * Returns the next simulated scalar value in the sequence according to the internal configuration.
     * </p>
     * 
     * @return  next scalar value as a Java <code>Object</code>
     * 
     * @throws  UnsupportedOperationException   the scalar type is <code>{@link JalScalarType#UNSUPPORTED}</code>
     */
    @Override
    public Object nextValue() throws UnsupportedOperationException {
        
        // Get the current value as object
        // - This should be the seed value if first invocation and random=false
        Object objCurr = this.currentValue();   // throws UnsupportedOperationException
        
        // Increment to the next value
        if (this.bolRandEnable)
            this.nextRandomValue();             // throws UnsupportedOperationException
        else
            this.nextIncrementalValue();        // throws UnsuppotedOperationException
        
        // Return the current object
        return objCurr;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Initializes the random number generator according to the argument values.
     * </p>
     * <p>
     * The returned value is given according to the following conditions on the argument:
     * <ul>
     * <li><code>{@link ScalarFactoryConfig#bolRandEnable()}</code> = <code>false</code>: <code>null</code>, </li>
     * <li><code>{@link ScalarFactoryConfig#bolRandEnable()}</code> = <code>false</code>:
     *     <ul>
     *     <li><code>{@link ScalarFactoryConfig#seed()}</code> = <code>0</code>: new <code>Random</code> instance with random seed.</li>
     *     <li><code>{@link ScalarFactoryConfig#seed()}</code> &ne; <code>0</code>: new <code>Random</code> instance with given seed value.</li>
     *     </ul>
     * </li>
     * </ul>
     * <p>
     * 
     * @implSpec
     * This method must be invoked <em>before</em> <code>{@link #initCurrentValues(long)}</code>. 
     * If random number generation is enabled that method requires the <code>{@link #genRandNumbers}</code>
     * instance for initial value generation.  
     * 
     * @param recConfig     the configuration record for the scalar factory
     * 
     * @return  new <code>Random</code> instance, or <code>null</code> if <code>bolRandEnable</code> is <code>false</code> 
     */
    private Random  initRandomGenator(ScalarFactoryConfig recConfig) {
        
        // If random number generation is disabled return null
        if (!recConfig.bolRandEnable())
            return null;
        
        // Create random number generator according to seed value
        if (recConfig.seed() == 0L)
            return new Random();
        
        return new Random(recConfig.seed());
    }
    
    /**
     * <p>
     * Initializes all sequence values for incremental scalar generation.
     * </p>
     * 
     * @param lngIncrSeed   the initial value for the scalar sequence  
     * 
     * @throws  ArithmeticException the argument was too large to convert to an integer
     */
    private void initCurrentValues(ScalarFactoryConfig recConfig) throws ArithmeticException {

        if (this.bolRandEnable) {
            this.bolValue = this.genRandNumbers.nextBoolean();
            this.intValue = this.genRandNumbers.nextInt();
            this.lngValue = this.genRandNumbers.nextLong();
            this.fltValue = this.genRandNumbers.nextFloat();
            this.dblValue = this.genRandNumbers.nextDouble();
            this.strValue = this.genRandNumbers.nextInt();
            
        } else {
            this.bolValue = (Math.toIntExact(recConfig.seed()) % 2 == 0) ? false : true;
            this.intValue = Math.toIntExact(recConfig.seed());   // throws ArithmeticException
            this.lngValue = recConfig.seed();
            this.fltValue = (float)recConfig.seed();
            this.dblValue = (double)recConfig.seed();
            this.strValue = Math.toIntExact(recConfig.seed());
        }
    }
    
    /**
     * <p>
     * Returns the current scalar value as a Java <code>Object</code>.
     * </p>
     * 
     * @return  current scalar value
     * 
     * @throws  UnsupportedOperationException   the scalar type is <code>{@link JalScalarType#UNSUPPORTED}</code>
     */
    private Object currentValue() throws UnsupportedOperationException {
        
        return switch (this.enmValueType) {
        case BOOLEAN -> this.bolValue;
        case INTEGER -> this.intValue;
        case LONG -> this.lngValue;
        case FLOAT -> this.fltValue;
        case DOUBLE -> this.dblValue;
        case STRING -> this.strPrefix + Integer.toString(this.strValue);
        case UNSUPPORTED -> throw new UnsupportedOperationException("Unsupported type case: " + this.enmValueType);
        };
    }
    
    /**
     * <p>
     * Generate the next scalar value incrementally according to value type and stores it.
     * </p>
     * 
     * @throws  UnsupportedOperationException   the scalar type is <code>{@link JalScalarType#UNSUPPORTED}</code>
     */
    private void nextIncrementalValue() throws UnsupportedOperationException {
        
        switch (this.enmValueType) {
        case BOOLEAN:
            this.bolValue = (this.bolIncr==0) ? this.bolValue : !this.bolValue;
            break;
        case INTEGER:
            this.intValue += this.intIncr;
            break;
        case LONG: 
            this.lngValue += this.lngIncr;
            break;
        case FLOAT:
            this.fltValue += this.fltIncr;
            break;
        case DOUBLE:
            this.dblValue += this.dblIncr;
            break;
        case STRING:
            this.strValue += this.strIncr;
            break;
        case UNSUPPORTED:
            throw new UnsupportedOperationException("Unsupported type case: " + this.enmValueType);
        };
    }
    
    /**
     * <p>
     * Generate the next scalar value randomly according to value type and stores it.
     * </p>
     * 
     * @throws  UnsupportedOperationException   the scalar type is <code>{@link JalScalarType#UNSUPPORTED}</code>
     */
    private void nextRandomValue() throws UnsupportedOperationException {
        
        switch (this.enmValueType) {
        case BOOLEAN:
            this.bolValue = genRandNumbers.nextBoolean();
            break;
        case INTEGER:
            this.intValue = genRandNumbers.nextInt();
            break;
        case LONG:
            this.lngValue = genRandNumbers.nextLong();
            break;
        case FLOAT:
            this.fltValue = genRandNumbers.nextFloat();
            break;
        case DOUBLE:
            this.dblValue = genRandNumbers.nextDouble();
            break;
        case STRING:
            this.strValue = genRandNumbers.nextInt();
            break;
        case UNSUPPORTED:
            throw new UnsupportedOperationException("Unsupported type case: " + this.enmValueType);
        };
    }
}
