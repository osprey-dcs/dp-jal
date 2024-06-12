/*
 * Project: dp-api-common
 * File:	StaticDataColumn.java
 * Package: com.ospreydcs.dp.api.query.model
 * Type: 	StaticDataColumn
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;

import com.ospreydcs.dp.api.model.DpSupportedType;
import com.ospreydcs.dp.api.model.IDataColumn;

/**
 * <p>
 * A named column of data containing values all of the same type.
 * </p>
 * <p>
 * Implements the <code>{@link IDataColumn}</code> interface as a static data column that is fully populated
 * at construction.  All column data values are backed by an internal <code>{@link ArrayList}</code> to 
 * provide fast indexing.
 * </p>
 * <p>
 * Typically, a <code>StaticDataColumn</code> is a unit of homogeneous data within a larger composite of
 * time-correlated, heterogeneous data such as a <code>StaticDataTable</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 8, 2024
 *
 * @see IDataColumn
 */
public class StaticDataColumn<T extends Object> implements IDataColumn<T>, Serializable {
    
    
    //
    // Class Constants
    //
    
    /** <code>Serializable</code> interface serialization ID - used for allocation size estimation */
    private static final long serialVersionUID = -5285814908229917071L;

    
    //
    // Attributes
    //
    
    /** The name of the data column, typically the unique name of the data source producing the column data */
    private final String            strName;
    
    /** The data type of the column data */ 
    private final DpSupportedType   enmType;

    /** The column data itself, anonymously typed as Java <code>Object</code> */
    private final ArrayList<T>      vecValues;

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates a new, initialized instance of <code>StaticDataColumn</code>.
     * </p>
     * <p>
     * This is the preferred creator for <code>StaticDataColumn</code> objects.
     * Creates an initialized <code>StaticDataColumn</code> instance fully populated with the argument data.
     * An exception is thrown if the data values argument is empty or its type is inconsistent with the
     * generic parameter <code>T</code>.
     * </p>
     *
     * @param <T>           data type of the column data
     * 
     * @param strName       name of the data column
     * @param enmType       data type of all column data (must be compatible with generic parameter <code>T</code>)
     * @param vecValues     vector of data values for the data column
     *  
     * @return  a new data column populated with the given argument data with the specified type
     *
     * @throws MissingResourceException the initializing data values container was empty 
     * @throws IllegalArgumentException the specified data type is incompatible with generic parameter <code>T</code>
     * 
     * @see #StaticDataColumn(String, DpSupportedType, ArrayList)
     */
    public static <T extends Object> StaticDataColumn<T> from(String strName, DpSupportedType enmType, ArrayList<T> vecValues) 
            throws MissingResourceException, IllegalArgumentException {
        
        return new StaticDataColumn<T>(strName, enmType, vecValues);
    }
    
    /**
     * <p>
     * Creates a new, initialized instance of <code>StaticDataColumn</code>.
     * </p>
     * <p>
     * Creates an initialized <code>StaticDataColumn</code> instance fully populated with the argument data.
     * An exception is thrown if the data values argument is empty or its type is inconsistent with the
     * generic parameter <code>T</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The newly created table does NOT take ownership of the argument data.  It creates a new array list 
     * container to maintain the data.
     * </li>
     * <br/>
     * <li>
     * This creator is provided as a convenience. 
     * Use creator <code>{@link #from(String, DpSupportedType, ArrayList)}</code> whenever possible.
     * Note that the data value container is left generic.  All data values within the collection are transferred,
     * in whatever order, to a new <code>{@link ArrayList}</code> for faster indexing.
     * </li>
     * </ul>
     * </p>
     *
     * @param <T>           data type of the column data
     * 
     * @param strName       name of the data column
     * @param enmType       data type of all column data (must be compatible with generic parameter <code>T</code>)
     * @param setValues     collection of data values for the data column
     *  
     * @throws MissingResourceException the initializing data values container was empty 
     * @throws IllegalArgumentException the specified data type is incompatible with generic parameter <code>T</code>
     * 
     * @return  a new data column populated with the given argument data with the specified type
     *
     * @see #StaticDataColumn(String, DpSupportedType, Collection)
     */
    public static <T extends Object> StaticDataColumn<T> from(String strName, DpSupportedType enmType, Collection<T> setValues) 
            throws MissingResourceException, IllegalArgumentException {
        
        return new StaticDataColumn<T>(strName, enmType, setValues);
    }
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>StaticDataColumn</code>.
     * </p>
     * <p>
     * This is the preferred constructor for <code>StaticDataColumn</code> objects.
     * Constructs an initialized <code>StaticDataColumn</code> instance fully populated with the argument data.
     * An exception is thrown if the data values argument is empty or its type is inconsistent with the
     * generic parameter <code>T</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The constructed table takes ownership of the given data (i.e., the array list argument).  This is in
     * contrast to constructor <code>{@link #StaticDataColumn(String, DpSupportedType, Collection)}</code> which
     * does not.
     * </p>
     *
     * @param strName       name of the data column
     * @param enmType       data type of all column data (must be compatible with generic parameter <code>T</code>)
     * @param vecValues     vector of data values for the data column
     *  
     * @throws MissingResourceException the initializing data values container was empty 
     * @throws IllegalArgumentException the specified data type is incompatible with generic parameter <code>T</code>
     */
    public StaticDataColumn(String strName, DpSupportedType enmType, ArrayList<T> vecValues) 
            throws MissingResourceException, IllegalArgumentException {
        
        // Check the arguments
        if (vecValues.isEmpty())
            throw new MissingResourceException("The data value container was empty.", vecValues.getClass().getName(), "vecValues");
        
        T   val = vecValues.get(0);
        if (!enmType.isAssignableFrom(val))
            throw new IllegalArgumentException("Data value type unassignable to requested type " + enmType);
        
        // Assign attributes
        this.strName = strName;
        this.enmType = enmType;
        this.vecValues = vecValues;
    }

    /**
     * <p>
     * Constructs a new, initialized instance of <code>StaticDataColumn</code>.
     * </p>
     * <p>
     * Constructs an initialized <code>StaticDataColumn</code> instance fully populated with the argument data.
     * An exception is thrown if the data values argument is empty or its type is inconsistent with the
     * generic parameter <code>T</code>.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The constructed table does NOT take ownership of the argument data.  It creates a new array list container
     * to maintain the data.
     * </li>
     * <br/>
     * <li>
     * This constructor is provided as a convenience. 
     * Use constructor <code>{@link #StaticDataColumn(String, DpSupportedType, ArrayList)}</code> whenever possible.
     * Note that the data value container is left generic.  All data values within the collection are transferred,
     * in whatever order, to a new <code>{@link ArrayList}</code> for faster indexing.
     * </li>
     * </ul>
     * </p>
     *
     * @param strName       name of the data column
     * @param enmType       data type of all column data (must be compatible with generic parameter <code>T</code>)
     * @param vecValues     collection of data values for the data column
     *  
     * @throws MissingResourceException the initializing data values container was empty 
     * @throws IllegalArgumentException the specified data type is incompatible with generic parameter <code>T</code>
     */
    public StaticDataColumn(String strName, DpSupportedType enmType, Collection<T> vecValues) 
            throws MissingResourceException, IllegalArgumentException {
        
        // Check the argument
        if (vecValues.isEmpty())
            throw new MissingResourceException("The data value container was empty.", vecValues.getClass().getName(), "vecValues");
        
        // Assign attributes
        this.strName = strName;
        this.enmType = enmType;
        this.vecValues = new ArrayList<T>(vecValues);

        // Check the type
        T   val = this.vecValues.get(0);
        if (!enmType.isAssignableFrom(val))
            throw new IllegalArgumentException("Data value type unassignable to requested type " + enmType);
    }


    //
    // IDataColumn<T> Interface
    //
    
    /**
     * @see @see com.ospreydcs.dp.api.model.IDataColumn#getName()
     */
    @Override
    public final String getName() {
        return this.strName;
    }


    /**
     * @see @see com.ospreydcs.dp.api.model.IDataColumn#getType()
     */
    @Override
    public final DpSupportedType getType() {
        return this.enmType;
    }

    /**
     * @see @see com.ospreydcs.dp.api.model.IDataColumn#getValue(int)
     */
    @Override
    public Object getValue(int index) throws IndexOutOfBoundsException {
        return this.vecValues.get(index);
    }


    /**
     * @see @see com.ospreydcs.dp.api.model.IDataColumn#getSize()
     */
    @Override
    public Integer getSize() {
        return this.vecValues.size();
    }


    /**
     * @see @see com.ospreydcs.dp.api.model.IDataColumn#getValues()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Object> getValues() throws ArithmeticException {
        return (List<Object>) this.vecValues;
    }

    /**
     * <p>
     * Returns the number of bytes required to serialize this data column.
     * </p>
     * <p>
     * The data column is serialized into a byte array buffer.
     * The size of the buffer is returned after serialization.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * If the serialization size of the data column is to large to fit in the data buffer 
     * (larger than <code>{@link Integer#MAX_VALUE}</code>) then an exception is thrown.
     * </p>
     * 
     * @throws UnsupportedOperationException    not thrown
     * @throws ArithmeticException              the data column could not be serialized (e.g., too large)
     * 
     * @see com.ospreydcs.dp.api.model.IDataColumn#allocationSize()
     */
    @Override
    public long allocationSize() throws UnsupportedOperationException, ArithmeticException {
        ByteArrayOutputStream   osByteArray = new ByteArrayOutputStream();
        
        try {
            ObjectOutputStream      osByteCounter = new ObjectOutputStream(osByteArray);
            
            osByteCounter.writeObject(this);
            osByteCounter.flush();
            osByteCounter.close();
            
            return osByteArray.size();
            
        } catch (IOException e) {
            throw new ArithmeticException("Unable to compute table size (e.g., too large)");
            
        }
    }
    

    //
    // IDataColumn<T> Default Implementation Overrides
    //
    
    /**
     * Overrides Default Implementation
     *
     * @see com.ospreydcs.dp.api.model.IDataColumn#getValueTyped(int)
     */
    @Override
    public T getValueTyped(int index) throws IndexOutOfBoundsException, ArithmeticException, ClassCastException {
        return this.vecValues.get(index);
    }

    /**
     * Overrides Default Implementation
     * 
     * @see com.ospreydcs.dp.api.model.IDataColumn#getValuesTyped()
     */
    @Override
    public List<T> getValuesTyped() throws ArithmeticException {
        return this.vecValues;
    }
    
}
