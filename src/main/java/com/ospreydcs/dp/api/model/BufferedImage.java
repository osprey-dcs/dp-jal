/*
 * Project: dp-api-common
 * File:	BufferedImage.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	BufferedImage
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
 * @since Oct 14, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;

import org.epics.nt.NTNDArray;
import org.epics.pvdata.pv.PVStructure;

import com.google.common.io.Files;
import com.ospreydcs.dp.api.util.Epics;
import com.ospreydcs.dp.grpc.v1.common.Image.FileType;

/**
 * <p>
 * Encapsulation of a buffered image.
 * </p>
 * <p>
 * A buffered image maintains all image data in a single byte array buffer.  Interpretation
 * for image rendering is specified by either a known image format convention (e.g., JPEG, TIFF, SVG, etc.)
 * or given explicitly using dimensional attributes.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Oct 14, 2022
 *
 */
public class BufferedImage {
    
    //
    // Attributes
    //
    
    /** The name of the image (if any) */
    private String              strName = null;
    
    /** The image timestamp (if any) */
    private Instant             insTms = null;
    
    /** The image format type (if any) */
    private Format              fmtImage = null;
    
    /** List of image dimensions */
    private List<ArrayDimension>     lstDims = null;
    
    /** Storage for the image */
    private byte[]              arrData = null;

    
    //
    // Class Types
    //
    
    /**
     * <p>
     * Enumeration of image formats.
     * </p>
     * <p>
     * The enumeration includes most standard image format types as well as
     * custom, user-specific formats.  In the latter case the dimensions attribute
     * of the image are available (see <code>BufferedImage#getDimensions</code>)
     * </p>  
     * <p>
     * Includes a back reference to the Data Platform <code>FileType</code>
     * Protobuf enumeration message for convenience.
     * </p>
     *
     * @author Christopher K. Allen
     * @since Oct 14, 2022
     * 
     * @see BufferedImage#getDimensions()
     */
    public enum Format {       
        /** Unprocessed image format */
        RAW("raw", FileType.RAW),   
        /** Joint Photographic Experts Group (lossy compr.) - JPEG */
        JPEG("jpg", FileType.JPEG), 
        /** Graphics Interchange Format - GIF */
        GIF("gif", FileType.GIF),   
        /** Tagged BufferedImage File Format - TIFF */
        TIFF("tif", FileType.TIFF), 
        /** Bitmap image file - BMP */
        BMP("bmp", FileType.BMP),   
        /** Portable Network Graphics - PNG */
        PNG("png", FileType.PNG),   
        /** Encapsulated Post-Script - EPS */
        EPS("eps", FileType.EPS),   
        /** Scalable Vector Format - SVG */
        SVG("svg", FileType.SVG),   
        /** Portable Document Format - JPEG */
        PDF("pdf", FileType.PDF),   
        /** From EPICS NTNDArray - User specified format */
        NTND("ntnd", null),          
        /** User image format - See {@link BufferedImage#getDimensions()} */
        CUSTOM("img", null),        
        /** Format not specified - Unknown, unspecified, or unsupported format */
        UNKNOWN(null, FileType.UNRECOGNIZED);
        
        //
        // Constant Attributes
        // 
        
        /** file extension (for image file) */
        private final String    strFileExt;
        
        /** value of the <code>FileType</code> message enumeration constant */
        private final FileType enmProto;

        /** Initializing constructor */
        private Format(String strFileExt, FileType intMsgNum) { this.strFileExt = strFileExt; this.enmProto = intMsgNum; };
        
        //
        // Getters 
        //
        
        /** Returns the file extension for the format */
        public String   getFileExtension()  { return this.strFileExt; };
        
        /** Returns the Data Platform <code>FileType</code> message enumeration value */
        public FileType getProtoEnum() { return this.enmProto; }

        //
        // Conversion
        //
        
        /**
         * Return the <code>Format</code> constant for the given file extension or 
         * <code>UNKNOWN</code> if not supported. 
         */
        public static Format from(String strFileExt) {
            for (Format fmt : Format.values())
                if (fmt.getFileExtension().equals(strFileExt))
                    return fmt;
            return UNKNOWN;
        }
        
        /** 
         * Return the <code>Format</code> constant for the gRPC <code>FileType</code> message,
         *  or <code>null</code> if not supported
         */
        public static Format from(FileType msgFileType) {
            for (Format enm : Format.values())
                if (enm.getProtoEnum() == msgFileType)
                    return enm;
            return null;
        }
    }
    
    
    
//    /**
//     * <p>
//     * Record class containing the dimension attributes for a user formatted image.
//     * </p>
//     *
//     * @author Christopher K. Allen
//     * @since Oct 14, 2022
//     *
//     */
//    public class ArrayDimension {
//
//        /** Number of image element values in the dimension */
//        private Integer cntVals;
//        
//        /** Offset (byte) index into the axis before image values start */
//        private Integer indOffset;
//        
//        /** Full size of the dimension including offset and all image element bins */
//        private Integer szFull;
//        
//        /** Size of the image element bin - for example a pixel (in bytes) */
//        private Integer szBin;
//        
//        /** Value order reversed flag */
//        private Boolean bolReversed;
//        
//        
//        /**
//         * <p>
//         * Constructs a new, intialized instance of <code>ArrayDimension</code>.
//         * </p>
//         *
//         */
//        public ArrayDimension(Integer cntVals, Integer indOffset, Integer szFull, Integer szBin, Boolean bolReversed) {
//            this.cntVals = cntVals;
//            this.indOffset = indOffset;
//            this.szFull = szFull;
//            this.szBin = szBin;
//            this.bolReversed = bolReversed;
//        }
//    }
    
//    //
//    // Data Conversion
//    //
//    
//    /**
//     * <p>
//     * Creates a new Data Platform Protobuf message from the contents of the
//     * given argument.
//     * </p>
//     * <p>
//     * The image data and the image format are taken from the <code>BufferedImage</code> 
//     * to create the returned gRPC message.
//     * If the argument has not been initialized an exception is thrown.
//     * If the format attribute is one that is not supported by the Data Platform
//     * an exception is thrown.
//     * </p>
//     * 
//     * @param img   image containing source data for the returned gRPC message
//     * 
//     * @return new <code>Image</code> gRPC message initialized from the given argument data
//     * 
//     * @throws UnsupportedOperationException either the image was uninitialized or the image format was unrecognized
//     */
//    public static Image createGrpcMsg(BufferedImage img) throws UnsupportedOperationException {
//        return img.createGrpcMessage();
//    }
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new <code>BufferedImage</code> instance initialized with the argument, assumed to be an EPICS
     * <code>NTNDArray</code> containing image data.
     * </p>
     * <p>
     * </p>
     * 
     * @param pvsNdArray    EPICS NTNDArray compatible structure containing image data
     * 
     * @return a new <code>BufferedImage</code> instance containing the image data of the argument
     * 
     * @throws IllegalArgumentException the argument was not NTNDArray compatible
     * @throws MissingResourceException the argument was missing data required for image creation (see message)
     */
    public static BufferedImage from(PVStructure pvsNdArray) throws IllegalArgumentException, MissingResourceException {
        
        // Check for NT array compatibility
        NTNDArray ntNdArray = NTNDArray.wrap(pvsNdArray);
        if (ntNdArray == null)
            throw new IllegalArgumentException("BufferedImage#from(PVStructure) - argument was incompatible with NTNDArray");

        // Extract the image data
        byte[]                  arrRawData = Epics.extractArrayRawData(pvsNdArray);
        List<ArrayDimension>    lstDims = Epics.extractArrayDimensions(pvsNdArray); // throws MissingResourceException
        
        // Extract optional data
        Instant     insTms = Epics.extractArrayDataTimestamp(pvsNdArray);
        String      strLbl = Epics.extractName(pvsNdArray);
        
        // Create the image and return it
        BufferedImage   image = new BufferedImage(strLbl, insTms, Format.NTND,lstDims, arrRawData);
        
        return image;
    }
    
    /**
     * <p>
     * Creates a new, fully-initialized instance of <code>BufferedImage</code>
     * populated with the given argument data.
     * </p>
     * 
     * @param strName  image name
     * @param insTms   image timestamp
     * @param fmtImage image format
     * @param lstDims  list of image dimensions (for custom formats)
     * @param arrData  the image data
     * 
     * @return new, initialized image populated with data from the arguments
     */
    public static BufferedImage from(String strName, Instant insTms, Format fmtImage, List<ArrayDimension> lstDims, byte[] arrData) {
        return new BufferedImage(strName, insTms, fmtImage, lstDims, arrData);
    }
    
    /**
     * <p>
     * Creates a new <code>BufferedImage</code> instance from the given file.
     * </p>
     * <p>
     * The argument is assumed to be an image file in known format.  The image format is
     * determined by the file extension.   No dimensional attributes are assigned.
     * </p>
     * 
     * @param file  image file to be loaded into new <code>BufferedImage</code> instance
     * 
     * @return a new <code>BufferedImage</code> populated from the given image file
     * 
     * @throws IllegalArgumentException the file size is larger than the largest byte array
     * @throws IOException              an I/O error occurred during file read
     */
    public static BufferedImage from(File file) throws IllegalArgumentException, IOException {
        String  strName = file.getName();
        
        String  strExten = Files.getFileExtension(strName);
        Format  fmtFile = Format.from(strExten);
        
        long    lngMsecs = file.lastModified();
        Date    dateTms = new Date(lngMsecs);
        Instant insTms = dateTms.toInstant();

        byte[]  arrData = Files.toByteArray(file);
     
        return new BufferedImage(strName, insTms, fmtFile, arrData);
    }
    
    
    //
    // Constructors

    /**
     * <p>
     * Constructs a new, fully-initialized, instance of <code>BufferedImage</code>.
     * populated with the given argument data.
     * </p>
     *
     * @param strName  image name
     * @param insTms   image timestamp
     * @param fmtImage image format
     * @param lstDims  list of image dimensions (for custom formats)
     * @param arrData  the image data
     */
    public BufferedImage(String strName, Instant insTms, Format fmtImage, List<ArrayDimension> lstDims, byte[] arrData) {
        this.strName = strName;
        this.insTms = insTms;
        this.fmtImage = fmtImage;
        this.lstDims = lstDims;
        this.arrData = arrData;
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>BufferedImage</code> from raw data and an image format.
     * </p>
     * <p>
     * This constructor is appropriate for represented file images in a known image format where
     * dimensional attributes are assumed by format.
     * </p>
     *
     * @param strName  image name
     * @param insTms   image timestamp
     * @param fmtImage image format
     * @param arrData  the image data
     */
    public BufferedImage(String strName, Instant insTms, Format fmt, byte[] arrData) {
       this(strName, insTms, fmt, new LinkedList<ArrayDimension>(), arrData); 
    }

    /**
     * <p>
     * Constructs a new, uninitialized instance of <code>BufferedImage</code>.
     * </p>
     * <p>
     * All attributes are left unspecified, empty, or <code>null</code>.
     * </p>
     */
    public BufferedImage() {
        this(null, null, Format.UNKNOWN, new LinkedList<ArrayDimension>(), null);
    }
    
    
    //
    // Operations
    //
    
    //
    // Attribute Query (Getters)
    //

    /**
     * Return the name of the image, or <code>null</code> if not set.
     * 
     * @return the current image name
     */
    public String getName() {
        return this.strName;
    }

    /**
     * Returns the timestamp of the image, or <code>null</code> if none
     * has been set.
     * 
     * @return the image timestamp
     */
    public Instant getTimestamp() {
        return this.insTms;
    }

    /**
     * Return the image format.
     * 
     * @return the current value of property fmtImage
     */
    public Format getFormat() {
        return this.fmtImage;
    }

    /**
     * Returns the list of image dimension attributes.
     * If the image has a user-specified format (@see {@link Format#CUSTOM}}
     * then the dimensions list may be used.  If the image is in a standard
     * format this attribute may not be used.
     * 
     * @return the list of image dimension attributes, 
     *         may be empty if unused
     *         
     * @see Format
     */
    public List<ArrayDimension> getDimensions() {
        return lstDims;
    }

    /**
     * Returns the data for the image as a raw byte array.  The format attribute
     * specifies how the byte array is translated (or the dimensions list for
     * custom formats).
     * 
     * @return image data byte array
     * 
     * @see #getFormat()
     * @see #getDimensions()
     */
    public byte[] getData() {
        return arrData;
    }

    
    //
    // Attribute Setters
    // 
    
    /**
     * Sets image name. Overwrites the previous name if set.
     * 
     * @param strName new image name 
     */
    public void setStrName(String strName) {
        this.strName = strName;
    }

    /**
     * Sets the timestamp for the image.  Overwrites the previous timestamp
     * if it was set.
     * 
     * @param insTms new image timestamp 
     */
    public void setTimestamp(Instant insTms) {
        this.insTms = insTms;
    }

    /**
     * Sets the image format.  Overwrites the previous format constant if it
     * was set.
     * 
     * @param fmtImage new image format identifier 
     */
    public void setTypImage(Format fmtImage) {
        this.fmtImage = fmtImage;
    }

    /**
     * Set the list of dimension attributes for the image.  Presumably
     * each element in the list represents the attributes of a single
     * image dimension.  For images in a non-standard format 
     * (see <code>{@link Format#CUSTOM}, {@link Format#NTND}</code>) 
     * the dimension attributes may be used to specify parameters.
     *  
     * @param lstDims new dimensions list for the image
     * 
     * @see Format
     */
    public void setLstDims(List<ArrayDimension> lstDims) {
        this.lstDims = lstDims;
    }

    /**
     * Sets the image data.  The raw image data is maintained as a 
     * simple byte array.  The image format specifies how the byte array
     * is translated (or the dimensions list for custom formats).
     * 
     * @param arrData sets name value of property arrData 
     * 
     * @see #getFormat()
     * @see #getDimensions()
     */
    public void setData(byte[] arrData) {
        this.arrData = arrData;
    }
    
    

}
