package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;

import com.ospreydcs.dp.jal.common.BufferedImage;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.values.ImageFactory;
import com.ospreydcs.dp.jal.tools.config.JalToolsConfig;
import com.ospreydcs.dp.jal.tools.config.datagen.JalToolsDataGenConfig;

/**
 * <p> 
 * Record containing <code>ImageFactory</code> configuration parameters.
 * </p>
 * <p>
 * The <code>ImageFactory</code> class requires 3 parameters: 1) the image 'size', the image 'format', and
 * 3) the image name 'prefix'.
 * <ul>
 * <li>'size' is the number of bytes required of each image and is contained in field <code>{@link #size}</code>.</li>
 * <li>'format' is the file format of the image and contained in field <code>{@link #enmFmt}</code>.</li>
 * <li>'prefix' is the name prefix given to each image (full name suffixed by index) and contained in field <code>{@link #strPref()}</code>.</li>
 * </ul>
 * </p>
 * 
 * @param   intSize     image size (in bytes)
 * @param   enmFormat   image format
 * @param   strPrefix   image name prefix (full image name is appended with index)
 */
public record ImageFactorySpec(int intSize, BufferedImage.Format enmFormat, String strPrefix) {
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new <code>ImageFactorySpec</code> configuration for a <code>ImageFactory</code>
     * with all default parameters.
     * </p>
     * <h2>Default Parameters</h2>
     * This creator uses default parameters values from the JAL Tools default configuration.
     * The following values are used:  
     * <ul>
     * <li><code>{@link #size} = {@link #INT_SIZE_DEF}</code>.</li>
     * <li><code>{@link #enmFmt} = {@link #ENM_FMT_DEF}</code>.</li>
     * <li><code>{@link #strPref} = {@link #STR_PREF_DEF}</code>.</li>
     * </ul>
     * </p>
     * 
     * @return  a new <code>ImageFactorySpec</code> configuration populated with all default parameters
     */
    public static ImageFactorySpec from() {
        return ImageFactorySpec.from(INT_SIZE_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ImageFactorySpec</code> configuration for a <code>ImageFactory</code>.
     * </p>
     * <h2>Default Parameters</h2>
     * This creator uses default parameters values from the JAL Tools default configuration.
     * The following values are used:  
     * <ul>
     * <li><code>{@link #enmFmt} = {@link #ENM_FMT_DEF}</code>.</li>
     * <li><code>{@link #strPref} = {@link #STR_PREF_DEF}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   intSize    image size (in bytes)
     * 
     * @return  a new <code>ImageFactorySpec</code> configuration populated with the given arguments
     */
    public static ImageFactorySpec from(int intSize) {
        return ImageFactorySpec.from(intSize, ENM_FMT_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ImageFactorySpec</code> configuration for a <code>ImageFactory</code>.
     * </p>
     * <h2>Default Parameters</h2>
     * This creator uses default parameters values from the JAL Tools default configuration.
     * The following values are used:  
     * <ul>
     * <li><code>{@link #strPref} = {@link #STR_PREF_DEF}</code>.</li>
     * </ul>
     * </p>
     * 
     * @param   intSize    image size (in bytes)
     * @param   enmFormat  image format
     * 
     * @return  a new <code>ImageFactorySpec</code> configuration populated with the given arguments
     */
    public static ImageFactorySpec from(int intSize, BufferedImage.Format enmFormat) {
        return ImageFactorySpec.from(intSize, enmFormat, STR_PREF_DEF);
    }
    
    /**
     * <p>
     * Creates and returns a new <code>ImageFactorySpec</code> configuration for a <code>ImageFactory</code>.
     * </p>
     * <p>
     * This creator is equivalent to the canonical constructor containing all required field values.
     * </p>
     * 
     * @param   intSize    image size (in bytes)
     * @param   enmFormat  image format
     * @param   strPrefix  image name prefix (full image name is appended with index)
     * 
     * @return  a new <code>ImageFactorySpec</code> configuration populated with the given arguments
     */
    public static ImageFactorySpec from(int intSize, BufferedImage.Format enmFormat, String strPrefix) {
        return new ImageFactorySpec(intSize, enmFormat, strPrefix);
    }
    
    /**
     * <p>
     * Parses the argument collection for the field values of the returned <code>ImageFactorySpec</code> instance.
     * </p>
     * <p>
     * The argument collection is assumed to originate from an application command-line argument collection.
     * The <code>{@link ImageFactory}</code> class has three parameters: 1) image 'size', 2) image 'format', and
     * 3) image name 'prefix'.
     * </p>
     * <p>
     * <h2>Format</h2>
     * The format of the arguments is the following :
     * <ul>
     * <pre>
     * <li>  > [size [format [prefix]]]</li>
     * </pre>
     * </ul>
     * where
     * <ul>
     * <li>'size' = size of images produced (in bytes) (int value),</li>
     * <li>'format' = image format (<code>{@link BufferedImage#Format})</code>,</li>
     * <li>'prefix' = name prefix given to all images produced (full name appended by index).</li>
     * </ul>
     * </p>
     * <p>
     * <h2>Optional Arguments</h2>
     * The brackets indicate optional values in the argument collection.  If not present they are populated with
     * the default values of the JAL Tools default configuration.
     * <ul>
     * <li>'size' = <code>{@link #INT_SIZE_DEF}</code>.</li>
     * <li>'format' = <code>{@link #ENM_FMT_DEF}</code>.</li>
     * <li>'prefix' = <code>{@link #STR_PREF_DEF}</code>.</li>
     * </ul>
     * </p>
     * <p>
     * Note that optional parameters are ordered and nested.  Due to the nature of string parsing the 
     * ordering must be respected.  For example, to include the 'prefix' parameter all other parameters
     * must be supplied.
     * </p>  
     * 
     * @param args  argument collection to be parsed, format as described above
     * 
     * @return  a new <code>TimestampFactorySpec</code> record populated with the parsed argument values
     * 
     * @throws NumberFormatException    invalid numeric format for the 'size' parameter 
     * @throws TypeNotPresentException  the 'format' was unrecognized (i.e., not a {@link BufferedImage#Format} constant) 
     */
    public static ImageFactorySpec parse(String...args) throws NumberFormatException, TypeNotPresentException {
        
        if (args.length < 1)
            return ImageFactorySpec.from();
        
        int     intSize = Integer.valueOf(args[0]);     // throws NumberFormatException
        if (args.length < 2)
            return ImageFactorySpec.from(intSize);
        
        BufferedImage.Format    enmFmt = BufferedImage.Format.getConstant(args[1]); // throws TypeNotPresentException
        if (args.length < 3) 
            return ImageFactorySpec.from(intSize, enmFmt);
        
        String  strPref = args[2];
        return ImageFactorySpec.from(intSize, enmFmt, strPref);
    }
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates and returns a new <code>ImageFactory</code> according to this configuration.
     * </p>
     * 
     * @return  a new <code>ImageFactory</code> instance ready for simulated image creation
     */
    public ImageFactory newFactory() {
        return ImageFactory.from(this.intSize, this.enmFormat, this.strPrefix);
    }
    
    
    // 
    // Record Overrides
    //
    
    /**
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof ImageFactorySpec spec)  {
            boolean bolResult = (this.intSize == spec.intSize)
                    && (this.enmFormat == spec.enmFormat)
                    && (this.strPrefix.equals(spec.strPrefix));
            return bolResult;
        }
        
        return false;
    }

    /**
     * @see java.lang.Record#toString()
     */
    @Override
    public String toString() {
        String  str = "(";
        
        str += "Class=" + this.getClass().getSimpleName() + ", ";
        str += "Sizes (bytes)=" + this.intSize + ", ";
        str += "Format=" + this.enmFormat + ", ";
        str += "Prefix=" + this.strPrefix + ")";
        
//        str += "Image size (bytes) : " + this.intSize + "\n";
//        str += "Image format       : " + this.enmFormat + "\n";
//        str += "Image name prefix  : " + this.strPrefix + "\n";
        
        return str;
    }

    
    //
    // JAL Library Resources
    //
    
    /** JAL Tools default configuration parameters for datum factories */
    private static final JalToolsDataGenConfig.Values   CFG_VAL_DEF = JalToolsConfig.getInstance().datagen.values;
    
    
    // 
    // Record Constants - Default Values
    //
    
    /** Image factory default image size (in bytes) */
    public static final int                     INT_SIZE_DEF = CFG_VAL_DEF.image.size;
    
    /** Image factory default image format */
    public static final BufferedImage.Format    ENM_FMT_DEF = CFG_VAL_DEF.image.format;
    
    /** Image factory default value for image prefix */
    public static final String                  STR_PREF_DEF = CFG_VAL_DEF.image.namePrefix;
    
}