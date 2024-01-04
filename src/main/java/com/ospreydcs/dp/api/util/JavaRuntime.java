/*
 * Project: dp-api-common
 * File:	JavaRuntime.java
 * Package: com.ospreydcs.dp.api.util
 * Type: 	JavaRuntime
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
 * @since Jan 3, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.util;

/**
 * <p>
 * Utility class containing miscellaneous Java runtime operations.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 3, 2024
 *
 */
public class JavaRuntime {

    
    
    /**
     * Returns the class name of the method calling this method.
     * 
     * @return the fully qualified class name in call stack at depth 2
     */
    public static String getCallerClass() {
        Throwable           throwable = new Throwable();
        StackTraceElement[] arrStackTrace = throwable.getStackTrace();
        String              strCallerClass = arrStackTrace[2].getClassName();
        
        return strCallerClass;
    }

    /**
     * Returns the name of the method calling this method.
     * 
     * @return 2nd method name in call stack
     */
    public static String getCallerName() {
        Throwable           throwable = new Throwable();
        StackTraceElement[] arrStackTrace = throwable.getStackTrace();
        String              strCallerName = arrStackTrace[2].getMethodName();
        
        return strCallerName;
    }
    
    /**
     * <p>
     * Returns the class qualified name of the method calling this method.
     * </p>
     * <p>
     * The returned value has the following form
     * <br/> <br/>
     * &nbsp; &nbsp; [ClassName]#[MethodName]
     * <br/> <br/>
     * where [ClassName] is the result of <code>{@link #getCallerClass()}</code> and [MethodName]
     * is the result of <code>{@link #getCallerName()}</code>.
     * </p>
     * 
     * @return fully qualified method name of the caller
     */
    public static String getQualifiedCallerName() {
        Throwable           throwable = new Throwable();
        StackTraceElement[] arrStackTrace = throwable.getStackTrace();
        String              strClsName = arrStackTrace[2].getClassName();
        String              strMthName = arrStackTrace[2].getMethodName();
        
        String              strQulName = strClsName + "#" + strMthName;
        
        return strQulName;
    }
    
    
    
    //
    // Private Methods
    //
    
    /**
     * <p>
     * Prevent construction of <code>JavaRuntime</code> instances.
     * </p>
     *
     */
    private JavaRuntime() {
    }

}
