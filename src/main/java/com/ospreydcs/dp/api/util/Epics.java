/*
 * Project: dp-api
 * File:	Epics.java
 * Package: com.ospreydcs.dp.api.util
 * Type: 	Epics
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
 *
 * @author Christopher K. Allen
 * @org    OspreyDCS
 * @since Oct 13, 2022
 * @version April 4, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Vector;
import java.util.stream.IntStream;

import org.epics.nt.HasTimeStamp;
import org.epics.nt.NTNDArray;
import org.epics.nt.NTTable;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVTimeStampFactory;
import org.epics.pvdata.property.TimeStamp;
import org.epics.pvdata.property.TimeStampFactory;
import org.epics.pvdata.pv.BooleanArrayData;
import org.epics.pvdata.pv.ByteArrayData;
import org.epics.pvdata.pv.DoubleArrayData;
import org.epics.pvdata.pv.FloatArrayData;
import org.epics.pvdata.pv.IntArrayData;
import org.epics.pvdata.pv.LongArrayData;
import org.epics.pvdata.pv.PVBooleanArray;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.PVUByteArray;
import org.epics.pvdata.pv.PVUIntArray;
import org.epics.pvdata.pv.PVULongArray;
import org.epics.pvdata.pv.PVUShortArray;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.ShortArrayData;
import org.epics.pvdata.pv.StringArrayData;
import org.epics.pvdata.pv.StructureArrayData;

import com.google.protobuf.ByteString;
import com.ospreydcs.dp.api.common.ArrayDimension;
import com.ospreydcs.dp.api.model.DpSupportedType;
import com.ospreydcs.dp.api.model.IDataColumn;
import com.ospreydcs.dp.api.model.table.StaticDataColumn;


/**
 * <p>
 * Utility class for supporting EPICS NT and PVStructure data types.
 * </p>
 * <p>
 * This class is used to extract information and data from the EPICS data types <code>{@link NTTable}</code> and
 * <code>{@link NTNDArray}</code>, and also the EPICS <code>{@link PVStructure}</code> data type in general.
 * Extracted data is converted to standard Java types and other supporting types.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Oct 13, 2022
 * @version April 4, 2024
 *
 * @see PVStructure
 * @see NTTable
 * @see NTNDArray
 */
public class Epics {


    //
    // Class Constants
    //
    
    /** NT structure - field name label */
    public static String STR_NT_NAME_LBL = "name";
    
    /** NT structure - field single value label */
    public static String STR_NT_VALUE_LBL = "value";
    
    /** NT structure - field multiple values label (i.e., for arrays) */
    public static String STR_NT_VALUES_LBL = "value";
    
    /** NT object timestamp field label */
    public static final String STR_NT_TMS_LBL = "timeStamp";
    

    /** NTNDArray - Timestamp label for NT tables, structures, and arrays */  
    public static final String STR_NTND_DATA_TMS_LBL = "dataTimeStamp";
    
    /** NTNDArray dimension PVStructure - data size label */
    public static final String STR_NTND_DIMS_SIZE = "size";

    /** NTNDArray dimension PVStructure - data offset label */
    public static final String STR_NTND_DIMS_OFFSET = "offset";

    /** NTNDArray dimension PVStructure - data full size (including offset) label */
    public static final String STR_NTND_DIMS_FULLSIZE = "fullSize";

    /** NTNDArray dimension PVStructure - binned data label */
    public static final String STR_NTND_DIMS_BINNING = "binning";

    /** NTNDArray dimension PVStructure - "data is reversed" label */
    public static final String STR_NTND_DIMS_REVERSE = "reverse";


    /** NTTable (and perhaps NTNDArray) timestamp column label */
    public static String STR_NTTBL_TMS_LBL = "timestamps";
    
    /** NTTable descriptor string field label */
    public static String STR_NTTBL_DSCR_LBL = "descriptor";

    
//    /** Convention - Timestamp epoch seconds label */
//    public static String STR_NT_TMS_EPOCH_LBL = "secondsPastEpoch";
//    
//    /** Convention - Timestamp epoch nanoseconds label */
//    public static String STR_NT_TMS_NANOS_LBL = "nanoseconds";

    

//    /** NTNDArray dimensions field label */
//    public static final String STR_NTNDARR_DIM_LBL = "dimension";

    
    
    //
    // NTTable Data Extraction
    //
    
    /**
     * <p>
     * Extracts the data from an EPICS Normative Type Table (NTTable) and
     * returns it as a list of data columns.
     * </p>
     * <p>
     * The argument is assumed to be <code>NTTable</code> compatible, that is,
     * it can be wrapped by an <code>NTTable</code>.  If not the method
     * throws an exception.
     * </p>
     * <p>
     * Each data column has the name of the table column from which it was extracted.
     * The EPICS PV data type is also extracted and contained in the data column.
     * Thus, the object types in the column can be inferred from the PV type.
     * </p>
     * 
     * @param ntTable EPICS <code>NTTable</code> compatible structure 
     * 
     * @return list of data columns, one for each column in the argument
     * 
     * @throws IllegalArgumentException the argument is not EPICS NTTable compatible
     * @throws TypeNotPresentException an supported EPICS type was encountered in the PV structure data
     */
    public static List<IDataColumn<Object>> extractTableColumns(PVStructure pvsTable) 
            throws IllegalArgumentException, TypeNotPresentException {
        
        // Wrap into an ntTable and exception check
        NTTable ntTable = NTTable.wrap(pvsTable);
        if (ntTable == null)
            throw new IllegalArgumentException("Epics#extractColumns(PVStructure) - argument was not compatible with NTTable");

        // The returned object
        List<IDataColumn<Object>> lstCols = new LinkedList<IDataColumn<Object>>();

        // For every column in the NTTable (by name)
        for (String strColNm : ntTable.getColumnNames()) {
            
            // Extract each ntTable column and create corresponding IDataColumn
            PVScalarArray pvarrScalar = ntTable.getColumn(strColNm);
            ScalarType    typScalar   = pvarrScalar.getScalarArray().getElementType();
            
            // Must check the data type and convert column data accordingly
            DpSupportedType typEpics;
            List<Object>    lstVals;
            switch (typScalar) {
                case pvBoolean -> {
                    typEpics = DpSupportedType.BOOLEAN;
                    lstVals = new ArrayList<>( Epics.toBooleanList((PVBooleanArray)pvarrScalar) );
                }
                case pvByte -> {
                    typEpics = DpSupportedType.BYTE_ARRAY;
                    lstVals = List.of( Epics.toByteString(PVByteArray.class.cast(pvarrScalar)) );
                }
                case pvUByte -> {
                    typEpics = DpSupportedType.BYTE_ARRAY;
                    lstVals = List.of( Epics.toByteString(PVUByteArray.class.cast(pvarrScalar)) );
                }
                case pvShort -> {
                    typEpics = DpSupportedType.INTEGER;
                    lstVals = new ArrayList<>( Epics.toIntegerList((PVShortArray)pvarrScalar) );
                }
                case pvUShort -> {
                    typEpics = DpSupportedType.INTEGER;
                    lstVals = new ArrayList<>( Epics.toIntegerList((PVUShortArray)pvarrScalar) );
                }
                case pvInt -> {
                    typEpics = DpSupportedType.INTEGER;
                    lstVals = new ArrayList<>( Epics.toIntegerList((PVIntArray)pvarrScalar) );
                }
                case pvUInt -> {
                    typEpics = DpSupportedType.INTEGER;
                    lstVals = new ArrayList<>( Epics.toIntegerList((PVUIntArray)pvarrScalar) );
                }
                case pvLong -> {
                    typEpics = DpSupportedType.LONG;
                    lstVals = new ArrayList<>( Epics.toLongArray((PVLongArray)pvarrScalar) );
                }
                case pvULong -> {
                    typEpics = DpSupportedType.LONG;
                    lstVals = new ArrayList<>( Epics.toLongList((PVULongArray)pvarrScalar) );
                }
                case pvFloat -> {
                    typEpics = DpSupportedType.FLOAT;
                    lstVals  = new ArrayList<>( Epics.toFloatArray((PVFloatArray)pvarrScalar) );
                }
                case pvDouble -> {
                    typEpics = DpSupportedType.DOUBLE;
                    lstVals  = new ArrayList<>( Epics.toDoubleArray((PVDoubleArray)pvarrScalar) );
                }
                case pvString -> {
                    typEpics = DpSupportedType.STRING;
                    lstVals  = new ArrayList<>( Epics.toStringList((PVStringArray)pvarrScalar) );
                }
                default -> {
                    throw new TypeNotPresentException("Encounter unsupported EPICS PV type " + typScalar, new Throwable());
                }
            }
            
            // Create the data column and add to list of columns
            IDataColumn<Object> colData = StaticDataColumn.from(strColNm, typEpics, lstVals);
            lstCols.add(colData);
        }
        
        return lstCols;
    }
    
//    /**
//     * <p>
//     * Returns a list of <code>Objects</code> given a list of elements of type
//     * <code>T</code>.  The objects in the returned list are the same as in the
//     * argument just with a type raising operation applied.  
//     * An <code>ArrayList&lt;Object&gt;</code> is currently the specific 
//     * list implementation.
//     * 
//     * @param <T> subtype of class <code>Object</code>
//     * 
//     * @param lstVals the list to be type-generalized
//     * 
//     * @return a list of the same objects typed to <codd>Object</code>
//     */
//    private static <T extends Object> List<Object> toObjectList(List<T> lstVals) {
//        ArrayList<Object> lstObjs = new ArrayList<Object>( lstVals.size() );
//        
//        lstObjs.addAll(lstVals);
//        
//        return lstObjs;
//    }
    
    /**
     * <p>
     * Extracts the timestamp for the argument <em>itself</em>, which is assumed to be 
     * <code>NTTable</code> compatible.
     * </p>
     * <p>
     * Note the difference between this method and <code>{@link #extractTableTimestamps(PVStructure)}</code>
     * which extracts the timestamp <em>column</em> from the argument.
     * </p>
     * 
     * @param pvsTable  <code>NTTable</code> compatible argument
     * 
     * @return          the timestamp given to the data table
     * 
     * @throws IllegalArgumentException the argument is not EPICS NTTable compatible
     * @throws MissingResourceException the argument did not have a timestamp 
     */
    public static Instant   extractTableTimestamp(PVStructure pvsTable) throws IllegalArgumentException, MissingResourceException {
        
        NTTable ntTable = NTTable.wrap(pvsTable);
        // If we cannot wrap the PV structure as an NTTable we quit now 
        if (ntTable == null)
            throw new IllegalArgumentException("Epics#extractTableTimestamp(PVStructure) - argument was not compatible with NTTable");
        
        // Get the timestamp of the table and check it
        Instant insTms = Epics.extractTimestamp(ntTable);

        if (insTms == null) 
            throw new UnsupportedOperationException("Epics#extractTableTimestamp(PVStructure) - argument does not contain a timestamp.");
        
        return insTms;
    }
    
    /**
     * <p>
     * Extracts the timestamp <em>column</em> from the given argument which is assumed to be 
     * <code>NTTable</code> compatible.
     * </p>
     * <p>
     * The argument is assumed to be an <code>NTTable</code> or an equivalent representation 
     * (a <code>PVStructure</code> that can be wrapped by an <code>NTTable</code>.   
     * The timestamp column for the <code>NTTable</code> is extracted and returned as an ordered vector of
     * Java <code>Instant</code> instances.
     * </p>
     * <p>
     * Consider the following conditions:
     * <ul>
     * <li>If the argument cannot be wrapped by an ntTable - <code>IllegalArgumentException</code></li>
     * <li>If the argument contains no timestamp column ("timestamps" field) - <code>MissingResourceException</code></li>
     * </ul>
     * Otherwise, we can extract the timestamp values from the column, convert
     * them to <code>Instant</code> instances, then create the time series.
     * </p>
     * 
     * @param pvsTable ntTable compatible PV structure containing "timestamps" column
     *  
     * @return ordered list of timestamps extracted from the given NTTable timestamps column
     * 
     * @throws IllegalArgumentException the argument is not EPICS NTTable compatible
     * @throws MissingResourceException the argument did not have a timestamp column 
     */
    public static ArrayList<Instant> extractTableTimestamps(PVStructure pvsTable) 
            throws IllegalArgumentException, MissingResourceException {
        
        // If we cannot wrap the PV structure as an NTTable we quit now 
        NTTable ntTable = NTTable.wrap(pvsTable);
        if (ntTable == null)
            throw new IllegalArgumentException("Epics#extractTableTimestamps(PVStructure) - argument was not compatible with NTTable");
        
        // Get the timestamp column and check it
        PVStructureArray pvsaTms = pvsTable.getStructureArrayField(STR_NTTBL_TMS_LBL);

        if (pvsaTms == null)
            throw new MissingResourceException("Epics#extractTableTimestamps(PVStructure) - argument contains NO timestamp column.", PVStructure.class.getName(), STR_NTTBL_TMS_LBL);

        // The returned value
        ArrayList<Instant> vecIns = new ArrayList<>(pvsaTms.getLength());

        // If the timestamp column exists, extract the data
        StructureArrayData saDataTms = new StructureArrayData();
        // Insert the PVStruct data into the StructArray data - "get" it in there
        pvsaTms.get(0, pvsaTms.getLength(), saDataTms);

        // For each structure in the structure array, treat it as a timestamp (should be time?)
        for (PVStructure pvsVal : saDataTms.data) {
            if (pvsVal instanceof HasTimeStamp ntHasTms) {
                vecIns.add(Epics.extractTimestamp(ntHasTms));
            }
        }
        
        return vecIns;
    }
    
    
    //
    // NTNDArray Data Extraction
    //
    
    /**
     * <p>
     * Extracts all the data from an <code>NTNDArray</code> as a byte array.
     * </p>
     * <p>
     * The argument provided is expected to be <code>NTNDArray</code> compatible,
     * that is, it can be wrapped by an ntNDArray type.  If it is not an 
     * exception is thrown. Otherwise, all data within the array
     * is extracted as a raw <code>byte[]</code> array and returned.
     * See <code>Epics{@link #extractArrayDimensions(PVStructure)}</code> for information
     * about the NtArray dimensions.
     * </p>
     * 
     * @param pvsNdArray an <code>NTNDArray</code> compatible structure

     * @return the data contained in the argument as raw byte array
     * 
     * @throws IllegalArgumentException the argument was not <code>NTNDArray</code> compatible
     * 
     * @see Epics#extractArrayDimensions(PVStructure)
     */
    public static byte[]    extractArrayRawData(PVStructure pvsNdArray) throws IllegalArgumentException {
        
        // Check for NT array compatibility
        NTNDArray ntNdArray = NTNDArray.wrap(pvsNdArray);
        if (ntNdArray == null)
            throw new IllegalArgumentException("Epics#extractData(PVStructure) - argument was incompatible with NTNDArray");
        
        // Extract the raw data
        PVUnion pvuData = ntNdArray.getValue();
        PVByteArray pvbaData = (PVByteArray)pvuData;
        ByteArrayData baData = new ByteArrayData();
        pvbaData.get(0, pvbaData.getLength(), baData);

        return baData.data;
    }

    /**
     * <p>
     * Extracts the <code>DataTimeStamp</code> from the given argument which is assumed to be 
     * <code>NTNDArray</code> compatible. 
     * </p>
     * <p>
     * <p>
     * The argument provided is expected to be <code>NTNDArray</code> compatible, that is, it can be wrapped by 
     * an ntNDArray type.  If it is not an exception is thrown. Otherwise, the "data timestamp" of the array
     * is extracted and packaged into an <code>Instant</code>.  
     * </p>
     * 
     * @param pvsNdArray an <code>NTNDArray</code> compatible structure
     * 
     * @return the value of the "data timestamp" field of the argument
     * 
     * @throws IllegalArgumentException the argument was not <code>NTNDArray</code> compatible
     * @throws MissingResourceException the argument contained no timestamp
     */
    public static Instant extractArrayDataTimestamp(PVStructure pvsNdArray) throws IllegalArgumentException {

        // Check for NT array compatibility
        NTNDArray ntNdArray = NTNDArray.wrap(pvsNdArray);
        if (ntNdArray == null)
            throw new IllegalArgumentException("Epics#getArrayDataTimestamp(PVStructure) - argument was incompatible with NTNDArray");
        
        // Extract the array timestamp and check it
        PVStructure pvsTmsData = ntNdArray.getDataTimeStamp();
        if (pvsTmsData instanceof HasTimeStamp tmsData) {
            return Epics.extractTimestamp(tmsData);
            
        } else 
            throw new MissingResourceException("Epics#getArrayDataTimestamp(PVStructure) - argument contained no timestamp.", 
                    PVStructure.class.getName(), 
                    STR_NTND_DATA_TMS_LBL);
    }
    
    /**
     * <p>
     * Extracts all dimensions information from the given argument.
     * </p>
     * <p>
     * The argument provided is expected to be <code>NTNDArray</code> compatible,
     * that is, it can be wrapped by an ntNDArray type.  If it is not an 
     * exception is thrown. Otherwise, the dimension information of the array
     * is extracted and packaged into the returned list.  The list contains
     * one entry for each dimension of the array, in order or table dimensions.
     * </p>
     * 
     * @param pvsNdArray an <code>NTNDArray</code> compatible structure
     * 
     * @return list of array dimensions information (one element per dimension)
     * 
     * @throws IllegalArgumentException the argument was not <code>NTNDArray</code> compatible
     */
    public static List<ArrayDimension> extractArrayDimensions(PVStructure pvsNdArray) throws IllegalArgumentException {
        
        // Wrap argument as an ntArray and exception check
        NTNDArray ntArray = NTNDArray.wrap(pvsNdArray);
        if (ntArray == null)
            throw new IllegalArgumentException("Epics#extractDimensions(PVStructure) - argument was incompatible with NTNDArray");
        
        PVStructureArray pvsaDimFld = ntArray.getDimension();
        StructureArrayData sadDims = new StructureArrayData();
        pvsaDimFld.get(0, pvsaDimFld.getLength(), sadDims);
        
        PVStructure[]   arrPvData = sadDims.data;
        int             intOffset = sadDims.offset;
        int             szArray = arrPvData.length;

        List<ArrayDimension> lstDims = IntStream
                   .range(intOffset, pvsaDimFld.getLength())
                   .mapToObj( i -> extractDimension(sadDims.data[i]))
                   .toList();                  
        
        return lstDims;
    }
    
    /**
     * <p>
     * Extract dimension description data for a given dimension of an <code>NTNDArray</code>.
     * </p>
     * <p>
     * The argument is assumed to be a dimension field from an <code>NTNDArray</code> instance obtained
     * with the <code>{@link NTNDArray#getDimension()}</code> method.  The information for each dimension
     * is extracted by reading the result into a <code>StructureArrayData</code> instance then identifying
     * each  dimension <i>i</i> information by index <code>{@link StructureArrayData#data}[i]</code>.
     * The above value is expected as an argument.
     * </p> 
     *
     * @param pvsDim PVStructure containing NTNDTable dimension information
     *  
     * @return metadata about the given dimension
     */
    private static ArrayDimension extractDimension(PVStructure pvsDim) {
        int cntVals = pvsDim.getIntField(STR_NTND_DIMS_SIZE).get();
        int indOffset = pvsDim.getIntField(STR_NTND_DIMS_OFFSET).get();
        int szFull = pvsDim.getIntField(STR_NTND_DIMS_FULLSIZE).get();
        int szBin = pvsDim.getIntField(STR_NTND_DIMS_BINNING).get();
        boolean bolReversed = pvsDim.getBooleanField(STR_NTND_DIMS_REVERSE).get();

        return new ArrayDimension(cntVals, indOffset, szFull, szBin, bolReversed);
    }
    
    
    // 
    // General NT Structures Data Extraction
    //

    /**
     * <p>
     * Extract the timestamp from an NT PVStructure that has a timestamp,
     * specifically, one exposing the <code>HasTimeStamp</code> interface.
     * </p>
     * <p>
     * By convention the timestamp field is labeled "timeStamp".  If
     * no timestamp is found then a <code>null</code> value is returned.
     * </p>
     *
     * @param ntData NT data structure having a timestamp
     * 
     * @return timestamp for argument, 
     *         or <code>null</code> if the structure contained no timestamp
     *         
     * @throws MissingResourceException the argument contained no timestamp
     */
    public static Instant extractTimestamp(HasTimeStamp ntData) throws MissingResourceException {
        
        // Get the timestamp and check it
        PVStructure pvsTmsNtArr = ntData.getTimeStamp();

        if (pvsTmsNtArr == null)
            throw new MissingResourceException("Epics#extractTimestamp(HasTimeStamp) - argument had no timestamp field.",
                    HasTimeStamp.class.getName(),
                    STR_NT_TMS_LBL
                    );

        // Extract the timestamp and create a equivalent Java Instant 
        PVTimeStamp pvTms = PVTimeStampFactory.create();
        if (pvTms.attach(pvsTmsNtArr)) {
            TimeStamp tms = TimeStampFactory.create();
            pvTms.get(tms);
            
            return Instant.ofEpochSecond(tms.getSecondsPastEpoch(), tms.getNanoseconds());
            
        } else 
            throw new MissingResourceException("Epics#extractTimestamp(HasTimeStamp) - argument had no timestamp field.",
                    HasTimeStamp.class.getName(),
                    STR_NT_TMS_LBL
                    );
    }
    
    /**
     * <p>
     * Extracts and returns the name of the given <code>PVStructure</code>
     * argument.
     * </p>  
     * <p>
     * The string field with name <code>Epics{@link #STR_NT_NAME_LBL}</code>
     * is acquired from the argument and the value of that field is returned.
     * If the argument does not contain a name field <code>null</code> is 
     * returned. 
     * </p>
     * 
     * @param  pvs PV structure containing name field
     * 
     * @return the value of the argument's name field
     * 
     * @throws MissingResourceException the argument contained no name field 
     */
    public static String extractName(PVStructure pvs) throws MissingResourceException {
        
        // Extract name and check
        PVString    pvstrName = pvs.getStringField(STR_NT_NAME_LBL);
        
        if (pvstrName == null)
            throw new MissingResourceException("Epics#extractName(PVStructure) - argument contains no name field.",
                    PVStructure.class.getName(),
                    STR_NT_NAME_LBL);
        
        return pvstrName.get();
    }

    
    //
    // PVArray Data Conversion
    //
    
    /**
     * Converts an EPICS <code>PVStringArray</code> to an equivalent 
     * list of strings.
     * 
     * @param pvarrString EPICS data structure
     * 
     * @return list of strings extracted from the argument
     */
    public static List<String> toStringList(PVStringArray pvarrString) {
        StringArrayData dataArrStr = new StringArrayData();
        pvarrString.get(0, pvarrString.getLength(), dataArrStr);
        
        return Arrays.asList(dataArrStr.data);
    }

    /**
     * Converts an EPICS <code>PVBooleanArray</code> to an equivalent 
     * list of <code>Boolean</code> instances.
     * 
     * @param pvarrBool EPICS data structure
     * 
     * @return list of <code>Boolean</code> values extracted from the argument
     */
    public static List<Boolean> toBooleanList(PVBooleanArray pvarrBool) {
        BooleanArrayData dataArrBol = new BooleanArrayData();
        pvarrBool.get(0, pvarrBool.getLength(), dataArrBol);

        ArrayList<Boolean> lstVals = new ArrayList<Boolean>(dataArrBol.data.length);
        for (boolean val : dataArrBol.data) 
            lstVals.add(val);

        return lstVals;
    }

    /**
     * <p>
     * Converts an EPICS <code>PVUByteArray</code> to an equivalent 
     * list of <code>Byte</code> instances.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * Since Java does not support unsigned bytes types the argument actually
     * contains <code>byte</code> types which are directly converted.
     * </p>
     * 
     * @param pvarrUByte EPICS data structure
     * 
     * @return list of <code>Byte</code> values extracted from the argument
     * 
     * @deprecated  The Data Platform does not support byte scalar data
     */
    @Deprecated(since="April 4, 2024")
    public static List<Byte> toByteList(PVUByteArray pvarrUByte) {
        ByteArrayData dataByteArr = new ByteArrayData();
        pvarrUByte.get(0, pvarrUByte.getLength(), dataByteArr);
        
        var lstVals = new ArrayList<Byte>(dataByteArr.data.length);
        for (byte val : dataByteArr.data) 
            lstVals.add(val);

        return lstVals;
    }

    /**
     * <p>
     * Converts an EPICS <code>PVByteArray</code> to an equivalent 
     * list of <code>Byte</code> instances.
     * </p>
     * <p>
     * 
     * @param pvarrByte EPICS data structure
     * 
     * @return list of <code>Byte</code> values extracted from the argument
     * 
     * @deprecated  The Data Platform does not support byte scalar data
     */
    @Deprecated(since="April 4, 2024")
    public static List<Byte> toByteList(PVByteArray pvarrByte) {
        ByteArrayData dataByteArr = new ByteArrayData();
        pvarrByte.get(0, pvarrByte.getLength(), dataByteArr);
        
        var lstVals = new ArrayList<Byte>(dataByteArr.data.length);
        for (byte val : dataByteArr.data) 
            lstVals.add(val);

        return lstVals;
    }

    /**
     * <p>
     * Converts an EPICS <code>PVByteArray</code> to a Protobuf <code>ByteString</code> instance.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * The returned instance is the result of converting the argument into a string of raw data 
     * bytes obtained from the argument. 
     * </p>
     * 
     * @param pvarrUByte EPICS data structure
     * 
     * @return instance of <code>ByteString</code> whose values are extracted from the argument
     * 
     * @see ByteString
     */
    public static ByteString toByteString(PVByteArray pvarrUByte) {
        
        // Get the argument data
        ByteArrayData   dataByteArr = new ByteArrayData();
        pvarrUByte.get(0, pvarrUByte.getLength(), dataByteArr);

        // Pack the byte string
        byte[]          arrBytesRaw = dataByteArr.data;
        ByteString      arrBytesStr = ByteString.copyFrom(arrBytesRaw);
        
        return arrBytesStr;
    }

    /**
     * <p>
     * Converts an EPICS <code>PVUByteArray</code> to a Protobuf <code>ByteString</code> instance.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * Java does not support unsigned bytes types.  The returned instance is the result of converting
     * the argument into a string of raw data bytes obtained from the argument. 
     * </p>
     * 
     * @param pvarrUByte EPICS data structure
     * 
     * @return instance of <code>ByteString</code> whose values are extracted from the argument
     * 
     * @see ByteString
     */
    public static ByteString toByteString(PVUByteArray pvarrUByte) {
        
        // Get the argument data
        ByteArrayData   dataByteArr = new ByteArrayData();
        pvarrUByte.get(0, pvarrUByte.getLength(), dataByteArr);

        // Pack the byte string
        byte[]          arrBytesRaw = dataByteArr.data;
        ByteString      arrBytesStr = ByteString.copyFrom(arrBytesRaw);
        
        return arrBytesStr;
    }

    /**
     * <p>
     * Converts an EPICS <code>PVUShortArray</code> to an equivalent list of <code>Integer</code> instances.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * Since Java does not support unsigned short types the argument actually contains <code>short</code> types 
     * which are converted.
     * <br/><br/>
     * The Data Platform does not support short integral types (as scalars).  Thus, they are converted to
     * <code>int</code> primitive types then <code>Integer</code> objects.
     * </p>
     * 
     * @param pvarrUShot EPICS data structure
     * 
     * @return list of <code>Short</code> values extracted from the argument
     */
    public static List<Integer> toIntegerList(PVUShortArray pvarrUShort) {
        ShortArrayData dataArrUShort = new ShortArrayData();
        pvarrUShort.get(0, pvarrUShort.getLength(), dataArrUShort);
        
        ArrayList<Integer> lstVals = new ArrayList<>(dataArrUShort.data.length);
        for (short shrVal : dataArrUShort.data) { 
            Integer intVal = (int)shrVal;
            
            lstVals.add(intVal);
        }
        
        return lstVals;
    }

    /**
     * <p>
     * Converts an EPICS <code>PVShortArray</code> to an equivalent list of <code>Integer</code> instances.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The Data Platform does not support short integral types (as scalars).  Thus, they are converted to
     * <code>int</code> primitive types then <code>Integer</code> objects.
     * </p>
     * 
     * @param pvarrShort EPICS data structure
     * 
     * @return list of <code>Short</code> values extracted from the argument
     */
    public static List<Integer> toIntegerList(PVShortArray pvarrShort) {
        ShortArrayData dataArrShort = new ShortArrayData();
        pvarrShort.get(0, pvarrShort.getLength(), dataArrShort);

        ArrayList<Integer> lstVals = new ArrayList<>(dataArrShort.data.length);
        for (short shrVal : dataArrShort.data) {
            Integer intVal = (int)shrVal;
            
            lstVals.add(intVal);
        }
        
        return lstVals;
    }

    /**
     * <p>
     * Converts an EPICS <code>PVUIntArray</code> to an equivalent list of <code>Integer</code> instances.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * Since Java does not support unsigned integer types the argument actually
     * contains <code>int</code> types which are directly converted.
     * </p>
     * 
     * @param pvarrUInt EPICS data structure
     * 
     * @return list of <code>Integer</code> values extracted from the argument
     */
    public static List<Integer> toIntegerList(PVUIntArray pvarrUInt) {
        IntArrayData dataArrInt = new IntArrayData();
        pvarrUInt.get(0, pvarrUInt.getLength(), dataArrInt);
        
        return Arrays.stream(dataArrInt.data).boxed().toList();
    }

    /**
     * <p>
     * Converts an EPICS <code>PVIntArray</code> to an equivalent list of <code>Integer</code> instances.
     * </p>
     * 
     * @param pvarrInt EPICS data structure
     * 
     * @return list of <code>Integer</code> values extracted from the argument
     */
    public static List<Integer> toIntegerList(PVIntArray pvarrInt) {
        IntArrayData dataArrInt = new IntArrayData();
        pvarrInt.get(0, pvarrInt.getLength(), dataArrInt);
        
        return Arrays.stream(dataArrInt.data).boxed().toList();
    }

    /**
     * <p>
     * Converts an EPICS <code>PVULongArray</code> to an equivalent 
     * list of <code>Long</code> instances.
     * </p>
     * <p>
     * <h2>NOTE:</h2>
     * Since Java does not support unsigned long types the argument actually
     * contains <code>long</code> types which are directly converted.
     * </p>
     * 
     * @param pvarrULong EPICS data structure
     * 
     * @return list of <code>Long</code> values extracted from the argument
     */
    public static List<Long> toLongList(PVULongArray pvarrULong) {
        LongArrayData dataArrLng = new LongArrayData();
        pvarrULong.get(0, pvarrULong.getLength(), dataArrLng);
        
        return Arrays.stream(dataArrLng.data).boxed().toList();
    }

    /**
     * <p>
     * Converts an EPICS <code>PVLongArray</code> to an equivalent 
     * list of <code>Long</code> instances.
     * </p>
     * 
     * @param pvarrLong EPICS data structure
     * 
     * @return list of <code>Long</code> values extracted from the argument
     */
    public static List<Long> toLongArray(PVLongArray pvarrLong) {
        LongArrayData dataArrLng = new LongArrayData();
        pvarrLong.get(0, pvarrLong.getLength(), dataArrLng);
        
        return Arrays.stream(dataArrLng.data).boxed().toList();
    }

    /**
     * <p>
     * Converts an EPICS <code>PVFloatArray</code> to an equivalent 
     * list of <code>Float</code> instances.
     * </p>
     * 
     * @param pvarrFloat EPICS data structure
     * 
     * @return list of <code>Float</code> values extracted from the argument
     */
    public static List<Float> toFloatArray(PVFloatArray pvarrFloat) {
        FloatArrayData dataArrFlt = new FloatArrayData();
        pvarrFloat.get(0, pvarrFloat.getLength(), dataArrFlt);
        
        ArrayList<Float> lstVals = new ArrayList<Float>(dataArrFlt.data.length);
        for (float val : dataArrFlt.data)    
            lstVals.add(val);
        
        return lstVals;
    }

    /**
     * <p>
     * Converts an EPICS <code>PVDoubleArray</code> to an equivalent 
     * list of <code>Double</code> instances.
     * </p>
     * 
     * @param pvarrDouble EPICS data structure
     * 
     * @return list of <code>Double</code> values extracted from the argument
     */
    public static List<Double> toDoubleArray(PVDoubleArray pvarrDouble) {
        DoubleArrayData dataArrDbl = new DoubleArrayData();
        pvarrDouble.get(0, pvarrDouble.getLength(), dataArrDbl);
        return Arrays.stream(dataArrDbl.data).boxed().toList();
    }

    
    //
    // Private Methods
    //
    
//    private static String getName(PVStructure variable) {
//        return variable.getStringField(STR_NT_NAME_LBL).get();
//    }
//
//    private static PVByteArray getPvByteArray(PVStructure ntNdArray) {
//        return (PVByteArray) ntNdArray.getUnionField(STR_NT_VALUES_LBL).get();
//    }
//

    
    
    //
    // Constructors
    //
    /**
     * <p>
     * Prevent construction of an <code>Epics</code> instance.
     * </p>
     */
    private Epics() {
    }

}
