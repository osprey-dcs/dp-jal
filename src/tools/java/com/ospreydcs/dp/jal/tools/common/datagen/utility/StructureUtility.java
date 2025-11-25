/*
 * Project: dp-data-simulator
 * File:	StructureUtility.java
 * Package: com.ospreydcs.dp.datasim.utility
 * Type: 	StructureUtility
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
 * @since May 18, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.jal.tools.common.datagen.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;

/**
 * <p>
 * Utility class for performing common structure operations.
 * </p>
 * <p>
 * The methods here are for processing tree structure objects created by instances of the class
 * <code>{@link com.ospreydcs.dp.datasim.model.values.StructureGenerator}</code>.
 * </p> 
 * 
 *
 * @author Christopher K. Allen
 * @since May 18, 2024
 *
 */
public final class StructureUtility {
    
    
    //
    // Enclosed Types
    //
    
    /**
     * <p>
     * Record containing a (name, value) pair) typically used for returning structure
     * nodes or values including field name.
     * </p>
     * 
     * @param   name    field name within tree structure
     * @param   value   field value within tree structure
     */
    public static record NameValuePair(String name, Object value) {
        
        @Override
        public String toString() {
            return name + "=" + value.toString();
        }
    }
    
    /**
     * <p>
     * Record containing an (index, value) pair typically used for returning structure nodes or
     * values including structure index location.
     * </p>
     * 
     * @param index     index list within tree structure
     * @param value     structure field value at the given index  
     */
    public static record IndexValuePair(List<Integer> index, Object value) {

        @Override
        public String toString() {
            return index.toString() + "=" + value.toString();
        }
    }
    

    //
    // Operations
    //
    
    /**
     * <p>
     * Computes the number of terminate nodes <i>n</i> for a symmetric tree structure.
     * </p>
     * <p
     * The number of terminal nodes <i>n</i> for a symmetric, tree structure is given 
     * according to the formula
     * <pre>
     *   <i>n</i> = <i>F</i><sup><i>D</i></sup>
     * </pre>
     * where <i>D</i> = <code>cntDepth</code> and <i>F</i> = <code>cntFanout</code>.
     * </p>
     *  
     * @param cntDepth  tree structure depth
     * @param cntFanout structure node fan out (number of sub-nodes per node)
     * 
     * @return  number of terminal fields for symmetric tree structure with given properties
     */
    public static int computeTerminalNodeCount(int cntDepth, int cntFanout) {
        int     cntNodes = 1;
        
        // Computes F^D 
        for (int i=0; i<cntDepth; i++)
            cntNodes *= cntFanout;
        
        return cntNodes;
    }
    
    /**
     * <p>
     * Compute the number of internal nodes <i>&sigma;</i> for a symmetric tree structure.
     * </p>
     * <p>
     * The number of internal nodes <i>&sigma;</i> (i.e., non-terminal nodes) within a symmetric,
     * tree structure is given according to the formula
     * <pre>
     *   <i>&sigma;</i> = (<i>F</i><sup><i>D</i></sup> - 1) / (<i>F</i> - 1)
     * </pre>
     * where <i>D</i> = <code>cntDepth</code> and <i>F</i> = <code>cntFanout</code>.
     * </p>
     * 
     * @param cntDepth  tree structure depth
     * @param cntFanout structure node fan out (number of sub-nodes per node)
     * 
     * @return  number of internal (non-terminal) nodes within tree structure with given properties
     */
    public static int   computeInternalNodeCount(int cntDepth, int cntFanout) {
        
        // Compute F^D
        int cntNodes = StructureUtility.computeTerminalNodeCount(cntDepth, cntFanout);
        
        // Compute (F^D - 1)/(F - 1)
        cntNodes = (cntNodes - 1)/(cntFanout - 1);
        
        return cntNodes;
    }
    
    /**
     * <p>
     * Computes the total number of nodes <i>N</i> within a symmetric tree structure according to the
     * formula
     * <pre/>
     *   <i>N</i> = (<i>F</i><sup><i>D</i>+1</sup> - 1) / (<i>F</i> - 1)
     * </pre>
     * where <i>D</i> = <code>cntDepth</code> and <i>F</i> = <code>cntFanout</code>.
     * </p>
     * 
     * @param cntDepth  tree structure depth
     * @param cntFanout structure node fan out (number of sub-nodes per node)
     * 
     * @return  total number of nodes within a symmetric tree structure with given properties
     */
    public static int computeTotalNodeCount(int cntDepth, int cntFanout) {
        
        // Computes F^(D+1) - 1
        int     cntNodes = cntFanout * StructureUtility.computeTerminalNodeCount(cntDepth, cntFanout) - 1;
        
        // Computes (F^(D+1) - 1)/(F - 1)
        cntNodes = cntNodes / (cntFanout - 1);
        
        return cntNodes;
    }
    
    /**
     * <p>
     * Counts the number of terminal fields within the given tree structure by direct inspection.
     * </p>
     * <p>
     * The number of terminal fields (final nodes) within the given structure are counted by recursively
     * traversing the nodes within the tree structure.  That is, this is a recursive method where the 
     * initial invocation is made using the <code>Map&lt;String, Object&gt;</code> type structure as the
     * argument.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The argument is not required to be a symmetric tree structure.
     * </p>
     *  
     * @param objStruct a Java <code>Map&lt;String, Object&gt;</code> object containing a tree structure
     * 
     * @return  the number of terminal nodes within the given tree structure arguments
     */
    public static int   terminalNodeCount(Object objStruct) {
        
        // Number of fields within this branch (total if root)
        int     cntFlds = 0;
        
        if (objStruct instanceof Map mapStruct) {
            for (Object objNode : mapStruct.values()) {
                cntFlds += StructureUtility.terminalNodeCount(objNode);
            }
        } else {
            cntFlds++;
        }
        
        return cntFlds;
    }
    
    /**
     * <p>
     * Counts the total number of nodes within the given tree structure by direct inspection.
     * </p>
     * <p>
     * The total number of nodes within the given structure are counted by recursively
     * traversing the nodes within the tree structure.  That is, this is a recursive method where the 
     * initial invocation is made using the <code>Map&lt;String, Object&gt;</code> type structure as the
     * argument.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The argument is not required to be a symmetric tree structure.
     * </p>
     *  
     * @param objStruct a Java <code>Map&lt;String, Object&gt;</code> object containing a tree structure
     * 
     * @return  total number of nodes within the given tree structure argument (including root node)
     */
    public static int   totalNodeCount(Object objStruct) {
        int     cntNodes = 1;   // include self
        
        if (objStruct instanceof Map mapStruct) {
            for (Object objNode : mapStruct.values()) {

                if (objNode instanceof Map) {
                    cntNodes += StructureUtility.totalNodeCount(objNode);
                    
                } else {
                    cntNodes++;
                }
            }
        }
        
        return cntNodes;
    }
    
    /**
     * <p>
     * Checks that the terminal node within the given structure contains a value of the given type.
     * </p>
     * <p>
     * The method recursively traverses the tree structure to isolate the first terminal node in the tree.
     * That value of that node is then compared with the given type argument using
     * <code>{@link ScalarType#isAssignable(Object)}</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * This method compares only the first terminal encountered.  It is assumed that all terminal values
     * are of the same type.
     * </p> 
     * 
     * @param objStruct a Java <code>Map&lt;String, Object&gt;</code> object containing a tree structure
     * @param enmType   the type enumeration for the terminal value
     * 
     * @return  <code>true</code> if the first terminal value encountered is of the given type,
     *          <code>false</code> otherwise
     * 
     * @throws IllegalArgumentException no terminal fields were found or root argument not a structure
     */
    public static boolean   isTerminalValueOfType(Object objStruct, JalScalarType enmType) throws IllegalArgumentException {
        
        if (objStruct instanceof Map mapStruct) {
            @SuppressWarnings("unchecked")
            List<Object>    lstFields = mapStruct.values().stream().toList();

            if (lstFields.isEmpty())
                throw new IllegalArgumentException("Encountered node with no fields.");

            Object          objField0 = lstFields.get(0);
            if (objField0 instanceof Map mapSubNode)
                return StructureUtility.isTerminalValueOfType(mapSubNode, enmType);
            else
                return enmType.isAssignable(objField0);
        }
        
        throw new IllegalArgumentException("Argument object was not a Structure Map.");
    }
    
    /**
     * <p>
     * Extracts all the field names (i.e., node names) from the given tree structure.
     * </p>
     * <p>
     * The method uses the recursive function <code>{@link #extractFieldNameHelper(List, int, Object)}</code>
     * to extract all field names according to their depth within the tree structure.
     * The returned value is a list of name lists.  Specifically, each list within the root list contains
     * the names of the fields at the given depth.  For example, <code>{@link List#get(int)}</code> returns
     * the field names at the first level when using argument <code>0</code>, the field names at the second 
     * level when using argument <code>1</code>, etc.
     * </p>
     * 
     * @param objStruct a Java <code>Map&lt;String, Object&gt;</code> object containing a tree structure
     * 
     * @return  a list containing structure field names at each level
     * 
     * @throws IllegalArgumentException encountered internal structure node (not terminal) without sub-fields
     */
    public static List<List<String>>    extractFieldNames(Object objStruct) throws IllegalArgumentException {
        
        // Returned object
        List<List<String>>      lstLvlFldNms = new LinkedList<>();

        // Build the field names list of lists
        StructureUtility.extractFieldNameHelper(lstLvlFldNms, 1, objStruct);
        
        return lstLvlFldNms;
    }
    
    /**
     * <p>
     * Convenience method for extracting the scalar value of the first terminal node encountered
     * traversing the tree structure.
     * </p>
     * <p>
     * The method recursive calls itself down each sub-node encountered within the tree structure
     * until it finds a terminal node (a node with no sub-nodes, i.e., not a <code>Map</code> object).
     * That value is then returned.
     * </p>
     * 
     * @param objStruct a Java <code>Map&lt;String, Object&gt;</code> object containing a tree structure
     * 
     * @return  the first terminal value encountered traversing the given tree
     * 
     * @throws IllegalArgumentException encountered internal structure node (not terminal) without sub-fields
     */
    public static Object    extractFirstTerminalValue(Object objStruct) throws IllegalArgumentException {

        if (objStruct instanceof Map mapStruct) {
            @SuppressWarnings("unchecked")
            List<Object>    lstSubNodes = new ArrayList<Object>(mapStruct.values());

            if (lstSubNodes.isEmpty())
                throw new IllegalArgumentException("Encountered internal structure node without sub-fields.");

            Object          objSbuNode0 = lstSubNodes.get(0);
            if (objSbuNode0 instanceof Map)
                return StructureUtility.extractFirstTerminalValue(objSbuNode0);
            else 
                return objSbuNode0;
            
        } else {
            return objStruct;
        }
    }

    /**
     * <p>
     * Extract the tree node (i.e., field) at the given index.
     * </p>
     * <p>
     * The returned value can be either an internal tree node or a terminal node, it depends upon the index
     * argument.  Thus, the returned value can be of type <code>Map&lt;String, Object&gt;</code> or some
     * scalar Java type such as <code>Boolean</code>, <code>Integer</code>, ..., <code>Double</code>, or
     * <code>String</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Tree structure indexes are given as lists of <code>Integer</code> objects where each integer value
     * selects for a tree branch.  Thus, index [<i>i</i><sub>0</sub>, <i>i</i><sub>1</sub>, ... ] selects the
     * <i>i</i><sub>0</sub> branch at the root node, the <i>i</i><sub>1</sub> branch at node <i>i</i><sub>0</sub>
     * of the first level, etc.
     * </p>
     * @param lstNodeIndex  index of desired node within the given tree structure
     * @param objStruct     a Java <code>Map&lt;String, Object&gt;</code> object containing a tree structure
     * 
     * @return  node at the given index within the given tree structure
     * 
     * @throws IndexOutOfBoundsException bad index for given structure, selected for node outside tree
     */
    public static Object    extractNodeAt(List<Integer> lstNodeIndex, Object objStruct) throws IndexOutOfBoundsException {

        // If object argument is a map use recursion
        if (objStruct instanceof Map mapStruct) {
            
            // Check for index boundary
            if (lstNodeIndex.size() == 0)
                return objStruct;
            
            // The index assumes field node contains sub-nodes
            @SuppressWarnings("unchecked")
            List<Object>    lstSubNodes = mapStruct.values().stream().toList();
            Integer         intIndex = lstNodeIndex.get(0);

            Object          objSubNode = lstSubNodes.get(intIndex);
            
            // If the index list has size > 1 need to keep going
            if (lstNodeIndex.size() > 1) {
                List<Integer>   lstSubNodeIndex = lstNodeIndex.subList(1, lstNodeIndex.size());
                
                return StructureUtility.extractNodeAt(lstSubNodeIndex, objSubNode);
                
            // Else - this is the final level 
            } else {
                return objSubNode;
            }
            
        } else {
            throw new IndexOutOfBoundsException("Bad index " + lstNodeIndex + ": Terminal node encountered before index recursion completed.");
            
        }
    }
    
    /**
     * <p>
     * Traverses the given tree structure and returns the node (field) with the given name.
     * </p>
     * <p>
     * This method defers to private method <code>{@link #extractNodeWithNameHelper(String, Object)}</code>
     * to recursively parse the given tree structure for the given field.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned object can be either an internal tree node of type <code>Map&lt;String, Object&gt;</code>
     * or a terminal node of Java scalar type.
     * </p>
     * 
     * @param strNodeName   name of desired structure node (field)
     * @param objStruct     a Java <code>Map&lt;String, Object&gt;</code> object containing a tree structure
     * 
     * @return  structure node (field) with given name
     *
     * @throws NoSuchElementException   a node with the given name was not found within the structure
     */
    public static Object    extractNodeWithName(String strNodeName, Object objStruct) throws NoSuchElementException {

        Object objValue = StructureUtility.extractNodeWithNameHelper(strNodeName, objStruct);
        
        if (objValue != null)
            return objValue;
        
        throw new NoSuchElementException("Unable to find node with name " + strNodeName);
    }
    
    /**
     * <p>
     * Extracts all terminal values of the given tree structure and returns them as a list of 
     * (name, value) pairs.
     * </p>
     * <p>
     * The method defers to the private method 
     * <code>{@link #extractTerminalValuesWithNameHelper(List, String, Object)}</code>
     * to recursively populate the returned value.  The list maintains the ordering of the 
     * terminal nodes as encountered during recursion.
     * </p>
     *  
     * @param mapStruct     a Java <code>Map&lt;String, Object&gt;</code> object containing a tree structure
     * 
     * @return  list of <code>{@link NameValuePair}</code> containing the terminal field names and values
     */
    public static List<NameValuePair> extractTerminalValuesWithName(Map<String, Object> mapStruct) {
        
        // The returned value
        List<NameValuePair>     lstNmValPairs = new LinkedList<>();
        
        // Root node name - empty to start
        String      strRootName = "";
        
        // Extract the terminal field values with field names using recursive method
        StructureUtility.extractTerminalValuesWithNameHelper(lstNmValPairs, strRootName, mapStruct);
        
        return lstNmValPairs;
    }
    
    /**
     * <p>
     * Extracts all terminal values of the given tree structure and returns them as a list of 
     * (index, value) pairs.
     * </p>
     * <p>
     * The method defers to the private method 
     * <code>{@link #extractTerminalValuesWithIndexHelper(List, String, Object)}</code>
     * to recursively populate the returned value.  The list maintains the ordering of the 
     * terminal nodes as encountered during recursion.
     * </p>
     *  
     * @param mapStruct     a Java <code>Map&lt;String, Object&gt;</code> object containing a tree structure
     * 
     * @return  list of <code>{@link IndexValuePair}</code> containing the terminal field indexes and values
     */
    public static List<IndexValuePair>  extractTerminalValuesWithIndex(Map<String, Object> mapStruct) {
        
        // The returned value
        List<IndexValuePair>        lstIndValPairs = new LinkedList<>();
        
        // Running node index used in recursion - Root node index is empty to start
        List<Integer>               lstRootIndex = new LinkedList<>();
        
        // Extract the terminal field values with structure indexes using recursive method
        StructureUtility.extractTerminalValuesWithIndexHelper(lstIndValPairs, lstRootIndex, mapStruct);
        
        return lstIndValPairs;
    }
    
    /**
     * <p>
     * Extracts all terminal values of the given tree structure and returns them as a list of 
     * (name, value) pairs.
     * </p>
     * <p>
     * This method is essentially equivalent to method <code>{@link #extractTerminalValuesWithName(Map)}</code>
     * except the returned container is a Java <code>Map</code> rather than a list of 
     * <code>{@link NameValuePair}</code> records.
     * </p>
     * 
     * @param objStruct a Java <code>Map&lt;String, Object&gt;</code> object containing a tree structure
     * 
     * @return  map containing the terminal field names and values as entries
     * 
     * @see #extractTerminalValuesWithName(Map)
     */
    public static Map<String, Object>   extractTerminalValuesWithNameAsMap(Object objStruct) {
        
        // The returned value
        Map<String, Object>     mapValues = new HashMap<>();
        
        // Root node field name - empty to start
        String      strFldNm = "";
        
        // Extract the terminal fields with field name, using recursion
        StructureUtility.extractTerminalValuesWithNameAsMapHelper(mapValues, strFldNm, objStruct);
        
        return mapValues;
    }
    
    /**
     * <p>
     * Extracts all terminal values of the given tree structure and returns them as a list of 
     * (index, value) pairs.
     * </p>
     * <p>
     * This method is essentially equivalent to method <code>{@link #extractTerminalValuesWithIndex(Map)}</code>
     * except the returned container is a Java <code>Map</code> rather than a list of 
     * <code>{@link IndexValuePair}</code> records.
     * </p>
     * 
     * @param objStruct a Java <code>Map&lt;String, Object&gt;</code> object containing a tree structure
     * 
     * @return  map containing the terminal field indexes and values as entries
     */
    public static Map<List<Integer>, Object> extractTerminalValuesWithIndexAsMap(Object objStruct) {
        
        // The returned value
        Map<List<Integer>, Object>  mapValues = new HashMap<>();
        
        // The running index used in recursion - empty for root node
        List<Integer>               lstIndex = new LinkedList<>();
        
        // Extract the terminal fields with index, using recursion
        StructureUtility.extractTerminalValuesWithIndexAsMapHelper(mapValues, lstIndex, objStruct);
        
        return mapValues;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Populates the argument <code>lstLstFldNms</code> from the structure node <code>objNode</code> using
     * recursion.
     * </p>
     * <p>
     * This method is called by <code>{@link #extractFieldNames(Object)}</code> to populate the list of string 
     * lists. This list contains one List&lt;String&gt; entry per structure level.
     * <p>
     * <h2>Usage</h2> 
     * For proper operation the method is called with the initial arguments
     * <code>
     * <pre>
     *      extractFieldNameHelper([], 1, objStruct)
     * </pre>
     * </code>
     * where <code>[]</code> indicates the empty list and <code>objStruct</code> is the root node of a tree
     * structure of type <code>Map&lt;String, Object&gt;</code>.
     * </p>
     * <p>
     * After initial invocation the method begins calling itself recursively on all sub-nodes encountered 
     * identified within the target node <code>objNode</code>.  An entry to the
     * <code>lstLstFldNms</code> is created for each sub-node and and each recursive call adds
     * an additional level.
     * </p>
     * 
     * @param lstLstFldNms  the list of all structure field names being generated (empty list upon initial invocation)
     * @param cntDepth      the current depth within the tree structure (1 upon initial invocation)
     * @param objNode       the current structure node (structure root on initial invocation)
     * 
     * @throws IllegalArgumentException encountered internal structure node (not terminal) without sub-fields
     */
    @SuppressWarnings("unchecked")
    private static void extractFieldNameHelper(List<List<String>> lstLstFldNms, int cntDepth, Object objNode) throws IllegalArgumentException {
        
        if (objNode instanceof Map mapStruct) {
            List<String>    lstNodeNames= mapStruct.keySet().stream().toList();
            List<Object>    lstSubNodes = mapStruct.values().stream().toList();
            if (lstSubNodes.isEmpty())
                throw new IllegalArgumentException("Encountered internal structure node without sub-fields.");

            if (lstLstFldNms.size() < cntDepth) 
                lstLstFldNms.add(new LinkedList<>(lstNodeNames));
            
            else {
                List<String>    lstLvlFldNms = lstLstFldNms.get(cntDepth-1);
                
                lstLvlFldNms.addAll(lstNodeNames);
            }
            
            // Now call recursively on all nodes
            for (Object objSubNode : lstSubNodes) 
                extractFieldNameHelper(lstLstFldNms, cntDepth+1, objSubNode);
        }
    }
    
    /**
     * <p>
     * Extracts the structure node with the given name from the given structure branch, or returns
     * </code>null</code> if not found.
     * </p>
     * <p>
     * This method is called by <code>{@link #extractNodeWithName(String, Object)}</code> on the
     * root node of the tree to search the entire tree structure.  It uses recursion to traverse
     * all branches of the given argument <code>objNode</code> which is assumed to be a structure
     * field (tree node).
     * </p>
     * 
     * @param strNodeName   name of the structure node (field name)
     * @param objNode       internal structure node (with recursion) or tree structure (initial invocation)
     * 
     * @return              structure node with given name, or <code>null</code> if not found.
     */
    private static Object extractNodeWithNameHelper(String strNodeName, Object objNode) {
        
        if (objNode instanceof Map mapStruct) {
            
            // Check this level for node with the given name and return it if so
            Object  objValue = mapStruct.get(strNodeName);
            
            if (objValue != null)
                return objValue;
            
            // Else search all nodes in this level using recursion
            @SuppressWarnings("unchecked")
            List<Object>    lstSubNodes = mapStruct.values().stream().toList();
            for (Object objSubNode : lstSubNodes) {
                objValue = StructureUtility.extractNodeWithNameHelper(strNodeName, objSubNode);

                // If there is a sub-node in with given name in this branch return it
                if (objValue != null)
                    return objValue;
            }
        } 
        
        // There were no nodes with given name in the target branch
        return null;
    }
    
    /**
     * <p>
     * Extracts the terminal values of the given tree structure node (with given name) populating the given list
     * of (name, value) pairs.
     * </p>
     * <p>
     * This method is called by <code>{@link #extractTerminalValuesWithName(Map)}</code> to populate the given
     * record list with (name, value) pairs for the terminal fields.  This is a recursive function that 
     * traverses all nodes within the given tree structure node <code>objNode</code>.  For proper operation
     * this method should be invoked initially with the following arguments
     * <ul>
     * <li><code>lstNmValPairs</code> = [] </li>
     * <li><code>strNodeName</code> = "" </li>
     * <li><code>objNode</code> = <code>Map&lt;String, Object&gt;</code> root of the tree structure
     * </ul>
     * An entry to <code>lstNmValPairs</code> is make whenever a terminal node is encountered.  The list should 
     * be populated after all recursion is complete. 
     * </p>
     * 
     * @param lstNmValPairs the container of terminal node (name, value) pairs (empty for 1st invocation)
     * @param strNodeName   the name of the current tree node (empty for 1st invocation)
     * @param objNode       the current tree node in recursion (tree structure root node for 1st invocation)
     */
    private static void extractTerminalValuesWithNameHelper(List<NameValuePair> lstNmValPairs, String strNodeName, Object objNode) {
    
        if (objNode instanceof Map mapStruct) {
            
            for (Object objSubNodeName : mapStruct.keySet()) {
                String  strSubNodeName = (String)objSubNodeName;
                Object  objSubNode = mapStruct.get(objSubNodeName);
                
                StructureUtility.extractTerminalValuesWithNameHelper(lstNmValPairs, strSubNodeName, objSubNode);
            }
            
        } else {
            NameValuePair recNmValPair = new NameValuePair(strNodeName, objNode);
            
            lstNmValPairs.add(recNmValPair);
        }
    }
    
    /**
     * <p>
     * Extracts the terminal values of the given tree structure node (with given name) populating the given list
     * of (index, value) pairs.
     * </p>
     * <p>
     * This method is called by <code>{@link #extractTerminalValuesWithIndex(Map)}</code> to populate the given
     * record list with (index, value) pairs for the terminal fields.  This is a recursive function that 
     * traverses all nodes within the given tree structure node <code>objNode</code>.  For proper operation
     * this method should be invoked initially with the following arguments
     * <ul>
     * <li><code>lstIndValPairs</code> = [] </li>
     * <li><code>lstNodeIndex</code> = [] </li>
     * <li><code>objNode</code> = <code>Map&lt;String, Object&gt;</code> root of the tree structure
     * </ul>
     * An entry to <code>lstIndValPairs</code> is make whenever a terminal node is encountered.  The list should 
     * be populated after all recursion is complete. 
     * </p>
     * 
     * @param lstIndValPairs the container of terminal node (name, value) pairs (empty for 1st invocation)
     * @param lstNodeIndex  the index of the current tree node (empty for 1st invocation)
     * @param objNode       the current tree node in recursion (tree structure root node for 1st invocation)
     */
    private static void extractTerminalValuesWithIndexHelper(List<IndexValuePair> lstIndValPairs, List<Integer> lstNodeIndex, Object objNode) {
        
        if (objNode instanceof Map mapStruct) {
            @SuppressWarnings("unchecked")
            List<Object>    lstSubNodes = mapStruct.values().stream().toList();
            Integer         intIndex = 0;
            
            for (Object objSubNode : lstSubNodes) {
                List<Integer>   lstSubNodeIndex = new LinkedList<>(lstNodeIndex);
                lstSubNodeIndex.add(intIndex);
                intIndex++;
                
                StructureUtility.extractTerminalValuesWithIndexHelper(lstIndValPairs, lstSubNodeIndex, objSubNode);
            }
            
        } else {
            IndexValuePair recIndValPair = new IndexValuePair(lstNodeIndex, objNode);
            
            lstIndValPairs.add(recIndValPair);
        }
        
    }
    
    /**
     * <p>
     * Extracts the terminal values of the given tree structure node (with given name) populating the given map
     * of (name, value) pairs.
     * </p>
     * <p>
     * This method is called by <code>{@link #extractTerminalValuesWithNameAsMap(Object)}</code> to populate
     * the <code>mapNmValPairs</code> argument with the (name, value) pairs of the terminal tree nodes.  
     * This method is essentially equivalent to 
     * <code>{@link #extractTerminalValuesWithNameHelper(List, String, Object)}</code>
     * only the target container is a Java <code>Map</code> rather than a <code>List</code> of 
     * <code>NameValuePair</code> records.
     * </p>
     * 
     * @param mapNmValPairs map containing (name, value) pairs for terminal structure nodes (empty for 1st invocation)
     * @param strNodeName   the name of the current tree node (empty for 1st invocation)    
     * @param objNode       the current tree node (<code>Map&lt;String, Object&gt;</code> root node for 1st invocation)
     */
    private static void extractTerminalValuesWithNameAsMapHelper(Map<String, Object> mapNmValPairs, String strNodeName, Object objNode) {
        
        if (objNode instanceof Map mapStruct) {  
            
            for (Object objName : mapStruct.keySet()) {
                String      strSubNodeName = (String)objName;
                Object      objSubNodeValue = mapStruct.get(objName);
                
                StructureUtility.extractTerminalValuesWithNameAsMapHelper(mapNmValPairs, strSubNodeName, objSubNodeValue);
            }
            
        } else {
            mapNmValPairs.put(strNodeName, objNode);
        }
    }
    
    /**
     * <p>
     * Extracts the terminal values of the given tree structure node (with given name) populating the given map
     * of (index, value) pairs.
     * </p>
     * <p>
     * This method is called by <code>{@link #extractTerminalValuesWithIndexAsMap(Object)}</code> to populate
     * the <code>mapIndValPairs</code> argument with the (index, value) pairs of the terminal tree nodes.  
     * This method is essentially equivalent to <code>{@link #extractTerminalValuesWithIndexHelper(List, String, Object)}</code>
     * only the target container is a Java <code>Map</code> rather than a <code>List</code> of 
     * <code>IndexValuePair</code> records.
     * </p>
     * @param mapIndValPairs    map containing (index, value) pairs for terminal structure nodes (empty for 1st invocation)
     * @param lstIndex          the index for the current tree node (empty for 1st invocation)
     * @param objNode           the current tree structure node (<code>Map&lt;String, Object&gt;</code> root node for 1st invocation)
     */
    private static void extractTerminalValuesWithIndexAsMapHelper(Map<List<Integer>, Object> mapIndValPairs, List<Integer> lstIndex, Object objNode) {
        
        if (objNode instanceof Map mapNode) {
            @SuppressWarnings("unchecked")
            List<Object>    lstSubNodes = mapNode.values().stream().toList();
            Integer         intIndex = 0;
            
            for (Object objSubNode : lstSubNodes) {
                List<Integer>   lstSubIndex = new LinkedList<>(lstIndex);
                lstSubIndex.add(intIndex);
                intIndex++;
                
                StructureUtility.extractTerminalValuesWithIndexAsMapHelper(mapIndValPairs, lstSubIndex, objSubNode);
            }
            
        } else {
            mapIndValPairs.put(lstIndex, objNode);
        }
    }
    
    /**
     * <p>
     * Prevent construction of <code>StructureUtility</code> instances.
     * </p>
     *
     */
    private StructureUtility() {
    }

    
    //
    // TODO Old - Remove
    //
    
//    @Deprecated
//    @SuppressWarnings("unchecked")
//    public static int   terminalFieldCount(Map<String, Object> mapStruct) throws IllegalArgumentException {
//        int     cntFlds = 0;
//        
//        for (Map.Entry<String, Object> entry : mapStruct.entrySet()) {
//            Object  objNode = entry.getValue();
//            
//            if (objNode instanceof Map map) {
//
//                try {
//                    cntFlds += terminalFieldCount(map);
//                    
//                } catch (ClassCastException e) {
//                    throw new IllegalArgumentException("Internal field was not a Map<String, Object> instance", e);
//                }
//                
//            } else {
//                cntFlds++;
//            }
//        }
//        
//        return cntFlds;
//    }
//    
//    @Deprecated
//    @SuppressWarnings("unchecked")
//    public static int   totalNodeCount(Map<String, Object> mapStruct) {
//        int     cntNodes = 1;
//        
//        for (Map.Entry<String, Object> entry : mapStruct.entrySet()) {
//            cntNodes++;
//            
//            Object  objNode = entry.getValue();
//            if (objNode instanceof Map map) {
//                cntNodes += totalNodeCount(map);
//            }
//        }
//        
//        return cntNodes;
//    }
//    
//    @Deprecated
//    public static boolean   isElementValueOfType(Map<String, Object> mapStruct, ScalarType enmType) {
//        
//        List<Object>    lstFields = mapStruct.values().stream().toList();
//        
//        if (lstFields.isEmpty())
//            throw new IllegalArgumentException("Encountered node with no fields.");
//        
//        Object          objVal1 = lstFields.get(0);
//        if (objVal1 instanceof Map map)
//            return StructureUtility.isElementValueOfType(map, enmType);
//        else
//            return enmType.isAssignable(objVal1);
//    }
    
}
