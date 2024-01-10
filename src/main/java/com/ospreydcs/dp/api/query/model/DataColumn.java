/*
 * Project: dp-api-common
 * File:	DataColumn.java
 * Package: com.ospreydcs.dp.api.query.model
 * Type: 	DataColumn
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

import java.util.Vector;

import com.ospreydcs.dp.api.model.DpSupportedType;

/**
 * <p>
 * A named column of data containing values all of the same type.
 * </p>
 * <p>
 * Typically, a <code>DataColumn</code> is a unit of homogeneous data within a larger composite of
 * time-correlated, heterogeneous data such as a <code>DataTable</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 8, 2024
 *
 */
public final class DataColumn {
    
    
    //
    // Attributes
    //
    
    /** The name of the data column, typically the unique name of the data source producing the column data */
    private String                      strName;
    
    /** The data type of the column data */ 
    private DpSupportedType             enmType;

    
    //
    // Resources
    //
    
    /** The column data itself, anonymously typed as Java <code>Object</code> */
    private Vector<? extends Object>    vecValues;

    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>DataColumn</code>.
     * </p>
     *
     */
    public DataColumn() {
        // TODO Auto-generated constructor stub
    }


    //
    // Attribute Getters
    //
    
    /**
     * @return the name of the data column
     */
    public final String getName() {
        return this.strName;
    }


    /**
     * @return the data type of the column data values 
     */
    public final DpSupportedType getType() {
        return this.enmType;
    }

    
    //
    // Value Access Methods
    //
}
