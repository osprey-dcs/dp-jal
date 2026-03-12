/*
 * Project: dp-data-simulator
 * File:	StructureIndexGenerator.java
 * Package: com.ospreydcs.dp.datasim.utility
 * Type: 	StructureIndexGenerator
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
 * @since May 23, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.jal.tools.common.datagen.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Utility class for creating structure node indexes appropriate for the Data Platform 
 * <code>Structure</code> message.
 * </p>
 * <p>
 * Specifically, the indexes are particular to tree structures supported by the <code>Structure</code>
 * Protobuf message in <em>common.proto</em>.  The class <code>{@link StructureFactory}</code> will
 * generate the Java equivalent of the <code>Structure</code> message for the symmetric, tree structure 
 * case.  Those structures can be converted to <code>Structure</code> messages using the utility
 * <code>ProtoMsg</code> in the the Data Platform Java Client API Library.
 * </p> 
 * <h2>Structure Indexes</h2>
 * <p>
 * We represent structure indexes as a list of integer values, specifically, as a Java
 * <code>List&lt;Integer&gt;</code> instance.  Thus, an index <i>i</i> into a tree structure has as
 * many components as there are levels within the structure.  We have
 * <pre>
 *   <i>i</i> = [<i>i</i><sub>0</sub>, <i>i</i><sub>1</sub>, <i>i</i><sub>2</sub>, ... ]
 * </pre>
 * where <i>i</i><sub>0</sub> is the tree branch off the root node, <i>i</i><sub>1</sub> is the branch off
 * the node at level 1, etc.  An example indexing scheme is shown below where an additional index digit
 * is added for each depth level down the tree. 
 * <pre>
 *                      0
 *                    / | \
 *                   /  |  \
 *             [0]  /   |   \ [2]
 *                 / [1]|    \
 *                o     o     o  
 *              /  \          | \ 
 *        [0,0]/    \[0,1]    |  \
 *            /      \   [2,0]|   \[2,1]
 *           o        o       0    o
 * </pre>
 * </p>
 * <h2>Index Sets</h2>
 * <p>
 * The class generates index sets only for <em>symmetric</em> tree structures, which is the structure type
 * created by <code>StructureFactory</code>.  More specifically, the class generates the index sets for the
 * <em>terminal nodes</em> of the symmetric tree structures created by <code>StructureFactory</code>.
 * The terminal nodes of structures created by <code>StructureFactory</code> contain scalar values of type 
 * specified at construction.  These are the only nodes within the tree structure that contain actual values.
 * All internal nodes contain <code>Structure</code> messages contain structure branches.
 * </p>
 *                  
 * @author Christopher K. Allen
 * @since May 23, 2024
 *
 */
public class StructureIndexGenerator implements Iterable<List<Integer>> {

    
    //
    // Internal Types
    //
    
    /**
     * <p>
     * Enumeration of the node types within the target tree structure to be iterated over while
     * generating indexes.
     * </p>
     */
    public static enum NodeSet {
        
        /** Iterate (generating indexes) over internal tree structure nodes (i.e., non-terminal). */
        INTERNAL,
        
        /** Iterate (generating indexes) over terminal tree structure nodes. */
        TERMINAL,
        
        /** Iterate (generating indexes) over all tree structure nodes. */
        ALL;
        
        
        /**
         * <p>
         * Compute and return the node count of a symmetric tree structure with the given properties
         * for this enumeration constant.
         * </p>
         * 
         * @param depth     symmetric tree structure depth > 0 (0 is root node)
         * @param fanout    symmetric tree structure fan out > 1 (number of sub-nodes per node)
         * 
         * @return  number of nodes in the enumerated node set for a symmetric tree structure
         */
        public int computeNodeCount(int depth, int fanout) {
            return switch (this) {
            case INTERNAL -> StructureUtility.computeInternalNodeCount(depth, fanout);
            case TERMINAL -> StructureUtility.computeTerminalNodeCount(depth, fanout);
            case ALL -> StructureUtility.computeTotalNodeCount(depth, fanout);
            };
        }
    };
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new instance of <code>StructureIndexGenerator</code> ready for symmetric tree structure index 
     * generation and iteration.
     * </p>
     * <p>
     * The returned instance will iterate through all terminal node index of a symmetric tree structure with the
     * given parameters (this is the default when no node set is provided).  
     * Index iterations can be restarted using <code>{@link #resetIndexIteration()}</code>
     * otherwise attempting to iterate beyond the number of terminal node indexes 
     * <code>{@link #getTerminalNodeCount()}</code> will throw an exception.
     * </p> 
     * 
     * @param depth     tree structure depth
     * @param fanOut    structure node fan out (number of sub-nodes per node)
     * 
     * @return  a new symmetric tree structure terminal node index generator ready for index iteration
     */
    public static StructureIndexGenerator   from(int depth, int fanOut) {
        return new StructureIndexGenerator(depth, fanOut);
    }
    
    /**
     * <p>
     * Creates a new instance of <code>StructureIndexGenerator</code> ready for symmetric tree structure index 
     * generation and iteration.
     * </p>
     * <p>
     * The returned instance will iterate through all terminal node index of a symmetric tree structure with the
     * given parameters (this is the default when no node set is provided).  
     * Index iterations can be restarted using <code>{@link #resetIndexIteration()}</code>
     * otherwise attempting to iterate beyond the number of terminal node indexes 
     * <code>{@link #getTerminalNodeCount()}</code> will throw an exception.
     * </p> 
     * 
     * @param depth         tree structure depth
     * @param fanOut        structure node fan out (number of sub-nodes per node)
     * @param enmNodeSet    the nodes within the structure to iterate over
     * 
     * @return  a new symmetric tree structure terminal node index generator ready for index iteration
     */
    public static StructureIndexGenerator   from(int depth, int fanout, NodeSet enmNodeSet) {
        return new StructureIndexGenerator(depth, fanout, enmNodeSet);
    }
    
    
    //
    // Configuration
    //
    
    /** The depth of the target tree structure */
    private final int       intDepth;
    
    /** The number of sub-nodes per node of target tree structure */
    private final int       intFanOut;
    
    /** The number of internal nodes within the target structure */
    private final int       cntNodesInternal;
    
    /** The number of terminal nodes within the target structure */
    private final int       cntNodesTerminal;
    
    /** The number of total nodes, including internal nodes, root node, and terminal nodes */
    private final int       cntNodesTotal;
    
    
    //
    //  Variables
    //
    
    /** The nodes within the tree structure for which indexes are being generated */
    private NodeSet     enmNodeSet;

    
    /** Tree structure index number with current index count */
    private int         ctrIndexCurr;
    
//    /** Tree structure index within current terminal node index count */
//    private int[]       arrTermIndexCurr;         
    
    /** Tree structure index with current total node index count */
    private List<Integer> lstTotalIndexCurr;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>StructureIndexGenerator</code> ready for index generation.
     * </p>
     * <p>
     * This constructor defaults to iterating over the terminal node set within the given tree structure.
     * </p>
     *
     * @param depth         tree structure depth
     * @param fanOut        structure node fan out (number of sub-nodes per node)
     * 
     * @throws  IllegalArgumentException depth must be greater than 0, fan out must be greater than 1
     */
    public StructureIndexGenerator(int depth, int fanOut) throws IllegalArgumentException {
        this(depth, fanOut, NodeSet.TERMINAL);
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>StructureIndexGenerator</code> ready for index generation.
     * </p>
     *
     * @param depth         tree structure depth
     * @param fanOut        structure node fan out (number of sub-nodes per node)
     * @param enmNodeSet    the nodes within the tree to iterator over when generating indexes
     * 
     * @throws  IllegalArgumentException depth must be greater than 0, fan out must be greater than 1
     */
    public StructureIndexGenerator(int depth, int fanOut, NodeSet enmNodeSet) throws IllegalArgumentException {
        
        // Check Arguments
        if (depth < 1)
            throw new IllegalArgumentException("Tree structure depth is less than 1.");
        if (fanOut < 2)
            throw new IllegalArgumentException("Tree structure fan out is less than 2.");
        
        this.intDepth = depth;
        this.intFanOut = fanOut;
        this.enmNodeSet = enmNodeSet;

//        this.cntNodesInternal = StructureUtility.computeInternalNodeCount(depth, fanOut);
//        this.cntNodesTerminal = StructureUtility.computeTerminalNodeCount(depth, fanOut);
//        this.cntNodesTotal = StructureUtility.computeTotalNodeCount(depth, fanOut);
        
        this.cntNodesInternal = NodeSet.INTERNAL.computeNodeCount(depth, fanOut);
        this.cntNodesTerminal = NodeSet.TERMINAL.computeNodeCount(depth, fanOut);
        this.cntNodesTotal = NodeSet.ALL.computeNodeCount(depth, fanOut);
        
        this.ctrIndexCurr = 0;
//        this.arrTermIndexCurr = this.initTermIndexCounter();
//        this.lstTotalIndexCurr = this.initTotalIndexCounter();
        this.lstTotalIndexCurr = this.initIndexCounter();
    }
    
    
    //
    // Configuration
    //

    /**
     * <p>
     * Returns the depth of the target tree structure (i.e., number of structure levels).
     * </p>
     * <p>
     * Note that this value is the number of digits in terminal field indexes produces by this
     * index generator instance.
     * </p>
     * 
     * @return  number of levels for tree structure
     */
    public int  getTreeDepth() {
        return this.intDepth;
    }
    
    /**
     * <p>
     * Returns the number of sub-nodes for each internal node of the targeted, symmetric, tree structure.
     * </p>
     * 
     * @return  the tree node fan out (i.e., number of sub-nodes of each internal node)
     */
    public int  getTreeNodeFanOut() {
        return this.intFanOut;
    }
    
    /**
     * <p>
     * Returns the node set of the target tree structure for which this instance is iterating.
     * </p>
     *  
     * @return  <code>NodeSet</code> enumeration constant specifying the structure nodes being iterated
     */
    public NodeSet  getNodeSet() {
        return this.enmNodeSet;
    }
    
    /**
     * <p>
     * Returns the number of terminal nodes within the symmetric tree structure identified at construction.
     * </p>
     * <p>
     * Note that the returned value is the number of tree structure indexes generated by this instance when used 
     * as an iterator.  It is the number of nodes containing scalar field value for those structures produced
     * by <code>{@link StructureFactory}</code>.
     * </p> 
     *  
     * @return  the number of terminal nodes within the tree structure identified at construction
     */
    public int  getTerminalNodeCount() {
        return this.cntNodesTerminal;
    }
    
    /**
     * <p>
     * Returns the total number of nodes within the symmetric, tree structure identified at construction.
     * </p>
     * <p>
     * The returned value is the total node count including internal nodes, and the root node.
     * </p>
     * 
     * @return  total node count of target symmetric, tree structure 
     */
    public int  getTotalNodeCount() {
        return this.cntNodesTotal;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Determines whether or not there are more tree structure indexes left in the current iteration.
     * </p>
     * 
     * @return  <code>true</code> there are more structure indexes available,
     *          <code>false</code> otherwise
     */
    public boolean      hasNext() {
        return this.ctrIndexCurr < this.totalIndexCount();
    }
    
    /**
     * <p>
     * Returns the next terminal node index in the full terminal nodex index set.
     * </p>
     * <p>
     * After construction this method may be called exactly <code>{@link #getTerminalNodeCount()}</code> times.
     * Calling the method beyond that will thrown an exception.
     * Optionally, one may use the <code>{@link #hasNext()}</code> method to determine if more
     * tree structure indexes are available.
     * </p>
     * <p>
     * <h2>Restarting</h2>
     * The method <code>{@link #resetIndexIteration()}</code> can be invoked to restart this index
     * creation process.  After the above method is called this method may again be called exactly 
     * <code>{@link #getTerminalNodeCount()}</code> times.  
     * <p>
     * 
     * @return  next terminal node index in the index sequence
     * 
     * @throws NoSuchElementException   called when no more indexes are available
     */
    public List<Integer>    nextIndex() throws NoSuchElementException {
        
        // Check current state
        if (this.ctrIndexCurr >= this.totalIndexCount()) 
            throw new NoSuchElementException(JavaRuntime.getQualifiedMethodNameSimple() + " - No more indexes available.");
        
        // Create the current tree structure index and increment index counter
        List<Integer>   lstIndex = this.createCurrentNodexIndex();
        this.ctrIndexCurr++;
        
        // If there are more indexes increment the current counter
        if (this.ctrIndexCurr < this.totalIndexCount()) {
            
            try {
//                this.incrCurrentIndex();
                this.incrCurrentIndexTotalNodes();
                
            } catch (IllegalStateException e) {
                throw new NoSuchElementException(JavaRuntime.getQualifiedMethodNameSimple() + " - Internal error: Unable to increment index.", e);
                
            }
        }
        
        // Return the index
        return lstIndex;
    }
    
    /**
     * <p>
     * Creates and returns a collection of all terminal node indexes for the target tree structure. 
     * </p>
     * <p>
     * This method first invokes <code>{@link #resetIndexIteration()}</code> to initialize the
     * internal index counting operation.  The method <code>{@link #nextIndex()}</code>
     * is then called while <code>{@link #hasNext()}</code> is <code>true</code> to populate the returned
     * list of index objects.
     * </p>
     * 
     * @return  collection all terminal node indexes for the target tree structure
     */
    public Collection<List<Integer>>  allIndexes() {
        
        // Returned object
        List<List<Integer>>     lstIndexes = new ArrayList<>(this.totalIndexCount());

        // Reset index counter then populate index container
        this.resetIndexIteration();
        while (this.hasNext()) {
            List<Integer>     lstIndex = this.nextIndex();
            
            lstIndexes.add(lstIndex);
        }
        
        return lstIndexes;
    }
    
    /**
     * <p>
     * Resets the index generator to create a new set of indexes for the target tree structure.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * After creation the current instance is ready to generate node indexes.  To generate
     * additional sets of indexes call the method to restart this instance.
     * </p>
     */
    public void resetIndexIteration() {
        this.resetIndexIteration(this.enmNodeSet);
    }
    
    /**
     * <p>
     * Resets the index generator to create a new set of indexes for the target tree structure across
     * the given node set.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * After creation the current instance is ready to generate terminal node indexes.  To generate
     * additional sets of indexes call the method to restart this instance.
     * </p>
     */
    public void resetIndexIteration(NodeSet enmNodeSet) {
        this.enmNodeSet = enmNodeSet;
        this.ctrIndexCurr = 0;
//        this.arrTermIndexCurr = this.initTermIndexCounter();
//        this.lstTotalIndexCurr = this.initTotalIndexCounter();
        this.lstTotalIndexCurr = this.initIndexCounter();
    }
    
    
    //
    // Iterable<List<Integer>> Interface
    //
    
    /**
     * <p>
     * Creates and returns an <code>{@link Iterator}</code> interface that iterates over all
     * terminal node indexes in this instance.
     * </p>
     * <p>
     * An anonymous <code>Iterator&lt;List&lt;Integer&gt;;gt;</code> interface is created which defers all 
     * operations to this instance.  This method may be called <em>only once</em> per index
     * iteration; that is, unless <code>{@link #resetIndexIteration()}</code> is invoked.
     * </p>
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<List<Integer>> iterator() {
        
        // Create an anonymous Iterator interface and return it
        Iterator<List<Integer>>     iter = new Iterator<List<Integer>>() {

            @Override
            public boolean hasNext() {
                return StructureIndexGenerator.this.hasNext();
            }

            @Override
            public List<Integer>    next() {
                return StructureIndexGenerator.this.nextIndex();
            }
        };
        
        return iter;
    }

    
    //
    // Support Methods
    //
    
//    /**
//     * <p>
//     * Creates the initial tree structure index for terminal node iteration.
//     * </p>
//     * 
//     * @return  new initial terminal node tree structure index
//     */
//    private int[]   initTermIndexCounter() {
//        
//        // The returned value
//        int[]       arrIndexInit = new int[this.intDepth];
//        
//        for (int iLvl=0; iLvl<this.intDepth; iLvl++) {
//            
//            arrIndexInit[iLvl] = 0;
//        }
//        
//        return arrIndexInit;
//    };
    
//    /**
//     * <p>
//     * Create the initial tree structure index for total node iteration.
//     * </p>
//     * 
//     * @return  new initial total node tree structure index
//     */
//    private List<Integer>   initTotalIndexCounter() {
//        
//        // The returned value
//        List<Integer>   lstIndexInit = new ArrayList<>(this.intDepth);
//        
////        // Add the first index
////        lstIndexInit.add(0);
//        
//        return lstIndexInit;
//    }
    
    /**
     * <p>
     * Creates the tree structure index counter and initializes according to node set
     * being iterated.
     * </p>
     * 
     * @return  new, initialized tree index counter, ready for iteration
     */
    private List<Integer>   initIndexCounter() {
        
        // The returned value
        List<Integer>   lstInitIndex = new ArrayList<>(this.intDepth);

        // For the case of terminal nodes need to add all index position
        if (this.enmNodeSet == NodeSet.TERMINAL) {
            for (int iLvl=0; iLvl<this.intDepth; iLvl++) {
                
                lstInitIndex.add(0);
            }
        }
        
        return lstInitIndex;
    }
    
//    /**
//     * <p>
//     * Creates a tree structure index from the current internal terminal node index counter.
//     * </p>
//     * <p>
//     * Converts the value within the primitive integer array <code>{@link #arrTermIndexCurr}</code>
//     * into Java <code>Integer</code> objects which are used to populate the returned ordered
//     * list.
//     * </p>
//     *  
//     * @return  the current value of the internal terminal node index counter as a list of integers
//     */
//    private List<Integer> createTerminalNodeIndex() {
//        
//        // Returned value
//        List<Integer>   lstIndex = new ArrayList<>(this.intDepth);
//        
//        for (int iLvl=0; iLvl<this.intDepth; iLvl++) {
//            Integer     intIndex = this.arrTermIndexCurr[iLvl];
//            
//            lstIndex.add(intIndex);
//        }
//        
//        return lstIndex;
//    }
    
    /**
     * <p>
     * Returns the correct, current tree structure internal index regardless of which node set is being
     * iterated.
     * </p>
     * <p>
     * The method checks the value of <code>{@link #enmNodeSet}</code> to determine the node set being iterated
     * and creates a new returned value accordingly.  
     * <ul>
     * <li><code>{@link NodeSet#INTERNAL}</code> - copies and returns <code>{@link #lstTotalIndexCurr}</code>.</li>
     * <li><code>{@link NodeSet#TERMINAL}</code> - calls <code>{@link #createTerminalNodeIndex()}</code>.</li>
     * <li><code>{@link NodeSet#ALL}</code> - copies and returns <code>{@link #lstTotalIndexCurr}</code>.</li>
     * </ul>
     * </p>
     *  
     * @return  the current node index value appropriate for node set being iterated 
     */
    private List<Integer>   createCurrentNodexIndex() {
        
//        if (this.enmNodeSet == NodeSet.TERMINAL) {
//
//            
//            return this.createTerminalNodeIndex();
//        }
        
        // Clone the current total node index and return
        List<Integer>   lstIndex = this.lstTotalIndexCurr
                .stream()
                .sequential()
                .<Integer>map(i -> Integer.valueOf(i))
                .toList();
        
        return lstIndex;
    }
    
//    /**
//     * <p>
//     * Increments the current internal tree index according to the node set being iterated.
//     * </p>
//     * <p>
//     * Calls the following according to the prescribed node set:
//     * <ul>
//     * <li><code>{@link NodeSet#INTERNAL}</code> - calls <code>{@link #incrCurrentIndexTotalNodes()}</code></li>.
//     * <li><code>{@link NodeSet#TERMINAL}</code> - calls <code>{@link #incrCurrentIndexTerminalNodes(int)}</code></li>.
//     * <li><code>{@link NodeSet#ALL}</code> - calls <code>{@link #incrCurrentIndexTotalNodes()}</code></li>.
//     * </ul>
//     * </p>
//     */
//    private void incrCurrentIndex() {
//        
//        switch (this.enmNodeSet) {
//        
//        case INTERNAL:
//            this.incrCurrentIndexTotalNodes();
//            break;
//        case TERMINAL:
//            this.incrCurrentIndexTerminalNodes(this.intDepth - 1);
//            break;
//        case ALL:
//            this.incrCurrentIndexTotalNodes();
//            break;
//        }
//    }
    
//    /**
//     * <p>
//     * Increments the current value of the internal terminal node index when iterating terminal nodes.
//     * </p>
//     * <p>
//     * The value within <code>{@link #arrTermIndexCurr}</code> is incremented according to the
//     * configuration of the tree structure.  This is a recursive function and should be
//     * called with argument equal to <code>{@link #intDepth} - 1</code>.
//     * </p>
//     * 
//     * @param iDim  tree structure index array dimension ({@link #intDepth} - 1) 
//     * 
//     * @throws IllegalStateException    index overflow exception
//     */
//    private void incrCurrentIndexTerminalNodes(int iDim) throws IllegalStateException {
//        Integer     indVal = this.arrTermIndexCurr[iDim];
//        Integer     indMax = this.intFanOut - 1;
//        
//        if (indVal < indMax) {
//            this.arrTermIndexCurr[iDim]++;
//            
//            return;
//            
//        } else {
//            
//            // Check if we hit out of bounds
//            if (iDim == 0)
//                throw new IllegalStateException(JavaRuntime.getQualifiedCallerNameSimple() + " - Tree index overflow while incrementing at index " + this.arrTermIndexCurr);
//            
//            this.arrTermIndexCurr[iDim] = 0;
//            this.incrCurrentIndexTerminalNodes(iDim - 1);
//        }
//    }
    
    /**
     * <p>
     * Increments the current value of the internal total node index for iterating total nodes or internal nodes.
     * </p>
     * </p>
     * This method is the entry point into the recursive function
     * <code>{@link #incrCurrentIndexTerminalNodes(int)}</code> which modifies the current index, incrementing
     * tree level indexes as necessary, or adding additional tree depth when necessary.
     * This method checks for exception conditions, such as an empty
     * total node index counter (upon first call), and for total node index size > than the tree structure
     * depth (indicating index overflow exception).
     * </p>
     * <p>
     * The target of this operation is the values within the <code>{@link #lstTotalIndexCurr}</code> container.
     * They are incremented according to the configuration of the symmetric tree structure.
     * </p>  
     *  
     * @throws IllegalStateException    index overflow exception, index size > tree depth
     */
    private void incrCurrentIndexTotalNodes() throws IllegalStateException {

        // Maximum index value
        final int indMax = this.intFanOut - 1;
        
        // Check for overflow
        if (this.lstTotalIndexCurr.size() > this.intDepth)
            throw new IllegalStateException(JavaRuntime.getQualifiedMethodNameSimple() + " - Tree index overflow while incrementing at index " + this.lstTotalIndexCurr);
        
        // First call - special case
        if (this.lstTotalIndexCurr.size() == 0) {
            this.lstTotalIndexCurr.add(0);
            
            return;
        }
        
        // Get the index size, convert to current tree depth
        int szIndex = this.lstTotalIndexCurr.size();
        int iLevel = szIndex - 1;
        
        // Get the current index value and compare to maximum
        int indVal = this.lstTotalIndexCurr.get(iLevel);
        
        if (indVal < indMax) {
            indVal++;       
            this.lstTotalIndexCurr.set(iLevel, indVal);
            
            return;
        }

        // Must increment other index values in the list (recursively process them)
        this.incrCurrentIndexTotalNodes(iLevel);
//        this.incrCurrentIndexTotalNodes(iLevel - 1);
    }
    
    /**
     * <p>
     * Recursively processes the internal total node index for increment at the given tree level.
     * </p>
     * <p>
     * This method is called by <code>{@link #incrCurrentIndexTotalNodes()}</code> to process a total
     * node index increment beyond the fan out maximum.  This method continues to call itself until
     * all affected indexes for all tree depths have been processed.
     * </p> 
     * 
     * @param iLevel    current tree level, or index within the total node index list
     * 
     */
    private void incrCurrentIndexTotalNodes(int iLevel) {

        // Maximum index value
        final int indMax = this.intFanOut - 1;
        
        // Exceptional case - need to add new level
        if (iLevel <= 0) {
            for (int i=0; i<this.lstTotalIndexCurr.size(); i++)
                this.lstTotalIndexCurr.set(i, 0);
            
            this.lstTotalIndexCurr.add(0);
            
            return;
        }
        
//        // Get the index at the given tree level and compare to max index value
//        int indValLvl = this.lstTotalIndexCurr.get(iLevel);
//        
//        // This should not occur - should be taken care of already
//        if (indValLvl < indMax) {
//            indValLvl++;
//            this.lstTotalIndexCurr.set(iLevel, indValLvl);
//            
//            return;
//        }

        // Inspect structure index 1 level up
        int iLevelUp = iLevel - 1;
        int indValLvlUp = this.lstTotalIndexCurr.get(iLevelUp);
        if (indValLvlUp < indMax) {
            
            // Increment the index one level up
            indValLvlUp++;
            this.lstTotalIndexCurr.set(iLevelUp, indValLvlUp);

            // Zero out all indexes below
            for (int i=iLevel; i<this.lstTotalIndexCurr.size(); i++)
                this.lstTotalIndexCurr.set(i, 0);

//            indValLvl = 0;
            
            return;
        }
                

        // Must process the next higher tree level
        this.incrCurrentIndexTotalNodes(iLevelUp);
    }
    
    /**
     * <p>
     * Returns the total number of indexes to be generated according to the node set being iterated.
     * </p>
     * 
     * @return  total number of nodes within the current node set
     */
    private int totalIndexCount() {
        
        return switch (this.enmNodeSet) {
        case INTERNAL -> this.cntNodesInternal;
        case TERMINAL -> this.cntNodesTerminal;
        case ALL -> this.cntNodesTotal;
        };
    }
}
