/*
 * Project: datastore-admin
 * File:	IDataTable.java
 * Package: com.ospreydcs.datastore.admin.model
 * Type: 	IDataTable
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
 * @since Oct 2, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.common;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;


/**
 * <p>
 * Defines the methods required for a correlated, time-series data table composed 
 * of <code>{@link IDataColumn}</code> objects.
 * </p>
 * <p>
 * Defines the basic functionality of class supporting time-series data table operations and  
 * allows such classes to present their data without exposing the underlying implementation.
 * In addition to the table data, time-series data tables contain column labels (typically data source names)
 * along with an additional column of timestamps for each column row.
 * </p>
 * <p>
 * <h2>Definition</h2>
 * A <code>IDataTable</code> object has a table structure consisting of a column of timestamps
 * and one or more columns of time-series data values.  The timestamp value at a given index applies
 * to all time-series data in that row, that is, the data columns are correlated.  
 * Thus the structure has the following representation:
 * <p/>
 * <p>
 * <pre/>
 *     Time    PV<sub>1</sub>    PV<sub>2</sub>  ...    PV<sub><i>N</i></sub>
 *      <i>t</i><sub>1</sub>     <i>v</i><sub>1,1</sub>   <i>v</i><sub>1,2</sub>  ...    <i>v</i><sub>1,<i>N</i></sub>  
 *      <i>t</i><sub>2</sub>     <i>v</i><sub>2,1</sub>   <i>v</i><sub>2,2</sub>  ...    <i>v</i><sub>2,<i>N</i></sub>
 *      :                          :
 *      :                          :
 *      <i>t</i><sub><i>M</i></sub>     <i>v</i><sub><i>M</i>,1</sub>   <i>v</i><sub><i>M</i>,2</sub>  ...    <i>v</i><sub><i>M</i>,<i>N</i></sub>
 * </pre>       
 * </p>
 * where 
 * <ul>
 * <li><i>t</i><sub></i>m</i></sub> is the timestamp for table row <i>m</i>.</li>
 * <li>PV<sub><i>n</i></sub> is the column name (i.e., data source name, or Process Variable name) at column <i>n</i>.</li>
 * <li><i>v</i><sub><i>m</i>,<i>n</i></sub> is the time-series value at row <i>m</i>, column <i>n</i>.</li>
 * <li><i>M</i> is the number of table rows (i.e., the "size" of each data column).
 * <li><i>N</i> is the number of table columns.
 * </ul>
 * For the sake of notational brevity, indexing in the above example table is 1 based whereas Java
 * supported 0-based indexing.
 * </p>
 * Note the following condition on the above data table:
 * <ul>
 * <li>
 * The timestamp of each data value <i>V<sub>m,n</sub></i> depends upon its row position <i>m</i>
 * within its data column PV<sub><i>n</i></sub>.
 * </li>
 * <li>  
 * The timestamp <i>t<sub>m</sub></i> refers to all data values {<i>V</i><sub>&mu;,<i>n</i></sub> | &mu;=<i>m</i>}
 * within data columns {PV<sub><i>n</i></sub>}.
 * </li>
 * </ul>
 * Thus, the enclosed data are all "time-correlated".
 * </p>
 * <p>
 * Data columns can be referenced by either a numeric index <i>n</i> or a column name
 * (e.g., the PV name).  This condition is provided since numeric column indexing
 * might provide some performance improvement. Referencing by column name is more natural but always requires a 
 * mapping lookup.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>Numeric indices are 0-based as is native to Java.</li>
 * <li>Implementations may chose their own indexing scheme.  In particular, data columns may have arbitrary ordering.</li>
 * </ul>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Oct 2, 2022
 *
 */
public interface IDataTable {

    
    //
    // State Query
    //
    
    /**
     * <p>
     * Indicates whether or not the table has been fully loaded with all expected time-series data.
     * </p>
     * <p>  
     * A returned value of <code>true</code> should indicate that the table is fully loaded with all
     * expected data and has full data integrity.
     * </p>
     * <p>
     * This conditional is provided to accommodate dynamic table loading or other situations where data tables
     * may exist but their data is not yet available, or the table may exist but its data is corrupt. 
     * </p>
     * 
     * @return <code>true</code> if the data table is complete,
     *         <code>false</code> otherwise
     */
    public boolean isTableComplete();
    
    /**
     * <p>
     * Indicates whether the data table is corrupt or some other error has occurred during the table construction 
     * or the data population process (at the time of invocation).
     * </p>
     * <p>  
     * Generally, a returned value of <code>true</code> should indicate that data table population has been
     * completed, or at least attempted, but the table data is corrupt or incomplete.
     * </p>
     * 
     * @return <code>true</code> if an error occurred table construction, population, or table data is corrupt,
     *         <code>false</code> if no errors are present
     */
    public boolean hasError();

    
    //
    // Properties Query
    //
    
    /**
     * <p>
     * Returns the number of rows in the data table. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Should return 0 if the data table has not been populated.</li>
     * <li>Should return <code>null</code> if the data table has not been initialized.</li> 
     * </ul>
     * </p>  
     * 
     * @return  the data set size in rows,
     *          or 0 if the set has not been populated
     */
    public Integer  getRowCount();
    
    /**
     * <p>
     * Returns the number of data columns in the data table.
     * </p>
     * <p>
     * This value does not include the timestamp column, the returned value represents the number of
     * <em>data</em> columns within the table.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Should return 0 if the data table has not been populated.</li>
     * <li>Should return <code>null</code> if the data table has not been initialized.</li> 
     * </ul>
     * </p>  
     * 
     * @return  number of data columns in the data table,
     *          or 0 if the table has not been populated
     */
    public Integer getColumnCount();
    
    /**
     * <p>
     * Returns the table column index for the column with the given name.
     * </p>
     * <p>
     * <code>IDataTable</code> implementations are not prescribed to follow any indexing scheme.  The indices
     * assigned to data columns are determined completely by the implementing class.
     * This method is included determination of table column ordering within the data table. 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Should return <code>null</code> if the data table has not been populated.</li> 
     * </ul>
     * </p>  
     * 
     * @param strName   table column name
     *  
     * @return          table column index for column with given name
     * 
     * @throws NoSuchElementException     the given column name is not within the managed column collection
     * 
     * @see {@link #getColumnName(int)}
     */
    public int      getColumnIndex(String strName) throws NoSuchElementException;
    
    /**
     * <p>
     * Returns an ordered list of all data table column names, in the order of index.
     * </p>
     * <p>
     * A default implementation could be implemented.  However, for the sake of performance and resource
     * minimization it is best to force implementations to provide the list of column names
     * (a default implementation would require the generation of a new list at each invocation).
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Should return an empty collection if the data table has not been populated.</li>
     * <li>Should return <code>null</code> if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @return ordered list of all data table column names, 
     *         or empty if the table has not been populated
     */
    public List<String> getColumnNames();

    
    //
    // Data Query
    //
    
    /**
     * <p>
     * Returns the ordered vector (i.e., <code>{@link List}</code> of timestamps for all data columns.
     * or <code>null</code> if the data set has not been initialized
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The returned list should be immutable - Do NOT modify this object.</li>
     * <li>Should return an empty collection if the data table has not been populated.</li>
     * <li>Should return <code>null</code> if the data table has not been initialized.</li>
     * </ul>
     * </p>
     * 
     * @return timestamp vector corresponding to each data column,
     *          or empty if the data table has not been populated
     */
    public List<Instant> getTimestamps();
    
    /**
     * <p>
     * Returns an entire column of the data table for the given column index, 
     * or <code>null</code> if the data table has not been populated.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The returned object should be immutable - Do NOT modify this object.</li>
     * <li>Should return <code>null</code> if the data table has not been populated.</li>
     * <li>Should return <code>null</code> if the data table has not been initialized.</li>
     * </ul>
     * </p>
     * 
     * @param indCol column index - must be one less than the column count
     * 
     * @return  the <code>IDataColumn</code> object corresponding to the given index,
     *          or <code>null</code> if the data table has not been populated
     * 
     * @throws IndexOutOfBoundsException column index out of bounds (0 &le; index &lt; <code>{@link #getColumnCount()}</code>)
     * throws ClassCastException        generic parameter <code>T</code> is not compatible with column type
     */
    public /* <T extends Object> IDataColumn<T> */ IDataColumn<Object> getColumn(int indCol) throws IndexOutOfBoundsException /*, ClassCastException */;
    
    /**
     * <p>
     * Returns an entire column of the data table for given column name, 
     * or <code>null</code> if the data table has not been populated.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The returned object should be immutable - Do NOT modify this object.</li>
     * <li>Should return <code>null</code> if the data table has not been populated.</li>
     * <li>Should return <code>null</code> if the data table has not been initialized.</li>
     * </ul>
     * </p>
     * 
     * @param <T> data type of column data
     * 
     * @param strName column name for the returned object
     * 
     * @return  the <code>IDataColumn</code> object corresponding to the given label,
     *          or <code>null</code> if the data set has not been initialized
     * 
     * @throws NoSuchElementException   the given column name is not within the managed column collection
     * throws ClassCastException       generic parameter <code>T</code> is not compatible with column type
     */
    public /* <T extends Object> IDataColumn<T> */ IDataColumn<Object> getColumn(String strName) throws NoSuchElementException /*, ClassCastException */;
    
    
    //
    // Default Interface Implementations
    //
    
    /**
     * <p>
     * Determine whether or not the given column name is present within the table.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * simply calls <code>{@link #getColumnIndex(String)}</code> and catches the 
     * <code>NoSuchElementException</code> to determine the returned value.  
     * </ul>
     * </p> 
     * 
     * @param strName column name to be checked
     * 
     * @return  <code>true</code> if table contains column (whether populated or not),
     *          <code>false</code> otherwise
     */
    default public boolean  hasColumn(String strName) {
        
        try {
            this.getColumnIndex(strName);
            
            return true;
            
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * <p>
     * Returns the timestamp for the given row index.
     * </p> 
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * simply calls <code>{@link #getTimestamps()}.{@link List#get(int)}</code> and returns 
     * the Java <code>Instant</code> value.  
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Should return <code>null</code> if the data table has not been initialized.</li>
     * <li>Should return <code>null</code> if the data table has not been populated.</li>
     * </ul>
     * </p>  
     * 
     * @param indRow row index
     * 
     * @return  timestamp at the given row index,
     *          or <code>null</code> if the data table has not been populated
     * 
     * @throws IndexOutOfBoundsException    index is out of bounds (0 &le; index &lt; <code>{@link #getRowCount()}</code>)
     */
    default public Instant getTimestamp(int indRow) throws IndexOutOfBoundsException {
        return this.getTimestamps().get(indRow);
    };
    
    /**
     * <p>
     * Return the single value of time-series data at the given indices, or <code>null</code> if the data table
     * has not been populated or initialized. 
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getColumn(int)}</code></b>.
     * <br/><br/>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * simply calls <code>{@link #getColumn(int)}.{@link IDataColumn#getValue(int)}</code> and returns 
     * the Java <code>Object</code> value.  
     * </ul>
     * </p> 
     * <p>
     * <h2>WARNING:</h2>
     * The data type of the returned value could be any heterogeneous type defined in
     * <code>{@link DpSupportedType}</code>.  
     * This includes a scalar value, an array, a data structure, an image, etc.
     * Use <code>{@link #getColumnType(int)}</code> to determine the returned value data type.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Should return <code>null</code> if the data table has not been populated.</li>
     * <li>Should return <code>null</code> if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @param indRow    data table row index
     * @param indCol    data table column index
     * 
     * @return  data value (object) at the given indices,
     *          or <code>null</code> if the data table has not been populated
     * 
     * @throws IndexOutOfBoundsException an index is out of bounds = [0, {@link getRowCount}</code>) &times; [0, <code>{@link #getColumnCount()}</code>)
     * @throws ArithmeticException       overflow exception - table is too large 
     */
    default public Object getValue(int indRow, int indCol) throws IndexOutOfBoundsException, ArithmeticException {
        return this.getColumn(indCol).getValue(indRow);
    }

    /**
     * <p>
     * Return the single value of time-series data at the given indices, or <code>null</code> if the data table
     * has not been populated or initialized. 
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getColumn(String)}</code></b>.
     * <br/><br/>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * simply calls <code>{@link #getColumn(String)}.{@link IDataColumn#getValue(int)}</code> and returns 
     * the Java <code>Object</code> value.  
     * </ul>
     * </p> 
     * <p>
     * <h2>WARNING:</h2>
     * The data type of the returned value could be any heterogeneous type defined in
     * <code>{@link DpSupportedType}</code>.  
     * This includes a scalar value, an array, a data structure, an image, etc.
     * Use <code>{@link #getColumnType(String)}</code> to determine the returned value data type.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Should return <code>null</code> if the data table has not been populated.</li>
     * <li>Should return <code>null</code> if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @param indRow    data table row index
     * @param strName    data table column name
     * 
     * @return  data value (object) at the given row index and column name,
     *          or <code>null</code> if the data table has not been populated
     * 
     * @throws IndexOutOfBoundsException row index is out of bounds = [0 &le; index < {@link getRowCount}</code>))
     * @throws NoSuchElementException    the given column name is not within the managed column collection
     * @throws ArithmeticException       overflow exception - table is too large 
     */
    default public Object getValue(int indRow, String strName) throws IndexOutOfBoundsException, NoSuchElementException, ArithmeticException {
        return this.getColumn(strName).getValue(indRow);
    }

    /**
     * <p>
     * Returns all row data at the given table row index as an <code>Object</code> array.
     * </p>
     * <p>
     * Since data tables are composed of <code>{@link IDataColumn}</code> implementations, tables
     * are required to create new containers or objects to return data values across rows.  
     * This operation returns a Java array, providing fast lookup via indexing.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getValue(int, int)}</code></b>.
     * <br/><br/>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * calls <code>{@link #getValue(int, int)}, in order of column index, to obtain 
     * a Java <code>Object</code> for each column.  
     * A new Java array is returned containing the data values in order of index. 
     * </ul>
     * </p> 
     * <p>
     * <h2>WARNING:</h2>
     * The data types within the returned array could be any heterogeneous type defined in
     * <code>{@link DpSupportedType}</code>.  
     * Specifically, each value object within the returned array can be of different type. 
     * Use <code>{@link #getColumnType(int)}</code> to determine the returned value data type per 
     * array index.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The value ordering is that of the column ordering.</li>
     * <li>Should return <code>null</code> if the data table has not been populated.</li>
     * <li>Should return <code>null</code> if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @param indRow    data table row index
     * 
     * @return  all row data values at given row index,
     *          or <code>null</code> if the data table has not been populated
     * 
     * @throws IndexOutOfBoundsException row index out of bounds (0 &le; index &lt; <code>{@link #getRowCount()}</code>) 
     * @throws ArithmeticException       overflow exception - table is too large 
     */
    default public Object[] getRowValues(int indRow) throws IndexOutOfBoundsException {
        
        // Get the number of columns and check it
        Integer         cntCols = this.getColumnCount();
        
        if (cntCols == null)
            return null;
        
        // Create object array as ordered column values (at indRow) across column collection 
        Object[] arrVals = IntStream
                .range(0, cntCols)
                .mapToObj(i -> this.getValue(indRow, i) )
                .toArray();
        
        return arrVals;
        
    };
    
    /**
     * <p>
     * Returns all row data at the given row index as an ordered Java <code>List</code>.
     * </p>
     * <h2>WARNING:</h2>
     * The data types within the returned ordered list could be any heterogeneous type defined in
     * <code>{@link DpSupportedType}</code>.  
     * Specifically, each value object within the returned array can be of different type. 
     * Use <code>{@link #getColumnType(int)}</code> to determine the returned value data type per 
     * ordered list index.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getRowValues(int)}</code></b>.
     * <br/><br/>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * calls <code>{@link #getRowValues(int)}</code> to obtain a Java <code>Object</code> array of data values.
     * The Java array is used to populate a Java <code>{@link List}</code> which is then returned.
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Returns an empty collection if the data table has not been populated.</li>
     * <li>Returns an empty collection if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @param indRow    row index, must be less than data set size
     * 
     * @return  all row data at given row index,
     *          or <code>null</code> if the data set has not been initialized
     * 
     * @throws IndexOutOfBoundsException column index is greater than column count 
     * @throws ArithmeticException       overflow exception - table is too large 
     */
    default public List<Object> getRowValuesAsList(int indRow) throws IndexOutOfBoundsException {
        Object[] arrVals     = this.getRowValues(indRow);
        
        if (arrVals == null)
            return List.of();

        List<Object> lstVals = List.of(arrVals);
        
        return lstVals;
    }
    
    /**
     * <p>
     * Return the name of the data column at the given column index.
     * </p>
     * <p>
     * <code>IDataTable</code> implementations are not prescribed to follow any indexing scheme.  The indices
     * assigned to data columns are determined completely by the implementing class.
     * This method is provided to determine the ordering of table columns within the data table. 
     * This method is included determination of table column ordering within the data table. 
     * <p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getColumn(int)}</code></b>.
     * <br/></br>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * calls <code>{@link #getColumn(int)}.{@link IDataColumn#getName()}</code> to obtain the returned  
     * a Java <code>String</code> value.  
     * </ul>
     * </p> 
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Should return <code>null</code> if the data table has not been populated.</li> 
     * </ul>
     * </p>  
     * 
     * @param indCol    column index - must be one less than the number of columns 
     * 
     * @return name of data column at the given column index, 
     *         or <code>null</code> if the data table has not been populated
     * 
     * @throws IndexOutOfBoundsException    column index is out of bounds (0 &le; index &lt; <code>{@link #getColumnCount()}</code>)
     * 
     * @see #getColumnIndex(String)
     */
    default public String   getColumnName(int indCol) throws IndexOutOfBoundsException {
        IDataColumn<Object> col = this.getColumn(indCol);
        
        if (col == null)
            return null;
        
        return col.getName();
    }

    /**
     * <p>
     * Returns the data type of the table data column with the given column index.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getColumn(int)}</code></b>.
     * <br/></br>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * is just a convenience method.  
     * It calls <code>{@link #getColumn(int)}.{@link IDataColumn#getType()}</code> 
     * to obtain the returned type.
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Returns <code>null</code> if the data table has not been populated.</li>
     * <li>Returns <code>null</code> if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @param indCol column index for the returned object
     * 
     * @return the column data type enumeration constant
     * 
     * @throws IndexOutOfBoundsException column index out of bounds (0 &le; index &lt; <code>{@link #getColumnCount()}</code>)
     */
    default public DpSupportedType getColumnType(int indCol) throws IndexOutOfBoundsException {
        IDataColumn<Object>     col = this.getColumn(indCol);
        
        if (col == null || col.getSize()==0)
            return null;
    
        return col.getType();        
    }

    /**
     * <p>
     * Returns the data type of the table data column with the given name.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getColumn(String)}</code></b>.
     * <br/></br>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * is just a convenience method.  
     * It calls <code>{@link #getColumn(String)}.{@link IDataColumn#getType()}</code> 
     * to obtain the returned type.
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Returns <code>null</code> if the data table has not been populated.</li>
     * <li>Returns <code>null</code> if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @param strName column name for the returned object
     * 
     * @return the column data type enumeration constant
     * 
     * @throws NoSuchElementException     the given column name is not within the managed column collection
     */
    default public DpSupportedType getColumnType(String strName) throws NoSuchElementException {
        IDataColumn<Object>     col = this.getColumn(strName);
        
        if (col == null)
            return null;
                
        if (col.getSize()==0)
            return null;
    
        return col.getType();        
    }

    /**
     * <p>
     * Returns the size (number of data values) of the data table column with the given index,
     * or 0 if the data table has not been populated.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getColumn(int)}</code></b>.
     * <br/></br>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * is just a convenience method.  
     * It calls <code>{@link #getColumn(int)}.{@link IDataColumn#getSize()}</code> 
     * to obtain the returned type.
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Returns 0 if the data table has not been populated.</li>
     * <li>Returns <code>null</code> if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @param indCol data table column index
     * 
     * @return  the number of data values within the specified data table column,
     *          or 0 if the data table has not been populated
     * 
     * @throws IndexOutOfBoundsException column index out of bounds (0 &le; index &lt; <code>{@link #getColumnCount()}</code>)
     */
    default public Integer getColumnSize(final int indCol) throws IndexOutOfBoundsException {
        IDataColumn<Object>     col = this.getColumn(indCol);
        
        if (col == null)
            return null;
        
        return col.getSize();
    }

    /**
     * <p>
     * Returns the size (number of data values) of the data table column with the given name,
     * or 0 if the data table has not been populated.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getColumn(String)}</code></b>.
     * <br/></br>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * is just a convenience method.  
     * It calls <code>{@link #getColumn(String)}.{@link IDataColumn#getSize()}</code> 
     * to obtain the returned type.
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Returns 0 if the data table has not been populated.</li>
     * <li>Returns <code>null</code> if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @param strName name of the table column
     * 
     * @return  the number of data values within the specified data table column,
     *          or 0 if the data table has not been populated
     * 
     * @throws NoSuchElementException column does not exist within column collection
     */
    default public Integer getColumnSize(String strName) throws NoSuchElementException {
        IDataColumn<Object>     col = this.getColumn(strName);
        
        if (col == null)
            return null;
        
        return col.getSize();
    }

    /**
     * <p>
     * Returns all column data of table column for the given table column index as an ordered list, 
     * or empty if the data set has not been populated.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getColumn(int)}</code></b>.
     * <br/></br>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * is just a convenience method.  
     * It calls <code>{@link #getColumn(int)}.{@link IDataColumn#getValues()}</code> 
     * to obtain a Java <code>Object</code> list which is then returned.
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The returned object should be immutable - Do NOT modify this object.</li>
     * <li>Returns an empty collection if the data table has not been populated.</li>
     * <li>Returns an empty collection if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @param indCol table column index for the returned object
     * 
     * @return  vector of column data values for the given column index,
     *          or empty if the data table has not been populated
     * 
     * @throws IndexOutOfBoundsException the column index is too large (&ge; <code>{@link #getColumnCount()}</code>)
     */
    default public List<Object> getColumnData(final int indCol) throws IndexOutOfBoundsException {
        IDataColumn<Object> col = this.getColumn(indCol);
        
        if (col == null)
            return List.of();
        
        return col.getValues();
    }

    /**
     * <p>
     * Returns all column data for the given column name as an ordered <code>Object</code> list, 
     * or empty if the data table has not been populated.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getColumn(String)}</code></b>.
     * <br/></br>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * is just a convenience method.  
     * It calls <code>{@link #getColumn(String)}.{@link IDataColumn#getValuesTyped()}</code> 
     * to obtain a Java <code>Object</code> list which is then returned.
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The returned object should be immutable - Do NOT modify this object.</li>
     * <li>Returns an empty collection if the data table has not been populated.</li>
     * <li>Returns an empty collection if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @param strName column name for the returned object
     * 
     * @return  vector of column data values for the given column index,
     *          or empty if the data table has not been populated
     * 
     * @throws IllegalArgumentException   the given column name was <code>null</code> 
     * @throws NoSuchElementException     the given column name is not within the managed column collection
     */
    default public List<Object> getColumnData(String strName) throws IllegalArgumentException, NoSuchElementException {
    
        // Exception checking
        if (strName == null)
            throw new IllegalArgumentException("IDataSet#getColumnData(String) - Cannot use null value as argument <strName>");
        
        IDataColumn<Object> col = this.getColumn(strName);
        
        if (col == null)
            return List.of();
        
        return col.getValues();
    }

    /**
     * <p>
     * Returns all column data for the given table column index as an ordered list of typed values, 
     * or empty if the data table has not been populated. 
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getColumn(int)}</code></b>.
     * <br/></br>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * is just a convenience method.  
     * It calls <code>{@link #getColumn(int)}.{@link IDataColumn#getValuesTyped()}</code> 
     * to obtain a Java <code>Object</code> list which is then returned.
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The returned object should be immutable - Do NOT modify this object.</li>
     * <li>Returns an empty collection if the data table has not been populated.</li>
     * <li>Returns an empty collection if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @param <T> data type of the column values
     * 
     * @param indCol column index for the returned object
     * 
     * @return  vector of column values for the given column index
     *          or <code>null</code> if the data set has not been initialized
     * 
     * @throws IndexOutOfBoundsException column index out of bounds (0 &le; index &lt; <code>{@link #getColumnCount()}</code>)
     * @throws ClassCastException        generic parameter <code>T</code> is not compatible with column type
     */
    @SuppressWarnings("unchecked")
    default public <T extends Object> List<T> getColumnDataTyped(int indCol) throws IndexOutOfBoundsException, ClassCastException {

        IDataColumn<Object> colObj = this.getColumn(indCol);
        
        // Check column
        if (colObj == null)
            return List.of();
        
        if (colObj.getSize() == 0)
            return List.of();
        
        // Check column type
        Object          objVal = colObj.getValue(0);
        DpSupportedType enmType = colObj.getType();
        if (!enmType.isAssignableFrom(objVal.getClass()))
            throw new ClassCastException("Generic parameter T is incompatible with columnType " + enmType);

        // Can safely cast 
        List<T>     lstVals = (List<T>)colObj.getValues();
        
        return lstVals;
    }
    
    /**
     * <p>
     * Returns all column data for the given column name as an ordered list of typed values, 
     * or empty if the data table has not been populated.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getColumn(String)}</code></b>.
     * <br/></br>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * is just a convenience method.  
     * It calls <code>{@link #getColumn(String)}.{@link IDataColumn#getValues()}</code> 
     * to obtain a Java <code>Object</code> list which is then returned.
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The returned object should be immutable - Do NOT modify this object.</li>
     * <li>Returns an empty collection if the data table has not been populated.</li>
     * <li>Returns an empty collection if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @param <T> data type of the column values
     * 
     * @param strName column name for the returned object
     * 
     * @return  ordered list of typed column values for the given column name, 
     *          or empty if the data table has not been populated
     * 
     * @throws IllegalArgumentException the given column label was <code>null</code> 
     * @throws NoSuchElementException   the given column name is not within the managed column collection
     * @throws ClassCastException       generic parameter <code>T</code> is not compatible with column type
     */
    @SuppressWarnings("unchecked")
    default public <T extends Object> List<T> getColumnDataTyped(String strName) 
            throws IllegalArgumentException, NoSuchElementException, ClassCastException {

        // Exception checking
        if (strName == null)
            throw new IllegalArgumentException("IDataSet#getColumnDataTyped(String) - Cannot use null value as argument <strLbl>");
        
        IDataColumn<Object> colObj = this.getColumn(strName);
        
        if (colObj == null)
            return List.of();
        
        if (colObj.getSize() == 0)
            return List.of();
        
        // Check column type
        Object          objVal = colObj.getValue(0);
        DpSupportedType enmType = colObj.getType();
        if (!enmType.isAssignableFrom(objVal.getClass()))
            throw new ClassCastException("Generic parameter T is incompatible with columnType " + enmType);

        // Can safely cast 
        List<T>     lstVals = (List<T>)colObj.getValues();
        
        return lstVals;
    }
    
//    /**
//     * <p>
//     * Returns all data columns of the data table as a list.
//     * </p>
//     * <h2>Default Implementation</h2>
//     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
//     * Implementing classes may wish to override the default implementation if better options are available.
//     * <br/><br/>
//     * <ul>
//     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
//     * calls <code>{@link #getColumnNames()}</code> to the list of data column names.  A new immutable list
//     * of data columns is created via repeated invocations of <code>{@link #getColumn(String)}</code> which is
//     * then returned.
//     * </ul>
//     * </p> 
//     * <p>
//     * <h2>NOTES:</h2>
//     * <ul>
//     * <li>Returns an empty collection if the data table has not been populated.</li>
//     * <li>Returns <code>null</code> value if the data table has not been initialized.</li>
//     * </ul>
//     * </p>  
//     * 
//     * @return  immutable list of table data columns in order given by <code>{@link #getColumnNames()}</code>
//     */
//    default public List<IDataColumn<Object>>   getColumns() {
//        List<String>    lstNames = this.getColumnNames();
//        
//        if (lstNames == null)
//            return null;
//        
//        return lstNames.stream().map(s -> this.getColumn(s)).toList();
//    }

    /**
     * <p>
     * Returns the length (number of data values) of the smallest data column in the table. 
     * </p>
     * <p> 
     * Since the table is has independent data columns, the number of data values in each 
     * column can be different.
     * Although pathological, the method is provided to indicate that the situation is possible.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getColumnNames()} {@link #getColumnSize(String)}</code></b>.
     * <br/></br>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * iterates (streams) through the list of table column names from <code>{@link #getColumnNames()}</code>.   
     * It calls <code>{@link IDataColumn#getColumnSize(String)}</code> parsing the values 
     * to obtain the returned value.
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Returns <code>null</code> if the data table has not been populated.</li>
     * <li>Returns 0 if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @return  length of the smallest data column within the table, 
     *          or 0 if the table has not been populated
     */
    default public Integer getColumnSizeMin() {
        List<String> lstColNms = this.getColumnNames();
        
        if (lstColNms == null)
            return null;
        
        if (lstColNms.isEmpty())
            return 0;
        
        return lstColNms
                .stream()
                .mapToInt(s -> this.getColumnSize(s) )
                .min()
                .getAsInt();
    }

    /**
     * <p>
     * Returns the length (number of data values) of the largest data column in the table. 
     * </p>
     * <p> 
     * Since the table is has independent data columns, the number of data values in each 
     * column can be different.
     * Although pathological, the method is provided to indicate that the situation is possible.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getColumnNames()} {@link #getColumnSize(String)}</code></b>.
     * <br/></br>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * iterates (streams) through the list of table column names from <code>{@link #getColumnNames()}</code>.   
     * It calls <code>{@link IDataColumn#getColumnSize(String)}</code> parsing the values 
     * to obtain the returned value.
     * </ul>
     * </p> 
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Returns <code>null</code> if the data table has not been populated.</li>
     * <li>Returns 0 if the data table has not been initialized.</li>
     * </ul>
     * </p>  
     * 
     * @return  Size of the largest data column in the table at time of calling
     *          (0 if the table has not been initialized)
     */
    default public Integer getColumnSizeMax() {
        List<String> lstColNms = this.getColumnNames();
        
        if (lstColNms == null)
            return null;
        
        if (lstColNms.isEmpty())
            return 0;
        
        return lstColNms
                .stream()
                .mapToInt( s -> this.getColumnSize(s) )
                .max()
                .getAsInt();
    }

    
    //
    // Memory Allocation
    //
    
    /**
     * <p>
     * Compute and return the estimated total memory allocation for the data in this table (in bytes).
     * </p>
     * <p>
     * Typically the returned value should be a conservative value, an overestimate favoring 64-bit architectures. 
     * It should also include allocation for the timestamps.
     * </p>
     * <p>
     * <h2>Default Implementation</h2>
     * Interface <code>{@link IDataTable}</code> provides a default implementation for this operation.
     * Implementing classes may wish to override the default implementation if better options are available.
     * <br/><br/>
     * <ul>
     * <b>Uses <code>{@link #getColumn(int)}</code></b>.
     * <br/></br>
     * The <code>default</code> implementation provided in <code>{@link IDataTable}</code> interface
     * iterates through all table columns invoking <code>{@link IDataColumn#allocationSize()}</code>
     * to sum the allocation size of each table column.
     * The allocation size of the timestamps is added by multiplying <code>{@link #getRowCount()}</code>
     * by the size of a time instant (two <code>{@link Long}</code> objects).   
     * </ul>
     * </p> 
     * <p>
     * <h2>WARNINGS:</h2>
     * The result returned by this operation is highly dependent upon the implementation (e.g., within <code>{@link IDataColumn}</code>).
     * <ul>
     * <li>Results should not be used for critical operations, only as estimates for evaluation purposes.</li>
     * <li>Implementations choosing not to supported this operation should throw a <code>UnsupportedOperationException</code>.
     * </ul>
     * </p>
     * 
     * @return  memory allocation required for all the data in the table (in bytes)
     * 
     * @throws UnsupportedOperationException    operation not supported by contained data column 
     * @throws ArithmeticException              error in allocation calculation (e.g., table to large)
     */
    default public long allocationSize() throws UnsupportedOperationException, ArithmeticException {
        long    lngSzTms = 2 * this.getRowCount() * Long.BYTES;
//        long    lngSzCols = this.getColumns().stream().mapToLong(IDataColumn::allocationSize).sum();
        long    lngSzCols = IntStream.range(0, this.getColumnCount()).mapToLong(i -> this.getColumn(i).allocationSize()).sum();
        
        return lngSzTms + lngSzCols;
    }

    

    // 
    // Debugging
    //
    
    /**
     * Creates a textual representation of the head of the table whose head size
     * is given by the argument.  If the given head size is larger than the number 
     * of table rows it is reduced to the table row count. 
     * 
     * @param szHead    number of table rows to print
     * 
     * @return  string printout of the table head.
     */
    default public String printHead(int szHead) {

        // Get list of column names and check it
        List<String> lstColNms = this.getColumnNames();
        if (lstColNms == null || lstColNms.isEmpty())
            return "IDataTable#printHead() - table is empty or uninitialized!";

        // There is data - get the minimum row size
        //  Reduce the head size if larger than the table size
        int cntRows = this.getColumnSizeMin(); 
        
        if (szHead > cntRows)
            szHead = cntRows;

        // Start the buffer and create the printout header
        StringBuffer    buf = new StringBuffer("timestamp \t");

        lstColNms.forEach( s -> buf.append(s + "\t") );
        buf.append("\n");
        
        // Print out the timestamps and data values
        for (int iRow=0; iRow<szHead; iRow++) { 
            // for each row
            Instant insTms = this.getTimestamp(iRow);
            
            buf.append(insTms.toString() + "\t");
            
            for (int iCol=0; iCol<this.getColumnCount(); iCol++) { 
                // for each column
                Object objVal = this.getValue(iRow, iCol);

                if (objVal != null)
                    buf.append(objVal.toString() + "\t");
                else
                    buf.append("null \t");
            }
            buf.append("\n");
        }

        return buf.toString();
    }
    
    /**
     * Creates a textual representation of the tail of the table whose tail size
     * is given by the argument.  If the given tail size is larger than the number 
     * of table rows it is reduced to the table row count. 
     * 
     * @param szTail    number of table rows to print
     * 
     * @return  string printout of the table head.
     */
    default public String printTail(int szTail) {

        // Get list of column names and check it
        List<String> lstColNms = this.getColumnNames();
        if (lstColNms == null || lstColNms.isEmpty())
            return "IDataTable#printTail() - table is empty or uninitialized!";

        // There is data - get the minimum row size
        //  Reduce the tail size if larger than the table size
        int cntRows = this.getColumnSizeMin();

        if (szTail > cntRows)
            szTail = cntRows;
        
        
        // Start the buffer and create the printout header
        StringBuffer    buf = new StringBuffer("timestamp \t");

        lstColNms.forEach( s -> buf.append(s + "\t") );
        buf.append("\n");
        
        // Print out the timestamps and data values
        for (int iRow=cntRows-szTail; iRow<cntRows; iRow++) {
            // for each row
            Instant insTms = this.getTimestamp(iRow);
            
            buf.append(insTms.toString() + "\t");
            
            for (int iCol=0; iCol<this.getColumnCount(); iCol++) {
                // for each column
                Object objVal = this.getValue(iRow, iCol);
                
                if (objVal != null)
                    buf.append(objVal.toString() + "\t");
                else
                    buf.append("null \t");
            }
            buf.append("\n");
        }
        
        return buf.toString();
    }
    

}
