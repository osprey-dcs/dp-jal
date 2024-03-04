/*
 * Project: dp-api-common
 * File:	JavaCollections.java
 * Package: com.ospreydcs.dp.api.common.util
 * Type: 	JavaCollections
 *
 * Copyright 2010-2022 the original author or authors.
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
 * @since Sep 22, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utility class for operations on Java collections and their derivatives.
 * 
 * @author Christopher K. Allen
 * @since Sep 22, 2022
 *
 */
public final class JavaCollections {

    /**
     * <p>
     * Returns a list of <code>Objects</code> given a list of elements of type
     * <code>T</code>.  The objects in the returned list are the same as in the
     * argument just with a type raising operation applied.  
     * An <code>ArrayList&lt;Object&gt;</code> is currently the specific 
     * list implementation.
     * 
     * @param <T> sub-type of class <code>Object</code>
     * 
     * @param lstVals the list to be type-generalized
     * 
     * @return a list of the same objects typed to <codd>Object</code>
     */
    public static <T extends Object> List<Object> toObjectList(List<T> lstVals) {
        ArrayList<Object> lstObjs = new ArrayList<Object>( lstVals.size() );
        
        lstObjs.addAll(lstVals);
        
        return lstObjs;
    }
    
    /**
     * Prints out the head of a collection as a string using carriage returns to delimit
     * the collection elements.  Specifically, print out the fist <code>szHead</code>
     * elements in the collection.  Calls <code>Object{@link #toString()}<code> method
     * on each element of the head.
     * 
     * @param <T> Type of elements in the collection
     * 
     * @param col   collection to print out
     * @param szHead    head size of print out (i.e., number of element values to print)
     * 
     * @return  string representation of the collection head
     */
    public static <T extends Object> String printHead(final Collection<T> col, final Integer szHead) {
        return JavaCollections.printHead(col, szHead, "");
    }

    /**
     * Prints out the head of a collection as a string using carriage returns to delimit
     * the collection elements.  Specifically, print out the fist <code>szHead</code>
     * elements in the collection. Calls <code>Object{@link #toString()}<code> method
     * on each element of the head.
     * 
     * @param <T> Type of elements in the collection
     * 
     * @param col   collection to print out
     * @param szHead    head size of print out (i.e., number of element values to print)
     * @param strLnPrefix desired prefix of the line before element value
     * 
     * @return  string representation of the collection head
     */
    public static <T extends Object> String printHead(final Collection<T> col, final Integer szHead, String strLnPrefix) {
        // Check head size
        int     iStop;
        if (szHead > col.size())
            iStop = col.size();
        else
            iStop = szHead;
        
        // Print out collection elements to CR delimited string
        int i = 0;
        StringBuffer    buf = new StringBuffer();
        for (T t : col) {
            if (++i > iStop) 
                break;
            
            buf.append(strLnPrefix + t.toString() + "\n");
        }
        
        return buf.toString();
    }
    
    /**
     * Prints out the tail of a collection as a string using carriage returns to delimit
     * the collection elements.  Specifically, prints out the last <code>szTail</code>
     * elements in the collection.  Calls <code>Object{@link #toString()}<code> method
     * on each element of the head.
     * 
     * @param <T> Type of elements in the collection
     * 
     * @param col   collection to print out
     * @param szTail tail size of print out (i.e., number of element values to print)
     * 
     * @return  string representation of the collection tail
     */
    public static <T extends Object> String printTail(final Collection<T> col, final Integer szTail) {
        return JavaCollections.printTail(col, szTail, "");
    }
    
    /**
     * Prints out the tail of a collection as a string using carriage returns to delimit
     * the collection elements.  Specifically, prints out the last <code>szTail</code>
     * elements in the collection.  Calls <code>Object{@link #toString()}<code> method
     * on each element of the head.
     * 
     * @param <T> Type of elements in the collection
     * 
     * @param col   collection to print out
     * @param szTail tail size of print out (i.e., number of element values to print)
     * @param strLnPrefix desired prefix of the line before element value
     * 
     * @return  string representation of the collection tail
     */
    public static <T extends Object> String printTail(final Collection<T> col, final Integer szTail, String strLnPrefix) {
        // Check tail size
        int     iStart;
        if (szTail > col.size())
            iStart = 0;
        else 
            iStart = col.size() - szTail - 1;
        
        int i = 0;
        StringBuffer buf = new StringBuffer();
        for (T t : col) {
            if (i++ < iStart)
                continue;
            
            buf.append(strLnPrefix + t.toString() + "\n");
        }
        
        return buf.toString();
    }
    
    /**
     * Prevents instance creation of <code>JavaCollections</code>.
     *
     */
    private JavaCollections() {
    }

}
