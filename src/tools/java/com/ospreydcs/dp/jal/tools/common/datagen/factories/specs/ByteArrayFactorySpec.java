package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ByteArrayFactory;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.JalToolsDataGenConfig;

/**
 * <p> 
 * Record containing <code>ByteArrayFactory</code> configuration parameters.
 * </p>
 * <p>
 * The <code>{@link ByteArrayFactory}</code> class has a simple configuration requiring only a single
 * parameter the array 'size'.  This parameter is the number of bytes contained in each byte array
 * produced.  Each byte array contains random values, the byte values of the heap at the time of allocation.
 * </p>  
 * 
 * @param szArrays  the number of bytes in each byte array
 * 
 * @see ByteArrayFactory
 */
public record ByteArrayFactorySpec(int szArrays) {
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>ByteArrayFactorySpec</code> configuration for a <code>ByteArrayFactory</code>
     * with default parameters.
     * </p>
     * <p>
     * The <code>ByteArrayFactory</code> instances produced from this configuration 
     * (i.e., see <code>{@link #newFactory()}</code> all produce byte arrays with size <code>{@link #szArrays()}</code>.
     * </p> 
     * <p>
     * <h2>Default Parameters</h2>
     * This creator uses default parameters values from the JAL Tools default configuration.
     * The following values are used:  
     * <ul>
     * <li><code>{@link #szArrays()} = {@link #INT_SIZE_DEF}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param szArrays  the number of bytes (array size) of all byte arrays produced by factory configuration
     * 
     * @return  a new <code>ByteArrayFactorySpec</code> populated with default argument
     */
    public static ByteArrayFactorySpec from() {
        return ByteArrayFactorySpec.from(INT_SIZE_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ByteArrayFactorySpec</code> configuration for a <code>ByteArrayFactory</code>.
     * </p>
     * <p>
     * The <code>ByteArrayFactory</code> instances produced from this configuration 
     * (i.e., see <code>{@link #newFactory()}</code> all produce byte arrays with size <code>{@link #szArrays()}</code>.
     * </p> 
     * 
     * @param szArrays  the number of bytes (array size) of all byte arrays produced by factory configuration
     * 
     * @return  a new <code>ByteArrayFactorySpec</code> populated with the given argument
     */
    public static ByteArrayFactorySpec from(int szArrays) {
        return new ByteArrayFactorySpec(szArrays);
    }
    
    /**
     * <p>
     * Parses the argument collection for the field values of the returned <code>ByteArrayFactorySpec</code> instance.
     * </p>
     * <p>
     * The argument collection is assumed to originate from an application command-line argument collection.
     * The <code>{@link ByteArrayFactorySpec}</code> specification is quite simple requiring only a single 
     * configuration parameter, the size of the arrays produced. 
     * <p>
     * <h2>Format</h2>
     * The format of the argument collection is assumed to be
     * <pre>
     * > [size]
     * </pre>
     * where
     * <ul>
     * <li>'size' = number of bytes in each byte array.</li>
     * </ul>
     * </p>
     * <p>
     * <h2>Optional Arguments</h2>
     * The brackets indicate optional arguments.  If not present the argument is populated with the default
     * values within the JAL Tools default configuration <code>{@link #INT_SIZE_DEF}</code>.
     * </p>
     * 
     * @param args  argument collection to be parsed, format as described above
     * 
     * @return  a new <code>ByteArrayFactorySpec</code> record populated with the parsed argument values
     * 
     * @throws NumberFormatException    the 'count' value could not be parsed
     */
    public static ByteArrayFactorySpec parse(String...args) throws NumberFormatException {
    
        if (args.length < 1)
            return ByteArrayFactorySpec.from();
        
        int cntBytes = Integer.valueOf(args[0]);    // throws NumberFormatException
        return ByteArrayFactorySpec.from(cntBytes);
    }
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates and returns a new <code>ByteArrayFactory</code> according to this configuration.
     * </p>
     * 
     * @return  a new <code>ByteArrayFactory</code> instance ready for simulated byte array creation
     */
    public ByteArrayFactory newFactory() {
        return ByteArrayFactory.from(this.szArrays);
    }
    

    //
    // Record Overrides
    //
    
    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ByteArrayFactorySpec spec)
            return this.szArrays == spec.szArrays;
        
        return false;
    }

    /**
     * @see java.lang.Record#toString()
     */
    @Override
    public String toString() {
        String  str = "(";
        
        str += "Class=" + this.getClass().getSimpleName() + ", ";
        str += "Size (bytes)=" + this.szArrays + ")";
        
//        str += "Array size (bytes) : " + this.szArrays + "\n";
        
        return str;
    }

    
    //
    // JAL Library Resources
    //
    

    /** JAL Tools default configuration parameters for datum factories */
    private static final JalToolsDataGenConfig.Values   CFG_DEF = JalToolsConfig.getInstance().datagen.values;
    
    // 
    // Record Constants - Default Values
    //
    
    /** Byte array factory default byte array size (in bytes) */
    public static final int         INT_SIZE_DEF = CFG_DEF.bytes.size;
    
}