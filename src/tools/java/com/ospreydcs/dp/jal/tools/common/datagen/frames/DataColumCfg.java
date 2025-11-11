/*
 * Project: dp-jal
 * File:	DataColumCfg.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.frames
 * Type: 	DataColumCfg
 *
 * Copyright 2010-2025 the original author or authors.
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
 * @since Nov 4, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.frames;

import java.lang.reflect.MalformedParametersException;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.common.BufferedImage;
import com.ospreydcs.dp.jal.tools.common.datagen.JalHeteroType;
import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.tools.common.datagen.values.ScalarGenerator;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Record containing parameters for configuring <code>{@link DataColumnGenerator}</code> instances.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 4, 2025
 *
 */
public record DataColumCfg<DataConfig extends Record>(
        String          strNmPref,
        int             cntCols,
        int             cntRows,
        JalHeteroType   enmDataType,
        JalScalarType   enmValueType,
        DataConfig      recDataCfg
        ) 
{
    public static record ScalarConfig() {};
    static record ArrayConfig(int[] shape) {};
    static record StructConfig(int depth, int fanout) {};
    static record ImageConfig(String strPref, BufferedImage.Format enmFmt, int size) {};

    
    /**
     * <p>
     * Checks if the the <code>{@link #enmDataType}</code> field is consistent with the <code>{@link #recDataCfg}</code> field.
     * </p>
     * 
     * @return
     */
    public boolean  isValid() {
        
        return switch (enmDataType) {
        case SCALAR -> (this.recDataCfg instanceof ScalarConfig);
        case ARRAY -> (this.recDataCfg instanceof ArrayConfig);
        case STRUCTURE -> (this.recDataCfg instanceof StructConfig);
        case IMAGE -> (this.recDataCfg instanceof ImageConfig);
        default -> false;
        };
    }
    
    /**
     * <p>
     * Parses and argument string to identify and create a Data Type configuration record (i.e., <code>DataConfig</code>).
     * </p>
     * <p>
     * <h2>DataConfig Records</h2>
     * Data Type configuration records are internal record used for the <code>DataConfig</code> generic type.  There is
     * one type for each data type supported for column data value creation. 
     * Currently there are the following:
     * <ol>
     * <li>Scalar - <code>{@link ScalarGenerator.ScalarConfig}</code>,</li>
     * <li>Array - <code>{@link ArrayConfig}</code>,</li>
     * <li>Structure - <code>{@link StructConfig}</code>,</li>
     * <li>Image - <code>{@link ImageConfig}</code>.
     * </ol> 
     * The above record fields contain the parameters necessary to configure the appropriate value generator for
     * column data.
     * </p> 
     * <p>
     * <h2>Caveat</h2>
     * Clearly it is not necessary for the arguments to be obtained from an application command line as described.  
     * So long as the stated conditions and formats are followed the method will create and return an appropriate 
     * Data Type configuration record.
     * </p> 
     * <p>
     * <h2>Usage</h2>
     * The argument is assumed to be part of an application command line.  For example, the command-line could
     * contain the delimited variable "--data" which appears as
     * <code>
     * <pre>
     * > java application --data DTYPE VTYPE field1 field2 ... --var2 param ...
     * </pre>
     * </code>
     * where 
     * <ul>
     * <li>DTYPE is a <code>JalHeteroType</code> enumeration constant,</li>
     * <li>DTYPE is a <code>JalScalarType</code> enumeration constant,</li>
     * <li>field1 is a configuration record field,</li>
     * <li>field2 is a configuration record field,</li>
     * <li>... are any additional fields.</li>
     * </ul>
     * The argument to this method is then the set of command-line strings
     * <code>
     * <pre>
     * DTYPE VTYPE field1 field2 ...
     * </pre>
     * </code>
     * The type of configuration record returned is given by the <code>DTYPE</code> value:
     * <ul>
     * <li><code>{@link JalHeteroType#SCALAR}</code> - <code>{@link ScalarGenerator.ScalarConfig}</code>.</li>
     * <li><code>{@link JalHeteroType#ARRAY}</code> - <code>{@link ArrayConfig}</code>.</li>
     * <li><code>{@link JalHeteroType#STRUCTURE}</code> - <code>{@link StructConfig}</code>.</li>
     * <li><code>{@link JalHeteroType#IMAGE}</code> - <code>{@link ImageConfig}</code>.</li>
     * </ul>
     * Thus, the number of elements within the argument string array is dependent upon the <code>JalHeteroType</code>
     * identified by the first element.  If the number of arguments is not appropriate for the given type
     * a <code>ConfigurationException</code> is thrown.
     * </p>
     *  
     * @param args  argument string defining a configuration record
     * 
     * @return  the configuration record defined and populated by the arguments
     * 
     * @throws IllegalArgumentException the argument contained no data
     * @throws TypeNotPresentException  the 1st argument was not a <code>JalHeteroType</code> enumeration constant
     * @throws ConfigurationException   the argument contained the wrong number of arguments for the <code>JalHeteroType</code>
     * @throws MalformedParametersException an enumeration constant within the argument set was not recognized (IMAGE)
     */
    public static Record    parseDataConfig(String...args) throws IllegalArgumentException, TypeNotPresentException, ConfigurationException, MalformedParametersException {
        if (args.length < 1)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() 
                    + " - Argument must contain at least one argument: " 
                    + args);

        // Get the Data Type of the arguments list
        String  strDataType = args[0];
        JalHeteroType   enmDataType;
        try {
            enmDataType = JalHeteroType.valueOf(JalHeteroType.class, strDataType); // throws IllegalArgumentException
        } catch (Exception e) {
            throw new TypeNotPresentException(strDataType, e);
        }
        
        
        Record  recConfig;
        switch (enmDataType) {
        case SCALAR:
            recConfig = new ScalarConfig();
            break;
            
        case ARRAY:
            if (args.length < 2)
                throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple()
                        + " - ARRAY 'shape' must have at least one element: " + args);
            int[] shape = Arrays.<String>stream(args, 1, args.length)
                            .mapToInt(arg -> Integer.parseInt(arg))     // throws NumberFormatException
                            .toArray();
            recConfig = new ArrayConfig(shape);
            break;
            
        case STRUCTURE:
            if (args.length != 3)
                throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple()
                        + " - STRUCTURE must have 'depth' and 'fanout' parameters: " + args);
            int depth = Integer.parseInt(args[1]);                      // throws NumberFormatException
            int fanout = Integer.parseInt(args[2]);
            recConfig = new StructConfig(depth, fanout);
            break;
            
        case IMAGE:
            if (args.length != 4)
                throw new ConfigurationException(JavaRuntime.getQualifiedMethodNameSimple()
                        + " - IMAGE must have 'strPref', 'fmt' and 'size' parameters: " + args);
            try {
                String                  strPref = args[1];
                BufferedImage.Format    enmFmt = BufferedImage.Format.valueOf(BufferedImage.Format.class, args[2]);    // throws IllegalArgumentException
                int                     size = Integer.parseInt(args[3]);   // throws NumberFormatException
                recConfig = new ImageConfig(strPref, enmFmt, size);
                
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new MalformedParametersException(JavaRuntime.getQualifiedMethodNameSimple()
                        + " - IMAGE Format parameter unrecognized: " 
                        + e.getMessage());
            }
            break;
            
        default:
            throw new NoSuchElementException(JavaRuntime.getQualifiedMethodNameSimple() + " - Data type not supported: " + enmDataType);
        
        }
        
        return recConfig;
    }
}
