/*
 * Project: dp-data-simulator
 * File:	StructureIndexGeneratorTest.java
 * Package: com.ospreydcs.dp.datasim.utility
 * Type: 	StructureIndexGeneratorTest
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
 * @since May 24, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.jal.tools.common.datagen.utility;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.jal.util.JavaRuntime;
import com.ospreydcs.dp.jal.tools.common.datagen.utility.StructureIndexGenerator.NodeSet;


/**
 * <p>
 * JUnit test cases for class <code>{@link #testStructureIndexGenerator()}</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 14, 2025
 *
 */
public class StructureIndexGeneratorTest {

    
    //
    // Test Fixture
    //
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    
    //
    // Test Cases
    //
    
    /**
     * Test method for {@link StructureIndexGenerator#from(int, int)}.
     */
    @Test
    public final void testFromIntInt() {
        
        // Parameters
        final int       intDepth = 2;
        final int       intFanOut = 3;
        final int       cntTermNodes = StructureUtility.computeTerminalNodeCount(intDepth, intFanOut);
        final int       cntTotalNodes = StructureUtility.computeTotalNodeCount(intDepth, intFanOut);
        
        StructureIndexGenerator genIndexes = StructureIndexGenerator.from(intDepth, intFanOut);
        
        Assert.assertEquals(intDepth, genIndexes.getTreeDepth());
        Assert.assertEquals(intFanOut, genIndexes.getTreeNodeFanOut());
        Assert.assertEquals(NodeSet.TERMINAL, genIndexes.getNodeSet());
        Assert.assertEquals(cntTermNodes, genIndexes.getTerminalNodeCount());
        Assert.assertEquals(cntTotalNodes, genIndexes.getTotalNodeCount());
    }
    
    /**
     * Test method for {@link StructureIndexGenerator#from(int, int, NodeSet)}.
     */
    @Test
    public final void testFromIntIntNodeSet() {
        
        // Parameters
        final int       intDepth = 2;
        final int       intFanOut = 3;
        final NodeSet   enmNodeSet = NodeSet.ALL;
        final int       cntTermNodes = StructureUtility.computeTerminalNodeCount(intDepth, intFanOut);
        final int       cntTotalNodes = StructureUtility.computeTotalNodeCount(intDepth, intFanOut);
        
        StructureIndexGenerator genIndexes = new StructureIndexGenerator(intDepth, intFanOut, enmNodeSet);
        
        Assert.assertEquals(intDepth, genIndexes.getTreeDepth());
        Assert.assertEquals(intFanOut, genIndexes.getTreeNodeFanOut());
        Assert.assertEquals(enmNodeSet, genIndexes.getNodeSet());
        Assert.assertEquals(cntTermNodes, genIndexes.getTerminalNodeCount());
        Assert.assertEquals(cntTotalNodes, genIndexes.getTotalNodeCount());
    }
    
    /**
     * Test method for {@link com.ospreydcs.dp.datasim.utility.StructureIndexGenerator#StructureIndexGenerator(int, int)}.
     */
    @Test
    public final void testStructureIndexGenerator() {
        
        // Parameters
        final int       intDepth = 2;
        final int       intFanOut = 3;
        final int       cntTermNodes = StructureUtility.computeTerminalNodeCount(intDepth, intFanOut);
        final int       cntTotalNodes = StructureUtility.computeTotalNodeCount(intDepth, intFanOut);
        
        StructureIndexGenerator genIndexes = new StructureIndexGenerator(intDepth, intFanOut);
        
        Assert.assertEquals(intDepth, genIndexes.getTreeDepth());
        Assert.assertEquals(intFanOut, genIndexes.getTreeNodeFanOut());
        Assert.assertEquals(NodeSet.TERMINAL, genIndexes.getNodeSet());
        Assert.assertEquals(cntTermNodes, genIndexes.getTerminalNodeCount());
        Assert.assertEquals(cntTotalNodes, genIndexes.getTotalNodeCount());
    }
    
    /**
     *  Test method for {@link StructureIndexGenerator#StructureIndexGenerator(int, int, com.ospreydcs.dp.datasim.utility.StructureIndexGenerator.NodeSet)}
     */
    @Test
    public final void testStructureIndexGeneratorIntIntNodeSet() {
        
        // Parameters
        final int       intDepth = 2;
        final int       intFanOut = 3;
        final NodeSet   enmNodeSet = NodeSet.ALL;
        final int       cntTermNodes = StructureUtility.computeTerminalNodeCount(intDepth, intFanOut);
        final int       cntTotalNodes = StructureUtility.computeTotalNodeCount(intDepth, intFanOut);
        
        StructureIndexGenerator genIndexes = new StructureIndexGenerator(intDepth, intFanOut, enmNodeSet);
        
        Assert.assertEquals(intDepth, genIndexes.getTreeDepth());
        Assert.assertEquals(intFanOut, genIndexes.getTreeNodeFanOut());
        Assert.assertEquals(enmNodeSet, genIndexes.getNodeSet());
        Assert.assertEquals(cntTermNodes, genIndexes.getTerminalNodeCount());
        Assert.assertEquals(cntTotalNodes, genIndexes.getTotalNodeCount());
    }

//    /**
//     * Test method for {@link com.ospreydcs.dp.datasim.utility.StructureIndexGenerator#getTreeDepth()}.
//     */
//    @Test
//    public final void testGetTreeDepth() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.datasim.utility.StructureIndexGenerator#getTreeNodeFanOut()}.
//     */
//    @Test
//    public final void testGetTreeNodeFanOut() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.datasim.utility.StructureIndexGenerator#getTerminalNodeCount()}.
//     */
//    @Test
//    public final void testTerminalNodeCount() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link com.ospreydcs.dp.datasim.utility.StructureIndexGenerator#getTotalNodeCount()}.
//     */
//    @Test
//    public final void testTotalNodeCount() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for {@link com.ospreydcs.dp.datasim.utility.StructureIndexGenerator#hasNext()}.
     */
    @Test
    public final void testHasNext() {
        
        // Parameters
        final int       intDepth = 3;
        final int       intFanOut = 3;
        final NodeSet   enmNodeSet = NodeSet.ALL;
        final int       cntTermNodes = StructureUtility.computeTerminalNodeCount(intDepth, intFanOut);
        final int       cntTotalNodes = StructureUtility.computeTotalNodeCount(intDepth, intFanOut);
        
        StructureIndexGenerator genIndexes = new StructureIndexGenerator(intDepth, intFanOut, enmNodeSet);
        
        Assert.assertEquals(intDepth, genIndexes.getTreeDepth());
        Assert.assertEquals(intFanOut, genIndexes.getTreeNodeFanOut());
        Assert.assertEquals(enmNodeSet, genIndexes.getNodeSet());
        Assert.assertEquals(cntTermNodes, genIndexes.getTerminalNodeCount());
        Assert.assertEquals(cntTotalNodes, genIndexes.getTotalNodeCount());
        
        Assert.assertTrue(genIndexes.hasNext());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.datasim.utility.StructureIndexGenerator#nextIndex()}.
     */
    @Test
    public final void testNextIndex() {
        
        // Parameters
        final int       intDepth = 2;
        final int       intFanOut = 3;
        final NodeSet   enmNodeSet = NodeSet.ALL;
        final int       cntNodes = enmNodeSet.computeNodeCount(intDepth, intFanOut);
        
        StructureIndexGenerator genIndexes = new StructureIndexGenerator(intDepth, intFanOut, enmNodeSet);
        
        Assert.assertTrue(genIndexes.hasNext());
        
        // Collect all indexes through one iteration
        List<List<Integer>>   lstIndexes = new LinkedList<>();
        
        while (genIndexes.hasNext()) {
            List<Integer>   lstIndex = genIndexes.nextIndex();
            
            lstIndexes.add(lstIndex);
        }
        
        Assert.assertFalse(genIndexes.hasNext());
        Assert.assertEquals(cntNodes, lstIndexes.size());
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + ": depth=" + intDepth + ", fanout=" + intFanOut + ", node set=" + enmNodeSet);
        System.out.println("  Indexes: " + lstIndexes);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.datasim.utility.StructureIndexGenerator#allIndexes()}.
     */
    @Test
    public final void testAllIndexes() {
        
        // Parameters
        final int       intDepth = 3;
        final int       intFanOut = 2;
        final NodeSet   enmNodeSet = NodeSet.TERMINAL;
        final int       cntNodes = enmNodeSet.computeNodeCount(intDepth, intFanOut);
        
        StructureIndexGenerator genIndexes = new StructureIndexGenerator(intDepth, intFanOut, enmNodeSet);
        
        Assert.assertTrue(genIndexes.hasNext());
        
        // Generate all indexes at once
        Collection<List<Integer>>   lstIndexes = genIndexes.allIndexes();
        
        Assert.assertFalse(genIndexes.hasNext());
        Assert.assertEquals(cntNodes, lstIndexes.size());
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + ": depth=" + intDepth + ", fanout=" + intFanOut + ", node set=" + enmNodeSet);
        System.out.println("  Indexes: " + lstIndexes);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.datasim.utility.StructureIndexGenerator#resetIndexIteration()}.
     */
    @Test
    public final void testResetIndexCounter() {
        
        // Parameters
        final int       intDepth = 3;
        final int       intFanOut = 3;
        final NodeSet   enmNodeSet = NodeSet.ALL;
        final int       cntNodes = enmNodeSet.computeNodeCount(intDepth, intFanOut);
        
        StructureIndexGenerator genIndexes = new StructureIndexGenerator(intDepth, intFanOut, enmNodeSet);
        
        Assert.assertTrue(genIndexes.hasNext());
        
        // Collect all indexes through one iteration
        List<List<Integer>>   lstIndexes = new LinkedList<>();
        
        while (genIndexes.hasNext()) {
            List<Integer>   lstIndex = genIndexes.nextIndex();
            
            lstIndexes.add(lstIndex);
        }
        
        Assert.assertFalse(genIndexes.hasNext());
        Assert.assertEquals(cntNodes, lstIndexes.size());
        
        // Do it again
        lstIndexes.clear();
        genIndexes.resetIndexIteration();
        
        Assert.assertTrue(genIndexes.hasNext());
        for (List<Integer> lstIndex : genIndexes) {
            lstIndexes.add(lstIndex);
        }
        
        Assert.assertFalse(genIndexes.hasNext());
        Assert.assertEquals(cntNodes, lstIndexes.size());
    }

    /**
     * Test method for {@link com.ospreydcs.dp.datasim.utility.StructureIndexGenerator#resetIndexIteration(NodeSet)}.
     */
    @Test
    public final void testResetIndexCounterNodeSet() {

        // Parameters
        final int       intDepth = 3;
        final int       intFanOut = 2;
        NodeSet         enmNodeSet = NodeSet.INTERNAL;
        
        // Create the structure index iterator
        StructureIndexGenerator genIndexes = new StructureIndexGenerator(intDepth, intFanOut, enmNodeSet);
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + ": depth=" + intDepth + ", fanout=" + intFanOut);
        
        Assert.assertTrue(genIndexes.hasNext());
        
        // Iterate through internal nodes
        List<List<Integer>> lstIndexes = new LinkedList<>();
        while (genIndexes.hasNext())
            lstIndexes.add( genIndexes.nextIndex() );
        
        Assert.assertFalse(genIndexes.hasNext());
        Assert.assertEquals(enmNodeSet.computeNodeCount(intDepth, intFanOut), lstIndexes.size());
        
        System.out.println("   node set=" + enmNodeSet + " indexes: " + lstIndexes);
        
        // Iterate through terminal nodes
        enmNodeSet = NodeSet.TERMINAL;
        genIndexes.resetIndexIteration(enmNodeSet);
        lstIndexes.clear();
        
        Assert.assertTrue(genIndexes.hasNext());
        
        for (List<Integer> lstIndex : genIndexes)
            lstIndexes.add(lstIndex);

        Assert.assertFalse(genIndexes.hasNext());
        Assert.assertEquals(enmNodeSet.computeNodeCount(intDepth, intFanOut), lstIndexes.size());
        
        System.out.println("   node set=" + enmNodeSet + " indexes: " + lstIndexes);
        
        // Iterate through all nodes
        enmNodeSet = NodeSet.ALL;
        genIndexes.resetIndexIteration(enmNodeSet);
        lstIndexes.clear();
        
        Assert.assertTrue(genIndexes.hasNext());
        
        Collection<List<Integer>>  setIndexes = genIndexes.allIndexes();
        
        Assert.assertFalse(genIndexes.hasNext());
        Assert.assertEquals(enmNodeSet.computeNodeCount(intDepth, intFanOut), setIndexes.size());
        
        System.out.println("   node set=" + enmNodeSet + " indexes: " + setIndexes);
    }

    /**
     * Test method for {@link com.ospreydcs.dp.datasim.utility.StructureIndexGenerator#iterator()}.
     */
    @Test
    public final void testIterator() {
        
        // Parameters
        final int       intDepth = 4;
        final int       intFanOut = 3;
        final NodeSet   enmNodeSet = NodeSet.INTERNAL;
        final int       cntNodes = enmNodeSet.computeNodeCount(intDepth, intFanOut);
        
        StructureIndexGenerator genIndexes = new StructureIndexGenerator(intDepth, intFanOut, enmNodeSet);
        
        Assert.assertTrue(genIndexes.hasNext());
        
        // Generate all indexes using iterator
        List<List<Integer>>   lstIndexes = new LinkedList<>();
        
        for (List<Integer> lstIndex : genIndexes) {
            lstIndexes.add(lstIndex);
        }
        
        Assert.assertFalse(genIndexes.hasNext());
        Assert.assertEquals(cntNodes, lstIndexes.size());
        
        System.out.println(JavaRuntime.getQualifiedMethodNameSimple() + ": depth=" + intDepth + ", fanout=" + intFanOut + ", node set=" + enmNodeSet);
        System.out.println("  Indexes: " + lstIndexes);
    }

}
