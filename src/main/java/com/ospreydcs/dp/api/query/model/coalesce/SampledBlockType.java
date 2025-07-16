package com.ospreydcs.dp.api.query.model.coalesce;

import com.ospreydcs.dp.api.query.model.superdom.SampledBlockSuperDom;

/**
 * <p>
 * Enumeration of possible <code>SampledBlock</code> sub-class types.
 * </p>
 * <p>
 * The enumeration contains constants for all supported <code>SampledBlock</code> child classes.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * The constant <code>{@link #BASE_CLASS}</code> is used for exceptional situations or where an
 * unsupported type is encountered.
 * </li>
 * <li>
 * Use method <code>{@link #getBlockType(SampledBlock)}</code> to retrieve the block type constant
 * from a <code>SampledBlock</code> object.
 * </li>
 * </ul>
 * 
 * @author Christopher K. Allen
 * @since Jul 9, 2025
 */
public enum SampledBlockType {
    
    /** 
     * A <code>SampledBlockClocked</code> instance. 
     */
    CLOCKED(SampledBlockClocked.class),
    
    /** 
     * A <code>SampledBlockTmsList</code> instance. 
     */
    TMS_LIST(SampledBlockTmsList.class),
    
    /** 
     * A <code>SampledBlockSuperDom</code> instance. 
     */
    SUPER_DOM(SampledBlockSuperDom.class),
    
    /** 
     * The exceptional case 
     */
    BASE_CLASS(SampledBlock.class),
    
    ;
    
    //
    // Enumeration Constant Attributes
    //
    
    /** The <code>SampledBlock</code> sub-class associated with this constant. */
    private final   Class<? extends SampledBlock>   clsType;
    
    //
    // Constant Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>SampledBlockType</code> instance.
     * </p>
     *
     * @param clsType   the <code>SampledBlock</code> sub-type represented by this constant
     */
    private SampledBlockType(Class<? extends SampledBlock> clsType) { this.clsType = clsType; };
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Returns the <code>SampledBlock</code> sub-type associated with this enumeration constant.
     * </p>
     * 
     * @return  <code>SampledBlock</code> Java sub-class represented by this constant 
     */
    public Class<? extends SampledBlock>    getBlockClass()  { return this.clsType; };
    
    /**
     * <p>
     * Returns the <code>SampledBlockType</code> constant corresponding to the class type of the argument.
     * </p>
     * <p>
     * Returns the value <code>{@link #BASE_CLASS}</code> if the argument is not recognized as a sub-class
     * of <code>SampledBlock</code>.
     * </p>
     * 
     * @param <T>       class type of the argument
     * @param objBlk    a <code>SampledBlock</code> sub-class object
     * 
     * @return  the <code>SampledBlockType</code> constant corresponding to the class type of the argument 
     */
    public static <T extends SampledBlock > SampledBlockType    getBlockType(T objBlk) {
        for (SampledBlockType enmType : SampledBlockType.values()) {
            Class<? extends SampledBlock>    clsEnm = enmType.getBlockClass();
            Class<? extends SampledBlock>    clsBlk = objBlk.getClass();
            
            if (clsEnm.isAssignableFrom(clsBlk))
                return enmType;
        }
        
        return SampledBlockType.BASE_CLASS;
    }
}