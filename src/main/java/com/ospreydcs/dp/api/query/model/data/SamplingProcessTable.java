/*
 * Project: dp-api-common
 * File:	SamplingProcessTable.java
 * Package: com.ospreydcs.dp.api.query.model.data
 * Type: 	SamplingProcessTable
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
 * @since Feb 23, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.query.model.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.concurrent.CompletionException;

import org.w3c.dom.ranges.RangeException;

import com.ospreydcs.dp.api.model.DpSupportedType;
import com.ospreydcs.dp.api.model.IDataColumn;
import com.ospreydcs.dp.api.model.IDataTable;
import com.ospreydcs.dp.api.query.model.grpc.CorrelatedQueryData;
import com.ospreydcs.dp.api.query.model.process.SamplingProcess;
import com.ospreydcs.dp.api.query.model.process.UniformSamplingBlock;

/**
 * <p>
 * Extends the <code>{@link SamplingProcess}</code> class to add <code>{@link IDataTable}</code> functionality.
 * </p>
 * <p>
 * This is essentially a wrapper class for <code>{@link SamplingProcess}</code> that adds the 
 * <code>{@link IDataTable}</code> operations for use as a data table. See the class documentation for those types 
 * for further information on their use and functionality.
 * </p>
 * <p>
 * This class is provided to reduce the duplication of memory requests, say for large data request results sets.
 * Almost all table accessing is done dynamically, and thus, is slower than a static table.  For fast table
 * access consider the creation of a static table with method <code>{@link #createStaticDataTable()}</code>.
 * It will shallow copy all superclass <code>SampleProcess</code> data into a static table.
 * </p> 
 * <p>
 * <h2>Paged Data Table</h2>
 * This class essentially functions as a paged data table.  The table "pages" are the 
 * <code>{@link UniformSamplingBlock}</code> instances forming the composite <code>{@link SamplingProcess}</code>.
 * (Note that class <code>{@link UniformSamplingBlock}</code> also implements the <code>{@link IDataTable}</code>
 * interface.)
 * Thus, any table row lookup must identify both the table page and the page row within the page.  Such the 
 * convenience record <code>{@link PageIndex}</code> is used internally for this action.
 * </p>  
 * <p>
 * <h2>Dynamic Accessing</h2>
 * No static table data is created for the <code>IDataTable</code> implementation.  All index lookups are done
 * dynamically and data is extracted from the component <code>{@link UniformSamplingBlock}</code> objects.
 * The only exceptions are the methods <code>{@link #getColumn(int)}</code> and <code>{@link #getColumn(String)}</code>
 * where the returned data column is constructed and returned.  Thus, use these methods sparingly.
 * </p>  
 * <h2>NOTES:</h2>
 * <ul>
 * <li>Due to the dynamic accessing (e.g., indexing), table access performance can be reduced.</li> 
 * <li>Use <code>{@link #createStaticDataTable()}</code> to create an equivalent static table for faster access.</li>
 * <li>Avoid use of <code>{@link #getColumn(int)}</code> and <code>{@link #getColumn(String)}</code>.</li>
 * </ul>
 * </p>
 * 
 * @author Christopher K. Allen
 * @since Feb 23, 2024
 *
 */
public class SamplingProcessTable extends SamplingProcess implements IDataTable {

    
    //
    // Class Types
    //
    
    /**
     *  Record containing the (data page index, page row index) (i.e., as computed from a table row index).
     */
    private record PageIndex(int indPage, int indPageRow) {};

    
    //
    // Attributes
    //

    /** The vector of timestamps for this sampling process - set at construction */
    private final ArrayList<Instant>    vecTimestamps;
    
    /** The vector of data source names - set at construction */
    private final ArrayList<String>     vecColumnName;

    /** The map of data source name to table column index - set at construction */
    private final Map<String, Integer>  mapSrcNmToInd;
    
    /** The map of table column index to source name - set at construction */
    private final Map<Integer, String>  mapIndToSrcNm;

    
    /** The vector of row indices for each page (sampling block) - set at construction */
    private final ArrayList<Integer>    vecPageRowInd;
    
    /** Storage of (created) full table columns - we remember table columns if we create them */
    private final Map<String, IDataColumn<Object>>  mapSrcNmToFullColumn;
    
    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new instance of <code>SamplingProcessTable</code> fully populated from argument data.
     * </p>
     * <p>
     * After creation the new instance is fully populated with sampled time-series data from
     * all data sources contained in the argument.  The new instance is also configured according to the order 
     * and the correlations within the argument.
     * The argument data must be consistent for proper time-series data construction or an exception is thrown.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>The argument collection is assumed to be sorted in the order of clock starting instants</li>
     * <li>Extensive data consistency check is performed during construction (within <code>SamplingProcess</code>).</li>
     * <li>Any detected data inconsistencies result in an exception. </li>
     * <li>Exception type determines the nature of any Data inconsistency. </li>
     * </ul>  
     *
     * @param setTargetData sorted set of <code>CorrelatedQueryData</code> used to build this table
     * 
     * @return a new instance of <code>SamplingProcessTable</code> fully populated and ready for table operations
     *  
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws RangeException           the argument contains time domain collisions
     * @throws UnsupportedOperationException an unsupported data type was detected within the argument
     * @throws CompletionException      the sampling process was corrupt after creation (see message)
     * 
     * @see SamplingProcess
     */
    public static SamplingProcessTable from(SortedSet<CorrelatedQueryData> setTargetData) 
            throws  MissingResourceException, 
                    IllegalArgumentException, 
                    IllegalStateException, 
                    RangeException, 
                    UnsupportedOperationException, 
                    CompletionException 
    {
        return new SamplingProcessTable(setTargetData);
    }
    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>SamplingProcessTable</code>.
     * </p>
     * <p>
     * The argument is passed to the superclass constructor <code>{@link SamplingProcess(SortedSet)}</code> where
     * all the time-series data initialization occurs.  This constructor then initializes the auxiliary 
     * containers used for table access.
     * </p>
     *
     * @param setTargetData sorted set of <code>CorrelatedQueryData</code> used to build this table
     * 
     * @throws MissingResourceException the argument is has empty data column(s)
     * @throws IllegalArgumentException the argument is has non-unique data sources, or unequal column sizes (see message)
     * @throws IllegalStateException    the argument contains duplicate data source names
     * @throws RangeException           the argument contains time domain collisions
     * @throws UnsupportedOperationException an unsupported data type was detected within the argument
     * @throws CompletionException      the sampling process was corrupt after creation (see message)
     */
    public SamplingProcessTable(SortedSet<CorrelatedQueryData> setTargetData)
            throws RangeException, MissingResourceException, IllegalArgumentException, IllegalStateException,
            UnsupportedOperationException, CompletionException {
        super(setTargetData);

        this.vecTimestamps = super.timestamps();
        this.vecColumnName = new ArrayList<>(super.setSrcNms);
        this.mapSrcNmToInd = this.createSrcNmToIndMap(super.setSrcNms);
        this.mapIndToSrcNm = this.createIndToSrcNmMap(super.setSrcNms);
        this.vecPageRowInd = this.createPageIndexVector(super.vecSmplBlocks);
        
        this.mapSrcNmToFullColumn = new HashMap<>();
    }
    
//    public SamplingProcessTable(SamplingProcess source) {
//        super(source);
//    }

    
    //
    // IDataTable Interface
    //

    /**
     * @return always returns <code>true</code>, table is complete at construction
     * 
     * @see @see com.ospreydcs.dp.api.model.IDataTable#isTableComplete()
     */
    @Override
    public boolean isTableComplete() {
        return true;
    }

    /**
     * @return always returns <code>false</code>, table is complete at construction
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#hasError()
     */
    @Override
    public boolean hasError() {
        return false;
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getRowCount()
     */
    @Override
    public Integer getRowCount() {
        return super.getSampleCount();
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getColumnCount()
     */
    @Override
    public Integer getColumnCount() {
        return super.getDataSourceCount();
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getColumnIndex(java.lang.String)
     */
    @Override
    public int getColumnIndex(String strName) throws NoSuchElementException {
        
        Integer indCol = this.mapSrcNmToInd.get(strName);
        if (indCol == null)
            throw new NoSuchElementException("Table does NOT contain data source " + strName);
        
        return indCol;
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getColumnNames()
     */
    @Override
    public final List<String> getColumnNames() {
        return this.vecColumnName;
    }

    /**
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getTimestamps()
     */
    @Override
    public final List<Instant> getTimestamps() {
        return this.vecTimestamps;
    }

    /**
     * <p>
     * Dynamically creates the entire data column for all data pages.
     * </p>
     * <p>
     * Creates the full time-series data column and stores the result in a map for future reference.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Avoid using this method if possible - creates additional resources.</li>
     * <li>If all data columns are to be accessed consider creating a static table with {@link #createStaticDataTable()}</li>
     * <li>See <code>{@link IDataTable#getColumn(int)</code> for additional documentation.</li>
     * </ul>
     * </p>
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumn(int)
     */
    @Override
    public IDataColumn<Object> getColumn(int indCol) throws IndexOutOfBoundsException, ClassCastException {

        // Check index
        if (indCol < 0 || indCol >= super.getDataSourceCount())
            throw new IndexOutOfBoundsException("Table column index " + indCol + " out of bounds [0, " + super.getDataSourceCount() + "]");
        
        // Get column name
        String  strColNm = this.getColumnName(indCol);
        
        // Is already created?
        IDataColumn<Object>     col = this.mapSrcNmToFullColumn.get(strColNm);
        
        // Must create the column (and save)
        if (col == null) {
            
            col = super.timeSeries(strColNm);
            this.mapSrcNmToFullColumn.put(strColNm, col);
        }
        
        return col;
    }

    /**
     * <p>
     * Dynamically creates the entire data column for all data pages.
     * </p>
     * <p>
     * Creates the full time-series data column and stores the result in a map for future reference.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Avoid using this method if possible - creates additional resources.</li>
     * <li>If all data columns are to be accessed consider creating a static table with {@link #createStaticDataTable()}</li>
     * <li>See <code>{@link IDataTable#getColumn(String)</code> for additional documentation.</li>
     * </ul>
     * </p>
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumn(java.lang.String)
     */
    @Override
    public IDataColumn<Object> getColumn(String strName) throws NoSuchElementException, ClassCastException {
        
        // Check the column name
        if (!super.hasSourceData(strName))
            throw new NoSuchElementException("Table has no data source with name " + strName);
        
        // Is already created?
        IDataColumn<Object>     col = this.mapSrcNmToFullColumn.get(strName);
        
        // Must create the column (and save)
        if (col == null) {
            
            col = super.timeSeries(strName);
            this.mapSrcNmToFullColumn.put(strName, col);
        }
        
        return col;
    }

    
    //
    // IDataTable Interface Default Overrides
    //

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumnName(int)
     */
    @Override
    public String getColumnName(int indCol) throws IndexOutOfBoundsException {

        // Check argument value
        if (indCol < 0 || indCol >= this.getColumnCount())
            throw new IndexOutOfBoundsException("Column index " + indCol + " not in [0, " + this.getColumnCount() + "]");

        return this.mapIndToSrcNm.get(indCol);
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumnType(int)
     */
    @Override
    public DpSupportedType getColumnType(int indCol) throws IndexOutOfBoundsException {
        String   strName = this.getColumnName(indCol);

        return super.getSourceType(strName);
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumnType(java.lang.String)
     */
    @Override
    public DpSupportedType getColumnType(String strName) throws NoSuchElementException {
        return super.getSourceType(strName);
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumnSize(int)
     */
    @Override
    public Integer getColumnSize(int indCol) throws IndexOutOfBoundsException {

        // Check argument value
        if (indCol < 0 || indCol >= this.getColumnCount())
            throw new IndexOutOfBoundsException("Column index " + indCol + " not in [0, " + this.getColumnCount() + "]");

        return super.getSampleCount();
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumnSize(java.lang.String)
     */
    @Override
    public Integer getColumnSize(String strName) throws NoSuchElementException {

        // Check argument value
        if (!super.hasSourceData(strName))
            throw new NoSuchElementException("Column name no represented in time-series data: " + strName);

        return super.getSampleCount();
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumnSizeMin()
     */
    @Override
    public Integer getColumnSizeMin() {
        return super.getSampleCount();
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumnSizeMax()
     */
    @Override
    public Integer getColumnSizeMax() {
        return super.getSampleCount();
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getTimestamp(int)
     */
    @Override
    public Instant getTimestamp(int indRow) throws IndexOutOfBoundsException {
        return this.vecTimestamps.get(indRow);
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     * 
     * @see com.ospreydcs.dp.api.model.IDataTable#getValue(int, int)
     */
    @Override
    public Object getValue(int indRow, int indCol) throws IndexOutOfBoundsException, ArithmeticException {

        // Get the sampling block indices
        PageIndex  recIndex = this.computePageIndex(indRow);

        // Get the page
        UniformSamplingBlock    tblPage = super.getSamplingBlock(recIndex.indPage);

        // Get source name for index and check that page contains data for that source
        String      strColNm = this.getColumnName(indCol);
        if (!tblPage.hasSourceData(strColNm))
            return null;

        // Return value
        Object      objVal = tblPage.getValue(recIndex.indPageRow, strColNm);

        return objVal;
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     * 
     * @see com.ospreydcs.dp.api.model.IDataTable#getValue(int, String)
     */
    @Override
    public Object getValue(int indRow, String strName) 
            throws IndexOutOfBoundsException, NoSuchElementException, ArithmeticException {

        // Get the sampling block indices
        PageIndex  recIndex = this.computePageIndex(indRow);

        // Get the page
        UniformSamplingBlock    tblPage = super.getSamplingBlock(recIndex.indPage);

        // Check that page contains data for that source
        if (!tblPage.hasSourceData(strName))
            return null;

        // Return value
        Object      objVal = tblPage.getValue(recIndex.indPageRow, strName);

        return objVal;
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getRowValues(int)
     */
    @Override
    public Object[] getRowValues(int indRow) throws IndexOutOfBoundsException {

        // Get the sampling block indices
        PageIndex  recIndex = this.computePageIndex(indRow);

        // Get the page
        UniformSamplingBlock    tblPage = super.getSamplingBlock(recIndex.indPage);

        // Allocate the object array and populate it
        Object[]    arrObjs = new Object[this.getColumnCount()];

        int indCol = 0;
        for (String strName : this.vecColumnName) { // thru ordered vector of column names
            Object  objVal;

            if (!tblPage.hasSourceData(strName))
                objVal = null;
            else
                objVal = tblPage.getValue(recIndex.indPageRow, strName);

            arrObjs[indCol] = objVal;
            indCol++;
        }

        return arrObjs;
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getRowValuesAsList(int)
     */
    @Override
    public List<Object> getRowValuesAsList(int indRow) throws IndexOutOfBoundsException {
        return List.of( this.getRowValues(indRow) );
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumnData(int)
     */
    @Override
    public List<Object> getColumnData(int indCol) throws IndexOutOfBoundsException {
        
        // Check argument
        String  strName = this.getColumnName(indCol);
        
        // Iterate through all data pages collecting data
        List<Object>    lstColVals = new ArrayList<>(super.getSampleCount());
        
        for (UniformSamplingBlock page : super.vecSmplBlocks) {
            
            if (page.hasSourceData(strName))
                lstColVals.addAll( page.getColumnData(strName) );
            
            else 
                for (int i=0; i<page.getSampleCount(); ++i)
                    lstColVals.add(null);
            
        }
        
        return lstColVals;
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see @see com.ospreydcs.dp.api.model.IDataTable#getColumnData(java.lang.String)
     */
    @Override
    public List<Object> getColumnData(String strName) throws IllegalArgumentException, NoSuchElementException {
        
        // Check argument
        if (!this.hasSourceData(strName))
            throw new NoSuchElementException("Data source name not represented within time-series data: " + strName);
        
        // Iterate through all data pages collecting data
        List<Object>    lstColVals = new ArrayList<>(super.getSampleCount());
        
        for (UniformSamplingBlock page : super.vecSmplBlocks) {
            
            if (page.hasSourceData(strName))
                lstColVals.addAll( page.getColumnData(strName) );
            
            else 
                for (int i=0; i<page.getSampleCount(); ++i)
                    lstColVals.add(null);
            
        }
        
        return lstColVals;
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumnDataTyped(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getColumnDataTyped(int indCol) throws IndexOutOfBoundsException, ClassCastException {
        
        // Get the value list as objects
        List<Object>    lstObjs = this.getColumnData(indCol);
        
        // Check the type
        if (lstObjs.isEmpty())
            return List.of();
        
        // Check the type
        Object          objVal = lstObjs.get(0);
        DpSupportedType enmType = this.getColumnType(indCol);
        
        if ( !enmType.isAssignableFrom(objVal.getClass()) )
             throw new ClassCastException("Generic parameter T is incompatible with column type " + enmType);
        
        // Safe to cast
        return (List<T>)lstObjs;
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#getColumnDataTyped(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getColumnDataTyped(String strName)
            throws IllegalArgumentException, NoSuchElementException, ClassCastException {

        // Get the value list as objects
        List<Object>    lstObjs = this.getColumnData(strName);
        
        // Check the type
        if (lstObjs.isEmpty())
            return List.of();
        
        // Check the type
        Object          objVal = lstObjs.get(0);
        DpSupportedType enmType = this.getColumnType(strName);
        
        if ( !enmType.isAssignableFrom(objVal.getClass()) )
             throw new ClassCastException("Generic parameter T is incompatible with column type " + enmType);
        
        // Safe to cast
        return (List<T>)lstObjs;
    }

    /**
     * <code>SamplingProcessTable</code> override of default <code>{@link IDataTable}</code> implementation.
     *
     * @see com.ospreydcs.dp.api.model.IDataTable#allocationSize()
     */
    @Override
    public long allocationSize() throws UnsupportedOperationException, ArithmeticException {
        
        // Iterate through all data pages collecting running sum
        long    lngSize = 0;
        
        for (UniformSamplingBlock page : super.vecSmplBlocks) {
            lngSize += page.allocationSize();
        }
        
        return lngSize;
    }


    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates and returns data source name to column index map - (data source name, table column index) pairs.
     * </p>
     * <p>
     * Given the ordered collection of data source names for the entire table, creates a map of 
     * data source name to table column index with all time-series data it contains.  The indices
     * appear in the order of the collection.
     * </p>
     * 
     * @param setSrcNms     ordered collection of unique data source name for the table
     * 
     * @return  a new map of data source names to table column indexes
     */
    private Map<String, Integer>    createSrcNmToIndMap(Collection<String> setSrcNms) {
        
        // Create indices in order of source name appearance
        Map<String, Integer>    map = new HashMap<>();
        Integer                 indCurr = 0;
        
        for (String strName : setSrcNms) {
            map.put(strName, indCurr);
            
            indCurr++;
        }
        
        return map;
    }
    
    /**
     * <p>
     * Creates and return the column index to data source name map - (table column index, data source name) pairs.
     * </p>
     * <p>
     * Given the ordered collection of data source names for the entire table, creates a map of 
     * table column index to the data source time-series data it contains.  The indices
     * appear in the order of the collection.
     * </p>
     * 
     * @param setSrcNms     ordered collection of unique data source name for the table
     *  
     * @return  a new map of table collection index to data source name
     */
    private Map<Integer, String>    createIndToSrcNmMap(Collection<String> setSrcNms) {
        
        // Create indices in order of source name appearance
        Map<Integer, String>    map = new HashMap<>();
        Integer                 indCurr = 0;
        
        for (String strName : setSrcNms) {
            map.put(indCurr, strName);
            
            indCurr++;
        }
        
        return map;
    }
    
    /**
     * <p>
     * Creates, computes, and returns a vector containing the table row index of each data page within 
     * the overall table.
     * </p>
     * <p>
     * Iterates through the argument of data pages (sampling blocks) to determine the the sizes (i.e., the number
     * of page rows returned by <code>{@link UniformSamplingBlock#getSampleCount()}</code> ).  The starting row
     * index of each data page is computed as the running sum of the page sizes.
     * </p>
     * <p>
     * The returned vector of indices has the form
     * <pre>
     *   [<i>i</i><sub>page<sub>0</sub></sub>, <i>i</i><sub>page<sub>1</sub></sub>, ..., <i>i</i><sub>page<sub><i>N</i>-1</sub></sub>]
     * </pre>
     * where <i>N</i> is the total number of data pages.  
     * Note that due to Java 0-based indexing <i>i</i><sub>page<sub>0</sub></sub> = 0.
     * </p>  
     *    
     * @param lstBlocks the ordered list of data pages for the table
     * 
     * @return  ordered vector of data page indices 
     */
    private ArrayList<Integer>      createPageIndexVector(List<UniformSamplingBlock> lstBlocks) {
        
        // Increase table row index in order of sampling block occurrence
        ArrayList<Integer>  vecInds = new ArrayList<>(lstBlocks.size());
        Integer             indTblRow = 0;
        
        for (UniformSamplingBlock blk : lstBlocks) {
            vecInds.add(indTblRow);
            
            indTblRow += blk.getSampleCount();
        }
        
        return vecInds;
    }
    
    /**
     * <p>
     * Computes the table data page indices for the given table row index.
     * </p>
     * <p>
     * Table data pages consist of <code>UniformSamplingBlock</code>, which themselves implement the 
     * <code>{@link IDataTable}</code> interface.  Thus, any (outer table) row index must identify both
     * the data page and the row index within the data page.
     * </p>
     * <p>
     * From the table row index argument, the pair of indices identifying the table page and and the page
     * row is returned.  The method iterates through each page index within the attribute 
     * <code>{@link #vecPageRowInd}</code> until it finds the smallest index 
     * <i>i</i><sub>page<sub><i>n</i></sub></sub>
     * such that
     * <pre>
     *      <i>i</i><sub>row</sub> < <i>i</i><sub>page<sub><i>n</i></sub>  
     * </pre>
     * where <i>i</i><sub>row</sub> is the argument and, by deduction, 
     * <i>i</i><sub>page<sub><i>n</i>-1</sub></sub> is the index of the page containing the argument 
     * (i.e., page<sub><i>n</i>-1</sub>).  The returned value is then the pair
     * <pre>
     *     (<i>n</i> - 1, <i>i</i><sub>row</sub> - <i>i</i><sub>page<sub><i>n</i>-1</sub></sub>) 
     * </pre>
     * which populates the <code>{@link PageIndex}</code> object.
     * </p>
     * <h2>NOTES:</h2>
     * This method requires the pre-computation of attribute <code>{@link #vecPageRowInd}</code> which contains
     * the table row index of each sampling block within the process.
     * </p>
     * 
     * @param indTblRow     row index for the full table
     * 
     * @return  data page index and row index within that data page  
     * 
     * @throws IndexOutOfBoundsException    row index out of bounds (0 &le; index < <code>{@link #getRowCount()}</code>)
     * @throws IllegalStateException        serious algorithm error, table page indexing vector likely corrupt
     */
    private PageIndex     computePageIndex(int indTblRow) throws IndexOutOfBoundsException, IllegalStateException {
        
        if (indTblRow < 0 || indTblRow >= super.getSampleCount())
            throw new IndexOutOfBoundsException("Table row index " + indTblRow + " out of bounds [0, " + super.getSampleCount() + "]");
        
        // Iterate through all data page indices
        Integer indPage = 0;
        Integer indPageRow = 0;
        
        Integer indPrev = null;
        for (Integer indCurr : SamplingProcessTable.this.vecPageRowInd) {
            
            // Loop initialization 
            // - don't start comparison until second page
            // - retain the value of previous index
            if (indPrev == null) {
                indPrev = indCurr;
                
                continue;
            }
            
            // If table index is less than current index it belongs to previous data table
            if (indTblRow < indCurr) {
                indPageRow = indTblRow - indPrev;
                
                return new PageIndex(indPage, indPageRow);
            }

            indPrev = indCurr;
            indPage++;
        }
        
        // Check last page
        if (indPage !=  (vecPageRowInd.size() - 1) )
            throw new IllegalStateException("Table row index inconsistent with page index list " + this.vecPageRowInd);

        // Process the last page
        indPageRow = indTblRow - indPrev;
        
        return new PageIndex(indPage, indPageRow); 
    }
    
//    private boolean checkColumnIndex(int indCol) throws IndexOutOfBoundsException {
//        
//        if (indCol < 0 || indCol >= super.getDataSourceCount())
//            throw new IndexOutOfBoundsException("Table column index " + indCol + " out of bounds [0, " + super.getDataSourceCount() + "]");
//        
//        return true;
//    }
}
