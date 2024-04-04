package com.ospreydcs.dp.api.common;

/**
 * <p>
 * Record containing the dimension attributes for a user-formatted data array.
 * </p>
 * <p>
 * This record is most commonly used for image data to describe its formatting.  For example, see
 * <code>{@link BufferedImage}</code>.  However, the record is arbitrary enough to describe dimensional
 * information to recover most data arrays stored as raw data.
 * </p>
 *
 * @param cntVals   Number of image elements (bins) in the dimension
 * @param indOffset Offset (byte) index into the axis before image element values start
 * @param szFull    Full size (in bytes) of the dimension including offset and all image element bins
 * @param szBin     Size (in bytes) of an image element bin, for example a pixel
 * @param bolReversed Value order reversed flag
 *  
 * @author Christopher K. Allen
 * @since Dec 30, 2023
 *
 */
public record ArrayDimension(Integer cntVals, Integer indOffset, Integer szFull, Integer szBin, Boolean bolReversed) {
    
}