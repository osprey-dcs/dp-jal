/*
 * Project: dp-api-common
 * File:	IngestionFrameConverter.java
 * Package: com.ospreydcs.dp.api.ingest.model.frame
 * Type: 	IngestionFrameConverter
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
 * @since Apr 16, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.frame;

import java.security.ProviderException;
import java.util.MissingResourceException;
import java.util.UUID;

import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.grpc.util.ProtoTime;
import com.ospreydcs.dp.api.ingest.IngestionFrame;
import com.ospreydcs.dp.api.model.IngestRequestUID;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.common.EventMetadata;
import com.ospreydcs.dp.grpc.v1.ingestion.IngestDataRequest;

/**
 * <p>
 * Utility class for converting <code>IngestionFrame</code> instances into Ingestion Service
 * <code>IngestDataRequest</code> messages.
 * </p>
 * <p>
 * Class instances convert the unit of ingestion for the Ingestion Service client API, i.e., the
 * <code>IngestionFrame</code> class object, to the unit of ingestion for the Ingestion Serivice
 * gRPC interface, i.e., the <code>IngestDataRequest</code> message.
 * All data, timestamps, attributes, and other metadata within an <code>IngestionFrame</code>
 * are transferred to a newly created <code>IngestDataRequest</code> message.
 * </p>
 * <p>
 * <h2>Client Request IDs</h2>
 * If no client request identifer is provided a <code>IngestionFrameConverter</code> instance
 * will create one for the new ingestion request message.
 * The client request identifier is "unique" within the lifetime of the current
 * Java Virtual Machine (JVM).  Request identifiers can, potentially, be repeated for
 * independent JVMs and should not be used to uniquely identify request beyond the
 * current JVM execution.
 * </p>
 * <p>
 * <h2>gRPC Message Size Limits</h2>
 * Class instances do no data processing, only data conversion.  Specifically, a large ingestion
 * frame will be converted to an equally large, single ingestion data request message.  
 * That is, the ingestion frame is converted regardless of any gRPC message size transmission 
 * limitation.  If ingestion frame sizes are comparable to the gRPC message size limitation
 * see <code>{@link IngestionFrameProcessorDeprecated}</code>.  Ingestion frame larger than the current
 * gRPC message size limitation should be processed before offering to this class.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li></li>
 * </ul>
 * </p>
 *  
 *
 * @author Christopher K. Allen
 * @since Apr 16, 2024
 *
 */
public final class IngestionFrameConverter {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new instance of <code>IngestionFrameConverter</code> ready for
     * converting ingestion frames to Ingestion Service ingest data request messages.
     * </p>
     * 
     * @return  new ingestion frame converter ready for use
     */
    public static IngestionFrameConverter   create() {
        return new IngestionFrameConverter();
    }
    /**
     * <p>
     * Creates and returns a new instance of <code>IngestionFrameConverter</code> ready for
     * converting ingestion frames to Ingestion Service ingest data request messages.
     * </p>
     *  
     * @param recProviderId     the default data-provider UID used when none is provided or available within frame
     * 
     * @return  new ingestion frame converter ready for use
     */
    public static IngestionFrameConverter create(ProviderUID recProviderId) {
        return new IngestionFrameConverter(recProviderId);
    }
    
    
    //
    // Class Constants
    //
    
    /** The name used for the name-based UUID client request identifier for each message (required by Ingestion Service) */
    public static final String      STR_UUID_NAME = "JavaClientApiLibrary-IngestionFrameConverter";
    
    
    //
    // Class Resources
    //
    
//    /** The locking object for synchronizing access to class resources */ 
//    private static final Object     objClassLock = new Object();
    
    /** The number of ingestion frames converted - used for request ID creation */
    private static long             cntFrames = 0L;

    
    //
    // Defining Attributes
    //
    
    /** The default data provider unique identifier - used when one is not provided or available within the frame */
    private final ProviderUID       recProviderUidDef;
    

    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionFrameConverter</code>.
     * </p>
     *
     */
    public IngestionFrameConverter() {
        this.recProviderUidDef = null;
    }
    
    /**
     * <p>
     * Constructs a new instance of <code>IngestionFrameConverter</code> with a default provider UID.
     * </p>
     *
     * @param recDefProviderId  default Data Provider UID to use when none is provided
     */
    public IngestionFrameConverter(ProviderUID recDefProviderId) {
        this.recProviderUidDef = recDefProviderId;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Converts the given ingestion frame to a new, <code>IngestDataRequest</code> message populated
     * with the given argument data.
     * </p>
     * <p>
     * The method attempt to recover the Data Provider UID by first inspecting the given argument
     * with <code>{@link IngestionFrame#getProviderUid()}</code>.  If a <code>null</code> value is
     * returned then the default value provided at construction is checked.  If no default value
     * was provided then an exception is thrown.
     * </p>
     * <p>
     * Once the Data Provider UID is obtained then the method defers to
     * <code>{@link #createRequest(IngestionFrame, ProviderUID)}</code>.
     * </p>
     * 
     * @param frame source of all data used to populated returned message
     * 
     * @return  new <code>IngestDataRequest</code> message populated with argument data
     * 
     * @throws ProviderException        Data Provider UID available (within the frame or default defined)
     * @throws MissingResourceException the argument had no timestamp assignments
     * @throws TypeNotPresentException  an unsupported data type was contained in the arugment data
     * @throws ClassCastException       bad type cast or structured data within argument was not converted
     * 
     * @see #createRequest(IngestionFrame, int)
     */
    public IngestDataRequest    createRequest(IngestionFrame frame) 
            throws ProviderException, MissingResourceException, TypeNotPresentException, ClassCastException  {
     
        // Recover the provider UID
        ProviderUID     recProviderUid;
        
        //  1st Look in the ingestion frame
        if (frame.getProviderUid() != null)
            recProviderUid = frame.getProviderUid();
        
        //  2nd look for a default value (set at construction)
        else if (this.recProviderUidDef != null)
            recProviderUid = this.recProviderUidDef;
        
        //  3rd bail out
        else 
            throw new ProviderException(JavaRuntime.getQualifiedMethodNameSimple() + " - no Data Provider UID was available.");
        
        
        // Create the message and return it
        IngestDataRequest    msgRqst = this.createRequest(frame, recProviderUid);
        
        return msgRqst;
    }
    
    /**
     * <p>
     * Converts the given ingestion frame to a new, <code>IngestDataRequest</code> message populated
     * with the given argument data.
     * </p>
     * <p>
     * Creates a new client request identifier string with 
     * <code>{@link #newClientRequestId()}</code> then defers to 
     * <code>{@link #createRequest(IngestionFrame, int, String)}</code>.
     * </p> 
     * 
     * @param frame     source of all data used to populated returned message
     * @param recPrvUid the data provider UID used in the returned message
     * 
     * @return  new <code>IngestDataRequest</code> message populated with argument data
     * 
     * @throws MissingResourceException the argument had no timestamp assignments
     * @throws TypeNotPresentException  an unsupported data type was contained in the arugment data
     * @throws ClassCastException       bad type cast or structured data within argument was not converted
     * 
     * @see #createRequest(IngestionFrame, int, String)
     */
    public IngestDataRequest    createRequest(IngestionFrame frame, ProviderUID recPrvUid) 
            throws IllegalStateException, MissingResourceException, TypeNotPresentException, ClassCastException  {

        // Create a new request ID
        IngestRequestUID  recRqstUid = frame.getClientRequestUid();
        if (recRqstUid == null)
            recRqstUid = IngestRequestUID.random();
            

        // Create the message and return it
        IngestDataRequest    msgRqst = this.createRequest(frame, recPrvUid, recRqstUid);
        
        return msgRqst;
    }
    
    /**
     * <p>
     * Converts the given ingestion frame to a new, <code>IngestDataRequest</code> message populated
     * with the given argument data.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * All exceptions are generated by the use of the <code>{@link ProtoMsg#from(IngestionFrame)}</code>
     * method when converted frame data to message data.
     * </p>
     * 
     * @param frame     source of all data used to populated returned message
     * @param recPrvUid the data provider UID used in the returned message
     * @param recRqstId the client request ID used in the returned message
     * 
     * @return  new <code>IngestDataRequest</code> message populated with argument data
     * 
     * @throws MissingResourceException the argument had no timestamp assignments
     * @throws TypeNotPresentException  an unsupported data type was contained in the arugment data
     * @throws ClassCastException       bad type cast or structured data within argument was not converted
     * 
     * @see ProtoMsg#from(IngestionFrame)
     */
    public IngestDataRequest createRequest(IngestionFrame frame, ProviderUID recPrvUid, IngestRequestUID recRqstId) 
            throws ProviderException, MissingResourceException, TypeNotPresentException, ClassCastException  {
        
        IngestDataRequest   msgRqst = IngestDataRequest.newBuilder()
                .setProviderId(recPrvUid.uid())
                .setClientRequestId(recRqstId.requestId())
//                .setRequestTime(ProtoTime.now())
                .addAllAttributes(ProtoMsg.createAttributes(frame.getAttributes()))
                .setEventMetadata(IngestionFrameConverter.extractEventMetadata(frame))
                .setIngestionDataFrame(ProtoMsg.from(frame))
                .build();

        return msgRqst;
    }

    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Creates and returns a new <code>EventMetadata</code> message using the snapshot parameters
     * within the given ingestion frame.
     * </p>
     * <p>
     * The optional "snapshot" parameters from the given ingestion frame are used to populate the
     * return message.  The returned message will <code>null</code> values for any snapshot parameters
     * that have not been set.
     * </p>
     * 
     * @param frame     source of event metadata parameters
     * 
     * @return          new <code>EventMetadata</code> message populated from the argument
     */
    private static EventMetadata   extractEventMetadata(IngestionFrame frame) {
        
        EventMetadata.Builder   bldrMsg = EventMetadata.newBuilder();
        
        if (frame.getSnapshotId() != null)
            bldrMsg
            .setDescription(frame.getSnapshotId());
        
        if (frame.getSnapshotDomain() != null) 
            bldrMsg
            .setStartTimestamp( ProtoMsg.from(frame.getSnapshotDomain().begin()) )
            .setStopTimestamp( ProtoMsg.from(frame.getSnapshotDomain().end()) );
        
        EventMetadata   msgMetadata = bldrMsg.build();
        
        return msgMetadata;
    }

//    /**
//     * <p>
//     * Generates a new, unique client request identifier and returns it.
//     * </p>
//     * <p>
//     * <s>The returns request identifier is "unique" within the lifetime of the current
//     * Java Virtual Machine (JVM).  Request identifiers can, potentially, be repeated for
//     * independent JVMs and should not be used to uniquely identify request beyond the
//     * current JVM execution.</s>
//     * </p>
//     * <p>
//     * The Ingestion Service will accept only Universally Unique IDs for the client request
//     * identifier.  This implementation is now using the Java <code>{@link UUID}</code> 
//     * UUID generation tool.
//     * </p>
//     * <p>
//     * <h2>Computation</h2>
//     * The return value is computed by taking the hash code for the 
//     * <code>IngestionFrameProcessorDeprecated</code> class and incrementing it by the current value
//     * of the class frame counter <code>{@link #cntFrames}</code> (which is then incremented).
//     * The <code>long</code> value is then converted to a string value and returned.
//     * </p>
//     * <p>
//     * <h2>Thread Safety</h2>
//     * This method is thread safe.  Computation of the UID is synchronized with the class
//     * lock instance <code>{@link #objClassLock}</code>.  This is necessary since the
//     * <code>{@link #cntFrames}</code> class instance must be modified atomically.
//     * </p>
//     *  
//     * @return  a new client request ID unique within the execution of the current JVM
//     */
//    private static IngestRequestUID newClientRequestId() {
//
//        UUID    uuidRqst = UUID.randomUUID();
//        
//        String  strClientId = uuidRqst.toString();
//        
//        // hash code used as client ID seed
//        long lngHash;
//        
//        synchronized (objClassLock) {
//            lngHash = IngestionFrameConverter.class.hashCode(); 
//            lngHash += IngestionFrameConverter.cntFrames;
//            
//            IngestionFrameConverter.cntFrames++;
//        }
//        
//        String strClientId = Long.toString(lngHash);
//        
//        return uuidRqst;
//    }
    
}
