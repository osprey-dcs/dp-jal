/*
 * Project: dp-jal
 * File:	IFrameColumnsFactory.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen
 * Type: 	IFrameColumnsFactory
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
 * @since Nov 28, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen;

import java.util.ArrayList;
import java.util.Set;

import com.ospreydcs.dp.jal.ingest.IngestionFrame;

import com.ospreydcs.dp.jal.common.DpSupportedType;
import com.ospreydcs.dp.jal.common.IDataColumn;

/**
 * <p>
 * Required operations for all data column factories of simulated, heterogeneous data.
 * </p> 
 * <p>
 * <h2>Data Column Factories</h2>
 * Implementation classes produce multiple columns of simulated data.  The data columns should all have the
 * same data type (column type) and each created collection should have the same number of columns.
 * Generated data columns are of type <code>{@link IDataColumn}</code>, specifically, implementation classes
 * of <code>IDataColumn&lt;Object&gt;</code>.
 * </p>
 * <p>
 * The <code>{@link #build(int)}</code> operation creates a new set of data columns all with size given by the
 * argument.  Each invocation of <code>{@link #build(int)}</code> should produce a new column set typically
 * with different simulated data.
 * </p>
 * <p>
 * <h2>Ingestion Frame Factories</h2>
 * Implementations of this interface are assumed to be utilized in ingestion frame factories.  Ingestion frame
 * factories produce ingestion frame instances <code>{@link IngestionFrame}</code> containing simulated data
 * for evaluation of the Data Platform Ingestion Service operations.
 * </p>  
 *  
 *
 * @author Christopher K. Allen
 * @since Nov 28, 2025
 *
 */
public interface IFrameColumnsFactory<T extends Object> {

    
    //
    // Configuration Query
    //
    
    /**
     * <p>
     * Returns the number of data columns created in each <code>{@link #build()}</code> operation.
     * </p>
     * 
     * @return  columns generated per factory build
     */
    default public int getColumnCount() {
        return this.getColumnNames().size();
    }
    
    /**
     * <p>
     * Returns an immutable set of unique column names for all factory generated column collections.
     * </p>
     * 
     * @return  set of column names for factory generated columns
     */
    public Set<String> getColumnNames(); 
    
//    /**
//     * <p>
//     * Returns the size of each column generated, that is, the number of values in the column.
//     * </p>
//     * 
//     * @return  number column values (or "rows")
//     */
//    public int getColumnSize();
    
    /**
     * <p>
     * The Data Platform type enumeration for the column values.
     * </p>
     *   
     * @return  the <code>DpSupported</code> type enumeration for column data values
     */
    public DpSupportedType getColumnType();
    

    //
    // Operations
    //
    
    /**
     * <p>
     * Creates a new collection of data columns according to the configuration record supplied at
     * construction.
     * </p>
     * <p>
     * Returned object is an immutable array list (i.e., a "vector").
     * The columns are ordered according to the ordering of the column name <code>Set</code>
     * returned by <code>{@link #getColumnNames()}</code> .  
     * </p>
     * 
     * @param   szCol   the size of the returned data columns (i.e., the number of rows)
     * 
     * @return  vector of data columns containing simulated data
     */
    public ArrayList<IDataColumn<T>> build(int szCol);

}
