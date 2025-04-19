/*
 * Project: dp-api-common
 * File:	SampledTimeSeries.java
 * Package: com.ospreydcs.dp.api.query.model.table
 * Type: 	SampledTimeSeries
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
 * @since Jan 30, 2024
 *
 * TODO:
 * - See documentation
 */
package com.ospreydcs.dp.api.query.model.coalesce;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import com.ospreydcs.dp.api.common.DpSupportedType;
import com.ospreydcs.dp.api.common.IDataColumn;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;

/**
 * <p>
 * Represents time series of sampled values from a time process of given type and name.
 * </p>
 * <p>
 * Class contains the sample values, or time series, from a sampled process.  
 * <ul>
 * <li>The values are ordered (assumed with increasing time).</li>  
 * <li>The sample times for the process are determined externally.</li>
 * <li>All samples must be of the same type (specified at construction).</li>
 * </ul>
 * </p>
 * <p>
 * <h2>TODO</h2>
 * <ul>
 * <li><s>Depending upon final usage, the container type for values might be changed to <code>Vector</code></s>.</li>
 * <li><s>Add an interface implementation such as <code>IDataColumn</code></s>.</li>
 * </ul>
 * 
 * @param   <T>     data type of the sampled value
 *
 * @author Christopher K. Allen
 * @since Jan 30, 2024
 *
 */
public class SampledTimeSeries<T extends Object> implements IDataColumn<T>, Serializable {
    
    
    //
    // Creators
    //
    
    /**
     * <p>
     * Create a new <code>SampledTimeSeries</code> instance filled with <code>null</code> values.
     * </p>
     * <p>
     * Creates the <code>null</code> time series with length given by argument <code>{@link cntSize}</code>.
     * Used to represent time series were sampling was interrupted or otherwise unavailable.
     * </p>
     *  
     * @param strSourceName name of the data source producing <code>null</code> values
     * @param enmType       assumed type of the <code>null</code> values
     * @param cntSize       size of the time series (i.e., number of <code>null</code> data values)
     * 
     * @return  a new <code>SampledTimeSeries</code> instance containing <code>null</code> data values
     */
    public static <T extends Object> SampledTimeSeries<T> nullSeries(String strSourceName, DpSupportedType enmType, int cntSize) {
        SampledTimeSeries<T>   stms = new SampledTimeSeries<T>(strSourceName, enmType, cntSize);
        
        return stms;
    }
    
    /**
     * <p>
     * Creates a new, initialized <code>SampledTimeSeries</code> instance populated with the given argument.
     * </p>
     * <p>
     * Creates a new <code>SampledTimeSeries</code> instance with the data extracted from the given
     * Protobuf message.  The new instance can be modified post-creation to add more time-series data.
     * </p>
     * 
     * @param <T>   the Java data type of the time series values
     *  
     * @param msgDataCol    Protobuf message containing initialization data
     * 
     * @return  new <code>SampledTimeSeries</code> instance populated with argument data
     * 
     * @throws MissingResourceException the argument contained no data (empty message)
     * @throws IllegalStateException    the argument contained non-uniform data types
     * @throws TypeNotPresentException  the argument contained an unsupported data type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> SampledTimeSeries<T>  from(com.ospreydcs.dp.grpc.v1.common.DataColumn msgDataCol) 
            throws MissingResourceException, IllegalStateException, TypeNotPresentException {
        
        // Extract message data
        String                  strName = msgDataCol.getName();
        DpSupportedType         enmType = ProtoMsg.extractType(msgDataCol);
        Class<? extends Object> clsType = enmType.getJavaType();
        List<T>                 lstVals = (List<T>) ProtoMsg.extractValuesAs(clsType, msgDataCol);

        // Create initialized time series
        SampledTimeSeries<T>   stms = new SampledTimeSeries<T>(strName, enmType, lstVals);

        return stms;
    }
    
    /**
     * <p>
     * Constructs a new, initialized instance of <code>SampledTimeSeries</code>.
     * </p>
     * <p>
     * The returned <code>SampledTimeSeries</code> instance is populated from the argument data.
     * The sample values are copied from the list argument into a new <code>ArrayList</code> object
     * for fast indexing.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Do NOT supply an empty data values container, an exception will be thrown.
     * Use constructor <code>{@link #SampledTimeSeries(String, DpSupportedType)}</code> instead.
     * </li>
     * <li>
     * Copies the values from argument <code>lstValues</code> into a new <code>{@link ArrayList}</code>
     * for faster indexed access.
     * </li>
     * </ul>
     * </p>
     *
     * @param <T>   the Java data type of the time series values
     * 
     * @param strSourceName     the name of the data source (PV) producing time series data
     * @param enmType           the data type of the time series
     * @param vecValues         ordered list of sampled data values in time series
     * 
     * @return  a new, <code>SampledTimeSeries</code> instance fully initialized with the argument data 
     * 
     * @throws MissingResourceException the data values container was empty (cannot check type consistency)
     * @throws IllegalArgumentException the specified data type is incompatible with generic parameter <code>T</code>
     */
    public static <T extends Object> SampledTimeSeries<T>   from(String strSourceName, DpSupportedType enmType, List<T> vecValues) 
            throws MissingResourceException, IllegalArgumentException 
    {
        return new SampledTimeSeries<T>(strSourceName, enmType, vecValues);
    }
    
    
    /**
     * <p>
     * Creates a new, initialized instance of <code>SampledTimeSeries</code>.
     * </p>
     * <p>
     * The returned <code>SampledTimeSeries</code> instance is populated directly with the argument data.
     * The returned instance assumes ownership of the given container of time-series values and no
     * new resources are created.  Thus, this is the most efficient creator. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * Do NOT supply an empty data values container, an exception will be thrown.
     * Use constructor <code>{@link #SampledTimeSeries(String, DpSupportedType)}</code> instead.
     * </li>
     * <li>
     * Sets the attribute <code>{@link #vecValues}</code> directly from argument <code>vecValues</code>. 
     * </li>
     * </ul>
     * </p>
     *
     * @param <T>   the Java data type of the time series values
     *  
     * @param strSourceName     the name of the data source (PV) producing time series data
     * @param enmType           the data type of the time series
     * @param vecValues         ordered list of sampled data values in time series
     * 
     * @throws MissingResourceException the data values container was empty (cannot check type consistency)
     * @throws IllegalArgumentException the specified data type is incompatible with generic parameter <code>T</code>
     * 
     * @return  a new, <code>SampledTimeSeries</code> instance fully initialized with the argument data 
     */
    public static <T extends Object> SampledTimeSeries<T> from(String strSourceName, DpSupportedType enmType, ArrayList<T> vecValues) 
            throws MissingResourceException, IllegalArgumentException 
    {
        return new SampledTimeSeries<T>(strSourceName, enmType, vecValues);
    }
    
    
    //
    // Class Constants
    //
    
    /** <code>Serializable</code> interface serialization ID - used for allocation size estimation */
    private static final long serialVersionUID = 7510269355880505323L;

    
    //
    // Attributes
    //
    
    /** Name of the data source producing time series values */
    private final String            strSourceName;
    
    /** Data type of the time series values */
    private final DpSupportedType   enmType;
    
    /** Sampled data value of the time series - linked list for anticipated modifications */
    private final ArrayList<T>      vecValues; // = new LinkedList<>();

    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new, partially initialized instance of <code>SampledTimeSeries</code>.
     * </p>
     * <p>
     * The list of values for the sampled time series is constructed but empty.
     * </p>
     *
     * @param strSourceName     the name of the data source (PV) producing time series data
     * @param enmType           the data type of the time series
     */
    public SampledTimeSeries(String strSourceName, DpSupportedType enmType) {
        this.strSourceName = strSourceName;
        this.enmType = enmType;
        this.vecValues = new ArrayList<>();
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>SampledTimeSeries</code> populated with <code>null</code> values.
     * </p>
     * <p>
     * Creates the <code>null</code> time series with length given by argument <code>{@link cntSize}</code>.
     * Can be used to represent time series were sampling was interrupted or otherwise unavailable.
     * </p>
     *
     * @param strSourceName name of the data source producing <code>null</code> values
     * @param enmType       assumed type of the <code>null</code> values
     * @param cntSize       size of the time series (i.e., number of <code>null</code> data values)
     */
    public SampledTimeSeries(String strSourceName, DpSupportedType enmType, int cntSize) {

        this.strSourceName = strSourceName;
        this.enmType = enmType;
        this.vecValues = new ArrayList<>(cntSize);
        
        // Populate sample value vector with null values
        for (int n=0; n<cntSize; n++) 
            this.vecValues.add(null);
    }

    /**
     * <p>
     * Constructs a new, initialized instance of <code>SampledTimeSeries</code>.
     * </p>
     * <p>
     * Copies the values from argument <code>lstValues</code> into a new <code>{@link ArrayList}</code>
     * for faster indexed access.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Do NOT supply an empty data values container, an exception will be thrown.
     * Use constructor <code>{@link #SampledTimeSeries(String, DpSupportedType)}</code> instead.
     * </p>
     *
     * @param strSourceName     the name of the data source (PV) producing time series data
     * @param enmType           the data type of the time series
     * @param vecValues         ordered list of sampled data values in time series
     * 
     * @throws MissingResourceException the data values container was empty (cannot check type consistency)
     * @throws IllegalArgumentException the specified data type is incompatible with generic parameter <code>T</code>
     */
    public SampledTimeSeries(String strSourceName, DpSupportedType enmType, List<T> lstValues) 
            throws MissingResourceException, IllegalArgumentException {
        this(strSourceName, enmType, new ArrayList<T>(lstValues));
    }

    /**
     * <p>
     * Constructs a new, initialized instance of <code>SampledTimeSeries</code>.
     * </p>
     * <p>
     * Sets the attribute <code>{@link #vecValues}</code> directly from argument <code>vecValues</code>. 
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Do NOT supply an empty data values container, an exception will be thrown.
     * Use constructor <code>{@link #SampledTimeSeries(String, DpSupportedType)}</code> instead.
     * </p>
     *
     * @param strSourceName     the name of the data source (PV) producing time series data
     * @param enmType           the data type of the time series
     * @param vecValues         ordered list of sampled data values in time series
     * 
     * @throws MissingResourceException the data values container was empty (cannot check type consistency)
     * @throws IllegalArgumentException the specified data type is incompatible with generic parameter <code>T</code>
     */
    public SampledTimeSeries(String strSourceName, DpSupportedType enmType, ArrayList<T> vecValues) 
            throws MissingResourceException, IllegalArgumentException {

        // Check the argument
        if (vecValues.isEmpty())
            throw new MissingResourceException("The data value container was empty.", vecValues.getClass().getName(), "vecValues");
        
        this.strSourceName = strSourceName;
        this.enmType = enmType;
        this.vecValues = vecValues;
        
        // Check the type
        T   val = this.vecValues.get(0);
        if (!enmType.isAssignableFrom(val))
            throw new IllegalArgumentException("Data value type unassignable to requested type " + enmType);
    }


    //
    // Operations
    //
    
    /**
     * <p>
     * Prepends the given ordered value list of sample values to the head of the time series.
     * </p>
     * <p>
     * Inserts the given argument list at the head of the current time series value list.
     * The type of the value must be compatible with the current time series or an exception
     * is thrown.  Specifically, the time series supported type identified in construction must
     * be assignable from objects of type <code>T</code>.
     * </p>
     *  
     * @param <T>       type of the new sample values, must be compatible with current time series
     * 
     * @param vecValues new sample value for time series
     * 
     * @return  <code>true</code> if the time series was modified by the operation,
     *          <code>false</code> otherwise (typically the argument list was empty)
     * 
     * @throws IllegalArgumentException the given sample values are of incompatible type
     */
    public boolean prependValues(List<T> lstValues) throws IllegalArgumentException {
        
        // Check for empty list
        if (lstValues.isEmpty())
            return false;
        
        // Check type compatibility
        T   value = lstValues.get(0);
        
        if (! this.enmType.isAssignableFrom( value.getClass()) )
            throw new IllegalArgumentException("Agument type " + value.getClass().getSimpleName() + " is incompatible with internal supported type " + this.enmType);

        // Add new values
        return this.vecValues.addAll(0, lstValues);
    }
    
    /**
     * <p>
     * Prepends the given time series to the head of this time series.
     * </p>
     * <p>
     * Inserts all the data values within the given argument at the head of the current time 
     * series value list.
     * The given time series must be from the same data source and have the same data type
     * as the this time series, otherwise an exception is thrown.
     * That is, concatenation is only possible for compatible time series.
     * </p>  
     * 
     * @param stsSeries time series to be prepended to this time series
     * 
     * @return  <code>true</code> if this time series was modified in the operation,
     *          <code>false</code> otherwise (typically an empty time series)
     *          
     * @throws IllegalArgumentException the argument was from a different data source and/or has different type
     */
    public boolean prependSeries(SampledTimeSeries<T> stsSeries) throws IllegalArgumentException {
        
        // Check the argument data source
        if (! this.strSourceName.equals(stsSeries.getName()) )
            throw new IllegalArgumentException("Argument is from a data source other than " + this.strSourceName);
        
        // Check the argument data type
        if (this.enmType != stsSeries.getType())
            throw new IllegalArgumentException("Argument type " + stsSeries.getType() + " not equal to " + this.enmType);
        
        // Add argument data values to this time series
        return this.vecValues.addAll(0, stsSeries.getValuesTyped());
    }
    
    /**
     * <p>
     * Appends the given ordered value list of sample values to the tail of the time series.
     * </p>
     * <p>
     * Adds the given argument list to the tail of the current time series value list.
     * The type of the value must be compatible with the current time series or an exception
     * is thrown.  Specifically, the time series supported type identified in construction must
     * be assignable from objects of type <code>T</code>.
     * </p>
     *  
     * @param <T>       type of the new sample values, must be compatible with current time series
     * 
     * @param vecValues new sample value for time series
     * 
     * @return  <code>true</code> if the time series was modified by the operation,
     *          <code>false</code> otherwise (typically the argument list was empty)
     * 
     * @throws IllegalArgumentException the given sample values are of incompatible type
     */
    public boolean appendValues(List<T> lstValues) throws IllegalArgumentException {

        // Check for empty list
        if (lstValues.isEmpty())
            return false;
        
        // Check type compatibility
        T   value = lstValues.get(0);
        
        if (! this.enmType.isAssignableFrom( value.getClass()) )
            throw new IllegalArgumentException("Agument type " + value.getClass().getSimpleName() + " is incompatible with internal supported type " + this.enmType);

        
        return this.vecValues.addAll(lstValues);
    }
    
    /**
     * <p>
     * Appends the given time series to the tail of this time series.
     * </p>
     * <p>
     * Inserts all the data values within the given argument at the tail of the current time 
     * series value list.
     * The given time series must be from the same data source and have the same data type
     * as the this time series, otherwise an exception is thrown.
     * That is, concatenation is only possible for compatible time series.
     * </p>  
     * 
     * @param stsSeries time series to be appended to this time series
     * 
     * @return  <code>true</code> if this time series was modified in the operation,
     *          <code>false</code> otherwise (typically an empty time series)
     *          
     * @throws IllegalArgumentException the argument was from a different data source and/or has different type
     */
    public boolean appendSeries(SampledTimeSeries<T> stsSeries) throws IllegalArgumentException {
        
        // Check the argument data source
        if (! this.strSourceName.equals(stsSeries.getName()) )
            throw new IllegalArgumentException("Argument is from a data source other than " + this.strSourceName);
        
        // Check the argument data type
        if (this.enmType != stsSeries.getType())
            throw new IllegalArgumentException("Argument type " + stsSeries.getType() + " not equal to " + this.enmType);
        
        // Add argument data values to this time series
        return this.vecValues.addAll(stsSeries.getValuesTyped());
    }
    
    
    //
    // IDataColumn<T> Interface
    //
    
    /**
     * @see com.ospreydcs.dp.api.model.IDataColumn#getName()
     */
    @Override
    public String getName() {
        return this.strSourceName;
    }

    /**
     * @see com.ospreydcs.dp.api.model.IDataColumn#getType()
     */
    @Override
    public DpSupportedType getType() {
        return this.enmType;
    }
    
    /**
     * @see com.ospreydcs.dp.api.model.IDataColumn#getSize()
     */
    @Override
    public final Integer getSize() {
        return this.vecValues.size();
    }
    
    /**
     * @see com.ospreydcs.dp.api.common.IDataColumn#clear()
     */
    @Override
    public final void clear() {
        this.vecValues.clear();
    }

    /**
     * @see com.ospreydcs.dp.api.model.IDataColumn#getValue(int)
     */
    @Override
    public final Object getValue(int index) throws IndexOutOfBoundsException, ArithmeticException {
        return this.vecValues.get(index);
    }
    
    /**
     * @see com.ospreydcs.dp.api.model.IDataColumn#getValues()
     */
    @Override
    @SuppressWarnings("unchecked")
    public final ArrayList<Object> getValues() {
        return (ArrayList<Object>) vecValues;
    }
    
    /**
     * @see com.ospreydcs.dp.api.model.IDataColumn#getValuesTyped()
     */
    @Override
    public final List<T> getValuesTyped() throws ArithmeticException {
        return this.vecValues;
    }
    
    /**
     * <p>
     * Returns the number of bytes required to serialize this time series.
     * </p>
     * <p>
     * The time series is serialized into a byte array buffer.
     * The size of the buffer is returned after serialization.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * If the serialization size of the time series is to large to fit in the data buffer 
     * (larger than <code>{@link Integer#MAX_VALUE}</code>) then an exception is thrown.
     * </p>
     * 
     * @throws UnsupportedOperationException    not thrown
     * @throws ArithmeticException              the time series could not be serialized (e.g., too large)
     * 
     * @see com.ospreydcs.dp.api.common.IDataColumn#allocationSize()
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
    
}
