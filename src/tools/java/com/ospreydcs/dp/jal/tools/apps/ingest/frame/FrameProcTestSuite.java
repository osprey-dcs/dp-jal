/*
 * Project: dp-jal
 * File:	FrameProcTestSuite.java
 * Package: com.ospreydcs.dp.jal.tools.apps.ingest.frame
 * Type: 	FrameProcTestSuite
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
 * @since Feb 5, 2026
 *
 */
package com.ospreydcs.dp.jal.tools.apps.ingest.frame;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import com.ospreydcs.dp.jal.tools.apps.ingest.common.FrameProcessorConfig;
import com.ospreydcs.dp.jal.tools.common.datagen.factories.specs.FrameFactorySpec;
import com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase;
import com.ospreydcs.dp.jal.util.JavaRuntime;

/**
 * <p>
 * Class for generating evaluation test suites for application <code>FrameProcessorEvaluator</code>.
 * </p>
 * <p>
 * Class instances create collections of <code>{@link FrameProcTestCase}</code> records which define a test
 * case situation for evaluation. Test case parameters are enumerated within <code>{@link FrameProcTestParam}</code>.
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
 * <code>{@link #isValidType(FrameProcTestParam, Object)}</code> and <code>{@link #createTestCase(Map)}</code>, which
 * check that a given parameter value is of the correct type and creates a <code>{@link FrameProcTestCase}</code> from
 * a map of (Param, Value) pairs, respectively.  These abstract implementations are particular to the parameter
 * enumeration <code>{@link FrameProcTestParam}</code> and test case record <code>{@link FrameProcTestCase}</code>.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>At least one value must be assigned for each parameter in <code>{@link FrameProcTestParam}</code>, otherwise the configuration is invalid.</li>
 * <li>Method <code>{@link #isValidConfiguration()}</code> is available to check for valid configuration.</li>
 * <li>Test suite size grows geometrically with the number of parameter values, where total case count is the product of the number of values.</li>
 * <li>Method <code>{@link #testCaseCount()}</code> is available to check the number of test cases for the current configuration.</li>
 * </ul>
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Feb 5, 2026
 *
 * @see TestCaseGeneratorBase
 */
public class FrameProcTestSuite extends TestSuiteGeneratorBase<FrameProcTestParam, FrameProcTestCase> {

    
    //
    // Creators
    //
    
    /**
     * <p>
     * Creates and returns a new, uninitialized <code>FrameProcTestSuite</code> instance.
     * </p>
     * <p>
     * The returned test suite generator must configured with values for test parameters enumerated
     * within <code>{@link FrameProcTestParam}</code>.
     * Parameter values are assigned with base-class method 
     * <code>{@link TestSuiteGeneratorBase#addParameterValue(FrameProcTestParam, Object)}</code>.
     * Once populated test suites are generated with method
     * <code>{@link TestSuiteGeneratorBase#createTestSuit()</code>.
     * </p> 
     *  
     * @return  a new <code>FrameProcTestSuite</code> ready for parameter value population
     * 
     * @see TestSuiteGeneratorBase
     */
    public static FrameProcTestSuite   from() {
        return new FrameProcTestSuite();
    }
    
    
    //
    // Constructors
    //
    
    /**
     * <p>
     * Constructs a new <code>FrameProcTestSuite</code> instance.
     * </p>
     */
    public FrameProcTestSuite() {
        super(FrameProcTestParam.class);
    }

    
    //
    // TestSuiteGeneratorBase Abstract Methods
    //
    
    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#isValidType(java.lang.Enum, java.lang.Object)
     */
    @Override
    protected boolean isValidType(FrameProcTestParam enmParam, Object objVal) {
        return enmParam.isInstance(objVal);
    }

    /**
     * @see com.ospreydcs.dp.jal.tools.common.score.TestSuiteGeneratorBase#createTestCase(java.util.Map)
     */
    @Override
    protected FrameProcTestCase createTestCase(Map<FrameProcTestParam, Object> mapTestVals)
            throws MissingResourceException, ClassCastException, UnsupportedOperationException {
        
        // Check for parameter completeness
        Set<FrameProcTestParam>  setMissingParam = TestSuiteGeneratorBase.missingParameters(FrameProcTestParam.class, mapTestVals);
        if (!setMissingParam.isEmpty())
            throw new MissingResourceException(
                        JavaRuntime.getQualifiedMethodNameSimple() + " - (Param, Value) map is missing parameter(s).",
                        Map.class.getName(), 
                        setMissingParam.toString()
                        );
        
        // Check for value completeness
        Set<FrameProcTestParam>  setMissingVals = TestSuiteGeneratorBase.missingValues(FrameProcTestParam.class, mapTestVals);
        if (!setMissingVals.isEmpty())
            throw new MissingResourceException(
                    JavaRuntime.getQualifiedMethodNameSimple() + " - (Param, Value) map is missing parameter value(s).",
                    Map.class.getName(), 
                    setMissingVals.toString()
                    );
        
        // Create space for parameter values
        Boolean             bolColSerEnbl = null;   // column serialization enable/disable
        Boolean             bolMThrdEnbl = null;    // multi-threaded processing enable/disable
        Integer             cntThrdsMax = null;     // maximum allowable multi-threaded thread count
        Boolean             bolDcmpEnbl = null;     // ingestion frame decomposition enable/disable
        Integer             szDcmpMax = null;       // maximum composite ingestion frame size (bytes)
        FrameFactorySpec    specFrm = null;         // ingestion frame definition (specification)
        Integer             cntFrms = null;         // number of ingestion frames in evaluation payload
        
        for (Map.Entry<FrameProcTestParam, Object> entry : mapTestVals.entrySet()) {
            FrameProcTestParam      enmParam = entry.getKey();
            Object                  objVal = entry.getValue();
            
            switch (enmParam) {
            case COL_SER_ENBL:
                bolColSerEnbl = Boolean.class.cast(objVal);    // throws ClassCastException
                break;
            case MTHREAD_ENABLE:
                bolMThrdEnbl = Boolean.class.cast(objVal);      // throws ClassCastException
                break;
            case MTHREAD_COUNT:
                cntThrdsMax = Integer.class.cast(objVal);       // throws ClassCastException
                break;
            case DCMP_ENABLE:
                bolDcmpEnbl = Boolean.class.cast(objVal);       // throws ClassCastException
                break;
            case DCMP_SIZE:
                szDcmpMax = Integer.class.cast(objVal);         // throws ClassCastException
                break;
            case FRAME_DEF:
                specFrm = FrameFactorySpec.class.cast(objVal);  // throws ClassCastException
                break;
            case FRAME_CNT:
                cntFrms = Integer.class.cast(objVal);           // throws ClassCastException
                break;
            default:
                throw new UnsupportedOperationException(JavaRuntime.getQualifiedMethodNameSimple() + " - Unrecognized parameter " + enmParam);
            }
        }
        
        // Create the test case and return it
        FrameProcessorConfig    recProcCfg = FrameProcessorConfig.from(bolColSerEnbl, bolDcmpEnbl, szDcmpMax, bolMThrdEnbl, cntThrdsMax);
        FrameProcTestCase       recTestCase = FrameProcTestCase.from(cntFrms, specFrm, recProcCfg);
        
        return recTestCase;
    }

}
