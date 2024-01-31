/*
 * Project: dp-api-common
 * File:	ResultRecord.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	ResultRecord
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
package com.ospreydcs.dp.api.model;

/**
 * <p>
 * Convenience record for returning extended results from condition and verification checks.
 * </p>
 * <p>
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
 *     ResultRecord isReady() {
 *         if (objTarget == null)
 *           return new ResultRecord(false, "Target object has not been assigned.");
 *         else
 *           return new ResultRecord(true);
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
 * @param success       whether or not condition/verification check succeeded
 * @param strCause      string message containing cause of success/failure
 * 
 * @author Christopher K. Allen
 * @since Jan 31, 2024
 *
 */
public record ResultRecord(boolean success, String strCause) {
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new <em>success</em> <code>ResultRecord</code> instance (cause string is empty).
     * </p>
     * 
     * @return  new <code>ResultRecord(true)</code> instance
     */
    public static ResultRecord newSuccess() {
        return new ResultRecord(true);
    }
    
    /**
     * <p>
     * Creates a new <em>failed</em> <code>ResultRecord</code> instance with the given cause.
     * </p>
     * 
     * @param strCause  cause of the failure
     * 
     * @return  new <code>ResultRecord(false, strCause)</code> instance
     */
    public static ResultRecord  newFailure(String strCause) {
        return new ResultRecord(false, strCause);
    }
    
    // 
    // Constructors
    //
    
    /**
     * <p>
     * Non-canonical constructor for <code>ResultRecord</code>.
     * </p>
     * <p>
     * Typically used for successful result, no cause is provided.
     * </p>
     *
     * @param Success   result of condition/verification check
     */
    public ResultRecord(boolean Success) {
        this(Success, "");
    }

    //
    // Field Getters
    //
    
    /**
     * Returns whether or not the condition/verification check was successful.
     *  
     * @return <code>true</code> if condition/verification check was succesul
     */
    public final boolean success() {
        return this.success;
    }
    
    /**
     * Returns whether of not the condition/verification check failed.
     * 
     * @return <code>true</code> if condition/verification check failed.
     */
    public final boolean failed() {
        return this.success == false;
    }

    /**
     * Returns the cause message for the condition/verification check.
     * <p>
     * Intended availability for failure situations.
     * 
     * @return the cause of the success or failure
     */
    public final String getCause() {
        return this.strCause;
    }

    
}
