/*
 * Project: dp-jal
 * File:	IngestChanTestSuite.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.channel
 * Type: 	IngestChanTestSuite
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
 * @since Feb 20, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.channel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.MalformedParametersException;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;

import javax.naming.ConfigurationException;

import com.ospreydcs.dp.jal.common.DpGrpcStreamType;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec;
import com.ospreydcs.dp.jal.tools.common.parse.AppOptionsParser;
import com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Class for generating test suites for application <code>IngestionChannelEvaluator</code>.
 * </p>
 * <p>
 * Class instances create collections of <code>{@link IngestChanTestCase}</code> records which define a test
 * case situation for evaluation. Test case parameters are enumerated within <code>{@link IngestChanTestParams}</code>.
 * </p>
 * <p>
 * <h2>Usage</h2>
 * Parameter values are added to the test suite using method 
 * <code>{@link TestSuiteGeneratorBase#addParameterValue(Enum, Object)}</code>.  
 * The <code>FrameProcTestSuite</code> instance is considered fully configured once all parameter values
 * for the test suite have been assigned.
 * After configuration test suites are generated with method <code>{@link TestSuiteGeneratorBase#createTestSuit()}</code>.
 * See the class documentation for <code>{@link TestSuiteGeneratorBase}</code> for detailed information on 
 * <code>FrameProcTestSuite</code> usage.
 * </p>
 * <p>
 * <h2>Operation</h2>
 * Most functionality is provided in base class <code>{@link TestSuiteGeneratorBase}</code>.  The base class provides
 * methods for configuring the test suite generator, confirming valid configurations, checking for missing parameter values,
 * retrieving parameter values, and obtaining the total test case count for the current configuration.
 * To support the base class operation the <code>FrameProcTestSuite</code> class supplies the abstract methods
 * <code>{@link #isValidType(IngestChanTestParams, Object)}</code> and <code>{@link #createTestCase(Map)}</code>, which
 * check that a given parameter value is of the correct type and creates a <code>{@link IngestChanTestCase}</code> from
 * a map of (Param, Value) pairs, respectively.  These abstract implementations are particular to the parameter
 * enumeration <code>{@link IngestChanTestParams}</code> and test case record <code>{@link IngestChanTestCase}</code>.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>At least one value must be assigned for each parameter in <code>{@link IngestChanTestParams}</code>, otherwise the configuration is invalid.</li>
 * <li>Method <code>{@link #isValidConfiguration()}</code> is available to check for valid configuration.</li>
 * <li>Test suite size grows geometrically with the number of parameter values, where total case count is the product of the number of values.</li>
 * <li>Method <code>{@link #testCaseCount()}</code> is available to check the number of test cases for the current configuration.</li>
 * </ul>
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Feb 20, 2026
 *
 * @see TestSuiteGeneratorBase
 */
public class IngestChanTestSuite extends TestSuiteGeneratorBase<IngestChanTestParams, IngestChanTestCase> {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new, uninitialized <code>IngestChanTestSuite</code> instance.
     * </p>
     * <p>
     * The returned test suite generator must configured with values for test parameters enumerated
     * within <code>{@link IngestChanTestParams}</code>.
     * Parameter values are assigned with base-class method 
     * <code>{@link TestSuiteGeneratorBase#addParameterValue(IngestChanTestParams, Object)}</code>.
     * Once populated test suites are generated with method
     * <code>{@link TestSuiteGeneratorBase#createTestSuit()</code>.
     * </p> 
     *  
     * @return  a new <code>IngestChanTestSuite</code> ready for parameter value population
     * 
     * @see TestSuiteGeneratorBase
     */
    public static IngestChanTestSuite   from() {
        return new IngestChanTestSuite();
    }
    
    /**
     * <p>
     * Creates and returns a new, fully configured <code>IngestChanTestSuite</code> instance.
     * </p>
     * <p>
     * The returned test suite generator is configured according to the given application command-line arguments.
     * So long as the command-line arguments were valid and correctly formatted the test suite generator is 
     * ready for test suite generation.
     * Test suites can be created with method <code>{@link TestSuiteGeneratorBase#createTestSuit()}</code>.
     * </p>
     * 
     * @param parser    command-line parser configured for <code>FrameProcessorEvaluator</code> options
     * @param args      the application command-line arguments
     * 
     * @return  a new <code>IngestChanTestSuite</code> instance fully configured from the given command-line arguments
     *  
     * @param args  the application command-line arguments
     * 
     * @throws ClassCastException       the <code>Param</code> enumeration does not implement <code>ITestParameter</code>
     * @throws IllegalArgumentException general error (typically bad argument type, bad argument count, enumeration constant not recognized)
     * @throws NoSuchMethodException    the Java class <code>{@link #getJavaType()}</code> does not contain method <code>valueOf(String)</code>
     * @throws SecurityException        the class loader denied access to method <code>valueOf(String)</code> (e.g., typically package access)
     * @throws IllegalAccessException   the method <code>valueOf(String)</code> is not accessible
     * @throws InvocationTargetException    the <code>valueOf(String)</code> method threw an exception (e.g., NumberFormatException)
     * @throws DateTimeParseException   invalid ISO-8605 date/time/duration format for 'period', 'start', or 'delay' 
     * @throws TypeNotPresentException  invalid enumeration constant (e.g., the 1st argument was not a <code>JalComplexType</code>)
     * @throws NumberFormatException    invalid numeric expression (typically for 'lngSeed' value)
     * @throws ConfigurationException   the argument contained the wrong number of arguments for the <code>JalComplexType</code>
     * @throws UnsupportedOperationException invalid field value format (typically 'numIncr' was invalid)
     * @throws MalformedParametersException  an enumeration constant within the argument set was not recognized (IMAGE)
     * @throws NoSuchElementException   the column data type was unrecognized (i.e., 'DTYPE' was not supported)
     * 
     * @see TestSuiteGeneratorBase
     */
    public static IngestChanTestSuite   from(AppOptionsParser parser, String...args) 
            throws DateTimeParseException, NumberFormatException, ClassCastException, 
                   UnsupportedOperationException, NoSuchMethodException, SecurityException, 
                   IllegalAccessException, InvocationTargetException, IllegalArgumentException, 
                   TypeNotPresentException, ConfigurationException, MalformedParametersException 
    {
        IngestChanTestSuite suite = IngestChanTestSuite.from();
        
        suite.parseParameterValues(parser, args);
        
        return suite;
    }
    

    //
    // Constructor
    //
    
    /**
     * <p>
     * Constructs a new <code>IngestChanTestSuite</code> instance.
     * </p>
     * <p>
     * Required of base class to obtain the test parameters enumeration class object.
     * </p>
     */
    protected IngestChanTestSuite() {
        super(IngestChanTestParams.class);
    }

    
    //
    // TestSuiteGeneratorBase Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#isValidType(java.lang.Enum, java.lang.Object)
     */
    @Override
    protected boolean isValidType(IngestChanTestParams enmParam, Object objVal) {
        return enmParam.isInstance(objVal);
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#createTestCase(java.util.Map)
     */
    @Override
    protected IngestChanTestCase createTestCase(Map<IngestChanTestParams, Object> mapTestVals)
            throws MissingResourceException, ClassCastException, UnsupportedOperationException {
        
        
        // Check for parameter completeness
        if (super.hasMissingParameters(mapTestVals))
            throw new MissingResourceException(
                        JavaRuntime.getQualifiedMethodNameSimple() + " - (Param, Value) map is missing parameter(s).",
                        Map.class.getName(), 
                        super.missingParameters(mapTestVals).toString()
                        );
        
        // Check for value completeness
        if (super.hasMissingValues(mapTestVals))
            throw new MissingResourceException(
                    JavaRuntime.getQualifiedMethodNameSimple() + " - (Param, Value) map is missing parameter value(s).",
                    Map.class.getName(), 
                    super.missingValues(mapTestVals).toString()
                    );
        
        // Create space for parameter values
        Boolean             bolColSerEnbl = null;   // enable/disable column serialization 
        DpGrpcStreamType    enmStrmType = null;     // the gRPC data stream type
        Boolean             bolMStrmEnbl = null;    // enable/disable multiple, concurrent gRPC data streams
        Integer             cntMStrmMax = null;     // maximum number of concurrent gRPC data streams
        Integer             cntFrames = null;         // number of ingestion frames in evaluation payload
        FrameFactorySpec    specFrame = null;         // ingestion frame definition (specification)

        // Assign parameter values from map entries
        for (Map.Entry<IngestChanTestParams, Object> entry : mapTestVals.entrySet()) {
            IngestChanTestParams    enmParam = entry.getKey();
            Object                  objVal = entry.getValue();
            
            switch (enmParam) {
            case COL_SER_ENBL:
                bolColSerEnbl = Boolean.class.cast(objVal);     // throws ClassCastException
                break;
            case STREAM_TYPE:
                enmStrmType = DpGrpcStreamType.class.cast(objVal);  // throws ClassCastException
                break;
            case MSTREAM_ENBL:
                bolMStrmEnbl = Boolean.class.cast(objVal);          // throws ClassCastException
                break;
            case MSTREAM_CNT:
                cntMStrmMax = Integer.class.cast(objVal);           // throws ClassCastException
                break;
            case FRAME_CNT:
                cntFrames = Integer.class.cast(objVal);             // throws ClassCastException
                break;
            case FRAME_DEF:
                specFrame = FrameFactorySpec.class.cast(objVal);      // throws ClassCastException
                break;
            default:
                throw new UnsupportedOperationException(JavaRuntime.getQualifiedMethodNameSimple() + " - Unrecognized parameter " + enmParam);
            }
        }
        
        // Create the test case and return
        IngestChanTestCase  recTestCase = IngestChanTestCase.from(bolColSerEnbl, enmStrmType, bolMStrmEnbl, cntMStrmMax, cntFrames, specFrame);
        
        return recTestCase;
    }

}
