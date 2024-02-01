/*
 * Project: dp-api-common
 * File:	SampledTimeSeries.java
 * Package: com.ospreydcs.dp.api.query.model.data
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
package com.ospreydcs.dp.api.query.model.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.model.DpSupportedType;

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
 * <li>Depending upon final usage, the container type for values might be changed to <code>Vector</code>.</li>
 * <li>Add an interface implementation such as <code>IDataColumn</code>.</li>
 * </ul>
 *
 * @author Christopher K. Allen
 * @since Jan 30, 2024
 *
 */
public class SampledTimeSeries {
    
    
    //
    // Attributes
    //
    
    /** Name of the data source producing time series values */
    private final String            strSourceName;
    
    /** Data type of the time series values */
    private final DpSupportedType   enmType;
    
    /** Sampled data value of the time series - linked list for anticipated modifications */
    private final List<Object>      lstValues = new LinkedList<>();

    
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
    public static SampledTimeSeries nullSeries(String strSourceName, DpSupportedType enmType, int cntSize) {
        List<Object>    lstNulls = new ArrayList<>(cntSize);
        
        for (int n=0; n<cntSize; n++) 
            lstNulls.add(null);
        
        SampledTimeSeries   stms = new SampledTimeSeries(strSourceName, enmType, lstNulls);
        
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
     * @param msgDataCol    Protobuf message containing initialization data
     * 
     * @return  new <code>SampledTimeSeries</code> instance populated with argument data
     * 
     * @throws IllegalArgumentException      the argument contained no data (empty message)
     * @throws UnsupportedOperationException the argument contained an unsupported data type
     */
    public static SampledTimeSeries  from(com.ospreydcs.dp.grpc.v1.common.DataColumn msgDataCol) 
            throws IllegalArgumentException, UnsupportedOperationException {
        
        // Extract message data
        String          strName = msgDataCol.getName();
        DpSupportedType enmType = ProtoMsg.extractType(msgDataCol);
        List<Object>    lstVals = ProtoMsg.extractValues(msgDataCol);

        // Create initialized time series
        SampledTimeSeries   stms = new SampledTimeSeries(strName, enmType, lstVals);

        return stms;
    }
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new, partially initialized instance of <code>SampledTimeSeries</code>.
     * </p>
     * <p>
     * The list of values for the sampled time series is empty.
     * </p>
     *
     * @param strSourceName     the name of the data source (PV) producing time series data
     * @param enmType           the data type of the time series
     */
    public SampledTimeSeries(String strSourceName, DpSupportedType enmType) {
        this.strSourceName = strSourceName;
        this.enmType = enmType;
    }

    /**
     * <p>
     * Constructs a new, initialized instance of <code>SampledTimeSeries</code>.
     * </p>
     *
     * @param strSourceName     the name of the data source (PV) producing time series data
     * @param enmType           the data type of the time series
     * @param lstValues         ordered list of sampled data values in time series
     * 
     * @throws IllegalArgumentException the given sample values are of incompatible type
     */
    public <T extends Object> SampledTimeSeries(String strSourceName, DpSupportedType enmType, List<T> lstValues) 
            throws IllegalArgumentException {
        this.strSourceName = strSourceName;
        this.enmType = enmType;

        this.appendValues(lstValues);
    }


    //
    // Attribute Getters
    //
    
    /**
     * Returns the data source name for the time series.
     * 
     * @return data source name
     */
    public final String getSourceName() {
        return strSourceName;
    }

    /**
     * Returns the data type of the time series.
     * 
     * @return time series data type as <code>DpSupportedType</code> enumeration constant
     */
    public final DpSupportedType getType() {
        return enmType;
    }
    
    /**
     * Returns the size of the time series.
     * 
     * @return  the number of data values within the time series
     */
    public final int getSize() {
        return this.lstValues.size();
    }

    /**
     * Returns the ordered list time series values.
     * 
     * @return the current list of time series values
     */
    public final List<Object> getValues() {
        return lstValues;
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
     * @param lstValues new sample value for time series
     * 
     * @return  <code>true</code> if the time series was modified by the operation,
     *          <code>false</code> otherwise (typically the argument list was empty)
     * 
     * @throws IllegalArgumentException the given sample values are of incompatible type
     */
    public <T extends Object> boolean prependValues(List<T> lstValues) throws IllegalArgumentException {
        
        // Check for empty list
        if (lstValues.isEmpty())
            return false;
        
        // Check type compatibility
        T   value = lstValues.get(0);
        
        if (! this.enmType.isAssignableFrom( value.getClass()) )
            throw new IllegalArgumentException("Agument type " + value.getClass().getSimpleName() + " is incompatible with internal supported type " + this.enmType);

        // Add new values
        return this.lstValues.addAll(0, lstValues);
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
    public boolean prependSeries(SampledTimeSeries stsSeries) throws IllegalArgumentException {
        
        // Check the argument data source
        if (! this.strSourceName.equals(stsSeries.getSourceName()) )
            throw new IllegalArgumentException("Argument is from a data source other than " + this.strSourceName);
        
        // Check the argument data type
        if (this.enmType != stsSeries.getType())
            throw new IllegalArgumentException("Argument type " + stsSeries.getType() + " not equal to " + this.enmType);
        
        // Add argument data values to this time series
        return this.lstValues.addAll(0, stsSeries.getValues());
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
     * @param lstValues new sample value for time series
     * 
     * @return  <code>true</code> if the time series was modified by the operation,
     *          <code>false</code> otherwise (typically the argument list was empty)
     * 
     * @throws IllegalArgumentException the given sample values are of incompatible type
     */
    public <T extends Object> boolean appendValues(List<T> lstValues) throws IllegalArgumentException {

        // Check for empty list
        if (lstValues.isEmpty())
            return false;
        
        // Check type compatibility
        T   value = lstValues.get(0);
        
        if (! this.enmType.isAssignableFrom( value.getClass()) )
            throw new IllegalArgumentException("Agument type " + value.getClass().getSimpleName() + " is incompatible with internal supported type " + this.enmType);

        
        return this.lstValues.addAll(lstValues);
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
    public boolean appendSeries(SampledTimeSeries stsSeries) throws IllegalArgumentException {
        
        // Check the argument data source
        if (! this.strSourceName.equals(stsSeries.getSourceName()) )
            throw new IllegalArgumentException("Argument is from a data source other than " + this.strSourceName);
        
        // Check the argument data type
        if (this.enmType != stsSeries.getType())
            throw new IllegalArgumentException("Argument type " + stsSeries.getType() + " not equal to " + this.enmType);
        
        // Add argument data values to this time series
        return this.lstValues.addAll(stsSeries.getValues());
    }
    
}
