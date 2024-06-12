/*
 * Project: dp-api-common
 * File:	ResultStatus.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	ResultStatus
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
 * @since Jan 31, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.common;

/**
 * <p>
 * Convenience record for returning an operation result status. 
 * </p>
 * <p>
 * The record contains extended information beyond the typical Boolean result of "succeed/fail" for a
 * processing operation.  The idea is that processing operations can return enough information to create 
 * for calling method to throw a meaningful exception whenever a failure is encountered.
 * </p>
 * <p>
 * <h2>Conditions and Verifications</h2>
 * According to Java convention, condition and verification checks are typically embodied by
 * class methods with names prefixed by 
 * <ul>
 * <li>"<code><b>is</b>Condition</code>" where <code>Condition</code> refers to the condition
 *     or state being checked.
 * </li> 
 * <li>"<code><b>has</b>Condition</code>" where <code>Condition</code> refers to the condition
 *     or state being verified.
 * </li>
 * </ul>
 * This record is used to provide additional information for condition or verification checking,
 * specifically, a string containing the cause for the success or failure.
 * </p>
 * <p>
 * For example, consider the contrived case where a class <code>WorkerThread</code> requires
 * some type of initialization before it can be launched.  It provides a method 
 * <code>isReady()</code> as follows: 
 * <pre>
 * <code>
 *   class WorkerThread {
 *   
 *     private Object   objTarget = null;
 *   
 *     ResultStatus isReady() {
 *         if (objTarget == null)
 *           return new ResultStatus(false, "Target object has not been assigned.");
 *         else
 *           return new ResultStatus(true);
 *     }
 *     
 *     ...
 *   }
 * </code>
 * </pre>
 * Here the <code>isReady()</code> method provides an explainable as to why the task cannot
 * be launched if it is not ready.
 * </p>
 * 
 * @param success   whether or not the operation/condition/verification succeeded
 * @param message   string message containing cause of success/failure
 * @param cause     possible root-cause exception
 * 
 * @author Christopher K. Allen
 * @since Jan 31, 2024
 *
 */
public record ResultStatus(boolean success, String message, Throwable cause) {
    
    //
    // Private Resources
    //
    
    /** The empty message */
    private static final String STR_EMPTY = "";
    
    /** The empty cause */
    private static final Throwable THR_EMPTY = new Throwable();
    
    
    //
    // Public Resources
    //
    
    /** The universal "Success" record - there need only be one. */
    public static final ResultStatus SUCCESS = new ResultStatus(true);
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new <em>success</em> <code>ResultStatus</code> instance (cause string is empty).
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * Use of this method should be limited, in most cases the static instance 
     * <code>{@link #SUCCESS}</code> can be used.
     * </p>
     * 
     * @return  new <code>ResultStatus(true)</code> instance
     * 
     * @see #SUCCESS
     */
    public static ResultStatus newSuccess() {
        return new ResultStatus(true);
    }
    
    /**
     * <p>
     * Creates a new <em>failed</em> <code>ResultStatus</code> instance with the given message.
     * </p>
     * 
     * @param strMsg description of the failure
     * 
     * @return  new <code>ResultStatus(false, strMsg)</code> instance
     */
    public static ResultStatus  newFailure(String strMsg) {
        return new ResultStatus(false, strMsg, THR_EMPTY);
    }
    
    /**
     * <p>
     * Creates a new <em>failed</em> <code>ResultStatus</code> instance with the given message and cause.
     * </p>
     * 
     * @param strMsg    description of the failure
     * @param thrCause  the exception originating the failure
     *  
     * @return  new <code>ResultStatus(false, strMsg, thrCause)</code> instance
     */
    public static ResultStatus  newFailure(String strMsg, Throwable thrCause) {
        return new ResultStatus(false, strMsg, thrCause);
    }
    
    
    // 
    // Constructors
    //
    
    /**
     * <p>
     * Non-canonical constructor for <code>ResultStatus</code>.
     * </p>
     * <p>
     * Typically used for successful result, no message or cause is provided.
     * </p>
     *
     * @param bolSuccess   result of condition/verification check
     */
    public ResultStatus(boolean bolSuccess) {
        this(bolSuccess, STR_EMPTY, THR_EMPTY);
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>ResultStatus</code>.
     * </p>
     * <p>
     * Typically used for failure result, includes message (no cause).
     * </p>
     *
     * @param bolSuccess    result of condition/verification check
     * @param strMsg        description of success/failure
     */
    public ResultStatus(boolean bolSuccess, String strMsg) {
        this(bolSuccess, strMsg, THR_EMPTY);
    }

    //
    // Conditionals
    //
    
    /**
     * Returns whether or not the condition/verification check was successful.
     *  
     * @return <code>true</code> if condition/verification check was successful
     */
    public final boolean isSuccess() {
        return this.success == true;
    }
    
    /**
     * Returns whether or not the condition/verification check failed.
     *  
     * @return <code>true</code> if condition/verification check failed
     */
    public final boolean isFailure() {
        return this.success == false;
    }
    
    /**
     * <p>
     * Returns whether or not the record contains an associated message.
     * </p>
     * <p>
     * Intended for failure situations where a failure message is typically provided.
     * </p>
     * 
     * @return  <code>true</code> if a result message was set (i.e., not equal to <code>{@link #STR_EMPTY}</code>)
     */
    public final boolean hasMessage() {
        return this.message != STR_EMPTY;
    }
    
    /**
     * <p>
     * Returns whether or not the record contains an associated exception.
     * </p>
     * <p>
     * Intended for failure situations where the cause exception could be provided.
     * </p>
     * 
     * @return  <code>true</code> if cause exception was set (i.e., not equal to <code>{@link #THR_EMPTY}</code>)
     */
    public final boolean hasCause() {
        return this.cause != THR_EMPTY;
    }
    
    
    //
    // Field Getters
    //
    
    /**
     * Returns the <code>{@link #success}</code> attribute directly
     *  
     * @return <code>true</code> if condition/verification check was successful
     */
    public final boolean success() {
        return this.success;
    }
    
//    /**
//     * Returns whether of not the condition/verification check failed.
//     * 
//     * @return <code>true</code> if condition/verification check failed.
//     */
//    public final boolean failed() {
//        return this.success == false;
//    }

    /**
     * <p>
     * Returns the description message for the condition/verification check.
     * </p>
     * <p>
     * Intended for failure situations.
     * </p>
     * 
     * @return message associated with success or failure
     */
    public final String message() {
        return this.message;
    }

    /**
     * <p>
     * Returns the originating exception (i.e., for a failure).
     * </p>
     * <p>
     * Intended for failure situations.
     * </p>
     * 
     * @return  the exception causing the failure
     */
    public final Throwable cause() {
        return this.cause;
    }
    
}
