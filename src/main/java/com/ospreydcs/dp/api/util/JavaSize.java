/*
 * Project: dp-api-common
 * File:	JavaSize.java
 * Package: com.ospreydcs.dp.api.util
 * Type: 	JavaSize
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
 * @since Oct 18, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Instant;

import com.ospreydcs.dp.api.model.BufferedImage;


/**
 * Utility class for estimating the memory allocation for Java objects.
 *
 * @author Christopher K. Allen
 * @since Oct 18, 2022
 *
 */
public class JavaSize {
    
    //
    // Class Constants
    //
    
    // Primitive type sizes (in bytes)
    public static final long SZ_byte    = 1; // Byte.BYTES
    public static final long SZ_boolean = 1; // Boolean.BYTES
    public static final long SZ_char    = 2; // etc.
    public static final long SZ_short   = 2;
    public static final long SZ_int     = 4;
    public static final long SZ_float   = 4;
    public static final long SZ_long    = 8;
    public static final long SZ_double  = 8;
    
    // Array headers. length, and reference fields
    public static final long SZ_ARR_HDR   = 12;
    public static final long SZ_ARR_LEN   = 4;
    public static final long SZ_ARR_RSRVD = SZ_ARR_HDR + SZ_ARR_LEN;
    
    public static final long SZ_ARR_REFS  = 4;
    
    // Java Object and Objects Header 
    public static final long SZ_Object    = 16;
    
    // Java Class reserved bytes 
    public static final long SZ_Class_RSRVD = 16;
    public static final long SZ_Enum_RSRVD  = 24 + SZ_Class_RSRVD;

    // String attribute sizes 
    public static final long SZ_STR_HDR   = 12;
    public static final long SZ_STR_HASH  = 4;
    public static final long SZ_STR_REF   = 4;
    public static final long SZ_STR_LEN   = 24;
    public static final long SZ_STR_RSRVD = SZ_STR_HDR+SZ_STR_HASH+SZ_STR_REF+SZ_STR_LEN;
    
    // Wrapped primitive sizes (in bytes) - includes storage, header, and gap
    public static final long SZ_Byte      = 16;
    public static final long SZ_Boolean   = 16;
    public static final long SZ_Character = 16;
    public static final long SZ_Short     = 16;
    public static final long SZ_Integer   = 16;
    public static final long SZ_Float     = 16;
    public static final long SZ_Long      = 24;
    public static final long SZ_Double    = 24;
//    public static final long SZ_Byte      = Byte.BYTES;
//    public static final long SZ_Boolean   = 16;
//    public static final long SZ_Character = Character.BYTES;
//    public static final long SZ_Short     = Short.BYTES;
//    public static final long SZ_Integer   = Integer.BYTES;
//    public static final long SZ_Float     = Float.BYTES;
//    public static final long SZ_Long      = Long.BYTES;
//    public static final long SZ_Double    = Double.BYTES;
    
    // Miscellaneous objects
    public static final long SZ_Instant   = 24;
//    public static final long SZ_NtDimension = 4*SZ_Integer + 1*SZ_Boolean + SZ_Class_RSRVD;
    public static final long SZ_ImageDimension = SZ_Integer;


    /**
     * <p>
     * Returns a conservative estimate of the memory allocation for the 
     * given argument, in bytes.
     * </p>
     * <p>
     * The returned estimated can be larger than the actual allocation size.
     * The estimated is calculated based upon a 64-bit architecture.  
     * Sizes for primitive array objects are calculated according to their 
     * array size and header size.
     * </p>
     * <p>
     * If the argument implements the <code>Serializable</code> interface,
     * in particular objects of Java <code>Collections</code>, then an 
     * overestimate of the allocation for the entire collection is provided 
     * by serializing it to a byte stream.
     * </p>
     * <p>
     * If the argument has value <code>null</code> a value 0L is returned.
     * </p>
     *  
     * @param o object 
     * 
     * @return approximate memory allocation for the given argument
     * 
     * @throws IllegalArgumentException the argument type was not recognized, 
     *                                  or IO error in serialization (see source)
     * 
     * @see #serialSizeof(Serializable)
     */
    public static long sizeof(Object o) throws IllegalArgumentException {
        if (o == null) return 0L;
        if (o instanceof Boolean) return SZ_Boolean;
        if (o instanceof Byte) return SZ_Byte;
        if (o instanceof Character)  return SZ_Character;
        if (o instanceof Short) return SZ_Short;
        if (o instanceof Integer) return SZ_Integer;
        if (o instanceof Float) return SZ_Float;
        if (o instanceof Long) return SZ_Long;
        if (o instanceof Double) return SZ_Double;
        
        if (o instanceof boolean[] a) return a.length*SZ_boolean + SZ_ARR_RSRVD;
        if (o instanceof byte[] a) return a.length*SZ_byte + SZ_ARR_RSRVD;
        if (o instanceof char[] a) return a.length*SZ_char + SZ_ARR_RSRVD;
        if (o instanceof short[] a) return a.length*SZ_short + SZ_ARR_RSRVD;
        if (o instanceof int[] a) return a.length*SZ_int + SZ_ARR_RSRVD;
        if (o instanceof float[] a) return a.length*4 + SZ_ARR_RSRVD;
        if (o instanceof long[] a) return a.length*SZ_long + SZ_ARR_RSRVD;
        if (o instanceof double[] a) return a.length*SZ_double + SZ_ARR_RSRVD;
        
        if (o instanceof Boolean[] a) return a.length*(SZ_boolean + SZ_ARR_REFS) + SZ_ARR_RSRVD;
        if (o instanceof Byte[] a) return a.length*(SZ_byte + SZ_ARR_REFS) + SZ_ARR_RSRVD;
        if (o instanceof Character[] a) return a.length*(SZ_char + SZ_ARR_REFS) + SZ_ARR_RSRVD;
        if (o instanceof Short[] a) return a.length*(SZ_short + SZ_ARR_REFS) + SZ_ARR_RSRVD;
        if (o instanceof Integer[] a) return a.length*(SZ_int + SZ_ARR_REFS) + SZ_ARR_RSRVD;
        if (o instanceof Float[] a) return a.length*(SZ_float + SZ_ARR_REFS) + SZ_ARR_RSRVD;
        if (o instanceof Long[] a) return a.length*(SZ_long + SZ_ARR_REFS) + SZ_ARR_RSRVD;
        if (o instanceof Double[] a) return a.length*(SZ_double + SZ_ARR_REFS) + SZ_ARR_RSRVD;
        
        if (o instanceof String s) return stringSizeof(s);
        if (o instanceof Instant) return SZ_Instant;
//        if (o instanceof ArrayDimension) return SZ_ImageDimension;
        if (o instanceof BufferedImage img) return imageSizeof(img);
        
        if (o instanceof Serializable s) return serialSizeof(s);

        throw new IllegalArgumentException("JavaSize.sizeof(Object) - unrecognized type in argument: " + o.toString());
    }
    
    /**
     * <p>
     * Computes the approximate size of the given string.
     * </p>
     * <p>
     * The size of a string is equal to its length times the size
     * of each character, plus the reserved allocation for every
     * Java string.
     * </p>
     * 
     * @param str string whose allocation size is to be computed
     * 
     * @return the memory allocation for the given string (in bytes)
     * 
     * @see #SZ_char
     * @see #SZ_STR_RSRVD
     */
    private static long stringSizeof(String str) {
        return str.length()*SZ_char + SZ_STR_RSRVD;
    }
    
    /**
     * <p>
     * Returns the approximate size of an <code>BufferedImage</code> instance.
     * </p>
     * <p>
     * The returned value is computed with the following:
     * <ul>
     * <li>The size of the name string</li>
     * <li>The size of the time instant</li>
     * <li>The (approx) size of the <code>Format</code> enumeration</li>
     * <li>The size of the image data</li>
     * </ul>
     * The values are summed and returned.
     * </p>
     * 
     * @param img image whose allocation is to be computed
     * 
     * @return the memory allocation for the given image (in bytes)
     */
    private static long imageSizeof(BufferedImage img) {
        Long    szName  = stringSizeof( img.getName() );
        Long    szTms   = SZ_Instant;
        Long    szFmt   = 2 * SZ_Enum_RSRVD;
        Integer cntDims = (img.getDimensions()!=null) ? img.getDimensions().size() : 0;
        Long    szDims  = SZ_ImageDimension * cntDims; 
        Integer szData  = img.getData().length;
        
        return szName + szTms + szFmt + szDims + szData;
    }
    
    /**
     * <p>
     * Computes an (overestimated) approximation to the memory allocation of
     * the argument, in bytes.
     * </p>
     * <p>
     * The memory allocation estimate of the argument is achieved by  
     * serializing it to a byte stream and counting the stream.
     * Note that this can significantly overestimate the allocation needed
     * if object in the collection have multiple references within the 
     * same collection (or others to be sized).
     * </p>
     *  
     * @param objSerial a serializable object (in particular a collection)
     * 
     * @return the memory allocation necessary to storage the entire collection
     * 
     * @throws IllegalArgumentException IO exception serializing argument (see exception source)
     */
    public static long serialSizeof(Serializable objSerial) throws IllegalArgumentException {
        try {
            ByteArrayOutputStream   osBytes = new ByteArrayOutputStream();
            ObjectOutputStream      osObj   = new ObjectOutputStream(osBytes);
            
            osObj.writeObject(objSerial);
            
            return osBytes.size();

        } catch (IOException e) {
            throw new IllegalArgumentException("JavaSize#structSizeof(Serializabl) - unable to stream argument " + objSerial.toString(), e);
        }
    }
       
    
    //
    // Private Methods
    //
    
    /**
     * <p>
     * Prevents construction of an instance of <code>JavaSize</code>.
     * </p>
     *
     */
    private JavaSize() {
    }

}
