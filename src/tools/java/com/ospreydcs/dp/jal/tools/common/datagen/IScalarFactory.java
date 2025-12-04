/*
 * Project: dp-data-simulator
 * File:	IScalarFactory.java
 * Package: com.ospreydcs.dp.datasim.frame.model
 * Type: 	IScalarFactory
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
 * @since May 13, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.jal.tools.common.datagen;

/**
 * <p>
 * Required operations for all generators of scalar-valued simulated, heterogeneous data.
 * </p>
 * <p>
 * Implementations are assumed to create a sequence of artificial data where the type is assumed to be
 * scalar valued (e.g., Boolean, integer, float, etc.).  The data types and specific values and within
 * the sequence are determined by the implementation and its configuration.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * As this interface inherits from <code>{@link IDatumFactory}</code> its use is primarily to obviate
 * scalar value creation.  The operation <code>{@link #nextDatum()}</code> returns a value of Java
 * type <code>Object</code>, thus, it is impossible to explicitly enforce the creation of scalar-typed
 * values.  
 * </p>  
 * <p>
 * This child interface overrides the operation <code>{@link IDatumFactory#getComplexType()}</code> 
 * with a default implementation that returns <code>{@link JalComplexType#SCALAR}</code>.
 * It is the responsibility of any implementing class to ensure that the created data values
 * are of the proper type.
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 13, 2024
 *
 */
public interface IScalarFactory extends IDatumFactory {

    
    //
    // Default Implementations
    //
    
    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IDatumFactory#getComplexType()
     */
    default public JalComplexType getComplexType() {
        return JalComplexType.SCALAR;
    }
    
}
