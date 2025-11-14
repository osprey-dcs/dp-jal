/*
 * Project: dp-data-simulator
 * File:	StructureGenerator.java
 * Package: com.ospreydcs.dp.datasim.frame.model
 * Type: 	StructureGenerator
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
 * @since May 9, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.jal.tools.common.datagen.values;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import com.ospreydcs.dp.jal.tools.common.datagen.IDataValueFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;

/**
 * <p>
 * Creates a structure of artificial data with <code>{@link #intFanOut}</code>
 * fields at each level, each field having the same number of sub-fields, up to a depth of
 * <code>{@link #intDepth}</code>.
 * </p>
 * <p>
 * The generated structures are essentially symmetric trees with each node containing the same number
 * of sub-nodes (sub-fields), including the root node.
 * Thus, the field values within each structure are sub-structures for all but the last level where
 * scalars are used, with type indicated by <code>{@link ScalarType}</code> at construction.  
 * Thus, this structure generator class produces sub-structures within a generated structure.
 * </p>
 * <p>
 * Internally, structure creation uses recursion to create a tree structure which expands
 * geometrically.  Thus, be cautious when specifying configuration parameters 
 * <code>{@link #intDepth}</code> and <code>{@link #intFanOut}</code>. 
 * </p>
 * <h2>Structure Size and Allocation</h2>
 * <p>
 * The structure is a tree with each node containing 
 * <i>F</i> = <code>{@link #intFanOut}</code> branches.  Each branch 
 * contains another tree with the same structure, except for the terminating 
 * branch. The depth <i>D</i> of the tree is given by 
 * <pre>
 *   <i>D</i> = <code>cntDepth</code>
 * </pre>
 * where <code>cntDepth</code> is a constructor argument value.  The final branch 
 * contains fields with generated scalar values as specified at construction.
 * </p>
 * <h2>WARNING:</h2>
 * <p>
 * Because of the nature of the returned tree structure, its total size grows 
 * geometrically (approximately exponential).  
 * For a structure with <i>F</i> fields per node and with a depth <i>D</i>, 
 * the number <i>n</i> of <code>Double</code> values contained in the final 
 * branch is given by
 * <pre>
 *     <i>n</i> = <i>F</i><sup><i>D</i></sup>
 * </pre>
 * However, the total number of nodes <i>N</i>(<i>D</i>) to a depth <i>D</i> 
 * in the tree is given by the 
 * following:
 * <pre>
 *     <i>N</i>(<i>D</i>) = 1 + <i>F</i> + <i>F</i><sup>2</sup> + &hellip; + <i>F<sup>D</sup></i>
 *     
 *          = (<i>F</i><sup><i>D</i>+1</sup> - 1)/(<i>F</i> - 1)
 *          &asymp; <i>F</i><sup><i>D</i>+1</sup>/(<i>F</i> - 1)
 * </pre>
 * Thus, for example, a structure with <i>F</i> = 5 fields and depth <i>D</i> = 3
 * the number <i>n</i> of doubles in the final branch is 
 * <i>n</i> = 5<sup>3</sup> = 125
 * while the total number of nodes <i>N</i> is given by 
 * <i>N</i> = (5<sup>4</sup> - 1)/4 = 156.
 * <br/>
 * <br/> 
 * However, a structure with <i>F</i> = 5 fields and depth <i>D</i> = 5
 * the number <i>n</i> of doubles in the final branch is 
 * <i>n</i> = 5<sup>5</sup> = 3,125
 * while the total number of nodes <i>N</i> is given by 
 * <i>N</i> = (5<sup>6</sup> - 1)/4 = 3,906.
 * </p>
 * <p>
 * Let <i>A<sub>m</sub></i> be the allocation for a map and <i>A<sub>t</sub></i>
 * be the allocation per end node. Then the total allocation will be
 * <pre>
 *     <i>A</i><sub>tot</sub> = <i>N</i>(<i>D</i>-1)&times;<i>F</i>&times;<i>A<sub>m</sub></i> + <i>F</i><sup><i>D</i></sup>&times;<i>A<sub>t</sub></i>
 *          &asymp; <i>A</i>(2<i>F<sup>D</i>+1</sup> - <i>F<sup>D</sup></i> - <i>F</i>)/(<i>F</i> -  1)
 *          &asymp; 2<i>A</i><i>F</i><sup><i>D</i>+1</sup>/(<i>F</i> - 1)
 * </pre>
 * where <i>A<sub>tot</sub></i> is the total allocation. The first line
 * consists of the sum of the allocation for the internal nodes (each containing
 * <i>F</i> maps) and the terminal nodes.  In the second line we have 
 * assumed the approximation 
 * <i>A</i> &asymp; <i>A<sub>m</sub></i> &asymp; <i>A<sub>t</sub></i>, where
 * <i>A</i> is the average allocation.  The third line makes use of the 
 * inequality
 * 1/<i>F</i><sup><i>D</i>-1</sup> &lt;&lt; 1, and the inequality
 * 1 &lt;&lt; 2<i>F</i>.
 * </p>
 * <p>
 * The following table is provided to help design frame factory configuration
 * files.  There are example structure configurations producing memory allocations
 * of several different orders of magnitude.
 * </p>
 * <p>
 * <table>
 *   <tr>
 *     <th><i>F</i> (fields)</th>
 *     <th><i>D</i> (depth) </th>
 *     <th><i>n</i> (branches final)</th>
 *     <th><i>N</i> (nodes total)</th>
 *     <th>~Memory (bytes)</th>
 *   </tr>
 *   <tr>
 *     <td>4</td>
 *     <td>2</td>
 *     <td>16</td>
 *     <td>21</td>
 *     <td>1,575</td>
 *   </tr>
 *   <tr>
 *     <td>5</td>
 *     <td>2</td>
 *     <td>25</td>
 *     <td>31</td>
 *     <td>2,325</td>
 *   </tr>
 *   <tr>
 *     <td>5</td>
 *     <td>3</td>
 *     <td>125</td>
 *     <td>156</td>
 *     <td>11,700</td>
 *   </tr>
 *   <tr>
 *     <td>5</td>
 *     <td>5</td>
 *     <td>3,125</td>
 *     <td>3,906</td>
 *     <td>292,950</td>
 *   </tr>
 *   <tr>
 *     <td>5</td>
 *     <td>6</td>
 *     <td>15,625</td>
 *     <td>19,531</td>
 *     <td>1,464,825</td>
 *   </tr>
 *   <tr>
 *     <td>5</td>
 *     <td>7</td>
 *     <td>78,125</td>
 *     <td>97,656</td>
 *     <td>7,324,200</td>
 *   </tr>
 *   <tr>
 *     <td>7</td>
 *     <td>2</td>
 *     <td>49</td>
 *     <td>57</td>
 *     <td>4,275</td>
 *   </tr>
 *   <tr>
 *     <td>7</td>
 *     <td>3</td>
 *     <td>343</td>
 *     <td>400</td>
 *     <td>30,000</td>
 *   </tr>
 *   <tr>
 *     <td>7</td>
 *     <td>4</td>
 *     <td>2,401</td>
 *     <td>2,801</td>
 *     <td>201,075</td>
 *   </tr>
 *   <tr>
 *     <td>7</td>
 *     <td>5</td>
 *     <td>16,807</td>
 *     <td>19,608</td>
 *     <td>1,470,600</td>
 *   </tr>
 *   <tr>
 *     <td>10</td>
 *     <td>3</td>
 *     <td>1,00</td>
 *     <td>1,111</td>
 *     <td>83,325</td>
 *   </tr>
 *   <tr>
 *     <td>10</td>
 *     <td>4</td>
 *     <td>10,000</td>
 *     <td>11,111</td>
 *     <td>833,325</td>
 *   </tr>
 *   <tr>
 *     <td>10</td>
 *     <td>5</td>
 *     <td>100,00</td>
 *     <td>111,111</td>
 *     <td>8,333,325</td>
 *   </tr>
 *   <tr>
 *     <td>10</td>
 *     <td>6</td>
 *     <td>1,000,000</td>
 *     <td>1,111,111</td>
 *     <td>83,333,325</td>
 *   </tr>
 *   <caption>Tree Structure Allocations</caption>
 * </table>
 * </p>  
 * <p>
 * The memory allocation is an (under-)estimate using 85for
 * terminal each node. This value is the approximate size of a <code>Double</code> plus a 15-character
 * string.  The internal node allocation must be multiplied by <i>F</i>
 * since there are <i>F</i> such entries. 
 * (Note this estimate does not include the allocation for the map container.)
 * </p>
 *
 * @author Christopher K. Allen
 * @since Oct 21, 2022
 * @version May 9, 2024
 *
 * @deprecated Replaced by StructureFactory
 */
@Deprecated(since="Nov 14, 2025", forRemoval=true)
public final class StructureGenerator implements IDataValueFactory {


    //
    // Class Constants
    //

    /** Prefix added to all structure field names */
    public static final String STR_PREFIX_FIELD_NAME = "F";

    /** Separator used within field names */
    public static final String STR_SEPARATOR_FIELD_NAME = ":";

    /** Unique field name prefix - added to field names when unique field names are specified */
    public static final String STR_PREFIX_UNIQ_FIELD_NAME = "S";

    /** Unique field name prefix separator */
    public static final String STR_SEPARATOR_UNIQ_FIELD_PREFIX_TO_INDEX = ":";
    
    /** Unique field name prefix separator */
    public static final String STR_SEPARATOR_UNIQ_FIELD_PREFIX_TO_NAME = "-";
    
    


//    /** Name of the system property used to store structure count */
//    private static final String         STR_PROP_NAME_STRUCT_COUNT = "dp.data-simulator.structure.count";
//    
//
//    static {
//        String  strCntStructs = System.getProperty(STR_PROP_NAME_STRUCT_COUNT, "0");
//        StructureGenerator.cntStructsTotal = Integer.valueOf(strCntStructs);
//    }
//    
//    
//    //
//    // Class Resources
//    //
//    
//    /** Random number generator used to create random values */
//    private final static Random     GEN_NUMS = new Random();
//    
//    
//    //
//    // Class Variables
//    //
//    
//    /** Structure count (total number of generated structures) */
//    private static int      cntStructsTotal;
    
    
    //
    // Resources
    //
    
    /** Generator of scalar field values */
    private final ScalarGenerator  valGenerator;
    
    
    //
    // Configuration
    //
    
    /** The depth of each (tree) structure produced */
    private Integer     intDepth = null;
    
    /** The current number of sub-fields per field (node) */
    private Integer     intFanOut = null;
    
    /** Create unique field names flag */
    private boolean     bolUniqFldNames = false;
    

    //
    // Instance Variables
    //
    
    /** Counter of generated structures - used for unique naming of structure fields */
    private int         cntStructs = 0;
    
    /** The current structure field counter - used for naming terminal fields */
    @Deprecated
    private int         cntTermFlds = 0;

    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>StructureGenerator</code>.
     * </p>
     * <p>
     * Field values are generated sequentially with the first value given as 0.
     * </p>
     *
     * @param depth     depth of generated (tree) structures
     * @param fanOut    number of sub-fields within each field (and number of fields in first level)
     * @param fieldType scalar type of each terminal field value
     * 
     * @throws IllegalArgumentException <code>depth</code> < 1 and/or <code>fanOut</code> < 1
     */
    public StructureGenerator(int depth, int fanOut, JalScalarType fieldType) throws IllegalArgumentException {
        this(depth, fanOut, fieldType, 0, false);
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>StructureGenerator</code>.
     * </p>
     * <p>
     * Field values are generated sequentially with the first value given as by the seed value.
     * </p>
     *
     * @param depth     depth of generated (tree) structures
     * @param fanOut    number of sub-fields within each field (and number of fields in first level)
     * @param fieldType scalar type of each terminal field value
     * @param seed      initial scalar field value 
     * 
     * @throws IllegalArgumentException <code>depth</code> < 1 and/or <code>fanOut</code> < 1
     */
    public StructureGenerator(int depth, int fanOut, JalScalarType fieldType, long seed) throws IllegalArgumentException {
        this(depth, fanOut, fieldType, seed, false);
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>StructureGenerator</code>.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * Using random generation can creating a significant resource demand for large number of values.
     * Internally random values are generated using a <code>{@link Random}</code> Java object.
     * </p>
     *
     * @param depth     depth of generated (tree) structures
     * @param fanOut    number of sub-fields within each field (and number of fields in first level)
     * @param fieldType scalar type of each terminal field value
     * @param seed      initial scalar field value 
     * @param useRandom terminal field values generated randomly - <code>true</code>, 
     *                  sequentially - <code>false</code> 
     * 
     * @throws IllegalArgumentException <code>depth</code> < 1 and/or <code>fanOut</code> < 1
     */
    public StructureGenerator(int depth, int fanOut, JalScalarType fieldType, long seed, boolean useRandom) throws IllegalArgumentException {
        
        // Check arguments
        if (depth < 1)
            throw new IllegalArgumentException("Argument depth = " + depth + " must be > 0.");
        if (fanOut < 1)
            throw new IllegalArgumentException("Argument fanOut = " + fanOut + " must be > 0.");
        
        this.intDepth = depth;
        this.intFanOut = fanOut;
        this.valGenerator = new ScalarGenerator(fieldType, seed, useRandom);
    }

    
    //
    // Configuration
    //
    
    /**
     * <p>
     * Toggles the use of a random value generator for terminal-level field value creation.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * <ul>
     * <li>
     * Using random generation can creating a significant resource demand for large number of values.
     * Internally random values are generated using a <code>{@link Random}</code> Java object.
     * </li>
     * <li>
     * Scalar values are generated incrementally by default.  Random number generation is turned on
     * using this function.
     * </li>
     * </p>  
     * 
     * @param useRandomValues   <code>true</code> generate scalar values randomly,
     *                          <code>false</code> generate scalar values incrementally
     */
    public void setRandomValues(boolean useRandomValues) {
        this.valGenerator.setRandom(useRandomValues);
    }
    
    /**
     * <p>
     * Toggle the generation of unique field names for all generated structures.
     * </p>
     * <p>
     * If <code>true</code> all structure field names generated by this instance will be unique.  Otherwise
     * field names will all be unique to their position with a generated structure, but all structures will
     * have a similar field naming pattern (although field values will be different).
     * </p>
     * 
     * @param uniqueNames   <code>true</code> all structures generated by this instance have unique field names,
     *                      <code>false</code> field names are only unique according to structure position
     */
    public void setUniqueFieldNames(boolean uniqueNames) {
        this.bolUniqFldNames = uniqueNames;
    }
    
    /**
     * <p>
     * Determines whether or not terminal-level field values within structures are generated 
     * randomly (i.e., using a random number generator).
     * </p>
     * 
     * @return  <code>true</code> the terminal field values are generated randomly,
     *          <code>false</code> the terminal field values are generated incrementally
     */
    public boolean      hasRandomValues() {
        return this.valGenerator.isRandom();
    }
    
    /**
     * <p>
     * Determines whether or not the unique field name feature is enabled.
     * </p>
     * 
     * @return  <code>true</code> if all generated structures have unique field names,
     *          <code>false</code> all generated structures have the same field names
     */
    public boolean      hasUniqueFieldNames() {
        return this.bolUniqFldNames;
    }
    
    /**
     * <p>
     * Returns the depth of structures generated, that is, the number of structure levels.
     * </p>
     * 
     * @return  number of levels for each generated structure 
     */
    public int  getDepth() {
        return this.intDepth;
    }
    
    /**
     * <p>
     * Returns the number sub-fields per structure field, including root node.
     * </p>
     * <p>
     * This is equivalent to the fan out of a tree graph, which is constant in this case.
     * </p> 
     * 
     * @return  number of fields contained in each tree-structure node
     */
    public int  getFieldsPerNode() {
        return this.intFanOut;
    }
    
    /**
     * <p>
     * Returns the scalar type of the terminal-level field values.
     * </p>
     * 
     * @return  scalar type of terminal-level structure field values. 
     */
    public JalScalarType   getType() {
        return this.valGenerator.getType();
    }
    
    
    /**
     * <p>
     * Returns the seed value used to initialize the scalar value sequence for terminal structure 
     * fields.
     * </p>
     * 
     * @return  scalar value seed provided at construction  
     */
    public long  getSeed() {
        return  this.valGenerator.getSeed();
    }

    
    //
    // IDataValueFactory Interface
    //
    
    /**
     * <p>
     * Creates the next structure in the generated sequence.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Within the Data Platform Java client API library structures are represented as Java 
     * <code>{@link Map}</code> objects with bindings <code>Map&lt;String, Object&gt;</code>.
     * </p>
     * 
     * @return the next structure returned as a Java <code>Object</code> within underlying type <code>Map</code>
     *
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IDataValueFactory.model.values.IDataValueGenerator#nextValue()
     */
    @Override
    public Object nextValue() {
        
        // Reset terminal-level field counter (for field name generation)
        this.cntTermFlds = 0;
        
        // Create structure
        List<Integer>   lstNodeIndex = new LinkedList<>();
        
        Object  objStruct = this.createStructure(lstNodeIndex);
        
        // Increase structure count (for unique field name generation)
        this.cntStructs++;
        
        return objStruct;
    }

    
    //
    // Support Methods
    //
    
    /**
     * </p>
     * Creates a (sub-)structure with <code>{@link #intFanOut}</code>
     * fields, each have the same number of sub-fields, up to a depth of
     * <code>{@link #intDepth} - cntDepth</code>.
     * </p>
     * <p>
     * This is a recursive function and should be called initially using the argument
     * value 0 to achieve full structure depth.
     * </p>
     * 
     * @param cntDepth current depth in recursion - use 0 to start
     * 
     * @return a <code>Map</code> structure containing artificial field values 
     * 
     * @see #intDepth
     * @see #intFanOut
     * @see #GEN_NUMS
     * 
     * @deprecated replaced by {@link #createStructure(List)}
     */
    @Deprecated(since="May 20, 2024", forRemoval=true)
    private Map<String, Object> createStructure(int cntDepth) {

        // Check if we are at maximum depth 
        // - if so this is a terminal operation
        // - create the final map, populate it, and return it
        if (cntDepth >= this.intDepth) {
            Map<String, Object> map = new TreeMap<>();
            
            for (int iFld = 0; iFld<intFanOut; iFld++) {
                String strFldNm = this.createFieldName(cntDepth, iFld, true);
                Object objVal   = this.valGenerator.nextValue();
                
                map.put(strFldNm, objVal);
            }
            
            this.cntTermFlds++;
            
            return map;
        }
        
        // Otherwise we are at mid-depth within structure
        // - create the map of fields and populate using recursion
        Map<String, Object> map = new TreeMap<>();
        
        for (int iFld = 0; iFld<intFanOut; iFld++) {
            String strFldNm = this.createFieldName(cntDepth, iFld, false);
            Object objFldVal = this.createStructure(cntDepth + 1);
            
            map.put(strFldNm, objFldVal);
        }
        
        return map;
    }
    
    /**
     * </p>
     * Creates a (sub-)structure with <code>{@link #intFanOut}</code>
     * field (nodes), each having the same number of sub-fields, up to a depth of
     * <code>{@link #intDepth}</code>.
     * </p>
     * <p>
     * This is a recursive function and should be called initially using the argument
     * containing an empty node index list.
     * </p>
     * <p>
     * Terminal nodes are Java <code>Object</code> instances containing a scalar value
     * generated artificially (simulated value).  The data type of the scalar is prescribed
     * at construction.
     * </p>
     * 
     * @param lstNodeIndex  current node index within tree structure - use [] to start
     * 
     * @return a <code>Map</code> structure containing artificial field values 
     * 
     * @see #intDepth
     * @see #intFanOut
     * @see #valGenerator
     */
    private Map<String, Object> createStructure(List<Integer> lstNodeIndex) {

        // Check if we are at maximum depth 
        // - if so this is a terminal operation
        if (lstNodeIndex.size() + 1 >= this.intDepth) {

            // Create the final node map
            Map<String, Object> map = new TreeMap<>();
            
            // Populate the scalar values for each final node  
            for (int iFld = 0; iFld<intFanOut; iFld++) {

                // Create index for each terminal field
                List<Integer>   lstSubNodeIndex = new LinkedList<>(lstNodeIndex);
                lstSubNodeIndex.add(iFld);
                
                // Create name and scalar value for each terminal field
                String strFldNm = this.createNodeName(lstSubNodeIndex);
                Object objVal   = this.valGenerator.nextValue();
                
                map.put(strFldNm, objVal);
            }
            
            return map;
        }
        
        // Otherwise we are at mid-depth within structure - Recursion
        // - create the map of fields and populate using recursion
        Map<String, Object> map = new TreeMap<>();
        
        for (int iFld = 0; iFld<intFanOut; iFld++) {
            List<Integer>   lstSubNodeIndex = new LinkedList<>(lstNodeIndex);
            lstSubNodeIndex.add(iFld);
            
            String strFldNm = this.createNodeName(lstSubNodeIndex);
            Object objFldVal = this.createStructure(lstSubNodeIndex);
            
            map.put(strFldNm, objFldVal);
        }
        
        return map;
    }
    
    /**
     * <p>
     * Creates a new structure field name.
     * </p>
     * <p>
     * Field names are created according to the depth (level) within the structure and the index within that
     * level.  If unique names are specified the structure number is prepended to the name.
     * If the terminal field flag is <code> then the terminal field counter value 
     * <code>{@link #cntTermFlds}</code> is appended to the name.
     * </p> 
     * 
     * @param cntDepth      depth of field with structure (i.e., structure level)
     * @param iField        field index within structure level
     * @param bolTerminal   is a terminal-depth field?
     * 
     * @return              new field name
     * 
     * @deprecated replaced by {@link #createNodeName(List)}
     */
    @Deprecated(since="May 20, 2024", forRemoval=true)
    private String  createFieldName(int cntDepth, int iField, boolean bolTerminal) {
        
        // The returned field name
        String  strFldNm = "";
        
        // If field names are unique prefix with structure number
        if (this.bolUniqFldNames)
            strFldNm = STR_PREFIX_UNIQ_FIELD_NAME + STR_SEPARATOR_FIELD_NAME + this.cntStructs + STR_SEPARATOR_FIELD_NAME;
        
        // Create field name with structure depth and field index
        strFldNm += STR_PREFIX_FIELD_NAME + STR_SEPARATOR_FIELD_NAME + cntDepth + STR_SEPARATOR_FIELD_NAME + iField;
        
        // If a terminal field add the current terminal field count
        if (bolTerminal) {
            strFldNm += STR_SEPARATOR_FIELD_NAME + this.cntTermFlds;
            
            this.cntTermFlds++;
        }
        
        return strFldNm;
    }

    /**
     * <p>
     * Creates a new structure node name.
     * </p>
     * <p>
     * Node names are created according to the index within the (tree) structure. 
     * If unique names are specified the structure number is prepended to the name.
     * </p> 
     * 
     * @param   lstNodeIndex    index of the node within the tree structure
     *  
     * @return  new node name corresponding to the given node index
     */
    private String  createNodeName(List<Integer> lstNodeIndex) {
        
        // The returned field name
        StringBuilder       bufNodeNm = new StringBuilder();
        
        // If field names are unique prefix with structure number
        if (this.bolUniqFldNames) {
            bufNodeNm.append(STR_PREFIX_UNIQ_FIELD_NAME);
            bufNodeNm.append(STR_SEPARATOR_UNIQ_FIELD_PREFIX_TO_INDEX);
            bufNodeNm.append(this.cntStructs);
            bufNodeNm.append(STR_SEPARATOR_UNIQ_FIELD_PREFIX_TO_NAME);
        }
        
        // Create field name with structure node index
        bufNodeNm.append(STR_PREFIX_FIELD_NAME);
        bufNodeNm.append(STR_SEPARATOR_FIELD_NAME);
        for (Integer intDepthIndex : lstNodeIndex) {
            bufNodeNm.append(intDepthIndex);
            bufNodeNm.append(STR_SEPARATOR_FIELD_NAME);
        }
        int     indLast = bufNodeNm.lastIndexOf(STR_SEPARATOR_FIELD_NAME);
        String  strNodeNm = bufNodeNm.substring(0, indLast);
        
        return strNodeNm;
    }
}
