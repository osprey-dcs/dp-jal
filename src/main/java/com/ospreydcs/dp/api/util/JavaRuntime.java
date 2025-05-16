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

import java.util.stream.Stream;

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

    //
    // Class Constants
    //
    
    /** Call stack depth to fetch current method details */
    public static final int     INT_STACK_DEPTH_CURR = 2;
    
    /** Call stack depth to fetch caller details */
    public static final int     INT_STACK_DEPTH_CALLER = 3;
    
    

    //
    // Operations
    //
    
    /**
     * Returns the fully qualified class name of the current method calling this method.
     * 
     * @return the fully qualified class name in call stack at depth {@value #INT_STACK_DEPTH_CURR}
     */
    public static String getMethodClass() {
        StackTraceElement[] arrStackTrace = Thread.currentThread().getStackTrace();
        String              strCallerClass = arrStackTrace[INT_STACK_DEPTH_CURR].getClassName();
        
        return strCallerClass;
    }

    /**
     * Returns the fully qualified class name of the method calling this method.
     * 
     * @return the fully qualified class name in call stack at depth {@value #INT_STACK_DEPTH_CALLER}
     */
    public static String getCallerClass() {
        StackTraceElement[] arrStackTrace = Thread.currentThread().getStackTrace();
        String              strCallerClass = arrStackTrace[INT_STACK_DEPTH_CALLER].getClassName();
        
        return strCallerClass;
    }
    
    /**
     * <p>
     * Returns the class type instance of the method calling this method.
     * 
     * @return  the class type in the call stack at depth {@value #INT_STACK_DEPTH_CALLER}
     */
    public static Class<?>  getCallerClassType() {
        StackTraceElement[] arrStackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement   lvlStackTrace = arrStackTrace[INT_STACK_DEPTH_CALLER];
        
        Class<?> clsCaller = lvlStackTrace.getClass();
        
        return clsCaller;
    }

    /**
     * Returns the simple class name of the current method calling this method.
     * 
     * @return the fully qualified class name in call stack at depth {@value #INT_STACK_DEPTH_CURR}
     */
    public static String getMethodClassSimple() {
        StackTraceElement[] arrStackTrace = Thread.currentThread().getStackTrace();
        String              strCallerClass = arrStackTrace[INT_STACK_DEPTH_CURR].getClassName();
        
        int                 indNameSimple = strCallerClass.lastIndexOf('.');
        String              strNameSimple = strCallerClass.substring(indNameSimple + 1);
        
        return strNameSimple;
    }

    /**
     * Returns the simple class name of the method calling the current method.
     * 
     * @return the fully qualified class name in call stack at depth {@value #INT_STACK_DEPTH_CALLER}
     */
    public static String getCallerClassSimple() {
        StackTraceElement[] arrStackTrace = Thread.currentThread().getStackTrace();
        String              strCallerClass = arrStackTrace[INT_STACK_DEPTH_CALLER].getClassName();
        
        int                 indNameSimple = strCallerClass.lastIndexOf('.');
        String              strNameSimple = strCallerClass.substring(indNameSimple + 1);
        
        return strNameSimple;
    }

    /**
     * Returns the name of the current method.
     * 
     * @return 2nd method name in call stack
     */
    public static String getMethodName() {
        StackTraceElement[] arrStackTrace = Thread.currentThread().getStackTrace();
        String              strCallerName = arrStackTrace[INT_STACK_DEPTH_CURR].getMethodName();
        
        return strCallerName;
    }
    
    /**
     * Returns the name of the method calling the current method.
     * 
     * @return 1st method name in call stack
     */
    public static String getCallerName() {
        StackTraceElement[] arrStackTrace = Thread.currentThread().getStackTrace();
        String              strCallerName = arrStackTrace[INT_STACK_DEPTH_CALLER].getMethodName();
        
        return strCallerName;
    }
    
    /**
     * <p>
     * Returns the fully qualified name of the current method (including class name).
     * </p>
     * <p>
     * The returned value has the following form
     * <br/> <br/>
     * &nbsp; &nbsp; [ClassName]#[MethodName]
     * <br/> <br/>
     * where [ClassName] is the result of <code>{@link #getMethodClass()}</code> and [MethodName]
     * is the result of <code>{@link #getMethodName()}</code>.
     * </p>
     * 
     * @return fully qualified method name 
     */
    public static String getQualifiedMethodName() {
        StackTraceElement[] arrStackTrace = Thread.currentThread().getStackTrace();
        String              strClsName = arrStackTrace[INT_STACK_DEPTH_CURR].getClassName();
        String              strMthName = arrStackTrace[INT_STACK_DEPTH_CURR].getMethodName();
        
        String              strQulName = strClsName + "#" + strMthName;
        
        return strQulName;
    }
    
    /**
     * <p>
     * Returns the fully qualified name of the method calling the current method (including class name).
     * </p>
     * <p>
     * The returned value has the following form
     * <br/> <br/>
     * &nbsp; &nbsp; [ClassName]#[MethodName]
     * <br/> <br/>
     * where [ClassName] is the result of <code>{@link #getMethodClass()}</code> and [MethodName]
     * is the result of <code>{@link #getMethodName()}</code>.
     * </p>
     * 
     * @return fully qualified method name of the caller
     */
    public static String getQualifiedCallerName() {
        StackTraceElement[] arrStackTrace = Thread.currentThread().getStackTrace();
        String              strClsName = arrStackTrace[INT_STACK_DEPTH_CALLER].getClassName();
        String              strMthName = arrStackTrace[INT_STACK_DEPTH_CALLER].getMethodName();
        
        String              strQulName = strClsName + "#" + strMthName;
        
        return strQulName;
    }
    
    /**
     * <p>
     * Returns the simple qualified name of the current method (including class name).
     * </p>
     * <p>
     * The returned value has the following form
     * <br/> <br/>
     * &nbsp; &nbsp; [ClassName]#[MethodName]
     * <br/> <br/>
     * where [ClassName] is the result of <code>{@link #getMethodClassSimple()}</code> and 
     * [MethodName] is the result of <code>{@link #getMethodName()}</code>.
     * </p>
     * 
     * @return simple qualified method name of the caller
     */
    public static String getQualifiedMethodNameSimple() {
        StackTraceElement[] arrStackTrace = Thread.currentThread().getStackTrace();
        String              strClsName = arrStackTrace[INT_STACK_DEPTH_CURR].getClassName();
        String              strMthName = arrStackTrace[INT_STACK_DEPTH_CURR].getMethodName();
        
        int                 indClsNameSimple = strClsName.lastIndexOf('.');
        String              strClsNameSimple = strClsName.substring(indClsNameSimple + 1);
        
        String              strQulName = strClsNameSimple + "#" + strMthName;
        
        return strQulName;
    }
    
    /**
     * <p>
     * Returns the simple qualified name of the method calling the current method (including class name).
     * </p>
     * <p>
     * The returned value has the following form
     * <br/> <br/>
     * &nbsp; &nbsp; [ClassName]#[MethodName]
     * <br/> <br/>
     * where [ClassName] is the result of <code>{@link #getMethodClassSimple()}</code> and 
     * [MethodName] is the result of <code>{@link #getMethodName()}</code>.
     * </p>
     * 
     * @return simple qualified method name of the caller
     */
    public static String getQualifiedCallerNameSimple() {
        StackTraceElement[] arrStackTrace = Thread.currentThread().getStackTrace();
        String              strClsName = arrStackTrace[INT_STACK_DEPTH_CALLER].getClassName();
        String              strMthName = arrStackTrace[INT_STACK_DEPTH_CALLER].getMethodName();
        
        int                 indClsNameSimple = strClsName.lastIndexOf('.');
        String              strClsNameSimple = strClsName.substring(indClsNameSimple + 1);
        
        String              strQulName = strClsNameSimple + "#" + strMthName;
        
        return strQulName;
    }
    
    /**
     * <p>
     * Determines whether or not the object is of a type having a default constructor.
     * </p>
     * <p>
     * Extracts the class type of the argument then calls 
     * <code>{@link #hasDefaultConstructor(Class)}</code>.
     * </p>
     * 
     * @param clsType   Java object
     * 
     * @return <code>true</code> if the class has a default (zero-argument) constructor,
     *         <code>false</code> otherwise
     *         
     * @see #hasDefaultConstructor(Class)
     */
    public static boolean    hasDefaultConstructor(Object obj) {
        Class<?>    clsType = obj.getClass();
        
        return JavaRuntime.hasDefaultConstructor(clsType);
    }
    
    /**
     * <p>
     * Determines whether or not a class type has a default constructor.
     * </p>
     * <p>
     * Parses through (i.e., streams through) all constructors for the given type
     * check for one having no arguments (i.e., the "zero-argument" constructor).
     * </p>
     * 
     * @param clsType   any anonymous class type
     * 
     * @return <code>true</code> if the class has a default (zero-argument) constructor,
     *         <code>false</code> otherwise
     */
    public static boolean    hasDefaultConstructor(Class<?> clsType) {
        return Stream.of( clsType.getConstructors() ).noneMatch(c -> c.getParameterCount() == 0);
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
