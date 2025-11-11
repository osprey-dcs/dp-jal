/*
 * Project: dp-data-simulator
 * File:	TensorUtility.java
 * Package: com.ospreydcs.dp.datasim.utility
 * Type: 	TensorUtility
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
 * @since May 17, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.jal.tools.common.datagen.utility;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import com.ospreydcs.dp.jal.tools.common.datagen.JalScalarType;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Utility class providing common tensor operations.
 * </p>
 * <h2>Tensor Attributes</h2>
 * <p>
 * Tensor shapes <b>S</b> = (N<sub>0</sub>, ..., N<sub><i>r</i>-1</sub>) are generally represented as as an array
 * of primitive <code>int</code>, specifically as type <code>int[]</code>.  
 * Note that the rank of the tensor is given by <code>S.length</code> in Java. 
 * </p>
 * <p>
 * Tensor indexes <b>i</b> = (i<sub>0</sub>, ..., i<sub><i>r</i>-1</sub>) 
 * are generally represented  an array of <code>Integer</code> Java objects, specifically, 
 * <code>Integer[]</code>.
 * <h2>Tensors</h2>
 * <p>
 * When dealing with tensors directly we assume they are represented in the form
 * <pre>
 *      list of lists of lists ... of list of scalar values
 * </pre>
 * Thus the actual element values within the tensor are contained in the last axis within the complex
 * data structure.  This is currently the format of the <code>Array</code> message within the Data Platform 
 * <code>common.proto</code> gRPC API.
 * </p>
 * <p>
 * Thus, when given a tensor <b>T</b> and an index <b>i</b> = (i<sub>0</sub>, ..., i<sub><i>r</i>-1</sub>) 
 * the element values are found by following the nested list indexes i<sub>0</sub>, i<sub>1</sub>, ...
 * to the final list at at index i<sub><i>r</i>-1</sub> where the element value is located.
 * Element values are always represented as Java objects.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since May 17, 2024
 *
 */
public final class TensorUtility {

    
    //
    // Shape and Index Operations
    //
    
    /**
     * <p>
     * Compares whether or not the two integer arrays are equivalent as shapes.
     * </p>
     * 
     * @param arr1  shape array under comparison
     * @param arr2  shape array under comparison
     * 
     * @return  <code>true</code> if shapes are equivalent, <code>false</code> otherwise
     */
    public static boolean equivalent(int[] arr1, int[] arr2) {
        int     cntDims = arr1.length;
        
        if (arr2.length != cntDims)
            return false;
        
        for (int iDim=0; iDim<cntDims; iDim++) {
            if (arr1[iDim] != arr2[iDim])
                return false;
        }
        
        return true;
    }
    
    /**
     * <p>
     * Compares whether or not the two integer arrays are equivalent as index arrays.
     * </p>
     * 
     * @param arr1  index array under comparison
     * @param arr2  index array under comparison
     * 
     * @return  <code>true</code> if indexes are equivalent, <code>false</code> otherwise
     */
    public static boolean equivalent(Integer[] arr1, Integer[] arr2) {
        int     cntDims = arr1.length;
        
        if (arr2.length != cntDims)
            return false;
        
        for (int iDim=0; iDim<cntDims; iDim++) {
            if (arr1[iDim] != arr2[iDim])
                return false;
        }
        
        return true;
    }
    
    /**
     * <p>
     * Computes the size of a tensor with the given shape.
     * </p>
     * 
     * @param arrTensorShape    integer array containing the shape of target tensor
     * 
     * @return  number of elements within target tensor (i.e., the size)
     */
    public static int computeTensorSize(int[] arrTensorShape) {
        int     cntAxes = arrTensorShape.length;
        int     szTensor = 1;
        
        for (int iAxis=0; iAxis<cntAxes; iAxis++) {
            int     szDim = arrTensorShape[iAxis];
            
            szTensor *= szDim;
        }
        
        return szTensor;
    }
    
    /**
     * <p>
     * Returns the rank of a tensor with the given shape.
     * </p>
     * 
     * @param arrTensorShape    integer array containing the shape of target tensor
     * 
     * @return  the rank of the tensor (specifically, <code>arrTensorShape.length</code>)
     */
    public static int computeTensorRank(int[] arrTensorShape) {
        return arrTensorShape.length;
    }
    
    /**
     * <p>
     * Creates and returns a string description for the given tensor shape.
     * </p>
     * 
     * @param arrTensorShape    integer array containing tensor shape
     * 
     * @return  tensor shape string representation
     */
    public static String  toString(int[] arrTensorShape) {
        int     cntAxes = arrTensorShape.length;
        
        String      strBuf = "(";
        for (int iAxis=0; iAxis<cntAxes; iAxis++) {
            int     szDim = arrTensorShape[iAxis];
            
            strBuf += szDim;
            
            if (iAxis < cntAxes-1)
                strBuf += ", ";
        }
        strBuf += ")";
        
        return strBuf;
    }
    
    /**
     * <p>
     * Creates and returns a string representation for the given tensor index.
     * </p>
     * 
     * @param arrIndex  integer array containing a tensor index
     * 
     * @return  tensor index string representation
     */
    public static String  toString(Integer[] arrIndex) {
        int     cntIndices = arrIndex.length;
        
        String      strBuf = "(";
        for (int iAxis=0; iAxis<cntIndices; iAxis++) {
            Integer intIndex = arrIndex[iAxis];
            
            strBuf += intIndex;
            
            if (iAxis < cntIndices-1)
                strBuf += ", ";
        }
        strBuf += ")";
        
        return strBuf;
    }
    
    /**
     * <p>
     * Creates and returns a string representation for the ordered list of tensor indexes.
     * </p>
     * 
     * @param lstIndexes    ordered list of integer arrays containing string indexes
     * 
     * @return  a string representation of all indexes within the argument
     */
    public static String  toString(List<Integer[]> lstIndexes) {
        String  strBuf = "{";
        String  strComma = ", ";
        
        for (Integer[] arrIndex : lstIndexes) {
            strBuf += TensorUtility.toString(arrIndex);
            strBuf += strComma;
        }
        
        int indLast = strBuf.lastIndexOf(strComma);
        strBuf = strBuf.substring(0, indLast);
        strBuf += "}";
        
        return strBuf;
    }
    
    //
    // Tensor Operations
    //
    
    /**
     * <p>
     * Determines the shape of the given tensor through inspection.
     * </p>
     * <p>
     * Parses through the first element of each axis to determine the size of the axis.  Collects them
     * all and returns the shape.  
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The given argument is assumed to be symmetrical, as is all tensors.  Otherwise parsing
     * will not work and shape returned is meaningless. 
     * </p>
     * 
     * @param objTensor     a tensor object represented as a list of lists of lists ... etc.
     * 
     * @return  the shape of the given argument
     * 
     * @throws IllegalArgumentException the argument is not a Java <code>List</code> instance
     */
    public static int[] tensorShape(Object objTensor) throws IllegalArgumentException {
        Object          obj = objTensor;
        List<Integer>   lstDims = new LinkedList<>();
        
        // Parse through the first element of each dimension to get axis sizes
        while (obj instanceof List vec) {
            Integer     intDim = vec.size();
            
            lstDims.add(intDim);
            
            obj = vec.get(0);
        }
        
        // Check rank
        int     iRank = lstDims.size();
        if (iRank == 0)
            throw new IllegalArgumentException(JavaRuntime.getQualifiedMethodNameSimple() + " - argument not a List.");
        
        // Convert list to int[]
        int[]   arrShape = new int[iRank];
        for (int i=0; i<iRank; i++)
            arrShape[i] = lstDims.get(i);
        
        return arrShape;
    }
    
    
    //
    // Tensor Element Operations
    //
    
    /**
     * <p>
     * Determines whether or not the terminal value of the given tensor is of the given type.
     * </p>
     * <p>
     * The comparison is made for the first value in the tensor only. Specifically, element (0,0,0,...)
     * is obtained and compared against the argument enumeration constant.
     * </p>
     * 
     * @param enmType       scalar type (enumeration constant) 
     * @param objTensor     tensor under test (as Java <code>Object</code>)
     * 
     * @return  <code>true</code> if the first array element is of the given scalar type,
     *          <code>false</code> otherwise
     */
    public static boolean isElementValueOfType(JalScalarType enmType, Object objTensor) {
        Object obj = TensorUtility.extractFirstElement(objTensor);
        
        return enmType.isAssignable(obj);
    }
    
    /**
     * <p>
     * Extracts the first element of the given tensor.
     * </p>
     * <p>
     * The first element of the tensor is the element at index position
     * (0, 0, ..., 0)
     * </p>
     * 
     * @param objTensor target tensor as a Java <code>Object</code> (should be a <code>List</code>)
     * @return
     */
    public static Object    extractFirstElement(Object objTensor) {
        Object obj = objTensor;
        
        while (obj instanceof List lst) {
            obj = lst.get(0);
        }

        return obj;
    }
    
    /**
     * <p>
     * Extracts the element at the given index from the given tensor.
     * </p>
     * 
     * @param arrElemIndex  tensor index set
     * @param objTensor     tensor as an object (should be a <code>List</code)
     * 
     * @return              tensor element at the given index set
     * 
     * @throws NoSuchElementException   the given index was invalid
     */
    public static Object  extractElementAt(Integer[] arrElemIndex, Object objTensor) throws NoSuchElementException {
        int     cntAxes = arrElemIndex.length;
        
        Object  obj = objTensor;
        for (int iAxis=0; iAxis<cntAxes; iAxis++) {
            int index = arrElemIndex[iAxis];

            if (obj instanceof List lst) {
                obj = lst.get(index);
                
            } else {
                throw new NoSuchElementException("Element at axis " + iAxis + ", index position " + index + " was not a List");
            }
        }
        
        return obj;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * @param lstDims   tensor dimensions list
     * @param objAxis   the axis vector
     * 
     * @deprecated  not used
     */
    @Deprecated(since="Nov 3, 2025", forRemoval=true)
    private void tensorShapeHelper(List<Integer> lstDims, Object objAxis) {
        
        if (objAxis instanceof List vec) {
            Integer intDim = vec.size();
            
            lstDims.add(intDim);
            
            Object  objElement = vec.get(0);
            
            this.tensorShapeHelper(lstDims, objElement);
        }
    }
    
    /**
     * <p>
     * Prevents construction of <code>TensorUtility</code> instance.
     * </p>
     *
     */
    private TensorUtility() {
    }

}
