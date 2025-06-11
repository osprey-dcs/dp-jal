/*
 * Project: dp-data-simulator
 * File:	DataColumnGenerator.java
 * Package: com.ospreydcs.dp.datasim.model.frame
 * Type: 	DataColumnGenerator
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
 * @since Aug 28, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.jal.tools.ingest.frame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.ospreydcs.dp.api.common.DpSupportedType;
import com.ospreydcs.dp.api.common.IDataColumn;
import com.ospreydcs.dp.api.model.table.StaticDataColumn;
import com.ospreydcs.dp.jal.tools.ingest.values.IDataValueGenerator;
import com.ospreydcs.dp.jal.tools.ingest.values.ScalarGenerator;

/**
 * <p>
 * Class for generating data columns of simulated data for an <code>IngestionFrame</code>.
 * </p>
 * <p>
 * Instances of this class are used primarily for column generation within <code>IngestionFrameGenerator</code>
 * instances.
 * </p>
 * <h2>NOTES:</h2>
 * Instances of this class require a <code>{@link SampleBlockConfig}</code> record class object for 
 * instantiation. 
 * </p>
 *
 *
 * @author Christopher K. Allen
 * @since Aug 28, 2024
 *
 */
public class DataColumnGenerator {

    
    //
    // Creator
    //
    
    /**
     * <p>
     * Creates a new <code>DataColumnGenerator</code> instance configured according to the parameters
     * in the given record class.
     * </p>
     * <p>
     * <h2>NOTES</h2>
     * If the data column configuration is inconsistent or corrupt an exception is thrown and no factory
     * is created.
     * </p>
     * 
     * @param cfgCols   the data column configuration record
     * 
     * @return  data column factory instance
     * 
     * @throws IllegalArgumentException inconsistent configuration record parameters (see detail message)
     */
    public static DataColumnGenerator from(SampleBlockConfig cfgCols) throws IllegalArgumentException {
        return new DataColumnGenerator(cfgCols);
    }

    
    //
    // Defining Attributes
    //
    
    /** The size of each column - number of data values */
    private final int                   szCols;
    
    /** The number of columns to generate */
    private final int                   cntCols;
    
    /** The Data Platform supported data type of the data column */
    private final DpSupportedType       enmDataType;
    
    /** The names of each column */
    private final Set<String>           setColNms; 
    
    
    //
    // Instance Resources
    //
    
    /** The value factory for generating column values */
    private final IDataValueGenerator   genValues;
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>DataColumnGenerator</code> ready for
     * data column creation.
     * </p>
     *
     * @param recCfg   the sample block configuration record
     * 
     * @throws IllegalArgumentException inconsistent configuration record parameters (see message)
     */
    public DataColumnGenerator(SampleBlockConfig recCfg) {
        this.szCols = recCfg.cntSamples();
        this.cntCols = recCfg.setPvNames().size();
        this.enmDataType = recCfg.enmDataType().getDpType();
        this.setColNms = recCfg.setPvNames();
        this.genValues = ScalarGenerator.from(recCfg.enmDataType());
    }

    
    //
    // Attribute Query
    //
    
    /**
     * <p>
     * Returns the number of data columns created in each <code>{@link #build()}</code> operation.
     * </p>
     * 
     * @return  columns generated per factory build
     */
    public final int getColumnCount() {
        return this.cntCols;
    }
    
    /**
     * <p>
     * Returns the size of each column generated, that is, the number of values in the column.
     * </p>
     * 
     * @return  number column values (or "rows")
     */
    public final int getColumnSize() {
        return this.szCols;
    }
    
    /**
     * <p>
     * The Data Platform type enumeration for the column values.
     * </p>
     *   
     * @return  the <code>DpSupported</code> type enumeration for column data values
     */
    public final DpSupportedType getType() {
        return this.enmDataType;
    }
    
    /**
     * <p>
     * Returns an immutable list of the column names for all factory generated column collections.
     * </p>
     * 
     * @return  list of column names for factory generated columns
     */
    public final Set<String> getColumnNames() {
        return this.setColNms;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Creates a new collection of data columns according to the configuration record supplied at
     * construction.
     * </p>
     * <p>
     * Returned object is an immutable array list (i.e., a "vector") created from Java streams.
     * </p>
     * 
     * @return  vector of data columns containing simulated data
     */
    public ArrayList<IDataColumn<Object>> build() {

        // Create (immutable) list of columns
        ArrayList<IDataColumn<Object>>   lstCols = this.setColNms
                .stream()
                .<IDataColumn<Object>>map(name -> this.buildColumn(name))
                .collect(
                        ArrayList::new, 
                        (vec, col) -> vec.add(col), 
                        (vec1, vec2) -> vec1.addAll(vec2)
                        );
        
        return lstCols;
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates a new data column with the given name and appropriate size.
     * </p>
     * <p>
     * The attribute <code>{@link #genValues}</code> is used to create the new values for
     * the data column.  The type parameter for the new column is given by <code>{@link #enmDataType}</code>
     * and the size of the column (i.e., number of rows) is given by <code>{@link #szCols}</code>.
     * </p>
     * <p>
     * Currently the concrete type of the returned interface is given by <code>{@link StaticDataColumn}</code>.
     * This class is provided in the Java API client library.
     * </p>
     * 
     * @param strName   data column name
     * 
     * @return  new, populated implementation of the <code>IDataColumn</code> interface.
     */
    private IDataColumn<Object> buildColumn(String strName) {
        
        // Create the value vector container and pack it with new values
        ArrayList<Object>   vecVals = new ArrayList<>(this.szCols);
        for (int i=0; i<this.szCols; i++) {
            Object objVal = this.genValues.nextValue();
            
            vecVals.add(objVal);
        }
        
        // Create the new column
        StaticDataColumn<Object>    col = StaticDataColumn.from(strName, this.enmDataType, vecVals);
        
        return col;
    }
    
}
