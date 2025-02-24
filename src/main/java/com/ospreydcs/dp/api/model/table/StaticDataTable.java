/*
 * Project: dp-api-common
 * File:	StaticDataTable.java
 * Package: com.ospreydcs.dp.api.query.model
 * Type: 	StaticDataTable
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
 * @since Jan 8, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model.table;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ospreydcs.dp.api.common.IDataColumn;
import com.ospreydcs.dp.api.common.IDataTable;

/**
 * <p>
 * Implements a static table of heterogeneous, time-correlated, time-series data.
 * </p>
 * <p>
 * Implements the <code>{@link IDataTable}</code> interface as a static data table.  The table can be modified
 * (e.g., to add data columns) post construction, but all data is maintained statically within Java containers.
 * The data table is composed of a collection of <code>{@link IDataColumn}</code> instances which may be accessed
 * either by table column index (determined by the table) or column name.
 * See Java documentation for interface <code>{@link IDataTable}</code> for details of data table operation. 
 * <p>
 * </p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>
 * The data within each column is homogeneous, that is, it all has the same data type.
 * </li>
 * <br/>
 * <li>
 * The data types between data columns may differ.  Thus, the term "heterogeneous data" refers to the table.
 * </li>
 * <br/>
 * <li>
 * Column names are always unique.
 * </li>
 * <br/>
 * <li>
 * Within the table, columns are ordered arbitrarily, however, once the table is initialized column
 * indices do not change.
 * </li>
 * </ul>
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since Jan 8, 2024
 *
 * @see IDataTable
 * @see IDataColumn
 */
public class StaticDataTable implements IDataTable, Serializable {
    

    //
    // Class Constants
    //
    
    /** <code>Serializable</code> interface serialization ID - used for allocation size estimation */
    private static final long serialVersionUID = -2035823436849106272L;

    
    //
    // Attributes
    //
    
    /** The ordered vector of timestamps corresponding to each data column (as a data time series) */
    private final ArrayList<Instant>                vecTms;
    
    /** The ordered vector of table data columns */
    private final ArrayList<IDataColumn<Object>>    vecCols;
    
    /** The vector of column names in order of indexing */
    private final ArrayList<String>                 vecColNms;
    
    /** Map of column names to table column index */
    private final Map<String, Integer>              mapNmToInd;
    
    /** Map of column names to table columns */
    private final Map<String, IDataColumn<Object>>  mapNmToCol;
    
    
    //
    // State Variables
    //
    
    /** Indicates whether or not table is complete */
    private boolean     bolTableCompleted = false;
    

    //
    // Creators
    //
    

    //
    // Constructors
    //

    /**
     * <p>
     * Default Constructor - Constructs a new, unpopulated instance of <code>StaticDataTable</code>.
     * </p>
     * <p>
     * The new <code>StaticDataType</code> instance is initialized but left unpopulated.  The 
     */
    public StaticDataTable() {
        this.vecTms = new ArrayList<>();
        this.vecCols = new ArrayList<>();
        
        this.vecColNms = new ArrayList<>();
        this.mapNmToCol = new HashMap<>();
        this.mapNmToInd = new HashMap<>();
    }
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>StaticDataTable</code>.
     * </p>
     * <p>
     * Constructs new, initialized instance of <code>StaticDataTable</code> fully populated with the
     * argument data.  The collection of data columns need not be ordered, however, the table column indices
     * will following the ordering if it exists.  The data values within each data column MUST be ordered 
     * according to the given list of timestamps.  That is, the data within the arguments is assumed to be
     * correlated.
     * </p>
     *
     * @param vecTms    ordered vector of timestamps for each column
     * @param setCols   unordered collection of data columns 
     * 
     * @throws  IllegalStateException       column data contained columns with duplicate names
     * @throws  IllegalArgumentException    column data contained columns with bad sizes (not equal to timestamp data)
     */
    public StaticDataTable(List<Instant> vecTms, Collection<IDataColumn<Object>> setCols) 
            throws IllegalStateException, IllegalArgumentException {
        this.vecTms = new ArrayList<>(vecTms);
        this.vecCols = new ArrayList<>(setCols);
        
        this.vecColNms = this.vecCols.stream().sequential().collect(ArrayList::new, (vec, col) -> vec.add(col.getName()), (agg, vec) -> agg.addAll(vec));
        this.mapNmToCol = this.vecCols.stream().collect(Collectors.toMap(c -> c.getName(), c -> c)); // throws IllegalStateException
        this.mapNmToInd = IntStream.range(0, this.vecCols.size()).collect(HashMap::new, (map, i) -> map.put(this.vecCols.get(i).getName(), i), (c, m) -> c.putAll(m));

        // Check the sizes
        Integer         cntRows = vecTms.size();
        List<String>    lstBadCols = this.vecCols.stream()
                                .filter(col -> !col.getSize().equals(cntRows))
                                .<String>map(IDataColumn::getName)
                                .toList();
        
        if (!lstBadCols.isEmpty())
            throw new IllegalArgumentException("Column size not equal to timestamps size " +cntRows + " for columns " + lstBadCols);
        
        this.bolTableCompleted = true;
    }

    
    //
    // Table Operations
    //
    
    /**
     * <p>
     * Sets the timestamps for the data table.
     * </p>
     * <p>
     * <h2>Default Construction Operation</h2>
     * This method should be invoked whenever the default constructor is used where the data table
     * is populated externally with operations <code>{@link #setTimestamps(ArrayList)}</code> and 
     * <code>{@link #addColumn(IDataColumn)}</code>.
     * <br/><br/>
     * <ul>
     * This method sets the internal timestamp vector to the given ordered list of time instants.
     * <ul>
     * <li>If the timestamp vector has already been set an exception is thrown.</li>
     * <li>If the the timestamp vector has a different size than any existing data columns an exception is thrown.</li> 
     * </ul>
     * </ul>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Call the method <code>{@link #setTableCompleted(boolean)}</code> when table population is completed.
     * </p>
     * 
     * @param lstTms    ordered list of timestamps for the data table
     * 
     * @return          <code>true</code> if the table timestamps were successfully assigned,
     *                  <code>false</code> otherwise
     *                  
     * @throws IllegalArgumentException the argument size does not match existing column data
     * @throws IllegalStateException    the table timestamp have already been assigned.
     */
    public boolean  setTimestamps(List<Instant> lstTms) 
            throws IllegalArgumentException, IllegalStateException {
        
        // Check if timestamps have already been assigned
        if (!this.vecCols.isEmpty())
            throw new IllegalStateException("Timestamps have already been assigned for this table.");
        
        // Check the size against existing data columns
        if (!this.vecCols.isEmpty()) {
            Integer     szCols = this.vecCols.get(0).getSize();
            
            if (lstTms.size() != szCols) 
                throw new IllegalArgumentException("Timestamp vector must have size of existing columns " + szCols);
        }
        
        // Everything is okay
        return this.vecTms.addAll(lstTms);
    }
    
    /**
     * <p>
     * Adds the given data column to the existing collection of table columns.
     * </p>
     * <p>
     * <h2>Default Construction Operation</h2>
     * This method should be invoked whenever the default constructor is used where the data table
     * is populated externally with operations <code>{@link #setTimestamps(ArrayList)}</code> and 
     * <code>{@link #addColumn(IDataColumn)}</code>.
     * <br/><br/>
     * <ul>
     * This method adds the given data column to the collection of existing data columns and registers its name
     * in the name-to-column map <code>{@link #mapNmToCol}</code>.
     * <ul>
     * <li>If the data column size does not match the timestamp vector size an exception is thrown.</li>
     * <li>If the data column size does not match any existing column sizes an exception is thrown.</li>
     * <li>If the data column name already exists within the column collection an exception is thrown.</li>
     * </ul>
     * </ul>
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Call the method <code>{@link #setTableCompleted(boolean)}</code> when table population is completed.
     * </p>
     * 
     * @param <T>       data type of the column data
     * @param colTyped  data column to be added to table 
     * 
     * @return          <code>true</code> if the data column was successfully added to the table,
     *                  <code>false</code> otherwise
     *                  
     * @throws IllegalArgumentException data column had bad size (did not match timestamps or existing columns)
     * @throws IllegalStateException    duplicate data column name (data column name already registered in table)
     */
    @SuppressWarnings("unchecked")
    public <T extends Object>   boolean  addColumn(IDataColumn<T>   colTyped) 
            throws IllegalArgumentException, IllegalStateException {
        
        // Check argument size against timestamp vector
        if (!this.vecTms.isEmpty()) {
            Integer     szTms = this.vecTms.size();
        
            if (colTyped.getSize() != szTms)
                throw new IllegalArgumentException("Data column must have timestamps size " + szTms);
        }
        
        // Check the argument against existing columns
        if (!this.vecCols.isEmpty()) {
            Integer     szCols = this.vecCols.get(0).getSize();
            
            if (colTyped.getSize() != szCols)
                throw new IllegalArgumentException("Data column mst have existing column size " + szCols);
        }
        
        // Check the column name
        String      strName = colTyped.getName();
        if ( this.mapNmToCol.containsKey(strName) )
            throw new IllegalStateException("Data column already exists in table collection: " + strName);

        
        // Add the data column to the existing collection
        IDataColumn<Object> colObj =  (IDataColumn<Object>)colTyped;
        Integer             indCol = this.vecCols.size();

        this.vecCols.add(colObj);
        
        this.vecColNms.add(strName);
        this.mapNmToCol.put(strName, colObj);
        this.mapNmToInd.put(strName, indCol);
        
        return true;
    }
    
    /**
     * <p>
     * Set the <code>{@link #bolTableCompleted}</code> flag to the given argument.
     * </p>
     * <p>
     * <h2>Default Construction Operation</h2>
     * This method should be invoked whenever the default constructor is used where the data table
     * is populated externally with operations <code>{@link #setTimestamps(ArrayList)}</code> and 
     * <code>{@link #addColumn(IDataColumn)}</code>.
     * <br/><br/>
     * <ul>
     * Once the client has finished populating the data table this method should be invoked with argument
     * equal to <code>true</code>, thus indicating that table population is complete.
     * </ul>
     * </p>
     * 
     * @param bolTableCompleted     <code>true</code> indicates that client has finished populating the table
     * 
     * @throws IllegalStateException    attempt to set <cod>true</code> when table contains no timestamps and/or column data
     */
    public void setTableCompleted(boolean bolTableCompleted) throws IllegalStateException {

        if (bolTableCompleted && this.vecTms.isEmpty())
            throw new IllegalStateException("The table contains no timestamps.");
        
        if (bolTableCompleted && this.vecCols.isEmpty())
            throw new IllegalStateException("The table contains no column data.");
        
        this.bolTableCompleted = bolTableCompleted;
    }

    
    //
    // IDataTable Interface
    //
    
    /**
     * @see com.ospreydcs.dp.api.model.IDataTable#isTableComplete()
     */
    @Override
    public boolean isTableComplete() {
        return this.bolTableCompleted;
    }

    /**
     * <p>
     * Always returns <code>false</code>.  
     * There is no internal error mechanism within <code>StaticDataTable</code>.
     * </p>
     *
     * @return  <code>false</code>
     * 
     * @see com.ospreydcs.dp.api.common.IDataTable#hasError()
     */
    @Override
    public boolean hasError() {
        return false;
    }

    /**
     * @see com.ospreydcs.dp.api.model.IDataTable#getRowCount()
     */
    @Override
    public Integer getRowCount() {
        return this.vecTms.size();
    }
    
    /**
     * @see com.ospreydcs.dp.api.common.IDataTable#clear()
     */
    @Override
    public void clear() {
        this.vecCols.forEach(IDataColumn::clear);
        
        this.vecTms.clear();
        this.vecColNms.clear();
        this.vecCols.clear();
        this.mapNmToCol.clear();
        this.mapNmToInd.clear();
    }

    /**
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumnCount()
     */
    @Override
    public Integer getColumnCount() {
        return this.vecCols.size();
    }

//    /**
//     *
//     * @see @see com.ospreydcs.dp.api.model.IDataTable#getColumnName(int)
//     */
//    @Override
//    public String getColumnName(int indCol) throws IndexOutOfBoundsException {
//        return this.vecCols.get(indCol).getName();
//    }

    /**
     * 
     * @see com.ospreydcs.dp.api.common.IDataTable#getColumnName(int)
     */
    @Override
    public int  getColumnIndex(String strColName) throws NoSuchElementException {

        Integer indCol = this.mapNmToInd.get(strColName);
        
        if (indCol == null)
          throw new NoSuchElementException("Table contains no column with name " + strColName);
        
        return indCol;
            
//        OptionalInt index = IntStream.range(0, this.vecCols.size()).filter(i -> this.vecCols.get(i).getName().equals(strColName)).findAny();
//        
//        if (index.isEmpty())
//            throw new NoSuchElementException("Table contains no column with name " + strColName);
//        
//        return index.getAsInt();
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getColumnNames()
     */
    @Override
    public final List<String> getColumnNames() {
        return this.vecColNms;
//        // Create a column name list in order of indexing
//        List<String>    lstNms = this.vecCols
//                                .stream()
//                                .<String>map(IDataColumn::getName)
//                                .toList();
//        
//        return lstNms;
    }

//    /**
//     *
//     * @see @see com.ospreydcs.dp.api.model.IDataTable#getTimestamp(int)
//     */
//    @Override
//    public Instant getTimestamp(int indRow) throws IndexOutOfBoundsException {
//        return this.vecTms.get(indRow);
//    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getTimestamps()
     */
    @Override
    public final List<Instant>    getTimestamps() {
        return this.vecTms;
    }

//    /**
//     *
//     * @see @see com.ospreydcs.dp.api.model.IDataTable#getValue(int, int)
//     */
//    @Override
//    public Object getValue(int indRow, int indCol) throws IndexOutOfBoundsException, ArithmeticException {
//        IDataColumn<Object>     col = this.vecCols.get(indCol);
//        Object                  val = col.getValue(indRow);
//        
//        return val;
//    }

//    /**
//     *
//     * @see @see com.ospreydcs.dp.api.model.IDataTable#getRowValues(int)
//     */
//    @Override
//    public Object[] getRowValues(int indRow) throws IndexOutOfBoundsException {
//        Integer         cntCols = this.getColumnCount();
//        
//        // Create object array as ordered column values (at indRow) across column collection 
//        Object[] arrVals = IntStream
//                .range(0, cntCols)
//                .mapToObj(i -> this.vecCols.get(i).getValue(indRow) )
//                .toArray();
//        
//        return arrVals;
//    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getColumn(int)
     */
    @Override
    public final /*<T extends Object> IDataColumn<T> */ IDataColumn<Object>   getColumn(int indCol) throws IndexOutOfBoundsException {
        return this.vecCols.get(indCol);
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getColumn(java.lang.String)
     */
    @Override
    public final /*<T extends Object> IDataColumn<T> */ IDataColumn<Object>    getColumn(String strName) throws NoSuchElementException {
        IDataColumn<Object>  col = this.mapNmToCol.get(strName);
        
        if (col == null) 
            throw new NoSuchElementException("Table contains no column with name " + strName);
        
        return col;
    }

//    /**
//     *
//     * @see @see com.ospreydcs.dp.api.model.IDataTable#allocationSize()
//     */
//    @Override
//    public long allocationSize() throws UnsupportedOperationException, ArithmeticException {
//        
//        ByteArrayOutputStream   osByteArray = new ByteArrayOutputStream();
//        
//        try {
//            ObjectOutputStream      osByteCounter = new ObjectOutputStream(osByteArray);
//            
//            osByteCounter.writeObject(this);
//            osByteCounter.flush();
//            osByteCounter.close();
//            
//            return osByteArray.size();
//            
//        } catch (IOException e) {
//            throw new ArithmeticException("Unable to compute table size (e.g., too large)");
//            
//        }
//    }

}
