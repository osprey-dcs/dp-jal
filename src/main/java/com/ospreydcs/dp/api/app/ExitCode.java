/*
 * Project: dp-api-common
 * File:	ExitCode.java
 * Package: com.ospreydcs.dp.api.app
 * Type: 	ExitCode
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
 * @since May 28, 2025
 *
 */
package com.ospreydcs.dp.api.app;

/**
 * <p>
 * Enumeration of potential application termination codes.
 * </p>
 * <p>
 * This enumeration is available for situations involving an irrecoverable error and the application is
 * forced to terminate using a Java <code>{@link System#exit(int)}</code> command.  The application can
 * furnish an exit code for the argument which indicates the status of the exit situation.
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 28, 2025
 *
 */
public enum ExitCode {

    /** Successful execution */
    SUCCESS(0),
    
    /** Bad argument count */
    BAD_ARG_COUNT(1),
    
    /** The input argument was invalid */
    INTPUT_ARG_INVALID(2),
    
    /** The input data pointed to by the input argument was corrupt */
    INPUT_CFG_CORRUPT(3),
    
    /** The input argument was invalid */
    OUTPUT_ARG_INVALID(4),
    
    /** A failure occurred in establishing an output sink */
    OUTPUT_FAILURE(5),
    
    /** Unable to establish a gRPC connection to a service */
    GRPC_CONN_FAILURE(6),
    
    /** General exception during application execution */
    EXECUTION_EXCEPTION(7)
    ;

    /** the integer value of the exit code */
    private final int   intValue;
    
    /** private constructor for enumeration constant - sets code value */
    private ExitCode(int intValue) { this.intValue = intValue; };
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Returns the integer value associated with this constant, that is, the exit code.
     * </p>
     * 
     * @return  the integer value of this exit code
     */
    public int  getCode() { 
        return this.intValue; 
    };
}
