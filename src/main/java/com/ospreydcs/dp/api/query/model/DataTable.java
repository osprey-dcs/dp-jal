/*
 * Project: dp-api-common
 * File:	DataTable.java
 * Package: com.ospreydcs.dp.api.query.model
 * Type: 	DataTable
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
package com.ospreydcs.dp.api.query.model;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ospreydcs.dp.grpc.v1.query.QueryResponse;

/**
 * <p>
 * Defines a table of heterogeneous, time-correlated, time-series data.
 * </p>
 * <p>
 * <h2>DEFINITION</h2>
 * The table is composed of an ordered vector of time instants (.i.e., timestamps) and a collection
 * of named data columns.  The general structure of a data table is as follows:
 * <br/><br/>
 * <pre>
 *    PV<sub>1</sub>  PV<sub>2</sub>  ...  PV<sub><i>N</i></sub>
 * <i>t</i><sub>1</sub> <i>V</i><sub>1,1</sub> <i>V</i><sub>1,2</sub>  ...  <i>V</i><sub>1,<i>N</i></sub>     
 * <i>t</i><sub>2</sub> <i>V</i><sub>2,1</sub> <i>V</i><sub>2,2</sub>  ...  <i>V</i><sub>2,<i>N</i></sub>
 *          .
 *          .
 *          .
 * <i>t</i><sub><i>M</i></sub> <i>V</i><sub><i>M</i>,1</sub> <i>V</i><sub><i>M</i>,2</sub>  ...  <i>V</i><sub><i>M</i>,<i>N</i></sub>     
 * </pre>
 * where <i>M</i> is the number of table rows, <i>N</i> is the number of table data columns, <i>t<sub>m,n</sub></i>
 * is the timestamp for table row <i>m</i>, PV<sub><i>n</i></sub> is the column name for table column <i>n</i>, and
 * <i>V<sub>m,n</sub></i> is the data value at table row <i>m</i> in table column <i>n</i>.
 * <p>
 * <p>
 * Note that table columns can be referenced by both the column name PV<sub><i>n</i></sub> and table column index
 * <i>n</i>.  Within the table, columns are ordered arbitrarily, however, once the table is initialized column
 * indices do not change. 
 * Column names are always unique.
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
 * The timestamp of each data value <i>V<sub>m,n</sub></i> depends upon its row position <i>m</i>
 * within its data column PV<sub><i>n</i></sub>.  
 * The timestamp <i>t<sub>m</sub></i> refers to all data values {<i>V</i><sub>&mu;,<i>n</i></sub> | &mu;=<i>m</i>}
 * within data columns {PV<sub><i>n</i></sub>}.
 * Thus, the enclosed data are all "time-correlated".
 * </li>
 * </ul>
 * 
 *
 * @author Christopher K. Allen
 * @since Jan 8, 2024
 *
 */
public class DataTable {
    
    //
    // Resources
    //
    
    /** The ordered vector of timestamps corresponding to each data column (as a data time series) */
    private final Vector<Instant>       vecTms;
    
    /** The ordered vector of table data columns */
    private final Vector<DataColumn>    vecCols;
    
    
    /** Map of column names to table columns */
    private final Map<String, DataColumn>   mapNmToCol;
    

    //
    // Creators
    //
    
//    public static from(QueryResponse.QueryReport.QueryData msgData) {
//        msgData.
//    }
    
    //
    // Constructors
    //

    /**
     * <p>
     * Constructs a new, uninitialized instance of <code>DataTable</code>.
     * </p>
     */
    public DataTable() {
        this.vecTms = new Vector<>();
        this.vecCols = new Vector<>();
        this.mapNmToCol = new HashMap<>();
    }
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>DataTable</code>.
     * </p>
     *
     * @param vecTms    ordered vector of timestamps for each column
     * @param setCols   (unordered) set of columns
     */
    public DataTable(Vector<Instant> vecTms, Collection<DataColumn> setCols) {
        this.vecTms = vecTms;
        this.vecCols = new Vector<>(setCols);
        this.mapNmToCol = this.vecCols.stream().collect(Collectors.toMap(c -> c.getName(), c -> c));
    }


    //
    // Table Data Query
    //
    
    /**
     * Return the ordered vector of timestamps
     * 
     * @return  table timestamps vector
     */
    public final Vector<Instant>    getTimestamps() {
        return this.vecTms;
    }
    
    /**
     * <p>
     * Returns the data column by table column index.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Table column indices are arbitrarily determined by the table itself.
     * </p>
     * 
     * @param indCol    table column index
     * 
     * @return  data column at given table column index
     * 
     * @throws ArrayIndexOutOfBoundsException   the index < 0 or greater than the number of tables columns
     */
    public final DataColumn getColumn(int indCol) throws ArrayIndexOutOfBoundsException {
        return this.vecCols.elementAt(indCol);
    }
    
    /**
     * <p>
     * Return the data column by column name.
     * </p>
     * <h2>NOTES:</h2>
     * Table column names are a unique property of the data column.
     * </p>
     * 
     * @param strColName    data column name
     * 
     * @return  data column with the given name
     * 
     * @throws NoSuchElementException   table contains no data columns with given name
     */
    public final DataColumn getColumn(String strColName) throws NoSuchElementException {
        DataColumn  col = this.mapNmToCol.get(strColName);
        
        if (col == null) 
            throw new NoSuchElementException("Table contains no column with name " + strColName);
        
        return col;
    }
    
    /**
     * <p>
     * Returns the table column index for the given data column name.
     * </p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Table column names are a unique property of the data column.</li>
     * <li>Table column indices are arbitrarily determined by the table itself.</li>
     * </p>
     * 
     * @param strColName    data column name
     * 
     * @return  the table column index for the given data column name
     * 
     * @throws NoSuchElementException   table contains no data columns with given name
     */
    public int  getColumnIndex(String strColName) throws NoSuchElementException {
        
        OptionalInt index = IntStream.range(0, this.vecCols.size()).filter(i -> this.vecCols.get(i).getName().equals(strColName)).findAny();
        
        if (index.isEmpty())
            throw new NoSuchElementException("Table contains no column with name " + strColName);
        
        return index.getAsInt();
    }
}
