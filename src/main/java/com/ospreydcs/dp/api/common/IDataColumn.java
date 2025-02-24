/*
 * Project: dp-api-common
 * File:	IDataColumn.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	IDataColumn
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
 * 
 * @author Christopher K. Allen
 * @org    OspreyDCS
 * @since Oct 2, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.common;

import java.util.Iterator;
import java.util.List;


/**
 * <p>
 * Defines the methods required for a data column containing time-series data.
 * <p>
 * Represents a column of data in the <code>IDataTable</code> data table structure.
 * That is, contains a column of time series data for a single EPICS PV value.
 * </p>
 * <p>
 * Since the contained data is for a single PV the data type is homogeneous.
 * That is, the data type of an EPICS PV is static.  Use <code>{@link #getEpicsType()}</code>
 * to determine the data type of the Java <code>Objects</code> contained
 * in this data structure.
 * </p>
 * <p>
 * Note that numeric indices are 0-based as is native to Java collections and arrays.
 * </p>
 * <p>
 * The type parameter <code>T</code> can be used to specify the type of the
 * column data if it is homogeneous.  Otherwise use <code>T = Object</code>.
 * </p> 
 * 
 * @param <T> the data type of a homogeneous data column
 *
 * @author Christopher K. Allen
 * @since Oct 2, 2022
 *
 */
public interface IDataColumn<T extends Object> extends Iterable<T> {

    
    //
    // Properties Query
    //
    
    /**
     * <p>
     * Return the name of this data column.
     * </p>
     * <p>
     * The returned value can be interpreted as a data source name if the underlying data is time-series data, 
     * a table column label if the data column is part of a data table, or other data label within the client
     * context.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Should return a <code>null</code> value if the data column has not been initialized.</li>
     * </ul>
     * </p>
     * 
     * @return label or name for the underlying column data  
     */
    public String getName();
    
    /**
     * <p>
     * Return the data type of all column data as a <code>{@link DpSupportedType}</code> enumeration constant.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Should return a <code>null</code> value if the data column has not been initialized.</li>
     * </ul>
     * </p>
     * 
     * @return the data type for all column data
     */
    public DpSupportedType getType();
    
    /**
     * <p>
     * Return the size of the data column.
     * </p>
     * <p>
     * Returns the number of data values within this data column.
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Operation <code>{@link #getValue(int)}</code> should return without exception for all 
     *     indices less than returned value.</li>
     * <li>Should return a <code>null</code> value if the data column has not been initialized.</li>
     * </ul>
     * </p>
     * 
     * @return the data column size 
     */
    public Integer getSize();
    
    /**
     * <p>
     * Clears the data column of all contents.
     * </p>
     * <p>
     * Implementations should free all internal references.  The idea is to mark the contents of the
     * current data column as eligible for garbage collection by the Java Virtual Machine (VM).  Thus,
     * this operation it is typically part of an effort to free heap memory and Java VM resources.
     * </p>
     * <p>
     * Typically, this method is invoked from <code>IDataTable</code> implementations and not directly by
     * clients.  Specifically, implementations should perform this method for all tables column during an
     * <code>{@link IDataTable#clear()}</code> operation.  See class documentation for 
     * <code>{@link IDataTable#clear()}</code> for more information.  The method is made available here
     * for attempts to Java VM memory management and garbage collection.   
     * </p>
     */
    public void clear();
    
    
    // 
    // Data Queries
    //
    
    /**
     * <p>
     * Returns data value at the given index. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Should throw exception if <code>index</code> &ge; <code>{@link #getSize()}</code>.</li>
     * <li>Should return a <code>null</code> value if the data column has not been initialized.</li>
     * </ul>
     * </p>
     * 
     * @param index column index
     * 
     * @return  data object at the given table indices,
     *          or <code>null</code> if the column has not been initialized
     * 
     * @throws IndexOutOfBoundsException    index is out of current column boundaries
     * @throws ArithmeticException          overflow of the row index (this should be rare) 
     */
    public Object getValue(int index) throws IndexOutOfBoundsException, ArithmeticException;
    
    /**
     * <p>
     * Returns all the data values in the data column as an ordered list.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Should return an empty container if data column has not been populated.</li>
     * <li>Should return a <code>null</code> value if the data column has not been initialized.</li>
     * </ul>
     * </p>
     * 
     * @return all data values of the column 
     * 
     * @throws ArithmeticException  internal overflow - column is too large 
     */
    public List<Object> getValues() throws ArithmeticException;
    
    
    //
    // Memory Allocation
    //
    
    /**
     * <p>
     * Compute and return the estimated total memory allocation for the data in this column (in bytes).
     * </p>
     * <p>
     * Typically the returned value should be a conservative value, an overestimate favoring 64-bit architectures. 
     * It should also include allocation for the timestamps.
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * The result returned by this operation is highly dependent upon the implementation.
     * <ul>
     * <li>Results should not be used for critical operations, only as estimates for evaluation purposes.</li>
     * <li>Implementations choosing not to supported this operation should throw a <code>UnsupportedOperationException</code>.
     * </ul>
     * </p>
     * 
     * @return  memory allocation required for all the data in the column (in bytes)
     * 
     * @throws UnsupportedOperationException    this operation is not supported by the implementation
     * @throws ArithmeticException              error in allocation calculation (e.g., column to large)
     */
    public long allocationSize() throws UnsupportedOperationException, ArithmeticException;
    
    
    //
    // Default Implementation
    //
    
    /**
     * </p>
     * <Returns the column value at given index as a typed value.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataColumn}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getValue(int)}</code></b>.
     * <br/></br>
     * The <code>default</code> implementation provided in <code>{@link IDataColumn}</code> interface
     * is just a convenience method.  
     * It calls <code>{@link #getValue(int)}</code> and <code>{@link IDataColumn#getType()}</code> 
     * to obtain the returned value and check the type.
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Returns <code>null</code> if the data column has not been populated.</li>
     * <li>Returns <code>null</code> if the data column has not been initialized.</li>
     * <li>Throws exception if <code>T</code> is incompatible with column data type,</li>
     * </ul>
     * </p>  
     * 
     * @param index column index
     * 
     * @return  the data entry at given column index as a typed value
     * 
     * @throws IndexOutOfBoundsException    index out of bounds (0 &le; index &lt; <code>{@link #getSize()}</code>) 
     * @throws ArithmeticException          overflow of the row index (this should be rare) 
     * @throws ClassCastException           generic type T incompatible with column data type
     */
    @SuppressWarnings("unchecked")
    default public T getValueTyped(int index)
            throws IndexOutOfBoundsException, ArithmeticException, ClassCastException {
        
        Object              objVal = this.getValue(index);

        // Check the value
        if (objVal == null)
            return null;
        
        // Check the data type
        DpSupportedType     enmType = this.getType();
        if ( !enmType.isAssignableFrom(objVal.getClass()) )
            throw new ClassCastException("IDataColumn<T>.getValueTyped(int) - Generic parameter T incompatible with column type " + enmType);
        
        // Data object can be safely cast
        return (T)objVal;
    }

    /**
     * <p>
     * Returns all the data values in the data column as a typed ordered vector,  
     * or the empty vector if the column has not been initialized or 
     * not populated.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataColumn}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getValues()}</code></b>.
     * <br/></br>
     * The <code>default</code> implementation provided in <code>{@link IDataColumn}</code> interface
     * is just a convenience method.  
     * It calls <code>{@link #getValues()}</code> and <code>{@link IDataColumn#getType()}</code> 
     * to obtain the returned value and check the type.
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Returns empty list if the data column has not been populated.</li>
     * <li>Returns <code>null</code> if the data column has not been initialized.</li>
     * <li>Throws exception if <code>T</code> is incompatible with column data type,</li>
     * </ul>
     * </p>  
     * 
     * @return all typed data values of the column 
     * 
     * @throws ArithmeticException internal overflow - column is too large
     * @throws ClassCastException  generic type T incompatible with column data type
     */
    @SuppressWarnings("unchecked")
    default public List<T> getValuesTyped() throws ArithmeticException, ClassCastException {
      
        List<Object>    lstVals = this.getValues();
        
        // Check value list
        if (lstVals == null)
            return null;
        
        if (lstVals.isEmpty())
            return (List<T>) lstVals;
        
        // Check type
        Object              objVal = lstVals.get(0);
        DpSupportedType     enmType = this.getType();
        if ( !enmType.isAssignableFrom(objVal.getClass()) )
            throw new ClassCastException("IDataColumn<T>.getValuesTyped() - Generic parameter T incompatible with column type " + enmType);
        
        // Value list can be safely cast
        return (List<T>) lstVals;
    };
    
    /**
     *
     * @see @see java.lang.Iterable#iterator()
     */
    default public Iterator<T>  iterator() {
        return this.getValuesTyped().iterator();
    }
    
//    //
//    // Data Modification
//    //
//    
//    /**
//     * Adds the given vector of values to the
//     * tail of this data column. The data is added in order of the argument.
//     * 
//     * @param lstData new data values to be added to the column tail
//     * 
//     * @return  <code>true</code> if the operation was successful
//     *          <code>false</code> if the <code>DataColumn</code> did not change
//     *          
//     * @throws NullPointerException the argument was a <code>null</code> value          
//     */
//    public boolean addData(Vector<T> vecData) throws NullPointerException ;
//    
//    /**
//     * Adds the given ordered list of data values to the tail of the data 
//     * column. The data is added in order of the argument.
//     * 
//     * @param lstData new data values to be added to the column tail
//     * 
//     * @return <code>true</code> if the data was successfully added,
//     *         <code>false</code> otherwise
//     *         
//     * @throws NullPointerException the argument was a <code>null</code> value          
//     */
//    default boolean addData(List<T> lstData) throws NullPointerException {
//        return this.addData(new Vector<T>(lstData));
//    }
//    
//    /**
//     * Removes all data from the data column.
//     */
//    public void clear();
        
}
