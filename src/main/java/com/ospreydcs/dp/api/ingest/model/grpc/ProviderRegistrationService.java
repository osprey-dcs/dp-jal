/*
 * Project: dp-api-common
 * File:	ProviderRegistrationService.java
 * Package: com.ospreydcs.dp.api.ingest.model.grpc
 * Type: 	ProviderRegistrationService
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
 * @since May 1, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.ingest.model.grpc;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ospreydcs.dp.api.common.AAdvancedApi;
import com.ospreydcs.dp.api.common.AAdvancedApi.STATUS;
import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.config.ingest.DpIngestionConfig;
import com.ospreydcs.dp.api.grpc.ingest.DpIngestionConnection;
import com.ospreydcs.dp.api.grpc.util.ProtoMsg;
import com.ospreydcs.dp.api.ingest.DpIngestionException;
import com.ospreydcs.dp.api.model.ProviderRegistrar;
import com.ospreydcs.dp.api.model.ProviderUID;
import com.ospreydcs.dp.api.util.JavaRuntime;
import com.ospreydcs.dp.grpc.v1.ingestion.RegisterProviderRequest;
import com.ospreydcs.dp.grpc.v1.ingestion.RegisterProviderResponse;

/**
 * <p>
 * Utility class for performing data provider registration with the Ingestion Service.
 * </p>
 * <p>
 * This is a convenience class for provider registration available while the Ingestion
 * Service undergoes development.  At the time of this class creation the Ingestion Service
 * does not currently support data provider registration (the RPC operation is available within
 * the interface but it is not implemented).  Thus, either this class will mock the 
 * registration process if it is not supported, or will perform the actual provider registration
 * if it is available.
 * </p> 
 * <p>
 * <h2>Usage</h2>
 * Invoke method <code>{@link #registerProvider(DpIngestionConnection, ProviderRegistrar)}</code>
 * to perform provider registration with Ingestion Service.  Upon first invocation the
 * availability of the provider registration service is tested.  If the service is available
 * the provider is registered using the given registration record.  If the service is not
 * available the registration process is mocked.
 * </p>
 * <p>
 * <h2>Provider Registration Test</h2>
 * Only a single test of provider registration availability is performed during the lifetime 
 * of the Java Virtual Machine (VM).  If the test has already been performed then the results
 * are returned immediately.  If the test has not been performed (i.e., upon the first
 * invocation of this method) an attempt is made to register the test data provider.
 * The results of the registration attempt are then returned.
 * </p>
 * <p>
 * The method <code>{@link #performRegisterProvider(DpIngestionConnection, ProviderRegistrar)}</code>
 * is called with the test provider registration record <code>{@link #REC_TEST_REGISTRAR}</code>.
 * <ul>
 * <li>
 * If no exceptions are thrown the registration process is assumed to be <b>implemented</b>.
 * </li>
 * <li>
 * If the method throws a <code>{@link io.grpc.StatusRuntimeException}</code> the registration
 * process is assumed to be <b>unimplemented</b> (although the exception message is not checked).
 * </li>
 * <li>
 * If the method throws a <code>{@link MissingResourceException}</code> the registration process
 * is assumed to be <b>implemented</b> but the registration of the test provider failed
 * (which is admittedly unusual).
 * </li>
 * </ul>
 * </p>
 * <p>
 * <h2>Mock Registration</h2>
 * If the Ingestion Service provider registration is not available the 
 * <code>{@link #registerProvider(DpIngestionConnection, ProviderRegistrar)}</code> will assign
 * a UID for the provider.  The UID is unique within this class and within the lifetime of the
 * current Java Virtual Machine.  The class maintains a collection of UIDs previously assigned
 * so that if the same provider registers more than once it will receive the same UID.
 * </p>
 *
 * @author Christopher K. Allen
 * @since May 1, 2024
 *
 */
@AAdvancedApi(status=STATUS.DEVELOPMENT, note="Mocks provider registration if not implemented by Ingestion Service")
public final class ProviderRegistrationService {

    
    //
    // Application Resources
    //
    
    /** Default Query Service configuration parameters */
    private static final DpIngestionConfig  CFG_DEFAULT = DpApiConfig.getInstance().ingest;
    
    
    //
    // Class Constants
    //
    
    /** Logging active flag */
    private static final boolean    BOL_LOGGING = CFG_DEFAULT.logging.active;
    
    
    /** Data Provider unique name - registration implementation available test */
    private static final String                 STR_PROVIDER_NAME = "PROVIDER_NAME_REGISTRATION_TEST";
    
    /** Data Provider attributes - registration implementation available test */
    private static final Map<String, String>    MAP_PROVIDER_ATTRS = Map.of(
                                                    "Class", ProviderRegistrationService.class.getSimpleName(),
                                                    "Package", ProviderRegistrationService.class.getPackageName(),
                                                    "Project", "dp-api-java",
                                                    "Objective", "Test provider registration available"
                                                );
    /** Data Provider registration record - registration implementation available test */
    private static final ProviderRegistrar      REC_TEST_REGISTRAR = ProviderRegistrar.from(STR_PROVIDER_NAME, MAP_PROVIDER_ATTRS);
    
    
    //
    // Class Resources
    //
    
    /** Class event logger */
    private static final Logger                 LOGGER = LogManager.getLogger();
    
    
    /** Map used to hold provider unique names for duration of this VM */
    private static final Map<String, Integer>   MAP_PROVIDER_UID = new HashMap<>();
    

    //
    // Class State Variables
    //
    
    /** Has provider registration implementation been tested */
    private static boolean  bolRegsitrationTested = false;
    
    /** Provider registration implementation available flag */
    private static boolean  bolRegistrationImplemented = false;

    
    //
    // Class Variables
    //
    
    /** The test provider's UID - valid if registration is implemented */
    private static ProviderUID  recProviderUid = null;
    
    /** Provider UID counter - Used to mock the provider registration process when unavailable */
    private static int          intProviderUidCounter = 1;
    

    
    //
    // Attribute Query
    //
    
    /**
     * <p>
     * Returns the <code>ProviderUID</code> record used in testing the availability of 
     * data provider registration implementation with the Ingestion Service.
     * </p>
     * <p>
     * <h2>NOTES:</h2>
     * The returned value will have a valid provider UID only if the following hold:
     * <ul>
     * <li>Data provider registration is implemented within the Ingestion Service.</li>
     * <li>The method <code>{@link #registerProvider(DpIngestionConnection, ProviderRegistrar)}</code> has been called.</li>
     * </ul>
     * If the method <code>{@link #registerProvider(DpIngestionConnection, ProviderRegistrar)}</code>
     * has not been invoked then an exception is thrown.
     * If the method has been called but provider registration is unavailable a <code>null</code>
     * value is returned.
     * </p>
     * 
     * @return  the <code>ProviderUID</code> record corresponding to <code>{@link #REC_TEST_REGISTRAR}</code>,
     *          or <code>null</code> if provider registration has not been implemented
     * 
     * @throws IllegalStateException method <code>{@link #registerProvider(DpIngestionConnection, ProviderRegistrar)}</code> not called
     */
    public static ProviderUID getTestProviderUid() throws IllegalStateException {
        
        // Check if provider registration test has been run
        if (!ProviderRegistrationService.bolRegsitrationTested)
            throw new IllegalStateException("Provider regisration implementation has not been tested.");
    
        return ProviderRegistrationService.recProviderUid;
    }
    
    
    //
    // Operations
    //
    
    /**
     * <p>
     * Performs or mocks the data provider registration task with the Ingestion Service.
     * </p>
     * <p>
     * First checks if the Ingestion Service provider registration has been implemented using
     * the given Ingestion Service connection.  
     * <ol>
     * <li>
     * If the registration service is available
     * the given provider registration record is used to obtain the provider UID which is
     * then returned as a client API record.  
     * </li>
     * <li>
     * If the registration service is not available the given provider registration record is
     * used to mock the behavior of the registration process.
     * </ol>
     * </p>  
     * <p>
     * 
     * @param recRegistration   data provider registration information (unique name)
     * 
     * @return  record containing unique identifier of the data provider with the Ingestion Service
     * 
     * @throws DpIngestionException either a gRPC runtime exception occurred or registration failed
     */
    public static ProviderUID registerProvider(DpIngestionConnection connIngest, ProviderRegistrar recRegistration) 
            throws DpIngestionException {

        // If provider registration is not implemented then mock behavior
        if (!ProviderRegistrationService.isRegistrationImplemented(connIngest)) {
            ProviderUID     recUid = ProviderRegistrationService.mockRegisterProvider(recRegistration);
            
            return recUid;
        }
        
        // Perform the registration request
        try {
            // Attempt blocking unary RPC call 
            ProviderUID recUid = ProviderRegistrationService.performRegisterProvider(connIngest, recRegistration);
            
            return recUid;
            
        } catch (io.grpc.StatusRuntimeException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                           + " - SERIOUS ERROR - gRPC threw runtime exception attempting to register provider: "
                           + "type=" + e.getClass().getName()
                           + ", details=" + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw new DpIngestionException(strMsg, e);
            
        } catch (MissingResourceException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - data provider registration failed with Ingestion Service: "
                    + e.getMessage();

            if (BOL_LOGGING)
                LOGGER.error(strMsg);

            throw new DpIngestionException(strMsg, e);
        }
    }
    
    
    //
    // Support Methods
    //
    
    /**
     * <p>
     * Determines whether or not data provider registration is available with the 
     * Ingestion Service.
     * </p>
     * <p>
     * Only a single test of provider registration availability is performed during the lifetime 
     * of the Java Virtual Machine (VM).  If the test has already been performed then the results
     * are returned immediately.  If the test has not been performed (i.e., upon the first
     * invocation of this method) an attempt is made to register the test data provider.
     * The results of the registration attempt are then returned.
     * </p>
     * <p>
     * <h2>Provider Registration Test</h2>
     * The method <code>{@link #performRegisterProvider(DpIngestionConnection, ProviderRegistrar)}</code>
     * is called with the test provider registration record <code>{@link #REC_TEST_REGISTRAR}</code>.
     * <ul>
     * <li>
     * If no exceptions are thrown the registration process is assumed to be <b>implemented</b>.
     * </li>
     * <li>
     * If the method throws a <code>{@link io.grpc.StatusRuntimeException}</code> the registration
     * process is assumed to be <b>unimplemented</b> (although the exception message is not checked).
     * </li>
     * </li>
     * If the method throws a <code>{@link MissingResourceException}</code> the registration process
     * is assumed to be <b>implemented</b> but the registration of the test provider failed
     * (which is admittedly unusual).
     * </li>
     * </ul>
     * </p>
     *  
     * @param connIngest    active connection to the Ingestion Service
     * 
     * @return  <code>true</code> if provider registration is available with the Ingestion Service,
     *          <code>false</code> otherwise
     */
    private static boolean isRegistrationImplemented(DpIngestionConnection connIngest) {
        
        // If we have already performed check return the results
        if (ProviderRegistrationService.bolRegsitrationTested)
            return ProviderRegistrationService.bolRegistrationImplemented;
        
        ProviderRegistrationService.bolRegsitrationTested = true;
        
        // Check if provider registration is implemented
        try {
            ProviderRegistrationService.recProviderUid = ProviderRegistrationService.performRegisterProvider(connIngest, REC_TEST_REGISTRAR);
            
            // If no exceptions were thrown provider registration is available
            ProviderRegistrationService.bolRegistrationImplemented = true;

            // If gRPC runtime exception is thrown this is likely due to unavailable service
        } catch (io.grpc.StatusRuntimeException e) {

            if (BOL_LOGGING)
                LOGGER.warn("Ingestion Service provider registration appears unimplemented, mocking registration: io.grpc.StatusRuntimeException = {}", e.getMessage());

            ProviderRegistrationService.bolRegistrationImplemented = false;
            
            // If MissingReourceException then registration available - but failed?
        } catch (MissingResourceException e) {
            
            if (BOL_LOGGING)
                LOGGER.warn("Ingestion Service provider registration appears implemented but test registration FAILED: MissingResourceException = {}", e.getMessage());

            ProviderRegistrationService.bolRegistrationImplemented = true;
        }
        
        return ProviderRegistrationService.bolRegistrationImplemented;
    }
    
    /**
     * <p>
     * Mocks the data provider registration task with the Ingestion Service.
     * </p>
     * <p>
     * Checks if the given data provider has already registered here, i.e., there is
     * a name entry in <code>{@link #MAP_PROVIDER_UID}</code>.  If so, its UID is
     * returned. If not, a new UID is created from the counter 
     * <code>{@link #intProviderUidCounter}</code>  and added to the map.
     * A <code>ProviderUID</code> record is created for the UID and returned.
     * </p>
     * 
     * @param recRegistration   data provider registration information (unique name)
     * 
     * @return  record containing UID for the data provider generated by this method 
     */
    private static ProviderUID mockRegisterProvider(ProviderRegistrar recRegistration) {

        String  strProviderName = recRegistration.name();
        int     intProviderUid;
        boolean bolIsNew;
        
        // Check if we have already registered provider
        if (MAP_PROVIDER_UID.containsKey(strProviderName)) {
            intProviderUid = MAP_PROVIDER_UID.get(strProviderName);
            bolIsNew = false;
            
        } else {
            intProviderUid = intProviderUidCounter;
            intProviderUidCounter++;
            
            MAP_PROVIDER_UID.put(strProviderName, Integer.valueOf(intProviderUid));
            bolIsNew = true;
        }
            
        // Create provider registration UID
        String          strProviderUid = Integer.toString(intProviderUid);
        ProviderUID     recUid = ProviderUID.from(strProviderUid, strProviderName, bolIsNew);
        
        return recUid;
    }

    /**
     * <p>
     * Performs the data provider registration task with the Ingestion Service.
     * </p>
     * <p>
     * The argument is converted to a <code>RegisterProviderRequest</code> message and
     * the <code>registerProvider()</code> operation is called directly on the blocking stub
     * of the gRPC ingestion connection given (this is a unary operation).  Any gRPC runtime
     * exceptions are caught.  The response is checked for exceptions then returned as
     * a <code>ProviderUID</code> record if successful.
     * </p>  
     * 
     * @param recRegistration   data provider registration information (unique name)
     * 
     * @return  record containing unique identifier of the data provider with the Ingestion Service
     * 
     * @throws  io.grpc.StatusRuntimeException  the provider registration service is unimplemented (probably)   
     * @throws  MissingResourceException        provider registration failed
     */
    private static ProviderUID performRegisterProvider(DpIngestionConnection connIngest, ProviderRegistrar recRegistration) 
            throws io.grpc.StatusRuntimeException, MissingResourceException {

        // Future Implementation of Provider Registration
        // Create the request message and response buffer
        RegisterProviderRequest     msgRqst = ProtoMsg.from(recRegistration);
        RegisterProviderResponse    msgRsp = null;
        
        // Perform the registration request
        try {
            // Attempt blocking unary RPC call 
            msgRsp = connIngest.getStubBlock().registerProvider(msgRqst);
            
        } catch (io.grpc.StatusRuntimeException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                           + " - gRPC threw runtime exception attempting to register provider: "
                           + "type=" + e.getClass().getName()
                           + ", details=" + e.getMessage();
            
            if (BOL_LOGGING)
                LOGGER.error(strMsg);
            
            throw e;
        }
     
        // Unpack the results - checking for failed registration
        ProviderUID recUid;
        
        try {
            recUid = ProtoMsg.toProviderUID(msgRsp);
            
        } catch (MissingResourceException e) {
            String  strMsg = JavaRuntime.getQualifiedMethodNameSimple()
                    + " - data provider registration failed: "
                    + e.getMessage();

            if (BOL_LOGGING)
                LOGGER.error(strMsg);

            throw e;
        }
        
        // Everything worked - return the provider UID
        return recUid;
    }

    
    //
    // Constructor
    //
    
    /**
     * <p>
     * Prevent construction of class instances.
     * </p>
     */
    private ProviderRegistrationService() {
    }

}
