package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import java.util.Arrays;

import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.StructureFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.values.JalToolsStructFactoryConfig;

/** 
 * <p>
 * Record containing <code>StructureFactory</code> configuration parameters 
 * </p>
 * <p>
 * The fields of this configuration record contain the parameters of the <code>StructureFactory</code> class.
 * A <code>{@link StructureFactory}</code> class contains 4 parameters: 
 * <ol>
 * <li>'depth' = the node depth of the tree structure (how many tree nodes until the terminal nodes are reached).</li>
 * <li>'fanout' = the number of sub-nodes for each tree structure node (until terminal nodes are reached).</li>
 * <li>'unique names' = enable/disable unique field names for each tree structure produced by factory.</li>
 * <li>'recScalarSpec' = the <code>{@link ScalarFactorySpec}</code> configuration scalar factory producing field values.</li>
 * </ol>
 * </p>
 * <p>
 * The fields of this configuration record contain the parameters of the <code>StructureFactory</code> class.
 * </p>  
 * 
 * @param intDepth      node depth of tree structures produced
 * @param intFanout     node fan-out of tree structure
 * @param bolUniqNms    enable/disable creation of unique field names by structure factory 
 * @param recScalarSpec configuration for scalar factory producing structure field values
 */
public record StructureFactorySpec(int intDepth, int intFanout, boolean bolUniqNms, ScalarFactorySpec recScalarSpec) {
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>StructureFactorySpec</code> record configured all default values.
     * </p>
     * <p>
     * This creator is offered as a convenience.  It creates a <code>{@link ScalarFactorySpec}</code> instance
     * from the argument collection which is then used in <code>StructureFactorySpec</code> construction.
     * The argument collection contains some the field values of the instantiated <code>ScalarFactorySpec</code>
     * and some the field values of the returned <code>StructureFactorySpec</code> instance.
     * This creator is uses the default values taken from the JAL Tools
     * default configuration as listed below:
     * <ul>
     * <li><code>intDepth = {@link #INT_TREE_DEPTH_DEF}</code>.</li>
     * <li><code>intFanout = {@link #INT_TREE_FANOUT_DEF}</code>.</li>
     * <li><code>enmType = {@link #ENM_FLD_VALS_TYPE_DEF}</code>.</li>
     * <li><code>bolUniqFldNms = {@link #BOL_FLD_NMS_UNIQ_DEF}</code>.</li>
     * <li><code>lngSeed = {@link #LNG_FLD_VALS_RAND_SEED_DEF}</code>.</li>
     * <li><code>bolRandEnbl = {@link #BOL_FLD_VALS_RAND_ENBL_DEF}</code>.</li>
     * <li><code>bolRandEnbl == true</code>:
     *   <ul>
     *   <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long)}</code>.</li>
     *   </ul>
     * </li>
     * <li><code>bolRandEnbl == false</code>:
     *   <ul>
     *   <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean)}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     * 
     * @return  a new <code>StructureFactorySpec</code> instance populated with all default field values
     */
    public static StructureFactorySpec    from() {
        return StructureFactorySpec.from(INT_TREE_DEPTH_DEF, INT_TREE_FANOUT_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>StructureFactorySpec</code> record configured with the given argument values.
     * </p>
     * <p>
     * This creator is offered as a convenience.  It creates a <code>{@link ScalarFactorySpec}</code> instance
     * from the argument collection which is then used in <code>StructureFactorySpec</code> construction.
     * The argument collection contains some the field values of the instantiated <code>ScalarFactorySpec</code>
     * and some the field values of the returned <code>StructureFactorySpec</code> instance.
     * This creator is uses the default values taken from the JAL Tools
     * default configuration as listed below:
     * <ul>
     * <li><code>enmType = {@link #ENM_FLD_VALS_TYPE_DEF}</code>.</li>
     * <li><code>bolUniqFldNms = {@link #BOL_FLD_NMS_UNIQ_DEF}</code>.</li>
     * <li><code>lngSeed = {@link #LNG_FLD_VALS_RAND_SEED_DEF}</code>.</li>
     * <li><code>bolRandEnbl = {@link #BOL_FLD_VALS_RAND_ENBL_DEF}</code>.</li>
     * <li><code>bolRandEnbl == true</code>:
     *   <ul>
     *   <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long)}</code>.</li>
     *   </ul>
     * </li>
     * <li><code>bolRandEnbl == false</code>:
     *   <ul>
     *   <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean)}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     * 
     * @param intDepth         node depth of tree structures produced
     * @param intFanout        node fan-out of tree structure
     * 
     * @return  a new <code>StructureFactorySpec</code> instance populated with the above argument values
     */
    public static StructureFactorySpec    from(int intDepth, int intFanout) {
        return StructureFactorySpec.from(intDepth, intFanout, BOL_FLD_NMS_UNIQ_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>StructureFactorySpec</code> record configured with the given argument values.
     * </p>
     * <p>
     * <p>
     * This creator is offered as a convenience.  It creates a <code>{@link ScalarFactorySpec}</code> instance
     * from the argument collection which is then used in <code>StructureFactorySpec</code> construction.
     * The argument collection contains some the field values of the instantiated <code>ScalarFactorySpec</code>
     * and some the field values of the returned <code>StructureFactorySpec</code> instance.
     * This creator is uses the default values taken from the JAL Tools
     * default configuration as listed below:
     * <ul>
     * <li><code>enmType = {@link #ENM_FLD_VALS_TYPE_DEF}</code>.</li>
     * <li><code>lngSeed = {@link #LNG_FLD_VALS_RAND_SEED_DEF}</code>.</li>
     * <li><code>bolRandEnbl = {@link #BOL_FLD_VALS_RAND_ENBL_DEF}</code>.</li>
     * <li><code>bolRandEnbl == true</code>:
     *   <ul>
     *   <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long)}</code>.</li>
     *   </ul>
     * </li>
     * <li><code>bolRandEnbl == false</code>:
     *   <ul>
     *   <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean)}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     * 
     * @param intDepth      node depth of tree structures produced
     * @param intFanout     node fan-out of tree structure
     * @param bolUniqNms    enable/disable creation of unique field names by structure factory 
     * 
     * @return  a new <code>StructureFactorySpec</code> instance populated with the above argument values
     */
    public static StructureFactorySpec    from(int intDepth, int intFanout, boolean bolUniqNms) {
        return StructureFactorySpec.from(intDepth, intFanout, bolUniqNms, ENM_FLD_VALS_TYPE_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>StructureFactorySpec</code> record configured with the given argument values.
     * </p>
     * <p>
     * This creator is offered as a convenience.  It creates a <code>{@link ScalarFactorySpec}</code> instance
     * from the argument collection which is then used in <code>StructureFactorySpec</code> construction.
     * The argument collection contains some the field values of the instantiated <code>ScalarFactorySpec</code>
     * and some the field values of the returned <code>StructureFactorySpec</code> instance.
     * This creator is uses the default values taken from the JAL Tools
     * default configuration as listed below:
     * <ul>
     * <li><code>bolUniqFldNms = {@link #BOL_FLD_NMS_UNIQ_DEF}</code>.</li>
     * <li><code>enmType = {@link #ENM_FLD_VALS_TYPE_DEF}</code>.</li>
     * <li><code>lngSeed = {@link #LNG_FLD_VALS_RAND_SEED_DEF}</code>.</li>
     * <li><code>bolRandEnbl = {@link #BOL_FLD_VALS_RAND_ENBL_DEF}</code>.</li>
     * <li><code>bolRandEnbl == true</code>:
     *   <ul>
     *   <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long)}</code>.</li>
     *   </ul>
     * </li>
     * <li><code>bolRandEnbl == false</code>:
     *   <ul>
     *   <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean)}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     * 
     * @param   intDepth      node depth of tree structures produced
     * @param   intFanout     node fan-out of tree structure
     * @param   enmType       the data type of the scalar values to generate
     * 
     * @return  a new <code>StructureFactorySpec</code> instance populated with the above argument values
     */
    public static StructureFactorySpec    from(int intDepth, int intFanout, JalScalarType enmType) {
        
        return StructureFactorySpec.from(intDepth, intFanout, BOL_FLD_NMS_UNIQ_DEF, enmType);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>StructureFactorySpec</code> record configured with the given argument values.
     * </p>
     * <p>
     * This creator is offered as a convenience.  It creates a <code>{@link ScalarFactorySpec}</code> instance
     * from the argument collection which is then used in <code>StructureFactorySpec</code> construction.
     * The argument collection contains some the field values of the instantiated <code>ScalarFactorySpec</code>
     * and all the field values of the returned <code>StructureFactorySpec</code> instance.
     * This creator is uses the default values taken from the JAL Tools
     * default configuration as listed below:
     * <ul>
     * <li><code>enmType = {@link #ENM_FLD_VALS_TYPE_DEF}</code>.</li>
     * <li><code>lngSeed = {@link #LNG_FLD_VALS_RAND_SEED_DEF}</code>.</li>
     * <li><code>bolRandEnbl = {@link #BOL_FLD_VALS_RAND_ENBL_DEF}</code>.</li>
     * <li><code>bolRandEnbl == true</code>:
     *   <ul>
     *   <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long)}</code>.</li>
     *   </ul>
     * </li>
     * <li><code>bolRandEnbl == false</code>:
     *   <ul>
     *   <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean)}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     * 
     * @param   intDepth      node depth of tree structures produced
     * @param   intFanout     node fan-out of tree structure
     * @param   bolUniqNms    enable/disable creation of unique field names by structure factory 
     * @param   enmType       the data type of the scalar values to generate
     * 
     * @return  a new <code>StructureFactorySpec</code> instance populated with the above argument values
     */
    public static StructureFactorySpec    from(int intDepth, int intFanout, boolean bolUniqNms, JalScalarType enmType) {
        
        return StructureFactorySpec.from(intDepth, intFanout, bolUniqNms, enmType, BOL_FLD_VALS_RAND_ENBL_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>StructureFactorySpec</code> record configured with the given argument values.
     * </p>
     * <p>
     * This creator is offered as a convenience.  It creates a <code>{@link ScalarFactorySpec}</code> instance
     * from the argument collection which is then used in <code>StructureFactorySpec</code> construction.
     * The argument collection contains some the field values of the instantiated <code>ScalarFactorySpec</code>
     * and all the field values of the returned <code>StructureFactorySpec</code> instance.
     * This creator is uses the default values taken from the JAL Tools
     * default configuration as listed below:
     * <ul>
     * <li><code>lngSeed = {@link #LNG_FLD_VALS_RAND_SEED_DEF}</code>.</li>
     * <li><code>bolRandEnbl == true</code>:
     *   <ul>
     *   <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long)}</code>.</li>
     *   </ul>
     * </li>
     * <li><code>bolRandEnbl == false</code>:
     *   <ul>
     *   <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean)}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     * 
     * @param   intDepth      node depth of tree structures produced
     * @param   intFanout     node fan-out of tree structure
     * @param   bolUniqNms    enable/disable creation of unique field names by structure factory 
     * @param   enmType       the data type of the scalar values to generate
     * @param   bolRandEnbl   enable/disable the use of random number generation for scalar values
     * 
     * @return  a new <code>StructureFactorySpec</code> instance populated with the above argument values
     */
    public static StructureFactorySpec    from(int intDepth, int intFanout, boolean bolUniqNms, JalScalarType enmType, boolean bolRandEnbl) {
        
        return StructureFactorySpec.from(intDepth, intFanout, bolUniqNms, enmType, bolRandEnbl, LNG_FLD_VALS_RAND_SEED_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>StructureFactorySpec</code> record configured with the given argument values.
     * </p>
     * <p>
     * This creator is offered as a convenience.  It creates a <code>{@link ScalarFactorySpec}</code> instance
     * from the argument collection which is then used in <code>StructureFactorySpec</code> construction.
     * The argument collection contains some the field values of the instantiated <code>ScalarFactorySpec</code>
     * and all the field values of the returned <code>StructureFactorySpec</code> instance.
     * This creator is uses the default values taken from the JAL Tools
     * default configuration as listed below:
     * <ul>
     * <li><code>bolRandEnbl == true</code>:
     *   <ul>
     *   <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long)}</code>.</li>
     *   </ul>
     * </li>
     * <li><code>bolRandEnbl == false</code>:
     *   <ul>
     *   <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean)}</code>.</li>
     *   </ul>
     * </li>
     * </ul>
     * </p>
     * 
     * @param   intDepth      node depth of tree structures produced
     * @param   intFanout     node fan-out of tree structure
     * @param   bolUniqNms    enable/disable creation of unique field names by structure factory 
     * @param   enmType       the data type of the scalar values to generate
     * @param   bolRandEnbl   enable/disable the use of random number generation for scalar values
     * @param   lngSeed       seed value for random number generation or start value for incremental value generation   
     * 
     * @return  a new <code>StructureFactorySpec</code> instance populated with the above argument values
     */
    public static StructureFactorySpec    from(int intDepth, int intFanout, boolean bolUniqNms, JalScalarType enmType, boolean bolRandEnbl, long lngSeed) {
        
        // Create scalar factory specification according to random enable/disable flag
        ScalarFactorySpec   recSpec;
        if (bolRandEnbl)
            recSpec = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed);
        else
            recSpec = ScalarFactorySpec.from(enmType, bolRandEnbl);
        
        return StructureFactorySpec.from(intDepth, intFanout, bolUniqNms, recSpec);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>StructureFactorySpec</code> record configured with the given argument values.
     * </p>
     * <p>
     * This creator is offered as a convenience.  It creates a <code>{@link ScalarFactorySpec}</code> instance
     * from the argument collection which is then used in <code>StructureFactorySpec</code> construction.
     * The argument collection contains some the field values of the instantiated <code>ScalarFactorySpec</code>
     * and all the field values of the returned <code>StructureFactorySpec</code> instance.
     * This creator is uses the default values taken from the JAL Tools
     * default configuration as listed below:
     * <ul>
     * <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long, Number)}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   intDepth      node depth of tree structures produced
     * @param   intFanout     node fan-out of tree structure
     * @param   bolUniqNms    enable/disable creation of unique field names by structure factory 
     * @param   enmType       the data type of the scalar values to generate
     * @param   bolRandEnbl   enable/disable the use of random number generation for scalar values
     * @param   lngSeed       seed value for random number generation or start value for incremental value generation   
     * @param   numIncr       numeric incremental value used when random generation is disabled (type depends upon data type)
     * 
     * @return  a new <code>StructureFactorySpec</code> instance populated with the above argument values
     */
    public static StructureFactorySpec    from(int intDepth, int intFanout, boolean bolUniqNms, JalScalarType enmType, boolean bolRandEnbl, long lngSeed, Number numIncr) {
        ScalarFactorySpec   recSpec = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed, numIncr);
        
        return StructureFactorySpec.from(intDepth, intFanout, bolUniqNms, recSpec);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>StructureFactorySpec</code> record configured with the given argument values.
     * </p>
     * <p>
     * This creator is offered as a convenience.  It creates a <code>{@link ScalarFactorySpec}</code> instance
     * from the argument collection which is then used in <code>StructureFactorySpec</code> construction.
     * The argument collection contains all the field values of the instantiated <code>ScalarFactorySpec</code>
     * and all the field values of the returned <code>StructureFactorySpec</code> instance.
     * </p>
     * 
     * @param   intDepth      node depth of tree structures produced
     * @param   intFanout     node fan-out of tree structure
     * @param   bolUniqNms    enable/disable creation of unique field names by structure factory 
     * @param   enmType       the data type of the scalar values to generate
     * @param   bolRandEnbl   enable/disable the use of random number generation for scalar values
     * @param   lngSeed       seed value for random number generation or start value for incremental value generation   
     * @param   numIncr       numeric incremental value used when random generation is disabled (type depends upon data type)
     * @param   strPrefix     prefix used for all string value generation (suffix given by integer value)
     * 
     * @return  a new <code>StructureFactorySpec</code> instance populated with the above argument values
     */
    public static StructureFactorySpec    from(int intDepth, int intFanout, boolean bolUniqNms, JalScalarType enmType, boolean bolRandEnbl, long lngSeed, Number numIncr, String strPrefx) {
        ScalarFactorySpec   recSpec = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed, numIncr, strPrefx);
        
        return StructureFactorySpec.from(intDepth, intFanout, bolUniqNms, recSpec);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>StructureFactorySpec</code> record configured with the given argument values.
     * </p>
     * <p>
     * This creator is uses the default values taken from the JAL Tools
     * default configuration and listed below:
     * <ul>
     * <li><code>{@link #bolUniqNms()} = {@link BOL_FLD_NMS_UNIQ_DEF}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param intDepth      node depth of tree structures produced
     * @param intFanout     node fan-out of tree structure
     * @param recScalarSpec configuration for scalar factory producing structure field values
     * 
     * @return  a new <code>StructureFactorySpec</code> instance populated with the above argument values
     */
    public static StructureFactorySpec    from(int intDepth, int intFanout, ScalarFactorySpec recScalarSpec) {
        return StructureFactorySpec.from(intDepth, intFanout, BOL_FLD_NMS_UNIQ_DEF, recScalarSpec);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>StructureFactorySpec</code> record configured with the given argument values.
     * </p>
     * <p>
     * This creator is equivalent to the canonical constructor where the argument collection contains all 
     * field values.
     * </p>
     * 
     * @param intDepth      node depth of tree structures produced
     * @param intFanout     node fan-out of tree structure
     * @param bolUniqNms    enable/disable creation of unique field names by structure factory 
     * @param recScalarSpec configuration for scalar factory producing structure field values
     * 
     * @return  a new <code>StructureFactorySpec</code> instance populated with the above argument values
     */
    public static StructureFactorySpec    from(int intDepth, int intFanout, boolean bolUniqNms, ScalarFactorySpec recScalarSpec) {
        return new StructureFactorySpec(intDepth, intFanout, bolUniqNms, recScalarSpec);
    }
    
    /**
     * <p>
     * Parses the argument collection for the field values of the returned <code>StructureFactorySpec</code> instance.
     * </p>
     * <p>
     * The argument collection is assumed to originate from an application command-line argument collection.
     * The <code>{@link TensorFactory}</code> class requires a 'shape' parameter and a 
     * <code>{@link ScalarFactory}</code> to create its element values.
     * </p>
     * <p>
     * <h2>Scalar Factory</h2>
     * The <code>{@link TensorFactory}</code> class requires a <code>{@link ScalarFactory}</code> instance.
     * The scalar factory is used to generate the elements
     * values of all tensors produced by the tensor factory described by this configuration.
     * Note the configuration for the <code>{@link ScalarFactorySpec}</code> field <code>{@link #recScalarSpec}</code>
     * is potentially included in the argument collection; if not provided a default scalar factory is supplied.
     * </p>  
     * <p>
     * <h2>Format</h2>
     * The format of the argument collection is assumed to be
     * <pre>
     * > depth fanout [bolUniqNms] [recScalarSpec]
     * </pre>
     * where
     * <ul>
     * <li>'depth' = tree structure node depth.</li>
     * <li>'fanout' = tree structure node fanout at each non-terminal node.</li>
     * <li>'bolUniqNms' = size of the 1st axis.</li>
     * <li>'recScalarSpec' = configuration record for the scalar factory producing tensor element values.</li>
     * </ul>
     * Note that 'depth' and 'fanout' are required parameters, thus, there must be at least 2 argument elements
     * or an exception is thrown.
     * </p>
     * <h2>Optional Arguments</h2>
     * The brackets indicate optional arguments.  The arguments are interpreted as follows:  
     * <ul>
     * <li>If the 'recScalarSpec' value is not present the argument is populated with the default scalar factory
     *     <code>{@link ScalarFactorySpec#from()}</code>.
     * </li>
     * <li>If the 'bolUniqNms' value is not present the value is taken from the JAL Tools default configuration
     *     with value <code>{@link #BOL_FLD_NMS_UNIQ_DEF}</code>
     * </li>
     * </ul>
     * </p>
     * 
     * @param args  argument collection to be parsed, format as described above
     * 
     * @return  a new <code>StructureFactorySpec</code> record populated with the parsed argument values
     * 
     * @throws <s>ConfigurationException       argument must have at least 2 elements; 'depth' and 'fanout' parameters</s>
     * @throws NumberFormatException        invalid numeric format (e.g., non-parseable 'depth', 'fanout', or scalar factory configuration)
     * @throws TypeNotPresentException      scalar factory configuration had unrecognized <code>JalScalarType</code> constant
     * @throws UnsupportedOperationException scalar factory configuration count not create 'increment' field
     * 
     * @see ScalarFactorySpec
     */
    public static StructureFactorySpec    parse(String...args) throws /* ConfigurationException,*/ NumberFormatException, TypeNotPresentException, UnsupportedOperationException {
        
        // Check argument size
        if (args.length < 2)
//            throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() 
//                    + " - Arguments must contain at least 2 elements, depth and fan-out: " + args);
            return StructureFactorySpec.from();
        
        // Parse the depth and fan-out parameters
        int     depth = Integer.valueOf(args[0]);   // throws NumberFormatException
        int     fanout = Integer.valueOf(args[1]);  // throws NumberFormatException
        if (args.length < 3)
            return StructureFactorySpec.from(depth, fanout);
        
        // Check if 3rd argument is a JalScalarType constant, ie., start of scalar factory configuration
        int     indScalCfg = 0; // the starting argument index for the scalar factory configuration (if it exists)
        boolean bolUniqNms;     // the enable/disable unique field names flag
        try {
            @SuppressWarnings("unused")
            JalScalarType   enmType = JalScalarType.valueFrom(args[2]);   // throws TypeNotPresentException

            // The scalar factory configuration exists and starts here (at index 2)
            //  No enable/disable unique field name provided - use default
            indScalCfg = 2;
            bolUniqNms = BOL_FLD_NMS_UNIQ_DEF;
            
        } catch (TypeNotPresentException e) {

            // The 3rd argument was not a JalScalarType
            //  Assume enable/disable unique field name flag and parse it
            indScalCfg = 3;
            bolUniqNms = Boolean.valueOf(args[2]);
        }
        
        String[]            arrScalCfg = Arrays.copyOfRange(args, indScalCfg, args.length);
        ScalarFactorySpec   recScalarSpec = ScalarFactorySpec.parse(arrScalCfg); // throws TypeNotPresentException, NumericFormatException, UnsupportedOperationException

        return StructureFactorySpec.from(depth, fanout, bolUniqNms, recScalarSpec);
    }

    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates and returns a new <code>StructureFactory</code> according to this configuration.
     * </p>
     * 
     * @return  a new <code>StructureFactory</code> instance ready for simulated byte array creation
     */
    public StructureFactory newFactory() {
        return StructureFactory.from(this.intDepth, this.intFanout, this.bolUniqNms, this.recScalarSpec.newFactory());
    }
    
    
    // 
    // Record Overrides
    //
    
    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof StructureFactorySpec spec)  {
            boolean bolResult = (this.intDepth == spec.intDepth)
                    && (this.intFanout == spec.intFanout)
                    && (this.bolUniqNms == spec.bolUniqNms)
                    && (this.recScalarSpec.equals(spec.recScalarSpec));
            return bolResult;
        }
        
        return false;
    }

    /**
     * @see java.lang.Record#toString()
     */
    @Override
    public String toString() {
        String  str = "";
        str += "Structure depth            : " + this.intDepth + "\n";
        str += "Structure fan-out          : " + this.intFanout + "\n";
        str += "Unique field names enabled : " + this.bolUniqNms + "\n";
        str += "Scalar Factory Configuration \n";
        str += this.recScalarSpec.toString();
        
        return str;
    }
    
    
    //
    // JAL Library Resources
    //
    
    /** JAL Tools default configuration parameters for datum factories */
    private static final JalToolsStructFactoryConfig    CFG_DEF = JalToolsConfig.getInstance().datagen.values.structure;
    
    
    // 
    // Record Constants - Default Values
    //

    /** Default tree-structure node depth */
    public static final int             INT_TREE_DEPTH_DEF = CFG_DEF.tree.depth;
    
    /** Default tree-structure node fan-out (i.e., before terminal nodes) */
    public static final int             INT_TREE_FANOUT_DEF = CFG_DEF.tree.fanout;
    

    /** Default field value scalar type */
    public static final JalScalarType   ENM_FLD_VALS_TYPE_DEF = CFG_DEF.fieldValues.type;
    
    /** Default field value random generation enable/disable flag value */
    public static final boolean         BOL_FLD_VALS_RAND_ENBL_DEF = CFG_DEF.fieldValues.random.enabled;
    
    /** Default field value random generator seed value (0 for 'random' seed) */
    public static final long            LNG_FLD_VALS_RAND_SEED_DEF = CFG_DEF.fieldValues.random.seed;

    
    /** Structure factory default value for unique field name creation */
    public static final boolean         BOL_FLD_NMS_UNIQ_DEF = CFG_DEF.fieldNames.unique.enabled;

}