/*
 * Project: dp-data-simulator
 * File:	IDataValueGenerator.java
 * Package: com.ospreydcs.dp.datasim.frame.model
 * Type: 	IDataValueGenerator
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
package com.ospreydcs.dp.jal.tools.ingest.model.values;

/**
 * <p>
 * Required operations for all generators of simulated, heterogeneous data.
 * </p>
 * <p>
 * Implementations are assumed to create a sequence of artificial data where the type and values within
 * the sequence are determined by the implementation and its configuration.
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 13, 2024
 *
 */
public interface IDataValueGenerator {

    //
    // Operations
    //
    
    /**
     * <p>
     * Returns the next simulated data value in the value sequence.
     * </p>
     * 
     * @return  next value in generated sequence as a Java <code>Object</code>
     */
    public Object nextValue();
    
    
}
