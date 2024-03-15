/*
 * Project: dp-api-common
 * File:	ProtoMsg.java
 * Package: com.ospreydcs.dp.api.util
 * Type: 	ProtoMsg
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

 * @author Christopher K. Allen
 * @org    OspreyDCS
 * @since Sep 27, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.util;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.SortedMap;
import java.util.Vector;
import java.util.stream.Collectors;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.ospreydcs.dp.api.common.BufferedImage;
import com.ospreydcs.dp.api.common.BufferedImage.Format;
import com.ospreydcs.dp.api.model.AAdvancedApi;
import com.ospreydcs.dp.api.model.DpSupportedType;
import com.ospreydcs.dp.api.model.PvMetaRecord;
import com.ospreydcs.dp.api.query.model.process.UniformSamplingClock;
import com.ospreydcs.dp.grpc.v1.common.Array;
import com.ospreydcs.dp.grpc.v1.common.Attribute;
import com.ospreydcs.dp.grpc.v1.common.DataColumn;
import com.ospreydcs.dp.grpc.v1.common.DataValue;
import com.ospreydcs.dp.grpc.v1.common.DataValue.ValueCase;
import com.ospreydcs.dp.grpc.v1.common.DataValueType;
import com.ospreydcs.dp.grpc.v1.common.Structure.Field;
import com.ospreydcs.dp.grpc.v1.common.Image;
import com.ospreydcs.dp.grpc.v1.common.Image.FileType;
import com.ospreydcs.dp.grpc.v1.common.SamplingClock;
import com.ospreydcs.dp.grpc.v1.common.Structure;
import com.ospreydcs.dp.grpc.v1.common.Timestamp;
import com.ospreydcs.dp.grpc.v1.common.TimestampList;
import com.ospreydcs.dp.grpc.v1.query.QueryMetadataResponse;
import com.ospreydcs.dp.grpc.v1.query.QueryTableResponse;

/**
 * <p>
 * Utility class for object extraction, conversion, and creation of Protobuf messages in the Data Platform
 * gRPC interface.
 * </p>
 * <p>
 * Task operations within class are identified by method or method prefix as follows:
 * <ul>
 * <li><code>from</code> Methods
 *     <br/>
 *     Building Protobuf messages atomically from Java language or library objects.
 *     The message type is determined by the argument type.
 * </li>
 * <br/>
 * <li><code>create-</code> Prefix 
 *     <br/>
 *     Builds Protobuf messages from multiple arguments and/or populated Java containers.
 *     <br/>
 * </li>
 * <br/>
 * <li><code>to-</code> Prefix 
 *     <br/>
 *     Converting Data Platform Protobuf messages to Java language object, library objects, or containers.
 *     <br/>
 * </li>
 * <br/>
 * <li><code>extract-</code> Prefix 
 *     <br/>
 *     Extracts specific fields from Protobuf messages as identified by the suffix.
 *     The field values are converted to Java language types.
 *     <br/>
 * </li>
 * </ul> 
 *
 * @author Christopher K. Allen
 * @since Sep 27, 2022
 * @version 2 - Dec, 2023
 */
public final class ProtoMsg {
    
    
    //
    // Java Objects to Protobuf Messages
    //
    
    /**
     * <p>
     * Converts the given Java <code>Data</code> object to an appropriate <code>Timestamp</code> message.
     * </p>
     * <p>
     * Note that the time instant represented by a Java <code>Data</code> has millisecond precision.  Thus,
     * the returned <code>Timestamp</code> offset component is limited by this condition, that is, the mapping
     * is not epimorphic ("onto").
     * </p>
     * 
     * @param date   Java time instant
     * 
     * @return  a Timestamp equivalence class to the millisecond  
     */
    public static Timestamp from(Date date) {
        return ProtoMsg.from(date.toInstant());
    }

    /**
     * Converts the given Java <code>Instant</code> object to a equivalent <code>Timestamp</code> message.
     * 
     * @param ins   Java time instant
     * 
     * @return  equivalent timestamp 
     */
    public static Timestamp from(Instant ins) {
        Timestamp.Builder bldr = Timestamp.newBuilder();
        
        bldr.setEpochSeconds(ins.getEpochSecond());
        bldr.setNanoseconds(ins.getNano());
        
        return bldr.build();
    }

    /**
     * Converts the given Java <code>Instant</code> list to a corresponding
     * ordered <code>TimestampList</code> message.
     * 
     * @param lstIns ordered list of time instants
     * 
     * @return equivalent <code>TimestampList</code> instance populated from the argument
     */
    public static TimestampList from(List<Instant> lstIns) {
        List<Timestamp> lstTms = lstIns.stream().map( t -> ProtoMsg.from(t) ).toList();
        
        TimestampList.Builder bldr = TimestampList.newBuilder();
        bldr.addAllTimestamps(lstTms);
        
        return bldr.build();
    }

    /**
     * <p>
     * Creates and returns a new Protobuf <code>Image</code> message from the given buffered image.
     * </p>
     * <p>
     * The image data and the image format are taken from the <code>BufferedImage</code> 
     * instance to create the returned gRPC message.
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * <ul>
     * <li>If the argument has not been initialized an exception is thrown.</li>
     * <li>If the format attribute is one that is not supported by the Data Platform an exception is thrown.</li>
     * </ul>
     * </p>
     * 
     * @return new <code>Image</code> message populated from the argument
     * 
     * @throws UnsupportedOperationException either the image was uninitialized or the image format was unrecognized
     */
    public static Image from(BufferedImage img) throws UnsupportedOperationException {
        
        // Exception checking
        if (img.getData()==null || img.getFormat()==null)
            throw new UnsupportedOperationException("image has not be initialized.");
        
        if (img.getFormat().getProtoEnum() == null)
            throw new UnsupportedOperationException("unsupported image format " + img.getFormat().name());
        
        Image.Builder bldrImg = Image.newBuilder();
        
        Image msgImage = bldrImg.setImage(ByteString.copyFrom(img.getData()))
                                .setFileType(img.getFormat().getProtoEnum())
                                .build();
        
        return msgImage;
    }
    
    /**
     * <p>
     * Creates a new Data Platform <code>DataColumn</code> message from the given arguments.
     * </p>
     * <p>
     * The given list of objects are converted to <code>DataValue</code> Protobuf message
     * and used to populate the new gRPC message.  The name field of the message
     * is set with the given name.  The objects in the argument list must be of
     * type supported by the method <code>{@link #createDataValue(Object)}</code> otherwise
     * an exception is thrown.
     * </p>
     * 
     * @param strName   value of the name field in the return result 
     * @param lstVals   list of values used to populate the returned result
     * 
     * @return a new <code>DataColumn</code> message populated with the given arguments
     * 
     * @throws TypeNotPresentException the object argument was not one of the supported types (see {@link #createDataValue(Object)})
     * @throws ClassCastException attempted conversion of list to array to sorted map to structure failed
     * 
     * @see #toDatum(Object)
     */
    public static DataColumn createDataColumn(String strName, List<Object> lstVals) throws TypeNotPresentException, ClassCastException {
        List<DataValue> lstValMsgs = lstVals.stream().map(v -> ProtoMsg.createDataValue(v)).toList();
        
        DataColumn.Builder  bldr = DataColumn.newBuilder();
        bldr.setName(strName);
        bldr.addAllDataValues(lstValMsgs);
        
        return bldr.build();
    }

    /**
     * <p>
     * Creates and initializes a new <code>DataValue</code> Protobuf message from the 
     * given <code>Object</code> argument.
     * </p> 
     * <p>
     * Only the value union field of the new <code>DataValue</code> instance is populated.
     * Note that the status field is left <b>empty</b>!
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>
     * The argument is assumed to be either a Java scalar (i.e., a wrapped Java primitive), 
     * or one of the supported complex data types (i.e., Data Platform heterogeneous data).
     * </li> 
     * <li>
     * Currently, the following wrapped primitives are supported (along with their corresponding
     * field in <code>DataValue</code> message):
     *   <ul>
     *   <li><code>Boolean</code> -> <code>DataValue.booleanValue</code> field</li>
     *   <li><code>Short</code> -> <code>DataValue.intValue</code> field</li>
     *   <li><code>Integer</code> -> <code>DataValue.intValue</code> field</li>
     *   <li><code>Long</code> -> <code>DataValue.longValue</code> field</li>
     *   <li><code>Float</code> -> <code>DataValue.floatValue</code> field</li>
     *   <li><code>Double</code> -> <code>DataValue.doubleValue</code> field</li>
     *   <li><code>String</code> -> <code>DataValue.stringValue</code> field</li>
     *   <li><code>byte[]</code> -> <code>DataValue.byteArrayValue</code> field</li>
     *   </ul>
     * </li>
     * <li>
     * Additionally, the method also currently supports the following:
     *   <ul>
     *   <li><code>List&lt;Object&gt;</code> -> <code>DataValue.arrayValue</code> field</li>
     *   <li><code>Map&lt;String, Object&gt;</code> -> <code>DataValue.structureValue</code> field</li>
     *   <li><code>BufferedImage</code> -> <code>DataValue.imageValue</code> field</li>
     *   </ul>
     * </li>
     * <li>
     * If any other <code>Object</code> type is encountered an exception is 
     * thrown.
     * </li>
     * </ul>
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * <ul>
     * <li>
     * This method is potentially (implicitly) recursive.  If the argument contains a data structure
     * rather than a scalar it then calls the appropriate method to unpack it - as described above.
     * The method for unpacking a data structure typically re-enters this method during the process.
     * </li>
     * <li>
     * There are requirements on arguments are of type <code>List&lt;Object&gt;</code>
     * and <code>Map&lt;String, Object&gt;</code>.  See the documentation
     * on methods <code>ProtoMsg.{@link #toArray(List)}</code> and 
     * <code>ProtoMsg.{@link #toStructure(Map)}</code>, respectively.  These
     * requirements are due to unchecked casting and if not followed a cast
     * exception will be thrown.
     * </li>
     * </ul>
     * </p>
     * 
     * @param objValue value used to populate the value union of the returned <code>DataValue</code> message
     * 
     * @return <code>DataValue</code> message containing the given value
     * 
     * @throws TypeNotPresentException the argument was not one of the above types
     * @throws ClassCastException attempted conversion of list to array to sorted map to structure failed
     * 
     * @see #toArray(List)
     * @see #toStructure(Map)
     * @see SuppressWarnings
     */
    @SuppressWarnings({ "unchecked" })
    public static DataValue createDataValue(Object objValue) throws TypeNotPresentException, ClassCastException {
        DataValue.Builder bldrDatum = DataValue.newBuilder();
        
        if (objValue instanceof Boolean val) {
            bldrDatum.setBooleanValue(val);
        } else if (objValue instanceof Short val) {
            bldrDatum.setIntValue(val);
        } else if (objValue instanceof Integer val) {
            bldrDatum.setIntValue(val);
        } else if (objValue instanceof Long val) {
            bldrDatum.setLongValue(val);
        } else if (objValue instanceof Float val) {
            bldrDatum.setFloatValue(val);
        } else if (objValue instanceof Double val) {
            bldrDatum.setDoubleValue(val);
        } else if (objValue instanceof String val) {
            bldrDatum.setStringValue(val);
        } else if (objValue instanceof byte[] val) {
            bldrDatum.setByteArrayValue(ByteString.copyFrom(val));
            
        } else if (objValue instanceof List list) {
            bldrDatum.setArrayValue( ProtoMsg.createArray(list) );
        } else if (objValue instanceof Map map) {
            bldrDatum.setStructureValue( ProtoMsg.createStructure(map) );
        } else if (objValue instanceof BufferedImage img) {
            bldrDatum.setImageValue( ProtoMsg.from(img) );
            
        } else {
            throw new TypeNotPresentException("ProtoMsg#toDatum(Object): Unsupported type " + objValue.getClass().getName(), null);
        }
        return bldrDatum.build(); 
    }
    
    /**
     * <p>
     * Creates a new <code>Array</code> Protobuf message from the given list of objects.
     * </p>
     * <p>
     * The <code>{@link #from(Object)}</code> method is called to convert all
     * the objects to <code>DataValue</code> messages which are then used to build
     * the returned array.  Note that the objects in the argument list may be 
     * arrays and data structures themselves.
     * </p>
     * 
     * @param lstObjs list of objects used to populate the returned array
     * 
     * @return a new linear array gRPC message populated with the given argument data
     * 
     * @throws TypeNotPresentException encountered an unsupported type in the argument list (see {@link #createDataValue(Object)})
     * @throws ClassCastException a data structure did not have the appropriate type
     * 
     * @see ProtoMsg#from(Object)
     */
    public static Array createArray(List<Object> lstObjs) throws TypeNotPresentException, ClassCastException {
        Array.Builder bldrArr = Array.newBuilder();
        List<DataValue>   lstVals = lstObjs.stream().map( o -> ProtoMsg.createDataValue(o) ).toList();
        bldrArr.addAllDataValues(lstVals);
        
        return bldrArr.build();
    }
    
    /**
     * <p>
     * Converts the given map of (field name, field value) pairs to a 
     * <em>Datastore</em> gRPC <code>Structure</code> message.
     * </p>
     * <p>
     * Note that the map is expected to represent a data structure. Thus,
     * field values may themselves be a sorted map, that is, a substructure
     * within the overall structure.  This method calls the private method
     * <code>{@link #toFields(SortedMap)}</code> to collect all the substructure
     * fields within a field value that is a substructure. 
     * </p>
     * <p>
     * Regarding the argument, any fields values that represent substructures
     * must be implemented with a <code>Map&ltString, Object&gt;</code> 
     * interface.  This condition is how substructures are recognized.  
     * Otherwise the method
     * attempts to convert the field value directly to a <code>Datum</code> 
     * message using <code>{@link #toDatum(Object)}</code>.
     * If other sorted map types are encountered a cast exception will be 
     * thrown as this method uses unchecked type casting. 
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * <ul>
     * <li>
     * This method calls the private method {@link #buildFields(Map)} capable of processing
     * multi-level data structures through <b>recursion</b>.  The method is invoked if an <code>Object</code>
     * value in the map is of type <code>Map</code>.  Thus, although this method is not reentrant it does
     * potentially create recursion.
     * </li>
     * <li>
     * If an unrecognized type for the field value is encountered an exception
     * will be thrown (@see <code>{@link #toDatum(Object)}</code>).
     * </li>
     * </ul>
     * </p>
     *   
     * @param mapStruct a map of (field name, field value) pairs representing a data structure
     * 
     * @return new gRPC <code>Structure</code> message populated with data from the argument
     * 
     * @throws UnsupportedOperationException a field value type was not recognized
     * @throws ClassCastException a sorted map of improper generic type was encountered
     * 
     * @see #createDataValue(Object)
     * @see #buildFields(Map);
     * @see SuppressWarnings
     */
    @SuppressWarnings({ "unchecked" })
    public static Structure createStructure(Map<String, Object> mapStruct) throws UnsupportedOperationException, ClassCastException {
        
        List<Field> lstFlds = new LinkedList<>();
        
        // For each entry in the map add the value to the fields list
        for (Map.Entry<String, Object> entry : mapStruct.entrySet()) {
            String strFldNm = entry.getKey();
            Object objFldVal = entry.getValue();
            
            // Check if the value is itself a map
            // - if so call the recursive function to collect substructures
            if (objFldVal instanceof Map map) {
                List<Field> lstSubFlds = ProtoMsg.buildFields(map);

                Structure   msgStruct  = Structure.newBuilder().addAllFields(lstSubFlds).build();
                DataValue   msgStrVal  = DataValue.newBuilder().setStructureValue(msgStruct).build();
                Field       msgFld     = Field.newBuilder().setName(strFldNm).setValue(msgStrVal).build();
                lstFlds.add( msgFld );
                
            // - if not directly convert the value to a Datum
            } else {
                DataValue   msgVal = ProtoMsg.createDataValue(objFldVal);
                Field       msgFld = Field.newBuilder().setName(strFldNm).setValue( msgVal ).build();

                lstFlds.add(msgFld);
            }
        }

        Structure.Builder bldrStruct = Structure.newBuilder();
        bldrStruct.addAllFields(lstFlds);
        
        return bldrStruct.build();
    }
    
    /**
     * <p>
     * Creates a new <code>Attribute</code> message from the given arguments.
     * </p>
     * <p>
     * An <code>Attribute</code> message is a simple (name,value) pair
     * that is directly populated with the given arguments.
     * </p>
     * 
     * @param strName  attribute name 
     * @param strValue attribute value
     * 
     * @return new <code>Attribute</code> message with the given argument values
     */
    public static Attribute createAttribute(String strName, String strValue) {
        return Attribute.newBuilder()
                .setName(strName)
                .setValue(strValue)
                .build();
    }
    
    /**
     * Creates a list of <code>Attribute</code> messages from the given map of (name,value) attribute pairs.
     * 
     * @param mapAttrs collection of (name,value) attribute pairs
     * 
     * @return list of <code>Attribute</code> message populated from the given collection
     * 
     * @see #createAttribute(String, String)
     */
    public static List<Attribute> createAttributes(Map<String, String> mapAttrs) {
        return mapAttrs.entrySet()
                .stream()
                .map( e -> ProtoMsg.createAttribute(e.getKey(), e.getValue() ))
                .toList();
    }


    //
    // Protobuf Messages to Java Objects
    //
    
    
    /**
     * <p>
     * Converts a <code>DataValueType</code> Protobuf enumeration to a <code>{@link DpSupportedType}</code> 
     * enumeration.
     * </p>
     * <p>
     * This mapping is NOT one-to-one (injective), but it is onto (surjective).  Specifically, there are more
     * Protobuf primitive types than Java primitive types.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * <ul>
     * <li>Unsigned Protobuf integral types get mapped to the Java unsigned equivalent.</li>
     * <li>All Data Platform structured types are part of the <code>DpSupportedType</code> enumeration.</li>
     * <li>In the case where the argument equals <code>{@link DataValueType#UNRECOGNIZED}</code> the returned 
     *     value is <code>{@link DpSupportedType#UNSUPPORTED_TYPE}</code>.
     * </ul>
     * </p>
     * 
     * @param msgValueType  Protobuf supported data type enumeration
     * @return              Data Platform API supported data type enumeration
     * 
     * @throws  TypeNotPresentException    the argument had an recognized type
     * 
     * @see DpSupportedType
     */
    public static DpSupportedType   toDpSupportedType(DataValueType msgValueType) throws TypeNotPresentException {
        
        return switch (msgValueType) {
        case DATA_TYPE_STRING -> DpSupportedType.STRING;
        case DATA_TYPE_BOOLEAN -> DpSupportedType.BOOLEAN;
        case DATA_TYPE_UINT -> DpSupportedType.INTEGER;
        case DATA_TYPE_ULONG -> DpSupportedType.LONG;
        case DATA_TYPE_INT -> DpSupportedType.INTEGER;
        case DATA_TYPE_LONG -> DpSupportedType.LONG;
        case DATA_TYPE_FLOAT -> DpSupportedType.FLOAT;
        case DATA_TYPE_DOUBLE -> DpSupportedType.DOUBLE;
        case DATA_TYPE_BYTES -> DpSupportedType.BYTE_ARRAY;
        case DATA_TYPE_ARRAY -> DpSupportedType.ARRAY;
        case DATA_TYPE_STRUCT -> DpSupportedType.STRUCTURE;
        case DATA_TYPE_IMAGE -> DpSupportedType.IMAGE;
        case UNRECOGNIZED -> DpSupportedType.UNSUPPORTED_TYPE; // throw new UnsupportedOperationException("Unimplemented case: " + msgValueType);
        default -> throw new IllegalArgumentException("Unexpected value: " + msgValueType);
        };
    }
    
    /**
     * <p>
     * Converts the given <code>Timestamp</code> message to a Long Unix epoch value in nanoseconds.
     * </p>
     * <p>
     * The returned value is created by multiplying the epoch seconds field by
     * 10<sup>9</sup> and adding the epoch nanoseconds field to the result.
     * </p>
     *
     * @param tms <code>Timestamp</code> message
     * 
     * @return a long Unix epoch value in nanoseconds
     */
    public static Long toLong(Timestamp tms) {
        return tms.getEpochSeconds() * 1_000_000_000L + tms.getNanoseconds();
    }
    
    /**
     * Converts the given <code>Timestamp</code> message to a Java <code>Instant</code> object.
     * 
     * @param msgTms   Data Platform <code>Timestamp</code> message
     * 
     * @return  equivalent Java instant
     * 
     * @throws DateTimeException instant exceeds the maximum or minimum instant
     * @throws ArithmeticException arithmetic overflow occurred
     */
    public static Instant toInstant(Timestamp msgTms) throws DateTimeException, ArithmeticException {
        Instant ins = Instant.ofEpochSecond(msgTms.getEpochSeconds(), msgTms.getNanoseconds());
        
        return ins;
    }
    
    /**
     * Converts the <code>TimestampList</code> message into a list of Java <code>Instant</code> objects.
     * 
     * @param msgTmsLst Data Platform <code>TimestampList</code> message
     * 
     * @return list of equivalent Java <code>Instant</code> objects
     * 
     * @throws DateTimeException an instant exceeds the maximum or minimum instant
     * @throws ArithmeticException arithmetic overflow occurred
     */
    public static List<Instant> toInstantList(TimestampList msgTmsLst) throws DateTimeException, ArithmeticException {
        List<Timestamp> lstTms = msgTmsLst.getTimestampsList();
        List<Instant>   lstIns = lstTms.stream().map( t -> ProtoMsg.toInstant(t) ).toList();
        
        return lstIns;
    }
    
    /**
     * <p>
     * Creates a new <code>BufferedImage</code> instance populated from the given <code>Image</code> message.
     * </p>
     * <p>
     * The <code>Image</code> Protobuf message contains only a format and a 
     * byte array (i.e., <code>byte[]</code>) containing the image data.
     * Thus, neither the name or the dimensions attributes of the returned 
     * image are set.  The timestamp of the image is set to the instant of
     * this method call.
     * </p>
     * 
     * @param msgImage <code>Image</code> Protobuf message containing image file data
     * 
     * @return new <code>BufferedImage</code> populated with the format and data of the argument
     */
    public static BufferedImage toBufferedImage(Image msgImage) {
        FileType   msgFType = msgImage.getFileType();
        ByteString bsData = msgImage.getImage();
        
        Format enmFmt = Format.from(msgFType);
        byte[] arrData = bsData.toByteArray();
        
        return BufferedImage.from(null, Instant.now(), enmFmt, null, arrData);
    }
    
    /**
     * <p>
     * Create a new, initialized instance of <code>UniformSamplingClock</code> from the given
     * Protobuf message representing a uniform sampling interval.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The <code>{@link UniformSamplingClock}</code> class is essentially equivalent in defining
     * attributes as the Protobuf <code>{@link SamplingClock}</code> message, however it is more
     * general and offers other features such as timestamp generation.
     * </p>
     * 
     * @param msgClock    Protobuf message representing a finite-duration uniform sampling clock
     * 
     * @return  new <code>UniformSamplingClock</code> instance initialized from the argument
     */
    public static UniformSamplingClock    toUniformSamplingClock(SamplingClock msgClock) {
        Instant insStart = ProtoMsg.toInstant(msgClock.getStartTime());
        int     intCount = msgClock.getCount();
        long    lngPeriod = msgClock.getPeriodNanos();
        
        return new UniformSamplingClock(insStart, intCount, lngPeriod, ChronoUnit.NANOS);
    }
    
    /**
     * <p>
     * Creates a new <code>{@link PvMetaRecord}</code> populated from the given Query Service metadata query 
     * response message.
     * </p>
     * <p>
     * Converts the given process variable metadata message into an API metadata record by extracting all 
     * message fields and converting them into API resources. 
     * </p>
     * <p>
     * <h2>TODO</h2>
     * <ul>
     * <li>
     * <b>Data Type</b> - The data type in the <code>PvInfo</code> message is specified as a string and must be 
     * converted to a corresponding <code>DpSupportedType</code> enumeration.  Here we assume the string value is 
     * the name of the <code>{@link DataValueType}</code> enumeration in <code>common.proto</code>.  The current
     * condition is brittle and a more robust solution should be implemented.
     * </li>
     * <br/>
     * <li>
     * <b>Data Type</b> - Currently everything is kluged because the Query Service returns a
     * string with value <code>DOUBLE</code> (all types are double right now).  Thus, this is
     * not even a constant name within enumeration <code>{@link DataValueType}</code>.  
     * So the kluge is to append the string "DATA_TYPE_" to the returned value.
     * </li>
     * <br/>
     * <li>
     * <b>Non-Uniform Sampling</b> - If the process variable had explicit timestamps, rather than a sampling
     * clock, only the <code>startTime</code> field of the <code>PvInfo</code> message is set.  This is not
     * particularly robust and should be addressed in the future.  The method here responds by setting the
     * <code>lastClock</code> record attribute to <code>null</code> 
     * </li>
     * </ul>
     * </p>
     * 
     * @param msgPvInfo Query Service process variable metadata response message
     * 
     * @return  equivalent API process variable metadata record
     * 
     * @throws IllegalArgumentException data type name in the message was not a <code>DataValueType</code> enumeration
     * @throws TypeNotPresentException  data type in message does not map to <code>DpSupportedType</code> 
     */
    public static PvMetaRecord  toPvMetaRecord(QueryMetadataResponse.MetadataResult.PvInfo msgPvInfo) 
            throws IllegalArgumentException, TypeNotPresentException {
        
//        // Check the type value string
//        String          strType = msgPvInfo.getLastBucketDataType();
//        boolean         bolValidType = false;
//        for (DataValueType msgType : DataValueType.values()) {
//            if ( strType.equals(msgType.name()) ) {
//                    bolValidType = true;
//                    break;
//            }
//        }
//        if (!bolValidType)
//            throw new IllegalArgumentException("Last PV data type " + strType + " is NOT a DataValueType enumeration name.");
        
        // Extract the name and timestamps
        String      strName = msgPvInfo.getPvName();
        Instant     insFirst = ProtoMsg.toInstant(msgPvInfo.getFirstTimestamp());
        Instant     insLast = ProtoMsg.toInstant(msgPvInfo.getLastTimestamp());

        // Extract the type value
        String          strType = msgPvInfo.getLastBucketDataType();
        
        // TODO - fix this kluge!
        strType = "DATA_TYPE_" + strType;
        // ------------------------
        
        DataValueType   msgType = DataValueType.valueOf(DataValueType.class, strType);  // throws IllegalArgumentException
        DpSupportedType enmType = ProtoMsg.toDpSupportedType(msgType);                    // throws TypeNotPresentException

        // Extract the sampling clock
        SamplingClock           msgClock = msgPvInfo.getLastSamplingClock();
        UniformSamplingClock    apiClock;
        
        if (msgClock.getCount() > 0)
            apiClock = ProtoMsg.toUniformSamplingClock(msgClock);
        else
            apiClock = null;
        
        // Create metadata record
        PvMetaRecord    recPvInfo = new PvMetaRecord(strName, enmType, apiClock, insFirst, insLast);
        
        return recPvInfo;
    }
    
    
    
    //
    // Protobuf Message Field Extraction
    //
    
    /**
     * <p>
     * Extracts the data type of the column values AND checks for uniformity.
     * </p>
     * <p>
     * The data type of the first column data value is extracted.  The data types of all column data
     * is then checked for uniformity against the first type.  If the column values are not all
     * of the same type an exception is thrown.  Otherwise, the data type of the first value is
     * returned, which is confirmed for all data values.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * If a data value is within the argument is empty, it is skipped during the uniformity check.
     * </p>
     * 
     * @param msgDataCal   Data Platform <code>DataColumn</code> message containing data
     * 
     * @return  The uniform data type of all data contained within the argument
     * 
     * @throws MissingResourceException   the argument was empty (contained no data)
     * @throws IllegalStateException      the column data was of non-uniform type
     * @throws TypeNotPresentException    an unsupported data type was encountered
     */
    public static DpSupportedType extractType(DataColumn msgDataCol) throws MissingResourceException, IllegalStateException, TypeNotPresentException {
        List<DataValue> lstVals = msgDataCol.getDataValuesList();
        
        // Check the argument
        if (lstVals.isEmpty())
            throw new MissingResourceException("The data column was empty.", msgDataCol.getName(), "getDataValueList()");
        
        // Get type of first data value
        DataValue   msgValFirst = lstVals.get(0);
        DpSupportedType enmType = ProtoMsg.extractType(msgValFirst);
        
        // Check all column value types
        boolean bolSameType = true;
        
        for (DataValue val : lstVals)
            try {
                DpSupportedType enmTypeVal = ProtoMsg.extractType(val);
                
                if (enmTypeVal != enmType)
                    bolSameType = false;
                
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
        
        if (!bolSameType)
            throw new IllegalStateException("The data column values were not all of the same type.");
        
        // Return the type of the first value if all value type are equal
        return enmType;
    }
    
    /**
     * <p>
     * Extracts the values from the given <code>DataColumn</code> message and returns them
     * as an ordered list of Java <code>Objects</code>.
     * </p>
     * <p>
     * Creates a Java ordered <code>List</code> of <code>Object</code> instances from the
     * given Data Platform <code>DataColumn</code> message.  The list of 
     * Protobuf <code>DataValue</code> messages are extracted and converted to <code>Object</code>
     * instances using the <code>{@link #extractValue(DataValue)}</code> method.
     * </p>
     *  
     * @param msgDataCol   Data Platform <code>DataColumn</code> message containing data
     * 
     * @return  a Java ordered <code>List</code> containing the data of the given argument
     * 
     * @throws TypeNotPresentException an unsupported type was encountered (see message)
     * 
     * @see #extractValue(DataValue)
     */
    public static List<Object> extractValues(DataColumn msgDataCol) throws TypeNotPresentException {
        List<DataValue> lstMsgVals = msgDataCol.getDataValuesList();
        List<Object>    lstObjVals = lstMsgVals.stream().map( ProtoMsg::extractValue ).toList();
        
        return lstObjVals;
    }
    
    /**
     * <p>
     * Extracts the values from the given <code>DataColumn</code> message and returns them
     * as an ordered list of Java <code>Objects</code> with type <code>T</code>.
     * </p>
     * <p>
     * Creates a Java ordered <code>List</code> of <code>T</code> instances from the
     * given Data Platform <code>DataColumn</code> message.  The list of 
     * Protobuf <code>DataValue</code> messages are extracted and converted to <code>T</code>
     * instances using the <code>{@link #extractValueAs(Class, DataValue)}</code> method.
     * </p>
     * 
     * @param <T>       type of the heterogeneous column values and the returned values
     * 
     * @param clsVal        class type of the returned value
     * @param msgDataCol    Data Platform <code>DataColumn</code> message containing data
     * 
     * @return  ordered list of data values within argument converted to type <code>T</code>
     * 
     * @throws TypeNotPresentException  an unsupported type was encountered
     * @throws ClassCastException       the message data value could not be cast to the given type
     * 
     * @see #extractValueAs(Class, DataValue)
     */
    @AAdvancedApi(status=AAdvancedApi.STATUS.TESTED_BETA, note="Calls extractValuesAs(Class<T>, DataValue).")
    public static <T extends Object> List<T> extractValuesAs(Class<T> clsType, DataColumn msgDataCol) 
        throws TypeNotPresentException, ClassCastException {
        List<DataValue> lstMsgVals = msgDataCol.getDataValuesList();
        List<T>         lstVals = lstMsgVals.stream().map(msgVal -> ProtoMsg.extractValueAs(clsType, msgVal)).toList();
        
        return lstVals;
    }
    
    /**
     * <p>
     * Extracts the data values from the given <code>Array</code> message and returns them
     * as a vector of Java objects.
     * </p>
     * <p> 
     * The underlying data value of each contained <code>DataValue</code> message is extracted in order
     * using <code>{@link #extractValue(DataValue)}</code>.  The resulting Java objects are used to
     * create an order list which is then converted into the returned vector.
     * </p>
     * 
     * @param msgArray  <code>Array</code> message whose values are extracted from its <code>DataValue</code> messages 
     * 
     * @return  Java <code>Vector</code> of extracted message data
     * 
     * @throws  TypeNotPresentException unsupported data type encountered
     * @see #extractValue(DataValue)
     */
    public static Vector<Object> extractValues(Array msgArray) throws TypeNotPresentException {
        List<DataValue> lstVals = msgArray.getDataValuesList();
        List<Object>    lstObjs = lstVals.stream().map(ProtoMsg::extractValue).toList();
        Vector<Object>  vecObjs = new Vector<>(lstObjs);
        return vecObjs;
    }
    
    /**
     * <p>
     * Extracts the field names and data values from the given <code>Structure</code> message and
     * returns them as a Java <code>Map</code> preserving the underlying data structure.
     * </p>
     * <p>
     * The fields of the structure, which are <code>Field</code> message objects, are parsed. 
     * The fields of the <code>Field</code> messages are a name string and a <code>DataValue</code>
     * message.  The data value of the <code>DataValue</code> instances are converted
     * to Java <code>Object</code> instances using the <code>{@link ProtoMsg#extractValue(DataValue)}</code>
     * method.
     * </p>
     * <p>
     * Note that the field value of a gRPC <code>Structure</code> object may itself be a
     * <code>Structure</code> object.  Thus, this method is re-entrant threw method 
     * {@link #extractValue(DataValue)}.
     * </p>
     *  
     * <h2>NOTES:</h2>
     * <ul>
     * <li>This method may be called recursively by the <code>{@link #extractValue(DataValue)}</code>
     * method which may return a <code>Map</code> corresponding to a field which 
     * itself is a <code>Structure</code> message.</li>
     * <br/>
     * <li> Note also that <code>{@link #extractValue(DataValue)}</code> makes use of the recursive 
     * function <code>{@link #getStructureFieldOrDatum(DataValue)}</code>.
     * </li>
     * <br/>
     * <li>The effect is to drill down into the structure fields until a <code>DataValue</code>
     * object is found.
     * </li>
     * <br/>
     * <li>Thus, the returned map may contain value objects which are themselves <code>Map</code>
     * instances.</li>
     * <br/>
     * <li>Thus, for proper processing the map value objects should be checked for type
     * in order to determine if they are field representing substructures of the original 
     * structure. </li>
     * </ul>
     * </p>
     * 
     * @param msgStruct the <code>Structure</code> message to be parsed 
     * 
     * @return a <code>Map</code> where the keys are the structure field names and
     *         the values are the extracted data values from the <code>Field</code> value fields
     *         
     * @throws TypeNotPresentException an unsupported type was encountered (see message) 
     */
    public static Map<String, Object> extractValues(Structure msgStruct) throws TypeNotPresentException {
        List<Field>         lstFlds = msgStruct.getFieldsList();
        Map<String, Object> map = lstFlds.stream().collect(Collectors.toMap(Field::getName, f -> ProtoMsg.extractValue(f.getValue())));

        return map;
    }
    
    /**
     * <p>
     * Extracts the data type of the value field from the <code>DataValue</code> argument and returns it
     * as a <code>DpSupportedType</code> enumeration constant.
     * </p>
     * <p>
     * <h2>Mappings</h2>
     * The following mappings are used for the <code>{@link DataValue#getValueCase()}</code> type 
     * enumeration, along with the corresponding Protobuf type (primitive or message):
     * <ul>
     * <code>
     * <li>bool: BOOLEANVALUE -> DpSupportedType.BOOLEAN</li>
     * <li>uint32: UINTVALUE -> DpSupportedType.INTEGER</li>
     * <li>sint32: INTVALUE -> DpSupportedType.INTEGER</li>
     * <li>uint64: ULONGVALUE -> DpSupportedType.LONG</li>
     * <li>sint64: LONGVALUE -> DpSupportedType.LONG</li>
     * <li>float: FLOATVALUE -> DpSupportedType.FLOAT</li>
     * <li>double: DOUBLEVALUE -> DpSupportedType.DOUBLE</li>
     * <li>string: STRINGVALUE -> DpSupportedType.STRING</li>
     * <li>bytes: BYTEARRAYVALUE -> DpSupportedType.BYTE_ARRAY</li>
     * <li>Array: ARRAYVALUE -> DpSupportedType.ARRAY</li>
     * <li>Structure: STRUCTUREVALUE -> DpSupportedType.STRUCTURE</li>
     * <li>Image: IMAGEVALUE -> DpSupportedType.IMAGE</li>
     * <li>null: VALUE_NOT_SET -> IllegalArgumentException</li>
     * </code>
     * </ul>
     *  
     * @param msgDataValue <code>DataValue</code> message whose value type is to be extracted
     *  
     * @return  <code>DpSupportedType</code> representation of the message value data type
     * 
     * @throws IllegalArgumentException the data value field was not set (empty)
     * @throws TypeNotPresentException  an unrecognized data type was encountered
     */
    public static DpSupportedType   extractType(DataValue msgDataValue) throws IllegalArgumentException, TypeNotPresentException {
        
        return switch (msgDataValue.getValueCase()) {
        case BOOLEANVALUE -> DpSupportedType.BOOLEAN;
        case UINTVALUE -> DpSupportedType.INTEGER;
        case INTVALUE -> DpSupportedType.INTEGER;
        case ULONGVALUE -> DpSupportedType.LONG;
        case LONGVALUE -> DpSupportedType.LONG;
        case FLOATVALUE -> DpSupportedType.FLOAT;
        case DOUBLEVALUE -> DpSupportedType.DOUBLE;
        case STRINGVALUE -> DpSupportedType.STRING;
        case BYTEARRAYVALUE -> DpSupportedType.BYTE_ARRAY;
        case ARRAYVALUE -> DpSupportedType.ARRAY;
        case STRUCTUREVALUE -> DpSupportedType.STRUCTURE;
        case IMAGEVALUE -> DpSupportedType.IMAGE;
        case VALUE_NOT_SET -> throw new IllegalArgumentException("The data value was not set.");
        default -> throw new TypeNotPresentException("The data value type was not recognized.", null);
        };
    }
    
    /**
     * <p>
     * Extracts the value field from a <code>DataValue</code> instance and return as Java <code>Object</code>.
     * </p>
     * <p>
     * The returned object is cast to an appropriate Java language or library type according to
     * the underlying type of the heterogeneous data value.  Thus, the object is one of the following:
     * <ul>
     * <li><code>null</code> value - the value union was not set.
     * <li><s>Throws <code>IllegalArgumentException</code> - the value union was not set.</s></li>
     * <li>a wrapped Java primitive - the value union contains a scalar</li>
     * <li>a populated Java container - the value union contains an array or structure</li>
     * <li>an <code>BufferedImage</code> object - the value union contains an image</li>
     * </ul>
     * </p>
     * <p>
     * For further details on the Java object mappings used here see <code>{@link #extractType(DataValue)}</code>
     * and <code>{@link DpSupportedType}</code>.
     * </p>
     * 
     * @param msgDataValue <code>DataValue</code> message whose value union is to be extracted
     *  
     * @return  Java object representation of the message value
     * 
     * throws IllegalArgumentException  the data value field was not set (empty)
     * @throws TypeNotPresentException  an unsupported type was encountered
     * 
     * @see #extractType(DataValue)
     * @see DpSupportedType
     */
    public static Object extractValue(DataValue msgDataValue) throws /* IllegalArgumentException, */ TypeNotPresentException {
        
        return switch (msgDataValue.getValueCase()) {
            case BOOLEANVALUE -> Boolean.valueOf( msgDataValue.getBooleanValue() );
            case UINTVALUE -> Integer.valueOf( msgDataValue.getUintValue() );
            case INTVALUE -> Integer.valueOf( msgDataValue.getIntValue() );
            case ULONGVALUE -> Long.valueOf( msgDataValue.getUlongValue() );
            case LONGVALUE -> Long.valueOf( msgDataValue.getLongValue() );
            case FLOATVALUE -> Float.valueOf( msgDataValue.getFloatValue() );
            case DOUBLEVALUE -> Double.valueOf( msgDataValue.getDoubleValue() );
            case STRINGVALUE -> msgDataValue.getStringValue();
            case BYTEARRAYVALUE -> msgDataValue.getByteArrayValue().toByteArray();
            case ARRAYVALUE -> ProtoMsg.extractValues(msgDataValue.getArrayValue());
            case STRUCTUREVALUE -> ProtoMsg.extractValues(msgDataValue.getStructureValue());
            case IMAGEVALUE -> ProtoMsg.toBufferedImage( msgDataValue.getImageValue() );
            case VALUE_NOT_SET -> null;
//                throw new IllegalArgumentException("The data value was not set.");
            default ->
                throw new TypeNotPresentException("The data value was not unrecognized - value " + msgDataValue.getDescriptorForType(), null);
        };
    }

    /**
     * <p>
     * Extracts the value of the given <code>DataValue</code> message as a known type.
     * </p>
     * <p>
     * The type of the heterogeneous data value is assumed to be known and given by the <code>Class&lt;T&gt;</code>
     * argument.  The list of currently supported types for parameter <code>&lt;T&gt;</code> is as follows:
     * <ul>
     * <li><code>java.lang.String</code></li>
     * <li><code>java.lang.Boolean</code></li>
     * <li><code>java.lang.Integer</code></li> 
     * <li><code>java.lang.Long</code></li>
     * <li><code>java.lang.Float</code></li>
     * <li><code>java.lang.Double</code></li>
     * <li><code>com.google.Protobuf.ByteString</code></li>
     * <li><code>Array</code> (Protobuf message)</li>
     * <li><code>Structure (Protobuf message)</code></li>
     * <li><code>Image (Protobuf message)</code></li>
     * </ul>
     * <b>NOTES</b>: 
     * <ul>
     * <li>The generic parameter <code>T</code> may include all native types listed above.</li>
     * <li>The generic parameter <code>T</code> may include Protobuf message listed above.</li>
     * <li>Unsigned integral Protobuf types are converted to signed Java types.</li> 
     * <li>Numeric accuracy may be lost during casts (due to sign).</li>  
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * This method is experimental and should be used with caution.  
     * <ul>
     * <li><s>If the incorrect type is specified the returned value will be empty (<code>null</code>)</s>.</li>
     * <li><s>If the message data field is empty (not set) an exception is thrown. </s></li>
     * <li>If the message data field is empty a <code>null</code> value is returned. </li>
     * <li>If the type supplied is not supported an exception is thrown.</li>
     * <li>If the heterogeneous value cannot be cast to the given type an exception is thrown.</li>
     * <li>Heterogeneous values can loose numerical accuracy if cast to smaller types.</li>
     * </ul> 
     * 
     * @param <T>       type of the heterogeneous data value and the returned value
     * 
     * @param clsVal    class type of the returned value
     * @param msgVal    Data Platform <code>DataValue</code> message
     * 
     * @return          extracted data value of the given message cast to the given type
     * 
     * throws IllegalArgumentException  the data value field was not set (empty)
     * @throws TypeNotPresentException  an unsupported type was encountered
     * @throws ClassCastException       the message data value could not be cast to the given type
     */
    @AAdvancedApi(status=AAdvancedApi.STATUS.TESTED_ALPHA, note="Passes through if-then cases")
    public static <T extends Object> T extractValueAs(Class<T> clsVal, DataValue msgVal) throws TypeNotPresentException, ClassCastException {
        
        if (msgVal.getValueCase() == ValueCase.VALUE_NOT_SET)
//            throw new IllegalArgumentException("The data value was not set.");
            return null;
        
        Object obj;
        if (clsVal.equals(String.class))        
            obj = msgVal.getStringValue();
        else if (clsVal.equals(Boolean.class))
            obj =  msgVal.getBooleanValue();
        else if (clsVal.equals(Integer.class)) {
            if (msgVal.getValueCase() == ValueCase.INTVALUE)
                obj = msgVal.getIntValue();
            else if (msgVal.getValueCase() == ValueCase.UINTVALUE)
                obj = msgVal.getUintValue();
            else
                throw new ClassCastException("Attempt to cast non integer value to java.lang.Integer.");
        }
        else if (clsVal.equals(Long.class)) {
            if (msgVal.getValueCase() == ValueCase.LONGVALUE)
                obj = msgVal.getLongValue();
            else if (msgVal.getValueCase() == ValueCase.ULONGVALUE)
                obj = msgVal.getUlongValue();
            else
                throw new ClassCastException("Attempt to cast non long integer value to java.lang.Long.");
        }
        else if (clsVal.equals(Float.class))
            obj = msgVal.getFloatValue();
        else if (clsVal.equals(Double.class))
            obj = msgVal.getDoubleValue();
        else if (clsVal.equals(ByteString.class))
            obj = msgVal.getByteArrayValue();
        else if (clsVal.equals(Array.class))
            obj = msgVal.getArrayValue();
        else if (clsVal.equals(Structure.class))
            obj = msgVal.getStructureValue();
        else if (clsVal.equals(Image.class))
            obj = msgVal.getImageValue();
        else
            throw new TypeNotPresentException("Unsupported class type " + clsVal.getName(), null);
        
        return clsVal.cast(obj);
    }
    
    
    //
    // Debugging
    //
    
    /**
     * <p>
     * Debugging utility for printing out some of the contents of the given Data Platform
     * <code>DataTable</code> instance to a string.
     * </p>
     * <p>  
     * If the argument is <code>null</code> then a string indicating as such is returned. 
     * </p>  
     * 
     * @param msgTbl   Data Platform <code>DataTable</code> instance
     * 
     * @return string representation of (some of) the response contents
     */
    public static String printout(QueryTableResponse.TableResult msgTbl) {
        if (msgTbl == null) 
            return "The DataTable is null";
        
        StringBuffer    buf = new StringBuffer();
        
        List<DataColumn>      lstData     = msgTbl.getDataColumnsList();

        buf.append("ColumnsList: (size = " + lstData.size() + ") \n");
        
        for (DataColumn data : lstData) {
            String      strName  = data.getName();
            List<DataValue> lstDatum = data.getDataValuesList();
            buf.append("  Data: Name=" + strName + ", getDataList.size()=" + lstDatum.size() + ", getDataList.get(0)=" + lstDatum.get(0));

            DataValue datum    = data.getDataValues(0);
            Map<FieldDescriptor, Object> mapFlds = datum.getAllFields();
            buf.append("    DataValue = getDataList().get(0): \n");
            buf.append("       Datum.getAllFields()= {" );
            mapFlds.forEach( (k,v) -> buf.append("(FieldDescriptor=" + k.toString() + ", Value=" + v.toString() +")"));
            buf.append("}\n");

        }
        
        return buf.toString();
    }
    
    
    // 
    // Private Support Methods
    //
    
    /**
     * <p>
     * Converts the given map of (name, value) pairs to a list of <code>Field</code> message.
     * </p>
     * <p>
     * Note that the argument map is expected to represent a data structure. Thus,
     * field values may themselves be a map, that is, a substructure
     * within the overall structure.  This method is recursive and continues to
     * call itself until it finds field values that are not sorted maps.
     * </p>
     * <p>
     * Regarding the argument, any fields values that represent substructures
     * must be implemented with a <code>Map&ltString, Object&gt;</code> 
     * interface.  This condition is how substructures are recognized.  
     * Otherwise the method
     * attempts to convert the field value directly to a <code>DataValue</code> message
     * using <code>{@link #createDataValue(Object)}</code>.
     * If other map types are encountered a cast exception will be 
     * thrown as this method uses unchecked type casting. 
     * </p>
     * <p>
     * <h2>WARNINGS:</h2>
     * <ul>
     * <li>
     * As described above this method is recursive for multi-level data structures.  The number
     * of recursions is equal to the depth of the original data structure presented to this method.
     * <li>
     * If an unrecognized type for the field value is encountered an exception
     * will be thrown (@see <code>{@link #createDataValue(Object)}</code>).
     * </li>
     * </ul>
     * </p>
     *   
     * @param mapStruct a map of (field name, field value) pairs representing a data structure
     * 
     * @return a list of new <code>Field</code> messages populated with data from the argument
     * 
     * @throws UnsupportedOperationException a field value type was not recognized
     * @throws ClassCastException a sorted map of improper generic type was encountered
     * 
     * @see #createDataValue(Object)
     * @see SuppressWarnings
     */
    @SuppressWarnings({ "unchecked" })
    private static List<Field> buildFields(Map<String, Object> mapStruct) throws UnsupportedOperationException, ClassCastException {
        
        List<Field> lstFlds = new LinkedList<>();
        for (Map.Entry<String, Object> entry : mapStruct.entrySet()) {
            String strFldNm = entry.getKey();
            Object objFldVal = entry.getValue();
            
            if (objFldVal instanceof Map map) {
                List<Field> lstSubFlds = ProtoMsg.buildFields(map);

                Structure   msgStruct  = Structure.newBuilder().addAllFields(lstSubFlds).build();
                DataValue   msgStrVal  = DataValue.newBuilder().setStructureValue(msgStruct).build();
                Field       msgFld     = Field.newBuilder().setName(strFldNm).setValue(msgStrVal).build();
                lstFlds.add(msgFld);
                
            } else {
                
                DataValue   msgVal = ProtoMsg.createDataValue(objFldVal);
                Field       msgFld = Field.newBuilder().setName(strFldNm).setValue(msgVal).build();
                
                lstFlds.add( msgFld );
            }
        }
        
        return lstFlds;
    }
    
    
    /**
     * Prevent creation of an instance of <code>ProtoMsg</code>.
     *
     */
    private ProtoMsg() {
    }

}
