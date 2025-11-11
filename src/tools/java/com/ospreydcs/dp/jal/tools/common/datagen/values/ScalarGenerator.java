/*
 * Project: dp-data-simulator
 * File:	ScalarGenerator.java
 * Package: com.ospreydcs.dp.datasim.frame.model
 * Type: 	ScalarGenerator
 *
 * Copyright 2010-2023 the original author or authors.
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
 * @since May 9, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.jal.tools.common.datagen.values;

import java.util.Random;

import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.JalToolsScalarValuesConfig;


/**
 * <p>
 * Generates scalar values either randomly or incrementally.
 * </p> 
 * <p>
 * Creates a sequence of scalar values meant to supply artificial, or "simulated", data.
 * </p>  
 * <ul>
 * <li>All scalar values are returns as Java <code>Object</code> instances.</li>
 * <li>Scalar value types (i.e. <code>Object</code> types) are determined by enumeration <code>{@link JalScalarType}</code>.</li>
 * <li>Sequences are generated incrementally or randomly (see <code>{@link #setRandom(boolean)}</code>)</li>.
 * </ul>
 * Values in the sequence are returned using repeated invocations of <code>{@link #nextValue()}</code>.
 *
 *
 * @author Christopher K. Allen
 * @since May 9, 2024
 * 
 * @deprecated Replaced by ScalarFactory
 */
@Deprecated(since="Nov 11, 2025", forRemoval=true)
public class ScalarGenerator implements IDataValueGenerator {
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>ScalarGenerator</code> providing simulated scalar values.
     * </p>
     * <p>
     * Scalar values are generated incrementally by default.
     * </p>
     *
     * @param type  scalar value type
     * 
     * @return  a new <code>ScalarGenerator</code> instances ready for simulated number generation
     */
    public static ScalarGenerator   from(JalScalarType enmType) {
        return new ScalarGenerator(enmType);
    }
    
    /**
     * <p>
     * Creates and returns a new instance of <code>ScalarGenerator</code> providing simulated scalar values.
     * </p>
     * <p>
     * Scalar values are generated incrementally by default (i.e., not random).  
     * The seed value is used for the first scalar value.
     * </p>
     *
     * @param type      scalar value type
     * @param seed      initial scalar value
     *  
     * @return  a new <code>ScalarGenerator</code> instances ready for simulated number generation
     */
    public static ScalarGenerator   from(JalScalarType enmType, long seed) {
        return new ScalarGenerator(enmType, seed);
    }
    
    /**
     * <p>
     * Creates and returns a new instance of <code>ScalarGenerator</code> providing simulated scalar values.
     * </p>
     * <p>
     * Scalar values are generated randomly if <code>random</code> is <code>true</code> and incrementally if
     * <code>random</code> is <code>false</code>.  
     * The seed value is used for the random number generator in the former case and as the first scalar value
     * in the latter case.
     * </p>
     *
     * @param type      scalar value type
     * @param seed      initial scalar value or random number seed
     * @param useRandom <code>true</code> generate random sequence, <code>false</code> generate incremental sequence
     *  
     * @return  a new <code>ScalarGenerator</code> instances ready for simulated number generation
     */
    public static ScalarGenerator   from(JalScalarType enmType, long seed, boolean useRandom) {
        return new ScalarGenerator(enmType, seed, useRandom);
    }
    
    //
    // Library Resources
    //
    
    /** The default parameters for scalar-valued simulated data generation */
    private static final JalToolsScalarValuesConfig     CFG_DEF = JalToolsConfig.getInstance().datagen.values.scalar;
    
    
    //
    // Class Constants - Default Arguments
    //
    
    /** The default enable/disable random number generator */
    public static final boolean BOL_RAND_ENBL_DEF = CFG_DEF.random.enabled;

    /** The default random number generator seed value */
    public static final long    LNG_RAND_SEED_DEF = CFG_DEF.random.seed;

    //
    // Class Constants - Scalar increment values
    //
    
    /** String value prefix */
    private final static String     STR_PREFIX_DEF = CFG_DEF.stringPrefix;

    
    /** The default incremental seed value */
    public static final long        LNG_INCR_SEED_DEF = CFG_DEF.increment.seed;
    
    /** Integer value default increment */
    private final static Integer    INT_INCR_DEF = CFG_DEF.increment.integerv;
    
    /** Long value default increment */
    private final static Long       LNG_INCR_DEF = CFG_DEF.increment.longv;
    
    /** Float value default increment */
    private final static Float      FLT_INCR_DEF = CFG_DEF.increment.floatv;
    
    /** Double value default increment */
    private final static Double     DBL_INCR_DEF = CFG_DEF.increment.doublev;
    
    /** String value increment */
    private final static Integer    INT_STR_INCR_DEF = CFG_DEF.increment.stringv;
    

    //
    // Resources
    //
    
    /** Random number generator used to create random values */
    private final Random        rndNumGenerator;
    

    //
    // Configuration
    //
    
    /** The field value type */
    private final JalScalarType enmFieldType;
    
    /** The initial seed value */
    private final long          lngSeed;
    
    
    /** Use randomly generated field values */
    private boolean             bolUseRandom;
    

    //
    // Instance Variables
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
    private Integer     intStrSuffix = 0;
    
    
    //
    // Constructors 
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>ScalarGenerator</code> providing simulated scalar values.
     * </p>
     * <p>
     * Scalar values are generated incrementally by default.
     * </p>
     *
     * @param type  scalar value type
     */
    public ScalarGenerator(JalScalarType type) {
        this(type, LNG_INCR_SEED_DEF);
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>ScalarGenerator</code> providing simulated scalar values.
     * </p>
     * <p>
     * Scalar values are generated incrementally by default (i.e., not random).  
     * The seed value is used for the first scalar value.
     * </p>
     *
     * @param type      scalar value type
     * @param seed      initial scalar value 
     * 
     * @throws  ArithmeticException the seed was too large to convert to an integer value
     */
    public ScalarGenerator(JalScalarType type, long seed) throws ArithmeticException {
        this(type, seed, BOL_RAND_ENBL_DEF);
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>ScalarGenerator</code> providing simulated scalar values.
     * </p>
     * <p>
     * Scalar values are generated randomly if <code>random</code> is <code>true</code> and incrementally if
     * <code>random</code> is <code>false</code>.  
     * The seed value is used for the random number generator in the former case and as the first scalar value
     * in the latter case.
     * </p>
     *
     * @param type      scalar value type
     * @param seed      initial scalar value or random number seed
     * @param useRandom <code>true</code> generate random sequence, <code>false</code> generate incremental sequence
     * 
     * @throws  ArithmeticException the seed was too large to convert to an integer value
     */
    public ScalarGenerator(JalScalarType type, long seed, boolean useRandom) throws ArithmeticException {
        this.enmFieldType = type;
        this.lngSeed = seed;
        this.bolUseRandom = useRandom;
        this.rndNumGenerator = new Random();

        // Initialize the scalar sequence
        this.initSequenceValues(seed);
    }
    

    //
    // Configuration
    //
    
    /**
     * <p>
     * Toggles the use of a random value generator for scalar value creation.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>
     * Using random generation can creating a significant resource demand for large number of values.
     * Internally random values are generated using a <code>{@link Random}</code> Java object.
     * </li>
     * <li>
     * Scalar values are generated incrementally by default.  Random number generation is turned on
     * using this function.
     * </li>
     * </p>  
     * 
     * @param useRandomValues   <code>true</code> generate scalar values randomly,
     *                          <code>false</code> generate scalar values incrementally
     */
    public void setRandom(boolean useRandomValues) {
        this.bolUseRandom = useRandomValues;
    }
    
    /**
     * @return  <code>true</code> the scalar sequence is generated randomly,
     *          <code>false</code> the scalar sequence is generated incrementally
     */
    public boolean      isRandom() {
        return this.bolUseRandom;
    }
    
    /**
     * @return  scalar type of generated sequence
     */
    public JalScalarType   getType() {
        return this.enmFieldType;
    }
    
    /**
     * @return  returns the seed value used to initialize the scalar value sequence
     */
    public long          getSeed() {
        return this.lngSeed;
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
     */
    @Override
    public Object nextValue() {
        
        // Get the current value as object
        // - This should be the seed value if first invocation and random=false
        Object objCurr = this.currentValue();
        
        // Increment to the next value
        if (this.bolUseRandom)
            this.nextRandomValue();
        else
            this.nextIncrementalValue();
        
        // Return the current object
        return objCurr;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Initializes all sequence values for incremental scalar generation.
     * </p>
     * 
     * @param lngSeed   the initial value for the scalar sequence  
     * 
     * @throws  ArithmeticException the argument was too large to convert to an integer
     */
    private void initSequenceValues(long lngSeed) throws ArithmeticException {

        if (this.bolUseRandom) {
            this.bolValue = this.rndNumGenerator.nextBoolean();
            this.intValue = this.rndNumGenerator.nextInt();
            this.lngValue = this.rndNumGenerator.nextLong();
            this.fltValue = this.rndNumGenerator.nextFloat();
            this.dblValue = this.rndNumGenerator.nextDouble();
            this.intStrSuffix = this.rndNumGenerator.nextInt();
            
        } else {
            this.bolValue = false;
            this.intValue = Math.toIntExact(lngSeed);   // throws ArithmeticException
            this.lngValue = Long.valueOf(lngSeed);
            this.fltValue = Float.valueOf(lngSeed);
            this.dblValue = Double.valueOf(lngSeed);
            this.intStrSuffix = Math.toIntExact(lngSeed);
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
        
        return switch (this.enmFieldType) {
        case BOOLEAN -> Boolean.valueOf( this.bolValue );
        case INTEGER -> Integer.valueOf( this.intValue );
        case LONG -> Long.valueOf( this.lngValue );
        case FLOAT -> Float.valueOf( this.fltValue );
        case DOUBLE -> Double.valueOf( this.dblValue );
        case STRING -> STR_PREFIX_DEF + Integer.toString(this.intStrSuffix);
        case UNSUPPORTED -> throw new UnsupportedOperationException("Unsuppoted case: " + this.enmFieldType);
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
        
        switch (this.enmFieldType) {
        case BOOLEAN:
            this.bolValue = !this.bolValue;
            break;
        case INTEGER:
            this.intValue = (this.intValue + INT_INCR_DEF);
            break;
        case LONG: 
            this.lngValue = (this.lngValue + LNG_INCR_DEF);
            break;
        case FLOAT:
            this.fltValue = (this.fltValue + FLT_INCR_DEF);
            break;
        case DOUBLE:
            this.dblValue = (this.dblValue + DBL_INCR_DEF);
            break;
        case STRING:
            this.intStrSuffix = (this.intStrSuffix + INT_STR_INCR_DEF);
            break;
        case UNSUPPORTED:
            throw new UnsupportedOperationException("Unsuppoted case: " + this.enmFieldType);
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
        
        switch (this.enmFieldType) {
        case BOOLEAN:
            this.bolValue = rndNumGenerator.nextBoolean();
            break;
        case INTEGER:
            this.intValue = rndNumGenerator.nextInt();
            break;
        case LONG:
            this.lngValue = rndNumGenerator.nextLong();
            break;
        case FLOAT:
            this.fltValue = rndNumGenerator.nextFloat();
            break;
        case DOUBLE:
            this.dblValue = rndNumGenerator.nextDouble();
            break;
        case STRING:
            this.intStrSuffix = rndNumGenerator.nextInt();
            break;
        case UNSUPPORTED:
            throw new UnsupportedOperationException("Unsuppoted case: " + this.enmFieldType);
        };
    }
}
