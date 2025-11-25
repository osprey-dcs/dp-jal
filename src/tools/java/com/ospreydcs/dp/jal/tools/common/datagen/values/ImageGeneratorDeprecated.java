/*
 * Project: dp-data-simulator
 * File:	ImageGeneratorDeprecated.java
 * Package: com.ospreydcs.dp.datasim.model.values
 * Type: 	ImageGeneratorDeprecated
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
 * @since May 14, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.jal.tools.common.datagen.values;

import java.time.Instant;

import com.ospreydcs.dp.jal.common.BufferedImage;
import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.tools.common.datagen.IDataValueFactory;

/**
 * <p>
 * Generates random image of type <code>{@link BufferedImage}</code> available in the Data Platform
 * Java client API.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since May 14, 2024
 *
 * @deprecated Replaced by ImageFactory
 */
@Deprecated(since="Nov 14, 2025", forRemoval=true)
public class ImageGeneratorDeprecated implements IDataValueFactory {

    
    //
    // Class Constants
    //
    
    /** The value type of all simulated data returned by this data value factory */
    public static final DpSupportedType     ENM_TYPE = DpSupportedType.IMAGE;
    
    
    //
    // Configuration
    //
    
    /** Image name prefix */
    private final String                strNamePrefix;
    
    /** Image format enumeration */
    private final BufferedImage.Format  enmFormat;
    
    /** Image size, that is, memory allocation */
    private final int                  szAlloc;
    
    
    //
    // Variables
    //
    
    /** Image counter - used for image name generation */
    private int         cntImages = 0;
    
    
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>ImageGeneratorDeprecated</code>.
     * </p>
     *
     * @param strNamePrefix prefix given to all image names (e.g., filename prefix)
     * @param enmFormat     image format enumeration
     * @param szAlloc       image size in bytes
     * 
     * @throws IllegalArgumentException image size must be greater than zero
     */
    public ImageGeneratorDeprecated(String strNamePrefix, BufferedImage.Format enmFormat, int szAlloc) throws IllegalArgumentException {
        this.strNamePrefix = strNamePrefix;
        this.enmFormat = enmFormat;
        this.szAlloc = szAlloc;
        
        if (this.szAlloc <= 0)
            throw new IllegalArgumentException("Image size must be greater than zero.");
    }
    
    
    //
    // IDataValueFactory Interface
    //
    
    /**
     * @see com.ospreydcs.dp.jal.tools.common.datagen.IDataValueFactory#getValueType()
     */
    @Override
    public DpSupportedType  getValueType() {
        return ENM_TYPE;
    }
    
    /**
     *
     * @see com.ospreydcs.dp.datasim.model.values.IDataValueGenerator#nextValue()
     */
    @Override
    public Object nextValue() {
        
        // Create a new image with empty data vector (raw allocation)
//        String  strName = this.strNamePrefix + "-" + Integer.toString(this.cntImages);
        String  strName = this.strNamePrefix + Integer.toString(this.cntImages);
        Instant insTms = Instant.now();
        byte[]  arrData = new byte[this.szAlloc];
        
        BufferedImage   image = new BufferedImage(strName, insTms, this.enmFormat, arrData);
        
        // Increment image counter then return image
        this.cntImages++;
        
        return image;
    }

}
