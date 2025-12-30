package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ScalarFactory;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.TensorFactory;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.values.JalToolsTensorFactoryConfig;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/** 
 * <p>
 * Record containing <code>TensorFactory</code> configuration parameters 
 * </p>
 * The <code>{@link TensorFactory}</code> class requires 2 parameters: 1) the 'shape' of the tensors
 * produced, and 2) the scalar factory producing the elements of the tensors.
 * <ul>
 * <li>'shape' = an int[] array containing the axes sizes { n1, n2, n3, ...}.</li>
 * <li>'recScalarSpec = an <code>{@link ScalarFactorySpec}</code> record specifying the scalar factory configuration.</li>
 * </ul>
 * </p>
 * 
 * @param   arrShape        the shape of the tensors produced 
 * @param   recScalarSpec   the scalar factory configuration for tensor element values
 */
public record TensorFactorySpec(int[] arrShape, ScalarFactorySpec recScalarSpec) {
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactorySpec</code> record configured with all default parameters.
     * </p>
     * <p>
     * This creator uses default values taken from the JAL Tools default configuration for fields not contained 
     * in the arguments.  Some default parameters are inherited from the <code>{@link ScalarFactorySpec}</code>
     * creator(s). Others are taken from the 'structure' default parameters of the JAL Tools default configuration.
     * <ul>
     * <li><code>arrShape = {@link #ARR_SHAPE_DEF}</code>.</li>
     * <li><code>enmType = {@link #ENM_TYPE_DEF}</code>.</li>
     * <li><code>bolRandEnbl = {@link #BOL_RAND_ENBL_DEF}</code>.</li>
     * <li><code>lngSeed = {@link #LNG_RAND_SEED_DEF}</code>.</li>
     * <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long)}.</code></li>
     * <li><code>{@link #TensorFactorySpec(int[], ScalarFactorySpec)} = {@link #from(int[], ScalarFactorySpec)}.</code></li>
     * </ul>
     * </p>
     * 
     * @return  a new <code>TensorFactorySpec</code> instance populated with the above argument(s)
     */
    public static TensorFactorySpec    from() {
        return TensorFactorySpec.from(ARR_SHAPE_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactorySpec</code> record configured according to the given argument(s).
     * </p>
     * <p>
     * This creator uses default values taken from the JAL Tools default configuration for fields not contained 
     * in the arguments.  Some default parameters are inherited from the <code>{@link ScalarFactorySpec}</code>
     * creator(s). Others are taken from the 'structure' default parameters of the JAL Tools default configuration.
     * <ul>
     * <li><code>enmType = {@link #ENM_TYPE_DEF}</code>.</li>
     * <li><code>bolRandEnbl = {@link #BOL_RAND_ENBL_DEF}</code>.</li>
     * <li><code>lngSeed = {@link #LNG_RAND_SEED_DEF}</code>.</li>
     * <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long)}.</code></li>
     * <li><code>{@link #TensorFactorySpec(int[], ScalarFactorySpec)} = {@link #from(int[], ScalarFactorySpec)}.</code></li>
     * </ul>
     * </p>
     * 
     * @param   arrShape      the shape of the tensors produced
     * 
     * @return  a new <code>TensorFactorySpec</code> instance populated with the above argument(s)
     */
    public static TensorFactorySpec    from(int[] arrShape) {
        return TensorFactorySpec.from(arrShape, ENM_TYPE_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactorySpec</code> record configured according to the given argument(s).
     * </p>
     * <p>
     * This creator uses default values taken from the JAL Tools default configuration for fields not contained 
     * in the arguments.  Some default parameters are inherited from the <code>{@link ScalarFactorySpec}</code>
     * creator(s). Others are taken from the 'structure' default parameters of the JAL Tools default configuration.
     * <ul>
     * <li><code>bolRandEnbl = {@link #BOL_RAND_ENBL_DEF}</code>.</li>
     * <li><code>lngSeed = {@link #LNG_RAND_SEED_DEF}</code>.</li>
     * <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long)}.</code></li>
     * <li><code>{@link #TensorFactorySpec(int[], ScalarFactorySpec)} = {@link #from(int[], ScalarFactorySpec)}.</code></li>
     * </ul>
     * </p>
     * 
     * @param   arrShape      the shape of the tensors produced
     * @param   enmType       the data type of the tensor element scalar values to generate
     * 
     * @return  a new <code>TensorFactorySpec</code> instance populated with the above argument(s)
     */
    public static TensorFactorySpec    from(int[] arrShape, JalScalarType enmType) {
        return TensorFactorySpec.from(arrShape, enmType, BOL_RAND_ENBL_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactorySpec</code> record configured according to the given argument(s).
     * </p>
     * <p>
     * This creator uses default values taken from the JAL Tools default configuration for fields not contained 
     * in the arguments.  Some default parameters are inherited from the <code>{@link ScalarFactorySpec}</code>
     * creator(s). Others are taken from the 'structure' default parameters of the JAL Tools default configuration.
     * <ul>
     * <li><code>lngSeed = {@link #LNG_RAND_SEED_DEF}</code>.</li>
     * <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long)}</code>.</li>
     * <li><code>{@link #TensorFactorySpec(int[], ScalarFactorySpec)} = {@link #from(int[], ScalarFactorySpec)}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   arrShape      the shape of the tensors produced
     * @param   enmType       the data type of the tensor element scalar values to generate
     * @param   bolRandEnbl   enable/disable the use of random number generation for tensor element scalar values
     * 
     * @return  a new <code>TensorFactorySpec</code> instance populated with the above argument(s)
     */
    public static TensorFactorySpec    from(int[] arrShape, JalScalarType enmType, boolean bolRandEnbl) {
        ScalarFactorySpec   recSpec;
        if (bolRandEnbl)
            recSpec = ScalarFactorySpec.from(enmType, bolRandEnbl, LNG_RAND_SEED_DEF);
        else
            recSpec = ScalarFactorySpec.from(enmType, bolRandEnbl);
        
        return TensorFactorySpec.from(arrShape, recSpec);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactorySpec</code> record configured according to the given argument(s).
     * </p>
     * <p>
     * This creator uses default values taken from the JAL Tools default configuration for fields not contained 
     * in the arguments.  The default parameters are inherited from the <code>{@link ScalarFactorySpec}</code>
     * creator(s).
     * <ul>
     * <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long)}</code>.</li>
     * <li><code>{@link #TensorFactorySpec(int[], ScalarFactorySpec)} = {@link #from(int[], ScalarFactorySpec)}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   arrShape      the shape of the tensors produced
     * @param   enmType       the data type of the tensor element scalar values to generate
     * @param   bolRandEnbl   enable/disable the use of random number generation for tensor element scalar values
     * @param   lngSeed       seed value for random number generation or start value for incremental value generation   
     * 
     * @return  a new <code>TensorFactorySpec</code> instance populated with the above argument(s)
     */
    public static TensorFactorySpec    from(int[] arrShape, JalScalarType enmType, boolean bolRandEnbl, long lngSeed) {
        ScalarFactorySpec   recScalarSpec = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed);
        
        return TensorFactorySpec.from(arrShape, recScalarSpec);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactorySpec</code> record configured according to the given argument(s).
     * </p>
     * <p>
     * This creator uses default values taken from the JAL Tools default configuration for fields not contained 
     * in the arguments.  The default parameters are inherited from the <code>{@link ScalarFactorySpec}</code>
     * creator(s).
     * <ul>
     * <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long, Number)}</code>.</li>
     * <li><code>{@link #TensorFactorySpec(int[], ScalarFactorySpec)} = {@link #from(int[], ScalarFactorySpec)}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   arrShape      the shape of the tensors produced
     * @param   enmType       the data type of the tensor element scalar values to generate
     * @param   bolRandEnbl   enable/disable the use of random number generation for tensor element scalar values
     * @param   lngSeed       seed value for random number generation or start value for incremental value generation   
     * @param   numIncr       numeric incremental value used when random generation is disabled (type depends upon data type)
     * 
     * @return  a new <code>TensorFactorySpec</code> instance populated with the above argument(s)
     */
    public static TensorFactorySpec    from(int[] arrShape, JalScalarType enmType, boolean bolRandEnbl, long lngSeed, Number numIncr) {
        ScalarFactorySpec   recScalarSpec = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed, numIncr);
        
        return TensorFactorySpec.from(arrShape, recScalarSpec);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactorySpec</code> record configured according to the given argument(s).
     * </p>
     * <p>
     * This is a convenience method where the scalar factory specification record is created here then attached
     * to the returned tensor factory specification record.
     * </p>
     * <p>
     * This creator is equivalent to the creator <code>{@link #from(int[], ScalarFactorySpec)}</code> requiring all 
     * field values of the scalar factory specification record <code>{@link ScalarFactorySpec}</code>.   
     * The scalar factory specification record is used to configure the scalar factory generating all tensor elements.
     * <ul>
     * <li><code>{@link #recScalarSpec} = {@link ScalarFactorySpec#from(JalScalarType, boolean, long, Number, String)}</code>.</li>
     * <li><code>{@link #TensorFactorySpec(int[], ScalarFactorySpec)} = {@link #from(int[], ScalarFactorySpec)}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   arrShape      the shape of the tensors produced
     * @param   enmType       the data type of the tensor element scalar values to generate
     * @param   bolRandEnbl   enable/disable the use of random number generation for tensor element scalar values
     * @param   lngSeed       seed value for random number generation or start value for incremental value generation   
     * @param   numIncr       numeric incremental value used when random generation is disabled (type depends upon data type)
     * @param   strPrefix     prefix used for all string value generation (suffix given by integer value)
     * 
     * @return  a new <code>TensorFactorySpec</code> instance populated with the above argument(s)
     */
    public static TensorFactorySpec    from(int[] arrShape, JalScalarType enmType, boolean bolRandEnbl, long lngSeed, Number numIncr, String strPrefix) {
        ScalarFactorySpec   recScalarSpec = ScalarFactorySpec.from(enmType, bolRandEnbl, lngSeed, numIncr, strPrefix);
        
        return TensorFactorySpec.from(arrShape, recScalarSpec);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>TensorFactorySpec</code> record configured according to the given argument(s).
     * </p>
     * <p>
     * This creator is equivalent to the canonical constructor requiring all field values of the record.
     * </p>
     * 
     * @param   arrShape        the shape of the tensors produced 
     * @param   recScalarSpec   the scalar factory configuration for tensor element values
     * 
     * @return  a new <code>TensorFactorySpec</code> instance populated with the above argument(s)
     */
    public static TensorFactorySpec    from(int[] arrShape, ScalarFactorySpec recScalarSpec) {
        return new TensorFactorySpec(arrShape, recScalarSpec);
    }
    
    /**
     * <p>
     * Parses the argument collection for the field values of the returned <code>TensorFactorySpec</code> instance.
     * </p>
     * <p>
     * The argument collection is assumed to originate from an application command-line argument collection.
     * The <code>{@link TensorFactory}</code> class requires a 'shape' parameter and a 
     * <code>{@link ScalarFactory}</code> to create its element values.
     * </p>
     * <h2>Scalar Factory</h2>
     * The <code>{@link TensorFactory}</code> class requires a <code>{@link ScalarFactory}</code> instance.
     * The scalar factory is used to generate the field values
     * of all tree structure fields produced by the structure factory described by this configuration.
     * Note the configuration for the <code>{@link ScalarFactorySpec}</code> field <code>{@link #recScalarSpec}</code>
     * is potentially included in the argument collection; it not a default scalar factory is supplied.
     * </p>  
     * <p>
     * <h2>Format</h2>
     * The format of the argument collection is assumed to be
     * <pre>
     * > n1 [n2 [n3 ...]]...] [recScalarSpec]
     * </pre>
     * where
     * <ul>
     * <li>'n1' = size of the 1st axis.</li>
     * <li>'n2' = size of the 1st axis.</li>
     * <li>'n3' = size of the 1st axis.</li>
     * <li>'...' = sizes of the remaining axes.</li>
     * <li>'recScalarSpec' = configuration record for the scalar factory producing tensor element values.</li>
     * </ul>
     * Note that the tensor shape is determined by the values
     * 'n1, 'n2', 'n3', ..., etc.  These values are then used to pack the shape array 
     * <code>{@link #shape}</code> = { n1, n2, n3, ... }.
     * Thus, at least one argument element is required to specify a tensor shape, otherwise an exception is thrown.
     * </p>
     * <h2>Optional Arguments</h2>
     * The brackets indicate optional arguments.  The arguments are interpreted as follows:  
     * <ul>
     * <li>If the 'recScalarSpec' value is not present the argument is populated with the default scalar factory
     *     <code>{@link ScalarFactorySpec#from()}</code>.
     * </li>
     * <li>The shape of the tensor is determined by the number an values within the set {n1, n2, n3, ...}.  A tensor
     *     must have at least one axis, thus, the value 'n1' is required and are optional, 
     *     indicating higher-dimensional tensors.
     * </li>
     * </ul>
     * </p>
     * 
     * @param args  argument collection to be parsed, format as described above
     * 
     * @return  a new <code>TensorFactorySpec</code> record populated with the parsed argument values
     * 
     * @throws ConfigurationException           the tensor shape was invalid (e.g., an axis size could not be parsed, non-positive axis size, etc.)
     * @throws TypeNotPresentException          unknown <code>JalScalarType</code> enumeration constant
     * @throws NumberFormatException            invalid numeric format (bad 'numIncr' or 'lngSeed') 
     * @throws UnsupportedOperationException    unable to create <code>{@link #numIncr}</code> field for numeric value type  
     */
    public static TensorFactorySpec    parse(String...args) throws /* IllegalArgumentException, */ ConfigurationException, NumberFormatException, TypeNotPresentException, UnsupportedOperationException {
        
        // Check arguments
        if (args.length < 1)
//            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - Arguments must have at least 1 element: " + args);
            return TensorFactorySpec.from();
        
        // Parse through the argument values extracting the shape
        //  Parsing continues until a non-integer value is found (the beginning of the scalar factory configuration)
        List<Integer>   lstAxes = new LinkedList<>();
        int             indAxes = 0;
        for (String strArg : args) {
            
            try {
                Integer intAxis = Integer.valueOf(strArg);
                lstAxes.add(intAxis);
                indAxes++;
                
            } catch (NumberFormatException e) {
                break;
            }
        }
        
        // Check that at least one axis size was correctly parsed and all axis size are positive
        if (indAxes == 0)
            throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() + " - Arguments did not contain shape description: " + Arrays.asList(args));
        if (!lstAxes.stream().allMatch(i -> (i > 0)))
            throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple() + " - Shape specification contained non-positive axis size: " + lstAxes);
        
        // Converted the list of axes sizes to an int array
        int[]       shape = lstAxes.stream().mapToInt(i -> i).toArray();

        // Extract the remaining arguments from the original argument set
        //  These are the configuration parameters for the scalar factory
        String[]            arrScalCfg = Arrays.copyOfRange(args, indAxes, args.length);
        ScalarFactorySpec   recScalarSpec  = ScalarFactorySpec.parse(arrScalCfg); // throws TypeNotPresentException, NumericFormatException, UnsupportedOperationException
        
        return TensorFactorySpec.from(shape, recScalarSpec);
    }
    
    //
    // Operations
    //

    /**
     * <p>
     * Creates and returns a new <code>TensorFactory</code> according to this configuration.
     * </p>
     * 
     * @return  a new <code>TensorFactory</code> instance ready for simulated byte array creation
     */
    public TensorFactory    newFactory() {
        ScalarFactory       facValues = this.recScalarSpec.newFactory();
        
        return TensorFactory.from(this.arrShape, facValues);
    }

    
    // 
    // Record Overrides
    //
    
    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof TensorFactorySpec spec)  {
            boolean bolResult = (Arrays.equals(this.arrShape, spec.arrShape))
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
        str += "Tensor shape : " + Arrays.toString(this.arrShape) + "\n";
        str += "Scalar Factory Configuration \n";
        str += this.recScalarSpec.toString();
        
        return str;
    }
    
    
    //
    // Library Resources
    //
    
    /** The default parameters for scalar-valued simulated data generation */
    private static final JalToolsTensorFactoryConfig     CFG_DEF = JalToolsConfig.getInstance().datagen.values.tensor;
    
    
    //
    // Record Constants - Default Arguments
    //
    
    /** The default scalar value type when none is given */
    public final static JalScalarType  ENM_TYPE_DEF = CFG_DEF.elements.type;
    
    /** The default string value prefix */
    public final static int[]   ARR_SHAPE_DEF = CFG_DEF.shapeArray();
    
    /** The default enable/disable random number generator */
    public static final boolean BOL_RAND_ENBL_DEF = CFG_DEF.elements.random.enabled;

    /** The default random number generator seed value */
    public static final long    LNG_RAND_SEED_DEF = CFG_DEF.elements.random.seed;
    
}